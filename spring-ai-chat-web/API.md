# Spring AI Chat Web 前后端接口文档

前端工程：`spring-ai-chat-web`  
前端地址：`http://localhost:5174`  
后端地址：`http://localhost:8083`  
联调方式：**Spring CORS**（前端直接请求 8083，不使用 Vite 代理）

> 本文件只描述接口。前端代码已经完成，后端代码未做任何修改。

---

## 1. 启动前端

```bash
cd /Users/Wys/Desktop/MAVEN/PCSpringAI/spring-ai-chat-web
npm install
npm run dev
```

打开 `http://localhost:5174`。

API 地址配置在 `.env.development`：

```env
VITE_API_BASE_URL=http://localhost:8083
```

生产构建：

```bash
npm run build
```

产物在 `dist/`。

---

## 2. Spring CORS 配置

前端 `5174` 和后端 `8083` 端口不同，浏览器会拦截跨域响应。在后端新增一个配置类：

```java
package com.darklands.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5174", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
```

说明：

- `allowedOrigins` 必须写具体前端地址，不能写 `*` 同时又开 `allowCredentials(true)`。
- 本项目不需要 Cookie，所以没有开 `allowCredentials`。
- 如果前端端口变了（例如 5175），这里要同步修改。
- 如果以后加了 JWT 请求头，浏览器会先发 `OPTIONS` 预检，`allowedMethods` 和 `allowedHeaders` 已经覆盖。
- 如果你使用 Spring Security，需要额外放行 `OPTIONS` 请求。

---

## 3. 通用约定

### 3.1 流式接口

四个聊天接口都是 `GET`，返回类型为：

```java
produces = "text/html;charset=UTF-8"
```

Spring MVC 返回 `Flux<String>`，每个字符串是模型返回的一小段文本。前端使用 `fetch` + `ReadableStream` 读取原始文本块，**不是 SSE**，所以没有 `data:` 前缀。

示例：

```java
@GetMapping(value = "/chat/stream", produces = "text/html;charset=UTF-8")
public Flux<String> chat(@RequestParam String prompt,
                         @RequestParam String chatId) {
    return chatClient.prompt()
            .user(prompt)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
            .stream()
            .content();
}
```

> 如果你更想用标准 SSE（`text/event-stream` + `data:`），前后端要一起改。当前前端按课程风格读取原始文本流。

### 3.2 普通 JSON 接口

历史记录、上传结果等返回：

```text
Content-Type: application/json;charset=UTF-8
```

消息结构统一为：

```json
[
  { "role": "user", "content": "你好" },
  { "role": "assistant", "content": "你好！有什么可以帮你的？" }
]
```

### 3.3 chatId

- 前端为每个会话生成一个稳定的 `chatId`。
- 同一个 `chatId` 的请求共享 `ChatMemory` 上下文。
- 后端把它作为 `ChatMemory.CONVERSATION_ID`。
- 不要信任前端传来的 `chatId` 做越权访问；如果以后接入登录，要校验该会话是否属于当前用户。

### 3.4 错误

流式接口失败时返回 4xx/5xx 即可，前端会显示：

> 请求失败（HTTP xxx）。请确认后端接口已启动，并已按 API.md 配置 Spring CORS。

历史接口失败时前端会静默返回空数组，建议没有数据时返回 `[]` 而不是 404。

---

## 4. 接口总览

| 模块 | 方法 | 路径 | 返回 |
|---|---|---|---|
| 对话机器人 | GET | `/ai/chat/stream` | 文本流 |
| 对话机器人 | GET | `/ai/history/chat` | JSON 数组 |
| 对话机器人 | GET | `/ai/history/chat/{chatId}` | JSON 消息数组 |
| 哄哄模拟器 | GET | `/ai/game/stream` | 文本流 |
| 哄哄模拟器 | GET | `/ai/history/honghong` | JSON 数组（可选） |
| 哄哄模拟器 | GET | `/ai/history/honghong/{chatId}` | JSON 消息数组（可选） |
| 智能客服 | GET | `/ai/service/stream` | 文本流 |
| 智能客服 | GET | `/ai/history/service` | JSON 数组 |
| 智能客服 | GET | `/ai/history/service/{chatId}` | JSON 消息数组 |
| ChatPDF | POST | `/ai/pdf/upload/{chatId}` | JSON |
| ChatPDF | GET | `/ai/pdf/chat/stream` | 文本流 |
| ChatPDF | GET | `/ai/pdf/preview/{chatId}` | `application/pdf` |
| ChatPDF | GET | `/ai/history/chatPDF` | JSON 数组（可选） |
| ChatPDF | GET | `/ai/history/chatPDF/{chatId}` | JSON 消息数组（可选） |

---

## 5. 对话机器人

### 5.1 流式对话

```http
GET /ai/chat/stream?prompt=你好&chatId=chat_1720000000000_abc123
```

参数：

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `prompt` | string | 是 | 用户输入 |
| `chatId` | string | 是 | 会话 ID |

响应：`text/html;charset=UTF-8` 文本流。

后端参考：

```java
@GetMapping(value = "/chat/stream", produces = "text/html;charset=UTF-8")
public Flux<String> chat(@RequestParam String prompt,
                         @RequestParam String chatId) {
    chatHistoryRepository.save("chat", chatId);

    return chatClient.prompt()
            .user(prompt)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
            .stream()
            .content();
}
```

`ChatClient` 需要挂上记忆 Advisor：

```java
@Bean
public ChatMemory chatMemory() {
    return MessageWindowChatMemory.builder()
            .maxMessages(20)
            .build();
}

@Bean
public ChatClient chatClient(DeepSeekChatModel model, ChatMemory chatMemory) {
    return ChatClient.builder(model)
            .defaultAdvisors(
                    new SimpleLoggerAdvisor(),
                    MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .build();
}
```

### 5.2 会话列表

```http
GET /ai/history/chat
```

响应支持两种格式，前端都能解析：

```json
["chat_1720000000000_abc123", "chat_1720000001000_def456"]
```

或：

```json
[
  { "chatId": "chat_1720000000000_abc123", "title": "解释一下 IoC" },
  { "chatId": "chat_1720000001000_def456", "title": "Spring 事务" }
]
```

### 5.3 会话消息

```http
GET /ai/history/chat/{chatId}
```

响应：

```json
[
  { "role": "user", "content": "你好" },
  { "role": "assistant", "content": "你好！有什么可以帮你的？" }
]
```

如果直接用 `ChatMemory`：

```java
List<Message> messages = chatMemory.get(chatId);
```

转换成 `{role, content}` 返回即可。内存版 `ChatMemory` 重启会丢；要持久化就换 JDBC repository 或自己建表。

---

## 6. 哄哄模拟器

### 6.1 流式对话

```http
GET /ai/game/stream?prompt=宝贝我错了&chatId=honghong_1720000000000_abc123
```

响应：文本流。前端会从完整回答里解析两行：

```text
（心情）你还知道错啊，那你说说错哪了？
得分：+5
原值：25/100
```

前端正则：

- `原值：25/100` → 更新进度条
- `得分：+5` → 显示本次得分
- 如果没有 `原值`，就把 `得分` 加到当前值上

后端系统提示词必须要求模型严格按这个格式输出，否则分数不会变化。

### 6.2 历史（可选）

```http
GET /ai/history/honghong
GET /ai/history/honghong/{chatId}
```

格式与对话机器人一致。当前前端只使用单局游戏，不强制要求历史接口。

---

## 7. 智能客服

### 7.1 流式对话

```http
GET /ai/service/stream?prompt=有哪些 Java 课程&chatId=service_1720000000000_abc123
```

响应：文本流。前端不关心工具调用细节，只展示最终文本。

后端参考：

```java
@GetMapping(value = "/service/stream", produces = "text/html;charset=UTF-8")
public Flux<String> service(@RequestParam String prompt,
                            @RequestParam String chatId) {
    chatHistoryRepository.save("service", chatId);

    return serviceChatClient.prompt()
            .user(prompt)
            .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
            .stream()
            .content();
}
```

`serviceChatClient` 需要挂工具：

```java
@Bean
public ChatClient serviceChatClient(DeepSeekChatModel model,
                                    ChatMemory chatMemory,
                                    CourseTools courseTools) {
    return ChatClient.builder(model)
            .defaultSystem("你是课程顾问小黑，请根据工具查询结果回答。")
            .defaultAdvisors(
                    new SimpleLoggerAdvisor(),
                    MessageChatMemoryAdvisor.builder(chatMemory).build()
            )
            .defaultTools(courseTools)
            .build();
}
```

### 7.2 历史

```http
GET /ai/history/service
GET /ai/history/service/{chatId}
```

格式同上。

---

## 8. ChatPDF

> ChatPDF 需要一个 **EmbeddingModel**。`spring-ai-starter-model-deepseek` 只提供 ChatModel，不提供 Embedding。你需要额外接入一个 OpenAI 兼容的 embedding 服务、Ollama，或其他向量模型。

### 8.1 上传 PDF

```http
POST /ai/pdf/upload/{chatId}
Content-Type: multipart/form-data

file: <PDF 文件>
```

响应：

```json
{
  "fileName": "操作系统.pdf",
  "pages": 32
}
```

前端只用 `fileName` 和 HTTP 状态码；`pages` 可选。

后端参考：

```java
@PostMapping("/upload/{chatId}")
public Map<String, Object> upload(@PathVariable String chatId,
                                  @RequestParam("file") MultipartFile file) {
    // 1. 保存文件
    // 2. PDFBox 或 PagePdfDocumentReader 解析
    // 3. 切分 → Embedding → VectorStore.add()
    // 4. 把 fileName 和 chatId 关联起来
    return Map.of("fileName", file.getOriginalFilename());
}
```

### 8.2 RAG 问答

```http
GET /ai/pdf/chat/stream?prompt=这份文档的核心结论是什么&chatId=pdf_1720000000000_abc123
```

响应：文本流。

后端建议：

- `PagePdfDocumentReader` 逐页读取 PDF
- `SimpleVectorStore` 或 Redis/其他 VectorStore 存储向量
- `QuestionAnswerAdvisor` 挂到 `ChatClient`
- 检索时按 `chatId` 或 `fileName` 过滤，确保只查当前 PDF

```java
@Bean
public ChatClient pdfChatClient(DeepSeekChatModel model,
                                ChatMemory chatMemory,
                                VectorStore vectorStore) {
    return ChatClient.builder(model)
            .defaultSystem("请只根据检索到的 PDF 内容回答，找不到就说不知道。")
            .defaultAdvisors(
                    new SimpleLoggerAdvisor(),
                    MessageChatMemoryAdvisor.builder(chatMemory).build(),
                    QuestionAnswerAdvisor.builder(vectorStore)
                            .searchRequest(SearchRequest.builder()
                                    .similarityThreshold(0.6)
                                    .topK(4)
                                    .build())
                            .build()
            )
            .build();
}
```

### 8.3 PDF 预览

```http
GET /ai/pdf/preview/{chatId}
```

响应：

```text
Content-Type: application/pdf
Content-Disposition: inline; filename="xxx.pdf"
```

前端用 `<iframe>` 直接加载这个地址。不要设置 `Content-Disposition: attachment`，否则浏览器会下载而不是预览。

### 8.4 历史（可选）

```http
GET /ai/history/chatPDF
GET /ai/history/chatPDF/{chatId}
```

格式同上。当前前端每个 PDF 使用一个新 `chatId`，历史接口不是必须的。

---

## 9. 建议的后端实现顺序

1. **CORS**：先让前端能请求到 8083，哪怕接口返回 404。
2. **ChatMemory + ChatClient**：加 `MessageWindowChatMemory` 和 `MessageChatMemoryAdvisor`。
3. **对话机器人**：`/ai/chat/stream` + 两个历史接口。
4. **哄哄模拟器**：复制 ChatClient，换系统提示词，端点 `/ai/game/stream`。
5. **智能客服**：加 `@Tool` 方法和 `defaultTools(...)`，端点 `/ai/service/stream`。
6. **ChatPDF**：接入 Embedding、PDF Reader、VectorStore、QuestionAnswerAdvisor。

---

## 10. 前端目录

```text
spring-ai-chat-web/
├── API.md
├── package.json
├── vite.config.js
├── .env.development
└── src/
    ├── main.js
    ├── App.vue
    ├── api/client.js
    ├── composables/useChatStream.js
    ├── components/
    │   ├── AppLayout.vue
    │   ├── ChatPanel.vue
    │   ├── ChatSessionView.vue
    │   └── SessionSidebar.vue
    ├── router/index.js
    ├── styles/global.css
    └── views/
        ├── HomeView.vue
        ├── ChatBotView.vue
        ├── GameView.vue
        ├── ServiceView.vue
        └── PdfView.vue
```

前端没有修改 `spring-ai-chat` 后端目录，也没有引入 Nginx 或 Vite 代理。跨域完全由后端 CORS 解决。

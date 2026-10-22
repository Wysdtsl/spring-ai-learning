package com.darklands.chat.controller;

import com.darklands.chat.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import static java.util.Arrays.stream;

/**
 * 课程风格：本模块只配置了 DeepSeek 一个模型，可以直接注入具体类型
 * 测试：GET http://localhost:8083/ai/chat?message=你好
 */
//与模型直接对话 调用call方法
@RequiredArgsConstructor
/*
@RequiredArgsConstructor 只把这两类字段放进构造函数：
final 字段
标了 @NonNull 的字段*/
@RequestMapping("/ai")
@RestController
public class ChatController {

    /*
    *
    * DeepSeekChatModel = 一台已经连上 DeepSeek 的电话
    * ChatClient = 一个帮你打电话的秘书
    *
    * ChatClient 内部还是用 DeepSeekChatModel 打电话，只是它帮你把一些事做了：
    自动加默认 System Prompt
    统一设置参数
    以后接 RAG、聊天记忆、工具调用更方便
    * */

   /* *//*字段注入*//*
    @Autowired
    private DeepSeekChatModel deepSeekChatModel;*/

    /*构造函数产生的是 ChatController 对象；chatClient 只是造这个对象时传进去的“零件”。
    *
    * 1. 容器里已经有一个 ChatClient Bean
      ChatClient chatClientBean = ...;
      2. Spring 创建 ChatController，把 ChatClient 传进去
      ChatController controller = new ChatController(chatClientBean);
      3. 把创建好的 ChatController 放进容器
      *
      * 构造函数的作用是：造一个 ChatController，并告诉它“以后你用这个 chatClient 干活”。
      * 而 chat() 方法内部，才使用 this.chatClient：
        chatClient.prompt().user(message).call().content();
      *
    * */

    /*你访问 /chat 时，Spring 调用的是 ChatController 的 chat() 方法*/
    private final ChatClient chatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return chatClient.prompt().user(message)
                .call()//给模型打电话
                .content();//拿到响应结果。
    }

    /**
     * 流式对话：每个元素是一小段文本增量
     * 测试：curl -N 'http://localhost:8083/ai/chat/stream?message=你好'
     */


  /*  @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatResponse> chatStream(@RequestParam("message") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        Flux<ChatResponse> stream = chatClient.stream(prompt);


        * data:{"result":{"output":{"text":"你好"}}}
          data:{"result":{"output":{"text":"，我是"}}}
          data:{"result":{"output":{"text":"DeepSeek。"}}}
        *  前端收到的是这种格式。

        可以stream.map(resp -> resp.getResult().getOutput().getText())  // 取这一段文本
        return stream;
    }

    @GetMapping(value = "/chat/stream2", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream2(@RequestParam("message") String message) {
        Prompt prompt = new Prompt(new UserMessage(message));
        Flux<ChatResponse> stream = chatClient.stream(prompt);
        Flux<String> stream2 = stream.map(resp -> resp.getResult().getOutput().getText());


        * data:{"result":{"output":{"text":"你好"}}}
          data:{"result":{"output":{"text":"，我是"}}}
          data:{"result":{"output":{"text":"DeepSeek。"}}}
        *  前端收到的是这种格式。

        可以stream.map(resp -> resp.getResult().getOutput().getText())  // 取这一段文本
        return stream2;*/

        //与模型对话 调用stream方法
            /*整体流程：
            * Controller 传入 chatId
          → ChatClient 发起请求
          → MessageChatMemoryAdvisor 拦截
          → 从 ChatMemory 读取该 chatId 的历史
          → 拼进本次 Prompt
          → 调用 DeepSeek
          → 拿到回复
          → Advisor 把「用户消息 + 助手回复」写回 ChatMemory*/

    /*
       1. Controller 调用 chatClient.prompt()
        2. ChatClient 组装 Prompt
        3. ChatClient 创建 ChatClientRequest（prompt + context）
        4. 把 request 传给 Advisor 链
        5. MemoryAdvisor 读 context，取历史，改 prompt
        6. LoggerAdvisor 打印 request
        7. ChatModel 收到 request.prompt()，调用 DeepSeek
        8. 模型返回 ChatResponse
        9. 包装成 ChatClientResponse
        10. LoggerAdvisor 打印 response
        11. MemoryAdvisor 把本轮消息写回 ChatMemory
        12. ChatClient 把最终结果返回 Controller

        不管有没有 .advisors(a -> a.param(...))，ChatClient 每次调用都会组装请求。
.advisors(a -> a.param(...)) 不是在“组装请求”，它只是往请求的 context 里放一个参数。
    * */
    //会话记忆调用层
    @RequestMapping(value = "/chat/stream", produces = "text/html;charset=utf-8")
    public Flux<String> chatStream(@RequestParam("prompt") String prompt,
                                   @RequestParam("chatId") String chatId) {
            //1、保存会话id
        chatHistoryRepository.save("chat", chatId);
           //2、请求模型
        return chatClient.prompt().
                user(prompt).
                advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                /*含义：
                a 是 Advisor 上下文构建器。
                param(...) 往上下文里放一个键值对。
                ChatMemory.CONVERSATION_ID 是约定的 key。
                chatId 是本次会话的 ID。
                MessageChatMemoryAdvisor 就是靠这个 key 找到对应的历史消息。
                如果不传这个参数，Advisor 不知道去哪个会话取历史，记忆就不会按预期工作。*/
                .stream()
                .content();

    }


    }
/*
* 会话日志：
* 跟大模型所有的会话都是由spring ai帮我们去做的，所以最终我们发出去的提示词长什么样子
* 大模型给我们返回了什么结果 我们全都不知道。为了解决这个问题
* 就要学习怎么在spring ai 中去实现与 ai会话时的日志功能
*
* SpringAi利用AOP原理提供了AI会话时的拦截、增强等功能 也就是Advisor（通知）
* */

/*
* 会话记忆：
* 定义会话存储方式:在将记忆拼接成提示词之前，先将会话记忆存储起来
* 配置会话记忆Advisor：配置Advisor 让SpringAI去处理会话记忆
* 添加会话id：在每一次发请求的时候添加会话id。用来管理会话记忆
*
* 关于定义会话存储方式，SpringAI定义了一个标准接口ChatMemory 里面有3个方法。
* add(String conversationId, List<Message> message) conversationId：会话id messages：消息 里面有消息的角色 消息的内容等等
* List<Message> get(String conversationId,in lastN)：根据会话id获取会话记忆
* clear(String conversationId)：根据会话id清空会话记忆
* */


/*提示词工程：通过优化提示词，使大模型生成出尽可能理想的内容，这一过程就叫提示词工程。
* 1、清晰明确的指令
* 2、使用分隔符标记输入
* 3、按步骤拆解复杂任务
* 4、提供输入输出示例
* 5、明确要求输出格式
* 6、给模型设定一个角色。*/


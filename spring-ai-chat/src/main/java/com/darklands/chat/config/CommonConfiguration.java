package com.darklands.chat.config;


import com.darklands.chat.constants.SystemConstants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration//告诉 Spring“这是一个配置类，里面可能有 @Bean 方法，请把它们注册到 IoC 容器里”。
public class CommonConfiguration {



    /*
    * ChatMemory：会话记忆的接口，按 conversationId 存取消息。
        MessageWindowChatMemory：只保留最近 N 条消息的滑动窗口。
        maxMessages(20)：每个会话最多保留 20 条消息，大约 10 轮问答。超过后，旧消息会被挤掉。
        默认使用内存存储，应用重启后历史丢失。
        它内部大致相当于：

          Map<String, List<Message>>
            key   = chatId
            value = 该会话的消息列表
    * */
    //会话记忆存储层
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)   // 只保留最近 20 条
                .build();
        /*
        * chatId = chat_001
        第 1 轮之后：
          [用户1, 助手1]
        第 2 轮之后：
          [用户1, 助手1, 用户2, 助手2]   ← 窗口满了
        第 3 轮请求时：
          当前窗口历史 = [用户1, 助手1, 用户2, 助手2]
          本次新消息   = 用户3
          拼给模型的 Prompt：
            [用户1, 助手1, 用户2, 助手2, 用户3]
        模型回复助手3后：
          写入 [用户3, 助手3]
          总数变成 6 条，超过 4
          淘汰最早的一轮 [用户1, 助手1]
        第 3 轮结束后，窗口变成：
          [用户2, 助手2, 用户3, 助手3]
                * */
    }



    @Bean//把这个方法返回的对象交给 Spring 保管 这个bean和ioc的bean是同一个
    @Primary//有多个 ChatClient 时，默认注入这个（DeepSeek）
    public ChatClient chatClient(DeepSeekChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                //拦截层
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),//配置内存Advisor MessageChatMemoryAdvisor：自动读写记忆
                        /*含义：
                        MessageChatMemoryAdvisor 是一个 Advisor，相当于模型调用前后的拦截器。
                        请求前：根据 chatId 从 ChatMemory 取历史消息，拼到本次 Prompt 里。
                        请求后：把本轮的用户消息和助手回复写回 ChatMemory。
                        流式输出时，它会在流结束后保存完整回复，不会只存半截。
                        SimpleLoggerAdvisor 放在后面，是为了让日志看到“已经拼好历史”的请求。Advisor 的顺序会影响日志内容。
                        */
                        new SimpleLoggerAdvisor())//配置日志Advisor
                .build(); //配置客户端
        /*
        * defaultAdvisors 这个是不是就是在给客户端增加一个功能：每次在请求发给模型前处理和模型处理后返回前端之前处理
        * defaultAdvisors 是在“模型调用”前后做处理，不是在“HTTP 响应”前后做处理。
        * */

        /*发送请求的能力是spring-ai-starter-model-deepseek
                             ↓
          DeepSeekChatModel（真正发 HTTP 请求、调用模型）

          只要注入DeepSeekChatModel 就可以发请求

          ChatClient 的意义是什么
          它的意义是：让“发请求”这件事更省事、更统一、更好扩展。
          还是用电话和秘书的比喻：
          DeepSeekChatModel = 电话，给你“能打电话”的能力
          ChatClient = 秘书，不增加打电话的能力，但帮你把常用的事做好
          */


    }

    /**
     * 哄哄模拟器专用：智谱 GLM（OpenAI 兼容接口）
     */
    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model, ChatMemory chatMemory) {
        return ChatClient.builder(model)
                .defaultSystem(SystemConstants.GAME_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
}

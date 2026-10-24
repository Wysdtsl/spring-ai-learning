package com.darklands.chat.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class GameController {
    private final ChatClient gameChatClient;

    public GameController(@Qualifier("gameChatClient") ChatClient gameChatClient) {
        this.gameChatClient = gameChatClient;
    }


    @RequestMapping(value = "/game/stream", produces = "text/plain;charset=utf-8")
    public Flux<String> chatStream(@RequestParam("prompt") String prompt,
                                   @RequestParam("chatId") String chatId) {
        //2、请求模型
        return gameChatClient.prompt().
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

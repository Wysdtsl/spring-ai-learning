package com.darklands.chat.controller;

import com.darklands.chat.repository.ChatHistoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 智能客服。
 *
 * serviceChatClient 在 CommonConfiguration 中创建，挂了 CourseTools；
 * 模型会在需要时自动调用 queryCourse、querySchool、queryReservation、createReservation。
 */
@RestController
@RequestMapping("/ai")
public class ServiceController {

    private final ChatClient serviceChatClient;
    private final ChatHistoryRepository chatHistoryRepository;

    public ServiceController(@Qualifier("serviceChatClient") ChatClient serviceChatClient,
                             ChatHistoryRepository chatHistoryRepository) {
        this.serviceChatClient = serviceChatClient;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @GetMapping(value = "/service/stream", produces = "text/plain;charset=UTF-8")
    public Flux<String> service(@RequestParam("prompt") String prompt,
                                @RequestParam("chatId") String chatId) {
        chatHistoryRepository.save("service", chatId);

        return serviceChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }
}

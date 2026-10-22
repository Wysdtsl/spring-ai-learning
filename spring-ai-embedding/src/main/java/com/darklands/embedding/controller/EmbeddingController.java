package com.darklands.embedding.controller;

import com.darklands.embedding.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试：GET http://localhost:8084/ai/embed?text=植物是怎样制造有机物的
 * 期望返回内容最接近的文本
 */
@RequestMapping("/ai")
@RestController
public class EmbeddingController {

    @Autowired
    private EmbeddingService embeddingService;

    @GetMapping("/findsim")
    public String findBestMatch(@RequestParam("text") String text) {

        return embeddingService.findBestMatch(text);
    }
}

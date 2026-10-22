package com.darklands.embedding.service;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 课程示例：用户输入一段话，从若干文本中找出语义最接近的一条
 */
@Service
public class EmbeddingService {

    /**
     * 候选文本集合（课程中为 3 个 docx 文本）
     */
    private final List<String> texts = List.of(
            "地球是太阳系中唯一已知存在生命的行星，表面约71%被水覆盖。",
            "光合作用是植物利用光能将二氧化碳和水转化为有机物并释放氧气的过程。",
            "计算机的冯·诺依曼体系结构由运算器、控制器、存储器、输入设备和输出设备组成。"
    );

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    /**
     * 找出与用户输入语义最相似的文本
     */
    public String findBestMatch(String input) {
        float[] inputVector = embeddingModel.embed(input);

        double bestScore = -1;
        String bestText = null;
        for (String text : texts) {
            float[] textVector = embeddingModel.embed(text);//对集合里每一行的内容进行向量化？
            double score = cosine(inputVector, textVector);
            if (score > bestScore) {
                bestScore = score;
                bestText = text;
            }
        }
        return bestText;
    }

    /**
     * 余弦相似度：点积 ÷ 模长乘积
     */
    private double cosine(float[] a, float[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}

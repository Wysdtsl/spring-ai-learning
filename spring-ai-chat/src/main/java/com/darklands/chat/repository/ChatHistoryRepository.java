package com.darklands.chat.repository;

import java.util.List;

public interface ChatHistoryRepository {

    /**
     * 记录某个模块下的一个会话
     * @param type   chat / honghong / service / chatPDF
     * @param chatId 会话 ID
     */
    void save(String type, String chatId);

    /**
     * 查询某个模块下的所有会话 ID
     */
    List<String> getChatIds(String type);
}
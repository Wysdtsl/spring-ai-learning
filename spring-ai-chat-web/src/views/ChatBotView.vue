<template>
  <ChatSessionView
    type="chat"
    endpoint="/ai/chat/stream"
    title="对话机器人"
    subtitle="带会话记忆的智能编码助手。每个 chatId 对应一段独立上下文。"
    welcome-title="有什么可以帮你的？"
    welcome-desc="我会记住当前会话的上下文。点击“新建对话”会切换到新的 chatId，记忆也会随之切换。"
    placeholder="输入你的问题，例如：解释一下 Spring 的 IoC"
  />
</template>

<script setup>
import ChatSessionView from '../components/ChatSessionView.vue'
</script>

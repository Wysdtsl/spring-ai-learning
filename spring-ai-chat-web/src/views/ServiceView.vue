<template>
  <ChatSessionView
    type="service"
    endpoint="/ai/service/stream"
    title="智能客服"
    subtitle="基于 Tool Calling 的课程顾问。模型可以调用 Java 方法查询课程、校区和预约。"
    welcome-title="你好，我是课程顾问小黑"
    welcome-desc="可以问我课程、校区和预约相关的问题。比如：“有哪些 Java 课程？”"
    placeholder="例如：帮我查一下北京校区的课程"
  />
</template>

<script setup>
import ChatSessionView from '../components/ChatSessionView.vue'
</script>

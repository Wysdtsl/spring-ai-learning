<template>
  <div class="page">
    <header class="page-header">
      <div>
        <h1 class="page-title">{{ title }}</h1>
        <p class="page-subtitle">{{ subtitle }}</p>
      </div>
    </header>

    <div class="chat-session-layout">
      <SessionSidebar
        :sessions="sessions"
        :active-id="activeId"
        @select="selectSession"
        @new="createSession"
      />

      <ChatPanel
        :messages="messages"
        :is-streaming="isStreaming"
        :error="error"
        :placeholder="placeholder"
        :welcome-title="welcomeTitle"
        :welcome-desc="welcomeDesc"
        @send="handleSend"
        @stop="stop"
      />
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import ChatPanel from './ChatPanel.vue'
import SessionSidebar from './SessionSidebar.vue'
import { fetchSessionMessages, fetchSessions } from '../api/client'
import { useChatStream } from '../composables/useChatStream'

const props = defineProps({
  type: { type: String, required: true },
  endpoint: { type: String, required: true },
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  welcomeTitle: { type: String, default: '有什么可以帮你的？' },
  welcomeDesc: { type: String, default: '输入问题开始对话。' },
  placeholder: { type: String, default: '输入你的问题...' }
})

const sessions = ref([])
const activeId = ref('')
const cache = new Map()
const { messages, isStreaming, error, send, stop, setMessages } = useChatStream()

function makeChatId() {
  return `${props.type}_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`
}

function createSession() {
  stop()
  if (activeId.value) cache.set(activeId.value, messages.value)

  const session = { chatId: makeChatId(), title: '新对话' }
  sessions.value.unshift(session)
  activeId.value = session.chatId
  setMessages([])
}

async function selectSession(session) {
  if (!session || session.chatId === activeId.value) return

  stop()
  if (activeId.value) cache.set(activeId.value, messages.value)

  activeId.value = session.chatId
  const cached = cache.get(session.chatId)
  if (cached) {
    setMessages(cached)
    return
  }

  setMessages([])
  const history = await fetchSessionMessages(props.type, session.chatId)
  cache.set(session.chatId, history)
  setMessages(history)
}

function handleSend(text) {
  const session = sessions.value.find((item) => item.chatId === activeId.value)
  if (!session) return

  if (session.title === '新对话') {
    session.title = text.slice(0, 24)
  }

  send({
    endpoint: props.endpoint,
    prompt: text,
    chatId: session.chatId
  })
}

async function loadSessions() {
  const list = await fetchSessions(props.type)
  sessions.value = list

  if (list.length === 0) {
    createSession()
    return
  }

  await selectSession(list[0])
}

onMounted(loadSessions)
onBeforeUnmount(stop)
</script>

<style scoped>
.chat-session-layout {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 18px;
  height: calc(100vh - 190px);
  min-height: 540px;
}

@media (max-width: 860px) {
  .chat-session-layout {
    grid-template-columns: 1fr;
    height: auto;
  }

  .chat-session-layout :deep(.chat-panel) {
    height: 68vh;
    min-height: 460px;
  }
}
</style>

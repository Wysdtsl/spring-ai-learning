<template>
  <section class="chat-panel card">
    <div ref="scrollEl" class="chat-messages">
      <div v-if="messages.length === 0" class="chat-welcome">
        <div class="welcome-orbit">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <h2>{{ welcomeTitle }}</h2>
        <p>{{ welcomeDesc }}</p>
      </div>

      <article
        v-for="(message, index) in messages"
        :key="index"
        class="chat-message"
        :class="message.role"
      >
        <div class="avatar">{{ message.role === 'user' ? '我' : 'AI' }}</div>
        <div class="bubble">
          <div class="bubble-text">{{ message.content }}<span v-if="message.isTyping" class="cursor">|</span></div>
        </div>
      </article>
    </div>

    <div v-if="error" class="chat-error">{{ error }}</div>

    <div class="chat-composer">
      <textarea
        ref="inputEl"
        v-model="draft"
        class="chat-input"
        rows="1"
        :placeholder="disabled ? '请先上传 PDF' : placeholder"
        :disabled="disabled"
        @input="autoResize"
        @keydown.enter.exact.prevent="submit"
      ></textarea>

      <button
        v-if="isStreaming"
        class="btn btn-ghost stop-btn"
        type="button"
        @click="$emit('stop')"
      >
        停止
      </button>
      <button
        v-else
        class="btn btn-primary send-btn"
        type="button"
        :disabled="disabled || !draft.trim()"
        @click="submit"
      >
        发送
      </button>
    </div>

    <p class="composer-tip">Enter 发送 · Shift + Enter 换行</p>
  </section>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'

const props = defineProps({
  messages: { type: Array, default: () => [] },
  isStreaming: { type: Boolean, default: false },
  error: { type: String, default: '' },
  placeholder: { type: String, default: '输入你的问题...' },
  welcomeTitle: { type: String, default: '有什么可以帮你的？' },
  welcomeDesc: { type: String, default: '输入问题开始对话。' },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['send', 'stop'])
const draft = ref('')
const scrollEl = ref(null)
const inputEl = ref(null)

function scrollToBottom() {
  nextTick(() => {
    if (scrollEl.value) {
      scrollEl.value.scrollTop = scrollEl.value.scrollHeight
    }
  })
}

function autoResize() {
  const el = inputEl.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${Math.min(el.scrollHeight, 160)}px`
}

function submit() {
  const text = draft.value.trim()
  if (!text || props.isStreaming || props.disabled) return
  emit('send', text)
  draft.value = ''
  nextTick(autoResize)
}

watch(
  () => props.messages,
  () => scrollToBottom(),
  { deep: true }
)
</script>

<style scoped>
.chat-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
  height: 100%;
  overflow: hidden;
}

.chat-messages {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 24px;
  scroll-behavior: smooth;
}

.chat-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 260px;
  text-align: center;
  color: var(--text-muted);
}

.chat-welcome h2 {
  margin: 18px 0 8px;
  color: var(--text);
  font-size: 22px;
  letter-spacing: -0.02em;
}

.chat-welcome p {
  max-width: 460px;
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
}

.welcome-orbit {
  position: relative;
  width: 72px;
  height: 72px;
  border: 1px solid var(--border-strong);
  border-radius: 50%;
  animation: spin 12s linear infinite;
}

.welcome-orbit span {
  position: absolute;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 16px var(--accent);
}

.welcome-orbit span:nth-child(1) {
  top: -5px;
  left: 31px;
}

.welcome-orbit span:nth-child(2) {
  right: 2px;
  bottom: 8px;
  background: var(--violet);
  box-shadow: 0 0 16px var(--violet);
}

.welcome-orbit span:nth-child(3) {
  left: 2px;
  bottom: 8px;
  background: var(--success);
  box-shadow: 0 0 16px var(--success);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
  align-items: flex-start;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.avatar {
  display: grid;
  place-items: center;
  flex: 0 0 34px;
  width: 34px;
  height: 34px;
  border-radius: 12px;
  background: var(--bg-soft);
  color: var(--text-muted);
  font-size: 12px;
  font-weight: 700;
}

.chat-message.assistant .avatar {
  color: #05121d;
  background: linear-gradient(135deg, var(--accent), var(--violet));
}

.bubble {
  max-width: min(760px, 82%);
  padding: 12px 15px;
  border: 1px solid var(--border);
  border-radius: 14px;
  background: var(--bg-soft);
}

.chat-message.user .bubble {
  border-color: rgba(103, 232, 249, 0.25);
  background: rgba(103, 232, 249, 0.1);
}

.bubble-text {
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 14px;
  line-height: 1.75;
}

.cursor {
  display: inline-block;
  margin-left: 2px;
  color: var(--accent);
  animation: blink 0.9s step-end infinite;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.chat-composer {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  margin: 0 24px;
  padding: 10px;
  border: 1px solid var(--border-strong);
  border-radius: 16px;
  background: rgba(11, 16, 32, 0.72);
}

.chat-input {
  flex: 1;
  min-height: 24px;
  max-height: 160px;
  padding: 8px 10px;
  border: none;
  outline: none;
  resize: none;
  color: var(--text);
  background: transparent;
  font-size: 14px;
  line-height: 1.6;
}

.chat-input::placeholder {
  color: var(--text-faint);
}

.send-btn,
.stop-btn {
  flex: 0 0 auto;
  min-height: 38px;
}

.composer-tip {
  margin: 8px 24px 16px;
  color: var(--text-faint);
  font-size: 12px;
  text-align: center;
}

.chat-error {
  margin: 0 24px 12px;
}

@media (max-width: 860px) {
  .chat-messages {
    padding: 16px;
  }

  .chat-composer {
    margin: 0 14px;
  }

  .composer-tip {
    margin: 8px 14px 12px;
  }

  .bubble {
    max-width: 88%;
  }
}
</style>

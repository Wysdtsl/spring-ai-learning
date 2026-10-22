import { ref } from 'vue'
import { API_BASE } from '../api/client'

const TYPE_INTERVAL = 15

export function useChatStream() {
  const messages = ref([])
  const isStreaming = ref(false)
  const error = ref('')

  let controller = null
  let timer = null
  let queue = []
  let doneReading = false
  let onDoneCallback = null

  function finishIfReady() {
    if (!doneReading || queue.length > 0) return

    const last = messages.value[messages.value.length - 1]
    if (last) {
      last.isTyping = false
      last.isComplete = true
    }

    if (timer) {
      clearInterval(timer)
      timer = null
    }

    isStreaming.value = false
    const callback = onDoneCallback
    onDoneCallback = null
    if (callback && last) callback(last.content)
  }

  function tick() {
    const last = messages.value[messages.value.length - 1]
    if (!last) return

    if (queue.length > 0) {
      const batch = queue.length > 180 ? 4 : 1
      for (let i = 0; i < batch && queue.length > 0; i += 1) {
        last.content += queue.shift()
      }
    }

    finishIfReady()
  }

  async function send({ endpoint, prompt, chatId, onDone }) {
    const text = (prompt || '').trim()
    if (!text || isStreaming.value) return

    error.value = ''
    messages.value.push({ role: 'user', content: text, isTyping: false, isComplete: true })
    messages.value.push({ role: 'assistant', content: '', isTyping: true, isComplete: false })

    isStreaming.value = true
    queue = []
    doneReading = false
    onDoneCallback = onDone || null
    controller = new AbortController()
    timer = setInterval(tick, TYPE_INTERVAL)

    try {
      const url = `${API_BASE}${endpoint}?prompt=${encodeURIComponent(text)}&chatId=${encodeURIComponent(chatId)}`
      const res = await fetch(url, { signal: controller.signal })

      if (!res.ok) throw new Error(`请求失败（HTTP ${res.status}）`)
      if (!res.body) throw new Error('当前浏览器不支持流式读取')

      const reader = res.body.getReader()
      const decoder = new TextDecoder()

      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        const chunk = decoder.decode(value, { stream: true })
        for (const char of chunk) queue.push(char)
      }

      doneReading = true
      finishIfReady()
    } catch (err) {
      if (err?.name === 'AbortError') {
        doneReading = true
        finishIfReady()
        return
      }

      error.value = err?.message || '请求失败'
      const last = messages.value[messages.value.length - 1]
      if (last) {
        if (!last.content) {
          last.content = '抱歉，请求失败。请确认后端接口已启动，并已按 API.md 配置 Spring CORS。'
        }
        last.isTyping = false
        last.isComplete = true
      }

      doneReading = true
      queue = []
      if (timer) {
        clearInterval(timer)
        timer = null
      }
      isStreaming.value = false
      onDoneCallback = null
    }
  }

  function stop() {
    if (controller) {
      controller.abort()
      controller = null
    }

    doneReading = true
    queue = []

    const last = messages.value[messages.value.length - 1]
    if (last && last.role === 'assistant') {
      last.isTyping = false
      last.isComplete = true
    }

    if (timer) {
      clearInterval(timer)
      timer = null
    }

    isStreaming.value = false
    onDoneCallback = null
  }

  function reset() {
    stop()
    messages.value = []
    error.value = ''
  }

  function setMessages(list) {
    messages.value = Array.isArray(list) ? list : []
    error.value = ''
  }

  return { messages, isStreaming, error, send, stop, reset, setMessages }
}

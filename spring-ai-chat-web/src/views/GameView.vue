<template>
  <div class="page game-page">
    <header class="page-header">
      <div>
        <h1 class="page-title">哄哄模拟器</h1>
        <p class="page-subtitle">AI 扮演生气的女朋友。根据你的回复质量增减原谅值，争取哄到 100。</p>
      </div>
    </header>

    <div class="game-layout">
      <aside class="game-status card" :style="{ '--score-color': scoreColor }">
        <div class="mood">{{ mood }}</div>
        <p class="status-label">当前原谅值</p>
        <div class="score-number">{{ score }}<span>/100</span></div>

        <div class="score-track">
          <div class="score-fill" :style="{ width: `${score}%` }"></div>
        </div>

        <div class="delta-row">
          <span>上一次得分</span>
          <strong :class="delta >= 0 ? 'positive' : 'negative'">
            {{ delta > 0 ? `+${delta}` : delta }}
          </strong>
        </div>

        <div class="status-hint">
          <p v-if="score >= 100">🎉 哄好了！点击“重新开始”再来一局。</p>
          <p v-else-if="score <= 0">💔 彻底生气了，点击“重新开始”再试一次。</p>
          <p v-else>回复里可以带一点具体的关心和行动，比空泛道歉更容易加分。</p>
        </div>

        <button class="btn btn-ghost reset-btn" type="button" @click="resetGame">
          重新开始
        </button>
      </aside>

      <ChatPanel
        :messages="messages"
        :is-streaming="isStreaming"
        :error="error"
        welcome-title="她正在生气"
        welcome-desc="先说点什么哄哄她吧。初始原谅值 20/100，每次回复会得到 -10 到 +10 的评分。"
        placeholder="输入你想对她说的话..."
        @send="handleSend"
        @stop="stop"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import ChatPanel from '../components/ChatPanel.vue'
import { useChatStream } from '../composables/useChatStream'

const score = ref(20)
const delta = ref(0)
const chatId = ref(makeChatId())
const { messages, isStreaming, error, send, stop, reset } = useChatStream()

function makeChatId() {
  return `honghong_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`
}

const mood = computed(() => {
  if (score.value >= 90) return '🥰'
  if (score.value >= 70) return '😊'
  if (score.value >= 50) return '🙂'
  if (score.value >= 30) return '😐'
  if (score.value > 0) return '😤'
  return '💔'
})

const scoreColor = computed(() => {
  if (score.value >= 70) return 'var(--success)'
  if (score.value >= 40) return 'var(--warning)'
  return 'var(--danger)'
})

function parseGameResult(text) {
  const valueMatch = text.match(/原值[：:]\s*(\d+)\s*\/\s*100/)
  const deltaMatch = text.match(/得分[：:]\s*([+-]?\d+)/)

  if (deltaMatch) {
    delta.value = Number(deltaMatch[1])
  }

  if (valueMatch) {
    score.value = clamp(Number(valueMatch[1]))
  } else if (deltaMatch) {
    score.value = clamp(score.value + Number(deltaMatch[1]))
  }
}

function clamp(value) {
  return Math.max(0, Math.min(100, value))
}

function handleSend(text) {
  send({
    endpoint: '/ai/game/stream',
    prompt: text,
    chatId: chatId.value,
    onDone: parseGameResult
  })
}

function resetGame() {
  stop()
  reset()
  chatId.value = makeChatId()
  score.value = 20
  delta.value = 0
}
</script>

<style scoped>
.game-page {
  max-width: 1180px;
}

.game-layout {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 18px;
  height: calc(100vh - 190px);
  min-height: 540px;
}

.game-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 26px 22px;
  text-align: center;
  overflow-y: auto;
}

.mood {
  font-size: 52px;
  line-height: 1;
  filter: drop-shadow(0 8px 20px rgba(251, 113, 133, 0.25));
}

.status-label {
  margin: 18px 0 6px;
  color: var(--text-muted);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.score-number {
  color: var(--score-color);
  font-size: 48px;
  font-weight: 800;
  letter-spacing: -0.06em;
  line-height: 1;
}

.score-number span {
  color: var(--text-faint);
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0;
}

.score-track {
  width: 100%;
  height: 10px;
  margin: 20px 0 18px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.14);
  overflow: hidden;
}

.score-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--danger), var(--warning), var(--success));
  transition: width 0.35s ease;
}

.delta-row {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding: 12px 0;
  border-top: 1px solid var(--border);
  border-bottom: 1px solid var(--border);
  color: var(--text-muted);
  font-size: 13px;
}

.delta-row strong {
  font-family: "SFMono-Regular", Consolas, monospace;
}

.positive {
  color: var(--success);
}

.negative {
  color: var(--danger);
}

.status-hint {
  flex: 1;
  display: flex;
  align-items: center;
  padding: 18px 0;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.75;
}

.status-hint p {
  margin: 0;
}

.reset-btn {
  width: 100%;
}

@media (max-width: 860px) {
  .game-layout {
    grid-template-columns: 1fr;
    height: auto;
  }

  .game-status {
    max-height: none;
  }

  .game-layout :deep(.chat-panel) {
    height: 66vh;
    min-height: 440px;
  }
}
</style>

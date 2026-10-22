<template>
  <aside class="session-sidebar card">
    <button class="btn btn-primary new-session" type="button" @click="$emit('new')">
      ＋ 新建对话
    </button>

    <div class="session-list">
      <button
        v-for="session in sessions"
        :key="session.chatId"
        type="button"
        class="session-item"
        :class="{ active: session.chatId === activeId }"
        @click="$emit('select', session)"
      >
        <span class="session-title">{{ session.title || '新对话' }}</span>
        <span class="session-id">{{ session.chatId.slice(-6) }}</span>
      </button>

      <p v-if="sessions.length === 0" class="session-empty">暂无会话</p>
    </div>
  </aside>
</template>

<script setup>
defineProps({
  sessions: { type: Array, default: () => [] },
  activeId: { type: String, default: '' }
})

defineEmits(['select', 'new'])
</script>

<style scoped>
.session-sidebar {
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 14px;
  overflow: hidden;
}

.new-session {
  width: 100%;
  margin-bottom: 12px;
}

.session-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.session-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid transparent;
  border-radius: 10px;
  color: var(--text-muted);
  background: transparent;
  text-align: left;
  transition: background 0.16s ease, color 0.16s ease, border-color 0.16s ease;
}

.session-item:hover {
  color: var(--text);
  background: var(--bg-hover);
}

.session-item.active {
  color: var(--text);
  border-color: rgba(103, 232, 249, 0.25);
  background: rgba(103, 232, 249, 0.08);
}

.session-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.session-id {
  flex: 0 0 auto;
  color: var(--text-faint);
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 10px;
}

.session-empty {
  margin: 20px 8px;
  color: var(--text-faint);
  font-size: 13px;
  text-align: center;
}

@media (max-width: 860px) {
  .session-sidebar {
    max-height: 180px;
  }
}
</style>

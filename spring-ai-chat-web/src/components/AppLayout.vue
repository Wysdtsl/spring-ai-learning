<template>
  <div class="app-shell">
    <header class="app-header">
      <RouterLink class="brand" to="/">
        <span class="brand-mark">AI</span>
        <span class="brand-text">Spring AI Lab</span>
      </RouterLink>

      <nav class="app-nav">
        <RouterLink to="/chatbot">对话机器人</RouterLink>
        <RouterLink to="/game">哄哄模拟器</RouterLink>
        <RouterLink to="/service">智能客服</RouterLink>
        <RouterLink to="/pdf">ChatPDF</RouterLink>
      </nav>

      <a class="backend-link" :href="API_BASE" target="_blank" rel="noreferrer">
        <span class="dot"></span>
        {{ API_BASE.replace('http://', '') }}
      </a>
    </header>

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterLink, RouterView } from 'vue-router'
import { API_BASE } from '../api/client'
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 14px 24px;
  border-bottom: 1px solid var(--border);
  background: rgba(11, 16, 32, 0.78);
  backdrop-filter: blur(18px);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 800;
  letter-spacing: -0.02em;
  white-space: nowrap;
}

.brand-mark {
  display: grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border-radius: 10px;
  color: #05121d;
  background: linear-gradient(135deg, var(--accent), var(--violet));
  font-size: 12px;
  font-weight: 900;
}

.app-nav {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  overflow-x: auto;
}

.app-nav a {
  padding: 8px 12px;
  border-radius: 999px;
  color: var(--text-muted);
  font-size: 13px;
  white-space: nowrap;
  transition: color 0.16s ease, background 0.16s ease;
}

.app-nav a:hover {
  color: var(--text);
  background: var(--bg-hover);
}

.app-nav a.router-link-active {
  color: var(--text);
  background: rgba(103, 232, 249, 0.1);
}

.backend-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  border: 1px solid var(--border);
  border-radius: 999px;
  color: var(--text-muted);
  font-size: 12px;
  white-space: nowrap;
}

.dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--success);
  box-shadow: 0 0 10px var(--success);
}

.app-main {
  min-height: calc(100vh - 61px);
}

@media (max-width: 860px) {
  .app-header {
    flex-wrap: wrap;
    gap: 10px;
  }

  .app-nav {
    order: 3;
    width: 100%;
  }

  .backend-link {
    margin-left: auto;
  }
}
</style>

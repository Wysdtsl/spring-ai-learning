<template>
  <div class="page home-page">
    <section class="hero">
      <div class="hero-copy">
        <span class="eyebrow">Spring AI + DeepSeek</span>
        <h1>把大模型能力<br />装进一个完整应用</h1>
        <p>
          四个模块分别覆盖 Prompt 对话、会话记忆、Tool Calling 与 RAG。
          前端已经按接口文档完成，后端按 <code>API.md</code> 补齐即可联调。
        </p>
        <div class="hero-actions">
          <RouterLink class="btn btn-primary" to="/chatbot">开始对话</RouterLink>
          <RouterLink class="btn btn-ghost" to="/pdf">试试 ChatPDF</RouterLink>
        </div>
      </div>

      <div class="hero-visual" aria-hidden="true">
        <div class="orb orb-a"></div>
        <div class="orb orb-b"></div>
        <div class="terminal">
          <div class="terminal-bar"><span></span><span></span><span></span></div>
          <pre><code>POST /ai/chat/stream
{
  "prompt": "你好",
  "chatId": "chat_..."
}

data: 你好！很高兴和你聊天
data: 。有什么我可以帮你的吗？</code></pre>
        </div>
      </div>
    </section>

    <section class="module-grid">
      <RouterLink
        v-for="item in modules"
        :key="item.path"
        :to="item.path"
        class="module-card card"
        :style="{ '--module-accent': item.accent }"
      >
        <div class="module-icon">{{ item.icon }}</div>
        <h2>{{ item.title }}</h2>
        <p>{{ item.desc }}</p>
        <span class="module-link">进入模块 →</span>
      </RouterLink>
    </section>

    <section class="note card">
      <div>
        <h3>联调前先确认</h3>
        <p>
          前端默认请求 <code>http://localhost:8083</code>。请在后端添加
          <code>WebMvcConfigurer</code> CORS 配置，允许
          <code>http://localhost:5174</code>。具体代码见项目根目录的 <code>API.md</code>。
        </p>
      </div>
      <span class="note-badge">CORS</span>
    </section>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'

const modules = [
  {
    icon: '💬',
    title: '对话机器人',
    desc: '带 ChatMemory 的多轮对话，支持会话列表、历史消息和流式打字机效果。',
    path: '/chatbot',
    accent: '#67e8f9'
  },
  {
    icon: '❤️',
    title: '哄哄模拟器',
    desc: 'AI 扮演生气的女朋友，根据回复质量增减原谅值，练习提示词与结构化解析。',
    path: '/game',
    accent: '#fb7185'
  },
  {
    icon: '🎧',
    title: '智能客服',
    desc: '基于 Tool Calling 的课程顾问，模型可调用 Java 方法查询课程、校区和预约。',
    path: '/service',
    accent: '#34d399'
  },
  {
    icon: '📄',
    title: 'ChatPDF',
    desc: '上传 PDF、向量化、语义检索与 RAG 问答，完整走通检索增强生成链路。',
    path: '/pdf',
    accent: '#a78bfa'
  }
]
</script>

<style scoped>
.home-page {
  padding-top: 40px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
  gap: 36px;
  align-items: center;
  min-height: 430px;
  margin-bottom: 34px;
}

.eyebrow {
  display: inline-flex;
  padding: 6px 12px;
  border: 1px solid rgba(103, 232, 249, 0.3);
  border-radius: 999px;
  color: var(--accent);
  background: rgba(103, 232, 249, 0.08);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero h1 {
  margin: 18px 0 16px;
  font-size: clamp(38px, 5.4vw, 68px);
  line-height: 1.02;
  letter-spacing: -0.055em;
  background: linear-gradient(120deg, #f8fbff 10%, var(--accent) 55%, var(--violet));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.hero-copy p {
  max-width: 560px;
  margin: 0;
  color: var(--text-muted);
  font-size: 15px;
  line-height: 1.85;
}

.hero-copy code,
.note code {
  padding: 2px 6px;
  border: 1px solid var(--border);
  border-radius: 6px;
  color: var(--accent);
  background: rgba(103, 232, 249, 0.07);
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 0.92em;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 26px;
}

.hero-visual {
  position: relative;
  min-height: 360px;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
  opacity: 0.8;
}

.orb-a {
  top: 10px;
  right: 34px;
  width: 180px;
  height: 180px;
  background: radial-gradient(circle, rgba(103, 232, 249, 0.32), transparent 68%);
}

.orb-b {
  right: 120px;
  bottom: 10px;
  width: 220px;
  height: 220px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.28), transparent 68%);
}

.terminal {
  position: relative;
  margin: 48px 0 0 20px;
  padding: 14px;
  border: 1px solid var(--border-strong);
  border-radius: 18px;
  background: rgba(8, 13, 27, 0.86);
  box-shadow: var(--shadow);
  transform: rotate(-2deg);
}

.terminal-bar {
  display: flex;
  gap: 6px;
  margin-bottom: 12px;
}

.terminal-bar span {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--border-strong);
}

.terminal-bar span:first-child {
  background: var(--danger);
}

.terminal-bar span:nth-child(2) {
  background: var(--warning);
}

.terminal-bar span:nth-child(3) {
  background: var(--success);
}

.terminal pre {
  margin: 0;
  overflow: hidden;
  color: #b9c7dc;
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
  line-height: 1.75;
  white-space: pre-wrap;
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.module-card {
  position: relative;
  padding: 20px;
  overflow: hidden;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.module-card::after {
  content: "";
  position: absolute;
  inset: auto -30% -60% auto;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, var(--module-accent), transparent 70%);
  opacity: 0.16;
}

.module-card:hover {
  transform: translateY(-4px);
  border-color: var(--module-accent);
}

.module-icon {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  margin-bottom: 16px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03);
  font-size: 21px;
}

.module-card h2 {
  margin: 0 0 8px;
  font-size: 17px;
  letter-spacing: -0.02em;
}

.module-card p {
  min-height: 66px;
  margin: 0 0 18px;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.7;
}

.module-link {
  color: var(--module-accent);
  font-size: 12px;
  font-weight: 700;
}

.note {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-top: 18px;
  padding: 18px 22px;
}

.note h3 {
  margin: 0 0 6px;
  font-size: 15px;
}

.note p {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
  line-height: 1.75;
}

.note-badge {
  flex: 0 0 auto;
  padding: 8px 14px;
  border: 1px solid rgba(251, 191, 36, 0.3);
  border-radius: 999px;
  color: var(--warning);
  background: rgba(251, 191, 36, 0.08);
  font-family: "SFMono-Regular", Consolas, monospace;
  font-size: 12px;
}

@media (max-width: 1000px) {
  .hero {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .hero-visual {
    display: none;
  }

  .module-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 620px) {
  .home-page {
    padding-top: 24px;
  }

  .module-grid {
    grid-template-columns: 1fr;
  }

  .note {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

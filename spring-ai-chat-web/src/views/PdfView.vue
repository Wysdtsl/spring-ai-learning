<template>
  <div class="page pdf-page">
    <header class="page-header">
      <div>
        <h1 class="page-title">ChatPDF</h1>
        <p class="page-subtitle">上传 PDF，后端完成解析、向量化和检索，再由模型基于检索结果回答。</p>
      </div>
    </header>

    <div class="pdf-layout">
      <section class="pdf-preview card">
        <div class="pdf-toolbar">
          <label class="btn btn-primary upload-btn">
            {{ uploading ? '上传中...' : '上传 PDF' }}
            <input
              type="file"
              accept="application/pdf"
              hidden
              :disabled="uploading"
              @change="handleFile"
            />
          </label>
          <span class="file-name">{{ fileName || '尚未上传 PDF' }}</span>
        </div>

        <div v-if="uploadError" class="chat-error">{{ uploadError }}</div>

        <iframe
          v-if="fileName"
          :key="previewKey"
          class="pdf-frame"
          :src="previewUrl"
          title="PDF 预览"
        ></iframe>

        <div v-else class="pdf-placeholder">
          <div class="placeholder-icon">📄</div>
          <h3>上传一份 PDF 开始</h3>
          <p>上传成功后，这里会显示 PDF 预览；右侧可以针对文档内容提问。</p>
        </div>
      </section>

      <ChatPanel
        :messages="messages"
        :is-streaming="isStreaming"
        :error="error"
        :disabled="!fileName"
        welcome-title="和 PDF 对话"
        welcome-desc="先上传 PDF，然后问它文档里的内容。例如：这份文档的核心结论是什么？"
        placeholder="针对 PDF 内容提问..."
        @send="handleSend"
        @stop="stop"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import ChatPanel from '../components/ChatPanel.vue'
import { pdfPreviewUrl, uploadPdf } from '../api/client'
import { useChatStream } from '../composables/useChatStream'

const chatId = ref('')
const fileName = ref('')
const uploading = ref(false)
const uploadError = ref('')
const previewKey = ref(0)
const { messages, isStreaming, error, send, stop, reset } = useChatStream()

const previewUrl = computed(() => {
  if (!chatId.value) return ''
  return `${pdfPreviewUrl(chatId.value)}?v=${previewKey.value}`
})

function makeChatId() {
  return `pdf_${Date.now()}_${Math.random().toString(36).slice(2, 9)}`
}

async function handleFile(event) {
  const file = event.target.files?.[0]
  if (!file) return

  if (file.type && file.type !== 'application/pdf') {
    uploadError.value = '请选择 PDF 文件。'
    event.target.value = ''
    return
  }

  const nextChatId = makeChatId()
  uploading.value = true
  uploadError.value = ''
  reset()

  try {
    const data = await uploadPdf(nextChatId, file)
    chatId.value = nextChatId
    fileName.value = data?.fileName || file.name
    previewKey.value += 1
  } catch (err) {
    uploadError.value = err?.message || '上传失败'
  } finally {
    uploading.value = false
    event.target.value = ''
  }
}

function handleSend(text) {
  if (!fileName.value) {
    uploadError.value = '请先上传 PDF。'
    return
  }

  send({
    endpoint: '/ai/pdf/chat/stream',
    prompt: text,
    chatId: chatId.value
  })
}
</script>

<style scoped>
.pdf-page {
  max-width: 1380px;
}

.pdf-layout {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.95fr);
  gap: 18px;
  height: calc(100vh - 190px);
  min-height: 560px;
}

.pdf-preview {
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.pdf-toolbar {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
}

.upload-btn {
  position: relative;
  overflow: hidden;
}

.file-name {
  overflow: hidden;
  color: var(--text-muted);
  font-size: 13px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pdf-preview .chat-error {
  margin: 12px 16px 0;
}

.pdf-frame {
  flex: 1;
  width: 100%;
  min-height: 0;
  border: none;
  background: #fff;
}

.pdf-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
  color: var(--text-muted);
}

.placeholder-icon {
  font-size: 52px;
  margin-bottom: 16px;
  filter: drop-shadow(0 10px 24px rgba(167, 139, 250, 0.3));
}

.pdf-placeholder h3 {
  margin: 0 0 8px;
  color: var(--text);
  font-size: 18px;
}

.pdf-placeholder p {
  max-width: 360px;
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
}

@media (max-width: 980px) {
  .pdf-layout {
    grid-template-columns: 1fr;
    height: auto;
  }

  .pdf-preview {
    height: 62vh;
    min-height: 420px;
  }

  .pdf-layout :deep(.chat-panel) {
    height: 62vh;
    min-height: 420px;
  }
}
</style>

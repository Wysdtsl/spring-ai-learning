export const API_BASE = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8083').replace(/\/+$/, '')

export function normalizeSessions(raw) {
  if (!Array.isArray(raw)) return []

  return raw
    .map((item) => {
      if (typeof item === 'string') {
        return { chatId: item, title: '新对话', updatedAt: '' }
      }

      const chatId = item?.chatId || item?.id
      if (!chatId) return null

      return {
        chatId: String(chatId),
        title: item?.title || '新对话',
        updatedAt: item?.updatedAt || ''
      }
    })
    .filter(Boolean)
}

export function normalizeMessages(raw) {
  if (!Array.isArray(raw)) return []

  return raw.map((item) => ({
    role: item?.role === 'assistant' ? 'assistant' : 'user',
    content: item?.content ?? item?.context ?? '',
    isTyping: false,
    isComplete: true
  }))
}

export async function fetchSessions(type) {
  try {
    const res = await fetch(`${API_BASE}/ai/history/${encodeURIComponent(type)}`)
    if (!res.ok) return []
    return normalizeSessions(await res.json())
  } catch {
    return []
  }
}

export async function fetchSessionMessages(type, chatId) {
  try {
    const res = await fetch(
      `${API_BASE}/ai/history/${encodeURIComponent(type)}/${encodeURIComponent(chatId)}`
    )
    if (!res.ok) return []
    return normalizeMessages(await res.json())
  } catch {
    return []
  }
}

export async function uploadPdf(chatId, file) {
  const form = new FormData()
  form.append('file', file)

  const res = await fetch(`${API_BASE}/ai/pdf/upload/${encodeURIComponent(chatId)}`, {
    method: 'POST',
    body: form
  })

  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(text || `上传失败（HTTP ${res.status}）`)
  }

  return res.json().catch(() => ({}))
}

export function pdfPreviewUrl(chatId) {
  return `${API_BASE}/ai/pdf/preview/${encodeURIComponent(chatId)}`
}

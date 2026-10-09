// Frontend backend se seedha connect hota hai (CORS backend me allow hai)
export const BASE = import.meta.env.VITE_API_URL || 'http://localhost:8081'

async function http(path, options = {}) {
  let res
  try {
    res = await fetch(BASE + path, {
      headers: { 'Content-Type': 'application/json' },
      ...options,
    })
  } catch {
    throw {
      status: 0,
      message: `Cannot reach the backend at ${BASE}. Check that it is running.`,
    }
  }

  if (res.status === 204) return null
  const body = await res.json().catch(() => null)

  if (!res.ok) {
    throw {
      status: res.status,
      message: body?.message || res.statusText,
      fieldErrors: body?.fieldErrors,
      body,
    }
  }
  return body
}

const json = (method, data) => ({ method, body: JSON.stringify(data) })

export const api = {
  // request builder
  sendRequest: (payload) => http('/api/request/send', json('POST', payload)),

  // history
  getHistory: () => http('/api/history'),
  getHistoryItem: (id) => http(`/api/history/${id}`),
  deleteHistory: (id) => http(`/api/history/${id}`, { method: 'DELETE' }),

  // meters
  getMeters: () => http('/api/meters'),
  createMeter: (data) => http('/api/meters', json('POST', data)),
  updateMeter: (id, data) => http(`/api/meters/${id}`, json('PUT', data)),
  deleteMeter: (id) => http(`/api/meters/${id}`, { method: 'DELETE' }),
}

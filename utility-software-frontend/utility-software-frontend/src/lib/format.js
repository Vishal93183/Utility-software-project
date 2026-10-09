const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
const pad = (n) => String(n).padStart(2, '0')

// "05 Oct 2026 22:10:15"
export function formatDateTime(value) {
  if (!value) return '--'
  const d = new Date(value)
  if (isNaN(d)) return '--'
  return `${pad(d.getDate())} ${MONTHS[d.getMonth()]} ${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

export const num = (v) => (v === null || v === undefined ? '--' : v)

import { useEffect, useState } from 'react'
import Icon from './Icon.jsx'
import { formatDateTime } from '../lib/format.js'

export default function HistoryPanel({ items, selectedId, onSelect, onDelete }) {
  const [menuId, setMenuId] = useState(null)

  useEffect(() => {
    const close = () => setMenuId(null)
    document.addEventListener('click', close)
    return () => document.removeEventListener('click', close)
  }, [])

  return (
    <section className="card history-card">
      <div className="card-head">
        <h2><Icon name="history" size={28} /> Request History</h2>
        <span className="count-badge">{items.length}</span>
      </div>

      <ul className="history-list">
        {items.length === 0 && (
          <li className="empty">No requests yet. Send one from the Request Builder.</li>
        )}
        {items.map((h) => (
          <li
            key={h.id}
            className={`history-row ${selectedId === h.id ? 'selected' : ''}`}
            onClick={() => onSelect(h.id)}
          >
            <span className={`pill ${h.opType === 'WRITE' ? 'write' : 'read'}`}>{h.opType}</span>
            <div className="history-text">
              <div className="history-name">{h.name}</div>
              <div className="history-time">{formatDateTime(h.createdAt)}</div>
            </div>
            <span className={`dot ${h.success ? 'green' : 'red'}`} title={h.success ? 'Success' : 'Failed'} />
            <div className="kebab-wrap">
              <button
                className="kebab"
                aria-label="More actions"
                onClick={(e) => {
                  e.stopPropagation()
                  setMenuId(menuId === h.id ? null : h.id)
                }}
              >
                <Icon name="dots" size={18} />
              </button>
              {menuId === h.id && (
                <div className="menu" onClick={(e) => e.stopPropagation()}>
                  <button
                    onClick={() => {
                      setMenuId(null)
                      onDelete(h.id)
                    }}
                  >
                    <Icon name="trash" size={15} /> Delete
                  </button>
                </div>
              )}
            </div>
          </li>
        ))}
      </ul>
    </section>
  )
}

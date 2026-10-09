import { useState } from 'react'
import Icon from './Icon.jsx'

// JSON ki ek line ko colour karta hai: key / string / number / boolean
function highlight(line) {
  const parts = []
  const re = /("(?:[^"\\]|\\.)*")(\s*:)?|\b(true|false|null)\b|(-?\d+(?:\.\d+)?)/g
  let last = 0
  let m
  while ((m = re.exec(line)) !== null) {
    if (m.index > last) parts.push(line.slice(last, m.index))
    if (m[1] && m[2]) {
      parts.push(<span key={m.index} className="tok-key">{m[1]}</span>)
      parts.push(m[2])
    } else if (m[1]) {
      parts.push(<span key={m.index} className="tok-str">{m[1]}</span>)
    } else if (m[3]) {
      parts.push(<span key={m.index} className="tok-bool">{m[3]}</span>)
    } else {
      parts.push(<span key={m.index} className="tok-num">{m[4]}</span>)
    }
    last = re.lastIndex
  }
  if (last < line.length) parts.push(line.slice(last))
  return parts
}

export default function ResponsePanel({ response, isError }) {
  const [copied, setCopied] = useState(false)
  const text = response ? JSON.stringify(response, null, 2) : ''
  const lines = text ? text.split('\n') : []

  const copy = async () => {
    if (!text) return
    try {
      await navigator.clipboard.writeText(text)
      setCopied(true)
      setTimeout(() => setCopied(false), 1500)
    } catch {
      /* clipboard blocked */
    }
  }

  return (
    <section className="card response-card">
      <div className="card-head">
        <h2><Icon name="doc" size={28} /> Response</h2>
        <button className="btn-soft" onClick={copy} disabled={!text}>
          <Icon name={copied ? 'check' : 'copy'} size={18} /> {copied ? 'Copied' : 'Copy'}
        </button>
      </div>

      <div className={`code-box ${isError ? 'is-error' : ''}`}>
        {lines.length === 0 ? (
          <p className="empty">Send a request to see the JSON response here.</p>
        ) : (
          <pre>
            {lines.map((line, i) => (
              <div className="code-line" key={i}>
                <span className="ln">{i + 1}</span>
                <code>{highlight(line)}</code>
              </div>
            ))}
          </pre>
        )}
      </div>
    </section>
  )
}

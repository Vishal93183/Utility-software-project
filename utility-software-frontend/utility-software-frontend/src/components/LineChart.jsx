const COLORS = ['#1a6bf5', '#16a34a', '#f59e0b', '#ef4444', '#8b5cf6', '#0891b2', '#db2777']
const W = 640
const H = 240
const PAD = { l: 50, r: 14, t: 14, b: 28 }

const time = (t) => new Date(t).toLocaleTimeString('en-GB')

// null value = meter offline -> line toot jaati hai
function pathFor(series, id, x, y) {
  let d = ''
  let pen = false
  series.forEach((s, i) => {
    const v = s.p[id]
    if (v === null || v === undefined) {
      pen = false
      return
    }
    d += `${pen ? 'L' : 'M'}${x(i).toFixed(1)} ${y(v).toFixed(1)} `
    pen = true
  })
  return d
}

export default function LineChart({ series, lines }) {
  if (series.length === 0) {
    return <div className="chart-empty">Start the simulation to see live power (W) here.</div>
  }

  const values = series.flatMap((s) => lines.map((l) => s.p[l.id]).filter((v) => v != null))
  const top = Math.max(100, Math.ceil((Math.max(0, ...values) * 1.1) / 100) * 100)
  const n = Math.max(series.length, 2)
  const x = (i) => PAD.l + (i / (n - 1)) * (W - PAD.l - PAD.r)
  const y = (v) => PAD.t + (1 - v / top) * (H - PAD.t - PAD.b)
  const ticks = [0, top / 2, top]

  return (
    <div>
      <svg className="chart" viewBox={`0 0 ${W} ${H}`} role="img" aria-label="Power over time">
        {ticks.map((t) => (
          <g key={t}>
            <line x1={PAD.l} x2={W - PAD.r} y1={y(t)} y2={y(t)} stroke="#e6ecf5" />
            <text x={PAD.l - 8} y={y(t) + 4} textAnchor="end" fontSize="11" fill="#6b7a90">{Math.round(t)}</text>
          </g>
        ))}
        <text x={12} y={PAD.t + 4} fontSize="11" fill="#6b7a90">W</text>

        {lines.map((l, i) => {
          const color = COLORS[i % COLORS.length]
          const lastIdx = series.length - 1
          const last = series[lastIdx].p[l.id]
          return (
            <g key={l.id}>
              <path d={pathFor(series, l.id, x, y)} fill="none" stroke={color} strokeWidth="2" strokeLinejoin="round" />
              {last != null && <circle cx={x(lastIdx)} cy={y(last)} r="3.5" fill={color} />}
            </g>
          )
        })}

        <text x={PAD.l} y={H - 8} fontSize="11" fill="#6b7a90">{time(series[0].t)}</text>
        <text x={W - PAD.r} y={H - 8} fontSize="11" fill="#6b7a90" textAnchor="end">{time(series[series.length - 1].t)}</text>
      </svg>

      <div className="legend">
        {lines.map((l, i) => (
          <span key={l.id}>
            <i style={{ background: COLORS[i % COLORS.length] }} /> {l.label}
          </span>
        ))}
      </div>
    </div>
  )
}

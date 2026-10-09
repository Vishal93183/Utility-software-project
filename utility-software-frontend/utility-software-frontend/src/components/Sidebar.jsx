import Icon from './Icon.jsx'

const ITEMS = [
  { key: 'home', label: 'Home', icon: 'home' },
  { key: 'meter-reading', label: 'Meter Reading', icon: 'gauge' },
  { key: 'simulation', label: 'Simulation', icon: 'gear' },
  { key: 'request-builder', label: 'Request Builder', icon: 'code' },
  { key: 'data-view', label: 'Data View', icon: 'bars' },
  { key: 'tariff', label: 'Tariff & Charges', icon: 'rupee' },
  { key: 'reports', label: 'Reports', icon: 'doc' },
  { key: 'settings', label: 'Settings', icon: 'gear' },
]

export default function Sidebar({ active, onNavigate, simRunning }) {
  return (
    <aside className="sidebar">
      <nav>
        {ITEMS.map((item) => (
          <button
            key={item.key}
            className={`nav-item ${active === item.key ? 'active' : ''}`}
            onClick={() => onNavigate(item.key)}
          >
            <Icon name={item.icon} size={22} />
            <span>{item.label}</span>
            {item.key === 'simulation' && simRunning && <span className="sim-dot" title="Simulation running" />}
          </button>
        ))}
      </nav>
    </aside>
  )
}

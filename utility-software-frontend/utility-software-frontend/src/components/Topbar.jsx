import Icon from './Icon.jsx'

const USER_NAME = 'Vishal Raj'

export default function Topbar() {
  return (
    <header className="topbar">
      <div className="brand">
        <Icon name="bolt" size={34} stroke={2} />
        <h1>Utility Software</h1>
      </div>
      <div className="topbar-right">
        <button className="bell" aria-label="Notifications">
          <Icon name="bell" size={24} />
          <span className="bell-dot" />
        </button>
        <button className="user">
          <span className="avatar">{USER_NAME[0]}</span>
          <span className="user-name">{USER_NAME}</span>
          <Icon name="chevron" size={16} />
        </button>
      </div>
    </header>
  )
}

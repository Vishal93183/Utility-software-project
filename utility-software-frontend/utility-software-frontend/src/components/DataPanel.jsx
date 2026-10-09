import Icon from './Icon.jsx'
import { formatDateTime, num } from '../lib/format.js'

export default function DataPanel({
  meters, live, onToggleLive, onRefresh, refreshing, error, onAdd, onEdit, onDelete,
}) {
  return (
    <section className="card data-card" id="data-view">
      <div className="card-head">
        <h2><Icon name="bars" size={28} /> Real Time Data View</h2>
        <div className="head-actions">
          <button className="btn-soft" onClick={onAdd}>
            <Icon name="plus" size={18} /> Add Meter
          </button>
          <button className={`live-pill ${live ? '' : 'paused'}`} onClick={onToggleLive} title="Click to pause or resume auto refresh">
            <span className={`dot ${live ? 'green' : 'grey'}`} /> {live ? 'Live Data' : 'Paused'}
          </button>
          <button className="btn-soft" onClick={onRefresh} disabled={refreshing}>
            <Icon name="refresh" size={18} className={refreshing ? 'spin' : ''} /> Refresh
          </button>
        </div>
      </div>

      {error && <div className="banner-error">{error}</div>}

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Meter ID</th>
              <th>Status</th>
              <th>Voltage (V)</th>
              <th>Current (A)</th>
              <th>Power (W)</th>
              <th>Reading (kWh)</th>
              <th>Last Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {meters.length === 0 && (
              <tr>
                <td colSpan="9" className="empty-cell">
                  No meters in the database yet. Click <strong>Add Meter</strong> to create one.
                </td>
              </tr>
            )}
            {meters.map((m, i) => {
              const online = m.status === 'ONLINE'
              return (
                <tr key={m.id}>
                  <td>{i + 1}</td>
                  <td>{m.meterCode} <span className="id-tag">id {m.id}</span></td>
                  <td>
                    <span className={`status ${online ? 'online' : 'offline'}`}>
                      <span className={`dot ${online ? 'green' : 'red'}`} />
                      {online ? 'Online' : 'Offline'}
                    </span>
                  </td>
                  <td>{online ? num(m.voltage) : '--'}</td>
                  <td>{online ? num(m.current) : '--'}</td>
                  <td>{online ? num(m.power) : '--'}</td>
                  <td>{online ? num(m.reading) : '--'}</td>
                  <td>{formatDateTime(m.lastUpdated)}</td>
                  <td>
                    <div className="row-actions">
                      <button className="icon-btn" onClick={() => onEdit(m)} aria-label={`Edit ${m.meterCode}`} title="Edit">
                        <Icon name="edit" size={17} />
                      </button>
                      <button className="icon-btn danger" onClick={() => onDelete(m)} aria-label={`Delete ${m.meterCode}`} title="Delete">
                        <Icon name="trash" size={17} />
                      </button>
                    </div>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>
    </section>
  )
}

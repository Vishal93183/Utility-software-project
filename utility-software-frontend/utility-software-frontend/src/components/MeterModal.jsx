import { useState } from 'react'
import Icon from './Icon.jsx'

const toStr = (v) => (v === null || v === undefined ? '' : String(v))
const toNum = (v) => (v === '' ? null : Number(v))

export default function MeterModal({ meter, onClose, onSave }) {
  const editing = Boolean(meter)
  const [f, setF] = useState({
    meterCode: meter?.meterCode ?? '',
    meterTypeId: toStr(meter?.meterTypeId),
    ipAddress: meter?.ipAddress ?? '',
    status: meter?.status ?? 'ONLINE',
    voltage: toStr(meter?.voltage),
    current: toStr(meter?.current),
    power: toStr(meter?.power),
    reading: toStr(meter?.reading),
  })
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const set = (k) => (e) => setF({ ...f, [k]: e.target.value })
  const online = f.status === 'ONLINE'

  const submit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await onSave({
        meterCode: f.meterCode.trim(),
        meterTypeId: toNum(f.meterTypeId),
        ipAddress: f.ipAddress.trim() || null,
        status: f.status,
        voltage: online ? toNum(f.voltage) : null,
        current: online ? toNum(f.current) : null,
        power: online ? toNum(f.power) : null,
        reading: online ? toNum(f.reading) : null,
      })
    } catch (err) {
      setError(err)
      setSaving(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <form className="modal" onClick={(e) => e.stopPropagation()} onSubmit={submit}>
        <div className="card-head">
          <h2>{editing ? `Edit ${meter.meterCode}` : 'Add Meter'}</h2>
          <button type="button" className="kebab" onClick={onClose} aria-label="Close">
            <Icon name="close" size={20} />
          </button>
        </div>

        <div className="modal-grid">
          <label>
            Meter Code
            <input value={f.meterCode} onChange={set('meterCode')} placeholder="e.g. MTR-006" autoFocus />
            {error?.fieldErrors?.meterCode && <small className="field-error">{error.fieldErrors.meterCode}</small>}
          </label>
          <label>
            Meter Type ID
            <input type="number" value={f.meterTypeId} onChange={set('meterTypeId')} />
          </label>
          <label className="span-2">
            IP Address
            <input value={f.ipAddress} onChange={set('ipAddress')} placeholder="e.g. 192.168.1.100:100" />
          </label>
          <label className="span-2">
            Status
            <div className="select-wrap">
              <select value={f.status} onChange={set('status')}>
                <option value="ONLINE">Online</option>
                <option value="OFFLINE">Offline</option>
              </select>
              <Icon name="chevron" size={18} />
            </div>
          </label>
          <label>Voltage (V)<input type="number" step="any" value={f.voltage} onChange={set('voltage')} disabled={!online} /></label>
          <label>Current (A)<input type="number" step="any" value={f.current} onChange={set('current')} disabled={!online} /></label>
          <label>Power (W)<input type="number" step="any" value={f.power} onChange={set('power')} disabled={!online} /></label>
          <label>Reading (kWh)<input type="number" step="any" value={f.reading} onChange={set('reading')} disabled={!online} /></label>
        </div>

        {error && !error.fieldErrors && <div className="banner-error">{error.message}</div>}

        <div className="modal-actions">
          <button type="button" className="btn-soft" onClick={onClose}>Cancel</button>
          <button type="submit" className="btn-save" disabled={saving}>
            {saving ? 'Saving...' : editing ? 'Save changes' : 'Add meter'}
          </button>
        </div>
      </form>
    </div>
  )
}

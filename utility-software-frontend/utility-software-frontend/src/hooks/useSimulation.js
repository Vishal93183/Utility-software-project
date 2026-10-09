import { useCallback, useEffect, useRef, useState } from 'react'
import { api } from '../lib/api.js'

const MAX_POINTS = 30
const MAX_LOG = 60

export const DEFAULT_CONFIG = {
  intervalSec: 2,   // kitne second baad naya reading
  vMin: 225,
  vMax: 235,
  aMin: 4,
  aMax: 7,
  speed: 60,        // 1 real second = 60 simulated seconds (kWh jaldi badhe)
  offlinePct: 0,    // har tick par meter offline hone ka chance (%)
}

const rand = (a, b) => a + Math.random() * (b - a)
const r1 = (v) => Math.round(v * 10) / 10
const r2 = (v) => Math.round(v * 100) / 100
const clock = () => new Date().toLocaleTimeString('en-GB')

const base = (m, status) => ({
  meterCode: m.meterCode,
  meterTypeId: m.meterTypeId ?? null,
  ipAddress: m.ipAddress ?? null,
  status,
})

/**
 * Browser-side simulation. Har tick par chune hue meters ke liye naye
 * voltage / current / power / reading banata hai aur PUT /api/meters/{id}
 * se backend database me save karta hai. Home page ka Real Time Data View
 * wahi data backend se padhta hai.
 */
export function useSimulation({ meters, reloadMeters }) {
  const [config, setConfig] = useState(DEFAULT_CONFIG)
  const [selected, setSelected] = useState([])
  const [running, setRunning] = useState(false)
  const [ticks, setTicks] = useState(0)
  const [log, setLog] = useState([])
  const [series, setSeries] = useState([])
  const [error, setError] = useState('')

  // latest values timer ke andar bhi milen, isliye refs
  const metersRef = useRef(meters)
  const cfgRef = useRef(config)
  const selRef = useRef(selected)
  const reloadRef = useRef(reloadMeters)
  metersRef.current = meters
  cfgRef.current = config
  selRef.current = selected
  reloadRef.current = reloadMeters

  const readings = useRef({})
  const busy = useRef(false)
  const fails = useRef(0)
  const initialised = useRef(false)

  // pehli baar meters aate hi sabko select kar do
  useEffect(() => {
    if (!initialised.current && meters.length > 0) {
      initialised.current = true
      setSelected(meters.map((m) => m.id))
    }
  }, [meters])

  const runTick = useCallback(async () => {
    if (busy.current) return
    busy.current = true

    const cfg = cfgRef.current
    const targets = metersRef.current.filter((m) => selRef.current.includes(m.id))
    const entries = []
    const point = { t: Date.now(), p: {} }

    try {
      for (const m of targets) {
        const goOffline = Math.random() * 100 < cfg.offlinePct
        let body

        if (goOffline) {
          body = base(m, 'OFFLINE')
          point.p[m.id] = null
          entries.push({ kind: 'off', text: `${m.meterCode} went OFFLINE (simulated fault)` })
        } else {
          const voltage = r1(rand(cfg.vMin, cfg.vMax))
          const current = r1(rand(cfg.aMin, cfg.aMax))
          const power = r1(voltage * current)
          const prev = readings.current[m.id] ?? m.reading ?? 0
          // energy = kW x simulated hours
          const reading = r2(prev + (power / 1000) * ((cfg.intervalSec * cfg.speed) / 3600))
          readings.current[m.id] = reading

          body = { ...base(m, 'ONLINE'), voltage, current, power, reading }
          point.p[m.id] = power
          entries.push({
            kind: 'ok',
            text: `${m.meterCode}   ${voltage} V   ${current} A   ${power} W   ${reading} kWh`,
          })
        }
        await api.updateMeter(m.id, body)
      }
      fails.current = 0
      setError('')
      await reloadRef.current()
    } catch (e) {
      fails.current += 1
      entries.push({ kind: 'err', text: e.message })
      setError(e.message)
      if (fails.current >= 3) setRunning(false) // 3 baar lagataar fail -> stop
    } finally {
      busy.current = false
    }

    const time = clock()
    setLog((l) => [...entries.map((e) => ({ ...e, time })).reverse(), ...l].slice(0, MAX_LOG))
    if (Object.keys(point.p).length > 0) setSeries((s) => [...s, point].slice(-MAX_POINTS))
    setTicks((t) => t + 1)
  }, [])

  // timer
  useEffect(() => {
    if (!running) return
    const id = setInterval(runTick, config.intervalSec * 1000)
    return () => clearInterval(id)
  }, [running, config.intervalSec, runTick])

  const start = () => {
    if (selRef.current.length === 0) {
      setError('Select at least one meter to simulate.')
      return
    }
    readings.current = {}
    fails.current = 0
    setError('')
    setRunning(true)
    runTick()
  }

  const stop = () => setRunning(false)

  const clear = () => {
    setLog([])
    setSeries([])
    setTicks(0)
    setError('')
  }

  return { config, setConfig, selected, setSelected, running, start, stop, clear, ticks, log, series, error }
}

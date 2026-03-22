import { useNavigate } from 'react-router'
import { useEffect, useState } from 'react'
import { listNegotiators, type Negotiator } from '../api'

export default function Home() {
  const navigate = useNavigate()
  const [negotiators, setNegotiators] = useState<Negotiator[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    listNegotiators()
      .then(setNegotiators)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="page">
      <h1>Pactum Negotiator</h1>
      <p className="subtitle">Negotiate with an AI bot or build your own</p>

      <div className="home-actions">
        <div className="home-card">
          <h2>Negotiate as Guest</h2>
          <p>Pick a bot and start negotiating</p>
          {loading ? (
            <p className="muted">Loading bots...</p>
          ) : negotiators.length === 0 ? (
            <p className="muted">No bots created yet. Build one first.</p>
          ) : (
            <div className="bot-list">
              {negotiators.map((n) => (
                <button
                  key={n.id}
                  className="bot-pick"
                  onClick={() => navigate(`/negotiate/${n.id}`)}
                >
                  <span className="bot-name">{n.name}</span>
                  <span className="bot-strategy">{n.strategy}</span>
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="home-card">
          <h2>Build a Negotiation Bot</h2>
          <p>Configure goals, limits, and strategy</p>
          <button className="btn-primary" onClick={() => navigate('/build')}>
            Create Bot
          </button>
        </div>
      </div>
    </div>
  )
}

import { useParams, useNavigate } from 'react-router'
import { useEffect, useState } from 'react'
import { getNegotiator, getPreferences, getOrCreateGuest, type Negotiator, type TermPreference } from '../api'

export default function Negotiate() {
  const { botId } = useParams()
  const navigate = useNavigate()
  const [bot, setBot] = useState<Negotiator | null>(null)
  const [preferences, setPreferences] = useState<TermPreference[]>([])
  const [guest, setGuest] = useState<{ id: number; name: string } | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!botId) return
    Promise.all([
      getNegotiator(+botId),
      getPreferences(+botId),
      getOrCreateGuest(),
    ])
      .then(([b, p, g]) => {
        setBot(b)
        setPreferences(p)
        setGuest(g)
      })
      .catch(() => navigate('/'))
      .finally(() => setLoading(false))
  }, [botId, navigate])

  if (loading) return <div className="page"><p>Loading...</p></div>
  if (!bot || !guest) return null

  return (
    <div className="page">
      <div className="build-header">
        <button className="btn-back" onClick={() => navigate('/')}>&larr; Back</button>
        <h1>Negotiating with {bot.name}</h1>
      </div>

      <p className="muted">Playing as: {guest.name}</p>

      <div className="negotiate-placeholder">
        <h2>Terms on the table</h2>
        <div className="terms-list">
          {preferences.map((p) => (
            <div key={p.id} className="term-card">
              <strong>{p.negotiationTerm.name}</strong>
              <span className="muted">{p.negotiationTerm.unit}</span>
              <div className="term-range">
                {p.negotiationTerm.min} &mdash; {p.negotiationTerm.max}
              </div>
              <p className="term-desc">{p.negotiationTerm.description}</p>
            </div>
          ))}
        </div>

        <p className="muted" style={{ marginTop: '2rem' }}>
          Negotiation UI coming soon &mdash; WebSocket integration needed
        </p>
      </div>
    </div>
  )
}

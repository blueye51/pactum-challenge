import { useNavigate } from 'react-router'
import { useEffect, useState } from 'react'
import { listNegotiators, getOrCreateGuest, getPreferences, type Negotiator, type TermPreference } from '../api'

export default function GuestLobby() {
  const navigate = useNavigate()
  const [guest, setGuest] = useState<{ id: number; name: string } | null>(null)
  const [negotiators, setNegotiators] = useState<Negotiator[]>([])
  const [prefsMap, setPrefsMap] = useState<Record<number, TermPreference[]>>({})
  const [loading, setLoading] = useState(true)
  const [expanded, setExpanded] = useState<number | null>(null)

  useEffect(() => {
    Promise.all([getOrCreateGuest(), listNegotiators()])
      .then(async ([g, bots]) => {
        setGuest(g)
        setNegotiators(bots)
        const map: Record<number, TermPreference[]> = {}
        await Promise.all(
          bots.map(async (b) => {
            map[b.id] = await getPreferences(b.id)
          })
        )
        setPrefsMap(map)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="page"><p>Loading...</p></div>

  return (
    <div className="page">
      <div className="build-header">
        <button className="btn-back" onClick={() => navigate('/')}>&larr; Back</button>
        <h1>Choose a Bot</h1>
      </div>

      {guest && <p className="muted">Playing as: {guest.name}</p>}

      {negotiators.length === 0 ? (
        <div className="empty-state">
          <p>No bots available yet.</p>
          <button className="btn-primary" onClick={() => navigate('/build')}>
            Create one
          </button>
        </div>
      ) : (
        <div className="bot-cards">
          {negotiators.map((n) => {
            const prefs = prefsMap[n.id] || []
            const isExpanded = expanded === n.id

            return (
              <div key={n.id} className="bot-card">
                <div className="bot-card-header" onClick={() => setExpanded(isExpanded ? null : n.id)}>
                  <div className="bot-card-info">
                    <strong>{n.name}</strong>
                    <span className="bot-strategy">{n.strategy}</span>
                  </div>
                  <span className={`expand-arrow ${isExpanded ? 'open' : ''}`}>&#9662;</span>
                </div>

                {isExpanded && (
                  <div className="bot-card-details">
                    <div className="bot-card-stats">
                      <span>Max rounds: {n.maxOffersCount}</span>
                    </div>

                    {prefs.length > 0 && (
                      <>
                        <h3>Terms this bot negotiates</h3>
                        <div className="bot-card-terms">
                          {prefs.map((p) => (
                            <div key={p.id} className="bot-term">
                              <span className="bot-term-name">{p.negotiationTerm.name}</span>
                              <span className="muted">{p.negotiationTerm.unit}</span>
                              <span className="bot-term-range">
                                {p.negotiationTerm.min} – {p.negotiationTerm.max}
                              </span>
                            </div>
                          ))}
                        </div>
                      </>
                    )}

                    <button
                      className="btn-primary"
                      style={{ marginTop: '16px', width: '100%' }}
                      onClick={() => navigate(`/negotiate/${n.id}`)}
                    >
                      Start Negotiation
                    </button>
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

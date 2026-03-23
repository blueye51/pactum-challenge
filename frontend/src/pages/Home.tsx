import { useNavigate } from 'react-router'

export default function Home() {
  const navigate = useNavigate()

  return (
    <div className="page home-page">
      <h1>Pactum Negotiator</h1>
      <p className="subtitle">Negotiate with an AI bot or build your own</p>

      <div className="home-actions">
        <button className="home-card home-card-clickable" onClick={() => navigate('/guest')}>
          <h2>Negotiate as Guest</h2>
          <p>Join as a guest and negotiate against a bot</p>
        </button>

        <button className="home-card home-card-clickable" onClick={() => navigate('/build')}>
          <h2>Build a Negotiation Bot</h2>
          <p>Configure goals, limits, and strategy</p>
        </button>
      </div>
    </div>
  )
}

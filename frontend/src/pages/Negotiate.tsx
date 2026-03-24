import { useParams, useNavigate } from 'react-router'
import { useEffect, useState, useRef } from 'react'
import { Client } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import NumberInput from '../components/NumberInput'
import {
  createSession,
  getOrCreateGuest,
  getNegotiator,
  getPreferences,
  getSessionMessages,
  type NegotiationSession,
  type Negotiator,
  type ChatMsg,
  type TermPreference,
  type OfferTermValue,
  type SessionStatus,
} from '../api'

export default function Negotiate() {
  const { botId } = useParams()
  const navigate = useNavigate()

  const [session, setSession] = useState<NegotiationSession | null>(null)
  const [bot, setBot] = useState<Negotiator | null>(null)
  const [guest, setGuest] = useState<{ id: number; name: string } | null>(null)
  const [preferences, setPreferences] = useState<TermPreference[]>([])
  const [messages, setMessages] = useState<ChatMsg[]>([])
  const [input, setInput] = useState('')
  const [connected, setConnected] = useState(false)
  const [loading, setLoading] = useState(true)
  const [thinking, setThinking] = useState(false)
  const [sessionStatus, setSessionStatus] = useState<SessionStatus>('ACTIVE')

  // Offer panel + pending attachment
  const [showOffer, setShowOffer] = useState(false)
  const [offerValues, setOfferValues] = useState<Record<number, number>>({})
  const [pendingOffer, setPendingOffer] = useState<OfferTermValue[] | null>(null)

  const clientRef = useRef<Client | null>(null)
  const messagesEndRef = useRef<HTMLDivElement>(null)

  const ended = sessionStatus === 'ACCEPTED' || sessionStatus === 'REJECTED'

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, thinking])

  useEffect(() => {
    if (!botId) return

    let stompClient: Client | null = null

    async function init() {
      try {
        const [g, b, prefs] = await Promise.all([
          getOrCreateGuest(),
          getNegotiator(+botId!),
          getPreferences(+botId!),
        ])
        setGuest(g)
        setBot(b)
        setPreferences(prefs)

        const initial: Record<number, number> = {}
        for (const p of prefs) {
          const mid = (p.negotiationTerm.min + p.negotiationTerm.max) / 2
          initial[p.negotiationTerm.id] = p.negotiationTerm.wholeNumber ? Math.round(mid) : mid
        }
        setOfferValues(initial)

        const s = await createSession(+botId!)
        setSession(s)
        setSessionStatus(s.sessionStatus)

        // Load existing chat history if rejoining
        const history = await getSessionMessages(s.id)
        if (history.length > 0) {
          setMessages(history)
          // Pick up session status from last message
          const lastWithStatus = [...history].reverse().find((m) => m.sessionStatus)
          if (lastWithStatus?.sessionStatus) {
            setSessionStatus(lastWithStatus.sessionStatus)
          }
        }

        stompClient = new Client({
          webSocketFactory: () => new SockJS('/ws'),
          onConnect: () => {
            setConnected(true)
            stompClient!.subscribe(`/topic/session/${s.id}`, (frame) => {
              const data = JSON.parse(frame.body) as ChatMsg
              // Skip our own messages — we add them locally
              if (data.sender === 'user') return
              setThinking(false)
              setMessages((prev) => [...prev, data])
              // Update session status if the bot response carries one
              if (data.sessionStatus) {
                setSessionStatus(data.sessionStatus)
              }
            })
          },
          onDisconnect: () => setConnected(false),
          reconnectDelay: 5000,
        })

        clientRef.current = stompClient
        stompClient.activate()
      } catch {
        navigate('/guest')
      } finally {
        setLoading(false)
      }
    }

    init()

    return () => {
      if (stompClient?.active) {
        stompClient.deactivate()
      }
    }
  }, [botId, navigate])

  function attachOffer() {
    const terms: OfferTermValue[] = preferences.map((p) => ({
      termId: p.negotiationTerm.id,
      termName: p.negotiationTerm.name,
      termUnit: p.negotiationTerm.unit,
      value: offerValues[p.negotiationTerm.id] ?? 0,
    }))
    setPendingOffer(terms)
    setShowOffer(false)
  }

  function removePendingOffer() {
    setPendingOffer(null)
  }

  function sendMessage() {
    if (!session || !clientRef.current?.active || ended) return
    if (!input.trim() && !pendingOffer) return

    // What we show locally
    const msg: ChatMsg = {
      type: 'message',
      sender: 'user',
      content: input.trim(),
      timestamp: Date.now(),
      ...(pendingOffer ? { offer: { terms: pendingOffer, status: 'PENDING' as const } } : {}),
    }

    // What we send to backend (matches IncomingMessage shape)
    const payload = {
      content: input.trim(),
      ...(pendingOffer ? { offer: { terms: pendingOffer } } : {}),
    }

    const destination = pendingOffer
      ? `/app/session/${session.id}/offer`
      : `/app/session/${session.id}/message`

    clientRef.current.publish({
      destination,
      body: JSON.stringify(payload),
    })

    setMessages((prev) => [...prev, msg])
    setInput('')
    setPendingOffer(null)
    setThinking(true)
  }

  function acceptOffer() {
    if (!session || !clientRef.current?.active || ended) return

    const msg: ChatMsg = {
      type: 'message',
      sender: 'user',
      content: 'I accept this offer.',
      timestamp: Date.now(),
    }

    clientRef.current.publish({
      destination: `/app/session/${session.id}/accept`,
      body: JSON.stringify({ content: 'I accept this offer.' }),
    })

    setMessages((prev) => [...prev, msg])
    setThinking(true)
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  if (loading) return <div className="page"><p>Loading...</p></div>
  if (!bot || !guest || !session) return null

  return (
    <div className="chat-page">
      <div className="chat-header">
        <button className="btn-back" onClick={() => navigate('/guest')}>&larr;</button>
        <div className="chat-header-info">
          <strong>{bot.name}</strong>
          <span className="muted">{connected ? 'Connected' : 'Connecting...'}</span>
        </div>
      </div>

      <div className="chat-messages">
        <div className="chat-system-msg">
          Negotiation started with {bot.name}. You are playing as {guest.name}.
        </div>

        {messages.map((msg, i) => (
          <div key={i} className={`chat-bubble ${msg.sender === 'user' ? 'chat-user' : 'chat-bot'}`}>
            {msg.content && <div className="chat-bubble-content">{msg.content}</div>}
            {msg.offer && (
              <div className={`offer-card offer-${msg.offer.status.toLowerCase()}`}>
                <div className="offer-card-header">
                  <span className="offer-card-title">Offer</span>
                  <span className={`offer-status offer-status-${msg.offer.status.toLowerCase()}`}>
                    {msg.offer.status}
                  </span>
                </div>
                <div className="offer-card-grid">
                  {msg.offer.terms.map((t, j) => (
                    <div key={j} className="offer-card-row">
                      <span className="offer-card-term">{t.termName}</span>
                      <span className="offer-card-value">{t.value} <span className="offer-card-unit">{t.termUnit}</span></span>
                    </div>
                  ))}
                </div>
                {msg.sender === 'bot' && msg.offer.status === 'PENDING' && !ended && (
                  <button className="btn-accept-offer" onClick={acceptOffer} disabled={!connected}>
                    Accept Offer
                  </button>
                )}
              </div>
            )}
          </div>
        ))}

        {thinking && (
          <div className="chat-bubble chat-bot">
            <div className="typing-indicator">
              <span></span><span></span><span></span>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {showOffer && !ended && (
        <div className="offer-panel">
          <div className="offer-panel-header">
            <h3>Prepare Offer</h3>
            <button className="btn-remove" onClick={() => setShowOffer(false)}>&times;</button>
          </div>
          <div className="offer-terms">
            {preferences.map((p) => (
              <div key={p.negotiationTerm.id} className="offer-term-row">
                <div className="offer-term-info">
                  <span className="offer-term-name">{p.negotiationTerm.name}</span>
                  <span className="muted">{p.negotiationTerm.unit}</span>
                  <span className="offer-term-range">{p.negotiationTerm.min} – {p.negotiationTerm.max}</span>
                </div>
                <NumberInput
                  className="input offer-term-input"
                  value={offerValues[p.negotiationTerm.id] ?? 0}
                  onChange={(v) => setOfferValues({ ...offerValues, [p.negotiationTerm.id]: v })}
                  min={p.negotiationTerm.min}
                  max={p.negotiationTerm.max}
                  wholeNumber={p.negotiationTerm.wholeNumber}
                />
              </div>
            ))}
          </div>
          <button className="btn-primary offer-submit" onClick={attachOffer} disabled={!connected}>
            Attach to Message
          </button>
        </div>
      )}

      {pendingOffer && !ended && (
        <div className="pending-offer-bar">
          <div className="pending-offer-preview">
            <span className="pending-offer-label">Offer attached</span>
            <span className="muted">{pendingOffer.map((t) => `${t.termName}: ${t.value}`).join(', ')}</span>
          </div>
          <button className="btn-remove" onClick={removePendingOffer}>&times;</button>
        </div>
      )}

      {ended ? (
        <div className={`session-ended-bar session-ended-${sessionStatus.toLowerCase()}`}>
          <span>
            {sessionStatus === 'ACCEPTED'
              ? 'Deal accepted! The negotiation has concluded successfully.'
              : 'The negotiation has ended. The bot walked away.'}
          </span>
          <button className="btn-primary" onClick={() => navigate('/guest')}>Back to Lobby</button>
        </div>
      ) : (
        <div className="chat-input-bar">
          <button
            className="btn-offer"
            onClick={() => setShowOffer(!showOffer)}
            disabled={!connected}
          >
            Offer
          </button>
          <input
            className="chat-input"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={pendingOffer ? 'Add a message with your offer...' : 'Type a message...'}
            disabled={!connected}
          />
          <button
            className="btn-send"
            onClick={sendMessage}
            disabled={!connected || (!input.trim() && !pendingOffer)}
          >
            Send
          </button>
        </div>
      )}
    </div>
  )
}

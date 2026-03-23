export type Strategy = 'INFINITE' | 'BALANCED' | 'CONCEDING'
export type Direction = 'MINIMIZE' | 'MAXIMIZE'

export interface Negotiator {
  id: number
  name: string
  acceptanceThreshold: number
  walkawayThreshold: number
  concessionRate: number
  maxOffersCount: number
  strategy: Strategy
}

export interface TermPreference {
  id: number
  negotiationTerm: {
    id: number
    name: string
    unit: string
    min: number
    max: number
    description: string
  }
  direction: Direction
  idealValue: number
  limitValue: number
  weight: number
  strictness: number
  reasoning: string | null
}

export interface CreateNegotiatorRequest {
  name: string
  acceptanceThreshold: number
  walkawayThreshold: number
  concessionRate: number
  maxOffersCount: number
  strategy: Strategy
}

export interface NegotiationTerm {
  id: number
  name: string
  unit: string
  min: number
  max: number
  description: string
}

export interface AddTermPreferenceRequest {
  termId?: number
  termName?: string
  termUnit?: string
  termDescription?: string
  termMin?: number
  termMax?: number
  direction: Direction
  idealValue: number
  limitValue: number
  weight: number
  strictness: number
  reasoning?: string
}

export interface NegotiationSession {
  id: number
  negotiator: Negotiator
  user: { id: number; name: string }
  score: number
}

export type OfferStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED' | 'COUNTERED'

export interface OfferTermValue {
  termId: number
  termName: string
  termUnit: string
  value: number
}

export interface ChatMsg {
  type: 'message'
  sender: 'user' | 'bot'
  content: string
  timestamp: number
  offer?: {
    terms: OfferTermValue[]
    status: OfferStatus
  }
}

const BASE = '/api'

export async function createNegotiator(req: CreateNegotiatorRequest): Promise<Negotiator> {
  const res = await fetch(`${BASE}/negotiators`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function addPreference(negotiatorId: number, req: AddTermPreferenceRequest): Promise<TermPreference> {
  const res = await fetch(`${BASE}/negotiators/${negotiatorId}/preferences`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function listNegotiators(): Promise<Negotiator[]> {
  const res = await fetch(`${BASE}/negotiators`)
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function getNegotiator(id: number): Promise<Negotiator> {
  const res = await fetch(`${BASE}/negotiators/${id}`)
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function getPreferences(negotiatorId: number): Promise<TermPreference[]> {
  const res = await fetch(`${BASE}/negotiators/${negotiatorId}/preferences`)
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function listTerms(): Promise<NegotiationTerm[]> {
  const res = await fetch(`${BASE}/terms`)
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function createSession(negotiatorId: number): Promise<NegotiationSession> {
  const res = await fetch(`${BASE}/sessions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ negotiatorId }),
  })
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function getOrCreateGuest(): Promise<{ id: number; name: string }> {
  const res = await fetch(`${BASE}/me`)
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

export async function getSessionMessages(sessionId: number): Promise<ChatMsg[]> {
  const res = await fetch(`${BASE}/sessions/${sessionId}/messages`)
  if (!res.ok) throw new Error(await res.text())
  return res.json()
}

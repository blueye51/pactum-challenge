import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router'
import NumberInput from '../components/NumberInput'
import HelpTip from '../components/HelpTip'
import {
  createNegotiator,
  addPreference,
  listTerms,
  type Strategy,
  type Direction,
  type CreateNegotiatorRequest,
  type AddTermPreferenceRequest,
  type NegotiationTerm,
} from '../api'

type Step = 'name' | 'settings' | 'preferences' | 'review'

interface PendingPreference extends AddTermPreferenceRequest {
  key: number
  displayName: string
  displayUnit: string
}

export default function BuildBot() {
  const navigate = useNavigate()
  const [step, setStep] = useState<Step>('name')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Step 1: Name + Market Context
  const [name, setName] = useState('')
  const [marketContext, setMarketContext] = useState('')

  // Step 2: Settings
  const [acceptanceThreshold, setAcceptanceThreshold] = useState(0.7)
  const [walkawayThreshold, setWalkawayThreshold] = useState(0.3)
  const [concessionRate, setConcessionRate] = useState(0.05)
  const [maxOffersCount, setMaxOffersCount] = useState(10)
  const [strategy, setStrategy] = useState<Strategy>('BALANCED')

  // Step 3: Preferences
  const [preferences, setPreferences] = useState<PendingPreference[]>([])
  const [nextKey, setNextKey] = useState(0)
  const [terms, setTerms] = useState<NegotiationTerm[]>([])

  // Preference form
  const [showPrefForm, setShowPrefForm] = useState(false)
  const [selectedTermId, setSelectedTermId] = useState<string>('custom')
  const [prefForm, setPrefForm] = useState(emptyPref())

  useEffect(() => {
    listTerms().then(setTerms).catch(() => {})
  }, [])

  function emptyPref() {
    return {
      termName: '',
      termUnit: '',
      termDescription: '',
      termMin: 0,
      termMax: 100,
      idealValue: 0,
      limitValue: 0,
      weight: 0.5,
      strictness: 0.5,
      reasoning: '',
    }
  }

  function handleTermSelect(value: string) {
    setSelectedTermId(value)
    if (value === 'custom') {
      setPrefForm(emptyPref())
      return
    }
    const term = terms.find((t) => t.id === +value)
    if (term) {
      setPrefForm({
        ...prefForm,
        termName: term.name,
        termUnit: term.unit,
        termDescription: term.description,
        termMin: term.min,
        termMax: term.max,
        idealValue: 0,
        limitValue: 0,
      })
    }
  }

  function addPref() {
    const isExisting = selectedTermId !== 'custom'
    const term = isExisting ? terms.find((t) => t.id === +selectedTermId) : null

    if (!isExisting && (!prefForm.termName || !prefForm.termUnit || !prefForm.termDescription)) return

    const direction: Direction = prefForm.idealValue <= prefForm.limitValue ? 'MINIMIZE' : 'MAXIMIZE'

    const req: AddTermPreferenceRequest = isExisting && term
      ? {
          termId: term.id,
          direction,
          idealValue: prefForm.idealValue,
          limitValue: prefForm.limitValue,
          weight: prefForm.weight,
          strictness: prefForm.strictness,
          reasoning: prefForm.reasoning,
        }
      : {
          termName: prefForm.termName,
          termUnit: prefForm.termUnit,
          termDescription: prefForm.termDescription,
          termMin: prefForm.termMin,
          termMax: prefForm.termMax,
          direction,
          idealValue: prefForm.idealValue,
          limitValue: prefForm.limitValue,
          weight: prefForm.weight,
          strictness: prefForm.strictness,
          reasoning: prefForm.reasoning,
        }

    setPreferences([
      ...preferences,
      {
        ...req,
        key: nextKey,
        displayName: term?.name ?? prefForm.termName ?? '',
        displayUnit: term?.unit ?? prefForm.termUnit ?? '',
      },
    ])
    setNextKey(nextKey + 1)
    setPrefForm(emptyPref())
    setSelectedTermId('custom')
    setShowPrefForm(false)
  }

  function removePref(key: number) {
    setPreferences(preferences.filter((p) => p.key !== key))
  }

  const isCustom = selectedTermId === 'custom'
  const selectedTerm = !isCustom ? terms.find((t) => t.id === +selectedTermId) : null
  const rangeMin = selectedTerm ? selectedTerm.min : prefForm.termMin
  const rangeMax = selectedTerm ? selectedTerm.max : prefForm.termMax

  const canAddPref = isCustom
    ? !!(prefForm.termName && prefForm.termUnit && prefForm.termDescription)
    : selectedTermId !== ''

  async function submit() {
    setSubmitting(true)
    setError(null)
    try {
      const req: CreateNegotiatorRequest = {
        name,
        acceptanceThreshold,
        walkawayThreshold,
        concessionRate,
        maxOffersCount,
        strategy,
        marketContext: marketContext.trim() || undefined,
      }
      const negotiator = await createNegotiator(req)

      for (const pref of preferences) {
        const { key: _k, displayName: _dn, displayUnit: _du, ...prefReq } = pref
        await addPreference(negotiator.id, prefReq)
      }

      navigate('/')
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Something went wrong')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="page">
      <div className="build-header">
        <button className="btn-back" onClick={() => navigate('/')}>
          &larr; Cancel
        </button>
        <h1>Build a Bot</h1>
        <div className="steps-indicator">
          {(['name', 'settings', 'preferences', 'review'] as Step[]).map((s, i) => (
            <span key={s} className={`step-dot ${step === s ? 'active' : ''} ${i < ['name', 'settings', 'preferences', 'review'].indexOf(step) ? 'done' : ''}`} />
          ))}
        </div>
      </div>

      {error && <div className="error-banner">{error}</div>}

      {step === 'name' && (
        <div className="step-content">
          <h2>Name your bot</h2>
          <p>Give your negotiation bot a name</p>
          <input
            type="text"
            className="input"
            placeholder="e.g. HardBaller, FairDealer..."
            value={name}
            onChange={(e) => setName(e.target.value)}
            autoFocus
          />

          <label className="form-field" style={{ marginTop: 24 }}>
            <span className="label-text">
              Market Context
              <HelpTip text="Describe what the bot is buying, the market it operates in, typical prices and timelines. This is shown to the supplier at the start of the negotiation so they know what they're dealing with." />
            </span>
            <textarea
              className="input"
              rows={4}
              placeholder="e.g. We're a retail chain sourcing phone accessories from Asian manufacturers. Typical unit prices are $2-$15, delivery 30-60 days by sea..."
              value={marketContext}
              onChange={(e) => setMarketContext(e.target.value)}
              style={{ resize: 'vertical' }}
            />
          </label>

          <div className="step-actions">
            <button
              className="btn-primary"
              disabled={!name.trim()}
              onClick={() => setStep('settings')}
            >
              Next
            </button>
          </div>
        </div>
      )}

      {step === 'settings' && (
        <div className="step-content">
          <h2>Configure Settings</h2>
          <p>Set the bot's negotiation parameters</p>

          <div className="form-grid">
            <label className="form-field">
              <span className="label-text">Strategy</span>
              <select
                className="input"
                value={strategy}
                onChange={(e) => setStrategy(e.target.value as Strategy)}
              >
                <option value="BALANCED">Balanced</option>
                <option value="INFINITE">Infinite</option>
                <option value="CONCEDING">Conceding</option>
              </select>
            </label>

            <label className="form-field">
              <span className="label-text">
                Acceptance Threshold
                <span className="label-hint">{acceptanceThreshold.toFixed(2)}</span>
              </span>
              <input
                type="range"
                min="0"
                max="1"
                step="0.01"
                value={acceptanceThreshold}
                onChange={(e) => setAcceptanceThreshold(+e.target.value)}
              />
            </label>

            <label className="form-field">
              <span className="label-text">
                Walkaway Threshold
                <span className="label-hint">{walkawayThreshold.toFixed(2)}</span>
              </span>
              <input
                type="range"
                min="0"
                max="1"
                step="0.01"
                value={walkawayThreshold}
                onChange={(e) => setWalkawayThreshold(+e.target.value)}
              />
            </label>

            <label className="form-field">
              <span className="label-text">
                Concession Rate
                <span className="label-hint">{concessionRate.toFixed(2)}</span>
              </span>
              <input
                type="range"
                min="0"
                max="0.2"
                step="0.01"
                value={concessionRate}
                onChange={(e) => setConcessionRate(+e.target.value)}
              />
            </label>

            <label className="form-field">
              <span className="label-text">Max Offers</span>
              <NumberInput
                className="input"
                min={1}
                max={50}
                value={maxOffersCount}
                onChange={setMaxOffersCount}
              />
            </label>
          </div>

          <div className="step-actions">
            <button className="btn-secondary" onClick={() => setStep('name')}>
              Back
            </button>
            <button className="btn-primary" onClick={() => setStep('preferences')}>
              Next
            </button>
          </div>
        </div>
      )}

      {step === 'preferences' && (
        <div className="step-content">
          <h2>Term Preferences</h2>
          <p>Add what the bot cares about when negotiating</p>

          {preferences.length > 0 && (
            <div className="pref-list">
              {preferences.map((p) => (
                <div key={p.key} className="pref-card">
                  <div className="pref-card-header">
                    <strong>{p.displayName}</strong>
                    <span className={`direction-badge ${p.direction.toLowerCase()}`}>
                      {p.direction}
                    </span>
                    <button className="btn-remove" onClick={() => removePref(p.key)}>
                      &times;
                    </button>
                  </div>
                  <div className="pref-card-details">
                    <span>Ideal: {p.idealValue}</span>
                    <span>Limit: {p.limitValue}</span>
                    <span>Weight: {p.weight}</span>
                    <span>Strictness: {p.strictness}</span>
                  </div>
                </div>
              ))}
            </div>
          )}

          {showPrefForm ? (
            <div className="pref-form">
              <h3>New Term Preference</h3>

              <div className="form-grid">
                <label className="form-field full-width">
                  <span className="label-text">Select a Term</span>
                  <select
                    className="input"
                    value={selectedTermId}
                    onChange={(e) => handleTermSelect(e.target.value)}
                    autoFocus
                  >
                    {terms.map((t) => (
                      <option key={t.id} value={t.id}>
                        {t.name} ({t.unit}) &mdash; {t.description}
                      </option>
                    ))}
                    <option value="custom">+ Create custom term</option>
                  </select>
                </label>

                {isCustom && (
                  <>
                    <label className="form-field">
                      <span className="label-text">Term Name</span>
                      <input
                        className="input"
                        placeholder="e.g. Warranty Period..."
                        value={prefForm.termName}
                        onChange={(e) => setPrefForm({ ...prefForm, termName: e.target.value })}
                      />
                    </label>

                    <label className="form-field">
                      <span className="label-text">Unit</span>
                      <input
                        className="input"
                        placeholder="e.g. USD, days, months..."
                        value={prefForm.termUnit}
                        onChange={(e) => setPrefForm({ ...prefForm, termUnit: e.target.value })}
                      />
                    </label>

                    <label className="form-field full-width">
                      <span className="label-text">Description</span>
                      <input
                        className="input"
                        placeholder="What does this term represent?"
                        value={prefForm.termDescription}
                        onChange={(e) =>
                          setPrefForm({ ...prefForm, termDescription: e.target.value })
                        }
                      />
                    </label>

                    <label className="form-field">
                      <span className="label-text">Term Min</span>
                      <NumberInput
                        className="input"
                        value={prefForm.termMin}
                        onChange={(v) => setPrefForm({ ...prefForm, termMin: v })}
                      />
                    </label>

                    <label className="form-field">
                      <span className="label-text">Term Max</span>
                      <NumberInput
                        className="input"
                        value={prefForm.termMax}
                        onChange={(v) => setPrefForm({ ...prefForm, termMax: v })}
                      />
                    </label>
                  </>
                )}

                <label className="form-field">
                  <span className="label-text">
                    Ideal Value
                    <span className="label-hint">{rangeMin} – {rangeMax}</span>
                    <HelpTip text="The best possible value for the bot. This is what it would pick in a perfect deal — e.g. the lowest price or fastest delivery." />
                  </span>
                  <NumberInput
                    className="input"
                    value={prefForm.idealValue}
                    onChange={(v) => setPrefForm({ ...prefForm, idealValue: v })}
                  />
                </label>

                <label className="form-field">
                  <span className="label-text">
                    Limit Value
                    <span className="label-hint">{rangeMin} – {rangeMax}</span>
                    <HelpTip text="The worst value the bot will still accept. Beyond this the deal isn't worth it — e.g. the max price it'll pay or the slowest delivery it'll tolerate." />
                  </span>
                  <NumberInput
                    className="input"
                    value={prefForm.limitValue}
                    onChange={(v) => setPrefForm({ ...prefForm, limitValue: v })}
                  />
                </label>

                <label className="form-field">
                  <span className="label-text">
                    Weight
                    <span className="label-hint">{prefForm.weight.toFixed(2)}</span>
                    <HelpTip text="How important this term is to the overall deal score. Higher weight = bigger impact on whether the bot accepts or rejects." />
                  </span>
                  <input
                    type="range"
                    min="0"
                    max="1"
                    step="0.01"
                    value={prefForm.weight}
                    onChange={(e) => setPrefForm({ ...prefForm, weight: +e.target.value })}
                  />
                </label>

                <label className="form-field">
                  <span className="label-text">
                    Strictness
                    <span className="label-hint">{prefForm.strictness.toFixed(2)}</span>
                    <HelpTip text="How willing the bot is to move on this term. Low = flexible, easy to concede (e.g. price). High = firm, hard to budge (e.g. delivery deadline)." />
                  </span>
                  <input
                    type="range"
                    min="0"
                    max="1"
                    step="0.01"
                    value={prefForm.strictness}
                    onChange={(e) => setPrefForm({ ...prefForm, strictness: +e.target.value })}
                  />
                </label>

                <label className="form-field full-width">
                  <span className="label-text">
                    Reasoning (optional)
                    <HelpTip text="The business context behind this preference. This helps explain the bot's stance — e.g. why it pushes hard on price or is flexible on delivery." />
                  </span>
                  <input
                    className="input"
                    placeholder="e.g. Lower price helps us stay within quarterly budget"
                    value={prefForm.reasoning}
                    onChange={(e) => setPrefForm({ ...prefForm, reasoning: e.target.value })}
                  />
                </label>
              </div>

              <div className="step-actions">
                <button className="btn-secondary" onClick={() => { setShowPrefForm(false); setSelectedTermId('custom') }}>
                  Cancel
                </button>
                <button
                  className="btn-primary"
                  disabled={!canAddPref}
                  onClick={addPref}
                >
                  Add Preference
                </button>
              </div>
            </div>
          ) : (
            <button className="btn-add" onClick={() => setShowPrefForm(true)}>
              + Add Term Preference
            </button>
          )}

          <div className="step-actions">
            <button className="btn-secondary" onClick={() => setStep('settings')}>
              Back
            </button>
            <button
              className="btn-primary"
              disabled={preferences.length === 0}
              onClick={() => setStep('review')}
            >
              Next
            </button>
          </div>
        </div>
      )}

      {step === 'review' && (
        <div className="step-content">
          <h2>Review & Create</h2>
          <p>Everything looks good? This will create your bot.</p>

          <div className="review-section">
            <h3>{name}</h3>
            <div className="review-grid">
              <div><span className="muted">Strategy:</span> {strategy}</div>
              <div><span className="muted">Acceptance:</span> {acceptanceThreshold}</div>
              <div><span className="muted">Walkaway:</span> {walkawayThreshold}</div>
              <div><span className="muted">Concession Rate:</span> {concessionRate}</div>
              <div><span className="muted">Max Offers:</span> {maxOffersCount}</div>
            </div>

            {marketContext.trim() && (
              <div style={{ marginBottom: 16 }}>
                <span className="muted">Market Context:</span>
                <p style={{ margin: '4px 0' }}>{marketContext}</p>
              </div>
            )}

            <h3>Preferences ({preferences.length})</h3>
            {preferences.map((p) => (
              <div key={p.key} className="review-pref">
                <strong>{p.displayName}</strong> ({p.displayUnit}) &mdash; {p.direction}
                <br />
                Ideal: {p.idealValue}, Limit: {p.limitValue}, Weight: {p.weight}, Strictness: {p.strictness}
                {p.reasoning && <><br /><span className="muted">{p.reasoning}</span></>}
              </div>
            ))}
          </div>

          <div className="step-actions">
            <button className="btn-secondary" onClick={() => setStep('preferences')} disabled={submitting}>
              Back
            </button>
            <button className="btn-primary" onClick={submit} disabled={submitting}>
              {submitting ? 'Creating...' : 'Create Bot'}
            </button>
          </div>
        </div>
      )}
    </div>
  )
}

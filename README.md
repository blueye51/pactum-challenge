# Pactum Challenge — Autonomous Negotiation Agent

A buyer-side negotiation bot that humans can negotiate against in real time.
The human plays the supplier, the bot plays the buyer. They go back and forth on terms like price, delivery time, payment terms, and contract length until a deal is reached — or the bot walks away.

Built with Spring Boot, React, WebSockets (STOMP), PostgreSQL, and DeepSeek LLM.

## Running

### Prerequisites

- Docker and Docker Compose
- A DeepSeek API key

### Setup

```bash
cp .env.example .env
# Edit .env and add your DEEPSEEK_API_KEY
```

### Start

```bash
docker-compose up --build
```

The app will be available at **http://localhost:8080** (frontend + API served together).

### Local Development (without Docker)

Start just the database:
```bash
docker-compose up postgres
```

Then run the backend and frontend separately:
```bash
cd backend/pactum_challange && ./mvnw spring-boot:run
cd frontend && npm install && npm run dev
```

In dev mode, the Vite dev server at `http://localhost:5173` proxies `/api` and `/ws` to `localhost:8080`.

## Tech Stack

- **Backend:** Spring Boot 4, Java 23, Spring WebSocket (STOMP), Spring Data JPA
- **Frontend:** React 19, TypeScript, Vite, STOMP.js
- **Database:** PostgreSQL 18
- **LLM:** DeepSeek API (chat completions)

## How the Negotiation Engine Works

Every time the supplier submits an offer, it passes through a **decision tree** — an ordered pipeline of evaluation nodes. Each node either makes a final decision (accept, reject, counter) or passes control to the next node. The first node to reach a verdict ends the evaluation.

### Scoring

At the core is a **utility scoring function**. Each negotiation term (price, delivery time, etc.) has a bot-configured preference with:

- **Ideal value** — the best outcome for the bot
- **Limit value** — the worst the bot will accept (walkaway boundary)
- **Weight** — how important this term is relative to others
- **Strictness** — how reluctant the bot is to move on this term

For each term in an offer, the value is normalized to a 0–1 scale between the limit and ideal, then multiplied by the term's weight. The weighted scores are summed into a single **offer score**.

```
term_score = clamp((value - limit) / (ideal - limit), 0, 1)
offer_score = sum(term_score * weight) for each term
```

### When the Bot Accepts

The bot accepts when the offer score meets or exceeds its **acceptance threshold**. This threshold isn't static — it drops each round by the bot's **concession rate**, making the bot more willing to accept as the negotiation progresses. It never drops below the walkaway threshold.

```
adjusted_threshold = acceptance_threshold - (concession_rate * round)
adjusted_threshold = max(adjusted_threshold, walkaway_threshold)
```

### When the Bot Rejects

Three things can trigger a rejection:

1. **Score below walkaway threshold** — the offer is too far from acceptable. On the very first offer, the bot gives a second chance instead of rejecting outright, and tells the supplier which term was the most problematic.
2. **Max rounds reached** — the bot has a configurable round limit. Once exhausted, it walks away.
3. **No progress** (strategy-dependent) — if the supplier's offer hasn't improved on any term compared to their previous offer, the bot rejects.

### How Counteroffers Are Generated

If the offer isn't accepted or rejected, the **CounterOfferNode** generates a counteroffer. For each term:

1. Start from the bot's previous offer value (or its ideal value if this is the first round)
2. Calculate a **concession** — how much to move toward the supplier's value:
   ```
   flexibility = (1 - weight) * (1 - strictness)
   concession = (supplier_value - bot_previous) * flexibility * concession_rate * round
   new_value = bot_previous + concession
   ```
3. Clamp to the limit value (the bot never crosses its hard boundary)

This means the bot concedes **more on terms it cares less about** (low weight, low strictness) and **holds firm on high-priority terms**. It also concedes more in later rounds, creating natural pressure toward a deal.

### Strategies

Three strategies control which nodes run and in what order:

| Strategy | Pipeline | Behavior |
|---|---|---|
| **BALANCED** | MaxRounds → Scoring → ProgressCheck → CounterOffer | Enforces round limits and checks that the supplier is making progress |
| **CONCEDING** | MaxRounds → Scoring → CounterOffer | Round limit but no progress check — more forgiving, concedes steadily |
| **INFINITE** | ProgressCheck → Scoring → CounterOffer | No round limit, but rejects if the supplier isn't improving |

### LLM Layer

After the engine makes its decision, the result is passed to DeepSeek to generate a natural-language response. The LLM doesn't decide — it communicates. It gets the bot's configured personality, market context, term preferences, and the engine's verdict, then writes a human-like message explaining the accept/reject/counter. The supplier can also chat freely between offers.

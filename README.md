# Beat Yesterday

A Strava analytics dashboard that imports your activities and visualizes performance stats. Full-stack Kotlin/React rewrite of the [Statistics for Strava](https://github.com/robiningelbrecht/statistics-for-strava) PHP app.

## Features

- **Dashboard** — Total activities, distance, elevation, and time at a glance. Monthly and yearly bar charts, sport breakdown pie chart, and recent activity feed.
- **Activities** — Paginated, filterable table of all activities with distance, elevation, time, speed, and heart rate columns.
- **Activity Detail** — Per-activity view with all available metrics (power, cadence, HR, calories, kudos) and a link back to Strava.
- **Gear** — Equipment list with total distance and retired status.
- **Strava Sync** — One-click import of your athlete profile, activities, and gear via the Strava API.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 19, TypeScript 5.7, Vite 6, Tailwind CSS 3.4, Recharts 2.15 |
| Backend | Kotlin 2.1, Spring Boot 3.4, JDK 21, Gradle 8.12 |
| Database | PostgreSQL 16, Flyway migrations |
| Testing | JUnit 5, MockK, H2 in-memory DB |
| Deployment | Google Cloud Run (multi-stage Dockerfile) |

## Prerequisites

- **JDK 21**
- **Node.js 22**
- **PostgreSQL 16** (or use Docker Compose)
- A [Strava API application](https://www.strava.com/settings/api) for OAuth credentials

## Getting Started

### 1. Set environment variables

Copy the example env file and fill in your Strava credentials:

```bash
cp .env.example .env
```

Required variables:

| Variable | Description |
|----------|-------------|
| `STRAVA_CLIENT_ID` | Your Strava API application client ID |
| `STRAVA_CLIENT_SECRET` | Your Strava API application client secret |
| `STRAVA_REFRESH_TOKEN` | A refresh token for your Strava account |

### 2a. Run with Docker Compose (recommended)

```bash
docker-compose up -d
```

This starts PostgreSQL 16 and the app on [http://localhost:8080](http://localhost:8080).

### 2b. Run locally (development)

Start the database:

```bash
docker-compose up -d db
```

In one terminal, start the backend:

```bash
cd backend && ./gradlew bootRun
```

In another terminal, start the frontend dev server (hot reload, proxies `/api` to `:8080`):

```bash
cd frontend && npm install && npm run dev
```

The frontend is available at [http://localhost:5173](http://localhost:5173).

## Commands

```bash
# Backend
cd backend && ./gradlew build          # Compile + package
cd backend && ./gradlew test           # Run tests (H2 in-memory)
cd backend && ./gradlew bootRun        # Start on :8080

# Frontend
cd frontend && npm install             # Install dependencies
cd frontend && npm run dev             # Dev server on :5173
cd frontend && npm run build           # Production build (tsc + Vite)

# Docker
docker-compose up -d                   # PostgreSQL + app
docker build -t beat-yesterday .       # Multi-stage image build
```

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/dashboard` | Aggregated stats, athlete info, recent activities |
| `GET` | `/api/activities?page=&size=&sportType=` | Paginated, filterable activity list |
| `GET` | `/api/activities/{id}` | Single activity detail |
| `GET` | `/api/gear` | All gear with usage distance |
| `POST` | `/api/import` | Trigger Strava import |
| `GET` | `/api/oauth/strava/url` | OAuth authorization URL |
| `GET` | `/api/oauth/strava/callback` | OAuth callback handler |
| `GET` | `/api/oauth/strava/status` | Auth status check |

## Architecture

Hexagonal / Clean Architecture. Dependencies point inward: infrastructure → application → domain.

```
React SPA → REST Controllers → DTOs → Use Cases → Domain Models → Repository Ports → JPA Adapters → PostgreSQL
```

- **Domain** — `Activity`, `Athlete`, `Gear` entities with value objects (`Kilometer`, `Meter`, `KmPerHour`, `Coordinate`). Repository port interfaces.
- **Application** — Use cases that orchestrate import: athlete → activities (paginated) → gear.
- **Infrastructure** — Strava API client with rate limiting and token refresh. JPA persistence with an anti-corruption layer (mappers between JPA entities and domain models).
- **Web** — REST controllers and response DTOs, separate from domain models.

## Deployment

The app is designed for Google Cloud Run using the multi-stage `Dockerfile`:

1. **Stage 1** — Node 22 builds the React frontend
2. **Stage 2** — Gradle + JDK 21 builds the Spring Boot backend
3. **Stage 3** — JRE 21 Alpine runtime serves both frontend and API

The `PORT` environment variable is read automatically from Cloud Run. Strava credentials are set as environment variables on the service. Database connectivity is configured via `DB_*` environment variables.

## License

This project is not currently licensed for redistribution.

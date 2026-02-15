# Beat Yesterday

A Strava analytics dashboard — full-stack rewrite of the Statistics for Strava PHP app. Import activities from Strava and visualize performance with monthly/yearly stats, gear tracking, and sport breakdowns.

## Tech Stack

- **Backend:** Kotlin 2.1.0 / Spring Boot 3.4.2 / JDK 21 / Gradle 8.12
- **Frontend:** React 19 / TypeScript 5.7 / Vite 6 / Tailwind CSS 3.4
- **Database:** PostgreSQL 16 (Flyway migrations)
- **Charts:** Recharts 2.15
- **Testing:** JUnit 5 + MockK (backend), H2 in-memory for test DB

## Architecture

Clean Architecture / Hexagonal pattern:

```
Frontend (React) → REST Controllers → DTOs → Use Cases → Domain Models → Repository Interfaces → JPA Adapters → PostgreSQL
```

### Backend Layers (`backend/src/main/kotlin/com/beatyesterday/`)
- `domain/` — Entities, value objects (`Kilometer`, `Meter`, `KmPerHour`, `Coordinate`), repository interfaces
- `application/import/` — Use cases: `RunImportUseCase` orchestrates athlete → activities → gear import
- `infrastructure/persistence/` — JPA entities, mappers, Spring Data repositories
- `infrastructure/strava/` — Strava API client, OAuth service (token refresh, rate limiting)
- `infrastructure/config/` — Security config, CORS config
- `web/controller/` — REST endpoints
- `web/dto/` — API response objects

### Frontend Structure (`frontend/src/`)
- `pages/` — Dashboard, Activities, ActivityDetail, Gear, Settings
- `components/` — layout (Sidebar), dashboard widgets, common (LoadingSpinner, StatCard)
- `api/client.ts` — Fetch API abstraction
- `types/index.ts` — TypeScript interfaces matching backend DTOs

## Local Development

```bash
# 1. Start PostgreSQL
docker-compose up -d

# 2. Start backend (port 8080)
cd backend && ./gradlew bootRun

# 3. Start frontend (port 5173, proxies /api → localhost:8080)
cd frontend && npm install && npm run dev
```

## Environment Variables

| Variable | Default | Required |
|----------|---------|----------|
| `DB_HOST` | localhost | no |
| `DB_PORT` | 5432 | no |
| `DB_NAME` | beatyesterday | no |
| `DB_USERNAME` | beatyesterday | no |
| `DB_PASSWORD` | beatyesterday | no |
| `STRAVA_CLIENT_ID` | — | **yes** |
| `STRAVA_CLIENT_SECRET` | — | **yes** |
| `STRAVA_REFRESH_TOKEN` | — | **yes** |

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/dashboard` | Dashboard stats, athlete, recent activities |
| GET | `/api/activities` | Paginated activities (filterable by sport type) |
| GET | `/api/activities/{id}` | Single activity detail |
| GET | `/api/gear` | All gear with distance |
| POST | `/api/import` | Trigger Strava import pipeline |
| GET | `/api/oauth/strava/url` | Strava OAuth authorization URL |
| GET | `/api/oauth/strava/callback` | OAuth callback |
| GET | `/api/oauth/strava/status` | Auth status check |

## Database

Schema managed by Flyway: `backend/src/main/resources/db/migration/`

Tables: `athlete`, `activity` (indexed on start_date_time, sport_type, gear_id), `gear`

## Docker Production Build

```bash
docker build -t beat-yesterday .
docker run -p 8080:8080 -e STRAVA_CLIENT_ID=... -e STRAVA_CLIENT_SECRET=... -e STRAVA_REFRESH_TOKEN=... beat-yesterday
```

Multi-stage Dockerfile: Node 22 (frontend build) → Gradle 8.12/JDK 21 (backend build) → JRE 21 Alpine (runtime). Frontend served from Spring Boot `/static/`.

## Conventions

- Domain entities use value objects (`Kilometer`, `Meter`, etc.) — not raw primitives
- Import pipeline is idempotent (activities checked by ID before saving)
- Strava API rate limiting: 60-second backoff on 429 responses
- CORS allowed for `http://localhost:5173` on all `/api/**` endpoints
- Custom Tailwind colors: `strava` (#FC4C02), `strava-dark` (#E34402)

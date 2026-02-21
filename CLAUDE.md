# Beat Yesterday

Strava analytics dashboard — import activities from Strava and visualize performance stats. Full-stack Kotlin/React rewrite of the Statistics for Strava PHP app.

## Tech Stack

- **Backend:** Kotlin 2.1 / Spring Boot 3.4 / JDK 21 / Gradle 8.12
- **Frontend:** React 19 / TypeScript 5.7 / Vite 6 / Tailwind CSS 3.4 / Recharts 2.15
- **Database:** PostgreSQL 16 (Flyway migrations)
- **Testing:** JUnit 5 + MockK, H2 in-memory DB
- **Deployment:** Google Cloud Run (container image from multi-stage Dockerfile)

## Commands

```bash
# Backend
cd backend && ./gradlew bootRun        # Start on :8080
cd backend && ./gradlew build           # Compile + package
cd backend && ./gradlew test            # Run tests (H2 in-memory)

# Frontend
cd frontend && npm install              # Install deps
cd frontend && npm run dev              # Start on :5173 (proxies /api → :8080)
cd frontend && npm run build            # tsc + vite production build

# Full stack (local)
docker-compose up -d                    # PostgreSQL + app (builds from Dockerfile)
docker build -t beat-yesterday .        # Multi-stage: Node 22 → Gradle/JDK 21 → JRE 21 Alpine

# Deploy to Google Cloud Run
gcloud run deploy beat-yesterday --source . --region <REGION> --allow-unauthenticated
```

## Deployment (Google Cloud Run)

The app runs on **Google Cloud Run** using the multi-stage `Dockerfile` (frontend build → backend build → JRE runtime).

- **Port:** Cloud Run sets `PORT` env var automatically; `application.yml` reads it via `${PORT:8080}`
- **Strava secrets:** Set as environment variables (or Secret Manager references) on the Cloud Run service
- **Database:** Connect to Cloud SQL for PostgreSQL via the Cloud Run "Cloud SQL connections" integration
  - Set `DB_HOST` to the Cloud SQL Unix socket path: `/cloudsql/PROJECT_ID:REGION:INSTANCE_NAME`
  - Or use private IP with Cloud SQL Auth Proxy
- **No extra config files needed** — Cloud Run deploys directly from the Dockerfile

## Architecture

Hexagonal / Clean Architecture. Dependencies point inward: infrastructure → application → domain.

```
React SPA → REST Controllers → DTOs → Use Cases → Domain Models → Repository Ports → JPA Adapters → PostgreSQL
```

### Backend (`backend/src/main/kotlin/com/beatyesterday/`)

| Layer | Path | Key files |
|-------|------|-----------|
| Domain | `domain/` | `Activity`, `Athlete`, `Gear` entities; value objects (`Kilometer`, `Meter`, `KmPerHour`, `Coordinate`); repository port interfaces |
| Application | `application/import/` | `RunImportUseCase` orchestrates: athlete → activities (paginated) → gear |
| Infrastructure | `infrastructure/strava/` | `StravaApiClient` (REST + rate limiting), `StravaOAuthService` (token refresh + 6h cache) |
| Infrastructure | `infrastructure/persistence/` | JPA entities, mappers (anti-corruption layer), Spring Data repos |
| Infrastructure | `infrastructure/config/` | `SecurityConfig` (permits all — MVP), `WebConfig` (CORS for localhost:5173) |
| Web | `web/controller/` | REST endpoints: Dashboard, Activities, Gear, Import, OAuth |
| Web | `web/dto/` | Response DTOs (separate from domain models) |

### Frontend (`frontend/src/`)

| Path | Purpose |
|------|---------|
| `pages/` | Dashboard, Activities, ActivityDetail, Gear, Settings |
| `components/` | Sidebar, MonthlyChart, SportBreakdown, StatCard, LoadingSpinner |
| `api/client.ts` | Generic fetch wrapper using Vite proxy |
| `types/index.ts` | TypeScript interfaces — must stay in sync with backend DTOs |

## API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/dashboard` | Aggregated stats, athlete info, recent activities |
| GET | `/api/activities?page=&size=&sportType=` | Paginated, filterable activity list |
| GET | `/api/activities/{id}` | Single activity detail |
| GET | `/api/gear` | All gear with usage distance |
| POST | `/api/import` | Trigger Strava import (synchronous) |
| GET | `/api/oauth/strava/url` | OAuth authorization URL |
| GET | `/api/oauth/strava/callback` | OAuth callback handler |
| GET | `/api/oauth/strava/status` | Auth status check |

## Environment Variables

| Variable | Default | Required |
|----------|---------|----------|
| `STRAVA_CLIENT_ID` | — | **yes** |
| `STRAVA_CLIENT_SECRET` | — | **yes** |
| `STRAVA_REFRESH_TOKEN` | — | **yes** |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | localhost / 5432 / beatyesterday / beatyesterday / beatyesterday | no |
| `PORT` | 8080 | no (Cloud Run sets this automatically) |

## Strava API Quirks

These are important when touching import or domain code:

- **Speeds** come as m/s — convert to km/h via `* 3.6`
- **Distances** come as meters — convert to km via `/ 1000`
- **sport_type** (not `type`) is the correct field — more granular (e.g., `TrailRun` vs `Run`)
- **Polyline** is nested: `activity.map.summaryPolyline`, not top-level
- **No "list gear" endpoint** — gear IDs are extracted by scanning imported activities
- **Rate limiting:** 429 → wait 60s and retry; 404 → return empty (gear may be deleted)
- **Pagination:** 200 activities/page, loop until page returns < 200

## Conventions

- Domain entities use value objects (`Kilometer`, `Meter`, etc.) — never raw primitives
- IDs use string prefixes: `"activity-{stravaId}"`, `"gear-{stravaId}"`
- Import is idempotent — activities checked by ID before insert
- JPA entities are separate from domain models (mapper functions convert between them)
- Custom Tailwind colors: `strava` (#FC4C02), `strava-dark` (#E34402)
- DB schema: Flyway in `backend/src/main/resources/db/migration/` (single migration: `V1__initial_schema.sql`)

## Known MVP Limitations

- **Single-tenant** — one athlete, no auth. SecurityConfig permits all requests.
- **Synchronous import** — POST `/api/import` blocks until complete
- **In-memory token cache** — StravaOAuthService; needs Redis/DB for multi-instance
- **Dashboard aggregation in Kotlin** — loads up to 10K activities into memory instead of SQL aggregation
- **Single Cloud Run instance** — no Redis/shared cache for token storage across instances

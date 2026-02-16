# Beat Yesterday

Strava analytics dashboard — import activities from Strava and visualize performance stats. Full-stack Kotlin/React rewrite of the Statistics for Strava PHP app.

## Getting Started (Local Development)

### Prerequisites

- **JDK 21** (use [SDKMAN](https://sdkman.io/) or [Adoptium](https://adoptium.net/))
- **Docker & Docker Compose** (for PostgreSQL)
- **Node.js 22+** (for frontend development)
- **Strava API credentials** (from [strava.com/settings/api](https://www.strava.com/settings/api))

### Quick Start

1. **Clone and set up environment variables:**
   ```bash
   cp .env.example .env
   # Edit .env with your actual Strava credentials
   ```

2. **Run verification script:**
   ```bash
   ./scripts/verify-setup.sh
   ```
   This checks JDK version, Docker status, PostgreSQL, port availability, and environment variables.

3. **Start the application:**
   ```bash
   # Option 1: Manual startup
   docker-compose up -d           # PostgreSQL
   cd backend && ./gradlew bootRun # Backend on :8080
   cd frontend && npm install && npm run dev # Frontend on :5173

   # Option 2: Use the all-in-one script (future enhancement)
   # ./scripts/start-dev.sh
   ```

4. **Access the application:**
   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080/api
   - Health check: http://localhost:8080/actuator/health

### Troubleshooting

If you encounter issues, see [docs/TROUBLESHOOTING.md](docs/TROUBLESHOOTING.md) for common problems and solutions:
- Port 8080 already in use
- Database connection refused
- JDK version mismatch
- Missing Strava credentials
- Flyway migration failures

## Tech Stack

- **Backend:** Kotlin 2.1 / Spring Boot 3.4 / JDK 21 / Gradle 8.12
- **Frontend:** React 19 / TypeScript 5.7 / Vite 6 / Tailwind CSS 3.4 / Recharts 2.15
- **Database:** PostgreSQL 16 (Flyway migrations)
- **Testing:** JUnit 5 + MockK, H2 in-memory DB (no tests written yet)

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

# Full stack
docker-compose up -d                    # PostgreSQL
docker build -t beat-yesterday .        # Multi-stage: Node 22 → Gradle/JDK 21 → JRE 21 Alpine
```

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

## Deployment

Beat Yesterday can be deployed to multiple cloud platforms. Choose based on your experience level and requirements:

### Cloud Platform Options

| Platform | Difficulty | Setup Time | Best For | Guide |
|----------|-----------|-----------|----------|-------|
| **Railway** | Easy | 10-15 min | New developers | [docs/DEPLOY_RAILWAY.md](docs/DEPLOY_RAILWAY.md) |
| **Render** | Easy | 15 min | Infrastructure-as-code | [docs/DEPLOY_RENDER.md](docs/DEPLOY_RENDER.md) |
| **Google Cloud Run** | Medium | 30 min | GCP users, scalability | [docs/DEPLOY_CLOUD_RUN.md](docs/DEPLOY_CLOUD_RUN.md) |

### Cloud Deployment Considerations

- **Health Checks:** All platforms use `/actuator/health` endpoint (Spring Boot Actuator)
- **Database:** Managed PostgreSQL included with Railway/Render; Cloud SQL for GCP
- **Token Caching:** Production deployments use database-backed cache for multi-instance support
- **Secrets Management:** Use platform-specific secret storage (Railway env vars, GCP Secret Manager, etc.)
- **Automatic SSL:** All platforms provide free HTTPS certificates
- **Auto-deploy:** Git push triggers automatic deployment on all platforms

### Environment Variables for Cloud

Required for all platforms:
```
STRAVA_CLIENT_ID=your_client_id
STRAVA_CLIENT_SECRET=your_client_secret
STRAVA_REFRESH_TOKEN=your_refresh_token
```

Database configuration (auto-provided by most platforms):
- **Railway/Render:** Use `DATABASE_URL` (automatically set)
- **Cloud Run:** Use `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` with Cloud SQL Proxy

### Cost Estimates (Monthly)

- **Railway:** $5 credit free tier, ~$3-8 typical usage
- **Render:** Free tier (750 hours/month), then $7-14 for web + database
- **Google Cloud Run:** ~$10-15 (Cloud Run + Cloud SQL)

## Known MVP Limitations

- **Single-tenant** — one athlete, no auth. SecurityConfig permits all requests.
- **Synchronous import** — POST `/api/import` blocks until complete
- **Dashboard aggregation in Kotlin** — loads up to 10K activities into memory instead of SQL aggregation
- **No tests yet** — test infrastructure is wired but no test files exist

### Production Enhancements (Implemented)

- ✅ **Distributed token cache** — Database-backed cache for multi-instance deployments (migration V2)
- ✅ **Health check endpoints** — `/actuator/health` for cloud platform monitoring
- ✅ **Cloud platform support** — `DATABASE_URL` compatibility for Railway/Render/Heroku

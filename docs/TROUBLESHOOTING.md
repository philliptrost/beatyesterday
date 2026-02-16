# Troubleshooting Guide

Common issues and solutions for Beat Yesterday development and deployment.

## Quick Diagnostics

Before diving into specific errors, run the setup verification script:

```bash
./scripts/verify-setup.sh
```

This will check JDK version, Docker status, PostgreSQL availability, port conflicts, and environment variables.

---

## Local Development Issues

### Port 8080 Already in Use

**Symptoms:**
```
***************************
APPLICATION FAILED TO START
***************************

Description:

Web server failed to start. Port 8080 was already in use.
```

**Cause:** Another application is using port 8080.

**Solutions:**

1. **Find the process using port 8080:**
   ```bash
   # macOS/Linux
   lsof -i :8080

   # Linux (alternative)
   netstat -tuln | grep 8080

   # Windows
   netstat -ano | findstr :8080
   ```

2. **Kill the process:**
   ```bash
   # macOS/Linux
   kill -9 <PID>

   # Windows
   taskkill /PID <PID> /F
   ```

3. **Or change the backend port:**
   ```bash
   # Set environment variable
   export PORT=8081

   # Run backend
   cd backend && ./gradlew bootRun
   ```

   **Important:** If you change the backend port, update the frontend proxy configuration:
   ```typescript
   // frontend/vite.config.ts
   server: {
     port: 5173,
     proxy: {
       '/api': 'http://localhost:8081'  // Update this
     }
   }
   ```

---

### Database Connection Refused

**Symptoms:**
```
org.postgresql.util.PSQLException: Connection to localhost:5432 refused
```
or
```
HikariPool-1 - Exception during pool initialization.
org.postgresql.util.PSQLException: The connection attempt failed.
```

**Cause:** PostgreSQL is not running.

**Solutions:**

1. **Start PostgreSQL with Docker Compose:**
   ```bash
   docker-compose up -d
   ```

2. **Verify PostgreSQL is running:**
   ```bash
   docker ps | grep postgres
   ```

   You should see output like:
   ```
   CONTAINER ID   IMAGE                PORTS
   abc123def456   postgres:16-alpine   0.0.0.0:5432->5432/tcp
   ```

3. **Check PostgreSQL logs if it fails to start:**
   ```bash
   docker logs <container_id>
   ```

4. **If port 5432 is in use:**
   ```bash
   # Find what's using port 5432
   lsof -i :5432

   # Option 1: Stop the conflicting service
   # Option 2: Change PostgreSQL port in docker-compose.yml
   ports:
     - "5433:5432"  # Map to 5433 instead

   # Then update environment variables
   export DB_PORT=5433
   ```

---

### JDK Version Mismatch

**Symptoms:**
```
error: invalid target release: 21
```
or
```
Execution failed for task ':compileKotlin'.
> 'compileJava' task (current target is 17) and 'compileKotlin' task (current target is 21)
```

**Cause:** Incorrect JDK version. This project requires JDK 21.

**Solutions:**

1. **Check your Java version:**
   ```bash
   java -version
   ```

2. **Install JDK 21 using SDKMAN (recommended):**
   ```bash
   # Install SDKMAN
   curl -s "https://get.sdkman.io" | bash
   source "$HOME/.sdkman/bin/sdkman-init.sh"

   # Install JDK 21
   sdk install java 21.0.1-tem

   # Set as default
   sdk default java 21.0.1-tem
   ```

3. **Or download from Adoptium:**
   - Visit: https://adoptium.net/
   - Download Eclipse Temurin 21 (LTS)
   - Set `JAVA_HOME` environment variable

4. **Verify installation:**
   ```bash
   java -version  # Should show version 21.x.x
   ```

---

### Missing Strava Credentials

**Symptoms:**
```
Application starts but /api/oauth/strava/status returns:
{
  "authenticated": false
}
```
or
```
Error refreshing Strava token: 400 Bad Request
```

**Cause:** Missing or invalid Strava API credentials.

**Solutions:**

1. **Get Strava API credentials:**
   - Go to https://www.strava.com/settings/api
   - Create a new application
   - Note your **Client ID** and **Client Secret**

2. **Obtain a refresh token:**

   **Option 1: Use the OAuth callback endpoint**
   ```bash
   # Start the backend
   cd backend && ./gradlew bootRun

   # In another terminal, get the authorization URL
   curl http://localhost:8080/api/oauth/strava/url

   # Open the URL in your browser, authorize
   # You'll be redirected to /api/oauth/strava/callback
   # The response will include your refresh token
   ```

   **Option 2: Manual OAuth flow**
   ```bash
   # Build the authorization URL
   https://www.strava.com/oauth/authorize?client_id=YOUR_CLIENT_ID&response_type=code&redirect_uri=http://localhost:8080/api/oauth/strava/callback&scope=activity:read_all

   # Open in browser, authorize, copy the 'code' from the redirect URL
   # Exchange the code for tokens
   curl -X POST https://www.strava.com/oauth/token \
     -d client_id=YOUR_CLIENT_ID \
     -d client_secret=YOUR_CLIENT_SECRET \
     -d code=YOUR_CODE \
     -d grant_type=authorization_code

   # Response includes "refresh_token" - save this!
   ```

3. **Set environment variables:**
   ```bash
   export STRAVA_CLIENT_ID=your_client_id
   export STRAVA_CLIENT_SECRET=your_client_secret
   export STRAVA_REFRESH_TOKEN=your_refresh_token
   ```

   Or create a `.env` file (recommended):
   ```bash
   cp .env.example .env
   # Edit .env with your actual credentials
   ```

4. **Restart the backend:**
   ```bash
   cd backend && ./gradlew bootRun
   ```

---

### Flyway Migration Failures

**Symptoms:**
```
FlywayException: Validate failed: Migration checksum mismatch
```
or
```
Migration V1__initial_schema.sql failed
```

**Causes and Solutions:**

**Checksum Mismatch:**
```bash
# The database schema is out of sync with migration files
# Solution: Clean the database and re-migrate

docker-compose down -v  # Remove volumes (deletes all data!)
docker-compose up -d    # Recreate database
cd backend && ./gradlew bootRun  # Re-run migrations
```

**Permission Denied:**
```bash
# The database user lacks CREATE TABLE permissions
# Solution: Check docker-compose.yml credentials match application.yml

# docker-compose.yml should have:
POSTGRES_USER: beatyesterday
POSTGRES_PASSWORD: beatyesterday

# Application.yml should have:
DB_USERNAME=beatyesterday
DB_PASSWORD=beatyesterday
```

**Migration Failed Mid-Execution:**
```bash
# Flyway thinks migration ran, but it didn't complete
# Solution: Manually fix the schema or repair Flyway

# Option 1: Reset database
docker-compose down -v && docker-compose up -d

# Option 2: Manually repair Flyway
# Connect to database
docker exec -it <postgres_container> psql -U beatyesterday -d beatyesterday

# Check flyway_schema_history table
SELECT * FROM flyway_schema_history;

# If V1 shows as failed, delete the row
DELETE FROM flyway_schema_history WHERE version = '1';

# Exit and restart backend
\q
cd backend && ./gradlew bootRun
```

---

### Gradle Build Failures

**Symptoms:**
```
Could not resolve all dependencies
```
or
```
Execution failed for task ':bootJar'
```

**Solutions:**

1. **Clean and rebuild:**
   ```bash
   cd backend
   ./gradlew clean build --refresh-dependencies
   ```

2. **Check internet connection:**
   - Gradle needs to download dependencies from Maven Central

3. **Clear Gradle cache:**
   ```bash
   rm -rf ~/.gradle/caches
   cd backend && ./gradlew build
   ```

4. **Verify Gradle version:**
   ```bash
   cd backend && ./gradlew --version
   # Should be Gradle 8.12
   ```

---

### Frontend Build Errors

**Symptoms:**
```
Cannot find module 'react'
```
or
```
Module not found: Error: Can't resolve './api/client'
```

**Solutions:**

1. **Install dependencies:**
   ```bash
   cd frontend
   rm -rf node_modules package-lock.json
   npm install
   ```

2. **Check Node.js version:**
   ```bash
   node -v  # Should be v18 or higher (v22 recommended)
   ```

3. **Clear npm cache:**
   ```bash
   npm cache clean --force
   npm install
   ```

---

## Cloud Deployment Issues

### Docker Build Failures

**Symptoms:**
```
ERROR [builder 5/5] RUN ./gradlew bootJar --no-daemon
```

**Cause:** Multi-stage Dockerfile requires specific context.

**Solution:**
```bash
# Build from repository root, not from backend/
cd /home/user/beatyesterday
docker build -t beat-yesterday .

# Don't use:
# cd backend && docker build ...
```

---

### Cloud Run: Cold Start Timeout

**Symptoms:**
- First request after inactivity takes >10 seconds
- Cloud Run logs show "Container startup timeout"

**Solutions:**

1. **Increase timeout in Cloud Run:**
   ```bash
   gcloud run deploy beat-yesterday \
     --timeout=60 \
     --min-instances=1  # Keep 1 instance warm
   ```

2. **Optimize JVM startup (already done in Dockerfile):**
   - Using JRE 21 Alpine (minimal footprint)
   - No JVM tuning needed for this size

3. **Set minimum instances (costs more but eliminates cold starts):**
   ```bash
   gcloud run deploy beat-yesterday --min-instances=1
   ```

---

### Railway: DATABASE_URL Not Recognized

**Symptoms:**
- Railway provides `DATABASE_URL`
- Application fails to connect to database

**Cause:** The application expects component-based env vars by default.

**Solution:**
Ensure `application.yml` has been updated to support `DATABASE_URL`:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:beatyesterday}}
```

This change is included in Phase 2 of the implementation plan.

---

### Strava Token Refresh Fails on Multiple Instances

**Symptoms:**
- Cloud Run with 2+ instances
- Intermittent 401 Unauthorized errors from Strava API
- Logs show "Refreshing Strava access token..." repeatedly

**Cause:** In-memory token cache doesn't work across multiple instances.

**Solution:**
Implement database-backed token cache (covered in Phase 4 of the implementation plan):
- Create `V2__strava_token_cache.sql` migration
- Implement `StravaTokenCache` interface
- Update `StravaOAuthService` to use cache

---

## Getting Help

If you're still stuck after trying these solutions:

1. **Check logs for detailed error messages:**
   ```bash
   # Backend logs
   cd backend && ./gradlew bootRun

   # Docker logs
   docker logs <container_id>

   # Cloud Run logs
   gcloud run logs read beat-yesterday --limit=50
   ```

2. **Run the verification script:**
   ```bash
   ./scripts/verify-setup.sh
   ```

3. **Review the CLAUDE.md file:**
   - Architecture overview
   - API endpoints
   - Known limitations

4. **Check deployment guides:**
   - `docs/DEPLOY_CLOUD_RUN.md` for GCP Cloud Run
   - `docs/DEPLOY_RAILWAY.md` for Railway
   - `docs/DEPLOY_RENDER.md` for Render

5. **Report issues:**
   - GitHub Issues: https://github.com/your-username/beatyesterday/issues
   - Include: OS, JDK version, error messages, steps to reproduce

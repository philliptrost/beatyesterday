# Deploy to Render

Render is a modern cloud platform that combines simplicity with infrastructure-as-code. This guide shows you how to deploy Beat Yesterday using Render's Blueprint (YAML configuration).

## Why Render?

- **Infrastructure-as-code** - Define services in `render.yaml`
- **Free PostgreSQL tier** - 90-day free trial, then $7/month
- **Automatic SSL** - Free certificates for custom domains
- **GitHub integration** - Auto-deploy on push
- **Simple pricing** - Predictable costs, no surprises

## Prerequisites

- GitHub account
- Strava API credentials (Client ID, Client Secret, Refresh Token)
- Your Beat Yesterday repository pushed to GitHub

## Cost Estimate

| Service | Free Tier | Paid |
|---------|-----------|------|
| Web Service | 750 hours/month | $7/month (Starter) |
| PostgreSQL | 90 days free | $7/month |
| **Total** | **Free for 90 days** | **$14/month** |

## Deployment Options

### Option A: Deploy with Blueprint (Recommended)

The repository includes `render.yaml` for one-click deployment.

### Option B: Manual Setup via Dashboard

Step-by-step setup through Render's web interface.

---

## Option A: Deploy with Blueprint

### Step 1: Connect GitHub Repository

1. Sign up at [render.com](https://render.com)
2. Click "**New +**" → "**Blueprint**"
3. Connect your GitHub account
4. Authorize Render to access repositories
5. Select **beatyesterday** repository

### Step 2: Configure Blueprint

Render automatically detects `render.yaml` and shows:

- **Web Service:** beat-yesterday (Docker)
- **Database:** beatyesterday-db (PostgreSQL 16)

Click "**Apply**" to create both services.

### Step 3: Add Strava Credentials

The Blueprint creates environment variable placeholders. You need to fill them in:

1. Go to **Dashboard** → **beat-yesterday** service
2. Click "**Environment**" in the left sidebar
3. Add the following **secret** variables:
   ```
   STRAVA_CLIENT_ID=your_actual_client_id
   STRAVA_CLIENT_SECRET=your_actual_client_secret
   STRAVA_REFRESH_TOKEN=your_actual_refresh_token
   ```

4. Click "**Save Changes**"
5. Render auto-deploys with the new variables

### Step 4: Wait for Deployment

- **First deploy:** ~5-10 minutes (builds Docker image)
- **Subsequent deploys:** ~3-5 minutes (uses cached layers)

Watch progress in "**Events**" tab.

### Step 5: Access Your Application

1. Go to **Dashboard** → **beat-yesterday**
2. Copy the service URL: `https://beat-yesterday.onrender.com`
3. Test the endpoints:
   ```bash
   curl https://beat-yesterday.onrender.com/actuator/health
   # Expected: {"status":"UP"}
   ```

---

## Option B: Manual Dashboard Setup

### Step 1: Create PostgreSQL Database

1. Click "**New +**" → "**PostgreSQL**"
2. Configure:
   - **Name:** beatyesterday-db
   - **Database:** beatyesterday
   - **User:** beatyesterday
   - **Region:** Choose closest to you
   - **Plan:** Free (90 days) or Starter ($7/month)
3. Click "**Create Database**"

### Step 2: Create Web Service

1. Click "**New +**" → "**Web Service**"
2. Connect your GitHub repository
3. Configure:
   - **Name:** beat-yesterday
   - **Environment:** Docker
   - **Region:** Same as database
   - **Branch:** main
   - **Dockerfile Path:** `./Dockerfile`
   - **Plan:** Free or Starter ($7/month)

### Step 3: Add Environment Variables

In the "**Environment**" section before deploying:

| Key | Value | Type |
|-----|-------|------|
| `DATABASE_URL` | (Select **beatyesterday-db** from dropdown) | Link |
| `STRAVA_CLIENT_ID` | your_client_id | Secret |
| `STRAVA_CLIENT_SECRET` | your_client_secret | Secret |
| `STRAVA_REFRESH_TOKEN` | your_refresh_token | Secret |
| `PORT` | 8080 | Normal |

### Step 4: Configure Health Check

- **Health Check Path:** `/actuator/health`
- **Timeout:** 30 seconds

### Step 5: Deploy

Click "**Create Web Service**" and wait for deployment.

---

## Configuration Details

### Environment Variables

Render automatically provides `DATABASE_URL` in the format:
```
postgres://user:password@host:5432/database
```

Spring Boot expects `jdbc:postgresql://...`, but the updated `application.yml` handles this:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://...}
```

### Health Checks

Render uses `/actuator/health` to determine service health:
- **Healthy:** Service receives traffic
- **Unhealthy:** Automatic restart after 3 failed checks

### Automatic Deployments

Render auto-deploys when you push to the connected branch:

```bash
git add .
git commit -m "Update feature"
git push origin main
# Render detects push and auto-deploys
```

#### Disable Auto-Deploy

1. Go to **Settings** → **Build & Deploy**
2. Toggle "**Auto-Deploy**" off
3. Manually deploy via "**Manual Deploy**" button

---

## Custom Domain

### Add Your Domain

1. Go to **Settings** → **Custom Domain**
2. Click "**+ Add Custom Domain**"
3. Enter your domain: `beatyesterday.com`
4. Render provides DNS records:
   ```
   Type: CNAME
   Name: @  (or subdomain)
   Value: beat-yesterday.onrender.com
   ```
5. Add records at your DNS provider
6. SSL certificate auto-provisions (Let's Encrypt)

### Subdomain

For `app.beatyesterday.com`:
```
Type: CNAME
Name: app
Value: beat-yesterday.onrender.com
```

---

## Database Management

### Connect to PostgreSQL

**Option 1: Render Shell**
```bash
# From dashboard, click Database → "Shell" tab
psql $DATABASE_URL
```

**Option 2: External Tool (TablePlus, pgAdmin)**
```
Host: (from "Connections" tab)
Port: 5432
Database: beatyesterday
User: beatyesterday
Password: (from "Connections" tab)
```

**Option 3: psql locally**
```bash
# Get connection string from Render dashboard
export DATABASE_URL="postgres://user:pass@host/db"
psql $DATABASE_URL
```

### Backup Database

Render automatically backs up paid PostgreSQL databases daily.

**Manual backup:**
```bash
# From dashboard → Database → "Backup" tab
# Or via CLI:
pg_dump $DATABASE_URL > backup.sql
```

**Restore:**
```bash
psql $DATABASE_URL < backup.sql
```

---

## Monitoring and Logs

### View Logs

**Real-time logs:**
1. Dashboard → beat-yesterday service
2. Click "**Logs**" tab
3. Tail logs in real-time

**Search logs:**
- Use the search box to filter by keyword
- Filter by severity: Info, Warning, Error

### Metrics

Render provides:
- **CPU usage**
- **Memory usage**
- **Request count**
- **Response times**

Access via "**Metrics**" tab.

### Alerts

Set up email/Slack alerts:
1. Settings → **Notifications**
2. Add email or Slack webhook
3. Configure alert thresholds:
   - Deploy failed
   - Service crashed
   - High error rate

---

## Scaling

### Free Tier Limits

- **750 hours/month** (enough for 24/7 uptime)
- **512MB RAM**
- **0.1 CPU**
- **Cold starts** after 15min inactivity (~30 seconds)

### Upgrade to Starter

For better performance:
```
Plan: Starter ($7/month)
- No cold starts
- 512MB RAM
- 0.5 CPU
- Priority support
```

### Horizontal Scaling

Not available on Free/Starter. Upgrade to Standard for:
- Multiple instances
- Auto-scaling
- Load balancing

---

## Troubleshooting

### Build Failed

**Check build logs:**
1. Dashboard → beat-yesterday
2. "**Events**" tab → Click failed deploy
3. Review error messages

Common issues:
- **Docker timeout:** Increase timeout in Settings
- **Out of memory:** Upgrade to Starter plan
- **Missing Dockerfile:** Ensure `Dockerfile` is in repository root

### Service Unhealthy

**Verify health endpoint:**
```bash
curl https://beat-yesterday.onrender.com/actuator/health
```

**Check application logs:**
- Look for startup errors
- Verify DATABASE_URL is set
- Confirm Strava credentials are correct

**Common causes:**
- Database not accessible → Check DATABASE_URL connection
- Port mismatch → Must be 8080
- Missing env vars → Verify all Strava credentials

### Database Connection Failed

**Verify connection:**
```bash
# From Render shell
psql $DATABASE_URL -c '\l'
```

**Check network:**
- Database and web service must be in same region
- IP allowlist should be empty (allow all) or include Render IPs

### Cold Starts (Free Tier)

Free tier services sleep after 15 minutes of inactivity:
- **First request:** ~30 seconds (cold start)
- **Subsequent requests:** Instant

**Solutions:**
1. **Upgrade to Starter** ($7/month) - eliminates cold starts
2. **Keep-alive ping** - External service pings every 10 minutes (not recommended)
3. **Accept cold starts** - Fine for hobby projects

---

## render.yaml Reference

The Blueprint file structure:

```yaml
databases:
  - name: beatyesterday-db
    databaseName: beatyesterday
    user: beatyesterday
    plan: free  # or starter
    region: oregon

services:
  - type: web
    name: beat-yesterday
    env: docker
    region: oregon
    plan: free  # or starter
    branch: main
    dockerfilePath: ./Dockerfile
    healthCheckPath: /actuator/health
    envVars:
      - key: DATABASE_URL
        fromDatabase:
          name: beatyesterday-db
          property: connectionString
      - key: STRAVA_CLIENT_ID
        sync: false  # Set in dashboard
```

### Update Blueprint

1. Edit `render.yaml` in repository
2. Commit and push changes
3. Render detects changes and prompts to apply
4. Review changes and click "**Apply**"

---

## Cost Management

### Monitor Usage

1. Dashboard → "**Billing**"
2. View current month spending
3. Set spending limits

### Free Tier Checklist

- Web Service: 750 hours/month (✓ 24/7 uptime)
- PostgreSQL: 90 days free, then $7/month
- Bandwidth: 100GB/month free
- Build minutes: Unlimited

### Optimize Costs

1. **Use free tier** for hobby projects
2. **Pause services** when not in use (Settings → Suspend)
3. **Optimize Docker image** (already done - Alpine Linux)
4. **Monitor database size** (free tier = 1GB storage)

---

## Migration to/from Render

### Export Database

```bash
# Get connection string from dashboard
pg_dump $DATABASE_URL > beatyesterday_backup.sql
```

### Import to Render

```bash
# Create new database on Render
# Get new DATABASE_URL
psql $NEW_DATABASE_URL < beatyesterday_backup.sql
```

---

## Comparison: Render vs Others

| Feature | Render | Railway | Cloud Run |
|---------|--------|---------|-----------|
| Setup Time | 15 min | 10 min | 30 min |
| Free Tier | 750 hrs/mo | $5 credit/mo | 2M req/mo |
| PostgreSQL | $7/mo (after 90 days) | Included | Cloud SQL $10/mo |
| IaC Support | render.yaml | ❌ | terraform |
| Cold Starts | 30 sec (free) | 2-3 sec | 2-3 sec |
| Best For | IaC, simplicity | New devs | GCP ecosystem |

---

## Next Steps

- Set up custom domain
- Configure email/Slack alerts
- Enable database backups (paid plans)
- Implement database-backed token cache for multi-instance

---

**Congratulations!** Your Beat Yesterday app is deployed on Render.

For other deployment options, see:
- [Railway Deployment](DEPLOY_RAILWAY.md) - Easiest for new developers
- [Google Cloud Run Deployment](DEPLOY_CLOUD_RUN.md) - For GCP users

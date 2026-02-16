# Deploy to Railway

Railway is the **easiest way to deploy Beat Yesterday**. Perfect for new developers who want a fully managed platform without the complexity of GCP or AWS.

## Why Railway?

- **Zero configuration** - Railway auto-detects your Dockerfile
- **Automatic PostgreSQL** - Provisions database with one click
- **GitHub integration** - Auto-deploy on every push
- **Free tier** - $5/month credit (enough for small projects)
- **Setup time** - 10-15 minutes from start to deployed app

## Prerequisites

- GitHub account
- Strava API credentials (Client ID, Client Secret, Refresh Token)
- Your Beat Yesterday repository pushed to GitHub

## Cost Estimate

- **Free tier:** $5/month credit
- **Typical usage:** $3-8/month for low traffic
- **What's included:** Web service + PostgreSQL + 512MB RAM + automatic SSL

## Step-by-Step Deployment

### Step 1: Create Railway Account

1. Visit [railway.app](https://railway.app)
2. Click "Start a New Project"
3. Sign in with GitHub
4. Authorize Railway to access your repositories

### Step 2: Create New Project from GitHub

1. Click "New Project"
2. Select "Deploy from GitHub repo"
3. Choose your **beatyesterday** repository
4. Railway automatically detects the Dockerfile
5. Click "Deploy Now"

**Note:** The first deployment will fail because PostgreSQL isn't set up yet. That's expected!

### Step 3: Add PostgreSQL Database

1. In your Railway project dashboard, click "**+ New**"
2. Select "**Database**" → "**PostgreSQL**"
3. Railway provisions a PostgreSQL 16 instance
4. The database automatically creates a `DATABASE_URL` variable

### Step 4: Link Database to Application

Railway automatically links the database, but verify:

1. Click on your **beat-yesterday** service
2. Go to "**Variables**" tab
3. You should see `DATABASE_URL` already connected (purple link icon)

### Step 5: Add Strava Credentials

In the "Variables" tab of your beat-yesterday service, add:

```
STRAVA_CLIENT_ID=your_actual_client_id
STRAVA_CLIENT_SECRET=your_actual_client_secret
STRAVA_REFRESH_TOKEN=your_actual_refresh_token
```

**How to add variables:**
1. Click "**+ New Variable**"
2. Enter variable name (e.g., `STRAVA_CLIENT_ID`)
3. Enter value
4. Click "Add"
5. Repeat for all three Strava credentials

### Step 6: Trigger Redeploy

1. Click "**Settings**" tab
2. Scroll to "**Deployment**" section
3. Click "**Redeploy**"

Or simply push a new commit to GitHub - Railway auto-deploys!

### Step 7: Access Your Application

1. Go to "**Settings**" tab
2. Under "**Networking**", click "**Generate Domain**"
3. Railway creates a public URL: `beat-yesterday.up.railway.app`
4. Click the URL to open your app

### Step 8: Verify Deployment

```bash
# Get your Railway URL from the dashboard
export RAILWAY_URL=https://beat-yesterday.up.railway.app

# Test health endpoint
curl $RAILWAY_URL/actuator/health
# Expected: {"status":"UP"}

# Test Strava status
curl $RAILWAY_URL/api/oauth/strava/status

# Open in browser
open $RAILWAY_URL
```

## Configuration Details

### Environment Variables

Railway automatically provides:

| Variable | Source | Description |
|----------|--------|-------------|
| `DATABASE_URL` | Auto-generated | PostgreSQL connection string |
| `PORT` | Auto-set (8080) | Application port |
| `RAILWAY_ENVIRONMENT` | Auto-set | `production` |

You manually add:

| Variable | Value | Description |
|----------|-------|-------------|
| `STRAVA_CLIENT_ID` | From Strava API settings | OAuth client ID |
| `STRAVA_CLIENT_SECRET` | From Strava API settings | OAuth client secret |
| `STRAVA_REFRESH_TOKEN` | From OAuth flow | API refresh token |

### How DATABASE_URL Works

Railway provides a PostgreSQL connection string like:
```
postgresql://postgres:password@containers-us-west-123.railway.app:5432/railway
```

The application automatically uses this via the updated `application.yml`:
```yaml
spring:
  datasource:
    url: ${DATABASE_URL:jdbc:postgresql://...}
```

Railway's format includes `postgresql://` but Spring Boot expects `jdbc:postgresql://`. Railway automatically handles this conversion.

## Automatic Deployments

Railway automatically deploys when you push to your connected branch:

```bash
# Make changes locally
git add .
git commit -m "Update feature"
git push origin main

# Railway detects the push and auto-deploys
# Watch logs in Railway dashboard
```

### Deployment Logs

1. Go to Railway project dashboard
2. Click your **beat-yesterday** service
3. Click "**Deployments**" tab
4. Click the latest deployment to view logs in real-time

## Custom Domain (Optional)

### Add Your Domain

1. Go to "**Settings**" tab
2. Under "**Networking**" → "**Custom Domain**"
3. Click "**+ Custom Domain**"
4. Enter your domain (e.g., `beatyesterday.com`)
5. Railway provides DNS records to add at your registrar:
   ```
   Type: CNAME
   Name: @  (or subdomain)
   Value: your-app.up.railway.app
   ```

### SSL Certificate

Railway automatically provisions SSL certificates for custom domains via Let's Encrypt. No configuration needed!

## Monitoring and Logs

### View Logs

1. Click your service in the dashboard
2. Click "**Deployments**" tab
3. Click latest deployment
4. Real-time logs appear in the console

### Metrics

Railway provides basic metrics:
- CPU usage
- Memory usage
- Network traffic
- Request count

Access via "**Metrics**" tab in your service.

## Database Management

### Access PostgreSQL

**Option 1: Railway Dashboard**
1. Click the PostgreSQL service
2. Go to "**Data**" tab
3. View tables and run queries in the web UI

**Option 2: psql via Railway CLI**
```bash
# Install Railway CLI
npm i -g @railway/cli

# Login
railway login

# Link to your project
railway link

# Connect to database
railway connect postgres
```

**Option 3: External Tool (TablePlus, pgAdmin)**
```bash
# Get connection details from PostgreSQL service "Variables" tab
Host: containers-us-west-123.railway.app
Port: 5432
User: postgres
Password: (from PGPASSWORD variable)
Database: railway
```

### Backup Database

```bash
# Using Railway CLI
railway connect postgres -- pg_dump > backup.sql

# Restore from backup
railway connect postgres < backup.sql
```

## Scaling

Railway automatically scales your application:

- **Horizontal scaling:** Not available on free tier (1 instance only)
- **Vertical scaling:** Upgrade plan for more RAM/CPU
- **Database:** Shared PostgreSQL (5GB storage on free tier)

### Upgrade Options

| Plan | Price | Resources |
|------|-------|-----------|
| Trial | $5 credit | 512MB RAM, 1GB disk |
| Hobby | $5/month + usage | 8GB RAM, 100GB disk |
| Pro | $20/month + usage | 32GB RAM, custom |

## Troubleshooting

### Deployment Failed

**Check build logs:**
1. Click "**Deployments**" tab
2. Click failed deployment
3. Review logs for errors

Common issues:
- **Docker build timeout:** Increase timeout in Settings
- **Out of memory:** Upgrade plan or optimize Docker image
- **Missing env vars:** Verify all Strava credentials are set

### Database Connection Failed

**Verify DATABASE_URL:**
```bash
# In Railway dashboard
1. Click PostgreSQL service
2. Go to "Variables" tab
3. Copy DATABASE_URL value
4. Go to beat-yesterday service
5. Verify DATABASE_URL is linked (purple link icon)
```

**Test connection:**
```bash
railway connect postgres -- psql -c '\l'
```

### Application Crashes on Startup

**Common causes:**
- Missing Strava credentials → Check environment variables
- Database not initialized → Flyway should auto-run migrations
- Port mismatch → Railway auto-sets PORT=8080

**Check logs:**
```bash
# Railway CLI
railway logs

# Or view in dashboard under Deployments tab
```

### Cold Starts

Railway keeps instances warm on paid plans. On free tier, expect:
- **First request after 5min idle:** 2-3 second cold start
- **Subsequent requests:** Instant

No configuration needed - this is automatic.

## Cost Management

### Monitor Usage

1. Click "**Usage**" in the top navigation
2. View current month's spending
3. Set budget alerts (Settings → Billing)

### Optimize Costs

1. **Use free tier credits** - $5/month covers most hobby projects
2. **Pause unused services** - Settings → "Pause Service"
3. **Optimize Docker image** - Already done (Alpine Linux, ~200MB)
4. **Monitor database size** - Free tier includes 5GB

### Billing

- **Prepaid credits:** $5 credit/month on Trial
- **Pay as you go:** Charged monthly for excess usage
- **Estimates:** ~$3-8/month for low-traffic apps

## Advanced Configuration

### Railway Config File (Optional)

Create `railway.toml` in repository root:

```toml
[build]
builder = "dockerfile"
dockerfilePath = "Dockerfile"

[deploy]
restartPolicyType = "on-failure"
restartPolicyMaxRetries = 3
healthcheckPath = "/actuator/health"
healthcheckTimeout = 100
```

### Environment-Specific Variables

Railway supports multiple environments (production, staging):

1. Click "**+ New Environment**" in dashboard
2. Name it (e.g., "staging")
3. Each environment has separate DATABASE_URL and variables
4. Deploy different branches to different environments

## Migration from Railway to GCP

If you outgrow Railway, migration is straightforward:

1. Export database:
   ```bash
   railway connect postgres -- pg_dump > migration.sql
   ```

2. Import to Cloud SQL:
   ```bash
   gcloud sql import sql beatyesterday-db migration.sql
   ```

3. Update environment variables in Cloud Run
4. Deploy same Docker image

## Support

- **Railway Docs:** https://docs.railway.app
- **Discord Community:** https://discord.gg/railway
- **Status Page:** https://status.railway.app

## Comparison: Railway vs Others

| Feature | Railway | Cloud Run | Render |
|---------|---------|-----------|--------|
| Setup Time | 10 min | 30 min | 15 min |
| Free Tier | $5 credit/mo | 2M req/mo | 750 hrs/mo |
| PostgreSQL | Included | Cloud SQL ($10/mo) | Free tier |
| Auto Deploy | Yes | CI/CD setup | Yes |
| Custom Domain | Free SSL | Free SSL | Free SSL |
| Best For | New developers | GCP users | IaC fans |

## Next Steps

- Set up custom domain
- Enable database backups (Settings → Postgres → Backups)
- Configure alerts for uptime monitoring
- Implement database-backed token cache for multi-instance support

---

**Congratulations!** Your Beat Yesterday app is now deployed on Railway.

For more advanced deployments, see:
- [Google Cloud Run Deployment](DEPLOY_CLOUD_RUN.md)
- [Render Deployment](DEPLOY_RENDER.md)

# Deploy to Google Cloud Run

Google Cloud Run is a fully managed serverless platform that runs containerized applications. This guide walks you through deploying Beat Yesterday to Cloud Run with Cloud SQL PostgreSQL.

> **Quick Start:** For an interactive, guided deployment experience, use the included helper script: `./scripts/deploy-gcp.sh` (see end of guide for details)

## Prerequisites

- Google Cloud Platform account
- `gcloud` CLI installed and configured
  - **Windows:** Download from [Google Cloud SDK installer](https://cloud.google.com/sdk/docs/install-windows)
  - **macOS:** Install via Homebrew: `brew install --cask google-cloud-sdk`
  - **Linux:** Follow [installation guide](https://cloud.google.com/sdk/docs/install-linux)
- Strava API credentials (Client ID, Client Secret, Refresh Token)
- Project with billing enabled

> **Windows Users:** After installing gcloud CLI, restart your terminal or PowerShell to ensure `gcloud` is in your PATH.

## Cost Estimate

- **Cloud Run:** Free tier includes 2 million requests/month, then $0.00002400 per request
- **Cloud SQL (db-f1-micro):** ~$7-10/month
- **Total for low traffic:** ~$10-15/month

## Step 1: Set Up Google Cloud Project

```bash
# Set your project ID
export PROJECT_ID=your-project-id
gcloud config set project $PROJECT_ID

# Enable required APIs
gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  sql-component.googleapis.com \
  sqladmin.googleapis.com \
  secretmanager.googleapis.com
```

## Step 2: Create Cloud SQL PostgreSQL Instance

```bash
# Create PostgreSQL 16 instance (adjust region as needed)
gcloud sql instances create beatyesterday-db \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=us-central1 \
  --storage-type=SSD \
  --storage-size=10GB \
  --storage-auto-increase

# Create the database
gcloud sql databases create beatyesterday \
  --instance=beatyesterday-db

# Set postgres user password
gcloud sql users set-password postgres \
  --instance=beatyesterday-db \
  --password=$(openssl rand -base64 32)

# Note the password or store it in Secret Manager
```

## Step 3: Store Secrets in Secret Manager

Cloud Run best practice is to use Secret Manager for sensitive values.

```bash
# Create secrets for Strava credentials
echo -n "your_strava_client_id" | \
  gcloud secrets create strava-client-id --data-file=-

echo -n "your_strava_client_secret" | \
  gcloud secrets create strava-client-secret --data-file=-

echo -n "your_strava_refresh_token" | \
  gcloud secrets create strava-refresh-token --data-file=-

# Create secret for database password
echo -n "your_postgres_password" | \
  gcloud secrets create db-password --data-file=-
```

## Step 4: Build and Push Docker Image

### Option A: Build with Cloud Build (Recommended)

```bash
# Build in the cloud (no local Docker required)
gcloud builds submit \
  --tag gcr.io/$PROJECT_ID/beat-yesterday \
  --timeout=20m
```

### Option B: Build Locally

```bash
# Build Docker image locally
docker build -t gcr.io/$PROJECT_ID/beat-yesterday .

# Configure Docker to use gcloud for authentication
gcloud auth configure-docker

# Push to Google Container Registry
docker push gcr.io/$PROJECT_ID/beat-yesterday
```

## Step 5: Deploy to Cloud Run

```bash
# Get Cloud SQL connection name
export CONNECTION_NAME=$(gcloud sql instances describe beatyesterday-db \
  --format='value(connectionName)')

# Deploy to Cloud Run
gcloud run deploy beat-yesterday \
  --image gcr.io/$PROJECT_ID/beat-yesterday \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --add-cloudsql-instances $CONNECTION_NAME \
  --set-env-vars "DB_HOST=/cloudsql/$CONNECTION_NAME" \
  --set-env-vars "DB_NAME=beatyesterday" \
  --set-env-vars "DB_USERNAME=postgres" \
  --set-secrets "DB_PASSWORD=db-password:latest" \
  --set-secrets "STRAVA_CLIENT_ID=strava-client-id:latest" \
  --set-secrets "STRAVA_CLIENT_SECRET=strava-client-secret:latest" \
  --set-secrets "STRAVA_REFRESH_TOKEN=strava-refresh-token:latest" \
  --max-instances 3 \
  --min-instances 0 \
  --memory 512Mi \
  --timeout 60s \
  --port 8080
```

## Step 6: Verify Deployment

```bash
# Get the service URL
export SERVICE_URL=$(gcloud run services describe beat-yesterday \
  --region us-central1 \
  --format='value(status.url)')

echo "Service URL: $SERVICE_URL"

# Test health endpoint
curl $SERVICE_URL/actuator/health

# Expected response:
# {"status":"UP"}

# Test Strava authentication status
curl $SERVICE_URL/api/oauth/strava/status

# Test dashboard endpoint
curl $SERVICE_URL/api/dashboard
```

## Cloud SQL Proxy Connection

Cloud Run connects to Cloud SQL via Unix domain sockets. The connection configuration in `application.yml` needs to be updated for production.

### Create Cloud Run Profile

Create `/backend/src/main/resources/application-cloudrun.yml`:

```yaml
spring:
  datasource:
    # Cloud SQL Proxy uses Unix socket connection
    url: jdbc:postgresql:///${DB_NAME:beatyesterday}?host=/cloudsql/${DB_HOST}&user=${DB_USERNAME:postgres}&password=${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    com.beatyesterday: INFO
    org.springframework: WARN
```

Then deploy with the profile:

```bash
gcloud run deploy beat-yesterday \
  --set-env-vars "SPRING_PROFILES_ACTIVE=cloudrun" \
  # ... other flags
```

## Automatic Deployments with Cloud Build

A complete `cloudbuild.yaml` configuration file is included in the repository root. It defines a full CI/CD pipeline that:
- Builds the Docker image (with layer caching for faster builds)
- Pushes to Google Container Registry
- Deploys to Cloud Run with all necessary environment variables and secrets

**Option 1: One-command deployment using cloudbuild.yaml**

```bash
# Get your Cloud SQL connection name
export CONNECTION_NAME=$(gcloud sql instances describe beatyesterday-db \
  --format='value(connectionName)')

# Deploy using Cloud Build
gcloud builds submit \
  --config cloudbuild.yaml \
  --substitutions=_DB_CONNECTION_NAME="$CONNECTION_NAME"
```

This single command handles the entire build and deployment process.

**Option 2: Automatic deployments on git push**

Then set up a build trigger:

```bash
# Connect your GitHub repository
gcloud builds triggers create github \
  --repo-name=beatyesterday \
  --repo-owner=your-github-username \
  --branch-pattern="^main$" \
  --build-config=cloudbuild.yaml
```

## Scaling Configuration

### Keep Instance Warm (Eliminate Cold Starts)

```bash
gcloud run services update beat-yesterday \
  --region us-central1 \
  --min-instances 1
```

Cost: ~$10/month for 1 always-on instance

### Cost-Optimized (Allow Cold Starts)

```bash
gcloud run services update beat-yesterday \
  --region us-central1 \
  --min-instances 0 \
  --max-instances 3
```

Cold start time: ~2-3 seconds with Alpine JRE 21

## Monitoring and Logs

### View Logs

```bash
# Stream logs in real-time
gcloud run logs tail beat-yesterday --region us-central1

# View recent logs
gcloud run logs read beat-yesterday --region us-central1 --limit=100
```

### Access Cloud Console

- **Cloud Run:** https://console.cloud.google.com/run
- **Cloud SQL:** https://console.cloud.google.com/sql
- **Logs:** https://console.cloud.google.com/logs

## Troubleshooting

### Container Failed to Start

Check logs for detailed error:
```bash
gcloud run logs read beat-yesterday --region us-central1 --limit=50
```

Common issues:
- Missing secrets: Verify all secrets exist and have latest version
- Cloud SQL connection: Check connection name is correct
- Port mismatch: Ensure `server.port=8080` in application.yml

### Database Connection Timeout

```bash
# Verify Cloud SQL instance is running
gcloud sql instances describe beatyesterday-db

# Check Cloud SQL connection is added to Cloud Run service
gcloud run services describe beat-yesterday --region us-central1 \
  --format='value(spec.template.spec.containers[0].resources.cloudSqlInstances)'
```

### Cold Start Timeout

Increase timeout or set min instances:
```bash
gcloud run services update beat-yesterday \
  --region us-central1 \
  --timeout 60s \
  --min-instances 1
```

### Token Refresh Issues with Multiple Instances

If you see intermittent 401 errors when Cloud Run scales to multiple instances, you need to implement the database-backed token cache (covered in Phase 4 of the implementation plan).

### Windows-Specific Issues

**gcloud command not found**
- Ensure you've restarted your terminal/PowerShell after installation
- Check `gcloud` is in your PATH: run `echo $env:PATH` (PowerShell) or `echo %PATH%` (CMD)
- Re-run the installer if needed

**Permission errors when creating secrets**
- Run PowerShell or CMD as Administrator
- Verify you have necessary IAM permissions in your GCP project

**Line ending issues with scripts**
- If you see `^M` or carriage return errors, convert line endings:
  ```powershell
  # PowerShell
  (Get-Content .\scripts\deploy-gcp.sh) | Set-Content -NoNewline .\scripts\deploy-gcp.sh
  ```
- Or use Git Bash instead of PowerShell to run bash scripts

**Echo command differences**
- Windows CMD/PowerShell `echo` adds newlines. Use the format shown in the guide:
  ```bash
  echo -n "your_value" | gcloud secrets create secret-name --data-file=-
  ```
- The `-n` flag prevents extra newlines in your secrets

## Deployment Helper Script (Optional)

For a guided, interactive deployment experience, use the helper script:

```bash
# From repository root (use Git Bash on Windows)
./scripts/deploy-gcp.sh
```

This script walks you through each step with validation and helpful prompts.

## Cleanup

To delete all resources:

```bash
# Delete Cloud Run service
gcloud run services delete beat-yesterday --region us-central1

# Delete Cloud SQL instance (Warning: deletes all data!)
gcloud sql instances delete beatyesterday-db

# Delete secrets
gcloud secrets delete strava-client-id
gcloud secrets delete strava-client-secret
gcloud secrets delete strava-refresh-token
gcloud secrets delete db-password

# Delete container images
gcloud container images delete gcr.io/$PROJECT_ID/beat-yesterday
```

## Next Steps

- Set up custom domain: https://cloud.google.com/run/docs/mapping-custom-domains
- Enable Cloud CDN for static assets
- Configure Cloud Armor for DDoS protection
- Set up Cloud Monitoring alerts
- Implement database-backed token cache for multi-instance support

## Cost Optimization Tips

1. **Use db-f1-micro** for low traffic (~$7/month)
2. **Set min-instances=0** to allow scale-to-zero (~$0 when idle)
3. **Use Artifact Registry** instead of GCR (10GB free storage)
4. **Enable Cloud SQL backups** only if needed (adds ~$0.05/GB/month)
5. **Monitor usage** in Cloud Console billing dashboard

---

For simpler alternatives, see:
- [Railway Deployment](DEPLOY_RAILWAY.md) - Easiest option for new developers
- [Render Deployment](DEPLOY_RENDER.md) - Infrastructure-as-code approach

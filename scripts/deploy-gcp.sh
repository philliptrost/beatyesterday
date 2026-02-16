#!/usr/bin/env bash
#
# Beat Yesterday - Google Cloud Run Deployment Helper
#
# This script guides you through deploying Beat Yesterday to Google Cloud Run.
# It validates prerequisites, checks GCP configuration, and executes deployment commands.
#
# Prerequisites:
#   - gcloud CLI installed and authenticated
#   - Strava API credentials (Client ID, Client Secret, Refresh Token)
#   - Active GCP project with billing enabled
#
# Usage:
#   ./scripts/deploy-gcp.sh
#

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print functions
print_header() {
    echo -e "\n${BLUE}===================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}===================================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}!${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Check if gcloud is installed
check_gcloud() {
    print_header "Checking Prerequisites"

    if ! command -v gcloud &> /dev/null; then
        print_error "gcloud CLI not found"
        echo ""
        echo "Please install gcloud CLI:"
        echo "  Windows: https://cloud.google.com/sdk/docs/install-windows"
        echo "  macOS:   https://cloud.google.com/sdk/docs/install-mac"
        echo "  Linux:   https://cloud.google.com/sdk/docs/install-linux"
        exit 1
    fi

    print_success "gcloud CLI installed ($(gcloud version --format='value(core)' 2>/dev/null || echo 'version unknown'))"
}

# Check if authenticated
check_auth() {
    if ! gcloud auth list --filter=status:ACTIVE --format="value(account)" &> /dev/null; then
        print_error "Not authenticated with gcloud"
        echo ""
        echo "Please run: gcloud auth login"
        exit 1
    fi

    ACTIVE_ACCOUNT=$(gcloud auth list --filter=status:ACTIVE --format="value(account)" 2>/dev/null | head -n1)
    print_success "Authenticated as: $ACTIVE_ACCOUNT"
}

# Get or set project ID
get_project_id() {
    CURRENT_PROJECT=$(gcloud config get-value project 2>/dev/null || echo "")

    if [ -z "$CURRENT_PROJECT" ]; then
        print_warning "No default project set"
        echo ""
        read -p "Enter your GCP Project ID: " PROJECT_ID
        gcloud config set project "$PROJECT_ID"
    else
        print_success "Using project: $CURRENT_PROJECT"
        echo ""
        read -p "Use this project? (Y/n): " CONFIRM
        if [[ "$CONFIRM" =~ ^[Nn]$ ]]; then
            read -p "Enter your GCP Project ID: " PROJECT_ID
            gcloud config set project "$PROJECT_ID"
        else
            PROJECT_ID="$CURRENT_PROJECT"
        fi
    fi

    export PROJECT_ID
    print_info "Project ID: $PROJECT_ID"
}

# Enable required APIs
enable_apis() {
    print_header "Enabling Required GCP APIs"

    print_info "Enabling APIs (this may take a few minutes)..."

    gcloud services enable \
        run.googleapis.com \
        cloudbuild.googleapis.com \
        sql-component.googleapis.com \
        sqladmin.googleapis.com \
        secretmanager.googleapis.com \
        --project="$PROJECT_ID"

    print_success "All required APIs enabled"
}

# Create Cloud SQL instance
create_cloud_sql() {
    print_header "Cloud SQL PostgreSQL Setup"

    # Check if instance already exists
    if gcloud sql instances describe beatyesterday-db --project="$PROJECT_ID" &> /dev/null; then
        print_warning "Cloud SQL instance 'beatyesterday-db' already exists"
        echo ""
        read -p "Skip Cloud SQL creation? (Y/n): " SKIP_SQL
        if [[ ! "$SKIP_SQL" =~ ^[Nn]$ ]]; then
            print_info "Skipping Cloud SQL creation"
            return
        fi
    fi

    echo ""
    print_info "Creating Cloud SQL instance (this takes ~5-10 minutes)..."
    print_warning "Instance: beatyesterday-db"
    print_warning "Database: PostgreSQL 16"
    print_warning "Tier: db-f1-micro (~\$7-10/month)"
    print_warning "Region: us-central1"
    echo ""
    read -p "Continue? (Y/n): " CONFIRM

    if [[ "$CONFIRM" =~ ^[Nn]$ ]]; then
        print_error "Cloud SQL creation cancelled"
        exit 1
    fi

    gcloud sql instances create beatyesterday-db \
        --database-version=POSTGRES_16 \
        --tier=db-f1-micro \
        --region=us-central1 \
        --storage-type=SSD \
        --storage-size=10GB \
        --storage-auto-increase \
        --project="$PROJECT_ID"

    print_success "Cloud SQL instance created"

    # Create database
    print_info "Creating database 'beatyesterday'..."
    gcloud sql databases create beatyesterday \
        --instance=beatyesterday-db \
        --project="$PROJECT_ID"

    print_success "Database created"

    # Set postgres password
    echo ""
    print_warning "Set a secure password for postgres user"
    DB_PASSWORD=$(openssl rand -base64 32 2>/dev/null || echo "changeme-$(date +%s)")
    echo "Generated password: $DB_PASSWORD"
    echo ""
    print_warning "SAVE THIS PASSWORD - you'll need it for Secret Manager"
    echo ""
    read -p "Press Enter to continue..."

    gcloud sql users set-password postgres \
        --instance=beatyesterday-db \
        --password="$DB_PASSWORD" \
        --project="$PROJECT_ID"

    print_success "Postgres password set"
}

# Create secrets in Secret Manager
create_secrets() {
    print_header "Secret Manager Setup"

    echo "You'll need your Strava API credentials."
    echo "Get them from: https://www.strava.com/settings/api"
    echo ""

    # Strava Client ID
    if gcloud secrets describe strava-client-id --project="$PROJECT_ID" &> /dev/null; then
        print_warning "Secret 'strava-client-id' already exists (skipping)"
    else
        read -p "Enter Strava Client ID: " STRAVA_CLIENT_ID
        echo -n "$STRAVA_CLIENT_ID" | gcloud secrets create strava-client-id \
            --data-file=- \
            --project="$PROJECT_ID"
        print_success "strava-client-id created"
    fi

    # Strava Client Secret
    if gcloud secrets describe strava-client-secret --project="$PROJECT_ID" &> /dev/null; then
        print_warning "Secret 'strava-client-secret' already exists (skipping)"
    else
        read -sp "Enter Strava Client Secret: " STRAVA_CLIENT_SECRET
        echo ""
        echo -n "$STRAVA_CLIENT_SECRET" | gcloud secrets create strava-client-secret \
            --data-file=- \
            --project="$PROJECT_ID"
        print_success "strava-client-secret created"
    fi

    # Strava Refresh Token
    if gcloud secrets describe strava-refresh-token --project="$PROJECT_ID" &> /dev/null; then
        print_warning "Secret 'strava-refresh-token' already exists (skipping)"
    else
        read -sp "Enter Strava Refresh Token: " STRAVA_REFRESH_TOKEN
        echo ""
        echo -n "$STRAVA_REFRESH_TOKEN" | gcloud secrets create strava-refresh-token \
            --data-file=- \
            --project="$PROJECT_ID"
        print_success "strava-refresh-token created"
    fi

    # Database Password
    if gcloud secrets describe db-password --project="$PROJECT_ID" &> /dev/null; then
        print_warning "Secret 'db-password' already exists (skipping)"
    else
        if [ -n "$DB_PASSWORD" ]; then
            echo -n "$DB_PASSWORD" | gcloud secrets create db-password \
                --data-file=- \
                --project="$PROJECT_ID"
            print_success "db-password created"
        else
            read -sp "Enter database password: " DB_PASSWORD_INPUT
            echo ""
            echo -n "$DB_PASSWORD_INPUT" | gcloud secrets create db-password \
                --data-file=- \
                --project="$PROJECT_ID"
            print_success "db-password created"
        fi
    fi
}

# Build and deploy
build_and_deploy() {
    print_header "Building and Deploying to Cloud Run"

    # Get Cloud SQL connection name
    CONNECTION_NAME=$(gcloud sql instances describe beatyesterday-db \
        --format='value(connectionName)' \
        --project="$PROJECT_ID")

    print_info "Cloud SQL Connection: $CONNECTION_NAME"
    echo ""

    # Choose build method
    echo "Build options:"
    echo "  1) Quick build (gcloud builds submit)"
    echo "  2) Full CI/CD pipeline (cloudbuild.yaml)"
    echo ""
    read -p "Choose build method (1 or 2): " BUILD_METHOD

    if [ "$BUILD_METHOD" = "2" ]; then
        print_info "Building with cloudbuild.yaml..."
        gcloud builds submit \
            --config cloudbuild.yaml \
            --substitutions=_DB_CONNECTION_NAME="$CONNECTION_NAME" \
            --project="$PROJECT_ID"
    else
        print_info "Building Docker image (this takes ~10-15 minutes)..."
        gcloud builds submit \
            --tag gcr.io/$PROJECT_ID/beat-yesterday \
            --timeout=20m \
            --project="$PROJECT_ID"

        print_success "Docker image built"

        # Deploy to Cloud Run
        print_info "Deploying to Cloud Run..."
        gcloud run deploy beat-yesterday \
            --image gcr.io/$PROJECT_ID/beat-yesterday \
            --platform managed \
            --region us-central1 \
            --allow-unauthenticated \
            --add-cloudsql-instances "$CONNECTION_NAME" \
            --set-env-vars "SPRING_PROFILES_ACTIVE=cloudrun" \
            --set-env-vars "DB_NAME=beatyesterday" \
            --set-env-vars "DB_USERNAME=postgres" \
            --set-env-vars "DB_HOST=$CONNECTION_NAME" \
            --set-secrets "DB_PASSWORD=db-password:latest" \
            --set-secrets "STRAVA_CLIENT_ID=strava-client-id:latest" \
            --set-secrets "STRAVA_CLIENT_SECRET=strava-client-secret:latest" \
            --set-secrets "STRAVA_REFRESH_TOKEN=strava-refresh-token:latest" \
            --max-instances 3 \
            --min-instances 0 \
            --memory 512Mi \
            --timeout 60s \
            --port 8080 \
            --project="$PROJECT_ID"
    fi

    print_success "Deployment complete!"
}

# Test deployment
test_deployment() {
    print_header "Testing Deployment"

    SERVICE_URL=$(gcloud run services describe beat-yesterday \
        --region us-central1 \
        --format='value(status.url)' \
        --project="$PROJECT_ID")

    print_info "Service URL: $SERVICE_URL"
    echo ""

    print_info "Testing health endpoint..."
    HEALTH_RESPONSE=$(curl -s "$SERVICE_URL/actuator/health" || echo "failed")

    if echo "$HEALTH_RESPONSE" | grep -q '"status":"UP"'; then
        print_success "Health check passed!"
    else
        print_error "Health check failed"
        echo "Response: $HEALTH_RESPONSE"
    fi

    echo ""
    print_success "Deployment successful!"
    echo ""
    echo -e "${GREEN}Your app is live at:${NC}"
    echo -e "${BLUE}$SERVICE_URL${NC}"
    echo ""
    echo "Next steps:"
    echo "  1. Visit the URL above to see your app"
    echo "  2. Update Strava OAuth redirect URI to: $SERVICE_URL/api/oauth/strava/callback"
    echo "  3. Test importing activities: POST $SERVICE_URL/api/import"
    echo ""
}

# Main execution
main() {
    clear
    echo -e "${BLUE}"
    cat << "EOF"
╔═══════════════════════════════════════════════════╗
║                                                   ║
║        Beat Yesterday - Cloud Run Deployment     ║
║                                                   ║
╚═══════════════════════════════════════════════════╝
EOF
    echo -e "${NC}"

    check_gcloud
    check_auth
    get_project_id

    echo ""
    read -p "Enable required GCP APIs? (Y/n): " ENABLE_APIS
    if [[ ! "$ENABLE_APIS" =~ ^[Nn]$ ]]; then
        enable_apis
    fi

    echo ""
    read -p "Create/configure Cloud SQL? (Y/n): " CREATE_SQL
    if [[ ! "$CREATE_SQL" =~ ^[Nn]$ ]]; then
        create_cloud_sql
    fi

    echo ""
    read -p "Create/configure secrets? (Y/n): " CREATE_SECRETS
    if [[ ! "$CREATE_SECRETS" =~ ^[Nn]$ ]]; then
        create_secrets
    fi

    echo ""
    read -p "Build and deploy to Cloud Run? (Y/n): " DEPLOY
    if [[ ! "$DEPLOY" =~ ^[Nn]$ ]]; then
        build_and_deploy
        test_deployment
    fi

    print_header "Deployment Complete!"
    print_success "All done!"
}

# Run main function
main

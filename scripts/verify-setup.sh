#!/bin/bash
# Beat Yesterday - Setup Verification Script
# Checks prerequisites before attempting to run the application

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Tracking variables
ERRORS=0
WARNINGS=0

echo "========================================="
echo "Beat Yesterday - Setup Verification"
echo "========================================="
echo ""

# Check 1: JDK Version
echo -n "Checking JDK version... "
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
    JAVA_MAJOR=$(echo "$JAVA_VERSION" | cut -d'.' -f1)

    if [ "$JAVA_MAJOR" -ge 21 ]; then
        echo -e "${GREEN}✓${NC} JDK $JAVA_VERSION"
    else
        echo -e "${RED}✗${NC} JDK $JAVA_VERSION (requires JDK 21+)"
        echo "  Install JDK 21: https://sdkman.io/ or https://adoptium.net/"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo -e "${RED}✗${NC} Java not found"
    echo "  Install JDK 21: https://sdkman.io/ or https://adoptium.net/"
    ERRORS=$((ERRORS + 1))
fi

# Check 2: Docker availability
echo -n "Checking Docker... "
if command -v docker &> /dev/null; then
    if docker ps &> /dev/null; then
        echo -e "${GREEN}✓${NC} Docker is running"
    else
        echo -e "${RED}✗${NC} Docker daemon not running"
        echo "  Start Docker Desktop or run: sudo systemctl start docker"
        ERRORS=$((ERRORS + 1))
    fi
else
    echo -e "${RED}✗${NC} Docker not found"
    echo "  Install Docker: https://docs.docker.com/get-docker/"
    ERRORS=$((ERRORS + 1))
fi

# Check 3: PostgreSQL container
echo -n "Checking PostgreSQL container... "
if docker ps 2>/dev/null | grep -q postgres:16-alpine; then
    echo -e "${GREEN}✓${NC} PostgreSQL is running"
elif docker ps -a 2>/dev/null | grep -q postgres:16-alpine; then
    echo -e "${YELLOW}⚠${NC} PostgreSQL container exists but is not running"
    echo "  Start it: docker-compose up -d"
    WARNINGS=$((WARNINGS + 1))
else
    echo -e "${YELLOW}⚠${NC} PostgreSQL container not found"
    echo "  Create it: docker-compose up -d"
    WARNINGS=$((WARNINGS + 1))
fi

# Check 4: Port 8080 availability
echo -n "Checking port 8080... "
if command -v lsof &> /dev/null; then
    if lsof -i :8080 &> /dev/null; then
        echo -e "${RED}✗${NC} Port 8080 is already in use"
        echo "  Find process: lsof -i :8080"
        echo "  Kill process: kill -9 <PID>"
        echo "  Or change port: export PORT=8081"
        ERRORS=$((ERRORS + 1))
    else
        echo -e "${GREEN}✓${NC} Port 8080 is available"
    fi
elif command -v netstat &> /dev/null; then
    if netstat -tuln 2>/dev/null | grep -q ":8080 "; then
        echo -e "${RED}✗${NC} Port 8080 is already in use"
        echo "  Find process: netstat -tuln | grep 8080"
        echo "  Or change port: export PORT=8081"
        ERRORS=$((ERRORS + 1))
    else
        echo -e "${GREEN}✓${NC} Port 8080 is available"
    fi
else
    echo -e "${YELLOW}⚠${NC} Cannot check (lsof/netstat not available)"
    WARNINGS=$((WARNINGS + 1))
fi

# Check 5: Environment variables
echo ""
echo "Checking environment variables:"

check_env_var() {
    local var_name=$1
    local var_value="${!var_name}"

    if [ -z "$var_value" ]; then
        echo -e "  ${RED}✗${NC} $var_name not set"
        return 1
    elif [ "$var_value" == "your_${var_name,,}_here" ] || [ "$var_value" == "your_client_id_here" ] || [ "$var_value" == "your_client_secret_here" ] || [ "$var_value" == "your_refresh_token_here" ]; then
        echo -e "  ${YELLOW}⚠${NC} $var_name is set to placeholder value"
        return 2
    else
        echo -e "  ${GREEN}✓${NC} $var_name is set"
        return 0
    fi
}

# Check Strava credentials (required)
check_env_var "STRAVA_CLIENT_ID"
if [ $? -eq 1 ]; then
    echo "    Set it: export STRAVA_CLIENT_ID=your_value"
    ERRORS=$((ERRORS + 1))
elif [ $? -eq 2 ]; then
    echo "    Update .env file with your actual Strava Client ID"
    WARNINGS=$((WARNINGS + 1))
fi

check_env_var "STRAVA_CLIENT_SECRET"
if [ $? -eq 1 ]; then
    echo "    Set it: export STRAVA_CLIENT_SECRET=your_value"
    ERRORS=$((ERRORS + 1))
elif [ $? -eq 2 ]; then
    echo "    Update .env file with your actual Strava Client Secret"
    WARNINGS=$((WARNINGS + 1))
fi

check_env_var "STRAVA_REFRESH_TOKEN"
if [ $? -eq 1 ]; then
    echo "    Set it: export STRAVA_REFRESH_TOKEN=your_value"
    ERRORS=$((ERRORS + 1))
elif [ $? -eq 2 ]; then
    echo "    Update .env file with your actual Strava Refresh Token"
    WARNINGS=$((WARNINGS + 1))
fi

# Check 6: Database connectivity (only if PostgreSQL is running)
if docker ps 2>/dev/null | grep -q postgres:16-alpine; then
    echo ""
    echo -n "Checking database connectivity... "

    DB_HOST=${DB_HOST:-localhost}
    DB_PORT=${DB_PORT:-5432}
    DB_NAME=${DB_NAME:-beatyesterday}
    DB_USERNAME=${DB_USERNAME:-beatyesterday}
    DB_PASSWORD=${DB_PASSWORD:-beatyesterday}

    # Try to connect using docker exec
    if docker exec $(docker ps -qf "ancestor=postgres:16-alpine") psql -U "$DB_USERNAME" -d "$DB_NAME" -c '\q' 2>/dev/null; then
        echo -e "${GREEN}✓${NC} Database connection successful"
    else
        echo -e "${YELLOW}⚠${NC} Cannot connect to database"
        echo "  This is normal if the database hasn't been initialized yet"
        echo "  The application will create the schema on first run"
        WARNINGS=$((WARNINGS + 1))
    fi
fi

# Check 7: Node.js (for frontend development)
echo ""
echo -n "Checking Node.js (for frontend)... "
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v)
    NODE_MAJOR=$(echo "$NODE_VERSION" | sed 's/v//' | cut -d'.' -f1)

    if [ "$NODE_MAJOR" -ge 18 ]; then
        echo -e "${GREEN}✓${NC} Node.js $NODE_VERSION"
    else
        echo -e "${YELLOW}⚠${NC} Node.js $NODE_VERSION (recommended: 22+)"
        echo "  Upgrade Node.js: https://nodejs.org/ or use nvm"
        WARNINGS=$((WARNINGS + 1))
    fi
else
    echo -e "${YELLOW}⚠${NC} Node.js not found (optional for backend-only development)"
    echo "  Install Node.js 22: https://nodejs.org/ or https://github.com/nvm-sh/nvm"
fi

# Summary
echo ""
echo "========================================="
echo "Summary"
echo "========================================="

if [ $ERRORS -eq 0 ] && [ $WARNINGS -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo ""
    echo "You're ready to run the application:"
    echo "  1. Start PostgreSQL: docker-compose up -d"
    echo "  2. Start backend: cd backend && ./gradlew bootRun"
    echo "  3. Start frontend: cd frontend && npm install && npm run dev"
    echo ""
    echo "Or use the quick start script:"
    echo "  ./scripts/start-dev.sh"
    exit 0
elif [ $ERRORS -eq 0 ]; then
    echo -e "${YELLOW}⚠ $WARNINGS warning(s) found${NC}"
    echo ""
    echo "You can proceed, but you may encounter issues."
    echo "See details above and consult docs/TROUBLESHOOTING.md"
    exit 0
else
    echo -e "${RED}✗ $ERRORS error(s) found${NC}"
    if [ $WARNINGS -gt 0 ]; then
        echo -e "${YELLOW}⚠ $WARNINGS warning(s) found${NC}"
    fi
    echo ""
    echo "Please fix the errors above before running the application."
    echo "See docs/TROUBLESHOOTING.md for detailed help."
    exit 1
fi

#!/bin/bash

# Code Arena - Simplified Automated Setup Script

set -e

ENV_FILE=".env"
ENV_EXAMPLE=".env.example"
SSL_DIR="infra/nginx/ssl"

echo "-------------------------------------------------------"
echo "🚀 Welcome to the Code Arena Setup Utility"
echo "-------------------------------------------------------"
echo "Please choose an environment configuration mode:"
echo "1) Full Manual: Recreate .env and prompt for all variables"
echo "2) Supplemental: Load existing .env and only prompt for missing keys"
echo "3) Skip Config: Use current .env and proceed with SSL/Docker"
echo "-------------------------------------------------------"
read -p "Select an option [1-3]: " setup_mode

case $setup_mode in
    1)
        echo "📝 Mode: Full Manual Configuration"
        rm -f "$ENV_FILE"
        touch "$ENV_FILE"
        exec {conf_fd}< "$ENV_EXAMPLE"
        while IFS= read -u "$conf_fd" -r line || [[ -n "$line" ]]; do
            if [[ -z "$line" ]] || [[ "$line" == \#* ]]; then
                echo "$line" >> "$ENV_FILE"
                continue
            fi
            var_name=$(echo "$line" | cut -d'=' -f1)
            read -p "Enter value for $var_name: " user_val
            echo "$var_name=$user_val" >> "$ENV_FILE"
        done
        exec {conf_fd}>&-
        ;;
    2)
        echo "🔍 Mode: Supplemental Configuration"
        if [ ! -f "$ENV_FILE" ]; then
            echo "ℹ️ No .env found, starting fresh."
            touch "$ENV_FILE"
        fi
        
        temp_env=".env.tmp"
        cp "$ENV_FILE" "$temp_env"
        
        exec {conf_fd}< "$ENV_EXAMPLE"
        while IFS= read -u "$conf_fd" -r line || [[ -n "$line" ]]; do
            if [[ -z "$line" ]] || [[ "$line" == \#* ]]; then
                continue
            fi
            var_name=$(echo "$line" | cut -d'=' -f1)
            
            # Check if var is already in .env or exported in shell
            if grep -q "^$var_name=" "$temp_env"; then
                current_val=$(grep "^$var_name=" "$temp_env" | cut -d'=' -f2-)
                echo "✅ $var_name is already set to: $current_val"
            elif [ -n "${!var_name}" ]; then
                 echo "$var_name=${!var_name}" >> "$temp_env"
                 echo "✅ $var_name inherited from shell: ${!var_name}"
            else
                read -p "❓ $var_name is missing. Enter value: " user_val
                echo "$var_name=$user_val" >> "$temp_env"
            fi
        done
        exec {conf_fd}>&-
        mv "$temp_env" "$ENV_FILE"
        ;;
    3)
        echo "Mode: Manual File Provision"
        if [ ! -f "$ENV_FILE" ]; then
            echo "❌ Error: $ENV_FILE not found! Please create it and run again."
            exit 1
        fi
        echo "✅ $ENV_FILE found. Proceeding with setup..."
        ;;
    *)
        echo "❌ Invalid option. Exiting."
        exit 1
        ;;
esac

echo "✅ Environment configuration finalized."

# 4. SSL Certificate Generation
if [ ! -f "$SSL_DIR/localhost.crt" ]; then
    echo "🔐 Generating self-signed SSL certificates..."
    mkdir -p "$SSL_DIR"
    openssl req -x509 -noenc -days 365 -newkey rsa:2048 \
      -keyout "$SSL_DIR/localhost.key" \
      -out "$SSL_DIR/localhost.crt" \
      -subj "/C=PT/ST=Lisbon/L=Lisbon/O=42/OU=Transcendence/CN=localhost"
    echo "✅ SSL certificates generated in $SSL_DIR"
else
    echo "✅ SSL certificates already exist."
fi

# 5. Docker Compose Startup
echo "🐳 Starting Docker containers..."

# Strictly require Docker Compose v2+
if ! docker compose version &> /dev/null; then
    echo "❌ Error: 'docker compose' (v2+) is required but not found."
    echo "💡 Please install or upgrade Docker: https://docs.docker.com/engine/install/"
    exit 1
fi

docker compose up -d --build

# 6. Service Readiness Verification
HTTPS_PORT=$(grep "^HOST_TO_PROXY_HTTPS_PORT=" "$ENV_FILE" | cut -d'=' -f2-)
HTTPS_PORT=${HTTPS_PORT:-443}
HEALTH_URL="https://localhost:$HTTPS_PORT/api/health"

echo "⏳ Waiting for Code Arena services to initialize..."
echo "This might take a moment as database migrations and backend startup are completed."

MAX_ATTEMPTS=60
ATTEMPT=1
READY=0

while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
    # Try using curl on host if available, ignoring SSL warnings (-k)
    # Check if response body contains "status":"UP"
    if command -v curl &> /dev/null; then
        if curl -k -s --max-time 3 "$HEALTH_URL" | grep -q '"status":"UP"'; then
            READY=1
            break
        fi
    else
        # Fallback to docker compose exec checking backend port directly via wget
        BACKEND_PORT=$(grep "^BACKEND_PORT=" "$ENV_FILE" | cut -d'=' -f2-)
        BACKEND_PORT=${BACKEND_PORT:-8080}
        if docker compose exec -T backend wget -q -O - http://127.0.0.1:$BACKEND_PORT/api/health | grep -q '"status":"UP"'; then
            READY=1
            break
        fi
    fi
    echo -n "•"
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
done

echo ""

if [ $READY -eq 1 ]; then
    echo "-------------------------------------------------------"
    echo "🎉 Setup complete! Reach the platform at:"
    echo "🔗 https://localhost"
    echo "-------------------------------------------------------"
else
    echo "-------------------------------------------------------"
    echo "⚠️ Setup script completed, but containers are still starting."
    echo "🔗 Reach the platform at: https://localhost"
    echo "💡 Monitor initialization progress with: docker compose ps"
    echo "-------------------------------------------------------"
fi

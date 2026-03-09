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
        while IFS= read -r line || [[ -n "$line" ]]; do
            if [[ -z "$line" ]] || [[ "$line" == \#* ]]; then
                echo "$line" >> "$ENV_FILE"
                continue
            fi
            var_name=$(echo "$line" | cut -d'=' -f1)
            read -p "Enter value for $var_name: " user_val
            echo "$var_name=$user_val" >> "$ENV_FILE"
        done < "$ENV_EXAMPLE"
        ;;
    2)
        echo "🔍 Mode: Supplemental Configuration"
        if [ ! -f "$ENV_FILE" ]; then
            echo "ℹ️ No .env found, starting fresh."
            touch "$ENV_FILE"
        fi
        
        temp_env=".env.tmp"
        cp "$ENV_FILE" "$temp_env"
        
        while IFS= read -r line || [[ -n "$line" ]]; do
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
        done < "$ENV_EXAMPLE"
        mv "$temp_env" "$ENV_FILE"
        ;;
    3)
        echo "� Mode: Manual File Provision"
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

echo "-------------------------------------------------------"
echo "🎉 Setup complete! Reach the platform at:"
echo "🔗 https://localhost"
echo "-------------------------------------------------------"

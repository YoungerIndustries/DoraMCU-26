#!/bin/bash
# =============================================================================
# DoraMCU Raspberry Pi Setup Script
# =============================================================================
# Automated setup for Raspberry Pi 4/5 deployment.
# Detects Pi model, RAM, and configures resource limits accordingly.
#
# Usage: sudo ./scripts/pi-setup.sh
# =============================================================================

set -e

echo "=========================================="
echo " DoraMCU Raspberry Pi Setup"
echo "=========================================="
echo ""

# ── Detect Pi Model ──────────────────────────────────────────────────────────

detect_pi_model() {
    if [ -f /proc/device-tree/model ]; then
        MODEL=$(cat /proc/device-tree/model)
        echo "Detected: $MODEL"
    else
        MODEL="Unknown"
        echo "Warning: Could not detect Pi model"
    fi

    if echo "$MODEL" | grep -q "Pi 5"; then
        PI_MODEL=5
    elif echo "$MODEL" | grep -q "Pi 4"; then
        PI_MODEL=4
    elif echo "$MODEL" | grep -q "Pi 3"; then
        PI_MODEL=3
    else
        PI_MODEL=0
        echo "Warning: Not a recognized Raspberry Pi model"
    fi
}

# ── Detect Available RAM ─────────────────────────────────────────────────────

detect_ram() {
    TOTAL_RAM_KB=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    TOTAL_RAM_MB=$((TOTAL_RAM_KB / 1024))
    TOTAL_RAM_GB=$((TOTAL_RAM_MB / 1024))
    echo "Available RAM: ${TOTAL_RAM_MB}MB (${TOTAL_RAM_GB}GB)"
}

# ── Configure Resource Limits ────────────────────────────────────────────────

configure_limits() {
    echo ""
    echo "Configuring resource limits based on hardware..."

    case $TOTAL_RAM_GB in
        2)
            JVM_XMX="-Xmx256m"
            NEURON_JVM="-Xmx128m"
            MYSQL_MEMORY="256M"
            SWAP_SIZE=2
            echo "Profile: Minimal (2GB RAM)"
            ;;
        4)
            JVM_XMX="-Xmx512m"
            NEURON_JVM="-Xmx256m"
            MYSQL_MEMORY="512M"
            SWAP_SIZE=2
            echo "Profile: Standard (4GB RAM)"
            ;;
        8)
            JVM_XMX="-Xmx1G"
            NEURON_JVM="-Xmx512m"
            MYSQL_MEMORY="1G"
            SWAP_SIZE=4
            echo "Profile: High (8GB RAM)"
            ;;
        *)
            JVM_XMX="-Xmx512m"
            NEURON_JVM="-Xmx256m"
            MYSQL_MEMORY="512M"
            SWAP_SIZE=2
            echo "Profile: Default (unknown RAM)"
            ;;
    esac
}

# ── Install Docker ───────────────────────────────────────────────────────────

install_docker() {
    echo ""
    echo "Checking Docker installation..."

    if command -v docker &> /dev/null; then
        DOCKER_VERSION=$(docker --version | grep -oP '\d+\.\d+\.\d+')
        echo "Docker is already installed: $DOCKER_VERSION"
    else
        echo "Installing Docker..."
        curl -fsSL https://get.docker.com -o get-docker.sh
        sh get-docker.sh
        rm get-docker.sh

        # Add current user to docker group
        USER=${SUDO_USER:-pi}
        usermod -aG docker "$USER"
        echo "Added user '$USER' to docker group"

        # Enable Docker service
        systemctl enable docker
        systemctl start docker
        echo "Docker service enabled and started"
    fi

    # Verify Docker Compose
    if command -v docker &> /dev/null && docker compose version &> /dev/null; then
        COMPOSE_VERSION=$(docker compose version | grep -oP '\d+\.\d+\.\d+')
        echo "Docker Compose: $COMPOSE_VERSION"
    else
        echo "Error: Docker Compose not found"
        exit 1
    fi
}

# ── Configure Swap ───────────────────────────────────────────────────────────

configure_swap() {
    echo ""
    echo "Configuring swap space (${SWAP_SIZE}GB)..."

    if [ -f /swapfile ]; then
        CURRENT_SWAP=$(ls -lh /swapfile | awk '{print $5}')
        echo "Swap file already exists: $CURRENT_SWAP"
    else
        echo "Creating ${SWAP_SIZE}GB swap file..."
        fallocate -l ${SWAP_SIZE}G /swapfile
        chmod 600 /swapfile
        mkswap /swapfile
        swapon /swapfile

        # Make persistent
        if ! grep -q '/swapfile' /etc/fstab; then
            echo '/swapfile none swap sw 0 0' >> /etc/fstab
            echo "Added swap to /etc/fstab"
        fi
    fi

    echo "Swap configured"
}

# ── Configure GPU Memory Split ───────────────────────────────────────────────

configure_gpu_memory() {
    echo ""
    echo "Checking GPU memory split..."

    if command -v vcgencmd &> /dev/null; then
        GPU_MEM=$(vcgencmd get_mem gpu 2>/dev/null || echo "unknown")
        echo "Current GPU memory: $GPU_MEM"

        # Suggest reducing GPU memory for headless server
        if echo "$GPU_MEM" | grep -q "128\|256"; then
            echo "Recommendation: Reduce GPU memory to 16MB for headless server"
            echo "Run: sudo raspi-config > Advanced Options > Memory Split > 16"
        fi
    fi
}

# ── Create Environment File ──────────────────────────────────────────────────

create_env_file() {
    echo ""
    echo "Creating environment file..."

    if [ -f .env ]; then
        echo ".env file already exists, skipping"
    else
        if [ -f .env.pi.example ]; then
            cp .env.pi.example .env
            echo "Created .env from .env.pi.example"
            echo "IMPORTANT: Edit .env and set secure passwords!"
        else
            echo "Warning: .env.pi.example not found, creating basic .env"
            cat > .env << 'EOF'
# DoraMCU Raspberry Pi Environment
DB_PASSWORD=change_me_pi_secure_password
JWT_SECRET=change_me_generate_with_openssl_rand_hex_32
NEURON_SECURITY_JWT_SECRET=change_me_generate_with_openssl_rand_hex_32
JVM_XMX=-Xmx512m
DL_MODE=rest
DEBUG=
EOF
        fi
    fi
}

# ── Health Check ─────────────────────────────────────────────────────────────

health_check() {
    echo ""
    echo "=========================================="
    echo " Running Health Checks"
    echo "=========================================="

    # Wait for services to start
    echo "Waiting for services to start..."
    sleep 10

    # Check app health
    echo -n "App health: "
    if curl -sf http://localhost:8080/actuator/health &> /dev/null; then
        echo "OK"
    else
        echo "NOT READY (may still be starting)"
    fi

    # Check neuron health
    echo -n "Neuron health: "
    if curl -sf http://localhost:8090/api/health/live &> /dev/null; then
        echo "OK"
    else
        echo "NOT READY (may still be starting)"
    fi

    # Check web UI
    echo -n "Web UI: "
    if curl -sf http://localhost:80/ &> /dev/null; then
        echo "OK"
    else
        echo "NOT READY (may still be starting)"
    fi

    echo ""
    echo "Docker containers status:"
    docker compose ps 2>/dev/null || echo "No containers running"
}

# ── Main ─────────────────────────────────────────────────────────────────────

main() {
    detect_pi_model
    detect_ram
    configure_limits
    install_docker
    configure_swap
    configure_gpu_memory
    create_env_file

    echo ""
    echo "=========================================="
    echo " Setup Complete!"
    echo "=========================================="
    echo ""
    echo "Next steps:"
    echo "1. Edit .env file with secure passwords"
    echo "2. Start the stack: docker compose --profile pi up -d"
    echo "3. Check status: docker compose ps"
    echo "4. View logs: docker compose logs -f app"
    echo ""
    echo "Access the application:"
    echo "  Web UI:  http://<pi-ip>:80"
    echo "  API:     http://<pi-ip>:8080"
    echo "  Neuron:  http://<pi-ip>:8090"
    echo ""
}

main "$@"

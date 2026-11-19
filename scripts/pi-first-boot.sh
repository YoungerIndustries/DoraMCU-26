#!/bin/bash
###############################################################################
# DoraMCU First Boot Script
# 
# This script runs once on the first boot of the Raspberry Pi. It:
# 1. Installs Docker Engine if not present
# 2. Loads pre-saved Docker images from tar files
# 3. Configures system settings (swap, hostname)
# 4. Starts all DoraMCU services via Docker Compose
# 5. Sets up systemd service for auto-start on future boots
# 6. Removes itself to prevent re-running
#
# Location: /boot/firmware/first-boot.sh (or /boot/first-boot.sh on older Pi OS)
# Log: /var/log/first-boot.log
###############################################################################

set -e

# ============================================================================
# Configuration
# ============================================================================

MARKER_FILE="/var/lib/doramcu-first-boot-done"
LOG_FILE="/var/log/first-boot.log"
DORAMCU_DIR="/home/pi/doramcu"
BOOT_FIRMWARE="/boot/firmware"
BOOT_ROOT="/boot"

# Detect boot path (newer Pi OS uses /boot/firmware, older uses /boot)
if [ -d "$BOOT_FIRMWARE" ]; then
    BOOT_PATH="$BOOT_FIRMWARE"
else
    BOOT_PATH="$BOOT_ROOT"
fi

# ============================================================================
# Logging
# ============================================================================

log() {
    local level="$1"
    shift
    local message="$*"
    local timestamp
    timestamp=$(date '+%Y-%m-%d %H:%M:%S')
    echo "[$timestamp] [$level] $message" | tee -a "$LOG_FILE"
}

log_info() { log "INFO" "$@"; }
log_ok() { log "OK" "$@"; }
log_warn() { log "WARN" "$@"; }
log_error() { log "ERROR" "$@"; }

# ============================================================================
# Pre-flight Checks
# ============================================================================

check_already_ran() {
    if [ -f "$MARKER_FILE" ]; then
        log_warn "First boot already completed (marker file exists: $MARKER_FILE)"
        log_warn "Skipping first boot setup."
        exit 0
    fi
}

check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "This script must run as root. Use: sudo bash $0"
        exit 1
    fi
}

# ============================================================================
# System Configuration
# ============================================================================

configure_swap() {
    log_info "Configuring swap size..."
    
    # Set swap to 2GB for Docker operations
    if [ -f /etc/dphys-swapfile ]; then
        sed -i 's/CONF_SWAPSIZE=.*/CONF_SWAPSIZE=2048/' /etc/dphys-swapfile
        systemctl restart dphys-swapfile
        log_ok "Swap configured to 2GB"
    else
        log_warn "dphys-swapfile not found, creating swap manually..."
        fallocate -l 2G /swapfile
        chmod 600 /swapfile
        mkswap /swapfile
        swapon /swapfile
        echo '/swapfile none swap sw 0 0' >> /etc/fstab
        log_ok "2GB swapfile created"
    fi
}

configure_hostname() {
    log_info "Configuring hostname..."
    
    # Read hostname from setup info if available
    local hostname="doramcu-server"
    if [ -f "$BOOT_PATH/doramcu-setup-info.txt" ]; then
        local found
        found=$(grep -i "hostname:" "$BOOT_PATH/doramcu-setup-info.txt" | head -1 | cut -d' ' -f2-)
        if [ -n "$found" ]; then
            hostname="$found"
        fi
    fi
    
    # Set hostname
    hostnamectl set-hostname "$hostname"
    echo "127.0.0.1 $hostname" >> /etc/hosts
    log_ok "Hostname set to: $hostname"
}

# ============================================================================
# Docker Installation
# ============================================================================

install_docker() {
    log_info "Checking Docker installation..."
    
    if command -v docker &> /dev/null; then
        log_ok "Docker is already installed: $(docker --version)"
        return 0
    fi
    
    log_info "Installing Docker Engine..."
    
    # Use the official Docker installation script
    curl -fsSL https://get.docker.com -o /tmp/get-docker.sh
    sh /tmp/get-docker.sh
    rm /tmp/get-docker.sh
    
    # Enable Docker service
    systemctl enable docker
    systemctl start docker
    
    # Add pi user to docker group
    usermod -aG docker pi
    
    log_ok "Docker installed: $(docker --version)"
}

install_docker_compose() {
    log_info "Checking Docker Compose..."
    
    if docker compose version &> /dev/null; then
        log_ok "Docker Compose is available: $(docker compose version)"
        return 0
    fi
    
    log_warn "Docker Compose plugin not found, installing..."
    
    # Install the Docker Compose plugin
    local compose_version="v2.24.0"
    local compose_url="https://github.com/docker/compose/releases/download/${compose_version}/docker-compose-linux-aarch64"
    
    curl -L "$compose_url" -o /usr/local/lib/docker/cli-plugins/docker-compose
    chmod +x /usr/local/lib/docker/cli-plugins/docker-compose
    
    log_ok "Docker Compose installed: $(docker compose version)"
}

# ============================================================================
# Docker Image Loading
# ============================================================================

load_docker_images() {
    log_info "Loading Docker images..."
    
    local image_tar="$BOOT_PATH/doramcu-images.tar"
    
    if [ ! -f "$image_tar" ]; then
        log_warn "No Docker images tar file found at: $image_tar"
        log_warn "Services will need to be built on the Pi (this will take 2-4 hours)"
        return 1
    fi
    
    local size_gb
    size_gb=$(du -g "$image_tar" | cut -f1)
    log_info "Loading images from: $image_tar (${size_gb}GB)"
    
    # Load all images
    docker load < "$image_tar"
    
    if [ $? -eq 0 ]; then
        log_ok "Docker images loaded successfully"
        docker images | grep doramcu | while read line; do
            log_ok "  $line"
        done
    else
        log_error "Failed to load Docker images"
        return 1
    fi
    
    return 0
}

# ============================================================================
# DoraMCU Setup
# ============================================================================

setup_doramcu_directory() {
    log_info "Setting up DoraMCU directory..."
    
    # Copy project files from boot partition to home directory
    local boot_doramcu="$BOOT_PATH/doramcu"
    
    if [ -d "$boot_doramcu" ]; then
        log_info "Copying DoraMCU files from boot partition..."
        
        # Create directory
        mkdir -p "$DORAMCU_DIR"
        
        # Copy files
        cp -r "$boot_doramcu/"* "$DORAMCU_DIR/" 2>/dev/null || true
        cp -r "$boot_doramcu"/.* "$DORAMCU_DIR/" 2>/dev/null || true
        
        log_ok "DoraMCU files copied to: $DORAMCU_DIR"
    else
        log_warn "No DoraMCU directory found on boot partition"
        log_warn "Creating empty directory at: $DORAMCU_DIR"
        mkdir -p "$DORAMCU_DIR"
    fi
    
    # Set ownership
    chown -R pi:pi "$DORAMCU_DIR"
    
    # Generate JWT secret if .env doesn't have one
    if [ -f "$DORAMCU_DIR/.env" ]; then
        if grep -q 'NEURON_SECURITY_JWT_SECRET=\$(openssl' "$DORAMCU_DIR/.env"; then
            log_info "Generating JWT secret..."
            local jwt_secret
            jwt_secret=$(openssl rand -hex 32)
            sed -i "s/NEURON_SECURITY_JWT_SECRET=\$(openssl rand -hex 32)/NEURON_SECURITY_JWT_SECRET=${jwt_secret}/" "$DORAMCU_DIR/.env"
            log_ok "JWT secret generated"
        fi
    else
        log_warn "No .env file found, creating default..."
        cat > "$DORAMCU_DIR/.env" << 'EOF'
# DoraMCU Environment Variables
NEURON_SECURITY_JWT_SECRET=CHANGE_ME_TO_RANDOM_SECRET
MYSQL_ROOT_PASSWORD=doramcu_root_password_change_me
MYSQL_DATABASE=doramcu
MYSQL_USER=doramcu
MYSQL_PASSWORD=doramcu_password_change_me
DL_MODE=rest
EOF
        chown pi:pi "$DORAMCU_DIR/.env"
    fi
}

# ============================================================================
# Service Setup
# ============================================================================

setup_systemd_service() {
    log_info "Setting up systemd service..."
    
    # Copy service file from boot partition if available
    local service_file="$BOOT_PATH/doramcu.service"
    if [ -f "$service_file" ]; then
        cp "$service_file" /etc/systemd/system/doramcu.service
    else
        # Create default service file
        cat > /etc/systemd/system/doramcu.service << 'EOF'
[Unit]
Description=DoraMCU Game Server
After=docker.service network-online.target
Requires=docker.service
Wants=network-online.target

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/pi/doramcu
ExecStart=/usr/local/bin/docker compose --profile pi up -d
ExecStop=/usr/local/bin/docker compose --profile pi down
Restart=on-failure
RestartSec=5
StandardOutput=journal
StandardError=journal
Environment=COMPOSE_PROJECT_NAME=doramcu

[Install]
WantedBy=multi-user.target
EOF
    fi
    
    # Reload systemd
    systemctl daemon-reload
    
    # Enable the service
    systemctl enable doramcu.service
    
    log_ok "Systemd service configured and enabled"
}

setup_first_boot_service_cleanup() {
    log_info "Cleaning up first-boot service..."
    
    # Disable the first-boot service if it exists
    if systemctl list-unit-files | grep -q "first-boot.service"; then
        systemctl disable first-boot.service
        systemctl stop first-boot.service
        rm -f /etc/systemd/system/first-boot.service
        systemctl daemon-reload
        log_ok "First-boot service removed"
    fi
}

# ============================================================================
# Start Services
# ============================================================================

start_services() {
    log_info "Starting DoraMCU services..."
    
    cd "$DORAMCU_DIR"
    
    # Check if docker-compose file exists
    if [ ! -f "docker-compose.yml" ]; then
        log_error "Docker Compose file not found in: $DORAMCU_DIR"
        log_error "Please ensure docker-compose.yml is present"
        return 1
    fi
    
    # Start services with Pi profile
    docker compose --profile pi up -d
    
    if [ $? -eq 0 ]; then
        log_ok "All services started successfully!"
        
        # Show service status
        log_info "Service status:"
        docker compose --profile pi ps | while read line; do
            log_ok "  $line"
        done
    else
        log_error "Failed to start services"
        return 1
    fi
}

# ============================================================================
# Cleanup
# ============================================================================

cleanup() {
    log_info "Cleaning up..."
    
    # Remove first-boot script from boot partition
    rm -f "$BOOT_PATH/first-boot.sh"
    
    # Remove setup info
    rm -f "$BOOT_PATH/doramcu-setup-info.txt"
    
    # Remove Docker images tar (save space)
    local image_tar="$BOOT_PATH/doramcu-images.tar"
    if [ -f "$image_tar" ]; then
        log_info "Removing Docker images tar to save space..."
        rm -f "$image_tar"
        log_ok "Docker images tar removed"
    fi
    
    # Remove DoraMCU directory from boot partition
    rm -rf "$BOOT_PATH/doramcu"
    
    # Create marker file
    touch "$MARKER_FILE"
    echo "First boot completed at: $(date)" > "$MARKER_FILE"
    
    log_ok "Cleanup complete"
}

show_summary() {
    log_info "=============================================="
    log_ok "DoraMCU first boot setup complete!"
    log_info "=============================================="
    log_info ""
    log_info "Server is now running at:"
    log_info "  http://$(hostname).local"
    log_info "  http://$(hostname -I | awk '{print $1}')"
    log_info ""
    log_info "Services:"
    log_info "  - nginx:    port 80 (web interface)"
    log_info "  - neuron:   port 8090 (player gateway)"
    log_info "  - app:      port 8080 (game server)"
    log_info "  - mysql:    port 3306 (database)"
    log_info "  - redis:    port 6379 (cache)"
    log_info "  - dl:       port 8083 (AI service)"
    log_info ""
    log_info "Monitoring:"
    log_info "  - docker compose ps"
    log_info "  - docker compose logs -f"
    log_info "  - http://<ip>:8090/api/v1/neuron/edge"
    log_info ""
    log_info "Log file: $LOG_FILE"
    log_info "=============================================="
}

# ============================================================================
# Main Execution
# ============================================================================

main() {
    # Initialize log file
    touch "$LOG_FILE"
    
    log_info "=============================================="
    log_info "DoraMCU First Boot Setup"
    log_info "=============================================="
    log_info "Date: $(date)"
    log_info "Hostname: $(hostname)"
    log_info "Kernel: $(uname -r)"
    log_info "Architecture: $(uname -m)"
    log_info "Memory: $(free -h | grep Mem | awk '{print $2}')"
    log_info "=============================================="
    log_info ""
    
    # Pre-flight checks
    check_already_ran
    check_root
    
    # System configuration
    configure_swap
    configure_hostname
    
    # Docker installation
    install_docker
    install_docker_compose
    
    # Load Docker images
    load_docker_images || true
    
    # Setup DoraMCU
    setup_doramcu_directory
    
    # Setup systemd service
    setup_systemd_service
    setup_first_boot_service_cleanup
    
    # Start services
    start_services
    
    # Cleanup
    cleanup
    
    # Show summary
    show_summary
    
    log_ok "First boot setup finished successfully!"
}

# Run main function
main "$@"

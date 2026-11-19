#!/bin/bash
###############################################################################
# DoraMCU Raspberry Pi Test Runner
#
# Comprehensive validation script for the Raspberry Pi ARM64 dedicated game
# server deployment. This script checks all critical components and reports
# pass/fail results with a clear summary.
#
# Usage:
#   sudo bash pi-test-runner.sh          # Run all tests
#   bash pi-test-runner.sh               # Run without sudo (some tests may fail)
#   bash pi-test-runner.sh --help        # Show help
#
# Tests performed:
#   1. Docker installation and service status
#   2. All 6 Docker containers running (nginx, neuron, app, MySQL, Redis, DL)
#   3. Network connectivity and web interface access
#   4. Neuron edge profile activation
#   5. Memory usage and resource constraints
#   6. systemd service status and auto-start configuration
#   7. Disk space and swap configuration
#
# Author: DoraMCU Project
# Date: 2026-11-17
###############################################################################

set -euo pipefail

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color
BOLD='\033[1m'

# Counters
PASS_COUNT=0
FAIL_COUNT=0
WARN_COUNT=0
TOTAL_TESTS=0

# Configuration
EXPECTED_SERVICES=("nginx" "neuron" "app" "mysql" "redis" "dl")
EXPECTED_SERVICE_COUNT=${#EXPECTED_SERVICES[@]}
PI_PROJECT_DIR="/home/pi/doramcu"
NEURON_PORT=8090
APP_PORT=8080
NGINX_PORT=80
DL_PORT=8083

# Helper functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
    ((PASS_COUNT++))
    ((TOTAL_TESTS++))
}

log_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
    ((FAIL_COUNT++))
    ((TOTAL_TESTS++))
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
    ((WARN_COUNT++))
    ((TOTAL_TESTS++))
}

log_section() {
    echo ""
    echo -e "${BOLD}${BLUE}========================================${NC}"
    echo -e "${BOLD}${BLUE} $1${NC}"
    echo -e "${BOLD}${BLUE}========================================${NC}"
}

separator() {
    echo -e "${BLUE}----------------------------------------${NC}"
}

# Check if running as root
check_root() {
    if [[ $EUID -ne 0 ]]; then
        log_warn "Not running as root. Some tests may fail due to permissions."
        echo ""
    fi
}

# Test 1: Docker installation
test_docker_installation() {
    log_section "1. Docker Installation"

    # Check Docker is installed
    if command -v docker &> /dev/null; then
        local docker_version
        docker_version=$(docker --version 2>/dev/null | grep -oP '\d+\.\d+\.\d+' || echo "unknown")
        log_pass "Docker is installed (version: $docker_version)"
    else
        log_fail "Docker is not installed"
        return 1
    fi

    # Check Docker service is running
    if systemctl is-active --quiet docker 2>/dev/null || service docker status &>/dev/null 2>&1; then
        log_pass "Docker service is running"
    else
        log_fail "Docker service is not running"
        return 1
    fi

    # Check Docker Compose plugin
    if docker compose version &>/dev/null 2>&1; then
        local compose_version
        compose_version=$(docker compose version --short 2>/dev/null || echo "unknown")
        log_pass "Docker Compose plugin is installed (version: $compose_version)"
    else
        log_fail "Docker Compose plugin is not installed"
    fi

    # Check Docker daemon can respond
    if docker info &>/dev/null 2>&1; then
        log_pass "Docker daemon is responsive"
    else
        log_fail "Docker daemon is not responsive"
    fi
}

# Test 2: Docker containers running
test_docker_containers() {
    log_section "2. Docker Containers"

    # Get all running containers
    local running_containers
    running_containers=$(docker ps --format '{{.Names}}' 2>/dev/null || echo "")

    if [[ -z "$running_containers" ]]; then
        log_fail "No Docker containers are running"
        return 1
    fi

    # Check each expected service
    local found_count=0
    for service in "${EXPECTED_SERVICES[@]}"; do
        # Look for container names containing the service name
        if echo "$running_containers" | grep -qi "$service"; then
            local container_name
            container_name=$(echo "$running_containers" | grep -i "$service" | head -1)
            local container_status
            container_status=$(docker inspect --format='{{.State.Status}}' "$container_name" 2>/dev/null || echo "unknown")
            log_pass "Service '$service' is running (container: $container_name, status: $container_status)"
            ((found_count++))
        else
            log_fail "Service '$service' is not running"
        fi
    done

    # Summary
    separator
    if [[ $found_count -eq $EXPECTED_SERVICE_COUNT ]]; then
        log_pass "All $EXPECTED_SERVICE_COUNT services are running"
    else
        log_fail "Only $found_count of $EXPECTED_SERVICE_COUNT services are running"
    fi
}

# Test 3: Network connectivity
test_network_connectivity() {
    log_section "3. Network Connectivity"

    # Get Pi's IP address
    local pi_ip
    pi_ip=$(hostname -I 2>/dev/null | awk '{print $1}' || echo "unknown")
    log_info "Raspberry Pi IP address: $pi_ip"

    # Test localhost connectivity
    if curl -s --connect-timeout 5 "http://localhost:$NGINX_PORT" &>/dev/null 2>&1; then
        log_pass "Nginx is accessible on localhost port $NGINX_PORT"
    else
        log_fail "Nginx is not accessible on localhost port $NGINX_PORT"
    fi

    # Test neuron endpoint
    if curl -s --connect-timeout 5 "http://localhost:$NEURON_PORT" &>/dev/null 2>&1; then
        log_pass "Neuron is accessible on localhost port $NEURON_PORT"
    else
        log_fail "Neuron is not accessible on localhost port $NEURON_PORT"
    fi

    # Test app endpoint
    if curl -s --connect-timeout 5 "http://localhost:$APP_PORT" &>/dev/null 2>&1; then
        log_pass "App is accessible on localhost port $APP_PORT"
    else
        log_fail "App is not accessible on localhost port $APP_PORT"
    fi

    # Test DL endpoint
    if curl -s --connect-timeout 5 "http://localhost:$DL_PORT" &>/dev/null 2>&1; then
        log_pass "DL service is accessible on localhost port $DL_PORT"
    else
        log_fail "DL service is not accessible on localhost port $DL_PORT"
    fi

    # Test network interface
    separator
    if command -v ip &>/dev/null; then
        local interfaces
        interfaces=$(ip -brief addr show 2>/dev/null | grep -v lo || echo "unknown")
        log_info "Network interfaces (excluding loopback):"
        echo "$interfaces" | while read -r line; do
            echo "  $line"
        done
    fi

    # Check Wi-Fi status
    if command -v iwgeth &>/dev/null || command -v iwconfig &>/dev/null; then
        if iwgeth 2>/dev/null | grep -q "ESSID"; then
            local wifi_essid
            wifi_essid=$(iwgeth 2>/dev/null | grep -oP 'ESSID:"\K[^"]+' || echo "unknown")
            log_pass "Wi-Fi is connected to: $wifi_essid"
        else
            log_warn "Wi-Fi status could not be determined"
        fi
    fi
}

# Test 4: Neuron edge profile
test_neuron_edge_profile() {
    log_section "4. Neuron Edge Profile"

    # Test edge endpoint
    local edge_response
    if edge_response=$(curl -s --connect-timeout 5 "http://localhost:$NEURON_PORT/api/v1/neuron/edge" 2>/dev/null); then
        log_pass "Neuron edge endpoint is accessible"

        # Parse the response for edge profile
        if echo "$edge_response" | grep -qi "edge\|EDGE"; then
            log_pass "Neuron edge profile appears to be active"
        else
            log_warn "Edge profile status unclear from response"
        fi

        # Show the response (truncated if too long)
        separator
        log_info "Edge endpoint response:"
        echo "$edge_response" | head -20
        if [[ ${#edge_response} -gt 500 ]]; then
            echo "  ... (truncated)"
        fi
    else
        log_fail "Neuron edge endpoint is not accessible"
        log_info "This may be normal if the edge endpoint requires authentication"
    fi

    # Check neuron logs for edge configuration
    separator
    if docker logs $(docker ps --format '{{.Names}}' 2>/dev/null | grep -i neuron | head -1) 2>/dev/null | grep -qi "edge\|EDGE"; then
        log_pass "Neuron logs mention edge configuration"
    else
        log_warn "Could not find edge configuration in neuron logs"
    fi
}

# Test 5: Memory usage
test_memory_usage() {
    log_section "5. Memory Usage"

    # Get total memory
    local total_mem_kb
    total_mem_kb=$(grep MemTotal /proc/meminfo | awk '{print $2}')
    local total_mem_mb=$((total_mem_kb / 1024))
    local total_mem_gb=$((total_mem_mb / 1024))

    # Get available memory
    local available_mem_kb
    available_mem_kb=$(grep MemAvailable /proc/meminfo | awk '{print $2}')
    local available_mem_mb=$((available_mem_kb / 1024))

    # Get used memory
    local used_mem_mb=$((total_mem_mb - available_mem_mb))

    log_info "Total memory: ${total_mem_mb}MB (${total_mem_gb}GB)"
    log_info "Used memory: ${used_mem_mb}MB"
    log_info "Available memory: ${available_mem_mb}MB"

    # Check if memory is sufficient
    if [[ $total_mem_mb -ge 4096 ]]; then
        log_pass "Total memory is sufficient (≥4GB)"
    else
        log_fail "Total memory is low (<4GB). 8GB recommended for all 6 services."
    fi

    # Check if available memory is sufficient
    if [[ $available_mem_mb -ge 512 ]]; then
        log_pass "Available memory is sufficient (≥512MB)"
    else
        log_warn "Available memory is low (<512MB). Services may experience OOM kills."
    fi

    # Docker container memory usage
    separator
    log_info "Docker container memory usage:"
    if docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}" 2>/dev/null; then
        log_pass "Docker stats retrieved successfully"
    else
        log_warn "Could not retrieve Docker stats"
    fi

    # Check swap
    separator
    local swap_total_kb
    swap_total_kb=$(grep SwapTotal /proc/meminfo | awk '{print $2}')
    local swap_total_mb=$((swap_total_kb / 1024))

    if [[ $swap_total_mb -gt 0 ]]; then
        log_pass "Swap is configured (${swap_total_mb}MB)"
    else
        log_warn "No swap configured. Recommended: 2GB for stability."
    fi
}

# Test 6: systemd service
test_systemd_service() {
    log_section "6. systemd Service Configuration"

    # Check if doramcu.service exists
    if systemctl list-unit-files | grep -q "doramcu.service"; then
        log_pass "doramcu.service is installed"
    else
        log_fail "doramcu.service is not installed"
        log_info "Auto-start may not work on reboot"
        return 1
    fi

    # Check service status
    if systemctl is-active --quiet doramcu.service 2>/dev/null; then
        log_pass "doramcu.service is active"
    else
        log_warn "doramcu.service is not active"
    fi

    # Check if service is enabled for auto-start
    if systemctl is-enabled doramcu.service &>/dev/null 2>&1; then
        log_pass "doramcu.service is enabled for auto-start"
    else
        log_fail "doramcu.service is not enabled for auto-start"
        log_info "Enable with: sudo systemctl enable doramcu.service"
    fi

    # Show service status
    separator
    log_info "Service status:"
    systemctl status doramcu.service --no-pager 2>/dev/null | head -15 || true
}

# Test 7: Disk space and swap
test_disk_space() {
    log_section "7. Disk Space and Storage"

    # Check disk space
    local disk_usage
    disk_usage=$(df -h / 2>/dev/null | tail -1)
    local available_space
    available_space=$(echo "$disk_usage" | awk '{print $4}')
    local use_percent
    use_percent=$(echo "$disk_usage" | awk '{print $5}')

    log_info "Root partition usage: $use_percent used, $available_space available"

    if [[ "$available_space" =~ ^[0-9]+G$ ]]; then
        local space_gb
        space_gb=$(echo "$available_space" | grep -oP '\d+')
        if [[ $space_gb -ge 10 ]]; then
            log_pass "Sufficient disk space available (≥10GB)"
        else
            log_warn "Low disk space (<10GB). Docker images and logs may fill the drive."
        fi
    else
        log_warn "Could not parse available disk space"
    fi

    # Check Docker disk usage
    separator
    log_info "Docker disk usage:"
    if docker system df 2>/dev/null; then
        log_pass "Docker disk usage retrieved"
    else
        log_warn "Could not retrieve Docker disk usage"
    fi

    # Check swap configuration
    separator
    local swap_total_kb
    swap_total_kb=$(grep SwapTotal /proc/meminfo | awk '{print $2}')
    local swap_total_mb=$((swap_total_kb / 1024))

    if [[ $swap_total_mb -ge 2048 ]]; then
        log_pass "Swap is configured correctly (≥2GB: ${swap_total_mb}MB)"
    elif [[ $swap_total_mb -gt 0 ]]; then
        log_warn "Swap is configured but small (${swap_total_mb}MB). Recommended: 2GB"
    else
        log_fail "No swap configured. This may cause OOM kills under load."
        log_info "Configure swap: sudo dphys-swapfile swapoff && sudo sed -i 's/CONF_SWAPSIZE=.*/CONF_SWAPSIZE=2048/' /etc/dphys-swapfile && sudo dphys-swapfile setup && sudo dphys-swapfile swapon"
    fi
}

# Test 8: Project files
test_project_files() {
    log_section "8. Project Files"

    # Check if project directory exists
    if [[ -d "$PI_PROJECT_DIR" ]]; then
        log_pass "Project directory exists: $PI_PROJECT_DIR"
    else
        log_fail "Project directory not found: $PI_PROJECT_DIR"
        log_info "Looking for alternative locations..."

        # Search for docker-compose.yml
        if find /home -name "docker-compose.yml" -type f 2>/dev/null | head -1 | grep -q .; then
            local found_path
            found_path=$(find /home -name "docker-compose.yml" -type f 2>/dev/null | head -1)
            log_info "Found docker-compose.yml at: $found_path"
            PI_PROJECT_DIR=$(dirname "$found_path")
        fi
    fi

    # Check for required files
    separator
    local required_files=("docker-compose.yml" ".env")
    for file in "${required_files[@]}"; do
        if [[ -f "$PI_PROJECT_DIR/$file" ]]; then
            log_pass "File exists: $file"
        else
            log_fail "File missing: $file"
        fi
    done

    # Check .env file has required variables
    separator
    if [[ -f "$PI_PROJECT_DIR/.env" ]]; then
        log_info "Checking .env file contents..."
        local env_vars=("MYSQL_ROOT_PASSWORD" "MYSQL_PASSWORD" "JWT_SECRET")
        for var in "${env_vars[@]}"; do
            if grep -q "^${var}=" "$PI_PROJECT_DIR/.env" 2>/dev/null; then
                log_pass ".env contains: $var"
            else
                log_warn ".env missing: $var"
            fi
        done
    fi
}

# Test 9: Docker Compose configuration
test_docker_compose() {
    log_section "9. Docker Compose Configuration"

    # Check if docker-compose.yml exists
    if [[ -f "$PI_PROJECT_DIR/docker-compose.yml" ]]; then
        log_pass "docker-compose.yml exists"
    else
        log_fail "docker-compose.yml not found"
        return 1
    fi

    # Validate compose configuration with Pi profile
    separator
    log_info "Validating Docker Compose configuration..."
    if cd "$PI_PROJECT_DIR" && docker compose --profile pi config &>/dev/null 2>&1; then
        log_pass "Docker Compose configuration is valid"
    else
        log_fail "Docker Compose configuration has errors"
        log_info "Run 'docker compose --profile pi config' to see errors"
    fi
}

# Test 10: Hardware information
test_hardware_info() {
    log_section "10. Hardware Information"

    # Get Pi model
    if [[ -f /proc/device-tree/model ]]; then
        local pi_model
        pi_model=$(cat /proc/device-tree/model 2>/dev/null || echo "unknown")
        log_info "Raspberry Pi model: $pi_model"

        if echo "$pi_model" | grep -qi "Pi 5"; then
            log_pass "Running on Raspberry Pi 5 (recommended)"
        elif echo "$pi_model" | grep -qi "Pi 4"; then
            log_pass "Running on Raspberry Pi 4"
        else
            log_warn "Not running on Raspberry Pi 4/5. Performance may vary."
        fi
    else
        log_warn "Could not determine Pi model"
    fi

    # Get CPU information
    separator
    local cpu_cores
    cpu_cores=$(nproc 2>/dev/null || echo "unknown")
    log_info "CPU cores: $cpu_cores"

    if [[ "$cpu_cores" =~ ^[0-9]+$ ]] && [[ $cpu_cores -ge 4 ]]; then
        log_pass "CPU cores sufficient (≥4)"
    else
        log_warn "CPU cores may be insufficient (<4)"
    fi

    # Get architecture
    local arch
    arch=$(uname -m 2>/dev/null || echo "unknown")
    log_info "Architecture: $arch"

    if [[ "$arch" == "aarch64" ]]; then
        log_pass "Running on ARM64 (aarch64)"
    else
        log_fail "Not running on ARM64. Expected: aarch64, Got: $arch"
    fi
}

# Print summary
print_summary() {
    log_section "TEST SUMMARY"

    echo ""
    echo -e "${BOLD}Total tests: $TOTAL_TESTS${NC}"
    echo -e "${GREEN}Passed: $PASS_COUNT${NC}"
    echo -e "${RED}Failed: $FAIL_COUNT${NC}"
    echo -e "${YELLOW}Warnings: $WARN_COUNT${NC}"
    echo ""

    if [[ $FAIL_COUNT -eq 0 ]]; then
        echo -e "${GREEN}${BOLD}✓ All tests passed! Your DoraMCU Raspberry Pi server is ready.${NC}"
        echo ""
        echo -e "${BOLD}Next steps:${NC}"
        echo "  1. Access the server at: http://$(hostname -I | awk '{print $1}')"
        echo "  2. Monitor services: docker ps"
        echo "  3. View logs: docker compose --profile pi logs -f"
        echo "  4. Edge monitoring: curl http://localhost:8090/api/v1/neuron/edge"
    else
        echo -e "${RED}${BOLD}✗ $FAIL_COUNT test(s) failed. Please review the failures above.${NC}"
        echo ""
        echo -e "${BOLD}Common fixes:${NC}"
        echo "  - Docker not running: sudo systemctl start docker"
        echo "  - Services not started: cd /home/pi/doramcu && docker compose --profile pi up -d"
        echo "  - No swap: sudo dphys-swapfile setup && sudo dphys-swapfile swapon"
        echo "  - Service not enabled: sudo systemctl enable doramcu.service"
    fi

    echo ""
    echo -e "${BOLD}Test completed at: $(date)${NC}"
}

# Show help
show_help() {
    echo "DoraMCU Raspberry Pi Test Runner"
    echo ""
    echo "Usage: sudo bash pi-test-runner.sh [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --help, -h       Show this help message"
    echo "  --quick, -q      Run only critical tests (Docker, containers, network)"
    echo "  --verbose, -v    Show detailed output for all tests"
    echo ""
    echo "Tests performed:"
    echo "  1. Docker installation and service status"
    echo "  2. All 6 Docker containers running"
    echo "  3. Network connectivity and web interface access"
    echo "  4. Neuron edge profile activation"
    echo "  5. Memory usage and resource constraints"
    echo "  6. systemd service status and auto-start"
    echo "  7. Disk space and swap configuration"
    echo "  8. Project files and configuration"
    echo "  9. Docker Compose configuration validation"
    echo "  10. Hardware information"
    echo ""
    echo "Exit codes:"
    echo "  0  All tests passed"
    echo "  1  One or more tests failed"
}

# Main execution
main() {
    echo ""
    echo -e "${BOLD}${BLUE}╔═══════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BOLD}${BLUE}║          DoraMCU Raspberry Pi Test Runner                 ║${NC}"
    echo -e "${BOLD}${BLUE}║          Comprehensive Deployment Validation              ║${NC}"
    echo -e "${BOLD}${BLUE}╚═══════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "${BOLD}Started at: $(date)${NC}"
    echo ""

    # Parse arguments
    local mode="full"
    for arg in "$@"; do
        case $arg in
            --help|-h)
                show_help
                exit 0
                ;;
            --quick|-q)
                mode="quick"
                ;;
            --verbose|-v)
                set -x
                ;;
            *)
                echo "Unknown option: $arg"
                show_help
                exit 1
                ;;
        esac
    done

    # Check root
    check_root

    # Run tests based on mode
    if [[ "$mode" == "quick" ]]; then
        log_info "Running quick tests only..."
        test_docker_installation
        test_docker_containers
        test_network_connectivity
    else
        log_info "Running all tests..."
        test_docker_installation
        test_docker_containers
        test_network_connectivity
        test_neuron_edge_profile
        test_memory_usage
        test_systemd_service
        test_disk_space
        test_project_files
        test_docker_compose
        test_hardware_info
    fi

    # Print summary
    print_summary

    # Exit with appropriate code
    if [[ $FAIL_COUNT -gt 0 ]]; then
        exit 1
    else
        exit 0
    fi
}

# Run main function
main "$@"

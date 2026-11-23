#!/bin/bash
# =============================================================================
# Deploy Dora News Network to Pi Brain Stem
# =============================================================================
# Deploys the news network JAR and systemd service to the Pi.
# Usage: sudo bash deploy-news-network.sh
# =============================================================================

set -e

NEWS_NETWORK_DIR="/opt/doramcu/news-network"
SERVICE_FILE="pi-doramcu-news-network.service"
JAR_FILE="dora-news-network-0.6.5.jar"

echo "=========================================="
echo " Dora News Network Pi Deployment"
echo "=========================================="
echo ""

# ── Create directory ─────────────────────────────────────────────────────────

echo "Creating news network directory..."
sudo mkdir -p "$NEWS_NETWORK_DIR"
sudo chown doramcu:doramcu "$NEWS_NETWORK_DIR"

# ── Copy JAR ─────────────────────────────────────────────────────────────────

echo "Copying news network JAR..."
if [ -f "/tmp/$JAR_FILE" ]; then
    sudo cp "/tmp/$JAR_FILE" "$NEWS_NETWORK_DIR/dora-news-network.jar"
    sudo chown doramcu:doramcu "$NEWS_NETWORK_DIR/dora-news-network.jar"
    echo "JAR copied from /tmp/$JAR_FILE"
else
    echo "Error: /tmp/$JAR_FILE not found"
    echo "Copy the JAR to /tmp/ first, then run this script."
    exit 1
fi

# ── Install systemd service ──────────────────────────────────────────────────

echo "Installing systemd service..."
if [ -f "/tmp/$SERVICE_FILE" ]; then
    sudo cp "/tmp/$SERVICE_FILE" /etc/systemd/system/doramcu-news-network.service
    echo "Service file installed"
else
    echo "Error: /tmp/$SERVICE_FILE not found"
    echo "Copy the service file to /tmp/ first, then run this script."
    exit 1
fi

# ── Reload systemd and start ─────────────────────────────────────────────────

echo "Reloading systemd daemon..."
sudo systemctl daemon-reload

echo "Enabling news network service..."
sudo systemctl enable doramcu-news-network

echo "Starting news network service..."
sudo systemctl start doramcu-news-network

# ── Verify ───────────────────────────────────────────────────────────────────

echo ""
echo "Waiting for service to start..."
sleep 5

echo ""
echo "Service status:"
sudo systemctl status doramcu-news-network --no-pager || true

echo ""
echo "Health check:"
curl -sf http://localhost:8084/actuator/health && echo " OK" || echo " NOT READY (may still be starting)"

echo ""
echo "=========================================="
echo " Deployment Complete!"
echo "=========================================="
echo ""
echo "Access the news network:"
echo "  Web UI:  http://<pi-ip>:8084"
echo "  API:     http://<pi-ip>:8084/api/news/feed"
echo ""
echo "Logs: journalctl -u doramcu-news-network -f"
echo ""

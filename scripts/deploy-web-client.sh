#!/bin/bash
# Deploy web client files to Pi Brain Stem
# Usage: ./scripts/deploy-web-client.sh
#
# This script uses the HTTP tunnel (port 9000) for reliable Pi command execution.
# SSH is unreliable due to keepalive issues — see STARTING_POINT.md section 0.

set -e

PI_HOST="192.168.1.111"
PI_TUNNEL="http://${PI_HOST}:9000/exec"
PI_WEB_DIR="/opt/doramcu/web"
LOCAL_WEB_DIR="dora-web"

echo "=== Deploying Web Client to Pi ==="
echo "Target: ${PI_WEB_DIR} via tunnel at ${PI_TUNNEL}"
echo ""

# Helper function to run commands on Pi via tunnel
pi_run() {
  local cmd="$1"
  local json="{\"command\":\"${cmd}\",\"timeout\":10000}"
  curl -s -X POST "${PI_TUNNEL}" -H "Content-Type: application/json" -d "${json}"
}

# Helper function to upload a file via tunnel (base64 encoded)
upload_file() {
  local local_file="$1"
  local remote_path="$2"
  local content
  content=$(base64 -w 0 "$local_file")
  local cmd="echo '${content}' | base64 -d > ${remote_path}; chmod 644 ${remote_path}"
  pi_run "$cmd" > /dev/null
  echo "  ✓ $(basename $local_file)"
}

# Create remote directories
echo "Creating directories..."
pi_run "mkdir -p ${PI_WEB_DIR}/js/views ${PI_WEB_DIR}/game/js/views ${PI_WEB_DIR}/game/css" > /dev/null

# Deploy all JS files
echo "Deploying JS files..."
for f in "${LOCAL_WEB_DIR}"/js/*.js; do
  [ -f "$f" ] && upload_file "$f" "${PI_WEB_DIR}/js/$(basename $f)"
done

# Deploy game files
echo "Deploying game files..."
for f in "${LOCAL_WEB_DIR}"/game/*.js "${LOCAL_WEB_DIR}"/game/*.html; do
  [ -f "$f" ] && upload_file "$f" "${PI_WEB_DIR}/game/$(basename $f)"
done

# Deploy game views
echo "Deploying game views..."
for f in "${LOCAL_WEB_DIR}"/game/js/views/*.js; do
  [ -f "$f" ] && upload_file "$f" "${PI_WEB_DIR}/game/js/views/$(basename $f)"
done

# Deploy game CSS
echo "Deploying game CSS..."
for f in "${LOCAL_WEB_DIR}"/game/css/*.css; do
  [ -f "$f" ] && upload_file "$f" "${PI_WEB_DIR}/game/css/$(basename $f)"
done

# Deploy root files
echo "Deploying root files..."
[ -f "${LOCAL_WEB_DIR}/sw.js" ] && upload_file "${LOCAL_WEB_DIR}/sw.js" "${PI_WEB_DIR}/sw.js"
[ -f "${LOCAL_WEB_DIR}/manifest.json" ] && upload_file "${LOCAL_WEB_DIR}/manifest.json" "${PI_WEB_DIR}/manifest.json"

echo ""
echo "=== Deployment Complete ==="
echo "Verifying deployment..."

# Verify key files
for file in "js/dtos.js" "js/api-client.js" "game/index.html" "game/game-app.js" "game/css/game.css"; do
  result=$(pi_run "test -f ${PI_WEB_DIR}/${file} && echo OK || echo MISSING")
  if echo "$result" | grep -q "OK"; then
    echo "  ✓ ${file}"
  else
    echo "  ✗ ${file} — MISSING!"
  fi
done

echo ""
echo "Web client available at: http://${PI_HOST}:8080/game/index.html"

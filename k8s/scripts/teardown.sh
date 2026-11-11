#!/bin/bash
# DoraMCU Kubernetes Teardown Script
# Usage: ./teardown.sh

set -e

NAMESPACE="doramcu"
K8S_DIR="$(cd "$(dirname "$0")/.." && pwd)/k8s"

echo "============================================"
echo "  DoraMCU Neuron Network — Teardown"
echo "============================================"
echo "Namespace: ${NAMESPACE}"
echo ""

# Confirm
read -p "Delete all resources in namespace '${NAMESPACE}'? (y/N) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
  echo "Aborted."
  exit 0
fi

# Delete all resources
echo "▶ Deleting namespace '${NAMESPACE}' and all resources..."
kubectl delete namespace ${NAMESPACE} --ignore-not-found=true

echo ""
echo "✓ Teardown complete!"
echo "  Namespace '${NAMESPACE}' and all resources have been deleted."

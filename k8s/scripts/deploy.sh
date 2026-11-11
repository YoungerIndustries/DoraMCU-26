#!/bin/bash
# DoraMCU Kubernetes Deployment Script
# Usage: ./deploy.sh [dev|staging|production]

set -e

ENVIRONMENT="${1:-dev}"
NAMESPACE="doramcu"
K8S_DIR="$(cd "$(dirname "$0")/.." && pwd)/k8s"

echo "============================================"
echo "  DoraMCU Neuron Network — Kubernetes Deploy"
echo "============================================"
echo "Environment: ${ENVIRONMENT}"
echo "Namespace:   ${NAMESPACE}"
echo "K8s Dir:     ${K8S_DIR}"
echo ""

# Validate environment
if [[ ! -d "${K8S_DIR}/overlays/${ENVIRONMENT}" ]]; then
  echo "ERROR: Environment '${ENVIRONMENT}' not found."
  echo "Available: dev, staging, production"
  exit 1
fi

# Check kubectl
if ! command -v kubectl &> /dev/null; then
  echo "ERROR: kubectl not found. Install from https://kubernetes.io/docs/tasks/tools/"
  exit 1
fi

# Check kustomize (bundled with kubectl 1.14+)
if ! kubectl kustomize --help &> /dev/null; then
  echo "ERROR: kustomize not found. Upgrade kubectl or install kustomize separately."
  exit 1
fi

# Create namespace if not exists
echo "▶ Creating namespace '${NAMESPACE}'..."
kubectl apply -f "${K8S_DIR}/base/namespace.yaml"

# Apply secrets first
echo "▶ Applying secrets..."
kubectl apply -k "${K8S_DIR}/overlays/${ENVIRONMENT}" --selector app.kubernetes.io/part-of=doramcu-neuron-network --dry-run=client -o yaml | \
  kubectl apply -f -

# Apply all resources
echo "▶ Deploying all resources..."
kubectl apply -k "${K8S_DIR}/overlays/${ENVIRONMENT}"

echo ""
echo "✓ Deployment complete!"
echo ""
echo "▶ Checking pod status..."
kubectl get pods -n ${NAMESPACE} --watch || true

echo ""
echo "============================================"
echo "  Useful Commands"
echo "============================================"
echo "  Watch pods:     kubectl get pods -n ${NAMESPACE} -w"
echo "  View logs:      kubectl logs -n ${NAMESPACE} -l app.kubernetes.io/name=neuron -f"
echo "  Scale neurons:  kubectl -n ${NAMESPACE} scale deployment neuron --replicas=N"
echo "  Check HPA:      kubectl get hpa -n ${NAMESPACE}"
echo "  Port forward:   kubectl port-forward -n ${NAMESPACE} svc/neuron 8090:8090"
echo "  Port forward:   kubectl port-forward -n ${NAMESPACE} svc/doramcu-app 8084:8084"
echo "  Port forward:   kubectl port-forward -n ${NAMESPACE} svc/grafana 3000:3000"
echo "  Port forward:   kubectl port-forward -n ${NAMESPACE} svc/prometheus 9090:9090"
echo "============================================"

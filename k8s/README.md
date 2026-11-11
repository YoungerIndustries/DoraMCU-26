# DoraMCU Kubernetes Deployment

Kubernetes manifests and deployment scripts for the DoraMCU Neuron Network — a distributed AI satellite node system with auto-scaling, monitoring, and high availability.

## Architecture

```
┌────────────────────────────────────────────────────────────┐
│                    Kubernetes Cluster                       │
│  Namespace: doramcu                                        │
│                                                             │
│  ┌──────────────┐    ┌──────────────┐                      │
│  │  Ingress     │───▶│  Neuron Pods │───▶ Coordinator      │
│  │  (Nginx)     │    │  (HPA: 2-20) │    (doramcu-app)     │
│  └──────────────┘    └──────────────┘                      │
│                                        │                   │
│                     ┌──────────────┐   │                   │
│                     │  Redis       │◀──┘                   │
│                     │  (Registry)  │                       │
│                     └──────────────┘                       │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │  MySQL       │  │  DL Python   │  │  Prometheus  │      │
│  │  (Stateful)  │  │  (GPU opt.)  │  │  + Grafana   │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────────────────────────────────────────────┘
```

## Prerequisites

- **Kubernetes cluster** (1.24+) — Minikube, Kind, EKS, GKE, AKS
- **kubectl** configured with cluster access
- **Helm** (optional, for advanced deployments)
- **Nginx Ingress Controller** installed in the cluster
- **Metrics Server** installed for HPA to work

### Install Metrics Server (if not present)

```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

### Install Nginx Ingress Controller

```bash
kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/kind/deploy.yaml
```

## Quick Start

### 1. Build Docker Images

```bash
# Build DoraMCU coordinator image
docker build -t doramcu/app:latest .

# Build Neuron image
docker build -f Dockerfile.neuron -t doramcu/neuron:latest .

# Build DL Python image
docker build -f dora-dl-python/Dockerfile.dl -t doramcu/dl-python:latest ./dora-dl-python
```

For local development with Minikube:

```bash
eval $(minikube docker-env)
docker build -t doramcu/app:latest .
docker build -f Dockerfile.neuron -t doramcu/neuron:latest .
```

### 2. Deploy

```bash
# Dev environment (1-3 neurons, reduced resources)
./k8s/scripts/deploy.sh dev

# Staging environment (2-10 neurons)
./k8s/scripts/deploy.sh staging

# Production environment (5-50 neurons, full HA)
./k8s/scripts/deploy.sh production
```

### 3. Verify

```bash
# Check all pods
kubectl get pods -n doramcu

# Check services
kubectl get svc -n doramcu

# Check HPA status
kubectl get hpa -n doramcu

# Check neuron logs
kubectl logs -n doramcu -l app.kubernetes.io/name=neuron -f
```

## Directory Structure

```
k8s/
├── base/                          # Base Kubernetes manifests
│   ├── namespace.yaml             # Namespace definition
│   ├── configmap.yaml             # ConfigMaps and Secrets
│   ├── resource-quota.yaml        # Resource quotas and limits
│   ├── app-deployment.yaml        # DoraMCU coordinator
│   ├── neuron-deployment.yaml     # Neuron nodes + HPA
│   ├── mysql.yaml                 # MySQL StatefulSet
│   ├── redis.yaml                 # Redis Deployment
│   ├── dl-service.yaml            # Python DL service
│   ├── prometheus.yaml            # Prometheus monitoring
│   ├── grafana.yaml               # Grafana dashboards
│   ├── ingress.yaml               # Nginx Ingress rules
│   └── kustomization.yaml         # Base Kustomize config
├── overlays/
│   ├── dev/                       # Dev environment overlay
│   │   └── kustomization.yaml
│   ├── staging/                   # Staging environment overlay
│   │   └── kustomization.yaml
│   └── production/                # Production environment overlay
│       └── kustomization.yaml
└── scripts/
    ├── deploy.sh                  # Deployment script
    └── teardown.sh                # Cleanup script
```

## Auto-Scaling

The Neuron HPA (Horizontal Pod Autoscaler) scales based on:

| Metric | Target | Description |
|--------|--------|-------------|
| CPU | 70% utilization | Scale when CPU exceeds 70% |
| Memory | 80% utilization | Scale when memory exceeds 80% |
| HTTP RPS | 100 avg/pod | Custom Prometheus metric |

### Scale Behavior

- **Scale Up**: Aggressive — up to 100% increase or 4 pods per minute
- **Scale Down**: Conservative — 10% decrease per minute, 5-minute stabilization window

### Manual Scaling

```bash
# Temporarily override HPA
kubectl -n doramcu scale deployment neuron --replicas=10

# Check current scale
kubectl -n doramcu get deployment neuron
```

## Environment Differences

| Setting | Dev | Staging | Production |
|---------|-----|---------|------------|
| Min Neurons | 1 | 2 | 5 |
| Max Neurons | 3 | 10 | 50 |
| App Replicas | 1 | 1 | 2 |
| Mesh Network | Disabled | Enabled | Enabled |
| Consensus | Disabled | Enabled | Enabled |
| CPU Quota | 8 cores | 16 cores | 32 cores |
| Memory Quota | 16Gi | 32Gi | 64Gi |
| Max Pods | 20 | 50 | 100 |

## Monitoring

### Prometheus

Prometheus scrapes metrics from:
- DoraMCU coordinator (`/actuator/prometheus` on port 8084)
- All Neuron pods (auto-discovered via Kubernetes SD)
- Prometheus itself

Access via port-forward:
```bash
kubectl port-forward -n doramcu svc/prometheus 9090:9090
# Open http://localhost:9090
```

### Grafana

Grafana provides dashboards for:
- Cluster overview (CPU, memory, pod count)
- AI inference metrics (latency, throughput, errors)
- Experience collection (samples collected/flushed)
- Consensus network (votes, duration)
- P2P mesh (peers, messages)
- Unity clients (connected, predictions)

Access via port-forward:
```bash
kubectl port-forward -n doramcu svc/grafana 3000:3000
# Open http://localhost:3000 (admin/admin)
```

## Service Discovery

Neurons discover each other via:
1. **Coordinator registry** — Initial peer discovery via `/api/v1/neuron/discover`
2. **Kubernetes DNS** — Headless service enables direct pod-to-pod access
3. **Mesh topology** — Peers propagate topology updates for P2P mesh

### DNS Resolution

```
neuron-xxxxx.neuron.doramcu.svc.cluster.local    # Direct pod access
neuron.doramcu.svc.cluster.local                  # Headless service (all pods)
doramcu-app.doramcu.svc.cluster.local            # Coordinator
doramcu-redis.doramcu.svc.cluster.local          # Redis
doramcu-mysql.doramcu.svc.cluster.local          # MySQL
```

## Security

### Secrets Management

In production, **never** commit secrets to Git. Use:

```bash
# Option 1: Sealed Secrets
kubectl apply -f sealed-secrets.yaml

# Option 2: External secrets operator
# Option 3: Manual injection
kubectl create secret generic doramcu-secrets \
  --from-literal=db-password='your-password' \
  --from-literal=jwt-secret='your-jwt-secret' \
  -n doramcu
```

### Network Policies

For production, add NetworkPolicy to restrict traffic:

```yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: neuron-network-policy
  namespace: doramcu
spec:
  podSelector:
    matchLabels:
      app.kubernetes.io/name: neuron
  policyTypes:
    - Ingress
    - Egress
  ingress:
    - from:
        - podSelector:
            matchLabels:
              app.kubernetes.io/name: neuron
        - podSelector:
            matchLabels:
              app.kubernetes.io/name: doramcu-app
  egress:
    - to:
        - podSelector:
            matchLabels:
              app.kubernetes.io/name: neuron
        - podSelector:
            matchLabels:
              app.kubernetes.io/name: doramcu-app
```

## GPU Support (Optional)

For GPU-accelerated DL training:

1. Install NVIDIA Device Plugin:
```bash
kubectl apply -f https://raw.githubusercontent.com/NVIDIA/k8s-device-plugin/main/nvidia-device-plugin.yml
```

2. Uncomment GPU resources in `dl-service.yaml`:
```yaml
resources:
  limits:
    nvidia.com/gpu: 1
```

## Troubleshooting

### Pods Not Starting

```bash
# Check pod events
kubectl describe pod -n doramcu <pod-name>

# Check logs
kubectl logs -n doramcu <pod-name>

# Check resource quotas
kubectl describe quota -n doramcu
```

### HPA Not Scaling

```bash
# Check metrics server
kubectl top pods -n doramcu

# Check HPA status
kubectl describe hpa -n doramcu neuron-hpa
```

### Neurons Not Registering

```bash
# Check coordinator logs
kubectl logs -n doramcu -l app.kubernetes.io/name=doramcu-app

# Check Redis connectivity
kubectl exec -n doramcu -it deploy/doramcu-redis -- redis-cli ping
```

## Teardown

```bash
./k8s/scripts/teardown.sh
```

Or manually:

```bash
kubectl delete namespace doramcu
```

## Custom Metrics (Advanced)

For custom metric-based scaling (HTTP RPS), install the Prometheus Adapter:

```bash
helm install prometheus-adapter prometheus-community/prometheus-adapter \
  --set prometheus.url=http://prometheus.doramcu.svc.cluster.local:9090 \
  --set prometheus.port=9090
```

Then the HPA's custom metric `neuron_http_requests_per_second` will work.

## Multi-Region Federation (Phase 16)

Deploy multiple coordinator instances across geographic regions with cross-region neuron synchronization.

### Architecture

```
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│  US Region      │  │  EU Region      │  │  APAC Region    │
│  Coordinator    │  │  Coordinator    │  │  Coordinator    │
│  (Primary)      │◀─▶│  (Peer)        │◀─▶│  (Peer)        │
│  Neurons x5     │  │  Neurons x5     │  │  Neurons x5     │
│  Redis          │  │  Redis          │  │  Redis          │
└─────────────────┘  └─────────────────┘  └─────────────────┘
```

### Deploy Multi-Region

```bash
# Deploy the multi-region overlay
kubectl apply -k k8s/overlays/multi-region

# Each region needs its own coordinator with federation enabled:
# - NEURON_COORDINATOR_FEDERATION_ENABLED=true
# - NEURON_COORDINATOR_FEDERATION_PRIMARY=true (one region only)
# - NEURON_COORDINATOR_FEDERATION_REGION=us-east (unique per region)
# - NEURON_COORDINATOR_FEDERATION_PEER_URLS=http://peer1:8084,http://peer2:8084
```

### Neuron Failover

Neurons automatically failover to peer coordinators if the local coordinator is unreachable:

```yaml
env:
  - name: NEURON_COORDINATOR_URL
    value: http://coordinator-us:8084
  - name: NEURON_COORDINATOR_FALLBACK_URLS
    value: http://coordinator-eu:8084,http://coordinator-apac:8084
  - name: NEURON_FAILOVER_ENABLED
    value: "true"
```

### Federation API

```bash
# Register a peer coordinator
curl -X POST http://coordinator:8084/api/v1/neuron/federation/peer/register \
  -H "Content-Type: application/json" \
  -d '{"region":"eu-west","url":"http://coordinator-eu:8084"}'

# Check federation status
curl http://coordinator:8084/api/v1/neuron/federation/status

# Trigger cross-region consensus
curl -X POST http://coordinator:8084/api/v1/neuron/federation/consensus/cross-region \
  -H "Content-Type: application/json" \
  -d '{"features":[0.1,0.2,0.3],"timeoutMs":10000}'
```

## Contributing

1. Add new resources to `k8s/base/`
2. Update `k8s/base/kustomization.yaml` to include new resources
3. Test with `kubectl apply -k k8s/overlays/dev`
4. Update overlays as needed for environment-specific behavior

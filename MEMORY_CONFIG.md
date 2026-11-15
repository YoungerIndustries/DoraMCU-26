# DoraMCU Memory Configuration Guide

> **Last updated:** 2026-11-15
> **Purpose:** Document all memory-related configuration for DoraMCU to prevent OOM crashes and optimize performance.
> **Note:** JVM args are centralized in `gradle.properties` (`app.jvm.args`, `app.javacpp.args`) and referenced by `build.gradle`, `Dockerfile`, and test tasks.

---

## Table of Contents

- [JVM Memory Configuration](#jvm-memory-configuration)
- [Deep Learning Memory Configuration](#deep-learning-memory-configuration)
- [DL_MODE: Rest vs Local](#dl_mode-rest-vs-local)
- [Test Memory Configuration](#test-memory-configuration)
- [Docker Memory Limits](#docker-memory-limits)
- [Edge/Neuron Memory](#edgeneuron-memory)
- [Troubleshooting OOM Crashes](#troubleshooting-oom-crashes)
- [Memory Configuration Summary](#memory-configuration-summary)

---

## JVM Memory Configuration

### Gradle Build Process
**File:** `gradle.properties`

```properties
org.gradle.jvmargs=-Xmx4G -XX:+UseG1GC -XX:MaxMetaspaceSize=1G
```

- **Heap:** 4GB maximum
- **GC:** G1 Garbage Collector (recommended for large heaps)
- **Metaspace:** 1GB maximum for class metadata

### Application Runtime (bootRun)
**Files:** `gradle.properties` (centralized args) → `build.gradle` (references them)

The JVM args are defined once in `gradle.properties`:
```properties
app.jvm.args=-Xmx4G -XX:+UseG1GC -XX:MaxGCPauseMillis=200
app.javacpp.args=-Dorg.bytedeco.javacpp.maxbytes=2G -Dorg.bytedeco.javacpp.maxphysicalbytes=2G -Dorg.bytedeco.javacpp.alloctype=direct
```

And referenced in `build.gradle`:
```gradle
tasks.named('bootRun') {
    def jvmArgs = project.property('app.jvm.args').toString().split() +
                   project.property('app.javacpp.args').toString().split()
    jvmArgs jvmArgs
}
```

| Parameter | Value | Purpose |
|-----------|-------|---------|
| `-Xmx4G` | 4GB | Maximum Java heap size |
| `-XX:+UseG1GC` | Enabled | G1 GC for predictable pause times |
| `-XX:MaxGCPauseMillis=200` | 200ms | Target maximum GC pause time |
| `javacpp.maxbytes` | 2G | Max native memory for DL4j/ND4j |
| `javacpp.maxphysicalbytes` | 2G | Max physical memory for native ops |
| `javacpp.alloctype` | direct | Use direct (off-heap) memory allocation |
| `jdwp` | port 5005 | Remote debugging agent |

**Note:** The 4GB heap + 2GB native memory = 6GB total RAM requirement for the Java process.

---

## Deep Learning Memory Configuration

### DL Memory Guard
**File:** `src/main/resources/application.properties` (lines 77-81)

The DL Memory Guard prevents OOM crashes by checking available memory before DL training operations.

```properties
dl.memory-guard.enabled=true
dl.memory-guard.min-free-memory-mb=1024
dl.memory-guard.max-physical-memory-mb=4096
```

| Property | Default | Purpose |
|----------|---------|---------|
| `dl.memory-guard.enabled` | `true` | Enable/disable memory checking before DL ops |
| `dl.memory-guard.min-free-memory-mb` | `1024` | Minimum free MB required before training |
| `dl.memory-guard.max-physical-memory-mb` | `4096` | Maximum physical memory MB for DL operations |

**How it works:** Before any DL training operation, the Memory Guard checks if free memory exceeds `min-free-memory-mb`. If not, the operation is skipped with a warning log instead of crashing.

### DL4j Network Architecture Memory Impact
**File:** `src/main/resources/application.properties` (lines 64-75)

```properties
dl.input-size=32
dl.hidden-layer1-size=64
dl.hidden-layer2-size=128
dl.hidden-layer3-size=64
dl.output-size=5
dl.learning-rate=0.001
dl.batch-size=32
dl.epochs=10
dl.seed=42
dl.blend-factor=0.5
dl.model-dir=dl-models
```

**Memory impact by layer size:**
- Input: 32 neurons → ~1 KB
- Hidden 1: 64 neurons → ~8 KB
- Hidden 2: 128 neurons → ~32 KB (largest layer)
- Hidden 3: 64 neurons → ~8 KB
- Output: 5 neurons → ~1 KB
- **Total network weights:** ~50 KB (negligible)
- **Training memory:** Batch size 32 × epochs 10 → ~100 MB peak during training

**Tuning recommendations:**
- Reduce `dl.batch-size` if OOM during training (try 16 or 8)
- Reduce `dl.hidden-layer2-size` if memory is tight (try 64 instead of 128)
- Increase `dl.memory-guard.min-free-memory-mb` if crashes occur (try 2048)

### DL4j Version
**File:** `gradle.properties`

```properties
dl4j.version=1.0.0-M2.1
```

DL4j 1.0.0-M2.1 is a milestone release. Known memory issues:
- Native memory leaks in ND4j operations (mitigated by `javacpp.alloctype=direct`)
- OOM during large galaxy generation with DL4j character prediction (mitigated by Memory Guard)

---

## DL_MODE: Rest vs Local

The `DL_MODE` environment variable controls whether Deep Learning runs in-process (DL4j) or via external Python service.

### DL_MODE=rest (Default for Docker)
**File:** `docker-compose.yml` (line 14)

```yaml
environment:
  - DL_MODE=rest
```

- **Java app memory:** 4GB heap + 2GB native = 6GB total
- **Python DL service:** Separate container (`doramcu-dl` on port 8083)
- **Advantages:**
  - Java app uses less memory (no DL4j in-process)
  - Python service can be scaled independently
  - No native memory conflicts between Java and Python
- **Disadvantages:**
  - Network latency for DL requests
  - Requires Python DL service to be running
- **When to use:** Production deployments, Docker/Kubernetes, multi-instance setups

### DL_MODE=local (Development)
- **Java app memory:** 12GB heap + 4GB native = 16GB total recommended
- **DL4j runs in-process:** No external service needed
- **Advantages:**
  - No network latency
  - Simpler development setup
  - Full control over DL operations
- **Disadvantages:**
  - High memory requirement (16GB+ RAM)
  - Native memory conflicts possible
  - OOM crashes more likely without proper tuning
- **When to use:** Local development, testing DL4j features, when Python service is unavailable

### Switching DL_MODE

**Docker:**
```bash
# Rest mode (default)
DL_MODE=rest docker-compose up

# Local mode (increase memory limit first!)
# Edit docker-compose.yml: memory limit 4G → 12G
DL_MODE=local docker-compose up
```

**Local development (bootRun):**
```bash
# Set in IDE run configuration or environment
export DL_MODE=local
./gradlew bootRun
```

**Application property:**
```properties
# application.properties
dl.rest-client.enabled=false  # local mode
dl.rest-client.enabled=true   # rest mode (default)
```

---

## Test Memory Configuration

### Core Tests
**File:** `build.gradle` (lines 99-103)

```gradle
tasks.named('test') {
    useJUnitPlatform()
    maxHeapSize = '4G'
    jvmArgs '-XX:+UseG1GC', '-XX:MaxGCPauseMillis=200',
            '-Dorg.bytedeco.javacpp.maxbytes=2G',
            '-Dorg.bytedeco.javacpp.maxphysicalbytes=2G',
            '-Dorg.bytedeco.javacpp.alloctype=direct'
}
```

- **Heap:** 4GB
- **Native:** 2GB
- **Total:** 6GB RAM required
- **Covers:** All unit and integration tests except heavy galaxy tests

### Heavy Galaxy Tests
**File:** `build.gradle` (lines 128-132)

```gradle
tasks.register('heavyTests', Test) {
    useJUnitPlatform()
    maxHeapSize = '8G'
    jvmArgs '-XX:+UseG1GC', '-XX:MaxGCPauseMillis=200',
            '-Dorg.bytedeco.javacpp.maxbytes=2G',
            '-Dorg.bytedeco.javacpp.maxphysicalbytes=2G',
            '-Dorg.bytedeco.javacpp.alloctype=direct'
}
```

- **Heap:** 8GB (double the core tests)
- **Native:** 2GB
- **Total:** 10GB RAM required
- **Covers:** Galaxy generation tests that create large in-memory galaxy structures

### Running Tests

```bash
# Core tests only (fast, 4GB)
./gradlew test -x heavyTests

# Heavy tests only (slow, 8GB)
./gradlew heavyTests

# All tests (requires 8GB+ RAM)
./gradlew check

# Specific test class
./gradlew test --tests "com.yisy.modules.doramcu.sfb.*"
```

**Why heavy tests are separate:**
- Galaxy generation tests create 10,000+ planet objects in memory
- With DL4j character prediction, memory usage spikes to 6-8GB
- Running with core tests would cause OOM on 4GB heap
- Separating allows CI to run core tests quickly and heavy tests on larger machines

### Test Profile
**File:** `src/test/resources/application-test.properties`

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
```

Tests use H2 in-memory database, which is lightweight (~50MB) and doesn't contribute significantly to memory usage.

---

## Docker Memory Limits

### Main Application
**File:** `docker-compose.yml` (app service)

```yaml
environment:
  - JVM_XMX=-Xmx4G
deploy:
  resources:
    limits:
      memory: 4G
    reservations:
      memory: 2G
```

| Setting | Value | Purpose |
|---------|-------|---------|
| `JVM_XMX` | `-Xmx4G` | JVM maximum heap size (passed to Dockerfile ENTRYPOINT) |
| `limits.memory` | 4G | Hard limit; container killed if exceeded |
| `reservations.memory` | 2G | Guaranteed minimum; Docker tries to reserve this |

**With DL_MODE=rest:** 4GB is sufficient (DL handled by Python service)
**With DL_MODE=local:** Increase to 12GB (DL4j in-process needs more memory)

### Neuron Service
**File:** `docker-compose.yml` (neuron service)

```yaml
environment:
  - JVM_XMX=-Xmx4G
deploy:
  resources:
    limits:
      memory: 4G
    reservations:
      memory: 2G
```

The neuron service (player gateway, client distribution, game sync) now uses 4GB heap
to handle galaxy generation and game state synchronization without OOM crashes.

### MySQL Database
```yaml
image: mysql:8.4
```
- No explicit memory limit; uses Docker default
- Typical usage: 256-512MB for DoraMCU workload
- Consider adding memory limit if host RAM is constrained

### Python DL Service
```yaml
container_name: doramcu-dl
ports:
  - "8083:8083"
```
- No explicit memory limit; uses Docker default
- Typical usage: 512MB-1GB for inference
- Models stored in `dl-models/` volume

### Redis Cache
```yaml
image: redis:7-alpine
command: redis-server --appendonly yes
```
- No explicit memory limit; uses Docker default
- Typical usage: 64-128MB for DoraMCU cache
- AOF persistence enabled (`--appendonly yes`)

---

## Edge/Neuron Memory

### Neuron Edge Device
**File:** `dora-neuron/src/main/resources/application.properties` (lines 112-115)

```properties
neuron.edge.swap-monitoring-enabled=true
neuron.edge.max-heap-mb=512
```

| Property | Value | Purpose |
|----------|-------|---------|
| `swap-monitoring-enabled` | `true` | Monitor swap usage on Linux edge devices |
| `max-heap-mb` | `512` | Recommended max heap for edge devices |

**Edge device constraints:**
- Target: Raspberry Pi 4 (2-4GB RAM)
- Uses ONNX Runtime instead of DL4j (lightweight inference)
- 512MB heap is sufficient for edge workload
- Swap monitoring alerts when swap usage exceeds threshold

### ONNX Runtime vs DL4j
**File:** `dora-neuron/build.gradle` (line 41)

```gradle
implementation "com.microsoft.onnxruntime:onnxruntime:${findProperty('onnxruntime.version')}"
```

- **ONNX Runtime:** ~50MB memory for inference
- **DL4j:** ~500MB-2GB memory for training + inference
- Edge devices use ONNX for lightweight inference without full DL4j overhead

---

## Troubleshooting OOM Crashes

### Symptom: Java Heap Space OOM

**Error:** `java.lang.OutOfMemoryError: Java heap space`

**Causes:**
1. Galaxy generation with too many planets
2. DL4j training with large batch size
3. Test suite running with insufficient heap

**Solutions:**
1. Increase `-Xmx` in bootRun: `-Xmx8G`
2. Reduce `dl.batch-size` in application.properties
3. Run heavy tests separately: `./gradlew heavyTests`

### Symptom: Native Memory OOM

**Error:** `java.lang.OutOfMemoryError: Native memory allocation failed`

**Causes:**
1. JavaCPP native memory exhausted
2. DL4j/ND4j direct memory allocation failure
3. Memory fragmentation

**Solutions:**
1. Increase `javacpp.maxbytes`: `-Dorg.bytedeco.javacpp.maxbytes=4G`
2. Enable Memory Guard: `dl.memory-guard.enabled=true`
3. Increase `dl.memory-guard.min-free-memory-mb`: `2048`
4. Use `DL_MODE=rest` to offload DL to Python service

### Symptom: GC Overhead Limit

**Error:** `java.lang.OutOfMemoryError: GC overhead limit exceeded`

**Causes:**
1. GC spending >98% of time reclaiming <2% of heap
2. Memory pressure from large objects

**Solutions:**
1. Increase heap: `-Xmx8G`
2. Tune GC pause: `-XX:MaxGCPauseMillis=500`
3. Reduce object creation in hot paths
4. Use profiling to identify memory leaks

### Symptom: Container Killed by OOM Killer

**Error:** Docker container exits with code 137

**Causes:**
1. Container exceeds memory limit in docker-compose.yml
2. DL_MODE=local with 4GB limit (insufficient)

**Solutions:**
1. Check Docker logs: `docker logs doramcu`
2. Increase memory limit: `memory: 12G` for DL_MODE=local
3. Switch to DL_MODE=rest for lower memory usage

### Crash Dump Files

The following crash dump files have been found in the project root:
- `dl4j-memory-crash-dump-1793970244951_78.txt`
- `dl4j-memory-crash-dump-1794014508270_79.txt`
- `hs_err_pid12052.log`

**These should be analyzed for specific crash causes and added to `.gitignore`.**

---

## Memory Configuration Summary

### Minimum System Requirements

| Mode | RAM | Heap | Native | Total |
|------|-----|------|--------|-------|
| **Development (H2, DL local)** | 16GB | 4GB | 2GB | 6GB + OS overhead |
| **Development (H2, DL rest)** | 8GB | 4GB | 0GB | 4GB + Python service |
| **Production (MySQL, DL rest)** | 8GB | 4GB | 0GB | 4GB + MySQL + Redis |
| **Production (MySQL, DL local)** | 16GB | 12GB | 4GB | 16GB + MySQL + Redis |
| **Edge/Neuron** | 2GB | 512MB | 50MB | ~1GB total |

### Recommended Configuration by Scenario

| Scenario | DL_MODE | Heap | Native | Docker Memory |
|----------|---------|------|--------|---------------|
| Local dev, 8GB RAM | rest | 4G | 0 | 4G |
| Local dev, 16GB RAM | local | 4G | 2G | 8G |
| CI/CD pipeline | rest | 4G | 0 | 4G |
| Production, small | rest | 4G | 0 | 4G |
| Production, large | local | 12G | 4G | 16G |
| Edge device | N/A (ONNX) | 512M | 50M | N/A |

### Quick Reference: Environment Variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `DL_MODE` | `rest` | `rest` = Python DL service, `local` = DL4j in-process |
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` = H2, `mysql` = MySQL |
| `DEBUG` | empty | Set to `true` to enable remote debugging on port 5005 |

### Quick Reference: Key Properties

| Property | Default | Purpose |
|----------|---------|---------|
| `dl.memory-guard.enabled` | `true` | Enable memory checking before DL ops |
| `dl.memory-guard.min-free-memory-mb` | `1024` | Min free MB before training |
| `dl.memory-guard.max-physical-memory-mb` | `4096` | Max physical MB for DL |
| `dl.batch-size` | `32` | Training batch size (reduce if OOM) |
| `dl.hidden-layer2-size` | `128` | Largest layer (reduce if OOM) |

---

## Monitoring Memory Usage

### JVM Metrics via Actuator

The application exposes memory metrics at:
```
GET /actuator/metrics/jvm.memory.used
GET /actuator/metrics/jvm.memory.max
GET /actuator/metrics/jvm.memory.committed
```

### Prometheus Integration

With Prometheus enabled, monitor:
- `jvm_memory_used_bytes` — Current heap usage
- `jvm_memory_max_bytes` — Maximum heap capacity
- `process_memory_usage` — Total process memory (heap + native)

### Docker Memory Monitoring

```bash
# Check container memory usage
docker stats doramcu

# Check all DoraMCU services
docker stats doramcu doramcu-mysql doramcu-dl doramcu-redis
```

### Edge Device Monitoring

```bash
# Check memory on edge device
free -h

# Check swap usage
swapon --show
```

---

## IDENTITY
**Memory Configuration Guide for DoraMCU**

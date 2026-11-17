# Brain Node — Desktop Server for DoraMCU

## Overview

The **Brain Node** is a desktop-based server that extends the `dora-neuron` module to handle heavy AI inference, persistent storage, and game management for the DoraMCU system. It acts as a backup server to the Raspberry Pi, which serves as the primary coordinator.

## Architecture

```
Raspberry Pi (Primary)          Desktop Brain Node (Backup)
┌─────────────────┐            ┌──────────────────────┐
│  dora-neuron    │◄──HTTP──►  │  dora-neuron         │
│  Base Node      │            │  Brain Node Profile  │
│  MySQL DB       │◄──Sync──►  │  H2 Embedded DB      │
│  Coordination   │            │  Large ONNX Models   │
└─────────────────┘            │  Multi-thread AI     │
      │                       └──────────────────────┘
      │ USB Serial
      └──────────────────────────────────────────────┘
```

## Quick Start

### Prerequisites

- **Java 27** (Temurin distribution recommended)
- **Gradle 9.8+** (wrapper included)
- **USB-to-TTL serial adapter** or Raspberry Pi Zero with USB gadget mode
- **Appropriate drivers** installed (CH340, FTDI, CP2102, etc.)

### Running the Brain Node

```bash
# Build the project
./gradlew :dora-neuron:build -x test

# Run with the brain-node profile
./gradlew :dora-neuron:bootRun --args='--spring.profiles.active=brain-node'
```

The Brain Node will start on `http://localhost:8091` with:
- H2 embedded database at `backups/brain-node/`
- USB serial auto-detection for Raspberry Pi
- Enhanced AI inference with 8 threads
- Larger experience buffers (5000 samples)

### Docker Deployment

```bash
docker run -d \
  --name brain-node \
  -p 8091:8091 \
  -v $(pwd)/backups/brain-node:/app/backups/brain-node \
  -e SPRING_PROFILES_ACTIVE=brain-node \
  dora-mcu/neuron:latest
```

## Configuration

### Spring Profile

The Brain Node uses the `brain-node` Spring profile defined in `application-brain-node.properties`.

### Key Configuration Properties

| Property | Default | Description |
|----------|---------|-------------|
| `brainnode.enabled` | `true` | Enable Brain Node functionality |
| `brainnode.pi-coordinator-url` | `http://localhost:8084` | Raspberry Pi coordinator URL |
| `brainnode.serial-enabled` | `true` | Enable USB serial communication |
| `brainnode.serial-port-auto-detect` | `true` | Auto-detect Pi's serial port |
| `brainnode.serial-baud-rate` | `115200` | Serial baud rate |
| `brainnode.inference-threads` | `8` | AI inference thread count |
| `brainnode.ai-model-size` | `large` | ONNX model size class |
| `brainnode.confidence-threshold` | `0.85` | AI confidence threshold |
| `brainnode.storage-type` | `H2` | Local storage type (H2/SQLite) |
| `brainnode.storage-path` | `backups/brain-node/` | Storage file path |
| `brainnode.experience-buffer-size` | `5000` | Experience sample buffer |
| `brainnode.auto-failover-enabled` | `true` | Auto-takeover when Pi is down |
| `brainnode.max-cpu-usage-percent` | `70` | Max CPU usage limit |
| `brainnode.max-memory-mb` | `4096` | Max memory usage in MB |

### Custom Configuration

Override defaults in `application.properties` or via command line:

```bash
./gradlew :dora-neuron:bootRun --args='--spring.profiles.active=brain-node --brainnode.serial-port=COM4'
```

## REST API

All Brain Node endpoints are prefixed with `/api/v1/brain-node`.

### Status

```
GET /api/v1/brain-node/status
```

Returns comprehensive Brain Node status including serial connection, AI readiness, Pi reachability, storage stats, and inference stats.

### Health

```
GET /api/v1/brain-node/health
```

Returns health status: `UP` or `DOWN` with component details.

### Pi Health Check

```
GET /api/v1/brain-node/pi/health
```

Check if the Raspberry Pi is reachable.

### Manual Sync

```
POST /api/v1/brain-node/sync
```

Trigger manual data synchronization with the Pi.

### Serial Command

```
POST /api/v1/brain-node/serial/command
Content-Type: application/json

{"command": "ls -la"}
```

Send a command to the Raspberry Pi via USB serial.

### Available Serial Ports

```
GET /api/v1/brain-node/serial/ports
```

List all available serial ports on the system.

### Restart Serial Connection

```
POST /api/v1/brain-node/serial/reconnect
```

Restart the USB serial connection to the Pi.

### Configuration

```
GET /api/v1/brain-node/config
```

Get current Brain Node configuration.

### Failover

```
POST /api/v1/brain-node/failover
```

Trigger manual failover to take over game management.

## USB Serial Setup

### USB-to-TTL Serial Adapter

1. **Wire the adapter** to the Pi's GPIO pins:
   - Black → GND (pin 6)
   - White → GPIO 14 / TXD (pin 8)
   - Green → GPIO 15 / RXD (pin 10)

2. **Enable UART on the Pi:**
   ```
   # /boot/firmware/config.txt
   enable_uart=1
   
   # /boot/firmware/cmdline.txt
   console=serial0,115200
   ```

3. **Install drivers** on Windows (CH340, FTDI, CP2102)

### USB Gadget Mode (Pi Zero/Zero 2 W)

1. **Edit cmdline.txt:**
   ```
   # Add to end of /boot/firmware/cmdline.txt
   modules-load=dwc2,g_serial
   ```

2. **Enable getty service:**
   ```bash
   sudo systemctl enable --now getty@ttyGS0
   ```

3. **Reboot the Pi** and connect via micro-USB

## Persistent Storage

The Brain Node uses an embedded H2 database for local storage:

- **Game states**: Persisted in `brainnode_game_state` table
- **Experience samples**: Stored in `brainnode_experience_sample` table
- **AI model cache**: Metadata in `brainnode_ai_model_cache` table

### H2 Console

Access the H2 database console at `http://localhost:8091/h2-console`:
- JDBC URL: `jdbc:h2:file:./backups/brain-node/brain-node-db`
- Username: `sa`
- Password: (empty)

### Data Sync

Data is automatically synchronized with the Raspberry Pi via the existing `DataSyncService` using vector clocks for conflict resolution.

## AI Inference

The Brain Node uses larger ONNX models with multi-threaded inference:

- **Thread pool**: Configurable (default 8 threads)
- **Model size**: Large models for desktop hardware
- **Confidence threshold**: Higher (0.85) for better accuracy
- **GPU acceleration**: Optional, configurable

### Model Management

```bash
# Swap model via API
curl -X POST http://localhost:8091/api/v1/co-server/model/swap \
  -H "Content-Type: application/json" \
  -d '{"modelPath": "models/brain-node/large-model.onnx", "version": 2}'
```

## Failover

### Automatic Failover

When the Pi becomes unreachable for >30 seconds:
1. Brain Node detects Pi is unreachable via health checks
2. LeaderElectionService triggers leader election
3. Brain Node promotes itself to co-server
4. Game management continues on Brain Node

### Manual Failover

```bash
curl -X POST http://localhost:8091/api/v1/brain-node/failover
```

## Troubleshooting

### Serial Port Not Detected

1. Check Device Manager for USB serial adapter
2. Verify drivers are installed (CH340, FTDI, CP2102)
3. Ensure Pi is connected via USB
4. Check `brainnode.serial-port-auto-detect=true`

### Pi Unreachable

1. Verify Pi is running: `curl http://<pi-ip>:8084/actuator/health`
2. Check `brainnode.pi-coordinator-url` is correct
3. Verify network connectivity between desktop and Pi
4. Check serial connection: `GET /api/v1/brain-node/serial/ports`

### AI Inference Errors

1. Check model path: `brainnode.ai-model-path`
2. Verify ONNX model file exists
3. Check inference thread count: `brainnode.inference-threads`
4. Monitor logs: `logging.level.com.yisy.modules.doraneuron=DEBUG`

### Database Issues

1. Check storage path: `brainnode.storage-path`
2. Verify directory permissions
3. Access H2 console: `http://localhost:8091/h2-console`
4. Check disk space

### Performance Issues

1. Reduce inference threads: `brainnode.inference-threads=4`
2. Lower CPU limit: `brainnode.max-cpu-usage-percent=50`
3. Reduce memory: `brainnode.max-memory-mb=2048`
4. Use smaller models: `brainnode.ai-model-size=medium`

## File Structure

```
dora-neuron/src/main/java/com/yisy/modules/doraneuron/
├── config/
│   └── BrainNodeConfig.java              # Brain Node configuration properties
├── service/brainnode/
│   ├── BrainNodeService.java             # Main orchestration
│   ├── SerialConsoleService.java         # USB serial communication
│   ├── BrainNodeStorageService.java      # Persistent storage
│   └── BrainNodeAiService.java           # Enhanced AI inference
├── controller/brainnode/
│   └── BrainNodeController.java          # REST API
└── model/brainnode/
    ├── BrainNodeStatus.java              # Status model
    ├── GameStateEntity.java              # Game state JPA entity
    ├── ExperienceSampleEntity.java       # Experience sample JPA entity
    └── AiModelCacheEntity.java           # AI model cache JPA entity

dora-neuron/src/main/resources/
└── application-brain-node.properties     # Brain Node Spring profile
```

## Testing

```bash
# Run all Brain Node tests
./gradlew :dora-neuron:test --tests "*brainnode*"

# Run specific test
./gradlew :dora-neuron:test --tests "SerialConsoleServiceTest"
```

## Monitoring

### Prometheus Metrics

Brain Node exposes Prometheus metrics at `/actuator/prometheus`:
- Inference latency and error rates
- Serial connection status
- Data sync statistics
- Storage usage

### Logs

Configure logging level:
```properties
logging.level.com.yisy.modules.doraneuron.service.brainnode=DEBUG
```

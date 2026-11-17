# Raspberry Pi DoraMCU Dedicated Game Server — Setup Guide

This guide walks you through transforming a Raspberry Pi into a fully self-contained DoraMCU dedicated game server. All six services (app, neuron, MySQL, Redis, nginx, DL) run via Docker Compose, making the Pi a standalone multiplayer game server accessible by players on your local network.

## Prerequisites

### Hardware
- **Raspberry Pi 4** (4GB or 8GB RAM) or **Raspberry Pi 5** (8GB recommended)
- High-speed microSD card (32GB+ minimum, Class A1 or better)
- USB keyboard and HDMI monitor (for initial setup)
- Ethernet cable (recommended for stable server operation)

### Software
- Raspberry Pi Imager (available at https://www.raspberrypi.com/software/)
- SSH client (PuTTY, OpenSSH, or built-in terminal)
- SFTP client for file transfer (optional)

## Step 1: Install Raspberry Pi OS

### Download and Flash
1. Download **Raspberry Pi Imager** from https://www.raspberrypi.com/software/
2. Insert your microSD card into your computer
3. Open Raspberry Pi Imager and select:
   - **Device**: Your Raspberry Pi model
   - **OS**: **Raspberry Pi OS (64-bit)** — Bookworm (latest)
     - ⚠️ **Important**: You must select the **64-bit** version. The 32-bit version does not support ARM64 Docker images.
4. Select your microSD card as the storage target
5. Click **Write** and wait for the process to complete

### Enable SSH and Configure Network
Before first boot, enable SSH and configure Wi-Fi (if not using Ethernet):

1. Eject and re-insert the microSD card
2. Open the `boot` partition (it will appear as a USB drive)
3. Create an empty file named `ssh` (no extension) in the root of the boot partition
4. For Wi-Fi, create a file named `wpa_supplicant.conf` with:
   ```
   country=US
   ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
   update_config=1

   network={
       ssid="YourNetworkName"
       psk="YourPassword"
       key_mgmt=WPA-PSK
   }
   ```
5. Eject the card safely and insert it into the Raspberry Pi

### First Boot
1. Connect the Pi to power via USB-C
2. Wait 2-3 minutes for the first boot
3. Find the Pi's IP address:
   - Check your router's DHCP client list
   - Or use: `sudo nmap -sn 192.168.1.0/24` (replace with your network)
4. SSH into the Pi:
   ```bash
   ssh pi@<pi-ip-address>
   ```
   Default password: `raspberry`

## Step 2: OS Configuration

### Initial Setup
```bash
# Update system packages
sudo apt update && sudo apt full-upgrade -y

# Set a strong password
passwd

# Change hostname (optional)
sudo hostnamectl set-hostname doramcu-server
```

### Configure Swap Space
Docker Compose with 6 services needs adequate swap to prevent OOM kills:

```bash
sudo dphys-swapfile swapoff
sudo nano /etc/dphys-swapfile
```

Change `CONF_SWAPSIZE` to:
```
CONF_SWAPSIZE=2048
```

Then restart the swap service:
```bash
sudo dphys-swapfile setup
sudo dphys-swapfile swapon
```

Verify with `free -h`:
```
              total        used        free      shared  buff/cache   available
Mem:           7.8Gi       512Mi       6.8Gi       12Mi       512Mi       7.1Gi
Swap:         2.0Gi          0B       2.0Gi
```

### Disable GPU Memory Split
For maximum RAM available to Docker containers, reduce GPU memory allocation:

```bash
sudo raspi-config
```
Navigate to: **Performance Options** → **GPU Memory** → Set to **16** (minimum)

Alternatively, edit `/boot/firmware/config.txt`:
```bash
sudo nano /boot/firmware/config.txt
```
Add or modify:
```
gpu_mem=16
```

### Overclocking (Optional, Pi 4/5 only)
For improved performance, enable moderate overclocking:

```bash
sudo nano /boot/firmware/config.txt
```

**Pi 4 (4GB):**
```
arm_freq=2000
gpu_freq=750
over_voltage=2
```

**Pi 5 (8GB):**
```
arm_freq=2.4
gpu_freq=800
over_voltage=2
```

Reboot after changes:
```bash
sudo reboot
```

## Step 3: Install Docker Engine

### Official Docker Installation Script
```bash
curl -fsSL https://get.docker.com | sudo sh -
```

### Add User to Docker Group
```bash
sudo usermod -aG docker $USER
newgrp docker
```

### Enable Docker Service
```bash
sudo systemctl enable docker
sudo systemctl start docker
```

### Verify Installation
```bash
docker --version
docker compose version
```

Expected output:
```
Docker version 27.x.x, build xxx
Docker Compose version v2.x.x
```

### Install Docker Compose Plugin (if not included)
```bash
sudo apt install -y docker-compose-plugin
```

## Step 4: Build ARM64 Docker Images

### Option A: Build on AMD64 Host (Recommended)
Building Java images on a Pi is extremely slow (2-4 hours). The recommended approach is to build ARM64 images on your development machine using Docker Buildx:

```bash
# On your AMD64 development machine
cd DoraMCU

# Set up Buildx for cross-compilation
docker buildx create --name arm64-builder --use
docker buildx inspect --bootstrap

# Build all ARM64 images
docker buildx build --platform linux/arm64 -t doramcu-app:latest -f Dockerfile .
docker buildx build --platform linux/arm64 -t doramcu-neuron:latest -f Dockerfile.neuron .
docker buildx build --platform linux/arm64 -t doramcu-nginx:latest -f Dockerfile.web .
docker buildx build --platform linux/arm64 -t doramcu-dl:arm64 -f Dockerfile.dl.arm64 .

# Push to a local registry or Docker Hub for the Pi to pull
docker push your-registry/doramcu-app:latest
docker push your-registry/doramcu-neuron:latest
docker push your-registry/doramcu-nginx:latest
docker push your-registry/doramcu-dl:arm64
```

### Option B: Build Directly on Pi
If you prefer to build directly on the Pi (expect 2-4 hours for Java images):

```bash
# On the Raspberry Pi
cd DoraMCU

# Build each image
docker compose -f docker-compose.yml -f docker-compose.pi.yml build
```

### Option C: Pull Pre-built Images
If images are available in a registry:

```bash
docker pull your-registry/doramcu-app:latest
docker pull your-registry/doramcu-neuron:latest
docker pull your-registry/doramcu-nginx:latest
docker pull your-registry/doramcu-dl:arm64
```

## Step 5: Deploy DoraMCU

### Clone the Repository
```bash
cd ~
git clone https://github.com/your-org/DoraMCU.git
cd DoraMCU
```

### Configure Environment Variables
Create a `.env` file with your settings:

```bash
# Database password
DB_PASSWORD=your_secure_password

# JWT secret for authentication
JWT_SECRET=your_jwt_secret_key

# Neuron JWT secret
NEURON_SECURITY_JWT_SECRET=your_neuron_jwt_secret
```

### Start All Services
```bash
docker compose -f docker-compose.yml -f docker-compose.pi.yml up -d
```

### Verify Deployment
```bash
# Check all container statuses
docker compose -f docker-compose.yml -f docker-compose.pi.yml ps

# View logs for each service
docker compose -f docker-compose.yml -f docker-compose.pi.yml logs -f
```

Expected output — all services should be running:
```
NAME                IMAGE                    STATUS
doramcu-nginx       doramcu-nginx:latest     Up (healthy)
doramcu-neuron      doramcu-neuron:latest    Up (healthy)
doramcu             doramcu-app:latest       Up (healthy)
doramcu-mysql       mysql:8.4                Up (healthy)
doramcu-redis       redis:7-alpine           Up (healthy)
doramcu-dl          doramcu-dl:arm64         Up (healthy)
```

## Step 6: Network Configuration

### Access the Server
Open a browser on any device on the same network and navigate to:
```
http://<pi-ip-address>
```

### Reserve a Static IP (Recommended)
To ensure the Pi always has the same IP address:

**Option 1: Router DHCP Reservation**
- Log into your router's admin panel
- Find DHCP reservations / static leases
- Reserve the Pi's MAC address to a fixed IP

**Option 2: Pi-side Static IP**
```bash
sudo nano /etc/dhcpcd.conf
```
Add at the end:
```
interface eth0
static ip_address=192.168.1.100/24
static routers=192.168.1.1
static domain_name_servers=192.168.1.1 8.8.8.8
```

### Port Forwarding (Optional)
To access the server from outside your local network:

1. Log into your router
2. Forward port 80 (HTTP) to the Pi's IP address
3. Consider using a dynamic DNS service (e.g., DuckDNS) for a stable domain name
4. ⚠️ **Security warning**: Exposing the server to the internet requires proper SSL/TLS configuration and firewall rules

## Step 7: Monitoring and Maintenance

### Check Service Health
```bash
# Overall status
docker compose -f docker-compose.yml -f docker-compose.pi.yml ps

# Resource usage
docker stats

# System memory
free -h

# CPU temperature
vcgencmd measure_temp
```

### View Logs
```bash
# All services
docker compose -f docker-compose.yml -f docker-compose.pi.yml logs -f

# Specific service
docker logs -f doramcu-app
docker logs -f doramcu-neuron
docker logs -f doramcu-dl
```

### Edge Monitoring Endpoint
The neuron gateway exposes an edge monitoring endpoint:
```bash
curl http://<pi-ip-address>:8090/api/v1/neuron/edge
```

This returns the current edge profile, resource usage, and optimization status.

### Auto-start on Boot
Docker containers are configured with `restart: unless-stopped`, so they will automatically restart after a Pi reboot. To ensure Docker itself starts on boot:

```bash
sudo systemctl enable docker
```

### Update Services
When a new version is available:
```bash
cd ~/DoraMCU
git pull
docker compose -f docker-compose.yml -f docker-compose.pi.yml up -d --build
```

## Troubleshooting

### Out of Memory (OOM) Errors
**Symptom**: Containers crash unexpectedly, `docker ps` shows restarting containers.

**Solutions**:
1. Check memory usage: `docker stats`
2. Increase swap: Edit `/etc/dphys-swapfile`, set `CONF_SWAPSIZE=4096`
3. Reduce service memory limits in `docker-compose.pi.yml`
4. Upgrade to Pi 5 with 8GB RAM

### MySQL Won't Start
**Symptom**: `doramcu-mysql` container keeps restarting.

**Solutions**:
1. Check logs: `docker logs doramcu-mysql`
2. Verify InnoDB buffer pool size is appropriate for available RAM
3. If disk space is low: `df -h` and clean up unused images: `docker system prune -a`

### DL Service Slow or Unresponsive
**Symptom**: AI features have high latency or time out.

**Solutions**:
1. CPU-only PyTorch on ARM64 is inherently slower than GPU-accelerated inference
2. Check DL logs: `docker logs doramcu-dl`
3. Reduce `DL_BLEND_FACTOR` in `docker-compose.pi.yml` to use less DL processing
4. Consider disabling DL features if not needed

### Neuron Edge Profile Not Applied
**Symptom**: Neuron does not auto-detect edge hardware.

**Solutions**:
1. Verify `NEURON_EDGE_FORCED_PROFILE=EDGE` is set in `docker-compose.pi.yml`
2. Check neuron logs: `docker logs doramcu-neuron`
3. The edge profile should show in logs at startup

### Docker Build Fails on Pi
**Symptom**: `docker build` runs out of memory during compilation.

**Solutions**:
1. Use Option A (build on AMD64 host with Buildx) instead
2. Increase swap to 4GB
3. Build one image at a time with `docker compose build <service>`

### Network Connectivity Issues
**Symptom**: Cannot access the server from other devices.

**Solutions**:
1. Verify the Pi's IP: `hostname -I`
2. Check firewall: `sudo ufw status` (disable if enabled: `sudo ufw disable`)
3. Test port 80: `curl http://localhost:80` from the Pi itself
4. Verify nginx is running: `docker ps | grep nginx`

## Resource Summary

| Service | Memory Limit | CPU Limit | Notes |
|---------|-------------|-----------|-------|
| nginx | 128M | — | Reverse proxy, minimal overhead |
| neuron | 512M | 0.5 cores | Edge profile, 256M JVM heap |
| app | 1G | 1.5 cores | Main game server, DL_MODE=rest |
| mysql | 512M | 0.5 cores | 256M InnoDB buffer pool |
| redis | 256M | 0.25 cores | 128M maxmemory, LRU eviction |
| dl | 512M | 0.5 cores | CPU-only PyTorch |
| **Total** | **~2.7G** | **~3.0 cores** | Leaves headroom for OS |

## Appendix: Docker Compose Reference

The Raspberry Pi deployment uses two Compose files:

1. **`docker-compose.yml`** — Base configuration with all service definitions
2. **`docker-compose.pi.yml`** — Pi-specific overlay that reduces resource limits and enables edge optimizations

To start:
```bash
docker compose -f docker-compose.yml -f docker-compose.pi.yml up -d
```

To stop:
```bash
docker compose -f docker-compose.yml -f docker-compose.pi.yml down
```

To rebuild and restart:
```bash
docker compose -f docker-compose.yml -f docker-compose.pi.yml up -d --build
```

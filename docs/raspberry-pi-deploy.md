# Raspberry Pi USB Boot Deployment Guide

This guide walks you through creating a bootable USB disk for Raspberry Pi 4/5 and deploying DoraMCU on it.

## Prerequisites

- **Raspberry Pi 4** (4GB+ RAM, 8GB recommended) or **Raspberry Pi 5** (8GB recommended)
- **USB 3.0 flash drive** (32GB+ recommended, Class 10 or better)
- **Computer** with USB port (Windows, macOS, or Linux)
- **Ethernet cable** (recommended for initial setup)
- **MicroSD card** (optional, for fallback boot)

## Step 1: Download Raspberry Pi OS

Download **Raspberry Pi OS 64-bit** (ARM64) — the 64-bit version is required for Docker ARM64 images:

- Full desktop: https://www.raspberrypi.com/software/operating-systems/
- Lite (no desktop, recommended for headless server): Choose "Raspberry Pi OS (64-bit) Lite"

## Step 2: Flash to USB Drive

### Using Raspberry Pi Imager (Recommended)

1. Download Raspberry Pi Imager: https://www.raspberrypi.com/software/
2. Open the imager and select:
   - **Device**: Your Raspberry Pi model (Pi 4 or Pi 5)
   - **OS**: Raspberry Pi OS (64-bit) Lite
   - **Storage**: Your USB flash drive
3. Click the gear icon (⚙️) before flashing to configure **SSH** and **Wi-Fi**:
   - Enable SSH
   - Set username and password (default: `pi` / `raspberry`)
   - Configure Wi-Fi (optional, if not using Ethernet)
4. Click **Write** and wait for the process to complete

### Using balenaEtcher

1. Download balenaEtcher: https://www.balena.io/etcher/
2. Select the OS image file
3. Select your USB drive
4. Click **Flash!**

## Step 3: Enable USB Boot

### For Raspberry Pi 4

The Pi 4 needs a boot mode configuration change to boot from USB:

1. **If you have a working MicroSD card**:
   - Boot from the MicroSD card
   - Run: `sudo rpi-eeprom-update -a`
   - Edit config: `sudo nano /etc/default/rpi-eeprom.config`
   - Set: `BOOT_ORDER=0xf41` (try USB, then network, then SD)
   - Run: `sudo rpi-eeprom-update -d -f /lib/firmware/rpi-eeprom/latest/pi4/BOOTLOADER.dat`
   - Reboot

2. **If you don't have a MicroSD card** (Pi 4 with USB boot only):
   - The Pi 4 ships with USB boot disabled by default
   - You need a MicroSD card temporarily to update the bootloader
   - Follow the steps above, then remove the SD card

### For Raspberry Pi 5

The Pi 5 boots from USB by default — no configuration needed!

## Step 4: First Boot

1. Insert the USB drive into the Raspberry Pi
2. Connect Ethernet cable
3. Power on the Pi
4. Wait 2-3 minutes for the first boot (filesystem expansion, etc.)

### Find the Pi's IP Address

```powershell
# On Windows, scan your network
Test-NetConnection -ComputerName raspberrypi.local

# Or check your router's DHCP client list
```

### SSH into the Pi

```powershell
ssh pi@raspberrypi.local
# Default password: raspberry
# You will be prompted to change the password
```

## Step 5: Install Docker Desktop for Raspberry Pi

### Automatic Setup (Recommended)

Run the included setup script:

```bash
# Clone the repository first
git clone https://github.com/YoungerIndustries/DoraMCU-26.git
cd DoraMCU-26

# Run the Pi setup script
chmod +x scripts/pi-setup.sh
sudo ./scripts/pi-setup.sh
```

### Manual Docker Installation

```bash
# Install Docker using the official script
curl -fsSL https://get.docker.com | sh

# Add pi user to docker group
sudo usermod -aG docker pi

# Enable Docker service
sudo systemctl enable docker
sudo systemctl start docker

# Reboot for group changes to take effect
sudo reboot
```

### Install Docker Compose V2

```bash
# Docker Compose V2 is included in Docker Desktop for Pi
# Verify installation
docker compose version
```

## Step 6: Deploy DoraMCU

### Clone the Repository

```bash
cd ~
git clone https://github.com/YoungerIndustries/DoraMCU-26.git
cd DoraMCU-26
```

### Configure Environment

```bash
# Copy the Pi environment template
cp .env.pi.example .env

# Edit the environment file
nano .env
```

Key settings in `.env`:
- `DB_PASSWORD` — MySQL root password (change from default!)
- `JWT_SECRET` — JWT signing key (generate with: `openssl rand -hex 32`)
- `NEURON_SECURITY_JWT_SECRET` — Neuron JWT key

### Start the Stack

```bash
# Start with Pi profile (reduced resources, ARM64 images)
docker compose --profile pi up -d

# Check status
docker compose ps

# View logs
docker compose logs -f app
```

### Verify Deployment

```bash
# Check app health
curl http://localhost:8080/actuator/health

# Check neuron health
curl http://localhost:8090/api/health/live

# Check web UI
curl http://localhost:80/
```

## Step 6.5: Set Up the HTTP Tunnel (Required for Deployment)

**The HTTP tunnel is the only reliable method for executing commands on the Pi.** SSH hangs due to keepalive issues — never use SSH for automated deployment.

### What is the Tunnel?

A Node.js HTTP server running on port 9000 that accepts JSON commands and executes them on the Pi:

- **Endpoint:** `POST http://192.168.1.111:9000/exec`
- **Payload:** `{"command":"your-command-here","timeout":30000}`
- **Response:** `{"code":0,"stdout":"...","stderr":"..."}`

### Install the Tunnel

```bash
# On the Pi, create the tunnel server
sudo -u doramcu -i bash -c 'cat > /tmp/tunnel-server.js << '\''EOF'\''
const http = require("http");
const { exec } = require("child_process");
const server = http.createServer((req, res) => {
  if (req.method === "POST" && req.url === "/exec") {
    let body = "";
    req.on("data", c => body += c);
    req.on("end", () => {
      let data; try { data = JSON.parse(body); } catch(e) { res.writeHead(400); res.end("Invalid JSON"); return; }
      const cmd = data.command || "echo no command";
      const timeout = data.timeout || 30000;
      const child = exec(cmd, { timeout, maxBuffer: 1024 * 1024 });
      let out = "", err = "";
      child.stdout.on("data", d => out += d);
      child.stderr.on("data", d => err += d);
      child.on("close", code => { res.writeHead(200, {"Content-Type":"application/json"}); res.end(JSON.stringify({code, stdout:out, stderr:err})); });
    });
  } else { res.writeHead(404); res.end("Not found"); }
});
server.listen(9000, "0.0.0.0", () => console.log("Tunnel server on port 9000"));
EOF'

# Create the systemd service
sudo tee /etc/systemd/system/doramcu-tunnel.service > /dev/null << 'EOF'
[Unit]
Description=DoraMCU HTTP Tunnel Server
After=network.target

[Service]
Type=simple
ExecStart=/usr/bin/node /tmp/tunnel-server.js
Restart=always
RestartSec=10
StandardOutput=append:/tmp/tunnel.log
StandardError=append:/tmp/tunnel.log
User=doramcu

[Install]
WantedBy=multi-user.target
EOF

# Enable and start
sudo systemctl enable doramcu-tunnel
sudo systemctl start doramcu-tunnel
```

### Test the Tunnel

From your PC (PowerShell):

```powershell
$body = '{"command":"hostname","timeout":5000}'
Invoke-RestMethod -Uri "http://192.168.1.111:9000/exec" -Method POST -ContentType "application/json" -Body $body
```

### Deployment Scripts (Using the Tunnel)

Once the tunnel is running, use the provided deployment scripts:

**Deploy the server JAR:**
```powershell
# Build and deploy the latest JAR
.\scripts\deploy-jar.ps1
```

**Deploy the web client:**
```powershell
# Deploy all web client files
.\scripts\deploy-web-client.ps1
```

Both scripts use the file server + curl method through the tunnel — no SSH required.

## Step 7: Access the Application

- **Web UI**: http://<pi-ip-address>:80
- **API**: http://<pi-ip-address>:8080
- **Neuron**: http://<pi-ip-address>:8090
- **Swagger UI**: http://<pi-ip-address>:8080/swagger-ui.html
- **Actuator**: http://<pi-ip-address>:8080/actuator/health

## Performance Tips

### Memory Optimization

For Pi 4 with 4GB RAM:
```bash
# In .env, set lower memory limits
JVM_XMX=-Xmx512m
```

For Pi 4 with 2GB RAM:
```bash
# Minimal configuration
JVM_XMX=-Xmx256m
```

### Swap Space

The Pi setup script creates 2GB swap by default. For more headroom:

```bash
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
# Add to /etc/fstab for persistence
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### GPU Memory Split

Reduce GPU memory to give more RAM to the system:

```bash
sudo raspi-config
# Advanced Options > Memory Split > Set to 16MB (headless) or 64MB (desktop)
```

## Troubleshooting

### Pi won't boot from USB

- Verify bootloader version: `vcgencmd bootloader_version`
- Check boot order: `vcgencmd bootloader_config`
- Ensure USB drive is USB 3.0 compatible

### Docker containers fail to start

```bash
# Check Docker logs
sudo journalctl -u docker.service

# Check container logs
docker compose logs

# Verify ARM64 architecture
docker info | grep -i arch
```

### Out of memory errors

```bash
# Check memory usage
free -h

# Check swap usage
swapon --show

# Reduce JVM memory in .env
nano .env
# Set JVM_XMX=-Xmx256m
docker compose --profile pi restart app
```

### MySQL won't start

```bash
# Check MySQL logs
docker compose logs mysql

# Increase swap if needed
sudo fallocate -l 4G /swapfile
sudo swapon /swapfile
```

## Maintenance

### Update DoraMCU

```bash
cd ~/DoraMCU-26
git pull
docker compose --profile pi up -d --build
```

### Backup Database

```bash
docker exec doramcu-mysql mysqldump -uroot -p$doramcu_password doramcu > backup_$(date +%F).sql
```

### View Resource Usage

```bash
# Docker stats
docker stats

# System resources
htop
```

## Hardware Recommendations

| Component | Minimum | Recommended |
|---|---|---|
| **Pi Model** | Pi 4 (4GB) | Pi 5 (8GB) |
| **USB Drive** | 32GB USB 3.0 | 64GB USB 3.1 |
| **RAM** | 4GB | 8GB |
| **Cooling** | Passive heatsink | Active fan + heatsink |
| **Power Supply** | 5V/3A USB-C | 5V/5A USB-C (Pi 5) |
| **Network** | Ethernet 100Mbps | Ethernet Gigabit |

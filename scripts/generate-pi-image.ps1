<#
.SYNOPSIS
    Generates a complete Raspberry Pi disk image for DoraMCU game server.

.DESCRIPTION
    This script creates a ready-to-flash .img disk image file for Raspberry Pi 4/5.
    It automates the entire process:
    1. Downloads Raspberry Pi OS 64-bit (Bookworm) Lite
    2. Resizes the root partition to fit all Docker images and project files
    3. Configures OS settings (SSH, Wi-Fi, hostname, GPU memory, swap)
    4. Builds ARM64 Docker images for all DoraMCU services on the PC
    5. Embeds Docker images and project files into the image
    6. Sets up first-boot scripts and systemd service for auto-start

    The output .img file can be written to a USB drive using:
    - Raspberry Pi Imager (recommended): Load as custom image
    - BalenaEtcher: Flash the .img file directly
    - win32diskimager: Write image to disk

    No physical USB drive is needed during image generation.

.PARAMETER OutputPath
    Full path for the output .img file. Default: C:\Users\kenba\DoraMCU\doramcu-pi-image.img

.PARAMETER ImageSizeGB
    Target size of the disk image in GB. Default: 16 (minimum for all services).

.PARAMETER WifiSsid
    Wi-Fi network name for the Pi to connect to.

.PARAMETER WifiPassword
    Wi-Fi password for the network.

.PARAMETER PiHostname
    Hostname for the Pi. Default: 'doramcu-server'.

.PARAMETER SkipImageBuild
    Skip building Docker images (useful for testing the OS image only).

.PARAMETER ImageDownloadUrl
    Custom URL for the Raspberry Pi OS image. Default: latest Bookworm 64-bit Lite.

.EXAMPLE
    .\scripts\generate-pi-image.ps1 -WifiSsid "MyNetwork" -WifiPassword "MyPassword"

.EXAMPLE
    .\scripts\generate-pi-image.ps1 -WifiSsid "OfficeWiFi" -WifiPassword "secret123" -OutputPath "D:\doramcu-pi.img"
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [string]$OutputPath = "",

    [Parameter(Mandatory = $false)]
    [int]$ImageSizeGB = 16,

    [Parameter(Mandatory = $true)]
    [string]$WifiSsid,

    [Parameter(Mandatory = $true)]
    [string]$WifiPassword,

    [Parameter(Mandatory = $false)]
    [string]$PiHostname = 'doramcu-server',

    [Parameter(Mandatory = $false)]
    [switch]$SkipImageBuild,

    [Parameter(Mandatory = $false)]
    [string]$ImageDownloadUrl = ''
)

# ============================================================================
# Configuration
# ============================================================================

$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Definition)

# Use the same drive as the output path for temp files to avoid C: space issues
$tempDrive = [System.IO.Path]::GetPathRoot($OutputPath)
if (-not $tempDrive) { $tempDrive = $env:SystemDrive }
$TempDir = Join-Path $tempDrive "doramcu-pi-image-gen"
$OsImageName = "raspios-bookworm-arm64.img"
$OsImageXz = "raspios-bookworm-arm64.img.xz"

# Default output path
if (-not $OutputPath) {
    $OutputPath = Join-Path $env:USERPROFILE "doramcu-pi-image.img"
}

# Default Raspberry Pi OS 64-bit Bookworm Lite download URL
if (-not $ImageDownloadUrl) {
    $ImageDownloadUrl = "https://downloads.raspberrypi.org/raspios_lite_arm64/images/raspios_lite_arm64-2025-05-07/2025-05-06-raspios-bookworm-arm64-lite.img.xz"
}

# Docker image names
$DockerImages = @(
    @{ Name = "doramcu-app:latest"; Dockerfile = "Dockerfile"; Context = "." },
    @{ Name = "doramcu-neuron:latest"; Dockerfile = "Dockerfile.neuron"; Context = "." },
    @{ Name = "doramcu-nginx:latest"; Dockerfile = "Dockerfile.web"; Context = "." },
    @{ Name = "doramcu-dl:arm64"; Dockerfile = "Dockerfile.dl.arm64"; Context = "." }
)

# ============================================================================
# Helper Functions
# ============================================================================

function Write-Step {
    param([string]$Message, [string]$Status = "INFO")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = switch ($Status) {
        "OK" { "Green" }
        "WARN" { "Yellow" }
        "ERROR" { "Red" }
        default { "Cyan" }
    }
    Write-Host "[$timestamp] [$Status] $Message" -ForegroundColor $color
}

function Write-Section {
    param([string]$Title)
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host "  $Title" -ForegroundColor Cyan
    Write-Host "================================================================" -ForegroundColor Cyan
}

function Test-Administrator {
    $currentUser = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
    return $currentUser.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Test-Prerequisites {
    Write-Section "Checking Prerequisites"

    # Check WSL
    Write-Step "Checking WSL installation..."
    $wslExe = "C:\Windows\System32\wsl.exe"
    if (Test-Path $wslExe) {
        $wslVersion = & $wslExe --version 2>&1 | Out-String
    } else {
        $wslVersion = wsl --version 2>&1 | Out-String
    }
    if ($LASTEXITCODE -ne 0) {
        Write-Step "WSL is not installed. Please install WSL first:" "ERROR"
        Write-Step "  wsl --install" "WARN"
        Write-Step "Then restart this script." "WARN"
        return $false
    }
    Write-Step "WSL is installed" "OK"

    # Check Docker Desktop
    Write-Step "Checking Docker Desktop..."
    $dockerVersion = docker --version 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Docker Desktop is not installed or not running." "ERROR"
        Write-Step "Please install Docker Desktop: https://www.docker.com/products/docker-desktop/" "WARN"
        return $false
    }
    Write-Step "Docker is installed: $dockerVersion" "OK"

    # Check Docker Buildx
    Write-Step "Checking Docker Buildx..."
    $buildxVersion = docker buildx version 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Docker Buildx is not installed." "ERROR"
        Write-Step "Install it: docker buildx install" "WARN"
        return $false
    }
    Write-Step "Docker Buildx is available: $buildxVersion" "OK"

    # Check disk space (need ~20GB for OS image + Docker images + output image)
    Write-Step "Checking disk space..."
    # Check the drive where output will go
    $outputDrive = [System.IO.Path]::GetPathRoot($OutputPath)
    if (-not $outputDrive) { $outputDrive = $env:SystemDrive }
    # Remove trailing backslash for CIM query (DeviceID='D:' not 'D:\')
    $outputDriveLetter = $outputDrive.TrimEnd('\')
    try {
        $outDrive = Get-CimInstance Win32_LogicalDisk -Filter "DeviceID='$outputDriveLetter'" -ErrorAction Stop
        $freeSpaceGB = [math]::Round($outDrive.FreeSpace / 1GB, 1)
    } catch {
        # Fallback: check all drives and use the one with most space
        $allDrives = Get-CimInstance Win32_LogicalDisk -Filter "DriveType=3" -ErrorAction SilentlyContinue
        $maxFree = 0
        foreach ($d in $allDrives) {
            $f = [math]::Round($d.FreeSpace / 1GB, 1)
            if ($f -gt $maxFree) { $maxFree = $f }
        }
        $freeSpaceGB = $maxFree
    }
    if ($freeSpaceGB -lt 30) {
        Write-Step "Insufficient disk space: ${freeSpaceGB}GB free, need at least 30GB." "ERROR"
        return $false
    }
    Write-Step "Disk space OK: ${freeSpaceGB}GB free on $outputDrive" "OK"

    # Check for required tools in WSL
    Write-Step "Checking WSL tools (fdisk, resize2fs, losetup)..."
    $toolCheck = wsl bash -c "command -v fdisk && command -v resize2fs && command -v losetup" 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Some required tools are missing in WSL." "WARN"
        Write-Step "Install them: sudo apt update && sudo apt install -y e2fsprogs util-linux" "WARN"
    } else {
        Write-Step "WSL tools available" "OK"
    }

    return $true
}

function Download-OSImage {
    Write-Section "Downloading Raspberry Pi OS"

    $imageFile = Join-Path $TempDir $OsImageXz
    $imageDir = Join-Path $TempDir "os-image"
    New-Item -ItemType Directory -Path $imageDir -Force | Out-Null

    Write-Step "Downloading Raspberry Pi OS 64-bit Bookworm..."
    Write-Step "URL: $ImageDownloadUrl"

    try {
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $ImageDownloadUrl -OutFile $imageFile -UseBasicParsing
        $ProgressPreference = 'Continue'

        $sizeMB = [math]::Round((Get-Item $imageFile).Length / 1MB, 1)
        Write-Step "Downloaded: ${sizeMB}MB" "OK"
    }
    catch {
        Write-Step "Failed to download OS image: $_" "ERROR"
        Write-Step "You may need to download manually and specify -ImageDownloadUrl" "WARN"
        return $false
    }

    # Decompress if .xz
    if ($imageFile.EndsWith('.xz')) {
        Write-Step "Decompressing image..."
        $imgName = ($imageFile -replace '\.xz$', '').Split('\')[-1]
        $imgPath = Join-Path $imageDir $imgName

        # Ensure output directory exists
        if (-not (Test-Path $imageDir)) {
            New-Item -ItemType Directory -Path $imageDir -Force | Out-Null
        }

        if (Get-Command 7z -ErrorAction SilentlyContinue) {
            7z x -y -o$imageDir $imageFile | Out-Null
        }
        elseif (wsl bash -c "command -v unxz" 2>&1 | Out-String) {
            # Use WSL unxz to decompress - ensure D: is mounted first
            $wslImageFile = $imageFile -replace '([A-Z]):', '/mnt/$1' -replace '\\', '/'
            $wslImageDir = $imageDir -replace '([A-Z]):', '/mnt/$1' -replace '\\', '/'
            $wslImgPath = $imgPath -replace '([A-Z]):', '/mnt/$1' -replace '\\', '/'
            $driveLetter = ($imageFile -replace '([A-Z]):.*', '$1')
            wsl bash -c "sudo mount -t drvfs ${driveLetter}: /mnt/${driveLetter} 2>/dev/null; mkdir -p '$wslImageDir' && unxz -c '$wslImageFile' > '$wslImgPath'"
        }
        else {
            Write-Step "No decompression tool found. Install 7-Zip or ensure WSL has unxz." "ERROR"
            return $false
        }

        $extracted = Get-ChildItem $imageDir -Filter "*.img" | Select-Object -First 1
        if (-not $extracted) {
            $extracted = Get-Item $imgPath -ErrorAction SilentlyContinue
        }
        if ($extracted) {
            $script:OsImagePath = $extracted.FullName
            Write-Step "Image extracted: $($extracted.Name)" "OK"
        }
        else {
            Write-Step "No .img file found after extraction" "ERROR"
            return $false
        }
    }
    else {
        $script:OsImagePath = $imageFile
    }

    return $true
}

function Create-ResizedImage {
    param(
        [string]$SourceImage,
        [string]$TargetImage,
        [int]$SizeGB
    )

    Write-Section "Creating Resized Disk Image"

    Write-Step "Source image: $SourceImage"
    Write-Step "Target size: ${SizeGB}GB"
    Write-Step "Output: $TargetImage"

    # Convert Windows path to WSL path (handles any drive letter)
    function ConvertTo-WslPath {
        param([string]$WinPath)
        $result = $WinPath -replace '\\', '/'
        if ($result -match '^(\w):') {
            $drive = $Matches[1].ToLower()
            $result = $result -replace "^${drive}:", "/mnt/$drive"
        }
        return $result
    }
    $wslSource = ConvertTo-WslPath $SourceImage
    $wslTarget = ConvertTo-WslPath $TargetImage

    # Use a comprehensive WSL script to resize the image
    $resizeScript = @"
#!/bin/bash
set -e

SOURCE="$wslSource"
TARGET="$wslTarget"
SIZE_GB=$SizeGB

echo "=== Step 1: Create empty target image ==="
dd if=/dev/zero of="\$TARGET" bs=1M count=1 seek=\$((SIZE_GB * 1024)) status=progress 2>&1
echo "Target image created: \$(du -h "\$TARGET" | cut -f1)"

echo ""
echo "=== Step 2: Copy source image to target ==="
SOURCE_SIZE=\$(du -b "\$SOURCE" | cut -f1)
dd if="\$SOURCE" of="\$TARGET" bs=4M status=progress conv=notrunc 2>&1
echo "Source copied to target"

echo ""
echo "=== Step 3: Setup loop device ==="
# Remove any existing loop devices
sudo losetup -j "\$TARGET" 2>/dev/null | cut -d: -f1 | xargs -r sudo losetup -d 2>/dev/null || true

LOOP=\$(sudo losetup --find --show --partscan "\$TARGET")
echo "Loop device: \$LOOP"

# Wait for partition devices to appear
sleep 2
PART_BOOT="\${LOOP}p1"
PART_ROOT="\${LOOP}p2"

echo "Boot partition: \$PART_BOOT"
echo "Root partition: \$PART_ROOT"

echo ""
echo "=== Step 4: Show current partition layout ==="
sudo fdisk -l "\$LOOP" | grep -E "Device|Boot|Linux"

echo ""
echo "=== Step 5: Resize root partition ==="
# Delete partition 2 and recreate it to fill the disk
# First, save the start sector of partition 2
PART2_START=\$(sudo fdisk -l "\$LOOP" | grep "\${LOOP}p2" | awk '{print \$2}')
echo "Root partition starts at sector: \$PART2_START"

# Use sfdisk to resize - delete partition 2 and recreate without end sector
echo "Resizing partition 2 to fill disk..."
# Get partition 1 info to preserve it
PART1_START=\$(sudo fdisk -l "\$LOOP" | grep "\${LOOP}p1" | awk '{print \$2}')
PART1_END=\$(sudo fdisk -l "\$LOOP" | grep "\${LOOP}p1" | awk '{print \$3}')
PART1_TYPE=\$(sudo fdisk -l "\$LOOP" | grep "\${LOOP}p1" | awk '{print \$NF}')

# Create sfdisk input
cat <<SFDISK_INPUT | sudo sfdisk "\$LOOP"
label: dos
label-id: 0x00000000
device: \$LOOP
unit: sectors

\$LOOP1 : start=\$PART1_START, size=\$((PART1_END - PART1_START + 1)), type=0C, bootable
\$LOOP2 : start=\$PART2_START, type=83
SFDISK_INPUT

echo "Partition table updated"

echo ""
echo "=== Step 6: Resize filesystem ==="
# Force kernel to re-read partition table
sudo partprobe "\$LOOP" 2>/dev/null || true
sleep 1

# Resize the ext4 filesystem
echo "Resizing ext4 filesystem..."
sudo resize2fs "\$PART_ROOT"
echo "Filesystem resized"

echo ""
echo "=== Step 7: Verify result ==="
sudo fdisk -l "\$LOOP" | grep -E "Device|Boot|Linux"
echo ""
echo "Filesystem info:"
sudo dumpe2fs -h "\$PART_ROOT" 2>/dev/null | grep -E "Block count|Block size|Free blocks"

echo ""
echo "=== Step 8: Cleanup loop device ==="
sudo losetup -d "\$LOOP" 2>/dev/null || true
echo "Loop device detached"

echo ""
echo "=== Image resize complete ==="
echo "Output: \$TARGET"
du -h "\$TARGET" | cut -f1
"@

    # Write the script to a temp file
    $scriptFile = Join-Path $TempDir "resize-image.sh"
    [System.IO.File]::WriteAllText($scriptFile, $resizeScript)

    Write-Step "Running resize script in WSL..."
    $wslScript = ConvertTo-WslPath $scriptFile

    try {
        $output = wsl bash "$wslScript" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Step "Resize script failed:" "ERROR"
            $output | ForEach-Object { Write-Step "  $_" "ERROR" }
            return $false
        }
        Write-Step "Image resized successfully!" "OK"
    }
    catch {
        Write-Step "WSL resize failed: $_" "ERROR"
        return $false
    }

    return $true
}

function Configure-ImagePartitions {
    param([string]$ImagePath)

    Write-Section "Configuring Image Partitions"

    # Convert PowerShell paths to WSL paths BEFORE embedding in bash script
    $wslImage = ConvertTo-WslPath $ImagePath
    $wslProjectRoot = ConvertTo-WslPath $ProjectRoot
    $wslScriptRoot = ConvertTo-WslPath $PSScriptRoot

    # Comprehensive configuration script
    $configScript = @"
#!/bin/bash
set -e

IMAGE="$wslImage"
WSL_PROJECT="$wslProjectRoot"
WSL_SCRIPTS="$wslScriptRoot"
MNT_BASE="/tmp/doramcu-img"
MNT_BOOT="\$MNT_BASE/boot"
MNT_ROOT="\$MNT_BASE/root"

echo "=== Setup mount points ==="
rm -rf "\$MNT_BASE"
mkdir -p "\$MNT_BOOT" "\$MNT_ROOT"

echo "=== Attach loop device ==="
sudo losetup -j "\$IMAGE" 2>/dev/null | cut -d: -f1 | xargs -r sudo losetup -d 2>/dev/null || true
LOOP=\$(sudo losetup --find --show --partscan "\$IMAGE")
echo "Loop device: \$LOOP"
sleep 2

PART_BOOT="\${LOOP}p1"
PART_ROOT="\${LOOP}p2"

echo "=== Mount partitions ==="
sudo mount -t vfat "\$PART_BOOT" "\$MNT_BOOT"
sudo mount -t ext4 "\$PART_ROOT" "\$MNT_ROOT"
echo "Partitions mounted"

echo ""
echo "=== Configure Boot Partition (FAT32) ==="

# 1. Enable SSH
sudo touch "\$MNT_BOOT/ssh"
echo "SSH enabled"

# 2. Wi-Fi configuration
sudo bash -c "cat > \"\$MNT_BOOT/wpa_supplicant.conf\" <<'WPA_EOF'
country=US
ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1

WPA_EOF"

# Add the network with proper escaping
sudo bash -c "cat >> \"\$MNT_BOOT/wpa_supplicant.conf\" <<WPA_NET
network={
    ssid="${WifiSsid}"
    psk="${WifiPassword}"
    key_mgmt=WPA-PSK
}
WPA_NET"
echo "Wi-Fi configured for: ${WifiSsid}"

# 3. Hostname configuration via usercfg.txt
sudo bash -c "cat > \"\$MNT_BOOT/usercfg.txt\" <<USERCFG
[pi]
username=pi
password=raspberry
hostname=${PiHostname}
USERCFG"
echo "Hostname configured: ${PiHostname}"

# 4. config.txt additions
CONFIG_FILE="\$MNT_BOOT/config.txt"
if [ -f "\$CONFIG_FILE" ]; then
    # Check if DoraMCU section already exists
    if ! grep -q "DoraMCU Game Server" "\$CONFIG_FILE"; then
        sudo bash -c "cat >> \"\$CONFIG_FILE\" <<'CONFIG_ADD'

# DoraMCU Game Server Configuration
# Reduce GPU memory to maximize RAM for Docker containers
gpu_mem=16

# Enable UART for serial console access
enable_uart=1
CONFIG_ADD"
        echo "config.txt updated"
    else
        echo "config.txt already has DoraMCU config"
    fi
else
    echo "config.txt not found - creating"
    sudo bash -c "cat > \"\$CONFIG_FILE\" <<'CONFIG_NEW'
# DoraMCU Game Server Configuration
gpu_mem=16
enable_uart=1
CONFIG_NEW
fi

echo "Boot partition configuration complete"

echo ""
echo "=== Configure Root Partition (ext4) ==="

# Create DoraMCU directory structure
DORA_DIR="\$MNT_ROOT/home/pi/doramcu"
sudo mkdir -p "\$DORA_DIR"
echo "Created: \$DORA_DIR"

# 1. Copy docker-compose.yml
if [ -f "\$WSL_PROJECT/docker-compose.yml" ]; then
    sudo cp "\$WSL_PROJECT/docker-compose.yml" "\$DORA_DIR/docker-compose.yml"
    echo "Copied docker-compose.yml"
else
    echo "WARNING: docker-compose.yml not found at \$WSL_PROJECT"
fi

# 2. Copy Dockerfiles
for df in Dockerfile Dockerfile.neuron Dockerfile.web Dockerfile.dl.arm64; do
    if [ -f "\$WSL_PROJECT/\$df" ]; then
        sudo cp "\$WSL_PROJECT/\$df" "\$DORA_DIR/\$df"
        echo "Copied \$df"
    else
        echo "WARNING: \$df not found"
    fi
done

# 3. Copy dora-dl-python directory
if [ -d "\$WSL_PROJECT/dora-dl-python" ]; then
    sudo cp -r "\$WSL_PROJECT/dora-dl-python" "\$DORA_DIR/dora-dl-python"
    echo "Copied dora-dl-python"
fi

# 4. Copy dora-web directory
if [ -d "\$WSL_PROJECT/dora-web" ]; then
    sudo cp -r "\$WSL_PROJECT/dora-web" "\$DORA_DIR/dora-web"
    echo "Copied dora-web"
fi

# 5. Create .env file
sudo bash -c "cat > \"\$DORA_DIR/.env\" <<'ENV_FILE'
# DoraMCU Environment Variables
# Generated by generate-pi-image.ps1

# Neuron JWT Secret (generate a random one: openssl rand -hex 32)
NEURON_SECURITY_JWT_SECRET=$(openssl rand -hex 32)

# Database settings
MYSQL_ROOT_PASSWORD=doramcu_root_password_change_me
MYSQL_DATABASE=doramcu
MYSQL_USER=doramcu
MYSQL_PASSWORD=doramcu_password_change_me

# DL mode: rest = offload all DL to Python service (recommended for Pi)
DL_MODE=rest
ENV_FILE"
echo "Created .env file"

# 6. Copy first-boot script
if [ -f "\$WSL_SCRIPTS/pi-first-boot.sh" ]; then
    sudo mkdir -p "\$MNT_ROOT/boot/firmware"
    sudo cp "\$WSL_SCRIPTS/pi-first-boot.sh" "\$MNT_ROOT/boot/firmware/first-boot.sh"
    sudo chmod +x "\$MNT_ROOT/boot/firmware/first-boot.sh"
    echo "Copied first-boot.sh"
else
    echo "WARNING: pi-first-boot.sh not found"
fi

# 7. Copy systemd service file
if [ -f "\$WSL_SCRIPTS/pi-doramcu.service" ]; then
    sudo cp "\$WSL_SCRIPTS/pi-doramcu.service" "\$MNT_ROOT/etc/systemd/system/doramcu.service"
    echo "Installed doramcu.service"
fi

# 8. Copy test runner script
if [ -f "\$WSL_SCRIPTS/pi-test-runner.sh" ]; then
    sudo cp "\$WSL_SCRIPTS/pi-test-runner.sh" "\$DORA_DIR/pi-test-runner.sh"
    sudo chmod +x "\$DORA_DIR/pi-test-runner.sh"
    echo "Copied pi-test-runner.sh"
fi

# 9. Enable systemd service (create symlink manually for chroot)
SYSTEMD_TARGET="\$MNT_ROOT/etc/systemd/system/multi-user.target.wants"
sudo mkdir -p "\$SYSTEMD_TARGET"
sudo ln -sf /etc/systemd/system/doramcu.service "\$SYSTEMD_TARGET/doramcu.service"
echo "Enabled doramcu.service for auto-start"

# 10. Configure swap size in rootfs
SWAP_CONF="\$MNT_ROOT/etc/dphys-swapfile"
if [ -f "\$SWAP_CONF" ]; then
    sudo sed -i 's/CONF_SWAPSIZE=.*/CONF_SWAPSIZE=2048/' "\$SWAP_CONF"
    echo "Swap configured to 2GB in rootfs"
fi

# 11. Set hostname in rootfs
if [ -f "\$MNT_ROOT/etc/hostname" ]; then
    echo "${PiHostname}" | sudo tee "\$MNT_ROOT/etc/hostname" > /dev/null
    echo "Hostname set in /etc/hostname"
fi

if [ -f "\$MNT_ROOT/etc/hosts" ]; then
    sudo sed -i "s/127.0.1.1.*/127.0.1.1\t${PiHostname}/" "\$MNT_ROOT/etc/hosts"
    echo "Hostname updated in /etc/hosts"
fi

# 12. Create setup info file
sudo bash -c "cat > \"\$MNT_BOOT/doramcu-setup-info.txt\" <<INFO_FILE
DoraMCU Pi Disk Image
Hostname: ${PiHostname}
Wi-Fi: ${WifiSsid}
Generated: $(date '+%Y-%m-%d %H:%M:%S')
Image Size: ${ImageSizeGB}GB
INFO_FILE"
echo "Created setup info"

echo ""
echo "=== Unmount partitions ==="
sudo umount "\$MNT_ROOT"
sudo umount "\$MNT_BOOT"
echo "Partitions unmounted"

echo ""
echo "=== Detach loop device ==="
sudo losetup -d "\$LOOP" 2>/dev/null || true
echo "Loop device detached"

echo ""
echo "=== Configuration complete ==="
"@

    # Write the script
    $scriptFile = Join-Path $TempDir "configure-image.sh"
    [System.IO.File]::WriteAllText($scriptFile, $configScript)

    Write-Step "Running configuration script in WSL..."
    $wslScript = ConvertTo-WslPath $scriptFile

    try {
        $output = wsl bash "$wslScript" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Step "Configuration script failed:" "ERROR"
            $output | ForEach-Object { Write-Step "  $_" "ERROR" }
            return $false
        }
        Write-Step "Image configuration complete!" "OK"
    }
    catch {
        Write-Step "WSL configuration failed: $_" "ERROR"
        return $false
    }

    return $true
}

function Build-DockerImages {
    Write-Section "Building ARM64 Docker Images"

    if ($SkipImageBuild) {
        Write-Step "Skipping image build (user requested)" "WARN"
        return $true
    }

    # Check if buildx builder exists
    Write-Step "Setting up Docker Buildx for ARM64..."
    docker buildx create --use --name arm64-builder --driver docker-container 2>&1 | Out-Null
    docker buildx inspect --bootstrap 2>&1 | Out-Null
    Write-Step "Buildx builder ready" "OK"

    # Build each image
    foreach ($img in $DockerImages) {
        Write-Step "Building: $($img.Name)"
        Write-Step "  Dockerfile: $($img.Dockerfile)"

        $dockerfilePath = Join-Path $ProjectRoot $img.Dockerfile
        if (-not (Test-Path $dockerfilePath)) {
            Write-Step "  Dockerfile not found: ${dockerfilePath}" "ERROR"
            continue
        }

        $buildArgs = @(
            "build",
            "--platform", "linux/arm64",
            "--load",
            "-t", $img.Name,
            "-f", $img.Dockerfile,
            $img.Context
        )

        Write-Step "  Running: docker buildx $($buildArgs -join ' ')"
        $output = docker buildx $buildArgs 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Step "  Build failed: $($output -join '`n')" "ERROR"
            continue
        }

        # Check image size
        $size = docker image inspect $img.Name --format='{{.Size}}' 2>&1 | Out-String
        $sizeMB = [math]::Round([int64]$size / 1MB, 1)
        Write-Step "  Built: ${sizeMB}MB" "OK"
    }

    # Save all images to a single tar file
    Write-Step "Saving all images to tar file..."
    $tarFile = Join-Path $TempDir "doramcu-images.tar"
    $imageNames = $DockerImages | ForEach-Object { $_.Name }
    docker save -o $tarFile $imageNames 2>&1 | Out-Null

    if (Test-Path $tarFile) {
        $tarSizeGB = [math]::Round((Get-Item $tarFile).Length / 1GB, 2)
        Write-Step "Images saved: ${tarSizeGB}GB" "OK"
        $script:ImagesTarPath = $tarFile
    }
    else {
        Write-Step "Failed to save images" "ERROR"
        return $false
    }

    return $true
}

function Embed-DockerImages {
    param([string]$ImagePath)

    Write-Section "Embedding Docker Images into Disk Image"

    if (-not $script:ImagesTarPath) {
        Write-Step "No Docker images to embed (skipped)" "WARN"
        return $true
    }

    $tarSizeGB = [math]::Round((Get-Item $script:ImagesTarPath).Length / 1GB, 2)
    Write-Step "Docker images tar: ${tarSizeGB}GB"

    $wslImage = ConvertTo-WslPath $ImagePath
    $wslTar = ConvertTo-WslPath $script:ImagesTarPath

    $embedScript = @"
#!/bin/bash
set -e

IMAGE="$wslImage"
TAR="$wslTar"
MNT_BASE="/tmp/doramcu-embed"
MNT_ROOT="\$MNT_BASE/root"

echo "=== Mount root partition ==="
rm -rf "\$MNT_BASE"
mkdir -p "\$MNT_ROOT"

sudo losetup -j "\$IMAGE" 2>/dev/null | cut -d: -f1 | xargs -r sudo losetup -d 2>/dev/null || true
LOOP=\$(sudo losetup --find --show --partscan "\$IMAGE")
sleep 2

PART_ROOT="\${LOOP}p2"
sudo mount -t ext4 "\$PART_ROOT" "\$MNT_ROOT"
echo "Root partition mounted"

echo ""
echo "=== Copy Docker images to image ==="
DORA_DIR="\$MNT_ROOT/home/pi/doramcu"
sudo cp "\$TAR" "\$DORA_DIR/doramcu-images.tar"
echo "Docker images embedded: \$(sudo du -h "\$DORA_DIR/doramcu-images.tar" | cut -f1)"

echo ""
echo "=== Unmount and cleanup ==="
sudo umount "\$MNT_ROOT"
sudo losetup -d "\$LOOP" 2>/dev/null || true
echo "Done"
"@

    $scriptFile = Join-Path $TempDir "embed-images.sh"
    [System.IO.File]::WriteAllText($scriptFile, $embedScript)

    Write-Step "Embedding Docker images into disk image..."
    $wslScript = ConvertTo-WslPath $scriptFile

    try {
        $output = wsl bash "$wslScript" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Step "Embed script failed:" "ERROR"
            $output | ForEach-Object { Write-Step "  $_" "ERROR" }
            return $false
        }
        Write-Step "Docker images embedded successfully!" "OK"
    }
    catch {
        Write-Step "WSL embed failed: $_" "ERROR"
        return $false
    }

    return $true
}

function Show-Summary {
    param([string]$ImagePath)

    Write-Section "Image Generation Complete!"

    $imgSizeGB = [math]::Round((Get-Item $ImagePath).Length / 1GB, 2)
    $imgSizeMB = [math]::Round((Get-Item $ImagePath).Length / 1MB, 1)

    Write-Host ""
    Write-Host "Disk image generated successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Output file:" -ForegroundColor Cyan
    Write-Host "  Path: $ImagePath" -ForegroundColor White
    Write-Host "  Size: ${imgSizeGB}GB (${imgSizeMB}MB)" -ForegroundColor White
    Write-Host ""
    Write-Host "Configuration:" -ForegroundColor Cyan
    Write-Host "  Hostname: ${PiHostname}" -ForegroundColor White
    Write-Host "  Wi-Fi: ${WifiSsid}" -ForegroundColor White
    Write-Host "  Docker images: $(if ($SkipImageBuild) { 'Skipped' } else { 'Included' })" -ForegroundColor White
    Write-Host ""
    Write-Host "Next steps - Write to USB drive:" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "  Option 1: Raspberry Pi Imager (Recommended)" -ForegroundColor Yellow
    Write-Host "    1. Download: https://www.raspberrypi.com/software/" -ForegroundColor Gray
    Write-Host "    2. Click OS -> Choose OS -> Use custom -> Select the .img file" -ForegroundColor Gray
    Write-Host "    3. Click Storage -> Select your USB drive" -ForegroundColor Gray
    Write-Host "    4. Click Write" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  Option 2: BalenaEtcher" -ForegroundColor Yellow
    Write-Host "    1. Download: https://etcher.balena.io/" -ForegroundColor Gray
    Write-Host "    2. Flash from file -> Select the .img file" -ForegroundColor Gray
    Write-Host "    3. Select target -> Your USB drive" -ForegroundColor Gray
    Write-Host "    4. Flash!" -ForegroundColor Gray
    Write-Host ""
    Write-Host "  Option 3: Win32 Disk Imager" -ForegroundColor Yellow
    Write-Host "    1. Download: https://sourceforge.net/projects/win32diskimager/" -ForegroundColor Gray
    Write-Host "    2. Select image file and device" -ForegroundColor Gray
    Write-Host "    3. Write" -ForegroundColor Gray
    Write-Host ""
    Write-Host "After writing to USB:" -ForegroundColor Cyan
    Write-Host "  1. Enable USB boot on your Pi (if not already enabled)" -ForegroundColor White
    Write-Host "  2. Plug USB into Pi and power on" -ForegroundColor White
    Write-Host "  3. Wait ~10 minutes for first boot" -ForegroundColor White
    Write-Host "  4. Access at: http://${PiHostname}.local or http://<pi-ip>" -ForegroundColor White
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Cyan
    Write-Host "  - SSH: ssh pi@${PiHostname}.local (password: raspberry)" -ForegroundColor Gray
    Write-Host "  - First boot log: /var/log/first-boot.log" -ForegroundColor Gray
    Write-Host "  - Service status: docker compose --profile pi ps" -ForegroundColor Gray
    Write-Host "  - Logs: docker compose --profile pi logs -f" -ForegroundColor Gray
    Write-Host ""
}

# ============================================================================
# Main Execution
# ============================================================================

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  DoraMCU Raspberry Pi Disk Image Generator" -ForegroundColor Cyan
Write-Host "  Creates a ready-to-flash .img file" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Note: Admin privileges are not required. WSL commands use sudo internally.

# Check prerequisites
if (-not (Test-Prerequisites)) {
    Write-Step "Prerequisites check failed. Please fix the issues above and try again." "ERROR"
    exit 1
}

# Create temp directory
New-Item -ItemType Directory -Path $TempDir -Force | Out-Null

# Step 1: Download OS image
$script:OsImagePath = $null
if (-not (Download-OSImage)) {
    Write-Step "OS image download failed." "ERROR"
    exit 1
}

# Step 2: Create resized image
Write-Step "Output image: $OutputPath"
if (-not (Create-ResizedImage -SourceImage $script:OsImagePath -TargetImage $OutputPath -SizeGB $ImageSizeGB)) {
    Write-Step "Image resize failed." "ERROR"
    exit 1
}

# Step 3: Configure partitions (SSH, Wi-Fi, hostname, project files)
if (-not (Configure-ImagePartitions -ImagePath $OutputPath)) {
    Write-Step "Image configuration failed." "ERROR"
    exit 1
}

# Step 4: Build Docker images
$script:ImagesTarPath = $null
if (-not (Build-DockerImages)) {
    Write-Step "Docker image build failed." "ERROR"
    Write-Step "The disk image was created without Docker images." "WARN"
    Write-Step "You can build and load them manually on the Pi." "WARN"
}

# Step 5: Embed Docker images into the disk image
if ($script:ImagesTarPath) {
    if (-not (Embed-DockerImages -ImagePath $OutputPath)) {
        Write-Step "Docker image embedding failed." "ERROR"
        Write-Step "The disk image was created, but Docker images may not be included." "WARN"
    }
}

# Show summary
Show-Summary -ImagePath $OutputPath

# Cleanup temp files (but keep the output image)
Write-Step "Cleaning up temporary files..."
$itemsToRemove = Get-ChildItem $TempDir -ErrorAction SilentlyContinue
if ($itemsToRemove) {
    Remove-Item -Path $TempDir -Recurse -Force -ErrorAction SilentlyContinue
}
Write-Step "Done! Your disk image is ready at: $OutputPath" "OK"

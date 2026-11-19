#Requires -RunAsAdministrator
<#
.SYNOPSIS
    Clones a Raspberry Pi SD card to a ready-to-flash .img disk image for DoraMCU.

.DESCRIPTION
    This script clones an existing Raspberry Pi SD card to a .img file, then:
    1. Clones the SD card (Disk 2) to a compact .img file
    2. Resizes the image to the target size (default 16GB)
    3. Configures OS settings (SSH, Wi-Fi, hostname, GPU memory, swap)
    4. Copies DoraMCU project files into the image
    5. Builds ARM64 Docker images for all DoraMCU services
    6. Embeds Docker images into the image
    7. Sets up first-boot scripts and systemd service

.PARAMETER OutputPath
    Full path for the output .img file.

.PARAMETER ImageSizeGB
    Target size of the disk image in GB. Default: 16.

.PARAMETER WifiSsid
    Wi-Fi network name for the Pi.

.PARAMETER WifiPassword
    Wi-Fi password.

.PARAMETER PiHostname
    Hostname for the Pi. Default: 'doramcu-server'.

.PARAMETER SkipImageBuild
    Skip building Docker images.

.PARAMETER DiskNumber
    Physical disk number of the SD card. Auto-detected if not specified.

.EXAMPLE
    .\scripts\clone-sd-to-image.ps1 -WifiSsid "MyNetwork" -WifiPassword "MyPassword"
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
    [int]$DiskNumber = -1
)

# ============================================================================
# Configuration
# ============================================================================

$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Definition)
$TempDir = Join-Path $env:TEMP "doramcu-pi-clone"

# Default output path
if (-not $OutputPath) {
    $OutputPath = Join-Path $env:USERPROFILE "doramcu-pi-image.img"
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

function ConvertTo-WslPath {
    param([string]$WinPath)
    $result = $WinPath -replace '\\', '/'
    if ($result -match '^(\w):') {
        $drive = $Matches[1].ToLower()
        $result = $result -replace "^${drive}:", "/mnt/$drive"
    }
    return $result
}

function Test-Administrator {
    $currentUser = New-Object Security.Principal.WindowsPrincipal([Security.Principal.WindowsIdentity]::GetCurrent())
    return $currentUser.IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
}

function Find-SDCard {
    <#
    .SYNOPSIS
        Find the SD card disk number by looking for a disk with a FAT32 "bootfs" partition.
    #>
    Write-Step "Searching for SD card..."

    # Look for disk with bootfs volume
    $volumes = Get-Volume -FileSystemLabel "bootfs" -ErrorAction SilentlyContinue
    if ($volumes) {
        foreach ($vol in $volumes) {
            $diskNum = $vol.DiskNumber
            if ($diskNum -ne $null) {
                Write-Step "Found SD card on Disk $diskNum (bootfs volume)" "OK"
                return $diskNum
            }
        }
    }

    # Fallback: look for USB disk with FAT32 partition ~512MB
    $disks = Get-PhysicalDisk | Where-Object { $_.BusType -eq 'USB' -or $_.BusType -eq 'SD' }
    foreach ($disk in $disks) {
        $diskNum = $disk.DeviceId
        $partitions = Get-Partition -DiskNumber $diskNum -ErrorAction SilentlyContinue
        foreach ($part in $partitions) {
            $vol = Get-Volume -Partition $part -ErrorAction SilentlyContinue
            if ($vol -and $vol.FileSystem -eq 'FAT32' -and $vol.Size -gt 0 -and $vol.Size -lt 1GB) {
                Write-Step "Found SD card on Disk $diskNum (FAT32 partition ~$([math]::Round($vol.Size/1MB,0))MB)" "OK"
                return $diskNum
            }
        }
    }

    Write-Step "SD card not found automatically." "ERROR"
    Write-Step "Please specify -DiskNumber manually." "WARN"
    return -1
}

function Test-Prerequisites {
    Write-Section "Checking Prerequisites"

    # Check admin
    Write-Step "Checking administrator privileges..."
    if (-not (Test-Administrator)) {
        Write-Step "This script must be run as Administrator." "ERROR"
        return $false
    }
    Write-Step "Running as Administrator" "OK"

    # Check WSL
    Write-Step "Checking WSL..."
    $wslCheck = wsl --version 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        Write-Step "WSL is not installed." "ERROR"
        return $false
    }
    Write-Step "WSL is installed" "OK"

    # Check Docker
    Write-Step "Checking Docker..."
    $dockerVersion = docker --version 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Docker is not installed or not running." "ERROR"
        return $false
    }
    Write-Step "Docker: $dockerVersion" "OK"

    # Check disk space
    Write-Step "Checking disk space..."
    $outputDrive = [System.IO.Path]::GetPathRoot($OutputPath)
    if (-not $outputDrive) { $outputDrive = $env:SystemDrive }
    try {
        $outDrive = Get-CimInstance Win32_LogicalDisk -Filter "DeviceID='$outputDrive'" -ErrorAction Stop
        $freeSpaceGB = [math]::Round($outDrive.FreeSpace / 1GB, 1)
    } catch {
        $freeSpaceGB = 9999
    }
    if ($freeSpaceGB -lt 30) {
        Write-Step "Insufficient disk space: ${freeSpaceGB}GB free, need at least 30GB." "ERROR"
        return $false
    }
    Write-Step "Disk space OK: ${freeSpaceGB}GB free on $outputDrive" "OK"

    return $true
}

# ============================================================================
# Clone SD Card
# ============================================================================

function Clone-SDCard {
    param(
        [int]$DiskNumber,
        [string]$TargetImage
    )

    Write-Section "Cloning SD Card (Disk $DiskNumber)"

    # Get disk info
    $disk = Get-PhysicalDisk -DeviceNumber $DiskNumber -ErrorAction SilentlyContinue
    if (-not $disk) {
        Write-Step "Disk $DiskNumber not found!" "ERROR"
        return $false
    }

    $diskSizeGB = [math]::Round($disk.Size / 1GB, 1)
    Write-Step "Disk: $($disk.FriendlyName)"
    Write-Step "Size: ${diskSizeGB}GB"
    Write-Step "BusType: $($disk.BusType)"

    # Get partition layout to determine actual used space
    $partitions = Get-Partition -DiskNumber $DiskNumber
    $lastPartitionEnd = 0
    foreach ($part in $partitions) {
        $partEnd = $part.StartOffset + $part.Size
        if ($partEnd -gt $lastPartitionEnd) {
            $lastPartitionEnd = $partEnd
        }
    }
    $partitionEndSizeGB = [math]::Ceiling($lastPartitionEnd / 1GB)

    # Pi OS Lite typically uses 2-4GB even on large partitions.
    # We can't determine exact ext4 usage from Windows, so clone a reasonable amount.
    # We'll shrink the filesystem in WSL after cloning if needed.
    $cloneSizeGB = 8  # Clone 8GB - enough for Pi OS Lite + buffer

    # If partition is smaller than our default, use partition size
    if ($partitionEndSizeGB -lt $cloneSizeGB) {
        $cloneSizeGB = [math]::Max($partitionEndSizeGB + 1, 4)
    }

    Write-Step "Partition end: ${partitionEndSizeGB}GB (ext4 partition may be larger than actual data)"
    Write-Step "Cloning ${cloneSizeGB}GB (will shrink filesystem in WSL if needed)"

    # The raw disk path
    $rawDisk = "\\.\PhysicalDrive$DiskNumber"

    Write-Step "Source: $rawDisk"
    Write-Step "Target: $TargetImage"

    # Clone using PowerShell - read raw disk in chunks and write to image file
    Write-Step "Starting clone (this may take a few minutes)..."
    $bufferSize = 1MB  # 1MB chunks
    $totalBytes = $cloneSizeGB * 1024 * 1024 * 1024
    $bytesRead = 0
    $startTime = Get-Date

    try {
        # Open raw disk for reading
        $diskStream = New-Object System.IO.FileStream(
            $rawDisk,
            [System.IO.FileMode]::Open,
            [System.IO.FileAccess]::Read,
            [System.IO.FileShare]::Read,
            $bufferSize,
            [System.IO.FileOptions]::None
        )

        # Create output file
        $fileStream = New-Object System.IO.FileStream(
            $TargetImage,
            [System.IO.FileMode]::Create,
            [System.IO.FileAccess]::Write,
            [System.IO.FileShare]::None,
            $bufferSize,
            [System.IO.FileOptions]::WriteThrough
        )

        $buffer = New-Object byte[] $bufferSize
        $totalRead = 0

        while ($totalRead -lt $totalBytes) {
            # Calculate how many bytes to read this iteration
            $remaining = $totalBytes - $totalRead
            $readSize = if ($remaining -lt $bufferSize) { $remaining } else { $bufferSize }

            # Resize buffer if needed for last chunk
            if ($readSize -lt $bufferSize) {
                $buffer = New-Object byte[] $readSize
            }

            $read = $diskStream.Read($buffer, 0, $readSize)
            if ($read -eq 0) {
                break
            }

            $fileStream.Write($buffer, 0, $read)
            $totalRead += $read

            # Progress reporting every 256MB
            if ($totalRead - $bytesRead -ge 256MB) {
                $bytesRead = $totalRead
                $percent = [math]::Round($totalRead / $totalBytes * 100, 1)
                $elapsed = (Get-Date) - $startTime
                $speedMBps = [math]::Round($totalRead / 1MB / $elapsed.TotalSeconds, 1)
                Write-Step "  ${percent}% ($([math]::Round($totalRead/1GB,1))/${cloneSizeGB}GB) @ ${speedMBps} MB/s" "INFO"
            }
        }

        $diskStream.Close()
        $fileStream.Close()

        $elapsed = (Get-Date) - $startTime
        $speedMBps = [math]::Round($totalRead / 1MB / $elapsed.TotalSeconds, 1)
        Write-Step "Clone complete: $([math]::Round($totalRead/1GB,2))GB in $($elapsed.Minutes)m$($elapsed.Seconds)s @ ${speedMBps} MB/s" "OK"
    }
    catch {
        Write-Step "Clone failed: $_" "ERROR"
        if ($diskStream) { $diskStream.Close() }
        if ($fileStream) { $fileStream.Close() }
        # Clean up partial file
        if (Test-Path $TargetImage) {
            Remove-Item $TargetImage -Force -ErrorAction SilentlyContinue
        }
        return $false
    }

    return $true
}

# ============================================================================
# Resize Image
# ============================================================================

function Create-ResizedImage {
    param(
        [string]$ImagePath,
        [int]$SizeGB
    )

    Write-Section "Resizing Image to ${SizeGB}GB"

    $wslImage = ConvertTo-WslPath $ImagePath

    $resizeScript = @"
#!/bin/bash
set -e

IMAGE="$wslImage"
SIZE_GB=$SizeGB

echo "=== Step 1: Setup loop device ==="
sudo losetup -j "\$IMAGE" 2>/dev/null | cut -d: -f1 | xargs -r sudo losetup -d 2>/dev/null || true
LOOP=\$(sudo losetup --find --show --partscan "\$IMAGE")
echo "Loop device: \$LOOP"
sleep 2

PART_BOOT="\${LOOP}p1"
PART_ROOT="\${LOOP}p2"

echo ""
echo "=== Step 2: Check filesystem ==="
# The cloned image may have a truncated ext4 filesystem (cloned 8GB of 238GB partition)
# Need to check and repair first
echo "Running filesystem check..."
sudo e2fsck -f -y "\$PART_ROOT" 2>&1 || true
echo ""

# Check actual filesystem size vs partition size
FS_BLOCK_COUNT=\$(sudo dumpe2fs -h "\$PART_ROOT" 2>/dev/null | grep "Block count:" | awk '{print \$3}')
FS_BLOCK_SIZE=\$(sudo dumpe2fs -h "\$PART_ROOT" 2>/dev/null | grep "Block size:" | awk '{print \$3}')
FS_SIZE_BYTES=\$((FS_BLOCK_COUNT * FS_BLOCK_SIZE))

# Get partition size from partition table
PART2_START=\$(sudo fdisk -l "\$LOOP" | grep "\${LOOP}p2" | awk '{print \$2}')
PART2_SECTORS=\$(sudo fdisk -l "\$LOOP" | grep "\${LOOP}p2" | awk '{print \$4}')
if [ -z "\$PART2_SECTORS" ] || [ "\$PART2_SECTORS" = "" ]; then
    # Partition may be truncated - get actual image size
    IMAGE_SIZE=\$(stat -c%s "\$IMAGE")
    PART2_SIZE_BYTES=\$((IMAGE_SIZE - PART2_START * 512))
else
    PART2_SIZE_BYTES=\$((PART2_SECTORS * 512))
fi

echo "Filesystem size: \$(echo "\$FS_SIZE_BYTES / 1073741824" | bc)GB"
echo "Partition size: \$(echo "\$PART2_SIZE_BYTES / 1073741824" | bc)GB"

# If filesystem is larger than the cloned data, shrink it to fit
if [ \$FS_SIZE_BYTES -gt \$PART2_SIZE_BYTES ]; then
    echo ""
    echo "=== Step 3: Shrink filesystem to fit cloned data ==="
    # Calculate target size (partition size with some margin)
    SHRINK_SIZE_MB=\$((PART2_SIZE_BYTES / 1024 / 1024 * 90 / 100))
    echo "Shrinking filesystem to \${SHRINK_SIZE_MB}MB..."
    sudo resize2fs "\$PART_ROOT" \${SHRINK_SIZE_MB}M 2>&1
    sudo e2fsck -f -y "\$PART_ROOT" 2>&1 || true
    echo "Filesystem shrunk"
fi

echo ""
echo "=== Step 4: Grow image to target size ==="
target_size=\$((SIZE_GB * 1024 * 1024 * 1024))
current_size=\$(stat -c%s "\$IMAGE")

if [ \$current_size -lt \$target_size ]; then
    # Detach loop device first
    sudo losetup -d "\$LOOP" 2>/dev/null || true

    # Grow the image file
    dd if=/dev/zero bs=1 count=0 seek=\$target_size of="\$IMAGE" status=none 2>&1
    echo "Image grown to \${SIZE_GB}GB"

    # Re-attach loop device
    LOOP=\$(sudo losetup --find --show --partscan "\$IMAGE")
    echo "New loop device: \$LOOP"
    sleep 2
    PART_ROOT="\${LOOP}p2"
fi

echo ""
echo "=== Step 5: Fix partition table ==="
PART2_START=\$(sudo fdisk -l "\$LOOP" | grep "\${LOOP}p2" | awk '{print \$2}')
PART1_START=\$(sudo fdisk -l "\$LOOP" | grep "\${LOOP}p1" | awk '{print \$2}')
PART1_END=\$(sudo fdisk -l "\$LOOP" | grep "\${LOOP}p1" | awk '{print \$3}')

echo "Root partition starts at sector: \$PART2_START"

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
echo "=== Step 6: Grow filesystem to fill partition ==="
sudo partprobe "\$LOOP" 2>/dev/null || true
sleep 1
sudo resize2fs "\$PART_ROOT" 2>&1
echo "Filesystem grown to fill partition"

echo ""
echo "=== Step 7: Verify ==="
sudo fdisk -l "\$LOOP" | grep -E "Device|Boot|Linux"
echo ""
echo "Filesystem info:"
sudo dumpe2fs -h "\$PART_ROOT" 2>/dev/null | grep -E "Block count|Block size|Free blocks"

echo ""
echo "=== Step 8: Cleanup ==="
sudo losetup -d "\$LOOP" 2>/dev/null || true
echo "Done"
"@

    $scriptFile = Join-Path $TempDir "resize-image.sh"
    [System.IO.File]::WriteAllText($scriptFile, $resizeScript)

    Write-Step "Running resize in WSL..."
    $wslScript = ConvertTo-WslPath $scriptFile

    try {
        $output = wsl bash "$wslScript" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Step "Resize failed:" "ERROR"
            $output | ForEach-Object { Write-Step "  $_" "ERROR" }
            return $false
        }
        Write-Step "Image resized to ${SizeGB}GB!" "OK"
    }
    catch {
        Write-Step "WSL resize failed: $_" "ERROR"
        return $false
    }

    return $true
}

# ============================================================================
# Configure Image (same as generate-pi-image.ps1)
# ============================================================================

function Configure-ImagePartitions {
    param([string]$ImagePath)

    Write-Section "Configuring Image Partitions"

    $wslImage = ConvertTo-WslPath $ImagePath
    $wslProjectRoot = ConvertTo-WslPath $ProjectRoot
    $wslScriptRoot = ConvertTo-WslPath $PSScriptRoot

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

sudo bash -c "cat >> \"\$MNT_BOOT/wpa_supplicant.conf\" <<WPA_NET
network={
    ssid="${WifiSsid}"
    psk="${WifiPassword}"
    key_mgmt=WPA-PSK
}
WPA_NET"
echo "Wi-Fi configured for: ${WifiSsid}"

# 3. Hostname configuration
sudo bash -c "cat > \"\$MNT_BOOT/usercfg.txt\" <<USERCFG
[pi]
username=pi
password=raspberry
hostname=${PiHostname}
USERCFG"
echo "Hostname configured: ${PiHostname}"

# 4. config.txt
CONFIG_FILE="\$MNT_BOOT/config.txt"
if [ -f "\$CONFIG_FILE" ]; then
    if ! grep -q "DoraMCU Game Server" "\$CONFIG_FILE"; then
        sudo bash -c "cat >> \"\$CONFIG_FILE\" <<'CONFIG_ADD'

# DoraMCU Game Server Configuration
gpu_mem=16
enable_uart=1
CONFIG_ADD"
        echo "config.txt updated"
    else
        echo "config.txt already has DoraMCU config"
    fi
else
    sudo bash -c "cat > \"\$CONFIG_FILE\" <<'CONFIG_NEW'
# DoraMCU Game Server Configuration
gpu_mem=16
enable_uart=1
CONFIG_NEW"
fi

echo "Boot partition configuration complete"

echo ""
echo "=== Configure Root Partition (ext4) ==="

DORA_DIR="\$MNT_ROOT/home/pi/doramcu"
sudo mkdir -p "\$DORA_DIR"
echo "Created: \$DORA_DIR"

# 1. Copy docker-compose.yml
if [ -f "\$WSL_PROJECT/docker-compose.yml" ]; then
    sudo cp "\$WSL_PROJECT/docker-compose.yml" "\$DORA_DIR/docker-compose.yml"
    echo "Copied docker-compose.yml"
fi

# 2. Copy Dockerfiles
for df in Dockerfile Dockerfile.neuron Dockerfile.web Dockerfile.dl.arm64; do
    if [ -f "\$WSL_PROJECT/\$df" ]; then
        sudo cp "\$WSL_PROJECT/\$df" "\$DORA_DIR/\$df"
        echo "Copied \$df"
    fi
done

# 3. Copy dora-dl-python
if [ -d "\$WSL_PROJECT/dora-dl-python" ]; then
    sudo cp -r "\$WSL_PROJECT/dora-dl-python" "\$DORA_DIR/dora-dl-python"
    echo "Copied dora-dl-python"
fi

# 4. Copy dora-web
if [ -d "\$WSL_PROJECT/dora-web" ]; then
    sudo cp -r "\$WSL_PROJECT/dora-web" "\$DORA_DIR/dora-web"
    echo "Copied dora-web"
fi

# 5. Create .env
sudo bash -c "cat > \"\$DORA_DIR/.env\" <<'ENV_FILE'
# DoraMCU Environment Variables
NEURON_SECURITY_JWT_SECRET=$(openssl rand -hex 32)
MYSQL_ROOT_PASSWORD=doramcu_root_password_change_me
MYSQL_DATABASE=doramcu
MYSQL_USER=doramcu
MYSQL_PASSWORD=doramcu_password_change_me
DL_MODE=rest
ENV_FILE"
echo "Created .env file"

# 6. First-boot script
if [ -f "\$WSL_SCRIPTS/pi-first-boot.sh" ]; then
    sudo mkdir -p "\$MNT_ROOT/boot/firmware"
    sudo cp "\$WSL_SCRIPTS/pi-first-boot.sh" "\$MNT_ROOT/boot/firmware/first-boot.sh"
    sudo chmod +x "\$MNT_ROOT/boot/firmware/first-boot.sh"
    echo "Copied first-boot.sh"
fi

# 7. Systemd service
if [ -f "\$WSL_SCRIPTS/pi-doramcu.service" ]; then
    sudo cp "\$WSL_SCRIPTS/pi-doramcu.service" "\$MNT_ROOT/etc/systemd/system/doramcu.service"
    echo "Installed doramcu.service"
fi

# 8. Test runner
if [ -f "\$WSL_SCRIPTS/pi-test-runner.sh" ]; then
    sudo cp "\$WSL_SCRIPTS/pi-test-runner.sh" "\$DORA_DIR/pi-test-runner.sh"
    sudo chmod +x "\$DORA_DIR/pi-test-runner.sh"
    echo "Copied pi-test-runner.sh"
fi

# 9. Enable systemd service
SYSTEMD_TARGET="\$MNT_ROOT/etc/systemd/system/multi-user.target.wants"
sudo mkdir -p "\$SYSTEMD_TARGET"
sudo ln -sf /etc/systemd/system/doramcu.service "\$SYSTEMD_TARGET/doramcu.service"
echo "Enabled doramcu.service"

# 10. Swap
SWAP_CONF="\$MNT_ROOT/etc/dphys-swapfile"
if [ -f "\$SWAP_CONF" ]; then
    sudo sed -i 's/CONF_SWAPSIZE=.*/CONF_SWAPSIZE=2048/' "\$SWAP_CONF"
    echo "Swap configured to 2GB"
fi

# 11. Hostname
if [ -f "\$MNT_ROOT/etc/hostname" ]; then
    echo "${PiHostname}" | sudo tee "\$MNT_ROOT/etc/hostname" > /dev/null
fi
if [ -f "\$MNT_ROOT/etc/hosts" ]; then
    sudo sed -i "s/127.0.1.1.*/127.0.1.1\t${PiHostname}/" "\$MNT_ROOT/etc/hosts"
fi

# 12. Setup info
sudo bash -c "cat > \"\$MNT_BOOT/doramcu-setup-info.txt\" <<INFO_FILE
DoraMCU Pi Disk Image (Cloned from SD card)
Hostname: ${PiHostname}
Wi-Fi: ${WifiSsid}
Generated: \$(date '+%Y-%m-%d %H:%M:%S')
Image Size: ${ImageSizeGB}GB
INFO_FILE"

echo ""
echo "=== Unmount and cleanup ==="
sudo umount "\$MNT_ROOT"
sudo umount "\$MNT_BOOT"
sudo losetup -d "\$LOOP" 2>/dev/null || true
echo "Configuration complete"
"@

    $scriptFile = Join-Path $TempDir "configure-image.sh"
    [System.IO.File]::WriteAllText($scriptFile, $configScript)

    Write-Step "Running configuration in WSL..."
    $wslScript = ConvertTo-WslPath $scriptFile

    try {
        $output = wsl bash "$wslScript" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Step "Configuration failed:" "ERROR"
            $output | ForEach-Object { Write-Step "  $_" "ERROR" }
            return $false
        }
        Write-Step "Configuration complete!" "OK"
    }
    catch {
        Write-Step "WSL configuration failed: $_" "ERROR"
        return $false
    }

    return $true
}

# ============================================================================
# Build & Embed Docker Images
# ============================================================================

function Build-DockerImages {
    Write-Section "Building ARM64 Docker Images"

    if ($SkipImageBuild) {
        Write-Step "Skipping image build" "WARN"
        return $true
    }

    Write-Step "Setting up Docker Buildx for ARM64..."
    docker buildx create --use --name arm64-builder --driver docker-container 2>&1 | Out-Null
    docker buildx inspect --bootstrap 2>&1 | Out-Null
    Write-Step "Buildx ready" "OK"

    foreach ($img in $DockerImages) {
        Write-Step "Building: $($img.Name)"
        $dockerfilePath = Join-Path $ProjectRoot $img.Dockerfile
        if (-not (Test-Path $dockerfilePath)) {
            Write-Step "  Dockerfile not found: ${dockerfilePath}" "ERROR"
            continue
        }

        $buildArgs = @(
            "build", "--platform", "linux/arm64", "--load",
            "-t", $img.Name, "-f", $img.Dockerfile, $img.Context
        )
        $output = docker buildx $buildArgs 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Step "  Build failed" "ERROR"
            continue
        }
        Write-Step "  Built successfully" "OK"
    }

    Write-Step "Saving images to tar..."
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

    Write-Section "Embedding Docker Images"

    if (-not $script:ImagesTarPath) {
        Write-Step "No Docker images to embed" "WARN"
        return $true
    }

    $wslImage = ConvertTo-WslPath $ImagePath
    $wslTar = ConvertTo-WslPath $script:ImagesTarPath

    $embedScript = @"
#!/bin/bash
set -e

IMAGE="$wslImage"
TAR="$wslTar"
MNT_BASE="/tmp/doramcu-embed"
MNT_ROOT="\$MNT_BASE/root"

rm -rf "\$MNT_BASE"
mkdir -p "\$MNT_ROOT"

sudo losetup -j "\$IMAGE" 2>/dev/null | cut -d: -f1 | xargs -r sudo losetup -d 2>/dev/null || true
LOOP=\$(sudo losetup --find --show --partscan "\$IMAGE")
sleep 2

PART_ROOT="\${LOOP}p2"
sudo mount -t ext4 "\$PART_ROOT" "\$MNT_ROOT"

DORA_DIR="\$MNT_ROOT/home/pi/doramcu"
sudo cp "\$TAR" "\$DORA_DIR/doramcu-images.tar"
echo "Docker images embedded: \$(sudo du -h "\$DORA_DIR/doramcu-images.tar" | cut -f1)"

sudo umount "\$MNT_ROOT"
sudo losetup -d "\$LOOP" 2>/dev/null || true
echo "Done"
"@

    $scriptFile = Join-Path $TempDir "embed-images.sh"
    [System.IO.File]::WriteAllText($scriptFile, $embedScript)

    Write-Step "Embedding Docker images..."
    $wslScript = ConvertTo-WslPath $scriptFile

    try {
        $output = wsl bash "$wslScript" 2>&1
        if ($LASTEXITCODE -ne 0) {
            Write-Step "Embed failed:" "ERROR"
            return $false
        }
        Write-Step "Docker images embedded!" "OK"
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
    Write-Host ""
    Write-Host "Disk image created successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Output: $ImagePath" -ForegroundColor Cyan
    Write-Host "Size: ${imgSizeGB}GB" -ForegroundColor White
    Write-Host "Hostname: ${PiHostname}" -ForegroundColor White
    Write-Host "Wi-Fi: ${WifiSsid}" -ForegroundColor White
    Write-Host "Docker images: $(if ($SkipImageBuild) { 'Skipped' } else { 'Included' })" -ForegroundColor White
    Write-Host ""
    Write-Host "Flash to USB with:" -ForegroundColor Cyan
    Write-Host "  - Raspberry Pi Imager (custom image)" -ForegroundColor Yellow
    Write-Host "  - BalenaEtcher" -ForegroundColor Yellow
    Write-Host "  - Win32 Disk Imager" -ForegroundColor Yellow
    Write-Host ""
}

# ============================================================================
# Main
# ============================================================================

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  DoraMCU SD Card Cloner" -ForegroundColor Cyan
Write-Host "  Clones SD card to ready-to-flash .img file" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
if (-not (Test-Prerequisites)) {
    Write-Step "Prerequisites check failed." "ERROR"
    exit 1
}

# Create temp directory
New-Item -ItemType Directory -Path $TempDir -Force | Out-Null

# Find SD card
if ($DiskNumber -eq -1) {
    $DiskNumber = Find-SDCard
    if ($DiskNumber -eq -1) {
        Write-Step "Cannot find SD card. Please specify -DiskNumber." "ERROR"
        exit 1
    }
}

# Step 1: Clone SD card
Write-Step "Output image: $OutputPath"
if (-not (Clone-SDCard -DiskNumber $DiskNumber -TargetImage $OutputPath)) {
    Write-Step "SD card clone failed." "ERROR"
    exit 1
}

# Step 2: Resize image
if (-not (Create-ResizedImage -ImagePath $OutputPath -SizeGB $ImageSizeGB)) {
    Write-Step "Image resize failed." "ERROR"
    exit 1
}

# Step 3: Configure partitions
if (-not (Configure-ImagePartitions -ImagePath $OutputPath)) {
    Write-Step "Image configuration failed." "ERROR"
    exit 1
}

# Step 4: Build Docker images
$script:ImagesTarPath = $null
if (-not (Build-DockerImages)) {
    Write-Step "Docker build failed." "ERROR"
    Write-Step "Image created without Docker images." "WARN"
}

# Step 5: Embed Docker images
if ($script:ImagesTarPath) {
    if (-not (Embed-DockerImages -ImagePath $OutputPath)) {
        Write-Step "Docker embed failed." "ERROR"
    }
}

# Summary
Show-Summary -ImagePath $OutputPath

# Cleanup
Write-Step "Cleaning up..."
Remove-Item -Path $TempDir -Recurse -Force -ErrorAction SilentlyContinue
Write-Step "Done! Image ready at: $OutputPath" "OK"

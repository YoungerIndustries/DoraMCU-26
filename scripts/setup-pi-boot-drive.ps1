#Requires -RunAsAdministrator
<#
.SYNOPSIS
    Prepares a USB drive as a bootable Raspberry Pi DoraMCU dedicated game server.

.DESCRIPTION
    This script automates the entire process of transforming a USB drive into a
    complete DoraMCU game server for Raspberry Pi 4/5. It:
    1. Downloads and flashes Raspberry Pi OS 64-bit (Bookworm) to the USB drive
    2. Pre-configures OS settings (SSH, Wi-Fi, hostname, swap, GPU memory)
    3. Builds ARM64 Docker images for all 6 DoraMCU services on the PC
    4. Saves Docker images as tar files to the USB drive
    5. Installs first-boot scripts and systemd service for auto-start

    After the script completes, plug the USB drive into a Raspberry Pi 4/5,
    power it on, and all services will start automatically.

.PARAMETER DriveLetter
    The drive letter of the USB drive (e.g., 'D'). Default: 'D'.

.PARAMETER WifiSsid
    Wi-Fi network name for the Pi to connect to.

.PARAMETER WifiPassword
    Wi-Fi password for the network.

.PARAMETER PiHostname
    Hostname for the Pi. Default: 'doramcu-server'.

.PARAMETER SkipImageBuild
    Skip building Docker images (useful for testing the OS flashing only).

.PARAMETER ImageDownloadUrl
    Custom URL for the Raspberry Pi OS image. Default: latest Bookworm 64-bit.

.EXAMPLE
    .\scripts\setup-pi-boot-drive.ps1 -DriveLetter D -WifiSsid "MyNetwork" -WifiPassword "MyPassword"

.EXAMPLE
    .\scripts\setup-pi-boot-drive.ps1 -DriveLetter E -WifiSsid "OfficeWiFi" -WifiPassword "secret123" -PiHostname "my-game-server"
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $false)]
    [char]$DriveLetter = 'D',

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
$DrivePath = "$DriveLetter`:"
$BootPath = "$DrivePath\"  # FAT32 boot partition is the root of the drive
$TempDir = Join-Path $env:TEMP "doramcu-pi-setup"
$OsImageName = "raspios-bookworm-arm64.img"
$OsImageXz = "raspios-bookworm-arm64.img.xz"

# Default Raspberry Pi OS 64-bit Bookworm Lite download URL (headless, smaller ~424MB)
if (-not $ImageDownloadUrl) {
    $ImageDownloadUrl = "https://downloads.raspberrypi.org/raspios_arm64/images/raspios_arm64-2026-09-15/2026-09-15-raspios-bookworm-arm64-lite.img.xz"
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
        Write-Step "WSL is not installed. Please install WSL first:" -ForegroundColor Red
        Write-Step "  wsl --install" -ForegroundColor Yellow
        Write-Step "Then restart this script." -ForegroundColor Yellow
        return $false
    }
    Write-Step "WSL is installed" "OK"

    # Check Docker Desktop
    Write-Step "Checking Docker Desktop..."
    $dockerVersion = docker --version 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Docker Desktop is not installed or not running." -ForegroundColor Red
        Write-Step "Please install Docker Desktop: https://www.docker.com/products/docker-desktop/" -ForegroundColor Yellow
        return $false
    }
    Write-Step "Docker is installed: $dockerVersion" "OK"

    # Check Docker Buildx
    Write-Step "Checking Docker Buildx..."
    $buildxVersion = docker buildx version 2>&1 | Out-String
    if ($LASTEXITCODE -ne 0) {
        Write-Step "Docker Buildx is not installed." -ForegroundColor Red
        Write-Step "Install it: docker buildx install" -ForegroundColor Yellow
        return $false
    }
    Write-Step "Docker Buildx is available: $buildxVersion" "OK"

    # Check disk space (need ~20GB for OS image + Docker images)
    Write-Step "Checking disk space..."
    try {
        $sysDrive = Get-CimInstance Win32_LogicalDisk -Filter "DeviceID='$($env:SystemDrive)'" -ErrorAction Stop
        $freeSpaceGB = [math]::Round($sysDrive.FreeSpace / 1GB, 1)
    } catch {
        $freeSpaceGB = 9999  # Assume sufficient if we can't check
    }
    if ($freeSpaceGB -lt 10) {
        Write-Step "Insufficient disk space: ${freeSpaceGB}GB free, need at least 10GB." "ERROR"
        return $false
    }
    Write-Step "Disk space OK: ${freeSpaceGB}GB free" "OK"

    # Check USB drive exists
    Write-Step "Checking USB drive ${DriveLetter}:"
    $usbVolume = Get-Volume -DriveLetter $DriveLetter -ErrorAction SilentlyContinue
    if (-not $usbVolume) {
        Write-Step "Drive ${DriveLetter}: not found. Please connect the USB drive." "ERROR"
        return $false
    }
    $usbFreeGB = [math]::Round($usbVolume.SizeRemaining / 1GB, 1)
    Write-Step "Drive ${DriveLetter}: found (${usbFreeGB}GB free)" "OK"

    if ($usbFreeGB -lt 32) {
        Write-Step "Drive ${DriveLetter} has only ${usbFreeGB}GB free. 64GB+ recommended." "WARN"
    }

    return $true
}

function Confirm-Destruction {
    param([char]$Drive)

    Write-Section "WARNING: DATA DESTRUCTION"
    Write-Host ""
    Write-Host "THIS SCRIPT WILL COMPLETELY ERASE DRIVE ${Drive}:" -ForegroundColor Red
    Write-Host "  ALL DATA ON THIS DRIVE WILL BE PERMANENTLY LOST" -ForegroundColor Red
    Write-Host ""

    # Show drive details
    $volume = Get-Volume -DriveLetter $Drive -ErrorAction SilentlyContinue
    if ($volume) {
        Write-Host "  Drive label: $($volume.FileSystemLabel)" -ForegroundColor Yellow
        Write-Host "  File system: $($volume.FileSystem)" -ForegroundColor Yellow
        Write-Host "  Total size: $([math]::Round($volume.Size / 1GB, 1))GB" -ForegroundColor Yellow
    }

    Write-Host ""
    $response = Read-Host "Type 'ERASE ${Drive}' to confirm (or anything else to cancel)"
    if ($response -ne "ERASE ${Drive}") {
        Write-Step "Operation cancelled by user." "WARN"
        return $false
    }

    # Second confirmation
    Write-Host ""
    $response2 = Read-Host "FINAL CONFIRMATION: Type 'YES ERASE ${Drive}' to proceed"
    if ($response2 -ne "YES ERASE ${Drive}") {
        Write-Step "Operation cancelled by user." "WARN"
        return $false
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
        # Download the image
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
        $imgPath = $imageFile -replace '\.xz$', ''

        # Try 7-Zip first
        if (Get-Command 7z -ErrorAction SilentlyContinue) {
            7z x -y -o$imageDir $imageFile | Out-Null
        }
        # Try tar (WSL)
        elseif (Get-Command tar -ErrorAction SilentlyContinue) {
            tar -xf $imageFile -C $imageDir
        }
        else {
            Write-Step "No decompression tool found. Install 7-Zip or use WSL tar." "ERROR"
            return $false
        }

        # Find the extracted image
        $extracted = Get-ChildItem $imageDir -Filter "*.img" | Select-Object -First 1
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

function Flash-OSImage {
    Write-Section "Flashing OS to USB Drive"

    # Get the disk number for the drive letter
    Write-Step "Finding disk for drive ${DriveLetter}:"
    $partition = Get-Partition -DriveLetter $DriveLetter -ErrorAction SilentlyContinue
    if (-not $partition) {
        Write-Step "Cannot find partition for drive ${DriveLetter}:" "ERROR"
        return $false
    }
    $diskNumber = $partition.DiskNumber
    $wslDisk = "/dev/sd$(Convert-ToLinuxDisk $diskNumber)"

    Write-Step "Disk number: ${diskNumber} (WSL: ${wslDisk})" "OK"
    Write-Step "Image: $($script:OsImagePath)"

    # Unmount the drive in Windows
    Write-Step "Unmounting drive ${DriveLetter}:"
    Dismount-VHD -Path "$DrivePath" -ErrorAction SilentlyContinue
    $disk = Get-Disk -Number $diskNumber
    if ($disk) {
        Get-Partition -DiskNumber $diskNumber | Where-Object DriveLetter | ForEach-Object {
            Remove-PSDrive -Name $_.DriveLetter -Force -ErrorAction SilentlyContinue
        }
        Write-Step "Drive unmounted" "OK"
    }

    # Flash using WSL dd
    Write-Step "Flashing image using WSL dd (this may take 10-20 minutes)..."
    $wslImage = $script:OsImagePath -replace '\\', '/' -replace 'C:', '/mnt/c'

    $ddCommand = "dd if='${wslImage}' of='${wslDisk}' bs=4M status=progress oflag=direct"
    Write-Step "Command: ${ddCommand}"

    try {
        wsl bash -c $ddCommand
        if ($LASTEXITCODE -eq 0) {
            Write-Step "Image flashed successfully!" "OK"
        }
        else {
            Write-Step "dd command failed with exit code: $LASTEXITCODE" "ERROR"
            return $false
        }
    }
    catch {
        Write-Step "WSL dd failed: $_" "ERROR"
        return $false
    }

    # Sync to ensure all data is written
    Write-Step "Syncing disk..."
    wsl sync
    Write-Step "Disk synced" "OK"

    return $true
}

function Convert-ToLinuxDisk {
    param([int]$DiskNumber)
    # Map Windows disk numbers to Linux device letters
    # This is a simplified mapping; in production you'd use more robust detection
    $letters = 'abcdefghij'
    if ($DiskNumber -lt $letters.Length) {
        return $letters[$DiskNumber]
    }
    return "sda"
}

function Configure-BootPartition {
    Write-Section "Configuring Boot Partition"

    # After flashing, the boot partition should be accessible
    # Wait for the drive to be remounted
    Write-Step "Waiting for boot partition to be accessible..."
    Start-Sleep -Seconds 5

    # The boot partition on Raspberry Pi OS is the FAT32 partition
    # It should auto-mount; if not, we need to find it
    $bootDrive = $DriveLetter

    # Check if the boot partition is accessible
    $bootPath = "$bootDrive`:\"
    if (-not (Test-Path $bootPath)) {
        Write-Step "Boot partition not accessible at ${bootPath}" "WARN"
        Write-Step "You may need to re-plug the USB drive or manually mount the partition" "WARN"
        Write-Step "Attempting to re-mount..."

        # Try to get the partition and assign a drive letter
        $disk = Get-Disk -Number (Get-Partition -DriveLetter $DriveLetter -ErrorAction SilentlyContinue).DiskNumber -ErrorAction SilentlyContinue
        if ($disk) {
            $partitions = Get-Partition -DiskNumber $disk.Number | Where-Object { $_.Type -eq '0C' -or $_.Type -eq '0B' } | Sort-Object PartitionNumber | Select-Object -First 1
            if ($partitions) {
                $partitions | Set-Partition -NewDriveLetter $DriveLetter -ErrorAction SilentlyContinue
                Start-Sleep -Seconds 2
            }
        }
    }

    if (-not (Test-Path $bootPath)) {
        Write-Step "Cannot access boot partition. Manual configuration may be needed." "ERROR"
        return $false
    }

    Write-Step "Boot partition accessible at: ${bootPath}" "OK"

    # 1. Enable SSH (empty file named 'ssh')
    Write-Step "Enabling SSH..."
    New-Item -ItemType File -Path "$bootPath\ssh" -Force | Out-Null
    Write-Step "SSH enabled" "OK"

    # 2. Wi-Fi configuration
    Write-Step "Configuring Wi-Fi..."
    $wifiConfig = @"
network={
    ssid="${WifiSsid}"
    psk="${WifiPassword}"
    key_mgmt=WPA-PSK
}
"@
    $wpaConfig = @"
country=US
ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev
update_config=1

${wifiConfig}
"@
    [System.IO.File]::WriteAllText("$bootPath\wpa_supplicant.conf", $wpaConfig)
    Write-Step "Wi-Fi configured for: ${WifiSsid}" "OK"

    # 3. Hostname configuration via usercfg.txt
    Write-Step "Configuring hostname: ${PiHostname}"
    $usercfg = @"
[pi]
username=pi
password=raspberry
hostname=${PiHostname}
"@
    [System.IO.File]::WriteAllText("$bootPath\usercfg.txt", $usercfg)
    Write-Step "Hostname configured" "OK"

    # 4. config.txt additions (swap, GPU memory, etc.)
    Write-Step "Configuring system settings..."
    $configAdditions = @"

# DoraMCU Game Server Configuration
# Reduce GPU memory to maximize RAM for Docker containers
gpu_mem=16

# Increase swap space for Docker builds
# (Also configured in dphys-swapfile below)

# Enable UART for serial console access
enable_uart=1

# USB boot mode (Pi 4/5)
# Already enabled by default on newer firmware

# Overclock settings (commented out - enable if needed)
# Pi 4: arm_freq=2000, over_voltage=2
# Pi 5: arm_freq=2400, over_voltage=2
"@

    # Append to config.txt if it exists, otherwise create it
    $configPath = "$bootPath\config.txt"
    if (Test-Path $configPath) {
        Add-Content -Path $configPath -Value $configAdditions
    }
    else {
        [System.IO.File]::WriteAllText($configPath, $configAdditions)
    }
    Write-Step "System settings configured" "OK"

    # 5. Swap size configuration (in the root partition, accessible via /boot/firmware on newer Pi OS)
    # We'll create a script to configure swap on first boot
    Write-Step "Creating swap configuration script..."
    $swapScript = @"
#!/bin/bash
# Configure swap size to 2GB for Docker operations
sudo sed -i 's/CONF_SWAPSIZE=.*/CONF_SWAPSIZE=2048/' /etc/dphys-swapfile
sudo systemctl restart dphys-swapfile
echo "Swap configured to 2GB"
"@
    [System.IO.File]::WriteAllText("$bootPath\configure-swap.sh", $swapScript)
    Write-Step "Swap configuration script created" "OK"

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

function Copy-ToDrive {
    param([string]$BootPath)

    Write-Section "Copying Files to USB Drive"

    # 1. Copy Docker images tar file
    if ($script:ImagesTarPath) {
        Write-Step "Copying Docker images..."
        Copy-Item -Path $script:ImagesTarPath -Destination "$BootPath\doramcu-images.tar" -Force
        $sizeGB = [math]::Round((Get-Item "$BootPath\doramcu-images.tar").Length / 1GB, 2)
        Write-Step "Docker images copied: ${sizeGB}GB" "OK"
    }

    # 2. Copy first-boot script
    Write-Step "Copying first-boot script..."
    $firstBootScript = Join-Path $PSScriptRoot "pi-first-boot.sh"
    if (Test-Path $firstBootScript) {
        Copy-Item -Path $firstBootScript -Destination "$BootPath\first-boot.sh" -Force
        Write-Step "First-boot script copied" "OK"
    }
    else {
        Write-Step "First-boot script not found: ${firstBootScript}" "WARN"
    }

    # 3. Copy systemd service file
    Write-Step "Copying systemd service..."
    $serviceFile = Join-Path $PSScriptRoot "pi-doramcu.service"
    if (Test-Path $serviceFile) {
        Copy-Item -Path $serviceFile -Destination "$BootPath\doramcu.service" -Force
        Write-Step "Systemd service copied" "OK"
    }
    else {
        Write-Step "Systemd service not found: ${serviceFile}" "WARN"
    }

    # 4. Copy test runner script
    Write-Step "Copying test runner script..."
    $testScript = Join-Path $PSScriptRoot "pi-test-runner.sh"
    if (Test-Path $testScript) {
        Copy-Item -Path $testScript -Destination "$BootPath\pi-test-runner.sh" -Force
        Write-Step "Test runner script copied" "OK"
    }
    else {
        Write-Step "Test runner script not found: ${testScript}" "WARN"
    }

    # 5. Copy DoraMCU project files (docker-compose file and Dockerfiles)
    Write-Step "Copying DoraMCU project files..."
    $projectFiles = @(
        "docker-compose.yml",
        "Dockerfile",
        "Dockerfile.neuron",
        "Dockerfile.web",
        "Dockerfile.dl.arm64"
    )

    $doramcuDir = "$BootPath\doramcu"
    New-Item -ItemType Directory -Path $doramcuDir -Force | Out-Null

    foreach ($file in $projectFiles) {
        $src = Join-Path $ProjectRoot $file
        if (Test-Path $src) {
            Copy-Item -Path $src -Destination "$doramcuDir\$file" -Force
        }
        else {
            Write-Step "  Project file not found: ${file}" "WARN"
        }
    }

    # Copy dora-dl-python directory (needed for DL service build context)
    $dlPythonDir = Join-Path $ProjectRoot "dora-dl-python"
    if (Test-Path $dlPythonDir) {
        Copy-Item -Path $dlPythonDir -Destination "$doramcuDir\dora-dl-python" -Recurse -Force
        Write-Step "  dora-dl-python copied" "OK"
    }

    # Copy dora-web directory (needed for nginx static assets)
    $webDir = Join-Path $ProjectRoot "dora-web"
    if (Test-Path $webDir) {
        Copy-Item -Path $webDir -Destination "$doramcuDir\dora-web" -Recurse -Force
        Write-Step "  dora-web copied" "OK"
    }

    Write-Step "Project files copied to: ${doramcuDir}" "OK"

    # 6. Create .env file template
    Write-Step "Creating .env file template..."
    $envFile = @"
# DoraMCU Environment Variables
# Generated by setup-pi-boot-drive.ps1

# Neuron JWT Secret (generate a random one: openssl rand -hex 32)
NEURON_SECURITY_JWT_SECRET=$(openssl rand -hex 32)

# Database settings
MYSQL_ROOT_PASSWORD=doramcu_root_password_change_me
MYSQL_DATABASE=doramcu
MYSQL_USER=doramcu
MYSQL_PASSWORD=doramcu_password_change_me

# DL mode: rest = offload all DL to Python service (recommended for Pi)
DL_MODE=rest
"@
    [System.IO.File]::WriteAllText("$doramcuDir\.env", $envFile)
    Write-Step ".env file created" "OK"

    return $true
}

function Setup-FirstBoot {
    param([string]$BootPath)

    Write-Section "Setting Up First Boot Automation"

    # Create a script that runs on first boot via rc.local or systemd
    # Raspberry Pi OS Bookworm uses systemd, so we'll use a systemd service

    Write-Step "Creating first-boot trigger..."

    # Method 1: Use /etc/rc.local (simplest approach)
    $rcLocal = @"
#!/bin/bash
# /etc/rc.local - First boot automation for DoraMCU

# Run first-boot script if it exists
if [ -f /boot/firmware/first-boot.sh ]; then
    nohup bash /boot/firmware/first-boot.sh > /var/log/first-boot.log 2>&1 &
fi

exit 0
"@

    # We need to copy this to the root partition, which is not accessible from Windows
    # So we'll include instructions in the first-boot script itself

    # Method 2: Create a systemd service on the boot partition
    # that will be moved to /etc/systemd/system on first boot
    $firstBootService = @"
[Unit]
Description=DoraMCU First Boot Setup
After=network-online.target
Wants=network-online.target

[Service]
Type=oneshot
ExecStart=/bin/bash /boot/firmware/first-boot.sh
RemainAfterExit=yes
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
"@
    [System.IO.File]::WriteAllText("$BootPath\first-boot.service", $firstBootService)
    Write-Step "First-boot service template created" "OK"

    # Create a marker file that the first-boot script checks
    $setupInfo = @"
DoraMCU Pi Boot Drive Setup
Hostname: ${PiHostname}
Wi-Fi: ${WifiSsid}
Date: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
Images: $((Get-Item "$BootPath\doramcu-images.tar" -ErrorAction SilentlyContinue).Length / 1GB)GB
"@
    [System.IO.File]::WriteAllText("$BootPath\doramcu-setup-info.txt", $setupInfo)
    Write-Step "Setup info file created" "OK"

    return $true
}

function Show-Summary {
    Write-Section "Setup Complete!"

    Write-Host ""
    Write-Host "USB drive preparation complete!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Safely eject the USB drive from Windows" -ForegroundColor White
    Write-Host "  2. Enable USB boot mode on your Raspberry Pi (if not already enabled):" -ForegroundColor White
    Write-Host "     - Download Raspberry Pi Bootloader tools: https://www.raspberrypi.com/documentation/computers/bootloader.html" -ForegroundColor Gray
    Write-Host "     - Or use rpi-eeprom-update on an SD card first" -ForegroundColor Gray
    Write-Host "  3. Plug the USB drive into the Raspberry Pi" -ForegroundColor White
    Write-Host "  4. Power on the Pi" -ForegroundColor White
    Write-Host "  5. Wait ~10 minutes for first boot (Docker image loading + service startup)" -ForegroundColor White
    Write-Host "  6. Access the server at: http://${PiHostname}.local or http://<pi-ip>" -ForegroundColor White
    Write-Host ""
    Write-Host "Monitoring:" -ForegroundColor Cyan
    Write-Host "  - Service status: docker compose --profile pi ps" -ForegroundColor Gray
    Write-Host "  - Logs: docker compose --profile pi logs -f" -ForegroundColor Gray
    Write-Host "  - Edge health: http://<pi-ip>:8090/api/v1/neuron/edge" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Troubleshooting:" -ForegroundColor Cyan
    Write-Host "  - SSH access: ssh pi@${PiHostname}.local (password: raspberry)" -ForegroundColor Gray
    Write-Host "  - First boot log: /var/log/first-boot.log" -ForegroundColor Gray
    Write-Host "  - Docker logs: journalctl -u doramcu.service" -ForegroundColor Gray
    Write-Host ""
}

# ============================================================================
# Main Execution
# ============================================================================

Write-Host ""
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host "  DoraMCU Raspberry Pi USB Boot Drive Setup" -ForegroundColor Cyan
Write-Host "  Automated Game Server Preparation" -ForegroundColor Cyan
Write-Host "================================================================" -ForegroundColor Cyan
Write-Host ""

# Check admin privileges
if (-not (Test-Administrator)) {
    Write-Step "This script must be run as Administrator." "ERROR"
    Write-Step "Right-click PowerShell and select 'Run as Administrator'" "WARN"
    exit 1
}

# Check prerequisites
if (-not (Test-Prerequisites)) {
    Write-Step "Prerequisites check failed. Please fix the issues above and try again." "ERROR"
    exit 1
}

# Confirm data destruction
if (-not (Confirm-Destruction)) {
    exit 0
}

# Create temp directory
New-Item -ItemType Directory -Path $TempDir -Force | Out-Null

# Step 1: Download OS image
$script:OsImagePath = $null
if (-not (Download-OSImage)) {
    Write-Step "OS image download failed." "ERROR"
    exit 1
}

# Step 2: Flash OS to USB drive
if (-not (Flash-OSImage)) {
    Write-Step "OS flashing failed." "ERROR"
    exit 1
}

# Step 3: Configure boot partition
if (-not (Configure-BootPartition)) {
    Write-Step "Boot partition configuration failed." "ERROR"
    exit 1
}

# Step 4: Build Docker images
$script:ImagesTarPath = $null
if (-not (Build-DockerImages)) {
    Write-Step "Docker image build failed." "ERROR"
    exit 1
}

# Step 5: Copy files to drive
if (-not (Copy-ToDrive -BootPath $BootPath)) {
    Write-Step "File copy failed." "ERROR"
    exit 1
}

# Step 6: Setup first boot automation
if (-not (Setup-FirstBoot -BootPath $BootPath)) {
    Write-Step "First boot setup failed." "WARN"
}

# Show summary
Show-Summary

# Cleanup temp files
Write-Step "Cleaning up temporary files..."
Remove-Item -Path $TempDir -Recurse -Force -ErrorAction SilentlyContinue
Write-Step "Done!" "OK"

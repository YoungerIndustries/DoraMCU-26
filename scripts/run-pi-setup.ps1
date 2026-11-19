# Auto-elevating wrapper for Raspberry Pi USB boot drive setup
# This script elevates to Administrator and runs setup-pi-boot-drive.ps1 with auto-confirmation

# ============================================================================
# Configuration - Edit these values for your setup
# ============================================================================
$DriveLetter = "D"
$WifiSsid = "Crown9"
$WifiPassword = "Crown9baby"
$PiHostname = "doramcu-server"

# ============================================================================
# Auto-elevation to Administrator
# ============================================================================
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$setupScript = Join-Path $scriptDir "setup-pi-boot-drive.ps1"

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if (-not $isAdmin) {
    Write-Host ""
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host "  Raspberry Pi USB Boot Drive Setup" -ForegroundColor Cyan
    Write-Host "==================================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Elevating to Administrator privileges..." -ForegroundColor Yellow
    Write-Host ""
    
    # Re-launch this script with Admin privileges
    Start-Process powershell.exe -Verb RunAs -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
    exit
}

# ============================================================================
# Pre-flight checks
# ============================================================================
Write-Host ""
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Raspberry Pi USB Boot Drive Setup" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Running as Administrator: YES" -ForegroundColor Green
Write-Host ""

# Check Docker Desktop
Write-Host "Checking Docker Desktop..." -ForegroundColor Yellow
try {
    $dockerVersion = docker info --format "{{.ServerVersion}}" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Docker Desktop: Running (v$dockerVersion)" -ForegroundColor Green
    } else {
        Write-Host "  ERROR: Docker Desktop is not running!" -ForegroundColor Red
        Write-Host "  Please start Docker Desktop and try again." -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
} catch {
    Write-Host "  ERROR: Docker is not installed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check Buildx
Write-Host "Checking Docker Buildx..." -ForegroundColor Yellow
try {
    $buildxVersion = docker buildx version 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  Docker Buildx: Available" -ForegroundColor Green
    } else {
        Write-Host "  ERROR: Docker Buildx is not available!" -ForegroundColor Red
        Read-Host "Press Enter to exit"
        exit 1
    }
} catch {
    Write-Host "  ERROR: Docker Buildx is not installed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check WSL
Write-Host "Checking WSL..." -ForegroundColor Yellow
$wslFound = $false
if (Test-Path "C:\Windows\System32\wsl.exe") {
    try {
        $wslOutput = & "C:\Windows\System32\wsl.exe" --version 2>&1
        if ($wslOutput -match "WSL" -or $LASTEXITCODE -eq 0) {
            $wslFound = $true
        }
    } catch {
        $wslFound = $true  # wsl.exe exists, so WSL is installed
    }
}
if ($wslFound) {
    Write-Host "  WSL: Installed" -ForegroundColor Green
} else {
    Write-Host "  ERROR: WSL is not installed!" -ForegroundColor Red
    Write-Host "  Install WSL: wsl --install" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# Check drive exists
Write-Host "Checking drive ${DriveLetter}:" -ForegroundColor Yellow
$drive = Get-Volume -DriveLetter $DriveLetter -ErrorAction SilentlyContinue
if ($drive) {
    $freeGB = [math]::Round($drive.SizeRemaining / 1GB, 1)
    $totalGB = [math]::Round($drive.Size / 1GB, 1)
    Write-Host "  Drive found: $($drive.FileSystemLabel) ($freeGB GB free of $totalGB GB)" -ForegroundColor Green
} else {
    Write-Host "  ERROR: Drive ${DriveLetter}: not found!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Check setup script exists
if (-not (Test-Path $setupScript)) {
    Write-Host ""
    Write-Host "  ERROR: Setup script not found: $setupScript" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# ============================================================================
# WARNING - Data destruction
# ============================================================================
Write-Host ""
Write-Host "==================================================" -ForegroundColor Red
Write-Host "  WARNING: DATA DESTRUCTION" -ForegroundColor Red
Write-Host "==================================================" -ForegroundColor Red
Write-Host ""
Write-Host "  This will ERASE ALL DATA on drive ${DriveLetter}: ($($drive.FileSystemLabel))" -ForegroundColor Red
Write-Host "  The drive will be reformatted with Raspberry Pi OS." -ForegroundColor Red
Write-Host ""
Write-Host "  Estimated time: 30-60 minutes" -ForegroundColor Yellow
Write-Host "    - OS download: ~5 minutes" -ForegroundColor Yellow
Write-Host "    - Flashing to USB: 10-20 minutes" -ForegroundColor Yellow
Write-Host "    - Docker image builds: 30-60 minutes" -ForegroundColor Yellow
Write-Host "    - File copy: ~5 minutes" -ForegroundColor Yellow
Write-Host ""

# Auto-confirm after countdown
Write-Host "  Auto-confirming in 10 seconds..." -ForegroundColor Yellow
for ($i = 10; $i -ge 1; $i--) {
    Write-Host "    $i..." -ForegroundColor Yellow -NoNewline
    Start-Sleep -Seconds 1
    Write-Host " " -NoNewline
}
Write-Host ""

Write-Host "  Proceeding with setup..." -ForegroundColor Green
Write-Host ""

# ============================================================================
# Run the setup script with auto-confirmation
# ============================================================================
# The setup script expects interactive input for "ERASE D" and "YES ERASE D"
# We pipe these responses automatically
$confirmations = @"
ERASE $DriveLetter
YES ERASE $DriveLetter
"@

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "  Starting USB boot drive preparation..." -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Drive: ${DriveLetter}:" -ForegroundColor White
Write-Host "  Wi-Fi SSID: $WifiSsid" -ForegroundColor White
Write-Host "  Pi Hostname: $PiHostname" -ForegroundColor White
Write-Host ""

# Run the setup script with piped confirmations
$confirmations | & $setupScript -DriveLetter $DriveLetter -WifiSsid $WifiSsid -WifiPassword $WifiPassword -PiHostname $PiHostname

# Check result
if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "==================================================" -ForegroundColor Green
    Write-Host "  SUCCESS: USB boot drive is ready!" -ForegroundColor Green
    Write-Host "==================================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "  Next steps:" -ForegroundColor Cyan
    Write-Host "    1. Safely eject the ${DriveLetter}: drive from Windows" -ForegroundColor White
    Write-Host "    2. Plug it into the Raspberry Pi 5" -ForegroundColor White
    Write-Host "    3. Power on the Pi and wait 10-15 minutes for first boot" -ForegroundColor White
    Write-Host "    4. Find the Pi's IP address on your network" -ForegroundColor White
    Write-Host "    5. SSH in: ssh pi@<pi-ip>" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "==================================================" -ForegroundColor Red
    Write-Host "  ERROR: Setup failed (exit code: $LASTEXITCODE)" -ForegroundColor Red
    Write-Host "==================================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Check the output above for error details." -ForegroundColor Yellow
}

Write-Host "Press Enter to close this window..." -ForegroundColor Gray
Read-Host

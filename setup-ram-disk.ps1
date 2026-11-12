<#
.SYNOPSIS
    Sets up a RAM disk using ImDisk Toolkit for DoraMCU development,
    providing fast temporary storage for Gradle cache and build outputs.

.DESCRIPTION
    This script downloads and installs ImDisk Toolkit, then creates a RAM disk
    that uses physical RAM as a virtual drive. The RAM disk is much faster than
    disk-based storage and is ideal for temporary build artifacts, Gradle cache,
    and test data.

    Recommended RAM disk size: 8 GB (adjustable based on available RAM).

    The RAM disk is configured to persist across reboots via a startup script.

.PREREQUISITES
    - Run as Administrator
    - At least 8 GB available RAM (16 GB total RAM recommended)
    - Internet connection (to download ImDisk)

.EXAMPLE
    .\setup-ram-disk.ps1
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$DriveLetter = "R",

    [Parameter(Mandatory=$false)]
    [int]$SizeMB = 8192,  # 8 GB default

    [Parameter(Mandatory=$false)]
    [string]$MountLetter = "R:"
)

$ErrorActionPreference = "Stop"

# Check for administrator privileges
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Error "This script must be run as Administrator. Right-click PowerShell and select 'Run as Administrator'."
    exit 1
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DoraMCU RAM Disk Setup (ImDisk)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check available RAM
$os = Get-CimInstance Win32_OperatingSystem
$totalRAMGB = [math]::Round($os.TotalVisibleMemorySize / 1MB, 2)
$freeRAMGB = [math]::Round($os.FreePhysicalMemory / 1MB, 2)
$requiredRAMGB = $SizeMB / 1024

Write-Host "Physical RAM: $totalRAMGB GB" -ForegroundColor Yellow
Write-Host "Free RAM: $freeRAMGB GB" -ForegroundColor Yellow
Write-Host "RAM disk size: $requiredRAMGB GB" -ForegroundColor Yellow
Write-Host ""

if ($os.FreePhysicalMemory -lt $SizeMB * 1024) {
    Write-Warning "Low free RAM. Consider reducing RAM disk size. Need $requiredRAMGB GB, but only $freeRAMGB GB is free."
    $confirm = Read-Host "Continue anyway? (Y/N)"
    if ($confirm -ne 'Y' -and $confirm -ne 'y') {
        exit 0
    }
}

# Define ImDisk installation path
$imDiskDir = "C:\Program Files\ImDisk"
$imDiskExe = "$imDiskDir\imdisk.exe"
$imDiskInstaller = "$env:TEMP\imdisk-2.0.5.zip"
$imDiskDownloadUrl = "https://sourceforge.net/projects/imdisk-toolkit/files/Released/imdisk-2.0.5.zip/download"

# Check if ImDisk is already installed
if (Test-Path $imDiskExe) {
    Write-Host "ImDisk is already installed at: $imDiskDir" -ForegroundColor Green
} else {
    Write-Host "ImDisk not found. Downloading and installing..." -ForegroundColor Yellow
    
    # Create ImDisk directory
    if (-not (Test-Path $imDiskDir)) {
        New-Item -ItemType Directory -Path $imDiskDir -Force | Out-Null
    }
    
    # Download ImDisk
    Write-Host "Downloading ImDisk from SourceForge..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $imDiskDownloadUrl -OutFile $imDiskInstaller -UseBasicParsing
        Write-Host "Download complete." -ForegroundColor Green
    } catch {
        Write-Error "Failed to download ImDisk. Please download manually from: $imDiskDownloadUrl"
        Write-Error "Then extract to: $imDiskDir"
        exit 1
    }
    
    # Extract ImDisk
    Write-Host "Extracting ImDisk..." -ForegroundColor Yellow
    try {
        Expand-Archive -Path $imDiskInstaller -DestinationPath $imDiskDir -Force
        Write-Host "Extraction complete." -ForegroundColor Green
    } catch {
        Write-Error "Failed to extract ImDisk archive."
        exit 1
    }
    
    # Clean up installer
    if (Test-Path $imDiskInstaller) {
        Remove-Item $imDiskInstaller -Force
    }
    
    # Verify installation
    if (-not (Test-Path $imDiskExe)) {
        Write-Error "ImDisk installation failed. imdisk.exe not found."
        exit 1
    }
    
    Write-Host "ImDisk installed successfully." -ForegroundColor Green
}

Write-Host ""

# Check if RAM disk is already mounted
$existingDrive = Get-PSDrive -Name $DriveLetter -ErrorAction SilentlyContinue
if ($existingDrive) {
    Write-Host "Drive $DriveLetter: is already in use." -ForegroundColor Yellow
    $unmount = Read-Host "Unmount existing drive? (Y/N)"
    if ($unmount -eq 'Y' -or $unmount -eq 'y') {
        Dismount-PSDrive -Name $DriveLetter -Force
    } else {
        exit 0
    }
}

# Check if ImDisk virtual disk already exists with this letter
$existingDisk = & $imDiskExe -l 2>$null | Select-String "$DriveLetter:"
if ($existingDisk) {
    Write-Host "Removing existing ImDisk volume on $DriveLetter:..." -ForegroundColor Yellow
    & $imDiskExe -D -L $DriveLetter -quiet
}

# Create RAM disk
Write-Host ""
Write-Host "Creating RAM disk..." -ForegroundColor Green
Write-Host "  Drive letter: $MountLetter" -ForegroundColor Green
Write-Host "  Size: $SizeMB MB ($([math]::Round($SizeMB/1024, 1)) GB)" -ForegroundColor Green
Write-Host "  File system: NTFS" -ForegroundColor Green
Write-Host ""

$createResult = & $imDiskExe -a -s $SizeMB MB -m $MountLetter -fs NTFS -p "/ts:1000"
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to create RAM disk. Error code: $LASTEXITCODE"
    exit 1
}

Write-Host "RAM disk created successfully!" -ForegroundColor Green
Write-Host ""

# Verify RAM disk
$ramDisk = Get-PSDrive -Name $DriveLetter -ErrorAction SilentlyContinue
if ($ramDisk) {
    $freeSpaceGB = [math]::Round($ramDisk.Description / 1GB, 2)
    Write-Host "Verified: $MountLetter is mounted and ready." -ForegroundColor Green
} else {
    # Try alternative verification
    if (Test-Path $MountLetter) {
        Write-Host "Verified: $MountLetter is mounted and ready." -ForegroundColor Green
    } else {
        Write-Warning "Could not verify RAM disk mount, but creation command succeeded."
    }
}

Write-Host ""

# Create persistence script for reboot
$persistenceScript = "$imDiskDir\mount-ramdisk.ps1"
$persistenceContent = @"
# Auto-mount RAM disk on boot
`"$imDiskExe`" -a -s $SizeMB MB -m $MountLetter -fs NTFS -p "/ts:1000" -quiet
"@

Set-Content -Path $persistenceScript -Value $persistenceContent -Encoding UTF8
Write-Host "Persistence script created: $persistenceScript" -ForegroundColor Green

# Create startup task for persistence
$taskName = "DoraMCU-RAMDisk"
Write-Host "Setting up Windows Task Scheduler for auto-mount on boot..." -ForegroundColor Yellow

# Remove existing task if present
Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue

# Create new scheduled task
$action = New-ScheduledTaskAction -Execute "PowerShell.exe" -Argument "-NoProfile -WindowStyle Hidden -File `"$persistenceScript`""
$trigger = New-ScheduledTaskTrigger -AtStartup
$principal = New-ScheduledTaskPrincipal -UserId "SYSTEM" -LogonType ServiceAccount -RunLevel Highest
$settings = New-ScheduledTaskSettingsSet -AllowStartIfOnBatteries -DontStopIfGoingOnBatteries -StartWhenAvailable

Register-ScheduledTask -TaskName $taskName -Action $action -Trigger $trigger -Principal $principal -Settings $settings -Force

Write-Host "Scheduled task created: $taskName" -ForegroundColor Green
Write-Host "RAM disk will auto-mount on system startup." -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RAM Disk Setup Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "RAM disk details:" -ForegroundColor Yellow
Write-Host "  Drive: $MountLetter" -ForegroundColor Yellow
Write-Host "  Size: $([math]::Round($SizeMB/1024, 1)) GB" -ForegroundColor Yellow
Write-Host "  Type: RAM disk (ImDisk)" -ForegroundColor Yellow
Write-Host "  Persists: Yes (auto-mount on boot)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Recommended usage:" -ForegroundColor Yellow
Write-Host "  1. Set GRADLE_USER_HOME=$MountLetter\.gradle for faster builds" -ForegroundColor Yellow
Write-Host "  2. Set JAVA_IO_TMPDIR=$MountLetter\tmp for faster temp file I/O" -ForegroundColor Yellow
Write-Host "  3. Store test data and build outputs on $MountLetter" -ForegroundColor Yellow
Write-Host ""
Write-Host "To set environment variables, run:" -ForegroundColor Gray
Write-Host "  [Environment]::SetEnvironmentVariable('GRADLE_USER_HOME', '$MountLetter\.gradle', 'User')" -ForegroundColor Gray
Write-Host "  [Environment]::SetEnvironmentVariable('JAVA_IO_TMPDIR', '$MountLetter\tmp', 'User')" -ForegroundColor Gray
Write-Host ""
Write-Host "To manually mount after reboot (if auto-mount fails):" -ForegroundColor Gray
Write-Host "  $persistenceScript" -ForegroundColor Gray
Write-Host ""

# Create tmp directory on RAM disk
$tmpDir = "$MountLetter\tmp"
if (-not (Test-Path $tmpDir)) {
    New-Item -ItemType Directory -Path $tmpDir -Force | Out-Null
    Write-Host "Created temporary directory: $tmpDir" -ForegroundColor Green
}

# Create .gradle directory on RAM disk
$gradleDir = "$MountLetter\.gradle"
if (-not (Test-Path $gradleDir)) {
    New-Item -ItemType Directory -Path $gradleDir -Force | Out-Null
    Write-Host "Created Gradle cache directory: $gradleDir" -ForegroundColor Green
}

Write-Host ""
Write-Host "Would you like to set the environment variables now? (Y/N)" -ForegroundColor Yellow
$response = Read-Host
if ($response -eq 'Y' -or $response -eq 'y') {
    [Environment]::SetEnvironmentVariable('GRADLE_USER_HOME', "$MountLetter\.gradle", 'User')
    [Environment]::SetEnvironmentVariable('JAVA_IO_TMPDIR', "$MountLetter\tmp", 'User')
    Write-Host ""
    Write-Host "Environment variables set!" -ForegroundColor Green
    Write-Host "  GRADLE_USER_HOME = $MountLetter\.gradle" -ForegroundColor Yellow
    Write-Host "  JAVA_IO_TMPDIR = $MountLetter\tmp" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Note: You may need to restart your terminal/IDE for changes to take effect." -ForegroundColor Yellow
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  All Done!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# DoraMCU Local Production Server Launcher
# Starts the production server on port 8080 with 3GB heap
# Usage: .\start-local-prod-server.ps1

$ErrorActionPreference = "Stop"

Write-Host "=== DoraMCU Local Production Server Launcher ===" -ForegroundColor Cyan
Write-Host ""

# Check if JAR exists
$jarPath = "build\libs\DoraMCU-0.6.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "ERROR: JAR not found at $jarPath" -ForegroundColor Red
    Write-Host "Run .\gradlew.bat bootJar -x test first to build." -ForegroundColor Yellow
    exit 1
}

# Check if production server is already running on port 8080
$portCheck = netstat -ano | Select-String "LISTENING" | Where-Object { $_ -match ":8080\s" }
if ($portCheck) {
    $pid = ($portCheck -split "\s+")[-1]
    Write-Host "WARNING: Production server already running on port 8080 (PID: $pid)" -ForegroundColor Yellow
    Write-Host "Stop it first with .\stop-local-prod-server.ps1" -ForegroundColor Yellow
    exit 1
}

# Create data and logs directories
New-Item -ItemType Directory -Force -Path "data", "logs", "tmp-prod" | Out-Null

Write-Host "Starting production server..." -ForegroundColor Green
Write-Host "  Port: 8080" -ForegroundColor White
Write-Host "  Database: H2 file-based (./data/doramcu)" -ForegroundColor White
Write-Host "  Heap: 1GB - 3GB" -ForegroundColor White
Write-Host "  Profile: production" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

# Copy pre-built database if production database doesn't exist
if (-not (Test-Path "data\doramcu.mv.db")) {
    Write-Host "Copying pre-built database from USB stick..." -ForegroundColor Yellow
    if (Test-Path "D:\doramcu-modules\data\doramcu.mv.db") {
        Copy-Item "D:\doramcu-modules\data\doramcu.mv.db" "data\doramcu.mv.db" -Force
        Write-Host "  Pre-built database copied (16 galaxies, no rebuild needed)." -ForegroundColor Green
    } else {
        Write-Host "  WARNING: Pre-built database not found on USB. Server will rebuild galaxies." -ForegroundColor Yellow
    }
}

# Start the production server
& java -Xms1g -Xmx3g `
    -Djava.io.tmpdir=./tmp-prod `
    -XX:+UseG1GC `
    -XX:MaxGCPauseMillis=200 `
    -XX:+HeapDumpOnOutOfMemoryError `
    -XX:HeapDumpPath=./logs/ `
    -Dlogging.file.name=logs/doramcu.log `
    -Dspring.profiles.active=production `
    -jar $jarPath

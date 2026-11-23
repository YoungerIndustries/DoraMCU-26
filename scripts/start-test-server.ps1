# DoraMCU Local Test Server Launcher
# Starts the test server on port 8081 with 2GB heap for pre-deployment testing
# Usage: .\start-test-server.ps1

$ErrorActionPreference = "Stop"

Write-Host "=== DoraMCU Test Server Launcher ===" -ForegroundColor Cyan
Write-Host ""

# Check if JAR exists
$jarPath = "build\libs\DoraMCU-0.6.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "ERROR: JAR not found at $jarPath" -ForegroundColor Red
    Write-Host "Run .\gradlew.bat bootJar -x test first to build." -ForegroundColor Yellow
    exit 1
}

# Check if test server is already running on port 8081
$portCheck = netstat -ano | Select-String "LISTENING" | Where-Object { $_ -match ":8081\s" }
if ($portCheck) {
    $pid = ($portCheck -split "\s+")[-1]
    Write-Host "WARNING: Test server already running on port 8081 (PID: $pid)" -ForegroundColor Yellow
    Write-Host "Stop it first with .\stop-test-server.ps1" -ForegroundColor Yellow
    exit 1
}

# Create data and logs directories
New-Item -ItemType Directory -Force -Path "data", "logs", "tmp" | Out-Null

Write-Host "Starting test server..." -ForegroundColor Green
Write-Host "  Port: 8081" -ForegroundColor White
Write-Host "  Database: H2 file-based (./data/doramcu-test)" -ForegroundColor White
Write-Host "  Heap: 1GB - 2GB" -ForegroundColor White
Write-Host "  Profile: test" -ForegroundColor White
Write-Host ""
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

# Copy pre-built database if test database doesn't exist
if (-not (Test-Path "data\doramcu-test.mv.db")) {
    Write-Host "Copying pre-built database from USB stick..." -ForegroundColor Yellow
    if (Test-Path "D:\doramcu-modules\data\doramcu.mv.db") {
        Copy-Item "D:\doramcu-modules\data\doramcu.mv.db" "data\doramcu-test.mv.db" -Force
        Write-Host "  Pre-built database copied (16 galaxies, no rebuild needed)." -ForegroundColor Green
    } else {
        Write-Host "  WARNING: Pre-built database not found on USB. Server will rebuild galaxies." -ForegroundColor Yellow
    }
}

# Start the test server
& java -Xms1g -Xmx2g -Djava.io.tmpdir=./tmp -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/ -Dlogging.file.name=logs/doramcu-test.log -Dspring.profiles.active=test -jar $jarPath

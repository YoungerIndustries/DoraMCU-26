# DoraMCU Brain Node Server Launcher
# Starts a PC server that registers with the Pi Brain Stem at 192.168.50.147:8080
# Usage: .\start-brain-node-server.ps1

$ErrorActionPreference = "Stop"

Write-Host "=== DoraMCU Brain Node Server Launcher ===" -ForegroundColor Cyan
Write-Host ""

# Check if JAR exists
$jarPath = "build\libs\DoraMCU-0.6.0.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "ERROR: JAR not found at $jarPath" -ForegroundColor Red
    Write-Host "Run .\gradlew.bat bootJar -x test first to build." -ForegroundColor Yellow
    exit 1
}

# Check if server is already running on port 8080
$portCheck = netstat -ano | Select-String "LISTENING" | Where-Object { $_ -match ":8080\s" }
if ($portCheck) {
    $pid = ($portCheck -split "\s+")[-1]
    Write-Host "WARNING: Server already running on port 8080 (PID: $pid)" -ForegroundColor Yellow
    Write-Host "Stop it first with .\stop-local-prod-server.ps1" -ForegroundColor Yellow
    exit 1
}

# Create data and logs directories
New-Item -ItemType Directory -Force -Path "data", "logs", "tmp-node" | Out-Null

# Copy pre-built database if not present
if (-not (Test-Path "data\doramcu.mv.db")) {
    Write-Host "Copying pre-built database from USB stick..." -ForegroundColor Yellow
    if (Test-Path "D:\doramcu-modules\data\doramcu.mv.db") {
        Copy-Item "D:\doramcu-modules\data\doramcu.mv.db" "data\doramcu.mv.db" -Force
        Write-Host "  Pre-built database copied (16 galaxies, no rebuild needed)." -ForegroundColor Green
    } else {
        Write-Host "  WARNING: Pre-built database not found on USB. Server will rebuild galaxies." -ForegroundColor Yellow
    }
}

Write-Host "Starting Brain Node server..." -ForegroundColor Green
Write-Host "  Port: 8080" -ForegroundColor White
Write-Host "  Brain Stem: http://192.168.50.147:8080" -ForegroundColor White
Write-Host "  Database: H2 file-based (./data/doramcu)" -ForegroundColor White
Write-Host "  Heap: 1GB - 3GB" -ForegroundColor White
Write-Host "  Profile: brain-node" -ForegroundColor White
Write-Host ""
Write-Host "This server will register with the Pi Brain Stem and sync game state." -ForegroundColor Cyan
Write-Host "Press Ctrl+C to stop the server" -ForegroundColor Yellow
Write-Host ""

# Start the brain node server
& java -Xms1g -Xmx3g -Djava.io.tmpdir=./tmp-node -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./logs/ -Dlogging.file.name=logs/doramcu-brain-node.log -Dspring.profiles.active=brain-node -jar $jarPath

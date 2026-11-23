# DoraMCU Local Development Launcher
# Starts both main server (port 8080) and news network (port 8084) with optimized memory
# Usage: .\start-local-dev.ps1

$ErrorActionPreference = "Stop"

Write-Host "=== DoraMCU Local Development Launcher ===" -ForegroundColor Cyan
Write-Host ""

# Check if JARs exist
$mainJar = "build\libs\DoraMCU-0.6.5.jar"
$newsJar = "dora-news-network\build\libs\dora-news-network-0.6.5.jar"

if (-not (Test-Path $mainJar)) {
    Write-Host "ERROR: Main server JAR not found at $mainJar" -ForegroundColor Red
    Write-Host "Run .\gradlew.bat bootJar -x test first to build." -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path $newsJar)) {
    Write-Host "ERROR: News network JAR not found at $newsJar" -ForegroundColor Red
    Write-Host "Run .\gradlew.bat :dora-news-network:bootJar -x test first to build." -ForegroundColor Yellow
    exit 1
}

# Check if ports are already in use
$port8080 = netstat -ano | Select-String "LISTENING" | Where-Object { $_ -match ":8080\s" }
$port8084 = netstat -ano | Select-String "LISTENING" | Where-Object { $_ -match ":8084\s" }

if ($port8080) {
    $portPid = ($port8080 -split "\s+")[-1]
    Write-Host "WARNING: Port 8080 already in use (PID: $portPid)" -ForegroundColor Yellow
    Write-Host "Stop the existing process first." -ForegroundColor Yellow
    exit 1
}

if ($port8084) {
    $portPid = ($port8084 -split "\s+")[-1]
    Write-Host "WARNING: Port 8084 already in use (PID: $portPid)" -ForegroundColor Yellow
    Write-Host "Stop the existing process first." -ForegroundColor Yellow
    exit 1
}

# Create directories
New-Item -ItemType Directory -Force -Path "data", "logs", "tmp-dev" | Out-Null

Write-Host "Starting local development environment..." -ForegroundColor Green
Write-Host ""
Write-Host "  Main Server:" -ForegroundColor White
Write-Host "    Port: 8080" -ForegroundColor Gray
Write-Host "    Heap: 512MB - 1GB (optimized for dev)" -ForegroundColor Gray
Write-Host "    Profile: dev" -ForegroundColor Gray
Write-Host ""
Write-Host "  News Network:" -ForegroundColor White
Write-Host "    Port: 8084" -ForegroundColor Gray
Write-Host "    Heap: 128MB - 256MB (lightweight proxy)" -ForegroundColor Gray
Write-Host "    Profile: dev" -ForegroundColor Gray
Write-Host ""
Write-Host "Total estimated memory: ~1.5 GB (vs ~6 GB for prod settings)" -ForegroundColor Yellow
Write-Host ""
Write-Host "Press Ctrl+C to stop both servers" -ForegroundColor Yellow
Write-Host ""

# Start main server in background
Write-Host "[1/2] Starting main server on port 8080..." -ForegroundColor Green
$mainProcess = Start-Process -NoNewWindow -PassThru `
    -FilePath "java" `
    -ArgumentList "-Xms512m", "-Xmx1g", `
        "-Djava.io.tmpdir=./tmp-dev", `
        "-XX:+UseG1GC", `
        "-XX:MaxGCPauseMillis=200", `
        "-Dspring.profiles.active=dev", `
        "-jar", $mainJar

Write-Host "  Main server started (PID: $($mainProcess.Id))" -ForegroundColor Green

# Wait for main server to be ready
Write-Host "  Waiting for main server to start..." -ForegroundColor Gray
$maxRetries = 60
$retryCount = 0
$serverReady = $false

while ($retryCount -lt $maxRetries -and -not $serverReady) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/news/feed?limit=1" -TimeoutSec 2 -UseBasicParsing -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $serverReady = $true
        }
    } catch {
        # Server not ready yet
    }
    if (-not $serverReady) {
        Start-Sleep -Milliseconds 1000
        $retryCount++
    }
}

if (-not $serverReady) {
    Write-Host "  WARNING: Main server slow to start, continuing anyway..." -ForegroundColor Yellow
} else {
    Write-Host "  Main server ready after $retryCount seconds" -ForegroundColor Green
}

# Start news network in background
Write-Host "[2/2] Starting news network on port 8084..." -ForegroundColor Green
$newsProcess = Start-Process -NoNewWindow -PassThru `
    -FilePath "java" `
    -ArgumentList "-Xms128m", "-Xmx256m", `
        "-XX:+UseG1GC", `
        "-Dspring.profiles.active=dev", `
        "-jar", $newsJar

Write-Host "  News network started (PID: $($newsProcess.Id))" -ForegroundColor Green
Write-Host ""
Write-Host "=== Development Environment Ready ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Main Server UI:    http://localhost:8080" -ForegroundColor White
Write-Host "  News Network UI:   http://localhost:8084" -ForegroundColor White
Write-Host "  API Docs:          http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host ""
Write-Host "PIDs: Main=$($mainProcess.Id), News=$($newsProcess.Id)" -ForegroundColor Gray
Write-Host ""

# Keep script running and monitor processes
try {
    while ($true) {
        Start-Sleep -Seconds 5
        
        # Check if main server is still running
        if ($mainProcess.HasExited) {
            Write-Host ""
            Write-Host "ERROR: Main server (PID $($mainProcess.Id)) exited unexpectedly!" -ForegroundColor Red
            Write-Host "Check logs/doramcu.log for details." -ForegroundColor Yellow
            break
        }
        
        # Check if news network is still running
        if ($newsProcess.HasExited) {
            Write-Host ""
            Write-Host "ERROR: News network (PID $($newsProcess.Id)) exited unexpectedly!" -ForegroundColor Red
            break
        }
    }
} finally {
    Write-Host ""
    Write-Host "Stopping development environment..." -ForegroundColor Yellow
    
    # Stop news network first
    if (-not $newsProcess.HasExited) {
        Write-Host "  Stopping news network (PID $($newsProcess.Id))..." -ForegroundColor Gray
        Stop-Process -Id $newsProcess.Id -Force -ErrorAction SilentlyContinue
    }
    
    # Stop main server
    if (-not $mainProcess.HasExited) {
        Write-Host "  Stopping main server (PID $($mainProcess.Id))..." -ForegroundColor Gray
        Stop-Process -Id $mainProcess.Id -Force -ErrorAction SilentlyContinue
    }
    
    Write-Host "  Servers stopped." -ForegroundColor Green
}

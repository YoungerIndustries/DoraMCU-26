# Deploy dora-news-network JAR to Pi via HTTP tunnel
$ErrorActionPreference = "Stop"

$PI_HOST = "192.168.1.111"
$PI_TUNNEL = "http://${PI_HOST}:9000/exec"
$PI_JAR_PATH = "/opt/doramcu/news-network/dora-news-network.jar"
$PC_IP = "192.168.1.120"
$PC_FILE_SERVER_PORT = 8888
$JAR_NAME = "dora-news-network-0.7.0.jar"

function Invoke-PiCommand {
    param([string]$Command, [int]$Timeout = 30000)
    $escapedCommand = $Command -replace '\\', '\\\\' -replace '"', '\"' -replace '\n', '\\n' -replace '\r', '\\r' -replace '\t', '\\t'
    $json = "{`"command`":`"$escapedCommand`",`"timeout`":$Timeout}"
    try {
        $response = Invoke-RestMethod -Uri $PI_TUNNEL -Method POST -ContentType "application/json" -Body ([System.Text.Encoding]::UTF8.GetBytes($json)) -TimeoutSec 300
        return $response
    } catch {
        Write-Host "  Command failed - $_" -ForegroundColor Yellow
        return $null
    }
}

Write-Host "=== Deploying Dora News Network JAR to Pi ===" -ForegroundColor Cyan

# Step 1: Stop the news network service
Write-Host ""
Write-Host "1. Stopping news network service..." -ForegroundColor Yellow
$result = Invoke-PiCommand -Command "sudo systemctl stop doramcu-news-network" -Timeout 15000
if ($result -and $result.code -eq 0) {
    Write-Host "  Service stopped" -ForegroundColor Green
}

# Step 2: Download JAR on Pi
Write-Host ""
Write-Host "2. Downloading JAR from file server..." -ForegroundColor Yellow
$downloadUrl = "http://${PC_IP}:${PC_FILE_SERVER_PORT}/dora-news-network/build/libs/${JAR_NAME}"
$downloadCmd = "curl -s -o ${PI_JAR_PATH}.new ${downloadUrl}"
$result = Invoke-PiCommand -Command $downloadCmd -Timeout 300000
if ($result -and $result.code -eq 0) {
    Write-Host "  Download successful" -ForegroundColor Green
} else {
    Write-Host "  Download failed: $($result.stderr)" -ForegroundColor Red
    exit 1
}

# Step 3: Verify download
Write-Host ""
Write-Host "3. Verifying download..." -ForegroundColor Yellow
$verifyCmd = "ls -la ${PI_JAR_PATH}.new ${PI_JAR_PATH} 2>/dev/null"
$result = Invoke-PiCommand -Command $verifyCmd -Timeout 10000
if ($result -and $result.stdout) {
    Write-Host "  $($result.stdout.Trim())" -ForegroundColor Gray
}

# Step 4: Replace JAR
Write-Host ""
Write-Host "4. Replacing JAR..." -ForegroundColor Yellow
$replaceCmd = "cp ${PI_JAR_PATH}.new ${PI_JAR_PATH} && rm ${PI_JAR_PATH}.new && chmod 755 ${PI_JAR_PATH}"
$result = Invoke-PiCommand -Command $replaceCmd -Timeout 30000
if ($result -and $result.code -eq 0) {
    Write-Host "  JAR replaced" -ForegroundColor Green
} else {
    Write-Host "  Failed: $($result.stderr)" -ForegroundColor Red
}

# Step 5: Start the service
Write-Host ""
Write-Host "5. Starting news network service..." -ForegroundColor Yellow
$result = Invoke-PiCommand -Command "sudo systemctl start doramcu-news-network" -Timeout 15000
if ($result -and $result.code -eq 0) {
    Write-Host "  Service started" -ForegroundColor Green
} else {
    Write-Host "  Failed: $($result.stderr)" -ForegroundColor Red
}

# Step 6: Wait and verify
Write-Host ""
Write-Host "Waiting for startup (~10 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

try {
    $healthCheck = Invoke-WebRequest -Uri "http://${PI_HOST}:8084/" -UseBasicParsing -TimeoutSec 10
    if ($healthCheck.StatusCode -eq 200) {
        Write-Host "  News network is responding!" -ForegroundColor Green
    }
} catch {
    Write-Host "  News network not yet responding" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Deployment Complete ===" -ForegroundColor Cyan
Write-Host "News Network: http://${PI_HOST}:8084/" -ForegroundColor Cyan

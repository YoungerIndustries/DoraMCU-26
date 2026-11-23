# Deploy JAR to Pi Brain Stem via HTTP tunnel (port 9000)
# Usage: .\scripts\deploy-jar.ps1
#
# This script builds the JAR, starts a local file server, downloads it on the Pi
# via the HTTP tunnel (port 9000), and restarts the server service.
# The file server + curl method is used because base64 echo chunking corrupts large files.

$ErrorActionPreference = "Stop"

$PI_HOST = "192.168.1.111"
$PI_TUNNEL = "http://${PI_HOST}:9000/exec"
$PI_JAR_PATH = "/opt/doramcu/server/doramcu-server.jar"
$PC_FILE_SERVER_PORT = 8888
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir

# Find the latest built JAR dynamically
$libsDir = Join-Path $ProjectRoot "build\libs"
$jarFiles = Get-ChildItem -Path $libsDir -Filter "DoraMCU-*.jar" -File | Sort-Object Name -Descending
if ($jarFiles.Count -eq 0) {
    Write-Error "No JAR found in $libsDir. Build first: .\gradlew.bat bootJar -x test"
    exit 1
}
$JAR_PATH = $jarFiles[0].FullName
$JAR_VERSION = $jarFiles[0].Name -replace 'DoraMCU-([0-9.]+)\.jar$', '$1'

Write-Host "=== Deploying JAR to Pi ===" -ForegroundColor Cyan
Write-Host "JAR: $JAR_PATH (v$JAR_VERSION)"
Write-Host "Target: $PI_JAR_PATH via tunnel at $PI_TUNNEL"
Write-Host ""

$jarSize = (Get-Item $JAR_PATH).Length
Write-Host "JAR size: $([math]::Round($jarSize / 1MB, 2)) MB"

# Helper function to run commands on Pi via tunnel
function Invoke-PiCommand {
    param([string]$Command, [int]$Timeout = 30000)

    # Properly escape the command for JSON string (handle backslashes, quotes, and control chars)
    $escapedCommand = $Command -replace '\\', '\\\\' -replace '"', '\"' -replace '\n', '\\n' -replace '\r', '\\r' -replace '\t', '\\t'
    $json = "{`"command`":`"$escapedCommand`",`"timeout`":$Timeout}"
    try {
        $response = Invoke-RestMethod -Uri $PI_TUNNEL -Method POST -ContentType "application/json" -Body ([System.Text.Encoding]::UTF8.GetBytes($json)) -TimeoutSec 300
        return $response
    } catch {
        Write-Host "  Warning: Command failed - $_" -ForegroundColor Yellow
        return $null
    }
}

# Get PC's ethernet IP (192.168.1.x subnet — same LAN as Pi ethernet)
$pcIpList = @(Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -like "192.168.1.*" } | Select-Object -ExpandProperty IPAddress)
$pcIp = $pcIpList[0]
if (-not $pcIp) {
    Write-Error "Cannot find PC ethernet IP (192.168.1.x). Make sure ethernet is connected."
    exit 1
}
Write-Host "PC ethernet IP: $pcIp" -ForegroundColor Gray

# Step 1: Stop the server on Pi
Write-Host ""
Write-Host "1. Stopping server on Pi..." -ForegroundColor Yellow
$result = Invoke-PiCommand -Command "sudo systemctl stop doramcu-server" -Timeout 15000
if ($result -and $result.code -ne 0) {
    Write-Host "  Stop stderr: $($result.stderr)" -ForegroundColor Yellow
}
Start-Sleep -Seconds 2

# Step 2: Start local file server
Write-Host ""
Write-Host "2. Starting file server on port $PC_FILE_SERVER_PORT..." -ForegroundColor Yellow
$fileServerScript = Join-Path $ScriptDir "file-server-temp.js"
if (-not (Test-Path $fileServerScript)) {
    Write-Error "File server script not found: $fileServerScript"
    exit 1
}

# Check if Node.js is available
try {
    $nodeVersion = node --version 2>$null
    if (-not $nodeVersion) { throw "Node.js not found" }
} catch {
    Write-Error "Node.js is required. Install from https://nodejs.org/"
    exit 1
}

$fileServerProcess = Start-Process -FilePath "node" -ArgumentList $fileServerScript -NoNewWindow -PassThru
Start-Sleep -Seconds 2  # Give the file server time to start

# Verify file server is running
try {
    $testReq = Invoke-WebRequest -Uri "http://localhost:$PC_FILE_SERVER_PORT/" -UseBasicParsing -TimeoutSec 3
    Write-Host "  File server started (PID: $($fileServerProcess.Id))" -ForegroundColor Green
} catch {
    Write-Host "  Warning: File server may not be ready yet, continuing..." -ForegroundColor Yellow
}

# Step 3: Download JAR on Pi via curl (through tunnel)
# The file server serves from the DoraMCU project root, so the path must include build/libs/
Write-Host ""
Write-Host "3. Downloading JAR on Pi from file server..." -ForegroundColor Yellow
$jarFileName = [System.IO.Path]::GetFileName($JAR_PATH)
$downloadCmd = "curl -s -o ${PI_JAR_PATH}.new http://${pcIp}:${PC_FILE_SERVER_PORT}/build/libs/${jarFileName}"
$result = Invoke-PiCommand -Command $downloadCmd -Timeout 300000
if ($result) {
    if ($result.code -eq 0) {
        Write-Host "  Download successful" -ForegroundColor Green
    } else {
        Write-Host "  Download failed (code $($result.code)): $($result.stderr)" -ForegroundColor Red
        Stop-Process -Id $fileServerProcess.Id -Force
        exit 1
    }
}

# Step 4: Verify downloaded file size
Write-Host ""
Write-Host "4. Verifying downloaded file..." -ForegroundColor Yellow
$verifyCmd = "ls -la ${PI_JAR_PATH}.new ${PI_JAR_PATH} 2>/dev/null"
$result = Invoke-PiCommand -Command $verifyCmd -Timeout 10000
if ($result -and $result.stdout) {
    Write-Host "  $($result.stdout.Trim())" -ForegroundColor Gray
}

# Step 5: Replace the JAR and set permissions
Write-Host ""
Write-Host "5. Replacing JAR and setting permissions..." -ForegroundColor Yellow
$replaceCmd = "cp ${PI_JAR_PATH}.new ${PI_JAR_PATH} && rm ${PI_JAR_PATH}.new && chmod 755 ${PI_JAR_PATH}"
$result = Invoke-PiCommand -Command $replaceCmd -Timeout 30000
if ($result -and $result.code -eq 0) {
    Write-Host "  JAR replaced successfully" -ForegroundColor Green
} else {
    Write-Host "  Failed to replace JAR: $($result.stderr)" -ForegroundColor Red
}

# Step 6: Start the server
Write-Host ""
Write-Host "6. Starting server..." -ForegroundColor Yellow
$result = Invoke-PiCommand -Command "sudo systemctl start doramcu-server" -Timeout 15000
if ($result -and $result.code -eq 0) {
    Write-Host "  Server started" -ForegroundColor Green
} else {
    Write-Host "  Failed to start server: $($result.stderr)" -ForegroundColor Red
}

# Step 7: Stop the file server
Write-Host ""
Write-Host "7. Cleaning up..." -ForegroundColor Yellow
Stop-Process -Id $fileServerProcess.Id -Force
Write-Host "  File server stopped" -ForegroundColor Gray

# Step 8: Wait and verify
Write-Host ""
Write-Host "Waiting for server startup (~30 seconds)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

Write-Host "Verifying server health..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-WebRequest -Uri "http://${PI_HOST}:8080/" -UseBasicParsing -TimeoutSec 10
    if ($healthCheck.StatusCode -eq 200) {
        Write-Host "  Server is responding!" -ForegroundColor Green
    }
} catch {
    Write-Host "  Server not yet responding (may still be starting up)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Deployment Complete ===" -ForegroundColor Cyan
Write-Host "Version: v$JAR_VERSION" -ForegroundColor Cyan
Write-Host "Server: http://${PI_HOST}:8080/" -ForegroundColor Cyan
Write-Host "Health: http://${PI_HOST}:8080/api/v1/4x/health" -ForegroundColor Cyan

# Deploy web client files to Pi Brain Stem via HTTP tunnel (port 9000)
# Usage: .\scripts\deploy-web-client.ps1
#
# This script uses a local file server + curl via the HTTP tunnel (port 9000)
# for reliable file transfer. SSH is unreliable due to keepalive issues.

$ErrorActionPreference = "Stop"

$PI_HOST = "192.168.1.111"
$PI_TUNNEL = "http://${PI_HOST}:9000/exec"
$PI_WEB_DIR = "/opt/doramcu/web"
$PC_FILE_SERVER_PORT = 8888
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = Split-Path -Parent $ScriptDir
$LOCAL_WEB_DIR = Join-Path $ProjectRoot "dora-web"

Write-Host "=== Deploying Web Client to Pi ===" -ForegroundColor Cyan
Write-Host "Target: ${PI_WEB_DIR} via tunnel at ${PI_TUNNEL}"
Write-Host ""

# Helper function to run commands on Pi via tunnel
function Invoke-PiCommand {
    param([string]$Command, [int]$Timeout = 10000)

    # Properly escape the command for JSON
    $escapedCommand = $Command.Replace('\', '\\').Replace('"', '\"')
    $jsonBody = [System.Text.Encoding]::UTF8.GetBytes("{`"command`":`"$escapedCommand`",`"timeout`":$Timeout}")
    try {
        $response = Invoke-RestMethod -Uri $PI_TUNNEL -Method POST -ContentType "application/json" -Body $jsonBody -TimeoutSec 15
        return $response
    } catch {
        Write-Host "  Warning: Command failed - $_" -ForegroundColor Yellow
        return $null
    }
}

# Get PC's ethernet IP (192.168.1.x subnet — same LAN as Pi ethernet)
$pcIp = (Get-NetIPAddress -AddressFamily IPv4 | Where-Object { $_.IPAddress -like "192.168.1.*" }).IPAddress[0]
if (-not $pcIp) {
    Write-Error "Cannot find PC ethernet IP (192.168.1.x). Make sure ethernet is connected."
    exit 1
}
Write-Host "PC ethernet IP: $pcIp" -ForegroundColor Gray

# Collect all files to deploy
Write-Host "Collecting files to deploy..." -ForegroundColor Yellow
$filesToDeploy = @()

# JS files
$jsFiles = Get-ChildItem -Path "$LOCAL_WEB_DIR\js" -Filter "*.js" -File -Recurse -ErrorAction SilentlyContinue
foreach ($f in $jsFiles) {
    $relPath = $f.FullName.Substring($LOCAL_WEB_DIR.Length).TrimStart('\', '/')
    $filesToDeploy += @{ LocalFile = $f.FullName; RemotePath = "${PI_WEB_DIR}/js/${relPath}" }
}

# Game files (HTML, JS at root of game/)
$gameFiles = Get-ChildItem -Path "$LOCAL_WEB_DIR\game" -Include "*.js", "*.html" -File -ErrorAction SilentlyContinue
foreach ($f in $gameFiles) {
    $relPath = $f.Name
    $filesToDeploy += @{ LocalFile = $f.FullName; RemotePath = "${PI_WEB_DIR}/game/${relPath}" }
}

# Game views
$gameViewFiles = Get-ChildItem -Path "$LOCAL_WEB_DIR\game\js\views" -Filter "*.js" -File -ErrorAction SilentlyContinue
foreach ($f in $gameViewFiles) {
    $filesToDeploy += @{ LocalFile = $f.FullName; RemotePath = "${PI_WEB_DIR}/game/js/views/${f.Name}" }
}

# Game CSS
$gameCssFiles = Get-ChildItem -Path "$LOCAL_WEB_DIR\game\css" -Filter "*.css" -File -ErrorAction SilentlyContinue
foreach ($f in $gameCssFiles) {
    $filesToDeploy += @{ LocalFile = $f.FullName; RemotePath = "${PI_WEB_DIR}/game/css/${f.Name}" }
}

# Root files
if (Test-Path "$LOCAL_WEB_DIR\sw.js") {
    $filesToDeploy += @{ LocalFile = "$LOCAL_WEB_DIR\sw.js"; RemotePath = "${PI_WEB_DIR}/sw.js" }
}
if (Test-Path "$LOCAL_WEB_DIR\manifest.json") {
    $filesToDeploy += @{ LocalFile = "$LOCAL_WEB_DIR\manifest.json"; RemotePath = "${PI_WEB_DIR}/manifest.json" }
}

Write-Host "Files to deploy: $($filesToDeploy.Count)" -ForegroundColor Gray

if ($filesToDeploy.Count -eq 0) {
    Write-Error "No files found in $LOCAL_WEB_DIR. Check the web client directory."
    exit 1
}

# Create remote directories
Write-Host ""
Write-Host "Creating directories on Pi..." -ForegroundColor Yellow
Invoke-PiCommand -Command "mkdir -p ${PI_WEB_DIR}/js/views ${PI_WEB_DIR}/game/js/views ${PI_WEB_DIR}/game/css" | Out-Null

# Start local file server
Write-Host ""
Write-Host "Starting file server on port $PC_FILE_SERVER_PORT..." -ForegroundColor Yellow
$fileServerScript = Join-Path $ScriptDir "file-server-temp.js"
if (-not (Test-Path $fileServerScript)) {
    Write-Error "File server script not found: $fileServerScript"
    exit 1
}

try {
    $nodeVersion = node --version 2>$null
    if (-not $nodeVersion) { throw "Node.js not found" }
} catch {
    Write-Error "Node.js is required. Install from https://nodejs.org/"
    exit 1
}

$fileServerProcess = Start-Process -FilePath "node" -ArgumentList $fileServerScript -NoNewWindow -PassThru
Start-Sleep -Seconds 2

try {
    $testReq = Invoke-WebRequest -Uri "http://localhost:$PC_FILE_SERVER_PORT/" -UseBasicParsing -TimeoutSec 3
    Write-Host "  File server started (PID: $($fileServerProcess.Id))" -ForegroundColor Green
} catch {
    Write-Host "  Warning: File server may not be ready yet, continuing..." -ForegroundColor Yellow
}

# Deploy each file via curl on Pi
Write-Host ""
Write-Host "Deploying files..." -ForegroundColor Yellow
$successCount = 0
$failCount = 0

foreach ($entry in $filesToDeploy) {
    $localFile = $entry.LocalFile
    $remotePath = $entry.RemotePath
    $fileName = [System.IO.Path]::GetFileName($localFile)
    $relDisplay = $remotePath.Substring($PI_WEB_DIR.Length + 1)

    # Download via curl on Pi
    # File server serves from DoraMCU root, so path is dora-web/ + relative path within dora-web/
    $relPath = $localFile.Substring($LOCAL_WEB_DIR.Length).TrimStart('\', '/').Replace('\', '/')
    $downloadCmd = "curl -s -o '$remotePath' http://${pcIp}:${PC_FILE_SERVER_PORT}/dora-web/${relPath}"
    $result = Invoke-PiCommand -Command $downloadCmd -Timeout 30000

    if ($result -and $result.code -eq 0) {
        Write-Host "  [OK] $relDisplay" -ForegroundColor Green
        $successCount++
    } else {
        Write-Host "  [FAIL] $relDisplay" -ForegroundColor Red
        $failCount++
    }
}

# Stop the file server
Write-Host ""
Write-Host "Stopping file server..." -ForegroundColor Yellow
Stop-Process -Id $fileServerProcess.Id -Force
Write-Host "  File server stopped" -ForegroundColor Gray

# Set permissions on deployed files
Write-Host ""
Write-Host "Setting permissions..." -ForegroundColor Yellow
Invoke-PiCommand -Command "chmod -R 644 ${PI_WEB_DIR}" | Out-Null

Write-Host ""
Write-Host "=== Deployment Summary ===" -ForegroundColor Cyan
Write-Host "Successful: $successCount" -ForegroundColor Green
if ($failCount -gt 0) {
    Write-Host "Failed: $failCount" -ForegroundColor Red
}

# Verify key files
Write-Host ""
Write-Host "Verifying deployment..." -ForegroundColor Yellow
$filesToCheck = @("js/dtos.js", "js/api-client.js", "game/index.html", "game/game-app.js", "game/css/game.css")
foreach ($file in $filesToCheck) {
    $checkCmd = "if [ -f ${PI_WEB_DIR}/${file} ]; then echo OK; else echo MISSING; fi"
    $result = Invoke-PiCommand -Command $checkCmd -Timeout 5000
    if ($result -and $result.stdout -match "OK") {
        Write-Host "  [OK] ${file}" -ForegroundColor Green
    } else {
        Write-Host "  [MISSING] ${file}" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Web client available at: http://${PI_HOST}:8080/game/index.html" -ForegroundColor Cyan

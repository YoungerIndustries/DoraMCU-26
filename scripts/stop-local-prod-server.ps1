# Stop DoraMCU Local Production Server (Port 8080)
# Usage: .\stop-local-prod-server.ps1

$portCheck = netstat -ano | Select-String "LISTENING" | Where-Object { $_ -match ":8080\s" }
if ($portCheck) {
    $pid = ($portCheck -split "\s+")[-1]
    Write-Host "Stopping production server on port 8080 (PID: $pid)..." -ForegroundColor Yellow
    Stop-Process -Id $pid -Force
    Start-Sleep -Seconds 2
    Write-Host "Production server stopped." -ForegroundColor Green
} else {
    Write-Host "No production server running on port 8080." -ForegroundColor Cyan
}

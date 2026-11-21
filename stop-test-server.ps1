# Stop DoraMCU Test Server (Port 8081)
# Usage: .\stop-test-server.ps1

$portCheck = netstat -ano | Select-String "LISTENING" | Where-Object { $_ -match ":8081\s" }
if ($portCheck) {
    $pid = ($portCheck -split "\s+")[-1]
    Write-Host "Stopping test server on port 8081 (PID: $pid)..." -ForegroundColor Yellow
    Stop-Process -Id $pid -Force
    Start-Sleep -Seconds 2
    Write-Host "Test server stopped." -ForegroundColor Green
} else {
    Write-Host "No test server running on port 8081." -ForegroundColor Cyan
}

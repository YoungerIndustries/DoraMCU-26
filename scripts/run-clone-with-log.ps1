# Wrapper script that runs clone-sd-to-image.ps1 and logs all output
$logFile = "D:\pi-clone-log.txt"
$startTime = Get-Date

"[$startTime] === Clone Script Started ===" | Out-File $logFile
"[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Running as Admin: $([Security.Principal.WindowsIdentity]::GetCurrent().Name)" | Out-File $logFile -Append
"[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Arguments: $args" | Out-File $logFile -Append
"[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Working directory: $PWD" | Out-File $logFile -Append

try {
    & "C:\Users\kenba\IdeaProjects\DoraMCU\scripts\clone-sd-to-image.ps1" `
        -DiskNumber 2 `
        -WifiSsid "Crown9" `
        -WifiPassword "Crown9baby" `
        -OutputPath "D:\doramcu-pi-image.img" `
        -ImageSizeGB 16 `
        -SkipImageBuild *>&1 | Tee-Object -Append -FilePath $logFile
} catch {
    "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] ERROR: $_" | Out-File $logFile -Append
}

"[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] === Clone Script Finished ===" | Out-File $logFile -Append
"[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] Exit code: $LASTEXITCODE" | Out-File $logFile -Append

if (Test-Path "D:\doramcu-pi-image.img") {
    $size = [math]::Round((Get-Item "D:\doramcu-pi-image.img").Length / 1GB, 2)
    "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] SUCCESS: Image created - ${size}GB" | Out-File $logFile -Append
} else {
    "[$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')] FAILED: Image file not found" | Out-File $logFile -Append
}

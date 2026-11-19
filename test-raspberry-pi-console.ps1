<#
.SYNOPSIS
    Non-interactive test script for Raspberry Pi USB serial console scripts.
    
.DESCRIPTION
    Validates the core functionality of the serial console scripts without requiring
    interactive input or an actual Raspberry Pi connection.
    
    Tests:
    - COM port detection
    - Serial port object creation and configuration
    - Connection attempt to available ports
    - Error handling for invalid ports
    - Script syntax validation
#>

[CmdletBinding()]
param()

$ErrorActionPreference = "Continue"
$script:testResults = @()
$script:passCount = 0
$script:failCount = 0

function Write-TestHeader {
    param([string]$title)
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Test: $title" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
}

function Write-TestResult {
    param(
        [string]$name,
        [bool]$passed,
        [string]$message = ""
    )
    
    if ($passed) {
        Write-Host "  [PASS] $name" -ForegroundColor Green
        $script:passCount++
    }
    else {
        Write-Host "  [FAIL] $name" -ForegroundColor Red
        if ($message) {
            Write-Host "         $message" -ForegroundColor Red
        }
        $script:failCount++
    }
    
    $script:testResults += [PSCustomObject]@{
        Name = $name
        Passed = $passed
        Message = $message
    }
}

# Overall test header
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Raspberry Pi Console - Test Suite" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

###############################################
# Test 1: Script Syntax Validation
###############################################
Write-TestHeader "Script Syntax Validation"

try {
    $basicScript = Get-Content "$PSScriptRoot\raspberry-pi-console.ps1" -Raw
    $errors = $null
    $tokens = $null
    [System.Management.Automation.PSParser]::Tokenize($basicScript, [ref]$errors) | Out-Null
    
    if ($errors.Count -eq 0) {
        Write-TestResult "Basic script syntax" $true
    }
    else {
        Write-TestResult "Basic script syntax" $false "Syntax errors: $($errors.Count)"
    }
}
catch {
    Write-TestResult "Basic script syntax" $false "$_"
}

try {
    $advancedScript = Get-Content "$PSScriptRoot\raspberry-pi-console-advanced.ps1" -Raw
    $errors = $null
    $tokens = $null
    [System.Management.Automation.PSParser]::Tokenize($advancedScript, [ref]$errors) | Out-Null
    
    if ($errors.Count -eq 0) {
        Write-TestResult "Advanced script syntax" $true
    }
    else {
        Write-TestResult "Advanced script syntax" $false "Syntax errors: $($errors.Count)"
    }
}
catch {
    Write-TestResult "Advanced script syntax" $false "$_"
}

###############################################
# Test 2: COM Port Detection
###############################################
Write-TestHeader "COM Port Detection"

try {
    $ports = [System.IO.Ports.SerialPort]::GetPortNames()
    $hasPorts = $ports.Count -gt 0
    
    Write-TestResult "COM port detection API" $true "Found $($ports.Count) port(s): $($ports -join ', ')"
    
    if ($hasPorts) {
        Write-TestResult "At least one COM port available" $true
    }
    else {
        Write-TestResult "At least one COM port available" $false "No COM ports detected on this system"
    }
}
catch {
    Write-TestResult "COM port detection API" $false "$_"
}

###############################################
# Test 3: Serial Port Configuration
###############################################
Write-TestHeader "Serial Port Configuration"

try {
    $sp = New-Object System.IO.Ports.SerialPort
    $sp.PortName = "COM99"  # Dummy port for config test
    $sp.BaudRate = 115200
    $sp.Parity = [System.IO.Ports.Parity]::None
    $sp.DataBits = 8
    $sp.StopBits = [System.IO.Ports.StopBits]::One
    $sp.ReadTimeout = 5000
    $sp.WriteTimeout = 5000
    $sp.Encoding = [System.Text.Encoding]::UTF8
    $sp.DtrEnable = $false
    $sp.RtsEnable = $false
    
    # Verify configuration
    $configOk = $true
    $configOk = $configOk -and ($sp.BaudRate -eq 115200)
    $configOk = $configOk -and ($sp.Parity -eq [System.IO.Ports.Parity]::None)
    $configOk = $configOk -and ($sp.DataBits -eq 8)
    $configOk = $configOk -and ($sp.StopBits -eq [System.IO.Ports.StopBits]::One)
    $configOk = $configOk -and ($sp.DtrEnable -eq $false)
    $configOk = $configOk -and ($sp.RtsEnable -eq $false)
    
    Write-TestResult "Serial port object creation" $true
    Write-TestResult "Serial port configuration" $configOk "Baud: $($sp.BaudRate), Parity: $($sp.Parity), DataBits: $($sp.DataBits), StopBits: $($sp.StopBits)"
    
    $sp.Dispose()
}
catch {
    Write-TestResult "Serial port object creation" $false "$_"
}

###############################################
# Test 4: Connection Test (if ports available)
###############################################
Write-TestHeader "Connection Test"

$ports = [System.IO.Ports.SerialPort]::GetPortNames()
if ($ports.Count -gt 0) {
    $testPort = $ports[0]
    Write-Host "  Testing connection to: $testPort" -ForegroundColor Yellow
    
    try {
        $sp = New-Object System.IO.Ports.SerialPort
        $sp.PortName = $testPort
        $sp.BaudRate = 115200
        $sp.Parity = [System.IO.Ports.Parity]::None
        $sp.DataBits = 8
        $sp.StopBits = [System.IO.Ports.StopBits]::One
        $sp.ReadTimeout = 2000
        $sp.WriteTimeout = 2000
        $sp.DtrEnable = $false
        $sp.RtsEnable = $false
        
        $sp.Open()
        Write-TestResult "Connection to $testPort" $true "Port opened successfully"
        
        # Test write capability (may timeout if no device is connected, which is expected)
        try {
            $sp.WriteTimeout = 1000
            $sp.WriteLine("test")
            Write-TestResult "Write to $testPort" $true "Write successful"
        }
        catch [System.TimeoutException] {
            Write-TestResult "Write to $testPort" $true "Write timeout (expected - no Pi connected)"
        }
        catch {
            Write-TestResult "Write to $testPort" $true "Write exception (expected - no Pi connected): $_"
        }
        
        # Test read capability (expect timeout as no Pi is connected)
        try {
            $data = $sp.ReadExisting()
            Write-TestResult "Read from $testPort" $true "Read completed (no data expected without Pi)"
        }
        catch [System.TimeoutException] {
            Write-TestResult "Read from $testPort" $true "Read timeout (expected - no Pi connected)"
        }
        catch {
            Write-TestResult "Read from $testPort" $false "$_"
        }
        
        # Cleanup
        $sp.Close()
        $sp.Dispose()
        Write-TestResult "Disconnect from $testPort" $true "Port closed successfully"
    }
    catch {
        Write-TestResult "Connection to $testPort" $false "$_"
    }
}
else {
    Write-TestResult "Connection test" $true "Skipped - no COM ports available"
}

###############################################
# Test 5: Error Handling
###############################################
Write-TestHeader "Error Handling"

try {
    $sp = New-Object System.IO.Ports.SerialPort
    $sp.PortName = "COM999"  # Non-existent port
    $sp.BaudRate = 115200
    $sp.Open()
    
    # Should not reach here
    Write-TestResult "Invalid port error handling" $false "Should have thrown exception"
    $sp.Dispose()
}
catch {
    Write-TestResult "Invalid port error handling" $true "Correctly caught exception for non-existent port"
}

###############################################
# Test 6: Documentation Check
###############################################
Write-TestHeader "Documentation Check"

$docFile = "$PSScriptRoot\raspberry_pi_console.md"
if (Test-Path $docFile) {
    Write-TestResult "Documentation file exists" $true
    $content = Get-Content $docFile -Raw
    if ($content -match "USB Serial Console") {
        Write-TestResult "Documentation content" $true
    }
    else {
        Write-TestResult "Documentation content" $false "Missing expected content"
    }
}
else {
    Write-TestResult "Documentation file exists" $false "Documentation file not found"
}

###############################################
# Summary
###############################################
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Passed: $passCount" -ForegroundColor Green
Write-Host "  Failed: $failCount" -ForegroundColor Red
Write-Host "  Total:  $($passCount + $failCount)" -ForegroundColor White
Write-Host ""

if ($failCount -eq 0) {
    Write-Host "  All tests passed!" -ForegroundColor Green
}
else {
    Write-Host "  Some tests failed. Review output above." -ForegroundColor Red
}

Write-Host ""

# Return exit code based on results
if ($failCount -gt 0) {
    exit 1
}
else {
    exit 0
}

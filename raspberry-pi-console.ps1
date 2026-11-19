<#
.SYNOPSIS
    Interactive serial console for controlling Raspberry Pi via USB connection.

.DESCRIPTION
    This script provides an interactive terminal interface for communicating with a Raspberry Pi
    over USB serial connection. It supports both USB-to-TTL serial adapters and USB gadget mode
    on Pi Zero/Zero 2 W.

    Features:
    - Automatic COM port detection
    - Interactive command input
    - Real-time output display
    - Connection status monitoring
    - Proper serial settings (115200 baud, 8N1)

.PREREQUISITES
    - Windows PowerShell 5.1 or PowerShell 7+
    - USB-to-TTL serial adapter or Raspberry Pi Zero with USB gadget mode enabled
    - Appropriate drivers installed (CH340, FTDI, CP2102, etc.)

.EXAMPLE
    .\raspberry-pi-console.ps1
    # Auto-detects and connects to available COM port

.EXAMPLE
    .\raspberry-pi-console.ps1 -PortName "COM4"
    # Connects to specific COM port

.EXAMPLE
    .\raspberry-pi-console.ps1 -PortName "COM4" -BaudRate 9600
    # Connects with custom baud rate
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$PortName,

    [Parameter(Mandatory=$false)]
    [int]$BaudRate = 115200,

    [Parameter(Mandatory=$false)]
    [System.IO.Ports.Parity]$Parity = [System.IO.Ports.Parity]::None,

    [Parameter(Mandatory=$false)]
    [int]$DataBits = 8,

    [Parameter(Mandatory=$false)]
    [System.IO.Ports.StopBits]$StopBits = [System.IO.Ports.StopBits]::One,

    [Parameter(Mandatory=$false)]
    [int]$ReadTimeout = 5000,

    [Parameter(Mandatory=$false)]
    [int]$WriteTimeout = 5000
)

$ErrorActionPreference = "Continue"

# Global variables
$serialPort = $null
$connected = $false
$exitConsole = $false

function Show-Banner {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Raspberry Pi USB Serial Console" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
}

function Get-AvailablePorts {
    Write-Host "Scanning for available COM ports..." -ForegroundColor Yellow
    $ports = [System.IO.Ports.SerialPort]::GetPortNames()
    
    if ($ports.Count -eq 0) {
        Write-Host "No COM ports found!" -ForegroundColor Red
        Write-Host ""
        Write-Host "Troubleshooting:" -ForegroundColor Yellow
        Write-Host "1. Check Device Manager for your USB serial adapter" -ForegroundColor White
        Write-Host "2. Install appropriate drivers (CH340, FTDI, CP2102, etc.)" -ForegroundColor White
        Write-Host "3. Ensure the Raspberry Pi is connected via USB" -ForegroundColor White
        Write-Host "4. For Pi Zero: Enable USB gadget mode in cmdline.txt" -ForegroundColor White
        return @()
    }
    
    Write-Host "Found $($ports.Count) COM port(s):" -ForegroundColor Green
    for ($i = 0; $i -lt $ports.Count; $i++) {
        Write-Host "  [$($i+1)] $($ports[$i])" -ForegroundColor White
    }
    Write-Host ""
    
    return $ports
}

function Select-Port {
    param([string[]]$ports)
    
    if ($ports.Count -eq 1) {
        Write-Host "Auto-selecting: $($ports[0])" -ForegroundColor Green
        return $ports[0]
    }
    
    do {
        $selection = Read-Host "Select port number (1-$($ports.Count)) or enter port name"
        
        if ([int]::TryParse($selection, [ref]$null)) {
            $index = [int]$selection - 1
            if ($index -ge 0 -and $index -lt $ports.Count) {
                return $ports[$index]
            }
        }
        elseif ($ports.Contains($selection)) {
            return $selection
        }
        
        Write-Host "Invalid selection. Please try again." -ForegroundColor Red
    } while ($true)
}

function Connect-Serial {
    param([string]$port)
    
    try {
        Write-Host "Connecting to $port..." -ForegroundColor Yellow
        Write-Host "Settings: $($BaudRate) baud, $DataBits data bits, $Parity parity, $StopBits stop bits" -ForegroundColor Gray
        
        $serialPort = New-Object System.IO.Ports.SerialPort
        $serialPort.PortName = $port
        $serialPort.BaudRate = $BaudRate
        $serialPort.Parity = $Parity
        $serialPort.DataBits = $DataBits
        $serialPort.StopBits = $StopBits
        $serialPort.ReadTimeout = $ReadTimeout
        $serialPort.WriteTimeout = $WriteTimeout
        $serialPort.Encoding = [System.Text.Encoding]::UTF8
        
        # Disable DTR/RTS to prevent reset on some Pi models
        $serialPort.DtrEnable = $false
        $serialPort.RtsEnable = $false
        
        $serialPort.Open()
        $connected = $true
        
        Write-Host "Connected to $port successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Type 'help' for commands, 'exit' or 'quit' to disconnect." -ForegroundColor Yellow
        Write-Host "----------------------------------------" -ForegroundColor Gray
        
        return $true
    }
    catch {
        Write-Host "Failed to connect to ${port}:" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
        return $false
    }
}

function Disconnect-Serial {
    if ($serialPort -and $serialPort.IsOpen) {
        try {
            $serialPort.Close()
            $serialPort.Dispose()
        }
        catch {
            # Ignore cleanup errors
        }
        $serialPort = $null
    }
    $connected = $false
    Write-Host "Disconnected." -ForegroundColor Yellow
}

function Read-SerialOutput {
    if (-not $connected -or -not $serialPort) {
        return
    }
    
    try {
        # Check for available data
        while ($serialPort.BytesToRead -gt 0) {
            $data = $serialPort.ReadExisting()
            if ($data) {
                # Write output without newline (data may contain its own newlines)
                Write-Host -NoNewline $data
            }
            # Small delay to allow buffer to fill
            Start-Sleep -Milliseconds 10
        }
    }
    catch [System.TimeoutException] {
        # Timeout is normal when no data
    }
    catch {
        # Ignore read errors
    }
}

function Send-Command {
    param([string]$command)
    
    if (-not $connected -or -not $serialPort) {
        Write-Host "Not connected!" -ForegroundColor Red
        return
    }
    
    try {
        # Send command with newline
        $serialPort.WriteLine($command)
    }
    catch {
        Write-Host "Failed to send command:" -ForegroundColor Red
        Write-Host $_.Exception.Message -ForegroundColor Red
    }
}

function Show-Help {
    Write-Host ""
    Write-Host "Available Commands:" -ForegroundColor Cyan
    Write-Host "  help              - Show this help message" -ForegroundColor White
    Write-Host "  status            - Show connection status" -ForegroundColor White
    Write-Host "  ports             - List available COM ports" -ForegroundColor White
    Write-Host "  connect [port]    - Connect to a COM port" -ForegroundColor White
    Write-Host "  disconnect        - Disconnect from current port" -ForegroundColor White
    Write-Host "  exit, quit        - Exit the console" -ForegroundColor White
    Write-Host ""
    Write-Host "When connected, any text you type will be sent to the Raspberry Pi." -ForegroundColor Yellow
    Write-Host "Output from the Pi will be displayed in real-time." -ForegroundColor Yellow
    Write-Host ""
}

function Show-Status {
    Write-Host ""
    if ($connected -and $serialPort) {
        Write-Host "Connection Status: CONNECTED" -ForegroundColor Green
        Write-Host "Port: $($serialPort.PortName)" -ForegroundColor White
        Write-Host "Baud Rate: $($serialPort.BaudRate)" -ForegroundColor White
        Write-Host "Bytes Received: $($serialPort.BytesToRead)" -ForegroundColor White
    }
    else {
        Write-Host "Connection Status: DISCONNECTED" -ForegroundColor Red
    }
    Write-Host ""
}

function Connect-Command {
    param([string]$port = $null)
    
    if (-not $port) {
        $ports = Get-AvailablePorts
        if ($ports.Count -eq 0) {
            return
        }
        $port = Select-Port $ports
    }
    
    # Disconnect if already connected
    if ($connected) {
        Disconnect-Serial
    }
    
    Connect-Serial $port
}

# Main console loop
function Start-Console {
    Show-Banner
    
    # Auto-connect if port specified
    if ($PortName) {
        if (-not (Connect-Serial $PortName)) {
            Write-Host ""
            Write-Host "Would you like to select a different port? (Y/N)" -ForegroundColor Yellow
            $response = Read-Host
            if ($response -ne 'Y' -and $response -ne 'y') {
                return
            }
        }
    }
    
    # Main interactive loop
    while (-not $exitConsole) {
        try {
            # Read serial output in background
            if ($connected) {
                Read-SerialOutput
            }
            
            # Get user input
            if ($connected) {
                $input = Read-Host
            }
            else {
                Write-Host "Enter 'connect' to start, 'help' for commands, or 'exit' to quit: " -NoNewline
                $input = Read-Host
            }
            
            # Parse commands
            if ($input -match '^\s*exit\s*$' -or $input -match '^\s*quit\s*$') {
                $exitConsole = $true
            }
            elseif ($input -match '^\s*help\s*$') {
                Show-Help
            }
            elseif ($input -match '^\s*status\s*$') {
                Show-Status
            }
            elseif ($input -match '^\s*ports\s*$') {
                Get-AvailablePorts | Out-Null
            }
            elseif ($input -match '^\s*connect\s*(.*)') {
                Connect-Command -port $Matches[1]
            }
            elseif ($input -match '^\s*disconnect\s*$') {
                Disconnect-Serial
            }
            elseif ($connected -and $input) {
                # Send command to Pi
                Send-Command $input
            }
        }
        catch [System.OperationCanceledException] {
            # User pressed Ctrl+C
            break
        }
        catch {
            Write-Host "Error: $_" -ForegroundColor Red
        }
    }
    
    # Cleanup
    Disconnect-Serial
    Write-Host ""
    Write-Host "Goodbye!" -ForegroundColor Yellow
}

# Run the console
Start-Console

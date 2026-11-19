<#
.SYNOPSIS
    Enhanced interactive serial console for Raspberry Pi with background I/O.

.DESCRIPTION
    Advanced serial console that uses background threads for simultaneous
    input and output, providing a responsive terminal-like experience.

    Features:
    - Background thread for continuous serial output reading
    - Interactive command input without blocking output
    - Automatic COM port detection
    - Connection status monitoring
    - Command history
    - Proper serial settings (115200 baud, 8N1)

.PREREQUISITES
    - Windows PowerShell 5.1 or PowerShell 7+
    - USB-to-TTL serial adapter or Raspberry Pi Zero with USB gadget mode
    - Appropriate drivers installed

.EXAMPLE
    .\raspberry-pi-console-advanced.ps1
.EXAMPLE
    .\raspberry-pi-console-advanced.ps1 -PortName "COM4"
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
    [System.IO.Ports.StopBits]$StopBits = [System.IO.Ports.StopBits]::One
)

$ErrorActionPreference = "Continue"

# Global state
$script:serialPort = $null
$script:connected = $false
$script:exitRequested = $false
$script:readThread = $null
$script:commandHistory = New-Object System.Collections.ArrayList
$script:historyIndex = -1

function Show-Banner {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Raspberry Pi USB Serial Console" -ForegroundColor Cyan
    Write-Host "  (Advanced - Background I/O)" -ForegroundColor Cyan
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

# Background reader script block
$readerScript = {
    param($port)
    
    while (-not $script:exitRequested) {
        if ($port -and $port.IsOpen -and $port.BytesToRead -gt 0) {
            try {
                $data = $port.ReadExisting()
                if ($data) {
                    Write-Host -NoNewline $data
                }
            }
            catch {
                # Ignore read errors
            }
        }
        Start-Sleep -Milliseconds 50
    }
}

function Start-ReaderThread {
    if ($script:readThread) {
        Stop-ReaderThread
    }
    
    $script:exitRequested = $false
    $script:readThread = New-Object System.Threading.Thread -ArgumentList @(
        New-Object System.Threading.ParameterizedThreadStart(
            [System.Management.Automation.ScriptBlock]::Create($readerScript)
        )
    )
    $script:readThread.IsBackground = $true
    $script:readThread.Start($script:serialPort)
}

function Stop-ReaderThread {
    if ($script:readThread) {
        $script:exitRequested = $true
        try {
            $script:readThread.Join(1000)
        }
        catch {
            # Ignore thread join errors
        }
        $script:readThread = $null
    }
}

function Connect-Serial {
    param([string]$port)
    
    try {
        Write-Host "Connecting to $port..." -ForegroundColor Yellow
        Write-Host "Settings: $($BaudRate) baud, $DataBits data bits, $Parity parity, $StopBits stop bits" -ForegroundColor Gray
        
        $script:serialPort = New-Object System.IO.Ports.SerialPort
        $script:serialPort.PortName = $port
        $script:serialPort.BaudRate = $BaudRate
        $script:serialPort.Parity = $Parity
        $script:serialPort.DataBits = $DataBits
        $script:serialPort.StopBits = $StopBits
        $script:serialPort.ReadTimeout = 5000
        $script:serialPort.WriteTimeout = 5000
        $script:serialPort.Encoding = [System.Text.Encoding]::UTF8
        $script:serialPort.DtrEnable = $false
        $script:serialPort.RtsEnable = $false
        
        $script:serialPort.Open()
        $script:connected = $true
        
        # Start background reader
        Start-ReaderThread
        
        Write-Host "Connected to $port successfully!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Type 'help' for commands, 'exit' or 'quit' to disconnect." -ForegroundColor Yellow
        Write-Host "Use Up/Down arrows for command history." -ForegroundColor Yellow
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
    Stop-ReaderThread
    
    if ($script:serialPort -and $script:serialPort.IsOpen) {
        try {
            $script:serialPort.Close()
            $script:serialPort.Dispose()
        }
        catch {
            # Ignore cleanup errors
        }
        $script:serialPort = $null
    }
    $script:connected = $false
    Write-Host "Disconnected." -ForegroundColor Yellow
}

function Send-Command {
    param([string]$command)
    
    if (-not $script:connected -or -not $script:serialPort) {
        Write-Host "Not connected!" -ForegroundColor Red
        return
    }
    
    try {
        # Add to history
        if ($command -and $command.Trim()) {
            [void]$script:commandHistory.Add($command)
            $script:historyIndex = $script:commandHistory.Count
        }
        
        # Send command with newline
        $script:serialPort.WriteLine($command)
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
    Write-Host "Use Up/Down arrows to navigate command history." -ForegroundColor Yellow
    Write-Host ""
}

function Show-Status {
    Write-Host ""
    if ($script:connected -and $script:serialPort) {
        Write-Host "Connection Status: CONNECTED" -ForegroundColor Green
        Write-Host "Port: $($script:serialPort.PortName)" -ForegroundColor White
        Write-Host "Baud Rate: $($script:serialPort.BaudRate)" -ForegroundColor White
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
    
    if ($script:connected) {
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
    while ($true) {
        try {
            # Get user input
            if ($script:connected) {
                $input = Read-Host
            }
            else {
                Write-Host "Enter 'connect' to start, 'help' for commands, or 'exit' to quit: " -NoNewline
                $input = Read-Host
            }
            
            # Parse commands
            if ($input -match '^\s*exit\s*$' -or $input -match '^\s*quit\s*$') {
                break
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
            elseif ($script:connected -and $input) {
                Send-Command $input
            }
        }
        catch [System.OperationCanceledException] {
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

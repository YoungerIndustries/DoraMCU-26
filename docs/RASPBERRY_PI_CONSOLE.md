# Raspberry Pi USB Serial Console

PowerShell scripts for controlling Raspberry Pi via USB serial connection on Windows.

## Overview

This project provides two PowerShell scripts for interactive serial console access to Raspberry Pi:

- **`raspberry-pi-console.ps1`** - Basic console with sequential I/O
- **`raspberry-pi-console-advanced.ps1`** - Enhanced console with background thread for simultaneous input/output

## Features

- Automatic COM port detection
- Interactive command input
- Real-time output display
- Connection status monitoring
- Command history (advanced version)
- Proper serial settings (115200 baud, 8N1)
- DTR/RTS control to prevent Pi reset

## Prerequisites

### Hardware Options

#### Option 1: USB-to-TTL Serial Cable (Any Pi Model)

You need a USB-to-serial adapter cable:
- Adafruit USB-to-TTL
- Pimoroni USB-to-UART
- Any FTDI/CH340/CP2102 based adapter

**Wiring:**

| Cable Color | Pi GPIO Pin | Function |
|-------------|-------------|----------|
| Black       | Pin 6       | GND      |
| White       | Pin 8       | TXD      |
| Green       | Pin 10      | RXD      |

#### Option 2: USB Gadget Mode (Pi Zero/Zero 2 W Only)

Single USB cable connection - no extra hardware needed.

### Software

- Windows 10/11
- PowerShell 5.1 or PowerShell 7+
- USB serial driver (CH340, FTDI, CP2102, etc.)

## Raspberry Pi Setup

### For USB-to-TTL Serial

1. **Enable UART in config.txt:**
   Edit `/boot/firmware/config.txt` and add:
   ```
   enable_uart=1
   ```

2. **Configure cmdline.txt:**
   Ensure `/boot/firmware/cmdline.txt` contains:
   ```
   console=serial0,115200
   ```

3. **Reboot the Pi:**
   ```bash
   sudo reboot
   ```

### For USB Gadget Mode (Pi Zero)

1. **Enable gadget mode in cmdline.txt:**
   Edit `/boot/firmware/cmdline.txt` and add `modules-load=dwc2,g_serial` to the end of the single line:
   ```
   console=serial0,115200 root=/dev/mmcblk0p2 rootfstype=ext4 elevator=deadline fsck.repair=yes rootwait modules-load=dwc2,g_serial
   ```

2. **Enable the getty service:**
   ```bash
   sudo systemctl enable --now getty@ttyGS0
   ```

3. **Reboot the Pi:**
   ```bash
   sudo reboot
   ```

## Windows Setup

### Install Drivers

1. Connect your USB serial adapter or Pi Zero
2. Open Device Manager (Win+X → Device Manager)
3. Look for your device under "Ports (COM & LPT)"
4. If not found, install the appropriate driver:
   - **CH340**: [WCH Website](http://www.wch-ic.com/downloads/CH341SER_ZIP.html)
   - **FTDI**: [FTDI Website](https://ftdichip.com/drivers/vcp-drivers/)
   - **CP2102**: [Silicon Labs Website](https://www.silabs.com/developers/usb-to-uart-bridge-vcp-drivers)

### Find Your COM Port

1. Open Device Manager
2. Expand "Ports (COM & LPT)"
3. Note the COM port number (e.g., COM4)

Or use PowerShell:
```powershell
[System.IO.Ports.SerialPort]::GetPortNames()
```

## Usage

### Basic Console

```powershell
.\raspberry-pi-console.ps1
```

Auto-detects available COM ports and prompts for selection.

```powershell
.\raspberry-pi-console.ps1 -PortName "COM4"
```

Connect directly to a specific port.

```powershell
.\raspberry-pi-console.ps1 -PortName "COM4" -BaudRate 9600
```

Custom baud rate for older systems.

### Advanced Console

```powershell
.\raspberry-pi-console-advanced.ps1
```

Same options as basic console, with background I/O thread.

## Console Commands

| Command | Description |
|---------|-------------|
| `help` | Show available commands |
| `status` | Show connection status |
| `ports` | List available COM ports |
| `connect [port]` | Connect to a COM port |
| `disconnect` | Disconnect from current port |
| `exit` or `quit` | Exit the console |

When connected, any text you type is sent to the Raspberry Pi.

## Serial Settings

| Parameter | Default Value |
|-----------|---------------|
| Baud Rate | 115200 |
| Data Bits | 8 |
| Parity | None |
| Stop Bits | 1 |
| Flow Control | None |

These can be customized via command-line parameters.

## Troubleshooting

### No COM Ports Found

1. Check Device Manager for your USB serial adapter
2. Install appropriate drivers
3. Ensure the Raspberry Pi is connected and powered
4. Try a different USB cable or port

### Connection Fails

1. Verify the COM port number
2. Check baud rate matches Pi configuration
3. Ensure no other program is using the port
4. Try disconnecting and reconnecting

### No Output from Pi

1. Verify UART is enabled in config.txt
2. Check cmdline.txt configuration
3. Ensure proper wiring (TX/RX crossed)
4. Try toggling DTR/RTS settings

### Pi Resets on Connection

The scripts disable DTR/RTS by default to prevent this. If still occurring:
1. Add a 100nF capacitor between GND and GPIO0 (Pi Zero)
2. Use a level shifter if needed

## Alternative Tools

| Tool | Type | Notes |
|------|------|-------|
| **PuTTY** | GUI | Free, simple, supports copy/paste |
| **CoolTerm** | GUI | Feature-rich serial terminal |
| **Tera Term** | GUI | Advanced terminal emulator |
| **minicom** | CLI | Available via WSL or Git Bash |
| **screen** | CLI | `screen \\.\COM4 115200` |

## License

MIT License - Feel free to use and modify as needed.

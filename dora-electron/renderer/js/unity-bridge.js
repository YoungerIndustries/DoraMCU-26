/**
 * Electron-Unity Bridge — WebSocket-based communication between Electron client and Unity 3D client.
 * 
 * Enables seamless integration between the Electron UI and Unity 3D engine:
 * - Electron launches Unity client
 * - Unity sends telemetry back to Electron for HUD overlay
 * - Shared state management via WebSocket
 * - Smooth transitions between Electron 3D and Unity 3D views
 * 
 * Usage:
 *   const bridge = new UnityBridge();
 *   bridge.startServer();
 *   bridge.launchUnity();
 *   bridge.on('telemetry', (data) => { ... });
 *   bridge.sendCommand('navigate', { sector: [0, 0] });
 */

class UnityBridge {
    constructor() {
        this.wsServer = null;
        this.unityProcess = null;
        this.isConnected = false;
        this.client = null;
        this.telemetryCallbacks = [];
        this.eventCallbacks = new Map();
        this.unityPath = null;
        this.serverPort = 8765;
        this.isRunning = false;
    }

    /**
     * Start the WebSocket server for Unity communication.
     */
    startServer() {
        return new Promise((resolve, reject) => {
            // Use Node.js ws module
            const WebSocket = require('ws');
            
            this.wsServer = new WebSocket.Server({ port: this.serverPort }, () => {
                console.log(`[UnityBridge] WebSocket server started on port ${this.serverPort}`);
                this.isRunning = true;
                resolve();
            });

            this.wsServer.on('connection', (ws) => {
                console.log('[UnityBridge] Unity client connected');
                this.isConnected = true;
                this.client = ws;

                ws.on('message', (data) => {
                    try {
                        const message = JSON.parse(data.toString());
                        this._handleMessage(message);
                    } catch (e) {
                        console.error('[UnityBridge] Failed to parse message:', e);
                    }
                });

                ws.on('close', () => {
                    console.log('[UnityBridge] Unity client disconnected');
                    this.isConnected = false;
                    this.client = null;
                });

                ws.on('error', (error) => {
                    console.error('[UnityBridge] WebSocket error:', error);
                });

                // Send handshake
                this._send({
                    type: 'handshake',
                    payload: {
                        serverUrl: 'http://localhost:8080',
                        timestamp: Date.now()
                    }
                });
            });

            this.wsServer.on('error', (error) => {
                console.error('[UnityBridge] Server error:', error);
                reject(error);
            });
        });
    }

    /**
     * Stop the WebSocket server.
     */
    stopServer() {
        if (this.wsServer) {
            this.wsServer.close();
            this.wsServer = null;
            this.isRunning = false;
            console.log('[UnityBridge] WebSocket server stopped');
        }
    }

    /**
     * Launch the Unity client application.
     */
    async launchUnity() {
        const { execFile } = require('child_process');
        const path = require('path');
        
        // Find Unity executable
        if (!this.unityPath) {
            this.unityPath = this._findUnityExecutable();
        }

        if (!this.unityPath) {
            console.error('[UnityBridge] Unity executable not found. Place DoraMCU.exe in project root.');
            return false;
        }

        return new Promise((resolve, reject) => {
            const args = [
                `-IPAddress 127.0.0.1`,
                `-Port ${this.serverPort}`
            ];

            this.unityProcess = execFile(this.unityPath, args, { timeout: 10000 }, (error, stdout, stderr) => {
                if (error) {
                    console.error('[UnityBridge] Failed to launch Unity:', error);
                    reject(error);
                    return;
                }
                console.log('[UnityBridge] Unity client launched');
                resolve(true);
            });

            this.unityProcess.on('exit', (code) => {
                console.log(`[UnityBridge] Unity process exited with code ${code}`);
                this.isConnected = false;
            });
        });
    }

    /**
     * Find the Unity executable.
     */
    _findUnityExecutable() {
        const path = require('path');
        const fs = require('fs');
        
        const possiblePaths = [
            path.join(__dirname, '..', '..', 'DoraMCU.exe'),
            path.join(__dirname, '..', '..', 'unity-client', 'Build', 'DoraMCU.exe'),
            path.join(process.cwd(), 'DoraMCU.exe'),
            path.join(process.cwd(), 'unity-client', 'Build', 'DoraMCU.exe')
        ];

        for (const p of possiblePaths) {
            if (fs.existsSync(p)) {
                return p;
            }
        }
        return null;
    }

    /**
     * Kill the Unity process.
     */
    killUnity() {
        if (this.unityProcess) {
            this.unityProcess.kill();
            this.unityProcess = null;
            console.log('[UnityBridge] Unity process killed');
        }
    }

    /**
     * Send a command to the Unity client.
     */
    sendCommand(command, payload) {
        if (!this.isConnected || !this.client) {
            console.warn('[UnityBridge] Not connected to Unity client');
            return false;
        }

        const message = {
            type: 'command',
            command,
            payload,
            timestamp: Date.now()
        };

        this._send(message);
        return true;
    }

    /**
     * Send telemetry data to Unity.
     */
    sendTelemetry(data) {
        return this.sendCommand('telemetry', data);
    }

    /**
     * Send game state to Unity.
     */
    sendGameState(state) {
        return this.sendCommand('gameState', state);
    }

    /**
     * Send navigation command to Unity.
     */
    navigateTo(sectorRow, sectorCol, subSectorRow, subSectorCol) {
        return this.sendCommand('navigate', {
            sectorRow,
            sectorCol,
            subSectorRow,
            subSectorCol
        });
    }

    /**
     * Send character data to Unity.
     */
    sendCharacterData(character) {
        return this.sendCommand('character', character);
    }

    /**
     * Request Unity to enter battlefield mode.
     */
    enterBattlefield(locationId, timePeriod, terrain) {
        return this.sendCommand('battlefield', {
            locationId,
            timePeriod,
            terrain
        });
    }

    /**
     * Request Unity to exit battlefield mode.
     */
    exitBattlefield() {
        return this.sendCommand('exitBattlefield', {});
    }

    /**
     * Register a callback for telemetry data from Unity.
     */
    onTelemetry(callback) {
        this.telemetryCallbacks.push(callback);
    }

    /**
     * Register a callback for a specific event type.
     */
    on(eventType, callback) {
        if (!this.eventCallbacks.has(eventType)) {
            this.eventCallbacks.set(eventType, []);
        }
        this.eventCallbacks.get(eventType).push(callback);
    }

    /**
     * Remove a callback.
     */
    off(eventType, callback) {
        if (this.eventCallbacks.has(eventType)) {
            const callbacks = this.eventCallbacks.get(eventType);
            const index = callbacks.indexOf(callback);
            if (index !== -1) {
                callbacks.splice(index, 1);
            }
        }
    }

    /**
     * Handle incoming messages from Unity.
     */
    _handleMessage(message) {
        switch (message.type) {
            case 'telemetry':
                this.telemetryCallbacks.forEach(cb => cb(message.payload));
                break;

            case 'event':
                if (this.eventCallbacks.has(message.event)) {
                    this.eventCallbacks.get(message.event).forEach(cb => cb(message.payload));
                }
                break;

            case 'ack':
                // Acknowledgment received
                break;

            default:
                console.log('[UnityBridge] Unknown message type:', message.type);
        }
    }

    /**
     * Send a message to the connected Unity client.
     */
    _send(message) {
        if (this.client && this.client.readyState === 1) {
            this.client.send(JSON.stringify(message));
        }
    }

    /**
     * Get connection status.
     */
    getStatus() {
        return {
            isRunning: this.isRunning,
            isConnected: this.isConnected,
            unityPath: this.unityPath,
            serverPort: this.serverPort
        };
    }

    /**
     * Clean up resources.
     */
    destroy() {
        this.killUnity();
        this.stopServer();
        this.telemetryCallbacks = [];
        this.eventCallbacks.clear();
    }
}

// Expose globally
window.UnityBridge = UnityBridge;

// Export for Node.js
if (typeof module !== 'undefined' && module.exports) {
    module.exports = UnityBridge;
}

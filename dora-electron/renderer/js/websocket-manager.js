/**
 * WebSocketConnectionManager — Centralized WebSocket connection management.
 *
 * Provides:
 * - Unified WebSocket connection management for all 3D visualization modules
 * - Automatic reconnection with exponential backoff
 * - Connection health monitoring with ping/pong
 * - Message routing to registered handlers
 * - Graceful degradation when server is unavailable
 *
 * Usage:
 *   const manager = new WebSocketConnectionManager();
 *   manager.connect('fleet-tracker', (message) => { /* handle */ });
 *   manager.connect('presence', (message) => { /* handle */ });
 *   manager.connect('narrative', (message) => { /* handle */ });
 *   manager.disconnect('fleet-tracker');
 *   manager.destroy();
 */

class WebSocketConnectionManager {
    /**
     * Create a new WebSocket connection manager.
     * @param {Object} options - Configuration options
     * @param {string} [options.serverUrl] - Override server URL (defaults to authService)
     * @param {number} [options.maxReconnectAttempts=10] - Maximum reconnection attempts
     * @param {number} [options.reconnectBaseDelay=1000] - Base delay for reconnection (ms)
     * @param {number} [options.pingInterval=30000] - Ping interval for health check (ms)
     * @param {number} [options.pingTimeout=5000] - Ping response timeout (ms)
     */
    constructor(options = {}) {
        this.serverUrl = options.serverUrl || null;
        this.maxReconnectAttempts = options.maxReconnectAttempts || 10;
        this.reconnectBaseDelay = options.reconnectBaseDelay || 1000;
        this.pingInterval = options.pingInterval || 30000;
        this.pingTimeout = options.pingTimeout || 5000;

        // WebSocket connections by endpoint name
        this.connections = new Map(); // endpointName -> connection state
        this.messageHandlers = new Map(); // endpointName -> Set of handlers
        this.reconnectTimers = new Map(); // endpointName -> timer ID

        // Global ping timer
        this.pingTimer = null;
        this.isDestroyed = false;

        // Connection status
        this.status = 'disconnected'; // disconnected, connecting, connected, error

        console.log('[WSManager] WebSocketConnectionManager created');
    }

    /**
     * Get the server URL from auth service or fallback.
     * @returns {string} Server URL without protocol
     */
    _getServerUrl() {
        if (this.serverUrl) return this.serverUrl;
        if (window.authService) return window.authService.getServerUrl();
        return 'http://localhost:8080';
    }

    /**
     * Build WebSocket URL for an endpoint.
     * @param {string} endpoint - Endpoint name (e.g., 'fleet-tracker', 'presence', 'narrative')
     * @param {Object} [params] - Query parameters
     * @returns {string} Full WebSocket URL
     */
    _buildWebSocketUrl(endpoint, params = {}) {
        const serverUrl = this._getServerUrl();
        const wsProtocol = serverUrl.includes('https') ? 'wss' : 'ws';
        const wsHost = serverUrl.replace(/https?:\/\//, '');

        // Get session info from localStorage
        const sessionId = params.sessionId || 
            localStorage.getItem('fps-match-id') || 
            localStorage.getItem('game-session-id') || 
            'default';
        const playerId = params.playerId || 
            localStorage.getItem('fps-player-id') || 
            localStorage.getItem('player-id') || 
            'player_' + Date.now();
        const playerName = params.playerName || 
            localStorage.getItem('fps-player-name') || 
            'Player';

        let url = `${wsProtocol}://${wsHost}/ws/${endpoint}`;
        const queryParams = [`sessionId=${encodeURIComponent(sessionId)}`, `playerId=${encodeURIComponent(playerId)}`];

        // Add playerName for presence endpoint
        if (endpoint === 'presence') {
            queryParams.push(`playerName=${encodeURIComponent(playerName)}`);
        }

        url += '?' + queryParams.join('&');
        return url;
    }

    /**
     * Connect to a WebSocket endpoint.
     * @param {string} endpoint - Endpoint name (e.g., 'fleet-tracker', 'presence', 'narrative')
     * @param {Function} handler - Message handler function
     * @param {Object} [params] - Additional query parameters
     * @returns {Promise<boolean>} Whether connection was established
     */
    async connect(endpoint, handler, params = {}) {
        if (this.isDestroyed) {
            console.warn('[WSManager] Manager destroyed, cannot connect');
            return false;
        }

        // Register message handler
        if (!this.messageHandlers.has(endpoint)) {
            this.messageHandlers.set(endpoint, new Set());
        }
        this.messageHandlers.get(endpoint).add(handler);

        // If already connected, just return success
        const existing = this.connections.get(endpoint);
        if (existing && existing.ws && existing.ws.readyState === WebSocket.OPEN) {
            console.log(`[WSManager] Already connected to ${endpoint}`);
            return true;
        }

        // Clear any existing reconnection timer
        this._clearReconnectTimer(endpoint);

        // Close existing connection if any
        if (existing && existing.ws) {
            try {
                existing.ws.close();
            } catch (e) {
                // Ignore close errors
            }
        }

        // Create new connection
        return this._createConnection(endpoint, params);
    }

    /**
     * Create a new WebSocket connection.
     * @param {string} endpoint - Endpoint name
     * @param {Object} [params] - Query parameters
     * @returns {Promise<boolean>} Whether connection was successful
     */
    _createConnection(endpoint, params = {}) {
        return new Promise((resolve) => {
            const wsUrl = this._buildWebSocketUrl(endpoint, params);
            console.log(`[WSManager] Connecting to ${endpoint}: ${wsUrl}`);

            this.status = 'connecting';
            this.connections.set(endpoint, {
                ws: null,
                connected: false,
                reconnectAttempts: 0,
                lastMessage: 0
            });

            try {
                const ws = new WebSocket(wsUrl);
                const connection = this.connections.get(endpoint);
                connection.ws = ws;

                ws.onopen = () => {
                    console.log(`[WSManager] Connected to ${endpoint}`);
                    connection.connected = true;
                    connection.reconnectAttempts = 0;
                    connection.lastMessage = Date.now();
                    this.status = 'connected';
                    resolve(true);
                };

                ws.onmessage = (event) => {
                    connection.lastMessage = Date.now();
                    this._dispatchMessage(endpoint, event.data);
                };

                ws.onclose = (event) => {
                    console.log(`[WSManager] Disconnected from ${endpoint}: ${event.code} ${event.reason}`);
                    connection.connected = false;
                    connection.ws = null;
                    this._updateStatus();

                    // Attempt reconnection
                    if (!this.isDestroyed) {
                        this._attemptReconnect(endpoint, params);
                    }
                    // Resolve false only on first connection attempt
                    if (connection.reconnectAttempts === 0) {
                        resolve(false);
                    }
                };

                ws.onerror = (error) => {
                    console.error(`[WSManager] WebSocket error for ${endpoint}:`, error);
                    this.status = 'error';
                };
            } catch (error) {
                console.error(`[WSManager] Failed to create WebSocket for ${endpoint}:`, error);
                this.status = 'error';
                resolve(false);
            }

            // Timeout for connection
            setTimeout(() => {
                const connection = this.connections.get(endpoint);
                if (connection && !connection.connected) {
                    console.warn(`[WSManager] Connection timeout for ${endpoint}`);
                    if (ws && ws.readyState === WebSocket.CONNECTING) {
                        ws.close();
                    }
                    resolve(false);
                }
            }, 5000);
        });
    }

    /**
     * Dispatch a message to all registered handlers for an endpoint.
     * @param {string} endpoint - Endpoint name
     * @param {string} data - Raw message data
     */
    _dispatchMessage(endpoint, data) {
        const handlers = this.messageHandlers.get(endpoint);
        if (!handlers) return;

        try {
            const message = JSON.parse(data);
            handlers.forEach(handler => {
                try {
                    handler(message);
                } catch (error) {
                    console.error(`[WSManager] Error in message handler for ${endpoint}:`, error);
                }
            });
        } catch (error) {
            console.error(`[WSManager] Failed to parse message for ${endpoint}:`, error);
        }
    }

    /**
     * Attempt to reconnect to an endpoint.
     * @param {string} endpoint - Endpoint name
     * @param {Object} [params] - Query parameters
     */
    _attemptReconnect(endpoint, params = {}) {
        const connection = this.connections.get(endpoint);
        if (!connection || this.isDestroyed) return;

        if (connection.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn(`[WSManager] Max reconnection attempts reached for ${endpoint}`);
            this.status = 'error';
            return;
        }

        connection.reconnectAttempts++;
        const delay = this.reconnectBaseDelay * Math.pow(2, connection.reconnectAttempts - 1);
        console.log(`[WSManager] Reconnecting to ${endpoint} in ${delay}ms (attempt ${connection.reconnectAttempts})`);

        const timer = setTimeout(() => {
            if (!this.isDestroyed) {
                this._createConnection(endpoint, params);
            }
        }, delay);

        this.reconnectTimers.set(endpoint, timer);
    }

    /**
     * Clear reconnection timer for an endpoint.
     * @param {string} endpoint - Endpoint name
     */
    _clearReconnectTimer(endpoint) {
        const timer = this.reconnectTimers.get(endpoint);
        if (timer) {
            clearTimeout(timer);
            this.reconnectTimers.delete(endpoint);
        }
    }

    /**
     * Disconnect from a specific endpoint.
     * @param {string} endpoint - Endpoint name
     */
    disconnect(endpoint) {
        this._clearReconnectTimer(endpoint);

        const connection = this.connections.get(endpoint);
        if (connection && connection.ws) {
            try {
                connection.ws.close();
            } catch (e) {
                // Ignore close errors
            }
            connection.ws = null;
            connection.connected = false;
        }

        this.connections.delete(endpoint);
        this.messageHandlers.delete(endpoint);
        this._updateStatus();
    }

    /**
     * Disconnect from all endpoints.
     */
    disconnectAll() {
        const endpoints = Array.from(this.connections.keys());
        endpoints.forEach(endpoint => this.disconnect(endpoint));
        this._stopPingTimer();
    }

    /**
     * Send a message to a specific endpoint.
     * @param {string} endpoint - Endpoint name
     * @param {Object} message - Message object
     * @returns {boolean} Whether message was sent
     */
    send(endpoint, message) {
        const connection = this.connections.get(endpoint);
        if (!connection || !connection.ws || connection.ws.readyState !== WebSocket.OPEN) {
            console.warn(`[WSManager] Cannot send to ${endpoint}: not connected`);
            return false;
        }

        try {
            connection.ws.send(JSON.stringify(message));
            return true;
        } catch (error) {
            console.error(`[WSManager] Failed to send to ${endpoint}:`, error);
            return false;
        }
    }

    /**
     * Send a ping to all connected endpoints.
     */
    _sendPings() {
        this.connections.forEach((connection, endpoint) => {
            if (connection.ws && connection.ws.readyState === WebSocket.OPEN) {
                try {
                    connection.ws.send(JSON.stringify({ type: 'ping' }));
                } catch (error) {
                    console.error(`[WSManager] Failed to ping ${endpoint}:`, error);
                }
            }
        });
    }

    /**
     * Start the ping timer for connection health monitoring.
     */
    _startPingTimer() {
        this._stopPingTimer();
        this.pingTimer = setInterval(() => {
            if (!this.isDestroyed) {
                this._sendPings();
            }
        }, this.pingInterval);
    }

    /**
     * Stop the ping timer.
     */
    _stopPingTimer() {
        if (this.pingTimer) {
            clearInterval(this.pingTimer);
            this.pingTimer = null;
        }
    }

    /**
     * Update overall connection status.
     */
    _updateStatus() {
        let hasConnected = false;
        let hasConnecting = false;

        this.connections.forEach((connection) => {
            if (connection.connected) hasConnected = true;
            if (connection.ws && connection.ws.readyState === WebSocket.CONNECTING) hasConnecting = true;
        });

        if (hasConnected) {
            this.status = 'connected';
            if (!this.pingTimer) {
                this._startPingTimer();
            }
        } else if (hasConnecting) {
            this.status = 'connecting';
        } else {
            this.status = 'disconnected';
            this._stopPingTimer();
        }
    }

    /**
     * Check if an endpoint is connected.
     * @param {string} endpoint - Endpoint name
     * @returns {boolean} Whether the endpoint is connected
     */
    isConnected(endpoint) {
        const connection = this.connections.get(endpoint);
        return connection && connection.connected;
    }

    /**
     * Get connection status for an endpoint.
     * @param {string} endpoint - Endpoint name
     * @returns {Object} Connection status object
     */
    getStatus(endpoint) {
        if (endpoint) {
            const connection = this.connections.get(endpoint);
            return {
                endpoint,
                connected: connection ? connection.connected : false,
                reconnectAttempts: connection ? connection.reconnectAttempts : 0,
                readyState: connection && connection.ws ? connection.ws.readyState : -1
            };
        }

        // Return status for all endpoints
        const statuses = {};
        this.connections.forEach((connection, name) => {
            statuses[name] = {
                connected: connection.connected,
                reconnectAttempts: connection.reconnectAttempts,
                readyState: connection.ws ? connection.ws.readyState : -1
            };
        });
        return {
            overall: this.status,
            endpoints: statuses
        };
    }

    /**
     * Get all connected endpoint names.
     * @returns {string[]} Array of connected endpoint names
     */
    getConnectedEndpoints() {
        const result = [];
        this.connections.forEach((connection, name) => {
            if (connection.connected) result.push(name);
        });
        return result;
    }

    /**
     * Destroy the manager and clean up all resources.
     */
    destroy() {
        this.isDestroyed = true;
        this.disconnectAll();
        this._stopPingTimer();
        this.connections.clear();
        this.messageHandlers.clear();
        this.reconnectTimers.clear();
        this.status = 'disconnected';
        console.log('[WSManager] Destroyed');
    }
}

// Expose globally
window.WebSocketConnectionManager = WebSocketConnectionManager;

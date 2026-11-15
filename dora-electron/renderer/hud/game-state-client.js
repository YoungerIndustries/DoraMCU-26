/**
 * GameStateClient — WebSocket client for real-time game state synchronization.
 *
 * Connects to /ws/game-state to provide bidirectional communication between
 * the unified game shell and the backend GameStateWebSocketHandler.
 *
 * Features:
 * - Session-based routing: join a game session, receive state updates
 * - Action dispatch: send player actions and receive results
 * - Real-time sync: all players see synchronized game state
 * - Automatic reconnection with exponential backoff
 * - Ping/pong health monitoring
 *
 * Usage:
 *   const client = new GameStateClient();
 *   client.setCallbacks({ onGameState: ..., onActionResult: ... });
 *   client.connect();
 *   client.joinSession(sessionId, playerId);
 *   client.sendAction('END_TURN');
 *   client.destroy();
 */

class GameStateClient {
    constructor() {
        this.isConnected = false;
        this.sessionId = null;
        this.playerId = null;
        this.gameType = null;
        this.ws = null;

        // Reconnection
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectDelay = 1000;
        this.reconnectTimer = null;

        // Ping/pong
        this.pingInterval = null;
        this.pingIntervalMs = 30000;

        // Callbacks
        this.callbacks = {
            onConnect: null,
            onDisconnect: null,
            onGameState: null,
            onActionResult: null,
            onEventNotification: null,
            onPlayerUpdate: null,
            onTurnUpdate: null,
            onError: null
        };
    }

    /**
     * Set callbacks for game state events.
     * @param {object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        this.callbacks = { ...this.callbacks, ...callbacks };
    }

    /**
     * Connect to the game state WebSocket endpoint.
     */
    connect() {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            console.warn('[GameStateClient] Already connected');
            return;
        }

        const serverUrl = (typeof window !== 'undefined' && window.authService)
            ? window.authService.getServerUrl()
            : 'http://localhost:8080';

        const wsUrl = `${serverUrl.replace('http://', 'ws://').replace('https://', 'wss://')}/ws/game-state`;

        console.log('[GameStateClient] Connecting to', wsUrl);
        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
            console.log('[GameStateClient] Connected');
            this.isConnected = true;
            this.reconnectAttempts = 0;
            this._startPing();
            if (this.callbacks.onConnect) {
                this.callbacks.onConnect();
            }
            // If we had a session, rejoin it
            if (this.sessionId && this.playerId) {
                this._send({ type: 'join', sessionId: this.sessionId, playerId: String(this.playerId) });
            }
        };

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                this._handleMessage(data);
            } catch (e) {
                console.error('[GameStateClient] Error parsing message:', e);
            }
        };

        this.ws.onclose = (event) => {
            console.log('[GameStateClient] Disconnected:', event.code, event.reason);
            this.isConnected = false;
            this._stopPing();
            if (this.callbacks.onDisconnect) {
                this.callbacks.onDisconnect(event.code, event.reason);
            }
            this._scheduleReconnect();
        };

        this.ws.onerror = (error) => {
            console.error('[GameStateClient] WebSocket error');
            if (this.callbacks.onError) {
                this.callbacks.onError('WebSocket error');
            }
        };
    }

    /**
     * Handle incoming WebSocket messages.
     * @param {object} data - Parsed JSON message
     */
    _handleMessage(data) {
        switch (data.type) {
            case 'game_state':
                this.gameType = data.gameType || this.gameType;
                if (this.callbacks.onGameState) {
                    this.callbacks.onGameState(data);
                }
                break;
            case 'action_result':
                if (this.callbacks.onActionResult) {
                    this.callbacks.onActionResult(data);
                }
                break;
            case 'event_notification':
                if (this.callbacks.onEventNotification) {
                    this.callbacks.onEventNotification(data);
                }
                break;
            case 'player_update':
                if (this.callbacks.onPlayerUpdate) {
                    this.callbacks.onPlayerUpdate(data);
                }
                break;
            case 'turn_update':
                if (this.callbacks.onTurnUpdate) {
                    this.callbacks.onTurnUpdate(data);
                }
                break;
            case 'pong':
                // Health check response
                break;
            case 'error':
                console.error('[GameStateClient] Server error:', data.message);
                if (this.callbacks.onError) {
                    this.callbacks.onError(data.message);
                }
                break;
            default:
                console.warn('[GameStateClient] Unknown message type:', data.type);
        }
    }

    /**
     * Join a game session.
     * @param {string} sessionId - Game session ID
     * @param {string|number} playerId - Player ID
     */
    joinSession(sessionId, playerId) {
        this.sessionId = sessionId;
        this.playerId = playerId;
        if (this.isConnected) {
            this._send({ type: 'join', sessionId, playerId: String(playerId) });
        }
    }

    /**
     * Leave the current game session.
     */
    leaveSession() {
        if (this.isConnected) {
            this._send({ type: 'leave' });
        }
        this.sessionId = null;
        this.playerId = null;
        this.gameType = null;
    }

    /**
     * Send a player action.
     * @param {string} action - Action name (e.g., 'END_TURN', 'EXPLORE')
     * @param {object} [params] - Optional action parameters
     */
    sendAction(action, params) {
        if (!this.isConnected) {
            console.warn('[GameStateClient] Not connected, cannot send action');
            return;
        }
        if (!this.sessionId) {
            console.warn('[GameStateClient] Not in a session, cannot send action');
            return;
        }
        this._send({ type: 'action', action, params: params || {} });
    }

    /**
     * Request the current game state.
     */
    requestState() {
        if (this.isConnected) {
            this._send({ type: 'state_request' });
        }
    }

    /**
     * Send a raw message.
     * @param {object} message - Message object
     */
    _send(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        }
    }

    /**
     * Start ping keepalive.
     */
    _startPing() {
        this._stopPing();
        this.pingInterval = setInterval(() => {
            this._send({ type: 'ping' });
        }, this.pingIntervalMs);
    }

    /**
     * Stop ping keepalive.
     */
    _stopPing() {
        if (this.pingInterval) {
            clearInterval(this.pingInterval);
            this.pingInterval = null;
        }
    }

    /**
     * Schedule reconnection with exponential backoff.
     */
    _scheduleReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('[GameStateClient] Max reconnection attempts reached');
            return;
        }
        this.reconnectAttempts++;
        const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
        console.log(`[GameStateClient] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
        this.reconnectTimer = setTimeout(() => {
            this.connect();
        }, delay);
    }

    /**
     * Get connection status.
     * @returns {object} Connection status info
     */
    getStatus() {
        return {
            isConnected: this.isConnected,
            sessionId: this.sessionId,
            playerId: this.playerId,
            gameType: this.gameType,
            reconnectAttempts: this.reconnectAttempts
        };
    }

    /**
     * Destroy the client and clean up resources.
     */
    destroy() {
        this._stopPing();
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }
        if (this.ws) {
            this.ws.onclose = null;
            this.ws.close();
            this.ws = null;
        }
        this.isConnected = false;
        this.sessionId = null;
        this.playerId = null;
        this.gameType = null;
        console.log('[GameStateClient] Destroyed');
    }
}

// Expose globally
window.GameStateClient = GameStateClient;

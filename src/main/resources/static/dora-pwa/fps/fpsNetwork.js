/**
 * WebSocket client for FPS game server communication.
 * Handles connection, input sending, and state receiving.
 */
class FpsNetwork {
    constructor(serverHost = 'localhost', serverPort = 8080) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.ws = null;
        this.sessionId = null;
        this.playerId = null;
        this.isConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectDelay = 3000;
        
        // Callbacks
        this.onConnected = null;
        this.onDisconnected = null;
        this.onGameState = null;
        this.onGameEvent = null;
        this.onError = null;
        this.onTransitionAck = null;
        
        // Input loop
        this.inputInterval = null;
    }
    
    connect(authToken = null) {
        if (this.isConnected) return;
        
        let url = `ws://${this.serverHost}:${this.serverPort}/ws/fps`;
        if (authToken) {
            url += `?token=${encodeURIComponent(authToken)}`;
        }
        
        try {
            this.ws = new WebSocket(url);
            
            this.ws.onopen = () => {
                console.log('[FpsNetwork] Connected to server');
            };
            
            this.ws.onmessage = (event) => {
                this.handleMessage(JSON.parse(event.data));
            };
            
            this.ws.onclose = (event) => {
                console.log(`[FpsNetwork] Disconnected: ${event.reason}`);
                this.isConnected = false;
                this.stopInputLoop();
                if (this.onDisconnected) this.onDisconnected();
                
                // Attempt reconnect
                if (this.reconnectAttempts < this.maxReconnectAttempts) {
                    setTimeout(() => {
                        this.reconnectAttempts++;
                        this.connect(authToken);
                    }, this.reconnectDelay);
                }
            };
            
            this.ws.onerror = (error) => {
                console.error('[FpsNetwork] WebSocket error');
                if (this.onError) this.onError('WebSocket error');
            };
        } catch (e) {
            console.error('[FpsNetwork] Connection failed:', e);
            if (this.onError) this.onError(e.message);
        }
    }
    
    disconnect() {
        this.isConnected = false;
        this.stopInputLoop();
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }
    
    sendMessage(obj) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
        this.ws.send(JSON.stringify(obj));
    }
    
    sendInput(move, aim, actions) {
        if (!this.isConnected || !this.sessionId) return;
        
        this.sendMessage({
            type: 'INPUT',
            sessionId: this.sessionId,
            playerId: this.playerId,
            timestamp: Date.now(),
            move: move,
            aim: aim,
            actions: actions
        });
    }
    
    sendHit(hitPoint, targetPlayerId) {
        if (!this.isConnected) return;
        this.sendMessage({
            type: 'HIT',
            sessionId: this.sessionId,
            playerId: this.playerId,
            hitPoint: hitPoint,
            targetId: targetPlayerId
        });
    }
    
    sendProjectile(origin, velocity) {
        if (!this.isConnected) return;
        this.sendMessage({
            type: 'PROJECTILE',
            sessionId: this.sessionId,
            playerId: this.playerId,
            origin: origin,
            velocity: velocity
        });
    }
    
    sendReady() {
        if (!this.isConnected) return;
        this.sendMessage({
            type: 'READY',
            sessionId: this.sessionId,
            playerId: this.playerId
        });
    }
    
    sendStartMatch() {
        if (!this.isConnected) return;
        this.sendMessage({
            type: 'START_MATCH',
            sessionId: this.sessionId,
            playerId: this.playerId
        });
    }
    
    sendChat(message) {
        if (!this.isConnected) return;
        this.sendMessage({
            type: 'CHAT',
            sessionId: this.sessionId,
            playerId: this.playerId,
            message: message
        });
    }
    
    handleMessage(data) {
        switch (data.type) {
            case 'CONNECT':
                this.sessionId = data.sessionId;
                this.playerId = data.playerId;
                this.isConnected = true;
                this.reconnectAttempts = 0;
                console.log(`[FpsNetwork] Session: ${this.sessionId}, Player: ${this.playerId}`);
                if (data.map) {
                    console.log(`[FpsNetwork] Server map: ${data.map.name} (theme: ${data.map.theme})`);
                }
                this.startInputLoop();
                if (this.onConnected) this.onConnected(this.sessionId, data);
                break;
                
            case 'STATE':
                if (this.onGameState) this.onGameState(data);
                break;
                
            case 'EVENT':
                if (this.onGameEvent) this.onGameEvent(data);
                break;
                
            case 'TRANSITION_ACK':
                if (this.onTransitionAck) this.onTransitionAck(data);
                break;
                
            case 'CHAT':
                if (this.onGameEvent) this.onGameEvent(data);
                break;
                
            case 'ERROR':
                console.error(`[FpsNetwork] Server error: ${data.message}`);
                if (this.onError) this.onError(data.message);
                break;
        }
    }
    
    startInputLoop() {
        // Send input at 20Hz
        this.inputInterval = setInterval(() => {
            if (!this.isConnected) return;
            
            const move = { x: 0, y: 0, z: 0 };
            const aim = { pitch: 0, yaw: 0 };
            const actions = [];
            
            // Get input from player controller
            if (window.fpsPlayer) {
                const input = window.fpsPlayer.getInput();
                if (input) {
                    this.sendInput(input.move, input.aim, input.actions);
                }
            }
        }, 50);
    }
    
    stopInputLoop() {
        if (this.inputInterval) {
            clearInterval(this.inputInterval);
            this.inputInterval = null;
        }
    }
    
    // REST API calls
    async fetchWeapons() {
        try {
            const response = await fetch(`http://${this.serverHost}:${this.serverPort}/api/v1/fps/weapons`);
            return await response.json();
        } catch (e) {
            console.error('[FpsNetwork] Failed to fetch weapons:', e);
            return [];
        }
    }
    
    async fetchMaps() {
        try {
            const response = await fetch(`http://${this.serverHost}:${this.serverPort}/api/v1/fps/maps`);
            return await response.json();
        } catch (e) {
            console.error('[FpsNetwork] Failed to fetch maps:', e);
            return [];
        }
    }
    
    async createMatch(mode, mapId) {
        try {
            const response = await fetch(`http://${this.serverHost}:${this.serverPort}/api/v1/fps/matches`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ mode, mapId })
            });
            const data = await response.json();
            // Parse serverUrl for dedicated server support
            if (data && data.serverUrl) {
                console.log(`[FpsNetwork] Using dedicated server: ${data.serverUrl}`);
                const uri = new URL(data.serverUrl);
                this.serverHost = uri.hostname;
                this.serverPort = uri.port;
            }
            return data;
        } catch (e) {
            console.error('[FpsNetwork] Failed to create match:', e);
            return null;
        }
    }
    
    async joinMatch(matchId) {
        try {
            const response = await fetch(`http://${this.serverHost}:${this.serverPort}/api/v1/fps/matches/${matchId}/join`, {
                method: 'POST'
            });
            return response.ok;
        } catch (e) {
            console.error('[FpsNetwork] Failed to join match:', e);
            return false;
        }
    }
    
    async listMatches() {
        try {
            const response = await fetch(`http://${this.serverHost}:${this.serverPort}/api/v1/fps/matches`);
            return await response.json();
        } catch (e) {
            console.error('[FpsNetwork] Failed to list matches:', e);
            return [];
        }
    }
}

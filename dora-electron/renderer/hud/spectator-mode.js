/**
 * SpectatorMode — 3D Spectator Mode for Game Observation
 *
 * Provides read-only observation of game sessions in 3D space.
 * Connects to /ws/spectator WebSocket endpoint for real-time redacted game state.
 *
 * Features:
 * - Session selection and join/leave
 * - Camera modes: overview, follow_fleet, follow_battle, free_roam
 * - Redacted game state display (no sensitive player data)
 * - Event history catch-up for late joiners
 * - Spectator list overlay
 *
 * Usage:
 *   const spectator = new SpectatorMode(containerElement, scene, camera);
 *   spectator.connect(serverUrl);
 *   spectator.joinSession(sessionId);
 *   spectator.setCameraMode('overview');
 *   spectator.destroy();
 */

class SpectatorMode {
    constructor(container, scene, camera) {
        this.container = container;
        this.scene = scene;
        this.camera = camera;
        this.ws = null;
        this.isConnected = false;
        this.serverUrl = null;

        // Spectator state
        this.currentSession = null;
        this.spectatorId = null;
        this.cameraMode = 'overview';
        this.followFleetId = null;
        this.gameState = null;
        this.eventHistory = [];
        this.spectators = [];

        // Three.js objects
        this.spectatorGroup = null;
        this.fleetMarkers = new Map();
        this.battleMarkers = [];
        this.playerMarkers = new Map();

        // UI elements
        this.uiContainer = null;
        this.sessionSelect = null;
        this.cameraModeSelect = null;
        this.spectatorList = null;
        this.statusDisplay = null;
        this.eventLog = null;

        // Reconnection
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectTimer = null;

        // Callbacks
        this.onStatusChange = null;
        this.onGameStateUpdate = null;
        this.onEventHistory = null;

        // Animation
        this.animationId = null;
    }

    /**
     * Connect to the spectator WebSocket endpoint.
     */
    connect(serverUrl) {
        this.serverUrl = serverUrl || (window.authService && window.authService.getServerUrl ? window.authService.getServerUrl() : 'http://localhost:8080');

        const wsUrl = this.serverUrl.replace('http://', 'ws://').replace('https://', 'wss://') + '/ws/spectator';

        try {
            this.ws = new WebSocket(wsUrl);

            this.ws.onopen = () => {
                console.log('[SpectatorMode] Connected');
                this.isConnected = true;
                this.reconnectAttempts = 0;
                this._onStatusChange('connected');
            };

            this.ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    this._handleMessage(data);
                } catch (e) {
                    console.error('[SpectatorMode] Failed to parse message:', e);
                }
            };

            this.ws.onclose = (event) => {
                console.warn('[SpectatorMode] Disconnected:', event.code);
                this.isConnected = false;
                this._onStatusChange('disconnected');
                this._attemptReconnect();
            };

            this.ws.onerror = (error) => {
                console.error('[SpectatorMode] WebSocket error:', error);
                this._onStatusChange('error');
            };
        } catch (e) {
            console.error('[SpectatorMode] Failed to connect:', e);
            this._attemptReconnect();
        }
    }

    /**
     * Handle incoming WebSocket messages.
     */
    _handleMessage(data) {
        switch (data.type) {
            case 'available_sessions':
                this._handleAvailableSessions(data);
                break;
            case 'spectator_joined':
                this._handleJoined(data);
                break;
            case 'spectator_left':
                this._handleLeft(data);
                break;
            case 'game_state':
                this._handleGameState(data);
                break;
            case 'event_history':
                this._handleEventHistory(data);
                break;
            case 'spectator_list':
                this._handleSpectatorList(data);
                break;
            case 'camera_mode_changed':
                this._handleCameraModeChanged(data);
                break;
            case 'follow_fleet':
                this._handleFollowFleet(data);
                break;
            case 'error':
                console.error('[SpectatorMode] Server error:', data.message);
                this._onStatusChange('error');
                break;
        }
    }

    _handleAvailableSessions(data) {
        if (data.sessions) {
            this._updateSessionList(data.sessions);
        }
    }

    _handleJoined(data) {
        this.currentSession = data.sessionId;
        this.spectatorId = data.spectatorId;
        this.cameraMode = data.cameraMode || 'overview';
        this._onStatusChange('joined');
        this._updateUI();
    }

    _handleLeft(data) {
        this.currentSession = null;
        this.spectatorId = null;
        this._onStatusChange('left');
        this._updateUI();
    }

    _handleGameState(data) {
        this.gameState = data.gameState || {};
        this.cameraMode = data.cameraMode || this.cameraMode;
        this.followFleetId = data.followFleetId || null;

        this._visualizeGameState(this.gameState);
        this._updateUI();

        if (this.onGameStateUpdate) {
            this.onGameStateUpdate(this.gameState);
        }
    }

    _handleEventHistory(data) {
        this.eventHistory = data.events || [];
        this._onStatusChange('history_loaded');

        if (this.onEventHistory) {
            this.onEventHistory(this.eventHistory);
        }
    }

    _handleSpectatorList(data) {
        this.spectators = data.spectators || [];
        this._updateSpectatorList();
    }

    _handleCameraModeChanged(data) {
        this.cameraMode = data.mode || 'overview';
        this._applyCameraMode(this.cameraMode);
    }

    _handleFollowFleet(data) {
        this.followFleetId = data.fleetId;
        this.cameraMode = data.cameraMode || 'follow_fleet';
        this._applyCameraMode(this.cameraMode);
    }

    /**
     * Visualize game state in 3D space.
     */
    _visualizeGameState(state) {
        if (typeof THREE === 'undefined' || !this.spectatorGroup) return;

        // Update fleet markers
        if (state.fleets) {
            for (const fleet of state.fleets) {
                this._updateFleetMarker(fleet);
            }
        }

        // Update player markers
        if (state.players) {
            for (const player of state.players) {
                this._updatePlayerMarker(player);
            }
        }
    }

    /**
     * Update or create a fleet marker.
     */
    _updateFleetMarker(fleet) {
        if (typeof THREE === 'undefined') return;

        let marker = this.fleetMarkers.get(fleet.id);
        if (!marker) {
            const geometry = new THREE.ConeGeometry(0.3, 0.6, 8);
            const material = new THREE.MeshBasicMaterial({ color: 0x00aaff });
            marker = new THREE.Mesh(geometry, material);
            marker.userData = { fleetId: fleet.id, fleetName: fleet.playerName || 'Fleet' };
            this.spectatorGroup.add(marker);
            this.fleetMarkers.set(fleet.id, marker);
        }

        // Position based on sector (simplified)
        const sector = fleet.sector || 'unknown';
        const hash = this._hashString(sector);
        marker.position.set(
            (hash % 100) - 50,
            ((hash * 7) % 100) - 50,
            ((hash * 13) % 50) - 25
        );

        // Scale by ship count
        const scale = 0.5 + (fleet.shipCount || 1) * 0.1;
        marker.scale.setScalar(scale);
    }

    /**
     * Update or create a player marker.
     */
    _updatePlayerMarker(player) {
        if (typeof THREE === 'undefined') return;

        const playerId = player.name || 'unknown';
        let marker = this.playerMarkers.get(playerId);
        if (!marker) {
            const geometry = new THREE.SphereGeometry(0.4, 8, 8);
            const material = new THREE.MeshBasicMaterial({ color: 0xffaa00 });
            marker = new THREE.Mesh(geometry, material);
            marker.userData = { playerName: player.name || 'Unknown' };
            this.spectatorGroup.add(marker);
            this.playerMarkers.set(playerId, marker);
        }

        // Position players in a circle
        const index = Array.from(this.playerMarkers.keys()).indexOf(playerId);
        const angle = (index / this.playerMarkers.size) * Math.PI * 2;
        marker.position.set(Math.cos(angle) * 60, Math.sin(angle) * 60, 0);
    }

    /**
     * Apply camera mode.
     */
    _applyCameraMode(mode) {
        this.cameraMode = mode;
        if (this.cameraModeSelect) {
            this.cameraModeSelect.value = mode;
        }

        switch (mode) {
            case 'overview':
                // Wide overview camera
                if (this.camera) {
                    this.camera.position.set(0, 50, 80);
                    this.camera.lookAt(0, 0, 0);
                }
                break;
            case 'follow_fleet':
                // Follow the selected fleet
                if (this.followFleetId && this.fleetMarkers.has(this.followFleetId)) {
                    const fleetMarker = this.fleetMarkers.get(this.followFleetId);
                    if (this.camera) {
                        this.camera.position.set(
                            fleetMarker.position.x,
                            fleetMarker.position.y + 10,
                            fleetMarker.position.z + 15
                        );
                        this.camera.lookAt(fleetMarker.position);
                    }
                }
                break;
            case 'follow_battle':
                // Focus on battle area
                if (this.camera) {
                    this.camera.position.set(0, 20, 30);
                    this.camera.lookAt(0, 0, 0);
                }
                break;
            case 'free_roam':
                // Enable free camera control (handled by OrbitControls)
                break;
        }
    }

    /**
     * Initialize the 3D spectator group.
     */
    _initSpectatorGroup() {
        if (this.spectatorGroup) return;
        if (typeof THREE === 'undefined') return;

        this.spectatorGroup = new THREE.Group();
        this.spectatorGroup.name = 'spectator-mode';
        this.scene.add(this.spectatorGroup);
    }

    /**
     * Build the UI controls.
     */
    _buildUI() {
        if (this.uiContainer) return;

        this.uiContainer = document.createElement('div');
        this.uiContainer.className = 'spectator-controls';
        this.uiContainer.style.cssText = `
            position: absolute;
            top: 20px;
            right: 20px;
            background: rgba(0, 0, 0, 0.8);
            border: 1px solid rgba(255, 170, 0, 0.5);
            border-radius: 8px;
            padding: 12px 16px;
            color: #fff;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            min-width: 250px;
            z-index: 1000;
            display: none;
        `;

        // Title
        const title = document.createElement('div');
        title.textContent = '👁 SPECTATOR MODE';
        title.style.cssText = 'color: #ffaa00; font-weight: bold; margin-bottom: 8px; font-size: 14px;';
        this.uiContainer.appendChild(title);

        // Session selector
        const sessionRow = document.createElement('div');
        sessionRow.style.cssText = 'display: flex; align-items: center; margin-bottom: 8px; gap: 8px;';
        const sessionLabel = document.createElement('span');
        sessionLabel.textContent = 'Session:';
        sessionLabel.style.color = '#ffaa00';
        this.sessionSelect = document.createElement('select');
        this.sessionSelect.style.cssText = 'background: #1a1a2e; color: #fff; border: 1px solid #ffaa00; padding: 4px 8px; border-radius: 4px; flex: 1;';
        this.sessionSelect.addEventListener('change', (e) => {
            this.joinSession(e.target.value);
        });
        sessionRow.appendChild(sessionLabel);
        sessionRow.appendChild(this.sessionSelect);
        this.uiContainer.appendChild(sessionRow);

        // Camera mode
        const cameraRow = document.createElement('div');
        cameraRow.style.cssText = 'display: flex; align-items: center; margin-bottom: 8px; gap: 8px;';
        const cameraLabel = document.createElement('span');
        cameraLabel.textContent = 'Camera:';
        cameraLabel.style.color = '#ffaa00';
        this.cameraModeSelect = document.createElement('select');
        this.cameraModeSelect.style.cssText = 'background: #1a1a2e; color: #fff; border: 1px solid #ffaa00; padding: 4px 8px; border-radius: 4px; flex: 1;';
        ['overview', 'follow_fleet', 'follow_battle', 'free_roam'].forEach(mode => {
            const option = document.createElement('option');
            option.value = mode;
            option.textContent = mode.replace('_', ' ').replace(/\b\w/g, l => l.toUpperCase());
            this.cameraModeSelect.appendChild(option);
        });
        this.cameraModeSelect.addEventListener('change', (e) => {
            this.setCameraMode(e.target.value);
        });
        cameraRow.appendChild(cameraLabel);
        cameraRow.appendChild(this.cameraModeSelect);
        this.uiContainer.appendChild(cameraRow);

        // Status
        this.statusDisplay = document.createElement('div');
        this.statusDisplay.style.cssText = 'color: #888; font-size: 11px; margin-bottom: 8px;';
        this.uiContainer.appendChild(this.statusDisplay);

        // Spectator list
        const spectatorLabel = document.createElement('div');
        spectatorLabel.textContent = 'Spectators:';
        spectatorLabel.style.cssText = 'color: #ffaa00; font-size: 11px; margin-bottom: 4px;';
        this.uiContainer.appendChild(spectatorLabel);
        this.spectatorList = document.createElement('div');
        this.spectatorList.style.cssText = 'color: #888; font-size: 11px; max-height: 80px; overflow-y: auto;';
        this.uiContainer.appendChild(this.spectatorList);

        // Event log
        const eventLabel = document.createElement('div');
        eventLabel.textContent = 'Events:';
        eventLabel.style.cssText = 'color: #ffaa00; font-size: 11px; margin-top: 8px; margin-bottom: 4px;';
        this.uiContainer.appendChild(eventLabel);
        this.eventLog = document.createElement('div');
        this.eventLog.style.cssText = 'color: #888; font-size: 10px; max-height: 60px; overflow-y: auto;';
        this.uiContainer.appendChild(this.eventLog);

        this.container.appendChild(this.uiContainer);
    }

    /**
     * Update the session list dropdown.
     */
    _updateSessionList(sessions) {
        if (!this.sessionSelect) return;

        const current = this.sessionSelect.value;
        this.sessionSelect.innerHTML = '';
        sessions.forEach(session => {
            const option = document.createElement('option');
            option.value = session.sessionId;
            option.textContent = `${session.sessionId} (${session.playerCount} players, ${session.spectatorCount} spectators)`;
            this.sessionSelect.appendChild(option);
        });
        if (current) {
            this.sessionSelect.value = current;
        }
    }

    /**
     * Update the spectator list display.
     */
    _updateSpectatorList() {
        if (!this.spectatorList) return;
        this.spectatorList.textContent = this.spectators.length > 0
            ? this.spectators.join(', ')
            : 'No other spectators';
    }

    /**
     * Update all UI elements.
     */
    _updateUI() {
        if (this.statusDisplay) {
            const status = this.currentSession ? `Watching: ${this.currentSession}` : 'Not watching';
            this.statusDisplay.textContent = status;
        }
    }

    /**
     * Show the UI controls.
     */
    showUI() {
        this._buildUI();
        if (this.uiContainer) {
            this.uiContainer.style.display = 'block';
        }
    }

    /**
     * Hide the UI controls.
     */
    hideUI() {
        if (this.uiContainer) {
            this.uiContainer.style.display = 'none';
        }
    }

    // ==================== Spectator Controls ====================

    /**
     * Join a session for observation.
     */
    joinSession(sessionId) {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'join', sessionId: sessionId });
        this._initSpectatorGroup();
        this.showUI();
    }

    /**
     * Leave the current session.
     */
    leaveSession() {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'leave' });
    }

    /**
     * Change camera mode.
     */
    setCameraMode(mode) {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'camera_mode', mode: mode });
    }

    /**
     * Follow a specific fleet.
     */
    followFleet(fleetId) {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'follow_fleet', fleetId: fleetId });
    }

    /**
     * List available sessions.
     */
    listSessions() {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'list_sessions' });
    }

    // ==================== WebSocket Communication ====================

    _sendMessage(message) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify(message));
        }
    }

    _attemptReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.error('[SpectatorMode] Max reconnection attempts reached');
            this._onStatusChange('max_reconnect');
            return;
        }

        const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
        this.reconnectAttempts++;

        this.reconnectTimer = setTimeout(() => {
            if (this.serverUrl) {
                this.connect(this.serverUrl);
            }
        }, delay);
    }

    // ==================== Callbacks ====================

    _onStatusChange(status) {
        if (this.onStatusChange) {
            this.onStatusChange(status);
        }
    }

    setOnStatusChange(callback) {
        this.onStatusChange = callback;
    }

    setOnGameStateUpdate(callback) {
        this.onGameStateUpdate = callback;
    }

    setOnEventHistory(callback) {
        this.onEventHistory = callback;
    }

    // ==================== Utilities ====================

    _hashString(str) {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            hash = ((hash << 5) - hash) + str.charCodeAt(i);
            hash |= 0;
        }
        return Math.abs(hash);
    }

    // ==================== Cleanup ====================

    _disposeObject(object) {
        if (object.geometry) object.geometry.dispose();
        if (object.material) {
            if (Array.isArray(object.material)) {
                object.material.forEach(m => m.dispose());
            } else {
                object.material.dispose();
            }
        }
    }

    /**
     * Destroy the spectator mode.
     */
    destroy() {
        this.leaveSession();

        if (this.ws) {
            this.ws.onclose = null;
            this.ws.onerror = null;
            this.ws.close();
            this.ws = null;
        }

        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }

        if (this.uiContainer && this.uiContainer.parentNode) {
            this.uiContainer.parentNode.removeChild(this.uiContainer);
        }

        if (this.spectatorGroup && this.scene) {
            while (this.spectatorGroup.children.length > 0) {
                const child = this.spectatorGroup.children[0];
                this.spectatorGroup.remove(child);
                this._disposeObject(child);
            }
            this.scene.remove(this.spectatorGroup);
        }

        this.fleetMarkers.clear();
        this.playerMarkers.clear();
        this.battleMarkers = [];
        this.eventHistory = [];

        console.log('[SpectatorMode] Destroyed');
    }

    /**
     * Get current spectator state.
     */
    getState() {
        return {
            connected: this.isConnected,
            session: this.currentSession,
            spectatorId: this.spectatorId,
            cameraMode: this.cameraMode,
            followFleetId: this.followFleetId,
            gameState: this.gameState,
            spectatorCount: this.spectators.length
        };
    }
}

// Expose globally
window.SpectatorMode = SpectatorMode;

/**
 * ReplayViz — 3D Game Replay Visualization
 *
 * Visualizes recorded game action logs in 3D space with playback controls.
 * Connects to /ws/replay WebSocket endpoint for real-time replay streaming.
 *
 * Features:
 * - Playback controls: play, pause, step forward/backward, speed control
 * - Timeline scrubber with action log entries
 * - 3D reconstruction of fleet movements, battles, and events
 * - Session list for selecting replay sessions
 *
 * Usage:
 *   const replayViz = new ReplayViz(containerElement, scene, camera);
 *   replayViz.connect(serverUrl);
 *   replayViz.selectSession(sessionId);
 *   replayViz.play();
 *   replayViz.destroy();
 */

class ReplayViz {
    constructor(container, scene, camera) {
        this.container = container;
        this.scene = scene;
        this.camera = camera;
        this.ws = null;
        this.isConnected = false;
        this.serverUrl = null;

        // Replay state
        this.currentSession = null;
        this.actionLogs = [];
        this.currentIndex = -1;
        this.isPlaying = false;
        this.speed = 1.0;
        this.totalActions = 0;

        // Three.js objects
        this.replayGroup = null;
        this.actionMarkers = [];
        this.fleetPositions = new Map(); // Track fleet positions over time
        this.effectPool = [];

        // UI elements
        this.uiContainer = null;
        this.timelineBar = null;
        this.timelineProgress = null;
        this.sessionList = null;
        this.statusDisplay = null;
        this.actionInfo = null;

        // Reconnection
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectTimer = null;

        // Callbacks
        this.onStatusChange = null;
        this.onActionPlay = null;
        this.onSessionSelect = null;

        // Animation
        this.animationId = null;
        this.clock = new (typeof THREE !== 'undefined' ? THREE.Clock : Object)();
    }

    /**
     * Connect to the replay WebSocket endpoint.
     */
    connect(serverUrl) {
        this.serverUrl = serverUrl || (window.authService && window.authService.getServerUrl ? window.authService.getServerUrl() : 'http://localhost:8080');

        const wsUrl = this.serverUrl.replace('http://', 'ws://').replace('https://', 'wss://') + '/ws/replay';

        try {
            this.ws = new WebSocket(wsUrl);

            this.ws.onopen = () => {
                console.log('[ReplayViz] Connected to replay endpoint');
                this.isConnected = true;
                this.reconnectAttempts = 0;
                this._onStatusChange('connected');
            };

            this.ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    this._handleMessage(data);
                } catch (e) {
                    console.error('[ReplayViz] Failed to parse message:', e);
                }
            };

            this.ws.onclose = (event) => {
                console.warn('[ReplayViz] Disconnected:', event.code, event.reason);
                this.isConnected = false;
                this.isPlaying = false;
                this._onStatusChange('disconnected');
                this._attemptReconnect();
            };

            this.ws.onerror = (error) => {
                console.error('[ReplayViz] WebSocket error:', error);
                this._onStatusChange('error');
            };
        } catch (e) {
            console.error('[ReplayViz] Failed to connect:', e);
            this._attemptReconnect();
        }
    }

    /**
     * Handle incoming WebSocket messages.
     */
    _handleMessage(data) {
        switch (data.type) {
            case 'replay_sessions':
                this._handleSessions(data);
                break;
            case 'replay_snapshot':
                this._handleSnapshot(data);
                break;
            case 'replay_action':
                this._handleAction(data);
                break;
            case 'replay_status':
                this._handleStatus(data);
                break;
            case 'replay_timeline':
                this._handleTimeline(data);
                break;
            case 'error':
                console.error('[ReplayViz] Server error:', data.message);
                this._onStatusChange('error');
                break;
        }
    }

    /**
     * Handle available sessions list.
     */
    _handleSessions(data) {
        if (!data.sessions || data.sessions.length === 0) return;
        this._updateSessionList(data.sessions);
    }

    /**
     * Handle initial replay snapshot.
     */
    _handleSnapshot(data) {
        this.currentSession = data.sessionId;
        this.totalActions = data.totalActions;
        this.actionLogs = data.timeline || [];
        this.currentIndex = -1;

        this._updateUI();
        this._onStatusChange('ready');

        if (this.onSessionSelect) {
            this.onSessionSelect(this.currentSession, this.totalActions);
        }
    }

    /**
     * Handle individual action during playback.
     */
    _handleAction(data) {
        this.currentIndex = data.index;
        this._visualizeAction(data);
        this._updateTimeline();

        if (this.onActionPlay) {
            this.onActionPlay(data);
        }
    }

    /**
     * Handle playback status updates.
     */
    _handleStatus(data) {
        this.isPlaying = data.status === 'playing';
        this.speed = data.speed || 1.0;
        this.currentIndex = data.currentIndex;
        this._onStatusChange(data.status);
        this._updateUI();
    }

    /**
     * Handle timeline progress updates.
     */
    _handleTimeline(data) {
        this.currentIndex = data.currentIndex;
        this.isPlaying = data.playing;
        this.speed = data.speed;
        this._updateTimeline();
    }

    /**
     * Visualize a single action in 3D space.
     */
    _visualizeAction(data) {
        if (typeof THREE === 'undefined') return;

        const actionType = data.actionType;
        const actionData = data.data || {};

        // Create action marker
        const marker = this._createActionMarker(actionType, actionData);
        if (marker) {
            this.replayGroup.add(marker);
            this.actionMarkers.push(marker);

            // Limit visible markers to last 50 actions
            while (this.actionMarkers.length > 50) {
                const old = this.actionMarkers.shift();
                this.replayGroup.remove(old);
                this._disposeObject(old);
            }
        }

        // Update fleet positions based on action data
        if (actionData.fleetName && actionData.x != null && actionData.y != null) {
            this.fleetPositions.set(actionData.fleetName, {
                x: actionData.x,
                y: actionData.y,
                z: actionData.z || 0,
                timestamp: data.timestamp
            });
            this._updateFleetMarker(actionData.fleetName, actionData.x, actionData.y, actionData.z || 0);
        }

        // Create effect for specific action types
        switch (actionType) {
            case 'ENGAGE_COMBAT':
                this._createCombatEffect(actionData);
                break;
            case 'MOVE_FLEET':
                this._createMovementEffect(actionData);
                break;
            case 'BUILD_STATION':
            case 'BUILD_FACILITY':
                this._createBuildEffect(actionData);
                break;
            case 'GAME_STARTED':
            case 'GAME_ENDED':
                this._createSystemEffect(actionType, actionData);
                break;
        }
    }

    /**
     * Create a marker for an action.
     */
    _createActionMarker(actionType, actionData) {
        if (typeof THREE === 'undefined') return null;

        const x = actionData.x || 0;
        const y = actionData.y || 0;
        const z = actionData.z || 0;

        let color, size;
        switch (actionType) {
            case 'MOVE_FLEET':
                color = 0x00ff00;
                size = 0.3;
                break;
            case 'ENGAGE_COMBAT':
                color = 0xff0000;
                size = 0.5;
                break;
            case 'BUILD_SHIP':
            case 'BUILD_SHIP_FROM_DESIGN':
                color = 0x0088ff;
                size = 0.4;
                break;
            case 'BUILD_STATION':
            case 'BUILD_FACILITY':
                color = 0xffaa00;
                size = 0.4;
                break;
            case 'BUY_COMMODITY':
            case 'SELL_COMMODITY':
                color = 0x00ffaa;
                size = 0.3;
                break;
            case 'COLONIZE_PLANET':
                color = 0xaa00ff;
                size = 0.6;
                break;
            case 'PLAYER_JOINED':
                color = 0x00ffff;
                size = 0.4;
                break;
            case 'PLAYER_LEFT':
                color = 0xff8888;
                size = 0.4;
                break;
            case 'GAME_STARTED':
            case 'GAME_ENDED':
                color = 0xffff00;
                size = 0.8;
                break;
            default:
                color = 0xffffff;
                size = 0.3;
        }

        const geometry = new THREE.SphereGeometry(size, 8, 8);
        const material = new THREE.MeshBasicMaterial({
            color: color,
            transparent: true,
            opacity: 0.8
        });
        const marker = new THREE.Mesh(geometry, material);
        marker.position.set(x, y, z);
        marker.userData = {
            actionType: actionType,
            index: this.currentIndex,
            timestamp: Date.now(),
            fadeOut: true
        };

        return marker;
    }

    /**
     * Create combat effect.
     */
    _createCombatEffect(actionData) {
        if (typeof THREE === 'undefined') return;

        const x = actionData.x || 0;
        const y = actionData.y || 0;
        const z = actionData.z || 0;

        // Explosion particles
        const particleCount = 15;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(particleCount * 3);
        const velocities = [];

        for (let i = 0; i < particleCount; i++) {
            positions[i * 3] = x;
            positions[i * 3 + 1] = y;
            positions[i * 3 + 2] = z;
            velocities.push({
                x: (Math.random() - 0.5) * 0.5,
                y: (Math.random() - 0.5) * 0.5,
                z: (Math.random() - 0.5) * 0.5
            });
        }

        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        const material = new THREE.PointsMaterial({
            color: 0xff4400,
            size: 0.3,
            transparent: true,
            opacity: 1.0
        });

        const particles = new THREE.Points(geometry, material);
        particles.userData = { velocities: velocities, lifetime: 60 };
        this.replayGroup.add(particles);
        this.effectPool.push(particles);
    }

    /**
     * Create movement effect (trail line).
     */
    _createMovementEffect(actionData) {
        if (typeof THREE === 'undefined') return;

        const fleetName = actionData.fleetName;
        const prevPos = this.fleetPositions.get(fleetName);
        if (!prevPos) return;

        const x = actionData.x;
        const y = actionData.y;
        const z = actionData.z || 0;

        const points = [
            new THREE.Vector3(prevPos.x, prevPos.y, prevPos.z),
            new THREE.Vector3(x, y, z)
        ];
        const geometry = new THREE.BufferGeometry().setFromPoints(points);
        const material = new THREE.LineBasicMaterial({
            color: 0x00ff00,
            transparent: true,
            opacity: 0.5
        });
        const line = new THREE.Line(geometry, material);
        line.userData = { fadeOut: true, lifetime: 120 };
        this.replayGroup.add(line);
        this.effectPool.push(line);
    }

    /**
     * Create build effect (spinning ring).
     */
    _createBuildEffect(actionData) {
        if (typeof THREE === 'undefined') return;

        const x = actionData.x || 0;
        const y = actionData.y || 0;
        const z = actionData.z || 0;

        const geometry = new THREE.RingGeometry(0.3, 0.5, 16);
        const material = new THREE.MeshBasicMaterial({
            color: 0xffaa00,
            transparent: true,
            opacity: 0.8,
            side: THREE.DoubleSide
        });
        const ring = new THREE.Mesh(geometry, material);
        ring.position.set(x, y, z);
        ring.rotation.x = Math.PI / 2;
        ring.userData = { fadeOut: true, lifetime: 90, rotate: true };
        this.replayGroup.add(ring);
        this.effectPool.push(ring);
    }

    /**
     * Create system effect (game start/end).
     */
    _createSystemEffect(actionType, actionData) {
        if (typeof THREE === 'undefined') return;

        const color = actionType === 'GAME_STARTED' ? 0x00ff00 : 0xff0000;

        // Flash effect
        const geometry = new THREE.SphereGeometry(2, 16, 16);
        const material = new THREE.MeshBasicMaterial({
            color: color,
            transparent: true,
            opacity: 0.3,
            side: THREE.DoubleSide
        });
        const flash = new THREE.Mesh(geometry, material);
        flash.userData = { fadeOut: true, lifetime: 30, expand: true };
        this.replayGroup.add(flash);
        this.effectPool.push(flash);
    }

    /**
     * Update fleet marker position.
     */
    _updateFleetMarker(fleetName, x, y, z) {
        if (typeof THREE === 'undefined') return;

        let marker = this._findFleetMarker(fleetName);
        if (!marker) {
            const geometry = new THREE.ConeGeometry(0.3, 0.6, 8);
            const material = new THREE.MeshBasicMaterial({ color: 0x00aaff });
            marker = new THREE.Mesh(geometry, material);
            marker.userData.fleetName = fleetName;
            this.replayGroup.add(marker);
        }
        marker.position.set(x, y, z);
    }

    /**
     * Find existing fleet marker by name.
     */
    _findFleetMarker(fleetName) {
        for (const child of this.replayGroup.children) {
            if (child.userData && child.userData.fleetName === fleetName) {
                return child;
            }
        }
        return null;
    }

    /**
     * Initialize the 3D replay group.
     */
    _initReplayGroup() {
        if (this.replayGroup) return;
        if (typeof THREE === 'undefined') return;

        this.replayGroup = new THREE.Group();
        this.replayGroup.name = 'replay-viz';
        this.scene.add(this.replayGroup);
    }

    /**
     * Build the UI controls.
     */
    _buildUI() {
        if (this.uiContainer) return;

        this.uiContainer = document.createElement('div');
        this.uiContainer.className = 'replay-controls';
        this.uiContainer.style.cssText = `
            position: absolute;
            bottom: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0, 0, 0, 0.8);
            border: 1px solid rgba(0, 170, 255, 0.5);
            border-radius: 8px;
            padding: 12px 16px;
            color: #fff;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            min-width: 400px;
            z-index: 1000;
            display: none;
        `;

        // Session selector
        const sessionRow = document.createElement('div');
        sessionRow.style.cssText = 'display: flex; align-items: center; margin-bottom: 8px; gap: 8px;';
        const sessionLabel = document.createElement('span');
        sessionLabel.textContent = 'Session:';
        sessionLabel.style.color = '#00aaff';
        const sessionSelect = document.createElement('select');
        sessionSelect.id = 'replay-session-select';
        sessionSelect.style.cssText = 'background: #1a1a2e; color: #fff; border: 1px solid #00aaff; padding: 4px 8px; border-radius: 4px; flex: 1;';
        sessionSelect.addEventListener('change', (e) => {
            this.selectSession(e.target.value);
        });
        this.sessionList = sessionSelect;
        sessionRow.appendChild(sessionLabel);
        sessionRow.appendChild(sessionSelect);
        this.uiContainer.appendChild(sessionRow);

        // Timeline
        const timelineContainer = document.createElement('div');
        timelineContainer.style.cssText = 'margin-bottom: 8px;';
        this.timelineBar = document.createElement('div');
        this.timelineBar.style.cssText = `
            width: 100%; height: 8px; background: #1a1a2e; border-radius: 4px;
            overflow: hidden; cursor: pointer; position: relative;
        `;
        this.timelineProgress = document.createElement('div');
        this.timelineProgress.style.cssText = `
            width: 0%; height: 100%; background: linear-gradient(90deg, #00aaff, #00ff88);
            transition: width 0.1s linear;
        `;
        this.timelineBar.appendChild(this.timelineProgress);
        this.timelineBar.addEventListener('click', (e) => {
            const rect = this.timelineBar.getBoundingClientRect();
            const ratio = (e.clientX - rect.left) / rect.width;
            const index = Math.floor(ratio * this.totalActions);
            this.seek(index);
        });
        timelineContainer.appendChild(this.timelineBar);
        this.uiContainer.appendChild(timelineContainer);

        // Controls
        const controlsRow = document.createElement('div');
        controlsRow.style.cssText = 'display: flex; align-items: center; gap: 8px; margin-bottom: 8px;';

        const createBtn = (label, onClick) => {
            const btn = document.createElement('button');
            btn.textContent = label;
            btn.style.cssText = 'background: #1a1a2e; color: #00aaff; border: 1px solid #00aaff; padding: 4px 12px; border-radius: 4px; cursor: pointer;';
            btn.addEventListener('click', onClick);
            return btn;
        };

        controlsRow.appendChild(createBtn('⏮', () => this.stop()));
        controlsRow.appendChild(createBtn('⏪', () => this.stepBackward()));
        controlsRow.appendChild(createBtn('▶', () => this.play()));
        controlsRow.appendChild(createBtn('⏸', () => this.pause()));
        controlsRow.appendChild(createBtn('⏩', () => this.stepForward()));
        controlsRow.appendChild(createBtn('⏹', () => this.stop()));

        // Speed control
        const speedSelect = document.createElement('select');
        speedSelect.id = 'replay-speed-select';
        speedSelect.style.cssText = 'background: #1a1a2e; color: #fff; border: 1px solid #00aaff; padding: 4px; border-radius: 4px; margin-left: auto;';
        [0.5, 1.0, 2.0, 4.0].forEach(speed => {
            const option = document.createElement('option');
            option.value = speed;
            option.textContent = speed + 'x';
            speedSelect.appendChild(option);
        });
        speedSelect.addEventListener('change', (e) => {
            this.speed = parseFloat(e.target.value);
            if (this.isPlaying) {
                this.play(this.speed);
            }
        });
        controlsRow.appendChild(speedSelect);
        this.uiContainer.appendChild(controlsRow);

        // Status and action info
        const statusRow = document.createElement('div');
        statusRow.style.cssText = 'display: flex; justify-content: space-between; font-size: 11px;';
        this.statusDisplay = document.createElement('span');
        this.statusDisplay.style.color = '#888';
        this.actionInfo = document.createElement('span');
        this.actionInfo.style.color = '#00ff88';
        statusRow.appendChild(this.statusDisplay);
        statusRow.appendChild(this.actionInfo);
        this.uiContainer.appendChild(statusRow);

        this.container.appendChild(this.uiContainer);
    }

    /**
     * Update the session list dropdown.
     */
    _updateSessionList(sessions) {
        if (!this.sessionList) return;

        this.sessionList.innerHTML = '';
        sessions.forEach(session => {
            const option = document.createElement('option');
            option.value = session.sessionId;
            option.textContent = `${session.sessionId} (${session.actionCount} actions, ${session.source})`;
            this.sessionList.appendChild(option);
        });
    }

    /**
     * Update the timeline progress bar.
     */
    _updateTimeline() {
        if (!this.timelineProgress) return;
        const progress = this.totalActions > 0 ? ((this.currentIndex + 1) / this.totalActions) * 100 : 0;
        this.timelineProgress.style.width = progress + '%';
    }

    /**
     * Update all UI elements.
     */
    _updateUI() {
        if (this.statusDisplay) {
            this.statusDisplay.textContent = this.isPlaying ? 'PLAYING' : this.currentSession ? 'PAUSED' : 'READY';
        }
        if (this.actionInfo) {
            this.actionInfo.textContent = `Action ${this.currentIndex + 1}/${this.totalActions}`;
        }
        this._updateTimeline();
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

    // ==================== Playback Controls ====================

    /**
     * Select a replay session.
     */
    selectSession(sessionId) {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'join', sessionId: sessionId });
    }

    /**
     * Start playback.
     */
    play(speed) {
        if (!this.isConnected || !this.ws) return;
        if (speed) this.speed = speed;
        this._sendMessage({ type: 'play', speed: this.speed });
        this._initReplayGroup();
        this.showUI();
    }

    /**
     * Pause playback.
     */
    pause() {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'pause' });
    }

    /**
     * Stop and reset playback.
     */
    stop() {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'stop' });
    }

    /**
     * Step forward one action.
     */
    stepForward() {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'step_forward' });
        this.showUI();
    }

    /**
     * Step backward one action.
     */
    stepBackward() {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'step_backward' });
        this.showUI();
    }

    /**
     * Seek to specific action index.
     */
    seek(index) {
        if (!this.isConnected || !this.ws) return;
        this._sendMessage({ type: 'seek', index: index });
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
            console.error('[ReplayViz] Max reconnection attempts reached');
            this._onStatusChange('max_reconnect');
            return;
        }

        const delay = Math.min(1000 * Math.pow(2, this.reconnectAttempts), 30000);
        this.reconnectAttempts++;

        console.log(`[ReplayViz] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`);
        this.reconnectTimer = setTimeout(() => {
            if (this.serverUrl) {
                this.connect(this.serverUrl);
            }
        }, delay);
    }

    // ==================== Animation Loop ====================

    /**
     * Update animation effects.
     */
    update() {
        // Fade out effects
        for (let i = this.effectPool.length - 1; i >= 0; i--) {
            const effect = this.effectPool[i];
            if (effect.userData.lifetime !== undefined) {
                effect.userData.lifetime--;
                effect.material.opacity = Math.max(0, effect.userData.lifetime / 60);

                if (effect.userData.rotate) {
                    effect.rotation.z += 0.05;
                }
                if (effect.userData.expand) {
                    effect.scale.multiplyScalar(1.05);
                }

                // Animate particle velocities
                if (effect.userData.velocities) {
                    const positions = effect.geometry.attributes.position.array;
                    for (let j = 0; j < effect.userData.velocities.length; j++) {
                        positions[j * 3] += effect.userData.velocities[j].x;
                        positions[j * 3 + 1] += effect.userData.velocities[j].y;
                        positions[j * 3 + 2] += effect.userData.velocities[j].z;
                        effect.userData.velocities[j].y -= 0.01; // gravity
                    }
                    effect.geometry.attributes.position.needsUpdate = true;
                }

                if (effect.userData.lifetime <= 0) {
                    this.replayGroup.remove(effect);
                    this._disposeObject(effect);
                    this.effectPool.splice(i, 1);
                }
            }
        }

        // Fade out old action markers
        for (let i = this.actionMarkers.length - 1; i >= 0; i--) {
            const marker = this.actionMarkers[i];
            const age = Date.now() - marker.userData.timestamp;
            if (age > 10000) { // Fade after 10 seconds
                marker.material.opacity = Math.max(0, 1 - (age - 10000) / 5000);
                if (marker.material.opacity <= 0) {
                    this.replayGroup.remove(marker);
                    this._disposeObject(marker);
                    this.actionMarkers.splice(i, 1);
                }
            }
        }
    }

    // ==================== Callbacks ====================

    _onStatusChange(status) {
        if (this.onStatusChange) {
            this.onStatusChange(status);
        }
    }

    /**
     * Set callback for status changes.
     */
    setOnStatusChange(callback) {
        this.onStatusChange = callback;
    }

    /**
     * Set callback for action playback.
     */
    setOnActionPlay(callback) {
        this.onActionPlay = callback;
    }

    /**
     * Set callback for session selection.
     */
    setOnSessionSelect(callback) {
        this.onSessionSelect = callback;
    }

    // ==================== Cleanup ====================

    /**
     * Dispose of a Three.js object.
     */
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
     * Destroy the replay visualization.
     */
    destroy() {
        // Stop playback
        this.stop();

        // Close WebSocket
        if (this.ws) {
            this.ws.onclose = null;
            this.ws.onerror = null;
            this.ws.close();
            this.ws = null;
        }

        // Clear reconnection timer
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer);
            this.reconnectTimer = null;
        }

        // Remove UI
        if (this.uiContainer && this.uiContainer.parentNode) {
            this.uiContainer.parentNode.removeChild(this.uiContainer);
        }

        // Clear 3D objects
        if (this.replayGroup && this.scene) {
            while (this.replayGroup.children.length > 0) {
                const child = this.replayGroup.children[0];
                this.replayGroup.remove(child);
                this._disposeObject(child);
            }
            this.scene.remove(this.replayGroup);
        }

        this.actionMarkers = [];
        this.effectPool = [];
        this.fleetPositions.clear();
        this.actionLogs = [];

        console.log('[ReplayViz] Destroyed');
    }

    /**
     * Get current replay state.
     */
    getState() {
        return {
            connected: this.isConnected,
            session: this.currentSession,
            playing: this.isPlaying,
            speed: this.speed,
            currentIndex: this.currentIndex,
            totalActions: this.totalActions,
            progress: this.totalActions > 0 ? (this.currentIndex + 1) / this.totalActions : 0
        };
    }
}

// Expose globally
window.ReplayViz = ReplayViz;

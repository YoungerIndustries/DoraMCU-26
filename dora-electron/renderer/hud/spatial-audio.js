/**
 * SpatialAudio — Proximity-based voice chat with 3D spatial audio.
 *
 * Provides:
 * - WebRTC peer connection management via VoiceChatService signaling
 * - 3D positional audio: volume and direction based on fleet/player distance
 * - Voice room per game session with participant tracking
 * - Mute/unmute state visualization (microphone icons on player avatars)
 * - Integrated with PresenceManager for speaker activity indicators
 * - WebSocket integration with VoiceChatService for room management
 *
 * Usage:
 *   const audio = new SpatialAudio(sceneRenderer, apiBaseUrl);
 *   await audio.joinRoom(roomId);
 *   audio.setPlayerPosition(playerId, x, y, z);
 *   audio.destroy();
 */

class SpatialAudio {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     * @param {string} apiBaseUrl - Base URL for the API
     */
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // Audio context
        this.audioContext = null;
        this.gainNodes = new Map(); // playerId -> GainNode
        this.pannerNodes = new Map(); // playerId -> PannerNode
        this.mediaStreams = new Map(); // playerId -> MediaStream
        
        // Player positions
        this.playerPositions = new Map(); // playerId -> {x, y, z}
        this.localPlayerId = null;
        this.localPosition = { x: 0, y: 0, z: 0 };
        
        // Voice room
        this.currentRoom = null;
        this.participants = new Map(); // playerId -> participant data
        this.isMuted = false;
        this.userMediaStream = null;
        
        // WebSocket
        this.ws = null;
        this.wsConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectDelay = 1000;
        
        // 3D objects
        this.speakerIndicators = new Map(); // playerId -> THREE.Group
        
        // UI elements
        this.container = null;
        this.controls = null;
        this.participantList = null;
        
        // Animation
        this.animationId = null;
        this.clock = new THREE.Clock();
        
        // Callbacks
        this.onSpeakerActivity = null;
        this.onRoomJoin = null;
        this.onRoomLeave = null;
    }

    /**
     * Initialize the spatial audio module.
     */
    init() {
        // Create audio context
        this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
        
        // Create UI
        this._createUI();
        
        console.log('[SpatialAudio] Initialized');
    }

    /**
     * Create the UI elements.
     */
    _createUI() {
        // Create main container
        this.container = document.createElement('div');
        this.container.id = 'spatial-audio-container';
        this.container.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            pointer-events: none;
            z-index: 100;
        `;
        document.body.appendChild(this.container);
        
        // Create controls
        this.controls = document.createElement('div');
        this.controls.className = 'spatial-audio-controls';
        this.controls.style.cssText = `
            position: absolute;
            bottom: 10px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(100, 200, 200, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            display: flex;
            gap: 10px;
            align-items: center;
        `;
        this.controls.innerHTML = `
            <span id="audio-status" style="color: #88aaaa; font-size: 11px; font-family: 'Courier New', monospace;">
                🔇 Disconnected
            </span>
            <button id="audio-mute-btn" style="
                background: rgba(50, 100, 200, 0.3);
                border: 1px solid rgba(100, 150, 255, 0.5);
                color: #aaccff;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">🎤 Mute</button>
            <button id="audio-join-btn" style="
                background: rgba(50, 150, 50, 0.3);
                border: 1px solid rgba(100, 200, 100, 0.5);
                color: #aaffaa;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">Join Voice</button>
            <button id="audio-participants-btn" style="
                background: rgba(50, 100, 200, 0.3);
                border: 1px solid rgba(100, 150, 255, 0.5);
                color: #aaccff;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">👥 Participants</button>
        `;
        this.container.appendChild(this.controls);
        
        // Create participant list
        this.participantList = document.createElement('div');
        this.participantList.className = 'participant-list';
        this.participantList.style.cssText = `
            position: absolute;
            bottom: 60px;
            left: 50%;
            transform: translateX(-50%);
            width: 250px;
            max-height: 200px;
            overflow-y: auto;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(100, 200, 200, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            color: #aaffaa;
            font-family: 'Courier New', monospace;
            font-size: 11px;
            display: none;
        `;
        this.participantList.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 8px; color: #88ffaa;">
                🎙️ Voice Participants
            </div>
            <div id="participant-items"></div>
        `;
        this.container.appendChild(this.participantList);
        
        // Event listeners
        document.getElementById('audio-mute-btn').addEventListener('click', () => {
            this.toggleMute();
        });
        document.getElementById('audio-join-btn').addEventListener('click', () => {
            this.toggleRoom();
        });
        document.getElementById('audio-participants-btn').addEventListener('click', () => {
            this.toggleParticipantList();
        });
        
        console.log('[SpatialAudio] UI created');
    }

    /**
     * Toggle the participant list.
     */
    toggleParticipantList() {
        if (!this.participantList) return;
        
        if (this.participantList.style.display === 'none') {
            this.participantList.style.display = 'block';
            this.updateParticipantListUI();
        } else {
            this.participantList.style.display = 'none';
        }
    }

    /**
     * Update the participant list UI.
     */
    updateParticipantListUI() {
        const container = document.getElementById('participant-items');
        if (!container) return;
        
        container.innerHTML = '';
        
        if (this.participants.size === 0) {
            container.innerHTML = '<div style="color: #88aa88;">No participants</div>';
            return;
        }
        
        for (const [playerId, participant] of this.participants) {
            const item = document.createElement('div');
            item.style.cssText = `
                padding: 5px;
                margin-bottom: 3px;
                background: rgba(50, 100, 200, 0.2);
                border-radius: 4px;
                display: flex;
                justify-content: space-between;
                align-items: center;
            `;
            
            const isSpeaking = participant.isSpeaking;
            const isMuted = participant.isMuted;
            
            item.innerHTML = `
                <span style="color: ${isSpeaking ? '#aaffaa' : '#8899aa'};">
                    ${isSpeaking ? '🔊' : isMuted ? '🔇' : '🎤'} ${participant.name || playerId}
                </span>
                <span style="color: #667788; font-size: 10px;">
                    ${this.getDistance(playerId).toFixed(1)}u
                </span>
            `;
            container.appendChild(item);
        }
    }

    /**
     * Get distance from local player to another player.
     * @param {string} playerId - The player ID
     * @returns {number} Distance in units
     */
    getDistance(playerId) {
        const pos = this.playerPositions.get(playerId);
        if (!pos) return 0;
        
        const dx = pos.x - this.localPosition.x;
        const dy = pos.y - this.localPosition.y;
        const dz = pos.z - this.localPosition.z;
        
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Connect to the voice chat WebSocket.
     */
    connect() {
        if (this.wsConnected) return;
        
        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';
            const wsUrl = serverUrl.replace('http', 'ws') + '/ws/voice-chat';
            
            this.ws = new WebSocket(wsUrl);
            
            this.ws.onopen = () => {
                this.wsConnected = true;
                this.reconnectAttempts = 0;
                this._updateStatus('🔌 Connected');
                console.log('[SpatialAudio] WebSocket connected');
            };
            
            this.ws.onmessage = (event) => {
                this._handleMessage(event.data);
            };
            
            this.ws.onclose = () => {
                this.wsConnected = false;
                this._updateStatus('🔇 Disconnected');
                this._reconnect();
            };
            
            this.ws.onerror = (error) => {
                console.error('[SpatialAudio] WebSocket error:', error);
            };
        } catch (error) {
            console.error('[SpatialAudio] Failed to connect:', error);
        }
    }

    /**
     * Handle incoming WebSocket messages.
     * @param {string} data - JSON message data
     */
    _handleMessage(data) {
        try {
            const message = JSON.parse(data);
            
            switch (message.type) {
                case 'participant_joined':
                    this.participants.set(message.playerId, {
                        name: message.name,
                        isSpeaking: false,
                        isMuted: false
                    });
                    this.updateParticipantListUI();
                    break;
                    
                case 'participant_left':
                    this.participants.delete(message.playerId);
                    this._cleanupPlayerAudio(message.playerId);
                    this.updateParticipantListUI();
                    break;
                    
                case 'speaker_activity':
                    this._handleSpeakerActivity(message.playerId, message.isSpeaking);
                    break;
                    
                case 'mute_state':
                    this._handleMuteState(message.playerId, message.isMuted);
                    break;
                    
                case 'room_info':
                    this._updateStatus(`🎙️ Room: ${message.roomName} (${message.participantCount} participants)`);
                    break;
            }
        } catch (error) {
            console.error('[SpatialAudio] Failed to handle message:', error);
        }
    }

    /**
     * Handle speaker activity update.
     * @param {string} playerId - The player ID
     * @param {boolean} isSpeaking - Whether the player is speaking
     */
    _handleSpeakerActivity(playerId, isSpeaking) {
        const participant = this.participants.get(playerId);
        if (participant) {
            participant.isSpeaking = isSpeaking;
            this.updateParticipantListUI();
        }
        
        // Update speaker indicator
        this._updateSpeakerIndicator(playerId, isSpeaking);
        
        if (this.onSpeakerActivity) {
            this.onSpeakerActivity(playerId, isSpeaking);
        }
    }

    /**
     * Handle mute state update.
     * @param {string} playerId - The player ID
     * @param {boolean} isMuted - Whether the player is muted
     */
    _handleMuteState(playerId, isMuted) {
        const participant = this.participants.get(playerId);
        if (participant) {
            participant.isMuted = isMuted;
            this.updateParticipantListUI();
        }
    }

    /**
     * Update the speaker indicator for a player.
     * @param {string} playerId - The player ID
     * @param {boolean} isSpeaking - Whether the player is speaking
     */
    _updateSpeakerIndicator(playerId, isSpeaking) {
        const indicator = this.speakerIndicators.get(playerId);
        if (!indicator) return;
        
        indicator.traverse(child => {
            if (child.isMesh && child.material.emissive) {
                child.material.emissiveIntensity = isSpeaking ? 0.8 : 0.2;
                child.material.color.setHex(isSpeaking ? 0x44ff44 : 0x444444);
                child.material.emissive.setHex(isSpeaking ? 0x22aa22 : 0x222222);
            }
        });
    }

    /**
     * Join a voice room.
     * @param {string} roomId - The room ID
     */
    async joinRoom(roomId) {
        if (this.currentRoom === roomId) return;
        
        this.currentRoom = roomId;
        
        // Request microphone access
        try {
            this.userMediaStream = await navigator.mediaDevices.getUserMedia({ audio: true });
            
            // Send join message
            if (this.wsConnected) {
                this.ws.send(JSON.stringify({
                    type: 'join',
                    roomId: roomId
                }));
            }
            
            this._updateStatus(`🎙️ Joined room: ${roomId}`);
            
            if (this.onRoomJoin) {
                this.onRoomJoin(roomId);
            }
            
            console.log('[SpatialAudio] Joined room:', roomId);
        } catch (error) {
            console.error('[SpatialAudio] Failed to access microphone:', error);
            this._updateStatus('🔇 Microphone access denied');
        }
    }

    /**
     * Leave the current voice room.
     */
    leaveRoom() {
        if (!this.currentRoom) return;
        
        // Send leave message
        if (this.wsConnected) {
            this.ws.send(JSON.stringify({
                type: 'leave',
                roomId: this.currentRoom
            }));
        }
        
        // Stop user media stream
        if (this.userMediaStream) {
            this.userMediaStream.getTracks().forEach(track => track.stop());
            this.userMediaStream = null;
        }
        
        const room = this.currentRoom;
        this.currentRoom = null;
        
        this._updateStatus('🔇 Disconnected');
        
        if (this.onRoomLeave) {
            this.onRoomLeave(room);
        }
        
        console.log('[SpatialAudio] Left room:', room);
    }

    /**
     * Toggle room join/leave.
     */
    toggleRoom() {
        if (this.currentRoom) {
            this.leaveRoom();
        } else {
            // Default room ID based on game session
            const roomId = 'default-room';
            this.joinRoom(roomId);
        }
    }

    /**
     * Toggle local mute state.
     */
    toggleMute() {
        this.isMuted = !this.isMuted;
        
        // Update local stream
        if (this.userMediaStream) {
            this.userMediaStream.getAudioTracks().forEach(track => {
                track.enabled = !this.isMuted;
            });
        }
        
        // Send mute state
        if (this.wsConnected && this.currentRoom) {
            this.ws.send(JSON.stringify({
                type: 'mute',
                isMuted: this.isMuted
            }));
        }
        
        const btn = document.getElementById('audio-mute-btn');
        if (btn) {
            btn.textContent = this.isMuted ? '🔇 Unmute' : '🎤 Mute';
            btn.style.background = this.isMuted ? 'rgba(200, 50, 50, 0.3)' : 'rgba(50, 100, 200, 0.3)';
        }
        
        console.log('[SpatialAudio] Muted:', this.isMuted);
    }

    /**
     * Set the position of a player in 3D space.
     * @param {string} playerId - The player ID
     * @param {number} x - X position
     * @param {number} y - Y position
     * @param {number} z - Z position
     */
    setPlayerPosition(playerId, x, y, z) {
        this.playerPositions.set(playerId, { x, y, z });
        
        // Update spatial audio panner
        this._updatePannerPosition(playerId, x, y, z);
        
        // Update speaker indicator
        this._updateSpeakerIndicatorPosition(playerId, x, y, z);
    }

    /**
     * Set the local player position.
     * @param {number} x - X position
     * @param {number} y - Y position
     * @param {number} z - Z position
     */
    setLocalPosition(x, y, z) {
        this.localPosition = { x, y, z };
        
        // Update listener position
        if (this.audioContext) {
            const listener = this.audioContext.listener;
            if (listener.positionX) {
                listener.positionX.value = x;
                listener.positionY.value = y;
                listener.positionZ.value = z;
            }
        }
    }

    /**
     * Update the panner position for a player.
     * @param {string} playerId - The player ID
     * @param {number} x - X position
     * @param {number} y - Y position
     * @param {number} z - Z position
     */
    _updatePannerPosition(playerId, x, y, z) {
        const panner = this.pannerNodes.get(playerId);
        if (panner) {
            panner.positionX.value = x;
            panner.positionY.value = y;
            panner.positionZ.value = z;
        }
        
        // Update gain based on distance
        const distance = this.getDistance(playerId);
        const gain = this.gainNodes.get(playerId);
        if (gain) {
            // Proximity-based volume: max at 0 distance, min at 50 distance
            const maxDistance = 50;
            const volume = Math.max(0, 1 - distance / maxDistance);
            gain.gain.value = this.isMuted ? 0 : volume;
        }
    }

    /**
     * Update the speaker indicator position.
     * @param {string} playerId - The player ID
     * @param {number} x - X position
     * @param {number} y - Y position
     * @param {number} z - Z position
     */
    _updateSpeakerIndicatorPosition(playerId, x, y, z) {
        let indicator = this.speakerIndicators.get(playerId);
        
        if (!indicator) {
            indicator = this._createSpeakerIndicator(playerId);
            this.speakerIndicators.set(playerId, indicator);
            
            if (this.sceneRenderer?.scene) {
                this.sceneRenderer.scene.add(indicator);
            }
        }
        
        indicator.position.set(x, y + 1, z);
    }

    /**
     * Create a speaker indicator for a player.
     * @param {string} playerId - The player ID
     * @returns {THREE.Group} The indicator group
     */
    _createSpeakerIndicator(playerId) {
        const group = new THREE.Group();
        group.userData = { playerId, type: 'speaker_indicator' };
        
        // Microphone icon (simple cylinder)
        const micGeometry = new THREE.CylinderGeometry(0.1, 0.1, 0.3, 8);
        const micMaterial = new THREE.MeshPhongMaterial({
            color: 0x444444,
            emissive: 0x222222,
            emissiveIntensity: 0.2
        });
        const mic = new THREE.Mesh(micGeometry, micMaterial);
        group.add(mic);
        
        // Ring indicator
        const ringGeometry = new THREE.RingGeometry(0.15, 0.2, 16);
        const ringMaterial = new THREE.MeshPhongMaterial({
            color: 0x444444,
            emissive: 0x222222,
            emissiveIntensity: 0.2,
            transparent: true,
            opacity: 0.5,
            side: THREE.DoubleSide
        });
        const ring = new THREE.Mesh(ringGeometry, ringMaterial);
        ring.rotation.x = Math.PI / 2;
        group.add(ring);
        
        return group;
    }

    /**
     * Clean up audio for a player.
     * @param {string} playerId - The player ID
     */
    _cleanupPlayerAudio(playerId) {
        // Remove gain node
        const gain = this.gainNodes.get(playerId);
        if (gain) {
            gain.disconnect();
            this.gainNodes.delete(playerId);
        }
        
        // Remove panner node
        const panner = this.pannerNodes.get(playerId);
        if (panner) {
            panner.disconnect();
            this.pannerNodes.delete(playerId);
        }
        
        // Remove media stream
        const stream = this.mediaStreams.get(playerId);
        if (stream) {
            stream.getTracks().forEach(track => track.stop());
            this.mediaStreams.delete(playerId);
        }
        
        // Remove speaker indicator
        const indicator = this.speakerIndicators.get(playerId);
        if (indicator && this.sceneRenderer?.scene) {
            this.sceneRenderer.scene.remove(indicator);
            this.speakerIndicators.delete(playerId);
        }
    }

    /**
     * Update the status display.
     * @param {string} status - Status text
     */
    _updateStatus(status) {
        const el = document.getElementById('audio-status');
        if (el) {
            el.textContent = status;
        }
    }

    /**
     * Reconnect to WebSocket.
     */
    _reconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('[SpatialAudio] Max reconnection attempts reached');
            return;
        }
        
        this.reconnectAttempts++;
        const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
        
        console.log(`[SpatialAudio] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
        
        setTimeout(() => {
            this.connect();
        }, delay);
    }

    /**
     * Start the animation loop.
     */
    startAnimation() {
        const animate = () => {
            this.animationId = requestAnimationFrame(animate);
            
            const delta = this.clock.getDelta();
            const time = this.clock.getElapsedTime();
            
            // Animate speaker indicators
            this.speakerIndicators.forEach((group, playerId) => {
                const participant = this.participants.get(playerId);
                if (participant && participant.isSpeaking) {
                    // Pulse effect for speaking players
                    const scale = 1 + Math.sin(time * 5) * 0.2;
                    group.scale.setScalar(scale);
                } else {
                    group.scale.setScalar(1);
                }
                
                // Rotate slowly
                group.rotation.y += delta * 0.5;
            });
        };
        
        animate();
    }

    /**
     * Stop the animation loop.
     */
    stopAnimation() {
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }
    }

    /**
     * Set callbacks for events.
     * @param {Object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        if (callbacks.onSpeakerActivity) this.onSpeakerActivity = callbacks.onSpeakerActivity;
        if (callbacks.onRoomJoin) this.onRoomJoin = callbacks.onRoomJoin;
        if (callbacks.onRoomLeave) this.onRoomLeave = callbacks.onRoomLeave;
    }

    /**
     * Destroy the spatial audio module.
     */
    destroy() {
        this.stopAnimation();
        this.leaveRoom();
        
        // Disconnect WebSocket
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        
        // Clean up all player audio
        for (const playerId of this.playerPositions.keys()) {
            this._cleanupPlayerAudio(playerId);
        }
        
        // Close audio context
        if (this.audioContext) {
            this.audioContext.close();
            this.audioContext = null;
        }
        
        // Remove UI elements
        if (this.container && this.container.parentNode) {
            this.container.parentNode.removeChild(this.container);
        }
        
        // Clear data
        this.playerPositions.clear();
        this.participants.clear();
        this.gainNodes.clear();
        this.pannerNodes.clear();
        this.mediaStreams.clear();
        this.speakerIndicators.clear();
        
        console.log('[SpatialAudio] Destroyed');
    }
}

// Expose globally
window.SpatialAudio = SpatialAudio;

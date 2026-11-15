/**
 * PresenceManager — Multiplayer presence visualization in 3D space.
 *
 * Provides:
 * - WebSocket-based player position tracking
 * - Player avatar markers with customizable appearance and team colors
 * - Activity indicators (mining, building, exploring) as 3D effects
 * - Proximity-based chat visualization
 * - Player list overlay with online status and current location
 * - Spatial audio for voice chat proximity effects
 *
 * Usage:
 *   const presence = new PresenceManager(sceneRenderer, apiBaseUrl);
 *   await presence.connect();
 *   presence.destroy();
 */

class PresenceManager {
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // WebSocket
        this.ws = null;
        this.wsConnected = false;
        this.reconnectAttempts = 0;
        
        // Player data
        this.players = new Map(); // playerId -> player data
        this.playerMeshes = new Map(); // playerId -> THREE.Group
        this.localPlayerId = null;
        
        // Activity types
        this.activityTypes = {
            idle: { color: 0x888888, label: 'Idle' },
            mining: { color: 0xff8800, label: 'Mining', particleColor: 0xffaa00 },
            building: { color: 0x00ff00, label: 'Building', particleColor: 0x44ff44 },
            exploring: { color: 0x0088ff, label: 'Exploring', particleColor: 0x44aaff },
            combat: { color: 0xff0000, label: 'Combat', particleColor: 0xff4444 },
            trading: { color: 0xffff00, label: 'Trading', particleColor: 0xffff44 }
        };
        
        // Animation
        this.animationFrame = null;
        
        // Chat bubbles
        this.chatBubbles = new Map();
        
        // Callbacks
        this.onPlayerClick = null;
        this.onPlayerHover = null;
        this.onPlayerChat = null;
    }

    async connect() {
        if (this.wsConnected) return true;
        
        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : '';
            const wsProtocol = serverUrl.includes('https') ? 'wss' : 'ws';
            const wsHost = serverUrl.replace(/https?:\/\//, '');
            
            const playerId = localStorage.getItem('fps-player-id') || localStorage.getItem('player-id');
            const playerName = localStorage.getItem('fps-player-name') || 'Player';
            const sessionId = localStorage.getItem('fps-match-id') || localStorage.getItem('game-session-id');
            
            this.localPlayerId = playerId;
            
            const wsUrl = `${wsProtocol}://${wsHost}/ws/presence?sessionId=${sessionId}&playerId=${playerId}&playerName=${encodeURIComponent(playerName)}`;
            
            this.ws = new WebSocket(wsUrl);
            
            this.ws.onopen = () => {
                console.log('[PresenceManager] Connected');
                this.wsConnected = true;
                this.reconnectAttempts = 0;
            };
            
            this.ws.onmessage = (event) => {
                this._handleMessage(event.data);
            };
            
            this.ws.onclose = () => {
                this.wsConnected = false;
                this._attemptReconnect();
            };
            
            return new Promise((resolve) => {
                const timeout = setTimeout(() => resolve(false), 5000);
                this.ws.addEventListener('open', () => {
                    clearTimeout(timeout);
                    resolve(true);
                }, { once: true });
            });
        } catch (error) {
            console.error('[PresenceManager] Connection failed:', error);
            return false;
        }
    }

    _handleMessage(data) {
        try {
            const message = JSON.parse(data);
            
            switch (message.type) {
                case 'player_joined':
                    this._addPlayer(message.data);
                    break;
                case 'player_left':
                    this._removePlayer(message.data.playerId);
                    break;
                case 'player_moved':
                    this._updatePlayerPosition(message.data);
                    break;
                case 'player_activity':
                    this._updatePlayerActivity(message.data);
                    break;
                case 'player_chat':
                    this._showChatBubble(message.data);
                    if (this.onPlayerChat) {
                        this.onPlayerChat(message.data);
                    }
                    break;
                case 'player_list':
                    this._updatePlayerList(message.data);
                    break;
            }
        } catch (error) {
            console.error('[PresenceManager] Parse error:', error);
        }
    }

    _addPlayer(data) {
        this.players.set(data.playerId, data);
        
        if (data.playerId === this.localPlayerId) return;
        
        this._createPlayerMarker(data);
    }

    _removePlayer(playerId) {
        this.players.delete(playerId);
        
        const mesh = this.playerMeshes.get(playerId);
        if (mesh && this.sceneRenderer?.scene) {
            this.sceneRenderer.scene.remove(mesh);
        }
        this.playerMeshes.delete(playerId);
        
        // Remove chat bubble
        const bubble = this.chatBubbles.get(playerId);
        if (bubble && this.sceneRenderer?.scene) {
            this.sceneRenderer.scene.remove(bubble);
        }
        this.chatBubbles.delete(playerId);
    }

    _updatePlayerPosition(data) {
        const player = this.players.get(data.playerId);
        if (!player) return;
        
        player.position = data.position;
        
        const mesh = this.playerMeshes.get(data.playerId);
        if (mesh && data.position) {
            mesh.position.set(
                data.position.x || 0,
                data.position.y || 0,
                data.position.z || 0
            );
        }
    }

    _updatePlayerActivity(data) {
        const player = this.players.get(data.playerId);
        if (!player) return;
        
        player.activity = data.activity;
        
        const mesh = this.playerMeshes.get(data.playerId);
        if (mesh) {
            this._updateActivityIndicator(mesh, data.activity);
        }
    }

    _createPlayerMarker(data) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        
        const group = new THREE.Group();
        group.position.set(
            data.position?.x || 0,
            data.position?.y || 0,
            data.position?.z || 0
        );
        
        // Avatar body
        const bodyColor = data.teamColor || 0x4488ff;
        const bodyGeo = new THREE.CapsuleGeometry ? 
            new THREE.CapsuleGeometry(0.3, 0.8, 4, 8) :
            new THREE.CylinderGeometry(0.3, 0.3, 0.8, 8);
        const bodyMat = new THREE.MeshStandardMaterial({
            color: bodyColor,
            emissive: bodyColor,
            emissiveIntensity: 0.3
        });
        const body = new THREE.Mesh(bodyGeo, bodyMat);
        body.position.y = 0.4;
        group.add(body);
        
        // Head
        const headGeo = new THREE.SphereGeometry(0.25, 8, 8);
        const headMat = new THREE.MeshStandardMaterial({ color: 0xffccaa });
        const head = new THREE.Mesh(headGeo, headMat);
        head.position.y = 1.0;
        group.add(head);
        
        // Activity indicator
        if (data.activity) {
            this._createActivityIndicator(group, data.activity);
        }
        
        // Name label
        if (data.playerName) {
            const label = this._createLabel(data.playerName);
            if (label) {
                label.position.y = 1.8;
                group.add(label);
            }
        }
        
        group.userData = {
            interactive: true,
            id: data.playerId,
            name: data.playerName || 'Player',
            type: 'player',
            data: data
        };
        
        scene.add(group);
        this.playerMeshes.set(data.playerId, group);
        
        if (this.sceneRenderer.interactiveObjects) {
            this.sceneRenderer.interactiveObjects.push(group);
        }
    }

    _createActivityIndicator(group, activity) {
        const config = this.activityTypes[activity] || this.activityTypes.idle;
        
        // Ring around player
        const ringGeo = new THREE.TorusGeometry(0.6, 0.05, 8, 16);
        const ringMat = new THREE.MeshStandardMaterial({
            color: config.color,
            emissive: config.color,
            emissiveIntensity: 0.5,
            transparent: true,
            opacity: 0.7
        });
        const ring = new THREE.Mesh(ringGeo, ringMat);
        ring.rotation.x = Math.PI / 2;
        ring.position.y = 0.5;
        ring.userData = { type: 'activityRing' };
        group.add(ring);
        
        // Particles for certain activities
        if (config.particleColor) {
            this._createActivityParticles(group, config.particleColor, activity);
        }
    }

    _updateActivityIndicator(group, activity) {
        const config = this.activityTypes[activity] || this.activityTypes.idle;
        
        const ring = group.children.find(c => c.userData?.type === 'activityRing');
        if (ring) {
            ring.material.color.setHex(config.color);
            ring.material.emissive.setHex(config.color);
        }
    }

    _createActivityParticles(group, color, activity) {
        if (typeof THREE === 'undefined') return;
        
        const count = 20;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        
        for (let i = 0; i < count; i++) {
            const angle = (i / count) * Math.PI * 2;
            const radius = 0.5 + Math.random() * 0.3;
            positions[i * 3] = Math.cos(angle) * radius;
            positions[i * 3 + 1] = Math.random() * 1.5;
            positions[i * 3 + 2] = Math.sin(angle) * radius;
        }
        
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        
        const material = new THREE.PointsMaterial({
            color: color,
            size: 0.1,
            transparent: true,
            opacity: 0.6
        });
        
        const particles = new THREE.Points(geometry, material);
        particles.userData = { type: 'activityParticles', activity };
        group.add(particles);
    }

    _showChatBubble(data) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        const playerId = data.playerId;
        
        // Remove existing bubble
        const existing = this.chatBubbles.get(playerId);
        if (existing) {
            scene.remove(existing);
        }
        
        // Create bubble
        const canvas = document.createElement('canvas');
        canvas.width = 512;
        canvas.height = 128;
        const ctx = canvas.getContext('2d');
        
        // Background
        ctx.fillStyle = 'rgba(0, 0, 0, 0.8)';
        ctx.beginPath();
        ctx.roundRect(0, 0, 512, 128, 20);
        ctx.fill();
        
        // Border
        ctx.strokeStyle = data.teamColor ? `#${data.teamColor.toString(16).padStart(6, '0')}` : '#4488ff';
        ctx.lineWidth = 2;
        ctx.stroke();
        
        // Name
        ctx.font = 'Bold 16px Arial';
        ctx.fillStyle = '#4488ff';
        ctx.textAlign = 'left';
        ctx.fillText(data.playerName || 'Player', 20, 25);
        
        // Message
        ctx.font = '14px Arial';
        ctx.fillStyle = '#ffffff';
        const words = (data.message || '').split(' ');
        let line = '';
        let y = 50;
        for (const word of words) {
            const test = line + word + ' ';
            if (ctx.measureText(test).width > 480) {
                ctx.fillText(line, 20, y);
                line = word + ' ';
                y += 20;
            } else {
                line = test;
            }
        }
        ctx.fillText(line, 20, y);
        
        const texture = new THREE.CanvasTexture(canvas);
        const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
        const sprite = new THREE.Sprite(spriteMat);
        sprite.scale.set(4, 1, 1);
        
        // Position above player
        const mesh = this.playerMeshes.get(playerId);
        if (mesh) {
            sprite.position.copy(mesh.position);
            sprite.position.y += 2.5;
        } else if (data.position) {
            sprite.position.set(data.position.x, data.position.y + 2.5, data.position.z);
        }
        
        scene.add(sprite);
        this.chatBubbles.set(playerId, sprite);
        
        // Auto-remove after 5 seconds
        setTimeout(() => {
            if (this.chatBubbles.has(playerId)) {
                scene.remove(sprite);
                this.chatBubbles.delete(playerId);
                texture.dispose();
                spriteMat.dispose();
            }
        }, 5000);
    }

    _createLabel(text) {
        if (typeof THREE === 'undefined') return null;
        
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = 256;
        canvas.height = 64;
        
        ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';
        ctx.beginPath();
        ctx.roundRect(0, 0, 256, 64, 10);
        ctx.fill();
        
        ctx.font = 'Bold 18px Arial';
        ctx.fillStyle = '#ffffff';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        
        const maxLen = 18;
        const displayText = text.length > maxLen ? text.substring(0, maxLen) + '...' : text;
        ctx.fillText(displayText, 128, 32);
        
        const texture = new THREE.CanvasTexture(canvas);
        const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
        const sprite = new THREE.Sprite(spriteMat);
        sprite.scale.set(2, 0.5, 1);
        
        return sprite;
    }

    _updatePlayerList(data) {
        // Update internal player list
        if (Array.isArray(data)) {
            data.forEach(player => {
                this.players.set(player.playerId, player);
            });
        }
    }

    setCallbacks(onPlayerClick, onPlayerHover, onPlayerChat) {
        this.onPlayerClick = onPlayerClick;
        this.onPlayerHover = onPlayerHover;
        this.onPlayerChat = onPlayerChat;
    }

    enableInteraction(onClick, onHover) {
        if (!this.sceneRenderer) return;
        
        this.onPlayerClick = onClick;
        this.onPlayerHover = onHover;
    }

    getOnlinePlayers() {
        return Array.from(this.players.values());
    }

    getPlayerCount() {
        return this.players.size;
    }

    startAnimation() {
        const animate = () => {
            this.animationFrame = requestAnimationFrame(animate);
            
            // Rotate activity rings
            this.playerMeshes.forEach(group => {
                const ring = group.children.find(c => c.userData?.type === 'activityRing');
                if (ring) {
                    ring.rotation.z += 0.02;
                }
                
                // Animate particles
                const particles = group.children.find(c => c.userData?.type === 'activityParticles');
                if (particles) {
                    particles.rotation.y += 0.01;
                    const positions = particles.geometry.attributes.position.array;
                    for (let i = 0; i < positions.length; i += 3) {
                        positions[i + 1] += 0.01;
                        if (positions[i + 1] > 1.5) {
                            positions[i + 1] = 0;
                        }
                    }
                    particles.geometry.attributes.position.needsUpdate = true;
                }
                
                // Make labels face camera
                if (this.sceneRenderer?.camera) {
                    group.children.forEach(child => {
                        if (child.isSprite) {
                            child.lookAt(this.sceneRenderer.camera.position);
                        }
                    });
                }
            });
            
            // Make chat bubbles face camera
            this.chatBubbles.forEach(bubble => {
                if (this.sceneRenderer?.camera) {
                    bubble.lookAt(this.sceneRenderer.camera.position);
                }
            });
        };
        animate();
    }

    stopAnimation() {
        if (this.animationFrame) {
            cancelAnimationFrame(this.animationFrame);
            this.animationFrame = null;
        }
    }

    _attemptReconnect() {
        if (this.reconnectAttempts >= 10) return;
        this.reconnectAttempts++;
        const delay = 1000 * Math.pow(2, this.reconnectAttempts - 1);
        setTimeout(() => this.connect(), delay);
    }

    /**
     * Send a chat message to other players.
     * @param {string} message - Chat message text
     */
    sendChat(message) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            console.warn('[PresenceManager] Not connected');
            return;
        }
        this.ws.send(JSON.stringify({
            type: 'chat',
            message
        }));
    }

    /**
     * Update local player position.
     * @param {number} x - X coordinate
     * @param {number} y - Y coordinate
     * @param {number} z - Z coordinate
     */
    updatePosition(x, y, z) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            console.warn('[PresenceManager] Not connected');
            return;
        }
        this.ws.send(JSON.stringify({
            type: 'move',
            x,
            y,
            z
        }));
    }

    /**
     * Update local player activity.
     * @param {string} activity - Activity type (idle, mining, building, exploring, combat, trading)
     */
    updateActivity(activity) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            console.warn('[PresenceManager] Not connected');
            return;
        }
        this.ws.send(JSON.stringify({
            type: 'activity',
            activity
        }));
    }

    disconnect() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        this.wsConnected = false;
    }

    destroy() {
        this.stopAnimation();
        this.disconnect();
        
        if (this.sceneRenderer?.scene) {
            this.playerMeshes.forEach(mesh => this.sceneRenderer.scene.remove(mesh));
            this.chatBubbles.forEach(bubble => this.sceneRenderer.scene.remove(bubble));
        }
        
        this.players.clear();
        this.playerMeshes.clear();
        this.chatBubbles.clear();
        
        console.log('[PresenceManager] Destroyed');
    }
}

window.PresenceManager = PresenceManager;

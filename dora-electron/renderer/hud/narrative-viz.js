/**
 * NarrativeViz — Visualize AI-generated narrative events as immersive 3D effects.
 *
 * Provides:
 * - Event-specific visual effects (battles, discoveries, diplomatic meetings, anomalies)
 * - Spatial audio for narrative events with directional sound
 * - Narrative timeline overlay showing past and upcoming events
 * - Interactive event markers that trigger narrative text and choices
 * - Support for all 10 narrative types and 8 styles from GameMasterService
 * - Portal effects for narrative transitions between locations
 *
 * Usage:
 *   const narrative = new NarrativeViz(sceneRenderer, apiBaseUrl);
 *   await narrative.connect();
 *   narrative.destroy();
 */

class NarrativeViz {
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // WebSocket
        this.ws = null;
        this.wsConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.pingInterval = null;
        
        // Event data
        this.events = new Map();
        this.eventMeshes = new Map();
        this.timeline = [];
        
        // Narrative types and their visual styles
        this.narrativeTypes = {
            battle: {
                color: 0xff4444,
                particleColor: 0xff8800,
                effect: 'explosion',
                label: 'Battle'
            },
            discovery: {
                color: 0x44ff44,
                particleColor: 0x88ff88,
                effect: 'sparkle',
                label: 'Discovery'
            },
            diplomatic: {
                color: 0x4488ff,
                particleColor: 0x88aaff,
                effect: 'portal',
                label: 'Diplomatic'
            },
            anomaly: {
                color: 0xaa44ff,
                particleColor: 0xcc88ff,
                effect: 'distortion',
                label: 'Anomaly'
            },
            exploration: {
                color: 0xffaa00,
                particleColor: 0xffcc44,
                effect: 'beacon',
                label: 'Exploration'
            },
            trade: {
                color: 0x00ffaa,
                particleColor: 0x44ffcc,
                effect: 'flow',
                label: 'Trade'
            },
            crisis: {
                color: 0xff0000,
                particleColor: 0xff4444,
                effect: 'alert',
                label: 'Crisis'
            },
            victory: {
                color: 0xffff00,
                particleColor: 0xffff88,
                effect: 'fireworks',
                label: 'Victory'
            },
            mystery: {
                color: 0x8844ff,
                particleColor: 0xaa88ff,
                effect: 'fog',
                label: 'Mystery'
            },
            personal: {
                color: 0xff88aa,
                particleColor: 0xffaacc,
                effect: 'glow',
                label: 'Personal'
            }
        };
        
        // Narrative styles
        this.narrativeStyles = {
            dramatic: { intensity: 1.5, duration: 3000 },
            subtle: { intensity: 0.5, duration: 1500 },
            epic: { intensity: 2.0, duration: 5000 },
            mysterious: { intensity: 0.8, duration: 4000 },
            urgent: { intensity: 1.8, duration: 2000 },
            peaceful: { intensity: 0.6, duration: 3500 },
            chaotic: { intensity: 1.6, duration: 2500 },
            reflective: { intensity: 0.7, duration: 4500 }
        };
        
        // Audio
        this.audioContext = null;
        this.spatialAudioEnabled = false;
        
        // Animation
        this.animationFrame = null;
        
        // Callbacks
        this.onEventClick = null;
    }

    async connect() {
        if (this.wsConnected) return true;
        
        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : '';
            const wsProtocol = serverUrl.includes('https') ? 'wss' : 'ws';
            const wsHost = serverUrl.replace(/https?:\/\//, '');
            
            const sessionId = localStorage.getItem('fps-match-id') || localStorage.getItem('game-session-id');
            const playerId = localStorage.getItem('fps-player-id') || localStorage.getItem('player-id');
            
            const wsUrl = `${wsProtocol}://${wsHost}/ws/narrative?sessionId=${sessionId}&playerId=${playerId}`;
            
            this.ws = new WebSocket(wsUrl);
            
            this.ws.onopen = () => {
                console.log('[NarrativeViz] Connected');
                this.wsConnected = true;
                this.reconnectAttempts = 0;
                this._startPing();
            };
            
            this.ws.onmessage = (event) => {
                this._handleMessage(event.data);
            };
            
            this.ws.onclose = () => {
                console.log('[NarrativeViz] Disconnected');
                this.wsConnected = false;
                this._stopPing();
                this._attemptReconnect();
            };
            
            this.ws.onerror = (error) => {
                console.error('[NarrativeViz] WebSocket error:', error);
            };
            
            return new Promise((resolve) => {
                const timeout = setTimeout(() => resolve(false), 5000);
                this.ws.addEventListener('open', () => {
                    clearTimeout(timeout);
                    resolve(true);
                }, { once: true });
            });
        } catch (error) {
            console.error('[NarrativeViz] Connection failed:', error);
            return false;
        }
    }

    _handleMessage(data) {
        try {
            const message = JSON.parse(data);
            
            switch (message.type) {
                case 'narrative_event':
                    this._handleNarrativeEvent(message.data);
                    break;
                case 'event_update':
                    this._handleEventUpdate(message.data);
                    break;
                case 'event_resolved':
                    this._handleEventResolved(message.data.eventId);
                    break;
                case 'timeline_update':
                    this._handleTimelineUpdate(message.data);
                    break;
                case 'pong':
                    // Server responded to ping - connection is alive
                    break;
            }
        } catch (error) {
            console.error('[NarrativeViz] Parse error:', error);
        }
    }

    _handleNarrativeEvent(data) {
        const eventId = data.eventId || `event_${Date.now()}`;
        this.events.set(eventId, data);
        
        // Add to timeline
        this.timeline.push({
            id: eventId,
            timestamp: Date.now(),
            type: data.type,
            title: data.title || 'Unknown Event',
            description: data.description || ''
        });
        
        // Keep timeline limited
        if (this.timeline.length > 50) {
            this.timeline.shift();
        }
        
        // Create visual effect
        this._createEventEffect(eventId, data);
        
        // Play sound
        if (this.spatialAudioEnabled) {
            this._playEventSound(data);
        }
        
        console.log(`[NarrativeViz] Event: ${data.title || data.type}`);
    }

    _handleEventUpdate(data) {
        const eventId = data.eventId;
        const event = this.events.get(eventId);
        if (event) {
            this.events.set(eventId, { ...event, ...data });
        }
    }

    _handleEventResolved(eventId) {
        const mesh = this.eventMeshes.get(eventId);
        if (mesh && this.sceneRenderer?.scene) {
            // Fade out effect
            this._fadeOutEffect(mesh);
        }
        
        setTimeout(() => {
            this.events.delete(eventId);
            this.eventMeshes.delete(eventId);
        }, 2000);
    }

    _handleTimelineUpdate(data) {
        if (Array.isArray(data)) {
            this.timeline = data;
        }
    }

    _createEventEffect(eventId, data) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        const typeConfig = this.narrativeTypes[data.type] || this.narrativeTypes.discovery;
        const styleConfig = this.narrativeStyles[data.style] || this.narrativeStyles.dramatic;
        
        const group = new THREE.Group();
        
        const position = data.position || { x: 0, y: 0, z: 0 };
        group.position.set(position.x, position.y, position.z);
        
        // Create effect based on type
        switch (typeConfig.effect) {
            case 'explosion':
                this._createExplosionEffect(group, typeConfig, styleConfig);
                break;
            case 'sparkle':
                this._createSparkleEffect(group, typeConfig, styleConfig);
                break;
            case 'portal':
                this._createPortalEffect(group, typeConfig, styleConfig);
                break;
            case 'distortion':
                this._createDistortionEffect(group, typeConfig, styleConfig);
                break;
            case 'beacon':
                this._createBeaconEffect(group, typeConfig, styleConfig);
                break;
            case 'flow':
                this._createFlowEffect(group, typeConfig, styleConfig);
                break;
            case 'alert':
                this._createAlertEffect(group, typeConfig, styleConfig);
                break;
            case 'fireworks':
                this._createFireworksEffect(group, typeConfig, styleConfig);
                break;
            case 'fog':
                this._createFogEffect(group, typeConfig, styleConfig);
                break;
            case 'glow':
                this._createGlowEffect(group, typeConfig, styleConfig);
                break;
            default:
                this._createDefaultEffect(group, typeConfig, styleConfig);
        }
        
        // Add event marker
        const marker = this._createEventMarker(data);
        if (marker) {
            marker.position.y = 3;
            group.add(marker);
        }
        
        group.userData = {
            interactive: true,
            id: eventId,
            name: data.title || 'Event',
            type: 'narrative_event',
            data: data
        };
        
        scene.add(group);
        this.eventMeshes.set(eventId, group);
        
        if (this.sceneRenderer.interactiveObjects) {
            this.sceneRenderer.interactiveObjects.push(group);
        }
        
        // Auto-remove after duration
        setTimeout(() => {
            this._handleEventResolved(eventId);
        }, styleConfig.duration);
    }

    _createExplosionEffect(group, config, style) {
        const count = Math.floor(50 * style.intensity);
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        const velocities = [];
        
        for (let i = 0; i < count; i++) {
            positions[i * 3] = 0;
            positions[i * 3 + 1] = 0;
            positions[i * 3 + 2] = 0;
            
            velocities.push({
                x: (Math.random() - 0.5) * 0.2 * style.intensity,
                y: (Math.random() - 0.5) * 0.2 * style.intensity,
                z: (Math.random() - 0.5) * 0.2 * style.intensity
            });
        }
        
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        
        const material = new THREE.PointsMaterial({
            color: config.particleColor,
            size: 0.2 * style.intensity,
            transparent: true,
            opacity: 0.8
        });
        
        const particles = new THREE.Points(geometry, material);
        particles.userData = { type: 'explosion', velocities };
        group.add(particles);
    }

    _createSparkleEffect(group, config, style) {
        const count = Math.floor(30 * style.intensity);
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        
        for (let i = 0; i < count; i++) {
            const angle = Math.random() * Math.PI * 2;
            const radius = Math.random() * 2 * style.intensity;
            positions[i * 3] = Math.cos(angle) * radius;
            positions[i * 3 + 1] = Math.random() * 2;
            positions[i * 3 + 2] = Math.sin(angle) * radius;
        }
        
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        
        const material = new THREE.PointsMaterial({
            color: config.particleColor,
            size: 0.15,
            transparent: true,
            opacity: 0.9
        });
        
        const particles = new THREE.Points(geometry, material);
        particles.userData = { type: 'sparkle' };
        group.add(particles);
    }

    _createPortalEffect(group, config, style) {
        const ringGeo = new THREE.TorusGeometry(1.5 * style.intensity, 0.1, 16, 32);
        const ringMat = new THREE.MeshStandardMaterial({
            color: config.color,
            emissive: config.color,
            emissiveIntensity: 0.8,
            transparent: true,
            opacity: 0.7
        });
        const ring = new THREE.Mesh(ringGeo, ringMat);
        ring.userData = { type: 'portal' };
        group.add(ring);
        
        // Inner glow
        const glowGeo = new THREE.CircleGeometry(1.5 * style.intensity, 32);
        const glowMat = new THREE.MeshStandardMaterial({
            color: config.color,
            emissive: config.color,
            emissiveIntensity: 0.3,
            transparent: true,
            opacity: 0.2,
            side: THREE.DoubleSide
        });
        const glow = new THREE.Mesh(glowGeo, glowMat);
        group.add(glow);
    }

    _createDistortionEffect(group, config, style) {
        const count = 40;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        
        for (let i = 0; i < count; i++) {
            const angle = (i / count) * Math.PI * 2;
            const radius = 1 + Math.random() * style.intensity;
            positions[i * 3] = Math.cos(angle) * radius;
            positions[i * 3 + 1] = (Math.random() - 0.5) * 2;
            positions[i * 3 + 2] = Math.sin(angle) * radius;
        }
        
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        
        const material = new THREE.PointsMaterial({
            color: config.particleColor,
            size: 0.2,
            transparent: true,
            opacity: 0.6
        });
        
        const particles = new THREE.Points(geometry, material);
        particles.userData = { type: 'distortion' };
        group.add(particles);
    }

    _createBeaconEffect(group, config, style) {
        const beamGeo = new THREE.CylinderGeometry(0.05, 0.5, 5 * style.intensity, 8);
        const beamMat = new THREE.MeshStandardMaterial({
            color: config.color,
            emissive: config.color,
            emissiveIntensity: 0.6,
            transparent: true,
            opacity: 0.5
        });
        const beam = new THREE.Mesh(beamGeo, beamMat);
        beam.position.y = 2.5 * style.intensity;
        group.add(beam);
    }

    _createFlowEffect(group, config, style) {
        const count = 20;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        
        for (let i = 0; i < count; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 3;
            positions[i * 3 + 1] = Math.random() * 2;
            positions[i * 3 + 2] = (Math.random() - 0.5) * 3;
        }
        
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        
        const material = new THREE.PointsMaterial({
            color: config.particleColor,
            size: 0.2,
            transparent: true,
            opacity: 0.7
        });
        
        const particles = new THREE.Points(geometry, material);
        particles.userData = { type: 'flow' };
        group.add(particles);
    }

    _createAlertEffect(group, config, style) {
        // Pulsing sphere
        const sphereGeo = new THREE.SphereGeometry(1 * style.intensity, 16, 16);
        const sphereMat = new THREE.MeshStandardMaterial({
            color: config.color,
            emissive: config.color,
            emissiveIntensity: 0.8,
            transparent: true,
            opacity: 0.4
        });
        const sphere = new THREE.Mesh(sphereGeo, sphereMat);
        sphere.userData = { type: 'alert' };
        group.add(sphere);
    }

    _createFireworksEffect(group, config, style) {
        const count = Math.floor(80 * style.intensity);
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        const velocities = [];
        
        for (let i = 0; i < count; i++) {
            positions[i * 3] = 0;
            positions[i * 3 + 1] = 0;
            positions[i * 3 + 2] = 0;
            
            const theta = Math.random() * Math.PI * 2;
            const phi = Math.random() * Math.PI;
            const speed = 0.1 + Math.random() * 0.2 * style.intensity;
            
            velocities.push({
                x: speed * Math.sin(phi) * Math.cos(theta),
                y: speed * Math.cos(phi),
                z: speed * Math.sin(phi) * Math.sin(theta)
            });
        }
        
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        
        const material = new THREE.PointsMaterial({
            color: config.particleColor,
            size: 0.15,
            transparent: true,
            opacity: 0.9
        });
        
        const particles = new THREE.Points(geometry, material);
        particles.userData = { type: 'fireworks', velocities };
        group.add(particles);
    }

    _createFogEffect(group, config, style) {
        const count = 50;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        
        for (let i = 0; i < count; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 4 * style.intensity;
            positions[i * 3 + 1] = Math.random() * 3;
            positions[i * 3 + 2] = (Math.random() - 0.5) * 4 * style.intensity;
        }
        
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        
        const material = new THREE.PointsMaterial({
            color: config.particleColor,
            size: 0.3,
            transparent: true,
            opacity: 0.3
        });
        
        const particles = new THREE.Points(geometry, material);
        particles.userData = { type: 'fog' };
        group.add(particles);
    }

    _createGlowEffect(group, config, style) {
        const glowGeo = new THREE.SphereGeometry(0.5 * style.intensity, 16, 16);
        const glowMat = new THREE.MeshStandardMaterial({
            color: config.color,
            emissive: config.color,
            emissiveIntensity: 0.6,
            transparent: true,
            opacity: 0.5
        });
        const glow = new THREE.Mesh(glowGeo, glowMat);
        glow.userData = { type: 'glow' };
        group.add(glow);
    }

    _createDefaultEffect(group, config, style) {
        const sphereGeo = new THREE.SphereGeometry(0.5, 16, 16);
        const sphereMat = new THREE.MeshStandardMaterial({
            color: config.color,
            emissive: config.color,
            emissiveIntensity: 0.5,
            transparent: true,
            opacity: 0.6
        });
        const sphere = new THREE.Mesh(sphereGeo, sphereMat);
        group.add(sphere);
    }

    _createEventMarker(data) {
        if (typeof THREE === 'undefined') return null;
        
        const canvas = document.createElement('canvas');
        canvas.width = 512;
        canvas.height = 128;
        const ctx = canvas.getContext('2d');
        
        // Background
        ctx.fillStyle = 'rgba(0, 0, 0, 0.85)';
        ctx.beginPath();
        ctx.roundRect(0, 0, 512, 128, 15);
        ctx.fill();
        
        // Border
        const typeConfig = this.narrativeTypes[data.type] || this.narrativeTypes.discovery;
        const color = `#${typeConfig.color.toString(16).padStart(6, '0')}`;
        ctx.strokeStyle = color;
        ctx.lineWidth = 3;
        ctx.stroke();
        
        // Title
        ctx.font = 'Bold 20px Arial';
        ctx.fillStyle = color;
        ctx.textAlign = 'center';
        ctx.fillText(data.title || 'Event', 256, 30);
        
        // Description
        ctx.font = '14px Arial';
        ctx.fillStyle = '#cccccc';
        const desc = data.description || '';
        const words = desc.split(' ');
        let line = '';
        let y = 55;
        for (const word of words) {
            const test = line + word + ' ';
            if (ctx.measureText(test).width > 480) {
                ctx.fillText(line.trim(), 256, y);
                line = word + ' ';
                y += 18;
                if (y > 100) break;
            } else {
                line = test;
            }
        }
        if (line.trim()) ctx.fillText(line.trim(), 256, y);
        
        const texture = new THREE.CanvasTexture(canvas);
        const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
        const sprite = new THREE.Sprite(spriteMat);
        sprite.scale.set(5, 1.25, 1);
        
        return sprite;
    }

    _fadeOutEffect(mesh) {
        mesh.traverse(child => {
            if (child.isMesh && child.material) {
                child.material.transparent = true;
                const fade = () => {
                    child.material.opacity -= 0.02;
                    if (child.material.opacity > 0) {
                        requestAnimationFrame(fade);
                    }
                };
                fade();
            }
            if (child.isPoints && child.material) {
                child.material.transparent = true;
                const fade = () => {
                    child.material.opacity -= 0.02;
                    if (child.material.opacity > 0) {
                        requestAnimationFrame(fade);
                    }
                };
                fade();
            }
        });
        
        // Remove from scene after fade
        setTimeout(() => {
            if (this.sceneRenderer?.scene) {
                this.sceneRenderer.scene.remove(mesh);
            }
        }, 1000);
    }

    enableSpatialAudio() {
        try {
            this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
            this.spatialAudioEnabled = true;
        } catch (error) {
            console.warn('[NarrativeViz] Spatial audio not available:', error);
        }
    }

    _playEventSound(data) {
        if (!this.audioContext) return;
        
        try {
            const typeConfig = this.narrativeTypes[data.type] || this.narrativeTypes.discovery;
            
            const oscillator = this.audioContext.createOscillator();
            const gainNode = this.audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(this.audioContext.destination);
            
            // Different frequencies for different event types
            const frequencies = {
                battle: 200,
                discovery: 600,
                diplomatic: 400,
                anomaly: 300,
                exploration: 500,
                trade: 450,
                crisis: 150,
                victory: 700,
                mystery: 350,
                personal: 550
            };
            
            oscillator.frequency.value = frequencies[data.type] || 400;
            oscillator.type = 'sine';
            
            gainNode.gain.setValueAtTime(0.2, this.audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, this.audioContext.currentTime + 1);
            
            oscillator.start(this.audioContext.currentTime);
            oscillator.stop(this.audioContext.currentTime + 1);
        } catch (error) {
            console.warn('[NarrativeViz] Sound error:', error);
        }
    }

    setCallbacks(onEventClick) {
        this.onEventClick = onEventClick;
    }

    getTimeline() {
        return this.timeline;
    }

    getActiveEvents() {
        return Array.from(this.events.values());
    }

    startAnimation() {
        const animate = () => {
            this.animationFrame = requestAnimationFrame(animate);
            
            this.eventMeshes.forEach(group => {
                // Animate explosion particles
                group.children.forEach(child => {
                    if (child.isPoints && child.userData?.type === 'explosion') {
                        const positions = child.geometry.attributes.position.array;
                        const velocities = child.userData.velocities;
                        for (let i = 0; i < velocities.length; i++) {
                            positions[i * 3] += velocities[i].x;
                            positions[i * 3 + 1] += velocities[i].y;
                            positions[i * 3 + 2] += velocities[i].z;
                        }
                        child.geometry.attributes.position.needsUpdate = true;
                    }
                    
                    // Animate fireworks
                    if (child.isPoints && child.userData?.type === 'fireworks') {
                        const positions = child.geometry.attributes.position.array;
                        const velocities = child.userData.velocities;
                        for (let i = 0; i < velocities.length; i++) {
                            positions[i * 3] += velocities[i].x;
                            positions[i * 3 + 1] += velocities[i].y - 0.002;
                            positions[i * 3 + 2] += velocities[i].z;
                        }
                        child.geometry.attributes.position.needsUpdate = true;
                    }
                    
                    // Rotate portal
                    if (child.isMesh && child.userData?.type === 'portal') {
                        child.rotation.z += 0.02;
                    }
                    
                    // Pulse alert
                    if (child.isMesh && child.userData?.type === 'alert') {
                        const scale = 1 + Math.sin(Date.now() * 0.005) * 0.2;
                        child.scale.set(scale, scale, scale);
                    }
                    
                    // Pulse glow
                    if (child.isMesh && child.userData?.type === 'glow') {
                        const scale = 1 + Math.sin(Date.now() * 0.003) * 0.15;
                        child.scale.set(scale, scale, scale);
                    }
                    
                    // Make labels face camera
                    if (child.isSprite && this.sceneRenderer?.camera) {
                        child.lookAt(this.sceneRenderer.camera.position);
                    }
                });
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

    /**
     * Acknowledge a narrative event.
     * @param {string} eventId - Event ID to acknowledge
     */
    acknowledgeEvent(eventId) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            console.warn('[NarrativeViz] Not connected');
            return;
        }
        this.ws.send(JSON.stringify({
            type: 'acknowledge',
            eventId
        }));
        console.log(`[NarrativeViz] Acknowledged event: ${eventId}`);
    }

    /**
     * Choose a narrative option.
     * @param {string} eventId - Event ID
     * @param {string} choiceId - Choice ID selected
     */
    chooseOption(eventId, choiceId) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
            console.warn('[NarrativeViz] Not connected');
            return;
        }
        this.ws.send(JSON.stringify({
            type: 'choose',
            eventId,
            choiceId
        }));
        console.log(`[NarrativeViz] Chose option ${choiceId} for event: ${eventId}`);
    }

    /**
     * Start ping keepalive interval.
     */
    _startPing() {
        this._stopPing();
        this.pingInterval = setInterval(() => {
            if (this.ws && this.ws.readyState === WebSocket.OPEN) {
                this.ws.send(JSON.stringify({ type: 'ping' }));
            }
        }, 30000);
    }

    /**
     * Stop ping keepalive interval.
     */
    _stopPing() {
        if (this.pingInterval) {
            clearInterval(this.pingInterval);
            this.pingInterval = null;
        }
    }

    /**
     * Attempt to reconnect to WebSocket.
     */
    _attemptReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('[NarrativeViz] Max reconnect attempts reached');
            return;
        }
        this.reconnectAttempts++;
        const delay = 1000 * Math.pow(2, this.reconnectAttempts - 1);
        console.log(`[NarrativeViz] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
        setTimeout(() => this.connect(), delay);
    }

    disconnect() {
        this._stopPing();
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
            this.eventMeshes.forEach(mesh => this.sceneRenderer.scene.remove(mesh));
        }
        
        this.events.clear();
        this.eventMeshes.clear();
        this.timeline = [];
        
        if (this.audioContext) {
            this.audioContext.close();
            this.audioContext = null;
        }
        
        console.log('[NarrativeViz] Destroyed');
    }
}

window.NarrativeViz = NarrativeViz;

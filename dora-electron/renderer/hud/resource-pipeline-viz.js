/**
 * ResourcePipelineViz — Visualize resource pipeline as animated 3D flows.
 *
 * Provides:
 * - Animated transport routes showing resource movement between systems
 * - Mining operation visualization with particle effects
 * - Processing facility indicators with production rate displays
 * - Resource flow statistics overlay
 * - ModuleType and RefinedResourceType visualization
 *
 * Usage:
 *   const pipeline = new ResourcePipelineViz(sceneRenderer, apiBaseUrl);
 *   await pipeline.loadPipeline(galaxyId);
 *   pipeline.destroy();
 */

class ResourcePipelineViz {
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // WebSocket
        this.ws = null;
        this.wsConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.pingInterval = null;
        this.currentGalaxyId = null;
        
        // Pipeline data
        this.pipelineData = null;
        this.routes = new Map();
        this.miningOps = new Map();
        this.processingFacilities = new Map();
        
        // Rendered objects
        this.routeLines = new Map();
        this.transportParticles = new Map();
        this.miningEffects = new Map();
        this.facilityMarkers = new Map();
        
        // Resource type colors
        this.resourceColors = {
            iron: 0x888888,
            copper: 0xcc8844,
            gold: 0xffcc00,
            titanium: 0x8888cc,
            uranium: 0x88ff00,
            water: 0x4488ff,
            hydrogen: 0xaaccff,
            helium: 0xffaacc,
            crystals: 0xcc44ff,
            organic: 0x44cc44
        };
        
        // Animation
        this.animationFrame = null;
        
        // Callbacks
        this.onRouteUpdate = null;
        this.onMiningUpdate = null;
        this.onFacilityUpdate = null;
        this.onStatisticsUpdate = null;
    }

    /**
     * Connect to WebSocket for real-time pipeline updates.
     * @param {string} galaxyId - Galaxy ID to subscribe to
     * @returns {Promise<boolean>} Whether connection was successful
     */
    async connect(galaxyId) {
        this.currentGalaxyId = galaxyId;
        
        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';
            const wsUrl = `${serverUrl.replace('http://', 'ws://').replace('https://', 'wss://')}/ws/resource-pipeline?galaxyId=${encodeURIComponent(galaxyId)}`;
            
            console.log('[ResourcePipelineViz] Connecting to WebSocket:', wsUrl);
            
            return new Promise((resolve) => {
                this.ws = new WebSocket(wsUrl);
                
                this.ws.onopen = () => {
                    console.log('[ResourcePipelineViz] WebSocket connected');
                    this.wsConnected = true;
                    this.reconnectAttempts = 0;
                    this._startPing();
                    resolve(true);
                };
                
                this.ws.onmessage = (event) => {
                    try {
                        const message = JSON.parse(event.data);
                        this._handleWebSocketMessage(message);
                    } catch (error) {
                        console.error('[ResourcePipelineViz] Parse error:', error);
                    }
                };
                
                this.ws.onclose = () => {
                    console.log('[ResourcePipelineViz] WebSocket disconnected');
                    this.wsConnected = false;
                    this._stopPing();
                    this._attemptReconnect();
                };
                
                this.ws.onerror = (error) => {
                    console.error('[ResourcePipelineViz] WebSocket error:', error);
                };
                
                // Timeout
                setTimeout(() => {
                    if (!this.wsConnected) {
                        console.warn('[ResourcePipelineViz] WebSocket connection timeout, falling back to REST');
                        resolve(false);
                    }
                }, 5000);
            });
        } catch (error) {
            console.error('[ResourcePipelineViz] WebSocket connection failed:', error);
            return false;
        }
    }

    /**
     * Handle incoming WebSocket messages.
     */
    _handleWebSocketMessage(message) {
        switch (message.type) {
            case 'pipeline_snapshot':
                this._handlePipelineSnapshot(message.data);
                break;
            case 'route_update':
                this._handleRouteUpdate(message.data);
                break;
            case 'mining_update':
                this._handleMiningUpdate(message.data);
                break;
            case 'facility_update':
                this._handleFacilityUpdate(message.data);
                break;
            case 'statistics_update':
                this._handleStatisticsUpdate(message.data);
                break;
            case 'pong':
                // Server responded to ping - connection is alive
                break;
            default:
                console.warn('[ResourcePipelineViz] Unknown message type:', message.type);
        }
    }

    /**
     * Handle full pipeline snapshot.
     */
    _handlePipelineSnapshot(data) {
        this.pipelineData = data;
        this._clearAll();
        
        // Clear existing maps
        this.routes.clear();
        this.miningOps.clear();
        this.processingFacilities.clear();
        
        // Render all data
        if (data.routes) {
            data.routes.forEach(route => {
                this.routes.set(route.id, route);
                this._createRoute(route);
            });
        }
        
        if (data.miningOperations) {
            data.miningOperations.forEach(op => {
                this.miningOps.set(op.id, op);
                this._createMiningEffect(op);
            });
        }
        
        if (data.processingFacilities) {
            data.processingFacilities.forEach(facility => {
                this.processingFacilities.set(facility.id, facility);
                this._createFacilityMarker(facility);
            });
        }
        
        console.log('[ResourcePipelineViz] Pipeline snapshot received');
    }

    /**
     * Handle route update.
     */
    _handleRouteUpdate(data) {
        const route = this.routes.get(data.id);
        if (route) {
            route.progress = data.progress;
            route.status = data.status;
        } else {
            this.routes.set(data.id, data);
        }
        
        if (this.onRouteUpdate) {
            this.onRouteUpdate(data);
        }
    }

    /**
     * Handle mining operation update.
     */
    _handleMiningUpdate(data) {
        const mining = this.miningOps.get(data.id);
        if (mining) {
            mining.currentOutput = data.currentOutput;
            mining.status = data.status;
        } else {
            this.miningOps.set(data.id, data);
        }
        
        if (this.onMiningUpdate) {
            this.onMiningUpdate(data);
        }
    }

    /**
     * Handle facility update.
     */
    _handleFacilityUpdate(data) {
        const facility = this.processingFacilities.get(data.id);
        if (facility) {
            facility.efficiency = data.efficiency;
            facility.status = data.status;
        } else {
            this.processingFacilities.set(data.id, data);
        }
        
        if (this.onFacilityUpdate) {
            this.onFacilityUpdate(data);
        }
    }

    /**
     * Handle statistics update.
     */
    _handleStatisticsUpdate(data) {
        if (this.onStatisticsUpdate) {
            this.onStatisticsUpdate(data);
        }
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
            console.warn('[ResourcePipelineViz] Max reconnect attempts reached');
            return;
        }
        this.reconnectAttempts++;
        const delay = 1000 * Math.pow(2, this.reconnectAttempts - 1);
        console.log(`[ResourcePipelineViz] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
        setTimeout(() => {
            if (this.currentGalaxyId) {
                this.connect(this.currentGalaxyId);
            }
        }, delay);
    }

    /**
     * Subscribe to pipeline updates for a galaxy.
     * @param {string} galaxyId - Galaxy ID
     */
    subscribeToPipeline(galaxyId) {
        this.currentGalaxyId = galaxyId;
        
        if (!this.wsConnected) {
            console.warn('[ResourcePipelineViz] Not connected, will subscribe on reconnect');
            return;
        }
        
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(JSON.stringify({
                type: 'subscribe',
                galaxyId
            }));
        }
    }

    /**
     * Set update callbacks.
     * @param {Function} onRouteUpdate - Called when a route is updated
     * @param {Function} onMiningUpdate - Called when a mining op is updated
     * @param {Function} onFacilityUpdate - Called when a facility is updated
     * @param {Function} onStatisticsUpdate - Called when statistics are updated
     */
    setCallbacks(onRouteUpdate, onMiningUpdate, onFacilityUpdate, onStatisticsUpdate) {
        this.onRouteUpdate = onRouteUpdate;
        this.onMiningUpdate = onMiningUpdate;
        this.onFacilityUpdate = onFacilityUpdate;
        this.onStatisticsUpdate = onStatisticsUpdate;
    }

    /**
     * Disconnect from WebSocket.
     */
    disconnect() {
        this._stopPing();
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }

    async loadPipeline(galaxyId) {
        console.log(`[ResourcePipelineViz] Loading pipeline for: ${galaxyId}`);
        
        try {
            const response = await fetch(`${this.apiBaseUrl}/resource-pipeline/${encodeURIComponent(galaxyId)}`);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            
            this.pipelineData = await response.json();
            this._clearAll();
            this._renderPipeline();
            
            return this.pipelineData;
        } catch (error) {
            console.error('[ResourcePipelineViz] Failed to load:', error);
            return null;
        }
    }

    _renderPipeline() {
        if (!this.pipelineData) return;
        
        if (this.pipelineData.routes) {
            this.pipelineData.routes.forEach(route => this._createRoute(route));
        }
        
        if (this.pipelineData.miningOperations) {
            this.pipelineData.miningOperations.forEach(op => this._createMiningEffect(op));
        }
        
        if (this.pipelineData.processingFacilities) {
            this.pipelineData.processingFacilities.forEach(facility => this._createFacilityMarker(facility));
        }
    }

    _createRoute(route) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        
        // Create route line
        const points = [];
        if (route.fromPosition && route.toPosition) {
            // Create curved path
            const from = new THREE.Vector3(route.fromPosition.x, route.fromPosition.y, route.fromPosition.z);
            const to = new THREE.Vector3(route.toPosition.x, route.toPosition.y, route.toPosition.z);
            const mid = from.clone().add(to).multiplyScalar(0.5);
            mid.y += from.distanceTo(to) * 0.1;
            
            const curve = new THREE.QuadraticBezierCurve3(from, mid, to);
            points.push(...curve.getPoints(50));
        }
        
        if (points.length === 0) return;
        
        const geometry = new THREE.BufferGeometry().setFromPoints(points);
        const color = this.resourceColors[route.resourceType] || 0x888888;
        
        const material = new THREE.LineBasicMaterial({
            color: color,
            transparent: true,
            opacity: 0.3
        });
        
        const line = new THREE.Line(geometry, material);
        scene.add(line);
        this.routeLines.set(route.id, line);
        
        // Create transport particles
        this._createTransportParticles(route, curve || null);
    }

    _createTransportParticles(route, curve) {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        const count = 10;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        
        const color = this.resourceColors[route.resourceType] || 0x888888;
        const material = new THREE.PointsMaterial({
            color: color,
            size: 0.3,
            transparent: true,
            opacity: 0.8
        });
        
        const particles = new THREE.Points(geometry, material);
        particles.userData = {
            route,
            curve,
            fromPos: route.fromPosition,
            toPos: route.toPosition,
            progress: new Array(count).fill(0).map(() => Math.random())
        };
        
        scene.add(particles);
        this.transportParticles.set(route.id, particles);
    }

    _createMiningEffect(op) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        
        const group = new THREE.Group();
        group.position.set(op.position?.x || 0, op.position?.y || 0, op.position?.z || 0);
        
        // Mining beam
        const beamGeo = new THREE.CylinderGeometry(0.1, 0.3, 2, 8);
        const beamMat = new THREE.MeshStandardMaterial({
            color: 0xff8800,
            emissive: 0xff4400,
            emissiveIntensity: 0.8,
            transparent: true,
            opacity: 0.6
        });
        const beam = new THREE.Mesh(beamGeo, beamMat);
        beam.position.y = -1;
        group.add(beam);
        
        // Particles
        const count = 30;
        const pGeo = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        
        for (let i = 0; i < count; i++) {
            const angle = Math.random() * Math.PI * 2;
            const radius = Math.random() * 0.5;
            positions[i * 3] = Math.cos(angle) * radius;
            positions[i * 3 + 1] = Math.random() * 2;
            positions[i * 3 + 2] = Math.sin(angle) * radius;
        }
        
        pGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        
        const color = this.resourceColors[op.resourceType] || 0xff8800;
        const pMat = new THREE.PointsMaterial({
            color: color,
            size: 0.15,
            transparent: true,
            opacity: 0.7
        });
        
        const particles = new THREE.Points(pGeo, pMat);
        particles.userData = { type: 'miningParticles' };
        group.add(particles);
        
        // Label
        if (op.name) {
            const label = this._createLabel(`${op.name} (${op.rate || 0}/s)`);
            if (label) {
                label.position.y = 2;
                group.add(label);
            }
        }
        
        scene.add(group);
        this.miningEffects.set(op.id, group);
    }

    _createFacilityMarker(facility) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        
        const group = new THREE.Group();
        group.position.set(facility.position?.x || 0, facility.position?.y || 0, facility.position?.z || 0);
        
        // Facility building
        const buildingGeo = new THREE.BoxGeometry(1, 1.5, 1);
        const buildingMat = new THREE.MeshStandardMaterial({
            color: 0x4488aa,
            emissive: 0x224466,
            emissiveIntensity: 0.3
        });
        const building = new THREE.Mesh(buildingGeo, buildingMat);
        building.position.y = 0.75;
        group.add(building);
        
        // Production indicator (rotating ring)
        const ringGeo = new THREE.TorusGeometry(0.8, 0.05, 8, 16);
        const ringMat = new THREE.MeshStandardMaterial({
            color: 0x00ff88,
            emissive: 0x00ff88,
            emissiveIntensity: 0.5
        });
        const ring = new THREE.Mesh(ringGeo, ringMat);
        ring.rotation.x = Math.PI / 2;
        ring.position.y = 1.5;
        ring.userData = { type: 'productionRing' };
        group.add(ring);
        
        // Label
        if (facility.name) {
            const label = this._createLabel(`${facility.name} (${facility.productionRate || 0}/s)`);
            if (label) {
                label.position.y = 2.5;
                group.add(label);
            }
        }
        
        scene.add(group);
        this.facilityMarkers.set(facility.id, group);
    }

    _createLabel(text) {
        if (typeof THREE === 'undefined') return null;
        
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = 512;
        canvas.height = 64;
        
        ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        ctx.beginPath();
        ctx.roundRect(0, 0, 512, 64, 10);
        ctx.fill();
        
        ctx.font = 'Bold 16px Arial';
        ctx.fillStyle = '#ffffff';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        
        const maxLen = 40;
        const displayText = text.length > maxLen ? text.substring(0, maxLen) + '...' : text;
        ctx.fillText(displayText, 256, 32);
        
        const texture = new THREE.CanvasTexture(canvas);
        const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
        const sprite = new THREE.Sprite(spriteMat);
        sprite.scale.set(4, 0.5, 1);
        
        return sprite;
    }

    startAnimation() {
        const animate = () => {
            this.animationFrame = requestAnimationFrame(animate);
            
            // Animate transport particles along routes
            this.transportParticles.forEach(particles => {
                const data = particles.userData;
                const positions = particles.geometry.attributes.position.array;
                
                for (let i = 0; i < data.progress.length; i++) {
                    data.progress[i] += 0.002;
                    if (data.progress[i] > 1) data.progress[i] = 0;
                    
                    const t = data.progress[i];
                    let point;
                    
                    if (data.curve) {
                        point = data.curve.getPoint(t);
                    } else {
                        const from = data.fromPos;
                        const to = data.toPos;
                        point = {
                            x: from.x + (to.x - from.x) * t,
                            y: from.y + (to.y - from.y) * t,
                            z: from.z + (to.z - from.z) * t
                        };
                    }
                    
                    positions[i * 3] = point.x;
                    positions[i * 3 + 1] = point.y;
                    positions[i * 3 + 2] = point.z;
                }
                
                particles.geometry.attributes.position.needsUpdate = true;
            });
            
            // Animate mining particles
            this.miningEffects.forEach(group => {
                const particles = group.children.find(c => c.userData?.type === 'miningParticles');
                if (particles) {
                    const positions = particles.geometry.attributes.position.array;
                    for (let i = 0; i < positions.length; i += 3) {
                        positions[i + 1] += 0.02;
                        if (positions[i + 1] > 2) {
                            positions[i + 1] = 0;
                            const angle = Math.random() * Math.PI * 2;
                            const radius = Math.random() * 0.5;
                            positions[i] = Math.cos(angle) * radius;
                            positions[i + 2] = Math.sin(angle) * radius;
                        }
                    }
                    particles.geometry.attributes.position.needsUpdate = true;
                }
            });
            
            // Rotate production rings
            this.facilityMarkers.forEach(group => {
                const ring = group.children.find(c => c.userData?.type === 'productionRing');
                if (ring) {
                    ring.rotation.z += 0.02;
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
        };
        animate();
    }

    stopAnimation() {
        if (this.animationFrame) {
            cancelAnimationFrame(this.animationFrame);
            this.animationFrame = null;
        }
    }

    getStatistics() {
        if (!this.pipelineData) return {};
        
        return {
            routes: this.pipelineData.routes?.length || 0,
            miningOperations: this.pipelineData.miningOperations?.length || 0,
            processingFacilities: this.pipelineData.processingFacilities?.length || 0
        };
    }

    _clearAll() {
        if (!this.sceneRenderer?.scene) return;
        
        const scene = this.sceneRenderer.scene;
        
        this.routeLines.forEach(line => scene.remove(line));
        this.routeLines.clear();
        
        this.transportParticles.forEach(p => scene.remove(p));
        this.transportParticles.clear();
        
        this.miningEffects.forEach(g => scene.remove(g));
        this.miningEffects.clear();
        
        this.facilityMarkers.forEach(g => scene.remove(g));
        this.facilityMarkers.clear();
    }

    destroy() {
        this.disconnect();
        this.stopAnimation();
        this._clearAll();
        this.pipelineData = null;
        this.routes.clear();
        this.miningOps.clear();
        this.processingFacilities.clear();
        console.log('[ResourcePipelineViz] Destroyed');
    }
}

window.ResourcePipelineViz = ResourcePipelineViz;

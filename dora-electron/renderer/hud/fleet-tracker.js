/**
 * FleetTracker — Real-time fleet visualization with WebSocket-driven updates.
 *
 * Provides:
 * - WebSocket subscription to fleet position updates
 * - Fleet markers with type-specific 3D models (warships, transports, fighters)
 * - Smooth movement animation between positions
 * - Combat visualization (attack lines, explosions, damage indicators)
 * - Fleet info panels on click
 * - Spatial audio for combat events
 *
 * Usage:
 *   const tracker = new FleetTracker(sceneRenderer, apiBaseUrl);
 *   await tracker.connect();
 *   tracker.subscribeToFleets(galaxyId);
 *   tracker.destroy();
 */

class FleetTracker {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     * @param {string} apiBaseUrl - Base URL for the API
     */
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // WebSocket connection
        this.ws = null;
        this.wsConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectDelay = 1000;
        this.pingInterval = null;
        this.currentGalaxyId = null;
        
        // Fleet data
        this.fleets = new Map(); // fleetId -> fleet data
        this.fleetMeshes = new Map(); // fleetId -> THREE.Group
        this.fleetAnimations = new Map(); // fleetId -> animation state
        
        // Combat data
        this.combatEvents = new Map(); // eventId -> combat event
        this.explosionEffects = [];
        this.attackLines = [];
        
        // Fleet type configurations
        this.fleetTypes = {
            warship: {
                color: 0xff4444,
                emissive: 0x880000,
                size: 1.5,
                geometry: 'warship',
                label: 'Warship'
            },
            transport: {
                color: 0x44ff44,
                emissive: 0x008800,
                size: 1.2,
                geometry: 'transport',
                label: 'Transport'
            },
            fighter: {
                color: 0x4444ff,
                emissive: 0x000088,
                size: 0.8,
                geometry: 'fighter',
                label: 'Fighter'
            },
            capital: {
                color: 0xffaa00,
                emissive: 0x885500,
                size: 2.5,
                geometry: 'capital',
                label: 'Capital Ship'
            },
            support: {
                color: 0xaa44ff,
                emissive: 0x550088,
                size: 1.0,
                geometry: 'support',
                label: 'Support'
            }
        };
        
        // Animation
        this.animationFrame = null;
        this.lastUpdate = 0;
        
        // Callbacks
        this.onFleetClick = null;
        this.onFleetHover = null;
        this.onCombatEvent = null;
        
        // Audio context for spatial audio
        this.audioContext = null;
        this.spatialAudioEnabled = false;
    }

    /**
     * Connect to the WebSocket for fleet updates.
     * @returns {Promise<boolean>} Whether connection was successful
     */
    async connect() {
        if (this.wsConnected) return true;
        
        try {
            // Get server URL from auth service
            const serverUrl = window.authService ? window.authService.getServerUrl() : '';
            const wsProtocol = serverUrl.includes('https') ? 'wss' : 'ws';
            const wsHost = serverUrl.replace(/https?:\/\//, '');
            
            // Get session info from localStorage
            const sessionId = localStorage.getItem('fps-match-id') || localStorage.getItem('game-session-id');
            const playerId = localStorage.getItem('fps-player-id') || localStorage.getItem('player-id');
            
            const wsUrl = `${wsProtocol}://${wsHost}/ws/fleet-tracker?sessionId=${sessionId}&playerId=${playerId}`;
            
            this.ws = new WebSocket(wsUrl);
            
            this.ws.onopen = () => {
                console.log('[FleetTracker] WebSocket connected');
                this.wsConnected = true;
                this.reconnectAttempts = 0;
                
                // Auto-subscribe if galaxy is set
                if (this.currentGalaxyId) {
                    this._sendSubscribe(this.currentGalaxyId);
                }
                
                // Start ping keepalive (every 30 seconds)
                this._startPing();
            };
            
            this.ws.onmessage = (event) => {
                this._handleWebSocketMessage(event.data);
            };
            
            this.ws.onclose = () => {
                console.log('[FleetTracker] WebSocket disconnected');
                this.wsConnected = false;
                this._stopPing();
                this._attemptReconnect();
            };
            
            this.ws.onerror = (error) => {
                console.error('[FleetTracker] WebSocket error:', error);
            };
            
            // Wait for connection
            return new Promise((resolve) => {
                const timeout = setTimeout(() => {
                    resolve(false);
                }, 5000);
                
                this.ws.addEventListener('open', () => {
                    clearTimeout(timeout);
                    resolve(true);
                }, { once: true });
            });
        } catch (error) {
            console.error('[FleetTracker] Connection failed:', error);
            return false;
        }
    }

    /**
     * Handle incoming WebSocket messages.
     * @param {string} data - Raw message data
     */
    _handleWebSocketMessage(data) {
        try {
            const message = JSON.parse(data);
            
            switch (message.type) {
                case 'fleet_update':
                    this._handleFleetUpdate(message.data);
                    break;
                case 'fleet_moved':
                    this._handleFleetMoved(message.data);
                    break;
                case 'fleet_combat':
                    this._handleCombatEvent(message.data);
                    break;
                case 'fleet_destroyed':
                    this._handleFleetDestroyed(message.data);
                    break;
                case 'fleet_created':
                    this._handleFleetCreated(message.data);
                    break;
                case 'pong':
                    // Server responded to ping - connection is alive
                    break;
                default:
                    console.warn('[FleetTracker] Unknown message type:', message.type);
            }
        } catch (error) {
            console.error('[FleetTracker] Failed to parse message:', error);
        }
    }

    /**
     * Handle fleet update message.
     * @param {Object} data - Fleet update data
     */
    _handleFleetUpdate(data) {
        const fleetId = data.fleetId || data.id;
        if (!fleetId) return;
        
        // Update fleet data
        this.fleets.set(fleetId, {
            ...this.fleets.get(fleetId),
            ...data,
            lastUpdate: Date.now()
        });
        
        // Update or create fleet marker
        this._updateFleetMarker(fleetId, data);
    }

    /**
     * Handle fleet movement message.
     * @param {Object} data - Movement data
     */
    _handleFleetMoved(data) {
        const fleetId = data.fleetId || data.id;
        if (!fleetId) return;
        
        const fleet = this.fleets.get(fleetId);
        if (!fleet) return;
        
        // Start smooth animation to new position
        this._animateFleetMovement(fleetId, data.fromPosition, data.toPosition, data.duration || 2000);
        
        // Update fleet data
        this.fleets.set(fleetId, {
            ...fleet,
            position: data.toPosition,
            destination: data.destination || null
        });
    }

    /**
     * Handle combat event message.
     * @param {Object} data - Combat event data
     */
    _handleCombatEvent(data) {
        const eventId = data.eventId || `combat_${Date.now()}`;
        this.combatEvents.set(eventId, data);
        
        // Visualize combat
        this._visualizeCombat(data);
        
        // Play combat sound
        if (this.spatialAudioEnabled) {
            this._playCombatSound(data);
        }
        
        // Notify callback
        if (this.onCombatEvent) {
            this.onCombatEvent(data);
        }
    }

    /**
     * Handle fleet destroyed message.
     * @param {Object} data - Destruction data
     */
    _handleFleetDestroyed(data) {
        const fleetId = data.fleetId || data.id;
        
        // Create explosion effect
        if (data.position) {
            this._createExplosion(data.position, data.size || 5);
        }
        
        // Remove fleet
        this._removeFleet(fleetId);
    }

    /**
     * Handle fleet created message.
     * @param {Object} data - New fleet data
     */
    _handleFleetCreated(data) {
        const fleetId = data.fleetId || data.id;
        this.fleets.set(fleetId, data);
        this._createFleetMarker(fleetId, data);
    }

    /**
     * Create a 3D marker for a fleet.
     * @param {string} fleetId - Fleet ID
     * @param {Object} data - Fleet data
     */
    _createFleetMarker(fleetId, data) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        const fleetType = this.fleetTypes[data.type] || this.fleetTypes.warship;
        
        // Create group for the fleet
        const group = new THREE.Group();
        group.position.set(
            data.position?.x || 0,
            data.position?.y || 0,
            data.position?.z || 0
        );
        
        // Create ship model based on type
        const mesh = this._createShipModel(fleetType, data);
        if (mesh) {
            group.add(mesh);
        }
        
        // Add health bar
        if (data.health !== undefined) {
            const healthBar = this._createHealthBar(data.health, data.maxHealth || 100);
            if (healthBar) {
                healthBar.position.y = fleetType.size + 0.5;
                group.add(healthBar);
            }
        }
        
        // Add label
        if (data.name) {
            const label = this._createLabel(data.name);
            if (label) {
                label.position.y = fleetType.size + 1;
                group.add(label);
            }
        }
        
        // Store data
        group.userData = {
            interactive: true,
            id: fleetId,
            name: data.name || `Fleet ${fleetId}`,
            type: data.type || 'warship',
            data: data
        };
        
        scene.add(group);
        this.fleetMeshes.set(fleetId, group);
        
        // Add to interactive objects
        if (this.sceneRenderer.interactiveObjects) {
            this.sceneRenderer.interactiveObjects.push(group);
        }
        
        console.log(`[FleetTracker] Created marker: ${data.name || fleetId}`);
    }

    /**
     * Update an existing fleet marker.
     * @param {string} fleetId - Fleet ID
     * @param {Object} data - Updated fleet data
     */
    _updateFleetMarker(fleetId, data) {
        const group = this.fleetMeshes.get(fleetId);
        if (!group) {
            this._createFleetMarker(fleetId, data);
            return;
        }
        
        // Update position if changed
        if (data.position) {
            group.position.set(
                data.position.x || group.position.x,
                data.position.y || group.position.y,
                data.position.z || group.position.z
            );
        }
        
        // Update health bar
        if (data.health !== undefined) {
            const healthBar = group.children.find(c => c.userData?.type === 'healthBar');
            if (healthBar) {
                this._updateHealthBar(healthBar, data.health, data.maxHealth || 100);
            }
        }
        
        // Update userData
        group.userData.data = { ...group.userData.data, ...data };
    }

    /**
     * Create a 3D ship model based on fleet type.
     * @param {Object} fleetType - Fleet type configuration
     * @param {Object} data - Fleet data
     * @returns {THREE.Group} Ship model group
     */
    _createShipModel(fleetType, data) {
        if (typeof THREE === 'undefined') return null;
        
        const group = new THREE.Group();
        const size = fleetType.size;
        
        const material = new THREE.MeshStandardMaterial({
            color: fleetType.color,
            emissive: fleetType.emissive,
            emissiveIntensity: 0.5,
            metalness: 0.7,
            roughness: 0.3
        });
        
        switch (fleetType.geometry) {
            case 'warship':
                // Main hull
                const hull = new THREE.Mesh(new THREE.BoxGeometry(size * 0.4, size * 0.3, size), material);
                group.add(hull);
                // Bridge
                const bridge = new THREE.Mesh(new THREE.BoxGeometry(size * 0.3, size * 0.2, size * 0.3), material);
                bridge.position.set(0, size * 0.25, size * 0.3);
                group.add(bridge);
                // Wings
                for (const side of [-1, 1]) {
                    const wing = new THREE.Mesh(new THREE.BoxGeometry(size * 0.8, size * 0.05, size * 0.4), material);
                    wing.position.set(side * size * 0.5, 0, 0);
                    group.add(wing);
                }
                break;
                
            case 'transport':
                // Cargo body
                const cargo = new THREE.Mesh(new THREE.BoxGeometry(size * 0.6, size * 0.5, size * 0.8), material);
                group.add(cargo);
                // Engine section
                const engine = new THREE.Mesh(new THREE.CylinderGeometry(size * 0.1, size * 0.2, size * 0.3, 8), material);
                engine.rotation.x = Math.PI / 2;
                engine.position.z = -size * 0.5;
                group.add(engine);
                break;
                
            case 'fighter':
                // Sleek body
                const body = new THREE.Mesh(new THREE.ConeGeometry(size * 0.2, size, 4), material);
                body.rotation.x = Math.PI / 2;
                group.add(body);
                // Wings
                for (const side of [-1, 1]) {
                    const wing = new THREE.Mesh(new THREE.BoxGeometry(size * 0.6, size * 0.02, size * 0.2), material);
                    wing.position.set(side * size * 0.4, 0, 0);
                    wing.rotation.z = side * 0.3;
                    group.add(wing);
                }
                break;
                
            case 'capital':
                // Large hull
                const capHull = new THREE.Mesh(new THREE.BoxGeometry(size * 0.6, size * 0.4, size * 1.5), material);
                group.add(capHull);
                // Bridge tower
                const capBridge = new THREE.Mesh(new THREE.BoxGeometry(size * 0.3, size * 0.6, size * 0.3), material);
                capBridge.position.set(0, size * 0.5, size * 0.5);
                group.add(capBridge);
                // Weapon turrets
                for (const z of [-0.5, 0, 0.5]) {
                    for (const side of [-1, 1]) {
                        const turret = new THREE.Mesh(new THREE.CylinderGeometry(size * 0.05, size * 0.08, size * 0.2, 6), material);
                        turret.position.set(side * size * 0.4, size * 0.2, z * size);
                        group.add(turret);
                    }
                }
                break;
                
            case 'support':
                // Medical/science vessel
                const supBody = new THREE.Mesh(new THREE.SphereGeometry(size * 0.4, 16, 16), material);
                group.add(supBody);
                // Cross indicator
                const crossMat = new THREE.MeshStandardMaterial({
                    color: 0xffffff,
                    emissive: 0xffffff,
                    emissiveIntensity: 0.8
                });
                const cross1 = new THREE.Mesh(new THREE.BoxGeometry(size * 0.5, size * 0.1, size * 0.1), crossMat);
                const cross2 = new THREE.Mesh(new THREE.BoxGeometry(size * 0.1, size * 0.1, size * 0.5), crossMat);
                group.add(cross1);
                group.add(cross2);
                break;
                
            default:
                // Generic ship
                const generic = new THREE.Mesh(new THREE.BoxGeometry(size * 0.3, size * 0.3, size * 0.8), material);
                group.add(generic);
        }
        
        // Add engine glow
        const glowMat = new THREE.MeshStandardMaterial({
            color: 0x00aaff,
            emissive: 0x00aaff,
            emissiveIntensity: 1,
            transparent: true,
            opacity: 0.8
        });
        const glow = new THREE.Mesh(new THREE.SphereGeometry(size * 0.1, 8, 8), glowMat);
        glow.position.z = -size * 0.5;
        group.add(glow);
        
        return group;
    }

    /**
     * Create a health bar for a fleet.
     * @param {number} health - Current health
     * @param {number} maxHealth - Maximum health
     * @returns {THREE.Mesh} Health bar
     */
    _createHealthBar(health, maxHealth) {
        if (typeof THREE === 'undefined') return null;
        
        const ratio = Math.max(0, health / maxHealth);
        const width = 2;
        const height = 0.2;
        
        // Background
        const bgGeo = new THREE.PlaneGeometry(width, height);
        const bgMat = new THREE.MeshBasicMaterial({
            color: 0x333333,
            transparent: true,
            opacity: 0.7,
            side: THREE.DoubleSide
        });
        const bg = new THREE.Mesh(bgGeo, bgMat);
        
        // Health fill
        const fillGeo = new THREE.PlaneGeometry(width * ratio, height);
        const fillColor = ratio > 0.6 ? 0x00ff00 : ratio > 0.3 ? 0xffff00 : 0xff0000;
        const fillMat = new THREE.MeshBasicMaterial({
            color: fillColor,
            transparent: true,
            opacity: 0.9,
            side: THREE.DoubleSide
        });
        const fill = new THREE.Mesh(fillGeo, fillMat);
        fill.position.x = -(width * (1 - ratio)) / 2;
        
        // Group
        const group = new THREE.Group();
        group.userData = { type: 'healthBar', fill, bg };
        group.add(bg);
        group.add(fill);
        
        return group;
    }

    /**
     * Update health bar.
     * @param {THREE.Group} healthBar - Health bar group
     * @param {number} health - Current health
     * @param {number} maxHealth - Maximum health
     */
    _updateHealthBar(healthBar, health, maxHealth) {
        if (!healthBar || !healthBar.userData?.fill) return;
        
        const ratio = Math.max(0, health / maxHealth);
        const width = 2;
        
        // Update fill geometry
        healthBar.userData.fill.geometry.dispose();
        const fillGeo = new THREE.PlaneGeometry(width * ratio, 0.2);
        healthBar.userData.fill.geometry = fillGeo;
        healthBar.userData.fill.position.x = -(width * (1 - ratio)) / 2;
        
        // Update color
        const fillColor = ratio > 0.6 ? 0x00ff00 : ratio > 0.3 ? 0xffff00 : 0xff0000;
        healthBar.userData.fill.material.color.setHex(fillColor);
    }

    /**
     * Create a text label.
     * @param {string} text - Label text
     * @returns {THREE.Sprite} Label sprite
     */
    _createLabel(text) {
        if (typeof THREE === 'undefined') return null;
        
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = 256;
        canvas.height = 64;
        
        ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        ctx.beginPath();
        ctx.roundRect(0, 0, 256, 64, 10);
        ctx.fill();
        
        ctx.font = 'Bold 18px Arial';
        ctx.fillStyle = '#ffffff';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        
        const maxLen = 20;
        const displayText = text.length > maxLen ? text.substring(0, maxLen) + '...' : text;
        ctx.fillText(displayText, 128, 32);
        
        const texture = new THREE.CanvasTexture(canvas);
        const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
        const sprite = new THREE.Sprite(spriteMat);
        sprite.scale.set(3, 0.75, 1);
        
        return sprite;
    }

    /**
     * Animate fleet movement between positions.
     * @param {string} fleetId - Fleet ID
     * @param {Object} fromPos - Starting position
     * @param {Object} toPos - Target position
     * @param {number} duration - Animation duration in ms
     */
    _animateFleetMovement(fleetId, fromPos, toPos, duration) {
        const group = this.fleetMeshes.get(fleetId);
        if (!group) return;
        
        const startTime = Date.now();
        const startX = fromPos?.x ?? group.position.x;
        const startY = fromPos?.y ?? group.position.y;
        const startZ = fromPos?.z ?? group.position.z;
        const endX = toPos?.x ?? startX;
        const endY = toPos?.y ?? startY;
        const endZ = toPos?.z ?? startZ;
        
        this.fleetAnimations.set(fleetId, {
            startTime,
            duration,
            startX, startY, startZ,
            endX, endY, endZ
        });
    }

    /**
     * Visualize a combat event.
     * @param {Object} data - Combat event data
     */
    _visualizeCombat(data) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        
        // Create attack line
        if (data.attackerPosition && data.targetPosition) {
            this._createAttackLine(data.attackerPosition, data.targetPosition, data.attackerType);
        }
        
        // Create explosion at target
        if (data.targetPosition && data.damage > 0) {
            this._createExplosion(data.targetPosition, data.damage / 10);
        }
        
        // Create damage indicator
        if (data.targetPosition && data.targetName) {
            this._createDamageIndicator(data.targetPosition, data.damage);
        }
    }

    /**
     * Create an attack line between two positions.
     * @param {Object} fromPos - Attacker position
     * @param {Object} toPos - Target position
     * @param {string} attackerType - Attacker fleet type
     */
    _createAttackLine(fromPos, toPos, attackerType) {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        
        const points = [
            new THREE.Vector3(fromPos.x, fromPos.y, fromPos.z),
            new THREE.Vector3(toPos.x, toPos.y, toPos.z)
        ];
        
        const geometry = new THREE.BufferGeometry().setFromPoints(points);
        
        // Color based on attacker type
        const colors = {
            warship: 0xff0000,
            fighter: 0x00ff00,
            capital: 0xffaa00,
            support: 0xaa00ff
        };
        
        const material = new THREE.LineBasicMaterial({
            color: colors[attackerType] || 0xffffff,
            transparent: true,
            opacity: 0.8
        });
        
        const line = new THREE.Line(geometry, material);
        scene.add(line);
        this.attackLines.push({ line, createdAt: Date.now() });
        
        // Fade out after 1 second
        setTimeout(() => {
            scene.remove(line);
            geometry.dispose();
            material.dispose();
            this.attackLines = this.attackLines.filter(l => l !== line);
        }, 1000);
    }

    /**
     * Create an explosion effect at a position.
     * @param {Object} position - Explosion position
     * @param {number} size - Explosion size
     */
    _createExplosion(position, size) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        
        // Explosion sphere
        const geo = new THREE.SphereGeometry(size, 16, 16);
        const mat = new THREE.MeshStandardMaterial({
            color: 0xff6600,
            emissive: 0xff4400,
            emissiveIntensity: 1,
            transparent: true,
            opacity: 0.9
        });
        const explosion = new THREE.Mesh(geo, mat);
        explosion.position.set(position.x, position.y, position.z);
        scene.add(explosion);
        
        // Point light for glow
        const light = new THREE.PointLight(0xff6600, 2, size * 10);
        light.position.copy(explosion.position);
        scene.add(light);
        
        this.explosionEffects.push({
            mesh: explosion,
            light,
            createdAt: Date.now(),
            duration: 1500
        });
        
        // Animate explosion
        this._animateExplosion(explosion, light, size);
    }

    /**
     * Animate an explosion effect.
     * @param {THREE.Mesh} explosion - Explosion mesh
     * @param {THREE.Light} light - Explosion light
     * @param {number} size - Initial size
     */
    _animateExplosion(explosion, light, size) {
        const startTime = Date.now();
        const duration = 1500;
        
        const animate = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(1, elapsed / duration);
            
            // Expand and fade
            const scale = 1 + progress * 2;
            explosion.scale.set(scale, scale, scale);
            explosion.material.opacity = 0.9 * (1 - progress);
            light.intensity = 2 * (1 - progress);
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                // Clean up
                if (this.sceneRenderer?.scene) {
                    this.sceneRenderer.scene.remove(explosion);
                    this.sceneRenderer.scene.remove(light);
                }
                explosion.geometry.dispose();
                explosion.material.dispose();
                light.dispose();
                
                // Remove from effects list
                this.explosionEffects = this.explosionEffects.filter(e => e.mesh !== explosion);
            }
        };
        
        animate();
    }

    /**
     * Create a damage number indicator.
     * @param {Object} position - Position
     * @param {number} damage - Damage amount
     */
    _createDamageIndicator(position, damage) {
        if (typeof THREE === 'undefined' || !this.sceneRenderer) return;
        
        const scene = this.sceneRenderer.scene;
        
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = 128;
        canvas.height = 64;
        
        ctx.font = 'Bold 32px Arial';
        ctx.fillStyle = '#ff0000';
        ctx.strokeStyle = '#000000';
        ctx.lineWidth = 3;
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.strokeText(`-${damage}`, 64, 32);
        ctx.fillText(`-${damage}`, 64, 32);
        
        const texture = new THREE.CanvasTexture(canvas);
        const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
        const sprite = new THREE.Sprite(spriteMat);
        sprite.position.set(position.x, position.y + 3, position.z);
        sprite.scale.set(2, 1, 1);
        
        scene.add(sprite);
        
        // Float up and fade
        const startTime = Date.now();
        const animate = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(1, elapsed / 2000);
            
            sprite.position.y += 0.02;
            sprite.material.opacity = 1 - progress;
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                scene.remove(sprite);
                texture.dispose();
                spriteMat.dispose();
            }
        };
        animate();
    }

    /**
     * Remove a fleet marker.
     * @param {string} fleetId - Fleet ID
     */
    _removeFleet(fleetId) {
        const group = this.fleetMeshes.get(fleetId);
        if (group && this.sceneRenderer?.scene) {
            this.sceneRenderer.scene.remove(group);
            
            // Remove from interactive objects
            if (this.sceneRenderer.interactiveObjects) {
                const index = this.sceneRenderer.interactiveObjects.indexOf(group);
                if (index !== -1) {
                    this.sceneRenderer.interactiveObjects.splice(index, 1);
                }
            }
            
            this.fleetMeshes.delete(fleetId);
            this.fleetAnimations.delete(fleetId);
        }
        
        this.fleets.delete(fleetId);
        console.log(`[FleetTracker] Removed fleet: ${fleetId}`);
    }

    /**
     * Enable spatial audio for combat events.
     */
    enableSpatialAudio() {
        try {
            this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
            this.spatialAudioEnabled = true;
            console.log('[FleetTracker] Spatial audio enabled');
        } catch (error) {
            console.warn('[FleetTracker] Spatial audio not available:', error);
        }
    }

    /**
     * Play a combat sound effect.
     * @param {Object} data - Combat event data
     */
    _playCombatSound(data) {
        if (!this.audioContext || !data.targetPosition) return;
        
        try {
            // Create oscillator for simple sound effect
            const oscillator = this.audioContext.createOscillator();
            const gainNode = this.audioContext.createGain();
            
            oscillator.connect(gainNode);
            gainNode.connect(this.audioContext.destination);
            
            // Sound characteristics based on damage
            const frequency = Math.max(100, 500 - data.damage * 5);
            oscillator.frequency.value = frequency;
            oscillator.type = 'sawtooth';
            
            gainNode.gain.setValueAtTime(0.3, this.audioContext.currentTime);
            gainNode.gain.exponentialRampToValueAtTime(0.01, this.audioContext.currentTime + 0.5);
            
            oscillator.start(this.audioContext.currentTime);
            oscillator.stop(this.audioContext.currentTime + 0.5);
        } catch (error) {
            console.warn('[FleetTracker] Failed to play sound:', error);
        }
    }

    /**
     * Subscribe to fleet updates for a galaxy.
     * @param {string} galaxyId - Galaxy ID
     */
    subscribeToFleets(galaxyId) {
        this.currentGalaxyId = galaxyId;
        
        if (!this.wsConnected) {
            console.warn('[FleetTracker] Not connected, will subscribe on reconnect');
            return;
        }
        
        this._sendSubscribe(galaxyId);
    }

    /**
     * Send subscribe message to the server.
     * @param {string} galaxyId - Galaxy ID
     */
    _sendSubscribe(galaxyId) {
        if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
        
        this.ws.send(JSON.stringify({
            type: 'subscribe',
            galaxyId
        }));
        
        console.log(`[FleetTracker] Subscribed to galaxy: ${galaxyId}`);
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
     * Set interaction callbacks.
     * @param {Function} onClick - Called when a fleet is clicked
     * @param {Function} onHover - Called when hovering over a fleet
     * @param {Function} onCombat - Called when a combat event occurs
     */
    setCallbacks(onClick, onHover, onCombat) {
        this.onFleetClick = onClick;
        this.onFleetHover = onHover;
        this.onCombatEvent = onCombat;
    }

    /**
     * Get all fleet data.
     * @returns {Map} Fleet data map
     */
    getAllFleets() {
        return new Map(this.fleets);
    }

    /**
     * Get fleet by ID.
     * @param {string} fleetId - Fleet ID
     * @returns {Object|undefined} Fleet data
     */
    getFleet(fleetId) {
        return this.fleets.get(fleetId);
    }

    /**
     * Animation loop for fleet movements.
     */
    startAnimation() {
        const animate = () => {
            this.animationFrame = requestAnimationFrame(animate);
            
            // Update fleet animations
            this.fleetAnimations.forEach((anim, fleetId) => {
                const elapsed = Date.now() - anim.startTime;
                const progress = Math.min(1, elapsed / anim.duration);
                
                // Ease in-out
                const eased = progress < 0.5
                    ? 2 * progress * progress
                    : 1 - Math.pow(-2 * progress + 2, 2) / 2;
                
                const x = anim.startX + (anim.endX - anim.startX) * eased;
                const y = anim.startY + (anim.endY - anim.startY) * eased;
                const z = anim.startZ + (anim.endZ - anim.startZ) * eased;
                
                const group = this.fleetMeshes.get(fleetId);
                if (group) {
                    group.position.set(x, y, z);
                }
                
                if (progress >= 1) {
                    this.fleetAnimations.delete(fleetId);
                }
            });
            
            // Make health bars face camera
            if (this.sceneRenderer?.camera) {
                this.fleetMeshes.forEach(group => {
                    const healthBar = group.children.find(c => c.userData?.type === 'healthBar');
                    if (healthBar) {
                        healthBar.lookAt(this.sceneRenderer.camera.position);
                    }
                    // Make labels face camera
                    group.children.forEach(child => {
                        if (child.isSprite) {
                            child.lookAt(this.sceneRenderer.camera.position);
                        }
                    });
                });
            }
        };
        animate();
    }

    /**
     * Stop animation loop.
     */
    stopAnimation() {
        if (this.animationFrame) {
            cancelAnimationFrame(this.animationFrame);
            this.animationFrame = null;
        }
    }

    /**
     * Attempt to reconnect to WebSocket.
     */
    _attemptReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('[FleetTracker] Max reconnect attempts reached');
            return;
        }
        
        this.reconnectAttempts++;
        const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
        
        console.log(`[FleetTracker] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
        
        setTimeout(() => {
            this.connect();
        }, delay);
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
        this.wsConnected = false;
    }

    /**
     * Destroy the fleet tracker and clean up resources.
     */
    destroy() {
        this.stopAnimation();
        this.disconnect();
        
        // Remove all fleet meshes
        if (this.sceneRenderer?.scene) {
            this.fleetMeshes.forEach(group => {
                this.sceneRenderer.scene.remove(group);
            });
        }
        
        // Clear all data
        this.fleets.clear();
        this.fleetMeshes.clear();
        this.fleetAnimations.clear();
        this.combatEvents.clear();
        this.explosionEffects = [];
        this.attackLines = [];
        
        if (this.audioContext) {
            this.audioContext.close();
            this.audioContext = null;
        }
        
        console.log('[FleetTracker] Destroyed');
    }
}

// Expose globally
window.FleetTracker = FleetTracker;

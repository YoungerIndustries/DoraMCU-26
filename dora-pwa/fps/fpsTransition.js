/**
 * FPS Transition System — Manages portal rendering and map transitions.
 *
 * Creates visual portal markers at transition points defined by the server.
 * When a player walks through a portal, the client requests a map transition
 * and rebuilds the entire scene for the new location.
 *
 * Usage:
 *   const transitions = new FpsTransitionSystem(scene, mapGroup, network, game);
 *   transitions.loadTransitionPoints(mapData.transitionPoints || []);
 *   transitions.update(deltaTime, playerPosition);
 */
class FpsTransitionSystem {
    constructor(scene, mapGroup, network, game) {
        this.scene = scene;
        this.mapGroup = mapGroup;
        this.network = network;
        this.game = game;
        this.portals = [];
        this.transitioning = false;
        this.transitionCooldown = 0;
        this.portalGroup = new THREE.Group();
        this.portalGroup.name = 'TransitionPortals';
        this.scene.add(this.portalGroup);
    }

    /**
     * Load transition points from server map data.
     */
    loadTransitionPoints(transitionPoints) {
        // Clear existing portals
        this.clearPortals();

        if (!transitionPoints || transitionPoints.length === 0) {
            console.log('[FpsTransitionSystem] No transition points for this map');
            return;
        }

        console.log(`[FpsTransitionSystem] Loading ${transitionPoints.length} transition points`);

        for (const tp of transitionPoints) {
            const portal = this._createPortal(tp);
            this.portals.push(portal);
            this.portalGroup.add(portal.mesh);
        }
    }

    /**
     * Create a visual portal marker.
     */
    _createPortal(tp) {
        const group = new THREE.Group();
        group.position.set(tp.x, tp.y, tp.z);

        // Portal frame - glowing ring
        const ringGeo = new THREE.TorusGeometry(1.5, 0.15, 16, 32);
        const ringMat = new THREE.MeshStandardMaterial({
            color: 0x8844ff,
            emissive: 0x8844ff,
            emissiveIntensity: 0.8,
            transparent: true,
            opacity: 0.9
        });
        const ring = new THREE.Mesh(ringGeo, ringMat);
        group.add(ring);

        // Portal inner glow
        const innerGeo = new THREE.CircleGeometry(1.4, 32);
        const innerMat = new THREE.MeshStandardMaterial({
            color: 0x4400aa,
            emissive: 0x6622cc,
            emissiveIntensity: 0.5,
            transparent: true,
            opacity: 0.4,
            side: THREE.DoubleSide
        });
        const inner = new THREE.Mesh(innerGeo, innerMat);
        group.add(inner);

        // Swirling particles inside portal
        const particleCount = 50;
        const particleGeo = new THREE.BufferGeometry();
        const positions = new Float32Array(particleCount * 3);
        const velocities = [];
        for (let i = 0; i < particleCount; i++) {
            const angle = Math.random() * Math.PI * 2;
            const radius = Math.random() * 1.2;
            positions[i * 3] = Math.cos(angle) * radius;
            positions[i * 3 + 1] = Math.sin(angle) * radius;
            positions[i * 3 + 2] = (Math.random() - 0.5) * 0.3;
            velocities.push({
                angle: angle,
                radius: radius,
                speed: 0.5 + Math.random() * 1.5
            });
        }
        particleGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        const particleMat = new THREE.PointsMaterial({
            color: 0xaa66ff,
            size: 0.08,
            transparent: true,
            opacity: 0.7
        });
        const particles = new THREE.Points(particleGeo, particleMat);
        group.add(particles);

        // Point light for glow effect
        const light = new THREE.PointLight(0x8844ff, 2, 8);
        light.position.set(0, 0, 0);
        group.add(light);

        // Label sprite
        const label = this._createLabel(tp.label);
        label.position.y = 2.2;
        group.add(label);

        group.userData = {
            id: tp.id,
            targetMapId: tp.targetMapId,
            targetTransitionId: tp.targetTransitionId,
            label: tp.label,
            velocities: velocities,
            ring: ring,
            inner: inner,
            particles: particles,
            light: light
        };

        return {
            id: tp.id,
            x: tp.x,
            y: tp.y,
            z: tp.z,
            targetMapId: tp.targetMapId,
            targetTransitionId: tp.targetTransitionId,
            label: tp.label,
            mesh: group
        };
    }

    /**
     * Create a text label sprite.
     */
    _createLabel(text) {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = 512;
        canvas.height = 128;

        ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';
        ctx.roundRect(0, 0, 512, 128, 20);
        ctx.fill();

        ctx.font = 'bold 32px Arial';
        ctx.fillStyle = '#cc99ff';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(text, 256, 64);

        const texture = new THREE.CanvasTexture(canvas);
        const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
        const sprite = new THREE.Sprite(spriteMat);
        sprite.scale.set(4, 1, 1);
        return sprite;
    }

    /**
     * Update portal animations and check for player proximity.
     */
    update(deltaTime, playerPosition) {
        if (this.transitioning) return;

        // Cooldown management
        if (this.transitionCooldown > 0) {
            this.transitionCooldown -= deltaTime;
        }

        const time = performance.now() / 1000;

        for (const portal of this.portals) {
            const { mesh, x, z } = portal;
            const userData = mesh.userData;

            // Rotate the ring
            mesh.rotation.y = time * 0.5;

            // Pulse the glow
            const pulse = 0.6 + Math.sin(time * 2) * 0.3;
            userData.ring.material.emissiveIntensity = pulse;
            userData.light.intensity = 1 + Math.sin(time * 2) * 1;

            // Animate particles swirling
            const positions = userData.particles.geometry.attributes.position.array;
            for (let i = 0; i < userData.velocities.length; i++) {
                const v = userData.velocities[i];
                v.angle += v.speed * deltaTime;
                positions[i * 3] = Math.cos(v.angle) * v.radius;
                positions[i * 3 + 1] = Math.sin(v.angle) * v.radius;
                positions[i * 3 + 2] = Math.sin(time * v.speed + i) * 0.15;
            }
            userData.particles.geometry.attributes.position.needsUpdate = true;

            // Check player proximity
            if (playerPosition && this.transitionCooldown <= 0) {
                const dx = playerPosition.x - x;
                const dz = playerPosition.z - z;
                const distance = Math.sqrt(dx * dx + dz * dz);

                if (distance < 2.0) {
                    // Highlight portal when player is near
                    userData.ring.material.emissiveIntensity = 1.5;
                    userData.inner.material.opacity = 0.7;
                    userData.light.intensity = 4;

                    // Show prompt
                    this._showTransitionPrompt(portal.label, distance);

                    // Auto-transition when very close
                    if (distance < 1.0) {
                        this._triggerTransition(portal);
                        break;
                    }
                } else {
                    // Hide prompt when far
                    this._hideTransitionPrompt();
                }
            }
        }
    }

    /**
     * Show the transition prompt on screen.
     */
    _showTransitionPrompt(label, distance) {
        let prompt = document.getElementById('transitionPrompt');
        if (!prompt) {
            prompt = document.createElement('div');
            prompt.id = 'transitionPrompt';
            prompt.style.cssText = `
                position: fixed;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                background: rgba(0, 0, 0, 0.8);
                color: #cc99ff;
                padding: 20px 40px;
                border-radius: 10px;
                border: 2px solid #8844ff;
                font-size: 18px;
                font-family: Arial, sans-serif;
                text-align: center;
                pointer-events: none;
                z-index: 1000;
                display: none;
            `;
            document.body.appendChild(prompt);
        }
        prompt.style.display = 'block';
        prompt.innerHTML = `
            <div style="font-size: 24px; margin-bottom: 8px;">🌀 Portal Detected</div>
            <div style="font-size: 16px;">${label}</div>
            <div style="font-size: 14px; color: #999; margin-top: 8px;">Walk closer to enter</div>
        `;
    }

    /**
     * Hide the transition prompt.
     */
    _hideTransitionPrompt() {
        const prompt = document.getElementById('transitionPrompt');
        if (prompt) {
            prompt.style.display = 'none';
        }
    }

    /**
     * Trigger a map transition.
     */
    _triggerTransition(portal) {
        if (this.transitioning || this.transitionCooldown > 0) return;

        this.transitioning = true;
        this.transitionCooldown = 3.0; // 3 second cooldown

        console.log(`[FpsTransitionSystem] Transitioning to ${portal.targetMapId} via ${portal.id}`);

        // Show loading indicator
        this._showTransitionLoading(portal.label);

        // Send transition request to server
        this.network.sendMessage({
            type: 'TRANSITION',
            playerId: this.network.playerId,
            sessionId: this.network.sessionId,
            targetMapId: portal.targetMapId,
            transitionId: portal.id
        });
    }

    /**
     * Show loading indicator during transition.
     */
    _showTransitionLoading(label) {
        let overlay = document.getElementById('transitionOverlay');
        if (!overlay) {
            overlay = document.createElement('div');
            overlay.id = 'transitionOverlay';
            overlay.style.cssText = `
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(0, 0, 0, 0.9);
                display: none;
                justify-content: center;
                align-items: center;
                flex-direction: column;
                z-index: 2000;
                color: #cc99ff;
                font-family: Arial, sans-serif;
            `;
            document.body.appendChild(overlay);
        }
        overlay.style.display = 'flex';
        overlay.innerHTML = `
            <div style="font-size: 48px; margin-bottom: 20px;">🌀</div>
            <div style="font-size: 24px;">Traveling to ${label}...</div>
            <div style="font-size: 14px; color: #666; margin-top: 10px;">Loading new location</div>
        `;
    }

    /**
     * Handle the server's transition acknowledgment.
     * Rebuilds the entire scene for the new map.
     */
    handleTransitionAck(data) {
        console.log('[FpsTransitionSystem] Transition acknowledged:', data.mapName);

        // Hide loading overlay
        const overlay = document.getElementById('transitionOverlay');
        if (overlay) overlay.style.display = 'none';

        // Clear the current map group
        this.scene.remove(this.mapGroup);
        this.mapGroup.children = [];

        // Clear existing portals
        this.clearPortals();

        // Clear existing decorators
        if (this.game.outdoorDecorator) {
            this.game.outdoorDecorator.dispose();
            this.game.outdoorDecorator = null;
        }
        if (this.game.officeDecorator) {
            this.game.officeDecorator.dispose();
            this.game.officeDecorator = null;
        }
        // Clear existing scene object loader
        if (this.game.sceneObjectLoader) {
            this.game.sceneObjectLoader.dispose();
            this.game.sceneObjectLoader = null;
        }

        // Rebuild the map with new data
        this.game.map.generateFromData(data.map);

        // Re-apply the appropriate decorator based on the new map
        const mapName = (data.map.name || '').toLowerCase();
        if (mapName.includes('younger') || mapName.includes('hq') || mapName.includes('office')) {
            // Office interior
            this.game.officeDecorator = new FpsOfficeDecorator(this.scene, this.mapGroup, data.map);
            this.game.officeDecorator.decorate();
        } else {
            // Outdoor scene
            this.game.outdoorDecorator = new FpsSceneDecorator(this.scene, this.mapGroup);
            this.game.outdoorDecorator.decorate();
        }

        // Load database-driven scene objects for the new map
        const sceneObjects = data.map.sceneObjects || [];
        if (sceneObjects.length > 0) {
            if (!this.game.sceneObjectLoader) {
                this.game.sceneObjectLoader = new FpsSceneObjectLoader(this.scene, this.map.mapGroup);
            }
            this.game.sceneObjectLoader.loadSceneObjects(sceneObjects);
            console.log('[FpsTransitionSystem] Loaded', sceneObjects.length, 'scene objects for', data.mapName);
        }

        // Load new transition points
        this.loadTransitionPoints(data.map.transitionPoints || []);

        // Reset player position to spawn
        this.game.player.respawn({
            x: data.spawnX,
            y: data.spawnY,
            z: data.spawnZ
        });

        // Update HUD with new map name
        const mapNameEl = document.getElementById('mapName');
        if (mapNameEl) {
            mapNameEl.textContent = data.mapName;
        }

        // Update fog and background based on theme
        this._updateAtmosphere(data.theme);

        this.transitioning = false;
        console.log(`[FpsTransitionSystem] Transitioned to ${data.mapName}`);
    }

    /**
     * Update fog and background based on map theme.
     */
    _updateAtmosphere(theme) {
        const themeLower = (theme || '').toLowerCase();

        if (themeLower.includes('earth') || themeLower.includes('hq') || themeLower.includes('office')) {
            // Indoor/dark theme
            this.scene.fog = new THREE.FogExp2(0x1a1a2a, 0.02);
            this.scene.background = new THREE.Color(0x0a0a15);
        } else if (themeLower.includes('dnd') || themeLower.includes('dragon')) {
            // Dungeon/fantasy theme
            this.scene.fog = new THREE.FogExp2(0x2a1a0a, 0.015);
            this.scene.background = new THREE.Color(0x1a0a00);
        } else if (themeLower.includes('traveller') || themeLower.includes('space')) {
            // Space theme
            this.scene.fog = new THREE.FogExp2(0x000011, 0.005);
            this.scene.background = new THREE.Color(0x000005);
        } else if (themeLower.includes('babylon')) {
            // Space station theme
            this.scene.fog = new THREE.FogExp2(0x111122, 0.008);
            this.scene.background = new THREE.Color(0x050510);
        } else if (themeLower.includes('cthulhu')) {
            // Lovecraftian horror theme
            this.scene.fog = new THREE.FogExp2(0x0a1a0a, 0.02);
            this.scene.background = new THREE.Color(0x000a00);
        } else {
            // Default outdoor theme
            this.scene.fog = new THREE.FogExp2(0x87CEEB, 0.008);
            this.scene.background = new THREE.Color(0x87CEEB);
        }
    }

    /**
     * Clear all portal markers.
     */
    clearPortals() {
        for (const portal of this.portals) {
            this.portalGroup.remove(portal.mesh);
            portal.mesh.traverse((child) => {
                if (child.geometry) child.geometry.dispose();
                if (child.material) {
                    if (Array.isArray(child.material)) {
                        child.material.forEach(m => m.dispose());
                    } else {
                        child.material.dispose();
                    }
                }
            });
        }
        this.portals = [];

        // Hide any visible prompts
        this._hideTransitionPrompt();
        const overlay = document.getElementById('transitionOverlay');
        if (overlay) overlay.style.display = 'none';
    }

    /**
     * Clean up all resources.
     */
    dispose() {
        this.clearPortals();
        this.scene.remove(this.portalGroup);
        const prompt = document.getElementById('transitionPrompt');
        if (prompt) prompt.remove();
        const overlay = document.getElementById('transitionOverlay');
        if (overlay) overlay.remove();
    }
}

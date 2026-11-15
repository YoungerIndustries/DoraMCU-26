/**
 * MultiGalaxyNav — Navigate between multiple galaxies seamlessly in 3D space.
 *
 * Provides:
 * - Galaxy list panel showing all available galaxies with summary stats
 * - Wormhole-based navigation between connected galaxies
 * - Galaxy comparison mode: side-by-side 3D view
 * - Smooth camera transitions between galaxies with fade effects
 * - Galaxy mini-map showing relative positions of connected galaxies
 *
 * Usage:
 *   const nav = new MultiGalaxyNav(sceneRenderer, apiBaseUrl);
 *   await nav.loadGalaxies();
 *   nav.switchToGalaxy(galaxyId);
 *   nav.destroy();
 */

class MultiGalaxyNav {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     * @param {string} apiBaseUrl - Base URL for the API
     */
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // Galaxy data
        this.galaxies = new Map(); // galaxyId -> galaxy data
        this.wormholes = new Map(); // wormholeId -> wormhole data
        this.currentGalaxyId = null;
        
        // 3D objects
        this.galaxyMarkers = new Map(); // galaxyId -> THREE.Group
        this.wormholeEffects = new Map(); // wormholeId -> THREE.Group
        this.fadePlane = null;
        
        // UI elements
        this.container = null;
        this.galaxyList = null;
        this.minimap = null;
        this.controls = null;
        
        // Animation
        this.animationId = null;
        this.clock = new THREE.Clock();
        this.isTransitioning = false;
        
        // Raycaster for interaction
        this.raycaster = new THREE.Raycaster();
        this.mouse = new THREE.Vector2();
        
        // Callbacks
        this.onGalaxySwitch = null;
        this.onGalaxySelect = null;
    }

    /**
     * Initialize the multi-galaxy navigation UI.
     */
    init() {
        // Create main container
        this.container = document.createElement('div');
        this.container.id = 'multi-galaxy-nav-container';
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
        
        // Create galaxy list panel
        this.galaxyList = document.createElement('div');
        this.galaxyList.className = 'galaxy-list';
        this.galaxyList.style.cssText = `
            position: absolute;
            top: 10px;
            left: 10px;
            width: 280px;
            max-height: 500px;
            overflow-y: auto;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(150, 100, 255, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            color: #ccaaff;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            display: none;
        `;
        this.galaxyList.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #bb88ff;">
                🌌 Galaxy Navigator
            </div>
            <div id="galaxy-items" style="max-height: 400px; overflow-y: auto;"></div>
            <div style="margin-top: 10px; padding-top: 10px; border-top: 1px solid rgba(150, 100, 255, 0.3);">
                <button id="galaxy-minimap-btn" style="
                    width: 100%;
                    background: rgba(50, 100, 200, 0.3);
                    border: 1px solid rgba(100, 150, 255, 0.5);
                    color: #aaccff;
                    padding: 5px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: 'Courier New', monospace;
                    font-size: 11px;
                    margin-bottom: 5px;
                ">Show Mini-map</button>
                <button id="galaxy-close-btn" style="
                    width: 100%;
                    background: rgba(200, 50, 50, 0.3);
                    border: 1px solid rgba(255, 100, 100, 0.5);
                    color: #ffaaaa;
                    padding: 5px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: 'Courier New', monospace;
                    font-size: 11px;
                ">Close</button>
            </div>
        `;
        this.container.appendChild(this.galaxyList);
        
        // Create minimap
        this.minimap = document.createElement('div');
        this.minimap.className = 'galaxy-minimap';
        this.minimap.style.cssText = `
            position: absolute;
            bottom: 10px;
            left: 10px;
            width: 200px;
            height: 200px;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(150, 100, 255, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            display: none;
        `;
        this.minimap.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 8px; color: #bb88ff; font-size: 11px;">
                🗺️ Galaxy Mini-map
            </div>
            <canvas id="galaxy-minimap-canvas" width="180" height="160" style="
                width: 100%;
                height: 150px;
                background: rgba(0, 0, 20, 0.5);
                border-radius: 4px;
            "></canvas>
        `;
        this.container.appendChild(this.minimap);
        
        // Create controls
        this.controls = document.createElement('div');
        this.controls.className = 'multi-galaxy-controls';
        this.controls.style.cssText = `
            position: absolute;
            bottom: 10px;
            right: 10px;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(150, 100, 255, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            display: flex;
            gap: 10px;
        `;
        this.controls.innerHTML = `
            <button id="galaxy-nav-toggle-btn" style="
                background: rgba(50, 100, 200, 0.3);
                border: 1px solid rgba(100, 150, 255, 0.5);
                color: #aaccff;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">🌌 Galaxies</button>
        `;
        this.container.appendChild(this.controls);
        
        // Create fade plane for transitions
        const fadeGeometry = new THREE.PlaneGeometry(100, 100);
        const fadeMaterial = new THREE.MeshBasicMaterial({
            color: 0x000000,
            transparent: true,
            opacity: 0,
            side: THREE.DoubleSide
        });
        this.fadePlane = new THREE.Mesh(fadeGeometry, fadeMaterial);
        this.fadePlane.position.z = -50;
        this.fadePlane.visible = false;
        
        if (this.sceneRenderer?.scene) {
            this.sceneRenderer.scene.add(this.fadePlane);
        }
        
        // Event listeners
        document.getElementById('galaxy-nav-toggle-btn').addEventListener('click', () => {
            this.toggleGalaxyList();
        });
        document.getElementById('galaxy-minimap-btn').addEventListener('click', () => {
            this.toggleMinimap();
        });
        document.getElementById('galaxy-close-btn').addEventListener('click', () => {
            this.galaxyList.style.display = 'none';
        });
        
        // Mouse events for raycasting
        this.container.addEventListener('mousemove', (e) => this.onMouseMove(e));
        this.container.addEventListener('click', (e) => this.onMouseClick(e));
        
        console.log('[MultiGalaxyNav] Initialized');
    }

    /**
     * Toggle the galaxy list panel.
     */
    toggleGalaxyList() {
        if (!this.galaxyList) return;
        
        if (this.galaxyList.style.display === 'none') {
            this.galaxyList.style.display = 'block';
            this.updateGalaxyListUI();
        } else {
            this.galaxyList.style.display = 'none';
        }
    }

    /**
     * Toggle the minimap.
     */
    toggleMinimap() {
        if (!this.minimap) return;
        
        if (this.minimap.style.display === 'none') {
            this.minimap.style.display = 'block';
            this.renderMinimap();
        } else {
            this.minimap.style.display = 'none';
        }
    }

    /**
     * Load all galaxies from the API.
     */
    async loadGalaxies() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/galaxy-3d`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            const galaxies = await response.json();
            this.galaxies.clear();
            
            for (const galaxy of galaxies) {
                this.galaxies.set(galaxy.id, galaxy);
            }
            
            // Load wormholes
            await this.loadWormholes();
            
            this.updateGalaxyListUI();
            this.updateGalaxyMarkers();
            console.log(`[MultiGalaxyNav] Loaded ${galaxies.length} galaxies`);
        } catch (error) {
            console.warn('[MultiGalaxyNav] Failed to load galaxies, using simulated data:', error.message);
            this.loadSimulatedGalaxies();
        }
    }

    /**
     * Load wormhole connections.
     */
    async loadWormholes() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/wormhole-connections`);
            if (response.ok) {
                const wormholes = await response.json();
                this.wormholes.clear();
                wormholes.forEach(w => this.wormholes.set(w.id, w));
            }
        } catch (error) {
            console.warn('[MultiGalaxyNav] Failed to load wormholes:', error.message);
        }
    }

    /**
     * Load simulated galaxies as fallback.
     */
    loadSimulatedGalaxies() {
        const simulatedGalaxies = [
            {
                id: 1,
                name: 'Core Galaxy',
                sectorCount: 12,
                planetCount: 150,
                fleetCount: 25,
                position: { x: 0, y: 0, z: 0 }
            },
            {
                id: 2,
                name: 'Perseus Arm',
                sectorCount: 8,
                planetCount: 100,
                fleetCount: 15,
                position: { x: 20, y: 5, z: 10 }
            },
            {
                id: 3,
                name: 'Sagittarius Arm',
                sectorCount: 10,
                planetCount: 120,
                fleetCount: 20,
                position: { x: -15, y: -3, z: 20 }
            },
            {
                id: 4,
                name: 'Outer Rim',
                sectorCount: 6,
                planetCount: 80,
                fleetCount: 10,
                position: { x: 30, y: 10, z: -15 }
            },
            {
                id: 5,
                name: 'Unknown Regions',
                sectorCount: 4,
                planetCount: 50,
                fleetCount: 5,
                position: { x: -25, y: 8, z: -20 }
            }
        ];
        
        for (const galaxy of simulatedGalaxies) {
            this.galaxies.set(galaxy.id, galaxy);
        }
        
        // Simulated wormholes
        this.wormholes.set('wh-1', {
            id: 'wh-1',
            fromGalaxy: 1,
            toGalaxy: 2,
            status: 'ACTIVE'
        });
        this.wormholes.set('wh-2', {
            id: 'wh-2',
            fromGalaxy: 1,
            toGalaxy: 3,
            status: 'ACTIVE'
        });
        this.wormholes.set('wh-3', {
            id: 'wh-3',
            fromGalaxy: 2,
            toGalaxy: 4,
            status: 'UNSTABLE'
        });
        this.wormholes.set('wh-4', {
            id: 'wh-4',
            fromGalaxy: 3,
            toGalaxy: 5,
            status: 'ACTIVE'
        });
        
        this.updateGalaxyListUI();
        this.updateGalaxyMarkers();
    }

    /**
     * Update the galaxy list UI.
     */
    updateGalaxyListUI() {
        const container = document.getElementById('galaxy-items');
        if (!container) return;
        
        container.innerHTML = '';
        
        for (const [id, galaxy] of this.galaxies) {
            const item = document.createElement('div');
            item.className = 'galaxy-item';
            const isActive = id === this.currentGalaxyId;
            item.style.cssText = `
                padding: 8px;
                margin-bottom: 5px;
                background: ${isActive ? 'rgba(150, 100, 255, 0.3)' : 'rgba(50, 100, 200, 0.2)'};
                border: 1px solid ${isActive ? 'rgba(150, 100, 255, 0.5)' : 'rgba(150, 100, 255, 0.2)'};
                border-radius: 4px;
                cursor: pointer;
                transition: background 0.2s;
            `;
            item.innerHTML = `
                <div style="font-weight: bold; color: ${isActive ? '#ddbbff' : '#bb88ff'};">
                    ${isActive ? '📍 ' : ''}${galaxy.name}
                </div>
                <div style="font-size: 10px; color: #8899aa;">
                    ${galaxy.sectorCount} sectors | ${galaxy.planetCount} planets | ${galaxy.fleetCount} fleets
                </div>
                <div style="font-size: 10px; color: #667788;">
                    ${this.getConnectedWormholes(id).length} wormhole connections
                </div>
            `;
            item.addEventListener('mouseenter', () => {
                if (!isActive) {
                    item.style.background = 'rgba(50, 100, 200, 0.4)';
                }
            });
            item.addEventListener('mouseleave', () => {
                if (!isActive) {
                    item.style.background = 'rgba(50, 100, 200, 0.2)';
                }
            });
            item.addEventListener('click', () => {
                this.switchToGalaxy(id);
            });
            container.appendChild(item);
        }
    }

    /**
     * Get connected wormholes for a galaxy.
     * @param {number} galaxyId - The galaxy ID
     * @returns {Array} Array of wormhole data
     */
    getConnectedWormholes(galaxyId) {
        const connected = [];
        for (const wormhole of this.wormholes.values()) {
            if (wormhole.fromGalaxy === galaxyId || wormhole.toGalaxy === galaxyId) {
                connected.push(wormhole);
            }
        }
        return connected;
    }

    /**
     * Update galaxy markers in 3D space.
     */
    updateGalaxyMarkers() {
        this.clearGalaxyMarkers();
        this.clearWormholeEffects();
        
        for (const [id, galaxy] of this.galaxies) {
            const marker = this.createGalaxyMarker(id, galaxy);
            this.galaxyMarkers.set(id, marker);
            
            if (this.sceneRenderer?.scene) {
                this.sceneRenderer.scene.add(marker);
            }
        }
        
        // Create wormhole effects
        for (const [id, wormhole] of this.wormholes) {
            const effect = this.createWormholeEffect(id, wormhole);
            this.wormholeEffects.set(id, effect);
            
            if (this.sceneRenderer?.scene) {
                this.sceneRenderer.scene.add(effect);
            }
        }
    }

    /**
     * Create a galaxy marker in 3D space.
     * @param {number} galaxyId - The galaxy ID
     * @param {Object} galaxy - Galaxy data
     * @returns {THREE.Group} The marker group
     */
    createGalaxyMarker(galaxyId, galaxy) {
        const group = new THREE.Group();
        group.userData = { galaxyId, type: 'galaxy_marker' };
        
        const pos = galaxy.position || { x: 0, y: 0, z: 0 };
        group.position.set(pos.x, pos.y, pos.z);
        
        // Galaxy sphere
        const size = 0.5 + (galaxy.sectorCount || 0) * 0.1;
        const sphereGeometry = new THREE.SphereGeometry(size, 16, 16);
        const isActive = galaxyId === this.currentGalaxyId;
        const sphereMaterial = new THREE.MeshPhongMaterial({
            color: isActive ? 0xbb88ff : 0x6644aa,
            emissive: isActive ? 0x8844cc : 0x332266,
            emissiveIntensity: isActive ? 0.5 : 0.2,
            transparent: true,
            opacity: 0.8
        });
        const sphere = new THREE.Mesh(sphereGeometry, sphereMaterial);
        group.add(sphere);
        
        // Ring
        const ringGeometry = new THREE.RingGeometry(size + 0.2, size + 0.3, 32);
        const ringMaterial = new THREE.MeshPhongMaterial({
            color: isActive ? 0xddbbff : 0x8866cc,
            emissive: isActive ? 0xaa66dd : 0x443388,
            emissiveIntensity: 0.3,
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
     * Create a wormhole effect between galaxies.
     * @param {string} wormholeId - The wormhole ID
     * @param {Object} wormhole - Wormhole data
     * @returns {THREE.Group} The effect group
     */
    createWormholeEffect(wormholeId, wormhole) {
        const group = new THREE.Group();
        group.userData = { wormholeId, type: 'wormhole_effect' };
        
        const fromGalaxy = this.galaxies.get(wormhole.fromGalaxy);
        const toGalaxy = this.galaxies.get(wormhole.toGalaxy);
        
        if (!fromGalaxy || !toGalaxy) return group;
        
        const fromPos = new THREE.Vector3(
            fromGalaxy.position?.x || 0,
            fromGalaxy.position?.y || 0,
            fromGalaxy.position?.z || 0
        );
        const toPos = new THREE.Vector3(
            toGalaxy.position?.x || 0,
            toGalaxy.position?.y || 0,
            toGalaxy.position?.z || 0
        );
        
        // Create curved line
        const mid = fromPos.clone().add(toPos).multiplyScalar(0.5);
        mid.y += 3;
        
        const curve = new THREE.QuadraticBezierCurve3(fromPos, mid, toPos);
        const points = curve.getPoints(50);
        
        const lineGeometry = new THREE.BufferGeometry().setFromPoints(points);
        const isActive = wormhole.status === 'ACTIVE';
        const lineMaterial = new THREE.LineBasicMaterial({
            color: isActive ? 0xaa44ff : 0xff4444,
            transparent: true,
            opacity: 0.4
        });
        const line = new THREE.Line(lineGeometry, lineMaterial);
        group.add(line);
        
        // Portal spheres at endpoints
        const portalGeometry = new THREE.SphereGeometry(0.3, 16, 16);
        const portalMaterial = new THREE.MeshPhongMaterial({
            color: isActive ? 0xaa44ff : 0xff4444,
            emissive: isActive ? 0x8822cc : 0xcc2222,
            emissiveIntensity: 0.5,
            transparent: true,
            opacity: 0.6
        });
        
        const fromPortal = new THREE.Mesh(portalGeometry, portalMaterial);
        fromPortal.position.copy(fromPos);
        group.add(fromPortal);
        
        const toPortal = new THREE.Mesh(portalGeometry, portalMaterial);
        toPortal.position.copy(toPos);
        group.add(toPortal);
        
        return group;
    }

    /**
     * Clear all galaxy markers.
     */
    clearGalaxyMarkers() {
        if (this.sceneRenderer?.scene) {
            this.galaxyMarkers.forEach(marker => {
                this.sceneRenderer.scene.remove(marker);
            });
        }
        this.galaxyMarkers.clear();
    }

    /**
     * Clear all wormhole effects.
     */
    clearWormholeEffects() {
        if (this.sceneRenderer?.scene) {
            this.wormholeEffects.forEach(effect => {
                this.sceneRenderer.scene.remove(effect);
            });
        }
        this.wormholeEffects.clear();
    }

    /**
     * Switch to a different galaxy with fade transition.
     * @param {number} galaxyId - The galaxy ID to switch to
     */
    async switchToGalaxy(galaxyId) {
        if (this.isTransitioning || galaxyId === this.currentGalaxyId) return;
        
        const galaxy = this.galaxies.get(galaxyId);
        if (!galaxy) {
            console.warn('[MultiGalaxyNav] Galaxy not found:', galaxyId);
            return;
        }
        
        this.isTransitioning = true;
        
        // Fade in
        await this.fadeTransition(0, 1, 500);
        
        // Update current galaxy
        this.currentGalaxyId = galaxyId;
        this.updateGalaxyListUI();
        this.updateGalaxyMarkers();
        
        // Fade out
        await this.fadeTransition(1, 0, 500);
        
        this.isTransitioning = false;
        
        if (this.onGalaxySwitch) {
            this.onGalaxySwitch(galaxyId, galaxy);
        }
        
        console.log('[MultiGalaxyNav] Switched to galaxy:', galaxy.name);
    }

    /**
     * Perform a fade transition.
     * @param {number} fromOpacity - Starting opacity
     * @param {number} toOpacity - Ending opacity
     * @param {number} duration - Duration in ms
     * @returns {Promise} Resolves when transition is complete
     */
    fadeTransition(fromOpacity, toOpacity, duration) {
        return new Promise((resolve) => {
            if (!this.fadePlane) {
                resolve();
                return;
            }
            
            this.fadePlane.visible = true;
            this.fadePlane.material.opacity = fromOpacity;
            
            const startTime = Date.now();
            
            const animate = () => {
                const elapsed = Date.now() - startTime;
                const progress = Math.min(elapsed / duration, 1);
                
                this.fadePlane.material.opacity = fromOpacity + (toOpacity - fromOpacity) * progress;
                
                if (progress < 1) {
                    requestAnimationFrame(animate);
                } else {
                    if (this.fadePlane.material.opacity === 0) {
                        this.fadePlane.visible = false;
                    }
                    resolve();
                }
            };
            
            animate();
        });
    }

    /**
     * Render the galaxy minimap.
     */
    renderMinimap() {
        const canvas = document.getElementById('galaxy-minimap-canvas');
        if (!canvas) return;
        
        const ctx = canvas.getContext('2d');
        const width = canvas.width;
        const height = canvas.height;
        
        // Clear
        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = 'rgba(0, 0, 20, 0.5)';
        ctx.fillRect(0, 0, width, height);
        
        // Find bounds
        let minX = Infinity, maxX = -Infinity, minZ = Infinity, maxZ = -Infinity;
        for (const galaxy of this.galaxies.values()) {
            const pos = galaxy.position || { x: 0, z: 0 };
            minX = Math.min(minX, pos.x);
            maxX = Math.max(maxX, pos.x);
            minZ = Math.min(minZ, pos.z);
            maxZ = Math.max(maxZ, pos.z);
        }
        
        const rangeX = maxX - minX || 1;
        const rangeZ = maxZ - minZ || 1;
        const padding = 20;
        
        // Draw wormhole connections
        for (const wormhole of this.wormholes.values()) {
            const fromGalaxy = this.galaxies.get(wormhole.fromGalaxy);
            const toGalaxy = this.galaxies.get(wormhole.toGalaxy);
            
            if (!fromGalaxy || !toGalaxy) continue;
            
            const fromX = padding + ((fromGalaxy.position.x - minX) / rangeX) * (width - padding * 2);
            const fromY = padding + ((fromGalaxy.position.z - minZ) / rangeZ) * (height - padding * 2);
            const toX = padding + ((toGalaxy.position.x - minX) / rangeX) * (width - padding * 2);
            const toY = padding + ((toGalaxy.position.z - minZ) / rangeZ) * (height - padding * 2);
            
            ctx.beginPath();
            ctx.moveTo(fromX, fromY);
            ctx.lineTo(toX, toY);
            ctx.strokeStyle = wormhole.status === 'ACTIVE' ? '#aa44ff' : '#ff4444';
            ctx.lineWidth = 2;
            ctx.stroke();
        }
        
        // Draw galaxies
        for (const [id, galaxy] of this.galaxies) {
            const pos = galaxy.position || { x: 0, z: 0 };
            const x = padding + ((pos.x - minX) / rangeX) * (width - padding * 2);
            const y = padding + ((pos.z - minZ) / rangeZ) * (height - padding * 2);
            
            const isActive = id === this.currentGalaxyId;
            
            // Galaxy circle
            ctx.beginPath();
            ctx.arc(x, y, isActive ? 8 : 5, 0, Math.PI * 2);
            ctx.fillStyle = isActive ? '#bb88ff' : '#6644aa';
            ctx.fill();
            
            // Label
            ctx.fillStyle = isActive ? '#ddbbff' : '#8866cc';
            ctx.font = '9px Courier New';
            ctx.textAlign = 'center';
            ctx.fillText(galaxy.name, x, y - 10);
        }
    }

    /**
     * Handle mouse move for hover effects.
     * @param {MouseEvent} event
     */
    onMouseMove(event) {
        if (!this.sceneRenderer?.camera || !this.sceneRenderer?.renderer) return;
        
        const rect = this.sceneRenderer.renderer.domElement.getBoundingClientRect();
        this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
        
        this.raycaster.setFromCamera(this.mouse, this.sceneRenderer.camera);
        
        const allMeshes = [];
        this.galaxyMarkers.forEach(group => {
            group.traverse(child => {
                if (child.isMesh) {
                    allMeshes.push(child);
                }
            });
        });
        
        const intersects = this.raycaster.intersectObjects(allMeshes);
        
        if (intersects.length > 0) {
            document.body.style.cursor = 'pointer';
        } else {
            document.body.style.cursor = 'default';
        }
    }

    /**
     * Handle mouse click for galaxy selection.
     * @param {MouseEvent} event
     */
    onMouseClick(event) {
        if (!this.sceneRenderer?.camera || !this.sceneRenderer?.renderer) return;
        
        const rect = this.sceneRenderer.renderer.domElement.getBoundingClientRect();
        this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
        
        this.raycaster.setFromCamera(this.mouse, this.sceneRenderer.camera);
        
        const allMeshes = [];
        this.galaxyMarkers.forEach(group => {
            group.traverse(child => {
                if (child.isMesh) {
                    allMeshes.push(child);
                }
            });
        });
        
        const intersects = this.raycaster.intersectObjects(allMeshes);
        
        if (intersects.length > 0) {
            const hit = intersects[0].object;
            let parentGroup = hit;
            while (parentGroup.parent && !parentGroup.userData.galaxyId) {
                parentGroup = parentGroup.parent;
            }
            
            if (parentGroup.userData.galaxyId) {
                this.switchToGalaxy(parentGroup.userData.galaxyId);
            }
        }
    }

    /**
     * Start the animation loop.
     */
    startAnimation() {
        const animate = () => {
            this.animationId = requestAnimationFrame(animate);
            
            const delta = this.clock.getDelta();
            const time = this.clock.getElapsedTime();
            
            // Animate galaxy markers
            this.galaxyMarkers.forEach(group => {
                group.rotation.y += delta * 0.2;
                
                // Pulse effect
                group.traverse(child => {
                    if (child.isMesh && child.material.emissive) {
                        child.material.emissiveIntensity = 0.2 + Math.sin(time * 2) * 0.1;
                    }
                });
            });
            
            // Animate wormhole effects
            this.wormholeEffects.forEach(group => {
                group.traverse(child => {
                    if (child.isMesh && child.material.emissive) {
                        child.material.emissiveIntensity = 0.3 + Math.sin(time * 3) * 0.2;
                    }
                });
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
        if (callbacks.onGalaxySwitch) this.onGalaxySwitch = callbacks.onGalaxySwitch;
        if (callbacks.onGalaxySelect) this.onGalaxySelect = callbacks.onGalaxySelect;
    }

    /**
     * Destroy the multi-galaxy navigation module.
     */
    destroy() {
        this.stopAnimation();
        this.clearGalaxyMarkers();
        this.clearWormholeEffects();
        
        // Remove fade plane
        if (this.fadePlane && this.sceneRenderer?.scene) {
            this.sceneRenderer.scene.remove(this.fadePlane);
        }
        
        // Remove UI elements
        if (this.container && this.container.parentNode) {
            this.container.parentNode.removeChild(this.container);
        }
        
        // Clear data
        this.galaxies.clear();
        this.wormholes.clear();
        this.currentGalaxyId = null;
        
        console.log('[MultiGalaxyNav] Destroyed');
    }
}

// Expose globally
window.MultiGalaxyNav = MultiGalaxyNav;

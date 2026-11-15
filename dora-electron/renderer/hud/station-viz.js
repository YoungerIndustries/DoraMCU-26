/**
 * StationViz — 3D space station visualization with operations display.
 *
 * Provides:
 * - Procedural station generation from SpaceStation data (size, modules, economy)
 * - Docking animation for ships approaching stations
 * - Visual indicators for station operations: refueling, repairs, trading
 * - Fuel tank level visualization with animated progress bars
 * - Station economy overlay showing trade goods and prices
 * - REST API integration with StationOperationsService
 *
 * Usage:
 *   const viz = new StationViz(sceneRenderer, apiBaseUrl);
 *   await viz.loadStations();
 *   viz.showStation(stationId);
 *   viz.destroy();
 */

class StationViz {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     * @param {string} apiBaseUrl - Base URL for the API
     */
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // Station data
        this.stations = new Map(); // stationId -> station data
        this.currentStationId = null;
        
        // 3D objects
        this.stationMeshes = new Map(); // stationId -> THREE.Group
        this.dockingShips = new Map(); // shipId -> THREE.Group
        this.operationEffects = new Map(); // effectId -> THREE.Group
        
        // UI elements
        this.container = null;
        this.infoPanel = null;
        this.stationList = null;
        this.controls = null;
        
        // Animation
        this.animationId = null;
        this.clock = new THREE.Clock();
        
        // Raycaster for interaction
        this.raycaster = new THREE.Raycaster();
        this.mouse = new THREE.Vector2();
        this.hoveredStation = null;
        
        // Callbacks
        this.onStationSelect = null;
        this.onDockingComplete = null;
    }

    /**
     * Initialize the station visualization UI.
     */
    init() {
        // Create main container
        this.container = document.createElement('div');
        this.container.id = 'station-viz-container';
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
        
        // Create station list panel
        this.stationList = document.createElement('div');
        this.stationList.className = 'station-list';
        this.stationList.style.cssText = `
            position: absolute;
            top: 10px;
            left: 10px;
            width: 250px;
            max-height: 400px;
            overflow-y: auto;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(255, 200, 100, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            color: #ffddaa;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            display: none;
        `;
        this.stationList.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #ffcc66;">
                🏗️ Space Stations
            </div>
            <div id="station-items" style="max-height: 350px; overflow-y: auto;"></div>
        `;
        this.container.appendChild(this.stationList);
        
        // Create info panel
        this.infoPanel = document.createElement('div');
        this.infoPanel.className = 'station-info-panel';
        this.infoPanel.style.cssText = `
            position: absolute;
            top: 10px;
            right: 10px;
            width: 300px;
            max-height: 500px;
            overflow-y: auto;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(255, 200, 100, 0.3);
            border-radius: 8px;
            padding: 15px;
            pointer-events: auto;
            color: #ffddaa;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            display: none;
        `;
        this.container.appendChild(this.infoPanel);
        
        // Create controls
        this.controls = document.createElement('div');
        this.controls.className = 'station-viz-controls';
        this.controls.style.cssText = `
            position: absolute;
            bottom: 10px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(255, 200, 100, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            display: flex;
            gap: 10px;
        `;
        this.controls.innerHTML = `
            <button id="station-toggle-btn" style="
                background: rgba(50, 100, 200, 0.3);
                border: 1px solid rgba(100, 150, 255, 0.5);
                color: #aaccff;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">🏗️ Stations</button>
            <button id="station-close-btn" style="
                background: rgba(200, 50, 50, 0.3);
                border: 1px solid rgba(255, 100, 100, 0.5);
                color: #ffaaaa;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">Close</button>
        `;
        this.container.appendChild(this.controls);
        
        // Event listeners
        document.getElementById('station-toggle-btn').addEventListener('click', () => {
            this.toggleStationList();
        });
        document.getElementById('station-close-btn').addEventListener('click', () => {
            this.destroy();
        });
        
        // Mouse events for raycasting
        this.container.addEventListener('mousemove', (e) => this.onMouseMove(e));
        this.container.addEventListener('click', (e) => this.onMouseClick(e));
        
        console.log('[StationViz] Initialized');
    }

    /**
     * Toggle the station list panel.
     */
    toggleStationList() {
        if (!this.stationList) return;
        
        if (this.stationList.style.display === 'none') {
            this.stationList.style.display = 'block';
            this.updateStationListUI();
        } else {
            this.stationList.style.display = 'none';
        }
    }

    /**
     * Load all stations from the API.
     */
    async loadStations() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/stations`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            const stations = await response.json();
            this.stations.clear();
            
            for (const station of stations) {
                this.stations.set(station.id, station);
            }
            
            this.updateStationListUI();
            this.updateStationMeshes();
            console.log(`[StationViz] Loaded ${stations.length} stations`);
        } catch (error) {
            console.warn('[StationViz] Failed to load stations, using simulated data:', error.message);
            this.loadSimulatedStations();
        }
    }

    /**
     * Load simulated stations as fallback.
     */
    loadSimulatedStations() {
        const simulatedStations = [
            {
                id: 1,
                name: 'Alpha Station',
                worldName: 'Core World',
                stationType: 'TRADING',
                operational: true,
                dockedShips: 3,
                maxShips: 10,
                fuelLevel: 75,
                hullLevel: 90,
                economy: {
                    goods: [
                        { name: 'Fuel', price: 100, stock: 500 },
                        { name: 'Food', price: 50, stock: 200 },
                        { name: 'Ammo', price: 200, stock: 100 }
                    ]
                }
            },
            {
                id: 2,
                name: 'Bravo Outpost',
                worldName: 'Perseus Arm',
                stationType: 'MILITARY',
                operational: true,
                dockedShips: 5,
                maxShips: 15,
                fuelLevel: 50,
                hullLevel: 70,
                economy: {
                    goods: [
                        { name: 'Fuel', price: 120, stock: 300 },
                        { name: 'Ammo', price: 180, stock: 500 }
                    ]
                }
            },
            {
                id: 3,
                name: 'Charlie Hub',
                worldName: 'Sagittarius Arm',
                stationType: 'RESEARCH',
                operational: false,
                dockedShips: 1,
                maxShips: 5,
                fuelLevel: 20,
                hullLevel: 40,
                economy: {
                    goods: [
                        { name: 'Fuel', price: 150, stock: 100 }
                    ]
                }
            }
        ];
        
        for (const station of simulatedStations) {
            this.stations.set(station.id, station);
        }
        
        this.updateStationListUI();
        this.updateStationMeshes();
    }

    /**
     * Update the station list UI.
     */
    updateStationListUI() {
        const container = document.getElementById('station-items');
        if (!container) return;
        
        container.innerHTML = '';
        
        for (const [id, station] of this.stations) {
            const item = document.createElement('div');
            item.className = 'station-item';
            const isActive = id === this.currentStationId;
            item.style.cssText = `
                padding: 8px;
                margin-bottom: 5px;
                background: ${isActive ? 'rgba(255, 200, 100, 0.3)' : 'rgba(50, 100, 200, 0.2)'};
                border: 1px solid ${isActive ? 'rgba(255, 200, 100, 0.5)' : 'rgba(255, 200, 100, 0.2)'};
                border-radius: 4px;
                cursor: pointer;
                transition: background 0.2s;
            `;
            item.innerHTML = `
                <div style="font-weight: bold; color: ${isActive ? '#ffddaa' : '#ffcc66'};">
                    ${isActive ? '📍 ' : ''}${station.name}
                </div>
                <div style="font-size: 10px; color: #8899aa;">
                    ${station.worldName} | ${station.stationType}
                </div>
                <div style="font-size: 10px; color: ${station.operational ? '#88ff88' : '#ff8888'};">
                    ${station.operational ? '✓ Operational' : '✗ Offline'}
                </div>
                <div style="font-size: 10px; color: #667788;">
                    ${station.dockedShips}/${station.maxShips} ships | Fuel: ${station.fuelLevel}%
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
                this.showStation(id);
            });
            container.appendChild(item);
        }
    }

    /**
     * Update station meshes in 3D space.
     */
    updateStationMeshes() {
        this.clearStationMeshes();
        
        for (const [id, station] of this.stations) {
            const mesh = this.createStationMesh(id, station);
            this.stationMeshes.set(id, mesh);
            
            if (this.sceneRenderer?.scene) {
                this.sceneRenderer.scene.add(mesh);
            }
        }
    }

    /**
     * Create a 3D station mesh.
     * @param {number} stationId - The station ID
     * @param {Object} station - Station data
     * @returns {THREE.Group} The station group
     */
    createStationMesh(stationId, station) {
        const group = new THREE.Group();
        group.userData = { stationId, type: 'station' };
        
        // Position based on station ID
        const angle = (stationId % 5) * Math.PI * 2 / 5;
        const radius = 8;
        group.position.set(
            Math.cos(angle) * radius,
            (stationId % 3) * 2 - 2,
            Math.sin(angle) * radius
        );
        
        // Station type determines appearance
        const isOperational = station.operational;
        const baseColor = isOperational ? 0xffcc66 : 0x664422;
        
        // Main platform (cylinder)
        const platformGeometry = new THREE.CylinderGeometry(1.5, 1.5, 0.3, 16);
        const platformMaterial = new THREE.MeshPhongMaterial({
            color: baseColor,
            specular: 0x888888,
            shininess: 40
        });
        const platform = new THREE.Mesh(platformGeometry, platformMaterial);
        group.add(platform);
        
        // Central hub
        const hubGeometry = new THREE.CylinderGeometry(0.5, 0.5, 1, 8);
        const hubMaterial = new THREE.MeshPhongMaterial({
            color: isOperational ? 0xffdd88 : 0x443311,
            emissive: isOperational ? 0x886622 : 0x221100,
            emissiveIntensity: isOperational ? 0.3 : 0.1
        });
        const hub = new THREE.Mesh(hubGeometry, hubMaterial);
        hub.position.y = 0.65;
        group.add(hub);
        
        // Docking arms based on maxShips
        const armCount = Math.min(station.maxShips, 8);
        for (let i = 0; i < armCount; i++) {
            const armAngle = (i / armCount) * Math.PI * 2;
            const armGeometry = new THREE.BoxGeometry(0.2, 0.1, 1);
            const armMaterial = new THREE.MeshPhongMaterial({
                color: 0x888888,
                specular: 0x666666,
                shininess: 30
            });
            const arm = new THREE.Mesh(armGeometry, armMaterial);
            arm.position.set(
                Math.cos(armAngle) * 1.8,
                0.15,
                Math.sin(armAngle) * 1.8
            );
            arm.rotation.y = armAngle;
            group.add(arm);
        }
        
        // Fuel tank indicator
        const fuelBarGeometry = new THREE.PlaneGeometry(1, 0.1);
        const fuelBarMaterial = new THREE.MeshPhongMaterial({
            color: 0x333333,
            side: THREE.DoubleSide
        });
        const fuelBar = new THREE.Mesh(fuelBarGeometry, fuelBarMaterial);
        fuelBar.position.set(0, -0.3, 0);
        fuelBar.rotation.x = -Math.PI / 2;
        group.add(fuelBar);
        
        // Fuel level fill
        const fuelLevel = (station.fuelLevel || 0) / 100;
        const fuelFillGeometry = new THREE.PlaneGeometry(1 * fuelLevel, 0.1);
        const fuelColor = fuelLevel > 0.5 ? 0x44ff44 : fuelLevel > 0.2 ? 0xffff44 : 0xff4444;
        const fuelFillMaterial = new THREE.MeshPhongMaterial({
            color: fuelColor,
            emissive: fuelColor,
            emissiveIntensity: 0.3,
            side: THREE.DoubleSide
        });
        const fuelFill = new THREE.Mesh(fuelFillGeometry, fuelFillMaterial);
        fuelFill.position.set(-(1 - fuelLevel) * 0.5, -0.29, 0);
        fuelFill.rotation.x = -Math.PI / 2;
        group.add(fuelFill);
        
        // Operation effects
        if (isOperational) {
            this.addOperationEffects(group, station);
        }
        
        return group;
    }

    /**
     * Add operation effects to a station.
     * @param {THREE.Group} group - The station group
     * @param {Object} station - Station data
     */
    addOperationEffects(group, station) {
        // Refueling effect (blue particles)
        if (station.stationType === 'TRADING') {
            const particleCount = 5;
            for (let i = 0; i < particleCount; i++) {
                const particleGeometry = new THREE.SphereGeometry(0.05, 8, 8);
                const particleMaterial = new THREE.MeshPhongMaterial({
                    color: 0x4488ff,
                    emissive: 0x2244aa,
                    emissiveIntensity: 0.5,
                    transparent: true,
                    opacity: 0.6
                });
                const particle = new THREE.Mesh(particleGeometry, particleMaterial);
                particle.userData = {
                    type: 'refuel_particle',
                    angle: (i / particleCount) * Math.PI * 2,
                    radius: 1.5,
                    speed: 0.5 + Math.random() * 0.5
                };
                group.add(particle);
            }
        }
        
        // Military effect (red beacon)
        if (station.stationType === 'MILITARY') {
            const beaconGeometry = new THREE.ConeGeometry(0.1, 0.5, 8);
            const beaconMaterial = new THREE.MeshPhongMaterial({
                color: 0xff4444,
                emissive: 0xaa0000,
                emissiveIntensity: 0.5
            });
            const beacon = new THREE.Mesh(beaconGeometry, beaconMaterial);
            beacon.position.y = 1.5;
            beacon.userData = { type: 'beacon' };
            group.add(beacon);
        }
        
        // Research effect (green glow)
        if (station.stationType === 'RESEARCH') {
            const glowGeometry = new THREE.SphereGeometry(0.3, 16, 16);
            const glowMaterial = new THREE.MeshPhongMaterial({
                color: 0x44ff44,
                emissive: 0x22aa22,
                emissiveIntensity: 0.3,
                transparent: true,
                opacity: 0.3
            });
            const glow = new THREE.Mesh(glowGeometry, glowMaterial);
            glow.position.y = 1;
            glow.userData = { type: 'research_glow' };
            group.add(glow);
        }
    }

    /**
     * Clear all station meshes.
     */
    clearStationMeshes() {
        if (this.sceneRenderer?.scene) {
            this.stationMeshes.forEach(mesh => {
                this.sceneRenderer.scene.remove(mesh);
            });
        }
        this.stationMeshes.clear();
    }

    /**
     * Show a specific station in detail.
     * @param {number} stationId - The station ID
     */
    showStation(stationId) {
        const station = this.stations.get(stationId);
        if (!station) {
            console.warn('[StationViz] Station not found:', stationId);
            return;
        }
        
        this.currentStationId = stationId;
        this.updateStationListUI();
        
        // Show info panel
        this.showInfoPanel(station);
        
        // Trigger callback
        if (this.onStationSelect) {
            this.onStationSelect(stationId, station);
        }
        
        console.log('[StationViz] Showing station:', station.name);
    }

    /**
     * Show the info panel for a station.
     * @param {Object} station - Station data
     */
    showInfoPanel(station) {
        if (!this.infoPanel) return;
        
        const economy = station.economy || {};
        const goods = economy.goods || [];
        
        this.infoPanel.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #ffcc66; font-size: 14px;">
                ${station.name}
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: #8899aa; font-size: 10px;">INFORMATION</div>
                <div>World: ${station.worldName}</div>
                <div>Type: ${station.stationType}</div>
                <div>Status: ${station.operational ? '✓ Operational' : '✗ Offline'}</div>
                <div>Docked: ${station.dockedShips}/${station.maxShips} ships</div>
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: #8899aa; font-size: 10px;">RESOURCES</div>
                <div>Fuel: ${station.fuelLevel}%</div>
                <div>Hull: ${station.hullLevel}%</div>
            </div>
            ${goods.length > 0 ? `
                <div style="margin-bottom: 8px;">
                    <div style="color: #8899aa; font-size: 10px;">MARKET</div>
                    ${goods.map(good => `
                        <div style="display: flex; justify-content: space-between;">
                            <span>${good.name}</span>
                            <span>${good.price} Ccr</span>
                            <span style="color: #667788;">(${good.stock} stock)</span>
                        </div>
                    `).join('')}
                </div>
            ` : ''}
            <div style="margin-top: 10px; padding-top: 10px; border-top: 1px solid rgba(255, 200, 100, 0.3);">
                <button onclick="window.hudController?.stationVizInstance?.simulateDocking(${station.id})" style="
                    width: 100%;
                    background: rgba(50, 150, 50, 0.3);
                    border: 1px solid rgba(100, 200, 100, 0.5);
                    color: #aaffaa;
                    padding: 5px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: 'Courier New', monospace;
                    margin-bottom: 5px;
                ">Simulate Docking</button>
                <button onclick="window.hudController?.stationVizInstance?.simulateRefuel(${station.id})" style="
                    width: 100%;
                    background: rgba(50, 100, 200, 0.3);
                    border: 1px solid rgba(100, 150, 255, 0.5);
                    color: #aaccff;
                    padding: 5px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: 'Courier New', monospace;
                ">Simulate Refuel</button>
            </div>
        `;
        this.infoPanel.style.display = 'block';
    }

    /**
     * Simulate a ship docking at a station.
     * @param {number} stationId - The station ID
     */
    simulateDocking(stationId) {
        const station = this.stations.get(stationId);
        if (!station || !this.sceneRenderer?.scene) return;
        
        const stationMesh = this.stationMeshes.get(stationId);
        if (!stationMesh) return;
        
        // Create docking ship
        const shipGroup = new THREE.Group();
        shipGroup.userData = { type: 'docking_ship', stationId };
        
        // Ship model
        const shipGeometry = new THREE.ConeGeometry(0.2, 0.6, 8);
        const shipMaterial = new THREE.MeshPhongMaterial({
            color: 0x888888,
            specular: 0x666666,
            shininess: 30
        });
        const ship = new THREE.Mesh(shipGeometry, shipMaterial);
        ship.rotation.x = Math.PI / 2;
        shipGroup.add(ship);
        
        // Start position (far from station)
        const startPos = stationMesh.position.clone();
        startPos.x += 5;
        startPos.z += 5;
        shipGroup.position.copy(startPos);
        
        this.sceneRenderer.scene.add(shipGroup);
        
        // Animate docking
        const endPos = stationMesh.position.clone();
        endPos.x += 2;
        endPos.z += 2;
        
        const duration = 2000;
        const startTime = Date.now();
        
        const animate = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(elapsed / duration, 1);
            
            // Smooth easing
            const eased = progress < 0.5 
                ? 2 * progress * progress 
                : 1 - Math.pow(-2 * progress + 2, 2) / 2;
            
            shipGroup.position.lerpVectors(startPos, endPos, eased);
            shipGroup.rotation.y += 0.02;
            
            if (progress < 1) {
                requestAnimationFrame(animate);
            } else {
                // Docking complete
                this.sceneRenderer.scene.remove(shipGroup);
                
                if (this.onDockingComplete) {
                    this.onDockingComplete(stationId);
                }
                
                console.log('[StationViz] Docking complete at:', station.name);
            }
        };
        
        animate();
    }

    /**
     * Simulate refueling at a station.
     * @param {number} stationId - The station ID
     */
    simulateRefuel(stationId) {
        const station = this.stations.get(stationId);
        if (!station) return;
        
        // Update fuel level
        station.fuelLevel = Math.min(100, station.fuelLevel + 25);
        
        // Update station mesh
        this.updateStationMeshes();
        
        // Update info panel
        this.showInfoPanel(station);
        
        console.log('[StationViz] Refueled at:', station.name, 'Fuel:', station.fuelLevel + '%');
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
        this.stationMeshes.forEach(group => {
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
            while (parentGroup.parent && !parentGroup.userData.stationId) {
                parentGroup = parentGroup.parent;
            }
            
            if (parentGroup.userData.stationId) {
                if (this.hoveredStation !== parentGroup) {
                    if (this.hoveredStation) {
                        this.hoveredStation.scale.setScalar(1);
                    }
                    this.hoveredStation = parentGroup;
                    this.hoveredStation.scale.setScalar(1.1);
                    document.body.style.cursor = 'pointer';
                }
            }
        } else {
            if (this.hoveredStation) {
                this.hoveredStation.scale.setScalar(1);
                this.hoveredStation = null;
            }
            document.body.style.cursor = 'default';
        }
    }

    /**
     * Handle mouse click for station selection.
     * @param {MouseEvent} event
     */
    onMouseClick(event) {
        if (!this.sceneRenderer?.camera || !this.sceneRenderer?.renderer) return;
        
        const rect = this.sceneRenderer.renderer.domElement.getBoundingClientRect();
        this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
        
        this.raycaster.setFromCamera(this.mouse, this.sceneRenderer.camera);
        
        const allMeshes = [];
        this.stationMeshes.forEach(group => {
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
            while (parentGroup.parent && !parentGroup.userData.stationId) {
                parentGroup = parentGroup.parent;
            }
            
            if (parentGroup.userData.stationId) {
                this.showStation(parentGroup.userData.stationId);
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
            
            // Animate stations
            this.stationMeshes.forEach(group => {
                group.rotation.y += delta * 0.1;
                
                // Animate particles
                group.traverse(child => {
                    if (child.isMesh && child.userData.type === 'refuel_particle') {
                        child.userData.angle += delta * child.userData.speed;
                        child.position.x = Math.cos(child.userData.angle) * child.userData.radius;
                        child.position.z = Math.sin(child.userData.angle) * child.userData.radius;
                        child.position.y = Math.sin(time * 2 + child.userData.angle) * 0.3;
                    }
                    
                    // Pulse beacon
                    if (child.isMesh && child.userData.type === 'beacon') {
                        child.material.emissiveIntensity = 0.3 + Math.sin(time * 3) * 0.2;
                    }
                    
                    // Pulse research glow
                    if (child.isMesh && child.userData.type === 'research_glow') {
                        child.material.opacity = 0.2 + Math.sin(time * 2) * 0.1;
                        child.scale.setScalar(1 + Math.sin(time * 1.5) * 0.1);
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
        if (callbacks.onStationSelect) this.onStationSelect = callbacks.onStationSelect;
        if (callbacks.onDockingComplete) this.onDockingComplete = callbacks.onDockingComplete;
    }

    /**
     * Destroy the station visualization and clean up resources.
     */
    destroy() {
        this.stopAnimation();
        this.clearStationMeshes();
        
        // Remove UI elements
        if (this.container && this.container.parentNode) {
            this.container.parentNode.removeChild(this.container);
        }
        
        // Clear data
        this.stations.clear();
        this.stationMeshes.clear();
        this.dockingShips.clear();
        this.operationEffects.clear();
        this.currentStationId = null;
        this.hoveredStation = null;
        
        console.log('[StationViz] Destroyed');
    }
}

// Expose globally
window.StationViz = StationViz;

/**
 * ShipViewer — 3D ship design visualization with procedural generation.
 *
 * Provides:
 * - Procedural 3D ship generation from ShipDesign data
 * - Support for ship classes: Scout, Cruiser, Corvette, Merchant, custom designs
 * - Visual representation of hull type, weapons, jump/fusion drives
 * - Ship comparison mode: side-by-side 3D view of two designs
 * - Click-to-inspect: show ship stats on hover/click
 * - REST API integration with ShipDesignService
 *
 * Usage:
 *   const viewer = new ShipViewer(sceneRenderer, apiBaseUrl);
 *   await viewer.loadDesigns();
 *   viewer.showDesign(designId);
 *   viewer.compareDesigns(designId1, designId2);
 *   viewer.destroy();
 */

class ShipViewer {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     * @param {string} apiBaseUrl - Base URL for the API
     */
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // Ship data
        this.designs = new Map(); // designId -> design data
        this.shipMeshes = new Map(); // designId -> THREE.Group
        this.comparisonMode = false;
        this.comparisonDesigns = [];
        
        // UI elements
        this.container = null;
        this.infoPanel = null;
        this.designList = null;
        this.controls = null;
        
        // Animation
        this.animationId = null;
        this.clock = new THREE.Clock();
        this.hoveredShip = null;
        
        // Ship type configurations
        this.shipTypes = {
            scout: {
                hullColor: 0x4488ff,
                scale: 0.6,
                length: 1.0,
                width: 0.4,
                height: 0.3
            },
            corvette: {
                hullColor: 0x44aaff,
                scale: 0.8,
                length: 1.5,
                width: 0.6,
                height: 0.4
            },
            cruiser: {
                hullColor: 0xff8844,
                scale: 1.2,
                length: 2.5,
                width: 1.0,
                height: 0.6
            },
            merchant: {
                hullColor: 0x88aa44,
                scale: 1.0,
                length: 2.0,
                width: 0.8,
                height: 0.5
            },
            capital: {
                hullColor: 0xff4444,
                scale: 1.5,
                length: 3.5,
                width: 1.5,
                height: 0.8
            }
        };
        
        // Raycaster for interaction
        this.raycaster = new THREE.Raycaster();
        this.mouse = new THREE.Vector2();
        
        // Callbacks
        this.onDesignSelect = null;
        this.onDesignHover = null;
    }

    /**
     * Initialize the ship viewer UI.
     */
    init() {
        // Create main container
        this.container = document.createElement('div');
        this.container.id = 'ship-viewer-container';
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
        
        // Create design list panel
        this.designList = document.createElement('div');
        this.designList.className = 'ship-design-list';
        this.designList.style.cssText = `
            position: absolute;
            top: 10px;
            left: 10px;
            width: 250px;
            max-height: 400px;
            overflow-y: auto;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(100, 150, 255, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            color: #aaccff;
            font-family: 'Courier New', monospace;
            font-size: 12px;
        `;
        this.designList.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #88aaff;">
                🚀 Ship Designs
            </div>
            <div id="ship-design-items" style="max-height: 350px; overflow-y: auto;"></div>
        `;
        this.container.appendChild(this.designList);
        
        // Create info panel
        this.infoPanel = document.createElement('div');
        this.infoPanel.className = 'ship-info-panel';
        this.infoPanel.style.cssText = `
            position: absolute;
            top: 10px;
            right: 10px;
            width: 300px;
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(100, 150, 255, 0.3);
            border-radius: 8px;
            padding: 15px;
            pointer-events: auto;
            color: #aaccff;
            font-family: 'Courier New', monospace;
            font-size: 12px;
            display: none;
        `;
        this.container.appendChild(this.infoPanel);
        
        // Create controls
        this.controls = document.createElement('div');
        this.controls.className = 'ship-viewer-controls';
        this.controls.style.cssText = `
            position: absolute;
            bottom: 10px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0, 0, 20, 0.85);
            border: 1px solid rgba(100, 150, 255, 0.3);
            border-radius: 8px;
            padding: 10px;
            pointer-events: auto;
            display: flex;
            gap: 10px;
        `;
        this.controls.innerHTML = `
            <button id="ship-compare-btn" style="
                background: rgba(50, 100, 200, 0.3);
                border: 1px solid rgba(100, 150, 255, 0.5);
                color: #aaccff;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">Compare Mode</button>
            <button id="ship-reset-view-btn" style="
                background: rgba(50, 100, 200, 0.3);
                border: 1px solid rgba(100, 150, 255, 0.5);
                color: #aaccff;
                padding: 5px 15px;
                border-radius: 4px;
                cursor: pointer;
                font-family: 'Courier New', monospace;
            ">Reset View</button>
            <button id="ship-close-btn" style="
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
        document.getElementById('ship-compare-btn').addEventListener('click', () => {
            this.toggleComparisonMode();
        });
        document.getElementById('ship-reset-view-btn').addEventListener('click', () => {
            this.resetView();
        });
        document.getElementById('ship-close-btn').addEventListener('click', () => {
            this.destroy();
        });
        
        // Mouse events for raycasting
        this.container.addEventListener('mousemove', (e) => this.onMouseMove(e));
        this.container.addEventListener('click', (e) => this.onMouseClick(e));
        
        console.log('[ShipViewer] Initialized');
    }

    /**
     * Load all ship designs from the API.
     */
    async loadDesigns() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/traveller/shipdesigns`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            
            const designs = await response.json();
            this.designs.clear();
            
            for (const design of designs) {
                this.designs.set(design.id, design);
            }
            
            this.updateDesignList();
            console.log(`[ShipViewer] Loaded ${designs.length} designs`);
        } catch (error) {
            console.warn('[ShipViewer] Failed to load designs, using presets:', error.message);
            this.loadPresetDesigns();
        }
    }

    /**
     * Load preset ship designs as fallback.
     */
    loadPresetDesigns() {
        const presets = [
            {
                id: 'preset-scout',
                name: 'Scout',
                totalTonnage: 500,
                hullType: 'STANDARD',
                jumpRating: 4,
                maneuverRating: 2,
                powerPlant: 'STANDARD',
                lifeSupport: 'STANDARD',
                computerClass: 0,
                cargoTons: 50,
                passengerCount: 10,
                passengerClass: '3',
                fuelTons: 100,
                weapons: [{ type: 'SMALL_LASER', count: 1, arc: 'A1' }],
                stats: {
                    unallocatedTons: 0,
                    hullPoints: 120,
                    estimatedCrew: 15,
                    constructionCost: 2500000,
                    annualMaintenanceCost: 125000,
                    totalDamageDice: 1,
                    averageDamage: 3.5,
                    gunneryBonus: 0,
                    isValid: true
                }
            },
            {
                id: 'preset-cruiser',
                name: 'Cruiser',
                totalTonnage: 40000,
                hullType: 'REINFORCED',
                jumpRating: 4,
                maneuverRating: 3,
                powerPlant: 'TRIPLE',
                lifeSupport: 'VACUUM',
                computerClass: 3,
                cargoTons: 4000,
                passengerCount: 200,
                passengerClass: '2',
                fuelTons: 4000,
                weapons: [
                    { type: 'LARGE_LASER', count: 6, arc: 'A1' },
                    { type: 'LARGE_LASER', count: 2, arc: 'A6' },
                    { type: 'STANDARD_MISSILE', count: 2, missiles: 12, arc: 'B1' }
                ],
                stats: {
                    unallocatedTons: 0,
                    hullPoints: 1200,
                    estimatedCrew: 450,
                    constructionCost: 800000000,
                    annualMaintenanceCost: 40000000,
                    totalDamageDice: 32,
                    averageDamage: 112.0,
                    gunneryBonus: 2,
                    isValid: true
                }
            },
            {
                id: 'preset-corvette',
                name: 'Corvette',
                totalTonnage: 2000,
                hullType: 'STANDARD',
                jumpRating: 2,
                maneuverRating: 1,
                powerPlant: 'STANDARD',
                lifeSupport: 'STANDARD',
                computerClass: 1,
                cargoTons: 200,
                passengerCount: 50,
                passengerClass: '3',
                fuelTons: 200,
                weapons: [{ type: 'SMALL_LASER', count: 1, arc: 'A1' }],
                stats: {
                    unallocatedTons: 0,
                    hullPoints: 400,
                    estimatedCrew: 40,
                    constructionCost: 10000000,
                    annualMaintenanceCost: 500000,
                    totalDamageDice: 1,
                    averageDamage: 3.5,
                    gunneryBonus: 0,
                    isValid: true
                }
            },
            {
                id: 'preset-merchant',
                name: 'Merchant',
                totalTonnage: 20000,
                hullType: 'STANDARD',
                jumpRating: 4,
                maneuverRating: 1,
                powerPlant: 'STANDARD',
                lifeSupport: 'STANDARD',
                computerClass: 1,
                cargoTons: 5000,
                passengerCount: 100,
                passengerClass: '4',
                fuelTons: 2000,
                weapons: [{ type: 'SMALL_LASER', count: 2, arc: 'A1' }],
                stats: {
                    unallocatedTons: 0,
                    hullPoints: 600,
                    estimatedCrew: 120,
                    constructionCost: 400000000,
                    annualMaintenanceCost: 20000000,
                    totalDamageDice: 2,
                    averageDamage: 7.0,
                    gunneryBonus: 0,
                    isValid: true
                }
            }
        ];
        
        for (const preset of presets) {
            this.designs.set(preset.id, preset);
        }
        
        this.updateDesignList();
    }

    /**
     * Update the design list UI.
     */
    updateDesignList() {
        const container = document.getElementById('ship-design-items');
        if (!container) return;
        
        container.innerHTML = '';
        
        for (const [id, design] of this.designs) {
            const item = document.createElement('div');
            item.className = 'ship-design-item';
            item.style.cssText = `
                padding: 8px;
                margin-bottom: 5px;
                background: rgba(50, 100, 200, 0.2);
                border: 1px solid rgba(100, 150, 255, 0.2);
                border-radius: 4px;
                cursor: pointer;
                transition: background 0.2s;
            `;
            item.innerHTML = `
                <div style="font-weight: bold; color: #88aaff;">${design.name}</div>
                <div style="font-size: 10px; color: #8899aa;">
                    ${design.totalTonnage.toLocaleString()} tons | J${design.jumpRating}/M${design.maneuverRating}
                </div>
            `;
            item.addEventListener('mouseenter', () => {
                item.style.background = 'rgba(50, 100, 200, 0.4)';
            });
            item.addEventListener('mouseleave', () => {
                item.style.background = 'rgba(50, 100, 200, 0.2)';
            });
            item.addEventListener('click', () => {
                this.showDesign(id);
            });
            container.appendChild(item);
        }
    }

    /**
     * Show a single ship design in 3D.
     * @param {string|number} designId - The design ID to show
     */
    showDesign(designId) {
        const design = this.designs.get(designId);
        if (!design) {
            console.warn('[ShipViewer] Design not found:', designId);
            return;
        }
        
        // Clear comparison mode
        this.comparisonMode = false;
        this.clearAllShips();
        
        // Create and position the ship
        const shipGroup = this.createShipMesh(design, 0, 0, 0);
        this.shipMeshes.set(designId, shipGroup);
        
        if (this.sceneRenderer?.scene) {
            this.sceneRenderer.scene.add(shipGroup);
        }
        
        // Show info panel
        this.showInfoPanel(design);
        
        // Trigger callback
        if (this.onDesignSelect) {
            this.onDesignSelect(design);
        }
        
        console.log('[ShipViewer] Showing design:', design.name);
    }

    /**
     * Compare two ship designs side by side.
     * @param {string|number} designId1 - First design ID
     * @param {string|number} designId2 - Second design ID
     */
    compareDesigns(designId1, designId2) {
        const design1 = this.designs.get(designId1);
        const design2 = this.designs.get(designId2);
        
        if (!design1 || !design2) {
            console.warn('[ShipViewer] One or both designs not found');
            return;
        }
        
        this.comparisonMode = true;
        this.clearAllShips();
        
        // Position ships side by side
        const ship1 = this.createShipMesh(design1, -3, 0, 0);
        const ship2 = this.createShipMesh(design2, 3, 0, 0);
        
        this.shipMeshes.set(designId1, ship1);
        this.shipMeshes.set(designId2, ship2);
        this.comparisonDesigns = [design1, design2];
        
        if (this.sceneRenderer?.scene) {
            this.sceneRenderer.scene.add(ship1);
            this.sceneRenderer.scene.add(ship2);
        }
        
        // Show comparison info
        this.showComparisonPanel(design1, design2);
        
        console.log('[ShipViewer] Comparing:', design1.name, 'vs', design2.name);
    }

    /**
     * Toggle comparison mode.
     */
    toggleComparisonMode() {
        this.comparisonMode = !this.comparisonMode;
        const btn = document.getElementById('ship-compare-btn');
        if (btn) {
            btn.textContent = this.comparisonMode ? 'Exit Compare' : 'Compare Mode';
            btn.style.background = this.comparisonMode ? 
                'rgba(200, 150, 50, 0.3)' : 'rgba(50, 100, 200, 0.3)';
        }
        
        if (!this.comparisonMode && this.comparisonDesigns.length === 2) {
            // Exit comparison mode, show first design
            this.showDesign(this.shipMeshes.keys().next().value);
        }
    }

    /**
     * Reset the camera view.
     */
    resetView() {
        if (this.sceneRenderer?.camera) {
            this.sceneRenderer.camera.position.set(0, 5, 10);
            this.sceneRenderer.camera.lookAt(0, 0, 0);
        }
    }

    /**
     * Create a 3D ship mesh from design data.
     * @param {Object} design - The ship design data
     * @param {number} x - X position
     * @param {number} y - Y position
     * @param {number} z - Z position
     * @returns {THREE.Group} The ship group
     */
    createShipMesh(design, x, y, z) {
        const group = new THREE.Group();
        group.position.set(x, y, z);
        group.userData = { designId: design.id, designName: design.name };
        
        // Determine ship type based on tonnage
        let shipType = 'scout';
        if (design.totalTonnage > 30000) shipType = 'capital';
        else if (design.totalTonnage > 10000) shipType = 'cruiser';
        else if (design.totalTonnage > 1000) shipType = 'corvette';
        else if (design.totalTonnage > 500) shipType = 'merchant';
        
        const config = this.shipTypes[shipType];
        
        // Main hull
        const hullGeometry = new THREE.BoxGeometry(
            config.length * config.scale,
            config.height * config.scale,
            config.width * config.scale
        );
        const hullMaterial = new THREE.MeshPhongMaterial({
            color: config.hullColor,
            specular: 0x444444,
            shininess: 30
        });
        const hull = new THREE.Mesh(hullGeometry, hullMaterial);
        hull.userData = { part: 'hull' };
        group.add(hull);
        
        // Hull type indicator (reinforced hull has extra armor plates)
        if (design.hullType === 'REINFORCED') {
            const armorGeometry = new THREE.BoxGeometry(
                config.length * config.scale * 1.05,
                config.height * config.scale * 0.1,
                config.width * config.scale * 1.05
            );
            const armorMaterial = new THREE.MeshPhongMaterial({
                color: 0x666666,
                specular: 0x888888,
                shininess: 50
            });
            const topArmor = new THREE.Mesh(armorGeometry, armorMaterial);
            topArmor.position.y = config.height * config.scale * 0.55;
            group.add(topArmor);
            
            const bottomArmor = new THREE.Mesh(armorGeometry, armorMaterial);
            bottomArmor.position.y = -config.height * config.scale * 0.55;
            group.add(bottomArmor);
        }
        
        // Bridge/tower
        const bridgeGeometry = new THREE.BoxGeometry(
            config.length * config.scale * 0.3,
            config.height * config.scale * 0.8,
            config.width * config.scale * 0.5
        );
        const bridgeMaterial = new THREE.MeshPhongMaterial({
            color: 0x888888,
            specular: 0x666666,
            shininess: 40
        });
        const bridge = new THREE.Mesh(bridgeGeometry, bridgeMaterial);
        bridge.position.y = config.height * config.scale * 0.6;
        bridge.userData = { part: 'bridge' };
        group.add(bridge);
        
        // Jump drive (rear engines)
        const jumpDriveCount = Math.min(design.jumpRating, 6);
        const engineGeometry = new THREE.CylinderGeometry(
            config.width * config.scale * 0.15,
            config.width * config.scale * 0.2,
            config.length * config.scale * 0.3,
            8
        );
        const engineMaterial = new THREE.MeshPhongMaterial({
            color: 0x4488ff,
            emissive: 0x2244aa,
            emissiveIntensity: 0.5
        });
        
        for (let i = 0; i < Math.min(jumpDriveCount, 4); i++) {
            const engine = new THREE.Mesh(engineGeometry, engineMaterial);
            engine.rotation.x = Math.PI / 2;
            engine.position.set(
                -config.length * config.scale * 0.5 - 0.15,
                (i % 2 === 0 ? 1 : -1) * config.height * config.scale * 0.3,
                (i < 2 ? 1 : -1) * config.width * config.scale * 0.25
            );
            engine.userData = { part: 'jump_drive' };
            group.add(engine);
        }
        
        // Fusion drive (front thrusters)
        const fusionRating = design.maneuverRating || 0;
        if (fusionRating > 0) {
            const thrusterGeometry = new THREE.ConeGeometry(
                config.width * config.scale * 0.1,
                config.length * config.scale * 0.2,
                8
            );
            const thrusterMaterial = new THREE.MeshPhongMaterial({
                color: 0xff8844,
                emissive: 0xaa4400,
                emissiveIntensity: 0.3
            });
            
            for (let i = 0; i < Math.min(fusionRating, 2); i++) {
                const thruster = new THREE.Mesh(thrusterGeometry, thrusterMaterial);
                thruster.rotation.x = -Math.PI / 2;
                thruster.position.set(
                    config.length * config.scale * 0.5 + 0.1,
                    (i === 0 ? 1 : -1) * config.height * config.scale * 0.2,
                    0
                );
                thruster.userData = { part: 'fusion_drive' };
                group.add(thruster);
            }
        }
        
        // Weapon systems
        if (design.weapons && design.weapons.length > 0) {
            const weaponGeometry = new THREE.CylinderGeometry(0.05, 0.05, 0.3, 8);
            const weaponMaterial = new THREE.MeshPhongMaterial({
                color: 0xff4444,
                emissive: 0xaa0000,
                emissiveIntensity: 0.3
            });
            
            design.weapons.forEach((weapon, idx) => {
                const weaponCount = Math.min(weapon.count || 1, 4);
                for (let w = 0; w < weaponCount; w++) {
                    const weaponMesh = new THREE.Mesh(weaponGeometry, weaponMaterial);
                    
                    // Position weapons on different arcs
                    let arcOffset = 0;
                    if (weapon.arc && weapon.arc.includes('A')) {
                        arcOffset = config.length * config.scale * 0.3;
                    } else if (weapon.arc && weapon.arc.includes('B')) {
                        arcOffset = -config.length * config.scale * 0.3;
                    }
                    
                    weaponMesh.rotation.z = Math.PI / 2;
                    weaponMesh.position.set(
                        arcOffset,
                        config.height * config.scale * 0.5 + 0.1,
                        (w % 2 === 0 ? 1 : -1) * config.width * config.scale * 0.4
                    );
                    weaponMesh.userData = { part: 'weapon', weaponType: weapon.type };
                    group.add(weaponMesh);
                }
            });
        }
        
        // Cargo bay indicator (for merchants)
        if (design.cargoTons > 1000) {
            const cargoGeometry = new THREE.BoxGeometry(
                config.length * config.scale * 0.4,
                config.height * config.scale * 0.3,
                config.width * config.scale * 0.6
            );
            const cargoMaterial = new THREE.MeshPhongMaterial({
                color: 0x88aa44,
                transparent: true,
                opacity: 0.5
            });
            const cargo = new THREE.Mesh(cargoGeometry, cargoMaterial);
            cargo.position.y = -config.height * config.scale * 0.2;
            cargo.userData = { part: 'cargo' };
            group.add(cargo);
        }
        
        // Scale the entire group
        group.scale.setScalar(config.scale * 0.5);
        
        return group;
    }

    /**
     * Show the info panel for a ship design.
     * @param {Object} design - The ship design data
     */
    showInfoPanel(design) {
        if (!this.infoPanel) return;
        
        const stats = design.stats || {};
        this.infoPanel.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #88aaff; font-size: 14px;">
                ${design.name}
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: #8899aa; font-size: 10px;">SPECIFICATIONS</div>
                <div>Tonnage: ${design.totalTonnage.toLocaleString()} tons</div>
                <div>Hull: ${design.hullType}</div>
                <div>Jump: J${design.jumpRating} | Maneuver: M${design.maneuverRating}</div>
                <div>Power: ${design.powerPlant}</div>
                <div>Life Support: ${design.lifeSupport}</div>
                <div>Computer: Class ${design.computerClass}</div>
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: #8899aa; font-size: 10px;">CAPACITY</div>
                <div>Cargo: ${design.cargoTons.toLocaleString()} tons</div>
                <div>Fuel: ${design.fuelTons.toLocaleString()} tons</div>
                <div>Passengers: ${design.passengerCount} (Class ${design.passengerClass})</div>
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: #8899aa; font-size: 10px;">COMBAT</div>
                <div>Hull Points: ${stats.hullPoints || 'N/A'}</div>
                <div>Weapons: ${design.weapons ? design.weapons.length : 0} systems</div>
                <div>Damage: ${stats.totalDamageDice || 0}d6 (${stats.averageDamage || 0} avg)</div>
                <div>Gunnery: +${stats.gunneryBonus || 0}</div>
            </div>
            <div style="margin-bottom: 8px;">
                <div style="color: #8899aa; font-size: 10px;">COST</div>
                <div>Construction: ${(stats.constructionCost || 0).toLocaleString()} Ccr</div>
                <div>Maintenance: ${(stats.annualMaintenanceCost || 0).toLocaleString()} Ccr/yr</div>
                <div>Crew: ${stats.estimatedCrew || 'N/A'}</div>
            </div>
            ${this.comparisonMode ? `
                <div style="margin-top: 10px; padding-top: 10px; border-top: 1px solid rgba(100, 150, 255, 0.3);">
                    <button onclick="window.hudController?.shipViewerInstance?.toggleComparisonMode()" style="
                        width: 100%;
                        background: rgba(200, 150, 50, 0.3);
                        border: 1px solid rgba(255, 200, 100, 0.5);
                        color: #ffddaa;
                        padding: 5px;
                        border-radius: 4px;
                        cursor: pointer;
                        font-family: 'Courier New', monospace;
                    ">Exit Comparison</button>
                </div>
            ` : ''}
        `;
        this.infoPanel.style.display = 'block';
    }

    /**
     * Show comparison panel for two ships.
     * @param {Object} design1 - First ship design
     * @param {Object} design2 - Second ship design
     */
    showComparisonPanel(design1, design2) {
        if (!this.infoPanel) return;
        
        const stats1 = design1.stats || {};
        const stats2 = design2.stats || {};
        
        this.infoPanel.innerHTML = `
            <div style="font-weight: bold; margin-bottom: 10px; color: #88aaff; font-size: 14px;">
                Comparison
            </div>
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px; font-size: 11px;">
                <div style="color: #4488ff;">
                    <div style="font-weight: bold;">${design1.name}</div>
                    <div>${design1.totalTonnage.toLocaleString()} tons</div>
                    <div>J${design1.jumpRating}/M${design1.maneuverRating}</div>
                    <div>HP: ${stats1.hullPoints || 0}</div>
                    <div>Dmg: ${stats1.averageDamage || 0}</div>
                </div>
                <div style="color: #ff8844;">
                    <div style="font-weight: bold;">${design2.name}</div>
                    <div>${design2.totalTonnage.toLocaleString()} tons</div>
                    <div>J${design2.jumpRating}/M${design2.maneuverRating}</div>
                    <div>HP: ${stats2.hullPoints || 0}</div>
                    <div>Dmg: ${stats2.averageDamage || 0}</div>
                </div>
            </div>
            <div style="margin-top: 10px; padding-top: 10px; border-top: 1px solid rgba(100, 150, 255, 0.3);">
                <button onclick="window.hudController?.shipViewerInstance?.toggleComparisonMode()" style="
                    width: 100%;
                    background: rgba(200, 150, 50, 0.3);
                    border: 1px solid rgba(255, 200, 100, 0.5);
                    color: #ffddaa;
                    padding: 5px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-family: 'Courier New', monospace;
                ">Exit Comparison</button>
            </div>
        `;
        this.infoPanel.style.display = 'block';
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
        this.shipMeshes.forEach(group => {
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
            while (parentGroup.parent && !parentGroup.userData.designId) {
                parentGroup = parentGroup.parent;
            }
            
            if (parentGroup.userData.designId) {
                if (this.hoveredShip !== parentGroup) {
                    // Unhighlight previous
                    if (this.hoveredShip) {
                        this.hoveredShip.traverse(child => {
                            if (child.isMesh && child.material.emissive) {
                                child.material.emissiveIntensity = child.userData.part === 'jump_drive' ? 0.5 :
                                                                     child.userData.part === 'weapon' ? 0.3 : 0;
                            }
                        });
                    }
                    
                    this.hoveredShip = parentGroup;
                    
                    // Highlight current
                    parentGroup.traverse(child => {
                        if (child.isMesh && child.material.emissive) {
                            child.material.emissiveIntensity = 0.8;
                        }
                    });
                    
                    // Show tooltip
                    const design = this.designs.get(parentGroup.userData.designId);
                    if (design && this.onDesignHover) {
                        this.onDesignHover(design);
                    }
                }
            }
        } else {
            if (this.hoveredShip) {
                this.hoveredShip.traverse(child => {
                    if (child.isMesh && child.material.emissive) {
                        child.material.emissiveIntensity = child.userData.part === 'jump_drive' ? 0.5 :
                                                             child.userData.part === 'weapon' ? 0.3 : 0;
                    }
                });
                this.hoveredShip = null;
            }
        }
    }

    /**
     * Handle mouse click for selection.
     * @param {MouseEvent} event
     */
    onMouseClick(event) {
        if (!this.sceneRenderer?.camera || !this.sceneRenderer?.renderer) return;
        
        const rect = this.sceneRenderer.renderer.domElement.getBoundingClientRect();
        this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
        
        this.raycaster.setFromCamera(this.mouse, this.sceneRenderer.camera);
        
        const allMeshes = [];
        this.shipMeshes.forEach(group => {
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
            while (parentGroup.parent && !parentGroup.userData.designId) {
                parentGroup = parentGroup.parent;
            }
            
            if (parentGroup.userData.designId) {
                const design = this.designs.get(parentGroup.userData.designId);
                if (design) {
                    if (this.comparisonMode) {
                        // In comparison mode, select for comparison
                        if (this.comparisonDesigns.length < 2) {
                            this.comparisonDesigns.push(design);
                        }
                    } else {
                        this.showDesign(parentGroup.userData.designId);
                    }
                }
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
            
            // Rotate ships slowly
            this.shipMeshes.forEach(group => {
                group.rotation.y += delta * 0.2;
                
                // Pulse jump drive engines
                group.traverse(child => {
                    if (child.isMesh && child.userData.part === 'jump_drive' && child.material.emissive) {
                        child.material.emissiveIntensity = 0.3 + Math.sin(time * 2) * 0.2;
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
     * Clear all ship meshes from the scene.
     */
    clearAllShips() {
        if (this.sceneRenderer?.scene) {
            this.shipMeshes.forEach(group => {
                this.sceneRenderer.scene.remove(group);
            });
        }
        this.shipMeshes.clear();
        this.comparisonDesigns = [];
    }

    /**
     * Set callbacks for events.
     * @param {Object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        if (callbacks.onDesignSelect) this.onDesignSelect = callbacks.onDesignSelect;
        if (callbacks.onDesignHover) this.onDesignHover = callbacks.onDesignHover;
    }

    /**
     * Destroy the ship viewer and clean up resources.
     */
    destroy() {
        this.stopAnimation();
        this.clearAllShips();
        
        // Remove UI elements
        if (this.container && this.container.parentNode) {
            this.container.parentNode.removeChild(this.container);
        }
        
        // Clear data
        this.designs.clear();
        this.shipMeshes.clear();
        this.comparisonDesigns = [];
        this.comparisonMode = false;
        this.hoveredShip = null;
        
        console.log('[ShipViewer] Destroyed');
    }
}

// Expose globally
window.ShipViewer = ShipViewer;

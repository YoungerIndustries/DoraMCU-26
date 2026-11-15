/**
 * Tycoon 3D World Renderer using Babylon.js
 *
 * Renders the tycoon world in 3D with:
 * - Hex grid terrain
 * - Vehicles (trains, buses, trucks, ships, aircraft)
 * - Stations with cargo/passenger indicators
 * - Industries with production indicators
 * - NPC characters
 * - Camera controls (orbit, chase, cab views)
 */
(function () {
    'use strict';

    const Tycoon3D = {
        engine: null,
        scene: null,
        camera: null,
        canvas: null,
        vehicles: {},
        stations: {},
        industries: {},
        residential: {},
        hqHubs: {},
        npcs: {},
        aiCommanders: {},
        battlefieldUnits: {},
        hexMeshes: {},
        planetId: null,
        currentTurn: 0,
        viewMode: 'orbit', // orbit, chase, cab
        chaseTarget: null,
        updateInterval: null,

        /**
         * Initializes the 3D renderer.
         * @param {string} canvasId - The canvas element ID
         * @param {number} planetId - The planet ID
         */
        init: function (canvasId, planetId) {
            this.canvas = document.getElementById(canvasId);
            if (!this.canvas) {
                console.error('Canvas not found:', canvasId);
                return false;
            }

            this.planetId = planetId;

            // Initialize Babylon.js engine
            this.engine = new BABYLON.Engine(this.canvas, true, {
                preserveDrawingBuffer: true,
                stencil: true,
                disableWebGL2Support: false
            });

            // Create scene
            this.scene = this._createScene();

            // Resize handler
            window.addEventListener('resize', () => {
                this.engine.resize();
            });

            // Render loop
            this.engine.runRenderLoop(() => {
                this.scene.render();
            });

            // Start periodic state updates
            this.startStateUpdates();

            console.log('Tycoon 3D renderer initialized');
            return true;
        },

        /**
         * Creates the Babylon.js scene.
         */
        _createScene: function () {
            const scene = new BABYLON.Scene(this.engine);
            scene.clearColor = new BABYLON.Color4(0.1, 0.1, 0.15, 1);

            // Camera
            this.camera = new BABYLON.ArcRotateCamera(
                'camera',
                -Math.PI / 4,
                Math.PI / 3,
                100,
                BABYLON.Vector3.Zero(),
                scene
            );
            this.camera.attachControl(this.canvas, true);
            this.camera.lowerRadiusLimit = 20;
            this.camera.upperRadiusLimit = 500;
            this.camera.wheelPrecision = 50;

            // Lights
            const hemiLight = new BABYLON.HemisphericLight('hemi', new BABYLON.Vector3(0, 1, 0), scene);
            hemiLight.intensity = 0.6;
            hemiLight.diffuse = new BABYLON.Color3(1, 0.9, 0.8);
            hemiLight.groundColor = new BABYLON.Color3(0.2, 0.2, 0.3);

            const dirLight = new BABYLON.DirectionalLight('dir', new BABYLON.Vector3(-1, -2, 1), scene);
            dirLight.intensity = 0.8;
            dirLight.position = new BABYLON.Vector3(20, 40, -20);

            // Shadows
            const shadowGenerator = new BABYLON.ShadowGenerator(1024, dirLight);
            shadowGenerator.useBlurExponentialShadowMap = true;
            shadowGenerator.blurKernel = 32;

            // Skybox
            const skybox = BABYLON.MeshBuilder.CreateBox('skybox', { size: 1000 }, scene);
            const skyboxMaterial = new BABYLON.StandardMaterial('skyboxMaterial', scene);
            skyboxMaterial.backFaceCulling = false;
            skyboxMaterial.reflectionTexture = new BABYLON.CubeTexture(
                'textures/skybox/', scene
            );
            skyboxMaterial.reflectionTexture.coordinatesMode = BABYLON.Texture.SKYBOX_MODE;
            skyboxMaterial.diffuseColor = new BABYLON.Color3(0, 0, 0);
            skyboxMaterial.specularColor = new BABYLON.Color3(0, 0, 0);
            skybox.material = skyboxMaterial;

            // Ground plane
            const ground = BABYLON.MeshBuilder.CreateGround('ground', {
                width: 500, height: 500, subdivisions: 2
            }, scene);
            const groundMat = new BABYLON.StandardMaterial('groundMat', scene);
            groundMat.diffuseColor = new BABYLON.Color3(0.15, 0.25, 0.15);
            groundMat.specularColor = new BABYLON.Color3(0, 0, 0);
            ground.material = groundMat;
            ground.receiveShadows = true;

            // Fog
            scene.fogMode = BABYLON.Scene.FOGMODE_EXP2;
            scene.fogDensity = 0.002;
            scene.fogColor = new BABYLON.Color3(0.1, 0.1, 0.15);

            return scene;
        },

        /**
         * Converts Hex3D coordinates to Babylon.js 3D position.
         */
        hexTo3D: function (hex) {
            const hexSize = 10;
            const x = hexSize * (Math.sqrt(3) * hex.x + Math.sqrt(3) / 2.0 * hex.y);
            const z = hexSize * (3.0 / 2.0 * hex.y);
            const y = hex.z * 5;
            return new BABYLON.Vector3(x, y, z);
        },

        /**
         * Renders the hex grid terrain.
         * @param {Array} terrainData - Array of terrain hexes
         */
        renderTerrain: function (terrainData) {
            if (!this.scene) return;

            // Clear existing hex meshes
            for (const key in this.hexMeshes) {
                this.hexMeshes[key].dispose();
            }
            this.hexMeshes = {};

            const hexSize = 8;
            for (const terrain of terrainData) {
                const pos = this.hexTo3D(terrain.hex);
                const key = terrain.hex.x + ',' + terrain.hex.y + ',' + terrain.hex.z;

                let color;
                switch (terrain.type) {
                    case 'GRASSLAND': color = new BABYLON.Color3(0.3, 0.6, 0.2); break;
                    case 'DESERT': color = new BABYLON.Color3(0.8, 0.7, 0.4); break;
                    case 'WATER': color = new BABYLON.Color3(0.2, 0.4, 0.8); break;
                    case 'MOUNTAIN': color = new BABYLON.Color3(0.5, 0.5, 0.5); break;
                    case 'SNOW': color = new BABYLON.Color3(0.9, 0.9, 0.95); break;
                    default: color = new BABYLON.Color3(0.3, 0.5, 0.3);
                }

                // Create hexagonal prism
                const hex = BABYLON.MeshBuilder.CreatePolyhedricPrism(
                    'hex_' + key,
                    { radius: hexSize, height: 1, tessellation: 6 },
                    this.scene
                );
                hex.position = pos;

                const mat = new BABYLON.StandardMaterial('mat_' + key, this.scene);
                mat.diffuseColor = color;
                mat.specularColor = new BABYLON.Color3(0.1, 0.1, 0.1);
                hex.material = mat;
                hex.receiveShadows = true;

                this.hexMeshes[key] = hex;
            }
        },

        /**
         * Renders vehicles in the 3D world.
         * @param {Array} vehicles - Array of vehicle state objects
         */
        renderVehicles: function (vehicles) {
            if (!this.scene) return;

            // Remove vehicles no longer in the list
            const vehicleIds = new Set(vehicles.map(v => v.id));
            for (const id in this.vehicles) {
                if (!vehicleIds.has(id)) {
                    this.vehicles[id].dispose();
                    delete this.vehicles[id];
                }
            }

            for (const vehicle of vehicles) {
                const pos = this.hexTo3D(vehicle);
                let color, height;

                switch (vehicle.type) {
                    case 'Freight Train':
                    case 'Passenger Train':
                        color = new BABYLON.Color3(0.8, 0.2, 0.2);
                        height = 3;
                        break;
                    case 'Bus':
                        color = new BABYLON.Color3(0.2, 0.8, 0.2);
                        height = 2;
                        break;
                    case 'Truck':
                    case 'Express Truck':
                        color = new BABYLON.Color3(0.2, 0.2, 0.8);
                        height = 2;
                        break;
                    case 'Cargo Ship':
                    case 'Passenger Ferry':
                        color = new BABYLON.Color3(0.6, 0.6, 0.2);
                        height = 2;
                        break;
                    case 'Aircraft':
                        color = new BABYLON.Color3(0.9, 0.9, 0.9);
                        height = 15;
                        break;
                    default:
                        color = new BABYLON.Color3(0.5, 0.5, 0.5);
                        height = 2;
                }

                if (!this.vehicles[vehicle.id]) {
                    // Create vehicle mesh
                    const mesh = BABYLON.MeshBuilder.CreateBox(
                        'vehicle_' + vehicle.id,
                        { width: 4, height: height, depth: 2 },
                        this.scene
                    );
                    const mat = new BABYLON.StandardMaterial('vmat_' + vehicle.id, this.scene);
                    mat.diffuseColor = color;
                    mesh.material = mat;
                    mesh.castShadows = true;
                    this.vehicles[vehicle.id] = mesh;
                }

                // Update position
                this.vehicles[vehicle.id].position = pos;
            }
        },

        /**
         * Renders stations in the 3D world with type-specific models.
         * @param {Array} stations - Array of station state objects
         */
        renderStations: function (stations) {
            if (!this.scene) return;

            // Remove stations no longer in the list
            const stationIds = new Set(stations.map(s => s.id));
            for (const id in this.stations) {
                if (!stationIds.has(id)) {
                    if (this.stations[id].building) this.stations[id].building.dispose();
                    if (this.stations[id].extras) {
                        this.stations[id].extras.forEach(e => e.dispose());
                    }
                    delete this.stations[id];
                }
            }

            for (const station of stations) {
                const pos = this.hexTo3D(station);

                if (!this.stations[station.id]) {
                    const meshes = this._createStationModel(station.id, station.stationType);
                    meshes.building.position = pos;
                    if (meshes.extras) meshes.extras.forEach(m => m.position = pos);
                    this.stations[station.id] = meshes;
                }

                // Update position
                this.stations[station.id].building.position = pos;
                if (this.stations[station.id].extras) {
                    this.stations[station.id].extras.forEach(m => m.position = pos);
                }
            }
        },

        /**
         * Creates a type-specific 3D model for a station.
         */
        _createStationModel: function (id, stationType) {
            const scene = this.scene;
            const extras = [];

            switch (stationType) {
                case 'BUS_STOP': {
                    // Simple shelter with bench
                    const shelter = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 3, height: 2, depth: 2 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.3, 0.6, 0.3);
                    shelter.material = mat;
                    shelter.castShadows = true;
                    return { building: shelter, extras: extras };
                }
                case 'RAIL_STATION': {
                    // Platform + building
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 8, height: 4, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.6, 0.6, 0.65);
                    building.material = mat;
                    building.castShadows = true;
                    // Platform
                    const platform = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id + '_platform', { width: 10, height: 0.5, depth: 3 }, scene
                    );
                    const pmat = new BABYLON.StandardMaterial('smat_' + id + '_p', scene);
                    pmat.diffuseColor = new BABYLON.Color3(0.5, 0.5, 0.5);
                    platform.material = pmat;
                    platform.position.z = 5;
                    extras.push(platform);
                    return { building: building, extras: extras };
                }
                case 'MONORAIL_STATION': {
                    // Elevated platform
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 6, height: 3, depth: 5 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.5, 0.7);
                    building.material = mat;
                    building.position.y = 5;
                    building.castShadows = true;
                    // Support pillars
                    for (let i = 0; i < 2; i++) {
                        const pillar = BABYLON.MeshBuilder.CreateCylinder(
                            'station_' + id + '_pillar' + i, { diameter: 0.5, height: 5, tessellation: 8 }, scene
                        );
                        pillar.material = mat;
                        pillar.position.y = 2.5;
                        pillar.position.x = -2 + i * 4;
                        extras.push(pillar);
                    }
                    return { building: building, extras: extras };
                }
                case 'MAGLEV_STATION': {
                    // Futuristic elevated station
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 8, height: 3, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.4, 0.5, 0.8);
                    building.material = mat;
                    building.position.y = 6;
                    building.castShadows = true;
                    // Glow ring
                    const ring = BABYLON.MeshBuilder.CreateTorus(
                        'station_' + id + '_ring', { diameter: 6, thickness: 0.3, tessellation: 16 }, scene
                    );
                    const rmat = new BABYLON.StandardMaterial('smat_' + id + '_r', scene);
                    rmat.diffuseColor = new BABYLON.Color3(0.2, 0.6, 1);
                    rmat.emissiveColor = new BABYLON.Color3(0.1, 0.3, 0.5);
                    ring.material = rmat;
                    ring.position.y = 6;
                    ring.rotation.x = Math.PI / 2;
                    extras.push(ring);
                    return { building: building, extras: extras };
                }
                case 'BOAT_DOCK': {
                    // Dock platform
                    const dock = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 6, height: 0.5, depth: 4 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.4, 0.3);
                    dock.material = mat;
                    dock.castShadows = true;
                    return { building: dock, extras: extras };
                }
                case 'PORT': {
                    // Large port with cranes
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 10, height: 3, depth: 8 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.4, 0.4, 0.5);
                    building.material = mat;
                    building.castShadows = true;
                    // Cranes
                    for (let i = 0; i < 2; i++) {
                        const crane = BABYLON.MeshBuilder.CreateBox(
                            'station_' + id + '_crane' + i, { width: 0.5, height: 8, depth: 0.5 }, scene
                        );
                        const cmat = new BABYLON.StandardMaterial('smat_' + id + '_c' + i, scene);
                        cmat.diffuseColor = new BABYLON.Color3(0.8, 0.6, 0.2);
                        crane.material = cmat;
                        crane.position.x = -3 + i * 6;
                        crane.position.y = 4;
                        extras.push(crane);
                    }
                    return { building: building, extras: extras };
                }
                case 'AIRPORT': {
                    // Terminal + runway indicator
                    const terminal = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 12, height: 4, depth: 8 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.6, 0.65, 0.7);
                    terminal.material = mat;
                    terminal.castShadows = true;
                    // Runway
                    const runway = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id + '_runway', { width: 20, height: 0.2, depth: 3 }, scene
                    );
                    const rmat = new BABYLON.StandardMaterial('smat_' + id + '_r', scene);
                    rmat.diffuseColor = new BABYLON.Color3(0.3, 0.3, 0.3);
                    runway.material = rmat;
                    runway.position.z = 10;
                    extras.push(runway);
                    return { building: terminal, extras: extras };
                }
                case 'HELIPORT': {
                    // Small helipad
                    const pad = BABYLON.MeshBuilder.CreateCylinder(
                        'station_' + id, { diameter: 6, height: 0.3, tessellation: 16 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.4, 0.4, 0.4);
                    pad.material = mat;
                    pad.castShadows = true;
                    // H marking
                    const h = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id + '_h', { width: 2, height: 0.1, depth: 0.5 }, scene
                    );
                    const hmat = new BABYLON.StandardMaterial('smat_' + id + '_h', scene);
                    hmat.diffuseColor = new BABYLON.Color3(1, 1, 1);
                    h.material = hmat;
                    h.position.y = 0.2;
                    extras.push(h);
                    return { building: pad, extras: extras };
                }
                case 'SPACEPORT': {
                    // Large space port with launch tower
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 14, height: 6, depth: 10 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.55, 0.65);
                    building.material = mat;
                    building.castShadows = true;
                    // Launch tower
                    const tower = BABYLON.MeshBuilder.CreateCylinder(
                        'station_' + id + '_tower', { diameter: 1, height: 15, tessellation: 8 }, scene
                    );
                    const tmat = new BABYLON.StandardMaterial('smat_' + id + '_t', scene);
                    tmat.diffuseColor = new BABYLON.Color3(0.7, 0.7, 0.7);
                    tower.material = tmat;
                    tower.position.x = 8;
                    tower.position.y = 7;
                    extras.push(tower);
                    return { building: building, extras: extras };
                }
                case 'ORBITAL_STATION': {
                    // Orbital station (floating ring)
                    const ring = BABYLON.MeshBuilder.CreateTorus(
                        'station_' + id, { diameter: 12, thickness: 2, tessellation: 24 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.6, 0.65, 0.8);
                    ring.material = mat;
                    ring.castShadows = true;
                    ring.position.y = 20;
                    return { building: ring, extras: extras };
                }
                case 'CARGO_STATION': {
                    // Cargo-only warehouse
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 8, height: 4, depth: 7 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.45, 0.4);
                    building.material = mat;
                    building.castShadows = true;
                    return { building: building, extras: extras };
                }
                default: {
                    // General station
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'station_' + id, { width: 6, height: 4, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('smat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.7, 0.7, 0.7);
                    building.material = mat;
                    building.castShadows = true;
                    return { building: building, extras: extras };
                }
            }
        },

        /**
         * Renders industries in the 3D world with type-specific models.
         * @param {Array} industries - Array of industry state objects
         */
        renderIndustries: function (industries) {
            if (!this.scene) return;

            // Remove industries no longer in the list
            const industryIds = new Set(industries.map(i => i.id));
            for (const id in this.industries) {
                if (!industryIds.has(id)) {
                    if (this.industries[id].building) this.industries[id].building.dispose();
                    if (this.industries[id].extras) {
                        this.industries[id].extras.forEach(e => e.dispose());
                    }
                    delete this.industries[id];
                }
            }

            for (const industry of industries) {
                const pos = this.hexTo3D(industry);

                if (!this.industries[industry.id]) {
                    const meshes = this._createIndustryModel(industry.id, industry.typeEnum, industry.displayName);
                    meshes.building.position = pos;
                    meshes.extras.forEach(m => m.position = pos);
                    this.industries[industry.id] = meshes;
                }

                // Update position
                this.industries[industry.id].building.position = pos;
                if (this.industries[industry.id].extras) {
                    this.industries[industry.id].extras.forEach(m => m.position = pos);
                }
            }
        },

        /**
         * Creates a type-specific 3D model for an industry.
         */
        _createIndustryModel: function (id, typeEnum, displayName) {
            const scene = this.scene;
            const extras = [];

            // Determine model based on industry type enum
            switch (typeEnum) {

                // ---- Primary Industries (simple shapes) ----

                case 'MINE': {
                    // Cone + box entrance, brown/gray
                    const building = BABYLON.MeshBuilder.CreateCone(
                        'industry_' + id, { diameter: 8, height: 6, tessellation: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.4, 0.35, 0.3);
                    building.material = mat;
                    building.castShadows = true;
                    // Entrance box
                    const entrance = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_entrance', { width: 3, height: 2, depth: 1 }, scene
                    );
                    const emat = new BABYLON.StandardMaterial('imat_' + id + '_e', scene);
                    emat.diffuseColor = new BABYLON.Color3(0.2, 0.15, 0.1);
                    entrance.material = emat;
                    entrance.position.y = -2;
                    extras.push(entrance);
                    return { building: building, extras: extras };
                }

                case 'COAL_MINE': {
                    // Box with chimney, dark gray
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 5, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.25, 0.25, 0.25);
                    building.material = mat;
                    building.castShadows = true;
                    // Chimney
                    const chimney = BABYLON.MeshBuilder.CreateCylinder(
                        'industry_' + id + '_chimney', { diameter: 1.5, height: 8, tessellation: 8 }, scene
                    );
                    const cmat = new BABYLON.StandardMaterial('imat_' + id + '_c', scene);
                    cmat.diffuseColor = new BABYLON.Color3(0.15, 0.15, 0.15);
                    chimney.material = cmat;
                    chimney.position.y = 6;
                    chimney.position.x = 2;
                    extras.push(chimney);
                    return { building: building, extras: extras };
                }

                case 'OIL_WELL': {
                    // Derrick frame (thin boxes), black
                    const base = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 4, height: 1, depth: 4 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.1, 0.1, 0.1);
                    base.material = mat;
                    base.castShadows = true;
                    // Derrick pole
                    const pole = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_pole', { width: 0.5, height: 10, depth: 0.5 }, scene
                    );
                    pole.material = mat;
                    pole.position.y = 5;
                    extras.push(pole);
                    // Cross beam
                    const beam = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_beam', { width: 4, height: 0.3, depth: 0.3 }, scene
                    );
                    beam.material = mat;
                    beam.position.y = 9;
                    extras.push(beam);
                    return { building: base, extras: extras };
                }

                case 'LOGGING_CAMP': {
                    // Small cabin + wood piles, brown
                    const cabin = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 5, height: 3, depth: 4 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.35, 0.2);
                    cabin.material = mat;
                    cabin.castShadows = true;
                    // Wood pile
                    const wood = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_wood', { width: 3, height: 2, depth: 3 }, scene
                    );
                    const wmat = new BABYLON.StandardMaterial('imat_' + id + '_w', scene);
                    wmat.diffuseColor = new BABYLON.Color3(0.6, 0.45, 0.25);
                    wood.material = wmat;
                    wood.position.x = 4;
                    extras.push(wood);
                    return { building: cabin, extras: extras };
                }

                case 'FARM': {
                    // Barn (red box) + silo (cylinder), green fields
                    const barn = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 6, height: 4, depth: 5 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.7, 0.2, 0.2);
                    barn.material = mat;
                    barn.castShadows = true;
                    // Silo
                    const silo = BABYLON.MeshBuilder.CreateCylinder(
                        'industry_' + id + '_silo', { diameter: 2, height: 7, tessellation: 12 }, scene
                    );
                    const smat = new BABYLON.StandardMaterial('imat_' + id + '_s', scene);
                    smat.diffuseColor = new BABYLON.Color3(0.6, 0.6, 0.6);
                    silo.material = smat;
                    silo.position.x = 4;
                    silo.position.y = 1;
                    extras.push(silo);
                    return { building: barn, extras: extras };
                }

                case 'RANCH': {
                    // Fence posts + barn, tan
                    const barn = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 6, height: 3, depth: 5 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.75, 0.65, 0.5);
                    barn.material = mat;
                    barn.castShadows = true;
                    // Fence posts
                    for (let i = 0; i < 4; i++) {
                        const post = BABYLON.MeshBuilder.CreateBox(
                            'industry_' + id + '_post' + i, { width: 0.3, height: 2, depth: 0.3 }, scene
                        );
                        post.material = mat;
                        post.position.x = -5 + i * 3;
                        post.position.z = 4;
                        extras.push(post);
                    }
                    return { building: barn, extras: extras };
                }

                case 'FISHING_PORT': {
                    // Dock (flat box) + boat, blue
                    const dock = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 1, depth: 4 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.4, 0.3, 0.2);
                    dock.material = mat;
                    dock.castShadows = true;
                    // Boat
                    const boat = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_boat', { width: 4, height: 1.5, depth: 2 }, scene
                    );
                    const bmat = new BABYLON.StandardMaterial('imat_' + id + '_b', scene);
                    bmat.diffuseColor = new BABYLON.Color3(0.2, 0.4, 0.7);
                    boat.material = bmat;
                    boat.position.z = 4;
                    extras.push(boat);
                    return { building: dock, extras: extras };
                }

                case 'ORCHARD': {
                    // Tree spheres + small shed, green
                    const shed = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 4, height: 3, depth: 3 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.4, 0.3);
                    shed.material = mat;
                    shed.castShadows = true;
                    // Trees
                    for (let i = 0; i < 3; i++) {
                        const tree = BABYLON.MeshBuilder.CreateSphere(
                            'industry_' + id + '_tree' + i, { diameter: 2, segments: 8 }, scene
                        );
                        const tmat = new BABYLON.StandardMaterial('imat_' + id + '_t' + i, scene);
                        tmat.diffuseColor = new BABYLON.Color3(0.2, 0.5, 0.2);
                        tree.material = tmat;
                        tree.position.x = -4 + i * 4;
                        tree.position.y = 2;
                        extras.push(tree);
                    }
                    return { building: shed, extras: extras };
                }

                // ---- Secondary Industries (medium complexity) ----

                case 'STEEL_MILL': {
                    // Large box + tall chimney, dark gray with orange glow
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 10, height: 6, depth: 8 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.3, 0.3, 0.3);
                    building.material = mat;
                    building.castShadows = true;
                    // Chimney
                    const chimney = BABYLON.MeshBuilder.CreateCylinder(
                        'industry_' + id + '_chimney', { diameter: 2, height: 12, tessellation: 8 }, scene
                    );
                    const cmat = new BABYLON.StandardMaterial('imat_' + id + '_c', scene);
                    cmat.diffuseColor = new BABYLON.Color3(0.2, 0.2, 0.2);
                    chimney.material = cmat;
                    chimney.position.y = 8;
                    chimney.position.x = 3;
                    extras.push(chimney);
                    // Orange glow indicator
                    const glow = BABYLON.MeshBuilder.CreateSphere(
                        'industry_' + id + '_glow', { diameter: 1.5, segments: 8 }, scene
                    );
                    const gmat = new BABYLON.StandardMaterial('imat_' + id + '_g', scene);
                    gmat.diffuseColor = new BABYLON.Color3(1, 0.4, 0.1);
                    gmat.emissiveColor = new BABYLON.Color3(0.5, 0.2, 0.05);
                    glow.material = gmat;
                    glow.position.y = 1;
                    extras.push(glow);
                    return { building: building, extras: extras };
                }

                case 'CEMENT_PLANT': {
                    // Silo towers + box, light gray
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 4, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.6, 0.6, 0.6);
                    building.material = mat;
                    building.castShadows = true;
                    // Silo towers
                    for (let i = 0; i < 3; i++) {
                        const silo = BABYLON.MeshBuilder.CreateCylinder(
                            'industry_' + id + '_silo' + i, { diameter: 1.5, height: 8, tessellation: 8 }, scene
                        );
                        silo.material = mat;
                        silo.position.x = -3 + i * 3;
                        silo.position.y = 5;
                        extras.push(silo);
                    }
                    return { building: building, extras: extras };
                }

                case 'GLASS_FACTORY': {
                    // Box with transparent panels, light blue
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 5, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.7, 0.9);
                    mat.alpha = 0.7;
                    building.material = mat;
                    building.castShadows = true;
                    return { building: building, extras: extras };
                }

                case 'SAWMILL': {
                    // Box + saw blade indicator, brown
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 7, height: 4, depth: 5 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.35, 0.2);
                    building.material = mat;
                    building.castShadows = true;
                    // Saw blade (thin cylinder)
                    const blade = BABYLON.MeshBuilder.CreateCylinder(
                        'industry_' + id + '_blade', { diameter: 3, height: 0.2, tessellation: 16 }, scene
                    );
                    const bmat = new BABYLON.StandardMaterial('imat_' + id + '_b', scene);
                    bmat.diffuseColor = new BABYLON.Color3(0.7, 0.7, 0.7);
                    blade.material = bmat;
                    blade.rotation.z = Math.PI / 2;
                    blade.position.x = 4;
                    blade.position.y = 2;
                    extras.push(blade);
                    return { building: building, extras: extras };
                }

                case 'BAKERY': {
                    // Small box with chimney, warm yellow
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 5, height: 3, depth: 4 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.9, 0.8, 0.5);
                    building.material = mat;
                    building.castShadows = true;
                    // Chimney
                    const chimney = BABYLON.MeshBuilder.CreateCylinder(
                        'industry_' + id + '_chimney', { diameter: 0.8, height: 3, tessellation: 8 }, scene
                    );
                    const cmat = new BABYLON.StandardMaterial('imat_' + id + '_c', scene);
                    cmat.diffuseColor = new BABYLON.Color3(0.6, 0.4, 0.3);
                    chimney.material = cmat;
                    chimney.position.y = 3;
                    chimney.position.x = 1.5;
                    extras.push(chimney);
                    return { building: building, extras: extras };
                }

                case 'SLAUGHTERHOUSE': {
                    // Box with cold storage, white/gray
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 7, height: 4, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.7, 0.75, 0.8);
                    building.material = mat;
                    building.castShadows = true;
                    // Cold storage unit
                    const cold = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_cold', { width: 3, height: 3, depth: 3 }, scene
                    );
                    const cmat = new BABYLON.StandardMaterial('imat_' + id + '_c', scene);
                    cmat.diffuseColor = new BABYLON.Color3(0.5, 0.7, 0.9);
                    cold.material = cmat;
                    cold.position.x = 5;
                    extras.push(cold);
                    return { building: building, extras: extras };
                }

                case 'REFINERY': {
                    // Multiple towers + pipes, industrial gray
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 10, height: 3, depth: 8 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.4, 0.4, 0.4);
                    building.material = mat;
                    building.castShadows = true;
                    // Towers
                    for (let i = 0; i < 4; i++) {
                        const tower = BABYLON.MeshBuilder.CreateCylinder(
                            'industry_' + id + '_tower' + i, { diameter: 1.2, height: 6 + i * 2, tessellation: 8 }, scene
                        );
                        tower.material = mat;
                        tower.position.x = -4 + i * 3;
                        tower.position.y = 4 + i;
                        extras.push(tower);
                    }
                    return { building: building, extras: extras };
                }

                case 'CHEMICAL_PLANT': {
                    // Tanks + pipes, green/yellow hazard colors
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 3, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.4, 0.5, 0.2);
                    building.material = mat;
                    building.castShadows = true;
                    // Tanks
                    for (let i = 0; i < 3; i++) {
                        const tank = BABYLON.MeshBuilder.CreateSphere(
                            'industry_' + id + '_tank' + i, { diameter: 3, segments: 12 }, scene
                        );
                        const tmat = new BABYLON.StandardMaterial('imat_' + id + '_t' + i, scene);
                        tmat.diffuseColor = new BABYLON.Color3(0.6, 0.7, 0.2);
                        tank.material = tmat;
                        tank.position.x = -3 + i * 3;
                        tank.position.y = 3;
                        extras.push(tank);
                    }
                    return { building: building, extras: extras };
                }

                // ---- Tertiary Industries (high-tech, complex) ----

                case 'ELECTRONICS_FACTORY': {
                    // Sleek box with antenna, blue/white
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 5, depth: 7 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.3, 0.5, 0.8);
                    building.material = mat;
                    building.castShadows = true;
                    // Antenna
                    const antenna = BABYLON.MeshBuilder.CreateCylinder(
                        'industry_' + id + '_antenna', { diameter: 0.3, height: 6, tessellation: 6 }, scene
                    );
                    const am = new BABYLON.StandardMaterial('imat_' + id + '_a', scene);
                    am.diffuseColor = new BABYLON.Color3(0.8, 0.8, 0.8);
                    antenna.material = am;
                    antenna.position.y = 5;
                    antenna.position.x = 2;
                    extras.push(antenna);
                    return { building: building, extras: extras };
                }

                case 'MACHINE_SHOP': {
                    // Large box with crane, steel gray
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 10, height: 6, depth: 8 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.5, 0.55);
                    building.material = mat;
                    building.castShadows = true;
                    // Crane arm
                    const crane = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_crane', { width: 0.5, height: 8, depth: 0.5 }, scene
                    );
                    crane.material = mat;
                    crane.position.y = 6;
                    crane.position.x = -4;
                    extras.push(crane);
                    const arm = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_arm', { width: 6, height: 0.3, depth: 0.3 }, scene
                    );
                    arm.material = mat;
                    arm.position.y = 9;
                    arm.position.x = -1;
                    extras.push(arm);
                    return { building: building, extras: extras };
                }

                case 'LUXURY_FACTORY': {
                    // Elegant building, gold accents
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 5, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.8, 0.7, 0.4);
                    building.material = mat;
                    building.castShadows = true;
                    // Gold accent stripe
                    const stripe = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_stripe', { width: 8.2, height: 0.3, depth: 6.2 }, scene
                    );
                    const smat = new BABYLON.StandardMaterial('imat_' + id + '_s', scene);
                    smat.diffuseColor = new BABYLON.Color3(1, 0.84, 0);
                    smat.emissiveColor = new BABYLON.Color3(0.3, 0.25, 0);
                    stripe.material = smat;
                    stripe.position.y = 2;
                    extras.push(stripe);
                    return { building: building, extras: extras };
                }

                case 'ADVANCED_CHEMICALS': {
                    // Lab-style building, white with green
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 5, depth: 6 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.9, 0.9, 0.9);
                    building.material = mat;
                    building.castShadows = true;
                    // Green lab indicator
                    const indicator = BABYLON.MeshBuilder.CreateSphere(
                        'industry_' + id + '_indicator', { diameter: 1.5, segments: 8 }, scene
                    );
                    const imat = new BABYLON.StandardMaterial('imat_' + id + '_i', scene);
                    imat.diffuseColor = new BABYLON.Color3(0.2, 0.7, 0.2);
                    imat.emissiveColor = new BABYLON.Color3(0.1, 0.3, 0.1);
                    indicator.material = imat;
                    indicator.position.y = 3;
                    extras.push(indicator);
                    return { building: building, extras: extras };
                }

                // ---- Space Industries (futuristic) ----

                case 'ROCKET_PARTS_FACTORY': {
                    // Hangar-style, metallic
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 12, height: 6, depth: 8 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.55, 0.6);
                    building.material = mat;
                    building.castShadows = true;
                    return { building: building, extras: extras };
                }

                case 'SPACECRAFT_ASSEMBLY': {
                    // Large dome, white/silver
                    const dome = BABYLON.MeshBuilder.CreateSphere(
                        'industry_' + id, { diameter: 10, segments: 16 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.85, 0.85, 0.9);
                    dome.material = mat;
                    dome.castShadows = true;
                    dome.scaling.y = 0.6;
                    return { building: dome, extras: extras };
                }

                case 'LANDER_FACTORY': {
                    // Clean room style, white
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 4, depth: 7 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.95, 0.95, 0.95);
                    building.material = mat;
                    building.castShadows = true;
                    return { building: building, extras: extras };
                }

                case 'ROCKET_LAUNCH_PAD': {
                    // Launch tower + pad, gray with red accents
                    const pad = BABYLON.MeshBuilder.CreateCylinder(
                        'industry_' + id, { diameter: 6, height: 1, tessellation: 16 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.4, 0.4, 0.4);
                    pad.material = mat;
                    pad.castShadows = true;
                    // Launch tower
                    const tower = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_tower', { width: 1, height: 12, depth: 1 }, scene
                    );
                    const tmat = new BABYLON.StandardMaterial('imat_' + id + '_t', scene);
                    tmat.diffuseColor = new BABYLON.Color3(0.6, 0.6, 0.6);
                    tower.material = tmat;
                    tower.position.x = 4;
                    tower.position.y = 5;
                    extras.push(tower);
                    // Red accent
                    const accent = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id + '_accent', { width: 1.2, height: 0.5, depth: 1.2 }, scene
                    );
                    const am = new BABYLON.StandardMaterial('imat_' + id + '_a', scene);
                    am.diffuseColor = new BABYLON.Color3(0.8, 0.1, 0.1);
                    accent.material = am;
                    accent.position.x = 4;
                    accent.position.y = 11;
                    extras.push(accent);
                    return { building: pad, extras: extras };
                }

                // Default: generic factory
                default: {
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + id, { width: 8, height: 6, depth: 8 }, scene
                    );
                    const mat = new BABYLON.StandardMaterial('imat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.3, 0.2);
                    building.material = mat;
                    building.castShadows = true;
                    return { building: building, extras: extras };
                }
            }
        },

        /**
         * Renders residential buildings (apartment buildings) in the 3D world.
         * @param {Array} residential - Array of residential building state objects
         */
        renderResidential: function (residential) {
            if (!this.scene) return;

            // Remove buildings no longer in the list
            const buildingIds = new Set(residential.map(r => r.id));
            for (const id in this.residential) {
                if (!buildingIds.has(id)) {
                    if (this.residential[id].building) this.residential[id].building.dispose();
                    if (this.residential[id].extras) {
                        this.residential[id].extras.forEach(e => e.dispose());
                    }
                    delete this.residential[id];
                }
            }

            for (const building of residential) {
                const pos = this.hexTo3D(building);

                if (!this.residential[building.id]) {
                    const meshes = this._createResidentialModel(building.id, building.buildingType, building.floors);
                    meshes.building.position = pos;
                    if (meshes.extras) meshes.extras.forEach(m => m.position = pos);
                    this.residential[building.id] = meshes;
                }

                // Update position
                this.residential[building.id].building.position = pos;
                if (this.residential[building.id].extras) {
                    this.residential[building.id].extras.forEach(m => m.position = pos);
                }
            }
        },

        /**
         * Creates a 3D model for a residential building.
         */
        _createResidentialModel: function (id, buildingType, floors) {
            const scene = this.scene;
            const extras = [];

            switch (buildingType) {
                case 'APARTMENT_BUILDING': {
                    // Tall apartment building with windows, basement indicator, and rooftop
                    const height = Math.max(6, floors * 1.5);
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'residential_' + id,
                        { width: 8, height: height, depth: 6 },
                        scene
                    );
                    const mat = new BABYLON.StandardMaterial('rmat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.65, 0.68, 0.72);
                    building.material = mat;
                    building.position.y = height / 2;
                    building.castShadows = true;

                    // Window grid on all four sides
                    for (let floor = 0; floor < Math.min(floors, 10); floor++) {
                        for (let win = 0; win < 3; win++) {
                            // Front and back
                            for (let side = 0; side < 2; side++) {
                                const window = BABYLON.MeshBuilder.CreateBox(
                                    'res_' + id + '_win_' + floor + '_' + win + '_' + side,
                                    { width: 1.0, height: 0.7, depth: 0.1 },
                                    scene
                                );
                                const wmat = new BABYLON.StandardMaterial('rmat_' + id + '_w_' + floor + '_' + win + '_' + side, scene);
                                wmat.diffuseColor = new BABYLON.Color3(0.3, 0.5, 0.8);
                                wmat.emissiveColor = new BABYLON.Color3(0.1, 0.15, 0.25);
                                window.material = wmat;
                                window.position.x = -2.5 + win * 2.5;
                                window.position.y = 1 + floor * 1.5;
                                window.position.z = side === 0 ? 3.05 : -3.05;
                                extras.push(window);
                            }
                            // Left and right
                            for (let side = 0; side < 2; side++) {
                                const window = BABYLON.MeshBuilder.CreateBox(
                                    'res_' + id + '_win_side_' + floor + '_' + win + '_' + side,
                                    { width: 0.1, height: 0.7, depth: 1.0 },
                                    scene
                                );
                                const wmat = new BABYLON.StandardMaterial('rmat_' + id + '_ws_' + floor + '_' + win + '_' + side, scene);
                                wmat.diffuseColor = new BABYLON.Color3(0.3, 0.5, 0.8);
                                wmat.emissiveColor = new BABYLON.Color3(0.1, 0.15, 0.25);
                                window.material = wmat;
                                window.position.x = side === 0 ? 4.05 : -4.05;
                                window.position.y = 1 + floor * 1.5;
                                window.position.z = -2 + win * 2;
                                extras.push(window);
                            }
                        }
                    }

                    // Basement indicator: darker base extending below ground
                    const basementHeight = 2;
                    const basement = BABYLON.MeshBuilder.CreateBox(
                        'res_' + id + '_basement',
                        { width: 8.5, height: basementHeight, depth: 6.5 },
                        scene
                    );
                    const bmat = new BABYLON.StandardMaterial('rmat_' + id + '_b', scene);
                    bmat.diffuseColor = new BABYLON.Color3(0.2, 0.2, 0.25);
                    basement.material = bmat;
                    basement.position.y = -basementHeight / 2;
                    extras.push(basement);

                    // Rooftop antenna
                    const antenna = BABYLON.MeshBuilder.CreateCylinder(
                        'res_' + id + '_antenna',
                        { diameter: 0.2, height: 2, tessellation: 6 },
                        scene
                    );
                    const antMat = new BABYLON.StandardMaterial('rmat_' + id + '_ant', scene);
                    antMat.diffuseColor = new BABYLON.Color3(0.5, 0.5, 0.5);
                    antenna.material = antMat;
                    antenna.position.y = height + 1;
                    antenna.position.x = 2;
                    extras.push(antenna);

                    // Entrance canopy
                    const canopy = BABYLON.MeshBuilder.CreateBox(
                        'res_' + id + '_canopy',
                        { width: 3, height: 0.2, depth: 2 },
                        scene
                    );
                    const cmat = new BABYLON.StandardMaterial('rmat_' + id + '_c', scene);
                    cmat.diffuseColor = new BABYLON.Color3(0.4, 0.4, 0.45);
                    canopy.material = cmat;
                    canopy.position.y = 1.5;
                    canopy.position.z = 4;
                    extras.push(canopy);

                    // "APARTMENTS" sign
                    const sign = BABYLON.MeshBuilder.CreateBox(
                        'res_' + id + '_sign',
                        { width: 4, height: 0.8, depth: 0.1 },
                        scene
                    );
                    const smat = new BABYLON.StandardMaterial('rmat_' + id + '_s', scene);
                    smat.diffuseColor = new BABYLON.Color3(0.2, 0.4, 0.6);
                    smat.emissiveColor = new BABYLON.Color3(0.1, 0.2, 0.3);
                    sign.material = smat;
                    sign.position.y = height - 1;
                    sign.position.z = 3.05;
                    extras.push(sign);

                    break;
                }
                case 'RESIDENTIAL_HOUSE': {
                    // Small house
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'residential_' + id,
                        { width: 4, height: 3, depth: 3 },
                        scene
                    );
                    const mat = new BABYLON.StandardMaterial('rmat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.75, 0.65, 0.55);
                    building.material = mat;
                    building.castShadows = true;
                    return { building: building, extras: extras };
                }
                case 'TOWN_HOUSE': {
                    // Medium townhouse
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'residential_' + id,
                        { width: 5, height: 5, depth: 4 },
                        scene
                    );
                    const mat = new BABYLON.StandardMaterial('rmat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.65, 0.6, 0.55);
                    building.material = mat;
                    building.castShadows = true;
                    return { building: building, extras: extras };
                }
                default: {
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'residential_' + id,
                        { width: 5, height: 4, depth: 4 },
                        scene
                    );
                    const mat = new BABYLON.StandardMaterial('rmat_' + id, scene);
                    mat.diffuseColor = new BABYLON.Color3(0.6, 0.6, 0.6);
                    building.material = mat;
                    building.castShadows = true;
                    return { building: building, extras: extras };
                }
            }
        },

        /**
         * Renders Younger Industries HQ Hubs in the 3D world.
         * @param {Array} hqHubs - Array of HQ Hub state objects
         */
        renderHQHubs: function (hqHubs) {
            if (!this.scene) return;

            // Remove HQs no longer in the list
            const hqIds = new Set(hqHubs.map(h => h.id));
            for (const id in this.hqHubs) {
                if (!hqIds.has(id)) {
                    if (this.hqHubs[id].building) this.hqHubs[id].building.dispose();
                    if (this.hqHubs[id].extras) {
                        this.hqHubs[id].extras.forEach(e => e.dispose());
                    }
                    delete this.hqHubs[id];
                }
            }

            for (const hq of hqHubs) {
                const pos = this.hexTo3D(hq);

                if (!this.hqHubs[hq.id]) {
                    const meshes = this._createHQHubModel(hq.id, hq.officeFloors, hq.basementLevels, hq.totalRooms);
                    meshes.building.position = pos;
                    if (meshes.extras) meshes.extras.forEach(m => m.position = pos);
                    this.hqHubs[hq.id] = meshes;
                }

                // Update position
                this.hqHubs[hq.id].building.position = pos;
                if (this.hqHubs[hq.id].extras) {
                    this.hqHubs[hq.id].extras.forEach(m => m.position = pos);
                }
            }
        },

        /**
         * Creates a 3D model for a Younger Industries HQ Hub.
         * A tall corporate skyscraper with helipad, basement indicator, and corporate branding.
         */
        _createHQHubModel: function (id, officeFloors, basementLevels, totalRooms) {
            const scene = this.scene;
            const extras = [];

            // Main skyscraper tower
            const towerHeight = Math.max(8, officeFloors * 2.5 + 2);
            const building = BABYLON.MeshBuilder.CreateBox(
                'hq_' + id,
                { width: 10, height: towerHeight, depth: 10 },
                scene
            );
            const mat = new BABYLON.StandardMaterial('hqmat_' + id, scene);
            mat.diffuseColor = new BABYLON.Color3(0.25, 0.35, 0.55);
            building.material = mat;
            building.position.y = towerHeight / 2;
            building.castShadows = true;

            // Window grid on all four sides
            for (let floor = 0; floor < officeFloors; floor++) {
                for (let win = 0; win < 4; win++) {
                    // Front and back
                    for (let side = 0; side < 2; side++) {
                        const window = BABYLON.MeshBuilder.CreateBox(
                            'hq_' + id + '_win_' + floor + '_' + win + '_' + side,
                            { width: 1.2, height: 0.8, depth: 0.1 },
                            scene
                        );
                        const wmat = new BABYLON.StandardMaterial('hqmat_' + id + '_w_' + floor + '_' + win + '_' + side, scene);
                        wmat.diffuseColor = new BABYLON.Color3(0.4, 0.6, 0.9);
                        wmat.emissiveColor = new BABYLON.Color3(0.15, 0.2, 0.35);
                        window.material = wmat;
                        window.position.x = -3 + win * 2;
                        window.position.y = 1.5 + floor * 2.5;
                        window.position.z = side === 0 ? 5.05 : -5.05;
                        extras.push(window);
                    }
                    // Left and right
                    for (let side = 0; side < 2; side++) {
                        const window = BABYLON.MeshBuilder.CreateBox(
                            'hq_' + id + '_win_side_' + floor + '_' + win + '_' + side,
                            { width: 0.1, height: 0.8, depth: 1.2 },
                            scene
                        );
                        const wmat = new BABYLON.StandardMaterial('hqmat_' + id + '_ws_' + floor + '_' + win + '_' + side, scene);
                        wmat.diffuseColor = new BABYLON.Color3(0.4, 0.6, 0.9);
                        wmat.emissiveColor = new BABYLON.Color3(0.15, 0.2, 0.35);
                        window.material = wmat;
                        window.position.x = side === 0 ? 5.05 : -5.05;
                        window.position.y = 1.5 + floor * 2.5;
                        window.position.z = -3 + win * 2;
                        extras.push(window);
                    }
                }
            }

            // Helipad on roof
            const helipad = BABYLON.MeshBuilder.CreateCylinder(
                'hq_' + id + '_helipad',
                { diameter: 5, height: 0.3, tessellation: 16 },
                scene
            );
            const hmat = new BABYLON.StandardMaterial('hqmat_' + id + '_h', scene);
            hmat.diffuseColor = new BABYLON.Color3(0.3, 0.3, 0.3);
            helipad.material = hmat;
            helipad.position.y = towerHeight + 0.15;
            extras.push(helipad);

            // "H" marking on helipad
            const hMark = BABYLON.MeshBuilder.CreateBox(
                'hq_' + id + '_hmark',
                { width: 1.5, height: 0.05, depth: 0.4 },
                scene
            );
            const hmMat = new BABYLON.StandardMaterial('hqmat_' + id + '_hm', scene);
            hmMat.diffuseColor = new BABYLON.Color3(1, 1, 1);
            hMark.material = hmMat;
            hMark.position.y = towerHeight + 0.35;
            extras.push(hMark);

            // Corporate logo / antenna on roof
            const antenna = BABYLON.MeshBuilder.CreateCylinder(
                'hq_' + id + '_antenna',
                { diameter: 0.3, height: 4, tessellation: 6 },
                scene
            );
            const antMat = new BABYLON.StandardMaterial('hqmat_' + id + '_ant', scene);
            antMat.diffuseColor = new BABYLON.Color3(0.8, 0.8, 0.8);
            antenna.material = antMat;
            antenna.position.y = towerHeight + 2;
            antenna.position.x = 3;
            extras.push(antenna);

            // Blinking light on antenna
            const light = BABYLON.MeshBuilder.CreateSphere(
                'hq_' + id + '_light',
                { diameter: 0.5, segments: 8 },
                scene
            );
            const lmat = new BABYLON.StandardMaterial('hqmat_' + id + '_l', scene);
            lmat.diffuseColor = new BABYLON.Color3(1, 0, 0);
            lmat.emissiveColor = new BABYLON.Color3(0.5, 0, 0);
            light.material = lmat;
            light.position.y = towerHeight + 4.2;
            light.position.x = 3;
            extras.push(light);

            // Basement indicator: darker base extending below ground
            const basementHeight = Math.max(1, basementLevels * 1.5);
            const basement = BABYLON.MeshBuilder.CreateBox(
                'hq_' + id + '_basement',
                { width: 10.5, height: basementHeight, depth: 10.5 },
                scene
            );
            const bmat = new BABYLON.StandardMaterial('hqmat_' + id + '_b', scene);
            bmat.diffuseColor = new BABYLON.Color3(0.15, 0.15, 0.2);
            basement.material = bmat;
            basement.position.y = -basementHeight / 2;
            extras.push(basement);

            // Entrance canopy
            const canopy = BABYLON.MeshBuilder.CreateBox(
                'hq_' + id + '_canopy',
                { width: 6, height: 0.3, depth: 3 },
                scene
            );
            const cmat = new BABYLON.StandardMaterial('hqmat_' + id + '_c', scene);
            cmat.diffuseColor = new BABYLON.Color3(0.2, 0.3, 0.5);
            canopy.material = cmat;
            canopy.position.y = 1;
            canopy.position.z = 6;
            extras.push(canopy);

            // "YOUNGER INDUSTRIES" sign (simplified as a glowing panel)
            const sign = BABYLON.MeshBuilder.CreateBox(
                'hq_' + id + '_sign',
                { width: 8, height: 1, depth: 0.2 },
                scene
            );
            const smat = new BABYLON.StandardMaterial('hqmat_' + id + '_s', scene);
            smat.diffuseColor = new BABYLON.Color3(0.1, 0.5, 0.9);
            smat.emissiveColor = new BABYLON.Color3(0.05, 0.25, 0.45);
            sign.material = smat;
            sign.position.y = towerHeight - 1;
            sign.position.z = 5.1;
            extras.push(sign);

            return { building: building, extras: extras };
        },

        /**
         * Renders NPCs in the 3D world.
         * @param {Array} npcs - Array of NPC objects
         */
        renderNpcs: function (npcs) {
            if (!this.scene) return;

            for (const npc of npcs) {
                const pos = this.hexTo3D(npc.position);

                if (!this.npcs[npc.id]) {
                    // NPC character (small cylinder)
                    const mesh = BABYLON.MeshBuilder.CreateCylinder(
                        'npc_' + npc.id,
                        { diameter: 1, height: 2, tessellation: 8 },
                        this.scene
                    );
                    const mat = new BABYLON.StandardMaterial('npcmat_' + npc.id, this.scene);

                    // Color by type
                    switch (npc.type) {
                        case 'TRAIN_DRIVER': mat.diffuseColor = new BABYLON.Color3(0.8, 0.2, 0.2); break;
                        case 'BUS_DRIVER': mat.diffuseColor = new BABYLON.Color3(0.2, 0.8, 0.2); break;
                        case 'FACTORY_WORKER': mat.diffuseColor = new BABYLON.Color3(0.6, 0.6, 0.2); break;
                        case 'STATION_MASTER': mat.diffuseColor = new BABYLON.Color3(0.2, 0.2, 0.8); break;
                        default: mat.diffuseColor = new BABYLON.Color3(0.5, 0.5, 0.5);
                    }

                    mesh.material = mat;
                    mesh.castShadows = true;
                    this.npcs[npc.id] = mesh;
                }

                // Update position
                this.npcs[npc.id].position = pos;
            }
        },

        /**
         * Renders unified HullModule structures on the planet.
         * @param {Array} hullModules - Array of hull module state objects
         */
        renderHullModules: function (hullModules) {
            if (!this.scene) return;

            // Clean up old hull modules
            for (const key in this.hullModules) {
                if (this.hullModules[key]) {
                    this.hullModules[key].building.dispose();
                    if (this.hullModules[key].extras) {
                        this.hullModules[key].extras.forEach(e => e.dispose());
                    }
                }
            }
            this.hullModules = {};

            for (const hm of hullModules) {
                if (!hm.active) continue;
                const id = hm.id;
                const pos = this.hexTo3D(hm.x, hm.y, hm.z);
                const result = this._createHullModuleModel(hm);
                result.building.position = pos;
                this.hullModules[id] = result;
            }
        },

        /**
         * Creates a 3D model for a HullModule based on its type.
         */
        _createHullModuleModel: function (hm) {
            const scene = this.scene;
            const id = hm.id;
            const extras = [];
            let building;

            const height = Math.max(2, hm.floors * 1.5);

            // Color by category
            let color;
            switch (hm.category) {
                case 'RESIDENTIAL': color = new BABYLON.Color3(0.65, 0.68, 0.72); break;
                case 'COMMERCIAL': color = new BABYLON.Color3(0.7, 0.6, 0.5); break;
                case 'INDUSTRY': color = new BABYLON.Color3(0.5, 0.45, 0.4); break;
                case 'INFRASTRUCTURE': color = new BABYLON.Color3(0.4, 0.5, 0.6); break;
                default: color = new BABYLON.Color3(0.6, 0.6, 0.6); break;
            }

            building = BABYLON.MeshBuilder.CreateBox(
                'hull_' + id,
                { width: 6, height: height, depth: 5 },
                scene
            );
            const mat = new BABYLON.StandardMaterial('hullmat_' + id, scene);
            mat.diffuseColor = color;
            building.material = mat;
            building.castShadows = true;

            // Windows for multi-floor buildings
            if (hm.floors > 1) {
                for (let f = 0; f < Math.min(hm.floors, 8); f++) {
                    for (let w = 0; w < 2; w++) {
                        const win = BABYLON.MeshBuilder.CreateBox(
                            'hull_' + id + '_win_' + f + '_' + w,
                            { width: 0.8, height: 0.6, depth: 0.1 },
                            scene
                        );
                        const wmat = new BABYLON.StandardMaterial('hullmat_' + id + '_w_' + f + '_' + w, scene);
                        wmat.diffuseColor = new BABYLON.Color3(0.3, 0.5, 0.8);
                        wmat.emissiveColor = new BABYLON.Color3(0.1, 0.15, 0.25);
                        win.material = wmat;
                        win.position.x = -1 + w * 2;
                        win.position.y = 1 + f * 1.5;
                        win.position.z = 2.55;
                        extras.push(win);
                    }
                }
            }

            // Production indicator for industries
            if (hm.category === 'INDUSTRY' && hm.productionLevel > 0) {
                const smoke = BABYLON.MeshBuilder.CreateCylinder(
                    'hull_' + id + '_smoke',
                    { height: 2, diameter: 1 },
                    scene
                );
                const smat = new BABYLON.StandardMaterial('hullmat_' + id + '_s', scene);
                smat.diffuseColor = new BABYLON.Color3(0.5, 0.5, 0.5);
                smat.alpha = 0.5;
                smoke.material = smat;
                smoke.position.y = height + 1;
                extras.push(smoke);
            }

            // Basement indicator
            if (hm.basementLevels > 0) {
                const basementH = Math.max(1, hm.basementLevels * 1.2);
                const basement = BABYLON.MeshBuilder.CreateBox(
                    'hull_' + id + '_basement',
                    { width: 6.5, height: basementH, depth: 5.5 },
                    scene
                );
                const bmat = new BABYLON.StandardMaterial('hullmat_' + id + '_b', scene);
                bmat.diffuseColor = new BABYLON.Color3(0.3, 0.3, 0.35);
                basement.material = bmat;
                basement.position.y = -basementH / 2;
                extras.push(basement);
            }

            return { building: building, extras: extras };
        },

        /**
         * Fetches and renders the current world state.
         */
        fetchWorldState: function () {
            if (!this.planetId) return;

            // Use FPS bridge endpoint for complete world state
            fetch(`/api/v1/planets/${this.planetId}/fps/world-state`)
                .then(response => response.json())
                .then(data => {
                    if (data.vehicles) this.renderVehicles(data.vehicles);
                    // HullModules is the unified building list (industries, stations, residential, HQ hubs)
                    // Skip separate lists if hullModules is available to avoid duplicates
                    if (data.hullModules && data.hullModules.length > 0) {
                        this.renderHullModules(data.hullModules);
                    } else {
                        // Fallback to separate lists for backward compatibility
                        if (data.stations) this.renderStations(data.stations);
                        if (data.industries) this.renderIndustries(data.industries);
                        if (data.residential) this.renderResidential(data.residential);
                        if (data.hqHubs) this.renderHQHubs(data.hqHubs);
                    }
                    // Render ships in orbit
                    if (data.ships && data.ships.length > 0) this.renderShips(data.ships);
                    // Render starbases
                    if (data.starbases && data.starbases.length > 0) this.renderStarbases(data.starbases);
                    // Render military units
                    if (data.militaryUnits && data.militaryUnits.length > 0) this.renderMilitaryUnits(data.militaryUnits);
                    // Render cargo
                    if (data.cargo && data.cargo.length > 0) this.renderCargo(data.cargo);
                    // Render battles
                    if (data.battles && data.battles.length > 0) this.renderBattles(data.battles);
                    // Render AI commanders
                    if (data.aiCommanders && data.aiCommanders.length > 0) this.renderAICommanders(data.aiCommanders);
                    // Render battlefield units
                    if (data.battlefieldUnits && data.battlefieldUnits.length > 0) this.renderBattlefieldUnits(data.battlefieldUnits);
                })
                .catch(error => {
                    // Fallback to surface endpoint if FPS bridge is not available
                    fetch(`/api/v1/planets/${this.planetId}/surface`)
                        .then(response => response.json())
                        .then(surfaceData => {
                            if (surfaceData.terrain) this.renderTerrain(surfaceData.terrain);
                            if (surfaceData.vehicles) this.renderVehicles(surfaceData.vehicles);
                            if (surfaceData.stations) this.renderStations(surfaceData.stations);
                            if (surfaceData.industries) this.renderIndustries(surfaceData.industries);
                        })
                        .catch(fallbackError => console.error('Failed to fetch world state:', fallbackError));
                });
        },

        /**
         * Starts periodic state updates.
         */
        startStateUpdates: function () {
            // Initial fetch
            this.fetchWorldState();

            // Periodic updates
            this.updateInterval = setInterval(() => {
                this.fetchWorldState();
            }, 5000);
        },

        /**
         * Stops the renderer.
         */
        dispose: function () {
            if (this.updateInterval) {
                clearInterval(this.updateInterval);
            }
            if (this.engine) {
                this.engine.dispose();
            }
            this.engine = null;
            this.scene = null;
            this.camera = null;
            this.vehicles = {};
            this.stations = {};
            this.industries = {};
            this.residential = {};
            this.hqHubs = {};
            this.hullModules = {};
            this.npcs = {};
            this.hexMeshes = {};
        },

        /**
         * Renders ships in orbit around the planet.
         */
        renderShips: function (ships) {
            ships.forEach(ship => {
                const pos = this.hexTo3D(ship.x, ship.y, ship.z);
                let mesh;
                const color = new BABYLON.Color3(0.3, 0.5, 0.8);
                switch (ship.shipType) {
                    case 'FRIGATE':
                        mesh = BABYLON.MeshBuilder.CreateBox(ship.id, {width: 2, height: 0.5, depth: 4}, this.scene);
                        break;
                    case 'DESTROYER':
                        mesh = BABYLON.MeshBuilder.CreateBox(ship.id, {width: 2.5, height: 0.6, depth: 5}, this.scene);
                        break;
                    case 'CRUISER':
                        mesh = BABYLON.MeshBuilder.CreateBox(ship.id, {width: 3, height: 0.8, depth: 7}, this.scene);
                        break;
                    case 'BATTLESHIP':
                        mesh = BABYLON.MeshBuilder.CreateBox(ship.id, {width: 4, height: 1, depth: 10}, this.scene);
                        break;
                    case 'CARRIER':
                        mesh = BABYLON.MeshBuilder.CreateBox(ship.id, {width: 5, height: 0.8, depth: 9}, this.scene);
                        break;
                    case 'MOTHERSHIP':
                        mesh = BABYLON.MeshBuilder.CreateSphere(ship.id, {diameter: 8}, this.scene);
                        break;
                    default:
                        mesh = BABYLON.MeshBuilder.CreateBox(ship.id, {width: 2, height: 0.5, depth: 4}, this.scene);
                }
                mesh.position = pos;
                mesh.material = this.createMaterial(color);
                // Add glow for shields
                if (ship.currentShieldPoints > 0) {
                    const glow = BABYLON.MeshBuilder.CreateSphere(ship.id + '-glow', {diameter: mesh.scaling.x * 2 + 1}, this.scene);
                    glow.material = this.createMaterial(new BABYLON.Color3(0.2, 0.6, 1), 0.3);
                    glow.position = pos;
                }
                this.addTooltip(mesh, `${ship.name} (${ship.shipType})\nHull: ${ship.currentHullPoints}/${ship.maxHullPoints}`);
            });
        },

        /**
         * Renders starbases in orbit.
         */
        renderStarbases: function (starbases) {
            starbases.forEach(sb => {
                const pos = this.hexTo3D(sb.x, sb.y, sb.z);
                // Starbase is a ring structure
                const ring = BABYLON.MeshBuilder.CreateTorus(sb.id, {diameter: sb.moduleCount * 2, thickness: 1}, this.scene);
                ring.position = pos;
                ring.rotation.y = Math.PI / 4;
                const color = new BABYLON.Color3(0.6, 0.6, 0.7);
                ring.material = this.createMaterial(color);
                // Add central hub
                const hub = BABYLON.MeshBuilder.CreateSphere(sb.id + '-hub', {diameter: 2}, this.scene);
                hub.position = pos;
                hub.material = this.createMaterial(new BABYLON.Color3(0.8, 0.8, 0.9));
                this.addTooltip(ring, `${sb.name} (${sb.starBaseType})\nModules: ${sb.moduleCount}\nHP: ${sb.currentHitPoints}/${sb.maxHitPoints}`);
            });
        },

        /**
         * Renders military units on the ground.
         */
        renderMilitaryUnits: function (units) {
            units.forEach(unit => {
                const pos = this.hexTo3D(unit.x, unit.y, unit.z);
                pos.y += 0.5; // Slightly above ground
                let mesh;
                const color = new BABYLON.Color3(0.2, 0.6, 0.2); // Military green
                switch (unit.unitType) {
                    case 'INFANTRY':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 0.3, height: 0.6, depth: 0.3}, this.scene);
                        break;
                    case 'TANK':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 1.2, height: 0.6, depth: 1.8}, this.scene);
                        break;
                    case 'ARTILLERY':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 1, height: 0.5, depth: 2}, this.scene);
                        break;
                    case 'MECH':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 1, height: 3, depth: 1}, this.scene);
                        break;
                    default:
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 0.5, height: 0.5, depth: 0.5}, this.scene);
                }
                mesh.position = pos;
                mesh.material = this.createMaterial(color);
                // Red indicator if in combat
                if (unit.inCombat) {
                    const indicator = BABYLON.MeshBuilder.CreateSphere(unit.id + '-alert', {diameter: 0.5}, this.scene);
                    indicator.position = pos.add(new BABYLON.Vector3(0, 1, 0));
                    indicator.material = this.createMaterial(new BABYLON.Color3(1, 0, 0));
                }
                this.addTooltip(mesh, `${unit.name}\n${unit.unitType}\nStrength: ${unit.strength}/${unit.maxStrength}\nMorale: ${unit.morale}`);
            });
        },

        /**
         * Renders cargo stacks at stations.
         */
        renderCargo: function (cargo) {
            cargo.forEach(c => {
                const pos = this.hexTo3D(c.x, c.y, c.z);
                pos.y += 0.3;
                // Create a small box representing cargo
                const size = Math.min(2, c.quantity / 50);
                const stack = BABYLON.MeshBuilder.CreateBox(c.cargoType + '-' + c.x + '-' + c.y, {width: size, height: size, depth: size}, this.scene);
                stack.position = pos;
                const color = BABYLON.Color3.FromHexString(c.color || '#8B4513');
                stack.material = this.createMaterial(color);
                this.addTooltip(stack, `${c.quantity} ${c.cargoType}\nAt: ${c.locationName}`);
            });
        },

        /**
         * Renders AI commanders as officer models with command radius indicators.
         */
        renderAICommanders: function (commanders) {
            // Clear previous commanders
            for (const key in this.aiCommanders) {
                if (this.aiCommanders[key].mesh) this.aiCommanders[key].mesh.dispose();
                if (this.aiCommanders[key].radius) this.aiCommanders[key].radius.dispose();
                if (this.aiCommanders[key].indicator) this.aiCommanders[key].indicator.dispose();
            }
            this.aiCommanders = {};

            commanders.forEach(cmd => {
                const pos = this.hexTo3D(cmd.x, cmd.y, cmd.z);
                const group = new BABYLON.TransformNode(cmd.id, this.scene);
                group.position = pos;

                // Commander body - tall officer model
                const body = BABYLON.MeshBuilder.CreateCylinder(cmd.id + '-body', {height: 2, diameterTop: 0.3, diameterBottom: 0.4}, this.scene);
                body.position.y = 1;
                body.parent = group;
                // Gold color for officers
                body.material = this.createMaterial(new BABYLON.Color3(0.9, 0.8, 0.2));

                // Head
                const head = BABYLON.MeshBuilder.CreateSphere(cmd.id + '-head', {diameter: 0.4}, this.scene);
                head.position.y = 2.2;
                head.parent = group;
                head.material = this.createMaterial(new BABYLON.Color3(0.8, 0.6, 0.4));

                // Cap/hat
                const cap = BABYLON.MeshBuilder.CreateCylinder(cmd.id + '-cap', {height: 0.3, diameter: 0.45}, this.scene);
                cap.position.y = 2.5;
                cap.parent = group;
                cap.material = this.createMaterial(new BABYLON.Color3(0.1, 0.1, 0.3));

                // Command radius ring
                const radius = BABYLON.MeshBuilder.CreateTorus(cmd.id + '-radius', {diameter: cmd.commandRadius * 2, thickness: 0.05}, this.scene);
                radius.rotation.x = Math.PI / 2;
                radius.position.y = 0.1;
                radius.parent = group;
                const radiusColor = cmd.inCombat ? new BABYLON.Color3(1, 0.2, 0.2) : new BABYLON.Color3(0.2, 0.8, 1);
                radius.material = this.createMaterial(radiusColor, 0.4);

                // Combat indicator
                let indicator = null;
                if (cmd.inCombat) {
                    indicator = BABYLON.MeshBuilder.CreateSphere(cmd.id + '-alert', {diameter: 0.6}, this.scene);
                    indicator.position.y = 3;
                    indicator.parent = group;
                    indicator.material = this.createMaterial(new BABYLON.Color3(1, 0, 0));
                    // Pulse animation
                    this.scene.registerBeforeRender(() => {
                        const scale = 1 + Math.sin(Date.now() / 150) * 0.3;
                        indicator.scaling = new BABYLON.Vector3(scale, scale, scale);
                    });
                }

                this.aiCommanders[cmd.id] = { mesh: group, radius: radius, indicator: indicator };
                this.addTooltip(group, `${cmd.name}\n${cmd.factionName} (${cmd.factionId})\nLeadership: ${cmd.leadership}\nPreference: ${cmd.tacticalPreference}\nUnits: ${cmd.unitCount}\nCommand Radius: ${cmd.commandRadius}\nAttack Bonus: +${cmd.attackBonus}\nDefense Bonus: +${cmd.defenseBonus}`);
            });
        },

        /**
         * Renders battlefield units with faction colors and formation patterns.
         */
        renderBattlefieldUnits: function (units) {
            // Clear previous units
            for (const key in this.battlefieldUnits) {
                if (this.battlefieldUnits[key].mesh) this.battlefieldUnits[key].mesh.dispose();
                if (this.battlefieldUnits[key].indicator) this.battlefieldUnits[key].indicator.dispose();
            }
            this.battlefieldUnits = {};

            // Faction color map
            const factionColors = {
                'US': new BABYLON.Color3(0.2, 0.3, 0.8),
                'RU': new BABYLON.Color3(0.8, 0.2, 0.2),
                'CN': new BABYLON.Color3(0.8, 0.3, 0.2),
                'DE': new BABYLON.Color3(0.2, 0.2, 0.2),
                'GB': new BABYLON.Color3(0.3, 0.3, 0.7),
                'FR': new BABYLON.Color3(0.2, 0.4, 0.8),
                'JP': new BABYLON.Color3(0.8, 0.3, 0.3),
                'IN': new BABYLON.Color3(0.8, 0.6, 0.2),
                'BR': new BABYLON.Color3(0.2, 0.7, 0.2),
                'KR': new BABYLON.Color3(0.4, 0.4, 0.9),
            };

            units.forEach(unit => {
                const pos = this.hexTo3D(unit.x, unit.y, unit.z);
                pos.y += 0.5;
                let mesh;
                const color = factionColors[unit.factionId] || new BABYLON.Color3(0.4, 0.6, 0.4);

                switch (unit.unitType) {
                    case 'INFANTRY':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 0.3, height: 0.6, depth: 0.3}, this.scene);
                        break;
                    case 'MECHANIZED':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 0.8, height: 0.5, depth: 1.2}, this.scene);
                        break;
                    case 'TANK':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 1.2, height: 0.6, depth: 1.8}, this.scene);
                        // Add turret
                        const turret = BABYLON.MeshBuilder.CreateBox(unit.id + '-turret', {width: 0.6, height: 0.4, depth: 0.8}, this.scene);
                        turret.position.y = 0.5;
                        turret.parent = mesh;
                        turret.material = this.createMaterial(color.scale(0.8));
                        // Barrel
                        const barrel = BABYLON.MeshBuilder.CreateCylinder(unit.id + '-barrel', {diameter: 0.1, height: 1.5}, this.scene);
                        barrel.rotation.x = Math.PI / 2;
                        barrel.position.z = 1.2;
                        barrel.position.y = 0.5;
                        barrel.parent = mesh;
                        barrel.material = this.createMaterial(new BABYLON.Color3(0.2, 0.2, 0.2));
                        break;
                    case 'ARTILLERY':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 1, height: 0.5, depth: 2}, this.scene);
                        break;
                    case 'RECON':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 0.5, height: 0.4, depth: 0.8}, this.scene);
                        break;
                    case 'ENGINEER':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 0.6, height: 0.5, depth: 0.6}, this.scene);
                        break;
                    case 'ANTI_AIR':
                        mesh = BABYLON.MeshBuilder.CreateCylinder(unit.id, {height: 1.5, diameter: 0.5}, this.scene);
                        break;
                    case 'AIR_SUPPORT':
                        // Aircraft - wing shape
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 3, height: 0.1, depth: 0.8}, this.scene);
                        pos.y += 5; // Flying
                        break;
                    case 'MECH':
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 1, height: 3, depth: 1}, this.scene);
                        // Legs
                        const leg1 = BABYLON.MeshBuilder.CreateBox(unit.id + '-leg1', {width: 0.3, height: 1, depth: 0.3}, this.scene);
                        leg1.position.x = -0.3;
                        leg1.position.y = -2;
                        leg1.parent = mesh;
                        leg1.material = this.createMaterial(color.scale(0.7));
                        const leg2 = BABYLON.MeshBuilder.CreateBox(unit.id + '-leg2', {width: 0.3, height: 1, depth: 0.3}, this.scene);
                        leg2.position.x = 0.3;
                        leg2.position.y = -2;
                        leg2.parent = mesh;
                        leg2.material = this.createMaterial(color.scale(0.7));
                        break;
                    default:
                        mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 0.5, height: 0.5, depth: 0.5}, this.scene);
                }
                mesh.position = pos;
                mesh.material = this.createMaterial(color);

                // Health bar
                const healthPercent = unit.strength / unit.maxStrength;
                const healthBar = BABYLON.MeshBuilder.CreateBox(unit.id + '-health', {width: 1, height: 0.1, depth: 0.1}, this.scene);
                healthBar.position = pos.add(new BABYLON.Vector3(0, 1.5, 0));
                const healthColor = healthPercent > 0.6 ? new BABYLON.Color3(0, 1, 0) : healthPercent > 0.3 ? new BABYLON.Color3(1, 1, 0) : new BABYLON.Color3(1, 0, 0);
                healthBar.material = this.createMaterial(healthColor);
                healthBar.scaling.x = healthPercent;

                // Combat indicator
                let indicator = null;
                if (unit.inCombat) {
                    indicator = BABYLON.MeshBuilder.CreateSphere(unit.id + '-alert', {diameter: 0.4}, this.scene);
                    indicator.position = pos.add(new BABYLON.Vector3(0, 1, 0));
                    indicator.material = this.createMaterial(new BABYLON.Color3(1, 0, 0));
                }

                this.battlefieldUnits[unit.id] = { mesh: mesh, indicator: indicator, healthBar: healthBar };
                this.addTooltip(mesh, `${unit.name}\n${unit.unitType}\nFaction: ${unit.factionName}\nStrength: ${unit.strength}/${unit.maxStrength}\nMorale: ${unit.morale}\nCommander: ${unit.commanderId}`);
            });
        },

        /**
         * Renders battle indicators.
         */
        renderBattles: function (battles) {
            battles.forEach(battle => {
                const pos = this.hexTo3D(battle.x, battle.y, battle.z);
                // Create an explosion effect for active battles
                if (battle.state === 'IN_PROGRESS') {
                    const explosion = BABYLON.MeshBuilder.CreateSphere(battle.battleId, {diameter: 3}, this.scene);
                    explosion.position = pos.add(new BABYLON.Vector3(0, 2, 0));
                    explosion.material = this.createMaterial(new BABYLON.Color3(1, 0.5, 0), 0.6);
                    // Animate pulsing
                    this.scene.registerBeforeRender(() => {
                        const scale = 1 + Math.sin(Date.now() / 200) * 0.3;
                        explosion.scaling = new BABYLON.Vector3(scale, scale, scale);
                    });
                } else {
                    // Completed battle marker
                    const marker = BABYLON.MeshBuilder.CreateCylinder(battle.battleId, {height: 1, diameter: 0.5}, this.scene);
                    marker.position = pos.add(new BABYLON.Vector3(0, 0.5, 0));
                    const color = battle.outcome === 'ATTACKER_VICTORY' ? new BABYLON.Color3(1, 0.3, 0.3) : new BABYLON.Color3(0.3, 0.3, 1);
                    marker.material = this.createMaterial(color);
                }
                this.addTooltip(explosion || marker, `Battle: ${battle.attackerFaction} vs ${battle.defenderFaction}\nOutcome: ${battle.outcome || 'In Progress'}`);
            });
        },

        /**
         * Renders universal battle units from any module.
         */
        renderUniversalBattle: function (units) {
            if (!units || !units.length) return;
            units.forEach(unit => {
                const pos = new BABYLON.Vector3(unit.hexX * 5, 0, unit.hexZ * 5);
                let mesh;
                const color = this.getModuleColor(unit.moduleId);

                // Create module-specific model
                if (unit.genericType === 'ARMORED' || unit.genericType === 'SPACECRAFT') {
                    mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 2, height: 2, depth: 2}, this.scene);
                } else if (unit.genericType === 'AIRCRAFT') {
                    mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 1.5, height: 0.5, depth: 2}, this.scene);
                } else {
                    mesh = BABYLON.MeshBuilder.CreateBox(unit.id, {width: 1, height: 1.5, depth: 1}, this.scene);
                }
                mesh.position = pos;
                mesh.material = this.createMaterial(color);

                // Health bar
                const healthPercent = unit.strength / unit.maxStrength;
                const healthBar = BABYLON.MeshBuilder.CreateBox(unit.id + '-health', {width: 1, height: 0.1, depth: 0.1}, this.scene);
                healthBar.position = pos.add(new BABYLON.Vector3(0, 2, 0));
                const healthColor = healthPercent > 0.6 ? new BABYLON.Color3(0, 1, 0) : healthPercent > 0.3 ? new BABYLON.Color3(1, 1, 0) : new BABYLON.Color3(1, 0, 0);
                healthBar.material = this.createMaterial(healthColor);
                healthBar.scaling.x = healthPercent;

                this.universalUnits[unit.id] = { mesh: mesh, healthBar: healthBar, module: unit.moduleId };
                this.addTooltip(mesh, `${unit.name}\nModule: ${unit.moduleId}\nType: ${unit.moduleUnitType}\nGeneric: ${unit.genericType}\nStrength: ${unit.strength}/${unit.maxStrength}\nMorale: ${unit.morale}`);
            });
        },

        /**
         * Get color for a module ID.
         */
        getModuleColor: function (moduleId) {
            const colors = {
                'Traveller': new BABYLON.Color3(1, 0.5, 0),
                'Babylon5': new BABYLON.Color3(0.5, 0, 1),
                'CaptainsLog': new BABYLON.Color3(1, 0.55, 0),
                'SFB': new BABYLON.Color3(0.2, 0.2, 1),
                'StarTrek4x': new BABYLON.Color3(0, 0.5, 1),
                'Earth2026': new BABYLON.Color3(0, 0, 1),
                'Dnd5e': new BABYLON.Color3(1, 0, 0),
                'FPS': new BABYLON.Color3(0.5, 0.5, 0.5),
                'MechWarrior': new BABYLON.Color3(0, 0.8, 0),
                'StarWars': new BABYLON.Color3(0, 0, 0),
                'StarshipTroopers': new BABYLON.Color3(0, 1, 0),
                'ZeroZeroSeven': new BABYLON.Color3(0.3, 0.3, 0.3)
            };
            return colors[moduleId] || new BABYLON.Color3(0.5, 0.5, 0.5);
        },

        /**
         * @param {string} vehicleId - The vehicle ID to chase
         */
        chaseVehicle: function (vehicleId) {
            if (this.vehicles[vehicleId]) {
                this.viewMode = 'chase';
                this.chaseTarget = vehicleId;
                this.camera.target = this.vehicles[vehicleId].position;
            }
        },

        /**
         * Sets the camera to orbit mode.
         */
        setOrbitMode: function () {
            this.viewMode = 'orbit';
            this.chaseTarget = null;
            this.camera.target = BABYLON.Vector3.Zero();
        }
    };

    // Export to global scope
    window.Tycoon3D = Tycoon3D;

})();

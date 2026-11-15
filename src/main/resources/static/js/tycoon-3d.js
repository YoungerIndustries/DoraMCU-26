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
        npcs: {},
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
         * Renders stations in the 3D world.
         * @param {Array} stations - Array of station state objects
         */
        renderStations: function (stations) {
            if (!this.scene) return;

            for (const station of stations) {
                const pos = this.hexTo3D(station);

                if (!this.stations[station.id]) {
                    // Station building
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'station_' + station.id,
                        { width: 6, height: 4, depth: 6 },
                        this.scene
                    );
                    building.position = pos;
                    const mat = new BABYLON.StandardMaterial('smat_' + station.id, this.scene);
                    mat.diffuseColor = new BABYLON.Color3(0.7, 0.7, 0.7);
                    building.material = mat;
                    building.castShadows = true;
                    this.stations[station.id] = { building: building };
                }

                // Update position
                this.stations[station.id].building.position = pos;
            }
        },

        /**
         * Renders industries in the 3D world.
         * @param {Array} industries - Array of industry state objects
         */
        renderIndustries: function (industries) {
            if (!this.scene) return;

            for (const industry of industries) {
                const pos = this.hexTo3D(industry);

                if (!this.industries[industry.id]) {
                    // Factory building
                    const building = BABYLON.MeshBuilder.CreateBox(
                        'industry_' + industry.id,
                        { width: 8, height: 6, depth: 8 },
                        this.scene
                    );
                    building.position = pos;
                    const mat = new BABYLON.StandardMaterial('imat_' + industry.id, this.scene);
                    mat.diffuseColor = new BABYLON.Color3(0.5, 0.3, 0.2);
                    building.material = mat;
                    building.castShadows = true;
                    this.industries[industry.id] = { building: building };
                }

                // Update position
                this.industries[industry.id].building.position = pos;
            }
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
         * Fetches and renders the current world state.
         */
        fetchWorldState: function () {
            if (!this.planetId) return;

            fetch(`/api/v1/planets/${this.planetId}/surface`)
                .then(response => response.json())
                .then(data => {
                    if (data.terrain) this.renderTerrain(data.terrain);
                    if (data.vehicles) this.renderVehicles(data.vehicles);
                    if (data.stations) this.renderStations(data.stations);
                    if (data.industries) this.renderIndustries(data.industries);
                })
                .catch(error => console.error('Failed to fetch world state:', error));
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
            this.npcs = {};
            this.hexMeshes = {};
        },

        /**
         * Sets the camera to chase a vehicle.
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

/**
 * 3D Scene Renderer — Shared Three.js scene manager for non-FPS HUD themes.
 *
 * Provides atmospheric 3D visuals for each game theme using Three.js primitives.
 * Each theme gets a scene-specific environment (starship bridge, dungeon, planet, etc.).
 *
 * Usage:
 *   const renderer = new SceneRenderer(containerElement, sceneConfig);
 *   renderer.init();
 *   renderer.updateScene(objects);
 *   renderer.setCamera(position, target);
 *   renderer.destroy();
 */

class SceneRenderer {
    constructor(container, config) {
        this.container = container;
        this.config = config || {};
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.animationId = null;
        this.objects = [];
        this.lights = [];
        this.particles = null;
        this.isInitialized = false;
    }

    /**
     * Initialize the Three.js scene, camera, renderer, and lights.
     */
    init() {
        if (this.isInitialized) return;

        // Load Three.js dynamically if not present
        if (typeof THREE === 'undefined') {
            console.warn('[SceneRenderer] Three.js not loaded. Skipping 3D scene.');
            return;
        }

        this.scene = new THREE.Scene();
        this._setupBackground();
        this._setupCamera();
        this._setupRenderer();
        this._setupLights();
        this._buildScene();
        this._setupAnimationLoop();

        this.isInitialized = true;
        console.log('[SceneRenderer] Scene initialized:', this.config.theme || 'default');

        // Handle resize
        this._resizeHandler = () => this._onResize();
        window.addEventListener('resize', this._resizeHandler);
    }

    /**
     * Set up background color and fog.
     */
    _setupBackground() {
        const bg = this.config.backgroundColor || 0x000000;
        this.scene.background = new THREE.Color(bg);

        if (this.config.fog) {
            this.scene.fog = new THREE.Fog(this.config.fog.color || bg, this.config.fog.near || 10, this.config.fog.far || 100);
        }
    }

    /**
     * Set up camera based on config.
     */
    _setupCamera() {
        const width = this.container.clientWidth || 800;
        const height = this.container.clientHeight || 600;
        this.camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000);

        const cam = this.config.camera || {};
        this.camera.position.set(cam.x || 0, cam.y || 5, cam.z || 10);
        if (cam.target) {
            this.camera.lookAt(new THREE.Vector3(cam.target.x || 0, cam.target.y || 0, cam.target.z || 0));
        }
    }

    /**
     * Set up WebGL renderer.
     */
    _setupRenderer() {
        this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
        this.renderer.setSize(this.container.clientWidth, this.container.clientHeight);
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        this.renderer.shadowMap.enabled = true;
        this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        this.container.appendChild(this.renderer.domElement);
    }

    /**
     * Set up lights based on config.
     */
    _setupLights() {
        // Ambient light
        const ambient = this.config.ambient || { intensity: 0.3, color: 0xffffff };
        const ambientLight = new THREE.AmbientLight(ambient.color, ambient.intensity);
        this.scene.add(ambientLight);
        this.lights.push(ambientLight);

        // Directional light
        const dir = this.config.directional || { intensity: 0.8, color: 0xffffff, x: 5, y: 10, z: 5 };
        const dirLight = new THREE.DirectionalLight(dir.color, dir.intensity);
        dirLight.position.set(dir.x, dir.y, dir.z);
        dirLight.castShadow = true;
        this.scene.add(dirLight);
        this.lights.push(dirLight);

        // Point lights from config
        if (this.config.pointLights) {
            for (const pl of this.config.pointLights) {
                const pointLight = new THREE.PointLight(pl.color || 0xffffff, pl.intensity || 1, pl.distance || 20);
                pointLight.position.set(pl.x || 0, pl.y || 0, pl.z || 0);
                this.scene.add(pointLight);
                this.lights.push(pointLight);
            }
        }
    }

    /**
     * Build theme-specific scene.
     */
    _buildScene() {
        const theme = this.config.theme;
        if (theme && this[`_build${theme.charAt(0).toUpperCase() + theme.slice(1)}Scene`]) {
            this[`_build${theme.charAt(0).toUpperCase() + theme.slice(1)}Scene`]();
        } else {
            this._buildDefaultScene();
        }
    }

    /**
     * Default scene: simple grid and skybox.
     */
    _buildDefaultScene() {
        // Ground grid
        const gridHelper = new THREE.GridHelper(50, 50, 0x444444, 0x222222);
        this.scene.add(gridHelper);
        this.objects.push(gridHelper);

        // Ground plane
        const groundGeo = new THREE.PlaneGeometry(50, 50);
        const groundMat = new THREE.MeshStandardMaterial({ color: 0x111111, roughness: 0.9 });
        const ground = new THREE.Mesh(groundGeo, groundMat);
        ground.rotation.x = -Math.PI / 2;
        ground.position.y = -0.01;
        ground.receiveShadow = true;
        this.scene.add(ground);
        this.objects.push(ground);
    }

    /**
     * Captain's Log scene: Starship bridge with starfield.
     */
    _buildCaptainsLogScene() {
        // Starfield
        this._createStarfield(500);

        // Bridge floor
        const floorGeo = new THREE.PlaneGeometry(20, 15);
        const floorMat = new THREE.MeshStandardMaterial({ color: 0x2a2a2a, roughness: 0.7 });
        const floor = new THREE.Mesh(floorGeo, floorMat);
        floor.rotation.x = -Math.PI / 2;
        floor.receiveShadow = true;
        this.scene.add(floor);
        this.objects.push(floor);

        // Bridge walls
        const wallMat = new THREE.MeshStandardMaterial({ color: 0x3a3a3a, roughness: 0.8 });
        const backWall = new THREE.Mesh(new THREE.BoxGeometry(20, 8, 0.5), wallMat);
        backWall.position.set(0, 4, -7.5);
        this.scene.add(backWall);
        this.objects.push(backWall);

        // Viewport window (large screen showing space)
        const viewportGeo = new THREE.PlaneGeometry(12, 5);
        const viewportMat = new THREE.MeshStandardMaterial({
            color: 0x000033,
            emissive: 0x000033,
            emissiveIntensity: 0.3
        });
        const viewport = new THREE.Mesh(viewportGeo, viewportMat);
        viewport.position.set(0, 4, -7.2);
        this.scene.add(viewport);
        this.objects.push(viewport);

        // Console panels
        const consoleMat = new THREE.MeshStandardMaterial({ color: 0x1a3a3a, roughness: 0.5 });
        const screenMat = new THREE.MeshStandardMaterial({
            color: 0x00ff88,
            emissive: 0x00ff88,
            emissiveIntensity: 0.5
        });

        for (let i = -2; i <= 2; i++) {
            // Console desk
            const desk = new THREE.Mesh(new THREE.BoxGeometry(3, 1.5, 1.5), consoleMat);
            desk.position.set(i * 4, 0.75, 3);
            desk.castShadow = true;
            this.scene.add(desk);
            this.objects.push(desk);

            // Screen
            const screen = new THREE.Mesh(new THREE.PlaneGeometry(2, 1), screenMat);
            screen.position.set(i * 4, 1.8, 2.76);
            this.scene.add(screen);
            this.objects.push(screen);
        }

        // Red alert light (toggleable)
        const redLight = new THREE.PointLight(0xff0000, 0, 20);
        redLight.position.set(0, 6, 0);
        redLight.name = 'redAlertLight';
        this.scene.add(redLight);
        this.lights.push(redLight);

        // Camera position
        this.camera.position.set(0, 3, 8);
        this.camera.lookAt(new THREE.Vector3(0, 2, 0));
    }

    /**
     * D&D 5e scene: Dungeon room with torches.
     */
    _buildDnd5eScene() {
        // Dungeon floor with grid
        const floorGeo = new THREE.PlaneGeometry(20, 20);
        const floorMat = new THREE.MeshStandardMaterial({ color: 0x4a3a2a, roughness: 0.9 });
        const floor = new THREE.Mesh(floorGeo, floorMat);
        floor.rotation.x = -Math.PI / 2;
        floor.receiveShadow = true;
        this.scene.add(floor);
        this.objects.push(floor);

        // Combat grid overlay
        const gridHelper = new THREE.GridHelper(20, 20, 0x6a5a4a, 0x5a4a3a);
        gridHelper.position.y = 0.01;
        this.scene.add(gridHelper);
        this.objects.push(gridHelper);

        // Stone walls
        const wallMat = new THREE.MeshStandardMaterial({ color: 0x5a5a5a, roughness: 0.95 });
        const wallPositions = [
            { pos: [0, 3, -10], size: [20, 6, 0.5] },
            { pos: [0, 3, 10], size: [20, 6, 0.5] },
            { pos: [-10, 3, 0], size: [0.5, 6, 20] },
            { pos: [10, 3, 0], size: [0.5, 6, 20] }
        ];
        for (const wp of wallPositions) {
            const wall = new THREE.Mesh(new THREE.BoxGeometry(...wp.size), wallMat);
            wall.position.set(...wp.pos);
            wall.castShadow = true;
            wall.receiveShadow = true;
            this.scene.add(wall);
            this.objects.push(wall);
        }

        // Torches with flickering lights
        const torchPositions = [[-9, 3, -9], [9, 3, -9], [-9, 3, 9], [9, 3, 9]];
        for (const tp of torchPositions) {
            // Torch holder
            const holder = new THREE.Mesh(
                new THREE.CylinderGeometry(0.1, 0.1, 0.5),
                new THREE.MeshStandardMaterial({ color: 0x8B4513 })
            );
            holder.position.set(tp[0], tp[1], tp[2]);
            this.scene.add(holder);
            this.objects.push(holder);

            // Torch flame light
            const torchLight = new THREE.PointLight(0xff6600, 1, 8);
            torchLight.position.set(tp[0], tp[1] + 0.5, tp[2]);
            torchLight.name = 'torch';
            this.scene.add(torchLight);
            this.lights.push(torchLight);
        }

        // Ceiling
        const ceiling = new THREE.Mesh(
            new THREE.PlaneGeometry(20, 20),
            new THREE.MeshStandardMaterial({ color: 0x3a3a3a, roughness: 0.95 })
        );
        ceiling.rotation.x = Math.PI / 2;
        ceiling.position.y = 6;
        this.scene.add(ceiling);
        this.objects.push(ceiling);

        // Camera: isometric view
        this.camera.position.set(12, 12, 12);
        this.camera.lookAt(new THREE.Vector3(0, 0, 0));
    }

    /**
     * Traveller scene: Planet surface with starfield.
     */
    _buildTravellerScene() {
        // Starfield
        this._createStarfield(800);

        // Terrain with hills
        const terrainGeo = new THREE.PlaneGeometry(100, 100, 32, 32);
        const positions = terrainGeo.attributes.position.array;
        for (let i = 0; i < positions.length; i += 3) {
            positions[i + 2] = Math.sin(positions[i] * 0.1) * 2 + Math.cos(positions[i + 1] * 0.1) * 2;
        }
        terrainGeo.computeVertexNormals();
        const terrainMat = new THREE.MeshStandardMaterial({ color: 0x3a5a3a, roughness: 0.9 });
        const terrain = new THREE.Mesh(terrainGeo, terrainMat);
        terrain.rotation.x = -Math.PI / 2;
        terrain.receiveShadow = true;
        this.scene.add(terrain);
        this.objects.push(terrain);

        // Rocks
        const rockMat = new THREE.MeshStandardMaterial({ color: 0x6a6a6a, roughness: 0.9 });
        for (let i = 0; i < 15; i++) {
            const rock = new THREE.Mesh(
                new THREE.DodecahedronGeometry(Math.random() * 1 + 0.3),
                rockMat
            );
            rock.position.set(
                (Math.random() - 0.5) * 40,
                Math.random() * 0.5,
                (Math.random() - 0.5) * 40
            );
            rock.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, 0);
            rock.castShadow = true;
            this.scene.add(rock);
            this.objects.push(rock);
        }

        // Sky planet/moon
        const moonGeo = new THREE.SphereGeometry(3, 32, 32);
        const moonMat = new THREE.MeshStandardMaterial({
            color: 0xccaa88,
            emissive: 0x332200,
            emissiveIntensity: 0.2
        });
        const moon = new THREE.Mesh(moonGeo, moonMat);
        moon.position.set(20, 30, -40);
        this.scene.add(moon);
        this.objects.push(moon);

        // Atmospheric fog
        this.scene.fog = new THREE.FogExp2(0x1a2a3a, 0.02);

        // Camera
        this.camera.position.set(0, 3, 10);
        this.camera.lookAt(new THREE.Vector3(0, 1, 0));
    }

    /**
     * Babylon5 scene: Space station interior.
     */
    _buildBabylon5Scene() {
        // Dark space background
        this._createStarfield(400);

        // Station floor
        const floorGeo = new THREE.PlaneGeometry(30, 20);
        const floorMat = new THREE.MeshStandardMaterial({ color: 0x2a2a3a, roughness: 0.6 });
        const floor = new THREE.Mesh(floorGeo, floorMat);
        floor.rotation.x = -Math.PI / 2;
        floor.receiveShadow = true;
        this.scene.add(floor);
        this.objects.push(floor);

        // Corridor walls
        const wallMat = new THREE.MeshStandardMaterial({ color: 0x3a3a4a, roughness: 0.7 });
        const leftWall = new THREE.Mesh(new THREE.BoxGeometry(0.5, 6, 20), wallMat);
        leftWall.position.set(-8, 3, 0);
        this.scene.add(leftWall);
        this.objects.push(leftWall);

        const rightWall = new THREE.Mesh(new THREE.BoxGeometry(0.5, 6, 20), wallMat);
        rightWall.position.set(8, 3, 0);
        this.scene.add(rightWall);
        this.objects.push(rightWall);

        // Viewports showing space
        const viewportMat = new THREE.MeshStandardMaterial({
            color: 0x000022,
            emissive: 0x000022,
            emissiveIntensity: 0.3
        });
        for (let i = -1; i <= 1; i++) {
            const vp = new THREE.Mesh(new THREE.PlaneGeometry(4, 3), viewportMat);
            vp.position.set(-8.2, 3, i * 6);
            vp.rotation.y = Math.PI / 2;
            this.scene.add(vp);
            this.objects.push(vp);
        }

        // Docked ships (simple boxes)
        const shipMat = new THREE.MeshStandardMaterial({ color: 0x5a5a6a, roughness: 0.5 });
        for (let i = 0; i < 3; i++) {
            const ship = new THREE.Mesh(new THREE.BoxGeometry(2, 1, 4), shipMat);
            ship.position.set(15 + i * 5, 2 + i * 2, -10 + i * 8);
            ship.rotation.y = Math.random() * 0.5;
            this.scene.add(ship);
            this.objects.push(ship);
        }

        // Ceiling
        const ceiling = new THREE.Mesh(
            new THREE.PlaneGeometry(16, 20),
            new THREE.MeshStandardMaterial({ color: 0x2a2a3a, roughness: 0.7 })
        );
        ceiling.rotation.x = Math.PI / 2;
        ceiling.position.y = 6;
        this.scene.add(ceiling);
        this.objects.push(ceiling);

        // Camera
        this.camera.position.set(0, 3, 10);
        this.camera.lookAt(new THREE.Vector3(0, 2, 0));
    }

    /**
     * Star Trek 4X scene: Tactical star map.
     */
    _buildStarTrek4xScene() {
        // Deep space background
        this._createStarfield(600);

        // Sector nodes (systems)
        const nodeMat = new THREE.MeshStandardMaterial({
            color: 0x4488ff,
            emissive: 0x4488ff,
            emissiveIntensity: 0.5
        });
        const nodePositions = [];
        for (let i = 0; i < 20; i++) {
            const node = new THREE.Mesh(new THREE.SphereGeometry(0.3, 8, 8), nodeMat);
            const x = (Math.random() - 0.5) * 40;
            const z = (Math.random() - 0.5) * 40;
            node.position.set(x, 0, z);
            this.scene.add(node);
            this.objects.push(node);
            nodePositions.push({ x, z });
        }

        // Trade routes (lines between nodes)
        const lineMat = new THREE.LineBasicMaterial({ color: 0x4488ff, transparent: true, opacity: 0.3 });
        for (let i = 0; i < nodePositions.length - 1; i++) {
            if (Math.random() > 0.5) {
                const points = [
                    new THREE.Vector3(nodePositions[i].x, 0, nodePositions[i].z),
                    new THREE.Vector3(nodePositions[i + 1].x, 0, nodePositions[i + 1].z)
                ];
                const lineGeo = new THREE.BufferGeometry().setFromPoints(points);
                const line = new THREE.Line(lineGeo, lineMat);
                this.scene.add(line);
                this.objects.push(line);
            }
        }

        // Fleet markers (red cones)
        const fleetMat = new THREE.MeshStandardMaterial({
            color: 0xff4444,
            emissive: 0xff4444,
            emissiveIntensity: 0.3
        });
        for (let i = 0; i < 5; i++) {
            const fleet = new THREE.Mesh(new THREE.ConeGeometry(0.3, 0.8, 8), fleetMat);
            const np = nodePositions[Math.floor(Math.random() * nodePositions.length)];
            fleet.position.set(np.x, 0.5, np.z);
            this.scene.add(fleet);
            this.objects.push(fleet);
        }

        // Grid plane
        const gridHelper = new THREE.GridHelper(50, 25, 0x222244, 0x111133);
        gridHelper.position.y = -0.5;
        this.scene.add(gridHelper);
        this.objects.push(gridHelper);

        // Camera: top-down
        this.camera.position.set(0, 30, 0);
        this.camera.lookAt(new THREE.Vector3(0, 0, 0));
    }

    /**
     * Mech Warrior scene: Battlefield terrain.
     */
    _buildMechWarriorScene() {
        // Sky with smoke
        this.scene.background = new THREE.Color(0x3a2a1a);
        this.scene.fog = new THREE.FogExp2(0x3a2a1a, 0.015);

        // Terrain with craters
        const terrainGeo = new THREE.PlaneGeometry(80, 80, 32, 32);
        const positions = terrainGeo.attributes.position.array;
        for (let i = 0; i < positions.length; i += 3) {
            const x = positions[i];
            const y = positions[i + 1];
            // Craters
            const dist1 = Math.sqrt((x - 10) ** 2 + (y - 5) ** 2);
            const dist2 = Math.sqrt((x + 15) ** 2 + (y + 10) ** 2);
            let z = Math.sin(x * 0.05) * Math.cos(y * 0.05) * 2;
            if (dist1 < 5) z -= (5 - dist1) * 0.5;
            if (dist2 < 4) z -= (4 - dist2) * 0.5;
            positions[i + 2] = z;
        }
        terrainGeo.computeVertexNormals();
        const terrainMat = new THREE.MeshStandardMaterial({ color: 0x5a4a3a, roughness: 0.95 });
        const terrain = new THREE.Mesh(terrainGeo, terrainMat);
        terrain.rotation.x = -Math.PI / 2;
        terrain.receiveShadow = true;
        this.scene.add(terrain);
        this.objects.push(terrain);

        // Debris
        const debrisMat = new THREE.MeshStandardMaterial({ color: 0x4a3a2a, roughness: 0.9 });
        for (let i = 0; i < 20; i++) {
            const debris = new THREE.Mesh(
                new THREE.BoxGeometry(Math.random() * 1 + 0.2, Math.random() * 0.5 + 0.1, Math.random() * 1 + 0.2),
                debrisMat
            );
            debris.position.set(
                (Math.random() - 0.5) * 50,
                Math.random() * 0.3,
                (Math.random() - 0.5) * 50
            );
            debris.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, Math.random() * Math.PI);
            this.scene.add(debris);
            this.objects.push(debris);
        }

        // Mech silhouette (player)
        const mechMat = new THREE.MeshStandardMaterial({ color: 0x2a2a2a, roughness: 0.6 });
        const mechBody = new THREE.Mesh(new THREE.BoxGeometry(2, 3, 1.5), mechMat);
        mechBody.position.set(0, 2, 0);
        mechBody.castShadow = true;
        this.scene.add(mechBody);
        this.objects.push(mechBody);

        // Mech head
        const mechHead = new THREE.Mesh(new THREE.BoxGeometry(1, 1, 1), mechMat);
        mechHead.position.set(0, 4, 0);
        this.scene.add(mechHead);
        this.objects.push(mechHead);

        // Camera: third-person behind mech
        this.camera.position.set(0, 5, 10);
        this.camera.lookAt(new THREE.Vector3(0, 2, 0));
    }

    /**
     * Earth 2026 scene: Urban street.
     */
    _buildEarth2026Scene() {
        // Sky
        this.scene.background = new THREE.Color(0x87CEEB);
        this.scene.fog = new THREE.FogExp2(0x87CEEB, 0.01);

        // Street
        const streetGeo = new THREE.PlaneGeometry(10, 60);
        const streetMat = new THREE.MeshStandardMaterial({ color: 0x3a3a3a, roughness: 0.8 });
        const street = new THREE.Mesh(streetGeo, streetMat);
        street.rotation.x = -Math.PI / 2;
        street.receiveShadow = true;
        this.scene.add(street);
        this.objects.push(street);

        // Sidewalks
        const sidewalkMat = new THREE.MeshStandardMaterial({ color: 0x8a8a8a, roughness: 0.8 });
        for (const side of [-1, 1]) {
            const sidewalk = new THREE.Mesh(new THREE.BoxGeometry(3, 0.2, 60), sidewalkMat);
            sidewalk.position.set(side * 6.5, 0.1, 0);
            sidewalk.receiveShadow = true;
            this.scene.add(sidewalk);
            this.objects.push(sidewalk);
        }

        // Buildings
        const buildingColors = [0x6a6a7a, 0x7a6a5a, 0x5a6a7a, 0x8a7a6a, 0x6a7a6a];
        for (const side of [-1, 1]) {
            for (let i = 0; i < 8; i++) {
                const height = Math.random() * 10 + 5;
                const width = Math.random() * 3 + 3;
                const color = buildingColors[Math.floor(Math.random() * buildingColors.length)];
                const building = new THREE.Mesh(
                    new THREE.BoxGeometry(width, height, 8),
                    new THREE.MeshStandardMaterial({ color, roughness: 0.8 })
                );
                building.position.set(side * (10 + Math.random() * 5), height / 2, -25 + i * 8);
                building.castShadow = true;
                building.receiveShadow = true;
                this.scene.add(building);
                this.objects.push(building);
            }
        }

        // Street lights
        const poleMat = new THREE.MeshStandardMaterial({ color: 0x4a4a4a });
        for (let i = 0; i < 6; i++) {
            for (const side of [-1, 1]) {
                const pole = new THREE.Mesh(new THREE.CylinderGeometry(0.1, 0.1, 5), poleMat);
                pole.position.set(side * 5, 2.5, -20 + i * 10);
                this.scene.add(pole);
                this.objects.push(pole);

                const light = new THREE.PointLight(0xffaa44, 0.5, 10);
                light.position.set(side * 5, 5, -20 + i * 10);
                this.scene.add(light);
                this.lights.push(light);
            }
        }

        // Vehicles (simple boxes)
        const vehicleMat = new THREE.MeshStandardMaterial({ color: 0x4a4a6a });
        for (let i = 0; i < 4; i++) {
            const vehicle = new THREE.Mesh(new THREE.BoxGeometry(2, 1.5, 4), vehicleMat);
            vehicle.position.set((Math.random() > 0.5 ? 1 : -1) * 3, 0.75, -15 + i * 12);
            vehicle.castShadow = true;
            this.scene.add(vehicle);
            this.objects.push(vehicle);
        }

        // Camera: third-person
        this.camera.position.set(0, 3, 10);
        this.camera.lookAt(new THREE.Vector3(0, 1, 0));
    }

    /**
     * Zero Zero Seven scene: Cold War spy office.
     */
    _buildZeroZeroSevenScene() {
        // Dark room
        this.scene.background = new THREE.Color(0x0a0a0a);
        this.scene.fog = new THREE.FogExp2(0x0a0a0a, 0.03);

        // Floor
        const floorGeo = new THREE.PlaneGeometry(15, 12);
        const floorMat = new THREE.MeshStandardMaterial({ color: 0x2a1a1a, roughness: 0.7 });
        const floor = new THREE.Mesh(floorGeo, floorMat);
        floor.rotation.x = -Math.PI / 2;
        floor.receiveShadow = true;
        this.scene.add(floor);
        this.objects.push(floor);

        // Walls
        const wallMat = new THREE.MeshStandardMaterial({ color: 0x3a2a2a, roughness: 0.8 });
        const backWall = new THREE.Mesh(new THREE.BoxGeometry(15, 8, 0.5), wallMat);
        backWall.position.set(0, 4, -6);
        this.scene.add(backWall);
        this.objects.push(backWall);

        const leftWall = new THREE.Mesh(new THREE.BoxGeometry(0.5, 8, 12), wallMat);
        leftWall.position.set(-7.5, 4, 0);
        this.scene.add(leftWall);
        this.objects.push(leftWall);

        const rightWall = new THREE.Mesh(new THREE.BoxGeometry(0.5, 8, 12), wallMat);
        rightWall.position.set(7.5, 4, 0);
        this.scene.add(rightWall);
        this.objects.push(rightWall);

        // Ceiling
        const ceiling = new THREE.Mesh(
            new THREE.PlaneGeometry(15, 12),
            new THREE.MeshStandardMaterial({ color: 0x1a1a1a, roughness: 0.9 })
        );
        ceiling.rotation.x = Math.PI / 2;
        ceiling.position.y = 8;
        this.scene.add(ceiling);
        this.objects.push(ceiling);

        // Desk
        const deskMat = new THREE.MeshStandardMaterial({ color: 0x4a3a2a, roughness: 0.6 });
        const deskTop = new THREE.Mesh(new THREE.BoxGeometry(4, 0.2, 2), deskMat);
        deskTop.position.set(0, 1.5, -3);
        deskTop.castShadow = true;
        this.scene.add(deskTop);
        this.objects.push(deskTop);

        // Desk legs
        for (const x of [-1.8, 1.8]) {
            for (const z of [-3.8, -2.2]) {
                const leg = new THREE.Mesh(new THREE.CylinderGeometry(0.05, 0.05, 1.5), deskMat);
                leg.position.set(x, 0.75, z);
                this.scene.add(leg);
                this.objects.push(leg);
            }
        }

        // Secure terminal (glowing screen on desk)
        const terminalMat = new THREE.MeshStandardMaterial({
            color: 0x00ff44,
            emissive: 0x00ff44,
            emissiveIntensity: 0.8
        });
        const screen = new THREE.Mesh(new THREE.PlaneGeometry(1.5, 1), terminalMat);
        screen.position.set(0, 2.2, -3.9);
        this.scene.add(screen);
        this.objects.push(screen);

        // Screen glow light
        const screenLight = new THREE.PointLight(0x00ff44, 0.5, 5);
        screenLight.position.set(0, 2.2, -3.5);
        this.scene.add(screenLight);
        this.lights.push(screenLight);

        // Red phone on desk
        const phoneMat = new THREE.MeshStandardMaterial({ color: 0xcc0000, roughness: 0.4 });
        const phone = new THREE.Mesh(new THREE.BoxGeometry(0.4, 0.15, 0.3), phoneMat);
        phone.position.set(1.5, 1.63, -3);
        this.scene.add(phone);
        this.objects.push(phone);

        // Red phone glow
        const phoneLight = new THREE.PointLight(0xff0000, 0.3, 3);
        phoneLight.position.set(1.5, 1.8, -3);
        phoneLight.name = 'redPhoneLight';
        this.scene.add(phoneLight);
        this.lights.push(phoneLight);

        // Filing cabinets
        const cabinetMat = new THREE.MeshStandardMaterial({ color: 0x5a5a5a, roughness: 0.5 });
        for (let i = 0; i < 3; i++) {
            const cabinet = new THREE.Mesh(new THREE.BoxGeometry(1, 3, 0.8), cabinetMat);
            cabinet.position.set(-5 + i * 1.2, 1.5, -5.5);
            cabinet.castShadow = true;
            this.scene.add(cabinet);
            this.objects.push(cabinet);
        }

        // World map on wall
        const mapMat = new THREE.MeshStandardMaterial({
            color: 0x4a5a3a,
            emissive: 0x1a2a0a,
            emissiveIntensity: 0.2
        });
        const map = new THREE.Mesh(new THREE.PlaneGeometry(6, 3), mapMat);
        map.position.set(0, 5, -5.7);
        this.scene.add(map);
        this.objects.push(map);

        // Map pins (red dots)
        const pinMat = new THREE.MeshStandardMaterial({
            color: 0xff0000,
            emissive: 0xff0000,
            emissiveIntensity: 0.5
        });
        for (let i = 0; i < 8; i++) {
            const pin = new THREE.Mesh(new THREE.SphereGeometry(0.08, 8, 8), pinMat);
            pin.position.set(
                -2.5 + Math.random() * 5,
                3.8 + Math.random() * 2,
                -5.6
            );
            this.scene.add(pin);
            this.objects.push(pin);
        }

        // Chair
        const chairMat = new THREE.MeshStandardMaterial({ color: 0x2a1a1a, roughness: 0.7 });
        const chairSeat = new THREE.Mesh(new THREE.BoxGeometry(0.8, 0.1, 0.8), chairMat);
        chairSeat.position.set(0, 1, -1.5);
        this.scene.add(chairSeat);
        this.objects.push(chairSeat);

        const chairBack = new THREE.Mesh(new THREE.BoxGeometry(0.8, 1, 0.1), chairMat);
        chairBack.position.set(0, 1.5, -1.1);
        this.scene.add(chairBack);
        this.objects.push(chairBack);

        // Camera: third-person, looking at the desk
        this.camera.position.set(0, 3, 5);
        this.camera.lookAt(new THREE.Vector3(0, 1.5, -3));
    }

    /**
     * Star Wars scene: Space battle with starships and starfield.
     */
    _buildStarWarsScene() {
        // Deep space background
        this.scene.background = new THREE.Color(0x050510);
        this.scene.fog = new THREE.FogExp2(0x050510, 0.008);

        // Starfield
        this._createStarfield(800);

        // Distant planet
        const planetGeo = new THREE.SphereGeometry(15, 32, 32);
        const planetMat = new THREE.MeshStandardMaterial({
            color: 0x2244aa,
            roughness: 0.8,
            emissive: 0x112244,
            emissiveIntensity: 0.1
        });
        const planet = new THREE.Mesh(planetGeo, planetMat);
        planet.position.set(40, -10, -60);
        this.scene.add(planet);
        this.objects.push(planet);

        // Planet atmosphere glow
        const atmosGeo = new THREE.SphereGeometry(16, 32, 32);
        const atmosMat = new THREE.MeshStandardMaterial({
            color: 0x4488ff,
            transparent: true,
            opacity: 0.1,
            emissive: 0x4488ff,
            emissiveIntensity: 0.2
        });
        const atmosphere = new THREE.Mesh(atmosGeo, atmosMat);
        atmosphere.position.copy(planet.position);
        this.scene.add(atmosphere);
        this.objects.push(atmosphere);

        // X-Wing silhouette (simplified)
        const xwingGroup = new THREE.Group();
        const hullMat = new THREE.MeshStandardMaterial({ color: 0xcccccc, roughness: 0.6 });
        const wingMat = new THREE.MeshStandardMaterial({ color: 0xaaaaaa, roughness: 0.7 });

        // Main hull
        const hull = new THREE.Mesh(new THREE.BoxGeometry(0.3, 0.3, 2), hullMat);
        xwingGroup.add(hull);

        // Wings (X configuration)
        const wing1 = new THREE.Mesh(new THREE.BoxGeometry(3, 0.05, 0.8), wingMat);
        wing1.rotation.z = Math.PI / 6;
        wing1.position.set(0, 0, -0.3);
        xwingGroup.add(wing1);

        const wing2 = new THREE.Mesh(new THREE.BoxGeometry(3, 0.05, 0.8), wingMat);
        wing2.rotation.z = -Math.PI / 6;
        wing2.position.set(0, 0, 0.3);
        xwingGroup.add(wing2);

        // Engine glow
        const engineMat = new THREE.MeshStandardMaterial({
            color: 0x00aaff,
            emissive: 0x00aaff,
            emissiveIntensity: 1
        });
        const engine1 = new THREE.Mesh(new THREE.SphereGeometry(0.1, 8, 8), engineMat);
        engine1.position.set(0.8, 0.3, -0.5);
        xwingGroup.add(engine1);
        const engine2 = engine1.clone();
        engine2.position.set(-0.8, 0.3, -0.5);
        xwingGroup.add(engine2);
        const engine3 = engine1.clone();
        engine3.position.set(0.8, 0.3, 0.5);
        xwingGroup.add(engine3);
        const engine4 = engine1.clone();
        engine4.position.set(-0.8, 0.3, 0.5);
        xwingGroup.add(engine4);

        xwingGroup.position.set(-5, 3, 5);
        xwingGroup.name = 'xwing';
        this.scene.add(xwingGroup);
        this.objects.push(xwingGroup);

        // TIE Fighter silhouette (simplified)
        const tieGroup = new THREE.Group();
        const tieHullMat = new THREE.MeshStandardMaterial({ color: 0x333333, roughness: 0.5 });
        const panelMat = new THREE.MeshStandardMaterial({ color: 0x555555, roughness: 0.6 });

        // Cockpit sphere
        const cockpit = new THREE.Mesh(new THREE.SphereGeometry(0.3, 8, 8), tieHullMat);
        tieGroup.add(cockpit);

        // Solar panels (hexagonal)
        const panel1 = new THREE.Mesh(new THREE.BoxGeometry(0.1, 2.5, 0.8), panelMat);
        panel1.position.set(0, 0, 0.8);
        tieGroup.add(panel1);
        const panel2 = panel1.clone();
        panel2.position.set(0, 0, -0.8);
        tieGroup.add(panel2);

        // Connecting struts
        const strut1 = new THREE.Mesh(new THREE.BoxGeometry(0.05, 0.05, 0.8), tieHullMat);
        strut1.position.set(0, 1.2, 0);
        tieGroup.add(strut1);
        const strut2 = strut1.clone();
        strut2.position.set(0, -1.2, 0);
        tieGroup.add(strut2);

        tieGroup.position.set(5, 4, -3);
        tieGroup.name = 'tiefighter';
        this.scene.add(tieGroup);
        this.objects.push(tieGroup);

        // Capital ship silhouette (Star Destroyer-like)
        const destroyerGroup = new THREE.Group();
        const destroyerMat = new THREE.MeshStandardMaterial({ color: 0x888888, roughness: 0.7 });

        // Main hull (wedge shape approximated)
        const mainHull = new THREE.Mesh(new THREE.BoxGeometry(2, 1, 8), destroyerMat);
        destroyerGroup.add(mainHull);

        // Bridge tower
        const bridge = new THREE.Mesh(new THREE.BoxGeometry(0.5, 0.8, 1), destroyerMat);
        bridge.position.set(0, 0.8, 2);
        destroyerGroup.add(bridge);

        // Engine glow
        const capEngineMat = new THREE.MeshStandardMaterial({
            color: 0xff4400,
            emissive: 0xff4400,
            emissiveIntensity: 0.8
        });
        const capEngine = new THREE.Mesh(new THREE.SphereGeometry(0.2, 8, 8), capEngineMat);
        capEngine.position.set(0, 0, -4.2);
        destroyerGroup.add(capEngine);

        destroyerGroup.position.set(15, 8, -20);
        destroyerGroup.rotation.y = -0.3;
        destroyerGroup.name = 'stardestroyer';
        this.scene.add(destroyerGroup);
        this.objects.push(destroyerGroup);

        // Laser fire effects (red and green beams)
        const laserColors = [0xff0000, 0x00ff00, 0xff0000, 0x00ff00];
        for (let i = 0; i < 4; i++) {
            const laserGeo = new THREE.CylinderGeometry(0.02, 0.02, 3, 4);
            const laserMat = new THREE.MeshStandardMaterial({
                color: laserColors[i],
                emissive: laserColors[i],
                emissiveIntensity: 2,
                transparent: true,
                opacity: 0.8
            });
            const laser = new THREE.Mesh(laserGeo, laserMat);
            laser.rotation.z = Math.PI / 2;
            laser.position.set(
                (Math.random() - 0.5) * 10,
                2 + Math.random() * 4,
                -2 + Math.random() * 4
            );
            laser.name = 'laser';
            this.scene.add(laser);
            this.objects.push(laser);
        }

        // Explosion effects
        for (let i = 0; i < 2; i++) {
            const expGeo = new THREE.SphereGeometry(0.5 + Math.random() * 0.5, 8, 8);
            const expMat = new THREE.MeshStandardMaterial({
                color: 0xff6600,
                emissive: 0xff4400,
                emissiveIntensity: 1,
                transparent: true,
                opacity: 0.7
            });
            const explosion = new THREE.Mesh(expGeo, expMat);
            explosion.position.set(
                (Math.random() - 0.5) * 8,
                3 + Math.random() * 3,
                -1 + Math.random() * 3
            );
            explosion.name = 'explosion';
            this.scene.add(explosion);
            this.objects.push(explosion);
        }

        // Camera: third-person, looking at the battle
        this.camera.position.set(0, 8, 15);
        this.camera.lookAt(new THREE.Vector3(0, 2, 0));
    }

    /**
     * Create a starfield particle system.
     */
    _createStarfield(count) {
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        const colors = new Float32Array(count * 3);

        for (let i = 0; i < count; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 200;
            positions[i * 3 + 1] = (Math.random() - 0.5) * 200;
            positions[i * 3 + 2] = (Math.random() - 0.5) * 200;

            // Star color variation
            const color = new THREE.Color();
            color.setHSL(Math.random() * 0.2 + 0.55, 0.3 + Math.random() * 0.3, 0.7 + Math.random() * 0.3);
            colors[i * 3] = color.r;
            colors[i * 3 + 1] = color.g;
            colors[i * 3 + 2] = color.b;
        }

        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));

        const material = new THREE.PointsMaterial({
            size: 0.5,
            vertexColors: true,
            transparent: true,
            opacity: 0.8
        });

        this.particles = new THREE.Points(geometry, material);
        this.scene.add(this.particles);
        this.objects.push(this.particles);
    }

    /**
     * Update scene with new objects based on game state.
     * @param {Object} state - Game state object with theme-specific data.
     */
    updateScene(state) {
        if (!this.isInitialized) return;

        const theme = this.config.theme;

        // Theme-specific updates
        if (theme === 'captains-log' && state.redAlert !== undefined) {
            const redLight = this.scene.getObjectByName('redAlertLight');
            if (redLight) {
                redLight.intensity = state.redAlert ? 2 : 0;
            }
        }

        if (theme === 'dnd-5e' && state.combatants) {
            // Add combatant markers
            this._updateCombatantMarkers(state.combatants);
        }

        if (theme === 'star-trek-4x' && state.systems) {
            // Update sector nodes
            this._updateSectorNodes(state.systems, state.fleets);
        }

        if (theme === 'mech-warrior' && state.explosions) {
            // Add explosion effects
            for (const exp of state.explosions) {
                this._addExplosion(exp.x, exp.y, exp.z);
            }
        }
    }

    /**
     * Update combatant markers in D&D scene.
     */
    _updateCombatantMarkers(combatants) {
        // Remove old markers
        const oldMarkers = this.scene.children.filter(c => c.name === 'combatant');
        for (const marker of oldMarkers) {
            this.scene.remove(marker);
        }

        // Add new markers
        if (!combatants) return;
        const colors = [0xff4444, 0x44ff44, 0x4444ff, 0xffff44, 0xff44ff, 0x44ffff];
        combatants.forEach((c, i) => {
            const marker = new THREE.Mesh(
                new THREE.CylinderGeometry(0.3, 0.3, 0.1, 8),
                new THREE.MeshStandardMaterial({
                    color: colors[i % colors.length],
                    emissive: colors[i % colors.length],
                    emissiveIntensity: 0.3
                })
            );
            marker.name = 'combatant';
            marker.position.set((i % 5 - 2) * 2, 0.1, (Math.floor(i / 5) - 1) * 2);
            this.scene.add(marker);
        });
    }

    /**
     * Update sector nodes in Star Trek 4X scene.
     */
    _updateSectorNodes(systems, fleets) {
        // Remove old nodes
        const oldNodes = this.scene.children.filter(c => c.name === 'sectorNode' || c.name === 'fleetMarker');
        for (const node of oldNodes) {
            this.scene.remove(node);
        }

        if (!systems) return;

        // Add system nodes
        const nodeMat = new THREE.MeshStandardMaterial({
            color: 0x4488ff,
            emissive: 0x4488ff,
            emissiveIntensity: 0.5
        });
        systems.forEach((sys, i) => {
            const node = new THREE.Mesh(new THREE.SphereGeometry(0.3, 8, 8), nodeMat);
            node.name = 'sectorNode';
            node.position.set((i % 5 - 2) * 8, 0, (Math.floor(i / 5) - 2) * 8);
            this.scene.add(node);
        });

        // Add fleet markers
        if (fleets) {
            const fleetMat = new THREE.MeshStandardMaterial({
                color: 0xff4444,
                emissive: 0xff4444,
                emissiveIntensity: 0.3
            });
            fleets.forEach((fleet, i) => {
                const marker = new THREE.Mesh(new THREE.ConeGeometry(0.3, 0.8, 8), fleetMat);
                marker.name = 'fleetMarker';
                marker.position.set((i % 5 - 2) * 8, 0.5, (Math.floor(i / 5) - 2) * 8);
                this.scene.add(marker);
            });
        }
    }

    /**
     * Add explosion effect in Mech Warrior scene.
     */
    _addExplosion(x, y, z) {
        const explosionGeo = new THREE.SphereGeometry(1, 8, 8);
        const explosionMat = new THREE.MeshStandardMaterial({
            color: 0xff4400,
            emissive: 0xff4400,
            emissiveIntensity: 1,
            transparent: true,
            opacity: 0.8
        });
        const explosion = new THREE.Mesh(explosionGeo, explosionMat);
        explosion.position.set(x, y, z);
        this.scene.add(explosion);

        // Fade out
        let opacity = 0.8;
        const fade = () => {
            opacity -= 0.05;
            if (opacity <= 0) {
                this.scene.remove(explosion);
                return;
            }
            explosion.material.opacity = opacity;
            explosion.scale.multiplyScalar(1.1);
            requestAnimationFrame(fade);
        };
        fade();
    }

    /**
     * Set camera position and target.
     */
    setCamera(position, target) {
        if (!this.camera) return;
        if (position) {
            this.camera.position.set(position.x || 0, position.y || 0, position.z || 0);
        }
        if (target) {
            this.camera.lookAt(new THREE.Vector3(target.x || 0, target.y || 0, target.z || 0));
        }
    }

    /**
     * Set lighting.
     */
    setLighting(ambient, directional) {
        if (!this.lights.length) return;
        if (ambient) {
            const ambLight = this.lights.find(l => l.isAmbientLight);
            if (ambLight) {
                ambLight.intensity = ambient.intensity || ambLight.intensity;
                if (ambient.color) ambLight.color.set(ambient.color);
            }
        }
        if (directional) {
            const dirLight = this.lights.find(l => l.isDirectionalLight);
            if (dirLight) {
                dirLight.intensity = directional.intensity || dirLight.intensity;
                if (directional.color) dirLight.color.set(directional.color);
            }
        }
    }

    /**
     * Toggle red alert mode (Captain's Log).
     */
    toggleRedAlert(enabled) {
        if (!this.isInitialized) return;
        const redLight = this.scene.getObjectByName('redAlertLight');
        if (redLight) {
            redLight.intensity = enabled ? 2 : 0;
        }
    }

    /**
     * Handle container resize.
     */
    _onResize() {
        if (!this.renderer || !this.camera) return;
        const width = this.container.clientWidth;
        const height = this.container.clientHeight;
        if (width === 0 || height === 0) return;
        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
        this.renderer.setSize(width, height);
    }

    /**
     * Animation loop.
     */
    _setupAnimationLoop() {
        const animate = () => {
            this.animationId = requestAnimationFrame(animate);

            // Rotate starfield
            if (this.particles) {
                this.particles.rotation.y += 0.0002;
            }

            // Flicker torch lights (D&D)
            if (this.config.theme === 'dnd-5e') {
                this.lights.forEach(light => {
                    if (light.name === 'torch') {
                        light.intensity = 0.8 + Math.sin(Date.now() * 0.01) * 0.2 + Math.random() * 0.1;
                    }
                });
            }

            this.renderer.render(this.scene, this.camera);
        };
        animate();
    }

    /**
     * Destroy the scene and clean up resources.
     */
    destroy() {
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }
        if (this._resizeHandler) {
            window.removeEventListener('resize', this._resizeHandler);
            this._resizeHandler = null;
        }
        if (this.renderer) {
            if (this.container.contains(this.renderer.domElement)) {
                this.container.removeChild(this.renderer.domElement);
            }
            this.renderer.dispose();
            this.renderer = null;
        }
        if (this.scene) {
            this.scene.children.forEach(child => {
                if (child.geometry) child.geometry.dispose();
                if (child.material) {
                    if (Array.isArray(child.material)) {
                        child.material.forEach(m => m.dispose());
                    } else {
                        child.material.dispose();
                    }
                }
            });
            this.scene = null;
        }
        this.camera = null;
        this.objects = [];
        this.lights = [];
        this.particles = null;
        this.isInitialized = false;
        console.log('[SceneRenderer] Scene destroyed');
    }

    // ===== Interactive 3D Features =====

    /**
     * Enable orbit controls for camera navigation.
     * Allows mouse drag to orbit, scroll to zoom, right-drag to pan.
     */
    enableOrbitControls() {
        if (!this.isInitialized || typeof THREE === 'undefined') return;

        // Simple orbit controls implementation
        this.orbitState = {
            isDragging: false,
            isPanning: false,
            previousMouse: { x: 0, y: 0 },
            spherical: { theta: 0, phi: Math.PI / 4, radius: 10 },
            target: new THREE.Vector3(0, 0, 0),
            minRadius: 2,
            maxRadius: 50,
            minPhi: 0.1,
            maxPhi: Math.PI - 0.1
        };

        const canvas = this.renderer.domElement;

        canvas.addEventListener('mousedown', (e) => {
            if (e.button === 0) {
                this.orbitState.isDragging = true;
            } else if (e.button === 2) {
                this.orbitState.isPanning = true;
            }
            this.orbitState.previousMouse = { x: e.clientX, y: e.clientY };
        });

        canvas.addEventListener('mousemove', (e) => {
            const delta = {
                x: e.clientX - this.orbitState.previousMouse.x,
                y: e.clientY - this.orbitState.previousMouse.y
            };

            if (this.orbitState.isDragging) {
                this.orbitState.spherical.theta -= delta.x * 0.01;
                this.orbitState.spherical.phi -= delta.y * 0.01;
                this.orbitState.spherical.phi = Math.max(
                    this.orbitState.minPhi,
                    Math.min(this.orbitState.maxPhi, this.orbitState.spherical.phi)
                );
                this._updateCameraFromSpherical();
            }

            if (this.orbitState.isPanning) {
                const panSpeed = this.orbitState.spherical.radius * 0.002;
                const right = new THREE.Vector3();
                const up = new THREE.Vector3();
                this.camera.getWorldDirection(new THREE.Vector3());
                right.crossVectors(this.camera.up, new THREE.Vector3().subVectors(this.camera.position, this.orbitState.target).normalize()).normalize();
                up.copy(this.camera.up);
                this.orbitState.target.add(right.multiplyScalar(-delta.x * panSpeed));
                this.orbitState.target.add(up.multiplyScalar(delta.y * panSpeed));
                this._updateCameraFromSpherical();
            }

            this.orbitState.previousMouse = { x: e.clientX, y: e.clientY };
        });

        canvas.addEventListener('mouseup', () => {
            this.orbitState.isDragging = false;
            this.orbitState.isPanning = false;
        });

        canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            this.orbitState.spherical.radius += e.deltaY * 0.01;
            this.orbitState.spherical.radius = Math.max(
                this.orbitState.minRadius,
                Math.min(this.orbitState.maxRadius, this.orbitState.spherical.radius)
            );
            this._updateCameraFromSpherical();
        }, { passive: false });

        canvas.addEventListener('contextmenu', (e) => e.preventDefault());

        // Initialize spherical coordinates from current camera position
        const dir = new THREE.Vector3().subVectors(this.camera.position, this.orbitState.target);
        this.orbitState.spherical.radius = dir.length();
        this.orbitState.spherical.phi = Math.acos(dir.y / this.orbitState.spherical.radius);
        this.orbitState.spherical.theta = Math.atan2(dir.x, dir.z);

        console.log('[SceneRenderer] Orbit controls enabled');
    }

    /**
     * Update camera position from spherical coordinates.
     */
    _updateCameraFromSpherical() {
        const s = this.orbitState.spherical;
        const t = this.orbitState.target;

        this.camera.position.set(
            t.x + s.radius * Math.sin(s.phi) * Math.sin(s.theta),
            t.y + s.radius * Math.cos(s.phi),
            t.z + s.radius * Math.sin(s.phi) * Math.cos(s.theta)
        );
        this.camera.lookAt(t);
    }

    /**
     * Set up raycasting for interactive object selection.
     * @param {Function} onClick - Callback when an object is clicked
     * @param {Function} onHover - Callback when hovering over an object
     */
    enableInteraction(onClick, onHover) {
        if (!this.isInitialized || typeof THREE === 'undefined') return;

        this.raycaster = new THREE.Raycaster();
        this.mouse = new THREE.Vector2();
        this.onClickCallback = onClick;
        this.onHoverCallback = onHover;
        this.hoveredObject = null;
        this.interactiveObjects = [];

        const canvas = this.renderer.domElement;

        canvas.addEventListener('click', (e) => {
            const rect = canvas.getBoundingClientRect();
            this.mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
            this.mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1;

            this.raycaster.setFromCamera(this.mouse, this.camera);
            const intersects = this.raycaster.intersectObjects(this.interactiveObjects, true);

            if (intersects.length > 0) {
                let obj = intersects[0].object;
                // Walk up to find the root interactive object
                while (obj.parent && !obj.userData.interactive) {
                    obj = obj.parent;
                }
                if (obj.userData.interactive && onClick) {
                    onClick(obj.userData);
                }
            }
        });

        canvas.addEventListener('mousemove', (e) => {
            const rect = canvas.getBoundingClientRect();
            this.mouse.x = ((e.clientX - rect.left) / rect.width) * 2 - 1;
            this.mouse.y = -((e.clientY - rect.top) / rect.height) * 2 + 1;

            this.raycaster.setFromCamera(this.mouse, this.camera);
            const intersects = this.raycaster.intersectObjects(this.interactiveObjects, true);

            // Reset previous hover
            if (this.hoveredObject) {
                this._setHoverHighlight(this.hoveredObject, false);
                this.hoveredObject = null;
            }

            if (intersects.length > 0) {
                let obj = intersects[0].object;
                while (obj.parent && !obj.userData.interactive) {
                    obj = obj.parent;
                }
                if (obj.userData.interactive) {
                    this.hoveredObject = obj;
                    this._setHoverHighlight(obj, true);
                    canvas.style.cursor = 'pointer';
                    if (onHover) {
                        onHover(obj.userData);
                    }
                } else {
                    canvas.style.cursor = 'default';
                }
            } else {
                canvas.style.cursor = 'default';
            }
        });

        console.log('[SceneRenderer] Interaction enabled');
    }

    /**
     * Set hover highlight on an object.
     */
    _setHoverHighlight(obj, highlight) {
        obj.traverse((child) => {
            if (child.isMesh && child.material) {
                if (highlight) {
                    child.material._originalEmissive = child.material.emissive ? child.material.emissive.getHex() : 0;
                    child.material._originalEmissiveIntensity = child.material.emissiveIntensity || 0;
                    if (child.material.emissive) {
                        child.material.emissive.setHex(0x444444);
                        child.material.emissiveIntensity = 0.5;
                    }
                } else {
                    if (child.material.emissive && child.material._originalEmissive !== undefined) {
                        child.material.emissive.setHex(child.material._originalEmissive);
                        child.material.emissiveIntensity = child.material._originalEmissiveIntensity;
                    }
                }
            }
        });
    }

    /**
     * Add an interactive 3D object to the scene.
     * @param {Object} options - Object configuration
     * @param {THREE.Geometry|THREE.BufferGeometry} options.geometry - 3D geometry
     * @param {THREE.Material} options.material - Material
     * @param {Vector3} options.position - Position {x, y, z}
     * @param {string} options.id - Unique identifier
     * @param {string} options.name - Display name
     * @param {string} options.type - Object type (planet, ship, station, etc.)
     * @param {Object} options.data - Additional data
     */
    addInteractiveObject(options) {
        if (!this.isInitialized) return null;

        const mesh = new THREE.Mesh(options.geometry, options.material);
        mesh.position.set(
            options.position?.x || 0,
            options.position?.y || 0,
            options.position?.z || 0
        );

        if (options.rotation) {
            mesh.rotation.set(
                options.rotation.x || 0,
                options.rotation.y || 0,
                options.rotation.z || 0
            );
        }

        if (options.scale) {
            mesh.scale.set(
                options.scale.x || 1,
                options.scale.y || 1,
                options.scale.z || 1
            );
        }

        mesh.userData = {
            interactive: true,
            id: options.id,
            name: options.name,
            type: options.type,
            data: options.data || {}
        };

        this.scene.add(mesh);
        this.interactiveObjects.push(mesh);

        // Add 3D label if name provided
        if (options.name && this.canvasTexture) {
            this.addLabel(mesh, options.name);
        }

        console.log(`[SceneRenderer] Added interactive object: ${options.name || options.id}`);
        return mesh;
    }

    /**
     * Remove an interactive object by ID.
     */
    removeInteractiveObject(id) {
        const index = this.interactiveObjects.findIndex(obj => obj.userData.id === id);
        if (index !== -1) {
            const obj = this.interactiveObjects[index];
            this.scene.remove(obj);
            this.interactiveObjects.splice(index, 1);
            console.log(`[SceneRenderer] Removed interactive object: ${id}`);
        }
    }

    /**
     * Update position of an interactive object.
     */
    updateObjectPosition(id, position) {
        const obj = this.interactiveObjects.find(o => o.userData.id === id);
        if (obj && position) {
            obj.position.set(position.x || 0, position.y || 0, position.z || 0);
        }
    }

    /**
     * Add a 3D text label above an object.
     */
    addLabel(parentObject, text) {
        if (typeof THREE === 'undefined') return;

        // Create canvas texture for text
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = 256;
        canvas.height = 64;

        ctx.fillStyle = 'rgba(0, 0, 0, 0.6)';
        ctx.roundRect(0, 0, 256, 64, 10);
        ctx.fill();

        ctx.font = 'Bold 24px Arial';
        ctx.fillStyle = '#ffffff';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(text, 128, 32);

        const texture = new THREE.CanvasTexture(canvas);
        const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
        const sprite = new THREE.Sprite(spriteMat);
        sprite.scale.set(3, 0.75, 1);
        sprite.position.y = 2;

        parentObject.add(sprite);
        return sprite;
    }

    /**
     * Create a 3D galaxy map with interactive sectors and systems.
     * @param {Object} galaxyData - Galaxy data from server
     */
    createGalaxyMap3D(galaxyData) {
        if (!this.isInitialized || !galaxyData) return;

        // Clear existing interactive objects
        this.interactiveObjects.forEach(obj => this.scene.remove(obj));
        this.interactiveObjects = [];

        if (!galaxyData.sectors) return;

        galaxyData.sectors.forEach(sector => {
            // Sector marker
            const sectorGeo = new THREE.BoxGeometry(1.5, 0.1, 1.5);
            const isPlayerSector = sector.controller && galaxyData.playerId === sector.controller;
            const sectorMat = new THREE.MeshStandardMaterial({
                color: isPlayerSector ? 0x00ff00 : (sector.controller ? 0xff0000 : 0x333366),
                emissive: isPlayerSector ? 0x004400 : (sector.controller ? 0x440000 : 0x111133),
                emissiveIntensity: 0.3,
                transparent: true,
                opacity: 0.8
            });

            this.addInteractiveObject({
                geometry: sectorGeo,
                material: sectorMat,
                position: {
                    x: sector.col * 2,
                    y: 0,
                    z: -sector.row * 2
                },
                id: `sector_${sector.id}`,
                name: `Sector ${sector.row},${sector.col}`,
                type: 'sector',
                data: sector
            });

            // Sub-sector systems
            if (sector.subSectors) {
                sector.subSectors.forEach(subSector => {
                    if (subSector.solarSystem) {
                        const sys = subSector.solarSystem;
                        const sysGeo = new THREE.SphereGeometry(0.3, 16, 16);
                        const sysMat = new THREE.MeshStandardMaterial({
                            color: sys.explored ? 0xffffff : 0x666666,
                            emissive: sys.explored ? 0x444444 : 0x222222,
                            emissiveIntensity: 0.2
                        });

                        this.addInteractiveObject({
                            geometry: sysGeo,
                            material: sysMat,
                            position: {
                                x: (sector.col * 2) + ((subSector.col || 0) * 0.4 - 0.6),
                                y: 0.3,
                                z: -(sector.row * 2) + ((subSector.row || 0) * 0.4 - 0.6)
                            },
                            id: `system_${sys.id}`,
                            name: sys.name || `System ${sys.id}`,
                            type: 'solarSystem',
                            data: sys
                        });
                    }
                });
            }
        });

        // Add fleet markers
        if (galaxyData.fleets) {
            galaxyData.fleets.forEach((fleet, i) => {
                const fleetGeo = new THREE.ConeGeometry(0.2, 0.6, 8);
                const fleetMat = new THREE.MeshStandardMaterial({
                    color: 0xffff00,
                    emissive: 0x888800,
                    emissiveIntensity: 0.5
                });

                this.addInteractiveObject({
                    geometry: fleetGeo,
                    material: fleetMat,
                    position: {
                        x: fleet.sectorCol * 2 + (fleet.subSectorCol * 0.4 - 0.6),
                        y: 0.8,
                        z: -(fleet.sectorRow * 2) + (fleet.subSectorRow * 0.4 - 0.6)
                    },
                    id: `fleet_${fleet.id}`,
                    name: fleet.name || `Fleet ${i}`,
                    type: 'fleet',
                    data: fleet
                });
            });
        }

        console.log(`[SceneRenderer] Galaxy map created with ${this.interactiveObjects.length} objects`);
    }

    /**
     * Create a 3D game selection carousel.
     * @param {Array} games - List of game objects with name, description, icon
     */
    createGameCarousel(games) {
        if (!this.isInitialized || !games) return;

        // Clear existing
        this.interactiveObjects.forEach(obj => this.scene.remove(obj));
        this.interactiveObjects = [];

        const radius = 8;
        const angleStep = (2 * Math.PI) / games.length;

        games.forEach((game, i) => {
            const angle = i * angleStep;
            const x = Math.sin(angle) * radius;
            const z = -Math.cos(angle) * radius;

            // Game planet/sphere
            const colors = [0x4488ff, 0xff4444, 0x44ff44, 0xffff44, 0xff44ff, 0x44ffff, 0xff8844, 0x8844ff];
            const color = colors[i % colors.length];

            const geo = new THREE.SphereGeometry(1.5, 32, 32);
            const mat = new THREE.MeshStandardMaterial({
                color: color,
                emissive: color,
                emissiveIntensity: 0.2,
                roughness: 0.3,
                metalness: 0.5
            });

            const mesh = this.addInteractiveObject({
                geometry: geo,
                material: mat,
                position: { x, y: 0, z },
                id: `game_${game.name.replace(/\s+/g, '_')}`,
                name: game.name,
                type: 'game',
                data: game
            });

            // Ring around planet
            const ringGeo = new THREE.TorusGeometry(2, 0.05, 8, 32);
            const ringMat = new THREE.MeshStandardMaterial({
                color: 0xffffff,
                emissive: 0x444444,
                emissiveIntensity: 0.3
            });
            const ring = new THREE.Mesh(ringGeo, ringMat);
            ring.rotation.x = Math.PI / 3;
            mesh.add(ring);

            // Label
            this.addLabel(mesh, game.name);

            // Session count indicator
            if (game.activeSessions > 0) {
                const indicatorGeo = new THREE.SphereGeometry(0.2, 8, 8);
                const indicatorMat = new THREE.MeshStandardMaterial({
                    color: 0x00ff00,
                    emissive: 0x00ff00,
                    emissiveIntensity: 0.8
                });
                const indicator = new THREE.Mesh(indicatorGeo, indicatorMat);
                indicator.position.set(1.5, 1.5, 0);
                mesh.add(indicator);
            }
        });

        // Slow rotation animation
        this.carouselRotation = 0;
        this.isCarousel = true;

        console.log(`[SceneRenderer] Game carousel created with ${games.length} games`);
    }

    /**
     * Update animation loop for carousel rotation.
     */
    _setupAnimationLoop() {
        const animate = () => {
            this.animationId = requestAnimationFrame(animate);

            // Rotate starfield
            if (this.particles) {
                this.particles.rotation.y += 0.0002;
            }

            // Rotate carousel
            if (this.isCarousel && this.interactiveObjects.length > 0) {
                this.carouselRotation += 0.002;
                const radius = 8;
                const angleStep = (2 * Math.PI) / this.interactiveObjects.length;
                this.interactiveObjects.forEach((obj, i) => {
                    if (obj.userData.type === 'game') {
                        const angle = i * angleStep + this.carouselRotation;
                        obj.position.x = Math.sin(angle) * radius;
                        obj.position.z = -Math.cos(angle) * radius;
                    }
                });
            }

            // Flicker torch lights (D&D)
            if (this.config.theme === 'dnd-5e') {
                this.lights.forEach(light => {
                    if (light.name === 'torch') {
                        light.intensity = 0.8 + Math.sin(Date.now() * 0.01) * 0.2 + Math.random() * 0.1;
                    }
                });
            }

            this.renderer.render(this.scene, this.camera);
        };
        animate();
    }

    /**
     * Create 3D portal effect for game entry.
     */
    createPortal(position, onEnter) {
        if (!this.isInitialized) return null;

        const group = new THREE.Group();
        group.position.set(
            position?.x || 0,
            position?.y || 0,
            position?.z || 0
        );

        // Portal ring
        const ringGeo = new THREE.TorusGeometry(2, 0.3, 16, 32);
        const ringMat = new THREE.MeshStandardMaterial({
            color: 0x8844ff,
            emissive: 0x8844ff,
            emissiveIntensity: 0.8,
            transparent: true,
            opacity: 0.8
        });
        const ring = new THREE.Mesh(ringGeo, ringMat);
        group.add(ring);

        // Portal center
        const centerGeo = new THREE.CircleGeometry(1.8, 32);
        const centerMat = new THREE.MeshStandardMaterial({
            color: 0x4400ff,
            emissive: 0x4400ff,
            emissiveIntensity: 1,
            transparent: true,
            opacity: 0.5,
            side: THREE.DoubleSide
        });
        const center = new THREE.Mesh(centerGeo, centerMat);
        group.add(center);

        // Particle ring
        const particleCount = 50;
        const particleGeo = new THREE.BufferGeometry();
        const positions = new Float32Array(particleCount * 3);
        for (let i = 0; i < particleCount; i++) {
            const angle = (i / particleCount) * Math.PI * 2;
            positions[i * 3] = Math.cos(angle) * 2.5;
            positions[i * 3 + 1] = Math.sin(angle) * 2.5;
            positions[i * 3 + 2] = 0;
        }
        particleGeo.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        const particleMat = new THREE.PointsMaterial({
            color: 0xaa88ff,
            size: 0.1,
            transparent: true,
            opacity: 0.8
        });
        const particles = new THREE.Points(particleGeo, particleMat);
        group.add(particles);

        group.userData = {
            interactive: true,
            id: 'portal',
            name: 'Portal',
            type: 'portal',
            data: { onEnter }
        };

        this.scene.add(group);
        this.interactiveObjects.push(group);

        // Animate portal
        const animatePortal = () => {
            if (!this.scene.children.includes(group)) return;
            ring.rotation.z += 0.02;
            particles.rotation.z -= 0.01;
            center.material.opacity = 0.3 + Math.sin(Date.now() * 0.003) * 0.2;
            requestAnimationFrame(animatePortal);
        };
        animatePortal();

        return group;
    }
}

// Expose globally
window.SceneRenderer = SceneRenderer;

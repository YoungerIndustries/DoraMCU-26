// Galaxy 3D View
// Three.js-based 3D galaxy viewer showing sectors, stars, planets and fleets
// Renders a 10x10 sector grid as a 3D galaxy with orbit controls

class Galaxy3DView {
    constructor(app) {
        this.app = app;
        this.element = null;
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.controls = null;
        this.animationId = null;
        this.objects = {
            sectors: [],
            stars: [],
            planets: [],
            fleets: []
        };
        this.webglSupported = true;

        // Check WebGL support
        try {
            const canvas = document.createElement('canvas');
            const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');
            if (!gl) {
                this.webglSupported = false;
            }
        } catch (e) {
            this.webglSupported = false;
        }

        // Listen for player data updates
        this.app.state.on('player:updated', () => {
            this._updateFromState();
        });

        this.app.state.on('sectors:updated', () => {
            this._updateFromState();
        });

        this.app.state.on('fleet:selected', () => {
            this._updateFromState();
        });
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'panel';
        this.element.id = 'galaxy-3d-panel';
        this.element.style.position = 'relative';
        this.element.style.width = '100%';
        this.element.style.height = '100%';
        this.element.style.minHeight = '500px';
        this.element.style.background = '#000';

        if (!this.webglSupported) {
            this.element.innerHTML = `
                <div class="panel-header">
                    <span>🌌 3D Galaxy View</span>
                </div>
                <div style="display:flex; align-items:center; justify-content:center; height:400px; color:#8892b0;">
                    <div style="text-align:center;">
                        <p>WebGL not supported in your browser.</p>
                        <p>Please use the 2D Map view instead.</p>
                    </div>
                </div>
            `;
            return this.element;
        }

        this.element.innerHTML = `
            <div class="panel-header">
                <span>🌌 3D Galaxy View</span>
                <span style="font-size: 0.8rem; color: var(--text-muted);" id="galaxy-3d-hint">Drag to rotate · Scroll to zoom · Right-drag to pan</span>
            </div>
            <div id="galaxy-3d-canvas-container" style="width:100%; height:calc(100% - 40px); position:relative;"></div>
            <div id="galaxy-3d-loading" style="position:absolute; top:50%; left:50%; transform:translate(-50%,-50%); color:#64ffda; font-size:1.2rem; z-index:10;">
                Loading galaxy...
            </div>
        `;

        // Initialize Three.js scene
        this._initThreeJS();

        return this.element;
    }

    show() {
        // Refresh data when view becomes active
        this._updateFromState();
        // Resize renderer to fit container
        this._resize();
        // Restart animation loop if it was stopped
        if (!this.animationId && this.renderer && this.scene && this.camera) {
            this._animate();
        }
    }

    hide() {
        // Stop animation loop when view is hidden to save resources
        if (this.animationId) {
            cancelAnimationFrame(this.animationId);
            this.animationId = null;
        }
    }

    _initThreeJS() {
        const container = document.getElementById('galaxy-3d-canvas-container');
        if (!container) return;

        // Create scene
        this.scene = new THREE.Scene();
        this.scene.background = new THREE.Color(0x050510);

        // Create camera
        const width = container.clientWidth;
        const height = container.clientHeight;
        this.camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000);
        this.camera.position.set(0, 40, 60);
        this.camera.lookAt(0, 0, 0);

        // Create renderer
        this.renderer = new THREE.WebGLRenderer({ antialias: true });
        this.renderer.setSize(width, height);
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        container.appendChild(this.renderer.domElement);

        // Add OrbitControls
        this.controls = new THREE.OrbitControls(this.camera, this.renderer.domElement);
        this.controls.enableDamping = true;
        this.controls.dampingFactor = 0.05;
        this.controls.minDistance = 10;
        this.controls.maxDistance = 200;
        this.controls.maxPolarAngle = Math.PI * 0.85;

        // Add lights
        const ambientLight = new THREE.AmbientLight(0x404060, 0.6);
        this.scene.add(ambientLight);

        const pointLight = new THREE.PointLight(0xffffff, 1.0);
        pointLight.position.set(0, 30, 0);
        this.scene.add(pointLight);

        // Create background starfield
        this._createStarfield();

        // Create sector grid
        this._createSectorGrid();

        // Hide loading indicator
        const loadingEl = document.getElementById('galaxy-3d-loading');
        if (loadingEl) loadingEl.style.display = 'none';

        // Start animation loop
        this._animate();

        // Handle resize
        const resizeObserver = new ResizeObserver(() => {
            this._resize();
        });
        resizeObserver.observe(container);
    }

    _createStarfield() {
        const starsGeometry = new THREE.BufferGeometry();
        const starCount = 2000;
        const positions = new Float32Array(starCount * 3);
        const colors = new Float32Array(starCount * 3);

        for (let i = 0; i < starCount; i++) {
            const i3 = i * 3;
            // Random positions in a large sphere
            const radius = 100 + Math.random() * 200;
            const theta = Math.random() * Math.PI * 2;
            const phi = Math.acos(2 * Math.random() - 1);
            positions[i3] = radius * Math.sin(phi) * Math.cos(theta);
            positions[i3 + 1] = radius * Math.sin(phi) * Math.sin(theta);
            positions[i3 + 2] = radius * Math.cos(phi);

            // Slight color variation
            const colorVal = 0.5 + Math.random() * 0.5;
            colors[i3] = colorVal;
            colors[i3 + 1] = colorVal;
            colors[i3 + 2] = colorVal + Math.random() * 0.2;
        }

        starsGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        starsGeometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));

        const starsMaterial = new THREE.PointsMaterial({
            size: 0.3,
            vertexColors: true,
            transparent: true,
            opacity: 0.8
        });

        const starField = new THREE.Points(starsGeometry, starsMaterial);
        this.scene.add(starField);
    }

    _createSectorGrid() {
        // Clear existing sector objects
        this.objects.sectors.forEach(obj => this.scene.remove(obj));
        this.objects.sectors = [];

        const gridSize = 10;
        const spacing = 8;
        const offset = (gridSize * spacing) / 2 - spacing / 2;

        for (let row = 0; row < gridSize; row++) {
            for (let col = 0; col < gridSize; col++) {
                const x = col * spacing - offset;
                const z = row * spacing - offset;

                // Sector marker - small glowing ring
                const ringGeometry = new THREE.RingGeometry(1.5, 2.0, 16);
                const ringMaterial = new THREE.MeshBasicMaterial({
                    color: 0x1a3a5a,
                    transparent: true,
                    opacity: 0.3,
                    side: THREE.DoubleSide
                });
                const ring = new THREE.Mesh(ringGeometry, ringMaterial);
                ring.rotation.x = -Math.PI / 2;
                ring.position.set(x, 0, z);
                this.scene.add(ring);
                this.objects.sectors.push(ring);

                // Grid line connections
                if (col < gridSize - 1) {
                    const lineGeo = new THREE.BufferGeometry().setFromPoints([
                        new THREE.Vector3(x, 0, z),
                        new THREE.Vector3(x + spacing, 0, z)
                    ]);
                    const lineMat = new THREE.LineBasicMaterial({
                        color: 0x0a1a3a,
                        transparent: true,
                        opacity: 0.2
                    });
                    const line = new THREE.Line(lineGeo, lineMat);
                    this.scene.add(line);
                    this.objects.sectors.push(line);
                }
                if (row < gridSize - 1) {
                    const lineGeo = new THREE.BufferGeometry().setFromPoints([
                        new THREE.Vector3(x, 0, z),
                        new THREE.Vector3(x, 0, z + spacing)
                    ]);
                    const lineMat = new THREE.LineBasicMaterial({
                        color: 0x0a1a3a,
                        transparent: true,
                        opacity: 0.2
                    });
                    const line = new THREE.Line(lineGeo, lineMat);
                    this.scene.add(line);
                    this.objects.sectors.push(line);
                }
            }
        }
    }

    _updateFromState() {
        if (!this.scene) return;

        // Clear dynamic objects
        this.objects.planets.forEach(obj => this.scene.remove(obj));
        this.objects.fleets.forEach(obj => this.scene.remove(obj));
        this.objects.planets = [];
        this.objects.fleets = [];

        const gridSize = 10;
        const spacing = 8;
        const offset = (gridSize * spacing) / 2 - spacing / 2;

        // Update sector colors based on explored status
        const sectors = this.app.state.sectors || [];
        const sectorMap = {};
        sectors.forEach(sector => {
            const key = `${sector.row}-${sector.col}`;
            sectorMap[key] = sector;
        });

        // Update sector ring colors
        this.objects.sectors.forEach((ring, idx) => {
            if (ring.geometry && ring.geometry.type === 'RingGeometry') {
                const row = Math.floor(idx / gridSize);
                const col = idx % gridSize;
                const key = `${row}-${col}`;
                const sector = sectorMap[key];

                if (sector) {
                    if (sector.explored) {
                        ring.material.color.setHex(0x2a5a8a);
                        ring.material.opacity = 0.6;
                    }
                    if (sector.hasSystems) {
                        ring.material.color.setHex(0x64ffda);
                        ring.material.opacity = 0.8;
                    }
                }
            }
        });

        // Add planets from player data
        const player = this.app.state.player;
        if (player) {
            // Add planets
            if (player.planets) {
                player.planets.forEach(planet => {
                    if (planet.sectorRow !== undefined && planet.sectorCol !== undefined) {
                        const x = planet.sectorCol * spacing - offset;
                        const z = planet.sectorRow * spacing - offset;

                        // Planet sphere
                        const planetGeo = new THREE.SphereGeometry(1.5, 16, 16);
                        const planetMat = new THREE.MeshPhongMaterial({
                            color: 0x4a9eff,
                            emissive: 0x1a3a6a,
                            shininess: 30
                        });
                        const planetMesh = new THREE.Mesh(planetGeo, planetMat);
                        planetMesh.position.set(x, 2, z);
                        this.scene.add(planetMesh);
                        this.objects.planets.push(planetMesh);

                        // Planet glow ring
                        const glowGeo = new THREE.RingGeometry(2.0, 3.0, 24);
                        const glowMat = new THREE.MeshBasicMaterial({
                            color: 0x4a9eff,
                            transparent: true,
                            opacity: 0.3,
                            side: THREE.DoubleSide
                        });
                        const glow = new THREE.Mesh(glowGeo, glowMat);
                        glow.rotation.x = -Math.PI / 2;
                        glow.position.set(x, 1.5, z);
                        this.scene.add(glow);
                        this.objects.planets.push(glow);
                    }
                });
            }

            // Add fleets
            if (player.fleets) {
                const selectedFleetId = this.app.state.selectedFleetId;
                player.fleets.forEach(fleet => {
                    if (fleet.sectorRow !== undefined && fleet.sectorCol !== undefined) {
                        const x = fleet.sectorCol * spacing - offset;
                        const z = fleet.sectorRow * spacing - offset;

                        // Fleet cone
                        const fleetGeo = new THREE.ConeGeometry(0.8, 2.5, 8);
                        const isSelected = fleet.fleetId === selectedFleetId;
                        const fleetMat = new THREE.MeshPhongMaterial({
                            color: isSelected ? 0xff6b35 : 0xffaa00,
                            emissive: isSelected ? 0x662200 : 0x332200,
                            shininess: 50
                        });
                        const fleetMesh = new THREE.Mesh(fleetGeo, fleetMat);
                        fleetMesh.position.set(x, 4, z);
                        fleetMesh.rotation.x = Math.PI; // Point downward
                        this.scene.add(fleetMesh);
                        this.objects.fleets.push(fleetMesh);
                    }
                });
            }
        }
    }

    _resize() {
        if (!this.renderer || !this.camera) return;
        const container = document.getElementById('galaxy-3d-canvas-container');
        if (!container) return;

        const width = container.clientWidth;
        const height = container.clientHeight;

        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
        this.renderer.setSize(width, height);
    }

    _animate() {
        this.animationId = requestAnimationFrame(() => this._animate());

        if (this.controls) {
            this.controls.update();
        }

        // Slowly rotate planets and fleets for visual effect
        const time = Date.now() * 0.001;
        this.objects.planets.forEach((obj, idx) => {
            if (obj.geometry && obj.geometry.type === 'SphereGeometry') {
                obj.rotation.y = time * 0.5;
            }
        });
        this.objects.fleets.forEach((obj, idx) => {
            obj.rotation.y = time * 0.3;
        });

        if (this.renderer && this.scene && this.camera) {
            this.renderer.render(this.scene, this.camera);
        }
    }
}

window.Galaxy3DView = Galaxy3DView;

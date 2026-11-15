/**
 * Adaptive3DView — Unified 3D game view controller.
 *
 * Single Three.js scene that adapts to game type:
 * - FPS mode: First-person camera with pointer lock, weapon model, crosshair
 * - Galaxy mode: Orbit camera for 4X strategy with galaxy map
 * - Tactical mode: Isometric/top-down camera for ground combat with grid
 * - Narrative mode: Cinematic camera for RPG scenes with character models
 *
 * Features:
 * - Smooth camera transitions between modes
 * - Shared rendering features: lighting, fog, post-processing effects
 * - Unified minimap system with game-type-specific rendering
 * - Integration with existing 3D modules (GalaxyMapRenderer, FleetTracker, BattleViz)
 *
 * Usage:
 *   const view = new Adaptive3DView(containerElement);
 *   view.init();
 *   view.setCameraMode('fps');  // 'fps', 'galaxy', 'tactical', 'narrative'
 *   view.addFleetMarker(id, x, y, z, type);
 *   view.destroy();
 */

class Adaptive3DView {
    /**
     * @param {HTMLElement} container - DOM container for the 3D canvas
     */
    constructor(container) {
        this.container = container;
        this.isInitialized = false;

        // Three.js core
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.animationId = null;

        // Camera modes
        this.currentMode = null; // 'fps', 'galaxy', 'tactical', 'narrative'
        this.targetMode = null;
        this.transitionProgress = 1; // 1 = complete
        this.transitionDuration = 1000; // ms
        this.transitionStart = 0;

        // Camera state for each mode
        this.cameraStates = {
            fps: {
                position: { x: 0, y: 1.7, z: 0 },
                rotation: { x: 0, y: 0, z: 0 },
                fov: 75,
                near: 0.1,
                far: 1000
            },
            galaxy: {
                position: { x: 0, y: 50, z: 100 },
                target: { x: 0, y: 0, z: 0 },
                fov: 60,
                near: 0.1,
                far: 2000
            },
            tactical: {
                position: { x: 0, y: 40, z: 40 },
                target: { x: 0, y: 0, z: 0 },
                fov: 45,
                near: 0.1,
                far: 500
            },
            narrative: {
                position: { x: 0, y: 5, z: 10 },
                target: { x: 0, y: 2, z: 0 },
                fov: 50,
                near: 0.1,
                far: 1000
            }
        };

        // Orbit controls state (for galaxy and tactical modes)
        this.orbitState = {
            spherical: { radius: 100, theta: Math.PI / 4, phi: Math.PI / 3 },
            target: { x: 0, y: 0, z: 0 },
            isDragging: false,
            lastMouse: { x: 0, y: 0 }
        };

        // FPS camera state
        this.fpsState = {
            velocity: { x: 0, y: 0, z: 0 },
            direction: { x: 0, y: 0, z: 0 },
            moveForward: false,
            moveBackward: false,
            moveLeft: false,
            moveRight: false,
            moveUp: false,
            moveDown: false,
            isLocked: false,
            speed: 10,
            jumpSpeed: 10
        };

        // Scene objects
        this.objects = new Map(); // id -> THREE.Object3D
        this.fleetMarkers = new Map(); // id -> THREE.Group
        this.particles = [];
        this.lights = [];
        this.gridHelper = null;
        this.crosshair = null;
        this.minimapCanvas = null;
        this.minimapContext = null;
        this.minimapVisible = false;

        // Post-processing
        this.bloomPass = null;
        this.effectComposer = null;

        // Shared environment
        this.starField = null;
        this.terrainMesh = null;
        this.skyDome = null;

        // Callbacks
        this.callbacks = {
            onModeChange: null,
            onObjectClick: null,
            onObjectHover: null,
            onTransitionComplete: null
        };

        // Raycaster for interaction
        this.raycaster = new (typeof THREE !== 'undefined' ? THREE.Raycaster : null)();
        this.mouse = new (typeof THREE !== 'undefined' ? THREE.Vector2 : null);
    }

    /**
     * Set callbacks for view events.
     * @param {object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        this.callbacks = { ...this.callbacks, ...callbacks };
    }

    /**
     * Initialize the Three.js scene, camera, renderer, and shared environment.
     */
    init() {
        if (this.isInitialized) return;

        if (typeof THREE === 'undefined') {
            console.warn('[Adaptive3DView] Three.js not loaded. Skipping 3D initialization.');
            return;
        }

        // Scene
        this.scene = new THREE.Scene();
        this._setupBackground();
        this._setupFog();

        // Camera (default to galaxy mode)
        this._setupCamera();

        // Renderer
        this._setupRenderer();

        // Shared lighting
        this._setupLights();

        // Shared environment
        this._createStarField();

        // Interaction
        this._setupInteraction();

        // Orbit controls
        this._setupOrbitControls();

        // Animation loop
        this._setupAnimationLoop();

        this.isInitialized = true;
        console.log('[Adaptive3DView] Initialized');

        // Handle resize
        this._resizeHandler = () => this._onResize();
        window.addEventListener('resize', this._resizeHandler);
    }

    /**
     * Set up background color.
     */
    _setupBackground() {
        this.scene.background = new THREE.Color(0x000000);
    }

    /**
     * Set up fog for depth.
     */
    _setupFog() {
        this.scene.fog = new THREE.FogExp2(0x000000, 0.002);
    }

    /**
     * Set up camera with default galaxy mode.
     */
    _setupCamera() {
        const width = this.container.clientWidth || 800;
        const height = this.container.clientHeight || 600;
        this.camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 2000);
        this.camera.position.set(0, 50, 100);
    }

    /**
     * Set up WebGL renderer.
     */
    _setupRenderer() {
        this.renderer = new THREE.WebGLRenderer({
            antialias: true,
            alpha: true,
            powerPreference: 'high-performance'
        });
        const width = this.container.clientWidth || 800;
        const height = this.container.clientHeight || 600;
        this.renderer.setSize(width, height);
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        this.renderer.shadowMap.enabled = true;
        this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
        this.renderer.toneMapping = THREE.ACESFilmicToneMapping;
        this.renderer.toneMappingExposure = 1.2;
        this.container.appendChild(this.renderer.domElement);
    }

    /**
     * Set up shared lighting.
     */
    _setupLights() {
        // Ambient light
        const ambient = new THREE.AmbientLight(0x404060, 0.5);
        this.scene.add(ambient);
        this.lights.push(ambient);

        // Directional light (sun)
        const directional = new THREE.DirectionalLight(0xffffff, 1);
        directional.position.set(50, 100, 50);
        directional.castShadow = true;
        directional.shadow.mapSize.width = 2048;
        directional.shadow.mapSize.height = 2048;
        this.scene.add(directional);
        this.lights.push(directional);

        // Hemisphere light for sky/ground color
        const hemi = new THREE.HemisphereLight(0x606080, 0x404040, 0.3);
        this.scene.add(hemi);
        this.lights.push(hemi);
    }

    /**
     * Create shared star field background.
     */
    _createStarField() {
        const starCount = 5000;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(starCount * 3);
        const colors = new Float32Array(starCount * 3);
        const sizes = new Float32Array(starCount);

        for (let i = 0; i < starCount; i++) {
            const i3 = i * 3;
            const radius = 500 + Math.random() * 500;
            const theta = Math.random() * Math.PI * 2;
            const phi = Math.acos(2 * Math.random() - 1);

            positions[i3] = radius * Math.sin(phi) * Math.cos(theta);
            positions[i3 + 1] = radius * Math.sin(phi) * Math.sin(theta);
            positions[i3 + 2] = radius * Math.cos(phi);

            // Star color variation
            const colorChoice = Math.random();
            if (colorChoice > 0.9) {
                colors[i3] = 0.8; colors[i3 + 1] = 0.85; colors[i3 + 2] = 1.0; // Blue
            } else if (colorChoice > 0.8) {
                colors[i3] = 1.0; colors[i3 + 1] = 0.9; colors[i3 + 2] = 0.7; // Yellow
            } else {
                colors[i3] = 1.0; colors[i3 + 1] = 1.0; colors[i3 + 2] = 1.0; // White
            }

            sizes[i] = 0.5 + Math.random() * 1.5;
        }

        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
        geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1));

        const material = new THREE.PointsMaterial({
            size: 1,
            vertexColors: true,
            transparent: true,
            opacity: 0.8,
            sizeAttenuation: true
        });

        this.starField = new THREE.Points(geometry, material);
        this.scene.add(this.starField);
    }

    /**
     * Set up mouse interaction for object picking.
     */
    _setupInteraction() {
        if (!this.raycaster || !this.mouse) return;

        this.renderer.domElement.addEventListener('click', (event) => {
            this._onObjectClick(event);
        });

        this.renderer.domElement.addEventListener('mousemove', (event) => {
            this._onObjectHover(event);
        });
    }

    /**
     * Set up orbit controls for galaxy and tactical modes.
     */
    _setupOrbitControls() {
        const canvas = this.renderer.domElement;

        canvas.addEventListener('mousedown', (event) => {
            if (this.currentMode === 'fps' && this.fpsState.isLocked) return;
            this.orbitState.isDragging = true;
            this.orbitState.lastMouse = { x: event.clientX, y: event.clientY };
        });

        canvas.addEventListener('mousemove', (event) => {
            if (!this.orbitState.isDragging) return;
            if (this.currentMode === 'fps' && this.fpsState.isLocked) return;

            const deltaX = event.clientX - this.orbitState.lastMouse.x;
            const deltaY = event.clientY - this.orbitState.lastMouse.y;

            this.orbitState.spherical.theta -= deltaX * 0.01;
            this.orbitState.spherical.phi -= deltaY * 0.01;
            this.orbitState.spherical.phi = Math.max(0.1, Math.min(Math.PI - 0.1, this.orbitState.spherical.phi));

            this.orbitState.lastMouse = { x: event.clientX, y: event.clientY };
            this._updateCameraFromOrbit();
        });

        canvas.addEventListener('mouseup', () => {
            this.orbitState.isDragging = false;
        });

        canvas.addEventListener('wheel', (event) => {
            if (this.currentMode === 'fps' && this.fpsState.isLocked) return;
            event.preventDefault();
            this.orbitState.spherical.radius *= event.deltaY > 0 ? 1.1 : 0.9;
            this.orbitState.spherical.radius = Math.max(5, Math.min(500, this.orbitState.spherical.radius));
            this._updateCameraFromOrbit();
        }, { passive: false });
    }

    /**
     * Update camera position from orbit spherical coordinates.
     */
    _updateCameraFromOrbit() {
        const { spherical, target } = this.orbitState;
        this.camera.position.x = target.x + spherical.radius * Math.sin(spherical.phi) * Math.cos(spherical.theta);
        this.camera.position.y = target.y + spherical.radius * Math.cos(spherical.phi);
        this.camera.position.z = target.z + spherical.radius * Math.sin(spherical.phi) * Math.sin(spherical.theta);
        this.camera.lookAt(target.x, target.y, target.z);
    }

    /**
     * Handle object click events.
     * @param {MouseEvent} event - Mouse click event
     */
    _onObjectClick(event) {
        if (!this.raycaster || !this.mouse) return;

        const rect = this.renderer.domElement.getBoundingClientRect();
        this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

        this.raycaster.setFromCamera(this.mouse, this.camera);
        const intersects = this.raycaster.intersectObjects(this.scene.children, true);

        if (intersects.length > 0) {
            let object = intersects[0].object;
            while (object && !object.userData.interactive) {
                object = object.parent;
            }
            if (object && object.userData.interactive && this.callbacks.onObjectClick) {
                this.callbacks.onObjectClick(object.userData, intersects[0].point);
            }
        }
    }

    /**
     * Handle object hover events.
     * @param {MouseEvent} event - Mouse move event
     */
    _onObjectHover(event) {
        if (!this.raycaster || !this.mouse || !this.callbacks.onObjectHover) return;

        const rect = this.renderer.domElement.getBoundingClientRect();
        this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;

        this.raycaster.setFromCamera(this.mouse, this.camera);
        const intersects = this.raycaster.intersectObjects(this.scene.children, true);

        if (intersects.length > 0) {
            let object = intersects[0].object;
            while (object && !object.userData.interactive) {
                object = object.parent;
            }
            if (object && object.userData.interactive) {
                this.callbacks.onObjectHover(object.userData);
                this.renderer.domElement.style.cursor = 'pointer';
                return;
            }
        }
        this.renderer.domElement.style.cursor = 'default';
    }

    /**
     * Set up the animation loop.
     */
    _setupAnimationLoop() {
        const animate = () => {
            this.animationId = requestAnimationFrame(animate);

            // Update transitions
            if (this.transitionProgress < 1) {
                const elapsed = Date.now() - this.transitionStart;
                this.transitionProgress = Math.min(1, elapsed / this.transitionDuration);
                this._updateCameraTransition();
                if (this.transitionProgress >= 1 && this.callbacks.onTransitionComplete) {
                    this.callbacks.onTransitionComplete(this.currentMode);
                }
            }

            // Mode-specific updates
            if (this.currentMode === 'fps') {
                this._updateFPSCamera();
            } else if (this.currentMode === 'galaxy' || this.currentMode === 'tactical') {
                if (!this.orbitState.isDragging) {
                    // Auto-rotate slowly
                    this.orbitState.spherical.theta += 0.0005;
                    this._updateCameraFromOrbit();
                }
            } else if (this.currentMode === 'narrative') {
                this._updateNarrativeCamera();
            }

            // Update particles
            this._updateParticles();

            // Render
            this.renderer.render(this.scene, this.camera);
        };
        animate();
    }

    /**
     * Update FPS camera movement.
     */
    _updateFPSCamera() {
        if (!this.fpsState.isLocked) return;

        const delta = 0.016; // ~60fps
        const speed = this.fpsState.speed * delta;

        // Damping
        this.fpsState.velocity.x -= this.fpsState.velocity.x * 10.0 * delta;
        this.fpsState.velocity.z -= this.fpsState.velocity.z * 10.0 * delta;
        this.fpsState.velocity.y -= 9.8 * 100.0 * delta; // Gravity

        this.fpsState.direction.x = Number(this.fpsState.moveLeft) - Number(this.fpsState.moveRight);
        this.fpsState.direction.z = Number(this.fpsState.moveBackward) - Number(this.fpsState.moveForward);
        this.fpsState.direction.normalize();

        if (this.fpsState.moveForward || this.fpsState.moveBackward) {
            this.fpsState.velocity.z -= this.fpsState.direction.z * speed * 100;
        }
        if (this.fpsState.moveLeft || this.fpsState.moveRight) {
            this.fpsState.velocity.x -= this.fpsState.direction.x * speed * 100;
        }

        // Apply movement
        const forward = new THREE.Vector3(0, 0, -1).applyQuaternion(this.camera.quaternion);
        const right = new THREE.Vector3(1, 0, 0).applyQuaternion(this.camera.quaternion);

        this.camera.position.addScaledVector(forward, -this.fpsState.velocity.z * delta);
        this.camera.position.addScaledVector(right, -this.fpsState.velocity.x * delta);

        // Keep camera at walking height
        this.camera.position.y = this.cameraStates.fps.position.y;
    }

    /**
     * Update narrative cinematic camera.
     */
    _updateNarrativeCamera() {
        const time = Date.now() * 0.0005;
        const state = this.cameraStates.narrative;

        // Gentle orbit around target
        const radius = 10;
        this.camera.position.x = state.target.x + Math.cos(time) * radius;
        this.camera.position.z = state.target.z + Math.sin(time) * radius;
        this.camera.position.y = state.target.y + 3 + Math.sin(time * 0.5) * 1;
        this.camera.lookAt(state.target.x, state.target.y, state.target.z);
    }

    /**
     * Update particle effects.
     */
    _updateParticles() {
        for (let i = this.particles.length - 1; i >= 0; i--) {
            const particle = this.particles[i];
            particle.life -= particle.deltaLife;
            if (particle.life <= 0) {
                this.scene.remove(particle.mesh);
                this.particles.splice(i, 1);
                continue;
            }
            particle.mesh.position.add(particle.velocity);
            particle.mesh.material.opacity = particle.life;
        }
    }

    /**
     * Set camera mode with smooth transition.
     * @param {string} mode - Camera mode: 'fps', 'galaxy', 'tactical', 'narrative'
     */
    setCameraMode(mode) {
        if (!this.isInitialized) {
            console.warn('[Adaptive3DView] Not initialized. Call init() first.');
            return;
        }

        if (mode === this.currentMode) return;

        const previousMode = this.currentMode;
        this.targetMode = mode;
        this.transitionProgress = 0;
        this.transitionStart = Date.now();

        // Mode-specific setup
        this._cleanupMode(previousMode);
        this._setupMode(mode);

        this.currentMode = mode;

        // Update camera FOV
        const state = this.cameraStates[mode];
        if (state) {
            this._animateFOV(this.camera.fov, state.fov);
        }

        // Update fog density per mode
        this._updateFogForMode(mode);

        console.log(`[Adaptive3DView] Camera mode changed: ${previousMode} -> ${mode}`);
        if (this.callbacks.onModeChange) {
            this.callbacks.onModeChange(mode, previousMode);
        }
    }

    /**
     * Clean up mode-specific elements.
     * @param {string} mode - Mode to clean up
     */
    _cleanupMode(mode) {
        switch (mode) {
            case 'fps':
                this._disablePointerLock();
                this._removeCrosshair();
                this._removeGrid();
                break;
            case 'galaxy':
                this._removeGrid();
                break;
            case 'tactical':
                this._removeGrid();
                if (this.terrainMesh) {
                    this.scene.remove(this.terrainMesh);
                    this.terrainMesh = null;
                }
                break;
            case 'narrative':
                if (this.skyDome) {
                    this.scene.remove(this.skyDome);
                    this.skyDome = null;
                }
                break;
        }
    }

    /**
     * Set up mode-specific elements.
     * @param {string} mode - Mode to set up
     */
    _setupMode(mode) {
        const state = this.cameraStates[mode];
        if (!state) return;

        switch (mode) {
            case 'fps':
                this._setupFPSMode();
                break;
            case 'galaxy':
                this._setupGalaxyMode();
                break;
            case 'tactical':
                this._setupTacticalMode();
                break;
            case 'narrative':
                this._setupNarrativeMode();
                break;
        }
    }

    /**
     * Set up FPS mode: pointer lock, crosshair, ground plane.
     */
    _setupFPSMode() {
        // Enable pointer lock
        this._enablePointerLock();

        // Create crosshair
        this._createCrosshair();

        // Create ground plane
        const groundGeo = new THREE.PlaneGeometry(200, 200);
        const groundMat = new THREE.MeshStandardMaterial({
            color: 0x333333,
            roughness: 0.8,
            metalness: 0.2
        });
        const ground = new THREE.Mesh(groundGeo, groundMat);
        ground.rotation.x = -Math.PI / 2;
        ground.receiveShadow = true;
        ground.userData = { id: 'fps-ground', type: 'ground' };
        this.scene.add(ground);
        this.objects.set('fps-ground', ground);

        // Create weapon model placeholder
        this._createWeaponModel();

        // Set camera position
        this.camera.position.set(state.position.x, state.position.y, state.position.z);
    }

    /**
     * Set up Galaxy mode: orbit camera, galaxy map.
     */
    _setupGalaxyMode() {
        const state = this.cameraStates.galaxy;
        this.orbitState.target = { ...state.target };
        this.orbitState.spherical.radius = 100;
        this.orbitState.spherical.theta = Math.PI / 4;
        this.orbitState.spherical.phi = Math.PI / 3;
        this._updateCameraFromOrbit();
    }

    /**
     * Set up Tactical mode: isometric camera, grid overlay, terrain.
     */
    _setupTacticalMode() {
        const state = this.cameraStates.tactical;
        this.orbitState.target = { ...state.target };
        this.orbitState.spherical.radius = 50;
        this.orbitState.spherical.theta = Math.PI / 4;
        this.orbitState.spherical.phi = Math.PI / 4; // Isometric angle
        this._updateCameraFromOrbit();

        // Create grid overlay
        this._createGrid(50, 50, 0x444444, 0x222222);

        // Create terrain
        this._createTerrain();
    }

    /**
     * Set up Narrative mode: cinematic camera, sky dome.
     */
    _setupNarrativeMode() {
        const state = this.cameraStates.narrative;
        this.camera.position.set(state.position.x, state.position.y, state.position.z);
        this.camera.lookAt(state.target.x, state.target.y, state.target.z);

        // Create sky dome
        this._createSkyDome();
    }

    /**
     * Enable pointer lock for FPS mode.
     */
    _enablePointerLock() {
        const canvas = this.renderer.domElement;

        canvas.addEventListener('click', () => {
            if (!this.fpsState.isLocked && this.currentMode === 'fps') {
                canvas.requestPointerLock();
            }
        });

        document.addEventListener('pointerlockchange', () => {
            this.fpsState.isLocked = document.pointerLockElement === canvas;
        });

        document.addEventListener('mousemove', (event) => {
            if (!this.fpsState.isLocked || this.currentMode !== 'fps') return;
            const sensitivity = 0.002;
            this.camera.rotation.y -= event.movementX * sensitivity;
            this.camera.rotation.x -= event.movementY * sensitivity;
            this.camera.rotation.x = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, this.camera.rotation.x));
        });

        document.addEventListener('keydown', (event) => {
            if (this.currentMode !== 'fps') return;
            switch (event.code) {
                case 'KeyW': this.fpsState.moveForward = true; break;
                case 'KeyS': this.fpsState.moveBackward = true; break;
                case 'KeyA': this.fpsState.moveLeft = true; break;
                case 'KeyD': this.fpsState.moveRight = true; break;
                case 'Space': this.fpsState.moveUp = true; break;
                case 'ShiftLeft': this.fpsState.moveDown = true; break;
            }
        });

        document.addEventListener('keyup', (event) => {
            if (this.currentMode !== 'fps') return;
            switch (event.code) {
                case 'KeyW': this.fpsState.moveForward = false; break;
                case 'KeyS': this.fpsState.moveBackward = false; break;
                case 'KeyA': this.fpsState.moveLeft = false; break;
                case 'KeyD': this.fpsState.moveRight = false; break;
                case 'Space': this.fpsState.moveUp = false; break;
                case 'ShiftLeft': this.fpsState.moveDown = false; break;
            }
        });
    }

    /**
     * Disable pointer lock.
     */
    _disablePointerLock() {
        if (document.pointerLockElement) {
            document.exitPointerLock();
        }
        this.fpsState.isLocked = false;
    }

    /**
     * Create crosshair overlay for FPS mode.
     */
    _createCrosshair() {
        const crosshair = document.createElement('div');
        crosshair.id = 'adaptive-crosshair';
        crosshair.style.cssText = `
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 20px;
            height: 20px;
            pointer-events: none;
            z-index: 100;
        `;
        crosshair.innerHTML = `
            <div style="position:absolute;top:9px;left:0;width:20px;height:2px;background:rgba(255,255,255,0.8);"></div>
            <div style="position:absolute;top:0;left:9px;width:2px;height:20px;background:rgba(255,255,255,0.8);"></div>
        `;
        this.container.appendChild(crosshair);
        this.crosshair = crosshair;
    }

    /**
     * Remove crosshair overlay.
     */
    _removeCrosshair() {
        if (this.crosshair && this.crosshair.parentNode) {
            this.crosshair.parentNode.removeChild(this.crosshair);
            this.crosshair = null;
        }
    }

    /**
     * Create weapon model placeholder.
     */
    _createWeaponModel() {
        const weaponGroup = new THREE.Group();

        // Gun barrel
        const barrelGeo = new THREE.CylinderGeometry(0.05, 0.05, 0.6, 8);
        const barrelMat = new THREE.MeshStandardMaterial({ color: 0x333333, metalness: 0.8, roughness: 0.2 });
        const barrel = new THREE.Mesh(barrelGeo, barrelMat);
        barrel.rotation.x = Math.PI / 2;
        barrel.position.set(0, 0, -0.3);
        weaponGroup.add(barrel);

        // Gun body
        const bodyGeo = new THREE.BoxGeometry(0.1, 0.15, 0.3);
        const bodyMat = new THREE.MeshStandardMaterial({ color: 0x222222, metalness: 0.6, roughness: 0.4 });
        const body = new THREE.Mesh(bodyGeo, bodyMat);
        weaponGroup.add(body);

        // Position weapon relative to camera
        weaponGroup.position.set(0.3, -0.3, -0.5);
        this.camera.add(weaponGroup);

        weaponGroup.userData = { id: 'fps-weapon', type: 'weapon' };
        this.objects.set('fps-weapon', weaponGroup);
    }

    /**
     * Create grid overlay for tactical mode.
     * @param {number} size - Grid size
     * @param {number} divisions - Number of divisions
     * @param {number} centerColor - Center line color
     * @param {number} gridColor - Grid line color
     */
    _createGrid(size, divisions, centerColor, gridColor) {
        this._removeGrid();
        this.gridHelper = new THREE.GridHelper(size, divisions, centerColor, gridColor);
        this.gridHelper.material.transparent = true;
        this.gridHelper.material.opacity = 0.5;
        this.scene.add(this.gridHelper);
    }

    /**
     * Remove grid overlay.
     */
    _removeGrid() {
        if (this.gridHelper) {
            this.scene.remove(this.gridHelper);
            this.gridHelper = null;
        }
    }

    /**
     * Create procedural terrain for tactical mode.
     */
    _createTerrain() {
        const size = 50;
        const segments = 50;
        const geometry = new THREE.PlaneGeometry(size, size, segments, segments);
        const vertices = geometry.attributes.position.array;

        // Generate terrain heightmap
        for (let i = 0; i < vertices.length; i += 3) {
            const x = vertices[i];
            const y = vertices[i + 1];
            vertices[i + 2] = this._noise(x * 0.1, y * 0.1) * 2 + this._noise(x * 0.05, y * 0.05) * 4;
        }
        geometry.computeVertexNormals();

        const material = new THREE.MeshStandardMaterial({
            color: 0x4a7c59,
            roughness: 0.9,
            metalness: 0.1,
            flatShading: true
        });

        this.terrainMesh = new THREE.Mesh(geometry, material);
        this.terrainMesh.rotation.x = -Math.PI / 2;
        this.terrainMesh.receiveShadow = true;
        this.terrainMesh.userData = { id: 'tactical-terrain', type: 'terrain' };
        this.scene.add(this.terrainMesh);
    }

    /**
     * Simple noise function for terrain generation.
     * @param {number} x - X coordinate
     * @param {number} y - Y coordinate
     * @returns {number} Noise value
     */
    _noise(x, y) {
        const n = Math.sin(x * 12.9898 + y * 78.233) * 43758.5453;
        return n - Math.floor(n);
    }

    /**
     * Create sky dome for narrative mode.
     */
    _createSkyDome() {
        const skyGeo = new THREE.SphereGeometry(500, 32, 32);
        const skyMat = new THREE.MeshBasicMaterial({
            color: 0x1a1a2e,
            side: THREE.BackSide
        });
        this.skyDome = new THREE.Mesh(skyGeo, skyMat);
        this.scene.add(this.skyDome);
    }

    /**
     * Update fog density based on camera mode.
     * @param {string} mode - Camera mode
     */
    _updateFogForMode(mode) {
        switch (mode) {
            case 'fps':
                this.scene.fog = new THREE.FogExp2(0x111122, 0.01);
                break;
            case 'galaxy':
                this.scene.fog = new THREE.FogExp2(0x000000, 0.0005);
                break;
            case 'tactical':
                this.scene.fog = new THREE.FogExp2(0x4a7c59, 0.008);
                break;
            case 'narrative':
                this.scene.fog = new THREE.FogExp2(0x1a1a2e, 0.003);
                break;
        }
    }

    /**
     * Animate FOV transition.
     * @param {number} targetFov - Target FOV value
     */
    _animateFOV(targetFov) {
        const startFov = this.camera.fov;
        const duration = 500;
        const startTime = Date.now();

        const animate = () => {
            const elapsed = Date.now() - startTime;
            const progress = Math.min(1, elapsed / duration);
            const eased = this._easeInOutCubic(progress);
            this.camera.fov = startFov + (targetFov - startFov) * eased;
            this.camera.updateProjectionMatrix();
            if (progress < 1) {
                requestAnimationFrame(animate);
            }
        };
        animate();
    }

    /**
     * Update camera during mode transition.
     */
    _updateCameraTransition() {
        if (this.transitionProgress >= 1) return;

        const eased = this._easeInOutCubic(this.transitionProgress);
        const state = this.cameraStates[this.currentMode];
        if (!state) return;

        // Interpolate camera position
        if (this.currentMode === 'fps') {
            this.camera.position.set(
                state.position.x,
                state.position.y,
                state.position.z
            );
        } else {
            this.orbitState.target = { ...state.target };
            this._updateCameraFromOrbit();
        }
    }

    /**
     * Easing function for smooth transitions.
     * @param {number} t - Progress value (0-1)
     * @returns {number} Eased value
     */
    _easeInOutCubic(t) {
        return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
    }

    /**
     * Add a fleet marker to the scene.
     * @param {string} id - Fleet ID
     * @param {number} x - X position
     * @param {number} y - Y position
     * @param {number} z - Z position
     * @param {string} type - Fleet type: 'warship', 'transport', 'fighter', 'capital', 'support'
     * @param {object} data - Fleet data
     */
    addFleetMarker(id, x, y, z, type, data) {
        if (!this.isInitialized) return;

        // Remove existing marker
        this.removeFleetMarker(id);

        const group = new THREE.Group();
        group.position.set(x, y, z);

        const colors = {
            warship: 0xff4444,
            transport: 0x4444ff,
            fighter: 0x44ff44,
            capital: 0xffaa00,
            support: 0xaa44ff
        };

        const color = colors[type] || 0xffffff;

        // Fleet cone marker
        const coneGeo = new THREE.ConeGeometry(1, 2, 8);
        const coneMat = new THREE.MeshStandardMaterial({
            color: color,
            emissive: color,
            emissiveIntensity: 0.3,
            transparent: true,
            opacity: 0.8
        });
        const cone = new THREE.Mesh(coneGeo, coneMat);
        cone.castShadow = true;
        group.add(cone);

        // Health bar
        const barGeo = new THREE.PlaneGeometry(2, 0.2);
        const barMat = new THREE.MeshBasicMaterial({
            color: 0x00ff00,
            transparent: true,
            opacity: 0.8,
            side: THREE.DoubleSide
        });
        const bar = new THREE.Mesh(barGeo, barMat);
        bar.position.y = 1.5;
        group.add(bar);

        group.userData = {
            interactive: true,
            id: id,
            type: 'fleet',
            fleetType: type,
            data: data || {}
        };

        this.scene.add(group);
        this.fleetMarkers.set(id, group);

        // Animate marker
        const animateMarker = () => {
            if (!this.fleetMarkers.has(id)) return;
            cone.rotation.y += 0.02;
            requestAnimationFrame(animateMarker);
        };
        animateMarker();
    }

    /**
     * Remove a fleet marker from the scene.
     * @param {string} id - Fleet ID
     */
    removeFleetMarker(id) {
        const marker = this.fleetMarkers.get(id);
        if (marker) {
            this.scene.remove(marker);
            this.fleetMarkers.delete(id);
        }
    }

    /**
     * Update a fleet marker position.
     * @param {string} id - Fleet ID
     * @param {number} x - X position
     * @param {number} y - Y position
     * @param {number} z - Z position
     */
    updateFleetMarkerPosition(id, x, y, z) {
        const marker = this.fleetMarkers.get(id);
        if (marker) {
            marker.position.set(x, y, z);
        }
    }

    /**
     * Update a fleet marker health bar.
     * @param {string} id - Fleet ID
     * @param {number} healthPercent - Health percentage (0-1)
     */
    updateFleetMarkerHealth(id, healthPercent) {
        const marker = this.fleetMarkers.get(id);
        if (marker) {
            const bar = marker.children[1]; // Health bar
            if (bar) {
                bar.scale.x = Math.max(0.01, healthPercent);
                bar.material.color.setHex(
                    healthPercent > 0.5 ? 0x00ff00 : healthPercent > 0.25 ? 0xffff00 : 0xff0000
                );
            }
        }
    }

    /**
     * Add a particle effect.
     * @param {number} x - X position
     * @param {number} y - Y position
     * @param {number} z - Z position
     * @param {string} type - Effect type: 'explosion', 'sparkle', 'smoke', 'fire'
     * @param {number} count - Number of particles
     */
    addParticleEffect(x, y, z, type, count) {
        if (!this.isInitialized) return;

        const colors = {
            explosion: 0xff6600,
            sparkle: 0xffff00,
            smoke: 0x888888,
            fire: 0xff3300
        };

        const color = colors[type] || 0xffffff;

        for (let i = 0; i < count; i++) {
            const size = 0.1 + Math.random() * 0.3;
            const geo = new THREE.SphereGeometry(size, 4, 4);
            const mat = new THREE.MeshBasicMaterial({
                color: color,
                transparent: true,
                opacity: 1
            });
            const mesh = new THREE.Mesh(geo, mat);
            mesh.position.set(x, y, z);

            const velocity = new THREE.Vector3(
                (Math.random() - 0.5) * 2,
                Math.random() * 2,
                (Math.random() - 0.5) * 2
            );

            this.scene.add(mesh);
            this.particles.push({
                mesh: mesh,
                velocity: velocity,
                life: 1,
                deltaLife: 0.01 + Math.random() * 0.02
            });
        }
    }

    /**
     * Add an attack beam between two points.
     * @param {number} x1 - Start X
     * @param {number} y1 - Start Y
     * @param {number} z1 - Start Z
     * @param {number} x2 - End X
     * @param {number} y2 - End Y
     * @param {number} z2 - End Z
     * @param {number} color - Beam color
     */
    addAttackBeam(x1, y1, z1, x2, y2, z2, color) {
        if (!this.isInitialized) return;

        const points = [
            new THREE.Vector3(x1, y1, z1),
            new THREE.Vector3(x2, y2, z2)
        ];
        const geo = new THREE.BufferGeometry().setFromPoints(points);
        const mat = new THREE.LineBasicMaterial({
            color: color || 0xff0000,
            transparent: true,
            opacity: 0.8
        });
        const line = new THREE.Line(geo, mat);
        this.scene.add(line);

        // Fade out and remove
        const fadeOut = () => {
            mat.opacity -= 0.05;
            if (mat.opacity <= 0) {
                this.scene.remove(line);
                geo.dispose();
                mat.dispose();
                return;
            }
            requestAnimationFrame(fadeOut);
        };
        fadeOut();
    }

    /**
     * Enable minimap overlay.
     * @param {boolean} visible - Whether to show the minimap
     */
    setMinimapVisible(visible) {
        this.minimapVisible = visible;

        if (visible && !this.minimapCanvas) {
            this._createMinimap();
        } else if (!visible && this.minimapCanvas) {
            this._removeMinimap();
        }
    }

    /**
     * Create minimap canvas overlay.
     */
    _createMinimap() {
        const size = 150;
        this.minimapCanvas = document.createElement('canvas');
        this.minimapCanvas.width = size;
        this.minimapCanvas.height = size;
        this.minimapCanvas.style.cssText = `
            position: absolute;
            bottom: 20px;
            right: 20px;
            width: ${size}px;
            height: ${size}px;
            border: 2px solid rgba(100, 255, 218, 0.5);
            border-radius: 50%;
            background: rgba(0, 0, 0, 0.7);
            z-index: 50;
        `;
        this.container.appendChild(this.minimapCanvas);
        this.minimapContext = this.minimapCanvas.getContext('2d');
    }

    /**
     * Remove minimap canvas overlay.
     */
    _removeMinimap() {
        if (this.minimapCanvas && this.minimapCanvas.parentNode) {
            this.minimapCanvas.parentNode.removeChild(this.minimapCanvas);
            this.minimapCanvas = null;
            this.minimapContext = null;
        }
    }

    /**
     * Update minimap rendering.
     */
    _updateMinimap() {
        if (!this.minimapContext || !this.minimapVisible) return;

        const ctx = this.minimapContext;
        const size = this.minimapCanvas.width;
        const center = size / 2;

        // Clear
        ctx.clearRect(0, 0, size, size);

        // Background
        ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        ctx.beginPath();
        ctx.arc(center, center, center, 0, Math.PI * 2);
        ctx.fill();

        // Draw fleet markers
        this.fleetMarkers.forEach((marker, id) => {
            const x = center + (marker.position.x - this.camera.position.x) * 0.5;
            const y = center + (marker.position.z - this.camera.position.z) * 0.5;

            // Clip to circle
            const dist = Math.sqrt((x - center) ** 2 + (y - center) ** 2);
            if (dist > center - 5) return;

            const color = marker.userData.fleetType === 'warship' ? '#ff4444' :
                          marker.userData.fleetType === 'transport' ? '#4444ff' :
                          marker.userData.fleetType === 'fighter' ? '#44ff44' : '#ffffff';

            ctx.fillStyle = color;
            ctx.beginPath();
            ctx.arc(x, y, 3, 0, Math.PI * 2);
            ctx.fill();
        });

        // Draw camera position
        ctx.fillStyle = '#64ffda';
        ctx.beginPath();
        ctx.arc(center, center, 4, 0, Math.PI * 2);
        ctx.fill();

        // Border
        ctx.strokeStyle = 'rgba(100, 255, 218, 0.5)';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.arc(center, center, center - 1, 0, Math.PI * 2);
        ctx.stroke();
    }

    /**
     * Handle window resize.
     */
    _onResize() {
        if (!this.renderer || !this.camera) return;

        const width = this.container.clientWidth;
        const height = this.container.clientHeight;
        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
        this.renderer.setSize(width, height);
    }

    /**
     * Get the current camera mode.
     * @returns {string} Current camera mode
     */
    getCameraMode() {
        return this.currentMode;
    }

    /**
     * Get the Three.js scene.
     * @returns {THREE.Scene} The scene object
     */
    getScene() {
        return this.scene;
    }

    /**
     * Get the Three.js camera.
     * @returns {THREE.Camera} The camera object
     */
    getCamera() {
        return this.camera;
    }

    /**
     * Get the Three.js renderer.
     * @returns {THREE.WebGLRenderer} The renderer object
     */
    getRenderer() {
        return this.renderer;
    }

    /**
     * Get the orbit state for external control.
     * @returns {object} Orbit state
     */
    getOrbitState() {
        return this.orbitState;
    }

    /**
     * Set the orbit target for galaxy/tactical modes.
     * @param {number} x - X coordinate
     * @param {number} y - Y coordinate
     * @param {number} z - Z coordinate
     */
    setOrbitTarget(x, y, z) {
        this.orbitState.target = { x, y, z };
        if (this.currentMode === 'galaxy' || this.currentMode === 'tactical') {
            this._updateCameraFromOrbit();
        }
    }

    /**
     * Focus camera on a specific point.
     * @param {number} x - X coordinate
     * @param {number} y - Y coordinate
     * @param {number} z - Z coordinate
     */
    focusOn(x, y, z) {
        if (this.currentMode === 'galaxy' || this.currentMode === 'tactical') {
            this.orbitState.target = { x, y, z };
            this._updateCameraFromOrbit();
        } else if (this.currentMode === 'narrative') {
            this.cameraStates.narrative.target = { x, y, z };
            this.camera.lookAt(x, y, z);
        }
    }

    /**
     * Start the animation loop.
     */
    startAnimation() {
        if (!this.isInitialized) {
            this.init();
        }
        // Animation loop is started in init()
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
     * Destroy the adaptive 3D view and clean up all resources.
     */
    destroy() {
        this.stopAnimation();

        // Disable pointer lock
        this._disablePointerLock();

        // Remove UI overlays
        this._removeCrosshair();
        this._removeMinimap();

        // Remove all objects
        this.objects.forEach(obj => {
            this.scene.remove(obj);
        });
        this.objects.clear();

        this.fleetMarkers.forEach(marker => {
            this.scene.remove(marker);
        });
        this.fleetMarkers.clear();

        // Remove particles
        this.particles.forEach(p => this.scene.remove(p.mesh));
        this.particles = [];

        // Remove grid and terrain
        this._removeGrid();
        if (this.terrainMesh) {
            this.scene.remove(this.terrainMesh);
            this.terrainMesh = null;
        }
        if (this.skyDome) {
            this.scene.remove(this.skyDome);
            this.skyDome = null;
        }
        if (this.starField) {
            this.scene.remove(this.starField);
            this.starField = null;
        }

        // Dispose renderer
        if (this.renderer) {
            if (this.renderer.domElement.parentNode) {
                this.renderer.domElement.parentNode.removeChild(this.renderer.domElement);
            }
            this.renderer.dispose();
            this.renderer = null;
        }

        // Clean up event listeners
        if (this._resizeHandler) {
            window.removeEventListener('resize', this._resizeHandler);
            this._resizeHandler = null;
        }

        this.scene = null;
        this.camera = null;
        this.isInitialized = false;

        console.log('[Adaptive3DView] Destroyed');
    }
}

// Expose globally
window.Adaptive3DView = Adaptive3DView;

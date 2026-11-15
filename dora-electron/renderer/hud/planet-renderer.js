/**
 * PlanetRenderer — Procedural planet rendering with detailed surface visualization.
 *
 * Provides:
 * - Procedural terrain generation from PlanetGeometry data
 * - Atmosphere rendering with shader-based effects (clouds, weather, biosphere)
 * - City and facility markers on planet surfaces
 * - Zoom transition from space view to orbital view to surface view
 * - Planet info overlay with stats, resources, and management options
 * - Terrain type visualization from PlanetTerrainCell model
 *
 * Usage:
 *   const planet = new PlanetRenderer(sceneRenderer, apiBaseUrl);
 *   await planet.loadPlanet(planetId);
 *   planet.setZoomLevel('orbital');
 *   planet.destroy();
 */

class PlanetRenderer {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     * @param {string} apiBaseUrl - Base URL for the API
     */
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1';
        
        // Planet data
        this.planetData = null;
        this.planetId = null;
        
        // Zoom levels: 'space', 'orbital', 'surface'
        this.zoomLevel = 'space';
        this.targetZoomLevel = 'space';
        
        // Rendered objects
        this.planetMesh = null;
        this.atmosphereMesh = null;
        this.cloudMesh = null;
        this.markerMeshes = new Map();
        this.terrainMesh = null;
        
        // Animation
        this.animationFrame = null;
        this.rotationSpeed = 0.001;
        this.autoRotate = true;
        
        // Terrain types and their colors
        this.terrainColors = {
            ocean: 0x1155aa,
            deep_ocean: 0x003388,
            beach: 0xddcc88,
            grassland: 0x44aa44,
            forest: 0x228833,
            mountain: 0x888888,
            snow: 0xffffff,
            desert: 0xddaa55,
            tundra: 0x88aa88,
            swamp: 0x446644,
            city: 0x6666aa,
            facility: 0xaa6644
        };
        
        // Interaction
        this.onMarkerClick = null;
        this.onMarkerHover = null;
    }

    /**
     * Load planet data from the backend API.
     * @param {string} planetId - Planet ID to load
     * @returns {Promise<Object>} Planet data
     */
    async loadPlanet(planetId) {
        console.log(`[PlanetRenderer] Loading planet: ${planetId}`);
        this.planetId = planetId;
        
        try {
            const response = await fetch(`${this.apiBaseUrl}/planet-surface/${encodeURIComponent(planetId)}`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            this.planetData = await response.json();
            
            // Clear existing
            this._clearAllMeshes();
            
            // Render planet
            this._renderPlanet();
            
            console.log(`[PlanetRenderer] Loaded planet: ${this.planetData.name || planetId}`);
            return this.planetData;
        } catch (error) {
            console.error('[PlanetRenderer] Failed to load planet:', error);
            // Create procedural planet with default data
            this.planetData = this._generateProceduralPlanet(planetId);
            this._renderPlanet();
            return this.planetData;
        }
    }

    /**
     * Generate procedural planet data when API is unavailable.
     * @param {string} planetId - Planet ID
     * @returns {Object} Procedural planet data
     */
    _generateProceduralPlanet(planetId) {
        const seed = planetId.hashCode || planetId.split('').reduce((a, b) => a + b.charCodeAt(0), 0);
        const random = (min, max) => min + ((seed * 9301 + 49297) % 233280) / 233280 * (max - min);
        
        return {
            id: planetId,
            name: `Planet ${planetId}`,
            radius: 5,
            type: 'terrestrial',
            atmosphere: true,
            hasClouds: true,
            gravity: 0.5 + Math.random() * 1.5,
            temperature: 200 + Math.random() * 300,
            terrain: this._generateTerrain(seed),
            facilities: [],
            resources: {
                minerals: Math.floor(Math.random() * 1000),
                energy: Math.floor(Math.random() * 500),
                population: Math.floor(Math.random() * 100000)
            }
        };
    }

    /**
     * Generate procedural terrain data.
     * @param {number} seed - Random seed
     * @returns {Array} Terrain grid
     */
    _generateTerrain(seed) {
        const size = 32;
        const terrain = [];
        
        for (let lat = 0; lat < size; lat++) {
            terrain[lat] = [];
            for (let lon = 0; lon < size; lon++) {
                const noise = this._noise(lat * 0.1, lon * 0.1, seed);
                const elevation = (noise + 1) / 2;
                
                let type;
                if (elevation < 0.3) type = 'deep_ocean';
                else if (elevation < 0.4) type = 'ocean';
                else if (elevation < 0.45) type = 'beach';
                else if (elevation < 0.6) type = 'grassland';
                else if (elevation < 0.7) type = 'forest';
                else if (elevation < 0.8) type = 'mountain';
                else if (elevation < 0.9) type = 'desert';
                else type = 'snow';
                
                terrain[lat][lon] = { type, elevation };
            }
        }
        
        return terrain;
    }

    /**
     * Simple noise function for terrain generation.
     * @param {number} x - X coordinate
     * @param {number} y - Y coordinate
     * @param {number} seed - Random seed
     * @returns {number} Noise value
     */
    _noise(x, y, seed) {
        const n = Math.sin(x * 12.9898 + y * 78.233 + seed) * 43758.5453;
        return n - Math.floor(n);
    }

    /**
     * Render the planet based on current zoom level.
     */
    _renderPlanet() {
        if (!this.sceneRenderer || !this.planetData) return;
        
        const scene = this.sceneRenderer.scene;
        
        switch (this.zoomLevel) {
            case 'space':
                this._renderSpaceView();
                break;
            case 'orbital':
                this._renderOrbitalView();
                break;
            case 'surface':
                this._renderSurfaceView();
                break;
        }
    }

    /**
     * Render planet from space view (distant sphere).
     */
    _renderSpaceView() {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        const data = this.planetData;
        const radius = data.radius || 5;
        
        // Planet sphere
        const geometry = new THREE.SphereGeometry(radius, 64, 64);
        const material = new THREE.MeshStandardMaterial({
            color: 0x2266aa,
            roughness: 0.8,
            metalness: 0.1
        });
        
        this.planetMesh = new THREE.Mesh(geometry, material);
        scene.add(this.planetMesh);
        
        // Atmosphere glow
        if (data.atmosphere !== false) {
            this._createAtmosphere(radius);
        }
        
        // Cloud layer
        if (data.hasClouds) {
            this._createClouds(radius);
        }
        
        // Set camera for space view
        if (this.sceneRenderer.camera) {
            this.sceneRenderer.camera.position.set(0, radius * 2, radius * 4);
            this.sceneRenderer.camera.lookAt(new THREE.Vector3(0, 0, 0));
        }
    }

    /**
     * Render planet from orbital view (closer, with surface details).
     */
    _renderOrbitalView() {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        const data = this.planetData;
        const radius = data.radius || 5;
        
        // Clear space view objects
        if (this.planetMesh) {
            scene.remove(this.planetMesh);
            this.planetMesh.geometry.dispose();
            this.planetMesh.material.dispose();
        }
        
        // Detailed planet with terrain texture
        const geometry = new THREE.SphereGeometry(radius, 128, 128);
        const texture = this._createTerrainTexture();
        
        const material = new THREE.MeshStandardMaterial({
            map: texture,
            roughness: 0.7,
            metalness: 0.1
        });
        
        this.planetMesh = new THREE.Mesh(geometry, material);
        scene.add(this.planetMesh);
        
        // Atmosphere
        this._createAtmosphere(radius);
        
        // Clouds
        if (data.hasClouds) {
            this._createClouds(radius);
        }
        
        // Facility markers
        if (data.facilities) {
            this._createFacilityMarkers(data.facilities, radius);
        }
        
        // Set camera for orbital view
        if (this.sceneRenderer.camera) {
            this.sceneRenderer.camera.position.set(0, radius * 0.5, radius * 2);
            this.sceneRenderer.camera.lookAt(new THREE.Vector3(0, 0, 0));
        }
    }

    /**
     * Render planet surface view (first-person on surface).
     */
    _renderSurfaceView() {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        const data = this.planetData;
        
        // Clear orbital view objects
        if (this.planetMesh) {
            scene.remove(this.planetMesh);
            this.planetMesh.geometry.dispose();
            this.planetMesh.material.dispose();
        }
        if (this.atmosphereMesh) {
            scene.remove(this.atmosphereMesh);
        }
        if (this.cloudMesh) {
            scene.remove(this.cloudMesh);
        }
        
        // Create terrain mesh
        this._createTerrainMesh(data.terrain);
        
        // Create sky dome
        this._createSkyDome();
        
        // Create facility markers
        if (data.facilities) {
            this._createSurfaceFacilityMarkers(data.facilities);
        }
        
        // Set camera for surface view
        if (this.sceneRenderer.camera) {
            this.sceneRenderer.camera.position.set(0, 2, 5);
            this.sceneRenderer.camera.lookAt(new THREE.Vector3(0, 1, 0));
        }
        
        // Enable orbit controls for surface navigation
        if (this.sceneRenderer.enableOrbitControls) {
            this.sceneRenderer.enableOrbitControls();
            if (this.sceneRenderer.orbitState) {
                this.sceneRenderer.orbitState.minRadius = 2;
                this.sceneRenderer.orbitState.maxRadius = 20;
            }
        }
    }

    /**
     * Create atmosphere glow effect.
     * @param {number} planetRadius - Planet radius
     */
    _createAtmosphere(planetRadius) {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        
        // Remove existing atmosphere
        if (this.atmosphereMesh) {
            scene.remove(this.atmosphereMesh);
        }
        
        const geometry = new THREE.SphereGeometry(planetRadius * 1.05, 64, 64);
        const material = new THREE.ShaderMaterial({
            vertexShader: `
                varying vec3 vNormal;
                varying vec3 vPosition;
                void main() {
                    vNormal = normalize(normalMatrix * normal);
                    vPosition = (modelViewMatrix * vec4(position, 1.0)).xyz;
                    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
                }
            `,
            fragmentShader: `
                varying vec3 vNormal;
                varying vec3 vPosition;
                void main() {
                    float intensity = pow(0.6 - dot(vNormal, vec3(0.0, 0.0, 1.0)), 2.0);
                    gl_FragColor = vec4(0.3, 0.6, 1.0, 1.0) * intensity;
                }
            `,
            blending: THREE.AdditiveBlending,
            side: THREE.BackSide,
            transparent: true
        });
        
        this.atmosphereMesh = new THREE.Mesh(geometry, material);
        scene.add(this.atmosphereMesh);
    }

    /**
     * Create cloud layer.
     * @param {number} planetRadius - Planet radius
     */
    _createClouds(planetRadius) {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        
        // Remove existing clouds
        if (this.cloudMesh) {
            scene.remove(this.cloudMesh);
        }
        
        const geometry = new THREE.SphereGeometry(planetRadius * 1.02, 64, 64);
        
        // Create cloud texture
        const canvas = document.createElement('canvas');
        canvas.width = 512;
        canvas.height = 256;
        const ctx = canvas.getContext('2d');
        
        // Generate cloud pattern
        ctx.fillStyle = 'rgba(0, 0, 0, 0)';
        ctx.fillRect(0, 0, 512, 256);
        
        for (let i = 0; i < 200; i++) {
            const x = Math.random() * 512;
            const y = Math.random() * 256;
            const r = Math.random() * 30 + 10;
            const opacity = Math.random() * 0.3 + 0.1;
            
            const gradient = ctx.createRadialGradient(x, y, 0, x, y, r);
            gradient.addColorStop(0, `rgba(255, 255, 255, ${opacity})`);
            gradient.addColorStop(1, 'rgba(255, 255, 255, 0)');
            
            ctx.fillStyle = gradient;
            ctx.beginPath();
            ctx.arc(x, y, r, 0, Math.PI * 2);
            ctx.fill();
        }
        
        const texture = new THREE.CanvasTexture(canvas);
        const material = new THREE.MeshStandardMaterial({
            map: texture,
            transparent: true,
            opacity: 0.6,
            depthWrite: false
        });
        
        this.cloudMesh = new THREE.Mesh(geometry, material);
        scene.add(this.cloudMesh);
    }

    /**
     * Create terrain texture from terrain data.
     * @returns {THREE.CanvasTexture} Terrain texture
     */
    _createTerrainTexture() {
        const canvas = document.createElement('canvas');
        canvas.width = 1024;
        canvas.height = 512;
        const ctx = canvas.getContext('2d');
        
        const terrain = this.planetData.terrain;
        if (!terrain) {
            // Default blue-green planet
            const gradient = ctx.createLinearGradient(0, 0, 0, 512);
            gradient.addColorStop(0, '#88aacc');
            gradient.addColorStop(0.3, '#2266aa');
            gradient.addColorStop(0.5, '#44aa44');
            gradient.addColorStop(0.7, '#2266aa');
            gradient.addColorStop(1, '#88aacc');
            ctx.fillStyle = gradient;
            ctx.fillRect(0, 0, 1024, 512);
        } else {
            // Render terrain grid
            const rows = terrain.length;
            const cols = terrain[0]?.length || 32;
            const cellW = 1024 / cols;
            const cellH = 512 / rows;
            
            for (let r = 0; r < rows; r++) {
                for (let c = 0; c < cols; c++) {
                    const cell = terrain[r]?.[c];
                    if (cell) {
                        const color = this.terrainColors[cell.type] || 0x2266aa;
                        ctx.fillStyle = `#${color.toString(16).padStart(6, '0')}`;
                        ctx.fillRect(c * cellW, r * cellH, cellW + 1, cellH + 1);
                    }
                }
            }
        }
        
        return new THREE.CanvasTexture(canvas);
    }

    /**
     * Create terrain mesh for surface view.
     * @param {Array} terrain - Terrain grid data
     */
    _createTerrainMesh(terrain) {
        if (typeof THREE === 'undefined' || !terrain) return;
        
        const scene = this.sceneRenderer.scene;
        
        // Remove existing terrain
        if (this.terrainMesh) {
            scene.remove(this.terrainMesh);
        }
        
        const rows = terrain.length;
        const cols = terrain[0]?.length || 32;
        
        const geometry = new THREE.PlaneGeometry(50, 50, cols - 1, rows - 1);
        const positions = geometry.attributes.position.array;
        
        for (let r = 0; r < rows; r++) {
            for (let c = 0; c < cols; c++) {
                const cell = terrain[r]?.[c];
                if (cell) {
                    const index = (r * cols + c) * 3;
                    positions[index + 2] = cell.elevation * 5; // Height
                }
            }
        }
        
        geometry.computeVertexNormals();
        
        // Create vertex colors
        const colors = new Float32Array(rows * cols * 3);
        for (let r = 0; r < rows; r++) {
            for (let c = 0; c < cols; c++) {
                const cell = terrain[r]?.[c];
                if (cell) {
                    const color = new THREE.Color(this.terrainColors[cell.type] || 0x2266aa);
                    const index = (r * cols + c) * 3;
                    colors[index] = color.r;
                    colors[index + 1] = color.g;
                    colors[index + 2] = color.b;
                }
            }
        }
        geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
        
        const material = new THREE.MeshStandardMaterial({
            vertexColors: true,
            roughness: 0.9,
            metalness: 0.1
        });
        
        this.terrainMesh = new THREE.Mesh(geometry, material);
        this.terrainMesh.rotation.x = -Math.PI / 2;
        scene.add(this.terrainMesh);
    }

    /**
     * Create sky dome for surface view.
     */
    _createSkyDome() {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        
        const geometry = new THREE.SphereGeometry(100, 32, 32);
        const material = new THREE.ShaderMaterial({
            vertexShader: `
                varying vec3 vWorldPosition;
                void main() {
                    vec4 worldPosition = modelMatrix * vec4(position, 1.0);
                    vWorldPosition = worldPosition.xyz;
                    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
                }
            `,
            fragmentShader: `
                varying vec3 vWorldPosition;
                void main() {
                    float h = normalize(vWorldPosition).y;
                    vec3 skyColor = mix(vec3(0.2, 0.4, 0.8), vec3(0.6, 0.8, 1.0), max(h, 0.0));
                    gl_FragColor = vec4(skyColor, 1.0);
                }
            `,
            side: THREE.BackSide
        });
        
        const sky = new THREE.Mesh(geometry, material);
        scene.add(sky);
    }

    /**
     * Create facility markers on the planet surface.
     * @param {Array} facilities - Facility data
     * @param {number} radius - Planet radius
     */
    _createFacilityMarkers(facilities, radius) {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        
        facilities.forEach(facility => {
            const marker = this._createFacilityMarker(facility);
            if (marker) {
                // Position on planet surface
                const lat = (facility.latitude || 0) * Math.PI / 180;
                const lon = (facility.longitude || 0) * Math.PI / 180;
                
                marker.position.set(
                    radius * Math.cos(lat) * Math.cos(lon),
                    radius * Math.sin(lat),
                    radius * Math.cos(lat) * Math.sin(lon)
                );
                
                scene.add(marker);
                this.markerMeshes.set(facility.id, marker);
                
                if (this.sceneRenderer.interactiveObjects) {
                    this.sceneRenderer.interactiveObjects.push(marker);
                }
            }
        });
    }

    /**
     * Create facility markers for surface view.
     * @param {Array} facilities - Facility data
     */
    _createSurfaceFacilityMarkers(facilities) {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        
        facilities.forEach(facility => {
            const marker = this._createSurfaceFacilityMarker(facility);
            if (marker) {
                marker.position.set(
                    facility.x || 0,
                    0,
                    facility.z || 0
                );
                
                scene.add(marker);
                this.markerMeshes.set(facility.id, marker);
                
                if (this.sceneRenderer.interactiveObjects) {
                    this.sceneRenderer.interactiveObjects.push(marker);
                }
            }
        });
    }

    /**
     * Create a single facility marker.
     * @param {Object} facility - Facility data
     * @returns {THREE.Group} Marker group
     */
    _createFacilityMarker(facility) {
        if (typeof THREE === 'undefined') return null;
        
        const group = new THREE.Group();
        
        // Marker color based on type
        const colors = {
            city: 0x6666ff,
            mining: 0xff6600,
            factory: 0xff0000,
            research: 0x00ff00,
            military: 0xff0000,
            trade: 0xffff00
        };
        
        const color = colors[facility.type] || 0xffffff;
        
        // Beacon
        const beaconGeo = new THREE.CylinderGeometry(0.05, 0.1, 0.5, 8);
        const beaconMat = new THREE.MeshStandardMaterial({
            color: color,
            emissive: color,
            emissiveIntensity: 0.5
        });
        const beacon = new THREE.Mesh(beaconGeo, beaconMat);
        group.add(beacon);
        
        // Base
        const baseGeo = new THREE.CylinderGeometry(0.2, 0.2, 0.05, 8);
        const baseMat = new THREE.MeshStandardMaterial({ color: 0x444444 });
        const base = new THREE.Mesh(baseGeo, baseMat);
        base.position.y = -0.25;
        group.add(base);
        
        // Label
        if (facility.name) {
            const label = this._createLabel(facility.name);
            if (label) {
                label.position.y = 0.5;
                group.add(label);
            }
        }
        
        group.userData = {
            interactive: true,
            id: facility.id,
            name: facility.name || 'Facility',
            type: facility.type || 'unknown',
            data: facility
        };
        
        return group;
    }

    /**
     * Create a surface facility marker.
     * @param {Object} facility - Facility data
     * @returns {THREE.Group} Marker group
     */
    _createSurfaceFacilityMarker(facility) {
        if (typeof THREE === 'undefined') return null;
        
        const group = new THREE.Group();
        
        const colors = {
            city: 0x6666ff,
            mining: 0xff6600,
            factory: 0xff0000,
            research: 0x00ff00,
            military: 0xff0000,
            trade: 0xffff00
        };
        
        const color = colors[facility.type] || 0xffffff;
        
        // Building
        const buildingGeo = new THREE.BoxGeometry(1, 2, 1);
        const buildingMat = new THREE.MeshStandardMaterial({
            color: color,
            emissive: color,
            emissiveIntensity: 0.2
        });
        const building = new THREE.Mesh(buildingGeo, buildingMat);
        building.position.y = 1;
        group.add(building);
        
        // Label
        if (facility.name) {
            const label = this._createLabel(facility.name);
            if (label) {
                label.position.y = 2.5;
                group.add(label);
            }
        }
        
        group.userData = {
            interactive: true,
            id: facility.id,
            name: facility.name || 'Facility',
            type: facility.type || 'unknown',
            data: facility
        };
        
        return group;
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
     * Set zoom level.
     * @param {string} level - Zoom level ('space', 'orbital', 'surface')
     */
    setZoomLevel(level) {
        if (!['space', 'orbital', 'surface'].includes(level)) {
            console.warn(`[PlanetRenderer] Invalid zoom level: ${level}`);
            return;
        }
        
        this.targetZoomLevel = level;
        
        // Animate transition
        this._transitionZoom(level);
    }

    /**
     * Transition between zoom levels.
     * @param {string} targetLevel - Target zoom level
     */
    _transitionZoom(targetLevel) {
        // For now, switch immediately
        this.zoomLevel = targetLevel;
        this._renderPlanet();
        
        console.log(`[PlanetRenderer] Zoom level: ${targetLevel}`);
    }

    /**
     * Enable interaction on facility markers.
     * @param {Function} onClick - Called when a marker is clicked
     * @param {Function} onHover - Called when hovering over a marker
     */
    enableInteraction(onClick, onHover) {
        if (!this.sceneRenderer) return;
        
        this.onMarkerClick = onClick;
        this.onMarkerHover = onHover;
        
        this.sceneRenderer.enableInteraction(
            (userData) => {
                if (this.onMarkerClick) {
                    this.onMarkerClick(userData);
                }
            },
            (userData) => {
                if (this.onMarkerHover) {
                    this.onMarkerHover(userData);
                }
            }
        );
    }

    /**
     * Get planet statistics.
     * @returns {Object} Planet statistics
     */
    getStatistics() {
        if (!this.planetData) return {};
        
        return {
            name: this.planetData.name,
            type: this.planetData.type,
            radius: this.planetData.radius,
            zoomLevel: this.zoomLevel,
            facilities: this.planetData.facilities?.length || 0,
            resources: this.planetData.resources
        };
    }

    /**
     * Clear all meshes.
     */
    _clearAllMeshes() {
        if (!this.sceneRenderer || !this.sceneRenderer.scene) return;
        
        const scene = this.sceneRenderer.scene;
        
        if (this.planetMesh) {
            scene.remove(this.planetMesh);
            this.planetMesh = null;
        }
        if (this.atmosphereMesh) {
            scene.remove(this.atmosphereMesh);
            this.atmosphereMesh = null;
        }
        if (this.cloudMesh) {
            scene.remove(this.cloudMesh);
            this.cloudMesh = null;
        }
        if (this.terrainMesh) {
            scene.remove(this.terrainMesh);
            this.terrainMesh = null;
        }
        
        this.markerMeshes.forEach(marker => {
            scene.remove(marker);
        });
        this.markerMeshes.clear();
    }

    /**
     * Animation loop.
     */
    startAnimation() {
        const animate = () => {
            this.animationFrame = requestAnimationFrame(animate);
            
            // Rotate planet
            if (this.autoRotate && this.planetMesh) {
                this.planetMesh.rotation.y += this.rotationSpeed;
            }
            
            // Rotate clouds independently
            if (this.cloudMesh) {
                this.cloudMesh.rotation.y += this.rotationSpeed * 1.2;
            }
            
            // Make labels face camera
            if (this.sceneRenderer?.camera) {
                this.markerMeshes.forEach(marker => {
                    marker.children.forEach(child => {
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
     * Destroy the planet renderer.
     */
    destroy() {
        this.stopAnimation();
        this._clearAllMeshes();
        
        this.planetData = null;
        this.planetId = null;
        
        console.log('[PlanetRenderer] Destroyed');
    }
}

// Expose globally
window.PlanetRenderer = PlanetRenderer;

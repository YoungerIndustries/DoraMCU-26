/**
 * GalaxyMapRenderer — Full 3D galaxy map with multi-level navigation.
 *
 * Extends the existing SceneRenderer with:
 * - LOD (Level of Detail) system for 10,000+ celestial bodies
 * - Camera presets from Galaxy3DService API (Top Down, Galactic Plane, etc.)
 * - Sector-based navigation with zoom levels (galaxy → sector → system)
 * - Interactive object selection with raycasting
 * - Minimap overlay for navigation context
 * - Color scheme switching (realistic, scifi, heatmap, empire, terrain)
 *
 * Usage:
 *   const map = new GalaxyMapRenderer(sceneRenderer, apiBaseUrl);
 *   await map.loadGalaxy('Corellian');
 *   map.setCameraPreset('top_down');
 *   map.enableInteraction(onClick, onHover);
 *   map.destroy();
 */

class GalaxyMapRenderer {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     * @param {string} apiBaseUrl - Base URL for the Galaxy3D API (e.g., '/api/v1/galaxy-3d')
     */
    constructor(sceneRenderer, apiBaseUrl) {
        this.sceneRenderer = sceneRenderer;
        this.apiBaseUrl = apiBaseUrl || '/api/v1/galaxy-3d';
        
        // Galaxy data
        this.galaxyData = null;
        this.galaxyName = null;
        this.colorScheme = 'realistic';
        
        // Navigation state
        this.zoomLevel = 'galaxy'; // 'galaxy', 'sector', 'system'
        this.currentSector = null;
        this.currentSystem = null;
        this.cameraPreset = null;
        
        // LOD configuration
        this.lodLevels = {
            galaxy: { minDistance: 500, maxBodies: 500, pointSize: 1.0, labelThreshold: Infinity },
            sector: { minDistance: 100, maxBodies: 2000, pointSize: 2.0, labelThreshold: 500 },
            system: { minDistance: 10, maxBodies: 10000, pointSize: 3.0, labelThreshold: 50 }
        };
        
        // Rendered objects tracking
        this.bodyMeshes = new Map(); // id -> mesh
        this.sectorMeshes = new Map(); // sectorId -> mesh
        this.systemMeshes = new Map(); // systemId -> mesh
        this.labelSprites = new Map(); // id -> sprite
        
        // Minimap
        this.minimapCanvas = null;
        this.minimapContext = null;
        this.minimapVisible = false;
        
        // Interaction
        this.onBodyClick = null;
        this.onBodyHover = null;
        this.selectedBody = null;
        
        // Animation
        this.animationFrame = null;
        this.autoRotate = false;
        this.rotationSpeed = 0.0001;
        
        // Body type to geometry mapping
        this.bodyGeometries = {
            star: { type: 'sphere', size: 1.0, color: '#ffffcc' },
            planet: { type: 'sphere', size: 0.5, color: '#4488ff' },
            moon: { type: 'sphere', size: 0.2, color: '#888888' },
            gas_giant: { type: 'sphere', size: 2.0, color: '#ddaa77' },
            asteroid_belt: { type: 'torus', size: 1.5, color: '#888888' },
            nebula: { type: 'cloud', size: 5.0, color: '#ff4488' },
            black_hole: { type: 'sphere', size: 1.5, color: '#000000' },
            comet: { type: 'sphere', size: 0.3, color: '#aaccff' },
            space_station: { type: 'box', size: 0.4, color: '#4488ff' },
            asteroid: { type: 'dodecahedron', size: 0.2, color: '#666666' },
            fleet: { type: 'cone', size: 0.5, color: '#ffff00' }
        };
    }

    /**
     * Load galaxy data from the backend API.
     * @param {string} galaxyName - Name of the galaxy to load
     * @param {string} colorScheme - Color scheme (realistic, scifi, heatmap, empire, terrain)
     * @returns {Promise<Object>} Galaxy 3D data
     */
    async loadGalaxy(galaxyName, colorScheme = 'realistic') {
        console.log(`[GalaxyMap] Loading galaxy: ${galaxyName} with scheme: ${colorScheme}`);
        this.galaxyName = galaxyName;
        this.colorScheme = colorScheme;
        
        try {
            const response = await fetch(`${this.apiBaseUrl}/galaxy/${encodeURIComponent(galaxyName)}?colorScheme=${colorScheme}`);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            this.galaxyData = await response.json();
            
            // Clear existing rendered objects
            this._clearAllMeshes();
            
            // Render the galaxy
            this._renderGalaxy();
            
            // Set default camera preset
            this.setCameraPreset('top_down');
            
            // Enable orbit controls for navigation
            if (this.sceneRenderer) {
                this.sceneRenderer.enableOrbitControls();
            }
            
            console.log(`[GalaxyMap] Loaded ${this.galaxyData.bodies?.length || 0} bodies`);
            return this.galaxyData;
        } catch (error) {
            console.error('[GalaxyMap] Failed to load galaxy:', error);
            throw error;
        }
    }

    /**
     * Render all celestial bodies with LOD.
     */
    _renderGalaxy() {
        if (!this.galaxyData || !this.galaxyData.bodies) return;
        
        const scene = this.sceneRenderer.scene;
        const camera = this.sceneRenderer.camera;
        const lod = this.lodLevels[this.zoomLevel];
        
        // Sort bodies by distance from camera for LOD
        const cameraPos = camera.position;
        const bodies = this.galaxyData.bodies.map(body => {
            const pos = body.position;
            const distance = Math.sqrt(
                (pos.x - cameraPos.x) ** 2 +
                (pos.y - cameraPos.y) ** 2 +
                (pos.z - cameraPos.z) ** 2
            );
            return { ...body, distance };
        }).sort((a, b) => a.distance - b.distance);
        
        // Limit bodies based on LOD
        const limitedBodies = bodies.slice(0, lod.maxBodies);
        
        // Create instanced meshes for performance
        this._createInstancedBodies(limitedBodies, lod);
        
        // Create labels for nearby bodies
        if (lod.labelThreshold < Infinity) {
            this._createLabels(limitedBodies.filter(b => b.distance < lod.labelThreshold));
        }
        
        // Create sector grid overlay
        this._createSectorGrid();
        
        // Update minimap
        if (this.minimapVisible) {
            this._updateMinimap();
        }
    }

    /**
     * Create instanced meshes for celestial bodies based on type.
     * @param {Array} bodies - Array of celestial body data
     * @param {Object} lod - LOD configuration
     */
    _createInstancedBodies(bodies, lod) {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        
        // Group bodies by type for instanced rendering
        const bodiesByType = {};
        bodies.forEach(body => {
            const type = body.type || 'star';
            if (!bodiesByType[type]) bodiesByType[type] = [];
            bodiesByType[type].push(body);
        });
        
        // Create meshes for each type
        for (const [type, typeBodies] of Object.entries(bodiesByType)) {
            const config = this.bodyGeometries[type] || this.bodyGeometries.star;
            
            // For far distances, use points for performance
            if (lod.minDistance > 200) {
                this._createPointMesh(typeBodies, type, config);
            } else {
                this._createDetailedMeshes(typeBodies, type, config);
            }
        }
    }

    /**
     * Create a point cloud mesh for distant bodies (high performance).
     * @param {Array} bodies - Array of celestial body data
     * @param {string} type - Body type
     * @param {Object} config - Geometry configuration
     */
    _createPointMesh(bodies, type, config) {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        const count = bodies.length;
        
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        const colors = new Float32Array(count * 3);
        const sizes = new Float32Array(count);
        
        bodies.forEach((body, i) => {
            positions[i * 3] = body.position.x;
            positions[i * 3 + 1] = body.position.y;
            positions[i * 3 + 2] = body.position.z;
            
            const color = new THREE.Color(body.color || config.color);
            colors[i * 3] = color.r;
            colors[i * 3 + 1] = color.g;
            colors[i * 3 + 2] = color.b;
            
            sizes[i] = (body.size || 1) * this.lodLevels[this.zoomLevel].pointSize;
        });
        
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
        geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1));
        
        const material = new THREE.PointsMaterial({
            size: this.lodLevels[this.zoomLevel].pointSize,
            vertexColors: true,
            transparent: true,
            opacity: 0.8,
            sizeAttenuation: true
        });
        
        const points = new THREE.Points(geometry, material);
        points.name = `galaxy_points_${type}`;
        points.userData = { type, bodies };
        scene.add(points);
        this.bodyMeshes.set(`points_${type}`, points);
    }

    /**
     * Create detailed meshes for nearby bodies.
     * @param {Array} bodies - Array of celestial body data
     * @param {string} type - Body type
     * @param {Object} config - Geometry configuration
     */
    _createDetailedMeshes(bodies, type, config) {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        
        bodies.forEach(body => {
            const mesh = this._createBodyMesh(body, config);
            if (mesh) {
                scene.add(mesh);
                this.bodyMeshes.set(body.id, mesh);
                
                // Add to interactive objects if SceneRenderer supports it
                if (this.sceneRenderer.interactiveObjects) {
                    this.sceneRenderer.interactiveObjects.push(mesh);
                }
            }
        });
    }

    /**
     * Create a single mesh for a celestial body.
     * @param {Object} body - Celestial body data
     * @param {Object} config - Geometry configuration
     * @returns {THREE.Mesh} The created mesh
     */
    _createBodyMesh(body, config) {
        if (typeof THREE === 'undefined') return null;
        
        let geometry;
        const size = (body.size || 1) * 0.5;
        
        switch (config.type) {
            case 'sphere':
                geometry = new THREE.SphereGeometry(size, 16, 16);
                break;
            case 'box':
                geometry = new THREE.BoxGeometry(size, size, size);
                break;
            case 'cone':
                geometry = new THREE.ConeGeometry(size * 0.5, size * 2, 8);
                break;
            case 'torus':
                geometry = new THREE.TorusGeometry(size, size * 0.3, 8, 16);
                break;
            case 'dodecahedron':
                geometry = new THREE.DodecahedronGeometry(size);
                break;
            case 'cloud':
                // Nebula: multiple transparent spheres
                const group = new THREE.Group();
                for (let i = 0; i < 5; i++) {
                    const cloudGeo = new THREE.SphereGeometry(size * (0.5 + Math.random() * 0.5), 8, 8);
                    const cloudMat = new THREE.MeshStandardMaterial({
                        color: body.color || config.color,
                        transparent: true,
                        opacity: 0.1 + Math.random() * 0.1,
                        emissive: body.color ? new THREE.Color(body.color) : new THREE.Color(config.color),
                        emissiveIntensity: 0.3
                    });
                    const cloud = new THREE.Mesh(cloudGeo, cloudMat);
                    cloud.position.set(
                        (Math.random() - 0.5) * size,
                        (Math.random() - 0.5) * size,
                        (Math.random() - 0.5) * size
                    );
                    group.add(cloud);
                }
                group.position.set(body.position.x, body.position.y, body.position.z);
                group.userData = {
                    interactive: true,
                    id: body.id,
                    name: body.name,
                    type: body.type,
                    data: body
                };
                return group;
            default:
                geometry = new THREE.SphereGeometry(size, 8, 8);
        }
        
        const color = new THREE.Color(body.color || config.color);
        const material = new THREE.MeshStandardMaterial({
            color: color,
            emissive: color,
            emissiveIntensity: body.type === 'star' ? 0.8 : 0.2,
            transparent: body.type === 'nebula',
            opacity: body.type === 'nebula' ? 0.3 : 1.0
        });
        
        const mesh = new THREE.Mesh(geometry, material);
        mesh.position.set(body.position.x, body.position.y, body.position.z);
        
        // Store body data in userData for interaction
        mesh.userData = {
            interactive: true,
            id: body.id,
            name: body.name,
            type: body.type,
            data: body
        };
        
        // Add glow effect for stars
        if (body.type === 'star') {
            const glowGeo = new THREE.SphereGeometry(size * 1.5, 8, 8);
            const glowMat = new THREE.MeshStandardMaterial({
                color: color,
                transparent: true,
                opacity: 0.2,
                emissive: color,
                emissiveIntensity: 0.5
            });
            const glow = new THREE.Mesh(glowGeo, glowMat);
            mesh.add(glow);
        }
        
        // Add ring for gas giants
        if (body.type === 'gas_giant') {
            const ringGeo = new THREE.RingGeometry(size * 1.3, size * 1.8, 32);
            const ringMat = new THREE.MeshStandardMaterial({
                color: 0xddaa77,
                transparent: true,
                opacity: 0.5,
                side: THREE.DoubleSide
            });
            const ring = new THREE.Mesh(ringGeo, ringMat);
            ring.rotation.x = Math.PI / 3;
            mesh.add(ring);
        }
        
        return mesh;
    }

    /**
     * Create text labels for nearby bodies.
     * @param {Array} bodies - Array of celestial body data
     */
    _createLabels(bodies) {
        if (typeof THREE === 'undefined') return;
        
        bodies.forEach(body => {
            const mesh = this.bodyMeshes.get(body.id);
            if (mesh && body.name) {
                const label = this._createLabel(body.name);
                if (label) {
                    mesh.add(label);
                    this.labelSprites.set(body.id, label);
                }
            }
        });
    }

    /**
     * Create a text label sprite.
     * @param {string} text - Label text
     * @returns {THREE.Sprite} The label sprite
     */
    _createLabel(text) {
        if (typeof THREE === 'undefined') return null;
        
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = 256;
        canvas.height = 64;
        
        // Background
        ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        ctx.beginPath();
        ctx.roundRect(0, 0, 256, 64, 10);
        ctx.fill();
        
        // Text
        ctx.font = 'Bold 20px Arial';
        ctx.fillStyle = '#ffffff';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        
        // Truncate long names
        const maxLen = 20;
        const displayText = text.length > maxLen ? text.substring(0, maxLen) + '...' : text;
        ctx.fillText(displayText, 128, 32);
        
        const texture = new THREE.CanvasTexture(canvas);
        const spriteMat = new THREE.SpriteMaterial({ map: texture, transparent: true });
        const sprite = new THREE.Sprite(spriteMat);
        sprite.scale.set(4, 1, 1);
        sprite.position.y = 2;
        
        return sprite;
    }

    /**
     * Create sector grid overlay.
     */
    _createSectorGrid() {
        if (typeof THREE === 'undefined') return;
        
        const scene = this.sceneRenderer.scene;
        
        // Remove existing grid
        const existingGrid = scene.getObjectByName('sector_grid');
        if (existingGrid) {
            scene.remove(existingGrid);
        }
        
        // Create grid based on zoom level
        const gridSize = this.zoomLevel === 'galaxy' ? 1000 : this.zoomLevel === 'sector' ? 200 : 50;
        const gridDivisions = this.zoomLevel === 'galaxy' ? 20 : this.zoomLevel === 'sector' ? 10 : 5;
        const gridColor = this.zoomLevel === 'galaxy' ? 0x222244 : 0x444488;
        
        const grid = new THREE.GridHelper(gridSize, gridDivisions, gridColor, 0x111122);
        grid.name = 'sector_grid';
        grid.position.y = -1;
        scene.add(grid);
    }

    /**
     * Set camera to a preset position.
     * @param {string} presetId - Preset ID (top_down, galactic_plane, close_up, wide_angle, sector_view)
     */
    setCameraPreset(presetId) {
        if (!this.sceneRenderer || !this.galaxyData) return;
        
        // Get presets from galaxy data
        const presets = this.galaxyData.cameraPresets || [];
        const preset = presets.find(p => p.id === presetId);
        
        if (!preset) {
            console.warn(`[GalaxyMap] Camera preset not found: ${presetId}`);
            return;
        }
        
        this.cameraPreset = preset;
        
        // Set camera position and target
        const pos = preset.position;
        const target = preset.target;
        
        this.sceneRenderer.setCamera(
            { x: pos.x, y: pos.y, z: pos.z },
            { x: target.x, y: target.y, z: target.z }
        );
        
        // Update orbit controls if enabled
        if (this.sceneRenderer.orbitState) {
            this.sceneRenderer.orbitState.minRadius = preset.minDistance || 1;
            this.sceneRenderer.orbitState.maxRadius = preset.maxDistance || 10000;
        }
        
        // Update LOD based on preset
        this._updateZoomLevel(presetId);
        
        // Re-render with new LOD
        this._renderGalaxy();
        
        console.log(`[GalaxyMap] Camera preset: ${preset.name}`);
    }

    /**
     * Update zoom level based on camera preset.
     * @param {string} presetId - Preset ID
     */
    _updateZoomLevel(presetId) {
        switch (presetId) {
            case 'top_down':
            case 'wide_angle':
                this.zoomLevel = 'galaxy';
                break;
            case 'galactic_plane':
            case 'sector_view':
                this.zoomLevel = 'sector';
                break;
            case 'close_up':
                this.zoomLevel = 'system';
                break;
            default:
                this.zoomLevel = 'galaxy';
        }
    }

    /**
     * Get available camera presets.
     * @returns {Array} List of camera presets
     */
    getCameraPresets() {
        return this.galaxyData?.cameraPresets || [];
    }

    /**
     * Filter bodies by type.
     * @param {string} type - Body type to filter
     * @returns {Array} Filtered bodies
     */
    getBodiesByType(type) {
        if (!this.galaxyData) return [];
        return this.galaxyData.bodies.filter(b => b.type === type);
    }

    /**
     * Get bodies near a position.
     * @param {Object} position - {x, y, z} position
     * @param {number} radius - Search radius
     * @returns {Array} Nearby bodies
     */
    getBodiesNearPosition(position, radius) {
        if (!this.galaxyData) return [];
        
        return this.galaxyData.bodies.filter(body => {
            const dx = body.position.x - position.x;
            const dy = body.position.y - position.y;
            const dz = body.position.z - position.z;
            return Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius;
        });
    }

    /**
     * Focus camera on a specific body.
     * @param {string} bodyId - Body ID to focus on
     */
    focusOnBody(bodyId) {
        if (!this.galaxyData || !this.sceneRenderer) return;
        
        const body = this.galaxyData.bodies.find(b => b.id === bodyId);
        if (!body) {
            console.warn(`[GalaxyMap] Body not found: ${bodyId}`);
            return;
        }
        
        this.selectedBody = body;
        
        // Calculate camera position for close-up view
        const pos = body.position;
        const distance = Math.max(body.size * 5, 10);
        
        this.sceneRenderer.setCamera(
            { x: pos.x + distance, y: pos.y + distance / 2, z: pos.z + distance },
            { x: pos.x, y: pos.y, z: pos.z }
        );
        
        // Update zoom level to system
        this.zoomLevel = 'system';
        this._renderGalaxy();
        
        // Highlight the selected body
        this._highlightBody(bodyId, true);
        
        console.log(`[GalaxyMap] Focused on: ${body.name}`);
    }

    /**
     * Highlight or unhighlight a body.
     * @param {string} bodyId - Body ID
     * @param {boolean} highlight - Whether to highlight
     */
    _highlightBody(bodyId, highlight) {
        const mesh = this.bodyMeshes.get(bodyId);
        if (!mesh) return;
        
        mesh.traverse(child => {
            if (child.isMesh && child.material) {
                if (highlight) {
                    child.material._originalEmissive = child.material.emissive ? child.material.emissive.getHex() : 0;
                    child.material._originalEmissiveIntensity = child.material.emissiveIntensity || 0;
                    if (child.material.emissive) {
                        child.material.emissive.setHex(0xffffff);
                        child.material.emissiveIntensity = 1.0;
                    }
                } else {
                    if (child.material._originalEmissive !== undefined) {
                        child.material.emissive.setHex(child.material._originalEmissive);
                        child.material.emissiveIntensity = child.material._originalEmissiveIntensity;
                    }
                }
            }
        });
    }

    /**
     * Enable interaction (click/hover) on celestial bodies.
     * @param {Function} onClick - Callback when a body is clicked
     * @param {Function} onHover - Callback when hovering over a body
     */
    enableInteraction(onClick, onHover) {
        if (!this.sceneRenderer) return;
        
        this.onBodyClick = onClick;
        this.onBodyHover = onHover;
        
        // Use SceneRenderer's interaction system
        this.sceneRenderer.enableInteraction(
            (userData) => {
                if (this.onBodyClick) {
                    this.onBodyClick(userData);
                }
                // Also focus on the body
                if (userData.id) {
                    this.focusOnBody(userData.id);
                }
            },
            (userData) => {
                if (this.onBodyHover) {
                    this.onBodyHover(userData);
                }
            }
        );
    }

    /**
     * Enable minimap overlay.
     * @param {HTMLElement} container - Container element for the minimap
     */
    enableMinimap(container) {
        if (!container) return;
        
        this.minimapVisible = true;
        this.minimapContainer = container;
        
        // Create canvas
        this.minimapCanvas = document.createElement('canvas');
        this.minimapCanvas.width = 200;
        this.minimapCanvas.height = 200;
        this.minimapCanvas.style.cssText = 'position:absolute;top:10px;right:10px;border:1px solid #444;background:rgba(0,0,0,0.8);border-radius:4px;';
        this.minimapContext = this.minimapCanvas.getContext('2d');
        
        container.appendChild(this.minimapCanvas);
        this._updateMinimap();
    }

    /**
     * Update the minimap display.
     */
    _updateMinimap() {
        if (!this.minimapContext || !this.galaxyData) return;
        
        const ctx = this.minimapContext;
        const canvas = this.minimapCanvas;
        const width = canvas.width;
        const height = canvas.height;
        
        // Clear
        ctx.fillStyle = 'rgba(0, 0, 0, 0.8)';
        ctx.fillRect(0, 0, width, height);
        
        // Calculate bounds
        const bodies = this.galaxyData.bodies;
        if (!bodies || bodies.length === 0) return;
        
        let minX = Infinity, maxX = -Infinity, minZ = Infinity, maxZ = -Infinity;
        bodies.forEach(body => {
            minX = Math.min(minX, body.position.x);
            maxX = Math.max(maxX, body.position.x);
            minZ = Math.min(minZ, body.position.z);
            maxZ = Math.max(maxZ, body.position.z);
        });
        
        const rangeX = maxX - minX || 1;
        const rangeZ = maxZ - minZ || 1;
        const padding = 10;
        
        // Draw bodies
        bodies.forEach(body => {
            const x = padding + ((body.position.x - minX) / rangeX) * (width - 2 * padding);
            const y = padding + ((body.position.z - minZ) / rangeZ) * (height - 2 * padding);
            
            const color = body.color || '#ffffff';
            ctx.fillStyle = color;
            ctx.beginPath();
            ctx.arc(x, y, Math.max(1, body.size * 0.1), 0, Math.PI * 2);
            ctx.fill();
        });
        
        // Draw camera position
        if (this.sceneRenderer && this.sceneRenderer.camera) {
            const cam = this.sceneRenderer.camera.position;
            const cx = padding + ((cam.x - minX) / rangeX) * (width - 2 * padding);
            const cy = padding + ((cam.z - minZ) / rangeZ) * (height - 2 * padding);
            
            ctx.strokeStyle = '#ff0000';
            ctx.lineWidth = 2;
            ctx.beginPath();
            ctx.moveTo(cx - 5, cy);
            ctx.lineTo(cx + 5, cy);
            ctx.moveTo(cx, cy - 5);
            ctx.lineTo(cx, cy + 5);
            ctx.stroke();
        }
    }

    /**
     * Toggle auto-rotation of the galaxy view.
     * @param {boolean} enabled - Whether to enable auto-rotation
     */
    setAutoRotate(enabled) {
        this.autoRotate = enabled;
    }

    /**
     * Switch color scheme.
     * @param {string} scheme - Color scheme ID
     */
    async switchColorScheme(scheme) {
        if (this.galaxyName) {
            await this.loadGalaxy(this.galaxyName, scheme);
        }
    }

    /**
     * Get galaxy statistics.
     * @returns {Object} Statistics about the galaxy
     */
    getStatistics() {
        if (!this.galaxyData) return {};
        
        const bodyCounts = {};
        this.galaxyData.bodies.forEach(body => {
            bodyCounts[body.type] = (bodyCounts[body.type] || 0) + 1;
        });
        
        return {
            totalBodies: this.galaxyData.bodies.length,
            bodyCounts,
            zoomLevel: this.zoomLevel,
            cameraPreset: this.cameraPreset?.name,
            selectedBody: this.selectedBody?.name
        };
    }

    /**
     * Clear all rendered meshes.
     */
    _clearAllMeshes() {
        if (!this.sceneRenderer || !this.sceneRenderer.scene) return;
        
        const scene = this.sceneRenderer.scene;
        
        // Remove body meshes
        this.bodyMeshes.forEach(mesh => {
            scene.remove(mesh);
        });
        this.bodyMeshes.clear();
        
        // Remove sector meshes
        this.sectorMeshes.forEach(mesh => {
            scene.remove(mesh);
        });
        this.sectorMeshes.clear();
        
        // Remove system meshes
        this.systemMeshes.forEach(mesh => {
            scene.remove(mesh);
        });
        this.systemMeshes.clear();
        
        // Remove labels
        this.labelSprites.clear();
        
        // Remove sector grid
        const grid = scene.getObjectByName('sector_grid');
        if (grid) scene.remove(grid);
        
        // Remove point clouds
        const pointMeshes = scene.children.filter(c => c.name?.startsWith('galaxy_points_'));
        pointMeshes.forEach(mesh => scene.remove(mesh));
        
        // Clear interactive objects
        if (this.sceneRenderer.interactiveObjects) {
            this.sceneRenderer.interactiveObjects = [];
        }
    }

    /**
     * Animation loop for auto-rotation and minimap updates.
     */
    startAnimation() {
        const animate = () => {
            this.animationFrame = requestAnimationFrame(animate);
            
            // Auto-rotate
            if (this.autoRotate && this.sceneRenderer && this.sceneRenderer.orbitState) {
                this.sceneRenderer.orbitState.spherical.theta += this.rotationSpeed;
                this.sceneRenderer._updateCameraFromSpherical();
            }
            
            // Update minimap periodically
            if (this.minimapVisible && this.minimapContext) {
                this._updateMinimap();
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
     * Destroy the galaxy map and clean up resources.
     */
    destroy() {
        this.stopAnimation();
        this._clearAllMeshes();
        
        // Remove minimap
        if (this.minimapCanvas && this.minimapContainer) {
            this.minimapContainer.removeChild(this.minimapCanvas);
            this.minimapCanvas = null;
            this.minimapContext = null;
        }
        
        this.galaxyData = null;
        this.galaxyName = null;
        this.selectedBody = null;
        
        console.log('[GalaxyMap] Destroyed');
    }
}

// Expose globally
window.GalaxyMapRenderer = GalaxyMapRenderer;

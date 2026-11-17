/**
 * FPS Scene Object Loader — Renders database-driven 3D props from server data.
 *
 * Converts scene object definitions received via WebSocket into Three.js meshes.
 * Uses procedural geometry matched to object types, with InstancedMesh batching
 * for duplicate types (trees, rocks, crates) to keep draw calls low.
 *
 * Usage:
 *   const loader = new FpsSceneObjectLoader(scene, mapGroup);
 *   loader.loadSceneObjects(sceneObjectData);
 *   // In game loop:
 *   loader.update(deltaTime);
 *   // On transition:
 *   loader.dispose();
 */
class FpsSceneObjectLoader {
    constructor(scene, mapGroup) {
        this.scene = scene;
        this.mapGroup = mapGroup;
        this.objects = [];
        this.instancedMeshes = [];
        this.lights = [];
        this.animatedObjects = [];
        this.textureLoader = new THREE.TextureLoader();
        this.loadedTextures = {};  // Cache loaded textures by path
    }

    /**
     * Load scene objects from server data.
     * Groups objects by type for InstancedMesh batching where applicable.
     */
    loadSceneObjects(sceneObjectData) {
        this.dispose();

        if (!sceneObjectData || sceneObjectData.length === 0) {
            console.log('[FpsSceneObjectLoader] No scene objects to load');
            return;
        }

        console.log(`[FpsSceneObjectLoader] Loading ${sceneObjectData.length} scene objects`);

        // Group objects by type for batching
        const groups = {};
        for (const obj of sceneObjectData) {
            const type = obj.objectType || 'unknown';
            if (!groups[type]) groups[type] = [];
            groups[type].push(obj);
        }

        // Process each group
        for (const [type, objects] of Object.entries(groups)) {
            if (objects.length >= 4) {
                // Use InstancedMesh for types with 4+ instances
                this._useInstancedMesh(type, objects);
            } else {
                // Individual meshes for unique objects
                for (const obj of objects) {
                    this._addSingleObject(obj);
                }
            }
        }

        console.log(`[FpsSceneObjectLoader] Loaded ${this.objects.length} individual + ${this.instancedMeshes.length} instanced groups`);
    }

    /**
     * Add a single scene object as an individual mesh.
     */
    _addSingleObject(obj) {
        const mesh = this._createProceduralMesh(obj);
        if (!mesh) return;

        mesh.position.set(obj.x, obj.y, obj.z);
        if (obj.rotationY) mesh.rotation.y = obj.rotationY;

        const sx = obj.scaleX || 1.0;
        const sy = obj.scaleY || 1.0;
        const sz = obj.scaleZ || 1.0;
        mesh.scale.set(sx, sy, sz);

        this.mapGroup.add(mesh);
        this.objects.push(mesh);

        // If the mesh has a light, track it
        if (mesh.children) {
            mesh.children.forEach(child => {
                if (child.isLight) {
                    this.lights.push(child);
                }
            });
        }

        // Track animated objects
        if (obj.objectType === 'street_lamp' || obj.objectType === 'terminal') {
            this.animatedObjects.push({ mesh, type: obj.objectType });
        }
    }

    /**
     * Create an InstancedMesh for multiple objects of the same type.
     */
    _useInstancedMesh(type, objects) {
        const geometry = this._getGeometryForType(type);
        const material = this._getMaterialForType(type, objects[0]);
        if (!geometry || !material) return;

        const mesh = new THREE.InstancedMesh(geometry, material, objects.length);
        mesh.name = `Instanced_${type}`;

        const dummy = new THREE.Object3D();
        for (let i = 0; i < objects.length; i++) {
            const obj = objects[i];
            dummy.position.set(obj.x, obj.y, obj.z);
            if (obj.rotationY) dummy.rotation.y = obj.rotationY;
            const sx = obj.scaleX || 1.0;
            const sy = obj.scaleY || 1.0;
            const sz = obj.scaleZ || 1.0;
            dummy.scale.set(sx, sy, sz);
            dummy.updateMatrix();
            mesh.setMatrixAt(i, dummy.matrix);
        }

        mesh.instanceMatrix.needsUpdate = true;
        this.mapGroup.add(mesh);
        this.instancedMeshes.push(mesh);
    }

    /**
     * Create procedural geometry for a given object type.
     * Passes the full obj to support texture loading.
     */
    _createProceduralMesh(obj) {
        const type = obj.objectType || 'unknown';
        const color = this._parseColor(obj.colorHex);
        const scale = obj.scaleFactor || 1.0;

        switch (type) {
            case 'tree':
                return this._createTree(color, scale, obj);
            case 'building':
                return this._createBuilding(color, scale, obj);
            case 'crate':
                return this._createCrate(color, scale, obj);
            case 'barrel':
                return this._createBarrel(color, scale, obj);
            case 'rock':
                return this._createRock(color, scale, obj);
            case 'street_lamp':
                return this._createStreetLamp(color, scale, obj);
            case 'desk':
                return this._createDesk(color, scale, obj);
            case 'filing_cabinet':
                return this._createFilingCabinet(color, scale, obj);
            case 'bookshelf':
                return this._createBookshelf(color, scale, obj);
            case 'terminal':
                return this._createTerminal(color, scale, obj);
            case 'chair':
                return this._createChair(color, scale, obj);
            case 'coffee_station':
                return this._createCoffeeStation(color, scale, obj);
            default:
                return this._createDefaultObject(color, scale, obj);
        }
    }

    /**
     * Get geometry for InstancedMesh batching.
     */
    _getGeometryForType(type) {
        switch (type) {
            case 'tree':
                return new THREE.ConeGeometry(1.5, 4, 8);
            case 'rock':
                return new THREE.DodecahedronGeometry(1, 0);
            case 'crate':
                return new THREE.BoxGeometry(1.5, 1.5, 1.5);
            case 'barrel':
                return new THREE.CylinderGeometry(0.5, 0.5, 1.2, 12);
            case 'building':
                return new THREE.BoxGeometry(1, 1, 1);
            case 'street_lamp':
                return new THREE.CylinderGeometry(0.05, 0.05, 4, 8);
            case 'desk':
                return new THREE.BoxGeometry(1.5, 0.1, 0.8);
            case 'filing_cabinet':
                return new THREE.BoxGeometry(0.6, 1.2, 0.6);
            case 'bookshelf':
                return new THREE.BoxGeometry(1.5, 2, 0.4);
            case 'terminal':
                return new THREE.BoxGeometry(0.6, 0.4, 0.3);
            default:
                return new THREE.SphereGeometry(0.5, 8, 8);
        }
    }

    /**
     * Get material for InstancedMesh batching.
     * Supports texture loading from server-provided texturePath.
     */
    _getMaterialForType(type, sampleObj) {
        const color = this._parseColor(sampleObj.colorHex);
        const matParams = { color: color };
        
        // Apply texture if available
        if (sampleObj.texturePath) {
            const texture = this._loadTexture(sampleObj.texturePath);
            if (texture) {
                matParams.map = texture;
                matParams.roughness = 0.8;
                matParams.metalness = 0.1;
            }
        }
        
        const mat = new THREE.MeshStandardMaterial(matParams);
        return mat;
    }
    
    /**
     * Load a texture from the server path, with caching.
     */
    _loadTexture(texturePath) {
        if (!texturePath) return null;
        
        // Return cached texture if already loaded
        if (this.loadedTextures[texturePath]) {
            return this.loadedTextures[texturePath];
        }
        
        try {
            // Resolve texture path relative to the app base
            const resolvedPath = texturePath.startsWith('/') ? texturePath : '/textures/' + texturePath;
            const texture = this.textureLoader.load(resolvedPath, undefined, undefined, (err) => {
                console.warn('[FpsSceneObjectLoader] Failed to load texture:', texturePath, err);
            });
            texture.wrapS = THREE.RepeatWrapping;
            texture.wrapT = THREE.RepeatWrapping;
            texture.repeat.set(2, 2);
            this.loadedTextures[texturePath] = texture;
            return texture;
        } catch (e) {
            console.warn('[FpsSceneObjectLoader] Error loading texture:', texturePath, e);
            return null;
        }
    }
    
    /**
     * Create material with optional texture for a single object.
     */
    _createMaterialWithTexture(color, obj) {
        const matParams = { color: color };
        
        if (obj && obj.texturePath) {
            const texture = this._loadTexture(obj.texturePath);
            if (texture) {
                matParams.map = texture;
                matParams.roughness = 0.8;
                matParams.metalness = 0.1;
            }
        }
        
        return new THREE.MeshStandardMaterial(matParams);
    }

    /**
     * Create a tree mesh (trunk + foliage).
     */
    _createTree(color, scale, obj) {
        const group = new THREE.Group();
        group.name = 'Tree';

        // Trunk with optional texture
        const trunkGeo = new THREE.CylinderGeometry(0.15 * scale, 0.25 * scale, 2 * scale, 8);
        const trunkMat = this._createMaterialWithTexture(0x8B4513, obj);
        const trunk = new THREE.Mesh(trunkGeo, trunkMat);
        trunk.position.y = 1 * scale;
        trunk.castShadow = true;
        group.add(trunk);

        // Foliage layers
        const foliageColors = [0x228B22, 0x2E8B57, 0x32CD32, 0x006400];
        const foliageColor = color || foliageColors[Math.floor(Math.random() * foliageColors.length)];

        const sizes = [
            { r: 1.5, h: 2, y: 2.5 },
            { r: 1.2, h: 1.5, y: 3.5 },
            { r: 0.8, h: 1.2, y: 4.2 }
        ];

        for (const s of sizes) {
            const geo = new THREE.ConeGeometry(s.r * scale, s.h * scale, 8);
            const mat = new THREE.MeshStandardMaterial({ color: foliageColor });
            const mesh = new THREE.Mesh(geo, mat);
            mesh.position.y = s.y * scale;
            mesh.castShadow = true;
            group.add(mesh);
        }

        return group;
    }

    /**
     * Create a building mesh with windows and door.
     * Supports texture from server data.
     */
    _createBuilding(color, scale, obj) {
        const group = new THREE.Group();
        group.name = 'Building';

        const buildingColor = color || 0x808080;
        const width = 4 * scale;
        const height = 6 * scale;
        const depth = 4 * scale;

        // Main structure with optional texture
        const geo = new THREE.BoxGeometry(width, height, depth);
        const mat = this._createMaterialWithTexture(buildingColor, obj);
        const mesh = new THREE.Mesh(geo, mat);
        mesh.position.y = height / 2;
        mesh.castShadow = true;
        mesh.receiveShadow = true;
        group.add(mesh);

        // Windows
        const windowMat = new THREE.MeshStandardMaterial({
            color: 0x87CEEB,
            emissive: 0x334455,
            emissiveIntensity: 0.3
        });
        for (let floor = 0; floor < 3; floor++) {
            for (let w = -1; w <= 1; w += 2) {
                const winGeo = new THREE.PlaneGeometry(0.6 * scale, 0.8 * scale);
                const win = new THREE.Mesh(winGeo, windowMat);
                win.position.set(w * 1.2 * scale, (1.5 + floor * 2) * scale, depth / 2 + 0.01);
                group.add(win);
            }
        }

        // Door
        const doorGeo = new THREE.PlaneGeometry(0.8 * scale, 1.5 * scale);
        const doorMat = new THREE.MeshStandardMaterial({ color: 0x654321 });
        const door = new THREE.Mesh(doorGeo, doorMat);
        door.position.set(0, 0.75 * scale, depth / 2 + 0.01);
        group.add(door);

        return group;
    }

    /**
     * Create a crate mesh.
     */
    _createCrate(color, scale, obj) {
        const crateColor = color || 0xDEB887;
        const geo = new THREE.BoxGeometry(1.2 * scale, 1.2 * scale, 1.2 * scale);
        const mat = this._createMaterialWithTexture(crateColor, obj);
        const mesh = new THREE.Mesh(geo, mat);
        mesh.position.y = 0.6 * scale;
        mesh.castShadow = true;
        return mesh;
    }

    /**
     * Create a barrel mesh.
     */
    _createBarrel(color, scale, obj) {
        const barrelColor = color || 0x4A4A4A;
        const geo = new THREE.CylinderGeometry(0.4 * scale, 0.4 * scale, 1 * scale, 12);
        const mat = this._createMaterialWithTexture(barrelColor, obj);
        const mesh = new THREE.Mesh(geo, mat);
        mesh.position.y = 0.5 * scale;
        mesh.castShadow = true;
        return mesh;
    }

    /**
     * Create a rock mesh.
     */
    _createRock(color, scale, obj) {
        const rockColor = color || 0x808080;
        const geo = new THREE.DodecahedronGeometry(0.8 * scale, 0);
        const mat = this._createMaterialWithTexture(rockColor, obj);
        mat.roughness = 0.9;
        const mesh = new THREE.Mesh(geo, mat);
        mesh.position.y = 0.4 * scale;
        mesh.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, 0);
        mesh.castShadow = true;
        return mesh;
    }

    /**
     * Create a street lamp with point light.
     */
    _createStreetLamp(color, scale, obj) {
        const group = new THREE.Group();
        group.name = 'StreetLamp';

        // Pole
        const poleGeo = new THREE.CylinderGeometry(0.05 * scale, 0.08 * scale, 4 * scale, 8);
        const poleMat = new THREE.MeshStandardMaterial({ color: 0x333333 });
        const pole = new THREE.Mesh(poleGeo, poleMat);
        pole.position.y = 2 * scale;
        group.add(pole);

        // Lamp head
        const headGeo = new THREE.SphereGeometry(0.2 * scale, 8, 8);
        const headMat = new THREE.MeshStandardMaterial({
            color: 0xFFFFAA,
            emissive: 0xFFAA00,
            emissiveIntensity: 1.0
        });
        const head = new THREE.Mesh(headGeo, headMat);
        head.position.y = 4 * scale;
        group.add(head);

        // Point light
        const light = new THREE.PointLight(0xFFAA00, 2, 15 * scale);
        light.position.y = 4 * scale;
        group.add(light);

        return group;
    }

    /**
     * Create a desk mesh.
     */
    _createDesk(color, scale, obj) {
        const group = new THREE.Group();
        group.name = 'Desk';

        const deskColor = color || 0x8B6914;

        // Top with optional texture
        const topGeo = new THREE.BoxGeometry(1.5 * scale, 0.1 * scale, 0.8 * scale);
        const topMat = this._createMaterialWithTexture(deskColor, obj);
        const top = new THREE.Mesh(topGeo, topMat);
        top.position.y = 0.8 * scale;
        top.castShadow = true;
        group.add(top);

        // Legs
        const legGeo = new THREE.BoxGeometry(0.08 * scale, 0.8 * scale, 0.08 * scale);
        const legMat = new THREE.MeshStandardMaterial({ color: 0x333333 });
        const positions = [
            [-0.65, 0.4, -0.3], [0.65, 0.4, -0.3],
            [-0.65, 0.4, 0.3], [0.65, 0.4, 0.3]
        ];
        for (const [x, y, z] of positions) {
            const leg = new THREE.Mesh(legGeo, legMat);
            leg.position.set(x * scale, y * scale, z * scale);
            group.add(leg);
        }

        return group;
    }

    /**
     * Create a filing cabinet mesh.
     */
    _createFilingCabinet(color, scale, obj) {
        const cabinetColor = color || 0x696969;
        const geo = new THREE.BoxGeometry(0.6 * scale, 1.2 * scale, 0.6 * scale);
        const mat = this._createMaterialWithTexture(cabinetColor, obj);
        const mesh = new THREE.Mesh(geo, mat);
        mesh.position.y = 0.6 * scale;
        mesh.castShadow = true;
        return mesh;
    }

    /**
     * Create a bookshelf mesh.
     */
    _createBookshelf(color, scale, obj) {
        const group = new THREE.Group();
        group.name = 'Bookshelf';

        const shelfColor = color || 0x8B6914;

        // Frame with optional texture
        const frameGeo = new THREE.BoxGeometry(1.5 * scale, 2 * scale, 0.4 * scale);
        const frameMat = this._createMaterialWithTexture(shelfColor, obj);
        const frame = new THREE.Mesh(frameGeo, frameMat);
        frame.position.y = 1 * scale;
        frame.castShadow = true;
        group.add(frame);

        // Books (colored strips)
        const bookColors = [0xCC0000, 0x0000CC, 0x00CC00, 0xFFCC00, 0x9933FF, 0xFF6600];
        for (let shelf = 0; shelf < 4; shelf++) {
            for (let b = 0; b < 5; b++) {
                const bookGeo = new THREE.BoxGeometry(0.15 * scale, 0.3 * scale, 0.3 * scale);
                const bookColor = bookColors[(shelf * 5 + b) % bookColors.length];
                const bookMat = new THREE.MeshStandardMaterial({ color: bookColor });
                const book = new THREE.Mesh(bookGeo, bookMat);
                book.position.set((-0.6 + b * 0.25) * scale, (0.3 + shelf * 0.5) * scale, 0.05 * scale);
                group.add(book);
            }
        }

        return group;
    }

    /**
     * Create a terminal (computer monitor) mesh.
     */
    _createTerminal(color, scale, obj) {
        const group = new THREE.Group();
        group.name = 'Terminal';

        // Monitor body
        const bodyGeo = new THREE.BoxGeometry(0.6 * scale, 0.4 * scale, 0.05 * scale);
        const bodyMat = new THREE.MeshStandardMaterial({ color: 0x222222 });
        const body = new THREE.Mesh(bodyGeo, bodyMat);
        body.position.y = 1 * scale;
        group.add(body);

        // Screen
        const screenGeo = new THREE.PlaneGeometry(0.5 * scale, 0.3 * scale);
        const screenMat = new THREE.MeshStandardMaterial({
            color: 0x00FF00,
            emissive: 0x00FF00,
            emissiveIntensity: 0.5
        });
        const screen = new THREE.Mesh(screenGeo, screenMat);
        screen.position.set(0, 1 * scale, 0.03 * scale);
        screen.name = 'TerminalScreen';
        group.add(screen);

        // Stand
        const standGeo = new THREE.BoxGeometry(0.1 * scale, 0.2 * scale, 0.1 * scale);
        const standMat = new THREE.MeshStandardMaterial({ color: 0x333333 });
        const stand = new THREE.Mesh(standGeo, standMat);
        stand.position.y = 0.9 * scale;
        group.add(stand);

        return group;
    }

    /**
     * Create a chair mesh.
     */
    _createChair(color, scale, obj) {
        const group = new THREE.Group();
        group.name = 'Chair';

        const chairColor = color || 0x333333;

        // Seat with optional texture
        const seatGeo = new THREE.BoxGeometry(0.5 * scale, 0.05 * scale, 0.5 * scale);
        const seatMat = this._createMaterialWithTexture(chairColor, obj);
        const seat = new THREE.Mesh(seatGeo, seatMat);
        seat.position.y = 0.5 * scale;
        group.add(seat);

        // Back
        const backGeo = new THREE.BoxGeometry(0.5 * scale, 0.5 * scale, 0.05 * scale);
        const back = new THREE.Mesh(backGeo, seatMat);
        back.position.set(0, 0.75 * scale, -0.22 * scale);
        group.add(back);

        // Legs
        const legGeo = new THREE.CylinderGeometry(0.02 * scale, 0.02 * scale, 0.5 * scale, 8);
        const legMat = new THREE.MeshStandardMaterial({ color: 0x888888 });
        const legPositions = [[-0.2, 0.25, -0.2], [0.2, 0.25, -0.2], [-0.2, 0.25, 0.2], [0.2, 0.25, 0.2]];
        for (const [x, y, z] of legPositions) {
            const leg = new THREE.Mesh(legGeo, legMat);
            leg.position.set(x * scale, y * scale, z * scale);
            group.add(leg);
        }

        return group;
    }

    /**
     * Create a coffee station mesh.
     */
    _createCoffeeStation(color, scale, obj) {
        const group = new THREE.Group();
        group.name = 'CoffeeStation';

        const stationColor = color || 0x696969;

        // Counter with optional texture
        const counterGeo = new THREE.BoxGeometry(1 * scale, 0.9 * scale, 0.6 * scale);
        const counterMat = this._createMaterialWithTexture(stationColor, obj);
        const counter = new THREE.Mesh(counterGeo, counterMat);
        counter.position.y = 0.45 * scale;
        counter.castShadow = true;
        group.add(counter);

        // Coffee machine
        const machineGeo = new THREE.BoxGeometry(0.3 * scale, 0.4 * scale, 0.3 * scale);
        const machineMat = new THREE.MeshStandardMaterial({ color: 0x222222 });
        const machine = new THREE.Mesh(machineGeo, machineMat);
        machine.position.set(0, 1.1 * scale, 0);
        group.add(machine);

        return group;
    }

    /**
     * Default fallback object (sphere).
     */
    _createDefaultObject(color, scale, obj) {
        const geo = new THREE.SphereGeometry(0.5 * scale, 8, 8);
        const mat = this._createMaterialWithTexture(color || 0xCCCCCC, obj);
        const mesh = new THREE.Mesh(geo, mat);
        mesh.position.y = 0.5 * scale;
        mesh.castShadow = true;
        return mesh;
    }

    /**
     * Parse a hex color string to a number.
     */
    _parseColor(hex) {
        if (!hex) return 0xCCCCCC;
        if (typeof hex === 'number') return hex;
        hex = hex.replace('#', '');
        if (hex.length === 3) {
            hex = hex[0] + hex[0] + hex[1] + hex[1] + hex[2] + hex[2];
        }
        const parsed = parseInt(hex, 16);
        return isNaN(parsed) ? 0xCCCCCC : parsed;
    }

    /**
     * Update animated objects (screen flicker, lamp pulse).
     */
    update(deltaTime) {
        const time = performance.now() / 1000;

        for (const anim of this.animatedObjects) {
            if (anim.type === 'terminal') {
                // Screen flicker
                const screen = anim.mesh.getObjectByName('TerminalScreen');
                if (screen && screen.material) {
                    screen.material.emissiveIntensity = 0.4 + Math.sin(time * 3) * 0.1;
                }
            } else if (anim.type === 'street_lamp') {
                // Lamp pulse
                anim.mesh.children.forEach(child => {
                    if (child.isPointLight) {
                        child.intensity = 1.8 + Math.sin(time * 2) * 0.2;
                    }
                });
            }
        }
    }

    /**
     * Dispose all created objects and clean up resources.
     */
    dispose() {
        // Remove individual objects
        for (const obj of this.objects) {
            this.mapGroup.remove(obj);
            if (obj.geometry) obj.geometry.dispose();
            if (obj.material) {
                if (Array.isArray(obj.material)) {
                    obj.material.forEach(m => m.dispose());
                } else {
                    obj.material.dispose();
                }
            }
            if (obj.children) {
                obj.children.forEach(child => {
                    if (child.geometry) child.geometry.dispose();
                    if (child.material) {
                        if (Array.isArray(child.material)) {
                            child.material.forEach(m => m.dispose());
                        } else {
                            child.material.dispose();
                        }
                    }
                });
            }
        }

        // Remove instanced meshes
        for (const mesh of this.instancedMeshes) {
            this.mapGroup.remove(mesh);
            if (mesh.geometry) mesh.geometry.dispose();
            if (mesh.material) mesh.material.dispose();
        }

        // Remove lights
        for (const light of this.lights) {
            this.mapGroup.remove(light);
        }

        this.objects = [];
        this.instancedMeshes = [];
        this.lights = [];
        this.animatedObjects = [];
    }
}

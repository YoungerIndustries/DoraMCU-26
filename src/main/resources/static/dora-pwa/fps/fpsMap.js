/**
 * Procedural map rendering using Three.js.
 * Creates floors, walls, and obstacles from server data.
 */
class FpsMap {
    constructor(scene) {
        this.scene = scene;
        this.spawnPoints = [];
        this.collisionBoxes = [];
        this.mapGroup = new THREE.Group();
        this.mapGroup.name = 'Map';
        this.scene.add(this.mapGroup);
    }
    
    generateDefault() {
        this.clear();
        
        const width = 100;
        const depth = 100;
        const height = 10;
        
        // Floor
        const floorGeo = new THREE.PlaneGeometry(width, depth);
        const floorMat = new THREE.MeshStandardMaterial({ 
            color: 0x555555,
            roughness: 0.8 
        });
        const floor = new THREE.Mesh(floorGeo, floorMat);
        floor.rotation.x = -Math.PI / 2;
        floor.position.y = 0;
        floor.receiveShadow = true;
        this.mapGroup.add(floor);
        
        // Walls
        const wallMat = new THREE.MeshStandardMaterial({ color: 0x888888 });
        const wallThickness = 1;
        
        // North wall
        const northWall = new THREE.Mesh(
            new THREE.BoxGeometry(width, height, wallThickness),
            wallMat
        );
        northWall.position.set(0, height / 2, -depth / 2);
        this.mapGroup.add(northWall);
        
        // South wall
        const southWall = new THREE.Mesh(
            new THREE.BoxGeometry(width, height, wallThickness),
            wallMat
        );
        southWall.position.set(0, height / 2, depth / 2);
        this.mapGroup.add(southWall);
        
        // West wall
        const westWall = new THREE.Mesh(
            new THREE.BoxGeometry(wallThickness, height, depth),
            wallMat
        );
        westWall.position.set(-width / 2, height / 2, 0);
        this.mapGroup.add(westWall);
        
        // East wall
        const eastWall = new THREE.Mesh(
            new THREE.BoxGeometry(wallThickness, height, depth),
            wallMat
        );
        eastWall.position.set(width / 2, height / 2, 0);
        this.mapGroup.add(eastWall);
        
        // Obstacles
        const obstacleMat = new THREE.MeshStandardMaterial({ color: 0x666666 });
        
        const obstacles = [
            { pos: [0, 2.5, 0], size: [10, 5, 10] },
            { pos: [-25, 1.5, -25], size: [6, 3, 6] },
            { pos: [25, 1.5, 25], size: [6, 3, 6] },
            { pos: [-25, 1.5, 25], size: [6, 3, 6] },
            { pos: [25, 1.5, -25], size: [6, 3, 6] }
        ];
        
        for (const obs of obstacles) {
            const geo = new THREE.BoxGeometry(...obs.size);
            const mesh = new THREE.Mesh(geo, obstacleMat);
            mesh.position.set(...obs.pos);
            mesh.castShadow = true;
            mesh.receiveShadow = true;
            this.mapGroup.add(mesh);
            
            this.collisionBoxes.push({
                min: [obs.pos[0] - obs.size[0]/2, obs.pos[1] - obs.size[1]/2, obs.pos[2] - obs.size[2]/2],
                max: [obs.pos[0] + obs.size[0]/2, obs.pos[1] + obs.size[1]/2, obs.pos[2] + obs.size[2]/2]
            });
        }
        
        // Spawn points (4 corners)
        const halfW = width / 2 - 5;
        const halfD = depth / 2 - 5;
        this.spawnPoints = [
            { x: -halfW, y: 1.7, z: -halfD },
            { x: halfW, y: 1.7, z: -halfD },
            { x: -halfW, y: 1.7, z: halfD },
            { x: halfW, y: 1.7, z: halfD }
        ];
        
        // Visual spawn markers
        const markerGeo = new THREE.SphereGeometry(0.5, 8, 8);
        const markerMat = new THREE.MeshBasicMaterial({ color: 0xffff00 });
        for (const sp of this.spawnPoints) {
            const marker = new THREE.Mesh(markerGeo, markerMat);
            marker.position.set(sp.x, sp.y, sp.z);
            this.mapGroup.add(marker);
        }
        
        // Grid helper
        const grid = new THREE.GridHelper(Math.max(width, depth), 20, 0x444444, 0x333333);
        this.mapGroup.add(grid);
        
        console.log(`[FpsMap] Generated default map: ${this.spawnPoints.length} spawn points, ${this.collisionBoxes.length} obstacles`);
    }
    
    generateFromData(mapData) {
        this.clear();
        
        const width = mapData.mapWidth || 100;
        const depth = mapData.mapDepth || 100;
        const height = mapData.mapHeight || 20;
        
        // Floor
        const floorGeo = new THREE.PlaneGeometry(width, depth);
        const floorMat = new THREE.MeshStandardMaterial({ color: 0x555555 });
        const floor = new THREE.Mesh(floorGeo, floorMat);
        floor.rotation.x = -Math.PI / 2;
        floor.receiveShadow = true;
        this.mapGroup.add(floor);
        
        // Walls
        const wallMat = new THREE.MeshStandardMaterial({ color: 0x888888 });
        const wallThickness = 1;
        const halfW = width / 2;
        const halfD = depth / 2;
        
        this.mapGroup.add(this.createWall(0, height/2, -halfD, width, height, wallThickness, wallMat));
        this.mapGroup.add(this.createWall(0, height/2, halfD, width, height, wallThickness, wallMat));
        this.mapGroup.add(this.createWall(-halfW, height/2, 0, wallThickness, height, depth, wallMat));
        this.mapGroup.add(this.createWall(halfW, height/2, 0, wallThickness, height, depth, wallMat));
        
        // Spawn points
        if (mapData.spawnPoints) {
            this.spawnPoints = mapData.spawnPoints;
            
            const markerGeo = new THREE.SphereGeometry(0.5, 8, 8);
            const markerMat = new THREE.MeshBasicMaterial({ color: 0xffff00 });
            for (const sp of this.spawnPoints) {
                const marker = new THREE.Mesh(markerGeo, markerMat);
                marker.position.set(sp.x, sp.y, sp.z);
                this.mapGroup.add(marker);
            }
        }
        
        // Collision boxes
        if (mapData.collisionBoxes) {
            const obstacleMat = new THREE.MeshStandardMaterial({ color: 0x666666 });
            for (const cb of mapData.collisionBoxes) {
                const center = [
                    (cb.minX + cb.maxX) / 2,
                    (cb.minY + cb.maxY) / 2,
                    (cb.minZ + cb.maxZ) / 2
                ];
                const size = [
                    cb.maxX - cb.minX,
                    cb.maxY - cb.minY,
                    cb.maxZ - cb.minZ
                ];
                
                const geo = new THREE.BoxGeometry(...size);
                const mesh = new THREE.Mesh(geo, obstacleMat);
                mesh.position.set(...center);
                mesh.castShadow = true;
                this.mapGroup.add(mesh);
                
                this.collisionBoxes.push({ min: [cb.minX, cb.minY, cb.minZ], max: [cb.maxX, cb.maxY, cb.maxZ] });
            }
        }
    }
    
    createWall(x, y, z, w, h, d, material) {
        const geo = new THREE.BoxGeometry(w, h, d);
        const mesh = new THREE.Mesh(geo, material);
        mesh.position.set(x, y, z);
        return mesh;
    }
    
    getRandomSpawnPoint() {
        if (this.spawnPoints.length === 0) return { x: 0, y: 1.7, z: 0 };
        return this.spawnPoints[Math.floor(Math.random() * this.spawnPoints.length)];
    }
    
    getSpawnPointForTeam(team) {
        if (this.spawnPoints.length === 0) return { x: 0, y: 1.7, z: 0 };
        const index = team * Math.floor(this.spawnPoints.length / 2);
        return this.spawnPoints[Math.min(index, this.spawnPoints.length - 1)];
    }
    
    clear() {
        while (this.mapGroup.children.length > 0) {
            const child = this.mapGroup.children[0];
            this.mapGroup.remove(child);
            if (child.geometry) child.geometry.dispose();
            if (child.material) child.material.dispose();
        }
        this.spawnPoints = [];
        this.collisionBoxes = [];
    }
}

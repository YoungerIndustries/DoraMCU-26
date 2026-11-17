/**
 * FPS Scene Decorator — Adds environmental decoration to the FPS open world.
 *
 * Adapts patterns from the Electron HUD 3d-scene-renderer.js for the FPS PWA client.
 * Decorates the 3D scene with sky, terrain, trees, buildings, props, lighting,
 * particles, capture point markers, and power-up indicators.
 *
 * Usage:
 *   const decorator = new FpsSceneDecorator(scene, mapGroup);
 *   decorator.decorate();
 */
class FpsSceneDecorator {
    constructor(scene, mapGroup) {
        this.scene = scene;
        this.mapGroup = mapGroup;
        this.decorations = new THREE.Group();
        this.decorations.name = 'Decorations';
        this.scene.add(this.decorations);
        this.lights = [];
        this.animatedObjects = [];
    }

    /**
     * Apply all decorations to the scene.
     */
    decorate() {
        this._setupSky();
        this._setupTerrain();
        this._addTrees();
        this._addBuildings();
        this._addStreetLamps();
        this._addRocks();
        this._addCratesAndBarrels();
        this._addParticles();
        this._addMoon();
        this._addCapturePointMarkers();
        this._addPowerUpIndicators();
        console.log('[FpsSceneDecorator] Scene decorated');
    }

    /**
     * Replace solid blue background with a gradient sky dome.
     */
    _setupSky() {
        // Sky dome
        const skyGeo = new THREE.SphereGeometry(400, 32, 32);
        const skyMat = new THREE.ShaderMaterial({
            uniforms: {
                topColor: { value: new THREE.Color(0x0077ff) },
                bottomColor: { value: new THREE.Color(0x87CEEB) },
                offset: { value: 20 },
                exponent: { value: 0.6 }
            },
            vertexShader: `
                varying vec3 vWorldPosition;
                void main() {
                    vec4 worldPosition = modelMatrix * vec4(position, 1.0);
                    vWorldPosition = worldPosition.xyz;
                    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
                }
            `,
            fragmentShader: `
                uniform vec3 topColor;
                uniform vec3 bottomColor;
                uniform float offset;
                uniform float exponent;
                varying vec3 vWorldPosition;
                void main() {
                    float h = normalize(vWorldPosition + offset).y;
                    gl_FragColor = vec4(mix(bottomColor, topColor, max(pow(max(h, 0.0), exponent), 0.0)), 1.0);
                }
            `,
            side: THREE.BackSide
        });
        const sky = new THREE.Mesh(skyGeo, skyMat);
        this.decorations.add(sky);

        // Sun
        const sunGeo = new THREE.SphereGeometry(8, 16, 16);
        const sunMat = new THREE.MeshBasicMaterial({ color: 0xffffaa });
        const sun = new THREE.Mesh(sunGeo, sunMat);
        sun.position.set(100, 80, -150);
        this.decorations.add(sun);

        // Update fog to match sky
        this.scene.fog = new THREE.FogExp2(0x87CEEB, 0.005);
    }

    /**
     * Add terrain elevation variation over the flat floor.
     */
    _setupTerrain() {
        // Grass patches scattered across the map
        const grassColors = [0x3a5a2a, 0x4a6a3a, 0x2a4a1a, 0x5a7a4a];
        for (let i = 0; i < 30; i++) {
            const size = Math.random() * 8 + 2;
            const grassGeo = new THREE.PlaneGeometry(size, size);
            const grassMat = new THREE.MeshStandardMaterial({
                color: grassColors[Math.floor(Math.random() * grassColors.length)],
                roughness: 0.9
            });
            const grass = new THREE.Mesh(grassGeo, grassMat);
            grass.rotation.x = -Math.PI / 2;
            grass.position.set(
                (Math.random() - 0.5) * 80,
                0.02,
                (Math.random() - 0.5) * 80
            );
            grass.receiveShadow = true;
            this.decorations.add(grass);
        }

        // Dirt paths
        const dirtMat = new THREE.MeshStandardMaterial({ color: 0x8a7a5a, roughness: 0.95 });
        for (let i = 0; i < 5; i++) {
            const pathGeo = new THREE.PlaneGeometry(Math.random() * 6 + 3, Math.random() * 20 + 10);
            const path = new THREE.Mesh(pathGeo, dirtMat);
            path.rotation.x = -Math.PI / 2;
            path.rotation.z = Math.random() * Math.PI;
            path.position.set(
                (Math.random() - 0.5) * 60,
                0.01,
                (Math.random() - 0.5) * 60
            );
            path.receiveShadow = true;
            this.decorations.add(path);
        }
    }

    /**
     * Create tree models and scatter them around the map.
     * Uses simple geometry: cylinder trunk + cone foliage.
     */
    _addTrees() {
        const trunkMat = new THREE.MeshStandardMaterial({ color: 0x5a3a1a, roughness: 0.9 });
        const foliageColors = [0x2a5a1a, 0x3a6a2a, 0x1a4a0a, 0x2a6a2a];

        for (let i = 0; i < 40; i++) {
            const treeGroup = new THREE.Group();
            const height = Math.random() * 4 + 3;

            // Trunk
            const trunkGeo = new THREE.CylinderGeometry(0.2, 0.3, height, 6);
            const trunk = new THREE.Mesh(trunkGeo, trunkMat);
            trunk.position.y = height / 2;
            trunk.castShadow = true;
            treeGroup.add(trunk);

            // Foliage layers (2-3 cones)
            const foliageColor = foliageColors[Math.floor(Math.random() * foliageColors.length)];
            const foliageMat = new THREE.MeshStandardMaterial({ color: foliageColor, roughness: 0.8 });
            const layers = Math.floor(Math.random() * 2) + 2;
            for (let l = 0; l < layers; l++) {
                const coneHeight = height * (0.4 - l * 0.1);
                const coneRadius = (1.5 - l * 0.3) * (0.8 + Math.random() * 0.4);
                const coneGeo = new THREE.ConeGeometry(coneRadius, coneHeight, 6);
                const cone = new THREE.Mesh(coneGeo, foliageMat);
                cone.position.y = height + l * 1.2;
                cone.castShadow = true;
                treeGroup.add(cone);
            }

            // Position away from center spawn area
            let x, z;
            do {
                x = (Math.random() - 0.5) * 90;
                z = (Math.random() - 0.5) * 90;
            } while (Math.abs(x) < 10 && Math.abs(z) < 10);

            treeGroup.position.set(x, 0, z);
            this.decorations.add(treeGroup);
        }
    }

    /**
     * Add buildings with varied heights, colors, and window patterns.
     */
    _addBuildings() {
        const buildingColors = [0x7a6a5a, 0x6a5a4a, 0x8a7a6a, 0x5a4a3a, 0x9a8a7a, 0x6a7a6a];
        const windowMat = new THREE.MeshStandardMaterial({
            color: 0x88ccff,
            emissive: 0x224466,
            emissiveIntensity: 0.3,
            roughness: 0.2
        });

        // Place buildings in clusters around the map edges
        const buildingClusters = [
            { cx: -35, cz: -35, count: 3 },
            { cx: 35, cz: -35, count: 3 },
            { cx: -35, cz: 35, count: 3 },
            { cx: 35, cz: 35, count: 3 },
            { cx: 0, cz: -40, count: 2 },
            { cx: 0, cz: 40, count: 2 }
        ];

        for (const cluster of buildingClusters) {
            for (let i = 0; i < cluster.count; i++) {
                const height = Math.random() * 8 + 4;
                const width = Math.random() * 4 + 3;
                const depth = Math.random() * 4 + 3;
                const color = buildingColors[Math.floor(Math.random() * buildingColors.length)];

                const buildingGroup = new THREE.Group();

                // Main structure
                const buildingGeo = new THREE.BoxGeometry(width, height, depth);
                const buildingMat = new THREE.MeshStandardMaterial({ color, roughness: 0.8 });
                const building = new THREE.Mesh(buildingGeo, buildingMat);
                building.position.y = height / 2;
                building.castShadow = true;
                building.receiveShadow = true;
                buildingGroup.add(building);

                // Roof
                const roofGeo = new THREE.BoxGeometry(width + 0.4, 0.3, depth + 0.4);
                const roofMat = new THREE.MeshStandardMaterial({ color: 0x4a3a2a, roughness: 0.9 });
                const roof = new THREE.Mesh(roofGeo, roofMat);
                roof.position.y = height + 0.15;
                roof.castShadow = true;
                buildingGroup.add(roof);

                // Windows on each face
                const windowRows = Math.floor(height / 2);
                const windowCols = Math.floor(width / 2);
                for (let row = 0; row < windowRows; row++) {
                    for (let col = 0; col < windowCols; col++) {
                        const winGeo = new THREE.PlaneGeometry(0.6, 0.8);

                        // Front face
                        const winFront = new THREE.Mesh(winGeo, windowMat);
                        winFront.position.set(
                            -width / 2 + 1 + col * 2,
                            1.5 + row * 2,
                            depth / 2 + 0.01
                        );
                        buildingGroup.add(winFront);

                        // Back face
                        const winBack = new THREE.Mesh(winGeo, windowMat);
                        winBack.position.set(
                            -width / 2 + 1 + col * 2,
                            1.5 + row * 2,
                            -depth / 2 - 0.01
                        );
                        winBack.rotation.y = Math.PI;
                        buildingGroup.add(winBack);
                    }
                }

                // Door
                const doorGeo = new THREE.PlaneGeometry(1, 2);
                const doorMat = new THREE.MeshStandardMaterial({ color: 0x3a2a1a });
                const door = new THREE.Mesh(doorGeo, doorMat);
                door.position.set(0, 1, depth / 2 + 0.02);
                buildingGroup.add(door);

                // Position
                const x = cluster.cx + (Math.random() - 0.5) * 15;
                const z = cluster.cz + (Math.random() - 0.5) * 15;
                buildingGroup.position.set(x, 0, z);
                buildingGroup.rotation.y = Math.random() * Math.PI;
                this.decorations.add(buildingGroup);
            }
        }
    }

    /**
     * Add street lamps with point lights.
     */
    _addStreetLamps() {
        const poleMat = new THREE.MeshStandardMaterial({ color: 0x3a3a3a, roughness: 0.7 });
        const lampHeadMat = new THREE.MeshStandardMaterial({
            color: 0xffdd88,
            emissive: 0xffaa44,
            emissiveIntensity: 0.5
        });

        // Place lamps along paths between building clusters
        const lampPositions = [
            { x: -20, z: -20 }, { x: -20, z: 0 }, { x: -20, z: 20 },
            { x: 20, z: -20 }, { x: 20, z: 0 }, { x: 20, z: 20 },
            { x: 0, z: -20 }, { x: 0, z: 20 },
            { x: -40, z: 0 }, { x: 40, z: 0 },
            { x: 0, z: -40 }, { x: 0, z: 40 }
        ];

        for (const pos of lampPositions) {
            const lampGroup = new THREE.Group();

            // Pole
            const poleGeo = new THREE.CylinderGeometry(0.08, 0.1, 6, 6);
            const pole = new THREE.Mesh(poleGeo, poleMat);
            pole.position.y = 3;
            lampGroup.add(pole);

            // Arm
            const armGeo = new THREE.CylinderGeometry(0.05, 0.05, 1.5, 6);
            const arm = new THREE.Mesh(armGeo, poleMat);
            arm.position.set(0.5, 6, 0);
            arm.rotation.z = Math.PI / 2;
            lampGroup.add(arm);

            // Lamp head
            const headGeo = new THREE.SphereGeometry(0.3, 8, 8);
            const head = new THREE.Mesh(headGeo, lampHeadMat);
            head.position.set(1, 6, 0);
            lampGroup.add(head);

            // Point light
            const light = new THREE.PointLight(0xffaa44, 0.8, 15);
            light.position.set(1, 5.8, 0);
            lampGroup.add(light);
            this.lights.push(light);

            lampGroup.position.set(pos.x, 0, pos.z);
            this.decorations.add(lampGroup);
        }
    }

    /**
     * Scatter rocks of various sizes.
     */
    _addRocks() {
        const rockMat = new THREE.MeshStandardMaterial({ color: 0x6a6a6a, roughness: 0.9 });
        const rockColors = [0x6a6a6a, 0x7a7a7a, 0x5a5a5a, 0x8a8a7a];

        for (let i = 0; i < 25; i++) {
            const size = Math.random() * 1.2 + 0.3;
            const rockGeo = new THREE.DodecahedronGeometry(size, 0);
            const color = rockColors[Math.floor(Math.random() * rockColors.length)];
            const mat = new THREE.MeshStandardMaterial({ color, roughness: 0.9 });
            const rock = new THREE.Mesh(rockGeo, mat);

            rock.position.set(
                (Math.random() - 0.5) * 90,
                size * 0.3,
                (Math.random() - 0.5) * 90
            );
            rock.rotation.set(
                Math.random() * Math.PI,
                Math.random() * Math.PI,
                Math.random() * Math.PI
            );
            rock.castShadow = true;
            rock.receiveShadow = true;
            this.decorations.add(rock);
        }
    }

    /**
     * Add crates and barrels as cover objects.
     */
    _addCratesAndBarrels() {
        const crateMat = new THREE.MeshStandardMaterial({ color: 0x8a6a3a, roughness: 0.85 });
        const barrelMat = new THREE.MeshStandardMaterial({ color: 0x5a4a3a, roughness: 0.8 });

        // Crates
        for (let i = 0; i < 15; i++) {
            const size = Math.random() * 1 + 0.8;
            const crateGeo = new THREE.BoxGeometry(size, size, size);
            const crate = new THREE.Mesh(crateGeo, crateMat);

            crate.position.set(
                (Math.random() - 0.5) * 70,
                size / 2,
                (Math.random() - 0.5) * 70
            );
            crate.rotation.y = Math.random() * Math.PI / 4;
            crate.castShadow = true;
            crate.receiveShadow = true;
            this.decorations.add(crate);
        }

        // Barrels
        for (let i = 0; i < 10; i++) {
            const barrelGeo = new THREE.CylinderGeometry(0.4, 0.4, 1.2, 8);
            const barrel = new THREE.Mesh(barrelGeo, barrelMat);

            barrel.position.set(
                (Math.random() - 0.5) * 70,
                0.6,
                (Math.random() - 0.5) * 70
            );
            barrel.castShadow = true;
            this.decorations.add(barrel);
        }
    }

    /**
     * Add ambient dust/pollen particle system.
     */
    _addParticles() {
        const count = 200;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        const velocities = [];

        for (let i = 0; i < count; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 100;
            positions[i * 3 + 1] = Math.random() * 20 + 1;
            positions[i * 3 + 2] = (Math.random() - 0.5) * 100;
            velocities.push({
                x: (Math.random() - 0.5) * 0.02,
                y: (Math.random() - 0.5) * 0.01,
                z: (Math.random() - 0.5) * 0.02
            });
        }

        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));

        const material = new THREE.PointsMaterial({
            size: 0.15,
            color: 0xffffee,
            transparent: true,
            opacity: 0.4
        });

        const particles = new THREE.Points(geometry, material);
        particles.userData.velocities = velocities;
        this.decorations.add(particles);
        this.animatedObjects.push(particles);
    }

    /**
     * Add a moon/planet in the sky.
     */
    _addMoon() {
        const moonGeo = new THREE.SphereGeometry(5, 32, 32);
        const moonMat = new THREE.MeshStandardMaterial({
            color: 0xccbb99,
            emissive: 0x221100,
            emissiveIntensity: 0.1,
            roughness: 0.9
        });
        const moon = new THREE.Mesh(moonGeo, moonMat);
        moon.position.set(-80, 60, -120);
        this.decorations.add(moon);

        // Atmospheric glow around moon
        const glowGeo = new THREE.SphereGeometry(6, 32, 32);
        const glowMat = new THREE.MeshBasicMaterial({
            color: 0x887766,
            transparent: true,
            opacity: 0.1
        });
        const glow = new THREE.Mesh(glowGeo, glowMat);
        glow.position.copy(moon.position);
        this.decorations.add(glow);
    }

    /**
     * Add holographic capture point markers.
     * These are placed at standard capture point locations.
     */
    _addCapturePointMarkers() {
        const capturePoints = [
            { id: 'A', x: 0, z: 0, color: 0x00ff00 },
            { id: 'B', x: -30, z: -30, color: 0x0088ff },
            { id: 'C', x: 30, z: 30, color: 0xff4444 }
        ];

        for (const cp of capturePoints) {
            const markerGroup = new THREE.Group();

            // Holographic ring
            const ringGeo = new THREE.TorusGeometry(2, 0.1, 8, 32);
            const ringMat = new THREE.MeshBasicMaterial({
                color: cp.color,
                transparent: true,
                opacity: 0.6
            });
            const ring = new THREE.Mesh(ringGeo, ringMat);
            ring.rotation.x = Math.PI / 2;
            ring.position.y = 3;
            markerGroup.add(ring);

            // Vertical beam
            const beamGeo = new THREE.CylinderGeometry(0.5, 0.5, 6, 16, 1, true);
            const beamMat = new THREE.MeshBasicMaterial({
                color: cp.color,
                transparent: true,
                opacity: 0.15,
                side: THREE.DoubleSide
            });
            const beam = new THREE.Mesh(beamGeo, beamMat);
            beam.position.y = 3;
            markerGroup.add(beam);

            // Label (canvas texture)
            const canvas = document.createElement('canvas');
            canvas.width = 128;
            canvas.height = 64;
            const ctx = canvas.getContext('2d');
            ctx.fillStyle = `#${cp.color.toString(16).padStart(6, '0')}`;
            ctx.font = 'bold 48px Arial';
            ctx.textAlign = 'center';
            ctx.fillText(`CP ${cp.id}`, 64, 48);
            const texture = new THREE.CanvasTexture(canvas);
            const labelGeo = new THREE.PlaneGeometry(3, 1.5);
            const labelMat = new THREE.MeshBasicMaterial({
                map: texture,
                transparent: true,
                side: THREE.DoubleSide
            });
            const label = new THREE.Mesh(labelGeo, labelMat);
            label.position.y = 6;
            markerGroup.add(label);

            // Point light for glow
            const light = new THREE.PointLight(cp.color, 0.5, 10);
            light.position.y = 3;
            markerGroup.add(light);
            this.lights.push(light);

            markerGroup.position.set(cp.x, 0, cp.z);
            markerGroup.userData.isCapturePoint = true;
            this.decorations.add(markerGroup);
            this.animatedObjects.push(markerGroup);
        }
    }

    /**
     * Add floating, rotating power-up indicators.
     */
    _addPowerUpIndicators() {
        const powerUps = [
            { type: 'health', x: 0, z: 0, color: 0xff0000, symbol: '+' },
            { type: 'ammo', x: -25, z: 25, color: 0xffaa00, symbol: '•' },
            { type: 'health', x: 25, z: -25, color: 0xff0000, symbol: '+' }
        ];

        for (const pu of powerUps) {
            const indicatorGroup = new THREE.Group();

            // Glowing orb
            const orbGeo = new THREE.SphereGeometry(0.5, 16, 16);
            const orbMat = new THREE.MeshBasicMaterial({
                color: pu.color,
                transparent: true,
                opacity: 0.7
            });
            const orb = new THREE.Mesh(orbGeo, orbMat);
            indicatorGroup.add(orb);

            // Outer glow
            const glowGeo = new THREE.SphereGeometry(0.8, 16, 16);
            const glowMat = new THREE.MeshBasicMaterial({
                color: pu.color,
                transparent: true,
                opacity: 0.2
            });
            const glow = new THREE.Mesh(glowGeo, glowMat);
            indicatorGroup.add(glow);

            // Symbol label
            const canvas = document.createElement('canvas');
            canvas.width = 64;
            canvas.height = 64;
            const ctx = canvas.getContext('2d');
            ctx.fillStyle = `#${pu.color.toString(16).padStart(6, '0')}`;
            ctx.font = 'bold 48px Arial';
            ctx.textAlign = 'center';
            ctx.textBaseline = 'middle';
            ctx.fillText(pu.symbol, 32, 32);
            const texture = new THREE.CanvasTexture(canvas);
            const labelGeo = new THREE.PlaneGeometry(1, 1);
            const labelMat = new THREE.MeshBasicMaterial({
                map: texture,
                transparent: true,
                side: THREE.DoubleSide
            });
            const label = new THREE.Mesh(labelGeo, labelMat);
            indicatorGroup.add(label);

            // Point light
            const light = new THREE.PointLight(pu.color, 0.4, 8);
            indicatorGroup.add(light);
            this.lights.push(light);

            indicatorGroup.position.set(pu.x, 2, pu.z);
            indicatorGroup.userData.isPowerUp = true;
            this.decorations.add(indicatorGroup);
            this.animatedObjects.push(indicatorGroup);
        }
    }

    /**
     * Update animated decorations (called each frame).
     */
    update(time) {
        // Animate particles
        for (const obj of this.animatedObjects) {
            if (obj.isPoints && obj.userData.velocities) {
                const positions = obj.geometry.attributes.position.array;
                const velocities = obj.userData.velocities;
                for (let i = 0; i < velocities.length; i++) {
                    positions[i * 3] += velocities[i].x;
                    positions[i * 3 + 1] += velocities[i].y;
                    positions[i * 3 + 2] += velocities[i].z;

                    // Wrap around
                    if (positions[i * 3] > 50) positions[i * 3] = -50;
                    if (positions[i * 3] < -50) positions[i * 3] = 50;
                    if (positions[i * 3 + 1] > 21) positions[i * 3 + 1] = 1;
                    if (positions[i * 3 + 1] < 1) positions[i * 3 + 1] = 21;
                    if (positions[i * 3 + 2] > 50) positions[i * 3 + 2] = -50;
                    if (positions[i * 3 + 2] < -50) positions[i * 3 + 2] = 50;
                }
                obj.geometry.attributes.position.needsUpdate = true;
            }

            // Rotate capture point markers and power-up indicators
            if (obj.userData.isCapturePoint) {
                obj.rotation.y = time * 0.5;
                // Pulse the beam opacity
                for (const child of obj.children) {
                    if (child.material && child.material.opacity !== undefined && child.geometry.type === 'CylinderGeometry') {
                        child.material.opacity = 0.1 + Math.sin(time * 2) * 0.05;
                    }
                }
            }

            if (obj.userData.isPowerUp) {
                obj.rotation.y = time * 1.5;
                obj.position.y = 2 + Math.sin(time * 2) * 0.5;
            }
        }
    }

    /**
     * Clean up all decorations.
     */
    dispose() {
        while (this.decorations.children.length > 0) {
            const child = this.decorations.children[0];
            this.decorations.remove(child);
            if (child.geometry) child.geometry.dispose();
            if (child.material) {
                if (Array.isArray(child.material)) {
                    child.material.forEach(m => m.dispose());
                } else {
                    child.material.dispose();
                }
            }
        }
        this.scene.remove(this.decorations);
        this.lights = [];
        this.animatedObjects = [];
    }
}

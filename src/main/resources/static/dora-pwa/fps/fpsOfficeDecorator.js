/**
 * FPS Office Interior Decorator — Younger Industries HQ office scene.
 *
 * Replaces the generic outdoor scene with a corporate office interior
 * when the map theme is "Earth2026" and the map name contains "Younger".
 *
 * Based on patterns from dora-electron/renderer/hud/3d-scene-renderer.js
 * (_buildZeroZeroSevenScene and _buildEarth2026Scene).
 *
 * Usage:
 *   const decorator = new FpsOfficeDecorator(scene, mapGroup, mapData);
 *   if (decorator.isOfficeMap()) {
 *       decorator.decorate();
 *   }
 */
class FpsOfficeDecorator {
    constructor(scene, mapGroup, mapData) {
        this.scene = scene;
        this.mapGroup = mapGroup;
        this.mapData = mapData || {};
        this.decorations = new THREE.Group();
        this.decorations.name = 'OfficeDecorations';
        this.scene.add(this.decorations);
        this.lights = [];
        this.animatedObjects = [];
    }

    /**
     * Check if the current map should use the office interior.
     */
    isOfficeMap() {
        const name = (this.mapData.name || '').toLowerCase();
        return name.includes('younger') || name.includes('hq') || name.includes('office');
    }

    /**
     * Build the Younger Industries HQ office interior.
     */
    decorate() {
        // Remove the default sky and outdoor elements if present
        this._removeOutdoorElements();

        // Office dimensions: 40x30x8 (width x depth x height)
        this._buildRoom();
        this._buildFloor();
        this._buildCeiling();
        this._buildWindows();
        this._buildDesks();
        this._buildTerminals();
        this._buildFilingCabinets();
        this._buildBookshelves();
        this._buildYoungerLogo();
        this._buildOfficeChairs();
        this._buildCoffeeStation();
        this._buildLighting();
        this._buildParticles();

        console.log('[FpsOfficeDecorator] Younger Industries HQ office decorated');
    }

    /**
     * Remove outdoor elements (sky dome, sun, moon) from the scene.
     */
    _removeOutdoorElements() {
        // Replace fog with indoor fog
        this.scene.fog = new THREE.FogExp2(0x1a1a2a, 0.02);
        this.scene.background = new THREE.Color(0x0a0a15);
    }

    /**
     * Build the four walls of the office room.
     */
    _buildRoom() {
        const wallMat = new THREE.MeshStandardMaterial({
            color: 0x2a2a3a,
            roughness: 0.8,
            metalness: 0.1
        });

        // Back wall
        const backWall = new THREE.Mesh(new THREE.BoxGeometry(40, 8, 0.5), wallMat);
        backWall.position.set(0, 4, -15);
        backWall.receiveShadow = true;
        this.decorations.add(backWall);

        // Left wall
        const leftWall = new THREE.Mesh(new THREE.BoxGeometry(0.5, 8, 30), wallMat);
        leftWall.position.set(-20, 4, 0);
        leftWall.receiveShadow = true;
        this.decorations.add(leftWall);

        // Right wall
        const rightWall = new THREE.Mesh(new THREE.BoxGeometry(0.5, 8, 30), wallMat);
        rightWall.position.set(20, 4, 0);
        rightWall.receiveShadow = true;
        this.decorations.add(rightWall);

        // Front wall (with entrance gap)
        const frontWallLeft = new THREE.Mesh(new THREE.BoxGeometry(15, 8, 0.5), wallMat);
        frontWallLeft.position.set(-12.5, 4, 15);
        frontWallLeft.receiveShadow = true;
        this.decorations.add(frontWallLeft);

        const frontWallRight = new THREE.Mesh(new THREE.BoxGeometry(15, 8, 0.5), wallMat);
        frontWallRight.position.set(12.5, 4, 15);
        frontWallRight.receiveShadow = true;
        this.decorations.add(frontWallRight);

        const frontWallTop = new THREE.Mesh(new THREE.BoxGeometry(10, 3, 0.5), wallMat);
        frontWallTop.position.set(0, 6.5, 15);
        frontWallTop.receiveShadow = true;
        this.decorations.add(frontWallTop);

        // Wall panels (decorative strips)
        const panelMat = new THREE.MeshStandardMaterial({
            color: 0x1a1a2a,
            roughness: 0.6,
            metalness: 0.3
        });
        for (let i = -3; i <= 3; i++) {
            const panel = new THREE.Mesh(new THREE.BoxGeometry(0.1, 6, 28), panelMat);
            panel.position.set(i * 5.5, 3.5, 0);
            this.decorations.add(panel);
        }
    }

    /**
     * Build the office floor with carpet pattern.
     */
    _buildFloor() {
        // Main floor
        const floorGeo = new THREE.PlaneGeometry(40, 30);
        const floorMat = new THREE.MeshStandardMaterial({
            color: 0x3a3a4a,
            roughness: 0.9
        });
        const floor = new THREE.Mesh(floorGeo, floorMat);
        floor.rotation.x = -Math.PI / 2;
        floor.receiveShadow = true;
        this.decorations.add(floor);

        // Carpet area in center
        const carpetGeo = new THREE.PlaneGeometry(20, 15);
        const carpetMat = new THREE.MeshStandardMaterial({
            color: 0x2a3a5a,
            roughness: 0.95
        });
        const carpet = new THREE.Mesh(carpetGeo, carpetMat);
        carpet.rotation.x = -Math.PI / 2;
        carpet.position.set(0, 0.01, 0);
        carpet.receiveShadow = true;
        this.decorations.add(carpet);

        // Floor tiles (subtle grid pattern)
        const tileMat = new THREE.MeshStandardMaterial({
            color: 0x4a4a5a,
            roughness: 0.7,
            metalness: 0.1
        });
        for (let x = -18; x <= 18; x += 4) {
            for (let z = -13; z <= 13; z += 4) {
                const tile = new THREE.Mesh(new THREE.PlaneGeometry(3.8, 3.8), tileMat);
                tile.rotation.x = -Math.PI / 2;
                tile.position.set(x, 0.005, z);
                tile.receiveShadow = true;
                this.decorations.add(tile);
            }
        }
    }

    /**
     * Build the ceiling with light panels.
     */
    _buildCeiling() {
        const ceilingGeo = new THREE.PlaneGeometry(40, 30);
        const ceilingMat = new THREE.MeshStandardMaterial({
            color: 0x1a1a2a,
            roughness: 0.9
        });
        const ceiling = new THREE.Mesh(ceilingGeo, ceilingMat);
        ceiling.rotation.x = Math.PI / 2;
        ceiling.position.y = 8;
        this.decorations.add(ceiling);

        // Ceiling light panels
        const panelLightMat = new THREE.MeshStandardMaterial({
            color: 0xffffff,
            emissive: 0xffffff,
            emissiveIntensity: 0.5,
            roughness: 0.1
        });
        for (let x = -15; x <= 15; x += 10) {
            for (let z = -10; z <= 10; z += 10) {
                const panel = new THREE.Mesh(new THREE.PlaneGeometry(3, 1.5), panelLightMat);
                panel.rotation.x = Math.PI / 2;
                panel.position.set(x, 7.95, z);
                this.decorations.add(panel);
            }
        }
    }

    /**
     * Build windows on the back wall showing city view.
     */
    _buildWindows() {
        const windowFrameMat = new THREE.MeshStandardMaterial({
            color: 0x1a1a1a,
            roughness: 0.3,
            metalness: 0.8
        });
        const glassMat = new THREE.MeshStandardMaterial({
            color: 0x88aacc,
            transparent: true,
            opacity: 0.3,
            roughness: 0.1,
            metalness: 0.5
        });

        // Three large windows on the back wall
        for (let i = -1; i <= 1; i++) {
            const windowGroup = new THREE.Group();

            // Window glass
            const glass = new THREE.Mesh(new THREE.PlaneGeometry(6, 5), glassMat);
            glass.position.set(i * 10, 4.5, -14.7);
            windowGroup.add(glass);

            // Window frame
            const frameTop = new THREE.Mesh(new THREE.BoxGeometry(6.4, 0.2, 0.3), windowFrameMat);
            frameTop.position.set(i * 10, 7.1, -14.7);
            windowGroup.add(frameTop);

            const frameBottom = new THREE.Mesh(new THREE.BoxGeometry(6.4, 0.2, 0.3), windowFrameMat);
            frameBottom.position.set(i * 10, 2.0, -14.7);
            windowGroup.add(frameBottom);

            const frameLeft = new THREE.Mesh(new THREE.BoxGeometry(0.2, 5.3, 0.3), windowFrameMat);
            frameLeft.position.set(i * 10 - 3.1, 4.5, -14.7);
            windowGroup.add(frameLeft);

            const frameRight = new THREE.Mesh(new THREE.BoxGeometry(0.2, 5.3, 0.3), windowFrameMat);
            frameRight.position.set(i * 10 + 3.1, 4.5, -14.7);
            windowGroup.add(frameRight);

            this.decorations.add(windowGroup);
        }

        // City skyline visible through windows (simple building silhouettes)
        const cityMat = new THREE.MeshStandardMaterial({
            color: 0x1a2a3a,
            emissive: 0x0a1a2a,
            emissiveIntensity: 0.2
        });
        for (let i = 0; i < 12; i++) {
            const height = Math.random() * 15 + 5;
            const width = Math.random() * 3 + 1;
            const building = new THREE.Mesh(new THREE.BoxGeometry(width, height, 1), cityMat);
            building.position.set(
                -18 + i * 3.5 + Math.random() * 2,
                height / 2 - 2,
                -25 - Math.random() * 10
            );
            this.decorations.add(building);
        }
    }

    /**
     * Build office desks arranged in rows.
     */
    _buildDesks() {
        const deskMat = new THREE.MeshStandardMaterial({
            color: 0x4a3a2a,
            roughness: 0.6,
            metalness: 0.1
        });
        const legMat = new THREE.MeshStandardMaterial({
            color: 0x2a2a2a,
            roughness: 0.4,
            metalness: 0.6
        });

        // Desk rows
        const deskPositions = [
            { x: -10, z: -8, rotation: 0 },
            { x: 0, z: -8, rotation: 0 },
            { x: 10, z: -8, rotation: 0 },
            { x: -10, z: 2, rotation: 0 },
            { x: 0, z: 2, rotation: 0 },
            { x: 10, z: 2, rotation: 0 },
        ];

        for (const pos of deskPositions) {
            const deskGroup = new THREE.Group();

            // Desk top
            const deskTop = new THREE.Mesh(new THREE.BoxGeometry(4, 0.15, 2), deskMat);
            deskTop.position.y = 1.5;
            deskTop.castShadow = true;
            deskTop.receiveShadow = true;
            deskGroup.add(deskTop);

            // Desk legs
            const legPositions = [
                { x: -1.8, z: -0.8 },
                { x: 1.8, z: -0.8 },
                { x: -1.8, z: 0.8 },
                { x: 1.8, z: 0.8 }
            ];
            for (const leg of legPositions) {
                const legMesh = new THREE.Mesh(new THREE.CylinderGeometry(0.04, 0.04, 1.5, 6), legMat);
                legMesh.position.set(leg.x, 0.75, leg.z);
                deskGroup.add(legMesh);
            }

            // Desk organizer
            const organizer = new THREE.Mesh(
                new THREE.BoxGeometry(0.6, 0.4, 0.3),
                new THREE.MeshStandardMaterial({ color: 0x3a3a3a, roughness: 0.5 })
            );
            organizer.position.set(1.5, 1.73, -0.5);
            deskGroup.add(organizer);

            deskGroup.position.set(pos.x, 0, pos.z);
            deskGroup.rotation.y = pos.rotation;
            this.decorations.add(deskGroup);
        }
    }

    /**
     * Build computer terminals on desks.
     */
    _buildTerminals() {
        const monitorMat = new THREE.MeshStandardMaterial({
            color: 0x1a1a1a,
            roughness: 0.3,
            metalness: 0.5
        });
        const screenMat = new THREE.MeshStandardMaterial({
            color: 0x00aaff,
            emissive: 0x0066cc,
            emissiveIntensity: 0.6,
            roughness: 0.1
        });

        const terminalPositions = [
            { x: -10, z: -8 },
            { x: 0, z: -8 },
            { x: 10, z: -8 },
            { x: -10, z: 2 },
            { x: 0, z: 2 },
            { x: 10, z: 2 },
        ];

        for (const pos of terminalPositions) {
            const terminalGroup = new THREE.Group();

            // Monitor stand
            const stand = new THREE.Mesh(
                new THREE.CylinderGeometry(0.05, 0.08, 0.3, 8),
                monitorMat
            );
            stand.position.y = 1.73;
            terminalGroup.add(stand);

            // Monitor screen
            const screen = new THREE.Mesh(new THREE.BoxGeometry(1.2, 0.75, 0.05), monitorMat);
            screen.position.y = 2.3;
            terminalGroup.add(screen);

            // Screen display
            const display = new THREE.Mesh(new THREE.PlaneGeometry(1.1, 0.65), screenMat);
            display.position.set(0, 2.3, 0.03);
            terminalGroup.add(display);

            // Screen glow light
            const screenLight = new THREE.PointLight(0x0066cc, 0.3, 3);
            screenLight.position.set(0, 2.3, 0.5);
            terminalGroup.add(screenLight);
            this.lights.push(screenLight);

            // Keyboard
            const keyboard = new THREE.Mesh(
                new THREE.BoxGeometry(0.8, 0.03, 0.3),
                new THREE.MeshStandardMaterial({ color: 0x2a2a2a, roughness: 0.5 })
            );
            keyboard.position.set(0, 1.58, 0.3);
            terminalGroup.add(keyboard);

            terminalGroup.position.set(pos.x, 0, pos.z + 0.5);
            this.decorations.add(terminalGroup);
        }
    }

    /**
     * Build filing cabinets along the walls.
     */
    _buildFilingCabinets() {
        const cabinetMat = new THREE.MeshStandardMaterial({
            color: 0x5a5a6a,
            roughness: 0.4,
            metalness: 0.6
        });
        const handleMat = new THREE.MeshStandardMaterial({
            color: 0x8a8a8a,
            roughness: 0.3,
            metalness: 0.8
        });

        // Along the left wall
        for (let i = 0; i < 4; i++) {
            const cabinetGroup = new THREE.Group();

            // Cabinet body
            const body = new THREE.Mesh(new THREE.BoxGeometry(0.8, 2.5, 0.6), cabinetMat);
            body.position.y = 1.25;
            body.castShadow = true;
            cabinetGroup.add(body);

            // Drawer handles
            for (let d = 0; d < 4; d++) {
                const handle = new THREE.Mesh(
                    new THREE.BoxGeometry(0.3, 0.05, 0.05),
                    handleMat
                );
                handle.position.set(0, 0.5 + d * 0.55, 0.32);
                cabinetGroup.add(handle);
            }

            cabinetGroup.position.set(-19, 0, -10 + i * 6);
            this.decorations.add(cabinetGroup);
        }
    }

    /**
     * Build bookshelves along the right wall.
     */
    _buildBookshelves() {
        const shelfMat = new THREE.MeshStandardMaterial({
            color: 0x3a2a1a,
            roughness: 0.7
        });
        const bookColors = [0x8a2a2a, 0x2a8a2a, 0x2a2a8a, 0x8a8a2a, 0x8a2a8a, 0x2a8a8a, 0x4a4a4a];

        // Two bookshelves
        for (let s = 0; s < 2; s++) {
            const shelfGroup = new THREE.Group();

            // Shelf frame
            const frame = new THREE.Mesh(new THREE.BoxGeometry(0.5, 4, 3), shelfMat);
            frame.position.y = 2;
            frame.castShadow = true;
            shelfGroup.add(frame);

            // Shelf boards
            for (let b = 0; b < 5; b++) {
                const board = new THREE.Mesh(
                    new THREE.BoxGeometry(0.45, 0.05, 2.9),
                    new THREE.MeshStandardMaterial({ color: 0x4a3a2a, roughness: 0.6 })
                );
                board.position.set(0, 0.5 + b * 0.8, 0);
                shelfGroup.add(board);

                // Books on each shelf
                for (let bk = 0; bk < 6; bk++) {
                    const bookHeight = 0.4 + Math.random() * 0.3;
                    const bookWidth = 0.1 + Math.random() * 0.1;
                    const bookColor = bookColors[Math.floor(Math.random() * bookColors.length)];
                    const book = new THREE.Mesh(
                        new THREE.BoxGeometry(bookWidth, bookHeight, 0.3),
                        new THREE.MeshStandardMaterial({ color: bookColor, roughness: 0.8 })
                    );
                    book.position.set(0, 0.55 + b * 0.8 + bookHeight / 2, -1 + bk * 0.4);
                    shelfGroup.add(book);
                }
            }

            shelfGroup.position.set(19.25, 0, -5 + s * 10);
            this.decorations.add(shelfGroup);
        }
    }

    /**
     * Build the Younger Industries logo on the back wall.
     */
    _buildYoungerLogo() {
        // Logo background panel
        const logoPanel = new THREE.Mesh(
            new THREE.BoxGeometry(8, 2, 0.1),
            new THREE.MeshStandardMaterial({
                color: 0x1a1a2a,
                roughness: 0.3,
                metalness: 0.7
            })
        );
        logoPanel.position.set(0, 6, -14.6);
        this.decorations.add(logoPanel);

        // Logo text using canvas texture
        const canvas = document.createElement('canvas');
        canvas.width = 512;
        canvas.height = 128;
        const ctx = canvas.getContext('2d');

        // Background
        ctx.fillStyle = '#1a1a2a';
        ctx.fillRect(0, 0, 512, 128);

        // "YI" logo
        ctx.fillStyle = '#00aaff';
        ctx.font = 'bold 80px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText('YI', 120, 64);

        // "Younger Industries" text
        ctx.fillStyle = '#aabbcc';
        ctx.font = '28px Arial';
        ctx.fillText('YOUNGER INDUSTRIES', 310, 55);

        // Subtitle
        ctx.fillStyle = '#667788';
        ctx.font = '16px Arial';
        ctx.fillText('SAN DIEGO HEADQUARTERS', 310, 85);

        const texture = new THREE.CanvasTexture(canvas);
        const logoMesh = new THREE.Mesh(
            new THREE.PlaneGeometry(8, 2),
            new THREE.MeshStandardMaterial({
                map: texture,
                transparent: true,
                roughness: 0.3,
                metalness: 0.5
            })
        );
        logoMesh.position.set(0, 6, -14.54);
        this.decorations.add(logoMesh);

        // Logo glow light
        const logoLight = new THREE.PointLight(0x00aaff, 0.5, 8);
        logoLight.position.set(0, 6, -13);
        this.decorations.add(logoLight);
        this.lights.push(logoLight);
    }

    /**
     * Build office chairs at desks.
     */
    _buildOfficeChairs() {
        const chairMat = new THREE.MeshStandardMaterial({
            color: 0x2a2a3a,
            roughness: 0.6
        });
        const wheelMat = new THREE.MeshStandardMaterial({
            color: 0x1a1a1a,
            roughness: 0.4,
            metalness: 0.6
        });

        const chairPositions = [
            { x: -10, z: -6 },
            { x: 0, z: -6 },
            { x: 10, z: -6 },
            { x: -10, z: 4 },
            { x: 0, z: 4 },
            { x: 10, z: 4 },
        ];

        for (const pos of chairPositions) {
            const chairGroup = new THREE.Group();

            // Seat
            const seat = new THREE.Mesh(new THREE.BoxGeometry(0.8, 0.1, 0.8), chairMat);
            seat.position.y = 0.9;
            chairGroup.add(seat);

            // Backrest
            const backrest = new THREE.Mesh(new THREE.BoxGeometry(0.8, 1, 0.1), chairMat);
            backrest.position.set(0, 1.4, 0.35);
            chairGroup.add(backrest);

            // Central pole
            const pole = new THREE.Mesh(
                new THREE.CylinderGeometry(0.04, 0.04, 0.9, 8),
                wheelMat
            );
            pole.position.y = 0.45;
            chairGroup.add(pole);

            // Base
            const base = new THREE.Mesh(
                new THREE.CylinderGeometry(0.4, 0.4, 0.05, 8),
                wheelMat
            );
            base.position.y = 0.025;
            chairGroup.add(base);

            // Wheels
            for (let w = 0; w < 5; w++) {
                const angle = (w / 5) * Math.PI * 2;
                const wheel = new THREE.Mesh(
                    new THREE.SphereGeometry(0.05, 6, 6),
                    wheelMat
                );
                wheel.position.set(
                    Math.cos(angle) * 0.35,
                    0.05,
                    Math.sin(angle) * 0.35
                );
                chairGroup.add(wheel);
            }

            chairGroup.position.set(pos.x, 0, pos.z);
            this.decorations.add(chairGroup);
        }
    }

    /**
     * Build a coffee/break station in the corner.
     */
    _buildCoffeeStation() {
        const stationGroup = new THREE.Group();

        // Counter
        const counter = new THREE.Mesh(
            new THREE.BoxGeometry(3, 1.2, 1),
            new THREE.MeshStandardMaterial({ color: 0x3a3a4a, roughness: 0.5 })
        );
        counter.position.y = 0.6;
        counter.castShadow = true;
        stationGroup.add(counter);

        // Coffee machine
        const coffeeMachine = new THREE.Mesh(
            new THREE.BoxGeometry(0.5, 0.8, 0.4),
            new THREE.MeshStandardMaterial({ color: 0x1a1a1a, roughness: 0.3, metalness: 0.7 })
        );
        coffeeMachine.position.set(-0.5, 1.6, 0);
        stationGroup.add(coffeeMachine);

        // Coffee machine display
        const display = new THREE.Mesh(
            new THREE.PlaneGeometry(0.2, 0.15),
            new THREE.MeshStandardMaterial({
                color: 0xff4400,
                emissive: 0xff4400,
                emissiveIntensity: 0.5
            })
        );
        display.position.set(-0.5, 1.8, 0.21);
        stationGroup.add(display);

        // Mugs on counter
        const mugMat = new THREE.MeshStandardMaterial({ color: 0xffffff, roughness: 0.4 });
        for (let m = 0; m < 3; m++) {
            const mug = new THREE.Mesh(
                new THREE.CylinderGeometry(0.08, 0.07, 0.15, 8),
                mugMat
            );
            mug.position.set(0.3 + m * 0.25, 1.28, 0);
            stationGroup.add(mug);
        }

        // Mini fridge
        const fridge = new THREE.Mesh(
            new THREE.BoxGeometry(0.8, 1.5, 0.7),
            new THREE.MeshStandardMaterial({ color: 0x4a4a5a, roughness: 0.4, metalness: 0.5 })
        );
        fridge.position.set(1.2, 0.75, 0);
        fridge.castShadow = true;
        stationGroup.add(fridge);

        stationGroup.position.set(17, 0, -12);
        this.decorations.add(stationGroup);
    }

    /**
     * Build the office lighting system.
     */
    _buildLighting() {
        // Main overhead lights
        const overheadPositions = [
            { x: -10, z: -8 },
            { x: 0, z: -8 },
            { x: 10, z: -8 },
            { x: -10, z: 2 },
            { x: 0, z: 2 },
            { x: 10, z: 2 },
            { x: 0, z: 10 }
        ];

        for (const pos of overheadPositions) {
            const light = new THREE.PointLight(0xffeedd, 0.6, 12);
            light.position.set(pos.x, 7.5, pos.z);
            this.decorations.add(light);
            this.lights.push(light);
        }

        // Ambient fill light
        const ambient = new THREE.PointLight(0x4466aa, 0.3, 30);
        ambient.position.set(0, 6, 0);
        this.decorations.add(ambient);
        this.lights.push(ambient);

        // Entrance light
        const entranceLight = new THREE.PointLight(0xffaa44, 0.5, 8);
        entranceLight.position.set(0, 7, 14);
        this.decorations.add(entranceLight);
        this.lights.push(entranceLight);
    }

    /**
     * Add subtle dust particles floating in the office.
     */
    _buildParticles() {
        const count = 100;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(count * 3);
        const velocities = [];

        for (let i = 0; i < count; i++) {
            positions[i * 3] = (Math.random() - 0.5) * 38;
            positions[i * 3 + 1] = Math.random() * 7 + 0.5;
            positions[i * 3 + 2] = (Math.random() - 0.5) * 28;
            velocities.push({
                x: (Math.random() - 0.5) * 0.005,
                y: (Math.random() - 0.5) * 0.003,
                z: (Math.random() - 0.5) * 0.005
            });
        }

        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));

        const material = new THREE.PointsMaterial({
            size: 0.05,
            color: 0xffffff,
            transparent: true,
            opacity: 0.2
        });

        const particles = new THREE.Points(geometry, material);
        particles.userData.velocities = velocities;
        this.decorations.add(particles);
        this.animatedObjects.push(particles);
    }

    /**
     * Update animated elements (particles, screen flicker).
     */
    update(time) {
        // Animate dust particles
        for (const obj of this.animatedObjects) {
            if (obj.isPoints && obj.userData.velocities) {
                const positions = obj.geometry.attributes.position.array;
                const velocities = obj.userData.velocities;
                for (let i = 0; i < velocities.length; i++) {
                    positions[i * 3] += velocities[i].x;
                    positions[i * 3 + 1] += velocities[i].y;
                    positions[i * 3 + 2] += velocities[i].z;

                    // Wrap around boundaries
                    if (Math.abs(positions[i * 3]) > 19) positions[i * 3] *= -0.9;
                    if (positions[i * 3 + 1] > 7.5 || positions[i * 3 + 1] < 0.5) velocities[i].y *= -1;
                    if (Math.abs(positions[i * 3 + 2]) > 14) positions[i * 3 + 2] *= -0.9;
                }
                obj.geometry.attributes.position.needsUpdate = true;
            }
        }

        // Flicker screen lights slightly
        for (const light of this.lights) {
            if (light.color.getHex() === 0x0066cc) {
                light.intensity = 0.3 + Math.sin(time * 0.001) * 0.05;
            }
        }
    }
}

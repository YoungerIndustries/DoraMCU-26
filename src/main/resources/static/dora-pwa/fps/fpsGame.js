/**
 * Main FPS game loop and orchestrator.
 * Initializes Three.js, manages remote players, HUD, and lobby.
 */
class FpsGame {
    constructor() {
        // Detect iframe mode: running inside HUD's 3D view panel
        this.isIframeMode = (window.self !== window.top);

        this.canvas = document.getElementById('gameCanvas');
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.clock = new THREE.Clock();

        this.network = null;
        this.state = null;
        this.map = null;
        this.player = null;
        this.weapon = null;

        this.remotePlayers = new Map();
        this.isInMatch = false;
        this.isAlive = true;
        this.respawnTimer = 0;
        
        this.init();
    }
    
    init() {
        // Three.js setup
        this.scene = new THREE.Scene();
        this.scene.background = new THREE.Color(0x87CEEB);
        this.scene.fog = new THREE.Fog(0x87CEEB, 50, 150);

        const width = window.innerWidth;
        const height = window.innerHeight;
        this.camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000);

        this.renderer = new THREE.WebGLRenderer({ canvas: this.canvas, antialias: true });
        this.renderer.setSize(width, height);
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        this.renderer.shadowMap.enabled = true;

        // Lighting
        const ambientLight = new THREE.AmbientLight(0x404040, 0.6);
        this.scene.add(ambientLight);

        const dirLight = new THREE.DirectionalLight(0xffffff, 0.8);
        dirLight.position.set(50, 100, 50);
        dirLight.castShadow = true;
        dirLight.shadow.mapSize.width = 2048;
        dirLight.shadow.mapSize.height = 2048;
        this.scene.add(dirLight);
        
        // Resize handler
        window.addEventListener('resize', () => {
            if (this.isIframeMode) return; // iframe uses postMessage resize
            this.camera.aspect = window.innerWidth / window.innerHeight;
            this.camera.updateProjectionMatrix();
            this.renderer.setSize(window.innerWidth, window.innerHeight);
        });

        // Iframe mode: listen for resize and commands from parent HUD
        if (this.isIframeMode) {
            window.addEventListener('message', (e) => {
                if (e.data.type === 'HUD_RESIZE') {
                    const width = e.data.width;
                    const height = e.data.height;
                    if (width && height) {
                        this.camera.aspect = width / height;
                        this.camera.updateProjectionMatrix();
                        this.renderer.setSize(width, height);
                    }
                } else if (e.data.type === 'HUD_MATCH_CREATE') {
                    this._handleCreateMatchFromHud(e.data);
                } else if (e.data.type === 'HUD_MATCH_JOIN') {
                    this._handleJoinMatchFromHud(e.data);
                } else if (e.data.type === 'HUD_MATCH_LEAVE') {
                    this._handleLeaveMatchFromHud();
                } else if (e.data.type === 'HUD_FIRE') {
                    this.player.actions.push('shoot');
                } else if (e.data.type === 'HUD_RELOAD') {
                    this.weapon.reload();
                } else if (e.data.type === 'HUD_SWITCH_WEAPON') {
                    this.weapon.switchWeapon(e.data.index);
                }
            });
        }

        // Initialize modules
        this.network = new FpsNetwork();
        window.fpsNetwork = this.network;

        this.state = new FpsState();

        this.map = new FpsMap(this.scene);
        this.map.generateDefault();

        // Scene decorator: chosen after server sends map data
        this.decorator = null;
        this.officeDecorator = null;
        this.outdoorDecorator = null;
        this.sceneObjectLoader = null;
        this.transitionSystem = null;

        this.player = new FpsPlayer(this.camera, this.canvas);
        window.fpsPlayer = this.player;

        this.weapon = new FpsWeapon(this.scene, this.camera);

        // Setup callbacks
        this.setupNetworkCallbacks();
        this.setupStateCallbacks();
        this.setupWeaponCallbacks();

        // In iframe mode, skip lobby UI and weapon input (HUD handles these)
        if (!this.isIframeMode) {
            this.setupLobbyUI();
            this.setupWeaponInput();
        }

        // Connect to server
        this.network.connect();

        // Start game loop
        this.animate();
    }
    
    setupNetworkCallbacks() {
        this.network.onConnected = (sessionId, data) => {
            console.log('[FpsGame] Connected:', sessionId);

            // Handle map data from server to choose the right scene decorator
            if (data && data.map) {
                console.log('[FpsGame] Map data received:', data.map.name, 'theme:', data.map.theme);

                // Replace the default arena map with the server's map data
                this.map.generateFromData(data.map);

                this._applyMapDecorator(data.map);
            } else {
                // Fallback: use outdoor decorator
                console.log('[FpsGame] No map data, using outdoor decorator');
                this.decorator = new FpsSceneDecorator(this.scene, this.map.mapGroup);
                this.decorator.decorate();
            }

            // Show click-to-start prompt instead of auto-hiding lobby
            if (!this.isIframeMode) {
                const mapName = (data && data.map) ? data.map.name : '3D World';
                this.updateLobbyStatus('Click to enter ' + mapName);
                const lobbyScreen = document.getElementById('lobbyScreen');
                if (lobbyScreen) {
                    // Replace lobby content with click-to-start prompt
                    lobbyScreen.innerHTML = `
                        <h1>DoraMCU 3D World</h1>
                        <p>Connected to ${mapName} — click anywhere to start</p>
                    `;
                    lobbyScreen.style.cursor = 'pointer';
                    lobbyScreen.addEventListener('click', () => {
                        this.enterGame();
                    });
                }
            }
            this.isInMatch = true;
        };

        this.network.onDisconnected = () => {
            console.log('[FpsGame] Disconnected');
            if (!this.isIframeMode) {
                this.updateLobbyStatus('Disconnected');
            }
        };
        
        this.network.onGameState = (data) => {
            this.state.updateFromServer(data);
            this.updateRemotePlayers(data.players);

            // Reconcile local player
            if (data.players) {
                const localId = this.network.playerId;
                for (const ps of data.players) {
                    if (ps.id === localId && ps.position) {
                        this.player.reconcile(ps.position, data.tick);
                        break;
                    }
                }
            }
        };

        this.network.onGameEvent = (data) => {
            this.state.addEvent(data);
            this.handleGameEvent(data);
        };

        this.network.onError = (error) => {
            console.error('[FpsGame] Error:', error);
            if (!this.isIframeMode) {
                this.updateLobbyStatus('Error: ' + error);
            }
        };

        this.network.onTransitionAck = (data) => {
            if (this.transitionSystem) {
                this.transitionSystem.handleTransitionAck(data);
            }
        };
    }
    
    setupStateCallbacks() {
        this.state.onLocalPlayerUpdated = (player) => {
            this.updateHUD();
        };

        this.state.onGameEvent = (evt) => {
            if (evt.type === 'DEATH' && evt.playerId === this.network.playerId) {
                this.onPlayerDeath();
            }
            if (evt.type === 'RESPAWN' && evt.playerId === this.network.playerId) {
                this.onPlayerRespawn();
            }
            if (evt.type === 'KILL') {
                this.addKillFeedItem(evt.playerId, evt.targetId);
            }
            if (evt.type === 'HIT' && evt.targetId === this.network.playerId) {
                this.showDamageFlash();
            }
            // Send events to parent HUD in iframe mode
            if (this.isIframeMode) {
                this._sendEventToHud(evt);
            }
        };
    }
    
    setupWeaponCallbacks() {
        this.weapon.onAmmoChanged = (ammo, reserve) => {
            if (!this.isIframeMode) {
                document.getElementById('ammoCounter').textContent = `${ammo} / ${reserve}`;
            }
        };

        this.weapon.onWeaponChanged = (name) => {
            if (!this.isIframeMode) {
                document.getElementById('weaponName').textContent = name;
            }
        };

        this.weapon.onReloadStarted = (reloading) => {
            if (!this.isIframeMode) {
                document.getElementById('reloadIndicator').style.display = reloading ? 'block' : 'none';
            }
        };
    }
    
    setupWeaponInput() {
        document.addEventListener('keydown', (e) => {
            // Reload
            if (e.code === 'KeyR') {
                this.weapon.reload();
            }
            // Weapon switching
            if (e.code === 'Digit1') this.weapon.switchWeapon(0);
            if (e.code === 'Digit2') this.weapon.switchWeapon(1);
            if (e.code === 'Digit3') this.weapon.switchWeapon(2);
            if (e.code === 'Digit4') this.weapon.switchWeapon(3);
            if (e.code === 'Digit5') this.weapon.switchWeapon(4);
            if (e.code === 'Digit6') this.weapon.switchWeapon(5);
        });
        
        // Mouse wheel weapon switch
        this.canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            const delta = e.deltaY > 0 ? 1 : -1;
            const newIndex = (this.weapon.currentWeaponIndex + delta + this.weapon.weapons.length) % this.weapon.weapons.length;
            this.weapon.switchWeapon(newIndex);
        });
    }
    
    setupLobbyUI() {
        const btnCreate = document.getElementById('btnCreateMatch');
        const btnJoin = document.getElementById('btnJoinMatch');
        const btnReady = document.getElementById('btnReady');
        
        btnCreate.addEventListener('click', async () => {
            this.updateLobbyStatus('Creating match...');
            const result = await this.network.createMatch('Deathmatch', 'arena');
            if (result && result.id) {
                this.currentMatchId = result.id;
                this.updateLobbyStatus(`Match created: ${result.id}`);
                btnReady.disabled = false;
            } else {
                this.updateLobbyStatus('Failed to create match');
            }
        });
        
        btnJoin.addEventListener('click', async () => {
            this.updateLobbyStatus('Loading matches...');
            const matches = await this.network.listMatches();
            
            if (matches && matches.length > 0) {
                const matchList = document.getElementById('matchList');
                matchList.innerHTML = '';
                
                for (const match of matches) {
                    const div = document.createElement('div');
                    div.className = 'match-item';
                    div.textContent = `${match.name || 'Match'} (${match.mode || 'Deathmatch'}) - ${match.playerCount || 0}/${match.maxPlayers || 16}`;
                    div.addEventListener('click', async () => {
                        this.updateLobbyStatus('Joining match...');
                        const success = await this.network.joinMatch(match.id);
                        if (success) {
                            this.currentMatchId = match.id;
                            this.updateLobbyStatus(`Joined: ${match.id}`);
                            btnReady.disabled = false;
                        } else {
                            this.updateLobbyStatus('Failed to join');
                        }
                    });
                    matchList.appendChild(div);
                }
            } else {
                this.updateLobbyStatus('No matches available');
            }
        });
        
        btnReady.addEventListener('click', () => {
            if (!this.isReady) {
                this.network.sendReady();
                this.isReady = true;
                btnReady.textContent = 'Unready';
                this.updateLobbyStatus('Ready! Waiting for match to start...');
            } else {
                this.isReady = false;
                btnReady.textContent = 'Ready';
                this.updateLobbyStatus('Not ready');
            }
        });
    }
    
    updateLobbyStatus(message) {
        if (this.isIframeMode) return;
        const el = document.getElementById('lobbyStatus');
        if (el) el.textContent = message;
    }
    
    enterGame() {
        // Called after user clicks to start the game
        console.log('[FpsGame] Entering game');
        const lobbyScreen = document.getElementById('lobbyScreen');
        if (lobbyScreen) {
            lobbyScreen.style.display = 'none';
        }
        const hud = document.getElementById('hud');
        if (hud) {
            hud.style.display = 'block';
        }
        // Lock cursor for FPS controls (requires user gesture — satisfied by click)
        if (this.canvas && this.canvas.requestPointerLock) {
            this.canvas.requestPointerLock();
        }
    }
    
    startMatch() {
        this.isInMatch = true;
        if (!this.isIframeMode) {
            document.getElementById('lobbyScreen').style.display = 'none';
            document.getElementById('hud').style.display = 'block';
        }

        // Spawn player
        const spawn = this.map.getRandomSpawnPoint();
        this.player.respawn(spawn);
        this.isAlive = true;

        console.log('[FpsGame] Match started');
    }
    
    updateRemotePlayers(players) {
        if (!players) return;
        
        const currentIds = new Set();
        
        for (const ps of players) {
            if (ps.id === this.network.playerId) continue; // Skip local player
            currentIds.add(ps.id);
            
            if (this.remotePlayers.has(ps.id)) {
                // Update existing
                const rp = this.remotePlayers.get(ps.id);
                if (ps.position) {
                    rp.targetPos = new THREE.Vector3(ps.position.x, ps.position.y, ps.position.z);
                }
                if (ps.rotation) {
                    rp.targetRot = ps.rotation.y || 0;
                }
                rp.health = ps.health || 100;
                rp.alive = ps.alive !== false;
                
                if (!rp.alive && rp.wasAlive) {
                    this.showDeathEffect(rp);
                }
                rp.wasAlive = rp.alive;
            } else {
                // Create new remote player
                const pos = ps.position ? new THREE.Vector3(ps.position.x, ps.position.y, ps.position.z) : new THREE.Vector3(0, 1.7, 0);
                this.createRemotePlayer(ps.id, ps.name || `Player_${ps.id}`, pos, ps.rotation?.y || 0, ps.health || 100);
            }
        }
        
        // Remove disconnected players
        for (const [id, rp] of this.remotePlayers) {
            if (!currentIds.has(id)) {
                this.removeRemotePlayer(id);
            }
        }
    }
    
    createRemotePlayer(id, name, position, rotation, health) {
        const group = new THREE.Group();
        
        // Body
        const bodyGeo = new THREE.CapsuleGeometry(0.4, 1.6, 4, 8);
        const bodyMat = new THREE.MeshStandardMaterial({ color: 0x0066ff });
        const body = new THREE.Mesh(bodyGeo, bodyMat);
        body.position.y = 0.8;
        body.castShadow = true;
        group.add(body);
        
        // Head
        const headGeo = new THREE.SphereGeometry(0.25, 8, 8);
        const headMat = new THREE.MeshStandardMaterial({ color: 0xffcc99 });
        const head = new THREE.Mesh(headGeo, headMat);
        head.position.y = 1.7;
        group.add(head);
        
        group.position.copy(position);
        group.rotation.y = rotation;
        this.scene.add(group);
        
        this.remotePlayers.set(id, {
            id, name, mesh: group,
            targetPos: position.clone(),
            targetRot: rotation,
            health, alive: true, wasAlive: true
        });
    }
    
    removeRemotePlayer(id) {
        const rp = this.remotePlayers.get(id);
        if (rp && rp.mesh) {
            this.scene.remove(rp.mesh);
            rp.mesh.traverse((child) => {
                if (child.geometry) child.geometry.dispose();
                if (child.material) child.material.dispose();
            });
        }
        this.remotePlayers.delete(id);
    }
    
    showDeathEffect(rp) {
        if (rp.mesh) {
            rp.mesh.traverse((child) => {
                if (child.material) {
                    child.material.transparent = true;
                    child.material.opacity = 0.3;
                }
            });
        }
    }
    
    handleGameEvent(evt) {
        // Handle hit effects on remote players
        if (evt.type === 'HIT' && evt.targetId) {
            const rp = this.remotePlayers.get(evt.targetId);
            if (rp && rp.mesh) {
                // Flash red
                rp.mesh.traverse((child) => {
                    if (child.material && child.material.color) {
                        const origColor = child.material.color.getHex();
                        child.material.color.setHex(0xff0000);
                        setTimeout(() => {
                            child.material.color.setHex(origColor);
                        }, 100);
                    }
                });
            }
        }
    }
    
    onPlayerDeath() {
        this.isAlive = false;
        this.respawnTimer = 5;
        if (!this.isIframeMode) {
            document.getElementById('deathScreen').style.display = 'flex';
        }
    }

    onPlayerRespawn() {
        this.isAlive = true;
        const spawn = this.map.getRandomSpawnPoint();
        this.player.respawn(spawn);
        if (!this.isIframeMode) {
            document.getElementById('deathScreen').style.display = 'none';
        }
    }

    showDamageFlash() {
        if (this.isIframeMode) return;
        const overlay = document.getElementById('damageOverlay');
        overlay.style.opacity = '1';
        setTimeout(() => {
            overlay.style.opacity = '0';
        }, 300);
    }

    addKillFeedItem(killerId, victimId) {
        if (this.isIframeMode) return;
        const feed = document.getElementById('killFeed');
        if (!feed) return;
        const item = document.createElement('div');
        item.className = 'kill-feed-item';

        const killer = this.state.getPlayer(killerId);
        const victim = this.state.getPlayer(victimId);
        const killerName = killer ? killer.name : killerId;
        const victimName = victim ? victim.name : victimId;

        item.textContent = `${killerName} 🔫 ${victimName}`;
        feed.appendChild(item);

        setTimeout(() => {
            if (item.parentNode) item.parentNode.removeChild(item);
        }, 5000);
    }
    
    updateHUD() {
        const local = this.state.localPlayer;
        if (!local) return;

        // In iframe mode, send state to parent HUD instead of updating DOM
        if (this.isIframeMode) {
            this._sendStateToHud(local);
            return;
        }

        // Health
        const healthPercent = Math.max(0, local.health / 100);
        const healthFill = document.getElementById('healthFill');
        if (healthFill) healthFill.style.width = `${healthPercent * 100}%`;
        const healthText = document.getElementById('healthText');
        if (healthText) healthText.textContent = `Health: ${local.health}`;

        // Score
        const scoreText = document.getElementById('scoreText');
        if (scoreText) scoreText.textContent = `Score: ${local.score || 0}`;

        // Player count
        const playerCount = document.getElementById('playerCount');
        if (playerCount) playerCount.textContent = `Players: ${this.state.getPlayerCount()}`;

        // Match timer
        if (this.state.matchInfo && this.state.matchInfo.timeRemaining > 0) {
            const minutes = Math.floor(this.state.matchInfo.timeRemaining / 60);
            const seconds = this.state.matchInfo.timeRemaining % 60;
            const matchTimer = document.getElementById('matchTimer');
            if (matchTimer) matchTimer.textContent =
                `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        }

        // CTP Team Scores
        this.updateCTPScores();

        // CTP Capture Points
        this.updateCTPCapturePoints();
    }

    // ── Iframe Communication ──

    _sendStateToHud(local) {
        const weapon = this.weapon.weapons[this.weapon.currentWeaponIndex];
        window.parent.postMessage({
            type: 'FPS_STATE_UPDATE',
            health: local.health,
            shield: local.shield || 0,
            armor: local.armor || 0,
            score: local.score || 0,
            kills: local.kills || 0,
            deaths: local.deaths || 0,
            weaponIndex: this.weapon.currentWeaponIndex,
            weaponName: weapon ? weapon.name : '',
            ammo: this.weapon.currentAmmo,
            reserveAmmo: this.weapon.currentReserve,
            playerCount: this.state.getPlayerCount(),
            matchInfo: this.state.matchInfo || null,
            teamScores: this.state.teamScores || {},
            capturePoints: this.state.capturePoints || []
        }, '*');
    }

    _sendEventToHud(evt) {
        window.parent.postMessage({
            type: 'FPS_EVENT',
            event: evt
        }, '*');
    }

    async _handleCreateMatchFromHud(data) {
        try {
            const result = await this.network.createMatch(data.mode || 'Deathmatch', data.map || 'arena');
            if (result && result.id) {
                this.currentMatchId = result.id;
                window.parent.postMessage({
                    type: 'FPS_MATCH_CREATED',
                    matchId: result.id
                }, '*');
                // Auto-join the created match
                await this.network.joinMatch(result.id);
            }
        } catch (err) {
            console.error('[FpsGame] Create match failed:', err);
            window.parent.postMessage({
                type: 'FPS_ERROR',
                message: 'Failed to create match'
            }, '*');
        }
    }

    async _handleJoinMatchFromHud(data) {
        try {
            const success = await this.network.joinMatch(data.matchId);
            if (success) {
                this.currentMatchId = data.matchId;
                window.parent.postMessage({
                    type: 'FPS_MATCH_JOINED',
                    matchId: data.matchId
                }, '*');
            }
        } catch (err) {
            console.error('[FpsGame] Join match failed:', err);
            window.parent.postMessage({
                type: 'FPS_ERROR',
                message: 'Failed to join match'
            }, '*');
        }
    }

    _handleLeaveMatchFromHud() {
        this.currentMatchId = null;
        this.isInMatch = false;
        this.network.leaveMatch();
        window.parent.postMessage({
            type: 'FPS_MATCH_LEFT'
        }, '*');
    }
    
    updateCTPScores() {
        if (this.isIframeMode) return;
        const scores = this.state.teamScores;
        if (!scores) return;

        const t0El = document.getElementById('team0Score');
        const t1El = document.getElementById('team1Score');
        if (t0El) t0El.textContent = scores['0'] || 0;
        if (t1El) t1El.textContent = scores['1'] || 0;
    }

    updateCTPCapturePoints() {
        if (this.isIframeMode) return;
        const container = document.getElementById('capturePointsContainer');
        if (!container) return;

        const points = this.state.capturePoints;
        if (!points || points.length === 0) {
            container.style.display = 'none';
            return;
        }

        container.style.display = 'flex';
        container.innerHTML = '';

        for (const cp of points) {
            const div = document.createElement('div');
            div.className = 'capture-point-status';

            let color = '#888'; // neutral
            if (cp.ownerTeam === 0) color = '#0066ff';
            else if (cp.ownerTeam === 1) color = '#ff3333';

            const progress = Math.max(0, Math.min(100, cp.progress || 0));

            div.innerHTML = `
                <div class="cp-label" style="color:${color}">${cp.id}</div>
                <div class="cp-bar-bg">
                    <div class="cp-bar-fill" style="width:${progress}%;background:${color}"></div>
                </div>
            `;
            container.appendChild(div);
        }
    }
    
    updateMinimap() {
        if (this.isIframeMode) return;
        const canvas = document.getElementById('minimapCanvas');
        if (!canvas) return;
        
        const ctx = canvas.getContext('2d');
        const size = 150;
        ctx.clearRect(0, 0, size, size);
        
        // Background
        ctx.fillStyle = 'rgba(0,0,0,0.5)';
        ctx.beginPath();
        ctx.arc(size/2, size/2, size/2, 0, Math.PI * 2);
        ctx.fill();
        
        const scale = 1.5;
        const cx = size / 2;
        const cy = size / 2;
        const px = this.player.position.x;
        const pz = this.player.position.z;
        
        // Remote players
        for (const [id, rp] of this.remotePlayers) {
            if (!rp.alive) continue;
            const dx = (rp.mesh.position.x - px) * scale + cx;
            const dz = (rp.mesh.position.z - pz) * scale + cy;
            
            if (dx > 0 && dx < size && dz > 0 && dz < size) {
                ctx.fillStyle = '#0066ff';
                ctx.beginPath();
                ctx.arc(dx, dz, 3, 0, Math.PI * 2);
                ctx.fill();
            }
        }
        
        // Local player (center)
        ctx.fillStyle = '#00ff00';
        ctx.beginPath();
        ctx.arc(cx, cy, 4, 0, Math.PI * 2);
        ctx.fill();
        
        // Direction indicator
        ctx.strokeStyle = '#00ff00';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(cx, cy);
        ctx.lineTo(cx - Math.sin(this.player.yaw) * 10, cy - Math.cos(this.player.yaw) * 10);
        ctx.stroke();
    }
    
    /**
     * Apply the appropriate scene decorator based on server map data.
     */
    _applyMapDecorator(mapData) {
        const officeDecorator = new FpsOfficeDecorator(this.scene, this.map.mapGroup, mapData);
        if (officeDecorator.isOfficeMap()) {
            // Office interior map: use office decorator
            this.officeDecorator = officeDecorator;
            this.outdoorDecorator = null;
            this.officeDecorator.decorate();
            console.log('[FpsGame] Applied office interior decorator for:', mapData.name);
        } else {
            // Outdoor map: use generic scene decorator
            this.outdoorDecorator = new FpsSceneDecorator(this.scene, this.map.mapGroup);
            this.officeDecorator = null;
            this.outdoorDecorator.decorate();
            console.log('[FpsGame] Applied outdoor scene decorator for:', mapData.name);
        }

        // Load database-driven scene objects (overlays on top of procedural decoration)
        if (!this.sceneObjectLoader) {
            this.sceneObjectLoader = new FpsSceneObjectLoader(this.scene, this.map.mapGroup);
        }
        const sceneObjects = mapData.sceneObjects || [];
        if (sceneObjects.length > 0) {
            this.sceneObjectLoader.loadSceneObjects(sceneObjects);
            console.log('[FpsGame] Loaded', sceneObjects.length, 'database-driven scene objects');
        } else {
            console.log('[FpsGame] No database scene objects, using procedural decoration only');
        }

        // Initialize transition system with map data
        if (!this.transitionSystem) {
            this.transitionSystem = new FpsTransitionSystem(this.scene, this.map.mapGroup, this.network, this);
        }
        this.transitionSystem.loadTransitionPoints(mapData.transitionPoints || []);
    }
    
    animate() {
        requestAnimationFrame(() => this.animate());
        
        const deltaTime = Math.min(this.clock.getDelta(), 0.1);
        const elapsed = this.clock.getElapsedTime();

        // Update scene decorations (particles, capture points, power-ups)
        if (this.decorator) {
            this.decorator.update(elapsed);
        }
        if (this.officeDecorator) {
            this.officeDecorator.update(elapsed);
        }
        if (this.outdoorDecorator) {
            this.outdoorDecorator.update(elapsed);
        }
        
        // Update database-driven scene objects (terminal flicker, lamp pulse)
        if (this.sceneObjectLoader) {
            this.sceneObjectLoader.update(deltaTime);
        }
        
        // Update transition portals
        if (this.transitionSystem && this.isInMatch && this.isAlive) {
            this.transitionSystem.update(deltaTime, this.player.position);
        }
        
        if (this.isInMatch && this.isAlive) {
            // Update player
            this.player.update(deltaTime);
            
            // Update weapon
            this.weapon.update(deltaTime);
            
            // Handle shooting
            if (this.player.actions.includes('shoot')) {
                this.weapon.fire();
            }
            
            // Update remote players (interpolation)
            for (const [id, rp] of this.remotePlayers) {
                if (rp.mesh && rp.targetPos) {
                    rp.mesh.position.lerp(rp.targetPos, 10 * deltaTime);
                    const targetRot = rp.targetRot || 0;
                    rp.mesh.rotation.y += (targetRot - rp.mesh.rotation.y) * 10 * deltaTime;
                }
            }
            
            // Update minimap
            this.updateMinimap();
        }
        
        // Respawn timer
        if (!this.isAlive && this.isInMatch) {
            this.respawnTimer -= deltaTime;
            if (!this.isIframeMode) {
                document.getElementById('respawnTimer').textContent = `Respawning in ${Math.ceil(this.respawnTimer)}...`;
            }
            if (this.respawnTimer <= 0) {
                this.onPlayerRespawn();
            }
        }
        
        // Render
        this.renderer.render(this.scene, this.camera);
    }
}

// Start the game when page loads
window.addEventListener('DOMContentLoaded', () => {
    window.fpsGame = new FpsGame();
});

/**
 * BattleViz — Real-time battle visualization module.
 *
 * Visualizes ship-to-ship combat in 3D space with:
 * - Attack beam effects between opposing fleets
 * - Explosion particles on damage/destruction
 * - Health bars and shield indicators
 * - Battle event log with timeline
 * - Integration with GameStateBridge for real battle data
 *
 * Usage:
 *   const battleViz = new BattleViz(sceneRenderer);
 *   battleViz.connect('http://localhost:8080');
 *   battleViz.setCallbacks({ onBattleClick: (battle) => { ... } });
 *   battleViz.destroy();
 */

class BattleViz {
    /**
     * @param {SceneRenderer} sceneRenderer - The existing SceneRenderer instance
     */
    constructor(sceneRenderer) {
        this.sceneRenderer = sceneRenderer;
        this.scene = sceneRenderer.scene;
        this.camera = sceneRenderer.camera;
        this.renderer = sceneRenderer.renderer;
        
        // Battle data
        this.battles = new Map();
        this.activeBattleId = null;
        
        // Three.js objects
        this.battleGroup = null;
        this.attackBeams = [];
        this.explosions = [];
        this.healthBars = new Map();
        
        // WebSocket
        this.ws = null;
        this.connected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectDelay = 1000;
        
        // Animation
        this.animationFrame = null;
        this.running = false;
        
        // Callbacks
        this.callbacks = {
            onBattleStart: null,
            onBattleEnd: null,
            onBattleClick: null,
            onDamage: null,
            onFleetDestroyed: null
        };
        
        // UI
        this.battleLog = null;
        this.battlePanel = null;
        
        // Raycaster for interaction
        this.raycaster = new THREE.Raycaster();
        this.mouse = new THREE.Vector2();
        
        console.log('[BattleViz] Initialized');
    }

    /**
     * Set callbacks for battle events.
     */
    setCallbacks(callbacks) {
        this.callbacks = { ...this.callbacks, ...callbacks };
    }

    /**
     * Connect to WebSocket for real-time battle updates.
     */
    connect(serverUrl) {
        const url = (serverUrl || 'http://localhost:8080').replace('http', 'ws') + '/ws/fleet-tracker';
        
        try {
            this.ws = new WebSocket(url);
            
            this.ws.onopen = () => {
                this.connected = true;
                this.reconnectAttempts = 0;
                console.log('[BattleViz] Connected');
            };
            
            this.ws.onmessage = (event) => this._handleMessage(event.data);
            
            this.ws.onclose = () => {
                this.connected = false;
                console.log('[BattleViz] Disconnected');
                this._attemptReconnect();
            };
            
            this.ws.onerror = (error) => {
                console.error('[BattleViz] WebSocket error:', error);
            };
        } catch (e) {
            console.error('[BattleViz] Failed to connect:', e);
            this._attemptReconnect();
        }
    }

    /**
     * Handle incoming WebSocket messages.
     * @private
     */
    _handleMessage(data) {
        try {
            const message = JSON.parse(data);
            
            switch (message.type) {
                case 'fleet_combat':
                    this._handleCombat(message);
                    break;
                case 'fleet_update':
                    this._handleFleetUpdate(message);
                    break;
                case 'fleet_destroyed':
                    this._handleFleetDestroyed(message);
                    break;
            }
        } catch (e) {
            console.error('[BattleViz] Failed to parse message:', e);
        }
    }

    /**
     * Handle combat event.
     * @private
     */
    _handleCombat(message) {
        if (!this.battleGroup) {
            this.battleGroup = new THREE.Group();
            this.battleGroup.name = 'battle-viz';
            this.scene.add(this.battleGroup);
        }
        
        if (message.battles) {
            for (const battle of message.battles) {
                this._updateBattle(battle);
            }
        }
        
        if (this.callbacks.onBattleStart) {
            this.callbacks.onBattleStart(message);
        }
    }

    /**
     * Update or create a battle visualization.
     * @private
     */
    _updateBattle(battle) {
        const existing = this.battles.get(battle.battleId);
        
        if (!existing) {
            this._createBattle(battle);
        } else {
            this._updateBattleVisuals(existing, battle);
        }
    }

    /**
     * Create a new battle visualization.
     * @private
     */
    _createBattle(battle) {
        const group = new THREE.Group();
        group.name = `battle-${battle.battleId}`;
        
        // Parse location
        const [locX, locZ] = (battle.location || '0,0').split(',').map(Number);
        group.position.set(locX * 10, 0, locZ * 10);
        
        // Attacker fleet marker
        const attackerGeo = new THREE.BoxGeometry(2, 1, 2);
        const attackerMat = new THREE.MeshStandardMaterial({ 
            color: 0xff3333,
            emissive: 0x660000,
            roughness: 0.3 
        });
        const attackerMesh = new THREE.Mesh(attackerGeo, attackerMat);
        attackerMesh.position.set(-5, 1, 0);
        attackerMesh.userData = { type: 'fleet', side: 'attacker', battleId: battle.battleId };
        group.add(attackerMesh);
        
        // Defender fleet marker
        const defenderGeo = new THREE.BoxGeometry(2, 1, 2);
        const defenderMat = new THREE.MeshStandardMaterial({ 
            color: 0x3333ff,
            emissive: 0x000066,
            roughness: 0.3 
        });
        const defenderMesh = new THREE.Mesh(defenderGeo, defenderMat);
        defenderMesh.position.set(5, 1, 0);
        defenderMesh.userData = { type: 'fleet', side: 'defender', battleId: battle.battleId };
        group.add(defenderMesh);
        
        // Health bars
        this._createHealthBar(group, battle.attackerHull, battle.attackerShips, -5, 2.5, 0, 'attacker');
        this._createHealthBar(group, battle.defenderHull, battle.defenderShips, 5, 2.5, 0, 'defender');
        
        // Attack beam
        const beamGeo = new THREE.CylinderGeometry(0.05, 0.05, 10, 8);
        const beamMat = new THREE.MeshBasicMaterial({ 
            color: 0xffff00,
            transparent: true,
            opacity: 0.8
        });
        const beam = new THREE.Mesh(beamGeo, beamMat);
        beam.rotation.z = Math.PI / 2;
        beam.position.set(0, 1, 0);
        beam.visible = false;
        group.add(beam);
        
        this.battleGroup.add(group);
        
        this.battles.set(battle.battleId, {
            group,
            attackerMesh,
            defenderMesh,
            beam,
            data: battle,
            animPhase: 0
        });
        
        console.log(`[BattleViz] Created battle: ${battle.attackerName} vs ${battle.defenderName}`);
    }

    /**
     * Create a health bar for a fleet.
     * @private
     */
    _createHealthBar(parent, hull, ships, x, y, z, side) {
        // Background
        const bgGeo = new THREE.PlaneGeometry(3, 0.3);
        const bgMat = new THREE.MeshBasicMaterial({ color: 0x333333, side: THREE.DoubleSide });
        const bg = new THREE.Mesh(bgGeo, bgMat);
        bg.position.set(x, y, z);
        bg.rotation.x = -Math.PI / 4;
        parent.add(bg);
        
        // Health fill
        const fillGeo = new THREE.PlaneGeometry(3, 0.3);
        const fillMat = new THREE.MeshBasicMaterial({ 
            color: side === 'attacker' ? 0xff3333 : 0x3333ff,
            side: THREE.DoubleSide
        });
        const fill = new THREE.Mesh(fillGeo, fillMat);
        fill.position.set(x, y, z);
        fill.rotation.x = -Math.PI / 4;
        fill.userData = { type: 'healthBar', side };
        parent.add(fill);
        
        // Ship count label (using a small sphere as indicator)
        const indicatorGeo = new THREE.SphereGeometry(0.2, 8, 8);
        const indicatorMat = new THREE.MeshBasicMaterial({ color: 0xffffff });
        const indicator = new THREE.Mesh(indicatorGeo, indicatorMat);
        indicator.position.set(x, y + 0.3, z);
        parent.add(indicator);
    }

    /**
     * Update battle visuals.
     * @private
     */
    _updateBattleVisuals(battleViz, battleData) {
        battleViz.data = battleData;
        
        // Update health bar scales based on hull points
        const attackerHealth = battleData.attackerHull / Math.max(battleData.attackerShips * 100, 1);
        const defenderHealth = battleData.defenderHull / Math.max(battleData.defenderShips * 100, 1);
        
        battleViz.group.children.forEach(child => {
            if (child.userData.type === 'healthBar') {
                const scale = child.userData.side === 'attacker' ? attackerHealth : defenderHealth;
                child.scale.x = Math.max(0.01, scale);
            }
        });
        
        // Flash attack beam
        battleViz.beam.visible = true;
        battleViz.animPhase = 10;
    }

    /**
     * Handle fleet update.
     * @private
     */
    _handleFleetUpdate(message) {
        if (this.callbacks.onDamage) {
            this.callbacks.onDamage(message);
        }
    }

    /**
     * Handle fleet destroyed event.
     * @private
     */
    _handleFleetDestroyed(message) {
        // Create explosion effect
        if (message.x !== undefined) {
            this._createExplosion(message.x, message.y || 0, message.z || 0);
        }
        
        // Remove battle if both sides destroyed
        if (message.battleId) {
            const battle = this.battles.get(message.battleId);
            if (battle) {
                this._removeBattle(message.battleId);
            }
        }
        
        if (this.callbacks.onFleetDestroyed) {
            this.callbacks.onFleetDestroyed(message);
        }
        
        this._addLogEntry(`Fleet destroyed: ${message.fleetName || message.fleetId}`);
    }

    /**
     * Create explosion particle effect.
     * @private
     */
    _createExplosion(x, y, z) {
        const particleCount = 20;
        const geometry = new THREE.BufferGeometry();
        const positions = new Float32Array(particleCount * 3);
        const velocities = [];
        
        for (let i = 0; i < particleCount; i++) {
            positions[i * 3] = x;
            positions[i * 3 + 1] = y;
            positions[i * 3 + 2] = z;
            
            velocities.push(new THREE.Vector3(
                (Math.random() - 0.5) * 2,
                Math.random() * 2,
                (Math.random() - 0.5) * 2
            ));
        }
        
        geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        
        const material = new THREE.PointsMaterial({
            color: 0xff6600,
            size: 0.5,
            transparent: true,
            opacity: 1
        });
        
        const particles = new THREE.Points(geometry, material);
        this.scene.add(particles);
        
        this.explosions.push({
            mesh: particles,
            velocities,
            life: 60, // frames
            maxLife: 60
        });
    }

    /**
     * Remove a battle visualization.
     * @private
     */
    _removeBattle(battleId) {
        const battle = this.battles.get(battleId);
        if (battle && this.battleGroup) {
            this.battleGroup.remove(battle.group);
            
            // Dispose geometries and materials
            battle.group.traverse((obj) => {
                if (obj.geometry) obj.geometry.dispose();
                if (obj.material) {
                    if (Array.isArray(obj.material)) {
                        obj.material.forEach(m => m.dispose());
                    } else {
                        obj.material.dispose();
                    }
                }
            });
        }
        
        this.battles.delete(battleId);
        
        if (this.callbacks.onBattleEnd) {
            this.callbacks.onBattleEnd(battleId);
        }
        
        console.log(`[BattleViz] Removed battle: ${battleId}`);
    }

    /**
     * Add entry to battle log.
     * @private
     */
    _addLogEntry(message) {
        const time = new Date().toLocaleTimeString();
        console.log(`[BattleViz] [${time}] ${message}`);
    }

    /**
     * Start animation loop.
     */
    start() {
        if (this.running) return;
        this.running = true;
        this._animate();
        console.log('[BattleViz] Animation started');
    }

    /**
     * Stop animation loop.
     */
    stop() {
        this.running = false;
        if (this.animationFrame) {
            cancelAnimationFrame(this.animationFrame);
            this.animationFrame = null;
        }
        console.log('[BattleViz] Animation stopped');
    }

    /**
     * Animation loop.
     * @private
     */
    _animate() {
        if (!this.running) return;
        
        // Animate attack beams
        this.battles.forEach((battle) => {
            if (battle.animPhase > 0) {
                battle.animPhase--;
                battle.beam.material.opacity = (battle.animPhase / 10) * 0.8;
                battle.beam.scale.y = 1 + Math.sin(battle.animPhase * 0.5) * 0.3;
                
                if (battle.animPhase <= 0) {
                    battle.beam.visible = false;
                }
            }
            
            // Rotate fleet markers slowly
            if (battle.attackerMesh) {
                battle.attackerMesh.rotation.y += 0.01;
            }
            if (battle.defenderMesh) {
                battle.defenderMesh.rotation.y -= 0.01;
            }
        });
        
        // Animate explosions
        for (let i = this.explosions.length - 1; i >= 0; i--) {
            const exp = this.explosions[i];
            exp.life--;
            
            const positions = exp.mesh.geometry.attributes.position.array;
            for (let j = 0; j < exp.velocities.length; j++) {
                positions[j * 3] += exp.velocities[j].x * 0.1;
                positions[j * 3 + 1] += exp.velocities[j].y * 0.1;
                positions[j * 3 + 2] += exp.velocities[j].z * 0.1;
                exp.velocities[j].y -= 0.05; // gravity
            }
            exp.mesh.geometry.attributes.position.needsUpdate = true;
            exp.mesh.material.opacity = exp.life / exp.maxLife;
            
            if (exp.life <= 0) {
                this.scene.remove(exp.mesh);
                exp.mesh.geometry.dispose();
                exp.mesh.material.dispose();
                this.explosions.splice(i, 1);
            }
        }
        
        this.animationFrame = requestAnimationFrame(() => this._animate());
    }

    /**
     * Handle mouse click for battle selection.
     */
    handleClick(event) {
        const rect = this.renderer.domElement.getBoundingClientRect();
        this.mouse.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
        this.mouse.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
        
        this.raycaster.setFromCamera(this.mouse, this.camera);
        
        const intersects = this.raycaster.intersectObjects(this.battleGroup?.children || [], true);
        
        if (intersects.length > 0) {
            let obj = intersects[0].object;
            while (obj && !obj.userData.battleId) {
                obj = obj.parent;
            }
            
            if (obj && obj.userData.battleId) {
                const battle = this.battles.get(obj.userData.battleId);
                if (battle && this.callbacks.onBattleClick) {
                    this.activeBattleId = obj.userData.battleId;
                    this.callbacks.onBattleClick(battle.data);
                }
            }
        }
    }

    /**
     * Get active battle count.
     */
    getActiveBattleCount() {
        return this.battles.size;
    }

    /**
     * Get all active battles.
     */
    getActiveBattles() {
        return Array.from(this.battles.values()).map(b => b.data);
    }

    /**
     * Attempt to reconnect.
     * @private
     */
    _attemptReconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('[BattleViz] Max reconnection attempts reached');
            return;
        }
        
        this.reconnectAttempts++;
        const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
        console.log(`[BattleViz] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);
        
        setTimeout(() => {
            if (!this.connected) {
                this.connect();
            }
        }, delay);
    }

    /**
     * Destroy the battle visualization and clean up.
     */
    destroy() {
        this.stop();
        
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        
        // Clean up all battle visuals
        this.battles.forEach((battle) => this._removeBattle(battle.battleId));
        
        // Clean up explosions
        this.explosions.forEach(exp => {
            this.scene.remove(exp.mesh);
            exp.mesh.geometry.dispose();
            exp.mesh.material.dispose();
        });
        this.explosions = [];
        
        // Remove battle group
        if (this.battleGroup && this.battleGroup.parent) {
            this.battleGroup.parent.remove(this.battleGroup);
            this.battleGroup = null;
        }
        
        this.connected = false;
        this.callbacks = {};
        
        console.log('[BattleViz] Destroyed');
    }
}

// Expose globally
window.BattleViz = BattleViz;

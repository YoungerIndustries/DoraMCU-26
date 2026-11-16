/**
 * Earth 2026 Client — RPG Hub with FPS Battlefield sub-mode
 * The RPG HUD is the default landing page. FPS Battlefield is accessed
 * from within the RPG HUD via the game mode selection cards.
 */
const Earth2026FpsClient = (function() {
    'use strict';

    // ============================================================
    // Configuration
    // ============================================================
    const API_BASE = '/api/v1';
    const WS_FPS = '/ws/fps';

    // ============================================================
    // Game State
    // ============================================================
    let gameState = {
        inMatch: false,
        paused: false,
        dead: false,
        matchId: null,
        playerId: null,
        playerName: 'Player',
        team: 0,
        health: 100,
        maxHealth: 100,
        armor: 0,
        ammo: 30,
        reserveAmmo: 120,
        currentWeapon: null,
        weapons: [],
        kills: 0,
        deaths: 0,
        assists: 0,
        position: { x: 0, y: 1.7, z: 0 },
        pitch: 0,
        yaw: 0,
        map: null,
        gameMode: 'DEATHMATCH',
        players: {},
        enemies: [],
        matchTime: 0,
        ping: 0
    };

    // ============================================================
    // RPG HUD State
    // ============================================================
    let rpgState = {
        character: null,
        locations: [],
        scenarios: [],
        portals: [],
        currentMode: 'rpg'  // 'rpg', 'fps', 'explore', 'corp'
    };

    // ============================================================
    // Canvas & Rendering
    // ============================================================
    let canvas, ctx;
    let minimapCanvas, minimapCtx;
    let animationFrameId = null;
    let lastFrameTime = 0;
    let fpsCounter = 0;
    let fpsDisplay = 60;

    // ============================================================
    // WebSocket Connection
    // ============================================================
    let ws = null;
    let wsReconnectTimer = null;

    // ============================================================
    // Input State
    // ============================================================
    let keys = {};
    let mouse = { x: 0, y: 0, dx: 0, dy: 0, left: false, right: false };
    let pointerLocked = false;

    // ============================================================
    // DOM Elements
    // ============================================================
    let els = {};

    // ============================================================
    // Initialization
    // ============================================================
    function init() {
        cacheElements();
        setupCanvas();
        bindRpgEvents();
        bindLobbyEvents();
        bindGameEvents();
        bindInputEvents();
        loadRpgHub();
        startRenderLoop();
    }

    function cacheElements() {
        els = {
            // RPG HUD elements
            rpgHud: document.getElementById('rpgHud'),
            rpgSidebar: document.getElementById('rpgSidebar'),
            rpgCharAvatar: document.getElementById('rpgCharAvatar'),
            rpgCharName: document.getElementById('rpgCharName'),
            rpgCharLocation: document.getElementById('rpgCharLocation'),
            rpgHealth: document.getElementById('rpgHealth'),
            rpgHealthBar: document.getElementById('rpgHealthBar'),
            rpgLevel: document.getElementById('rpgLevel'),
            rpgXP: document.getElementById('rpgXP'),
            rpgXPBar: document.getElementById('rpgXPBar'),
            rpgStr: document.getElementById('rpgStr'),
            rpgDex: document.getElementById('rpgDex'),
            rpgCon: document.getElementById('rpgCon'),
            rpgInt: document.getElementById('rpgInt'),
            rpgWis: document.getElementById('rpgWis'),
            rpgCha: document.getElementById('rpgCha'),
            rpgInventory: document.getElementById('rpgInventory'),
            rpgModeGrid: document.getElementById('rpgModeGrid'),
            rpgScenarioList: document.getElementById('rpgScenarioList'),
            rpgLocationList: document.getElementById('rpgLocationList'),
            rpgPortalList: document.getElementById('rpgPortalList'),
            rpgCredits: document.getElementById('rpgCredits'),
            rpgInfluence: document.getElementById('rpgInfluence'),
            rpgTech: document.getElementById('rpgTech'),
            rpgBtnRefresh: document.getElementById('rpgBtnRefresh'),
            rpgBtnHQ: document.getElementById('rpgBtnHQ'),
            rpgBtnOffice: document.getElementById('rpgBtnOffice'),
            rpgBtnLogout: document.getElementById('rpgBtnLogout'),
            rpgToastContainer: document.getElementById('rpgToastContainer'),

            // Overlays
            lobbyOverlay: document.getElementById('lobbyOverlay'),
            pauseOverlay: document.getElementById('pauseOverlay'),
            deathOverlay: document.getElementById('deathOverlay'),
            matchEndOverlay: document.getElementById('matchEndOverlay'),
            loadingOverlay: document.getElementById('loadingOverlay'),

            // HUD elements
            fpsHud: document.getElementById('fpsHud'),
            crosshair: document.getElementById('crosshair'),
            hitmarker: document.getElementById('hitmarker'),
            damageOverlay: document.getElementById('damageOverlay'),
            killFeed: document.getElementById('killFeed'),
            centerMessage: document.getElementById('centerMessage'),
            centerMessageText: document.getElementById('centerMessageText'),

            // Top bar
            matchMode: document.getElementById('matchMode'),
            matchMap: document.getElementById('matchMap'),
            matchTimer: document.getElementById('matchTimer'),
            hudPing: document.getElementById('hudPing'),
            hudFps: document.getElementById('hudFps'),

            // Health & Armor
            healthBar: document.getElementById('healthBar'),
            healthValue: document.getElementById('healthValue'),
            armorBar: document.getElementById('armorBar'),
            armorValue: document.getElementById('armorValue'),

            // Player info
            playerName: document.getElementById('playerName'),
            playerTeam: document.getElementById('playerTeam'),
            playerKills: document.getElementById('playerKills'),
            playerDeaths: document.getElementById('playerDeaths'),
            playerAssists: document.getElementById('playerAssists'),

            // Weapon & Ammo
            weaponName: document.getElementById('weaponName'),
            weaponType: document.getElementById('weaponType'),
            ammoMag: document.getElementById('ammoMag'),
            ammoReserve: document.getElementById('ammoReserve'),
            reloadIndicator: document.getElementById('reloadIndicator'),
            reloadBar: document.getElementById('reloadBar'),
            weaponSlots: document.getElementById('weaponSlots'),

            // Minimap
            minimapCanvas: document.getElementById('minimapCanvas'),

            // Lobby
            mapGrid: document.getElementById('mapGrid'),
            modeGrid: document.getElementById('modeGrid'),
            weaponGrid: document.getElementById('weaponGrid'),
            matchesList: document.getElementById('matchesList'),
            lobbyCharName: document.getElementById('lobbyCharName'),
            lobbyCharLevel: document.getElementById('lobbyCharLevel'),
            lobbyCharLocation: document.getElementById('lobbyCharLocation'),

            // Death
            deathKiller: document.getElementById('deathKiller'),
            deathWeapon: document.getElementById('deathWeapon'),
            respawnCountdown: document.getElementById('respawnCountdown'),

            // Chat
            chatMessages: document.getElementById('chatMessages'),
            chatInput: document.getElementById('chatInput'),

            // Toast
            toastContainer: document.getElementById('toastContainer'),

            // Loading
            loadingText: document.getElementById('loadingText')
        };
    }

    function setupCanvas() {
        canvas = document.getElementById('fpsCanvas');
        ctx = canvas.getContext('2d');
        minimapCanvas = document.getElementById('minimapCanvas');
        minimapCtx = minimapCanvas.getContext('2d');
        resizeCanvas();
        window.addEventListener('resize', resizeCanvas);
    }

    function resizeCanvas() {
        canvas.width = window.innerWidth;
        canvas.height = window.innerHeight;
    }

    // ============================================================
    // Render Loop
    // ============================================================
    function startRenderLoop() {
        lastFrameTime = performance.now();
        function loop(timestamp) {
            const delta = timestamp - lastFrameTime;
            lastFrameTime = timestamp;
            fpsCounter++;
            if (fpsCounter % 60 === 0) {
                fpsDisplay = Math.round(1000 / delta);
                if (els.hudFps) els.hudFps.textContent = fpsDisplay + ' FPS';
            }
            renderScene(delta);
            renderMinimap();
            animationFrameId = requestAnimationFrame(loop);
        }
        animationFrameId = requestAnimationFrame(loop);
    }

    function renderScene(delta) {
        if (!ctx) return;
        const w = canvas.width;
        const h = canvas.height;

        // Sky gradient
        const skyGrad = ctx.createLinearGradient(0, 0, 0, h / 2);
        skyGrad.addColorStop(0, '#0a1a2a');
        skyGrad.addColorStop(1, '#1a3a4a');
        ctx.fillStyle = skyGrad;
        ctx.fillRect(0, 0, w, h / 2);

        // Ground gradient
        const groundGrad = ctx.createLinearGradient(0, h / 2, 0, h);
        groundGrad.addColorStop(0, '#2a3a2a');
        groundGrad.addColorStop(1, '#1a2a1a');
        ctx.fillStyle = groundGrad;
        ctx.fillRect(0, h / 2, w, h / 2);

        // Draw grid lines for ground perspective
        ctx.strokeStyle = 'rgba(0, 255, 136, 0.1)';
        ctx.lineWidth = 1;
        const horizon = h / 2;
        for (let i = 0; i < 20; i++) {
            const y = horizon + (i * i * h * 0.0025);
            ctx.beginPath();
            ctx.moveTo(0, y);
            ctx.lineTo(w, y);
            ctx.stroke();
        }
        for (let i = -10; i <= 10; i++) {
            const x = w / 2 + i * w * 0.05;
            ctx.beginPath();
            ctx.moveTo(w / 2, horizon);
            ctx.lineTo(x, h);
            ctx.stroke();
        }

        // Draw buildings/structures (simple 3D boxes)
        drawBuildings(w, h);

        // Draw enemies
        drawEnemies(w, h);

        // Draw weapon model
        drawWeapon(w, h);

        // Draw particles
        drawParticles(w, h);
    }

    function drawBuildings(w, h) {
        const horizon = h / 2;
        const buildings = [
            { x: -0.6, width: 0.15, height: 0.3, color: '#1a2a3a' },
            { x: -0.3, width: 0.1, height: 0.4, color: '#2a3a2a' },
            { x: 0.1, width: 0.2, height: 0.25, color: '#1a2a2a' },
            { x: 0.4, width: 0.12, height: 0.35, color: '#2a2a3a' },
            { x: 0.7, width: 0.18, height: 0.2, color: '#1a3a3a' }
        ];

        buildings.forEach(b => {
            const screenX = w / 2 + b.x * w;
            const screenW = b.width * w;
            const screenH = b.height * h;
            const screenY = horizon - screenH;

            // Building body
            ctx.fillStyle = b.color;
            ctx.fillRect(screenX, screenY, screenW, screenH);

            // Building outline
            ctx.strokeStyle = 'rgba(0, 255, 136, 0.2)';
            ctx.lineWidth = 1;
            ctx.strokeRect(screenX, screenY, screenW, screenH);

            // Windows
            ctx.fillStyle = 'rgba(255, 200, 100, 0.3)';
            for (let wy = screenY + 10; wy < screenY + screenH - 10; wy += 20) {
                for (let wx = screenX + 5; wx < screenX + screenW - 5; wx += 15) {
                    ctx.fillRect(wx, wy, 8, 12);
                }
            }
        });
    }

    function drawEnemies(w, h) {
        const horizon = h / 2;
        // Draw enemy silhouettes
        gameState.enemies.forEach((enemy, i) => {
            const screenX = w / 2 + enemy.x * w * 0.5;
            const screenY = horizon - enemy.y * h * 0.3;
            const size = 30 + enemy.distance * 10;

            // Enemy body
            ctx.fillStyle = 'rgba(255, 50, 50, 0.6)';
            ctx.beginPath();
            ctx.arc(screenX, screenY - size / 2, size / 4, 0, Math.PI * 2);
            ctx.fill();
            ctx.fillRect(screenX - size / 6, screenY - size / 2, size / 3, size / 2);

            // Health bar
            const barWidth = size / 2;
            const barHeight = 4;
            ctx.fillStyle = 'rgba(0,0,0,0.5)';
            ctx.fillRect(screenX - barWidth / 2, screenY - size - 5, barWidth, barHeight);
            ctx.fillStyle = 'rgba(255, 50, 50, 0.8)';
            ctx.fillRect(screenX - barWidth / 2, screenY - size - 5, barWidth * (enemy.health / 100), barHeight);
        });
    }

    function drawWeapon(w, h) {
        // Simple weapon model at bottom-right
        const weaponX = w - 200;
        const weaponY = h - 150;

        ctx.save();
        ctx.translate(weaponX, weaponY);

        // Weapon body
        ctx.fillStyle = '#3a3a3a';
        ctx.fillRect(0, 50, 150, 20);
        ctx.fillRect(100, 30, 60, 40);

        // Weapon details
        ctx.fillStyle = '#4a4a4a';
        ctx.fillRect(120, 20, 40, 10);

        // Muzzle flash (if shooting)
        if (mouse.left && Math.random() > 0.5) {
            ctx.fillStyle = 'rgba(255, 200, 50, 0.8)';
            ctx.beginPath();
            ctx.arc(160, 40, 15, 0, Math.PI * 2);
            ctx.fill();
        }

        ctx.restore();
    }

    function drawParticles(w, h) {
        // Dust/smoke particles
        ctx.fillStyle = 'rgba(100, 100, 100, 0.1)';
        for (let i = 0; i < 20; i++) {
            const x = (Math.sin(Date.now() * 0.001 + i) * 0.5 + 0.5) * w;
            const y = (Math.cos(Date.now() * 0.0005 + i * 2) * 0.5 + 0.5) * h;
            const size = 2 + Math.sin(Date.now() * 0.002 + i) * 2;
            ctx.beginPath();
            ctx.arc(x, y, size, 0, Math.PI * 2);
            ctx.fill();
        }
    }

    function renderMinimap() {
        if (!minimapCtx) return;
        const w = minimapCanvas.width;
        const h = minimapCanvas.height;

        // Clear
        minimapCtx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        minimapCtx.fillRect(0, 0, w, h);

        // Grid
        minimapCtx.strokeStyle = 'rgba(0, 255, 136, 0.1)';
        minimapCtx.lineWidth = 1;
        for (let i = 0; i < w; i += 20) {
            minimapCtx.beginPath();
            minimapCtx.moveTo(i, 0);
            minimapCtx.lineTo(i, h);
            minimapCtx.stroke();
        }
        for (let i = 0; i < h; i += 20) {
            minimapCtx.beginPath();
            minimapCtx.moveTo(0, i);
            minimapCtx.lineTo(w, i);
            minimapCtx.stroke();
        }

        // Buildings on minimap
        minimapCtx.fillStyle = 'rgba(50, 50, 50, 0.8)';
        minimapCtx.fillRect(w * 0.2, h * 0.2, w * 0.15, h * 0.15);
        minimapCtx.fillRect(w * 0.6, h * 0.3, w * 0.2, h * 0.1);
        minimapCtx.fillRect(w * 0.3, h * 0.6, w * 0.1, h * 0.2);

        // Players on minimap
        Object.entries(gameState.players).forEach(([id, player]) => {
            const mx = w / 2 + (player.posX || 0) * 0.5;
            const my = h / 2 + (player.posZ || 0) * 0.5;

            minimapCtx.fillStyle = id === gameState.playerId ? '#00ff88' :
                                   player.team === gameState.team ? '#00ccff' : '#ff3333';
            minimapCtx.beginPath();
            minimapCtx.arc(mx, my, 4, 0, Math.PI * 2);
            minimapCtx.fill();
        });

        // Player direction indicator
        const px = w / 2;
        const py = h / 2;
        minimapCtx.strokeStyle = '#00ff88';
        minimapCtx.lineWidth = 2;
        minimapCtx.beginPath();
        minimapCtx.moveTo(px, py);
        minimapCtx.lineTo(px + Math.sin(gameState.yaw) * 15, py - Math.cos(gameState.yaw) * 15);
        minimapCtx.stroke();
    }

    // ============================================================
    // RPG Hub — Main landing page
    // ============================================================
    async function loadRpgHub() {
        // Load character data
        await loadRpgCharacter();
        // Load locations
        await loadRpgLocations();
        // Load scenarios
        await loadRpgScenarios();
        // Load portals
        await loadRpgPortals();
    }

    async function loadRpgCharacter() {
        const charId = localStorage.getItem('selected_char_id');
        if (!charId) {
            // Try to load the first available character
            try {
                const res = await fetch(`${API_BASE}/earth2026/characters`);
                if (res.ok) {
                    const chars = await res.json();
                    const charArray = Array.isArray(chars) ? chars : [chars];
                    if (charArray.length > 0) {
                        localStorage.setItem('selected_char_id', charArray[0].id.toString());
                        await loadRpgCharacterById(charArray[0].id);
                        return;
                    }
                }
            } catch (e) { /* ignore */ }
            // No character — show default state
            updateRpgCharacterUI({
                name: 'Player',
                earthLocation: 'San Diego, CA',
                hitPoints: 100,
                maxHitPoints: 100,
                level: 1,
                experiencePoints: 0,
                strScore: 10, dexScore: 10, conScore: 10,
                intScore: 10, wisScore: 10, chaScore: 10
            });
            return;
        }
        await loadRpgCharacterById(parseInt(charId));
    }

    async function loadRpgCharacterById(id) {
        try {
            const res = await fetch(`${API_BASE}/earth2026/characters/${id}`);
            if (res.ok) {
                const char = await res.json();
                rpgState.character = char;
                updateRpgCharacterUI(char);
                // Sync FPS game state
                gameState.playerName = char.name || 'Player';
                gameState.playerId = id.toString();
                gameState.health = char.hitPoints || 100;
                gameState.maxHealth = char.maxHitPoints || 100;
            }
        } catch (err) {
            console.error('Failed to load RPG character:', err);
        }
    }

    function updateRpgCharacterUI(char) {
        if (!els.rpgCharName) return;
        els.rpgCharName.textContent = char.name || 'Player';
        els.rpgCharLocation.textContent = '📍 ' + (char.earthLocation || 'San Diego, CA');
        
        const hp = char.hitPoints || 100;
        const maxHp = char.maxHitPoints || 100;
        if (els.rpgHealth) els.rpgHealth.textContent = `${hp}/${maxHp}`;
        if (els.rpgHealthBar) els.rpgHealthBar.style.width = `${(hp / maxHp) * 100}%`;
        
        const level = char.level || 1;
        if (els.rpgLevel) els.rpgLevel.textContent = level;
        
        const xp = char.experiencePoints || 0;
        const xpNeeded = level * 1000;
        if (els.rpgXP) els.rpgXP.textContent = `${xp} / ${xpNeeded}`;
        if (els.rpgXPBar) els.rpgXPBar.style.width = `${Math.min(100, (xp / xpNeeded) * 100)}%`;
        
        if (els.rpgStr) els.rpgStr.textContent = char.strScore || 10;
        if (els.rpgDex) els.rpgDex.textContent = char.dexScore || 10;
        if (els.rpgCon) els.rpgCon.textContent = char.conScore || 10;
        if (els.rpgInt) els.rpgInt.textContent = char.intScore || 10;
        if (els.rpgWis) els.rpgWis.textContent = char.wisScore || 10;
        if (els.rpgCha) els.rpgCha.textContent = char.chaScore || 10;
    }

    async function loadRpgLocations() {
        try {
            const res = await fetch(`${API_BASE}/earth2026/locations`);
            if (res.ok) {
                const locations = await res.json();
                rpgState.locations = Array.isArray(locations) ? locations : [];
                renderRpgLocations(rpgState.locations);
            }
        } catch (err) {
            console.error('Failed to load locations:', err);
        }
    }

    function renderRpgLocations(locations) {
        if (!els.rpgLocationList) return;
        if (locations.length === 0) {
            els.rpgLocationList.innerHTML = '<div style="color:#9aa0a6;font-size:0.85rem;padding:8px;">No locations available</div>';
            return;
        }
        els.rpgLocationList.innerHTML = locations.map(loc => `
            <div class="rpg-location-item" data-loc="${(loc.name || '').replace(/'/g, "\\'")}">
                <div class="loc-name">${loc.name || 'Unknown'}</div>
                <div class="loc-detail">${loc.region || ''} ${loc.hasPortal ? '· 🌀 Portal' : ''}</div>
            </div>
        `).join('');
        
        els.rpgLocationList.querySelectorAll('.rpg-location-item').forEach(item => {
            item.addEventListener('click', () => {
                const charId = localStorage.getItem('selected_char_id');
                if (charId) moveToLocation(parseInt(charId), item.dataset.loc);
            });
        });
    }

    async function loadRpgScenarios() {
        try {
            const res = await fetch(`${API_BASE}/earth2026/scenarios?level=1`);
            if (res.ok) {
                const scenarios = await res.json();
                rpgState.scenarios = Array.isArray(scenarios) ? scenarios : [];
                renderRpgScenarios(rpgState.scenarios);
            }
        } catch (err) {
            console.error('Failed to load scenarios:', err);
        }
    }

    function renderRpgScenarios(scenarios) {
        if (!els.rpgScenarioList) return;
        if (scenarios.length === 0) {
            els.rpgScenarioList.innerHTML = '<div style="color:#9aa0a6;font-size:0.85rem;padding:16px;text-align:center;">No scenarios available</div>';
            return;
        }
        els.rpgScenarioList.innerHTML = scenarios.map(scen => {
            const status = scen.status || 'AVAILABLE';
            let statusColor = '#64ffda';
            if (status === 'IN_PROGRESS') statusColor = '#ffd700';
            else if (status === 'COMPLETED') statusColor = '#98c379';
            else if (status === 'FAILED') statusColor = '#e06c75';
            return `
                <div class="rpg-scenario-item" data-scen-id="${scen.id}">
                    <div class="scen-name">${scen.name || scen.title || 'Unknown'}</div>
                    <div class="scen-detail">Level ${scen.minLevel || scen.level || 1} | XP: ${scen.xpReward || scen.xp || 0} | <span style="color:${statusColor}">${status}</span></div>
                </div>
            `;
        }).join('');
        
        els.rpgScenarioList.querySelectorAll('.rpg-scenario-item').forEach(item => {
            item.addEventListener('click', () => {
                const charId = localStorage.getItem('selected_char_id');
                if (charId) startScenarioAction(parseInt(charId), parseInt(item.dataset.scenId));
            });
        });
    }

    async function loadRpgPortals() {
        try {
            const res = await fetch(`${API_BASE}/earth2026/locations/portals`);
            if (res.ok) {
                const portals = await res.json();
                rpgState.portals = Array.isArray(portals) ? portals : [];
                renderRpgPortals(rpgState.portals);
            }
        } catch (err) {
            console.error('Failed to load portals:', err);
        }
    }

    function renderRpgPortals(portals) {
        if (!els.rpgPortalList) return;
        if (portals.length === 0) {
            els.rpgPortalList.innerHTML = '<div style="color:#9aa0a6;font-size:0.85rem;padding:8px;">No portals available</div>';
            return;
        }
        els.rpgPortalList.innerHTML = portals.map(p => `
            <div class="rpg-location-item">
                <div class="loc-name">${p.name || 'Unknown'}</div>
                <div class="loc-detail">→ ${p.portalGalaxy || 'Unknown Galaxy'}</div>
            </div>
        `).join('');
    }

    // Enter FPS mode from RPG HUD
    function enterFpsMode() {
        rpgState.currentMode = 'fps';
        if (els.rpgHud) els.rpgHud.classList.add('hidden');
        if (els.fpsCanvas) els.fpsCanvas.classList.add('active');
        if (els.fpsHud) els.fpsHud.classList.add('active');
        if (els.lobbyOverlay) els.lobbyOverlay.classList.add('active');
        loadLobbyData();
    }

    // Return to RPG HUD from FPS mode
    function returnToRpgHub() {
        rpgState.currentMode = 'rpg';
        if (els.rpgHud) els.rpgHud.classList.remove('hidden');
        if (els.fpsCanvas) els.fpsCanvas.classList.remove('active');
        if (els.fpsHud) els.fpsHud.classList.remove('active');
        if (els.lobbyOverlay) els.lobbyOverlay.classList.remove('active');
        // Reset FPS state
        gameState.inMatch = false;
        if (ws) { ws.close(); ws = null; }
    }

    // ============================================================
    // RPG Event Bindings
    // ============================================================
    function bindRpgEvents() {
        // Top bar buttons
        if (els.rpgBtnRefresh) els.rpgBtnRefresh.addEventListener('click', loadRpgHub);
        if (els.rpgBtnHQ) els.rpgBtnHQ.addEventListener('click', () => {
            window.location.href = 'younger-hq.html';
        });
        if (els.rpgBtnOffice) els.rpgBtnOffice.addEventListener('click', () => {
            window.location.href = 'game.html';
        });
        if (els.rpgBtnLogout) els.rpgBtnLogout.addEventListener('click', () => {
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            window.location.href = 'index.html';
        });

        // Game mode cards
        if (els.rpgModeGrid) {
            els.rpgModeGrid.querySelectorAll('.rpg-mode-card').forEach(card => {
                card.addEventListener('click', () => {
                    const mode = card.dataset.mode;
                    if (mode === 'fps') {
                        enterFpsMode();
                    } else if (mode === 'corp') {
                        window.location.href = 'game.html';
                    } else if (mode === 'rpg' || mode === 'explore') {
                        rpgToast('RPG Adventure mode — select a scenario to begin', 'info');
                    }
                });
            });
        }
    }

    function rpgToast(message, type = 'info') {
        if (!els.rpgToastContainer) return;
        const toast = document.createElement('div');
        toast.className = `rpg-toast ${type}`;
        toast.textContent = message;
        els.rpgToastContainer.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
    }

    // ============================================================
    // RPG Actions
    // ============================================================
    async function moveToLocation(characterId, location) {
        try {
            await fetch(`${API_BASE}/earth2026/characters/${characterId}/location`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ location })
            });
            rpgToast(`Moved to ${location}`, 'success');
            await loadRpgCharacter();
        } catch (err) {
            console.error('Failed to move character:', err);
            rpgToast('Failed to move character', 'error');
        }
    }

    async function startScenarioAction(characterId, scenarioId) {
        try {
            const response = await fetch(`${API_BASE}/earth2026/scenarios/${scenarioId}/start`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            });
            if (response.ok) {
                rpgToast('Scenario started! The adventure begins...', 'success');
                await loadRpgScenarios();
            } else {
                const err = await response.json().catch(() => ({}));
                rpgToast('Cannot start scenario: ' + (err.error || 'Unknown error'), 'error');
            }
        } catch (err) {
            console.error('Failed to start scenario:', err);
            rpgToast('Failed to start scenario', 'error');
        }
    }

    // ============================================================
    // Lobby
    // ============================================================
    async function loadLobbyData() {
        showLoading('Loading battlefield data...');

        try {
            // Load maps
            const mapsRes = await fetch(`${API_BASE}/fps/maps?theme=Earth2026`);
            if (mapsRes.ok) {
                const maps = await mapsRes.json();
                renderMapGrid(maps);
            }

            // Load weapons
            const weaponsRes = await fetch(`${API_BASE}/fps/weapons`);
            if (weaponsRes.ok) {
                const weapons = await weaponsRes.json();
                renderWeaponGrid(weapons);
            }

            // Load active matches
            const matchesRes = await fetch(`${API_BASE}/fps/matches`);
            if (matchesRes.ok) {
                const matches = await matchesRes.json();
                renderMatchesList(matches);
            }

            // Load character info
            loadCharacterInfo();

        } catch (err) {
            console.error('Failed to load lobby data:', err);
        }

        hideLoading();
    }

    function renderMapGrid(maps) {
        if (!els.mapGrid) return;
        const icons = ['🏙️', '🏢', '🏭', '🏗️', '🌉', '🏟️'];
        els.mapGrid.innerHTML = maps.map((map, i) => `
            <div class="map-card ${i === 0 ? 'active' : ''}" data-map-id="${map.id}">
                <div class="map-icon">${icons[i % icons.length]}</div>
                <div class="map-name">${map.name || 'Unknown'}</div>
                <div class="map-desc">${map.description || ''}</div>
            </div>
        `).join('');

        els.mapGrid.querySelectorAll('.map-card').forEach(card => {
            card.addEventListener('click', () => {
                els.mapGrid.querySelectorAll('.map-card').forEach(c => c.classList.remove('active'));
                card.classList.add('active');
                gameState.map = maps.find(m => m.id === card.dataset.mapId);
            });
        });
    }

    function renderWeaponGrid(weapons) {
        if (!els.weaponGrid) return;
        const icons = {
            'PISTOL': '🔫', 'RIFLE': '🔫', 'SHOTGUN': '💥',
            'MACHINE_GUN': '⚡', 'SNIPER': '🎯', 'ROCKET_LAUNCHER': '🚀', 'GRENADE': '💣'
        };
        els.weaponGrid.innerHTML = weapons.map(w => `
            <div class="weapon-card" data-weapon-id="${w.id}">
                <div class="weapon-card-icon">${icons[w.type] || '🔫'}</div>
                <div class="weapon-card-name">${w.name}</div>
                <div class="weapon-card-type">${w.type}</div>
            </div>
        `).join('');

        els.weaponGrid.querySelectorAll('.weapon-card').forEach(card => {
            card.addEventListener('click', () => {
                els.weaponGrid.querySelectorAll('.weapon-card').forEach(c => c.classList.remove('selected'));
                card.classList.add('selected');
            });
        });
    }

    function renderMatchesList(matches) {
        if (!els.matchesList) return;
        if (matches.length === 0) {
            els.matchesList.innerHTML = '<div class="matches-empty">No active matches</div>';
            return;
        }
        els.matchesList.innerHTML = matches.map(m => `
            <div class="match-item" data-room-id="${m.roomId}">
                <div class="match-item-info">
                    <div class="match-item-name">${m.mode || 'DEATHMATCH'}</div>
                    <div class="match-item-details">${m.map || 'Unknown Map'}</div>
                </div>
                <div class="match-item-players">${m.playerCount || 0}/${m.maxPlayers || 8}</div>
            </div>
        `).join('');

        els.matchesList.querySelectorAll('.match-item').forEach(item => {
            item.addEventListener('click', () => {
                joinMatch(item.dataset.roomId);
            });
        });
    }

    async function loadCharacterInfo() {
        const charId = localStorage.getItem('selected_char_id');
        if (!charId) {
            gameState.playerName = 'Player_' + Math.floor(Math.random() * 9999);
            gameState.playerId = gameState.playerName;
            updateLobbyCharacter();
            return;
        }

        try {
            const res = await fetch(`${API_BASE}/earth2026/characters/${charId}`);
            if (res.ok) {
                const char = await res.json();
                gameState.playerName = char.name || 'Player';
                gameState.playerId = charId;
                updateLobbyCharacter();
            }
        } catch (err) {
            console.error('Failed to load character:', err);
        }
    }

    function updateLobbyCharacter() {
        if (els.lobbyCharName) els.lobbyCharName.textContent = gameState.playerName;
        if (els.lobbyCharLevel) els.lobbyCharLevel.textContent = '--';
        if (els.lobbyCharLocation) els.lobbyCharLocation.textContent = 'San Diego, CA';
    }

    // ============================================================
    // Match Management
    // ============================================================
    async function createMatch() {
        showLoading('Creating match...');

        try {
            const mode = document.querySelector('.mode-card.active')?.dataset.mode || 'DEATHMATCH';
            const res = await fetch(`${API_BASE}/fps/matches?playerId=${gameState.playerId}&playerName=${encodeURIComponent(gameState.playerName)}&mode=${mode}`, {
                method: 'POST'
            });

            if (res.ok) {
                const data = await res.json();
                gameState.matchId = data.roomId;
                gameState.gameMode = mode;
                showToast('Match created! Connecting...', 'success');
                connectToFpsServer();
            } else {
                showToast('Failed to create match', 'error');
            }
        } catch (err) {
            console.error('Create match failed:', err);
            showToast('Failed to create match', 'error');
        }

        hideLoading();
    }

    async function joinMatch(roomId) {
        showLoading('Joining match...');

        try {
            const res = await fetch(`${API_BASE}/fps/matches/${roomId}/join?playerId=${gameState.playerId}&playerName=${encodeURIComponent(gameState.playerName)}`, {
                method: 'POST'
            });

            if (res.ok) {
                gameState.matchId = roomId;
                showToast('Joined match! Connecting...', 'success');
                connectToFpsServer();
            } else {
                showToast('Failed to join match', 'error');
            }
        } catch (err) {
            console.error('Join match failed:', err);
            showToast('Failed to join match', 'error');
        }

        hideLoading();
    }

    async function quickPlay() {
        showLoading('Finding match...');

        try {
            // Try to join an existing match
            const matchesRes = await fetch(`${API_BASE}/fps/matches`);
            let matches = [];
            if (matchesRes.ok) matches = await matchesRes.json();

            if (matches.length > 0) {
                const match = matches[0];
                await joinMatch(match.roomId);
            } else {
                // Create a new match
                await createMatch();
            }
        } catch (err) {
            console.error('Quick play failed:', err);
            showToast('Failed to find match', 'error');
            hideLoading();
        }
    }

    // ============================================================
    // WebSocket Connection
    // ============================================================
    function connectToFpsServer() {
        if (ws) {
            ws.close();
        }

        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
        const wsUrl = `${protocol}//${window.location.host}${WS_FPS}?sessionId=${gameState.matchId}&playerId=${gameState.playerId}&playerName=${encodeURIComponent(gameState.playerName)}&team=${gameState.team}`;

        try {
            ws = new WebSocket(wsUrl);

            ws.onopen = () => {
                console.log('Connected to FPS server');
                enterMatch();
            };

            ws.onmessage = (event) => {
                try {
                    const data = JSON.parse(event.data);
                    handleWsMessage(data);
                } catch (err) {
                    console.error('Failed to parse WS message:', err);
                }
            };

            ws.onclose = () => {
                console.log('Disconnected from FPS server');
                if (gameState.inMatch && !gameState.paused) {
                    showToast('Disconnected from server', 'error');
                    // Attempt reconnect
                    wsReconnectTimer = setTimeout(() => {
                        if (gameState.inMatch) connectToFpsServer();
                    }, 3000);
                }
            };

            ws.onerror = (err) => {
                console.error('WebSocket error:', err);
            };
        } catch (err) {
            console.error('Failed to connect to FPS server:', err);
            showToast('Failed to connect to server', 'error');
            hideLoading();
        }
    }

    function handleWsMessage(data) {
        switch (data.type) {
            case 'CONNECT_ACK':
                gameState.playerId = data.playerId;
                gameState.team = data.team;
                if (data.weapons) {
                    gameState.weapons = data.weapons;
                    updateWeaponSlots();
                }
                break;

            case 'STATE':
                updateGameState(data);
                break;

            case 'MATCH_STARTED':
                showCenterMessage('MATCH STARTED');
                break;

            case 'PLAYER_JOINED':
                addChatMessage('System', `${data.playerName} joined the match`);
                break;

            case 'PLAYER_LEFT':
                addChatMessage('System', `${data.playerName} left the match`);
                break;

            case 'KILL':
                addKillFeedEntry(data.killerName, data.weapon, data.victimName);
                if (data.victimId === gameState.playerId) {
                    onPlayerDeath(data.killerName, data.weapon);
                }
                break;

            case 'DAMAGE':
                if (data.victimId === gameState.playerId) {
                    showDamageIndicator();
                }
                if (data.attackerId === gameState.playerId) {
                    showHitMarker();
                }
                break;

            case 'RESPAWN':
                onPlayerRespawn();
                break;

            case 'MATCH_ENDED':
                onMatchEnd(data);
                break;

            case 'CHAT':
                addChatMessage(data.playerName, data.message);
                break;

            default:
                console.log('Unknown WS message:', data.type, data);
        }
    }

    function updateGameState(data) {
        // Update player states
        if (data.players) {
            gameState.players = {};
            data.players.forEach(p => {
                gameState.players[p.id] = {
                    posX: p.posX || 0,
                    posZ: p.posZ || 0,
                    health: p.health || 0,
                    team: p.team || 0,
                    kills: p.kills || 0,
                    deaths: p.deaths || 0,
                    name: p.name || 'Unknown'
                };

                // Update own stats
                if (p.id === gameState.playerId) {
                    gameState.health = p.health || 100;
                    gameState.maxHealth = p.maxHealth || 100;
                    gameState.ammo = p.ammo || 30;
                    gameState.reserveAmmo = p.reserveAmmo || 120;
                    gameState.kills = p.kills || 0;
                    gameState.deaths = p.deaths || 0;
                    gameState.position = {
                        x: p.posX || 0,
                        y: p.posY || 1.7,
                        z: p.posZ || 0
                    };
                    updateHud();
                }
            });
        }

        // Update match time
        if (data.tick != null) {
            gameState.matchTime = data.tick;
            if (els.matchTimer) {
                const seconds = Math.floor(data.tick / 1000);
                const mins = Math.floor(seconds / 60).toString().padStart(2, '0');
                const secs = (seconds % 60).toString().padStart(2, '0');
                els.matchTimer.textContent = `${mins}:${secs}`;
            }
        }
    }

    function sendWsMessage(data) {
        if (ws && ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify(data));
        }
    }

    // ============================================================
    // HUD Updates
    // ============================================================
    function updateHud() {
        // Health
        if (els.healthBar) {
            const healthPercent = (gameState.health / gameState.maxHealth) * 100;
            els.healthBar.style.width = healthPercent + '%';
            els.healthBar.style.background = healthPercent < 30 ?
                'linear-gradient(90deg, #ff3333, #cc0000)' :
                'linear-gradient(90deg, #00ff88, #00cc66)';
        }
        if (els.healthValue) els.healthValue.textContent = gameState.health;

        // Armor
        if (els.armorBar) els.armorBar.style.width = (gameState.armor / 100 * 100) + '%';
        if (els.armorValue) els.armorValue.textContent = gameState.armor;

        // Player info
        if (els.playerName) els.playerName.textContent = gameState.playerName;
        if (els.playerTeam) els.playerTeam.textContent = 'Team ' + gameState.team;
        if (els.playerKills) els.playerKills.textContent = 'K: ' + gameState.kills;
        if (els.playerDeaths) els.playerDeaths.textContent = 'D: ' + gameState.deaths;
        if (els.playerAssists) els.playerAssists.textContent = 'A: ' + gameState.assists;

        // Ammo
        if (els.ammoMag) els.ammoMag.textContent = gameState.ammo;
        if (els.ammoReserve) els.ammoReserve.textContent = gameState.reserveAmmo;

        // Weapon
        if (gameState.currentWeapon) {
            if (els.weaponName) els.weaponName.textContent = gameState.currentWeapon.name || '';
            if (els.weaponType) els.weaponType.textContent = gameState.currentWeapon.type || '';
        }
    }

    function updateWeaponSlots() {
        if (!els.weaponSlots) return;
        els.weaponSlots.innerHTML = gameState.weapons.map((w, i) => `
            <div class="weapon-slot ${i === 0 ? 'active' : ''}" data-index="${i}">${i + 1}</div>
        `).join('');

        els.weaponSlots.querySelectorAll('.weapon-slot').forEach(slot => {
            slot.addEventListener('click', () => {
                const idx = parseInt(slot.dataset.index);
                els.weaponSlots.querySelectorAll('.weapon-slot').forEach(s => s.classList.remove('active'));
                slot.classList.add('active');
                gameState.currentWeapon = gameState.weapons[idx];
                updateHud();
            });
        });

        // Set initial weapon
        if (gameState.weapons.length > 0) {
            gameState.currentWeapon = gameState.weapons[0];
            updateHud();
        }
    }

    function addKillFeedEntry(killer, weapon, victim) {
        if (!els.killFeed) return;
        const entry = document.createElement('div');
        entry.className = 'kill-feed-entry';
        entry.innerHTML = `
            <span class="kill-feed-killer">${killer}</span>
            <span class="kill-feed-weapon">[${weapon}]</span>
            <span class="kill-feed-victim">${victim}</span>
        `;
        els.killFeed.appendChild(entry);
        setTimeout(() => entry.remove(), 4000);
    }

    function showHitMarker() {
        if (!els.hitmarker) return;
        els.hitmarker.classList.remove('hidden');
        setTimeout(() => els.hitmarker.classList.add('hidden'), 150);
    }

    function showDamageIndicator() {
        if (!els.damageOverlay) return;
        els.damageOverlay.classList.add('active');
        setTimeout(() => els.damageOverlay.classList.remove('active'), 200);
    }

    function showCenterMessage(text) {
        if (!els.centerMessage || !els.centerMessageText) return;
        els.centerMessageText.textContent = text;
        els.centerMessage.classList.remove('hidden');
        setTimeout(() => els.centerMessage.classList.add('hidden'), 2000);
    }

    function addChatMessage(name, message) {
        if (!els.chatMessages) return;
        const msg = document.createElement('div');
        msg.className = 'chat-message';
        msg.innerHTML = `<span class="chat-name">${name}:</span> ${message}`;
        els.chatMessages.appendChild(msg);
        els.chatMessages.scrollTop = els.chatMessages.scrollHeight;
    }

    // ============================================================
    // Match Flow
    // ============================================================
    function enterMatch() {
        gameState.inMatch = true;
        gameState.paused = false;
        gameState.dead = false;

        // Hide lobby, show HUD
        if (els.lobbyOverlay) els.lobbyOverlay.classList.add('hidden');
        if (els.loadingOverlay) els.loadingOverlay.classList.add('hidden');

        // Request pointer lock
        canvas.requestPointerLock();

        // Send ready
        sendWsMessage({ type: 'READY', sessionId: gameState.matchId });
    }

    function onPlayerDeath(killerName, weaponName) {
        gameState.dead = true;
        if (els.deathOverlay) els.deathOverlay.classList.remove('hidden');
        if (els.deathKiller) els.deathKiller.textContent = `Killed by: ${killerName}`;
        if (els.deathWeapon) els.deathWeapon.textContent = `Weapon: ${weaponName}`;

        // Auto respawn countdown
        let countdown = 5;
        if (els.respawnCountdown) els.respawnCountdown.textContent = countdown;
        const timer = setInterval(() => {
            countdown--;
            if (els.respawnCountdown) els.respawnCountdown.textContent = countdown;
            if (countdown <= 0) {
                clearInterval(timer);
                respawnPlayer();
            }
        }, 1000);
    }

    function onPlayerRespawn() {
        gameState.dead = false;
        gameState.health = gameState.maxHealth;
        gameState.ammo = gameState.currentWeapon ? gameState.currentWeapon.magSize || 30 : 30;
        if (els.deathOverlay) els.deathOverlay.classList.add('hidden');
        updateHud();
        showCenterMessage('RESPAWNED');
    }

    function respawnPlayer() {
        sendWsMessage({ type: 'INPUT', sessionId: gameState.matchId, actions: ['RESPAWN'] });
    }

    function onMatchEnd(data) {
        gameState.inMatch = false;
        if (els.matchEndOverlay) els.matchEndOverlay.classList.remove('hidden');
        if (els.matchEndTitle) els.matchEndTitle.textContent = data.winner ? 'VICTORY' : 'DEFEAT';

        const stats = document.getElementById('matchEndStats');
        if (stats) {
            stats.innerHTML = `
                <div>Kills: ${gameState.kills}</div>
                <div>Deaths: ${gameState.deaths}</div>
                <div>Assists: ${gameState.assists}</div>
                <div>K/D Ratio: ${(gameState.deaths > 0 ? gameState.kills / gameState.deaths : gameState.kills).toFixed(2)}</div>
            `;
        }
    }

    function pauseMatch() {
        gameState.paused = !gameState.paused;
        if (els.pauseOverlay) {
            els.pauseOverlay.classList.toggle('hidden', !gameState.paused);
        }
        if (gameState.paused) {
            document.exitPointerLock();
        } else {
            canvas.requestPointerLock();
        }
    }

    function leaveMatch() {
        gameState.inMatch = false;
        gameState.paused = false;
        gameState.dead = false;

        if (ws) { ws.close(); ws = null; }
        if (wsReconnectTimer) clearTimeout(wsReconnectTimer);

        if (els.pauseOverlay) els.pauseOverlay.classList.add('hidden');
        if (els.deathOverlay) els.deathOverlay.classList.add('hidden');
        if (els.matchEndOverlay) els.matchEndOverlay.classList.add('hidden');

        if (document.pointerLockElement) document.exitPointerLock();

        // Return to RPG HUD instead of FPS lobby
        returnToRpgHub();
    }

    // ============================================================
    // Input Handling
    // ============================================================
    function bindInputEvents() {
        // Keyboard
        document.addEventListener('keydown', (e) => {
            keys[e.code] = true;

            // Chat
            if (e.code === 'Enter' && els.chatInput) {
                e.preventDefault();
                const msg = els.chatInput.value.trim();
                if (msg && ws) {
                    sendWsMessage({ type: 'CHAT', sessionId: gameState.matchId, message: msg });
                    els.chatInput.value = '';
                }
                return;
            }

            // Pause
            if (e.code === 'Escape' && gameState.inMatch) {
                pauseMatch();
                return;
            }

            // Reload
            if (e.code === 'KeyR' && gameState.inMatch && !gameState.dead) {
                sendWsMessage({ type: 'INPUT', sessionId: gameState.matchId, actions: ['RELOAD'] });
            }

            // Weapon switch
            if (e.code >= 'Digit1' && e.code <= 'Digit9' && gameState.inMatch) {
                const idx = parseInt(e.code.replace('Digit', '')) - 1;
                if (idx < gameState.weapons.length) {
                    gameState.currentWeapon = gameState.weapons[idx];
                    updateWeaponSlots();
                    updateHud();
                }
            }
        });

        document.addEventListener('keyup', (e) => {
            keys[e.code] = false;
        });

        // Mouse
        document.addEventListener('mousemove', (e) => {
            if (pointerLocked && gameState.inMatch && !gameState.paused && !gameState.dead) {
                gameState.yaw += e.movementX * 0.002;
                gameState.pitch -= e.movementY * 0.002;
                gameState.pitch = Math.max(-Math.PI / 2, Math.min(Math.PI / 2, gameState.pitch));
            }
        });

        document.addEventListener('mousedown', (e) => {
            if (e.button === 0) mouse.left = true;
            if (e.button === 2) mouse.right = true;

            // Shoot
            if (e.button === 0 && gameState.inMatch && !gameState.paused && !gameState.dead) {
                sendWsMessage({
                    type: 'INPUT',
                    sessionId: gameState.matchId,
                    actions: ['SHOOT']
                });
            }
        });

        document.addEventListener('mouseup', (e) => {
            if (e.button === 0) mouse.left = false;
            if (e.button === 2) mouse.right = false;
        });

        document.addEventListener('contextmenu', (e) => e.preventDefault());

        // Pointer lock
        document.addEventListener('pointerlockchange', () => {
            pointerLocked = document.pointerLockElement === canvas;
        });

        // Continuous input
        setInterval(() => {
            if (!gameState.inMatch || gameState.paused || gameState.dead) return;

            let moveX = 0, moveZ = 0;
            if (keys['KeyW']) moveZ -= 1;
            if (keys['KeyS']) moveZ += 1;
            if (keys['KeyA']) moveX -= 1;
            if (keys['KeyD']) moveX += 1;

            const actions = [];
            if (keys['ShiftLeft']) actions.push('SPRINT');
            if (keys['ControlLeft']) actions.push('CROUCH');
            if (keys['Space']) actions.push('JUMP');
            if (mouse.right) actions.push('AIM');

            sendWsMessage({
                type: 'INPUT',
                sessionId: gameState.matchId,
                timestamp: Date.now(),
                move: { x: moveX, z: moveZ },
                aim: { pitch: gameState.pitch, yaw: gameState.yaw },
                actions: actions
            });
        }, 50);
    }

    // ============================================================
    // Event Bindings
    // ============================================================
    function bindLobbyEvents() {
        if (els.btnCreateMatch) els.btnCreateMatch.addEventListener('click', createMatch);
        if (els.btnQuickPlay) els.btnQuickPlay.addEventListener('click', quickPlay);
        if (els.btnBackToOffice) els.btnBackToOffice.addEventListener('click', () => {
            returnToRpgHub();
        });
        if (els.btnEnterHQ) els.btnEnterHQ.addEventListener('click', () => {
            window.location.href = 'younger-hq.html';
        });

        // Mode selection
        if (els.modeGrid) {
            els.modeGrid.querySelectorAll('.mode-card').forEach(card => {
                card.addEventListener('click', () => {
                    els.modeGrid.querySelectorAll('.mode-card').forEach(c => c.classList.remove('active'));
                    card.classList.add('active');
                });
            });
        }
    }

    function bindGameEvents() {
        if (els.btnPause) els.btnPause.addEventListener('click', pauseMatch);
        if (els.btnMenu) els.btnMenu.addEventListener('click', pauseMatch);
        if (els.btnResume) els.btnResume.addEventListener('click', pauseMatch);
        if (els.btnRestart) els.btnRestart.addEventListener('click', () => {
            leaveMatch();
            quickPlay();
        });
        if (els.btnLeaveMatch) els.btnLeaveMatch.addEventListener('click', leaveMatch);
        if (els.btnRespawn) els.btnRespawn.addEventListener('click', respawnPlayer);
        if (els.btnPlayAgain) els.btnPlayAgain.addEventListener('click', () => {
            leaveMatch();
            quickPlay();
        });
        if (els.btnBackToLobby) els.btnBackToLobby.addEventListener('click', leaveMatch);

        // Chat input
        if (els.chatInput) {
            els.chatInput.addEventListener('keydown', (e) => {
                if (e.code === 'Enter') {
                    e.preventDefault();
                    const msg = els.chatInput.value.trim();
                    if (msg && ws) {
                        sendWsMessage({ type: 'CHAT', sessionId: gameState.matchId, message: msg });
                        els.chatInput.value = '';
                    }
                }
            });
        }
    }

    // ============================================================
    // Utilities
    // ============================================================
    function showLoading(text) {
        if (els.loadingText) els.loadingText.textContent = text || 'Loading...';
        if (els.loadingOverlay) els.loadingOverlay.classList.remove('hidden');
    }

    function hideLoading() {
        if (els.loadingOverlay) els.loadingOverlay.classList.add('hidden');
    }

    function showToast(message, type = 'info') {
        if (!els.toastContainer) return;
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        els.toastContainer.appendChild(toast);
        setTimeout(() => toast.remove(), 3000);
    }

    // ============================================================
    // Public API
    // ============================================================
    return {
        init,
        getState: () => gameState
    };
})();

// ============================================================
// Auto-initialize on DOM ready
// ============================================================
document.addEventListener('DOMContentLoaded', () => {
    Earth2026FpsClient.init();
});

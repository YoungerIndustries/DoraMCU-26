// Game Launcher — Main UI logic
// Uses window.electronAPI exposed by preload.js (contextIsolation: true)
// authService is provided by auth-service.js as window.authService (browser-compatible)

// Tab Navigation
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initConnectButton();
    initGameClientControls();
    initSessionControls();
    initGalaxyControls();
    initSettingsControls();
    initModal();
    initGameClientStatusListener();
    initFpsControls();
    refreshDashboard();

    // Initialize Home tab modules on page load if Home is the default active tab
    const activeNav = document.querySelector('.nav-item.active');
    if (activeNav && activeNav.dataset.tab === 'home') {
        if (window.initOnboarding && !window._onboardingInitialized) {
            window._onboardingInitialized = true;
            window.initOnboarding();
        }
        if (window.initNewsFeed && !window._newsFeedInitialized) {
            window._newsFeedInitialized = true;
            window.initNewsFeed();
        }
    }
});

function initTabs() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const tabId = item.dataset.tab;
            switchTab(tabId);
        });
    });
}

function switchTab(tabId) {
    document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
    
    const navItem = document.querySelector(`.nav-item[data-tab="${tabId}"]`);
    const tabContent = document.getElementById(tabId);
    
    if (navItem) navItem.classList.add('active');
    if (tabContent) tabContent.classList.add('active');

    // Dispatch custom event for tab-specific initialization
    document.dispatchEvent(new CustomEvent('tabshown', { detail: { tab: tabId } }));

    // Initialize Home tab modules on first visit
    if (tabId === 'home') {
        if (window.initOnboarding && !window._onboardingInitialized) {
            window._onboardingInitialized = true;
            window.initOnboarding();
        }
        if (window.initNewsFeed && !window._newsFeedInitialized) {
            window._newsFeedInitialized = true;
            window.initNewsFeed();
        }
    }

    // Initialize HUD tab on first visit
    if (tabId === 'hud') {
        if (window.HUDController && !window._hudInitialized) {
            window._hudInitialized = true;
            initHudControls();
        }
    }

    // Initialize Voice Settings Panel on Settings tab visit
    if (tabId === 'settings') {
        if (window.voiceSettingsPanel && !window._voiceSettingsInitialized) {
            window._voiceSettingsInitialized = true;
            window.voiceSettingsPanel.init();
        }
    }

}

function initConnectButton() {
    const connectBtn = document.getElementById('connectBtn');
    if (connectBtn) {
        connectBtn.addEventListener('click', async () => {
            connectBtn.textContent = 'Connecting...';
            connectBtn.disabled = true;
            
            const result = await window.authService.connect();
            
            if (result.success) {
                connectBtn.textContent = 'Connected';
                updateUserStatus(true, 'Guest');
                addActivity('Connected to server');
                refreshDashboard();
            } else {
                connectBtn.textContent = 'Connect to Server';
                connectBtn.disabled = false;
                updateUserStatus(false, 'Guest');
                addActivity('Failed to connect: ' + result.error);
            }
        });
    }
}

function initGameClientControls() {
    // Guard: electronAPI may not be available in browser context
    if (!window.electronAPI) {
        console.warn('electronAPI not available — game client controls disabled');
        return;
    }

    const launchBtn = document.getElementById('launchGameBtn');
    const startBtn = document.getElementById('startClientBtn');
    const stopBtn = document.getElementById('stopClientBtn');
    
    if (launchBtn) {
        launchBtn.addEventListener('click', async () => {
            launchBtn.textContent = 'Launching...';
            launchBtn.disabled = true;
            try {
                const result = await window.electronAPI.downloadAndStartGameClient(window.authService.getServerUrl());
                if (result.success) {
                    addActivity('Game client launched');
                } else {
                    addActivity('Failed to launch: ' + result.error);
                }
            } catch (e) {
                addActivity('Failed to launch: ' + e.message);
            }
            launchBtn.textContent = 'Launch Game Client';
            launchBtn.disabled = false;
        });
    }
    
    if (startBtn) {
        startBtn.addEventListener('click', async () => {
            try {
                await window.electronAPI.startGameClient();
                addActivity('Game client started');
            } catch (e) {
                addActivity('Failed to start client: ' + e.message);
            }
        });
    }
    
    if (stopBtn) {
        stopBtn.addEventListener('click', async () => {
            try {
                await window.electronAPI.stopGameClient();
                addActivity('Game client stopped');
            } catch (e) {
                addActivity('Failed to stop client: ' + e.message);
            }
        });
    }
}

function initSessionControls() {
    const createBtn = document.getElementById('createSessionBtn');
    const refreshBtn = document.getElementById('refreshSessionsBtn');
    
    if (createBtn) {
        createBtn.addEventListener('click', async () => {
            const sessionName = prompt('Enter session name:');
            if (sessionName) {
                try {
                    await window.authService.fetchJson('/api/v1/4x/sessions', {
                        method: 'POST',
                        body: JSON.stringify({ sessionName })
                    });
                    addActivity('Session created: ' + sessionName);
                    refreshSessions();
                } catch (error) {
                    addActivity('Failed to create session: ' + error.message);
                }
            }
        });
    }
    
    if (refreshBtn) {
        refreshBtn.addEventListener('click', refreshSessions);
    }
}

function initGalaxyControls() {
    const select = document.getElementById('galaxySelect');
    if (select) {
        select.addEventListener('change', async () => {
            const galaxyName = select.value;
            if (galaxyName) {
                await loadGalaxyInfo(galaxyName);
            }
        });
    }
}

function initSettingsControls() {
    const saveBtn = document.getElementById('saveSettingsBtn');
    const serverUrlInput = document.getElementById('serverUrl');
    
    if (saveBtn) {
        saveBtn.addEventListener('click', () => {
            const url = serverUrlInput.value.trim();
            if (url) {
                window.authService.setServerUrl(url);
                addActivity('Server URL updated to: ' + url);
            }
        });
    }
}

function initModal() {
    const modal = document.getElementById('statusModal');
    const closeBtn = document.getElementById('closeModal');
    
    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            modal.classList.remove('active');
        });
    }
    
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });
    }
}

function initGameClientStatusListener() {
    // Guard: electronAPI may not be available in browser context
    if (!window.electronAPI) {
        console.warn('electronAPI not available — game client status listener disabled');
        return;
    }

    if (window.electronAPI.onGameClientDeploymentStatus) {
        window.electronAPI.onGameClientDeploymentStatus((data) => {
            updateClientStatus(data.status, data.message);
            if (data.progress !== undefined) {
                addActivity(`Download progress: ${data.progress}%`);
            }
        });
    }

    if (window.electronAPI.onGameClientStatusUpdate) {
        window.electronAPI.onGameClientStatusUpdate((data) => {
            showModal('Game Client Status', JSON.stringify(data, null, 2));
        });
    }
}

// UI Update Functions

function updateUserStatus(connected, name) {
    const statusEl = document.getElementById('userStatus');
    const nameEl = document.getElementById('userName');
    
    if (statusEl) {
        statusEl.textContent = connected ? 'Connected' : 'Disconnected';
        statusEl.className = 'user-status' + (connected ? ' connected' : '');
    }
    if (nameEl) nameEl.textContent = name || 'Guest';
}

function updateClientStatus(status, message) {
    const statusBar = document.getElementById('clientStatusBar');
    const statusEl = document.getElementById('gameClientStatus');
    
    if (statusBar) {
        const indicator = statusBar.querySelector('.status-indicator');
        const text = statusBar.querySelector('.status-text');
        
        if (indicator) {
            indicator.className = 'status-indicator';
            if (status === 'started' || status === 'running') {
                indicator.classList.add('connected');
            } else if (status === 'downloading' || status === 'starting') {
                indicator.classList.add('downloading');
            } else {
                indicator.classList.add('disconnected');
            }
        }
        if (text) text.textContent = message || status;
    }
    
    if (statusEl) {
        const statusMap = {
            'started': 'Running',
            'running': 'Running',
            'stopped': 'Stopped',
            'stopping': 'Stopping...',
            'starting': 'Starting...',
            'downloading': 'Downloading...',
            'error': 'Error'
        };
        statusEl.textContent = statusMap[status] || status;
    }
}

function addActivity(message) {
    const list = document.getElementById('activityList');
    if (!list) return;
    
    const emptyState = list.querySelector('.empty-state');
    if (emptyState) emptyState.remove();
    
    const item = document.createElement('div');
    item.className = 'activity-item';
    const time = new Date().toLocaleTimeString();
    item.textContent = `[${time}] ${message}`;
    
    list.insertBefore(item, list.firstChild);
    
    // Keep only last 20 items
    while (list.children.length > 20) {
        list.removeChild(list.lastChild);
    }
}

function showModal(title, body) {
    const modal = document.getElementById('statusModal');
    const modalBody = document.getElementById('modalBody');
    
    if (modal && modalBody) {
        modal.querySelector('h3').textContent = title;
        modalBody.innerHTML = `<pre style="white-space: pre-wrap; word-wrap: break-word;">${body}</pre>`;
        modal.classList.add('active');
    }
}

// Data Refresh Functions

async function refreshDashboard() {
    if (!window.authService.connected) return;
    
    try {
        // Fetch sessions
        const sessions = await window.authService.fetchJson('/api/v1/4x/sessions').catch(() => []);
        document.getElementById('activeGamesCount').textContent = sessions.length || 0;
        
        // Fetch galaxies
        const galaxies = await window.authService.fetchJson('/api/v1/galaxies').catch(() => []);
        document.getElementById('galaxiesCount').textContent = galaxies.length || 0;
        
        // Update galaxy dropdown
        const select = document.getElementById('galaxySelect');
        if (select && galaxies.length) {
            select.innerHTML = '<option value="">Select a galaxy...</option>';
            galaxies.forEach(g => {
                const option = document.createElement('option');
                option.value = g.name;
                option.textContent = g.name;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Failed to refresh dashboard:', error);
    }
}

async function refreshSessions() {
    if (!window.authService.connected) return;
    
    const list = document.getElementById('sessionsList');
    if (!list) return;
    
    try {
        const sessions = await window.authService.fetchJson('/api/v1/4x/sessions').catch(() => []);
        
        if (!sessions.length) {
            list.innerHTML = '<p class="empty-state">No active sessions</p>';
            return;
        }
        
        list.innerHTML = sessions.map(s => `
            <div class="session-item">
                <div class="session-info">
                    <h4>${s.sessionName || s.name || 'Unknown'}</h4>
                    <p>ID: ${s.sessionId || 'N/A'} | Players: ${s.playerCount || 0}</p>
                </div>
                <div class="session-actions">
                    <button class="btn btn-secondary" onclick="joinSession('${s.sessionId}')">Join</button>
                </div>
            </div>
        `).join('');
        
        document.getElementById('onlinePlayersCount').textContent = sessions.reduce((sum, s) => sum + (s.playerCount || 0), 0);
    } catch (error) {
        list.innerHTML = '<p class="empty-state">Failed to load sessions</p>';
    }
}

async function loadGalaxyInfo(galaxyName) {
    const info = document.getElementById('galaxyInfo');
    if (!info) return;
    
    try {
        const galaxy = await window.authService.fetchJson(`/api/v1/galaxies/${galaxyName}`).catch(() => null);
        
        if (!galaxy) {
            info.innerHTML = '<div class="card"><p class="empty-state">Galaxy not found</p></div>';
            return;
        }
        
        info.innerHTML = `
            <div class="card">
                <h3>${galaxy.name}</h3>
                <p style="color: var(--text-secondary); margin-bottom: 16px;">${galaxy.description || 'No description'}</p>
                <div class="galaxy-stats">
                    <div class="galaxy-stat">
                        <div class="value">${galaxy.totalSectors || 100}</div>
                        <div class="label">Sectors</div>
                    </div>
                    <div class="galaxy-stat">
                        <div class="value">${galaxy.solarSystems ? galaxy.solarSystems.length : 0}</div>
                        <div class="label">Solar Systems</div>
                    </div>
                    <div class="galaxy-stat">
                        <div class="value">${galaxy.gameType || 'N/A'}</div>
                        <div class="label">Game Type</div>
                    </div>
                    <div class="galaxy-stat">
                        <div class="value">${galaxy.difficultyLevel || 'N/A'}</div>
                        <div class="label">Difficulty</div>
                    </div>
                </div>
            </div>
        `;
    } catch (error) {
        info.innerHTML = '<div class="card"><p class="empty-state">Failed to load galaxy info</p></div>';
    }
}

// Global functions for inline event handlers
window.joinSession = async function(sessionId) {
    try {
        await window.authService.fetchJson(`/api/v1/4x/sessions/${sessionId}/join`, {
            method: 'POST'
        });
        addActivity('Joined session: ' + sessionId);
        refreshSessions();
    } catch (error) {
        addActivity('Failed to join session: ' + error.message);
    }
};

// FPS Controls
function initFpsControls() {
    const fpsGameCard = document.getElementById('fpsGameCard');
    const launchFpsWebBtn = document.getElementById('launchFpsWebBtn');
    const launchFpsUnityBtn = document.getElementById('launchFpsUnityBtn');
    const quickPlayWebBtn = document.getElementById('quickPlayWebBtn');
    const quickPlayUnityBtn = document.getElementById('quickPlayUnityBtn');

    // Click on FPS game card in Games tab → switch to FPS tab
    if (fpsGameCard) {
        fpsGameCard.addEventListener('click', () => {
            switchTab('fps');
        });
    }

    // Launch Web FPS in a new window/tab
    const launchWebFps = () => {
        const serverUrl = window.authService.getServerUrl().replace('/api/v1', '');
        window.open(`${serverUrl}/dora-pwa/fps/index.html`, '_blank');
    };

    if (launchFpsWebBtn) {
        launchFpsWebBtn.addEventListener('click', launchWebFps);
    }
    if (quickPlayWebBtn) {
        quickPlayWebBtn.addEventListener('click', launchWebFps);
    }

    // Launch Unity FPS
    const launchUnityFps = async () => {
        if (window.electronAPI) {
            try {
                await window.electronAPI.downloadAndStartGameClient(window.authService.getServerUrl());
                addActivity('Unity FPS client launched');
            } catch (e) {
                addActivity('Failed to launch Unity FPS: ' + e.message);
            }
        } else {
            addActivity('Unity FPS requires the Electron desktop client');
        }
    };

    if (launchFpsUnityBtn) {
        launchFpsUnityBtn.addEventListener('click', launchUnityFps);
    }
    if (quickPlayUnityBtn) {
        quickPlayUnityBtn.addEventListener('click', launchUnityFps);
    }

    // Load active FPS matches
    refreshFpsMatches();
}

async function refreshFpsMatches() {
    if (!window.authService.connected) return;

    const list = document.getElementById('fpsMatchesList');
    if (!list) return;

    try {
        const matches = await window.authService.fetchJson('/api/v1/fps/matches').catch(() => []);

        if (!matches.length) {
            list.innerHTML = '<p class="empty-state">No active matches. Create one to start playing!</p>';
            return;
        }

        list.innerHTML = matches.map(m => `
            <div class="session-item">
                <div class="session-info">
                    <h4>${m.mapName || 'Unknown Map'}</h4>
                    <p>Mode: ${m.mode || 'N/A'} | Players: ${m.playerCount || 0}</p>
                </div>
                <div class="session-actions">
                    <button class="btn btn-secondary" onclick="joinFpsMatch('${m.id}')">Join</button>
                </div>
            </div>
        `).join('');
    } catch (error) {
        list.innerHTML = '<p class="empty-state">Failed to load matches</p>';
    }
}

// Global function for FPS match joining
window.joinFpsMatch = async function(matchId) {
    try {
        await window.authService.fetchJson(`/api/v1/fps/matches/${matchId}/join`, {
            method: 'POST'
        });
        addActivity('Joined FPS match: ' + matchId);
        // Launch web FPS client
        const serverUrl = window.authService.getServerUrl().replace('/api/v1', '');
        window.open(`${serverUrl}/dora-pwa/fps/index.html?match=${matchId}`, '_blank');
    } catch (error) {
        addActivity('Failed to join FPS match: ' + error.message);
    }
};

// FPS match listing and joining
async function loadFpsMatches() {
    try {
        const matches = await window.authService.fetchJson('/api/v1/fps/matches');
        const list = document.getElementById('fpsMatchesList');
        if (list) {
            list.innerHTML = (matches || []).map(m => `
                <div class="session-item">
                    <div class="session-info">
                        <h4>${m.name || 'Match'}</h4>
                        <p>Mode: ${m.mode || 'N/A'} | Players: ${m.playerCount || 0}</p>
                    </div>
                    <div class="session-actions">
                        <button class="btn btn-secondary" onclick="joinFpsMatch('${m.id}')">Join</button>
                    </div>
                </div>
            `).join('');
        }
    } catch (error) {
        const list = document.getElementById('fpsMatchesList');
        if (list) list.innerHTML = '<p class="empty-state">Failed to load matches</p>';
    }
}

// HUD Controls — Initialize the Game HUD shell
function initHudControls() {
    // Guard: HUDController may not be available yet
    if (!window.HUDController) {
        console.warn('HUD: HUDController not available');
        return;
    }

    // Initialize the HUD controller
    const hudController = new window.HUDController();
    window.HUDController = hudController; // replace class with instance
    hudController.init();

    // Load built-in templates
    Promise.all([
        hudController.loadTemplatesFromPath('hud/templates/earth-2026.json'),
        hudController.loadTemplatesFromPath('hud/templates/fps.json'),
        hudController.loadTemplatesFromPath('hud/templates/captains-log.json'),
        hudController.loadTemplatesFromPath('hud/templates/dnd-5e.json'),
        hudController.loadTemplatesFromPath('hud/templates/traveller.json'),
        hudController.loadTemplatesFromPath('hud/templates/babylon5.json'),
        hudController.loadTemplatesFromPath('hud/templates/star-trek-4x.json'),
        hudController.loadTemplatesFromPath('hud/templates/mech-warrior.json'),
        hudController.loadTemplatesFromPath('hud/templates/zero-zero-seven.json'),
        hudController.loadTemplatesFromPath('hud/templates/star-wars.json'),
        hudController.loadTemplatesFromPath('hud/templates/unified-world.json'),
    ]).then(() => {
        // Load the default or saved theme
        const savedTheme = localStorage.getItem('hud-theme');
        if (savedTheme) {
            hudController.loadTemplate(savedTheme);
        } else {
            hudController.loadTemplate('earth-2026');
        }

        // Initialize and register game module adapters
        initHudAdapters();

        // Add initial event log entry
        hudController.addEventLogEntry('HUD initialized — select a theme to begin', 'success');
    }).catch(err => {
        console.warn('HUD: Failed to load templates', err);
    });
}

// HUD Adapters — Initialize game module adapters
function initHudAdapters() {
    // Earth 2026 Adapter
    if (window.Earth2026Adapter) {
        try {
            const apiBase = window.authService ? authService.getServerUrl() + '/earth2026' : '/api/v1/earth2026';
            const e2026Adapter = new window.Earth2026Adapter(apiBase);
            window.HUDController.setAdapter(e2026Adapter);
            e2026Adapter.init(window.HUDController);
            console.log('HUD: Earth 2026 adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize Earth 2026 adapter', err);
        }
    }

    // FPS Adapter
    if (window.FpsAdapter) {
        try {
            const apiBase = window.authService ? authService.getServerUrl() + '/fps' : '/api/v1/fps';
            const fpsAdapter = new window.FpsAdapter(apiBase);
            window.FpsAdapter = fpsAdapter; // store instance for resize notifications
            fpsAdapter.init(window.HUDController);
            console.log('HUD: FPS adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize FPS adapter', err);
        }
    }

    // Captain's Log Adapter
    if (window.CaptainsLogAdapter) {
        try {
            const apiBase = window.authService ? authService.getServerUrl() + '/captains-log' : '/api/v1/captains-log';
            const clAdapter = new window.CaptainsLogAdapter(apiBase);
            clAdapter.init(window.HUDController);
            console.log('HUD: Captain\'s Log adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize Captain\'s Log adapter', err);
        }
    }

    // D&D 5e Adapter
    if (window.Dnd5eAdapter) {
        try {
            const apiBase = window.authService ? authService.getServerUrl() + '/dnd5e' : '/api/v1/dnd5e';
            const dndAdapter = new window.Dnd5eAdapter(apiBase);
            dndAdapter.init(window.HUDController);
            console.log('HUD: D&D 5e adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize D&D 5e adapter', err);
        }
    }

    // Traveller Adapter
    if (window.TravellerAdapter) {
        try {
            const apiBase = window.authService ? authService.getServerUrl() + '/traveller' : '/api/v1/traveller';
            const travellerAdapter = new window.TravellerAdapter(apiBase);
            travellerAdapter.init(window.HUDController);
            console.log('HUD: Traveller adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize Traveller adapter', err);
        }
    }

    // Babylon5 Adapter
    if (window.Babylon5Adapter) {
        try {
            const apiBase = window.authService ? authService.getServerUrl() + '/babylon5' : '/api/v1/babylon5';
            const b5Adapter = new window.Babylon5Adapter(apiBase);
            b5Adapter.init(window.HUDController);
            console.log('HUD: Babylon5 adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize Babylon5 adapter', err);
        }
    }

    // Star Trek 4X Adapter
    if (window.StarTrek4xAdapter) {
        try {
            const apiBase = window.authService ? authService.getServerUrl() + '/star-trek-4x' : '/api/star-trek-4x';
            const st4xAdapter = new window.StarTrek4xAdapter(apiBase);
            st4xAdapter.init(window.HUDController);
            console.log('HUD: Star Trek 4X adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize Star Trek 4X adapter', err);
        }
    }

    // Mech Warrior Adapter
    if (window.MechWarriorAdapter) {
        try {
            const serverUrl = window.authService ? authService.getServerUrl() : '/api';
            const fpsBase = serverUrl + '/v1/mechwarrior/fps';
            const missionBase = serverUrl + '/v1/mechwarrior/missions';
            const enemyBase = serverUrl + '/v1/mechwarrior/enemies';
            const mwAdapter = new window.MechWarriorAdapter(fpsBase, missionBase, enemyBase);
            mwAdapter.init(window.HUDController);
            console.log('HUD: Mech Warrior adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize Mech Warrior adapter', err);
        }
    }

    // Zero Zero Seven Adapter
    if (window.ZeroZeroSevenAdapter) {
        try {
            const serverUrl = window.authService ? authService.getServerUrl() : '/api';
            const z7Base = serverUrl + '/zero-zero-seven';
            const z7Adapter = new window.ZeroZeroSevenAdapter(z7Base);
            z7Adapter.init(window.HUDController);
            console.log('HUD: Zero Zero Seven adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize Zero Zero Seven adapter', err);
        }
    }

    // Star Wars Adapter
    if (window.StarWarsAdapter) {
        try {
            const serverUrl = window.authService ? authService.getServerUrl() : '/api';
            const swBase = serverUrl + '/rules/engines';
            const swAdapter = new window.StarWarsAdapter(swBase);
            swAdapter.init(window.HUDController);
            console.log('HUD: Star Wars adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize Star Wars adapter', err);
        }
    }

    // AI Narrative Service
    if (window.AiNarrativeService) {
        try {
            const serverUrl = window.authService ? authService.getServerUrl() : 'http://localhost:8080';
            const aiNarrative = new window.AiNarrativeService(serverUrl);
            window.AiNarrative = aiNarrative;
            console.log('HUD: AI Narrative service initialized');
        } catch (err) {
            console.warn('HUD: Failed to initialize AI Narrative service', err);
        }
    }

    // Unified World Adapter (manages all module adapters internally)
    if (window.UnifiedWorldAdapter) {
        try {
            const serverUrl = window.authService ? authService.getServerUrl() : '/api';
            const unifiedAdapter = new window.UnifiedWorldAdapter(serverUrl);
            unifiedAdapter.init(window.HUDController);
            window.UnifiedWorld = unifiedAdapter; // store instance for external access
            console.log('HUD: Unified World adapter registered');
        } catch (err) {
            console.warn('HUD: Failed to initialize Unified World adapter', err);
        }
    }

    // Enable drag-and-drop for action buttons after adapters render
    setTimeout(() => {
        if (window.HUDController && window.HUDController.enableDragDrop) {
            window.HUDController.enableDragDrop('hudResponsesPanelBody');
            console.log('HUD: Drag-and-drop initialized');
        }
    }, 500);
}

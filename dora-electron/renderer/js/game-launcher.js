// Game Launcher — Main UI logic
const authService = require('./auth-service');

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
    refreshDashboard();
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
}

function initConnectButton() {
    const connectBtn = document.getElementById('connectBtn');
    if (connectBtn) {
        connectBtn.addEventListener('click', async () => {
            connectBtn.textContent = 'Connecting...';
            connectBtn.disabled = true;
            
            const result = await authService.connect();
            
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
    const launchBtn = document.getElementById('launchGameBtn');
    const startBtn = document.getElementById('startClientBtn');
    const stopBtn = document.getElementById('stopClientBtn');
    
    if (launchBtn) {
        launchBtn.addEventListener('click', async () => {
            launchBtn.textContent = 'Launching...';
            launchBtn.disabled = true;
            const result = await ipcRenderer.invoke('download-and-start-game-client', authService.getServerUrl());
            if (result.success) {
                addActivity('Game client launched');
            } else {
                addActivity('Failed to launch: ' + result.error);
            }
            launchBtn.textContent = 'Launch Game Client';
            launchBtn.disabled = false;
        });
    }
    
    if (startBtn) {
        startBtn.addEventListener('click', async () => {
            await ipcRenderer.invoke('start-game-client');
            addActivity('Game client started');
        });
    }
    
    if (stopBtn) {
        stopBtn.addEventListener('click', async () => {
            await ipcRenderer.invoke('stop-game-client');
            addActivity('Game client stopped');
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
                    await authService.fetchJson('/api/v1/4x/sessions', {
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
                authService.setServerUrl(url);
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
    ipcRenderer.on('game-client-deployment-status', (event, data) => {
        updateClientStatus(data.status, data.message);
        if (data.progress !== undefined) {
            addActivity(`Download progress: ${data.progress}%`);
        }
    });
    
    ipcRenderer.on('game-client-status-update', (event, data) => {
        showModal('Game Client Status', JSON.stringify(data, null, 2));
    });
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
    if (!authService.connected) return;
    
    try {
        // Fetch sessions
        const sessions = await authService.fetchJson('/api/v1/4x/sessions').catch(() => []);
        document.getElementById('activeGamesCount').textContent = sessions.length || 0;
        
        // Fetch galaxies
        const galaxies = await authService.fetchJson('/api/v1/galaxies').catch(() => []);
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
    if (!authService.connected) return;
    
    const list = document.getElementById('sessionsList');
    if (!list) return;
    
    try {
        const sessions = await authService.fetchJson('/api/v1/4x/sessions').catch(() => []);
        
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
        const galaxy = await authService.fetchJson(`/api/v1/galaxies/${galaxyName}`).catch(() => null);
        
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
        await authService.fetchJson(`/api/v1/4x/sessions/${sessionId}/join`, {
            method: 'POST'
        });
        addActivity('Joined session: ' + sessionId);
        refreshSessions();
    } catch (error) {
        addActivity('Failed to join session: ' + error.message);
    }
};

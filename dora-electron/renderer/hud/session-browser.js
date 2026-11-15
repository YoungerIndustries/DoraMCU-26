/* Session Browser
   Unified session browser and lobby for all game types */

class SessionBrowser {
    constructor(container) {
        this.container = container || document.getElementById('session-browser-container');
        this.serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';
        this.sessions = [];
        this.selectedSession = null;
        this.filters = {
            gameType: 'all',
            search: '',
            sortBy: 'players'
        };
        this.callbacks = {
            onJoin: null,
            onCreate: null,
            onSelect: null,
            onError: null
        };

        this._createUI();
    }

    /**
     * Set callbacks for session events.
     * @param {object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        this.callbacks = { ...this.callbacks, ...callbacks };
    }

    /**
     * Create the session browser UI.
     */
    _createUI() {
        if (!this.container) {
            console.error('[SessionBrowser] No container element found');
            return;
        }

        this.container.innerHTML = `
            <div class="session-browser" id="sessionBrowser">
                <!-- Header -->
                <div class="session-browser-header">
                    <h2 class="session-browser-title">Game Sessions</h2>
                    <button class="session-browser-btn session-browser-btn-primary" id="createSessionBtn">
                        <span class="btn-icon">➕</span> Create Session
                    </button>
                </div>

                <!-- Filters -->
                <div class="session-browser-filters">
                    <div class="filter-group">
                        <label class="filter-label">Game Type</label>
                        <select class="filter-select" id="gameTypeFilter">
                            <option value="all">All Games</option>
                            <option value="FPS">FPS</option>
                            <option value="MECHWARRIOR">MechWarrior</option>
                            <option value="DORAMCU_4X">DoraMCU 4X</option>
                            <option value="STAR_TREK_4X">Star Trek 4X</option>
                            <option value="ZERO_ZERO_SEVEN">007</option>
                            <option value="TRANSPORT_TYCOON">Transport Tycoon</option>
                            <option value="TRAVELLER">Traveller</option>
                            <option value="CAPTAINS_LOG">Captain's Log</option>
                            <option value="EARTH_2026">Earth 2026</option>
                            <option value="BABYLON_5">Babylon 5</option>
                            <option value="DND_5E">D&D 5E</option>
                            <option value="SFB">Star Frontiers</option>
                            <option value="STAR_WARS">Star Wars</option>
                            <option value="THIRD_REICH">Third Reich</option>
                            <option value="STARSHIP_TROOPERS">Starship Troopers</option>
                        </select>
                    </div>
                    <div class="filter-group">
                        <label class="filter-label">Search</label>
                        <input type="text" class="filter-input" id="searchInput" placeholder="Search sessions..." />
                    </div>
                    <div class="filter-group">
                        <label class="filter-label">Sort By</label>
                        <select class="filter-select" id="sortByFilter">
                            <option value="players">Player Count</option>
                            <option value="name">Name</option>
                            <option value="state">Game State</option>
                        </select>
                    </div>
                </div>

                <!-- Session List -->
                <div class="session-list" id="sessionList">
                    <!-- Session cards will be inserted here -->
                </div>

                <!-- Session Detail / Lobby -->
                <div class="session-detail" id="sessionDetail" style="display:none;">
                    <div class="session-detail-header">
                        <h3 class="session-detail-title" id="sessionDetailTitle">Session Details</h3>
                        <button class="session-detail-close" id="sessionDetailClose">✕</button>
                    </div>
                    <div class="session-detail-content">
                        <div class="session-info-grid" id="sessionInfoGrid">
                            <!-- Session info will be inserted here -->
                        </div>
                        <div class="session-players" id="sessionPlayers">
                            <h4>Players</h4>
                            <div class="player-list" id="playerList">
                                <!-- Player list will be inserted here -->
                            </div>
                        </div>
                        <div class="session-actions">
                            <button class="session-browser-btn session-browser-btn-primary" id="joinSessionBtn">
                                <span class="btn-icon">🎮</span> Join Session
                            </button>
                            <button class="session-browser-btn" id="leaveSessionBtn" style="display:none;">
                                <span class="btn-icon">🚪</span> Leave Session
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Create Session Modal -->
                <div class="session-modal" id="createSessionModal" style="display:none;">
                    <div class="session-modal-content">
                        <div class="session-modal-header">
                            <h3>Create New Session</h3>
                            <button class="session-modal-close" id="createSessionClose">✕</button>
                        </div>
                        <form class="session-form" id="createSessionForm">
                            <div class="form-group">
                                <label class="form-label">Game Type</label>
                                <select class="form-select" id="newSessionGameType" required>
                                    <option value="">Select game type...</option>
                                    <option value="FPS">FPS</option>
                                    <option value="MECHWARRIOR">MechWarrior</option>
                                    <option value="DORAMCU_4X">DoraMCU 4X</option>
                                    <option value="STAR_TREK_4X">Star Trek 4X</option>
                                    <option value="ZERO_ZERO_SEVEN">007</option>
                                    <option value="TRANSPORT_TYCOON">Transport Tycoon</option>
                                    <option value="TRAVELLER">Traveller</option>
                                    <option value="CAPTAINS_LOG">Captain's Log</option>
                                    <option value="EARTH_2026">Earth 2026</option>
                                    <option value="BABYLON_5">Babylon 5</option>
                                    <option value="DND_5E">D&D 5E</option>
                                    <option value="SFB">Star Frontiers</option>
                                    <option value="STAR_WARS">Star Wars</option>
                                    <option value="THIRD_REICH">Third Reich</option>
                                    <option value="STARSHIP_TROOPERS">Starship Troopers</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Session Name</label>
                                <input type="text" class="form-input" id="newSessionName" placeholder="My Awesome Session" required />
                            </div>
                            <div class="form-group">
                                <label class="form-label">Max Players</label>
                                <input type="number" class="form-input" id="newSessionMaxPlayers" min="2" max="100" value="10" />
                            </div>
                            <div class="form-actions">
                                <button type="button" class="session-browser-btn" id="cancelCreateBtn">Cancel</button>
                                <button type="submit" class="session-browser-btn session-browser-btn-primary">Create</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        `;

        this._bindEvents();
    }

    /**
     * Bind event listeners.
     */
    _bindEvents() {
        // Create session button
        const createBtn = document.getElementById('createSessionBtn');
        if (createBtn) {
            createBtn.addEventListener('click', () => this._showCreateModal());
        }

        // Close create modal
        const createClose = document.getElementById('createSessionClose');
        if (createClose) {
            createClose.addEventListener('click', () => this._hideCreateModal());
        }

        // Cancel create
        const cancelCreate = document.getElementById('cancelCreateBtn');
        if (cancelCreate) {
            cancelCreate.addEventListener('click', () => this._hideCreateModal());
        }

        // Create session form
        const createForm = document.getElementById('createSessionForm');
        if (createForm) {
            createForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this._createSession();
            });
        }

        // Game type filter
        const gameTypeFilter = document.getElementById('gameTypeFilter');
        if (gameTypeFilter) {
            gameTypeFilter.addEventListener('change', (e) => {
                this.filters.gameType = e.target.value;
                this._renderSessions();
            });
        }

        // Search input
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', (e) => {
                this.filters.search = e.target.value.toLowerCase();
                this._renderSessions();
            });
        }

        // Sort by filter
        const sortByFilter = document.getElementById('sortByFilter');
        if (sortByFilter) {
            sortByFilter.addEventListener('change', (e) => {
                this.filters.sortBy = e.target.value;
                this._renderSessions();
            });
        }

        // Close session detail
        const detailClose = document.getElementById('sessionDetailClose');
        if (detailClose) {
            detailClose.addEventListener('click', () => this._hideSessionDetail());
        }

        // Join session button
        const joinBtn = document.getElementById('joinSessionBtn');
        if (joinBtn) {
            joinBtn.addEventListener('click', () => this._joinSession());
        }

        // Leave session button
        const leaveBtn = document.getElementById('leaveSessionBtn');
        if (leaveBtn) {
            leaveBtn.addEventListener('click', () => this._leaveSession());
        }
    }

    /**
     * Load sessions from the API.
     */
    async loadSessions() {
        try {
            // Try to fetch from API
            const response = await fetch(`${this.serverUrl}/api/v1/sessions`, {
                headers: {
                    'Authorization': window.authService ? `Bearer ${window.authService.authToken}` : ''
                }
            });

            if (response.ok) {
                this.sessions = await response.json();
            } else {
                // Fallback to simulated data
                this.sessions = this._generateSimulatedSessions();
            }

            this._renderSessions();
            console.log(`[SessionBrowser] Loaded ${this.sessions.length} sessions`);
        } catch (error) {
            console.warn('[SessionBrowser] API unavailable, using simulated data:', error.message);
            this.sessions = this._generateSimulatedSessions();
            this._renderSessions();
        }
    }

    /**
     * Generate simulated session data for demo.
     * @returns {Array} Simulated sessions
     */
    _generateSimulatedSessions() {
        return [
            {
                id: 'session-1',
                name: 'Galactic Conquest',
                gameType: 'DORAMCU_4X',
                state: 'WAITING',
                players: [
                    { id: 'player-1', name: 'Commander X', ready: true },
                    { id: 'player-2', name: 'Admiral Y', ready: false }
                ],
                maxPlayers: 8,
                createdAt: new Date().toISOString()
            },
            {
                id: 'session-2',
                name: 'Death Star Assault',
                gameType: 'FPS',
                state: 'IN_PROGRESS',
                players: [
                    { id: 'player-3', name: 'Rebel Pilot', ready: true },
                    { id: 'player-4', name: 'Stormtrooper', ready: true },
                    { id: 'player-5', name: 'Jedi Knight', ready: true }
                ],
                maxPlayers: 16,
                createdAt: new Date().toISOString()
            },
            {
                id: 'session-3',
                name: 'Traveller Adventure',
                gameType: 'TRAVELLER',
                state: 'WAITING',
                players: [
                    { id: 'player-6', name: 'Explorer', ready: false }
                ],
                maxPlayers: 6,
                createdAt: new Date().toISOString()
            },
            {
                id: 'session-4',
                name: 'Dragon\'s Lair',
                gameType: 'DND_5E',
                state: 'IN_PROGRESS',
                players: [
                    { id: 'player-7', name: 'Paladin', ready: true },
                    { id: 'player-8', name: 'Wizard', ready: true },
                    { id: 'player-9', name: 'Rogue', ready: true },
                    { id: 'player-10', name: 'Cleric', ready: true }
                ],
                maxPlayers: 5,
                createdAt: new Date().toISOString()
            },
            {
                id: 'session-5',
                name: 'Star Trek Diplomacy',
                gameType: 'STAR_TREK_4X',
                state: 'WAITING',
                players: [],
                maxPlayers: 10,
                createdAt: new Date().toISOString()
            },
            {
                id: 'session-6',
                name: 'Battlefield Earth',
                gameType: 'THIRD_REICH',
                state: 'PAUSED',
                players: [
                    { id: 'player-11', name: 'Tank Commander', ready: true },
                    { id: 'player-12', name: 'Infantry Lead', ready: true }
                ],
                maxPlayers: 12,
                createdAt: new Date().toISOString()
            }
        ];
    }

    /**
     * Render the session list.
     */
    _renderSessions() {
        const sessionList = document.getElementById('sessionList');
        if (!sessionList) return;

        // Filter sessions
        let filtered = this.sessions.filter(session => {
            // Game type filter
            if (this.filters.gameType !== 'all' && session.gameType !== this.filters.gameType) {
                return false;
            }
            // Search filter
            if (this.filters.search && !session.name.toLowerCase().includes(this.filters.search)) {
                return false;
            }
            return true;
        });

        // Sort sessions
        filtered.sort((a, b) => {
            switch (this.filters.sortBy) {
                case 'players':
                    return b.players.length - a.players.length;
                case 'name':
                    return a.name.localeCompare(b.name);
                case 'state':
                    return a.state.localeCompare(b.state);
                default:
                    return 0;
            }
        });

        // Render session cards
        sessionList.innerHTML = filtered.map(session => this._createSessionCard(session)).join('');

        // Bind click handlers
        sessionList.querySelectorAll('.session-card').forEach(card => {
            card.addEventListener('click', () => {
                const sessionId = card.dataset.sessionId;
                this._selectSession(sessionId);
            });
        });
    }

    /**
     * Create a session card HTML.
     * @param {object} session - Session data
     * @returns {string} HTML string
     */
    _createSessionCard(session) {
        const profile = window.getProfileForGameType(session.gameType);
        const playerCount = session.players.length;
        const isFull = playerCount >= session.maxPlayers;
        const stateIcon = this._getStateIcon(session.state);
        const stateColor = this._getStateColor(session.state);

        return `
            <div class="session-card" data-session-id="${session.id}">
                <div class="session-card-header">
                    <div class="session-card-title">
                        <span class="session-game-icon">${profile ? profile.id : '🎮'}</span>
                        <span class="session-name">${session.name}</span>
                    </div>
                    <div class="session-state" style="color: ${stateColor}">
                        ${stateIcon} ${session.state.replace('_', ' ')}
                    </div>
                </div>
                <div class="session-card-body">
                    <div class="session-game-type">${session.gameType.replace(/_/g, ' ')}</div>
                    <div class="session-players">
                        <span class="player-count">${playerCount}/${session.maxPlayers}</span>
                        <span class="player-label">players</span>
                    </div>
                    <div class="session-player-avatars">
                        ${session.players.slice(0, 5).map(p => `
                            <span class="player-avatar" title="${p.name}">${p.name.charAt(0)}</span>
                        `).join('')}
                        ${playerCount > 5 ? `<span class="player-avatar-more">+${playerCount - 5}</span>` : ''}
                    </div>
                </div>
                <div class="session-card-footer">
                    ${isFull ?
                        '<button class="session-join-btn session-join-btn-full" disabled>Full</button>' :
                        '<button class="session-join-btn">Quick Join</button>'
                    }
                </div>
            </div>
        `;
    }

    /**
     * Get state icon.
     * @param {string} state - Session state
     * @returns {string} Icon emoji
     */
    _getStateIcon(state) {
        switch (state) {
            case 'WAITING': return '⏳';
            case 'IN_PROGRESS': return '🎮';
            case 'PAUSED': return '⏸️';
            case 'FINISHED': return '✅';
            default: return '❓';
        }
    }

    /**
     * Get state color.
     * @param {string} state - Session state
     * @returns {string} Color hex
     */
    _getStateColor(state) {
        switch (state) {
            case 'WAITING': return '#f59e0b';
            case 'IN_PROGRESS': return '#10b981';
            case 'PAUSED': return '#3b82f6';
            case 'FINISHED': return '#6b7280';
            default: return '#9ca3af';
        }
    }

    /**
     * Select a session and show details.
     * @param {string} sessionId - Session ID
     */
    _selectSession(sessionId) {
        const session = this.sessions.find(s => s.id === sessionId);
        if (!session) return;

        this.selectedSession = session;

        // Show session detail
        const detail = document.getElementById('sessionDetail');
        const title = document.getElementById('sessionDetailTitle');
        const infoGrid = document.getElementById('sessionInfoGrid');
        const playerList = document.getElementById('playerList');
        const joinBtn = document.getElementById('joinSessionBtn');
        const leaveBtn = document.getElementById('leaveSessionBtn');

        detail.style.display = 'block';
        title.textContent = session.name;

        // Session info
        const profile = window.getProfileForGameType(session.gameType);
        infoGrid.innerHTML = `
            <div class="session-info-item">
                <span class="info-label">Game Type</span>
                <span class="info-value">${session.gameType.replace(/_/g, ' ')}</span>
            </div>
            <div class="session-info-item">
                <span class="info-label">UI Profile</span>
                <span class="info-value">${profile ? profile.displayName : 'Unknown'}</span>
            </div>
            <div class="session-info-item">
                <span class="info-label">State</span>
                <span class="info-value">${this._getStateIcon(session.state)} ${session.state.replace('_', ' ')}</span>
            </div>
            <div class="session-info-item">
                <span class="info-label">Players</span>
                <span class="info-value">${session.players.length}/${session.maxPlayers}</span>
            </div>
            <div class="session-info-item">
                <span class="info-label">Created</span>
                <span class="info-value">${new Date(session.createdAt).toLocaleString()}</span>
            </div>
        `;

        // Player list
        if (session.players.length > 0) {
            playerList.innerHTML = session.players.map(p => `
                <div class="player-item">
                    <span class="player-avatar">${p.name.charAt(0)}</span>
                    <span class="player-name">${p.name}</span>
                    <span class="player-ready ${p.ready ? 'ready' : 'not-ready'}">${p.ready ? '✓ Ready' : 'Not Ready'}</span>
                </div>
            `).join('');
        } else {
            playerList.innerHTML = '<div class="no-players">No players yet</div>';
        }

        // Show/hide join/leave buttons
        const isJoined = session.players.some(p => p.id === 'current-user');
        joinBtn.style.display = isJoined ? 'none' : 'block';
        leaveBtn.style.display = isJoined ? 'block' : 'none';

        if (this.callbacks.onSelect) {
            this.callbacks.onSelect(session);
        }
    }

    /**
     * Hide session detail.
     */
    _hideSessionDetail() {
        const detail = document.getElementById('sessionDetail');
        if (detail) {
            detail.style.display = 'none';
        }
        this.selectedSession = null;
    }

    /**
     * Join the selected session.
     */
    async _joinSession() {
        if (!this.selectedSession) return;

        try {
            // Try API join
            const response = await fetch(`${this.serverUrl}/api/v1/sessions/${this.selectedSession.id}/join`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': window.authService ? `Bearer ${window.authService.authToken}` : ''
                }
            });

            if (response.ok) {
                const result = await response.json();
                console.log('[SessionBrowser] Joined session:', this.selectedSession.id);
                if (this.callbacks.onJoin) {
                    this.callbacks.onJoin(this.selectedSession, result);
                }
            } else {
                throw new Error('API join failed');
            }
        } catch (error) {
            console.warn('[SessionBrowser] API unavailable, simulating join:', error.message);
            // Simulate join
            if (this.callbacks.onJoin) {
                this.callbacks.onJoin(this.selectedSession, { sessionId: this.selectedSession.id });
            }
        }
    }

    /**
     * Leave the selected session.
     */
    async _leaveSession() {
        if (!this.selectedSession) return;

        try {
            await fetch(`${this.serverUrl}/api/v1/sessions/${this.selectedSession.id}/leave`, {
                method: 'POST',
                headers: {
                    'Authorization': window.authService ? `Bearer ${window.authService.authToken}` : ''
                }
            });
            console.log('[SessionBrowser] Left session:', this.selectedSession.id);
        } catch (error) {
            console.warn('[SessionBrowser] API unavailable, simulating leave');
        }

        this._hideSessionDetail();
    }

    /**
     * Show create session modal.
     */
    _showCreateModal() {
        const modal = document.getElementById('createSessionModal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }

    /**
     * Hide create session modal.
     */
    _hideCreateModal() {
        const modal = document.getElementById('createSessionModal');
        if (modal) {
            modal.style.display = 'none';
        }
        const form = document.getElementById('createSessionForm');
        if (form) {
            form.reset();
        }
    }

    /**
     * Create a new session.
     */
    async _createSession() {
        const gameType = document.getElementById('newSessionGameType').value;
        const name = document.getElementById('newSessionName').value;
        const maxPlayers = parseInt(document.getElementById('newSessionMaxPlayers').value);

        if (!gameType || !name) {
            console.warn('[SessionBrowser] Missing required fields');
            return;
        }

        try {
            const response = await fetch(`${this.serverUrl}/api/v1/sessions`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': window.authService ? `Bearer ${window.authService.authToken}` : ''
                },
                body: JSON.stringify({ gameType, name, maxPlayers })
            });

            if (response.ok) {
                const result = await response.json();
                console.log('[SessionBrowser] Created session:', result.id);
                this._hideCreateModal();
                if (this.callbacks.onCreate) {
                    this.callbacks.onCreate(result);
                }
                // Reload sessions
                await this.loadSessions();
            } else {
                throw new Error('API create failed');
            }
        } catch (error) {
            console.warn('[SessionBrowser] API unavailable, simulating create:', error.message);
            // Simulate create
            const newSession = {
                id: `session-${Date.now()}`,
                name,
                gameType,
                state: 'WAITING',
                players: [],
                maxPlayers,
                createdAt: new Date().toISOString()
            };
            this.sessions.unshift(newSession);
            this._renderSessions();
            this._hideCreateModal();
            if (this.callbacks.onCreate) {
                this.callbacks.onCreate(newSession);
            }
        }
    }

    /**
     * Destroy the session browser.
     */
    destroy() {
        if (this.container) {
            this.container.innerHTML = '';
        }
        this.sessions = [];
        this.selectedSession = null;
    }
}

// Expose globally
window.SessionBrowser = SessionBrowser;

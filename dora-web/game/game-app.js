// Main App Controller
// Wires views to game state, handles view switching and keyboard shortcuts

class GameApp {
    constructor() {
        this.state = new GameStateManager();
        this.api = null;
        this.views = {};
        this.currentView = null;

        // Initialize API client — auto-detect server origin
        const storedUrl = localStorage.getItem('serverUrl') || window.location.origin;
        this.api = new ApiClient(storedUrl);

        // Bind state events
        this._bindStateEvents();

        // Keyboard shortcuts
        this._bindKeyboardShortcuts();
    }

    async init() {
        // Create and register all views
        this.views.login = new LoginView(this);
        this.views.character = new CharacterView(this);
        this.views.earth = new EarthView(this);
        this.views.galaxy = new GalaxyView(this);
        this.views.main = new MainView(this);
        this.views.hud = new HudView(this);
        this.views.map = new MapView(this);
        this.views.shipBuild = new ShipBuildView(this);
        this.views.inventory = new InventoryView(this);
        this.views.menu = new MenuView(this);
        this.views.help = new HelpView(this);

        // Render all views
        const container = document.getElementById('view-container');
        if (container) {
            Object.values(this.views).forEach(view => {
                const el = view.render();
                el.style.display = 'none';
                container.appendChild(el);
            });
        }

        // Check for existing session
        if (this.state.isAuthenticated && this.state.token) {
            this.api.setToken(this.state.token);
            await this._checkSession();
        } else {
            this.switchView('login');
        }

        // Hide loading screen
        const loadingScreen = document.getElementById('loading-screen');
        if (loadingScreen) {
            loadingScreen.style.display = 'none';
        }

        console.log('[GameApp] Initialized');
    }

    async _checkSession() {
        this.state.setLoading(true, 'Checking session...');
        try {
            // Check onboarding config
            const config = await this.api.fetchOnboardingConfig(this.state.username);
            if (config && config.hasCharacters) {
                // Player has characters — Earth 2026 galaxy is already built,
                // auto-select it as the default galaxy
                this.state.selectGalaxy({ name: config.galaxyName || 'Earth 2026' });

                // Check if player already has a session
                const sessions = await this.api.fetchSessions();
                const username = this.state.username;
                let mySession = null;

                if (sessions && sessions.length > 0 && username) {
                    // Strategy 1: match by session name pattern "Username's Galaxy"
                    mySession = sessions.find(s => s.name && s.name.startsWith(username + "'"));
                    // Strategy 2: match by playerId in players array
                    if (!mySession && config.playerId != null) {
                        mySession = sessions.find(s => s.players && s.players.includes(config.playerId));
                    }
                }

                if (mySession) {
                    // Player has an existing session — go straight to main game
                    this.state.currentSession = mySession;
                    this.state.sessionId = mySession.sessionId;
                    console.log('[GameApp] Loaded existing session:', mySession.sessionId);
                    // Load player data (resources, fleets, planets) into the game state
                    await this._loadPlayerData();
                    this.switchView('main');
                } else {
                    // Player has characters but no session — land at Earth 2026 HQ first
                    // The Earth view's buttons will create the session and proceed to main game
                    console.log('[GameApp] No session found — landing at Earth 2026 HQ');
                    this.switchView('earth');
                }
            } else {
                // No characters, go to character creation
                this.switchView('character');
            }
        } catch (error) {
            console.error('[GameApp] Session check failed:', error);
            this.state.setError('Failed to check session. Please login again.');
            this.state.clearAuth();
            this.switchView('login');
        }
        this.state.setLoading(false);
    }

    async _loadSessions(playerId) {
        try {
            const sessions = await this.api.fetchSessions();
            if (!sessions || sessions.length === 0) {
                console.log('[GameApp] No sessions found, creating one...');
                await this._createDefaultSession();
                return;
            }

            // Look for a session belonging to this player.
            // The session name follows the pattern "Username's Galaxy" when auto-created.
            // Also check by playerId in the players array (playerId may differ between
            // onboarding config and session system, so use both strategies).
            const username = this.state.username;
            let mySession = null;

            if (username) {
                // Strategy 1: match by session name pattern "Username's Galaxy"
                mySession = sessions.find(s => s.name && s.name.startsWith(username + "'"));
            }

            // Strategy 2: match by playerId in players array
            if (!mySession && playerId != null) {
                mySession = sessions.find(s => s.players && s.players.includes(playerId));
            }

            if (mySession) {
                this.state.currentSession = mySession;
                this.state.sessionId = mySession.sessionId;
                console.log('[GameApp] Loaded player session:', mySession.sessionId);
                return;
            }

            // No existing session for this player — create one
            console.log('[GameApp] No session found for', username, '— creating one...');
            await this._createDefaultSession();
        } catch (error) {
            console.error('[GameApp] Failed to load sessions:', error);
        }
    }

    async _createDefaultSession() {
        try {
            const galaxyName = this.state.currentGalaxy?.name || 'Earth 2026';
            const sessionName = `${this.state.username}'s Galaxy`;
            const session = await this.api.createSession(sessionName);
            if (session) {
                // Set session state immediately — createSession already sets up the player
                this.state.currentSession = session;
                this.state.sessionId = session.sessionId;
                // Try to join, but the player is already in the session from createSession,
                // so a 409 conflict is expected and harmless
                try {
                    await this.api.joinSession(session.sessionId);
                } catch (joinError) {
                    // 409 means player is already in session — this is fine
                    if (!joinError.message.includes('409') && !joinError.message.includes('Conflict')) {
                        console.warn('[GameApp] Join session non-critical error:', joinError.message);
                    }
                }
                console.log('[GameApp] Created session:', session.sessionId);
            }
        } catch (error) {
            console.error('[GameApp] Failed to create default session:', error);
        }
    }

    async _loadPlayerData() {
        try {
            const gameState = await this.api.fetchResources(this.state.sessionId);
            if (!gameState) {
                console.warn('[GameApp] No game state returned');
                return;
            }

            // Safety check: if empireSummary is null, the session is broken
            // (e.g., playerId mismatch from old PlayerIdExtractor hash bug).
            // Redirect to Earth 2026 HQ so the player can start fresh.
            if (gameState.empireSummary === null && gameState.corporationState === null) {
                console.warn('[GameApp] Session has no empire data — session is broken. Redirecting to Earth 2026 HQ.');
                this.state.currentSession = null;
                this.state.sessionId = null;
                this.switchView('earth');
                return;
            }

            // GameStateResponse structure:
            // { empireSummary, fleets, pipelineSummary, subSectors, players, ... }
            // Map it to the game state so the HUD displays correct values.

            // Set fleets from the response
            const fleets = gameState.fleets || [];

            // Extract empire data
            const empire = gameState.empireSummary || {};
            const pipeline = gameState.pipelineSummary || {};
            const corpState = gameState.corporationState || {};

            // Build a PlayerInfo-shaped object from the game state response
            // Use corporation credit balance as the primary credits source (starting capital + earnings),
            // fall back to empire creditIncome (per-turn facility income) for display
            const creditsBalance = corpState.creditBalance != null ? corpState.creditBalance : (empire.creditIncome || 0);
            const playerData = {
                resources: {
                    credits: creditsBalance,
                    minerals: empire.mineralProduction || 0
                },
                rawResources: pipeline.rawResources || {},
                refinedResources: pipeline.refinedResources || {},
                modules: pipeline.modules || {},
                fleets: fleets,
                ships: fleets.flatMap(f => f.ships || []),
                planets: [] // Planets are tracked by empireSummary.planetCount
            };
            // Attach planet count so getPlanetCount() can use it
            playerData._planetCount = empire.planetCount || 0;
            playerData._shipCount = empire.navyShipCount || playerData.ships.length;

            this.state.setPlayer(playerData);

            // Set sectors from subSectors for the map view
            if (gameState.subSectors && gameState.subSectors.length > 0) {
                this.state.setSectors(this._buildSectorsFromSubSectors(gameState.subSectors));
            }

            console.log('[GameApp] Player data loaded:', {
                credits: playerData.resources.credits,
                minerals: playerData.resources.minerals,
                fleets: fleets.length,
                ships: playerData._shipCount,
                planets: playerData._planetCount
            });
        } catch (error) {
            console.error('[GameApp] Failed to load player data:', error);
        }
    }

    _buildSectorsFromSubSectors(subSectors) {
        const sectorMap = new Map();
        for (const ss of subSectors) {
            const key = `${ss.sectorRow},${ss.sectorCol}`;
            if (!sectorMap.has(key)) {
                sectorMap.set(key, {
                    row: ss.sectorRow,
                    col: ss.sectorCol,
                    explored: true,
                    hasSystems: false,
                    subSectors: []
                });
            }
            const sector = sectorMap.get(key);
            sector.hasSystems = sector.hasSystems || ss.systemCount > 0;
            sector.subSectors.push(ss);
        }
        return Array.from(sectorMap.values());
    }

    _bindStateEvents() {
        this.state.on('view:changed', ({ view }) => {
            this.switchView(view);
        });

        this.state.on('auth:changed', ({ authenticated }) => {
            if (!authenticated) {
                this.switchView('login');
            }
        });

        this.state.on('loading:changed', ({ loading, message }) => {
            this._updateLoading(loading, message);
        });

        this.state.on('error:changed', ({ error }) => {
            this._updateError(error);
        });
    }

    _bindKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            // F1 - Help
            if (e.key === 'F1') {
                e.preventDefault();
                if (this.views.help) {
                    this.views.help.toggle();
                }
            }

            // Escape - Close overlays
            if (e.key === 'Escape') {
                if (this.views.help && this.views.help.isVisible()) {
                    this.views.help.hide();
                }
            }

            // Tab - Toggle map (in main view)
            if (e.key === 'Tab' && this.currentView === 'main') {
                e.preventDefault();
                if (this.views.map) {
                    this.views.map.toggle();
                }
            }

            // V - Ship builder
            if (e.key === 'v' || e.key === 'V') {
                if (this.currentView === 'main' && this.views.shipBuild) {
                    this.views.shipBuild.show();
                }
            }

            // I - Inventory
            if (e.key === 'i' || e.key === 'I') {
                if (this.currentView === 'main' && this.views.inventory) {
                    this.views.inventory.show();
                }
            }

            // M - Map
            if (e.key === 'm' || e.key === 'M') {
                if (this.currentView === 'main' && this.views.map) {
                    this.views.map.show();
                }
            }
        });
    }

    switchView(viewName) {
        if (!this.views[viewName]) {
            console.warn(`[GameApp] View "${viewName}" not found`);
            return;
        }

        // Hide all views
        Object.entries(this.views).forEach(([name, view]) => {
            const el = view.getElement();
            if (el) {
                el.style.display = 'none';
            }
        });

        // Show target view
        const targetView = this.views[viewName];
        const targetEl = targetView.getElement();
        if (targetEl) {
            targetEl.style.display = 'block';
        }

        this.currentView = viewName;
        this.state.setCurrentView(viewName);

        // Call view's show method if it exists
        if (typeof targetView.show === 'function') {
            targetView.show();
        }

        console.log(`[GameApp] Switched to view: ${viewName}`);
    }

    _updateLoading(loading, message) {
        const loadingEl = document.getElementById('loading-overlay');
        if (loadingEl) {
            loadingEl.style.display = loading ? 'flex' : 'none';
            const msgEl = loadingEl.querySelector('.loading-message');
            if (msgEl && message) {
                msgEl.textContent = message;
            }
        }
    }

    _updateError(error) {
        const errorEl = document.getElementById('error-toast');
        if (errorEl) {
            if (error) {
                errorEl.textContent = error;
                errorEl.style.display = 'block';
                // Auto-hide after 5 seconds
                setTimeout(() => {
                    errorEl.style.display = 'none';
                    this.state.clearError();
                }, 5000);
            } else {
                errorEl.style.display = 'none';
            }
        }
    }

    // ── API Helpers ──

    async login(username, password) {
        this.state.setLoading(true, 'Logging in...');
        try {
            const response = await this.api.login(username, password);
            this.state.setAuth(response);
            await this._checkSession();
        } catch (error) {
            this.state.setError(`Login failed: ${error.message}`);
            throw error;
        } finally {
            this.state.setLoading(false);
        }
    }

    async register(username, password) {
        this.state.setLoading(true, 'Creating account...');
        try {
            const response = await this.api.register(username, password);
            this.state.setAuth(response);
            await this._checkSession();
        } catch (error) {
            this.state.setError(`Registration failed: ${error.message}`);
            throw error;
        } finally {
            this.state.setLoading(false);
        }
    }

    async devLogin() {
        this.state.setLoading(true, 'Dev login...');
        try {
            const response = await this.api.devLogin();
            this.state.setAuth(response);
            await this._checkSession();
        } catch (error) {
            this.state.setError(`Dev login failed: ${error.message}`);
            throw error;
        } finally {
            this.state.setLoading(false);
        }
    }

    async logout() {
        try {
            await this.api.logout();
        } finally {
            this.state.clearAuth();
            this.api.setToken(null);
            this.switchView('login');
        }
    }

    async saveGame() {
        this.state.setLoading(true, 'Saving game...');
        try {
            await this.api.saveGame();
            console.log('[GameApp] Game saved');
        } catch (error) {
            this.state.setError(`Save failed: ${error.message}`);
        } finally {
            this.state.setLoading(false);
        }
    }
}

// Export as global
window.GameApp = GameApp;

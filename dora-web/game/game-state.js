// Central Game State Manager
// Replaces GameManager singleton — manages auth, player data, galaxy data, and events

class GameStateManager {
    constructor() {
        // Auth state
        this.isAuthenticated = false;
        this.token = null;
        this.username = null;
        this.pin = null;
        this.empireName = null;
        this.corporationId = 0;
        this.corporationName = null;
        this.corporationTicker = null;

        // Player data
        this.player = null;
        this.playerStats = null;
        this.resources = {};

        // Galaxy data
        this.galaxies = [];
        this.currentGalaxy = null;
        this.sectors = [];
        this.selectedSector = null;

        // Game state
        this.sessionId = null;
        this.currentView = 'login';
        this.isLoading = false;
        this.error = null;

        // Ship building
        this.shipDesigns = [];
        this.selectedShipType = null;
        this.selectedModules = [];

        // Fleet management
        this.selectedFleet = null;
        this.fleetMoveMode = false;

        // Combat
        this.inCombat = false;
        this.combatState = null;

        // Event listeners
        this._listeners = {};

        // Restore from localStorage
        this._restore();
    }

    // ── Event System ──

    on(event, callback) {
        if (!this._listeners[event]) {
            this._listeners[event] = [];
        }
        this._listeners[event].push(callback);
        return () => {
            this._listeners[event] = this._listeners[event].filter(cb => cb !== callback);
        };
    }

    emit(event, data = {}) {
        if (this._listeners[event]) {
            this._listeners[event].forEach(cb => cb(data));
        }
    }

    // ── Auth State ──

    setAuth(loginResponse) {
        this.isAuthenticated = true;
        this.token = loginResponse.token;
        this.username = loginResponse.username;
        this.pin = loginResponse.pin;
        this.empireName = loginResponse.empireName;
        this.corporationId = loginResponse.corporationId;
        this.corporationName = loginResponse.corporationName;
        this.corporationTicker = loginResponse.corporationTicker;
        this._save();
        this.emit('auth:changed', { authenticated: true, username: this.username });
    }

    clearAuth() {
        this.isAuthenticated = false;
        this.token = null;
        this.username = null;
        this.pin = null;
        this.empireName = null;
        this.corporationId = 0;
        this.corporationName = null;
        this.corporationTicker = null;
        localStorage.removeItem('doramcu_game_state');
        this.emit('auth:changed', { authenticated: false });
    }

    // ── Player Data ──

    setPlayer(playerData) {
        this.player = new PlayerInfo(playerData);
        this.resources = this.player.resources || {};
        this._save();
        this.emit('player:updated', this.player);
    }

    setPlayerStats(stats) {
        this.playerStats = stats;
        this.emit('stats:updated', stats);
    }

    updateResources(resources) {
        this.resources = resources;
        if (this.player) {
            this.player.resources = resources;
        }
        this._save();
        this.emit('resources:updated', resources);
    }

    // ── Galaxy Data ──

    setGalaxies(galaxies) {
        this.galaxies = galaxies;
        this.emit('galaxies:updated', galaxies);
    }

    selectGalaxy(galaxy) {
        this.currentGalaxy = galaxy;
        this.emit('galaxy:selected', galaxy);
    }

    setSectors(sectors) {
        this.sectors = sectors;
        this.emit('sectors:updated', sectors);
    }

    selectSector(sector) {
        this.selectedSector = sector;
        this.emit('sector:selected', sector);
    }

    // ── View Management ──

    setCurrentView(view) {
        this.currentView = view;
        // Note: Do NOT emit 'view:changed' here — switchView() in GameApp already
        // handles the view switch and calls this method. Emitting would create
        // an infinite loop: switchView() -> setCurrentView() -> emit -> switchView() ...
    }

    // ── Loading State ──

    setLoading(loading, message = '') {
        this.isLoading = loading;
        this.emit('loading:changed', { loading, message });
    }

    setError(error) {
        this.error = error;
        this.emit('error:changed', { error });
    }

    clearError() {
        this.error = null;
        this.emit('error:changed', { error: null });
    }

    // ── Ship Building ──

    setShipDesigns(designs) {
        this.shipDesigns = designs;
        this.emit('shipDesigns:updated', designs);
    }

    selectShipType(type) {
        this.selectedShipType = type;
        this.emit('shipType:selected', { type });
    }

    addModule(module) {
        if (!this.selectedModules.includes(module)) {
            this.selectedModules.push(module);
            this.emit('modules:updated', { modules: [...this.selectedModules] });
        }
    }

    removeModule(module) {
        this.selectedModules = this.selectedModules.filter(m => m !== module);
        this.emit('modules:updated', { modules: [...this.selectedModules] });
    }

    clearModules() {
        this.selectedModules = [];
        this.emit('modules:updated', { modules: [] });
    }

    // ── Fleet Management ──

    selectFleet(fleet) {
        this.selectedFleet = fleet;
        this.emit('fleet:selected', { fleet });
    }

    clearFleetSelection() {
        this.selectedFleet = null;
        this.fleetMoveMode = false;
        this.emit('fleet:selected', { fleet: null });
    }

    setFleetMoveMode(enabled) {
        this.fleetMoveMode = enabled;
        this.emit('fleetMoveMode:changed', { enabled });
    }

    // ── Combat ──

    setCombatState(state) {
        this.inCombat = state !== null;
        this.combatState = state;
        this.emit('combat:changed', { inCombat: this.inCombat, state });
    }

    // ── Persistence ──

    _save() {
        const data = {
            token: this.token,
            username: this.username,
            sessionId: this.sessionId,
            currentGalaxy: this.currentGalaxy ? this.currentGalaxy.name : null
        };
        try {
            localStorage.setItem('doramcu_game_state', JSON.stringify(data));
        } catch (e) {
            console.warn('Failed to save game state:', e);
        }
    }

    _restore() {
        try {
            const raw = localStorage.getItem('doramcu_game_state');
            if (raw) {
                const data = JSON.parse(raw);
                this.token = data.token || null;
                this.username = data.username || null;
                this.sessionId = data.sessionId || null;
                this.isAuthenticated = !!this.token;
            }
        } catch (e) {
            console.warn('Failed to restore game state:', e);
        }
    }

    // ── Getters ──

    getCredits() {
        return this.resources['credits'] || 0;
    }

    getMinerals() {
        return this.resources['minerals'] || 0;
    }

    getShipCount() {
        if (!this.player) return 0;
        return this.player._shipCount || this.player.ships?.length || 0;
    }

    getFleetCount() {
        return this.player ? this.player.fleets?.length || 0 : 0;
    }

    getPlanetCount() {
        if (!this.player) return 0;
        return this.player._planetCount || this.player.planets?.length || 0;
    }
}

// Export as global singleton
window.GameStateManager = GameStateManager;

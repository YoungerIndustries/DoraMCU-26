/**
 * Client-side game state management.
 * Tracks all players, scores, and match information.
 */
class FpsState {
    constructor() {
        this.players = new Map();
        this.localPlayer = null;
        this.matchInfo = null;
        this.recentEvents = [];
        this.maxEvents = 50;
        
        // Capture the Point state
        this.capturePoints = [];
        this.teamScores = {};
        
        // Callbacks
        this.onLocalPlayerUpdated = null;
        this.onPlayerJoined = null;
        this.onPlayerLeft = null;
        this.onGameEvent = null;
        this.onMatchInfoUpdated = null;
        this.onMatchStarted = null;
        this.onMatchEnded = null;
    }
    
    updateFromServer(state) {
        const currentIds = new Set();
        
        // Process players
        if (state.players) {
            for (const ps of state.players) {
                currentIds.add(ps.id);
                
                if (this.players.has(ps.id)) {
                    // Update existing
                    this.players.set(ps.id, ps);
                } else {
                    // New player
                    this.players.set(ps.id, ps);
                    if (this.onPlayerJoined) this.onPlayerJoined(ps.id, ps);
                }
            }
            
            // Remove disconnected players
            for (const id of this.players.keys()) {
                if (!currentIds.has(id)) {
                    this.players.delete(id);
                    if (this.onPlayerLeft) this.onPlayerLeft(id);
                }
            }
        }
        
        // Update local player
        if (window.fpsNetwork && window.fpsNetwork.playerId) {
            const localId = window.fpsNetwork.playerId;
            if (this.players.has(localId)) {
                this.localPlayer = this.players.get(localId);
                if (this.onLocalPlayerUpdated) this.onLocalPlayerUpdated(this.localPlayer);
            }
        }
        
        // Update match info
        if (state.matchInfo) {
            this.matchInfo = state.matchInfo;
            if (this.onMatchInfoUpdated) this.onMatchInfoUpdated(this.matchInfo);
        }
        
        // Process events
        if (state.events) {
            for (const evt of state.events) {
                this.recentEvents.push(evt);
                if (this.onGameEvent) this.onGameEvent(evt);
                
                if (evt.type === 'MATCH_END' && this.onMatchEnded) this.onMatchEnded();
                if (evt.type === 'MATCH_START' && this.onMatchStarted) this.onMatchStarted();
            }
        }
        
        // Process capture points (CTP mode)
        if (state.capturePoints) {
            this.capturePoints = state.capturePoints;
        }
        
        // Process team scores (CTP mode)
        if (state.teamScores) {
            this.teamScores = state.teamScores;
        }
        
        // Trim events
        while (this.recentEvents.length > this.maxEvents) {
            this.recentEvents.shift();
        }
    }
    
    addEvent(evt) {
        this.recentEvents.push(evt);
        if (this.onGameEvent) this.onGameEvent(evt);
        while (this.recentEvents.length > this.maxEvents) {
            this.recentEvents.shift();
        }
    }
    
    getPlayer(playerId) {
        return this.players.get(playerId) || null;
    }
    
    getAlivePlayers() {
        const alive = [];
        for (const ps of this.players.values()) {
            if (ps.alive) alive.push(ps);
        }
        return alive;
    }
    
    getPlayersByScore() {
        const sorted = Array.from(this.players.values());
        sorted.sort((a, b) => (b.score || 0) - (a.score || 0));
        return sorted;
    }
    
    getKillEvents() {
        return this.recentEvents.filter(e => e.type === 'KILL' || e.type === 'HIT');
    }
    
    getLocalHealthPercent() {
        if (!this.localPlayer) return 1;
        return Math.max(0, Math.min(1, this.localPlayer.health / 100));
    }
    
    getLocalAmmoPercent() {
        if (!this.localPlayer) return 1;
        return Math.max(0, Math.min(1, this.localPlayer.ammo / 30));
    }
    
    getPlayerCount() {
        return this.players.size;
    }
    
    clear() {
        this.players.clear();
        this.localPlayer = null;
        this.matchInfo = null;
        this.recentEvents = [];
        this.capturePoints = [];
        this.teamScores = {};
    }
}

// Shared API Client Module for DoraMCU
// Provides a unified interface for all API calls across client modules

class ApiClient {
    constructor(baseUrl) {
        this.baseUrl = baseUrl || window.location.origin;
        this.token = localStorage.getItem('doramcu_auth_token');
    }

    setBaseUrl(url) {
        this.baseUrl = url;
    }

    setToken(token) {
        this.token = token;
        if (token) {
            localStorage.setItem('doramcu_auth_token', token);
        } else {
            localStorage.removeItem('doramcu_auth_token');
        }
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        try {
            const response = await fetch(url, {
                ...options,
                headers
            });

            if (!response.ok) {
                // Try to extract a friendly error message from the response body
                let message = `API error: ${response.status} ${response.statusText}`;
                try {
                    const errBody = await response.json();
                    if (errBody && errBody.error) {
                        message = errBody.error;
                    }
                } catch (_) {
                    // Not JSON, use default message
                }
                throw new Error(message);
            }

            // Handle empty responses
            if (response.status === 204) {
                return null;
            }

            return await response.json();
        } catch (error) {
            console.error(`API request failed: ${endpoint}`, error);
            throw error;
        }
    }

    // Game Sessions
    async getGameSessions() {
        return this.request('/api/v1/4x/sessions');
    }

    async createGameSession(data) {
        return this.request('/api/v1/4x/sessions', {
            method: 'POST',
            body: JSON.stringify({ sessionName: data.name })
        });
    }

    async getGameSession(id) {
        return this.request(`/api/v1/4x/sessions/${id}`);
    }

    async joinSession(id) {
        return this.request(`/api/v1/4x/sessions/${id}/join`, {
            method: 'POST'
        });
    }

    async leaveSession(id) {
        return this.request(`/api/v1/4x/sessions/${id}/leave`, {
            method: 'POST'
        });
    }

    async updateGameSession(id, data) {
        return this.request(`/api/v1/4x/sessions/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    async deleteGameSession(id) {
        return this.request(`/api/v1/4x/sessions/${id}`, {
            method: 'DELETE'
        });
    }

    // Characters (Earth2026)
    async getCharacters() {
        return this.request('/api/v1/earth2026/characters');
    }

    async getCharacter(id) {
        return this.request(`/api/v1/traveller/characters/${id}`);
    }

    // Authentication
    async login(username, password) {
        const response = await this.request('/api/v1/4x/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });

        if (response && response.token) {
            this.setToken(response.token);
        }

        return response;
    }

    async register(username, password) {
        const response = await this.request('/api/v1/4x/auth/register', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });

        if (response && response.token) {
            this.setToken(response.token);
        }

        return response;
    }

    async devLogin() {
        const response = await this.request('/api/v1/4x/auth/dev-login');
        if (response && response.token) {
            this.setToken(response.token);
        }
        return response;
    }

    async logout() {
        this.setToken(null);
        try {
            return await this.request('/api/v1/4x/auth/logout', { method: 'POST' });
        } catch (e) {
            // Ignore logout errors
        }
    }

    // Onboarding
    async fetchOnboardingConfig(username) {
        const params = username ? `?username=${encodeURIComponent(username)}` : '';
        return this.request(`/api/v1/onboarding/config${params}`);
    }

    async createCharacter(data) {
        return this.request('/api/v1/onboarding/character', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    async fetchEarthLanding(username) {
        return this.request(`/api/v1/onboarding/earth?username=${encodeURIComponent(username || '')}`);
    }

    async fetchCurrentUser() {
        return this.request('/api/v1/4x/auth/me');
    }

    // Galaxy List
    async fetchGalaxies() {
        return this.request('/api/v1/4x/auth/galaxies');
    }

    // Galaxy 3D
    async fetchGalaxy3D(galaxyName, colorScheme = 'realistic') {
        return this.request(`/api/v1/galaxy-3d/galaxy/${encodeURIComponent(galaxyName)}?colorScheme=${encodeURIComponent(colorScheme)}`);
    }

    async fetchAiAdmins(galaxyName) {
        return this.request(`/api/v1/galaxy-3d/galaxy/${encodeURIComponent(galaxyName)}/ai-admins`);
    }

    async fetchGalaxyStatistics() {
        return this.request('/api/v1/galaxy-3d/statistics');
    }

    // FourX Exploration
    async fetchSectors() {
        return this.request('/api/v1/fourx/exploration/sectors');
    }

    async scanSector(sectorId) {
        return this.request(`/api/v1/fourx/exploration/scan/${encodeURIComponent(sectorId)}`, {
            method: 'POST'
        });
    }

    async fetchDiscoveredSystems() {
        return this.request('/api/v1/fourx/exploration/systems');
    }

    async fetchExplorationReport(systemId) {
        return this.request(`/api/v1/fourx/exploration/systems/${encodeURIComponent(systemId)}`);
    }

    // Sessions
    async fetchSessions() {
        return this.request('/api/v1/4x/sessions');
    }

    async createSession(sessionName) {
        return this.request('/api/v1/4x/sessions', {
            method: 'POST',
            body: JSON.stringify({ sessionName: sessionName })
        });
    }

    async joinSession(sessionId) {
        return this.request(`/api/v1/4x/sessions/${encodeURIComponent(sessionId)}/join`, {
            method: 'POST'
        });
    }

    async getGameState(sessionId, summary = false) {
        const param = summary ? '?summary=true' : '';
        return this.request(`/api/v1/4x/sessions/${encodeURIComponent(sessionId)}/state${param}`);
    }

    // Ship Building (session-based)
    async buildShip(sessionId, shipType, modules) {
        return this.request(`/api/v1/4x/sessions/${encodeURIComponent(sessionId)}/actions/build-ship`, {
            method: 'POST',
            body: JSON.stringify({ shipType: shipType, modules: modules || [] })
        });
    }

    async fetchShipDesigns() {
        return this.request('/api/v1/game/ships/designs');
    }

    // Player
    async fetchResources(sessionId) {
        if (sessionId) {
            // Use summary mode to avoid downloading the full 146MB galaxy state.
            // The full mode iterates all ~19K solar systems and takes ~18s on Pi.
            // Summary mode returns in ~0.2s with ownership info only.
            return this.request(`/api/v1/4x/sessions/${encodeURIComponent(sessionId)}/state?summary=true`);
        }
        return this.request('/api/v1/4x/auth/me');
    }

    async fetchPlayerStats() {
        return this.request('/api/v1/4x/auth/me');
    }

    // Fleet Movement (session-based)
    async moveFleet(sessionId, fleetId, targetRow, targetCol, solarSystemId) {
        return this.request(`/api/v1/4x/sessions/${encodeURIComponent(sessionId)}/actions/move-fleet`, {
            method: 'POST',
            body: JSON.stringify({ fleetId, targetRow, targetCol, solarSystemId })
        });
    }

    // Combat (session-based)
    async startBattle(sessionId, attackerFleetId, defenderFleetId) {
        return this.request(`/api/v1/4x/sessions/${encodeURIComponent(sessionId)}/actions/engage-combat`, {
            method: 'POST',
            body: JSON.stringify({ attackerFleetId, defenderFleetId })
        });
    }

    async resolveCombat(action) {
        return this.request('/api/v1/battle/resolve', {
            method: 'POST',
            body: JSON.stringify({ action })
        });
    }

    // Save/Load
    async saveGame() {
        return this.request('/api/v1/save/snapshot/now', {
            method: 'POST',
            body: '{}'
        });
    }

    async loadGame(sessionId) {
        return this.request(`/api/v1/save/export/${encodeURIComponent(sessionId)}`);
    }

    async listSaveableSessions() {
        return this.request('/api/v1/save/sessions');
    }

    async importSave(saveData) {
        return this.request('/api/v1/save/import', {
            method: 'POST',
            body: JSON.stringify(saveData)
        });
    }

    // Client Manifest
    async getClientManifest() {
        return this.request('/api/v1/clients/manifest');
    }

    // Health Check
    async healthCheck() {
        return this.request('/actuator/health');
    }

    // Event Source for SSE
    createEventSource() {
        return new EventSource(`${this.baseUrl}/api/v1/game/events`);
    }
}

// Export as global
window.ApiClient = ApiClient;

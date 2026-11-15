// Shared API Client Module for DoraMCU
// Provides a unified interface for all API calls across client modules

class ApiClient {
    constructor(baseUrl) {
        this.baseUrl = baseUrl || 'http://localhost:8080';
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
                throw new Error(`API error: ${response.status} ${response.statusText}`);
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
        return this.request('/api/v1/game-sessions');
    }

    async createGameSession(data) {
        return this.request('/api/v1/game-sessions', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    async getGameSession(id) {
        return this.request(`/api/v1/game-sessions/${id}`);
    }

    async updateGameSession(id, data) {
        return this.request(`/api/v1/game-sessions/${id}`, {
            method: 'PUT',
            body: JSON.stringify(data)
        });
    }

    async deleteGameSession(id) {
        return this.request(`/api/v1/game-sessions/${id}`, {
            method: 'DELETE'
        });
    }

    // Characters
    async getCharacters() {
        return this.request('/api/v1/characters');
    }

    async createCharacter(data) {
        return this.request('/api/v1/characters', {
            method: 'POST',
            body: JSON.stringify(data)
        });
    }

    async getCharacter(id) {
        return this.request(`/api/v1/characters/${id}`);
    }

    // Authentication
    async login(username, password) {
        const response = await this.request('/api/v1/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });

        if (response && response.token) {
            this.setToken(response.token);
        }

        return response;
    }

    async logout() {
        this.setToken(null);
        return this.request('/api/v1/auth/logout', { method: 'POST' });
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
        return new EventSource(`${this.baseUrl}/api/v1/events`);
    }
}

// Export as global
window.ApiClient = ApiClient;

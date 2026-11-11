// Auth Service — Server connection and authentication
const { ipcRenderer } = require('electron');

class AuthService {
    constructor() {
        this.serverUrl = 'http://localhost:8080';
        this.connected = false;
        this.token = null;
        this.user = null;
    }

    setServerUrl(url) {
        this.serverUrl = url;
    }

    getServerUrl() {
        return this.serverUrl;
    }

    async connect() {
        try {
            const response = await fetch(this.serverUrl + '/actuator/health', {
                method: 'GET',
                mode: 'cors'
            });
            if (response.ok) {
                this.connected = true;
                this.user = { name: 'Guest' };
                return { success: true, status: await response.json() };
            }
        } catch (error) {
            console.error('Failed to connect to server:', error.message);
        }
        this.connected = false;
        return { success: false, error: 'Server unreachable' };
    }

    isAuthenticated() {
        return this.token !== null;
    }

    getStatus() {
        return {
            connected: this.connected,
            serverUrl: this.serverUrl,
            user: this.user
        };
    }

    async fetchJson(path, options = {}) {
        try {
            const response = await fetch(this.serverUrl + path, {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                }
            });
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }
            return await response.json();
        } catch (error) {
            console.error(`Failed to fetch ${path}:`, error.message);
            throw error;
        }
    }
}

module.exports = new AuthService();

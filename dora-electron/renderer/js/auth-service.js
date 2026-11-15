// Auth Service — Server connection and authentication
// Browser-compatible singleton (no CommonJS require/module.exports)
(function() {
    'use strict';

    class AuthService {
        constructor() {
            this.serverUrl = localStorage.getItem('serverUrl') || 'http://localhost:8080';
            this.connected = false;
            this.token = localStorage.getItem('authToken') || null;
            this.user = null;
        }

        setServerUrl(url) {
            this.serverUrl = url;
            localStorage.setItem('serverUrl', url);
        }

        getServerUrl() {
            return this.serverUrl;
        }

        async connect() {
            // Try /actuator/health first, then fall back to root endpoint
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
                console.debug('Health endpoint unavailable, trying root endpoint...');
            }
            // Fallback: try root endpoint
            try {
                const response = await fetch(this.serverUrl + '/', {
                    method: 'GET',
                    mode: 'cors'
                });
                if (response.ok) {
                    this.connected = true;
                    this.user = { name: 'Guest' };
                    return { success: true, status: { status: 'UP' } };
                }
            } catch (error) {
                console.error('Failed to connect to server:', error.message);
            }
            this.connected = false;
            return { success: false, error: 'Server unreachable' };
        }

        async login(username, password) {
            try {
                const result = await this.fetchJson('/api/auth/login', {
                    method: 'POST',
                    body: JSON.stringify({ username, password })
                });
                this.token = result.token;
                this.user = { name: username };
                this.connected = true;
                localStorage.setItem('authToken', this.token);
                return { success: true, username };
            } catch (error) {
                return { success: false, error: error.message };
            }
        }

        async register(body) {
            try {
                const result = await this.fetchJson('/api/v1/onboarding/register', {
                    method: 'POST',
                    body: JSON.stringify(body)
                });
                this.token = result.token;
                this.user = { name: result.username };
                this.connected = true;
                localStorage.setItem('authToken', this.token);
                return result;
            } catch (error) {
                throw error;
            }
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
                const headers = {
                    'Content-Type': 'application/json',
                    ...options.headers
                };
                if (this.token) {
                    headers['Authorization'] = 'Bearer ' + this.token;
                }
                const response = await fetch(this.serverUrl + path, {
                    ...options,
                    headers
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

    // Expose as global singleton for browser scripts
    window.authService = new AuthService();
})();

/* ============================================
   DoraMCU Unified Authentication Service
   ============================================
   Single auth service that works across all client layers:
   - static/ (Spring Boot web portal)
   - dora-electron/ (Electron desktop app)
   - dora-web/ (PWA and platform-adaptive clients)
   
   Replaces: auth.js, auth-service.js, and inline login patterns.
   Uses consistent localStorage keys: 'token' and 'username'.
   
   Usage:
     // Initialize
     const auth = new UnifiedAuthService();
     
     // Login
     await auth.login('user@example.com', 'password');
     
     // Check status
     if (auth.isAuthenticated()) {
         console.log(auth.getUsername());
     }
     
     // Make authenticated request
     const data = await auth.fetchJson('/api/game/state');
     
     // Auto-refresh token
     auth.enableAutoRefresh();
   ============================================ */

'use strict';

class UnifiedAuthService {
    /**
     * Create a new UnifiedAuthService instance
     * @param {Object} [options] - Configuration options
     * @param {string} [options.serverUrl] - Override server URL (defaults to localStorage or current origin)
     * @param {boolean} [options.autoMigrate=true] - Automatically migrate legacy auth keys
     * @param {number} [options.tokenRefreshThreshold=300000] - Refresh token when expires in less than ms (5min default)
     */
    constructor(options = {}) {
        // Consistent localStorage keys across all pages
        this.TOKEN_KEY = 'token';
        this.USERNAME_KEY = 'username';
        this.SERVER_URL_KEY = 'serverUrl';
        this.TOKEN_EXPIRY_KEY = 'tokenExpiry';

        // Configuration
        this.serverUrl = options.serverUrl || this._getStoredServerUrl();
        this.autoMigrate = options.autoMigrate !== false;
        this.tokenRefreshThreshold = options.tokenRefreshThreshold || 300000; // 5 minutes
        this._refreshInterval = null;

        // State
        this.token = localStorage.getItem(this.TOKEN_KEY) || null;
        this.user = null;
        this.connected = false;
        this._listeners = [];

        // Migrate legacy keys on init
        if (this.autoMigrate) {
            this._migrateLegacyKeys();
        }

        // Update token after migration
        this.token = localStorage.getItem(this.TOKEN_KEY) || this.token;
        if (this.token) {
            this.user = { name: localStorage.getItem(this.USERNAME_KEY) || 'Unknown' };
        }
    }

    // ── Server URL Management ──

    /**
     * Get the stored server URL
     * @returns {string} Base API URL
     * @private
     */
    _getStoredServerUrl() {
        const stored = localStorage.getItem(this.SERVER_URL_KEY);
        if (stored && stored !== '') {
            return stored.replace(/\/$/, ''); // Remove trailing slash
        }
        // Default to current origin
        if (typeof window !== 'undefined') {
            return window.location.origin;
        }
        return 'http://localhost:8080';
    }

    /**
     * Get the current server URL
     * @returns {string}
     */
    getServerUrl() {
        return this.serverUrl;
    }

    /**
     * Set the server URL
     * @param {string} url - Server URL
     */
    setServerUrl(url) {
        this.serverUrl = url.replace(/\/$/, '');
        localStorage.setItem(this.SERVER_URL_KEY, this.serverUrl);
        this._notify('serverUrlChanged', { url: this.serverUrl });
    }

    // ── Legacy Key Migration ──

    /**
     * Migrate legacy localStorage keys to unified keys
     * @private
     */
    _migrateLegacyKeys() {
        // Migrate authToken to token
        const legacyToken = localStorage.getItem('authToken');
        if (legacyToken && !localStorage.getItem(this.TOKEN_KEY)) {
            localStorage.setItem(this.TOKEN_KEY, legacyToken);
            localStorage.removeItem('authToken');
            this.token = legacyToken;
        }

        // Migrate adminUsername to username
        const legacyUsername = localStorage.getItem('adminUsername');
        if (legacyUsername && !localStorage.getItem(this.USERNAME_KEY)) {
            localStorage.setItem(this.USERNAME_KEY, legacyUsername);
            localStorage.removeItem('adminUsername');
            if (this.token) {
                this.user = { name: legacyUsername };
            }
        }

        // Migrate dora_token to token (from index.html sidebar login)
        const doraToken = localStorage.getItem('dora_token');
        if (doraToken && !localStorage.getItem(this.TOKEN_KEY)) {
            localStorage.setItem(this.TOKEN_KEY, doraToken);
            localStorage.removeItem('dora_token');
            this.token = doraToken;
        }

        // Migrate dora_username to username (from index.html sidebar login)
        const doraUsername = localStorage.getItem('dora_username');
        if (doraUsername && !localStorage.getItem(this.USERNAME_KEY)) {
            localStorage.setItem(this.USERNAME_KEY, doraUsername);
            localStorage.removeItem('dora_username');
            if (this.token) {
                this.user = { name: doraUsername };
            }
        }
    }

    // ── Core Authentication ──

    /**
     * Store authentication credentials
     * @param {string} token - Authentication token
     * @param {string} username - Username
     * @param {number} [expiry] - Token expiry timestamp (ms)
     */
    setCredentials(token, username, expiry = null) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USERNAME_KEY, username);
        if (expiry) {
            localStorage.setItem(this.TOKEN_EXPIRY_KEY, String(expiry));
        }
        this.token = token;
        this.user = { name: username };
        this._notify('authenticated', { username });
    }

    /**
     * Get stored authentication credentials
     * @returns {{token: string|null, username: string|null}}
     */
    getCredentials() {
        return {
            token: localStorage.getItem(this.TOKEN_KEY),
            username: localStorage.getItem(this.USERNAME_KEY)
        };
    }

    /**
     * Check if user is authenticated
     * @returns {boolean}
     */
    isAuthenticated() {
        // Check stored token first
        const storedToken = localStorage.getItem(this.TOKEN_KEY);
        if (!storedToken) return false;

        // Check token expiry if available
        const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
        if (expiry) {
            const expiryTime = parseInt(expiry);
            if (Date.now() >= expiryTime) {
                this.clearCredentials();
                return false;
            }
        }

        // Update internal state
        if (!this.token) {
            this.token = storedToken;
            this.user = { name: localStorage.getItem(this.USERNAME_KEY) || 'Unknown' };
        }

        return true;
    }

    /**
     * Clear authentication credentials
     */
    clearCredentials() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USERNAME_KEY);
        localStorage.removeItem(this.TOKEN_EXPIRY_KEY);
        this.token = null;
        this.user = null;
        this._stopAutoRefresh();
        this._notify('loggedOut');
    }

    /**
     * Get the current username
     * @returns {string|null}
     */
    getUsername() {
        return this.user ? this.user.name : localStorage.getItem(this.USERNAME_KEY);
    }

    /**
     * Get the current token
     * @returns {string|null}
     */
    getToken() {
        return this.token || localStorage.getItem(this.TOKEN_KEY);
    }

    /**
     * Get authorization header for API requests
     * @returns {Object} Headers object with Authorization if authenticated
     */
    getAuthHeader() {
        const token = this.getToken();
        if (token) {
            return { 'Authorization': `Bearer ${token}` };
        }
        return {};
    }

    // ── Authentication Methods ──

    /**
     * Login with email/username and password
     * @param {string} username - Username or email
     * @param {string} password - Password
     * @returns {Promise<Object>} Login result
     */
    async login(username, password) {
        try {
            const response = await fetch(`${this.serverUrl}/api/v1/4x/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                const error = await response.json().catch(() => ({ message: 'Login failed' }));
                throw new Error(error.message || 'Login failed');
            }

            const result = await response.json();
            this.setCredentials(result.token, result.username, result.expiry);
            this.connected = true;
            this._startAutoRefresh();
            return result;
        } catch (error) {
            this.connected = false;
            this._notify('loginError', { error: error.message });
            throw error;
        }
    }

    /**
     * Login with PIN code
     * @param {string} pin - PIN code
     * @returns {Promise<Object>} Login result
     */
    async loginWithPin(pin) {
        try {
            const response = await fetch(`${this.serverUrl}/api/v1/4x/auth/login/pin`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ pin })
            });

            if (!response.ok) {
                const error = await response.json().catch(() => ({ message: 'PIN login failed' }));
                throw new Error(error.message || 'PIN login failed');
            }

            const result = await response.json();
            this.setCredentials(result.token, result.username, result.expiry);
            this.connected = true;
            this._startAutoRefresh();
            return result;
        } catch (error) {
            this.connected = false;
            this._notify('loginError', { error: error.message });
            throw error;
        }
    }

    /**
     * Register a new account
     * @param {Object} data - Registration data
     * @param {string} data.username - Username
     * @param {string} data.email - Email address
     * @param {string} data.password - Password
     * @returns {Promise<Object>} Registration result
     */
    async register(data) {
        try {
            const response = await fetch(`${this.serverUrl}/api/v1/4x/auth/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                const error = await response.json().catch(() => ({ message: 'Registration failed' }));
                throw new Error(error.message || 'Registration failed');
            }

            const result = await response.json();
            if (result.token) {
                this.setCredentials(result.token, result.username, result.expiry);
                this.connected = true;
                this._startAutoRefresh();
            }
            return result;
        } catch (error) {
            this._notify('registrationError', { error: error.message });
            throw error;
        }
    }

    /**
     * Logout and clear credentials
     * @returns {Promise<void>}
     */
    async logout() {
        // Call server logout endpoint if available
        try {
            await fetch(`${this.serverUrl}/api/v1/4x/auth/logout`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...this.getAuthHeader()
                }
            });
        } catch (error) {
            // Ignore server logout errors
            console.debug('Server logout failed, clearing local credentials:', error.message);
        }

        this.clearCredentials();
        this.connected = false;
    }

    /**
     * Verify the current token is valid
     * @returns {Promise<boolean>} True if token is valid
     */
    async verifyToken() {
        const token = this.getToken();
        if (!token) return false;

        try {
            const response = await fetch(`${this.serverUrl}/api/v1/4x/auth/server-info`, {
                method: 'GET',
                headers: this.getAuthHeader()
            });

            if (response.ok) {
                const result = await response.json();
                if (result.valid) {
                    return true;
                }
            }
        } catch (error) {
            console.debug('Token verification failed:', error.message);
        }

        this.clearCredentials();
        return false;
    }

    /**
     * Refresh the current token
     * @returns {Promise<string|null>} New token or null if refresh failed
     */
    async refreshToken() {
        const token = this.getToken();
        if (!token) return null;

        try {
            const response = await fetch(`${this.serverUrl}/api/v1/4x/auth/server-info`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...this.getAuthHeader()
                }
            });

            if (response.ok) {
                const result = await response.json();
                this.setCredentials(result.token, this.getUsername(), result.expiry);
                this._notify('tokenRefreshed');
                return result.token;
            }
        } catch (error) {
            console.debug('Token refresh failed:', error.message);
        }

        // Refresh failed, user needs to re-login
        this.clearCredentials();
        return null;
    }

    // ── Auto Refresh ──

    /**
     * Enable automatic token refresh
     * @param {number} [intervalMs] - Check interval in ms (default: 3 minutes)
     */
    enableAutoRefresh(intervalMs = 180000) {
        this._startAutoRefresh(intervalMs);
    }

    /**
     * Disable automatic token refresh
     */
    disableAutoRefresh() {
        this._stopAutoRefresh();
    }

    /**
     * Start automatic token refresh check
     * @param {number} [intervalMs] - Check interval in ms
     * @private
     */
    _startAutoRefresh(intervalMs = 180000) {
        this._stopAutoRefresh();
        this._refreshInterval = setInterval(() => {
            this._checkTokenExpiry();
        }, intervalMs);
    }

    /**
     * Stop automatic token refresh check
     * @private
     */
    _stopAutoRefresh() {
        if (this._refreshInterval) {
            clearInterval(this._refreshInterval);
            this._refreshInterval = null;
        }
    }

    /**
     * Check if token is expiring soon and refresh if needed
     * @private
     */
    async _checkTokenExpiry() {
        const expiry = localStorage.getItem(this.TOKEN_EXPIRY_KEY);
        if (!expiry) return;

        const expiryTime = parseInt(expiry);
        const timeUntilExpiry = expiryTime - Date.now();

        if (timeUntilExpiry <= this.tokenRefreshThreshold) {
            await this.refreshToken();
        }
    }

    // ── Server Status ──

    /**
     * Check if the server is reachable
     * @returns {Promise<boolean>}
     */
    async checkServerStatus() {
        try {
            const response = await fetch(`${this.serverUrl}/actuator/health`, {
                method: 'GET',
                mode: 'cors'
            });
            if (response.ok) {
                this.connected = true;
                return true;
            }
        } catch (error) {
            // Fallback: try root endpoint
            try {
                const response = await fetch(`${this.serverUrl}/`, {
                    method: 'GET',
                    mode: 'cors'
                });
                if (response.ok) {
                    this.connected = true;
                    return true;
                }
            } catch (fallbackError) {
                // Ignore
            }
        }

        this.connected = false;
        return false;
    }

    // ── HTTP Helper Methods ──

    /**
     * Make an authenticated GET request
     * @param {string} path - API path
     * @param {Object} [options] - Fetch options
     * @returns {Promise<any>} Response JSON
     */
    async fetchJson(path, options = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...this.getAuthHeader(),
            ...options.headers
        };

        try {
            const response = await fetch(`${this.serverUrl}${path}`, {
                ...options,
                headers
            });

            if (!response.ok) {
                // If 401, token may be expired
                if (response.status === 401) {
                    const refreshed = await this.refreshToken();
                    if (refreshed) {
                        // Retry with new token
                        const retryHeaders = {
                            'Content-Type': 'application/json',
                            ...this.getAuthHeader(),
                            ...options.headers
                        };
                        const retryResponse = await fetch(`${this.serverUrl}${path}`, {
                            ...options,
                            headers: retryHeaders
                        });
                        if (!retryResponse.ok) {
                            throw new Error(`HTTP ${retryResponse.status}`);
                        }
                        return await retryResponse.json();
                    }
                    // Refresh failed, clear credentials
                    this.clearCredentials();
                    throw new Error('Authentication required');
                }

                throw new Error(`HTTP ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            if (error.message === 'Authentication required') {
                this._notify('authRequired');
            }
            throw error;
        }
    }

    /**
     * Make an authenticated POST request
     * @param {string} path - API path
     * @param {Object} [body] - Request body
     * @returns {Promise<any>} Response JSON
     */
    async postJson(path, body = {}) {
        return this.fetchJson(path, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    }

    /**
     * Make an authenticated PUT request
     * @param {string} path - API path
     * @param {Object} [body] - Request body
     * @returns {Promise<any>} Response JSON
     */
    async putJson(path, body = {}) {
        return this.fetchJson(path, {
            method: 'PUT',
            body: JSON.stringify(body)
        });
    }

    /**
     * Make an authenticated DELETE request
     * @param {string} path - API path
     * @returns {Promise<any>} Response JSON
     */
    async deleteJson(path) {
        return this.fetchJson(path, {
            method: 'DELETE'
        });
    }

    // ── Event System ──

    /**
     * Subscribe to auth events
     * @param {string} event - Event name: authenticated, loggedOut, loginError, tokenRefreshed, authRequired, serverUrlChanged
     * @param {Function} callback - Event callback
     * @returns {Function} Unsubscribe function
     */
    on(event, callback) {
        this._listeners.push({ event, callback });
        return () => {
            this._listeners = this._listeners.filter(l => !(l.event === event && l.callback === callback));
        };
    }

    /**
     * Notify all listeners of an event
     * @param {string} event - Event name
     * @param {Object} [data] - Event data
     * @private
     */
    _notify(event, data = {}) {
        this._listeners
            .filter(l => l.event === event)
            .forEach(l => l.callback(data));
    }

    // ── Status ──

    /**
     * Get current auth status
     * @returns {Object} Status object
     */
    getStatus() {
        return {
            connected: this.connected,
            isAuthenticated: this.isAuthenticated(),
            serverUrl: this.serverUrl,
            user: this.user,
            tokenExpiry: localStorage.getItem(this.TOKEN_EXPIRY_KEY)
        };
    }
}

// Expose globally for browser scripts
if (typeof window !== 'undefined') {
    window.UnifiedAuthService = UnifiedAuthService;
    
    // Create default singleton instance for backward compatibility
    window.authService = new UnifiedAuthService();
}

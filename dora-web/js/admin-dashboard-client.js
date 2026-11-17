// Admin Dashboard API Client Module for DoraMCU
// Provides methods for fetching dashboard data and subscribing to real-time updates

class AdminDashboardClient {
    constructor(baseUrl) {
        this.baseUrl = baseUrl || 'http://localhost:8080';
        this.token = localStorage.getItem('doramcu_auth_token');
        this.ws = null;
        this.wsReconnectTimer = null;
        this.onUpdateCallback = null;
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

    _headers() {
        const headers = { 'Content-Type': 'application/json' };
        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }
        return headers;
    }

    // Fetch the combined dashboard summary
    async getDashboardSummary() {
        const response = await fetch(`${this.baseUrl}/api/v1/admin/ai-orders/dashboard/summary`, {
            method: 'GET',
            headers: this._headers()
        });
        if (!response.ok) {
            throw new Error(`Dashboard summary failed: ${response.status} ${response.statusText}`);
        }
        return response.json();
    }

    // Fetch AI admins filtered by galaxy
    async getAdminsByGalaxy(galaxyName) {
        const response = await fetch(
            `${this.baseUrl}/api/v1/galaxy-3d/galaxy/${encodeURIComponent(galaxyName)}/ai-admins`,
            { method: 'GET', headers: this._headers() }
        );
        if (!response.ok) {
            throw new Error(`Galaxy admins failed: ${response.status} ${response.statusText}`);
        }
        return response.json();
    }

    // Fetch AI admin positions in a galaxy
    async getAdminPositions(galaxyName) {
        const response = await fetch(
            `${this.baseUrl}/api/v1/admin/ai-orders/admins/${encodeURIComponent(galaxyName)}/positions`,
            { method: 'GET', headers: this._headers() }
        );
        if (!response.ok) {
            throw new Error(`Admin positions failed: ${response.status} ${response.statusText}`);
        }
        return response.json();
    }

    // Fetch dispatch summary only
    async getDispatchSummary() {
        const response = await fetch(`${this.baseUrl}/api/v1/admin/ai-orders/dispatch/summary`, {
            method: 'GET',
            headers: this._headers()
        });
        if (!response.ok) {
            throw new Error(`Dispatch summary failed: ${response.status} ${response.statusText}`);
        }
        return response.json();
    }

    // Subscribe to real-time dashboard updates via WebSocket
    subscribeToUpdates(callback) {
        this.onUpdateCallback = callback;
        this._connectWebSocket();
    }

    // Unsubscribe and close WebSocket
    unsubscribe() {
        this.onUpdateCallback = null;
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        if (this.wsReconnectTimer) {
            clearTimeout(this.wsReconnectTimer);
            this.wsReconnectTimer = null;
        }
    }

    _connectWebSocket() {
        if (this.ws && (this.ws.readyState === WebSocket.CONNECTING || this.ws.readyState === WebSocket.OPEN)) {
            return;
        }

        const wsProtocol = this.baseUrl.startsWith('https') ? 'wss' : 'ws';
        const wsUrl = `${wsProtocol}://${this.baseUrl.replace(/^https?:\/\//, '').split('/')[0]}/ws/presence`;
        const queryParams = [];
        if (this.token) {
            queryParams.push(`token=${encodeURIComponent(this.token)}`);
        }
        const fullUrl = `${wsUrl}${queryParams.length ? '?' + queryParams.join('&') : ''}`;

        this.ws = new WebSocket(fullUrl);

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                if (data.type === 'dashboard_update' && this.onUpdateCallback) {
                    this.onUpdateCallback(data);
                }
            } catch (e) {
                console.warn('AdminDashboardClient: failed to parse WebSocket message', e);
            }
        };

        this.ws.onclose = () => {
            console.log('AdminDashboardClient: WebSocket disconnected, reconnecting in 5s...');
            this.wsReconnectTimer = setTimeout(() => {
                if (this.onUpdateCallback) {
                    this._connectWebSocket();
                }
            }, 5000);
        };

        this.ws.onerror = (error) => {
            console.error('AdminDashboardClient: WebSocket error', error);
        };
    }
}

// Export as global
window.AdminDashboardClient = AdminDashboardClient;

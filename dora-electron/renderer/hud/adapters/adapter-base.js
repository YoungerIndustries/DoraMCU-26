/* Game Module Adapter — Base class for HUD game adapters */

class GameModuleAdapter {
    constructor(apiBase) {
        this.apiBase = apiBase;
        this.hudController = null;
        this.subscribers = {
            stateChange: [],
            narrativeUpdate: []
        };
    }

    // ── Initialization ──

    init(hudController) {
        this.hudController = hudController;
        console.log(`[${this.constructor.name}] Initialized with API base: ${this.apiBase}`);
    }

    destroy() {
        this.subscribers = { stateChange: [], narrativeUpdate: [] };
        this.hudController = null;
    }

    // ── API Fetch Helper ──

    async _fetch(path, options = {}) {
        const url = this.apiBase + path;
        const headers = { ...options.headers };

        // Add auth token if available
        if (window.authService && window.authService.authToken) {
            headers['Authorization'] = `Bearer ${window.authService.authToken}`;
        }

        try {
            const response = await fetch(url, {
                method: options.method || 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    ...headers
                },
                body: options.body ? JSON.stringify(options.body) : undefined,
            });

            if (!response.ok) {
                if (response.status === 404) {
                    return null;
                }
                throw new Error(`API ${response.status}: ${response.statusText}`);
            }

            if (response.status === 204) {
                return null;
            }

            return await response.json();
        } catch (err) {
            console.warn(`[${this.constructor.name}] API error: ${path}`, err);
            throw err;
        }
    }

    // ── Event Subscription ──

    onStateChange(callback) {
        this.subscribers.stateChange.push(callback);
        return () => {
            this.subscribers.stateChange = this.subscribers.stateChange.filter(cb => cb !== callback);
        };
    }

    onNarrativeUpdate(callback) {
        this.subscribers.narrativeUpdate.push(callback);
        return () => {
            this.subscribers.narrativeUpdate = this.subscribers.narrativeUpdate.filter(cb => cb !== callback);
        };
    }

    _notifyStateChange(state) {
        for (const cb of this.subscribers.stateChange) {
            try {
                cb(state);
            } catch (err) {
                console.warn(`[${this.constructor.name}] State change callback error`, err);
            }
        }
    }

    _notifyNarrativeUpdate(prompt) {
        for (const cb of this.subscribers.narrativeUpdate) {
            try {
                cb(prompt);
            } catch (err) {
                console.warn(`[${this.constructor.name}] Narrative update callback error`, err);
            }
        }
    }

    // ── Abstract Methods (override in subclass) ──

    async getGameState() {
        throw new Error('getGameState not implemented');
    }

    async getNarrativePrompt() {
        throw new Error('getNarrativePrompt not implemented');
    }

    async getAvailableActions() {
        throw new Error('getAvailableActions not implemented');
    }

    async executeAction(actionId, params = {}) {
        throw new Error('executeAction not implemented');
    }
}

// Expose globally
window.GameModuleAdapter = GameModuleAdapter;

/**
 * DoraMCU Electron Bridge
 * 
 * This script is injected into the web UI when running inside the Electron desktop app.
 * It provides the web UI with access to Electron-specific features:
 * - Game client launch/stop/status
 * - Desktop notifications
 * - Native file dialogs
 * - Server health monitoring
 * - App info (version, platform)
 * 
 * The web UI detects Electron by checking for window.electronAPI.
 */

(function() {
    'use strict';

    // Check if we're running in Electron
    if (!window.electronAPI || !window.electronAPI.isElectron) {
        console.log('[DoraMCU] Not running in Electron - bridge disabled');
        return;
    }

    console.log('[DoraMCU] Electron bridge loaded - desktop features enabled');

    // Mark the page as running in Electron
    window.__isElectron = true;

    // ─── Game Client Manager ───────────────────────────────────────────────

    const GameClientManager = {
        _status: 'unknown',
        _listeners: [],

        /**
         * Download and start the Unity game client
         * @param {string} [serverUrl] - Optional server URL override
         * @returns {Promise<{success: boolean, error?: string}>}
         */
        async launch(serverUrl) {
            try {
                const result = await window.electronAPI.downloadAndStartGameClient(serverUrl);
                this._status = result.success ? 'starting' : 'error';
                this._notifyListeners(result);
                return result;
            } catch (err) {
                this._status = 'error';
                this._notifyListeners({ success: false, error: err.message });
                return { success: false, error: err.message };
            }
        },

        /**
         * Start the game client if already downloaded
         * @returns {Promise<{success: boolean}>}
         */
        async start() {
            try {
                this._status = 'starting';
                this._notifyListeners({ status: 'starting' });
                return await window.electronAPI.startGameClient();
            } catch (err) {
                this._status = 'error';
                return { success: false, error: err.message };
            }
        },

        /**
         * Stop the running game client
         * @returns {Promise<{success: boolean}>}
         */
        async stop() {
            try {
                this._status = 'stopping';
                this._notifyListeners({ status: 'stopping' });
                return await window.electronAPI.stopGameClient();
            } catch (err) {
                return { success: false, error: err.message };
            }
        },

        /**
         * Get the current game client status
         * @returns {Promise<{running: boolean}>}
         */
        async getStatus() {
            try {
                const result = await window.electronAPI.getGameClientStatus();
                this._status = result.running ? 'running' : 'stopped';
                return result;
            } catch (err) {
                return { running: false, error: err.message };
            }
        },

        /**
         * Register a listener for game client status changes
         * @param {Function} callback - Receives status update objects
         * @returns {Function} Unsubscribe function
         */
        onStatusChange(callback) {
            this._listeners.push(callback);
            return () => {
                this._listeners = this._listeners.filter(fn => fn !== callback);
            };
        },

        _notifyListeners(data) {
            this._listeners.forEach(fn => {
                try {
                    fn(data);
                } catch (err) {
                    console.error('[DoraMCU] Game client listener error:', err);
                }
            });
        }
    };

    // Set up game client event listeners
    window.electronAPI.onGameClientDeploymentStatus((data) => {
        GameClientManager._status = data.status || 'unknown';
        GameClientManager._notifyListeners(data);
        // Dispatch custom event for web UI components
        window.dispatchEvent(new CustomEvent('game-client-deployment', { detail: data }));
    });

    window.electronAPI.onGameClientStatusUpdate((data) => {
        GameClientManager._notifyListeners(data);
        window.dispatchEvent(new CustomEvent('game-client-status', { detail: data }));
    });

    // ─── Desktop Notifications ─────────────────────────────────────────────

    const Notifications = {
        /**
         * Show a native desktop notification
         * @param {string} title - Notification title
         * @param {string} body - Notification body text
         */
        async show(title, body) {
            try {
                await window.electronAPI.showNotification(title, body);
            } catch (err) {
                console.error('[DoraMCU] Failed to show notification:', err);
            }
        }
    };

    // ─── Native Dialogs ────────────────────────────────────────────────────

    const Dialogs = {
        /**
         * Open a file selection dialog
         * @param {Object} [options] - Dialog options
         * @returns {Promise<{filePaths?: string[], canceled: boolean}>}
         */
        async openFile(options) {
            return await window.electronAPI.openFileDialog(options || {});
        },

        /**
         * Open a save file dialog
         * @param {Object} [options] - Dialog options
         * @returns {Promise<{filePath?: string, canceled: boolean}>}
         */
        async saveFile(options) {
            return await window.electronAPI.saveFileDialog(options || {});
        },

        /**
         * Show a message box (alert/confirm)
         * @param {Object} options - Message box options
         * @param {string} options.message - Message text
         * @param {string} [options.title] - Dialog title
         * @param {string} [options.type] - 'info', 'warning', 'error', 'question'
         * @param {string[]} [options.buttons] - Button labels
         * @returns {Promise<{response: number, checkboxChecked: boolean}>}
         */
        async messageBox(options) {
            return await window.electronAPI.showMessageBox(options || {});
        },

        /**
         * Simple alert dialog
         * @param {string} message - Message to display
         * @param {string} [title] - Optional title
         */
        async alert(message, title) {
            return this.messageBox({
                type: 'info',
                title: title || 'DoraMCU',
                message: message,
                buttons: ['OK']
            });
        },

        /**
         * Confirmation dialog
         * @param {string} message - Question to ask
         * @param {string} [title] - Optional title
         * @returns {Promise<boolean>} True if user confirmed
         */
        async confirm(message, title) {
            const result = await this.messageBox({
                type: 'question',
                title: title || 'DoraMCU',
                message: message,
                buttons: ['Cancel', 'OK']
            });
            return result.response === 1;
        }
    };

    // ─── App Info ───────────────────────────────────────────────────────────

    const AppInfo = {
        _cache: null,

        /**
         * Get app information (version, platform, server URL)
         * @returns {Promise<{version: string, platform: string, serverUrl: string, serverOnline: boolean}>}
         */
        async get() {
            this._cache = await window.electronAPI.getAppInfo();
            return this._cache;
        },

        /**
         * Check if the Spring Boot backend server is online
         * @returns {Promise<{online: boolean}>}
         */
        async isServerOnline() {
            const result = await window.electronAPI.checkServerHealth();
            return result;
        },

        /**
         * Reload the web UI
         */
        reload() {
            window.electronAPI.reloadWebUI();
        }
    };

    // ─── Expose to window ──────────────────────────────────────────────────

    window.DoraElectron = {
        gameClient: GameClientManager,
        notifications: Notifications,
        dialogs: Dialogs,
        appInfo: AppInfo,

        /**
         * Check if running in Electron
         * @returns {boolean}
         */
        get isElectron() {
            return true;
        }
    };

    // Dispatch ready event so web UI components can listen for it
    window.dispatchEvent(new CustomEvent('electron-ready', {
        detail: { api: window.DoraElectron }
    }));

})();

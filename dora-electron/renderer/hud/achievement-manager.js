/**
 * AchievementManager — Cross-game achievement tracking and display.
 *
 * Provides:
 * - Achievement grid with unlock status and rarity colors
 * - Leaderboard with rank, score, and player stats
 * - Player stats dashboard (hours played, games completed, wins/losses)
 * - Game history with results and replay links
 * - Cross-game inventory display
 * - Real-time achievement notifications via WebSocket
 *
 * Usage:
 *   const manager = new AchievementManager(containerElement, playerId, apiBaseUrl);
 *   manager.init();
 *   manager.connect();
 *   manager.destroy();
 */

class AchievementManager {
    /**
     * @param {HTMLElement} container - DOM container for the achievement UI
     * @param {string} playerId - Player ID for tracking
     * @param {string} apiBaseUrl - Base URL for the API
     */
    constructor(container, playerId, apiBaseUrl) {
        this.container = container;
        this.playerId = playerId || 'anonymous';
        this.apiBaseUrl = apiBaseUrl || '/api/v1/achievements';

        // WebSocket connection
        this.ws = null;
        this.wsConnected = false;
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 10;
        this.reconnectDelay = 1000;
        this.pingInterval = null;

        // Data
        this.achievements = [];
        this.leaderboard = [];
        this.stats = null;
        this.history = [];
        this.inventory = [];
        this.notifications = [];
        this.progress = null;

        // UI state
        this.currentTab = 'achievements'; // 'achievements', 'leaderboard', 'stats', 'history', 'inventory'
        this.filterCategory = 'all';

        // Callbacks
        this.callbacks = {
            onAchievementUnlocked: null,
            onNotification: null,
            onError: null
        };

        // Create UI
        this._createUI();
    }

    /**
     * Set callbacks for achievement events.
     * @param {object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        this.callbacks = { ...this.callbacks, ...callbacks };
    }

    /**
     * Create the achievement UI structure.
     */
    _createUI() {
        if (!this.container) return;

        this.container.innerHTML = `
            <div class="achievement-manager" id="achievementManager">
                <!-- Tab Navigation -->
                <div class="achievement-tabs" id="achievementTabs">
                    <button class="achievement-tab active" data-tab="achievements">🏆 Achievements</button>
                    <button class="achievement-tab" data-tab="leaderboard">📊 Leaderboard</button>
                    <button class="achievement-tab" data-tab="stats">📈 Stats</button>
                    <button class="achievement-tab" data-tab="history">📜 History</button>
                    <button class="achievement-tab" data-tab="inventory">🎒 Inventory</button>
                </div>

                <!-- Notification Bar -->
                <div class="achievement-notification-bar" id="achievementNotificationBar" style="display:none;">
                    <span class="notification-icon">🏆</span>
                    <span class="notification-text" id="notificationText"></span>
                    <button class="notification-close" id="notificationClose">×</button>
                </div>

                <!-- Progress Bar -->
                <div class="achievement-progress" id="achievementProgress">
                    <div class="progress-header">
                        <span class="progress-label">Overall Progress</span>
                        <span class="progress-stats" id="progressStats">0/0 unlocked</span>
                    </div>
                    <div class="progress-bar-track">
                        <div class="progress-bar-fill" id="progressBarFill" style="width: 0%"></div>
                    </div>
                    <div class="progress-points" id="progressPoints">0 points</div>
                </div>

                <!-- Filter Bar -->
                <div class="achievement-filter-bar" id="achievementFilterBar">
                    <select class="filter-select" id="categoryFilter">
                        <option value="all">All Categories</option>
                        <option value="combat">⚔️ Combat</option>
                        <option value="exploration">🗺️ Exploration</option>
                        <option value="social">🤝 Social</option>
                        <option value="rpg">✨ RPG</option>
                        <option value="strategy">🏗️ Strategy</option>
                    </select>
                    <select class="filter-select" id="rarityFilter">
                        <option value="all">All Rarities</option>
                        <option value="common">Common</option>
                        <option value="uncommon">Uncommon</option>
                        <option value="rare">Rare</option>
                        <option value="epic">Epic</option>
                        <option value="legendary">Legendary</option>
                    </select>
                    <select class="filter-select" id="statusFilter">
                        <option value="all">All Status</option>
                        <option value="unlocked">Unlocked</option>
                        <option value="locked">Locked</option>
                    </select>
                </div>

                <!-- Content Area -->
                <div class="achievement-content" id="achievementContent">
                    <!-- Achievements Grid -->
                    <div class="achievement-grid" id="achievementGrid"></div>

                    <!-- Leaderboard -->
                    <div class="leaderboard-table" id="leaderboardTable" style="display:none;">
                        <table>
                            <thead>
                                <tr>
                                    <th>Rank</th>
                                    <th>Player</th>
                                    <th>Score</th>
                                    <th>Games</th>
                                    <th>W/L</th>
                                    <th>Hours</th>
                                </tr>
                            </thead>
                            <tbody id="leaderboardBody"></tbody>
                        </table>
                    </div>

                    <!-- Stats Dashboard -->
                    <div class="stats-dashboard" id="statsDashboard" style="display:none;">
                        <div class="stats-grid">
                            <div class="stat-card">
                                <div class="stat-icon">🎮</div>
                                <div class="stat-value" id="statGamesCompleted">0</div>
                                <div class="stat-label">Games Completed</div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">🏆</div>
                                <div class="stat-value" id="statWins">0</div>
                                <div class="stat-label">Wins</div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">💀</div>
                                <div class="stat-value" id="statLosses">0</div>
                                <div class="stat-label">Losses</div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">⏱️</div>
                                <div class="stat-value" id="statHoursPlayed">0</div>
                                <div class="stat-label">Hours Played</div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">📊</div>
                                <div class="stat-value" id="statWinRate">0%</div>
                                <div class="stat-label">Win Rate</div>
                            </div>
                            <div class="stat-card">
                                <div class="stat-icon">⭐</div>
                                <div class="stat-value" id="statTotalPoints">0</div>
                                <div class="stat-label">Total Points</div>
                            </div>
                        </div>
                    </div>

                    <!-- Game History -->
                    <div class="game-history" id="gameHistory" style="display:none;">
                        <div class="history-list" id="historyList"></div>
                    </div>

                    <!-- Inventory -->
                    <div class="inventory-grid" id="inventoryGrid" style="display:none;"></div>
                </div>
            </div>
        `;

        // Bind events
        this._bindEvents();
    }

    /**
     * Bind UI events.
     */
    _bindEvents() {
        // Tab switching
        document.querySelectorAll('.achievement-tab').forEach(tab => {
            tab.addEventListener('click', () => {
                this._switchTab(tab.dataset.tab);
            });
        });

        // Filters
        document.getElementById('categoryFilter').addEventListener('change', (e) => {
            this.filterCategory = e.target.value;
            this._renderAchievements();
        });

        const rarityFilter = document.getElementById('rarityFilter');
        rarityFilter.addEventListener('change', (e) => {
            this.filterRarity = e.target.value;
            this._renderAchievements();
        });

        const statusFilter = document.getElementById('statusFilter');
        statusFilter.addEventListener('change', (e) => {
            this.filterStatus = e.target.value;
            this._renderAchievements();
        });

        // Notification close
        document.getElementById('notificationClose').addEventListener('click', () => {
            document.getElementById('achievementNotificationBar').style.display = 'none';
        });
    }

    /**
     * Switch between tabs.
     * @param {string} tab - Tab name
     */
    _switchTab(tab) {
        this.currentTab = tab;

        // Update tab buttons
        document.querySelectorAll('.achievement-tab').forEach(t => {
            t.classList.toggle('active', t.dataset.tab === tab);
        });

        // Show/hide content
        document.getElementById('achievementGrid').style.display = tab === 'achievements' ? 'grid' : 'none';
        document.getElementById('leaderboardTable').style.display = tab === 'leaderboard' ? 'block' : 'none';
        document.getElementById('statsDashboard').style.display = tab === 'stats' ? 'block' : 'none';
        document.getElementById('gameHistory').style.display = tab === 'history' ? 'block' : 'none';
        document.getElementById('inventoryGrid').style.display = tab === 'inventory' ? 'grid' : 'none';

        // Show/hide filter bar
        document.getElementById('achievementFilterBar').style.display = tab === 'achievements' ? 'flex' : 'none';

        // Load data for tab
        switch (tab) {
            case 'leaderboard':
                this.loadLeaderboard();
                break;
            case 'stats':
                this.loadStats();
                break;
            case 'history':
                this.loadHistory();
                break;
            case 'inventory':
                this.loadInventory();
                break;
        }
    }

    /**
     * Initialize the achievement manager.
     */
    init() {
        this.loadAchievements();
        this.loadProgress();
        console.log('[AchievementManager] Initialized');
    }

    /**
     * Connect to WebSocket for real-time notifications.
     */
    connect() {
        const serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';
        const wsUrl = `${serverUrl.replace('http', 'ws')}/ws/achievements`;

        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
            this.wsConnected = true;
            this.reconnectAttempts = 0;
            console.log('[AchievementManager] WebSocket connected');

            // Subscribe
            this._send({ type: 'subscribe', playerId: this.playerId });

            // Start ping
            this._startPing();
        };

        this.ws.onmessage = (event) => {
            this._handleMessage(JSON.parse(event.data));
        };

        this.ws.onclose = () => {
            this.wsConnected = false;
            this._stopPing();
            console.log('[AchievementManager] WebSocket disconnected');
            this._reconnect();
        };

        this.ws.onerror = (error) => {
            console.error('[AchievementManager] WebSocket error:', error);
        };
    }

    /**
     * Handle incoming WebSocket messages.
     * @param {object} data - Message data
     */
    _handleMessage(data) {
        switch (data.type) {
            case 'achievements':
                this.achievements = data.achievements || [];
                this._renderAchievements();
                break;
            case 'notifications':
                this.notifications = data.notifications || [];
                this._showNotification(this.notifications[this.notifications.length - 1]);
                break;
            case 'achievement_unlocked':
                this._handleAchievementUnlocked(data);
                break;
            case 'unlock_response':
                if (data.unlocked) {
                    this.loadAchievements();
                    this.loadProgress();
                }
                break;
            case 'pong':
                break;
            case 'error':
                console.error('[AchievementManager] Error:', data.message);
                if (this.callbacks.onError) {
                    this.callbacks.onError(data.message);
                }
                break;
        }
    }

    /**
     * Handle achievement unlock event.
     * @param {object} data - Achievement data
     */
    _handleAchievementUnlocked(data) {
        this._showNotification({
            achievementName: data.name,
            description: data.description,
            icon: data.icon
        });

        if (this.callbacks.onAchievementUnlocked) {
            this.callbacks.onAchievementUnlocked(data);
        }

        // Refresh achievements
        this.loadAchievements();
        this.loadProgress();
    }

    /**
     * Show a notification.
     * @param {object} notification - Notification data
     */
    _showNotification(notification) {
        if (!notification) return;

        const bar = document.getElementById('achievementNotificationBar');
        const text = document.getElementById('notificationText');
        text.textContent = `${notification.icon} ${notification.achievementName}: ${notification.description}`;
        bar.style.display = 'flex';

        // Auto-hide after 5 seconds
        setTimeout(() => {
            bar.style.display = 'none';
        }, 5000);

        if (this.callbacks.onNotification) {
            this.callbacks.onNotification(notification);
        }
    }

    /**
     * Send a message via WebSocket.
     * @param {object} data - Message data
     */
    _send(data) {
        if (this.ws && this.wsConnected) {
            this.ws.send(JSON.stringify(data));
        }
    }

    /**
     * Start ping keepalive.
     */
    _startPing() {
        this.pingInterval = setInterval(() => {
            this._send({ type: 'ping' });
        }, 30000);
    }

    /**
     * Stop ping keepalive.
     */
    _stopPing() {
        if (this.pingInterval) {
            clearInterval(this.pingInterval);
            this.pingInterval = null;
        }
    }

    /**
     * Reconnect with exponential backoff.
     */
    _reconnect() {
        if (this.reconnectAttempts >= this.maxReconnectAttempts) {
            console.warn('[AchievementManager] Max reconnection attempts reached');
            return;
        }

        this.reconnectAttempts++;
        const delay = this.reconnectDelay * Math.pow(2, this.reconnectAttempts - 1);
        console.log(`[AchievementManager] Reconnecting in ${delay}ms (attempt ${this.reconnectAttempts})`);

        setTimeout(() => {
            this.connect();
        }, delay);
    }

    /**
     * Load achievements from REST API.
     */
    async loadAchievements() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/${this.playerId}`);
            if (response.ok) {
                this.achievements = await response.json();
                this._renderAchievements();
            }
        } catch (error) {
            console.error('[AchievementManager] Error loading achievements:', error);
        }
    }

    /**
     * Load progress from REST API.
     */
    async loadProgress() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/${this.playerId}/progress`);
            if (response.ok) {
                this.progress = await response.json();
                this._renderProgress();
            }
        } catch (error) {
            console.error('[AchievementManager] Error loading progress:', error);
        }
    }

    /**
     * Load leaderboard from REST API.
     */
    async loadLeaderboard() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/leaderboard?limit=100`);
            if (response.ok) {
                this.leaderboard = await response.json();
                this._renderLeaderboard();
            }
        } catch (error) {
            console.error('[AchievementManager] Error loading leaderboard:', error);
        }
    }

    /**
     * Load stats from REST API.
     */
    async loadStats() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/${this.playerId}/stats`);
            if (response.ok) {
                this.stats = await response.json();
                this._renderStats();
            }
        } catch (error) {
            console.error('[AchievementManager] Error loading stats:', error);
        }
    }

    /**
     * Load history from REST API.
     */
    async loadHistory() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/${this.playerId}/history?limit=50`);
            if (response.ok) {
                this.history = await response.json();
                this._renderHistory();
            }
        } catch (error) {
            console.error('[AchievementManager] Error loading history:', error);
        }
    }

    /**
     * Load inventory from REST API.
     */
    async loadInventory() {
        try {
            const response = await fetch(`${this.apiBaseUrl}/${this.playerId}/inventory`);
            if (response.ok) {
                this.inventory = await response.json();
                this._renderInventory();
            }
        } catch (error) {
            console.error('[AchievementManager] Error loading inventory:', error);
        }
    }

    /**
     * Render achievements grid.
     */
    _renderAchievements() {
        const grid = document.getElementById('achievementGrid');
        if (!grid) return;

        let filtered = this.achievements;

        // Apply filters
        if (this.filterCategory !== 'all') {
            filtered = filtered.filter(a => a.category === this.filterCategory);
        }
        if (this.filterRarity && this.filterRarity !== 'all') {
            filtered = filtered.filter(a => a.rarity === this.filterRarity);
        }
        if (this.filterStatus && this.filterStatus !== 'all') {
            filtered = filtered.filter(a => this.filterStatus === 'unlocked' ? a.unlocked : !a.unlocked);
        }

        const rarityColors = {
            common: '#9ca3af',
            uncommon: '#22c55e',
            rare: '#3b82f6',
            epic: '#a855f7',
            legendary: '#f59e0b'
        };

        grid.innerHTML = filtered.map(achievement => `
            <div class="achievement-card ${achievement.unlocked ? 'unlocked' : 'locked'}"
                 data-rarity="${achievement.rarity}">
                <div class="achievement-icon">${achievement.icon}</div>
                <div class="achievement-name">${achievement.name}</div>
                <div class="achievement-description">${achievement.description}</div>
                <div class="achievement-meta">
                    <span class="achievement-rarity" style="color: ${rarityColors[achievement.rarity] || '#9ca3af'}">
                        ${achievement.rarity}
                    </span>
                    <span class="achievement-points">${achievement.points} pts</span>
                    ${achievement.unlocked ? `<span class="achievement-unlocked">✓ Unlocked</span>` : '<span class="achievement-locked">🔒 Locked</span>'}
                </div>
            </div>
        `).join('');
    }

    /**
     * Render progress bar.
     */
    _renderProgress() {
        if (!this.progress) return;

        const percentage = this.progress.total > 0 ? (this.progress.unlocked / this.progress.total * 100) : 0;
        document.getElementById('progressStats').textContent = `${this.progress.unlocked}/${this.progress.total} unlocked`;
        document.getElementById('progressBarFill').style.width = `${percentage}%`;
        document.getElementById('progressPoints').textContent = `${this.progress.points} points`;
    }

    /**
     * Render leaderboard.
     */
    _renderLeaderboard() {
        const body = document.getElementById('leaderboardBody');
        if (!body) return;

        body.innerHTML = this.leaderboard.map(entry => `
            <tr class="${entry.playerId === this.playerId ? 'current-player' : ''}">
                <td class="rank">${entry.rank}</td>
                <td class="player">${entry.playerId}</td>
                <td class="score">${entry.score.toLocaleString()}</td>
                <td class="games">${entry.gamesCompleted}</td>
                <td class="wl">${entry.wins}/${entry.losses}</td>
                <td class="hours">${entry.hoursPlayed.toFixed(1)}</td>
            </tr>
        `).join('');
    }

    /**
     * Render stats dashboard.
     */
    _renderStats() {
        if (!this.stats) return;

        document.getElementById('statGamesCompleted').textContent = this.stats.gamesCompleted;
        document.getElementById('statWins').textContent = this.stats.wins;
        document.getElementById('statLosses').textContent = this.stats.losses;
        document.getElementById('statHoursPlayed').textContent = this.stats.hoursPlayed.toFixed(1);

        const totalGames = this.stats.wins + this.stats.losses;
        const winRate = totalGames > 0 ? (this.stats.wins / totalGames * 100) : 0;
        document.getElementById('statWinRate').textContent = `${winRate.toFixed(1)}%`;
        document.getElementById('statTotalPoints').textContent = this.progress ? this.progress.points : 0;
    }

    /**
     * Render game history.
     */
    _renderHistory() {
        const list = document.getElementById('historyList');
        if (!list) return;

        list.innerHTML = this.history.map(entry => `
            <div class="history-entry">
                <div class="history-icon">${entry.result === 'win' ? '🏆' : entry.result === 'loss' ? '💀' : '🔄'}</div>
                <div class="history-details">
                    <div class="history-game">${entry.gameType}</div>
                    <div class="history-result">${entry.result}</div>
                    <div class="history-duration">${entry.durationMinutes} min</div>
                </div>
                <div class="history-date">${new Date(entry.timestamp).toLocaleDateString()}</div>
                ${entry.replayId ? `<button class="history-replay" data-replay="${entry.replayId}">▶ Replay</button>` : ''}
            </div>
        `).join('');
    }

    /**
     * Render inventory.
     */
    _renderInventory() {
        const grid = document.getElementById('inventoryGrid');
        if (!grid) return;

        grid.innerHTML = this.inventory.map(item => `
            <div class="inventory-item ${item.equipped ? 'equipped' : ''}">
                <div class="item-icon">${item.icon}</div>
                <div class="item-name">${item.name}</div>
                <div class="item-description">${item.description}</div>
                <div class="item-category">${item.category}</div>
            </div>
        `).join('');
    }

    /**
     * Unlock an achievement.
     * @param {string} achievementId - Achievement ID
     */
    unlockAchievement(achievementId) {
        this._send({ type: 'unlock', playerId: this.playerId, achievementId });
    }

    /**
     * Get the current progress.
     * @returns {object} Progress data
     */
    getProgress() {
        return this.progress;
    }

    /**
     * Get the current leaderboard.
     * @returns {Array} Leaderboard entries
     */
    getLeaderboard() {
        return this.leaderboard;
    }

    /**
     * Get the current stats.
     * @returns {object} Player stats
     */
    getStats() {
        return this.stats;
    }

    /**
     * Destroy the achievement manager and clean up resources.
     */
    destroy() {
        this._stopPing();

        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }

        if (this.container) {
            this.container.innerHTML = '';
        }

        this.achievements = [];
        this.leaderboard = [];
        this.stats = null;
        this.history = [];
        this.inventory = [];
        this.notifications = [];

        console.log('[AchievementManager] Destroyed');
    }
}

// Expose globally
window.AchievementManager = AchievementManager;

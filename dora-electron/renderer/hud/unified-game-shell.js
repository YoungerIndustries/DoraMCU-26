/* Unified Game Shell
   Main game play interface that adapts panels, controls, and 3D view based on game type */

class UnifiedGameShell {
    constructor(container) {
        this.container = container || document.getElementById('game-shell-container');
        this.currentGameType = null;
        this.currentProfile = null;
        this.currentSession = null;
        this.adapter = null;
        this.scene3D = null;
        this.gameStateClient = null;
        this.wsConnected = false;
        this.musicPlayer = null;
        this.callbacks = {
            onGameStart: null,
            onGameEnd: null,
            onAction: null,
            onStateChange: null,
            onError: null
        };

        // Create the shell UI
        this._createUI();
    }

    /**
     * Set callbacks for game events.
     * @param {object} callbacks - Callback functions
     */
    setCallbacks(callbacks) {
        this.callbacks = { ...this.callbacks, ...callbacks };
    }

    /**
     * Create the unified game shell UI structure.
     */
    _createUI() {
        if (!this.container) {
            console.error('[UnifiedGameShell] No container element found');
            return;
        }

        this.container.innerHTML = `
            <div class="game-shell" id="gameShell">
                <!-- Game State Bar -->
                <div class="game-state-bar" id="gameStateBar">
                    <div class="game-state-bar-left">
                        <span class="game-type-badge" id="gameTypeBadge">--</span>
                        <span class="session-name" id="sessionName">No Session</span>
                        <span class="player-info" id="playerInfo">--</span>
                    </div>
                    <div class="game-state-bar-center" id="gameStateBarCenter">
                        <!-- Dynamic state items -->
                    </div>
                    <div class="game-state-bar-right">
                        <button class="game-shell-btn" id="pauseBtn" title="Pause">⏸️</button>
                        <button class="game-shell-btn" id="minimapToggleBtn" title="Toggle Minimap">🗺️</button>
                        <button class="game-shell-btn" id="musicToggleBtn" title="Toggle Music Player">🎵</button>
                        <button class="game-shell-btn" id="fullscreenBtn" title="Fullscreen">⛶</button>
                        <button class="game-shell-btn" id="screenshotBtn" title="Screenshot">📸</button>
                        <button class="game-shell-btn" id="settingsBtn" title="Settings">⚙️</button>
                    </div>
                </div>

                <!-- Main Game Area -->
                <div class="game-main-area" id="gameMainArea">
                    <!-- Left Panel (Info) -->
                    <div class="game-panel game-panel-left" id="gamePanelLeft">
                        <div class="panel-header">
                            <h3 class="panel-title" id="panelLeftTitle">Info</h3>
                        </div>
                        <div class="panel-content" id="panelLeftContent">
                            <!-- Dynamic components -->
                        </div>
                    </div>

                    <!-- Center Area -->
                    <div class="game-center-area" id="gameCenterArea">
                        <!-- 3D View Container -->
                        <div class="game-3d-view" id="game3DView">
                            <canvas id="game3DCanvas"></canvas>
                            <!-- FPS Crosshair Overlay -->
                            <div class="fps-crosshair" id="fpsCrosshair" style="display:none;">
                                <div class="crosshair-dot"></div>
                                <div class="crosshair-line crosshair-top"></div>
                                <div class="crosshair-line crosshair-bottom"></div>
                                <div class="crosshair-line crosshair-left"></div>
                                <div class="crosshair-line crosshair-right"></div>
                            </div>
                            <!-- Minimap Overlay -->
                            <div class="game-minimap" id="gameMinimap" style="display:none;">
                                <canvas id="minimapCanvas" width="200" height="200"></canvas>
                            </div>
                        </div>

                        <!-- Prompt/Narrative Panel -->
                        <div class="game-panel game-panel-prompt" id="gamePanelPrompt">
                            <div class="panel-header">
                                <h3 class="panel-title" id="panelPromptTitle">Narrative</h3>
                            </div>
                            <div class="panel-content" id="panelPromptContent">
                                <!-- Dynamic narrative content -->
                            </div>
                        </div>
                    </div>

                    <!-- Right Panel (Actions) -->
                    <div class="game-panel game-panel-right" id="gamePanelRight">
                        <div class="panel-header">
                            <h3 class="panel-title" id="panelRightTitle">Actions</h3>
                        </div>
                        <div class="panel-content" id="panelRightContent">
                            <!-- Dynamic action buttons -->
                        </div>
                    </div>

                    <!-- Bottom Panel (Log) -->
                    <div class="game-panel game-panel-bottom" id="gamePanelBottom">
                        <div class="panel-header">
                            <h3 class="panel-title" id="panelBottomTitle">Log</h3>
                            <button class="panel-clear-btn" id="clearLogBtn" title="Clear log">🗑️</button>
                        </div>
                        <div class="panel-content game-log" id="gameLogContent">
                            <!-- Log entries -->
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Bind event listeners
        this._bindEvents();
    }

    /**
     * Bind event listeners for game shell controls.
     */
    _bindEvents() {
        const pauseBtn = document.getElementById('pauseBtn');
        if (pauseBtn) {
            pauseBtn.addEventListener('click', () => this._togglePause());
        }

        const minimapToggleBtn = document.getElementById('minimapToggleBtn');
        if (minimapToggleBtn) {
            minimapToggleBtn.addEventListener('click', () => this._toggleMinimap());
        }

        const musicToggleBtn = document.getElementById('musicToggleBtn');
        if (musicToggleBtn) {
            musicToggleBtn.addEventListener('click', () => this._toggleMusicPlayer());
        }

        const fullscreenBtn = document.getElementById('fullscreenBtn');
        if (fullscreenBtn) {
            fullscreenBtn.addEventListener('click', () => this._toggleFullscreen());
        }

        const screenshotBtn = document.getElementById('screenshotBtn');
        if (screenshotBtn) {
            screenshotBtn.addEventListener('click', () => this._takeScreenshot());
        }

        const settingsBtn = document.getElementById('settingsBtn');
        if (settingsBtn) {
            settingsBtn.addEventListener('click', () => this._openSettings());
        }

        const clearLogBtn = document.getElementById('clearLogBtn');
        if (clearLogBtn) {
            clearLogBtn.addEventListener('click', () => this._clearLog());
        }
    }

    /**
     * Initialize the game shell with a specific game type.
     * @param {string} gameType - Game type enum name (e.g., 'TRAVELLER', 'FPS')
     * @param {string} sessionId - Game session ID
     * @param {object} gameSpecificTemplate - Optional game-specific template overrides
     */
    async init(gameType, sessionId, gameSpecificTemplate = null) {
        try {
            this.currentGameType = gameType;
            this.currentSession = sessionId;

            // Get UI profile for this game type
            this.currentProfile = window.getProfileForGameType(gameType);

            // Load unified template
            const template = new window.UnifiedGameTemplate();
            const mergedTemplate = template.loadTemplate(gameType, gameSpecificTemplate);

            // Apply layout based on profile
            this._applyLayout(mergedTemplate);

            // Apply theme
            this._applyTheme(mergedTemplate.theme);

            // Setup state bar
            this._setupStateBar(mergedTemplate);

            // Setup panels
            await this._setupPanels(mergedTemplate);

            // Setup 3D view
            this._setup3DView(mergedTemplate.view3D);

            // Update UI elements
            document.getElementById('gameTypeBadge').textContent = this.currentProfile.displayName;
            document.getElementById('sessionName').textContent = sessionId || 'No Session';

            // Add log entry
            this._addLog('system', `Game shell initialized: ${gameType} (${this.currentProfile.displayName})`);

            if (this.callbacks.onGameStart) {
                this.callbacks.onGameStart({ gameType, sessionId, profile: this.currentProfile });
            }

            console.log(`[UnifiedGameShell] Initialized for ${gameType} with profile ${this.currentProfile.id}`);

        } catch (error) {
            console.error('[UnifiedGameShell] Init error:', error);
            if (this.callbacks.onError) {
                this.callbacks.onError(error);
            }
        }
    }

    /**
     * Apply layout based on UI profile.
     * @param {object} template - Merged template configuration
     */
    _applyLayout(template) {
        const gameShell = document.getElementById('gameShell');
        const gameMainArea = document.getElementById('gameMainArea');

        // Remove existing layout classes
        gameShell.classList.remove('layout-fullscreen', 'layout-split', 'layout-sidebar');

        // Apply layout class
        switch (template.layout) {
            case 'fullscreen':
                gameShell.classList.add('layout-fullscreen');
                // Hide side panels for FPS
                document.getElementById('gamePanelLeft').style.display = 'none';
                document.getElementById('gamePanelRight').style.display = 'none';
                document.getElementById('gamePanelPrompt').style.display = 'none';
                break;
            case 'split':
                gameShell.classList.add('layout-split');
                document.getElementById('gamePanelLeft').style.display = 'block';
                document.getElementById('gamePanelRight').style.display = 'block';
                document.getElementById('gamePanelPrompt').style.display = 'block';
                break;
            case 'sidebar':
                gameShell.classList.add('layout-sidebar');
                document.getElementById('gamePanelLeft').style.display = 'block';
                document.getElementById('gamePanelRight').style.display = 'none';
                document.getElementById('gamePanelPrompt').style.display = 'block';
                break;
            default:
                gameShell.classList.add('layout-split');
        }
    }

    /**
     * Apply theme colors.
     * @param {object} theme - Theme configuration
     */
    _applyTheme(theme) {
        const root = document.documentElement;
        root.style.setProperty('--game-shell-accent', theme.accentColor);
        root.style.setProperty('--game-shell-panel-bg', theme.panelBackground);
        root.style.setProperty('--game-shell-border', theme.borderColor);
    }

    /**
     * Setup the game state bar.
     * @param {object} template - Merged template configuration
     */
    _setupStateBar(template) {
        const center = document.getElementById('gameStateBarCenter');
        center.innerHTML = '';

        template.stateBar.items.forEach(item => {
            const stateItem = document.createElement('div');
            stateItem.className = 'game-state-item';
            stateItem.id = `state-${item}`;
            stateItem.innerHTML = `
                <span class="state-label">${item.replace(/_/g, ' ').toUpperCase()}</span>
                <span class="state-value" id="state-value-${item}">--</span>
            `;
            center.appendChild(stateItem);
        });
    }

    /**
     * Setup panels with components.
     * @param {object} template - Merged template configuration
     */
    async _setupPanels(template) {
        // Left panel (Info)
        const leftTitle = document.getElementById('panelLeftTitle');
        const leftContent = document.getElementById('panelLeftContent');
        leftTitle.textContent = template.panels.info.title;
        leftContent.innerHTML = '';
        for (const component of template.panels.info.components) {
            leftContent.appendChild(this._createComponent(component));
        }

        // Prompt panel (Narrative)
        const promptTitle = document.getElementById('panelPromptTitle');
        const promptContent = document.getElementById('panelPromptContent');
        promptTitle.textContent = template.panels.prompt.title;
        promptContent.innerHTML = '';
        for (const component of template.panels.prompt.components) {
            promptContent.appendChild(this._createComponent(component));
        }

        // Right panel (Actions)
        const rightTitle = document.getElementById('panelRightTitle');
        const rightContent = document.getElementById('panelRightContent');
        rightTitle.textContent = template.panels.responses.title;
        rightContent.innerHTML = '';
        for (const component of template.panels.responses.components) {
            const btn = this._createComponent(component);
            btn.addEventListener('click', () => this._handleAction(component.binding));
            rightContent.appendChild(btn);
        }

        // Bottom panel (Log) - title set by profile
        const bottomTitle = document.getElementById('panelBottomTitle');
        bottomTitle.textContent = template.panels.actions.title;
    }

    /**
     * Create a UI component element.
     * @param {object} component - Component configuration
     * @returns {HTMLElement} Created element
     */
    _createComponent(component) {
        const el = document.createElement('div');
        el.className = `game-component game-component-${component.type}`;
        el.dataset.binding = component.binding;

        switch (component.type) {
            case 'stat-bar':
                el.innerHTML = `
                    <div class="stat-bar-header">
                        <span class="stat-label">${component.label}</span>
                        <span class="stat-value" id="stat-value-${component.binding}">--</span>
                    </div>
                    <div class="stat-bar-track">
                        <div class="stat-bar-fill" id="stat-fill-${component.binding}" style="width: 0%"></div>
                    </div>
                `;
                break;
            case 'stat-number':
                el.innerHTML = `
                    <span class="stat-label">${component.label}:</span>
                    <span class="stat-value" id="stat-value-${component.binding}">--</span>
                `;
                break;
            case 'selector':
                el.innerHTML = `
                    <label class="selector-label">${component.label}</label>
                    <select class="game-selector" id="selector-${component.binding}">
                        <option value="">Select...</option>
                    </select>
                `;
                break;
            case 'action-button':
                el.className += ' action-button';
                el.innerHTML = `
                    <span class="action-icon">${component.icon || '🔘'}</span>
                    <span class="action-label">${component.label}</span>
                `;
                break;
            case 'narrative-text':
                el.innerHTML = `<div class="narrative-text" id="narrative-${component.binding}">${component.label}</div>`;
                break;
            case 'kill-feed':
                el.innerHTML = `<div class="kill-feed" id="kill-feed-${component.binding}"></div>`;
                break;
            case 'event-log':
            case 'combat-log':
            case 'battle-log':
                el.innerHTML = `<div class="game-log-scroll" id="log-${component.binding}"></div>`;
                break;
            case 'dice-roller':
                el.innerHTML = `
                    <div class="dice-roller" id="dice-roller-${component.binding}">
                        <button class="dice-btn" data-dice="d6">d6</button>
                        <button class="dice-btn" data-dice="d20">d20</button>
                        <span class="dice-result" id="dice-result">--</span>
                    </div>
                `;
                // Bind dice buttons
                el.querySelectorAll('.dice-btn').forEach(btn => {
                    btn.addEventListener('click', () => this._rollDice(btn.dataset.dice));
                });
                break;
            case 'scoreboard':
                el.innerHTML = `<div class="scoreboard" id="scoreboard-${component.binding}"></div>`;
                break;
            case 'chat-input':
                el.innerHTML = `
                    <input type="text" class="game-chat-input" id="chat-${component.binding}" 
                           placeholder="${component.placeholder || 'Type...'}" />
                `;
                const chatInput = el.querySelector('input');
                chatInput.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter' && chatInput.value.trim()) {
                        this._handleChat(chatInput.value.trim());
                        chatInput.value = '';
                    }
                });
                break;
            default:
                el.textContent = component.label || component.type;
        }

        return el;
    }

    /**
     * Setup 3D view based on profile.
     * @param {object} view3D - 3D view configuration
     */
    _setup3DView(view3D) {
        const crosshair = document.getElementById('fpsCrosshair');
        const minimap = document.getElementById('gameMinimap');

        // Show/hide crosshair
        crosshair.style.display = view3D.showCrosshair ? 'block' : 'none';

        // Show/hide minimap
        minimap.style.display = view3D.showMinimap ? 'block' : 'none';

        // Setup canvas
        const canvas = document.getElementById('game3DCanvas');
        const container = document.getElementById('game3DView');
        canvas.width = container.clientWidth;
        canvas.height = container.clientHeight;

        // Initialize Three.js scene if available
        if (window.THREE) {
            this._initThreeJSScene(view3D);
        }
    }

    /**
     * Initialize Three.js scene for the game using Adaptive3DView.
     * @param {object} view3D - 3D view configuration
     */
    _initThreeJSScene(view3D) {
        const container = document.getElementById('game3DView');
        if (!container) {
            console.warn('[UnifiedGameShell] No game3DView container found');
            return;
        }

        // Use Adaptive3DView if available, otherwise fall back to basic Three.js
        if (window.Adaptive3DView) {
            this.scene3D = new Adaptive3DView(container);
            this.scene3D.init();

            // Map camera mode from profile to Adaptive3DView mode
            const cameraMode = view3D.cameraMode || 'galaxy';
            this.scene3D.setCameraMode(cameraMode);

            // Show/hide minimap based on profile
            if (view3D.showMinimap) {
                this.scene3D.setMinimapVisible(true);
            }

            this.scene3D.setCallbacks({
                onModeChange: (mode) => {
                    console.log('[UnifiedGameShell] 3D mode changed to:', mode);
                },
                onObjectClick: (userData, point) => {
                    if (this.callbacks.onAction) {
                        this.callbacks.onAction({ type: 'object_click', data: userData, point: point });
                    }
                },
                onObjectHover: (userData) => {
                    console.log('[UnifiedGameShell] Object hovered:', userData.id);
                }
            });

            console.log('[UnifiedGameShell] Adaptive3DView initialized with mode:', cameraMode);
        } else if (window.THREE) {
            // Fallback to basic Three.js scene
            const canvas = document.getElementById('game3DCanvas');
            if (!canvas) return;

            this.scene3D = {
                scene: new window.THREE.Scene(),
                camera: new window.THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000),
                renderer: new window.THREE.WebGLRenderer({ canvas, antialias: true }),
                animationId: null
            };

            this.scene3D.renderer.setSize(container.clientWidth, container.clientHeight);
            this.scene3D.renderer.setClearColor(0x000000);

            const ambientLight = new window.THREE.AmbientLight(0x404040, 0.5);
            this.scene3D.scene.add(ambientLight);

            const directionalLight = new window.THREE.DirectionalLight(0xffffff, 1);
            directionalLight.position.set(5, 10, 7);
            this.scene3D.scene.add(directionalLight);

            switch (view3D.cameraMode) {
                case 'fps':
                    this.scene3D.camera.position.set(0, 1.6, 5);
                    break;
                case 'galaxy':
                    this.scene3D.camera.position.set(0, 20, 30);
                    this.scene3D.camera.lookAt(0, 0, 0);
                    break;
                case 'tactical':
                    this.scene3D.camera.position.set(10, 15, 10);
                    this.scene3D.camera.lookAt(0, 0, 0);
                    break;
                case 'narrative':
                    this.scene3D.camera.position.set(0, 5, 10);
                    break;
            }

            // Animation loop
            this._animationLoop();
        }
    }

    /**
     * Animation loop for basic Three.js scene fallback.
     */
    _animationLoop() {
        if (!this.scene3D || this._isPaused) return;
        if (!this.scene3D.renderer) return; // Adaptive3DView has its own loop

        this.scene3D.renderer.render(this.scene3D.scene, this.scene3D.camera);
        this.scene3D.animationId = requestAnimationFrame(() => this._animationLoop());
    }

    /**
     * Update a state value.
     * @param {string} binding - State binding name
     * @param {number|string} value - New value
     */
    updateState(binding, value) {
        const valueEl = document.getElementById(`state-value-${binding}`);
        if (valueEl) {
            valueEl.textContent = value;
        }

        // Also update stat bars
        const statValueEl = document.getElementById(`stat-value-${binding}`);
        if (statValueEl) {
            statValueEl.textContent = value;
        }

        const statFillEl = document.getElementById(`stat-fill-${binding}`);
        if (statFillEl && typeof value === 'number') {
            const percentage = Math.min(100, Math.max(0, value));
            statFillEl.style.width = `${percentage}%`;
        }

        if (this.callbacks.onStateChange) {
            this.callbacks.onStateChange({ binding, value });
        }
    }

    /**
     * Update narrative text.
     * @param {string} text - Narrative text
     */
    updateNarrative(text) {
        const narrativeEl = document.querySelector('.narrative-text');
        if (narrativeEl) {
            narrativeEl.textContent = text;
        }
    }

    /**
     * Add a log entry.
     * @param {string} type - Log type (system, combat, narrative, event)
     * @param {string} message - Log message
     */
    _addLog(type, message) {
        const logContent = document.getElementById('gameLogContent');
        if (!logContent) return;

        const timestamp = new Date().toLocaleTimeString();
        const entry = document.createElement('div');
        entry.className = `log-entry log-${type}`;
        entry.innerHTML = `<span class="log-timestamp">[${timestamp}]</span> <span class="log-message">${message}</span>`;
        logContent.appendChild(entry);
        logContent.scrollTop = logContent.scrollHeight;

        // Limit log entries
        while (logContent.children.length > 100) {
            logContent.removeChild(logContent.firstChild);
        }
    }

    /**
     * Handle an action button click.
     * @param {string} actionBinding - Action binding name
     */
    async _handleAction(actionBinding) {
        this._addLog('system', `Action: ${actionBinding}`);

        if (this.callbacks.onAction) {
            try {
                const result = await this.callbacks.onAction(actionBinding);
                if (result && result.message) {
                    this._addLog('system', result.message);
                }
            } catch (error) {
                console.error('[UnifiedGameShell] Action error:', error);
                this._addLog('error', `Action failed: ${error.message}`);
            }
        }
    }

    /**
     * Handle chat message.
     * @param {string} message - Chat message
     */
    _handleChat(message) {
        this._addLog('chat', message);
        if (this.adapter && this.adapter.sendChat) {
            this.adapter.sendChat(message);
        }
    }

    /**
     * Roll dice.
     * @param {string} diceType - Dice type (d6, d20, etc.)
     */
    _rollDice(diceType) {
        const sides = parseInt(diceType.substring(1));
        const result = Math.floor(Math.random() * sides) + 1;
        const resultEl = document.getElementById('dice-result');
        if (resultEl) {
            resultEl.textContent = result;
        }
        this._addLog('dice', `Rolled ${diceType}: ${result}`);
    }

    /**
     * Toggle pause state.
     */
    _togglePause() {
        this._isPaused = !this._isPaused;
        const pauseBtn = document.getElementById('pauseBtn');
        if (pauseBtn) {
            pauseBtn.textContent = this._isPaused ? '▶️' : '⏸️';
        }
        this._addLog('system', this._isPaused ? 'Game paused' : 'Game resumed');

        if (!this._isPaused && this._animationLoop) {
            this._animationLoop();
        }
    }

    /**
     * Toggle minimap visibility.
     */
    _toggleMinimap() {
        const minimap = document.getElementById('gameMinimap');
        if (minimap) {
            minimap.style.display = minimap.style.display === 'none' ? 'block' : 'none';
        }
    }

    /**
     * Toggle fullscreen.
     */
    _toggleFullscreen() {
        if (!document.fullscreenElement) {
            document.documentElement.requestFullscreen();
        } else {
            document.exitFullscreen();
        }
    }

    /**
     * Take screenshot of 3D view.
     */
    _takeScreenshot() {
        const canvas = document.getElementById('game3DCanvas');
        if (canvas) {
            const link = document.createElement('a');
            link.download = `game-screenshot-${Date.now()}.png`;
            link.href = canvas.toDataURL();
            link.click();
            this._addLog('system', 'Screenshot saved');
        }
    }

    /**
     * Open settings (placeholder).
     */
    _openSettings() {
        this._addLog('system', 'Settings panel opened');
    }

    /**
     * Clear the game log.
     */
    _clearLog() {
        const logContent = document.getElementById('gameLogContent');
        if (logContent) {
            logContent.innerHTML = '';
        }
    }

    /**
     * Destroy the game shell and cleanup resources.
     */
    destroy() {
        // Clean up WebSocket client
        if (this.gameStateClient) {
            this.gameStateClient.destroy();
            this.gameStateClient = null;
        }
        this.wsConnected = false;

        if (this.scene3D) {
            // Adaptive3DView has a destroy method
            if (this.scene3D.destroy) {
                this.scene3D.destroy();
            } else if (this.scene3D.renderer) {
                // Basic Three.js fallback
                if (this.scene3D.animationId) {
                    cancelAnimationFrame(this.scene3D.animationId);
                }
                this.scene3D.renderer.dispose();
            }
            this.scene3D = null;
        }

        if (this.container) {
            this.container.innerHTML = '';
        }

        this.currentGameType = null;
        this.currentProfile = null;
        this.currentSession = null;
        this.adapter = null;

        if (this.callbacks.onGameEnd) {
            this.callbacks.onGameEnd();
        }

        console.log('[UnifiedGameShell] Destroyed');
    }

    /**
     * Get the current game type.
     * @returns {string|null} Current game type
     */
    getGameType() {
        return this.currentGameType;
    }

    /**
     * Get the current UI profile.
     * @returns {object|null} Current UI profile
     */
    getProfile() {
        return this.currentProfile;
    }

    /**
     * Get the 3D scene object.
     * @returns {object|null} Three.js scene object
     */
    getScene3D() {
        return this.scene3D;
    }

    /**
     * Initialize WebSocket connection for real-time game state.
     * @param {string} sessionId - Game session ID
     * @param {string|number} playerId - Player ID
     */
    connectWebSocket(sessionId, playerId) {
        if (typeof GameStateClient === 'undefined') {
            console.warn('[UnifiedGameShell] GameStateClient not available');
            return;
        }
        this.gameStateClient = new GameStateClient();
        this.gameStateClient.setCallbacks({
            onConnect: () => {
                this.wsConnected = true;
                this._addLog('system', 'Connected to game state server');
                this.gameStateClient.joinSession(sessionId, playerId);
                if (this.callbacks.onWsConnect) this.callbacks.onWsConnect();
            },
            onDisconnect: (code, reason) => {
                this.wsConnected = false;
                this._addLog('system', `Disconnected: ${reason}`);
                if (this.callbacks.onWsDisconnect) this.callbacks.onWsDisconnect(code, reason);
            },
            onGameState: (data) => {
                this._updateStateFromWebSocket(data);
                if (this.callbacks.onGameState) this.callbacks.onGameState(data);
            },
            onActionResult: (data) => {
                this._addLog('action', `${data.action}: ${data.success ? 'Success' : 'Failed'}`);
                if (this.callbacks.onActionResult) this.callbacks.onActionResult(data);
            },
            onEventNotification: (data) => {
                this._addLog('event', `${data.eventType}: ${JSON.stringify(data)}`);
                if (this.callbacks.onEventNotification) this.callbacks.onEventNotification(data);
            },
            onPlayerUpdate: (data) => {
                this._addLog('player', `${data.eventType}: player ${data.playerId}`);
                if (this.callbacks.onPlayerUpdate) this.callbacks.onPlayerUpdate(data);
            },
            onTurnUpdate: (data) => {
                this._updateTurnBar(data.currentTurn, data.currentPlayerId);
                if (this.callbacks.onTurnUpdate) this.callbacks.onTurnUpdate(data);
            },
            onError: (message) => {
                this._addLog('error', message);
                if (this.callbacks.onError) this.callbacks.onError(message);
            }
        });
        this.gameStateClient.connect();
    }

    /**
     * Update the state bar from WebSocket game state data.
     * @param {object} data - Game state message
     */
    _updateStateFromWebSocket(data) {
        const stateBar = document.getElementById('gameStateBar');
        if (!stateBar || !this.currentProfile) return;

        const profile = this.currentProfile;
        const stats = stateBar.querySelectorAll('.stat-value');

        if (data.empire && profile.stateBarItems.length >= 4) {
            // 4X strategy stats
            if (stats[0]) stats[0].textContent = data.currentTurn || 0;
            if (stats[1]) stats[1].textContent = data.empire.credits || 0;
            if (stats[2]) stats[2].textContent = data.empire.minerals || 0;
            if (stats[3]) stats[3].textContent = data.empire.energy || 0;
        } else if (data.commander) {
            // RPG stats
            if (stats[0]) stats[0].textContent = data.commander.hp || 0;
            if (stats[1]) stats[1].textContent = data.commander.tempHp || 0;
            if (stats[2]) stats[2].textContent = data.commander.spellSlots || 0;
        }

        // Update turn indicator
        const turnIndicator = document.getElementById('turnIndicator');
        if (turnIndicator) {
            turnIndicator.textContent = `Turn ${data.currentTurn || 0}`;
        }

        // Update my turn indicator
        const myTurnIndicator = document.getElementById('myTurnIndicator');
        if (myTurnIndicator) {
            myTurnIndicator.textContent = data.isMyTurn ? 'Your Turn' : 'Waiting...';
            myTurnIndicator.style.color = data.isMyTurn ? '#4caf50' : '#9e9e9e';
        }
    }

    /**
     * Update the turn bar from WebSocket turn update.
     * @param {number} turn - Current turn number
     * @param {number} currentPlayerId - Current player ID
     */
    _updateTurnBar(turn, currentPlayerId) {
        const turnIndicator = document.getElementById('turnIndicator');
        if (turnIndicator) {
            turnIndicator.textContent = `Turn ${turn}`;
        }
    }

    /**
     * Send an action via WebSocket.
     * @param {string} action - Action name
     * @param {object} [params] - Optional parameters
     */
    sendAction(action, params) {
        if (this.gameStateClient && this.gameStateClient.isConnected) {
            this.gameStateClient.sendAction(action, params);
        } else if (this.callbacks.onAction) {
            // Fallback to callback
            this.callbacks.onAction(action, params);
        }
    }

    /**
     * Get the WebSocket client instance.
     * @returns {GameStateClient|null}
     */
    getWebSocketClient() {
        return this.gameStateClient;
    }

    /**
     * Get WebSocket connection status.
     * @returns {boolean}
     */
    isWebSocketConnected() {
        return this.wsConnected;
    }

    /**
     * Initialize the music player if not already created.
     */
    _initMusicPlayer() {
        if (this.musicPlayer) {
            return;
        }

        // Check if MusicPlayer class is available
        if (typeof MusicPlayer === 'undefined') {
            console.warn('[UnifiedGameShell] MusicPlayer class not found');
            return;
        }

        this.musicPlayer = new MusicPlayer({
            service: 'pandora',
            width: 400,
            height: 600
        });

        this.musicPlayer.setCallbacks({
            onReady: () => {
                console.log('[UnifiedGameShell] Music player ready');
            },
            onError: (error) => {
                console.error('[UnifiedGameShell] Music player error:', error);
            }
        });

        this.musicPlayer.init();
    }

    /**
     * Toggle the music player visibility.
     */
    _toggleMusicPlayer() {
        if (!this.musicPlayer) {
            this._initMusicPlayer();
            // After initialization, show the player
            if (this.musicPlayer) {
                setTimeout(() => {
                    this.musicPlayer.show();
                }, 100);
            }
        } else {
            this.musicPlayer.toggle();
        }
    }
}

// Expose globally
window.UnifiedGameShell = UnifiedGameShell;

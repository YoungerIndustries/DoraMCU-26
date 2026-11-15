/* HUD Controller — Template engine, panel renderer, theme switching, resize handles */

class HUDController {
    constructor() {
        this.templates = {};
        this.currentTemplate = null;
        this.adapter = null;
        this.eventLogEntries = [];
        this.diceResults = [];
        this.sceneRenderer = null;
        this.galaxyMapRenderer = null;
        this.fleetTracker = null;
        this.planetRenderer = null;
        this.presenceManager = null;
        this.resourcePipelineViz = null;
        this.narrativeViz = null;
        this.wsManager = null;

        // DOM elements (cached)
        this.el = {};
    }

    // ── Initialization ──

    init() {
        this._cacheElements();
        this._setupResizeHandles();
        this._setupThemeSelector();
        this._setup3DViewInteraction();
        this._setupToggleButtons();
        this._setupPresetControls();
        this._setupVoiceIntegration();
        this._loadLayoutState();
        this._initWebSocketManager();
        console.log('[HUD] Controller initialized');
    }

    _initWebSocketManager() {
        if (typeof WebSocketConnectionManager !== 'undefined') {
            this.wsManager = new WebSocketConnectionManager();
            console.log('[HUD] WebSocket manager initialized');
        } else {
            console.warn('[HUD] WebSocketConnectionManager not loaded');
        }
    }

    _cacheElements() {
        const ids = [
            'hudLeftPanel', 'hudRightPanel', 'hudTopBar', 'hudBottomBar',
            'hudInfoPanelBody', 'hudPromptPanelBody',
            'hudResponsesPanelBody', 'hudActionsPanelBody',
            'hud3DView', 'hud3DPlaceholder', 'hudThemeSelector', 'hudCrosshair',
            'hudEventLog', 'hudDiceResults',
            'hudLeftResize', 'hudRightResize',
            'hudLeftSplit', 'hudRightSplit',
            // Customization controls
            'hudToggleLeft', 'hudToggleRight', 'hudToggleFullscreen3D',
            'hudPresetSelector', 'hudSavePreset', 'hudDeletePreset', 'hudResetLayout',
            // Voice controls
            'hudVoiceButton', 'hudRecordingIndicator', 'hudVoiceStatus', 'hudStatusText'
        ];
        for (const id of ids) {
            this.el[id] = document.getElementById(id);
        }
    }

    // ── Template Management ──

    registerTemplate(template) {
        this.templates[template.id] = template;
        console.log(`[HUD] Template registered: ${template.id} ("${template.name}")`);

        // Add to theme selector dropdown
        if (this.el.hudThemeSelector) {
            const option = document.createElement('option');
            option.value = template.id;
            option.textContent = template.name;
            this.el.hudThemeSelector.appendChild(option);
        }
    }

    loadTemplate(templateId) {
        const template = this.templates[templateId];
        if (!template) {
            console.warn(`[HUD] Template not found: ${templateId}`);
            return;
        }

        this.currentTemplate = template;
        localStorage.setItem('hud-theme', templateId);

        // Update theme selector
        if (this.el.hudThemeSelector) {
            this.el.hudThemeSelector.value = templateId;
        }

        // Apply theme CSS variables
        this._applyTheme(template);

        // Render panels
        this._renderPanels(template);

        // Apply 3D view config
        this._applyView3D(template);

        // Dispatch event for adapters
        document.dispatchEvent(new CustomEvent('hud-template-changed', {
            detail: { templateId, template }
        }));

        this.addEventLogEntry(`Theme loaded: ${template.name}`, 'success');
        console.log(`[HUD] Template loaded: ${templateId}`);
    }

    async loadTemplatesFromPath(path) {
        try {
            const response = await fetch(path);
            if (!response.ok) throw new Error(`HTTP ${response.status}`);
            const templates = await response.json();
            const list = Array.isArray(templates) ? templates : [templates];
            for (const t of list) {
                this.registerTemplate(t);
            }
            return list;
        } catch (err) {
            console.warn(`[HUD] Failed to load templates from ${path}`, err);
            return [];
        }
    }

    // ── Theme Application ──

    _applyTheme(template) {
        const root = document.documentElement;
        if (template.theme && template.theme.cssVariables) {
            for (const [key, value] of Object.entries(template.theme.cssVariables)) {
                root.style.setProperty(key, value);
            }
        }
        // Apply panel widths
        if (template.layout) {
            if (template.layout.leftPanelWidth) {
                this.el.hudLeftPanel.style.width = template.layout.leftPanelWidth + 'px';
            }
            if (template.layout.rightPanelWidth) {
                this.el.hudRightPanel.style.width = template.layout.rightPanelWidth + 'px';
            }
            if (template.layout.leftSplitRatio !== undefined) {
                document.documentElement.style.setProperty('--hud-left-split-ratio', template.layout.leftSplitRatio);
            }
            if (template.layout.rightSplitRatio !== undefined) {
                document.documentElement.style.setProperty('--hud-right-split-ratio', template.layout.rightSplitRatio);
            }
        }
    }

    // ── Panel Rendering ──

    _renderPanels(template) {
        const panels = template.panels || {};

        // Info panel
        if (panels.info) {
            this.renderInfoPanel(panels.info);
        }

        // Prompt panel
        if (panels.prompt) {
            this.renderPromptPanel(panels.prompt);
        }

        // Responses panel
        if (panels.responses) {
            this.renderResponsesPanel(panels.responses);
        }

        // Actions panel
        if (panels.actions) {
            this.renderActionsPanel(panels.actions);
        }
    }

    renderInfoPanel(panelDef) {
        const body = this.el.hudInfoPanelBody;
        if (!body) return;

        if (!panelDef.visible) {
            body.innerHTML = '';
            return;
        }

        let html = '';
        for (const comp of (panelDef.components || [])) {
            html += this._renderComponent(comp);
        }
        body.innerHTML = html || this._emptyState('No info data');
    }

    renderPromptPanel(panelDef) {
        const body = this.el.hudPromptPanelBody;
        if (!body) return;

        if (!panelDef.visible) {
            body.innerHTML = '';
            return;
        }

        let html = '';
        for (const comp of (panelDef.components || [])) {
            html += this._renderComponent(comp);
        }
        body.innerHTML = html || this._emptyState('No prompts yet');
    }

    renderResponsesPanel(panelDef) {
        const body = this.el.hudResponsesPanelBody;
        if (!body) return;

        if (!panelDef.visible) {
            body.innerHTML = '';
            return;
        }

        let html = '';
        for (const comp of (panelDef.components || [])) {
            html += this._renderComponent(comp);
        }
        body.innerHTML = html || this._emptyState('No actions available');
    }

    renderActionsPanel(panelDef) {
        const body = this.el.hudActionsPanelBody;
        if (!body) return;

        if (!panelDef.visible) {
            body.innerHTML = '';
            return;
        }

        let html = '';
        for (const comp of (panelDef.components || [])) {
            html += this._renderComponent(comp);
        }
        body.innerHTML = html || this._emptyState('No actions yet');
    }

    _renderComponent(comp) {
        switch (comp.type) {
            case 'stat-bar':
                return `<div class="hud-stat-bar" data-stat="${comp.dataBinding || ''}">
                    <div class="hud-stat-bar-label">
                        <span>${comp.label}</span>
                        <span class="stat-value">—</span>
                    </div>
                    <div class="hud-stat-bar-track">
                        <div class="hud-stat-bar-fill" style="width: 0%"></div>
                    </div>
                </div>`;

            case 'narrative-text':
                return `<div class="hud-narrative-text" data-binding="${comp.dataBinding || ''}">
                    <p>${comp.label || 'Waiting for narrative...'}</p>
                </div>`;

            case 'action-button':
                return `<button class="hud-action-button" data-action="${comp.dataBinding || ''}">
                    <span class="action-icon">${comp.icon || '⚡'}</span>
                    <span class="action-label">${comp.label}</span>
                    ${comp.hint ? `<span class="action-hint">${comp.hint}</span>` : ''}
                </button>`;

            case 'dice-roller':
                return `<div class="hud-dice-results" id="hudDiceResults"></div>`;

            case 'event-log':
                return `<div class="hud-event-log" id="hudEventLog"></div>`;

            case 'chat-input':
                return `<div class="hud-chat-input">
                    <input type="text" placeholder="${comp.placeholder || 'Type a message...'}" />
                    <button>Send</button>
                </div>`;

            case 'selector':
                return `<select class="hud-selector" data-selector="${comp.dataBinding || ''}">
                    <option value="">${comp.label || 'Select...'}</option>
                </select>`;

            default:
                return `<div class="hud-component">${comp.label || comp.type}</div>`;
        }
    }

    _emptyState(message) {
        return `<div class="hud-empty-state">
            <span class="empty-icon">📋</span>
            <span>${message}</span>
        </div>`;
    }

    // ── 3D View Configuration ──

    _applyView3D(template) {
        const view3D = template.view3D || {};

        // Crosshair
        if (this.el.hudCrosshair) {
            this.el.hudCrosshair.style.display = view3D.showCrosshair ? 'block' : 'none';
        }

        // Minimap
        const minimap = this.el.hud3DView ? this.el.hud3DView.querySelector('.hud-minimap') : null;
        if (minimap) {
            minimap.style.display = view3D.showMinimap ? 'block' : 'none';
        }

        // 3D Scene Renderer for non-FPS themes
        this._init3DScene(template);

        // Galaxy map mode: check if template has galaxy config
        const galaxyConfig = view3D.galaxyConfig || template.galaxyConfig;
        if (galaxyConfig && galaxyConfig.enabled) {
            // Initialize galaxy map after scene is ready
            setTimeout(() => {
                this.initGalaxyMap(
                    galaxyConfig.name || 'Corellian',
                    galaxyConfig.colorScheme || 'realistic'
                );
            }, 100);
        }
    }

    // ── 3D Scene Renderer ──

    _init3DScene(template) {
        // Destroy existing fleet tracker
        this.destroyFleetTracker();

        // Destroy existing planet renderer
        this.destroyPlanetRenderer();

        // Destroy existing presence manager
        this.destroyPresenceManager();

        // Destroy existing resource pipeline viz
        this.destroyResourcePipelineViz();

        // Destroy existing narrative viz
        this.destroyNarrativeViz();

        // Destroy existing galaxy map
        this.destroyGalaxyMap();

        // Destroy existing scene
        if (this.sceneRenderer) {
            this.sceneRenderer.destroy();
            this.sceneRenderer = null;
        }

        // Remove FPS iframe for non-FPS themes
        const view3D = this.el.hud3DView;
        if (!view3D) return;

        const iframe = view3D.querySelector('iframe');
        const placeholder = this.el.hud3DPlaceholder;

        // FPS theme keeps its iframe
        if (template.id === 'fps') {
            if (placeholder) placeholder.style.display = 'none';
            return;
        }

        // Non-FPS themes: remove iframe, show 3D scene
        if (iframe) {
            iframe.style.display = 'none';
        }
        if (placeholder) {
            placeholder.style.display = 'none';
        }

        // Check if theme has scene config
        const sceneConfig = view3D.sceneConfig || template.view3D?.sceneConfig;
        if (!sceneConfig && !view3D.renderer) {
            // Build scene config from template
            const theme = template.id;
            const config = {
                theme: theme,
                backgroundColor: 0x000000
            };

            // Theme-specific defaults
            if (theme === 'captains-log') {
                config.backgroundColor = 0x000011;
                config.ambient = { intensity: 0.2, color: 0x4444ff };
                config.directional = { intensity: 0.5, color: 0x8888ff, x: 0, y: 5, z: 3 };
            } else if (theme === 'dnd-5e') {
                config.backgroundColor = 0x1a1a1a;
                config.ambient = { intensity: 0.15, color: 0xffaa44 };
                config.directional = { intensity: 0.3, color: 0xffaa44, x: 0, y: 5, z: 0 };
            } else if (theme === 'traveller') {
                config.backgroundColor = 0x0a0a1a;
                config.ambient = { intensity: 0.3, color: 0x8888aa };
                config.directional = { intensity: 0.6, color: 0xffffff, x: 10, y: 15, z: 5 };
            } else if (theme === 'babylon5') {
                config.backgroundColor = 0x000011;
                config.ambient = { intensity: 0.2, color: 0x4466aa };
                config.directional = { intensity: 0.4, color: 0x8888cc, x: 0, y: 5, z: 3 };
            } else if (theme === 'star-trek-4x') {
                config.backgroundColor = 0x000022;
                config.ambient = { intensity: 0.3, color: 0x4488ff };
                config.directional = { intensity: 0.5, color: 0x88aaff, x: 0, y: 20, z: 0 };
            } else if (theme === 'mech-warrior') {
                config.backgroundColor = 0x3a2a1a;
                config.ambient = { intensity: 0.3, color: 0xffaa44 };
                config.directional = { intensity: 0.6, color: 0xffcc88, x: 5, y: 10, z: 5 };
            } else if (theme === 'earth-2026') {
                config.backgroundColor = 0x87CEEB;
                config.ambient = { intensity: 0.5, color: 0xffffff };
                config.directional = { intensity: 0.8, color: 0xffffff, x: 5, y: 10, z: 5 };
            } else if (theme === 'zero-zero-seven') {
                config.backgroundColor = 0x0a0a0a;
                config.ambient = { intensity: 0.2, color: 0xff4444 };
                config.directional = { intensity: 0.5, color: 0xff6644, x: 3, y: 8, z: 3 };
                config.pointLights = [
                    { color: 0xff2200, intensity: 1.5, distance: 15, x: 0, y: 4, z: 0 }
                ];
            } else if (theme === 'star-wars') {
                config.backgroundColor = 0x050510;
                config.ambient = { intensity: 0.3, color: 0x4444ff };
                config.directional = { intensity: 0.6, color: 0xffffee, x: 5, y: 10, z: 5 };
                config.pointLights = [
                    { color: 0xffaa00, intensity: 2.0, distance: 20, x: 0, y: 5, z: 0 },
                    { color: 0x00aaff, intensity: 1.5, distance: 15, x: -5, y: 3, z: 5 }
                ];
            }

            // Create scene renderer
            if (typeof SceneRenderer !== 'undefined') {
                this.sceneRenderer = new SceneRenderer(view3D, config);
                this.sceneRenderer.init();
            }
            return;
        }

        // Custom scene config from template
        if (typeof SceneRenderer !== 'undefined') {
            this.sceneRenderer = new SceneRenderer(view3D, sceneConfig);
            this.sceneRenderer.init();
        }
    }

    /**
     * Update the 3D scene with game state.
     * Called by adapters to reflect game state in the 3D view.
     */
    update3DScene(state) {
        if (this.sceneRenderer) {
            this.sceneRenderer.updateScene(state);
        }
    }

    /**
     * Toggle red alert mode (Captain's Log).
     */
    toggleRedAlert(enabled) {
        if (this.sceneRenderer) {
            this.sceneRenderer.toggleRedAlert(enabled);
        }
    }

    /**
     * Get the current scene renderer instance.
     */
    getSceneRenderer() {
        return this.sceneRenderer;
    }

    // ── Galaxy Map Integration ──

    /**
     * Initialize the GalaxyMapRenderer for galaxy-themed templates.
     * @param {string} galaxyName - Name of the galaxy to load
     * @param {string} colorScheme - Color scheme (realistic, scifi, heatmap, empire, terrain)
     */
    async initGalaxyMap(galaxyName, colorScheme = 'realistic') {
        if (!this.sceneRenderer) {
            console.warn('[HUD] Scene renderer not initialized for galaxy map');
            return null;
        }

        // Destroy existing galaxy map if any
        if (this.galaxyMapRenderer) {
            this.galaxyMapRenderer.destroy();
            this.galaxyMapRenderer = null;
        }

        // Get API base URL from auth service
        const apiBaseUrl = window.authService ? window.authService.getServerUrl() + '/api/v1/galaxy-3d' : '/api/v1/galaxy-3d';

        // Create galaxy map renderer
        if (typeof GalaxyMapRenderer !== 'undefined') {
            this.galaxyMapRenderer = new GalaxyMapRenderer(this.sceneRenderer, apiBaseUrl);

            try {
                await this.galaxyMapRenderer.loadGalaxy(galaxyName, colorScheme);

                // Enable interaction
                this.galaxyMapRenderer.enableInteraction(
                    (userData) => this._onGalaxyBodyClick(userData),
                    (userData) => this._onGalaxyBodyHover(userData)
                );

                // Enable minimap
                const view3D = this.el.hud3DView;
                if (view3D) {
                    this.galaxyMapRenderer.enableMinimap(view3D);
                }

                // Start animation loop
                this.galaxyMapRenderer.startAnimation();

                this.addEventLogEntry(`Galaxy map loaded: ${galaxyName}`, 'success');
                console.log(`[HUD] Galaxy map initialized: ${galaxyName}`);
                return this.galaxyMapRenderer;
            } catch (error) {
                console.error('[HUD] Failed to initialize galaxy map:', error);
                this.addEventLogEntry(`Failed to load galaxy: ${error.message}`, 'danger');
                this.galaxyMapRenderer = null;
                return null;
            }
        } else {
            console.warn('[HUD] GalaxyMapRenderer not loaded');
            return null;
        }
    }

    /**
     * Handle click on a celestial body in the galaxy map.
     * @param {Object} userData - Body data from interaction
     */
    _onGalaxyBodyClick(userData) {
        const { id, name, type, data } = userData;

        this.addEventLogEntry(`Selected: ${name} (${type})`, 'info');

        // Show body info in the info panel
        this._showBodyInfo(data);

        // Dispatch event for adapters
        document.dispatchEvent(new CustomEvent('galaxy-body-clicked', {
            detail: { id, name, type, data }
        }));
    }

    /**
     * Handle hover over a celestial body in the galaxy map.
     * @param {Object} userData - Body data from interaction
     */
    _onGalaxyBodyHover(userData) {
        const { name, type } = userData;

        // Update status bar or tooltip
        if (this.el.hudStatusText) {
            this.el.hudStatusText.textContent = `${name} (${type})`;
        }
    }

    /**
     * Show body information in the info panel.
     * @param {Object} bodyData - Celestial body data
     */
    _showBodyInfo(bodyData) {
        const body = this.el.hudInfoPanelBody;
        if (!body) return;

        if (!bodyData) {
            body.innerHTML = this._emptyState('No body selected');
            return;
        }

        let html = `
            <div class="hud-body-info">
                <h3>${bodyData.name || 'Unknown Body'}</h3>
                <div class="hud-body-detail"><strong>Type:</strong> ${bodyData.type || 'Unknown'}</div>
                <div class="hud-body-detail"><strong>Size:</strong> ${bodyData.size || 'N/A'}</div>
                <div class="hud-body-detail"><strong>Position:</strong> (${bodyData.position?.x?.toFixed(1) || 0}, ${bodyData.position?.y?.toFixed(1) || 0}, ${bodyData.position?.z?.toFixed(1) || 0})</div>
        `;

        // Show additional properties if available
        if (bodyData.properties) {
            html += '<div class="hud-body-properties"><strong>Properties:</strong><ul>';
            for (const [key, value] of Object.entries(bodyData.properties)) {
                html += `<li>${key}: ${value}</li>`;
            }
            html += '</ul></div>';
        }

        html += '</div>';
        body.innerHTML = html;
    }

    /**
     * Set camera preset on the galaxy map.
     * @param {string} presetId - Preset ID
     */
    setGalaxyCameraPreset(presetId) {
        if (this.galaxyMapRenderer) {
            this.galaxyMapRenderer.setCameraPreset(presetId);
            this.addEventLogEntry(`Camera: ${presetId}`, 'info');
        }
    }

    /**
     * Switch color scheme on the galaxy map.
     * @param {string} scheme - Color scheme ID
     */
    async switchGalaxyColorScheme(scheme) {
        if (this.galaxyMapRenderer) {
            await this.galaxyMapRenderer.switchColorScheme(scheme);
            this.addEventLogEntry(`Color scheme: ${scheme}`, 'info');
        }
    }

    /**
     * Focus on a specific body in the galaxy map.
     * @param {string} bodyId - Body ID
     */
    focusOnGalaxyBody(bodyId) {
        if (this.galaxyMapRenderer) {
            this.galaxyMapRenderer.focusOnBody(bodyId);
        }
    }

    /**
     * Get galaxy map statistics.
     * @returns {Object} Statistics
     */
    getGalaxyMapStats() {
        if (this.galaxyMapRenderer) {
            return this.galaxyMapRenderer.getStatistics();
        }
        return {};
    }

    /**
     * Destroy the galaxy map renderer.
     */
    destroyGalaxyMap() {
        if (this.galaxyMapRenderer) {
            this.galaxyMapRenderer.destroy();
            this.galaxyMapRenderer = null;
            console.log('[HUD] Galaxy map destroyed');
        }
    }

    // ── Fleet Tracker Integration ──

    /**
     * Initialize the FleetTracker for real-time fleet visualization.
     * @returns {Promise<FleetTracker|null>} The fleet tracker instance
     */
    async initFleetTracker() {
        if (!this.sceneRenderer) {
            console.warn('[HUD] Scene renderer not initialized for fleet tracker');
            return null;
        }

        // Destroy existing fleet tracker if any
        if (this.fleetTracker) {
            this.fleetTracker.destroy();
            this.fleetTracker = null;
        }

        const apiBaseUrl = window.authService ? window.authService.getServerUrl() + '/api/v1' : '/api/v1';

        if (typeof FleetTracker !== 'undefined') {
            this.fleetTracker = new FleetTracker(this.sceneRenderer, apiBaseUrl);

            try {
                const connected = await this.fleetTracker.connect();
                if (connected) {
                    // Subscribe to fleet updates for the current galaxy
                    const galaxyId = this.galaxyMap ? this.galaxyMap.galaxyName : 'Corellian';
                    this.fleetTracker.subscribeToFleets(galaxyId);

                    // Set callbacks
                    this.fleetTracker.setCallbacks(
                        (userData) => this._onFleetClick(userData),
                        (userData) => this._onFleetHover(userData),
                        (combatData) => this._onCombatEvent(combatData)
                    );

                    // Start animation loop
                    this.fleetTracker.startAnimation();

                    this.addEventLogEntry('Fleet tracker connected', 'success');
                    console.log('[HUD] Fleet tracker initialized');
                    return this.fleetTracker;
                } else {
                    console.warn('[HUD] Fleet tracker connection failed');
                    this.addEventLogEntry('Fleet tracker connection failed', 'warning');
                    this.fleetTracker = null;
                    return null;
                }
            } catch (error) {
                console.error('[HUD] Failed to initialize fleet tracker:', error);
                this.fleetTracker = null;
                return null;
            }
        } else {
            console.warn('[HUD] FleetTracker not loaded');
            return null;
        }
    }

    /**
     * Handle click on a fleet.
     * @param {Object} userData - Fleet data
     */
    _onFleetClick(userData) {
        const { id, name, type, data } = userData;
        this.addEventLogEntry(`Fleet selected: ${name} (${type})`, 'info');
        this._showFleetInfo(data);

        document.dispatchEvent(new CustomEvent('fleet-clicked', {
            detail: { id, name, type, data }
        }));
    }

    /**
     * Handle hover over a fleet.
     * @param {Object} userData - Fleet data
     */
    _onFleetHover(userData) {
        const { name, type } = userData;
        if (this.el.hudStatusText) {
            this.el.hudStatusText.textContent = `${name} (${type})`;
        }
    }

    /**
     * Handle combat event.
     * @param {Object} combatData - Combat event data
     */
    _onCombatEvent(combatData) {
        const msg = combatData.targetName
            ? `${combatData.attackerName || 'Unknown'} attacked ${combatData.targetName} (damage: ${combatData.damage})`
            : 'Combat event detected';
        this.addEventLogEntry(msg, 'danger');
    }

    /**
     * Show fleet information in the info panel.
     * @param {Object} fleetData - Fleet data
     */
    _showFleetInfo(fleetData) {
        const body = this.el.hudInfoPanelBody;
        if (!body || !fleetData) return;

        let html = `
            <div class="hud-body-info">
                <h3>${fleetData.name || 'Unknown Fleet'}</h3>
                <div class="hud-body-detail"><strong>Type:</strong> ${fleetData.type || 'Unknown'}</div>
                <div class="hud-body-detail"><strong>Health:</strong> ${fleetData.health ?? 'N/A'} / ${fleetData.maxHealth ?? 'N/A'}</div>
                <div class="hud-body-detail"><strong>Position:</strong> (${fleetData.position?.x?.toFixed(1) || 0}, ${fleetData.position?.y?.toFixed(1) || 0}, ${fleetData.position?.z?.toFixed(1) || 0})</div>
        `;

        if (fleetData.composition) {
            html += '<div class="hud-body-properties"><strong>Composition:</strong><ul>';
            for (const [shipType, count] of Object.entries(fleetData.composition)) {
                html += `<li>${shipType}: ${count}</li>`;
            }
            html += '</ul></div>';
        }

        html += '</div>';
        body.innerHTML = html;
    }

    /**
     * Get the fleet tracker instance.
     * @returns {FleetTracker|null}
     */
    getFleetTracker() {
        return this.fleetTracker;
    }

    /**
     * Destroy the fleet tracker.
     */
    destroyFleetTracker() {
        if (this.fleetTracker) {
            this.fleetTracker.destroy();
            this.fleetTracker = null;
            console.log('[HUD] Fleet tracker destroyed');
        }
    }

    // ── Planet Renderer Integration ──

    /**
     * Initialize the PlanetRenderer for planet surface views.
     * @param {string} planetId - Planet ID to load
     * @returns {Promise<PlanetRenderer|null>}
     */
    async initPlanetRenderer(planetId) {
        if (!this.sceneRenderer) {
            console.warn('[HUD] Scene renderer not initialized for planet renderer');
            return null;
        }

        if (this.planetRenderer) {
            this.planetRenderer.destroy();
            this.planetRenderer = null;
        }

        const apiBaseUrl = window.authService ? window.authService.getServerUrl() + '/api/v1' : '/api/v1';

        if (typeof PlanetRenderer !== 'undefined') {
            this.planetRenderer = new PlanetRenderer(this.sceneRenderer, apiBaseUrl);

            try {
                await this.planetRenderer.loadPlanet(planetId);

                this.planetRenderer.enableInteraction(
                    (userData) => this._onPlanetMarkerClick(userData),
                    (userData) => this._onPlanetMarkerHover(userData)
                );

                this.planetRenderer.startAnimation();

                this.addEventLogEntry(`Planet loaded: ${this.planetRenderer.planetData?.name || planetId}`, 'success');
                console.log('[HUD] Planet renderer initialized');
                return this.planetRenderer;
            } catch (error) {
                console.error('[HUD] Failed to initialize planet renderer:', error);
                this.planetRenderer = null;
                return null;
            }
        } else {
            console.warn('[HUD] PlanetRenderer not loaded');
            return null;
        }
    }

    _onPlanetMarkerClick(userData) {
        const { id, name, type, data } = userData;
        this.addEventLogEntry(`Facility selected: ${name} (${type})`, 'info');
        this._showFleetInfo(data);

        document.dispatchEvent(new CustomEvent('planet-marker-clicked', {
            detail: { id, name, type, data }
        }));
    }

    _onPlanetMarkerHover(userData) {
        const { name, type } = userData;
        if (this.el.hudStatusText) {
            this.el.hudStatusText.textContent = `${name} (${type})`;
        }
    }

    /**
     * Set planet zoom level.
     * @param {string} level - 'space', 'orbital', or 'surface'
     */
    setPlanetZoomLevel(level) {
        if (this.planetRenderer) {
            this.planetRenderer.setZoomLevel(level);
            this.addEventLogEntry(`Planet zoom: ${level}`, 'info');
        }
    }

    getPlanetRenderer() {
        return this.planetRenderer;
    }

    destroyPlanetRenderer() {
        if (this.planetRenderer) {
            this.planetRenderer.destroy();
            this.planetRenderer = null;
            console.log('[HUD] Planet renderer destroyed');
        }
    }

    // ── Presence Manager Integration ──

    /**
     * Initialize the PresenceManager for multiplayer presence.
     * @returns {Promise<PresenceManager|null>}
     */
    async initPresenceManager() {
        if (!this.sceneRenderer) {
            console.warn('[HUD] Scene renderer not initialized for presence manager');
            return null;
        }

        if (this.presenceManager) {
            this.presenceManager.destroy();
            this.presenceManager = null;
        }

        const apiBaseUrl = window.authService ? window.authService.getServerUrl() + '/api/v1' : '/api/v1';

        if (typeof PresenceManager !== 'undefined') {
            this.presenceManager = new PresenceManager(this.sceneRenderer, apiBaseUrl);

            try {
                const connected = await this.presenceManager.connect();
                if (connected) {
                    this.presenceManager.setCallbacks(
                        (userData) => this._onPlayerClick(userData),
                        (userData) => this._onPlayerHover(userData),
                        (chatData) => this._onPlayerChat(chatData)
                    );

                    this.presenceManager.startAnimation();

                    this.addEventLogEntry(`Presence connected (${this.presenceManager.getPlayerCount()} players)`, 'success');
                    return this.presenceManager;
                } else {
                    this.addEventLogEntry('Presence connection failed', 'warning');
                    this.presenceManager = null;
                    return null;
                }
            } catch (error) {
                console.error('[HUD] Failed to initialize presence manager:', error);
                this.presenceManager = null;
                return null;
            }
        } else {
            console.warn('[HUD] PresenceManager not loaded');
            return null;
        }
    }

    _onPlayerClick(userData) {
        const { name, data } = userData;
        this.addEventLogEntry(`Player: ${name}`, 'info');

        document.dispatchEvent(new CustomEvent('player-clicked', {
            detail: userData
        }));
    }

    _onPlayerHover(userData) {
        const { name } = userData;
        if (this.el.hudStatusText) {
            this.el.hudStatusText.textContent = name;
        }
    }

    _onPlayerChat(chatData) {
        const msg = `${chatData.playerName || 'Player'}: ${chatData.message || ''}`;
        this.addEventLogEntry(msg, 'info');
    }

    getPresenceManager() {
        return this.presenceManager;
    }

    destroyPresenceManager() {
        if (this.presenceManager) {
            this.presenceManager.destroy();
            this.presenceManager = null;
            console.log('[HUD] Presence manager destroyed');
        }
    }

    // ── Resource Pipeline Viz Integration ──

    /**
     * Initialize the ResourcePipelineViz for resource pipeline visualization.
     * @param {string} galaxyId - Galaxy ID
     * @returns {Promise<ResourcePipelineViz|null>}
     */
    async initResourcePipelineViz(galaxyId) {
        if (!this.sceneRenderer) {
            console.warn('[HUD] Scene renderer not initialized for resource pipeline');
            return null;
        }

        if (this.resourcePipelineViz) {
            this.resourcePipelineViz.destroy();
            this.resourcePipelineViz = null;
        }

        const apiBaseUrl = window.authService ? window.authService.getServerUrl() + '/api/v1' : '/api/v1';

        if (typeof ResourcePipelineViz !== 'undefined') {
            this.resourcePipelineViz = new ResourcePipelineViz(this.sceneRenderer, apiBaseUrl);

            // Try WebSocket connection first for real-time updates
            const wsConnected = await this.resourcePipelineViz.connect(galaxyId);
            
            if (wsConnected) {
                // WebSocket connected - snapshot will be sent by server
                this.resourcePipelineViz.startAnimation();
                this.addEventLogEntry('Resource pipeline loaded (WebSocket mode)', 'success');
                return this.resourcePipelineViz;
            }
            
            // Fall back to REST loading
            try {
                const data = await this.resourcePipelineViz.loadPipeline(galaxyId);
                if (data) {
                    this.resourcePipelineViz.startAnimation();
                    this.addEventLogEntry('Resource pipeline loaded (REST mode)', 'success');
                    return this.resourcePipelineViz;
                } else {
                    this.addEventLogEntry('Failed to load resource pipeline', 'warning');
                    this.resourcePipelineViz = null;
                    return null;
                }
            } catch (error) {
                console.error('[HUD] Failed to initialize resource pipeline:', error);
                this.resourcePipelineViz = null;
                return null;
            }
        } else {
            console.warn('[HUD] ResourcePipelineViz not loaded');
            return null;
        }
    }

    getResourcePipelineViz() {
        return this.resourcePipelineViz;
    }

    destroyResourcePipelineViz() {
        if (this.resourcePipelineViz) {
            this.resourcePipelineViz.destroy();
            this.resourcePipelineViz = null;
            console.log('[HUD] Resource pipeline viz destroyed');
        }
    }

    // ── Narrative Viz Integration ──

    /**
     * Initialize the NarrativeViz for AI narrative event visualization.
     * @returns {Promise<NarrativeViz|null>}
     */
    async initNarrativeViz() {
        if (!this.sceneRenderer) {
            console.warn('[HUD] Scene renderer not initialized for narrative viz');
            return null;
        }

        if (this.narrativeViz) {
            this.narrativeViz.destroy();
            this.narrativeViz = null;
        }

        const apiBaseUrl = window.authService ? window.authService.getServerUrl() + '/api/v1' : '/api/v1';

        if (typeof NarrativeViz !== 'undefined') {
            this.narrativeViz = new NarrativeViz(this.sceneRenderer, apiBaseUrl);

            try {
                const connected = await this.narrativeViz.connect();
                if (connected) {
                    this.narrativeViz.setCallbacks(
                        (userData) => this._onNarrativeEventClick(userData)
                    );

                    this.narrativeViz.startAnimation();

                    this.addEventLogEntry('Narrative visualization connected', 'success');
                    return this.narrativeViz;
                } else {
                    this.addEventLogEntry('Narrative visualization connection failed', 'warning');
                    this.narrativeViz = null;
                    return null;
                }
            } catch (error) {
                console.error('[HUD] Failed to initialize narrative viz:', error);
                this.narrativeViz = null;
                return null;
            }
        } else {
            console.warn('[HUD] NarrativeViz not loaded');
            return null;
        }
    }

    _onNarrativeEventClick(userData) {
        const { name, data } = userData;
        this.addEventLogEntry(`Narrative event: ${name}`, 'info');

        document.dispatchEvent(new CustomEvent('narrative-event-clicked', {
            detail: userData
        }));
    }

    /**
     * Get the narrative timeline.
     * @returns {Array} Timeline of narrative events
     */
    getNarrativeTimeline() {
        if (this.narrativeViz) {
            return this.narrativeViz.getTimeline();
        }
        return [];
    }

    getNarrativeViz() {
        return this.narrativeViz;
    }

    destroyNarrativeViz() {
        if (this.narrativeViz) {
            this.narrativeViz.destroy();
            this.narrativeViz = null;
            console.log('[HUD] Narrative viz destroyed');
        }
    }

    _setupThemeSelector() {
        if (!this.el.hudThemeSelector) return;

        this.el.hudThemeSelector.addEventListener('change', (e) => {
            const templateId = e.target.value;
            if (templateId) {
                this.loadTemplate(templateId);
            }
        });
    }

    // ── Resize Handles ──

    _setupResizeHandles() {
        this._setupResizeHandle('hudLeftResize', 'hudLeftPanel', (width) => {
            if (this.el.hudLeftPanel) {
                this.el.hudLeftPanel.style.width = width + 'px';
            }
        });

        this._setupResizeHandle('hudRightResize', 'hudRightPanel', (width) => {
            if (this.el.hudRightPanel) {
                this.el.hudRightPanel.style.width = width + 'px';
            }
        });

        this._setupSplitHandle('hudLeftSplit', 'hudLeftPanel', (ratio) => {
            document.documentElement.style.setProperty('--hud-left-split-ratio', ratio);
        });

        this._setupSplitHandle('hudRightSplit', 'hudRightPanel', (ratio) => {
            document.documentElement.style.setProperty('--hud-right-split-ratio', ratio);
        });
    }

    _setupResizeHandle(handleId, panelId, onResize) {
        const handle = this.el[handleId];
        const panel = this.el[panelId];
        if (!handle || !panel) return;

        let startX, startWidth;

        handle.addEventListener('mousedown', (e) => {
            e.preventDefault();
            startX = e.clientX;
            startWidth = panel.offsetWidth;
            handle.classList.add('active');

            const onMouseMove = (e) => {
                const delta = e.clientX - startX;
                const newWidth = Math.max(200, Math.min(500, startWidth + delta));
                onResize(newWidth);
            };

            const onMouseUp = () => {
                handle.classList.remove('active');
                document.removeEventListener('mousemove', onMouseMove);
                document.removeEventListener('mouseup', onMouseUp);
                this.resize3DIframe();
            };

            document.addEventListener('mousemove', onMouseMove);
            document.addEventListener('mouseup', onMouseUp);
        });
    }

    _setupSplitHandle(handleId, panelId, onResize) {
        const handle = this.el[handleId];
        const panel = this.el[panelId];
        if (!handle || !panel) return;

        let startY, startRatio;

        handle.addEventListener('mousedown', (e) => {
            e.preventDefault();
            startY = e.clientY;
            const currentRatio = parseFloat(getComputedStyle(document.documentElement)
                .getPropertyValue(panelId === this.el.hudLeftPanel ? '--hud-left-split-ratio' : '--hud-right-split-ratio')) || 0.5;
            startRatio = currentRatio;
            handle.classList.add('active');

            const panelHeight = panel.offsetHeight;

            const onMouseMove = (e) => {
                const delta = e.clientY - startY;
                const ratio = Math.max(0.2, Math.min(0.8, startRatio + delta / panelHeight));
                onResize(ratio);
            };

            const onMouseUp = () => {
                handle.classList.remove('active');
                document.removeEventListener('mousemove', onMouseMove);
                document.removeEventListener('mouseup', onMouseUp);
            };

            document.addEventListener('mousemove', onMouseMove);
            document.addEventListener('mouseup', onMouseUp);
        });
    }

    // ── 3D View Interaction ──

    _setup3DViewInteraction() {
        const view3D = this.el.hud3DView;
        if (!view3D) return;

        view3D.addEventListener('click', () => {
            // Focus iframe if present
            const iframe = view3D.querySelector('iframe');
            if (iframe) iframe.focus();
        });
    }

    resize3DIframe() {
        // Notify FPS adapter of iframe resize
        if (window.FpsAdapter && window.FpsAdapter.notifyIframeResize) {
            window.FpsAdapter.notifyIframeResize();
        }
    }

    // ── Adapter Registration ──

    setAdapter(adapter) {
        this.adapter = adapter;
        console.log(`[HUD] Adapter registered: ${adapter.constructor.name}`);
    }

    // ── Event Log ──

    addEventLogEntry(message, type = 'info') {
        const time = new Date().toLocaleTimeString();
        this.eventLogEntries.push({ time, message, type });

        // Keep only last 50 entries
        if (this.eventLogEntries.length > 50) {
            this.eventLogEntries = this.eventLogEntries.slice(-50);
        }

        // Render
        this._renderEventLog();
    }

    _renderEventLog() {
        const container = this.el.hudEventLog;
        if (!container) return;

        container.innerHTML = this.eventLogEntries.map(entry => `
            <div class="hud-event-log-entry ${entry.type}">
                <div class="event-time">${entry.time}</div>
                <div class="event-message">${entry.message}</div>
            </div>
        `).join('');

        // Scroll to bottom
        const parent = container.closest('.hud-panel-body');
        if (parent) parent.scrollTop = parent.scrollHeight;
    }

    // ── Dice Results ──

    addDiceResult(diceValues, label, success = null) {
        this.diceResults.push({ diceValues, label, success });

        // Keep only last 20 results
        if (this.diceResults.length > 20) {
            this.diceResults = this.diceResults.slice(-20);
        }

        this._renderDiceResults();
    }

    _renderDiceResults() {
        const container = this.el.hudDiceResults;
        if (!container) return;

        container.innerHTML = this.diceResults.map(result => {
            const diceHtml = result.diceValues.map(val => {
                const cls = result.success !== null
                    ? (result.success ? 'success' : 'failure') : '';
                return `<div class="hud-dice-die ${cls}">${val}</div>`;
            }).join('');

            return `<div class="hud-dice-result">
                <div style="display:flex;gap:6px;">${diceHtml}</div>
                <div class="hud-dice-info">
                    <div class="dice-label">${result.label}</div>
                    <div class="dice-detail">${result.diceValues.join(' + ')} = ${result.diceValues.reduce((a, b) => a + b, 0)}</div>
                </div>
            </div>`;
        }).join('');
    }

    // ── Panel Visibility ──

    togglePanel(panel) {
        const panelMap = {
            'left': this.el.hudLeftPanel,
            'right': this.el.hudRightPanel
        };
        const el = panelMap[panel];
        if (!el) return;

        const isHidden = el.classList.contains('hidden');
        el.classList.toggle('hidden', !isHidden);

        // Update button state
        const btnId = panel === 'left' ? 'hudToggleLeft' : 'hudToggleRight';
        const btn = this.el[btnId];
        if (btn) {
            btn.classList.toggle('active', !isHidden);
        }

        // Save layout state
        this._saveLayoutState();
        // Resize iframe after panel toggle
        setTimeout(() => this.resize3DIframe(), 50);
    }

    // ── Fullscreen 3D Mode ──

    toggleFullscreen3D() {
        const container = document.getElementById('hudContainer');
        if (!container) return;

        const isFullscreen = container.classList.contains('hud-fullscreen-3d');
        container.classList.toggle('hud-fullscreen-3d', !isFullscreen);

        // Update button state
        if (this.el.hudToggleFullscreen3D) {
            this.el.hudToggleFullscreen3D.classList.toggle('active', !isFullscreen);
        }

        // Escape key exits fullscreen
        if (!isFullscreen) {
            const escapeHandler = (e) => {
                if (e.key === 'Escape') {
                    this.toggleFullscreen3D();
                    document.removeEventListener('keydown', escapeHandler);
                }
            };
            document.addEventListener('keydown', escapeHandler);
        }

        // Save layout state
        this._saveLayoutState();
        // Resize iframe after fullscreen toggle
        setTimeout(() => this.resize3DIframe(), 100);
    }

    // ── Toggle Button Setup ──

    _setupToggleButtons() {
        if (this.el.hudToggleLeft) {
            this.el.hudToggleLeft.addEventListener('click', () => this.togglePanel('left'));
        }
        if (this.el.hudToggleRight) {
            this.el.hudToggleRight.addEventListener('click', () => this.togglePanel('right'));
        }
        if (this.el.hudToggleFullscreen3D) {
            this.el.hudToggleFullscreen3D.addEventListener('click', () => this.toggleFullscreen3D());
        }
    }

    // ── Preset Management ──

    _setupPresetControls() {
        if (this.el.hudPresetSelector) {
            this.el.hudPresetSelector.addEventListener('change', (e) => {
                const presetName = e.target.value;
                if (presetName) {
                    this.loadPreset(presetName);
                }
            });
        }
        if (this.el.hudSavePreset) {
            this.el.hudSavePreset.addEventListener('click', () => this.savePreset());
        }
        if (this.el.hudDeletePreset) {
            this.el.hudDeletePreset.addEventListener('click', () => this.deletePreset());
        }
        if (this.el.hudResetLayout) {
            this.el.hudResetLayout.addEventListener('click', () => this.resetLayout());
        }

        // Populate preset dropdown
        this._refreshPresetDropdown();
    }

    _getPresets() {
        try {
            const data = localStorage.getItem('hud-presets');
            return data ? JSON.parse(data) : {};
        } catch (e) {
            return {};
        }
    }

    _savePresets(presets) {
        localStorage.setItem('hud-presets', JSON.stringify(presets));
    }

    _captureLayoutSnapshot() {
        return {
            theme: this.currentTemplateId,
            leftPanelWidth: this.el.hudLeftPanel ? this.el.hudLeftPanel.style.width : '',
            rightPanelWidth: this.el.hudRightPanel ? this.el.hudRightPanel.style.width : '',
            leftSplitRatio: getComputedStyle(document.documentElement).getPropertyValue('--hud-left-split-ratio').trim(),
            rightSplitRatio: getComputedStyle(document.documentElement).getPropertyValue('--hud-right-split-ratio').trim(),
            leftPanelHidden: this.el.hudLeftPanel ? this.el.hudLeftPanel.classList.contains('hidden') : false,
            rightPanelHidden: this.el.hudRightPanel ? this.el.hudRightPanel.classList.contains('hidden') : false,
            fullscreen3D: document.getElementById('hudContainer') ? document.getElementById('hudContainer').classList.contains('hud-fullscreen-3d') : false
        };
    }

    _applyLayoutSnapshot(snapshot) {
        // Apply theme
        if (snapshot.theme && this.templates[snapshot.theme]) {
            this.loadTemplate(snapshot.theme);
        }

        // Apply panel widths
        if (snapshot.leftPanelWidth && this.el.hudLeftPanel) {
            this.el.hudLeftPanel.style.width = snapshot.leftPanelWidth;
        }
        if (snapshot.rightPanelWidth && this.el.hudRightPanel) {
            this.el.hudRightPanel.style.width = snapshot.rightPanelWidth;
        }

        // Apply split ratios
        if (snapshot.leftSplitRatio) {
            document.documentElement.style.setProperty('--hud-left-split-ratio', snapshot.leftSplitRatio);
        }
        if (snapshot.rightSplitRatio) {
            document.documentElement.style.setProperty('--hud-right-split-ratio', snapshot.rightSplitRatio);
        }

        // Apply panel visibility
        if (this.el.hudLeftPanel) {
            this.el.hudLeftPanel.classList.toggle('hidden', snapshot.leftPanelHidden);
            if (this.el.hudToggleLeft) {
                this.el.hudToggleLeft.classList.toggle('active', snapshot.leftPanelHidden);
            }
        }
        if (this.el.hudRightPanel) {
            this.el.hudRightPanel.classList.toggle('hidden', snapshot.rightPanelHidden);
            if (this.el.hudToggleRight) {
                this.el.hudToggleRight.classList.toggle('active', snapshot.rightPanelHidden);
            }
        }

        // Apply fullscreen mode
        const container = document.getElementById('hudContainer');
        if (container) {
            container.classList.toggle('hud-fullscreen-3d', snapshot.fullscreen3D);
            if (this.el.hudToggleFullscreen3D) {
                this.el.hudToggleFullscreen3D.classList.toggle('active', snapshot.fullscreen3D);
            }
        }

        // Resize iframe
        setTimeout(() => this.resize3DIframe(), 100);
    }

    savePreset() {
        const name = prompt('Enter preset name:', 'My Layout');
        if (!name) return;

        const presets = this._getPresets();
        presets[name] = {
            snapshot: this._captureLayoutSnapshot(),
            timestamp: Date.now()
        };
        this._savePresets(presets);
        this._refreshPresetDropdown();
        this.el.hudPresetSelector.value = name;
        this.addEventLogEntry(`Preset saved: "${name}"`, 'success');
    }

    loadPreset(name) {
        const presets = this._getPresets();
        const preset = presets[name];
        if (!preset) {
            this.addEventLogEntry(`Preset not found: "${name}"`, 'warning');
            return;
        }
        this._applyLayoutSnapshot(preset.snapshot);
        this.addEventLogEntry(`Preset loaded: "${name}"`, 'success');
    }

    deletePreset() {
        const name = this.el.hudPresetSelector ? this.el.hudPresetSelector.value : '';
        if (!name) {
            this.addEventLogEntry('Select a preset to delete', 'warning');
            return;
        }
        if (!confirm(`Delete preset "${name}"?`)) return;

        const presets = this._getPresets();
        delete presets[name];
        this._savePresets(presets);
        this._refreshPresetDropdown();
        this.addEventLogEntry(`Preset deleted: "${name}"`, 'info');
    }

    _refreshPresetDropdown() {
        if (!this.el.hudPresetSelector) return;
        const presets = this._getPresets();
        const names = Object.keys(presets);

        // Save current selection
        const selected = this.el.hudPresetSelector.value;

        // Rebuild dropdown
        this.el.hudPresetSelector.innerHTML = '<option value="">Presets...</option>';
        for (const name of names.sort()) {
            const option = document.createElement('option');
            option.value = name;
            option.textContent = name;
            this.el.hudPresetSelector.appendChild(option);
        }

        // Restore selection if still valid
        if (names.includes(selected)) {
            this.el.hudPresetSelector.value = selected;
        }
    }

    resetLayout() {
        if (!confirm('Reset layout to defaults? This will clear all customizations.')) return;

        // Clear panel widths
        if (this.el.hudLeftPanel) this.el.hudLeftPanel.style.width = '';
        if (this.el.hudRightPanel) this.el.hudRightPanel.style.width = '';

        // Reset split ratios
        document.documentElement.style.setProperty('--hud-left-split-ratio', '0.4');
        document.documentElement.style.setProperty('--hud-right-split-ratio', '0.5');

        // Show panels
        if (this.el.hudLeftPanel) {
            this.el.hudLeftPanel.classList.remove('hidden');
            if (this.el.hudToggleLeft) this.el.hudToggleLeft.classList.remove('active');
        }
        if (this.el.hudRightPanel) {
            this.el.hudRightPanel.classList.remove('hidden');
            if (this.el.hudToggleRight) this.el.hudToggleRight.classList.remove('active');
        }

        // Exit fullscreen
        const container = document.getElementById('hudContainer');
        if (container) {
            container.classList.remove('hud-fullscreen-3d');
            if (this.el.hudToggleFullscreen3D) this.el.hudToggleFullscreen3D.classList.remove('active');
        }

        // Clear saved layout state
        localStorage.removeItem('hud-layout-state');

        // Resize iframe
        setTimeout(() => this.resize3DIframe(), 100);
        this.addEventLogEntry('Layout reset to defaults', 'info');
    }

    // ── Layout State Persistence ──

    _saveLayoutState() {
        const state = {
            leftPanelWidth: this.el.hudLeftPanel ? this.el.hudLeftPanel.style.width : '',
            rightPanelWidth: this.el.hudRightPanel ? this.el.hudRightPanel.style.width : '',
            leftSplitRatio: getComputedStyle(document.documentElement).getPropertyValue('--hud-left-split-ratio').trim(),
            rightSplitRatio: getComputedStyle(document.documentElement).getPropertyValue('--hud-right-split-ratio').trim(),
            leftPanelHidden: this.el.hudLeftPanel ? this.el.hudLeftPanel.classList.contains('hidden') : false,
            rightPanelHidden: this.el.hudRightPanel ? this.el.hudRightPanel.classList.contains('hidden') : false,
            fullscreen3D: document.getElementById('hudContainer') ? document.getElementById('hudContainer').classList.contains('hud-fullscreen-3d') : false
        };
        localStorage.setItem('hud-layout-state', JSON.stringify(state));
    }

    _loadLayoutState() {
        try {
            const data = localStorage.getItem('hud-layout-state');
            if (!data) return;
            const state = JSON.parse(data);

            // Apply saved state
            if (state.leftPanelWidth && this.el.hudLeftPanel) {
                this.el.hudLeftPanel.style.width = state.leftPanelWidth;
            }
            if (state.rightPanelWidth && this.el.hudRightPanel) {
                this.el.hudRightPanel.style.width = state.rightPanelWidth;
            }
            if (state.leftSplitRatio) {
                document.documentElement.style.setProperty('--hud-left-split-ratio', state.leftSplitRatio);
            }
            if (state.rightSplitRatio) {
                document.documentElement.style.setProperty('--hud-right-split-ratio', state.rightSplitRatio);
            }
            if (state.leftPanelHidden && this.el.hudLeftPanel) {
                this.el.hudLeftPanel.classList.add('hidden');
                if (this.el.hudToggleLeft) this.el.hudToggleLeft.classList.add('active');
            }
            if (state.rightPanelHidden && this.el.hudRightPanel) {
                this.el.hudRightPanel.classList.add('hidden');
                if (this.el.hudToggleRight) this.el.hudToggleRight.classList.add('active');
            }
            if (state.fullscreen3D) {
                const container = document.getElementById('hudContainer');
                if (container) {
                    container.classList.add('hud-fullscreen-3d');
                    if (this.el.hudToggleFullscreen3D) this.el.hudToggleFullscreen3D.classList.add('active');
                }
            }

            // Resize iframe after loading state
            setTimeout(() => this.resize3DIframe(), 100);
        } catch (e) {
            console.warn('[HUD] Failed to load layout state:', e);
        }
    }

    // ── Drag and Drop ──

    enableDragDrop(panelId) {
        const panel = document.getElementById(panelId);
        if (!panel) return;

        const buttons = panel.querySelectorAll('.hud-action-button');
        if (buttons.length === 0) return;

        let draggedButton = null;

        buttons.forEach(button => {
            // Add drag handle
            const handle = document.createElement('span');
            handle.className = 'hud-drag-handle';
            handle.textContent = '⠿';
            button.insertBefore(handle, button.firstChild);

            // Make button draggable
            button.draggable = true;

            button.addEventListener('dragstart', (e) => {
                draggedButton = button;
                button.classList.add('dragging');
                e.dataTransfer.effectAllowed = 'move';
                e.dataTransfer.setData('text/plain', '');
            });

            button.addEventListener('dragend', () => {
                button.classList.remove('dragging');
                // Remove drag-over class from all buttons
                buttons.forEach(btn => btn.classList.remove('drag-over'));
                draggedButton = null;
                // Save order
                this._saveComponentOrder(panelId);
            });

            button.addEventListener('dragover', (e) => {
                e.preventDefault();
                e.dataTransfer.dropEffect = 'move';
                if (button !== draggedButton) {
                    button.classList.add('drag-over');
                }
            });

            button.addEventListener('dragleave', () => {
                button.classList.remove('drag-over');
            });

            button.addEventListener('drop', (e) => {
                e.preventDefault();
                button.classList.remove('drag-over');
                if (draggedButton && button !== draggedButton) {
                    // Reorder: insert draggedButton before button
                    const parent = button.parentNode;
                    const buttonsArray = Array.from(parent.querySelectorAll('.hud-action-button'));
                    const draggedIndex = buttonsArray.indexOf(draggedButton);
                    const targetIndex = buttonsArray.indexOf(button);

                    if (draggedIndex < targetIndex) {
                        parent.insertBefore(draggedButton, button.nextSibling);
                    } else {
                        parent.insertBefore(draggedButton, button);
                    }
                }
            });
        });

        console.log(`[HUD] Drag-and-drop enabled for ${panelId} (${buttons.length} buttons)`);
    }

    _saveComponentOrder(panelId) {
        const panel = document.getElementById(panelId);
        if (!panel) return;

        const buttons = panel.querySelectorAll('.hud-action-button');
        const order = [];
        buttons.forEach(btn => {
            const action = btn.getAttribute('data-action');
            if (action) order.push(action);
        });

        localStorage.setItem(`hud-component-order-${panelId}`, JSON.stringify(order));
    }

    _loadComponentOrder(panelId) {
        try {
            const data = localStorage.getItem(`hud-component-order-${panelId}`);
            if (!data) return null;
            return JSON.parse(data);
        } catch (e) {
            return null;
        }
    }

    // ── Voice Integration ──

    _setupVoiceIntegration() {
        if (!window.voiceService) {
            console.warn('[HUD] Voice service not loaded');
            return;
        }

        const vs = window.voiceService;

        // Sync server URL from auth service
        if (window.authService) {
            vs.setServerUrl(window.authService.getServerUrl());
        }

        // Wire recording callbacks
        vs.onRecordingStart = () => this._onRecordingStart();
        vs.onRecordingStop = () => this._onRecordingStop();

        // Wire WebSocket callbacks
        vs.onVoiceCommandResult = (data) => this.handleVoiceCommand(data);
        vs.onWebSocketConnected = () => {
            this._updateVoiceStatus('WS connected');
            this.addEventLogEntry('Voice WebSocket connected', 'success');
        };
        vs.onWebSocketDisconnected = () => {
            this._updateVoiceStatus('WS disconnected');
        };

        // Wire button click
        if (this.el.hudVoiceButton) {
            this.el.hudVoiceButton.addEventListener('click', () => this._onVoiceButtonClick());
        }

        // Try to connect WebSocket if FPS session is active
        this._tryConnectVoiceWebSocket();

        console.log('[HUD] Voice integration initialized');
    }

    _tryConnectVoiceWebSocket() {
        const vs = window.voiceService;
        if (!vs) return;

        const sessionId = localStorage.getItem('fps-match-id');
        const playerId = localStorage.getItem('fps-player-id');
        const playerName = localStorage.getItem('fps-player-name');

        if (sessionId && playerId) {
            vs.connectWebSocket(sessionId, playerId, playerName || 'Player', 0)
                .then(result => {
                    if (!result.success) {
                        console.warn('[HUD] WebSocket voice connection failed:', result.error);
                    }
                });
        }
    }

    _onVoiceButtonClick() {
        const vs = window.voiceService;
        if (!vs) return;

        // Check voice settings
        const settings = window.voiceSettingsPanel ? window.voiceSettingsPanel.getSettings() : null;
        if (settings && !settings.enableCommands) {
            this.addEventLogEntry('Voice commands are disabled in settings', 'warning');
            this._updateVoiceStatus('Commands disabled');
            setTimeout(() => this._updateVoiceStatus(''), 2000);
            return;
        }

        if (vs.isCurrentlyRecording()) {
            // Stop recording and process as voice command
            this._stopRecordingAndCommand();
        } else if (settings && settings.enableLocalStt && vs.isSpeechRecognitionAvailable()) {
            // Use Web Speech API for instant voice command (local STT mode)
            this._handleSpeechRecognitionCommand();
        } else if (settings && settings.enableWebSocket && vs.isSpeechRecognitionAvailable()) {
            // WebSocket mode with local STT
            this._handleSpeechRecognitionCommand();
        } else if (vs.isSpeechRecognitionAvailable()) {
            // Use Web Speech API for instant voice command (no recording indicator needed)
            this._handleSpeechRecognitionCommand();
        } else {
            // Fall back to microphone recording
            vs.startRecording().then(result => {
                if (result.success) {
                    this._onRecordingStart();
                } else {
                    this.addEventLogEntry('Microphone access denied: ' + result.error, 'danger');
                    this._updateVoiceStatus('Mic error');
                }
            });
        }
    }

    async _handleSpeechRecognitionCommand() {
        const vs = window.voiceService;
        if (!vs) return;

        this._updateVoiceStatus('Listening... speak now');
        if (this.el.hudVoiceButton) {
            this.el.hudVoiceButton.classList.add('recording');
        }

        try {
            const result = await vs.transcribeLocal();

            // Remove recording indicator
            if (this.el.hudVoiceButton) {
                this.el.hudVoiceButton.classList.remove('recording');
            }

            if (result.success && result.text) {
                // Send the transcribed text via WebSocket or REST
                this._sendTranscribedCommand(result.text);
            } else {
                this.addEventLogEntry('No speech detected: ' + result.error, 'warning');
                this._updateVoiceStatus('No speech');
                setTimeout(() => this._updateVoiceStatus(''), 3000);
            }
        } catch (err) {
            if (this.el.hudVoiceButton) {
                this.el.hudVoiceButton.classList.remove('recording');
            }
            this.addEventLogEntry('Speech recognition error: ' + err.message, 'danger');
            this._updateVoiceStatus('Error');
            setTimeout(() => this._updateVoiceStatus(''), 3000);
        }
    }

    async _sendTranscribedCommand(text) {
        const vs = window.voiceService;
        if (!vs) return;

        this._updateVoiceStatus('Processing...');

        // Try WebSocket first
        if (vs.wsConnected && vs.ws && vs.ws.readyState === WebSocket.OPEN) {
            const sessionId = localStorage.getItem('fps-match-id') || null;
            const playerId = localStorage.getItem('fps-player-id') || null;

            const wsResult = await vs.sendVoiceCommandViaWebSocket(sessionId, playerId);
            if (wsResult) {
                if (wsResult.success) {
                    this.handleVoiceCommand(wsResult);
                } else {
                    this.addEventLogEntry('Voice command failed: ' + wsResult.error, 'danger');
                    this._updateVoiceStatus('Error');
                    setTimeout(() => this._updateVoiceStatus(''), 3000);
                }
                return;
            }
        }

        // Fall back to REST: send the transcribed text directly
        // Since REST expects audio, we use the command endpoint with the text
        this.addEventLogEntry(`Voice: ${text}`, 'info');
        this._showTranscription(text);

        // Route to adapter for immediate action
        if (this.adapter && this.adapter.handleVoiceCommand) {
            // Use VoiceCommandService via REST for interpretation
            const sessionId = localStorage.getItem('fps-match-id') || null;
            const commandResult = await vs.sendVoiceCommand(null, sessionId, null);
            if (commandResult.success) {
                this.handleVoiceCommand(commandResult);
            } else {
                // Direct adapter routing for the transcribed text
                this._updateVoiceStatus('Ready');
                setTimeout(() => this._updateVoiceStatus(''), 3000);
            }
        } else {
            this._updateVoiceStatus('Ready');
            setTimeout(() => this._updateVoiceStatus(''), 3000);
        }
    }

    _onRecordingStart() {
        if (this.el.hudVoiceButton) {
            this.el.hudVoiceButton.classList.add('recording');
        }
        if (this.el.hudRecordingIndicator) {
            this.el.hudRecordingIndicator.style.display = 'inline-flex';
        }
        this._updateVoiceStatus('Recording... click mic to stop');
    }

    _onRecordingStop() {
        if (this.el.hudVoiceButton) {
            this.el.hudVoiceButton.classList.remove('recording');
        }
        if (this.el.hudRecordingIndicator) {
            this.el.hudRecordingIndicator.style.display = 'none';
        }
    }

    _updateVoiceStatus(text) {
        if (this.el.hudVoiceStatus) {
            this.el.hudVoiceStatus.textContent = text;
        }
    }

    async _stopRecordingAndCommand() {
        const vs = window.voiceService;
        if (!vs) return;

        this._updateVoiceStatus('Processing...');

        try {
            const stopResult = await vs.stopRecording();
            this._onRecordingStop();

            if (!stopResult.success) {
                this.addEventLogEntry('Recording failed: ' + stopResult.error, 'warning');
                this._updateVoiceStatus('');
                return;
            }

            // Send as voice command
            const sessionId = localStorage.getItem('fps-match-id') || null;
            const commandResult = await vs.sendVoiceCommand(stopResult.audioBase64, sessionId, null);

            if (commandResult.success) {
                this.handleVoiceCommand(commandResult);
            } else {
                this.addEventLogEntry('Voice command failed: ' + commandResult.error, 'danger');
                this._updateVoiceStatus('Error');
                setTimeout(() => this._updateVoiceStatus(''), 3000);
            }
        } catch (err) {
            this._onRecordingStop();
            this.addEventLogEntry('Voice error: ' + err.message, 'danger');
            this._updateVoiceStatus('Error');
            setTimeout(() => this._updateVoiceStatus(''), 3000);
        }
    }

    handleVoiceCommand(result) {
        const { commandType, parameters, executed, message, transcribedText } = result;

        // Show transcribed text in prompt panel
        if (transcribedText) {
            this._showTranscription(transcribedText);
        }

        // Log the command type
        if (executed) {
            this.addEventLogEntry(`Voice: ${message} [${commandType}]`, 'success');
        } else {
            this.addEventLogEntry(`Voice: ${message || 'Unknown command'} [${commandType}]`, 'warning');
        }

        // Route to adapter if available (e.g., FPS game actions)
        if (this.adapter && this.adapter.handleVoiceCommand) {
            this.adapter.handleVoiceCommand(commandType, parameters || {});
        }

        // Speak the response via TTS if there's a message
        if (message && commandType !== 'UNKNOWN') {
            this._speakResponse(message);
        }

        this._updateVoiceStatus('Ready');
        setTimeout(() => this._updateVoiceStatus(''), 3000);
    }

    _showTranscription(text) {
        const body = this.el.hudPromptPanelBody;
        if (!body) return;

        const div = document.createElement('div');
        div.className = 'hud-narrative-text';
        div.style.cssText = 'padding:8px;margin:4px 0;border-left:3px solid var(--hud-accent-primary);background:var(--hud-bg-panel);border-radius:4px;';
        div.innerHTML = `<strong>🎤 Voice:</strong> ${text}`;
        body.prepend(div);

        // Keep only last 10 entries
        while (body.children.length > 10) {
            body.removeChild(body.lastChild);
        }
    }

    async _speakResponse(text) {
        const vs = window.voiceService;
        if (!vs) return;

        // Check if TTS is enabled in settings
        const settings = window.voiceSettingsPanel ? window.voiceSettingsPanel.getSettings() : null;
        if (settings && !settings.enableTts) return;

        try {
            // Use selected TTS voice from settings
            const voice = (settings && settings.ttsVoice) || null;
            const result = await vs.synthesize(text, voice);
            if (result.success && result.audioBase64) {
                await vs.playAudio(result.audioBase64);
            }
        } catch (err) {
            console.warn('[HUD] TTS playback failed:', err.message);
        }
    }

    // ── Public API ──

    get currentTemplateId() {
        return this.currentTemplate ? this.currentTemplate.id : null;
    }

    get adapterInstance() {
        return this.adapter;
    }

    get galaxyMapRendererInstance() {
        return this.galaxyMapRenderer;
    }

    get fleetTrackerInstance() {
        return this.fleetTracker;
    }

    get planetRendererInstance() {
        return this.planetRenderer;
    }

    get presenceManagerInstance() {
        return this.presenceManager;
    }

    get resourcePipelineVizInstance() {
        return this.resourcePipelineViz;
    }

    get narrativeVizInstance() {
        return this.narrativeViz;
    }

    /**
     * Initialize the replay visualization module.
     * @returns {Promise<ReplayViz|null>}
     */
    async initReplayViz() {
        if (!this.sceneRenderer) {
            console.warn('[HUD] Scene renderer not initialized for replay viz');
            return null;
        }

        if (this.replayViz) {
            return this.replayViz;
        }

        if (typeof ReplayViz === 'undefined') {
            console.warn('[HUD] ReplayViz not loaded');
            return null;
        }

        try {
            const serverUrl = window.authService && window.authService.getServerUrl
                ? window.authService.getServerUrl()
                : 'http://localhost:8080';

            this.replayViz = new ReplayViz(
                this.container || document.body,
                this.sceneRenderer.scene,
                this.sceneRenderer.camera
            );

            this.replayViz.connect(serverUrl);

            this.replayViz.setOnStatusChange((status) => {
                console.log('[HUD] Replay status:', status);
            });

            this.replayViz.setOnActionPlay((action) => {
                console.log('[HUD] Replay action:', action.actionType, 'at index', action.index);
            });

            this.replayViz.setOnSessionSelect((sessionId, totalActions) => {
                console.log(`[HUD] Selected replay session: ${sessionId} (${totalActions} actions)`);
            });

            console.log('[HUD] ReplayViz initialized');
            return this.replayViz;
        } catch (e) {
            console.error('[HUD] Failed to initialize ReplayViz:', e);
            return null;
        }
    }

    get replayVizInstance() {
        return this.replayViz;
    }

    /**
     * Initialize the spectator mode module.
     * @returns {Promise<SpectatorMode|null>}
     */
    async initSpectatorMode() {
        if (!this.sceneRenderer) {
            console.warn('[HUD] Scene renderer not initialized for spectator mode');
            return null;
        }

        if (this.spectatorMode) {
            return this.spectatorMode;
        }

        if (typeof SpectatorMode === 'undefined') {
            console.warn('[HUD] SpectatorMode not loaded');
            return null;
        }

        try {
            const serverUrl = window.authService && window.authService.getServerUrl
                ? window.authService.getServerUrl()
                : 'http://localhost:8080';

            this.spectatorMode = new SpectatorMode(
                this.container || document.body,
                this.sceneRenderer.scene,
                this.sceneRenderer.camera
            );

            this.spectatorMode.connect(serverUrl);

            this.spectatorMode.setOnStatusChange((status) => {
                console.log('[HUD] Spectator status:', status);
            });

            this.spectatorMode.setOnGameStateUpdate((gameState) => {
                console.log('[HUD] Spectator game state update:', gameState);
            });

            console.log('[HUD] SpectatorMode initialized');
            return this.spectatorMode;
        } catch (e) {
            console.error('[HUD] Failed to initialize SpectatorMode:', e);
            return null;
        }
    }

    get spectatorModeInstance() {
        return this.spectatorMode;
    }

    get wsManagerInstance() {
        return this.wsManager;
    }

    get resourcePipelineVizInstance() {
        return this.resourcePipelineViz;
    }

    /**
     * Get WebSocket connection status for all endpoints.
     * @returns {Object} Connection status object
     */
    getWebSocketStatus() {
        if (!this.wsManager) {
            return { overall: 'not_initialized', endpoints: {} };
        }
        return this.wsManager.getStatus();
    }

    /**
     * Initialize the ShipViewer for 3D ship design visualization.
     * @returns {ShipViewer|null} The ShipViewer instance or null on failure
     */
    initShipViewer() {
        if (this.shipViewer) {
            return this.shipViewer;
        }

        if (!window.ShipViewer) {
            console.warn('[HUD] ShipViewer not loaded');
            return null;
        }

        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';

            this.shipViewer = new ShipViewer(this.sceneRenderer, serverUrl);
            this.shipViewer.init();
            this.shipViewer.loadDesigns();
            this.shipViewer.startAnimation();

            this.shipViewer.setCallbacks({
                onDesignSelect: (design) => {
                    console.log('[HUD] Ship design selected:', design.name);
                },
                onDesignHover: (design) => {
                    console.log('[HUD] Ship design hovered:', design.name);
                }
            });

            console.log('[HUD] ShipViewer initialized');
            return this.shipViewer;
        } catch (e) {
            console.error('[HUD] Failed to initialize ShipViewer:', e);
            return null;
        }
    }

    get shipViewerInstance() {
        return this.shipViewer;
    }

    /**
     * Initialize the EconomyViz for economic data visualization.
     * @param {number} galaxyId - The galaxy ID to load
     * @returns {EconomyViz|null} The EconomyViz instance or null on failure
     */
    initEconomyViz(galaxyId) {
        if (this.economyViz) {
            return this.economyViz;
        }

        if (!window.EconomyViz) {
            console.warn('[HUD] EconomyViz not loaded');
            return null;
        }

        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';

            this.economyViz = new EconomyViz(this.sceneRenderer, serverUrl);
            this.economyViz.init();
            this.economyViz.startAnimation();
            
            if (galaxyId) {
                this.economyViz.loadEconomicData(galaxyId);
            }

            this.economyViz.setCallbacks({
                onGalaxySelect: (galaxyId) => {
                    console.log('[HUD] Galaxy economy selected:', galaxyId);
                },
                onBondClick: (bondId) => {
                    console.log('[HUD] Bond clicked:', bondId);
                }
            });

            console.log('[HUD] EconomyViz initialized');
            return this.economyViz;
        } catch (e) {
            console.error('[HUD] Failed to initialize EconomyViz:', e);
            return null;
        }
    }

    get economyVizInstance() {
        return this.economyViz;
    }

    /**
     * Initialize the ScenePersistence for camera state management.
     * @returns {ScenePersistence|null} The ScenePersistence instance or null on failure
     */
    initScenePersistence() {
        if (this.scenePersistence) {
            return this.scenePersistence;
        }

        if (!window.ScenePersistence) {
            console.warn('[HUD] ScenePersistence not loaded');
            return null;
        }

        try {
            this.scenePersistence = new ScenePersistence(this.sceneRenderer);
            this.scenePersistence.init();
            this.scenePersistence.startAutoSave();

            this.scenePersistence.setCallbacks({
                onViewpointSelect: (name) => {
                    console.log('[HUD] Viewpoint selected:', name);
                },
                onViewpointDelete: (name) => {
                    console.log('[HUD] Viewpoint deleted:', name);
                }
            });

            console.log('[HUD] ScenePersistence initialized');
            return this.scenePersistence;
        } catch (e) {
            console.error('[HUD] Failed to initialize ScenePersistence:', e);
            return null;
        }
    }

    get scenePersistenceInstance() {
        return this.scenePersistence;
    }

    /**
     * Initialize the MultiGalaxyNav for multi-galaxy navigation.
     * @returns {MultiGalaxyNav|null} The MultiGalaxyNav instance or null on failure
     */
    initMultiGalaxyNav() {
        if (this.multiGalaxyNav) {
            return this.multiGalaxyNav;
        }

        if (!window.MultiGalaxyNav) {
            console.warn('[HUD] MultiGalaxyNav not loaded');
            return null;
        }

        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';

            this.multiGalaxyNav = new MultiGalaxyNav(this.sceneRenderer, serverUrl);
            this.multiGalaxyNav.init();
            this.multiGalaxyNav.startAnimation();
            this.multiGalaxyNav.loadGalaxies();

            this.multiGalaxyNav.setCallbacks({
                onGalaxySwitch: (galaxyId, galaxy) => {
                    console.log('[HUD] Galaxy switched:', galaxy.name);
                },
                onGalaxySelect: (galaxyId) => {
                    console.log('[HUD] Galaxy selected:', galaxyId);
                }
            });

            console.log('[HUD] MultiGalaxyNav initialized');
            return this.multiGalaxyNav;
        } catch (e) {
            console.error('[HUD] Failed to initialize MultiGalaxyNav:', e);
            return null;
        }
    }

    get multiGalaxyNavInstance() {
        return this.multiGalaxyNav;
    }

    /**
     * Initialize the SpatialAudio for voice chat with 3D spatial audio.
     * @param {string} roomId - Optional room ID to join
     * @returns {SpatialAudio|null} The SpatialAudio instance or null on failure
     */
    initSpatialAudio(roomId) {
        if (this.spatialAudio) {
            return this.spatialAudio;
        }

        if (!window.SpatialAudio) {
            console.warn('[HUD] SpatialAudio not loaded');
            return null;
        }

        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';

            this.spatialAudio = new SpatialAudio(this.sceneRenderer, serverUrl);
            this.spatialAudio.init();
            this.spatialAudio.connect();
            this.spatialAudio.startAnimation();

            if (roomId) {
                this.spatialAudio.joinRoom(roomId);
            }

            this.spatialAudio.setCallbacks({
                onSpeakerActivity: (playerId, isSpeaking) => {
                    console.log('[HUD] Speaker activity:', playerId, isSpeaking);
                },
                onRoomJoin: (roomId) => {
                    console.log('[HUD] Joined voice room:', roomId);
                },
                onRoomLeave: (roomId) => {
                    console.log('[HUD] Left voice room:', roomId);
                }
            });

            console.log('[HUD] SpatialAudio initialized');
            return this.spatialAudio;
        } catch (e) {
            console.error('[HUD] Failed to initialize SpatialAudio:', e);
            return null;
        }
    }

    get spatialAudioInstance() {
        return this.spatialAudio;
    }

    /**
     * Initialize the StationViz for space station visualization.
     * @returns {StationViz|null} The StationViz instance or null on failure
     */
    initStationViz() {
        if (this.stationViz) {
            return this.stationViz;
        }

        if (!window.StationViz) {
            console.warn('[HUD] StationViz not loaded');
            return null;
        }

        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';

            this.stationViz = new StationViz(this.sceneRenderer, serverUrl);
            this.stationViz.init();
            this.stationViz.startAnimation();
            this.stationViz.loadStations();

            this.stationViz.setCallbacks({
                onStationSelect: (stationId, station) => {
                    console.log('[HUD] Station selected:', station.name);
                },
                onDockingComplete: (stationId) => {
                    console.log('[HUD] Docking complete at station:', stationId);
                }
            });

            console.log('[HUD] StationViz initialized');
            return this.stationViz;
        } catch (e) {
            console.error('[HUD] Failed to initialize StationViz:', e);
            return null;
        }
    }

    get stationVizInstance() {
        return this.stationViz;
    }

    /**
     * Initialize the UnifiedGameShell for game type-aware game play interface.
     * @param {string} gameType - Game type enum name (e.g., 'TRAVELLER', 'FPS')
     * @param {string} sessionId - Game session ID
     * @param {object} gameSpecificTemplate - Optional game-specific template overrides
     * @returns {UnifiedGameShell|null} The UnifiedGameShell instance or null on failure
     */
    initUnifiedGameShell(gameType, sessionId, gameSpecificTemplate) {
        if (this.unifiedGameShell) {
            this.unifiedGameShell.destroy();
        }

        if (!window.UnifiedGameShell) {
            console.warn('[HUD] UnifiedGameShell not loaded');
            return null;
        }

        try {
            // Find or create container
            let container = document.getElementById('game-shell-container');
            if (!container) {
                container = document.createElement('div');
                container.id = 'game-shell-container';
                container.style.width = '100%';
                container.style.height = '100%';

                // Try to find the HUD tab content area
                const hudTab = document.getElementById('hud');
                if (hudTab) {
                    hudTab.appendChild(container);
                } else {
                    console.warn('[HUD] No HUD tab found, appending to body');
                    document.body.appendChild(container);
                }
            }

            this.unifiedGameShell = new UnifiedGameShell(container);
            this.unifiedGameShell.setCallbacks({
                onGameStart: (data) => {
                    console.log('[HUD] Game started:', data.gameType, data.sessionId);
                    // Connect WebSocket when game starts
                    if (data.sessionId && data.playerId) {
                        this.unifiedGameShell.connectWebSocket(data.sessionId, data.playerId);
                    }
                },
                onGameEnd: () => {
                    console.log('[HUD] Game ended');
                },
                onAction: async (actionBinding) => {
                    console.log('[HUD] Action:', actionBinding);
                    // Try WebSocket first, then fallback
                    if (this.unifiedGameShell && this.unifiedGameShell.isWebSocketConnected()) {
                        this.unifiedGameShell.sendAction(actionBinding);
                        return { message: `Sent via WebSocket: ${actionBinding}` };
                    }
                    return { message: `Executed: ${actionBinding}` };
                },
                onStateChange: (data) => {
                    console.log('[HUD] State change:', data.binding, data.value);
                },
                onGameState: (data) => {
                    console.log('[HUD] Game state update:', data);
                },
                onTurnUpdate: (data) => {
                    console.log('[HUD] Turn update:', data.currentTurn);
                },
                onPlayerUpdate: (data) => {
                    console.log('[HUD] Player update:', data.eventType);
                },
                onError: (error) => {
                    console.error('[HUD] Game shell error:', error);
                }
            });

            await this.unifiedGameShell.init(gameType, sessionId, gameSpecificTemplate);

            console.log('[HUD] UnifiedGameShell initialized for', gameType);
            return this.unifiedGameShell;
        } catch (e) {
            console.error('[HUD] Failed to initialize UnifiedGameShell:', e);
            return null;
        }
    }

    get unifiedGameShellInstance() {
        return this.unifiedGameShell;
    }

    /**
     * Initialize the SessionBrowser for browsing and joining game sessions.
     * @returns {SessionBrowser|null} The SessionBrowser instance or null on failure
     */
    initSessionBrowser() {
        if (this.sessionBrowser) {
            return this.sessionBrowser;
        }

        if (!window.SessionBrowser) {
            console.warn('[HUD] SessionBrowser not loaded');
            return null;
        }

        try {
            // Find or create container
            let container = document.getElementById('session-browser-container');
            if (!container) {
                container = document.createElement('div');
                container.id = 'session-browser-container';
                container.style.width = '100%';
                container.style.height = '100%';

                // Try to find the sessions tab content area
                const sessionsTab = document.getElementById('sessions');
                if (sessionsTab) {
                    sessionsTab.appendChild(container);
                } else {
                    console.warn('[HUD] No sessions tab found, appending to body');
                    document.body.appendChild(container);
                }
            }

            this.sessionBrowser = new SessionBrowser(container);
            this.sessionBrowser.setCallbacks({
                onJoin: (session, result) => {
                    console.log('[HUD] Joined session:', session.name);
                    // Launch the unified game shell for this session
                    this.initUnifiedGameShell(session.gameType, session.id);
                },
                onCreate: (session) => {
                    console.log('[HUD] Created session:', session.name);
                },
                onSelect: (session) => {
                    console.log('[HUD] Selected session:', session.name);
                },
                onError: (error) => {
                    console.error('[HUD] Session browser error:', error);
                }
            });

            this.sessionBrowser.loadSessions();

            console.log('[HUD] SessionBrowser initialized');
            return this.sessionBrowser;
        } catch (e) {
            console.error('[HUD] Failed to initialize SessionBrowser:', e);
            return null;
        }
    }

    get sessionBrowserInstance() {
        return this.sessionBrowser;
    }

    /**
     * Initialize the Adaptive3DView for unified 3D game rendering.
     * @param {HTMLElement} container - Optional container element
     * @param {string} cameraMode - Optional camera mode: 'fps', 'galaxy', 'tactical', 'narrative'
     * @returns {Adaptive3DView|null} The Adaptive3DView instance or null on failure
     */
    initAdaptive3DView(container, cameraMode) {
        if (this.adaptive3DView) {
            if (cameraMode) {
                this.adaptive3DView.setCameraMode(cameraMode);
            }
            return this.adaptive3DView;
        }

        if (!window.Adaptive3DView) {
            console.warn('[HUD] Adaptive3DView not loaded');
            return null;
        }

        try {
            // Create container if not provided
            if (!container) {
                container = document.getElementById('adaptive-3d-container');
                if (!container) {
                    container = document.createElement('div');
                    container.id = 'adaptive-3d-container';
                    container.style.cssText = 'width:100%;height:100%;position:relative;';

                    // Try to find the HUD tab content area
                    const hudTab = document.getElementById('hud');
                    if (hudTab) {
                        hudTab.appendChild(container);
                    } else {
                        console.warn('[HUD] No HUD tab found, appending to body');
                        document.body.appendChild(container);
                    }
                }
            }

            this.adaptive3DView = new Adaptive3DView(container);
            this.adaptive3DView.init();

            if (cameraMode) {
                this.adaptive3DView.setCameraMode(cameraMode);
            } else {
                // Default to galaxy mode
                this.adaptive3DView.setCameraMode('galaxy');
            }

            this.adaptive3DView.setCallbacks({
                onModeChange: (mode, previousMode) => {
                    console.log('[HUD] 3D camera mode changed:', mode);
                },
                onObjectClick: (userData, point) => {
                    console.log('[HUD] 3D object clicked:', userData.id, point);
                },
                onObjectHover: (userData) => {
                    console.log('[HUD] 3D object hovered:', userData.id);
                },
                onTransitionComplete: (mode) => {
                    console.log('[HUD] 3D camera transition complete:', mode);
                }
            });

            console.log('[HUD] Adaptive3DView initialized');
            return this.adaptive3DView;
        } catch (e) {
            console.error('[HUD] Failed to initialize Adaptive3DView:', e);
            return null;
        }
    }

    get adaptive3DViewInstance() {
        return this.adaptive3DView;
    }

    /**
     * Initialize the AchievementManager for cross-game achievements.
     * @param {HTMLElement} container - Optional container element
     * @param {string} playerId - Player ID for tracking
     * @returns {AchievementManager|null} The AchievementManager instance or null on failure
     */
    initAchievementManager(container, playerId) {
        if (this.achievementManager) {
            return this.achievementManager;
        }

        if (!window.AchievementManager) {
            console.warn('[HUD] AchievementManager not loaded');
            return null;
        }

        try {
            // Create container if not provided
            if (!container) {
                container = document.getElementById('achievement-container');
                if (!container) {
                    container = document.createElement('div');
                    container.id = 'achievement-container';
                    container.style.cssText = 'width:100%;height:100%;position:relative;';

                    // Try to find the settings tab content area
                    const settingsTab = document.getElementById('settings');
                    if (settingsTab) {
                        settingsTab.appendChild(container);
                    } else {
                        console.warn('[HUD] No settings tab found, appending to body');
                        document.body.appendChild(container);
                    }
                }
            }

            const serverUrl = window.authService ? window.authService.getServerUrl() : 'http://localhost:8080';

            this.achievementManager = new AchievementManager(container, playerId || 'anonymous', `${serverUrl}/api/v1/achievements`);
            this.achievementManager.init();
            this.achievementManager.connect();

            this.achievementManager.setCallbacks({
                onAchievementUnlocked: (data) => {
                    console.log('[HUD] Achievement unlocked:', data.name);
                },
                onNotification: (notification) => {
                    console.log('[HUD] Achievement notification:', notification.achievementName);
                },
                onError: (error) => {
                    console.error('[HUD] Achievement error:', error);
                }
            });

            console.log('[HUD] AchievementManager initialized');
            return this.achievementManager;
        } catch (e) {
            console.error('[HUD] Failed to initialize AchievementManager:', e);
            return null;
        }
    }

    get achievementManagerInstance() {
        return this.achievementManager;
    }

    /**
     * Initialize the UnifiedGameShell with a specific session and player.
     * This is the main entry point for the game launch flow.
     * @param {string} sessionId - Game session ID
     * @param {string} gameType - Game type (e.g., 'TRAVELLER', 'DORAMCU_4X')
     * @param {string|number} playerId - Player ID
     * @param {object} [options] - Additional options
     */
    launchGame(sessionId, gameType, playerId, options = {}) {
        const shell = this.initUnifiedGameShell();
        if (!shell) {
            console.error('[HUD] Cannot launch game: UnifiedGameShell not initialized');
            return null;
        }
        shell.startGame(gameType, {
            sessionId,
            playerName: options.playerName || 'Player',
            playerCount: options.playerCount || 1
        });
        // Connect WebSocket for real-time sync
        shell.connectWebSocket(sessionId, playerId);
        return shell;
    }
}

// Expose globally
window.HUDController = HUDController;

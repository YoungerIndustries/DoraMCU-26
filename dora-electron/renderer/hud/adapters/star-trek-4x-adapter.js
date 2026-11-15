/* Star Trek 4X Adapter — Connects HUD panels to Star Trek 4X REST API */

class StarTrek4xAdapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.sessions = [];
        this.selectedSessionId = null;
        this.currentSession = null;
        this.factions = [];
        this.selectedFactionId = null;
        this.currentFaction = null;
        this.systems = [];
        this.selectedSystemId = null;
        this.colonies = [];
        this.tradeRoutes = [];
        this.researchQueue = [];
        this.fleetComposition = [];
        this.relations = [];
        this.empires = [];
        this.species = [];
        this.divisions = [];
        this.currentCharacter = null;
        this.currentView = 'session'; // session, exploration, expansion, exploitation, extermination, diplomacy, character

        // AI Inference integration
        this.aiAdapter = null;
        this.aiEnabled = true;
    }

    /**
     * Initialize AI inference for this adapter.
     * Call after init() to enable AI-assisted decisions.
     */
    async initAiInference(neuronService) {
        if (typeof window !== 'undefined' && window.AiInferenceAdapter) {
            this.aiAdapter = new window.AiInferenceAdapter({
                gameId: 'star-trek-4x',
                neuronService: neuronService,
                enableAi: this.aiEnabled
            });
            await this.aiAdapter.initialize();
            console.log('[StarTrek4x] AI inference initialized');
        }
    }

    /**
     * Get AI-assisted strategic recommendation for current game state.
     * @returns {Promise<Object>} AI recommendation with action and confidence
     */
    async getAiRecommendation() {
        if (!this.aiAdapter) {
            console.log('[StarTrek4x] AI not initialized, use initAiInference() first');
            return null;
        }

        const gameState = this._encodeGameState();
        const availableActions = this._getAvailableActions();

        return this.aiAdapter.requestAiDecision(gameState, availableActions);
    }

    /**
     * Get AI-assisted fleet movement recommendation.
     * @param {Object} fleet - Fleet object
     * @returns {Promise<Object>} Movement recommendation
     */
    async getAiFleetMovement(fleet) {
        if (!this.aiAdapter) return null;

        const gameState = {
            ...this._encodeGameState(),
            fleetX: fleet.x || 0,
            fleetY: fleet.y || 0,
            fleetSize: fleet.ships ? fleet.ships.length : 0,
            fleetHealth: fleet.health || 1,
            fleetFuel: fleet.fuel || 1
        };

        const actions = ['move_north', 'move_south', 'move_east', 'move_west',
                         'attack', 'defend', 'explore', 'trade', 'wait'];

        return this.aiAdapter.requestAiDecision(gameState, actions);
    }

    /**
     * Get AI-assisted diplomacy recommendation.
     * @param {string} targetFactionId - Target faction ID
     * @returns {Promise<Object>} Diplomacy recommendation
     */
    async getAiDiplomacyRecommendation(targetFactionId) {
        if (!this.aiAdapter) return null;

        const targetFaction = this.factions.find(f => f.id === targetFactionId);
        const relation = this.relations.find(r =>
            (r.faction1 === this.selectedFactionId && r.faction2 === targetFactionId) ||
            (r.faction2 === this.selectedFactionId && r.faction1 === targetFactionId)
        );

        const gameState = {
            ...this._encodeGameState(),
            targetFactionPower: targetFaction ? (targetFaction.power || 0) : 0,
            relationScore: relation ? (relation.score || 0) : 0,
            atWar: relation ? relation.atWar : false
        };

        const actions = ['declare_war', 'peace_treaty', 'trade_agreement',
                         'alliance', 'embargo', 'diplomatic_mission', 'ignore'];

        return this.aiAdapter.requestAiDecision(gameState, actions);
    }

    /**
     * Submit gameplay experience for AI learning.
     * @param {Object} gameState - Game state at decision time
     * @param {string} action - Action taken
     * @param {number} reward - Outcome reward
     */
    submitExperience(gameState, action, reward) {
        if (this.aiAdapter) {
            this.aiAdapter.submitExperience(gameState, action, reward);
        }
    }

    /**
     * Encode current game state to 64-dim vector for AI inference.
     * @returns {number[]} Normalized state vector
     * @private
     */
    _encodeGameState() {
        const vector = new Array(64).fill(0);
        let idx = 0;

        // Faction resources (indices 0-7)
        if (this.currentFaction) {
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentFaction.resources || 0) / 10000));
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentFaction.influence || 0) / 1000));
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentFaction.research || 0) / 1000));
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentFaction.population || 0) / 100000));
        }

        // Fleet strength (indices 4-7)
        vector[idx++] = Math.max(-1, Math.min(1, (this.fleetComposition.length || 0) / 50));
        vector[idx++] = Math.max(-1, Math.min(1, (this.fleetComposition.reduce((s, f) => s + (f.count || 0), 0) || 0) / 200));

        // Colony count (indices 6-7)
        vector[idx++] = Math.max(-1, Math.min(1, (this.colonies.length || 0) / 20));

        // Research progress (indices 8-11)
        if (this.currentSession) {
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentSession.currentTurn || 0) / 100));
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentSession.researchPoints || 0) / 500));
        }

        // Relations (indices 12-15)
        const avgRelation = this.relations.length > 0
            ? this.relations.reduce((s, r) => s + (r.score || 0), 0) / this.relations.length
            : 0;
        vector[idx++] = Math.max(-1, Math.min(1, avgRelation / 100));
        const atWarCount = this.relations.filter(r => r.atWar).length;
        vector[idx++] = Math.max(-1, Math.min(1, atWarCount / 5));

        // Systems controlled (indices 16-19)
        vector[idx++] = Math.max(-1, Math.min(1, (this.systems.length || 0) / 50));

        // Trade routes (indices 20-23)
        vector[idx++] = Math.max(-1, Math.min(1, (this.tradeRoutes.length || 0) / 20));

        // Current view encoding (indices 24-30)
        const views = ['session', 'exploration', 'expansion', 'exploitation', 'extermination', 'diplomacy', 'character'];
        const viewIdx = views.indexOf(this.currentView);
        if (viewIdx >= 0) {
            vector[idx++] = viewIdx / (views.length - 1);
        } else {
            idx++;
        }

        return vector;
    }

    /**
     * Get available actions based on current game state.
     * @returns {string[]} Available action names
     * @private
     */
    _getAvailableActions() {
        const actions = [];

        switch (this.currentView) {
            case 'exploration':
                actions.push('explore_system', 'scan_anomaly', 'map_route', 'send_probe');
                break;
            case 'expansion':
                actions.push('colonize', 'build_outpost', 'establish_trade', 'terraform');
                break;
            case 'exploitation':
                actions.push('mine_resources', 'trade', 'research', 'produce');
                break;
            case 'extermination':
                actions.push('attack', 'defend', 'reinforce', 'retreat', 'ambush');
                break;
            case 'diplomacy':
                actions.push('negotiate', 'declare_war', 'peace_treaty', 'alliance', 'embargo');
                break;
            default:
                actions.push('explore', 'expand', 'exploit', 'exterminate', 'diplomacy');
        }

        return actions;
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Restore session state from localStorage
        const savedSessionId = localStorage.getItem('st4x-session-id');
        if (savedSessionId) {
            this.selectedSessionId = savedSessionId;
        }
        const savedFactionId = localStorage.getItem('st4x-faction-id');
        if (savedFactionId) {
            this.selectedFactionId = savedFactionId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'star-trek-4x') {
                this._onTemplateLoaded();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'star-trek-4x') {
                this._handleAction(action);
            }
        });

        // Listen for chat input (commands)
        const chatInput = document.getElementById('hudChatInput');
        if (chatInput) {
            chatInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this._handleCommand(chatInput.value);
                    chatInput.value = '';
                }
            });
        }

        console.log('[StarTrek4xAdapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._loadSessions();
        this._loadEmpires();
        this._loadSpecies();
        this._loadDivisions();
    }

    destroy() {
        super.destroy();
    }

    // ── Action Handler ──

    _handleAction(action) {
        switch (action) {
            case 'action-create-session':
                this._createSession();
                break;
            case 'action-advance-turn':
                this._advanceTurn();
                break;
            case 'action-scan-sector':
                this._scanSector();
                break;
            case 'action-first-contact':
                this._firstContact();
                break;
            case 'action-scientific-mission':
                this._scientificMission();
                break;
            case 'action-establish-colony':
                this._establishColony();
                break;
            case 'action-build-starbase':
                this._buildStarbase();
                break;
            case 'action-claim-sector':
                this._claimSector();
                break;
            case 'action-establish-trade':
                this._establishTrade();
                break;
            case 'action-start-research':
                this._startResearch();
                break;
            case 'action-deploy-fleet':
                this._deployFleet();
                break;
            case 'action-engage-fleet':
                this._engageFleet();
                break;
            case 'action-planetary-invasion':
                this._planetaryInvasion();
                break;
            case 'action-declare-war':
                this._declareWar();
                break;
            case 'action-establish-peace':
                this._establishPeace();
                break;
            case 'action-create-treaty':
                this._createTreaty();
                break;
            case 'action-create-character':
                this._createCharacter();
                break;
            case 'action-import-character':
                this._importCharacter();
                break;
        }
    }

    _handleCommand(command) {
        if (!command || !command.trim()) return;
        const cmd = command.trim().toLowerCase();
        this.hudController.addEventLogEntry(`Command: "${command}"`, 'info');

        if (cmd === 'status') {
            this._showStatus();
        } else if (cmd === 'resources') {
            this._showResources();
        } else if (cmd === 'fleet') {
            this._showFleet();
        } else if (cmd === 'relations') {
            this._showRelations();
        } else if (cmd.startsWith('scan ')) {
            const sector = command.trim().substring(5);
            this.hudController.addEventLogEntry(`Scanning sector: ${sector}`, 'info');
            this._scanSector(sector);
        } else {
            this.hudController.addEventLogEntry(`Unknown command: "${command}". Try: status, resources, fleet, relations, scan <sector>`, 'warning');
        }
    }

    _showStatus() {
        if (this.currentSession) {
            this.hudController.addEventLogEntry(`Session: ${this.currentSession.name || 'Unknown'} (Turn ${this.currentSession.currentTurn || 0})`, 'info');
        }
        if (this.currentFaction) {
            this.hudController.addEventLogEntry(`Faction: ${this.currentFaction.name || 'Unknown'}`, 'info');
        }
    }

    _showResources() {
        if (!this.selectedSessionId || !this.selectedFactionId) {
            this.hudController.addEventLogEntry('Select a session and faction first', 'warning');
            return;
        }
        this._getResources();
    }

    _showFleet() {
        if (!this.selectedFactionId) {
            this.hudController.addEventLogEntry('Select a faction first', 'warning');
            return;
        }
        this._getFleetComposition();
    }

    _showRelations() {
        if (!this.selectedFactionId) {
            this.hudController.addEventLogEntry('Select a faction first', 'warning');
            return;
        }
        this._getRelations();
    }

    // ── Session Management ──

    async _loadSessions() {
        try {
            this.sessions = await this._fetch('/sessions') || [];
            this._renderSessionSelector();
            if (this.selectedSessionId) {
                await this._selectSession(this.selectedSessionId);
            } else if (this.sessions.length > 0) {
                await this._selectSession(this.sessions[0].id);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load sessions', 'danger');
            this._renderSessionSelector();
        }
    }

    async _selectSession(sessionId) {
        try {
            this.selectedSessionId = sessionId;
            localStorage.setItem('st4x-session-id', sessionId);
            this.currentSession = await this._fetch(`/sessions/${sessionId}`);
            if (this.currentSession) {
                // Extract factions from session
                this.factions = this.currentSession.factions || [];
                this._renderSessionInfo();
                this._renderPromptPanel();
                if (this.selectedFactionId) {
                    await this._selectFaction(this.selectedFactionId);
                } else if (this.factions.length > 0) {
                    await this._selectFaction(this.factions[0].id);
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load session details', 'danger');
        }
    }

    async _createSession() {
        try {
            const sessionName = `Session ${this.sessions.length + 1}`;
            const result = await this._fetch('/sessions', {
                method: 'POST',
                body: JSON.stringify({ name: sessionName })
            });
            if (result) {
                this.hudController.addEventLogEntry(`Created session: ${sessionName}`, 'success');
                await this._loadSessions();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create session: ' + err.message, 'danger');
        }
    }

    async _advanceTurn() {
        if (!this.selectedSessionId) {
            this.hudController.addEventLogEntry('Select a session first', 'warning');
            return;
        }
        try {
            const result = await this._fetch(`/sessions/${this.selectedSessionId}/advance-turn`, {
                method: 'POST'
            });
            if (result) {
                const turn = result.currentTurn || (this.currentSession ? (this.currentSession.currentTurn || 0) + 1 : 1);
                this.hudController.addEventLogEntry(`Turn ${turn} processed: economy, colony growth, diplomacy events`, 'success');
                if (result.events && result.events.length > 0) {
                    result.events.forEach(event => {
                        this.hudController.addEventLogEntry(`  - ${event.description || event}`, 'info');
                    });
                }
                await this._selectSession(this.selectedSessionId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to advance turn: ' + err.message, 'danger');
        }
    }

    _renderSessionSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        let html = `<div style="margin-bottom:10px;">
            <label style="font-size:12px;color:var(--hud-accent-primary);">Session</label>
            <select class="hud-selector" id="hudSt4xSessionSelector">
                <option value="">Select Session...</option>
                ${this.sessions.map(s => `
                    <option value="${s.id}" ${s.id === this.selectedSessionId ? 'selected' : ''}>
                        ${s.name || 'Unnamed'} (Turn ${s.currentTurn || 0})
                    </option>
                `).join('')}
            </select>
        </div>`;

        if (this.factions.length > 0) {
            html += `<div style="margin-bottom:10px;">
                <label style="font-size:12px;color:var(--hud-accent-primary);">Faction</label>
                <select class="hud-selector" id="hudSt4xFactionSelector">
                    <option value="">Select Faction...</option>
                    ${this.factions.map(f => `
                        <option value="${f.id}" ${f.id === this.selectedFactionId ? 'selected' : ''}>
                            ${f.name || 'Unknown'}
                        </option>
                    `).join('')}
                </select>
            </div>`;
        }

        if (this.currentSession) {
            html += this._renderSessionInfoContent();
        }

        body.innerHTML = html;

        // Bind session selector change
        const sessionSelector = document.getElementById('hudSt4xSessionSelector');
        if (sessionSelector) {
            sessionSelector.addEventListener('change', (e) => {
                if (e.target.value) {
                    this._selectSession(e.target.value);
                }
            });
        }

        // Bind faction selector change
        const factionSelector = document.getElementById('hudSt4xFactionSelector');
        if (factionSelector) {
            factionSelector.addEventListener('change', (e) => {
                if (e.target.value) {
                    this._selectFaction(e.target.value);
                }
            });
        }
    }

    _renderSessionInfoContent() {
        const s = this.currentSession;
        if (!s) return '';

        let html = `<div class="hud-character-sheet">`;
        html += `<div class="hud-character-name">${s.name || 'Unnamed Session'}</div>`;
        html += `<div class="hud-character-role">Turn ${s.currentTurn || 0} • ${s.factions ? s.factions.length : 0} factions</div>`;

        // Resources
        if (s.resources) {
            const r = s.resources;
            html += `<div style="margin:10px 0;">
                <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Resources</div>
                ${this._renderStatBar('Credits', r.credits || 0, 1000)}
                ${this._renderStatBar('Minerals', r.minerals || 0, 500)}
                ${this._renderStatBar('Energy', r.energy || 0, 500)}
            </div>`;
        }

        // Colonies
        if (s.colonyCount) {
            html += `<div style="font-size:12px;color:#94a3b8;">Colonies: ${s.colonyCount}</div>`;
        }
        if (s.starbaseCount) {
            html += `<div style="font-size:12px;color:#94a3b8;">Starbases: ${s.starbaseCount}</div>`;
        }

        html += `</div>`;
        return html;
    }

    _renderStatBar(label, value, max) {
        const percent = Math.min(100, Math.max(0, (value / max) * 100));
        return `<div class="hud-stat-bar" style="margin-bottom:3px;">
            <div class="hud-stat-bar-label" style="font-size:10px;">${label}</div>
            <div class="hud-stat-bar-track">
                <div class="hud-stat-bar-fill" style="width:${percent}%;background:var(--hud-accent-primary);"></div>
            </div>
            <div class="hud-stat-bar-value" style="font-size:10px;">${value}</div>
        </div>`;
    }

    _renderSessionInfo() {
        this._renderSessionSelector();
    }

    async _selectFaction(factionId) {
        try {
            this.selectedFactionId = factionId;
            localStorage.setItem('st4x-faction-id', factionId);
            this.currentFaction = this.factions.find(f => f.id === factionId);
            if (this.currentFaction) {
                this._renderSessionInfo();
                this.hudController.addEventLogEntry(`Selected faction: ${this.currentFaction.name || 'Unknown'}`, 'info');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to select faction', 'danger');
        }
    }

    // ── Exploration ──

    async _scanSector(sectorId) {
        if (!this.selectedSessionId) {
            this.hudController.addEventLogEntry('Select a session first', 'warning');
            return;
        }
        try {
            const params = new URLSearchParams({ gameSessionId: this.selectedSessionId });
            if (sectorId) params.append('sectorId', sectorId);
            const result = await this._fetch(`/exploration/scan?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Sector scan complete: ${result.systemsFound || 0} systems discovered`, 'success');
                if (result.systems && result.systems.length > 0) {
                    this.systems = result.systems;
                    result.systems.forEach(sys => {
                        this.hudController.addEventLogEntry(`  - ${sys.name || 'Unknown System'} (${sys.type || 'Unknown'})`, 'info');
                    });
                    this._renderSystemSelector();
                }

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        'DISCOVERY',
                        'startrek4x',
                        `Sector scan: ${result.systemsFound || 0} systems discovered`,
                        `${result.systems?.map(s => s.name).join(', ') || 'Unknown systems'}`,
                        { systemsFound: result.systemsFound, sectorId }
                    );
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to scan sector: ' + err.message, 'danger');
        }
    }

    async _getSystems() {
        if (!this.selectedSessionId) return;
        try {
            this.systems = await this._fetch(`/exploration/systems?gameSessionId=${this.selectedSessionId}`) || [];
            this._renderSystemSelector();
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load systems', 'danger');
        }
    }

    async _firstContact() {
        if (!this.selectedSystemId) {
            this.hudController.addEventLogEntry('Select a system first', 'warning');
            return;
        }
        try {
            const result = await this._fetch(`/exploration/first-contact/${this.selectedSystemId}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`First contact initiated! ${result.speciesName || 'Unknown species'} detected`, 'success');
                if (result.scenario) {
                    this.hudController.addEventLogEntry(result.scenario, 'info');
                }
                this._renderPromptPanel(result.scenario || `First contact with ${result.speciesName || 'unknown species'}`);

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        'DISCOVERY',
                        'startrek4x',
                        `First contact: ${result.speciesName || 'Unknown species'}`,
                        result.scenario || 'First contact initiated.',
                        { speciesName: result.speciesName, systemId: this.selectedSystemId }
                    );
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('First contact failed: ' + err.message, 'danger');
        }
    }

    async _scientificMission() {
        if (!this.selectedSystemId) {
            this.hudController.addEventLogEntry('Select a system first', 'warning');
            return;
        }
        try {
            const result = await this._fetch(`/exploration/scientific-mission/${this.selectedSystemId}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Scientific mission launched! ${result.findings || 'Discoveries made'}`, 'success');
                if (result.description) {
                    this._renderPromptPanel(result.description);
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Scientific mission failed: ' + err.message, 'danger');
        }
    }

    _renderSystemSelector() {
        if (this.systems.length === 0) return;

        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        let html = `<div style="margin-bottom:10px;">
            <label style="font-size:12px;color:var(--hud-accent-primary);">Discovered Systems</label>
            <select class="hud-selector" id="hudSt4xSystemSelector">
                <option value="">Select System...</option>
                ${this.systems.map(s => `
                    <option value="${s.id}" ${s.id === this.selectedSystemId ? 'selected' : ''}>
                        ${s.name || 'Unknown'} (${s.type || 'Unknown'})
                    </option>
                `).join('')}
            </select>
        </div>`;

        html += `<div class="hud-narrative-text">
            <p>Select a system to explore, establish contact, or launch missions.</p>
        </div>`;

        promptBody.innerHTML = html;

        const systemSelector = document.getElementById('hudSt4xSystemSelector');
        if (systemSelector) {
            systemSelector.addEventListener('change', (e) => {
                if (e.target.value) {
                    this.selectedSystemId = e.target.value;
                    const sys = this.systems.find(s => s.id === e.target.value);
                    if (sys) {
                        this._renderPromptPanel(`${sys.name || 'Unknown System'} (${sys.type || 'Unknown'})\n\n${sys.description || 'No description available.'}`);
                    }
                }
            });
        }
    }

    // ── Expansion ──

    async _establishColony() {
        if (!this.selectedSessionId || !this.selectedFactionId || !this.selectedSystemId) {
            this.hudController.addEventLogEntry('Select a session, faction, and system first', 'warning');
            return;
        }
        try {
            const colonyName = `Colony ${this.colonies.length + 1}`;
            const params = new URLSearchParams({
                gameSessionId: this.selectedSessionId,
                factionId: this.selectedFactionId,
                systemId: this.selectedSystemId,
                colonyName: colonyName
            });
            const result = await this._fetch(`/expansion/colony?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Colony established: ${colonyName}`, 'success');
                await this._getColonies();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to establish colony: ' + err.message, 'danger');
        }
    }

    async _buildStarbase() {
        if (!this.selectedSessionId || !this.selectedFactionId || !this.selectedSystemId) {
            this.hudController.addEventLogEntry('Select a session, faction, and system first', 'warning');
            return;
        }
        try {
            const starbaseName = `Starbase ${this.colonies.length + 1}`;
            const params = new URLSearchParams({
                gameSessionId: this.selectedSessionId,
                factionId: this.selectedFactionId,
                systemId: this.selectedSystemId,
                starbaseName: starbaseName
            });
            const result = await this._fetch(`/expansion/starbase?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Starbase constructed: ${starbaseName}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to build starbase: ' + err.message, 'danger');
        }
    }

    async _claimSector() {
        if (!this.selectedSessionId || !this.selectedFactionId) {
            this.hudController.addEventLogEntry('Select a session and faction first', 'warning');
            return;
        }
        try {
            const sectorId = '1'; // Default sector
            const params = new URLSearchParams({
                gameSessionId: this.selectedSessionId,
                factionId: this.selectedFactionId,
                sectorId: sectorId
            });
            const result = await this._fetch(`/expansion/claim-sector?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Sector ${sectorId} claimed!`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to claim sector: ' + err.message, 'danger');
        }
    }

    async _getColonies() {
        if (!this.selectedSessionId) return;
        try {
            this.colonies = await this._fetch(`/expansion/colonies?gameSessionId=${this.selectedSessionId}`) || [];
            this.hudController.addEventLogEntry(`Loaded ${this.colonies.length} colonies`, 'info');
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load colonies', 'danger');
        }
    }

    // ── Exploitation ──

    async _getResources() {
        if (!this.selectedSessionId || !this.selectedFactionId) return;
        try {
            const result = await this._fetch(`/exploitation/resources?gameSessionId=${this.selectedSessionId}&factionId=${this.selectedFactionId}`);
            if (result) {
                this.hudController.addEventLogEntry(`Resources: Credits ${result.credits || 0}, Minerals ${result.minerals || 0}, Energy ${result.energy || 0}`, 'info');
                if (result.resources && result.resources.length > 0) {
                    result.resources.forEach(r => {
                        this.hudController.addEventLogEntry(`  - ${r.type || 'Unknown'}: ${r.amount || 0}`, 'info');
                    });
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load resources', 'danger');
        }
    }

    async _establishTrade() {
        if (!this.selectedSessionId || !this.selectedFactionId) {
            this.hudController.addEventLogEntry('Select a session and faction first', 'warning');
            return;
        }
        try {
            const params = new URLSearchParams({
                gameSessionId: this.selectedSessionId,
                factionId: this.selectedFactionId,
                originSystemId: this.selectedSystemId || '1',
                destSystemId: '2',
                cargoType: 'minerals'
            });
            const result = await this._fetch(`/exploitation/trade?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Trade route established! Profit: ${result.profit || 0} credits`, 'success');
                await this._getTradeRoutes();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to establish trade: ' + err.message, 'danger');
        }
    }

    async _getTradeRoutes() {
        if (!this.selectedSessionId) return;
        try {
            this.tradeRoutes = await this._fetch(`/exploitation/trade-routes?gameSessionId=${this.selectedSessionId}`) || [];
            this.hudController.addEventLogEntry(`Loaded ${this.tradeRoutes.length} trade routes`, 'info');
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load trade routes', 'danger');
        }
    }

    async _startResearch() {
        if (!this.selectedFactionId) {
            this.hudController.addEventLogEntry('Select a faction first', 'warning');
            return;
        }
        try {
            const params = new URLSearchParams({
                factionId: this.selectedFactionId,
                techLevel: '1',
                category: 'weapons'
            });
            const result = await this._fetch(`/exploitation/research?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Research started: ${result.technologyName || 'New technology'}`, 'success');
                if (result.turnsToComplete) {
                    this.hudController.addEventLogEntry(`Completion: ${result.turnsToComplete} turns`, 'info');
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to start research: ' + err.message, 'danger');
        }
    }

    // ── Extermination ──

    async _deployFleet() {
        if (!this.selectedFactionId || !this.selectedSystemId) {
            this.hudController.addEventLogEntry('Select a faction and system first', 'warning');
            return;
        }
        try {
            const params = new URLSearchParams({
                factionId: this.selectedFactionId,
                systemId: this.selectedSystemId,
                fleetSize: '3'
            });
            const result = await this._fetch(`/extermination/fleet-deploy?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Fleet deployed to ${this.selectedSystemId}: ${result.fleetSize || 3} ships`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to deploy fleet: ' + err.message, 'danger');
        }
    }

    async _engageFleet() {
        if (!this.selectedFactionId || !this.selectedSystemId) {
            this.hudController.addEventLogEntry('Select a faction and system first', 'warning');
            return;
        }
        try {
            const params = new URLSearchParams({
                attackerFactionId: this.selectedFactionId,
                defenderFactionId: '2', // Default enemy
                systemId: this.selectedSystemId,
                attackerFleetSize: '3',
                defenderFleetSize: '2'
            });
            const result = await this._fetch(`/extermination/fleet-engage?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Fleet engagement! ${result.outcome || 'Battle complete'}`, 'success');
                if (result.attackerLosses !== undefined) {
                    this.hudController.addEventLogEntry(`  Attacker losses: ${result.attackerLosses} ships`, 'warning');
                }
                if (result.defenderLosses !== undefined) {
                    this.hudController.addEventLogEntry(`  Defender losses: ${result.defenderLosses} ships`, 'info');
                }
                this._renderPromptPanel(`Battle Report\n\n${result.outcome || 'The battle is over.'}\n\nAttacker losses: ${result.attackerLosses || 0}\nDefender losses: ${result.defenderLosses || 0}`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to engage fleet: ' + err.message, 'danger');
        }
    }

    async _planetaryInvasion() {
        if (!this.selectedFactionId || !this.selectedSystemId) {
            this.hudController.addEventLogEntry('Select a faction and system first', 'warning');
            return;
        }
        try {
            const params = new URLSearchParams({
                systemId: this.selectedSystemId,
                attackerFactionId: this.selectedFactionId,
                defenderFactionId: '2'
            });
            const result = await this._fetch(`/extermination/planetary-invasion?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Planetary invasion launched! ${result.outcome || 'Invasion in progress'}`, 'success');
                this._renderPromptPanel(`Invasion Report\n\n${result.outcome || 'The invasion has begun.'}`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to launch invasion: ' + err.message, 'danger');
        }
    }

    async _getFleetComposition() {
        if (!this.selectedFactionId) return;
        try {
            this.fleetComposition = await this._fetch(`/extermination/fleet-composition/${this.selectedFactionId}`) || [];
            if (this.fleetComposition.length > 0) {
                this.hudController.addEventLogEntry(`Fleet composition:`, 'info');
                this.fleetComposition.forEach(ship => {
                    this.hudController.addEventLogEntry(`  - ${ship.shipType || 'Unknown'}: ${ship.count || 0}`, 'info');
                });
            } else {
                this.hudController.addEventLogEntry('No fleet deployed', 'warning');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load fleet composition', 'danger');
        }
    }

    // ── Diplomacy ──

    async _getRelations() {
        if (!this.selectedFactionId) return;
        try {
            this.relations = await this._fetch(`/diplomacy/relations/${this.selectedFactionId}`) || [];
            if (this.relations.length > 0) {
                this.hudController.addEventLogEntry(`Diplomatic relations:`, 'info');
                this.relations.forEach(rel => {
                    this.hudController.addEventLogEntry(`  - ${rel.factionName || 'Unknown'}: ${rel.relation || 'Neutral'} (${rel.score || 0})`, 'info');
                });
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load relations', 'danger');
        }
    }

    async _declareWar() {
        if (!this.selectedFactionId) {
            this.hudController.addEventLogEntry('Select a faction first', 'warning');
            return;
        }
        try {
            const params = new URLSearchParams({
                attackerId: this.selectedFactionId,
                defenderId: '2'
            });
            const result = await this._fetch(`/diplomacy/declare-war?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`War declared!`, 'danger');
                this._renderPromptPanel(`WAR DECLARED\n\nHostilities have begun. All diplomatic channels are closed.`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to declare war: ' + err.message, 'danger');
        }
    }

    async _establishPeace() {
        if (!this.selectedFactionId) {
            this.hudController.addEventLogEntry('Select a faction first', 'warning');
            return;
        }
        try {
            const params = new URLSearchParams({
                faction1Id: this.selectedFactionId,
                faction2Id: '2'
            });
            const result = await this._fetch(`/diplomacy/establish-peace?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Peace established!`, 'success');
                this._renderPromptPanel(`PEACE TREATY\n\nA peace treaty has been signed. Relations are restored.`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to establish peace: ' + err.message, 'danger');
        }
    }

    async _createTreaty() {
        if (!this.selectedFactionId) {
            this.hudController.addEventLogEntry('Select a faction first', 'warning');
            return;
        }
        try {
            const params = new URLSearchParams({
                faction1Id: this.selectedFactionId,
                faction2Id: '2',
                treatyType: 'trade'
            });
            const result = await this._fetch(`/diplomacy/treaty?${params}`, { method: 'POST' });
            if (result) {
                this.hudController.addEventLogEntry(`Treaty created: ${result.treatyType || 'trade'}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create treaty: ' + err.message, 'danger');
        }
    }

    // ── Character Management ──

    async _loadEmpires() {
        try {
            this.empires = await this._fetch('/rpg/empires') || [];
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load empires', 'danger');
        }
    }

    async _loadSpecies() {
        try {
            this.species = await this._fetch('/rpg/species') || [];
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load species', 'danger');
        }
    }

    async _loadDivisions() {
        try {
            this.divisions = await this._fetch('/rpg/divisions') || [];
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load divisions', 'danger');
        }
    }

    async _createCharacter() {
        try {
            const name = `Character ${Date.now()}`;
            const empire = this.empires.length > 0 ? this.empires[0].name : 'Federation';
            const species = this.species.length > 0 ? this.species[0].name : 'Human';
            const division = this.divisions.length > 0 ? this.divisions[0].name : 'Command';
            const params = new URLSearchParams({
                name: name,
                empire: empire,
                species: species,
                division: division
            });
            const result = await this._fetch(`/rpg/characters/create?${params}`, { method: 'POST' });
            if (result) {
                this.currentCharacter = result;
                this.hudController.addEventLogEntry(`Character created: ${name} (${species}, ${division})`, 'success');
                this._renderCharacterSheet();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create character: ' + err.message, 'danger');
        }
    }

    async _importCharacter() {
        try {
            // Import from Captain's Log — use a default character ID
            const characterId = localStorage.getItem('cl-character-id') || '1';
            const params = new URLSearchParams({ characterId: characterId });
            const result = await this._fetch(`/rpg/characters/import?${params}`, { method: 'POST' });
            if (result) {
                this.currentCharacter = result;
                this.hudController.addEventLogEntry(`Character imported: ${result.name || 'Unknown'}`, 'success');
                this._renderCharacterSheet();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to import character: ' + err.message, 'danger');
        }
    }

    _renderCharacterSheet() {
        if (!this.currentCharacter) return;
        const c = this.currentCharacter;

        let html = `<div class="hud-character-sheet">`;
        html += `<div class="hud-character-name">${c.name || 'Unknown'}</div>`;
        html += `<div class="hud-character-role">${c.species || 'Human'} • ${c.division || 'Command'}</div>`;

        if (c.empire) {
            html += `<div style="font-size:12px;color:#94a3b8;margin-bottom:8px;">Empire: ${c.empire}</div>`;
        }

        // Attributes if available
        if (c.attributes) {
            const attrs = c.attributes;
            html += `<div style="margin:10px 0;">
                <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Attributes</div>`;
            for (const [key, value] of Object.entries(attrs)) {
                html += this._renderStatBar(key, value, 10);
            }
            html += `</div>`;
        }

        html += `</div>`;

        const body = document.getElementById('hudInfoPanelBody');
        if (body) {
            body.innerHTML = html;
        }
    }

    // ── Prompt Panel Rendering ──

    _renderPromptPanel(narrative) {
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        if (narrative) {
            promptBody.innerHTML = `<div class="hud-narrative-text">
                <p>${narrative.replace(/\n/g, '</p><p>')}</p>
            </div>`;
        } else if (this.currentSession) {
            promptBody.innerHTML = `<div class="hud-narrative-text">
                <p>Session: ${this.currentSession.name || 'Unnamed'}</p>
                <p>Turn ${this.currentSession.currentTurn || 0}</p>
                <p>${this.currentSession.description || 'Explore, expand, exploit, and exterminate to build your empire.'}</p>
            </div>`;
        } else {
            promptBody.innerHTML = `<div class="hud-narrative-text">
                <p>Select a session to begin your empire strategy...</p>
            </div>`;
        }
    }

    // ── AI Narrative ──

    _generateNarrative(context) {
        if (!window.AiNarrative) return;
        const sessionId = 'st4x-narrative-' + (this.selectedSessionId || 'default');
        const systemPrompt = window.AiNarrative.getSystemPrompt('star-trek-4x');
        const factionName = this.currentFaction ? this.currentFaction.name : 'Federation';
        const turn = this.currentSession ? this.currentSession.currentTurn || 0 : 0;
        const message = `Faction: ${factionName}. Turn: ${turn}. ${context}`;

        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        const loadingEl = document.createElement('div');
        loadingEl.className = 'hud-narrative-loading';
        loadingEl.textContent = '✨ Generating narrative...';
        loadingEl.style.cssText = 'color: #1e40af; font-style: italic; margin: 10px 0;';
        promptPanel.appendChild(loadingEl);

        window.AiNarrative.streamToPanel(
            sessionId, message, systemPrompt,
            null,
            (fullText) => {
                if (loadingEl.parentNode) loadingEl.remove();
                if (fullText) {
                    const narrativeEl = document.createElement('div');
                    narrativeEl.className = 'hud-narrative-text';
                    narrativeEl.style.cssText = 'color: #93c5fd; margin: 10px 0; padding: 10px; border-left: 3px solid #1e40af; background: rgba(30, 64, 175, 0.1);';
                    narrativeEl.textContent = fullText;
                    promptPanel.appendChild(narrativeEl);
                    promptPanel.scrollTop = promptPanel.scrollHeight;
                }
            },
            (err) => {
                if (loadingEl.parentNode) loadingEl.remove();
                this.hudController.addEventLogEntry('AI narrative failed: ' + err, 'warning');
            }
        );
    }

    async generateNarrative() {
        this._generateNarrative('Describe the current state of the empire and what happens next.');
    }
}

// Expose globally
window.StarTrek4xAdapter = StarTrek4xAdapter;

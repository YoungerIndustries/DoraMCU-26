/* Star Wars Adapter — Connects HUD panels to the unified Game Engine API */

class StarWarsAdapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.engineBase = apiBase + '/STAR_WARS';
        this.factions = [];
        this.units = [];
        this.scenarios = [];
        this.lore = [];
        this.forcePowers = [];
        this.selectedFactionId = null;
        this.selectedUnitId = null;
        this.selectedScenarioId = null;
        this.currentUnit = null;
        this.currentScenario = null;
        this.currentFaction = null;
        this.combatants = [];
        this.currentView = 'faction'; // faction, unit, scenario, lore, force
        this.engineState = null;

        // AI Inference integration
        this.aiAdapter = null;
        this.aiEnabled = true;
    }

    /**
     * Initialize AI inference for this adapter.
     */
    async initAiInference(neuronService) {
        if (typeof window !== 'undefined' && window.AiInferenceAdapter) {
            this.aiAdapter = new window.AiInferenceAdapter({
                gameId: 'star-wars',
                neuronService: neuronService,
                enableAi: this.aiEnabled
            });
            await this.aiAdapter.initialize();
            console.log('[StarWars] AI inference initialized');
        }
    }

    /**
     * Get AI-assisted combat recommendation.
     * @returns {Promise<Object>} Combat recommendation
     */
    async getAiCombatRecommendation() {
        if (!this.aiAdapter) return null;

        const gameState = this._encodeGameState();
        const actions = ['attack', 'defend', 'flank', 'retreat', 'use_force_power',
                         'call_reinforcements', 'surrender', 'negotiate'];
        return this.aiAdapter.requestAiDecision(gameState, actions);
    }

    /**
     * Get AI-assisted Force power recommendation.
     * @returns {Promise<Object>} Force power recommendation
     */
    async getAiForcePowerRecommendation() {
        if (!this.aiAdapter) return null;

        const gameState = {
            ...this._encodeGameState(),
            forcePowerCount: this.forcePowers.length,
            availablePowers: Math.min(this.forcePowers.length, 5)
        };

        const actions = ['mind_trick', 'force_push', 'force_lightning',
                         'telekinesis', 'healing', 'precognition', 'cloak'];
        return this.aiAdapter.requestAiDecision(gameState, actions);
    }

    /**
     * Submit gameplay experience for AI learning.
     */
    submitExperience(gameState, action, reward) {
        if (this.aiAdapter) {
            this.aiAdapter.submitExperience(gameState, action, reward);
        }
    }

    /**
     * Encode current game state to 64-dim vector.
     * @private
     */
    _encodeGameState() {
        const vector = new Array(64).fill(0);
        let idx = 0;

        // Faction strength (indices 0-3)
        if (this.currentFaction) {
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentFaction.power || 0) / 1000));
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentFaction.units || 0) / 50));
        }

        // Unit stats (indices 4-7)
        if (this.currentUnit) {
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentUnit.health || 0) / 100));
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentUnit.attack || 0) / 100));
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentUnit.defense || 0) / 100));
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentUnit.speed || 0) / 100));
        }

        // Combatants (indices 8-11)
        vector[idx++] = Math.max(-1, Math.min(1, (this.combatants.length || 0) / 20));
        const friendlyCount = this.combatants.filter(c => c.friendly).length;
        vector[idx++] = Math.max(-1, Math.min(1, friendlyCount / 10));
        const enemyCount = this.combatants.filter(c => !c.friendly).length;
        vector[idx++] = Math.max(-1, Math.min(1, enemyCount / 10));

        // Scenario progress (indices 12-15)
        if (this.currentScenario) {
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentScenario.turn || 0) / 50));
            vector[idx++] = Math.max(-1, Math.min(1, (this.currentScenario.objectivesCompleted || 0) / 10));
        }

        // Force powers (indices 16-19)
        vector[idx++] = Math.max(-1, Math.min(1, (this.forcePowers.length || 0) / 10));

        // Current view encoding (indices 20-23)
        const views = ['faction', 'unit', 'scenario', 'lore', 'force'];
        const viewIdx = views.indexOf(this.currentView);
        if (viewIdx >= 0) {
            vector[idx++] = viewIdx / (views.length - 1);
        } else {
            idx++;
        }

        return vector;
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Restore session state from localStorage
        const savedFactionId = localStorage.getItem('sw-faction-id');
        if (savedFactionId) {
            this.selectedFactionId = savedFactionId;
        }
        const savedUnitId = localStorage.getItem('sw-unit-id');
        if (savedUnitId) {
            this.selectedUnitId = savedUnitId;
        }
        const savedScenarioId = localStorage.getItem('sw-scenario-id');
        if (savedScenarioId) {
            this.selectedScenarioId = savedScenarioId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'star-wars') {
                this._onTemplateLoaded();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'star-wars') {
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

        console.log('[StarWarsAdapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._loadEngineState();
        this._loadAllData();
    }

    destroy() {
        super.destroy();
    }

    // ── Action Handler ──

    _handleAction(action) {
        switch (action) {
            case 'action-browse-factions':
                this._currentView = 'faction';
                this._renderFactions();
                break;
            case 'action-browse-units':
                this._currentView = 'unit';
                this._renderUnits();
                break;
            case 'action-browse-scenarios':
                this._currentView = 'scenario';
                this._renderScenarios();
                break;
            case 'action-browse-lore':
                this._currentView = 'lore';
                this._renderLore();
                break;
            case 'action-browse-force-powers':
                this._currentView = 'force';
                this._renderForcePowers();
                break;
            case 'action-task-attempt':
                this._taskAttempt();
                break;
            case 'action-attack':
                this._attack();
                break;
            case 'action-defend':
                this._defend();
                break;
            case 'action-force-power':
                this._useForcePower();
                break;
            case 'action-roll-d6':
                this._rollDice(6, 1);
                break;
            case 'action-roll-d20':
                this._rollDice(20, 1);
                break;
            case 'action-roll-2d6':
                this._rollDice(6, 2);
                break;
            case 'action-start-scenario':
                this._startScenario();
                break;
            case 'action-next-turn':
                this._nextTurn();
                break;
            case 'action-generate-encounter':
                this._generateEncounter();
                break;
            case 'action-generate-narrative':
                this._generateNarrative();
                break;
        }
    }

    // ── Command Handler ──

    _handleCommand(cmd) {
        const parts = cmd.trim().split(' ');
        const command = parts[0].toLowerCase();

        switch (command) {
            case 'status':
                this._renderInfoPanel();
                this._addEventLogEntry('📊 Status refreshed');
                break;
            case 'factions':
                this._renderFactions();
                this._addEventLogEntry('🏴 Factions list refreshed');
                break;
            case 'units':
                this._renderUnits();
                this._addEventLogEntry('⚔️ Units list refreshed');
                break;
            case 'scenarios':
                this._renderScenarios();
                this._addEventLogEntry('🗺️ Scenarios list refreshed');
                break;
            case 'lore':
                this._renderLore();
                this._addEventLogEntry('📖 Lore refreshed');
                break;
            case 'combat':
                this._attack();
                break;
            case 'roll':
                const sides = parseInt(parts[1]) || 6;
                const count = parseInt(parts[2]) || 1;
                this._rollDice(sides, count);
                break;
            default:
                this._addEventLogEntry(`❓ Unknown command: ${command}. Available: status, factions, units, scenarios, lore, combat, roll <sides> <count>`);
        }
    }

    // ── Engine State ──

    async _loadEngineState() {
        try {
            const data = await this._fetch('/STAR_WARS');
            this.engineState = data;
            this._addEventLogEntry(`🎮 Star Wars engine: ${data.name || 'loaded'} (Turn ${data.currentTurn || 0})`);
        } catch (error) {
            console.error('[StarWarsAdapter] Error loading engine state:', error);
        }
    }

    // ── Data Loading via queryRules ──

    async _loadAllData() {
        try {
            const data = await this._engineAction('queryRules', { category: '' });
            if (data && data.success && data.rules) {
                this.factions = data.rules.filter(r => r.category === 'Faction') || [];
                this.units = data.rules.filter(r => ['Ground Unit', 'Starship', 'Ground Vehicle', 'Character'].includes(r.category)) || [];
                this.forcePowers = data.rules.filter(r => r.category === 'Force Power' || r.category === 'Leader Ability') || [];
                this.lore = data.rules.filter(r => ['planet', 'era', 'species', 'event'].includes(r.category)) || [];
                this.scenarios = data.rules.filter(r => r.category === 'Scenario') || [];

                this._addEventLogEntry(`📊 Loaded ${this.factions.length} factions, ${this.units.length} units, ${this.forcePowers.length} abilities, ${this.lore.length} lore entries`);
            }

            // Also try to load scenarios from encounter service
            await this._loadScenariosFromEncounter();

            // Render initial view
            this._renderFactions();
        } catch (error) {
            console.error('[StarWarsAdapter] Error loading data:', error);
            this._renderPromptPanel('Failed to load Star Wars data. The game engine may not be running.');
        }
    }

    async _loadScenariosFromEncounter() {
        try {
            const data = await this._engineAction('generateEncounter', { areaId: 'galaxy', level: 1 });
            if (data && data.success && data.encounter) {
                // Add generated encounter to scenarios if not already present
                const existing = this.scenarios.find(s => s.title === data.encounter.name);
                if (!existing && data.encounter.name) {
                    this.scenarios.push({
                        id: this.scenarios.length + 1,
                        title: data.encounter.name,
                        category: 'Scenario',
                        text: data.encounter.description || ''
                    });
                }
            }
        } catch (error) {
            // Scenarios may not be available via encounter service
        }
    }

    // ── Engine Action Helper ──

    async _engineAction(actionType, params = {}) {
        const body = { action: actionType, ...params };
        return await this._fetch(`/STAR_WARS/action`, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    }

    // ── Faction Management ──

    _renderFactions() {
        const infoPanel = document.getElementById('hudInfoPanelBody');
        if (!infoPanel) return;

        let html = `
            <div class="hud-section-title">⭐ Star Wars — Galaxy at War</div>
            <div class="hud-selector-group">
                <label for="hudFactionSelect">Select Faction:</label>
                <select id="hudFactionSelect" class="hud-selector">
                    <option value="">-- Choose Faction --</option>
        `;

        for (const faction of this.factions) {
            const selected = faction.title === this.selectedFactionId ? 'selected' : '';
            html += `<option value="${faction.title}" ${selected}>${faction.title}</option>`;
        }

        html += `
                </select>
            </div>
        `;

        if (this.currentFaction) {
            html += `
                <div class="hud-stat-group">
                    <div class="hud-stat-label">Faction: ${this.currentFaction.title}</div>
                    <div class="hud-stat-label">${this.currentFaction.text || ''}</div>
                </div>
            `;
        }

        infoPanel.innerHTML = html;

        const select = document.getElementById('hudFactionSelect');
        if (select) {
            select.addEventListener('change', (e) => {
                const factionTitle = e.target.value;
                if (factionTitle) {
                    this.selectedFactionId = factionTitle;
                    this.currentFaction = this.factions.find(f => f.title === factionTitle);
                    localStorage.setItem('sw-faction-id', factionTitle);
                    this._renderPromptPanel(`🏴 <strong>${this.currentFaction.title}</strong>\n\n${this.currentFaction.text || 'No description available.'}`);
                    this._renderUnitsForFaction(factionTitle);
                }
            });
        }
    }

    _renderUnitsForFaction(factionTitle) {
        const factionUnits = this.units.filter(u => {
            // Units don't have faction field in rules entries; filter by name or description
            return true;
        });
        this._renderUnitSelector(factionUnits);
    }

    // ── Unit Management ──

    _renderUnits() {
        this._renderUnitSelector(this.units);
    }

    _renderUnitSelector(units) {
        const infoPanel = document.getElementById('hudInfoPanelBody');
        if (!infoPanel) return;

        let html = `
            <div class="hud-section-title">⭐ Star Wars — Galaxy at War</div>
            <div class="hud-selector-group">
                <label for="hudUnitSelect">Select Unit:</label>
                <select id="hudUnitSelect" class="hud-selector">
                    <option value="">-- Choose Unit --</option>
        `;

        for (const unit of units) {
            const selected = unit.title === this.selectedUnitId ? 'selected' : '';
            html += `<option value="${unit.title}" ${selected}>${unit.title} (${unit.category || 'Unit'})</option>`;
        }

        html += `
                </select>
            </div>
        `;

        if (this.currentUnit) {
            html += `
                <div class="hud-stat-group">
                    <div class="hud-stat-label">Unit: ${this.currentUnit.title}</div>
                    <div class="hud-stat-label">${this.currentUnit.text || ''}</div>
                </div>
            `;
        }

        infoPanel.innerHTML = html;

        const select = document.getElementById('hudUnitSelect');
        if (select) {
            select.addEventListener('change', (e) => {
                const unitTitle = e.target.value;
                if (unitTitle) {
                    this.selectedUnitId = unitTitle;
                    this.currentUnit = units.find(u => u.title === unitTitle);
                    localStorage.setItem('sw-unit-id', unitTitle);
                    this._renderPromptPanel(`⚔️ <strong>${this.currentUnit.title}</strong> (${this.currentUnit.category || 'Unit'})\n\n${this.currentUnit.text || 'No description available.'}`);
                }
            });
        }
    }

    // ── Scenario Management ──

    _renderScenarios() {
        const infoPanel = document.getElementById('hudInfoPanelBody');
        if (!infoPanel) return;

        let html = `
            <div class="hud-section-title">⭐ Star Wars — Galaxy at War</div>
            <div class="hud-selector-group">
                <label for="hudScenarioSelect">Select Scenario:</label>
                <select id="hudScenarioSelect" class="hud-selector">
                    <option value="">-- Choose Scenario --</option>
        `;

        for (const scenario of this.scenarios) {
            const selected = scenario.title === this.selectedScenarioId ? 'selected' : '';
            html += `<option value="${scenario.title}" ${selected}>${scenario.title}</option>`;
        }

        html += `
                </select>
            </div>
        `;

        if (this.currentScenario) {
            html += `
                <div class="hud-stat-group">
                    <div class="hud-stat-label">Scenario: ${this.currentScenario.title}</div>
                    <div class="hud-stat-label">${this.currentScenario.text || ''}</div>
                </div>
            `;
        }

        infoPanel.innerHTML = html;

        const select = document.getElementById('hudScenarioSelect');
        if (select) {
            select.addEventListener('change', (e) => {
                const scenarioTitle = e.target.value;
                if (scenarioTitle) {
                    this.selectedScenarioId = scenarioTitle;
                    this.currentScenario = this.scenarios.find(s => s.title === scenarioTitle);
                    localStorage.setItem('sw-scenario-id', scenarioTitle);
                    this._renderPromptPanel(`🗺️ <strong>${this.currentScenario.title}</strong>\n\n${this.currentScenario.text || 'No description available.'}`);
                }
            });
        }
    }

    // ── Lore Browsing ──

    _renderLore() {
        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        let html = '<div class="hud-section-title">📖 Star Wars Lore</div>';

        // Group by category
        const categories = {};
        for (const lore of this.lore) {
            if (!categories[lore.category]) categories[lore.category] = [];
            categories[lore.category].push(lore);
        }

        for (const [category, entries] of Object.entries(categories)) {
            html += `<div class="hud-subsection-title">${category.charAt(0).toUpperCase() + category.slice(1)} (${entries.length})</div>`;
            for (const entry of entries.slice(0, 10)) {
                html += `<div class="hud-lore-entry"><strong>${entry.title}</strong>: ${entry.text || ''}</div>`;
            }
            if (entries.length > 10) {
                html += `<div class="hud-stat-label">... and ${entries.length - 10} more</div>`;
            }
        }

        promptPanel.innerHTML = html;
    }

    // ── Force Powers ──

    _renderForcePowers() {
        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        let html = '<div class="hud-section-title">✨ Force Powers</div>';

        const lightSide = this.forcePowers.filter(p => p.text && p.text.includes('Light'));
        const darkSide = this.forcePowers.filter(p => p.text && p.text.includes('Dark'));
        const allPowers = this.forcePowers;

        html += '<div class="hud-subsection-title">Light Side Powers</div>';
        for (const power of allPowers.slice(0, Math.ceil(allPowers.length / 2))) {
            html += `<div class="hud-lore-entry"><strong>${power.title}</strong>: ${power.text || ''}</div>`;
        }

        html += '<div class="hud-subsection-title">Dark Side Powers</div>';
        for (const power of allPowers.slice(Math.ceil(allPowers.length / 2))) {
            html += `<div class="hud-lore-entry"><strong>${power.title}</strong>: ${power.text || ''}</div>`;
        }

        promptPanel.innerHTML = html;
    }

    // ── Combat Actions ──

    async _taskAttempt() {
        const difficulty = 12;
        const skillLevel = this.currentUnit ? 3 : 0;

        try {
            const data = await this._engineAction('taskAttempt', { difficulty, skillLevel });
            if (data && data.success !== undefined) {
                const result = data.success ? '✅ SUCCESS' : '❌ FAILURE';
                this._addEventLogEntry(`🎯 Task Attempt: ${result} — ${data.description || 'Roll complete'}`);
                this._addDiceResult({
                    dice: [data.roll || 0],
                    target: data.target || difficulty,
                    success: data.success,
                    description: data.description || ''
                });

                // AI narrative
                if (window.AiNarrative) {
                    const context = this.currentScenario ? `During the ${this.currentScenario.title} scenario, ` : '';
                    const narrative = data.success ? 'a successful action' : 'a failed attempt';
                    window.AiNarrative.streamToPanel(
                        'sw-narrative-session',
                        `${context}The player made ${narrative}. ${data.description || ''}`,
                        'You are a Star Wars battle narrator. Describe the scene in the style of a Star Wars movie, with dramatic tension and vivid imagery.'
                    );
                }

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        data.success ? 'COMBAT_WIN' : 'COMBAT_LOSS',
                        'starwars',
                        `Task attempt: ${data.success ? 'success' : 'failure'}`,
                        data.description || '',
                        { difficulty, skillLevel, roll: data.roll }
                    );
                }
            }
        } catch (error) {
            this._addEventLogEntry(`❌ Task attempt failed: ${error.message}`);
        }
    }

    async _attack() {
        const attacker = this.currentUnit ? {
            attack: 5,
            defense: 3,
            damage: 2,
            name: this.currentUnit.title
        } : { attack: 5, defense: 3, damage: 2, name: 'Player' };

        const defender = {
            attack: 3,
            defense: 5,
            damage: 1,
            name: 'Enemy'
        };

        try {
            const data = await this._engineAction('combatRound', { attacker, defender });
            if (data && data.hit !== undefined) {
                const result = data.hit ? '💥 HIT' : '💨 MISS';
                this._addEventLogEntry(`⚔️ Attack: ${result} — ${data.description || 'Combat round complete'}`);
                if (data.damage > 0) {
                    this._addEventLogEntry(`💥 Damage: ${data.damage}`);
                }

                // AI narrative
                if (window.AiNarrative) {
                    window.AiNarrative.streamToPanel(
                        'sw-narrative-session',
                        `Combat: ${data.hit ? 'The attack connects!' : 'The attack misses!'} ${data.description || ''}`,
                        'You are a Star Wars battle narrator. Describe the combat in the style of a Star Wars movie.'
                    );
                }

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        data.hit ? 'COMBAT_WIN' : 'COMBAT_LOSS',
                        'starwars',
                        `Attack: ${data.hit ? 'HIT' : 'MISS'}`,
                        `${data.damage || 0} damage. ${data.description || ''}`,
                        { attacker: attacker.name, defender: defender.name, hit: data.hit, damage: data.damage }
                    );
                }
            }
        } catch (error) {
            this._addEventLogEntry(`❌ Attack failed: ${error.message}`);
        }
    }

    async _defend() {
        this._addEventLogEntry('🛡️ Defensive stance adopted. Next attack will have reduced damage.');
    }

    async _useForcePower() {
        const difficulty = 14;
        const skillLevel = 5; // Force user bonus

        try {
            const data = await this._engineAction('taskAttempt', { difficulty, skillLevel });
            if (data && data.success !== undefined) {
                const result = data.success ? '✨ Force Power Succeeds' : '💫 Force Power Fails';
                this._addEventLogEntry(`🌀 Force Power: ${result} — ${data.description || 'Force attempt complete'}`);
                this._addDiceResult({
                    dice: [data.roll || 0],
                    target: data.target || difficulty,
                    success: data.success,
                    description: data.description || ''
                });
            }
        } catch (error) {
            this._addEventLogEntry(`❌ Force power failed: ${error.message}`);
        }
    }

    // ── Dice Rolling ──

    async _rollDice(sides, count) {
        try {
            const data = await this._engineAction('rollDice', { sides, count });
            if (data && data.result !== undefined) {
                this._addEventLogEntry(`🎲 ${count}d${sides} = ${data.result}`);
                this._addDiceResult({
                    dice: [data.result],
                    description: data.description || `${count}d${sides} = ${data.result}`
                });
            }
        } catch (error) {
            this._addEventLogEntry(`❌ Dice roll failed: ${error.message}`);
        }
    }

    // ── Scenario Management ──

    async _startScenario() {
        if (!this.currentScenario) {
            this._addEventLogEntry('⚠️ No scenario selected. Browse scenarios first.');
            return;
        }

        try {
            // Start the engine
            await this._fetch('/STAR_WARS/start', { method: 'POST' });
            this._addEventLogEntry(`🎬 Scenario started: ${this.currentScenario.title}`);
            this._renderPromptPanel(`🎬 <strong>${this.currentScenario.title}</strong>\n\n${this.currentScenario.text || 'The battle begins...'}`);

            // AI narrative
            if (window.AiNarrative) {
                window.AiNarrative.streamToPanel(
                    'sw-narrative-session',
                    `The scenario "${this.currentScenario.title}" begins. ${this.currentScenario.text || 'The galaxy holds its breath...'}`,
                    'You are a Star Wars battle narrator. Describe the opening of this battle in cinematic style.'
                );
            }
        } catch (error) {
            this._addEventLogEntry(`❌ Failed to start scenario: ${error.message}`);
        }
    }

    async _nextTurn() {
        try {
            await this._fetch('/STAR_WARS/nextTurn', { method: 'POST' });
            this._addEventLogEntry('⏭️ Turn advanced');
        } catch (error) {
            this._addEventLogEntry(`❌ Failed to advance turn: ${error.message}`);
        }
    }

    async _generateEncounter() {
        try {
            const data = await this._engineAction('generateEncounter', { areaId: 'galaxy', level: 1 });
            if (data && data.success && data.encounter) {
                this._addEventLogEntry(`🎲 Random encounter: ${data.encounter.name || 'Unknown'}`);
                this._renderPromptPanel(`🎲 <strong>Random Encounter: ${data.encounter.name || 'Unknown'}</strong>\n\n${data.encounter.description || 'A mysterious encounter in the galaxy...'}`);
            }
        } catch (error) {
            this._addEventLogEntry(`❌ Failed to generate encounter: ${error.message}`);
        }
    }

    // ── AI Narrative ──

    async _generateNarrative() {
        if (!window.AiNarrative) {
            this._addEventLogEntry('⚠️ AI narrative service not available');
            return;
        }

        const context = [];
        if (this.currentFaction) context.push(`Faction: ${this.currentFaction.title}`);
        if (this.currentUnit) context.push(`Unit: ${this.currentUnit.title}`);
        if (this.currentScenario) context.push(`Scenario: ${this.currentScenario.title}`);

        window.AiNarrative.streamToPanel(
            'sw-narrative-session',
            `Generate a Star Wars narrative scene. ${context.join('. ') || 'The galaxy is vast and full of adventure.'}`,
            'You are a Star Wars narrator. Generate vivid, cinematic scenes in the style of the Star Wars saga, with dramatic tension, heroism, and the Force.'
        );
    }

    // ── Panel Rendering ──

    _renderInfoPanel() {
        if (this._currentView === 'faction') {
            this._renderFactions();
        } else if (this._currentView === 'unit') {
            this._renderUnits();
        } else if (this._currentView === 'scenario') {
            this._renderScenarios();
        }
    }

    _renderPromptPanel(text) {
        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;
        promptPanel.innerHTML = `<div class="hud-narrative-text">${text.replace(/\n/g, '<br>')}</div>`;
    }

    _addEventLogEntry(text) {
        const eventLog = document.getElementById('hudEventLog');
        if (eventLog) {
            const entry = document.createElement('div');
            entry.className = 'hud-event-log-entry';
            entry.textContent = text;
            eventLog.appendChild(entry);
            eventLog.scrollTop = eventLog.scrollHeight;
        }
    }

    _addDiceResult(result) {
        if (this.hudController && this.hudController.addDiceResult) {
            this.hudController.addDiceResult(result);
        }
    }
}

// Expose globally
window.StarWarsAdapter = StarWarsAdapter;

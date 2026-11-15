/* Unified World Adapter — Aggregates all game module adapters into one persistent world */

class UnifiedWorldAdapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.modules = {};
        this.enabledModules = new Set(['earth2026']);
        this.adapters = {};
        this.currentView = 'unified';
        this.narrativeSessionId = 'unified-narrative';
        // Unified world services
        this.unifiedApiBase = '';
        this.recentEvents = [];
        this.worldNodes = [];
        this.worldEdges = [];
        this.quests = [];
        this.activeQuests = [];
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Load unified world template
        this._loadUnifiedTemplate();

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action) {
                this._handleAction(action, btn.dataset.module);
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

        // Listen for narrative button
        const narrativeBtn = document.getElementById('hudGenerateNarrative');
        if (narrativeBtn) {
            narrativeBtn.addEventListener('click', () => {
                this._generateUnifiedNarrative();
            });
        }

        console.log('[UnifiedWorldAdapter] Initialized');
    }

    destroy() {
        super.destroy();
        for (const adapter of Object.values(this.adapters)) {
            if (adapter && adapter.destroy) {
                adapter.destroy();
            }
        }
    }

    // ── Unified Template Loading ──

    async _loadUnifiedTemplate() {
        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : '';
            const response = await fetch(`${serverUrl}/dora-electron/renderer/hud/templates/unified-world.json`);
            const template = await response.json();

            // Register with HUD controller
            if (this.hudController && this.hudController.registerTemplate) {
                this.hudController.registerTemplate(template);
            }

            // Store module definitions
            this.modules = {};
            if (template.modules) {
                template.modules.forEach(mod => {
                    this.modules[mod.id] = mod;
                });
            }

            // Load enabled modules from localStorage
            const saved = localStorage.getItem('unified-enabled-modules');
            if (saved) {
                try {
                    const enabled = JSON.parse(saved);
                    this.enabledModules = new Set(enabled);
                    // Always include earth2026
                    this.enabledModules.add('earth2026');
                } catch (e) {
                    console.warn('[UnifiedWorldAdapter] Failed to parse saved modules:', e);
                }
            }

            // Render module toggles
            this._renderModuleToggles();

            // Initialize enabled module adapters
            await this._initEnabledAdapters();

            // Render unified actions
            this._renderUnifiedActions();

            // Render Earth 2026 character sheet (base)
            this._renderInfoPanel();

            // Set unified world as current template
            if (this.hudController) {
                this.hudController.currentTemplateId = 'unified-world';
            }

            // Initialize 3D scene
            this._init3DScene();

            this._addEventLogEntry('🌐 Unified World loaded with ' + this.enabledModules.size + ' active modules');
        } catch (error) {
            console.error('[UnifiedWorldAdapter] Error loading unified template:', error);
            this._addEventLogEntry('❌ Failed to load unified world template');
        }
    }

    // ── Module Toggle Rendering ──

    _renderModuleToggles() {
        const container = document.getElementById('hudModuleToggles');
        if (!container) return;

        let html = '';
        for (const [id, mod] of Object.entries(this.modules)) {
            const isActive = this.enabledModules.has(id);
            const alwaysActive = mod.alwaysActive ? ' always-active' : '';
            const activeClass = isActive ? ' active' : '';
            html += `
                <button class="hud-module-toggle-btn${activeClass}${alwaysActive}" 
                        data-module="${id}" 
                        title="${mod.name}${mod.alwaysAlways ? ' (always active)' : ''}">
                    <span class="hud-module-icon">${mod.icon}</span>
                    <span class="hud-module-name">${mod.name}</span>
                </button>
            `;
        }
        container.innerHTML = html;

        // Bind toggle clicks
        container.querySelectorAll('.hud-module-toggle-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                const moduleId = btn.dataset.module;
                if (this.modules[moduleId] && this.modules[moduleId].alwaysActive) return;
                this._toggleModule(moduleId);
            });
        });
    }

    async _toggleModule(moduleId) {
        if (this.enabledModules.has(moduleId)) {
            this.enabledModules.delete(moduleId);
            this._addEventLogEntry(`🔴 ${this.modules[moduleId].name} module disabled`);
        } else {
            this.enabledModules.add(moduleId);
            this._addEventLogEntry(`🟢 ${this.modules[moduleId].name} module enabled`);
        }

        // Save to localStorage
        localStorage.setItem('unified-enabled-modules', JSON.stringify([...this.enabledModules]));

        // Update toggle buttons
        this._renderModuleToggles();

        // Init/destroy adapter as needed
        if (this.enabledModules.has(moduleId) && !this.adapters[moduleId]) {
            await this._initAdapter(moduleId);
        } else if (!this.enabledModules.has(moduleId) && this.adapters[moduleId]) {
            this._destroyAdapter(moduleId);
        }

        // Re-render unified actions
        this._renderUnifiedActions();

        // Re-render info panel (character extensions)
        this._renderInfoPanel();

        // Update 3D scene overlays
        this._update3DScene();
    }

    // ── Adapter Management ──

    async _initEnabledAdapters() {
        const serverUrl = window.authService ? window.authService.getServerUrl() : '';
        for (const moduleId of this.enabledModules) {
            const mod = this.modules[moduleId];
            if (!mod) continue;

            // Construct API base URL
            let apiBase = mod.apiBase;
            if (apiBase && !apiBase.startsWith('http')) {
                apiBase = serverUrl + apiBase;
            }

            await this._initAdapter(moduleId, apiBase);
        }
    }

    async _initAdapter(moduleId, apiBase) {
        const mod = this.modules[moduleId];
        if (!mod) return;

        try {
            let adapter;
            switch (moduleId) {
                case 'earth2026':
                    adapter = new Earth2026Adapter(apiBase || (window.authService ? window.authService.getServerUrl() + '/earth2026' : ''));
                    break;
                case 'captains-log':
                    adapter = new CaptainsLogAdapter(apiBase || (window.authService ? window.authService.getServerUrl() + '/captains-log' : ''));
                    break;
                case 'dnd-5e':
                    adapter = new Dnd5eAdapter(apiBase || (window.authService ? window.authService.getServerUrl() + '/dnd5e' : ''));
                    break;
                case 'traveller':
                    adapter = new TravellerAdapter(apiBase || (window.authService ? window.authService.getServerUrl() + '/traveller' : ''));
                    break;
                case 'star-wars':
                    adapter = new StarWarsAdapter(apiBase || (window.authService ? window.authService.getServerUrl() + '/rules/engines' : ''));
                    break;
                case 'babylon5':
                    adapter = new Babylon5Adapter(apiBase || (window.authService ? window.authService.getServerUrl() + '/babylon5' : ''));
                    break;
                case 'star-trek-4x':
                    adapter = new StarTrek4xAdapter(apiBase || (window.authService ? window.authService.getServerUrl() + '/star-trek-4x' : ''));
                    break;
                case 'mech-warrior':
                    adapter = new MechWarriorAdapter(
                        apiBase || (window.authService ? window.authService.getServerUrl() + '/mechwarrior/fps' : ''),
                        (window.authService ? window.authService.getServerUrl() + '/mechwarrior/missions' : ''),
                        (window.authService ? window.authService.getServerUrl() + '/mechwarrior/enemies' : '')
                    );
                    break;
                case 'zero-zero-seven':
                    adapter = new ZeroZeroSevenAdapter(apiBase || (window.authService ? window.authService.getServerUrl() + '/zero-zero-seven' : ''));
                    break;
                default:
                    console.warn('[UnifiedWorldAdapter] No adapter class for module:', moduleId);
                    return;
            }

            if (adapter) {
                // Override the adapter's template ID so it works in unified mode
                adapter.currentTemplateId = moduleId;
                adapter.hudController = this.hudController;
                await adapter.init(this.hudController);
                this.adapters[moduleId] = adapter;
                console.log(`[UnifiedWorldAdapter] Initialized ${moduleId} adapter`);
            }
        } catch (error) {
            console.error(`[UnifiedWorldAdapter] Error initializing ${moduleId} adapter:`, error);
        }
    }

    _destroyAdapter(moduleId) {
        const adapter = this.adapters[moduleId];
        if (adapter) {
            if (adapter.destroy) adapter.destroy();
            delete this.adapters[moduleId];
            console.log(`[UnifiedWorldAdapter] Destroyed ${moduleId} adapter`);
        }
    }

    // ── Unified Actions Rendering ──

    _renderUnifiedActions() {
        const container = document.getElementById('hudResponsesPanelBody');
        if (!container) return;

        // Load unified template to get action definitions
        this._loadUnifiedTemplateActions(container);
    }

    async _loadUnifiedTemplateActions(container) {
        try {
            const serverUrl = window.authService ? window.authService.getServerUrl() : '';
            const response = await fetch(`${serverUrl}/dora-electron/renderer/hud/templates/unified-world.json`);
            const template = await response.json();

            if (!template.panels || !template.panels.responses || !template.panels.responses.components) {
                return;
            }

            let html = '';
            for (const component of template.panels.responses.components) {
                if (component.type === 'action-group') {
                    const moduleId = component.module;
                    const isEnabled = this.enabledModules.has(moduleId);
                    const mod = this.modules[moduleId];
                    const modName = mod ? mod.name : component.label;
                    const modIcon = mod ? mod.icon : '📦';
                    const statusClass = isEnabled ? 'active' : 'inactive';

                    html += `
                        <div class="hud-action-group" data-module="${moduleId}">
                            <div class="hud-action-group-header" data-module="${moduleId}">
                                <span class="hud-group-toggle">▼</span>
                                <span class="hud-module-status ${statusClass}"></span>
                                <span>${modIcon} ${modName}</span>
                            </div>
                            <div class="hud-action-group-body" data-module="${moduleId}">
                    `;

                    if (component.actions) {
                        for (const action of component.actions) {
                            const disabled = !isEnabled ? ' style="opacity:0.4;pointer-events:none"' : '';
                            html += `
                                <button class="hud-action-button"${disabled} data-action="${action.dataBinding}" data-module="${moduleId}">
                                    <span class="hud-action-icon">${action.icon}</span>
                                    <span class="hud-action-label">${action.label}</span>
                                    <span class="hud-action-hint">${action.hint}</span>
                                </button>
                            `;
                        }
                    }

                    html += `
                            </div>
                        </div>
                    `;
                }
            }

            container.innerHTML = html;

            // Bind action group header clicks (collapse/expand)
            container.querySelectorAll('.hud-action-group-header').forEach(header => {
                header.addEventListener('click', () => {
                    const body = header.nextElementSibling;
                    if (body) {
                        body.classList.toggle('collapsed');
                        header.classList.toggle('collapsed');
                    }
                });
            });

            // Enable/disable action groups based on module state
            container.querySelectorAll('.hud-action-group').forEach(group => {
                const moduleId = group.dataset.module;
                const isEnabled = this.enabledModules.has(moduleId);
                const body = group.querySelector('.hud-action-group-body');
                if (body) {
                    body.querySelectorAll('.hud-action-button').forEach(btn => {
                        if (isEnabled) {
                            btn.style.opacity = '';
                            btn.style.pointerEvents = '';
                        } else {
                            btn.style.opacity = '0.4';
                            btn.style.pointerEvents = 'none';
                        }
                    });
                }
            });
        } catch (error) {
            console.error('[UnifiedWorldAdapter] Error loading unified actions:', error);
        }
    }

    // ── Info Panel Rendering (Character Sheet + Extensions) ──

    _renderInfoPanel() {
        const container = document.getElementById('hudInfoPanelBody');
        if (!container) return;

        // Render Earth 2026 base character sheet
        const earthAdapter = this.adapters['earth2026'];
        if (earthAdapter && earthAdapter._renderInfoPanel) {
            earthAdapter._renderInfoPanel();
        } else {
            // Default character selector
            container.innerHTML = `
                <div class="hud-empty-state">
                    <span class="empty-icon">🌍</span>
                    <span>Earth 2026 character will appear here</span>
                </div>
            `;
        }

        // Add module extensions
        this._renderModuleExtensions(container);
    }

    _renderModuleExtensions(container) {
        // Captain's Log extension
        if (this.enabledModules.has('captains-log') && this.adapters['captains-log']) {
            const cl = this.adapters['captains-log'];
            if (cl.selectedCharacter) {
                const ext = document.createElement('div');
                ext.className = 'hud-character-extension';
                ext.innerHTML = `
                    <div class="hud-extension-title">🖊️ Captain's Log</div>
                    <div class="hud-extension-content">
                        ${cl.selectedMission ? `Mission: ${cl.selectedMission.title || 'Active'} (Act ${cl.selectedMission.currentAct || 1})` : 'No active mission'}
                        ${cl.selectedMission ? `<br>Momentum: ${cl.selectedMission.momentum || 0}/1 | Threat: ${cl.selectedMission.threat || 0}` : ''}
                    </div>
                `;
                container.appendChild(ext);
            }
        }

        // D&D 5e extension
        if (this.enabledModules.has('dnd-5e') && this.adapters['dnd-5e']) {
            const dnd = this.adapters['dnd-5e'];
            if (dnd.selectedCharacter) {
                const ext = document.createElement('div');
                ext.className = 'hud-character-extension';
                ext.innerHTML = `
                    <div class="hud-extension-title">🐉 D&D 5e</div>
                    <div class="hud-extension-content">
                        ${dnd.selectedCharacter.name || 'Character'} (${dnd.selectedCharacter.race || ''} ${dnd.selectedCharacter.className || ''} ${dnd.selectedCharacter.level || 1})
                        ${dnd.selectedCharacter.spellSlots ? `<br>Spell Slots: ${JSON.stringify(dnd.selectedCharacter.spellSlots)}` : ''}
                    </div>
                `;
                container.appendChild(ext);
            }
        }

        // Traveller extension
        if (this.enabledModules.has('traveller') && this.adapters['traveller']) {
            const trav = this.adapters['traveller'];
            if (trav.selectedCharacter) {
                const ext = document.createElement('div');
                ext.className = 'hud-character-extension';
                ext.innerHTML = `
                    <div class="hud-extension-title">🚀 Traveller</div>
                    <div class="hud-extension-content">
                        ${trav.selectedCharacter.name || 'Character'} — ${trav.selectedCharacter.career || 'No career'}
                        ${trav.selectedCharacter.psionic ? '<br>Psionic!' : ''}
                    </div>
                `;
                container.appendChild(ext);
            }
        }

        // Star Wars extension
        if (this.enabledModules.has('star-wars') && this.adapters['star-wars']) {
            const sw = this.adapters['star-wars'];
            if (sw.currentFaction) {
                const ext = document.createElement('div');
                ext.className = 'hud-character-extension';
                ext.innerHTML = `
                    <div class="hud-extension-title">⭐ Star Wars</div>
                    <div class="hud-extension-content">
                        Faction: ${sw.currentFaction.name || 'Unknown'}
                        ${sw.currentUnit ? `<br>Unit: ${sw.currentUnit.name || 'Unknown'}` : ''}
                    </div>
                `;
                container.appendChild(ext);
            }
        }
    }

    // ── Action Handler ──

    async _handleAction(action, moduleId) {
        // Route action to the appropriate module adapter
        if (moduleId && this.adapters[moduleId]) {
            const adapter = this.adapters[moduleId];
            if (adapter._handleAction) {
                await adapter._handleAction(action);
            }
        } else if (action === 'generate-narrative') {
            await this._generateUnifiedNarrative();
        } else {
            // Try to find the module that owns this action
            for (const [id, adapter] of Object.entries(this.adapters)) {
                if (adapter._handleAction) {
                    await adapter._handleAction(action);
                    break;
                }
            }
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
            case 'modules':
                this._addEventLogEntry(`📦 Active modules: ${[...this.enabledModules].join(', ')}`);
                break;
            case 'enable':
                const enableMod = parts[1];
                if (enableMod && this.modules[enableMod]) {
                    this._toggleModule(enableMod);
                }
                break;
            case 'disable':
                const disableMod = parts[1];
                if (disableMod && this.modules[disableMod] && !this.modules[disableMod].alwaysActive) {
                    this._toggleModule(disableMod);
                }
                break;
            case 'narrative':
                this._generateUnifiedNarrative();
                break;
            default:
                this._addEventLogEntry(`❓ Unknown command: ${command}. Available: status, modules, enable <mod>, disable <mod>, narrative`);
        }
    }

    // ── Unified Narrative ──

    async _generateUnifiedNarrative() {
        if (!window.AiNarrative) {
            this._addEventLogEntry('⚠️ AI narrative service not available');
            return;
        }

        // Aggregate context from all enabled modules
        let context = 'You are the narrator for a unified game world combining multiple game systems.\n';
        context += 'Active modules: ' + [...this.enabledModules].join(', ') + '.\n';

        for (const [moduleId, adapter] of Object.entries(this.adapters)) {
            if (adapter.selectedCharacter) {
                context += `\n${this.modules[moduleId]?.name || moduleId}: Character ${adapter.selectedCharacter.name || 'unknown'}\n`;
            }
            if (adapter.selectedMission) {
                context += `${this.modules[moduleId]?.name || moduleId}: Mission ${adapter.selectedMission.title || 'active'}\n`;
            }
            if (adapter.currentFaction) {
                context += `${this.modules[moduleId]?.name || moduleId}: Faction ${adapter.currentFaction.name || 'unknown'}\n`;
            }
        }

        context += '\nGenerate a brief narrative summary of the current state of this unified world.';

        const narrativeText = document.getElementById('hudNarrativeText');
        if (narrativeText) {
            narrativeText.innerHTML = '<span style="color:var(--hud-text-muted)">Generating narrative...</span>';
        }

        try {
            await window.AiNarrative.streamToPanel(
                this.narrativeSessionId,
                context,
                'You are the narrator for a unified game world combining multiple game systems. Describe the current state of the world in an engaging way.',
                (chunk) => {
                    if (narrativeText) {
                        narrativeText.innerHTML = chunk;
                    }
                }
            );
        } catch (error) {
            console.error('[UnifiedWorldAdapter] Error generating narrative:', error);
            if (narrativeText) {
                narrativeText.innerHTML = '<span style="color:#ef4444">Failed to generate narrative</span>';
            }
        }
    }

    // ── 3D Scene Management ──

    _init3DScene() {
        if (this.hudController && this.hudController._init3DScene) {
            this.hudController._init3DScene('unified-world');
        }
    }

    _update3DScene() {
        if (this.hudController && this.hudController.update3DScene) {
            const sceneData = {
                modules: [...this.enabledModules],
                theme: 'unified-world'
            };
            this.hudController.update3DScene(sceneData);
        }
    }

    // ── Event Log ──

    _addEventLogEntry(message) {
        if (this.hudController && this.hudController.addEventLogEntry) {
            this.hudController.addEventLogEntry(message);
        }
    }

    // ── Unified World Services ──

    async _loadUnifiedServices() {
        this.unifiedApiBase = (window.authService ? window.authService.getServerUrl() : '') + '/unified';
        await Promise.all([
            this._loadRecentEvents(),
            this._loadWorldMap(),
            this._loadQuests()
        ]);
    }

    async _loadRecentEvents() {
        try {
            const response = await this._fetch(`${this.unifiedApiBase}/events/recent?limit=20`);
            if (response) {
                this.recentEvents = response;
                this._renderEventNotifications();
            }
        } catch (error) {
            console.warn('[UnifiedWorldAdapter] Failed to load events:', error);
        }
    }

    async _loadWorldMap() {
        try {
            const [nodesResponse, edgesResponse] = await Promise.all([
                this._fetch(`${this.unifiedApiBase}/worldmap/nodes`),
                this._fetch(`${this.unifiedApiBase}/worldmap/edges`)
            ]);
            this.worldNodes = nodesResponse || [];
            this.worldEdges = edgesResponse || [];
        } catch (error) {
            console.warn('[UnifiedWorldAdapter] Failed to load world map:', error);
        }
    }

    async _loadQuests() {
        try {
            const response = await this._fetch(`${this.unifiedApiBase}/quests`);
            if (response) {
                this.quests = response;
                this.activeQuests = response.filter(q => q.status === 'ACTIVE');
                this._renderQuestLog();
            }
        } catch (error) {
            console.warn('[UnifiedWorldAdapter] Failed to load quests:', error);
        }
    }

    async _publishEvent(type, sourceModule, title, description, payload) {
        try {
            const response = await this._fetch(`${this.unifiedApiBase}/events/publish`, {
                method: 'POST',
                body: JSON.stringify({ type, sourceModule, title, description, payload })
            });
            if (response) {
                this.recentEvents.unshift(response);
                this._renderEventNotifications();
                // Auto-generate quest from significant events
                if (['MISSION_COMPLETE', 'COMBAT_WIN', 'DISCOVERY', 'FACTION_UNLOCK'].includes(type)) {
                    await this._generateQuestFromEvent(response);
                }
            }
            return response;
        } catch (error) {
            console.warn('[UnifiedWorldAdapter] Failed to publish event:', error);
        }
    }

    async _generateQuestFromEvent(event) {
        try {
            const response = await this._fetch(`${this.unifiedApiBase}/quests/generate`, {
                method: 'POST',
                body: JSON.stringify({
                    title: 'Aftermath: ' + event.title,
                    description: 'Following the event "' + event.title + '", new opportunities have arisen.',
                    sourceModules: [event.sourceModule],
                    objectives: [{
                        description: 'Investigate the consequences of ' + event.title,
                        module: event.sourceModule,
                        type: 'investigation',
                        target: 1
                    }],
                    rewards: { xp: 100, reputation: 10 }
                })
            });
            if (response) {
                this.quests.unshift(response);
                this._renderQuestLog();
                this._addEventLogEntry('📜 New quest available: ' + response.title);
            }
        } catch (error) {
            console.warn('[UnifiedWorldAdapter] Failed to generate quest:', error);
        }
    }

    async _discoverNode(nodeId) {
        try {
            const response = await this._fetch(`${this.unifiedApiBase}/worldmap/discover`, {
                method: 'POST',
                body: JSON.stringify({ nodeId })
            });
            if (response && response.discovered) {
                this._addEventLogEntry('🗺️ Discovered: ' + nodeId);
                await this._loadWorldMap();
            }
            return response;
        } catch (error) {
            console.warn('[UnifiedWorldAdapter] Failed to discover node:', error);
        }
    }

    async _activateQuest(questId) {
        try {
            const response = await this._fetch(`${this.unifiedApiBase}/quests/${questId}/activate`, {
                method: 'POST'
            });
            if (response && response.activated) {
                this._addEventLogEntry('📜 Quest activated: ' + questId);
                await this._loadQuests();
            }
            return response;
        } catch (error) {
            console.warn('[UnifiedWorldAdapter] Failed to activate quest:', error);
        }
    }

    async _completeObjective(questId, objectiveId) {
        try {
            const response = await this._fetch(`${this.unifiedApiBase}/quests/${questId}/objective/complete`, {
                method: 'POST',
                body: JSON.stringify({ objectiveId })
            });
            if (response && response.completed) {
                this._addEventLogEntry('✅ Objective completed');
                await this._loadQuests();
            }
            return response;
        } catch (error) {
            console.warn('[UnifiedWorldAdapter] Failed to complete objective:', error);
        }
    }

    // ── Event Notifications ──

    _renderEventNotifications() {
        const container = document.getElementById('hudEventNotifications');
        if (!container) return;

        if (this.recentEvents.length === 0) {
            container.innerHTML = '';
            return;
        }

        let html = '<div class="hud-event-notifications-header">🔔 Recent Events</div>';
        for (const event of this.recentEvents.slice(0, 5)) {
            const icon = this._getEventIcon(event.type);
            html += `
                <div class="hud-event-notification" data-event-id="${event.id}">
                    <span class="hud-event-icon">${icon}</span>
                    <span class="hud-event-title">${event.title}</span>
                    <span class="hud-event-module">${event.sourceModule}</span>
                </div>
            `;
        }
        container.innerHTML = html;
    }

    _getEventIcon(type) {
        const icons = {
            'MISSION_COMPLETE': '🎯',
            'COMBAT_WIN': '⚔️',
            'COMBAT_LOSS': '💀',
            'DISCOVERY': '🔍',
            'LEVEL_UP': '⭐',
            'FACTION_UNLOCK': '🏴',
            'QUEST_COMPLETE': '📜',
            'TERRITORY_CAPTURE': '🏰',
            'TRADE_DEAL': '💰',
            'DIPLOMACY_EVENT': '🤝',
            'CHARACTER_DEATH': '☠️',
            'CUSTOM': '📌'
        };
        return icons[type] || '📌';
    }

    // ── Quest Log ──

    _renderQuestLog() {
        const container = document.getElementById('hudQuestLog');
        if (!container) return;

        if (this.quests.length === 0) {
            container.innerHTML = `
                <div class="hud-empty-state">
                    <span class="empty-icon">📜</span>
                    <span>No quests available</span>
                </div>
            `;
            return;
        }

        let html = '<div class="hud-quest-log-header">📜 Quest Log</div>';
        for (const quest of this.quests.slice(0, 10)) {
            const statusIcon = quest.status === 'ACTIVE' ? '🔵' : quest.status === 'COMPLETED' ? '✅' : '⚪';
            const progress = `${quest.completedObjectives || 0}/${quest.totalObjectives || 0}`;
            html += `
                <div class="hud-quest-item" data-quest-id="${quest.id}" data-quest-status="${quest.status}">
                    <span class="hud-quest-status">${statusIcon}</span>
                    <div class="hud-quest-info">
                        <div class="hud-quest-title">${quest.title}</div>
                        <div class="hud-quest-progress">${progress}</div>
                    </div>
                    ${quest.status === 'AVAILABLE' ? `<button class="hud-quest-activate" data-quest-id="${quest.id}">Accept</button>` : ''}
                </div>
            `;
        }
        container.innerHTML = html;

        // Bind activate buttons
        container.querySelectorAll('.hud-quest-activate').forEach(btn => {
            btn.addEventListener('click', async () => {
                await this._activateQuest(btn.dataset.questId);
            });
        });
    }

    // ── World Map Panel ──

    _renderWorldMapPanel() {
        const container = document.getElementById('hudWorldMap');
        if (!container) return;

        if (this.worldNodes.length === 0) {
            container.innerHTML = `
                <div class="hud-empty-state">
                    <span class="empty-icon">🗺️</span>
                    <span>World map is empty. Explore to discover locations.</span>
                </div>
            `;
            return;
        }

        const discovered = this.worldNodes.filter(n => n.discovered);
        let html = `
            <div class="hud-worldmap-header">
                🗺️ World Map (${discovered.length}/${this.worldNodes.length} discovered)
            </div>
            <div class="hud-worldmap-canvas">
        `;

        for (const node of discovered) {
            const typeIcon = this._getNodeIcon(node.type);
            html += `
                <div class="hud-worldmap-node" data-node-id="${node.id}" 
                     style="left:${node.x}%;top:${node.y}%">
                    <span class="hud-node-icon">${typeIcon}</span>
                    <span class="hud-node-name">${node.name}</span>
                </div>
            `;
        }

        for (const edge of this.worldEdges) {
            const source = this.worldNodes.find(n => n.id === edge.sourceId && n.discovered);
            const target = this.worldNodes.find(n => n.id === edge.targetId && n.discovered);
            if (source && target) {
                html += `
                    <div class="hud-worldmap-edge" 
                         style="left:${source.x}%;top:${source.y}%;width:${Math.abs(target.x - source.x)}%;height:${Math.abs(target.y - source.y)}%">
                    </div>
                `;
            }
        }

        html += '</div>';
        container.innerHTML = html;
    }

    _getNodeIcon(type) {
        const icons = {
            'LOCATION': '📍',
            'FACTION': '🏴',
            'PLANET': '🌍',
            'STAR_SYSTEM': '⭐',
            'DUNGEON': '🏚️',
            'CITY': '🏙️',
            'SPACE_STATION': '🛸',
            'TERRITORY': '🏰',
            'CUSTOM': '📌'
        };
        return icons[type] || '📍';
    }

    // ── Enhanced Command Handler ──

    _handleCommand(cmd) {
        const parts = cmd.trim().split(' ');
        const command = parts[0].toLowerCase();

        switch (command) {
            case 'status':
                this._renderInfoPanel();
                this._addEventLogEntry('📊 Status refreshed');
                break;
            case 'modules':
                this._addEventLogEntry(`📦 Active modules: ${[...this.enabledModules].join(', ')}`);
                break;
            case 'enable':
                const enableMod = parts[1];
                if (enableMod && this.modules[enableMod]) {
                    this._toggleModule(enableMod);
                }
                break;
            case 'disable':
                const disableMod = parts[1];
                if (disableMod && this.modules[disableMod] && !this.modules[disableMod].alwaysActive) {
                    this._toggleModule(disableMod);
                }
                break;
            case 'narrative':
                this._generateUnifiedNarrative();
                break;
            case 'events':
                this._loadRecentEvents();
                this._addEventLogEntry(`🔔 ${this.recentEvents.length} recent events`);
                break;
            case 'quests':
                this._loadQuests();
                this._addEventLogEntry(`📜 ${this.quests.length} quests, ${this.activeQuests.length} active`);
                break;
            case 'map':
                this._loadWorldMap();
                this._renderWorldMapPanel();
                this._addEventLogEntry(`🗺️ ${this.worldNodes.filter(n => n.discovered).length}/${this.worldNodes.length} nodes discovered`);
                break;
            default:
                this._addEventLogEntry(`❓ Unknown command: ${command}. Available: status, modules, enable <mod>, disable <mod>, narrative, events, quests, map`);
        }
    }
}

// Expose globally
window.UnifiedWorldAdapter = UnifiedWorldAdapter;

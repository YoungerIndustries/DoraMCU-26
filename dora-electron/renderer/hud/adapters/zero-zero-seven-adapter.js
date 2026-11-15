/* Zero Zero Seven Adapter — Connects HUD panels to Zero Zero Seven REST API */

class ZeroZeroSevenAdapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.sessions = [];
        this.selectedSessionId = null;
        this.currentSession = null;
        this.empires = [];
        this.selectedEmpireId = null;
        this.currentEmpire = null;
        this.territories = [];
        this.operations = [];
        this.intelligenceLevel = 0;
        this.surveillanceCoverage = 0;
        this.networks = [];
        this.safehouses = [];
        this.relations = [];
        this.agents = [];
        this.availableAgents = [];
        this.currentView = 'session'; // session, operations, intelligence, networks, diplomacy, agents
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Restore session state from localStorage
        const savedSessionId = localStorage.getItem('z7-session-id');
        if (savedSessionId) {
            this.selectedSessionId = savedSessionId;
        }
        const savedEmpireId = localStorage.getItem('z7-empire-id');
        if (savedEmpireId) {
            this.selectedEmpireId = savedEmpireId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'zero-zero-seven') {
                this._onTemplateLoaded();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'zero-zero-seven') {
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

        console.log('[ZeroZeroSevenAdapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._loadSessions();
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
            case 'action-sabotage':
                this._launchSabotage();
                break;
            case 'action-assassination':
                this._launchAssassination();
                break;
            case 'action-infiltration':
                this._launchInfiltration();
                break;
            case 'action-gather-intel':
                this._gatherIntelligence();
                break;
            case 'action-detect-networks':
                this._detectNetworks();
                break;
            case 'action-neutralize-network':
                this._neutralizeNetwork();
                break;
            case 'action-establish-network':
                this._establishNetwork();
                break;
            case 'action-build-safehouse':
                this._buildSafehouse();
                break;
            case 'action-declare-war':
                this._declareWar();
                break;
            case 'action-establish-peace':
                this._establishPeace();
                break;
            case 'action-create-alliance':
                this._createAlliance();
                break;
            case 'action-trade-agreement':
                this._createTradeAgreement();
                break;
            case 'action-recruit-agent':
                this._recruitAgent();
                break;
            case 'action-view-agents':
                this._viewAgents();
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
            case 'operations':
                this._loadOperations();
                this._addEventLogEntry('📋 Operations list refreshed');
                break;
            case 'intelligence':
                this._gatherIntelligence();
                break;
            case 'networks':
                this._loadNetworks();
                this._addEventLogEntry('🏢 Networks list refreshed');
                break;
            case 'diplomacy':
                this._loadRelations();
                this._addEventLogEntry('🤝 Diplomacy status refreshed');
                break;
            case 'agents':
                this._loadAgents();
                this._addEventLogEntry('👤 Agents list refreshed');
                break;
            default:
                this._addEventLogEntry(`❓ Unknown command: ${command}. Available: status, operations, intelligence, networks, diplomacy, agents`);
        }
    }

    // ── Session Management ──

    async _loadSessions() {
        try {
            const data = await this._fetch('/sessions');
            this.sessions = data || [];

            if (this.sessions.length === 0) {
                this._renderSessionSelector();
                this._renderPromptPanel('No active sessions. Create a new session to begin your covert operations.');
                return;
            }

            // Restore selected session if valid
            if (this.selectedSessionId) {
                const session = this.sessions.find(s => s.id === this.selectedSessionId);
                if (session) {
                    this.currentSession = session;
                    this._loadEmpires();
                    return;
                }
            }

            this._renderSessionSelector();
            this._renderPromptPanel('Select a session to continue, or create a new one.');
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error loading sessions:', error);
            this._renderPromptPanel('Failed to load sessions. Check your connection and try again.');
        }
    }

    _renderSessionSelector() {
        const infoPanel = document.getElementById('hudInfoPanelBody');
        if (!infoPanel) return;

        let html = `
            <div class="hud-section-title">🕵️ CLASSIFIED — Spy Operations</div>
            <div class="hud-selector-group">
                <label for="hudSessionSelect">Select Session:</label>
                <select id="hudSessionSelect" class="hud-selector">
                    <option value="">-- New Session --</option>
        `;

        for (const session of this.sessions) {
            const selected = session.id === this.selectedSessionId ? 'selected' : '';
            html += `<option value="${session.id}" ${selected}>${session.name || 'Session ' + session.id} (Turn ${session.turn || 1})</option>`;
        }

        html += `
                </select>
            </div>
        `;

        if (this.currentSession) {
            html += `
                <div class="hud-stat-group">
                    <div class="hud-stat-label">Session: ${this.currentSession.name || 'Untitled'}</div>
                    <div class="hud-stat-label">Turn: ${this.currentSession.turn || 1}</div>
                </div>
            `;
        }

        infoPanel.innerHTML = html;

        const select = document.getElementById('hudSessionSelect');
        if (select) {
            select.addEventListener('change', (e) => {
                const sessionId = e.target.value;
                if (sessionId) {
                    this.selectedSessionId = sessionId;
                    this.currentSession = this.sessions.find(s => s.id === sessionId);
                    localStorage.setItem('z7-session-id', sessionId);
                    this._loadEmpires();
                }
            });
        }
    }

    async _createSession() {
        const name = prompt('Enter session name:', 'Covert Operations');
        if (!name) return;

        try {
            const data = await this._fetch(`/sessions?name=${encodeURIComponent(name)}`, { method: 'POST' });
            this.currentSession = data;
            this.selectedSessionId = data.id;
            localStorage.setItem('z7-session-id', data.id);
            this._addEventLogEntry(`🎮 Session created: ${name}`);
            this._loadSessions();
            this._loadEmpires();
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error creating session:', error);
            this._addEventLogEntry(`❌ Failed to create session: ${error.message}`);
        }
    }

    async _advanceTurn() {
        if (!this.selectedSessionId) {
            this._addEventLogEntry('⚠️ Select a session first');
            return;
        }

        try {
            const data = await this._fetch(`/sessions/${this.selectedSessionId}/advance-turn`, { method: 'POST' });
            if (data) {
                this.currentSession = data;
                this._addEventLogEntry(`⏭️ Turn advanced to ${data.turn || 'unknown'}`);
                this._renderInfoPanel();
                this._renderPromptPanel(`Turn ${data.turn || 'unknown'} complete. Operations processed.`);

                // AI narrative
                if (window.AiNarrative) {
                    window.AiNarrative.streamToPanel(
                        'z7-narrative-session',
                        `Turn ${data.turn} has ended in the spy operations game. Summarize the results of covert operations, intelligence gathering, and diplomatic events.`,
                        'You are a strategic advisor for a Cold War spy empire. Describe covert operations, intelligence reports, and diplomatic developments in a tense, dramatic style.'
                    );
                }
            }
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error advancing turn:', error);
            this._addEventLogEntry(`❌ Failed to advance turn: ${error.message}`);
        }
    }

    // ── Empire Management ──

    async _loadEmpires() {
        if (!this.selectedSessionId) return;

        try {
            const data = await this._fetch(`/sessions/${this.selectedSessionId}/empires`);
            this.empires = data || [];

            if (this.empires.length === 0) {
                this._addEventLogEntry('⚠️ No empires available in this session');
                return;
            }

            // Restore selected empire if valid
            if (this.selectedEmpireId) {
                const empire = this.empires.find(e => e.id === this.selectedEmpireId);
                if (empire) {
                    this.currentEmpire = empire;
                    this._renderInfoPanel();
                    this._loadOperations();
                    return;
                }
            }

            this._renderInfoPanel();
            this._renderPromptPanel('Select your empire to begin operations.');
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error loading empires:', error);
        }
    }

    // ── Info Panel Rendering ──

    _renderInfoPanel() {
        const infoPanel = document.getElementById('hudInfoPanelBody');
        if (!infoPanel) return;

        let html = `<div class="hud-section-title">🕵️ CLASSIFIED — Empire Status</div>`;

        // Session selector
        html += `
            <div class="hud-selector-group">
                <label for="hudSessionSelect">Session:</label>
                <select id="hudSessionSelect" class="hud-selector">
                    <option value="">-- Select --</option>
        `;
        for (const session of this.sessions) {
            const selected = session.id === this.selectedSessionId ? 'selected' : '';
            html += `<option value="${session.id}" ${selected}>${session.name || 'Session ' + session.id}</option>`;
        }
        html += `</select></div>`;

        // Empire selector
        if (this.empires.length > 0) {
            html += `
                <div class="hud-selector-group">
                    <label for="hudEmpireSelect">Empire:</label>
                    <select id="hudEmpireSelect" class="hud-selector">
                        <option value="">-- Select --</option>
            `;
            for (const empire of this.empires) {
                const selected = empire.id === this.selectedEmpireId ? 'selected' : '';
                html += `<option value="${empire.id}" ${selected}>${empire.name || empire.id}</option>`;
            }
            html += `</select></div>`;
        }

        // Stats
        if (this.currentSession) {
            html += `
                <div class="hud-stat-group">
                    <div class="hud-stat-label">Turn</div>
                    <div class="hud-stat-bar"><div class="hud-stat-fill" style="width: ${Math.min(100, (this.currentSession.turn || 1) * 5)}%"></div></div>
                    <div class="hud-stat-value">${this.currentSession.turn || 1}</div>
                </div>
            `;
        }

        html += `
            <div class="hud-stat-group">
                <div class="hud-stat-label">Intelligence Level</div>
                <div class="hud-stat-bar"><div class="hud-stat-fill" style="width: ${Math.min(100, this.intelligenceLevel * 10)}%"></div></div>
                <div class="hud-stat-value">${this.intelligenceLevel}</div>
            </div>
            <div class="hud-stat-group">
                <div class="hud-stat-label">Surveillance Coverage</div>
                <div class="hud-stat-bar"><div class="hud-stat-fill" style="width: ${Math.min(100, this.surveillanceCoverage)}%"></div></div>
                <div class="hud-stat-value">${this.surveillanceCoverage}%</div>
            </div>
            <div class="hud-stat-group">
                <div class="hud-stat-label">Active Operations</div>
                <div class="hud-stat-bar"><div class="hud-stat-fill" style="width: ${Math.min(100, this.operations.length * 20)}%"></div></div>
                <div class="hud-stat-value">${this.operations.length}</div>
            </div>
        `;

        // Empire details
        if (this.currentEmpire) {
            html += `
                <div class="hud-section-title" style="margin-top: 10px;">🏛️ ${this.currentEmpire.name || 'Empire'}</div>
                <div class="hud-stat-label">Territories: ${this.territories.length || 0}</div>
                <div class="hud-stat-label">Networks: ${this.networks.length || 0}</div>
                <div class="hud-stat-label">Agents: ${this.agents.length || 0}</div>
            `;
        }

        infoPanel.innerHTML = html;

        // Bind session selector
        const sessionSelect = document.getElementById('hudSessionSelect');
        if (sessionSelect) {
            sessionSelect.addEventListener('change', (e) => {
                const sessionId = e.target.value;
                if (sessionId) {
                    this.selectedSessionId = sessionId;
                    this.currentSession = this.sessions.find(s => s.id === sessionId);
                    localStorage.setItem('z7-session-id', sessionId);
                    this._loadEmpires();
                }
            });
        }

        // Bind empire selector
        const empireSelect = document.getElementById('hudEmpireSelect');
        if (empireSelect) {
            empireSelect.addEventListener('change', (e) => {
                const empireId = e.target.value;
                if (empireId) {
                    this.selectedEmpireId = empireId;
                    this.currentEmpire = this.empires.find(e => e.id === empireId);
                    localStorage.setItem('z7-empire-id', empireId);
                    this._renderInfoPanel();
                    this._loadOperations();
                    this._loadNetworks();
                    this._loadAgents();
                }
            });
        }
    }

    // ── Covert Operations ──

    async _loadOperations() {
        if (!this.selectedSessionId) return;

        try {
            const data = await this._fetch(`/operations?gameSessionId=${this.selectedSessionId}`);
            this.operations = data || [];
            this._renderInfoPanel();
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error loading operations:', error);
        }
    }

    async _launchSabotage() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const targetEmpire = prompt('Enter target empire ID:');
        if (!targetEmpire) return;

        const agentId = prompt('Enter agent ID (optional):');
        const difficulty = prompt('Enter difficulty (1-10):', '5');

        try {
            const params = new URLSearchParams({ targetEmpireId: targetEmpire, difficulty: difficulty || '5' });
            if (agentId) params.append('agentId', agentId);

            const data = await this._fetch(`/operations/sabotage?${params}`, {
                method: 'POST',
                body: JSON.stringify({ empireId: this.selectedEmpireId, targetEmpireId: targetEmpire, difficulty: parseInt(difficulty) || 5, agentId: agentId || null })
            });

            this._addEventLogEntry(`💣 Sabotage launched against ${targetEmpire}`);
            this._renderPromptPanel(`Sabotage operation initiated against ${targetEmpire}. Agent deployed. Awaiting results...`);

            // AI narrative
            if (window.AiNarrative) {
                window.AiNarrative.streamToPanel(
                    'z7-narrative-session',
                    `A sabotage operation has been launched against ${targetEmpire}. Describe the covert operation, the agent's actions, and the outcome.`,
                    'You are a strategic advisor for a Cold War spy empire. Describe covert operations, intelligence reports, and diplomatic developments in a tense, dramatic style.'
                );
            }

            // Publish cross-module event
            if (window.UnifiedWorld) {
                window.UnifiedWorld._publishEvent(
                    'DISCOVERY',
                    'zerozeroseven',
                    `Sabotage launched against ${targetEmpire}`,
                    `Covert sabotage operation initiated.`,
                    { targetEmpire, difficulty: parseInt(difficulty) || 5 }
                );
            }
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error launching sabotage:', error);
            this._addEventLogEntry(`❌ Sabotage failed: ${error.message}`);
        }
    }

    async _launchAssassination() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const targetEmpire = prompt('Enter target empire ID:');
        if (!targetEmpire) return;

        const agentId = prompt('Enter agent ID (optional):');
        const difficulty = prompt('Enter difficulty (1-10):', '7');

        try {
            const data = await this._fetch(`/operations/assassination`, {
                method: 'POST',
                body: JSON.stringify({ empireId: this.selectedEmpireId, targetEmpireId: targetEmpire, difficulty: parseInt(difficulty) || 7, agentId: agentId || null })
            });

            this._addEventLogEntry(`🔫 Assassination ordered against ${targetEmpire}`);
            this._renderPromptPanel(`Assassination mission initiated against ${targetEmpire}. The target has been identified. Agent en route...`);

            if (window.AiNarrative) {
                window.AiNarrative.streamToPanel(
                    'z7-narrative-session',
                    `An assassination mission has been ordered against ${targetEmpire}. Describe the mission briefing, the agent's preparation, and the execution.`,
                    'You are a strategic advisor for a Cold War spy empire. Describe covert operations, intelligence reports, and diplomatic developments in a tense, dramatic style.'
                );
            }
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error launching assassination:', error);
            this._addEventLogEntry(`❌ Assassination failed: ${error.message}`);
        }
    }

    async _launchInfiltration() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const targetEmpire = prompt('Enter target empire ID:');
        if (!targetEmpire) return;

        const agentId = prompt('Enter agent ID (optional):');
        const difficulty = prompt('Enter difficulty (1-10):', '6');

        try {
            const data = await this._fetch(`/operations/infiltration`, {
                method: 'POST',
                body: JSON.stringify({ empireId: this.selectedEmpireId, targetEmpireId: targetEmpire, difficulty: parseInt(difficulty) || 6, agentId: agentId || null })
            });

            this._addEventLogEntry(`🕵️ Infiltration launched against ${targetEmpire}`);
            this._renderPromptPanel(`Infiltration operation initiated against ${targetEmpire}. Agent going deep undercover...`);

            if (window.AiNarrative) {
                window.AiNarrative.streamToPanel(
                    'z7-narrative-session',
                    `An infiltration operation has been launched against ${targetEmpire}. Describe the agent going undercover, the challenges faced, and the intelligence gathered.`,
                    'You are a strategic advisor for a Cold War spy empire. Describe covert operations, intelligence reports, and diplomatic developments in a tense, dramatic style.'
                );
            }
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error launching infiltration:', error);
            this._addEventLogEntry(`❌ Infiltration failed: ${error.message}`);
        }
    }

    // ── Intelligence ──

    async _gatherIntelligence() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        try {
            const data = await this._fetch(`/intelligence/gather?empireId=${this.selectedEmpireId}`);
            if (data) {
                this.intelligenceLevel = data.intelligenceLevel || this.intelligenceLevel + 1;
                this._addEventLogEntry(`📡 Intelligence gathered: level ${this.intelligenceLevel}`);
                this._renderInfoPanel();
                this._renderPromptPanel(`Intelligence gathering complete. Intelligence level increased to ${this.intelligenceLevel}. New information available.`);

                if (window.AiNarrative) {
                    window.AiNarrative.streamToPanel(
                        'z7-narrative-session',
                        `Intelligence has been gathered. Describe the new information discovered, any threats identified, and strategic implications.`,
                        'You are a strategic advisor for a Cold War spy empire. Describe covert operations, intelligence reports, and diplomatic developments in a tense, dramatic style.'
                    );
                }
            }
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error gathering intelligence:', error);
            this._addEventLogEntry(`❌ Intelligence gathering failed: ${error.message}`);
        }
    }

    async _detectNetworks() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const locationId = prompt('Enter location ID to scan (optional):');

        try {
            const params = new URLSearchParams({ empireId: this.selectedEmpireId });
            if (locationId) params.append('locationId', locationId);

            const data = await this._fetch(`/intelligence/counter-intelligence/detect?${params}`);
            if (data) {
                this._addEventLogEntry(`🔍 Network detection complete: ${data.detected || 0} networks found`);
                this._renderPromptPanel(`Counter-intelligence scan complete. ${data.detected || 0} enemy networks detected in the area.`);
            }
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error detecting networks:', error);
            this._addEventLogEntry(`❌ Network detection failed: ${error.message}`);
        }
    }

    async _neutralizeNetwork() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const networkId = prompt('Enter network ID to neutralize:');
        if (!networkId) return;

        const strength = prompt('Enter operation strength (1-10):', '5');

        try {
            const data = await this._fetch(`/intelligence/counter-intelligence/neutralize`, {
                method: 'POST',
                body: JSON.stringify({ empireId: this.selectedEmpireId, networkId, strength: parseInt(strength) || 5 })
            });

            this._addEventLogEntry(`🚫 Network ${networkId} neutralized`);
            this._renderPromptPanel(`Counter-intelligence operation complete. Enemy network ${networkId} has been neutralized.`);
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error neutralizing network:', error);
            this._addEventLogEntry(`❌ Network neutralization failed: ${error.message}`);
        }
    }

    // ── Spy Networks ──

    async _loadNetworks() {
        if (!this.selectedEmpireId) return;

        try {
            const data = await this._fetch(`/networks?empireId=${this.selectedEmpireId}`);
            this.networks = data || [];
            this._renderInfoPanel();
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error loading networks:', error);
        }
    }

    async _establishNetwork() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const name = prompt('Enter network name:');
        if (!name) return;

        const locationId = prompt('Enter location ID:');
        if (!locationId) return;

        try {
            const data = await this._fetch(`/networks/establish`, {
                method: 'POST',
                body: JSON.stringify({ empireId: this.selectedEmpireId, name, locationId })
            });

            this._addEventLogEntry(`🏢 Network established: ${name}`);
            this._renderPromptPanel(`Spy network "${name}" established at location ${locationId}. Recruitment begins...`);

            if (window.AiNarrative) {
                window.AiNarrative.streamToPanel(
                    'z7-narrative-session',
                    `A new spy network "${name}" has been established. Describe the network setup, the local contacts recruited, and the initial intelligence flow.`,
                    'You are a strategic advisor for a Cold War spy empire. Describe covert operations, intelligence reports, and diplomatic developments in a tense, dramatic style.'
                );
            }
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error establishing network:', error);
            this._addEventLogEntry(`❌ Network establishment failed: ${error.message}`);
        }
    }

    async _buildSafehouse() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const networkId = prompt('Enter network ID:');
        if (!networkId) return;

        try {
            const data = await this._fetch(`/networks/${networkId}/safehouse`, {
                method: 'POST',
                body: JSON.stringify({ empireId: this.selectedEmpireId })
            });

            this._addEventLogEntry(`🏠 Safehouse built for network ${networkId}`);
            this._renderPromptPanel(`Safehouse constructed for network ${networkId}. Agents now have a secure location.`);
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error building safehouse:', error);
            this._addEventLogEntry(`❌ Safehouse construction failed: ${error.message}`);
        }
    }

    // ── Diplomacy ──

    async _loadRelations() {
        if (!this.selectedSessionId) return;

        try {
            const data = await this._fetch(`/diplomacy?gameSessionId=${this.selectedSessionId}`);
            this.relations = data || [];
            this._renderPromptPanel('Diplomatic relations updated.');
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error loading relations:', error);
        }
    }

    async _declareWar() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const targetEmpire = prompt('Enter target empire to declare war on:');
        if (!targetEmpire) return;

        try {
            const data = await this._fetch(`/diplomacy/declare-war`, {
                method: 'POST',
                body: JSON.stringify({ attackerId: this.selectedEmpireId, defenderId: targetEmpire })
            });

            this._addEventLogEntry(`⚔️ War declared on ${targetEmpire}`);
            this._renderPromptPanel(`WAR DECLARED! Hostilities have begun against ${targetEmpire}. All covert operations are now authorized.`);

            if (window.AiNarrative) {
                window.AiNarrative.streamToPanel(
                    'z7-narrative-session',
                    `War has been declared against ${targetEmpire}. Describe the declaration, the immediate response, and the escalation of covert operations.`,
                    'You are a strategic advisor for a Cold War spy empire. Describe covert operations, intelligence reports, and diplomatic developments in a tense, dramatic style.'
                );
            }
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error declaring war:', error);
            this._addEventLogEntry(`❌ War declaration failed: ${error.message}`);
        }
    }

    async _establishPeace() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const targetEmpire = prompt('Enter empire to establish peace with:');
        if (!targetEmpire) return;

        try {
            const data = await this._fetch(`/diplomacy/peace`, {
                method: 'POST',
                body: JSON.stringify({ faction1Id: this.selectedEmpireId, faction2Id: targetEmpire })
            });

            this._addEventLogEntry(`🕊️ Peace established with ${targetEmpire}`);
            this._renderPromptPanel(`Peace negotiations successful. Hostilities with ${targetEmpire} have ceased.`);
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error establishing peace:', error);
            this._addEventLogEntry(`❌ Peace negotiation failed: ${error.message}`);
        }
    }

    async _createAlliance() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const targetEmpire = prompt('Enter empire to ally with:');
        if (!targetEmpire) return;

        try {
            const data = await this._fetch(`/diplomacy/alliance`, {
                method: 'POST',
                body: JSON.stringify({ faction1Id: this.selectedEmpireId, faction2Id: targetEmpire })
            });

            this._addEventLogEntry(`🤝 Alliance formed with ${targetEmpire}`);
            this._renderPromptPanel(`Alliance established with ${targetEmpire}. Joint operations now possible.`);
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error creating alliance:', error);
            this._addEventLogEntry(`❌ Alliance failed: ${error.message}`);
        }
    }

    async _createTradeAgreement() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const targetEmpire = prompt('Enter empire for trade agreement:');
        if (!targetEmpire) return;

        try {
            const data = await this._fetch(`/diplomacy/trade-agreement`, {
                method: 'POST',
                body: JSON.stringify({ faction1Id: this.selectedEmpireId, faction2Id: targetEmpire })
            });

            this._addEventLogEntry(`📋 Trade agreement with ${targetEmpire}`);
            this._renderPromptPanel(`Trade agreement signed with ${targetEmpire}. Economic benefits flowing.`);
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error creating trade agreement:', error);
            this._addEventLogEntry(`❌ Trade agreement failed: ${error.message}`);
        }
    }

    // ── Agents ──

    async _loadAgents() {
        if (!this.selectedEmpireId) return;

        try {
            const data = await this._fetch(`/agents?empireId=${this.selectedEmpireId}`);
            this.agents = data || [];
            this._renderInfoPanel();
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error loading agents:', error);
        }
    }

    async _recruitAgent() {
        if (!this.selectedEmpireId) {
            this._addEventLogEntry('⚠️ Select an empire first');
            return;
        }

        const name = prompt('Enter agent name:');
        if (!name) return;

        const codeName = prompt('Enter code name:', 'Agent ' + String(Math.floor(Math.random() * 100)).padStart(2, '0'));

        try {
            const data = await this._fetch(`/agents/recruit`, {
                method: 'POST',
                body: JSON.stringify({ empireId: this.selectedEmpireId, name, codeName })
            });

            this._addEventLogEntry(`👤 Agent recruited: ${codeName || name}`);
            this._renderPromptPanel(`New agent "${codeName || name}" has been recruited. Training begins immediately.`);

            if (window.AiNarrative) {
                window.AiNarrative.streamToPanel(
                    'z7-narrative-session',
                    `A new agent "${codeName || name}" has been recruited. Describe the recruitment process, the agent's background, and their first assignment.`,
                    'You are a strategic advisor for a Cold War spy empire. Describe covert operations, intelligence reports, and diplomatic developments in a tense, dramatic style.'
                );
            }
        } catch (error) {
            console.error('[ZeroZeroSevenAdapter] Error recruiting agent:', error);
            this._addEventLogEntry(`❌ Agent recruitment failed: ${error.message}`);
        }
    }

    _viewAgents() {
        if (this.agents.length === 0) {
            this._renderPromptPanel('No agents recruited yet. Use "Recruit Agent" to add spies to your roster.');
            return;
        }

        let narrative = '📖 Agent Roster:\n\n';
        for (const agent of this.agents) {
            narrative += `• ${agent.codeName || agent.name} (${agent.status || 'unknown'})`;
            if (agent.skills) {
                narrative += ` — Stealth: ${agent.skills.stealth || 0}, Charisma: ${agent.skills.charisma || 0}, Combat: ${agent.skills.combat || 0}, Technical: ${agent.skills.technical || 0}`;
            }
            narrative += '\n';
        }

        this._renderPromptPanel(narrative);
    }

    // ── AI Narrative ──

    _generateNarrative() {
        if (!window.AiNarrative) {
            this._addEventLogEntry('⚠️ AI narrative service unavailable');
            return;
        }

        let context = 'Describe the current state of the spy operations.';
        if (this.currentEmpire) {
            context += ` The empire "${this.currentEmpire.name}" is on turn ${this.currentSession?.turn || 1}.`;
        }
        if (this.operations.length > 0) {
            context += ` There are ${this.operations.length} active operations.`;
        }
        if (this.agents.length > 0) {
            context += ` ${this.agents.length} agents are in the field.`;
        }

        window.AiNarrative.streamToPanel(
            'z7-narrative-session',
            context,
            'You are a strategic advisor for a Cold War spy empire. Describe covert operations, intelligence reports, and diplomatic developments in a tense, dramatic style.'
        );
    }

    // ── Prompt Panel Rendering ──

    _renderPromptPanel(text) {
        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        promptPanel.innerHTML = `
            <div class="hud-narrative-text" style="white-space: pre-wrap; font-family: 'Courier New', monospace;">
                ${this._escapeHtml(text)}
            </div>
        `;
    }

    // ── Utility ──

    _escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    _addEventLogEntry(message) {
        const log = document.getElementById('hudEventLog');
        if (log) {
            const entry = document.createElement('div');
            entry.className = 'hud-log-entry';
            entry.textContent = `[${new Date().toLocaleTimeString()}] ${message}`;
            log.appendChild(entry);
            log.scrollTop = log.scrollHeight;
        }
    }
}

// Expose globally
window.ZeroZeroSevenAdapter = ZeroZeroSevenAdapter;

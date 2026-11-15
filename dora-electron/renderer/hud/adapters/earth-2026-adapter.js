/* Earth 2026 Adapter — Connects HUD panels to Earth 2026 REST API */

class Earth2026Adapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.characters = [];
        this.selectedCharacterId = null;
        this.currentCharacter = null;
        this.scenarios = [];
        this.selectedScenarioId = null;
        this.currentScenario = null;
        this.survivalStatus = null;
        this.parties = [];
        this.organizations = [];
        this.companions = [];
        this.locations = [];
        this.properties = [];
        this.currentPartyId = null;
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Restore session state from localStorage
        const savedCharId = localStorage.getItem('e2026-character-id');
        if (savedCharId) {
            this.selectedCharacterId = savedCharId;
        }
        const savedScenarioId = localStorage.getItem('e2026-scenario-id');
        if (savedScenarioId) {
            this.selectedScenarioId = savedScenarioId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'earth-2026') {
                this._onTemplateLoaded();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'earth-2026') {
                this._handleAction(action);
            }
        });

        // Listen for character selector change
        const charSelector = document.getElementById('hudCharSelector');
        if (charSelector) {
            charSelector.addEventListener('change', (e) => {
                this._selectCharacter(e.target.value);
            });
        }

        // Listen for scenario selector change
        const scenarioSelector = document.getElementById('hudScenarioSelector');
        if (scenarioSelector) {
            scenarioSelector.addEventListener('change', (e) => {
                this._selectScenario(e.target.value);
            });
        }

        // Listen for party chat submit
        const chatInput = document.querySelector('.hud-chat-input');
        if (chatInput) {
            const input = chatInput.querySelector('input');
            const button = chatInput.querySelector('button');
            if (input && button) {
                const sendChat = () => {
                    const msg = input.value.trim();
                    if (msg) {
                        this._sendPartyChat(msg);
                        input.value = '';
                    }
                };
                button.addEventListener('click', sendChat);
                input.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter') sendChat();
                });
            }
        }

        console.log('[Earth2026Adapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._loadCharacters();
        this._loadScenarios();
        this._loadLocations();
    }

    // ── Character Management ──

    async _loadCharacters() {
        try {
            this.characters = await this._fetch('/characters') || [];
            this._renderCharacterSelector();
            if (this.selectedCharacterId) {
                await this._selectCharacter(this.selectedCharacterId);
            } else if (this.characters.length > 0) {
                await this._selectCharacter(this.characters[0].id);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load characters', 'danger');
        }
    }

    _renderCharacterSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        let selectorHtml = `<select class="hud-selector" id="hudCharSelector">
            <option value="">Select Character...</option>
            ${this.characters.map(c => `
                <option value="${c.id}" ${c.id === this.selectedCharacterId ? 'selected' : ''}>
                    ${c.name || 'Unnamed'} (Lv.${c.level || 1})
                </option>
            `).join('')}
        </select>`;

        // Add create button
        selectorHtml += `<button class="hud-action-button" data-action="create-character" style="margin-bottom:10px;">
            <span class="action-icon">➕</span>
            <span class="action-label">Create New Character</span>
        </button>`;

        // Prepend selector to info panel
        const existing = body.querySelector('#hudCharSelector');
        if (existing) {
            existing.parentElement.outerHTML = selectorHtml;
        } else {
            body.insertAdjacentHTML('afterbegin', selectorHtml);
        }

        // Re-bind event
        const newSelector = document.getElementById('hudCharSelector');
        if (newSelector) {
            newSelector.addEventListener('change', (e) => {
                this._selectCharacter(e.target.value);
            });
        }
    }

    async _selectCharacter(characterId) {
        if (!characterId) return;
        this.selectedCharacterId = characterId;
        localStorage.setItem('e2026-character-id', characterId);

        try {
            this.currentCharacter = await this._fetch(`/characters/${characterId}`);
            if (this.currentCharacter) {
                this._renderCharacterSheet();
                await this._loadSurvivalStatus();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load character details', 'danger');
        }
    }

    _renderCharacterSheet() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body || !this.currentCharacter) return;

        const c = this.currentCharacter;
        const abilities = [
            { name: 'STR', value: c.str || c.strength || 10 },
            { name: 'DEX', value: c.dex || c.dexterity || 10 },
            { name: 'CON', value: c.con || c.constitution || 10 },
            { name: 'INT', value: c.int || c.intelligence || 10 },
            { name: 'WIS', value: c.wis || c.wisdom || 10 },
            { name: 'CHA', value: c.cha || c.charisma || 10 }
        ];

        const hpPercent = c.maxHp ? Math.round((c.hp / c.maxHp) * 100) : 100;
        const xpPercent = c.nextLevelXp ? Math.round((c.xp / c.nextLevelXp) * 100) : 0;

        let sheetHtml = `
        <div class="hud-character-sheet">
            <div class="char-name">${c.name || 'Unnamed'}</div>
            <div class="char-meta">Level ${c.level || 1} | ${c.className || 'No class'}</div>

            <div class="ability-grid">
                ${abilities.map(a => `
                    <div class="ability-score">
                        <div class="ability-name">${a.name}</div>
                        <div class="ability-value">${a.value}</div>
                        <div class="ability-mod">${a.value >= 10 ? '+' : ''}${Math.floor((a.value - 10) / 2)}</div>
                    </div>
                `).join('')}
            </div>

            <div class="hud-stat-bar">
                <div class="hud-stat-bar-label">
                    <span>HP</span>
                    <span class="stat-value">${c.hp || 0} / ${c.maxHp || 0}</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill ${hpPercent > 50 ? 'success' : hpPercent > 25 ? 'warning' : 'danger'}"
                         style="width: ${hpPercent}%"></div>
                </div>
            </div>

            <div class="hud-stat-bar">
                <div class="hud-stat-bar-label">
                    <span>XP</span>
                    <span class="stat-value">${c.xp || 0} / ${c.nextLevelXp || '?'}</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill info" style="width: ${xpPercent}%"></div>
                </div>
            </div>
        </div>`;

        // Survival stats
        if (this.survivalStatus) {
            const s = this.survivalStatus;
            sheetHtml += `
            <div style="margin-bottom:10px;">
                <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin-bottom:6px;">Survival</div>
                <div class="hud-stat-bar">
                    <div class="hud-stat-bar-label">
                        <span>Hunger</span>
                        <span class="stat-value">${s.hunger || 0}%</span>
                    </div>
                    <div class="hud-stat-bar-track">
                        <div class="hud-stat-bar-fill ${s.hunger > 70 ? 'danger' : s.hunger > 40 ? 'warning' : 'success'}"
                             style="width: ${s.hunger || 0}%"></div>
                    </div>
                </div>
                <div class="hud-stat-bar">
                    <div class="hud-stat-bar-label">
                        <span>Thirst</span>
                        <span class="stat-value">${s.thirst || 0}%</span>
                    </div>
                    <div class="hud-stat-bar-track">
                        <div class="hud-stat-bar-fill ${s.thirst > 70 ? 'danger' : s.thirst > 40 ? 'warning' : 'success'}"
                             style="width: ${s.thirst || 0}%"></div>
                    </div>
                </div>
                <div class="hud-stat-bar">
                    <div class="hud-stat-bar-label">
                        <span>Fatigue</span>
                        <span class="stat-value">${s.fatigue || 0}%</span>
                    </div>
                    <div class="hud-stat-bar-track">
                        <div class="hud-stat-bar-fill ${s.fatigue > 70 ? 'danger' : s.fatigue > 40 ? 'warning' : 'success'}"
                             style="width: ${s.fatigue || 0}%"></div>
                    </div>
                </div>
            </div>`;
        }

        // Location
        if (c.locationName || c.location) {
            sheetHtml += `<div style="font-size:11px;color:var(--hud-text-muted);margin-bottom:6px;">📍 ${c.locationName || c.location || 'Unknown'}</div>`;
        }

        // Inventory summary
        if (c.inventory && c.inventory.length > 0) {
            sheetHtml += `<div style="font-size:11px;color:var(--hud-text-muted);margin-bottom:6px;">🎒 Items: ${c.inventory.length}</div>`;
        }

        // Replace the character sheet (keep selector)
        const selector = body.querySelector('#hudCharSelector') || body.querySelector('.hud-selector');
        const createBtn = body.querySelector('[data-action="create-character"]');
        if (selector) {
            const selectorHtml = selector.outerHTML;
            const createBtnHtml = createBtn ? createBtn.outerHTML : '';
            body.innerHTML = selectorHtml + createBtnHtml + sheetHtml;

            // Re-bind selector
            const newSelector = document.getElementById('hudCharSelector');
            if (newSelector) {
                newSelector.addEventListener('change', (e) => {
                    this._selectCharacter(e.target.value);
                });
            }
        } else {
            body.innerHTML = sheetHtml;
        }
    }

    // ── Survival Status ──

    async _loadSurvivalStatus() {
        if (!this.selectedCharacterId) return;
        try {
            this.survivalStatus = await this._fetch(`/survival/status/${this.selectedCharacterId}`);
            if (this.survivalStatus) {
                this._renderCharacterSheet(); // re-render with survival stats
            }
        } catch (err) {
            // Survival status may not be available yet
        }
    }

    // ── Scenario Management ──

    async _loadScenarios() {
        try {
            this.scenarios = await this._fetch('/scenarios') || [];
            this._renderScenarioSelector();
            if (this.selectedScenarioId) {
                await this._selectScenario(this.selectedScenarioId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load scenarios', 'danger');
        }
    }

    _renderScenarioSelector() {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body) return;

        const selectorHtml = `<select class="hud-selector" id="hudScenarioSelector">
            <option value="">Select Scenario...</option>
            ${this.scenarios.map(s => `
                <option value="${s.id}" ${s.id === this.selectedScenarioId ? 'selected' : ''}>
                    ${s.name || 'Unnamed Scenario'}
                </option>
            `).join('')}
        </select>`;

        const startBtnHtml = `<button class="hud-action-button" data-action="start-scenario" style="margin-bottom:10px;">
            <span class="action-icon">▶️</span>
            <span class="action-label">Start Scenario</span>
        </button>`;

        const existing = body.querySelector('#hudScenarioSelector');
        if (existing) {
            existing.parentElement.outerHTML = selectorHtml;
        } else {
            body.insertAdjacentHTML('afterbegin', selectorHtml + startBtnHtml);
        }

        // Re-bind
        const newSelector = document.getElementById('hudScenarioSelector');
        if (newSelector) {
            newSelector.addEventListener('change', (e) => {
                this._selectScenario(e.target.value);
            });
        }
    }

    async _selectScenario(scenarioId) {
        if (!scenarioId) return;
        this.selectedScenarioId = scenarioId;
        localStorage.setItem('e2026-scenario-id', scenarioId);

        try {
            this.currentScenario = await this._fetch(`/scenarios/${scenarioId}`);
            if (this.currentScenario) {
                this._renderScenarioNarrative();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load scenario', 'danger');
        }
    }

    _renderScenarioNarrative() {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body || !this.currentScenario) return;

        const s = this.currentScenario;
        const narrative = s.description || s.briefing || s.prompt || 'No description available.';

        // Keep selectors
        const selector = body.querySelector('#hudScenarioSelector');
        const startBtn = body.querySelector('[data-action="start-scenario"]');
        const selectorHtml = selector ? selector.outerHTML : '';
        const startBtnHtml = startBtn ? startBtn.outerHTML : '';

        body.innerHTML = selectorHtml + startBtnHtml + `
            <div class="hud-narrative-text">
                <p><strong>${s.name || 'Scenario'}</strong></p>
                <p>${narrative}</p>
                ${s.difficulty ? `<div class="narrative-source">Difficulty: ${s.difficulty}</div>` : ''}
                ${s.status ? `<div class="narrative-source">Status: ${s.status}</div>` : ''}
            </div>`;

        // Re-bind
        const newSelector = document.getElementById('hudScenarioSelector');
        if (newSelector) {
            newSelector.addEventListener('change', (e) => {
                this._selectScenario(e.target.value);
            });
        }
    }

    // ── Location Management ──

    async _loadLocations() {
        try {
            this.locations = await this._fetch('/locations') || [];
        } catch (err) {
            // Locations may not be available
        }
    }

    // ── Action Handlers ──

    async _handleAction(action) {
        if (!this.selectedCharacterId) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }

        switch (action) {
            case 'create-character':
                await this._createCharacter();
                break;
            case 'scavenge':
                await this._scavenge();
                break;
            case 'rest':
                await this._rest();
                break;
            case 'travel':
                await this._travel();
                break;
            case 'start-scenario':
                await this._startScenario();
                break;
            case 'join-party':
                await this._joinParty();
                break;
            case 'join-organization':
                await this._joinOrganization();
                break;
            case 'recruit-companion':
                await this._recruitCompanion();
                break;
            case 'view-properties':
                await this._viewProperties();
                break;
            default:
                this.hudController.addEventLogEntry(`Unknown action: ${action}`, 'warning');
        }
    }

    // ── Character Creation ──

    async _createCharacter() {
        const name = prompt('Enter character name:');
        if (!name) return;

        try {
            const character = await this._fetch('/characters', {
                method: 'POST',
                body: { name }
            });
            if (character) {
                this.characters.push(character);
                this._renderCharacterSelector();
                await this._selectCharacter(character.id);
                this.hudController.addEventLogEntry(`Character created: ${name}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create character', 'danger');
        }
    }

    // ── Survival Actions ──

    async _scavenge() {
        try {
            const result = await this._fetch(`/survival/scavenge/${this.selectedCharacterId}`, {
                method: 'POST'
            });
            if (result) {
                this.hudController.addEventLogEntry(
                    `Scavenge: ${result.message || 'Found supplies!'}`,
                    result.success ? 'success' : 'warning'
                );
                if (result.items) {
                    for (const item of result.items) {
                        this.hudController.addEventLogEntry(`  Found: ${item.name || item}`, 'info');
                    }
                }
                await this._loadSurvivalStatus();
                await this._selectCharacter(this.selectedCharacterId);

                // Generate AI narrative
                const items = result.items ? result.items.map(i => i.name || i).join(', ') : 'supplies';
                this._generateNarrative(`Scavenged and found: ${items}. ${result.message || ''}`);

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        'DISCOVERY',
                        'earth2026',
                        `Scavenged and found: ${items}`,
                        result.message || '',
                        { items, character: this.currentCharacter?.name }
                    );
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Scavenge failed', 'danger');
        }
    }

    async _rest() {
        try {
            const result = await this._fetch(`/survival/rest/${this.selectedCharacterId}`, {
                method: 'POST'
            });
            if (result) {
                this.hudController.addEventLogEntry(
                    `Rest: ${result.message || 'Recovered some energy'}`,
                    'success'
                );
                await this._loadSurvivalStatus();
                await this._selectCharacter(this.selectedCharacterId);

                // Generate AI narrative
                this._generateNarrative(`Rested and recovered. ${result.message || 'Energy restored.'}`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Rest failed', 'danger');
        }
    }

    // ── Travel ──

    async _travel() {
        if (this.locations.length === 0) {
            this.hudController.addEventLogEntry('No locations available', 'warning');
            return;
        }

        const locationNames = this.locations.map((l, i) => `${i + 1}. ${l.name || l} (${l.distance || '?'} km)`).join('\n');
        const choice = prompt(`Available locations:\n${locationNames}\n\nEnter location number:`);
        if (!choice) return;

        const index = parseInt(choice) - 1;
        if (index < 0 || index >= this.locations.length) {
            this.hudController.addEventLogEntry('Invalid location', 'warning');
            return;
        }

        const target = this.locations[index];
        try {
            const result = await this._fetch(`/characters/${this.selectedCharacterId}/travel`, {
                method: 'POST',
                body: { locationId: target.id }
            });
            if (result) {
                this.hudController.addEventLogEntry(
                    `Traveled to ${target.name || target}: ${result.message || 'Arrived safely'}`,
                    'success'
                );
                await this._selectCharacter(this.selectedCharacterId);

                // Generate AI narrative
                this._generateNarrative(`Traveled to ${target.name || target}. ${result.message || 'Arrived safely.'}`);

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        'DISCOVERY',
                        'earth2026',
                        `Traveled to ${target.name || target}`,
                        result.message || 'Arrived safely.',
                        { location: target.name || target, character: this.currentCharacter?.name }
                    );
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Travel failed', 'danger');
        }
    }

    // ── Scenario ──

    async _startScenario() {
        if (!this.selectedScenarioId) {
            this.hudController.addEventLogEntry('Select a scenario first', 'warning');
            return;
        }

        try {
            const result = await this._fetch(`/scenarios/${this.selectedScenarioId}/start`, {
                method: 'POST',
                body: { characterId: this.selectedCharacterId }
            });
            if (result) {
                this.hudController.addEventLogEntry(
                    `Scenario started: ${this.currentScenario?.name || 'Unknown'}`,
                    'success'
                );
                if (result.narrative || result.prompt) {
                    this.hudController.addEventLogEntry(result.narrative || result.prompt, 'info');
                }
                this._renderScenarioNarrative();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to start scenario', 'danger');
        }
    }

    // ── Party Management ──

    async _joinParty() {
        try {
            this.parties = await this._fetch('/mmog/parties') || [];

            if (this.parties.length === 0) {
                // Offer to create a party
                const create = confirm('No active parties. Create a new party?');
                if (create) {
                    await this._createParty();
                }
                return;
            }

            const partyNames = this.parties.map((p, i) => `${i + 1}. ${p.name || p} (${p.memberCount || 0} members)`).join('\n');
            const choice = prompt(`Active parties:\n${partyNames}\n\nEnter party number to join:`);
            if (!choice) return;

            const index = parseInt(choice) - 1;
            if (index < 0 || index >= this.parties.length) return;

            const party = this.parties[index];
            const result = await this._fetch(`/mmog/parties/${party.id}/join`, {
                method: 'POST',
                body: { characterId: this.selectedCharacterId }
            });
            if (result) {
                this.currentPartyId = party.id;
                this.hudController.addEventLogEntry(`Joined party: ${party.name || party}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to join party', 'danger');
        }
    }

    async _createParty() {
        const name = prompt('Enter party name:');
        if (!name) return;

        try {
            const party = await this._fetch('/mmog/parties', {
                method: 'POST',
                body: { name, characterId: this.selectedCharacterId }
            });
            if (party) {
                this.currentPartyId = party.id;
                this.hudController.addEventLogEntry(`Party created: ${name}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create party', 'danger');
        }
    }

    async _sendPartyChat(message) {
        if (!this.currentPartyId) {
            this.hudController.addEventLogEntry('Join a party first to chat', 'warning');
            return;
        }

        try {
            await this._fetch(`/mmog/parties/${this.currentPartyId}/chat`, {
                method: 'POST',
                body: { characterId: this.selectedCharacterId, message }
            });
            this.hudController.addEventLogEntry(`You: ${message}`, 'info');
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to send message', 'danger');
        }
    }

    // ── Organization Management ──

    async _joinOrganization() {
        try {
            this.organizations = await this._fetch('/organizations') || [];

            if (this.organizations.length === 0) {
                this.hudController.addEventLogEntry('No organizations available', 'warning');
                return;
            }

            const orgNames = this.organizations.map((o, i) => `${i + 1}. ${o.name || o}`).join('\n');
            const choice = prompt(`Organizations:\n${orgNames}\n\nEnter organization number to join:`);
            if (!choice) return;

            const index = parseInt(choice) - 1;
            if (index < 0 || index >= this.organizations.length) return;

            const org = this.organizations[index];
            const result = await this._fetch(`/organizations/${org.id}/join`, {
                method: 'POST',
                body: { characterId: this.selectedCharacterId }
            });
            if (result) {
                this.hudController.addEventLogEntry(`Joined organization: ${org.name || org}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to join organization', 'danger');
        }
    }

    // ── Companion Management ──

    async _recruitCompanion() {
        try {
            this.companions = await this._fetch('/companions') || [];

            if (this.companions.length === 0) {
                this.hudController.addEventLogEntry('No companions available', 'warning');
                return;
            }

            const compNames = this.companions.map((c, i) => `${i + 1}. ${c.name || c} (${c.className || 'Unknown'})`).join('\n');
            const choice = prompt(`Available companions:\n${compNames}\n\nEnter companion number to recruit:`);
            if (!choice) return;

            const index = parseInt(choice) - 1;
            if (index < 0 || index >= this.companions.length) return;

            const comp = this.companions[index];
            const result = await this._fetch(`/companions/${comp.id}/recruit`, {
                method: 'POST',
                body: { characterId: this.selectedCharacterId }
            });
            if (result) {
                this.hudController.addEventLogEntry(`Recruited companion: ${comp.name || comp}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to recruit companion', 'danger');
        }
    }

    // ── Real Estate ──

    async _viewProperties() {
        try {
            this.properties = await this._fetch('/real-estate') || [];

            if (this.properties.length === 0) {
                this.hudController.addEventLogEntry('No properties available', 'warning');
                return;
            }

            const propList = this.properties.map((p, i) => `${i + 1}. ${p.name || p} — ${p.price || '?'} credits`).join('\n');
            this.hudController.addEventLogEntry(`Available properties:\n${propList}`, 'info');

            const choice = prompt(`Properties for sale:\n${propList}\n\nEnter property number to purchase (or cancel):`);
            if (!choice) return;

            const index = parseInt(choice) - 1;
            if (index < 0 || index >= this.properties.length) return;

            const prop = this.properties[index];
            const result = await this._fetch(`/real-estate/${prop.id}/purchase`, {
                method: 'POST',
                body: { characterId: this.selectedCharacterId }
            });
            if (result) {
                this.hudController.addEventLogEntry(`Purchased property: ${prop.name || prop}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to view properties', 'danger');
        }
    }

    // ── Abstract Method Implementations ──

    async getGameState() {
        return {
            character: this.currentCharacter,
            scenario: this.currentScenario,
            survival: this.survivalStatus,
            partyId: this.currentPartyId
        };
    }

    async getNarrativePrompt() {
        if (this.currentScenario) {
            return this.currentScenario.description || this.currentScenario.briefing || '';
        }
        return 'Select a scenario to begin your adventure.';
    }

    async getAvailableActions() {
        return [
            { id: 'scavenge', label: 'Scavenge', icon: '🔍' },
            { id: 'rest', label: 'Rest', icon: '🏕️' },
            { id: 'travel', label: 'Travel', icon: '🗺️' },
            { id: 'join-party', label: 'Join Party', icon: '👥' },
            { id: 'join-organization', label: 'Join Organization', icon: '🏢' },
            { id: 'recruit-companion', label: 'Recruit Companion', icon: '🤝' },
            { id: 'view-properties', label: 'View Properties', icon: '🏠' }
        ];
    }

    async executeAction(actionId, params = {}) {
        return this._handleAction(actionId);
    }

    // ── AI Narrative ──

    _generateNarrative(context) {
        if (!window.AiNarrative) return;
        const sessionId = 'e2026-narrative-' + (this.selectedCharacterId || 'default');
        const systemPrompt = window.AiNarrative.getSystemPrompt('earth-2026');
        const characterName = this.currentCharacter ? this.currentCharacter.name : 'Survivor';
        const location = this.currentCharacter ? this.currentCharacter.location || 'Unknown' : 'Unknown';
        const message = `Character: ${characterName}. Location: ${location}. ${context}`;

        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        const loadingEl = document.createElement('div');
        loadingEl.className = 'hud-narrative-loading';
        loadingEl.textContent = '✨ Generating narrative...';
        loadingEl.style.cssText = 'color: #16a34a; font-style: italic; margin: 10px 0;';
        promptPanel.appendChild(loadingEl);

        window.AiNarrative.streamToPanel(
            sessionId, message, systemPrompt,
            null,
            (fullText) => {
                if (loadingEl.parentNode) loadingEl.remove();
                if (fullText) {
                    const narrativeEl = document.createElement('div');
                    narrativeEl.className = 'hud-narrative-text';
                    narrativeEl.style.cssText = 'color: #86efac; margin: 10px 0; padding: 10px; border-left: 3px solid #16a34a; background: rgba(22, 163, 74, 0.1);';
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
        if (!this.selectedCharacterId) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }
        this._generateNarrative('Describe what happens next in this survival scenario.');
    }
}

// Expose globally
window.Earth2026Adapter = Earth2026Adapter;

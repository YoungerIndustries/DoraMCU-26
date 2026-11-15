/* Babylon5 Adapter — Connects HUD panels to Babylon5 REST API */

class Babylon5Adapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.characters = [];
        this.selectedCharacterId = null;
        this.currentCharacter = null;
        this.storylines = [];
        this.selectedStorylineId = null;
        this.currentStoryline = null;
        this.dialogues = [];
        this.selectedDialogueId = null;
        this.currentDialogue = null;
        this.locations = [];
        this.selectedLocationId = null;
        this.currentLocation = null;
        this.races = [];
        this.selectedRaceId = null;
        this.currentRace = null;
        this.speciesList = [];
        this.currentView = 'characters'; // characters, storylines, dialogues, locations, races
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Restore session state from localStorage
        const savedCharId = localStorage.getItem('b5-character-id');
        if (savedCharId) {
            this.selectedCharacterId = savedCharId;
        }
        const savedStorylineId = localStorage.getItem('b5-storyline-id');
        if (savedStorylineId) {
            this.selectedStorylineId = savedStorylineId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'babylon5') {
                this._onTemplateLoaded();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'babylon5') {
                this._handleAction(action);
            }
        });

        // Listen for chat input (search)
        const chatInput = document.getElementById('hudChatInput');
        if (chatInput) {
            chatInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this._handleSearch(chatInput.value);
                    chatInput.value = '';
                }
            });
        }

        console.log('[Babylon5Adapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._loadCharacters();
        this._loadStorylines();
        this._loadRaces();
    }

    destroy() {
        super.destroy();
    }

    // ── Action Handler ──

    _handleAction(action) {
        switch (action) {
            case 'action-browse-characters':
                this._switchView('characters');
                break;
            case 'action-browse-storylines':
                this._switchView('storylines');
                break;
            case 'action-browse-dialogues':
                this._switchView('dialogues');
                break;
            case 'action-browse-locations':
                this._switchView('locations');
                break;
            case 'action-browse-races':
                this._switchView('races');
                break;
            case 'action-generate-galaxy':
                this._generateGalaxy();
                break;
            case 'action-view-story-arc':
                this._viewStoryArc();
                break;
            case 'action-view-quotes':
                this._viewQuotes();
                break;
            case 'action-view-relationships':
                this._viewRelationships();
                break;
            case 'action-view-personality':
                this._viewPersonality();
                break;
        }
    }

    _handleSearch(query) {
        if (!query || !query.trim()) return;
        this.hudController.addEventLogEntry(`Searching for: "${query}"`, 'info');
        // Search characters by name
        const found = this.characters.filter(c =>
            c.name && c.name.toLowerCase().includes(query.toLowerCase())
        );
        if (found.length > 0) {
            this.hudController.addEventLogEntry(`Found ${found.length} character(s) matching "${query}"`, 'success');
            found.forEach(c => {
                this.hudController.addEventLogEntry(`  - ${c.name} (${c.species || 'Unknown'})`, 'info');
            });
        } else {
            this.hudController.addEventLogEntry(`No characters found matching "${query}"`, 'warning');
        }
    }

    _switchView(view) {
        this.currentView = view;
        this.hudController.addEventLogEntry(`Switched to ${view} view`, 'info');

        switch (view) {
            case 'characters':
                this._renderCharacterSelector();
                this._renderPromptPanel();
                break;
            case 'storylines':
                this._renderStorylineSelector();
                this._renderPromptPanel();
                break;
            case 'dialogues':
                this._renderDialogueSelector();
                this._renderPromptPanel();
                break;
            case 'locations':
                this._renderLocationSelector();
                this._renderPromptPanel();
                break;
            case 'races':
                this._renderRaceSelector();
                this._renderPromptPanel();
                break;
        }
    }

    // ── Character Management ──

    async _loadCharacters(species) {
        try {
            const url = species ? `/characters?species=${encodeURIComponent(species)}` : '/characters';
            this.characters = await this._fetch(url) || [];
            // Extract unique species list
            this.speciesList = [...new Set(this.characters.map(c => c.species).filter(s => s))];
            this._renderCharacterSelector();
            if (this.selectedCharacterId) {
                await this._selectCharacter(this.selectedCharacterId);
            } else if (this.characters.length > 0) {
                await this._selectCharacter(this.characters[0].id);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load characters', 'danger');
            this._renderCharacterSelector();
        }
    }

    async _selectCharacter(characterId) {
        try {
            this.selectedCharacterId = characterId;
            localStorage.setItem('b5-character-id', characterId);
            this.currentCharacter = await this._fetch(`/characters/${characterId}`);
            if (this.currentCharacter) {
                this._renderCharacterSheet();
                this._renderPromptPanel();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load character details', 'danger');
        }
    }

    _renderCharacterSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        let selectorHtml = `<div style="margin-bottom:10px;">
            <label style="font-size:12px;color:var(--hud-accent-primary);">Character</label>
            <select class="hud-selector" id="hudB5CharSelector">
                <option value="">Select Character...</option>
                ${this.characters.map(c => `
                    <option value="${c.id}" ${c.id === this.selectedCharacterId ? 'selected' : ''}>
                        ${c.name || 'Unknown'} (${c.species || 'Unknown'})
                    </option>
                `).join('')}
            </select>
        </div>`;

        if (this.speciesList.length > 0) {
            selectorHtml += `<div style="margin-bottom:10px;">
                <label style="font-size:12px;color:var(--hud-accent-primary);">Filter by Species</label>
                <select class="hud-selector" id="hudB5SpeciesFilter">
                    <option value="">All Species</option>
                    ${this.speciesList.map(s => `<option value="${s}">${s}</option>`).join('')}
                </select>
            </div>`;
        }

        if (this.currentCharacter) {
            selectorHtml += this._renderCharacterSheetContent();
        }

        body.innerHTML = selectorHtml;

        // Bind character selector change
        const charSelector = document.getElementById('hudB5CharSelector');
        if (charSelector) {
            charSelector.addEventListener('change', (e) => {
                if (e.target.value) {
                    this._selectCharacter(e.target.value);
                }
            });
        }

        // Bind species filter change
        const speciesFilter = document.getElementById('hudB5SpeciesFilter');
        if (speciesFilter) {
            speciesFilter.addEventListener('change', (e) => {
                const species = e.target.value;
                this._loadCharacters(species || undefined);
            });
        }
    }

    _renderCharacterSheetContent() {
        const c = this.currentCharacter;
        if (!c) return '';

        let html = `<div class="hud-character-sheet">`;
        html += `<div class="hud-character-name">${c.name || 'Unknown'}</div>`;
        html += `<div class="hud-character-role">${c.role || ''} ${c.species ? `• ${c.species}` : ''}</div>`;

        if (c.actor) {
            html += `<div style="font-size:12px;color:#94a3b8;margin-bottom:8px;">Portrayed by ${c.actor}</div>`;
        }

        // Characteristics
        if (c.characteristics) {
            const ch = c.characteristics;
            html += `<div style="margin:10px 0;">
                <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Characteristics</div>
                <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:5px;">
                    ${this._renderCharacteristicBar('ST', ch.ST || 0, 10)}
                    ${this._renderCharacteristicBar('DX', ch.DX || 0, 10)}
                    ${this._renderCharacteristicBar('EN', ch.EN || 0, 10)}
                    ${this._renderCharacteristicBar('IN', ch.IN || 0, 10)}
                    ${this._renderCharacteristicBar('ED', ch.ED || 0, 10)}
                    ${this._renderCharacteristicBar('SO', ch.SO || 0, 10)}
                </div>
            </div>`;
        }

        // Skills
        if (c.keySkills && c.keySkills.length > 0) {
            html += `<div style="margin:10px 0;">
                <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Key Skills</div>
                <div style="display:flex;flex-wrap:wrap;gap:4px;">
                    ${c.keySkills.map(s => `<span class="hud-skill-tag">${s}</span>`).join('')}
                </div>
            </div>`;
        }

        if (c.archetype) {
            html += `<div style="font-size:12px;color:#94a3b8;">Archetype: ${c.archetype}</div>`;
        }

        if (c.firstAppearance) {
            html += `<div style="font-size:11px;color:#64748b;">First: ${c.firstAppearance}</div>`;
        }

        html += `</div>`;
        return html;
    }

    _renderCharacteristicBar(label, value, max) {
        const percent = Math.min(100, Math.max(0, (value / max) * 100));
        return `<div class="hud-stat-bar" style="margin-bottom:3px;">
            <div class="hud-stat-bar-label" style="font-size:10px;">${label}</div>
            <div class="hud-stat-bar-track">
                <div class="hud-stat-bar-fill" style="width:${percent}%;background:var(--hud-accent-primary);"></div>
            </div>
            <div class="hud-stat-bar-value" style="font-size:10px;">${value}</div>
        </div>`;
    }

    _renderCharacterSheet() {
        this._renderCharacterSelector();
    }

    // ── Storyline Management ──

    async _loadStorylines() {
        try {
            this.storylines = await this._fetch('/storylines') || [];
            if (this.selectedStorylineId) {
                await this._selectStoryline(this.selectedStorylineId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load storylines', 'danger');
        }
    }

    async _selectStoryline(storylineId) {
        try {
            this.selectedStorylineId = storylineId;
            localStorage.setItem('b5-storyline-id', storylineId);
            this.currentStoryline = await this._fetch(`/storylines/${storylineId}`);
            if (this.currentStoryline) {
                this._renderPromptPanel();

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        'DISCOVERY',
                        'babylon5',
                        `Storyline selected: ${this.currentStoryline.title || 'Unknown'}`,
                        this.currentStoryline.description || '',
                        { storylineId, title: this.currentStoryline.title }
                    );
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load storyline details', 'danger');
        }
    }

    _renderStorylineSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        let html = `<div style="margin-bottom:10px;">
            <label style="font-size:12px;color:var(--hud-accent-primary);">Storyline</label>
            <select class="hud-selector" id="hudB5StorylineSelector">
                <option value="">Select Storyline...</option>
                ${this.storylines.map(s => `
                    <option value="${s.id}" ${s.id === this.selectedStorylineId ? 'selected' : ''}>
                        ${s.title || 'Unknown'}
                    </option>
                `).join('')}
            </select>
        </div>`;

        if (this.currentStoryline) {
            html += `<div class="hud-character-sheet">
                <div class="hud-character-name">${this.currentStoryline.title || 'Unknown'}</div>
                ${this.currentStoryline.description ? `<div style="font-size:12px;color:#94a3b8;margin:5px 0;">${this.currentStoryline.description}</div>` : ''}
                ${this.currentStoryline.seasons ? `<div style="font-size:12px;color:#64748b;">Seasons: ${this.currentStoryline.seasons.join(', ')}</div>` : ''}
                ${this.currentStoryline.keyCharacters && this.currentStoryline.keyCharacters.length > 0 ? `
                    <div style="margin:10px 0;">
                        <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Key Characters</div>
                        <div style="display:flex;flex-wrap:wrap;gap:4px;">
                            ${this.currentStoryline.keyCharacters.map(c => `<span class="hud-skill-tag">${c}</span>`).join('')}
                        </div>
                    </div>
                ` : ''}
            </div>`;
        }

        body.innerHTML = html;

        const selector = document.getElementById('hudB5StorylineSelector');
        if (selector) {
            selector.addEventListener('change', (e) => {
                if (e.target.value) {
                    this._selectStoryline(e.target.value);
                }
            });
        }
    }

    // ── Dialogue Management ──

    async _loadDialogues() {
        try {
            this.dialogues = await this._fetch('/dialogues') || [];
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load dialogues', 'danger');
        }
    }

    _renderDialogueSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        let html = `<div style="margin-bottom:10px;">
            <label style="font-size:12px;color:var(--hud-accent-primary);">Dialogue Scene</label>
            <select class="hud-selector" id="hudB5DialogueSelector">
                <option value="">Select Dialogue...</option>
                ${this.dialogues.map(d => `
                    <option value="${d.id}" ${d.id === this.selectedDialogueId ? 'selected' : ''}>
                        ${d.title || 'Unknown'}
                    </option>
                `).join('')}
            </select>
        </div>`;

        if (this.currentDialogue) {
            html += `<div class="hud-character-sheet">
                <div class="hud-character-name">${this.currentDialogue.title || 'Unknown'}</div>
                ${this.currentDialogue.scene ? `<div style="font-size:12px;color:#94a3b8;margin:5px 0;">Scene: ${this.currentDialogue.scene}</div>` : ''}
                ${this.currentDialogue.characters && this.currentDialogue.characters.length > 0 ? `
                    <div style="margin:10px 0;">
                        <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Characters</div>
                        <div style="display:flex;flex-wrap:wrap;gap:4px;">
                            ${this.currentDialogue.characters.map(c => `<span class="hud-skill-tag">${c}</span>`).join('')}
                        </div>
                    </div>
                ` : ''}
            </div>`;
        }

        body.innerHTML = html;

        const selector = document.getElementById('hudB5DialogueSelector');
        if (selector) {
            selector.addEventListener('change', (e) => {
                if (e.target.value) {
                    this._selectDialogue(e.target.value);
                }
            });
        }
    }

    async _selectDialogue(dialogueId) {
        try {
            this.selectedDialogueId = dialogueId;
            // Dialogues are loaded all at once, so find from the list
            this.currentDialogue = this.dialogues.find(d => d.id === dialogueId);
            if (this.currentDialogue) {
                this._renderDialogueSelector();
                this._renderPromptPanel();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load dialogue', 'danger');
        }
    }

    // ── Location Management ──

    async _loadLocations() {
        try {
            this.locations = await this._fetch('/locations') || [];
            if (this.selectedLocationId) {
                await this._selectLocation(this.selectedLocationId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load locations', 'danger');
        }
    }

    async _selectLocation(locationId) {
        try {
            this.selectedLocationId = locationId;
            this.currentLocation = await this._fetch(`/locations/${locationId}`);
            if (this.currentLocation) {
                this._renderLocationSelector();
                this._renderPromptPanel();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load location details', 'danger');
        }
    }

    _renderLocationSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        let html = `<div style="margin-bottom:10px;">
            <label style="font-size:12px;color:var(--hud-accent-primary);">Location</label>
            <select class="hud-selector" id="hudB5LocationSelector">
                <option value="">Select Location...</option>
                ${this.locations.map(l => `
                    <option value="${l.id}" ${l.id === this.selectedLocationId ? 'selected' : ''}>
                        ${l.name || 'Unknown'} (${l.type || 'Unknown'})
                    </option>
                `).join('')}
            </select>
        </div>`;

        if (this.currentLocation) {
            const l = this.currentLocation;
            html += `<div class="hud-character-sheet">
                <div class="hud-character-name">${l.name || 'Unknown'}</div>
                <div class="hud-character-role">${l.type || ''} ${l.system ? `• ${l.system}` : ''}</div>
                ${l.faction ? `<div style="font-size:12px;color:#94a3b8;margin:5px 0;">Faction: ${l.faction}</div>` : ''}
                ${l.description ? `<div style="font-size:12px;color:#cbd5e1;margin:5px 0;line-height:1.5;">${l.description}</div>` : ''}
            </div>`;
        }

        body.innerHTML = html;

        const selector = document.getElementById('hudB5LocationSelector');
        if (selector) {
            selector.addEventListener('change', (e) => {
                if (e.target.value) {
                    this._selectLocation(e.target.value);
                }
            });
        }
    }

    // ── Race Management ──

    async _loadRaces() {
        try {
            this.races = await this._fetch('/races') || [];
            if (this.selectedRaceId) {
                await this._selectRace(this.selectedRaceId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load races', 'danger');
        }
    }

    async _selectRace(raceId) {
        try {
            this.selectedRaceId = raceId;
            this.currentRace = await this._fetch(`/races/${raceId}`);
            if (this.currentRace) {
                this._renderRaceSelector();
                this._renderPromptPanel();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load race details', 'danger');
        }
    }

    _renderRaceSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        let html = `<div style="margin-bottom:10px;">
            <label style="font-size:12px;color:var(--hud-accent-primary);">Race</label>
            <select class="hud-selector" id="hudB5RaceSelector">
                <option value="">Select Race...</option>
                ${this.races.map(r => `
                    <option value="${r.id}" ${r.id === this.selectedRaceId ? 'selected' : ''}>
                        ${r.name || 'Unknown'}
                    </option>
                `).join('')}
            </select>
        </div>`;

        if (this.currentRace) {
            const r = this.currentRace;
            html += `<div class="hud-character-sheet">
                <div class="hud-character-name">${r.name || 'Unknown'}</div>
                ${r.homeworld ? `<div style="font-size:12px;color:#94a3b8;">Homeworld: ${r.homeworld}</div>` : ''}
                ${r.government ? `<div style="font-size:12px;color:#94a3b8;">Government: ${r.government}</div>` : ''}
                ${r.lifespan ? `<div style="font-size:12px;color:#94a3b8;">Lifespan: ${r.lifespan} years</div>` : ''}
                ${r.combatStyle ? `<div style="font-size:12px;color:#94a3b8;">Combat: ${r.combatStyle}</div>` : ''}
            </div>`;

            // Characteristics
            if (r.characteristics) {
                const ch = r.characteristics;
                html += `<div style="margin:10px 0;">
                    <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Base Characteristics</div>
                    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:5px;">
                        ${this._renderCharacteristicBar('ST', ch.ST || 0, 10)}
                        ${this._renderCharacteristicBar('DX', ch.DX || 0, 10)}
                        ${this._renderCharacteristicBar('EN', ch.EN || 0, 10)}
                        ${this._renderCharacteristicBar('IN', ch.IN || 0, 10)}
                        ${this._renderCharacteristicBar('ED', ch.ED || 0, 10)}
                        ${this._renderCharacteristicBar('SO', ch.SO || 0, 10)}
                    </div>
                </div>`;
            }

            // Special Abilities
            if (r.specialAbilities && r.specialAbilities.length > 0) {
                html += `<div style="margin:10px 0;">
                    <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Special Abilities</div>
                    <div style="display:flex;flex-wrap:wrap;gap:4px;">
                        ${r.specialAbilities.map(a => `<span class="hud-skill-tag">${a}</span>`).join('')}
                    </div>
                </div>`;
            }

            // Modifiers
            html += `<div style="margin:10px 0;font-size:11px;color:#64748b;">
                Trade Bonus: ${r.tradeBonus || 0} | Social DM: ${r.socialDM || 0} | Nav DM: ${r.navigationDM || 0} | Armor Bonus: ${r.armorBonus || 0}
            </div>`;
        }

        body.innerHTML = html;

        const selector = document.getElementById('hudB5RaceSelector');
        if (selector) {
            selector.addEventListener('change', (e) => {
                if (e.target.value) {
                    this._selectRace(e.target.value);
                }
            });
        }
    }

    // ── Galaxy Generation ──

    async _generateGalaxy() {
        try {
            this.hudController.addEventLogEntry('Generating Babylon 5 galaxy...', 'info');
            const result = await this._fetch('/galaxy/generate', { method: 'POST', body: {} });
            if (result) {
                this.hudController.addEventLogEntry(`Galaxy "${result.galaxyName}" created with ${result.empireCount} empires`, 'success');
                if (result.empires && result.empires.length > 0) {
                    result.empires.forEach(empire => {
                        this.hudController.addEventLogEntry(`  - ${empire.name}: ${empire.planetCount} planets`, 'info');
                    });
                }
                this._renderGalaxyResult(result);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to generate galaxy: ' + err.message, 'danger');
        }
    }

    _renderGalaxyResult(result) {
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        let html = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">🌌 Galaxy Generated</h3>
            <p><strong>Galaxy:</strong> ${result.galaxyName || 'Unknown'}</p>
            <p><strong>Galaxy ID:</strong> ${result.galaxyId || 'Unknown'}</p>
            <p><strong>Empires:</strong> ${result.empireCount || 0}</p>
        </div>`;

        if (result.empires && result.empires.length > 0) {
            html += `<div style="margin:10px 0;">`;
            result.empires.forEach(empire => {
                html += `<div style="margin:5px 0;padding:8px;background:rgba(59,130,246,0.1);border-left:3px solid var(--hud-accent-primary);">
                    <strong>${empire.name}</strong> — ${empire.planetCount} planets
                    ${empire.capitalPlanetId ? ` (Capital: ${empire.capitalPlanetId})` : ''}
                </div>`;
            });
            html += `</div>`;
        }

        promptBody.innerHTML = html;
    }

    // ── Character Detail Views ──

    _viewStoryArc() {
        if (!this.currentCharacter || !this.currentCharacter.storyArc) {
            this.hudController.addEventLogEntry('No story arc available for this character', 'warning');
            return;
        }
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        promptBody.innerHTML = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">📋 Story Arc: ${this.currentCharacter.name}</h3>
            <p style="line-height:1.6;">${this.currentCharacter.storyArc}</p>
        </div>`;
    }

    _viewQuotes() {
        if (!this.currentCharacter || !this.currentCharacter.famousQuotes || this.currentCharacter.famousQuotes.length === 0) {
            this.hudController.addEventLogEntry('No famous quotes available for this character', 'warning');
            return;
        }
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        let html = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">🗣️ Famous Quotes: ${this.currentCharacter.name}</h3>`;
        this.currentCharacter.famousQuotes.forEach((quote, i) => {
            html += `<div style="margin:8px 0;padding:8px;background:rgba(59,130,246,0.1);border-left:3px solid var(--hud-accent-primary);font-style:italic;">
                "${quote}"
            </div>`;
        });
        html += `</div>`;
        promptBody.innerHTML = html;
    }

    _viewRelationships() {
        if (!this.currentCharacter || !this.currentCharacter.relationships || this.currentCharacter.relationships.length === 0) {
            this.hudController.addEventLogEntry('No relationships available for this character', 'warning');
            return;
        }
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        let html = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">🔗 Relationships: ${this.currentCharacter.name}</h3>`;
        this.currentCharacter.relationships.forEach(rel => {
            html += `<div style="margin:5px 0;padding:8px;background:rgba(59,130,246,0.1);border-left:3px solid var(--hud-accent-primary);">
                <strong>${rel.character || 'Unknown'}</strong>: ${rel.type || 'Unknown'} — ${rel.description || ''}
            </div>`;
        });
        html += `</div>`;
        promptBody.innerHTML = html;
    }

    _viewPersonality() {
        if (!this.currentCharacter || !this.currentCharacter.personality) {
            this.hudController.addEventLogEntry('No personality profile available for this character', 'warning');
            return;
        }
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        const p = this.currentCharacter.personality;
        let html = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">📊 Personality: ${this.currentCharacter.name}</h3>`;
        if (p.description) {
            html += `<p style="line-height:1.6;">${p.description}</p>`;
        }
        if (p.traits && p.traits.length > 0) {
            html += `<div style="margin:10px 0;">
                <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Traits</div>
                <div style="display:flex;flex-wrap:wrap;gap:4px;">
                    ${p.traits.map(t => `<span class="hud-skill-tag">${t}</span>`).join('')}
                </div>
            </div>`;
        }
        html += `</div>`;
        promptBody.innerHTML = html;
    }

    // ── Prompt Panel Rendering ──

    _renderPromptPanel() {
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        switch (this.currentView) {
            case 'storylines':
                this._renderStorylineNarrative();
                break;
            case 'dialogues':
                this._renderDialogueNarrative();
                break;
            case 'locations':
                this._renderLocationNarrative();
                break;
            case 'races':
                this._renderRaceNarrative();
                break;
            default:
                this._renderCharacterNarrative();
        }
    }

    _renderCharacterNarrative() {
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        if (!this.currentCharacter) {
            promptBody.innerHTML = `<div class="hud-narrative-text">
                <p>Select a character to explore the Babylon 5 universe...</p>
            </div>`;
            return;
        }

        const c = this.currentCharacter;
        let html = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">${c.name}</h3>
            ${c.role ? `<p style="color:#94a3b8;font-size:13px;">${c.role}</p>` : ''}
            ${c.description ? `<p style="line-height:1.6;margin:10px 0;">${c.description}</p>` : ''}
        </div>`;

        promptBody.innerHTML = html;
    }

    _renderStorylineNarrative() {
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        if (!this.currentStoryline) {
            promptBody.innerHTML = `<div class="hud-narrative-text">
                <p>Select a storyline to explore...</p>
            </div>`;
            return;
        }

        const s = this.currentStoryline;
        let html = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">${s.title}</h3>
            ${s.description ? `<p style="line-height:1.6;margin:10px 0;">${s.description}</p>` : ''}
        </div>`;

        if (s.chapters && s.chapters.length > 0) {
            html += `<div style="margin:15px 0;">`;
            s.chapters.forEach(chapter => {
                html += `<div style="margin:10px 0;padding:10px;background:rgba(59,130,246,0.1);border-left:3px solid var(--hud-accent-primary);">
                    <div style="font-size:12px;color:var(--hud-accent-primary);">Season ${chapter.season}</div>
                    <div style="font-weight:bold;margin:5px 0;">${chapter.title}</div>
                    ${chapter.description ? `<div style="font-size:12px;color:#cbd5e1;line-height:1.5;margin:5px 0;">${chapter.description}</div>` : ''}
                    ${chapter.keyEvents && chapter.keyEvents.length > 0 ? `
                        <div style="margin:5px 0;">
                            <div style="font-size:11px;color:#94a3b8;">Key Events:</div>
                            <ul style="margin:5px 0;padding-left:20px;font-size:12px;color:#cbd5e1;">
                                ${chapter.keyEvents.map(e => `<li>${e}</li>`).join('')}
                            </ul>
                        </div>
                    ` : ''}
                </div>`;
            });
            html += `</div>`;
        }

        promptBody.innerHTML = html;
    }

    _renderDialogueNarrative() {
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        if (!this.currentDialogue) {
            promptBody.innerHTML = `<div class="hud-narrative-text">
                <p>Select a dialogue scene to view...</p>
            </div>`;
            return;
        }

        const d = this.currentDialogue;
        let html = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">${d.title}</h3>
            ${d.scene ? `<p style="color:#94a3b8;font-size:13px;">Scene: ${d.scene}</p>` : ''}
        </div>`;

        if (d.lines && d.lines.length > 0) {
            html += `<div style="margin:15px 0;">`;
            d.lines.forEach((line, idx) => {
                const speakerColor = line.character === d.lines[0]?.character ? '#60a5fa' : '#f472b6';
                html += `<div style="margin:8px 0;padding:8px;background:rgba(59,130,246,0.05);border-radius:4px;">
                    <strong style="color:${speakerColor};font-size:12px;">${line.character || 'Unknown'}:</strong>
                    <span style="color:#cbd5e1;font-size:13px;"> ${line.text || ''}</span>
                    <button class="hud-speak-button" data-character="${line.character || ''}" data-text="${(line.text || '').replace(/"/g, '&quot;')}" data-index="${idx}" title="Speak this line">🔊</button>
                </div>`;
            });
            html += `</div>`;
        }

        promptBody.innerHTML = html;

        // Wire up speak buttons
        promptBody.querySelectorAll('.hud-speak-button').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const character = btn.dataset.character;
                const text = btn.dataset.text;
                this._speakDialogueLine(character, text);
            });
        });
    }

    /**
     * Speak a dialogue line using the character's voice profile.
     * Looks up the character ID from the server and calls voiceService.speakAsCharacter.
     */
    async _speakDialogueLine(characterName, text) {
        if (!window.voiceService) {
            console.warn('[Babylon5] Voice service not available');
            return;
        }

        // Check if character TTS is enabled
        const settings = window.voiceService.settings || {};
        if (settings.enableCharacterTts === false) {
            console.log('[Babylon5] Character TTS is disabled');
            return;
        }

        try {
            // Look up the character ID by name
            const characters = await this._getCharacterMap();
            const characterId = characters.get(characterName);

            if (characterId) {
                await window.voiceService.speakAsCharacter(characterId, text);
            } else {
                // Fallback: speak with default profile
                console.log('[Babylon5] Character not found, using default voice for:', characterName);
                await window.voiceService.synthesize({ text });
            }
        } catch (error) {
            console.error('[Babylon5] Failed to speak dialogue line:', error.message);
        }
    }

    /**
     * Get a map of character names to IDs from the server.
     */
    async _getCharacterMap() {
        if (this._characterMap) return this._characterMap;

        try {
            const vs = window.voiceService;
            if (vs && vs.getCharactersWithVoices) {
                const result = await vs.getCharactersWithVoices();
                if (result.success && result.characters) {
                    this._characterMap = new Map();
                    result.characters.forEach(c => {
                        this._characterMap.set(c.name, c.id);
                    });
                }
            }
        } catch (error) {
            console.warn('[Babylon5] Failed to load character map:', error.message);
        }

        if (!this._characterMap) {
            this._characterMap = new Map();
        }
        return this._characterMap;
    }

    _renderLocationNarrative() {
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        if (!this.currentLocation) {
            promptBody.innerHTML = `<div class="hud-narrative-text">
                <p>Select a location to explore...</p>
            </div>`;
            return;
        }

        const l = this.currentLocation;
        promptBody.innerHTML = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">${l.name}</h3>
            <p style="color:#94a3b8;font-size:13px;">${l.type || ''} ${l.system ? `• ${l.system}` : ''} ${l.faction ? `• ${l.faction}` : ''}</p>
            ${l.description ? `<p style="line-height:1.6;margin:10px 0;">${l.description}</p>` : ''}
        </div>`;
    }

    _renderRaceNarrative() {
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (!promptBody) return;

        if (!this.currentRace) {
            promptBody.innerHTML = `<div class="hud-narrative-text">
                <p>Select a race to learn about...</p>
            </div>`;
            return;
        }

        const r = this.currentRace;
        let html = `<div class="hud-narrative-text">
            <h3 style="color:var(--hud-accent-primary);margin:0 0 10px 0;">${r.name}</h3>
            ${r.homeworld ? `<p style="color:#94a3b8;font-size:13px;">Homeworld: ${r.homeworld}</p>` : ''}
            ${r.government ? `<p style="color:#94a3b8;font-size:13px;">Government: ${r.government}</p>` : ''}
            ${r.lifespan ? `<p style="color:#94a3b8;font-size:13px;">Lifespan: ${r.lifespan} years</p>` : ''}
            ${r.description ? `<p style="line-height:1.6;margin:10px 0;">${r.description}</p>` : ''}
        </div>`;

        if (r.specialAbilities && r.specialAbilities.length > 0) {
            html += `<div style="margin:10px 0;">
                <div style="font-size:12px;color:var(--hud-accent-primary);margin-bottom:5px;">Special Abilities</div>
                <div style="display:flex;flex-wrap:wrap;gap:4px;">
                    ${r.specialAbilities.map(a => `<span class="hud-skill-tag">${a}</span>`).join('')}
                </div>
            </div>`;
        }

        promptBody.innerHTML = html;
    }

    // ── Public API ──

    getGameState() {
        return {
            characters: this.characters,
            selectedCharacter: this.currentCharacter,
            storylines: this.storylines,
            selectedStoryline: this.currentStoryline,
            dialogues: this.dialogues,
            locations: this.locations,
            races: this.races,
            currentView: this.currentView
        };
    }

    getNarrativePrompt() {
        if (this.currentCharacter) {
            return {
                title: this.currentCharacter.name,
                text: this.currentCharacter.description || '',
                type: 'character'
            };
        }
        if (this.currentStoryline) {
            return {
                title: this.currentStoryline.title,
                text: this.currentStoryline.description || '',
                type: 'storyline'
            };
        }
        return {
            title: 'Babylon 5 Universe',
            text: 'Select a character, storyline, or location to explore...',
            type: 'intro'
        };
    }

    getAvailableActions() {
        return [
            { id: 'action-browse-characters', label: 'Browse Characters' },
            { id: 'action-browse-storylines', label: 'Browse Storylines' },
            { id: 'action-browse-dialogues', label: 'Browse Dialogues' },
            { id: 'action-browse-locations', label: 'Browse Locations' },
            { id: 'action-browse-races', label: 'Browse Races' },
            { id: 'action-generate-galaxy', label: 'Generate Galaxy' }
        ];
    }

    async executeAction(actionId, params) {
        this._handleAction(actionId);
        return { success: true };
    }

    // ── AI Narrative ──

    _generateNarrative(context) {
        if (!window.AiNarrative) return;
        const sessionId = 'b5-narrative-' + (this.selectedCharacterId || 'default');
        const systemPrompt = window.AiNarrative.getSystemPrompt('babylon5');
        const characterName = this.currentCharacter ? this.currentCharacter.name : 'Explorer';
        const message = `Character: ${characterName}. ${this.currentStoryline ? 'Storyline: ' + this.currentStoryline.title + '.' : ''} ${context}`;

        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        const loadingEl = document.createElement('div');
        loadingEl.className = 'hud-narrative-loading';
        loadingEl.textContent = '✨ Generating narrative...';
        loadingEl.style.cssText = 'color: #3b82f6; font-style: italic; margin: 10px 0;';
        promptPanel.appendChild(loadingEl);

        window.AiNarrative.streamToPanel(
            sessionId, message, systemPrompt,
            null,
            (fullText) => {
                if (loadingEl.parentNode) loadingEl.remove();
                if (fullText) {
                    const narrativeEl = document.createElement('div');
                    narrativeEl.className = 'hud-narrative-text';
                    narrativeEl.style.cssText = 'color: #93c5fd; margin: 10px 0; padding: 10px; border-left: 3px solid #3b82f6; background: rgba(59, 130, 246, 0.1);';
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
        this._generateNarrative('Continue the Babylon5 narrative. Describe what happens next.');
    }
}

// Expose globally
window.Babylon5Adapter = Babylon5Adapter;

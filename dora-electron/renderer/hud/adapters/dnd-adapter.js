/* D&D 5th Edition Adapter — Connects HUD panels to D&D 5e REST API */

class Dnd5eAdapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.characters = [];
        this.selectedCharacterId = null;
        this.currentCharacter = null;
        this.campaigns = [];
        this.selectedCampaignId = null;
        this.currentCampaign = null;
        this.combats = [];
        this.selectedCombatId = null;
        this.currentCombat = null;
        this.spells = [];
        this.currentSpells = [];
        this.combatLog = [];
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Restore session state from localStorage
        const savedCharId = localStorage.getItem('dnd-character-id');
        if (savedCharId) {
            this.selectedCharacterId = savedCharId;
        }
        const savedCampaignId = localStorage.getItem('dnd-campaign-id');
        if (savedCampaignId) {
            this.selectedCampaignId = savedCampaignId;
        }
        const savedCombatId = localStorage.getItem('dnd-combat-id');
        if (savedCombatId) {
            this.selectedCombatId = savedCombatId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'dnd-5e') {
                this._onTemplateLoaded();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'dnd-5e') {
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

        // Listen for campaign selector change
        const campaignSelector = document.getElementById('hudCampaignSelector');
        if (campaignSelector) {
            campaignSelector.addEventListener('change', (e) => {
                this._selectCampaign(e.target.value);
            });
        }

        // Listen for combat selector change
        const combatSelector = document.getElementById('hudCombatSelector');
        if (combatSelector) {
            combatSelector.addEventListener('change', (e) => {
                this._selectCombat(e.target.value);
            });
        }

        // Listen for chat input
        const chatInput = document.getElementById('hudChatInput');
        if (chatInput) {
            chatInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this._handleChat(chatInput.value);
                    chatInput.value = '';
                }
            });
        }

        console.log('[Dnd5eAdapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._loadCharacters();
        this._loadCampaigns();
        this._loadSpells();
    }

    destroy() {
        super.destroy();
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
            this._renderCharacterSelector();
        }
    }

    _renderCharacterSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        let selectorHtml = `<select class="hud-selector" id="hudCharSelector">
            <option value="">Select Character...</option>
            ${this.characters.map(c => `
                <option value="${c.id}" ${c.id === this.selectedCharacterId ? 'selected' : ''}>
                    ${c.name || 'Unnamed'} (${c.race || '?'} ${c.characterClass || '?'}) Lv.${c.level || 1}
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
        localStorage.setItem('dnd-character-id', characterId);

        try {
            this.currentCharacter = await this._fetch(`/characters/${characterId}`);
            if (this.currentCharacter) {
                this._renderCharacterSheet();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load character details', 'danger');
        }
    }

    _renderCharacterSheet() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body || !this.currentCharacter) return;

        const c = this.currentCharacter;

        // 6 ability scores with modifiers
        const abilities = [
            { name: 'STR', label: 'Strength', score: c.strScore || 10, mod: c.strMod || 0 },
            { name: 'DEX', label: 'Dexterity', score: c.dexScore || 10, mod: c.dexMod || 0 },
            { name: 'CON', label: 'Constitution', score: c.conScore || 10, mod: c.conMod || 0 },
            { name: 'INT', label: 'Intelligence', score: c.intScore || 10, mod: c.intMod || 0 },
            { name: 'WIS', label: 'Wisdom', score: c.wisScore || 10, mod: c.wisMod || 0 },
            { name: 'CHA', label: 'Charisma', score: c.chaScore || 10, mod: c.chaMod || 0 }
        ];

        // HP as stat bar
        const maxHp = c.maxHitPoints || 1;
        const hpPercent = Math.round((c.hitPoints || 0) / maxHp * 100);

        // Temp HP
        const tempHp = c.tempHitPoints || 0;

        let sheetHtml = `
        <div class="hud-character-sheet">
            <div class="char-name">${c.name || 'Unnamed'}</div>
            <div class="char-meta">${c.race || '?'} ${c.characterClass || '?'} Level ${c.level || 1}</div>
            ${c.background ? `<div class="char-meta" style="font-size:11px;">${c.background}</div>` : ''}

            <div style="display:flex;gap:8px;margin:8px 0;">
                <div style="flex:1;text-align:center;padding:4px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:4px;">
                    <div style="font-size:10px;color:var(--hud-text-muted);text-transform:uppercase;">AC</div>
                    <div style="font-size:18px;font-weight:700;">${c.armorClass || 10}</div>
                </div>
                <div style="flex:1;text-align:center;padding:4px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:4px;">
                    <div style="font-size:10px;color:var(--hud-text-muted);text-transform:uppercase;">Init</div>
                    <div style="font-size:18px;font-weight:700;">${c.dexMod ? (c.dexMod >= 0 ? '+' : '') + c.dexMod : 0}</div>
                </div>
                <div style="flex:1;text-align:center;padding:4px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:4px;">
                    <div style="font-size:10px;color:var(--hud-text-muted);text-transform:uppercase;">Speed</div>
                    <div style="font-size:18px;font-weight:700;">${c.speed || 30}<span style="font-size:10px;">ft</span></div>
                </div>
                <div style="flex:1;text-align:center;padding:4px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:4px;">
                    <div style="font-size:10px;color:var(--hud-text-muted);text-transform:uppercase;">Prof</div>
                    <div style="font-size:18px;font-weight:700;">+${c.proficiencyBonus || 2}</div>
                </div>
            </div>

            <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin:8px 0 4px;">Ability Scores</div>
            <div class="ability-grid">
                ${abilities.map(a => `
                    <div class="ability-score">
                        <div class="ability-name">${a.name}</div>
                        <div class="ability-value">${a.score}</div>
                        <div style="font-size:10px;color:var(--hud-text-muted);">${a.mod >= 0 ? '+' : ''}${a.mod}</div>
                    </div>
                `).join('')}
            </div>

            <div class="hud-stat-bar" style="margin-top:8px;">
                <div class="hud-stat-bar-label">
                    <span>HP</span>
                    <span class="stat-value">${c.hitPoints || 0} / ${maxHp}</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill ${hpPercent > 50 ? 'success' : hpPercent > 25 ? 'warning' : 'danger'}"
                         style="width: ${hpPercent}%"></div>
                </div>
            </div>

            ${tempHp > 0 ? `
                <div class="hud-stat-bar" style="margin-top:4px;">
                    <div class="hud-stat-bar-label">
                        <span>Temp HP</span>
                        <span class="stat-value">${tempHp}</span>
                    </div>
                    <div class="hud-stat-bar-track">
                        <div class="hud-stat-bar-fill info" style="width: ${Math.min(100, tempHp / maxHp * 100)}%"></div>
                    </div>
                </div>
            ` : ''}

            ${c.exhaustionLevel > 0 ? `
                <div style="margin-top:6px;padding:4px 8px;background:rgba(220,38,38,0.2);border:1px solid #dc2626;border-radius:4px;font-size:12px;color:#fca5a5;">
                    ⚠️ Exhaustion Level ${c.exhaustionLevel}
                </div>
            ` : ''}

            ${c.conditions ? `
                <div style="margin-top:6px;padding:4px 8px;background:rgba(251,191,36,0.2);border:1px solid #f59e0b;border-radius:4px;font-size:12px;color:#fde68a;">
                    🎭 ${c.conditions}
                </div>
            ` : ''}

            ${c.inspiration ? `
                <div style="margin-top:6px;padding:4px 8px;background:rgba(168,85,247,0.2);border:1px solid #a855f7;border-radius:4px;font-size:12px;color:#e9d5ff;">
                    ✨ Inspiration
                </div>
            ` : ''}
        </div>`;

        // Spell slots
        if (c.spellSlots && c.spellSlots.length > 0) {
            sheetHtml += `
            <div style="margin-top:10px;">
                <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin-bottom:4px;">Spell Slots</div>
                <div style="display:flex;flex-wrap:wrap;gap:4px;">
                    ${c.spellSlots.map(slot => `
                        <div style="padding:2px 6px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:3px;font-size:11px;">
                            Lv.${slot.level}: ${slot.remaining || 0}/${slot.max || 0}
                        </div>
                    `).join('')}
                </div>
            </div>`;
        }

        // Hit Dice
        if (c.hitDice) {
            sheetHtml += `
            <div style="margin-top:6px;font-size:11px;color:var(--hud-text-muted);">
                🎲 Hit Dice: ${c.hitDice}
            </div>`;
        }

        // XP
        sheetHtml += `
        <div style="margin-top:8px;font-size:11px;color:var(--hud-text-muted);">
            <div>⭐ XP: ${c.experiencePoints || 0}</div>
        </div>`;

        // Replace the character sheet (keep selector and create button)
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

    // ── Campaign Management ──

    async _loadCampaigns() {
        try {
            this.campaigns = await this._fetch('/campaigns') || [];
            this._renderCampaignSelector();
            if (this.selectedCampaignId) {
                await this._selectCampaign(this.selectedCampaignId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load campaigns', 'danger');
            this._renderCampaignSelector();
        }
    }

    _renderCampaignSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        const campaignSelectorHtml = `<select class="hud-selector" id="hudCampaignSelector" style="margin-top:10px;">
            <option value="">Select Campaign...</option>
            ${this.campaigns.map(c => `
                <option value="${c.id}" ${c.id === this.selectedCampaignId ? 'selected' : ''}>
                    ${c.name || 'Unnamed'} (${c.status || 'ACTIVE'})
                </option>
            `).join('')}
        </select>`;

        const createCampaignBtnHtml = `<button class="hud-action-button" data-action="create-campaign" style="margin-top:6px;margin-bottom:10px;">
            <span class="action-icon">➕</span>
            <span class="action-label">New Campaign</span>
        </button>`;

        const existing = body.querySelector('#hudCampaignSelector');
        if (existing) {
            existing.outerHTML = campaignSelectorHtml;
        } else {
            body.insertAdjacentHTML('beforeend', campaignSelectorHtml + createCampaignBtnHtml);
        }

        // Re-bind
        const newSelector = document.getElementById('hudCampaignSelector');
        if (newSelector) {
            newSelector.addEventListener('change', (e) => {
                this._selectCampaign(e.target.value);
            });
        }
    }

    async _selectCampaign(campaignId) {
        if (!campaignId) return;
        this.selectedCampaignId = campaignId;
        localStorage.setItem('dnd-campaign-id', campaignId);

        try {
            this.currentCampaign = await this._fetch(`/campaigns/${campaignId}`);
            if (this.currentCampaign) {
                this._renderCampaignInfo();
                await this._loadCombats();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load campaign details', 'danger');
        }
    }

    _renderCampaignInfo() {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body || !this.currentCampaign) return;

        const c = this.currentCampaign;

        let narrativeHtml = `
        <div class="hud-narrative-text">
            <div style="font-size:14px;font-weight:600;margin-bottom:8px;">📜 ${c.name || 'Unnamed Campaign'}</div>
            ${c.description ? `<div style="font-size:12px;line-height:1.6;margin-bottom:8px;">${c.description}</div>` : ''}
            <div style="font-size:11px;color:var(--hud-text-muted);">
                <div>Status: ${c.status || 'ACTIVE'}</div>
                ${c.currentLevel ? `<div>Current Level: ${c.currentLevel}</div>` : ''}
                ${c.settings ? `<div>Settings: ${c.settings}</div>` : ''}
            </div>
        </div>`;

        body.innerHTML = narrativeHtml;
    }

    // ── Combat Management ──

    async _loadCombats() {
        if (!this.selectedCampaignId) return;
        try {
            // Load combats from campaign
            const campaign = await this._fetch(`/campaigns/${this.selectedCampaignId}`);
            this.combats = campaign.combats || [];
            this._renderCombatSelector();
            if (this.selectedCombatId) {
                await this._selectCombat(this.selectedCombatId);
            }
        } catch (err) {
            this._renderCombatSelector();
        }
    }

    _renderCombatSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        const combatSelectorHtml = `<select class="hud-selector" id="hudCombatSelector" style="margin-top:10px;">
            <option value="">Select Combat...</option>
            ${this.combats.map(c => `
                <option value="${c.id}" ${c.id === this.selectedCombatId ? 'selected' : ''}>
                    ${c.encounterName || 'Unnamed'} (${c.status || 'ACTIVE'})
                </option>
            `).join('')}
        </select>`;

        const createCombatBtnHtml = `<button class="hud-action-button" data-action="create-combat" style="margin-top:6px;margin-bottom:10px;">
            <span class="action-icon">⚔️</span>
            <span class="action-label">New Encounter</span>
        </button>`;

        const existing = body.querySelector('#hudCombatSelector');
        if (existing) {
            existing.outerHTML = combatSelectorHtml;
        } else {
            body.insertAdjacentHTML('beforeend', combatSelectorHtml + createCombatBtnHtml);
        }

        // Re-bind
        const newSelector = document.getElementById('hudCombatSelector');
        if (newSelector) {
            newSelector.addEventListener('change', (e) => {
                this._selectCombat(e.target.value);
            });
        }
    }

    async _selectCombat(combatId) {
        if (!combatId) return;
        this.selectedCombatId = combatId;
        localStorage.setItem('dnd-combat-id', combatId);

        try {
            this.currentCombat = await this._fetch(`/combat/${combatId}`);
            if (this.currentCombat) {
                this._renderCombatInfo();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load combat details', 'danger');
        }
    }

    _renderCombatInfo() {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body || !this.currentCombat) return;

        const c = this.currentCombat;

        let combatHtml = `
        <div class="hud-narrative-text">
            <div style="font-size:14px;font-weight:600;margin-bottom:8px;">⚔️ ${c.encounterName || 'Unnamed Encounter'}</div>
            <div style="font-size:11px;color:var(--hud-text-muted);margin-bottom:8px;">
                <div>Round: ${c.currentRound || 1} | Status: ${c.status || 'ACTIVE'}</div>
            </div>
            <div style="font-size:12px;font-weight:600;margin-bottom:4px;">Combatants:</div>`;

        if (c.combatants && c.combatants.length > 0) {
            c.combatants.forEach(combatant => {
                const hpPercent = combatant.maxHp > 0 ? Math.round(combatant.currentHp / combatant.maxHp * 100) : 0;
                const isDead = combatant.currentHp <= 0;
                combatHtml += `
                <div style="padding:4px 8px;margin:4px 0;background:${isDead ? 'rgba(220,38,38,0.2)' : 'var(--hud-accent-glow)'};border:1px solid ${isDead ? '#dc2626' : 'var(--hud-accent-primary)'};border-radius:4px;">
                    <div style="font-size:12px;font-weight:600;">${combatant.name} ${combatant.isCharacter ? '(PC)' : '(NPC)'}</div>
                    <div style="font-size:11px;">HP: ${combatant.currentHp}/${combatant.maxHp} (${hpPercent}%) | AC: ${combatant.ac || '?'}</div>
                    ${combatant.conditions && combatant.conditions.length > 0 ? `<div style="font-size:10px;color:#fde68a;">${combatant.conditions.join(', ')}</div>` : ''}
                </div>`;
            });
        } else {
            combatHtml += `<div style="font-size:11px;color:var(--hud-text-muted);">No combatants yet</div>`;
        }

        combatHtml += `</div>`;
        body.innerHTML = combatHtml;
    }

    // ── Spell Management ──

    async _loadSpells() {
        try {
            this.spells = await this._fetch('/spells') || [];
        } catch (err) {
            // Spells may not be available
        }
    }

    // ── Dice Rolling ──

    async _rollDice(formula, mode = 'normal') {
        try {
            let endpoint;
            switch (mode) {
                case 'advantage':
                    endpoint = `/dice/roll/advantage`;
                    break;
                case 'disadvantage':
                    endpoint = `/dice/roll/disadvantage`;
                    break;
                default:
                    endpoint = `/dice/roll`;
            }

            const result = await this._fetch(endpoint, 'POST', { formula: formula });

            if (result) {
                const total = result.total || result.results?.reduce((a, b) => a + b, 0) || 0;
                const diceResults = result.results || [total];

                this.hudController.addDiceResult({
                    formula: formula,
                    rolls: diceResults,
                    total: total,
                    mode: mode
                });

                this._addCombatLog(`🎲 Rolled ${formula} (${mode}): ${diceResults.join(', ')} = ${total}`);
                return result;
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Dice roll failed', 'danger');
        }
        return null;
    }

    // ── Action Handlers ──

    async _handleAction(action) {
        switch (action) {
            case 'create-character':
                await this._createCharacter();
                break;
            case 'create-campaign':
                await this._createCampaign();
                break;
            case 'create-combat':
                await this._createCombat();
                break;
            case 'action-attack':
                await this._makeAttack();
                break;
            case 'action-cast-spell':
                await this._castSpell();
                break;
            case 'action-roll-d20':
                await this._rollDice('d20', 'normal');
                break;
            case 'action-roll-advantage':
                await this._rollDice('d20', 'advantage');
                break;
            case 'action-roll-disadvantage':
                await this._rollDice('d20', 'disadvantage');
                break;
            case 'action-heal':
                await this._healCombatant();
                break;
            case 'action-dash':
                this._addCombatLog('🏃 Character dashes (doubles movement)');
                this.hudController.addEventLogEntry('Dashed!', 'info');
                break;
            case 'action-dodge':
                this._addCombatLog('🛡️ Character dodges (attack rolls have disadvantage)');
                this.hudController.addEventLogEntry('Dodged!', 'info');
                break;
            case 'action-help':
                this._addCombatLog('🤝 Character helps an ally (advantage on next attack)');
                this.hudController.addEventLogEntry('Helped ally!', 'info');
                break;
            case 'action-skill-check':
                await this._skillCheck();
                break;
            case 'action-saving-throw':
                await this._savingThrow();
                break;
            case 'action-end-round':
                await this._endRound();
                break;
            default:
                console.log('[Dnd5eAdapter] Unknown action:', action);
        }
    }

    // ── Character Creation ──

    async _createCharacter() {
        const name = prompt('Character name:');
        if (!name) return;

        const race = prompt('Race (Human, Elf, Dwarf, Halfling, Gnome, Half-Orc, Tiefling):', 'Human');
        const characterClass = prompt('Class (Fighter, Wizard, Rogue, Cleric, Ranger, Paladin, Bard, Druid, Monk, Warlock, Barbarian, Sorcerer):', 'Fighter');

        try {
            const character = await this._fetch('/characters', 'POST', {
                name: name,
                race: race || 'Human',
                characterClass: characterClass || 'Fighter',
                level: 1,
                scoreMethod: 'standard'
            });

            if (character) {
                this.characters.push(character);
                this._renderCharacterSelector();
                await this._selectCharacter(character.id);
                this.hudController.addEventLogEntry(`Character ${name} created!`, 'success');
                this._addCombatLog(`✨ New character created: ${name} (${race} ${characterClass})`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create character', 'danger');
        }
    }

    // ── Campaign Creation ──

    async _createCampaign() {
        const name = prompt('Campaign name:');
        if (!name) return;

        const description = prompt('Campaign description:', '');

        try {
            const campaign = await this._fetch('/campaigns', 'POST', {
                name: name,
                description: description || '',
                status: 'ACTIVE'
            });

            if (campaign) {
                this.campaigns.push(campaign);
                this._renderCampaignSelector();
                await this._selectCampaign(campaign.id);
                this.hudController.addEventLogEntry(`Campaign ${name} created!`, 'success');
                this._addCombatLog(`📜 New campaign created: ${name}`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create campaign', 'danger');
        }
    }

    // ── Combat Creation ──

    async _createCombat() {
        const name = prompt('Encounter name:', 'Combat Encounter');
        if (!name) return;

        try {
            const combat = await this._fetch('/combat', 'POST', {
                encounterName: name
            });

            if (combat) {
                this.combats.push(combat);
                this._renderCombatSelector();
                await this._selectCombat(combat.id);
                this.hudController.addEventLogEntry(`Encounter "${name}" created!`, 'success');
                this._addCombatLog(`⚔️ New encounter: ${name}`);

                // Add character as combatant if selected
                if (this.selectedCharacterId && this.currentCharacter) {
                    await this._addCombatant(combat.id, this.currentCharacter.name, this.currentCharacter.armorClass || 10, this.currentCharacter.maxHitPoints || 10, true);
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create combat', 'danger');
        }
    }

    async _addCombatant(combatId, name, ac, maxHp, isCharacter) {
        try {
            const combatant = await this._fetch(`/combat/${combatId}/combatant`, 'POST', {
                name: name,
                ac: ac,
                maxHp: maxHp,
                isCharacter: isCharacter
            });

            if (combatant) {
                this._addCombatLog(`👤 ${name} added to combat (AC: ${ac}, HP: ${maxHp})`);
                await this._selectCombat(combatId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to add combatant', 'danger');
        }
    }

    // ── Attack ──

    async _makeAttack() {
        if (!this.selectedCombatId) {
            this.hudController.addEventLogEntry('No active combat!', 'warning');
            return;
        }

        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('No character selected!', 'warning');
            return;
        }

        const targetName = prompt('Target name:');
        if (!targetName) return;

        const attackBonus = prompt('Attack bonus (including proficiency):', '5');
        const damageRoll = prompt('Damage dice (e.g., 1d8+3):', '1d8+3');

        try {
            const result = await this._fetch(`/combat/${this.selectedCombatId}/attack`, 'POST', {
                attackerName: this.currentCharacter.name,
                targetName: targetName,
                attackBonus: parseInt(attackBonus) || 5,
                damageRoll: damageRoll
            });

            if (result) {
                const hitMsg = result.hit ? 'HIT!' : 'MISS!';
                const damage = result.damage || 0;
                this._addCombatLog(`⚔️ ${this.currentCharacter.name} attacks ${targetName}: ${hitMsg} (${damage} damage)`);
                this.hudController.addEventLogEntry(`${hitMsg} ${damage} damage!`, hitMsg === 'HIT!' ? 'success' : 'warning');

                // Show dice result
                this.hudController.addDiceResult({
                    formula: 'd20',
                    rolls: [result.attackRoll || 0],
                    total: result.attackRoll || 0,
                    mode: 'attack',
                    hit: result.hit,
                    damage: damage
                });

                await this._selectCombat(this.selectedCombatId);

                // Generate AI narrative
                this._generateNarrative(`${this.currentCharacter.name} attacks ${targetName}: ${result.hit ? 'HIT' : 'MISS'} for ${damage} damage.`);

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        result.hit ? 'COMBAT_WIN' : 'COMBAT_LOSS',
                        'dnd5e',
                        `${this.currentCharacter.name} attacks ${targetName}: ${result.hit ? 'HIT' : 'MISS'}`,
                        `${damage} damage.`,
                        { attacker: this.currentCharacter.name, target: targetName, hit: result.hit, damage }
                    );
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Attack failed', 'danger');
        }
    }

    // ── Spell Casting ──

    async _castSpell() {
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('No character selected!', 'warning');
            return;
        }

        // Show available spells
        const spellList = this.currentSpells.length > 0 ? this.currentSpells : this.spells;
        if (spellList.length === 0) {
            this.hudController.addEventLogEntry('No spells available!', 'warning');
            return;
        }

        let spellMenu = 'Available spells:\n';
        spellList.forEach((spell, i) => {
            spellMenu += `${i + 1}. ${spell.name} (Lv.${spell.level})\n`;
        });

        const choice = prompt(spellMenu + '\nEnter spell number:');
        const spellIndex = parseInt(choice) - 1;
        if (spellIndex < 0 || spellIndex >= spellList.length) return;

        const spell = spellList[spellIndex];
        const targetName = prompt('Target name (or "self"):');

        try {
            const result = await this._fetch('/gameplay/cast-spell', 'POST', {
                characterId: this.selectedCharacterId,
                spellName: spell.name,
                targetName: targetName || 'self'
            });

            if (result) {
                this._addCombatLog(`✨ ${this.currentCharacter.name} casts ${spell.name} on ${targetName || 'self'}`);
                this.hudController.addEventLogEntry(`Cast ${spell.name}!`, 'success');

                if (this.selectedCombatId) {
                    await this._selectCombat(this.selectedCombatId);
                }

                // Generate AI narrative
                this._generateNarrative(`${this.currentCharacter.name} casts ${spell.name} on ${targetName || 'self'}.`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Spell cast failed', 'danger');
        }
    }

    // ── Heal ──

    async _healCombatant() {
        if (!this.selectedCombatId) {
            this.hudController.addEventLogEntry('No active combat!', 'warning');
            return;
        }

        const targetName = prompt('Combatant to heal:');
        if (!targetName) return;

        const amount = prompt('Heal amount:', '10');

        try {
            const result = await this._fetch(`/combat/${this.selectedCombatId}/heal`, 'POST', {
                name: targetName,
                amount: parseInt(amount) || 10
            });

            if (result) {
                this._addCombatLog(`💚 ${targetName} healed for ${amount} HP (now: ${result.currentHp}/${result.maxHp})`);
                this.hudController.addEventLogEntry(`${targetName} healed!`, 'success');
                await this._selectCombat(this.selectedCombatId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Heal failed', 'danger');
        }
    }

    // ── Skill Check ──

    async _skillCheck() {
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('No character selected!', 'warning');
            return;
        }

        const skill = prompt('Skill name (Athletics, Acrobatics, Stealth, Perception, Investigation, Insight, Animal Handling, Arcana, History, Nature, Religion, Deception, Intimidation, Performance, Persuasion):', 'Perception');
        const dc = prompt('DC (Difficulty Class):', '10');
        const advantage = confirm('Roll with advantage?');

        try {
            const result = await this._fetch('/gameplay/skill-check', 'POST', {
                characterId: this.selectedCharacterId,
                skill: skill,
                dc: parseInt(dc) || 10,
                advantage: advantage,
                disadvantage: !advantage
            });

            if (result) {
                const success = result.success ? 'SUCCESS!' : 'FAILURE!';
                this._addCombatLog(`📋 ${this.currentCharacter.name} ${skill} check: ${success} (rolled ${result.roll}, DC ${result.dc})`);
                this.hudController.addEventLogEntry(`${skill}: ${success}`, success === 'SUCCESS!' ? 'success' : 'warning');

                this.hudController.addDiceResult({
                    formula: 'd20',
                    rolls: [result.roll || 0],
                    total: result.roll || 0,
                    mode: advantage ? 'advantage' : 'normal',
                    dc: result.dc,
                    success: result.success
                });
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Skill check failed', 'danger');
        }
    }

    // ── Saving Throw ──

    async _savingThrow() {
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('No character selected!', 'warning');
            return;
        }

        const ability = prompt('Ability (STR, DEX, CON, INT, WIS, CHA):', 'WIS');
        const dc = prompt('DC (Difficulty Class):', '10');
        const advantage = confirm('Roll with advantage?');

        try {
            const result = await this._fetch('/gameplay/saving-throw', 'POST', {
                characterId: this.selectedCharacterId,
                ability: ability,
                dc: parseInt(dc) || 10,
                advantage: advantage,
                disadvantage: !advantage
            });

            if (result) {
                const success = result.success ? 'SUCCESS!' : 'FAILURE!';
                this._addCombatLog(`💀 ${this.currentCharacter.name} ${ability} saving throw: ${success} (rolled ${result.roll}, DC ${result.dc})`);
                this.hudController.addEventLogEntry(`${ability} Save: ${success}`, success === 'SUCCESS!' ? 'success' : 'warning');

                this.hudController.addDiceResult({
                    formula: 'd20',
                    rolls: [result.roll || 0],
                    total: result.roll || 0,
                    mode: advantage ? 'advantage' : 'normal',
                    dc: result.dc,
                    success: result.success
                });
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Saving throw failed', 'danger');
        }
    }

    // ── End Round ──

    async _endRound() {
        if (!this.selectedCombatId) {
            this.hudController.addEventLogEntry('No active combat!', 'warning');
            return;
        }

        try {
            const combat = await this._fetch(`/combat/${this.selectedCombatId}/end-round`, 'POST');
            if (combat) {
                this._addCombatLog(`⏭️ Round ${combat.currentRound || 1} begins`);
                this.hudController.addEventLogEntry(`Round ${combat.currentRound || 1}`, 'info');
                await this._selectCombat(this.selectedCombatId);

                // Generate AI narrative
                this._generateNarrative(`Round ${combat.currentRound || 1} begins. Describe what happens next in combat.`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to end round', 'danger');
        }
    }

    // ── Chat ──

    _handleChat(message) {
        if (!message.trim()) return;
        this._addCombatLog(`💬 ${this.currentCharacter?.name || 'Player'}: ${message}`);
        this.hudController.addEventLogEntry(message, 'info');
    }

    // ── Combat Log ──

    _addCombatLog(entry) {
        this.combatLog.push(entry);
        if (this.combatLog.length > 50) {
            this.combatLog.shift();
        }
        this.hudController.addEventLogEntry(entry, 'info');
    }

    // ── AI Narrative ──

    _generateNarrative(context) {
        if (!window.AiNarrative) return;
        const sessionId = 'dnd-narrative-' + (this.selectedCharacterId || 'default');
        const systemPrompt = window.AiNarrative.getSystemPrompt('dnd-5e');
        const characterName = this.currentCharacter ? this.currentCharacter.name : 'Adventurer';
        const campaignName = this.currentCampaign ? this.currentCampaign.name : 'Unknown Campaign';
        const message = `Character: ${characterName} (${this.currentCharacter ? this.currentCharacter.race : ''} ${this.currentCharacter ? this.currentCharacter.characterClass : ''} ${this.currentCharacter ? this.currentCharacter.level : ''}). Campaign: ${campaignName}. ${this.selectedCombatId ? 'In combat.' : 'Exploring.'} ${context}`;

        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        const loadingEl = document.createElement('div');
        loadingEl.className = 'hud-narrative-loading';
        loadingEl.textContent = '✨ Generating narrative...';
        loadingEl.style.cssText = 'color: #b45309; font-style: italic; margin: 10px 0;';
        promptPanel.appendChild(loadingEl);

        window.AiNarrative.streamToPanel(
            sessionId, message, systemPrompt,
            null,
            (fullText) => {
                if (loadingEl.parentNode) loadingEl.remove();
                if (fullText) {
                    const narrativeEl = document.createElement('div');
                    narrativeEl.className = 'hud-narrative-text';
                    narrativeEl.style.cssText = 'color: #fcd34d; margin: 10px 0; padding: 10px; border-left: 3px solid #b45309; background: rgba(180, 83, 9, 0.1);';
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
        const characterName = this.currentCharacter ? this.currentCharacter.name : 'Adventurer';
        this._generateNarrative(`Continue the narrative for ${characterName}. Describe what happens next.`);
    }
}

// Expose globally
window.Dnd5eAdapter = Dnd5eAdapter;

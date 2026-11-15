/* Classic Traveller Adapter — Connects HUD panels to Traveller REST API */

class TravellerAdapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.characters = [];
        this.selectedCharacterId = null;
        this.currentCharacter = null;
        this.careerLog = [];
        this.combatLog = [];
        this.currentCombatId = null;
        this.combatType = null; // 'firearms' or 'melee'
        this.psionicDisciplines = [];
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Restore session state from localStorage
        const savedCharId = localStorage.getItem('traveller-character-id');
        if (savedCharId) {
            this.selectedCharacterId = savedCharId;
        }
        const savedCombatId = localStorage.getItem('traveller-combat-id');
        if (savedCombatId) {
            this.currentCombatId = savedCombatId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'traveller') {
                this._onTemplateLoaded();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'traveller') {
                this._handleAction(action);
            }
        });

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

        console.log('[TravellerAdapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._loadCharacters();
        this._loadPsionicDisciplines();
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

        let selectorHtml = `<select class="hud-selector" id="hudTravellerCharSelector">
            <option value="">Select Character...</option>
            ${this.characters.map(c => `
                <option value="${c.id}" ${c.id === this.selectedCharacterId ? 'selected' : ''}>
                    ${c.name || 'Unnamed'} (${c.careerStatus || 'NEW'}) Age ${c.age || 18}
                </option>
            `).join('')}
        </select>`;

        selectorHtml += `<button class="hud-action-button" data-action="create-character" style="margin-bottom:10px;">
            <span class="action-icon">➕</span>
            <span class="action-label">Create New Character</span>
        </button>`;

        selectorHtml += `<button class="hud-action-button" data-action="generate-character" style="margin-bottom:10px;">
            <span class="action-icon">🎲</span>
            <span class="action-label">Generate Character (Full)</span>
        </button>`;

        const existing = body.querySelector('#hudTravellerCharSelector');
        if (existing) {
            existing.parentElement.outerHTML = selectorHtml;
        } else {
            body.insertAdjacentHTML('afterbegin', selectorHtml);
        }

        const newSelector = document.getElementById('hudTravellerCharSelector');
        if (newSelector) {
            newSelector.addEventListener('change', (e) => {
                this._selectCharacter(e.target.value);
            });
        }
    }

    async _selectCharacter(characterId) {
        if (!characterId) return;
        this.selectedCharacterId = characterId;
        localStorage.setItem('traveller-character-id', characterId);

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

        // 7 ability scores (2D6)
        const abilities = [
            { name: 'ST', label: 'Strength', score: c.st || 10 },
            { name: 'DX', label: 'Dexterity', score: c.dx || 10 },
            { name: 'END', label: 'Endurance', score: c.end || 10 },
            { name: 'EDU', label: 'Education', score: c.edu || 10 },
            { name: 'INT', label: 'Intelligence', score: c.intScore || 10 },
            { name: 'SOC', label: 'Social Standing', score: c.soc || 10 },
            { name: 'APP', label: 'Appearance', score: c.app || 10 }
        ];

        // HP stat bar
        const maxHp = c.hitPoints || c.end || 10;
        const hpPercent = Math.round(((c.currentHp || c.hitPoints || maxHp) / maxHp) * 100);

        let sheetHtml = `
        <div class="hud-character-sheet">
            <div class="char-name">${c.name || 'Unnamed'}</div>
            <div class="char-meta">Age ${c.age || 18} | ${c.race || 'Human'} | ${c.careerStatus || 'NEW'}</div>
            ${c.alienRace ? `<div class="char-meta" style="font-size:11px;">${c.alienRace}</div>` : ''}

            <div style="display:flex;gap:8px;margin:8px 0;">
                <div style="flex:1;text-align:center;padding:4px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:4px;">
                    <div style="font-size:10px;color:var(--hud-text-muted);text-transform:uppercase;">Wealth</div>
                    <div style="font-size:16px;font-weight:700;">${c.wealth || 0} <span style="font-size:10px;">WC</span></div>
                </div>
                <div style="flex:1;text-align:center;padding:4px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:4px;">
                    <div style="font-size:10px;color:var(--hud-text-muted);text-transform:uppercase;">Career</div>
                    <div style="font-size:14px;font-weight:700;">${c.currentCareer || '—'}</div>
                </div>
            </div>

            <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin:8px 0 4px;">Ability Scores (2D6)</div>
            <div class="ability-grid" style="grid-template-columns: repeat(7, 1fr);">
                ${abilities.map(a => `
                    <div class="ability-score">
                        <div class="ability-name">${a.name}</div>
                        <div class="ability-value">${a.score}</div>
                    </div>
                `).join('')}
            </div>

            <div class="hud-stat-bar" style="margin-top:8px;">
                <div class="hud-stat-bar-label">
                    <span>HP</span>
                    <span class="stat-value">${c.currentHp || c.hitPoints || maxHp} / ${maxHp}</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill ${hpPercent > 50 ? 'success' : hpPercent > 25 ? 'warning' : 'danger'}"
                         style="width: ${hpPercent}%"></div>
                </div>
            </div>

            ${c.psionicProfile && c.psionicProfile.length > 0 ? `
                <div style="margin-top:6px;padding:4px 8px;background:rgba(168,85,247,0.2);border:1px solid #a855f7;border-radius:4px;font-size:12px;color:#e9d5ff;">
                    🧠 Psionic: ${c.psionicProfile.join(', ')}
                </div>
            ` : ''}
        </div>`;

        // Skills
        if (c.skills && c.skills.length > 0) {
            sheetHtml += `
            <div style="margin-top:10px;">
                <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin-bottom:4px;">Skills (${c.skills.length})</div>
                <div style="display:flex;flex-wrap:wrap;gap:4px;max-height:100px;overflow-y:auto;">
                    ${c.skills.map(skill => `
                        <div style="padding:2px 6px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:3px;font-size:11px;">
                            ${skill}
                        </div>
                    `).join('')}
                </div>
            </div>`;
        }

        // Career tours
        if (c.careerTours && c.careerTours.length > 0) {
            sheetHtml += `
            <div style="margin-top:8px;">
                <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin-bottom:4px;">Career Tours</div>
                ${c.careerTours.map(tour => `
                    <div style="font-size:11px;padding:2px 0;color:var(--hud-text-muted);">
                        ${tour.career || '?'}: ${tour.tours || 0} tours, ${tour.promotions || 0} promotions
                    </div>
                `).join('')}
            </div>`;
        }

        // Replace the character sheet (keep selector and buttons)
        const existingSheet = body.querySelector('.hud-character-sheet');
        if (existingSheet) {
            existingSheet.outerHTML = sheetHtml;
        } else {
            const buttons = body.querySelectorAll('.hud-action-button');
            const lastButton = buttons[buttons.length - 1];
            if (lastButton) {
                lastButton.insertAdjacentHTML('afterend', sheetHtml);
            } else {
                body.insertAdjacentHTML('beforeend', sheetHtml);
            }
        }
    }

    // ── Career Service ──

    async _serveCareer(career) {
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }

        try {
            this.hudController.addEventLogEntry(`Starting ${career} career tour...`, 'info');
            const result = await this._fetch(`/characters/${this.currentCharacter.id}/career?career=${career}`, {
                method: 'POST',
                body: JSON.stringify({})
            });

            if (result) {
                this.currentCharacter = await this._fetch(`/characters/${this.currentCharacter.id}`);
                this._renderCharacterSheet();
                this._renderCareerNarrative(result);
                this.hudController.addEventLogEntry(`${career} tour complete!`, 'success');

                // Generate AI narrative
                const events = result.events ? result.events.map(e => e.description || e).join(', ') : 'No events';
                this._generateNarrative(`${this.currentCharacter.name} completed a ${career} career tour. Events: ${events}.`);

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        'LEVEL_UP',
                        'traveller',
                        `${this.currentCharacter.name} completed ${career} career tour`,
                        `Events: ${events}.`,
                        { character: this.currentCharacter.name, career, events: result.events }
                    );
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Career service failed: ${err.message}`, 'danger');
        }
    }

    _renderCareerNarrative(result) {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body) return;

        let narrativeHtml = '';

        if (result.events && result.events.length > 0) {
            narrativeHtml += `<div style="margin-top:8px;">
                <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin-bottom:4px;">Career Events</div>
                ${result.events.map(event => `
                    <div style="padding:6px 8px;background:var(--hud-accent-glow);border-left:3px solid var(--hud-accent-primary);margin-bottom:4px;font-size:12px;">
                        ${event.description || event}
                    </div>
                `).join('')}
            </div>`;
        }

        if (result.skills && result.skills.length > 0) {
            narrativeHtml += `<div style="margin-top:6px;">
                <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin-bottom:4px;">Skills Learned</div>
                ${result.skills.map(skill => `
                    <div style="font-size:12px;padding:2px 0;color:#86efac;">+ ${skill}</div>
                `).join('')}
            </div>`;
        }

        if (result.wealth) {
            narrativeHtml += `<div style="margin-top:6px;font-size:12px;color:#fde68a;">
                💰 Wealth gained: ${result.wealth} WC
            </div>`;
        }

        if (result.promotion) {
            narrativeHtml += `<div style="margin-top:6px;font-size:12px;color:#67e8f9;">
                ⭐ Promoted!
            </div>`;
        }

        body.innerHTML = narrativeHtml || '<div style="color:var(--hud-text-muted);font-size:12px;">Career tour complete.</div>';
    }

    async _retireCharacter() {
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }

        try {
            this.hudController.addEventLogEntry('Retiring character from service...', 'info');
            const result = await this._fetch(`/characters/${this.currentCharacter.id}/retire`, {
                method: 'POST',
                body: JSON.stringify({})
            });

            if (result) {
                this.currentCharacter = await this._fetch(`/characters/${this.currentCharacter.id}`);
                this._renderCharacterSheet();
                this.hudController.addEventLogEntry(`${this.currentCharacter.name} is now retired! Ready for adventure.`, 'success');
                this._renderAdventurePrompt();
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Retirement failed: ${err.message}`, 'danger');
        }
    }

    _renderAdventurePrompt() {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body) return;

        body.innerHTML = `
        <div style="padding:10px;">
            <div style="font-size:14px;font-weight:600;color:var(--hud-accent-primary);margin-bottom:8px;">
                🌌 Adventure Awaits
            </div>
            <div style="font-size:12px;color:var(--hud-text-muted);line-height:1.6;">
                ${this.currentCharacter.name} has retired from service and is ready for adventure.
                The stars await! Choose an action to begin your journey.
            </div>
        </div>`;
    }

    // ── Combat ──

    async _startFirearmsCombat() {
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }

        try {
            this.hudController.addEventLogEntry('Starting firearms combat...', 'info');
            const result = await this._fetch('/firearms/combat', {
                method: 'POST',
                body: JSON.stringify({
                    characterId: this.currentCharacter.id
                })
            });

            if (result) {
                this.currentCombatId = result.id;
                this.combatType = 'firearms';
                localStorage.setItem('traveller-combat-id', this.currentCombatId);
                this._renderCombatStatus(result);
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Combat start failed: ${err.message}`, 'danger');
        }
    }

    async _startMeleeCombat() {
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }

        try {
            this.hudController.addEventLogEntry('Starting melee combat...', 'info');
            const result = await this._fetch('/melee/combat', {
                method: 'POST',
                body: JSON.stringify({
                    characterId: this.currentCharacter.id
                })
            });

            if (result) {
                this.currentCombatId = result.id;
                this.combatType = 'melee';
                localStorage.setItem('traveller-combat-id', this.currentCombatId);
                this._renderCombatStatus(result);
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Combat start failed: ${err.message}`, 'danger');
        }
    }

    async _combatRound() {
        if (!this.currentCombatId) {
            this.hudController.addEventLogEntry('No active combat', 'warning');
            return;
        }

        try {
            const endpoint = this.combatType === 'melee' ? '/melee' : '/firearms';
            const result = await this._fetch(`${endpoint}/combat/${this.currentCombatId}/round`, {
                method: 'POST',
                body: JSON.stringify({})
            });

            if (result) {
                this._renderCombatLog(result);
                this._roll2D6(result.roll || 7, `Combat round`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Combat round failed: ${err.message}`, 'danger');
        }
    }

    _renderCombatStatus(combat) {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body) return;

        let html = `
        <div style="padding:10px;">
            <div style="font-size:14px;font-weight:600;color:var(--hud-accent-primary);margin-bottom:8px;">
                ⚔️ ${this.combatType === 'melee' ? 'Melee' : 'Firearms'} Combat
            </div>
            <div style="font-size:12px;color:var(--hud-text-muted);line-height:1.6;">
                ${combat.description || 'Combat initiated!'}
            </div>
            ${combat.enemies && combat.enemies.length > 0 ? `
                <div style="margin-top:8px;">
                    <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin-bottom:4px;">Enemies</div>
                    ${combat.enemies.map(enemy => `
                        <div style="padding:4px 8px;background:rgba(220,38,38,0.2);border:1px solid #dc2626;border-radius:4px;font-size:12px;margin-bottom:4px;">
                            ${enemy.name || 'Unknown'} — HP: ${enemy.hitPoints || '?'}
                        </div>
                    `).join('')}
                </div>
            ` : ''}
        </div>`;

        body.innerHTML = html;
    }

    _renderCombatLog(result) {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body) return;

        const existing = body.innerHTML;
        const logEntry = `
        <div style="margin-top:8px;padding:6px 8px;background:var(--hud-accent-glow);border-left:3px solid var(--hud-accent-primary);font-size:12px;">
            ${result.narrative || result.description || 'Combat round complete.'}
        </div>`;
        body.innerHTML = existing + logEntry;
        body.scrollTop = body.scrollHeight;
    }

    // ── Psionics ──

    async _loadPsionicDisciplines() {
        try {
            this.psionicDisciplines = await this._fetch('/psionics/disciplines') || [];
        } catch (err) {
            // Non-critical
        }
    }

    async _detectPsionic() {
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }

        try {
            this.hudController.addEventLogEntry('Detecting psionic activity...', 'info');
            const result = await this._fetch(`/psionics/characters/${this.currentCharacter.id}/detect`, {
                method: 'POST',
                body: JSON.stringify({})
            });

            if (result) {
                const roll = result.roll || 7;
                this._roll2D6(roll, 'Psionic Detection');
                this.hudController.addEventLogEntry(
                    result.detected ? 'Psionic activity detected!' : 'No psionic activity detected.',
                    result.detected ? 'success' : 'info'
                );
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Psionic detection failed: ${err.message}`, 'danger');
        }
    }

    async _powerCheck() {
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }

        try {
            this.hudController.addEventLogEntry('Attempting psionic power check...', 'info');
            const result = await this._fetch(`/psionics/characters/${this.currentCharacter.id}/power-check`, {
                method: 'POST',
                body: JSON.stringify({})
            });

            if (result) {
                const roll = result.roll || 7;
                this._roll2D6(roll, 'Psionic Power');
                this.hudController.addEventLogEntry(
                    result.success ? 'Psionic power activated!' : 'Psionic power failed.',
                    result.success ? 'success' : 'warning'
                );
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Psionic power check failed: ${err.message}`, 'danger');
        }
    }

    // ── Dice Rolling ──

    _roll2D6(target, label) {
        const d1 = Math.floor(Math.random() * 6) + 1;
        const d2 = Math.floor(Math.random() * 6) + 1;
        const total = d1 + d2;

        this.hudController.addDiceResult({
            label: label || '2D6 Roll',
            dice: [d1, d2],
            total: total,
            target: target,
            success: total <= target
        });
    }

    async _roll2D6FromServer(label) {
        try {
            const result = await this._fetch('/rules/roll?dice=2d6', {
                method: 'GET'
            });

            if (result) {
                this.hudController.addDiceResult({
                    label: label || '2D6 Roll',
                    dice: result.dice || [1, 1],
                    total: result.total || 2,
                    success: true
                });
            }
        } catch (err) {
            // Fallback to client-side roll
            this._roll2D6(7, label);
        }
    }

    // ── Action Handlers ──

    async _handleAction(action) {
        switch (action) {
            case 'create-character':
                await this._createCharacter();
                break;
            case 'generate-character':
                await this._generateCharacter();
                break;
            case 'career-soldier':
                await this._serveCareer('SOLDIER');
                break;
            case 'career-explorer':
                await this._serveCareer('EXPLORER');
                break;
            case 'career-gunner':
                await this._serveCareer('GUNNER');
                break;
            case 'career-pilot':
                await this._serveCareer('PILOT');
                break;
            case 'career-merchant':
                await this._serveCareer('MERCHANT');
                break;
            case 'career-scout':
                await this._serveCareer('SCOUT');
                break;
            case 'career-spy':
                await this._serveCareer('SPY');
                break;
            case 'career-noble':
                await this._serveCareer('NOBLE');
                break;
            case 'action-firearms':
                if (this.currentCombatId) {
                    await this._combatRound();
                } else {
                    await this._startFirearmsCombat();
                }
                break;
            case 'action-melee':
                if (this.currentCombatId) {
                    await this._combatRound();
                } else {
                    await this._startMeleeCombat();
                }
                break;
            case 'action-psionic-detect':
                await this._detectPsionic();
                break;
            case 'action-psionic-power':
                await this._powerCheck();
                break;
            case 'action-roll-2d6':
                await this._roll2D6FromServer('Manual 2D6 Roll');
                break;
            case 'action-retire':
                await this._retireCharacter();
                break;
        }
    }

    // ── Character Creation ──

    async _createCharacter() {
        try {
            this.hudController.addEventLogEntry('Creating new character...', 'info');
            const result = await this._fetch('/characters', {
                method: 'POST',
                body: JSON.stringify({
                    name: 'Traveller ' + (this.characters.length + 1)
                })
            });

            if (result) {
                await this._loadCharacters();
                await this._selectCharacter(result.id);
                this.hudController.addEventLogEntry(`Character ${result.name} created!`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Character creation failed: ${err.message}`, 'danger');
        }
    }

    async _generateCharacter() {
        try {
            this.hudController.addEventLogEntry('Generating full character with careers...', 'info');
            const result = await this._fetch('/characters/generate', {
                method: 'POST',
                body: JSON.stringify({
                    name: 'Generated Traveller ' + (this.characters.length + 1)
                })
            });

            if (result) {
                await this._loadCharacters();
                await this._selectCharacter(result.id);
                this.hudController.addEventLogEntry(`Character ${result.name} generated with full career history!`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Character generation failed: ${err.message}`, 'danger');
        }
    }

    // ── Chat Handler ──

    async _handleChat(message) {
        if (!message.trim()) return;

        this.hudController.addEventLogEntry(`You: ${message}`, 'info');

        // Try to use AI for narrative response
        try {
            const result = await this._fetch('/ai/chat', {
                method: 'POST',
                body: JSON.stringify({
                    message: message,
                    context: 'traveller'
                })
            });

            if (result && result.response) {
                this.hudController.addEventLogEntry(`DM: ${result.response}`, 'success');
            }
        } catch (err) {
            // Fallback: just log the message
        }
    }

    // ── AI Narrative ──

    _generateNarrative(context) {
        if (!window.AiNarrative) return;
        const sessionId = 'traveller-narrative-' + (this.currentCharacter ? this.currentCharacter.id : 'default');
        const systemPrompt = window.AiNarrative.getSystemPrompt('traveller');
        const characterName = this.currentCharacter ? this.currentCharacter.name : 'Traveller';
        const message = `Character: ${characterName}. ${this.currentCharacter ? 'Age: ' + this.currentCharacter.age + '. Status: ' + (this.currentCharacter.status || 'ACTIVE') + '.' : ''} ${context}`;

        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        const loadingEl = document.createElement('div');
        loadingEl.className = 'hud-narrative-loading';
        loadingEl.textContent = '✨ Generating narrative...';
        loadingEl.style.cssText = 'color: #d97706; font-style: italic; margin: 10px 0;';
        promptPanel.appendChild(loadingEl);

        window.AiNarrative.streamToPanel(
            sessionId, message, systemPrompt,
            null,
            (fullText) => {
                if (loadingEl.parentNode) loadingEl.remove();
                if (fullText) {
                    const narrativeEl = document.createElement('div');
                    narrativeEl.className = 'hud-narrative-text';
                    narrativeEl.style.cssText = 'color: #fcd34d; margin: 10px 0; padding: 10px; border-left: 3px solid #d97706; background: rgba(217, 119, 6, 0.1);';
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
        if (!this.currentCharacter) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }
        this._generateNarrative(`Continue the narrative for ${this.currentCharacter.name}. Describe what happens next.`);
    }
}

// Expose globally
window.TravellerAdapter = TravellerAdapter;

/* Captain's Log Adapter — Connects HUD panels to Captain's Log REST API */

class CaptainsLogAdapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.characters = [];
        this.selectedCharacterId = null;
        this.currentCharacter = null;
        this.missions = [];
        this.selectedMissionId = null;
        this.currentMission = null;
        this.starships = [];
        this.selectedStarshipId = null;
        this.currentStarship = null;
        this.logEntries = [];
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Restore session state from localStorage
        const savedCharId = localStorage.getItem('cl-character-id');
        if (savedCharId) {
            this.selectedCharacterId = savedCharId;
        }
        const savedMissionId = localStorage.getItem('cl-mission-id');
        if (savedMissionId) {
            this.selectedMissionId = savedMissionId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'captains-log') {
                this._onTemplateLoaded();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'captains-log') {
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

        // Listen for mission selector change
        const missionSelector = document.getElementById('hudMissionSelector');
        if (missionSelector) {
            missionSelector.addEventListener('change', (e) => {
                this._selectMission(e.target.value);
            });
        }

        console.log('[CaptainsLogAdapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._loadCharacters();
        this._loadStarships();
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
                    ${c.name || 'Unnamed'} (${c.rank || 'ENSIGN'})
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
        localStorage.setItem('cl-character-id', characterId);

        try {
            this.currentCharacter = await this._fetch(`/characters/${characterId}`);
            if (this.currentCharacter) {
                this._renderCharacterSheet();
                await this._loadMissions();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load character details', 'danger');
        }
    }

    _renderCharacterSheet() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body || !this.currentCharacter) return;

        const c = this.currentCharacter;

        // 6 attributes
        const attributes = [
            { name: 'FIT', label: 'Fitness', value: c.fitness || 0 },
            { name: 'TEC', label: 'Technical', value: c.technical || 0 },
            { name: 'STR', label: 'Strength', value: c.strength || 0 },
            { name: 'REA', label: 'Reason', value: c.reason || 0 },
            { name: 'INS', label: 'Insight', value: c.insight || 0 },
            { name: 'PRE', label: 'Presence', value: c.presence || 0 }
        ];

        // 6 disciplines
        const disciplines = [
            { name: 'Command', value: c.command || 0 },
            { name: 'Leadership', value: c.leadership || 0 },
            { name: 'Science', value: c.science || 0 },
            { name: 'Security', value: c.security || 0 },
            { name: 'Medicine', value: c.medicine || 0 },
            { name: 'Operations', value: c.operations || 0 }
        ];

        // Constitution as stat bar
        const maxConstitution = 6;
        const conPercent = Math.round((c.constitution || 0) / maxConstitution * 100);

        let sheetHtml = `
        <div class="hud-character-sheet">
            <div class="char-name">${c.name || 'Unnamed'}</div>
            <div class="char-meta">${c.rank || 'ENSIGN'} | ${c.assignment || 'No assignment'}</div>

            <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin:8px 0 4px;">Attributes</div>
            <div class="ability-grid">
                ${attributes.map(a => `
                    <div class="ability-score">
                        <div class="ability-name">${a.name}</div>
                        <div class="ability-value">${'★'.repeat(a.value)}${'☆'.repeat(3 - a.value)}</div>
                    </div>
                `).join('')}
            </div>

            <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin:8px 0 4px;">Disciplines</div>
            <div class="ability-grid">
                ${disciplines.map(d => `
                    <div class="ability-score">
                        <div class="ability-name">${d.name}</div>
                        <div class="ability-value">${'★'.repeat(d.value)}${'☆'.repeat(3 - d.value)}</div>
                    </div>
                `).join('')}
            </div>

            <div class="hud-stat-bar" style="margin-top:8px;">
                <div class="hud-stat-bar-label">
                    <span>Constitution</span>
                    <span class="stat-value">${c.constitution || 0} / ${maxConstitution}</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill ${conPercent > 50 ? 'success' : conPercent > 25 ? 'warning' : 'danger'}"
                         style="width: ${conPercent}%"></div>
                </div>
            </div>

            ${c.injuryState && c.injuryState !== 'HEALTHY' ? `
                <div style="margin-top:6px;padding:4px 8px;background:rgba(220,38,38,0.2);border:1px solid #dc2626;border-radius:4px;font-size:12px;color:#fca5a5;">
                    ⚠️ ${c.injuryState}
                </div>
            ` : ''}
        </div>`;

        // Starship info
        if (c.assignedStarship || this.currentStarship) {
            const ship = c.assignedStarship || this.currentStarship;
            sheetHtml += `
            <div style="margin-top:10px;padding:8px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:4px;">
                <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin-bottom:4px;">🚀 Starship</div>
                <div style="font-size:13px;font-weight:600;">${ship.name || 'Unnamed Ship'}</div>
                ${ship.classType ? `<div style="font-size:11px;color:var(--hud-text-muted);">${ship.classType}</div>` : ''}
            </div>`;
        }

        // Career stats
        sheetHtml += `
        <div style="margin-top:8px;font-size:11px;color:var(--hud-text-muted);">
            <div>📋 Missions: ${c.missionsCompleted || 0}</div>
            <div>⭐ XP: ${c.experiencePoints || 0}</div>
            <div>🎯 Career Events: ${c.careerEvents || 0}</div>
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

    // ── Starship Management ──

    async _loadStarships() {
        try {
            this.starships = await this._fetch('/starships') || [];
        } catch (err) {
            // Starships may not be available
        }
    }

    // ── Mission Management ──

    async _loadMissions() {
        if (!this.selectedCharacterId) return;
        try {
            this.missions = await this._fetch(`/missions?characterId=${this.selectedCharacterId}`) || [];
            this._renderMissionSelector();
            if (this.selectedMissionId) {
                await this._selectMission(this.selectedMissionId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load missions', 'danger');
            this._renderMissionSelector();
        }
    }

    _renderMissionSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        const missionSelectorHtml = `<select class="hud-selector" id="hudMissionSelector" style="margin-top:10px;">
            <option value="">Select Mission...</option>
            ${this.missions.map(m => `
                <option value="${m.id}" ${m.id === this.selectedMissionId ? 'selected' : ''}>
                    #${m.missionNumber || '?'} ${m.name || 'Unnamed'} (${m.status || 'PLANNED'})
                </option>
            `).join('')}
        </select>`;

        const createMissionBtnHtml = `<button class="hud-action-button" data-action="create-mission" style="margin-top:6px;margin-bottom:10px;">
            <span class="action-icon">➕</span>
            <span class="action-label">New Mission</span>
        </button>`;

        const existing = body.querySelector('#hudMissionSelector');
        if (existing) {
            existing.outerHTML = missionSelectorHtml;
        } else {
            body.insertAdjacentHTML('beforeend', missionSelectorHtml + createMissionBtnHtml);
        }

        // Re-bind
        const newSelector = document.getElementById('hudMissionSelector');
        if (newSelector) {
            newSelector.addEventListener('change', (e) => {
                this._selectMission(e.target.value);
            });
        }
    }

    async _selectMission(missionId) {
        if (!missionId) return;
        this.selectedMissionId = missionId;
        localStorage.setItem('cl-mission-id', missionId);

        try {
            this.currentMission = await this._fetch(`/missions/${missionId}`);
            if (this.currentMission) {
                this._renderMissionStatus();
                this._renderMissionNarrative();
                this._renderTaskButtons();
                await this._loadLogEntries();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load mission', 'danger');
        }
    }

    _renderMissionStatus() {
        // Append mission status to info panel
        const body = document.getElementById('hudInfoPanelBody');
        if (!body || !this.currentMission) return;

        const m = this.currentMission;

        // Remove existing mission status
        const existingStatus = body.querySelector('#hudMissionStatus');
        if (existingStatus) existingStatus.remove();

        const momentumPercent = Math.round((m.momentum || 0) * 100);
        const threatPercent = Math.min(100, Math.round((m.threat || 0) / 5 * 100));

        const statusHtml = `
        <div id="hudMissionStatus" style="margin-top:10px;padding:8px;background:var(--hud-accent-glow);border:1px solid var(--hud-accent-primary);border-radius:4px;">
            <div style="font-size:11px;color:var(--hud-text-muted);text-transform:uppercase;margin-bottom:4px;">📡 Mission Status</div>
            <div style="font-size:13px;font-weight:600;">Act ${m.currentAct || 1} · Beat ${m.currentBeat || 1}/5</div>
            <div style="font-size:11px;color:var(--hud-text-muted);margin-top:2px;">${m.type || 'Unknown'} · ${m.status || 'PLANNED'}</div>

            <div class="hud-stat-bar" style="margin-top:6px;">
                <div class="hud-stat-bar-label">
                    <span>Momentum</span>
                    <span class="stat-value">${m.momentum || 0}</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill success" style="width: ${momentumPercent}%"></div>
                </div>
            </div>

            <div class="hud-stat-bar" style="margin-top:4px;">
                <div class="hud-stat-bar-label">
                    <span>Threat</span>
                    <span class="stat-value">${m.threat || 0}</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill ${threatPercent > 60 ? 'danger' : threatPercent > 30 ? 'warning' : 'info'}" style="width: ${threatPercent}%"></div>
                </div>
            </div>
        </div>`;

        body.insertAdjacentHTML('beforeend', statusHtml);
    }

    _renderMissionNarrative() {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body || !this.currentMission) return;

        const m = this.currentMission;

        // Get the current act's log text
        let narrative = '';
        if (m.currentAct === 1 && m.act1Log) {
            narrative = m.act1Log;
        } else if (m.currentAct === 2 && m.act2Log) {
            narrative = m.act2Log;
        } else if (m.currentAct === 3 && m.act3Log) {
            narrative = m.act3Log;
        }

        if (!narrative) {
            narrative = m.summary || 'No log entries yet. Begin the mission by writing your first log entry.';
        }

        // Keep mission selector if present
        const selector = body.querySelector('#hudMissionSelector');
        const createBtn = body.querySelector('[data-action="create-mission"]');
        const selectorHtml = selector ? selector.outerHTML : '';
        const createBtnHtml = createBtn ? createBtn.outerHTML : '';

        body.innerHTML = selectorHtml + createBtnHtml + `
            <div class="hud-narrative-text">
                <p><strong>Act ${m.currentAct || 1} — Beat ${m.currentBeat || 1}</strong></p>
                <p>${narrative}</p>
                ${m.development ? `<div class="narrative-source">Development: ${m.development}</div>` : ''}
            </div>`;

        // Re-bind selector
        const newSelector = document.getElementById('hudMissionSelector');
        if (newSelector) {
            newSelector.addEventListener('change', (e) => {
                this._selectMission(e.target.value);
            });
        }
    }

    // ── Task Attempt Buttons ──

    _renderTaskButtons() {
        const body = document.getElementById('hudResponsesPanelBody');
        if (!body || !this.currentCharacter || !this.currentMission) return;

        const c = this.currentCharacter;

        // Task attempt mappings: action → attribute + discipline
        const taskMappings = [
            { action: 'task-pilot', label: 'Pilot', icon: '✈️', attribute: 'fitness', discipline: 'operations' },
            { action: 'task-navigate', label: 'Navigate', icon: '🧭', attribute: 'reason', discipline: 'operations' },
            { action: 'task-science', label: 'Science', icon: '🔬', attribute: 'reason', discipline: 'science' },
            { action: 'task-security', label: 'Security', icon: '🔫', attribute: 'strength', discipline: 'security' },
            { action: 'task-medical', label: 'Medical', icon: '🏥', attribute: 'insight', discipline: 'medicine' },
            { action: 'task-engineering', label: 'Engineering', icon: '⚙️', attribute: 'technical', discipline: 'operations' },
            { action: 'task-diplomacy', label: 'Diplomacy', icon: '🤝', attribute: 'presence', discipline: 'leadership' },
            { action: 'task-command', label: 'Command', icon: '👑', attribute: 'presence', discipline: 'command' },
            { action: 'task-leadership', label: 'Leadership', icon: '📢', attribute: 'presence', discipline: 'leadership' }
        ];

        // Calculate target numbers for each task
        const buttonsHtml = taskMappings.map(t => {
            const attrValue = c[t.attribute] || 0;
            const discValue = c[t.discipline] || 0;
            const total = attrValue + discValue;
            // Target number: higher is harder. Base 11, modified by skill
            const targetNumber = Math.max(5, 11 - total);
            return `<button class="hud-action-button" data-action="${t.action}">
                <span class="action-icon">${t.icon}</span>
                <span class="action-label">${t.label}</span>
                <span class="action-hint">TN ${targetNumber}</span>
            </button>`;
        }).join('');

        // Oracle and utility buttons
        const utilityButtons = `
            <button class="hud-action-button" data-action="oracle-query">
                <span class="action-icon">🎲</span>
                <span class="action-label">Oracle Query</span>
                <span class="action-hint">Ask yes/no</span>
            </button>
            <button class="hud-action-button" data-action="log-entry">
                <span class="action-icon">📝</span>
                <span class="action-label">Log Entry</span>
                <span class="action-hint">Write log</span>
            </button>
            <button class="hud-action-button" data-action="advance-beat">
                <span class="action-icon">⏭️</span>
                <span class="action-label">Advance Beat</span>
                <span class="action-hint">Next beat</span>
            </button>
            <button class="hud-action-button" data-action="complete-mission">
                <span class="action-icon">✅</span>
                <span class="action-label">Complete Mission</span>
                <span class="action-hint">Finish</span>
            </button>`;

        body.innerHTML = buttonsHtml + utilityButtons;
    }

    // ── Log Entries ──

    async _loadLogEntries() {
        if (!this.selectedMissionId) return;
        try {
            this.logEntries = await this._fetch(`/missions/${this.selectedMissionId}/entries`) || [];
        } catch (err) {
            // Log entries may not exist yet
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
            case 'create-mission':
                await this._createMission();
                break;
            case 'task-pilot':
                await this._attemptTask('fitness', 'operations');
                break;
            case 'task-navigate':
                await this._attemptTask('reason', 'operations');
                break;
            case 'task-science':
                await this._attemptTask('reason', 'science');
                break;
            case 'task-security':
                await this._attemptTask('strength', 'security');
                break;
            case 'task-medical':
                await this._attemptTask('insight', 'medicine');
                break;
            case 'task-engineering':
                await this._attemptTask('technical', 'operations');
                break;
            case 'task-diplomacy':
                await this._attemptTask('presence', 'leadership');
                break;
            case 'task-command':
                await this._attemptTask('presence', 'command');
                break;
            case 'task-leadership':
                await this._attemptTask('presence', 'leadership');
                break;
            case 'oracle-query':
                await this._queryOracle();
                break;
            case 'log-entry':
                await this._writeLogEntry();
                break;
            case 'advance-beat':
                await this._advanceBeat();
                break;
            case 'complete-mission':
                await this._completeMission();
                break;
            default:
                this.hudController.addEventLogEntry(`Unknown action: ${action}`, 'warning');
        }
    }

    // ── Character Creation ──

    async _createCharacter() {
        const name = prompt('Enter character name:');
        if (!name) return;

        const rank = prompt('Enter rank (ENSIGN, LIEUTENANT, COMMANDER, CAPTAIN):', 'ENSIGN') || 'ENSIGN';
        const assignment = prompt('Enter assignment (COMMAND, SCIENCE, SECURITY, ENGINEERING, MEDICAL):', 'COMMAND') || 'COMMAND';

        try {
            const character = await this._fetch('/characters', {
                method: 'POST',
                body: { name, rank, assignment }
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

    // ── Mission Creation ──

    async _createMission() {
        if (!this.selectedCharacterId) {
            this.hudController.addEventLogEntry('Select a character first', 'warning');
            return;
        }

        const name = prompt('Enter mission name:');
        if (!name) return;

        const type = prompt('Mission type (EXPLORATION, DIPLOMACY, COMBAT, SCIENCE, RESCUE):', 'EXPLORATION') || 'EXPLORATION';

        try {
            const mission = await this._fetch('/missions', {
                method: 'POST',
                body: { name, type, characterId: this.selectedCharacterId }
            });
            if (mission) {
                this.missions.push(mission);
                this._renderMissionSelector();
                await this._selectMission(mission.id);
                this.hudController.addEventLogEntry(`Mission created: ${name}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create mission', 'danger');
        }
    }

    // ── Task Attempts ──

    async _attemptTask(attribute, discipline) {
        if (!this.selectedCharacterId || !this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a character and mission first', 'warning');
            return;
        }

        try {
            // Query suggested difficulty
            let difficulty = 11;
            try {
                const diffResult = await this._fetch(`/tasks/difficulty?missionId=${this.selectedMissionId}&characterId=${this.selectedCharacterId}`);
                if (diffResult && diffResult.difficulty) {
                    difficulty = diffResult.difficulty;
                }
            } catch (e) {
                // Use default difficulty
            }

            // Calculate boons from momentum
            const boons = this.currentMission && this.currentMission.momentum > 0 ? 1 : 0;

            // Attempt the task
            const result = await this._fetch('/tasks/attempt', {
                method: 'POST',
                body: {
                    characterId: this.selectedCharacterId,
                    missionId: this.selectedMissionId,
                    attribute,
                    discipline,
                    difficulty,
                    boons
                }
            });

            if (result) {
                // Display dice result
                const d1 = result.dice ? result.dice[0] : '?';
                const d2 = result.dice ? result.dice[1] : '?';
                const successes = result.successes || 0;
                const newMomentum = result.momentum || 0;
                const newThreat = result.threat || 0;
                const success = result.success || false;

                this.hudController.addDiceResult(d1, d2, difficulty, successes, success);

                if (success) {
                    this.hudController.addEventLogEntry(
                        `✅ Task succeeded! ${successes} success(es). Momentum: ${newMomentum}, Threat: ${newThreat}`,
                        'success'
                    );
                } else {
                    this.hudController.addEventLogEntry(
                        `❌ Task failed. ${successes} success(es). Momentum: ${newMomentum}, Threat: ${newThreat}`,
                        'danger'
                    );
                }

                // Update mission state
                if (this.currentMission) {
                    this.currentMission.momentum = newMomentum;
                    this.currentMission.threat = newThreat;
                    this._renderMissionStatus();
                }

                // Generate AI narrative
                this._generateNarrative(`Task attempt: ${attribute} + ${discipline}. Result: ${success ? 'SUCCESS' : 'FAILURE'}. ${successes} success(es). Momentum: ${newMomentum}, Threat: ${newThreat}.`);

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        success ? 'MISSION_COMPLETE' : 'COMBAT_LOSS',
                        'captains-log',
                        `${attribute} + ${discipline} task ${success ? 'succeeded' : 'failed'}`,
                        `${successes} success(es). Momentum: ${newMomentum}, Threat: ${newThreat}.`,
                        { attribute, discipline, successes, momentum: newMomentum, threat: newThreat }
                    );
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Task attempt failed', 'danger');
        }
    }

    // ── Oracle Queries ──

    async _queryOracle() {
        const question = prompt('Enter your yes/no question for the Oracle:');
        if (!question) return;

        const probability = prompt(
            'Probability level (VERY_UNLIKELY, UNLIKELY, FIFTY_FIFTY, LIKELY, VERY_LIKELY):',
            'FIFTY_FIFTY'
        ) || 'FIFTY_FIFTY';

        try {
            const result = await this._fetch(`/oracle/yesno?probability=${probability}`);
            if (result) {
                const answer = result.answer || result.outcome || 'Unknown';
                this.hudController.addEventLogEntry(
                    `🎲 Oracle: "${question}" → ${answer}`,
                    answer.toLowerCase().includes('yes') ? 'success' : 'warning'
                );

                // Generate AI narrative
                this._generateNarrative(`Oracle query: "${question}". Probability: ${probability}. Answer: ${answer}.`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Oracle query failed', 'danger');
        }
    }

    // ── Log Entry ──

    async _writeLogEntry() {
        if (!this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a mission first', 'warning');
            return;
        }

        const entry = prompt('Write your log entry:');
        if (!entry) return;

        try {
            const result = await this._fetch(`/missions/${this.selectedMissionId}/entries`, {
                method: 'POST',
                body: { text: entry }
            });

            if (result) {
                this.hudController.addEventLogEntry(`Log entry written`, 'success');
                // Update the narrative with the new entry
                if (this.currentMission) {
                    const actKey = `act${this.currentMission.currentAct}Log`;
                    const currentLog = this.currentMission[actKey] || '';
                    this.currentMission[actKey] = currentLog + '\n\n' + entry;
                    this._renderMissionNarrative();
                }
            }
        } catch (err) {
            // Fallback: update log directly on mission
            try {
                const act = this.currentMission ? this.currentMission.currentAct : 1;
                const currentLog = this.currentMission ? (this.currentMission[`act${act}Log`] || '') : '';
                await this._fetch(`/missions/${this.selectedMissionId}/log?act=${act}`, {
                    method: 'PUT',
                    body: { log: currentLog + '\n\n' + entry }
                });
                this.hudController.addEventLogEntry(`Log entry written`, 'success');
                this._renderMissionNarrative();
            } catch (err2) {
                this.hudController.addEventLogEntry('Failed to write log entry', 'danger');
            }
        }
    }

    // ── Beat Advancement ──

    async _advanceBeat() {
        if (!this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a mission first', 'warning');
            return;
        }

        try {
            const mission = await this._fetch(`/missions/${this.selectedMissionId}/advance-beat`, {
                method: 'PUT'
            });
            if (mission) {
                this.currentMission = mission;
                this.hudController.addEventLogEntry(
                    `Advanced to Beat ${mission.currentBeat || 1} of Act ${mission.currentAct || 1}`,
                    'success'
                );
                this._renderMissionStatus();
                this._renderMissionNarrative();
                this._renderTaskButtons();

                // Generate AI narrative
                this._generateNarrative(`Advanced to Beat ${mission.currentBeat || 1} of Act ${mission.currentAct || 1}. Continue the mission.`);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to advance beat', 'danger');
        }
    }

    // ── Mission Completion ──

    async _completeMission() {
        if (!this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a mission first', 'warning');
            return;
        }

        const confirmComplete = confirm('Complete this mission? This will finalize the current mission.');
        if (!confirmComplete) return;

        try {
            const mission = await this._fetch(`/missions/${this.selectedMissionId}/complete`, {
                method: 'PUT'
            });
            if (mission) {
                this.currentMission = mission;
                this.hudController.addEventLogEntry(
                    `Mission #${mission.missionNumber || '?'} completed!`,
                    'success'
                );
                this._renderMissionStatus();
                this._renderMissionNarrative();
                // Refresh character to update mission count
                if (this.selectedCharacterId) {
                    await this._selectCharacter(this.selectedCharacterId);
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to complete mission', 'danger');
        }
    }

    // ── Abstract Method Implementations ──

    async getGameState() {
        return {
            character: this.currentCharacter,
            mission: this.currentMission,
            starship: this.currentStarship
        };
    }

    async getNarrativePrompt() {
        if (this.currentMission) {
            const actKey = `act${this.currentMission.currentAct}Log`;
            return this.currentMission[actKey] || this.currentMission.summary || '';
        }
        return 'Select a mission to begin your adventure.';
    }

    async getAvailableActions() {
        return [
            { id: 'task-pilot', label: 'Pilot', icon: '✈️' },
            { id: 'task-navigate', label: 'Navigate', icon: '🧭' },
            { id: 'task-science', label: 'Science', icon: '🔬' },
            { id: 'task-security', label: 'Security', icon: '🔫' },
            { id: 'task-medical', label: 'Medical', icon: '🏥' },
            { id: 'task-engineering', label: 'Engineering', icon: '⚙️' },
            { id: 'task-diplomacy', label: 'Diplomacy', icon: '🤝' },
            { id: 'task-command', label: 'Command', icon: '👑' },
            { id: 'task-leadership', label: 'Leadership', icon: '📢' },
            { id: 'oracle-query', label: 'Oracle Query', icon: '🎲' },
            { id: 'log-entry', label: 'Log Entry', icon: '📝' },
            { id: 'advance-beat', label: 'Advance Beat', icon: '⏭️' }
        ];
    }

    async executeAction(actionId, params = {}) {
        return this._handleAction(actionId);
    }

    // ── AI Narrative ──

    _generateNarrative(context) {
        if (!window.AiNarrative) return;
        const sessionId = 'cl-narrative-' + (this.selectedCharacterId || 'default');
        const systemPrompt = window.AiNarrative.getSystemPrompt('captains-log');
        const characterName = this.currentCharacter ? this.currentCharacter.name : 'Captain';
        const missionName = this.currentMission ? this.currentMission.name || `Mission #${this.currentMission.missionNumber}` : 'Unknown Mission';
        const message = `Character: ${characterName}. Mission: ${missionName}. Act ${this.currentMission ? this.currentMission.currentAct : '?'}, Beat ${this.currentMission ? this.currentMission.currentBeat : '?'}. ${context}`;

        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        // Show loading indicator
        const loadingEl = document.createElement('div');
        loadingEl.className = 'hud-narrative-loading';
        loadingEl.textContent = '✨ Generating narrative...';
        loadingEl.style.cssText = 'color: #0d9488; font-style: italic; margin: 10px 0;';
        promptPanel.appendChild(loadingEl);

        window.AiNarrative.streamToPanel(
            sessionId,
            message,
            systemPrompt,
            null, // onChunk - skip for simplicity
            (fullText) => {
                // onComplete - replace loading with full text
                if (loadingEl.parentNode) loadingEl.remove();
                if (fullText) {
                    const narrativeEl = document.createElement('div');
                    narrativeEl.className = 'hud-narrative-text';
                    narrativeEl.style.cssText = 'color: #99f6e4; margin: 10px 0; padding: 10px; border-left: 3px solid #0d9488; background: rgba(13, 148, 136, 0.1);';
                    narrativeEl.textContent = fullText;
                    promptPanel.appendChild(narrativeEl);
                    promptPanel.scrollTop = promptPanel.scrollHeight;
                }
            },
            (err) => {
                // onError
                if (loadingEl.parentNode) loadingEl.remove();
                this.hudController.addEventLogEntry('AI narrative failed: ' + err, 'warning');
            }
        );
    }

    async generateNarrative() {
        if (!this.selectedCharacterId || !this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a character and mission first', 'warning');
            return;
        }
        const characterName = this.currentCharacter ? this.currentCharacter.name : 'Captain';
        const missionName = this.currentMission ? this.currentMission.name || `Mission #${this.currentMission.missionNumber}` : 'Unknown Mission';
        this._generateNarrative(`Continue the narrative for ${characterName} on ${missionName}. Describe what happens next.`);
    }
}

// Expose globally
window.CaptainsLogAdapter = CaptainsLogAdapter;

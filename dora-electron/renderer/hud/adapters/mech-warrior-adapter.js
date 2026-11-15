/* Mech Warrior Adapter — Connects HUD panels to Mech Warrior REST APIs */

class MechWarriorAdapter extends GameModuleAdapter {
    constructor(fpsBase, missionBase, enemyBase) {
        super(fpsBase);
        this.missionBase = missionBase;
        this.enemyBase = enemyBase;
        this.missions = [];
        this.selectedMissionId = null;
        this.currentMission = null;
        this.sessions = [];
        this.selectedSessionId = null;
        this.currentSession = null;
        this.selectedPilotId = null;
        this.currentPilot = null;
        this.mechStatus = null;
        this.enemies = [];
        this.pilotStats = null;
        this.currentView = 'mission'; // mission, session, combat
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Restore session state from localStorage
        const savedMissionId = localStorage.getItem('mw-mission-id');
        if (savedMissionId) {
            this.selectedMissionId = savedMissionId;
        }
        const savedSessionId = localStorage.getItem('mw-session-id');
        if (savedSessionId) {
            this.selectedSessionId = savedSessionId;
        }
        const savedPilotId = localStorage.getItem('mw-pilot-id');
        if (savedPilotId) {
            this.selectedPilotId = savedPilotId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'mech-warrior') {
                this._onTemplateLoaded();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'mech-warrior') {
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

        console.log('[MechWarriorAdapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._loadMissions();
    }

    destroy() {
        super.destroy();
    }

    // ── Action Handler ──

    _handleAction(action) {
        switch (action) {
            case 'action-browse-missions':
                this._loadMissions();
                break;
            case 'action-accept-mission':
                this._acceptMission();
                break;
            case 'action-start-mission':
                this._startMission();
                break;
            case 'action-fire-ll':
                this._fireWeapon('LL');
                break;
            case 'action-fire-lr':
                this._fireWeapon('LR');
                break;
            case 'action-fire-rl':
                this._fireWeapon('RL');
                break;
            case 'action-fire-erppc':
                this._fireWeapon('ER_PPC');
                break;
            case 'action-fire-lrm':
                this._fireWeapon('LRM');
                break;
            case 'action-fire-ac20':
                this._fireWeapon('AC20');
                break;
            case 'action-jump-jets':
                this._useJumpJets();
                break;
            case 'action-heat-override':
                this._toggleHeatOverride();
                break;
            case 'action-face-left':
                this._faceDirection('left');
                break;
            case 'action-face-right':
                this._faceDirection('right');
                break;
            case 'action-walk-run':
                this._toggleSpeedMode();
                break;
            case 'action-process-tick':
                this._processTick();
                break;
            case 'action-complete-mission':
                this._completeMission();
                break;
            case 'action-abandon-mission':
                this._abandonMission();
                break;
        }
    }

    // ── AI Narrative ──

    _generateNarrative(context) {
        if (!window.AiNarrative) return;
        const sessionId = 'mw-narrative-' + (this.selectedSessionId || 'default');
        const systemPrompt = window.AiNarrative.getSystemPrompt('mech-warrior');
        const missionName = this.currentMission ? this.currentMission.name : 'Unknown Mission';
        const mechName = this.mechStatus ? this.mechStatus.mechName : 'Mech';
        const message = `Mission: ${missionName}. Mech: ${mechName}. ${this.mechStatus ? 'Heat: ' + this.mechStatus.heat + '%. Armor: ' + this.mechStatus.armor + '%.' : ''} ${context}`;

        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        const loadingEl = document.createElement('div');
        loadingEl.className = 'hud-narrative-loading';
        loadingEl.textContent = '✨ Generating narrative...';
        loadingEl.style.cssText = 'color: #ea580c; font-style: italic; margin: 10px 0;';
        promptPanel.appendChild(loadingEl);

        window.AiNarrative.streamToPanel(
            sessionId, message, systemPrompt,
            null,
            (fullText) => {
                if (loadingEl.parentNode) loadingEl.remove();
                if (fullText) {
                    const narrativeEl = document.createElement('div');
                    narrativeEl.className = 'hud-narrative-text';
                    narrativeEl.style.cssText = 'color: #fdba74; margin: 10px 0; padding: 10px; border-left: 3px solid #ea580c; background: rgba(234, 88, 12, 0.1);';
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
        if (!this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a mission first', 'warning');
            return;
        }
        this._generateNarrative('Describe the current battlefield situation and what happens next.');
    }

    // ── Commands ──

    _handleCommand(command) {
        if (!command || !command.trim()) return;
        const cmd = command.trim().toLowerCase();
        this.hudController.addEventLogEntry(`Command: "${command}"`, 'info');

        if (cmd === 'status') {
            this._showStatus();
        } else if (cmd === 'mech') {
            this._showMechStatus();
        } else if (cmd === 'enemies') {
            this._showEnemies();
        } else if (cmd === 'missions') {
            this._loadMissions();
        } else if (cmd.startsWith('fire ')) {
            const weapon = command.trim().substring(5).toUpperCase();
            this._fireWeapon(weapon);
        } else {
            this.hudController.addEventLogEntry(`Unknown command: "${command}". Try: status, mech, enemies, missions, fire <weapon>`, 'warning');
        }
    }

    _showStatus() {
        if (this.currentMission) {
            this.hudController.addEventLogEntry(`Mission: ${this.currentMission.name || 'Unknown'} (${this.currentMission.difficulty || 'Unknown'})`, 'info');
        }
        if (this.currentSession) {
            this.hudController.addEventLogEntry(`Session: Active`, 'info');
        }
        if (this.mechStatus) {
            this.hudController.addEventLogEntry(`Mech: Heat ${this.mechStatus.heat || 0}%, Armor ${this.mechStatus.armor || 0}%, Fuel ${this.mechStatus.fuel || 0}%`, 'info');
        }
    }

    _showMechStatus() {
        if (!this.selectedSessionId || !this.selectedPilotId) {
            this.hudController.addEventLogEntry('Join a session first', 'warning');
            return;
        }
        this._getMechStatus();
    }

    _showEnemies() {
        if (!this.selectedSessionId) {
            this.hudController.addEventLogEntry('Start a mission first', 'warning');
            return;
        }
        this._getEnemies();
    }

    // ── Mission Management ──

    async _loadMissions() {
        try {
            this.missions = await this._fetchFromMission('/missions/all') || [];
            this._renderMissionSelector();
            if (this.selectedMissionId) {
                await this._selectMission(this.selectedMissionId);
            } else if (this.missions.length > 0) {
                await this._selectMission(this.missions[0].id);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load missions: ' + err.message, 'danger');
            this._renderMissionSelector();
        }
    }

    async _selectMission(missionId) {
        try {
            this.selectedMissionId = missionId;
            localStorage.setItem('mw-mission-id', missionId);
            this.currentMission = await this._fetchFromMission(`/missions/${missionId}`);
            if (this.currentMission) {
                this._renderMissionInfo();
                this._renderPromptPanel();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load mission details: ' + err.message, 'danger');
        }
    }

    async _acceptMission() {
        if (!this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a mission first', 'warning');
            return;
        }
        try {
            const pilotId = this.selectedPilotId || 'player-1';
            await this._fetchFromMission(`/missions/${this.selectedMissionId}/accept?pilotId=${pilotId}`, {
                method: 'POST'
            });
            this.hudController.addEventLogEntry(`Mission accepted: ${this.currentMission?.name || 'Unknown'}`, 'success');
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to accept mission: ' + err.message, 'danger');
        }
    }

    async _startMission() {
        if (!this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a mission first', 'warning');
            return;
        }
        try {
            const pilotId = this.selectedPilotId || 'player-1';
            const result = await this._fetchFromMission(`/missions/${this.selectedMissionId}/start?pilotId=${pilotId}`, {
                method: 'POST'
            });
            if (result) {
                this.selectedSessionId = result.sessionId || result.id;
                localStorage.setItem('mw-session-id', this.selectedSessionId);
                this.hudController.addEventLogEntry(`Mission started! Session: ${this.selectedSessionId}`, 'success');
                await this._addPilot();
                await this._spawnEnemies();
                this._renderSessionInfo();
                this._renderCombatActions();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to start mission: ' + err.message, 'danger');
        }
    }

    async _completeMission() {
        if (!this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a mission first', 'warning');
            return;
        }
        try {
            const pilotId = this.selectedPilotId || 'player-1';
            await this._fetchFromMission(`/missions/${this.selectedMissionId}/complete?pilotId=${pilotId}`, {
                method: 'POST'
            });
            this.hudController.addEventLogEntry(`Mission completed: ${this.currentMission?.name || 'Unknown'}`, 'success');
            this._resetSession();
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to complete mission: ' + err.message, 'danger');
        }
    }

    async _abandonMission() {
        if (!this.selectedMissionId) {
            this.hudController.addEventLogEntry('Select a mission first', 'warning');
            return;
        }
        try {
            const pilotId = this.selectedPilotId || 'player-1';
            await this._fetchFromMission(`/missions/${this.selectedMissionId}/abandon?pilotId=${pilotId}`, {
                method: 'POST'
            });
            this.hudController.addEventLogEntry(`Mission abandoned: ${this.currentMission?.name || 'Unknown'}`, 'warning');
            this._resetSession();
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to abandon mission: ' + err.message, 'danger');
        }
    }

    _resetSession() {
        this.selectedSessionId = null;
        this.selectedPilotId = null;
        this.currentSession = null;
        this.mechStatus = null;
        this.enemies = [];
        localStorage.removeItem('mw-session-id');
        localStorage.removeItem('mw-pilot-id');
        this._renderMissionSelector();
        this._renderPromptPanel();
    }

    // ── Session Management ──

    async _addPilot() {
        if (!this.selectedSessionId) return;
        try {
            const pilotData = {
                pilotId: this.selectedPilotId || 'player-1',
                mechClass: 'Light',
                mechName: 'Griffin',
                weapons: [
                    { location: 'LL', weaponType: 'LRM_5' },
                    { location: 'LR', weaponType: 'ER_PPC' },
                    { location: 'RL', weaponType: 'AC_2' }
                ]
            };
            await this._fetch(`/sessions/${this.selectedSessionId}/pilots`, {
                method: 'POST',
                body: JSON.stringify(pilotData)
            });
            this.selectedPilotId = pilotData.pilotId;
            localStorage.setItem('mw-pilot-id', this.selectedPilotId);
            this.hudController.addEventLogEntry(`Pilot added: ${pilotData.mechName}`, 'success');
            await this._getMechStatus();
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to add pilot: ' + err.message, 'danger');
        }
    }

    async _getMechStatus() {
        if (!this.selectedSessionId || !this.selectedPilotId) return;
        try {
            this.mechStatus = await this._fetch(`/sessions/${this.selectedSessionId}/pilots/${this.selectedPilotId}/mech`);
            if (this.mechStatus) {
                this._renderMechStatus();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to get mech status: ' + err.message, 'danger');
        }
    }

    async _getPilotStats() {
        if (!this.selectedSessionId || !this.selectedPilotId) return;
        try {
            this.pilotStats = await this._fetch(`/sessions/${this.selectedSessionId}/pilots/${this.selectedPilotId}/stats`);
            if (this.pilotStats) {
                this.hudController.addEventLogEntry(`Pilot stats: Kills ${this.pilotStats.kills || 0}, Damage ${this.pilotStats.damageDealt || 0}`, 'info');
            }
        } catch (err) {
            // Stats may not be available yet
        }
    }

    // ── Combat Actions ──

    async _fireWeapon(weaponType) {
        if (!this.selectedSessionId) {
            this.hudController.addEventLogEntry('Start a mission first', 'warning');
            return;
        }
        try {
            const fireData = {
                pilotId: this.selectedPilotId || 'player-1',
                weaponType: weaponType,
                targetLocation: 'Center_Torso',
                targetPilotId: 'enemy-1'
            };
            const result = await this._fetch(`/sessions/${this.selectedSessionId}/combat/fire`, {
                method: 'POST',
                body: JSON.stringify(fireData)
            });
            if (result) {
                const hitMsg = result.hit ? 'HIT!' : 'MISS';
                const dmg = result.damage || 0;
                this.hudController.addEventLogEntry(`Fired ${weaponType}: ${hitMsg} (${dmg} damage)`, result.hit ? 'success' : 'warning');
                if (result.hit) {
                    this.hudController.addDiceResult({
                        dice: [weaponType],
                        total: dmg,
                        target: 0,
                        success: true,
                        narrative: `${weaponType} hits for ${dmg} damage!`
                    });
                }
                await this._getMechStatus();
                await this._getEnemies();

                // Generate AI narrative
                this._generateNarrative(`Fired ${weaponType} at enemy: ${result.hit ? 'HIT' : 'MISS'} for ${dmg} damage.`);

                // Publish cross-module event
                if (window.UnifiedWorld) {
                    window.UnifiedWorld._publishEvent(
                        result.hit ? 'COMBAT_WIN' : 'COMBAT_LOSS',
                        'mechwarrior',
                        `Fired ${weaponType}: ${result.hit ? 'HIT' : 'MISS'}`,
                        `${dmg} damage.`,
                        { weaponType, hit: result.hit, damage: dmg }
                    );
                }
            }
        } catch (err) {
            this.hudController.addEventLogEntry(`Failed to fire ${weaponType}: ${err.message}`, 'danger');
        }
    }

    async _useJumpJets() {
        if (!this.selectedSessionId) {
            this.hudController.addEventLogEntry('Start a mission first', 'warning');
            return;
        }
        try {
            const jumpData = {
                pilotId: this.selectedPilotId || 'player-1',
                distance: 10
            };
            await this._fetch(`/sessions/${this.selectedSessionId}/movement/jump`, {
                method: 'POST',
                body: JSON.stringify(jumpData)
            });
            this.hudController.addEventLogEntry('Jump jets activated! Moved 10m', 'success');
            await this._getMechStatus();
        } catch (err) {
            this.hudController.addEventLogEntry('Jump jets failed: ' + err.message, 'danger');
        }
    }

    async _toggleHeatOverride() {
        if (!this.selectedSessionId || !this.selectedPilotId) {
            this.hudController.addEventLogEntry('Join a session first', 'warning');
            return;
        }
        try {
            await this._fetch(`/sessions/${this.selectedSessionId}/pilots/${this.selectedPilotId}/heat/override`, {
                method: 'POST'
            });
            this.hudController.addEventLogEntry('Heat override toggled!', 'warning');
            await this._getMechStatus();
        } catch (err) {
            this.hudController.addEventLogEntry('Heat override failed: ' + err.message, 'danger');
        }
    }

    async _faceDirection(direction) {
        if (!this.selectedSessionId) {
            this.hudController.addEventLogEntry('Start a mission first', 'warning');
            return;
        }
        this.hudController.addEventLogEntry(`Facing ${direction}`, 'info');
        // Facing is handled by the server on next tick
    }

    async _toggleSpeedMode() {
        if (!this.selectedSessionId) {
            this.hudController.addEventLogEntry('Start a mission first', 'warning');
            return;
        }
        this.hudController.addEventLogEntry('Speed mode toggled', 'info');
    }

    async _processTick() {
        if (!this.selectedSessionId) {
            this.hudController.addEventLogEntry('Start a mission first', 'warning');
            return;
        }
        try {
            await this._fetch(`/sessions/${this.selectedSessionId}/tick`, {
                method: 'POST'
            });
            this.hudController.addEventLogEntry('Game tick processed', 'info');
            await this._getMechStatus();
            await this._getEnemies();

            // Generate AI narrative
            this._generateNarrative('Game tick processed. Describe the current battlefield state and any changes.');

            // Publish cross-module event
            if (window.UnifiedWorld) {
                window.UnifiedWorld._publishEvent(
                    'COMBAT_WIN',
                    'mechwarrior',
                    'Game tick processed',
                    'Battlefield state updated.',
                    { sessionId: this.selectedSessionId }
                );
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Tick processing failed: ' + err.message, 'danger');
        }
    }

    // ── Enemy Management ──

    async _spawnEnemies() {
        if (!this.selectedSessionId || !this.selectedMissionId) return;
        try {
            this.enemies = await this._fetchFromEnemy(`/enemies/spawn?sessionId=${this.selectedSessionId}&missionId=${this.selectedMissionId}`, {
                method: 'POST'
            });
            if (this.enemies && this.enemies.length > 0) {
                this.hudController.addEventLogEntry(`${this.enemies.length} enemies spawned!`, 'warning');
                this._renderEnemyAlerts();
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to spawn enemies: ' + err.message, 'danger');
        }
    }

    async _getEnemies() {
        if (!this.selectedSessionId) return;
        try {
            this.enemies = await this._fetchFromEnemy(`/enemies?sessionId=${this.selectedSessionId}`) || [];
            if (this.enemies.length > 0) {
                this._renderEnemyAlerts();
            }
        } catch (err) {
            // Enemies may not be available
        }
    }

    // ── API Helper for Mission Base ──

    async _fetchFromMission(path, options = {}) {
        const url = this.missionBase + path;
        const token = localStorage.getItem('auth-token') || '';
        const headers = {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        };
        const response = await fetch(url, { ...options, headers });
        if (response.status === 204) return null;
        if (!response.ok) throw new Error(`${response.status} ${response.statusText}`);
        return response.json();
    }

    // ── API Helper for Enemy Base ──

    async _fetchFromEnemy(path, options = {}) {
        const url = this.enemyBase + path;
        const token = localStorage.getItem('auth-token') || '';
        const headers = {
            'Content-Type': 'application/json',
            ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        };
        const response = await fetch(url, { ...options, headers });
        if (response.status === 204) return null;
        if (!response.ok) throw new Error(`${response.status} ${response.statusText}`);
        return response.json();
    }

    // ── Panel Rendering ──

    _renderMissionSelector() {
        const infoPanel = document.getElementById('hudInfoPanelBody');
        if (!infoPanel) return;

        let html = `
            <div class="hud-section">
                <h4 class="hud-section-title">📋 Missions</h4>
                <select id="mwMissionSelect" class="hud-selector">
        `;

        if (this.missions.length === 0) {
            html += `<option value="">No missions available</option>`;
        } else {
            this.missions.forEach(mission => {
                const selected = mission.id === this.selectedMissionId ? 'selected' : '';
                html += `<option value="${mission.id}" ${selected}>${mission.name || 'Unknown'} (${mission.difficulty || 'Unknown'})</option>`;
            });
        }

        html += `</select>`;

        if (this.currentMission) {
            html += `
                <div class="hud-character-sheet" style="margin-top:10px;">
                    <div class="hud-character-name">${this.currentMission.name || 'Unknown Mission'}</div>
                    <div class="hud-character-details">
                        <div class="hud-character-stat">
                            <span class="hud-character-stat-label">Difficulty:</span>
                            <span class="hud-character-stat-value">${this.currentMission.difficulty || 'Unknown'}</span>
                        </div>
                        <div class="hud-character-stat">
                            <span class="hud-character-stat-label">Type:</span>
                            <span class="hud-character-stat-value">${this.currentMission.type || 'Unknown'}</span>
                        </div>
                        <div class="hud-character-stat">
                            <span class="hud-character-stat-label">Status:</span>
                            <span class="hud-character-stat-value">${this.currentMission.status || 'Available'}</span>
                        </div>
                    </div>
                    ${this.currentMission.description ? `<p class="hud-narrative-text" style="margin-top:8px;font-size:0.85em;">${this.currentMission.description}</p>` : ''}
                </div>
            `;
        }

        html += `</div>`;
        infoPanel.innerHTML = html;

        const select = document.getElementById('mwMissionSelect');
        if (select) {
            select.addEventListener('change', (e) => {
                this._selectMission(e.target.value);
            });
        }
    }

    _renderMissionInfo() {
        this._renderMissionSelector();
    }

    _renderSessionInfo() {
        const infoPanel = document.getElementById('hudInfoPanelBody');
        if (!infoPanel) return;

        let html = `
            <div class="hud-section">
                <h4 class="hud-section-title">🤖 Mech Status</h4>
                <div class="hud-character-sheet">
                    <div class="hud-character-name">${this.currentMission?.name || 'Mission Active'}</div>
                    <div class="hud-character-details">
                        <div class="hud-character-stat">
                            <span class="hud-character-stat-label">Session:</span>
                            <span class="hud-character-stat-value">${this.selectedSessionId || 'N/A'}</span>
                        </div>
                        <div class="hud-character-stat">
                            <span class="hud-character-stat-label">Pilot:</span>
                            <span class="hud-character-stat-value">${this.selectedPilotId || 'N/A'}</span>
                        </div>
                    </div>
                </div>
        `;

        if (this.mechStatus) {
            html += `
                <div style="margin-top:10px;">
                    <div class="hud-stat-bar">
                        <div class="hud-stat-bar-label">🔥 Heat</div>
                        <div class="hud-stat-bar-track">
                            <div class="hud-stat-bar-fill" style="width:${this.mechStatus.heat || 0}%;background:${this.mechStatus.heat > 80 ? '#ef4444' : '#ea580c'};"></div>
                        </div>
                        <div class="hud-stat-bar-value">${this.mechStatus.heat || 0}%</div>
                    </div>
                    <div class="hud-stat-bar">
                        <div class="hud-stat-bar-label">🛡️ Armor</div>
                        <div class="hud-stat-bar-track">
                            <div class="hud-stat-bar-fill" style="width:${this.mechStatus.armor || 100}%;"></div>
                        </div>
                        <div class="hud-stat-bar-value">${this.mechStatus.armor || 100}%</div>
                    </div>
                    <div class="hud-stat-bar">
                        <div class="hud-stat-bar-label">⛽ Fuel</div>
                        <div class="hud-stat-bar-track">
                            <div class="hud-stat-bar-fill" style="width:${this.mechStatus.fuel || 100}%;"></div>
                        </div>
                        <div class="hud-stat-bar-value">${this.mechStatus.fuel || 100}%</div>
                    </div>
                </div>
            `;
        }

        // Armor grid
        if (this.mechStatus?.armorLocations) {
            html += `<div style="margin-top:10px;"><strong>Armor Integrity:</strong><div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:4px;margin-top:4px;">`;
            const locations = ['Head', 'CT', 'LT', 'RT', 'LL', 'RL'];
            locations.forEach(loc => {
                const val = this.mechStatus.armorLocations[loc] || 0;
                const max = 100;
                html += `<div class="hud-stat-bar"><div class="hud-stat-bar-label" style="font-size:0.7em;">${loc}</div><div class="hud-stat-bar-track"><div class="hud-stat-bar-fill" style="width:${(val/max)*100}%;"></div></div></div>`;
            });
            html += `</div></div>`;
        }

        html += `</div>`;
        infoPanel.innerHTML = html;
    }

    _renderMechStatus() {
        this._renderSessionInfo();
    }

    _renderPromptPanel() {
        const promptPanel = document.getElementById('hudPromptPanelBody');
        if (!promptPanel) return;

        let html = '';

        if (this.currentMission) {
            html += `
                <div class="hud-section">
                    <h4 class="hud-section-title">📜 Mission Briefing</h4>
                    <div class="hud-narrative-text">
                        <p><strong>${this.currentMission.name || 'Unknown'}</strong></p>
                        <p>${this.currentMission.description || 'No briefing available.'}</p>
            `;

            if (this.currentMission.objectives && this.currentMission.objectives.length > 0) {
                html += `<p style="margin-top:8px;"><strong>Objectives:</strong></p><ul>`;
                this.currentMission.objectives.forEach(obj => {
                    html += `<li>${obj.description || obj.name || 'Unknown objective'}</li>`;
                });
                html += `</ul>`;
            }

            html += `</div></div>`;
        }

        if (this.enemies && this.enemies.length > 0) {
            html += `
                <div class="hud-section" style="margin-top:10px;">
                    <h4 class="hud-section-title">⚠️ Enemy Alerts</h4>
                    <div class="hud-narrative-text">
            `;
            this.enemies.forEach(enemy => {
                html += `<p style="color:#ef4444;">🔴 ${enemy.mechName || 'Enemy Mech'} (${enemy.mechClass || 'Unknown'}) - ${enemy.status || 'Active'}</p>`;
            });
            html += `</div></div>`;
        }

        if (!html) {
            html = `<div class="hud-section"><div class="hud-narrative-text"><p>Select a mission to begin...</p></div></div>`;
        }

        promptPanel.innerHTML = html;
    }

    _renderEnemyAlerts() {
        this._renderPromptPanel();
    }

    _renderCombatActions() {
        const responsesPanel = document.getElementById('hudResponsesPanelBody');
        if (!responsesPanel) return;

        // The action buttons are already rendered by the template; just ensure they're visible
        this._renderPromptPanel();
    }

    _renderBattleFeed(text, type = 'info') {
        this.hudController.addEventLogEntry(text, type);
    }
}

// Expose globally
window.MechWarriorAdapter = MechWarriorAdapter;

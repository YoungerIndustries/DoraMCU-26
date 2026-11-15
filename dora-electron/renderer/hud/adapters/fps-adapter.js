/* FPS Adapter — Connects HUD panels to the FPS game via iframe and REST API */

class FpsAdapter extends GameModuleAdapter {
    constructor(apiBase) {
        super(apiBase);
        this.matches = [];
        this.currentMatchId = null;
        this.currentMatch = null;

        // Iframe reference
        this.iframe = null;
        this.iframeReady = false;

        // Game state (received from iframe via postMessage)
        this.localPlayer = null;
        this.matchInfo = null;
        this.recentEvents = [];
        this.maxEvents = 50;
        this.capturePoints = [];
        this.teamScores = {};
        this.players = [];

        // AI Inference integration
        this.aiAdapter = null;
        this.aiEnabled = true;

        // Weapon state (received from iframe)
        this.weapons = [
            { name: 'Pistol', damage: 25, fireRate: 4, reloadTime: 1.5, magSize: 12, reserveAmmo: 60 },
            { name: 'Assault Rifle', damage: 20, fireRate: 10, reloadTime: 2.5, magSize: 30, reserveAmmo: 120 },
            { name: 'Shotgun', damage: 50, fireRate: 1.5, reloadTime: 3, magSize: 6, reserveAmmo: 30 },
            { name: 'Machine Gun', damage: 15, fireRate: 15, reloadTime: 3.5, magSize: 50, reserveAmmo: 200 },
            { name: 'Sniper', damage: 80, fireRate: 1, reloadTime: 3, magSize: 5, reserveAmmo: 20 },
            { name: 'Rocket Launcher', damage: 100, fireRate: 0.5, reloadTime: 4, magSize: 2, reserveAmmo: 8 }
        ];
        this.currentWeaponIndex = 0;
        this.currentAmmo = 12;
        this.currentReserve = 60;

        // Server info for iframe URL
        this.serverHost = null;
        this.serverPort = null;
    }

    // ── Initialization ──

    init(hudController) {
        super.init(hudController);

        // Extract server host/port from API base URL
        try {
            const url = new URL(this.apiBase);
            this.serverHost = url.hostname;
            this.serverPort = url.port || (url.protocol === 'https:' ? '443' : '80');
        } catch (e) {
            this.serverHost = 'localhost';
            this.serverPort = '8080';
        }

        // Restore session state from localStorage
        const savedMatchId = localStorage.getItem('fps-match-id');
        if (savedMatchId) {
            this.currentMatchId = savedMatchId;
        }

        // Listen for template changes
        document.addEventListener('hud-template-changed', (e) => {
            if (e.detail.templateId === 'fps') {
                this._onTemplateLoaded();
            } else {
                // Remove iframe when switching away from FPS
                this._removeIframe();
            }
        });

        // Listen for action button clicks
        document.addEventListener('click', (e) => {
            const btn = e.target.closest('.hud-action-button');
            if (!btn) return;
            const action = btn.dataset.action;
            if (action && this.currentTemplateId === 'fps') {
                this._handleAction(action);
            }
        });

        // Listen for match selector change
        const matchSelector = document.getElementById('hudMatchSelector');
        if (matchSelector) {
            matchSelector.addEventListener('change', (e) => {
                this._selectMatch(e.target.value);
            });
        }

        // Listen for team chat submit
        const chatInput = document.querySelector('.hud-chat-input');
        if (chatInput) {
            const input = chatInput.querySelector('input');
            const button = chatInput.querySelector('button');
            if (input && button) {
                const sendChat = () => {
                    const msg = input.value.trim();
                    if (msg) {
                        this._sendChat(msg);
                        input.value = '';
                    }
                };
                button.addEventListener('click', sendChat);
                input.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter') sendChat();
                });
            }
        }

        console.log('[FpsAdapter] Initialized');
    }

    get currentTemplateId() {
        return this.hudController ? this.hudController.currentTemplateId : null;
    }

    _onTemplateLoaded() {
        this._createIframe();
        this._loadMatches();
    }

    destroy() {
        this._removeIframe();
        super.destroy();
    }

    // ── Iframe Lifecycle ──

    _createIframe() {
        const view3D = document.getElementById('hud3DView');
        if (!view3D) return;

        // Remove existing iframe
        this._removeIframe();

        // Show loading indicator
        const loadingEl = document.getElementById('hud3DLoading');
        if (loadingEl) loadingEl.style.display = 'flex';

        // Hide placeholder
        const placeholder = document.getElementById('hud3DPlaceholder');
        if (placeholder) placeholder.style.display = 'none';

        // Create iframe
        this.iframe = document.createElement('iframe');
        this.iframe.sandbox = 'allow-same-origin allow-scripts allow-forms allow-popups';
        this.iframe.style.cssText = 'position:absolute;top:0;left:0;width:100%;height:100%;border:none;';

        // Build iframe URL
        let protocol = window.location.protocol === 'https:' ? 'https:' : 'http:';
        let host = this.serverHost;
        let port = this.serverPort;
        if (this.serverHost === 'localhost' || this.serverHost === window.location.hostname) {
            host = window.location.hostname;
            port = window.location.port || (window.location.protocol === 'https:' ? '443' : '80');
        }
        // Skip port 80/443 in URL
        const portStr = (port && port !== '80' && port !== '443') ? `:${port}` : '';
        this.iframe.src = `${protocol}//${host}${portStr}/dora-pwa/fps/index.html`;

        // Append to container
        let container = document.getElementById('hud3DIframeContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'hud3DIframeContainer';
            container.style.cssText = 'position:absolute;top:0;left:0;width:100%;height:100%;';
            view3D.appendChild(container);
        }
        container.appendChild(this.iframe);

        // Setup listeners
        this._setupIframeListeners();

        // Notify resize after load
        this.iframe.addEventListener('load', () => {
            this.iframeReady = true;
            if (loadingEl) loadingEl.style.display = 'none';
            this._notifyIframeResize();

            // If we have a saved match, join it
            if (this.currentMatchId) {
                this._sendToIframe({
                    type: 'HUD_MATCH_JOIN',
                    matchId: this.currentMatchId
                });
            }
        });
    }

    _removeIframe() {
        if (this.iframe) {
            this.iframe.src = 'about:blank';
            const parent = this.iframe.parentNode;
            if (parent) parent.removeChild(this.iframe);
            this.iframe = null;
        }
        this.iframeReady = false;

        // Show placeholder
        const placeholder = document.getElementById('hud3DPlaceholder');
        if (placeholder) placeholder.style.display = '';
    }

    _setupIframeListeners() {
        window.addEventListener('message', (e) => {
            if (!e.data || !e.data.type) return;

            switch (e.data.type) {
                case 'FPS_STATE_UPDATE':
                    this._handleStateUpdate(e.data);
                    break;
                case 'FPS_EVENT':
                    this._handleGameEvent(e.data.event);
                    break;
                case 'FPS_MATCH_CREATED':
                    this.currentMatchId = e.data.matchId;
                    localStorage.setItem('fps-match-id', e.data.matchId);
                    this.hudController.addEventLogEntry('Match created', 'success');
                    break;
                case 'FPS_MATCH_JOINED':
                    this.currentMatchId = e.data.matchId;
                    localStorage.setItem('fps-match-id', e.data.matchId);
                    this.hudController.addEventLogEntry('Joined match', 'success');
                    break;
                case 'FPS_MATCH_LEFT':
                    this.currentMatchId = null;
                    localStorage.removeItem('fps-match-id');
                    this.hudController.addEventLogEntry('Left match', 'info');
                    this._renderMatchSelector();
                    this._clearPanels();
                    break;
                case 'FPS_ERROR':
                    this.hudController.addEventLogEntry(e.data.message, 'danger');
                    break;
            }
        });
    }

    _sendToIframe(data) {
        if (this.iframe && this.iframeReady) {
            this.iframe.contentWindow.postMessage(data, '*');
        }
    }

    _notifyIframeResize() {
        const view3D = document.getElementById('hud3DView');
        if (view3D && this.iframe && this.iframeReady) {
            const rect = view3D.getBoundingClientRect();
            this._sendToIframe({
                type: 'HUD_RESIZE',
                width: rect.width,
                height: rect.height
            });
        }
    }

    // ── State Handling (from iframe) ──

    _handleStateUpdate(data) {
        this.localPlayer = {
            health: data.health,
            shield: data.shield,
            armor: data.armor,
            score: data.score,
            kills: data.kills,
            deaths: data.deaths,
            name: 'Player'
        };
        this.currentWeaponIndex = data.weaponIndex;
        this.currentAmmo = data.ammo;
        this.currentReserve = data.reserveAmmo;
        this.matchInfo = data.matchInfo;
        this.teamScores = data.teamScores || {};
        this.capturePoints = data.capturePoints || [];
        this.players = [{
            id: 'local',
            name: 'Player',
            score: data.score,
            kills: data.kills,
            deaths: data.deaths
        }];

        this._renderPlayerStatus();
        this._renderWeaponPanel();
        this._renderScoreboard();
    }

    _handleGameEvent(evt) {
        this.recentEvents.push(evt);
        while (this.recentEvents.length > this.maxEvents) {
            this.recentEvents.shift();
        }

        if (evt.type === 'KILL') {
            const killer = this.state?.getPlayer?.(evt.playerId);
            const victim = this.state?.getPlayer?.(evt.targetId);
            this.hudController.addEventLogEntry(
                `${killer?.name || evt.playerId || 'Unknown'} eliminated ${victim?.name || evt.targetId || 'Unknown'}`,
                'warning'
            );
        } else if (evt.type === 'HIT') {
            this.hudController.addEventLogEntry(
                `${evt.shooterName || 'Unknown'} hit ${evt.targetName || 'Unknown'}`,
                'info'
            );
        }

        this._renderKillFeed();
    }

    // ── Match Management (REST API) ──

    async _loadMatches() {
        try {
            this.matches = await this._fetch('/matches') || [];
            this._renderMatchSelector();

            // If we have a saved match, try to reconnect
            if (this.currentMatchId) {
                await this._selectMatch(this.currentMatchId);
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to load matches', 'danger');
            this._renderMatchSelector();
        }
    }

    _renderMatchSelector() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body) return;

        let selectorHtml = `<select class="hud-selector" id="hudMatchSelector">
            <option value="">Select Match...</option>
            ${this.matches.map(m => `
                <option value="${m.id}" ${m.id === this.currentMatchId ? 'selected' : ''}>
                    ${m.mapName || m.name || 'Unknown Map'} (${m.mode || 'TDM'}) — ${m.playerCount || 0} players
                </option>
            `).join('')}
        </select>`;

        selectorHtml += `<button class="hud-action-button" data-action="create-match" style="margin-bottom:10px;">
            <span class="action-icon">➕</span>
            <span class="action-label">Create New Match</span>
        </button>`;

        const existing = body.querySelector('#hudMatchSelector');
        if (existing) {
            existing.parentElement.outerHTML = selectorHtml;
        } else {
            body.insertAdjacentHTML('afterbegin', selectorHtml);
        }

        const newSelector = document.getElementById('hudMatchSelector');
        if (newSelector) {
            newSelector.addEventListener('change', (e) => {
                this._selectMatch(e.target.value);
            });
        }
    }

    async _selectMatch(matchId) {
        if (!matchId) return;
        this.currentMatchId = matchId;
        localStorage.setItem('fps-match-id', matchId);

        try {
            // Join via REST API
            await this._fetch(`/matches/${matchId}/join`, {
                method: 'POST'
            });

            this.hudController.addEventLogEntry('Joined match', 'success');

            // Tell iframe to join
            this._sendToIframe({
                type: 'HUD_MATCH_JOIN',
                matchId: matchId
            });

            this._renderPlayerStatus();
            this._renderKillFeed();
            this._renderScoreboard();
            this._renderWeaponPanel();

        } catch (err) {
            this.hudController.addEventLogEntry('Failed to join match', 'danger');
        }
    }

    async _createMatch() {
        const mode = prompt('Match mode (TDM, CTP, FFA):', 'TDM');
        if (!mode) return;

        try {
            // Create via REST API
            const match = await this._fetch('/matches', {
                method: 'POST',
                body: { mode: mode.toUpperCase(), mapId: 'default' }
            });

            if (match) {
                this.matches.push(match);
                this._renderMatchSelector();

                // Tell iframe to create and join
                this._sendToIframe({
                    type: 'HUD_MATCH_CREATE',
                    mode: mode.toUpperCase(),
                    map: 'default'
                });

                this.currentMatchId = match.id;
                localStorage.setItem('fps-match-id', match.id);
                this.hudController.addEventLogEntry(`Match created: ${mode.toUpperCase()}`, 'success');
            }
        } catch (err) {
            this.hudController.addEventLogEntry('Failed to create match', 'danger');
        }
    }

    async _leaveMatch() {
        this.currentMatchId = null;
        this.localPlayer = null;
        this.players = [];
        this.matchInfo = null;
        this.recentEvents = [];
        localStorage.removeItem('fps-match-id');

        // Tell iframe to leave
        this._sendToIframe({ type: 'HUD_MATCH_LEAVE' });

        this.hudController.addEventLogEntry('Left match', 'info');
        this._renderMatchSelector();
        this._renderKillFeed();
        this._renderScoreboard();
        this._clearPanels();
    }

    // ── Panel Rendering ──

    _renderPlayerStatus() {
        const body = document.getElementById('hudInfoPanelBody');
        if (!body || !this.localPlayer) return;

        const p = this.localPlayer;
        const healthPercent = Math.max(0, Math.min(100, p.health || 100));
        const shieldPercent = p.shield ? Math.max(0, Math.min(100, p.shield)) : 100;
        const armorPercent = p.armor ? Math.max(0, Math.min(100, p.armor)) : 100;

        const healthClass = healthPercent > 50 ? 'success' : healthPercent > 25 ? 'warning' : 'danger';
        const shieldClass = shieldPercent > 50 ? 'info' : shieldPercent > 25 ? 'warning' : 'danger';
        const armorClass = armorPercent > 50 ? 'success' : armorPercent > 25 ? 'warning' : 'danger';

        let statusHtml = `
            <div style="margin-bottom:8px;">
                <div style="font-size:12px;font-weight:600;color:var(--hud-text-primary);margin-bottom:4px;">
                    ${p.name || 'Player'}
                </div>
                <div style="font-size:11px;color:var(--hud-text-muted);margin-bottom:10px;">
                    Score: ${p.score || 0} | Kills: ${p.kills || 0} | Deaths: ${p.deaths || 0}
                </div>
            </div>

            <div class="hud-stat-bar">
                <div class="hud-stat-bar-label">
                    <span>Health</span>
                    <span class="stat-value">${Math.round(healthPercent)}%</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill ${healthClass}" style="width: ${healthPercent}%"></div>
                </div>
            </div>

            <div class="hud-stat-bar">
                <div class="hud-stat-bar-label">
                    <span>Shield</span>
                    <span class="stat-value">${Math.round(shieldPercent)}%</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill ${shieldClass}" style="width: ${shieldPercent}%"></div>
                </div>
            </div>

            <div class="hud-stat-bar">
                <div class="hud-stat-bar-label">
                    <span>Armor</span>
                    <span class="stat-value">${Math.round(armorPercent)}%</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill ${armorClass}" style="width: ${armorPercent}%"></div>
                </div>
            </div>

            <div style="margin-top:10px;font-size:11px;color:var(--hud-text-muted);">
                Players: ${this.players.length || 0} | ${this.matchInfo ? this.matchInfo.mode || 'TDM' : 'TDM'}
            </div>

            ${this.matchInfo && this.matchInfo.timeRemaining ? `
            <div style="font-size:11px;color:var(--hud-text-muted);margin-top:4px;">
                Time: ${this.matchInfo.timeRemaining}s
            </div>` : ''}

            <button class="hud-action-button" data-action="leave-match" style="margin-top:10px;">
                <span class="action-icon">🚪</span>
                <span class="action-label">Leave Match</span>
            </button>`;

        const selector = body.querySelector('#hudMatchSelector');
        const createBtn = body.querySelector('[data-action="create-match"]');
        if (selector || createBtn) {
            const selectorHtml = selector ? selector.outerHTML : '';
            const createBtnHtml = createBtn ? createBtn.outerHTML : '';
            body.innerHTML = selectorHtml + createBtnHtml + statusHtml;

            const newSelector = document.getElementById('hudMatchSelector');
            if (newSelector) {
                newSelector.addEventListener('change', (e) => {
                    this._selectMatch(e.target.value);
                });
            }
        } else {
            body.innerHTML = statusHtml;
        }
    }

    _renderKillFeed() {
        const body = document.getElementById('hudPromptPanelBody');
        if (!body) return;

        const killEvents = this.recentEvents
            .filter(e => e.type === 'KILL' || e.type === 'HIT' || e.type === 'CHAT')
            .slice(-10);

        if (killEvents.length === 0) {
            body.innerHTML = `<div class="hud-narrative-text">
                <p>${this.currentMatchId ? 'Waiting for action...' : 'Join a match to begin...'}</p>
            </div>`;
            return;
        }

        let feedHtml = '<div class="hud-kill-feed">';
        for (const evt of killEvents) {
            let message = '';
            let type = 'info';

            if (evt.type === 'KILL') {
                message = `${evt.killerName || evt.playerId || 'Unknown'} eliminated ${evt.victimName || evt.targetId || 'Unknown'}`;
                type = 'warning';
            } else if (evt.type === 'HIT') {
                message = `${evt.shooterName || 'Unknown'} hit ${evt.targetName || 'Unknown'}`;
                type = 'info';
            } else if (evt.type === 'CHAT') {
                message = `${evt.playerName || 'Unknown'}: ${evt.message || ''}`;
                type = 'info';
            }

            feedHtml += `<div class="hud-event-log-entry ${type}">
                <div class="event-message">${message}</div>
            </div>`;
        }
        feedHtml += '</div>';

        body.innerHTML = feedHtml;
    }

    _renderWeaponPanel() {
        const body = document.getElementById('hudResponsesPanelBody');
        if (!body) return;

        const weapon = this.weapons[this.currentWeaponIndex];
        const ammoPercent = weapon.magSize > 0 ? Math.round((this.currentAmmo / weapon.magSize) * 100) : 0;

        let html = '';

        for (let i = 0; i < this.weapons.length; i++) {
            const w = this.weapons[i];
            const isActive = i === this.currentWeaponIndex;
            const ammoHint = `${w.magSize}/${w.reserveAmmo}`;
            html += `<button class="hud-action-button" data-action="switch-weapon-${i}" 
                style="${isActive ? 'border-color:var(--hud-accent-primary);background:var(--hud-accent-glow);' : ''}">
                <span class="action-icon">${i === 5 ? '🚀' : '🔫'}</span>
                <span class="action-label">${w.name}</span>
                <span class="action-hint">${ammoHint}</span>
            </button>`;
        }

        html += `
            <div class="hud-stat-bar" style="margin-top:10px;">
                <div class="hud-stat-bar-label">
                    <span>Ammo (${weapon.name})</span>
                    <span class="stat-value">${this.currentAmmo}/${this.currentReserve}</span>
                </div>
                <div class="hud-stat-bar-track">
                    <div class="hud-stat-bar-fill ${ammoPercent > 30 ? 'success' : ammoPercent > 10 ? 'warning' : 'danger'}" 
                         style="width: ${ammoPercent}%"></div>
                </div>
            </div>`;

        html += `
            <button class="hud-action-button" data-action="fire" style="margin-top:8px;">
                <span class="action-icon">💥</span>
                <span class="action-label">Fire</span>
                <span class="action-hint">Click 3D view to shoot</span>
            </button>
            <button class="hud-action-button" data-action="reload">
                <span class="action-icon">🔄</span>
                <span class="action-label">Reload</span>
                <span class="action-hint">Press R</span>
            </button>`;

        body.innerHTML = html;
    }

    _renderScoreboard() {
        const body = document.getElementById('hudActionsPanelBody');
        if (!body) return;

        const sortedPlayers = [...this.players].sort((a, b) => (b.score || 0) - (a.score || 0));

        let html = '<div class="hud-scoreboard">';
        html += '<table style="width:100%;border-collapse:collapse;font-size:11px;">';
        html += '<thead><tr style="color:var(--hud-text-muted);text-transform:uppercase;letter-spacing:0.03em;">';
        html += '<th style="text-align:left;padding:4px;">Player</th>';
        html += '<th style="text-align:center;padding:4px;">Score</th>';
        html += '<th style="text-align:center;padding:4px;">K/D</th>';
        html += '</tr></thead><tbody>';

        for (const p of sortedPlayers) {
            const kd = (p.kills || 0) + '/' + (p.deaths || 0);
            html += `<tr style="color:var(--hud-accent-primary);font-weight:600;">
                <td style="padding:4px;">${p.name || 'Unknown'}</td>
                <td style="text-align:center;padding:4px;">${p.score || 0}</td>
                <td style="text-align:center;padding:4px;">${kd}</td>
            </tr>`;
        }

        html += '</tbody></table></div>';

        if (this.teamScores && Object.keys(this.teamScores).length > 0) {
            html += '<div style="margin-top:10px;">';
            for (const [team, score] of Object.entries(this.teamScores)) {
                const teamClass = team.toLowerCase().includes('red') ? 'danger' : team.toLowerCase().includes('blue') ? 'info' : 'success';
                html += `<div class="hud-stat-bar">
                    <div class="hud-stat-bar-label">
                        <span>${team}</span>
                        <span class="stat-value">${score || 0}</span>
                    </div>
                    <div class="hud-stat-bar-track">
                        <div class="hud-stat-bar-fill ${teamClass}" style="width: ${Math.min(100, score || 0)}%"></div>
                    </div>
                </div>`;
            }
            html += '</div>';
        }

        if (this.capturePoints && this.capturePoints.length > 0) {
            html += '<div style="margin-top:10px;font-size:11px;color:var(--hud-text-muted);">';
            html += '<div style="text-transform:uppercase;margin-bottom:4px;">Capture Points</div>';
            for (const cp of this.capturePoints) {
                const cpClass = cp.team === 'red' ? 'danger' : cp.team === 'blue' ? 'info' : 'warning';
                html += `<div class="hud-event-log-entry ${cpClass}">
                    <div class="event-message">${cp.name || 'Point'} — ${cp.team || 'Neutral'}</div>
                </div>`;
            }
            html += '</div>';
        }

        const chatInput = body.querySelector('.hud-chat-input');
        const chatHtml = chatInput ? chatInput.outerHTML : '';

        body.innerHTML = html + chatHtml;

        const newChat = body.querySelector('.hud-chat-input');
        if (newChat) {
            const input = newChat.querySelector('input');
            const button = newChat.querySelector('button');
            if (input && button) {
                const sendChat = () => {
                    const msg = input.value.trim();
                    if (msg) {
                        this._sendChat(msg);
                        input.value = '';
                    }
                };
                button.addEventListener('click', sendChat);
                input.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter') sendChat();
                });
            }
        }
    }

    _clearPanels() {
        const promptBody = document.getElementById('hudPromptPanelBody');
        if (promptBody) promptBody.innerHTML = '<div class="hud-narrative-text"><p>Join a match to begin...</p></div>';

        const responsesBody = document.getElementById('hudResponsesPanelBody');
        if (responsesBody) responsesBody.innerHTML = '';

        const actionsBody = document.getElementById('hudActionsPanelBody');
        if (actionsBody) {
            const chatInput = actionsBody.querySelector('.hud-chat-input');
            const chatHtml = chatInput ? chatInput.outerHTML : '';
            actionsBody.innerHTML = chatHtml;
        }
    }

    // ── Action Handlers ──

    async _handleAction(action) {
        if (action === 'create-match') {
            await this._createMatch();
            return;
        }

        if (action === 'leave-match') {
            await this._leaveMatch();
            return;
        }

        if (!this.currentMatchId) {
            this.hudController.addEventLogEntry('Join a match first', 'warning');
            return;
        }

        // Weapon switch
        if (action.startsWith('switch-weapon-')) {
            const index = parseInt(action.replace('switch-weapon-', ''));
            if (!isNaN(index) && index >= 0 && index < this.weapons.length) {
                this._switchWeapon(index);
            }
            return;
        }

        // Fire and reload — send to iframe
        if (action === 'fire') {
            this._sendToIframe({ type: 'HUD_FIRE' });
            return;
        }
        if (action === 'reload') {
            this._sendToIframe({ type: 'HUD_RELOAD' });
            return;
        }

        this.hudController.addEventLogEntry(`Unknown action: ${action}`, 'warning');
    }

    _switchWeapon(index) {
        this.currentWeaponIndex = index;
        const weapon = this.weapons[index];
        this.currentAmmo = weapon.magSize;
        this.currentReserve = weapon.reserveAmmo;

        this.hudController.addEventLogEntry(`Switched to ${weapon.name}`, 'info');

        // Send to iframe
        this._sendToIframe({
            type: 'HUD_SWITCH_WEAPON',
            index: index
        });

        this._renderWeaponPanel();
    }

    _sendChat(message) {
        this.hudController.addEventLogEntry(`You: ${message}`, 'info');
        // Chat is handled by the iframe game via WebSocket
    }

    // ── Public API for HUD controller ──

    notifyIframeResize() {
        this._notifyIframeResize();
    }

    // ── Abstract Method Implementations ──

    async getGameState() {
        return {
            match: this.currentMatch,
            localPlayer: this.localPlayer,
            players: this.players,
            matchInfo: this.matchInfo,
            teamScores: this.teamScores,
            capturePoints: this.capturePoints
        };
    }

    async getNarrativePrompt() {
        if (this.recentEvents.length > 0) {
            const lastEvent = this.recentEvents[this.recentEvents.length - 1];
            if (lastEvent.type === 'KILL') {
                return `${lastEvent.killerName || 'Unknown'} eliminated ${lastEvent.victimName || 'Unknown'}`;
            }
        }
        return this.currentMatchId ? 'Match in progress...' : 'Join a match to begin...';
    }

    async getAvailableActions() {
        const actions = this.weapons.map((w, i) => ({
            id: `switch-weapon-${i}`,
            label: w.name,
            icon: i === 5 ? '🚀' : '🔫'
        }));
        actions.push({ id: 'fire', label: 'Fire', icon: '💥' });
        actions.push({ id: 'reload', label: 'Reload', icon: '🔄' });
        return actions;
    }

    async executeAction(actionId, params = {}) {
        return this._handleAction(actionId);
    }

    // ── Voice Command Handling ──

    handleVoiceCommand(commandType, parameters) {
        switch (commandType) {
            case 'MOVEMENT':
                this._handleVoiceMovement(parameters);
                break;
            case 'COMBAT':
                this._handleVoiceCombat(parameters);
                break;
            case 'INTERACTION':
                this._handleVoiceInteraction(parameters);
                break;
            case 'QUERY':
                this._handleVoiceQuery(parameters);
                break;
            default:
                this.hudController.addEventLogEntry(`Voice command type not handled: ${commandType}`, 'warning');
        }
    }

    _handleVoiceMovement(params) {
        const direction = params.direction;
        if (!direction) return;

        // Map movement directions to keyboard-like actions sent to iframe
        const movementMap = {
            'forward': 'KeyW',
            'backward': 'KeyS',
            'left': 'KeyA',
            'right': 'KeyD',
            'up': 'Space',
            'down': 'ShiftLeft'
        };

        const key = movementMap[direction.toLowerCase()];
        if (key) {
            this._sendToIframe({ type: 'HUD_KEY_PRESS', key: key });
            this.hudController.addEventLogEntry(`Voice movement: ${direction}`, 'info');
        }
    }

    _handleVoiceCombat(params) {
        const action = params.action;
        if (!action) return;

        const actionLower = action.toLowerCase();

        if (actionLower === 'fire' || actionLower === 'shoot') {
            this._sendToIframe({ type: 'HUD_FIRE' });
            this.hudController.addEventLogEntry('Voice command: fire', 'info');
        } else if (actionLower === 'reload') {
            this._sendToIframe({ type: 'HUD_RELOAD' });
            this.hudController.addEventLogEntry('Voice command: reload', 'info');
        } else if (actionLower.startsWith('switch') || actionLower.startsWith('weapon')) {
            // Extract weapon name or index
            const weaponName = params.weapon;
            if (weaponName) {
                const index = this.weapons.findIndex(w => w.name.toLowerCase() === weaponName.toLowerCase());
                if (index >= 0) {
                    this._switchWeapon(index);
                } else {
                    this.hudController.addEventLogEntry(`Weapon not found: ${weaponName}`, 'warning');
                }
            }
        }
    }

    _handleVoiceInteraction(params) {
        const action = params.action;
        if (!action) return;

        const actionLower = action.toLowerCase();

        if (actionLower === 'pick' || actionLower === 'pickup') {
            this._sendToIframe({ type: 'HUD_KEY_PRESS', key: 'KeyE' });
            this.hudController.addEventLogEntry('Voice command: pickup', 'info');
        } else if (actionLower === 'use') {
            this._sendToIframe({ type: 'HUD_KEY_PRESS', key: 'KeyE' });
            this.hudController.addEventLogEntry('Voice command: use', 'info');
        } else if (actionLower === 'crouch') {
            this._sendToIframe({ type: 'HUD_KEY_PRESS', key: 'ShiftLeft' });
            this.hudController.addEventLogEntry('Voice command: crouch', 'info');
        } else if (actionLower === 'jump') {
            this._sendToIframe({ type: 'HUD_KEY_PRESS', key: 'Space' });
            this.hudController.addEventLogEntry('Voice command: jump', 'info');
        }
    }

    _handleVoiceQuery(params) {
        // Queries are handled by the HUD controller's TTS response
        // Just log that the query was processed
        const question = params.question;
        if (question) {
            this.hudController.addEventLogEntry(`Voice query: ${question}`, 'info');
        }
    }

    // ── AI Inference Integration ──────────────────────────────────────────────

    /**
     * Initialize AI inference for this adapter.
     */
    async initAiInference(neuronService) {
        if (typeof window !== 'undefined' && window.AiInferenceAdapter) {
            this.aiAdapter = new window.AiInferenceAdapter({
                gameId: 'fps',
                neuronService: neuronService,
                enableAi: this.aiEnabled
            });
            await this.aiAdapter.initialize();
            console.log('[FPS] AI inference initialized');
        }
    }

    /**
     * Get AI-assisted tactical recommendation.
     * @returns {Promise<Object>} Tactical recommendation
     */
    async getAiTacticalRecommendation() {
        if (!this.aiAdapter) return null;

        const gameState = this._encodeGameState();
        const actions = ['attack', 'defend', 'flank', 'retreat', 'capture_point',
                         'defend_point', 'reload', 'use_medkit', 'throw_grenade',
                         'take_cover', 'push_team', 'hold_position'];
        return this.aiAdapter.requestAiDecision(gameState, actions);
    }

    /**
     * Get AI-assisted weapon recommendation.
     * @returns {Promise<Object>} Weapon recommendation
     */
    async getAiWeaponRecommendation() {
        if (!this.aiAdapter) return null;

        const gameState = {
            ...this._encodeGameState(),
            currentWeaponIndex: this.localPlayer ? (this.localPlayer.weaponIndex || 0) : 0,
            ammoCount: this.localPlayer ? (this.localPlayer.ammo || 0) : 0
        };

        const actions = this.weapons.map(w => w.name.toLowerCase().replace(/\s+/g, '_'));
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

        // Player stats (indices 0-7)
        if (this.localPlayer) {
            vector[idx++] = Math.max(-1, Math.min(1, (this.localPlayer.health || 0) / 100));
            vector[idx++] = Math.max(-1, Math.min(1, (this.localPlayer.ammo || 0) / 100));
            vector[idx++] = Math.max(-1, Math.min(1, (this.localPlayer.score || 0) / 1000));
            vector[idx++] = Math.max(-1, Math.min(1, (this.localPlayer.kills || 0) / 50));
            vector[idx++] = Math.max(-1, Math.min(1, (this.localPlayer.deaths || 0) / 50));
            vector[idx++] = Math.max(-1, Math.min(1, (this.localPlayer.x || 0) / 1000));
            vector[idx++] = Math.max(-1, Math.min(1, (this.localPlayer.y || 0) / 1000));
            vector[idx++] = Math.max(-1, Math.min(1, (this.localPlayer.z || 0) / 100));
        }

        // Match info (indices 8-11)
        if (this.matchInfo) {
            vector[idx++] = Math.max(-1, Math.min(1, (this.matchInfo.timeRemaining || 0) / 300));
            vector[idx++] = Math.max(-1, Math.min(1, (this.matchInfo.round || 0) / 20));
        }

        // Team scores (indices 12-15)
        const scores = Object.values(this.teamScores);
        if (scores.length > 0) {
            const maxScore = Math.max(...scores);
            const minScore = Math.min(...scores);
            vector[idx++] = Math.max(-1, Math.min(1, maxScore / 1000));
            vector[idx++] = Math.max(-1, Math.min(1, (maxScore - minScore) / 500));
        } else {
            idx += 2;
        }

        // Players (indices 16-19)
        vector[idx++] = Math.max(-1, Math.min(1, (this.players.length || 0) / 32));
        const alivePlayers = this.players.filter(p => p.health > 0).length;
        vector[idx++] = Math.max(-1, Math.min(1, alivePlayers / 16));

        // Capture points (indices 20-23)
        vector[idx++] = Math.max(-1, Math.min(1, (this.capturePoints.length || 0) / 10));
        const capturedPoints = this.capturePoints.filter(p => p.captured).length;
        vector[idx++] = Math.max(-1, Math.min(1, capturedPoints / 5));

        // Weapon state (indices 24-27)
        if (this.localPlayer && this.weapons[this.localPlayer.weaponIndex]) {
            const weapon = this.weapons[this.localPlayer.weaponIndex];
            vector[idx++] = Math.max(-1, Math.min(1, (weapon.damage || 0) / 100));
            vector[idx++] = Math.max(-1, Math.min(1, (weapon.fireRate || 0) / 20));
        } else {
            idx += 2;
        }

        return vector;
    }
}

// Expose globally
window.FpsAdapter = FpsAdapter;

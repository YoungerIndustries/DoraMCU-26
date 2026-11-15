/**
 * DoraMCU Game UI - GAME MODES Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';
    // GAME MODES — Fetch and display active game mode state

    async function loadGameModes() {
        if (!GameUIState.sessionId) return;
        const galaxyName = GameUIState.gameState?.sessionName || '';
        if (!galaxyName) return;
        try {
            const baseUrl = GameAPI.getBaseUrl ? GameAPI.getBaseUrl() : '';
            const response = await fetch(baseUrl + '/api/v1/game-creator/games/' + encodeURIComponent(galaxyName) + '/state');
            if (!response.ok) return;
            const data = await response.json();
            renderGameModes(data);
        } catch (err) {
            // Silently ignore — game modes may not be initialized for this galaxy
        }
    }

    function renderGameModes(data) {
        const listEl = document.getElementById('gameModesList');
        const stateEl = document.getElementById('gameModesState');
        if (!listEl || !stateEl) return;

        if (!data || !data.activeModes || data.activeModes.length === 0) {
            listEl.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 10px; font-size: 0.8rem;">No game modes active</div>';
            stateEl.style.display = 'none';
            return;
        }

        // Render active mode badges
        const modeIcons = {
            'RPG': '📜', 'FOUR_X': '🌌', 'TYCOON': '💰', 'TACTICAL': '⚔️',
            'SURVIVAL': '🏕️', 'SANDBOX': '🏗️', 'MMORPG': '👥', 'ROGUELIKE': '🎲',
            'SIMULATION': '📊', 'ADVENTURE': '🗺️', 'FPS': '🔫'
        };
        let html = '';
        data.activeModes.forEach(mode => {
            const icon = modeIcons[mode] || '🎮';
            html += '<div style="display:inline-block; margin:2px; padding:3px 8px; background:rgba(6,182,212,0.15); border:1px solid rgba(6,182,212,0.3); border-radius:4px; font-size:0.7rem; color:var(--accent-cyan);">' + icon + ' ' + mode.replace('_', ' ') + '</div>';
        });
        listEl.innerHTML = html;

        // Render per-module state details if available
        if (data.moduleStates && data.moduleStates.length > 0) {
            let stateHtml = '';
            data.moduleStates.forEach(ms => {
                const icon = modeIcons[ms.mode] || '🎮';
                stateHtml += '<div style="margin-bottom:4px; padding:4px 6px; border-left:2px solid rgba(6,182,212,0.4); background:rgba(6,182,212,0.05); border-radius:0 4px 4px 0;">';
                stateHtml += '<div style="font-weight:bold; color:var(--accent-cyan); margin-bottom:2px;">' + icon + ' ' + (ms.displayName || ms.mode) + '</div>';
                stateHtml += '<div style="color:var(--text-secondary);">' + (ms.state || 'No data') + '</div>';
                stateHtml += '</div>';
            });
            stateEl.innerHTML = stateHtml;
            stateEl.style.display = 'block';
        } else if (data.stateSummary) {
            // Fallback to combined summary
            stateEl.textContent = data.stateSummary;
            stateEl.style.display = 'block';
        } else {
            stateEl.style.display = 'none';
        }
    }

    // Register public functions on GameUI
    GameUI.loadGameModes = loadGameModes;
    GameUI.renderGameModes = renderGameModes;
}());

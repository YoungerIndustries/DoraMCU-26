/**
 * DoraMCU Game UI - SSE LISTENERS / EVENT LOG / TURN TIMER Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';
    function setupSSEListeners() {
        GameSSE.on('TURN_CHANGED', (data) => {
            const payload = data.payload || {};
            const turn = payload.turn || '?';
            const playerName = payload.currentPlayerName || payload.playerName || '?';
            const autoAdvanced = payload.autoAdvanced === true;
            
            if (autoAdvanced) {
                GameUI.showToast(`Ã¢ÂÂ© Auto-advanced to Turn ${turn}: ${playerName}`, 'info');
            }
            addEventLog('TURN_CHANGED', `Turn ${turn}: ${playerName}${autoAdvanced ? ' (auto)' : ''}`);
            loadGameState();
        });

        GameSSE.on('ACTION_RESULT', (data) => {
            addEventLog('ACTION_RESULT', data.payload?.message || 'Action completed');
            loadGameState();
        });

        GameSSE.on('PLAYER_JOINED', (data) => {
            addEventLog('PLAYER_JOINED', `${data.payload?.playerName || 'Player'} joined the game`);
            loadGameState();
        });

        GameSSE.on('PLAYER_LEFT', (data) => {
            addEventLog('PLAYER_LEFT', `${data.payload?.playerName || 'Player'} left the game`);
            loadGameState();
        });

        GameSSE.on('GAME_STATE', (data) => {
            loadGameState();
        });

        GameSSE.on('connected', () => {
            addEventLog('SYSTEM', 'Connected to game server');
        });

        GameSSE.on('disconnected', (data) => {
            // Only log disconnection if it's not a clean disconnect (e.g., user leaving)
            if (data && data.GameUIState.sessionId) {
                addEventLog('WARNING', 'Disconnected from game server, reconnecting...');
            }
        });

        GameSSE.on('error', (data) => {
            // Suppress error logs during reconnection attempts
            if (data && data.message) {
                addEventLog('WARNING', 'SSE connection issue, reconnecting...');
            }
        });
    }


    function addEventLog(type, message) {
        if (!GameUIState.elements.eventLog) return;
        const entry = document.createElement('div');
        entry.className = 'event-entry fade-in';
        const now = new Date();
        const time = now.toLocaleTimeString('en-US', { hour12: false, hour: '2-digit', minute: '2-digit', second: '2-digit' });
        entry.innerHTML = `
            <span class="event-time">${time}</span>
            <span class="event-type ${type}">${type}</span>
            <span class="event-message">${GameUI.escapeHtml(message)}</span>
        `;
        GameUIState.elements.eventLog.appendChild(entry);
        GameUIState.elements.eventLog.scrollTop = GameUIState.elements.eventLog.scrollHeight;

        // Limit log entries
        while (GameUIState.elements.eventLog.children.length > 100) {
            GameUIState.elements.eventLog.removeChild(GameUIState.elements.eventLog.firstChild);
        }
    }


    function startTurnTimerCountdown() {
        stopTurnTimerCountdown();
        if (GameUIState.turnTimerTotal <= 0) return;
        
        GameUIState.turnTimerInterval = setInterval(() => {
            GameUIState.turnTimerRemaining--;
            if (GameUIState.turnTimerRemaining <= 0) {
                GameUIState.turnTimerRemaining = 0;
            }
            updateTurnTimerDisplay();
            updateTurnProgressBar();
        }, 1000);
    }

    function stopTurnTimerCountdown() {
        if (GameUIState.turnTimerInterval) {
            clearInterval(GameUIState.turnTimerInterval);
            GameUIState.turnTimerInterval = null;
        }
    }

    function updateTurnTimerDisplay() {
        const timerEl = GameUIState.elements.turnTimerValue;
        const timerContainer = GameUIState.elements.turnTimer;
        if (!timerEl) return;
        
        timerEl.textContent = GameUIState.turnTimerRemaining;
        
        // Pulse effect when timer is about to expire (last 2 seconds)
        if (timerContainer) {
            if (GameUIState.turnTimerRemaining <= 2 && GameUIState.turnTimerRemaining > 0) {
                timerContainer.classList.add('urgent');
            } else {
                timerContainer.classList.remove('urgent');
            }
        }
    }

    function updateTurnProgressBar() {
        const fillEl = GameUIState.elements.turnProgressFill;
        if (!fillEl || GameUIState.turnTimerTotal <= 0) return;
        
        const elapsed = GameUIState.turnTimerTotal - GameUIState.turnTimerRemaining;
        const percent = Math.min(100, (elapsed / GameUIState.turnTimerTotal) * 100);
        fillEl.style.width = percent + '%';
        
        // Change color when approaching turn end
        if (GameUIState.turnTimerRemaining <= 2 && GameUIState.turnTimerRemaining > 0) {
            fillEl.classList.add('urgent');
        } else {
            fillEl.classList.remove('urgent');
        }
    }

    function updateTurnIntervalDisplay() {
        const el = GameUIState.elements.turnIntervalValue;
        if (el && GameUIState.turnTimerTotal > 0) {
            el.textContent = GameUIState.turnTimerTotal;
        }
    }

    function updateSchedulerStatus() {
        const dotEl = GameUIState.elements.schedulerDot;
        const labelEl = GameUIState.elements.schedulerLabel;
        if (!dotEl || !labelEl) return;
        
        if (GameUIState.gameState && GameUIState.gameState.schedulerEnabled) {
            dotEl.classList.remove('disabled');
            labelEl.textContent = 'Auto';
        } else {
            dotEl.classList.add('disabled');
            labelEl.textContent = 'Manual';
        }
    }


    // Register public functions on GameUI
    GameUI.setupSSEListeners = setupSSEListeners;
    GameUI.addEventLog = addEventLog;
    GameUI.startTurnTimerCountdown = startTurnTimerCountdown;
    GameUI.stopTurnTimerCountdown = stopTurnTimerCountdown;
    GameUI.updateTurnTimerDisplay = updateTurnTimerDisplay;
    GameUI.updateTurnProgressBar = updateTurnProgressBar;
    GameUI.updateTurnIntervalDisplay = updateTurnIntervalDisplay;
    GameUI.updateSchedulerStatus = updateSchedulerStatus;
}());

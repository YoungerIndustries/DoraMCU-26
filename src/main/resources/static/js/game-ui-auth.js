/**
 * DoraMCU Game UI - AUTH & SESSIONS Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';
    async function checkAuth() {
        const result = await GameAPI.ensureSession();

        if (result.GameUIState.sessionId) {
            // Valid stored session Ã¢â‚¬â€ enter the game directly
            const username = localStorage.getItem('username');
            if (username) {
                GameUIState.elements.playerName.textContent = username;
            }
            checkGalaxyProgress();
            GameUIState.sessionId = result.GameUIState.sessionId;
            enterSession();
            return;
        }

        // Check if needs session selection (authenticated but no session)
        if (result.needsSessionSelection) {
            const username = localStorage.getItem('username');
            if (username) {
                GameUIState.elements.playerName.textContent = username;
            }
            checkGalaxyProgress();
            showSessionSelection();
            return;
        }

        // Not authenticated Ã¢â‚¬â€ show login screen
        showLogin();
    }

    function showLogin() {
        if (GameUIState.elements.loginOverlay) GameUIState.elements.loginOverlay.classList.remove('hidden');
        if (GameUIState.elements.sessionOverlay) GameUIState.elements.sessionOverlay.classList.add('hidden');
        if (GameUIState.elements.gameContainer) GameUIState.elements.gameContainer.classList.add('hidden');
    }

    function showSessionSelection() {
        if (GameUIState.elements.loginOverlay) GameUIState.elements.loginOverlay.classList.add('hidden');
        if (GameUIState.elements.sessionOverlay) GameUIState.elements.sessionOverlay.classList.remove('hidden');
        if (GameUIState.elements.gameContainer) GameUIState.elements.gameContainer.classList.add('hidden');
        updateSessionServerIndicator();
        loadSessions();
    }

    function updateSessionServerIndicator() {
        if (!GameUIState.elements.sessionServerIndicator) return;
        const base = GameAPI.getServerUrl();
        GameUIState.elements.sessionServerIndicator.textContent = 'Server: ' + (base || 'Default Host');
    }

    function showGame() {
        if (GameUIState.elements.loginOverlay) GameUIState.elements.loginOverlay.classList.add('hidden');
        if (GameUIState.elements.sessionOverlay) GameUIState.elements.sessionOverlay.classList.add('hidden');
        if (GameUIState.elements.gameContainer) GameUIState.elements.gameContainer.classList.remove('hidden');
    }

    // Login handler
    async function handleLogin(e) {
        e.preventDefault();
        const username = GameUIState.elements.loginUsername.value.trim();
        const password = GameUIState.elements.loginPassword.value;
        if (!username || !password) {
            showLoginError('Please enter username and password');
            return;
        }
        GameUIState.elements.loginBtn.disabled = true;
        GameUIState.elements.loginBtn.textContent = 'Signing in...';
        hideLoginError();

        try {
            const data = await GameAPI.login(username, password);
            GameUIState.elements.playerName.textContent = username;
            checkGalaxyProgress();
            showSessionSelection();
        } catch (err) {
            showLoginError('Invalid username or password. Check your credentials and try again.');
        } finally {
            GameUIState.elements.loginBtn.disabled = false;
            GameUIState.elements.loginBtn.textContent = 'Sign In';
        }
    }

    // Player PIN Login handler
    async function handlePinLogin(e) {
        e.preventDefault();
        const pin = GameUIState.elements.loginPinInput ? GameUIState.elements.loginPinInput.value.trim() : '';
        if (!pin) {
            showLoginError('Please enter your 4-6 digit Player PIN code');
            return;
        }
        if (GameUIState.elements.loginPinBtn) {
            GameUIState.elements.loginPinBtn.disabled = true;
            GameUIState.elements.loginPinBtn.textContent = 'Validating PIN...';
        }
        hideLoginError();

        try {
            const data = await GameAPI.pinLogin(pin);
            GameUIState.elements.playerName.textContent = data.username || data.empireName || 'Commander';
            checkGalaxyProgress();
            showSessionSelection();
        } catch (err) {
            showLoginError(err.message || 'Invalid or unregistered Player PIN code');
        } finally {
            if (GameUIState.elements.loginPinBtn) {
                GameUIState.elements.loginPinBtn.disabled = false;
                GameUIState.elements.loginPinBtn.textContent = 'Sign In with PIN';
            }
        }
    }

    // Join Session with Player PIN
    async function handleJoinByPin() {
        const pin = GameUIState.elements.joinByPinInput ? GameUIState.elements.joinByPinInput.value.trim() : '';
        if (!pin) {
            if (GameUIState.elements.joinByPinError) {
                GameUIState.elements.joinByPinError.textContent = 'Please enter a 4-6 digit Player PIN code';
                GameUIState.elements.joinByPinError.style.display = 'block';
            }
            return;
        }
        try {
            if (GameUIState.elements.joinByPinError) GameUIState.elements.joinByPinError.style.display = 'none';
            const data = await GameAPI.pinLogin(pin);
            GameUIState.elements.playerName.textContent = data.username || data.empireName || 'Commander';
            GameUI.showToast('Player PIN connected: ' + (data.empireName || data.username), 'success');
            loadSessions();
        } catch (err) {
            if (GameUIState.elements.joinByPinError) {
                GameUIState.elements.joinByPinError.textContent = err.message || 'Failed to authenticate PIN';
                GameUIState.elements.joinByPinError.style.display = 'block';
            }
        }
    }

    function handleLogout() {
        GameSSE.disconnect();
        GameAPI.clearAuth();
        GameUIState.sessionId = null;
        GameUIState.gameState = null;
        window.location.href = 'index.html';
    }

    function handleBackToLogin() {
        GameSSE.disconnect();
        GameAPI.clearAuth();
        GameUIState.sessionId = null;
        GameUIState.gameState = null;
        window.location.href = 'index.html';
    }

    // Register new player handler
    async function handleRegisterPlayer() {
        const username = GameUIState.elements.registerUsername ? GameUIState.elements.registerUsername.value.trim() : '';
        const password = GameUIState.elements.registerPassword ? GameUIState.elements.registerPassword.value : '';
        const errorEl = GameUIState.elements.registerError;

        if (!username || !password) {
            if (errorEl) {
                errorEl.textContent = 'Please enter both username and password';
                errorEl.style.display = 'block';
            }
            return;
        }

        if (username.length < 3) {
            if (errorEl) {
                errorEl.textContent = 'Username must be at least 3 characters long';
                errorEl.style.display = 'block';
            }
            return;
        }

        if (password.length < 4) {
            if (errorEl) {
                errorEl.textContent = 'Password must be at least 4 characters long';
                errorEl.style.display = 'block';
            }
            return;
        }

        if (errorEl) {
            errorEl.textContent = '';
            errorEl.style.display = 'none';
        }

        try {
            const data = await GameAPI.register(username, password);
            GameUIState.elements.playerName.textContent = data.username || username;
            if (data.pin) {
                GameUI.showToast('Registered! Your Player PIN is: ' + data.pin, 'success');
            }
            checkGalaxyProgress();
            showSessionSelection();
        } catch (err) {
            if (errorEl) {
                // Provide helpful error message based on status code
                let message = err.message || 'Registration failed';
                if (err.status === 400) {
                    if (message.toLowerCase().includes('username')) {
                        message = 'Username is already taken or invalid (min 3 characters)';
                    } else if (message.toLowerCase().includes('password')) {
                        message = 'Password must be at least 4 characters long';
                    } else {
                        message = 'Invalid registration data. Username: min 3 chars. Password: min 4 chars.';
                    }
                }
                errorEl.textContent = message;
                errorEl.style.display = 'block';
            }
        }
    }

    function showLoginError(message) {
        if (GameUIState.elements.loginError && GameUIState.elements.loginForm.style.display !== 'none') {
            GameUIState.elements.loginError.textContent = message;
            GameUIState.elements.loginError.style.display = 'block';
        }
        if (GameUIState.elements.loginPinError && GameUIState.elements.pinLoginForm && GameUIState.elements.pinLoginForm.style.display !== 'none') {
            GameUIState.elements.loginPinError.textContent = message;
            GameUIState.elements.loginPinError.style.display = 'block';
        }
    }

    function hideLoginError() {
        if (GameUIState.elements.loginError) {
            GameUIState.elements.loginError.style.display = 'none';
        }
        if (GameUIState.elements.loginPinError) {
            GameUIState.elements.loginPinError.style.display = 'none';
        }
    }

    // Session management
    async function loadSessions() {
        try {
            const sessions = await GameAPI.listSessions();
            renderSessionList(sessions || []);
        } catch (err) {
            addEventLog('ERROR', 'Failed to load sessions: ' + err.message);
        }
    }

    function renderSessionList(sessions) {
        if (!GameUIState.elements.sessionList) return;
        GameUIState.elements.sessionList.innerHTML = '';
        if (!sessions.length) {
            GameUIState.elements.sessionList.innerHTML = '<div class="text-center" style="color: var(--text-muted); padding: 20px;">No active sessions. Create one to start playing.</div>';
            return;
        }
        sessions.forEach(session => {
            const item = document.createElement('div');
            item.className = 'session-item';
            item.innerHTML = `
                <div>
                    <div class="session-name">${GameUI.escapeHtml(session.name || session.GameUIState.sessionId)}</div>
                    <div class="session-players">${session.playerIds ? session.playerIds.length : 0} player(s)</div>
                </div>
                <div class="session-actions">
                    <button class="btn-small primary" data-session-id="${session.GameUIState.sessionId}">Join</button>
                </div>
            `;
            GameUIState.elements.sessionList.appendChild(item);
        });

        // Bind join buttons
        GameUIState.elements.sessionList.querySelectorAll('[data-session-id]').forEach(btn => {
            btn.addEventListener('click', async (e) => {
                e.stopPropagation();
                const id = btn.getAttribute('data-session-id');
                await joinSession(id);
            });
        });
    }

    async function handleCreateSession() {
        const name = GameUIState.elements.sessionNameInput.value.trim();
        if (!name) {
            alert('Please enter a session name');
            return;
        }
        GameUIState.elements.createSessionBtn.disabled = true;
        try {
            const result = await GameAPI.createSession(name);
            GameUIState.sessionId = result.GameUIState.sessionId;
            enterSession();
        } catch (err) {
            alert('Failed to create session: ' + err.message);
        } finally {
            GameUIState.elements.createSessionBtn.disabled = false;
        }
    }

    async function joinSession(id) {
        try {
            await GameAPI.joinSession(id);
            GameUIState.sessionId = id;
            enterSession();
        } catch (err) {
            if (err.alreadyInSession) {
                // Player is already in this session Ã¢â‚¬â€ proceed directly to the game
                GameUIState.sessionId = id;
                enterSession();
            } else {
                alert('Failed to join session: ' + err.message);
            }
        }
    }

    function enterSession() {
        showGame();
        GameSSE.connect(GameUIState.sessionId);
        loadGameState();
    }

    async function handleLeaveSession() {
        if (!GameUIState.sessionId) return;
        stopSubsectorPolling();
        try {
            await GameAPI.leaveSession(GameUIState.sessionId);
        } catch (err) {
            console.error('Leave session error:', err);
        }
        GameSSE.disconnect();
        GameUIState.sessionId = null;
        GameUIState.gameState = null;
        showSessionSelection();

    // Register public functions on GameUI
    GameUI.checkAuth = checkAuth;
    GameUI.showLogin = showLogin;
    GameUI.showSessionSelection = showSessionSelection;
    GameUI.updateSessionServerIndicator = updateSessionServerIndicator;
    GameUI.showGame = showGame;
    GameUI.handleLogin = handleLogin;
    GameUI.handlePinLogin = handlePinLogin;
    GameUI.handleJoinByPin = handleJoinByPin;
    GameUI.handleLogout = handleLogout;
    GameUI.handleBackToLogin = handleBackToLogin;
    GameUI.handleRegisterPlayer = handleRegisterPlayer;
    GameUI.showLoginError = showLoginError;
    GameUI.hideLoginError = hideLoginError;
    GameUI.loadSessions = loadSessions;
    GameUI.renderSessionList = renderSessionList;
    GameUI.handleCreateSession = handleCreateSession;
    GameUI.joinSession = joinSession;
    GameUI.enterSession = enterSession;
    GameUI.handleLeaveSession = handleLeaveSession;
}());

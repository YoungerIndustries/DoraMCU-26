/**
 * Onboarding flow module for the DoraMCU News page.
 *
 * Manages the multi-step onboarding journey:
 * Door Panel → Promo Code → Account → Corporation → Character → Earth 2026
 */

(function() {
    'use strict';

    // State
    let currentStep = 0;
    let promoCode = '';
    let promoValid = false;
    let authToken = '';
    let registeredUsername = '';
    let empireName = '';
    let playerPin = '';
    let characterData = null;

    // API base URL — read from settings or default
    function getApiUrl() {
        return (localStorage.getItem('serverUrl') || 'http://localhost:8080');
    }

    // API call helper
    async function apiCall(endpoint, method, body) {
        const url = getApiUrl() + endpoint;
        const options = {
            method: method || 'GET',
            headers: { 'Content-Type': 'application/json' }
        };
        if (body) {
            options.body = JSON.stringify(body);
        }
        const response = await fetch(url, options);
        if (!response.ok) {
            const error = await response.json().catch(() => ({ error: 'Request failed' }));
            throw new Error(error.error || 'Request failed');
        }
        return response.json();
    }

    // Step navigation
    function goToStep(step) {
        currentStep = step;

        // Update progress indicator
        document.querySelectorAll('.progress-step').forEach(el => {
            const s = parseInt(el.dataset.step);
            el.classList.toggle('active', s === step);
            el.classList.toggle('completed', s < step);
        });

        // Show/hide steps
        document.querySelectorAll('.onboarding-step').forEach(el => {
            el.classList.remove('active');
        });

        const stepIds = ['stepPromo', 'stepRegister', 'stepCorporation', 'stepCharacter', 'stepEarth'];
        if (stepIds[step - 1]) {
            document.getElementById(stepIds[step - 1]).classList.add('active');
        }
    }

    // Load and display the player's current characters
    // Returns the list of characters so callers can react to having characters
    async function loadCharacters() {
        const container = document.getElementById('charactersList');
        if (!container) return [];

        // Check if user is authenticated - try playerId first, then try to resolve from username
        let playerId = localStorage.getItem('playerId');
        
        // If no playerId but we have a username/token, try to resolve it from the server
        if (!playerId) {
            const username = localStorage.getItem('username');
            const authToken = localStorage.getItem('authToken');
            if (username && authToken) {
                try {
                    // Fetch the onboarding config to get the playerId
                    const configUrl = getApiUrl() + '/api/v1/onboarding/config?username=' + encodeURIComponent(username);
                    const configResp = await fetch(configUrl, {
                        headers: { 'Authorization': 'Bearer ' + authToken }
                    });
                    if (configResp.ok) {
                        const configData = await configResp.json();
                        if (configData.playerId) {
                            playerId = configData.playerId;
                            localStorage.setItem('playerId', playerId);
                        }
                    }
                } catch (e) {
                    console.warn('Could not resolve playerId from server:', e);
                }
            }
        }

        if (!playerId) {
            container.innerHTML = '<p class="empty-state">Log in or complete onboarding to see your characters.</p>';
            return [];
        }

        container.innerHTML = '<p class="empty-state">Loading characters...</p>';

        try {
            const data = await apiCall('/api/v1/earth2026/characters/player/' + playerId, 'GET');

            if (!data || data.length === 0) {
                container.innerHTML = '<p class="empty-state">No characters yet. Complete onboarding to create your first character.</p>';
                return [];
            }

            let html = '';
            data.forEach(function(ch) {
                const location = ch.earthLocation || 'Unknown';
                const level = ch.level || 1;
                const occupation = ch.occupation || 'No occupation';
                const hp = ch.hitPoints + '/' + ch.maxHitPoints;
                const home = ch.homeStructureId ? 'Has home' : 'No home';

                html += '<div class="character-card" data-character-id="' + ch.id + '">' +
                    '<div class="character-card-header">' +
                    '<h4 class="character-name">' + ch.name + '</h4>' +
                    '<span class="character-level">Level ' + level + '</span>' +
                    '</div>' +
                    '<div class="character-details">' +
                    '<div class="character-detail"><span class="detail-label">Location:</span> ' + location + '</div>' +
                    '<div class="character-detail"><span class="detail-label">Occupation:</span> ' + occupation + '</div>' +
                    '<div class="character-detail"><span class="detail-label">HP:</span> ' + hp + '</div>' +
                    '<div class="character-detail"><span class="detail-label">Home:</span> ' + home + '</div>' +
                    '</div>' +
                    '<button class="btn btn-primary btn-small land-on-earth-btn">Land on Earth</button>' +
                    '</div>';
            });

            container.innerHTML = html;

            // Attach click handlers to Land on Earth buttons
            container.querySelectorAll('.land-on-earth-btn').forEach(function(btn) {
                btn.addEventListener('click', function(e) {
                    e.stopPropagation();
                    const card = e.target.closest('.character-card');
                    const characterId = card.dataset.characterId;
                    landOnEarth(characterId);
                });
            });

            // Also make the entire card clickable to land on Earth
            container.querySelectorAll('.character-card').forEach(function(card) {
                card.addEventListener('click', function(e) {
                    // Don't trigger if the button was clicked (it handles its own click)
                    if (e.target.classList.contains('land-on-earth-btn')) return;
                    const characterId = card.dataset.characterId;
                    landOnEarth(characterId);
                });
            });

            return data;

        } catch (e) {
            container.innerHTML = '<p class="empty-state">Failed to load characters: ' + e.message + '</p>';
            return [];
        }
    }

    // Land on Earth with an existing character
    async function landOnEarth(characterId) {
        try {
            if (typeof addActivity === 'function') {
                addActivity('Landing on Earth with character #' + characterId + '...');
            }

            // Fetch character details
            const character = await apiCall('/api/v1/earth2026/characters/' + characterId, 'GET');

            if (typeof addActivity === 'function') {
                addActivity('Landed: ' + character.name + ' at ' + (character.earthLocation || 'Unknown'));
                addActivity('Level ' + character.level + ' | HP: ' + character.hitPoints + '/' + character.maxHitPoints);
            }

            // Store the current character so the UI knows which character is active
            localStorage.setItem('currentCharacterId', characterId);
            localStorage.setItem('currentCharacterName', character.name);

            // Update the Earth landing card with this character's details
            const characterValueEl = document.getElementById('characterValue');
            if (characterValueEl) {
                characterValueEl.textContent = character.name +
                    (character.occupation ? ' (' + character.occupation + ')' : '');
            }

            const homeLocationValueEl = document.getElementById('homeLocationValue');
            if (homeLocationValueEl) {
                if (character.homeStructureId) {
                    homeLocationValueEl.textContent = 'Apartment #' + character.homeStructureId + ', ' + (character.earthLocation || 'San Diego, CA');
                } else {
                    homeLocationValueEl.textContent = character.earthLocation || 'San Diego, CA';
                }
            }

            // Hide the door panel and onboarding flow, show the Earth landing card
            const doorPanel = document.getElementById('doorPanel');
            if (doorPanel) doorPanel.style.display = 'none';

            const onboardingFlow = document.getElementById('onboardingFlow');
            if (onboardingFlow) onboardingFlow.style.display = 'none';

            // Show the Earth landing step directly (step 5) with the character's data
            // We reuse the stepEarth element but make it visible outside the onboarding flow
            // by scrolling to the Earth landing section of the My Characters area

            // Update user status in sidebar
            const statusEl = document.getElementById('userStatus');
            if (statusEl) {
                statusEl.textContent = 'On Earth';
                statusEl.className = 'user-status connected';
            }
            const nameEl = document.getElementById('userName');
            if (nameEl && character.name) nameEl.textContent = character.name;

            // Attempt to launch the game client
            if (window.electronAPI && window.electronAPI.downloadAndStartGameClient) {
                if (typeof addActivity === 'function') {
                    addActivity('Launching game client...');
                }
                try {
                    const launchResult = await window.electronAPI.downloadAndStartGameClient(window.authService ? window.authService.getServerUrl() : 'http://localhost:8080');
                    if (launchResult && launchResult.success) {
                        if (typeof addActivity === 'function') {
                            addActivity('Game client launched successfully');
                        }
                    } else {
                        if (typeof addActivity === 'function') {
                            addActivity('Game client not available yet — you can launch it later from the Games tab. Your character ' + character.name + ' is ready on Earth 2026.');
                        }
                    }
                } catch (launchErr) {
                    if (typeof addActivity === 'function') {
                        addActivity('Game client not available yet. Your character ' + character.name + ' is ready on Earth 2026. Error: ' + launchErr.message);
                    }
                }
            } else {
                if (typeof addActivity === 'function') {
                    addActivity('Game client launch is only available in the Electron desktop app. Your character ' + character.name + ' is ready on Earth 2026.');
                }
            }

        } catch (e) {
            if (typeof addActivity === 'function') {
                addActivity('Error landing on Earth: ' + e.message);
            }
            alert('Failed to land on Earth: ' + e.message);
        }
    }

    // Load and display all promo codes
    async function loadPromoCodes() {
        const container = document.getElementById('promoCodesTableContainer');
        if (!container) return;

        container.innerHTML = '<p class="empty-state">Loading promo codes...</p>';

        try {
            const data = await apiCall('/api/v1/onboarding/promo-codes', 'GET');

            if (!data || data.length === 0) {
                container.innerHTML = '<p class="empty-state">No promo codes found.</p>';
                return;
            }

            let html = '<table class="promo-codes-table">' +
                '<thead><tr>' +
                '<th>Code</th>' +
                '<th>Description</th>' +
                '<th>Type</th>' +
                '<th>Usage</th>' +
                '<th>Status</th>' +
                '<th>Expiry</th>' +
                '</tr></thead><tbody>';

            data.forEach(function(pc) {
                const status = pc.expired ? 'Expired' :
                               pc.usageLimitReached ? 'Full' :
                               pc.active ? 'Active' : 'Inactive';
                const statusClass = pc.active && !pc.expired && !pc.usageLimitReached ? 'status-active' :
                                    pc.expired ? 'status-expired' :
                                    pc.usageLimitReached ? 'status-full' : 'status-inactive';
                const usage = pc.usedCount + '/' + (pc.maxUses || '∞');
                const expiry = pc.expiryDate ? new Date(pc.expiryDate).toLocaleDateString() : 'No expiry';
                const desc = pc.description || '-';

                html += '<tr>' +
                    '<td><code class="promo-code-value">' + pc.code + '</code></td>' +
                    '<td>' + desc + '</td>' +
                    '<td>' + pc.type + '</td>' +
                    '<td>' + usage + '</td>' +
                    '<td><span class="status-badge ' + statusClass + '">' + status + '</span></td>' +
                    '<td>' + expiry + '</td>' +
                    '</tr>';
            });

            html += '</tbody></table>';
            container.innerHTML = html;

        } catch (e) {
            container.innerHTML = '<p class="empty-state">Failed to load promo codes: ' + e.message + '</p>';
        }
    }

    // Fetch door panel data
    async function fetchDoorPanel() {
        try {
            const data = await apiCall('/api/v1/onboarding/door', 'GET');
            if (data.title) {
                document.getElementById('doorTitle').textContent = data.title;
            }
            if (data.subtitle) {
                document.getElementById('doorSubtitle').textContent = data.subtitle;
            }
        } catch (e) {
            console.warn('Could not fetch door panel data:', e);
        }
    }

    // Validate promo code
    async function validatePromo() {
        const code = document.getElementById('promoCode').value.trim();
        const feedback = document.getElementById('promoFeedback');

        if (!code) {
            feedback.className = 'promo-feedback error';
            feedback.textContent = 'Please enter a promo code.';
            return;
        }

        feedback.className = 'promo-feedback';
        feedback.textContent = 'Validating...';

        try {
            const data = await apiCall('/api/v1/onboarding/promo/validate', 'POST', { code });
            if (data.valid) {
                promoCode = code;
                promoValid = true;
                feedback.className = 'promo-feedback success';

                // Route based on code type
                const codeType = data.codeType || 'PLAYER';

                if (codeType === 'CORPORATION') {
                    // Corporation code — navigate to the corporation screen
                    feedback.textContent = '✓ ' + (data.description || 'Corporation code validated!');
                    if (typeof addActivity === 'function') {
                        addActivity('Corporation code validated: ' + code);
                    }
                    if (data.corporationId) {
                        localStorage.setItem('corporationId', data.corporationId);
                    }
                    // Auto-advance to corporation screen
                    setTimeout(() => showCorporationScreen(data.corporationId), 1000);
                } else if (codeType === 'CHARACTER') {
                    // Character code — land on Earth with the associated character
                    feedback.textContent = '✓ ' + (data.description || 'Character code validated!');
                    if (typeof addActivity === 'function') {
                        addActivity('Character code validated: ' + code);
                    }
                    if (data.characterId) {
                        // Land on Earth with this character
                        setTimeout(() => landOnEarth(data.characterId), 1000);
                    } else {
                        feedback.textContent = '✗ Character code has no associated character.';
                        feedback.className = 'promo-feedback error';
                    }
                } else {
                    // PLAYER or other type — continue with the onboarding flow
                    feedback.textContent = '✓ ' + (data.description || 'Promo code is valid!');
                    if (data.bonuses) {
                        feedback.textContent += ' Bonuses: ' + data.bonuses;
                    }
                    // Auto-advance to account registration step
                    setTimeout(() => goToStep(2), 1000);
                }
            } else {
                feedback.className = 'promo-feedback error';
                feedback.textContent = '✗ ' + (data.error || 'Invalid promo code');
            }
        } catch (e) {
            feedback.className = 'promo-feedback error';
            feedback.textContent = '✗ ' + e.message;
        }
    }

    // Show the corporation screen for a given corporation (player) ID
    async function showCorporationScreen(corporationId) {
        try {
            if (typeof addActivity === 'function') {
                addActivity('Loading corporation screen...');
            }

            // Hide the door panel and onboarding flow
            const doorPanel = document.getElementById('doorPanel');
            if (doorPanel) doorPanel.style.display = 'none';

            const onboardingFlow = document.getElementById('onboardingFlow');
            if (onboardingFlow) onboardingFlow.style.display = 'none';

            // Show the corporation screen
            const corpScreen = document.getElementById('corporationScreen');
            if (corpScreen) {
                corpScreen.style.display = 'block';
            }

            // Load corporation details
            if (corporationId) {
                const corpData = await apiCall('/api/v1/earth2026/corporation/' + corporationId, 'GET');
                renderCorporationScreen(corpData);
            } else {
                // No corporationId — show a generic corporation info panel
                if (typeof addActivity === 'function') {
                    addActivity('Welcome to your corporation screen. Complete onboarding to define your corporation.');
                }
                const corpNameEl = document.getElementById('corporationNameValue');
                if (corpNameEl) corpNameEl.textContent = 'Not yet defined';
                const corpDetailsEl = document.getElementById('corporationDetails');
                if (corpDetailsEl) corpDetailsEl.innerHTML = '<p class="empty-state">Complete the onboarding flow to create your corporation.</p>';
            }

            // Update user status in sidebar
            const statusEl = document.getElementById('userStatus');
            if (statusEl) {
                statusEl.textContent = 'Corporation';
                statusEl.className = 'user-status connected';
            }

        } catch (e) {
            if (typeof addActivity === 'function') {
                addActivity('Error loading corporation screen: ' + e.message);
            }
            // If the corporation endpoint doesn't exist yet, show a fallback
            const corpScreen = document.getElementById('corporationScreen');
            if (corpScreen) {
                corpScreen.style.display = 'block';
            }
            const doorPanel = document.getElementById('doorPanel');
            if (doorPanel) doorPanel.style.display = 'none';
            const onboardingFlow = document.getElementById('onboardingFlow');
            if (onboardingFlow) onboardingFlow.style.display = 'none';
            const corpNameEl = document.getElementById('corporationNameValue');
            if (corpNameEl) corpNameEl.textContent = 'Corporation #' + (corporationId || 'N/A');
            const corpDetailsEl = document.getElementById('corporationDetails');
            if (corpDetailsEl) corpDetailsEl.innerHTML = '<p class="empty-state">Your corporation details will appear here once configured.</p>';
        }
    }

    // Render corporation details on the corporation screen
    function renderCorporationScreen(corpData) {
        const corpNameEl = document.getElementById('corporationNameValue');
        if (corpNameEl && corpData.empireName) {
            corpNameEl.textContent = corpData.empireName;
        }

        const corpDetailsEl = document.getElementById('corporationDetails');
        if (corpDetailsEl) {
            let html = '';
            if (corpData.governmentType) {
                html += '<div class="corporation-detail"><span class="detail-label">Government:</span> ' + corpData.governmentType + '</div>';
            }
            if (corpData.ethos) {
                html += '<div class="corporation-detail"><span class="detail-label">Ethos:</span> ' + corpData.ethos + '</div>';
            }
            if (corpData.email) {
                html += '<div class="corporation-detail"><span class="detail-label">Contact:</span> ' + corpData.email + '</div>';
            }
            if (corpData.funds) {
                html += '<div class="corporation-detail"><span class="detail-label">Funds:</span> ' + corpData.funds + '</div>';
            }
            if (corpData.reputation) {
                html += '<div class="corporation-detail"><span class="detail-label">Reputation:</span> ' + corpData.reputation + '</div>';
            }
            if (!html) {
                html = '<p class="empty-state">Corporation details are being loaded.</p>';
            }
            corpDetailsEl.innerHTML = html;
        }

        if (typeof addActivity === 'function') {
            addActivity('Corporation loaded: ' + (corpData.empireName || 'Unknown'));
        }
    }

    // Register account and corporation
    async function registerAccount() {
        const username = document.getElementById('regUsername').value.trim();
        const password = document.getElementById('regPassword').value;
        const email = document.getElementById('regEmail').value.trim();
        const corpName = document.getElementById('empireName').value.trim();
        const govType = document.getElementById('governmentType').value;
        const ethos = document.getElementById('ethos').value;

        if (!username) {
            alert('Username is required.');
            return;
        }
        if (!password || password.length < 4) {
            alert('Password must be at least 4 characters.');
            return;
        }

        const body = {
            username,
            password,
            email: email || username + '@doramcu.local',
            empireName: corpName || username + ' Empire',
            governmentType: govType,
            ethos: ethos
        };

        if (promoCode && promoValid) {
            body.promoCode = promoCode;
        }

        try {
            const data = await apiCall('/api/v1/onboarding/register', 'POST', body);
            authToken = data.token;
            registeredUsername = data.username;
            empireName = data.empireName;
            playerPin = data.playerPin || '';
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('username', registeredUsername);
            if (data.playerId) {
                localStorage.setItem('playerId', data.playerId);
            }
            goToStep(4);
        } catch (e) {
            alert('Registration failed: ' + e.message);
        }
    }

    // Create character
    async function createCharacter() {
        const name = document.getElementById('characterName').value.trim();
        const age = parseInt(document.getElementById('characterAge').value) || 25;
        const occupation = document.getElementById('characterOccupation').value.trim();

        if (!name) {
            alert('Character name is required.');
            return;
        }

        try {
            const data = await apiCall('/api/v1/onboarding/character', 'POST', {
                username: registeredUsername,
                characterName: name,
                age,
                occupation: occupation || 'Citizen'
            });

            characterData = data.character;

            // Update Earth landing details
            document.getElementById('characterValue').textContent = name +
                (occupation ? ' (' + occupation + ')' : '');

            if (characterData && characterData.homeStructureId) {
                document.getElementById('homeLocationValue').textContent =
                    'Apartment #' + characterData.homeStructureId + ', San Diego, CA';
            }

            // Fetch Earth landing data
            try {
                const earthData = await apiCall('/api/v1/onboarding/earth?username=' + registeredUsername, 'GET');
                if (earthData.scenarios && earthData.scenarios.length > 0) {
                    const scenariosDiv = document.getElementById('earthScenarios');
                    scenariosDiv.innerHTML = '<h4>Available Scenarios</h4>' +
                        earthData.scenarios.map(s =>
                            '<div class="scenario-item"><strong>' + s.name + '</strong><p>' +
                            (s.description || '') + '</p><span class="scenario-difficulty">Difficulty: ' +
                            s.difficulty + '</span></div>'
                        ).join('');
                }
            } catch (e) {
                console.warn('Could not fetch Earth scenarios:', e);
            }

            goToStep(5);
        } catch (e) {
            alert('Character creation failed: ' + e.message);
        }
    }

    // Begin journey — connect to server, authenticate, and launch game client
    async function beginJourney() {
        const beginBtn = document.getElementById('beginJourneyBtn');
        if (beginBtn) {
            beginBtn.textContent = 'Starting...';
            beginBtn.disabled = true;
        }

        try {
            // Step 1: Update auth service with the token from registration
            if (authToken && window.authService) {
                window.authService.token = authToken;
                window.authService.connected = true;
                window.authService.user = { name: registeredUsername };
                localStorage.setItem('authToken', authToken);
            }

            // Step 2: Verify server connection
            const connectResult = await window.authService.connect();
            if (!connectResult.success) {
                throw new Error('Cannot reach the game server at ' + window.authService.getServerUrl() + '. Please ensure the server is running.');
            }

            // Step 3: Switch to dashboard tab so the user sees progress
            document.querySelectorAll('.nav-item').forEach(el => {
                el.classList.remove('active');
                if (el.dataset.tab === 'dashboard') {
                    el.classList.add('active');
                }
            });
            document.querySelectorAll('.tab-content').forEach(el => {
                el.classList.remove('active');
            });
            document.getElementById('dashboard').classList.add('active');

            // Update user status in sidebar
            const statusEl = document.getElementById('userStatus');
            if (statusEl) {
                statusEl.textContent = 'Connected';
                statusEl.className = 'user-status connected';
            }
            const nameEl = document.getElementById('userName');
            if (nameEl && registeredUsername) nameEl.textContent = registeredUsername;

            // Add activity log entry
            if (typeof addActivity === 'function') {
                addActivity('Onboarding complete — ' + registeredUsername + ' landed on Earth 2026');
                addActivity('Character: ' + (characterData ? characterData.characterName : 'N/A'));
                addActivity('Home: Apartment, San Diego, CA');
            }

            // Step 4: Attempt to launch the game client
            if (window.electronAPI && window.electronAPI.downloadAndStartGameClient) {
                if (typeof addActivity === 'function') {
                    addActivity('Attempting to launch game client...');
                }
                try {
                    const launchResult = await window.electronAPI.downloadAndStartGameClient(window.authService.getServerUrl());
                    if (launchResult && launchResult.success) {
                        if (typeof addActivity === 'function') {
                            addActivity('Game client launched successfully');
                        }
                    } else {
                        if (typeof addActivity === 'function') {
                            addActivity('Game client not available yet — you can launch it later from the Games tab. The server is running and your account is ready.');
                        }
                    }
                } catch (launchErr) {
                    // Game client launch failed — but the account is ready
                    if (typeof addActivity === 'function') {
                        addActivity('Game client not available yet — you can launch it later from the Games tab. Error: ' + launchErr.message);
                    }
                }
            } else {
                // electronAPI not available (running in browser)
                if (typeof addActivity === 'function') {
                    addActivity('Game client launch is only available in the Electron desktop app. Your account is ready on the server.');
                }
            }

            // Refresh dashboard data if connected
            if (window.authService.connected && typeof refreshDashboard === 'function') {
                refreshDashboard();
            }

        } catch (e) {
            // Show error feedback
            if (typeof addActivity === 'function') {
                addActivity('Error: ' + e.message);
            }
            alert('Failed to begin journey: ' + e.message);
        } finally {
            if (beginBtn) {
                beginBtn.textContent = 'Begin Your Journey';
                beginBtn.disabled = false;
            }
        }
    }

    // Initialize event listeners
    async function init() {
        // Step through button
        const stepBtn = document.getElementById('stepThroughBtn');
        if (stepBtn) {
            stepBtn.addEventListener('click', () => {
                document.getElementById('doorPanel').style.display = 'none';
                document.getElementById('onboardingFlow').style.display = 'block';
                goToStep(1);
            });
        }

        // Promo validation
        const validateBtn = document.getElementById('validatePromoBtn');
        if (validateBtn) {
            validateBtn.addEventListener('click', validatePromo);
        }

        // Skip promo
        const skipBtn = document.getElementById('skipPromoBtn');
        if (skipBtn) {
            skipBtn.addEventListener('click', () => goToStep(2));
        }

        // Register navigation
        const nextFromRegister = document.getElementById('nextFromRegisterBtn');
        if (nextFromRegister) {
            nextFromRegister.addEventListener('click', () => {
                const username = document.getElementById('regUsername').value.trim();
                const password = document.getElementById('regPassword').value;
                if (!username || !password) {
                    alert('Please fill in username and password.');
                    return;
                }
                goToStep(3);
            });
        }

        const backFromRegister = document.getElementById('backFromRegisterBtn');
        if (backFromRegister) {
            backFromRegister.addEventListener('click', () => goToStep(1));
        }

        // Corporation navigation
        const backFromCorp = document.getElementById('backFromCorpBtn');
        if (backFromCorp) {
            backFromCorp.addEventListener('click', () => goToStep(2));
        }

        const registerAndCorp = document.getElementById('registerAndCorpBtn');
        if (registerAndCorp) {
            registerAndCorp.addEventListener('click', registerAccount);
        }

        // Character navigation
        const backFromChar = document.getElementById('backFromCharBtn');
        if (backFromChar) {
            backFromChar.addEventListener('click', () => goToStep(3));
        }

        const createChar = document.getElementById('createCharacterBtn');
        if (createChar) {
            createChar.addEventListener('click', createCharacter);
        }

        // Begin journey
        const beginBtn = document.getElementById('beginJourneyBtn');
        if (beginBtn) {
            beginBtn.addEventListener('click', beginJourney);
        }

        // Fetch door panel data on load
        fetchDoorPanel();

        // Load promo codes on init
        loadPromoCodes();

        // Refresh promo codes button
        const refreshPromoBtn = document.getElementById('refreshPromoCodesBtn');
        if (refreshPromoBtn) {
            refreshPromoBtn.addEventListener('click', loadPromoCodes);
        }

        // Back to News button from corporation screen
        const backFromCorpBtn = document.getElementById('backFromCorporationBtn');
        if (backFromCorpBtn) {
            backFromCorpBtn.addEventListener('click', function() {
                const corpScreen = document.getElementById('corporationScreen');
                if (corpScreen) corpScreen.style.display = 'none';
                const doorPanel = document.getElementById('doorPanel');
                if (doorPanel) doorPanel.style.display = 'block';
                const statusEl = document.getElementById('userStatus');
                if (statusEl) {
                    statusEl.textContent = 'Disconnected';
                    statusEl.className = 'user-status';
                }
            });
        }

        // Quick access promo code — standalone entry above character list
        const quickValidateBtn = document.getElementById('quickValidatePromoBtn');
        if (quickValidateBtn) {
            quickValidateBtn.addEventListener('click', async function() {
                const codeEl = document.getElementById('quickPromoCode');
                const feedbackEl = document.getElementById('quickPromoFeedback');
                const code = codeEl ? codeEl.value.trim() : '';

                if (!code) {
                    if (feedbackEl) {
                        feedbackEl.className = 'promo-feedback error';
                        feedbackEl.textContent = 'Please enter a landing key.';
                    }
                    return;
                }

                if (feedbackEl) {
                    feedbackEl.className = 'promo-feedback';
                    feedbackEl.textContent = 'Validating...';
                }

                try {
                    const data = await apiCall('/api/v1/onboarding/promo/validate', 'POST', { code });
                    if (data.valid) {
                        const codeType = data.codeType || 'PLAYER';

                        if (codeType === 'CORPORATION') {
                            if (feedbackEl) {
                                feedbackEl.className = 'promo-feedback success';
                                feedbackEl.textContent = '✓ ' + (data.description || 'Corporation code validated!');
                            }
                            if (typeof addActivity === 'function') {
                                addActivity('Corporation code validated: ' + code);
                            }
                            if (data.corporationId) {
                                localStorage.setItem('corporationId', data.corporationId);
                            }
                            setTimeout(() => showCorporationScreen(data.corporationId), 800);
                        } else if (codeType === 'CHARACTER') {
                            if (feedbackEl) {
                                feedbackEl.className = 'promo-feedback success';
                                feedbackEl.textContent = '✓ ' + (data.description || 'Character code validated!');
                            }
                            if (typeof addActivity === 'function') {
                                addActivity('Character code validated: ' + code);
                            }
                            if (data.characterId) {
                                setTimeout(() => landOnEarth(data.characterId), 800);
                            } else {
                                if (feedbackEl) {
                                    feedbackEl.textContent = '✗ Character code has no associated character.';
                                    feedbackEl.className = 'promo-feedback error';
                                }
                            }
                        } else {
                            // PLAYER type — show success and start onboarding flow
                            if (feedbackEl) {
                                feedbackEl.className = 'promo-feedback success';
                                feedbackEl.textContent = '✓ ' + (data.description || 'Landing key valid! Starting onboarding...');
                            }
                            promoCode = code;
                            promoValid = true;
                            // Show the onboarding flow and go to step 2 (account)
                            const doorPanel = document.getElementById('doorPanel');
                            if (doorPanel) doorPanel.style.display = 'none';
                            const onboardingFlow = document.getElementById('onboardingFlow');
                            if (onboardingFlow) onboardingFlow.style.display = 'block';
                            setTimeout(() => goToStep(2), 800);
                        }
                    } else {
                        if (feedbackEl) {
                            feedbackEl.className = 'promo-feedback error';
                            feedbackEl.textContent = '✗ ' + (data.error || 'Invalid landing key');
                        }
                    }
                } catch (e) {
                    if (feedbackEl) {
                        feedbackEl.className = 'promo-feedback error';
                        feedbackEl.textContent = '✗ ' + e.message;
                    }
                }
            });
        }

        // Allow Enter key in quick promo input
        const quickPromoInput = document.getElementById('quickPromoCode');
        if (quickPromoInput) {
            quickPromoInput.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    const btn = document.getElementById('quickValidatePromoBtn');
                    if (btn) btn.click();
                }
            });
        }

        // Refresh characters button
        const refreshCharBtn = document.getElementById('refreshCharactersBtn');
        if (refreshCharBtn) {
            refreshCharBtn.addEventListener('click', loadCharacters);
        }

        // Load characters and check if player already has existing characters
        // If they do, skip the onboarding flow and show the characters panel directly
        const characters = await loadCharacters();
        if (characters && characters.length > 0) {
            // Player has existing characters — hide the door panel and onboarding flow
            const doorPanel = document.getElementById('doorPanel');
            if (doorPanel) doorPanel.style.display = 'none';

            const onboardingFlow = document.getElementById('onboardingFlow');
            if (onboardingFlow) onboardingFlow.style.display = 'none';

            // Update user status in sidebar
            const statusEl = document.getElementById('userStatus');
            if (statusEl) {
                statusEl.textContent = 'Connected';
                statusEl.className = 'user-status connected';
            }
            const username = localStorage.getItem('username');
            if (username) {
                const nameEl = document.getElementById('userName');
                if (nameEl) nameEl.textContent = username;
            }

            // If there's only one character, auto-land on Earth with it
            if (characters.length === 1) {
                landOnEarth(characters[0].id);
            }
        }
    }

    // Expose init globally
    window.initOnboarding = init;

    // Note: Auto-init disabled — onboarding is initialized by game-launcher.js
    // when the News tab is first visited (via switchTab).
    // This prevents double initialization and ensures proper tab-based loading.

})();

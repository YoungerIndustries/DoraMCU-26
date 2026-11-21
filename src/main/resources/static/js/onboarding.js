/**
 * DoraMCU Onboarding Flow
 * Multi-step wizard: Door → Promo → Account → Empire → Character → Earth 2026
 */

(function() {
    'use strict';

    // ============================================
    // State
    // ============================================

    const state = {
        currentStep: 1,
        totalSteps: 6,
        promoCode: null,
        promoValid: false,
        promoData: null,
        username: null,
        email: null,
        password: null,
        empireName: null,
        governmentType: 'DEMOCRACY',
        ethos: 'MATERIALIST',
        registered: false,
        token: null,
        playerId: null,
        corporationId: null,
        characterName: null,
        characterAge: 25,
        occupation: 'CEO',
        characterCreated: false,
        newsEventSource: null
    };

    // ============================================
    // DOM Elements
    // ============================================

    const elements = {
        // Progress
        progressBarFill: document.getElementById('progressBarFill'),
        progressSteps: document.querySelectorAll('.progress-step'),

        // Steps
        steps: {
            1: document.getElementById('stepDoor'),
            2: document.getElementById('stepPromo'),
            3: document.getElementById('stepRegister'),
            4: document.getElementById('stepEmpire'),
            5: document.getElementById('stepCharacter'),
            6: document.getElementById('stepEarth')
        },

        // Door
        btnStepThrough: document.getElementById('btnStepThrough'),
        btnSkipToLogin: document.getElementById('btnSkipToLogin'),
        doorFrame: document.querySelector('.door-frame'),

        // Promo
        promoCodeInput: document.getElementById('promoCodeInput'),
        btnValidatePromo: document.getElementById('btnValidatePromo'),
        btnSkipPromo: document.getElementById('btnSkipPromo'),
        btnPromoContinue: document.getElementById('btnPromoContinue'),
        promoResult: document.getElementById('promoResult'),
        promoResultIcon: document.getElementById('promoResultIcon'),
        promoResultTitle: document.getElementById('promoResultTitle'),
        promoResultDesc: document.getElementById('promoResultDesc'),
        promoBonuses: document.getElementById('promoBonuses'),
        promoError: document.getElementById('promoError'),

        // Register
        registerForm: document.getElementById('registerForm'),
        regUsername: document.getElementById('regUsername'),
        regEmail: document.getElementById('regEmail'),
        regPassword: document.getElementById('regPassword'),
        regPasswordConfirm: document.getElementById('regPasswordConfirm'),
        registerError: document.getElementById('registerError'),
        btnRegBack: document.getElementById('btnRegBack'),
        btnRegNext: document.getElementById('btnRegNext'),

        // Empire
        empireForm: document.getElementById('empireForm'),
        empireName: document.getElementById('empireName'),
        governmentType: document.getElementById('governmentType'),
        ethos: document.getElementById('ethos'),
        previewCorp: document.getElementById('previewCorp'),
        previewTicker: document.getElementById('previewTicker'),
        previewAI: document.getElementById('previewAI'),
        previewCapital: document.getElementById('previewCapital'),
        empireError: document.getElementById('empireError'),
        btnEmpireBack: document.getElementById('btnEmpireBack'),
        btnEmpireNext: document.getElementById('btnEmpireNext'),

        // Character
        characterForm: document.getElementById('characterForm'),
        charName: document.getElementById('charName'),
        charAge: document.getElementById('charAge'),
        charOccupation: document.getElementById('charOccupation'),
        characterError: document.getElementById('characterError'),
        btnCharBack: document.getElementById('btnCharBack'),
        btnCharNext: document.getElementById('btnCharNext'),

        // Earth
        earthHomeLocation: document.getElementById('earthHomeLocation'),
        earthCorpName: document.getElementById('earthCorpName'),
        earthCharName: document.getElementById('earthCharName'),
        earthScenarios: document.getElementById('earthScenarios'),
        scenariosGrid: document.getElementById('scenariosGrid'),
        btnBeginJourney: document.getElementById('btnBeginJourney'),
        btnEarthDashboard: document.getElementById('btnEarthDashboard'),

        // Loading
        loadingOverlay: document.getElementById('loadingOverlay'),
        loadingText: document.getElementById('loadingText'),

        // News
        newsFeed: document.getElementById('newsFeed'),
        newsDot: document.getElementById('newsDot'),
        newsStatusText: document.getElementById('newsStatusText'),
        btnNewsRefresh: document.getElementById('btnNewsRefresh'),

        // Toast
        toastContainer: document.getElementById('toastContainer')
    };

    // ============================================
    // Step Navigation
    // ============================================

    function goToStep(step) {
        if (step < 1 || step > state.totalSteps) return;

        // Hide current step
        if (state.currentStep !== step) {
            elements.steps[state.currentStep].classList.remove('active');
        }

        // Show new step
        state.currentStep = step;
        elements.steps[step].classList.add('active');

        // Update progress
        updateProgress();

        // Scroll to top
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function updateProgress() {
        // Update step circles
        elements.progressSteps.forEach((el, i) => {
            const stepNum = i + 1;
            el.classList.remove('active', 'completed');
            if (stepNum === state.currentStep) {
                el.classList.add('active');
            } else if (stepNum < state.currentStep) {
                el.classList.add('completed');
            }
        });

        // Update progress bar
        const pct = ((state.currentStep - 1) / (state.totalSteps - 1)) * 100;
        elements.progressBarFill.style.width = pct + '%';
    }

    // ============================================
    // Toast Notifications
    // ============================================

    function showToast(message, type) {
        type = type || 'info';
        const toast = document.createElement('div');
        toast.className = 'toast ' + type;
        toast.textContent = message;
        elements.toastContainer.appendChild(toast);

        // Auto-remove after 4 seconds
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateX(50px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => toast.remove(), 300);
        }, 4000);
    }

    // ============================================
    // Loading Overlay
    // ============================================

    function showLoading(text) {
        elements.loadingText.textContent = text || 'Processing...';
        elements.loadingOverlay.classList.remove('hidden');
    }

    function hideLoading() {
        elements.loadingOverlay.classList.add('hidden');
    }

    // ============================================
    // API Helper
    // ============================================

    async function apiCall(url, options) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json'
            }
        };

        if (state.token) {
            defaultOptions.headers['Authorization'] = 'Bearer ' + state.token;
        }

        const response = await fetch(url, { ...defaultOptions, ...options });
        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
            throw new Error(data.error || data.message || 'Request failed');
        }

        return { ok: true, data, status: response.status };
    }

    // ============================================
    // Step 1: Door Panel
    // ============================================

    elements.btnStepThrough.addEventListener('click', function() {
        // Animate door opening
        if (elements.doorFrame) {
            elements.doorFrame.classList.add('open');
        }

        // Advance to promo step after animation
        setTimeout(() => {
            goToStep(2);
        }, 1200);
    });

    elements.btnSkipToLogin.addEventListener('click', function() {
        window.location.href = 'index.html';
    });

    // ============================================
    // Step 2: Promo Code
    // ============================================

    elements.btnValidatePromo.addEventListener('click', async function() {
        const code = elements.promoCodeInput.value.trim();
        if (!code) {
            elements.promoError.textContent = 'Please enter a promo code';
            elements.promoError.classList.remove('hidden');
            return;
        }

        elements.promoError.classList.add('hidden');
        elements.promoResult.classList.add('hidden');
        elements.btnValidatePromo.disabled = true;

        try {
            const result = await apiCall('/api/v1/onboarding/promo/validate', {
                method: 'POST',
                body: JSON.stringify({ code })
            });

            if (result.ok && result.data.valid) {
                state.promoCode = code;
                state.promoValid = true;
                state.promoData = result.data;

                // Show success
                elements.promoResultIcon.textContent = '✅';
                elements.promoResultTitle.textContent = 'Promo Code Valid!';
                elements.promoResultDesc.textContent = result.data.description || 'This code is valid and ready to use.';

                // Show bonuses if available
                if (result.data.bonuses) {
                    elements.promoBonuses.innerHTML = '';
                    const bonuses = typeof result.data.bonuses === 'string'
                        ? JSON.parse(result.data.bonuses)
                        : result.data.bonuses;
                    Object.entries(bonuses).forEach(([key, val]) => {
                        const tag = document.createElement('span');
                        tag.className = 'promo-bonus-tag';
                        tag.textContent = key + ': ' + val;
                        elements.promoBonuses.appendChild(tag);
                    });
                    elements.promoBonuses.classList.remove('hidden');
                } else {
                    elements.promoBonuses.classList.add('hidden');
                }

                elements.promoResult.classList.remove('hidden');
                showToast('Promo code validated successfully!', 'success');
            }
        } catch (err) {
            elements.promoError.textContent = err.message || 'Invalid promo code';
            elements.promoError.classList.remove('hidden');
            state.promoValid = false;
        } finally {
            elements.btnValidatePromo.disabled = false;
        }
    });

    elements.btnSkipPromo.addEventListener('click', function() {
        state.promoCode = null;
        state.promoValid = false;
        goToStep(3);
    });

    elements.btnPromoContinue.addEventListener('click', function() {
        goToStep(3);
    });

    // ============================================
    // Step 3: Account Registration
    // ============================================

    elements.btnRegBack.addEventListener('click', function() {
        goToStep(2);
    });

    elements.registerForm.addEventListener('submit', function(e) {
        e.preventDefault();

        const username = elements.regUsername.value.trim();
        const email = elements.regEmail.value.trim();
        const password = elements.regPassword.value;
        const passwordConfirm = elements.regPasswordConfirm.value;

        // Validation
        if (!username || username.length < 3) {
            elements.registerError.textContent = 'Username must be at least 3 characters';
            elements.registerError.classList.remove('hidden');
            return;
        }

        if (!password || password.length < 4) {
            elements.registerError.textContent = 'Password must be at least 4 characters';
            elements.registerError.classList.remove('hidden');
            return;
        }

        if (password !== passwordConfirm) {
            elements.registerError.textContent = 'Passwords do not match';
            elements.registerError.classList.remove('hidden');
            return;
        }

        // Save state
        state.username = username;
        state.email = email || username + '@doramcu.local';
        state.password = password;

        // Pre-fill empire name suggestion
        elements.empireName.value = username + ' Industries';
        updateEmpirePreview();

        goToStep(4);
    });

    // ============================================
    // Step 4: Empire Setup
    // ============================================

    elements.empireName.addEventListener('input', updateEmpirePreview);

    function updateEmpirePreview() {
        const name = elements.empireName.value.trim() || '—';
        elements.previewCorp.textContent = name;

        // Generate ticker
        const tickerBase = name.replace(/[^A-Za-z]/g, '').substring(0, 4).toUpperCase();
        elements.previewTicker.textContent = tickerBase || '—';

        // Generate AI VP name
        const aiName = (state.username || 'User').replace(/ /g, '') + ' AI';
        elements.previewAI.textContent = aiName;
    }

    elements.btnEmpireBack.addEventListener('click', function() {
        goToStep(3);
    });

    elements.empireForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const empireName = elements.empireName.value.trim();
        if (!empireName) {
            elements.empireError.textContent = 'Empire name is required';
            elements.empireError.classList.remove('hidden');
            return;
        }

        // Save state
        state.empireName = empireName;
        state.governmentType = elements.governmentType.value;
        state.ethos = elements.ethos.value;

        // Show loading
        showLoading('Creating your account and empire...');

        try {
            // Call registration API
            const registerBody = {
                username: state.username,
                password: state.password,
                email: state.email,
                empireName: state.empireName,
                governmentType: state.governmentType,
                ethos: state.ethos
            };

            if (state.promoCode) {
                registerBody.promoCode = state.promoCode;
            }

            const result = await apiCall('/api/v1/onboarding/register', {
                method: 'POST',
                body: JSON.stringify(registerBody)
            });

            if (result.ok) {
                state.registered = true;
                state.token = result.data.token;
                state.playerId = result.data.playerId;
                state.corporationId = result.data.corporationId;

                // Store token for subsequent requests
                localStorage.setItem('doramcu_token', state.token);
                localStorage.setItem('doramcu_username', state.username);

                // If character was auto-created, skip to Earth step
                if (result.data.characterId) {
                    state.characterCreated = true;
                    state.characterName = result.data.characterName;
                    showToast('Account, empire, and character created!', 'success');
                    showEarthLanding(result.data);
                    return;
                }

                showToast('Account and empire created! Now create your character.', 'success');

                // Pre-fill character name
                elements.charName.value = state.username + ' Prime';
                goToStep(5);
            }
        } catch (err) {
            elements.empireError.textContent = err.message || 'Registration failed';
            elements.empireError.classList.remove('hidden');
            hideLoading();
        }
    });

    // ============================================
    // Step 5: Character Creation
    // ============================================

    elements.btnCharBack.addEventListener('click', function() {
        goToStep(4);
    });

    elements.characterForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const charName = elements.charName.value.trim();
        if (!charName) {
            elements.characterError.textContent = 'Character name is required';
            elements.characterError.classList.remove('hidden');
            return;
        }

        // Save state
        state.characterName = charName;
        state.characterAge = parseInt(elements.charAge.value) || 25;
        state.occupation = elements.charOccupation.value.trim() || 'CEO';

        showLoading('Creating your character...');

        try {
            const result = await apiCall('/api/v1/onboarding/character', {
                method: 'POST',
                body: JSON.stringify({
                    username: state.username,
                    characterName: state.characterName,
                    age: state.characterAge,
                    occupation: state.occupation
                })
            });

            if (result.ok) {
                state.characterCreated = true;
                showToast('Character created! Welcome to Earth 2026.', 'success');
                showEarthLanding(result.data);
            }
        } catch (err) {
            elements.characterError.textContent = err.message || 'Character creation failed';
            elements.characterError.classList.remove('hidden');
            hideLoading();
        }
    });

    // ============================================
    // Step 6: Earth 2026 Landing
    // ============================================

    function showEarthLanding(registerData) {
        hideLoading();

        // Populate Earth info
        if (registerData) {
            elements.earthCorpName.textContent = registerData.corporationName || state.empireName || '—';
            elements.earthCharName.textContent = registerData.characterName || state.characterName || '—';
        } else {
            elements.earthCorpName.textContent = state.empireName || '—';
            elements.earthCharName.textContent = state.characterName || '—';
        }

        // Fetch Earth landing data
        fetchEarthData();

        goToStep(6);
    }

    async function fetchEarthData() {
        try {
            const result = await apiCall('/api/v1/onboarding/earth?username=' + encodeURIComponent(state.username));

            if (result.ok && result.data) {
                const data = result.data;

                // Update location
                if (data.landingCity) {
                    elements.earthHomeLocation.textContent = data.landingCity;
                }

                // Show scenarios
                if (data.scenarios && data.scenarios.length > 0) {
                    elements.scenariosGrid.innerHTML = '';
                    data.scenarios.forEach(scenario => {
                        const card = document.createElement('div');
                        card.className = 'scenario-card';
                        card.innerHTML = '<h5>' + (scenario.name || scenario.title || 'Scenario') + '</h5>' +
                            '<p>' + (scenario.description || '') + '</p>';
                        elements.scenariosGrid.appendChild(card);
                    });
                    elements.earthScenarios.classList.remove('hidden');
                }
            }
        } catch (err) {
            console.warn('Failed to fetch Earth data:', err);
        }
    }

    elements.btnBeginJourney.addEventListener('click', function() {
        // Redirect to the game
        window.location.href = 'game.html';
    });

    elements.btnEarthDashboard.addEventListener('click', function() {
        window.location.href = 'game.html';
    });

    // ============================================
    // News Feed (SSE)
    // ============================================

    function connectNewsFeed() {
        // Close existing connection
        if (state.newsEventSource) {
            state.newsEventSource.close();
        }

        elements.newsStatusText.textContent = 'Connecting...';
        elements.newsDot.className = 'news-dot';

        try {
            const eventSource = new EventSource('/api/v1/news/live');
            state.newsEventSource = eventSource;

            eventSource.onopen = function() {
                elements.newsDot.className = 'news-dot connected';
                elements.newsStatusText.textContent = 'Live';
            };

            eventSource.onmessage = function(event) {
                try {
                    const data = JSON.parse(event.data);
                    addNewsItem(data);
                } catch (e) {
                    // Raw text event
                    addNewsItem({ type: 'NEWS', title: event.data, timestamp: new Date().toISOString() });
                }
            };

            eventSource.onerror = function() {
                elements.newsDot.className = 'news-dot error';
                elements.newsStatusText.textContent = 'Disconnected';
                eventSource.close();

                // Auto-reconnect after 5 seconds
                setTimeout(connectNewsFeed, 5000);
            };
        } catch (err) {
            elements.newsStatusText.textContent = 'SSE not supported';
        }

        // Also fetch recent news from door endpoint
        fetchRecentNews();
    }

    async function fetchRecentNews() {
        try {
            const result = await apiCall('/api/v1/onboarding/door');
            if (result.ok && result.data && result.data.recentNews) {
                elements.newsFeed.innerHTML = '';
                result.data.recentNews.forEach(item => {
                    addNewsItem(item, true);
                });
            }
        } catch (err) {
            console.warn('Failed to fetch recent news:', err);
        }
    }

    function addNewsItem(item, prepend) {
        const div = document.createElement('div');
        div.className = 'news-item';

        const typeEl = document.createElement('div');
        typeEl.className = 'news-item-type';
        typeEl.textContent = item.type || 'NEWS';

        const titleEl = document.createElement('div');
        titleEl.className = 'news-item-title';
        titleEl.textContent = item.title || item.message || 'Update';

        const timeEl = document.createElement('div');
        timeEl.className = 'news-item-time';
        const ts = item.timestamp ? new Date(item.timestamp) : new Date();
        timeEl.textContent = ts.toLocaleTimeString();

        div.appendChild(typeEl);
        div.appendChild(titleEl);
        div.appendChild(timeEl);

        if (prepend) {
            elements.newsFeed.insertBefore(div, elements.newsFeed.firstChild);
        } else {
            elements.newsFeed.appendChild(div);
        }

        // Limit to 50 items
        while (elements.newsFeed.children.length > 50) {
            elements.newsFeed.removeChild(elements.newsFeed.lastChild);
        }
    }

    elements.btnNewsRefresh.addEventListener('click', function() {
        fetchRecentNews();
        if (state.newsEventSource && state.newsEventSource.readyState === EventSource.CLOSED) {
            connectNewsFeed();
        }
    });

    // ============================================
    // Initialization
    // ============================================

    function init() {
        // Check if already logged in
        const existingToken = localStorage.getItem('doramcu_token');
        if (existingToken) {
            // Verify token
            fetch('/api/v1/4x/auth/server-info', {
                headers: { 'Authorization': 'Bearer ' + existingToken }
            }).then(res => {
                if (res.ok) {
                    // Already logged in, redirect to dashboard
                    window.location.href = 'game.html';
                }
            }).catch(() => {
                // Token invalid, continue with onboarding
            });
        }

        // Start news feed
        connectNewsFeed();

        // Initial progress
        updateProgress();

        console.log('DoraMCU Onboarding initialized');
    }

    // Run on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();

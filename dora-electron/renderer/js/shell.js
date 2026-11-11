/* ============================================
   DoraMCU Shell Controller
   Routing, navigation, Unity client control, toast notifications
   ============================================ */

const Shell = (function() {
    'use strict';

    // DOM Elements
    let elements = {};
    
    // State
    let currentRoute = '#dashboard';
    let unityStatus = 'disconnected';

    // Page title map
    const pageTitles = {
        '#dashboard': 'Dashboard',
        '#game/doramcu-4x': 'DoraMCU 4X',
        '#game/traveller': 'Traveller',
        '#game/captains-log': "Captain's Log",
        '#game/dnd-5e': 'D&D 5e',
        '#game/third-reich': 'Third Reich',
        '#game/earth-2026': 'Earth 2026',
        '#admin': 'Admin Panel',
        '#health': 'Health Dashboard'
    };

    // Route to page mapping for iframe loading
    const routeToPage = {
        '#game/doramcu-4x': 'game.html',
        '#game/traveller': 'traveller-rules.html',
        '#game/dnd-5e': 'dnd5e.html',
        '#admin': 'admin.html',
        '#health': 'health.html'
    };

    function cacheElements() {
        elements = {
            loginView: document.getElementById('login-view'),
            loginForm: document.getElementById('login-form'),
            loginError: document.getElementById('login-error'),
            mainShell: document.getElementById('main-shell'),
            sidebar: document.getElementById('sidebar'),
            sidebarToggle: document.getElementById('sidebar-toggle'),
            topbar: document.getElementById('topbar'),
            pageTitle: document.getElementById('current-page-title'),
            contentArea: document.getElementById('content-area'),
            dashboardView: document.getElementById('dashboard-view'),
            iframeContainer: document.getElementById('iframe-container'),
            gameIframe: document.getElementById('game-iframe'),
            gameCards: document.getElementById('game-cards'),
            unityStatus: document.getElementById('unity-status'),
            unityToggleBtn: document.getElementById('unity-toggle-btn'),
            userMenuBtn: document.getElementById('user-menu-btn'),
            userDropdown: document.getElementById('user-dropdown'),
            userInitial: document.getElementById('user-initial'),
            displayUsername: document.getElementById('display-username'),
            logoutBtn: document.getElementById('logout-btn'),
            toastContainer: document.getElementById('toast-container')
        };
    }

    function init() {
        cacheElements();
        
        // Initialize auth
        const session = AuthService.init();
        
        if (session.authenticated) {
            showMainShell(session.username);
        } else {
            showLoginView();
        }

        // Setup event listeners
        setupLoginHandler();
        setupNavigation();
        setupSidebarToggle();
        setupUserMenu();
        setupUnityClient();
        setupAuthExpiration();

        // Handle initial hash
        if (session.authenticated && window.location.hash) {
            handleRoute(window.location.hash);
        } else if (session.authenticated) {
            handleRoute('#dashboard');
        }

        // Listen for hash changes
        window.addEventListener('hashchange', () => {
            const hash = window.location.hash || '#dashboard';
            handleRoute(hash);
        });
    }

    function showLoginView() {
        if (elements.loginView) {
            elements.loginView.classList.remove('hidden');
        }
        if (elements.mainShell) {
            elements.mainShell.classList.add('hidden');
        }
        
        // Pre-fill server URL
        const serverUrlInput = document.getElementById('server-url');
        if (serverUrlInput) {
            serverUrlInput.value = AuthService.getServerUrl();
        }
    }

    function showMainShell(username) {
        if (elements.loginView) {
            elements.loginView.classList.add('hidden');
        }
        if (elements.mainShell) {
            elements.mainShell.classList.remove('hidden');
        }

        // Update user info
        updateUserUI(username);
    }

    function updateUserUI(username) {
        const name = username || 'User';
        if (elements.userInitial) {
            elements.userInitial.textContent = name.charAt(0).toUpperCase();
        }
        if (elements.displayUsername) {
            elements.displayUsername.textContent = name;
        }
    }

    function setupLoginHandler() {
        if (!elements.loginForm) return;

        elements.loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const serverUrlInput = document.getElementById('server-url');
            const usernameInput = document.getElementById('username');
            const passwordInput = document.getElementById('password');
            const loginBtn = document.getElementById('login-btn');

            const serverUrl = serverUrlInput ? serverUrlInput.value.trim() : '';
            const username = usernameInput ? usernameInput.value.trim() : '';
            const password = passwordInput ? passwordInput.value : '';

            if (!serverUrl || !username || !password) {
                showLoginError('Please fill in all fields');
                return;
            }

            // Save server URL
            AuthService.setServerUrl(serverUrl);

            // Disable button during login
            if (loginBtn) {
                loginBtn.disabled = true;
                loginBtn.querySelector('.btn-text').textContent = 'Signing in...';
            }

            try {
                const result = await AuthService.login(username, password);
                
                if (result.success) {
                    hideLoginError();
                    showMainShell(result.username);
                    handleRoute('#dashboard');
                } else {
                    showLoginError(result.error || 'Login failed');
                }
            } catch (error) {
                showLoginError('Connection failed. Please check the server URL.');
            } finally {
                if (loginBtn) {
                    loginBtn.disabled = false;
                    loginBtn.querySelector('.btn-text').textContent = 'Sign In';
                }
            }
        });
    }

    function showLoginError(message) {
        if (elements.loginError) {
            elements.loginError.textContent = message;
            elements.loginError.classList.remove('hidden');
        }
    }

    function hideLoginError() {
        if (elements.loginError) {
            elements.loginError.classList.add('hidden');
        }
    }

    function setupNavigation() {
        // Sidebar nav items
        document.querySelectorAll('.nav-item[data-route]').forEach(item => {
            const link = item.querySelector('.nav-link');
            if (link) {
                link.addEventListener('click', (e) => {
                    e.preventDefault();
                    const route = item.dataset.route;
                    navigate(route);
                });
            }
        });

        // Logout
        if (elements.logoutBtn) {
            elements.logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                handleLogout();
            });
        }
    }

    function navigate(route) {
        window.location.hash = route;
    }

    function handleRoute(route) {
        currentRoute = route;
        
        // Update page title
        if (elements.pageTitle) {
            const title = pageTitles[route] || 'Dashboard';
            elements.pageTitle.textContent = title;
        }

        // Update active nav item
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
            if (item.dataset.route === route) {
                item.classList.add('active');
            }
        });

        // Show/hide views
        if (route === '#dashboard') {
            showDashboard();
        } else if (route.startsWith('#game/') || route === '#admin' || route === '#health') {
            loadContentPanel(route);
        }
    }

    function showDashboard() {
        if (elements.dashboardView) {
            elements.dashboardView.classList.remove('hidden');
        }
        if (elements.iframeContainer) {
            elements.iframeContainer.classList.add('hidden');
        }
        
        // Render game cards
        if (elements.gameCards) {
            GameLauncher.renderDashboard(elements.gameCards);
        }
    }

    function loadContentPanel(route) {
        const page = routeToPage[route];
        if (!page) {
            // Show placeholder for unavailable modules
            showPlaceholder(route);
            return;
        }

        if (elements.dashboardView) {
            elements.dashboardView.classList.add('hidden');
        }
        if (elements.iframeContainer) {
            elements.iframeContainer.classList.remove('hidden');
        }

        // Load page in iframe
        const serverUrl = AuthService.getServerUrl();
        if (elements.gameIframe) {
            elements.gameIframe.src = serverUrl + '/' + page;
        }
    }

    function showPlaceholder(route) {
        if (elements.dashboardView) {
            elements.dashboardView.classList.add('hidden');
        }
        if (elements.iframeContainer) {
            elements.iframeContainer.classList.remove('hidden');
        }

        // Show placeholder content in iframe
        if (elements.gameIframe) {
            const moduleName = pageTitles[route] || 'Game Module';
            const placeholderHtml = `
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body {
                            font-family: 'Inter', sans-serif;
                            background: #0c0a1a;
                            color: #f0e6ff;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                            height: 100vh;
                            margin: 0;
                        }
                        .placeholder {
                            text-align: center;
                            padding: 40px;
                        }
                        .placeholder h2 {
                            font-family: 'Orbitron', sans-serif;
                            color: #c084fc;
                            margin-bottom: 16px;
                        }
                        .placeholder p {
                            color: #8b7faa;
                            font-size: 1.1rem;
                        }
                    </style>
                </head>
                <body>
                    <div class="placeholder">
                        <h2>${moduleName}</h2>
                        <p>This game module is coming soon.</p>
                    </div>
                </body>
                </html>
            `;
            elements.gameIframe.srcdoc = placeholderHtml;
        }
    }

    function setupSidebarToggle() {
        if (!elements.sidebarToggle) return;

        elements.sidebarToggle.addEventListener('click', () => {
            if (elements.sidebar) {
                elements.sidebar.classList.toggle('collapsed');
            }
            if (elements.topbar) {
                elements.topbar.classList.toggle('expanded');
            }
        });
    }

    function setupUserMenu() {
        if (!elements.userMenuBtn) return;

        elements.userMenuBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            if (elements.userDropdown) {
                elements.userDropdown.classList.toggle('hidden');
            }
        });

        // Close dropdown on outside click
        document.addEventListener('click', (e) => {
            if (elements.userDropdown && !elements.userDropdown.classList.contains('hidden')) {
                if (!elements.userDropdown.contains(e.target) && e.target !== elements.userMenuBtn) {
                    elements.userDropdown.classList.add('hidden');
                }
            }
        });
    }

    function handleLogout() {
        AuthService.logout();
        
        // Reset UI
        if (elements.userDropdown) {
            elements.userDropdown.classList.add('hidden');
        }
        
        // Clear iframe
        if (elements.gameIframe) {
            elements.gameIframe.src = 'about:blank';
        }

        // Show login
        showLoginView();
        showToast('Logged out successfully', 'success');
    }

    function setupUnityClient() {
        if (!elements.unityToggleBtn) return;

        // Check initial status
        checkUnityStatus();

        // Toggle button
        elements.unityToggleBtn.addEventListener('click', async () => {
            if (unityStatus === 'running') {
                await stopUnityClient();
            } else {
                await startUnityClient();
            }
        });

        // Listen for status updates from main process
        if (window.electronAPI && window.electronAPI.onGameClientStatusUpdate) {
            window.electronAPI.onGameClientStatusUpdate((data) => {
                updateUnityStatusUI(data.status);
            });
        }

        if (window.electronAPI && window.electronAPI.onGameClientDeploymentStatus) {
            window.electronAPI.onGameClientDeploymentStatus((data) => {
                showToast(data.message, 'info');
            });
        }
    }

    async function checkUnityStatus() {
        if (!window.electronAPI) {
            updateUnityStatusUI('unavailable');
            return;
        }

        try {
            const status = await window.electronAPI.getGameClientStatus();
            unityStatus = status;
            updateUnityStatusUI(status);
        } catch (error) {
            updateUnityStatusUI('disconnected');
        }
    }

    async function startUnityClient() {
        if (!window.electronAPI) {
            showToast('Unity client control is only available in the desktop app', 'warning');
            return;
        }

        try {
            showToast('Starting Unity client...', 'info');
            await window.electronAPI.startGameClient();
            unityStatus = 'running';
            updateUnityStatusUI('running');
            showToast('Unity client started', 'success');
        } catch (error) {
            showToast('Failed to start Unity client: ' + error.message, 'error');
        }
    }

    async function stopUnityClient() {
        if (!window.electronAPI) return;

        try {
            await window.electronAPI.stopGameClient();
            unityStatus = 'stopped';
            updateUnityStatusUI('stopped');
            showToast('Unity client stopped', 'success');
        } catch (error) {
            showToast('Failed to stop Unity client: ' + error.message, 'error');
        }
    }

    function updateUnityStatusUI(status) {
        if (!elements.unityStatus) return;

        const indicator = elements.unityStatus.querySelector('.status-indicator');
        const text = elements.unityStatus.querySelector('.status-text');

        if (indicator) {
            indicator.className = 'status-indicator';
            if (status === 'running') {
                indicator.classList.add('running');
            } else if (status === 'connected' || status === 'stopped') {
                indicator.classList.add('connected');
            }
        }

        if (text) {
            const statusText = {
                'running': 'Unity: Running',
                'stopped': 'Unity: Stopped',
                'connected': 'Unity: Connected',
                'disconnected': 'Unity: Disconnected',
                'unavailable': 'Unity: N/A'
            };
            text.textContent = statusText[status] || 'Unity: Unknown';
        }
    }

    function setupAuthExpiration() {
        AuthService.onAuthExpired(() => {
            showToast('Session expired. Please log in again.', 'warning');
            handleLogout();
        });
    }

    // Toast notification system
    function showToast(message, type = 'info') {
        if (!elements.toastContainer) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.textContent = message;
        elements.toastContainer.appendChild(toast);

        // Auto-remove after 4 seconds
        setTimeout(() => {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(20px)';
            toast.style.transition = 'all 0.3s ease';
            setTimeout(() => {
                if (toast.parentNode) {
                    toast.parentNode.removeChild(toast);
                }
            }, 300);
        }, 4000);
    }

    return {
        init,
        navigate,
        showToast
    };
})();

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', () => {
    Shell.init();
});

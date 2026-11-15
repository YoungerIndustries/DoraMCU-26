/**
 * Tycoon View Controller
 *
 * Manages switching between 2D isometric and 3D Babylon.js views.
 * Lazy loads Babylon.js only when 3D view is selected to minimize initial load time.
 *
 * Features:
 * - View toggle between 2D and 3D modes
 * - Lazy loading of Babylon.js from CDN
 * - State synchronization between views
 * - Graceful fallback if 3D fails to load
 * - Memory management: dispose 3D scene when switching back to 2D
 */
(function () {
    'use strict';

    /**
     * View Controller State
     */
    const state = {
        currentView: '2d',          // '2d' or '3d'
        babylonLoaded: false,       // Whether Babylon.js is loaded
        babylonLoading: false,      // Whether Babylon.js is currently loading
        scene3D: null,             // Reference to Tycoon3D instance
        planetId: null,            // Current planet ID
        worldState: null,          // Cached world state from API
        updateInterval: null       // Interval for state updates
    };

    /**
     * Loads Babylon.js from CDN dynamically.
     * @returns {Promise<boolean>} True if loaded successfully
     */
    function loadBabylonJS() {
        if (state.babylonLoaded) return Promise.resolve(true);
        if (state.babylonLoading) {
            return new Promise((resolve) => {
                const check = setInterval(() => {
                    if (state.babylonLoaded) {
                        clearInterval(check);
                        resolve(true);
                    }
                }, 100);
                setTimeout(() => {
                    clearInterval(check);
                    resolve(state.babylonLoaded);
                }, 30000);
            });
        }

        state.babylonLoading = true;
        return new Promise((resolve) => {
            const script = document.createElement('script');
            script.src = 'https://cdn.babylonjs.com/babylon.js';
            script.onload = () => {
                console.log('Babylon.js loaded successfully');
                state.babylonLoaded = true;
                state.babylonLoading = false;
                resolve(true);
            };
            script.onerror = () => {
                console.error('Failed to load Babylon.js from CDN');
                state.babylonLoading = false;
                resolve(false);
            };
            document.head.appendChild(script);
        });
    }

    /**
     * Switches to 2D isometric view.
     */
    function switchTo2D() {
        if (state.currentView === '2d') return;

        console.log('Switching to 2D view');

        // Hide 3D container, show 2D container
        const container3D = document.getElementById('tycoon3DContainer');
        const container2D = document.getElementById('hexGridContainer');
        if (container3D) container3D.style.display = 'none';
        if (container2D) container2D.style.display = 'block';

        // Update button states
        const btn2D = document.getElementById('view2DBtn');
        const btn3D = document.getElementById('view3DBtn');
        const label = document.getElementById('viewModeLabel');
        if (btn2D) btn2D.style.background = 'var(--accent-cyan)';
        if (btn3D) btn3D.style.background = 'var(--accent-purple)';
        if (label) label.textContent = 'Mode: 2D';

        // Dispose 3D scene to free memory
        if (state.scene3D && state.scene3D.dispose) {
            state.scene3D.dispose();
            state.scene3D = null;
        }

        // Stop 3D state updates
        if (state.updateInterval) {
            clearInterval(state.updateInterval);
            state.updateInterval = null;
        }

        state.currentView = '2d';
    }

    /**
     * Switches to 3D Babylon.js view.
     * @param {number} planetId - The planet ID to render
     */
    async function switchTo3D(planetId) {
        if (state.currentView === '3d') return;

        console.log('Switching to 3D view for planet', planetId);
        state.planetId = planetId;

        // Show loading indicator
        const container3D = document.getElementById('tycoon3DContainer');
        const container2D = document.getElementById('hexGridContainer');
        if (container3D) container3D.style.display = 'block';
        if (container2D) container2D.style.display = 'none';

        // Update button states
        const btn2D = document.getElementById('view2DBtn');
        const btn3D = document.getElementById('view3DBtn');
        const label = document.getElementById('viewModeLabel');
        if (btn2D) btn2D.style.background = 'var(--accent-purple)';
        if (btn3D) btn3D.style.background = 'var(--accent-cyan)';
        if (label) label.textContent = 'Mode: 3D';

        // Load Babylon.js if not already loaded
        const loaded = await loadBabylonJS();
        if (!loaded) {
            console.error('Failed to load Babylon.js, falling back to 2D');
            showToast('3D view unavailable - Babylon.js failed to load', 'error');
            switchTo2D();
            return;
        }

        // Load tycoon-3d.js if not already loaded
        if (typeof Tycoon3D === 'undefined') {
            await loadTycoon3DScript();
        }

        // Initialize 3D renderer
        try {
            if (typeof Tycoon3D !== 'undefined') {
                state.scene3D = Tycoon3D;
                state.scene3D.init('tycoon3DCanvas', planetId);
                console.log('3D renderer initialized');

                // Start periodic state updates
                state.updateInterval = setInterval(() => {
                    if (state.scene3D && state.scene3D.updateWorldState) {
                        state.scene3D.updateWorldState(planetId);
                    }
                }, 5000); // Update every 5 seconds
            }
        } catch (error) {
            console.error('Failed to initialize 3D renderer:', error);
            showToast('3D view initialization failed', 'error');
            switchTo2D();
            return;
        }

        state.currentView = '3d';
    }

    /**
     * Loads tycoon-3d.js script dynamically.
     * @returns {Promise<void>}
     */
    function loadTycoon3DScript() {
        return new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.src = 'js/tycoon-3d.js';
            script.onload = () => {
                console.log('tycoon-3d.js loaded');
                resolve();
            };
            script.onerror = () => {
                console.error('Failed to load tycoon-3d.js');
                reject(new Error('tycoon-3d.js not found'));
            };
            document.head.appendChild(script);
        });
    }

    /**
     * Shows a toast notification.
     * @param {string} message - Message to display
     * @param {string} type - 'error', 'success', 'info'
     */
    function showToast(message, type) {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        container.appendChild(toast);

        setTimeout(() => {
            toast.remove();
        }, 3000);
    }

    /**
     * Initializes the view controller.
     * Sets up event listeners for view toggle buttons.
     */
    function init() {
        console.log('Tycoon View Controller initialized');

        // Wait for DOM to be ready
        const initButtons = () => {
            const btn2D = document.getElementById('view2DBtn');
            const btn3D = document.getElementById('view3DBtn');

            if (btn2D && btn3D) {
                btn2D.addEventListener('click', () => {
                    switchTo2D();
                });

                btn3D.addEventListener('click', () => {
                    // Get planet ID from current context
                    const planetId = state.planetId || getCurrentPlanetId();
                    if (planetId) {
                        switchTo3D(planetId);
                    } else {
                        showToast('No planet selected for 3D view', 'error');
                    }
                });
                console.log('View toggle buttons initialized');
            }
        };

        // Initialize when DOM is ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', initButtons);
        } else {
            initButtons();
        }
    }

    /**
     * Gets the current planet ID from the game state.
     * @returns {number|null} Planet ID or null
     */
    function getCurrentPlanetId() {
        // Try to get from game state
        if (typeof window.gameState !== 'undefined' && window.gameState.currentPlanetId) {
            return window.gameState.currentPlanetId;
        }
        // Try to get from URL
        const urlParams = new URLSearchParams(window.location.search);
        const planetId = urlParams.get('planetId');
        if (planetId) return parseInt(planetId);
        // Default to planet 1
        return 1;
    }

    /**
     * Sets the current planet ID for 3D view.
     * @param {number} planetId - Planet ID
     */
    function setPlanetId(planetId) {
        state.planetId = planetId;
    }

    /**
     * Exports public API
     */
    window.TycoonViewController = {
        init: init,
        switchTo2D: switchTo2D,
        switchTo3D: switchTo3D,
        setPlanetId: setPlanetId,
        getCurrentView: () => state.currentView,
        loadBabylonJS: loadBabylonJS
    };

    // Auto-initialize when script loads
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

})();

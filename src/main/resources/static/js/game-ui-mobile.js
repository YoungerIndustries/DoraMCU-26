/**
 * DoraMCU Game UI - Mobile Panel Toggle Module
 *
 * Adds mobile-responsive panel toggle buttons and overlay
 * for the collapsible side panels on small screens.
 * Works with the existing mobile CSS in game.css.
 */
(function() {
    'use strict';

    const MOBILE_BREAKPOINT = 768;

    /**
     * Check if the current viewport is in mobile mode.
     * @returns {boolean}
     */
    function isMobile() {
        return window.innerWidth <= MOBILE_BREAKPOINT;
    }

    /**
     * Create the panel toggle buttons and overlay for mobile.
     */
    function createMobileControls() {
        // Prevent duplicate creation
        if (document.getElementById('mobilePanelToggleLeft')) return;

        const gameContainer = document.getElementById('gameContainer');
        if (!gameContainer) return;

        // Create overlay
        const overlay = document.createElement('div');
        overlay.className = 'panel-overlay';
        overlay.id = 'panelOverlay';
        overlay.addEventListener('click', closeAllPanels);
        gameContainer.appendChild(overlay);

        // Create left panel toggle button
        const leftToggle = document.createElement('button');
        leftToggle.className = 'panel-toggle left';
        leftToggle.id = 'mobilePanelToggleLeft';
        leftToggle.innerHTML = '&#9776;'; // Hamburger icon
        leftToggle.title = 'Toggle left panel';
        leftToggle.addEventListener('click', () => togglePanel('left-panel'));
        gameContainer.appendChild(leftToggle);

        // Create right panel toggle button
        const rightToggle = document.createElement('button');
        rightToggle.className = 'panel-toggle right';
        rightToggle.id = 'mobilePanelToggleRight';
        rightToggle.innerHTML = '&#9776;'; // Hamburger icon
        rightToggle.title = 'Toggle right panel';
        rightToggle.addEventListener('click', () => togglePanel('right-panel'));
        gameContainer.appendChild(rightToggle);

        // Show/hide toggle buttons based on viewport
        updateToggleVisibility();
    }

    /**
     * Toggle a panel's open/closed state on mobile.
     * @param {string} panelClass - The CSS class of the panel to toggle.
     */
    function togglePanel(panelClass) {
        const panel = document.querySelector('.' + panelClass);
        if (!panel) return;

        const overlay = document.getElementById('panelOverlay');
        const isOpen = panel.classList.contains('open');

        if (isOpen) {
            closePanel(panelClass);
        } else {
            closeAllPanels();
            panel.classList.add('open');
            if (overlay) overlay.classList.add('visible');
        }
    }

    /**
     * Close a specific panel.
     * @param {string} panelClass
     */
    function closePanel(panelClass) {
        const panel = document.querySelector('.' + panelClass);
        if (panel) panel.classList.remove('open');
        hideOverlay();
    }

    /**
     * Close all panels and hide overlay.
     */
    function closeAllPanels() {
        const leftPanel = document.querySelector('.left-panel');
        const rightPanel = document.querySelector('.right-panel');
        if (leftPanel) leftPanel.classList.remove('open');
        if (rightPanel) rightPanel.classList.remove('open');
        hideOverlay();
    }

    /**
     * Hide the panel overlay.
     */
    function hideOverlay() {
        const overlay = document.getElementById('panelOverlay');
        if (overlay) overlay.classList.remove('visible');
    }

    /**
     * Update toggle button visibility based on viewport.
     */
    function updateToggleVisibility() {
        const leftToggle = document.getElementById('mobilePanelToggleLeft');
        const rightToggle = document.getElementById('mobilePanelToggleRight');

        if (isMobile()) {
            if (leftToggle) leftToggle.style.display = 'flex';
            if (rightToggle) rightToggle.style.display = 'flex';
        } else {
            if (leftToggle) leftToggle.style.display = 'none';
            if (rightToggle) rightToggle.style.display = 'none';
            // Close any open panels when switching to desktop
            closeAllPanels();
        }
    }

    /**
     * Handle swipe gestures for panel navigation on mobile.
     */
    function setupSwipeGestures() {
        let touchStartX = 0;
        let touchStartY = 0;
        const SWIPE_THRESHOLD = 50;

        document.addEventListener('touchstart', (e) => {
            touchStartX = e.touches[0].clientX;
            touchStartY = e.touches[0].clientY;
        }, { passive: true });

        document.addEventListener('touchend', (e) => {
            const touchEndX = e.changedTouches[0].clientX;
            const touchEndY = e.changedTouches[0].clientY;
            const deltaX = touchEndX - touchStartX;
            const deltaY = touchEndY - touchStartY;

            // Only handle horizontal swipes
            if (Math.abs(deltaX) < SWIPE_THRESHOLD || Math.abs(deltaX) < Math.abs(deltaY)) return;

            if (deltaX > 0 && touchStartX < 50) {
                // Swipe right from left edge - open left panel
                togglePanel('left-panel');
            } else if (deltaX < 0 && touchStartX > window.innerWidth - 50) {
                // Swipe left from right edge - open right panel
                togglePanel('right-panel');
            } else if (deltaX < 0) {
                // Swipe left - close any open panel
                closeAllPanels();
            }
        }, { passive: true });
    }

    /**
     * Handle window resize events.
     */
    function setupResizeHandler() {
        let resizeTimeout;
        window.addEventListener('resize', () => {
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(updateToggleVisibility, 100);
        });
    }

    /**
     * Handle ESC key to close panels.
     */
    function setupKeyboardHandler() {
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && isMobile()) {
                closeAllPanels();
            }
        });
    }

    /**
     * Initialize mobile panel controls.
     */
    function init() {
        // Wait for game container to be available
        const waitForGameContainer = setInterval(() => {
            const gameContainer = document.getElementById('gameContainer');
            if (gameContainer && !gameContainer.classList.contains('hidden')) {
                clearInterval(waitForGameContainer);
                createMobileControls();
                setupSwipeGestures();
                setupResizeHandler();
                setupKeyboardHandler();
            }
        }, 100);

        // Also create controls immediately if container already exists
        if (document.getElementById('gameContainer')) {
            createMobileControls();
            setupSwipeGestures();
            setupResizeHandler();
            setupKeyboardHandler();
        }
    }

    // Register public functions on GameUI
    if (typeof GameUI !== 'undefined') {
        GameUI.toggleMobilePanel = togglePanel;
        GameUI.closeAllMobilePanels = closeAllPanels;
        GameUI.isMobileViewport = isMobile;
    }

    // Auto-initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

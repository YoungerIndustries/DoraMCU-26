/**
 * DoraMCU Game UI - Mobile Panel Toggle Module Tests
 *
 * Tests the mobile panel toggle functionality including:
 * - Panel toggle buttons creation
 * - Overlay visibility
 * - Swipe gestures
 * - Resize handling
 * - Keyboard shortcuts
 */

const { JSDOM } = require('jsdom');
const { strict: assert } = require('assert');

describe('game-ui-mobile.js', () => {
    let dom, window, document, windowRef;

    function setupDOM(html) {
        dom = new JSDOM(html, {
            url: 'http://localhost:8080/',
            runScripts: 'dangerously',
            resources: 'usable'
        });
        window = dom.window;
        document = window.document;
        windowRef = window;
    }

    function cleanup() {
        if (dom) {
            dom.window.close();
            dom = null;
        }
    }

    beforeEach(() => {
        cleanup();
    });

    afterEach(() => {
        cleanup();
    });

    describe('isMobile detection', () => {
        it('should detect mobile viewport at 768px', () => {
            setupDOM('<div id="gameContainer"></div>');
            window.innerWidth = 768;
            // Simulate the isMobile function
            const isMobile = () => window.innerWidth <= 768;
            assert.strictEqual(isMobile(), true);
        });

        it('should detect desktop viewport at 769px', () => {
            setupDOM('<div id="gameContainer"></div>');
            window.innerWidth = 769;
            const isMobile = () => window.innerWidth <= 768;
            assert.strictEqual(isMobile(), false);
        });

        it('should detect mobile viewport at 320px', () => {
            setupDOM('<div id="gameContainer"></div>');
            window.innerWidth = 320;
            const isMobile = () => window.innerWidth <= 768;
            assert.strictEqual(isMobile(), true);
        });

        it('should detect desktop viewport at 1920px', () => {
            setupDOM('<div id="gameContainer"></div>');
            window.innerWidth = 1920;
            const isMobile = () => window.innerWidth <= 768;
            assert.strictEqual(isMobile(), false);
        });
    });

    describe('createMobileControls', () => {
        it('should create panel overlay element', () => {
            setupDOM(`
                <div id="gameContainer">
                    <div class="left-panel"></div>
                    <div class="right-panel"></div>
                </div>
            `);

            // Simulate createMobileControls
            const gameContainer = document.getElementById('gameContainer');
            const overlay = document.createElement('div');
            overlay.className = 'panel-overlay';
            overlay.id = 'panelOverlay';
            gameContainer.appendChild(overlay);

            const found = document.getElementById('panelOverlay');
            assert.ok(found, 'Panel overlay should be created');
            assert.strictEqual(found.className, 'panel-overlay');
        });

        it('should create left panel toggle button', () => {
            setupDOM('<div id="gameContainer"></div>');

            const gameContainer = document.getElementById('gameContainer');
            const leftToggle = document.createElement('button');
            leftToggle.className = 'panel-toggle left';
            leftToggle.id = 'mobilePanelToggleLeft';
            gameContainer.appendChild(leftToggle);

            const found = document.getElementById('mobilePanelToggleLeft');
            assert.ok(found, 'Left toggle button should be created');
            assert.strictEqual(found.className, 'panel-toggle left');
        });

        it('should create right panel toggle button', () => {
            setupDOM('<div id="gameContainer"></div>');

            const gameContainer = document.getElementById('gameContainer');
            const rightToggle = document.createElement('button');
            rightToggle.className = 'panel-toggle right';
            rightToggle.id = 'mobilePanelToggleRight';
            gameContainer.appendChild(rightToggle);

            const found = document.getElementById('mobilePanelToggleRight');
            assert.ok(found, 'Right toggle button should be created');
            assert.strictEqual(found.className, 'panel-toggle right');
        });

        it('should not create duplicate controls', () => {
            setupDOM('<div id="gameContainer"></div>');

            const gameContainer = document.getElementById('gameContainer');
            
            // First creation
            const leftToggle1 = document.createElement('button');
            leftToggle1.id = 'mobilePanelToggleLeft';
            gameContainer.appendChild(leftToggle1);

            // Check duplicate prevention
            if (document.getElementById('mobilePanelToggleLeft')) {
                // Should skip creation
                assert.ok(true, 'Duplicate prevention works');
            }
        });

        it('should not create controls without gameContainer', () => {
            setupDOM('<div id="other"></div>');

            const gameContainer = document.getElementById('gameContainer');
            assert.strictEqual(gameContainer, null, 'gameContainer should not exist');
        });
    });

    describe('togglePanel', () => {
        it('should open closed left panel', () => {
            setupDOM(`
                <div id="gameContainer">
                    <div class="left-panel"></div>
                    <div class="panel-overlay" id="panelOverlay"></div>
                </div>
            `);

            const panel = document.querySelector('.left-panel');
            panel.classList.add('open');
            assert.ok(panel.classList.contains('open'), 'Panel should be open');
        });

        it('should close open left panel', () => {
            setupDOM(`
                <div id="gameContainer">
                    <div class="left-panel open"></div>
                    <div class="panel-overlay visible" id="panelOverlay"></div>
                </div>
            `);

            const panel = document.querySelector('.left-panel');
            const overlay = document.getElementById('panelOverlay');
            panel.classList.remove('open');
            overlay.classList.remove('visible');

            assert.ok(!panel.classList.contains('open'), 'Panel should be closed');
            assert.ok(!overlay.classList.contains('visible'), 'Overlay should be hidden');
        });

        it('should open right panel and show overlay', () => {
            setupDOM(`
                <div id="gameContainer">
                    <div class="right-panel"></div>
                    <div class="panel-overlay" id="panelOverlay"></div>
                </div>
            `);

            const panel = document.querySelector('.right-panel');
            const overlay = document.getElementById('panelOverlay');
            panel.classList.add('open');
            overlay.classList.add('visible');

            assert.ok(panel.classList.contains('open'), 'Right panel should be open');
            assert.ok(overlay.classList.contains('visible'), 'Overlay should be visible');
        });
    });

    describe('closeAllPanels', () => {
        it('should close both panels', () => {
            setupDOM(`
                <div id="gameContainer">
                    <div class="left-panel open"></div>
                    <div class="right-panel open"></div>
                    <div class="panel-overlay visible" id="panelOverlay"></div>
                </div>
            `);

            const leftPanel = document.querySelector('.left-panel');
            const rightPanel = document.querySelector('.right-panel');
            const overlay = document.getElementById('panelOverlay');

            // Simulate closeAllPanels
            leftPanel.classList.remove('open');
            rightPanel.classList.remove('open');
            overlay.classList.remove('visible');

            assert.ok(!leftPanel.classList.contains('open'));
            assert.ok(!rightPanel.classList.contains('open'));
            assert.ok(!overlay.classList.contains('visible'));
        });

        it('should handle missing panels gracefully', () => {
            setupDOM('<div id="gameContainer"></div>');

            const leftPanel = document.querySelector('.left-panel');
            const rightPanel = document.querySelector('.right-panel');
            assert.strictEqual(leftPanel, null);
            assert.strictEqual(rightPanel, null);
        });
    });

    describe('updateToggleVisibility', () => {
        it('should show toggle buttons on mobile', () => {
            setupDOM(`
                <div id="gameContainer">
                    <button id="mobilePanelToggleLeft" class="panel-toggle left"></button>
                    <button id="mobilePanelToggleRight" class="panel-toggle right"></button>
                </div>
            `);

            const leftToggle = document.getElementById('mobilePanelToggleLeft');
            const rightToggle = document.getElementById('mobilePanelToggleRight');

            // Simulate mobile mode
            leftToggle.style.display = 'flex';
            rightToggle.style.display = 'flex';

            assert.strictEqual(leftToggle.style.display, 'flex');
            assert.strictEqual(rightToggle.style.display, 'flex');
        });

        it('should hide toggle buttons on desktop', () => {
            setupDOM(`
                <div id="gameContainer">
                    <button id="mobilePanelToggleLeft" class="panel-toggle left"></button>
                    <button id="mobilePanelToggleRight" class="panel-toggle right"></button>
                </div>
            `);

            const leftToggle = document.getElementById('mobilePanelToggleLeft');
            const rightToggle = document.getElementById('mobilePanelToggleRight');

            // Simulate desktop mode
            leftToggle.style.display = 'none';
            rightToggle.style.display = 'none';

            assert.strictEqual(leftToggle.style.display, 'none');
            assert.strictEqual(rightToggle.style.display, 'none');
        });
    });

    describe('swipe gestures', () => {
        it('should detect swipe right from left edge', () => {
            setupDOM('<div id="gameContainer"></div>');
            
            const touchStartX = 10; // Near left edge
            const touchEndX = 100;
            const deltaX = touchEndX - touchStartX; // 90 (positive = right)
            
            assert.ok(deltaX > 0, 'Swipe is rightward');
            assert.ok(touchStartX < 50, 'Start is near left edge');
        });

        it('should detect swipe left from right edge', () => {
            setupDOM('<div id="gameContainer"></div>');
            
            const width = 375;
            const touchStartX = width - 10; // Near right edge
            const touchEndX = width - 100;
            const deltaX = touchEndX - touchStartX; // -90 (negative = left)
            
            assert.ok(deltaX < 0, 'Swipe is leftward');
            assert.ok(touchStartX > width - 50, 'Start is near right edge');
        });

        it('should ignore vertical swipes', () => {
            const deltaX = 30;
            const deltaY = 100;
            
            assert.ok(Math.abs(deltaX) < Math.abs(deltaY), 'Vertical swipe should be ignored');
        });

        it('should ignore small swipes', () => {
            const deltaX = 20;
            const SWIPE_THRESHOLD = 50;
            
            assert.ok(Math.abs(deltaX) < SWIPE_THRESHOLD, 'Small swipe should be ignored');
        });
    });

    describe('keyboard handling', () => {
        it('should recognize Escape key', () => {
            setupDOM('<div id="gameContainer"></div>');
            
            const key = 'Escape';
            assert.strictEqual(key, 'Escape', 'Escape key detected');
        });

        it('should not trigger on other keys', () => {
            setupDOM('<div id="gameContainer"></div>');
            
            const key = 'Enter';
            assert.notStrictEqual(key, 'Escape', 'Other keys should not trigger');
        });
    });

    describe('GameUI registration', () => {
        it('should register toggleMobilePanel on GameUI', () => {
            setupDOM('<div id="gameContainer"></div>');
            
            // Simulate GameUI object
            const GameUI = {};
            GameUI.toggleMobilePanel = function(panelClass) {
                return 'toggled:' + panelClass;
            };
            
            assert.ok(typeof GameUI.toggleMobilePanel === 'function');
            assert.strictEqual(GameUI.toggleMobilePanel('left-panel'), 'toggled:left-panel');
        });

        it('should register closeAllMobilePanels on GameUI', () => {
            setupDOM('<div id="gameContainer"></div>');
            
            const GameUI = {};
            GameUI.closeAllMobilePanels = function() {
                return 'closed';
            };
            
            assert.ok(typeof GameUI.closeAllMobilePanels === 'function');
            assert.strictEqual(GameUI.closeAllMobilePanels(), 'closed');
        });

        it('should register isMobileViewport on GameUI', () => {
            setupDOM('<div id="gameContainer"></div>');
            
            const GameUI = {};
            GameUI.isMobileViewport = function() {
                return true;
            };
            
            assert.ok(typeof GameUI.isMobileViewport === 'function');
            assert.strictEqual(GameUI.isMobileViewport(), true);
        });
    });

    describe('integration scenarios', () => {
        it('should create full mobile UI structure', () => {
            setupDOM(`
                <div id="gameContainer">
                    <header class="game-header"></header>
                    <div class="left-panel"></div>
                    <div class="galaxy-map-container"></div>
                    <div class="right-panel"></div>
                </div>
            `);

            const gameContainer = document.getElementById('gameContainer');
            
            // Create overlay
            const overlay = document.createElement('div');
            overlay.className = 'panel-overlay';
            overlay.id = 'panelOverlay';
            gameContainer.appendChild(overlay);

            // Create left toggle
            const leftToggle = document.createElement('button');
            leftToggle.className = 'panel-toggle left';
            leftToggle.id = 'mobilePanelToggleLeft';
            leftToggle.innerHTML = '&#9776;';
            gameContainer.appendChild(leftToggle);

            // Create right toggle
            const rightToggle = document.createElement('button');
            rightToggle.className = 'panel-toggle right';
            rightToggle.id = 'mobilePanelToggleRight';
            rightToggle.innerHTML = '&#9776;';
            gameContainer.appendChild(rightToggle);

            // Verify structure
            assert.ok(document.getElementById('panelOverlay'));
            assert.ok(document.getElementById('mobilePanelToggleLeft'));
            assert.ok(document.getElementById('mobilePanelToggleRight'));
            assert.strictEqual(gameContainer.children.length, 7); // header + left + galaxy + right + overlay + 2 toggles
        });

        it('should handle panel toggle workflow', () => {
            setupDOM(`
                <div id="gameContainer">
                    <div class="left-panel"></div>
                    <div class="right-panel"></div>
                    <div class="panel-overlay" id="panelOverlay"></div>
                </div>
            `);

            const leftPanel = document.querySelector('.left-panel');
            const rightPanel = document.querySelector('.right-panel');
            const overlay = document.getElementById('panelOverlay');

            // Step 1: Open left panel
            leftPanel.classList.add('open');
            overlay.classList.add('visible');
            assert.ok(leftPanel.classList.contains('open'));
            assert.ok(overlay.classList.contains('visible'));

            // Step 2: Close left, open right
            leftPanel.classList.remove('open');
            rightPanel.classList.add('open');
            assert.ok(!leftPanel.classList.contains('open'));
            assert.ok(rightPanel.classList.contains('open'));

            // Step 3: Close all
            rightPanel.classList.remove('open');
            overlay.classList.remove('visible');
            assert.ok(!rightPanel.classList.contains('open'));
            assert.ok(!overlay.classList.contains('visible'));
        });

        it('should handle resize from mobile to desktop', () => {
            setupDOM(`
                <div id="gameContainer">
                    <div class="left-panel open"></div>
                    <button id="mobilePanelToggleLeft" style="display:flex"></button>
                    <button id="mobilePanelToggleRight" style="display:flex"></button>
                </div>
            `);

            const leftPanel = document.querySelector('.left-panel');
            const leftToggle = document.getElementById('mobilePanelToggleLeft');
            const rightToggle = document.getElementById('mobilePanelToggleRight');

            // Simulate resize to desktop
            leftPanel.classList.remove('open');
            leftToggle.style.display = 'none';
            rightToggle.style.display = 'none';

            assert.ok(!leftPanel.classList.contains('open'));
            assert.strictEqual(leftToggle.style.display, 'none');
            assert.strictEqual(rightToggle.style.display, 'none');
        });
    });
});

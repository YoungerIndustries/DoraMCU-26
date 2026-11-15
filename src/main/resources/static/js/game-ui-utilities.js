/**
 * DoraMCU Game UI - UTILITIES Module
 * Split from game-ui-main.js for modularity.
 */
(function() {
    'use strict';

    function hideModal(id) {
        const modal = document.getElementById(id);
        if (modal) modal.classList.add('hidden');
    }

    // Utility
    function escapeHtml(str) {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // --------------------------------------------------------
    // Toast Notifications
    // --------------------------------------------------------
    function showToast(message, type) {
        const container = document.getElementById('toastContainer');
        if (!container) return;

        const icons = {
            success: 'Ã¢Å“â€¦',
            error: 'Ã¢ÂÅ’',
            warning: 'Ã¢Å¡Â Ã¯Â¸Â',
            info: 'Ã¢â€žÂ¹Ã¯Â¸Â'
        };

        const toast = document.createElement('div');
        toast.className = `toast ${type || 'info'}`;
        toast.innerHTML = `
            <span class="toast-icon">${icons[type] || 'Ã¢â€žÂ¹Ã¯Â¸Â'}</span>
            <div class="toast-content">
                <div class="toast-message">${GameUI.escapeHtml(message)}</div>
            </div>
            <button class="toast-close">&times;</button>
            <div class="toast-progress"></div>
        `;

        toast.querySelector('.toast-close').addEventListener('click', () => {
            GameUI.removeToast(toast);
        });

        container.appendChild(toast);

        setTimeout(() => {
            GameUI.removeToast(toast);
        }, 4000);
    }

    function removeToast(toast) {
        if (!toast || !toast.parentNode) return;
        toast.style.animation = 'toastSlideOut 0.3s ease-in forwards';
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }

    // --------------------------------------------------------
    // Command Bar (Ctrl+K)
    // --------------------------------------------------------
    function showCommandBar() {
        const commandBar = document.getElementById('commandBar');
        const input = document.getElementById('commandBarInput');
        if (commandBar) {
            commandBar.classList.remove('hidden');
            GameUIState.commandBarVisible = true;
            if (input) {
                input.value = '';
                input.focus();
                const items = commandBar.querySelectorAll('.command-bar-item');
                items.forEach(item => item.style.display = 'flex');
            }
        }
    }

    function hideCommandBar() {
        const commandBar = document.getElementById('commandBar');
        if (commandBar) {
            commandBar.classList.add('hidden');
            GameUIState.commandBarVisible = false;
        }
    }

    // --------------------------------------------------------
    // Panel Collapse
    // --------------------------------------------------------
    function setupPanelCollapse() {
        document.addEventListener('click', (e) => {
            const header = e.target.closest('.panel-section h3');
            if (header) {
                const section = header.closest('.panel-section');
                if (section) {
                    section.classList.toggle('collapsed');
                }
            }
        });
    }

    // --------------------------------------------------------
    // Command Bar Setup (Ctrl+K)
    // --------------------------------------------------------
    function setupCommandBar() {
        const commandBar = document.getElementById('commandBar');
        const input = document.getElementById('commandBarInput');
        const results = document.getElementById('commandBarResults');

        if (!commandBar || !input) return;

        input.addEventListener('input', () => {
            const query = input.value.toLowerCase().trim();
            const items = results.querySelectorAll('.command-bar-item');
            items.forEach(item => {
                const title = item.querySelector('.command-bar-item-title')?.textContent?.toLowerCase() || '';
                const desc = item.querySelector('.command-bar-item-desc')?.textContent?.toLowerCase() || '';
                const action = item.dataset.action || '';
                const match = !query || title.includes(query) || desc.includes(query) || action.includes(query);
                item.style.display = match ? 'flex' : 'none';
            });
            GameUIState.commandBarSelectedIndex = 0;
        });

        input.addEventListener('keydown', (e) => {
            const visibleItems = Array.from(results.querySelectorAll('.command-bar-item'))
                .filter(item => item.style.display !== 'none');

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                GameUIState.commandBarSelectedIndex = Math.min(GameUIState.commandBarSelectedIndex + 1, visibleItems.length - 1);
                updateCommandBarSelection(visibleItems);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                GameUIState.commandBarSelectedIndex = Math.max(GameUIState.commandBarSelectedIndex - 1, 0);
                updateCommandBarSelection(visibleItems);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                const selected = visibleItems[GameUIState.commandBarSelectedIndex];
                if (selected) {
                    executeCommand(selected.dataset.action);
                    GameUI.hideCommandBar();
                }
            } else if (e.key === 'Escape') {
                GameUI.hideCommandBar();
            }
        });

        results.addEventListener('click', (e) => {
            const item = e.target.closest('.command-bar-item');
            if (item) {
                executeCommand(item.dataset.action);
                GameUI.hideCommandBar();
            }
        });

        commandBar.addEventListener('click', (e) => {
            if (e.target === commandBar) {
                GameUI.hideCommandBar();
            }
        });
    }

    function updateCommandBarSelection(items) {
        items.forEach((item, i) => {
            item.classList.toggle('selected', i === GameUIState.commandBarSelectedIndex);
        });
        if (items[GameUIState.commandBarSelectedIndex]) {
            items[GameUIState.commandBarSelectedIndex].scrollIntoView({ block: 'nearest' });
        }
    }

    function executeCommand(action) {
        switch (action) {
            case 'advance-turn':
                const advanceBtn = document.getElementById('advanceTurnBtn');
                if (advanceBtn && !advanceBtn.disabled) advanceBtn.click();
                break;
            case 'build-ship':
                const buildBtn = document.getElementById('buildShipBtn');
                if (buildBtn && !buildBtn.disabled) buildBtn.click();
                break;
            case 'move-fleet':
                const moveBtn = document.getElementById('moveFleetBtn');
                if (moveBtn && !moveBtn.disabled) moveBtn.click();
                break;
            case 'engage-combat':
                const combatBtn = document.getElementById('engageCombatBtn');
                if (combatBtn && !combatBtn.disabled) combatBtn.click();
                break;
            case 'zoom-in':
                const zoomInBtn = document.getElementById('zoomInBtn');
                if (zoomInBtn) zoomInBtn.click();
                break;
            case 'zoom-out':
                const zoomOutBtn = document.getElementById('zoomOutBtn');
                if (zoomOutBtn) zoomOutBtn.click();
                break;
            case 'reset-view':
                const resetBtn = document.getElementById('resetViewBtn');
                if (resetBtn) resetBtn.click();
                break;
            case 'develop-character':
                if (typeof CharacterUI !== 'undefined' && CharacterUI.showCharacterModal) {
                    CharacterUI.showCharacterModal();
                }
                break;
            case 'trade-market':
                if (typeof TradeUI !== 'undefined' && TradeUI.showTradeModal && GameUIState.sessionId && GameUIState.currentSystemId) {
                    TradeUI.showTradeModal(GameUIState.sessionId, GameUIState.currentSystemId);
                } else {
                    GameUI.showToast('Navigate to a system first', 'warning');
                }
                break;
        }
    }

    // --------------------------------------------------------
    // Keyboard Shortcuts
    // --------------------------------------------------------
    function setupKeyboardShortcuts() {
        document.addEventListener('keydown', (e) => {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') {
                if (e.key === 'Escape' && GameUIState.commandBarVisible) {
                    GameUI.hideCommandBar();
                    return;
                }
                return;
            }

            if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
                e.preventDefault();
                if (GameUIState.commandBarVisible) {
                    GameUI.hideCommandBar();
                } else {
                    GameUI.showCommandBar();
                }
                return;
            }

            if (e.key === 'Escape') {
                if (GameUIState.commandBarVisible) {
                    GameUI.hideCommandBar();
                    return;
                }
                const modals = document.querySelectorAll('.modal-overlay:not(.hidden)');
                if (modals.length > 0) {
                    modals[modals.length - 1].classList.add('hidden');
                    return;
                }
                return;
            }

            if (e.key === 'Enter' && !GameUIState.commandBarVisible) {
                const modals = document.querySelectorAll('.modal-overlay:not(.hidden)');
                if (modals.length === 0) {
                    const advanceBtn = document.getElementById('advanceTurnBtn');
                    if (advanceBtn && !advanceBtn.disabled) {
                        advanceBtn.click();
                    }
                }
                return;
            }

            if (e.key === 'b' || e.key === 'B') {
                const buildBtn = document.getElementById('buildShipBtn');
                if (buildBtn && !buildBtn.disabled) buildBtn.click();
                return;
            }

            if (e.key === 'm' || e.key === 'M') {
                const moveBtn = document.getElementById('moveFleetBtn');
                if (moveBtn && !moveBtn.disabled) moveBtn.click();
                return;
            }

            if (e.key === 'c' || e.key === 'C') {
                const combatBtn = document.getElementById('engageCombatBtn');
                if (combatBtn && !combatBtn.disabled) combatBtn.click();
                return;
            }

            if (e.key === '+' || e.key === '=') {
                const zoomInBtn = document.getElementById('zoomInBtn');
                if (zoomInBtn) zoomInBtn.click();
                return;
            }

            if (e.key === '-') {
                const zoomOutBtn = document.getElementById('zoomOutBtn');
                if (zoomOutBtn) zoomOutBtn.click();
                return;
            }

            if (e.key === 'r' || e.key === 'R') {
                const resetBtn = document.getElementById('resetViewBtn');
                if (resetBtn) resetBtn.click();
                return;
            }
        });
    }

    // --------------------------------------------------------
    // Event Log Filter
    // --------------------------------------------------------
    function setupEventLogFilter() {
        const filterBtns = document.querySelectorAll('.event-log-filter-btn');
        filterBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                filterBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');

                const filter = btn.dataset.filter;
                const entries = document.querySelectorAll('.event-entry');
                entries.forEach(entry => {
                    if (filter === 'all') {
                        entry.style.display = 'flex';
                    } else {
                        const typeEl = entry.querySelector('.event-type');
                        if (typeEl && typeEl.textContent === filter) {
                            entry.style.display = 'flex';
                        } else {
                            entry.style.display = 'none';
                        }
                    }
                });
            });
        });
    }

    // --------------------------------------------------------
    // Minimap
    // --------------------------------------------------------
    function setupMinimap() {
        const minimapCanvas = document.getElementById('minimapCanvas');
        if (!minimapCanvas) return;

        const minimap = document.getElementById('minimap');
        minimapCanvas.width = minimap.offsetWidth;
        minimapCanvas.height = minimap.offsetHeight;

        minimap.addEventListener('click', (e) => {
            const rect = minimap.getBoundingClientRect();
            const ratioX = (e.clientX - rect.left) / rect.width;
            const ratioY = (e.clientY - rect.top) / rect.height;

            const galaxyCanvas = document.getElementById('galaxyCanvas');
            if (!galaxyCanvas) return;

            const targetX = -(ratioX * galaxyCanvas.width - galaxyCanvas.width / 2);
            const targetY = -(ratioY * galaxyCanvas.height - galaxyCanvas.height / 2);

            const startX = window._mapOffsetX || 0;
            const startY = window._mapOffsetY || 0;
            const duration = 300;
            const startTime = performance.now();

            function animatePan(time) {
                const elapsed = time - startTime;
                const progress = Math.min(1, elapsed / duration);
                const ease = 1 - Math.pow(1 - progress, 3);

                window._mapOffsetX = startX + (targetX - startX) * ease;
                window._mapOffsetY = startY + (targetY - startY) * ease;

                mapOffsetXDirect(targetX * ease + startX * (1 - ease));
                mapOffsetYDirect(targetY * ease + startY * (1 - ease));

                if (progress < 1) {
                    requestAnimationFrame(animatePan);
                }
            }
            requestAnimationFrame(animatePan);
        });
    }

    function mapOffsetXDirect(value) {
        const canvas = document.getElementById('galaxyCanvas');
        if (canvas) canvas.dataset.mapOffsetX = value;
    }

    function mapOffsetYDirect(value) {
        const canvas = document.getElementById('galaxyCanvas');
        if (canvas) canvas.dataset.mapOffsetY = value;
    }

    // --------------------------------------------------------
    // Resource Change Animation
    // --------------------------------------------------------
    function updateResourceChange(resourceId, oldValue, newValue) {
        const changeEl = document.getElementById(resourceId + 'Change');
        if (!changeEl) return;

        const diff = newValue - oldValue;
        if (diff === 0) {
            changeEl.textContent = '+0';
            changeEl.className = 'resource-change positive';
            return;
        }

        const sign = diff > 0 ? '+' : '';
        changeEl.textContent = sign + diff;
        changeEl.className = `resource-change ${diff > 0 ? 'positive' : 'negative'}`;

        changeEl.style.transform = 'scale(1.3)';
        changeEl.style.opacity = '1';
        setTimeout(() => {
            changeEl.style.transform = 'scale(1)';
        }, 300);

    // Register public functions on GameUI
    GameUI.hideModal = hideModal;
    GameUI.escapeHtml = escapeHtml;
    GameUI.showToast = showToast;
    GameUI.removeToast = removeToast;
    GameUI.showCommandBar = showCommandBar;
    GameUI.hideCommandBar = hideCommandBar;
    GameUI.setupPanelCollapse = setupPanelCollapse;
    GameUI.setupCommandBar = setupCommandBar;
    GameUI.updateCommandBarSelection = updateCommandBarSelection;
    GameUI.executeCommand = executeCommand;
    GameUI.setupKeyboardShortcuts = setupKeyboardShortcuts;
    GameUI.setupEventLogFilter = setupEventLogFilter;
    GameUI.setupMinimap = setupMinimap;
    GameUI.animatePan = animatePan;
    GameUI.mapOffsetXDirect = mapOffsetXDirect;
    GameUI.mapOffsetYDirect = mapOffsetYDirect;
    GameUI.updateResourceChange = updateResourceChange;
}());

// Main View
// Main gameplay container with sidebar navigation and panel switching
// Preserves panel state across switches using show/hide pattern

class MainView {
    constructor(app) {
        this.app = app;
        this.element = null;
        this.activePanel = 'galaxy3d';
        this.renderedPanels = {};
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'view-panel';
        this.element.id = 'main-view';
        this.element.innerHTML = `
            <div class="main-layout">
                <div class="sidebar" id="main-sidebar">
                    <div class="sidebar-item active" data-panel="galaxy3d">
                        <span>ðŸŒŒ</span><span>3D Galaxy</span>
                    </div>
                    <div class="sidebar-item" data-panel="map">
                        <span>ðŸ—ºï¸</span><span>2D Map</span>
                    </div>
                    <div class="sidebar-item" data-panel="shipBuild">
                        <span>ðŸš€</span><span>Ship Builder</span>
                    </div>
                    <div class="sidebar-item" data-panel="inventory">
                        <span>ðŸ“¦</span><span>Inventory</span>
                    </div>
                    <div class="sidebar-item" data-panel="menu">
                        <span>â˜°</span><span>Menu</span>
                    </div>
                    <div style="flex: 1;"></div>
                    <div class="sidebar-item" data-panel="help">
                        <span>â“</span><span>Help</span>
                    </div>
                </div>
                <div class="main-content">
                    <div class="hud-bar" id="main-hud"></div>
                    <div class="content-area" id="main-content"></div>
                </div>
            </div>
        `;

        // Bind sidebar
        this.element.querySelectorAll('.sidebar-item').forEach(item => {
            item.addEventListener('click', () => {
                const panel = item.dataset.panel;
                this._switchPanel(panel);
            });
        });

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        // Render HUD
        const hudEl = this.element.querySelector('#main-hud');
        if (hudEl && !hudEl.querySelector('.hud-item')) {
            const hudView = this.app.views.hud;
            if (hudView) {
                const hudContent = hudView.render();
                hudEl.appendChild(hudContent);
            }
        }

        // Pre-render all panels and hide them
        const contentEl = this.element.querySelector('#main-content');
        const panels = ['galaxy3d', 'map', 'shipBuild', 'inventory', 'menu', 'help'];
        panels.forEach(panelName => {
            const view = this.app.views[panelName];
            if (view && !this.renderedPanels[panelName]) {
                const panelEl = view.render();
                panelEl.style.display = 'none';
                contentEl.appendChild(panelEl);
                this.renderedPanels[panelName] = panelEl;
            }
        });

        // Switch to default panel - 3D Galaxy view
        this._switchPanel('galaxy3d');
    }

    _switchPanel(panelName) {
        this.activePanel = panelName;

        // Update sidebar
        this.element.querySelectorAll('.sidebar-item').forEach(item => {
            item.classList.toggle('active', item.dataset.panel === panelName);
        });

        // Hide all panels and call hide() lifecycle
        Object.entries(this.renderedPanels).forEach(([name, el]) => {
            el.style.display = name === panelName ? 'block' : 'none';
            if (name !== panelName) {
                const view = this.app.views[name];
                if (view && typeof view.hide === 'function') {
                    view.hide();
                }
            }
        });

        // Call show() on the active view to refresh data
        const view = this.app.views[panelName];
        if (view && typeof view.show === 'function') {
            view.show();
        }
    }
}

window.MainView = MainView;

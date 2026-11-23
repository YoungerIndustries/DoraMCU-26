// HUD View
// Resource HUD displaying credits, minerals, corporation, character

class HudView {
    constructor(app) {
        this.app = app;
        this.element = null;

        // Subscribe to resource updates
        app.state.on('resources:updated', () => this._update());
        app.state.on('player:updated', () => this._update());
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'hud-bar';
        this.element.id = 'hud-bar';
        this._update();
        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        this._update();
    }

    _update() {
        if (!this.element) return;

        const state = this.app.state;
        const credits = state.getCredits();
        const minerals = state.getMinerals();
        const ships = state.getShipCount();
        const fleets = state.getFleetCount();
        const planets = state.getPlanetCount();
        const corp = state.corporationName || '—';
        const username = state.username || '—';

        this.element.innerHTML = `
            <div class="hud-item">
                <span class="icon">💰</span>
                <span class="label">Credits</span>
                <span class="value">${credits.toLocaleString()}</span>
            </div>
            <div class="hud-item">
                <span class="icon">⛏️</span>
                <span class="label">Minerals</span>
                <span class="value">${minerals.toLocaleString()}</span>
            </div>
            <div class="hud-item">
                <span class="icon">🚀</span>
                <span class="label">Ships</span>
                <span class="value">${ships}</span>
            </div>
            <div class="hud-item">
                <span class="icon">🛸</span>
                <span class="label">Fleets</span>
                <span class="value">${fleets}</span>
            </div>
            <div class="hud-item">
                <span class="icon">🪐</span>
                <span class="label">Planets</span>
                <span class="value">${planets}</span>
            </div>
            <div style="flex: 1;"></div>
            <div class="hud-item">
                <span class="label">Corp:</span>
                <span class="value">${corp}</span>
            </div>
            <div class="hud-item">
                <span class="label">Player:</span>
                <span class="value">${username}</span>
            </div>
        `;
    }
}

window.HudView = HudView;

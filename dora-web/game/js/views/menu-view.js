// Menu View
// Main menu and pause menu with save/logout

class MenuView {
    constructor(app) {
        this.app = app;
        this.element = null;
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'panel';
        this.element.id = 'menu-panel';
        this.element.innerHTML = `
            <div class="panel-header">
                <span>Menu</span>
            </div>
            <div class="panel-body">
                <div class="card" style="margin-bottom: 16px;">
                    <div class="card-header">Game</div>
                    <div class="card-body">
                        <button class="btn" style="width: 100%; margin-bottom: 8px;" id="save-game-btn">💾 Save Game</button>
                        <button class="btn" style="width: 100%; margin-bottom: 8px;" id="load-game-btn">📂 Load Game</button>
                    </div>
                </div>
                <div class="card" style="margin-bottom: 16px;">
                    <div class="card-header">Session</div>
                    <div class="card-body">
                        <button class="btn" style="width: 100%; margin-bottom: 8px;" id="back-galaxy-btn">🌌 Galaxy Selection</button>
                        <button class="btn btn-danger" style="width: 100%;" id="logout-btn">🚪 Logout</button>
                    </div>
                </div>
                <div class="card">
                    <div class="card-header">Info</div>
                    <div class="card-body">
                        <p>Player: ${this.app.state.username || '—'}</p>
                        <p>Corporation: ${this.app.state.corporationName || '—'}</p>
                        <p>Galaxy: ${this.app.state.currentGalaxy ? this.app.state.currentGalaxy.name : '—'}</p>
                    </div>
                </div>
                <div id="menu-result" style="margin-top: 12px;"></div>
            </div>
        `;

        this.resultEl = this.element.querySelector('#menu-result');

        this.element.querySelector('#save-game-btn').addEventListener('click', () => this._saveGame());
        this.element.querySelector('#load-game-btn').addEventListener('click', () => this._loadGame());
        this.element.querySelector('#back-galaxy-btn').addEventListener('click', () => this.app.switchView('galaxy'));
        this.element.querySelector('#logout-btn').addEventListener('click', () => this._logout());

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        // Update info
        const infoCard = this.element.querySelector('.card:last-of-type .card-body');
        if (infoCard) {
            infoCard.innerHTML = `
                <p>Player: ${this.app.state.username || '—'}</p>
                <p>Corporation: ${this.app.state.corporationName || '—'}</p>
                <p>Galaxy: ${this.app.state.currentGalaxy ? this.app.state.currentGalaxy.name : '—'}</p>
            `;
        }
    }

    async _saveGame() {
        this.resultEl.innerHTML = '<div style="color: var(--text-muted);">Saving...</div>';
        try {
            await this.app.saveGame();
            this.resultEl.innerHTML = '<div style="color: var(--accent-success);">Game saved!</div>';
        } catch (error) {
            this.resultEl.innerHTML = `<div style="color: var(--accent-danger);">Save failed: ${error.message}</div>`;
        }
    }

    async _loadGame() {
        this.resultEl.innerHTML = '<div style="color: var(--text-muted);">Loading saved sessions...</div>';
        try {
            const sessions = await this.app.api.listSaveableSessions();
            if (sessions && sessions.length > 0) {
                const session = sessions[0];
                const data = await this.app.api.loadGame(session.sessionId);
                if (data) {
                    this.app.state.setPlayer(data.player || {});
                    this.resultEl.innerHTML = `<div style="color: var(--accent-success);">Loaded: ${session.sessionName}</div>`;
                }
            } else {
                this.resultEl.innerHTML = '<div style="color: var(--text-muted);">No saved games found.</div>';
            }
        } catch (error) {
            this.resultEl.innerHTML = `<div style="color: var(--accent-danger);">Load failed: ${error.message}</div>`;
        }
    }

    async _logout() {
        if (confirm('Are you sure you want to logout?')) {
            await this.app.logout();
        }
    }
}

window.MenuView = MenuView;

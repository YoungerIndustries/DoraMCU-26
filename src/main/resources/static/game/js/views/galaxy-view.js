// Galaxy View
// Galaxy selection screen with health/completion cards

class GalaxyView {
    constructor(app) {
        this.app = app;
        this.element = null;
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'view-panel';
        this.element.id = 'galaxy-view';
        this.element.innerHTML = `
            <div class="galaxy-container">
                <h1 style="margin-bottom: 8px; color: var(--accent-primary);">Select Galaxy</h1>
                <p style="color: var(--text-secondary); margin-bottom: 24px;">Choose a galaxy to explore</p>
                <div class="galaxy-grid" id="galaxy-grid"></div>
                <div style="margin-top: 24px; display: flex; gap: 12px;">
                    <button class="btn" id="refresh-galaxies-btn">↻ Refresh</button>
                    <button class="btn btn-primary" id="enter-galaxy-btn" disabled>Enter Selected Galaxy</button>
                </div>
            </div>
        `;

        this.gridEl = this.element.querySelector('#galaxy-grid');
        this.enterBtn = this.element.querySelector('#enter-galaxy-btn');

        this.element.querySelector('#refresh-galaxies-btn').addEventListener('click', () => this._loadGalaxies());
        this.enterBtn.addEventListener('click', () => this._enterGalaxy());

        this._loadGalaxies();

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        this._loadGalaxies();
    }

    async _loadGalaxies() {
        this.gridEl.innerHTML = '<div style="color: var(--text-muted);">Loading galaxies...</div>';
        try {
            const galaxyNames = await this.app.api.fetchGalaxies();
            if (galaxyNames && galaxyNames.length > 0) {
                // Server returns array of galaxy name strings; convert to objects
                const galaxies = galaxyNames.map(name => ({ name, health: '—', completion: '—' }));
                this.app.state.setGalaxies(galaxies);
                this._renderGalaxies(galaxies);
            } else {
                this.gridEl.innerHTML = '<div style="color: var(--text-muted);">No galaxies available</div>';
            }
        } catch (error) {
            this.gridEl.innerHTML = `<div style="color: var(--accent-danger);">Failed to load galaxies: ${error.message}</div>`;
        }
    }

    _renderGalaxies(galaxies) {
        this.gridEl.innerHTML = '';
        galaxies.forEach(galaxy => {
            const card = document.createElement('div');
            card.className = 'galaxy-card';
            const name = typeof galaxy === 'string' ? galaxy : (galaxy.name || 'Unnamed Galaxy');
            const health = typeof galaxy === 'object' ? (galaxy.health || '—') : '—';
            const completion = typeof galaxy === 'object' ? (galaxy.completion || '—') : '—';
            card.innerHTML = `
                <h3>${name}</h3>
                <div class="galaxy-stats">
                    <span>Health: ${health}</span>
                    <span>Completion: ${completion}</span>
                </div>
            `;
            card.addEventListener('click', () => {
                this.gridEl.querySelectorAll('.galaxy-card').forEach(c => c.style.borderColor = 'var(--border-color)');
                card.style.borderColor = 'var(--accent-primary)';
                this.app.state.selectGalaxy(typeof galaxy === 'string' ? { name } : galaxy);
                this.enterBtn.disabled = false;
            });
            this.gridEl.appendChild(card);
        });
    }

    async _enterGalaxy() {
        if (!this.app.state.currentGalaxy) {
            this.app.state.setError('Please select a galaxy first');
            return;
        }

        this.app.state.setLoading(true, 'Entering galaxy...');
        try {
            await this.app._loadPlayerData();
            this.app.switchView('main');
        } catch (error) {
            this.app.state.setError(`Failed to enter galaxy: ${error.message}`);
        } finally {
            this.app.state.setLoading(false);
        }
    }
}

window.GalaxyView = GalaxyView;

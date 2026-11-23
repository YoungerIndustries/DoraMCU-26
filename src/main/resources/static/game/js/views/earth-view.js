// Earth 2026 Landing View
// Corporation HQ landing page — shown after character creation before entering the main game

class EarthView {
    constructor(app) {
        this.app = app;
        this.element = null;
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'view-panel';
        this.element.id = 'earth-view';
        this.element.innerHTML = `
            <div class="earth-landing">
                <div class="earth-header">
                    <h1 style="text-align: center; margin-bottom: 8px; color: var(--accent-primary);">🌍 Earth 2026</h1>
                    <p style="text-align: center; color: var(--text-muted); margin-bottom: 32px;">Your Corporation HQ Awaits</p>
                </div>

                <div class="earth-content">
                    <div class="card" id="earth-hq-card">
                        <div class="card-header">Corporation Headquarters</div>
                        <div class="card-body">
                            <div id="earth-hq-info" class="loading-state">
                                <p>Loading your corporation HQ...</p>
                            </div>
                        </div>
                    </div>

                    <div class="card" id="earth-locations-card" style="margin-top: 24px;">
                        <div class="card-header">Available Locations</div>
                        <div class="card-body">
                            <div id="earth-locations-list" class="loading-state">
                                <p>Loading locations...</p>
                            </div>
                        </div>
                    </div>

                    <div class="card" id="earth-scenarios-card" style="margin-top: 24px;">
                        <div class="card-header">First Scenarios</div>
                        <div class="card-body">
                            <div id="earth-scenarios-list" class="loading-state">
                                <p>Loading scenarios...</p>
                            </div>
                        </div>
                    </div>

                    <div style="margin-top: 32px; text-align: center;">
                        <button class="btn btn-success btn-lg" id="enter-corp-hq-btn" style="display: none;">
                            Enter Corporation HQ
                        </button>
                        <button class="btn btn-primary btn-lg" id="start-journey-btn" style="display: none;">
                            Begin Your Journey →
                        </button>
                    </div>
                </div>
            </div>
        `;

        this.hqInfoEl = this.element.querySelector('#earth-hq-info');
        this.locationsEl = this.element.querySelector('#earth-locations-list');
        this.scenariosEl = this.element.querySelector('#earth-scenarios-list');
        this.enterHqBtn = this.element.querySelector('#enter-corp-hq-btn');
        this.startJourneyBtn = this.element.querySelector('#start-journey-btn');

        this.enterHqBtn.addEventListener('click', () => this._enterHQ());
        this.startJourneyBtn.addEventListener('click', () => this._startJourney());

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        this._loadEarthData();
    }

    async _loadEarthData() {
        try {
            const data = await this.app.api.fetchEarthLanding(this.app.state.username);
            if (!data) return;

            // Render HQ info
            this.hqInfoEl.innerHTML = `
                <div style="text-align: center; padding: 24px;">
                    <div style="font-size: 3rem; margin-bottom: 16px;">🏢</div>
                    <h2 style="color: var(--accent-primary); margin-bottom: 8px;">${data.landingCity || 'Corporation HQ'}</h2>
                    <p style="color: var(--text-secondary); margin-bottom: 16px;">${data.message || 'Your office is ready.'}</p>
                    <div style="background: var(--bg-hover); padding: 16px; border-radius: var(--border-radius); text-align: left;">
                        <p><strong>Galaxy:</strong> ${data.galaxy || 'Earth 2026'}</p>
                        <p><strong>Location:</strong> ${data.location || 'Earth 2026'}</p>
                    </div>
                </div>
            `;

            // Render locations
            if (data.locations && data.locations.length > 0) {
                let html = '<div class="grid grid-2">';
                data.locations.forEach(loc => {
                    const locName = loc.name || loc.city || loc.label || 'Unknown';
                    const locDesc = loc.description || loc.type || '';
                    html += `
                        <div class="location-card" style="padding: 16px; border: 1px solid var(--border-color); border-radius: var(--border-radius); cursor: pointer; transition: all 150ms ease;" data-location="${locName}">
                            <strong>${locName}</strong>
                            ${locDesc ? `<div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 4px;">${locDesc}</div>` : ''}
                        </div>
                    `;
                });
                html += '</div>';
                this.locationsEl.innerHTML = html;

                // Add click handlers
                this.locationsEl.querySelectorAll('.location-card').forEach(card => {
                    card.addEventListener('click', () => {
                        this.locationsEl.querySelectorAll('.location-card').forEach(c => {
                            c.style.borderColor = 'var(--border-color)';
                            c.style.background = '';
                        });
                        card.style.borderColor = 'var(--accent-primary)';
                        card.style.background = 'var(--bg-hover)';
                    });
                });
            } else {
                this.locationsEl.innerHTML = '<p style="color: var(--text-muted);">No locations available yet.</p>';
            }

            // Render scenarios
            if (data.scenarios && data.scenarios.length > 0) {
                let html = '<div class="grid grid-2">';
                data.scenarios.forEach(scenario => {
                    const scenarioName = scenario.name || scenario.title || 'Unknown';
                    const scenarioDesc = scenario.description || scenario.summary || '';
                    html += `
                        <div class="scenario-card" style="padding: 16px; border: 1px solid var(--border-color); border-radius: var(--border-radius); cursor: pointer; transition: all 150ms ease;" data-scenario="${scenarioName}">
                            <strong>🎯 ${scenarioName}</strong>
                            ${scenarioDesc ? `<div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 4px;">${scenarioDesc}</div>` : ''}
                        </div>
                    `;
                });
                html += '</div>';
                this.scenariosEl.innerHTML = html;
            } else {
                this.scenariosEl.innerHTML = '<p style="color: var(--text-muted);">No scenarios available yet.</p>';
            }

            // Show action buttons
            this.enterHqBtn.style.display = 'inline-flex';
            this.startJourneyBtn.style.display = 'inline-flex';

        } catch (error) {
            console.error('[EarthView] Failed to load Earth 2026 data:', error);
            this.hqInfoEl.innerHTML = `
                <div style="text-align: center; padding: 24px;">
                    <div style="font-size: 3rem; margin-bottom: 16px;">🏢</div>
                    <h2 style="color: var(--accent-primary); margin-bottom: 8px;">Corporation HQ</h2>
                    <p style="color: var(--text-secondary);">Welcome to Earth 2026. Your corporation is ready.</p>
                </div>
            `;
            this.locationsEl.innerHTML = '<p style="color: var(--text-muted);">Locations will be available soon.</p>';
            this.scenariosEl.innerHTML = '<p style="color: var(--text-muted);">Scenarios will be available soon.</p>';
            this.enterHqBtn.style.display = 'inline-flex';
            this.startJourneyBtn.style.display = 'inline-flex';
        }
    }

    async _enterHQ() {
        // Enter the corporation HQ — proceed to main game with corporation context
        this.app.state.setLoading(true, 'Entering Corporation HQ...');
        try {
            // Create a session for the player
            await this.app._createDefaultSession();
            // Load player data (resources, fleets, planets) into the game state
            await this.app._loadPlayerData();
            this.app.switchView('main');
        } catch (error) {
            this.app.state.setError(`Failed to enter HQ: ${error.message}`);
        } finally {
            this.app.state.setLoading(false);
        }
    }

    async _startJourney() {
        // Begin the journey — same as entering HQ for now
        this._enterHQ();
    }
}

window.EarthView = EarthView;

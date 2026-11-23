// Inventory View
// Displays ships, fleets, planets from player/me with fleet movement controls

class InventoryView {
    constructor(app) {
        this.app = app;
        this.element = null;
        this.playerData = null;

        // Listen for fleet selection changes from other views
        this.app.state.on('fleet:selected', () => {
            if (this.playerData) {
                this._render(this.playerData);
            }
        });
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'panel';
        this.element.id = 'inventory-panel';
        this.element.innerHTML = `
            <div class="panel-header">
                <span>Inventory</span>
                <button class="btn btn-sm" id="refresh-inventory-btn">↻ Refresh</button>
            </div>
            <div class="panel-body" id="inventory-content">
                <div style="color: var(--text-muted);">Loading inventory...</div>
            </div>
        `;

        this.contentEl = this.element.querySelector('#inventory-content');
        this.element.querySelector('#refresh-inventory-btn').addEventListener('click', () => this._load());

        this._load();

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        this._load();
    }

    async _load() {
        this.contentEl.innerHTML = '<div style="color: var(--text-muted);">Loading inventory...</div>';
        try {
            const sessionId = this.app.state.sessionId;
            let player;
            if (sessionId) {
                player = await this.app.api.fetchResources(sessionId);
            } else {
                player = await this.app.api.fetchResources();
            }
            if (player) {
                this.app.state.setPlayer(player);
                this.playerData = player;
                this._render(player);
            } else {
                this.contentEl.innerHTML = '<div style="color: var(--text-muted);">No inventory data</div>';
            }
        } catch (error) {
            this.contentEl.innerHTML = `<div style="color: var(--accent-danger);">Failed to load: ${error.message}</div>`;
        }
    }

    _render(player) {
        let html = '';

        // Ships
        html += '<h3 style="margin-bottom: 12px;">Ships</h3>';
        if (player.ships && player.ships.length > 0) {
            html += '<div class="grid grid-2">';
            player.ships.forEach(ship => {
                html += `
                    <div class="card">
                        <div class="card-header">${ship.name || ship.design || 'Unnamed Ship'}</div>
                        <div class="card-body">
                            <p>Design: ${ship.design || '—'}</p>
                            <p>Hull: ${ship.hull || 0} | Drives: ${ship.drives || 0}</p>
                            <p>Cargo: ${ship.cargo || 0} | Fuel: ${ship.fuel || 0}</p>
                            ${ship.combat ? `<p>Combat: Armor ${ship.combat.armor} | Weapons ${ship.combat.weapons} | Shields ${ship.combat.shields}</p>` : ''}
                        </div>
                    </div>
                `;
            });
            html += '</div>';
        } else {
            html += '<p style="color: var(--text-muted); margin-bottom: 16px;">No ships — use Ship Builder (V) to create ships</p>';
        }

        // Fleets
        html += '<h3 style="margin: 16px 0 12px;">Fleets</h3>';
        if (player.fleets && player.fleets.length > 0) {
            html += '<div class="grid grid-2">';
            player.fleets.forEach(fleet => {
                const isSelected = this.app.state.selectedFleet && this.app.state.selectedFleet.id === fleet.id;
                html += `
                    <div class="card" style="${isSelected ? 'border-color: var(--accent-primary); box-shadow: 0 0 0 2px rgba(59,130,246,0.3);' : ''}">
                        <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                            <span>${fleet.name || 'Unnamed Fleet'}</span>
                            <button class="btn btn-sm select-fleet-btn" data-fleet-id="${fleet.id}" title="Select for movement">
                                ${isSelected ? '✓ Selected' : 'Select'}
                            </button>
                        </div>
                        <div class="card-body">
                            <p>Sector: ${fleet.sectorRow !== undefined ? fleet.sectorRow + ',' + fleet.sectorCol : '—'}</p>
                            <p>Ships: ${fleet.ships ? fleet.ships.length : 0}</p>
                            <p>Combat Power: ${fleet.combatPower || 0}</p>
                            ${isSelected ? this._renderMoveControls(fleet) : ''}
                        </div>
                    </div>
                `;
            });
            html += '</div>';
        } else {
            html += '<p style="color: var(--text-muted); margin-bottom: 16px;">No fleets yet</p>';
        }

        // Planets
        html += '<h3 style="margin: 16px 0 12px;">Planets</h3>';
        if (player.planets && player.planets.length > 0) {
            html += '<div class="grid grid-2">';
            player.planets.forEach(planet => {
                html += `
                    <div class="card">
                        <div class="card-header">${planet.name || 'Unnamed Planet'}</div>
                        <div class="card-body">
                            <p>Type: ${planet.type || '—'}</p>
                            <p>Size: ${planet.size || 0} | Population: ${planet.population || 0}</p>
                            <p>Atmosphere: ${planet.atmosphere || 0} | Temperature: ${planet.temperature || 0}</p>
                        </div>
                    </div>
                `;
            });
            html += '</div>';
        } else {
            html += '<p style="color: var(--text-muted);">No planets</p>';
        }

        this.contentEl.innerHTML = html;

        // Bind fleet selection buttons
        this.contentEl.querySelectorAll('.select-fleet-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const fleetId = parseInt(btn.dataset.fleetId);
                const fleet = player.fleets.find(f => f.id === fleetId);
                if (fleet) {
                    this.app.state.selectFleet(fleet);
                    this._render(player);
                }
            });
        });

        // Bind move fleet forms
        this.contentEl.querySelectorAll('.move-fleet-form').forEach(form => {
            form.addEventListener('submit', (e) => {
                e.preventDefault();
                const fleetId = parseInt(form.dataset.fleetId);
                const row = parseInt(form.querySelector('[name="targetRow"]').value);
                const col = parseInt(form.querySelector('[name="targetCol"]').value);
                this._moveFleet(fleetId, row, col);
            });
        });

        // Bind cancel buttons
        this.contentEl.querySelectorAll('.cancel-move-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                this.app.state.clearFleetSelection();
                this._render(player);
            });
        });
    }

    _renderMoveControls(fleet) {
        return `
            <div style="margin-top: 12px; padding-top: 12px; border-top: 1px solid var(--border-color);">
                <form class="move-fleet-form" data-fleet-id="${fleet.id}" style="display: flex; gap: 8px; align-items: flex-end;">
                    <div>
                        <label style="font-size: 0.75rem; color: var(--text-muted); display: block; margin-bottom: 2px;">Row</label>
                        <input type="number" name="targetRow" class="form-control" style="width: 70px; padding: 4px 8px;" min="0" max="9" value="${fleet.sectorRow || 0}" />
                    </div>
                    <div>
                        <label style="font-size: 0.75rem; color: var(--text-muted); display: block; margin-bottom: 2px;">Col</label>
                        <input type="number" name="targetCol" class="form-control" style="width: 70px; padding: 4px 8px;" min="0" max="9" value="${fleet.sectorCol || 0}" />
                    </div>
                    <button type="submit" class="btn btn-primary btn-sm">Move Fleet</button>
                    <button type="button" class="btn btn-sm cancel-move-btn" title="Cancel selection">✕</button>
                </form>
                <div class="move-result" style="margin-top: 8px; font-size: 0.8rem;"></div>
            </div>
        `;
    }

    async _moveFleet(fleetId, targetRow, targetCol) {
        const sessionId = this.app.state.sessionId;
        if (!sessionId) {
            this.app.state.setError('No active session. Please join a session first.');
            return;
        }

        const resultEl = this.contentEl.querySelector(`.move-fleet-form[data-fleet-id="${fleetId}"]`)?.nextElementSibling;
        if (resultEl) {
            resultEl.innerHTML = '<div style="color: var(--text-muted);">Moving fleet...</div>';
        }

        try {
            await this.app.api.moveFleet(sessionId, fleetId, targetRow, targetCol);
            if (resultEl) {
                resultEl.innerHTML = '<div style="color: var(--accent-success);">Fleet moved successfully!</div>';
            }
            // Refresh player data to get updated fleet positions
            await this._load();
        } catch (error) {
            if (resultEl) {
                resultEl.innerHTML = `<div style="color: var(--accent-danger);">Move failed: ${error.message}</div>`;
            }
        }
    }
}

window.InventoryView = InventoryView;

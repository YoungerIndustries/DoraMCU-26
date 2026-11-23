// Map View
// 2D sector grid (10x10) with scan buttons, fleet positions, and fleet movement

class MapView {
    constructor(app) {
        this.app = app;
        this.element = null;
        this.selectedSector = null;
        this.sectorData = {};
        this.fleetPositions = [];

        // Listen for fleet selection changes
        this.app.state.on('fleet:selected', () => {
            this._renderGrid();
            this._updateSectorInfo();
        });

        // Listen for fleet move mode changes
        this.app.state.on('fleetMoveMode:changed', () => {
            this._renderGrid();
            this._updateSectorInfo();
        });

        // Listen for player data load — update fleet positions and sector data
        this.app.state.on('player:updated', () => {
            this._updateFleetPositionsFromState();
            this._updateSectorDataFromState();
            this._renderGrid();
            this._updateSectorInfo();
        });

        // Listen for sector data updates
        this.app.state.on('sectors:updated', () => {
            this.sectorData = this._buildSectorDataMap();
            this._renderGrid();
        });
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'panel';
        this.element.id = 'map-panel';
        this.element.innerHTML = `
            <div class="panel-header">
                <span>Galaxy Map</span>
                <span style="font-size: 0.8rem; color: var(--text-muted);" id="map-hint">Click a sector to select, then scan</span>
            </div>
            <div class="panel-body">
                <div style="display: flex; gap: 24px; align-items: flex-start;">
                    <div>
                        <div class="sector-grid" id="sector-grid"></div>
                        <div style="margin-top: 8px; font-size: 0.75rem; color: var(--text-muted); display: flex; gap: 12px; flex-wrap: wrap;">
                            <span>■ Explored</span>
                            <span>■ Has Systems</span>
                            <span>⚓ Fleet</span>
                            <span>■ Selected</span>
                        </div>
                    </div>
                    <div style="flex: 1; min-width: 200px;">
                        <div class="card" id="sector-info">
                            <div class="card-header">Sector Info</div>
                            <div class="card-body">
                                <p style="color: var(--text-muted);">Select a sector to view details</p>
                            </div>
                        </div>
                        <div style="margin-top: 12px; display: flex; gap: 8px;">
                            <button class="btn btn-primary" id="scan-sector-btn" disabled>Scan Sector</button>
                            <button class="btn btn-sm" id="move-fleet-here-btn" disabled>Move Fleet Here</button>
                        </div>
                        <div id="scan-result" style="margin-top: 12px;"></div>
                    </div>
                </div>
            </div>
        `;

        this.gridEl = this.element.querySelector('#sector-grid');
        this.infoEl = this.element.querySelector('#sector-info');
        this.scanBtn = this.element.querySelector('#scan-sector-btn');
        this.moveFleetBtn = this.element.querySelector('#move-fleet-here-btn');
        this.resultEl = this.element.querySelector('#scan-result');
        this.hintEl = this.element.querySelector('#map-hint');

        this._loadFleetPositions();
        this._renderGrid();
        this.scanBtn.addEventListener('click', () => this._scanSector());
        this.moveFleetBtn.addEventListener('click', () => this._moveFleetHere());

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        this._loadFleetPositions();
        this._renderGrid();
        this._updateMoveFleetBtn();
    }

    toggle() {
        // For keyboard shortcut toggle
    }

    async _loadFleetPositions() {
        try {
            const sessionId = this.app.state.sessionId;
            if (sessionId) {
                const player = await this.app.api.fetchResources(sessionId);
                if (player && player.fleets) {
                    this.fleetPositions = player.fleets.filter(f => f.sectorRow !== undefined && f.sectorCol !== undefined);
                }
            }
        } catch (error) {
            console.warn('[MapView] Failed to load fleet positions:', error);
            this.fleetPositions = [];
        }
    }

    _renderGrid() {
        this.gridEl.innerHTML = '';
        const inMoveMode = this.app.state.fleetMoveMode && this.app.state.selectedFleet;

        for (let row = 0; row < 10; row++) {
            for (let col = 0; col < 10; col++) {
                const cell = document.createElement('div');
                cell.className = 'sector-cell';
                cell.dataset.row = row;
                cell.dataset.col = col;

                // Build cell content
                let content = `${row},${col}`;

                // Check for fleets in this sector
                const fleetsHere = this.fleetPositions.filter(f => f.sectorRow === row && f.sectorCol === col);
                if (fleetsHere.length > 0) {
                    content = '⚓';
                    cell.title = fleetsHere.map(f => f.name || `Fleet ${f.id}`).join(', ');
                }

                cell.textContent = content;

                const data = this.sectorData[`${row}-${col}`];
                if (data) {
                    if (data.explored) cell.classList.add('explored');
                    if (data.hasSystem) cell.classList.add('has-system');
                }

                if (this.selectedSector && this.selectedSector.row === row && this.selectedSector.col === col) {
                    cell.classList.add('selected');
                }

                // Highlight in move mode
                if (inMoveMode) {
                    cell.style.cursor = 'crosshair';
                    cell.style.borderColor = 'var(--accent-warning)';
                }

                cell.addEventListener('click', () => this._selectSector(row, col));
                this.gridEl.appendChild(cell);
            }
        }
    }

    _selectSector(row, col) {
        // If in fleet move mode, move fleet directly
        if (this.app.state.fleetMoveMode && this.app.state.selectedFleet) {
            this._moveFleetToSector(row, col);
            return;
        }

        this.selectedSector = { row, col };
        this.scanBtn.disabled = false;
        this.resultEl.innerHTML = '';

        this._updateSectorInfo();
        this._updateMoveFleetBtn();
        this._renderGrid();
    }

    _updateSectorInfo() {
        if (!this.selectedSector) return;
        const { row, col } = this.selectedSector;
        const data = this.sectorData[`${row}-${col}`];

        let html = `
            <div class="card-header">Sector ${row},${col}</div>
            <div class="card-body">
                <p>Status: ${data ? (data.explored ? 'Explored' : 'Scanned') : 'Unknown'}</p>
                ${data && data.systems ? `<p>Systems: ${data.systems}</p>` : ''}
        `;

        // Show fleets in this sector
        const fleetsHere = this.fleetPositions.filter(f => f.sectorRow === row && f.sectorCol === col);
        if (fleetsHere.length > 0) {
            html += '<p style="margin-top: 8px;">Fleets:</p><ul style="margin-left: 16px; font-size: 0.8rem;">';
            fleetsHere.forEach(f => {
                const isSelected = this.app.state.selectedFleet && this.app.state.selectedFleet.id === f.id;
                html += `<li style="${isSelected ? 'color: var(--accent-primary); font-weight: 600;' : ''}">${f.name || `Fleet ${f.id}`} (${f.ships ? f.ships.length : 0} ships)</li>`;
            });
            html += '</ul>';
        }

        html += '</div>';
        this.infoEl.innerHTML = html;
    }

    _updateMoveFleetBtn() {
        const hasSelectedFleet = !!this.app.state.selectedFleet;
        const hasSelectedSector = !!this.selectedSector;
        this.moveFleetBtn.disabled = !(hasSelectedFleet && hasSelectedSector);

        if (hasSelectedFleet) {
            this.hintEl.textContent = `Selected: ${this.app.state.selectedFleet.name || 'Fleet'}. Click a sector to move.`;
        } else {
            this.hintEl.textContent = 'Select a fleet in Inventory, then click a sector to move it. Or click a sector to scan.';
        }
    }

    async _scanSector() {
        if (!this.selectedSector) return;

        const { row, col } = this.selectedSector;
        const sectorId = `${row},${col}`;
        this.scanBtn.disabled = true;
        this.scanBtn.textContent = 'Scanning...';
        this.resultEl.innerHTML = '<div style="color: var(--text-muted);">Scanning sector...</div>';

        try {
            const systems = await this.app.api.scanSector(sectorId);
            this.sectorData[`${row}-${col}`] = {
                explored: true,
                hasSystem: Array.isArray(systems) && systems.length > 0,
                systems: Array.isArray(systems) ? systems.length : 0,
                systemDetails: systems || []
            };

            const systemsFound = this.sectorData[`${row}-${col}`].systems;
            this.resultEl.innerHTML = `<div style="color: var(--accent-success);">Scan complete! ${systemsFound} system(s) found.</div>`;
            
            // Show system details
            if (systems && systems.length > 0) {
                let details = '<div style="margin-top: 8px;">';
                systems.forEach(sys => {
                    details += `<div class="card" style="margin-bottom: 8px;">
                        <div class="card-header">${sys.name || 'Unknown System'}</div>
                        <div class="card-body">
                            <p>Star: ${sys.starType || 'Unknown'} | Planets: ${sys.planets || 0} | Habitable: ${sys.habitablePlanets || 0}</p>
                            <p>Resources: ${sys.resources || 'Unknown'}</p>
                        </div>
                    </div>`;
                });
                details += '</div>';
                this.resultEl.innerHTML += details;
            }
            
            this._selectSector(row, col);
            this._renderGrid();
        } catch (error) {
            this.resultEl.innerHTML = `<div style="color: var(--accent-danger);">Scan failed: ${error.message}</div>`;
        } finally {
            this.scanBtn.disabled = false;
            this.scanBtn.textContent = 'Scan Sector';
        }
    }

    async _moveFleetHere() {
        if (!this.selectedSector || !this.app.state.selectedFleet) return;
        const { row, col } = this.selectedSector;
        await this._moveFleetToSector(row, col);
    }

    async _moveFleetToSector(row, col) {
        const fleet = this.app.state.selectedFleet;
        if (!fleet) return;

        const sessionId = this.app.state.sessionId;
        if (!sessionId) {
            this.resultEl.innerHTML = '<div style="color: var(--accent-danger);">No active session</div>';
            return;
        }

        this.resultEl.innerHTML = `<div style="color: var(--text-muted);">Moving ${fleet.name || 'fleet'} to ${row},${col}...</div>`;

        try {
            await this.app.api.moveFleet(sessionId, fleet.id, row, col);
            this.resultEl.innerHTML = `<div style="color: var(--accent-success);">${fleet.name || 'Fleet'} moved to ${row},${col}!</div>`;
            
            // Update fleet position locally
            const idx = this.fleetPositions.findIndex(f => f.id === fleet.id);
            if (idx >= 0) {
                this.fleetPositions[idx] = { ...this.fleetPositions[idx], sectorRow: row, sectorCol: col };
            } else {
                this.fleetPositions.push({ ...fleet, sectorRow: row, sectorCol: col });
            }

            this._renderGrid();
            this._updateSectorInfo();

            // Clear move mode
            this.app.state.setFleetMoveMode(false);
        } catch (error) {
            this.resultEl.innerHTML = `<div style="color: var(--accent-danger);">Move failed: ${error.message}</div>`;
        }
    }

    // Update fleet positions from game state after player data loads
    _updateFleetPositionsFromState() {
        const player = this.app.state.player;
        if (player && player.fleets) {
            this.fleetPositions = player.fleets.filter(f => f.sectorRow !== undefined && f.sectorCol !== undefined);
        }
    }

    // Update sector data from game state sectors
    _updateSectorDataFromState() {
        const sectors = this.app.state.sectors;
        if (sectors && sectors.length > 0) {
            this.sectorData = this._buildSectorDataMap();
        }
    }

    // Build a map of sector data from game state sectors
    _buildSectorDataMap() {
        const map = {};
        const sectors = this.app.state.sectors || [];
        for (const sector of sectors) {
            const key = `${sector.row}-${sector.col}`;
            map[key] = {
                explored: sector.explored || false,
                hasSystem: sector.hasSystems || false,
                systems: sector.subSectors ? sector.subSectors.length : 0
            };
        }
        return map;
    }
}

window.MapView = MapView;

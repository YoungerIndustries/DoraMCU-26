// Ship Build View
// Ship building panel: 5 types, 6 modules, resource costs

class ShipBuildView {
    constructor(app) {
        this.app = app;
        this.element = null;

        this.shipTypes = ['Fighter', 'Corvette', 'Frigate', 'Cruiser', 'Dreadnought'];
        this.modules = ['Engine', 'Weapons', 'Shields', 'Armor', 'Cargo', 'Sensors'];
        this.selectedType = null;
        this.selectedModules = [];
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'panel';
        this.element.id = 'ship-build-panel';
        this.element.innerHTML = `
            <div class="panel-header">
                <span>Ship Builder</span>
                <span style="font-size: 0.8rem; color: var(--text-muted);">Press V to toggle</span>
            </div>
            <div class="panel-body">
                <div class="grid grid-2">
                    <div>
                        <h3 style="margin-bottom: 12px;">Ship Type</h3>
                        <div id="ship-type-grid" class="grid grid-1"></div>
                    </div>
                    <div>
                        <h3 style="margin-bottom: 12px;">Modules</h3>
                        <div id="module-grid" class="grid grid-2"></div>
                    </div>
                </div>
                <div style="margin-top: 24px; display: flex; justify-content: space-between; align-items: center;">
                    <div id="build-cost" style="color: var(--text-secondary);">
                        Select a ship type and modules to see cost
                    </div>
                    <div style="display: flex; gap: 8px;">
                        <button class="btn" id="clear-modules-btn">Clear Modules</button>
                        <button class="btn btn-primary" id="build-ship-btn" disabled>Build Ship</button>
                    </div>
                </div>
                <div id="build-result" style="margin-top: 12px;"></div>
            </div>
        `;

        this.typeGrid = this.element.querySelector('#ship-type-grid');
        this.moduleGrid = this.element.querySelector('#module-grid');
        this.costEl = this.element.querySelector('#build-cost');
        this.buildBtn = this.element.querySelector('#build-ship-btn');
        this.resultEl = this.element.querySelector('#build-result');

        this._renderTypes();
        this._renderModules();

        this.element.querySelector('#clear-modules-btn').addEventListener('click', () => {
            this.selectedModules = [];
            this._renderModules();
            this._updateCost();
        });
        this.buildBtn.addEventListener('click', () => this._buildShip());

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        this._renderTypes();
        this._renderModules();
    }

    _renderTypes() {
        this.typeGrid.innerHTML = '';
        this.shipTypes.forEach(type => {
            const card = document.createElement('div');
            card.style.cssText = `padding: 12px; border: 1px solid var(--border-color); border-radius: var(--border-radius); cursor: pointer; transition: all 150ms ease; ${this.selectedType === type ? 'border-color: var(--accent-primary); background: var(--bg-hover);' : ''}`;
            card.textContent = type;
            card.addEventListener('click', () => {
                this.selectedType = type;
                this._renderTypes();
                this._updateCost();
            });
            this.typeGrid.appendChild(card);
        });
    }

    _renderModules() {
        this.moduleGrid.innerHTML = '';
        this.modules.forEach(module => {
            const selected = this.selectedModules.includes(module);
            const card = document.createElement('div');
            card.style.cssText = `padding: 12px; border: 1px solid var(--border-color); border-radius: var(--border-radius); cursor: pointer; text-align: center; transition: all 150ms ease; ${selected ? 'border-color: var(--accent-primary); background: var(--bg-hover);' : ''}`;
            card.textContent = module;
            card.addEventListener('click', () => {
                if (selected) {
                    this.selectedModules = this.selectedModules.filter(m => m !== module);
                } else {
                    this.selectedModules.push(module);
                }
                this._renderModules();
                this._updateCost();
            });
            this.moduleGrid.appendChild(card);
        });
    }

    _updateCost() {
        if (!this.selectedType) {
            this.costEl.textContent = 'Select a ship type and modules to see cost';
            this.buildBtn.disabled = true;
            return;
        }
        const credits = this.app.state.getCredits();
        const cost = this._calculateCost();
        this.costEl.textContent = `Cost: ${cost} credits (You have: ${credits.toLocaleString()})`;
        this.buildBtn.disabled = credits < cost;
    }

    _calculateCost() {
        const typeCosts = { Fighter: 100, Corvette: 250, Frigate: 500, Cruiser: 1000, Dreadnought: 2500 };
        const moduleCost = 50;
        return (typeCosts[this.selectedType] || 100) + (this.selectedModules.length * moduleCost);
    }

    async _buildShip() {
        if (!this.selectedType) return;

        const sessionId = this.app.state.sessionId;
        if (!sessionId) {
            this.resultEl.innerHTML = '<div style="color: var(--accent-danger);">No active session. Please join a session first.</div>';
            return;
        }

        this.buildBtn.disabled = true;
        this.buildBtn.textContent = 'Building...';
        this.resultEl.innerHTML = '<div style="color: var(--text-muted);">Building ship...</div>';

        try {
            await this.app.api.buildShip(sessionId, this.selectedType, this.selectedModules);
            this.resultEl.innerHTML = '<div style="color: var(--accent-success);">Ship built successfully!</div>';
            this.selectedModules = [];
            this._renderModules();
            this._updateCost();

            // Refresh player data
            await this.app._loadPlayerData();
        } catch (error) {
            this.resultEl.innerHTML = `<div style="color: var(--accent-danger);">Build failed: ${error.message}</div>`;
        } finally {
            this.buildBtn.disabled = false;
            this.buildBtn.textContent = 'Build Ship';
        }
    }
}

window.ShipBuildView = ShipBuildView;

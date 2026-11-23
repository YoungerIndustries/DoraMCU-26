// Character Creation View
// Multi-step wizard: name/age → species → career → review

class CharacterView {
    constructor(app) {
        this.app = app;
        this.element = null;
        this.currentStep = 1;
        this.data = {
            name: '',
            age: 25,
            species: 'Human',
            career: 'Soldier'
        };

        // Species list (10 alien races from Traveller)
        this.species = [
            'Human', 'Aslan', 'Dzvene', 'Grant', 'Haruhi',
            'Linzie', 'Mongeth', 'Nden', 'Vargr', 'Yehar'
        ];

        // Career list (8 careers)
        this.careers = [
            'Soldier', 'Explorer', 'Pirate', 'Merchant',
            'Guard', 'Navy', 'Outworld', 'Refugee'
        ];
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'view-panel';
        this.element.id = 'character-view';
        this.element.innerHTML = `
            <div class="character-wizard">
                <h1 style="text-align: center; margin-bottom: 24px; color: var(--accent-primary);">Create Character</h1>

                <div class="wizard-steps">
                    <div class="wizard-step active" data-step="1">
                        <span class="step-number">1</span>
                        <span>Identity</span>
                    </div>
                    <div class="wizard-step" data-step="2">
                        <span class="step-number">2</span>
                        <span>Species</span>
                    </div>
                    <div class="wizard-step" data-step="3">
                        <span class="step-number">3</span>
                        <span>Career</span>
                    </div>
                    <div class="wizard-step" data-step="4">
                        <span class="step-number">4</span>
                        <span>Review</span>
                    </div>
                </div>

                <div id="wizard-content"></div>

                <div style="display: flex; justify-content: space-between; margin-top: 24px;">
                    <button class="btn" id="wizard-prev" style="display: none;">← Back</button>
                    <div></div>
                    <button class="btn btn-primary" id="wizard-next">Next →</button>
                </div>
            </div>
        `;

        this.contentEl = this.element.querySelector('#wizard-content');
        this.prevBtn = this.element.querySelector('#wizard-prev');
        this.nextBtn = this.element.querySelector('#wizard-next');

        this.prevBtn.addEventListener('click', () => this._prevStep());
        this.nextBtn.addEventListener('click', () => this._nextStep());

        this._renderStep();

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        this.currentStep = 1;
        this._renderStep();
        this._updateButtons();
    }

    _renderStep() {
        switch (this.currentStep) {
            case 1: this._renderIdentity(); break;
            case 2: this._renderSpecies(); break;
            case 3: this._renderCareer(); break;
            case 4: this._renderReview(); break;
        }
        this._updateSteps();
        this._updateButtons();
    }

    _renderIdentity() {
        this.contentEl.innerHTML = `
            <div class="card">
                <div class="card-header">Character Identity</div>
                <div class="card-body">
                    <div class="form-group">
                        <label for="char-name">Character Name</label>
                        <input type="text" id="char-name" class="form-control" placeholder="Enter character name" value="${this.data.name}">
                    </div>
                    <div class="form-group">
                        <label for="char-age">Age</label>
                        <input type="number" id="char-age" class="form-control" min="18" max="120" value="${this.data.age}">
                    </div>
                </div>
            </div>
        `;

        this.contentEl.querySelector('#char-name').addEventListener('input', (e) => {
            this.data.name = e.target.value;
        });
        this.contentEl.querySelector('#char-age').addEventListener('input', (e) => {
            this.data.age = parseInt(e.target.value) || 25;
        });
    }

    _renderSpecies() {
        let html = '<div class="card"><div class="card-header">Select Species</div><div class="card-body"><div class="grid grid-2">';
        this.species.forEach(species => {
            const selected = this.data.species === species ? 'border-color: var(--accent-primary); background: var(--bg-hover);' : '';
            html += `
                <div class="species-card" style="${selected} padding: 16px; border: 1px solid var(--border-color); border-radius: var(--border-radius); cursor: pointer; transition: all 150ms ease;" data-species="${species}">
                    <strong>${species}</strong>
                    <div style="font-size: 0.8rem; color: var(--text-muted); margin-top: 4px;">Click to select</div>
                </div>
            `;
        });
        html += '</div></div></div>';
        this.contentEl.innerHTML = html;

        this.contentEl.querySelectorAll('.species-card').forEach(card => {
            card.addEventListener('click', () => {
                this.data.species = card.dataset.species;
                this._renderStep();
            });
        });
    }

    _renderCareer() {
        this.contentEl.innerHTML = `
            <div class="card">
                <div class="card-header">Select Career</div>
                <div class="card-body">
                    <p style="margin-bottom: 16px; color: var(--text-secondary); font-size: 0.875rem;">
                        Choose your character's career. You'll roll 2d6 for skill checks.
                    </p>
                    <div class="grid grid-2" id="career-grid"></div>
                    <div style="margin-top: 16px; text-align: center;">
                        <button class="btn" id="roll-dice-btn">🎲 Roll 2d6</button>
                        <div id="dice-result" style="margin-top: 8px; font-size: 1.5rem; font-weight: bold; color: var(--accent-primary);"></div>
                    </div>
                </div>
            </div>
        `;

        const grid = this.contentEl.querySelector('#career-grid');
        this.careers.forEach(career => {
            const selected = this.data.career === career ? 'border-color: var(--accent-primary); background: var(--bg-hover);' : '';
            grid.innerHTML += `
                <div class="career-card" style="${selected} padding: 16px; border: 1px solid var(--border-color); border-radius: var(--border-radius); cursor: pointer; text-align: center;" data-career="${career}">
                    <strong>${career}</strong>
                </div>
            `;
        });

        this.contentEl.querySelectorAll('.career-card').forEach(card => {
            card.addEventListener('click', () => {
                this.data.career = card.dataset.career;
                this._renderStep();
            });
        });

        this.contentEl.querySelector('#roll-dice-btn').addEventListener('click', () => {
            const d1 = Math.floor(Math.random() * 6) + 1;
            const d2 = Math.floor(Math.random() * 6) + 1;
            const total = d1 + d2;
            this.contentEl.querySelector('#dice-result').textContent = `${d1} + ${d2} = ${total}`;
        });
    }

    _renderReview() {
        this.contentEl.innerHTML = `
            <div class="card">
                <div class="card-header">Review Character</div>
                <div class="card-body">
                    <table style="width: 100%;">
                        <tr><td style="padding: 8px 0; color: var(--text-muted);">Name</td><td style="padding: 8px 0; font-weight: 600;">${this.data.name || '—'}</td></tr>
                        <tr><td style="padding: 8px 0; color: var(--text-muted);">Age</td><td style="padding: 8px 0;">${this.data.age}</td></tr>
                        <tr><td style="padding: 8px 0; color: var(--text-muted);">Species</td><td style="padding: 8px 0;">${this.data.species}</td></tr>
                        <tr><td style="padding: 8px 0; color: var(--text-muted);">Career</td><td style="padding: 8px 0;">${this.data.career}</td></tr>
                    </table>
                    <div style="margin-top: 24px; text-align: center;">
                        <button class="btn btn-success btn-lg" id="create-character-btn">Create Character</button>
                    </div>
                </div>
            </div>
        `;

        this.contentEl.querySelector('#create-character-btn').addEventListener('click', () => this._createCharacter());
    }

    _updateSteps() {
        this.element.querySelectorAll('.wizard-step').forEach(step => {
            const stepNum = parseInt(step.dataset.step);
            step.classList.remove('active', 'completed');
            if (stepNum === this.currentStep) {
                step.classList.add('active');
            } else if (stepNum < this.currentStep) {
                step.classList.add('completed');
            }
        });
    }

    _updateButtons() {
        this.prevBtn.style.display = this.currentStep > 1 ? 'inline-flex' : 'none';
        this.nextBtn.textContent = this.currentStep === 4 ? 'Finish' : 'Next →';
        this.nextBtn.style.display = this.currentStep === 4 ? 'none' : 'inline-flex';
    }

    _prevStep() {
        if (this.currentStep > 1) {
            this.currentStep--;
            this._renderStep();
        }
    }

    _nextStep() {
        if (this.currentStep === 1) {
            if (!this.data.name.trim()) {
                alert('Please enter a character name');
                return;
            }
        }

        if (this.currentStep < 4) {
            this.currentStep++;
            this._renderStep();
        }
    }

    async _createCharacter() {
        if (!this.data.name.trim()) {
            alert('Please enter a character name');
            this.currentStep = 1;
            this._renderStep();
            return;
        }

        this.app.state.setLoading(true, 'Creating character...');
        try {
            const occupation = this.data.career;
            await this.app.api.createCharacter({
                username: this.app.state.username,
                characterName: this.data.name,
                age: this.data.age,
                occupation: occupation
            });

            // Character created — go to Earth 2026 corporation HQ landing page
            this.app.state.selectGalaxy({ name: 'Earth 2026' });
            this.app.switchView('earth');
        } catch (error) {
            this.app.state.setError(`Character creation failed: ${error.message}`);
        } finally {
            this.app.state.setLoading(false);
        }
    }
}

window.CharacterView = CharacterView;

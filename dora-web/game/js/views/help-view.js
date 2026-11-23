// Help View
// F1 overlay with keyboard shortcuts and controls

class HelpView {
    constructor(app) {
        this.app = app;
        this.element = null;
        this.visible = false;
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'help-overlay';
        this.element.id = 'help-overlay';
        this.element.innerHTML = `
            <div class="help-content">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <h2>Controls & Shortcuts</h2>
                    <button class="btn btn-sm" id="close-help-btn">✕</button>
                </div>
                <table>
                    <thead>
                        <tr><th>Key</th><th>Action</th></tr>
                    </thead>
                    <tbody>
                        <tr><td><kbd>F1</kbd></td><td>Toggle this help screen</td></tr>
                        <tr><td><kbd>Esc</kbd></td><td>Close overlays</td></tr>
                        <tr><td><kbd>Tab</kbd></td><td>Toggle galaxy map</td></tr>
                        <tr><td><kbd>M</kbd></td><td>Show map panel</td></tr>
                        <tr><td><kbd>V</kbd></td><td>Show ship builder</td></tr>
                        <tr><td><kbd>I</kbd></td><td>Show inventory</td></tr>
                    </tbody>
                </table>
                <h3 style="margin-top: 24px; margin-bottom: 12px;">Game Guide</h3>
                <div style="color: var(--text-secondary); font-size: 0.875rem; line-height: 1.6;">
                    <p><strong>Map:</strong> Click a sector on the 10×10 grid, then press Scan to reveal solar systems.</p>
                    <p><strong>Ship Builder:</strong> Select a ship type and add modules, then build. Ships cost credits.</p>
                    <p><strong>Inventory:</strong> View your ships, fleets, and planets.</p>
                    <p><strong>Menu:</strong> Save/load game, return to galaxy selection, or logout.</p>
                </div>
                <div style="margin-top: 24px; text-align: center; color: var(--text-muted); font-size: 0.75rem;">
                    DoraMCU Web Client v1.0
                </div>
            </div>
        `;

        this.element.querySelector('#close-help-btn').addEventListener('click', () => this.hide());
        this.element.addEventListener('click', (e) => {
            if (e.target === this.element) this.hide();
        });

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        this.element.classList.add('visible');
        this.visible = true;
    }

    hide() {
        this.element.classList.remove('visible');
        this.visible = false;
    }

    toggle() {
        if (this.visible) {
            this.hide();
        } else {
            this.show();
        }
    }

    isVisible() {
        return this.visible;
    }
}

window.HelpView = HelpView;

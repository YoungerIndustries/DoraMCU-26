// Login View
// Login/Register screen with dev login, error display, and loading states

class LoginView {
    constructor(app) {
        this.app = app;
        this.element = null;
        this.errorEl = null;
        this.usernameInput = null;
        this.passwordInput = null;
    }

    render() {
        this.element = document.createElement('div');
        this.element.className = 'view-panel';
        this.element.id = 'login-view';
        this.element.innerHTML = `
            <div class="login-container">
                <div class="login-box">
                    <h1>DoraMCU</h1>
                    <div class="login-error" id="login-error"></div>
                    <div class="form-group">
                        <label for="login-username">Username</label>
                        <input type="text" id="login-username" class="form-control" placeholder="Enter username" autocomplete="username">
                    </div>
                    <div class="form-group">
                        <label for="login-password">Password</label>
                        <input type="password" id="login-password" class="form-control" placeholder="Enter password" autocomplete="current-password">
                    </div>
                    <button class="btn btn-primary btn-lg" id="login-btn">Login</button>
                    <button class="btn btn-lg" id="register-btn">Register</button>
                    <button class="btn btn-lg" id="dev-login-btn">Dev Login</button>
                    <div style="margin-top: 16px; text-align: center;">
                        <a href="../" style="color: var(--text-muted); font-size: 0.875rem;">← Back to Client Portal</a>
                    </div>
                </div>
            </div>
        `;

        // Cache elements
        this.errorEl = this.element.querySelector('#login-error');
        this.usernameInput = this.element.querySelector('#login-username');
        this.passwordInput = this.element.querySelector('#login-password');

        // Bind events
        this.element.querySelector('#login-btn').addEventListener('click', () => this._login());
        this.element.querySelector('#register-btn').addEventListener('click', () => this._register());
        this.element.querySelector('#dev-login-btn').addEventListener('click', () => this._devLogin());

        // Enter key submits
        this.passwordInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') this._login();
        });
        this.usernameInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') this.passwordInput.focus();
        });

        return this.element;
    }

    getElement() {
        return this.element;
    }

    show() {
        this.clearError();
        this.usernameInput.value = '';
        this.passwordInput.value = '';
        this.usernameInput.focus();
    }

    async _login() {
        const username = this.usernameInput.value.trim();
        const password = this.passwordInput.value;

        if (!username || !password) {
            this.showError('Please enter username and password');
            return;
        }

        try {
            await this.app.login(username, password);
        } catch (error) {
            this.showError(error.message || 'Login failed');
        }
    }

    async _register() {
        const username = this.usernameInput.value.trim();
        const password = this.passwordInput.value;

        if (!username || !password) {
            this.showError('Please enter username and password');
            return;
        }

        if (password.length < 4) {
            this.showError('Password must be at least 4 characters');
            return;
        }

        try {
            await this.app.register(username, password);
        } catch (error) {
            this.showError(error.message || 'Registration failed');
        }
    }

    async _devLogin() {
        try {
            await this.app.devLogin();
        } catch (error) {
            this.showError(error.message || 'Dev login failed');
        }
    }

    showError(message) {
        this.errorEl.textContent = message;
        this.errorEl.style.display = 'block';
    }

    clearError() {
        this.errorEl.textContent = '';
        this.errorEl.style.display = 'none';
    }
}

window.LoginView = LoginView;

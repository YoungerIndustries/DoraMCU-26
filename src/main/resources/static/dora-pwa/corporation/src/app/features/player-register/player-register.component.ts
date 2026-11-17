import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

/**
 * Player registration component — the landing page for new players.
 *
 * Provides a registration form with platform detection that registers the player
 * against the neuron's player auth endpoint and triggers client distribution.
 */
@Component({
  selector: 'app-player-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="register-container">
      <div class="register-card">
        <h1>Join DoraMCU</h1>
        <p class="subtitle">Create your player account</p>

        <form (ngSubmit)="onRegister()" class="register-form">
          <div class="form-group">
            <label for="email">Email</label>
            <input
              type="email"
              id="email"
              [(ngModel)]="registerData.email"
              name="email"
              required
              placeholder="Enter your email"
            />
          </div>

          <div class="form-group">
            <label for="username">Username</label>
            <input
              type="text"
              id="username"
              [(ngModel)]="registerData.username"
              name="username"
              required
              placeholder="Choose a username"
            />
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <input
              type="password"
              id="password"
              [(ngModel)]="registerData.password"
              name="password"
              required
              placeholder="Create a password"
            />
          </div>

          <div class="platform-info" *ngIf="platform">
            <p>Detected platform: <strong>{{ platform }}</strong> ({{ os }})</p>
          </div>

          <button type="submit" class="btn-primary" [disabled]="loading">
            {{ loading ? 'Creating account...' : 'Create Account' }}
          </button>

          <p class="error" *ngIf="error">{{ error }}</p>
        </form>

        <p class="login-link">
          Already have an account?
          <a routerLink="/">Log in</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .register-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: #0a0a1a;
    }

    .register-card {
      background: #1a1a2e;
      padding: 40px;
      border-radius: 12px;
      width: 100%;
      max-width: 400px;
      text-align: center;
    }

    h1 {
      color: #00d4ff;
      font-size: 2.5em;
      margin: 0 0 10px;
    }

    .subtitle {
      color: #a0a0b0;
      margin: 0 0 30px;
    }

    .register-form {
      text-align: left;
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      color: #a0a0b0;
      margin-bottom: 5px;
      font-size: 0.9em;
    }

    input {
      width: 100%;
      padding: 12px;
      background: #0a0a1a;
      border: 1px solid #333;
      border-radius: 6px;
      color: #e0e0e0;
      font-size: 1em;
      box-sizing: border-box;
    }

    input:focus {
      outline: none;
      border-color: #00d4ff;
    }

    .platform-info {
      background: #0a0a1a;
      padding: 10px;
      border-radius: 6px;
      margin-bottom: 20px;
      color: #a0a0b0;
      font-size: 0.9em;
    }

    .platform-info strong {
      color: #00d4ff;
    }

    .btn-primary {
      width: 100%;
      padding: 12px;
      background: #00d4ff;
      color: #0a0a1a;
      border: none;
      border-radius: 6px;
      font-size: 1.1em;
      font-weight: bold;
      cursor: pointer;
      margin-top: 10px;
    }

    .btn-primary:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .error {
      color: #ff4444;
      margin-top: 10px;
      font-size: 0.9em;
    }

    .login-link {
      color: #a0a0b0;
      margin-top: 20px;
    }

    .login-link a {
      color: #00d4ff;
      text-decoration: none;
    }
  `]
})
export class PlayerRegisterComponent {
  registerData = {
    email: '',
    username: '',
    password: '',
    platform: 'desktop',
    os: 'windows'
  };

  platform = '';
  os = '';
  loading = false;
  error = '';

  private apiBaseUrl = '/api/v1/player';

  constructor(private router: Router) {
    this.detectPlatform();
  }

  detectPlatform() {
    const ua = navigator.userAgent;

    // Detect OS
    if (/android/i.test(ua)) {
      this.os = 'android';
      this.registerData.os = 'android';
    } else if (/iPad|iPhone|iPod/.test(ua)) {
      this.os = 'ios';
      this.registerData.os = 'ios';
    } else if (/mac/i.test(ua)) {
      this.os = 'macos';
      this.registerData.os = 'macos';
    } else if (/linux/i.test(ua)) {
      this.os = 'linux';
      this.registerData.os = 'linux';
    } else {
      this.os = 'windows';
      this.registerData.os = 'windows';
    }

    // Detect platform
    const width = window.innerWidth;
    if (/Mobile|Android/i.test(ua) && width < 768) {
      this.platform = 'mobile';
      this.registerData.platform = 'mobile';
    } else if (/Tablet|iPad/i.test(ua) || (width >= 768 && width < 1024)) {
      this.platform = 'tablet';
      this.registerData.platform = 'tablet';
    } else {
      this.platform = 'desktop';
      this.registerData.platform = 'desktop';
    }
  }

  async onRegister() {
    this.loading = true;
    this.error = '';

    try {
      const response = await fetch(`${this.apiBaseUrl}/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(this.registerData)
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || 'Registration failed');
      }

      // Store tokens
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('playerId', data.playerId);

      // Fetch client distribution data
      await this.fetchClientDistribution();

      // Redirect to dashboard
      this.router.navigate(['/dashboard']);
    } catch (err: any) {
      this.error = err.message || 'Registration failed. Please try again.';
    } finally {
      this.loading = false;
    }
  }

  async fetchClientDistribution() {
    try {
      const response = await fetch(
        `${this.apiBaseUrl}/platform?platform=${this.registerData.platform}&os=${this.registerData.os}`
      );
      if (response.ok) {
        const distribution = await response.json();
        localStorage.setItem('clientDistribution', JSON.stringify(distribution));
        console.log('[PlayerRegister] Client distribution:', distribution);
      }
    } catch (error) {
      console.error('[PlayerRegister] Failed to fetch client distribution:', error);
    }
  }
}

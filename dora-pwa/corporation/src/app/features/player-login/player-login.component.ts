import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

/**
 * Player login component — the landing page for returning players.
 *
 * Provides a login form that authenticates against the neuron's player auth endpoint
 * and redirects to the game dashboard on success.
 */
@Component({
  selector: 'app-player-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="login-container">
      <div class="login-card">
        <h1>DoraMCU</h1>
        <p class="subtitle">Player Game Node</p>

        <form (ngSubmit)="onLogin()" class="login-form">
          <div class="form-group">
            <label for="email">Email</label>
            <input
              type="email"
              id="email"
              [(ngModel)]="loginData.email"
              name="email"
              required
              placeholder="Enter your email"
            />
          </div>

          <div class="form-group">
            <label for="password">Password</label>
            <input
              type="password"
              id="password"
              [(ngModel)]="loginData.password"
              name="password"
              required
              placeholder="Enter your password"
            />
          </div>

          <button type="submit" class="btn-primary" [disabled]="loading">
            {{ loading ? 'Logging in...' : 'Log In' }}
          </button>

          <p class="error" *ngIf="error">{{ error }}</p>
        </form>

        <p class="register-link">
          Don't have an account?
          <a routerLink="/register">Create one</a>
        </p>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: #0a0a1a;
    }

    .login-card {
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

    .login-form {
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

    .register-link {
      color: #a0a0b0;
      margin-top: 20px;
    }

    .register-link a {
      color: #00d4ff;
      text-decoration: none;
    }
  `]
})
export class PlayerLoginComponent {
  loginData = {
    email: '',
    password: ''
  };

  loading = false;
  error = '';

  private apiBaseUrl = '/api/v1/player';

  constructor(private router: Router) {}

  async onLogin() {
    this.loading = true;
    this.error = '';

    try {
      const response = await fetch(`${this.apiBaseUrl}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(this.loginData)
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || 'Login failed');
      }

      // Store tokens
      localStorage.setItem('accessToken', data.accessToken);
      localStorage.setItem('refreshToken', data.refreshToken);
      localStorage.setItem('playerId', data.playerId);

      // Redirect to dashboard
      this.router.navigate(['/dashboard']);
    } catch (err: any) {
      this.error = err.message || 'Login failed. Please try again.';
    } finally {
      this.loading = false;
    }
  }
}

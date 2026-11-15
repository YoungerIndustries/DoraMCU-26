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
      <div class="portal-frame">
        <div class="portal-arch"></div>
        <div class="login-card">

          <div class="warning-sign">
            <div class="sign-nails">
              <span class="nail top-left"></span>
              <span class="nail top-right"></span>
              <span class="nail bottom-left"></span>
              <span class="nail bottom-right"></span>
            </div>
            <div class="sign-header">⚠ WARNING ⚠</div>
            <p class="sign-text">
              YOU STAND AT THE ENTRANCE<br>
              TO ANOTHER UNIVERSE.<br><br>
              ONCE YOU WALK THRU THIS DOOR<br>
              YOU WILL BECOME IMMORTAL.<br><br>
              STEP THRU THIS DOOR AND THERE<br>
              BE NOT JUST DRAGONS BUT<br>
              FAR FAR WORSE.<br><br>
              <strong>You have been warned</strong>
            </p>
          </div>

          <h1 class="title">⚔ DoraMCU ⚔</h1>
          <p class="subtitle">Enter the Realm, Adventurer</p>

          <form (ngSubmit)="onLogin()" class="login-form">
            <div class="form-group">
              <label for="email">📜 Adventurer's Name</label>
              <input
                type="email"
                id="email"
                [(ngModel)]="loginData.email"
                name="email"
                required
                placeholder="Enter your email"
                class="parchment-input"
              />
            </div>

            <div class="form-group">
              <label for="password">🔮 Secret Incantation</label>
              <input
                type="password"
                id="password"
                [(ngModel)]="loginData.password"
                name="password"
                required
                placeholder="Enter your password"
                class="parchment-input"
              />
            </div>

            <button type="submit" class="btn-portal" [disabled]="loading">
              {{ loading ? '🌀 Entering the Portal...' : '🚪 Step Through the Portal' }}
            </button>

            <p class="error" *ngIf="error">⚠ {{ error }}</p>
          </form>

          <p class="register-link">
            New to this realm?
            <a routerLink="/register">Begin your quest</a>
          </p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: #0a0a0a;
      background-image:
        radial-gradient(ellipse at 20% 50%, rgba(120, 0, 0, 0.15) 0%, transparent 50%),
        radial-gradient(ellipse at 80% 50%, rgba(120, 0, 0, 0.15) 0%, transparent 50%),
        radial-gradient(ellipse at 50% 100%, rgba(80, 0, 120, 0.2) 0%, transparent 50%);
      font-family: 'Georgia', 'Times New Roman', serif;
      overflow: hidden;
    }

    .portal-frame {
      position: relative;
      display: flex;
      flex-direction: column;
      align-items: center;
    }

    .portal-arch {
      position: absolute;
      top: -20px;
      left: -20px;
      right: -20px;
      bottom: -20px;
      border: 3px solid #8b0000;
      border-radius: 50% 50% 10% 10%;
      box-shadow:
        0 0 30px rgba(139, 0, 0, 0.5),
        inset 0 0 30px rgba(139, 0, 0, 0.3),
        0 0 60px rgba(128, 0, 128, 0.3);
      animation: portalPulse 3s ease-in-out infinite;
    }

    @keyframes portalPulse {
      0%, 100% { box-shadow: 0 0 30px rgba(139, 0, 0, 0.5), inset 0 0 30px rgba(139, 0, 0, 0.3); }
      50% { box-shadow: 0 0 50px rgba(139, 0, 0, 0.8), inset 0 0 50px rgba(139, 0, 0, 0.5); }
    }

    .login-card {
      position: relative;
      background: linear-gradient(135deg, #1a0a0a 0%, #2a1515 50%, #1a0a2a 100%);
      padding: 40px;
      border-radius: 10px;
      width: 100%;
      max-width: 500px;
      text-align: center;
      border: 2px solid #8b4513;
      box-shadow:
        0 0 20px rgba(139, 69, 19, 0.5),
        inset 0 0 20px rgba(0, 0, 0, 0.5);
    }

    .warning-sign {
      background: linear-gradient(180deg, #d2b48c 0%, #c4a882 50%, #b8956a 100%);
      padding: 20px 25px;
      margin: -20px -20px 25px -20px;
      border-radius: 5px 5px 0 0;
      border: 3px solid #8b4513;
      border-bottom: none;
      position: relative;
      box-shadow:
        inset 0 0 20px rgba(139, 69, 19, 0.3),
        0 -5px 15px rgba(0, 0, 0, 0.3);
      transform: rotate(-1deg);
    }

    .sign-nails {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
    }

    .nail {
      position: absolute;
      width: 8px;
      height: 8px;
      background: radial-gradient(circle, #666 30%, #333 70%);
      border-radius: 50%;
      box-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
    }

    .nail.top-left { top: 5px; left: 5px; }
    .nail.top-right { top: 5px; right: 5px; }
    .nail.bottom-left { bottom: 5px; left: 5px; }
    .nail.bottom-right { bottom: 5px; right: 5px; }

    .sign-header {
      color: #8b0000;
      font-size: 1.4em;
      font-weight: bold;
      text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.3);
      margin-bottom: 10px;
      letter-spacing: 3px;
    }

    .sign-text {
      color: #3a1a00;
      font-size: 0.85em;
      line-height: 1.4;
      margin: 0;
      font-weight: bold;
      text-shadow: 0 0 1px rgba(0, 0, 0, 0.2);
    }

    .title {
      color: #ff4500;
      font-size: 2.5em;
      margin: 0 0 10px;
      text-shadow:
        0 0 10px rgba(255, 69, 0, 0.8),
        0 0 20px rgba(255, 69, 0, 0.5),
        2px 2px 4px rgba(0, 0, 0, 0.8);
      letter-spacing: 3px;
    }

    .subtitle {
      color: #cd853f;
      margin: 0 0 25px;
      font-style: italic;
      font-size: 1.1em;
      text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
    }

    .login-form {
      text-align: left;
    }

    .form-group {
      margin-bottom: 20px;
    }

    label {
      display: block;
      color: #cd853f;
      margin-bottom: 5px;
      font-size: 0.95em;
      font-weight: bold;
      text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
    }

    .parchment-input {
      width: 100%;
      padding: 12px;
      background: linear-gradient(180deg, #2a1a0a 0%, #1a0f05 100%);
      border: 2px solid #8b4513;
      border-radius: 4px;
      color: #d2b48c;
      font-size: 1em;
      font-family: 'Georgia', 'Times New Roman', serif;
      box-sizing: border-box;
      box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.5);
    }

    .parchment-input:focus {
      outline: none;
      border-color: #ff4500;
      box-shadow:
        inset 0 0 10px rgba(0, 0, 0, 0.5),
        0 0 10px rgba(255, 69, 0, 0.5);
    }

    .parchment-input::placeholder {
      color: #8b7355;
      font-style: italic;
    }

    .btn-portal {
      width: 100%;
      padding: 14px;
      background: linear-gradient(180deg, #8b0000 0%, #5a0000 100%);
      color: #ffd700;
      border: 2px solid #ffd700;
      border-radius: 4px;
      font-size: 1.1em;
      font-weight: bold;
      font-family: 'Georgia', 'Times New Roman', serif;
      cursor: pointer;
      margin-top: 10px;
      text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.8);
      box-shadow:
        0 0 15px rgba(139, 0, 0, 0.5),
        inset 0 0 10px rgba(0, 0, 0, 0.3);
      transition: all 0.3s ease;
      letter-spacing: 1px;
    }

    .btn-portal:hover:not(:disabled) {
      background: linear-gradient(180deg, #a00000 0%, #700000 100%);
      box-shadow:
        0 0 25px rgba(139, 0, 0, 0.8),
        0 0 15px rgba(255, 69, 0, 0.5),
        inset 0 0 10px rgba(0, 0, 0, 0.3);
      transform: scale(1.02);
    }

    .btn-portal:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .error {
      color: #ff4444;
      margin-top: 10px;
      font-size: 0.9em;
      text-shadow: 0 0 5px rgba(255, 0, 0, 0.3);
    }

    .register-link {
      color: #8b7355;
      margin-top: 20px;
      font-style: italic;
    }

    .register-link a {
      color: #ffd700;
      text-decoration: none;
      text-shadow: 0 0 5px rgba(255, 215, 0, 0.3);
    }

    .register-link a:hover {
      text-shadow: 0 0 10px rgba(255, 215, 0, 0.6);
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

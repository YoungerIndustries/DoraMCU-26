import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { CorporationService } from '../../core/services/corporation.service';
import { RoleState } from '../../core/models/corporation-state';

@Component({
  selector: 'app-role-switch',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
  ],
  template: `
    <div class="role-page">
      <h1>Role Management</h1>

      @if (role) {
        <mat-card class="role-card">
          <div class="role-header">
            <mat-icon>{{ role.currentRole === 'CEO' ? 'business_center' : 'person' }}</mat-icon>
            <div>
              <h2>{{ role.roleDisplayName }}</h2>
              <p class="role-subtitle">
                @if (role.currentRole === 'CEO') {
                  Full corporation control. Manage contracts, hire/fire crew, and make strategic decisions.
                } @else {
                  AI assistant manages your corporation. You remain in the orders loop and can adventure.
                }
              </p>
            </div>
          </div>

          <div class="role-info">
            <div class="info-item">
              <span class="label">Current Role</span>
              <span class="value" [class.ceo]="role.currentRole === 'CEO'"
                [class.vp]="role.currentRole === 'VP'">
                {{ role.currentRole }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">AI Assistant</span>
              <span class="value" [class.active]="role.aiAssistantActive">
                {{ role.aiAssistantActive ? 'Active' : 'Inactive' }}
              </span>
            </div>
            <div class="info-item">
              <span class="label">In Orders Loop</span>
              <span class="value">{{ role.inOrdersLoop ? 'Yes' : 'No' }}</span>
            </div>
          </div>

          <div class="actions">
            @if (role.currentRole === 'CEO') {
              <button mat-raised-button color="accent" (click)="switchTo('VP')">
                <mat-icon>swap_horiz</mat-icon> Switch to VP Mode
              </button>
              <p class="switch-hint">
                Switching to VP mode will activate your AI assistant to manage the corporation
                while you adventure.
              </p>
            } @else {
              <button mat-raised-button color="primary" (click)="switchTo('CEO')">
                <mat-icon>swap_horiz</mat-icon> Switch to CEO Mode
              </button>
              <p class="switch-hint">
                Switching to CEO mode will pause the AI assistant and give you full control.
              </p>
            }
          </div>
        </mat-card>
      } @else {
        <p class="loading">Loading role state...</p>
      }
    </div>
  `,
  styles: `
    .role-page {
      padding: var(--spacing-lg);
      max-width: 700px;
      margin: 0 auto;
    }
    h1 {
      margin: 0 0 var(--spacing-lg) 0;
    }
    .role-card {
      padding: var(--spacing-lg);
    }
    .role-header {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-lg);
      padding-bottom: var(--spacing-md);
      border-bottom: 1px solid var(--border-subtle);
    }
    .role-header mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: var(--mat-primary);
    }
    .role-header h2 {
      margin: 0;
    }
    .role-subtitle {
      margin: 4px 0 0 0;
      color: var(--text-secondary);
      font-size: 0.85rem;
    }
    .role-info {
      display: flex;
      flex-direction: column;
      gap: var(--spacing-sm);
      margin-bottom: var(--spacing-lg);
      padding: var(--spacing-md);
      background: var(--bg-elevated);
      border-radius: var(--radius-sm);
      border: 1px solid var(--border-subtle);
    }
    .info-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    .label {
      color: var(--text-hint);
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      font-weight: 500;
    }
    .value {
      font-weight: 600;
      font-size: 0.95rem;
    }
    .value.ceo { color: var(--info); }
    .value.vp { color: #ce93d8; }
    .value.active { color: var(--success); }
    .actions {
      text-align: center;
      padding-top: var(--spacing-md);
      border-top: 1px solid var(--border-subtle);
    }
    .switch-hint {
      margin-top: var(--spacing-sm);
      color: var(--text-hint);
      font-size: 0.8rem;
      font-style: italic;
    }
    .loading {
      color: var(--text-secondary);
      text-align: center;
      padding: var(--spacing-xl);
    }
  `,
})
export class RoleSwitchComponent implements OnInit {
  role: RoleState | null = null;

  constructor(
    private corpService: CorporationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadRole();
  }

  loadRole(): void {
    this.corpService.getRole().subscribe({
      next: (r) => (this.role = r),
      error: (err) => {
        console.error('Failed to load role', err);
        // Show default CEO role on error so the page doesn't hang
        this.role = {
          characterId: 1,
          currentRole: 'CEO',
          roleDisplayName: 'Chief Executive Officer',
          aiAssistantActive: false,
          inOrdersLoop: true,
        };
      },
    });
  }

  switchTo(role: string): void {
    const target = role === 'CEO' ? 'CEO' : 'VP';
    const ref = this.snackBar.open(`Switch to ${target} mode?`, 'Confirm', {
      duration: 8000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
    });
    ref.onAction().subscribe(() => {
      this.corpService.switchRole(role).subscribe({
        next: (r) => {
          this.role = r;
          this.corpService.loadOfficeState();
          this.snackBar.open(`Switched to ${target} mode`, 'Close', { duration: 3000 });
        },
        error: (err) => {
          this.snackBar.open('Failed to switch role', 'Close', { duration: 3000 });
          console.error('Failed to switch role', err);
        },
      });
    });
  }
}

import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { RouterLink } from '@angular/router';
import { CorporationService } from '../../core/services/corporation.service';
import { ContractSummary } from '../../core/models/corporation-state';

@Component({
  selector: 'app-contracts-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatTabsModule,
    MatProgressBarModule,
    MatIconModule,
    MatDialogModule,
    RouterLink,
  ],
  template: `
    <div class="traveller-contracts-page">
      <!-- Starfield Background -->
      <div class="starfield"></div>
      
      <div class="header traveller-header">
        <div class="header-content">
          <div class="header-badge">
            <span class="badge-icon">⬡</span>
          </div>
          <div class="header-text">
            <h1>CONTRACT DATABASE</h1>
            <p class="subtitle">Available & Active Missions</p>
          </div>
        </div>
      </div>

      <mat-tab-group class="traveller-tabs">
        <!-- Available Contracts -->
        <mat-tab label="Available ({{ available().length }})">
          <div class="tab-content">
            @if (available().length === 0) {
              <p class="empty">No available contracts. Generate samples from the office.</p>
            } @else {
              <table class="traveller-table contracts-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Type</th>
                    <th>Difficulty</th>
                    <th>Payment</th>
                    <th>XP</th>
                    <th>Duration</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (c of available(); track c.id) {
                    <tr>
                      <td>{{ c.title }}</td>
                      <td><span class="type-badge">{{ c.type }}</span></td>
                      <td>{{ c.difficulty }}/10</td>
                      <td>{{ c.payment | number }}</td>
                      <td>{{ c.xpReward }}</td>
                      <td>{{ c.durationTurns }} turns</td>
                      <td>
                        <button mat-raised-button class="traveller-btn" (click)="accept(c.id)">
                          Accept
                        </button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            }
          </div>
        </mat-tab>

        <!-- Active Contracts -->
        <mat-tab label="Active ({{ active().length }})">
          <div class="tab-content">
            @if (active().length === 0) {
              <p class="empty">No active contracts</p>
            } @else {
              <table class="traveller-table contracts-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Type</th>
                    <th>Progress</th>
                    <th>Turns Left</th>
                    <th>Crew</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (c of active(); track c.id) {
                    <tr>
                      <td><a routerLink="/contracts/{{ c.id }}">{{ c.title }}</a></td>
                      <td><span class="type-badge">{{ c.type }}</span></td>
                      <td>
                        <mat-progress-bar
                          mode="determinate"
                          [value]="c.progressPercent || 0"
                          class="traveller-progress"
                        ></mat-progress-bar>
                        <span class="progress-text">{{ (c.progressPercent || 0).toFixed(0) }}%</span>
                      </td>
                      <td>{{ c.turnsRemaining }}</td>
                      <td>{{ c.assignedCrewCount || 0 }}</td>
                      <td>
                        <button mat-stroked-button class="traveller-stroked-btn" (click)="processTurn(c.id)">
                          <mat-icon>play_arrow</mat-icon> Process Turn
                        </button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            }
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: `
    /* Traveller Theme - Space Opera Aesthetics */
    .traveller-contracts-page {
      padding: var(--spacing-lg);
      max-width: 1400px;
      margin: 0 auto;
      position: relative;
      min-height: 100vh;
      background: linear-gradient(135deg, #0a0e27 0%, #1a1a3e 30%, #0d1b2a 60%, #0a0e27 100%);
      color: #e0e0e0;
      font-family: 'Courier New', monospace;
    }

    /* Animated Starfield Background */
    .starfield {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      z-index: 0;
      background-image: 
        radial-gradient(2px 2px at 20px 30px, #ffffff, transparent),
        radial-gradient(2px 2px at 40px 70px, rgba(255,255,255,0.8), transparent),
        radial-gradient(1px 1px at 90px 40px, #ffffff, transparent),
        radial-gradient(1px 1px at 130px 80px, rgba(255,255,255,0.6), transparent),
        radial-gradient(2px 2px at 160px 30px, #ffffff, transparent),
        radial-gradient(1px 1px at 200px 60px, rgba(255,255,255,0.7), transparent),
        radial-gradient(2px 2px at 230px 90px, #ffffff, transparent),
        radial-gradient(1px 1px at 260px 20px, rgba(255,255,255,0.5), transparent),
        radial-gradient(2px 2px at 300px 50px, #ffffff, transparent),
        radial-gradient(1px 1px at 340px 80px, rgba(255,255,255,0.8), transparent),
        radial-gradient(2px 2px at 380px 30px, #ffffff, transparent),
        radial-gradient(1px 1px at 420px 70px, rgba(255,255,255,0.6), transparent),
        radial-gradient(2px 2px at 460px 40px, #ffffff, transparent),
        radial-gradient(1px 1px at 500px 90px, rgba(255,255,255,0.7), transparent),
        radial-gradient(2px 2px at 540px 20px, #ffffff, transparent),
        radial-gradient(1px 1px at 580px 60px, rgba(255,255,255,0.5), transparent),
        radial-gradient(2px 2px at 620px 80px, #ffffff, transparent),
        radial-gradient(1px 1px at 660px 30px, rgba(255,255,255,0.8), transparent),
        radial-gradient(2px 2px at 700px 50px, #ffffff, transparent),
        radial-gradient(1px 1px at 740px 90px, rgba(255,255,255,0.6), transparent);
      background-size: 800px 120px;
      animation: twinkle 5s ease-in-out infinite alternate;
    }

    @keyframes twinkle {
      0% { opacity: 0.5; }
      50% { opacity: 1; }
      100% { opacity: 0.6; }
    }

    /* Header Styling */
    .traveller-header {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: var(--spacing-lg);
      flex-wrap: wrap;
      gap: var(--spacing-sm);
      position: relative;
      z-index: 1;
      border-bottom: 2px solid #00ffcc;
      padding-bottom: var(--spacing-md);
    }

    .header-content {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);
      flex-wrap: wrap;
      width: 100%;
    }

    .header-badge {
      width: 60px;
      height: 60px;
      border: 2px solid #00ffcc;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: radial-gradient(circle, rgba(0,255,204,0.2) 0%, transparent 70%);
      animation: pulse-badge 2s ease-in-out infinite;
    }

    @keyframes pulse-badge {
      0%, 100% { box-shadow: 0 0 10px rgba(0,255,204,0.5); }
      50% { box-shadow: 0 0 20px rgba(0,255,204,0.8), 0 0 30px rgba(0,255,204,0.4); }
    }

    .badge-icon {
      font-size: 2rem;
      color: #00ffcc;
    }

    .header-text {
      flex: 1;
    }

    h1 {
      margin: 0;
      font-size: 1.75rem;
      color: #00ffcc;
      text-transform: uppercase;
      letter-spacing: 0.15em;
      text-shadow: 0 0 10px rgba(0,255,204,0.5);
      font-weight: 700;
    }

    .subtitle {
      margin: 4px 0 0 0;
      font-size: 0.75rem;
      color: #88aacc;
      letter-spacing: 0.1em;
    }

    .traveller-btn {
      background: linear-gradient(135deg, #006666 0%, #00aa88 100%) !important;
      color: #ffffff !important;
      border: 1px solid #00ffcc !important;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      font-family: 'Courier New', monospace;
      box-shadow: 0 0 10px rgba(0,255,204,0.3);
      transition: all 0.3s ease;
    }

    .traveller-btn:hover {
      box-shadow: 0 0 20px rgba(0,255,204,0.6);
      transform: translateY(-2px);
    }

    .traveller-stroked-btn {
      color: #00ffcc !important;
      border: 1px solid #00ffcc !important;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      font-family: 'Courier New', monospace;
      font-size: 0.75rem;
    }

    /* Traveller Tabs */
    .traveller-tabs {
      --mat-tab-header-active-label-text-color: #00ffcc;
      --mat-tab-header-inactive-label-text-color: #88aacc;
      --mat-tab-header-active-indicator-color: #00ffcc;
      --mat-tab-header-divider-color: rgba(0,255,204,0.2);
      position: relative;
      z-index: 1;
    }

    .tab-content {
      padding: var(--spacing-md) 0;
    }

    /* Traveller Table */
    .traveller-table {
      width: 100%;
      border-collapse: collapse;
    }

    .traveller-table th {
      padding: var(--spacing-sm) var(--spacing-md);
      text-align: left;
      border-bottom: 2px solid rgba(0,255,204,0.3);
      color: #00ffcc;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      font-size: 0.75rem;
      font-weight: 600;
    }

    .traveller-table td {
      padding: var(--spacing-sm) var(--spacing-md);
      text-align: left;
      border-bottom: 1px solid rgba(0,255,204,0.1);
      color: #c0c0c0;
    }

    .traveller-table a {
      color: #00ffcc;
      text-decoration: none;
      font-weight: 500;
      transition: all 0.2s ease;
    }

    .traveller-table a:hover {
      text-shadow: 0 0 5px rgba(0,255,204,0.5);
    }

    .traveller-table tbody tr:hover {
      background: rgba(0,255,204,0.05);
    }

    .type-badge {
      display: inline-flex;
      align-items: center;
      padding: 3px 10px;
      border-radius: 20px;
      background: rgba(0,255,204,0.1);
      font-size: 0.65rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.03em;
      color: #00ffcc;
      border: 1px solid rgba(0,255,204,0.3);
    }

    .traveller-progress {
      --mat-progress-bar-active-color: #00ffcc;
    }

    .progress-text {
      font-size: 0.7rem;
      color: #6688aa;
      font-weight: 500;
    }

    .empty {
      color: #6688aa;
      font-style: italic;
      text-align: center;
      padding: var(--spacing-xl);
    }

    /* Desktop-specific optimizations */
    :host-context(app-desktop-app) .traveller-contracts-page {
      padding: var(--spacing-xl);
    }
    :host-context(app-desktop-app) .traveller-table th,
    :host-context(app-desktop-app) .traveller-table td {
      padding: var(--spacing-md);
    }

    /* Mobile-specific optimizations */
    :host-context(app-mobile-app) .traveller-contracts-page {
      padding: var(--spacing-sm);
    }
    :host-context(app-mobile-app) h1 {
      font-size: 1.375rem;
    }
    :host-context(app-mobile-app) .traveller-table {
      display: block;
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }
    :host-context(app-mobile-app) .traveller-table th,
    :host-context(app-mobile-app) .traveller-table td {
      padding: var(--spacing-sm);
      font-size: 0.875rem;
      white-space: nowrap;
    }
  `,
})
export class ContractsListComponent implements OnInit {
  state = this.corpService.state;

  available() {
    return this.state()?.availableContracts || [];
  }

  active() {
    return this.state()?.activeContracts || [];
  }

  constructor(
    private corpService: CorporationService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.corpService.loadOfficeState();
  }

  accept(contractId: number): void {
    this.corpService.acceptContract(contractId).subscribe({
      next: () => this.corpService.loadOfficeState(),
      error: (err) => console.error('Failed to accept contract', err),
    });
  }

  processTurn(contractId: number): void {
    this.corpService.processContractTurn(contractId).subscribe({
      next: () => this.corpService.loadOfficeState(),
      error: (err) => console.error('Failed to process turn', err),
    });
  }
}

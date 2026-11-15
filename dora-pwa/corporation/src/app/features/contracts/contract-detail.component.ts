import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CorporationService } from '../../core/services/corporation.service';
import { ContractStatus } from '../../core/models/corporation-state';

@Component({
  selector: 'app-contract-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressBarModule,
    MatIconModule,
    RouterLink,
  ],
  template: `
    <div class="detail-page">
      <a routerLink="/contracts">← Back to Contracts</a>

      @if (status) {
        <mat-card class="detail-card">
          <h2>{{ status.title }}</h2>

          <div class="info-grid">
            <div class="info-item">
              <span class="label">Type</span>
              <span class="value">{{ status.type }}</span>
            </div>
            <div class="info-item">
              <span class="label">Difficulty</span>
              <span class="value">{{ status.difficulty }}/10</span>
            </div>
            <div class="info-item">
              <span class="label">Status</span>
              <span class="value">{{ status.status }}</span>
            </div>
            <div class="info-item">
              <span class="label">Turns Remaining</span>
              <span class="value">{{ status.turnsRemaining }} / {{ status.durationTurns }}</span>
            </div>
          </div>

          <div class="progress-section">
            <h3>Progress</h3>
            <mat-progress-bar
              mode="determinate"
              [value]="status.progressPercent"
              color="accent"
            ></mat-progress-bar>
            <span class="progress-text">{{ status.progressPercent.toFixed(1) }}%</span>
          </div>

          <div class="success-section">
            <h3>Estimated Success Chance</h3>
            <div class="success-value" [class.high]="status.estimatedSuccessChance > 70"
              [class.medium]="status.estimatedSuccessChance > 40"
              [class.low]="status.estimatedSuccessChance <= 40">
              {{ status.estimatedSuccessChance.toFixed(1) }}%
            </div>
          </div>

          <div class="crew-section">
            <h3>Assigned Crew ({{ status.assignedCrew.length }})</h3>
            @if (status.assignedCrew.length === 0) {
              <p class="empty">No crew assigned</p>
            } @else {
              <table class="crew-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Position</th>
                    <th>Performance</th>
                  </tr>
                </thead>
                <tbody>
                  @for (member of status.assignedCrew; track member.aiCharacterId) {
                    <tr>
                      <td>{{ member.name }}</td>
                      <td>{{ member.position }}</td>
                      <td>{{ member.performanceRating | number:'1.1-1' }}</td>
                    </tr>
                  }
                </tbody>
              </table>
            }
          </div>

          @if (status.status === 'ACTIVE') {
            <button mat-raised-button color="primary" (click)="processTurn()">
              <mat-icon>play_arrow</mat-icon> Process Turn
            </button>
          }
        </mat-card>
      } @else {
        <p class="loading">Loading contract details...</p>
      }
    </div>
  `,
  styles: `
    .detail-page {
      padding: var(--spacing-lg);
      max-width: 900px;
      margin: 0 auto;
    }
    .detail-page a {
      color: var(--primary-light);
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: var(--spacing-xs);
      margin-bottom: var(--spacing-md);
      font-weight: 500;
      font-size: 0.875rem;
    }
    .detail-card {
      padding: var(--spacing-lg);
    }
    h2 {
      margin: 0 0 var(--spacing-lg) 0;
    }
    h3 {
      margin: var(--spacing-lg) 0 var(--spacing-sm) 0;
      font-size: 0.85rem;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-lg);
    }
    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }
    .label {
      font-size: 0.7rem;
      color: var(--text-hint);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      font-weight: 500;
    }
    .value {
      font-size: 1.1rem;
      font-weight: 600;
      color: var(--text-primary);
    }
    .progress-section {
      margin: var(--spacing-lg) 0;
    }
    .progress-text {
      font-size: 0.8rem;
      color: var(--text-secondary);
      font-weight: 500;
    }
    .success-value {
      font-size: 2.5rem;
      font-weight: 700;
      letter-spacing: -0.02em;
    }
    .success-value.high { color: var(--success); }
    .success-value.medium { color: var(--warning); }
    .success-value.low { color: var(--error); }
    .crew-section {
      margin-top: var(--spacing-lg);
    }
    .crew-table {
      width: 100%;
      border-collapse: collapse;
    }
    .crew-table th, .crew-table td {
      padding: var(--spacing-sm) var(--spacing-md);
      text-align: left;
      border-bottom: 1px solid var(--border-subtle);
    }
    .empty {
      color: var(--text-hint);
      font-style: italic;
    }
    .loading {
      color: var(--text-secondary);
      text-align: center;
      padding: var(--spacing-xl);
    }
  `,
})
export class ContractDetailComponent implements OnInit {
  contractId = 0;
  status: ContractStatus | null = null;

  constructor(
    private route: ActivatedRoute,
    private corpService: CorporationService
  ) {}

  ngOnInit(): void {
    this.contractId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadStatus();
  }

  loadStatus(): void {
    this.corpService.getContractStatus(this.contractId).subscribe({
      next: (s) => (this.status = s),
      error: (err) => console.error('Failed to load contract status', err),
    });
  }

  processTurn(): void {
    this.corpService.processContractTurn(this.contractId).subscribe({
      next: () => this.loadStatus(),
      error: (err) => console.error('Failed to process turn', err),
    });
  }
}

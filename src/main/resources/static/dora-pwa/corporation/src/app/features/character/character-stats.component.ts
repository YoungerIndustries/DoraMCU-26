import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterLink } from '@angular/router';
import { CorporationService } from '../../core/services/corporation.service';
import { CharacterStats, getCharacterLevel, getLevelProgress, getXpForNextLevel } from '../../core/models/traveller-character';

@Component({
  selector: 'app-character-stats',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatProgressBarModule, RouterLink],
  template: `
    <div class="character-stats">
      @if (loading) {
        <mat-progress-bar mode="indeterminate"></mat-progress-bar>
      }

      @if (error) {
        <div class="error">
          <p>{{ error }}</p>
          <button mat-raised-button color="primary" (click)="loadStats()">Retry</button>
        </div>
      }

      @if (!loading && !error && stats) {
        <div class="stats-content">
          <mat-card class="header-card">
            <div class="header">
              <h2>{{ stats.characterName }}</h2>
              <p class="subtitle">Character Statistics & Progress</p>
            </div>
          </mat-card>

          <div class="stats-grid">
            <mat-card class="stat-card">
              <h3>Level</h3>
              <div class="level-display">
                <span class="level-number">{{ level }}</span>
                <span class="level-label">Level {{ level }}</span>
              </div>
            </mat-card>

            <mat-card class="stat-card">
              <h3>Total XP</h3>
              <div class="xp-display">
                <span class="xp-number">{{ stats.totalXpEarned }}</span>
                <span class="xp-label">Experience Points</span>
              </div>
            </mat-card>

            <mat-card class="stat-card">
              <h3>Contracts Completed</h3>
              <div class="contracts-display">
                <span class="contracts-number">{{ stats.contractsCompleted }}</span>
                <span class="contracts-label">Completed</span>
              </div>
            </mat-card>

            <mat-card class="stat-card">
              <h3>Active Contracts</h3>
              <div class="active-display">
                <span class="active-number">{{ stats.contractsActive }}</span>
                <span class="active-label">In Progress</span>
              </div>
            </mat-card>
          </div>

          <mat-card class="xp-progress-card">
            <h3>XP Progress to Next Level</h3>
            <div class="xp-progress">
              <div class="xp-info">
                <span>{{ stats.totalXpEarned }} / {{ stats.totalXpEarned + xpForNext }} XP</span>
                <span>{{ levelProgress }}%</span>
              </div>
              <mat-progress-bar mode="determinate" [value]="levelProgress"></mat-progress-bar>
              <div class="xp-next">
                @if (xpForNext > 0) {
                  <span>{{ xpForNext }} XP until Level {{ level + 1 }}</span>
                }
                @if (xpForNext === 0) {
                  <span class="max-level">Maximum Level Reached!</span>
                }
              </div>
            </div>
            @if (stats.isLevelUpReady) {
              <div class="level-up-notification">
                <p>🎉 Level Up! Your character has reached a new level!</p>
              </div>
            }
          </mat-card>

          <mat-card class="history-card">
            <h3>Contract History</h3>
            @if (stats.assignments && stats.assignments.length > 0) {
              <div class="history-list">
                @for (assignment of stats.assignments; track assignment.id) {
                  <div class="history-item">
                    <div class="history-info">
                      <h4>{{ assignment.contractTitle }}</h4>
                      <div class="history-meta">
                        <span class="status" [class.completed]="assignment.status === 'COMPLETED'"
                              [class.failed]="assignment.status === 'FAILED'"
                              [class.active]="assignment.status === 'ACTIVE'">
                          {{ assignment.status }}
                        </span>
                        <span class="xp">XP: {{ assignment.xpEarned }}</span>
                        <span class="date">Assigned: {{ assignment.assignedAt | date:'shortDate' }}</span>
                      </div>
                    </div>
                  </div>
                }
              </div>
            } @else {
              <p class="no-data">No contract history available.</p>
            }
          </mat-card>
        </div>
      }

      @if (!loading && !error && !stats) {
        <div class="no-stats">
          <mat-card>
            <p>No character stats found.</p>
            <button mat-raised-button color="primary" routerLink="/dashboard">Back to Dashboard</button>
          </mat-card>
        </div>
      }
    </div>
  `,
  styles: `
    .character-stats {
      padding: var(--spacing-lg);
      max-width: 1200px;
      margin: 0 auto;
    }
    .loading {
      margin-bottom: var(--spacing-md);
    }
    .error {
      color: var(--error);
      padding: var(--spacing-md);
    }
    .header-card {
      margin-bottom: var(--spacing-md);
      position: relative;
      overflow: hidden;
    }
    .header-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, var(--primary-dark), var(--mat-primary));
    }
    .header {
      padding: var(--spacing-lg);
    }
    .header h2 {
      margin: 0 0 var(--spacing-sm) 0;
      color: var(--text-primary);
    }
    .subtitle {
      margin: 0;
      color: var(--text-secondary);
      font-size: 0.85rem;
    }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-lg);
    }
    @media (max-width: 768px) {
      .stats-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }
    .stat-card {
      text-align: center;
      position: relative;
      overflow: hidden;
    }
    .stat-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, var(--primary-dark), var(--mat-primary));
    }
    .stat-card h3 {
      margin: var(--spacing-md) var(--spacing-md) var(--spacing-sm);
      color: var(--text-secondary);
      font-size: 0.75rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .level-display, .xp-display, .contracts-display, .active-display {
      padding: var(--spacing-md);
    }
    .level-number, .xp-number, .contracts-number, .active-number {
      display: block;
      font-size: 2rem;
      font-weight: 700;
      color: var(--mat-primary);
      letter-spacing: -0.02em;
    }
    .level-label, .xp-label, .contracts-label, .active-label {
      display: block;
      font-size: 0.7rem;
      color: var(--text-hint);
      margin-top: var(--spacing-xs);
    }
    .xp-progress-card {
      margin-bottom: var(--spacing-md);
    }
    .xp-progress-card h3 {
      margin: var(--spacing-md) var(--spacing-md) var(--spacing-sm);
      color: var(--text-secondary);
      font-size: 0.85rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .xp-progress {
      padding: 0 var(--spacing-md) var(--spacing-md);
    }
    .xp-info {
      display: flex;
      justify-content: space-between;
      margin-bottom: var(--spacing-sm);
      font-size: 0.8rem;
      color: var(--text-secondary);
      font-weight: 500;
    }
    .xp-next {
      margin-top: var(--spacing-sm);
      font-size: 0.8rem;
      color: var(--text-hint);
    }
    .max-level {
      color: var(--success);
      font-weight: 700;
    }
    .level-up-notification {
      padding: var(--spacing-md);
      margin: 0 var(--spacing-md) var(--spacing-md);
      background: var(--success-bg);
      border-radius: var(--radius-sm);
      border-left: 4px solid var(--success);
    }
    .level-up-notification p {
      margin: 0;
      color: var(--success);
      font-weight: 600;
    }
    .history-card {
      margin-bottom: var(--spacing-md);
    }
    .history-card h3 {
      margin: var(--spacing-md) var(--spacing-md) var(--spacing-sm);
      color: var(--text-secondary);
      font-size: 0.85rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .history-list {
      padding: 0 var(--spacing-md) var(--spacing-md);
    }
    .history-item {
      padding: var(--spacing-sm) 0;
      border-bottom: 1px solid var(--border-subtle);
    }
    .history-item:last-child {
      border-bottom: none;
    }
    .history-info h4 {
      margin: 0 0 var(--spacing-sm) 0;
      color: var(--text-primary);
      font-size: 0.95rem;
    }
    .history-meta {
      display: flex;
      gap: var(--spacing-md);
      font-size: 0.8rem;
      color: var(--text-hint);
    }
    .status.completed {
      color: var(--success);
    }
    .status.failed {
      color: var(--error);
    }
    .status.active {
      color: var(--mat-primary);
    }
    .xp {
      color: #ce93d8;
    }
    .no-data {
      padding: var(--spacing-md);
      color: var(--text-hint);
      text-align: center;
    }
    .no-stats {
      text-align: center;
      padding: var(--spacing-xl) var(--spacing-md);
    }
  `,
})
export class CharacterStatsComponent implements OnInit {
  loading = true;
  error: string | null = null;

  stats: CharacterStats | null = null;
  level = 1;
  levelProgress = 0;
  xpForNext = 100;

  constructor(
    private corpService: CorporationService
  ) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.loading = true;
    this.error = null;
    const charId = this.corpService.characterId;

    this.corpService.getCharacterStats(charId).subscribe({
      next: (stats: CharacterStats) => {
        this.stats = stats;
        this.level = getCharacterLevel(stats.totalXpEarned);
        this.levelProgress = getLevelProgress(stats.totalXpEarned);
        this.xpForNext = getXpForNextLevel(stats.totalXpEarned);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load character stats: ' + err.message;
        this.loading = false;
      },
    });
  }
}

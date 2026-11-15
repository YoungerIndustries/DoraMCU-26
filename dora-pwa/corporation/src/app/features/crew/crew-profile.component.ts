import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CorporationService } from '../../core/services/corporation.service';
import { CareerDetails } from '../../core/models/corporation-state';
import { LevelBadgeComponent } from '../../shared/components/level-badge.component';
import { SkillBarComponent } from '../../shared/components/skill-bar.component';

@Component({
  selector: 'app-crew-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatProgressBarModule,
    MatSnackBarModule,
    RouterLink,
    LevelBadgeComponent,
    SkillBarComponent,
  ],
  template: `
    <div class="profile-page">
      <a routerLink="/crew">← Back to Crew</a>

      @if (career) {
        <mat-card class="profile-card">
          <div class="profile-header">
            <h2>{{ career.aiCharacterName }}</h2>
            <app-level-badge [level]="career.careerLevel"></app-level-badge>
          </div>

          <div class="info-grid">
            <div class="info-item">
              <span class="label">Position</span>
              <span class="value">{{ career.position }}</span>
            </div>
            <div class="info-item">
              <span class="label">Career Level</span>
              <span class="value">{{ career.careerLevel }}</span>
            </div>
            <div class="info-item">
              <span class="label">XP Earned</span>
              <span class="value">{{ career.xpEarned }}</span>
            </div>
            <div class="info-item">
              <span class="label">Next Level Threshold</span>
              <span class="value">{{ career.nextLevelThreshold }}</span>
            </div>
            <div class="info-item">
              <span class="label">Salary Multiplier</span>
              <span class="value">{{ career.salaryMultiplier }}x</span>
            </div>
            <div class="info-item">
              <span class="label">Effective Salary</span>
              <span class="value">{{ career.effectiveSalary | number }}</span>
            </div>
          </div>

          <div class="xp-section">
            <h3>XP Progress</h3>
            <mat-progress-bar
              mode="determinate"
              [value]="getProgressPercent()"
              color="accent"
            ></mat-progress-bar>
            <span class="progress-text">
              {{ career.xpEarned }} / {{ career.nextLevelThreshold }} XP
            </span>
            @if (career.eligibleForPromotion) {
              <span class="promotion-badge">Eligible for Promotion!</span>
            }
          </div>

          <div class="skills-section">
            <h3>Skills</h3>
            @for (entry of career.skills | keyvalue; track entry.key) {
              <app-skill-bar [name]="entry.key" [level]="entry.value"></app-skill-bar>
            }
          </div>

          <div class="actions">
            <button mat-raised-button color="primary" (click)="promote()"
              [disabled]="!career.eligibleForPromotion">
              Promote
            </button>
            <button mat-stroked-button color="warn" (click)="demote()">
              Demote
            </button>
          </div>
        </mat-card>
      } @else {
        <p class="loading">Loading crew profile...</p>
      }
    </div>
  `,
  styles: `
    .profile-page {
      padding: var(--spacing-lg);
      max-width: 900px;
      margin: 0 auto;
    }
    .profile-page a {
      color: var(--primary-light);
      text-decoration: none;
      display: inline-flex;
      align-items: center;
      gap: var(--spacing-xs);
      margin-bottom: var(--spacing-md);
      font-weight: 500;
      font-size: 0.875rem;
    }
    .profile-card {
      padding: var(--spacing-lg);
    }
    .profile-header {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-lg);
      padding-bottom: var(--spacing-md);
      border-bottom: 1px solid var(--border-subtle);
    }
    .profile-header h2 {
      margin: 0;
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
    .xp-section {
      margin: var(--spacing-lg) 0;
    }
    .progress-text {
      font-size: 0.8rem;
      color: var(--text-secondary);
      font-weight: 500;
    }
    .promotion-badge {
      display: inline-flex;
      align-items: center;
      margin-left: var(--spacing-sm);
      padding: 3px 10px;
      border-radius: 20px;
      background: var(--success-bg);
      color: var(--success);
      font-size: 0.7rem;
      font-weight: 600;
    }
    .skills-section {
      margin: var(--spacing-lg) 0;
    }
    .actions {
      display: flex;
      gap: var(--spacing-sm);
      margin-top: var(--spacing-lg);
      padding-top: var(--spacing-md);
      border-top: 1px solid var(--border-subtle);
    }
    .loading {
      color: var(--text-secondary);
      text-align: center;
      padding: var(--spacing-xl);
    }
  `,
})
export class CrewProfileComponent implements OnInit {
  hiringId = 0;
  career: CareerDetails | null = null;

  constructor(
    private route: ActivatedRoute,
    private corpService: CorporationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.hiringId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadCareer();
  }

  loadCareer(): void {
    this.corpService.getCrewCareer(this.hiringId).subscribe({
      next: (c) => (this.career = c),
      error: (err) => console.error('Failed to load career details', err),
    });
  }

  getProgressPercent(): number {
    if (!this.career || this.career.nextLevelThreshold === 0) return 100;
    return Math.min(100, (this.career.xpEarned / this.career.nextLevelThreshold) * 100);
  }

  promote(): void {
    this.corpService.promoteCrew(this.hiringId).subscribe({
      next: (c) => {
        this.career = c;
        this.snackBar.open('Crew member promoted!', 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.snackBar.open('Failed to promote crew member', 'Close', { duration: 3000 });
        console.error('Failed to promote', err);
      },
    });
  }

  demote(): void {
    const ref = this.snackBar.open('Demote this crew member?', 'Confirm', {
      duration: 8000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
    });
    ref.onAction().subscribe(() => {
      this.corpService.demoteCrew(this.hiringId).subscribe({
        next: (c) => {
          this.career = c;
          this.snackBar.open('Crew member demoted', 'Close', { duration: 3000 });
        },
        error: (err) => {
          this.snackBar.open('Failed to demote crew member', 'Close', { duration: 3000 });
          console.error('Failed to demote', err);
        },
      });
    });
  }
}

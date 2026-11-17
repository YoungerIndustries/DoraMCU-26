import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { CorporationService } from '../../core/services/corporation.service';
import { QualifiedContract, CharacterAssignment } from '../../core/models/traveller-character';

@Component({
  selector: 'app-character-contracts',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatProgressBarModule, MatTabsModule, MatSnackBarModule],
  template: `
    <div class="character-contracts">
      @if (loading) {
        <mat-progress-bar mode="indeterminate"></mat-progress-bar>
      }

      @if (error) {
        <div class="error">
          <p>{{ error }}</p>
          <button mat-raised-button color="primary" (click)="loadContracts()">Retry</button>
        </div>
      }

      @if (!loading && !error) {
        <div class="contracts-content">
          <mat-card class="header-card">
            <div class="header">
              <h2>Qualified Contracts</h2>
              <p class="subtitle">Contracts your character can work on based on abilities and skills</p>
            </div>
          </mat-card>

          <mat-card class="contracts-card">
            <mat-tab-group>
              <mat-tab label="Qualified ({{ qualifiedContracts.length }})">
                @if (qualifiedContracts.length > 0) {
                  <div class="contracts-list">
                    @for (contract of qualifiedContracts; track contract.id) {
                      <div class="contract-item">
                        <div class="contract-info">
                          <h3>{{ contract.title }}</h3>
                          <div class="contract-meta">
                            <span class="type">{{ contract.type }}</span>
                            <span class="difficulty">Difficulty: {{ contract.difficulty }}</span>
                            <span class="payment">Payment: {{ contract.payment }} credits</span>
                            <span class="xp">XP: {{ contract.xpReward }}</span>
                            <span class="duration">Duration: {{ contract.durationTurns }} turns</span>
                          </div>
                          <div class="qualification">
                            <span class="bonus">Character Bonus: +{{ contract.characterBonus.toFixed(1) }}</span>
                            <span class="reason">{{ contract.qualificationReason }}</span>
                          </div>
                          @if (contract.requiredSkills && contract.requiredSkills.length > 0) {
                            <div class="required-skills">
                              <span>Required: </span>
                              @for (skill of contract.requiredSkills; track skill) {
                                <span class="skill-tag">{{ skill }}</span>
                              }
                            </div>
                          }
                        </div>
                        <div class="contract-actions">
                          <button mat-raised-button color="primary" (click)="assignToContract(contract.id)">
                            Assign Character
                          </button>
                        </div>
                      </div>
                    }
                  </div>
                } @else {
                  <p class="no-data">No qualified contracts available.</p>
                }
              </mat-tab>

              <mat-tab label="Active ({{ activeAssignments.length }})">
                @if (activeAssignments.length > 0) {
                  <div class="contracts-list">
                    @for (assignment of activeAssignments; track assignment.id) {
                      <div class="contract-item">
                        <div class="contract-info">
                          <h3>{{ assignment.contractTitle }}</h3>
                          <div class="contract-meta">
                            <span class="status" [class.active]="assignment.status === 'ACTIVE'">
                              Status: {{ assignment.status }}
                            </span>
                            <span class="xp">XP Earned: {{ assignment.xpEarned }}</span>
                          </div>
                        </div>
                        <div class="contract-actions">
                          <span class="assigned-date">Assigned: {{ assignment.assignedAt | date:'shortDate' }}</span>
                        </div>
                      </div>
                    }
                  </div>
                } @else {
                  <p class="no-data">No active assignments.</p>
                }
              </mat-tab>
            </mat-tab-group>
          </mat-card>
        </div>
      }
    </div>
  `,
  styles: `
    .character-contracts {
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
    .contracts-card {
      margin-bottom: var(--spacing-md);
    }
    .contracts-list {
      padding: var(--spacing-md);
    }
    .contract-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: var(--spacing-md);
      margin-bottom: var(--spacing-sm);
      background: var(--bg-elevated);
      border-radius: var(--radius-sm);
      border-left: 4px solid var(--mat-primary);
      transition: background var(--transition-fast);
    }
    .contract-item:hover {
      background: var(--bg-card-hover);
    }
    .contract-info {
      flex: 1;
    }
    .contract-info h3 {
      margin: 0 0 var(--spacing-sm) 0;
      color: var(--text-primary);
      font-size: 1rem;
    }
    .contract-meta {
      display: flex;
      flex-wrap: wrap;
      gap: var(--spacing-md);
      font-size: 0.8rem;
      color: var(--text-secondary);
      margin-bottom: var(--spacing-sm);
    }
    .type {
      color: var(--mat-primary);
      font-weight: 600;
    }
    .difficulty {
      color: var(--warning);
    }
    .payment {
      color: var(--success);
    }
    .xp {
      color: #ce93d8;
    }
    .qualification {
      display: flex;
      gap: var(--spacing-md);
      font-size: 0.8rem;
      margin-bottom: var(--spacing-xs);
    }
    .bonus {
      color: var(--success);
      font-weight: 600;
    }
    .reason {
      color: var(--text-hint);
      font-style: italic;
    }
    .required-skills {
      font-size: 0.8rem;
      color: var(--text-hint);
    }
    .skill-tag {
      display: inline-flex;
      align-items: center;
      padding: 2px 8px;
      margin-left: 4px;
      background: var(--bg-card);
      border-radius: 3px;
      color: var(--text-secondary);
      font-size: 0.7rem;
      font-weight: 500;
    }
    .contract-actions {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
      gap: var(--spacing-sm);
    }
    .assigned-date {
      font-size: 0.7rem;
      color: var(--text-hint);
    }
    .status.active {
      color: var(--success);
    }
    .no-data {
      padding: var(--spacing-md);
      color: var(--text-hint);
      text-align: center;
    }
  `,
})
export class CharacterContractsComponent implements OnInit {
  loading = true;
  error: string | null = null;

  qualifiedContracts: QualifiedContract[] = [];
  activeAssignments: CharacterAssignment[] = [];

  constructor(
    private corpService: CorporationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadContracts();
  }

  loadContracts(): void {
    this.loading = true;
    this.error = null;
    const charId = this.corpService.characterId;

    // Parallelize both API calls
    forkJoin({
      contracts: this.corpService.getQualifiedContracts(charId),
      assignments: this.corpService.getCharacterAssignments(charId),
    }).subscribe({
      next: ({ contracts, assignments }) => {
        this.qualifiedContracts = contracts;
        this.activeAssignments = assignments.filter(a => a.status === 'ACTIVE');
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load contracts: ' + err.message;
        this.loading = false;
      },
    });
  }

  assignToContract(contractId: number): void {
    const charId = this.corpService.characterId;
    this.corpService.assignCharacterToContract(charId, contractId).subscribe({
      next: () => {
        this.snackBar.open('Character assigned to contract!', 'Close', { duration: 3000 });
        this.loadContracts();
      },
      error: (err: any) => {
        this.snackBar.open('Failed to assign character: ' + err.message, 'Close', { duration: 3000 });
      },
    });
  }
}

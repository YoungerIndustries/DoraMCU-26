import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CorporationService } from '../../core/services/corporation.service';
import { PlayerCharactersService } from '../../core/services/player-characters.service';
import { PlayerCharacterEntry } from '../../core/models/player-character';

@Component({
  selector: 'app-character-landing',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatProgressSpinnerModule, MatIconModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="landing-container">
      <div class="landing-header">
        <h1 class="landing-title">Select Your Character</h1>
        <p class="landing-subtitle">Choose a character to access your corporation dashboard</p>
      </div>

      <!-- Loading State -->
      <div *ngIf="loading" class="loading-state">
        <mat-progress-spinner diameter="48" mode="indeterminate"></mat-progress-spinner>
        <p>Loading your characters...</p>
      </div>

      <!-- Error State -->
      <div *ngIf="!loading && error" class="error-state">
        <mat-icon color="warn">error_outline</mat-icon>
        <p>{{ error }}</p>
        <button mat-raised-button color="primary" (click)="loadCharacters()">
          <mat-icon>refresh</mat-icon>
          Retry
        </button>
      </div>

      <!-- Character List -->
      <div *ngIf="!loading && !error" class="character-list">
        <div *ngIf="characters().length === 0" class="empty-state">
          <mat-icon>person_add</mat-icon>
          <h2>No Characters Yet</h2>
          <p>Create your first character to begin your corporation journey.</p>
          <button mat-raised-button color="primary" (click)="createNewCharacter()">
            <mat-icon>add_circle</mat-icon>
            Create New Character
          </button>
        </div>

        <div class="characters-grid" *ngIf="characters().length > 0">
          <mat-card
            *ngFor="let char of characters()"
            class="character-card"
            (click)="selectCharacter(char)"
            [class.commander]="char.characterRole === 'COMMANDER'"
          >
            <div class="card-header">
              <span class="role-badge" [class]="char.characterRole.toLowerCase()">{{ char.characterRole }}</span>
              <mat-icon *ngIf="char.isAiControlled" class="ai-icon" matTooltip="AI Controlled">smart_toy</mat-icon>
            </div>
            <h3 class="char-name">{{ char.characterName }}</h3>
            <div class="char-details">
              <div class="detail-row" *ngIf="char.galaxyName">
                <mat-icon>public</mat-icon>
                <span>{{ char.galaxyName }}</span>
              </div>
              <div class="detail-row" *ngIf="char.homeLocationId">
                <mat-icon>location_on</mat-icon>
                <span>{{ char.homeLocationType || 'Location' }}: {{ char.homeLocationId }}</span>
              </div>
              <div class="detail-row" *ngIf="char.characterType">
                <mat-icon>badge</mat-icon>
                <span>{{ char.characterType }}</span>
              </div>
            </div>
          </mat-card>

          <!-- Create New Character Card -->
          <mat-card class="character-card create-card" (click)="createNewCharacter()">
            <mat-icon class="create-icon">add_circle_outline</mat-icon>
            <h3 class="char-name">Create New Character</h3>
            <p class="create-hint">Register a new character for your corporation</p>
          </mat-card>
        </div>
      </div>
    </div>
  `,
  styles: `
    .landing-container {
      max-width: 960px;
      margin: 0 auto;
      padding: var(--spacing-xl);
    }

    .landing-header {
      text-align: center;
      margin-bottom: var(--spacing-xl);
    }

    .landing-title {
      font-size: 2rem;
      font-weight: 500;
      margin: 0 0 var(--spacing-sm);
      color: var(--text-primary);
    }

    .landing-subtitle {
      font-size: 1rem;
      color: var(--text-secondary);
      margin: 0;
    }

    .loading-state, .error-state, .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: var(--spacing-xl);
      gap: var(--spacing-md);
    }

    .loading-state p, .error-state p, .empty-state p {
      color: var(--text-secondary);
      margin: 0;
    }

    .error-state mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: var(--text-hint);
    }

    .empty-state h2 {
      margin: 0;
      color: var(--text-primary);
    }

    .characters-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: var(--spacing-lg);
    }

    .character-card {
      cursor: pointer;
      transition: transform var(--transition-fast), box-shadow var(--transition-fast);
      background: var(--bg-card);
      border: 1px solid var(--border-subtle);
    }

    .character-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-md);
      background: var(--bg-card-hover);
    }

    .character-card.commander {
      border-color: var(--mat-primary);
      box-shadow: var(--shadow-glow);
    }

    .card-header {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      margin-bottom: var(--spacing-sm);
    }

    .role-badge {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 500;
      text-transform: uppercase;
    }

    .role-badge.commander {
      background: rgba(100, 181, 246, 0.2);
      color: var(--mat-primary);
    }

    .role-badge.crew {
      background: rgba(102, 187, 106, 0.2);
      color: var(--success);
    }

    .role-badge.owned {
      background: rgba(255, 167, 38, 0.2);
      color: var(--warning);
    }

    .role-badge.encountered {
      background: rgba(158, 158, 158, 0.2);
      color: var(--text-secondary);
    }

    .ai-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: var(--accent);
    }

    .char-name {
      font-size: 1.1rem;
      font-weight: 500;
      margin: 0 0 var(--spacing-sm);
      color: var(--text-primary);
    }

    .char-details {
      display: flex;
      flex-direction: column;
      gap: var(--spacing-xs);
    }

    .detail-row {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      font-size: 0.875rem;
      color: var(--text-secondary);
    }

    .detail-row mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      color: var(--text-hint);
    }

    .create-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      border-style: dashed;
      min-height: 180px;
      text-align: center;
    }

    .create-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: var(--text-hint);
      margin-bottom: var(--spacing-sm);
    }

    .create-hint {
      font-size: 0.875rem;
      color: var(--text-hint);
      margin: 0;
    }
  `
})
export class CharacterLandingComponent implements OnInit {
  characters: import('@angular/core').Signal<PlayerCharacterEntry[]>;
  loading = true;
  error: string | null = null;

  private _characters = new Array<PlayerCharacterEntry>();

  constructor(
    private corporationService: CorporationService,
    private playerCharactersService: PlayerCharactersService,
    private router: Router,
    private snackBar: MatSnackBar,
  ) {
    this.characters = () => this._characters;
  }

  ngOnInit(): void {
    this.loadCharacters();
  }

  loadCharacters(): void {
    this.loading = true;
    this.error = null;

    const playerId = this.corporationService.getPlayerId();
    if (!playerId) {
      this.error = 'Player ID not found. Please complete onboarding first.';
      this.loading = false;
      return;
    }

    this.playerCharactersService.getCharacters(playerId).subscribe({
      next: (chars) => {
        this._characters = chars;
        this.loading = false;
        // Store playerId for future use
        this.playerCharactersService.setPlayerId(playerId);
      },
      error: (err) => {
        console.error('Failed to load characters', err);
        this.error = 'Failed to load characters. Please check your connection and try again.';
        this.loading = false;
      },
    });
  }

  selectCharacter(char: PlayerCharacterEntry): void {
    // Set as active character
    this.corporationService.setActiveCharacter(char.characterId);

    // If not already commander, set as commander
    if (char.characterRole !== 'COMMANDER') {
      const playerId = this.corporationService.getPlayerId();
      if (playerId) {
        this.playerCharactersService.setCommander(playerId, char.id).subscribe({
          next: () => {
            this.snackBar.open(`${char.characterName} set as commander`, 'OK', { duration: 3000 });
          },
          error: (err) => {
            console.warn('Failed to set commander', err);
          },
        });
      }
    }

    this.snackBar.open(`Landing as ${char.characterName}`, 'OK', { duration: 2000 });

    // Navigate to dashboard
    this.router.navigate(['/dashboard']);
  }

  createNewCharacter(): void {
    // Navigate to onboarding for new character creation
    this.router.navigate(['/character']);
  }
}

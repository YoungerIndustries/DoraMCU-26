import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CorporationService } from '../../core/services/corporation.service';
import { CharacterStats, CharacterAbilities, CharacterSkill, CharacterCareer } from '../../core/models/traveller-character';
import { AbilityBarComponent } from '../../shared/components/ability-bar.component';
import { SkillBarComponent } from '../../shared/components/skill-bar.component';

@Component({
  selector: 'app-character-overview',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatCardModule, MatProgressBarModule, MatTabsModule, RouterLink, AbilityBarComponent, SkillBarComponent],
  template: `
    <div class="traveller-character-overview">
      <!-- Starfield Background -->
      <div class="starfield"></div>
      
      @if (loading) {
        <mat-progress-bar mode="indeterminate" class="traveller-progress"></mat-progress-bar>
      }

      @if (error) {
        <div class="error traveller-error">
          <p>{{ error }}</p>
          <button mat-raised-button class="traveller-btn" (click)="loadCharacter()">Retry</button>
        </div>
      }

      @if (!loading && !error && character) {
        <div class="character-content">
          <mat-card class="traveller-card header-card">
            <div class="header">
              <div class="character-info">
                <div class="character-badge">
                  <span class="badge-icon">⬡</span>
                </div>
                <div class="info-text">
                  <h2>{{ character.characterName }}</h2>
                  <p class="subtitle">Age: {{ character.age }} | Wealth: {{ character.wealth }} credits</p>
                </div>
              </div>
            </div>
          </mat-card>

          @if (abilities) {
            <mat-card class="traveller-card abilities-card">
              <div class="card-header traveller-card-header">
                <div class="header-left">
                  <span class="card-icon">◈</span>
                  <h3>ABILITIES - UWP PROFILE</h3>
                </div>
              </div>
              <div class="abilities-grid">
                @for (ability of abilitiesList; track ability.key) {
                  <app-ability-bar
                    [key]="ability.key"
                    [name]="ability.name"
                    [score]="ability.score"
                  ></app-ability-bar>
                }
              </div>
            </mat-card>
          }

          <mat-card class="traveller-card skills-card">
            <div class="card-header traveller-card-header">
              <div class="header-left">
                <span class="card-icon">◈</span>
                <h3>SKILLS DATABASE</h3>
              </div>
            </div>
            <mat-tab-group class="traveller-tabs">
              @if (skillsCategories.length > 0) {
                @for (category of skillsCategories; track category.key) {
                  <mat-tab [label]="category.key">
                    <div class="skills-list">
                      @for (skill of category.value; track skill.name) {
                        <app-skill-bar
                          [name]="skill.name"
                          [level]="skill.level"
                        ></app-skill-bar>
                      }
                    </div>
                  </mat-tab>
                }
              } @else {
                <mat-tab label="No Skills">
                  <p class="no-data">No skills data available.</p>
                </mat-tab>
              }
            </mat-tab-group>
          </mat-card>

          <mat-card class="traveller-card career-card">
            <div class="card-header traveller-card-header">
              <div class="header-left">
                <span class="card-icon">◈</span>
                <h3>CAREER HISTORY</h3>
              </div>
            </div>
            @if (career && career.length > 0) {
              <div class="career-list">
                @for (tour of career; track tour.tourNumber) {
                  <div class="career-tour">
                    <div class="tour-header">
                      <span class="tour-number">Tour {{ tour.tourNumber }}</span>
                      <span class="career-name">{{ tour.career }}</span>
                      <span class="rank">{{ tour.rank }}</span>
                    </div>
                    <div class="tour-details">
                      <span>{{ tour.termLength }} years</span>
                      @if (tour.wounds) {
                        <span class="wounds">Wounds: {{ tour.wounds }}</span>
                      }
                      @if (tour.discharges) {
                        <span class="discharge">{{ tour.discharges }}</span>
                      }
                    </div>
                  </div>
                }
              </div>
            } @else {
              <p class="no-data">No career history available.</p>
            }
          </mat-card>
        </div>
      }

      @if (!loading && !error && !character) {
        <div class="no-character">
          <mat-card class="traveller-card">
            <p>No Traveller character found.</p>
            <button mat-raised-button class="traveller-btn" routerLink="/dashboard">Back to Command</button>
          </mat-card>
        </div>
      }
    </div>
  `,
  styles: `
    /* Traveller Theme - Space Opera Aesthetics */
    .traveller-character-overview {
      padding: var(--spacing-lg);
      max-width: 1200px;
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

    .traveller-progress {
      --mat-progress-bar-active-color: #00ffcc;
      position: relative;
      z-index: 1;
      margin-bottom: var(--spacing-md);
    }

    .traveller-error {
      color: #ff6666;
      padding: var(--spacing-md);
      position: relative;
      z-index: 1;
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

    /* Traveller Cards */
    .traveller-card {
      background: linear-gradient(135deg, rgba(10,14,39,0.95) 0%, rgba(26,26,62,0.95) 100%) !important;
      border: 1px solid rgba(0,255,204,0.3) !important;
      border-radius: 4px !important;
      box-shadow: 0 4px 15px rgba(0,0,0,0.5), inset 0 0 10px rgba(0,255,204,0.05) !important;
      color: #e0e0e0 !important;
      position: relative;
      z-index: 1;
      overflow: hidden;
    }

    .traveller-card::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 2px;
      background: linear-gradient(90deg, transparent, #00ffcc, transparent);
    }

    .header-card {
      margin-bottom: var(--spacing-md);
    }

    .header {
      padding: var(--spacing-lg);
    }

    .character-info {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);
    }

    .character-badge {
      width: 50px;
      height: 50px;
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
      font-size: 1.5rem;
      color: #00ffcc;
    }

    .info-text h2 {
      margin: 0 0 var(--spacing-sm) 0;
      color: #00ffcc;
      text-shadow: 0 0 5px rgba(0,255,204,0.3);
    }

    .subtitle {
      margin: 0;
      color: #88aacc;
      font-size: 0.85rem;
    }

    .traveller-card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: var(--spacing-sm);
      padding: var(--spacing-md) var(--spacing-md) 0 var(--spacing-md);
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
    }

    .card-icon {
      color: #00ffcc;
      font-size: 1.2rem;
    }

    .traveller-card-header h3 {
      margin: 0;
      font-size: 0.85rem;
      color: #00ffcc;
      text-transform: uppercase;
      letter-spacing: 0.1em;
      font-weight: 600;
    }

    .abilities-card, .skills-card, .career-card {
      margin-bottom: var(--spacing-md);
    }

    .abilities-card h3, .skills-card h3, .career-card h3 {
      margin: var(--spacing-md) var(--spacing-md) var(--spacing-sm);
      color: #00ffcc;
      font-size: 0.85rem;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .abilities-grid {
      padding: 0 var(--spacing-md) var(--spacing-md);
    }

    .skills-list {
      padding: var(--spacing-md);
    }

    /* Traveller Tabs */
    .traveller-tabs {
      --mat-tab-header-active-label-text-color: #00ffcc;
      --mat-tab-header-inactive-label-text-color: #88aacc;
      --mat-tab-header-active-indicator-color: #00ffcc;
      --mat-tab-header-divider-color: rgba(0,255,204,0.2);
    }

    .career-list {
      padding: 0 var(--spacing-md) var(--spacing-md);
    }

    .career-tour {
      padding: var(--spacing-sm) 0;
      border-bottom: 1px solid rgba(0,255,204,0.1);
    }

    .career-tour:last-child {
      border-bottom: none;
    }

    .tour-header {
      display: flex;
      gap: var(--spacing-md);
      align-items: center;
      margin-bottom: var(--spacing-xs);
    }

    .tour-number {
      font-weight: 700;
      color: #00ffcc;
      font-size: 0.85rem;
    }

    .career-name {
      flex: 1;
      color: #e0e0e0;
      font-weight: 500;
    }

    .rank {
      color: #ffaa00;
      font-weight: 600;
      font-size: 0.85rem;
      text-shadow: 0 0 5px rgba(255,170,0,0.3);
    }

    .tour-details {
      display: flex;
      gap: var(--spacing-md);
      font-size: 0.8rem;
      color: #6688aa;
    }

    .wounds {
      color: #ff6666;
    }

    .discharge {
      color: #00ff88;
    }

    .no-data {
      padding: var(--spacing-md);
      color: #6688aa;
      text-align: center;
    }

    .no-character {
      text-align: center;
      padding: var(--spacing-xl) var(--spacing-md);
      position: relative;
      z-index: 1;
    }
  `,
})
export class CharacterOverviewComponent implements OnInit {
  loading = true;
  error: string | null = null;

  character: CharacterStats | null = null;
  abilities: CharacterAbilities | null = null;
  skills: CharacterSkill[] = [];
  career: CharacterCareer[] = [];

  abilitiesList: Array<{ key: string; name: string; score: number }> = [];
  skillsByCategory: Record<string, CharacterSkill[]> = {};
  skillsCategories: Array<{ key: string; value: CharacterSkill[] }> = [];

  constructor(
    private corpService: CorporationService
  ) {}

  ngOnInit(): void {
    this.loadCharacter();
  }

  loadCharacter(): void {
    this.loading = true;
    this.error = null;
    const charId = this.corpService.characterId;

    // Parallelize all 4 API calls
    forkJoin({
      stats: this.corpService.getCharacterStats(charId),
      abilities: this.corpService.getCharacterAbilities(charId),
      skills: this.corpService.getCharacterSkills(charId),
      career: this.corpService.getCharacterCareer(charId),
    }).subscribe({
      next: ({ stats, abilities, skills, career }) => {
        this.character = stats;
        this.abilities = abilities;
        this.abilitiesList = [
          { key: 'DEX', name: 'Dexterity', score: abilities.dex },
          { key: 'ST', name: 'Strength', score: abilities.st },
          { key: 'END', name: 'Endurance', score: abilities.end },
          { key: 'INT', name: 'Intelligence', score: abilities.int },
          { key: 'EDU', name: 'Education', score: abilities.edu },
          { key: 'SOC', name: 'Social Standing', score: abilities.soc },
          { key: 'APP', name: 'Appearance', score: abilities.app },
        ];
        this.skills = skills;
        this.skillsByCategory = {};
        skills.forEach(skill => {
          const category = skill.category || 'Other';
          if (!this.skillsByCategory[category]) {
            this.skillsByCategory[category] = [];
          }
          this.skillsByCategory[category].push(skill);
        });
        this.skillsCategories = Object.entries(this.skillsByCategory).map(([key, value]) => ({ key, value }));
        this.career = career;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Failed to load character data: ' + err.message;
        this.loading = false;
      },
    });
  }
}

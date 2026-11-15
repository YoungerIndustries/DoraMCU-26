import { Component, Input } from '@angular/core';
import { getAbilityColor, getAbilityModifier } from '../../core/models/traveller-character';

@Component({
  selector: 'app-ability-bar',
  standalone: true,
  template: `
    <div class="ability-bar" [style.border-left-color]="color">
      <div class="ability-header">
        <span class="ability-key">{{ key }}</span>
        <span class="ability-name">{{ name }}</span>
        <span class="ability-modifier" [style.color]="color">
          {{ modifier >= 0 ? '+' : '' }}{{ modifier }}
        </span>
      </div>
      <div class="bar-container">
        <div class="bar-fill" [style.width.%]="(score / 20) * 100" [style.background]="color"></div>
      </div>
      <div class="ability-footer">
        <span class="ability-score">{{ score }}/20</span>
      </div>
    </div>
  `,
  styles: `
    .ability-bar {
      border-left: 4px solid var(--text-secondary);
      padding: 10px 14px;
      margin-bottom: 10px;
      background: var(--bg-elevated);
      border-radius: var(--radius-sm);
      transition: background var(--transition-fast);
    }
    .ability-bar:hover {
      background: var(--bg-card-hover);
    }
    .ability-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 6px;
    }
    .ability-key {
      font-weight: 700;
      font-size: 0.95rem;
      min-width: 32px;
      color: var(--text-primary);
      letter-spacing: 0.05em;
    }
    .ability-name {
      flex: 1;
      font-size: 0.8rem;
      color: var(--text-secondary);
      margin-left: 10px;
    }
    .ability-modifier {
      font-weight: 700;
      font-size: 1rem;
      min-width: 32px;
      text-align: right;
    }
    .bar-container {
      height: 8px;
      background: var(--bg-input);
      border-radius: 4px;
      overflow: hidden;
      margin-bottom: 6px;
    }
    .bar-fill {
      height: 100%;
      border-radius: 4px;
      transition: width 0.4s ease;
    }
    .ability-footer {
      text-align: right;
    }
    .ability-score {
      font-size: 0.7rem;
      color: var(--text-hint);
      font-weight: 500;
    }
  `,
})
export class AbilityBarComponent {
  @Input() key = '';
  @Input() name = '';
  @Input() score = 10;

  get color(): string {
    return getAbilityColor(this.score);
  }

  get modifier(): number {
    return getAbilityModifier(this.score);
  }
}

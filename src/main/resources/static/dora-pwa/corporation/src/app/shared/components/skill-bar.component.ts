import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-skill-bar',
  standalone: true,
  template: `
    <div class="skill-bar">
      <span class="skill-name">{{ name }}</span>
      <div class="bar-container">
        <div class="bar-fill" [style.width.%]="level * 10"></div>
      </div>
      <span class="skill-level">{{ level }}/10</span>
    </div>
  `,
  styles: `
    .skill-bar {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 6px;
    }
    .skill-name {
      min-width: 110px;
      font-size: 0.8rem;
      color: var(--text-secondary);
      font-weight: 500;
    }
    .bar-container {
      flex: 1;
      height: 10px;
      background: var(--bg-input);
      border-radius: 5px;
      overflow: hidden;
    }
    .bar-fill {
      height: 100%;
      background: linear-gradient(90deg, var(--primary-dark), var(--mat-primary));
      border-radius: 5px;
      transition: width 0.4s ease;
      box-shadow: 0 0 6px rgba(100, 181, 246, 0.3);
    }
    .skill-level {
      min-width: 42px;
      text-align: right;
      font-size: 0.7rem;
      color: var(--text-hint);
      font-weight: 600;
    }
  `,
})
export class SkillBarComponent {
  @Input() name = '';
  @Input() level = 1;
}

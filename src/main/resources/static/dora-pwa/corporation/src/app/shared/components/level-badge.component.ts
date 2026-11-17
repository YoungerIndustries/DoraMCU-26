import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-level-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span class="badge" [ngClass]="'level-' + level.toLowerCase()">
      {{ level }}
    </span>
  `,
  styles: `
    .badge {
      display: inline-flex;
      align-items: center;
      padding: 3px 10px;
      border-radius: 20px;
      font-size: 0.65rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .level-junior { background: var(--info-bg); color: var(--info); }
    .level-senior { background: var(--success-bg); color: var(--success); }
    .level-lead { background: var(--warning-bg); color: var(--warning); }
    .level-director { background: rgba(142, 36, 170, 0.15); color: #ce93d8; }
  `,
})
export class LevelBadgeComponent {
  @Input() level = 'JUNIOR';
}

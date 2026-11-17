import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { CorporationEvent } from '../../core/models/corporation-state';

@Component({
  selector: 'app-event-feed',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="event-feed">
      <h3>Recent Events</h3>
      @if (events.length === 0) {
        <p class="empty">No recent events</p>
      } @else {
        @for (event of events; track event.timestamp) {
          <div class="event-item" [ngClass]="'severity-' + event.severity.toLowerCase()">
            <span class="event-type">{{ event.eventType }}</span>
            <span class="event-message">{{ event.message }}</span>
            <span class="event-time">{{ event.timestamp | date:'short' }}</span>
          </div>
        }
      }
    </div>
  `,
  styles: `
    .event-feed {
      max-height: 300px;
      overflow-y: auto;
    }
    h3 {
      margin: 0 0 10px 0;
      font-size: 0.85rem;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      font-weight: 500;
    }
    .empty {
      color: var(--text-hint);
      font-style: italic;
      text-align: center;
      padding: 16px;
    }
    .event-item {
      display: flex;
      flex-direction: column;
      padding: 10px 12px;
      margin-bottom: 6px;
      border-radius: var(--radius-sm);
      background: var(--bg-elevated);
      border-left: 3px solid var(--text-hint);
      transition: background var(--transition-fast);
    }
    .event-item:hover {
      background: var(--bg-card-hover);
    }
    .severity-critical { border-left-color: var(--error); background: var(--error-bg); }
    .severity-warning { border-left-color: var(--warning); background: var(--warning-bg); }
    .severity-info { border-left-color: var(--info); background: var(--info-bg); }
    .severity-success { border-left-color: var(--success); background: var(--success-bg); }
    .event-type {
      font-size: 0.65rem;
      text-transform: uppercase;
      color: var(--text-hint);
      font-weight: 600;
      letter-spacing: 0.05em;
    }
    .event-message {
      font-size: 0.85rem;
      margin: 3px 0;
      color: var(--text-primary);
    }
    .event-time {
      font-size: 0.65rem;
      color: var(--text-hint);
    }
  `,
})
export class EventFeedComponent {
  @Input() events: CorporationEvent[] = [];
}

import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { CorporationService } from '../../core/services/corporation.service';
import { Scene3DComponent } from '../../shared/components/scene-3d.component';
import { AiAction } from '../../core/models/corporation-state';

@Component({
  selector: 'app-ai-actions',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatFormFieldModule,
    Scene3DComponent,
  ],
  template: `
    <div class="ai-actions-page">
      <div class="header">
        <h1>AI Assistant Actions</h1>
        <div class="controls">
          <mat-form-field appearance="outline">
            <mat-label>Filter by type</mat-label>
            <mat-select [(value)]="filterType" (selectionChange)="loadActions()">
              <mat-option value="">All Types</mat-option>
              <mat-option value="PROCESS_CONTRACT">Process Contract</mat-option>
              <mat-option value="ACCEPT_CONTRACT">Accept Contract</mat-option>
              <mat-option value="HIRE_CREW">Hire Crew</mat-option>
              <mat-option value="FIRE_CREW">Fire Crew</mat-option>
              <mat-option value="SKIP_TURN">Skip Turn</mat-option>
            </mat-select>
          </mat-form-field>
          <button mat-raised-button color="primary" (click)="triggerAiTurn()">
            <mat-icon>play_arrow</mat-icon> Trigger AI Turn
          </button>
        </div>
      </div>

      <!-- AI Actions Timeline 3D -->
      <mat-card class="ai-timeline-card">
        <div class="card-header">
          <h3>AI Actions Timeline</h3>
          <div class="timeline-legend">
            <span class="legend-item"><span class="legend-dot success"></span>Success</span>
            <span class="legend-item"><span class="legend-dot failed"></span>Failed</span>
            <span class="legend-item"><span class="legend-dot skipped"></span>Skipped</span>
          </div>
        </div>
        <div class="ai-timeline-container">
          <app-scene-3d
            #aiScene
            (objectClick)="onAiObjectClick($event)"
            (objectHover)="onAiObjectHover($event)"
          ></app-scene-3d>
        </div>
        @if (hoveredAi) {
          <div class="hover-info">
            <span class="hover-name">{{ hoveredAi.name }}</span>
            <span class="hover-detail">{{ hoveredAi.detail }}</span>
          </div>
        }
      </mat-card>

      @if (actions.length === 0) {
        <p class="empty">No AI actions recorded yet</p>
      } @else {
        @for (action of actions; track action.id) {
          <mat-card class="actions-card">
            <div class="action-header">
              <span class="action-type" [ngClass]="action.actionType.toLowerCase()">
                {{ action.actionType }}
              </span>
              <span class="action-result" [ngClass]="action.result.toLowerCase()">
                {{ action.result }}
              </span>
              <span class="action-time">{{ action.timestamp | date:'medium' }}</span>
            </div>
            <p class="action-description">{{ action.description }}</p>
            @if (action.personalitySnapshot) {
              <details class="personality-details">
                <summary>Personality Snapshot</summary>
                <pre>{{ action.personalitySnapshot }}</pre>
              </details>
            }
          </mat-card>
        }
      }
    </div>
  `,
  styles: `
    .ai-actions-page {
      padding: var(--spacing-lg);
      max-width: 1400px;
      margin: 0 auto;
    }
    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-lg);
      flex-wrap: wrap;
      gap: var(--spacing-sm);
    }
    h1 {
      margin: 0;
    }
    .controls {
      display: flex;
      gap: var(--spacing-sm);
      align-items: center;
    }
    .actions-card {
      padding: var(--spacing-md);
      margin-bottom: var(--spacing-sm);
      transition: background var(--transition-fast);
    }
    .actions-card:hover {
      background: var(--bg-card-hover) !important;
    }
    .action-header {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      margin-bottom: var(--spacing-xs);
      flex-wrap: wrap;
    }
    .action-type {
      display: inline-flex;
      align-items: center;
      padding: 3px 10px;
      border-radius: 20px;
      font-size: 0.65rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.03em;
      background: var(--info-bg);
      color: var(--info);
    }
    .action-type.accept_contract { background: var(--success-bg); color: var(--success); }
    .action-type.hire_crew { background: rgba(106, 27, 154, 0.15); color: #ce93d8; }
    .action-type.fire_crew { background: var(--error-bg); color: var(--error); }
    .action-type.skip_turn { background: var(--bg-elevated); color: var(--text-secondary); }
    .action-result {
      font-size: 0.65rem;
      padding: 3px 10px;
      border-radius: 20px;
      font-weight: 600;
    }
    .action-result.success { background: var(--success-bg); color: var(--success); }
    .action-result.failed { background: var(--error-bg); color: var(--error); }
    .action-result.skipped { background: var(--bg-elevated); color: var(--text-secondary); }
    .action-time {
      margin-left: auto;
      font-size: 0.7rem;
      color: var(--text-hint);
    }
    .action-description {
      margin: var(--spacing-xs) 0;
      font-size: 0.85rem;
      color: var(--text-primary);
    }
    .personality-details {
      margin-top: var(--spacing-sm);
    }
    .personality-details summary {
      cursor: pointer;
      font-size: 0.75rem;
      color: var(--text-secondary);
      font-weight: 500;
    }
    .personality-details pre {
      background: var(--bg-input);
      padding: var(--spacing-sm);
      border-radius: var(--radius-sm);
      font-size: 0.75rem;
      overflow-x: auto;
      margin-top: var(--spacing-xs);
    }
    /* AI Timeline 3D */
    .ai-timeline-card {
      margin-bottom: var(--spacing-md);
      overflow: hidden;
    }
    .ai-timeline-card .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-sm);
      padding: 0 var(--spacing-sm);
    }
    .ai-timeline-card .card-header h3 {
      margin: 0;
      font-size: 0.85rem;
      color: var(--text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .timeline-legend {
      display: flex;
      gap: 12px;
    }
    .timeline-legend .legend-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 0.7rem;
      color: var(--text-secondary);
    }
    .timeline-legend .legend-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      display: inline-block;
    }
    .legend-dot.success { background: #44ff88; }
    .legend-dot.failed { background: #ff4444; }
    .legend-dot.skipped { background: #888888; }
    .ai-timeline-container {
      height: 250px;
      width: 100%;
    }
    .hover-info {
      position: absolute;
      bottom: 10px;
      left: 10px;
      background: rgba(0, 0, 20, 0.85);
      border: 1px solid rgba(68, 136, 255, 0.3);
      border-radius: 6px;
      padding: 8px 12px;
      display: flex;
      flex-direction: column;
      gap: 2px;
      z-index: 10;
    }
    .hover-name {
      font-size: 0.85rem;
      font-weight: 600;
      color: #ffffff;
    }
    .hover-detail {
      font-size: 0.7rem;
      color: #8888cc;
    }

    .empty {
      color: var(--text-hint);
      font-style: italic;
      text-align: center;
      padding: var(--spacing-xl);
    }
  `,
})
export class AiActionsComponent implements OnInit, AfterViewInit {
  actions: AiAction[] = [];
  filterType = '';
  hoveredAi: { name: string; detail: string } | null = null;

  @ViewChild('aiScene') aiScene?: Scene3DComponent;

  constructor(private corpService: CorporationService) {}

  ngOnInit(): void {
    this.loadActions();
  }

  ngAfterViewInit(): void {
    // Will be populated after loadActions completes
  }

  loadActions(): void {
    this.corpService.getAiActions(50).subscribe({
      next: (actions) => {
        this.actions = this.filterType
          ? actions.filter((a) => a.actionType === this.filterType)
          : actions;
        // Update 3D visualization
        if (this.aiScene) {
          this._populateAiTimeline(this.actions);
        }
      },
      error: (err) => console.error('Failed to load AI actions', err),
    });
  }

  triggerAiTurn(): void {
    this.corpService.triggerAiTurn().subscribe({
      next: () => {
        this.loadActions();
        this.corpService.loadOfficeState();
      },
      error: (err) => console.error('Failed to trigger AI turn', err),
    });
  }

  onAiObjectClick(event: { id: string; userData?: Record<string, any> }): void {
    console.log('AI action clicked:', event);
  }

  onAiObjectHover(event: { id: string | null; userData?: Record<string, any> }): void {
    if (event.id && event.userData) {
      this.hoveredAi = {
        name: event.userData['name'] || event.id,
        detail: event.userData['detail'] || '',
      };
    } else {
      this.hoveredAi = null;
    }
  }

  private _populateAiTimeline(actions: AiAction[]): void {
    if (!this.aiScene) return;
    const scene = this.aiScene as any;
    scene.clearAll();

    // Timeline axis (long box)
    scene.addBox(
      'timeline-axis',
      { x: 0, y: -1, z: 0 },
      { x: 20, y: 0.1, z: 0.1 },
      0x333366,
      { name: 'Timeline', detail: 'AI action history', interactive: false }
    );

    // Actions positioned along timeline
    actions.forEach((action: AiAction, i: number) => {
      const x = -10 + (i / Math.max(actions.length, 1)) * 20;
      const z = 0;

      // Color by result
      let color: number;
      if (action.result === 'SUCCESS') {
        color = 0x44ff88;
      } else if (action.result === 'FAILED') {
        color = 0xff4444;
      } else {
        color = 0x888888;
      }

      scene.addSphere(
        `ai-${action.id}`,
        { x, y: 0, z },
        0.3,
        color,
        {
          name: action.actionType,
          detail: `${action.result} | ${action.description} | ${new Date(action.timestamp).toLocaleString()}`,
          interactive: true,
        }
      );
    });
  }
}

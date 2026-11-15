import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { MatSidenavModule, MatSidenav } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { CommonModule } from '@angular/common';
import { CorporationService } from '../../core/services/corporation.service';
import { OfflineBannerComponent } from '../../shared/components/offline-banner.component';
import { Scene3DComponent } from '../../shared/components/scene-3d.component';
import { EventFeedComponent } from '../../shared/components/event-feed.component';

@Component({
  selector: 'app-desktop-app',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    MatSidenavModule,
    MatListModule,
    MatIconModule,
    MatToolbarModule,
    MatButtonModule,
    MatTooltipModule,
    MatCardModule,
    MatProgressBarModule,
    CommonModule,
    OfflineBannerComponent,
    Scene3DComponent,
    EventFeedComponent,
  ],
  template: `
    <app-offline-banner></app-offline-banner>
    <div class="game-shell">
      <!-- Game State Bar (Top) -->
      <div class="game-state-bar">
        <div class="state-bar-left">
          <mat-icon class="corp-icon">business</mat-icon>
          <span class="corp-name">DoraMCU Corporation</span>
          <span class="separator">|</span>
          <span class="page-title">{{ pageTitle }}</span>
        </div>
        <div class="state-bar-center">
          <div class="stat-item">
            <span class="stat-label">Credits</span>
            <span class="stat-value">{{ corpService.state()?.creditBalance ?? 0 | number }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">Contracts</span>
            <span class="stat-value">{{ corpService.state()?.activeContracts?.length ?? 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">Crew</span>
            <span class="stat-value">{{ corpService.state()?.activeHires?.length ?? 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">Role</span>
            <span class="stat-value">{{ corpService.state()?.currentRole ?? 'None' }}</span>
          </div>
        </div>
        <div class="state-bar-right">
          <button
            *ngIf="activeCharacterName"
            mat-button
            routerLink="/"
            matTooltip="Change character"
            class="character-selector"
          >
            <mat-icon>person</mat-icon>
            <span>{{ activeCharacterName }}</span>
          </button>
          <button mat-icon-button matTooltip="Settings">
            <mat-icon>settings</mat-icon>
          </button>
        </div>
      </div>

      <!-- Main Game Area -->
      <div class="game-main-area">
        <!-- Left Panel (Navigation + Info) -->
        <div class="game-panel game-panel-left">
          <div class="panel-header">
            <h3 class="panel-title">Navigation</h3>
          </div>
          <mat-nav-list class="nav-list">
            <a mat-list-item routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">
              <mat-icon matListItemIcon>app_registration</mat-icon>
              <span matListItemTitle>Select Character</span>
            </a>
            <a mat-list-item routerLink="/dashboard" routerLinkActive="active">
              <mat-icon matListItemIcon>dashboard</mat-icon>
              <span matListItemTitle>Dashboard</span>
            </a>
            <a mat-list-item routerLink="/contracts" routerLinkActive="active">
              <mat-icon matListItemIcon>assignment</mat-icon>
              <span matListItemTitle>Contracts</span>
            </a>
            <a mat-list-item routerLink="/crew" routerLinkActive="active">
              <mat-icon matListItemIcon>groups</mat-icon>
              <span matListItemTitle>Crew</span>
            </a>
            <a mat-list-item routerLink="/finance" routerLinkActive="active">
              <mat-icon matListItemIcon>account_balance_wallet</mat-icon>
              <span matListItemTitle>Finance</span>
            </a>
            <div class="nav-divider"></div>
            <a mat-list-item routerLink="/ai-actions" routerLinkActive="active">
              <mat-icon matListItemIcon>smart_toy</mat-icon>
              <span matListItemTitle>AI Actions</span>
            </a>
            <a mat-list-item routerLink="/role" routerLinkActive="active">
              <mat-icon matListItemIcon>swap_horiz</mat-icon>
              <span matListItemTitle>Role Switch</span>
            </a>
            <div class="nav-divider"></div>
            <a mat-list-item routerLink="/character" routerLinkActive="active">
              <mat-icon matListItemIcon>person</mat-icon>
              <span matListItemTitle>Character</span>
            </a>
          </mat-nav-list>

          <!-- Context-sensitive info panel -->
          <div class="panel-header">
            <h3 class="panel-title">Info</h3>
          </div>
          <div class="panel-content info-panel">
            <mat-card class="info-card">
              <mat-card-content>
                <div class="info-item">
                  <span class="info-label">Office Level</span>
                  <span class="info-value">{{ corpService.state()?.officeBuildingLevel ?? 1 }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">AI Assistant</span>
                  <span class="info-value" [class.active]="corpService.state()?.aiAssistantActive">
                    {{ corpService.state()?.aiAssistantActive ? 'Active' : 'Inactive' }}
                  </span>
                </div>
                <div class="info-item">
                  <span class="info-label">Insolvency</span>
                  <span class="info-value" [class.danger]="corpService.state()?.isInsolvent">
                    {{ corpService.state()?.isInsolvent ? '⚠️ Risk' : '✓ Safe' }}
                  </span>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </div>

        <!-- Center Area (3D View + Content) -->
        <div class="game-center-area">
          <!-- 3D View Container -->
          <div class="game-3d-view">
            <app-scene-3d #scene3d></app-scene-3d>
          </div>

          <!-- Content Area (overlaid on 3D) -->
          <div class="content-overlay">
            <router-outlet></router-outlet>
          </div>
        </div>

        <!-- Right Panel (Actions) -->
        <div class="game-panel game-panel-right">
          <div class="panel-header">
            <h3 class="panel-title">Actions</h3>
          </div>
          <div class="panel-content action-panel">
            <button mat-raised-button color="primary" routerLink="/contracts" class="action-btn">
              <mat-icon>assignment</mat-icon>
              View Contracts
            </button>
            <button mat-raised-button color="primary" routerLink="/crew" class="action-btn">
              <mat-icon>groups</mat-icon>
              Manage Crew
            </button>
            <button mat-raised-button color="accent" routerLink="/ai-actions" class="action-btn">
              <mat-icon>smart_toy</mat-icon>
              AI Actions
            </button>
            <button mat-raised-button routerLink="/finance" class="action-btn">
              <mat-icon>account_balance_wallet</mat-icon>
              Finance
            </button>
          </div>
        </div>
      </div>

      <!-- Narrative/Event Feed (Bottom) -->
      <div class="game-panel game-panel-narrative">
        <div class="panel-header">
          <h3 class="panel-title">Event Feed</h3>
        </div>
        <div class="panel-content narrative-content">
          <app-event-feed></app-event-feed>
        </div>
      </div>
    </div>
  `,
  styles: `
    :host {
      display: block;
      height: 100vh;
      width: 100vw;
      background: #000011;
      color: #e0e0e0;
      font-family: 'Roboto', 'Segoe UI', sans-serif;
      overflow: hidden;
    }

    .game-shell {
      display: flex;
      flex-direction: column;
      height: 100vh;
      width: 100vw;
      background: #000011;
    }

    /* Game State Bar (Top) */
    .game-state-bar {
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 48px;
      min-height: 48px;
      background: linear-gradient(180deg, #0a0a2e 0%, #050520 100%);
      border-bottom: 1px solid #1a1a4e;
      padding: 0 16px;
      z-index: 100;
    }
    .state-bar-left {
      display: flex;
      align-items: center;
      gap: 10px;
    }
    .corp-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
      color: #4488ff;
    }
    .corp-name {
      font-size: 0.9rem;
      font-weight: 700;
      color: #ffffff;
      letter-spacing: 0.05em;
    }
    .separator {
      color: #333366;
      margin: 0 4px;
    }
    .page-title {
      font-size: 0.85rem;
      color: #8888cc;
    }
    .state-bar-center {
      display: flex;
      align-items: center;
      gap: 24px;
    }
    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 2px;
    }
    .stat-label {
      font-size: 0.6rem;
      text-transform: uppercase;
      color: #6666aa;
      letter-spacing: 0.1em;
    }
    .stat-value {
      font-size: 0.9rem;
      font-weight: 700;
      color: #44ff88;
    }
    .state-bar-right {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .character-selector {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #8888cc;
      padding: 6px 12px;
      border-radius: 4px;
      transition: background 0.2s, color 0.2s;
      font-size: 0.85rem;
    }
    .character-selector mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: #4488ff;
    }
    .character-selector:hover {
      background: rgba(68, 136, 255, 0.15);
      color: #ffffff;
    }

    /* Main Game Area */
    .game-main-area {
      display: flex;
      flex: 1;
      overflow: hidden;
    }

    /* Left Panel */
    .game-panel {
      background: linear-gradient(180deg, #0a0a2e 0%, #050520 100%);
      border-right: 1px solid #1a1a4e;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
    .game-panel-left {
      width: 240px;
      min-width: 240px;
      border-right: 1px solid #1a1a4e;
    }
    .game-panel-right {
      width: 220px;
      min-width: 220px;
      border-right: none;
      border-left: 1px solid #1a1a4e;
    }
    .panel-header {
      padding: 10px 16px;
      border-bottom: 1px solid #1a1a4e;
      background: rgba(10, 10, 46, 0.8);
    }
    .panel-title {
      font-size: 0.75rem;
      font-weight: 700;
      text-transform: uppercase;
      color: #4488ff;
      letter-spacing: 0.1em;
      margin: 0;
    }
    .panel-content {
      flex: 1;
      overflow-y: auto;
      padding: 8px;
    }

    /* Navigation list */
    .nav-list {
      padding: 4px 0;
    }
    .nav-list a {
      color: #8888cc !important;
    }
    .nav-list a.active {
      background: rgba(68, 136, 255, 0.15) !important;
      border-left: 3px solid #4488ff;
      color: #ffffff !important;
    }
    .nav-list a.active mat-icon {
      color: #4488ff;
    }
    .nav-list a:hover:not(.active) {
      background: rgba(68, 136, 255, 0.08) !important;
    }
    .nav-divider {
      height: 1px;
      background: #1a1a4e;
      margin: 6px 16px;
    }

    /* Info panel */
    .info-panel {
      padding: 8px;
    }
    .info-card {
      background: rgba(10, 10, 46, 0.6) !important;
      border: 1px solid #1a1a4e !important;
      border-radius: 6px !important;
    }
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 6px 0;
      border-bottom: 1px solid rgba(26, 26, 78, 0.5);
    }
    .info-item:last-child {
      border-bottom: none;
    }
    .info-label {
      font-size: 0.75rem;
      color: #6666aa;
    }
    .info-value {
      font-size: 0.8rem;
      font-weight: 600;
      color: #44ff88;
    }
    .info-value.active {
      color: #44ff88;
    }
    .info-value.danger {
      color: #ff4444;
    }

    /* Center Area */
    .game-center-area {
      flex: 1;
      position: relative;
      display: flex;
      flex-direction: column;
      overflow: hidden;
    }
    .game-3d-view {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 0;
    }
    .content-overlay {
      position: relative;
      z-index: 10;
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      pointer-events: none;
    }
    .content-overlay ::ng-deep * {
      pointer-events: auto;
    }

    /* Right panel actions */
    .action-panel {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    .action-btn {
      width: 100%;
      justify-content: flex-start !important;
      margin-bottom: 4px;
      background: rgba(10, 10, 46, 0.8) !important;
      border: 1px solid #1a1a4e !important;
      color: #ccccff !important;
      font-size: 0.8rem !important;
    }
    .action-btn mat-icon {
      margin-right: 8px;
      font-size: 18px;
      width: 18px;
      height: 18px;
    }
    .action-btn:hover {
      background: rgba(68, 136, 255, 0.2) !important;
    }

    /* Narrative panel (Bottom) */
    .game-panel-narrative {
      height: 150px;
      min-height: 100px;
      max-height: 200px;
      border-top: 1px solid #1a1a4e;
      background: linear-gradient(180deg, #050520 0%, #0a0a2e 100%);
    }
    .narrative-content {
      padding: 4px 8px;
    }
  `,
})
export class DesktopAppComponent implements OnInit, OnDestroy {
  activeCharacterName: string | null = null;
  pageTitle = 'DoraMCU Corporation';

  @ViewChild('drawer') drawer?: MatSidenav;

  constructor(
    private corpService: CorporationService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.corpService.loadOfficeState();
    this.corpService.loadFinancialReport();
    this.corpService.subscribeToEvents();

    // Listen for route changes to update character name and page title
    this.router.events.subscribe(() => {
      this.updateCharacterName();
      this.updatePageTitle();
    });
  }

  ngOnDestroy(): void {
    // Cleanup if needed
  }

  updateCharacterName(): void {
    const charId = localStorage.getItem('currentCharacterId');
    if (charId) {
      const state = this.corpService.state();
      if (state) {
        this.activeCharacterName = state.characterName || null;
      } else {
        this.activeCharacterName = null;
      }
    } else {
      this.activeCharacterName = null;
    }
  }

  updatePageTitle(): void {
    const url = this.router.url;
    if (url === '/' || url === '') {
      this.pageTitle = 'Select Character';
    } else if (url === '/dashboard') {
      this.pageTitle = 'Dashboard';
    } else if (url === '/contracts') {
      this.pageTitle = 'Contracts';
    } else if (url === '/crew') {
      this.pageTitle = 'Crew';
    } else if (url === '/finance') {
      this.pageTitle = 'Finance';
    } else if (url === '/ai-actions') {
      this.pageTitle = 'AI Actions';
    } else if (url === '/role') {
      this.pageTitle = 'Role Switch';
    } else if (url === '/character') {
      this.pageTitle = 'Character';
    } else {
      this.pageTitle = 'DoraMCU Corporation';
    }
  }
}

import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { MatSidenavModule, MatSidenav } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatCardModule } from '@angular/material/card';
import { MediaMatcher } from '@angular/cdk/layout';
import { CorporationService } from '../../core/services/corporation.service';
import { OfflineBannerComponent } from '../../shared/components/offline-banner.component';
import { Scene3DComponent } from '../../shared/components/scene-3d.component';
import { EventFeedComponent } from '../../shared/components/event-feed.component';

@Component({
  selector: 'app-mobile-app',
  standalone: true,
  imports: [
    CommonModule,
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
    OfflineBannerComponent,
    Scene3DComponent,
    EventFeedComponent,
  ],
  template: `
    <app-offline-banner></app-offline-banner>
    <div class="game-shell">
      <!-- Game State Bar (Top) -->
      <div class="game-state-bar">
        <button mat-icon-button (click)="drawer?.toggle()" aria-label="Toggle navigation">
          <mat-icon>menu</mat-icon>
        </button>
        <div class="state-bar-center">
          <span class="page-title">{{ pageTitle }}</span>
          <div class="stat-item">
            <span class="stat-value">{{ corpService.state()?.creditBalance ?? 0 | number }}</span>
          </div>
        </div>
        <div class="state-bar-right">
          <button
            *ngIf="activeCharacterName"
            mat-icon-button
            routerLink="/"
            matTooltip="Change character"
          >
            <mat-icon>person</mat-icon>
          </button>
        </div>
      </div>

      <!-- Navigation Drawer -->
      <mat-sidenav #drawer mode="over" [opened]="false" class="sidenav">
        <div class="sidenav-header">
          <mat-icon class="header-icon">business</mat-icon>
          <div class="header-text">
            <span class="header-title">DoraMCU</span>
            <span class="header-subtitle">Corporation</span>
          </div>
        </div>
        <mat-nav-list class="nav-list">
          <a mat-list-item routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" (click)="drawer.close()">
            <mat-icon matListItemIcon>app_registration</mat-icon>
            <span matListItemTitle>Select Character</span>
          </a>
          <a mat-list-item routerLink="/dashboard" routerLinkActive="active" (click)="drawer.close()">
            <mat-icon matListItemIcon>dashboard</mat-icon>
            <span matListItemTitle>Dashboard</span>
          </a>
          <a mat-list-item routerLink="/contracts" routerLinkActive="active" (click)="drawer.close()">
            <mat-icon matListItemIcon>assignment</mat-icon>
            <span matListItemTitle>Contracts</span>
          </a>
          <a mat-list-item routerLink="/crew" routerLinkActive="active" (click)="drawer.close()">
            <mat-icon matListItemIcon>groups</mat-icon>
            <span matListItemTitle>Crew</span>
          </a>
          <a mat-list-item routerLink="/finance" routerLinkActive="active" (click)="drawer.close()">
            <mat-icon matListItemIcon>account_balance_wallet</mat-icon>
            <span matListItemTitle>Finance</span>
          </a>
          <div class="nav-divider"></div>
          <a mat-list-item routerLink="/ai-actions" routerLinkActive="active" (click)="drawer.close()">
            <mat-icon matListItemIcon>smart_toy</mat-icon>
            <span matListItemTitle>AI Actions</span>
          </a>
          <a mat-list-item routerLink="/role" routerLinkActive="active" (click)="drawer.close()">
            <mat-icon matListItemIcon>swap_horiz</mat-icon>
            <span matListItemTitle>Role Switch</span>
          </a>
          <div class="nav-divider"></div>
          <a mat-list-item routerLink="/character" routerLinkActive="active" (click)="drawer.close()">
            <mat-icon matListItemIcon>person</mat-icon>
            <span matListItemTitle>Character</span>
          </a>
        </mat-nav-list>
        <!-- Info panel in drawer -->
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
            </mat-card-content>
          </mat-card>
        </div>
      </mat-sidenav>

      <!-- Main Game Area -->
      <div class="game-main-area">
        <!-- 3D View Background -->
        <div class="game-3d-view">
          <app-scene-3d></app-scene-3d>
        </div>

        <!-- Content Area (overlaid on 3D) -->
        <div class="content-overlay">
          <router-outlet></router-outlet>
        </div>
      </div>

      <!-- Event Feed (Bottom) -->
      <div class="game-panel-narrative">
        <app-event-feed></app-event-feed>
      </div>

      <!-- Bottom navigation bar -->
      <nav class="bottom-nav">
        <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" class="nav-item">
          <mat-icon>home</mat-icon>
          <span>Home</span>
        </a>
        <a routerLink="/contracts" routerLinkActive="active" class="nav-item">
          <mat-icon>assignment</mat-icon>
          <span>Contracts</span>
        </a>
        <a routerLink="/crew" routerLinkActive="active" class="nav-item">
          <mat-icon>groups</mat-icon>
          <span>Crew</span>
        </a>
        <a routerLink="/finance" routerLinkActive="active" class="nav-item">
          <mat-icon>account_balance_wallet</mat-icon>
          <span>Finance</span>
        </a>
        <a routerLink="/character" routerLinkActive="active" class="nav-item">
          <mat-icon>person</mat-icon>
          <span>Character</span>
        </a>
      </nav>
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
      padding: 0 8px;
      z-index: 100;
    }
    .state-bar-center {
      display: flex;
      align-items: center;
      gap: 12px;
      flex: 1;
    }
    .page-title {
      font-size: 0.9rem;
      font-weight: 500;
      color: #8888cc;
    }
    .stat-item {
      display: flex;
      align-items: center;
    }
    .stat-value {
      font-size: 0.85rem;
      font-weight: 700;
      color: #44ff88;
    }
    .state-bar-right {
      display: flex;
      align-items: center;
    }

    /* Navigation Drawer */
    .sidenav {
      position: fixed;
      top: 48px;
      left: 0;
      bottom: 0;
      width: 280px;
      background: linear-gradient(180deg, #0a0a2e 0%, #050520 100%);
      border-right: 1px solid #1a1a4e;
      display: flex;
      flex-direction: column;
      z-index: 200;
      overflow-y: auto;
    }
    .sidenav-header {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px 20px;
      border-bottom: 1px solid #1a1a4e;
    }
    .header-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: #4488ff;
    }
    .header-text {
      display: flex;
      flex-direction: column;
    }
    .header-title {
      font-size: 1rem;
      font-weight: 700;
      color: #ffffff;
    }
    .header-subtitle {
      font-size: 0.65rem;
      color: #6666aa;
      text-transform: uppercase;
      letter-spacing: 0.1em;
    }
    .nav-list {
      padding: 4px 0;
    }
    .nav-list a.active {
      background: rgba(68, 136, 255, 0.15) !important;
      border-left: 3px solid #4488ff;
      color: #ffffff !important;
    }
    .nav-list a.active mat-icon {
      color: #4488ff;
    }
    .nav-divider {
      height: 1px;
      background: #1a1a4e;
      margin: 6px 16px;
    }

    /* Info panel */
    .panel-header {
      padding: 10px 16px;
      border-top: 1px solid #1a1a4e;
    }
    .panel-title {
      font-size: 0.7rem;
      font-weight: 700;
      text-transform: uppercase;
      color: #4488ff;
      letter-spacing: 0.1em;
      margin: 0;
    }
    .panel-content {
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
    .info-label {
      font-size: 0.7rem;
      color: #6666aa;
    }
    .info-value {
      font-size: 0.75rem;
      font-weight: 600;
      color: #44ff88;
    }

    /* Main Game Area */
    .game-main-area {
      flex: 1;
      position: relative;
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
      padding: 12px;
      padding-bottom: 120px;
      pointer-events: none;
    }
    .content-overlay ::ng-deep * {
      pointer-events: auto;
    }

    /* Narrative panel */
    .game-panel-narrative {
      height: 80px;
      min-height: 60px;
      border-top: 1px solid #1a1a4e;
      background: rgba(5, 5, 32, 0.9);
      overflow: hidden;
      z-index: 50;
    }

    /* Bottom navigation */
    .bottom-nav {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      display: flex;
      justify-content: space-around;
      align-items: center;
      background: linear-gradient(180deg, #0a0a2e 0%, #050520 100%);
      border-top: 1px solid #1a1a4e;
      box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.5);
      z-index: 1000;
      padding: 4px 0;
      padding-bottom: env(safe-area-inset-bottom, 0);
    }
    .nav-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #6666aa;
      text-decoration: none;
      padding: 6px 4px;
      min-width: 48px;
      min-height: 48px;
      transition: color 0.2s, background 0.2s;
      position: relative;
      flex: 1;
    }
    .nav-item:active {
      background: rgba(68, 136, 255, 0.15);
    }
    .nav-item.active {
      color: #4488ff;
    }
    .nav-item.active::before {
      content: '';
      position: absolute;
      top: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 24px;
      height: 2px;
      background: #4488ff;
      border-radius: 1px;
    }
    .nav-item mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }
    .nav-item span {
      font-size: 0.55rem;
      margin-top: 2px;
      font-weight: 500;
    }
  `,
})
export class MobileAppComponent implements OnInit, OnDestroy {
  activeCharacterName: string | null = null;
  pageTitle = 'DoraMCU Corporation';

  @ViewChild('drawer') drawer?: MatSidenav;

  private mobileQuery: MediaQueryList;
  private mobileQueryListener: (event: MediaQueryListEvent) => void;

  constructor(
    private corpService: CorporationService,
    private router: Router,
    private media: MediaMatcher,
  ) {
    this.mobileQuery = this.media.matchMedia('(max-width: 768px)');
    this.mobileQueryListener = () => {
      // Mobile shell is only shown on mobile, so this is just for safety
      if (!this.mobileQuery.matches && this.drawer) {
        this.drawer.close();
      }
    };
  }

  ngOnInit(): void {
    this.mobileQuery.addEventListener('change', this.mobileQueryListener);
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
    this.mobileQuery.removeEventListener('change', this.mobileQueryListener);
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

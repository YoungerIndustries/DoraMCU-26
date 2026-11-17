import { Component, OnInit, signal, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';
import { CorporationService } from '../../core/services/corporation.service';
import { CorporationEvent, ContractSummary, CrewSummary } from '../../core/models/corporation-state';
import { LevelBadgeComponent } from '../../shared/components/level-badge.component';
import { EventFeedComponent } from '../../shared/components/event-feed.component';
import { Scene3DComponent } from '../../shared/components/scene-3d.component';

@Component({
  selector: 'app-dashboard-overview',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatProgressBarModule,
    MatButtonModule,
    MatIconModule,
    RouterLink,
    LevelBadgeComponent,
    EventFeedComponent,
    Scene3DComponent,
  ],
  template: `
    <div class="traveller-dashboard">
      <!-- Starfield Background -->
      <div class="starfield"></div>
      
      <div class="header traveller-header">
        <div class="header-content">
          <div class="header-badge">
            <span class="badge-icon">⬡</span>
          </div>
          <div class="header-text">
            <h1>STELLAR CORPORATION COMMAND</h1>
            <p class="subtitle">Imperial Standard Date Cycle {{ state()?.officeBuildingLevel }}-{{ state()?.creditBalance ? Math.floor(state().creditBalance / 1000) : 0 }}</p>
          </div>
          <button mat-raised-button class="traveller-btn" (click)="refresh()">
            <mat-icon>refresh</mat-icon> Refresh Sensors
          </button>
        </div>
      </div>

      @if (loading()) {
        <mat-progress-bar mode="indeterminate" class="traveller-progress"></mat-progress-bar>
      }

      <!-- Insolvency Warning -->
      @if (state()?.isInsolvent) {
        <mat-card class="traveller-warning-card">
          <mat-icon class="warning-icon">warning</mat-icon>
          <div class="warning-content">
            <span class="warning-title">⚠ CRITICAL ALERT: TREASURY DEPLETED ⚠</span>
            <span class="warning-text">Corporation balance is negative! Immediate action required to avoid bankruptcy proceedings by the Imperial Revenue Service.</span>
          </div>
        </mat-card>
      }

      <!-- Sector Map 3D View -->
      <mat-card class="traveller-card sector-map-card">
        <div class="card-header traveller-card-header">
          <div class="header-left">
            <span class="card-icon">◈</span>
            <h3>SECTOR NAVIGATION CHART</h3>
          </div>
          <div class="map-legend">
            <span class="legend-item"><span class="legend-dot contract"></span>Contracts</span>
            <span class="legend-item"><span class="legend-dot crew"></span>Crew</span>
            <span class="legend-item"><span class="legend-dot available"></span>Available</span>
          </div>
        </div>
        <div class="galaxy-map-container sector-map-container">
          <app-scene-3d
            #galaxyScene
            (objectClick)="onMapObjectClick($event)"
            (objectHover)="onMapObjectHover($event)"
          ></app-scene-3d>
        </div>
        @if (hoveredObject) {
          <div class="hover-info traveller-hover">
            <span class="hover-name">{{ hoveredObject.name }}</span>
            <span class="hover-detail">{{ hoveredObject.detail }}</span>
          </div>
        }
      </mat-card>

      <!-- Summary Cards -->
      <div class="cards-grid traveller-cards-grid">
        <mat-card class="traveller-card summary-card">
          <div class="card-header">
            <mat-icon>account_balance_wallet</mat-icon>
            <h3>Credit Balance</h3>
          </div>
          <div class="card-value">{{ state()?.creditBalance | number }} credits</div>
          <div class="card-sub">Salaries owed: {{ state()?.totalSalaryObligations | number }}</div>
        </mat-card>

        <mat-card class="traveller-card summary-card">
          <div class="card-header">
            <mat-icon>assignment</mat-icon>
            <h3>Active Contracts</h3>
          </div>
          <div class="card-value">{{ state()?.activeContracts?.length || 0 }}</div>
          <a mat-button class="traveller-link" routerLink="/contracts">View Contracts</a>
        </mat-card>

        <mat-card class="traveller-card summary-card">
          <div class="card-header">
            <mat-icon>groups</mat-icon>
            <h3>Crew Members</h3>
          </div>
          <div class="card-value">{{ state()?.activeHires?.length || 0 }}</div>
          <a mat-button class="traveller-link" routerLink="/crew">View Crew</a>
        </mat-card>

        <mat-card class="traveller-card summary-card">
          <div class="card-header">
            <mat-icon>smart_toy</mat-icon>
            <h3>AI Assistant</h3>
          </div>
          <div class="card-value" [class.active]="state()?.aiAssistantActive">
            {{ state()?.aiAssistantActive ? 'Active' : 'Inactive' }}
          </div>
          <div class="card-sub">Role: {{ state()?.currentRole }}</div>
        </mat-card>

        <mat-card class="traveller-card summary-card">
          <div class="card-header">
            <mat-icon>person</mat-icon>
            <h3>Traveller Character</h3>
          </div>
          <div class="card-value">Linked</div>
          <a mat-button class="traveller-link" routerLink="/character">View Character</a>
        </mat-card>
      </div>

      <!-- Crew List -->
      <mat-card class="traveller-card crew-card">
        <div class="card-header traveller-card-header">
          <div class="header-left">
            <span class="card-icon">◈</span>
            <h3>CREW MANIFEST</h3>
          </div>
          <a mat-button class="traveller-link" routerLink="/crew">See All</a>
        </div>
        @if (state()?.activeHires?.length) {
          <table class="traveller-table crew-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Position</th>
                <th>Rank</th>
                <th>Performance</th>
                <th>Salary</th>
              </tr>
            </thead>
            <tbody>
              @for (hire of state()?.activeHires; track hire.id) {
                <tr>
                  <td><a routerLink="/crew/{{ hire.id }}">{{ hire.aiCharacterName }}</a></td>
                  <td>{{ hire.position }}</td>
                  <td><app-level-badge [level]="hire.careerLevel || 'JUNIOR'"></app-level-badge></td>
                  <td>{{ hire.performanceRating | number:'1.1-1' }}</td>
                  <td>{{ hire.dailySalary | number }} /day</td>
                </tr>
              }
            </tbody>
          </table>
        } @else {
          <p class="empty">No crew members on board</p>
        }
      </mat-card>

      <!-- Recent Events -->
      <mat-card class="traveller-card events-card">
        <app-event-feed [events]="events()"></app-event-feed>
      </mat-card>
    </div>
  `,
  styles: `
    /* Traveller Theme - Space Opera Aesthetics */
    .traveller-dashboard {
      padding: var(--spacing-lg);
      max-width: 1400px;
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

    /* Header Styling */
    .traveller-header {
      display: flex;
      justify-content: center;
      align-items: center;
      margin-bottom: var(--spacing-lg);
      flex-wrap: wrap;
      gap: var(--spacing-sm);
      position: relative;
      z-index: 1;
      border-bottom: 2px solid #00ffcc;
      padding-bottom: var(--spacing-md);
    }

    .header-content {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);
      flex-wrap: wrap;
      width: 100%;
    }

    .header-badge {
      width: 60px;
      height: 60px;
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
      font-size: 2rem;
      color: #00ffcc;
    }

    .header-text {
      flex: 1;
    }

    h1 {
      margin: 0;
      font-size: 1.75rem;
      color: #00ffcc;
      text-transform: uppercase;
      letter-spacing: 0.15em;
      text-shadow: 0 0 10px rgba(0,255,204,0.5);
      font-weight: 700;
    }

    .subtitle {
      margin: 4px 0 0 0;
      font-size: 0.75rem;
      color: #88aacc;
      letter-spacing: 0.1em;
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

    /* Progress Bar */
    .traveller-progress {
      --mat-progress-bar-active-color: #00ffcc;
      position: relative;
      z-index: 1;
    }

    /* Warning Card */
    .traveller-warning-card {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      background: linear-gradient(135deg, #3d0000 0%, #660000 50%, #3d0000 100%) !important;
      color: #ff6666 !important;
      margin-bottom: var(--spacing-md);
      padding: var(--spacing-md);
      border-radius: var(--radius-md) !important;
      border: 2px solid #ff0000 !important;
      box-shadow: 0 0 20px rgba(255,0,0,0.5), inset 0 0 20px rgba(255,0,0,0.2) !important;
      position: relative;
      z-index: 1;
      animation: warning-flash 1.5s ease-in-out infinite;
    }

    @keyframes warning-flash {
      0%, 100% { box-shadow: 0 0 20px rgba(255,0,0,0.5), inset 0 0 20px rgba(255,0,0,0.2); }
      50% { box-shadow: 0 0 30px rgba(255,0,0,0.8), inset 0 0 30px rgba(255,0,0,0.4); }
    }

    .warning-content {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .warning-title {
      font-weight: 700;
      font-size: 1rem;
      letter-spacing: 0.1em;
    }

    .warning-text {
      font-size: 0.8rem;
      opacity: 0.9;
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

    /* Cards Grid */
    .traveller-cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-lg);
    }

    .summary-card {
      padding: var(--spacing-lg);
      text-align: center;
      min-height: 48px;
    }

    .summary-card .card-header {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: var(--spacing-sm);
      margin-bottom: var(--spacing-sm);
    }

    .summary-card .card-header mat-icon {
      color: #00ffcc;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .summary-card .card-header h3 {
      margin: 0;
      font-size: 0.75rem;
      color: #88aacc;
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }

    .card-value {
      font-size: 2rem;
      font-weight: 700;
      color: #00ffcc;
      letter-spacing: -0.02em;
      text-shadow: 0 0 5px rgba(0,255,204,0.3);
    }

    .card-value.active {
      color: #00ff88;
      text-shadow: 0 0 5px rgba(0,255,136,0.5);
    }

    .card-sub {
      font-size: 0.7rem;
      color: #6688aa;
      margin-top: var(--spacing-xs);
    }

    .traveller-link {
      color: #00ffcc !important;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      font-size: 0.75rem;
    }

    /* Crew & Events Cards */
    .crew-card, .events-card {
      padding: var(--spacing-lg);
      margin-bottom: var(--spacing-md);
    }

    .crew-card .card-header, .events-card .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-md);
      padding: 0 var(--spacing-sm);
    }

    /* Traveller Table */
    .traveller-table {
      width: 100%;
      border-collapse: collapse;
    }

    .traveller-table th {
      padding: var(--spacing-sm) var(--spacing-md);
      text-align: left;
      border-bottom: 2px solid rgba(0,255,204,0.3);
      color: #00ffcc;
      text-transform: uppercase;
      letter-spacing: 0.05em;
      font-size: 0.75rem;
      font-weight: 600;
    }

    .traveller-table td {
      padding: var(--spacing-sm) var(--spacing-md);
      text-align: left;
      border-bottom: 1px solid rgba(0,255,204,0.1);
      color: #c0c0c0;
    }

    .traveller-table a {
      color: #00ffcc;
      text-decoration: none;
      font-weight: 500;
      transition: all 0.2s ease;
    }

    .traveller-table a:hover {
      text-shadow: 0 0 5px rgba(0,255,204,0.5);
    }

    .traveller-table tbody tr:hover {
      background: rgba(0,255,204,0.05);
    }

    .empty {
      color: #6688aa;
      font-style: italic;
      text-align: center;
      padding: var(--spacing-xl);
    }

    /* Sector Map */
    .sector-map-card {
      margin-bottom: var(--spacing-md);
      overflow: hidden;
    }

    .sector-map-card .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-sm);
      padding: 0 var(--spacing-sm);
    }

    .map-legend {
      display: flex;
      gap: 12px;
    }

    .legend-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 0.7rem;
      color: #88aacc;
    }

    .legend-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      display: inline-block;
    }

    .legend-dot.contract { 
      background: #ffaa00; 
      box-shadow: 0 0 5px rgba(255,170,0,0.5);
    }
    .legend-dot.crew { 
      background: #4488ff; 
      box-shadow: 0 0 5px rgba(68,136,255,0.5);
    }
    .legend-dot.available { 
      background: #44ff88; 
      box-shadow: 0 0 5px rgba(68,255,136,0.5);
    }

    .sector-map-container {
      height: 300px;
      width: 100%;
      background: radial-gradient(ellipse at center, #0a0e27 0%, #000000 100%);
      border-top: 1px solid rgba(0,255,204,0.2);
      border-bottom: 1px solid rgba(0,255,204,0.2);
    }

    .traveller-hover {
      position: absolute;
      bottom: 10px;
      left: 10px;
      background: rgba(0,0,20,0.9);
      border: 1px solid rgba(0,255,204,0.5);
      border-radius: 4px;
      padding: 8px 12px;
      display: flex;
      flex-direction: column;
      gap: 2px;
      z-index: 10;
      box-shadow: 0 0 10px rgba(0,255,204,0.3);
    }

    .traveller-hover .hover-name {
      font-size: 0.85rem;
      font-weight: 600;
      color: #00ffcc;
    }

    .traveller-hover .hover-detail {
      font-size: 0.7rem;
      color: #88aacc;
    }

    /* Desktop-specific optimizations */
    :host-context(app-desktop-app) .traveller-dashboard {
      padding: var(--spacing-xl);
    }
    :host-context(app-desktop-app) .traveller-cards-grid {
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    }
    :host-context(app-desktop-app) .card-value {
      font-size: 2.25rem;
    }

    /* Mobile-specific optimizations */
    :host-context(app-mobile-app) .traveller-dashboard {
      padding: var(--spacing-sm);
    }
    :host-context(app-mobile-app) h1 {
      font-size: 1.375rem;
    }
    :host-context(app-mobile-app) .traveller-cards-grid {
      grid-template-columns: repeat(2, 1fr);
      gap: var(--spacing-sm);
    }
    :host-context(app-mobile-app) .summary-card {
      padding: var(--spacing-md);
    }
    :host-context(app-mobile-app) .card-value {
      font-size: 1.5rem;
    }
    :host-context(app-mobile-app) .traveller-table {
      display: block;
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }
    :host-context(app-mobile-app) .traveller-header {
      flex-direction: column;
      align-items: flex-start;
    }
    :host-context(app-mobile-app) .sector-map-container {
      height: 200px;
    }
  `,
})
export class DashboardOverviewComponent implements OnInit, AfterViewInit {
  state = this.corpService.state;
  loading = signal(false);
  events = signal<CorporationEvent[]>([]);
  hoveredObject: { name: string; detail: string } | null = null;

  @ViewChild('galaxyScene') galaxyScene?: Scene3DComponent;

  constructor(private corpService: CorporationService) {}

  ngOnInit(): void {
    this.refresh();
    this.corpService.events$.subscribe((event) => {
      if (event) {
        this.events.update((e) => [event, ...e].slice(0, 10));
      }
    });
  }

  ngAfterViewInit(): void {
    // Populate galaxy map after view init using effect-like pattern
    const s = this.corpService.state();
    if (s && this.galaxyScene) {
      this._populateGalaxyMap(s);
    }
  }

  refresh(): void {
    this.loading.set(true);
    this.corpService.loadOfficeState();
    this.corpService.loadFinancialReport();
    setTimeout(() => {
      this.loading.set(false);
      // Refresh galaxy map after data loads
      const s = this.corpService.state();
      if (s && this.galaxyScene) {
        this._populateGalaxyMap(s);
      }
    }, 500);
  }

  onMapObjectClick(event: { id: string; userData?: Record<string, any> }): void {
    const data = event.userData || {};
    if (data['type'] === 'contract') {
      console.log('Contract clicked:', data['title']);
    } else if (data['type'] === 'crew') {
      console.log('Crew clicked:', data['name']);
    }
  }

  onMapObjectHover(event: { id: string | null; userData?: Record<string, any> }): void {
    if (event.id && event.userData) {
      this.hoveredObject = {
        name: event.userData['name'] || event.userData['title'] || event.id,
        detail: event.userData['detail'] || '',
      };
    } else {
      this.hoveredObject = null;
    }
  }

  private _populateGalaxyMap(state: any): void {
    if (!this.galaxyScene) return;
    const scene = this.galaxyScene as any;
    scene.clearAll();

    // Add contract locations as orange spheres
    const contracts: ContractSummary[] = state.activeContracts || [];
    contracts.forEach((c: ContractSummary, i: number) => {
      const angle = (i / Math.max(contracts.length, 1)) * Math.PI * 2;
      const radius = 5 + Math.random() * 5;
      scene.addSphere(
        `contract-${c.id}`,
        { x: Math.cos(angle) * radius, y: 0, z: Math.sin(angle) * radius },
        0.4 + (c.difficulty || 1) * 0.1,
        0xffaa00,
        {
          type: 'contract',
          id: c.id,
          title: c.title,
          name: c.title,
          detail: `${c.type} | Payment: ${c.payment} | Difficulty: ${c.difficulty}`,
          interactive: true,
        }
      );
    });

    // Add crew positions as blue spheres
    const crew: CrewSummary[] = state.activeHires || [];
    crew.forEach((m: CrewSummary, i: number) => {
      const angle = (i / Math.max(crew.length, 1)) * Math.PI * 2 + Math.PI;
      const radius = 8 + Math.random() * 4;
      scene.addSphere(
        `crew-${m.id}`,
        { x: Math.cos(angle) * radius, y: 0, z: Math.sin(angle) * radius },
        0.3 + (m.performanceRating || 0) * 0.05,
        0x4488ff,
        {
          type: 'crew',
          id: m.id,
          name: m.aiCharacterName,
          detail: `${m.position} | Rating: ${m.performanceRating} | Salary: ${m.dailySalary}`,
          interactive: true,
        }
      );
    });

    // Add available contracts as green spheres (outer ring)
    const available: ContractSummary[] = state.availableContracts || [];
    available.forEach((c: ContractSummary, i: number) => {
      const angle = (i / Math.max(available.length, 1)) * Math.PI * 2;
      const radius = 14 + Math.random() * 3;
      scene.addSphere(
        `available-${c.id}`,
        { x: Math.cos(angle) * radius, y: 0, z: Math.sin(angle) * radius },
        0.25,
        0x44ff88,
        {
          type: 'available',
          id: c.id,
          title: c.title,
          name: c.title,
          detail: `${c.type} | Payment: ${c.payment} | Difficulty: ${c.difficulty}`,
          interactive: true,
        }
      );
    });

    // Center: corporation headquarters (large torus)
    scene.addTorus(
      'headquarters',
      { x: 0, y: 0, z: 0 },
      2,
      0.3,
      0x8844ff,
      {
        type: 'headquarters',
        name: 'Corporation HQ',
        detail: `${state.corporationName} | Level ${state.officeBuildingLevel}`,
        interactive: true,
      }
    );
  }
}

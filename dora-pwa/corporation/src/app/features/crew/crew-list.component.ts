import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { CorporationService } from '../../core/services/corporation.service';
import { LevelBadgeComponent } from '../../shared/components/level-badge.component';
import { Scene3DComponent } from '../../shared/components/scene-3d.component';
import { AiCharacterSummary, CrewSummary } from '../../core/models/corporation-state';

@Component({
  selector: 'app-crew-list',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    RouterLink,
    LevelBadgeComponent,
    Scene3DComponent,
  ],
  template: `
    <div class="traveller-crew-page">
      <!-- Starfield Background -->
      <div class="starfield"></div>
      
      <div class="header traveller-header">
        <div class="header-content">
          <div class="header-badge">
            <span class="badge-icon">⬡</span>
          </div>
          <div class="header-text">
            <h1>CREW MANIFEST</h1>
            <p class="subtitle">Ship Personnel Database</p>
          </div>
          <button mat-raised-button class="traveller-btn" (click)="openHireDialog()">
            <mat-icon>person_add</mat-icon> Recruit Crew
          </button>
        </div>
      </div>

      <!-- Ship 3D View -->
      <mat-card class="traveller-card ship-view-card">
        <div class="card-header traveller-card-header">
          <div class="header-left">
            <span class="card-icon">◈</span>
            <h3>SHIP DIAGRAM</h3>
          </div>
          <div class="ship-legend">
            <span class="legend-item"><span class="legend-dot captain"></span>Captain</span>
            <span class="legend-item"><span class="legend-dot officer"></span>Officer</span>
            <span class="legend-item"><span class="legend-dot crew"></span>Crew</span>
          </div>
        </div>
        <div class="ship-view-container">
          <app-scene-3d
            #shipScene
            (objectClick)="onShipObjectClick($event)"
            (objectHover)="onShipObjectHover($event)"
          ></app-scene-3d>
        </div>
        @if (hoveredShip) {
          <div class="hover-info traveller-hover">
            <span class="hover-name">{{ hoveredShip.name }}</span>
            <span class="hover-detail">{{ hoveredShip.detail }}</span>
          </div>
        }
      </mat-card>

      @if (state()?.activeHires?.length) {
        <table class="traveller-table crew-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Position</th>
              <th>Rank</th>
              <th>Performance</th>
              <th>Salary</th>
              <th>Status</th>
              <th>Actions</th>
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
                <td><span class="status-badge">{{ hire.status }}</span></td>
                <td>
                  <button mat-icon-button class="traveller-warn-btn" (click)="fire(hire.id)">
                    <mat-icon>delete</mat-icon>
                  </button>
                </td>
              </tr>
            }
          </tbody>
        </table>
      } @else {
        <p class="empty">No crew members on board</p>
      }
    </div>
  `,
  styles: `
    /* Traveller Theme - Space Opera Aesthetics */
    .traveller-crew-page {
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

    /* Traveller Table */
    .traveller-table {
      width: 100%;
      border-collapse: collapse;
      position: relative;
      z-index: 1;
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

    .status-badge {
      display: inline-flex;
      align-items: center;
      padding: 3px 10px;
      border-radius: 20px;
      background: rgba(0,255,136,0.1);
      color: #00ff88;
      font-size: 0.65rem;
      font-weight: 600;
      letter-spacing: 0.03em;
      border: 1px solid rgba(0,255,136,0.3);
    }

    .traveller-warn-btn {
      color: #ff6666 !important;
      background: rgba(255,0,0,0.1) !important;
      border: 1px solid rgba(255,0,0,0.3) !important;
    }

    /* Ship 3D View */
    .ship-view-card {
      margin-bottom: var(--spacing-md);
      overflow: hidden;
    }

    .ship-legend {
      display: flex;
      gap: 12px;
    }

    .ship-legend .legend-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 0.7rem;
      color: #88aacc;
    }

    .ship-legend .legend-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      display: inline-block;
    }

    .legend-dot.captain { 
      background: #ffaa00; 
      box-shadow: 0 0 5px rgba(255,170,0,0.5);
    }
    .legend-dot.officer { 
      background: #4488ff; 
      box-shadow: 0 0 5px rgba(68,136,255,0.5);
    }
    .legend-dot.crew { 
      background: #44ff88; 
      box-shadow: 0 0 5px rgba(68,255,136,0.5);
    }

    .ship-view-container {
      height: 280px;
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

    .empty {
      color: #6688aa;
      font-style: italic;
      text-align: center;
      padding: var(--spacing-xl);
      position: relative;
      z-index: 1;
    }
  `,
})
export class CrewListComponent implements OnInit, AfterViewInit {
  state = this.corpService.state;
  hoveredShip: { name: string; detail: string } | null = null;

  @ViewChild('shipScene') shipScene?: Scene3DComponent;

  // Preset positions for hiring
  positions = ['CFO', 'SECURITY_CHIEF', 'ENGINEER', 'NAVIGATOR', 'TACTICIAN', 'DIPLOMAT', 'SCIENTIST'];

  // Position to ship location mapping
  positionLocations: Record<string, { x: number; y: number; z: number }> = {
    'CFO': { x: 0, y: 2, z: 0 },
    'CAPTAIN': { x: 0, y: 2, z: 0 },
    'SECURITY_CHIEF': { x: -3, y: 0, z: 2 },
    'ENGINEER': { x: 0, y: -1, z: 0 },
    'NAVIGATOR': { x: 3, y: 1, z: 0 },
    'TACTICIAN': { x: 3, y: 0, z: 2 },
    'DIPLOMAT': { x: -3, y: 1, z: 0 },
    'SCIENTIST': { x: 0, y: 0, z: -3 },
  };

  constructor(
    private corpService: CorporationService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.corpService.loadOfficeState();
  }

  ngAfterViewInit(): void {
    const s = this.corpService.state();
    if (s && this.shipScene) {
      this._populateShipView(s.activeHires || []);
    }
  }

  onShipObjectClick(event: { id: string; userData?: Record<string, any> }): void {
    console.log('Ship object clicked:', event);
  }

  onShipObjectHover(event: { id: string | null; userData?: Record<string, any> }): void {
    if (event.id && event.userData) {
      this.hoveredShip = {
        name: event.userData['name'] || event.id,
        detail: event.userData['detail'] || '',
      };
    } else {
      this.hoveredShip = null;
    }
  }

  private _populateShipView(crew: CrewSummary[]): void {
    if (!this.shipScene) return;
    const scene = this.shipScene as any;
    scene.clearAll();

    // Ship hull (large box)
    scene.addBox(
      'ship-hull',
      { x: 0, y: 0, z: 0 },
      { x: 8, y: 4, z: 6 },
      0x2a2a4a,
      { name: 'Ship Hull', detail: 'Corporation vessel', interactive: false }
    );

    // Bridge (smaller box on top)
    scene.addBox(
      'bridge',
      { x: 0, y: 2.5, z: 0 },
      { x: 3, y: 1, z: 2 },
      0x3a3a6a,
      { name: 'Bridge', detail: 'Command center', interactive: false }
    );

    // Engine room (glowing sphere at bottom)
    scene.addSphere(
      'engine',
      { x: 0, y: -2, z: 0 },
      0.8,
      0x4488ff,
      { name: 'Engine Room', detail: 'Power core', interactive: false }
    );

    // Crew members positioned by role
    crew.forEach((member: CrewSummary, i: number) => {
      const loc = this.positionLocations[member.position] || {
        x: (i % 3 - 1) * 2,
        y: 0,
        z: Math.floor(i / 3) * 2 - 2,
      };

      // Color by rank
      let color: number;
      if (member.position === 'CAPTAIN' || member.position === 'CFO') {
        color = 0xffaa00; // Gold for captain/CFO
      } else if (['SECURITY_CHIEF', 'TACTICIAN', 'NAVIGATOR'].includes(member.position)) {
        color = 0x4488ff; // Blue for officers
      } else {
        color = 0x44ff88; // Green for crew
      }

      scene.addSphere(
        `crew-${member.id}`,
        { x: loc.x, y: loc.y, z: loc.z },
        0.3 + (member.performanceRating || 0) * 0.05,
        color,
        {
          name: member.aiCharacterName,
          detail: `${member.position} | Rating: ${member.performanceRating} | Level: ${member.careerLevel || 'JUNIOR'}`,
          interactive: true,
        }
      );
    });
  }

  fire(hiringId: number): void {
    const ref = this.snackBar.open('Fire this crew member?', 'Confirm', {
      duration: 8000,
      horizontalPosition: 'center',
      verticalPosition: 'bottom',
    });
    ref.onAction().subscribe(() => {
      this.corpService.fireAiCharacter(hiringId).subscribe({
        next: () => {
          this.corpService.loadOfficeState();
          this.snackBar.open('Crew member fired', 'Close', { duration: 3000 });
        },
        error: (err) => {
          this.snackBar.open('Failed to fire crew member', 'Close', { duration: 3000 });
          console.error('Failed to fire crew', err);
        },
      });
    });
  }

  openHireDialog(): void {
    const characters: AiCharacterSummary[] = this.state()?.availableAiCharacters || [];
    if (characters.length === 0) {
      this.snackBar.open('No available AI characters to hire', 'Close', { duration: 3000 });
      return;
    }

    // Hire the first available character with default position and salary
    const char = characters[0];
    const position = this.positions[0];
    const salary = 500;

    this.corpService.hireAiCharacter(char.id, position, salary).subscribe({
      next: () => {
        this.corpService.loadOfficeState();
        this.snackBar.open(`${char.name} hired as ${position}`, 'Close', { duration: 3000 });
      },
      error: (err) => {
        this.snackBar.open('Failed to hire crew member', 'Close', { duration: 3000 });
        console.error('Failed to hire', err);
      },
    });
  }
}

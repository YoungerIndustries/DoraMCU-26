import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { CorporationService } from '../../core/services/corporation.service';
import { Scene3DComponent } from '../../shared/components/scene-3d.component';

@Component({
  selector: 'app-finance',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    Scene3DComponent,
  ],
  template: `
    <div class="traveller-finance-page">
      <!-- Starfield Background -->
      <div class="starfield"></div>
      
      <div class="header traveller-header">
        <div class="header-content">
          <div class="header-badge">
            <span class="badge-icon">⬡</span>
          </div>
          <div class="header-text">
            <h1>TREASURY REPORT</h1>
            <p class="subtitle">Corporation Financial Status</p>
          </div>
        </div>
      </div>

      @if (report()) {
        <!-- Insolvency Warning -->
        @if (report()!.isInsolvent) {
          <mat-card class="traveller-warning-card">
            <mat-icon>warning</mat-icon>
            <div class="warning-content">
              <span class="warning-title">⚠ CRITICAL ALERT: TREASURY DEPLETED ⚠</span>
              <span class="warning-text">Corporation balance is negative! Immediate action required to avoid bankruptcy proceedings by the Imperial Revenue Service.</span>
            </div>
          </mat-card>
        }

        <!-- Summary Cards -->
        <div class="cards-grid traveller-cards-grid">
          <mat-card class="traveller-card summary-card">
            <div class="card-header">
              <mat-icon>account_balance_wallet</mat-icon>
              <h3>Credit Balance</h3>
            </div>
            <div class="card-value" [class.negative]="report()!.creditBalance < 0">
              {{ report()!.creditBalance | number }}
            </div>
          </mat-card>

          <mat-card class="traveller-card summary-card">
            <div class="card-header">
              <mat-icon>trending_up</mat-icon>
              <h3>Total Revenue</h3>
            </div>
            <div class="card-value positive">{{ report()!.totalRevenue | number }}</div>
          </mat-card>

          <mat-card class="traveller-card summary-card">
            <div class="card-header">
              <mat-icon>trending_down</mat-icon>
              <h3>Total Expenses</h3>
            </div>
            <div class="card-value negative">{{ report()!.totalExpenses | number }}</div>
          </mat-card>

          <mat-card class="traveller-card summary-card">
            <div class="card-header">
              <mat-icon>show_chart</mat-icon>
              <h3>Net Profit</h3>
            </div>
            <div class="card-value" [class.negative]="report()!.netProfit < 0">
              {{ report()!.netProfit | number }}
            </div>
          </mat-card>
        </div>

        <!-- Economic Visualization 3D -->
        <mat-card class="traveller-card economy-viz-card">
          <div class="card-header traveller-card-header">
            <div class="header-left">
              <span class="card-icon">◈</span>
              <h3>ECONOMIC OVERVIEW</h3>
            </div>
            <div class="economy-legend">
              <span class="legend-item"><span class="legend-dot healthy"></span>Healthy</span>
              <span class="legend-item"><span class="legend-dot warning"></span>Warning</span>
              <span class="legend-item"><span class="legend-dot critical"></span>Critical</span>
            </div>
          </div>
          <div class="economy-viz-container">
            <app-scene-3d
              #economyScene
              (objectClick)="onEconomyObjectClick($event)"
              (objectHover)="onEconomyObjectHover($event)"
            ></app-scene-3d>
          </div>
          @if (hoveredEconomy) {
            <div class="hover-info traveller-hover">
              <span class="hover-name">{{ hoveredEconomy.name }}</span>
              <span class="hover-detail">{{ hoveredEconomy.detail }}</span>
            </div>
          }
        </mat-card>

        <!-- Transaction History -->
        <mat-card class="traveller-card transactions-card">
          <div class="card-header traveller-card-header">
            <div class="header-left">
              <span class="card-icon">◈</span>
              <h3>TRANSACTION LOG ({{ transactions.length }})</h3>
            </div>
          </div>
          @if (transactions.length === 0) {
            <p class="empty">No transactions yet</p>
          } @else {
            <table class="traveller-table transactions-table">
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Amount</th>
                  <th>Description</th>
                  <th>Date</th>
                </tr>
              </thead>
              <tbody>
                @for (txn of transactions; track txn.id) {
                  <tr>
                    <td><span class="type-badge" [ngClass]="txn.type.toLowerCase()">{{ txn.type }}</span></td>
                    <td [class.positive]="txn.amount > 0" [class.negative]="txn.amount < 0">
                      {{ txn.amount > 0 ? '+' : '' }}{{ txn.amount | number }}
                    </td>
                    <td>{{ txn.description }}</td>
                    <td>{{ txn.createdAt | date:'medium' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          }
        </mat-card>
      } @else {
        <p class="loading">Loading financial report...</p>
      }
    </div>
  `,
  styles: `
    /* Traveller Theme - Space Opera Aesthetics */
    .traveller-finance-page {
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

    .traveller-cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-lg);
    }

    .summary-card {
      padding: var(--spacing-lg);
      text-align: center;
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
      font-size: 1.75rem;
      font-weight: 700;
      letter-spacing: -0.02em;
      color: #00ffcc;
      text-shadow: 0 0 5px rgba(0,255,204,0.3);
    }

    .card-value.positive { 
      color: #00ff88; 
      text-shadow: 0 0 5px rgba(0,255,136,0.3);
    }
    .card-value.negative { 
      color: #ff6666; 
      text-shadow: 0 0 5px rgba(255,102,102,0.3);
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

    .traveller-table tbody tr:hover {
      background: rgba(0,255,204,0.05);
    }

    .transactions-card {
      padding: var(--spacing-lg);
      margin-bottom: var(--spacing-md);
    }

    .type-badge {
      display: inline-flex;
      align-items: center;
      padding: 3px 10px;
      border-radius: 20px;
      font-size: 0.65rem;
      font-weight: 600;
      text-transform: uppercase;
      letter-spacing: 0.03em;
    }

    .type-badge.revenue { 
      background: rgba(0,255,136,0.1); 
      color: #00ff88; 
      border: 1px solid rgba(0,255,136,0.3);
    }
    .type-badge.salary { 
      background: rgba(68,136,255,0.1); 
      color: #4488ff; 
      border: 1px solid rgba(68,136,255,0.3);
    }
    .type-badge.penalty { 
      background: rgba(255,102,102,0.1); 
      color: #ff6666; 
      border: 1px solid rgba(255,102,102,0.3);
    }
    .type-badge.expense { 
      background: rgba(170,136,255,0.1); 
      color: #aa88ff; 
      border: 1px solid rgba(170,136,255,0.3);
    }
    .type-badge.bonus { 
      background: rgba(255,170,0,0.1); 
      color: #ffaa00; 
      border: 1px solid rgba(255,170,0,0.3);
    }

    .positive { color: #00ff88; }
    .negative { color: #ff6666; }

    .empty {
      color: #6688aa;
      font-style: italic;
      text-align: center;
      padding: var(--spacing-xl);
    }

    /* Economy Visualization */
    .economy-viz-card {
      margin-bottom: var(--spacing-md);
      overflow: hidden;
    }

    .economy-legend {
      display: flex;
      gap: 12px;
    }

    .economy-legend .legend-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 0.7rem;
      color: #88aacc;
    }

    .economy-legend .legend-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      display: inline-block;
    }

    .legend-dot.healthy { 
      background: #44ff88; 
      box-shadow: 0 0 5px rgba(68,255,136,0.5);
    }
    .legend-dot.warning { 
      background: #ffaa00; 
      box-shadow: 0 0 5px rgba(255,170,0,0.5);
    }
    .legend-dot.critical { 
      background: #ff4444; 
      box-shadow: 0 0 5px rgba(255,68,68,0.5);
    }

    .economy-viz-container {
      height: 250px;
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

    .loading {
      color: #88aacc;
      text-align: center;
      padding: var(--spacing-xl);
      position: relative;
      z-index: 1;
    }
  `,
})
export class FinanceComponent implements OnInit, AfterViewInit {
  report = this.corpService.financialReport;
  transactions: { id: number; type: string; amount: number; description: string; createdAt: string }[] = [];
  hoveredEconomy: { name: string; detail: string } | null = null;

  @ViewChild('economyScene') economyScene?: Scene3DComponent;

  constructor(private corpService: CorporationService) {}

  ngOnInit(): void {
    this.corpService.loadFinancialReport();
    this.corpService.getTransactions().subscribe({
      next: (txns) => (this.transactions = txns),
      error: (err) => console.error('Failed to load transactions', err),
    });
  }

  ngAfterViewInit(): void {
    const r = this.corpService.financialReport();
    if (r && this.economyScene) {
      this._populateEconomyViz(r);
    }
  }

  onEconomyObjectClick(event: { id: string; userData?: Record<string, any> }): void {
    console.log('Economy object clicked:', event);
  }

  onEconomyObjectHover(event: { id: string | null; userData?: Record<string, any> }): void {
    if (event.id && event.userData) {
      this.hoveredEconomy = {
        name: event.userData['name'] || event.id,
        detail: event.userData['detail'] || '',
      };
    } else {
      this.hoveredEconomy = null;
    }
  }

  private _populateEconomyViz(report: any): void {
    if (!this.economyScene) return;
    const scene = this.economyScene as any;
    scene.clearAll();

    // Determine health color based on balance
    const balance = report.creditBalance || 0;
    const netProfit = report.netProfit || 0;
    let healthColor: number;
    if (balance > 0 && netProfit > 0) {
      healthColor = 0x44ff88; // Healthy
    } else if (balance > 0) {
      healthColor = 0xffaa00; // Warning
    } else {
      healthColor = 0xff4444; // Critical
    }

    // Center: corporation treasury (large sphere, color-coded)
    scene.addSphere(
      'treasury',
      { x: 0, y: 0, z: 0 },
      1.5 + Math.min(Math.abs(balance) / 10000, 2),
      healthColor,
      {
        name: 'Treasury',
        detail: `Balance: ${balance} | Net Profit: ${netProfit}`,
        interactive: true,
      }
    );

    // Revenue stream (green cone)
    scene.addCone(
      'revenue',
      { x: -5, y: 0, z: 0 },
      0.5,
      1 + (report.totalRevenue || 0) / 5000,
      0x44ff88,
      {
        name: 'Revenue',
        detail: `Total: ${report.totalRevenue || 0} credits`,
        interactive: true,
      }
    );

    // Expenses stream (red cone)
    scene.addCone(
      'expenses',
      { x: 5, y: 0, z: 0 },
      0.5,
      1 + (report.totalExpenses || 0) / 5000,
      0xff4444,
      {
        name: 'Expenses',
        detail: `Total: ${report.totalExpenses || 0} credits`,
        interactive: true,
      }
    );

    // Salary obligations (blue box)
    scene.addBox(
      'salaries',
      { x: 0, y: 0, z: -5 },
      { x: 0.8, y: 0.8 + (report.totalSalaryObligations || 0) / 5000, z: 0.8 },
      0x4488ff,
      {
        name: 'Salary Obligations',
        detail: `Total: ${report.totalSalaryObligations || 0} credits`,
        interactive: true,
      }
    );

    // Transaction count (torus ring)
    scene.addTorus(
      'transactions',
      { x: 0, y: 0, z: 5 },
      1,
      0.2,
      0xaa88ff,
      {
        name: 'Transactions',
        detail: `Count: ${report.transactionCount || 0}`,
        interactive: true,
      }
    );
  }
}

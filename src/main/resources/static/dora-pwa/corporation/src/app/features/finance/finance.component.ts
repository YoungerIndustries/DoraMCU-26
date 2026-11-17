import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { CorporationService } from '../../core/services/corporation.service';
import { FinancialBreakdown, PayrollSummary, FinanceTransaction, ContractSummary, CrewSummary } from '../../core/models/corporation-state';

type TabKey = 'treasury' | 'pnl' | 'contracts' | 'crew';

@Component({
  selector: 'app-finance',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatDividerModule,
    MatProgressBarModule,
  ],
  template: `
    <div class="traveller-finance-page">
      <div class="starfield"></div>

      <div class="header traveller-header">
        <div class="header-content">
          <div class="header-badge">
            <span class="badge-icon">⬡</span>
          </div>
          <div class="header-text">
            <h1>FINANCIAL MANAGEMENT</h1>
            <p class="subtitle">Corporation Treasury, P&L, Contracts & Crew</p>
          </div>
        </div>
      </div>

      @if (loading()) {
        <p class="loading">Loading financial data...</p>
      } @else {

        <!-- Insolvency Warning -->
        @if (isInsolvent()) {
          <mat-card class="traveller-warning-card">
            <mat-icon>warning</mat-icon>
            <div class="warning-content">
              <span class="warning-title">⚠ CRITICAL ALERT: TREASURY DEPLETED ⚠</span>
              <span class="warning-text">Corporation balance is negative! Immediate action required.</span>
            </div>
          </mat-card>
        }

        <!-- Tab Navigation -->
        <div class="tab-nav">
          @for (tab of tabs; track tab.key) {
            <button
              class="tab-btn"
              [class.active]="activeTab() === tab.key"
              (click)="activeTab.set(tab.key)"
            >
              <mat-icon>{{ tab.icon }}</mat-icon>
              {{ tab.label }}
            </button>
          }
        </div>

        <!-- Tab Content -->
        <div class="tab-content">

          <!-- ==================== TREASURY TAB ==================== -->
          @if (activeTab() === 'treasury') {
            <div class="treasury-tab">
              <!-- Summary Cards -->
              <div class="cards-grid">
                <mat-card class="traveller-card summary-card">
                  <div class="card-header">
                    <mat-icon>account_balance_wallet</mat-icon>
                    <h3>Credit Balance</h3>
                  </div>
                  <div class="card-value" [class.negative]="creditBalance() < 0">
                    {{ creditBalance() | number }}
                  </div>
                </mat-card>

                <mat-card class="traveller-card summary-card">
                  <div class="card-header">
                    <mat-icon>trending_up</mat-icon>
                    <h3>Total Revenue</h3>
                  </div>
                  <div class="card-value positive">{{ totalRevenue() | number }}</div>
                </mat-card>

                <mat-card class="traveller-card summary-card">
                  <div class="card-header">
                    <mat-icon>trending_down</mat-icon>
                    <h3>Total Expenses</h3>
                  </div>
                  <div class="card-value negative">{{ totalExpenses() | number }}</div>
                </mat-card>

                <mat-card class="traveller-card summary-card">
                  <div class="card-header">
                    <mat-icon>show_chart</mat-icon>
                    <h3>Net Profit</h3>
                  </div>
                  <div class="card-value" [class.negative]="netProfit() < 0">
                    {{ netProfit() | number }}
                  </div>
                </mat-card>
              </div>

              <!-- Account Breakdown -->
              <mat-card class="traveller-card account-card">
                <div class="card-header traveller-card-header">
                  <span class="card-icon">◈</span>
                  <h3>TREASURY ACCOUNTS</h3>
                </div>
                <div class="accounts-grid">
                  <div class="account-item">
                    <div class="account-label">Operating Account</div>
                    <div class="account-value">{{ creditBalance() | number }} cr</div>
                  </div>
                  <div class="account-item">
                    <div class="account-label">Daily Payroll</div>
                    <div class="account-value">{{ totalDailyPayroll() | number }} cr</div>
                  </div>
                  <div class="account-item">
                    <div class="account-label">Period Payroll (7 turns)</div>
                    <div class="account-value">{{ periodPayroll() | number }} cr</div>
                  </div>
                  <div class="account-item">
                    <div class="account-label">Active Hires</div>
                    <div class="account-value">{{ activeHireCount() }}</div>
                  </div>
                </div>
              </mat-card>

              <!-- Transaction History -->
              <mat-card class="traveller-card transactions-card">
                <div class="card-header traveller-card-header">
                  <span class="card-icon">◈</span>
                  <h3>TRANSACTION LOG ({{ transactions().length }})</h3>
                </div>
                @if (transactions().length === 0) {
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
                      @for (txn of transactions(); track txn.id) {
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
            </div>
          }

          <!-- ==================== P&L TAB ==================== -->
          @if (activeTab() === 'pnl') {
            <div class="pnl-tab">
              @if (breakdown()) {
                <!-- Revenue Breakdown -->
                <mat-card class="traveller-card pnl-card">
                  <div class="card-header traveller-card-header">
                    <span class="card-icon">◈</span>
                    <h3>REVENUE BREAKDOWN</h3>
                  </div>
                  <div class="breakdown-section">
                    <div class="breakdown-row">
                      <span class="breakdown-label">Contract Revenue</span>
                      <span class="breakdown-value positive">{{ breakdown()!.revenueBreakdown.contractRevenue | number }} cr</span>
                    </div>
                    <div class="breakdown-row">
                      <span class="breakdown-label">Bonuses</span>
                      <span class="breakdown-value positive">{{ breakdown()!.revenueBreakdown.bonuses | number }} cr</span>
                    </div>
                    <mat-divider></mat-divider>
                    <div class="breakdown-row total">
                      <span class="breakdown-label">Total Revenue</span>
                      <span class="breakdown-value positive">{{ breakdown()!.totalRevenue | number }} cr</span>
                    </div>
                  </div>
                </mat-card>

                <!-- Expense Breakdown -->
                <mat-card class="traveller-card pnl-card">
                  <div class="card-header traveller-card-header">
                    <span class="card-icon">◈</span>
                    <h3>EXPENSE BREAKDOWN</h3>
                  </div>
                  <div class="breakdown-section">
                    <div class="breakdown-row">
                      <span class="breakdown-label">Crew Salaries</span>
                      <span class="breakdown-value negative">{{ breakdown()!.expenseBreakdown.salaries | number }} cr</span>
                    </div>
                    <div class="breakdown-row">
                      <span class="breakdown-label">Penalties</span>
                      <span class="breakdown-value negative">{{ breakdown()!.expenseBreakdown.penalties | number }} cr</span>
                    </div>
                    <div class="breakdown-row">
                      <span class="breakdown-label">General Expenses</span>
                      <span class="breakdown-value negative">{{ breakdown()!.expenseBreakdown.generalExpenses | number }} cr</span>
                    </div>
                    <mat-divider></mat-divider>
                    <div class="breakdown-row total">
                      <span class="breakdown-label">Total Expenses</span>
                      <span class="breakdown-value negative">{{ breakdown()!.totalExpenses | number }} cr</span>
                    </div>
                  </div>
                </mat-card>

                <!-- Net Profit -->
                <mat-card class="traveller-card net-profit-card">
                  <div class="card-header traveller-card-header">
                    <span class="card-icon">◈</span>
                    <h3>NET PROFIT</h3>
                  </div>
                  <div class="net-profit-value" [class.positive]="breakdown()!.netProfit >= 0" [class.negative]="breakdown()!.netProfit < 0">
                    {{ breakdown()!.netProfit >= 0 ? '+' : '' }}{{ breakdown()!.netProfit | number }} cr
                  </div>
                </mat-card>
              } @else {
                <p class="loading">Loading breakdown...</p>
              }
            </div>
          }

          <!-- ==================== CONTRACTS TAB ==================== -->
          @if (activeTab() === 'contracts') {
            <div class="contracts-tab">
              <!-- Available Contracts with ROI -->
              <mat-card class="traveller-card contracts-card">
                <div class="card-header traveller-card-header">
                  <span class="card-icon">◈</span>
                  <h3>AVAILABLE CONTRACTS</h3>
                </div>
                @if (availableContracts().length === 0) {
                  <p class="empty">No available contracts</p>
                } @else {
                  <table class="traveller-table contracts-table">
                    <thead>
                      <tr>
                        <th>Title</th>
                        <th>Type</th>
                        <th>Payment</th>
                        <th>Est. Cost</th>
                        <th>Net ROI</th>
                        <th>Duration</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (contract of availableContracts(); track contract.id) {
                        <tr>
                          <td>{{ contract.title }}</td>
                          <td><span class="type-badge">{{ contract.type }}</span></td>
                          <td class="positive">{{ contract.payment | number }} cr</td>
                          <td>{{ estimateCost(contract) | number }} cr</td>
                          <td [class.positive]="calculateRoi(contract) >= 0" [class.negative]="calculateRoi(contract) < 0">
                            {{ calculateRoi(contract) | number }} cr
                          </td>
                          <td>{{ contract.durationTurns }} turns</td>
                          <td>
                            <button mat-raised-button color="primary" (click)="acceptContract(contract.id)">
                              Accept
                            </button>
                          </td>
                        </tr>
                      }
                    </tbody>
                  </table>
                }
              </mat-card>

              <!-- Active Contracts with Crew Assignment -->
              <mat-card class="traveller-card contracts-card">
                <div class="card-header traveller-card-header">
                  <span class="card-icon">◈</span>
                  <h3>ACTIVE CONTRACTS</h3>
                </div>
                @if (activeContracts().length === 0) {
                  <p class="empty">No active contracts</p>
                } @else {
                  @for (contract of activeContracts(); track contract.id) {
                    <div class="active-contract-item">
                      <div class="contract-info">
                        <div class="contract-title">{{ contract.title }}</div>
                        <div class="contract-meta">
                          <span class="type-badge">{{ contract.type }}</span>
                          <span>{{ contract.payment | number }} cr</span>
                          <span>{{ contract.durationTurns }} turns</span>
                        </div>
                        <div class="progress-bar-container">
                          <div class="progress-bar" [style.width.%]="contract.progressPercent || 0"></div>
                          <span class="progress-label">{{ (contract.progressPercent || 0).toFixed(0) }}%</span>
                        </div>
                        <div class="crew-assignment">
                          <span class="crew-label">Assigned Crew: {{ contract.assignedCrewCount || 0 }}</span>
                          <button mat-raised-button color="accent" (click)="openAssignDialog(contract.id)">
                            Assign Crew
                          </button>
                        </div>
                      </div>
                    </div>
                  }
                }
              </mat-card>

              <!-- Crew Assignment Dialog -->
              @if (assigningContractId() != null) {
                <div class="assign-dialog-overlay" (click)="closeAssignDialog()">
                  <div class="assign-dialog" (click)="$event.stopPropagation()">
                    <h3>Assign Crew to Contract</h3>
                    @for (hire of crewMembers(); track hire.id) {
                      <div class="crew-item">
                        <span class="crew-name">{{ hire.aiCharacterName }}</span>
                        <span class="crew-position">{{ hire.position }}</span>
                        <button mat-raised-button color="primary" (click)="assignCrew(hire.id)">
                          Assign
                        </button>
                      </div>
                    }
                    <button mat-raised-button (click)="closeAssignDialog()">Close</button>
                  </div>
                </div>
              }
            </div>
          }

          <!-- ==================== CREW TAB ==================== -->
          @if (activeTab() === 'crew') {
            <div class="crew-tab">
              <!-- Payroll Summary -->
              <mat-card class="traveller-card payroll-card">
                <div class="card-header traveller-card-header">
                  <span class="card-icon">◈</span>
                  <h3>PAYROLL SUMMARY</h3>
                </div>
                <div class="payroll-summary">
                  <div class="payroll-item">
                    <span class="payroll-label">Total Daily Payroll</span>
                    <span class="payroll-value">{{ payrollSummary()?.totalDailyPayroll | number }} cr</span>
                  </div>
                  <div class="payroll-item">
                    <span class="payroll-label">Period Payroll (7 turns)</span>
                    <span class="payroll-value">{{ payrollSummary()?.periodPayroll | number }} cr</span>
                  </div>
                  <div class="payroll-item">
                    <span class="payroll-label">Active Hires</span>
                    <span class="payroll-value">{{ payrollSummary()?.activeHireCount }}</span>
                  </div>
                </div>
              </mat-card>

              <!-- Crew Manifest -->
              <mat-card class="traveller-card crew-card">
                <div class="card-header traveller-card-header">
                  <span class="card-icon">◈</span>
                  <h3>CREW MANIFEST</h3>
                </div>
                @if (crewMembers().length === 0) {
                  <p class="empty">No crew members</p>
                } @else {
                  <table class="traveller-table crew-table">
                    <thead>
                      <tr>
                        <th>Name</th>
                        <th>Position</th>
                        <th>Daily Salary</th>
                        <th>Career Level</th>
                        <th>XP Earned</th>
                        <th>Performance</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      @for (hire of crewMembers(); track hire.id) {
                        <tr>
                          <td>{{ hire.aiCharacterName }}</td>
                          <td>{{ hire.position }}</td>
                          <td>{{ hire.dailySalary | number }} cr</td>
                          <td>{{ hire.careerLevel || 'N/A' }}</td>
                          <td>{{ hire.xpEarned || 0 }}</td>
                          <td>{{ hire.performanceRating?.toFixed(1) || 'N/A' }}</td>
                          <td><span class="status-badge active">{{ hire.status }}</span></td>
                        </tr>
                      }
                    </tbody>
                  </table>
                }
              </mat-card>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: `
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
        radial-gradient(1px 1px at 90px 40px, #ffffff, transparent);
    }

    .header { position: relative; z-index: 1; }
    .header-content { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .header-badge { width: 48px; height: 48px; border-radius: 8px; background: rgba(0,255,204,0.1); border: 1px solid rgba(0,255,204,0.3); display: flex; align-items: center; justify-content: center; }
    .badge-icon { font-size: 24px; color: #00ffcc; }
    .header-text h1 { margin: 0; font-size: 1.5rem; color: #00ffcc; letter-spacing: 2px; }
    .subtitle { margin: 4px 0 0; color: #88aacc; font-size: 0.85rem; }

    /* Tab Navigation */
    .tab-nav {
      display: flex;
      gap: 8px;
      margin-bottom: 24px;
      position: relative;
      z-index: 1;
    }
    .tab-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 24px;
      background: rgba(0,255,204,0.05);
      border: 1px solid rgba(0,255,204,0.2);
      border-radius: 8px;
      color: #88aacc;
      font-family: 'Courier New', monospace;
      font-size: 0.9rem;
      cursor: pointer;
      transition: all 0.2s;
    }
    .tab-btn:hover {
      background: rgba(0,255,204,0.1);
      color: #00ffcc;
    }
    .tab-btn.active {
      background: rgba(0,255,204,0.15);
      border-color: #00ffcc;
      color: #00ffcc;
      box-shadow: 0 0 12px rgba(0,255,204,0.3);
    }
    .tab-btn mat-icon { font-size: 18px; width: 18px; height: 18px; }

    .tab-content { position: relative; z-index: 1; }

    /* Cards */
    .cards-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }
    .traveller-card {
      background: rgba(10,14,39,0.8);
      border: 1px solid rgba(0,255,204,0.2);
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 16px;
    }
    .traveller-card-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
      padding-bottom: 12px;
      border-bottom: 1px solid rgba(0,255,204,0.15);
    }
    .card-icon { color: #00ffcc; font-size: 1.2rem; }
    .traveller-card-header h3 { margin: 0; font-size: 1rem; color: #00ffcc; letter-spacing: 1px; }

    .summary-card .card-header { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
    .summary-card .card-header mat-icon { color: #00ffcc; }
    .summary-card .card-header h3 { margin: 0; font-size: 0.85rem; color: #88aacc; }
    .card-value { font-size: 1.8rem; font-weight: bold; color: #00ffcc; }
    .card-value.positive { color: #44ff88; }
    .card-value.negative { color: #ff4444; }

    /* Warning */
    .traveller-warning-card {
      background: rgba(255,68,68,0.1);
      border: 1px solid rgba(255,68,68,0.5);
      border-radius: 8px;
      padding: 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 24px;
    }
    .traveller-warning-card mat-icon { color: #ff4444; font-size: 24px; }
    .warning-content { display: flex; flex-direction: column; gap: 4px; }
    .warning-title { color: #ff4444; font-weight: bold; font-size: 0.9rem; }
    .warning-text { color: #ffaaaa; font-size: 0.8rem; }

    /* Accounts Grid */
    .accounts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
    }
    .account-item {
      background: rgba(0,255,204,0.05);
      border: 1px solid rgba(0,255,204,0.15);
      border-radius: 8px;
      padding: 16px;
    }
    .account-label { font-size: 0.8rem; color: #88aacc; margin-bottom: 8px; }
    .account-value { font-size: 1.4rem; font-weight: bold; color: #00ffcc; }

    /* P&L Breakdown */
    .breakdown-section { display: flex; flex-direction: column; gap: 12px; }
    .breakdown-row { display: flex; justify-content: space-between; align-items: center; }
    .breakdown-row.total { font-weight: bold; padding-top: 8px; }
    .breakdown-label { color: #88aacc; font-size: 0.9rem; }
    .breakdown-value { font-size: 1rem; font-weight: 600; }
    .breakdown-value.positive { color: #44ff88; }
    .breakdown-value.negative { color: #ff4444; }
    .net-profit-card { text-align: center; }
    .net-profit-value { font-size: 2.5rem; font-weight: bold; padding: 24px; }
    .net-profit-value.positive { color: #44ff88; }
    .net-profit-value.negative { color: #ff4444; }

    /* Tables */
    .traveller-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.85rem;
    }
    .traveller-table th {
      text-align: left;
      padding: 12px 8px;
      border-bottom: 1px solid rgba(0,255,204,0.3);
      color: #00ffcc;
      font-size: 0.8rem;
      text-transform: uppercase;
      letter-spacing: 1px;
    }
    .traveller-table td {
      padding: 10px 8px;
      border-bottom: 1px solid rgba(0,255,204,0.1);
      color: #c0c0c0;
    }
    .traveller-table tr:hover { background: rgba(0,255,204,0.05); }
    .type-badge {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 0.75rem;
      background: rgba(0,255,204,0.15);
      color: #00ffcc;
    }
    .status-badge {
      display: inline-block;
      padding: 2px 8px;
      border-radius: 4px;
      font-size: 0.75rem;
    }
    .status-badge.active { background: rgba(68,255,136,0.15); color: #44ff88; }

    /* Active Contract Item */
    .active-contract-item {
      padding: 16px;
      border-bottom: 1px solid rgba(0,255,204,0.1);
    }
    .contract-title { font-size: 1rem; font-weight: bold; color: #00ffcc; margin-bottom: 8px; }
    .contract-meta { display: flex; gap: 12px; align-items: center; margin-bottom: 12px; color: #88aacc; font-size: 0.85rem; }
    .progress-bar-container {
      position: relative;
      height: 8px;
      background: rgba(0,255,204,0.1);
      border-radius: 4px;
      margin-bottom: 12px;
      overflow: hidden;
    }
    .progress-bar {
      height: 100%;
      background: linear-gradient(90deg, #00ffcc, #44ff88);
      border-radius: 4px;
      transition: width 0.3s;
    }
    .progress-label {
      position: absolute;
      right: 8px;
      top: -20px;
      font-size: 0.75rem;
      color: #88aacc;
    }
    .crew-assignment { display: flex; justify-content: space-between; align-items: center; }
    .crew-label { font-size: 0.85rem; color: #88aacc; }

    /* Assign Dialog */
    .assign-dialog-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.7);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }
    .assign-dialog {
      background: rgba(10,14,39,0.95);
      border: 1px solid rgba(0,255,204,0.3);
      border-radius: 12px;
      padding: 24px;
      min-width: 400px;
      max-width: 600px;
    }
    .assign-dialog h3 { margin: 0 0 16px; color: #00ffcc; }
    .crew-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 12px;
      border-bottom: 1px solid rgba(0,255,204,0.1);
    }
    .crew-name { color: #00ffcc; font-weight: 600; }
    .crew-position { color: #88aacc; font-size: 0.85rem; margin-left: 12px; }

    /* Payroll */
    .payroll-summary { display: flex; flex-direction: column; gap: 16px; }
    .payroll-item { display: flex; justify-content: space-between; align-items: center; }
    .payroll-label { color: #88aacc; }
    .payroll-value { font-size: 1.2rem; font-weight: bold; color: #00ffcc; }

    .empty { color: #88aacc; text-align: center; padding: 24px; }
    .loading { color: #88aacc; text-align: center; padding: 48px; position: relative; z-index: 1; }
  `,
})
export class FinanceComponent implements OnInit {
  activeTab = signal<TabKey>('treasury');

  tabs: { key: TabKey; label: string; icon: string }[] = [
    { key: 'treasury', label: 'Treasury', icon: 'account_balance_wallet' },
    { key: 'pnl', label: 'P&L', icon: 'show_chart' },
    { key: 'contracts', label: 'Contracts', icon: 'assignment' },
    { key: 'crew', label: 'Crew', icon: 'people' },
  ];

  // Treasury signals
  loading = signal(true);
  creditBalance = signal(0);
  totalRevenue = signal(0);
  totalExpenses = signal(0);
  netProfit = signal(0);
  isInsolvent = signal(false);
  totalDailyPayroll = signal(0);
  periodPayroll = signal(0);
  activeHireCount = signal(0);
  transactions = signal<FinanceTransaction[]>([]);

  // P&L signal
  breakdown = signal<FinancialBreakdown | null>(null);

  // Contracts signals
  availableContracts = signal<ContractSummary[]>([]);
  activeContracts = signal<ContractSummary[]>([]);
  assigningContractId = signal<number | null>(null);

  // Crew signals
  crewMembers = signal<CrewSummary[]>([]);
  payrollSummary = signal<PayrollSummary | null>(null);

  constructor(private corpService: CorporationService) {}

  ngOnInit(): void {
    this.loadAllData();
  }

  loadAllData(): void {
    this.loading.set(true);

    // Load financial report for treasury
    this.corpService.loadFinancialReport();
    this.corpService.financialReport.subscribe({
      next: (report) => {
        if (report) {
          this.creditBalance.set(report.creditBalance);
          this.totalRevenue.set(report.totalRevenue);
          this.totalExpenses.set(report.totalExpenses);
          this.netProfit.set(report.netProfit);
          this.isInsolvent.set(report.isInsolvent);
        }
      },
    });

    // Load transactions
    this.corpService.getTransactions().subscribe({
      next: (txns) => this.transactions.set(txns),
    });

    // Load financial breakdown for P&L
    this.corpService.getFinancialBreakdown().subscribe({
      next: (b) => {
        this.breakdown.set(b);
        this.totalDailyPayroll.set(b.totalDailyPayroll);
        this.activeHireCount.set(b.activeHireCount);
      },
    });

    // Load payroll summary
    this.corpService.getPayrollSummary().subscribe({
      next: (p) => {
        this.payrollSummary.set(p);
        this.periodPayroll.set(p.periodPayroll);
      },
    });

    // Load office state for contracts and crew
    this.corpService.loadOfficeState();
    this.corpService.state.subscribe({
      next: (state) => {
        if (state) {
          this.availableContracts.set(state.availableContracts || []);
          this.activeContracts.set(state.activeContracts || []);
          this.crewMembers.set(state.activeHires || []);
        }
        this.loading.set(false);
      },
    });
  }

  // Contract actions
  acceptContract(contractId: number): void {
    this.corpService.acceptContract(contractId).subscribe({
      next: () => this.loadAllData(),
    });
  }

  estimateCost(contract: ContractSummary): number {
    const dailyCost = this.crewMembers().reduce((sum, h) => sum + h.dailySalary, 0);
    return Math.round((dailyCost * contract.durationTurns) / 7);
  }

  calculateRoi(contract: ContractSummary): number {
    return contract.payment - this.estimateCost(contract);
  }

  // Crew assignment
  openAssignDialog(contractId: number): void {
    this.assigningContractId.set(contractId);
  }

  closeAssignDialog(): void {
    this.assigningContractId.set(null);
  }

  assignCrew(hiringId: number): void {
    const contractId = this.assigningContractId();
    if (contractId == null) return;
    this.corpService.assignCrewToContract(contractId, hiringId).subscribe({
      next: () => {
        this.closeAssignDialog();
        this.loadAllData();
      },
    });
  }
}

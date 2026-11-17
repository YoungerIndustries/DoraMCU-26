import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatBottomSheetModule } from '@angular/material/bottom-sheet';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MediaMatcher } from '@angular/cdk/layout';

@Component({
  selector: 'app-mobile-nav',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule, RouterLink, RouterLinkActive],
  template: `
    @if (isHandset) {
      <nav class="mobile-nav">
        <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" class="nav-item">
          <mat-icon>dashboard</mat-icon>
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
    }
  `,
  styles: `
    .mobile-nav {
      position: fixed;
      bottom: 0;
      left: 0;
      right: 0;
      display: flex;
      justify-content: space-around;
      align-items: center;
      background: var(--bg-surface);
      border-top: 1px solid var(--border-subtle);
      box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.3);
      z-index: 1000;
      padding: 4px 0;
      padding-bottom: env(safe-area-inset-bottom, 0);
    }
    .nav-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: var(--text-hint);
      text-decoration: none;
      padding: 8px 4px;
      min-width: 48px;
      min-height: 48px;
      border-radius: var(--radius-sm);
      transition: color var(--transition-fast), background var(--transition-fast);
      position: relative;
    }
    .nav-item:active {
      background: var(--bg-elevated);
    }
    .nav-item.active {
      color: var(--mat-primary);
    }
    .nav-item.active::before {
      content: '';
      position: absolute;
      top: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 24px;
      height: 2px;
      background: var(--mat-primary);
      border-radius: 1px;
    }
    .nav-item mat-icon {
      font-size: 22px;
      width: 22px;
      height: 22px;
    }
    .nav-item span {
      font-size: 0.6rem;
      margin-top: 3px;
      font-weight: 500;
    }
  `,
})
export class MobileNavComponent {
  isHandset = false;

  private mobileQuery: MediaQueryList;
  private mobileQueryListener: () => void;

  constructor(private media: MediaMatcher) {
    this.mobileQuery = this.media.matchMedia('(max-width: 768px)');
    this.mobileQueryListener = () => {
      this.isHandset = this.mobileQuery.matches;
    };
  }

  ngOnInit(): void {
    this.mobileQuery.addEventListener('change', this.mobileQueryListener);
    this.isHandset = this.mobileQuery.matches;
  }

  ngOnDestroy(): void {
    this.mobileQuery.removeEventListener('change', this.mobileQueryListener);
  }
}

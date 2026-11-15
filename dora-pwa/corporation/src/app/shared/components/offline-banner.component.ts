import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MediaMatcher } from '@angular/cdk/layout';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-offline-banner',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  template: `
    @if (isOffline && isHandset) {
      <div class="offline-banner">
        <mat-icon>wifi_off</mat-icon>
        <span>You are offline. Some features may be unavailable.</span>
        @if (lastUpdated) {
          <span class="last-updated">Last updated: {{ lastUpdated }}</span>
        }
      </div>
    }
  `,
  styles: `
    .offline-banner {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      background: #ff6f00;
      color: #fff;
      padding: 8px 16px;
      z-index: 1001;
      font-size: 0.875rem;
    }
    .offline-banner mat-icon {
      font-size: 20px;
      height: 20px;
      width: 20px;
    }
    .last-updated {
      margin-left: auto;
      font-size: 0.75rem;
      opacity: 0.8;
    }
  `,
})
export class OfflineBannerComponent implements OnInit, OnDestroy {
  isOffline = false;
  isHandset = false;
  lastUpdated: string | null = null;

  private onlineSub?: Subscription;
  private offlineSub?: Subscription;
  private mobileQuery: MediaQueryList;
  private mobileQueryListener: () => void;

  constructor(private media: MediaMatcher) {
    this.mobileQuery = this.media.matchMedia('(max-width: 768px)');
    this.mobileQueryListener = () => {
      this.isHandset = this.mobileQuery.matches;
    };
  }

  ngOnInit(): void {
    this.isHandset = this.mobileQuery.matches;
    this.mobileQuery.addEventListener('change', this.mobileQueryListener);

    this.isOffline = !navigator.onLine;
    if (this.isOffline) {
      this.lastUpdated = new Date().toLocaleTimeString();
    }

    this.onlineSub = new Subscription();
    this.offlineSub = new Subscription();

    window.addEventListener('online', () => {
      this.isOffline = false;
      this.lastUpdated = null;
    });

    window.addEventListener('offline', () => {
      this.isOffline = true;
      this.lastUpdated = new Date().toLocaleTimeString();
    });
  }

  ngOnDestroy(): void {
    this.mobileQuery.removeEventListener('change', this.mobileQueryListener);
    this.onlineSub?.unsubscribe();
    this.offlineSub?.unsubscribe();
  }
}

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DesktopAppComponent } from './shell/desktop/desktop-app.component';
import { MobileAppComponent } from './shell/mobile/mobile-app.component';
import { PlatformDetectionService } from './core/services/platform-detection.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    DesktopAppComponent,
    MobileAppComponent,
  ],
  template: `
    @if (isDesktop) {
      <app-desktop-app></app-desktop-app>
    } @else {
      <app-mobile-app></app-mobile-app>
    }
  `,
  styles: `
    :host {
      display: block;
      height: 100vh;
      width: 100vw;
      overflow: hidden;
    }
  `,
})
export class AppComponent {
  isDesktop = true;

  constructor(private platformService: PlatformDetectionService) {
    // Initialize from the service
    this.isDesktop = this.platformService.isDesktop();

    // Listen for platform changes (e.g., window resize)
    this.platformService.isDesktop$.subscribe((isDesktop) => {
      this.isDesktop = isDesktop;
    });
  }
}

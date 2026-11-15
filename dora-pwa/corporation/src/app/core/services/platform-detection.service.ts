import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { MediaMatcher } from '@angular/cdk/layout';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PlatformDetectionService {
  private readonly breakpoint = 768;

  private isDesktopSubject = new BehaviorSubject<boolean>(true);
  readonly isDesktop$ = this.isDesktopSubject.asObservable();

  private isMobileSubject = new BehaviorSubject<boolean>(false);
  readonly isMobile$ = this.isMobileSubject.asObservable();

  constructor(
    @Inject(PLATFORM_ID) private platformId: Object,
    private media: MediaMatcher,
  ) {
    if (isPlatformBrowser(platformId)) {
      this.initializeDetection();
    }
  }

  private initializeDetection(): void {
    const mobileQuery = this.media.matchMedia(`(max-width: ${this.breakpoint}px)`);
    const isDesktop = !mobileQuery.matches;
    this.isDesktopSubject.next(isDesktop);
    this.isMobileSubject.next(!isDesktop);

    mobileQuery.addEventListener('change', (event) => {
      const isDesktop = !event.matches;
      this.isDesktopSubject.next(isDesktop);
      this.isMobileSubject.next(!isDesktop);
    });
  }

  /**
   * Check if the current platform is desktop
   */
  isDesktop(): boolean {
    return this.isDesktopSubject.value;
  }

  /**
   * Check if the current platform is mobile
   */
  isMobile(): boolean {
    return this.isMobileSubject.value;
  }
}

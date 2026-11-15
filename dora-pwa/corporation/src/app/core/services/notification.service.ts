import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Observable, of, BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private permissionSubject = new BehaviorSubject<NotificationPermission>('default');

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  get permission$(): Observable<NotificationPermission> {
    return this.permissionSubject.asObservable();
  }

  init(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.checkPermission();
  }

  private checkPermission(): void {
    if (!('Notification' in window)) {
      this.permissionSubject.next('denied');
      return;
    }
    this.permissionSubject.next(Notification.permission);
  }

  requestPermission(): Observable<boolean> {
    if (!isPlatformBrowser(this.platformId)) {
      return of(false);
    }

    if (!('Notification' in window)) {
      this.permissionSubject.next('denied');
      return of(false);
    }

    if (Notification.permission === 'granted') {
      return of(true);
    }

    return new Observable((observer) => {
      Notification.requestPermission().then((permission) => {
        this.permissionSubject.next(permission);
        observer.next(permission === 'granted');
        observer.complete();
      });
    });
  }

  notify(title: string, options?: NotificationOptions): void {
    if (!isPlatformBrowser(this.platformId)) return;

    if (Notification.permission === 'granted') {
      new Notification(title, {
        icon: '/corporation/favicon.ico',
        badge: '/corporation/favicon.ico',
        ...options,
      });
    }
  }

  // Map SSE corporation events to notifications
  handleCorporationEvent(event: { type?: string; message?: string; severity?: string }): void {
    if (!event.type || !event.message) return;

    const shouldNotify =
      event.type === 'CORPORATION_EMERGENCY' ||
      event.type === 'CORPORATION_INSOLVENCY_WARNING' ||
      event.type === 'CORPORATION_CONTRACT_COMPLETED' ||
      (event.severity === 'CRITICAL' && event.type === 'CORPORATION_CREW_ISSUE');

    if (shouldNotify) {
      const title = this.getEventTitle(event.type);
      const icon = this.getEventIcon(event.type);

      this.notify(title, {
        body: event.message,
        icon: icon,
        tag: event.type,
        requireInteraction: event.severity === 'CRITICAL',
      });
    }
  }

  private getEventTitle(type?: string): string {
    switch (type) {
      case 'CORPORATION_EMERGENCY':
        return '🚨 Corporation Emergency';
      case 'CORPORATION_INSOLVENCY_WARNING':
        return '⚠️ Insolvency Warning';
      case 'CORPORATION_CONTRACT_COMPLETED':
        return '✅ Contract Completed';
      case 'CORPORATION_CREW_ISSUE':
        return '👥 Crew Issue';
      default:
        return 'Corporation Update';
    }
  }

  private getEventIcon(type?: string): string {
    switch (type) {
      case 'CORPORATION_EMERGENCY':
        return '/corporation/assets/emergency.png';
      case 'CORPORATION_INSOLVENCY_WARNING':
        return '/corporation/assets/warning.png';
      default:
        return '/corporation/favicon.ico';
    }
  }
}

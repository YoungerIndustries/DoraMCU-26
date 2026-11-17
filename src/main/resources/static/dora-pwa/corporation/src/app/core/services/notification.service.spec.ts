import { TestBed } from '@angular/core/testing';
import { PLATFORM_ID } from '@angular/core';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        NotificationService,
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    });
    service = TestBed.inject(NotificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('init', () => {
    it('should check permission on init', () => {
      // Mock Notification API
      (window as any).Notification = {
        permission: 'default',
        requestPermission: jasmine.createSpy('requestPermission').and.returnValue(
          Promise.resolve('granted')
        ),
      };

      service.init();
      service.permission$.subscribe((perm) => {
        expect(perm).toBe('default');
      });
    });
  });

  describe('requestPermission', () => {
    it('should return false when Notification is not supported', () => {
      delete (window as any).Notification;

      service.requestPermission().subscribe((result) => {
        expect(result).toBeFalse();
      });
    });

    it('should return true when permission is already granted', () => {
      (window as any).Notification = {
        permission: 'granted',
      };

      service.requestPermission().subscribe((result) => {
        expect(result).toBeTrue();
      });
    });

    it('should request permission when not granted', () => {
      const mockRequest = jasmine.createSpy('requestPermission').and.returnValue(
        Promise.resolve('granted')
      );
      (window as any).Notification = {
        permission: 'default',
        requestPermission: mockRequest,
      };

      service.requestPermission().subscribe((result) => {
        expect(result).toBeTrue();
        expect(mockRequest).toHaveBeenCalled();
      });
    });

    it('should return false when permission is denied', () => {
      const mockRequest = jasmine.createSpy('requestPermission').and.returnValue(
        Promise.resolve('denied')
      );
      (window as any).Notification = {
        permission: 'default',
        requestPermission: mockRequest,
      };

      service.requestPermission().subscribe((result) => {
        expect(result).toBeFalse();
      });
    });
  });

  describe('notify', () => {
    it('should create a notification when permission is granted', () => {
      const mockNotification = jasmine.createSpy('Notification');
      (window as any).Notification = {
        permission: 'granted',
      };
      (window as any).Notification.prototype.constructor = mockNotification;

      // Override new Notification
      const originalNotification = window.Notification;
      (window as any).Notification = class {
        constructor(title: string, options?: NotificationOptions) {
          expect(title).toBe('Test Title');
        }
        static permission = 'granted';
      };

      service.notify('Test Title', { body: 'Test body' });

      // Restore
      (window as any).Notification = originalNotification;
    });

    it('should not create notification when permission is not granted', () => {
      (window as any).Notification = {
        permission: 'denied',
      };

      // Should not throw
      service.notify('Test Title');
    });
  });

  describe('handleCorporationEvent', () => {
    beforeEach(() => {
      // Mock Notification to avoid errors
      (window as any).Notification = {
        permission: 'granted',
      };
    });

    it('should notify on CORPORATION_EMERGENCY', () => {
      const notifySpy = spyOn(service, 'notify');

      service.handleCorporationEvent({
        type: 'CORPORATION_EMERGENCY',
        message: 'Emergency alert',
        severity: 'CRITICAL',
      });

      expect(notifySpy).toHaveBeenCalled();
    });

    it('should notify on CORPORATION_INSOLVENCY_WARNING', () => {
      const notifySpy = spyOn(service, 'notify');

      service.handleCorporationEvent({
        type: 'CORPORATION_INSOLVENCY_WARNING',
        message: 'Insolvency warning',
        severity: 'WARNING',
      });

      expect(notifySpy).toHaveBeenCalled();
    });

    it('should notify on CORPORATION_CONTRACT_COMPLETED', () => {
      const notifySpy = spyOn(service, 'notify');

      service.handleCorporationEvent({
        type: 'CORPORATION_CONTRACT_COMPLETED',
        message: 'Contract completed',
        severity: 'INFO',
      });

      expect(notifySpy).toHaveBeenCalled();
    });

    it('should notify on CRITICAL CORPORATION_CREW_ISSUE', () => {
      const notifySpy = spyOn(service, 'notify');

      service.handleCorporationEvent({
        type: 'CORPORATION_CREW_ISSUE',
        message: 'Crew issue',
        severity: 'CRITICAL',
      });

      expect(notifySpy).toHaveBeenCalled();
    });

    it('should not notify on non-critical CORPORATION_CREW_ISSUE', () => {
      const notifySpy = spyOn(service, 'notify');

      service.handleCorporationEvent({
        type: 'CORPORATION_CREW_ISSUE',
        message: 'Minor crew issue',
        severity: 'INFO',
      });

      expect(notifySpy).not.toHaveBeenCalled();
    });

    it('should not notify when event has no type or message', () => {
      const notifySpy = spyOn(service, 'notify');

      service.handleCorporationEvent({});
      expect(notifySpy).not.toHaveBeenCalled();

      service.handleCorporationEvent({ type: 'SOME_EVENT' });
      expect(notifySpy).not.toHaveBeenCalled();

      service.handleCorporationEvent({ message: 'Some message' });
      expect(notifySpy).not.toHaveBeenCalled();
    });
  });

  describe('permission$', () => {
    it('should emit permission changes', () => {
      let emitted = false;
      service.permission$.subscribe((perm) => {
        emitted = true;
        expect(typeof perm).toBe('string');
      });

      // Initial emission from init
      service.init();
    });
  });
});

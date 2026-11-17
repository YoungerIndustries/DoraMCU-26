import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { MediaMatcher } from '@angular/cdk/layout';
import { PlatformDetectionService } from './platform-detection.service';

describe('PlatformDetectionService', () => {
  let service: PlatformDetectionService;
  let mediaMatcher: jasmine.SpyObj<MediaMatcher>;

  function createMockMediaQuery(matches: boolean): MediaQueryList {
    const listeners: Array<(e: MediaQueryListEvent) => void> = [];
    return {
      matches,
      media: '(max-width: 768px)',
      addEventListener: jasmine.createSpy('addEventListener').and.callFake((event, listener) => {
        if (event === 'change') listeners.push(listener as any);
      }),
      removeEventListener: jasmine.createSpy('removeEventListener'),
      dispatchChange: (newMatches: boolean) => {
        listeners.forEach((fn) => fn({ matches: newMatches } as MediaQueryListEvent));
      },
    } as unknown as MediaQueryList;
  }

  beforeEach(() => {
    const mockMediaQuery = createMockMediaQuery(false); // default: desktop
    mediaMatcher = jasmine.createSpyObj('MediaMatcher', ['matchMedia']);
    mediaMatcher.matchMedia.and.returnValue(mockMediaQuery);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        PlatformDetectionService,
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: MediaMatcher, useValue: mediaMatcher },
      ],
    });

    service = TestBed.inject(PlatformDetectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should detect desktop by default', () => {
    expect(service.isDesktop()).toBeTrue();
    expect(service.isMobile()).toBeFalse();
  });

  it('should detect mobile when screen width is below breakpoint', () => {
    const mockMediaQuery = createMockMediaQuery(true); // mobile
    const mobileMatcher = jasmine.createSpyObj('MediaMatcher', ['matchMedia']);
    mobileMatcher.matchMedia.and.returnValue(mockMediaQuery);

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        PlatformDetectionService,
        { provide: PLATFORM_ID, useValue: 'browser' },
        { provide: MediaMatcher, useValue: mobileMatcher },
      ],
    });

    service = TestBed.inject(PlatformDetectionService);
    expect(service.isDesktop()).toBeFalse();
    expect(service.isMobile()).toBeTrue();
  });

  it('should not initialize detection on server-side (non-browser)', () => {
    const serverMatcher = jasmine.createSpyObj('MediaMatcher', ['matchMedia']);

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        PlatformDetectionService,
        { provide: PLATFORM_ID, useValue: 'server' },
        { provide: MediaMatcher, useValue: serverMatcher },
      ],
    });

    service = TestBed.inject(PlatformDetectionService);
    expect(service.isDesktop()).toBeTrue();
    expect(service.isMobile()).toBeFalse();
    expect(serverMatcher.matchMedia).not.toHaveBeenCalled();
  });

  it('should expose isDesktop$ observable', () => {
    service.isDesktop$.subscribe((isDesktop) => {
      expect(typeof isDesktop).toBe('boolean');
    });
  });

  it('should expose isMobile$ observable', () => {
    service.isMobile$.subscribe((isMobile) => {
      expect(typeof isMobile).toBe('boolean');
    });
  });
});

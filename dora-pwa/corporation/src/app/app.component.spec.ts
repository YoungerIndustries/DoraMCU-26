import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { MediaMatcher } from '@angular/cdk/layout';
import { AppComponent } from './app.component';
import { PlatformDetectionService } from './core/services/platform-detection.service';
import { DesktopAppComponent } from './shell/desktop/desktop-app.component';
import { MobileAppComponent } from './shell/mobile/mobile-app.component';

describe('AppComponent', () => {
  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;
  let platformService: jasmine.SpyObj<PlatformDetectionService>;
  let mediaMatcher: jasmine.SpyObj<MediaMatcher>;

  beforeEach(async () => {
    const mockMediaQuery = jasmine.createSpyObj('MediaQueryList', ['addEventListener', 'removeEventListener']);
    mockMediaQuery.matches = false; // desktop

    mediaMatcher = jasmine.createSpyObj('MediaMatcher', ['matchMedia']);
    mediaMatcher.matchMedia.and.returnValue(mockMediaQuery);

    const isDesktop$Subscribe = jasmine.createSpy('subscribe');
    platformService = jasmine.createSpyObj('PlatformDetectionService', ['isDesktop', 'isMobile']);
    platformService.isDesktop.and.returnValue(true);
    (platformService as any).isDesktop$ = { subscribe: isDesktop$Subscribe };

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        AppComponent,
        DesktopAppComponent,
        MobileAppComponent,
      ],
      providers: [
        { provide: PlatformDetectionService, useValue: platformService },
        { provide: MediaMatcher, useValue: mediaMatcher },
        { provide: PLATFORM_ID, useValue: 'browser' },
        provideRouter([]),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize isDesktop from PlatformDetectionService', () => {
    expect(component.isDesktop).toBeTrue();
    expect(platformService.isDesktop).toHaveBeenCalled();
  });

  it('should subscribe to isDesktop$ observable', () => {
    expect((platformService as any).isDesktop$.subscribe).toHaveBeenCalled();
  });

  it('should render desktop app when isDesktop is true', () => {
    component.isDesktop = true;
    fixture.detectChanges();

    const desktopEl = fixture.nativeElement.querySelector('app-desktop-app');
    expect(desktopEl).toBeTruthy();
  });

  it('should render mobile app when isDesktop is false', () => {
    component.isDesktop = false;
    fixture.detectChanges();

    const mobileEl = fixture.nativeElement.querySelector('app-mobile-app');
    expect(mobileEl).toBeTruthy();
  });

  it('should switch from desktop to mobile when isDesktop changes', () => {
    component.isDesktop = false;
    fixture.detectChanges();

    const mobileEl = fixture.nativeElement.querySelector('app-mobile-app');
    const desktopEl = fixture.nativeElement.querySelector('app-desktop-app');
    expect(mobileEl).toBeTruthy();
    expect(desktopEl).toBeNull();
  });

  it('should switch from mobile to desktop when isDesktop changes', () => {
    component.isDesktop = false;
    fixture.detectChanges();

    component.isDesktop = true;
    fixture.detectChanges();

    const desktopEl = fixture.nativeElement.querySelector('app-desktop-app');
    const mobileEl = fixture.nativeElement.querySelector('app-mobile-app');
    expect(desktopEl).toBeTruthy();
    expect(mobileEl).toBeNull();
  });
});

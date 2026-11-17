import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter, NavigationEnd } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { MediaMatcher } from '@angular/cdk/layout';
import { MobileAppComponent } from './mobile-app.component';
import { CorporationService } from '../../core/services/corporation.service';
import { OfflineBannerComponent } from '../../shared/components/offline-banner.component';
import { CorporationState } from '../../core/models/corporation-state';
import { of } from 'rxjs';

describe('MobileAppComponent', () => {
  let component: MobileAppComponent;
  let fixture: ComponentFixture<MobileAppComponent>;
  let corpService: jasmine.SpyObj<CorporationService>;
  let mediaMatcher: jasmine.SpyObj<MediaMatcher>;

  const mockState: CorporationState = {
    corporationId: 1,
    corporationName: 'Test Corp',
    characterName: 'Test Character',
    creditBalance: 10000,
    totalSalaryObligations: 500,
    isInsolvent: false,
    activeContracts: [],
    availableContracts: [],
    activeHires: [],
    availableAiCharacters: [],
    officeBuildingLevel: 1,
    currentRole: 'COMMANDER',
    aiAssistantActive: false,
  };

  function createMockMediaQuery(matches: boolean): MediaQueryList {
    const listeners: Array<(e: MediaQueryListEvent) => void> = [];
    return {
      matches,
      media: '(max-width: 768px)',
      addEventListener: jasmine.createSpy('addEventListener').and.callFake((event, listener) => {
        if (event === 'change') listeners.push(listener as any);
      }),
      removeEventListener: jasmine.createSpy('removeEventListener').and.callFake((event, listener) => {
        // no-op
      }),
      dispatchChange: (newMatches: boolean) => {
        listeners.forEach((fn) => fn({ matches: newMatches } as MediaQueryListEvent));
      },
    } as unknown as MediaQueryList;
  }

  function createMockRouter(url: string): object {
    return {
      url,
      events: of(new NavigationEnd(1, url, url)),
      navigate: jasmine.createSpy('navigate'),
    };
  }

  beforeEach(async () => {
    const mockMediaQuery = createMockMediaQuery(true); // mobile
    mediaMatcher = jasmine.createSpyObj('MediaMatcher', ['matchMedia']);
    mediaMatcher.matchMedia.and.returnValue(mockMediaQuery);

    corpService = jasmine.createSpyObj('CorporationService', ['loadOfficeState', 'loadFinancialReport', 'subscribeToEvents']);
    (corpService as any).state = () => mockState;

    const mockRouter = createMockRouter('/dashboard');

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        MobileAppComponent,
        OfflineBannerComponent,
      ],
      providers: [
        { provide: CorporationService, useValue: corpService },
        { provide: Router, useValue: mockRouter },
        { provide: MediaMatcher, useValue: mediaMatcher },
        { provide: PLATFORM_ID, useValue: 'browser' },
        provideRouter([]),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    localStorage.setItem('currentCharacterId', '1');
    fixture = TestBed.createComponent(MobileAppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load office state on init', () => {
    expect(corpService.loadOfficeState).toHaveBeenCalled();
  });

  it('should load financial report on init', () => {
    expect(corpService.loadFinancialReport).toHaveBeenCalled();
  });

  it('should subscribe to events on init', () => {
    expect(corpService.subscribeToEvents).toHaveBeenCalled();
  });

  it('should set page title for contracts', () => {
    (component as any).router.url = '/contracts';
    component.updatePageTitle();
    expect(component.pageTitle).toBe('Contracts');
  });

  it('should set page title for dashboard', () => {
    (component as any).router.url = '/dashboard';
    component.updatePageTitle();
    expect(component.pageTitle).toBe('Dashboard');
  });

  it('should set page title for character selection', () => {
    (component as any).router.url = '/';
    component.updatePageTitle();
    expect(component.pageTitle).toBe('Select Character');
  });

  it('should set page title for crew', () => {
    (component as any).router.url = '/crew';
    component.updatePageTitle();
    expect(component.pageTitle).toBe('Crew');
  });

  it('should set page title for finance', () => {
    (component as any).router.url = '/finance';
    component.updatePageTitle();
    expect(component.pageTitle).toBe('Finance');
  });

  it('should set page title for AI actions', () => {
    (component as any).router.url = '/ai-actions';
    component.updatePageTitle();
    expect(component.pageTitle).toBe('AI Actions');
  });

  it('should set page title for role switch', () => {
    (component as any).router.url = '/role';
    component.updatePageTitle();
    expect(component.pageTitle).toBe('Role Switch');
  });

  it('should set page title for character', () => {
    (component as any).router.url = '/character';
    component.updatePageTitle();
    expect(component.pageTitle).toBe('Character');
  });

  it('should set default page title for unknown route', () => {
    (component as any).router.url = '/unknown';
    component.updatePageTitle();
    expect(component.pageTitle).toBe('DoraMCU Corporation');
  });

  it('should update character name when character ID is set', () => {
    localStorage.setItem('currentCharacterId', '1');
    component.updateCharacterName();
    expect(component.activeCharacterName).toBe('Test Character');
  });

  it('should clear character name when no character ID is set', () => {
    localStorage.removeItem('currentCharacterId');
    component.updateCharacterName();
    expect(component.activeCharacterName).toBeNull();
  });
});

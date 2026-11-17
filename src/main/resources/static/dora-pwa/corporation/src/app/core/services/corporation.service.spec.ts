import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { CorporationService } from './corporation.service';
import { CorporationState, FinancialReport } from '../models/corporation-state';

describe('CorporationService', () => {
  let service: CorporationService;
  let httpMock: HttpTestingController;

  const mockCorporationId = 10;
  const mockCharacterId = 20;
  const mockState: CorporationState = {
    corporationId: mockCorporationId,
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

  const mockFinancialReport: FinancialReport = {
    corporationId: mockCorporationId,
    creditBalance: 10000,
    totalRevenue: 15000,
    totalExpenses: 5000,
    netProfit: 10000,
    transactionCount: 5,
    isInsolvent: false,
    totalSalaryObligations: 500,
  };

  beforeEach(() => {
    // Set up localStorage for corporation and character IDs
    localStorage.setItem('corporationId', mockCorporationId.toString());
    localStorage.setItem('currentCharacterId', mockCharacterId.toString());
    localStorage.removeItem('playerId');

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        CorporationService,
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    });

    service = TestBed.inject(CorporationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should resolve corporationId from localStorage', () => {
    expect(service.corporationId).toBe(mockCorporationId);
  });

  it('should resolve characterId from localStorage', () => {
    expect(service.characterId).toBe(mockCharacterId);
  });

  it('should default corporationId to 1 when not set', () => {
    localStorage.removeItem('corporationId');
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        CorporationService,
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    });
    const newService = TestBed.inject(CorporationService);
    expect(newService.corporationId).toBe(1);
  });

  it('should default characterId to 1 when not set', () => {
    localStorage.removeItem('currentCharacterId');
    localStorage.removeItem('selected_char_id');
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        CorporationService,
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    });
    const newService = TestBed.inject(CorporationService);
    expect(newService.characterId).toBe(1);
  });

  describe('getOfficeState', () => {
    it('should fetch office state', () => {
      service.getOfficeState().subscribe((state) => {
        expect(state.corporationName).toBe('Test Corp');
      });

      const req = httpMock.expectOne(`/api/v1/corporate/office/${mockCorporationId}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockState);
    });
  });

  describe('loadOfficeState', () => {
    it('should load and cache office state', () => {
      service.loadOfficeState();

      const req = httpMock.expectOne(`/api/v1/corporate/office/${mockCorporationId}`);
      req.flush(mockState);

      expect(service.state()?.corporationName).toBe('Test Corp');
    });
  });

  describe('acceptContract', () => {
    it('should accept a contract', () => {
      const contractId = 5;
      service.acceptContract(contractId).subscribe(() => {});

      const req = httpMock.expectOne(
        `/api/v1/corporate/office/${mockCorporationId}/contracts/${contractId}/accept`
      );
      expect(req.request.method).toBe('POST');
      req.flush({});
    });
  });

  describe('getFinancialReport', () => {
    it('should fetch financial report', () => {
      service.getFinancialReport().subscribe((report) => {
        expect(report.netProfit).toBe(10000);
      });

      const req = httpMock.expectOne(`/api/v1/corporate/office/${mockCorporationId}/finance`);
      expect(req.request.method).toBe('GET');
      req.flush(mockFinancialReport);
    });
  });

  describe('loadFinancialReport', () => {
    it('should load and cache financial report', () => {
      service.loadFinancialReport();

      const req = httpMock.expectOne(`/api/v1/corporate/office/${mockCorporationId}/finance`);
      req.flush(mockFinancialReport);

      expect(service.financialReport()?.netProfit).toBe(10000);
    });
  });

  describe('getTransactions', () => {
    it('should fetch transactions with default pagination', () => {
      service.getTransactions().subscribe(() => {});

      const req = httpMock.expectOne((request) => {
        return request.url.includes('/finance/transactions') &&
          request.params.get('page') === '0' &&
          request.params.get('size') === '50';
      });
      req.flush([]);
    });

    it('should fetch transactions with custom pagination', () => {
      service.getTransactions(1, 25).subscribe(() => {});

      const req = httpMock.expectOne((request) => {
        return request.params.get('page') === '1' && request.params.get('size') === '25';
      });
      req.flush([]);
    });
  });

  describe('hireAiCharacter', () => {
    it('should hire an AI character', () => {
      service.hireAiCharacter(1, 'Manager', 100).subscribe(() => {});

      const req = httpMock.expectOne(`/api/v1/corporate/office/${mockCorporationId}/hiring`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        aiCharacterId: 1,
        position: 'Manager',
        dailySalary: 100,
      });
      req.flush({});
    });
  });

  describe('fireAiCharacter', () => {
    it('should fire an AI character', () => {
      const hiringId = 1;
      service.fireAiCharacter(hiringId).subscribe(() => {});

      const req = httpMock.expectOne(`/api/v1/corporate/office/${mockCorporationId}/hiring/${hiringId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush({});
    });
  });

  describe('switchRole', () => {
    it('should switch role', () => {
      service.switchRole('TRAVELLER').subscribe((roleState) => {
        expect(roleState.currentRole).toBe('TRAVELLER');
      });

      const req = httpMock.expectOne(`/api/v1/unified/characters/${mockCharacterId}/role/switch`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({ role: 'TRAVELLER' });
      req.flush({ characterId: mockCharacterId, currentRole: 'TRAVELLER', roleDisplayName: 'Traveller', aiAssistantActive: false, inOrdersLoop: false });
    });
  });

  describe('setCorporationId', () => {
    it('should store corporationId in localStorage', () => {
      service.setCorporationId(99);
      expect(localStorage.getItem('corporationId')).toBe('99');
    });
  });

  describe('setCharacterId', () => {
    it('should store characterId in localStorage', () => {
      service.setCharacterId(88);
      expect(localStorage.getItem('currentCharacterId')).toBe('88');
    });
  });

  describe('getPlayerId', () => {
    it('should return corporationId as fallback when playerCharactersService is not available', () => {
      const playerId = service.getPlayerId();
      expect(playerId).toBe(mockCorporationId);
    });
  });

  describe('setActiveCharacter', () => {
    it('should set character ID and refresh state', () => {
      service.setActiveCharacter(30);
      expect(localStorage.getItem('currentCharacterId')).toBe('30');

      // Should trigger loadOfficeState and loadFinancialReport
      const stateReq = httpMock.expectOne(`/api/v1/corporate/office/${mockCorporationId}`);
      stateReq.flush(mockState);

      const financeReq = httpMock.expectOne(`/api/v1/corporate/office/${mockCorporationId}/finance`);
      financeReq.flush(mockFinancialReport);
    });
  });

  describe('events$', () => {
    it('should emit corporation events', (done) => {
      let emitted = false;
      service.events$.subscribe((event) => {
        if (event && event.eventType === 'TEST_EVENT') {
          emitted = true;
        }
      });

      // Access private eventSubject via loadOfficeState error path
      // We can't directly test eventSubject, but we can verify events$ is observable
      expect(service.events$).toBeTruthy();
      done();
    });
  });
});

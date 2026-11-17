import { Injectable, signal, Inject, Optional, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, BehaviorSubject } from 'rxjs';
import {
  CorporationState,
  FinancialReport,
  FinanceTransaction,
  FinancialBreakdown,
  PayrollSummary,
  ContractCostEstimate,
  AiAction,
  AiTurnSummary,
  CareerDetails,
  ContractStatus,
  RoleState,
  CorporationEvent,
} from '../models/corporation-state';
import {
  CharacterStats,
  QualifiedContract,
  CharacterAbilities,
  CharacterSkill,
  CharacterCareer,
  CharacterAssignment,
} from '../models/traveller-character';
import { NotificationService } from './notification.service';
import { PlayerCharactersService } from './player-characters.service';

@Injectable({
  providedIn: 'root',
})
export class CorporationService {
  private readonly apiBase = '/api/v1/corporate/office';
  private readonly unifiedBase = '/api/v1/unified/characters';

  private readonly _corporationId = this.getCorporationId();
  private readonly _characterId = this.getCharacterId();

  // Expose IDs for components that need them
  get corporationId(): number { return this._corporationId; }
  get characterId(): number { return this._characterId; }

  // SSE event stream
  private eventSubject = new BehaviorSubject<CorporationEvent | null>(null);
  public events$ = this.eventSubject.asObservable();

  // Cached state
  public state = signal<CorporationState | null>(null);
  public financialReport = signal<FinancialReport | null>(null);

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object,
    @Optional() private notificationService?: NotificationService,
    private playerCharactersService?: PlayerCharactersService,
  ) {}

  // ---- Corporation State ----

  getOfficeState(): Observable<CorporationState> {
    return this.http.get<CorporationState>(`${this.apiBase}/${this.corporationId}`);
  }

  loadOfficeState(): void {
    this.getOfficeState().subscribe({
      next: (state) => {
        this.state.set(state);
      },
      error: (err) => console.error('Failed to load office state', err),
    });
  }

  // ---- Contracts ----

  acceptContract(contractId: number): Observable<unknown> {
    return this.http.post(`${this.apiBase}/${this.corporationId}/contracts/${contractId}/accept`, null);
  }

  processContractTurn(contractId: number): Observable<unknown> {
    return this.http.post(`${this.apiBase}/contracts/${contractId}/process-turn`, null);
  }

  getContractStatus(contractId: number): Observable<ContractStatus> {
    return this.http.get<ContractStatus>(`${this.apiBase}/contracts/${contractId}/status`);
  }

  getContractCrew(contractId: number): Observable<unknown> {
    return this.http.get(`${this.apiBase}/contracts/${contractId}/crew`);
  }

  // ---- Crew ----

  hireAiCharacter(aiCharacterId: number, position: string, dailySalary: number): Observable<unknown> {
    return this.http.post(`${this.apiBase}/${this.corporationId}/hiring`, {
      aiCharacterId,
      position,
      dailySalary,
    });
  }

  fireAiCharacter(hiringId: number): Observable<unknown> {
    return this.http.delete(`${this.apiBase}/${this.corporationId}/hiring/${hiringId}`);
  }

  getCrewCareer(hiringId: number): Observable<CareerDetails> {
    return this.http.get<CareerDetails>(`/api/v1/corporate/crew/${hiringId}/career`);
  }

  promoteCrew(hiringId: number): Observable<CareerDetails> {
    return this.http.post<CareerDetails>(`/api/v1/corporate/crew/${hiringId}/promote`, null);
  }

  demoteCrew(hiringId: number): Observable<CareerDetails> {
    return this.http.post<CareerDetails>(`/api/v1/corporate/crew/${hiringId}/demote`, null);
  }

  // ---- Finance ----

  getFinancialReport(): Observable<FinancialReport> {
    return this.http.get<FinancialReport>(`${this.apiBase}/${this.corporationId}/finance`);
  }

  loadFinancialReport(): void {
    this.getFinancialReport().subscribe({
      next: (report) => {
        this.financialReport.set(report);
      },
      error: (err) => console.error('Failed to load financial report', err),
    });
  }

  getTransactions(page: number = 0, size: number = 50): Observable<FinanceTransaction[]> {
    return this.http.get<FinanceTransaction[]>(`${this.apiBase}/${this.corporationId}/finance/transactions`, {
      params: { page, size },
    });
  }

  getFinancialBreakdown(): Observable<FinancialBreakdown> {
    return this.http.get<FinancialBreakdown>(`${this.apiBase}/${this.corporationId}/finance/breakdown`);
  }

  getPayrollSummary(): Observable<PayrollSummary> {
    return this.http.get<PayrollSummary>(`${this.apiBase}/${this.corporationId}/finance/payroll`);
  }

  // ---- Contract Crew Assignment ----

  assignCrewToContract(contractId: number, hiringId: number): Observable<unknown> {
    return this.http.post(`${this.apiBase}/contracts/${contractId}/assign-crew`, { hiringId });
  }

  removeCrewFromContract(contractId: number, hiringId: number): Observable<unknown> {
    return this.http.post(`${this.apiBase}/contracts/${contractId}/remove-crew`, { hiringId });
  }

  /**
   * Estimate the cost of a contract based on crew salaries and duration.
   */
  estimateContractCost(contract: ContractSummary): ContractCostEstimate {
    const dailyCost = this.state()?.activeHires?.reduce((sum, hire) => sum + hire.dailySalary, 0) || 0;
    const estimatedCost = Math.round((dailyCost * contract.durationTurns) / 7);
    return {
      contractId: contract.id,
      title: contract.title,
      payment: contract.payment,
      estimatedCost,
      netRoi: contract.payment - estimatedCost,
      durationTurns: contract.durationTurns,
      difficulty: contract.difficulty,
    };
  }

  // ---- AI Assistant ----

  triggerAiTurn(): Observable<AiTurnSummary> {
    return this.http.post<AiTurnSummary>(`${this.apiBase}/../ai/turn`, {
      playerId: this.corporationId,
    });
  }

  getAiActions(limit: number = 20): Observable<AiAction[]> {
    return this.http.get<AiAction[]>(`${this.apiBase}/../ai/actions`, {
      params: { playerId: this.corporationId, limit },
    });
  }

  getAiStatus(): Observable<{ active: boolean; playerId: number }> {
    return this.http.get<{ active: boolean; playerId: number }>(`${this.apiBase}/../ai/status`, {
      params: { playerId: this.corporationId },
    });
  }

  // ---- Role Switching ----

  getRole(): Observable<RoleState> {
    return this.http.get<RoleState>(`${this.unifiedBase}/${this.characterId}/role`);
  }

  switchRole(role: string): Observable<RoleState> {
    return this.http.post<RoleState>(`${this.unifiedBase}/${this.characterId}/role/switch`, { role });
  }

  // ---- Traveller Character Integration ----

  getQualifiedContracts(characterId: number): Observable<QualifiedContract[]> {
    return this.http.get<QualifiedContract[]>(`${this.apiBase}/character/qualified`, {
      params: { characterId },
    });
  }

  assignCharacterToContract(characterId: number, contractId: number): Observable<unknown> {
    return this.http.post(`${this.apiBase}/character/${characterId}/assign`, null, {
      params: { contractId },
    });
  }

  getCharacterStats(characterId: number): Observable<CharacterStats> {
    return this.http.get<CharacterStats>(`${this.apiBase}/character/${characterId}/stats`);
  }

  getCharacterAbilities(characterId: number): Observable<CharacterAbilities> {
    return this.http.get<CharacterAbilities>(`${this.apiBase}/character/${characterId}/abilities`);
  }

  getCharacterSkills(characterId: number): Observable<CharacterSkill[]> {
    return this.http.get<CharacterSkill[]>(`${this.apiBase}/character/${characterId}/skills`);
  }

  getCharacterCareer(characterId: number): Observable<CharacterCareer[]> {
    return this.http.get<CharacterCareer[]>(`${this.apiBase}/character/${characterId}/career`);
  }

  getCharacterAssignments(characterId: number): Observable<CharacterAssignment[]> {
    return this.http.get<CharacterAssignment[]>(`${this.apiBase}/character/${characterId}/assignments`);
  }

  // ---- SSE Events ----

  subscribeToEvents(): void {
    if (!isPlatformBrowser(this.platformId)) return;

    const eventSource = new EventSource('/api/v1/game/events?corporation=true');

    eventSource.onmessage = (event) => {
      try {
        const corpEvent: CorporationEvent = JSON.parse(event.data);
        this.eventSubject.next(corpEvent);

        // Push notification for important events
        if (this.notificationService) {
          this.notificationService.handleCorporationEvent({
            type: corpEvent.eventType,
            message: corpEvent.message,
            severity: corpEvent.severity,
          });
        }

        // Refresh state on important events
        if (
          corpEvent.eventType === 'CORPORATION_CONTRACT_COMPLETED' ||
          corpEvent.eventType === 'CORPORATION_EMERGENCY'
        ) {
          this.loadOfficeState();
          this.loadFinancialReport();
        }
      } catch (e) {
        console.error('Failed to parse SSE event', e);
      }
    };

    eventSource.onerror = () => {
      console.warn('SSE connection lost, reconnecting...');
      eventSource.close();
      // Reconnect after 5 seconds
      setTimeout(() => this.subscribeToEvents(), 5000);
    };
  }

  // ---- Utilities ----

  private getCorporationId(): number {
    // 1. Check URL params first (passed from landing page navigation)
    if (isPlatformBrowser(this.platformId)) {
      const urlParams = new URLSearchParams(window.location.search);
      const urlCorpId = urlParams.get('corporationId');
      if (urlCorpId) {
        const id = parseInt(urlCorpId, 10);
        if (!isNaN(id)) {
          // Persist to localStorage for future visits
          localStorage.setItem('corporationId', id.toString());
          return id;
        }
      }
    }
    // 2. Check localStorage (set by onboarding.js)
    const stored = localStorage.getItem('corporationId');
    if (stored) return parseInt(stored, 10);
    // Default for demo
    return 1;
  }

  private getCharacterId(): number {
    // 1. Check URL params first (passed from landing page navigation)
    if (isPlatformBrowser(this.platformId)) {
      const urlParams = new URLSearchParams(window.location.search);
      const urlCharId = urlParams.get('characterId');
      if (urlCharId) {
        const id = parseInt(urlCharId, 10);
        if (!isNaN(id)) {
          // Persist to localStorage for future visits
          localStorage.setItem('currentCharacterId', id.toString());
          return id;
        }
      }
    }
    // 2. Check localStorage — try multiple keys set by different parts of the app
    let stored = localStorage.getItem('currentCharacterId');
    if (!stored) stored = localStorage.getItem('selected_char_id'); // set by character selection in index.html
    if (stored) return parseInt(stored, 10);
    // Default for demo
    return 1;
  }

  setCorporationId(id: number): void {
    localStorage.setItem('corporationId', id.toString());
  }

  setCharacterId(id: number): void {
    localStorage.setItem('currentCharacterId', id.toString());
  }

  // ---- Player ID & Character Switching ----

  getPlayerId(): number | null {
    if (this.playerCharactersService) {
      return this.playerCharactersService.getPlayerId();
    }
    // Fallback: use corporationId as playerId
    return this.corporationId || null;
  }

  setActiveCharacter(characterId: number): void {
    this.setCharacterId(characterId);
    // Refresh office state with the new character context
    this.loadOfficeState();
    this.loadFinancialReport();
  }
}

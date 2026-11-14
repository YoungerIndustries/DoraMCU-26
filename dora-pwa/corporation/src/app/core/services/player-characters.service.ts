import { Injectable, Inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable } from 'rxjs';
import { PlayerCharacterEntry } from '../models/player-character';

@Injectable({
  providedIn: 'root',
})
export class PlayerCharactersService {
  private readonly apiBase = '/api/v1/players';

  constructor(
    private http: HttpClient,
    @Inject(PLATFORM_ID) private platformId: Object,
  ) {}

  // ---- Player ID Resolution ----

  getPlayerId(): number | null {
    if (!isPlatformBrowser(this.platformId)) return null;

    // 1. Check URL params
    const urlParams = new URLSearchParams(window.location.search);
    const urlPlayerId = urlParams.get('playerId');
    if (urlPlayerId) {
      const id = parseInt(urlPlayerId, 10);
      if (!isNaN(id)) {
        localStorage.setItem('playerId', id.toString());
        return id;
      }
    }

    // 2. Check localStorage
    const stored = localStorage.getItem('playerId');
    if (stored) {
      const id = parseInt(stored, 10);
      if (!isNaN(id)) return id;
    }

    // 3. Fall back to corporationId if available (same value in many cases)
    const corpId = localStorage.getItem('corporationId');
    if (corpId) {
      const id = parseInt(corpId, 10);
      if (!isNaN(id)) {
        localStorage.setItem('playerId', id.toString());
        return id;
      }
    }

    return null;
  }

  setPlayerId(id: number): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('playerId', id.toString());
    }
  }

  // ---- Character List ----

  getCharacters(playerId: number): Observable<PlayerCharacterEntry[]> {
    return this.http.get<PlayerCharacterEntry[]>(`${this.apiBase}/${playerId}/characters`);
  }

  getCrew(playerId: number, galaxyName?: string): Observable<PlayerCharacterEntry[]> {
    const params: Record<string, string> = {};
    if (galaxyName) params.galaxyName = galaxyName;
    return this.http.get<PlayerCharacterEntry[]>(`${this.apiBase}/${playerId}/characters/crew`, { params });
  }

  // ---- Role Changes ----

  setCommander(playerId: number, registryId: number): Observable<PlayerCharacterEntry> {
    return this.http.put<PlayerCharacterEntry>(
      `${this.apiBase}/${playerId}/characters/${registryId}/commander`,
      null
    );
  }

  promoteCrew(playerId: number, registryId: number): Observable<PlayerCharacterEntry> {
    return this.http.put<PlayerCharacterEntry>(
      `${this.apiBase}/${playerId}/characters/${registryId}/promote`,
      null
    );
  }

  demoteCrew(playerId: number, registryId: number): Observable<PlayerCharacterEntry> {
    return this.http.put<PlayerCharacterEntry>(
      `${this.apiBase}/${playerId}/characters/${registryId}/demote`,
      null
    );
  }
}

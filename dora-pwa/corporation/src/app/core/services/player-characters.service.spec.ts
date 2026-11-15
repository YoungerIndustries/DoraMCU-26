import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { PlayerCharactersService } from './player-characters.service';
import { PlayerCharacterEntry } from '../models/player-character';

describe('PlayerCharactersService', () => {
  let service: PlayerCharactersService;
  let httpMock: HttpTestingController;
  let http: HttpClient;

  const mockPlayerId = 42;
  const mockCharacters: PlayerCharacterEntry[] = [
    {
      id: 1,
      playerId: mockPlayerId,
      playerName: 'TestPlayer',
      characterType: 'TRAVELLER',
      characterId: 100,
      characterName: 'Commander Shepard',
      characterRole: 'COMMANDER',
      galaxyName: 'Milky Way',
      isAiControlled: false,
      homeLocationId: 'Sol-1',
      homeLocationType: 'Planet',
      createdAt: '2024-01-01T00:00:00Z',
    },
    {
      id: 2,
      playerId: mockPlayerId,
      playerName: 'TestPlayer',
      characterType: 'AI_CREW',
      characterId: 101,
      characterName: 'AI Assistant',
      characterRole: 'CREW',
      galaxyName: 'Milky Way',
      isAiControlled: true,
      homeLocationId: 'Sol-2',
      homeLocationType: 'Station',
      createdAt: '2024-02-01T00:00:00Z',
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        PlayerCharactersService,
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    });
    service = TestBed.inject(PlayerCharactersService);
    httpMock = TestBed.inject(HttpTestingController);
    http = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getPlayerId', () => {
    it('should return playerId from URL params and persist to localStorage', () => {
      window.location.search = '?playerId=99';
      const id = service.getPlayerId();
      expect(id).toBe(99);
      expect(localStorage.getItem('playerId')).toBe('99');
    });

    it('should return playerId from localStorage when URL params are missing', () => {
      window.location.search = '';
      localStorage.setItem('playerId', '77');
      const id = service.getPlayerId();
      expect(id).toBe(77);
    });

    it('should fall back to corporationId when playerId is not set', () => {
      window.location.search = '';
      localStorage.removeItem('playerId');
      localStorage.setItem('corporationId', '55');
      const id = service.getPlayerId();
      expect(id).toBe(55);
    });

    it('should return null when no ID is available', () => {
      window.location.search = '';
      localStorage.removeItem('playerId');
      localStorage.removeItem('corporationId');
      const id = service.getPlayerId();
      expect(id).toBeNull();
    });

    it('should ignore invalid playerId in URL params', () => {
      window.location.search = '?playerId=abc';
      localStorage.removeItem('playerId');
      localStorage.removeItem('corporationId');
      const id = service.getPlayerId();
      expect(id).toBeNull();
    });
  });

  describe('setPlayerId', () => {
    it('should store playerId in localStorage', () => {
      service.setPlayerId(123);
      expect(localStorage.getItem('playerId')).toBe('123');
    });
  });

  describe('getCharacters', () => {
    it('should fetch characters for a given playerId', () => {
      service.getCharacters(mockPlayerId).subscribe((chars) => {
        expect(chars.length).toBe(2);
        expect(chars[0].characterName).toBe('Commander Shepard');
      });

      const req = httpMock.expectOne(`/api/v1/players/${mockPlayerId}/characters`);
      expect(req.request.method).toBe('GET');
      req.flush(mockCharacters);
    });
  });

  describe('getCrew', () => {
    it('should fetch crew for a given playerId', () => {
      service.getCrew(mockPlayerId).subscribe((crew) => {
        expect(crew.length).toBe(1);
      });

      const crew = [mockCharacters[1]];
      const req = httpMock.expectOne(`/api/v1/players/${mockPlayerId}/characters/crew`);
      expect(req.request.method).toBe('GET');
      req.flush(crew);
    });

    it('should include galaxyName param when provided', () => {
      service.getCrew(mockPlayerId, 'Andromeda').subscribe(() => {});

      const req = httpMock.expectOne((request) => {
        return request.url.includes('/crew') && request.params.get('galaxyName') === 'Andromeda';
      });
      req.flush([]);
    });
  });

  describe('setCommander', () => {
    it('should set a character as commander', () => {
      const registryId = 1;
      service.setCommander(mockPlayerId, registryId).subscribe((result) => {
        expect(result.characterRole).toBe('COMMANDER');
      });

      const req = httpMock.expectOne(
        `/api/v1/players/${mockPlayerId}/characters/${registryId}/commander`
      );
      expect(req.request.method).toBe('PUT');
      req.flush({ ...mockCharacters[0], characterRole: 'COMMANDER' });
    });
  });

  describe('promoteCrew', () => {
    it('should promote a crew member', () => {
      const registryId = 2;
      service.promoteCrew(mockPlayerId, registryId).subscribe((result) => {
        expect(result.characterRole).toBe('CREW');
      });

      const req = httpMock.expectOne(
        `/api/v1/players/${mockPlayerId}/characters/${registryId}/promote`
      );
      expect(req.request.method).toBe('PUT');
      req.flush({ ...mockCharacters[1], characterRole: 'CREW' });
    });
  });

  describe('demoteCrew', () => {
    it('should demote a crew member', () => {
      const registryId = 2;
      service.demoteCrew(mockPlayerId, registryId).subscribe((result) => {
        expect(result.characterRole).toBe('OWNED');
      });

      const req = httpMock.expectOne(
        `/api/v1/players/${mockPlayerId}/characters/${registryId}/demote`
      );
      expect(req.request.method).toBe('PUT');
      req.flush({ ...mockCharacters[1], characterRole: 'OWNED' });
    });
  });
});

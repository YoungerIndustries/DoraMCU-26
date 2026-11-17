import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { PLATFORM_ID } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CharacterLandingComponent } from './character-landing.component';
import { CorporationService } from '../../core/services/corporation.service';
import { PlayerCharactersService } from '../../core/services/player-characters.service';
import { PlayerCharacterEntry } from '../../core/models/player-character';
import { of, throwError } from 'rxjs';

describe('CharacterLandingComponent', () => {
  let component: CharacterLandingComponent;
  let fixture: ComponentFixture<CharacterLandingComponent>;
  let corpService: jasmine.SpyObj<CorporationService>;
  let playerCharsService: jasmine.SpyObj<PlayerCharactersService>;
  let router: jasmine.SpyObj<Router>;
  let snackBar: jasmine.SpyObj<MatSnackBar>;

  const mockCharacters: PlayerCharacterEntry[] = [
    {
      id: 1,
      playerId: 42,
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
      playerId: 42,
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

  beforeEach(async () => {
    corpService = jasmine.createSpyObj('CorporationService', ['getPlayerId', 'setActiveCharacter']);
    corpService.getPlayerId.and.returnValue(42);

    playerCharsService = jasmine.createSpyObj('PlayerCharactersService', ['getCharacters', 'setCommander', 'setPlayerId']);
    playerCharsService.getCharacters.and.returnValue(of(mockCharacters));

    router = jasmine.createSpyObj('Router', ['navigate']);
    snackBar = jasmine.createSpyObj('MatSnackBar', ['open']);

    await TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        CharacterLandingComponent,
      ],
      providers: [
        { provide: CorporationService, useValue: corpService },
        { provide: PlayerCharactersService, useValue: playerCharsService },
        { provide: Router, useValue: router },
        { provide: MatSnackBar, useValue: snackBar },
        { provide: PLATFORM_ID, useValue: 'browser' },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CharacterLandingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load characters on init', () => {
    expect(playerCharsService.getCharacters).toHaveBeenCalledWith(42);
    expect(component.characters().length).toBe(2);
    expect(component.loading).toBeFalse();
  });

  it('should show error when playerId is not available', () => {
    corpService.getPlayerId.and.returnValue(null);
    component.loadCharacters();
    fixture.detectChanges();

    expect(component.error).toContain('Player ID not found');
    expect(component.loading).toBeFalse();
  });

  it('should show error when API call fails', () => {
    playerCharsService.getCharacters.and.returnValue(throwError(() => new Error('Network error')));
    component.loadCharacters();
    fixture.detectChanges();

    expect(component.error).toContain('Failed to load characters');
    expect(component.loading).toBeFalse();
  });

  it('should select a commander character and navigate to dashboard', () => {
    const char = mockCharacters[0]; // COMMANDER role
    component.selectCharacter(char);

    expect(corpService.setActiveCharacter).toHaveBeenCalledWith(char.characterId);
    expect(snackBar.open).toHaveBeenCalledWith(`Landing as ${char.characterName}`, 'OK', { duration: 2000 });
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should set commander when selecting a non-commander character', () => {
    const char = mockCharacters[1]; // CREW role
    playerCharsService.setCommander.and.returnValue(of({ ...char, characterRole: 'COMMANDER' }));
    component.selectCharacter(char);

    expect(playerCharsService.setCommander).toHaveBeenCalledWith(42, char.id);
    expect(snackBar.open).toHaveBeenCalledWith(`${char.characterName} set as commander`, 'OK', { duration: 3000 });
  });

  it('should navigate to character page when creating new character', () => {
    component.createNewCharacter();
    expect(router.navigate).toHaveBeenCalledWith(['/character']);
  });

  it('should retry loading characters on error', () => {
    component.error = 'Some error';
    playerCharsService.getCharacters.and.returnValue(of(mockCharacters));
    component.loadCharacters();
    fixture.detectChanges();

    expect(component.error).toBeNull();
    expect(component.characters().length).toBe(2);
  });

  describe('empty state', () => {
    beforeEach(() => {
      playerCharsService.getCharacters.and.returnValue(of([]));
    });

    it('should show empty state when no characters exist', () => {
      component.loadCharacters();
      fixture.detectChanges();

      expect(component.characters().length).toBe(0);
      expect(component.loading).toBeFalse();
      expect(component.error).toBeNull();
    });
  });
});

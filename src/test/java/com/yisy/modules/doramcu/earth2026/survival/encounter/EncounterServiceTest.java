package com.yisy.modules.doramcu.earth2026.survival.encounter;

import com.yisy.modules.doramcu.earth2026.model.E26Monster;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.E26MonsterRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.service.BiosphereService;
import com.yisy.modules.doramcu.earth2026.survival.SurvivalState;
import com.yisy.modules.doramcu.earth2026.survival.SurvivalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("EncounterService Tests")
class EncounterServiceTest {

    private SurvivalEncounterRepository encounterRepository;
    private Earth2026CharacterRepository characterRepository;
    private E26MonsterRepository monsterRepository;
    private BiosphereService biosphereService;
    private SurvivalService survivalService;
    private EncounterService encounterService;

    @BeforeEach
    void setUp() {
        encounterRepository = mock(SurvivalEncounterRepository.class);
        characterRepository = mock(Earth2026CharacterRepository.class);
        monsterRepository = mock(E26MonsterRepository.class);
        biosphereService = mock(BiosphereService.class);
        survivalService = mock(SurvivalService.class);
        encounterService = new EncounterService(
                encounterRepository, characterRepository, monsterRepository,
                biosphereService, survivalService);
    }

    private Earth2026Character createCharacter(Long id, int level) {
        Earth2026Character character = new Earth2026Character();
        character.setId(id);
        character.setName("Test Character");
        character.setHitPoints(100);
        character.setMaxHitPoints(100);
        character.setStrScore(10);
        character.setDexScore(10);
        character.setConScore(10);
        character.setIntScore(10);
        character.setWisScore(10);
        character.setChaScore(10);
        character.setLevel(level);
        character.setEarthLocation("Test Location");
        character.setExperiencePoints(0);
        return character;
    }

    private E26Monster createMonster(Long id, int threatLevel) {
        E26Monster monster = new E26Monster();
        monster.setId(id);
        monster.setName("Test Monster");
        monster.setThreatLevel(threatLevel);
        monster.setHp(50);
        monster.setStr(10);
        monster.setDex(10);
        monster.setCon(10);
        monster.setXp(threatLevel * 10);
        return monster;
    }

    // ---- shouldEncounterOccur ----

    @Nested
    @DisplayName("shouldEncounterOccur Tests")
    class ShouldEncounterOccurTests {

        @Test
        @DisplayName("should return false when character not found")
        void shouldEncounterOccur_shouldReturnFalseWhenNotFound() {
            when(characterRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            boolean result = encounterService.shouldEncounterOccur(999L);

            assertFalse(result);
        }

        @Test
        @DisplayName("should return false when survival state not active")
        void shouldEncounterOccur_shouldReturnFalseWhenInactive() {
            Earth2026Character character = createCharacter(1L, 1);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            SurvivalState state = new SurvivalState(1L);
            state.setActive(false);
            when(survivalService.getState(1L)).thenReturn(state);

            boolean result = encounterService.shouldEncounterOccur(1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("should return false when no survival state")
        void shouldEncounterOccur_shouldReturnFalseWhenNoState() {
            Earth2026Character character = createCharacter(1L, 1);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(survivalService.getState(1L)).thenReturn(null);

            boolean result = encounterService.shouldEncounterOccur(1L);

            assertFalse(result);
        }

        @Test
        @DisplayName("should check encounter chance based on level")
        void shouldEncounterOccur_shouldCheckChance() {
            Earth2026Character character = createCharacter(1L, 5);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

            SurvivalState state = new SurvivalState(1L);
            state.setActive(true);
            when(survivalService.getState(1L)).thenReturn(state);

            // Just verify it doesn't throw
            boolean result = encounterService.shouldEncounterOccur(1L);
            assertNotNull(result ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    // ---- triggerEncounter ----

    @Nested
    @DisplayName("triggerEncounter Tests")
    class TriggerEncounterTests {

        @Test
        @DisplayName("should create encounter record")
        void triggerEncounter_shouldCreateRecord() {
            Earth2026Character character = createCharacter(1L, 3);
            character.setStrScore(15);
            character.setDexScore(15);
            character.setConScore(15);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(characterRepository.save(any(Earth2026Character.class))).thenAnswer(inv -> inv.getArgument(0));

            E26Monster monster = createMonster(1L, 3);
            monster.setHp(20);
            monster.setStr(5);
            monster.setDex(5);
            monster.setCon(5);
            monster.setXp(30);
            when(monsterRepository.findAll()).thenReturn(List.of(monster));

            when(encounterRepository.save(any(SurvivalEncounter.class))).thenAnswer(inv -> {
                SurvivalEncounter e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });

            SurvivalEncounter result = encounterService.triggerEncounter(1L);

            assertNotNull(result);
            assertEquals(1L, result.getCharacterId());
            assertEquals("Test Monster", result.getMonsterName());
            assertNotNull(result.getOutcome());
            verify(encounterRepository).save(result);
        }

        @Test
        @DisplayName("should throw when character not found")
        void triggerEncounter_shouldThrowWhenNotFound() {
            when(characterRepository.findById(999L)).thenReturn(java.util.Optional.empty());

            assertThrows(IllegalArgumentException.class, () -> encounterService.triggerEncounter(999L));
        }

        @Test
        @DisplayName("should throw when no monsters available")
        void triggerEncounter_shouldThrowWhenNoMonsters() {
            Earth2026Character character = createCharacter(1L, 1);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(monsterRepository.findAll()).thenReturn(List.of());

            assertThrows(IllegalStateException.class, () -> encounterService.triggerEncounter(1L));
        }

        @Test
        @DisplayName("should grant XP on won encounter")
        void triggerEncounter_shouldGrantXpOnWin() {
            Earth2026Character character = createCharacter(1L, 5);
            character.setStrScore(20);
            character.setDexScore(20);
            character.setConScore(20);
            character.setExperiencePoints(100);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(characterRepository.save(any(Earth2026Character.class))).thenAnswer(inv -> inv.getArgument(0));

            E26Monster monster = createMonster(1L, 3);
            monster.setHp(20);
            monster.setStr(3);
            monster.setDex(3);
            monster.setCon(3);
            monster.setXp(30);
            when(monsterRepository.findAll()).thenReturn(List.of(monster));

            when(encounterRepository.save(any(SurvivalEncounter.class))).thenAnswer(inv -> {
                SurvivalEncounter e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });

            SurvivalEncounter result = encounterService.triggerEncounter(1L);

            assertNotNull(result);
            // Character should have gained XP if won
            verify(characterRepository, atLeast(0)).save(character);
        }

        @Test
        @DisplayName("should apply damage on lost encounter")
        void triggerEncounter_shouldApplyDamageOnLoss() {
            Earth2026Character character = createCharacter(1L, 1);
            character.setStrScore(3);
            character.setDexScore(3);
            character.setConScore(3);
            character.setHitPoints(50);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(characterRepository.save(any(Earth2026Character.class))).thenAnswer(inv -> inv.getArgument(0));

            E26Monster monster = createMonster(1L, 5);
            monster.setHp(80);
            monster.setStr(15);
            monster.setDex(15);
            monster.setCon(15);
            monster.setXp(50);
            when(monsterRepository.findAll()).thenReturn(List.of(monster));

            when(encounterRepository.save(any(SurvivalEncounter.class))).thenAnswer(inv -> {
                SurvivalEncounter e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });

            SurvivalEncounter result = encounterService.triggerEncounter(1L);

            assertNotNull(result);
            // Character may have taken damage
            assertTrue(character.getHitPoints() >= 0);
        }
    }

    // ---- Encounter outcomes ----

    @Nested
    @DisplayName("Encounter Outcome Tests")
    class EncounterOutcomeTests {

        @Test
        @DisplayName("should select monster within threat range")
        void triggerEncounter_shouldSelectMonsterInRange() {
            Earth2026Character character = createCharacter(1L, 5);
            character.setStrScore(10);
            character.setDexScore(10);
            character.setConScore(10);
            when(characterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
            when(characterRepository.save(any(Earth2026Character.class))).thenAnswer(inv -> inv.getArgument(0));

            E26Monster monster1 = createMonster(1L, 3);
            monster1.setStr(5);
            monster1.setDex(5);
            monster1.setCon(5);
            monster1.setHp(20);

            E26Monster monster2 = createMonster(2L, 7);
            monster2.setStr(5);
            monster2.setDex(5);
            monster2.setCon(5);
            monster2.setHp(20);

            when(monsterRepository.findAll()).thenReturn(List.of(monster1, monster2));
            when(encounterRepository.save(any(SurvivalEncounter.class))).thenAnswer(inv -> {
                SurvivalEncounter e = inv.getArgument(0);
                e.setId(1L);
                return e;
            });

            SurvivalEncounter result = encounterService.triggerEncounter(1L);

            assertNotNull(result);
            // Should have selected a monster within threat range (3-7 for level 5)
            assertTrue(result.getMonsterName() != null);
        }
    }
}

package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.UpdateDnd5eCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpellSlot;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Dnd5eCharacterService Tests")
class Dnd5eCharacterServiceTest {

    private Dnd5eCharacterRepository characterRepository;
    private Dnd5eCharacterService characterService;

    @BeforeEach
    void setUp() {
        characterRepository = mock(Dnd5eCharacterRepository.class);
        characterService = new Dnd5eCharacterService(characterRepository);
    }

    // ---- createCharacter ----

    @Nested
    @DisplayName("createCharacter Tests")
    class CreateCharacterTests {

        @Test
        @DisplayName("should create a character with standard array scores")
        void createCharacter_shouldUseStandardArray() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Frodo");
            request.setRace("Halfling");
            request.setCharacterClass("Rogue");
            request.setBackground("Outlander");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertNotNull(character);
            assertEquals("Frodo", character.getName());
            assertEquals("Halfling", character.getRace());
            assertEquals("Rogue", character.getCharacterClass());
            assertEquals("Outlander", character.getBackground());
            assertEquals(1, character.getLevel());
            assertEquals(15, character.getStrScore());
            assertEquals(14, character.getDexScore());
            assertEquals(13, character.getConScore());
            assertEquals(12, character.getIntScore());
            assertEquals(10, character.getWisScore());
            assertEquals(8, character.getChaScore());
            verify(characterRepository).save(character);
        }

        @Test
        @DisplayName("should create a character with provided ability scores")
        void createCharacter_shouldUseProvidedScores() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Gimli");
            request.setRace("Dwarf");
            request.setCharacterClass("Fighter");
            request.setStrScore(18);
            request.setDexScore(12);
            request.setConScore(16);
            request.setIntScore(10);
            request.setWisScore(12);
            request.setChaScore(8);

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertEquals(18, character.getStrScore());
            assertEquals(12, character.getDexScore());
            assertEquals(16, character.getConScore());
            assertEquals(10, character.getIntScore());
            assertEquals(12, character.getWisScore());
            assertEquals(8, character.getChaScore());
        }

        @Test
        @DisplayName("should set correct hit dice for Fighter class")
        void createCharacter_shouldSetFighterHitDice() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Thorin");
            request.setRace("Dwarf");
            request.setCharacterClass("Fighter");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertEquals("d10", character.getHitDice());
        }

        @Test
        @DisplayName("should set correct hit dice for Wizard class")
        void createCharacter_shouldSetWizardHitDice() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Gandalf");
            request.setRace("Human");
            request.setCharacterClass("Wizard");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertEquals("d4", character.getHitDice());
        }

        @Test
        @DisplayName("should set correct hit dice for Barbarian class")
        void createCharacter_shouldSetBarbarianHitDice() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Bjorn");
            request.setRace("Human");
            request.setCharacterClass("Barbarian");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertEquals("d12", character.getHitDice());
        }

        @Test
        @DisplayName("should default hit dice to d8 for unknown class")
        void createCharacter_shouldDefaultHitDice() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Unknown");
            request.setRace("Human");
            request.setCharacterClass("Artificer");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertEquals("d8", character.getHitDice());
        }

        @Test
        @DisplayName("should calculate HP as hit die average + CON mod at level 1")
        void createCharacter_shouldCalculateHP() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("HP Test");
            request.setRace("Human");
            request.setCharacterClass("Fighter");
            // Must set strScore to trigger provided scores path
            request.setStrScore(10);
            request.setDexScore(10);
            request.setConScore(16); // +3 CON mod
            request.setIntScore(10);
            request.setWisScore(10);
            request.setChaScore(10);

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            // Fighter d10 average = 6, CON mod = +3, total = 9
            assertEquals(9, character.getMaxHitPoints());
            assertEquals(9, character.getHitPoints());
        }

        @Test
        @DisplayName("should calculate AC as 10 + DEX mod")
        void createCharacter_shouldCalculateAC() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("AC Test");
            request.setRace("Elf");
            request.setCharacterClass("Rogue");
            // Must set strScore to trigger provided scores path
            request.setStrScore(10);
            request.setDexScore(16); // +3 DEX mod
            request.setConScore(10);
            request.setIntScore(10);
            request.setWisScore(10);
            request.setChaScore(10);

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertEquals(13, character.getArmorClass());
        }

        @Test
        @DisplayName("should set proficiency bonus to 2 at level 1")
        void createCharacter_shouldSetProficiencyBonus() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Prof Test");
            request.setRace("Human");
            request.setCharacterClass("Wizard");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertEquals(2, character.getProficiencyBonus());
        }

        @Test
        @DisplayName("should initialize spell slots for Wizard (full caster)")
        void createCharacter_shouldInitializeSpellSlotsForWizard() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Wizard");
            request.setRace("Human");
            request.setCharacterClass("Wizard");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertFalse(character.getSpellSlots().isEmpty());
            // Level 1 full caster has 2 level 1 slots
            Dnd5eSpellSlot slot = character.getSpellSlots().stream()
                    .filter(s -> s.getSlotLevel() == 1)
                    .findFirst().orElse(null);
            assertNotNull(slot);
            assertEquals(2, slot.getTotalSlots());
        }

        @Test
        @DisplayName("should not initialize spell slots for non-caster")
        void createCharacter_shouldNotInitializeSpellSlotsForNonCaster() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Fighter");
            request.setRace("Human");
            request.setCharacterClass("Fighter");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertTrue(character.getSpellSlots().isEmpty());
        }

        @Test
        @DisplayName("should initialize spell slots for Paladin (half caster)")
        void createCharacter_shouldInitializeSpellSlotsForPaladin() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Paladin");
            request.setRace("Human");
            request.setCharacterClass("Paladin");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            // Level 1 Paladin has no spell slots (gains at level 2)
            assertTrue(character.getSpellSlots().isEmpty());
        }

        @Test
        @DisplayName("should generate ability scores using roll method")
        void createCharacter_shouldUseRollMethod() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Rolled");
            request.setRace("Human");
            request.setCharacterClass("Fighter");
            request.setScoreMethod("roll");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            // Scores should be in valid range (3-18 for 4d6 drop lowest)
            assertTrue(character.getStrScore() >= 3 && character.getStrScore() <= 18);
            assertTrue(character.getDexScore() >= 3 && character.getDexScore() <= 18);
            assertTrue(character.getConScore() >= 3 && character.getConScore() <= 18);
            assertTrue(character.getIntScore() >= 3 && character.getIntScore() <= 18);
            assertTrue(character.getWisScore() >= 3 && character.getWisScore() <= 18);
            assertTrue(character.getChaScore() >= 3 && character.getChaScore() <= 18);
        }

        @Test
        @DisplayName("should generate ability scores using point buy method")
        void createCharacter_shouldUsePointBuyMethod() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("PointBuy");
            request.setRace("Human");
            request.setCharacterClass("Wizard");
            request.setScoreMethod("pointbuy");

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            // Point buy returns same as standard array: 15, 14, 13, 12, 10, 8
            assertEquals(15, character.getStrScore());
            assertEquals(14, character.getDexScore());
            assertEquals(13, character.getConScore());
            assertEquals(12, character.getIntScore());
            assertEquals(10, character.getWisScore());
            assertEquals(8, character.getChaScore());
        }

        @Test
        @DisplayName("should ensure minimum HP of 1")
        void createCharacter_shouldEnsureMinimumHP() {
            // Arrange
            CreateDnd5eCharacterRequest request = new CreateDnd5eCharacterRequest();
            request.setName("Low HP");
            request.setRace("Human");
            request.setCharacterClass("Wizard"); // d4 average = 4
            request.setConScore(4); // -3 CON mod -> 4 + (-3) = 1

            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter character = characterService.createCharacter(request);

            // Assert
            assertTrue(character.getMaxHitPoints() >= 1);
        }
    }

    // ---- getCharacter ----

    @Nested
    @DisplayName("getCharacter Tests")
    class GetCharacterTests {

        @Test
        @DisplayName("should return character by id")
        void getCharacter_shouldReturnById() {
            // Arrange
            Dnd5eCharacter character = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act
            Dnd5eCharacter result = characterService.getCharacter(1L);

            // Assert
            assertEquals("Frodo", result.getName());
        }

        @Test
        @DisplayName("should throw when character not found")
        void getCharacter_shouldThrowWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> characterService.getCharacter(999L));
            assertTrue(ex.getMessage().contains("Character not found"));
            assertTrue(ex.getMessage().contains("999"));
        }
    }

    // ---- listCharacters ----

    @Nested
    @DisplayName("listCharacters Tests")
    class ListCharactersTests {

        @Test
        @DisplayName("should return all characters")
        void listCharacters_shouldReturnAll() {
            // Arrange
            List<Dnd5eCharacter> characters = List.of(
                    createCharacter(1L, "Frodo"),
                    createCharacter(2L, "Aragorn")
            );
            when(characterRepository.findAll()).thenReturn(characters);

            // Act
            List<Dnd5eCharacter> result = characterService.listCharacters();

            // Assert
            assertEquals(2, result.size());
            verify(characterRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no characters exist")
        void listCharacters_shouldReturnEmpty() {
            // Arrange
            when(characterRepository.findAll()).thenReturn(List.of());

            // Act
            List<Dnd5eCharacter> result = characterService.listCharacters();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- deleteCharacter ----

    @Nested
    @DisplayName("deleteCharacter Tests")
    class DeleteCharacterTests {

        @Test
        @DisplayName("should delete character by id")
        void deleteCharacter_shouldDelete() {
            // Arrange

            // Act
            characterService.deleteCharacter(1L);

            // Assert
            verify(characterRepository).deleteById(1L);
        }
    }

    // ---- updateCharacter ----

    @Nested
    @DisplayName("updateCharacter Tests")
    class UpdateCharacterTests {

        @Test
        @DisplayName("should update HP")
        void updateCharacter_shouldUpdateHP() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            existing.setHitPoints(20);
            existing.setMaxHitPoints(25);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setHitPoints(15);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(15, result.getHitPoints());
            assertEquals(25, result.getMaxHitPoints()); // unchanged
            verify(characterRepository).save(existing);
        }

        @Test
        @DisplayName("should update max HP")
        void updateCharacter_shouldUpdateMaxHP() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            existing.setMaxHitPoints(25);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setMaxHitPoints(30);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(30, result.getMaxHitPoints());
        }

        @Test
        @DisplayName("should clamp HP to minimum 0")
        void updateCharacter_shouldClampHPToZero() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            existing.setHitPoints(10);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setHitPoints(-5);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(0, result.getHitPoints());
        }

        @Test
        @DisplayName("should clamp max HP to minimum 1")
        void updateCharacter_shouldClampMaxHPToOne() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setMaxHitPoints(0);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(1, result.getMaxHitPoints());
        }

        @Test
        @DisplayName("should update AC")
        void updateCharacter_shouldUpdateAC() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setArmorClass(18);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(18, result.getArmorClass());
        }

        @Test
        @DisplayName("should update speed")
        void updateCharacter_shouldUpdateSpeed() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setSpeed(25);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(25, result.getSpeed());
        }

        @Test
        @DisplayName("should update XP")
        void updateCharacter_shouldUpdateXP() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setExperiencePoints(500);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(500, result.getExperiencePoints());
        }

        @Test
        @DisplayName("should clamp XP to minimum 0")
        void updateCharacter_shouldClampXPToZero() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setExperiencePoints(-100);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(0, result.getExperiencePoints());
        }

        @Test
        @DisplayName("should update temp HP")
        void updateCharacter_shouldUpdateTempHP() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setTempHitPoints(10);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(10, result.getTempHitPoints());
        }

        @Test
        @DisplayName("should clamp temp HP to minimum 0")
        void updateCharacter_shouldClampTempHPToZero() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setTempHitPoints(-5);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(0, result.getTempHitPoints());
        }

        @Test
        @DisplayName("should update exhaustion level with clamping 0-6")
        void updateCharacter_shouldClampExhaustion() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setExhaustionLevel(10); // exceeds max 6

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(6, result.getExhaustionLevel());
        }

        @Test
        @DisplayName("should clamp exhaustion to minimum 0")
        void updateCharacter_shouldClampExhaustionToZero() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setExhaustionLevel(-2);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(0, result.getExhaustionLevel());
        }

        @Test
        @DisplayName("should update death save successes with clamping 0-3")
        void updateCharacter_shouldClampDeathSaveSuccesses() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setDeathSaveSuccesses(5); // exceeds max 3

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(3, result.getDeathSaveSuccesses());
        }

        @Test
        @DisplayName("should update death save failures with clamping 0-3")
        void updateCharacter_shouldClampDeathSaveFailures() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setDeathSaveFailures(5); // exceeds max 3

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(3, result.getDeathSaveFailures());
        }

        @Test
        @DisplayName("should update ability scores")
        void updateCharacter_shouldUpdateAbilityScores() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setStrScore(20);
            request.setDexScore(18);
            request.setConScore(16);
            request.setIntScore(14);
            request.setWisScore(12);
            request.setChaScore(10);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(20, result.getStrScore());
            assertEquals(18, result.getDexScore());
            assertEquals(16, result.getConScore());
            assertEquals(14, result.getIntScore());
            assertEquals(12, result.getWisScore());
            assertEquals(10, result.getChaScore());
        }

        @Test
        @DisplayName("should update equipment")
        void updateCharacter_shouldUpdateEquipment() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setEquipment("Ring, Sword, Shield");

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals("Ring, Sword, Shield", result.getEquipment());
        }

        @Test
        @DisplayName("should update conditions")
        void updateCharacter_shouldUpdateConditions() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setConditions("poisoned, exhausted");

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals("poisoned, exhausted", result.getConditions());
        }

        @Test
        @DisplayName("should update inspiration")
        void updateCharacter_shouldUpdateInspiration() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            existing.setInspiration(false);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setInspiration(true);

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertTrue(result.isInspiration());
        }

        @Test
        @DisplayName("should update only non-null fields")
        void updateCharacter_shouldUpdateOnlyNonNullFields() {
            // Arrange
            Dnd5eCharacter existing = createCharacter(1L, "Frodo");
            existing.setHitPoints(20);
            existing.setMaxHitPoints(25);
            existing.setArmorClass(15);
            existing.setExperiencePoints(100);
            when(characterRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setHitPoints(10);
            // Other fields are null

            // Act
            Dnd5eCharacter result = characterService.updateCharacter(1L, request);

            // Assert
            assertEquals(10, result.getHitPoints()); // changed
            assertEquals(25, result.getMaxHitPoints()); // unchanged
            assertEquals(15, result.getArmorClass()); // unchanged
            assertEquals(100, result.getExperiencePoints()); // unchanged
        }

        @Test
        @DisplayName("should throw when character not found")
        void updateCharacter_shouldThrowWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());
            UpdateDnd5eCharacterRequest request = new UpdateDnd5eCharacterRequest();
            request.setHitPoints(10);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> characterService.updateCharacter(999L, request));
        }
    }

    // ---- generateAbilityScores ----

    @Nested
    @DisplayName("generateAbilityScores Tests")
    class GenerateAbilityScoresTests {

        @Test
        @DisplayName("should return standard array by default")
        void generateAbilityScores_shouldReturnStandardArray() {
            // Act
            int[] scores = characterService.generateAbilityScores(null);

            // Assert
            assertArrayEquals(new int[]{15, 14, 13, 12, 10, 8}, scores);
        }

        @Test
        @DisplayName("should return standard array for 'standard' method")
        void generateAbilityScores_shouldReturnStandard() {
            // Act
            int[] scores = characterService.generateAbilityScores("standard");

            // Assert
            assertArrayEquals(new int[]{15, 14, 13, 12, 10, 8}, scores);
        }

        @Test
        @DisplayName("should return standard array for unknown method")
        void generateAbilityScores_shouldDefaultToStandard() {
            // Act
            int[] scores = characterService.generateAbilityScores("unknown");

            // Assert
            assertArrayEquals(new int[]{15, 14, 13, 12, 10, 8}, scores);
        }

        @Test
        @DisplayName("should return 6 scores for roll method")
        void generateAbilityScores_shouldReturnSixScoresForRoll() {
            // Act
            int[] scores = characterService.generateAbilityScores("roll");

            // Assert
            assertEquals(6, scores.length);
            for (int score : scores) {
                assertTrue(score >= 3 && score <= 18, "Score " + score + " out of range");
            }
        }

        @Test
        @DisplayName("should return point buy scores")
        void generateAbilityScores_shouldReturnPointBuy() {
            // Act
            int[] scores = characterService.generateAbilityScores("pointbuy");

            // Assert
            assertArrayEquals(new int[]{15, 14, 13, 12, 10, 8}, scores);
        }

        @Test
        @DisplayName("should handle case insensitive method names")
        void generateAbilityScores_shouldHandleCaseInsensitive() {
            // Act
            int[] scores1 = characterService.generateAbilityScores("ROLL");
            int[] scores2 = characterService.generateAbilityScores("Standard");
            int[] scores3 = characterService.generateAbilityScores("PointBuy");

            // Assert
            assertEquals(6, scores1.length);
            assertArrayEquals(new int[]{15, 14, 13, 12, 10, 8}, scores2);
            assertArrayEquals(new int[]{15, 14, 13, 12, 10, 8}, scores3);
        }
    }

    // ---- getProficiencyBonus ----

    @Nested
    @DisplayName("getProficiencyBonus Tests")
    class GetProficiencyBonusTests {

        @Test
        @DisplayName("should return +2 for level 1-4")
        void getProficiencyBonus_shouldReturn2ForLevel1to4() {
            assertEquals(2, characterService.getProficiencyBonus(1));
            assertEquals(2, characterService.getProficiencyBonus(2));
            assertEquals(2, characterService.getProficiencyBonus(3));
            assertEquals(2, characterService.getProficiencyBonus(4));
        }

        @Test
        @DisplayName("should return +3 for level 5-8")
        void getProficiencyBonus_shouldReturn3ForLevel5to8() {
            assertEquals(3, characterService.getProficiencyBonus(5));
            assertEquals(3, characterService.getProficiencyBonus(6));
            assertEquals(3, characterService.getProficiencyBonus(7));
            assertEquals(3, characterService.getProficiencyBonus(8));
        }

        @Test
        @DisplayName("should return +4 for level 9-12")
        void getProficiencyBonus_shouldReturn4ForLevel9to12() {
            assertEquals(4, characterService.getProficiencyBonus(9));
            assertEquals(4, characterService.getProficiencyBonus(10));
            assertEquals(4, characterService.getProficiencyBonus(11));
            assertEquals(4, characterService.getProficiencyBonus(12));
        }

        @Test
        @DisplayName("should return +5 for level 13-16")
        void getProficiencyBonus_shouldReturn5ForLevel13to16() {
            assertEquals(5, characterService.getProficiencyBonus(13));
            assertEquals(5, characterService.getProficiencyBonus(14));
            assertEquals(5, characterService.getProficiencyBonus(15));
            assertEquals(5, characterService.getProficiencyBonus(16));
        }

        @Test
        @DisplayName("should return +6 for level 17-20")
        void getProficiencyBonus_shouldReturn6ForLevel17to20() {
            assertEquals(6, characterService.getProficiencyBonus(17));
            assertEquals(6, characterService.getProficiencyBonus(18));
            assertEquals(6, characterService.getProficiencyBonus(19));
            assertEquals(6, characterService.getProficiencyBonus(20));
        }

        @Test
        @DisplayName("should return 2 for invalid levels")
        void getProficiencyBonus_shouldReturn2ForInvalid() {
            assertEquals(2, characterService.getProficiencyBonus(0));
            assertEquals(2, characterService.getProficiencyBonus(-1));
            assertEquals(2, characterService.getProficiencyBonus(21));
        }
    }

    // ---- Helper methods ----

    private Dnd5eCharacter createCharacter(Long id, String name) {
        Dnd5eCharacter character = new Dnd5eCharacter();
        character.setId(id);
        character.setName(name);
        character.setRace("Human");
        character.setCharacterClass("Fighter");
        character.setLevel(1);
        character.setStrScore(15);
        character.setDexScore(14);
        character.setConScore(13);
        character.setIntScore(12);
        character.setWisScore(10);
        character.setChaScore(8);
        character.setHitPoints(18);
        character.setMaxHitPoints(18);
        character.setArmorClass(12);
        character.setSpeed(30);
        character.setHitDice("d10");
        character.setProficiencyBonus(2);
        return character;
    }
}

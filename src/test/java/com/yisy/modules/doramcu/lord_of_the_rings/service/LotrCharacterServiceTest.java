package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCharacterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrCharacterService Tests")
class LotrCharacterServiceTest {

    private LotrCharacterRepository characterRepository;
    private LotrCharacterService characterService;

    @BeforeEach
    void setUp() {
        characterRepository = mock(LotrCharacterRepository.class);
        characterService = new LotrCharacterService(characterRepository);
    }

    // ---- createCharacter ----

    @Nested
    @DisplayName("createCharacter Tests")
    class CreateCharacterTests {

        @Test
        @DisplayName("should create a character with generated ability scores")
        void createCharacter_shouldGenerateScores() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Frodo", "Hobbit", "Scout");

            // Assert
            assertNotNull(character);
            assertEquals("Frodo", character.getName());
            assertEquals("Hobbit", character.getRace());
            assertEquals("Scout", character.getCalling());
            assertEquals(1, character.getLevel());
            assertTrue(character.getStrScore() >= 3 && character.getStrScore() <= 18);
            assertTrue(character.getDexScore() >= 3 && character.getDexScore() <= 18);
            assertTrue(character.getConScore() >= 3 && character.getConScore() <= 18);
            assertTrue(character.getIntScore() >= 3 && character.getIntScore() <= 18);
            assertTrue(character.getWisScore() >= 3 && character.getWisScore() <= 18);
            assertTrue(character.getChaScore() >= 3 && character.getChaScore() <= 18);
            verify(characterRepository).save(character);
        }

        @Test
        @DisplayName("should set correct HP for Hobbit")
        void createCharacter_shouldSetHobbitHP() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Sam", "Hobbit", "Scout");

            // Assert
            // Hobbit base HP = 6 + CON mod
            int expectedHp = 6 + Math.max(0, (character.getConScore() - 10) / 2);
            assertEquals(expectedHp, character.getMaxHitPoints());
            assertEquals(expectedHp, character.getHitPoints());
        }

        @Test
        @DisplayName("should set correct HP for Dwarf")
        void createCharacter_shouldSetDwarfHP() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Gimli", "Dwarf", "Warrior");

            // Assert
            // Dwarf base HP = 10 + CON mod
            int expectedHp = 10 + Math.max(0, (character.getConScore() - 10) / 2);
            assertEquals(expectedHp, character.getMaxHitPoints());
        }

        @Test
        @DisplayName("should set correct HP for Ent")
        void createCharacter_shouldSetEntHP() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Treebeard", "Ent", "Warrior");

            // Assert
            // Ent base HP = 12 + CON mod
            int expectedHp = 12 + Math.max(0, (character.getConScore() - 10) / 2);
            assertEquals(expectedHp, character.getMaxHitPoints());
        }

        @Test
        @DisplayName("should set correct AC for Dwarf")
        void createCharacter_shouldSetDwarfAC() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Gimli", "Dwarf", "Warrior");

            // Assert
            assertEquals(14, character.getArmorClass());
        }

        @Test
        @DisplayName("should set correct AC for Ent")
        void createCharacter_shouldSetEntAC() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Treebeard", "Ent", "Warrior");

            // Assert
            assertEquals(16, character.getArmorClass());
        }

        @Test
        @DisplayName("should set correct AC for Elf")
        void createCharacter_shouldSetElfAC() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Legolas", "Elf", "Ranger");

            // Assert
            assertEquals(12, character.getArmorClass());
        }

        @Test
        @DisplayName("should set correct AC for Hobbit")
        void createCharacter_shouldSetHobbitAC() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Frodo", "Hobbit", "Scout");

            // Assert
            assertEquals(10, character.getArmorClass());
        }

        @Test
        @DisplayName("should set default AC for unknown race")
        void createCharacter_shouldSetDefaultAC() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Aragorn", "Man", "Ranger");

            // Assert
            assertEquals(10, character.getArmorClass());
        }

        @Test
        @DisplayName("should set hit dice for Hobbit")
        void createCharacter_shouldSetHobbitHitDice() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Frodo", "Hobbit", "Scout");

            // Assert
            assertEquals("d6", character.getHitDice());
        }

        @Test
        @DisplayName("should set hit dice for Dwarf")
        void createCharacter_shouldSetDwarfHitDice() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Gimli", "Dwarf", "Warrior");

            // Assert
            assertEquals("d10", character.getHitDice());
        }

        @Test
        @DisplayName("should set hit dice for Ent")
        void createCharacter_shouldSetEntHitDice() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Treebeard", "Ent", "Warrior");

            // Assert
            assertEquals("d12", character.getHitDice());
        }

        @Test
        @DisplayName("should set hit dice for Elf")
        void createCharacter_shouldSetElfHitDice() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Legolas", "Elf", "Ranger");

            // Assert
            assertEquals("d8", character.getHitDice());
        }

        @Test
        @DisplayName("should set hit dice for Man")
        void createCharacter_shouldSetManHitDice() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Aragorn", "Man", "Ranger");

            // Assert
            assertEquals("d8", character.getHitDice());
        }

        @Test
        @DisplayName("should set hit dice for Maia")
        void createCharacter_shouldSetMaiaHitDice() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Gandalf", "Maia", "Mage");

            // Assert
            assertEquals("d8", character.getHitDice());
        }

        @Test
        @DisplayName("should set proficiency bonus to 2")
        void createCharacter_shouldSetProficiencyBonus() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Frodo", "Hobbit", "Scout");

            // Assert
            assertEquals(2, character.getProficiencyBonus());
        }

        @Test
        @DisplayName("should set default hope to 5")
        void createCharacter_shouldSetDefaultHope() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Frodo", "Hobbit", "Scout");

            // Assert
            assertEquals(5, character.getHope());
        }

        @Test
        @DisplayName("should set default shadow to 0")
        void createCharacter_shouldSetDefaultShadow() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Frodo", "Hobbit", "Scout");

            // Assert
            assertEquals(0, character.getShadow());
        }

        @Test
        @DisplayName("should set default corruption to 0")
        void createCharacter_shouldSetDefaultCorruption() {
            // Arrange
            when(characterRepository.save(any(LotrCharacter.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCharacter character = characterService.createCharacter("Frodo", "Hobbit", "Scout");

            // Assert
            assertEquals(0, character.getCorruption());
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return character by id")
        void findById_shouldReturnCharacter() {
            // Arrange
            LotrCharacter character = createCharacter(1L, "Frodo");
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act
            LotrCharacter result = characterService.findById(1L);

            // Assert
            assertEquals("Frodo", result.getName());
        }

        @Test
        @DisplayName("should throw when character not found")
        void findById_shouldThrowWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> characterService.findById(999L));
            assertTrue(ex.getMessage().contains("Character not found"));
            assertTrue(ex.getMessage().contains("999"));
        }
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all characters")
        void findAll_shouldReturnAll() {
            // Arrange
            List<LotrCharacter> characters = List.of(
                    createCharacter(1L, "Frodo"),
                    createCharacter(2L, "Aragorn")
            );
            when(characterRepository.findAll()).thenReturn(characters);

            // Act
            List<LotrCharacter> result = characterService.findAll();

            // Assert
            assertEquals(2, result.size());
            verify(characterRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no characters exist")
        void findAll_shouldReturnEmpty() {
            // Arrange
            when(characterRepository.findAll()).thenReturn(List.of());

            // Act
            List<LotrCharacter> result = characterService.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- findByRace ----

    @Nested
    @DisplayName("findByRace Tests")
    class FindByRaceTests {

        @Test
        @DisplayName("should return characters by race")
        void findByRace_shouldReturnCharacters() {
            // Arrange
            List<LotrCharacter> characters = List.of(
                    createCharacter(1L, "Frodo"),
                    createCharacter(2L, "Sam")
            );
            when(characterRepository.findByRace("Hobbit")).thenReturn(characters);

            // Act
            List<LotrCharacter> result = characterService.findByRace("Hobbit");

            // Assert
            assertEquals(2, result.size());
        }
    }

    // ---- findByCalling ----

    @Nested
    @DisplayName("findByCalling Tests")
    class FindByCallingTests {

        @Test
        @DisplayName("should return characters by calling")
        void findByCalling_shouldReturnCharacters() {
            // Arrange
            List<LotrCharacter> characters = List.of(
                    createCharacter(1L, "Frodo")
            );
            when(characterRepository.findByCalling("Scout")).thenReturn(characters);

            // Act
            List<LotrCharacter> result = characterService.findByCalling("Scout");

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- findRingBearers ----

    @Nested
    @DisplayName("findRingBearers Tests")
    class FindRingBearersTests {

        @Test
        @DisplayName("should return ring bearers")
        void findRingBearers_shouldReturnRingBearers() {
            // Arrange
            LotrCharacter frodo = createCharacter(1L, "Frodo");
            frodo.setOneRingBearer(true);
            when(characterRepository.findByOneRingBearerTrue()).thenReturn(List.of(frodo));

            // Act
            List<LotrCharacter> result = characterService.findRingBearers();

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isOneRingBearer());
        }
    }

    // ---- generateAbilityScores ----

    @Nested
    @DisplayName("generateAbilityScores Tests")
    class GenerateAbilityScoresTests {

        @Test
        @DisplayName("should return 6 scores")
        void generateAbilityScores_shouldReturnSixScores() {
            // Act
            int[] scores = characterService.generateAbilityScores();

            // Assert
            assertEquals(6, scores.length);
            for (int score : scores) {
                assertTrue(score >= 3 && score <= 18, "Score " + score + " out of range");
            }
        }

        @Test
        @DisplayName("should generate scores in valid range")
        void generateAbilityScores_shouldBeInRange() {
            // Act - roll multiple times
            for (int i = 0; i < 100; i++) {
                int[] scores = characterService.generateAbilityScores();
                for (int score : scores) {
                    assertTrue(score >= 3 && score <= 18);
                }
            }
        }
    }

    // ---- roll4d6DropLowest ----

    @Nested
    @DisplayName("roll4d6DropLowest Tests")
    class Roll4d6DropLowestTests {

        @Test
        @DisplayName("should return value between 3 and 18")
        void roll4d6DropLowest_shouldBeInRange() {
            // Act
            for (int i = 0; i < 100; i++) {
                int result = characterService.roll4d6DropLowest();
                assertTrue(result >= 3 && result <= 18, "Result " + result + " out of range");
            }
        }
    }

    // ---- calculateMaxHp ----

    @Nested
    @DisplayName("calculateMaxHp Tests")
    class CalculateMaxHpTests {

        @Test
        @DisplayName("should calculate Hobbit HP correctly")
        void calculateMaxHp_shouldCalculateHobbit() {
            // Act
            int hp = characterService.calculateMaxHp("Hobbit", "Scout", 14); // +2 CON mod

            // Assert
            assertEquals(8, hp); // 6 + 2
        }

        @Test
        @DisplayName("should calculate Dwarf HP correctly")
        void calculateMaxHp_shouldCalculateDwarf() {
            // Act
            int hp = characterService.calculateMaxHp("Dwarf", "Warrior", 16); // +3 CON mod

            // Assert
            assertEquals(13, hp); // 10 + 3
        }

        @Test
        @DisplayName("should calculate Ent HP correctly")
        void calculateMaxHp_shouldCalculateEnt() {
            // Act
            int hp = characterService.calculateMaxHp("Ent", "Warrior", 10); // 0 CON mod

            // Assert
            assertEquals(12, hp); // 12 + 0
        }

        @Test
        @DisplayName("should calculate Elf HP correctly")
        void calculateMaxHp_shouldCalculateElf() {
            // Act
            int hp = characterService.calculateMaxHp("Elf", "Ranger", 12); // +1 CON mod

            // Assert
            assertEquals(9, hp); // 8 + 1
        }

        @Test
        @DisplayName("should calculate Man HP correctly")
        void calculateMaxHp_shouldCalculateMan() {
            // Act
            int hp = characterService.calculateMaxHp("Man", "Warrior", 8); // -1 CON mod

            // Assert
            assertEquals(8, hp); // 8 + 0 (clamped)
        }

        @Test
        @DisplayName("should calculate Maia HP correctly")
        void calculateMaxHp_shouldCalculateMaia() {
            // Act
            int hp = characterService.calculateMaxHp("Maia", "Mage", 10); // 0 CON mod

            // Assert
            assertEquals(8, hp); // 8 + 0
        }

        @Test
        @DisplayName("should default to 8 HP for unknown race")
        void calculateMaxHp_shouldDefaultForUnknownRace() {
            // Act
            int hp = characterService.calculateMaxHp("Unknown", "Scout", 10);

            // Assert
            assertEquals(8, hp); // 8 + 0
        }

        @Test
        @DisplayName("should clamp negative CON mod to 0")
        void calculateMaxHp_shouldClampNegativeConMod() {
            // Act
            int hp = characterService.calculateMaxHp("Hobbit", "Scout", 4); // -3 CON mod

            // Assert
            assertEquals(6, hp); // 6 + 0 (clamped)
        }
    }

    // ---- calculateBaseAc ----

    @Nested
    @DisplayName("calculateBaseAc Tests")
    class CalculateBaseAcTests {

        @Test
        @DisplayName("should return 14 for Dwarf")
        void calculateBaseAc_shouldReturn14ForDwarf() {
            assertEquals(14, characterService.calculateBaseAc("Dwarf"));
        }

        @Test
        @DisplayName("should return 16 for Ent")
        void calculateBaseAc_shouldReturn16ForEnt() {
            assertEquals(16, characterService.calculateBaseAc("Ent"));
        }

        @Test
        @DisplayName("should return 12 for Elf")
        void calculateBaseAc_shouldReturn12ForElf() {
            assertEquals(12, characterService.calculateBaseAc("Elf"));
        }

        @Test
        @DisplayName("should return 10 for Hobbit")
        void calculateBaseAc_shouldReturn10ForHobbit() {
            assertEquals(10, characterService.calculateBaseAc("Hobbit"));
        }

        @Test
        @DisplayName("should return 10 for unknown race")
        void calculateBaseAc_shouldReturn10ForUnknown() {
            assertEquals(10, characterService.calculateBaseAc("Unknown"));
        }
    }

    // ---- getHitDieForRace ----

    @Nested
    @DisplayName("getHitDieForRace Tests")
    class GetHitDieForRaceTests {

        @Test
        @DisplayName("should return d12 for Ent")
        void getHitDieForRace_shouldReturnD12ForEnt() {
            assertEquals("d12", characterService.getHitDieForRace("Ent"));
        }

        @Test
        @DisplayName("should return d10 for Dwarf")
        void getHitDieForRace_shouldReturnD10ForDwarf() {
            assertEquals("d10", characterService.getHitDieForRace("Dwarf"));
        }

        @Test
        @DisplayName("should return d8 for Man")
        void getHitDieForRace_shouldReturnD8ForMan() {
            assertEquals("d8", characterService.getHitDieForRace("Man"));
        }

        @Test
        @DisplayName("should return d8 for Elf")
        void getHitDieForRace_shouldReturnD8ForElf() {
            assertEquals("d8", characterService.getHitDieForRace("Elf"));
        }

        @Test
        @DisplayName("should return d6 for Hobbit")
        void getHitDieForRace_shouldReturnD6ForHobbit() {
            assertEquals("d6", characterService.getHitDieForRace("Hobbit"));
        }

        @Test
        @DisplayName("should return d8 for unknown race")
        void getHitDieForRace_shouldReturnD8ForUnknown() {
            assertEquals("d8", characterService.getHitDieForRace("Unknown"));
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
            assertEquals(3, characterService.getProficiencyBonus(8));
        }

        @Test
        @DisplayName("should return +4 for level 9-12")
        void getProficiencyBonus_shouldReturn4ForLevel9to12() {
            assertEquals(4, characterService.getProficiencyBonus(9));
            assertEquals(4, characterService.getProficiencyBonus(12));
        }

        @Test
        @DisplayName("should return +5 for level 13-16")
        void getProficiencyBonus_shouldReturn5ForLevel13to16() {
            assertEquals(5, characterService.getProficiencyBonus(13));
            assertEquals(5, characterService.getProficiencyBonus(16));
        }

        @Test
        @DisplayName("should return +6 for level 17+")
        void getProficiencyBonus_shouldReturn6ForLevel17Plus() {
            assertEquals(6, characterService.getProficiencyBonus(17));
            assertEquals(6, characterService.getProficiencyBonus(20));
        }
    }

    // ---- delete ----

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("should delete character by id")
        void delete_shouldDelete() {
            // Act
            characterService.delete(1L);

            // Assert
            verify(characterRepository).deleteById(1L);
        }
    }

    // ---- Helper methods ----

    private LotrCharacter createCharacter(Long id, String name) {
        LotrCharacter character = new LotrCharacter();
        character.setId(id);
        character.setName(name);
        character.setRace("Hobbit");
        character.setCalling("Scout");
        character.setLevel(1);
        character.setStrScore(10);
        character.setDexScore(14);
        character.setConScore(12);
        character.setIntScore(8);
        character.setWisScore(12);
        character.setChaScore(16);
        character.setHitPoints(8);
        character.setMaxHitPoints(8);
        character.setArmorClass(10);
        character.setCorruption(0);
        character.setHope(5);
        character.setShadow(0);
        return character;
    }
}

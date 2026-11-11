package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eExhaustion;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eExhaustionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Dnd5eExhaustionService Tests")
class Dnd5eExhaustionServiceTest {

    private Dnd5eExhaustionRepository exhaustionRepository;
    private Dnd5eCharacterRepository characterRepository;
    private Dnd5eExhaustionService service;

    private Dnd5eCharacter character;
    private Dnd5eCharacter exhaustedCharacter;

    @BeforeEach
    void setUp() {
        exhaustionRepository = mock(Dnd5eExhaustionRepository.class);
        characterRepository = mock(Dnd5eCharacterRepository.class);
        service = new Dnd5eExhaustionService(exhaustionRepository, characterRepository);

        // Character with no exhaustion
        character = new Dnd5eCharacter();
        character.setId(1L);
        character.setName("Frodo");
        character.setRace("Halfling");
        character.setCharacterClass("Rogue");
        character.setLevel(3);
        character.setStrScore(10);
        character.setDexScore(16);
        character.setConScore(14);
        character.setIntScore(12);
        character.setWisScore(12);
        character.setChaScore(10);
        character.setProficiencyBonus(2);
        character.setHitPoints(24);
        character.setMaxHitPoints(24);
        character.setSpeed(25);
        character.setExhaustionLevel(0);

        // Character with exhaustion level 3
        exhaustedCharacter = new Dnd5eCharacter();
        exhaustedCharacter.setId(2L);
        exhaustedCharacter.setName("Aragorn");
        exhaustedCharacter.setRace("Human");
        exhaustedCharacter.setCharacterClass("Fighter");
        exhaustedCharacter.setLevel(5);
        exhaustedCharacter.setStrScore(16);
        exhaustedCharacter.setDexScore(12);
        exhaustedCharacter.setConScore(14);
        exhaustedCharacter.setIntScore(10);
        exhaustedCharacter.setWisScore(12);
        exhaustedCharacter.setChaScore(14);
        exhaustedCharacter.setProficiencyBonus(3);
        exhaustedCharacter.setHitPoints(45);
        exhaustedCharacter.setMaxHitPoints(45);
        exhaustedCharacter.setSpeed(30);
        exhaustedCharacter.setExhaustionLevel(3);
    }

    // ---- getExhaustionLevel ----

    @Nested
    @DisplayName("getExhaustionLevel Tests")
    class GetExhaustionLevelTests {

        @Test
        @DisplayName("should return 0 for non-exhausted character")
        void shouldReturnZero() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act
            int level = service.getExhaustionLevel(1L);

            // Assert
            assertEquals(0, level);
        }

        @Test
        @DisplayName("should return current exhaustion level")
        void shouldReturnCurrentLevel() {
            // Arrange
            when(characterRepository.findById(2L)).thenReturn(Optional.of(exhaustedCharacter));

            // Act
            int level = service.getExhaustionLevel(2L);

            // Assert
            assertEquals(3, level);
        }

        @Test
        @DisplayName("should throw if character not found")
        void shouldThrowWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.getExhaustionLevel(999L));
        }
    }

    // ---- gainExhaustion ----

    @Nested
    @DisplayName("gainExhaustion Tests")
    class GainExhaustionTests {

        @Test
        @DisplayName("should gain 1 level of exhaustion")
        void shouldGainOneLevel() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.gainExhaustion(1L, 1, "Starvation");

            // Assert
            assertEquals(1, result.getExhaustionLevel());
            verify(characterRepository).save(character);
            verify(exhaustionRepository).save(any(Dnd5eExhaustion.class));
        }

        @Test
        @DisplayName("should gain multiple levels of exhaustion")
        void shouldGainMultipleLevels() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.gainExhaustion(1L, 3, "Extreme Cold");

            // Assert
            assertEquals(3, result.getExhaustionLevel());
        }

        @Test
        @DisplayName("should cap at max exhaustion level 6")
        void shouldCapAtMaxLevel() {
            // Arrange
            when(characterRepository.findById(2L)).thenReturn(Optional.of(exhaustedCharacter));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.gainExhaustion(2L, 10, "DM Award");

            // Assert
            assertEquals(6, result.getExhaustionLevel());
        }

        @Test
        @DisplayName("should log event with description")
        void shouldLogEventWithDescription() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            service.gainExhaustion(1L, 1, "Starvation", "Went a day without food");

            // Assert
            verify(exhaustionRepository).save(argThat(event ->
                    "Went a day without food".equals(event.getDescription())
            ));
        }

        @Test
        @DisplayName("should throw if character not found")
        void shouldThrowWhenNotFound() {
            // Arrange
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> service.gainExhaustion(999L, 1, "Starvation"));
        }
    }

    // ---- removeExhaustion ----

    @Nested
    @DisplayName("removeExhaustion Tests")
    class RemoveExhaustionTests {

        @Test
        @DisplayName("should remove 1 level of exhaustion")
        void shouldRemoveOneLevel() {
            // Arrange
            when(characterRepository.findById(2L)).thenReturn(Optional.of(exhaustedCharacter));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.removeExhaustion(2L, 1, "Long Rest");

            // Assert
            assertEquals(2, result.getExhaustionLevel());
        }

        @Test
        @DisplayName("should not go below 0")
        void shouldNotGoBelowZero() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act
            Dnd5eCharacter result = service.removeExhaustion(1L, 5, "Long Rest");

            // Assert
            assertEquals(0, result.getExhaustionLevel());
            // No save should happen since level didn't change
            verify(characterRepository, never()).save(any());
        }

        @Test
        @DisplayName("should remove multiple levels")
        void shouldRemoveMultipleLevels() {
            // Arrange
            when(characterRepository.findById(2L)).thenReturn(Optional.of(exhaustedCharacter));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.removeExhaustion(2L, 3, "Greater Restoration");

            // Assert
            assertEquals(0, result.getExhaustionLevel());
        }
    }

    // ---- setExhaustionLevel ----

    @Nested
    @DisplayName("setExhaustionLevel Tests")
    class SetExhaustionLevelTests {

        @Test
        @DisplayName("should set exhaustion to specified level")
        void shouldSetLevel() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.setExhaustionLevel(1L, 4, "DM Adjustment");

            // Assert
            assertEquals(4, result.getExhaustionLevel());
        }

        @Test
        @DisplayName("should clamp negative levels to 0")
        void shouldClampNegativeToZero() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act
            Dnd5eCharacter result = service.setExhaustionLevel(1L, -5, "DM Adjustment");

            // Assert
            assertEquals(0, result.getExhaustionLevel());
        }

        @Test
        @DisplayName("should clamp levels above 6 to 6")
        void shouldClampAboveSixToSix() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.setExhaustionLevel(1L, 10, "DM Adjustment");

            // Assert
            assertEquals(6, result.getExhaustionLevel());
        }

        @Test
        @DisplayName("should not save if level unchanged")
        void shouldNotSaveIfUnchanged() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act
            service.setExhaustionLevel(1L, 0, "DM Adjustment");

            // Assert
            verify(characterRepository, never()).save(any());
        }
    }

    // ---- clearExhaustion ----

    @Nested
    @DisplayName("clearExhaustion Tests")
    class ClearExhaustionTests {

        @Test
        @DisplayName("should clear all exhaustion")
        void shouldClearAllExhaustion() {
            // Arrange
            when(characterRepository.findById(2L)).thenReturn(Optional.of(exhaustedCharacter));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.clearExhaustion(2L);

            // Assert
            assertEquals(0, result.getExhaustionLevel());
        }

        @Test
        @DisplayName("should do nothing if already at 0")
        void shouldDoNothingIfZero() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));

            // Act
            Dnd5eCharacter result = service.clearExhaustion(1L);

            // Assert
            assertEquals(0, result.getExhaustionLevel());
            verify(characterRepository, never()).save(any());
        }
    }

    // ---- Exhaustion Effects ----

    @Nested
    @DisplayName("Exhaustion Effects Tests")
    class ExhaustionEffectsTests {

        @Test
        @DisplayName("should calculate d20 penalty for level 0")
        void d20PenaltyLevel0() {
            assertEquals(0, service.getD20TestPenalty(0));
        }

        @Test
        @DisplayName("should calculate d20 penalty for level 1")
        void d20PenaltyLevel1() {
            assertEquals(-2, service.getD20TestPenalty(1));
        }

        @Test
        @DisplayName("should calculate d20 penalty for level 3")
        void d20PenaltyLevel3() {
            assertEquals(-6, service.getD20TestPenalty(3));
        }

        @Test
        @DisplayName("should calculate d20 penalty for level 6")
        void d20PenaltyLevel6() {
            assertEquals(-12, service.getD20TestPenalty(6));
        }

        @Test
        @DisplayName("should calculate speed reduction for level 0")
        void speedReductionLevel0() {
            assertEquals(0, service.getSpeedReduction(0));
        }

        @Test
        @DisplayName("should calculate speed reduction for level 1")
        void speedReductionLevel1() {
            assertEquals(5, service.getSpeedReduction(1));
        }

        @Test
        @DisplayName("should calculate speed reduction for level 6")
        void speedReductionLevel6() {
            assertEquals(30, service.getSpeedReduction(6));
        }

        @Test
        @DisplayName("should calculate effective speed with no exhaustion")
        void effectiveSpeedNoExhaustion() {
            assertEquals(30, service.getEffectiveSpeed(30, 0));
        }

        @Test
        @DisplayName("should calculate effective speed with exhaustion")
        void effectiveSpeedWithExhaustion() {
            assertEquals(20, service.getEffectiveSpeed(30, 2));
        }

        @Test
        @DisplayName("should not reduce speed below 0")
        void effectiveSpeedMinimumZero() {
            assertEquals(0, service.getEffectiveSpeed(25, 6));
        }

        @Test
        @DisplayName("should get character d20 penalty")
        void characterD20Penalty() {
            // Arrange
            when(characterRepository.findById(2L)).thenReturn(Optional.of(exhaustedCharacter));

            // Act
            int penalty = service.getCharacterD20Penalty(2L);

            // Assert
            assertEquals(-6, penalty);
        }

        @Test
        @DisplayName("should get character effective speed")
        void characterEffectiveSpeed() {
            // Arrange
            when(characterRepository.findById(2L)).thenReturn(Optional.of(exhaustedCharacter));

            // Act
            int speed = service.getCharacterEffectiveSpeed(2L);

            // Assert
            assertEquals(15, speed); // 30 - (5 * 3) = 15
        }
    }

    // ---- Exhaustion History ----

    @Nested
    @DisplayName("Exhaustion History Tests")
    class ExhaustionHistoryTests {

        @Test
        @DisplayName("should return exhaustion history")
        void shouldReturnHistory() {
            // Arrange
            Dnd5eExhaustion event = new Dnd5eExhaustion();
            event.setId(1L);
            event.setCharacterId(1L);
            event.setCharacterName("Frodo");
            event.setLevelChange(1);
            event.setResultingLevel(1);
            event.setSource("Starvation");
            event.setDescription("Went a day without food");

            when(exhaustionRepository.findExhaustionHistory(1L)).thenReturn(List.of(event));

            // Act
            List<Dnd5eExhaustion> history = service.getExhaustionHistory(1L);

            // Assert
            assertEquals(1, history.size());
            assertEquals("Starvation", history.get(0).getSource());
        }

        @Test
        @DisplayName("should return empty list if no history")
        void shouldReturnEmptyHistory() {
            // Arrange
            when(exhaustionRepository.findExhaustionHistory(1L)).thenReturn(Collections.emptyList());

            // Act
            List<Dnd5eExhaustion> history = service.getExhaustionHistory(1L);

            // Assert
            assertTrue(history.isEmpty());
        }

        @Test
        @DisplayName("should list all exhaustion events")
        void shouldListAllEvents() {
            // Arrange
            when(exhaustionRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<Dnd5eExhaustion> events = service.listAllExhaustionEvents();

            // Assert
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("should list exhaustion by source")
        void shouldListBySource() {
            // Arrange
            when(exhaustionRepository.findBySource("Starvation")).thenReturn(Collections.emptyList());

            // Act
            List<Dnd5eExhaustion> events = service.listExhaustionBySource("Starvation");

            // Assert
            assertTrue(events.isEmpty());
        }

        @Test
        @DisplayName("should list characters with exhaustion")
        void shouldListCharactersWithExhaustion() {
            // Arrange
            when(exhaustionRepository.findCharactersWithExhaustion()).thenReturn(Collections.emptyList());

            // Act
            List<Dnd5eExhaustion> events = service.listCharactersWithExhaustion();

            // Assert
            assertTrue(events.isEmpty());
        }
    }

    // ---- Common Exhaustion Sources ----

    @Nested
    @DisplayName("Common Exhaustion Sources Tests")
    class CommonExhaustionSourcesTests {

        @Test
        @DisplayName("should apply starvation")
        void shouldApplyStarvation() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.applyStarvation(1L);

            // Assert
            assertEquals(1, result.getExhaustionLevel());
            verify(exhaustionRepository).save(argThat(event ->
                    "Starvation".equals(event.getSource())
            ));
        }

        @Test
        @DisplayName("should apply dehydration")
        void shouldApplyDehydration() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.applyDehydration(1L);

            // Assert
            assertEquals(1, result.getExhaustionLevel());
            verify(exhaustionRepository).save(argThat(event ->
                    "Dehydration".equals(event.getSource())
            ));
        }

        @Test
        @DisplayName("should apply extreme heat")
        void shouldApplyExtremeHeat() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.applyExtremeTemperature(1L, true);

            // Assert
            assertEquals(1, result.getExhaustionLevel());
            verify(exhaustionRepository).save(argThat(event ->
                    "Extreme Heat".equals(event.getSource())
            ));
        }

        @Test
        @DisplayName("should apply extreme cold")
        void shouldApplyExtremeCold() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.applyExtremeTemperature(1L, false);

            // Assert
            assertEquals(1, result.getExhaustionLevel());
            verify(exhaustionRepository).save(argThat(event ->
                    "Extreme Cold".equals(event.getSource())
            ));
        }

        @Test
        @DisplayName("should apply sleep deprivation")
        void shouldApplySleepDeprivation() {
            // Arrange
            when(characterRepository.findById(1L)).thenReturn(Optional.of(character));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.applySleepDeprivation(1L);

            // Assert
            assertEquals(1, result.getExhaustionLevel());
            verify(exhaustionRepository).save(argThat(event ->
                    "Sleep Deprivation".equals(event.getSource())
            ));
        }

        @Test
        @DisplayName("should remove exhaustion after long rest")
        void shouldRemoveAfterLongRest() {
            // Arrange
            when(characterRepository.findById(2L)).thenReturn(Optional.of(exhaustedCharacter));
            when(characterRepository.save(any(Dnd5eCharacter.class))).thenAnswer(inv -> inv.getArgument(0));
            when(exhaustionRepository.save(any(Dnd5eExhaustion.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCharacter result = service.removeExhaustionAfterLongRest(2L);

            // Assert
            assertEquals(2, result.getExhaustionLevel());
            verify(exhaustionRepository).save(argThat(event ->
                    "Long Rest".equals(event.getSource())
            ));
        }
    }

    // ---- Constants ----

    @Nested
    @DisplayName("Constants Tests")
    class ConstantsTests {

        @Test
        @DisplayName("MAX_EXHAUSTION_LEVEL should be 6")
        void maxExhaustionLevel() {
            assertEquals(6, Dnd5eExhaustionService.MAX_EXHAUSTION_LEVEL);
        }

        @Test
        @DisplayName("MIN_EXHAUSTION_LEVEL should be 0")
        void minExhaustionLevel() {
            assertEquals(0, Dnd5eExhaustionService.MIN_EXHAUSTION_LEVEL);
        }

        @Test
        @DisplayName("D20_PENALTY_MULTIPLIER should be 2")
        void d20PenaltyMultiplier() {
            assertEquals(2, Dnd5eExhaustionService.D20_PENALTY_MULTIPLIER);
        }

        @Test
        @DisplayName("SPEED_REDUCTION_PER_LEVEL should be 5")
        void speedReductionPerLevel() {
            assertEquals(5, Dnd5eExhaustionService.SPEED_REDUCTION_PER_LEVEL);
        }
    }
}

package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.DiceRollResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dnd5eDiceService Tests")
class Dnd5eDiceServiceTest {

    private Dnd5eDiceService diceService;

    @BeforeEach
    void setUp() {
        diceService = new Dnd5eDiceService();
    }

    // ---- rollDice ----

    @Nested
    @DisplayName("rollDice Tests")
    class RollDiceTests {

        @Test
        @DisplayName("should roll a single d20")
        void rollDice_shouldRollSingleD20() {
            // Act
            DiceRollResponse response = diceService.rollDice("d20");

            // Assert
            assertNotNull(response);
            assertEquals(1, response.getIndividualRolls().size());
            assertTrue(response.getIndividualRolls().get(0) >= 1 && response.getIndividualRolls().get(0) <= 20);
            assertEquals(response.getIndividualRolls().get(0), response.getTotal());
            assertNotNull(response.getDescription());
        }

        @Test
        @DisplayName("should roll 2d6")
        void rollDice_shouldRoll2d6() {
            // Act
            DiceRollResponse response = diceService.rollDice("2d6");

            // Assert
            assertEquals(2, response.getIndividualRolls().size());
            for (int roll : response.getIndividualRolls()) {
                assertTrue(roll >= 1 && roll <= 6);
            }
            int sum = response.getIndividualRolls().stream().mapToInt(Integer::intValue).sum();
            assertEquals(sum, response.getTotal());
        }

        @Test
        @DisplayName("should roll 3d8+2 with positive modifier")
        void rollDice_shouldRollWithPositiveModifier() {
            // Act
            DiceRollResponse response = diceService.rollDice("3d8+2");

            // Assert
            assertEquals(3, response.getIndividualRolls().size());
            for (int roll : response.getIndividualRolls()) {
                assertTrue(roll >= 1 && roll <= 8);
            }
            int sum = response.getIndividualRolls().stream().mapToInt(Integer::intValue).sum();
            assertEquals(sum + 2, response.getTotal());
        }

        @Test
        @DisplayName("should roll 2d6-1 with negative modifier")
        void rollDice_shouldRollWithNegativeModifier() {
            // Act
            DiceRollResponse response = diceService.rollDice("2d6-1");

            // Assert
            assertEquals(2, response.getIndividualRolls().size());
            int sum = response.getIndividualRolls().stream().mapToInt(Integer::intValue).sum();
            assertEquals(sum - 1, response.getTotal());
        }

        @Test
        @DisplayName("should roll 4d6dl dropping lowest")
        void rollDice_shouldDropLowest() {
            // Act
            DiceRollResponse response = diceService.rollDice("4d6dl");

            // Assert
            // After dropping lowest, 3 rolls remain
            assertEquals(3, response.getIndividualRolls().size());
            for (int roll : response.getIndividualRolls()) {
                assertTrue(roll >= 1 && roll <= 6);
            }
            // Total should be sum of remaining rolls (lowest dropped)
            int sum = response.getIndividualRolls().stream().mapToInt(Integer::intValue).sum();
            assertEquals(sum, response.getTotal());
        }

        @Test
        @DisplayName("should roll d4")
        void rollDice_shouldRollD4() {
            // Act
            DiceRollResponse response = diceService.rollDice("d4");

            // Assert
            int roll = response.getIndividualRolls().get(0);
            assertTrue(roll >= 1 && roll <= 4);
        }

        @Test
        @DisplayName("should roll d6")
        void rollDice_shouldRollD6() {
            // Act
            DiceRollResponse response = diceService.rollDice("d6");

            // Assert
            int roll = response.getIndividualRolls().get(0);
            assertTrue(roll >= 1 && roll <= 6);
        }

        @Test
        @DisplayName("should roll d8")
        void rollDice_shouldRollD8() {
            // Act
            DiceRollResponse response = diceService.rollDice("d8");

            // Assert
            int roll = response.getIndividualRolls().get(0);
            assertTrue(roll >= 1 && roll <= 8);
        }

        @Test
        @DisplayName("should roll d10")
        void rollDice_shouldRollD10() {
            // Act
            DiceRollResponse response = diceService.rollDice("d10");

            // Assert
            int roll = response.getIndividualRolls().get(0);
            assertTrue(roll >= 1 && roll <= 10);
        }

        @Test
        @DisplayName("should roll d12")
        void rollDice_shouldRollD12() {
            // Act
            DiceRollResponse response = diceService.rollDice("d12");

            // Assert
            int roll = response.getIndividualRolls().get(0);
            assertTrue(roll >= 1 && roll <= 12);
        }

        @Test
        @DisplayName("should roll d100")
        void rollDice_shouldRollD100() {
            // Act
            DiceRollResponse response = diceService.rollDice("d100");

            // Assert
            int roll = response.getIndividualRolls().get(0);
            assertTrue(roll >= 1 && roll <= 100);
        }

        @Test
        @DisplayName("should handle case insensitive formula")
        void rollDice_shouldBeCaseInsensitive() {
            // Act
            DiceRollResponse response1 = diceService.rollDice("2d6");
            DiceRollResponse response2 = diceService.rollDice("2D6");
            DiceRollResponse response3 = diceService.rollDice("2D6DL");

            // Assert
            assertEquals(2, response1.getIndividualRolls().size());
            assertEquals(2, response2.getIndividualRolls().size());
            assertEquals(1, response3.getIndividualRolls().size()); // 2d6dl drops 1
        }

        @Test
        @DisplayName("should throw on null formula")
        void rollDice_shouldThrowOnNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> diceService.rollDice(null));
        }

        @Test
        @DisplayName("should throw on empty formula")
        void rollDice_shouldThrowOnEmpty() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> diceService.rollDice(""));
        }

        @Test
        @DisplayName("should throw on invalid formula")
        void rollDice_shouldThrowOnInvalidFormula() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> diceService.rollDice("abc"));
        }

        @Test
        @DisplayName("should throw on unsupported dice type")
        void rollDice_shouldThrowOnUnsupportedDice() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> diceService.rollDice("d5"));
        }

        @Test
        @DisplayName("should set correct formula in response")
        void rollDice_shouldSetFormula() {
            // Act
            DiceRollResponse response = diceService.rollDice("2d6+3");

            // Assert
            assertEquals("2d6+3", response.getFormula());
        }

        @Test
        @DisplayName("should include modifier in description")
        void rollDice_shouldIncludeModifierInDescription() {
            // Act
            DiceRollResponse response = diceService.rollDice("2d6+3");

            // Assert
            assertTrue(response.getDescription().contains("+3"));
        }

        @Test
        @DisplayName("should include drop lowest in description")
        void rollDice_shouldIncludeDropLowestInDescription() {
            // Act
            DiceRollResponse response = diceService.rollDice("4d6dl");

            // Assert
            assertTrue(response.getDescription().toLowerCase().contains("drop lowest"));
        }
    }

    // ---- rollWithAdvantage ----

    @Nested
    @DisplayName("rollWithAdvantage Tests")
    class RollWithAdvantageTests {

        @Test
        @DisplayName("should return higher of two rolls")
        void rollWithAdvantage_shouldReturnHigher() {
            // Act
            DiceRollResponse response = diceService.rollWithAdvantage("d20");

            // Assert
            assertNotNull(response);
            // The total should be within valid d20 range
            assertTrue(response.getTotal() >= 1 && response.getTotal() <= 20);
            assertTrue(response.getFormula().contains("advantage"));
        }

        @Test
        @DisplayName("should work with multi-dice formula")
        void rollWithAdvantage_shouldWorkWithMultiDice() {
            // Act
            DiceRollResponse response = diceService.rollWithAdvantage("2d6+3");

            // Assert
            assertNotNull(response);
            assertTrue(response.getTotal() >= 1);
            assertTrue(response.getFormula().contains("advantage"));
        }

        @Test
        @DisplayName("should throw on null formula")
        void rollWithAdvantage_shouldThrowOnNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> diceService.rollWithAdvantage(null));
        }
    }

    // ---- rollWithDisadvantage ----

    @Nested
    @DisplayName("rollWithDisadvantage Tests")
    class RollWithDisadvantageTests {

        @Test
        @DisplayName("should return lower of two rolls")
        void rollWithDisadvantage_shouldReturnLower() {
            // Act
            DiceRollResponse response = diceService.rollWithDisadvantage("d20");

            // Assert
            assertNotNull(response);
            assertTrue(response.getTotal() >= 1 && response.getTotal() <= 20);
            assertTrue(response.getFormula().contains("disadvantage"));
        }

        @Test
        @DisplayName("should work with multi-dice formula")
        void rollWithDisadvantage_shouldWorkWithMultiDice() {
            // Act
            DiceRollResponse response = diceService.rollWithDisadvantage("2d6+3");

            // Assert
            assertNotNull(response);
            assertTrue(response.getTotal() >= 1);
            assertTrue(response.getFormula().contains("disadvantage"));
        }

        @Test
        @DisplayName("should throw on null formula")
        void rollWithDisadvantage_shouldThrowOnNull() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> diceService.rollWithDisadvantage(null));
        }
    }

    // ---- rollMultiple ----

    @Nested
    @DisplayName("rollMultiple Tests")
    class RollMultipleTests {

        @Test
        @DisplayName("should roll multiple formulas and combine totals")
        void rollMultiple_shouldCombineTotals() {
            // Arrange
            List<String> formulas = List.of("2d6", "d20");

            // Act
            DiceRollResponse response = diceService.rollMultiple(formulas);

            // Assert
            assertNotNull(response);
            // Should have rolls from both formulas: 2d6 (2 rolls) + d20 (1 roll) = 3 rolls
            assertEquals(3, response.getIndividualRolls().size());
            // Total should be sum of all individual rolls
            int sum = response.getIndividualRolls().stream().mapToInt(Integer::intValue).sum();
            assertEquals(sum, response.getTotal());
        }

        @Test
        @DisplayName("should join formulas in response")
        void rollMultiple_shouldJoinFormulas() {
            // Arrange
            List<String> formulas = List.of("2d6", "d20");

            // Act
            DiceRollResponse response = diceService.rollMultiple(formulas);

            // Assert
            assertEquals("2d6 + d20", response.getFormula());
        }

        @Test
        @DisplayName("should handle single formula")
        void rollMultiple_shouldHandleSingleFormula() {
            // Arrange
            List<String> formulas = List.of("d20");

            // Act
            DiceRollResponse response = diceService.rollMultiple(formulas);

            // Assert
            assertEquals(1, response.getIndividualRolls().size());
        }

        @Test
        @DisplayName("should handle three formulas")
        void rollMultiple_shouldHandleThreeFormulas() {
            // Arrange
            List<String> formulas = List.of("d4", "d6", "d8");

            // Act
            DiceRollResponse response = diceService.rollMultiple(formulas);

            // Assert
            assertEquals(3, response.getIndividualRolls().size());
        }

        @Test
        @DisplayName("should include all descriptions")
        void rollMultiple_shouldIncludeDescriptions() {
            // Arrange
            List<String> formulas = List.of("2d6", "d20");

            // Act
            DiceRollResponse response = diceService.rollMultiple(formulas);

            // Assert
            assertNotNull(response.getDescription());
            assertTrue(response.getDescription().contains(";"));
        }
    }
}

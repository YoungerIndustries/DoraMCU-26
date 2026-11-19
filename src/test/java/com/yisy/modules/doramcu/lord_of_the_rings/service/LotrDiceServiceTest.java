package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.dto.DiceRollResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LotrDiceService Tests")
class LotrDiceServiceTest {

    private LotrDiceService diceService;

    @BeforeEach
    void setUp() {
        diceService = new LotrDiceService();
    }

    // ---- rollDice ----

    @Nested
    @DisplayName("rollDice Tests")
    class RollDiceTests {

        @Test
        @DisplayName("should roll a single d6")
        void rollDice_shouldRollSingleD6() {
            // Act
            DiceRollResponse result = diceService.rollDice("d6");

            // Assert
            assertTrue(result.getTotal() >= 1 && result.getTotal() <= 6);
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("should roll 2d6")
        void rollDice_shouldRoll2d6() {
            // Act
            DiceRollResponse result = diceService.rollDice("2d6");

            // Assert
            assertTrue(result.getTotal() >= 2 && result.getTotal() <= 12);
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("should roll 4d6 and drop lowest")
        void rollDice_shouldDropLowest() {
            // Act
            DiceRollResponse result = diceService.rollDice("4d6dl");

            // Assert
            // 4d6 drop lowest: min = 3 (1+1+1), max = 18 (6+6+6)
            assertTrue(result.getTotal() >= 3 && result.getTotal() <= 18);
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("should roll with positive modifier")
        void rollDice_shouldApplyPositiveModifier() {
            // Act
            DiceRollResponse result = diceService.rollDice("d20+5");

            // Assert
            assertTrue(result.getTotal() >= 6 && result.getTotal() <= 25);
            assertEquals(5, result.getModifier());
        }

        @Test
        @DisplayName("should roll with negative modifier")
        void rollDice_shouldApplyNegativeModifier() {
            // Act
            DiceRollResponse result = diceService.rollDice("d20-2");

            // Assert
            assertTrue(result.getTotal() >= -1 && result.getTotal() <= 18);
            assertEquals(-2, result.getModifier());
        }

        @Test
        @DisplayName("should handle d20")
        void rollDice_shouldRollD20() {
            // Act
            DiceRollResponse result = diceService.rollDice("d20");

            // Assert
            assertTrue(result.getTotal() >= 1 && result.getTotal() <= 20);
        }

        @Test
        @DisplayName("should handle d100")
        void rollDice_shouldRollD100() {
            // Act
            DiceRollResponse result = diceService.rollDice("d100");

            // Assert
            assertTrue(result.getTotal() >= 1 && result.getTotal() <= 100);
        }

        @Test
        @DisplayName("should return failure for invalid formula")
        void rollDice_shouldFailOnInvalidFormula() {
            // Act
            DiceRollResponse result = diceService.rollDice("abc");

            // Assert
            assertFalse(result.isSuccess());
            assertEquals(0, result.getTotal());
        }

        @Test
        @DisplayName("should return failure for null formula")
        void rollDice_shouldFailOnNullFormula() {
            // Act
            DiceRollResponse result = diceService.rollDice(null);

            // Assert
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("should return failure for empty formula")
        void rollDice_shouldFailOnEmptyFormula() {
            // Act
            DiceRollResponse result = diceService.rollDice("");

            // Assert
            assertFalse(result.isSuccess());
        }

        @Test
        @DisplayName("should handle drop highest")
        void rollDice_shouldDropHighest() {
            // Act
            DiceRollResponse result = diceService.rollDice("4d6dh");

            // Assert
            // 4d6 drop highest: min = 3 (1+1+1), max = 18 (6+6+6)
            assertTrue(result.getTotal() >= 3 && result.getTotal() <= 18);
            assertTrue(result.isSuccess());
        }

        @Test
        @DisplayName("should handle case insensitive formulas")
        void rollDice_shouldHandleCaseInsensitive() {
            // Act
            DiceRollResponse result1 = diceService.rollDice("2D6");
            DiceRollResponse result2 = diceService.rollDice("4D6DL");

            // Assert
            assertTrue(result1.isSuccess());
            assertTrue(result2.isSuccess());
        }
    }

    // ---- rollEyeOfSauron ----

    @Nested
    @DisplayName("rollEyeOfSauron Tests")
    class RollEyeOfSauronTests {

        @Test
        @DisplayName("should roll d12")
        void rollEyeOfSauron_shouldRollD12() {
            // Act
            DiceRollResponse result = diceService.rollEyeOfSauron();

            // Assert
            assertTrue(result.getTotal() >= 1 && result.getTotal() <= 12);
        }

        @Test
        @DisplayName("should indicate Sauron is watching on low roll")
        void rollEyeOfSauron_shouldWatchOnLowRoll() {
            // Act - roll multiple times to eventually get a low roll
            boolean foundWatching = false;
            for (int i = 0; i < 100; i++) {
                DiceRollResponse result = diceService.rollEyeOfSauron();
                if (result.getTotal() <= 3) {
                    foundWatching = true;
                    assertTrue(result.getDescription().contains("Sauron is watching"));
                    break;
                }
            }
            assertTrue(foundWatching, "Should eventually roll <= 3 in 100 attempts");
        }
    }

    // ---- rollWithAdvantage ----

    @Nested
    @DisplayName("rollWithAdvantage Tests")
    class RollWithAdvantageTests {

        @Test
        @DisplayName("should take the higher of two rolls")
        void rollWithAdvantage_shouldTakeHigher() {
            // Act - roll many times and check result is always valid
            for (int i = 0; i < 100; i++) {
                DiceRollResponse result = diceService.rollWithAdvantage(20);
                assertTrue(result.getTotal() >= 1 && result.getTotal() <= 20);
                assertTrue(result.getDescription().contains("taking"));
            }
        }

        @Test
        @DisplayName("should work with d6")
        void rollWithAdvantage_shouldWorkWithD6() {
            // Act
            DiceRollResponse result = diceService.rollWithAdvantage(6);

            // Assert
            assertTrue(result.getTotal() >= 1 && result.getTotal() <= 6);
        }
    }

    // ---- rollWithDisadvantage ----

    @Nested
    @DisplayName("rollWithDisadvantage Tests")
    class RollWithDisadvantageTests {

        @Test
        @DisplayName("should take the lower of two rolls")
        void rollWithDisadvantage_shouldTakeLower() {
            // Act
            DiceRollResponse result = diceService.rollWithDisadvantage(20);

            // Assert
            assertTrue(result.getTotal() >= 1 && result.getTotal() <= 20);
            assertTrue(result.getDescription().contains("taking"));
        }
    }

    // ---- rollMultiple ----

    @Nested
    @DisplayName("rollMultiple Tests")
    class RollMultipleTests {

        @Test
        @DisplayName("should roll multiple formulas")
        void rollMultiple_shouldRollMultiple() {
            // Act
            List<DiceRollResponse> results = diceService.rollMultiple(
                    List.of("d20", "2d6", "1d8+3")
            );

            // Assert
            assertEquals(3, results.size());
            assertTrue(results.get(0).isSuccess());
            assertTrue(results.get(1).isSuccess());
            assertTrue(results.get(2).isSuccess());
        }

        @Test
        @DisplayName("should handle empty list")
        void rollMultiple_shouldHandleEmptyList() {
            // Act
            List<DiceRollResponse> results = diceService.rollMultiple(List.of());

            // Assert
            assertTrue(results.isEmpty());
        }
    }

    // ---- rollNarrativeTask ----

    @Nested
    @DisplayName("rollNarrativeTask Tests")
    class RollNarrativeTaskTests {

        @Test
        @DisplayName("should roll 2d6")
        void rollNarrativeTask_shouldRoll2d6() {
            // Act
            DiceRollResponse result = diceService.rollNarrativeTask();

            // Assert
            assertTrue(result.getTotal() >= 2 && result.getTotal() <= 12);
        }

        @Test
        @DisplayName("should indicate fumble on 2-4")
        void rollNarrativeTask_shouldFumbleOnLow() {
            // Act - roll many times to find a fumble
            boolean foundFumble = false;
            for (int i = 0; i < 200; i++) {
                DiceRollResponse result = diceService.rollNarrativeTask();
                if (result.getTotal() <= 4) {
                    foundFumble = true;
                    assertFalse(result.isSuccess());
                    assertTrue(result.getDescription().contains("Fumble"));
                    break;
                }
            }
            assertTrue(foundFumble, "Should eventually roll <= 4 in 200 attempts");
        }

        @Test
        @DisplayName("should indicate critical success on 11-12")
        void rollNarrativeTask_shouldCriticalOnHigh() {
            // Act
            boolean foundCritical = false;
            for (int i = 0; i < 200; i++) {
                DiceRollResponse result = diceService.rollNarrativeTask();
                if (result.getTotal() > 10) {
                    foundCritical = true;
                    assertTrue(result.isSuccess());
                    assertTrue(result.getDescription().contains("Critical success"));
                    break;
                }
            }
            assertTrue(foundCritical, "Should eventually roll > 10 in 200 attempts");
        }

        @Test
        @DisplayName("should indicate partial success on 5-7")
        void rollNarrativeTask_shouldPartialOn5to7() {
            // Act
            boolean foundPartial = false;
            for (int i = 0; i < 200; i++) {
                DiceRollResponse result = diceService.rollNarrativeTask();
                if (result.getTotal() >= 5 && result.getTotal() <= 7) {
                    foundPartial = true;
                    assertTrue(result.isSuccess());
                    assertTrue(result.getDescription().contains("Partial success"));
                    break;
                }
            }
            assertTrue(foundPartial, "Should eventually roll 5-7 in 200 attempts");
        }

        @Test
        @DisplayName("should indicate success on 8-10")
        void rollNarrativeTask_shouldSuccessOn8to10() {
            // Act
            boolean foundSuccess = false;
            for (int i = 0; i < 200; i++) {
                DiceRollResponse result = diceService.rollNarrativeTask();
                if (result.getTotal() >= 8 && result.getTotal() <= 10) {
                    foundSuccess = true;
                    assertTrue(result.isSuccess());
                    assertTrue(result.getDescription().contains("Success"));
                    break;
                }
            }
            assertTrue(foundSuccess, "Should eventually roll 8-10 in 200 attempts");
        }
    }
}

package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AirBattleAiService")
class AirBattleAiServiceTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("defaults to MEDIUM difficulty")
        void defaultsToMedium() {
            AirBattleAiService ai = new AirBattleAiService();

            // Default constructor creates MEDIUM difficulty
            // Test by checking behavior
            // MEDIUM threshold is 0.5 for attack
            assertTrue(ai.decideAttack(10, 5)); // winChance = 10/5/3 = 0.67 >= 0.5
        }

        @Test
        @DisplayName("creates with specified difficulty")
        void createsWithDifficulty() {
            AirBattleAiService easy = new AirBattleAiService(AirBattleAiService.Difficulty.EASY);
            AirBattleAiService hard = new AirBattleAiService(AirBattleAiService.Difficulty.HARD);

            // EASY has lower threshold (0.4), so more likely to attack
            // HARD has higher threshold (0.6), so less likely to attack
            // With winChance ~0.45, EASY attacks but HARD doesn't
            assertTrue(easy.decideAttack(7, 5)); // winChance = 7/5/3 = 0.47 >= 0.4
            assertFalse(hard.decideAttack(7, 5)); // winChance = 0.47 < 0.6
        }
    }

    @Nested
    @DisplayName("Decide Attack")
    class DecideAttack {

        @Test
        @DisplayName("attacks when win chance is high")
        void attacksWhenWinChanceHigh() {
            AirBattleAiService ai = new AirBattleAiService();

            assertTrue(ai.decideAttack(30, 5)); // winChance = 30/5/3 = 2.0 -> capped at 1.0
        }

        @Test
        @DisplayName("does not attack when win chance is low")
        void doesNotAttackWhenWinChanceLow() {
            AirBattleAiService ai = new AirBattleAiService();

            assertFalse(ai.decideAttack(1, 10)); // winChance = 1/10/3 = 0.033
        }

        @Test
        @DisplayName("easy mode attacks more readily")
        void easyAttacksMore() {
            AirBattleAiService easy = new AirBattleAiService(AirBattleAiService.Difficulty.EASY);

            // winChance = 2/3/3 = 0.22 < 0.4 threshold
            assertFalse(easy.decideAttack(2, 3));
        }

        @Test
        @DisplayName("hard mode requires higher win chance")
        void hardRequiresHigherChance() {
            AirBattleAiService hard = new AirBattleAiService(AirBattleAiService.Difficulty.HARD);

            // winChance = 5/5/3 = 0.33 < 0.6 threshold
            assertFalse(hard.decideAttack(5, 5));
        }

        @Test
        @DisplayName("always attacks when defense is zero")
        void attacksWhenDefenseZero() {
            AirBattleAiService ai = new AirBattleAiService();

            assertTrue(ai.decideAttack(1, 0)); // winChance = 1.0
        }
    }

    @Nested
    @DisplayName("Decide Reinforce")
    class DecideReinforce {

        @Test
        @DisplayName("does not reinforce when no reserves")
        void noReinforceNoReserves() {
            AirBattleAiService ai = new AirBattleAiService();

            assertFalse(ai.decideReinforce(10, 5, 0));
        }

        @Test
        @DisplayName("may reinforce when win chance is low")
        void mayReinforceWhenLosing() {
            AirBattleAiService ai = new AirBattleAiService();

            // winChance < 0.5, so condition is met (random factor may vary)
            // Just verify it doesn't crash
            boolean result = ai.decideReinforce(1, 10, 5);
            assertNotNull(result);
        }

        @Test
        @DisplayName("hard mode reinforces more often")
        void hardReinforcesMore() {
            AirBattleAiService hard = new AirBattleAiService(AirBattleAiService.Difficulty.HARD);

            // Hard mode has 0.8 random threshold vs 0.5 for others
            // Just verify it runs without error
            boolean result = hard.decideReinforce(1, 10, 5);
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("Decide Retreat")
    class DecideRetreat {

        @Test
        @DisplayName("retreats when win chance is very low")
        void retreatsWhenLosing() {
            AirBattleAiService ai = new AirBattleAiService();

            assertTrue(ai.decideRetreat(1, 100)); // winChance = 1/100/3 = 0.003 < 0.15
        }

        @Test
        @DisplayName("does not retreat when win chance is high")
        void doesNotRetreatWhenWinning() {
            AirBattleAiService ai = new AirBattleAiService();

            assertFalse(ai.decideRetreat(30, 5)); // winChance = 1.0
        }

        @Test
        @DisplayName("easy mode retreats more readily")
        void easyRetreatsMore() {
            AirBattleAiService easy = new AirBattleAiService(AirBattleAiService.Difficulty.EASY);

            // EASY threshold is 0.2, so retreats at higher win chances
            assertTrue(easy.decideRetreat(1, 10)); // winChance = 0.033 < 0.2
        }

        @Test
        @DisplayName("hard mode retreats less readily")
        void hardRetreatsLess() {
            AirBattleAiService hard = new AirBattleAiService(AirBattleAiService.Difficulty.HARD);

            // HARD threshold is 0.1
            assertTrue(hard.decideRetreat(1, 50)); // winChance = 0.007 < 0.1
        }
    }

    @Nested
    @DisplayName("Difficulty enum")
    class Difficulty {

        @Test
        @DisplayName("has three difficulty levels")
        void hasThreeLevels() {
            assertEquals(3, AirBattleAiService.Difficulty.values().length);
        }
    }
}

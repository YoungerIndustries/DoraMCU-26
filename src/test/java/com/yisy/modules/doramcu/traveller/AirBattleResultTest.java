package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AirBattleResult")
class AirBattleResultTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates result with battle id, description, and outcome")
        void createsWithFields() {
            AirBattleResult result = new AirBattleResult("battle-1", "Victory", AirBattleResult.Outcome.ATTACKER_VICTORY);

            assertEquals("battle-1", result.getBattleId());
            assertEquals("Victory", result.getDescription());
            assertEquals(AirBattleResult.Outcome.ATTACKER_VICTORY, result.getOutcome());
        }

        @Test
        @DisplayName("creates result with losses and events")
        void createsWithLosses() {
            AirBattleEvent event = new AirBattleEvent(1, "Round 1");
            AirBattleResult result = new AirBattleResult("battle-1", "Victory", AirBattleResult.Outcome.ATTACKER_VICTORY,
                    2, 1, List.of(event));

            assertEquals(2, result.getAttackerLosses());
            assertEquals(1, result.getDefenderLosses());
            assertEquals(1, result.getEvents().size());
        }

        @Test
        @DisplayName("default constructor creates empty result")
        void defaultConstructor() {
            AirBattleResult result = new AirBattleResult();

            assertNull(result.getBattleId());
            assertNull(result.getDescription());
            assertNull(result.getOutcome());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("sets odds and dice roll")
        void setsOddsAndDice() {
            AirBattleResult result = new AirBattleResult("battle-1", "Victory", AirBattleResult.Outcome.ATTACKER_VICTORY);
            result.setOdds(3);
            result.setDiceRoll(8);

            assertEquals(3, result.getOdds());
            assertEquals(8, result.getDiceRoll());
        }
    }

    @Nested
    @DisplayName("Outcome enum")
    class Outcome {

        @Test
        @DisplayName("has five outcome types")
        void hasFiveOutcomes() {
            assertEquals(5, AirBattleResult.Outcome.values().length);
        }

        @Test
        @DisplayName("includes all expected outcomes")
        void includesExpectedOutcomes() {
            AirBattleResult.Outcome[] values = AirBattleResult.Outcome.values();

            assertNotNull(java.util.Arrays.stream(values).filter(v -> v == AirBattleResult.Outcome.ATTACKER_VICTORY).findFirst().orElse(null));
            assertNotNull(java.util.Arrays.stream(values).filter(v -> v == AirBattleResult.Outcome.DEFENDER_VICTORY).findFirst().orElse(null));
            assertNotNull(java.util.Arrays.stream(values).filter(v -> v == AirBattleResult.Outcome.PYRRHIC_VICTORY).findFirst().orElse(null));
            assertNotNull(java.util.Arrays.stream(values).filter(v -> v == AirBattleResult.Outcome.DRAW).findFirst().orElse(null));
            assertNotNull(java.util.Arrays.stream(values).filter(v -> v == AirBattleResult.Outcome.RETREAT).findFirst().orElse(null));
        }
    }
}

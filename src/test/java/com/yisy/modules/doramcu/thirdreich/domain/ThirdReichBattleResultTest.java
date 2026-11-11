package com.yisy.modules.doramcu.thirdreich.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ThirdReichBattleResult")
class ThirdReichBattleResultTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates result with battle ID and outcome")
        void createsWithIdAndOutcome() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);

            assertEquals("b-1", result.getBattleId());
            assertEquals(ThirdReichBattleResult.Outcome.ATTACKER_VICTORY, result.getOutcome());
            assertEquals(0, result.getAttackerCasualties());
            assertEquals(0, result.getDefenderCasualties());
            assertEquals(0, result.getRoundsFought());
            assertNull(result.getNarrativeSummary());
            assertTrue(result.getEvents().isEmpty());
        }

        @Test
        @DisplayName("throws on null battle ID")
        void throwsOnNullId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ThirdReichBattleResult(null, ThirdReichBattleResult.Outcome.DRAW));
        }

        @Test
        @DisplayName("throws on blank battle ID")
        void throwsOnBlankId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ThirdReichBattleResult("  ", ThirdReichBattleResult.Outcome.DRAW));
        }

        @Test
        @DisplayName("throws on null outcome")
        void throwsOnNullOutcome() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ThirdReichBattleResult("b-1", null));
        }
    }

    @Nested
    @DisplayName("Casualties")
    class Casualties {

        @Test
        @DisplayName("set and get casualties")
        void setAndGetCasualties() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            result.setAttackerCasualties(50);
            result.setDefenderCasualties(100);
            result.setAttackerUnitsLost(2);
            result.setDefenderUnitsLost(5);
            result.setRoundsFought(3);

            assertEquals(50, result.getAttackerCasualties());
            assertEquals(100, result.getDefenderCasualties());
            assertEquals(2, result.getAttackerUnitsLost());
            assertEquals(5, result.getDefenderUnitsLost());
            assertEquals(3, result.getRoundsFought());
        }
    }

    @Nested
    @DisplayName("Events")
    class Events {

        @Test
        @DisplayName("addEvent adds to event list")
        void addEvent() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.DRAW);
            result.addEvent("Battle started");
            result.addEvent("Round 1 combat");

            assertEquals(2, result.getEvents().size());
            assertEquals("Battle started", result.getEvents().get(0));
        }
    }

    @Nested
    @DisplayName("Narrative Generation")
    class NarrativeGeneration {

        @Test
        @DisplayName("generateNarrative creates ATTACKER_VICTORY text")
        void attackerVictoryNarrative() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            result.setRoundsFought(3);
            result.setAttackerCasualties(20);
            result.setAttackerUnitsLost(1);
            result.setDefenderCasualties(80);
            result.setDefenderUnitsLost(3);
            result.generateNarrative();

            assertNotNull(result.getNarrativeSummary());
            assertTrue(result.getNarrativeSummary().contains("decisive victory"));
            assertTrue(result.getNarrativeSummary().contains("3 rounds"));
        }

        @Test
        @DisplayName("generateNarrative creates DEFENDER_VICTORY text")
        void defenderVictoryNarrative() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.DEFENDER_VICTORY);
            result.setRoundsFought(2);
            result.generateNarrative();

            assertTrue(result.getNarrativeSummary().contains("repelled the attack"));
        }

        @Test
        @DisplayName("generateNarrative creates PYRRHIC_VICTORY text")
        void pyrrhicVictoryNarrative() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.PYRRHIC_VICTORY);
            result.setRoundsFought(5);
            result.generateNarrative();

            assertTrue(result.getNarrativeSummary().contains("terrible cost"));
        }

        @Test
        @DisplayName("generateNarrative creates DEFENDER_HOLDS text")
        void defenderHoldsNarrative() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.DEFENDER_HOLDS);
            result.setRoundsFought(4);
            result.generateNarrative();

            assertTrue(result.getNarrativeSummary().contains("held the line"));
        }

        @Test
        @DisplayName("generateNarrative creates DRAW text")
        void drawNarrative() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.DRAW);
            result.setRoundsFought(3);
            result.generateNarrative();

            assertTrue(result.getNarrativeSummary().contains("both sides withdrew"));
        }

        @Test
        @DisplayName("generateNarrative creates RETREAT text")
        void retreatNarrative() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.RETREAT);
            result.setRoundsFought(1);
            result.generateNarrative();

            assertTrue(result.getNarrativeSummary().contains("retreat"));
        }

        @Test
        @DisplayName("narrative includes casualty counts")
        void narrativeIncludesCasualties() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            result.setRoundsFought(2);
            result.setAttackerCasualties(30);
            result.setAttackerUnitsLost(1);
            result.setDefenderCasualties(60);
            result.setDefenderUnitsLost(2);
            result.generateNarrative();

            assertTrue(result.getNarrativeSummary().contains("30"));
            assertTrue(result.getNarrativeSummary().contains("60"));
        }
    }

    @Nested
    @DisplayName("Outcomes")
    class Outcomes {

        @Test
        @DisplayName("all outcomes are accessible")
        void allOutcomes() {
            assertEquals(6, ThirdReichBattleResult.Outcome.values().length);
            assertNotNull(ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            assertNotNull(ThirdReichBattleResult.Outcome.DEFENDER_VICTORY);
            assertNotNull(ThirdReichBattleResult.Outcome.PYRRHIC_VICTORY);
            assertNotNull(ThirdReichBattleResult.Outcome.DEFENDER_HOLDS);
            assertNotNull(ThirdReichBattleResult.Outcome.DRAW);
            assertNotNull(ThirdReichBattleResult.Outcome.RETREAT);
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToString {

        @Test
        @DisplayName("toString contains result info")
        void containsInfo() {
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            result.setAttackerCasualties(50);
            result.setDefenderCasualties(100);
            String str = result.toString();

            assertTrue(str.contains("b-1"));
            assertTrue(str.contains("ATTACKER_VICTORY"));
        }
    }
}

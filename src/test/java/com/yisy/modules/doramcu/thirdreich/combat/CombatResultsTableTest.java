package com.yisy.modules.doramcu.thirdreich.combat;

import com.yisy.modules.doramcu.thirdreich.combat.CombatResultsTable.CombatResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CombatResultsTable")
class CombatResultsTableTest {

    private final CombatResultsTable crt = new CombatResultsTable();

    @Nested
    @DisplayName("Odds Calculation")
    class OddsCalculation {

        @Test
        @DisplayName("equal attack and defense yields 1.0 odds")
        void equalOdds() {
            assertEquals(1.0, CombatResultsTable.calculateOdds(10, 10), 0.01);
        }

        @Test
        @DisplayName("double attack yields 2.0 odds")
        void doubleAttack() {
            assertEquals(2.0, CombatResultsTable.calculateOdds(20, 10), 0.01);
        }

        @Test
        @DisplayName("half attack yields 0.5 odds")
        void halfAttack() {
            assertEquals(0.5, CombatResultsTable.calculateOdds(5, 10), 0.01);
        }

        @Test
        @DisplayName("zero defense caps at max odds")
        void zeroDefense() {
            assertEquals(6.0, CombatResultsTable.calculateOdds(10, 0), 0.01);
        }

        @Test
        @DisplayName("zero attack yields 0 odds")
        void zeroAttack() {
            assertEquals(0.0, CombatResultsTable.calculateOdds(0, 10), 0.01);
        }
    }

    @Nested
    @DisplayName("Odds Index")
    class OddsIndex {

        @Test
        @DisplayName("0.25 maps to index 0")
        void veryLowOdds() {
            assertEquals(0, CombatResultsTable.getOddsIndex(0.25));
        }

        @Test
        @DisplayName("0.5 maps to index 1")
        void lowOdds() {
            assertEquals(1, CombatResultsTable.getOddsIndex(0.5));
        }

        @Test
        @DisplayName("1.0 maps to index 2")
        void evenOdds() {
            assertEquals(2, CombatResultsTable.getOddsIndex(1.0));
        }

        @Test
        @DisplayName("2.0 maps to index 3")
        void goodOdds() {
            assertEquals(3, CombatResultsTable.getOddsIndex(2.0));
        }

        @Test
        @DisplayName("3.0 maps to index 4")
        void betterOdds() {
            assertEquals(4, CombatResultsTable.getOddsIndex(3.0));
        }

        @Test
        @DisplayName("4.0 maps to index 5")
        void greatOdds() {
            assertEquals(5, CombatResultsTable.getOddsIndex(4.0));
        }

        @Test
        @DisplayName("6.0 maps to index 6")
        void maxOdds() {
            assertEquals(6, CombatResultsTable.getOddsIndex(6.0));
        }
    }

    @Nested
    @DisplayName("CRT Lookup")
    class CrtLookup {

        @Test
        @DisplayName("overwhelming odds with good roll yields attacker victory")
        void overwhelmingOdds() {
            // Odds 6.0 (index 6), roll 12 (index 10)
            CombatResult result = crt.lookup(6.0, 12);
            assertEquals(CombatResult.DEFENDER_ROUT, result);
        }

        @Test
        @DisplayName("hopeless odds yields attacker rout")
        void hopelessOdds() {
            // Odds 0.25 (index 0), any roll
            CombatResult result = crt.lookup(0.25, 7);
            assertEquals(CombatResult.ATTACKER_ROUT, result);
        }

        @Test
        @DisplayName("even odds with middle roll yields even")
        void evenOdds() {
            // Odds 1.0 (index 2), roll 7 (index 5)
            CombatResult result = crt.lookup(1.0, 7);
            assertEquals(CombatResult.EVEN, result);
        }

        @Test
        @DisplayName("good odds with high roll yields attacker victory")
        void goodOddsHighRoll() {
            // Odds 3.0 (index 4), roll 12 (index 10)
            CombatResult result = crt.lookup(3.0, 12);
            assertEquals(CombatResult.ATTACKER_VICTORY, result);
        }

        @Test
        @DisplayName("dice roll clamped to valid range")
        void diceClamped() {
            // Roll 1 should map to index 0
            assertNotNull(crt.lookup(1.0, 1));
            // Roll 20 should map to index 10
            assertNotNull(crt.lookup(1.0, 20));
        }
    }

    @Nested
    @DisplayName("Casualty Percentages")
    class CasualtyPercentages {

        @Test
        @DisplayName("attacker victory has low attacker casualties")
        void attackerVictoryCasualties() {
            assertEquals(5, CombatResultsTable.getAttackerCasualtyPercent(CombatResult.ATTACKER_VICTORY));
        }

        @Test
        @DisplayName("attacker victory has high defender casualties")
        void attackerVictoryDefCasualties() {
            assertEquals(25, CombatResultsTable.getDefenderCasualtyPercent(CombatResult.ATTACKER_VICTORY));
        }

        @Test
        @DisplayName("attacker rout has high attacker casualties")
        void attackerRoutCasualties() {
            assertEquals(40, CombatResultsTable.getAttackerCasualtyPercent(CombatResult.ATTACKER_ROUT));
        }

        @Test
        @DisplayName("defender rout has high defender casualties")
        void defenderRoutCasualties() {
            assertEquals(35, CombatResultsTable.getDefenderCasualtyPercent(CombatResult.DEFENDER_ROUT));
        }

        @Test
        @DisplayName("even result has equal casualties")
        void evenCasualties() {
            assertEquals(15, CombatResultsTable.getAttackerCasualtyPercent(CombatResult.EVEN));
            assertEquals(15, CombatResultsTable.getDefenderCasualtyPercent(CombatResult.EVEN));
        }
    }

    @Nested
    @DisplayName("Morale Penalties")
    class MoralePenalties {

        @Test
        @DisplayName("attacker victory boosts attacker morale")
        void attackerVictoryMorale() {
            assertEquals(-5, CombatResultsTable.getAttackerMoralePenalty(CombatResult.ATTACKER_VICTORY));
        }

        @Test
        @DisplayName("attacker rout severely penalizes attacker morale")
        void attackerRoutMorale() {
            assertEquals(20, CombatResultsTable.getAttackerMoralePenalty(CombatResult.ATTACKER_ROUT));
        }

        @Test
        @DisplayName("defender rout severely penalizes defender morale")
        void defenderRoutMorale() {
            assertEquals(20, CombatResultsTable.getDefenderMoralePenalty(CombatResult.DEFENDER_ROUT));
        }

        @Test
        @DisplayName("defender victory boosts defender morale")
        void defenderVictoryMorale() {
            assertEquals(-5, CombatResultsTable.getDefenderMoralePenalty(CombatResult.DEFENDER_VICTORY));
        }
    }

    @Nested
    @DisplayName("CombatResult Enum")
    class CombatResultEnum {

        @Test
        @DisplayName("all results have display names")
        void allHaveDisplayNames() {
            for (CombatResult result : CombatResult.values()) {
                assertNotNull(result.getDisplayName());
                assertFalse(result.getDisplayName().isBlank());
            }
        }

        @Test
        @DisplayName("toString returns display name")
        void toStringReturnsDisplayName() {
            assertEquals("Attacker Victory", CombatResult.ATTACKER_VICTORY.toString());
        }

        @Test
        @DisplayName("enum has 7 results")
        void hasSevenResults() {
            assertEquals(7, CombatResult.values().length);
        }
    }
}

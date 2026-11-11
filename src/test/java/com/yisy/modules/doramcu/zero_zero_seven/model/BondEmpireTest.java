package com.yisy.modules.doramcu.zero_zero_seven.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BondEmpire Tests")
class BondEmpireTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            BondEmpire empire = new BondEmpire();
            assertNull(empire.getId());
            assertNull(empire.getName());
            assertEquals(0, empire.getIntelligenceBudget());
            assertEquals(5, empire.getMilitaryStrength());
            assertEquals(5, empire.getEconomicPower());
            assertEquals(5, empire.getDiplomaticInfluence());
            assertEquals(0, empire.getSpyNetworkCount());
            assertEquals(0, empire.getAgentCount());
            assertEquals(0, empire.getResearchPoints());
            assertEquals(0, empire.getEconomyOutput());
            assertTrue(empire.getReputationMap().isEmpty());
            assertTrue(empire.getControlledTerritories().isEmpty());
        }
    }

    @Nested
    @DisplayName("Reputation")
    class Reputation {

        @Test
        @DisplayName("Get reputation returns 0 for unknown empire")
        void unknownEmpire() {
            BondEmpire empire = new BondEmpire();
            assertEquals(0, empire.getReputation("Unknown"));
        }

        @Test
        @DisplayName("Modify reputation adds delta")
        void modifyPositive() {
            BondEmpire empire = new BondEmpire();
            empire.modifyReputation("Allies", 30);
            assertEquals(30, empire.getReputation("Allies"));
        }

        @Test
        @DisplayName("Modify reputation clamps at 100")
        void clampMax() {
            BondEmpire empire = new BondEmpire();
            empire.modifyReputation("Allies", 60);
            empire.modifyReputation("Allies", 50);
            assertEquals(100, empire.getReputation("Allies"));
        }

        @Test
        @DisplayName("Modify reputation clamps at -100")
        void clampMin() {
            BondEmpire empire = new BondEmpire();
            empire.modifyReputation("Enemies", -60);
            empire.modifyReputation("Enemies", -50);
            assertEquals(-100, empire.getReputation("Enemies"));
        }
    }

    @Nested
    @DisplayName("War and alliance checks")
    class WarAndAlliance {

        @Test
        @DisplayName("At war when reputation below -50")
        void atWar() {
            BondEmpire empire = new BondEmpire();
            empire.modifyReputation("Adversary", -51);
            assertTrue(empire.isAtWarWith("Adversary"));
        }

        @Test
        @DisplayName("Not at war when reputation is -50")
        void notAtWarAtThreshold() {
            BondEmpire empire = new BondEmpire();
            empire.modifyReputation("Rival", -50);
            assertFalse(empire.isAtWarWith("Rival"));
        }

        @Test
        @DisplayName("Allied when reputation above 75")
        void allied() {
            BondEmpire empire = new BondEmpire();
            empire.modifyReputation("Ally", 76);
            assertTrue(empire.isAlliedWith("Ally"));
        }

        @Test
        @DisplayName("Not allied when reputation is 75")
        void notAlliedAtThreshold() {
            BondEmpire empire = new BondEmpire();
            empire.modifyReputation("Partner", 75);
            assertFalse(empire.isAlliedWith("Partner"));
        }
    }

    @Nested
    @DisplayName("Power score")
    class PowerScore {

        @Test
        @DisplayName("Calculates weighted power score")
        void weightedScore() {
            BondEmpire empire = new BondEmpire();
            empire.setMilitaryStrength(10);
            empire.setEconomicPower(8);
            empire.setDiplomaticInfluence(6);
            double expected = 10 * 0.4 + 8 * 0.35 + 6 * 0.25;
            assertEquals(expected, empire.getPowerScore(), 0.001);
        }

        @Test
        @DisplayName("Default power score")
        void defaultScore() {
            BondEmpire empire = new BondEmpire();
            double expected = 5 * 0.4 + 5 * 0.35 + 5 * 0.25;
            assertEquals(expected, empire.getPowerScore(), 0.001);
        }
    }

    @Nested
    @DisplayName("Enums")
    class Enums {

        @Test
        @DisplayName("ColdWarAlignment values")
        void alignment() {
            assertSame(BondEmpire.ColdWarAlignment.WESTERN, BondEmpire.ColdWarAlignment.valueOf("WESTERN"));
            assertSame(BondEmpire.ColdWarAlignment.SOVIET, BondEmpire.ColdWarAlignment.valueOf("SOVIET"));
            assertSame(BondEmpire.ColdWarAlignment.NON_ALIGNED, BondEmpire.ColdWarAlignment.valueOf("NON_ALIGNED"));
            assertSame(BondEmpire.ColdWarAlignment.AXIS, BondEmpire.ColdWarAlignment.valueOf("AXIS"));
        }
    }
}

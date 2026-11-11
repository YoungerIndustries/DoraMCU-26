package com.yisy.modules.doramcu.fourx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxFaction Tests")
class FourxFactionTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            FourxFaction faction = new FourxFaction();
            assertNull(faction.getId());
            assertNull(faction.getName());
            assertNull(faction.getGameModule());
            assertNull(faction.getCombatStyle());
            assertEquals(FourxFaction.DiplomaticStance.NEUTRAL, faction.getDefaultStance());
            assertTrue(faction.getReputationMap().isEmpty());
            assertEquals(0, faction.getFleetStrength());
            assertEquals(0, faction.getColonyCount());
            assertEquals(0, faction.getResearchPoints());
            assertEquals(0, faction.getEconomyOutput());
        }

        @Test
        @DisplayName("Parameterized constructor sets fields")
        void parameterized() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            assertEquals("f1", faction.getId());
            assertEquals("Federation", faction.getName());
            assertEquals("StarTrek4X", faction.getGameModule());
        }
    }

    @Nested
    @DisplayName("Reputation")
    class Reputation {

        @Test
        @DisplayName("Get reputation returns 0 for unknown faction")
        void unknownFaction() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            assertEquals(0, faction.getReputation("Unknown"));
        }

        @Test
        @DisplayName("Modify reputation adds delta")
        void modifyPositive() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.modifyReputation("Romulans", 30);
            assertEquals(30, faction.getReputation("Romulans"));
        }

        @Test
        @DisplayName("Modify reputation clamps at 100")
        void clampMax() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.modifyReputation("Allies", 60);
            faction.modifyReputation("Allies", 50);
            assertEquals(100, faction.getReputation("Allies"));
        }

        @Test
        @DisplayName("Modify reputation clamps at -100")
        void clampMin() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.modifyReputation("Enemies", -60);
            faction.modifyReputation("Enemies", -50);
            assertEquals(-100, faction.getReputation("Enemies"));
        }
    }

    @Nested
    @DisplayName("War and alliance checks")
    class WarAndAlliance {

        @Test
        @DisplayName("At war when reputation below -50")
        void atWar() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.modifyReputation("Klingons", -51);
            assertTrue(faction.isAtWarWith("Klingons"));
        }

        @Test
        @DisplayName("Not at war when reputation is -50")
        void notAtWarAtThreshold() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.modifyReputation("Klingons", -50);
            assertFalse(faction.isAtWarWith("Klingons"));
        }

        @Test
        @DisplayName("Allied when reputation above 75")
        void allied() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.modifyReputation("Vulcans", 76);
            assertTrue(faction.isAlliedWith("Vulcans"));
        }

        @Test
        @DisplayName("Not allied when reputation is 75")
        void notAlliedAtThreshold() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.modifyReputation("Vulcans", 75);
            assertFalse(faction.isAlliedWith("Vulcans"));
        }
    }

    @Nested
    @DisplayName("Setters and getters")
    class SettersAndGetters {

        @Test
        @DisplayName("Combat style")
        void combatStyle() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.setCombatStyle("OFFENSIVE");
            assertEquals("OFFENSIVE", faction.getCombatStyle());
        }

        @Test
        @DisplayName("Diplomatic stance")
        void diplomaticStance() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.setDefaultStance(FourxFaction.DiplomaticStance.ALLIED);
            assertEquals(FourxFaction.DiplomaticStance.ALLIED, faction.getDefaultStance());
        }

        @Test
        @DisplayName("Resources")
        void resources() {
            FourxFaction faction = new FourxFaction("f1", "Federation", "StarTrek4X");
            faction.setFleetStrength(100);
            faction.setColonyCount(5);
            faction.setResearchPoints(200);
            faction.setEconomyOutput(50);
            assertEquals(100, faction.getFleetStrength());
            assertEquals(5, faction.getColonyCount());
            assertEquals(200, faction.getResearchPoints());
            assertEquals(50, faction.getEconomyOutput());
        }
    }
}

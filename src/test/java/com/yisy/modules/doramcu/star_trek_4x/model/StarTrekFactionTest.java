package com.yisy.modules.doramcu.star_trek_4x.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StarTrekFaction Tests")
class StarTrekFactionTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            StarTrekFaction faction = new StarTrekFaction();
            assertNull(faction.getId());
            assertNull(faction.getName());
            assertNull(faction.getDescription());
            assertNull(faction.getSfbEmpireName());
            assertNull(faction.getGalaxy());
            assertNull(faction.getCombatStyle());
            assertEquals(StarTrekFaction.DiplomaticStance.NEUTRAL, faction.getDefaultStance());
            assertTrue(faction.getReputationMap().isEmpty());
            assertTrue(faction.getControlledSectors().isEmpty());
            assertEquals(0, faction.getFleetStrength());
            assertEquals(0, faction.getColonyCount());
            assertEquals(0, faction.getResearchPoints());
            assertEquals(0, faction.getEconomyOutput());
            assertNull(faction.getGameSession());
        }

        @Test
        @DisplayName("Parameterized constructor sets name, sfbEmpireName, gameSession")
        void parameterized() {
            GameSession session = new GameSession();
            StarTrekFaction faction = new StarTrekFaction("Federation", "FEDERATION", session);
            assertEquals("Federation", faction.getName());
            assertEquals("FEDERATION", faction.getSfbEmpireName());
            assertSame(session, faction.getGameSession());
        }
    }

    @Nested
    @DisplayName("Reputation")
    class Reputation {

        @Test
        @DisplayName("Get reputation returns 0 for unknown faction")
        void unknownFaction() {
            StarTrekFaction faction = new StarTrekFaction();
            assertEquals(0, faction.getReputation("Unknown"));
        }

        @Test
        @DisplayName("Modify reputation adds delta")
        void modifyPositive() {
            StarTrekFaction faction = new StarTrekFaction();
            faction.modifyReputation("Klingon", 30);
            assertEquals(30, faction.getReputation("Klingon"));
        }

        @Test
        @DisplayName("Modify reputation clamps at 100")
        void clampMax() {
            StarTrekFaction faction = new StarTrekFaction();
            faction.modifyReputation("Romulan", 60);
            faction.modifyReputation("Romulan", 50);
            assertEquals(100, faction.getReputation("Romulan"));
        }

        @Test
        @DisplayName("Modify reputation clamps at -100")
        void clampMin() {
            StarTrekFaction faction = new StarTrekFaction();
            faction.modifyReputation("Borg", -60);
            faction.modifyReputation("Borg", -50);
            assertEquals(-100, faction.getReputation("Borg"));
        }
    }

    @Nested
    @DisplayName("War and alliance checks")
    class WarAndAlliance {

        @Test
        @DisplayName("At war when reputation below -50")
        void atWar() {
            StarTrekFaction faction = new StarTrekFaction();
            faction.modifyReputation("Borg", -51);
            assertTrue(faction.isAtWarWith("Borg"));
        }

        @Test
        @DisplayName("Not at war when reputation is -50")
        void notAtWarAtThreshold() {
            StarTrekFaction faction = new StarTrekFaction();
            faction.modifyReputation("Klingon", -50);
            assertFalse(faction.isAtWarWith("Klingon"));
        }

        @Test
        @DisplayName("Allied when reputation above 75")
        void allied() {
            StarTrekFaction faction = new StarTrekFaction();
            faction.modifyReputation("Romulan", 76);
            assertTrue(faction.isAlliedWith("Romulan"));
        }

        @Test
        @DisplayName("Not allied when reputation is 75")
        void notAlliedAtThreshold() {
            StarTrekFaction faction = new StarTrekFaction();
            faction.modifyReputation("Ferengi", 75);
            assertFalse(faction.isAlliedWith("Ferengi"));
        }
    }

    @Nested
    @DisplayName("Sector management")
    class SectorManagement {

        @Test
        @DisplayName("Add controlled sector")
        void addSector() {
            StarTrekFaction faction = new StarTrekFaction();
            faction.getControlledSectors().add(1);
            faction.getControlledSectors().add(2);
            assertEquals(2, faction.getControlledSectors().size());
            assertTrue(faction.getControlledSectors().contains(1));
            assertTrue(faction.getControlledSectors().contains(2));
        }
    }

    @Nested
    @DisplayName("Enums")
    class Enums {

        @Test
        @DisplayName("DiplomaticStance values")
        void diplomaticStance() {
            assertSame(StarTrekFaction.DiplomaticStance.HOSTILE, StarTrekFaction.DiplomaticStance.valueOf("HOSTILE"));
            assertSame(StarTrekFaction.DiplomaticStance.NEUTRAL, StarTrekFaction.DiplomaticStance.valueOf("NEUTRAL"));
            assertSame(StarTrekFaction.DiplomaticStance.FRIENDLY, StarTrekFaction.DiplomaticStance.valueOf("FRIENDLY"));
            assertSame(StarTrekFaction.DiplomaticStance.ALLIED, StarTrekFaction.DiplomaticStance.valueOf("ALLIED"));
        }
    }
}

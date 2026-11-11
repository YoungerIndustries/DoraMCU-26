package com.yisy.modules.doramcu.thirdreich.commander;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AICommander")
class AICommanderTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates commander with default values")
        void createsWithDefaults() {
            AICommander commander = new AICommander("cmd-1", "John Doe");

            assertEquals("cmd-1", commander.getId());
            assertEquals("John Doe", commander.getName());
            assertEquals(5, commander.getLeadership());
            assertEquals(AICommander.TacticalPreference.BALANCED, commander.getTacticalPreference());
            assertFalse(commander.isHistoricalProfile());
            assertTrue(commander.getTraits().isEmpty());
            assertNull(commander.getSourceGame());
        }

        @Test
        @DisplayName("throws on null ID")
        void throwsOnNullId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new AICommander(null, "Test"));
        }

        @Test
        @DisplayName("throws on blank ID")
        void throwsOnBlankId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new AICommander("  ", "Test"));
        }

        @Test
        @DisplayName("throws on null name")
        void throwsOnNullName() {
            assertThrows(IllegalArgumentException.class, () ->
                    new AICommander("cmd-1", null));
        }
    }

    @Nested
    @DisplayName("Leadership")
    class Leadership {

        @Test
        @DisplayName("clamps leadership to 1-10 range")
        void clampsLeadership() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.setLeadership(0);
            assertEquals(1, commander.getLeadership());
            commander.setLeadership(15);
            assertEquals(10, commander.getLeadership());
        }
    }

    @Nested
    @DisplayName("Traits")
    class Traits {

        @Test
        @DisplayName("addTrait adds unique traits")
        void addTrait() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.addTrait(CommanderTrait.AGGRESSIVE);
            commander.addTrait(CommanderTrait.FLANKER);

            assertEquals(2, commander.getTraits().size());
            assertTrue(commander.getTraits().contains(CommanderTrait.AGGRESSIVE));
        }

        @Test
        @DisplayName("addTrait ignores duplicates")
        void ignoresDuplicates() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.addTrait(CommanderTrait.AGGRESSIVE);
            commander.addTrait(CommanderTrait.AGGRESSIVE);

            assertEquals(1, commander.getTraits().size());
        }

        @Test
        @DisplayName("addTrait ignores null")
        void ignoresNull() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.addTrait(null);
            assertTrue(commander.getTraits().isEmpty());
        }
    }

    @Nested
    @DisplayName("Combat Modifiers")
    class CombatModifiers {

        @Test
        @DisplayName("aggressive preference adds attack bonus")
        void aggressiveAttackBonus() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.setTacticalPreference(AICommander.TacticalPreference.AGGRESSIVE);
            commander.addTrait(CommanderTrait.AGGRESSIVE);
            // AGGRESSIVE trait = +1 atk, AGGRESSIVE preference = +1 atk
            assertEquals(2, commander.getTotalAttackBonus());
        }

        @Test
        @DisplayName("defensive preference adds defense bonus")
        void defensiveDefenseBonus() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.setTacticalPreference(AICommander.TacticalPreference.DEFENSIVE);
            commander.addTrait(CommanderTrait.DEFENSIVE);
            // DEFENSIVE trait = +1 def, DEFENSIVE preference = +1 def
            assertEquals(2, commander.getTotalDefenseBonus());
        }

        @Test
        @DisplayName("balanced preference adds no bonus")
        void balancedNoBonus() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.setTacticalPreference(AICommander.TacticalPreference.BALANCED);
            assertEquals(0, commander.getTotalAttackBonus());
            assertEquals(0, commander.getTotalDefenseBonus());
        }

        @Test
        @DisplayName("leadership modifier for high leadership")
        void highLeadershipModifier() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.setLeadership(10);
            assertEquals(2, commander.getLeadershipModifier());
        }

        @Test
        @DisplayName("leadership modifier for medium leadership")
        void mediumLeadershipModifier() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.setLeadership(7);
            assertEquals(1, commander.getLeadershipModifier());
        }

        @Test
        @DisplayName("leadership modifier for average leadership")
        void averageLeadershipModifier() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.setLeadership(5);
            assertEquals(0, commander.getLeadershipModifier());
        }

        @Test
        @DisplayName("leadership modifier for low leadership")
        void lowLeadershipModifier() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.setLeadership(3);
            assertEquals(-1, commander.getLeadershipModifier());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("setSourceGame and setHistoricalProfile work")
        void settersWork() {
            AICommander commander = new AICommander("cmd-1", "Test");
            commander.setSourceGame("Star Trek");
            commander.setHistoricalProfile(true);

            assertEquals("Star Trek", commander.getSourceGame());
            assertTrue(commander.isHistoricalProfile());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToString {

        @Test
        @DisplayName("toString contains commander info")
        void containsInfo() {
            AICommander commander = new AICommander("cmd-1", "Rommel");
            commander.setLeadership(9);
            String str = commander.toString();

            assertTrue(str.contains("cmd-1"));
            assertTrue(str.contains("Rommel"));
            assertTrue(str.contains("9"));
        }
    }
}

package com.yisy.modules.doramcu.thirdreich.commander;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AICommanderService")
class AICommanderServiceTest {

    private final AICommanderService service = new AICommanderService();

    @Nested
    @DisplayName("createFromProfile")
    class CreateFromProfile {

        @Test
        @DisplayName("creates and stores commander from profile")
        void createsAndStores() {
            AICommander commander = service.createFromProfile(AICommanderProfile.ROMMEL);

            assertNotNull(commander);
            assertEquals("rommel", commander.getId());
            assertEquals("Erwin Rommel", commander.getName());
            assertEquals(9, commander.getLeadership());
            assertTrue(commander.isHistoricalProfile());

            // Verify stored
            assertEquals(commander, service.getCommander("rommel"));
        }

        @Test
        @DisplayName("throws on null profile")
        void throwsOnNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    service.createFromProfile(null));
        }
    }

    @Nested
    @DisplayName("generateFromCharacter")
    class GenerateFromCharacter {

        @Test
        @DisplayName("generates commander from stats map")
        void generatesFromStats() {
            Map<String, Object> stats = Map.of(
                    "name", "James T. Kirk",
                    "leadership", 8,
                    "aggression", 7,
                    "tactics", 6,
                    "morale", 5
            );
            AICommander commander = service.generateFromCharacter(stats, "Star Trek");

            assertNotNull(commander);
            assertEquals("James T. Kirk", commander.getName());
            assertEquals("Star Trek", commander.getSourceGame());
            assertEquals(8, commander.getLeadership());
            assertEquals(AICommander.TacticalPreference.AGGRESSIVE, commander.getTacticalPreference());
            assertTrue(commander.getTraits().contains(CommanderTrait.AGGRESSIVE));
        }

        @Test
        @DisplayName("defaults leadership to 5")
        void defaultsLeadership() {
            Map<String, Object> stats = Map.of("name", "Unknown");
            AICommander commander = service.generateFromCharacter(stats, "Test");

            assertEquals(5, commander.getLeadership());
        }

        @Test
        @DisplayName("high aggression yields aggressive preference")
        void highAggression() {
            Map<String, Object> stats = Map.of("name", "Patton", "aggression", 9);
            AICommander commander = service.generateFromCharacter(stats, "Test");

            assertEquals(AICommander.TacticalPreference.AGGRESSIVE, commander.getTacticalPreference());
        }

        @Test
        @DisplayName("low aggression yields defensive preference")
        void lowAggression() {
            Map<String, Object> stats = Map.of("name", "Cautious", "aggression", 2);
            AICommander commander = service.generateFromCharacter(stats, "Test");

            assertEquals(AICommander.TacticalPreference.DEFENSIVE, commander.getTacticalPreference());
        }

        @Test
        @DisplayName("medium aggression yields balanced preference")
        void mediumAggression() {
            Map<String, Object> stats = Map.of("name", "Balanced", "aggression", 5);
            AICommander commander = service.generateFromCharacter(stats, "Test");

            assertEquals(AICommander.TacticalPreference.BALANCED, commander.getTacticalPreference());
        }

        @Test
        @DisplayName("high morale adds inspirational trait")
        void highMorale() {
            Map<String, Object> stats = Map.of("name", "Inspiring", "morale", 9);
            AICommander commander = service.generateFromCharacter(stats, "Test");

            assertTrue(commander.getTraits().contains(CommanderTrait.INSPIRATIONAL));
        }

        @Test
        @DisplayName("high tactics adds flanker trait")
        void highTactics() {
            Map<String, Object> stats = Map.of("name", "Tactical", "tactics", 8);
            AICommander commander = service.generateFromCharacter(stats, "Test");

            assertTrue(commander.getTraits().contains(CommanderTrait.FLANKER));
        }

        @Test
        @DisplayName("high leadership adds logistician trait")
        void highLeadership() {
            Map<String, Object> stats = Map.of("name", "Leader", "leadership", 9);
            AICommander commander = service.generateFromCharacter(stats, "Test");

            assertTrue(commander.getTraits().contains(CommanderTrait.LOGISTICIAN));
        }

        @Test
        @DisplayName("assigns random trait if no stats qualify")
        void randomTraitIfNone() {
            Map<String, Object> stats = Map.of("name", "Average", "leadership", 5, "aggression", 5);
            AICommander commander = service.generateFromCharacter(stats, "Test");

            assertFalse(commander.getTraits().isEmpty());
        }

        @Test
        @DisplayName("throws on null stats")
        void throwsOnNull() {
            assertThrows(IllegalArgumentException.class, () ->
                    service.generateFromCharacter(null, "Test"));
        }

        @Test
        @DisplayName("throws on empty stats")
        void throwsOnEmpty() {
            assertThrows(IllegalArgumentException.class, () ->
                    service.generateFromCharacter(Map.of(), "Test"));
        }
    }

    @Nested
    @DisplayName("createRandomCommander")
    class CreateRandomCommander {

        @Test
        @DisplayName("creates commander with random values")
        void createsRandom() {
            AICommander commander = service.createRandomCommander("Random General");

            assertEquals("Random General", commander.getName());
            assertTrue(commander.getLeadership() >= 3 && commander.getLeadership() <= 10);
            assertFalse(commander.getTraits().isEmpty());
            assertTrue(commander.getTraits().size() <= 3);
        }
    }

    @Nested
    @DisplayName("getCommander")
    class GetCommander {

        @Test
        @DisplayName("returns null for unknown ID")
        void returnsNullForUnknown() {
            assertNull(service.getCommander("nonexistent"));
        }

        @Test
        @DisplayName("returns stored commander")
        void returnsStored() {
            AICommander commander = service.createFromProfile(AICommanderProfile.PATTON);
            assertEquals(commander, service.getCommander("patton"));
        }
    }

    @Nested
    @DisplayName("getAllCommanders")
    class GetAllCommanders {

        @Test
        @DisplayName("returns all registered commanders")
        void returnsAll() {
            service.createFromProfile(AICommanderProfile.ROMMEL);
            service.createFromProfile(AICommanderProfile.PATTON);

            assertEquals(2, service.getAllCommanders().size());
        }
    }

    @Nested
    @DisplayName("getAllProfiles")
    class GetAllProfiles {

        @Test
        @DisplayName("returns all historical profiles")
        void returnsAll() {
            assertEquals(10, service.getAllProfiles().size());
        }
    }
}

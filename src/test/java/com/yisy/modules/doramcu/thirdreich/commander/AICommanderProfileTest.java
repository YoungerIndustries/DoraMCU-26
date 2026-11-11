package com.yisy.modules.doramcu.thirdreich.commander;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AICommanderProfile")
class AICommanderProfileTest {

    @Test
    @DisplayName("all profiles have valid data")
    void allProfilesValid() {
        for (AICommanderProfile profile : AICommanderProfile.values()) {
            assertNotNull(profile.getId());
            assertFalse(profile.getId().isBlank());
            assertNotNull(profile.getName());
            assertFalse(profile.getName().isBlank());
            assertTrue(profile.getLeadership() >= 1 && profile.getLeadership() <= 10);
            assertNotNull(profile.getTacticalPreference());
            assertNotNull(profile.getTraits());
            assertFalse(profile.getTraits().isEmpty());
            assertNotNull(profile.getDescription());
        }
    }

    @Test
    @DisplayName("getAllProfiles returns all profiles")
    void getAllProfiles() {
        assertEquals(AICommanderProfile.values().length, AICommanderProfile.getAllProfiles().size());
    }

    @Nested
    @DisplayName("Specific Profiles")
    class SpecificProfiles {

        @Test
        @DisplayName("Rommel has correct stats")
        void rommelStats() {
            assertEquals("rommel", AICommanderProfile.ROMMEL.getId());
            assertEquals("Erwin Rommel", AICommanderProfile.ROMMEL.getName());
            assertEquals(9, AICommanderProfile.ROMMEL.getLeadership());
            assertEquals(AICommander.TacticalPreference.AGGRESSIVE, AICommanderProfile.ROMMEL.getTacticalPreference());
            assertTrue(AICommanderProfile.ROMMEL.getTraits().contains(CommanderTrait.ARMOR_SPECIALIST));
        }

        @Test
        @DisplayName("Patton has correct stats")
        void pattonStats() {
            assertEquals("patton", AICommanderProfile.PATTON.getId());
            assertEquals(8, AICommanderProfile.PATTON.getLeadership());
            assertEquals(AICommander.TacticalPreference.AGGRESSIVE, AICommanderProfile.PATTON.getTacticalPreference());
        }

        @Test
        @DisplayName("Montgomery is defensive")
        void montgomeryDefensive() {
            assertEquals(AICommander.TacticalPreference.DEFENSIVE, AICommanderProfile.MONTGOMERY.getTacticalPreference());
            assertTrue(AICommanderProfile.MONTGOMERY.getTraits().contains(CommanderTrait.CAUTIOUS));
        }

        @Test
        @DisplayName("Zhukov has high leadership")
        void zhukovHighLeadership() {
            assertEquals(9, AICommanderProfile.ZHUKOV.getLeadership());
        }
    }

    @Nested
    @DisplayName("createCommander")
    class CreateCommander {

        @Test
        @DisplayName("creates commander from profile")
        void createsFromProfile() {
            AICommander commander = AICommanderProfile.ROMMEL.createCommander();

            assertEquals("rommel", commander.getId());
            assertEquals("Erwin Rommel", commander.getName());
            assertEquals(9, commander.getLeadership());
            assertEquals(AICommander.TacticalPreference.AGGRESSIVE, commander.getTacticalPreference());
            assertTrue(commander.isHistoricalProfile());
            assertEquals(3, commander.getTraits().size());
        }

        @Test
        @DisplayName("creates commander with all traits")
        void createsWithTraits() {
            AICommander commander = AICommanderProfile.PATTON.createCommander();

            assertTrue(commander.getTraits().contains(CommanderTrait.ARMOR_SPECIALIST));
            assertTrue(commander.getTraits().contains(CommanderTrait.AGGRESSIVE));
            assertTrue(commander.getTraits().contains(CommanderTrait.INSPIRATIONAL));
        }
    }

    @Test
    @DisplayName("enum has 10 profiles")
    void hasTenProfiles() {
        assertEquals(10, AICommanderProfile.values().length);
    }
}

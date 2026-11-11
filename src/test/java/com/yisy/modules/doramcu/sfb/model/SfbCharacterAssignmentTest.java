package com.yisy.modules.doramcu.sfb.model;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SfbCharacterAssignmentTest {

    private CaptainLogCharacter character;
    private SfbCharacterAssignment assignment;

    @BeforeEach
    void setUp() {
        character = new CaptainLogCharacter();
        character.setId(1L);
        character.setName("Test Character");
        character.setInjuryState(CaptainLogCharacter.InjuryState.HEALTHY);
        assignment = new SfbCharacterAssignment(1L, "battle-1", SfbRole.ADMIRAL);
        assignment.setCharacter(character);
    }

    // ------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor initializes defaults")
        void noArgConstructor() {
            SfbCharacterAssignment a = new SfbCharacterAssignment();
            assertNull(a.getCharacterId());
            assertNull(a.getCharacterName());
            assertNull(a.getBattleId());
            assertNull(a.getRole());
            assertNull(a.getShipId());
            assertNull(a.getStation());
            assertEquals(0, a.getSide());
            assertFalse(a.isActive());
            assertNull(a.getCharacter());
            assertNull(a.getModifiers());
            assertNull(a.getAiCharacterId());
        }

        @Test
        @DisplayName("parameterized constructor sets characterId, battleId, role")
        void parameterizedConstructor() {
            SfbCharacterAssignment a = new SfbCharacterAssignment(42L, "battle-x", SfbRole.CAPTAIN);
            assertEquals(42L, a.getCharacterId());
            assertEquals("battle-x", a.getBattleId());
            assertEquals(SfbRole.CAPTAIN, a.getRole());
            assertNull(a.getAiCharacterId());
        }
    }

    // ------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("characterId getter and setter")
        void characterId() {
            assignment.setCharacterId(99L);
            assertEquals(99L, assignment.getCharacterId());
        }

        @Test
        @DisplayName("characterName getter and setter")
        void characterName() {
            assignment.setCharacterName("Admiral Smith");
            assertEquals("Admiral Smith", assignment.getCharacterName());
        }

        @Test
        @DisplayName("battleId getter and setter")
        void battleId() {
            assignment.setBattleId("battle-99");
            assertEquals("battle-99", assignment.getBattleId());
        }

        @Test
        @DisplayName("role getter and setter")
        void role() {
            assignment.setRole(SfbRole.CREW);
            assertEquals(SfbRole.CREW, assignment.getRole());
        }

        @Test
        @DisplayName("shipId getter and setter")
        void shipId() {
            assignment.setShipId("ship-1");
            assertEquals("ship-1", assignment.getShipId());
        }

        @Test
        @DisplayName("station getter and setter")
        void station() {
            assignment.setStation(SfbCrewStationType.HELM);
            assertEquals(SfbCrewStationType.HELM, assignment.getStation());
        }

        @Test
        @DisplayName("side getter and setter")
        void side() {
            assignment.setSide(1);
            assertEquals(1, assignment.getSide());
        }

        @Test
        @DisplayName("active getter and setter")
        void active() {
            assignment.setActive(true);
            assertTrue(assignment.isActive());
            assignment.setActive(false);
            assertFalse(assignment.isActive());
        }

        @Test
        @DisplayName("modifiers getter and setter")
        void modifiers() {
            Map<String, Integer> mods = new HashMap<>();
            mods.put("FIT", 2);
            mods.put("TEC", -1);
            assignment.setModifiers(mods);
            assertEquals(2, assignment.getModifiers().get("FIT"));
            assertEquals(-1, assignment.getModifiers().get("TEC"));
        }

        @Test
        @DisplayName("aiCharacterId getter and setter")
        void aiCharacterId() {
            assertNull(assignment.getAiCharacterId());
            assignment.setAiCharacterId(42L);
            assertEquals(42L, assignment.getAiCharacterId());
            assignment.setAiCharacterId(null);
            assertNull(assignment.getAiCharacterId());
        }
    }

    // ------------------------------------------------------------------
    // isValid() - ADMIRAL role
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("isValid() - ADMIRAL role")
    class IsValidAdmiralTests {

        @Test
        @DisplayName("valid when character is healthy and role is ADMIRAL")
        void validAdmiral() {
            assignment.setRole(SfbRole.ADMIRAL);
            assignment.setCharacter(character);
            assertTrue(assignment.isValid());
        }

        @Test
        @DisplayName("valid ADMIRAL does not require ship")
        void admiralNoShipRequired() {
            assignment.setRole(SfbRole.ADMIRAL);
            assignment.setShipId(null);
            assertTrue(assignment.isValid());
        }

        @Test
        @DisplayName("invalid when character is null")
        void invalidNullCharacter() {
            assignment.setCharacter(null);
            assertFalse(assignment.isValid());
        }

        @Test
        @DisplayName("invalid when character is DOWN")
        void invalidDownCharacter() {
            character.setInjuryState(CaptainLogCharacter.InjuryState.DOWN);
            assertFalse(assignment.isValid());
        }
    }

    // ------------------------------------------------------------------
    // isValid() - CAPTAIN role
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("isValid() - CAPTAIN role")
    class IsValidCaptainTests {

        @Test
        @DisplayName("valid when character is healthy, role is CAPTAIN, and ship is set")
        void validCaptain() {
            assignment.setRole(SfbRole.CAPTAIN);
            assignment.setShipId("ship-1");
            assertTrue(assignment.isValid());
        }

        @Test
        @DisplayName("invalid when ship is null")
        void invalidNullShip() {
            assignment.setRole(SfbRole.CAPTAIN);
            assignment.setShipId(null);
            assertFalse(assignment.isValid());
        }

        @Test
        @DisplayName("invalid when ship is empty")
        void invalidEmptyShip() {
            assignment.setRole(SfbRole.CAPTAIN);
            assignment.setShipId("");
            assertFalse(assignment.isValid());
        }

        @Test
        @DisplayName("station not required for CAPTAIN")
        void stationNotRequired() {
            assignment.setRole(SfbRole.CAPTAIN);
            assignment.setShipId("ship-1");
            assignment.setStation(null);
            assertTrue(assignment.isValid());
        }
    }

    // ------------------------------------------------------------------
    // isValid() - CREW role
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("isValid() - CREW role")
    class IsValidCrewTests {

        @Test
        @DisplayName("valid when character is healthy, role is CREW, ship and station are set")
        void validCrew() {
            assignment.setRole(SfbRole.CREW);
            assignment.setShipId("ship-1");
            assignment.setStation(SfbCrewStationType.HELM);
            assertTrue(assignment.isValid());
        }

        @Test
        @DisplayName("invalid when ship is null")
        void invalidNullShip() {
            assignment.setRole(SfbRole.CREW);
            assignment.setShipId(null);
            assignment.setStation(SfbCrewStationType.HELM);
            assertFalse(assignment.isValid());
        }

        @Test
        @DisplayName("invalid when station is null")
        void invalidNullStation() {
            assignment.setRole(SfbRole.CREW);
            assignment.setShipId("ship-1");
            assignment.setStation(null);
            assertFalse(assignment.isValid());
        }

        @Test
        @DisplayName("invalid when ship is empty")
        void invalidEmptyShip() {
            assignment.setRole(SfbRole.CREW);
            assignment.setShipId("");
            assignment.setStation(SfbCrewStationType.HELM);
            assertFalse(assignment.isValid());
        }

        @Test
        @DisplayName("valid with all station types")
        void validAllStationTypes() {
            assignment.setRole(SfbRole.CREW);
            assignment.setShipId("ship-1");

            for (SfbCrewStationType station : SfbCrewStationType.values()) {
                assignment.setStation(station);
                assertTrue(assignment.isValid(), "Should be valid for station: " + station);
            }
        }
    }

    // ------------------------------------------------------------------
    // isValid() - Injury states
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("isValid() - Injury states")
    class IsValidInjuryStateTests {

        @Test
        @DisplayName("valid when character is HEALTHY")
        void validHealthy() {
            character.setInjuryState(CaptainLogCharacter.InjuryState.HEALTHY);
            assignment.setRole(SfbRole.ADMIRAL);
            assertTrue(assignment.isValid());
        }

        @Test
        @DisplayName("valid when character is STUNNED")
        void validStunned() {
            character.setInjuryState(CaptainLogCharacter.InjuryState.STUNNED);
            assignment.setRole(SfbRole.ADMIRAL);
            assertTrue(assignment.isValid());
        }

        @Test
        @DisplayName("valid when character is INJURED")
        void validInjured() {
            character.setInjuryState(CaptainLogCharacter.InjuryState.INJURED);
            assignment.setRole(SfbRole.ADMIRAL);
            assertTrue(assignment.isValid());
        }

        @Test
        @DisplayName("invalid when character is DOWN")
        void invalidDown() {
            character.setInjuryState(CaptainLogCharacter.InjuryState.DOWN);
            assignment.setRole(SfbRole.ADMIRAL);
            assertFalse(assignment.isValid());
        }
    }

    // ------------------------------------------------------------------
    // AI Character ID
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("AI Character ID")
    class AiCharacterIdTests {

        @Test
        @DisplayName("aiCharacterId is null for player-controlled characters")
        void nullForPlayerControlled() {
            assignment.setRole(SfbRole.ADMIRAL);
            assertNull(assignment.getAiCharacterId());
        }

        @Test
        @DisplayName("aiCharacterId is set for AI-controlled admirals")
        void setForAiControlled() {
            assignment.setRole(SfbRole.ADMIRAL);
            assignment.setAiCharacterId(5L);
            assertEquals(5L, assignment.getAiCharacterId());
            assertTrue(assignment.isValid());
        }

        @Test
        @DisplayName("aiCharacterId does not affect validation")
        void doesNotAffectValidation() {
            assignment.setRole(SfbRole.ADMIRAL);
            assignment.setAiCharacterId(10L);
            assertTrue(assignment.isValid());
            assignment.setAiCharacterId(null);
            assertTrue(assignment.isValid());
        }
    }

    // ------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString contains character ID, role, ship, and station")
        void toStringContainsFields() {
            assignment.setRole(SfbRole.CAPTAIN);
            assignment.setShipId("ship-1");
            assignment.setStation(SfbCrewStationType.TACTICAL);

            String s = assignment.toString();
            assertTrue(s.contains("character=1"));
            assertTrue(s.contains("role=CAPTAIN"));
            assertTrue(s.contains("ship=ship-1"));
            assertTrue(s.contains("station=TACTICAL"));
        }

        @Test
        @DisplayName("toString with null values")
        void toStringWithNulls() {
            assignment.setRole(SfbRole.ADMIRAL);
            assignment.setShipId(null);
            assignment.setStation(null);

            String s = assignment.toString();
            assertTrue(s.contains("character=1"));
            assertTrue(s.contains("role=ADMIRAL"));
            assertTrue(s.contains("ship=null"));
            assertTrue(s.contains("station=null"));
        }
    }
}

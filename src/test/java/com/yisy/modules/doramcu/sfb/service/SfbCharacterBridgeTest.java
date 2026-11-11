package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.sfb.model.SfbCharacterAssignment;
import com.yisy.modules.doramcu.sfb.model.SfbCrewStationType;
import com.yisy.modules.doramcu.sfb.model.SfbRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SfbCharacterBridgeTest {

    private SfbCharacterBridge bridge;
    private CaptainLogCharacter character;

    @BeforeEach
    void setUp() {
        bridge = new SfbCharacterBridge();
        character = new CaptainLogCharacter();
        character.setId(1L);
        character.setName("Test Captain");
        character.setRank(CaptainLogCharacter.Rank.CAPTAIN);
        character.setInjuryState(CaptainLogCharacter.InjuryState.HEALTHY);
        character.setConstitution(10);
        character.setFitness(8);
        character.setTechnical(6);
        character.setStrength(7);
        character.setReason(9);
        character.setInsight(5);
        character.setPresence(6);
        character.setCommand(10);
        character.setLeadership(8);
        character.setScience(7);
        character.setSecurity(6);
        character.setMedicine(4);
        character.setOperations(8);
    }

    // ------------------------------------------------------------------
    // Character Registration
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Character Registration")
    class RegistrationTests {

        @Test
        @DisplayName("register character with existing ID")
        void registerWithId() {
            Long id = bridge.registerCharacter(character);
            assertEquals(1L, id);
            assertNotNull(bridge.loadCharacter(1L));
        }

        @Test
        @DisplayName("register character without ID assigns new ID")
        void registerWithoutId() {
            CaptainLogCharacter noId = new CaptainLogCharacter();
            noId.setName("No ID Character");
            noId.setRank(CaptainLogCharacter.Rank.ENSIGN);
            noId.setConstitution(10);

            Long id = bridge.registerCharacter(noId);
            assertNotNull(id);
            assertEquals(noId, bridge.loadCharacter(id));
        }

        @Test
        @DisplayName("loadCharacter returns null for unknown ID")
        void loadUnknownCharacter() {
            assertNull(bridge.loadCharacter(999L));
        }

        @Test
        @DisplayName("getAllCharacters returns registered characters")
        void getAllCharacters() {
            bridge.registerCharacter(character);
            List<CaptainLogCharacter> all = bridge.getAllCharacters();
            assertEquals(1, all.size());
            assertEquals("Test Captain", all.get(0).getName());
        }
    }

    // ------------------------------------------------------------------
    // Character Availability
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Character Availability")
    class AvailabilityTests {

        @Test
        @DisplayName("available when healthy with valid rank")
        void availableHealthy() {
            bridge.registerCharacter(character);
            assertTrue(bridge.isCharacterAvailable(character));
        }

        @Test
        @DisplayName("not available when constitution <= 0")
        void notAvailableIncapacitated() {
            character.setConstitution(0);
            assertFalse(bridge.isCharacterAvailable(character));
        }

        @Test
        @DisplayName("not available when rank is null")
        void notAvailableNoRank() {
            character.setRank(null);
            assertFalse(bridge.isCharacterAvailable(character));
        }

        @Test
        @DisplayName("not available when character is null")
        void notAvailableNull() {
            assertFalse(bridge.isCharacterAvailable(null));
        }

        @Test
        @DisplayName("not available when constitution is negative")
        void notAvailableNegativeConstitution() {
            character.setConstitution(-5);
            assertFalse(bridge.isCharacterAvailable(character));
        }
    }

    // ------------------------------------------------------------------
    // Assign Character to Battle - ADMIRAL
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Assign Character - ADMIRAL")
    class AssignAdmiralTests {

        @Test
        @DisplayName("assign ADMIRAL without ship")
        void assignAdmiral() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.ADMIRAL, null, null);

            assertNotNull(assignment);
            assertEquals(SfbRole.ADMIRAL, assignment.getRole());
            assertEquals("battle-1", assignment.getBattleId());
            assertTrue(assignment.isActive());
        }

        @Test
        @DisplayName("ADMIRAL assignment has fleet bonuses")
        void admiralFleetBonuses() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.ADMIRAL, null, null);

            Map<String, Integer> modifiers = assignment.getModifiers();
            assertTrue(modifiers.containsKey("FLEET_ENERGY_BONUS"));
            assertTrue(modifiers.containsKey("FLEET_ACCURACY_BONUS"));
            assertTrue(modifiers.containsKey("FLEET_SHIELD_BONUS"));
            assertEquals(5, modifiers.get("FLEET_ENERGY_BONUS")); // command/2 = 10/2
            assertEquals(4, modifiers.get("FLEET_ACCURACY_BONUS")); // leadership/2 = 8/2
        }
    }

    // ------------------------------------------------------------------
    // Assign Character to Battle - CAPTAIN
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Assign Character - CAPTAIN")
    class AssignCaptainTests {

        @Test
        @DisplayName("assign CAPTAIN with ship")
        void assignCaptain() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.CAPTAIN, "ship-1", null);

            assertNotNull(assignment);
            assertEquals(SfbRole.CAPTAIN, assignment.getRole());
            assertEquals("ship-1", assignment.getShipId());
        }

        @Test
        @DisplayName("CAPTAIN assignment has ship bonuses")
        void captainShipBonuses() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.CAPTAIN, "ship-1", null);

            Map<String, Integer> modifiers = assignment.getModifiers();
            assertTrue(modifiers.containsKey("SHIELD_EFFICIENCY"));
            assertTrue(modifiers.containsKey("SENSOR_RANGE"));
            assertTrue(modifiers.containsKey("WEAPON_DAMAGE"));
            assertTrue(modifiers.containsKey("EVASION"));
            assertTrue(modifiers.containsKey("DAMCON"));
            assertTrue(modifiers.containsKey("CREW_MORALE"));
        }

        @Test
        @DisplayName("CAPTAIN requires ship ID")
        void captainRequiresShip() {
            bridge.registerCharacter(character);
            assertThrows(IllegalArgumentException.class, () ->
                    bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.CAPTAIN, null, null));
        }

        @Test
        @DisplayName("CAPTAIN requires non-empty ship ID")
        void captainRequiresNonEmptyShip() {
            bridge.registerCharacter(character);
            assertThrows(IllegalArgumentException.class, () ->
                    bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.CAPTAIN, "", null));
        }
    }

    // ------------------------------------------------------------------
    // Assign Character to Battle - CREW
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Assign Character - CREW")
    class AssignCrewTests {

        @Test
        @DisplayName("assign CREW with ship and station")
        void assignCrew() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.CREW, "ship-1", SfbCrewStationType.HELM);

            assertNotNull(assignment);
            assertEquals(SfbRole.CREW, assignment.getRole());
            assertEquals("ship-1", assignment.getShipId());
            assertEquals(SfbCrewStationType.HELM, assignment.getStation());
        }

        @Test
        @DisplayName("CREW requires ship ID")
        void crewRequiresShip() {
            bridge.registerCharacter(character);
            assertThrows(IllegalArgumentException.class, () ->
                    bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.CREW, null, SfbCrewStationType.HELM));
        }

        @Test
        @DisplayName("CREW requires station")
        void crewRequiresStation() {
            bridge.registerCharacter(character);
            assertThrows(IllegalArgumentException.class, () ->
                    bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.CREW, "ship-1", null));
        }

        @Test
        @DisplayName("CREW at HELM station has movement bonuses")
        void crewHelmBonuses() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.CREW, "ship-1", SfbCrewStationType.HELM);

            Map<String, Integer> modifiers = assignment.getModifiers();
            assertTrue(modifiers.containsKey("MOVEMENT_ACCURACY"));
            assertTrue(modifiers.containsKey("SPEED_BONUS"));
        }

        @Test
        @DisplayName("CREW at TACTICAL station has weapon bonuses")
        void crewTacticalBonuses() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.CREW, "ship-1", SfbCrewStationType.TACTICAL);

            Map<String, Integer> modifiers = assignment.getModifiers();
            assertTrue(modifiers.containsKey("HIT_PROBABILITY"));
            assertTrue(modifiers.containsKey("WEAPON_DAMAGE_BONUS"));
        }

        @Test
        @DisplayName("CREW at ENGINEERING station has damcon bonuses")
        void crewEngineeringBonuses() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.CREW, "ship-1", SfbCrewStationType.ENGINEERING);

            Map<String, Integer> modifiers = assignment.getModifiers();
            assertTrue(modifiers.containsKey("DAMCON_SPEED"));
            assertTrue(modifiers.containsKey("ENERGY_BONUS"));
        }

        @Test
        @DisplayName("CREW at SENSORS station has detection bonuses")
        void crewSensorsBonuses() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.CREW, "ship-1", SfbCrewStationType.SENSORS);

            Map<String, Integer> modifiers = assignment.getModifiers();
            assertTrue(modifiers.containsKey("DETECTION_RANGE"));
            assertTrue(modifiers.containsKey("CLOAK_DETECTION"));
        }

        @Test
        @DisplayName("CREW at MEDICAL station has recovery bonuses")
        void crewMedicalBonuses() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.CREW, "ship-1", SfbCrewStationType.MEDICAL);

            Map<String, Integer> modifiers = assignment.getModifiers();
            assertTrue(modifiers.containsKey("INJURY_RECOVERY"));
            assertTrue(modifiers.containsKey("DAMAGE_MITIGATION"));
        }
    }

    // ------------------------------------------------------------------
    // Assign Character - Error Cases
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Assign Character - Error Cases")
    class AssignErrorTests {

        @Test
        @DisplayName("throw when character not found")
        void characterNotFound() {
            assertThrows(IllegalArgumentException.class, () ->
                    bridge.assignCharacterToBattle("battle-1", 999L, SfbRole.ADMIRAL, null, null));
        }

        @Test
        @DisplayName("throw when character not available")
        void characterNotAvailable() {
            character.setConstitution(0);
            bridge.registerCharacter(character);
            assertThrows(IllegalStateException.class, () ->
                    bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.ADMIRAL, null, null));
        }
    }

    // ------------------------------------------------------------------
    // Battle Assignments
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Battle Assignments")
    class BattleAssignmentTests {

        @Test
        @DisplayName("getBattleAssignments returns empty for unknown battle")
        void unknownBattle() {
            List<SfbCharacterAssignment> assignments = bridge.getBattleAssignments("unknown");
            assertTrue(assignments.isEmpty());
        }

        @Test
        @DisplayName("getBattleAssignments returns all assignments")
        void getAllAssignments() {
            bridge.registerCharacter(character);
            bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.ADMIRAL, null, null);

            List<SfbCharacterAssignment> assignments = bridge.getBattleAssignments("battle-1");
            assertEquals(1, assignments.size());
        }

        @Test
        @DisplayName("getShipAssignments filters by ship")
        void getShipAssignments() {
            bridge.registerCharacter(character);
            bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.CAPTAIN, "ship-1", null);

            List<SfbCharacterAssignment> shipAssignments = bridge.getShipAssignments("battle-1", "ship-1");
            assertEquals(1, shipAssignments.size());

            List<SfbCharacterAssignment> otherShip = bridge.getShipAssignments("battle-1", "ship-2");
            assertTrue(otherShip.isEmpty());
        }

        @Test
        @DisplayName("getAdmiralAssignment returns admiral")
        void getAdmiralAssignment() {
            bridge.registerCharacter(character);
            SfbCharacterAssignment admiral = bridge.assignCharacterToBattle(
                    "battle-1", 1L, SfbRole.ADMIRAL, null, null);
            admiral.setSide(0);

            SfbCharacterAssignment found = bridge.getAdmiralAssignment("battle-1", 0);
            assertNotNull(found);
            assertEquals(SfbRole.ADMIRAL, found.getRole());
        }

        @Test
        @DisplayName("getAdmiralAssignment returns null when not found")
        void getAdmiralNotFound() {
            assertNull(bridge.getAdmiralAssignment("battle-1", 0));
        }

        @Test
        @DisplayName("getCaptainAssignment returns captain")
        void getCaptainAssignment() {
            bridge.registerCharacter(character);
            bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.CAPTAIN, "ship-1", null);

            SfbCharacterAssignment found = bridge.getCaptainAssignment("battle-1", "ship-1");
            assertNotNull(found);
            assertEquals(SfbRole.CAPTAIN, found.getRole());
        }

        @Test
        @DisplayName("getCrewAssignments returns crew")
        void getCrewAssignments() {
            bridge.registerCharacter(character);
            bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.CREW, "ship-1", SfbCrewStationType.HELM);

            List<SfbCharacterAssignment> crew = bridge.getCrewAssignments("battle-1", "ship-1");
            assertEquals(1, crew.size());
            assertEquals(SfbRole.CREW, crew.get(0).getRole());
        }
    }

    // ------------------------------------------------------------------
    // Remove Character from Battle
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Remove Character from Battle")
    class RemoveCharacterTests {

        @Test
        @DisplayName("remove character from battle")
        void removeCharacter() {
            bridge.registerCharacter(character);
            bridge.assignCharacterToBattle("battle-1", 1L, SfbRole.ADMIRAL, null, null);
            assertEquals(1, bridge.getBattleAssignments("battle-1").size());

            bridge.removeCharacterFromBattle("battle-1", 1L);
            assertTrue(bridge.getBattleAssignments("battle-1").isEmpty());
        }

        @Test
        @DisplayName("remove from unknown battle does nothing")
        void removeFromUnknownBattle() {
            bridge.removeCharacterFromBattle("unknown", 1L);
            // No exception thrown
        }
    }

    // ------------------------------------------------------------------
    // Calculate Character Modifiers
    // ------------------------------------------------------------------

    @Nested
    @DisplayName("Calculate Character Modifiers")
    class CalculateModifiersTests {

        @Test
        @DisplayName("modifiers include all attributes")
        void includesAllAttributes() {
            Map<String, Integer> modifiers = bridge.calculateCharacterModifiers(
                    character, SfbRole.ADMIRAL, null);

            assertEquals(8, modifiers.get("FIT"));
            assertEquals(6, modifiers.get("TEC"));
            assertEquals(7, modifiers.get("STR"));
            assertEquals(9, modifiers.get("REA"));
            assertEquals(5, modifiers.get("INS"));
            assertEquals(6, modifiers.get("PRE"));
        }

        @Test
        @DisplayName("modifiers include all disciplines")
        void includesAllDisciplines() {
            Map<String, Integer> modifiers = bridge.calculateCharacterModifiers(
                    character, SfbRole.ADMIRAL, null);

            assertEquals(10, modifiers.get("COMMAND"));
            assertEquals(8, modifiers.get("LEADERSHIP"));
            assertEquals(7, modifiers.get("SCIENCE"));
            assertEquals(6, modifiers.get("SECURITY"));
            assertEquals(4, modifiers.get("MEDICINE"));
            assertEquals(8, modifiers.get("OPERATIONS"));
        }
    }
}

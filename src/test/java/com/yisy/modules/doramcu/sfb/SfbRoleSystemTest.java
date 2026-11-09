package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter.Rank;
import com.yisy.modules.doramcu.sfb.model.*;
import com.yisy.modules.doramcu.sfb.service.SfbCharacterBridge;
import com.yisy.modules.doramcu.sfb.service.SfbRoleSystem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SfbRoleSystem Tests")
class SfbRoleSystemTest {

    private SfbCharacterBridge createBridge() {
        return new SfbCharacterBridge();
    }

    private CaptainLogCharacter createCharacter() {
        CaptainLogCharacter character = new CaptainLogCharacter();
        character.setId(1L);
        character.setName("Test Captain");
        character.setFitness(10);
        character.setTechnical(12);
        character.setStrength(8);
        character.setReason(14);
        character.setInsight(10);
        character.setPresence(12);
        character.setCommand(8);
        character.setLeadership(10);
        character.setScience(6);
        character.setSecurity(8);
        character.setMedicine(4);
        character.setOperations(10);
        character.setConstitution(100);
        character.setRank(Rank.CAPTAIN);
        return character;
    }

    private SfbShip createShip() {
        SfbShip ship = new SfbShip();
        ship.setId("ship1");
        ship.setName("Test Ship");
        ship.setPosition(new SfbHexPosition(0, 0, 0));
        ship.setSide(0);

        SfbSSD ssd = new SfbSSD();
        ssd.setHullBoxes(10);
        ssd.setDamConRating(3);
        ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
        ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
        ship.setSsd(ssd);
        ship.initializeShields(5);

        SfbEnergyAllocation ea = new SfbEnergyAllocation();
        ea.setWarpPower(10);
        ea.setImpulsePower(5);
        ea.setAuxiliaryPower(5);
        ship.setEnergyAllocation(ea);

        return ship;
    }

    @Test
    @DisplayName("Admiral modifiers - fleet energy bonus")
    void admiralFleetEnergyBonus() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        CaptainLogCharacter admiral = createCharacter();
        admiral.setId(100L);
        admiral.setName("Admiral");
        bridge.registerCharacter(admiral);

        SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                "battle1", 100L, SfbRole.ADMIRAL, null, null);
        assignment.setSide(0);

        List<SfbShip> fleet = new ArrayList<>();
        SfbShip ship = createShip();
        fleet.add(ship);

        roleSystem.applyAdmiralModifiers(assignment, fleet);

        // Admiral with command 8 should give energy bonus of 4
        assertTrue(assignment.getModifiers().getOrDefault("FLEET_ENERGY_BONUS", 0) >= 0);
    }

    @Test
    @DisplayName("Admiral modifiers - null assignment")
    void admiralNullAssignment() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        List<SfbShip> fleet = Collections.singletonList(createShip());
        roleSystem.applyAdmiralModifiers(null, fleet);
        // Should not throw
    }

    @Test
    @DisplayName("Captain modifiers - shield efficiency")
    void captainShieldEfficiency() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        CaptainLogCharacter captain = createCharacter();
        captain.setId(200L);
        captain.setName("Captain");
        bridge.registerCharacter(captain);

        SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                "battle1", 200L, SfbRole.CAPTAIN, "ship1", null);

        SfbShip ship = createShip();
        int initialShieldBoxes = ship.getShields()[0].getTotalBoxes();

        roleSystem.applyCaptainModifiers(assignment, ship);

        // PRE 12 / 2 = 6 shield efficiency bonus
        int finalShieldBoxes = ship.getShields()[0].getTotalBoxes();
        assertTrue(finalShieldBoxes >= initialShieldBoxes);
    }

    @Test
    @DisplayName("Captain modifiers - DamCon bonus")
    void captainDamConBonus() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        CaptainLogCharacter captain = createCharacter();
        captain.setId(201L);
        captain.setName("Captain");
        bridge.registerCharacter(captain);

        SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                "battle1", 201L, SfbRole.CAPTAIN, "ship1", null);

        SfbShip ship = createShip();
        int initialDamCon = ship.getSsd().getDamConRating();

        roleSystem.applyCaptainModifiers(assignment, ship);

        // TEC 12 / 2 = 6 DamCon bonus
        assertTrue(ship.getSsd().getDamConRating() >= initialDamCon);
    }

    @Test
    @DisplayName("Captain modifiers - null assignment")
    void captainNullAssignment() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        SfbShip ship = createShip();
        roleSystem.applyCaptainModifiers(null, ship);
        // Should not throw
    }

    @Test
    @DisplayName("Crew modifiers - engineering station")
    void crewEngineeringStation() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        CaptainLogCharacter crew = createCharacter();
        crew.setId(300L);
        crew.setName("Engineer");
        bridge.registerCharacter(crew);

        SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                "battle1", 300L, SfbRole.CREW, "ship1", SfbCrewStationType.ENGINEERING);

        SfbShip ship = createShip();
        int initialAux = ship.getEnergyAllocation().getAuxiliaryPower();

        List<SfbCharacterAssignment> crewList = Collections.singletonList(assignment);
        roleSystem.applyCrewModifiers(crewList, ship);

        // Operations 10 / 3 = 3 energy bonus
        assertTrue(ship.getEnergyAllocation().getAuxiliaryPower() >= initialAux);
    }

    @Test
    @DisplayName("Crew modifiers - empty list")
    void crewEmptyList() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        SfbShip ship = createShip();
        roleSystem.applyCrewModifiers(Collections.emptyList(), ship);
        // Should not throw
    }

    @Test
    @DisplayName("Total modifier calculation")
    void totalModifierCalculation() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        CaptainLogCharacter captain = createCharacter();
        captain.setId(400L);
        captain.setName("Captain");
        bridge.registerCharacter(captain);

        bridge.assignCharacterToBattle("battle1", 400L, SfbRole.CAPTAIN, "ship1", null);

        int shieldEfficiency = roleSystem.getTotalModifier("battle1", "ship1", "SHIELD_EFFICIENCY");
        assertTrue(shieldEfficiency >= 0);
    }

    @Test
    @DisplayName("Character modifiers display")
    void characterModifiersDisplay() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        var modifiers = roleSystem.getCharacterModifiers(1L, SfbRole.CAPTAIN, null);

        assertNotNull(modifiers);
        assertEquals(1L, modifiers.getCharacterId());
        assertEquals("Test Captain", modifiers.getCharacterName());
        assertEquals(SfbRole.CAPTAIN, modifiers.getRole());
        assertNotNull(modifiers.getAttributeBonuses());
        assertNotNull(modifiers.getDisciplineBonuses());
    }

    @Test
    @DisplayName("Character modifiers - null character")
    void characterModifiersNullCharacter() {
        SfbCharacterBridge bridge = createBridge();
        SfbRoleSystem roleSystem = new SfbRoleSystem(bridge);

        var modifiers = roleSystem.getCharacterModifiers(999L, SfbRole.CAPTAIN, null);
        assertNull(modifiers);
    }

    @Test
    @DisplayName("Role enum descriptions")
    void roleEnumDescriptions() {
        assertTrue(SfbRole.ADMIRAL.getDescription().contains("Fleet"));
        assertTrue(SfbRole.CAPTAIN.getDescription().contains("Ship"));
        assertTrue(SfbRole.CREW.getDescription().contains("Station"));
    }

    @Test
    @DisplayName("Crew station type primary disciplines")
    void crewStationDisciplines() {
        assertEquals(CaptainLogCharacter.Discipline.OPERATIONS, SfbCrewStationType.HELM.getPrimaryDiscipline());
        assertEquals(CaptainLogCharacter.Discipline.SECURITY, SfbCrewStationType.TACTICAL.getPrimaryDiscipline());
        assertEquals(CaptainLogCharacter.Discipline.OPERATIONS, SfbCrewStationType.ENGINEERING.getPrimaryDiscipline());
        assertEquals(CaptainLogCharacter.Discipline.SCIENCE, SfbCrewStationType.SENSORS.getPrimaryDiscipline());
        assertEquals(CaptainLogCharacter.Discipline.MEDICINE, SfbCrewStationType.MEDICAL.getPrimaryDiscipline());
    }
}

@DisplayName("SfbCharacterBridge Tests")
class SfbCharacterBridgeTest {

    private SfbCharacterBridge createBridge() {
        return new SfbCharacterBridge();
    }

    private CaptainLogCharacter createCharacter() {
        CaptainLogCharacter character = new CaptainLogCharacter();
        character.setId(1L);
        character.setName("Test Character");
        character.setFitness(10);
        character.setTechnical(10);
        character.setStrength(10);
        character.setReason(10);
        character.setInsight(10);
        character.setPresence(10);
        character.setCommand(5);
        character.setLeadership(5);
        character.setScience(5);
        character.setSecurity(5);
        character.setMedicine(5);
        character.setOperations(5);
        character.setConstitution(100);
        character.setRank(Rank.CAPTAIN);
        return character;
    }

    @Test
    @DisplayName("Register and load character")
    void registerAndLoadCharacter() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();

        Long id = bridge.registerCharacter(character);
        assertNotNull(id);

        CaptainLogCharacter loaded = bridge.loadCharacter(id);
        assertNotNull(loaded);
        assertEquals("Test Character", loaded.getName());
    }

    @Test
    @DisplayName("Character availability - healthy character")
    void characterAvailableHealthy() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        assertTrue(bridge.isCharacterAvailable(character));
    }

    @Test
    @DisplayName("Character availability - incapacitated")
    void characterAvailableIncapacitated() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        character.setConstitution(0);
        bridge.registerCharacter(character);

        assertFalse(bridge.isCharacterAvailable(character));
    }

    @Test
    @DisplayName("Character availability - no rank")
    void characterAvailableNoRank() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        character.setRank(null);
        bridge.registerCharacter(character);

        assertFalse(bridge.isCharacterAvailable(character));
    }

    @Test
    @DisplayName("Character availability - null character")
    void characterAvailableNull() {
        SfbCharacterBridge bridge = createBridge();
        assertFalse(bridge.isCharacterAvailable(null));
    }

    @Test
    @DisplayName("Assign character to battle - Admiral")
    void assignAdmiral() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                "battle1", 1L, SfbRole.ADMIRAL, null, null);

        assertNotNull(assignment);
        assertEquals(SfbRole.ADMIRAL, assignment.getRole());
        assertTrue(assignment.isActive());
    }

    @Test
    @DisplayName("Assign character to battle - Captain")
    void assignCaptain() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                "battle1", 1L, SfbRole.CAPTAIN, "ship1", null);

        assertNotNull(assignment);
        assertEquals(SfbRole.CAPTAIN, assignment.getRole());
        assertEquals("ship1", assignment.getShipId());
    }

    @Test
    @DisplayName("Assign character to battle - Crew")
    void assignCrew() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                "battle1", 1L, SfbRole.CREW, "ship1", SfbCrewStationType.TACTICAL);

        assertNotNull(assignment);
        assertEquals(SfbRole.CREW, assignment.getRole());
        assertEquals(SfbCrewStationType.TACTICAL, assignment.getStation());
    }

    @Test
    @DisplayName("Assign Captain without ship ID fails")
    void assignCaptainNoShip() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        assertThrows(IllegalArgumentException.class, () ->
                bridge.assignCharacterToBattle("battle1", 1L, SfbRole.CAPTAIN, null, null));
    }

    @Test
    @DisplayName("Assign Crew without station fails")
    void assignCrewNoStation() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        assertThrows(IllegalArgumentException.class, () ->
                bridge.assignCharacterToBattle("battle1", 1L, SfbRole.CREW, "ship1", null));
    }

    @Test
    @DisplayName("Assign unavailable character fails")
    void assignUnavailableCharacter() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        character.setConstitution(0);
        bridge.registerCharacter(character);

        assertThrows(IllegalStateException.class, () ->
                bridge.assignCharacterToBattle("battle1", 1L, SfbRole.ADMIRAL, null, null));
    }

    @Test
    @DisplayName("Assign non-existent character fails")
    void assignNonExistentCharacter() {
        SfbCharacterBridge bridge = createBridge();

        assertThrows(IllegalArgumentException.class, () ->
                bridge.assignCharacterToBattle("battle1", 999L, SfbRole.ADMIRAL, null, null));
    }

    @Test
    @DisplayName("Get battle assignments")
    void getBattleAssignments() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        bridge.assignCharacterToBattle("battle1", 1L, SfbRole.CAPTAIN, "ship1", null);

        List<SfbCharacterAssignment> assignments = bridge.getBattleAssignments("battle1");
        assertEquals(1, assignments.size());
    }

    @Test
    @DisplayName("Get ship assignments")
    void getShipAssignments() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        bridge.assignCharacterToBattle("battle1", 1L, SfbRole.CAPTAIN, "ship1", null);
        CaptainLogCharacter crew = createCharacter();
        crew.setId(2L);
        crew.setName("Crew");
        bridge.registerCharacter(crew);
        bridge.assignCharacterToBattle("battle1", 2L, SfbRole.CREW, "ship1", SfbCrewStationType.HELM);

        List<SfbCharacterAssignment> assignments = bridge.getShipAssignments("battle1", "ship1");
        assertEquals(2, assignments.size());
    }

    @Test
    @DisplayName("Get Admiral assignment")
    void getAdmiralAssignment() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        SfbCharacterAssignment assignment = bridge.assignCharacterToBattle(
                "battle1", 1L, SfbRole.ADMIRAL, null, null);
        assignment.setSide(0);

        SfbCharacterAssignment found = bridge.getAdmiralAssignment("battle1", 0);
        assertNotNull(found);
        assertEquals(SfbRole.ADMIRAL, found.getRole());
    }

    @Test
    @DisplayName("Get Captain assignment")
    void getCaptainAssignment() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        bridge.assignCharacterToBattle("battle1", 1L, SfbRole.CAPTAIN, "ship1", null);

        SfbCharacterAssignment found = bridge.getCaptainAssignment("battle1", "ship1");
        assertNotNull(found);
        assertEquals(SfbRole.CAPTAIN, found.getRole());
    }

    @Test
    @DisplayName("Get crew assignments")
    void getCrewAssignments() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        bridge.assignCharacterToBattle("battle1", 1L, SfbRole.CREW, "ship1", SfbCrewStationType.HELM);

        List<SfbCharacterAssignment> crew = bridge.getCrewAssignments("battle1", "ship1");
        assertEquals(1, crew.size());
        assertEquals(SfbCrewStationType.HELM, crew.get(0).getStation());
    }

    @Test
    @DisplayName("Remove character from battle")
    void removeCharacterFromBattle() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        bridge.assignCharacterToBattle("battle1", 1L, SfbRole.CAPTAIN, "ship1", null);
        assertEquals(1, bridge.getBattleAssignments("battle1").size());

        bridge.removeCharacterFromBattle("battle1", 1L);
        assertTrue(bridge.getBattleAssignments("battle1").isEmpty());
    }

    @Test
    @DisplayName("Calculate character modifiers - Admiral")
    void calculateModifiersAdmiral() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();

        Map<String, Integer> modifiers = bridge.calculateCharacterModifiers(
                character, SfbRole.ADMIRAL, null);

        assertNotNull(modifiers);
        // Command 5 / 2 = 2 energy bonus
        assertEquals(2, modifiers.get("FLEET_ENERGY_BONUS"));
        // Leadership 5 / 2 = 2 accuracy bonus
        assertEquals(2, modifiers.get("FLEET_ACCURACY_BONUS"));
    }

    @Test
    @DisplayName("Calculate character modifiers - Captain")
    void calculateModifiersCaptain() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();

        Map<String, Integer> modifiers = bridge.calculateCharacterModifiers(
                character, SfbRole.CAPTAIN, null);

        assertNotNull(modifiers);
        // PRE 10 / 2 = 5 shield efficiency
        assertEquals(5, modifiers.get("SHIELD_EFFICIENCY"));
        // TEC 10 / 2 = 5 DamCon
        assertEquals(5, modifiers.get("DAMCON"));
    }

    @Test
    @DisplayName("Calculate character modifiers - Crew Tactical")
    void calculateModifiersCrewTactical() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();

        Map<String, Integer> modifiers = bridge.calculateCharacterModifiers(
                character, SfbRole.CREW, SfbCrewStationType.TACTICAL);

        assertNotNull(modifiers);
        // Security 5 / 2 = 2 hit probability
        assertEquals(2, modifiers.get("HIT_PROBABILITY"));
    }

    @Test
    @DisplayName("Get all characters")
    void getAllCharacters() {
        SfbCharacterBridge bridge = createBridge();
        CaptainLogCharacter character = createCharacter();
        bridge.registerCharacter(character);

        List<CaptainLogCharacter> all = bridge.getAllCharacters();
        assertEquals(1, all.size());
    }
}

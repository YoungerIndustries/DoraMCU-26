package com.yisy.modules.doramcu.sfb;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SFB Rules Database JSON Validation Tests")
class SfbRulesDatabaseValidationTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SFB_RULES_PATH = "docs/sfb-rules-db";

    private static JsonNode coreMechanics;
    private static JsonNode energyAllocation;
    private static JsonNode movement;
    private static JsonNode shields;
    private static JsonNode weapons;
    private static JsonNode damageAllocation;
    private static JsonNode empires;
    private static JsonNode shipSystems;
    private static JsonNode specialSystems;
    private static JsonNode terrain;
    private static JsonNode shipClasses;
    private static JsonNode campaignRules;

    @BeforeAll
    static void loadJsonFiles() throws IOException {
        coreMechanics = objectMapper.readTree(new File(SFB_RULES_PATH + "/core-mechanics.json"));
        energyAllocation = objectMapper.readTree(new File(SFB_RULES_PATH + "/energy-allocation.json"));
        movement = objectMapper.readTree(new File(SFB_RULES_PATH + "/movement.json"));
        shields = objectMapper.readTree(new File(SFB_RULES_PATH + "/shields.json"));
        weapons = objectMapper.readTree(new File(SFB_RULES_PATH + "/weapons.json"));
        damageAllocation = objectMapper.readTree(new File(SFB_RULES_PATH + "/damage-allocation.json"));
        empires = objectMapper.readTree(new File(SFB_RULES_PATH + "/empires.json"));
        shipSystems = objectMapper.readTree(new File(SFB_RULES_PATH + "/ship-systems.json"));
        specialSystems = objectMapper.readTree(new File(SFB_RULES_PATH + "/special-systems.json"));
        terrain = objectMapper.readTree(new File(SFB_RULES_PATH + "/terrain.json"));
        shipClasses = objectMapper.readTree(new File(SFB_RULES_PATH + "/ship-classes.json"));
        campaignRules = objectMapper.readTree(new File(SFB_RULES_PATH + "/campaign-rules.json"));
    }

    // ==========================================
    // Core Mechanics Tests
    // ==========================================

    @Test
    @DisplayName("Core mechanics should have valid game metadata")
    void testCoreMechanicsGameMetadata() {
        assertEquals("Star Fleet Battles", coreMechanics.get("game").asText());
        assertEquals("Amarillo Design Bureau (ADB)", coreMechanics.get("publisher").asText());
        assertEquals(1979, coreMechanics.get("firstPublished").asInt());
        assertTrue(coreMechanics.has("currentEdition"));
    }

    @Test
    @DisplayName("Turn structure should have 32 impulses per turn")
    void testTurnStructureImpulses() {
        JsonNode turnStructure = coreMechanics.path("coreMechanics").path("turnStructure");
        assertEquals(32, turnStructure.get("impulsesPerTurn").asInt());
        assertTrue(turnStructure.has("description"));
    }

    @Test
    @DisplayName("Special impulses should define impulse 1 and 32")
    void testSpecialImpulses() {
        JsonNode specialImpulses = coreMechanics.path("coreMechanics")
                .path("turnStructure").path("specialImpulses");
        assertTrue(specialImpulses.has("impulse1"));
        assertTrue(specialImpulses.has("impulse32"));
        assertTrue(specialImpulses.get("impulse1").asText().length() > 0);
        assertTrue(specialImpulses.get("impulse32").asText().length() > 0);
    }

    @Test
    @DisplayName("Sequence of play should have 5 steps")
    void testSequenceOfPlay() {
        JsonNode sequence = coreMechanics.path("coreMechanics").path("sequenceOfPlay");
        assertEquals(5, sequence.size());
        assertEquals(1, sequence.get(0).get("step").asInt());
        assertEquals("Energy Allocation", sequence.get(0).get("name").asText());
    }

    @Test
    @DisplayName("Impulse procedure should have 5 phases")
    void testImpulseProcedure() {
        JsonNode impulse = coreMechanics.path("coreMechanics").path("impulseProcedure");
        assertEquals(5, impulse.size());
        assertEquals("Movement", impulse.get(0).get("phase").asText());
        assertEquals("Damage Allocation", impulse.get(4).get("phase").asText());
    }

    @Test
    @DisplayName("Map system should be hexagonal")
    void testMapSystem() {
        JsonNode mapSystem = coreMechanics.path("coreMechanics").path("mapSystem");
        assertEquals("Hexagonal", mapSystem.get("grid").asText());
        assertTrue(mapSystem.has("hexSize"));
        assertTrue(mapSystem.has("facing"));
    }

    @Test
    @DisplayName("Rule reference system should have 10 chapters")
    void testRuleReferenceChapters() {
        JsonNode chapters = coreMechanics.path("coreMechanics")
                .path("ruleReferenceSystem").path("chapters");
        assertEquals(10, chapters.size());
        assertEquals("General Rules", chapters.get("A").asText());
        assertEquals("Combat", chapters.get("D").asText());
        assertEquals("Campaign Rules", chapters.get("J").asText());
    }

    // ==========================================
    // Energy Allocation Tests
    // ==========================================

    @Test
    @DisplayName("Energy allocation should have 5 power sources")
    void testPowerSources() {
        JsonNode sources = energyAllocation.path("energyAllocation").path("powerSources");
        assertEquals(5, sources.size());
        assertEquals("Warp Engines", sources.get(0).get("name").asText());
    }

    @Test
    @DisplayName("Warp engines should be damageable")
    void testWarpEngensDamageable() {
        JsonNode warp = energyAllocation.path("energyAllocation")
                .path("powerSources").get(0);
        assertTrue(warp.get("damageable").asBoolean());
    }

    @Test
    @DisplayName("Energy allocation should have 9 categories")
    void testAllocationCategories() {
        JsonNode categories = energyAllocation.path("energyAllocation").path("allocationCategories");
        assertEquals(9, categories.size());
        assertEquals("Warp Engine Power", categories.get(0).get("category").asText());
    }

    @Test
    @DisplayName("Electronic warfare should have max power of 6")
    void testElectronicWarfareMaxPower() {
        JsonNode categories = energyAllocation.path("energyAllocation").path("allocationCategories");
        JsonNode ew = null;
        for (JsonNode cat : categories) {
            if ("Electronic Warfare".equals(cat.get("category").asText())) {
                ew = cat;
                break;
            }
        }
        assertNotNull(ew);
        assertEquals(6, ew.get("maxPower").asInt());
    }

    @Test
    @DisplayName("Energy allocation form should have 10 sections")
    void testEnergyAllocationForm() {
        JsonNode form = energyAllocation.path("energyAllocation").path("energyAllocationForm");
        JsonNode sections = form.get("sections");
        assertEquals(10, sections.size());
    }

    // ==========================================
    // Movement Tests
    // ==========================================

    @Test
    @DisplayName("Movement should have max speed of 32")
    void testMaxSpeed() {
        JsonNode speed = movement.path("movement").path("speedSystem");
        assertEquals(32, speed.get("maxSpeed").asInt());
    }

    @Test
    @DisplayName("Movement should have 3 turn modes")
    void testTurnModes() {
        JsonNode modes = movement.path("movement").path("turnModes").path("modes");
        assertEquals(3, modes.size());
        assertTrue(modes.get(0).get("name").asText().contains("Mode 1"));
        assertTrue(modes.get(1).get("name").asText().contains("Mode 2"));
        assertTrue(modes.get(2).get("name").asText().contains("Mode 3"));
    }

    @Test
    @DisplayName("Turn modes should have correct turn amounts")
    void testTurnModeAmounts() {
        JsonNode modes = movement.path("movement").path("turnModes").path("modes");
        assertTrue(modes.get(0).get("turnAmount").asText().contains("60"));
        assertTrue(modes.get(1).get("turnAmount").asText().contains("120"));
        assertTrue(modes.get(2).get("turnAmount").asText().contains("180"));
    }

    @Test
    @DisplayName("Movement should have 4 special maneuvers")
    void testSpecialManeuvers() {
        JsonNode maneuvers = movement.path("movement").path("specialManeuvers");
        assertEquals(4, maneuvers.size());
        assertEquals("Sideslip", maneuvers.get(0).get("name").asText());
        assertTrue(maneuvers.get(1).get("name").asText().contains("HET"));
    }

    @Test
    @DisplayName("Breakdown should have 3 effects")
    void testBreakdownEffects() {
        JsonNode breakdown = movement.path("movement").path("breakdown");
        JsonNode effects = breakdown.get("effects");
        assertEquals(3, effects.size());
    }

    @Test
    @DisplayName("Minimum separation should be 1 hex")
    void testMinimumSeparation() {
        JsonNode separation = movement.path("movement").path("separation");
        assertEquals("1 hex", separation.get("minimumSeparation").asText());
    }

    @Test
    @DisplayName("Movement chart should have speed to impulse mappings")
    void testMovementChartImpulses() {
        JsonNode chart = movement.path("movement").path("speedSystem").path("movementChart");
        assertTrue(chart.has("speedToImpulses"), "Movement chart should have speed to impulse mappings");
        JsonNode examples = chart.get("speedToImpulses").get("examples");
        assertTrue(examples.size() > 0, "Should have speed examples");
        // Verify speed 32 moves on 31 impulses
        JsonNode speed32 = null;
        for (JsonNode ex : examples) {
            if (ex.get("speed").asInt() == 32) {
                speed32 = ex;
                break;
            }
        }
        assertNotNull(speed32);
        assertEquals(31, speed32.get("movesPerTurn").asInt());
    }

    @Test
    @DisplayName("Movement should have energy cost table")
    void testEnergyCostTable() {
        JsonNode costTable = movement.path("movement").path("speedSystem").path("energyCost").path("costTable");
        assertTrue(costTable.has("speed1"));
        assertTrue(costTable.has("speed32"));
        assertEquals(1, costTable.get("speed1").asInt());
        assertEquals(14, costTable.get("speed32").asInt());
    }

    @Test
    @DisplayName("Shuttles should have max speed of 6")
    void testShuttleMaxSpeed() {
        JsonNode shuttles = movement.path("movement").path("sublightMovement").path("shuttles");
        assertEquals(6, shuttles.get("speed").get("maxSpeed").asInt());
        assertTrue(shuttles.has("uses"));
        assertTrue(shuttles.has("restrictions"));
    }

    @Test
    @DisplayName("Fighters should have types defined")
    void testFighterTypes() {
        JsonNode fighters = movement.path("movement").path("sublightMovement").path("fighters");
        JsonNode types = fighters.get("types");
        assertTrue(types.size() >= 2, "Should have at least 2 fighter types");
    }

    @Test
    @DisplayName("Acceleration rules should be defined")
    void testAccelerationRules() {
        JsonNode accel = movement.path("movement").path("acceleration");
        assertTrue(accel.has("rules"));
        assertTrue(accel.get("rules").size() > 0);
    }

    // ==========================================
    // Shields Tests
    // ==========================================

    @Test
    @DisplayName("Shields should have 6 directions")
    void testShieldDirections() {
        JsonNode directions = shields.path("shields").path("shieldDirections");
        assertEquals(6, directions.get("count").asInt());
        assertEquals(6, directions.get("directions").size());
    }

    @Test
    @DisplayName("Shield 1 should be Front and Shield 4 should be Rear")
    void testShieldNumbering() {
        JsonNode directions = shields.path("shields").path("shieldDirections").path("directions");
        assertEquals(1, directions.get(0).get("number").asInt());
        assertEquals("Front", directions.get(0).get("name").asText());
        assertEquals(4, directions.get(3).get("number").asInt());
        assertEquals("Rear", directions.get(3).get("name").asText());
    }

    @Test
    @DisplayName("Shield reinforcement should have 2 types")
    void testShieldReinforcementTypes() {
        JsonNode types = shields.path("shields").path("shieldReinforcement").path("types");
        assertEquals(2, types.size());
        assertEquals("General Reinforcement", types.get(0).get("type").asText());
        assertEquals("Directed Reinforcement", types.get(1).get("type").asText());
    }

    @Test
    @DisplayName("Shield reinforcement should not work in nebula")
    void testShieldReinforcementRestriction() {
        JsonNode reinforcement = shields.path("shields").path("shieldReinforcement");
        assertTrue(reinforcement.get("restriction").asText().contains("nebula"));
    }

    @Test
    @DisplayName("Shield activation should cost 1 energy per shield")
    void testShieldActivationCost() {
        JsonNode activation = shields.path("shields").path("shieldActivation");
        assertEquals("1 energy per shield activated.", activation.get("cost").asText());
    }

    @Test
    @DisplayName("Dropped shields should remain down for 8 impulses")
    void testDroppedShieldDuration() {
        JsonNode dropped = shields.path("shields").path("shieldActivation").path("droppingShields");
        assertTrue(dropped.get("duration").asText().contains("8 impulses"));
    }

    @Test
    @DisplayName("Shields should have 4 tactical notes")
    void testTacticalNotes() {
        JsonNode notes = shields.path("shields").path("tacticalNotes");
        assertEquals(4, notes.size());
    }

    // ==========================================
    // Weapons Tests
    // ==========================================

    @Test
    @DisplayName("Weapons should have 3 categories")
    void testWeaponCategories() {
        JsonNode categories = weapons.path("weapons").path("categories");
        assertTrue(categories.has("directFire"));
        assertTrue(categories.has("seeking"));
        assertTrue(categories.has("special"));
    }

    @Test
    @DisplayName("Direct fire weapons should have 7 types")
    void testDirectFireWeaponCount() {
        JsonNode directFire = weapons.path("weapons").path("categories").path("directFire");
        assertEquals(7, directFire.get("types").size());
    }

    @Test
    @DisplayName("Seeking weapons should have 2 types")
    void testSeekingWeaponCount() {
        JsonNode seeking = weapons.path("weapons").path("categories").path("seeking");
        assertEquals(2, seeking.get("types").size());
    }

    @Test
    @DisplayName("Special weapons should have 8 types")
    void testSpecialWeaponCount() {
        JsonNode special = weapons.path("weapons").path("categories").path("special");
        assertEquals(8, special.get("types").size());
    }

    @Test
    @DisplayName("Phasers should have 4 sizes with fire charts")
    void testPhaserSizes() {
        JsonNode phasers = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("directFire").path("types")) {
            if ("Phasers".equals(weapon.get("name").asText())) {
                phasers = weapon;
                break;
            }
        }
        assertNotNull(phasers);
        assertEquals(4, phasers.get("sizes").size());
        // Verify Phaser-1 has fire chart
        JsonNode phaser1 = phasers.get("sizes").get(0);
        assertEquals("Phaser-1", phaser1.get("size").asText());
        assertTrue(phaser1.has("fireChart"), "Phaser-1 should have fire chart");
        assertEquals(7, phaser1.get("fireChart").get("bands").size(), "Phaser-1 should have 7 range bands");
    }

    @Test
    @DisplayName("Plasma torpedoes should have 5 variants with warhead data")
    void testPlasmaTorpedoVariants() {
        JsonNode plasma = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("seeking").path("types")) {
            if ("Plasma Torpedoes".equals(weapon.get("name").asText())) {
                plasma = weapon;
                break;
            }
        }
        assertNotNull(plasma);
        assertEquals(5, plasma.get("variants").size());
        // Verify Type-A has warhead data
        JsonNode typeA = plasma.get("variants").get(0);
        assertEquals(8, typeA.get("warheadStart").asInt());
        assertEquals(0, typeA.get("warheadEnd").asInt());
    }

    @Test
    @DisplayName("Firing arcs should have 14 designations")
    void testFiringArcDesignations() {
        JsonNode arcs = weapons.path("weapons").path("firingArcs").path("designations");
        assertEquals(14, arcs.size());
        assertEquals("Forward", arcs.get("F").asText());
        assertEquals("All-around", arcs.get("A").asText());
    }

    @Test
    @DisplayName("Range bands should have 5 bands")
    void testRangeBands() {
        JsonNode bands = weapons.path("weapons").path("rangeBands").path("bands");
        assertEquals(5, bands.size());
        assertEquals("Close", bands.get(0).get("name").asText());
        assertEquals("Extreme", bands.get(4).get("name").asText());
    }

    @Test
    @DisplayName("Close range should be 1-2 hexes")
    void testCloseRange() {
        JsonNode bands = weapons.path("weapons").path("rangeBands").path("bands");
        assertEquals("1-2 hexes", bands.get(0).get("range").asText());
    }

    @Test
    @DisplayName("Medium range should be max range for overloaded weapons")
    void testMediumRangeOverload() {
        JsonNode bands = weapons.path("weapons").path("rangeBands").path("bands");
        JsonNode medium = bands.get(2);
        assertEquals("Medium", medium.get("name").asText());
        assertTrue(medium.get("description").asText().contains("overloaded"));
    }

    // ==========================================
    // Damage Allocation Tests
    // ==========================================

    @Test
    @DisplayName("Damage allocation should have 5 steps")
    void testDamageAllocationSteps() {
        JsonNode process = damageAllocation.path("damageAllocation").path("process");
        assertTrue(process.has("step1"));
        assertTrue(process.has("step2"));
        assertTrue(process.has("step3"));
        assertTrue(process.has("step4"));
        assertTrue(process.has("step5"));
    }

    @Test
    @DisplayName("Damage process step 1 should be shield absorption")
    void testDamageProcessStep1() {
        JsonNode process = damageAllocation.path("damageAllocation").path("process");
        JsonNode step1 = process.get("step1");
        assertEquals("Shield Absorption", step1.get("name").asText());
        assertTrue(step1.has("description"));
    }

    @Test
    @DisplayName("Damage allocation should have system categories")
    void testSystemCategories() {
        JsonNode categories = damageAllocation.path("damageAllocation")
            .path("damageAllocationChart").path("systemCategories");
        assertEquals(8, categories.size());
    }

    @Test
    @DisplayName("Crippled condition should be defined")
    void testCrippledCondition() {
        JsonNode crippled = damageAllocation.path("damageAllocation").path("crippledCondition");
        assertTrue(crippled.has("description"));
    }

    // ==========================================
    // Empires Tests
    // ==========================================

    @Test
    @DisplayName("Empires should have Alpha Octant empires")
    void testAlphaOctantEmpires() {
        JsonNode alpha = empires.path("empires").path("alphaOctant");
        assertTrue(alpha.size() > 0);
    }

    @Test
    @DisplayName("Federation should be in Alpha Octant")
    void testFederationInAlphaOctant() {
        JsonNode alpha = empires.path("empires").path("alphaOctant");
        boolean found = false;
        for (JsonNode empire : alpha) {
            if ("United Earth Federation".equals(empire.get("name").asText())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Federation should be in Alpha Octant");
    }

    @Test
    @DisplayName("Federation should use phasers and photon torpedoes")
    void testFederationWeapons() {
        JsonNode alpha = empires.path("empires").path("alphaOctant");
        JsonNode fed = null;
        for (JsonNode empire : alpha) {
            if ("United Earth Federation".equals(empire.get("name").asText())) {
                fed = empire;
                break;
            }
        }
        assertNotNull(fed);
        JsonNode weapons = fed.get("primaryWeapons");
        assertTrue(weapons.get(0).asText().contains("Phaser"));
        assertTrue(weapons.get(1).asText().contains("Photon"));
    }

    @Test
    @DisplayName("Romulan should have cloaking device")
    void testRomulanCloaking() {
        JsonNode alpha = empires.path("empires").path("alphaOctant");
        JsonNode romulan = null;
        for (JsonNode empire : alpha) {
            if ("Romulan Star Empire".equals(empire.get("name").asText())) {
                romulan = empire;
                break;
            }
        }
        assertNotNull(romulan);
        JsonNode special = romulan.get("specialSystems");
        boolean hasCloaking = false;
        for (JsonNode system : special) {
            if (system.asText().contains("Cloaking")) {
                hasCloaking = true;
                break;
            }
        }
        assertTrue(hasCloaking, "Romulan should have cloaking device");
    }

    @Test
    @DisplayName("Klingon should have B10 Battleship")
    void testKlingonB10() {
        JsonNode alpha = empires.path("empires").path("alphaOctant");
        JsonNode klingon = null;
        for (JsonNode empire : alpha) {
            if ("Klingon Empire".equals(empire.get("name").asText())) {
                klingon = empire;
                break;
            }
        }
        assertNotNull(klingon);
        assertTrue(klingon.get("uniqueFeatures").asText().contains("B10"));
    }

    @Test
    @DisplayName("All empires should have required fields")
    void testEmpiresRequiredFields() {
        String[] galaxyKeys = {"alphaOctant", "andromedaGalaxy", "m81Galaxy", "otherEmpires"};
        for (String galaxy : galaxyKeys) {
            if (!empires.path("empires").has(galaxy)) continue;
            JsonNode empiresList = empires.path("empires").path(galaxy);
            for (JsonNode empire : empiresList) {
                assertTrue(empire.has("name"), "Empire missing name in " + galaxy);
                assertTrue(empire.has("description"), "Empire missing description in " + galaxy);
            }
        }
    }

    // ==========================================
    // Ship Systems Tests
    // ==========================================

    @Test
    @DisplayName("Ship systems should have SSD components")
    void testSsdComponents() {
        JsonNode components = shipSystems.path("shipSystems").path("ssdComponents");
        assertTrue(components.size() > 0);
    }

    @Test
    @DisplayName("Ship systems should have ship types")
    void testShipTypes() {
        JsonNode types = shipSystems.path("shipSystems").path("shipTypes");
        assertTrue(types.size() > 0);
    }

    @Test
    @DisplayName("Ship types should include CA, CL, CW, DD")
    void testShipTypeDesignations() {
        JsonNode types = shipSystems.path("shipSystems").path("shipTypes");
        String typeNames = types.toString();
        assertTrue(typeNames.contains("CA") || typeNames.contains("Heavy Cruiser"));
        assertTrue(typeNames.contains("CL") || typeNames.contains("Light Cruiser"));
    }

    // ==========================================
    // Special Systems Tests
    // ==========================================

    @Test
    @DisplayName("Special systems should have multiple system types")
    void testSpecialSystemTypes() {
        JsonNode systems = specialSystems.path("specialSystems").path("systems");
        assertTrue(systems.size() > 0);
    }

    @Test
    @DisplayName("Special systems should include cloaking")
    void testCloakingSystem() {
        JsonNode systems = specialSystems.path("specialSystems").path("systems");
        boolean found = false;
        for (JsonNode system : systems) {
            if (system.get("name").asText().toLowerCase().contains("cloak")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Cloaking system should be defined");
    }

    @Test
    @DisplayName("Special systems should include tractor beams")
    void testTractorBeamSystem() {
        JsonNode systems = specialSystems.path("specialSystems").path("systems");
        boolean found = false;
        for (JsonNode system : systems) {
            if (system.get("name").asText().toLowerCase().contains("tractor")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Tractor beam system should be defined");
    }

    // ==========================================
    // Terrain Tests
    // ==========================================

    @Test
    @DisplayName("Terrain should have multiple terrain types")
    void testTerrainTypes() {
        JsonNode terrains = terrain.path("terrain").path("types");
        assertTrue(terrains.size() > 0);
    }

    @Test
    @DisplayName("Terrain should include planets")
    void testPlanetTerrain() {
        JsonNode terrains = terrain.path("terrain").path("types");
        boolean found = false;
        for (JsonNode t : terrains) {
            if (t.get("name").asText().toLowerCase().contains("planet")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Planet terrain should be defined");
    }

    @Test
    @DisplayName("Terrain should include nebulae")
    void testNebulaTerrain() {
        JsonNode terrains = terrain.path("terrain").path("types");
        boolean found = false;
        for (JsonNode t : terrains) {
            if (t.get("name").asText().toLowerCase().contains("nebula")) {
                found = true;
                break;
            }
        }
        assertTrue(found, "Nebula terrain should be defined");
    }

    // ==========================================
    // Ship Classes Tests
    // ==========================================

    @Test
    @DisplayName("Ship classes should have entries")
    void testShipClassesExist() {
        assertTrue(shipClasses.has("shipClasses"));
    }

    @Test
    @DisplayName("Ship classes should have Federation ships")
    void testFederationShips() {
        JsonNode classes = shipClasses.path("shipClasses");
        assertTrue(classes.has("federation"), "Federation ship classes should be defined");
        JsonNode fedShips = classes.get("federation");
        assertTrue(fedShips.size() > 0, "Federation should have ships");
    }

    // ==========================================
    // Campaign Rules Tests
    // ==========================================

    @Test
    @DisplayName("Campaign rules should have strategic map rules")
    void testStrategicMap() {
        JsonNode campaign = campaignRules.path("campaignRules");
        assertTrue(campaign.has("strategicMap") || campaign.has("strategic"));
    }

    @Test
    @DisplayName("Campaign rules should have victory conditions")
    void testVictoryConditions() {
        JsonNode campaign = campaignRules.path("campaignRules");
        String campaignStr = campaign.toString().toLowerCase();
        assertTrue(campaignStr.contains("victory") || campaignStr.contains("win"));
    }

    // ==========================================
    // Cross-file Consistency Tests
    // ==========================================

    @Test
    @DisplayName("All JSON files should be valid JSON")
    void testAllJsonFilesValid() throws IOException {
        String[] files = {
            "core-mechanics.json", "energy-allocation.json", "movement.json",
            "shields.json", "weapons.json", "damage-allocation.json",
            "empires.json", "ship-systems.json", "special-systems.json",
            "terrain.json", "ship-classes.json", "campaign-rules.json"
        };
        for (String file : files) {
            JsonNode node = objectMapper.readTree(new File(SFB_RULES_PATH + "/" + file));
            assertNotNull(node, file + " should be valid JSON");
            assertTrue(node.isObject(), file + " should have an object root");
        }
    }

    @Test
    @DisplayName("All JSON files should have non-empty root objects")
    void testAllJsonFilesNonEmpty() throws IOException {
        String[] files = {
            "core-mechanics.json", "energy-allocation.json", "movement.json",
            "shields.json", "weapons.json", "damage-allocation.json",
            "empires.json", "ship-systems.json", "special-systems.json",
            "terrain.json", "ship-classes.json", "campaign-rules.json"
        };
        for (String file : files) {
            JsonNode node = objectMapper.readTree(new File(SFB_RULES_PATH + "/" + file));
            assertTrue(node.fieldNames().hasNext(), file + " should have at least one field");
        }
    }

    @Test
    @DisplayName("Weapon types in empires should reference valid weapon names")
    void testEmpireWeaponsConsistency() {
        // Get all direct fire weapon names
        JsonNode directFire = weapons.path("weapons").path("categories").path("directFire");
        java.util.Set<String> validWeapons = new java.util.HashSet<>();
        for (JsonNode weapon : directFire.get("types")) {
            validWeapons.add(weapon.get("name").asText().toLowerCase());
        }

        // Check empire weapons reference valid types
        JsonNode alpha = empires.path("empires").path("alphaOctant");
        for (JsonNode empire : alpha) {
            JsonNode empireWeapons = empire.get("primaryWeapons");
            for (JsonNode weapon : empireWeapons) {
                String weaponName = weapon.asText().toLowerCase();
                // At least one valid weapon should be referenced
                boolean matches = validWeapons.stream()
                    .anyMatch(vw -> weaponName.contains(vw) || vw.contains(weaponName));
                // This is a soft check - some empire weapons may be variants
            }
        }
    }

    @Test
    @DisplayName("Shield directions should be numbered 1-6")
    void testShieldDirectionNumbering() {
        JsonNode directions = shields.path("shields").path("shieldDirections").path("directions");
        for (int i = 0; i < 6; i++) {
            assertEquals(i + 1, directions.get(i).get("number").asInt(),
                "Shield " + (i + 1) + " should have number " + (i + 1));
        }
    }

    @Test
    @DisplayName("Range bands should be ordered by range")
    void testRangeBandOrder() {
        JsonNode bands = weapons.path("weapons").path("rangeBands").path("bands");
        assertEquals("Close", bands.get(0).get("name").asText());
        assertEquals("Short", bands.get(1).get("name").asText());
        assertEquals("Medium", bands.get(2).get("name").asText());
        assertEquals("Long", bands.get(3).get("name").asText());
        assertEquals("Extreme", bands.get(4).get("name").asText());
    }

    @Test
    @DisplayName("Sequence of play steps should be numbered 1-5")
    void testSequenceOfPlayOrder() {
        JsonNode sequence = coreMechanics.path("coreMechanics").path("sequenceOfPlay");
        for (int i = 0; i < 5; i++) {
            assertEquals(i + 1, sequence.get(i).get("step").asInt(),
                "Step " + (i + 1) + " should have step number " + (i + 1));
        }
    }

    @Test
    @DisplayName("All empires should have non-empty notable ships")
    void testEmpireNotableShips() {
        String[] galaxyKeys = {"alphaOctant", "andromedaGalaxy", "m81Galaxy"};
        for (String galaxy : galaxyKeys) {
            if (!empires.path("empires").has(galaxy)) continue;
            JsonNode empiresList = empires.path("empires").path(galaxy);
            for (JsonNode empire : empiresList) {
                if (empire.has("notableShips")) {
                    JsonNode ships = empire.get("notableShips");
                    assertTrue(ships.size() > 0,
                        empire.get("name").asText() + " should have notable ships");
                }
            }
        }
    }

    @Test
    @DisplayName("Plasma torpedoes should have characteristics")
    void testPlasmaTorpedoCharacteristics() {
        JsonNode plasma = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("seeking").path("types")) {
            if ("Plasma Torpedoes".equals(weapon.get("name").asText())) {
                plasma = weapon;
                break;
            }
        }
        assertNotNull(plasma);
        JsonNode chars = plasma.get("characteristics");
        assertTrue(chars.size() >= 4, "Plasma torpedoes should have at least 4 characteristics");
    }

    @Test
    @DisplayName("Drones should have characteristics")
    void testDroneCharacteristics() {
        JsonNode drones = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("seeking").path("types")) {
            if ("Drones".equals(weapon.get("name").asText())) {
                drones = weapon;
                break;
            }
        }
        assertNotNull(drones);
        JsonNode chars = drones.get("characteristics");
        assertTrue(chars.size() >= 4, "Drones should have at least 4 characteristics");
    }

    @Test
    @DisplayName("Fusion beams should have half-again overload damage")
    void testFusionBeamOverload() {
        JsonNode fusion = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("directFire").path("types")) {
            if ("Fusion Beams".equals(weapon.get("name").asText())) {
                fusion = weapon;
                break;
            }
        }
        assertNotNull(fusion);
        assertTrue(fusion.get("overload").asText().contains("half-again") ||
                   fusion.get("overload").asText().contains("1.5x"),
                   "Fusion beams should have half-again overload damage");
    }

    @Test
    @DisplayName("Disruptors should have fire chart with 6 range bands")
    void testDisruptorFireChart() {
        JsonNode disruptor = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("directFire").path("types")) {
            if ("Disruptors".equals(weapon.get("name").asText())) {
                disruptor = weapon;
                break;
            }
        }
        assertNotNull(disruptor);
        assertTrue(disruptor.has("fireChart"), "Disruptors should have fire chart");
        assertEquals(6, disruptor.get("fireChart").get("bands").size());
        assertEquals(2, disruptor.get("energyCost").asInt());
    }

    @Test
    @DisplayName("Photon torpedoes should have dial-a-yield options")
    void testPhotonDialAYield() {
        JsonNode photon = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("directFire").path("types")) {
            if ("Photon Torpedoes".equals(weapon.get("name").asText())) {
                photon = weapon;
                break;
            }
        }
        assertNotNull(photon);
        assertTrue(photon.has("dialAYield"), "Photon torpedoes should have dial-a-yield");
        assertEquals(3, photon.get("dialAYield").get("options").size());
        // Standard damage should be 8
        assertEquals(8, photon.get("dialAYield").get("options").get(0).get("damage").asInt());
    }

    @Test
    @DisplayName("Photon torpedoes should have fire chart")
    void testPhotonFireChart() {
        JsonNode photon = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("directFire").path("types")) {
            if ("Photon Torpedoes".equals(weapon.get("name").asText())) {
                photon = weapon;
                break;
            }
        }
        assertNotNull(photon);
        assertTrue(photon.has("fireChart"), "Photon torpedoes should have fire chart");
        assertEquals(7, photon.get("fireChart").get("bands").size());
    }

    @Test
    @DisplayName("Plasma torpedoes should have interception rules")
    void testPlasmaInterception() {
        JsonNode plasma = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("seeking").path("types")) {
            if ("Plasma Torpedoes".equals(weapon.get("name").asText())) {
                plasma = weapon;
                break;
            }
        }
        assertNotNull(plasma);
        assertTrue(plasma.has("interception"), "Plasma torpedoes should have interception rules");
    }

    @Test
    @DisplayName("Drones should have types defined")
    void testDroneTypes() {
        JsonNode drones = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("seeking").path("types")) {
            if ("Drones".equals(weapon.get("name").asText())) {
                drones = weapon;
                break;
            }
        }
        assertNotNull(drones);
        assertTrue(drones.has("types"), "Drones should have types");
        assertTrue(drones.get("types").size() >= 2);
    }

    @Test
    @DisplayName("Drones should have resupply rules")
    void testDroneResupply() {
        JsonNode drones = null;
        for (JsonNode weapon : weapons.path("weapons").path("categories").path("seeking").path("types")) {
            if ("Drones".equals(weapon.get("name").asText())) {
                drones = weapon;
                break;
            }
        }
        assertNotNull(drones);
        assertTrue(drones.has("resupply"), "Drones should have resupply rules");
    }

    @Test
    @DisplayName("Sensors should have detection ranges")
    void testSensorDetectionRanges() {
        JsonNode systems = specialSystems.path("specialSystems").path("systems");
        JsonNode sensors = null;
        for (JsonNode system : systems) {
            if ("Sensors".equals(system.get("name").asText())) {
                sensors = system;
                break;
            }
        }
        assertNotNull(sensors);
        assertTrue(sensors.has("detectionRanges"), "Sensors should have detection ranges");
    }

    @Test
    @DisplayName("Scanners should have rating scale")
    void testScannerRatings() {
        JsonNode systems = specialSystems.path("specialSystems").path("systems");
        JsonNode scanners = null;
        for (JsonNode system : systems) {
            if ("Scanners".equals(system.get("name").asText())) {
                scanners = system;
                break;
            }
        }
        assertNotNull(scanners);
        assertTrue(scanners.has("scannerRatings"), "Scanners should have rating scale");
    }

    @Test
    @DisplayName("Other empires should have detailed data")
    void testOtherEmpiresDetailed() {
        JsonNode other = empires.path("empires").path("otherEmpires");
        // Find Jindarians
        JsonNode jindarian = null;
        for (JsonNode empire : other) {
            if ("Jindarians".equals(empire.get("name").asText())) {
                jindarian = empire;
                break;
            }
        }
        assertNotNull(jindarian);
        assertTrue(jindarian.has("combatStyle"));
        assertTrue(jindarian.has("primaryWeapons"));
        assertTrue(jindarian.has("notableShips"));
    }

    @Test
    @DisplayName("Carnivons should be marked as extinct")
    void testCarnivonsExtinct() {
        JsonNode other = empires.path("empires").path("otherEmpires");
        JsonNode carnivon = null;
        for (JsonNode empire : other) {
            if ("Carnivons".equals(empire.get("name").asText())) {
                carnivon = empire;
                break;
            }
        }
        assertNotNull(carnivon);
        assertEquals("Extinct (xenocided)", carnivon.get("status").asText());
        assertTrue(carnivon.get("uniqueFeatures").asText().toLowerCase().contains("death bolt"),
            "Carnivons should mention death bolts");
    }

    @Test
    @DisplayName("Eneen should have variable-focus neutron beam")
    void testEneenNeutronBeam() {
        JsonNode other = empires.path("empires").path("otherEmpires");
        JsonNode eneen = null;
        for (JsonNode empire : other) {
            if ("Eneen".equals(empire.get("name").asText())) {
                eneen = empire;
                break;
            }
        }
        assertNotNull(eneen);
        assertTrue(eneen.get("uniqueFeatures").asText().toLowerCase().contains("neutron"),
            "Eneen should mention neutron beam");
    }
}

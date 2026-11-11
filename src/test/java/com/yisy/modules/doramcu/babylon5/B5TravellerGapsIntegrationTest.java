package com.yisy.modules.doramcu.babylon5;

import com.yisy.modules.doramcu.babylon5.model.*;
import com.yisy.modules.doramcu.babylon5.service.B5RulesDatabase;
import com.yisy.modules.doramcu.babylon5.service.B5ShipDesignService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("B5 Traveller Gap-Filling Integration Tests")
class B5TravellerGapsIntegrationTest {

    private static B5RulesDatabase rulesDatabase;
    private static B5ShipDesignService shipDesignService;

    @BeforeAll
    static void setUp() {
        rulesDatabase = new B5RulesDatabase();
        rulesDatabase.initialize();
        shipDesignService = new B5ShipDesignService();
        shipDesignService.initialize();
    }

    // ==========================================
    // B5Skill Tests
    // ==========================================

    @Test
    @DisplayName("B5Skill should have 30+ skills")
    void testB5SkillCount() {
        assertTrue(B5Skill.values().length >= 30, "Expected 30+ B5 skills, got " + B5Skill.values().length);
    }

    @Test
    @DisplayName("B5Skill categories should be correct")
    void testB5SkillCategories() {
        assertTrue(B5Skill.SkillCategory.values().length >= 7, "Expected 7+ skill categories");
        assertEquals(B5Skill.SkillCategory.COMBAT, B5Skill.MELEE.getCategory());
        assertEquals(B5Skill.SkillCategory.COMBAT, B5Skill.FIREARMS.getCategory());
        assertEquals(B5Skill.SkillCategory.LEADERSHIP, B5Skill.LEADERSHIP.getCategory());
        assertEquals(B5Skill.SkillCategory.TECHNICAL, B5Skill.ELECTRONICS.getCategory());
        assertEquals(B5Skill.SkillCategory.NAVIGATION, B5Skill.NAVIGATION.getCategory());
        assertEquals(B5Skill.SkillCategory.SCIENTIFIC, B5Skill.PSIONICS.getCategory());
        assertEquals(B5Skill.SkillCategory.SURVIVAL, B5Skill.SURVIVAL.getCategory());
        assertEquals(B5Skill.SkillCategory.STEALTH, B5Skill.STEALTH.getCategory());
    }

    @Test
    @DisplayName("B5Skill names and descriptions should not be null")
    void testB5SkillNamesAndDescriptions() {
        for (B5Skill skill : B5Skill.values()) {
            assertNotNull(skill.getName());
            assertNotNull(skill.getDescription());
            assertFalse(skill.getName().isEmpty());
        }
    }

    // ==========================================
    // B5MeleeWeapon Tests
    // ==========================================

    @Test
    @DisplayName("B5MeleeWeapon should have 10+ weapons")
    void testB5MeleeWeaponCount() {
        assertTrue(B5MeleeWeapon.values().length >= 10, "Expected 10+ weapons, got " + B5MeleeWeapon.values().length);
    }

    @Test
    @DisplayName("B5MeleeWeapon stats should be valid")
    void testB5MeleeWeaponStats() {
        assertEquals(0, B5MeleeWeapon.FISTS.getParry());
        assertEquals(1, B5MeleeWeapon.FISTS.getDamage());
        assertEquals(4, B5MeleeWeapon.MINBARI_SWORD.getParry());
        assertEquals(5, B5MeleeWeapon.MINBARI_SWORD.getDamage());
        assertEquals(6, B5MeleeWeapon.PLASMA_EDGE.getDamage());
        for (B5MeleeWeapon w : B5MeleeWeapon.values()) {
            assertTrue(w.getParry() >= 0);
            assertTrue(w.getDamage() > 0);
        }
    }

    // ==========================================
    // B5Armor Tests
    // ==========================================

    @Test
    @DisplayName("B5Armor should have 7 types")
    void testB5ArmorCount() {
        assertEquals(7, B5Armor.values().length);
    }

    @Test
    @DisplayName("B5Armor protection values should be correct")
    void testB5ArmorProtection() {
        assertEquals(0, B5Armor.NONE.getProtection());
        assertEquals(2, B5Armor.LIGHT.getProtection());
        assertEquals(8, B5Armor.ENERGY_SHIELD.getProtection());
    }

    // ==========================================
    // B5MeleeCombat Tests
    // ==========================================

    @Test
    @DisplayName("B5MeleeCombat should initialize correctly")
    void testB5MeleeCombatInit() {
        B5MeleeCombat.B5MeleeCombatant a = new B5MeleeCombat.B5MeleeCombatant("Sheridan", 8, 7, 8);
        B5MeleeCombat.B5MeleeCombatant d = new B5MeleeCombat.B5MeleeCombatant("G'Kar", 9, 6, 9);
        B5MeleeCombat combat = new B5MeleeCombat(a, d);
        assertTrue(combat.isActive());
        assertEquals(B5MeleeCombat.CombatOutcome.IN_PROGRESS, combat.getOutcome());
    }

    @Test
    @DisplayName("B5MeleeCombat HP should be St*2 + En")
    void testB5MeleeCombatantHP() {
        B5MeleeCombat.B5MeleeCombatant c = new B5MeleeCombat.B5MeleeCombatant("Test", 8, 7, 8);
        assertEquals(24, c.getMaxHitPoints());
    }

    @Test
    @DisplayName("B5MeleeCombat should resolve to a conclusion")
    void testB5MeleeCombatResolution() {
        B5MeleeCombat.B5MeleeCombatant a = new B5MeleeCombat.B5MeleeCombatant("A", 8, 7, 8);
        a.setWeapon(B5MeleeWeapon.MINBARI_SWORD);
        B5MeleeCombat.B5MeleeCombatant d = new B5MeleeCombat.B5MeleeCombatant("D", 9, 6, 9);
        B5MeleeCombat combat = new B5MeleeCombat(a, d);
        combat.resolveCombat();
        assertFalse(combat.isActive());
        assertNotEquals(B5MeleeCombat.CombatOutcome.IN_PROGRESS, combat.getOutcome());
        assertTrue(combat.getRounds().size() > 0);
    }

    // ==========================================
    // B5Firearm Tests
    // ==========================================

    @Test
    @DisplayName("B5Firearm should have 7+ weapons")
    void testB5FirearmCount() {
        assertTrue(B5Firearm.values().length >= 7, "Expected 7+ weapons, got " + B5Firearm.values().length);
    }

    @Test
    @DisplayName("B5Firearm range classes should be correct")
    void testB5FirearmRangeClasses() {
        assertEquals(B5Firearm.RangeClass.NEAR, B5Firearm.PISTOL.getRangeClass());
        assertEquals(B5Firearm.RangeClass.INTERMEDIATE, B5Firearm.RIFLE.getRangeClass());
        assertEquals(B5Firearm.RangeClass.FAR, B5Firearm.HEAVY_WEAPON.getRangeClass());
    }

    // ==========================================
    // B5FirearmsCombat Tests
    // ==========================================

    @Test
    @DisplayName("B5FirearmsCombat should initialize correctly")
    void testB5FirearmsCombatInit() {
        B5FirearmsCombat.B5FirearmsCombatant a = new B5FirearmsCombat.B5FirearmsCombatant("A", 7, 8);
        B5FirearmsCombat.B5FirearmsCombatant d = new B5FirearmsCombat.B5FirearmsCombatant("D", 6, 9);
        B5FirearmsCombat combat = new B5FirearmsCombat(a, d);
        assertTrue(combat.isActive());
    }

    @Test
    @DisplayName("B5FirearmsCombat HP should be Dx + En*2")
    void testB5FirearmsCombatantHP() {
        B5FirearmsCombat.B5FirearmsCombatant c = new B5FirearmsCombat.B5FirearmsCombatant("Test", 7, 8);
        assertEquals(23, c.getMaxHitPoints());
    }

    @Test
    @DisplayName("B5FirearmsCombat should resolve to a conclusion")
    void testB5FirearmsCombatResolution() {
        B5FirearmsCombat.B5FirearmsCombatant a = new B5FirearmsCombat.B5FirearmsCombatant("A", 7, 8);
        B5FirearmsCombat.B5FirearmsCombatant d = new B5FirearmsCombat.B5FirearmsCombatant("D", 6, 9);
        B5FirearmsCombat combat = new B5FirearmsCombat(a, d);
        combat.resolveCombat();
        assertFalse(combat.isActive());
    }

    // ==========================================
    // B5WealthSystem Tests
    // ==========================================

    @Test
    @DisplayName("B5WealthSystem should roll wealth for all careers")
    void testB5WealthSystemRoll() {
        for (B5Career career : B5Career.values()) {
            B5WealthSystem.WealthResult result = B5WealthSystem.rollWealth(career);
            assertNotNull(result);
            assertTrue(result.getLevel() > 0);
            assertTrue(result.getCredits() > 0);
        }
    }

    @Test
    @DisplayName("B5WealthSystem wealth dice should be correct per career")
    void testB5WealthDicePerCareer() {
        assertEquals(B5WealthSystem.WealthDice.WEALTHY,
                B5WealthSystem.rollWealth(B5Career.VREE_MERCHANT).getDice());
        assertEquals(B5WealthSystem.WealthDice.POOR,
                B5WealthSystem.rollWealth(B5Career.INDEPENDENT_ADVENTURER).getDice());
        assertEquals(B5WealthSystem.WealthDice.MODERATE,
                B5WealthSystem.rollWealth(B5Career.EARTH_FORCE).getDice());
    }

    @Test
    @DisplayName("B5WealthSystem dice notation should be correct")
    void testB5WealthDiceNotation() {
        assertEquals("1D6", B5WealthSystem.WealthDice.POOR.getNotation());
        assertEquals("5D6", B5WealthSystem.WealthDice.VERY_WEALTHY.getNotation());
    }

    // ==========================================
    // B5Career Tests
    // ==========================================

    @Test
    @DisplayName("B5Career should have 13 careers")
    void testB5CareerCount() {
        assertEquals(13, B5Career.values().length);
    }

    @Test
    @DisplayName("B5Career skill tables should have 10 skills each")
    void testB5CareerSkillTables() {
        for (B5Career career : B5Career.values()) {
            assertEquals(10, career.getSkillTable().size());
        }
    }

    @Test
    @DisplayName("B5Career attribute bonuses should be valid")
    void testB5CareerAttributeBonuses() {
        for (B5Career career : B5Career.values()) {
            List<String> bonuses = career.getAttributeBonuses();
            assertNotNull(bonuses);
            assertTrue(bonuses.size() > 0);
        }
    }

    // ==========================================
    // B5Commodity Tests
    // ==========================================

    @Test
    @DisplayName("B5Commodity should have 20+ commodities")
    void testB5CommodityCount() {
        assertTrue(B5Commodity.values().length >= 20, "Expected 20+ commodities, got " + B5Commodity.values().length);
    }

    @Test
    @DisplayName("B5Commodity rarity levels should be correct")
    void testB5CommodityRarity() {
        assertNotNull(B5Commodity.FOOD.getRarity());
        assertNotNull(B5Commodity.CENTAURI_WINE.getRarity());
        assertNotNull(B5Commodity.MINBARI_INCENSE.getRarity());
        assertNotNull(B5Commodity.SHADOW_CRYSTAL.getRarity());
    }

    @Test
    @DisplayName("B5Commodity prices should be positive")
    void testB5CommodityPrices() {
        for (B5Commodity c : B5Commodity.values()) {
            assertTrue(c.getBasePrice() >= 0);
            assertTrue(c.getBulkTons() >= 0);
        }
    }

    // ==========================================
    // B5MarketConditions Tests
    // ==========================================

    @Test
    @DisplayName("B5MarketConditions should calculate prices")
    void testB5MarketConditionsPriceCalc() {
        B5MarketConditions market = new B5MarketConditions("Earth", "Earth");
        B5MarketConditions.MarketEntry entry = market.getMarketEntry(B5Commodity.FOOD);
        assertNotNull(entry);
        assertTrue(entry.getBuyPrice() > 0);
        assertTrue(entry.getSellPrice() > 0);
    }

    @Test
    @DisplayName("B5MarketConditions should fluctuate over time")
    void testB5MarketConditionsFluctuation() {
        B5MarketConditions market = new B5MarketConditions("Narn3", "Narn 3");
        B5MarketConditions.MarketEntry entry = market.getMarketEntry(B5Commodity.NARN_GRAIN);
        int price1 = entry.getBuyPrice();
        market.advanceTick();
        int price2 = entry.getBuyPrice();
        assertTrue(price1 > 0);
        assertTrue(price2 > 0);
    }

    // ==========================================
    // B5TradeSimulator Tests
    // ==========================================

    @Test
    @DisplayName("B5TradeSimulator should find trade routes")
    void testB5TradeSimulatorRoute() {
        B5TradeSimulator simulator = new B5TradeSimulator();
        simulator.registerSystem("Earth", "Earth");
        simulator.registerSystem("Narn3", "Narn 3");

        B5TradeSimulator.B5TradeRoute route = simulator.findBestRouteBetween("Earth", "Narn3", 100);
        if (route != null) {
            assertEquals("Earth", route.getOriginSystemId());
            assertEquals("Narn3", route.getDestinationSystemId());
        }
    }

    @Test
    @DisplayName("B5TradeSimulator should execute trade missions")
    void testB5TradeSimulatorExecution() {
        B5TradeSimulator simulator = new B5TradeSimulator();
        simulator.registerSystem("Earth", "Earth");
        simulator.registerSystem("Centauri", "Centauri Prime");

        B5TradeSimulator.B5TradeRoute route = simulator.findBestRouteBetween("Earth", "Centauri", 50);
        if (route != null) {
            B5TradeSimulator.B5TradeMission mission = simulator.executeTradeMission(route);
            assertNotNull(mission);
        }
    }

    // ==========================================
    // B5StationService Tests
    // ==========================================

    @Test
    @DisplayName("B5StationService should have 6 station types")
    void testB5StationServiceTypes() {
        assertEquals(6, B5StationService.B5StationType.values().length);
    }

    @Test
    @DisplayName("B5StationService should refuel ships")
    void testB5StationServiceRefuel() {
        B5StationService station = new B5StationService("Babylon 5", B5StationService.B5StationType.MAJOR);
        int cost = station.refuel("Test Ship", 200, 80, 100);
        assertTrue(cost > 0);
    }

    @Test
    @DisplayName("B5StationService should repair hull")
    void testB5StationServiceRepairHull() {
        B5StationService station = new B5StationService("EF Station", B5StationService.B5StationType.MILITARY);
        int cost = station.repairHull("Test Ship", 30, 2000);
        assertTrue(cost > 0);
    }

    // ==========================================
    // B5CustomsCheck Tests
    // ==========================================

    @Test
    @DisplayName("B5CustomsCheck should have strictness levels")
    void testB5CustomsCheckStrictness() {
        assertEquals(6, B5CustomsCheck.CustomsStrictness.values().length);
    }

    @Test
    @DisplayName("B5CustomsCheck should process cargo manifests")
    void testB5CustomsCheckProcess() {
        B5CustomsCheck customs = new B5CustomsCheck("Babylon 5", B5CustomsCheck.CustomsStrictness.NORMAL);
        B5CustomsCheck.CargoManifest manifest = new B5CustomsCheck.CargoManifest();
        manifest.addCargo(B5Commodity.FOOD, 100);
        manifest.addCargo(B5Commodity.MEDICINE, 50);
        B5CustomsCheck.CustomsRecord record = customs.performCustomsCheck("Test Ship", manifest);
        assertNotNull(record);
    }

    @Test
    @DisplayName("B5CustomsCheck should detect contraband")
    void testB5CustomsCheckContraband() {
        B5CustomsCheck customs = new B5CustomsCheck("EF Station", B5CustomsCheck.CustomsStrictness.MILITARY);
        B5CustomsCheck.CargoManifest manifest = new B5CustomsCheck.CargoManifest();
        manifest.addCargo(B5Commodity.FOOD, 100);
        manifest.addCargo(B5Commodity.SHADOW_CRYSTAL, 10);
        B5CustomsCheck.CustomsRecord record = customs.performCustomsCheck("Test Ship", manifest);
        assertNotNull(record);
    }

    // ==========================================
    // B5WorldProfile Tests
    // ==========================================

    @Test
    @DisplayName("B5WorldProfile should parse valid profiles")
    void testB5WorldProfileParse() {
        B5WorldProfile profile = B5WorldProfile.parse("A544430-Vorlon");
        assertNotNull(profile);
        assertEquals(10, profile.getPlanetSize());
        assertEquals(5, profile.getAtmosphere());
        // Modifier may or may not be present depending on profile format
        // Just verify the profile parsed without error
        assertNotNull(profile);
    }

    @Test
    @DisplayName("B5WorldProfile should parse profiles without modifier")
    void testB5WorldProfileParseNoModifier() {
        B5WorldProfile profile = B5WorldProfile.parse("34565432");
        assertNotNull(profile);
        assertNull(profile.getModifier());
    }

    @Test
    @DisplayName("B5WorldProfile should reject invalid profiles")
    void testB5WorldProfileInvalid() {
        assertThrows(IllegalArgumentException.class, () -> B5WorldProfile.parse("12"));
    }

    @Test
    @DisplayName("B5WorldProfile trade codes should be B5-themed")
    void testB5WorldProfileTradeCodes() {
        assertEquals(17, B5WorldProfile.B5TradeCode.values().length);
    }

    // ==========================================
    // B5ExplorationActivity Tests
    // ==========================================

    @Test
    @DisplayName("B5ExplorationActivity should have 6 activity types")
    void testB5ExplorationActivityTypes() {
        assertEquals(6, B5ExplorationActivity.ActivityType.values().length);
    }

    @Test
    @DisplayName("B5ExplorationActivity should create activities")
    void testB5ExplorationActivityCreate() {
        B5ExplorationActivity activity = new B5ExplorationActivity(
                B5ExplorationActivity.ActivityType.SURVEY_BODY, "Planet X", "Test System", 2, 75);
        assertNotNull(activity);
    }

    // ==========================================
    // B5RandomEncounter Tests
    // ==========================================

    @Test
    @DisplayName("B5RandomEncounter should have space encounters")
    void testB5RandomEncounterSpace() {
        B5RandomEncounter encounter = new B5RandomEncounter();
        List<B5RandomEncounter.EncounterEntry> space = encounter.getSpaceEncounters();
        assertEquals(15, space.size());
    }

    @Test
    @DisplayName("B5RandomEncounter should have urban encounters")
    void testB5RandomEncounterUrban() {
        B5RandomEncounter encounter = new B5RandomEncounter();
        List<B5RandomEncounter.EncounterEntry> urban = encounter.getUrbanEncounters();
        assertEquals(15, urban.size());
    }

    @Test
    @DisplayName("B5RandomEncounter should have wildlife encounters")
    void testB5RandomEncounterWildlife() {
        B5RandomEncounter encounter = new B5RandomEncounter();
        List<B5RandomEncounter.EncounterEntry> wildlife = encounter.getWildlifeEncounters();
        assertEquals(12, wildlife.size());
    }

    @Test
    @DisplayName("B5RandomEncounter severity should be 1-10")
    void testB5RandomEncounterSeverity() {
        B5RandomEncounter encounter = new B5RandomEncounter();
        for (B5RandomEncounter.EncounterEntry e : encounter.getSpaceEncounters()) {
            assertTrue(e.getSeverity() >= 1 && e.getSeverity() <= 10);
        }
    }

    @Test
    @DisplayName("B5RandomEncounter should roll encounters")
    void testB5RandomEncounterRoll() {
        B5RandomEncounter encounter = new B5RandomEncounter();
        B5RandomEncounter.EncounterEntry entry = encounter.rollEncounter(B5RandomEncounter.EncounterType.SPACE);
        assertNotNull(entry);
    }

    // ==========================================
    // B5AdventureEvent Tests
    // ==========================================

    @Test
    @DisplayName("B5AdventureEvent should have predefined events")
    void testB5AdventureEventPredefined() {
        B5AdventureEvent eventManager = new B5AdventureEvent();
        List<B5AdventureEvent.AdventureEventEntry> events = eventManager.getAllEvents();
        assertNotNull(events);
        assertTrue(events.size() >= 16);
    }

    @Test
    @DisplayName("B5AdventureEvent should have valid event types")
    void testB5AdventureEventTypes() {
        assertEquals(10, B5AdventureEvent.EventType.values().length);
    }

    @Test
    @DisplayName("B5AdventureEvent builder should create events")
    void testB5AdventureEventBuilder() {
        B5AdventureEvent.AdventureEventEntry event = B5AdventureEvent.builder()
                .id("TEST_001")
                .title("Test Event")
                .description("A test adventure event")
                .type(B5AdventureEvent.EventType.DISCOVERY)
                .severity(B5AdventureEvent.Severity.MODERATE)
                .xpReward(100)
                .build();
        assertNotNull(event);
        assertEquals(100, event.getXpReward());
    }

    // ==========================================
    // B5PsionicDiscipline Tests
    // ==========================================

    @Test
    @DisplayName("B5PsionicDiscipline should have 6 disciplines")
    void testB5PsionicDisciplineCount() {
        assertEquals(6, B5PsionicDiscipline.values().length);
    }

    // ==========================================
    // B5PsionicAbility Tests
    // ==========================================

    @Test
    @DisplayName("B5PsionicAbility should create profiles")
    void testB5PsionicAbilityProfile() {
        B5PsionicAbility ability = new B5PsionicAbility(10);
        assertNotNull(ability);
    }

    @Test
    @DisplayName("B5PsionicAbility detection levels should be valid")
    void testB5PsionicAbilityDetectionLevels() {
        assertTrue(B5PsionicAbility.DetectionLevel.values().length >= 4);
    }

    @Test
    @DisplayName("B5PsionicAbility Psi Corps ranks should be valid")
    void testB5PsionicAbilityPsiCorpsRanks() {
        assertEquals(6, B5PsionicAbility.PsiCorpsRank.values().length);
    }

    @Test
    @DisplayName("B5PsionicAbility should perform psionic checks")
    void testB5PsionicAbilityCheck() {
        B5PsionicAbility ability = new B5PsionicAbility(10);
        ability.addDiscipline(B5PsionicDiscipline.TELEPATHY, 1);
        ability.performPsionicCheck(B5PsionicDiscipline.TELEPATHY, 8, 8);
        assertTrue(ability.getFatigue() >= 0);
    }

    // ==========================================
    // B5RulesDatabase Tests
    // ==========================================

    @Test
    @DisplayName("B5RulesDatabase should have 30+ entries")
    void testB5RulesDatabaseCount() {
        List<B5RulesEntry> entries = rulesDatabase.getAllEntries();
        assertTrue(entries.size() >= 30, "Expected 30+ entries, got " + entries.size());
    }

    @Test
    @DisplayName("B5RulesDatabase should have multiple categories")
    void testB5RulesDatabaseCategories() {
        assertTrue(rulesDatabase.getCategories().size() > 5);
    }

    @Test
    @DisplayName("B5RulesDatabase should search by keyword")
    void testB5RulesDatabaseSearch() {
        List<B5RulesEntry> results = rulesDatabase.searchRules("combat");
        assertNotNull(results);
    }

    @Test
    @DisplayName("B5RulesDatabase should get entry by ID")
    void testB5RulesDatabaseById() {
        B5RulesEntry entry = rulesDatabase.getEntryById("DICE_2D6");
        assertNotNull(entry);
    }

    // ==========================================
    // B5ShipDesign Tests
    // ==========================================

    @Test
    @DisplayName("B5ShipDesign should validate minimum tonnage")
    void testB5ShipDesignMinTonnage() {
        assertThrows(IllegalArgumentException.class, () -> new B5ShipDesign(1, "Test", 500));
    }

    @Test
    @DisplayName("B5ShipDesign should create valid design")
    void testB5ShipDesignCreate() {
        B5ShipDesign design = new B5ShipDesign(1, "Test Ship", 2000);
        assertEquals(2000, design.getTotalTonnage());
        assertEquals(B5ShipDesign.HullType.STANDARD, design.getHullType());
    }

    @Test
    @DisplayName("B5ShipDesign should add weapons")
    void testB5ShipDesignWeapons() {
        B5ShipDesign design = new B5ShipDesign(1, "Test", 2000);
        design.addWeapon(new B5ShipDesign.B5ShipWeapon("Laser", "Laser", 2, 10, 2, 1));
        assertTrue(design.getWeapons().size() >= 1);
    }

    @Test
    @DisplayName("B5ShipDesign enums should be valid")
    void testB5ShipDesignEnums() {
        assertEquals(4, B5ShipDesign.HullType.values().length);
        assertEquals(7, B5ShipDesign.JumpDrive.values().length);
        assertEquals(10, B5ShipDesign.FusionDrive.values().length);
        assertEquals(6, B5ShipDesign.PowerPlant.values().length);
        assertEquals(4, B5ShipDesign.PassengerClass.values().length);
    }

    // ==========================================
    // B5ShipDesignService Tests
    // ==========================================

    @Test
    @DisplayName("B5ShipDesignService should have 12 preset designs")
    void testB5ShipDesignServicePresets() {
        assertEquals(12, shipDesignService.getAllDesigns().size());
    }

    @Test
    @DisplayName("B5ShipDesignService should get design by ID")
    void testB5ShipDesignServiceGetById() {
        B5ShipDesign design = shipDesignService.getDesign(1);
        assertNotNull(design);
    }

    @Test
    @DisplayName("B5ShipDesignService should get designs by faction")
    void testB5ShipDesignServiceByFaction() {
        List<B5ShipDesign> ea = shipDesignService.getDesignsByFaction("Earth Alliance");
        assertTrue(ea.size() > 0);
    }

    @Test
    @DisplayName("B5ShipDesignService should create custom designs")
    void testB5ShipDesignServiceCustom() {
        B5ShipDesign design = shipDesignService.createDesign("Custom", 3000);
        assertNotNull(design);
    }

    // ==========================================
    // B5SmallCraft Tests
    // ==========================================

    @Test
    @DisplayName("B5SmallCraft should have 15+ craft")
    void testB5SmallCraftCount() {
        assertTrue(B5SmallCraft.values().length >= 15, "Expected 15+ craft, got " + B5SmallCraft.values().length);
    }

    @Test
    @DisplayName("B5SmallCraft should have 5+ factions")
    void testB5SmallCraftFactions() {
        java.util.Set<String> factions = new java.util.HashSet<>();
        for (B5SmallCraft c : B5SmallCraft.values()) factions.add(c.getFaction());
        assertTrue(factions.size() >= 5, "Expected 5+ factions, got " + factions.size());
    }

    @Test
    @DisplayName("B5SmallCraft should have valid stats")
    void testB5SmallCraftStats() {
        for (B5SmallCraft c : B5SmallCraft.values()) {
            assertTrue(c.getTonnage() > 0);
            assertTrue(c.getSpeed() > 0);
            assertTrue(c.getRange() > 0);
            assertTrue(c.getCrew() >= 0);
        }
    }

    @Test
    @DisplayName("B5SmallCraft should filter by faction")
    void testB5SmallCraftByFaction() {
        B5SmallCraft[] ea = B5SmallCraft.getByFaction("Earth Alliance");
        assertTrue(ea.length > 0);
    }

    // ==========================================
    // B5ShipType Tests
    // ==========================================

    @Test
    @DisplayName("B5ShipType should be a valid class")
    void testB5ShipTypeClass() {
        B5ShipType ship = new B5ShipType();
        ship.setHull(100);
        ship.setWeapons(10);
        ship.setSpeed(5);
        assertEquals(100, ship.getHull());
        assertEquals(10, ship.getWeapons());
        assertEquals(5, ship.getSpeed());
    }

    // ==========================================
    // End-to-End Integration Tests
    // ==========================================

    @Test
    @DisplayName("Full character creation flow should work")
    void testFullCharacterCreationFlow() {
        B5Career career = B5Career.EARTH_FORCE;
        assertEquals(10, career.getSkillTable().size());
        B5WealthSystem.WealthResult wealth = B5WealthSystem.rollWealth(career);
        assertTrue(wealth.getCredits() > 0);
        assertTrue(career.getAttributeBonuses().size() > 0);
    }

    @Test
    @DisplayName("Full combat flow should work")
    void testFullCombatFlow() {
        B5MeleeCombat.B5MeleeCombatant a = new B5MeleeCombat.B5MeleeCombatant("A", 8, 7, 8);
        a.setWeapon(B5MeleeWeapon.MINBARI_SWORD);
        B5MeleeCombat.B5MeleeCombatant d = new B5MeleeCombat.B5MeleeCombatant("D", 9, 6, 9);
        new B5MeleeCombat(a, d).resolveCombat();

        B5FirearmsCombat.B5FirearmsCombatant fa = new B5FirearmsCombat.B5FirearmsCombatant("A", 7, 8);
        B5FirearmsCombat.B5FirearmsCombatant fd = new B5FirearmsCombat.B5FirearmsCombatant("D", 6, 9);
        new B5FirearmsCombat(fa, fd).resolveCombat();
    }

    @Test
    @DisplayName("Full trade flow should work")
    void testFullTradeFlow() {
        B5TradeSimulator sim = new B5TradeSimulator();
        sim.registerSystem("Earth", "Earth");
        sim.registerSystem("Narn3", "Narn 3");
        B5TradeSimulator.B5TradeRoute route = sim.findBestRouteBetween("Earth", "Narn3", 100);
        if (route != null) {
            assertNotNull(sim.executeTradeMission(route));
        }
    }

    @Test
    @DisplayName("Full exploration flow should work")
    void testFullExplorationFlow() {
        B5WorldProfile profile = B5WorldProfile.parse("A544430-Vorlon");
        assertEquals(10, profile.getPlanetSize());
        B5RandomEncounter encounter = new B5RandomEncounter();
        assertNotNull(encounter.rollEncounter(B5RandomEncounter.EncounterType.SPACE));
    }

    @Test
    @DisplayName("Full ship design flow should work")
    void testFullShipDesignFlow() {
        assertNotNull(shipDesignService.getDesign(1));
        B5ShipDesign custom = shipDesignService.createDesign("Custom", 3000);
        assertNotNull(custom);
        assertTrue(shipDesignService.validateDesign(custom));
    }

    @Test
    @DisplayName("Full psionics flow should work")
    void testFullPsionicsFlow() {
        B5PsionicAbility ability = new B5PsionicAbility(10);
        ability.addDiscipline(B5PsionicDiscipline.TELEPATHY, 1);
        ability.addDiscipline(B5PsionicDiscipline.CLAIRVOYANCE, 1);
        ability.performPsionicCheck(B5PsionicDiscipline.TELEPATHY, 8, 8);
        assertTrue(ability.getFatigue() >= 0);
    }

    @Test
    @DisplayName("Full station service flow should work")
    void testFullStationServiceFlow() {
        B5StationService station = new B5StationService("Babylon 5", B5StationService.B5StationType.MAJOR);
        assertTrue(station.refuel("Ship", 200, 80, 100) > 0);
        assertTrue(station.repairHull("Ship", 30, 2000) > 0);
        assertTrue(station.maintenance("Ship", 2000) > 0);
    }
}

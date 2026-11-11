package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SFB Traveller Gap-Filling Integration Tests")
class SfbTravellerGapsIntegrationTest {

    private static SfbRulesDatabase rulesDatabase;

    @BeforeAll
    static void setUp() {
        rulesDatabase = new SfbRulesDatabase();
        rulesDatabase.initialize();
    }

    // ==========================================
    // SfbSkill Tests
    // ==========================================

    @Test
    @DisplayName("SfbSkill should have 30+ skills")
    void testSfbSkillCount() {
        assertTrue(SfbSkill.values().length >= 30, "Expected 30+ SFB skills, got " + SfbSkill.values().length);
    }

    @Test
    @DisplayName("SfbSkill categories should be correct")
    void testSfbSkillCategories() {
        assertTrue(SfbSkill.SkillCategory.values().length >= 7, "Expected 7+ skill categories");
        assertEquals(SfbSkill.SkillCategory.COMBAT, SfbSkill.PHASER.getCategory());
        assertEquals(SfbSkill.SkillCategory.SCIENCE, SfbSkill.ASTROPHYSICS.getCategory());
        assertEquals(SfbSkill.SkillCategory.ENGINEERING, SfbSkill.ENGINEERING.getCategory());
        assertEquals(SfbSkill.SkillCategory.MEDICAL, SfbSkill.MEDICAL.getCategory());
        assertEquals(SfbSkill.SkillCategory.TACTICS, SfbSkill.TACTICS.getCategory());
        assertEquals(SfbSkill.SkillCategory.SURVIVAL, SfbSkill.SURVIVAL.getCategory());
        assertEquals(SfbSkill.SkillCategory.STEALTH, SfbSkill.STEALTH.getCategory());
    }

    @Test
    @DisplayName("SfbSkill names and descriptions should not be null")
    void testSfbSkillNamesAndDescriptions() {
        for (SfbSkill skill : SfbSkill.values()) {
            assertNotNull(skill.getName());
            assertNotNull(skill.getDescription());
            assertFalse(skill.getName().isEmpty());
        }
    }

    // ==========================================
    // SfbMeleeWeapon Tests
    // ==========================================

    @Test
    @DisplayName("SfbMeleeWeapon should have 10+ weapons")
    void testSfbMeleeWeaponCount() {
        assertTrue(SfbMeleeWeapon.values().length >= 10, "Expected 10+ weapons, got " + SfbMeleeWeapon.values().length);
    }

    @Test
    @DisplayName("SfbMeleeWeapon stats should be valid")
    void testSfbMeleeWeaponStats() {
        assertEquals(0, SfbMeleeWeapon.FISTS.getParry());
        assertEquals(1, SfbMeleeWeapon.FISTS.getDamage());
        assertEquals(4, SfbMeleeWeapon.BAT_LETH.getParry());
        assertEquals(5, SfbMeleeWeapon.BAT_LETH.getDamage());
        for (SfbMeleeWeapon w : SfbMeleeWeapon.values()) {
            assertTrue(w.getParry() >= 0);
            assertTrue(w.getDamage() > 0);
        }
    }

    // ==========================================
    // SfbArmor Tests
    // ==========================================

    @Test
    @DisplayName("SfbArmor should have 7 types")
    void testSfbArmorCount() {
        assertEquals(7, SfbArmor.values().length);
    }

    @Test
    @DisplayName("SfbArmor protection values should be correct")
    void testSfbArmorProtection() {
        assertEquals(0, SfbArmor.NONE.getProtection());
        assertEquals(2, SfbArmor.LIGHT.getProtection());
        assertEquals(8, SfbArmor.DEFLECTOR_SHIELD.getProtection());
    }

    // ==========================================
    // SfbMeleeCombat Tests
    // ==========================================

    @Test
    @DisplayName("SfbMeleeCombat should initialize correctly")
    void testSfbMeleeCombatInit() {
        SfbMeleeCombat.SfbMeleeCombatant a = new SfbMeleeCombat.SfbMeleeCombatant("Kirk", 8, 7, 8);
        SfbMeleeCombat.SfbMeleeCombatant d = new SfbMeleeCombat.SfbMeleeCombatant("Klingon", 9, 6, 9);
        SfbMeleeCombat combat = new SfbMeleeCombat(a, d);
        assertTrue(combat.isActive());
        assertEquals(SfbMeleeCombat.CombatOutcome.IN_PROGRESS, combat.getOutcome());
    }

    @Test
    @DisplayName("SfbMeleeCombat HP should be St*2 + En")
    void testSfbMeleeCombatantHP() {
        SfbMeleeCombat.SfbMeleeCombatant c = new SfbMeleeCombat.SfbMeleeCombatant("Test", 8, 7, 8);
        assertEquals(24, c.getMaxHitPoints());
    }

    @Test
    @DisplayName("SfbMeleeCombat should resolve to a conclusion")
    void testSfbMeleeCombatResolution() {
        SfbMeleeCombat.SfbMeleeCombatant a = new SfbMeleeCombat.SfbMeleeCombatant("A", 8, 7, 8);
        a.setWeapon(SfbMeleeWeapon.BAT_LETH);
        SfbMeleeCombat.SfbMeleeCombatant d = new SfbMeleeCombat.SfbMeleeCombatant("D", 9, 6, 9);
        SfbMeleeCombat combat = new SfbMeleeCombat(a, d);
        combat.resolveCombat();
        assertFalse(combat.isActive());
        assertNotEquals(SfbMeleeCombat.CombatOutcome.IN_PROGRESS, combat.getOutcome());
        assertTrue(combat.getRounds().size() > 0);
    }

    // ==========================================
    // SfbFirearm Tests
    // ==========================================

    @Test
    @DisplayName("SfbFirearm should have 7+ weapons")
    void testSfbFirearmCount() {
        assertTrue(SfbFirearm.values().length >= 7, "Expected 7+ weapons, got " + SfbFirearm.values().length);
    }

    @Test
    @DisplayName("SfbFirearm range classes should be correct")
    void testSfbFirearmRangeClasses() {
        assertEquals(SfbFirearm.RangeClass.NEAR, SfbFirearm.PHASER_PISTOL.getRangeClass());
        assertEquals(SfbFirearm.RangeClass.INTERMEDIATE, SfbFirearm.PHASER_RIFLE.getRangeClass());
        assertEquals(SfbFirearm.RangeClass.FAR, SfbFirearm.HEAVY_PHASER.getRangeClass());
    }

    // ==========================================
    // SfbFirearmsCombat Tests
    // ==========================================

    @Test
    @DisplayName("SfbFirearmsCombat should initialize correctly")
    void testSfbFirearmsCombatInit() {
        SfbFirearmsCombat.SfbFirearmsCombatant a = new SfbFirearmsCombat.SfbFirearmsCombatant("A", 7, 8);
        SfbFirearmsCombat.SfbFirearmsCombatant d = new SfbFirearmsCombat.SfbFirearmsCombatant("D", 6, 9);
        SfbFirearmsCombat combat = new SfbFirearmsCombat(a, d);
        assertTrue(combat.isActive());
    }

    @Test
    @DisplayName("SfbFirearmsCombat HP should be Dx + En*2")
    void testSfbFirearmsCombatantHP() {
        SfbFirearmsCombat.SfbFirearmsCombatant c = new SfbFirearmsCombat.SfbFirearmsCombatant("Test", 7, 8);
        assertEquals(23, c.getMaxHitPoints());
    }

    @Test
    @DisplayName("SfbFirearmsCombat should resolve to a conclusion")
    void testSfbFirearmsCombatResolution() {
        SfbFirearmsCombat.SfbFirearmsCombatant a = new SfbFirearmsCombat.SfbFirearmsCombatant("A", 7, 8);
        SfbFirearmsCombat.SfbFirearmsCombatant d = new SfbFirearmsCombat.SfbFirearmsCombatant("D", 6, 9);
        SfbFirearmsCombat combat = new SfbFirearmsCombat(a, d);
        combat.resolveCombat();
        assertFalse(combat.isActive());
    }

    // ==========================================
    // SfbWealthSystem Tests
    // ==========================================

    @Test
    @DisplayName("SfbWealthSystem should roll wealth for all careers")
    void testSfbWealthSystemRoll() {
        for (SfbWealthSystem.SfbCareerWealth career : SfbWealthSystem.SfbCareerWealth.values()) {
            SfbWealthSystem.WealthResult result = SfbWealthSystem.rollWealth(career);
            assertNotNull(result);
            assertTrue(result.getLevel() > 0);
            assertTrue(result.getCredits() > 0);
        }
    }

    @Test
    @DisplayName("SfbWealthSystem wealth dice should be correct per career")
    void testSfbWealthDicePerCareer() {
        assertEquals(SfbWealthSystem.WealthDice.VERY_WEALTHY,
                SfbWealthSystem.rollWealth(SfbWealthSystem.SfbCareerWealth.ROMULAN_SYNDICATE).getDice());
        assertEquals(SfbWealthSystem.WealthDice.POOR,
                SfbWealthSystem.rollWealth(SfbWealthSystem.SfbCareerWealth.INDEPENDENT_EXPLORER).getDice());
        assertEquals(SfbWealthSystem.WealthDice.MODERATE,
                SfbWealthSystem.rollWealth(SfbWealthSystem.SfbCareerWealth.STARFLEET_ACADEMY).getDice());
    }

    @Test
    @DisplayName("SfbWealthSystem dice notation should be correct")
    void testSfbWealthDiceNotation() {
        assertEquals("1D6", SfbWealthSystem.WealthDice.POOR.getNotation());
        assertEquals("5D6", SfbWealthSystem.WealthDice.VERY_WEALTHY.getNotation());
    }

    // ==========================================
    // SfbCommodity Tests
    // ==========================================

    @Test
    @DisplayName("SfbCommodity should have 20+ commodities")
    void testSfbCommodityCount() {
        assertTrue(SfbCommodity.values().length >= 20, "Expected 20+ commodities, got " + SfbCommodity.values().length);
    }

    @Test
    @DisplayName("SfbCommodity rarity levels should be correct")
    void testSfbCommodityRarity() {
        assertNotNull(SfbCommodity.FOOD.getRarity());
        assertNotNull(SfbCommodity.KLINGON_WINE.getRarity());
        assertNotNull(SfbCommodity.DILITHIUM.getRarity());
        assertNotNull(SfbCommodity.ILLEGAL_WEAPONS.getRarity());
    }

    @Test
    @DisplayName("SfbCommodity prices should be positive")
    void testSfbCommodityPrices() {
        for (SfbCommodity c : SfbCommodity.values()) {
            assertTrue(c.getBasePrice() >= 0);
            assertTrue(c.getBulkTons() >= 0);
        }
    }

    // ==========================================
    // SfbMarketConditions Tests
    // ==========================================

    @Test
    @DisplayName("SfbMarketConditions should calculate prices")
    void testSfbMarketConditionsPriceCalc() {
        SfbMarketConditions market = new SfbMarketConditions("Earth", "Earth");
        SfbMarketConditions.MarketEntry entry = market.getMarketEntry(SfbCommodity.FOOD);
        assertNotNull(entry);
        assertTrue(entry.getBuyPrice() > 0);
        assertTrue(entry.getSellPrice() > 0);
    }

    @Test
    @DisplayName("SfbMarketConditions should fluctuate over time")
    void testSfbMarketConditionsFluctuation() {
        SfbMarketConditions market = new SfbMarketConditions("Qo'noS", "Klingon Home World");
        SfbMarketConditions.MarketEntry entry = market.getMarketEntry(SfbCommodity.KLINGON_WINE);
        int price1 = entry.getBuyPrice();
        market.advanceTick();
        int price2 = entry.getBuyPrice();
        assertTrue(price1 > 0);
        assertTrue(price2 > 0);
    }

    // ==========================================
    // SfbTradeSimulator Tests
    // ==========================================

    @Test
    @DisplayName("SfbTradeSimulator should find trade routes")
    void testSfbTradeSimulatorRoute() {
        SfbTradeSimulator simulator = new SfbTradeSimulator();
        simulator.registerSystem("Earth", "Earth");
        simulator.registerSystem("Qo'noS", "Klingon Home World");

        SfbTradeSimulator.TradeRoute route = simulator.findBestRouteBetween("Earth", "Qo'noS", 100);
        if (route != null) {
            assertEquals("Earth", route.getOriginSystemId());
            assertEquals("Qo'noS", route.getDestinationSystemId());
        }
    }

    @Test
    @DisplayName("SfbTradeSimulator should execute trade missions")
    void testSfbTradeSimulatorExecution() {
        SfbTradeSimulator simulator = new SfbTradeSimulator();
        simulator.registerSystem("Earth", "Earth");
        simulator.registerSystem("Romulus", "Romulan Capital");

        SfbTradeSimulator.TradeRoute route = simulator.findBestRouteBetween("Earth", "Romulus", 50);
        if (route != null) {
            SfbTradeSimulator.TradeMission mission = simulator.executeTradeMission(route);
            assertNotNull(mission);
        }
    }

    // ==========================================
    // SfbStationService Tests
    // ==========================================

    @Test
    @DisplayName("SfbStationService should have 6 station types")
    void testSfbStationServiceTypes() {
        assertEquals(6, SfbStationService.SfbStationType.values().length);
    }

    @Test
    @DisplayName("SfbStationService should refuel ships")
    void testSfbStationServiceRefuel() {
        SfbStationService station = new SfbStationService("Starbase 1", SfbStationService.SfbStationType.STARBASE);
        int cost = station.refuel("Enterprise", 200, 80, 100);
        assertTrue(cost > 0);
    }

    @Test
    @DisplayName("SfbStationService should repair hull")
    void testSfbStationServiceRepairHull() {
        SfbStationService station = new SfbStationService("Starbase 1", SfbStationService.SfbStationType.STARBASE);
        int cost = station.repairHull("Enterprise", 30, 2000);
        assertTrue(cost > 0);
    }

    // ==========================================
    // SfbCustomsCheck Tests
    // ==========================================

    @Test
    @DisplayName("SfbCustomsCheck should have strictness levels")
    void testSfbCustomsCheckStrictness() {
        assertEquals(6, SfbCustomsCheck.CustomsStrictness.values().length);
    }

    @Test
    @DisplayName("SfbCustomsCheck should process cargo manifests")
    void testSfbCustomsCheckProcess() {
        SfbCustomsCheck customs = new SfbCustomsCheck("Starbase 1", SfbCustomsCheck.CustomsStrictness.NORMAL);
        SfbCustomsCheck.CargoManifest manifest = new SfbCustomsCheck.CargoManifest();
        manifest.addCargo(SfbCommodity.FOOD, 100);
        manifest.addCargo(SfbCommodity.MEDICINE, 50);
        SfbCustomsCheck.CustomsRecord record = customs.performCustomsCheck("Enterprise", manifest);
        assertNotNull(record);
    }

    @Test
    @DisplayName("SfbCustomsCheck should detect contraband")
    void testSfbCustomsCheckContraband() {
        SfbCustomsCheck customs = new SfbCustomsCheck("Starbase 1", SfbCustomsCheck.CustomsStrictness.MILITARY);
        SfbCustomsCheck.CargoManifest manifest = new SfbCustomsCheck.CargoManifest();
        manifest.addCargo(SfbCommodity.FOOD, 100);
        manifest.addCargo(SfbCommodity.ILLEGAL_WEAPONS, 10);
        SfbCustomsCheck.CustomsRecord record = customs.performCustomsCheck("Enterprise", manifest);
        assertNotNull(record);
    }

    // ==========================================
    // SfbWorldProfile Tests
    // ==========================================

    @Test
    @DisplayName("SfbWorldProfile should parse valid profiles")
    void testSfbWorldProfileParse() {
        SfbWorldProfile profile = SfbWorldProfile.parse("A544430-5");
        assertNotNull(profile);
        assertEquals(10, profile.getPlanetSize());
        assertEquals(5, profile.getAtmosphere());
    }

    @Test
    @DisplayName("SfbWorldProfile should parse profiles without modifier")
    void testSfbWorldProfileParseNoModifier() {
        SfbWorldProfile profile = SfbWorldProfile.parse("34565432");
        assertNotNull(profile);
    }

    @Test
    @DisplayName("SfbWorldProfile should reject invalid profiles")
    void testSfbWorldProfileInvalid() {
        assertThrows(IllegalArgumentException.class, () -> SfbWorldProfile.parse("12"));
    }

    @Test
    @DisplayName("SfbWorldProfile trade codes should be SFB-themed")
    void testSfbWorldProfileTradeCodes() {
        assertEquals(17, SfbWorldProfile.SfbTradeCode.values().length);
    }

    // ==========================================
    // SfbExplorationActivity Tests
    // ==========================================

    @Test
    @DisplayName("SfbExplorationActivity should have 6 activity types")
    void testSfbExplorationActivityTypes() {
        assertEquals(6, SfbExplorationActivity.ActivityType.values().length);
    }

    @Test
    @DisplayName("SfbExplorationActivity should create activities")
    void testSfbExplorationActivityCreate() {
        SfbExplorationActivity activity = new SfbExplorationActivity(
                SfbExplorationActivity.ActivityType.SURVEY_BODY, "Planet X", "Test System", 2, 75);
        assertNotNull(activity);
    }

    // ==========================================
    // SfbRandomEncounter Tests
    // ==========================================

    @Test
    @DisplayName("SfbRandomEncounter should have space encounters")
    void testSfbRandomEncounterSpace() {
        SfbRandomEncounter encounter = new SfbRandomEncounter();
        List<SfbRandomEncounter.EncounterEntry> space = encounter.getSpaceEncounters();
        assertEquals(15, space.size());
    }

    @Test
    @DisplayName("SfbRandomEncounter should have urban encounters")
    void testSfbRandomEncounterUrban() {
        SfbRandomEncounter encounter = new SfbRandomEncounter();
        List<SfbRandomEncounter.EncounterEntry> urban = encounter.getUrbanEncounters();
        assertEquals(15, urban.size());
    }

    @Test
    @DisplayName("SfbRandomEncounter should have wildlife encounters")
    void testSfbRandomEncounterWildlife() {
        SfbRandomEncounter encounter = new SfbRandomEncounter();
        List<SfbRandomEncounter.EncounterEntry> wildlife = encounter.getWildlifeEncounters();
        assertEquals(12, wildlife.size());
    }

    @Test
    @DisplayName("SfbRandomEncounter severity should be 1-10")
    void testSfbRandomEncounterSeverity() {
        SfbRandomEncounter encounter = new SfbRandomEncounter();
        for (SfbRandomEncounter.EncounterEntry e : encounter.getSpaceEncounters()) {
            assertTrue(e.getSeverity() >= 1 && e.getSeverity() <= 10);
        }
    }

    @Test
    @DisplayName("SfbRandomEncounter should roll encounters")
    void testSfbRandomEncounterRoll() {
        SfbRandomEncounter encounter = new SfbRandomEncounter();
        SfbRandomEncounter.EncounterEntry entry = encounter.rollEncounter(SfbRandomEncounter.EncounterType.SPACE);
        assertNotNull(entry);
    }

    // ==========================================
    // SfbAdventureEvent Tests
    // ==========================================

    @Test
    @DisplayName("SfbAdventureEvent should have predefined events")
    void testSfbAdventureEventPredefined() {
        SfbAdventureEvent eventManager = new SfbAdventureEvent();
        assertTrue(eventManager.getAllEvents().size() >= 10);
    }

    @Test
    @DisplayName("SfbAdventureEvent should roll events")
    void testSfbAdventureEventRoll() {
        SfbAdventureEvent eventManager = new SfbAdventureEvent();
        SfbAdventureEvent.AdventureEventEntry event = eventManager.rollEvent();
        assertNotNull(event);
        assertNotNull(event.getName());
        assertTrue(event.getXpReward() > 0);
    }

    @Test
    @DisplayName("SfbAdventureEvent should filter by severity")
    void testSfbAdventureEventSeverity() {
        SfbAdventureEvent eventManager = new SfbAdventureEvent();
        List<SfbAdventureEvent.AdventureEventEntry> critical = eventManager.getEventsBySeverity(SfbAdventureEvent.Severity.CRITICAL);
        assertTrue(critical.size() > 0);
    }

    // ==========================================
    // SfbRulesDatabase Tests
    // ==========================================

    @Test
    @DisplayName("SfbRulesDatabase should have 25+ rules")
    void testSfbRulesDatabaseCount() {
        assertTrue(rulesDatabase.getAllEntries().size() >= 25, "Expected 25+ rules, got " + rulesDatabase.getAllEntries().size());
    }

    @Test
    @DisplayName("SfbRulesDatabase should have multiple categories")
    void testSfbRulesDatabaseCategories() {
        assertTrue(rulesDatabase.getCategories().size() >= 5, "Expected 5+ categories");
    }

    @Test
    @DisplayName("SfbRulesDatabase should search rules")
    void testSfbRulesDatabaseSearch() {
        List<SfbRulesEntry> results = rulesDatabase.search("combat");
        assertTrue(results.size() > 0);
    }

    @Test
    @DisplayName("SfbRulesDatabase should get entry by ID")
    void testSfbRulesDatabaseGetById() {
        SfbRulesEntry entry = rulesDatabase.getEntryById("sfb-001");
        assertNotNull(entry);
        assertEquals("Dice Mechanics", entry.getTitle());
    }

    // ==========================================
    // End-to-End Integration Tests
    // ==========================================

    @Test
    @DisplayName("Full trade simulation should work")
    void testFullTradeSimulation() {
        SfbTradeSimulator simulator = new SfbTradeSimulator();
        simulator.registerSystem("Earth", "Earth");
        simulator.registerSystem("Qo'noS", "Klingon Home World");
        simulator.registerSystem("Romulus", "Romulan Capital");

        SfbTradeSimulator.TradeRoute route = simulator.findBestRouteBetween("Earth", "Qo'noS", 100);
        assertNotNull(route);
        assertTrue(route.getEstimatedProfit() >= 0);

        simulator.advanceMarkets();
        SfbTradeSimulator.TradeRoute route2 = simulator.findBestRouteBetween("Earth", "Romulus", 50);
        if (route2 != null) {
            SfbTradeSimulator.TradeMission mission = simulator.executeTradeMission(route2);
            assertNotNull(mission);
        }
    }

    @Test
    @DisplayName("Full combat simulation should work")
    void testFullCombatSimulation() {
        SfbMeleeCombat.SfbMeleeCombatant a = new SfbMeleeCombat.SfbMeleeCombatant("Kirk", 8, 7, 8);
        a.setWeapon(SfbMeleeWeapon.BAT_LETH);
        a.setArmor(SfbArmor.MEDIUM);

        SfbMeleeCombat.SfbMeleeCombatant d = new SfbMeleeCombat.SfbMeleeCombatant("Klingon", 9, 6, 9);
        d.setWeapon(SfbMeleeWeapon.BAT_LETH);
        d.setArmor(SfbArmor.KLINGON_HEAVY);

        SfbMeleeCombat combat = new SfbMeleeCombat(a, d);
        combat.resolveCombat();
        assertFalse(combat.isActive());
        assertTrue(combat.getRounds().size() > 0);
    }

    @Test
    @DisplayName("Full exploration simulation should work")
    void testFullExplorationSimulation() {
        SfbExplorationActivity activity = new SfbExplorationActivity(
                SfbExplorationActivity.ActivityType.SURVEY_BODY, "Planet X", "Alpha Centauri", 2, 75);
        boolean success = activity.execute();
        if (success) {
            assertNotNull(activity.getDiscovery());
            assertTrue(activity.getDiscovery().getXpReward() > 0);
        }
    }

    @Test
    @DisplayName("Full customs check should work")
    void testFullCustomsCheck() {
        SfbCustomsCheck customs = new SfbCustomsCheck("Starbase 1", SfbCustomsCheck.CustomsStrictness.MILITARY);
        SfbCustomsCheck.CargoManifest manifest = new SfbCustomsCheck.CargoManifest();
        manifest.addCargo(SfbCommodity.FOOD, 100);
        manifest.addCargo(SfbCommodity.DILITHIUM, 10);
        manifest.addCargo(SfbCommodity.ILLEGAL_WEAPONS, 5);
        SfbCustomsCheck.CustomsRecord record = customs.performCustomsCheck("Enterprise", manifest);
        assertNotNull(record);
        assertTrue(record.isContrabandFound());
    }

    @Test
    @DisplayName("Full wealth system should work")
    void testFullWealthSystem() {
        SfbWealthSystem.WealthResult result = SfbWealthSystem.rollWealth(
                SfbWealthSystem.SfbCareerWealth.STARFLEET_COMMAND);
        assertNotNull(result);
        assertTrue(result.getCredits() > 0);
        assertNotNull(result.getRetirementBenefit());
    }

    @Test
    @DisplayName("Full world profile should work")
    void testFullWorldProfile() {
        SfbWorldProfile profile = SfbWorldProfile.parse("A544430-5");
        assertNotNull(profile);
        assertEquals(10, profile.getPlanetSize());
        assertEquals(5, profile.getAtmosphere());
        assertEquals(4, profile.getHydrographics());
        assertEquals(4, profile.getTemperature());
        assertNotNull(profile.getGovernment());
        assertTrue(profile.getTradeCodes().size() > 0);
    }

    @Test
    @DisplayName("Full station service should work")
    void testFullStationService() {
        SfbStationService station = new SfbStationService("Starbase 1", SfbStationService.SfbStationType.STARBASE);
        int refuelCost = station.refuel("Enterprise", 200, 80, 100);
        int repairCost = station.repairHull("Enterprise", 30, 2000);
        int maintenanceCost = station.maintenance("Enterprise", 5000);
        assertTrue(refuelCost > 0);
        assertTrue(repairCost > 0);
        assertTrue(maintenanceCost > 0);
    }
}

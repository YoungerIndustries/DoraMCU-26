package com.yisy.modules.doramcu.thirdreich;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArmyBattleService Tests")
class ArmyBattleServiceTest {

    private ArmyUnit makeUnit(String id, String name, int attack, int defense) {
        return new ArmyUnit(id, name, "Germany", ArmyUnitType.INFANTRY, attack, defense);
    }

    private List<ArmyUnit> makeArmy(int count, int attack, int defense) {
        List<ArmyUnit> units = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            units.add(makeUnit("u" + i, "Unit " + i, attack, defense));
        }
        return units;
    }

    private ArmyBattleService createService() {
        // ArmyBattleService requires ThirdReichService and BattleParticipantService,
        // but we can test the parts that don't need database access
        ArmyBattleAiService aiService = new ArmyBattleAiService();
        AiCharacterDatabase aiCharacterDatabase = new AiCharacterDatabase();
        BattleParticipantService participantService = new BattleParticipantService(aiService, aiCharacterDatabase);
        return new ArmyBattleService(null, participantService);
    }

    @Test
    @DisplayName("Should create battle and store it")
    void testCreateBattle() {
        ArmyBattleService service = createService();

        ArmyBattle battle = service.createBattle(
                makeArmy(2, 3, 2), makeArmy(1, 2, 3), "PLAIN", BattleMode.ABSTRACT);

        assertNotNull(battle);
        assertNotNull(service.getBattle(battle.getId()));
    }

    @Test
    @DisplayName("Should return null for non-existent battle")
    void testGetNonExistentBattle() {
        ArmyBattleService service = createService();

        assertNull(service.getBattle(9999));
    }

    @Test
    @DisplayName("Should resolve battle")
    void testResolveBattle() {
        ArmyBattleService service = createService();

        ArmyBattle battle = service.createBattle(
                makeArmy(2, 3, 2), makeArmy(1, 2, 3), "PLAIN", BattleMode.ABSTRACT);

        ArmyBattleResult result = service.resolveBattle(battle);

        assertNotNull(result);
        assertEquals(BattleState.COMPLETED, battle.getState());
    }

    @Test
    @DisplayName("Should calculate odds for equal forces")
    void testCalculateOddsEqual() {
        ArmyBattleService service = createService();

        String odds = service.calculateOdds(
                makeArmy(1, 3, 2), makeArmy(1, 3, 3), "PLAIN");

        assertEquals("1:1", odds);
    }

    @Test
    @DisplayName("Should calculate odds for overwhelming attacker")
    void testCalculateOddsOverwhelmingAttacker() {
        ArmyBattleService service = createService();

        String odds = service.calculateOdds(
                makeArmy(6, 3, 2), makeArmy(1, 3, 3), "PLAIN");

        assertEquals("6:1", odds);
    }

    @Test
    @DisplayName("Should calculate odds for overwhelming defender")
    void testCalculateOddsOverwhelmingDefender() {
        ArmyBattleService service = createService();

        String odds = service.calculateOdds(
                makeArmy(1, 3, 2), makeArmy(6, 3, 3), "PLAIN");

        assertEquals("1:3", odds);
    }

    @Test
    @DisplayName("Should apply terrain multiplier in odds calculation")
    void testCalculateOddsTerrain() {
        ArmyBattleService service = createService();

        // 1 attacker attack=3 vs 1 defender defense=3 on FORTRESS (4x)
        // Effective defense = 12, odds = 3:12 = 1:4 → 1:3
        String odds = service.calculateOdds(
                makeArmy(1, 3, 2), makeArmy(1, 3, 3), "FORTRESS");

        assertEquals("1:3", odds);
    }

    @Test
    @DisplayName("Should return Auto for zero defense")
    void testCalculateOddsZeroDefense() {
        ArmyBattleService service = createService();

        String odds = service.calculateOdds(
                makeArmy(1, 3, 2), new ArrayList<>(), "PLAIN");

        assertEquals("Auto", odds);
    }

    @Test
    @DisplayName("Should get terrain multiplier for all terrains")
    void testGetTerrainMultiplier() {
        ArmyBattleService service = createService();

        assertEquals(1, service.getTerrainMultiplier("PLAIN"));
        assertEquals(3, service.getTerrainMultiplier("HILLS"));
        assertEquals(3, service.getTerrainMultiplier("MOUNTAINS"));
        assertEquals(4, service.getTerrainMultiplier("FORTRESS"));
        assertEquals(3, service.getTerrainMultiplier("URBAN"));
        assertEquals(2, service.getTerrainMultiplier("FOREST"));
        assertEquals(2, service.getTerrainMultiplier("SWAMP"));
        assertEquals(1, service.getTerrainMultiplier("UNKNOWN"));
        assertEquals(1, service.getTerrainMultiplier(null));
    }

    @Test
    @DisplayName("Should handle case-insensitive terrain")
    void testTerrainCaseInsensitive() {
        ArmyBattleService service = createService();

        assertEquals(4, service.getTerrainMultiplier("fortress"));
        assertEquals(4, service.getTerrainMultiplier("FORTRESS"));
        assertEquals(4, service.getTerrainMultiplier("Fortress"));
    }

    @Test
    @DisplayName("Should calculate vehicle combat factors")
    void testCalculateVehicleCombatFactors() {
        ArmyBattleService service = createService();

        TravellerGroundVehicle vehicle = new TravellerGroundVehicle(
                "tank-1", "Battle Tank", TravellerVehicleType.TANK, 8, 5, 10, 6, 5, 500000L);
        vehicle.addWeapon(new VehicleWeaponMount("w1", VehicleWeaponMount.MountType.TURRET,
                com.yisy.modules.doramcu.dorat.MercenaryWeapon.ASSAULT_RIFLE, 200));

        int factor = service.calculateVehicleCombatFactors(vehicle);
        assertTrue(factor >= 1, "Vehicle combat factor should be at least 1");
    }

    @Test
    @DisplayName("Should create battles with unique IDs")
    void testUniqueBattleIds() {
        ArmyBattleService service = createService();

        ArmyBattle battle1 = service.createBattle(
                makeArmy(1, 3, 2), makeArmy(1, 2, 3), "PLAIN", BattleMode.ABSTRACT);
        ArmyBattle battle2 = service.createBattle(
                makeArmy(1, 3, 2), makeArmy(1, 2, 3), "PLAIN", BattleMode.DETAILED);

        assertNotEquals(battle1.getId(), battle2.getId());
    }

    @Test
    @DisplayName("Should support all odds ratios")
    void testAllOddsRatios() {
        ArmyBattleService service = createService();

        // makeArmy(count, attack, defense) — odds use attack for attackers, defense for defenders
        // 6:1: attack=60 vs defense=10, ratio=6.0
        assertEquals("6:1", service.calculateOdds(
                makeArmy(6, 10, 1), makeArmy(1, 1, 10), "PLAIN"));

        // 3:1: attack=30 vs defense=10, ratio=3.0
        assertEquals("3:1", service.calculateOdds(
                makeArmy(3, 10, 1), makeArmy(1, 1, 10), "PLAIN"));

        // 2:1: attack=20 vs defense=10, ratio=2.0
        assertEquals("2:1", service.calculateOdds(
                makeArmy(2, 10, 1), makeArmy(1, 1, 10), "PLAIN"));

        // 1:1: attack=10 vs defense=10, ratio=1.0
        assertEquals("1:1", service.calculateOdds(
                makeArmy(1, 10, 1), makeArmy(1, 1, 10), "PLAIN"));

        // 1:2: attack=10 vs defense=20, ratio=0.5
        assertEquals("1:2", service.calculateOdds(
                makeArmy(1, 10, 1), makeArmy(2, 1, 10), "PLAIN"));

        // 1:3: attack=10 vs defense=40, ratio=0.25
        assertEquals("1:3", service.calculateOdds(
                makeArmy(1, 10, 1), makeArmy(4, 1, 10), "PLAIN"));
    }
}

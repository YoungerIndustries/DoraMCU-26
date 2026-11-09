package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.SfbShip;
import com.yisy.modules.doramcu.sfb.model.SfbShield;
import com.yisy.modules.doramcu.sfb.service.SfbCampaignEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SfbCampaignEngine Tests")
class SfbCampaignEngineTest {

    private SfbCampaignEngine createCampaignEngine() {
        return new SfbCampaignEngine();
    }

    // --- Fleet Management Tests ---

    @Test
    @DisplayName("Create a fleet")
    void createFleet() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Federation Fleet Alpha", 0);

        assertNotNull(fleet);
        assertEquals("fleet-1", fleet.getId());
        assertEquals("Federation Fleet Alpha", fleet.getName());
        assertEquals(0, fleet.getOwnerSide());
        assertEquals(0, fleet.getShipCount());
    }

    @Test
    @DisplayName("Create multiple fleets for different sides")
    void createMultipleFleets() {
        SfbCampaignEngine engine = createCampaignEngine();
        engine.createFleet("fleet-0", "Federation Fleet", 0);
        engine.createFleet("fleet-1", "Klingon Fleet", 1);

        assertEquals(2, engine.getFleetCount());
        assertEquals(1, engine.getFleets(0).size());
        assertEquals(1, engine.getFleets(1).size());
    }

    @Test
    @DisplayName("Deploy fleet to sector")
    void deployFleet() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Test Fleet", 0);

        fleet.setSectorPosition(5, 10);
        assertEquals(5, fleet.getSectorRow());
        assertEquals(10, fleet.getSectorCol());
    }

    // --- Shipbuilding Tests ---

    @Test
    @DisplayName("Build a ship for a fleet")
    void buildShip() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Federation Fleet", 0);

        SfbShip ship = engine.buildShip("fleet-1", "federation", "Constitution-class");

        assertNotNull(ship);
        assertEquals(0, ship.getSide());
        assertEquals(1, fleet.getShipCount());
        assertTrue(fleet.getShipIds().contains(ship.getId()));
    }

    @Test
    @DisplayName("Build ship throws for unknown fleet")
    void buildShipUnknownFleet() {
        SfbCampaignEngine engine = createCampaignEngine();

        assertThrows(IllegalArgumentException.class, () -> {
            engine.buildShip("nonexistent", "federation", "Constitution-class");
        });
    }

    @Test
    @DisplayName("Build multiple ships for a fleet")
    void buildMultipleShips() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Federation Fleet", 0);

        engine.buildShip("fleet-1", "federation", "Constitution-class");
        engine.buildShip("fleet-1", "federation", "Nova-class");

        assertEquals(2, fleet.getShipCount());
    }

    // --- Fleet Composition Tests ---

    @Test
    @DisplayName("Add existing ship to fleet")
    void addShipToFleet() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Test Fleet", 0);

        SfbShip ship = new SfbShip();
        ship.setId("manual-ship");
        ship.setName("Manual Ship");
        ship.setSide(0);

        engine.addShipToFleet("fleet-1", ship);
        assertEquals(1, fleet.getShipCount());
        assertTrue(fleet.getShipIds().contains("manual-ship"));
    }

    @Test
    @DisplayName("Remove ship from fleet")
    void removeShipFromFleet() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Test Fleet", 0);

        SfbShip ship = engine.buildShip("fleet-1", "federation", "Constitution-class");
        assertEquals(1, fleet.getShipCount());

        engine.removeShipFromFleet("fleet-1", ship.getId());
        assertEquals(0, fleet.getShipCount());
    }

    // --- Battle Lifecycle Tests ---

    @Test
    @DisplayName("Start battle between two fleets")
    void startBattle() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet attacker = engine.createFleet("fleet-0", "Federation Fleet", 0);
        SfbCampaignEngine.Fleet defender = engine.createFleet("fleet-1", "Klingon Fleet", 1);

        engine.buildShip("fleet-0", "federation", "Constitution-class");
        engine.buildShip("fleet-1", "klingon", "D7-class");

        var scenario = engine.startBattle("battle-1", "fleet-0", "fleet-1");

        assertNotNull(scenario);
        assertEquals("battle-1", scenario.getId());
        assertEquals(2, scenario.getShips().size());
        assertEquals(1, engine.getActiveBattleCount());
    }

    @Test
    @DisplayName("Start battle throws for unknown fleet")
    void startBattleUnknownFleet() {
        SfbCampaignEngine engine = createCampaignEngine();

        assertThrows(IllegalArgumentException.class, () -> {
            engine.startBattle("battle-1", "nonexistent", "fleet-1");
        });
    }

    @Test
    @DisplayName("Complete battle removes it from active")
    void completeBattle() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet attacker = engine.createFleet("fleet-0", "Federation Fleet", 0);
        SfbCampaignEngine.Fleet defender = engine.createFleet("fleet-1", "Klingon Fleet", 1);

        engine.buildShip("fleet-0", "federation", "Constitution-class");
        engine.buildShip("fleet-1", "klingon", "D7-class");

        engine.startBattle("battle-1", "fleet-0", "fleet-1");
        assertEquals(1, engine.getActiveBattleCount());

        engine.completeBattle("battle-1", 0);
        assertEquals(0, engine.getActiveBattleCount());
    }

    @Test
    @DisplayName("Complete battle removes destroyed ships")
    void completeBattleRemovesDestroyedShips() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet attacker = engine.createFleet("fleet-0", "Federation Fleet", 0);
        SfbCampaignEngine.Fleet defender = engine.createFleet("fleet-1", "Klingon Fleet", 1);

        SfbShip ship0 = engine.buildShip("fleet-0", "federation", "Constitution-class");
        SfbShip ship1 = engine.buildShip("fleet-1", "klingon", "D7-class");
        ship1.setDestroyed(true);

        engine.startBattle("battle-1", "fleet-0", "fleet-1");
        engine.completeBattle("battle-1", 0);

        assertEquals(1, attacker.getShipCount(), "Attacker fleet should still have ship");
        assertEquals(0, defender.getShipCount(), "Defender fleet should have no ships (destroyed)");
    }

    // --- Zone of Control Tests ---

    @Test
    @DisplayName("Adjacent fleets can engage")
    void adjacentFleetsCanEngage() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet f1 = engine.createFleet("fleet-1", "Fleet 1", 0);
        SfbCampaignEngine.Fleet f2 = engine.createFleet("fleet-2", "Fleet 2", 1);

        f1.setSectorPosition(5, 5);
        f2.setSectorPosition(5, 6); // Adjacent

        assertTrue(engine.canEngage("fleet-1", "fleet-2"));
    }

    @Test
    @DisplayName("Distant fleets cannot engage")
    void distantFleetsCannotEngage() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet f1 = engine.createFleet("fleet-1", "Fleet 1", 0);
        SfbCampaignEngine.Fleet f2 = engine.createFleet("fleet-2", "Fleet 2", 1);

        f1.setSectorPosition(0, 0);
        f2.setSectorPosition(10, 10); // Far away

        assertFalse(engine.canEngage("fleet-1", "fleet-2"));
    }

    @Test
    @DisplayName("Same sector fleets can engage")
    void sameSectorFleetsCanEngage() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet f1 = engine.createFleet("fleet-1", "Fleet 1", 0);
        SfbCampaignEngine.Fleet f2 = engine.createFleet("fleet-2", "Fleet 2", 1);

        f1.setSectorPosition(5, 5);
        f2.setSectorPosition(5, 5); // Same sector

        assertTrue(engine.canEngage("fleet-1", "fleet-2"));
    }

    // --- Repair Tests ---

    @Test
    @DisplayName("Repair fleet restores shields")
    void repairFleetRestoresShields() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Test Fleet", 0);

        SfbShip ship = engine.buildShip("fleet-1", "federation", "Constitution-class");

        // Damage shields
        SfbShield shield = ship.getShields()[0];
        shield.absorbDamage(10);
        int boxesBefore = shield.getBoxesRemaining();

        // Repair
        engine.repairFleet("fleet-1", 2); // Dockyard bonus of 2

        int boxesAfter = shield.getBoxesRemaining();
        assertTrue(boxesAfter >= boxesBefore, "Shields should be repaired");
    }

    @Test
    @DisplayName("Repair fleet reduces excess damage")
    void repairFleetReducesExcessDamage() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Test Fleet", 0);

        SfbShip ship = engine.buildShip("fleet-1", "federation", "Constitution-class");
        ship.getSsd().addExcessDamage(20);
        assertEquals(20, ship.getSsd().getExcessDamage());

        engine.repairFleet("fleet-1", 3); // DamCon 4 + dockyard 3 = 7, * 2 = 14 repaired

        assertTrue(ship.getSsd().getExcessDamage() < 20, "Excess damage should be reduced");
    }

    @Test
    @DisplayName("Repair uncripples ship when excess damage cleared")
    void repairUncripplesShip() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Test Fleet", 0);

        SfbShip ship = engine.buildShip("fleet-1", "federation", "Constitution-class");
        ship.getSsd().addExcessDamage(5);
        ship.setCrippled(true);

        engine.repairFleet("fleet-1", 5); // High dockyard bonus to clear damage

        assertFalse(ship.isCrippled(), "Ship should be uncrippled when excess damage is cleared");
    }

    @Test
    @DisplayName("Repair does not affect destroyed ships")
    void repairDoesNotAffectDestroyed() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Test Fleet", 0);

        SfbShip ship = engine.buildShip("fleet-1", "federation", "Constitution-class");
        ship.setDestroyed(true);

        engine.repairFleet("fleet-1", 10);

        assertTrue(ship.isDestroyed(), "Destroyed ship should remain destroyed");
    }

    // --- Getters Tests ---

    @Test
    @DisplayName("Get fleet by ID")
    void getFleetById() {
        SfbCampaignEngine engine = createCampaignEngine();
        engine.createFleet("fleet-1", "Test Fleet", 0);

        SfbCampaignEngine.Fleet fleet = engine.getFleet("fleet-1");
        assertNotNull(fleet);
        assertEquals("Test Fleet", fleet.getName());
    }

    @Test
    @DisplayName("Get null for unknown fleet")
    void getUnknownFleet() {
        SfbCampaignEngine engine = createCampaignEngine();

        assertNull(engine.getFleet("nonexistent"));
    }

    @Test
    @DisplayName("Shipyard contains built ships")
    void shipyardContainsShips() {
        SfbCampaignEngine engine = createCampaignEngine();
        SfbCampaignEngine.Fleet fleet = engine.createFleet("fleet-1", "Test Fleet", 0);

        engine.buildShip("fleet-1", "federation", "Constitution-class");
        engine.buildShip("fleet-1", "klingon", "D7-class");

        assertEquals(2, engine.getShipyard().size());
    }
}

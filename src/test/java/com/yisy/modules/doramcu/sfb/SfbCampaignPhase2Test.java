package com.yisy.modules.doramcu.sfb;

import com.yisy.modules.doramcu.sfb.model.SfbBattleOutcome;
import com.yisy.modules.doramcu.sfb.model.SfbHexPosition;
import com.yisy.modules.doramcu.sfb.model.SfbShip;
import com.yisy.modules.doramcu.sfb.model.SfbSSD;
import com.yisy.modules.doramcu.sfb.service.SfbCampaignEngine;
import com.yisy.modules.doramcu.sfb.service.SfbCampaignHistory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SfbCampaignPhase2Test {

    private final SfbCampaignEngine engine = new SfbCampaignEngine();

    private SfbShip createShip(String id, String shipClass, int side) {
        SfbShip ship = new SfbShip();
        ship.setId(id);
        ship.setShipClass(shipClass);
        ship.setName(shipClass + "-" + id);
        ship.setSide(side);
        ship.setSsd(new SfbSSD());
        return ship;
    }

    // ---- Battle Outcome Tests ----

    @Nested
    @DisplayName("Battle Outcome Tracking")
    class BattleOutcomeTests {

        @Test
        @DisplayName("Should record battle outcome when battle is completed")
        void testCompleteBattleRecordsOutcome() {
            engine.createFleet("f0", "Fleet 0", 0);
            engine.createFleet("f1", "Fleet 1", 1);

            SfbShip ship0 = createShip("s0", "S09", 0);
            ship0.setPosition(new SfbHexPosition(1, 1, 0));
            engine.addShipToFleet("f0", ship0);

            SfbShip ship1 = createShip("s1", "K01", 1);
            ship1.setPosition(new SfbHexPosition(1, 1, 0));
            engine.addShipToFleet("f1", ship1);

            engine.startBattle("b1", "f0", "f1");
            ship1.setDestroyed(true);

            SfbBattleOutcome outcome = engine.completeBattle("b1", 0);

            assertNotNull(outcome);
            assertEquals("b1", outcome.getBattleId());
            assertEquals(SfbBattleOutcome.Result.SIDE_0_VICTORY, outcome.getResult());
            assertEquals(0, outcome.getWinningSide());
            assertEquals(0, outcome.getShipsLostSide0());
            assertEquals(1, outcome.getShipsLostSide1());
            assertTrue(outcome.getDestroyedShipIds().contains("s1"));
        }

        @Test
        @DisplayName("Should record stalemate when winningSide is -1")
        void testStalemateOutcome() {
            engine.createFleet("f2", "Fleet 2", 0);
            engine.createFleet("f3", "Fleet 3", 1);

            engine.addShipToFleet("f2", createShip("s2", "S09", 0));
            engine.addShipToFleet("f3", createShip("s3", "K01", 1));

            engine.startBattle("b2", "f2", "f3");
            SfbBattleOutcome outcome = engine.completeBattle("b2", -1);

            assertNotNull(outcome);
            assertEquals(SfbBattleOutcome.Result.STALEMATE, outcome.getResult());
            assertEquals(-1, outcome.getWinningSide());
        }

        @Test
        @DisplayName("Should record mutual destruction when both sides lose ships")
        void testMutualDestructionOutcome() {
            engine.createFleet("f4", "Fleet 4", 0);
            engine.createFleet("f5", "Fleet 5", 1);

            SfbShip ship0 = createShip("s4", "S09", 0);
            engine.addShipToFleet("f4", ship0);

            SfbShip ship1 = createShip("s5", "K01", 1);
            engine.addShipToFleet("f5", ship1);

            engine.startBattle("b3", "f4", "f5");
            ship0.setDestroyed(true);
            ship1.setDestroyed(true);

            SfbBattleOutcome outcome = engine.completeBattle("b3", 0);

            assertNotNull(outcome);
            assertEquals(SfbBattleOutcome.Result.MUTUAL_DESTRUCTION, outcome.getResult());
            assertEquals(1, outcome.getShipsLostSide0());
            assertEquals(1, outcome.getShipsLostSide1());
        }

        @Test
        @DisplayName("Should return null when completing non-existent battle")
        void testCompleteNonExistentBattle() {
            assertNull(engine.completeBattle("nonexistent", 0));
        }

        @Test
        @DisplayName("Should remove destroyed ships from fleets")
        void testDestroyedShipsRemovedFromFleet() {
            engine.createFleet("f6", "Fleet 6", 0);
            engine.createFleet("f7", "Fleet 7", 1);

            engine.addShipToFleet("f6", createShip("s6", "S09", 0));
            SfbShip ship1 = createShip("s7", "K01", 1);
            ship1.setDestroyed(true);
            engine.addShipToFleet("f7", ship1);

            engine.startBattle("b4", "f6", "f7");
            engine.completeBattle("b4", 0);

            assertEquals(1, engine.getFleet("f6").getShipCount());
            assertEquals(0, engine.getFleet("f7").getShipCount());
        }
    }

    // ---- Campaign History Tests ----

    @Nested
    @DisplayName("Campaign History")
    class CampaignHistoryTests {

        @Test
        @DisplayName("Should retrieve all battle outcomes")
        void testGetAllOutcomes() {
            SfbCampaignHistory history = engine.getCampaignHistory();
            assertTrue(history.getAllOutcomes().isEmpty());

            engine.createFleet("h0", "History 0", 0);
            engine.createFleet("h1", "History 1", 1);
            engine.addShipToFleet("h0", createShip("hs0", "S09", 0));
            engine.addShipToFleet("h1", createShip("hs1", "K01", 1));

            engine.startBattle("hb1", "h0", "h1");
            engine.completeBattle("hb1", 0);

            engine.startBattle("hb2", "h0", "h1");
            engine.completeBattle("hb2", 1);

            assertEquals(2, history.getAllOutcomes().size());
        }

        @Test
        @DisplayName("Should retrieve outcome by battle ID")
        void testGetOutcomeByBattleId() {
            engine.createFleet("h2", "History 2", 0);
            engine.createFleet("h3", "History 3", 1);
            engine.addShipToFleet("h2", createShip("hs2", "S09", 0));
            engine.addShipToFleet("h3", createShip("hs3", "K01", 1));

            engine.startBattle("hb3", "h2", "h3");
            engine.completeBattle("hb3", 0);

            SfbBattleOutcome outcome = engine.getCampaignHistory().getOutcome("hb3");
            assertNotNull(outcome);
            assertEquals("hb3", outcome.getBattleId());
        }

        @Test
        @DisplayName("Should return null for non-existent battle ID")
        void testGetNonExistentOutcome() {
            assertNull(engine.getCampaignHistory().getOutcome("nonexistent"));
        }

        @Test
        @DisplayName("Should filter outcomes by side")
        void testGetOutcomesForSide() {
            engine.createFleet("h4", "History 4", 0);
            engine.createFleet("h5", "History 5", 1);
            engine.addShipToFleet("h4", createShip("hs4", "S09", 0));
            engine.addShipToFleet("h5", createShip("hs5", "K01", 1));

            engine.startBattle("hb4", "h4", "h5");
            engine.completeBattle("hb4", 0);

            engine.startBattle("hb5", "h4", "h5");
            engine.completeBattle("hb5", 1);

            List<SfbBattleOutcome> side0Outcomes = engine.getCampaignHistory().getOutcomesForSide(0);
            assertTrue(side0Outcomes.stream().anyMatch(o -> o.getBattleId().equals("hb4")));
        }

        @Test
        @DisplayName("Should filter outcomes by result type")
        void testGetOutcomesByResult() {
            engine.createFleet("h6", "History 6", 0);
            engine.createFleet("h7", "History 7", 1);
            engine.addShipToFleet("h6", createShip("hs6", "S09", 0));
            engine.addShipToFleet("h7", createShip("hs7", "K01", 1));

            engine.startBattle("hb6", "h6", "h7");
            engine.completeBattle("hb6", 0);

            engine.startBattle("hb7", "h6", "h7");
            engine.completeBattle("hb7", -1);

            List<SfbBattleOutcome> stalemates = engine.getCampaignHistory()
                    .getOutcomesByResult(SfbBattleOutcome.Result.STALEMATE);
            assertEquals(1, stalemates.size());
            assertEquals("hb7", stalemates.get(0).getBattleId());
        }

        @Test
        @DisplayName("Should calculate side statistics")
        void testGetSideStatistics() {
            engine.createFleet("h8", "History 8", 0);
            engine.createFleet("h9", "History 9", 1);
            engine.addShipToFleet("h8", createShip("hs8", "S09", 0));
            SfbShip s1 = createShip("hs9", "K01", 1);
            s1.setDestroyed(true);
            engine.addShipToFleet("h9", s1);

            engine.startBattle("hb8", "h8", "h9");
            engine.completeBattle("hb8", 0);

            Map<String, Integer> stats = engine.getCampaignHistory().getSideStatistics(0);
            assertEquals(1, stats.get("wins"));
            assertEquals(0, stats.get("losses"));
        }

        @Test
        @DisplayName("Should clear all outcomes")
        void testClearOutcomes() {
            engine.createFleet("h10", "History 10", 0);
            engine.createFleet("h11", "History 11", 1);
            engine.addShipToFleet("h10", createShip("hs10", "S09", 0));
            engine.addShipToFleet("h11", createShip("hs11", "K01", 1));

            engine.startBattle("hb9", "h10", "h11");
            engine.completeBattle("hb9", 0);

            engine.getCampaignHistory().clear();
            assertTrue(engine.getCampaignHistory().getAllOutcomes().isEmpty());
        }
    }

    // ---- Fleet Composition Tests ----

    @Nested
    @DisplayName("Fleet Composition")
    class FleetCompositionTests {

        @Test
        @DisplayName("Should return ship class breakdown for a fleet")
        void testGetFleetComposition() {
            engine.createFleet("comp0", "Composition 0", 0);
            engine.addShipToFleet("comp0", createShip("c0a", "S09", 0));
            engine.addShipToFleet("comp0", createShip("c0b", "S09", 0));
            engine.addShipToFleet("comp0", createShip("c0c", "K01", 1));

            Map<String, Integer> composition = engine.getFleetComposition("comp0");
            assertEquals(2, composition.size());
            assertEquals(2, composition.get("S09"));
            assertEquals(1, composition.get("K01"));
        }

        @Test
        @DisplayName("Should return empty map for non-existent fleet")
        void testGetFleetCompositionNotFound() {
            assertTrue(engine.getFleetComposition("nonexistent").isEmpty());
        }

        @Test
        @DisplayName("Should return composition for all fleets of a side")
        void testGetAllFleetCompositions() {
            engine.createFleet("comp1", "Composition 1", 0);
            engine.createFleet("comp2", "Composition 2", 0);
            engine.addShipToFleet("comp1", createShip("c1", "S09", 0));
            engine.addShipToFleet("comp2", createShip("c2", "K01", 1));

            Map<String, Map<String, Integer>> compositions = engine.getAllFleetCompositions(0);
            assertEquals(2, compositions.size());
            assertTrue(compositions.containsKey("comp1"));
            assertTrue(compositions.containsKey("comp2"));
        }
    }

    // ---- Fleet Transfer Tests ----

    @Nested
    @DisplayName("Fleet Transfer")
    class FleetTransferTests {

        @Test
        @DisplayName("Should transfer ships between fleets")
        void testTransferShips() {
            engine.createFleet("tr0", "Transfer 0", 0);
            engine.createFleet("tr1", "Transfer 1", 0);
            engine.addShipToFleet("tr0", createShip("t0", "S09", 0));
            engine.addShipToFleet("tr0", createShip("t1", "S09", 0));

            int transferred = engine.transferShips("tr0", "tr1", List.of("t0"));
            assertEquals(1, transferred);
            assertEquals(1, engine.getFleet("tr0").getShipCount());
            assertEquals(1, engine.getFleet("tr1").getShipCount());
        }

        @Test
        @DisplayName("Should throw when source fleet not found")
        void testTransferSourceNotFound() {
            engine.createFleet("tr2", "Transfer 2", 0);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.transferShips("nonexistent", "tr2", List.of("t0")));
        }

        @Test
        @DisplayName("Should throw when target fleet not found")
        void testTransferTargetNotFound() {
            engine.createFleet("tr3", "Transfer 3", 0);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.transferShips("tr3", "nonexistent", List.of("t0")));
        }

        @Test
        @DisplayName("Should return 0 when ship not in source fleet")
        void testTransferShipNotInSource() {
            engine.createFleet("tr4", "Transfer 4", 0);
            engine.createFleet("tr5", "Transfer 5", 0);

            assertEquals(0, engine.transferShips("tr4", "tr5", List.of("nonexistent")));
        }
    }

    // ---- Fleet Split Tests ----

    @Nested
    @DisplayName("Fleet Split")
    class FleetSplitTests {

        @Test
        @DisplayName("Should split fleet into two")
        void testSplitFleet() {
            SfbCampaignEngine.Fleet f0 = engine.createFleet("sp0", "Split 0", 0);
            f0.setSectorPosition(5, 5);
            engine.addShipToFleet("sp0", createShip("sp0a", "S09", 0));
            engine.addShipToFleet("sp0", createShip("sp0b", "S09", 0));
            engine.addShipToFleet("sp0", createShip("sp0c", "S09", 0));

            SfbCampaignEngine.Fleet newFleet = engine.splitFleet("sp0", "sp1", "Split 1");

            assertNotNull(newFleet);
            assertEquals("sp1", newFleet.getId());
            assertEquals(0, newFleet.getOwnerSide());
            assertEquals(5, newFleet.getSectorRow());
            assertEquals(5, newFleet.getSectorCol());
            assertEquals(1, newFleet.getShipCount());
            assertEquals(2, engine.getFleet("sp0").getShipCount());
        }

        @Test
        @DisplayName("Should throw when source fleet not found")
        void testSplitSourceNotFound() {
            assertThrows(IllegalArgumentException.class,
                    () -> engine.splitFleet("nonexistent", "new", "New Fleet"));
        }

        @Test
        @DisplayName("Should throw when fleet has fewer than 2 ships")
        void testSplitInsufficientShips() {
            engine.createFleet("sp2", "Split 2", 0);
            engine.addShipToFleet("sp2", createShip("sp2a", "S09", 0));

            assertThrows(IllegalStateException.class,
                    () -> engine.splitFleet("sp2", "sp3", "Split 3"));
        }
    }

    // ---- Fleet Merge Tests ----

    @Nested
    @DisplayName("Fleet Merge")
    class FleetMergeTests {

        @Test
        @DisplayName("Should merge two fleets")
        void testMergeFleets() {
            engine.createFleet("mg0", "Merge 0", 0);
            engine.createFleet("mg1", "Merge 1", 0);
            engine.addShipToFleet("mg0", createShip("mg0a", "S09", 0));
            engine.addShipToFleet("mg1", createShip("mg0b", "S09", 0));

            SfbCampaignEngine.Fleet merged = engine.mergeFleets("mg0", "mg1");

            assertEquals("mg0", merged.getId());
            assertEquals(2, merged.getShipCount());
            assertNull(engine.getFleet("mg1"));
            assertEquals(1, engine.getFleetCount());
        }

        @Test
        @DisplayName("Should throw when primary fleet not found")
        void testMergePrimaryNotFound() {
            engine.createFleet("mg2", "Merge 2", 0);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.mergeFleets("nonexistent", "mg2"));
        }

        @Test
        @DisplayName("Should throw when secondary fleet not found")
        void testMergeSecondaryNotFound() {
            engine.createFleet("mg3", "Merge 3", 0);
            assertThrows(IllegalArgumentException.class,
                    () -> engine.mergeFleets("mg3", "nonexistent"));
        }
    }

    // ---- Integration Scenario Tests ----

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("Should track full campaign workflow")
        void testFullCampaignWorkflow() {
            engine.createFleet("alpha", "Alpha Fleet", 0);
            engine.createFleet("bravo", "Bravo Fleet", 1);

            engine.addShipToFleet("alpha", createShip("a0", "S09", 0));
            engine.addShipToFleet("alpha", createShip("a1", "S09", 0));
            SfbShip s2 = createShip("b0", "K01", 1);
            engine.addShipToFleet("bravo", s2);
            engine.addShipToFleet("bravo", createShip("b1", "K01", 1));

            Map<String, Integer> alphaComp = engine.getFleetComposition("alpha");
            assertEquals(2, alphaComp.get("S09"));

            s2.setDestroyed(true);
            engine.startBattle("battle1", "alpha", "bravo");
            SfbBattleOutcome outcome1 = engine.completeBattle("battle1", 0);
            assertEquals(SfbBattleOutcome.Result.SIDE_0_VICTORY, outcome1.getResult());

            assertEquals(1, engine.getCampaignHistory().getOutcomeCount());
            assertNotNull(engine.getCampaignHistory().getOutcome("battle1"));

            engine.startBattle("battle2", "alpha", "bravo");
            SfbBattleOutcome outcome2 = engine.completeBattle("battle2", -1);
            assertEquals(SfbBattleOutcome.Result.STALEMATE, outcome2.getResult());

            Map<String, Integer> stats = engine.getCampaignHistory().getSideStatistics(0);
            assertEquals(1, stats.get("wins"));

            SfbCampaignEngine.Fleet charlie = engine.splitFleet("alpha", "charlie", "Charlie Fleet");
            assertNotNull(charlie);
            assertTrue(charlie.getShipCount() >= 1);

            assertEquals(3, engine.getFleetCount());
            assertEquals(2, engine.getCampaignHistory().getOutcomeCount());
        }

        @Test
        @DisplayName("Should handle sector-based outcome filtering")
        void testSectorBasedFiltering() {
            engine.createFleet("sec0", "Sector 0", 0);
            engine.createFleet("sec1", "Sector 1", 1);

            SfbShip s0 = createShip("sec0a", "S09", 0);
            s0.setPosition(new SfbHexPosition(5, 5, 0));
            engine.addShipToFleet("sec0", s0);

            SfbShip s1 = createShip("sec1a", "K01", 1);
            s1.setPosition(new SfbHexPosition(5, 5, 0));
            engine.addShipToFleet("sec1", s1);

            engine.startBattle("sec_battle", "sec0", "sec1");
            engine.completeBattle("sec_battle", 0);

            List<SfbBattleOutcome> sectorOutcomes = engine.getCampaignHistory()
                    .getOutcomesInSector(5, 5);
            assertEquals(1, sectorOutcomes.size());
            assertEquals("sec_battle", sectorOutcomes.get(0).getBattleId());
        }
    }
}

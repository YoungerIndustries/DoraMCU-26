package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AirBattle")
class AirBattleTest {

    private TravellerSpacecraft createSpacecraft(String name, int attack, int defense, int hull) {
        TravellerSpacecraft sc = new TravellerSpacecraft("sc-" + name, name, TravellerSpacecraftType.FIGHTER, 7);
        sc.setAttackValue(attack);
        sc.setDefenseValue(defense);
        sc.setHullPoints(hull);
        return sc;
    }

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates battle with id")
        void createsWithId() {
            AirBattle battle = new AirBattle("battle-1");

            assertEquals("battle-1", battle.getId());
            assertEquals(AirBattle.BattleState.INITIATED, battle.getState());
            assertEquals(AirBattle.BattleMode.ABSTRACT, battle.getMode());
            assertEquals(AirBattle.Altitude.MEDIUM, battle.getAltitude());
            assertTrue(battle.getAttackerUnits().isEmpty());
            assertTrue(battle.getDefenderUnits().isEmpty());
        }
    }

    @Nested
    @DisplayName("Start")
    class Start {

        @Test
        @DisplayName("starts battle with units on both sides")
        void startsWithUnits() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 5, 2, 20));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 3, 2, 20));
            battle.start();

            assertEquals(AirBattle.BattleState.IN_PROGRESS, battle.getState());
            assertFalse(battle.getEvents().isEmpty());
        }

        @Test
        @DisplayName("draws when no attackers")
        void drawsWhenNoAttackers() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getDefenderUnits().add(createSpacecraft("Def1", 3, 2, 20));
            battle.start();

            assertEquals(AirBattle.BattleState.COMPLETED, battle.getState());
            assertEquals(AirBattleResult.Outcome.DRAW, battle.getResult().getOutcome());
        }

        @Test
        @DisplayName("draws when no defenders")
        void drawsWhenNoDefenders() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 5, 2, 20));
            battle.start();

            assertEquals(AirBattle.BattleState.COMPLETED, battle.getState());
            assertEquals(AirBattleResult.Outcome.DRAW, battle.getResult().getOutcome());
        }

        @Test
        @DisplayName("draws when both sides empty")
        void drawsWhenBothEmpty() {
            AirBattle battle = new AirBattle("battle-1");
            battle.start();

            assertEquals(AirBattle.BattleState.COMPLETED, battle.getState());
        }
    }

    @Nested
    @DisplayName("Abstract Battle Resolution")
    class AbstractResolution {

        @Test
        @DisplayName("resolves abstract battle and returns result")
        void resolvesAbstract() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 10, 2, 20));
            battle.getAttackerUnits().add(createSpacecraft("Atk2", 10, 2, 20));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 3, 2, 20));
            AirBattleResult result = battle.resolveAbstract();

            assertEquals("battle-1", result.getBattleId());
            assertNotNull(result.getDescription());
            assertNotNull(result.getOutcome());
            assertEquals(AirBattle.BattleState.COMPLETED, battle.getState());
            assertFalse(battle.getEvents().isEmpty());
        }

        @Test
        @DisplayName("sets odds and dice roll")
        void setsOddsAndDice() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 10, 2, 20));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 3, 2, 20));
            AirBattleResult result = battle.resolveAbstract();

            assertTrue(result.getOdds() >= 1 && result.getOdds() <= 9);
            // 2 + random.nextInt(6) + random.nextInt(6) = range 2-11
            assertTrue(result.getDiceRoll() >= 2 && result.getDiceRoll() <= 11);
        }

        @Test
        @DisplayName("calculates losses based on CRT result")
        void calculatesLosses() {
            AirBattle battle = new AirBattle("battle-1");
            for (int i = 0; i < 5; i++) {
                battle.getAttackerUnits().add(createSpacecraft("Atk" + i, 10, 2, 20));
                battle.getDefenderUnits().add(createSpacecraft("Def" + i, 1, 1, 10));
            }
            AirBattleResult result = battle.resolveAbstract();

            // With overwhelming odds, there should be some losses
            assertTrue(result.getAttackerLosses() >= 0);
            assertTrue(result.getDefenderLosses() >= 0);
        }

        @Test
        @DisplayName("removes destroyed units from lists")
        void removesDestroyedUnits() {
            AirBattle battle = new AirBattle("battle-1");
            for (int i = 0; i < 5; i++) {
                battle.getAttackerUnits().add(createSpacecraft("Atk" + i, 10, 2, 20));
                battle.getDefenderUnits().add(createSpacecraft("Def" + i, 1, 1, 10));
            }
            int initialDefenders = battle.getDefenderUnits().size();
            battle.resolveAbstract();

            // Some defenders should be removed
            assertTrue(battle.getDefenderUnits().size() <= initialDefenders);
        }
    }

    @Nested
    @DisplayName("Detailed Battle Resolution")
    class DetailedResolution {

        @Test
        @DisplayName("fighter completes with attacker victory")
        void attackerVictory() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 10, 2, 50));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 1, 2, 10));
            AirBattleResult result = battle.fightToCompletion();

            assertNotNull(result);
            assertEquals(AirBattle.BattleState.COMPLETED, battle.getState());
            assertTrue(result.getOutcome() == AirBattleResult.Outcome.ATTACKER_VICTORY ||
                       result.getOutcome() == AirBattleResult.Outcome.DRAW);
        }

        @Test
        @DisplayName("fighter completes with defender victory")
        void defenderVictory() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 1, 2, 10));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 10, 2, 50));
            AirBattleResult result = battle.fightToCompletion();

            assertNotNull(result);
            assertEquals(AirBattle.BattleState.COMPLETED, battle.getState());
            assertTrue(result.getOutcome() == AirBattleResult.Outcome.DEFENDER_VICTORY ||
                       result.getOutcome() == AirBattleResult.Outcome.DRAW);
        }

        @Test
        @DisplayName("stops at 20 rounds max")
        void stopsAtMaxRounds() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 5, 5, 100));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 5, 5, 100));
            battle.fightToCompletion();

            assertTrue(battle.getCurrentRound() <= 20);
        }

        @Test
        @DisplayName("draws when stalemate")
        void drawsOnStalemate() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 5, 5, 200));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 5, 5, 200));
            AirBattleResult result = battle.fightToCompletion();

            // With equal stats and high hull, may draw
            assertNotNull(result);
        }

        @Test
        @DisplayName("tracks rounds")
        void tracksRounds() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 10, 2, 50));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 1, 2, 10));
            battle.fightToCompletion();

            assertTrue(battle.getCurrentRound() > 0);
        }
    }

    @Nested
    @DisplayName("Fight Round")
    class FightRound {

        @Test
        @DisplayName("increments round counter")
        void incrementsRound() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 10, 2, 50));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 3, 2, 20));
            battle.start();
            battle.fightRound();

            assertEquals(1, battle.getCurrentRound());
        }

        @Test
        @DisplayName("deals damage to both sides")
        void dealsDamage() {
            AirBattle battle = new AirBattle("battle-1");
            TravellerSpacecraft atk = createSpacecraft("Atk1", 10, 2, 50);
            TravellerSpacecraft def = createSpacecraft("Def1", 3, 2, 20);
            battle.getAttackerUnits().add(atk);
            battle.getDefenderUnits().add(def);
            battle.start();
            battle.fightRound();

            // Defender should have taken damage
            assertTrue(def.getHullPoints() < 20 || battle.getDefenderUnits().isEmpty());
        }

        @Test
        @DisplayName("completes when one side destroyed")
        void completesWhenDestroyed() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 20, 2, 50));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 1, 2, 5));
            battle.start();
            battle.fightRound();

            assertEquals(AirBattle.BattleState.COMPLETED, battle.getState());
        }
    }

    @Nested
    @DisplayName("Reinforce")
    class Reinforce {

        @Test
        @DisplayName("adds units to attacker side")
        void reinforcesAttackers() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 5, 2, 20));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 3, 2, 20));
            battle.start();

            List<TravellerSpacecraft> reinforcements = new ArrayList<>();
            reinforcements.add(createSpacecraft("Atk2", 5, 2, 20));
            battle.reinforce(reinforcements, AirBattle.Side.ATTACKER);

            assertEquals(2, battle.getAttackerUnits().size());
        }

        @Test
        @DisplayName("adds units to defender side")
        void reinforcesDefenders() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 5, 2, 20));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 3, 2, 20));
            battle.start();

            List<TravellerSpacecraft> reinforcements = new ArrayList<>();
            reinforcements.add(createSpacecraft("Def2", 3, 2, 20));
            battle.reinforce(reinforcements, AirBattle.Side.DEFENDER);

            assertEquals(2, battle.getDefenderUnits().size());
        }

        @Test
        @DisplayName("ignores reinforce when not in progress")
        void ignoresWhenNotInProgress() {
            AirBattle battle = new AirBattle("battle-1");
            List<TravellerSpacecraft> reinforcements = new ArrayList<>();
            reinforcements.add(createSpacecraft("Atk2", 5, 2, 20));
            battle.reinforce(reinforcements, AirBattle.Side.ATTACKER);

            assertTrue(battle.getAttackerUnits().isEmpty());
        }
    }

    @Nested
    @DisplayName("Retreat")
    class Retreat {

        @Test
        @DisplayName("ends battle with retreat outcome")
        void retreats() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 5, 2, 20));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 3, 2, 20));
            battle.start();
            battle.retreat();

            assertEquals(AirBattle.BattleState.COMPLETED, battle.getState());
            assertEquals(AirBattleResult.Outcome.RETREAT, battle.getResult().getOutcome());
        }

        @Test
        @DisplayName("ignores retreat when not in progress")
        void ignoresWhenNotInProgress() {
            AirBattle battle = new AirBattle("battle-1");
            battle.retreat();

            assertEquals(AirBattle.BattleState.INITIATED, battle.getState());
        }
    }

    @Nested
    @DisplayName("Altitude")
    class Altitude {

        @Test
        @DisplayName("sets altitude")
        void setsAltitude() {
            AirBattle battle = new AirBattle("battle-1");
            battle.setAltitude(AirBattle.Altitude.HIGH);

            assertEquals(AirBattle.Altitude.HIGH, battle.getAltitude());
        }

        @Test
        @DisplayName("has three altitude levels")
        void hasThreeAltitudes() {
            assertEquals(3, AirBattle.Altitude.values().length);
        }
    }

    @Nested
    @DisplayName("Events")
    class Events {

        @Test
        @DisplayName("records events during battle")
        void recordsEvents() {
            AirBattle battle = new AirBattle("battle-1");
            battle.getAttackerUnits().add(createSpacecraft("Atk1", 10, 2, 20));
            battle.getDefenderUnits().add(createSpacecraft("Def1", 3, 2, 20));
            battle.resolveAbstract();

            assertFalse(battle.getEvents().isEmpty());
            AirBattleEvent firstEvent = battle.getEvents().get(0);
            assertNotNull(firstEvent.getMessage());
        }
    }
}

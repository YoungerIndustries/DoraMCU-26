package com.yisy.modules.doramcu.thirdreich.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ThirdReichBattle")
class ThirdReichBattleTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates battle with INITIATED state")
        void createsWithInitiatedState() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");

            assertEquals("b-1", battle.getId());
            assertEquals(ThirdReichBattle.BattleState.INITIATED, battle.getState());
            assertEquals(0, battle.getCurrentRound());
            assertTrue(battle.getAttackerUnits().isEmpty());
            assertTrue(battle.getDefenderUnits().isEmpty());
            assertTrue(battle.getEvents().isEmpty());
            assertNull(battle.getResult());
        }

        @Test
        @DisplayName("throws on null ID")
        void throwsOnNullId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ThirdReichBattle(null));
        }

        @Test
        @DisplayName("throws on blank ID")
        void throwsOnBlankId() {
            assertThrows(IllegalArgumentException.class, () ->
                    new ThirdReichBattle("  "));
        }
    }

    @Nested
    @DisplayName("Unit Management")
    class UnitManagement {

        @Test
        @DisplayName("addAttackerUnit adds to attacker list")
        void addAttackerUnit() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Alpha", ThirdReichUnitType.INFANTRY);
            battle.addAttackerUnit(unit);

            assertEquals(1, battle.getAttackerUnits().size());
            assertEquals(unit, battle.getAttackerUnits().get(0));
        }

        @Test
        @DisplayName("addDefenderUnit adds to defender list")
        void addDefenderUnit() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-2", "Bravo", ThirdReichUnitType.TANK);
            battle.addDefenderUnit(unit);

            assertEquals(1, battle.getDefenderUnits().size());
        }

        @Test
        @DisplayName("null units are ignored")
        void nullUnitsIgnored() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addAttackerUnit(null);
            assertTrue(battle.getAttackerUnits().isEmpty());
        }
    }

    @Nested
    @DisplayName("Start")
    class Start {

        @Test
        @DisplayName("starts battle with units on both sides")
        void startsWithUnits() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Alpha", ThirdReichUnitType.INFANTRY));
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-2", "Bravo", ThirdReichUnitType.INFANTRY));
            battle.start();

            assertEquals(ThirdReichBattle.BattleState.IN_PROGRESS, battle.getState());
            assertFalse(battle.getEvents().isEmpty());
        }

        @Test
        @DisplayName("does not start with no attackers")
        void noStartNoAttackers() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-2", "Bravo", ThirdReichUnitType.INFANTRY));
            battle.start();

            assertEquals(ThirdReichBattle.BattleState.INITIATED, battle.getState());
        }

        @Test
        @DisplayName("does not start with no defenders")
        void noStartNoDefenders() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Alpha", ThirdReichUnitType.INFANTRY));
            battle.start();

            assertEquals(ThirdReichBattle.BattleState.INITIATED, battle.getState());
        }

        @Test
        @DisplayName("includes terrain in events")
        void includesTerrain() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.setTerrainContext(new TerrainContext(TerrainType.URBAN, "Terra"));
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Alpha", ThirdReichUnitType.INFANTRY));
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-2", "Bravo", ThirdReichUnitType.INFANTRY));
            battle.start();

            String events = String.join(" ", battle.getEvents());
            assertTrue(events.contains("Urban"));
        }
    }

    @Nested
    @DisplayName("Round Management")
    class RoundManagement {

        @Test
        @DisplayName("nextRound increments round number")
        void nextRoundIncrements() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.nextRound();
            assertEquals(1, battle.getCurrentRound());
            battle.nextRound();
            assertEquals(2, battle.getCurrentRound());
        }

        @Test
        @DisplayName("nextRound adds event")
        void nextRoundAddsEvent() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.nextRound();
            assertFalse(battle.getEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("End Battle")
    class EndBattle {

        @Test
        @DisplayName("endBattle sets result and state")
        void endBattleSetsResult() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            ThirdReichBattleResult result = new ThirdReichBattleResult("b-1", ThirdReichBattleResult.Outcome.ATTACKER_VICTORY);
            battle.endBattle(result);

            assertEquals(ThirdReichBattle.BattleState.COMPLETED, battle.getState());
            assertEquals(result, battle.getResult());
        }

        @Test
        @DisplayName("retreat sets state to RETREAT")
        void retreatSetsState() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.retreat();
            assertEquals(ThirdReichBattle.BattleState.RETREAT, battle.getState());
        }
    }

    @Nested
    @DisplayName("Combat Power Calculations")
    class CombatPower {

        @Test
        @DisplayName("attacker total attack sums effective attacks")
        void attackerTotalAttack() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            ThirdReichGroundUnit unit1 = new ThirdReichGroundUnit("u-1", "Alpha", ThirdReichUnitType.TANK);
            ThirdReichGroundUnit unit2 = new ThirdReichGroundUnit("u-2", "Bravo", ThirdReichUnitType.INFANTRY);
            battle.addAttackerUnit(unit1);
            battle.addAttackerUnit(unit2);

            // Tank atk=4, Infantry atk=1, total=5
            assertEquals(5, battle.getAttackerTotalAttack());
        }

        @Test
        @DisplayName("defender total defense sums effective defenses")
        void defenderTotalDefense() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Alpha", ThirdReichUnitType.TANK);
            battle.addDefenderUnit(unit);

            // Tank def=3
            assertEquals(3, battle.getDefenderTotalDefense());
        }

        @Test
        @DisplayName("destroyed units excluded from totals")
        void destroyedExcluded() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            ThirdReichGroundUnit unit = new ThirdReichGroundUnit("u-1", "Alpha", ThirdReichUnitType.TANK);
            unit.setStrength(0);
            battle.addAttackerUnit(unit);

            assertEquals(0, battle.getAttackerTotalAttack());
        }

        @Test
        @DisplayName("commander IDs can be set")
        void commanderIds() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.setAttackerCommanderId("cmd-1");
            battle.setDefenderCommanderId("cmd-2");
            assertEquals("cmd-1", battle.getAttackerCommanderId());
            assertEquals("cmd-2", battle.getDefenderCommanderId());
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToString {

        @Test
        @DisplayName("toString contains battle info")
        void containsInfo() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            String str = battle.toString();
            assertTrue(str.contains("b-1"));
            assertTrue(str.contains("INITIATED"));
        }
    }
}

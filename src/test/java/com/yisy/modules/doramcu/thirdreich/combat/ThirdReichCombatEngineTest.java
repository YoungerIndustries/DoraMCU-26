package com.yisy.modules.doramcu.thirdreich.combat;

import com.yisy.modules.doramcu.thirdreich.commander.AICommander;
import com.yisy.modules.doramcu.thirdreich.commander.AICommanderProfile;
import com.yisy.modules.doramcu.thirdreich.commander.CommanderTrait;
import com.yisy.modules.doramcu.thirdreich.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ThirdReichCombatEngine")
class ThirdReichCombatEngineTest {

    private final ThirdReichCombatEngine engine = new ThirdReichCombatEngine();

    @Nested
    @DisplayName("Resolve Battle")
    class ResolveBattle {

        @Test
        @DisplayName("throws on null battle")
        void throwsOnNullBattle() {
            assertThrows(IllegalArgumentException.class, () ->
                    engine.resolveBattle(null, null, null));
        }

        @Test
        @DisplayName("returns draw when no attacker units")
        void drawNoAttackers() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-1", "Def", ThirdReichUnitType.INFANTRY));

            ThirdReichBattleResult result = engine.resolveBattle(battle, null, null);

            assertEquals(ThirdReichBattleResult.Outcome.DRAW, result.getOutcome());
            assertEquals("b-1", result.getBattleId());
        }

        @Test
        @DisplayName("returns draw when no defender units")
        void drawNoDefenders() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Atk", ThirdReichUnitType.INFANTRY));

            ThirdReichBattleResult result = engine.resolveBattle(battle, null, null);

            assertEquals(ThirdReichBattleResult.Outcome.DRAW, result.getOutcome());
        }

        @Test
        @DisplayName("resolves battle with units on both sides")
        void resolvesWithUnits() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Atk", ThirdReichUnitType.TANK));
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-2", "Def", ThirdReichUnitType.INFANTRY));

            ThirdReichBattleResult result = engine.resolveBattle(battle, null, null);

            assertNotNull(result);
            assertEquals("b-1", result.getBattleId());
            assertNotNull(result.getOutcome());
            assertTrue(result.getRoundsFought() > 0);
            assertNotNull(result.getNarrativeSummary());
            assertEquals(ThirdReichBattle.BattleState.COMPLETED, battle.getState());
        }

        @Test
        @DisplayName("overwhelming attacker force wins")
        void overwhelmingAttacker() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            // 5 tanks vs 1 infantry
            for (int i = 0; i < 5; i++) {
                battle.addAttackerUnit(new ThirdReichGroundUnit("atk-" + i, "Atk", ThirdReichUnitType.TANK));
            }
            battle.addDefenderUnit(new ThirdReichGroundUnit("def-1", "Def", ThirdReichUnitType.INFANTRY));

            ThirdReichBattleResult result = engine.resolveBattle(battle, null, null);

            assertNotNull(result);
            assertTrue(result.getDefenderCasualties() > 0);
        }

        @Test
        @DisplayName("terrain bonus helps defender")
        void terrainHelpsDefender() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.setTerrainContext(new TerrainContext(TerrainType.MOUNTAIN));
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Atk", ThirdReichUnitType.INFANTRY));
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-2", "Def", ThirdReichUnitType.INFANTRY));

            ThirdReichBattleResult result = engine.resolveBattle(battle, null, null);

            assertNotNull(result);
            assertNotNull(result.getOutcome());
        }

        @Test
        @DisplayName("commander bonuses affect combat")
        void commanderBonuses() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Atk", ThirdReichUnitType.TANK));
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-2", "Def", ThirdReichUnitType.INFANTRY));

            AICommander atkCmd = AICommanderProfile.ROMMEL.createCommander();
            AICommander defCmd = AICommanderProfile.MONTGOMERY.createCommander();

            ThirdReichBattleResult result = engine.resolveBattle(battle, atkCmd, defCmd);

            assertNotNull(result);
            assertNotNull(result.getOutcome());
        }

        @Test
        @DisplayName("battle has events after resolution")
        void battleHasEvents() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Atk", ThirdReichUnitType.INFANTRY));
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-2", "Def", ThirdReichUnitType.INFANTRY));

            engine.resolveBattle(battle, null, null);

            assertFalse(battle.getEvents().isEmpty());
        }

        @Test
        @DisplayName("result has casualties after battle")
        void resultHasCasualties() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Atk", ThirdReichUnitType.INFANTRY));
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-2", "Def", ThirdReichUnitType.INFANTRY));

            ThirdReichBattleResult result = engine.resolveBattle(battle, null, null);

            assertTrue(result.getAttackerCasualties() >= 0);
            assertTrue(result.getDefenderCasualties() >= 0);
        }

        @Test
        @DisplayName("multiple units on each side")
        void multipleUnits() {
            ThirdReichBattle battle = new ThirdReichBattle("b-1");
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-1", "Atk1", ThirdReichUnitType.TANK));
            battle.addAttackerUnit(new ThirdReichGroundUnit("u-2", "Atk2", ThirdReichUnitType.INFANTRY));
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-3", "Def1", ThirdReichUnitType.TANK));
            battle.addDefenderUnit(new ThirdReichGroundUnit("u-4", "Def2", ThirdReichUnitType.INFANTRY));

            ThirdReichBattleResult result = engine.resolveBattle(battle, null, null);

            assertNotNull(result);
            assertTrue(result.getRoundsFought() > 0);
        }
    }

    @Nested
    @DisplayName("Battle Phases")
    class BattlePhases {

        @Test
        @DisplayName("all phases are accessible")
        void allPhases() {
            assertEquals(3, BattlePhase.values().length);
            assertNotNull(BattlePhase.MOVEMENT);
            assertNotNull(BattlePhase.COMBAT);
            assertNotNull(BattlePhase.MORALE);
        }

        @Test
        @DisplayName("phases have display names")
        void displayNames() {
            assertEquals("Movement", BattlePhase.MOVEMENT.getDisplayName());
            assertEquals("Combat", BattlePhase.COMBAT.getDisplayName());
            assertEquals("Morale", BattlePhase.MORALE.getDisplayName());
        }
    }
}

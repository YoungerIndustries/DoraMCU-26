package com.yisy.modules.doradl.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ActionReward Tests")
class ActionRewardTest {

    @Nested
    @DisplayName("Action enum")
    class ActionEnum {

        @Test
        @DisplayName("All 17 action values exist")
        void allValues() {
            assertEquals(17, ActionReward.Action.values().length);
        }

        @Test
        @DisplayName("All action indices are correct")
        void indices() {
            assertEquals(0, ActionReward.Action.BUILD_SHIP.getIndex());
            assertEquals(1, ActionReward.Action.BUILD_STATION.getIndex());
            assertEquals(2, ActionReward.Action.EXPAND_COLONY.getIndex());
            assertEquals(3, ActionReward.Action.RESEARCH_TECH.getIndex());
            assertEquals(4, ActionReward.Action.DIPLOMATIC_STANCE.getIndex());
            assertEquals(5, ActionReward.Action.MOVE_FLEET.getIndex());
            assertEquals(6, ActionReward.Action.ENGAGE_COMBAT.getIndex());
            assertEquals(7, ActionReward.Action.RETREAT.getIndex());
            assertEquals(8, ActionReward.Action.FLANK_ENEMY.getIndex());
            assertEquals(9, ActionReward.Action.AMBUSH.getIndex());
            assertEquals(10, ActionReward.Action.OPTIMIZE_MINING.getIndex());
            assertEquals(11, ActionReward.Action.OPTIMIZE_TRADE.getIndex());
            assertEquals(12, ActionReward.Action.REPAIR_FLEET.getIndex());
            assertEquals(13, ActionReward.Action.UPGRADE_STATION.getIndex());
            assertEquals(14, ActionReward.Action.FORTIFY_PLANET.getIndex());
            assertEquals(15, ActionReward.Action.SCOUT_AREA.getIndex());
            assertEquals(16, ActionReward.Action.WAIT.getIndex());
        }

        @Test
        @DisplayName("fromIndex returns correct action for all valid indices")
        void fromIndexValid() {
            for (ActionReward.Action action : ActionReward.Action.values()) {
                assertSame(action, ActionReward.Action.fromIndex(action.getIndex()));
            }
        }

        @Test
        @DisplayName("fromIndex throws for invalid index")
        void fromIndexInvalid() {
            assertThrows(IllegalArgumentException.class, () -> ActionReward.Action.fromIndex(-1));
            assertThrows(IllegalArgumentException.class, () -> ActionReward.Action.fromIndex(17));
        }
    }

    @Nested
    @DisplayName("ActionCategory enum")
    class ActionCategoryEnum {

        @Test
        @DisplayName("All 4 category values exist")
        void allValues() {
            assertEquals(4, ActionReward.ActionCategory.values().length);
        }

        @Test
        @DisplayName("STRATEGIC category exists")
        void strategic() {
            assertSame(ActionReward.ActionCategory.STRATEGIC, ActionReward.ActionCategory.valueOf("STRATEGIC"));
        }

        @Test
        @DisplayName("TACTICAL category exists")
        void tactical() {
            assertSame(ActionReward.ActionCategory.TACTICAL, ActionReward.ActionCategory.valueOf("TACTICAL"));
        }

        @Test
        @DisplayName("ECONOMIC category exists")
        void economic() {
            assertSame(ActionReward.ActionCategory.ECONOMIC, ActionReward.ActionCategory.valueOf("ECONOMIC"));
        }

        @Test
        @DisplayName("DEFENSIVE category exists")
        void defensive() {
            assertSame(ActionReward.ActionCategory.DEFENSIVE, ActionReward.ActionCategory.valueOf("DEFENSIVE"));
        }
    }

    @Nested
    @DisplayName("Action.getCategory()")
    class ActionCategory {

        @Test
        @DisplayName("BUILD_SHIP through DIPLOMATIC_STANCE are STRATEGIC")
        void strategicActions() {
            assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.BUILD_SHIP.getCategory());
            assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.BUILD_STATION.getCategory());
            assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.EXPAND_COLONY.getCategory());
            assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.RESEARCH_TECH.getCategory());
            assertEquals(ActionReward.ActionCategory.STRATEGIC, ActionReward.Action.DIPLOMATIC_STANCE.getCategory());
        }

        @Test
        @DisplayName("MOVE_FLEET through AMBUSH are TACTICAL")
        void tacticalActions() {
            assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.MOVE_FLEET.getCategory());
            assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.ENGAGE_COMBAT.getCategory());
            assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.RETREAT.getCategory());
            assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.FLANK_ENEMY.getCategory());
            assertEquals(ActionReward.ActionCategory.TACTICAL, ActionReward.Action.AMBUSH.getCategory());
        }

        @Test
        @DisplayName("OPTIMIZE_MINING through UPGRADE_STATION are ECONOMIC")
        void economicActions() {
            assertEquals(ActionReward.ActionCategory.ECONOMIC, ActionReward.Action.OPTIMIZE_MINING.getCategory());
            assertEquals(ActionReward.ActionCategory.ECONOMIC, ActionReward.Action.OPTIMIZE_TRADE.getCategory());
            assertEquals(ActionReward.ActionCategory.ECONOMIC, ActionReward.Action.REPAIR_FLEET.getCategory());
            assertEquals(ActionReward.ActionCategory.ECONOMIC, ActionReward.Action.UPGRADE_STATION.getCategory());
        }

        @Test
        @DisplayName("FORTIFY_PLANET through WAIT are DEFENSIVE")
        void defensiveActions() {
            assertEquals(ActionReward.ActionCategory.DEFENSIVE, ActionReward.Action.FORTIFY_PLANET.getCategory());
            assertEquals(ActionReward.ActionCategory.DEFENSIVE, ActionReward.Action.SCOUT_AREA.getCategory());
            assertEquals(ActionReward.ActionCategory.DEFENSIVE, ActionReward.Action.WAIT.getCategory());
        }
    }

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            ActionReward reward = new ActionReward();
            assertNull(reward.getState());
            assertNull(reward.getAction());
            assertEquals(0.0, reward.getReward());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets state, action, reward")
        void setsFields() {
            GameStateFeatures state = new GameStateFeatures();
            ActionReward reward = new ActionReward(state, ActionReward.Action.BUILD_SHIP, 0.75);
            assertSame(state, reward.getState());
            assertEquals(ActionReward.Action.BUILD_SHIP, reward.getAction());
            assertEquals(0.75, reward.getReward());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get state")
        void state() {
            ActionReward reward = new ActionReward();
            GameStateFeatures state = new GameStateFeatures();
            reward.setState(state);
            assertSame(state, reward.getState());
        }

        @Test
        @DisplayName("Set and get action")
        void action() {
            ActionReward reward = new ActionReward();
            reward.setAction(ActionReward.Action.ENGAGE_COMBAT);
            assertEquals(ActionReward.Action.ENGAGE_COMBAT, reward.getAction());
        }

        @Test
        @DisplayName("Set and get reward")
        void reward() {
            ActionReward reward = new ActionReward();
            reward.setReward(-0.5);
            assertEquals(-0.5, reward.getReward());
        }
    }

    @Nested
    @DisplayName("toOneHot()")
    class ToOneHot {

        @Test
        @DisplayName("Creates vector with 1.0 at action index")
        void correctIndex() {
            ActionReward reward = new ActionReward();
            reward.setAction(ActionReward.Action.BUILD_SHIP);
            double[] oneHot = reward.toOneHot();
            assertEquals(17, oneHot.length);
            assertEquals(1.0, oneHot[0]);
            for (int i = 1; i < 17; i++) {
                assertEquals(0.0, oneHot[i]);
            }
        }

        @Test
        @DisplayName("WAIT action sets index 16")
        void waitAction() {
            ActionReward reward = new ActionReward();
            reward.setAction(ActionReward.Action.WAIT);
            double[] oneHot = reward.toOneHot();
            assertEquals(1.0, oneHot[16]);
            assertEquals(0.0, oneHot[0]);
        }
    }

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("Equal state, action, reward are equal")
        void equal() {
            GameStateFeatures state = new GameStateFeatures();
            ActionReward a = new ActionReward(state, ActionReward.Action.BUILD_SHIP, 0.5);
            ActionReward b = new ActionReward(state, ActionReward.Action.BUILD_SHIP, 0.5);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("Different action makes not equal")
        void differentAction() {
            GameStateFeatures state = new GameStateFeatures();
            ActionReward a = new ActionReward(state, ActionReward.Action.BUILD_SHIP, 0.5);
            ActionReward b = new ActionReward(state, ActionReward.Action.ENGAGE_COMBAT, 0.5);
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("Different reward makes not equal")
        void differentReward() {
            GameStateFeatures state = new GameStateFeatures();
            ActionReward a = new ActionReward(state, ActionReward.Action.BUILD_SHIP, 0.5);
            ActionReward b = new ActionReward(state, ActionReward.Action.BUILD_SHIP, 0.6);
            assertNotEquals(a, b);
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes action and reward")
        void containsActionAndReward() {
            GameStateFeatures state = new GameStateFeatures();
            ActionReward reward = new ActionReward(state, ActionReward.Action.BUILD_SHIP, 0.75);
            String result = reward.toString();
            assertTrue(result.contains("BUILD_SHIP"));
            assertTrue(result.contains("0.75"));
        }
    }
}

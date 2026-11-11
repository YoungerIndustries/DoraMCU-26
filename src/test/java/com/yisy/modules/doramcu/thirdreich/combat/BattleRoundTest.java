package com.yisy.modules.doramcu.thirdreich.combat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BattleRound")
class BattleRoundTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates round with number")
        void createsWithNumber() {
            BattleRound round = new BattleRound(1);

            assertEquals(1, round.getRoundNumber());
            assertEquals(0, round.getAttackerDamageDealt());
            assertEquals(0, round.getDefenderDamageDealt());
            assertEquals(0, round.getAttackerMoraleLoss());
            assertEquals(0, round.getDefenderMoraleLoss());
            assertEquals(0, round.getAttackerUnitsDestroyed());
            assertEquals(0, round.getDefenderUnitsDestroyed());
            assertTrue(round.getEvents().isEmpty());
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("set damage dealt")
        void setDamage() {
            BattleRound round = new BattleRound(1);
            round.setAttackerDamageDealt(50);
            round.setDefenderDamageDealt(30);

            assertEquals(50, round.getAttackerDamageDealt());
            assertEquals(30, round.getDefenderDamageDealt());
        }

        @Test
        @DisplayName("set morale loss")
        void setMorale() {
            BattleRound round = new BattleRound(1);
            round.setAttackerMoraleLoss(10);
            round.setDefenderMoraleLoss(15);

            assertEquals(10, round.getAttackerMoraleLoss());
            assertEquals(15, round.getDefenderMoraleLoss());
        }

        @Test
        @DisplayName("set units destroyed")
        void setUnitsDestroyed() {
            BattleRound round = new BattleRound(1);
            round.setAttackerUnitsDestroyed(2);
            round.setDefenderUnitsDestroyed(3);

            assertEquals(2, round.getAttackerUnitsDestroyed());
            assertEquals(3, round.getDefenderUnitsDestroyed());
        }
    }

    @Nested
    @DisplayName("Events")
    class Events {

        @Test
        @DisplayName("addEvent adds to list")
        void addEvent() {
            BattleRound round = new BattleRound(1);
            round.addEvent("Attacker strikes");
            round.addEvent("Defender counter-attacks");

            assertEquals(2, round.getEvents().size());
            assertEquals("Attacker strikes", round.getEvents().get(0));
        }
    }

    @Nested
    @DisplayName("ToString")
    class ToString {

        @Test
        @DisplayName("toString contains round info")
        void containsInfo() {
            BattleRound round = new BattleRound(3);
            round.setAttackerDamageDealt(50);
            round.setDefenderDamageDealt(30);
            String str = round.toString();

            assertTrue(str.contains("3"));
            assertTrue(str.contains("50"));
        }
    }

    @Nested
    @DisplayName("Side Enum")
    class SideEnum {

        @Test
        @DisplayName("has ATTACKER and DEFENDER")
        void hasSides() {
            assertEquals(2, BattleRound.Side.values().length);
            assertNotNull(BattleRound.Side.ATTACKER);
            assertNotNull(BattleRound.Side.DEFENDER);
        }
    }
}

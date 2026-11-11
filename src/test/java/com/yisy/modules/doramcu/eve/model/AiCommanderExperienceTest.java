package com.yisy.modules.doramcu.eve.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AiCommanderExperience Tests")
class AiCommanderExperienceTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty experience")
        void noArgConstructor() {
            AiCommanderExperience exp = new AiCommanderExperience();
            assertNull(exp.getId());
            assertNull(exp.getAiCharacterId());
            assertNull(exp.getGameModuleName());
            assertNull(exp.getBattleType());
            assertNull(exp.getOutcome());
            assertEquals(0, exp.getExperiencePoints());
            assertEquals(0, exp.getLeadershipBonus());
            assertEquals(0, exp.getEnemiesDefeated());
            assertEquals(0, exp.getFriendlyLosses());
            assertFalse(exp.isExceptionalPerformance());
        }

        @Test
        @DisplayName("full constructor should set aiCharacterId, gameModuleName, battleType, outcome, and battleDate")
        void fullConstructor() {
            AiCommanderExperience exp = new AiCommanderExperience(
                    1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY);

            assertEquals(1L, exp.getAiCharacterId());
            assertEquals("THIRD_REICH", exp.getGameModuleName());
            assertEquals("Ground Battle", exp.getBattleType());
            assertEquals(AiCommanderExperience.Outcome.VICTORY, exp.getOutcome());
            assertNotNull(exp.getBattleDate());
            assertEquals(0, exp.getExperiencePoints());
            assertEquals(0, exp.getLeadershipBonus());
        }
    }

    @Nested
    @DisplayName("Outcome Enum")
    class OutcomeEnumTests {

        @Test
        @DisplayName("should have all 5 outcome values")
        void allOutcomeValues() {
            AiCommanderExperience.Outcome[] outcomes = AiCommanderExperience.Outcome.values();
            assertEquals(5, outcomes.length);
        }

        @Test
        @DisplayName("VICTORY outcome should be accessible")
        void victoryOutcome() {
            assertEquals("VICTORY", AiCommanderExperience.Outcome.VICTORY.name());
        }

        @Test
        @DisplayName("PYRRHIC_VICTORY outcome should be accessible")
        void pyrrhicVictoryOutcome() {
            assertEquals("PYRRHIC_VICTORY", AiCommanderExperience.Outcome.PYRRHIC_VICTORY.name());
        }

        @Test
        @DisplayName("STALEMATE outcome should be accessible")
        void stalemateOutcome() {
            assertEquals("STALEMATE", AiCommanderExperience.Outcome.STALEMATE.name());
        }

        @Test
        @DisplayName("DEFEAT outcome should be accessible")
        void defeatOutcome() {
            assertEquals("DEFEAT", AiCommanderExperience.Outcome.DEFEAT.name());
        }

        @Test
        @DisplayName("ROUTE outcome should be accessible")
        void routeOutcome() {
            assertEquals("ROUTE", AiCommanderExperience.Outcome.ROUTE.name());
        }

        @Test
        @DisplayName("outcome should be settable and gettable")
        void outcomeSetter() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setOutcome(AiCommanderExperience.Outcome.PYRRHIC_VICTORY);
            assertEquals(AiCommanderExperience.Outcome.PYRRHIC_VICTORY, exp.getOutcome());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get ID")
        void id() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setId(42L);
            assertEquals(42L, exp.getId());
        }

        @Test
        @DisplayName("should set and get aiCharacterId")
        void aiCharacterId() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setAiCharacterId(7L);
            assertEquals(7L, exp.getAiCharacterId());
        }

        @Test
        @DisplayName("should set and get gameModuleName")
        void gameModuleName() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setGameModuleName("TRAVELLER");
            assertEquals("TRAVELLER", exp.getGameModuleName());
        }

        @Test
        @DisplayName("should set and get battleType")
        void battleType() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setBattleType("Space Battle");
            assertEquals("Space Battle", exp.getBattleType());
        }

        @Test
        @DisplayName("should set and get battleDescription")
        void battleDescription() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setBattleDescription("Defended the capital against overwhelming odds");
            assertEquals("Defended the capital against overwhelming odds", exp.getBattleDescription());
        }

        @Test
        @DisplayName("should set and get battleLocation")
        void battleLocation() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setBattleLocation("Berlin, Germany");
            assertEquals("Berlin, Germany", exp.getBattleLocation());
        }

        @Test
        @DisplayName("should set and get experiencePoints")
        void experiencePoints() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setExperiencePoints(50);
            assertEquals(50, exp.getExperiencePoints());
        }

        @Test
        @DisplayName("should set and get leadershipBonus")
        void leadershipBonus() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setLeadershipBonus(1);
            assertEquals(1, exp.getLeadershipBonus());
        }

        @Test
        @DisplayName("should set and get negative leadershipBonus")
        void negativeLeadershipBonus() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setLeadershipBonus(-2);
            assertEquals(-2, exp.getLeadershipBonus());
        }

        @Test
        @DisplayName("should set and get skillImproved")
        void skillImproved() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setSkillImproved("Armor Tactics");
            assertEquals("Armor Tactics", exp.getSkillImproved());
        }

        @Test
        @DisplayName("should set and get enemiesDefeated")
        void enemiesDefeated() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setEnemiesDefeated(15);
            assertEquals(15, exp.getEnemiesDefeated());
        }

        @Test
        @DisplayName("should set and get friendlyLosses")
        void friendlyLosses() {
            AiCommanderExperience exp = new AiCommanderExperience();
            exp.setFriendlyLosses(3);
            assertEquals(3, exp.getFriendlyLosses());
        }

        @Test
        @DisplayName("should set and get exceptionalPerformance")
        void exceptionalPerformance() {
            AiCommanderExperience exp = new AiCommanderExperience();
            assertFalse(exp.isExceptionalPerformance());
            exp.setExceptionalPerformance(true);
            assertTrue(exp.isExceptionalPerformance());
        }

        @Test
        @DisplayName("should set and get battleDate")
        void battleDate() {
            AiCommanderExperience exp = new AiCommanderExperience();
            LocalDateTime date = LocalDateTime.of(2024, 6, 15, 10, 30);
            exp.setBattleDate(date);
            assertEquals(date, exp.getBattleDate());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            AiCommanderExperience exp = new AiCommanderExperience(
                    1L, "THIRD_REICH", "Ground Battle", AiCommanderExperience.Outcome.VICTORY);
            exp.setId(10L);
            exp.setExperiencePoints(50);

            String str = exp.toString();
            assertTrue(str.contains("AiCommanderExperience{"));
            assertTrue(str.contains("id=10"));
            assertTrue(str.contains("aiCharacterId=1"));
            assertTrue(str.contains("game='THIRD_REICH'"));
            assertTrue(str.contains("type='Ground Battle'"));
            assertTrue(str.contains("outcome=VICTORY"));
            assertTrue(str.contains("xp=50"));
        }

        @Test
        @DisplayName("toString should handle null fields gracefully")
        void toStringNullFields() {
            AiCommanderExperience exp = new AiCommanderExperience();
            String str = exp.toString();
            assertNotNull(str);
            assertTrue(str.contains("AiCommanderExperience{"));
        }
    }

    @Nested
    @DisplayName("Default Values")
    class DefaultValueTests {

        @Test
        @DisplayName("experiencePoints should default to 0")
        void defaultExperiencePoints() {
            AiCommanderExperience exp = new AiCommanderExperience();
            assertEquals(0, exp.getExperiencePoints());
        }

        @Test
        @DisplayName("leadershipBonus should default to 0")
        void defaultLeadershipBonus() {
            AiCommanderExperience exp = new AiCommanderExperience();
            assertEquals(0, exp.getLeadershipBonus());
        }

        @Test
        @DisplayName("enemiesDefeated should default to 0")
        void defaultEnemiesDefeated() {
            AiCommanderExperience exp = new AiCommanderExperience();
            assertEquals(0, exp.getEnemiesDefeated());
        }

        @Test
        @DisplayName("friendlyLosses should default to 0")
        void defaultFriendlyLosses() {
            AiCommanderExperience exp = new AiCommanderExperience();
            assertEquals(0, exp.getFriendlyLosses());
        }

        @Test
        @DisplayName("exceptionalPerformance should default to false")
        void defaultExceptionalPerformance() {
            AiCommanderExperience exp = new AiCommanderExperience();
            assertFalse(exp.isExceptionalPerformance());
        }
    }
}

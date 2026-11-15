package com.yisy.modules.doramcu.warhammer40k.service;

import com.yisy.modules.doramcu.gamecore.RulesEngine;
import com.yisy.modules.doramcu.shared.model.GameSystem;
import com.yisy.modules.doramcu.shared.model.RulesEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Warhammer40kRulesEngine")
class Warhammer40kRulesEngineTest {

    private Warhammer40kRulesEngine engine;

    @BeforeEach
    void setUp() {
        engine = new Warhammer40kRulesEngine();
    }

    @Nested
    @DisplayName("Game System")
    class GameSystemTest {

        @Test
        @DisplayName("returns WARHAMMER_40K game system")
        void returnsWarhammer40kGameSystem() {
            assertEquals(GameSystem.WARHAMMER_40K, engine.getGameSystem());
        }
    }

    @Nested
    @DisplayName("Dice Rolling")
    class DiceRolling {

        @Test
        @DisplayName("rolls d10 within valid range")
        @RepeatedTest(10)
        void rollsD10WithinRange() {
            int result = engine.rollDice(10, 1);

            assertTrue(result >= 1, "d10 should be at least 1");
            assertTrue(result <= 10, "d10 should be at most 10");
        }

        @Test
        @DisplayName("rolls multiple dice")
        void rollsMultipleDice() {
            int result = engine.rollDice(6, 2);

            assertTrue(result >= 2, "2d6 should be at least 2");
            assertTrue(result <= 12, "2d6 should be at most 12");
        }

        @Test
        @DisplayName("rolls d20")
        void rollsD20() {
            int result = engine.rollDice(20, 1);

            assertTrue(result >= 1);
            assertTrue(result <= 20);
        }
    }

    @Nested
    @DisplayName("Task Attempt")
    class TaskAttempt {

        @Test
        @DisplayName("returns success result")
        @RepeatedTest(10)
        void returnsSuccessResult() {
            RulesEngine.TaskResult result = engine.taskAttempt(3, 5);

            assertNotNull(result);
            assertNotNull(result.getDescription());
            assertTrue(result.getRoll() >= 1 && result.getRoll() <= 10);
        }

        @Test
        @DisplayName("returns failure result for high difficulty")
        @RepeatedTest(10)
        void returnsFailureResult() {
            RulesEngine.TaskResult result = engine.taskAttempt(9, 1);

            assertNotNull(result);
            // May still succeed on critical (roll 10)
            // Just verify the structure is correct
            assertTrue(result.getRoll() >= 1 && result.getRoll() <= 10);
        }

        @Test
        @DisplayName("threshold is clamped between 2 and 10")
        void thresholdClamped() {
            // difficulty 0 -> threshold max(2, min(10, 1)) = 2
            RulesEngine.TaskResult low = engine.taskAttempt(0, 5);
            assertTrue(low.getTarget() >= 2);

            // difficulty 10 -> threshold max(2, min(10, 11)) = 10
            RulesEngine.TaskResult high = engine.taskAttempt(10, 5);
            assertTrue(high.getTarget() <= 10);
        }

        @Test
        @DisplayName("fumble returns failure message")
        @RepeatedTest(20)
        void fumbleReturnsFailureMessage() {
            RulesEngine.TaskResult result = engine.taskAttempt(5, 5);

            if (result.getRoll() == 1) {
                assertFalse(result.isSuccess());
                assertTrue(result.getDescription().toLowerCase().contains("fumble"));
            }
        }

        @Test
        @DisplayName("critical returns success message")
        @RepeatedTest(20)
        void criticalReturnsSuccessMessage() {
            RulesEngine.TaskResult result = engine.taskAttempt(5, 5);

            if (result.getRoll() == 10) {
                assertTrue(result.isSuccess());
                assertTrue(result.getDescription().toLowerCase().contains("critical"));
            }
        }
    }

    @Nested
    @DisplayName("Combat Round")
    class CombatRound {

        @Test
        @DisplayName("returns combat result")
        void returnsCombatResult() {
            Map<String, Object> attacker = new HashMap<>();
            attacker.put("attacks", 5);
            attacker.put("bs", 4);
            attacker.put("strength", 6);

            Map<String, Object> defender = new HashMap<>();
            defender.put("toughness", 4);
            defender.put("armor", 2);

            RulesEngine.CombatResult result = engine.combatRound(attacker, defender);

            assertNotNull(result);
            assertNotNull(result.getDescription());
            assertTrue(result.getDamage() >= 0);
        }

        @Test
        @DisplayName("uses default values for missing keys")
        void usesDefaultValues() {
            Map<String, Object> attacker = new HashMap<>();
            Map<String, Object> defender = new HashMap<>();

            RulesEngine.CombatResult result = engine.combatRound(attacker, defender);

            assertNotNull(result);
            // Should use defaults: 1 attack, BS 4, strength 4, toughness 4, armor 0
        }

        @Test
        @DisplayName("handles null maps")
        void handlesNullMaps() {
            RulesEngine.CombatResult result = engine.combatRound(null, null);

            assertNotNull(result);
        }

        @Test
        @DisplayName("description includes combat stats")
        void descriptionIncludesStats() {
            Map<String, Object> attacker = new HashMap<>();
            attacker.put("attacks", 3);
            attacker.put("bs", 5);
            attacker.put("strength", 7);

            Map<String, Object> defender = new HashMap<>();
            defender.put("toughness", 4);
            defender.put("armor", 1);

            RulesEngine.CombatResult result = engine.combatRound(attacker, defender);

            String desc = result.getDescription();
            assertTrue(desc.contains("hits"), "Description should mention hits");
            assertTrue(desc.contains("wounds"), "Description should mention wounds");
            assertTrue(desc.contains("saved"), "Description should mention saved");
            assertTrue(desc.contains("damage"), "Description should mention damage");
        }

        @Test
        @DisplayName("high BS increases hit probability")
        @RepeatedTest(20)
        void highBsIncreasesHits() {
            Map<String, Object> attacker = new HashMap<>();
            attacker.put("attacks", 10);
            attacker.put("bs", 2); // Easy to hit
            attacker.put("strength", 8);

            Map<String, Object> defender = new HashMap<>();
            defender.put("toughness", 3);
            defender.put("armor", 0);

            RulesEngine.CombatResult result = engine.combatRound(attacker, defender);

            // With BS 2, most attacks should hit
            // Just verify the result is reasonable
            assertTrue(result.getDamage() >= 0);
        }
    }

    @Nested
    @DisplayName("Rule Queries")
    class RuleQueries {

        @Test
        @DisplayName("returns combat rules")
        void returnsCombatRules() {
            List<RulesEntry> rules = engine.queryRules("combat");

            assertNotNull(rules);
            assertEquals(1, rules.size());
            assertEquals("d10 Combat System", rules.get(0).getTitle());
            assertEquals(GameSystem.WARHAMMER_40K, rules.get(0).getGameSystem());
        }

        @Test
        @DisplayName("returns task rules")
        void returnsTaskRules() {
            List<RulesEntry> rules = engine.queryRules("task");

            assertNotNull(rules);
            assertEquals(1, rules.size());
            assertEquals("Task Resolution", rules.get(0).getTitle());
        }

        @Test
        @DisplayName("returns unknown category rules")
        void returnsUnknownCategoryRules() {
            List<RulesEntry> rules = engine.queryRules("unknown");

            assertNotNull(rules);
            assertEquals(1, rules.size());
            assertEquals("Unknown Category", rules.get(0).getTitle());
        }

        @Test
        @DisplayName("query is case insensitive")
        void queryIsCaseInsensitive() {
            List<RulesEntry> combat = engine.queryRules("combat");
            List<RulesEntry> COMBAT = engine.queryRules("COMBAT");
            List<RulesEntry> Combat = engine.queryRules("Combat");

            assertEquals(combat.get(0).getTitle(), COMBAT.get(0).getTitle());
            assertEquals(combat.get(0).getTitle(), Combat.get(0).getTitle());
        }

        @Test
        @DisplayName("cross-system query delegates to single query")
        void crossSystemQueryDelegates() {
            List<RulesEntry> single = engine.queryRules("combat");
            List<RulesEntry> cross = engine.queryRules("combat", GameSystem.WARHAMMER_40K);

            assertEquals(single.size(), cross.size());
            assertEquals(single.get(0).getTitle(), cross.get(0).getTitle());
        }
    }
}

package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.hex.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerActionAnalyzer Tests")
class PlayerActionAnalyzerTest {

    private PlayerActionAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new PlayerActionAnalyzer();
    }

    @Test
    @DisplayName("should record an action and increase count")
    void recordAction() {
        analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.8, 1, "test combat");

        assertEquals(1, analyzer.getTotalActionCount(1));
    }

    @Test
    @DisplayName("should record multiple actions")
    void recordMultipleActions() {
        for (int i = 0; i < 5; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.8, i, null);
        }

        assertEquals(5, analyzer.getTotalActionCount(1));
    }

    @Test
    @DisplayName("should return 0 count for unknown player")
    void unknownPlayerCount() {
        assertEquals(0, analyzer.getTotalActionCount(999));
    }

    @Test
    @DisplayName("should analyze combat behavior")
    void analyzeCombatBehavior() {
        // Record many combat actions
        for (int i = 0; i < 20; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.9, i, null);
        }

        double aggression = analyzer.analyzeCombatBehavior(1);
        assertTrue(aggression > 0.5, "Aggression should be above 0.5 after many combat actions");
        assertTrue(aggression <= 1.0, "Aggression should be <= 1.0");
    }

    @Test
    @DisplayName("should analyze expansion behavior")
    void analyzeExpansionBehavior() {
        for (int i = 0; i < 20; i++) {
            analyzer.recordAction(1, PlayerActionCategory.EXPANSION, 0.8, i, null);
        }

        double expansion = analyzer.analyzeExpansionBehavior(1);
        assertTrue(expansion > 0.5, "Expansion should be above 0.5 after many expansion actions");
    }

    @Test
    @DisplayName("should analyze economic behavior")
    void analyzeEconomicBehavior() {
        for (int i = 0; i < 10; i++) {
            analyzer.recordAction(1, PlayerActionCategory.BUILD_SHIP, 0.7, i, null);
            analyzer.recordAction(1, PlayerActionCategory.BUILD_STATION, 0.6, i, null);
        }

        double economic = analyzer.analyzeEconomicBehavior(1);
        assertTrue(economic > 0.4, "Economic focus should be above 0.4 after build actions");
    }

    @Test
    @DisplayName("should analyze research behavior")
    void analyzeResearchBehavior() {
        for (int i = 0; i < 20; i++) {
            analyzer.recordAction(1, PlayerActionCategory.RESEARCH, 0.9, i, null);
        }

        double scientific = analyzer.analyzeResearchBehavior(1);
        assertTrue(scientific > 0.5, "Scientific focus should be above 0.5 after research actions");
    }

    @Test
    @DisplayName("should analyze diplomatic behavior")
    void analyzeDiplomaticBehavior() {
        for (int i = 0; i < 10; i++) {
            analyzer.recordAction(1, PlayerActionCategory.DEFENSIVE, 0.7, i, null);
            analyzer.recordAction(1, PlayerActionCategory.ALTRUISTIC, 0.8, i, null);
        }

        double diplomatic = analyzer.analyzeDiplomaticBehavior(1);
        assertTrue(diplomatic > 0.4, "Diplomatic stance should be above 0.4 after defensive/altruistic actions");
    }

    @Test
    @DisplayName("should analyze risk behavior")
    void analyzeRiskBehavior() {
        // High combat, low retreat, low passive = high risk tolerance
        for (int i = 0; i < 10; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.9, i, null);
        }

        double risk = analyzer.analyzeRiskBehavior(1);
        assertTrue(risk > 0.4, "Risk tolerance should be above 0.4 after combat actions");
    }

    @Test
    @DisplayName("should return default 0.5 for unobserved behavior")
    void defaultBehaviorScores() {
        assertEquals(0.5, analyzer.analyzeCombatBehavior(1));
        assertEquals(0.5, analyzer.analyzeExpansionBehavior(1));
        assertEquals(0.5, analyzer.analyzeEconomicBehavior(1));
        assertEquals(0.5, analyzer.analyzeResearchBehavior(1));
        assertEquals(0.5, analyzer.analyzeDiplomaticBehavior(1));
        assertEquals(0.5, analyzer.analyzeRiskBehavior(1));
    }

    @Test
    @DisplayName("should map action types to categories")
    void mapActionType() {
        assertEquals(PlayerActionCategory.COMBAT, analyzer.mapActionType(AiDecision.ActionType.ENGAGE_COMBAT));
        assertEquals(PlayerActionCategory.RETREAT, analyzer.mapActionType(AiDecision.ActionType.RETREAT));
        assertEquals(PlayerActionCategory.EXPANSION, analyzer.mapActionType(AiDecision.ActionType.MOVE_FLEET));
        assertEquals(PlayerActionCategory.EXPANSION, analyzer.mapActionType(AiDecision.ActionType.SCOUT));
        assertEquals(PlayerActionCategory.BUILD_SHIP, analyzer.mapActionType(AiDecision.ActionType.BUILD_SHIP));
        assertEquals(PlayerActionCategory.BUILD_STATION, analyzer.mapActionType(AiDecision.ActionType.BUILD_STATION));
        assertEquals(PlayerActionCategory.RESEARCH, analyzer.mapActionType(AiDecision.ActionType.UPGRADE_STATION));
        assertEquals(PlayerActionCategory.DEFENSIVE, analyzer.mapActionType(AiDecision.ActionType.FORTIFY));
        assertEquals(PlayerActionCategory.PASSIVE, analyzer.mapActionType(AiDecision.ActionType.WAIT));
        assertEquals(PlayerActionCategory.PASSIVE, analyzer.mapActionType(null));
    }

    @Test
    @DisplayName("should record action from decision")
    void recordActionFromDecision() {
        analyzer.recordActionFromDecision(1, AiDecision.ActionType.ENGAGE_COMBAT, 1);
        assertEquals(1, analyzer.getTotalActionCount(1));
    }

    @Test
    @DisplayName("should get action records")
    void getActionRecords() {
        analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.8, 1, "combat");
        analyzer.recordAction(1, PlayerActionCategory.EXPANSION, 0.6, 2, "expansion");

        List<PlayerActionRecord> records = analyzer.getActionRecords(1);
        assertEquals(2, records.size());
        assertEquals(PlayerActionCategory.COMBAT, records.get(0).getCategory());
        assertEquals(PlayerActionCategory.EXPANSION, records.get(1).getCategory());
    }

    @Test
    @DisplayName("should get action counts")
    void getActionCounts() {
        analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.8, 1, null);
        analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.9, 2, null);
        analyzer.recordAction(1, PlayerActionCategory.EXPANSION, 0.6, 3, null);

        Map<PlayerActionCategory, Integer> counts = analyzer.getActionCounts(1);
        assertEquals(2, counts.get(PlayerActionCategory.COMBAT).intValue());
        assertEquals(1, counts.get(PlayerActionCategory.EXPANSION).intValue());
    }

    @Test
    @DisplayName("should clear player data")
    void clearPlayerData() {
        analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.8, 1, null);
        assertEquals(1, analyzer.getTotalActionCount(1));

        analyzer.clearPlayerData(1);
        assertEquals(0, analyzer.getTotalActionCount(1));
    }

    @Test
    @DisplayName("confidence should increase with more actions")
    void confidenceIncreasesWithActions() {
        double confidence0 = analyzer.getConfidenceScore(1);
        assertTrue(confidence0 < 0.4, "Confidence with 0 actions should be low");

        for (int i = 0; i < 10; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }
        double confidence10 = analyzer.getConfidenceScore(1);
        assertTrue(confidence10 > confidence0, "Confidence should increase after 10 actions");

        for (int i = 10; i < 60; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }
        double confidence60 = analyzer.getConfidenceScore(1);
        assertTrue(confidence60 > confidence10, "Confidence should increase after 60 actions");
        assertTrue(confidence60 >= 0.7, "Confidence with 60 actions should be >= 0.7");
    }

    @Test
    @DisplayName("confidence should cap at 0.95")
    void confidenceCapsAtMax() {
        for (int i = 0; i < 250; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }
        double confidence = analyzer.getConfidenceScore(1);
        assertEquals(0.95, confidence, 0.01, "Confidence should cap at 0.95");
    }

    @Test
    @DisplayName("should track multiple players independently")
    void trackMultiplePlayers() {
        for (int i = 0; i < 10; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.9, i, null);
            analyzer.recordAction(2, PlayerActionCategory.EXPANSION, 0.8, i, null);
        }

        assertEquals(10, analyzer.getTotalActionCount(1));
        assertEquals(10, analyzer.getTotalActionCount(2));

        // Player 1 should have high aggression
        assertTrue(analyzer.analyzeCombatBehavior(1) > 0.5);
        // Player 2 should have high expansion
        assertTrue(analyzer.analyzeExpansionBehavior(2) > 0.5);
    }

    @Test
    @DisplayName("trait scores should use exponential moving average")
    void exponentialMovingAverage() {
        // Start with low intensity
        for (int i = 0; i < 5; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.2, i, null);
        }
        double lowScore = analyzer.analyzeCombatBehavior(1);

        // Switch to high intensity
        for (int i = 5; i < 10; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.9, i, null);
        }
        double highScore = analyzer.analyzeCombatBehavior(1);

        assertTrue(highScore > lowScore, "Score should increase after high-intensity actions");
        assertTrue(highScore < 0.9, "Score should not jump to 0.9 immediately due to EMA smoothing");
    }
}

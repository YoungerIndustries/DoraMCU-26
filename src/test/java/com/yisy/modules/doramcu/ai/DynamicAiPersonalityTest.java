package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.babylon5.model.B5Personality;
import com.yisy.modules.doramcu.hex.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DynamicAiPersonality Tests")
class DynamicAiPersonalityTest {

    private Player player;
    private PlayerActionAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        player = new Player(1, "test@test.com", "Test Empire");
        player.setGovernmentType(Player.GovernmentType.DICTATORSHIP);
        player.setEthos(Player.Ethos.MILITARIST);
        analyzer = new PlayerActionAnalyzer();
    }

    @Test
    @DisplayName("should create from player with base personality")
    void createFromPlayer() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);

        assertNotNull(dynamic);
        assertEquals(Player.GovernmentType.DICTATORSHIP, dynamic.getGovernmentType());
        assertEquals(Player.Ethos.MILITARIST, dynamic.getEthos());
        assertEquals("DICTATORSHIP_MILITARIST", dynamic.getLabel());
    }

    @Test
    @DisplayName("should return base traits when no observed data")
    void baseTraitsWhenNoObserved() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);

        // With 0 actions, should return base personality values
        AiPersonality base = AiPersonality.fromPlayer(player);
        assertEquals(base.getAggression(), dynamic.getAggression(), 0.01);
        assertEquals(base.getExpansionDrive(), dynamic.getExpansionDrive(), 0.01);
        assertEquals(base.getEconomicFocus(), dynamic.getEconomicFocus(), 0.01);
    }

    @Test
    @DisplayName("should blend base and observed traits")
    void blendBaseAndObserved() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);

        // Record many combat actions to shift aggression
        for (int i = 0; i < 100; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.3, i, null);
        }

        dynamic.updateFromAnalyzer(analyzer, 1);

        // With 100 actions, observed weight should be ~0.75
        // Base aggression for DICTATORSHIP+MILITARIST is 0.9
        // Observed aggression is ~0.3 (low intensity combat)
        // Blended should be between base and observed
        double blended = dynamic.getAggression();
        assertTrue(blended > 0.3 && blended < 0.9,
                "Blended aggression should be between base (0.9) and observed (0.3), got: " + blended);
    }

    @Test
    @DisplayName("should increase observed weight with more actions")
    void observedWeightIncreasesWithActions() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);

        // Record low-intensity combat actions
        for (int i = 0; i < 5; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.1, i, null);
        }
        dynamic.updateFromAnalyzer(analyzer, 1);
        double aggression5 = dynamic.getAggression();

        // Record more actions
        for (int i = 5; i < 250; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.1, i, null);
        }
        dynamic.updateFromAnalyzer(analyzer, 1);
        double aggression250 = dynamic.getAggression();

        // With more actions, observed weight increases, so aggression should be closer to 0.1
        assertTrue(aggression250 < aggression5,
                "Aggression should decrease toward observed value with more actions");
    }

    @Test
    @DisplayName("should update single observed trait")
    void updateObservedTrait() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);
        dynamic.updateObservedTrait("aggression", 0.8);

        // With 0 actions, observed weight is low, so blended should be close to base
        // But the observed trait should be stored
        assertTrue(dynamic.getAggression() >= 0.0 && dynamic.getAggression() <= 1.0);
    }

    @Test
    @DisplayName("should clamp observed trait values to [0, 1]")
    void clampObservedTraitValues() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);
        dynamic.updateObservedTrait("aggression", -0.5);
        dynamic.updateObservedTrait("expansionDrive", 1.5);

        // Values should be clamped
        assertTrue(dynamic.getAggression() >= 0.0);
        assertTrue(dynamic.getExpansionDrive() <= 1.0);
    }

    @Test
    @DisplayName("confidence score should match action count")
    void confidenceScoreMatchesActionCount() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);

        assertEquals(0.1, dynamic.getConfidenceScore(), 0.01, "Confidence with 0 actions should be ~0.1");

        for (int i = 0; i < 10; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }
        dynamic.updateFromAnalyzer(analyzer, 1);
        assertTrue(dynamic.getConfidenceScore() >= 0.4, "Confidence with 10 actions should be >= 0.4");

        for (int i = 10; i < 250; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }
        dynamic.updateFromAnalyzer(analyzer, 1);
        assertEquals(0.9, dynamic.getConfidenceScore(), 0.01, "Confidence with 250 actions should be 0.9");
    }

    @Test
    @DisplayName("should convert to B5Personality")
    void toB5Personality() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);

        for (int i = 0; i < 50; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.8, i, null);
        }
        dynamic.updateFromAnalyzer(analyzer, 1);

        B5Personality b5 = dynamic.toB5Personality();

        assertNotNull(b5);
        // Core axes should match dynamic personality
        assertEquals(dynamic.getAggression(), b5.getAggression(), 0.01);
        assertEquals(dynamic.getDiplomaticStance(), b5.getDiplomacy(), 0.01);
        assertEquals(dynamic.getEconomicFocus(), b5.getEconomicFocus(), 0.01);
        assertEquals(dynamic.getScientificFocus(), b5.getScientificFocus(), 0.01);
        assertEquals(dynamic.getExpansionDrive(), b5.getExpansionDrive(), 0.01);
        assertEquals(dynamic.getRiskTolerance(), b5.getRiskTolerance(), 0.01);
        // Extra axes should default to 0.5
        assertEquals(0.5, b5.getAltruism(), 0.01);
        assertEquals(0.5, b5.getDetermination(), 0.01);
        assertEquals(0.5, b5.getAdaptability(), 0.01);
        assertEquals(0.5, b5.getLeadership(), 0.01);
        assertEquals(0.5, b5.getCunning(), 0.01);
        assertEquals(0.5, b5.getHonor(), 0.01);
    }

    @Test
    @DisplayName("should return all axes in toMap")
    void toMap() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);

        var map = dynamic.toMap();
        assertEquals(6, map.size());
        assertTrue(map.containsKey("aggression"));
        assertTrue(map.containsKey("expansionDrive"));
        assertTrue(map.containsKey("economicFocus"));
        assertTrue(map.containsKey("scientificFocus"));
        assertTrue(map.containsKey("diplomaticStance"));
        assertTrue(map.containsKey("riskTolerance"));
    }

    @Test
    @DisplayName("all blended values should be in [0, 1] range")
    void allValuesInRange() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);

        for (int i = 0; i < 100; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }
        dynamic.updateFromAnalyzer(analyzer, 1);

        assertTrue(dynamic.getAggression() >= 0.0 && dynamic.getAggression() <= 1.0);
        assertTrue(dynamic.getExpansionDrive() >= 0.0 && dynamic.getExpansionDrive() <= 1.0);
        assertTrue(dynamic.getEconomicFocus() >= 0.0 && dynamic.getEconomicFocus() <= 1.0);
        assertTrue(dynamic.getScientificFocus() >= 0.0 && dynamic.getScientificFocus() <= 1.0);
        assertTrue(dynamic.getDiplomaticStance() >= 0.0 && dynamic.getDiplomaticStance() <= 1.0);
        assertTrue(dynamic.getRiskTolerance() >= 0.0 && dynamic.getRiskTolerance() <= 1.0);
    }

    @Test
    @DisplayName("should create from existing AiPersonality")
    void createFromAiPersonality() {
        AiPersonality base = new AiPersonality(Player.GovernmentType.DEMOCRACY, Player.Ethos.PACIFIST);
        DynamicAiPersonality dynamic = new DynamicAiPersonality(base);

        assertEquals(Player.GovernmentType.DEMOCRACY, dynamic.getGovernmentType());
        assertEquals(Player.Ethos.PACIFIST, dynamic.getEthos());
    }

    @Test
    @DisplayName("toString should include confidence and action count")
    void toStringIncludesInfo() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);

        for (int i = 0; i < 50; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }
        dynamic.updateFromAnalyzer(analyzer, 1);

        String str = dynamic.toString();
        assertTrue(str.contains("confidence="));
        assertTrue(str.contains("actions="));
        assertTrue(str.contains("50"));
    }

    @Test
    @DisplayName("observed action count should be tracked")
    void observedActionCount() {
        DynamicAiPersonality dynamic = new DynamicAiPersonality(player);
        assertEquals(0, dynamic.getObservedActionCount());

        for (int i = 0; i < 30; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }
        dynamic.updateFromAnalyzer(analyzer, 1);
        assertEquals(30, dynamic.getObservedActionCount());
    }
}

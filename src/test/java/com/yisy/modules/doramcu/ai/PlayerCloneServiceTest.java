package com.yisy.modules.doramcu.ai;

import com.yisy.modules.doramcu.babylon5.model.B5Personality;
import com.yisy.modules.doramcu.hex.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PlayerCloneService Tests")
class PlayerCloneServiceTest {

    private PlayerActionAnalyzer analyzer;
    private PlayerCloneService cloneService;
    private Player player;

    @BeforeEach
    void setUp() {
        analyzer = new PlayerActionAnalyzer();
        cloneService = new PlayerCloneService(analyzer);
        player = new Player(1, "test@test.com", "Test Empire");
        player.setGovernmentType(Player.GovernmentType.DEMOCRACY);
        player.setEthos(Player.Ethos.MATERIALIST);
    }

    @Test
    @DisplayName("should build personality from player")
    void buildPersonality() {
        DynamicAiPersonality personality = cloneService.buildPersonality(player);

        assertNotNull(personality);
        assertEquals(Player.GovernmentType.DEMOCRACY, personality.getGovernmentType());
        assertEquals(Player.Ethos.MATERIALIST, personality.getEthos());
    }

    @Test
    @DisplayName("should return null for null player")
    void buildPersonalityNullPlayer() {
        assertNull(cloneService.buildPersonality(null));
    }

    @Test
    @DisplayName("should cache personality")
    void cachePersonality() {
        DynamicAiPersonality p1 = cloneService.buildPersonality(player);
        DynamicAiPersonality p2 = cloneService.getOrCreatePersonality(player);

        assertNotNull(p1);
        assertNotNull(p2);
        // Both should have the same base personality
        assertEquals(p1.getGovernmentType(), p2.getGovernmentType());
    }

    @Test
    @DisplayName("should refresh personality with new data")
    void refreshPersonality() {
        cloneService.buildPersonality(player);

        // Record actions
        for (int i = 0; i < 50; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.8, i, null);
        }

        cloneService.refreshPersonality(1);

        DynamicAiPersonality personality = cloneService.getOrCreatePersonality(player);
        assertEquals(50, personality.getObservedActionCount());
    }

    @Test
    @DisplayName("should create EmpireAiCore clone")
    void createClone() {
        for (int i = 0; i < 30; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.7, i, null);
        }

        EmpireAiCore clone = cloneService.createClone(100, player, 5);

        assertNotNull(clone);
        assertEquals(100, clone.getId());
        assertNotNull(clone.getDynamicPersonality());
        assertEquals(30, clone.getDynamicPersonality().getObservedActionCount());
    }

    @Test
    @DisplayName("should return null clone for null player")
    void createCloneNullPlayer() {
        assertNull(cloneService.createClone(100, null, 5));
    }

    @Test
    @DisplayName("should create B5Personality")
    void createB5Personality() {
        for (int i = 0; i < 50; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.8, i, null);
        }

        B5Personality b5 = cloneService.createB5Personality(player);

        assertNotNull(b5);
        assertTrue(b5.getAggression() > 0.0);
    }

    @Test
    @DisplayName("should return null B5Personality for null player")
    void createB5PersonalityNullPlayer() {
        assertNull(cloneService.createB5Personality(null));
    }

    @Test
    @DisplayName("should get confidence score")
    void getConfidenceScore() {
        assertEquals(0.1, cloneService.getConfidenceScore(1), 0.01);

        for (int i = 0; i < 100; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }

        double confidence = cloneService.getConfidenceScore(1);
        assertTrue(confidence >= 0.7, "Confidence with 100 actions should be >= 0.7");
    }

    @Test
    @DisplayName("should get observed action count")
    void getObservedActionCount() {
        assertEquals(0, cloneService.getObservedActionCount(1));

        for (int i = 0; i < 25; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.5, i, null);
        }

        assertEquals(25, cloneService.getObservedActionCount(1));
    }

    @Test
    @DisplayName("should clear cache")
    void clearCache() {
        cloneService.buildPersonality(player);
        cloneService.clearCache(1);

        // Should build a new one
        DynamicAiPersonality personality = cloneService.getOrCreatePersonality(player);
        assertNotNull(personality);
    }

    @Test
    @DisplayName("should clear all caches")
    void clearAllCaches() {
        Player player2 = new Player(2, "test2@test.com", "Test Empire 2");
        player2.setGovernmentType(Player.GovernmentType.MONARCHY);
        player2.setEthos(Player.Ethos.PACIFIST);

        cloneService.buildPersonality(player);
        cloneService.buildPersonality(player2);

        cloneService.clearAllCaches();

        // Should build new ones
        DynamicAiPersonality p1 = cloneService.getOrCreatePersonality(player);
        DynamicAiPersonality p2 = cloneService.getOrCreatePersonality(player2);
        assertNotNull(p1);
        assertNotNull(p2);
    }

    @Test
    @DisplayName("clone should use dynamic personality for decisions")
    void cloneUsesDynamicPersonality() {
        for (int i = 0; i < 100; i++) {
            analyzer.recordAction(1, PlayerActionCategory.COMBAT, 0.2, i, null);
        }

        EmpireAiCore clone = cloneService.createClone(100, player, 5);

        // getPersonality() should return the dynamic personality
        AiPersonality personality = clone.getPersonality();
        assertInstanceOf(DynamicAiPersonality.class, personality);

        // The dynamic personality should have lower aggression than base
        // because observed combat intensity is low (0.2)
        DynamicAiPersonality dynamic = (DynamicAiPersonality) personality;
        assertTrue(dynamic.getAggression() < 0.5,
                "Aggression should be lower than base due to low-intensity observed combat");
    }

    @Test
    @DisplayName("EmpireAiCore should support personality refresh")
    void empireAiCoreRefreshPersonality() {
        EmpireAiCore clone = cloneService.createClone(100, player, 5);

        // Record more actions
        for (int i = 0; i < 50; i++) {
            analyzer.recordAction(1, PlayerActionCategory.EXPANSION, 0.9, i, null);
        }

        clone.refreshPersonality(analyzer);

        DynamicAiPersonality dynamic = clone.getDynamicPersonality();
        assertNotNull(dynamic);
        // Should have updated with new data
        assertTrue(dynamic.getExpansionDrive() > 0.5,
                "Expansion drive should be high after many expansion actions");
    }
}

package com.yisy.modules.doramcu.hex.planet;

import com.yisy.modules.doramcu.ai.AiDecisionContext;
import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GalaxyOverlordAITest {

    private GalaxyOverlordAI overlord;

    @BeforeEach
    void setUp() {
        Player player = new Player(1, "ai@test.com", "AI Empire");
        overlord = new GalaxyOverlordAI(1, "Test Empire", player, 5,
            GalaxyOverlordAI.AiPersonality.BALANCED);
    }

    @Test
    void constructor_setsFields() {
        assertEquals(1, overlord.getEmpireId());
        assertEquals("Test Empire", overlord.getEmpireName());
        assertEquals(GalaxyOverlordAI.AiPersonality.BALANCED, overlord.getPersonality());
    }

    @Test
    void constructor_randomPersonality() {
        Player player = new Player(2, "ai2@test.com", "Random Empire");
        GalaxyOverlordAI randomOverlord = new GalaxyOverlordAI(2, "Random", player, 3);
        assertNotNull(randomOverlord.getPersonality());
    }

    @Test
    void getRelationWith_defaultZero() {
        assertEquals(0, overlord.getRelationWith(2));
    }

    @Test
    void setRelationWith_setsValue() {
        overlord.setRelationWith(2, 50);
        assertEquals(50, overlord.getRelationWith(2));
    }

    @Test
    void setRelationWith_clampsToRange() {
        overlord.setRelationWith(2, 150);
        assertEquals(100, overlord.getRelationWith(2));
        overlord.setRelationWith(2, -150);
        assertEquals(-100, overlord.getRelationWith(2));
    }

    @Test
    void modifyRelationWith_adjustsCorrectly() {
        overlord.setRelationWith(2, 30);
        overlord.modifyRelationWith(2, 20);
        assertEquals(50, overlord.getRelationWith(2));
    }

    @Test
    void isAllyWith_highRelation() {
        overlord.setRelationWith(2, 60);
        assertTrue(overlord.isAllyWith(2));
    }

    @Test
    void isAllyWith_lowRelation() {
        overlord.setRelationWith(2, 30);
        assertFalse(overlord.isAllyWith(2));
    }

    @Test
    void isEnemyWith_lowRelation() {
        overlord.setRelationWith(2, -60);
        assertTrue(overlord.isEnemyWith(2));
    }

    @Test
    void isEnemyWith_highRelation() {
        overlord.setRelationWith(2, 30);
        assertFalse(overlord.isEnemyWith(2));
    }

    @Test
    void getTradePolicy_returnsPolicy() {
        assertNotNull(overlord.getTradePolicy());
    }

    @Test
    void setTradePolicy_setsNewPolicy() {
        EmpireTradePolicy newPolicy = new EmpireTradePolicy();
        newPolicy.setTariffPercentage(30);
        overlord.setTradePolicy(newPolicy);
        assertEquals(30, overlord.getTradePolicy().getTariffPercentage());
    }

    @Test
    void setTradePolicy_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> overlord.setTradePolicy(null));
    }

    @Test
    void processTurn_returnsDecisions() {
        AiDecisionContext context = AiDecisionContext.builder().build();
        var decisions = overlord.processTurn(context, 1);
        assertNotNull(decisions);
    }

    @Test
    void processTurn_issuesContractsOnTurn5() {
        AiDecisionContext context = AiDecisionContext.builder().build();
        overlord.processTurn(context, 5);
        var contracts = overlord.getAvailableContracts();
        assertTrue(contracts.size() > 0);
    }

    @Test
    void getAvailableContracts_returnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class, () ->
            overlord.getAvailableContracts().add(null));
    }

    @Test
    void getRelations_returnsUnmodifiableMap() {
        assertThrows(UnsupportedOperationException.class, () ->
            overlord.getRelations().put(2, 50));
    }

    @Test
    void personality_aggressive_values() {
        assertEquals(0.9, GalaxyOverlordAI.AiPersonality.AGGRESSIVE.getAggression(), 0.001);
        assertEquals("Expansionist Warlord", GalaxyOverlordAI.AiPersonality.AGGRESSIVE.getLabel());
    }

    @Test
    void personality_diplomatic_values() {
        assertEquals(0.9, GalaxyOverlordAI.AiPersonality.DIPLOMATIC.getDiplomacy(), 0.001);
        assertEquals("Peaceful Trader", GalaxyOverlordAI.AiPersonality.DIPLOMATIC.getLabel());
    }

    @Test
    void personality_random_returnsValid() {
        GalaxyOverlordAI.AiPersonality personality = GalaxyOverlordAI.AiPersonality.random(new java.util.Random());
        assertNotNull(personality);
    }

    @Test
    void onContractCompleted_increasesReputation() {
        EmpireReputationTracker tracker = new EmpireReputationTracker();
        overlord.onContractCompleted(tracker, 10);
        assertTrue(tracker.getReputation(1) > 0);
    }

    @Test
    void onEnemyContractCompleted_decreasesReputation() {
        EmpireReputationTracker tracker = new EmpireReputationTracker();
        tracker.modifyReputation(1, 50);
        overlord.onEnemyContractCompleted(tracker);
        assertTrue(tracker.getReputation(1) < 50);
    }

    @Test
    void issueContracts_filtersByReputation() {
        TransportCorporation corp = new TransportCorporation("Test Corp", "player1", 10000);
        EmpireReputationTracker tracker = new EmpireReputationTracker();
        tracker.modifyReputation(1, 10);
        AiDecisionContext context = AiDecisionContext.builder().build();
        overlord.processTurn(context, 5);
        var contracts = overlord.issueContracts(corp, tracker);
        assertNotNull(contracts);
    }

    @Test
    void updateTradePolicy_aggressiveIncreasesTariff() {
        Player player = new Player(3, "agg@test.com", "Aggressive Empire");
        GalaxyOverlordAI aggressive = new GalaxyOverlordAI(1, "Aggressive", player, 5,
            GalaxyOverlordAI.AiPersonality.AGGRESSIVE);
        aggressive.setRelationWith(2, -60);
        aggressive.updateTradePolicy();
        assertTrue(aggressive.getTradePolicy().getTariffPercentage() > 0);
    }
}

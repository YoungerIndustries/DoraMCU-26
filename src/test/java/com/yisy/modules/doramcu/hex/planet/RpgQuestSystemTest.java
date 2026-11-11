package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RpgQuestSystemTest {

    private RpgQuestSystem questSystem;

    @BeforeEach
    void setUp() {
        questSystem = new RpgQuestSystem();
    }

    @Test
    void constructor_initializesCorrectly() {
        assertNotNull(questSystem);
        assertTrue(questSystem.getActiveQuests().isEmpty());
    }

    @Test
    void getAvailableQuests_emptyInitially() {
        TransportCorporation corp = new TransportCorporation("Test", "p1", 10000);
        EmpireReputationTracker tracker = new EmpireReputationTracker();
        assertTrue(questSystem.getAvailableQuests(corp, tracker).isEmpty());
    }

    @Test
    void acceptQuest_notAvailable_returnsFalse() {
        TransportCorporation corp = new TransportCorporation("Test", "p1", 1000);
        assertFalse(questSystem.acceptQuest("nonexistent", corp, new EmpireReputationTracker()));
    }
}

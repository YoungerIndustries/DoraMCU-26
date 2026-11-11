package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmpireReputationTrackerTest {

    private EmpireReputationTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new EmpireReputationTracker();
    }

    @Test
    void getReputation_defaultReturnsZero() {
        assertEquals(0, tracker.getReputation(1));
    }

    @Test
    void modifyReputation_increases() {
        tracker.modifyReputation(1, 30);
        assertEquals(30, tracker.getReputation(1));
    }

    @Test
    void modifyReputation_decreases() {
        tracker.modifyReputation(1, 30);
        tracker.modifyReputation(1, -10);
        assertEquals(20, tracker.getReputation(1));
    }

    @Test
    void modifyReputation_clampsToZero() {
        tracker.modifyReputation(1, -50);
        assertEquals(0, tracker.getReputation(1));
    }

    @Test
    void modifyReputation_clampsToHundred() {
        tracker.modifyReputation(1, 150);
        assertEquals(100, tracker.getReputation(1));
    }

    @Test
    void getTariffDiscount_lowReputation() {
        tracker.modifyReputation(1, 10);
        assertEquals(0.0, tracker.getTariffDiscount(1), 0.001);
    }

    @Test
    void getTariffDiscount_highReputation() {
        tracker.modifyReputation(1, 80);
        assertTrue(tracker.getTariffDiscount(1) > 0);
    }

    @Test
    void independentReputation_perEmpire() {
        tracker.modifyReputation(1, 50);
        tracker.modifyReputation(2, 30);
        assertEquals(50, tracker.getReputation(1));
        assertEquals(30, tracker.getReputation(2));
    }

    @Test
    void getChangeHistory_recordsChanges() {
        tracker.modifyReputation(1, 20);
        tracker.modifyReputation(1, -5);
        var history = tracker.getChangeHistory(1);
        assertEquals(2, history.size());
    }

}

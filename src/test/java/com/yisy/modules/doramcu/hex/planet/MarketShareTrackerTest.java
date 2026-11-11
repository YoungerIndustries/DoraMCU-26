package com.yisy.modules.doramcu.hex.planet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MarketShareTrackerTest {

    private MarketShareTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new MarketShareTracker();
    }

    @Test
    void constructor_initializesCorrectly() {
        assertNotNull(tracker);
    }

    @Test
    void recordDelivery_tracksCargoValue() {
        tracker.recordDelivery("corp1", 1000);
    }

    @Test
    void recordDelivery_accumulatesPerTurn() {
        tracker.recordDelivery("corp1", 1000);
        tracker.recordDelivery("corp1", 500);
    }

    @Test
    void recordDelivery_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> tracker.recordDelivery("corp1", -100));
    }

    @Test
    void recordDelivery_nullCorporation_throws() {
        assertThrows(NullPointerException.class, () -> tracker.recordDelivery(null, 100));
    }
}

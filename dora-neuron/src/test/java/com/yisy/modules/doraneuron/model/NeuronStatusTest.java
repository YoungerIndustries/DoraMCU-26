package com.yisy.modules.doraneuron.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeuronStatus Tests")
class NeuronStatusTest {

    @Test
    @DisplayName("All 4 enum values exist")
    void allValues() {
        NeuronStatus[] values = NeuronStatus.values();
        assertEquals(4, values.length);
    }

    @Test
    @DisplayName("Enum values are ACTIVE, IDLE, DEGRADED, OFFLINE")
    void enumValues() {
        assertSame(NeuronStatus.ACTIVE, NeuronStatus.valueOf("ACTIVE"));
        assertSame(NeuronStatus.IDLE, NeuronStatus.valueOf("IDLE"));
        assertSame(NeuronStatus.DEGRADED, NeuronStatus.valueOf("DEGRADED"));
        assertSame(NeuronStatus.OFFLINE, NeuronStatus.valueOf("OFFLINE"));
    }

    @Test
    @DisplayName("valueOf throws for invalid name")
    void valueOfInvalid() {
        assertThrows(IllegalArgumentException.class, () -> NeuronStatus.valueOf("UNKNOWN"));
    }
}

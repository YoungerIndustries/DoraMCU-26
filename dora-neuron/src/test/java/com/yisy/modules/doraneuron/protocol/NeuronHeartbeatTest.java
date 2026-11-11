package com.yisy.modules.doraneuron.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeuronHeartbeat Tests")
class NeuronHeartbeatTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            NeuronHeartbeat heartbeat = new NeuronHeartbeat();
            assertNull(heartbeat.getNodeId());
            assertNull(heartbeat.getStatus());
            assertEquals(0, heartbeat.getCurrentModelVersion());
            assertEquals(0, heartbeat.getExperienceCount());
            assertEquals(0, heartbeat.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets all fields")
        void setsFields() {
            NeuronHeartbeat heartbeat = new NeuronHeartbeat("node-1", "ACTIVE", 42L, 100, 12345L);
            assertEquals("node-1", heartbeat.getNodeId());
            assertEquals("ACTIVE", heartbeat.getStatus());
            assertEquals(42L, heartbeat.getCurrentModelVersion());
            assertEquals(100, heartbeat.getExperienceCount());
            assertEquals(12345L, heartbeat.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get nodeId")
        void nodeId() {
            NeuronHeartbeat heartbeat = new NeuronHeartbeat();
            heartbeat.setNodeId("node-2");
            assertEquals("node-2", heartbeat.getNodeId());
        }

        @Test
        @DisplayName("Set and get status")
        void status() {
            NeuronHeartbeat heartbeat = new NeuronHeartbeat();
            heartbeat.setStatus("DEGRADED");
            assertEquals("DEGRADED", heartbeat.getStatus());
        }

        @Test
        @DisplayName("Set and get currentModelVersion")
        void currentModelVersion() {
            NeuronHeartbeat heartbeat = new NeuronHeartbeat();
            heartbeat.setCurrentModelVersion(100L);
            assertEquals(100L, heartbeat.getCurrentModelVersion());
        }

        @Test
        @DisplayName("Set and get experienceCount")
        void experienceCount() {
            NeuronHeartbeat heartbeat = new NeuronHeartbeat();
            heartbeat.setExperienceCount(500);
            assertEquals(500, heartbeat.getExperienceCount());
        }

        @Test
        @DisplayName("Set and get timestamp")
        void timestamp() {
            NeuronHeartbeat heartbeat = new NeuronHeartbeat();
            heartbeat.setTimestamp(67890L);
            assertEquals(67890L, heartbeat.getTimestamp());
        }
    }
}

package com.yisy.modules.doramcu.zero_zero_seven.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SpyNetwork Tests")
class SpyNetworkTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            SpyNetwork network = new SpyNetwork();
            assertNull(network.getId());
            assertNull(network.getName());
            assertEquals(1, network.getLevel());
            assertEquals(0, network.getIntelligenceGathered());
            assertEquals(0, network.getDetectionRisk());
            assertTrue(network.isActive());
            assertTrue(network.getAgents().isEmpty());
            assertEquals(0, network.getSafehouseCount());
        }
    }

    @Nested
    @DisplayName("Level clamping")
    class LevelClamping {

        @Test
        @DisplayName("Level clamps to 1-5")
        void clampLevel() {
            SpyNetwork network = new SpyNetwork();
            network.setLevel(0);
            assertEquals(1, network.getLevel());
            network.setLevel(10);
            assertEquals(5, network.getLevel());
            network.setLevel(3);
            assertEquals(3, network.getLevel());
        }
    }

    @Nested
    @DisplayName("Detection risk clamping")
    class DetectionRiskClamping {

        @Test
        @DisplayName("Clamps to 0-100")
        void clampRisk() {
            SpyNetwork network = new SpyNetwork();
            network.setDetectionRisk(-10);
            assertEquals(0, network.getDetectionRisk());
            network.setDetectionRisk(150);
            assertEquals(100, network.getDetectionRisk());
            network.setDetectionRisk(50);
            assertEquals(50, network.getDetectionRisk());
        }
    }

    @Nested
    @DisplayName("Agent management")
    class AgentManagement {

        @Test
        @DisplayName("Assign agent adds to list")
        void assignAgent() {
            SpyNetwork network = new SpyNetwork();
            network.assignAgent(1L);
            network.assignAgent(2L);
            assertEquals(2, network.getAgents().size());
            assertTrue(network.getAgents().contains(1L));
            assertTrue(network.getAgents().contains(2L));
        }

        @Test
        @DisplayName("Remove agent removes from list")
        void removeAgent() {
            SpyNetwork network = new SpyNetwork();
            network.assignAgent(1L);
            network.assignAgent(2L);
            network.removeAgent(1L);
            assertEquals(1, network.getAgents().size());
            assertFalse(network.getAgents().contains(1L));
            assertTrue(network.getAgents().contains(2L));
        }
    }

    @Nested
    @DisplayName("Upgrade")
    class Upgrade {

        @Test
        @DisplayName("Upgrade increases level and stats")
        void upgrade() {
            SpyNetwork network = new SpyNetwork();
            network.setLevel(1);
            network.setIntelligenceGathered(10);
            network.setDetectionRisk(20);
            network.upgrade();
            assertEquals(2, network.getLevel());
            assertEquals(15, network.getIntelligenceGathered());
            assertEquals(30, network.getDetectionRisk());
        }

        @Test
        @DisplayName("Upgrade at max level does nothing")
        void upgradeAtMax() {
            SpyNetwork network = new SpyNetwork();
            network.setLevel(5);
            network.setIntelligenceGathered(50);
            network.setDetectionRisk(100);
            network.upgrade();
            assertEquals(5, network.getLevel());
            assertEquals(50, network.getIntelligenceGathered());
            assertEquals(100, network.getDetectionRisk());
        }

        @Test
        @DisplayName("Multiple upgrades")
        void multipleUpgrades() {
            SpyNetwork network = new SpyNetwork();
            network.setLevel(1);
            network.setIntelligenceGathered(0);
            network.setDetectionRisk(0);
            network.upgrade();
            network.upgrade();
            network.upgrade();
            assertEquals(4, network.getLevel());
            assertEquals(15, network.getIntelligenceGathered());
            assertEquals(30, network.getDetectionRisk());
        }
    }
}

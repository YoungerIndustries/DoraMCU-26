package com.yisy.modules.doraneuron.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeuronIdentity Tests")
class NeuronIdentityTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes null/zero defaults")
        void defaults() {
            NeuronIdentity identity = new NeuronIdentity();
            assertNull(identity.getNodeId());
            assertNull(identity.getNodeName());
            assertNull(identity.getRegion());
            assertNull(identity.getHostAddress());
            assertEquals(0, identity.getPort());
            assertEquals(0, identity.getRegisteredAt());
            assertEquals(0, identity.getLastHeartbeat());
            assertNull(identity.getStatus());
            assertEquals(0.0, identity.getVoteWeight());
            assertEquals(0, identity.getCurrentModelVersion());
            assertEquals(0, identity.getExperienceCount());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get nodeId")
        void nodeId() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setNodeId("node-1");
            assertEquals("node-1", identity.getNodeId());
        }

        @Test
        @DisplayName("Set and get nodeName")
        void nodeName() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setNodeName("Alpha Neuron");
            assertEquals("Alpha Neuron", identity.getNodeName());
        }

        @Test
        @DisplayName("Set and get region")
        void region() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setRegion("us-east-1");
            assertEquals("us-east-1", identity.getRegion());
        }

        @Test
        @DisplayName("Set and get hostAddress")
        void hostAddress() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setHostAddress("10.0.0.5");
            assertEquals("10.0.0.5", identity.getHostAddress());
        }

        @Test
        @DisplayName("Set and get port")
        void port() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setPort(8090);
            assertEquals(8090, identity.getPort());
        }

        @Test
        @DisplayName("Set and get registeredAt")
        void registeredAt() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setRegisteredAt(1000L);
            assertEquals(1000L, identity.getRegisteredAt());
        }

        @Test
        @DisplayName("Set and get lastHeartbeat")
        void lastHeartbeat() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setLastHeartbeat(2000L);
            assertEquals(2000L, identity.getLastHeartbeat());
        }

        @Test
        @DisplayName("Set and get status")
        void status() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setStatus(NeuronStatus.ACTIVE);
            assertEquals(NeuronStatus.ACTIVE, identity.getStatus());
        }

        @Test
        @DisplayName("Set and get voteWeight")
        void voteWeight() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setVoteWeight(1.5);
            assertEquals(1.5, identity.getVoteWeight());
        }

        @Test
        @DisplayName("Set and get currentModelVersion")
        void currentModelVersion() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setCurrentModelVersion(42L);
            assertEquals(42L, identity.getCurrentModelVersion());
        }

        @Test
        @DisplayName("Set and get experienceCount")
        void experienceCount() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setExperienceCount(100);
            assertEquals(100, identity.getExperienceCount());
        }
    }

    @Nested
    @DisplayName("getUrl()")
    class GetUrl {

        @Test
        @DisplayName("Builds URL from hostAddress and port")
        void buildsUrl() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setHostAddress("10.0.0.5");
            identity.setPort(8090);
            assertEquals("http://10.0.0.5:8090", identity.getUrl());
        }

        @Test
        @DisplayName("Handles null hostAddress")
        void nullHost() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setPort(8090);
            assertEquals("http://null:8090", identity.getUrl());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes nodeId, nodeName, region, status, and url")
        void toStringContent() {
            NeuronIdentity identity = new NeuronIdentity();
            identity.setNodeId("node-1");
            identity.setNodeName("Alpha");
            identity.setRegion("us-east-1");
            identity.setStatus(NeuronStatus.ACTIVE);
            identity.setHostAddress("10.0.0.5");
            identity.setPort(8090);
            String result = identity.toString();
            assertTrue(result.contains("node-1"));
            assertTrue(result.contains("Alpha"));
            assertTrue(result.contains("us-east-1"));
            assertTrue(result.contains("ACTIVE"));
            assertTrue(result.contains("http://10.0.0.5:8090"));
        }
    }
}

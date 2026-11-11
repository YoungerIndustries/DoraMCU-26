package com.yisy.modules.doraneuron.protocol.mesh;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MeshPeerInfo Tests")
class MeshPeerInfoTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            MeshPeerInfo peer = new MeshPeerInfo();
            assertNull(peer.getNodeId());
            assertNull(peer.getNodeName());
            assertNull(peer.getRegion());
            assertNull(peer.getHostAddress());
            assertEquals(0, peer.getPort());
            assertNull(peer.getStatus());
            assertEquals(0, peer.getModelVersion());
            assertEquals(0.0, peer.getVoteWeight());
            assertEquals(0, peer.getPeerCount());
            assertEquals(0, peer.getExperienceCount());
            assertEquals(0, peer.getLastHeartbeat());
            assertNull(peer.getCpuProfile());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get nodeId")
        void nodeId() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setNodeId("node-1");
            assertEquals("node-1", peer.getNodeId());
        }

        @Test
        @DisplayName("Set and get nodeName")
        void nodeName() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setNodeName("Alpha Neuron");
            assertEquals("Alpha Neuron", peer.getNodeName());
        }

        @Test
        @DisplayName("Set and get region")
        void region() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setRegion("us-east-1");
            assertEquals("us-east-1", peer.getRegion());
        }

        @Test
        @DisplayName("Set and get hostAddress")
        void hostAddress() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setHostAddress("10.0.0.5");
            assertEquals("10.0.0.5", peer.getHostAddress());
        }

        @Test
        @DisplayName("Set and get port")
        void port() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setPort(8090);
            assertEquals(8090, peer.getPort());
        }

        @Test
        @DisplayName("Set and get status")
        void status() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setStatus("ACTIVE");
            assertEquals("ACTIVE", peer.getStatus());
        }

        @Test
        @DisplayName("Set and get modelVersion")
        void modelVersion() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setModelVersion(42L);
            assertEquals(42L, peer.getModelVersion());
        }

        @Test
        @DisplayName("Set and get voteWeight")
        void voteWeight() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setVoteWeight(1.5);
            assertEquals(1.5, peer.getVoteWeight());
        }

        @Test
        @DisplayName("Set and get peerCount")
        void peerCount() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setPeerCount(5);
            assertEquals(5, peer.getPeerCount());
        }

        @Test
        @DisplayName("Set and get experienceCount")
        void experienceCount() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setExperienceCount(100);
            assertEquals(100, peer.getExperienceCount());
        }

        @Test
        @DisplayName("Set and get lastHeartbeat")
        void lastHeartbeat() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setLastHeartbeat(12345L);
            assertEquals(12345L, peer.getLastHeartbeat());
        }

        @Test
        @DisplayName("Set and get cpuProfile")
        void cpuProfile() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setCpuProfile("GPU");
            assertEquals("GPU", peer.getCpuProfile());
        }
    }

    @Nested
    @DisplayName("getUrl()")
    class GetUrl {

        @Test
        @DisplayName("Builds URL from hostAddress and port")
        void buildsUrl() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setHostAddress("10.0.0.5");
            peer.setPort(8090);
            assertEquals("http://10.0.0.5:8090", peer.getUrl());
        }

        @Test
        @DisplayName("Handles null hostAddress")
        void nullHost() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setPort(8090);
            assertEquals("http://null:8090", peer.getUrl());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes nodeId, host, region, and status")
        void toStringContent() {
            MeshPeerInfo peer = new MeshPeerInfo();
            peer.setNodeId("node-1");
            peer.setHostAddress("10.0.0.5");
            peer.setPort(8090);
            peer.setRegion("us-east-1");
            peer.setStatus("ACTIVE");
            String result = peer.toString();
            assertTrue(result.contains("node-1"));
            assertTrue(result.contains("10.0.0.5:8090"));
            assertTrue(result.contains("us-east-1"));
            assertTrue(result.contains("ACTIVE"));
        }
    }
}

package com.yisy.modules.doraneuron.protocol.mesh;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MeshMessage Tests")
class MeshMessageTest {

    private static class ConcreteMeshMessage extends MeshMessage {
        public ConcreteMeshMessage() {
            super();
        }
        public ConcreteMeshMessage(String type, String fromNodeId) {
            super(type, fromNodeId);
        }
    }

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes defaults with auto timestamp and zero hop count")
        void defaults() {
            MeshMessage msg = new ConcreteMeshMessage();
            assertNull(msg.getType());
            assertNull(msg.getFromNodeId());
            assertNull(msg.getToNodeId());
            assertNull(msg.getMessageId());
            assertEquals(0, msg.getHopCount());
            assertEquals(0, msg.getMaxHops());
            assertTrue(msg.getTimestamp() > 0);
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets type, fromNodeId, generates messageId, timestamp, and zero hop count")
        void setsFields() {
            MeshMessage msg = new ConcreteMeshMessage("PEER_DISCOVERY", "node-1");
            assertEquals("PEER_DISCOVERY", msg.getType());
            assertEquals("node-1", msg.getFromNodeId());
            assertNotNull(msg.getMessageId());
            assertTrue(msg.getTimestamp() > 0);
            assertEquals(0, msg.getHopCount());
        }
    }

    @Nested
    @DisplayName("incrementHop()")
    class IncrementHop {

        @Test
        @DisplayName("Increments hop count and returns true when below max")
        void incrementsAndReturnsTrue() {
            MeshMessage msg = new ConcreteMeshMessage("TEST", "node-1");
            msg.setMaxHops(3);
            assertTrue(msg.incrementHop());
            assertEquals(1, msg.getHopCount());
        }

        @Test
        @DisplayName("Returns false when hop count reaches max")
        void returnsFalseAtMax() {
            MeshMessage msg = new ConcreteMeshMessage("TEST", "node-1");
            msg.setMaxHops(2);
            msg.incrementHop(); // hopCount = 1
            assertFalse(msg.incrementHop()); // hopCount = 2, returns false since 2 < 2 is false
            assertEquals(2, msg.getHopCount());
        }

        @Test
        @DisplayName("Multiple increments")
        void multipleIncrements() {
            MeshMessage msg = new ConcreteMeshMessage("TEST", "node-1");
            msg.setMaxHops(5);
            assertTrue(msg.incrementHop()); // 1
            assertTrue(msg.incrementHop()); // 2
            assertTrue(msg.incrementHop()); // 3
            assertTrue(msg.incrementHop()); // 4
            assertEquals(4, msg.getHopCount());
        }
    }

    @Nested
    @DisplayName("isExpired()")
    class IsExpired {

        @Test
        @DisplayName("Returns false when hop count is below max")
        void notExpired() {
            MeshMessage msg = new ConcreteMeshMessage("TEST", "node-1");
            msg.setMaxHops(5);
            msg.setHopCount(3);
            assertFalse(msg.isExpired());
        }

        @Test
        @DisplayName("Returns true when hop count equals max")
        void expired() {
            MeshMessage msg = new ConcreteMeshMessage("TEST", "node-1");
            msg.setMaxHops(3);
            msg.setHopCount(3);
            assertTrue(msg.isExpired());
        }

        @Test
        @DisplayName("Returns true when maxHops is 0 and hopCount is 0")
        void zeroMaxHops() {
            MeshMessage msg = new ConcreteMeshMessage("TEST", "node-1");
            msg.setMaxHops(0);
            assertTrue(msg.isExpired());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get type")
        void type() {
            MeshMessage msg = new ConcreteMeshMessage();
            msg.setType("CONSENSUS_REQUEST");
            assertEquals("CONSENSUS_REQUEST", msg.getType());
        }

        @Test
        @DisplayName("Set and get fromNodeId")
        void fromNodeId() {
            MeshMessage msg = new ConcreteMeshMessage();
            msg.setFromNodeId("node-2");
            assertEquals("node-2", msg.getFromNodeId());
        }

        @Test
        @DisplayName("Set and get toNodeId")
        void toNodeId() {
            MeshMessage msg = new ConcreteMeshMessage();
            msg.setToNodeId("node-3");
            assertEquals("node-3", msg.getToNodeId());
        }

        @Test
        @DisplayName("Set and get messageId")
        void messageId() {
            MeshMessage msg = new ConcreteMeshMessage();
            msg.setMessageId("msg-123");
            assertEquals("msg-123", msg.getMessageId());
        }

        @Test
        @DisplayName("Set and get hopCount")
        void hopCount() {
            MeshMessage msg = new ConcreteMeshMessage();
            msg.setHopCount(5);
            assertEquals(5, msg.getHopCount());
        }

        @Test
        @DisplayName("Set and get maxHops")
        void maxHops() {
            MeshMessage msg = new ConcreteMeshMessage();
            msg.setMaxHops(10);
            assertEquals(10, msg.getMaxHops());
        }

        @Test
        @DisplayName("Set and get timestamp")
        void timestamp() {
            MeshMessage msg = new ConcreteMeshMessage();
            msg.setTimestamp(12345L);
            assertEquals(12345L, msg.getTimestamp());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes type, from, messageId, and hops")
        void toStringContent() {
            MeshMessage msg = new ConcreteMeshMessage("PEER_DISCOVERY", "node-1");
            msg.setMaxHops(5);
            msg.setHopCount(2);
            String result = msg.toString();
            assertTrue(result.contains("PEER_DISCOVERY"));
            assertTrue(result.contains("node-1"));
            assertTrue(result.contains("hops=2/5"));
        }
    }
}

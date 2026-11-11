package com.yisy.modules.doraneuron.protocol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConsensusRequest Tests")
class ConsensusRequestTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            ConsensusRequest request = new ConsensusRequest();
            assertNull(request.getFeatures());
            assertEquals(0, request.getTimeoutMs());
            assertNull(request.getRequestingNode());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets features and timeoutMs")
        void setsFields() {
            double[] features = {1.0, 2.0, 3.0};
            ConsensusRequest request = new ConsensusRequest(features, 5000L);
            assertSame(features, request.getFeatures());
            assertEquals(5000L, request.getTimeoutMs());
            assertNull(request.getRequestingNode());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get features")
        void features() {
            ConsensusRequest request = new ConsensusRequest();
            double[] features = {0.1, 0.2};
            request.setFeatures(features);
            assertSame(features, request.getFeatures());
        }

        @Test
        @DisplayName("Set and get timeoutMs")
        void timeoutMs() {
            ConsensusRequest request = new ConsensusRequest();
            request.setTimeoutMs(10000L);
            assertEquals(10000L, request.getTimeoutMs());
        }

        @Test
        @DisplayName("Set and get requestingNode")
        void requestingNode() {
            ConsensusRequest request = new ConsensusRequest();
            request.setRequestingNode("node-1");
            assertEquals("node-1", request.getRequestingNode());
        }
    }

    @Nested
    @DisplayName("getFeatureDim()")
    class GetFeatureDim {

        @Test
        @DisplayName("Returns feature array length")
        void returnsLength() {
            ConsensusRequest request = new ConsensusRequest();
            request.setFeatures(new double[64]);
            assertEquals(64, request.getFeatureDim());
        }

        @Test
        @DisplayName("Returns 0 for null features")
        void nullFeatures() {
            ConsensusRequest request = new ConsensusRequest();
            assertEquals(0, request.getFeatureDim());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes featureDim, timeoutMs, and requestingNode")
        void toStringContent() {
            ConsensusRequest request = new ConsensusRequest();
            request.setFeatures(new double[64]);
            request.setTimeoutMs(5000L);
            request.setRequestingNode("node-1");
            String result = request.toString();
            assertTrue(result.contains("featureDim=64"));
            assertTrue(result.contains("timeoutMs=5000"));
            assertTrue(result.contains("node-1"));
        }
    }
}

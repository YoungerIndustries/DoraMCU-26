package com.yisy.modules.doraneuron.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExperienceSample Tests")
class ExperienceSampleTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            ExperienceSample sample = new ExperienceSample();
            assertNull(sample.getState());
            assertEquals(0, sample.getAction());
            assertEquals(0.0, sample.getReward());
            assertNull(sample.getNextState());
            assertFalse(sample.isDone());
            assertEquals(0.0f, sample.getConfidence());
            assertEquals(0, sample.getModelVersion());
            assertEquals(0, sample.getTimestamp());
            assertNull(sample.getNodeId());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets state, action, reward, and auto-generates timestamp")
        void setsFields() {
            double[] state = {1.0, 2.0, 3.0};
            ExperienceSample sample = new ExperienceSample(state, 5, 0.75);
            assertSame(state, sample.getState());
            assertEquals(5, sample.getAction());
            assertEquals(0.75, sample.getReward());
            assertTrue(sample.getTimestamp() > 0);
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get state")
        void state() {
            ExperienceSample sample = new ExperienceSample();
            double[] state = {0.1, 0.2};
            sample.setState(state);
            assertSame(state, sample.getState());
        }

        @Test
        @DisplayName("Set and get action")
        void action() {
            ExperienceSample sample = new ExperienceSample();
            sample.setAction(10);
            assertEquals(10, sample.getAction());
        }

        @Test
        @DisplayName("Set and get reward")
        void reward() {
            ExperienceSample sample = new ExperienceSample();
            sample.setReward(-1.5);
            assertEquals(-1.5, sample.getReward());
        }

        @Test
        @DisplayName("Set and get nextState")
        void nextState() {
            ExperienceSample sample = new ExperienceSample();
            double[] nextState = {0.3, 0.4};
            sample.setNextState(nextState);
            assertSame(nextState, sample.getNextState());
        }

        @Test
        @DisplayName("Set and get done")
        void done() {
            ExperienceSample sample = new ExperienceSample();
            sample.setDone(true);
            assertTrue(sample.isDone());
        }

        @Test
        @DisplayName("Set and get confidence")
        void confidence() {
            ExperienceSample sample = new ExperienceSample();
            sample.setConfidence(0.95f);
            assertEquals(0.95f, sample.getConfidence());
        }

        @Test
        @DisplayName("Set and get modelVersion")
        void modelVersion() {
            ExperienceSample sample = new ExperienceSample();
            sample.setModelVersion(42L);
            assertEquals(42L, sample.getModelVersion());
        }

        @Test
        @DisplayName("Set and get timestamp")
        void timestamp() {
            ExperienceSample sample = new ExperienceSample();
            sample.setTimestamp(12345L);
            assertEquals(12345L, sample.getTimestamp());
        }

        @Test
        @DisplayName("Set and get nodeId")
        void nodeId() {
            ExperienceSample sample = new ExperienceSample();
            sample.setNodeId("node-1");
            assertEquals("node-1", sample.getNodeId());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes action, reward, confidence, modelVersion, and stateDim")
        void toStringContent() {
            ExperienceSample sample = new ExperienceSample();
            sample.setState(new double[]{1.0, 2.0, 3.0});
            sample.setAction(5);
            sample.setReward(0.75);
            sample.setConfidence(0.9f);
            sample.setModelVersion(42L);
            String result = sample.toString();
            assertTrue(result.contains("action=5"));
            assertTrue(result.contains("reward=0.75"));
            assertTrue(result.contains("confidence=0.9"));
            assertTrue(result.contains("modelVersion=42"));
            assertTrue(result.contains("stateDim=3"));
        }

        @Test
        @DisplayName("toString handles null state")
        void toStringNullState() {
            ExperienceSample sample = new ExperienceSample();
            String result = sample.toString();
            assertTrue(result.contains("stateDim=0"));
        }
    }
}

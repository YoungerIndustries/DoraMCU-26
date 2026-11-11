package com.yisy.modules.doraneuron.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeuronDecision Tests")
class NeuronDecisionTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            NeuronDecision decision = new NeuronDecision();
            assertNull(decision.getProbabilities());
            assertEquals(0, decision.getSelectedAction());
            assertEquals(0.0f, decision.getConfidence());
            assertEquals(0, decision.getModelVersion());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets all fields")
        void setsFields() {
            float[] probs = {0.1f, 0.5f, 0.4f};
            NeuronDecision decision = new NeuronDecision(probs, 1, 0.5f, 42L);
            assertSame(probs, decision.getProbabilities());
            assertEquals(1, decision.getSelectedAction());
            assertEquals(0.5f, decision.getConfidence());
            assertEquals(42L, decision.getModelVersion());
        }
    }

    @Nested
    @DisplayName("fromProbabilities()")
    class FromProbabilities {

        @Test
        @DisplayName("Finds argmax and sets confidence to max probability")
        void argmax() {
            float[] probs = {0.1f, 0.7f, 0.2f};
            NeuronDecision decision = NeuronDecision.fromProbabilities(probs, 10L);
            assertEquals(1, decision.getSelectedAction());
            assertEquals(0.7f, decision.getConfidence());
            assertEquals(10L, decision.getModelVersion());
        }

        @Test
        @DisplayName("Selects first index on tie")
        void tieBreaksFirst() {
            float[] probs = {0.5f, 0.5f, 0.0f};
            NeuronDecision decision = NeuronDecision.fromProbabilities(probs, 1L);
            assertEquals(0, decision.getSelectedAction());
            assertEquals(0.5f, decision.getConfidence());
        }

        @Test
        @DisplayName("Clones probabilities array")
        void clonesProbabilities() {
            float[] probs = {0.3f, 0.6f, 0.1f};
            NeuronDecision decision = NeuronDecision.fromProbabilities(probs, 1L);
            assertNotSame(probs, decision.getProbabilities());
            assertArrayEquals(probs, decision.getProbabilities());
        }

        @Test
        @DisplayName("Handles single element array")
        void singleElement() {
            float[] probs = {1.0f};
            NeuronDecision decision = NeuronDecision.fromProbabilities(probs, 1L);
            assertEquals(0, decision.getSelectedAction());
            assertEquals(1.0f, decision.getConfidence());
        }

        @Test
        @DisplayName("Handles all zeros")
        void allZeros() {
            float[] probs = {0.0f, 0.0f, 0.0f};
            NeuronDecision decision = NeuronDecision.fromProbabilities(probs, 1L);
            assertEquals(0, decision.getSelectedAction());
            assertEquals(0.0f, decision.getConfidence());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get probabilities")
        void probabilities() {
            NeuronDecision decision = new NeuronDecision();
            float[] probs = {0.2f, 0.8f};
            decision.setProbabilities(probs);
            assertSame(probs, decision.getProbabilities());
        }

        @Test
        @DisplayName("Set and get selectedAction")
        void selectedAction() {
            NeuronDecision decision = new NeuronDecision();
            decision.setSelectedAction(5);
            assertEquals(5, decision.getSelectedAction());
        }

        @Test
        @DisplayName("Set and get confidence")
        void confidence() {
            NeuronDecision decision = new NeuronDecision();
            decision.setConfidence(0.99f);
            assertEquals(0.99f, decision.getConfidence());
        }

        @Test
        @DisplayName("Set and get modelVersion")
        void modelVersion() {
            NeuronDecision decision = new NeuronDecision();
            decision.setModelVersion(100L);
            assertEquals(100L, decision.getModelVersion());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes selectedAction, confidence, and modelVersion")
        void toStringContent() {
            NeuronDecision decision = new NeuronDecision();
            decision.setSelectedAction(3);
            decision.setConfidence(0.85f);
            decision.setModelVersion(42L);
            String result = decision.toString();
            assertTrue(result.contains("selectedAction=3"));
            assertTrue(result.contains("confidence=0.85"));
            assertTrue(result.contains("modelVersion=42"));
        }
    }
}

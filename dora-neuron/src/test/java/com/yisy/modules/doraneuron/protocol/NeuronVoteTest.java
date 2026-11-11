package com.yisy.modules.doraneuron.protocol;

import com.yisy.modules.doraneuron.model.NeuronDecision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("NeuronVote Tests")
class NeuronVoteTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            NeuronVote vote = new NeuronVote();
            assertNull(vote.getNodeId());
            assertNull(vote.getDecision());
            assertEquals(0.0, vote.getWeight());
            assertEquals(0, vote.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets nodeId, decision, weight, and auto-generates timestamp")
        void setsFields() {
            NeuronDecision decision = new NeuronDecision();
            NeuronVote vote = new NeuronVote("node-1", decision, 1.5);
            assertEquals("node-1", vote.getNodeId());
            assertSame(decision, vote.getDecision());
            assertEquals(1.5, vote.getWeight());
            assertTrue(vote.getTimestamp() > 0);
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get nodeId")
        void nodeId() {
            NeuronVote vote = new NeuronVote();
            vote.setNodeId("node-2");
            assertEquals("node-2", vote.getNodeId());
        }

        @Test
        @DisplayName("Set and get decision")
        void decision() {
            NeuronVote vote = new NeuronVote();
            NeuronDecision decision = new NeuronDecision();
            vote.setDecision(decision);
            assertSame(decision, vote.getDecision());
        }

        @Test
        @DisplayName("Set and get weight")
        void weight() {
            NeuronVote vote = new NeuronVote();
            vote.setWeight(2.5);
            assertEquals(2.5, vote.getWeight());
        }

        @Test
        @DisplayName("Set and get timestamp")
        void timestamp() {
            NeuronVote vote = new NeuronVote();
            vote.setTimestamp(12345L);
            assertEquals(12345L, vote.getTimestamp());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes nodeId, weight, and decision")
        void toStringContent() {
            NeuronDecision decision = new NeuronDecision();
            decision.setSelectedAction(2);
            NeuronVote vote = new NeuronVote("node-1", decision, 1.0);
            String result = vote.toString();
            assertTrue(result.contains("node-1"));
            assertTrue(result.contains("weight=1.0"));
        }
    }
}

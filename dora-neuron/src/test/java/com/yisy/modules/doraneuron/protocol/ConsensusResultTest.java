package com.yisy.modules.doraneuron.protocol;

import com.yisy.modules.doraneuron.model.NeuronDecision;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConsensusResult Tests")
class ConsensusResultTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            ConsensusResult result = new ConsensusResult();
            assertNull(result.getDecision());
            assertNull(result.getVotes());
            assertEquals(0.0, result.getTotalWeight());
            assertEquals(0, result.getVoteCount());
            assertEquals(0, result.getDurationMs());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets decision, votes, durationMs, voteCount, and totalWeight")
        void setsFields() {
            NeuronDecision decision = new NeuronDecision();
            NeuronVote vote1 = new NeuronVote("node-1", decision, 1.0);
            NeuronVote vote2 = new NeuronVote("node-2", decision, 2.0);
            List<NeuronVote> votes = Arrays.asList(vote1, vote2);

            ConsensusResult result = new ConsensusResult(decision, votes, 100L);

            assertSame(decision, result.getDecision());
            assertSame(votes, result.getVotes());
            assertEquals(100L, result.getDurationMs());
            assertEquals(2, result.getVoteCount());
            assertEquals(3.0, result.getTotalWeight());
        }

        @Test
        @DisplayName("Handles null votes")
        void nullVotes() {
            NeuronDecision decision = new NeuronDecision();
            ConsensusResult result = new ConsensusResult(decision, null, 50L);
            assertNull(result.getVotes());
            assertEquals(0, result.getVoteCount());
            assertEquals(0.0, result.getTotalWeight());
        }

        @Test
        @DisplayName("Handles empty votes")
        void emptyVotes() {
            NeuronDecision decision = new NeuronDecision();
            ConsensusResult result = new ConsensusResult(decision, Collections.emptyList(), 50L);
            assertEquals(0, result.getVoteCount());
            assertEquals(0.0, result.getTotalWeight());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get decision")
        void decision() {
            ConsensusResult result = new ConsensusResult();
            NeuronDecision decision = new NeuronDecision();
            result.setDecision(decision);
            assertSame(decision, result.getDecision());
        }

        @Test
        @DisplayName("Set votes updates voteCount")
        void votesUpdateCount() {
            ConsensusResult result = new ConsensusResult();
            NeuronDecision decision = new NeuronDecision();
            NeuronVote vote = new NeuronVote("node-1", decision, 1.5);
            result.setVotes(Arrays.asList(vote));
            assertEquals(1, result.getVoteCount());
        }

        @Test
        @DisplayName("Set null votes sets voteCount to 0")
        void nullVotesSetCount() {
            ConsensusResult result = new ConsensusResult();
            result.setVotes(null);
            assertEquals(0, result.getVoteCount());
        }

        @Test
        @DisplayName("Set and get totalWeight")
        void totalWeight() {
            ConsensusResult result = new ConsensusResult();
            result.setTotalWeight(5.5);
            assertEquals(5.5, result.getTotalWeight());
        }

        @Test
        @DisplayName("Set and get voteCount")
        void voteCount() {
            ConsensusResult result = new ConsensusResult();
            result.setVoteCount(10);
            assertEquals(10, result.getVoteCount());
        }

        @Test
        @DisplayName("Set and get durationMs")
        void durationMs() {
            ConsensusResult result = new ConsensusResult();
            result.setDurationMs(200L);
            assertEquals(200L, result.getDurationMs());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes voteCount, totalWeight, durationMs, and decision")
        void toStringContent() {
            NeuronDecision decision = new NeuronDecision();
            decision.setSelectedAction(1);
            NeuronVote vote = new NeuronVote("node-1", decision, 2.5);
            ConsensusResult result = new ConsensusResult(decision, Arrays.asList(vote), 150L);
            String str = result.toString();
            assertTrue(str.contains("voteCount=1"));
            assertTrue(str.contains("totalWeight=2.5"));
            assertTrue(str.contains("durationMs=150"));
        }
    }
}

package com.yisy.modules.doraneuron.protocol;

import com.yisy.modules.doraneuron.model.ExperienceSample;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExperienceBatch Tests")
class ExperienceBatchTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            ExperienceBatch batch = new ExperienceBatch();
            assertNull(batch.getNodeId());
            assertNull(batch.getSamples());
            assertEquals(0, batch.getTotalCollected());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets nodeId, samples, and totalCollected")
        void setsFields() {
            ExperienceSample sample = new ExperienceSample();
            List<ExperienceSample> samples = Arrays.asList(sample);
            ExperienceBatch batch = new ExperienceBatch("node-1", samples, 100L);
            assertEquals("node-1", batch.getNodeId());
            assertSame(samples, batch.getSamples());
            assertEquals(100L, batch.getTotalCollected());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get nodeId")
        void nodeId() {
            ExperienceBatch batch = new ExperienceBatch();
            batch.setNodeId("node-2");
            assertEquals("node-2", batch.getNodeId());
        }

        @Test
        @DisplayName("Set and get samples")
        void samples() {
            ExperienceBatch batch = new ExperienceBatch();
            ExperienceSample sample = new ExperienceSample();
            batch.setSamples(Arrays.asList(sample));
            assertEquals(1, batch.getSamples().size());
        }

        @Test
        @DisplayName("Set and get totalCollected")
        void totalCollected() {
            ExperienceBatch batch = new ExperienceBatch();
            batch.setTotalCollected(500L);
            assertEquals(500L, batch.getTotalCollected());
        }
    }

    @Nested
    @DisplayName("getSize()")
    class GetSize {

        @Test
        @DisplayName("Returns sample list size")
        void returnsSize() {
            ExperienceBatch batch = new ExperienceBatch();
            batch.setSamples(Arrays.asList(new ExperienceSample(), new ExperienceSample()));
            assertEquals(2, batch.getSize());
        }

        @Test
        @DisplayName("Returns 0 for null samples")
        void nullSamples() {
            ExperienceBatch batch = new ExperienceBatch();
            assertEquals(0, batch.getSize());
        }

        @Test
        @DisplayName("Returns 0 for empty samples")
        void emptySamples() {
            ExperienceBatch batch = new ExperienceBatch();
            batch.setSamples(Collections.emptyList());
            assertEquals(0, batch.getSize());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToString {

        @Test
        @DisplayName("toString includes nodeId, size, and totalCollected")
        void toStringContent() {
            ExperienceBatch batch = new ExperienceBatch();
            batch.setNodeId("node-1");
            batch.setSamples(Arrays.asList(new ExperienceSample()));
            batch.setTotalCollected(100L);
            String result = batch.toString();
            assertTrue(result.contains("node-1"));
            assertTrue(result.contains("size=1"));
            assertTrue(result.contains("totalCollected=100"));
        }
    }
}

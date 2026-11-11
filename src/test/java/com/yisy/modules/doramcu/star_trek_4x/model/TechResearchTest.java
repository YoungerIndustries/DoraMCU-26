package com.yisy.modules.doramcu.star_trek_4x.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TechResearch Tests")
class TechResearchTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            TechResearch research = new TechResearch();
            assertNull(research.getId());
            assertNull(research.getName());
            assertEquals(0, research.getTargetTechLevel());
            assertNull(research.getCategory());
            assertEquals(0, research.getResearchPointsSpent());
            assertEquals(100, research.getResearchPointsNeeded());
            assertFalse(research.isCompleted());
            assertNull(research.getOwnerFactionId());
            assertNull(research.getGameSession());
        }
    }

    @Nested
    @DisplayName("Research points")
    class ResearchPoints {

        @Test
        @DisplayName("Add research points increases spent")
        void addPoints() {
            TechResearch research = new TechResearch();
            research.addResearchPoints(30);
            assertEquals(30, research.getResearchPointsSpent());
        }

        @Test
        @DisplayName("Multiple additions accumulate")
        void multipleAdditions() {
            TechResearch research = new TechResearch();
            research.addResearchPoints(30);
            research.addResearchPoints(20);
            research.addResearchPoints(10);
            assertEquals(60, research.getResearchPointsSpent());
        }

        @Test
        @DisplayName("Auto-complete when points meet threshold")
        void autoComplete() {
            TechResearch research = new TechResearch();
            research.setResearchPointsNeeded(100);
            research.addResearchPoints(100);
            assertTrue(research.isCompleted());
        }

        @Test
        @DisplayName("Auto-complete when points exceed threshold")
        void autoCompleteExceed() {
            TechResearch research = new TechResearch();
            research.setResearchPointsNeeded(100);
            research.addResearchPoints(150);
            assertTrue(research.isCompleted());
        }

        @Test
        @DisplayName("Not completed when points below threshold")
        void notCompleted() {
            TechResearch research = new TechResearch();
            research.setResearchPointsNeeded(100);
            research.addResearchPoints(99);
            assertFalse(research.isCompleted());
        }
    }

    @Nested
    @DisplayName("Progress calculation")
    class ProgressCalculation {

        @Test
        @DisplayName("Zero progress at start")
        void zeroProgress() {
            TechResearch research = new TechResearch();
            research.setResearchPointsNeeded(100);
            assertEquals(0.0, research.getProgress());
        }

        @Test
        @DisplayName("50% progress at half points")
        void halfProgress() {
            TechResearch research = new TechResearch();
            research.setResearchPointsNeeded(100);
            research.addResearchPoints(50);
            assertEquals(50.0, research.getProgress());
        }

        @Test
        @DisplayName("100% progress at full points")
        void fullProgress() {
            TechResearch research = new TechResearch();
            research.setResearchPointsNeeded(100);
            research.addResearchPoints(100);
            assertEquals(100.0, research.getProgress());
        }

        @Test
        @DisplayName("Progress with custom threshold")
        void customThreshold() {
            TechResearch research = new TechResearch();
            research.setResearchPointsNeeded(200);
            research.addResearchPoints(50);
            assertEquals(25.0, research.getProgress());
        }
    }

    @Nested
    @DisplayName("ResearchCategory enum")
    class ResearchCategoryEnum {

        @Test
        @DisplayName("All research category values")
        void allCategories() {
            assertSame(TechResearch.ResearchCategory.WEAPON, TechResearch.ResearchCategory.valueOf("WEAPON"));
            assertSame(TechResearch.ResearchCategory.TRANSPORT, TechResearch.ResearchCategory.valueOf("TRANSPORT"));
            assertSame(TechResearch.ResearchCategory.COMMS, TechResearch.ResearchCategory.valueOf("COMMS"));
            assertSame(TechResearch.ResearchCategory.COMPUTING, TechResearch.ResearchCategory.valueOf("COMPUTING"));
            assertSame(TechResearch.ResearchCategory.MEDICINE, TechResearch.ResearchCategory.valueOf("MEDICINE"));
            assertSame(TechResearch.ResearchCategory.INDUSTRY, TechResearch.ResearchCategory.valueOf("INDUSTRY"));
        }

        @Test
        @DisplayName("Set category")
        void setCategory() {
            TechResearch research = new TechResearch();
            research.setCategory(TechResearch.ResearchCategory.WEAPON);
            assertEquals(TechResearch.ResearchCategory.WEAPON, research.getCategory());
        }
    }

    @Nested
    @DisplayName("Tech level")
    class TechLevel {

        @Test
        @DisplayName("Set target tech level")
        void setTargetTechLevel() {
            TechResearch research = new TechResearch();
            research.setTargetTechLevel(15);
            assertEquals(15, research.getTargetTechLevel());
        }
    }
}

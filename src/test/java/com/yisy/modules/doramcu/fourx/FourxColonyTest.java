package com.yisy.modules.doramcu.fourx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxColony Tests")
class FourxColonyTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            FourxColony colony = new FourxColony();
            assertNull(colony.getId());
            assertNull(colony.getName());
            assertEquals(0, colony.getPopulation());
            assertEquals(0, colony.getMineralsPerTurn());
            assertEquals(0, colony.getEnergyPerTurn());
            assertEquals(0, colony.getFoodPerTurn());
            assertEquals(0, colony.getResearchPerTurn());
            assertTrue(colony.getBuildings().isEmpty());
            assertTrue(colony.isOperational());
        }

        @Test
        @DisplayName("Parameterized constructor sets fields")
        void parameterized() {
            FourxColony colony = new FourxColony("c1", "New Earth", "f1", "alpha-centauri");
            assertEquals("c1", colony.getId());
            assertEquals("New Earth", colony.getName());
            assertEquals("f1", colony.getOwnerFactionId());
            assertEquals("alpha-centauri", colony.getStarSystemId());
        }
    }

    @Nested
    @DisplayName("Buildings")
    class Buildings {

        @Test
        @DisplayName("Add building")
        void addBuilding() {
            FourxColony colony = new FourxColony("c1", "New Earth", "f1", "alpha-centauri");
            colony.addBuilding("Factory");
            colony.addBuilding("Mine");
            assertEquals(2, colony.getBuildings().size());
            assertTrue(colony.getBuildings().contains("Factory"));
            assertTrue(colony.getBuildings().contains("Mine"));
        }
    }

    @Nested
    @DisplayName("Setters and getters")
    class SettersAndGetters {

        @Test
        @DisplayName("Population")
        void population() {
            FourxColony colony = new FourxColony("c1", "New Earth", "f1", "alpha-centauri");
            colony.setPopulation(1_000_000L);
            assertEquals(1_000_000L, colony.getPopulation());
        }

        @Test
        @DisplayName("Resource production")
        void resources() {
            FourxColony colony = new FourxColony("c1", "New Earth", "f1", "alpha-centauri");
            colony.setMineralsPerTurn(100);
            colony.setEnergyPerTurn(50);
            colony.setFoodPerTurn(200);
            colony.setResearchPerTurn(25);
            assertEquals(100, colony.getMineralsPerTurn());
            assertEquals(50, colony.getEnergyPerTurn());
            assertEquals(200, colony.getFoodPerTurn());
            assertEquals(25, colony.getResearchPerTurn());
        }

        @Test
        @DisplayName("Operational status")
        void operational() {
            FourxColony colony = new FourxColony("c1", "New Earth", "f1", "alpha-centauri");
            assertTrue(colony.isOperational());
            colony.setOperational(false);
            assertFalse(colony.isOperational());
        }
    }
}

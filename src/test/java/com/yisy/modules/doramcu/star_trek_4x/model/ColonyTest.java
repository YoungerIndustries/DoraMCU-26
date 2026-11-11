package com.yisy.modules.doramcu.star_trek_4x.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Colony Tests")
class ColonyTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            Colony colony = new Colony();
            assertNull(colony.getId());
            assertNull(colony.getName());
            assertEquals(0, colony.getPopulation());
            assertEquals(0, colony.getMineralsPerTurn());
            assertEquals(0, colony.getEnergyPerTurn());
            assertEquals(0, colony.getFoodPerTurn());
            assertEquals(0, colony.getResearchPerTurn());
            assertTrue(colony.getBuildings().isEmpty());
            assertNull(colony.getOwnerFactionId());
            assertNull(colony.getStarSystemId());
            assertNull(colony.getEarth2026CountryId());
            assertTrue(colony.isOperational());
            assertNull(colony.getGameSession());
        }
    }

    @Nested
    @DisplayName("Buildings")
    class Buildings {

        @Test
        @DisplayName("Add building adds to list")
        void addBuilding() {
            Colony colony = new Colony();
            colony.addBuilding("Factory");
            colony.addBuilding("Mine");
            assertEquals(2, colony.getBuildings().size());
            assertTrue(colony.getBuildings().contains("Factory"));
            assertTrue(colony.getBuildings().contains("Mine"));
        }

        @Test
        @DisplayName("Multiple buildings")
        void multipleBuildings() {
            Colony colony = new Colony();
            colony.addBuilding("Factory");
            colony.addBuilding("Mine");
            colony.addBuilding("Lab");
            colony.addBuilding("Farm");
            assertEquals(4, colony.getBuildings().size());
        }
    }

    @Nested
    @DisplayName("Resource production")
    class ResourceProduction {

        @Test
        @DisplayName("Set resource values")
        void setResources() {
            Colony colony = new Colony();
            colony.setMineralsPerTurn(10);
            colony.setEnergyPerTurn(20);
            colony.setFoodPerTurn(15);
            colony.setResearchPerTurn(5);
            assertEquals(10, colony.getMineralsPerTurn());
            assertEquals(20, colony.getEnergyPerTurn());
            assertEquals(15, colony.getFoodPerTurn());
            assertEquals(5, colony.getResearchPerTurn());
        }
    }

    @Nested
    @DisplayName("Operational status")
    class OperationalStatus {

        @Test
        @DisplayName("Default is operational")
        void defaultOperational() {
            Colony colony = new Colony();
            assertTrue(colony.isOperational());
        }

        @Test
        @DisplayName("Set operational to false")
        void setNonOperational() {
            Colony colony = new Colony();
            colony.setOperational(false);
            assertFalse(colony.isOperational());
        }
    }

    @Nested
    @DisplayName("Population")
    class Population {

        @Test
        @DisplayName("Set population")
        void setPopulation() {
            Colony colony = new Colony();
            colony.setPopulation(1000000L);
            assertEquals(1000000L, colony.getPopulation());
        }
    }
}

package com.yisy.modules.doramcu.fourx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxColonyEntity Tests")
class FourxColonyEntityTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Parameterized constructor sets fields")
        void parameterized() {
            FourxColonyEntity entity = new FourxColonyEntity("c1", "New Earth", "f1", "alpha-centauri");
            assertEquals("c1", entity.getColonyId());
            assertEquals("New Earth", entity.getName());
            assertEquals("f1", entity.getOwnerFactionId());
            assertEquals("alpha-centauri", entity.getStarSystemId());
            assertEquals(0, entity.getPopulation());
            assertTrue(entity.isOperational());
            assertNotNull(entity.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("toDto conversion")
    class ToDto {

        @Test
        @DisplayName("Converts all fields correctly")
        void convertsAllFields() {
            FourxColonyEntity entity = new FourxColonyEntity("c1", "New Earth", "f1", "alpha-centauri");
            entity.setPopulation(1_000_000L);
            entity.setMineralsPerTurn(100);
            entity.setEnergyPerTurn(50);
            entity.setFoodPerTurn(200);
            entity.setResearchPerTurn(25);
            entity.setOperational(false);

            FourxColony dto = entity.toDto();
            assertEquals("c1", dto.getId());
            assertEquals("New Earth", dto.getName());
            assertEquals("f1", dto.getOwnerFactionId());
            assertEquals("alpha-centauri", dto.getStarSystemId());
            assertEquals(1_000_000L, dto.getPopulation());
            assertEquals(100, dto.getMineralsPerTurn());
            assertEquals(50, dto.getEnergyPerTurn());
            assertEquals(200, dto.getFoodPerTurn());
            assertEquals(25, dto.getResearchPerTurn());
            assertFalse(dto.isOperational());
        }
    }

    @Nested
    @DisplayName("Setters and getters")
    class SettersAndGetters {

        @Test
        @DisplayName("Resource production")
        void resources() {
            FourxColonyEntity entity = new FourxColonyEntity("c1", "New Earth", "f1", "alpha-centauri");
            entity.setMineralsPerTurn(100);
            entity.setEnergyPerTurn(50);
            entity.setFoodPerTurn(200);
            entity.setResearchPerTurn(25);
            assertEquals(100, entity.getMineralsPerTurn());
            assertEquals(50, entity.getEnergyPerTurn());
            assertEquals(200, entity.getFoodPerTurn());
            assertEquals(25, entity.getResearchPerTurn());
        }

        @Test
        @DisplayName("Buildings JSON")
        void buildingsJson() {
            FourxColonyEntity entity = new FourxColonyEntity("c1", "New Earth", "f1", "alpha-centauri");
            entity.setBuildingsJson("[\"Factory\",\"Mine\"]");
            assertEquals("[\"Factory\",\"Mine\"]", entity.getBuildingsJson());
        }
    }
}

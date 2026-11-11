package com.yisy.modules.doramcu.fourx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxDiplomaticRelationEntity Tests")
class FourxDiplomaticRelationEntityTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Parameterized constructor sets fields")
        void parameterized() {
            FourxDiplomaticRelationEntity entity = new FourxDiplomaticRelationEntity("rel1", "f1", "f2");
            assertEquals("rel1", entity.getRelationId());
            assertEquals("f1", entity.getFaction1Id());
            assertEquals("f2", entity.getFaction2Id());
            assertEquals(0, entity.getRelationScore());
            assertFalse(entity.isAtWar());
            assertNotNull(entity.getLastEventAt());
            assertNotNull(entity.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Relation score clamping")
    class RelationScoreClamping {

        @Test
        @DisplayName("Set score clamps at 100")
        void clampMax() {
            FourxDiplomaticRelationEntity entity = new FourxDiplomaticRelationEntity("rel1", "f1", "f2");
            entity.setRelationScore(150);
            assertEquals(100, entity.getRelationScore());
        }

        @Test
        @DisplayName("Set score clamps at -100")
        void clampMin() {
            FourxDiplomaticRelationEntity entity = new FourxDiplomaticRelationEntity("rel1", "f1", "f2");
            entity.setRelationScore(-150);
            assertEquals(-100, entity.getRelationScore());
        }
    }

    @Nested
    @DisplayName("toDto conversion")
    class ToDto {

        @Test
        @DisplayName("Converts all fields correctly")
        void convertsAllFields() {
            FourxDiplomaticRelationEntity entity = new FourxDiplomaticRelationEntity("rel1", "f1", "f2");
            entity.setRelationScore(50);
            entity.setAtWar(true);

            FourxDiplomaticRelation dto = entity.toDto();
            assertEquals("rel1", dto.getId());
            assertEquals("f1", dto.getFaction1Name());
            assertEquals("f2", dto.getFaction2Name());
            assertEquals(50, dto.getRelationScore());
            assertTrue(dto.isAtWar());
            assertNotNull(dto.getLastEventAt());
        }
    }

    @Nested
    @DisplayName("Setters and getters")
    class SettersAndGetters {

        @Test
        @DisplayName("War status")
        void warStatus() {
            FourxDiplomaticRelationEntity entity = new FourxDiplomaticRelationEntity("rel1", "f1", "f2");
            assertFalse(entity.isAtWar());
            entity.setAtWar(true);
            assertTrue(entity.isAtWar());
        }

        @Test
        @DisplayName("Treaties JSON")
        void treatiesJson() {
            FourxDiplomaticRelationEntity entity = new FourxDiplomaticRelationEntity("rel1", "f1", "f2");
            entity.setActiveTreatiesJson("[\"TRADE\"]");
            assertEquals("[\"TRADE\"]", entity.getActiveTreatiesJson());
        }
    }
}

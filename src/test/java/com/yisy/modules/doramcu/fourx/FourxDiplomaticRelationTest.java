package com.yisy.modules.doramcu.fourx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FourxDiplomaticRelation Tests")
class FourxDiplomaticRelationTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation();
            assertNull(relation.getId());
            assertNull(relation.getFaction1Name());
            assertNull(relation.getFaction2Name());
            assertEquals(0, relation.getRelationScore());
            assertFalse(relation.isAtWar());
            assertTrue(relation.getActiveTreaties().isEmpty());
        }

        @Test
        @DisplayName("Parameterized constructor sets factions")
        void parameterized() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation("Federation", "Klingons");
            assertEquals("Federation", relation.getFaction1Name());
            assertEquals("Klingons", relation.getFaction2Name());
            assertEquals(0, relation.getRelationScore());
            assertFalse(relation.isAtWar());
            assertNotNull(relation.getLastEventAt());
        }
    }

    @Nested
    @DisplayName("Relation score")
    class RelationScore {

        @Test
        @DisplayName("Set score clamps at 100")
        void clampMax() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation("A", "B");
            relation.setRelationScore(150);
            assertEquals(100, relation.getRelationScore());
        }

        @Test
        @DisplayName("Set score clamps at -100")
        void clampMin() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation("A", "B");
            relation.setRelationScore(-150);
            assertEquals(-100, relation.getRelationScore());
        }

        @Test
        @DisplayName("Modify relation adds delta")
        void modifyRelation() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation("A", "B");
            relation.modifyRelation(25);
            assertEquals(25, relation.getRelationScore());
            assertNotNull(relation.getLastEventAt());
        }

        @Test
        @DisplayName("Modify relation auto-sets war when below -50")
        void autoWar() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation("A", "B");
            relation.modifyRelation(-51);
            assertTrue(relation.isAtWar());
        }

        @Test
        @DisplayName("Modify relation clears war when above -50")
        void clearWar() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation("A", "B");
            relation.modifyRelation(-60);
            assertTrue(relation.isAtWar());
            relation.modifyRelation(15);
            assertFalse(relation.isAtWar());
            assertEquals(-45, relation.getRelationScore());
        }
    }

    @Nested
    @DisplayName("Treaties")
    class Treaties {

        @Test
        @DisplayName("Add treaty")
        void addTreaty() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation("A", "B");
            relation.addTreaty("TRADE");
            assertTrue(relation.hasTreaty("TRADE"));
            assertEquals(1, relation.getActiveTreaties().size());
        }

        @Test
        @DisplayName("Remove treaty")
        void removeTreaty() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation("A", "B");
            relation.addTreaty("TRADE");
            relation.addTreaty("MILITARY");
            relation.removeTreaty("TRADE");
            assertFalse(relation.hasTreaty("TRADE"));
            assertTrue(relation.hasTreaty("MILITARY"));
        }

        @Test
        @DisplayName("Has treaty returns false for missing treaty")
        void hasTreatyMissing() {
            FourxDiplomaticRelation relation = new FourxDiplomaticRelation("A", "B");
            assertFalse(relation.hasTreaty("NONEXISTENT"));
        }
    }
}

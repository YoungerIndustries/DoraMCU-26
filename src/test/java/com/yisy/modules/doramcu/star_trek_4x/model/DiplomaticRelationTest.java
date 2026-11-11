package com.yisy.modules.doramcu.star_trek_4x.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DiplomaticRelation Tests")
class DiplomaticRelationTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("Default constructor initializes defaults")
        void defaults() {
            DiplomaticRelation relation = new DiplomaticRelation();
            assertNull(relation.getId());
            assertNull(relation.getFaction1Name());
            assertNull(relation.getFaction2Name());
            assertEquals(0, relation.getRelationScore());
            assertFalse(relation.isAtWar());
            assertTrue(relation.getActiveTreaties().isEmpty());
            assertNull(relation.getLastEventAt());
            assertNull(relation.getGameSession());
        }

        @Test
        @DisplayName("Parameterized constructor sets names and timestamp")
        void parameterized() {
            GameSession session = new GameSession();
            DiplomaticRelation relation = new DiplomaticRelation("Federation", "Klingon", session);
            assertEquals("Federation", relation.getFaction1Name());
            assertEquals("Klingon", relation.getFaction2Name());
            assertSame(session, relation.getGameSession());
            assertNotNull(relation.getLastEventAt());
        }
    }

    @Nested
    @DisplayName("Relation score clamping")
    class RelationScoreClamping {

        @Test
        @DisplayName("Score clamps at 100")
        void clampMax() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.setRelationScore(150);
            assertEquals(100, relation.getRelationScore());
        }

        @Test
        @DisplayName("Score clamps at -100")
        void clampMin() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.setRelationScore(-150);
            assertEquals(-100, relation.getRelationScore());
        }

        @Test
        @DisplayName("Score within range is unchanged")
        void withinRange() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.setRelationScore(50);
            assertEquals(50, relation.getRelationScore());
        }
    }

    @Nested
    @DisplayName("Modify relation")
    class ModifyRelation {

        @Test
        @DisplayName("Modify adds delta and updates timestamp")
        void modifyPositive() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.modifyRelation(30);
            assertEquals(30, relation.getRelationScore());
            assertNotNull(relation.getLastEventAt());
        }

        @Test
        @DisplayName("Auto-set war when score below -50")
        void autoWar() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.modifyRelation(-51);
            assertTrue(relation.isAtWar());
        }

        @Test
        @DisplayName("Not at war when score is -50")
        void notAtWarAtThreshold() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.modifyRelation(-50);
            assertFalse(relation.isAtWar());
        }

        @Test
        @DisplayName("Multiple modifications clamp at bounds")
        void multipleMods() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.modifyRelation(60);
            relation.modifyRelation(50);
            assertEquals(100, relation.getRelationScore());
        }
    }

    @Nested
    @DisplayName("Treaty management")
    class TreatyManagement {

        @Test
        @DisplayName("Add treaty adds to list")
        void addTreaty() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.addTreaty("Peace");
            relation.addTreaty("Trade");
            assertEquals(2, relation.getActiveTreaties().size());
            assertTrue(relation.hasTreaty("Peace"));
            assertTrue(relation.hasTreaty("Trade"));
        }

        @Test
        @DisplayName("Remove treaty removes from list")
        void removeTreaty() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.addTreaty("Peace");
            relation.addTreaty("Trade");
            relation.removeTreaty("Peace");
            assertEquals(1, relation.getActiveTreaties().size());
            assertFalse(relation.hasTreaty("Peace"));
            assertTrue(relation.hasTreaty("Trade"));
        }

        @Test
        @DisplayName("Has treaty returns false for missing treaty")
        void hasTreatyFalse() {
            DiplomaticRelation relation = new DiplomaticRelation();
            relation.addTreaty("Peace");
            assertFalse(relation.hasTreaty("Alliance"));
        }
    }
}

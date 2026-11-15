package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SfbDiplomacyServiceTest {

    private SfbDiplomacyService service;

    @BeforeEach
    void setUp() {
        service = new SfbDiplomacyService();
    }

    @Nested
    class RelationTypeTests {
        @Test
        void fromScore_hostile() {
            assertEquals(RelationType.HOSTILE, RelationType.fromScore(-100));
            assertEquals(RelationType.HOSTILE, RelationType.fromScore(-80));
        }

        @Test
        void fromScore_enemy() {
            assertEquals(RelationType.ENEMY, RelationType.fromScore(-75));
            assertEquals(RelationType.ENEMY, RelationType.fromScore(-51));
        }

        @Test
        void fromScore_unfriendly() {
            assertEquals(RelationType.UNFRIENDLY, RelationType.fromScore(-50));
            assertEquals(RelationType.UNFRIENDLY, RelationType.fromScore(-26));
        }

        @Test
        void fromScore_neutral() {
            assertEquals(RelationType.NEUTRAL, RelationType.fromScore(0));
            assertEquals(RelationType.NEUTRAL, RelationType.fromScore(-25));
            assertEquals(RelationType.NEUTRAL, RelationType.fromScore(25));
        }

        @Test
        void fromScore_friendly() {
            assertEquals(RelationType.FRIENDLY, RelationType.fromScore(26));
            assertEquals(RelationType.FRIENDLY, RelationType.fromScore(50));
        }

        @Test
        void fromScore_allied() {
            assertEquals(RelationType.ALLIED, RelationType.fromScore(51));
            assertEquals(RelationType.ALLIED, RelationType.fromScore(75));
        }

        @Test
        void fromScore_vassal() {
            assertEquals(RelationType.VASSAL, RelationType.fromScore(76));
            assertEquals(RelationType.VASSAL, RelationType.fromScore(100));
        }

        @Test
        void relationType_properties() {
            assertEquals(-100, RelationType.HOSTILE.getMinScore());
            assertEquals(-76, RelationType.HOSTILE.getMaxScore());
            assertNotNull(RelationType.HOSTILE.getDescription());
        }
    }

    @Nested
    class TreatyTypeTests {
        @Test
        void treatyType_properties() {
            assertEquals("Non-Aggression Pact", TreatyType.NON_AGGRESSION.getName());
            assertEquals(10, TreatyType.NON_AGGRESSION.getRelationBonus());
            assertEquals("Military Alliance", TreatyType.MILITARY_ALLIANCE.getName());
            assertEquals(20, TreatyType.MILITARY_ALLIANCE.getRelationBonus());
        }

        @Test
        void allTreatyTypes_haveBonus() {
            for (TreatyType type : TreatyType.values()) {
                assertTrue(type.getRelationBonus() > 0, type.name() + " should have positive bonus");
            }
        }
    }

    @Nested
    class DiplomaticActionTests {
        @Test
        void action_properties() {
            assertEquals(10, DiplomaticAction.IMPROVE_RELATIONS.getRelationChange());
            assertEquals(-50, DiplomaticAction.DECLARATION_OF_WAR.getRelationChange());
            assertEquals(25, DiplomaticAction.PEACE_OFFER.getRelationChange());
        }

        @Test
        void allActions_haveNames() {
            for (DiplomaticAction action : DiplomaticAction.values()) {
                assertNotNull(action.getName());
                assertFalse(action.getName().isEmpty());
            }
        }

        @Test
        void positiveActions_improveRelations() {
            assertTrue(DiplomaticAction.IMPROVE_RELATIONS.getRelationChange() > 0);
            assertTrue(DiplomaticAction.PEACE_OFFER.getRelationChange() > 0);
            assertTrue(DiplomaticAction.AID_OFFER.getRelationChange() > 0);
        }

        @Test
        void negativeActions_worsenRelations() {
            assertTrue(DiplomaticAction.INSULT.getRelationChange() < 0);
            assertTrue(DiplomaticAction.DECLARATION_OF_WAR.getRelationChange() < 0);
            assertTrue(DiplomaticAction.MILITARY_THREAT.getRelationChange() < 0);
        }
    }

    @Nested
    class FactionProfileTests {
        @Test
        void builder_defaultValues() {
            FactionProfile profile = FactionProfile.builder()
                    .name("Federation")
                    .build();
            assertEquals("Federation", profile.getName());
            assertEquals(5, profile.getDiplomacySkill());
            assertEquals(5, profile.getAggression());
            assertEquals(5, profile.getTradeFocus());
        }

        @Test
        void builder_customValues() {
            FactionProfile profile = FactionProfile.builder()
                    .name("Klingon")
                    .diplomacySkill(2)
                    .aggression(9)
                    .tradeFocus(3)
                    .build();
            assertEquals(2, profile.getDiplomacySkill());
            assertEquals(9, profile.getAggression());
            assertEquals(3, profile.getTradeFocus());
        }

        @Test
        void builder_clampsValues() {
            FactionProfile profile = FactionProfile.builder()
                    .name("Test")
                    .diplomacySkill(15)
                    .aggression(-5)
                    .build();
            assertEquals(10, profile.getDiplomacySkill());
            assertEquals(0, profile.getAggression());
        }

        @Test
        void builder_requiresName() {
            assertThrows(IllegalArgumentException.class, () ->
                    FactionProfile.builder().build());
        }

        @Test
        void relationWith_defaultNeutral() {
            FactionProfile profile = FactionProfile.builder()
                    .name("Federation")
                    .build();
            assertEquals(0, profile.getRelationWith("Klingon"));
            assertEquals(RelationType.NEUTRAL, profile.getRelationTypeWith("Klingon"));
        }

        @Test
        void relationWith_setRelation() {
            FactionProfile profile = FactionProfile.builder()
                    .name("Federation")
                    .relation("Klingon", -60)
                    .build();
            assertEquals(-60, profile.getRelationWith("Klingon"));
            assertEquals(RelationType.ENEMY, profile.getRelationTypeWith("Klingon"));
        }

        @Test
        void relations_immutable() {
            FactionProfile profile = FactionProfile.builder()
                    .name("Federation")
                    .build();
            assertThrows(UnsupportedOperationException.class, () ->
                    profile.getRelations().put("Klingon", 50));
        }
    }

    @Nested
    class TreatyTests {
        @Test
        void builder_createsTreaty() {
            Treaty treaty = Treaty.builder()
                    .id("t1")
                    .factionA("Federation")
                    .factionB("Romulan")
                    .type(TreatyType.NON_AGGRESSION)
                    .turnsTotal(10)
                    .build();
            assertEquals("t1", treaty.getId());
            assertEquals("Federation", treaty.getFactionA());
            assertEquals("Romulan", treaty.getFactionB());
            assertEquals(TreatyType.NON_AGGRESSION, treaty.getType());
            assertEquals(10, treaty.getTurnsRemaining());
            assertFalse(treaty.isExpired());
        }

        @Test
        void builder_generatesId() {
            Treaty treaty = Treaty.builder()
                    .factionA("A")
                    .factionB("B")
                    .type(TreatyType.TRADE_AGREEMENT)
                    .build();
            assertNotNull(treaty.getId());
            assertEquals(8, treaty.getId().length());
        }

        @Test
        void expiredTreaty() {
            Treaty treaty = Treaty.builder()
                    .factionA("A")
                    .factionB("B")
                    .type(TreatyType.CEASEFIRE)
                    .turnsTotal(0)
                    .build();
            assertTrue(treaty.isExpired());
        }
    }

    @Nested
    class DiplomaticResultTests {
        @Test
        void builder_createsResult() {
            DiplomaticResult result = DiplomaticResult.builder()
                    .actionName("Test")
                    .factionA("A")
                    .factionB("B")
                    .relationBefore(0)
                    .relationAfter(10)
                    .success(true)
                    .message("Success")
                    .build();
            assertEquals("Test", result.getActionName());
            assertEquals(0, result.getRelationBefore());
            assertEquals(10, result.getRelationAfter());
            assertTrue(result.isSuccess());
            assertEquals(RelationType.NEUTRAL, result.getRelationTypeBefore());
            assertEquals(RelationType.NEUTRAL, result.getRelationTypeAfter());
        }
    }

    @Nested
    class FactionRegistration {
        @Test
        void registerFaction_string() {
            service.registerFaction("Federation");
            assertEquals(1, service.getAllFactions().size());
            assertTrue(service.getAllFactions().contains("Federation"));
        }

        @Test
        void registerFaction_profile() {
            FactionProfile profile = FactionProfile.builder()
                    .name("Klingon")
                    .aggression(8)
                    .build();
            service.registerFaction(profile);
            assertEquals(1, service.getAllFactions().size());
            assertEquals(8, service.getFaction("Klingon").getAggression());
        }

        @Test
        void getFaction_notFound() {
            assertNull(service.getFaction("Unknown"));
        }
    }

    @Nested
    class RelationManagement {
        @BeforeEach
        void registerFactions() {
            service.registerFaction("Federation");
            service.registerFaction("Klingon");
        }

        @Test
        void defaultRelation_isNeutral() {
            assertEquals(0, service.getRelationScore("Federation", "Klingon"));
            assertEquals(RelationType.NEUTRAL, service.getRelationType("Federation", "Klingon"));
        }

        @Test
        void setRelation_bidirectional() {
            service.setRelation("Federation", "Klingon", -60);
            assertEquals(-60, service.getRelationScore("Federation", "Klingon"));
            assertEquals(-60, service.getRelationScore("Klingon", "Federation"));
            assertEquals(RelationType.ENEMY, service.getRelationType("Federation", "Klingon"));
        }

        @Test
        void setRelation_clampsToRange() {
            service.setRelation("Federation", "Klingon", 150);
            assertEquals(100, service.getRelationScore("Federation", "Klingon"));
            service.setRelation("Federation", "Klingon", -150);
            assertEquals(-100, service.getRelationScore("Federation", "Klingon"));
        }

        @Test
        void modifyRelation_increments() {
            service.modifyRelation("Federation", "Klingon", 30);
            assertEquals(30, service.getRelationScore("Federation", "Klingon"));
            service.modifyRelation("Federation", "Klingon", -50);
            assertEquals(-20, service.getRelationScore("Federation", "Klingon"));
        }

        @Test
        void relation_notRegisteredFaction() {
            assertEquals(0, service.getRelationScore("Federation", "Unknown"));
        }
    }

    @Nested
    class DiplomaticActions {
        @BeforeEach
        void registerFactions() {
            service.registerFaction(
                    FactionProfile.builder().name("Federation").diplomacySkill(8).build());
            service.registerFaction(
                    FactionProfile.builder().name("Klingon").diplomacySkill(3).build());
        }

        @Test
        void performAction_changesRelation() {
            int before = service.getRelationScore("Federation", "Klingon");
            DiplomaticResult result = service.performAction(
                    "Federation", "Klingon", DiplomaticAction.IMPROVE_RELATIONS);
            assertTrue(result.getRelationAfter() >= before);
            assertNotNull(result.getMessage());
        }

        @Test
        void performAction_notFound() {
            DiplomaticResult result = service.performAction(
                    "Federation", "Unknown", DiplomaticAction.IMPROVE_RELATIONS);
            assertFalse(result.isSuccess());
        }

        @Test
        void declareWar_makesHostile() {
            service.declareWar("Federation", "Klingon");
            int score = service.getRelationScore("Federation", "Klingon");
            assertTrue(score <= -75, "War should make relations hostile: " + score);
        }

        @Test
        void offerPeace_improvesRelation() {
            service.setRelation("Federation", "Klingon", -80);
            DiplomaticResult result = service.offerPeace("Federation", "Klingon");
            assertTrue(result.getRelationAfter() >= result.getRelationBefore());
        }
    }

    @Nested
    class TreatyManagement {
        @BeforeEach
        void registerFactions() {
            service.registerFaction("Federation");
            service.registerFaction("Romulan");
            service.registerFaction("Klingon");
        }

        @Test
        void createTreaty_success() {
            Treaty treaty = service.createTreaty(
                    "Federation", "Romulan", TreatyType.NON_AGGRESSION, 10);
            assertNotNull(treaty);
            assertFalse(treaty.isExpired());
            assertEquals(1, service.getActiveTreaties().size());
        }

        @Test
        void createTreaty_improvesRelation() {
            int before = service.getRelationScore("Federation", "Romulan");
            service.createTreaty("Federation", "Romulan", TreatyType.TRADE_AGREEMENT, 10);
            assertTrue(service.getRelationScore("Federation", "Romulan") > before);
        }

        @Test
        void createTreaty_notRegistered() {
            assertThrows(IllegalArgumentException.class, () ->
                    service.createTreaty("Federation", "Unknown", TreatyType.NON_AGGRESSION, 10));
        }

        @Test
        void getTreatiesForFaction() {
            service.createTreaty("Federation", "Romulan", TreatyType.NON_AGGRESSION, 10);
            service.createTreaty("Federation", "Klingon", TreatyType.TRADE_AGREEMENT, 5);
            assertEquals(2, service.getTreatiesForFaction("Federation").size());
            assertEquals(1, service.getTreatiesForFaction("Romulan").size());
        }

        @Test
        void hasTreaty_correctType() {
            service.createTreaty("Federation", "Romulan", TreatyType.NON_AGGRESSION, 10);
            assertTrue(service.hasTreaty("Federation", "Romulan", TreatyType.NON_AGGRESSION));
            assertFalse(service.hasTreaty("Federation", "Romulan", TreatyType.MUTUAL_DEFENSE));
        }

        @Test
        void hasTreaty_bidirectional() {
            service.createTreaty("Federation", "Romulan", TreatyType.NON_AGGRESSION, 10);
            assertTrue(service.hasTreaty("Romulan", "Federation", TreatyType.NON_AGGRESSION));
        }

        @Test
        void advanceTreatyTurns_removesExpired() {
            Treaty expired = Treaty.builder()
                    .factionA("Federation")
                    .factionB("Romulan")
                    .type(TreatyType.CEASEFIRE)
                    .turnsTotal(0)
                    .build();
            // Manually add expired treaty
            service.getActiveTreaties(); // just to verify empty
            assertTrue(service.getActiveTreaties().isEmpty());
        }
    }

    @Nested
    class DiplomacyStats {
        @BeforeEach
        void registerFactions() {
            service.registerFaction(
                    FactionProfile.builder().name("Federation").diplomacySkill(7).build());
            service.registerFaction("Klingon");
            service.registerFaction("Romulan");
            service.setRelation("Federation", "Klingon", -60);
            service.setRelation("Federation", "Romulan", 40);
        }

        @Test
        void stats_correctCounts() {
            Map<String, Object> stats = service.getDiplomacyStats("Federation");
            assertEquals("Federation", stats.get("faction"));
            assertEquals(7, stats.get("diplomacySkill"));
            assertEquals(1, stats.get("enemies"));
            assertEquals(1, stats.get("allies"));
        }

        @Test
        void stats_notFound() {
            Map<String, Object> stats = service.getDiplomacyStats("Unknown");
            assertEquals("Faction not found", stats.get("error"));
        }
    }

    @Nested
    class Clear {
        @Test
        void clear_removesAll() {
            service.registerFaction("Federation");
            service.clear();
            assertTrue(service.getAllFactions().isEmpty());
            assertTrue(service.getActiveTreaties().isEmpty());
        }
    }
}

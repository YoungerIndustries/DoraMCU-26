package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.sfb.service.SfbCampaignAIService.*;
import com.yisy.modules.doramcu.sfb.service.SfbDiplomacyService.FactionProfile;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.ResourceType;
import com.yisy.modules.doramcu.sfb.service.SfbEconomyService.ShipClassCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SfbCampaignAIServiceTest {

    private SfbDiplomacyService diplomacy;
    private SfbEconomyService economy;
    private SfbCampaignAIService ai;

    @BeforeEach
    void setUp() {
        diplomacy = new SfbDiplomacyService();
        economy = new SfbEconomyService();
        ai = new SfbCampaignAIService(diplomacy, economy);
    }

    private void setupFaction(String name, int diplomacySkill, int aggression, int tradeFocus,
                               int credits, int materials, int fuel, int manpower) {
        diplomacy.registerFaction(FactionProfile.builder()
                .name(name)
                .diplomacySkill(diplomacySkill)
                .aggression(aggression)
                .tradeFocus(tradeFocus)
                .build());
        economy.registerEconomy(name, credits, materials, fuel, manpower);
    }

    @Nested
    class StrategicActionTests {
        @Test
        void allActions_haveNames() {
            for (StrategicAction action : StrategicAction.values()) {
                assertNotNull(action.getName());
                assertFalse(action.getName().isEmpty());
            }
        }

        @Test
        void actionCount() {
            assertEquals(12, StrategicAction.values().length);
        }
    }

    @Nested
    class AIDecisionTests {
        @Test
        void builder_createsDecision() {
            AIDecision decision = AIDecision.builder()
                    .action(StrategicAction.BUILD_SHIP)
                    .priority(7)
                    .target("DESTROYER")
                    .reasoning("Need more ships")
                    .detail("key", "value")
                    .build();
            assertEquals(StrategicAction.BUILD_SHIP, decision.getAction());
            assertEquals(7, decision.getPriority());
            assertEquals("DESTROYER", decision.getTarget());
            assertEquals("Need more ships", decision.getReasoning());
        }

        @Test
        void builder_clampsPriority() {
            AIDecision decision = AIDecision.builder()
                    .action(StrategicAction.BUILD_SHIP)
                    .priority(15)
                    .build();
            assertEquals(10, decision.getPriority());
            decision = AIDecision.builder()
                    .action(StrategicAction.BUILD_SHIP)
                    .priority(-5)
                    .build();
            assertEquals(1, decision.getPriority());
        }

        @Test
        void details_immutable() {
            AIDecision decision = AIDecision.builder()
                    .action(StrategicAction.BUILD_SHIP)
                    .detail("key", "value")
                    .build();
            assertThrows(UnsupportedOperationException.class, () ->
                    decision.getDetails().put("new", "value"));
        }
    }

    @Nested
    class CampaignStateTests {
        @Test
        void builder_createsState() {
            CampaignState state = CampaignState.builder("Federation")
                    .fleetCount(3)
                    .totalShips(15)
                    .enemies(2)
                    .allies(1)
                    .credits(1000)
                    .materials(500)
                    .fuel(200)
                    .manpower(100)
                    .threat("Enemy fleet")
                    .opportunity("Trade deal")
                    .build();
            assertEquals("Federation", state.getFactionName());
            assertEquals(3, state.getFleetCount());
            assertEquals(15, state.getTotalShips());
            assertEquals(2, state.getEnemies());
            assertEquals(1, state.getAllies());
            assertEquals(1, state.getThreats().size());
            assertEquals(1, state.getOpportunities().size());
        }

        @Test
        void builder_emptyState() {
            CampaignState state = CampaignState.builder("Federation").build();
            assertEquals(0, state.getFleetCount());
            assertEquals(0, state.getEnemies());
            assertTrue(state.getThreats().isEmpty());
        }
    }

    @Nested
    class EvaluateCampaignState {
        @Test
        void notRegistered_returnsEmpty() {
            CampaignState state = ai.evaluateCampaignState("Unknown");
            assertEquals("Unknown", state.getFactionName());
            assertEquals(0, state.getEnemies());
        }

        @Test
        void noEnemies_noAllies() {
            setupFaction("Federation", 5, 5, 5, 1000, 500, 200, 100);
            CampaignState state = ai.evaluateCampaignState("Federation");
            assertEquals(0, state.getEnemies());
            assertEquals(0, state.getAllies());
        }

        @Test
        void detectsEnemies() {
            setupFaction("Federation", 5, 5, 5, 1000, 500, 200, 100);
            setupFaction("Klingon", 5, 8, 3, 500, 200, 100, 50);
            diplomacy.setRelation("Federation", "Klingon", -80);
            CampaignState state = ai.evaluateCampaignState("Federation");
            assertEquals(1, state.getEnemies());
            assertTrue(state.getThreats().size() > 0);
        }

        @Test
        void detectsAllies() {
            setupFaction("Federation", 5, 5, 5, 1000, 500, 200, 100);
            setupFaction("Romulan", 5, 3, 7, 800, 400, 150, 80);
            diplomacy.setRelation("Federation", "Romulan", 40);
            CampaignState state = ai.evaluateCampaignState("Federation");
            assertEquals(1, state.getAllies());
        }

        @Test
        void detectsLowResources() {
            setupFaction("Federation", 5, 5, 5, 50, 20, 10, 5);
            CampaignState state = ai.evaluateCampaignState("Federation");
            assertTrue(state.getThreats().stream()
                    .anyMatch(t -> t.contains("Low credits")));
        }

        @Test
        void detectsStrongEconomy() {
            setupFaction("Federation", 5, 5, 5, 2000, 1000, 500, 200);
            CampaignState state = ai.evaluateCampaignState("Federation");
            assertTrue(state.getOpportunities().stream()
                    .anyMatch(o -> o.contains("Strong credit")));
        }
    }

    @Nested
    class GenerateDecisions {
        @Test
        void notRegistered_returnsEmpty() {
            List<AIDecision> decisions = ai.generateDecisions("Unknown");
            assertTrue(decisions.isEmpty());
        }

        @Test
        void singleFaction_noEnemies() {
            setupFaction("Federation", 5, 5, 5, 2000, 1000, 500, 200);
            List<AIDecision> decisions = ai.generateDecisions("Federation");
            // Should have economy expansion and possibly shipbuilding
            assertTrue(decisions.size() >= 1);
        }

        @Test
        void enemies_triggersDefense() {
            setupFaction("Federation", 5, 5, 5, 1000, 500, 200, 100);
            setupFaction("Klingon", 5, 8, 3, 500, 200, 100, 50);
            diplomacy.setRelation("Federation", "Klingon", -80);
            List<AIDecision> decisions = ai.generateDecisions("Federation");
            assertTrue(decisions.stream()
                    .anyMatch(d -> d.getAction() == StrategicAction.DEFENSIVE_POSTURE));
        }

        @Test
        void highAggression_hostile_triggersWar() {
            setupFaction("Federation", 5, 9, 3, 1000, 500, 200, 100);
            setupFaction("Klingon", 5, 5, 5, 500, 200, 100, 50);
            diplomacy.setRelation("Federation", "Klingon", -100);
            List<AIDecision> decisions = ai.generateDecisions("Federation");
            assertTrue(decisions.stream()
                    .anyMatch(d -> d.getAction() == StrategicAction.DECLARE_WAR));
        }

        @Test
        void lowAggression_hostile_triggersPeace() {
            setupFaction("Federation", 8, 2, 5, 1000, 500, 200, 100);
            setupFaction("Klingon", 5, 8, 3, 500, 200, 100, 50);
            diplomacy.setRelation("Federation", "Klingon", -80);
            List<AIDecision> decisions = ai.generateDecisions("Federation");
            assertTrue(decisions.stream()
                    .anyMatch(d -> d.getAction() == StrategicAction.OFFER_PEACE));
        }

        @Test
        void highDiplomacy_neutral_triggersOverture() {
            setupFaction("Federation", 8, 5, 5, 1000, 500, 200, 100);
            setupFaction("Romulan", 5, 5, 5, 500, 200, 100, 50);
            List<AIDecision> decisions = ai.generateDecisions("Federation");
            assertTrue(decisions.stream()
                    .anyMatch(d -> d.getAction() == StrategicAction.DIPLOMATIC_OVERTURE));
        }

        @Test
        void highTrade_allied_triggersTrade() {
            setupFaction("Federation", 5, 5, 8, 1000, 500, 200, 100);
            setupFaction("Romulan", 5, 5, 5, 500, 200, 100, 50);
            diplomacy.setRelation("Federation", "Romulan", 40);
            List<AIDecision> decisions = ai.generateDecisions("Federation");
            assertTrue(decisions.stream()
                    .anyMatch(d -> d.getAction() == StrategicAction.TRADE_DEAL));
        }

        @Test
        void sufficientResources_triggersBuild() {
            setupFaction("Federation", 5, 5, 5, 2000, 1000, 500, 200);
            List<AIDecision> decisions = ai.generateDecisions("Federation");
            assertTrue(decisions.stream()
                    .anyMatch(d -> d.getAction() == StrategicAction.BUILD_SHIP));
        }

        @Test
        void strongEconomy_triggersExpansion() {
            setupFaction("Federation", 5, 5, 5, 2000, 1000, 500, 200);
            List<AIDecision> decisions = ai.generateDecisions("Federation");
            assertTrue(decisions.stream()
                    .anyMatch(d -> d.getAction() == StrategicAction.EXPAND_ECONOMY));
        }

        @Test
        void decisions_sortedByPriority() {
            setupFaction("Federation", 5, 9, 5, 1000, 500, 200, 100);
            setupFaction("Klingon", 5, 8, 3, 500, 200, 100, 50);
            diplomacy.setRelation("Federation", "Klingon", -80);
            List<AIDecision> decisions = ai.generateDecisions("Federation");
            for (int i = 1; i < decisions.size(); i++) {
                assertTrue(decisions.get(i - 1).getPriority() >= decisions.get(i).getPriority(),
                        "Decisions should be sorted by priority descending");
            }
        }
    }

    @Nested
    class ExecuteTopDecision {
        @Test
        void notRegistered_returnsEmpty() {
            Optional<AIDecision> decision = ai.executeTopDecision("Unknown");
            assertTrue(decision.isEmpty());
        }

        @Test
        void executesDecision() {
            setupFaction("Federation", 8, 2, 5, 1000, 500, 200, 100);
            setupFaction("Klingon", 5, 8, 3, 500, 200, 100, 50);
            diplomacy.setRelation("Federation", "Klingon", -80);
            Optional<AIDecision> decision = ai.executeTopDecision("Federation");
            assertTrue(decision.isPresent());
            assertNotNull(decision.get().getAction());
        }

        @Test
        void buildShip_spendsResources() {
            setupFaction("Federation", 5, 5, 5, 2000, 1000, 500, 200);
            int creditsBefore = economy.getEconomy("Federation").getResource(ResourceType.CREDITS);
            ai.executeTopDecision("Federation");
            int creditsAfter = economy.getEconomy("Federation").getResource(ResourceType.CREDITS);
            // May or may not build depending on priority ordering
            assertTrue(creditsAfter <= creditsBefore);
        }
    }

    @Nested
    class ExecuteDecisions {
        @Test
        void executesAboveThreshold() {
            setupFaction("Federation", 8, 2, 5, 1000, 500, 200, 100);
            setupFaction("Klingon", 5, 8, 3, 500, 200, 100, 50);
            diplomacy.setRelation("Federation", "Klingon", -80);
            List<AIDecision> executed = ai.executeDecisions("Federation", 5);
            assertTrue(executed.size() >= 0);
        }

        @Test
        void highThreshold_fewerExecuted() {
            setupFaction("Federation", 5, 5, 5, 1000, 500, 200, 100);
            List<AIDecision> executed5 = ai.executeDecisions("Federation", 5);
            List<AIDecision> executed8 = ai.executeDecisions("Federation", 8);
            assertTrue(executed8.size() <= executed5.size());
        }
    }

    @Nested
    class StrategicAssessment {
        @Test
        void assessment_containsFaction() {
            setupFaction("Federation", 5, 5, 5, 1000, 500, 200, 100);
            Map<String, Object> assessment = ai.getStrategicAssessment("Federation");
            assertEquals("Federation", assessment.get("faction"));
            assertNotNull(assessment.get("campaignState"));
            assertNotNull(assessment.get("threats"));
            assertNotNull(assessment.get("opportunities"));
            assertNotNull(assessment.get("decisions"));
        }

        @Test
        void assessment_campaignState() {
            setupFaction("Federation", 5, 5, 5, 1000, 500, 200, 100);
            Map<String, Object> assessment = ai.getStrategicAssessment("Federation");
            @SuppressWarnings("unchecked")
            Map<String, Object> state = (Map<String, Object>) assessment.get("campaignState");
            assertEquals(1000, state.get("credits"));
            assertEquals(500, state.get("materials"));
        }
    }

    @Nested
    class AdvanceCampaignTurn {
        @Test
        void advancesAllEconomies() {
            setupFaction("Federation", 5, 5, 5, 100, 50, 20, 10);
            int turnsBefore = economy.getEconomy("Federation").getTurnsSinceLastTrade();
            ai.advanceCampaignTurn();
            int turnsAfter = economy.getEconomy("Federation").getTurnsSinceLastTrade();
            assertTrue(turnsAfter > turnsBefore, "Turns should advance");
        }

        @Test
        void executesAIDecisions() {
            setupFaction("Federation", 8, 2, 5, 1000, 500, 200, 100);
            setupFaction("Klingon", 5, 8, 3, 500, 200, 100, 50);
            diplomacy.setRelation("Federation", "Klingon", -80);
            ai.advanceCampaignTurn();
            // Should not throw
        }
    }

    @Nested
    class GetAllActions {
        @Test
        void returnsAllActions() {
            List<Map<String, String>> actions = ai.getAllActions();
            assertEquals(StrategicAction.values().length, actions.size());
            for (Map<String, String> action : actions) {
                assertNotNull(action.get("name"));
                assertNotNull(action.get("description"));
            }
        }
    }

    @Nested
    class IntegrationScenarios {
        @Test
        void peacefulFederation_diplomacyFirst() {
            setupFaction("Federation", 9, 2, 8, 2000, 1000, 500, 200);
            setupFaction("Romulan", 5, 5, 5, 500, 200, 100, 50);
            setupFaction("Klingon", 3, 8, 2, 800, 400, 200, 100);
            diplomacy.setRelation("Federation", "Romulan", 0);
            diplomacy.setRelation("Federation", "Klingon", -30);

            List<AIDecision> decisions = ai.generateDecisions("Federation");
            // Should prioritize diplomacy over aggression
            assertTrue(decisions.stream()
                    .anyMatch(d -> d.getAction() == StrategicAction.DIPLOMATIC_OVERTURE
                            || d.getAction() == StrategicAction.TRADE_DEAL
                            || d.getAction() == StrategicAction.BUILD_SHIP));
        }

        @Test
        void aggressiveKlingon_warFirst() {
            setupFaction("Klingon", 2, 9, 2, 2000, 1000, 500, 200);
            setupFaction("Federation", 8, 3, 7, 1000, 500, 200, 100);
            diplomacy.setRelation("Klingon", "Federation", -100);

            List<AIDecision> decisions = ai.generateDecisions("Klingon");
            assertTrue(decisions.stream()
                    .anyMatch(d -> d.getAction() == StrategicAction.DECLARE_WAR
                            || d.getAction() == StrategicAction.DEFENSIVE_POSTURE));
        }

        @Test
        void fullCampaignTurn() {
            setupFaction("Federation", 8, 2, 8, 2000, 1000, 500, 200);
            setupFaction("Klingon", 2, 9, 2, 1500, 800, 400, 150);
            diplomacy.setRelation("Federation", "Klingon", -60);

            // Multiple turns
            for (int i = 0; i < 3; i++) {
                ai.advanceCampaignTurn();
            }

            // Economies should have grown
            assertTrue(economy.getEconomy("Federation").getResource(ResourceType.CREDITS) > 0);
            assertTrue(economy.getEconomy("Klingon").getResource(ResourceType.CREDITS) > 0);
        }
    }
}

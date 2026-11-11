package com.yisy.modules.doramcu.traveller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AirCampaignLog")
class AirCampaignLogTest {

    @Nested
    @DisplayName("Constructor")
    class Constructor {

        @Test
        @DisplayName("creates campaign with id and name")
        void createsWithIdAndName() {
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Test Campaign");

            assertEquals("camp-1", campaign.getCampaignId());
            assertEquals("Test Campaign", campaign.getCampaignName());
            assertTrue(campaign.getBattles().isEmpty());
        }

        @Test
        @DisplayName("default constructor creates empty campaign")
        void defaultConstructor() {
            AirCampaignLog campaign = new AirCampaignLog();

            assertNull(campaign.getCampaignId());
            assertNull(campaign.getCampaignName());
        }
    }

    @Nested
    @DisplayName("Add Battle")
    class AddBattle {

        @Test
        @DisplayName("adds battle experience to campaign")
        void addsBattle() {
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Test Campaign");
            AirBattleExperience exp = new AirBattleExperience("battle-1", "Luftwaffe", "Germany");
            campaign.addBattle(exp);

            assertEquals(1, campaign.getBattles().size());
            assertEquals(exp, campaign.getBattles().get(0));
        }

        @Test
        @DisplayName("adds multiple battles")
        void addsMultipleBattles() {
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Test Campaign");
            campaign.addBattle(new AirBattleExperience("battle-1", "Luftwaffe", "Germany"));
            campaign.addBattle(new AirBattleExperience("battle-2", "RAF", "UK"));

            assertEquals(2, campaign.getBattles().size());
        }
    }

    @Nested
    @DisplayName("Generate Summary")
    class GenerateSummary {

        @Test
        @DisplayName("generates summary with battle count")
        void generatesSummary() {
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Test Campaign");
            campaign.addBattle(new AirBattleExperience("battle-1", "Luftwaffe", "Germany"));
            campaign.setTotalAttackerLosses(5);
            campaign.setTotalDefenderLosses(3);

            String summary = campaign.generateSummary();

            assertTrue(summary.contains("Test Campaign"));
            assertTrue(summary.contains("1 battles"));
            assertTrue(summary.contains("5 attacker losses"));
            assertTrue(summary.contains("3 defender losses"));
        }

        @Test
        @DisplayName("generates summary with zero battles")
        void generatesEmptySummary() {
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Test Campaign");

            String summary = campaign.generateSummary();

            assertTrue(summary.contains("0 battles"));
        }
    }

    @Nested
    @DisplayName("Setters")
    class Setters {

        @Test
        @DisplayName("sets nationality and dates")
        void settersWork() {
            AirCampaignLog campaign = new AirCampaignLog("camp-1", "Test Campaign");
            campaign.setNationality("Germany");
            campaign.setStartDate("1940-01-01");
            campaign.setEndDate("1940-06-15");
            campaign.setTotalAttackerLosses(10);
            campaign.setTotalDefenderLosses(8);

            assertEquals("Germany", campaign.getNationality());
            assertEquals("1940-01-01", campaign.getStartDate());
            assertEquals("1940-06-15", campaign.getEndDate());
            assertEquals(10, campaign.getTotalAttackerLosses());
            assertEquals(8, campaign.getTotalDefenderLosses());
        }
    }
}

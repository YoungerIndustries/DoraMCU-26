package com.yisy.modules.doramcu.traveller.shipcombat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CampaignService Tests")
class CampaignServiceTest {

    private ShipCombatCampaignService service;

    @BeforeEach
    void setUp() {
        service = new ShipCombatCampaignService();
    }

    // ---- createCampaign ----

    @Nested
    @DisplayName("createCampaign Tests")
    class CreateCampaignTests {

        @Test
        @DisplayName("should create a campaign")
        void createCampaign_shouldCreate() {
            ShipCombatCampaignService.Campaign campaign = service.createCampaign(
                    "camp-1", "Core War", "Imperial vs Rebels",
                    "Core", "Imperial Navy", "Rebels");

            assertNotNull(campaign);
            assertEquals("camp-1", campaign.getId());
            assertEquals("Core War", campaign.getName());
            assertEquals("Core", campaign.getSector());
            assertEquals("Imperial Navy", campaign.getFaction0());
            assertEquals("Rebels", campaign.getFaction1());
        }

        @Test
        @DisplayName("should store campaign in memory")
        void createCampaign_shouldStore() {
            service.createCampaign(
                    "camp-2", "Test", "", "Sector", "F0", "F1");

            assertTrue(service.listCampaigns().contains("camp-2"));
        }

        @Test
        @DisplayName("should use default values")
        void createCampaign_shouldUseDefaults() {
            ShipCombatCampaignService.Campaign campaign = service.createCampaign(
                    "camp-3", "Test", null, null, null, null);

            assertEquals("", campaign.getDescription());
            assertEquals("Unknown Sector", campaign.getSector());
            assertEquals("Faction 0", campaign.getFaction0());
            assertEquals("Faction 1", campaign.getFaction1());
        }
    }

    // ---- getCampaign ----

    @Nested
    @DisplayName("getCampaign Tests")
    class GetCampaignTests {

        @Test
        @DisplayName("should return campaign by ID")
        void getCampaign_shouldReturn() {
            service.createCampaign("camp-4", "Test", "", "Sector", "F0", "F1");

            ShipCombatCampaignService.Campaign campaign = service.getCampaign("camp-4");
            assertEquals("camp-4", campaign.getId());
        }

        @Test
        @DisplayName("should throw for unknown campaign")
        void getCampaign_shouldThrow() {
            assertThrows(IllegalArgumentException.class, () -> service.getCampaign("nonexistent"));
        }
    }

    // ---- addBattle ----

    @Nested
    @DisplayName("addBattle Tests")
    class AddBattleTests {

        @Test
        @DisplayName("should add a battle to a campaign")
        void addBattle_shouldAdd() {
            service.createCampaign("camp-5", "Test", "", "Sector", "F0", "F1");
            ShipCombatCampaignService.Campaign campaign = service.addBattle("camp-5", "battle-1", "SIDE_0_VICTORY", 5);

            assertEquals(1, campaign.getBattles().size());
            assertEquals("battle-1", campaign.getBattles().get(0).getBattleId());
            assertEquals("SIDE_0_VICTORY", campaign.getBattles().get(0).getOutcome());
            assertEquals(5, campaign.getBattles().get(0).getRounds());
        }

        @Test
        @DisplayName("should count faction 0 wins")
        void addBattle_shouldCountWins() {
            service.createCampaign("camp-6", "Test", "", "Sector", "F0", "F1");
            service.addBattle("camp-6", "b1", "SIDE_0_VICTORY", 3);
            service.addBattle("camp-6", "b2", "SIDE_1_VICTORY", 4);
            service.addBattle("camp-6", "b3", "SIDE_0_VICTORY", 2);

            ShipCombatCampaignService.Campaign campaign = service.getCampaign("camp-6");
            assertEquals(2, campaign.getFaction0Wins());
            assertEquals(1, campaign.getFaction1Wins());
        }

        @Test
        @DisplayName("should count stalemates")
        void addBattle_shouldCountStalemates() {
            service.createCampaign("camp-7", "Test", "", "Sector", "F0", "F1");
            service.addBattle("camp-7", "b1", "STALEMATE", 10);
            service.addBattle("camp-7", "b2", "MUTUAL_DESTRUCTION", 8);

            ShipCombatCampaignService.Campaign campaign = service.getCampaign("camp-7");
            assertEquals(2, campaign.getStalemates());
        }

        @Test
        @DisplayName("should throw for unknown campaign")
        void addBattle_shouldThrow() {
            assertThrows(IllegalArgumentException.class,
                    () -> service.addBattle("nonexistent", "b1", "WIN", 1));
        }
    }

    // ---- toSummary ----

    @Nested
    @DisplayName("toSummary Tests")
    class ToSummaryTests {

        @Test
        @DisplayName("should return complete summary")
        void toSummary_shouldReturnComplete() {
            service.createCampaign("camp-8", "Core War", "Description", "Core", "Imperial", "Rebels");
            service.addBattle("camp-8", "b1", "SIDE_0_VICTORY", 5);

            Map<String, Object> summary = service.getCampaign("camp-8").toSummary();

            assertEquals("camp-8", summary.get("id"));
            assertEquals("Core War", summary.get("name"));
            assertEquals("Core", summary.get("sector"));
            assertEquals(1, summary.get("totalBattles"));
            assertEquals(1, summary.get("faction0Wins"));
        }
    }

    // ---- listCampaigns ----

    @Nested
    @DisplayName("listCampaigns Tests")
    class ListCampaignsTests {

        @Test
        @DisplayName("should return empty list initially")
        void listCampaigns_shouldReturnEmpty() {
            assertTrue(service.listCampaigns().isEmpty());
        }

        @Test
        @DisplayName("should return campaign IDs")
        void listCampaigns_shouldReturnIds() {
            service.createCampaign("c1", "T1", "", "S", "F0", "F1");
            service.createCampaign("c2", "T2", "", "S", "F0", "F1");

            assertEquals(2, service.listCampaigns().size());
        }
    }

    // ---- deleteCampaign ----

    @Nested
    @DisplayName("deleteCampaign Tests")
    class DeleteCampaignTests {

        @Test
        @DisplayName("should delete a campaign")
        void deleteCampaign_shouldDelete() {
            service.createCampaign("camp-9", "Test", "", "S", "F0", "F1");
            assertTrue(service.deleteCampaign("camp-9"));
            assertFalse(service.listCampaigns().contains("camp-9"));
        }

        @Test
        @DisplayName("should return false for unknown campaign")
        void deleteCampaign_shouldReturnFalse() {
            assertFalse(service.deleteCampaign("nonexistent"));
        }
    }
}

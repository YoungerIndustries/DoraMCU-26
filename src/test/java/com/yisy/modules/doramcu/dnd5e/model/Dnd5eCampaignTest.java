package com.yisy.modules.doramcu.dnd5e.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dnd5eCampaign Entity Tests")
class Dnd5eCampaignTest {

    @Nested
    @DisplayName("Basic Properties")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get name")
        void name_shouldWork() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.setName("Dragonlance");
            assertEquals("Dragonlance", campaign.getName());
        }

        @Test
        @DisplayName("should set and get description")
        void description_shouldWork() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.setDescription("A campaign in Krynn");
            assertEquals("A campaign in Krynn", campaign.getDescription());
        }

        @Test
        @DisplayName("should set and get DM name")
        void dmName_shouldWork() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.setDmName("Dungeon Master");
            assertEquals("Dungeon Master", campaign.getDmName());
        }

        @Test
        @DisplayName("should set and get setting name")
        void settingName_shouldWork() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.setSettingName("Forgotten Realms");
            assertEquals("Forgotten Realms", campaign.getSettingName());
        }

        @Test
        @DisplayName("should default starting level to 1")
        void startingLevel_shouldDefaultToOne() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            assertEquals(1, campaign.getStartingLevel());
        }

        @Test
        @DisplayName("should set and get starting level")
        void startingLevel_shouldSet() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.setStartingLevel(5);
            assertEquals(5, campaign.getStartingLevel());
        }

        @Test
        @DisplayName("should default active to true")
        void active_shouldDefaultToTrue() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            assertTrue(campaign.isActive());
        }

        @Test
        @DisplayName("should set and get active status")
        void active_shouldSet() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.setActive(false);
            assertFalse(campaign.isActive());
        }

        @Test
        @DisplayName("should set and get id")
        void id_shouldWork() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.setId(42L);
            assertEquals(42L, campaign.getId());
        }
    }

    @Nested
    @DisplayName("Relationships")
    class RelationshipTests {

        @Test
        @DisplayName("should initialize empty campaign characters list")
        void campaignCharacters_shouldInitializeEmpty() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            assertNotNull(campaign.getCampaignCharacters());
            assertTrue(campaign.getCampaignCharacters().isEmpty());
        }

        @Test
        @DisplayName("should set and get campaign characters")
        void campaignCharacters_shouldSet() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            List<Dnd5eCampaignCharacter> chars = new ArrayList<>();
            campaign.setCampaignCharacters(chars);
            assertSame(chars, campaign.getCampaignCharacters());
        }

        @Test
        @DisplayName("should initialize empty sessions list")
        void sessions_shouldInitializeEmpty() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            assertNotNull(campaign.getSessions());
            assertTrue(campaign.getSessions().isEmpty());
        }

        @Test
        @DisplayName("should set and get sessions")
        void sessions_shouldSet() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            List<Dnd5eSession> sessions = new ArrayList<>();
            campaign.setSessions(sessions);
            assertSame(sessions, campaign.getSessions());
        }
    }

    @Nested
    @DisplayName("Lifecycle Callbacks")
    class LifecycleTests {

        @Test
        @DisplayName("should set createdAt and updatedAt on persist")
        void onCreate_shouldSetTimestamps() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.onCreate();
            assertNotNull(campaign.getCreatedAt());
            assertNotNull(campaign.getUpdatedAt());
        }

        @Test
        @DisplayName("should update updatedAt on update")
        void onUpdate_shouldSetUpdatedAt() {
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.onCreate();
            var created = campaign.getUpdatedAt();
            try {
                Thread.sleep(10);
            } catch (InterruptedException ignored) {}
            campaign.onUpdate();
            assertNotEquals(created, campaign.getUpdatedAt());
        }
    }
}

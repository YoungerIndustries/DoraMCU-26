package com.yisy.modules.doramcu.dnd5e.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dnd5eCampaignCharacter Entity Tests")
class Dnd5eCampaignCharacterTest {

    @Nested
    @DisplayName("Basic Properties")
    class BasicPropertiesTests {

        @Test
        @DisplayName("should set and get id")
        void id_shouldWork() {
            Dnd5eCampaignCharacter cc = new Dnd5eCampaignCharacter();
            cc.setId(42L);
            assertEquals(42L, cc.getId());
        }

        @Test
        @DisplayName("should set and get role")
        void role_shouldWork() {
            Dnd5eCampaignCharacter cc = new Dnd5eCampaignCharacter();
            cc.setRole("Ring Bearer");
            assertEquals("Ring Bearer", cc.getRole());
        }

        @Test
        @DisplayName("should allow null role")
        void role_shouldAllowNull() {
            Dnd5eCampaignCharacter cc = new Dnd5eCampaignCharacter();
            cc.setRole(null);
            assertNull(cc.getRole());
        }
    }

    @Nested
    @DisplayName("Relationships")
    class RelationshipTests {

        @Test
        @DisplayName("should set and get campaign")
        void campaign_shouldWork() {
            Dnd5eCampaignCharacter cc = new Dnd5eCampaignCharacter();
            Dnd5eCampaign campaign = new Dnd5eCampaign();
            campaign.setId(1L);
            cc.setCampaign(campaign);
            assertSame(campaign, cc.getCampaign());
        }

        @Test
        @DisplayName("should set and get character")
        void character_shouldWork() {
            Dnd5eCampaignCharacter cc = new Dnd5eCampaignCharacter();
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setId(10L);
            cc.setCharacter(character);
            assertSame(character, cc.getCharacter());
        }
    }

    @Nested
    @DisplayName("Lifecycle Callbacks")
    class LifecycleTests {

        @Test
        @DisplayName("should set joinedAt on persist")
        void onCreate_shouldSetJoinedAt() {
            Dnd5eCampaignCharacter cc = new Dnd5eCampaignCharacter();
            cc.onCreate();
            assertNotNull(cc.getJoinedAt());
        }
    }
}

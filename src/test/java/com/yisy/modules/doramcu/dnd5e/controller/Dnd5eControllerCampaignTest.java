package com.yisy.modules.doramcu.dnd5e.controller;

import com.yisy.modules.doramcu.dnd5e.dto.AddCampaignCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCampaignRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eSessionRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaign;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSession;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCampaignService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCharacterService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCombatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eDiceService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eGameplayService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eCraftingService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eExhaustionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eFeatService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eHeroicInspirationService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eToolProficiencyService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5ePsionService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eInventoryService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eRulesService;
import com.yisy.modules.doramcu.dnd5e.service.Dnd5eSpellService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Dnd5eController Campaign Tests")
class Dnd5eControllerCampaignTest {

    private MockMvc mockMvc;

    private Dnd5eCampaignService campaignService;
    private Dnd5eCharacterService characterService;
    private Dnd5eCombatService combatService;
    private Dnd5eRulesService rulesService;
    private Dnd5eSpellService spellService;
    private Dnd5eDiceService diceService;
    private Dnd5eGameplayService gameplayService;
    private Dnd5eInventoryService inventoryService;
    private Dnd5eCraftingService craftingService;
    private Dnd5eHeroicInspirationService heroicInspirationService;
    private Dnd5eExhaustionService exhaustionService;
    private Dnd5eFeatService featService;
    private Dnd5eToolProficiencyService toolProficiencyService;
    private Dnd5ePsionService psionService;

    @BeforeEach
    void setUp() {
        campaignService = mock(Dnd5eCampaignService.class);
        characterService = mock(Dnd5eCharacterService.class);
        combatService = mock(Dnd5eCombatService.class);
        rulesService = mock(Dnd5eRulesService.class);
        spellService = mock(Dnd5eSpellService.class);
        diceService = mock(Dnd5eDiceService.class);
        gameplayService = mock(Dnd5eGameplayService.class);
        inventoryService = mock(Dnd5eInventoryService.class);
        craftingService = mock(Dnd5eCraftingService.class);
        heroicInspirationService = mock(Dnd5eHeroicInspirationService.class);
        exhaustionService = mock(Dnd5eExhaustionService.class);
        featService = mock(Dnd5eFeatService.class);
        toolProficiencyService = mock(Dnd5eToolProficiencyService.class);
        psionService = mock(Dnd5ePsionService.class);

        Dnd5eController controller = new Dnd5eController(
                characterService, combatService, spellService, rulesService,
                diceService, gameplayService, campaignService, inventoryService, craftingService,
                heroicInspirationService, exhaustionService, featService, toolProficiencyService,
                psionService
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    // ---- Campaign CRUD ----

    @Nested
    @DisplayName("POST /campaigns Tests")
    class CreateCampaignTests {

        @Test
        @DisplayName("should create a campaign successfully")
        void createCampaign_success() throws Exception {
            // Arrange
            CreateDnd5eCampaignRequest request = new CreateDnd5eCampaignRequest();
            request.setName("Dragonlance");
            request.setDescription("Krynn campaign");
            request.setDmName("DM");
            request.setSettingName("Dragonlance");
            request.setStartingLevel(1);

            Dnd5eCampaign campaign = createCampaign(1L, "Dragonlance");
            when(campaignService.createCampaign(any())).thenReturn(campaign);

            String json = """
                    {
                        "name": "Dragonlance",
                        "description": "Krynn campaign",
                        "dmName": "DM",
                        "settingName": "Dragonlance",
                        "startingLevel": 1
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/v1/dnd5e/campaigns")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Dragonlance"));

            verify(campaignService).createCampaign(any());
        }
    }

    @Nested
    @DisplayName("GET /campaigns Tests")
    class ListCampaignsTests {

        @Test
        @DisplayName("should return all campaigns")
        void listCampaigns_success() throws Exception {
            // Arrange
            when(campaignService.listCampaigns()).thenReturn(List.of(
                    createCampaign(1L, "Campaign 1"),
                    createCampaign(2L, "Campaign 2")
            ));

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/campaigns"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Campaign 1"))
                    .andExpect(jsonPath("$[1].name").value("Campaign 2"));
        }

        @Test
        @DisplayName("should return empty list when no campaigns")
        void listCampaigns_empty() throws Exception {
            // Arrange
            when(campaignService.listCampaigns()).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/campaigns"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /campaigns/active Tests")
    class ListActiveCampaignsTests {

        @Test
        @DisplayName("should return only active campaigns")
        void listActiveCampaigns_success() throws Exception {
            // Arrange
            Dnd5eCampaign active = createCampaign(1L, "Active Campaign");
            active.setActive(true);
            when(campaignService.listActiveCampaigns()).thenReturn(List.of(active));

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/campaigns/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].active").value(true));
        }
    }

    @Nested
    @DisplayName("GET /campaigns/{id} Tests")
    class GetCampaignTests {

        @Test
        @DisplayName("should return campaign by id")
        void getCampaign_success() throws Exception {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignService.getCampaign(1L)).thenReturn(campaign);

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/campaigns/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Test Campaign"));
        }

    }

    @Nested
    @DisplayName("PATCH /campaigns/{id} Tests")
    class UpdateCampaignTests {

        @Test
        @DisplayName("should update campaign")
        void updateCampaign_success() throws Exception {
            // Arrange
            Dnd5eCampaign updated = createCampaign(1L, "Updated Name");
            updated.setDescription("Updated description");
            when(campaignService.updateCampaign(eq(1L), any())).thenReturn(updated);

            String json = """
                    {
                        "name": "Updated Name",
                        "description": "Updated description"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/campaigns/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Name"));
        }
    }

    @Nested
    @DisplayName("DELETE /campaigns/{id} Tests")
    class DeleteCampaignTests {

        @Test
        @DisplayName("should delete campaign")
        void deleteCampaign_success() throws Exception {
            // Arrange
            doNothing().when(campaignService).deleteCampaign(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/dnd5e/campaigns/1"))
                    .andExpect(status().isOk());

            verify(campaignService).deleteCampaign(1L);
        }

    }

    @Nested
    @DisplayName("PATCH /campaigns/{id}/active Tests")
    class SetActiveTests {

        @Test
        @DisplayName("should set campaign active")
        void setActive_success() throws Exception {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            campaign.setActive(true);
            doNothing().when(campaignService).setActive(1L, true);
            when(campaignService.getCampaign(1L)).thenReturn(campaign);

            String json = "{\"active\": true}";

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/campaigns/1/active")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.active").value(true));
        }
    }

    // ---- Campaign Characters ----

    @Nested
    @DisplayName("POST /campaigns/{id}/characters Tests")
    class AddCharacterTests {

        @Test
        @DisplayName("should add character to campaign")
        void addCharacter_success() throws Exception {
            // Arrange
            Dnd5eCampaignCharacter cc = new Dnd5eCampaignCharacter();
            cc.setId(1L);
            cc.setRole("Player Character");

            when(campaignService.addCharacterToCampaign(eq(1L), any())).thenReturn(cc);

            String json = """
                    {
                        "characterId": 10,
                        "role": "Player Character"
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/v1/dnd5e/campaigns/1/characters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("Player Character"));
        }
    }

    @Nested
    @DisplayName("DELETE /campaigns/{campaignId}/characters/{characterId} Tests")
    class RemoveCharacterTests {

        @Test
        @DisplayName("should remove character from campaign")
        void removeCharacter_success() throws Exception {
            // Arrange
            doNothing().when(campaignService).removeCharacterFromCampaign(1L, 10L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/dnd5e/campaigns/1/characters/10"))
                    .andExpect(status().isOk());

            verify(campaignService).removeCharacterFromCampaign(1L, 10L);
        }
    }

    // ---- Sessions ----

    @Nested
    @DisplayName("POST /campaigns/{id}/sessions Tests")
    class CreateSessionTests {

        @Test
        @DisplayName("should create a session")
        void createSession_success() throws Exception {
            // Arrange
            Dnd5eSession session = new Dnd5eSession();
            session.setId(1L);
            session.setSessionName("The Beginning");
            session.setSessionNumber(1);
            session.setSessionDate(LocalDate.of(2026, 11, 10));
            session.setXpAwarded(500);
            session.setDurationMinutes(180);

            when(campaignService.createSession(eq(1L), any())).thenReturn(session);

            String json = """
                    {
                        "sessionName": "The Beginning",
                        "sessionNumber": 1,
                        "sessionDate": "2026-11-10",
                        "notes": "First session",
                        "xpAwarded": 500,
                        "durationMinutes": 180
                    }
                    """;

            // Act & Assert
            mockMvc.perform(post("/api/v1/dnd5e/campaigns/1/sessions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionName").value("The Beginning"))
                    .andExpect(jsonPath("$.sessionNumber").value(1));
        }
    }

    @Nested
    @DisplayName("GET /campaigns/{id}/sessions Tests")
    class ListSessionsTests {

        @Test
        @DisplayName("should return sessions for a campaign")
        void listSessions_success() throws Exception {
            // Arrange
            Dnd5eSession s1 = new Dnd5eSession();
            s1.setId(1L);
            s1.setSessionNumber(1);
            s1.setSessionName("Session 1");
            Dnd5eSession s2 = new Dnd5eSession();
            s2.setId(2L);
            s2.setSessionNumber(2);
            s2.setSessionName("Session 2");

            when(campaignService.listSessions(1L)).thenReturn(List.of(s2, s1));

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/campaigns/1/sessions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].sessionNumber").value(2))
                    .andExpect(jsonPath("$[1].sessionNumber").value(1));
        }
    }

    @Nested
    @DisplayName("GET /campaigns/{campaignId}/sessions/{sessionId} Tests")
    class GetSessionTests {

        @Test
        @DisplayName("should return session by id")
        void getSession_success() throws Exception {
            // Arrange
            Dnd5eSession session = new Dnd5eSession();
            session.setId(10L);
            session.setSessionName("The Beginning");
            when(campaignService.getSession(1L, 10L)).thenReturn(session);

            // Act & Assert
            mockMvc.perform(get("/api/v1/dnd5e/campaigns/1/sessions/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionName").value("The Beginning"));
        }
    }

    @Nested
    @DisplayName("PATCH /campaigns/{campaignId}/sessions/{sessionId} Tests")
    class UpdateSessionTests {

        @Test
        @DisplayName("should update session")
        void updateSession_success() throws Exception {
            // Arrange
            Dnd5eSession session = new Dnd5eSession();
            session.setId(10L);
            session.setSessionName("Updated Session");
            session.setSessionNumber(2);
            when(campaignService.updateSession(eq(1L), eq(10L), any())).thenReturn(session);

            String json = """
                    {
                        "sessionName": "Updated Session",
                        "sessionNumber": 2,
                        "notes": "Updated notes",
                        "xpAwarded": 1000
                    }
                    """;

            // Act & Assert
            mockMvc.perform(patch("/api/v1/dnd5e/campaigns/1/sessions/10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.sessionName").value("Updated Session"));
        }
    }

    @Nested
    @DisplayName("DELETE /campaigns/{campaignId}/sessions/{sessionId} Tests")
    class DeleteSessionTests {

        @Test
        @DisplayName("should delete session")
        void deleteSession_success() throws Exception {
            // Arrange
            doNothing().when(campaignService).deleteSession(1L, 10L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/dnd5e/campaigns/1/sessions/10"))
                    .andExpect(status().isOk());

            verify(campaignService).deleteSession(1L, 10L);
        }
    }

    // ---- Helper methods ----

    private Dnd5eCampaign createCampaign(Long id, String name) {
        Dnd5eCampaign campaign = new Dnd5eCampaign();
        campaign.setId(id);
        campaign.setName(name);
        campaign.setDmName("Test DM");
        campaign.setActive(true);
        campaign.setStartingLevel(1);
        return campaign;
    }
}

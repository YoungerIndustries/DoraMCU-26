package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaign;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCampaignCharacter;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrSession;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCampaignCharacterRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCampaignRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("LotrCampaignService Tests")
class LotrCampaignServiceTest {

    private LotrCampaignRepository campaignRepository;
    private LotrCampaignCharacterRepository ccRepository;
    private LotrSessionRepository sessionRepository;
    private LotrCampaignService campaignService;

    @BeforeEach
    void setUp() {
        campaignRepository = mock(LotrCampaignRepository.class);
        ccRepository = mock(LotrCampaignCharacterRepository.class);
        sessionRepository = mock(LotrSessionRepository.class);
        campaignService = new LotrCampaignService(campaignRepository, ccRepository, sessionRepository);
    }

    // ---- createCampaign ----

    @Nested
    @DisplayName("createCampaign Tests")
    class CreateCampaignTests {

        @Test
        @DisplayName("should create a campaign with default hope/shadow")
        void createCampaign_shouldSetDefaults() {
            // Arrange
            when(campaignRepository.save(any(LotrCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrCampaign campaign = campaignService.createCampaign(
                    "Fellowship of the Ring", "Destroy the One Ring", "Bilbo", "Quest");

            // Assert
            assertNotNull(campaign);
            assertEquals("Fellowship of the Ring", campaign.getName());
            assertEquals("Destroy the One Ring", campaign.getDescription());
            assertEquals("Bilbo", campaign.getNarratorName());
            assertEquals("Quest", campaign.getQuestType());
            assertEquals(5, campaign.getHopeLevel());
            assertEquals(0, campaign.getShadowLevel());
            assertTrue(campaign.isActive());
            verify(campaignRepository).save(campaign);
        }
    }

    // ---- findById ----

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should return campaign by id")
        void findById_shouldReturnCampaign() {
            // Arrange
            LotrCampaign campaign = createCampaign(1L, "Fellowship");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

            // Act
            LotrCampaign result = campaignService.findById(1L);

            // Assert
            assertEquals("Fellowship", result.getName());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void findById_shouldThrowWhenNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> campaignService.findById(999L));
            assertTrue(ex.getMessage().contains("Campaign not found"));
        }
    }

    // ---- findAll ----

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("should return all campaigns")
        void findAll_shouldReturnAll() {
            // Arrange
            List<LotrCampaign> campaigns = List.of(
                    createCampaign(1L, "Fellowship"),
                    createCampaign(2L, "Two Towers")
            );
            when(campaignRepository.findAll()).thenReturn(campaigns);

            // Act
            List<LotrCampaign> result = campaignService.findAll();

            // Assert
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("should return empty list when no campaigns exist")
        void findAll_shouldReturnEmpty() {
            // Arrange
            when(campaignRepository.findAll()).thenReturn(List.of());

            // Act
            List<LotrCampaign> result = campaignService.findAll();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- findActive ----

    @Nested
    @DisplayName("findActive Tests")
    class FindActiveTests {

        @Test
        @DisplayName("should return active campaigns")
        void findActive_shouldReturnActive() {
            // Arrange
            LotrCampaign campaign = createCampaign(1L, "Active Campaign");
            campaign.setActive(true);
            when(campaignRepository.findByActiveTrue()).thenReturn(List.of(campaign));

            // Act
            List<LotrCampaign> result = campaignService.findActive();

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isActive());
        }
    }

    // ---- delete ----

    @Nested
    @DisplayName("delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("should delete campaign by id")
        void delete_shouldDelete() {
            // Act
            campaignService.delete(1L);

            // Assert
            verify(campaignRepository).deleteById(1L);
        }
    }

    // ---- createSession ----

    @Nested
    @DisplayName("createSession Tests")
    class CreateSessionTests {

        @Test
        @DisplayName("should create a session for a campaign")
        void createSession_shouldCreateSession() {
            // Arrange
            LotrCampaign campaign = createCampaign(1L, "Fellowship");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(sessionRepository.save(any(LotrSession.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrSession session = campaignService.createSession(
                    1L, "Session 1", 1, LocalDate.of(2024, 1, 1), "First session");

            // Assert
            assertNotNull(session);
            assertEquals("Session 1", session.getSessionName());
            assertEquals(1, session.getSessionNumber());
            assertEquals(LocalDate.of(2024, 1, 1), session.getSessionDate());
            assertEquals("First session", session.getNotes());
            assertEquals(0, session.getXpAwarded());
            assertEquals(0, session.getCorruptionChange());
            assertEquals(0, session.getHopeChange());
            assertEquals(0, session.getShadowChange());
            verify(sessionRepository).save(session);
        }

        @Test
        @DisplayName("should use current date when sessionDate is null")
        void createSession_shouldUseCurrentDate() {
            // Arrange
            LotrCampaign campaign = createCampaign(1L, "Fellowship");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(sessionRepository.save(any(LotrSession.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            LotrSession session = campaignService.createSession(
                    1L, "Session 1", 1, null, null);

            // Assert
            assertEquals(LocalDate.now(), session.getSessionDate());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void createSession_shouldThrowWhenCampaignNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.createSession(999L, "Session", 1, null, null));
        }
    }

    // ---- listSessions ----

    @Nested
    @DisplayName("listSessions Tests")
    class ListSessionsTests {

        @Test
        @DisplayName("should return sessions for a campaign")
        void listSessions_shouldReturnSessions() {
            // Arrange
            LotrSession session1 = new LotrSession();
            session1.setId(1L);
            session1.setSessionName("Session 1");
            LotrSession session2 = new LotrSession();
            session2.setId(2L);
            session2.setSessionName("Session 2");
            when(sessionRepository.findByCampaignId(1L)).thenReturn(List.of(session1, session2));

            // Act
            List<LotrSession> result = campaignService.listSessions(1L);

            // Assert
            assertEquals(2, result.size());
        }
    }

    // ---- findSessionById ----

    @Nested
    @DisplayName("findSessionById Tests")
    class FindSessionByIdTests {

        @Test
        @DisplayName("should return session by id")
        void findSessionById_shouldReturnSession() {
            // Arrange
            LotrSession session = new LotrSession();
            session.setId(1L);
            session.setSessionName("Session 1");
            when(sessionRepository.findById(1L)).thenReturn(Optional.of(session));

            // Act
            LotrSession result = campaignService.findSessionById(1L);

            // Assert
            assertEquals("Session 1", result.getSessionName());
        }

        @Test
        @DisplayName("should throw when session not found")
        void findSessionById_shouldThrowWhenNotFound() {
            // Arrange
            when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> campaignService.findSessionById(999L));
            assertTrue(ex.getMessage().contains("Session not found"));
        }
    }

    // ---- Helper methods ----

    private LotrCampaign createCampaign(Long id, String name) {
        LotrCampaign campaign = new LotrCampaign();
        campaign.setId(id);
        campaign.setName(name);
        campaign.setDescription("Test campaign");
        campaign.setNarratorName("Bilbo");
        campaign.setQuestType("Quest");
        campaign.setHopeLevel(5);
        campaign.setShadowLevel(0);
        campaign.setActive(true);
        return campaign;
    }
}

package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.dto.AddCampaignCharacterRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eCampaignRequest;
import com.yisy.modules.doramcu.dnd5e.dto.CreateDnd5eSessionRequest;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaign;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSession;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCampaignRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCharacterRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Dnd5eCampaignService Tests")
class Dnd5eCampaignServiceTest {

    private Dnd5eCampaignRepository campaignRepository;
    private Dnd5eSessionRepository sessionRepository;
    private Dnd5eCharacterRepository characterRepository;
    private Dnd5eCampaignService campaignService;

    @BeforeEach
    void setUp() {
        campaignRepository = mock(Dnd5eCampaignRepository.class);
        sessionRepository = mock(Dnd5eSessionRepository.class);
        characterRepository = mock(Dnd5eCharacterRepository.class);
        campaignService = new Dnd5eCampaignService(campaignRepository, sessionRepository, characterRepository);
    }

    // ---- createCampaign ----

    @Nested
    @DisplayName("createCampaign Tests")
    class CreateCampaignTests {

        @Test
        @DisplayName("should create a campaign with all fields")
        void createCampaign_shouldCreateWithAllFields() {
            // Arrange
            CreateDnd5eCampaignRequest request = new CreateDnd5eCampaignRequest();
            request.setName("Dragonlance Chronicles");
            request.setDescription("A campaign set in Krynn");
            request.setDmName("Dungeon Master");
            request.setSettingName("Dragonlance");
            request.setStartingLevel(3);

            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCampaign campaign = campaignService.createCampaign(request);

            // Assert
            assertNotNull(campaign);
            assertEquals("Dragonlance Chronicles", campaign.getName());
            assertEquals("A campaign set in Krynn", campaign.getDescription());
            assertEquals("Dungeon Master", campaign.getDmName());
            assertEquals("Dragonlance", campaign.getSettingName());
            assertEquals(3, campaign.getStartingLevel());
            assertTrue(campaign.isActive());
            verify(campaignRepository).save(campaign);
        }

        @Test
        @DisplayName("should create a campaign with default starting level 1")
        void createCampaign_shouldDefaultStartingLevel() {
            // Arrange
            CreateDnd5eCampaignRequest request = new CreateDnd5eCampaignRequest();
            request.setName("New Campaign");
            request.setDmName("DM");
            request.setStartingLevel(1);

            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCampaign campaign = campaignService.createCampaign(request);

            // Assert
            assertEquals(1, campaign.getStartingLevel());
        }

        @Test
        @DisplayName("should set campaign as active by default")
        void createCampaign_shouldSetActiveByDefault() {
            // Arrange
            CreateDnd5eCampaignRequest request = new CreateDnd5eCampaignRequest();
            request.setName("Active Campaign");
            request.setDmName("DM");

            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Dnd5eCampaign campaign = campaignService.createCampaign(request);

            // Assert
            assertTrue(campaign.isActive());
        }
    }

    // ---- listCampaigns ----

    @Nested
    @DisplayName("listCampaigns Tests")
    class ListCampaignsTests {

        @Test
        @DisplayName("should return all campaigns")
        void listCampaigns_shouldReturnAll() {
            // Arrange
            List<Dnd5eCampaign> campaigns = List.of(createCampaign(1L, "Campaign 1"), createCampaign(2L, "Campaign 2"));
            when(campaignRepository.findAll()).thenReturn(campaigns);

            // Act
            List<Dnd5eCampaign> result = campaignService.listCampaigns();

            // Assert
            assertEquals(2, result.size());
            verify(campaignRepository).findAll();
        }

        @Test
        @DisplayName("should return empty list when no campaigns exist")
        void listCampaigns_shouldReturnEmpty() {
            // Arrange
            when(campaignRepository.findAll()).thenReturn(List.of());

            // Act
            List<Dnd5eCampaign> result = campaignService.listCampaigns();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- listActiveCampaigns ----

    @Nested
    @DisplayName("listActiveCampaigns Tests")
    class ListActiveCampaignsTests {

        @Test
        @DisplayName("should return only active campaigns")
        void listActiveCampaigns_shouldReturnActiveOnly() {
            // Arrange
            Dnd5eCampaign active = createCampaign(1L, "Active");
            active.setActive(true);
            when(campaignRepository.findByActive(true)).thenReturn(List.of(active));

            // Act
            List<Dnd5eCampaign> result = campaignService.listActiveCampaigns();

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isActive());
            verify(campaignRepository).findByActive(true);
        }
    }

    // ---- getCampaign ----

    @Nested
    @DisplayName("getCampaign Tests")
    class GetCampaignTests {

        @Test
        @DisplayName("should return campaign by id")
        void getCampaign_shouldReturnById() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

            // Act
            Dnd5eCampaign result = campaignService.getCampaign(1L);

            // Assert
            assertEquals("Test Campaign", result.getName());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void getCampaign_shouldThrowWhenNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> campaignService.getCampaign(999L));
            assertTrue(ex.getMessage().contains("Campaign not found"));
            assertTrue(ex.getMessage().contains("999"));
        }
    }

    // ---- updateCampaign ----

    @Nested
    @DisplayName("updateCampaign Tests")
    class UpdateCampaignTests {

        @Test
        @DisplayName("should update all campaign fields")
        void updateCampaign_shouldUpdateAllFields() {
            // Arrange
            Dnd5eCampaign existing = createCampaign(1L, "Old Name");
            existing.setDescription("Old description");
            existing.setDmName("Old DM");
            existing.setSettingName("Old Setting");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateDnd5eCampaignRequest request = new CreateDnd5eCampaignRequest();
            request.setName("New Name");
            request.setDescription("New description");
            request.setDmName("New DM");
            request.setSettingName("New Setting");
            request.setStartingLevel(5);

            // Act
            Dnd5eCampaign result = campaignService.updateCampaign(1L, request);

            // Assert
            assertEquals("New Name", result.getName());
            assertEquals("New description", result.getDescription());
            assertEquals("New DM", result.getDmName());
            assertEquals("New Setting", result.getSettingName());
            assertEquals(5, result.getStartingLevel());
            verify(campaignRepository).save(existing);
        }

        @Test
        @DisplayName("should update only non-null fields")
        void updateCampaign_shouldUpdateOnlyNonNullFields() {
            // Arrange
            Dnd5eCampaign existing = createCampaign(1L, "Original Name");
            existing.setDescription("Original description");
            existing.setDmName("Original DM");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateDnd5eCampaignRequest request = new CreateDnd5eCampaignRequest();
            request.setName("New Name");
            // Other fields are null

            // Act
            Dnd5eCampaign result = campaignService.updateCampaign(1L, request);

            // Assert
            assertEquals("New Name", result.getName());
            assertEquals("Original description", result.getDescription());
            assertEquals("Original DM", result.getDmName());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void updateCampaign_shouldThrowWhenNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());
            CreateDnd5eCampaignRequest request = new CreateDnd5eCampaignRequest();
            request.setName("New Name");

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.updateCampaign(999L, request));
        }
    }

    // ---- deleteCampaign ----

    @Nested
    @DisplayName("deleteCampaign Tests")
    class DeleteCampaignTests {

        @Test
        @DisplayName("should delete existing campaign")
        void deleteCampaign_shouldDeleteExisting() {
            // Arrange
            when(campaignRepository.existsById(1L)).thenReturn(true);

            // Act
            campaignService.deleteCampaign(1L);

            // Assert
            verify(campaignRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void deleteCampaign_shouldThrowWhenNotFound() {
            // Arrange
            when(campaignRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> campaignService.deleteCampaign(999L));
            assertTrue(ex.getMessage().contains("Campaign not found"));
            verify(campaignRepository, never()).deleteById(anyLong());
        }
    }

    // ---- setActive ----

    @Nested
    @DisplayName("setActive Tests")
    class SetActiveTests {

        @Test
        @DisplayName("should set campaign to active")
        void setActive_shouldActivate() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Inactive Campaign");
            campaign.setActive(false);
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            campaignService.setActive(1L, true);

            // Assert
            assertTrue(campaign.isActive());
            verify(campaignRepository).save(campaign);
        }

        @Test
        @DisplayName("should set campaign to inactive")
        void setActive_shouldDeactivate() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Active Campaign");
            campaign.setActive(true);
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            campaignService.setActive(1L, false);

            // Assert
            assertFalse(campaign.isActive());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void setActive_shouldThrowWhenNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.setActive(999L, true));
        }
    }

    // ---- addCharacterToCampaign ----

    @Nested
    @DisplayName("addCharacterToCampaign Tests")
    class AddCharacterToCampaignTests {

        @Test
        @DisplayName("should add character to campaign with specified role")
        void addCharacter_shouldAddWithRole() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            campaign.setCampaignCharacters(new ArrayList<>());
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setId(10L);
            character.setName("Frodo");

            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(characterRepository.findById(10L)).thenReturn(Optional.of(character));
            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            AddCampaignCharacterRequest request = new AddCampaignCharacterRequest();
            request.setCharacterId(10L);
            request.setRole("Ring Bearer");

            // Act
            Dnd5eCampaignCharacter result = campaignService.addCharacterToCampaign(1L, request);

            // Assert
            assertNotNull(result);
            assertEquals("Ring Bearer", result.getRole());
            assertEquals(1, campaign.getCampaignCharacters().size());
        }

        @Test
        @DisplayName("should default role to Player Character")
        void addCharacter_shouldDefaultRole() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            campaign.setCampaignCharacters(new ArrayList<>());
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setId(10L);
            character.setName("Aragorn");

            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(characterRepository.findById(10L)).thenReturn(Optional.of(character));
            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            AddCampaignCharacterRequest request = new AddCampaignCharacterRequest();
            request.setCharacterId(10L);
            // Role is null

            // Act
            Dnd5eCampaignCharacter result = campaignService.addCharacterToCampaign(1L, request);

            // Assert
            assertEquals("Player Character", result.getRole());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void addCharacter_shouldThrowWhenCampaignNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());
            AddCampaignCharacterRequest request = new AddCampaignCharacterRequest();
            request.setCharacterId(10L);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.addCharacterToCampaign(999L, request));
        }

        @Test
        @DisplayName("should throw when character not found")
        void addCharacter_shouldThrowWhenCharacterNotFound() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(characterRepository.findById(999L)).thenReturn(Optional.empty());

            AddCampaignCharacterRequest request = new AddCampaignCharacterRequest();
            request.setCharacterId(999L);

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> campaignService.addCharacterToCampaign(1L, request));
            assertTrue(ex.getMessage().contains("Character not found"));
        }
    }

    // ---- removeCharacterFromCampaign ----

    @Nested
    @DisplayName("removeCharacterFromCampaign Tests")
    class RemoveCharacterFromCampaignTests {

        @Test
        @DisplayName("should remove character from campaign")
        void removeCharacter_shouldRemove() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            Dnd5eCharacter character = new Dnd5eCharacter();
            character.setId(10L);
            character.setName("Frodo");

            Dnd5eCampaignCharacter cc = new Dnd5eCampaignCharacter();
            cc.setCampaign(campaign);
            cc.setCharacter(character);

            List<Dnd5eCampaignCharacter> chars = new ArrayList<>();
            chars.add(cc);
            campaign.setCampaignCharacters(chars);

            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            campaignService.removeCharacterFromCampaign(1L, 10L);

            // Assert
            assertTrue(campaign.getCampaignCharacters().isEmpty());
            verify(campaignRepository).save(campaign);
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void removeCharacter_shouldThrowWhenCampaignNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.removeCharacterFromCampaign(999L, 10L));
        }
    }

    // ---- createSession ----

    @Nested
    @DisplayName("createSession Tests")
    class CreateSessionTests {

        @Test
        @DisplayName("should create a session with all fields")
        void createSession_shouldCreateWithAllFields() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            campaign.setSessions(new ArrayList<>());
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateDnd5eSessionRequest request = new CreateDnd5eSessionRequest();
            request.setSessionName("The Beginning");
            request.setSessionNumber(1);
            request.setSessionDate(LocalDate.of(2026, 11, 10));
            request.setNotes("First session notes");
            request.setXpAwarded(500);
            request.setDurationMinutes(180);

            // Act
            Dnd5eSession session = campaignService.createSession(1L, request);

            // Assert
            assertNotNull(session);
            assertEquals("The Beginning", session.getSessionName());
            assertEquals(1, session.getSessionNumber());
            assertEquals(LocalDate.of(2026, 11, 10), session.getSessionDate());
            assertEquals("First session notes", session.getNotes());
            assertEquals(500, session.getXpAwarded());
            assertEquals(180, session.getDurationMinutes());
            assertEquals(1, campaign.getSessions().size());
        }

        @Test
        @DisplayName("should default session date to today")
        void createSession_shouldDefaultDate() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            campaign.setSessions(new ArrayList<>());
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(campaignRepository.save(any(Dnd5eCampaign.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateDnd5eSessionRequest request = new CreateDnd5eSessionRequest();
            request.setSessionName("Session 1");
            request.setSessionNumber(1);
            // Date is null

            // Act
            Dnd5eSession session = campaignService.createSession(1L, request);

            // Assert
            assertEquals(LocalDate.now(), session.getSessionDate());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void createSession_shouldThrowWhenCampaignNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());
            CreateDnd5eSessionRequest request = new CreateDnd5eSessionRequest();
            request.setSessionName("Session");
            request.setSessionNumber(1);

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.createSession(999L, request));
        }
    }

    // ---- listSessions ----

    @Nested
    @DisplayName("listSessions Tests")
    class ListSessionsTests {

        @Test
        @DisplayName("should return sessions ordered by session number desc")
        void listSessions_shouldReturnOrdered() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

            Dnd5eSession s1 = new Dnd5eSession();
            s1.setId(1L);
            s1.setSessionNumber(1);
            Dnd5eSession s2 = new Dnd5eSession();
            s2.setId(2L);
            s2.setSessionNumber(2);

            when(sessionRepository.findByCampaignIdOrderBySessionNumberDesc(1L))
                    .thenReturn(List.of(s2, s1));

            // Act
            List<Dnd5eSession> result = campaignService.listSessions(1L);

            // Assert
            assertEquals(2, result.size());
            assertEquals(2, result.get(0).getSessionNumber());
            assertEquals(1, result.get(1).getSessionNumber());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void listSessions_shouldThrowWhenCampaignNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.listSessions(999L));
        }
    }

    // ---- getSession ----

    @Nested
    @DisplayName("getSession Tests")
    class GetSessionTests {

        @Test
        @DisplayName("should return session by id")
        void getSession_shouldReturnById() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

            Dnd5eSession session = new Dnd5eSession();
            session.setId(10L);
            session.setSessionName("The Beginning");
            when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

            // Act
            Dnd5eSession result = campaignService.getSession(1L, 10L);

            // Assert
            assertEquals("The Beginning", result.getSessionName());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void getSession_shouldThrowWhenCampaignNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.getSession(999L, 10L));
        }

        @Test
        @DisplayName("should throw when session not found")
        void getSession_shouldThrowWhenSessionNotFound() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> campaignService.getSession(1L, 999L));
            assertTrue(ex.getMessage().contains("Session not found"));
        }
    }

    // ---- updateSession ----

    @Nested
    @DisplayName("updateSession Tests")
    class UpdateSessionTests {

        @Test
        @DisplayName("should update all session fields")
        void updateSession_shouldUpdateAllFields() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

            Dnd5eSession session = new Dnd5eSession();
            session.setId(10L);
            session.setSessionName("Old Name");
            session.setSessionNumber(1);
            session.setSessionDate(LocalDate.of(2026, 1, 1));
            session.setNotes("Old notes");
            when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
            when(sessionRepository.save(any(Dnd5eSession.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateDnd5eSessionRequest request = new CreateDnd5eSessionRequest();
            request.setSessionName("New Name");
            request.setSessionNumber(2);
            request.setSessionDate(LocalDate.of(2026, 11, 11));
            request.setNotes("New notes");
            request.setXpAwarded(1000);
            request.setDurationMinutes(240);

            // Act
            Dnd5eSession result = campaignService.updateSession(1L, 10L, request);

            // Assert
            assertEquals("New Name", result.getSessionName());
            assertEquals(2, result.getSessionNumber());
            assertEquals(LocalDate.of(2026, 11, 11), result.getSessionDate());
            assertEquals("New notes", result.getNotes());
            assertEquals(1000, result.getXpAwarded());
            assertEquals(240, result.getDurationMinutes());
        }

        @Test
        @DisplayName("should update only non-null fields")
        void updateSession_shouldUpdateOnlyNonNullFields() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

            Dnd5eSession session = new Dnd5eSession();
            session.setId(10L);
            session.setSessionName("Original");
            session.setSessionNumber(1);
            session.setSessionDate(LocalDate.of(2026, 1, 1));
            session.setNotes("Original notes");
            when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));
            when(sessionRepository.save(any(Dnd5eSession.class))).thenAnswer(inv -> inv.getArgument(0));

            CreateDnd5eSessionRequest request = new CreateDnd5eSessionRequest();
            request.setSessionName("New Name");
            // Other fields are null/defaults

            // Act
            Dnd5eSession result = campaignService.updateSession(1L, 10L, request);

            // Assert
            assertEquals("New Name", result.getSessionName());
            assertEquals("Original notes", result.getNotes());
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void updateSession_shouldThrowWhenCampaignNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());
            CreateDnd5eSessionRequest request = new CreateDnd5eSessionRequest();
            request.setSessionName("Updated");

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.updateSession(999L, 10L, request));
        }
    }

    // ---- deleteSession ----

    @Nested
    @DisplayName("deleteSession Tests")
    class DeleteSessionTests {

        @Test
        @DisplayName("should delete existing session")
        void deleteSession_shouldDelete() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));

            Dnd5eSession session = new Dnd5eSession();
            session.setId(10L);
            session.setSessionName("To Delete");
            when(sessionRepository.findById(10L)).thenReturn(Optional.of(session));

            // Act
            campaignService.deleteSession(1L, 10L);

            // Assert
            verify(sessionRepository).delete(session);
        }

        @Test
        @DisplayName("should throw when campaign not found")
        void deleteSession_shouldThrowWhenCampaignNotFound() {
            // Arrange
            when(campaignRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.deleteSession(999L, 10L));
        }

        @Test
        @DisplayName("should throw when session not found")
        void deleteSession_shouldThrowWhenSessionNotFound() {
            // Arrange
            Dnd5eCampaign campaign = createCampaign(1L, "Test Campaign");
            when(campaignRepository.findById(1L)).thenReturn(Optional.of(campaign));
            when(sessionRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(IllegalArgumentException.class,
                    () -> campaignService.deleteSession(1L, 999L));
        }
    }

    // ---- Helper methods ----

    private Dnd5eCampaign createCampaign(Long id, String name) {
        Dnd5eCampaign campaign = new Dnd5eCampaign();
        campaign.setId(id);
        campaign.setName(name);
        campaign.setDmName("Test DM");
        campaign.setActive(true);
        campaign.setCampaignCharacters(new ArrayList<>());
        campaign.setSessions(new ArrayList<>());
        return campaign;
    }
}

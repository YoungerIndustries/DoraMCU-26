package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Chat;
import com.yisy.modules.doramcu.earth2026.model.Earth2026OnlineStatus;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Party;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026OnlineStatusRepository;
import com.yisy.modules.doramcu.earth2026.service.Earth2026ChatService;
import com.yisy.modules.doramcu.earth2026.service.Earth2026PartyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Earth2026PartyController Tests")
class Earth2026PartyControllerTest {

    private Earth2026PartyService partyService;
    private Earth2026ChatService chatService;
    private Earth2026OnlineStatusRepository onlineStatusRepository;
    private Earth2026PartyController controller;

    @BeforeEach
    void setUp() {
        partyService = mock(Earth2026PartyService.class);
        chatService = mock(Earth2026ChatService.class);
        onlineStatusRepository = mock(Earth2026OnlineStatusRepository.class);
        controller = new Earth2026PartyController(partyService, chatService, onlineStatusRepository);
    }

    // ---- getAllParties ----

    @Nested
    @DisplayName("getAllParties Tests")
    class GetAllPartiesTests {

        @Test
        @DisplayName("should return active parties")
        void getAllParties_shouldReturnActiveParties() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Active", 1L, 1L);
            party.setId(1L);
            when(partyService.getActiveParties()).thenReturn(List.of(party));

            // Act
            List<Earth2026Party> result = controller.getAllParties();

            // Assert
            assertEquals(1, result.size());
            assertEquals("Active", result.get(0).getName());
        }

        @Test
        @DisplayName("should return empty list when no parties")
        void getAllParties_shouldReturnEmptyList() {
            // Arrange
            when(partyService.getActiveParties()).thenReturn(List.of());

            // Act
            List<Earth2026Party> result = controller.getAllParties();

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getRecruitingParties ----

    @Nested
    @DisplayName("getRecruitingParties Tests")
    class GetRecruitingPartiesTests {

        @Test
        @DisplayName("should return recruiting parties")
        void getRecruitingParties_shouldReturnRecruitingParties() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Recruiting", 1L, 1L);
            party.setId(1L);
            when(partyService.getRecruitingParties()).thenReturn(List.of(party));

            // Act
            List<Earth2026Party> result = controller.getRecruitingParties();

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- getParty ----

    @Nested
    @DisplayName("getParty Tests")
    class GetPartyTests {

        @Test
        @DisplayName("should return party when found")
        void getParty_shouldReturn200WhenFound() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            when(partyService.getParty(1L)).thenReturn(Optional.of(party));

            // Act
            ResponseEntity<Earth2026Party> response = controller.getParty(1L);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Party", response.getBody().getName());
        }

        @Test
        @DisplayName("should return 404 when not found")
        void getParty_shouldReturn404WhenNotFound() {
            // Arrange
            when(partyService.getParty(999L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<Earth2026Party> response = controller.getParty(999L);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- getPartiesAtLocation ----

    @Nested
    @DisplayName("getPartiesAtLocation Tests")
    class GetPartiesAtLocationTests {

        @Test
        @DisplayName("should return parties at a location")
        void getPartiesAtLocation_shouldReturnParties() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            when(partyService.getPartiesAtLocation(5L)).thenReturn(List.of(party));

            // Act
            List<Earth2026Party> result = controller.getPartiesAtLocation(5L);

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- createParty ----

    @Nested
    @DisplayName("createParty Tests")
    class CreatePartyTests {

        @Test
        @DisplayName("should create party with name, location, and leader")
        void createParty_shouldCreateParty() {
            // Arrange
            Earth2026Party party = new Earth2026Party("New Party", 1L, 1L);
            party.setId(1L);
            when(partyService.createParty("New Party", 1L, 1L)).thenReturn(party);

            // Act
            ResponseEntity<?> response = controller.createParty(
                    Map.of("name", "New Party", "locationId", 1L, "leaderId", 1L));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody() instanceof Earth2026Party);
        }

        @Test
        @DisplayName("should return 400 when name is missing")
        void createParty_shouldReturn400WhenNameMissing() {
            // Act
            ResponseEntity<?> response = controller.createParty(
                    Map.of("locationId", 1L, "leaderId", 1L));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertTrue(((Map<?, ?>) response.getBody()).containsKey("error"));
        }

        @Test
        @DisplayName("should return 400 when name is blank")
        void createParty_shouldReturn400WhenNameBlank() {
            // Act
            ResponseEntity<?> response = controller.createParty(
                    Map.of("name", "  ", "locationId", 1L, "leaderId", 1L));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when leader not found")
        void createParty_shouldReturn400WhenLeaderNotFound() {
            // Arrange
            when(partyService.createParty("Party", 1L, 999L))
                    .thenThrow(new IllegalArgumentException("Leader character not found: 999"));

            // Act
            ResponseEntity<?> response = controller.createParty(
                    Map.of("name", "Party", "locationId", 1L, "leaderId", 999L));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    // ---- disbandParty ----

    @Nested
    @DisplayName("disbandParty Tests")
    class DisbandPartyTests {

        @Test
        @DisplayName("should disband party")
        void disbandParty_shouldDisbandParty() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            party.setStatus("disbanded");
            when(partyService.disbandParty(1L)).thenReturn(Optional.of(party));

            // Act
            ResponseEntity<?> response = controller.disbandParty(1L);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            Map<?, ?> body = (Map<?, ?>) response.getBody();
            assertEquals("Party disbanded", body.get("message"));
        }

        @Test
        @DisplayName("should return 404 when party not found")
        void disbandParty_shouldReturn404WhenNotFound() {
            // Arrange
            when(partyService.disbandParty(999L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.disbandParty(999L);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- setRecruiting ----

    @Nested
    @DisplayName("setRecruiting Tests")
    class SetRecruitingTests {

        @Test
        @DisplayName("should set recruiting to true")
        void setRecruiting_shouldSetToTrue() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            party.setRecruiting(true);
            when(partyService.setRecruiting(1L, true)).thenReturn(Optional.of(party));

            // Act
            ResponseEntity<?> response = controller.setRecruiting(1L, Map.of("recruiting", true));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when recruiting field is missing")
        void setRecruiting_shouldReturn400WhenMissing() {
            // Act
            ResponseEntity<?> response = controller.setRecruiting(1L, Map.of());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when party not found")
        void setRecruiting_shouldReturn404WhenNotFound() {
            // Arrange
            when(partyService.setRecruiting(999L, true)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.setRecruiting(999L, Map.of("recruiting", true));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- setScenario ----

    @Nested
    @DisplayName("setScenario Tests")
    class SetScenarioTests {

        @Test
        @DisplayName("should set scenario for party")
        void setScenario_shouldSetScenario() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            party.setScenarioId(42L);
            when(partyService.setScenario(1L, 42L)).thenReturn(Optional.of(party));

            // Act
            ResponseEntity<?> response = controller.setScenario(1L, Map.of("scenarioId", 42L));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when party not found")
        void setScenario_shouldReturn404WhenNotFound() {
            // Arrange
            when(partyService.setScenario(999L, 1L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.setScenario(999L, Map.of("scenarioId", 1L));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- moveParty ----

    @Nested
    @DisplayName("moveParty Tests")
    class MovePartyTests {

        @Test
        @DisplayName("should move party to new location")
        void moveParty_shouldMoveParty() {
            // Arrange
            Earth2026Party party = new Earth2026Party("Party", 1L, 1L);
            party.setId(1L);
            party.setLocationId(5L);
            when(partyService.moveParty(1L, 5L)).thenReturn(Optional.of(party));

            // Act
            ResponseEntity<?> response = controller.moveParty(1L, Map.of("locationId", 5L));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when party not found")
        void moveParty_shouldReturn404WhenNotFound() {
            // Arrange
            when(partyService.moveParty(999L, 1L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.moveParty(999L, Map.of("locationId", 1L));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- getLocationChat ----

    @Nested
    @DisplayName("getLocationChat Tests")
    class GetLocationChatTests {

        @Test
        @DisplayName("should return location chat messages")
        void getLocationChat_shouldReturnMessages() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "Hello");
            msg.setId(1L);
            when(chatService.getLocationMessages(5L)).thenReturn(List.of(msg));

            // Act
            List<Earth2026Chat> result = controller.getLocationChat(5L);

            // Assert
            assertEquals(1, result.size());
            assertEquals("Hello", result.get(0).getContent());
        }
    }

    // ---- getPrivateChat ----

    @Nested
    @DisplayName("getPrivateChat Tests")
    class GetPrivateChatTests {

        @Test
        @DisplayName("should return private chat messages")
        void getPrivateChat_shouldReturnMessages() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "DM");
            msg.setId(1L);
            when(chatService.getPrivateMessages(1L, 2L)).thenReturn(List.of(msg));

            // Act
            List<Earth2026Chat> result = controller.getPrivateChat(1L, 2L);

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- getPartyChat ----

    @Nested
    @DisplayName("getPartyChat Tests")
    class GetPartyChatTests {

        @Test
        @DisplayName("should return party chat messages")
        void getPartyChat_shouldReturnMessages() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "Party");
            msg.setId(1L);
            when(chatService.getPartyMessages(3L)).thenReturn(List.of(msg));

            // Act
            List<Earth2026Chat> result = controller.getPartyChat(3L);

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- sendMessage ----

    @Nested
    @DisplayName("sendMessage Tests")
    class SendMessageTests {

        @Test
        @DisplayName("should send public message")
        void sendMessage_shouldSendPublicMessage() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "Hello");
            msg.setId(1L);
            msg.setMessageType("public");
            when(chatService.sendPublicMessage(1L, 5L, "Hello")).thenReturn(msg);

            // Act
            ResponseEntity<?> response = controller.sendMessage(
                    Map.of("senderId", 1L, "content", "Hello", "locationId", 5L));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody() instanceof Earth2026Chat);
        }

        @Test
        @DisplayName("should send private message")
        void sendMessage_shouldSendPrivateMessage() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "DM");
            msg.setId(1L);
            msg.setMessageType("private");
            when(chatService.sendPrivateMessage(1L, 2L, "DM")).thenReturn(msg);

            // Act
            ResponseEntity<?> response = controller.sendMessage(
                    Map.of("senderId", 1L, "content", "DM", "type", "private", "recipientId", 2L));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should send party message")
        void sendMessage_shouldSendPartyMessage() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "Team");
            msg.setId(1L);
            msg.setMessageType("party");
            when(chatService.sendPartyMessage(1L, 3L, "Team")).thenReturn(msg);

            // Act
            ResponseEntity<?> response = controller.sendMessage(
                    Map.of("senderId", 1L, "content", "Team", "type", "party", "partyId", 3L));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when content is missing")
        void sendMessage_shouldReturn400WhenContentMissing() {
            // Act
            ResponseEntity<?> response = controller.sendMessage(
                    Map.of("senderId", 1L));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when content is blank")
        void sendMessage_shouldReturn400WhenContentBlank() {
            // Act
            ResponseEntity<?> response = controller.sendMessage(
                    Map.of("senderId", 1L, "content", "  "));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }
    }

    // ---- getOnlineCharacters ----

    @Nested
    @DisplayName("getOnlineCharacters Tests")
    class GetOnlineCharactersTests {

        @Test
        @DisplayName("should return online characters")
        void getOnlineCharacters_shouldReturnOnlineCharacters() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            status.setId(1L);
            status.setOnline(true);
            when(onlineStatusRepository.findByIsOnlineTrue()).thenReturn(List.of(status));

            // Act
            List<Earth2026OnlineStatus> result = controller.getOnlineCharacters();

            // Assert
            assertEquals(1, result.size());
            assertTrue(result.get(0).isOnline());
        }
    }

    // ---- getOnlineAtLocation ----

    @Nested
    @DisplayName("getOnlineAtLocation Tests")
    class GetOnlineAtLocationTests {

        @Test
        @DisplayName("should return online characters at a location")
        void getOnlineAtLocation_shouldReturnCharacters() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            status.setId(1L);
            status.setLocationId(5L);
            when(onlineStatusRepository.findByLocationId(5L)).thenReturn(List.of(status));

            // Act
            List<Earth2026OnlineStatus> result = controller.getOnlineAtLocation(5L);

            // Assert
            assertEquals(1, result.size());
        }
    }

    // ---- getOnlineStatus ----

    @Nested
    @DisplayName("getOnlineStatus Tests")
    class GetOnlineStatusTests {

        @Test
        @DisplayName("should return online status when found")
        void getOnlineStatus_shouldReturn200WhenFound() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            status.setId(1L);
            when(onlineStatusRepository.findByCharacterId(1L)).thenReturn(Optional.of(status));

            // Act
            ResponseEntity<Earth2026OnlineStatus> response = controller.getOnlineStatus(1L);

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when not found")
        void getOnlineStatus_shouldReturn404WhenNotFound() {
            // Arrange
            when(onlineStatusRepository.findByCharacterId(999L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<Earth2026OnlineStatus> response = controller.getOnlineStatus(999L);

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    // ---- setActivity ----

    @Nested
    @DisplayName("setActivity Tests")
    class SetActivityTests {

        @Test
        @DisplayName("should set activity")
        void setActivity_shouldSetActivity() {
            // Arrange
            Earth2026OnlineStatus status = new Earth2026OnlineStatus(1L);
            status.setId(1L);
            status.setActivity("idle");
            when(onlineStatusRepository.findByCharacterId(1L)).thenReturn(Optional.of(status));
            when(onlineStatusRepository.save(status)).thenReturn(status);

            // Act
            ResponseEntity<?> response = controller.setActivity(1L, Map.of("activity", "exploring"));

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("exploring", status.getActivity());
            verify(onlineStatusRepository).save(status);
        }

        @Test
        @DisplayName("should return 400 when activity is missing")
        void setActivity_shouldReturn400WhenActivityMissing() {
            // Act
            ResponseEntity<?> response = controller.setActivity(1L, Map.of());

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 when activity is blank")
        void setActivity_shouldReturn400WhenActivityBlank() {
            // Act
            ResponseEntity<?> response = controller.setActivity(1L, Map.of("activity", "  "));

            // Assert
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when character not found")
        void setActivity_shouldReturn404WhenNotFound() {
            // Arrange
            when(onlineStatusRepository.findByCharacterId(999L)).thenReturn(Optional.empty());

            // Act
            ResponseEntity<?> response = controller.setActivity(999L, Map.of("activity", "exploring"));

            // Assert
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }
}

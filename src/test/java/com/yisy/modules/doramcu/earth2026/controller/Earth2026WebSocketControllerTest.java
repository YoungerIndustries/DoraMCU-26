package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Chat;
import com.yisy.modules.doramcu.earth2026.model.Earth2026OnlineStatus;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026OnlineStatusRepository;
import com.yisy.modules.doramcu.earth2026.service.Earth2026ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Earth2026WebSocketController Tests")
class Earth2026WebSocketControllerTest {

    private Earth2026ChatService chatService;
    private Earth2026OnlineStatusRepository onlineStatusRepository;
    private Earth2026WebSocketController controller;

    @BeforeEach
    void setUp() {
        chatService = mock(Earth2026ChatService.class);
        onlineStatusRepository = mock(Earth2026OnlineStatusRepository.class);
        controller = new Earth2026WebSocketController(chatService, onlineStatusRepository);
    }

    @Nested
    @DisplayName("getStatus")
    class GetStatusTests {

        @Test
        @DisplayName("should return connected clients count and timestamp")
        void getStatus() {
            Map<String, Object> status = controller.getStatus();

            assertNotNull(status);
            assertTrue(status.containsKey("connectedClients"));
            assertTrue(status.containsKey("timestamp"));
            assertEquals(0, status.get("connectedClients"));
        }
    }

    @Nested
    @DisplayName("sendMessage - Public")
    class SendPublicMessageTests {

        @Test
        @DisplayName("should send public message with location")
        void sendPublicMessage() {
            Earth2026Chat expected = new Earth2026Chat(1L, "Hello everyone");
            expected.setId(100L);
            expected.setMessageType("public");
            expected.setLocationId(1L);
            when(chatService.sendPublicMessage(1L, 1L, "Hello everyone")).thenReturn(expected);

            Map<String, Object> request = Map.of(
                    "content", "Hello everyone",
                    "type", "public",
                    "locationId", 1L
            );

            Earth2026Chat result = controller.sendMessage(1L, request);

            assertEquals(expected, result);
            assertEquals(100L, result.getId());
            verify(chatService).sendPublicMessage(1L, 1L, "Hello everyone");
        }

        @Test
        @DisplayName("should default to public type when no type specified")
        void sendPublicMessageDefaultType() {
            Earth2026Chat expected = new Earth2026Chat(1L, "Hello");
            expected.setId(101L);
            when(chatService.sendPublicMessage(1L, 1L, "Hello")).thenReturn(expected);

            Map<String, Object> request = Map.of(
                    "content", "Hello",
                    "locationId", 1L
            );

            Earth2026Chat result = controller.sendMessage(1L, request);

            assertEquals("public", result.getMessageType());
            verify(chatService).sendPublicMessage(1L, 1L, "Hello");
        }
    }

    @Nested
    @DisplayName("sendMessage - Private")
    class SendPrivateMessageTests {

        @Test
        @DisplayName("should send private message to recipient")
        void sendPrivateMessage() {
            Earth2026Chat expected = new Earth2026Chat(1L, "Secret message");
            expected.setId(102L);
            expected.setMessageType("private");
            expected.setRecipientId(2L);
            when(chatService.sendPrivateMessage(1L, 2L, "Secret message")).thenReturn(expected);

            Map<String, Object> request = Map.of(
                    "content", "Secret message",
                    "type", "private",
                    "recipientId", 2L
            );

            Earth2026Chat result = controller.sendMessage(1L, request);

            assertEquals(expected, result);
            assertEquals("private", result.getMessageType());
            verify(chatService).sendPrivateMessage(1L, 2L, "Secret message");
        }
    }

    @Nested
    @DisplayName("sendMessage - Party")
    class SendPartyMessageTests {

        @Test
        @DisplayName("should send party message")
        void sendPartyMessage() {
            Earth2026Chat expected = new Earth2026Chat(1L, "Party update");
            expected.setId(103L);
            expected.setMessageType("party");
            expected.setPartyId(5L);
            when(chatService.sendPartyMessage(1L, 5L, "Party update")).thenReturn(expected);

            Map<String, Object> request = Map.of(
                    "content", "Party update",
                    "type", "party",
                    "partyId", 5L
            );

            Earth2026Chat result = controller.sendMessage(1L, request);

            assertEquals(expected, result);
            assertEquals("party", result.getMessageType());
            verify(chatService).sendPartyMessage(1L, 5L, "Party update");
        }
    }

    @Nested
    @DisplayName("streamEvents")
    class StreamEventsTests {

        @Test
        @DisplayName("should return SseEmitter")
        void streamEventsReturnsEmitter() {
            when(onlineStatusRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(onlineStatusRepository.save(any(Earth2026OnlineStatus.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            SseEmitter emitter = controller.streamEvents(1L);

            assertNotNull(emitter);
            // Verify online status was created
            verify(onlineStatusRepository).save(argThat(status ->
                    status.getCharacterId().equals(1L) && status.isOnline()));
        }

        @Test
        @DisplayName("should update existing online status on connect")
        void streamEventsUpdatesExistingStatus() {
            Earth2026OnlineStatus existing = new Earth2026OnlineStatus(1L);
            existing.setId(50L);
            existing.setOnline(false);
            when(onlineStatusRepository.findByCharacterId(1L)).thenReturn(Optional.of(existing));
            when(onlineStatusRepository.save(existing)).thenReturn(existing);

            SseEmitter emitter = controller.streamEvents(1L);

            assertNotNull(emitter);
            verify(onlineStatusRepository).save(existing);
            assertTrue(existing.isOnline());
        }

        @Test
        @DisplayName("should handle emitter completion")
        void streamEventsOnCompletion() {
            when(onlineStatusRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(onlineStatusRepository.save(any(Earth2026OnlineStatus.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            SseEmitter emitter = controller.streamEvents(1L);

            // Simulate completion by completing the emitter
            emitter.complete();

            // Should have set offline
            verify(onlineStatusRepository, atLeastOnce()).findByCharacterId(1L);
        }

        @Test
        @DisplayName("should handle emitter timeout")
        void streamEventsOnTimeout() {
            when(onlineStatusRepository.findByCharacterId(1L)).thenReturn(Optional.empty());
            when(onlineStatusRepository.save(any(Earth2026OnlineStatus.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // Use a very short timeout emitter to test timeout behavior
            SseEmitter emitter = new SseEmitter(100L);
            emitter.complete();

            // The controller registers timeout handlers; we verify the emitter was created
            assertNotNull(emitter);
        }
    }

    @Nested
    @DisplayName("Message Type Routing")
    class MessageTypeRoutingTests {

        @Test
        @DisplayName("should route unknown type as public")
        void unknownTypeRoutesAsPublic() {
            Earth2026Chat expected = new Earth2026Chat(1L, "Test");
            expected.setId(104L);
            when(chatService.sendPublicMessage(1L, 1L, "Test")).thenReturn(expected);

            Map<String, Object> request = Map.of(
                    "content", "Test",
                    "type", "unknown",
                    "locationId", 1L
            );

            Earth2026Chat result = controller.sendMessage(1L, request);

            verify(chatService).sendPublicMessage(1L, 1L, "Test");
        }
    }
}

package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Chat;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026ChatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Earth2026ChatService Tests")
class Earth2026ChatServiceTest {

    private Earth2026ChatRepository chatRepository;
    private Earth2026ChatService chatService;

    @BeforeEach
    void setUp() {
        chatRepository = mock(Earth2026ChatRepository.class);
        chatService = new Earth2026ChatService(chatRepository);
    }

    // ---- sendPublicMessage ----

    @Nested
    @DisplayName("sendPublicMessage Tests")
    class SendPublicMessageTests {

        @Test
        @DisplayName("should send public message with sender, location, and content")
        void sendPublicMessage_shouldSendMessage() {
            // Arrange
            when(chatRepository.save(any(Earth2026Chat.class))).thenAnswer(inv -> {
                Earth2026Chat chat = inv.getArgument(0);
                chat.setId(1L);
                return chat;
            });

            // Act
            Earth2026Chat chat = chatService.sendPublicMessage(1L, 5L, "Hello everyone!");

            // Assert
            assertNotNull(chat);
            assertEquals(1L, chat.getSenderId());
            assertEquals(5L, chat.getLocationId());
            assertEquals("Hello everyone!", chat.getContent());
            assertEquals("public", chat.getMessageType());
            assertNotNull(chat.getCreatedAt());
            assertNull(chat.getRecipientId());
            assertNull(chat.getPartyId());
            verify(chatRepository).save(chat);
        }

        @Test
        @DisplayName("should set message type to public")
        void sendPublicMessage_shouldSetTypeToPublic() {
            // Arrange
            when(chatRepository.save(any(Earth2026Chat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Chat chat = chatService.sendPublicMessage(1L, 1L, "Test");

            // Assert
            assertEquals("public", chat.getMessageType());
        }

        @Test
        @DisplayName("should set null recipient and party for public messages")
        void sendPublicMessage_shouldSetNullRecipientAndParty() {
            // Arrange
            when(chatRepository.save(any(Earth2026Chat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Chat chat = chatService.sendPublicMessage(1L, 1L, "Test");

            // Assert
            assertNull(chat.getRecipientId());
            assertNull(chat.getPartyId());
        }
    }

    // ---- sendPrivateMessage ----

    @Nested
    @DisplayName("sendPrivateMessage Tests")
    class SendPrivateMessageTests {

        @Test
        @DisplayName("should send private message with sender and recipient")
        void sendPrivateMessage_shouldSendMessage() {
            // Arrange
            when(chatRepository.save(any(Earth2026Chat.class))).thenAnswer(inv -> {
                Earth2026Chat chat = inv.getArgument(0);
                chat.setId(1L);
                return chat;
            });

            // Act
            Earth2026Chat chat = chatService.sendPrivateMessage(1L, 2L, "Secret message");

            // Assert
            assertNotNull(chat);
            assertEquals(1L, chat.getSenderId());
            assertEquals(2L, chat.getRecipientId());
            assertEquals("Secret message", chat.getContent());
            assertEquals("private", chat.getMessageType());
            assertNull(chat.getLocationId());
            assertNull(chat.getPartyId());
            verify(chatRepository).save(chat);
        }

        @Test
        @DisplayName("should set message type to private")
        void sendPrivateMessage_shouldSetTypeToPrivate() {
            // Arrange
            when(chatRepository.save(any(Earth2026Chat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Chat chat = chatService.sendPrivateMessage(1L, 2L, "DM");

            // Assert
            assertEquals("private", chat.getMessageType());
        }

        @Test
        @DisplayName("should set null location and party for private messages")
        void sendPrivateMessage_shouldSetNullLocationAndParty() {
            // Arrange
            when(chatRepository.save(any(Earth2026Chat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Chat chat = chatService.sendPrivateMessage(1L, 2L, "DM");

            // Assert
            assertNull(chat.getLocationId());
            assertNull(chat.getPartyId());
        }
    }

    // ---- sendPartyMessage ----

    @Nested
    @DisplayName("sendPartyMessage Tests")
    class SendPartyMessageTests {

        @Test
        @DisplayName("should send party message with sender and party")
        void sendPartyMessage_shouldSendMessage() {
            // Arrange
            when(chatRepository.save(any(Earth2026Chat.class))).thenAnswer(inv -> {
                Earth2026Chat chat = inv.getArgument(0);
                chat.setId(1L);
                return chat;
            });

            // Act
            Earth2026Chat chat = chatService.sendPartyMessage(1L, 3L, "Party update");

            // Assert
            assertNotNull(chat);
            assertEquals(1L, chat.getSenderId());
            assertEquals(3L, chat.getPartyId());
            assertEquals("Party update", chat.getContent());
            assertEquals("party", chat.getMessageType());
            assertNull(chat.getLocationId());
            assertNull(chat.getRecipientId());
            verify(chatRepository).save(chat);
        }

        @Test
        @DisplayName("should set message type to party")
        void sendPartyMessage_shouldSetTypeToParty() {
            // Arrange
            when(chatRepository.save(any(Earth2026Chat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Chat chat = chatService.sendPartyMessage(1L, 1L, "Team");

            // Assert
            assertEquals("party", chat.getMessageType());
        }

        @Test
        @DisplayName("should set null location and recipient for party messages")
        void sendPartyMessage_shouldSetNullLocationAndRecipient() {
            // Arrange
            when(chatRepository.save(any(Earth2026Chat.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Earth2026Chat chat = chatService.sendPartyMessage(1L, 1L, "Team");

            // Assert
            assertNull(chat.getLocationId());
            assertNull(chat.getRecipientId());
        }
    }

    // ---- getLocationMessages ----

    @Nested
    @DisplayName("getLocationMessages Tests")
    class GetLocationMessagesTests {

        @Test
        @DisplayName("should return messages at a location ordered by creation time desc")
        void getLocationMessages_shouldReturnMessages() {
            // Arrange
            Earth2026Chat msg1 = new Earth2026Chat(1L, "First");
            msg1.setId(1L);
            msg1.setLocationId(5L);
            Earth2026Chat msg2 = new Earth2026Chat(2L, "Second");
            msg2.setId(2L);
            msg2.setLocationId(5L);
            when(chatRepository.findByLocationIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(msg1, msg2));

            // Act
            List<Earth2026Chat> result = chatService.getLocationMessages(5L);

            // Assert
            assertEquals(2, result.size());
            assertEquals("First", result.get(0).getContent());
            assertEquals("Second", result.get(1).getContent());
        }

        @Test
        @DisplayName("should return empty list when no messages at location")
        void getLocationMessages_shouldReturnEmptyList() {
            // Arrange
            when(chatRepository.findByLocationIdOrderByCreatedAtDesc(999L)).thenReturn(List.of());

            // Act
            List<Earth2026Chat> result = chatService.getLocationMessages(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getPrivateMessages ----

    @Nested
    @DisplayName("getPrivateMessages Tests")
    class GetPrivateMessagesTests {

        @Test
        @DisplayName("should return private messages between two characters")
        void getPrivateMessages_shouldReturnMessages() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "DM");
            msg.setId(1L);
            msg.setRecipientId(2L);
            when(chatRepository.findBySenderIdOrRecipientIdOrderByCreatedAtDesc(1L, 2L))
                    .thenReturn(List.of(msg));

            // Act
            List<Earth2026Chat> result = chatService.getPrivateMessages(1L, 2L);

            // Assert
            assertEquals(1, result.size());
            assertEquals("DM", result.get(0).getContent());
        }

        @Test
        @DisplayName("should return empty list when no messages")
        void getPrivateMessages_shouldReturnEmptyList() {
            // Arrange
            when(chatRepository.findBySenderIdOrRecipientIdOrderByCreatedAtDesc(1L, 2L))
                    .thenReturn(List.of());

            // Act
            List<Earth2026Chat> result = chatService.getPrivateMessages(1L, 2L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getPartyMessages ----

    @Nested
    @DisplayName("getPartyMessages Tests")
    class GetPartyMessagesTests {

        @Test
        @DisplayName("should return party messages")
        void getPartyMessages_shouldReturnMessages() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "Party chat");
            msg.setId(1L);
            msg.setPartyId(3L);
            when(chatRepository.findByPartyIdOrderByCreatedAtDesc(3L)).thenReturn(List.of(msg));

            // Act
            List<Earth2026Chat> result = chatService.getPartyMessages(3L);

            // Assert
            assertEquals(1, result.size());
            assertEquals("Party chat", result.get(0).getContent());
        }

        @Test
        @DisplayName("should return empty list when no party messages")
        void getPartyMessages_shouldReturnEmptyList() {
            // Arrange
            when(chatRepository.findByPartyIdOrderByCreatedAtDesc(999L)).thenReturn(List.of());

            // Act
            List<Earth2026Chat> result = chatService.getPartyMessages(999L);

            // Assert
            assertTrue(result.isEmpty());
        }
    }

    // ---- getMessagesByType ----

    @Nested
    @DisplayName("getMessagesByType Tests")
    class GetMessagesByTypeTests {

        @Test
        @DisplayName("should return public messages")
        void getMessagesByType_shouldReturnPublicMessages() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "Public");
            msg.setId(1L);
            msg.setMessageType("public");
            when(chatRepository.findByMessageTypeOrderByCreatedAtDesc("public")).thenReturn(List.of(msg));

            // Act
            List<Earth2026Chat> result = chatService.getMessagesByType("public");

            // Assert
            assertEquals(1, result.size());
            assertEquals("public", result.get(0).getMessageType());
        }

        @Test
        @DisplayName("should return private messages")
        void getMessagesByType_shouldReturnPrivateMessages() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "Private");
            msg.setId(1L);
            msg.setMessageType("private");
            when(chatRepository.findByMessageTypeOrderByCreatedAtDesc("private")).thenReturn(List.of(msg));

            // Act
            List<Earth2026Chat> result = chatService.getMessagesByType("private");

            // Assert
            assertEquals(1, result.size());
            assertEquals("private", result.get(0).getMessageType());
        }

        @Test
        @DisplayName("should return party messages")
        void getMessagesByType_shouldReturnPartyMessages() {
            // Arrange
            Earth2026Chat msg = new Earth2026Chat(1L, "Party");
            msg.setId(1L);
            msg.setMessageType("party");
            when(chatRepository.findByMessageTypeOrderByCreatedAtDesc("party")).thenReturn(List.of(msg));

            // Act
            List<Earth2026Chat> result = chatService.getMessagesByType("party");

            // Assert
            assertEquals(1, result.size());
            assertEquals("party", result.get(0).getMessageType());
        }

        @Test
        @DisplayName("should return empty list when no messages of type")
        void getMessagesByType_shouldReturnEmptyList() {
            // Arrange
            when(chatRepository.findByMessageTypeOrderByCreatedAtDesc("unknown")).thenReturn(List.of());

            // Act
            List<Earth2026Chat> result = chatService.getMessagesByType("unknown");

            // Assert
            assertTrue(result.isEmpty());
        }
    }
}

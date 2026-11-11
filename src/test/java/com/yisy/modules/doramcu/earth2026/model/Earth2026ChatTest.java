package com.yisy.modules.doramcu.earth2026.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Earth2026Chat Tests")
class Earth2026ChatTest {

    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {

        @Test
        @DisplayName("no-arg constructor should create empty chat")
        void noArgConstructor() {
            Earth2026Chat chat = new Earth2026Chat();
            assertNull(chat.getId());
            assertNull(chat.getSenderId());
            assertNull(chat.getRecipientId());
            assertNull(chat.getLocationId());
            assertNull(chat.getPartyId());
            assertNull(chat.getContent());
            assertEquals("public", chat.getMessageType());
            assertNull(chat.getCreatedAt());
        }

        @Test
        @DisplayName("full constructor should set sender, content, and timestamp")
        void fullConstructor() {
            Earth2026Chat chat = new Earth2026Chat(1L, "Hello world");
            assertEquals(1L, chat.getSenderId());
            assertEquals("Hello world", chat.getContent());
            assertNotNull(chat.getCreatedAt());
            assertEquals("public", chat.getMessageType());
        }
    }

    @Nested
    @DisplayName("Getters and Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("should set and get sender ID")
        void senderId() {
            Earth2026Chat chat = new Earth2026Chat();
            chat.setSenderId(42L);
            assertEquals(42L, chat.getSenderId());
        }

        @Test
        @DisplayName("should set and get recipient ID")
        void recipientId() {
            Earth2026Chat chat = new Earth2026Chat();
            chat.setRecipientId(99L);
            assertEquals(99L, chat.getRecipientId());
        }

        @Test
        @DisplayName("should set and get location ID")
        void locationId() {
            Earth2026Chat chat = new Earth2026Chat();
            chat.setLocationId(5L);
            assertEquals(5L, chat.getLocationId());
        }

        @Test
        @DisplayName("should set and get party ID")
        void partyId() {
            Earth2026Chat chat = new Earth2026Chat();
            chat.setPartyId(10L);
            assertEquals(10L, chat.getPartyId());
        }

        @Test
        @DisplayName("should set and get content")
        void content() {
            Earth2026Chat chat = new Earth2026Chat();
            chat.setContent("Test message");
            assertEquals("Test message", chat.getContent());
        }

        @Test
        @DisplayName("should set and get message type")
        void messageType() {
            Earth2026Chat chat = new Earth2026Chat();
            chat.setMessageType("private");
            assertEquals("private", chat.getMessageType());
        }

        @Test
        @DisplayName("should set and get created at")
        void createdAt() {
            Earth2026Chat chat = new Earth2026Chat();
            LocalDateTime now = LocalDateTime.now();
            chat.setCreatedAt(now);
            assertEquals(now, chat.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Message Types")
    class MessageTypeTests {

        @Test
        @DisplayName("default message type should be public")
        void defaultMessageType() {
            Earth2026Chat chat = new Earth2026Chat();
            assertEquals("public", chat.getMessageType());
        }

        @Test
        @DisplayName("should support private message type")
        void privateMessageType() {
            Earth2026Chat chat = new Earth2026Chat();
            chat.setMessageType("private");
            chat.setRecipientId(2L);
            assertEquals("private", chat.getMessageType());
            assertEquals(2L, chat.getRecipientId());
            assertNull(chat.getLocationId());
        }

        @Test
        @DisplayName("should support party message type")
        void partyMessageType() {
            Earth2026Chat chat = new Earth2026Chat();
            chat.setMessageType("party");
            chat.setPartyId(5L);
            assertEquals("party", chat.getMessageType());
            assertEquals(5L, chat.getPartyId());
        }

        @Test
        @DisplayName("should support public message type with location")
        void publicMessageType() {
            Earth2026Chat chat = new Earth2026Chat();
            chat.setMessageType("public");
            chat.setLocationId(3L);
            assertEquals("public", chat.getMessageType());
            assertEquals(3L, chat.getLocationId());
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString should include key fields")
        void toStringContent() {
            Earth2026Chat chat = new Earth2026Chat(1L, "Hello");
            chat.setId(100L);
            chat.setMessageType("private");

            String str = chat.toString();
            assertTrue(str.contains("id=100"));
            assertTrue(str.contains("senderId=1"));
            assertTrue(str.contains("messageType='private'"));
            assertTrue(str.contains("createdAt="));
        }
    }
}

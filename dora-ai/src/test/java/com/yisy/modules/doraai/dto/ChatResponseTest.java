package com.yisy.modules.doraai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatResponse Tests")
class ChatResponseTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/false defaults")
        void defaults() {
            ChatResponse response = new ChatResponse();
            assertNull(response.getSessionId());
            assertNull(response.getContent());
            assertFalse(response.isDone());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets sessionId, content, done")
        void setsFields() {
            ChatResponse response = new ChatResponse("sess-1", "Hello!", true);
            assertEquals("sess-1", response.getSessionId());
            assertEquals("Hello!", response.getContent());
            assertTrue(response.isDone());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get sessionId")
        void sessionId() {
            ChatResponse response = new ChatResponse();
            response.setSessionId("game-123");
            assertEquals("game-123", response.getSessionId());
        }

        @Test
        @DisplayName("Set and get content")
        void content() {
            ChatResponse response = new ChatResponse();
            response.setContent("Response text");
            assertEquals("Response text", response.getContent());
        }

        @Test
        @DisplayName("Set and get done")
        void done() {
            ChatResponse response = new ChatResponse();
            response.setDone(true);
            assertTrue(response.isDone());
        }
    }
}

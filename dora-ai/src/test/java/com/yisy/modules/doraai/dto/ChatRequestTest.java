package com.yisy.modules.doraai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChatRequest Tests")
class ChatRequestTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null defaults")
        void defaults() {
            ChatRequest request = new ChatRequest();
            assertNull(request.getSessionId());
            assertNull(request.getMessage());
            assertNull(request.getMode());
            assertNull(request.getProjectId());
            assertNull(request.getSourceLanguage());
            assertNull(request.getInputType());
            assertNull(request.getFileContext());
            assertNull(request.getProviderId());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets sessionId, message, mode, projectId")
        void setsFields() {
            ChatRequest request = new ChatRequest("sess-1", "Hello", "chat", "proj-1");
            assertEquals("sess-1", request.getSessionId());
            assertEquals("Hello", request.getMessage());
            assertEquals("chat", request.getMode());
            assertEquals("proj-1", request.getProjectId());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get sessionId")
        void sessionId() {
            ChatRequest request = new ChatRequest();
            request.setSessionId("game-123");
            assertEquals("game-123", request.getSessionId());
        }

        @Test
        @DisplayName("Set and get message")
        void message() {
            ChatRequest request = new ChatRequest();
            request.setMessage("How are you?");
            assertEquals("How are you?", request.getMessage());
        }

        @Test
        @DisplayName("Set and get mode")
        void mode() {
            ChatRequest request = new ChatRequest();
            request.setMode("code-gen");
            assertEquals("code-gen", request.getMode());
        }

        @Test
        @DisplayName("Set and get projectId")
        void projectId() {
            ChatRequest request = new ChatRequest();
            request.setProjectId("my-project");
            assertEquals("my-project", request.getProjectId());
        }

        @Test
        @DisplayName("Set and get sourceLanguage")
        void sourceLanguage() {
            ChatRequest request = new ChatRequest();
            request.setSourceLanguage("zh");
            assertEquals("zh", request.getSourceLanguage());
        }

        @Test
        @DisplayName("Set and get inputType")
        void inputType() {
            ChatRequest request = new ChatRequest();
            request.setInputType("voice");
            assertEquals("voice", request.getInputType());
        }

        @Test
        @DisplayName("Set and get fileContext")
        void fileContext() {
            ChatRequest request = new ChatRequest();
            request.setFileContext("public class Foo {}");
            assertEquals("public class Foo {}", request.getFileContext());
        }

        @Test
        @DisplayName("Set and get providerId")
        void providerId() {
            ChatRequest request = new ChatRequest();
            request.setProviderId("ollama");
            assertEquals("ollama", request.getProviderId());
        }
    }
}

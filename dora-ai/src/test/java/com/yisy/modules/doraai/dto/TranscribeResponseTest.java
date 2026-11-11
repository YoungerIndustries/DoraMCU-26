package com.yisy.modules.doraai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TranscribeResponse Tests")
class TranscribeResponseTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null/zero defaults")
        void defaults() {
            TranscribeResponse response = new TranscribeResponse();
            assertNull(response.getText());
            assertNull(response.getLanguage());
            assertEquals(0.0, response.getConfidence());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets text, language, and defaults confidence to 1.0")
        void setsFields() {
            TranscribeResponse response = new TranscribeResponse("Hello world", "en");
            assertEquals("Hello world", response.getText());
            assertEquals("en", response.getLanguage());
            assertEquals(1.0, response.getConfidence());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get text")
        void text() {
            TranscribeResponse response = new TranscribeResponse();
            response.setText("Transcribed text");
            assertEquals("Transcribed text", response.getText());
        }

        @Test
        @DisplayName("Set and get language")
        void language() {
            TranscribeResponse response = new TranscribeResponse();
            response.setLanguage("zh");
            assertEquals("zh", response.getLanguage());
        }

        @Test
        @DisplayName("Set and get confidence")
        void confidence() {
            TranscribeResponse response = new TranscribeResponse();
            response.setConfidence(0.95);
            assertEquals(0.95, response.getConfidence());
        }
    }
}

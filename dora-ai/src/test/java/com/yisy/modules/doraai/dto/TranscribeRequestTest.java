package com.yisy.modules.doraai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TranscribeRequest Tests")
class TranscribeRequestTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null defaults")
        void defaults() {
            TranscribeRequest request = new TranscribeRequest();
            assertNull(request.getAudioBase64());
            assertNull(request.getLanguageHint());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets audioBase64")
        void setsFields() {
            TranscribeRequest request = new TranscribeRequest("base64data");
            assertEquals("base64data", request.getAudioBase64());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get audioBase64")
        void audioBase64() {
            TranscribeRequest request = new TranscribeRequest();
            request.setAudioBase64("SGVsbG8=");
            assertEquals("SGVsbG8=", request.getAudioBase64());
        }

        @Test
        @DisplayName("Set and get languageHint")
        void languageHint() {
            TranscribeRequest request = new TranscribeRequest();
            request.setLanguageHint("en");
            assertEquals("en", request.getLanguageHint());
        }
    }
}

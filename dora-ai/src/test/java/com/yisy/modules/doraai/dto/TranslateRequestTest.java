package com.yisy.modules.doraai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TranslateRequest Tests")
class TranslateRequestTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null defaults")
        void defaults() {
            TranslateRequest request = new TranslateRequest();
            assertNull(request.getText());
            assertNull(request.getSourceLanguage());
            assertNull(request.getTargetLanguage());
        }
    }

    @Nested
    @DisplayName("2-arg constructor")
    class ConstructorTwoArgs {

        @Test
        @DisplayName("Sets text and targetLanguage")
        void setsFields() {
            TranslateRequest request = new TranslateRequest("Hello", "fr");
            assertEquals("Hello", request.getText());
            assertEquals("fr", request.getTargetLanguage());
            assertNull(request.getSourceLanguage());
        }
    }

    @Nested
    @DisplayName("3-arg constructor")
    class ConstructorThreeArgs {

        @Test
        @DisplayName("Sets text, sourceLanguage, and targetLanguage")
        void setsFields() {
            TranslateRequest request = new TranslateRequest("Hello", "en", "fr");
            assertEquals("Hello", request.getText());
            assertEquals("en", request.getSourceLanguage());
            assertEquals("fr", request.getTargetLanguage());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get text")
        void text() {
            TranslateRequest request = new TranslateRequest();
            request.setText("Goodbye");
            assertEquals("Goodbye", request.getText());
        }

        @Test
        @DisplayName("Set and get sourceLanguage")
        void sourceLanguage() {
            TranslateRequest request = new TranslateRequest();
            request.setSourceLanguage("zh");
            assertEquals("zh", request.getSourceLanguage());
        }

        @Test
        @DisplayName("Set and get targetLanguage")
        void targetLanguage() {
            TranslateRequest request = new TranslateRequest();
            request.setTargetLanguage("de");
            assertEquals("de", request.getTargetLanguage());
        }
    }
}

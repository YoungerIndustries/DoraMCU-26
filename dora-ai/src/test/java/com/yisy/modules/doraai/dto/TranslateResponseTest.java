package com.yisy.modules.doraai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TranslateResponse Tests")
class TranslateResponseTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null defaults")
        void defaults() {
            TranslateResponse response = new TranslateResponse();
            assertNull(response.getTranslatedText());
            assertNull(response.getSourceLanguage());
            assertNull(response.getTargetLanguage());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets translatedText, sourceLanguage, targetLanguage")
        void setsFields() {
            TranslateResponse response = new TranslateResponse("Bonjour", "en", "fr");
            assertEquals("Bonjour", response.getTranslatedText());
            assertEquals("en", response.getSourceLanguage());
            assertEquals("fr", response.getTargetLanguage());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get translatedText")
        void translatedText() {
            TranslateResponse response = new TranslateResponse();
            response.setTranslatedText("Hola");
            assertEquals("Hola", response.getTranslatedText());
        }

        @Test
        @DisplayName("Set and get sourceLanguage")
        void sourceLanguage() {
            TranslateResponse response = new TranslateResponse();
            response.setSourceLanguage("en");
            assertEquals("en", response.getSourceLanguage());
        }

        @Test
        @DisplayName("Set and get targetLanguage")
        void targetLanguage() {
            TranslateResponse response = new TranslateResponse();
            response.setTargetLanguage("es");
            assertEquals("es", response.getTargetLanguage());
        }
    }
}

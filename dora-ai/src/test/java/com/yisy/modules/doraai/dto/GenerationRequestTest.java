package com.yisy.modules.doraai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GenerationRequest Tests")
class GenerationRequestTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null defaults")
        void defaults() {
            GenerationRequest request = new GenerationRequest();
            assertNull(request.getProjectId());
            assertNull(request.getDescription());
            assertNull(request.getLanguage());
            assertNull(request.getContext());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets projectId and description")
        void setsFields() {
            GenerationRequest request = new GenerationRequest("proj-1", "Create a service");
            assertEquals("proj-1", request.getProjectId());
            assertEquals("Create a service", request.getDescription());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get projectId")
        void projectId() {
            GenerationRequest request = new GenerationRequest();
            request.setProjectId("my-project");
            assertEquals("my-project", request.getProjectId());
        }

        @Test
        @DisplayName("Set and get description")
        void description() {
            GenerationRequest request = new GenerationRequest();
            request.setDescription("Generate a controller");
            assertEquals("Generate a controller", request.getDescription());
        }

        @Test
        @DisplayName("Set and get language")
        void language() {
            GenerationRequest request = new GenerationRequest();
            request.setLanguage("java");
            assertEquals("java", request.getLanguage());
        }

        @Test
        @DisplayName("Set and get context")
        void context() {
            GenerationRequest request = new GenerationRequest();
            request.setContext("Spring Boot REST API");
            assertEquals("Spring Boot REST API", request.getContext());
        }
    }
}

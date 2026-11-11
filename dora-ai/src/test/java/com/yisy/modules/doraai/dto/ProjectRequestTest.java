package com.yisy.modules.doraai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProjectRequest Tests")
class ProjectRequestTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null defaults")
        void defaults() {
            ProjectRequest request = new ProjectRequest();
            assertNull(request.getName());
            assertNull(request.getPath());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets name and path")
        void setsFields() {
            ProjectRequest request = new ProjectRequest("MyProject", "/path/to/project");
            assertEquals("MyProject", request.getName());
            assertEquals("/path/to/project", request.getPath());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get name")
        void name() {
            ProjectRequest request = new ProjectRequest();
            request.setName("DoraMCU");
            assertEquals("DoraMCU", request.getName());
        }

        @Test
        @DisplayName("Set and get path")
        void path() {
            ProjectRequest request = new ProjectRequest();
            request.setPath("/home/user/projects/doramcu");
            assertEquals("/home/user/projects/doramcu", request.getPath());
        }
    }
}

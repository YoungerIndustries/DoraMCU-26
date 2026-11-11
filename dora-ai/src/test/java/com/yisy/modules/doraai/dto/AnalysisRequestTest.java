package com.yisy.modules.doraai.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnalysisRequest Tests")
class AnalysisRequestTest {

    @Nested
    @DisplayName("Default constructor")
    class DefaultConstructor {

        @Test
        @DisplayName("Initializes null defaults")
        void defaults() {
            AnalysisRequest request = new AnalysisRequest();
            assertNull(request.getProjectId());
            assertNull(request.getFilePaths());
            assertNull(request.getDescription());
            assertNull(request.getCode());
        }
    }

    @Nested
    @DisplayName("Parameterized constructor")
    class ParameterizedConstructor {

        @Test
        @DisplayName("Sets projectId, filePaths, description")
        void setsFields() {
            List<String> paths = List.of("src/Main.java");
            AnalysisRequest request = new AnalysisRequest("proj-1", paths, "Check for bugs");
            assertEquals("proj-1", request.getProjectId());
            assertSame(paths, request.getFilePaths());
            assertEquals("Check for bugs", request.getDescription());
        }
    }

    @Nested
    @DisplayName("Field setters and getters")
    class Fields {

        @Test
        @DisplayName("Set and get projectId")
        void projectId() {
            AnalysisRequest request = new AnalysisRequest();
            request.setProjectId("my-project");
            assertEquals("my-project", request.getProjectId());
        }

        @Test
        @DisplayName("Set and get filePaths")
        void filePaths() {
            AnalysisRequest request = new AnalysisRequest();
            List<String> paths = List.of("a.java", "b.java");
            request.setFilePaths(paths);
            assertSame(paths, request.getFilePaths());
        }

        @Test
        @DisplayName("Set and get description")
        void description() {
            AnalysisRequest request = new AnalysisRequest();
            request.setDescription("Find null pointer issues");
            assertEquals("Find null pointer issues", request.getDescription());
        }

        @Test
        @DisplayName("Set and get code")
        void code() {
            AnalysisRequest request = new AnalysisRequest();
            request.setCode("public class Foo {}");
            assertEquals("public class Foo {}", request.getCode());
        }
    }
}

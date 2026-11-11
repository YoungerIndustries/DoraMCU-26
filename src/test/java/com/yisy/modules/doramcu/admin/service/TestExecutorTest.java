package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase.TestStatus;
import com.yisy.modules.doramcu.admin.model.TestDefinition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestExecutorTest {

    @Mock
    private TestRegistry testRegistry;

    private TestExecutor testExecutor;

    @BeforeEach
    void setUp() {
        testExecutor = new TestExecutor(testRegistry);
    }

    @Test
    @DisplayName("runAllTests should return test results from registry")
    void runAllTests_shouldReturnResults() {
        TestDefinition apiTest = new TestDefinition("api-health", "Health endpoint", "API", "Test health");
        TestDefinition gameTest = new TestDefinition("game-progress", "Galaxy progress", "GAME", "Test galaxy");

        when(testRegistry.getTestCatalog()).thenReturn(Arrays.asList(apiTest, gameTest));
        when(testRegistry.executeTest("api-health")).thenReturn(true);
        when(testRegistry.executeTest("game-progress")).thenReturn(true);

        List<AiAdminTestCase> results = testExecutor.runAllTests();

        assertNotNull(results);
        assertEquals(2, results.size());
        for (AiAdminTestCase tc : results) {
            assertEquals(TestStatus.PASSED, tc.getStatus());
            assertTrue(tc.getDurationMs() >= 0);
        }
    }

    @Test
    @DisplayName("runAllTests should mark failed tests")
    void runAllTests_failedTest() {
        TestDefinition apiTest = new TestDefinition("api-health", "Health endpoint", "API", "Test health");

        when(testRegistry.getTestCatalog()).thenReturn(List.of(apiTest));
        when(testRegistry.executeTest("api-health")).thenReturn(false);

        List<AiAdminTestCase> results = testExecutor.runAllTests();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TestStatus.FAILED, results.get(0).getStatus());
    }

    @Test
    @DisplayName("runTestsByCategory should filter by category")
    void runTestsByCategory_shouldFilter() {
        TestDefinition apiTest = new TestDefinition("api-health", "Health endpoint", "API", "Test health");

        when(testRegistry.getTestsByCategory("API")).thenReturn(List.of(apiTest));
        when(testRegistry.executeTest("api-health")).thenReturn(true);

        List<AiAdminTestCase> results = testExecutor.runTestsByCategory("API");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("API", results.get(0).getCategory());
    }

    @Test
    @DisplayName("runTestById should run a specific test")
    void runTestById_shouldRunTest() {
        TestDefinition apiTest = new TestDefinition("api-health", "Health endpoint", "API", "Test health");

        when(testRegistry.getTestById("api-health")).thenReturn(apiTest);
        when(testRegistry.executeTest("api-health")).thenReturn(true);

        AiAdminTestCase result = testExecutor.runTestById("api-health");

        assertNotNull(result);
        assertEquals("Health endpoint", result.getTestName());
        assertEquals(TestStatus.PASSED, result.getStatus());
    }

    @Test
    @DisplayName("runTestById should return null for unknown test")
    void runTestById_unknownTest() {
        when(testRegistry.getTestById("unknown")).thenReturn(null);

        AiAdminTestCase result = testExecutor.runTestById("unknown");

        assertNull(result);
    }

    @Test
    @DisplayName("runTestsByIds should run multiple tests")
    void runTestsByIds_shouldRunTests() {
        TestDefinition apiTest = new TestDefinition("api-health", "Health endpoint", "API", "Test health");
        TestDefinition gameTest = new TestDefinition("game-progress", "Galaxy progress", "GAME", "Test galaxy");

        when(testRegistry.getTestsByIds(Arrays.asList("api-health", "game-progress")))
                .thenReturn(Arrays.asList(apiTest, gameTest));
        when(testRegistry.executeTest("api-health")).thenReturn(true);
        when(testRegistry.executeTest("game-progress")).thenReturn(false);

        List<AiAdminTestCase> results = testExecutor.runTestsByIds(Arrays.asList("api-health", "game-progress"));

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals(TestStatus.PASSED, results.get(0).getStatus());
        assertEquals(TestStatus.FAILED, results.get(1).getStatus());
    }

    @Test
    @DisplayName("runAllTests should handle test not found gracefully")
    void runAllTests_testNotFound() {
        TestDefinition apiTest = new TestDefinition("api-health", "Health endpoint", "API", "Test health");

        when(testRegistry.getTestCatalog()).thenReturn(List.of(apiTest));
        when(testRegistry.executeTest("api-health")).thenReturn(false);

        List<AiAdminTestCase> results = testExecutor.runAllTests();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals(TestStatus.FAILED, results.get(0).getStatus());
    }
}

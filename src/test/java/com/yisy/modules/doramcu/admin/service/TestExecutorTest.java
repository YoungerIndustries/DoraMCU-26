package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase.TestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestExecutorTest {

    @Mock
    private RestTemplate restTemplate;

    private TestExecutor testExecutor;

    @BeforeEach
    void setUp() {
        testExecutor = new TestExecutor(restTemplate, "http://localhost:8080");
    }

    @Test
    @DisplayName("runApiHealthTests should return test results")
    void runApiHealthTests_shouldReturnResults() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"status\":\"ok\"}"));

        List<AiAdminTestCase> results = testExecutor.runApiHealthTests();

        assertNotNull(results);
        assertEquals(3, results.size());
        for (AiAdminTestCase tc : results) {
            assertEquals("API", tc.getCategory());
            assertEquals(TestStatus.PASSED, tc.getStatus());
            assertTrue(tc.getDurationMs() >= 0);
        }
    }

    @Test
    @DisplayName("runApiHealthTests should mark failed test when endpoint returns error")
    void runApiHealthTests_failedEndpoint() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        List<AiAdminTestCase> results = testExecutor.runApiHealthTests();

        assertNotNull(results);
        for (AiAdminTestCase tc : results) {
            assertEquals(TestStatus.FAILED, tc.getStatus());
        }
    }

    @Test
    @DisplayName("runGameMechanicsTests should return test results")
    void runGameMechanicsTests_shouldReturnResults() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"progress\":0.5}"));

        List<AiAdminTestCase> results = testExecutor.runGameMechanicsTests();

        assertNotNull(results);
        assertEquals(2, results.size());
        for (AiAdminTestCase tc : results) {
            assertEquals("GAME", tc.getCategory());
        }
    }

    @Test
    @DisplayName("runAiChatTests should return test results")
    void runAiChatTests_shouldReturnResults() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        List<AiAdminTestCase> results = testExecutor.runAiChatTests();

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("AI_CHAT", results.get(0).getCategory());
        // 400 is expected (not 404), so it should pass
        assertEquals(TestStatus.PASSED, results.get(0).getStatus());
    }

    @Test
    @DisplayName("runAuthTests should return test results")
    void runAuthTests_shouldReturnResults() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        List<AiAdminTestCase> results = testExecutor.runAuthTests();

        assertNotNull(results);
        assertEquals(2, results.size());
        for (AiAdminTestCase tc : results) {
            assertEquals("AUTH", tc.getCategory());
        }
    }

    @Test
    @DisplayName("runTestsByCategory should filter by category")
    void runTestsByCategory_shouldFilter() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"status\":\"ok\"}"));

        List<AiAdminTestCase> results = testExecutor.runTestsByCategory("API");

        assertNotNull(results);
        for (AiAdminTestCase tc : results) {
            assertEquals("API", tc.getCategory());
        }
    }

    @Test
    @DisplayName("runAllTests should return all categories")
    void runAllTests_shouldReturnAll() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"status\":\"ok\"}"));

        List<AiAdminTestCase> results = testExecutor.runAllTests();

        assertNotNull(results);
        assertTrue(results.size() >= 6); // At least 3 API + 2 GAME + 1 AI_CHAT + 2 AUTH
    }
}

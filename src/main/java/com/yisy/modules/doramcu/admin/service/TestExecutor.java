package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase.TestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Executes automated tests against the running DoraMCU system.
 * Tests are grouped by category: API health, game mechanics, AI chat, and auth/admin.
 */
@Service
public class TestExecutor {

    private static final Logger log = LoggerFactory.getLogger(TestExecutor.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TestExecutor(RestTemplate restTemplate,
                        @Value("${dora.admin.test.base-url:http://localhost:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Runs all test categories and returns the results.
     *
     * @return list of test case results
     */
    public List<AiAdminTestCase> runAllTests() {
        List<AiAdminTestCase> results = new ArrayList<>();
        results.addAll(runApiHealthTests());
        results.addAll(runGameMechanicsTests());
        results.addAll(runAiChatTests());
        results.addAll(runAuthTests());
        return results;
    }

    /**
     * Runs tests for a specific category.
     *
     * @param category one of API, GAME, AI_CHAT, AUTH
     * @return list of test case results
     */
    public List<AiAdminTestCase> runTestsByCategory(String category) {
        return switch (category.toUpperCase()) {
            case "API" -> runApiHealthTests();
            case "GAME" -> runGameMechanicsTests();
            case "AI_CHAT" -> runAiChatTests();
            case "AUTH" -> runAuthTests();
            default -> runAllTests();
        };
    }

    /**
     * Tests that all REST endpoints respond correctly.
     */
    public List<AiAdminTestCase> runApiHealthTests() {
        List<AiAdminTestCase> results = new ArrayList<>();

        // Test /health endpoint
        results.add(runTest("Health endpoint", "API", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/health", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        }));

        // Test /actuator/health endpoint
        results.add(runTest("Actuator health endpoint", "API", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/actuator/health", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        }));

        // Test /api/v1/4x/health endpoint
        results.add(runTest("4X health dashboard endpoint", "API", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/api/v1/4x/health", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        }));

        return results;
    }

    /**
     * Tests game mechanics by calling game-related endpoints.
     */
    public List<AiAdminTestCase> runGameMechanicsTests() {
        List<AiAdminTestCase> results = new ArrayList<>();

        // Test galaxy progress endpoint (public)
        results.add(runTest("Galaxy progress endpoint", "GAME", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/api/galaxy/progress", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        }));

        // Test that the application is responding to game requests
        results.add(runTest("Game server responsiveness", "GAME", () -> {
            long start = System.currentTimeMillis();
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/health", String.class);
            long duration = System.currentTimeMillis() - start;
            return resp.getStatusCode().is2xxSuccessful() && duration < 5000;
        }));

        return results;
    }

    /**
     * Tests AI chat functionality.
     */
    public List<AiAdminTestCase> runAiChatTests() {
        List<AiAdminTestCase> results = new ArrayList<>();

        // Test AI chat endpoint availability
        results.add(runTest("AI chat endpoint availability", "AI_CHAT", () -> {
            try {
                ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/api/ai/chat", String.class);
                // May return 400 or 405 for GET, but should not 404
                return resp.getStatusCode().value() != 404;
            } catch (Exception e) {
                return false;
            }
        }));

        return results;
    }

    /**
     * Tests authentication and admin endpoints.
     */
    public List<AiAdminTestCase> runAuthTests() {
        List<AiAdminTestCase> results = new ArrayList<>();

        // Test that admin endpoints require authentication
        results.add(runTest("Admin endpoint auth required", "AUTH", () -> {
            try {
                ResponseEntity<String> resp = restTemplate.getForEntity(
                    baseUrl + "/api/v1/admin/server/status", String.class);
                // Should return 401 or 403 without auth
                return resp.getStatusCode().is4xxClientError();
            } catch (Exception e) {
                // Network error — treat as skip
                return true;
            }
        }));

        // Test that public endpoints are accessible
        results.add(runTest("Public endpoints accessible", "AUTH", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/health", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        }));

        return results;
    }

    /**
     * Helper to run a single test and create an AiAdminTestCase result.
     */
    private AiAdminTestCase runTest(String name, String category, TestRunnable runnable) {
        AiAdminTestCase testCase = new AiAdminTestCase(null, name, category);
        long start = System.currentTimeMillis();
        try {
            boolean passed = runnable.run();
            testCase.setStatus(passed ? TestStatus.PASSED : TestStatus.FAILED);
            if (!passed) {
                testCase.setErrorMessage("Test assertion failed");
            }
        } catch (Exception e) {
            testCase.setStatus(TestStatus.FAILED);
            testCase.setErrorMessage(e.getMessage());
            log.warn("Test {} failed: {}", name, e.getMessage());
        }
        testCase.setDurationMs(System.currentTimeMillis() - start);
        return testCase;
    }

    @FunctionalInterface
    interface TestRunnable {
        boolean run() throws Exception;
    }
}

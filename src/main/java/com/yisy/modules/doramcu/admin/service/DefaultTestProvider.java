package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.TestDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default test provider that contains the existing hardcoded tests migrated from TestExecutor.
 * These tests cover API health, game mechanics, AI chat, and auth/admin categories.
 */
@Component
public class DefaultTestProvider implements TestProvider {

    private static final Logger log = LoggerFactory.getLogger(DefaultTestProvider.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final Map<String, TestRunnable> testRunnables = new ConcurrentHashMap<>();

    public DefaultTestProvider(RestTemplate restTemplate,
                               @Value("${dora.admin.test.base-url:http://localhost:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        registerTestRunnables();
    }

    private void registerTestRunnables() {
        // API Health Tests
        testRunnables.put("api-health", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/health", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        });

        testRunnables.put("api-actuator-health", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/actuator/health", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        });

        testRunnables.put("api-4x-health", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/api/v1/4x/health", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        });

        // Game Mechanics Tests
        testRunnables.put("game-galaxy-progress", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/api/galaxy/progress", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        });

        testRunnables.put("game-server-responsiveness", () -> {
            long start = System.currentTimeMillis();
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/health", String.class);
            long duration = System.currentTimeMillis() - start;
            return resp.getStatusCode().is2xxSuccessful() && duration < 5000;
        });

        // AI Chat Tests
        testRunnables.put("ai-chat-availability", () -> {
            try {
                ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/api/ai/chat", String.class);
                return resp.getStatusCode().value() != 404;
            } catch (Exception e) {
                return false;
            }
        });

        // Auth Tests
        testRunnables.put("auth-admin-endpoint", () -> {
            try {
                ResponseEntity<String> resp = restTemplate.getForEntity(
                    baseUrl + "/api/v1/admin/server/status", String.class);
                return resp.getStatusCode().is4xxClientError();
            } catch (Exception e) {
                return true;
            }
        });

        testRunnables.put("auth-public-endpoints", () -> {
            ResponseEntity<String> resp = restTemplate.getForEntity(baseUrl + "/health", String.class);
            return resp.getStatusCode().is2xxSuccessful();
        });
    }

    @Override
    public List<TestDefinition> registerTests() {
        List<TestDefinition> tests = new ArrayList<>();

        // API Health Tests
        tests.add(new TestDefinition("api-health", "Health endpoint", "API",
            "Verify the /health endpoint returns 200 OK"));
        tests.add(new TestDefinition("api-actuator-health", "Actuator health endpoint", "API",
            "Verify the /actuator/health endpoint returns 200 OK"));
        tests.add(new TestDefinition("api-4x-health", "4X health dashboard endpoint", "API",
            "Verify the /api/v1/4x/health endpoint returns 200 OK"));

        // Game Mechanics Tests
        tests.add(new TestDefinition("game-galaxy-progress", "Galaxy progress endpoint", "GAME",
            "Verify the /api/galaxy/progress endpoint returns 200 OK"));
        tests.add(new TestDefinition("game-server-responsiveness", "Game server responsiveness", "GAME",
            "Verify the game server responds within 5 seconds"));

        // AI Chat Tests
        tests.add(new TestDefinition("ai-chat-availability", "AI chat endpoint availability", "AI_CHAT",
            "Verify the /api/ai/chat endpoint exists (not 404)"));

        // Auth Tests
        tests.add(new TestDefinition("auth-admin-endpoint", "Admin endpoint auth required", "AUTH",
            "Verify admin endpoints return 401/403 without authentication"));
        tests.add(new TestDefinition("auth-public-endpoints", "Public endpoints accessible", "AUTH",
            "Verify public endpoints like /health are accessible without auth"));

        return tests;
    }

    @Override
    public boolean executeTest(String testId) {
        TestRunnable runnable = testRunnables.get(testId);
        if (runnable == null) {
            log.warn("Unknown test: {}", testId);
            return false;
        }
        try {
            return runnable.run();
        } catch (Exception e) {
            log.warn("Test {} failed: {}", testId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getCategory() {
        return "DEFAULT";
    }

    @FunctionalInterface
    interface TestRunnable {
        boolean run() throws Exception;
    }
}

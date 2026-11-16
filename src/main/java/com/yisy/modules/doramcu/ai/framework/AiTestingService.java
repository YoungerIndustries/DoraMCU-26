package com.yisy.modules.doramcu.ai.framework;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Unified AI testing service that provides automated validation for all AI subsystems.
 * <p>
 * This service consolidates test execution, result analysis, and reporting
 * into a single interface. It supports:
 * <ul>
 *   <li>Decision-making accuracy tests</li>
 *   <li>Personality evolution tests</li>
 *   <li>Network topology tests</li>
 *   <li>Learning algorithm validation</li>
 *   <li>Cross-subsystem integration tests</li>
 * </ul>
 * </p>
 */
public interface AiTestingService {

    /**
     * Runs all AI tests and returns a report.
     */
    TestReport runAllTests();

    /**
     * Runs tests for a specific category.
     */
    TestReport runTestsByCategory(TestCategory category);

    /**
     * Runs a specific test by ID.
     */
    Optional<TestResult> runTestById(String testId);

    /**
     * Analyzes test results and generates recommendations.
     */
    TestAnalysis analyzeResults(TestReport report);

    /**
     * Returns the catalog of all available tests.
     */
    Map<String, TestMetadata> getTestCatalog();

    /**
     * Test categories.
     */
    enum TestCategory {
        DECISION_MAKING,
        PERSONALITY,
        NETWORK,
        LEARNING,
        INTEGRATION,
        PERFORMANCE
    }

    /**
     * Represents a test report.
     */
    class TestReport {
        private final String sessionId;
        private final long timestamp;
        private final int totalTests;
        private final int passedTests;
        private final int failedTests;
        private final int skippedTests;
        private final List<TestResult> results;

        public TestReport(String sessionId, long timestamp, int totalTests, int passedTests,
                          int failedTests, int skippedTests, List<TestResult> results) {
            this.sessionId = sessionId;
            this.timestamp = timestamp;
            this.totalTests = totalTests;
            this.passedTests = passedTests;
            this.failedTests = failedTests;
            this.skippedTests = skippedTests;
            this.results = results;
        }

        public String getSessionId() { return sessionId; }
        public long getTimestamp() { return timestamp; }
        public int getTotalTests() { return totalTests; }
        public int getPassedTests() { return passedTests; }
        public int getFailedTests() { return failedTests; }
        public int getSkippedTests() { return skippedTests; }
        public List<TestResult> getResults() { return results; }
        public double getPassRate() {
            return totalTests > 0 ? (double) passedTests / totalTests : 0.0;
        }
    }

    /**
     * Represents the result of a single test.
     */
    class TestResult {
        public enum TestStatus {
            PASSED, FAILED, SKIPPED, ERROR
        }

        private final String testId;
        private final String testName;
        private final TestCategory category;
        private final TestStatus status;
        private final String message;
        private final long durationMs;

        public TestResult(String testId, String testName, TestCategory category,
                          TestStatus status, String message, long durationMs) {
            this.testId = testId;
            this.testName = testName;
            this.category = category;
            this.status = status;
            this.message = message;
            this.durationMs = durationMs;
        }

        public String getTestId() { return testId; }
        public String getTestName() { return testName; }
        public TestCategory getCategory() { return category; }
        public TestStatus getStatus() { return status; }
        public String getMessage() { return message; }
        public long getDurationMs() { return durationMs; }
    }

    /**
     * Represents analysis of test results.
     */
    class TestAnalysis {
        private final double overallScore;
        private final List<String> recommendations;
        private final Map<String, Double> categoryScores;

        public TestAnalysis(double overallScore, List<String> recommendations,
                            Map<String, Double> categoryScores) {
            this.overallScore = overallScore;
            this.recommendations = recommendations;
            this.categoryScores = categoryScores;
        }

        public double getOverallScore() { return overallScore; }
        public List<String> getRecommendations() { return recommendations; }
        public Map<String, Double> getCategoryScores() { return categoryScores; }
    }

    /**
     * Metadata about a test.
     */
    class TestMetadata {
        private final String testId;
        private final String testName;
        private final TestCategory category;
        private final String description;

        public TestMetadata(String testId, String testName, TestCategory category, String description) {
            this.testId = testId;
            this.testName = testName;
            this.category = category;
            this.description = description;
        }

        public String getTestId() { return testId; }
        public String getTestName() { return testName; }
        public TestCategory getCategory() { return category; }
        public String getDescription() { return description; }
    }

}

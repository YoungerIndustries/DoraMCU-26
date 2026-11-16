package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.admin.model.TestDefinition;
import com.yisy.modules.doramcu.admin.service.TestProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of AiTestingService that bridges the existing TestProvider infrastructure
 * with the unified AI framework testing interface.
 * <p>
 * This service discovers all TestProvider beans and provides a unified testing API.
 * </p>
 */
@Service
public class AiTestingServiceImpl implements AiTestingService, AiService {

    private static final Logger log = LoggerFactory.getLogger(AiTestingServiceImpl.class);

    private final List<com.yisy.modules.doramcu.admin.service.TestProvider> adminTestProviders = new ArrayList<>();
    private final Map<String, TestMetadata> testCatalog = new HashMap<>();
    private final Map<String, com.yisy.modules.doramcu.admin.service.TestProvider> testProviderMap = new HashMap<>();
    private AiServiceStatus status = AiServiceStatus.STOPPED;

    @Autowired
    public AiTestingServiceImpl(ApplicationContext context) {
        // Discover all TestProvider beans from admin service
        Map<String, com.yisy.modules.doramcu.admin.service.TestProvider> beans = context.getBeansOfType(com.yisy.modules.doramcu.admin.service.TestProvider.class);
        for (com.yisy.modules.doramcu.admin.service.TestProvider provider : beans.values()) {
            adminTestProviders.add(provider);
            testProviderMap.put(provider.getCategory(), provider);
        }
        buildTestCatalog();
        log.info("AI Testing Service initialized with {} providers and {} tests",
                adminTestProviders.size(), testCatalog.size());
    }

    private void buildTestCatalog() {
        testCatalog.clear();
        for (com.yisy.modules.doramcu.admin.service.TestProvider provider : adminTestProviders) {
            for (TestDefinition definition : provider.registerTests()) {
                if (definition.isEnabled()) {
                    TestCategory category = mapCategory(provider.getCategory());
                    TestMetadata metadata = new TestMetadata(
                            definition.getId(),
                            definition.getName(),
                            category,
                            definition.getDescription()
                    );
                    testCatalog.put(definition.getId(), metadata);
                }
            }
        }
    }

    private TestCategory mapCategory(String category) {
        return switch (category.toUpperCase()) {
            case "AI_NETWORK" -> TestCategory.NETWORK;
            case "AI_PERSONALITY", "PERSONALITY" -> TestCategory.PERSONALITY;
            case "AI_DECISION", "DECISION" -> TestCategory.DECISION_MAKING;
            case "AI_LEARNING", "LEARNING" -> TestCategory.LEARNING;
            case "INTEGRATION" -> TestCategory.INTEGRATION;
            case "PERFORMANCE" -> TestCategory.PERFORMANCE;
            default -> TestCategory.INTEGRATION;
        };
    }

    @Override
    public TestReport runAllTests() {
        String sessionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        List<TestResult> results = new ArrayList<>();

        for (TestMetadata metadata : testCatalog.values()) {
            results.add(runTestInternal(metadata.getTestId()));
        }

        long duration = System.currentTimeMillis() - startTime;
        int passed = (int) results.stream().filter(r -> r.getStatus() == TestResult.TestStatus.PASSED).count();
        int failed = (int) results.stream().filter(r -> r.getStatus() == TestResult.TestStatus.FAILED).count();
        int skipped = (int) results.stream().filter(r -> r.getStatus() == TestResult.TestStatus.SKIPPED).count();

        log.info("Test report {}: {} total, {} passed, {} failed, {} skipped ({}ms)",
                sessionId, results.size(), passed, failed, skipped, duration);

        return new TestReport(sessionId, startTime, results.size(), passed, failed, skipped, results);
    }

    @Override
    public TestReport runTestsByCategory(TestCategory category) {
        String sessionId = UUID.randomUUID().toString();
        long startTime = System.currentTimeMillis();
        List<TestResult> results = new ArrayList<>();

        for (TestMetadata metadata : testCatalog.values()) {
            if (metadata.getCategory() == category) {
                results.add(runTestInternal(metadata.getTestId()));
            }
        }

        int passed = (int) results.stream().filter(r -> r.getStatus() == TestResult.TestStatus.PASSED).count();
        int failed = (int) results.stream().filter(r -> r.getStatus() == TestResult.TestStatus.FAILED).count();
        int skipped = (int) results.stream().filter(r -> r.getStatus() == TestResult.TestStatus.SKIPPED).count();

        return new TestReport(sessionId, startTime, results.size(), passed, failed, skipped, results);
    }

    @Override
    public Optional<TestResult> runTestById(String testId) {
        if (!testCatalog.containsKey(testId)) {
            return Optional.empty();
        }
        return Optional.of(runTestInternal(testId));
    }

    private TestResult runTestInternal(String testId) {
        TestMetadata metadata = testCatalog.get(testId);
        if (metadata == null) {
            return new TestResult(testId, "Unknown", TestCategory.INTEGRATION,
                    TestResult.TestStatus.ERROR, "Test not found", 0);
        }

        long startTime = System.currentTimeMillis();
        try {
            // Find the provider for this test
            for (com.yisy.modules.doramcu.admin.service.TestProvider provider : adminTestProviders) {
                List<TestDefinition> tests = provider.registerTests();
                for (TestDefinition test : tests) {
                    if (test.getId().equals(testId)) {
                        boolean passed = provider.executeTest(testId);
                        long duration = System.currentTimeMillis() - startTime;
                        return new TestResult(
                                testId,
                                metadata.getTestName(),
                                metadata.getCategory(),
                                passed ? TestResult.TestStatus.PASSED : TestResult.TestStatus.FAILED,
                                passed ? "Test passed" : "Test failed",
                                duration
                        );
                    }
                }
            }
            return new TestResult(testId, metadata.getTestName(), metadata.getCategory(),
                    TestResult.TestStatus.ERROR, "No provider found", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn("Test {} failed with exception: {}", testId, e.getMessage());
            return new TestResult(testId, metadata.getTestName(), metadata.getCategory(),
                    TestResult.TestStatus.ERROR, e.getMessage(), duration);
        }
    }

    @Override
    public TestAnalysis analyzeResults(TestReport report) {
        if (report.getTotalTests() == 0) {
            return new TestAnalysis(0.0, Collections.emptyList(), Collections.emptyMap());
        }

        double overallScore = report.getPassRate();

        // Calculate category scores
        Map<String, Double> categoryScores = new HashMap<>();
        for (TestResult.TestStatus status : TestResult.TestStatus.values()) {
            // Group by category
        }
        Map<TestCategory, List<TestResult>> byCategory = report.getResults().stream()
                .collect(Collectors.groupingBy(TestResult::getCategory));

        for (Map.Entry<TestCategory, List<TestResult>> entry : byCategory.entrySet()) {
            List<TestResult> results = entry.getValue();
            long passed = results.stream().filter(r -> r.getStatus() == TestResult.TestStatus.PASSED).count();
            categoryScores.put(entry.getKey().name(), results.isEmpty() ? 0.0 : (double) passed / results.size());
        }

        // Generate recommendations
        List<String> recommendations = new ArrayList<>();
        if (overallScore < 0.8) {
            recommendations.add("Overall test pass rate is below 80%. Consider reviewing failing tests.");
        }
        for (Map.Entry<String, Double> entry : categoryScores.entrySet()) {
            if (entry.getValue() < 0.7) {
                recommendations.add("Category " + entry.getKey() + " has low pass rate (" + String.format("%.0f", entry.getValue() * 100) + "%).");
            }
        }

        return new TestAnalysis(overallScore, recommendations, categoryScores);
    }

    public void registerTestProvider(com.yisy.modules.doramcu.admin.service.TestProvider provider) {
        adminTestProviders.add(provider);
        testProviderMap.put(provider.getCategory(), provider);
        buildTestCatalog();
        log.info("Registered test provider: {} with {} tests", provider.getCategory(), provider.registerTests().size());
    }

    public List<com.yisy.modules.doramcu.admin.service.TestProvider> getTestProviders() {
        return Collections.unmodifiableList(adminTestProviders);
    }

    @Override
    public Map<String, TestMetadata> getTestCatalog() {
        return Collections.unmodifiableMap(testCatalog);
    }

    // --- AiService implementation for AiFrameworkRegistry discovery ---

    @Override
    public String getServiceId() {
        return "ai-testing-service";
    }

    @Override
    public String getServiceName() {
        return "AI Testing Service";
    }

    @Override
    public AiServiceStatus getStatus() {
        return status;
    }

    @Override
    public void initialize() {
        status = AiServiceStatus.RUNNING;
        log.info("AI Testing Service initialized with {} providers and {} tests",
                adminTestProviders.size(), testCatalog.size());
    }

    @Override
    public void shutdown() {
        status = AiServiceStatus.STOPPED;
        log.info("AI Testing Service shutdown");
    }

    @Override
    public boolean isHealthy() {
        return status == AiServiceStatus.RUNNING;
    }

    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("status", status.name());
        metrics.put("testProviders", adminTestProviders.size());
        metrics.put("totalTests", testCatalog.size());
        return metrics;
    }
}

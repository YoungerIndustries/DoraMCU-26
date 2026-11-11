package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.TestDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Collects all TestProvider beans at startup and builds a test catalog.
 * The TestExecutor queries this registry instead of running hardcoded tests.
 */
@Service
public class TestRegistry {

    private static final Logger log = LoggerFactory.getLogger(TestRegistry.class);

    private final List<TestProvider> testProviders;
    private List<TestDefinition> testCatalog;

    public TestRegistry(List<TestProvider> testProviders) {
        this.testProviders = testProviders;
    }

    @PostConstruct
    public void init() {
        testCatalog = new ArrayList<>();
        for (TestProvider provider : testProviders) {
            List<TestDefinition> tests = provider.registerTests();
            testCatalog.addAll(tests);
            log.info("Registered {} tests from provider: {}", tests.size(), provider.getClass().getSimpleName());
        }
        log.info("TestRegistry initialized with {} total tests", testCatalog.size());
    }

    /**
     * Returns the full test catalog with all registered tests.
     *
     * @return list of all test definitions
     */
    public List<TestDefinition> getTestCatalog() {
        return testCatalog;
    }

    /**
     * Returns tests filtered by category.
     *
     * @param category the category name (e.g., API, GAME, AI_CHAT, AUTH)
     * @return list of test definitions in the specified category
     */
    public List<TestDefinition> getTestsByCategory(String category) {
        return testCatalog.stream()
                .filter(t -> category.equalsIgnoreCase(t.getCategory()))
                .collect(Collectors.toList());
    }

    /**
     * Returns a test definition by its ID.
     *
     * @param testId the unique test ID
     * @return the test definition or null if not found
     */
    public TestDefinition getTestById(String testId) {
        return testCatalog.stream()
                .filter(t -> t.getId().equals(testId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns tests filtered by a list of IDs.
     *
     * @param testIds list of test IDs
     * @return list of test definitions matching the IDs
     */
    public List<TestDefinition> getTestsByIds(List<String> testIds) {
        return testCatalog.stream()
                .filter(t -> testIds.contains(t.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Executes a test by finding the provider that registered it.
     *
     * @param testId the unique test ID
     * @return true if the test passed, false if not found or failed
     */
    public boolean executeTest(String testId) {
        for (TestProvider provider : testProviders) {
            List<TestDefinition> tests = provider.registerTests();
            if (tests.stream().anyMatch(t -> t.getId().equals(testId))) {
                return provider.executeTest(testId);
            }
        }
        log.warn("Test not found: {}", testId);
        return false;
    }

    /**
     * Returns the list of available categories.
     *
     * @return list of unique category names
     */
    public List<String> getCategories() {
        return testCatalog.stream()
                .map(TestDefinition::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }
}

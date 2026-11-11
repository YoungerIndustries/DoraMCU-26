package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.AiAdminTestCase.TestStatus;
import com.yisy.modules.doramcu.admin.model.TestDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes automated tests against the running DoraMCU system.
 * Uses TestRegistry to discover and run tests instead of hardcoded test methods.
 * Tests are grouped by category: API health, game mechanics, AI chat, and auth/admin.
 */
@Service
public class TestExecutor {

    private static final Logger log = LoggerFactory.getLogger(TestExecutor.class);

    private final TestRegistry testRegistry;

    public TestExecutor(TestRegistry testRegistry) {
        this.testRegistry = testRegistry;
    }

    /**
     * Runs all registered tests and returns the results.
     *
     * @return list of test case results
     */
    public List<AiAdminTestCase> runAllTests() {
        List<TestDefinition> allTests = testRegistry.getTestCatalog();
        List<AiAdminTestCase> results = new ArrayList<>();
        for (TestDefinition test : allTests) {
            results.add(runTestDefinition(test));
        }
        return results;
    }

    /**
     * Runs tests for a specific category.
     *
     * @param category one of API, GAME, AI_CHAT, AUTH
     * @return list of test case results
     */
    public List<AiAdminTestCase> runTestsByCategory(String category) {
        List<TestDefinition> tests = testRegistry.getTestsByCategory(category);
        List<AiAdminTestCase> results = new ArrayList<>();
        for (TestDefinition test : tests) {
            results.add(runTestDefinition(test));
        }
        return results;
    }

    /**
     * Runs a specific test by its ID.
     *
     * @param testId the unique test ID
     * @return the test case result
     */
    public AiAdminTestCase runTestById(String testId) {
        TestDefinition test = testRegistry.getTestById(testId);
        if (test == null) {
            log.warn("Test not found: {}", testId);
            return null;
        }
        return runTestDefinition(test);
    }

    /**
     * Runs a list of tests by their IDs.
     *
     * @param testIds list of test IDs to run
     * @return list of test case results
     */
    public List<AiAdminTestCase> runTestsByIds(List<String> testIds) {
        List<TestDefinition> tests = testRegistry.getTestsByIds(testIds);
        List<AiAdminTestCase> results = new ArrayList<>();
        for (TestDefinition test : tests) {
            results.add(runTestDefinition(test));
        }
        return results;
    }

    /**
     * Helper to run a single TestDefinition and create an AiAdminTestCase result.
     */
    private AiAdminTestCase runTestDefinition(TestDefinition test) {
        AiAdminTestCase testCase = new AiAdminTestCase(null, test.getName(), test.getCategory());
        long start = System.currentTimeMillis();
        boolean passed = testRegistry.executeTest(test.getId());
        testCase.setStatus(passed ? TestStatus.PASSED : TestStatus.FAILED);
        if (!passed) {
            testCase.setErrorMessage("Test assertion failed");
        }
        testCase.setDurationMs(System.currentTimeMillis() - start);
        return testCase;
    }
}

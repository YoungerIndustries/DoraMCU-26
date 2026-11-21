package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;
import com.yisy.modules.doramcu.admin.model.TestDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

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
        return runAllTests(null);
    }

    /**
     * Runs all registered tests with an optional progress callback.
     *
     * @param callback optional progress callback
     * @return list of test case results
     */
    public List<AiAdminTestCase> runAllTests(OrderProgressCallback callback) {
        List<TestDefinition> allTests = testRegistry.getTestCatalog();
        List<AiAdminTestCase> results = new ArrayList<>();
        int total = allTests.size();
        for (int i = 0; i < allTests.size(); i++) {
            TestDefinition test = allTests.get(i);
            if (callback != null) {
                callback.onTestStarted(test.getName());
            }
            AiAdminTestCase result = runTestDefinition(test);
            results.add(result);
            if (callback != null) {
                callback.onTestCompleted(result);
                callback.onProgressUpdate(i + 1, total);
            }
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
        return runTestsByCategory(category, null);
    }

    /**
     * Runs tests for a specific category with an optional progress callback.
     *
     * @param category one of API, GAME, AI_CHAT, AUTH
     * @param callback optional progress callback
     * @return list of test case results
     */
    public List<AiAdminTestCase> runTestsByCategory(String category, OrderProgressCallback callback) {
        List<TestDefinition> tests = testRegistry.getTestsByCategory(category);
        List<AiAdminTestCase> results = new ArrayList<>();
        int total = tests.size();
        for (int i = 0; i < tests.size(); i++) {
            TestDefinition test = tests.get(i);
            if (callback != null) {
                callback.onTestStarted(test.getName());
            }
            AiAdminTestCase result = runTestDefinition(test);
            results.add(result);
            if (callback != null) {
                callback.onTestCompleted(result);
                callback.onProgressUpdate(i + 1, total);
            }
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
        return runTestById(testId, null);
    }

    /**
     * Runs a specific test by its ID with an optional progress callback.
     *
     * @param testId the unique test ID
     * @param callback optional progress callback
     * @return the test case result
     */
    public AiAdminTestCase runTestById(String testId, OrderProgressCallback callback) {
        TestDefinition test = testRegistry.getTestById(testId);
        if (test == null) {
            log.warn("Test not found: {}", testId);
            return null;
        }
        if (callback != null) {
            callback.onTestStarted(test.getName());
        }
        AiAdminTestCase result = runTestDefinition(test);
        if (callback != null) {
            callback.onTestCompleted(result);
            callback.onProgressUpdate(1, 1);
        }
        return result;
    }

    /**
     * Runs a list of tests by their IDs.
     *
     * @param testIds list of test IDs to run
     * @return list of test case results
     */
    public List<AiAdminTestCase> runTestsByIds(List<String> testIds) {
        return runTestsByIds(testIds, null);
    }

    /**
     * Runs a list of tests by their IDs with an optional progress callback.
     *
     * @param testIds list of test IDs to run
     * @param callback optional progress callback
     * @return list of test case results
     */
    public List<AiAdminTestCase> runTestsByIds(List<String> testIds, OrderProgressCallback callback) {
        List<TestDefinition> tests = testRegistry.getTestsByIds(testIds);
        List<AiAdminTestCase> results = new ArrayList<>();
        int total = tests.size();
        for (int i = 0; i < tests.size(); i++) {
            TestDefinition test = tests.get(i);
            if (callback != null) {
                callback.onTestStarted(test.getName());
            }
            AiAdminTestCase result = runTestDefinition(test);
            results.add(result);
            if (callback != null) {
                callback.onTestCompleted(result);
                callback.onProgressUpdate(i + 1, total);
            }
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
        testCase.setStatus(passed ? AiAdminTestCase.TestStatus.PASSED : AiAdminTestCase.TestStatus.FAILED);
        if (!passed) {
            testCase.setErrorMessage("Test assertion failed");
        }
        testCase.setDurationMs(System.currentTimeMillis() - start);
        return testCase;
    }
}

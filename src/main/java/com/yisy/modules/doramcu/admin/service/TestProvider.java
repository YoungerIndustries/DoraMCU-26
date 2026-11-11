package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.TestDefinition;

import java.util.List;

/**
 * Interface for registering tests with the TestRegistry.
 * Any module can implement this interface to add its own tests to the AI admin system.
 * The TestRegistry collects all TestProvider beans at startup and builds a test catalog.
 */
public interface TestProvider {

    /**
     * Register tests provided by this provider.
     * Each test definition includes metadata (id, name, category, description)
     * and a runnable that executes the test logic.
     *
     * @return list of test definitions with their execution logic
     */
    List<TestDefinition> registerTests();

    /**
     * Execute a test by its ID. Returns true if the test passes, false if it fails.
     * Any errors during execution should be handled internally and return false.
     *
     * @param testId the unique test ID from the TestDefinition
     * @return true if the test passed
     */
    boolean executeTest(String testId);

    /**
     * Returns the category this provider belongs to.
     * Used for filtering tests by category.
     *
     * @return category name (e.g., API, GAME, AI_CHAT, AUTH)
     */
    default String getCategory() {
        return "DEFAULT";
    }
}

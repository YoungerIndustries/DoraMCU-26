package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminTestCase;

/**
 * Callback interface for tracking order execution progress.
 * Called by TestExecutor after each test case completes.
 */
@FunctionalInterface
public interface OrderProgressCallback {

    /**
     * Called when a test starts executing.
     *
     * @param testName name of the test starting
     */
    void onTestStarted(String testName);

    /**
     * Called when a test completes.
     *
     * @param testCase the completed test case result
     */
    default void onTestCompleted(AiAdminTestCase testCase) {
        // Default no-op implementation
    }

    /**
     * Called to update overall progress.
     *
     * @param testsCompleted number of tests completed so far
     * @param testsTotal total number of tests
     */
    default void onProgressUpdate(int testsCompleted, int testsTotal) {
        // Default no-op implementation
    }
}

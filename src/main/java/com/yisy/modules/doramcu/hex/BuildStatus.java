package com.yisy.modules.doramcu.hex;

/**
 * Lifecycle status of a build request in the galaxy construction queue.
 */
public enum BuildStatus {
    /**
     * Request created, waiting in queue.
     */
    PENDING,

    /**
     * Dependencies satisfied, waiting for its turn.
     */
    READY,

    /**
     * Currently being built by the DoraMCU module.
     */
    IN_PROGRESS,

    /**
     * Build completed successfully.
     */
    COMPLETED,

    /**
     * Build failed (see error message in BuildResult).
     */
    FAILED,

    /**
     * Build was cancelled (e.g., galaxy creation aborted).
     */
    CANCELLED
}

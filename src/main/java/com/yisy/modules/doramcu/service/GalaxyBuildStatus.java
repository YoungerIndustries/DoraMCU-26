package com.yisy.modules.doramcu.service;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tracks the build status of a galaxy background generation task.
 * <p>
 * This DTO provides thread-safe access to build progress information,
 * allowing external components to query the current state of galaxy
 * sector generation without blocking the build thread.
 * </p>
 */
public class GalaxyBuildStatus {

    private final String galaxyName;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean completed = new AtomicBoolean(false);
    private final AtomicInteger completionPercent = new AtomicInteger(0);
    private final AtomicInteger systemsGenerated = new AtomicInteger(0);
    private volatile String phase = "Pending";
    private volatile String errorMessage;
    private volatile long startTime;
    private volatile long endTime;

    public GalaxyBuildStatus(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    /**
     * Returns the name of the galaxy being built.
     */
    public String getGalaxyName() {
        return galaxyName;
    }

    /**
     * Returns whether the build is currently running.
     */
    public boolean isRunning() {
        return running.get();
    }

    void setRunning(boolean running) {
        this.running.set(running);
        if (running) {
            this.startTime = System.currentTimeMillis();
        } else {
            this.endTime = System.currentTimeMillis();
        }
    }

    /**
     * Returns whether the build has completed successfully.
     */
    public boolean isCompleted() {
        return completed.get();
    }

    void setCompleted(boolean completed) {
        this.completed.set(completed);
    }

    /**
     * Returns the current completion percentage (0-100).
     */
    public int getCompletionPercent() {
        return completionPercent.get();
    }

    void setCompletionPercent(int percent) {
        this.completionPercent.set(Math.max(0, Math.min(100, percent)));
    }

    /**
     * Returns the number of systems generated during this build.
     */
    public int getSystemsGenerated() {
        return systemsGenerated.get();
    }

    void setSystemsGenerated(int count) {
        this.systemsGenerated.set(count);
    }

    /**
     * Returns the current phase of the build (e.g., "Generating remaining sectors...").
     */
    public String getPhase() {
        return phase;
    }

    void setPhase(String phase) {
        this.phase = phase;
    }

    /**
     * Returns the error message if the build failed, or null if no error.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    void setFailed(String message) {
        this.errorMessage = message;
        this.phase = "Failed: " + message;
    }

    /**
     * Returns whether the build has failed.
     */
    public boolean isFailed() {
        return errorMessage != null;
    }

    /**
     * Returns the start time in milliseconds since epoch, or 0 if not started.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Returns the end time in milliseconds since epoch, or 0 if not finished.
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * Returns the elapsed time in milliseconds since the build started.
     * If the build is still running, returns time since start.
     * If the build has finished, returns total duration.
     */
    public long getElapsedTimeMs() {
        if (startTime == 0) {
            return 0;
        }
        if (endTime > 0) {
            return endTime - startTime;
        }
        return System.currentTimeMillis() - startTime;
    }

    @Override
    public String toString() {
        return "GalaxyBuildStatus{" +
                "galaxyName='" + galaxyName + '\'' +
                ", running=" + running.get() +
                ", completed=" + completed.get() +
                ", completionPercent=" + completionPercent.get() +
                ", systemsGenerated=" + systemsGenerated.get() +
                ", phase='" + phase + '\'' +
                ", errorMessage=" + errorMessage +
                ", elapsedMs=" + getElapsedTimeMs() +
                '}';
    }
}

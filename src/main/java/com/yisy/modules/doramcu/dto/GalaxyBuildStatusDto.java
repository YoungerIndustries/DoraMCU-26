package com.yisy.modules.doramcu.dto;

/**
 * Response DTO for galaxy background build status.
 * <p>
 * Returned by the build status endpoint so clients can monitor
 * the progress of background galaxy sector generation.
 * </p>
 */
public class GalaxyBuildStatusDto {

    /**
     * Name of the galaxy being built.
     */
    private String galaxyName;

    /**
     * Whether the build is currently running.
     */
    private boolean running;

    /**
     * Whether the build has completed successfully.
     */
    private boolean completed;

    /**
     * Whether the build has failed.
     */
    private boolean failed;

    /**
     * Current completion percentage (0-100).
     */
    private int completionPercent;

    /**
     * Number of systems generated during this build.
     */
    private int systemsGenerated;

    /**
     * Current phase description (e.g., "Generating remaining sectors...").
     */
    private String phase;

    /**
     * Error message if the build failed, or null if no error.
     */
    private String errorMessage;

    /**
     * Start time in milliseconds since epoch, or 0 if not started.
     */
    private long startTime;

    /**
     * End time in milliseconds since epoch, or 0 if not finished.
     */
    private long endTime;

    /**
     * Elapsed time in milliseconds since the build started.
     */
    private long elapsedMs;

    /**
     * Default constructor for JSON deserialization.
     */
    public GalaxyBuildStatusDto() {
    }

    /**
     * Constructor with all fields.
     */
    public GalaxyBuildStatusDto(String galaxyName, boolean running, boolean completed,
                                 boolean failed, int completionPercent, int systemsGenerated,
                                 String phase, String errorMessage, long startTime,
                                 long endTime, long elapsedMs) {
        this.galaxyName = galaxyName;
        this.running = running;
        this.completed = completed;
        this.failed = failed;
        this.completionPercent = completionPercent;
        this.systemsGenerated = systemsGenerated;
        this.phase = phase;
        this.errorMessage = errorMessage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.elapsedMs = elapsedMs;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public void setGalaxyName(String galaxyName) {
        this.galaxyName = galaxyName;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public int getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(int completionPercent) {
        this.completionPercent = completionPercent;
    }

    public int getSystemsGenerated() {
        return systemsGenerated;
    }

    public void setSystemsGenerated(int systemsGenerated) {
        this.systemsGenerated = systemsGenerated;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }
}

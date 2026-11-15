package com.yisy.modules.doramcu.hex;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Represents the result of a completed build request.
 * <p>
 * The DoraMCU game module returns this object after processing a BuildRequest.
 * It contains the constructed object reference, any metadata, and error information
 * if the build failed.
 * </p>
 */
public class BuildResult {

    private final String requestId;
    private final BuildObjectType objectType;
    private final BuildStatus status;
    private final Object builtObject;
    private final String errorMessage;
    private final Instant completedAt;
    private final Duration buildDuration;
    private final Map<String, Object> metadata;

    private BuildResult(BuildResultBuilder builder) {
        this.requestId = builder.requestId;
        this.objectType = builder.objectType;
        this.status = builder.status;
        this.builtObject = builder.builtObject;
        this.errorMessage = builder.errorMessage;
        this.completedAt = builder.completedAt != null ? builder.completedAt : Instant.now();
        this.buildDuration = builder.buildDuration;
        this.metadata = builder.metadata != null ? Map.copyOf(builder.metadata) : Map.of();
    }

    /**
     * Creates a successful build result.
     */
    public static BuildResult success(String requestId, BuildObjectType objectType, Object builtObject) {
        return new BuildResultBuilder(requestId, objectType, BuildStatus.COMPLETED)
                .builtObject(builtObject)
                .build();
    }

    /**
     * Creates a successful build result with metadata.
     */
    public static BuildResult success(String requestId, BuildObjectType objectType, Object builtObject,
                                       Map<String, Object> metadata) {
        return new BuildResultBuilder(requestId, objectType, BuildStatus.COMPLETED)
                .builtObject(builtObject)
                .metadata(metadata)
                .build();
    }

    /**
     * Creates a failed build result.
     */
    public static BuildResult failure(String requestId, BuildObjectType objectType, String errorMessage) {
        return new BuildResultBuilder(requestId, objectType, BuildStatus.FAILED)
                .errorMessage(errorMessage)
                .build();
    }

    public String getRequestId() {
        return requestId;
    }

    public BuildObjectType getObjectType() {
        return objectType;
    }

    public BuildStatus getStatus() {
        return status;
    }

    public Object getBuiltObject() {
        return builtObject;
    }

    /**
     * Returns the built object cast to the specified type.
     */
    public <T> T getBuiltObject(Class<T> type) {
        return type.cast(builtObject);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Duration getBuildDuration() {
        return buildDuration;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public boolean isSuccess() {
        return status == BuildStatus.COMPLETED;
    }

    public boolean isFailure() {
        return status == BuildStatus.FAILED;
    }

    @Override
    public String toString() {
        return "BuildResult{request='" + requestId + "', type=" + objectType +
                ", status=" + status + (errorMessage != null ? ", error='" + errorMessage + "'" : "") + "}";
    }

    /**
     * Builder for constructing BuildResult instances.
     */
    public static class BuildResultBuilder {
        private final String requestId;
        private final BuildObjectType objectType;
        private final BuildStatus status;
        private Object builtObject;
        private String errorMessage;
        private Instant completedAt;
        private Duration buildDuration;
        private java.util.Map<String, Object> metadata;

        BuildResultBuilder(String requestId, BuildObjectType objectType, BuildStatus status) {
            this.requestId = requestId;
            this.objectType = objectType;
            this.status = status;
        }

        public BuildResultBuilder builtObject(Object builtObject) {
            this.builtObject = builtObject;
            return this;
        }

        public BuildResultBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public BuildResultBuilder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public BuildResultBuilder buildDuration(Duration buildDuration) {
            this.buildDuration = buildDuration;
            return this;
        }

        public BuildResultBuilder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public BuildResult build() {
            return new BuildResult(this);
        }
    }
}

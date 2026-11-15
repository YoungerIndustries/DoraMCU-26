package com.yisy.modules.doramcu.hex;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a single build request submitted by the Galaxy Overlord to the
 * DoraMCU game module interface.
 * <p>
 * The Galaxy Overlord maps the entire galaxy first, then creates prioritized
 * build requests for every object needed. Requests are processed one at a time
 * by the build queue, with planets built sequentially (one at a time) to avoid
 * resource contention.
 * </p>
 * <p>
 * Each request specifies:
 * <ul>
 *   <li>The type of object to build (planet, sector, city, etc.)</li>
 *   <li>The priority level (Earth hub first, then outward)</li>
 *   <li>Parameters needed for construction (coordinates, names, etc.)</li>
 *   <li>Dependencies on other build requests</li>
 * </ul>
 * </p>
 */
public class BuildRequest {

    private final String requestId;
    private final BuildObjectType objectType;
    private final GalaxyBuildPriority priority;
    private final String galaxyName;
    private final Map<String, Object> parameters;
    private final Set<String> dependencies;
    private final Instant requestedAt;
    private BuildStatus status;

    /**
     * Creates a new build request.
     *
     * @param objectType the type of object to build
     * @param priority the build priority (determines processing order)
     * @param galaxyName the target galaxy name
     * @param parameters construction parameters (coordinates, names, config)
     */
    public BuildRequest(BuildObjectType objectType, GalaxyBuildPriority priority,
                        String galaxyName, Map<String, Object> parameters) {
        this(objectType, priority, galaxyName, parameters, new HashSet<>());
    }

    /**
     * Creates a new build request with dependencies.
     *
     * @param objectType the type of object to build
     * @param priority the build priority
     * @param galaxyName the target galaxy name
     * @param parameters construction parameters
     * @param dependencies request IDs that must complete before this request
     */
    public BuildRequest(BuildObjectType objectType, GalaxyBuildPriority priority,
                        String galaxyName, Map<String, Object> parameters,
                        Set<String> dependencies) {
        this.requestId = UUID.randomUUID().toString();
        this.objectType = objectType;
        this.priority = priority;
        this.galaxyName = galaxyName;
        this.parameters = parameters != null ? new HashMap<>(parameters) : new HashMap<>();
        this.dependencies = dependencies != null ? new HashSet<>(dependencies) : new HashSet<>();
        this.requestedAt = Instant.now();
        this.status = BuildStatus.PENDING;
    }

    public String getRequestId() {
        return requestId;
    }

    public BuildObjectType getObjectType() {
        return objectType;
    }

    public GalaxyBuildPriority getPriority() {
        return priority;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Returns a parameter value by key.
     */
    public <T> T getParameter(String key, Class<T> type) {
        Object value = parameters.get(key);
        if (value == null) return null;
        return type.cast(value);
    }

    /**
     * Returns a parameter value by key, with a default fallback.
     */
    public <T> T getParameter(String key, Class<T> type, T defaultValue) {
        T value = getParameter(key, type);
        return value != null ? value : defaultValue;
    }

    public Set<String> getDependencies() {
        return Collections.unmodifiableSet(dependencies);
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public BuildStatus getStatus() {
        return status;
    }

    void setStatus(BuildStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "BuildRequest{id='" + requestId + "', type=" + objectType +
                ", priority=" + priority + ", galaxy='" + galaxyName +
                "', status=" + status + ", deps=" + dependencies.size() + "}";
    }
}

package com.yisy.modules.doramcu.hex;

import java.util.List;
import java.util.Map;

/**
 * Interface between the Galaxy Overlord and the DoraMCU game module for
 * queued construction of galaxy objects.
 * <p>
 * The Galaxy Overlord maps the entire galaxy first, then sends a prioritized
 * list of build requests through this interface. The DoraMCU module processes
 * each request and returns the finished object. This allows queued construction
 * so the system doesn't get bogged down by trying to build everything at once.
 * </p>
 * <p>
 * Key design principles:
 * <ul>
 *   <li>Galaxy Overlord maps the galaxy, then submits build requests</li>
 *   <li>DoraMCU module processes requests one at a time in priority order</li>
 *   <li>Planets are built sequentially (one at a time)</li>
 *   <li>Largest objects (planets, sectors) are built first in each priority tier</li>
 *   <li>Galaxy Overlord can request entire cities for 3D adventuring</li>
 * </ul>
 * </p>
 */
public interface GalaxyModuleInterface {

    /**
     * Functional interface for handling build requests.
     * <p>
     * Implementations construct the requested object and return a BuildResult.
     * This is the core interface between the Galaxy Overlord and the DoraMCU
     * game module — the Overlord sends requests, the module builds and returns
     * the finished product.
     * </p>
     */
    @FunctionalInterface
    interface BuildHandler {
        /**
         * Builds the requested object.
         *
         * @param request the build request specifying what to build
         * @return the build result with the constructed object or error
         */
        BuildResult build(BuildRequest request);
    }

    /**
     * Submits a build request to the DoraMCU module.
     * <p>
     * The Galaxy Overlord calls this method after mapping the galaxy to request
     * construction of specific objects. Requests are queued and processed in
     * priority order.
     * </p>
     *
     * @param request the build request
     */
    void submitBuildRequest(BuildRequest request);

    /**
     * Submits multiple build requests to the DoraMCU module.
     *
     * @param requests the build requests to submit
     */
    void submitBuildRequests(List<BuildRequest> requests);

    /**
     * Returns the current queue statistics.
     *
     * @return map of queue statistics
     */
    Map<String, Object> getQueueStats();

    /**
     * Returns the result of a completed build request.
     *
     * @param requestId the request ID
     * @return the build result, or null if not yet completed
     */
    BuildResult getBuildResult(String requestId);

    /**
     * Cancels all pending build requests.
     */
    void cancelAll();

    /**
     * Checks if the build queue is currently processing.
     *
     * @return true if processing
     */
    boolean isProcessing();

    /**
     * Waits for all build requests to complete.
     *
     * @param timeoutSeconds maximum time to wait
     * @return true if all requests completed, false if timeout
     */
    boolean waitForCompletion(long timeoutSeconds);

    /**
     * Requests the construction of an entire city for 3D adventuring.
     * <p>
     * This is a convenience method that creates a CITY_3D build request
     * with the specified parameters. The city will include buildings,
     * roads, and points of interest suitable for 3D exploration.
     * </p>
     *
     * @param cityName the name of the city
     * @param planetName the planet where the city is located
     * @param galaxyName the galaxy name
     * @param priority the build priority
     * @param parameters additional parameters (population, district count, etc.)
     * @return the build request ID for tracking
     */
    String requestCity3D(String cityName, String planetName, String galaxyName,
                          GalaxyBuildPriority priority, Map<String, Object> parameters);

    /**
     * Requests the construction of a planet.
     * <p>
     * Planets are built sequentially (one at a time) to avoid resource contention.
     * This is the largest single object type in the build queue.
     * </p>
     *
     * @param planetName the name of the planet
     * @param galaxyName the galaxy name
     * @param priority the build priority
     * @param parameters planet parameters (radius, atmosphere, etc.)
     * @return the build request ID for tracking
     */
    String requestPlanet(String planetName, String galaxyName,
                          GalaxyBuildPriority priority, Map<String, Object> parameters);

    /**
     * Requests the construction of a sector.
     *
     * @param sectorRow the sector row coordinate
     * @param sectorCol the sector column coordinate
     * @param galaxyName the galaxy name
     * @param parameters sector parameters
     * @return the build request ID for tracking
     */
    String requestSector(int sectorRow, int sectorCol, String galaxyName,
                          Map<String, Object> parameters);
}

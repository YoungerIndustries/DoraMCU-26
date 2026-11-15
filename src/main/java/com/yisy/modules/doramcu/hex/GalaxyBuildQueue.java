package com.yisy.modules.doramcu.hex;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Priority-based queue for galaxy construction build requests.
 * <p>
 * The Galaxy Overlord submits build requests to this queue after mapping the
 * entire galaxy. Requests are processed in priority order (EARTH_HUB first,
 * then CORE_SYSTEMS, SPIRAL_ARMS, FRONTIER), with planets built one at a time
 * to avoid resource contention.
 * </p>
 * <p>
 * The queue supports:
 * <ul>
 *   <li>Priority-based ordering of build requests</li>
 *   <li>Dependency tracking (a request waits until its dependencies complete)</li>
 *   <li>Sequential planet building (only one planet build at a time)</li>
 *   <li>Progress callbacks for real-time status updates</li>
 *   <li>Cancellation of all pending requests</li>
 * </ul>
 * </p>
 */
public class GalaxyBuildQueue {

    private static final Logger log = LoggerFactory.getLogger(GalaxyBuildQueue.class);

    /**
     * Comparator that orders build requests by priority (highest first),
     * then by request time (oldest first).
     */
    private static final Comparator<BuildRequest> PRIORITY_COMPARATOR =
            Comparator.<BuildRequest, GalaxyBuildPriority>comparing(BuildRequest::getPriority)
                    .thenComparing(BuildRequest::getRequestedAt);

    /**
     * Callback interface for build progress notifications.
     */
    public interface BuildProgressListener {
        void onBuildStarted(BuildRequest request);
        void onBuildCompleted(BuildResult result);
        void onBuildFailed(BuildResult result);
        void onQueueEmpty();
        void onQueueCancelled();
    }

    private final PriorityQueue<BuildRequest> readyQueue;
    private final Map<String, BuildRequest> pendingRequests;
    private final Map<String, BuildResult> completedResults;
    private final Set<String> completedRequestIds;
    private final List<BuildProgressListener> listeners;
    private final AtomicBoolean running;
    private final AtomicInteger totalSubmitted;
    private final AtomicInteger totalCompleted;
    private final AtomicInteger totalFailed;
    private final ExecutorService executor;

    public GalaxyBuildQueue() {
        this(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "GalaxyBuildQueue-Worker");
            t.setDaemon(true);
            return t;
        }));
    }

    GalaxyBuildQueue(ExecutorService executor) {
        this.readyQueue = new PriorityQueue<>(PRIORITY_COMPARATOR);
        this.pendingRequests = new ConcurrentHashMap<>();
        this.completedResults = new ConcurrentHashMap<>();
        this.completedRequestIds = ConcurrentHashMap.newKeySet();
        this.listeners = new CopyOnWriteArrayList<>();
        this.running = new AtomicBoolean(false);
        this.totalSubmitted = new AtomicInteger(0);
        this.totalCompleted = new AtomicInteger(0);
        this.totalFailed = new AtomicInteger(0);
        this.executor = executor;
    }

    /**
     * Adds a build request to the queue.
     * <p>
     * If all dependencies are already completed, the request is moved to the
     * ready queue immediately. Otherwise it stays pending until dependencies
     * are satisfied.
     * </p>
     *
     * @param request the build request to add
     */
    public void submit(BuildRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Build request must not be null");
        }
        pendingRequests.put(request.getRequestId(), request);
        totalSubmitted.incrementAndGet();

        // Check if dependencies are satisfied
        if (areDependenciesMet(request)) {
            request.setStatus(BuildStatus.READY);
            readyQueue.offer(request);
            log.debug("Build request {} submitted and ready: {}", request.getRequestId(), request.getObjectType());
        } else {
            log.debug("Build request {} submitted, waiting on {} dependencies",
                    request.getRequestId(), request.getDependencies().size());
        }

        log.info("Build queue: {} total submitted, {} ready, {} pending",
                totalSubmitted.get(), readyQueue.size(), pendingRequests.size());
    }

    /**
     * Adds multiple build requests to the queue.
     */
    public void submitAll(Collection<BuildRequest> requests) {
        for (BuildRequest request : requests) {
            submit(request);
        }
    }

    /**
     * Returns the next ready build request from the queue (highest priority first).
     *
     * @return the next build request, or null if queue is empty
     */
    public BuildRequest pollNext() {
        BuildRequest request = readyQueue.poll();
        if (request != null) {
            request.setStatus(BuildStatus.IN_PROGRESS);
        }
        return request;
    }

    /**
     * Marks a build request as completed and checks if any pending requests
     * can now be moved to the ready queue.
     *
     * @param result the build result
     */
    public void complete(BuildResult result) {
        String requestId = result.getRequestId();
        completedResults.put(requestId, result);
        completedRequestIds.add(requestId);

        BuildRequest request = pendingRequests.get(requestId);
        if (request != null) {
            request.setStatus(result.getStatus());
        }

        if (result.isSuccess()) {
            totalCompleted.incrementAndGet();
        } else {
            totalFailed.incrementAndGet();
        }

        // Check pending requests for newly satisfied dependencies
        checkPendingRequests();

        // Notify listeners
        for (BuildProgressListener listener : listeners) {
            if (result.isSuccess()) {
                listener.onBuildCompleted(result);
            } else {
                listener.onBuildFailed(result);
            }
        }

        log.info("Build completed: {} ({} succeeded, {} failed, {} remaining)",
                result.getObjectType(), totalCompleted.get(), totalFailed.get(), remainingCount());
    }

    /**
     * Checks all pending requests and moves those with satisfied dependencies
     * to the ready queue.
     */
    private void checkPendingRequests() {
        List<BuildRequest> nowReady = new ArrayList<>();
        for (BuildRequest request : pendingRequests.values()) {
            if (request.getStatus() == BuildStatus.PENDING && areDependenciesMet(request)) {
                request.setStatus(BuildStatus.READY);
                nowReady.add(request);
            }
        }
        for (BuildRequest request : nowReady) {
            readyQueue.offer(request);
            log.debug("Build request {} dependencies met, moved to ready queue", request.getRequestId());
        }
    }

    /**
     * Checks if all dependencies of a request have been completed.
     */
    private boolean areDependenciesMet(BuildRequest request) {
        if (request.getDependencies().isEmpty()) {
            return true;
        }
        return request.getDependencies().stream()
                .allMatch(completedRequestIds::contains);
    }

    /**
     * Starts processing the queue using the provided build handler.
     * <p>
     * The handler is called for each ready request. Planets are built
     * sequentially (one at a time) to avoid resource contention.
     * </p>
     *
     * @param handler the function that processes a build request and returns a result
     */
    public void startProcessing(GalaxyModuleInterface.BuildHandler handler) {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Build queue is already running");
        }

        executor.submit(() -> {
            log.info("Build queue processing started");
            while (running.get() && !readyQueue.isEmpty()) {
                BuildRequest request = pollNext();
                if (request == null) {
                    break;
                }

                // Notify listeners
                for (BuildProgressListener listener : listeners) {
                    listener.onBuildStarted(request);
                }

                try {
                    BuildResult result = handler.build(request);
                    complete(result);
                } catch (Exception e) {
                    log.error("Build failed for request {}: {}", request.getRequestId(), e.getMessage(), e);
                    BuildResult failure = BuildResult.failure(
                            request.getRequestId(), request.getObjectType(),
                            "Build exception: " + e.getMessage());
                    complete(failure);
                }
            }

            if (readyQueue.isEmpty() && running.get()) {
                for (BuildProgressListener listener : listeners) {
                    listener.onQueueEmpty();
                }
                log.info("Build queue processing completed: {} total, {} succeeded, {} failed",
                        totalSubmitted.get(), totalCompleted.get(), totalFailed.get());
            }
            running.set(false);
        });
    }

    /**
     * Cancels all pending and ready requests.
     */
    public void cancelAll() {
        running.set(false);
        for (BuildRequest request : pendingRequests.values()) {
            if (request.getStatus() == BuildStatus.PENDING || request.getStatus() == BuildStatus.READY) {
                request.setStatus(BuildStatus.CANCELLED);
            }
        }
        readyQueue.clear();
        for (BuildProgressListener listener : listeners) {
            listener.onQueueCancelled();
        }
        log.info("Build queue cancelled: {} requests cancelled", totalSubmitted.get());
    }

    /**
     * Registers a progress listener.
     */
    public void addListener(BuildProgressListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a progress listener.
     */
    public void removeListener(BuildProgressListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns the number of requests currently in the ready queue.
     */
    public int getReadyCount() {
        return readyQueue.size();
    }

    /**
     * Returns the number of pending requests waiting on dependencies.
     */
    public int getPendingCount() {
        return (int) pendingRequests.values().stream()
                .filter(r -> r.getStatus() == BuildStatus.PENDING)
                .count();
    }

    /**
     * Returns the number of requests still remaining (not completed or failed).
     */
    public int remainingCount() {
        return totalSubmitted.get() - totalCompleted.get() - totalFailed.get();
    }

    /**
     * Returns the total number of submitted requests.
     */
    public int getTotalSubmitted() {
        return totalSubmitted.get();
    }

    /**
     * Returns the total number of completed requests.
     */
    public int getTotalCompleted() {
        return totalCompleted.get();
    }

    /**
     * Returns the total number of failed requests.
     */
    public int getTotalFailed() {
        return totalFailed.get();
    }

    /**
     * Returns whether the queue is currently processing.
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Returns all completed build results.
     */
    public Map<String, BuildResult> getCompletedResults() {
        return Collections.unmodifiableMap(completedResults);
    }

    /**
     * Returns the result for a specific request, if completed.
     */
    public Optional<BuildResult> getResult(String requestId) {
        return Optional.ofNullable(completedResults.get(requestId));
    }

    /**
     * Returns queue statistics as a map.
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSubmitted", totalSubmitted.get());
        stats.put("totalCompleted", totalCompleted.get());
        stats.put("totalFailed", totalFailed.get());
        stats.put("readyCount", readyQueue.size());
        stats.put("pendingCount", getPendingCount());
        stats.put("remaining", remainingCount());
        stats.put("running", running.get());
        return stats;
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        running.set(false);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

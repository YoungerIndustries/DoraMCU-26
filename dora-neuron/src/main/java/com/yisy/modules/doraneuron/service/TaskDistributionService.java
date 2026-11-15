package com.yisy.modules.doraneuron.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.protocol.mesh.AiTaskMessage;
import com.yisy.modules.doraneuron.service.mesh.NeuronMeshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.Comparator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Distributes AI inference tasks to connected neurons via the P2P mesh.
 * 
 * The Base Node receives AI tasks and distributes them to available neurons
 * based on their capabilities and current load. Results are aggregated and
 * returned to the requester.
 */
@Service
@ConditionalOnProperty(prefix = "neuron", name = "standalone", havingValue = "true", matchIfMissing = false)

public class TaskDistributionService {

    private static final Logger log = LoggerFactory.getLogger(TaskDistributionService.class);

    private final NeuronConfig config;
    private final NeuronMeshService meshService;

    /** Queue of pending tasks waiting for a neuron. */
    private final LinkedBlockingQueue<TaskRequest> taskQueue = new LinkedBlockingQueue<>();
    
    /** Map of task ID to future for result collection. */
    private final ConcurrentHashMap<String, CompletableFuture<Map<String, Object>>> pendingTasks = new ConcurrentHashMap<>();
    
    /** Task counter for unique IDs. */
    private final AtomicLong taskCounter = new AtomicLong(0);
    
    /** Executor for task timeout handling. */
    private final ScheduledExecutorService timeoutExecutor = Executors.newScheduledThreadPool(2);
    
    /** Total tasks distributed. */
    private final AtomicLong totalDistributed = new AtomicLong(0);
    
    /** Total tasks completed. */
    private final AtomicLong totalCompleted = new AtomicLong(0);

    public TaskDistributionService(NeuronConfig config, NeuronMeshService meshService) {
        this.config = config;
        this.meshService = meshService;
    }

    @PostConstruct
    public void init() {
        log.info("Task distribution service initialized for node: {}", config.getNodeId());
        
        // Start task processing thread
        Thread processorThread = new Thread(this::processTaskQueue, "task-processor");
        processorThread.setDaemon(true);
        processorThread.start();
    }

    /**
     * Submit an AI task for distribution to connected neurons.
     * 
     * @param taskType the type of AI task (e.g., "inference", "classification")
     * @param input the input data for the task
     * @param timeoutMs timeout in milliseconds
     * @return future that completes with the task result
     */
    public CompletableFuture<Map<String, Object>> submitTask(String taskType, Map<String, Object> input, long timeoutMs) {
        String taskId = "task-" + config.getNodeId() + "-" + taskCounter.incrementAndGet();
        
        TaskRequest task = new TaskRequest(taskId, taskType, input, timeoutMs);
        taskQueue.offer(task);
        
        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        pendingTasks.put(taskId, future);
        
        // Set timeout
        timeoutExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.completeExceptionally(new TimeoutException("Task " + taskId + " timed out after " + timeoutMs + "ms"));
                pendingTasks.remove(taskId);
                log.warn("Task {} timed out", taskId);
            }
        }, timeoutMs, TimeUnit.MILLISECONDS);
        
        log.debug("Task {} submitted: {}", taskId, taskType);
        return future;
    }

    /**
     * Process the task queue, distributing tasks to available neurons.
     */
    private void processTaskQueue() {
        while (true) {
            try {
                TaskRequest task = taskQueue.take();
                distributeTask(task);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.error("Error processing task queue: {}", e.getMessage());
            }
        }
    }

    /**
     * Distribute a task to an available neuron using capability-aware weighted selection.
     * Peers are selected based on their capability score (CPU cores, memory, GPU, queue depth, latency).
     */
    private void distributeTask(TaskRequest task) {
        // Find available neurons
        List<String> availablePeers = meshService.getConnectedPeerIds();

        if (availablePeers.isEmpty()) {
            // No peers available — if this is co-server, handle locally
            log.warn("No available peers for task {}", task.taskId);
            CompletableFuture<Map<String, Object>> future = pendingTasks.get(task.taskId);
            if (future != null && !future.isDone()) {
                future.complete(Map.of(
                        "taskId", task.taskId,
                        "status", "no_peers_available",
                        "message", "No neurons available for task distribution"
                ));
            }
            return;
        }

        // Select a peer using capability-aware weighted selection
        String selectedPeer = selectBestPeer(availablePeers);

        // Create task message
        AiTaskMessage taskMessage = new AiTaskMessage(
                task.taskId, task.taskType, task.input, task.timeoutMs);
        taskMessage.setFromNodeId(config.getNodeId());

        // Send to selected peer
        meshService.sendMessage(selectedPeer, taskMessage);
        totalDistributed.incrementAndGet();

        log.debug("Task {} distributed to peer {} (capability-aware selection)", task.taskId, selectedPeer);
    }

    /**
     * Select the best peer using weighted random selection based on capability scores.
     * Higher capability score = higher probability of being selected.
     */
    private String selectBestPeer(List<String> peerIds) {
        List<Map.Entry<String, Double>> scoredPeers = new ArrayList<>();

        for (String peerId : peerIds) {
            com.yisy.modules.doraneuron.protocol.mesh.MeshPeerInfo peerInfo = meshService.getPeerInfo(peerId);
            if (peerInfo != null) {
                double score = peerInfo.getCapabilityScore();
                if (score > 0) {
                    scoredPeers.add(Map.entry(peerId, score));
                }
            }
        }

        // Fallback to round-robin if no peers have capability scores
        if (scoredPeers.isEmpty()) {
            String fallback = peerIds.get((int) (taskCounter.get() % peerIds.size()));
            log.debug("No capability scores available, falling back to round-robin: {}", fallback);
            return fallback;
        }

        // Weighted random selection: higher score = higher probability
        double totalScore = scoredPeers.stream().mapToDouble(Map.Entry::getValue).sum();
        double randomValue = ThreadLocalRandom.current().nextDouble(0, totalScore);
        double cumulativeScore = 0;

        for (Map.Entry<String, Double> entry : scoredPeers) {
            cumulativeScore += entry.getValue();
            if (randomValue <= cumulativeScore) {
                return entry.getKey();
            }
        }

        // Fallback to highest score peer
        String bestPeer = scoredPeers.stream()
                .max(Map.Entry.comparingByValue(Comparator.naturalOrder()))
                .map(Map.Entry::getKey)
                .orElse(peerIds.get(0));
        log.debug("Selected best peer by capability score: {}", bestPeer);
        return bestPeer;
    }

    /**
     * Handle a task result from a peer.
     */
    public void handleTaskResult(String taskId, Map<String, Object> result) {
        CompletableFuture<Map<String, Object>> future = pendingTasks.remove(taskId);
        if (future != null && !future.isDone()) {
            future.complete(result);
            totalCompleted.incrementAndGet();
            log.debug("Task {} completed", taskId);
        }
    }

    /**
     * Get the current task statistics.
     */
    public Map<String, Object> getTaskStats() {
        return Map.of(
                "totalDistributed", totalDistributed.get(),
                "totalCompleted", totalCompleted.get(),
                "pendingTasks", pendingTasks.size(),
                "queuedTasks", taskQueue.size(),
                "connectedPeers", meshService.getConnectedPeerCount()
        );
    }

    /**
     * Represents a task request.
     */
    private static class TaskRequest {
        final String taskId;
        final String taskType;
        final Map<String, Object> input;
        final long timeoutMs;

        TaskRequest(String taskId, String taskType, Map<String, Object> input, long timeoutMs) {
            this.taskId = taskId;
            this.taskType = taskType;
            this.input = input;
            this.timeoutMs = timeoutMs;
        }
    }
}

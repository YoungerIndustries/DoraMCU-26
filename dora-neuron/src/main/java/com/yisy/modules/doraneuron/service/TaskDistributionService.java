package com.yisy.modules.doraneuron.service;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.protocol.mesh.AiTaskMessage;
import com.yisy.modules.doraneuron.service.mesh.NeuronMeshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Distributes AI inference tasks to connected neurons via the P2P mesh.
 * 
 * The Base Node receives AI tasks and distributes them to available neurons
 * based on their capabilities and current load. Results are aggregated and
 * returned to the requester.
 */
@Service
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
     * Distribute a task to an available neuron.
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

        // Select a peer (round-robin for now)
        String selectedPeer = availablePeers.get((int) (taskCounter.get() % availablePeers.size()));
        
        // Create task message
        AiTaskMessage taskMessage = new AiTaskMessage(
                task.taskId, task.taskType, task.input, task.timeoutMs);
        taskMessage.setFromNodeId(config.getNodeId());

        // Send to selected peer
        meshService.sendMessage(selectedPeer, taskMessage);
        totalDistributed.incrementAndGet();
        
        log.debug("Task {} distributed to peer {}", task.taskId, selectedPeer);
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

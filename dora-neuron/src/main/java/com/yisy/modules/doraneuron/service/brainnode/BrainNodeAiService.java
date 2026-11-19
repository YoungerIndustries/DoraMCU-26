package com.yisy.modules.doraneuron.service.brainnode;

import com.yisy.modules.doraneuron.config.BrainNodeConfig;
import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.NeuronDecision;
import com.yisy.modules.doraneuron.util.InferenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Enhanced AI inference service for the Brain Node.
 * <p>
 * Extends the base SatelliteAiService with support for larger ONNX models,
 * multi-threaded inference, GPU acceleration, model versioning, and A/B testing.
 */
@Service
@ConditionalOnBean({BrainNodeConfig.class, InferenceEngine.class})
@EnableConfigurationProperties({BrainNodeConfig.class, NeuronConfig.class})
public class BrainNodeAiService {

    private static final Logger log = LoggerFactory.getLogger(BrainNodeAiService.class);

    private final InferenceEngine engine;
    private final BrainNodeConfig brainNodeConfig;
    private final NeuronConfig neuronConfig;
    private final NeuronPrometheusMetrics prometheusMetrics;

    /** Thread pool for concurrent inference requests */
    private ExecutorService inferenceExecutor;

    /** Current model version */
    private volatile long currentModelVersion;

    /** Inference request counter */
    private final AtomicLong inferenceCount = new AtomicLong(0);

    /** Inference error counter */
    private final AtomicLong errorCount = new AtomicLong(0);

    /** Model A/B test configuration */
    private final Map<String, String> modelVariants = new ConcurrentHashMap<>();

    public BrainNodeAiService(InferenceEngine engine,
                              BrainNodeConfig brainNodeConfig,
                              NeuronConfig neuronConfig,
                              NeuronPrometheusMetrics prometheusMetrics) {
        this.engine = engine;
        this.brainNodeConfig = brainNodeConfig;
        this.neuronConfig = neuronConfig;
        this.prometheusMetrics = prometheusMetrics;
        this.currentModelVersion = 0;
    }

    @PostConstruct
    public void init() {
        log.info("Initializing BrainNodeAiService with {} inference threads", brainNodeConfig.getInferenceThreads());

        // Create thread pool for concurrent inference
        inferenceExecutor = new ThreadPoolExecutor(
                brainNodeConfig.getInferenceThreads(),
                brainNodeConfig.getInferenceThreads(),
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadFactory() {
                    private final AtomicLong threadNumber = new AtomicLong(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "brain-node-inference-" + threadNumber.incrementAndGet());
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        // Load the initial model
        loadInitialModel();

        log.info("BrainNodeAiService initialized with GPU acceleration: {}",
                brainNodeConfig.isGpuAccelerationEnabled());
    }

    @PreDestroy
    public void destroy() {
        log.info("Shutting down BrainNodeAiService");
        if (inferenceExecutor != null && !inferenceExecutor.isShutdown()) {
            inferenceExecutor.shutdown();
            try {
                if (!inferenceExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    inferenceExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                inferenceExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Load the initial ONNX model for the Brain Node.
     */
    private void loadInitialModel() {
        try {
            String modelPath = brainNodeConfig.getAiModelPath();
            log.info("Loading Brain Node ONNX model from: {}", modelPath);

            engine.reloadModel(modelPath);
            currentModelVersion = neuronConfig.getModelVersion();
            prometheusMetrics.setModelVersion(currentModelVersion);
            log.info("Brain Node model loaded successfully (version: {})", currentModelVersion);
        } catch (Exception e) {
            log.error("Error loading Brain Node model: {}", e.getMessage(), e);
        }
    }

    /**
     * Run inference on a game state feature vector using the Brain Node's enhanced model.
     *
     * @param features the game state feature vector
     * @return a decision containing selected action, confidence, and full probability distribution
     */
    public NeuronDecision predict(double[] features) {
        if (!engine.isModelLoaded()) {
            log.warn("No ONNX model loaded on Brain Node; returning empty decision");
            prometheusMetrics.recordInferenceError();
            errorCount.incrementAndGet();
            return new NeuronDecision();
        }

        long start = System.currentTimeMillis();
        try {
            float[] output = engine.runInference(features);
            if (output == null) {
                log.warn("Brain Node inference returned null output");
                prometheusMetrics.recordInferenceError();
                errorCount.incrementAndGet();
                return new NeuronDecision();
            }

            NeuronDecision decision = NeuronDecision.fromProbabilities(output, currentModelVersion);

            // Check confidence threshold (higher for Brain Node)
            double threshold = brainNodeConfig.getConfidenceThreshold();
            if (decision.getConfidence() < threshold) {
                log.debug("Low confidence Brain Node prediction: {} (threshold: {})",
                        decision.getConfidence(), threshold);
            }

            long duration = System.currentTimeMillis() - start;
            prometheusMetrics.recordInference(duration);
            inferenceCount.incrementAndGet();

            log.debug("Brain Node AI prediction: action={}, confidence={}, modelVersion={}, duration={}ms",
                    decision.getSelectedAction(), decision.getConfidence(), currentModelVersion, duration);
            return decision;
        } catch (Exception e) {
            log.error("Brain Node inference failed: {}", e.getMessage());
            prometheusMetrics.recordInferenceError();
            errorCount.incrementAndGet();
            return new NeuronDecision();
        }
    }

    /**
     * Run inference on a float[] feature vector.
     */
    public NeuronDecision predict(float[] features) {
        double[] doubleFeatures = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            doubleFeatures[i] = features[i];
        }
        return predict(doubleFeatures);
    }

    /**
     * Submit an async inference request for concurrent processing.
     *
     * @param features the game state feature vector
     * @return a Future containing the decision
     */
    public Future<NeuronDecision> predictAsync(double[] features) {
        return inferenceExecutor.submit(() -> predict(features));
    }

    /**
     * Run batch inference on multiple feature vectors.
     *
     * @param featuresList list of feature vectors
     * @return list of decisions
     */
    public NeuronDecision[] predictBatch(double[][] featuresList) {
        NeuronDecision[] decisions = new NeuronDecision[featuresList.length];
        for (int i = 0; i < featuresList.length; i++) {
            decisions[i] = predict(featuresList[i]);
        }
        return decisions;
    }

    /**
     * Swap to a new ONNX model without downtime.
     *
     * @param modelPath path to the new ONNX model file
     * @param version   the version number of the new model
     */
    public void swapModel(String modelPath, long version) {
        log.info("Brain Node swapping ONNX model: {} (version {})", modelPath, version);
        engine.reloadModel(modelPath);
        currentModelVersion = version;
        prometheusMetrics.setModelVersion(version);
        log.info("Brain Node model swap complete. Current version: {}", version);
    }

    /**
     * Register a model variant for A/B testing.
     *
     * @param variantName the name of the variant (e.g., "A", "B")
     * @param modelPath   the path to the model file
     */
    public void registerModelVariant(String variantName, String modelPath) {
        modelVariants.put(variantName, modelPath);
        log.info("Registered model variant: {} -> {}", variantName, modelPath);
    }

    /**
     * Get the model path for a specific variant.
     *
     * @param variantName the name of the variant
     * @return the model path or null if not found
     */
    public String getModelVariant(String variantName) {
        return modelVariants.get(variantName);
    }

    /**
     * Get all registered model variants.
     *
     * @return map of variant names to model paths
     */
    public Map<String, String> getModelVariants() {
        return Map.copyOf(modelVariants);
    }

    /**
     * Return the current model version.
     */
    public long getCurrentModelVersion() {
        return currentModelVersion;
    }

    /**
     * Check if the AI service is ready (model loaded and engine available).
     */
    public boolean isReady() {
        return engine.isModelLoaded();
    }

    /**
     * Get inference statistics.
     *
     * @return map of inference statistics
     */
    public Map<String, Object> getInferenceStats() {
        long total = inferenceCount.get();
        long errors = errorCount.get();
        double errorRate = total > 0 ? (double) errors / total : 0.0;

        return Map.of(
                "inferenceCount", total,
                "errorCount", errors,
                "errorRate", errorRate,
                "currentModelVersion", currentModelVersion,
                "inferenceThreads", brainNodeConfig.getInferenceThreads(),
                "confidenceThreshold", brainNodeConfig.getConfidenceThreshold(),
                "gpuAccelerationEnabled", brainNodeConfig.isGpuAccelerationEnabled(),
                "modelVariants", modelVariants.size(),
                "modelLoaded", engine.isModelLoaded()
        );
    }
}

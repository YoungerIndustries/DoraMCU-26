package com.yisy.modules.doraneuron.service;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.config.NeuronPrometheusMetrics;
import com.yisy.modules.doraneuron.model.NeuronDecision;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.protocol.ConsensusRequest;
import com.yisy.modules.doraneuron.protocol.ConsensusResult;
import com.yisy.modules.doraneuron.protocol.NeuronVote;
import com.yisy.modules.doraneuron.producer.NeuronEventProducer;
import com.yisy.modules.doraneuron.service.NeuronWebSocketClient.ConsensusCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Orchestrates distributed consensus voting across the Neuron network.
 *
 * This service implements two roles in the consensus protocol:
 *
 * <b>1. Consensus Requester</b> — When a high-stakes AI decision is needed,
 * this service submits a {@link ConsensusRequest} to the coordinator, which
 * broadcasts to all registered Neurons and collects weighted votes. The
 * coordinator aggregates the votes and returns a {@link ConsensusResult}.
 *
 * <b>2. Consensus Voter</b> — When the coordinator asks this Neuron to vote,
 * the {@code vote()} method runs local inference via {@link SatelliteAiService}
 * and returns a {@link NeuronVote} with this Neuron's decision and weight.
 *
 * The consensus mechanism mirrors biological brain decision-making: instead
 * of relying on a single neuron's output, the collective brain aggregates
 * weighted predictions from many neurons to produce a more robust decision.
 */
@Service
@ConditionalOnProperty(prefix = "neuron", name = "enable-consensus", havingValue = "true", matchIfMissing = true)
public class ConsensusService {

    private static final Logger log = LoggerFactory.getLogger(ConsensusService.class);

    private final NeuronConfig config;
    private final RestTemplate restTemplate;
    private final NeuronRegistryService registryService;

    private final SatelliteAiService satelliteAiService;
    private final NeuronWebSocketClient webSocketClient;
    private final NeuronPrometheusMetrics prometheusMetrics;
    private final NeuronEventProducer eventProducer; // null if Kafka not configured

    /** Cumulative count of consensus rounds requested by this Neuron. */
    private volatile long consensusRequested = 0;

    /** Cumulative count of votes cast by this Neuron. */
    private volatile long votesCast = 0;

    /** Last consensus result received (for health monitoring). */
    private volatile ConsensusResult lastConsensusResult;

    public ConsensusService(NeuronConfig config, RestTemplate restTemplate,
                            NeuronRegistryService registryService,
                            @Autowired(required = false) SatelliteAiService satelliteAiService,
                            NeuronWebSocketClient webSocketClient,
                            NeuronPrometheusMetrics prometheusMetrics,
                            @Autowired(required = false) NeuronEventProducer eventProducer) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.registryService = registryService;
        this.satelliteAiService = satelliteAiService;
        this.webSocketClient = webSocketClient;
        this.prometheusMetrics = prometheusMetrics;
        this.eventProducer = eventProducer;

        // Register for real-time consensus vote requests via WebSocket
        webSocketClient.addConsensusCallback(this::handleConsensusRequestWs);
    }

    /**
     * Handle a consensus vote request received via WebSocket from the coordinator.
     * Runs local inference and sends the vote response back.
     */
    public void handleConsensusRequestWs(String requestId, double[] features, long timeoutMs) {
        if (features == null || satelliteAiService == null) {
            log.warn("Cannot handle WebSocket consensus request: features or AI service unavailable");
            return;
        }

        NeuronIdentity identity = registryService.getIdentity();
        if (identity == null) {
            log.warn("Cannot handle WebSocket consensus request: identity not available");
            return;
        }

        NeuronDecision decision = satelliteAiService.predict(features);
        float[] probs = decision.getProbabilities();
        double[] doubleProbs = new double[probs.length];
        for (int i = 0; i < probs.length; i++) {
            doubleProbs[i] = probs[i];
        }
        webSocketClient.sendConsensusVote(
                requestId,
                doubleProbs,
                decision.getSelectedAction(),
                decision.getConfidence(),
                identity.getVoteWeight()
        );
        votesCast++;
        prometheusMetrics.recordConsensusVote();
        log.debug("Cast WebSocket consensus vote: {} (request: {})", decision.getSelectedAction(), requestId);
    }

    /**
     * Handle a consensus request received via Kafka from the coordinator.
     * Runs local inference and publishes the vote to Kafka.
     * This method is called by ConsensusRequestEventConsumer.
     *
     * @param features the game state feature vector
     * @param timeoutMs the timeout for the consensus
     * @param requestingRegion the region making the request (optional)
     */
    public void handleKafkaConsensusRequest(double[] features, long timeoutMs, String requestingRegion) {
        if (features == null || satelliteAiService == null) {
            log.warn("Cannot handle Kafka consensus request: features or AI service unavailable");
            return;
        }

        NeuronIdentity identity = registryService.getIdentity();
        if (identity == null) {
            log.warn("Cannot handle Kafka consensus request: identity not available");
            return;
        }

        NeuronDecision decision = satelliteAiService.predict(features);
        NeuronVote vote = new NeuronVote(
                identity.getNodeId(),
                decision,
                identity.getVoteWeight()
        );

        // Publish vote via Kafka if available
        if (eventProducer != null && eventProducer.isAvailable()) {
            eventProducer.publishVote(vote);
        }

        votesCast++;
        prometheusMetrics.recordConsensusVote();
        log.debug("Cast Kafka consensus vote: {} (region: {})", decision.getSelectedAction(), requestingRegion);
    }

    /**
     * Submit a consensus request to the coordinator.
     *
     * The coordinator broadcasts the request to all active Neurons, collects
     * weighted votes, aggregates them, and returns the consensus result.
     *
     * @param features the game state feature vector to evaluate
     * @return the aggregated consensus decision
     */
    public ConsensusResult requestConsensus(double[] features) {
        return requestConsensus(features, config.getConsensusTimeoutMs());
    }

    /**
     * Submit a consensus request to the coordinator with a custom timeout.
     *
     * @param features  the game state feature vector to evaluate
     * @param timeoutMs maximum time to wait for consensus (milliseconds)
     * @return the aggregated consensus decision
     */
    public ConsensusResult requestConsensus(double[] features, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        log.info("Requesting consensus for {}-dim features (timeout: {}ms)",
                features.length, timeoutMs);

        ConsensusRequest request = new ConsensusRequest(features, timeoutMs);
        String nodeId = registryService.getIdentity() != null
                ? registryService.getIdentity().getNodeId() : null;
        request.setRequestingNode(nodeId);

        try {
            String url = config.getCoordinatorUrl() + "/api/v1/neuron/consensus";
            ResponseEntity<ConsensusResult> response = restTemplate.postForEntity(
                    url, request, ConsensusResult.class);

            ConsensusResult result = response.getBody();
            if (result != null) {
                lastConsensusResult = result;
                consensusRequested++;
                long duration = System.currentTimeMillis() - startTime;
                prometheusMetrics.recordConsensusRequested();
                prometheusMetrics.recordConsensusDuration(duration);
                log.info("Consensus received: {} votes, total weight {}, duration {}ms",
                        result.getVoteCount(), result.getTotalWeight(), duration);
                return result;
            } else {
                log.warn("Coordinator returned null consensus result");
                prometheusMetrics.recordConsensusError();
                return createFallbackResult(features, startTime);
            }
        } catch (Exception e) {
            log.error("Consensus request failed: {}", e.getMessage());
            prometheusMetrics.recordConsensusError();
            return createFallbackResult(features, startTime);
        }
    }

    /**
     * Called by the coordinator when this Neuron is asked to vote.
     *
     * Runs local inference on the provided features and returns a vote
     * with this Neuron's decision and configured vote weight.
     *
     * @param request the consensus request containing features to evaluate
     * @return this Neuron's vote
     */
    public NeuronVote vote(ConsensusRequest request) {
        NeuronIdentity identity = registryService.getIdentity();
        if (identity == null) {
            log.warn("Cannot vote: identity not initialized");
            return null;
        }

        NeuronDecision decision;
        if (satelliteAiService != null) {
            decision = satelliteAiService.predict(request.getFeatures());
        } else {
            // Fallback: return empty decision if AI service is not available
            log.warn("Cannot vote: AI service not available");
            decision = new NeuronDecision();
        }

        NeuronVote vote = new NeuronVote(
                identity.getNodeId(),
                decision,
                identity.getVoteWeight()
        );
        votesCast++;
        prometheusMetrics.recordConsensusVote();
        log.debug("Cast vote: {} (weight: {})", vote.getDecision(), vote.getWeight());
        return vote;
    }

    /**
     * Run a local prediction without consensus — useful when consensus
     * is unavailable or for low-stakes decisions.
     *
     * @param features the game state feature vector
     * @return a local decision, or null if AI is not available
     */
    public NeuronDecision predictLocal(double[] features) {
        if (satelliteAiService != null) {
            return satelliteAiService.predict(features);
        } else {
            log.warn("Local prediction not available: AI service not configured");
            return null;
        }
    }

    /**
     * Create a fallback result when consensus fails.
     * If local AI is available, uses local prediction; otherwise returns empty result.
     */
    private ConsensusResult createFallbackResult(double[] features, long startTime) {
        NeuronDecision decision;
        if (satelliteAiService != null) {
            decision = satelliteAiService.predict(features);
        } else {
            decision = new NeuronDecision();
        }

        ConsensusResult result = new ConsensusResult(decision, java.util.Collections.emptyList(),
                System.currentTimeMillis() - startTime);
        lastConsensusResult = result;
        consensusRequested++;
        prometheusMetrics.recordConsensusRequested();
        log.warn("Returning fallback consensus result (local prediction only)");
        return result;
    }

    /**
     * Return the number of consensus rounds requested by this Neuron.
     */
    public long getConsensusRequested() {
        return consensusRequested;
    }

    /**
     * Return the number of votes cast by this Neuron.
     */
    public long getVotesCast() {
        return votesCast;
    }

    /**
     * Return the last consensus result received.
     */
    public ConsensusResult getLastConsensusResult() {
        return lastConsensusResult;
    }
}

package com.yisy.modules.doraneuron.service.mesh;

import com.yisy.modules.doraneuron.config.NeuronConfig;
import com.yisy.modules.doraneuron.model.NeuronDecision;
import com.yisy.modules.doraneuron.model.NeuronIdentity;
import com.yisy.modules.doraneuron.protocol.mesh.MeshConsensusRequest;
import com.yisy.modules.doraneuron.protocol.mesh.MeshConsensusResponse;
import com.yisy.modules.doraneuron.protocol.mesh.MeshMessage;
import com.yisy.modules.doraneuron.service.SatelliteAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements decentralized consensus via direct P2P mesh voting.
 *
 * This service allows a neuron to request consensus from its mesh peers
 * without involving the coordinator. The requesting neuron broadcasts
 * a MeshConsensusRequest to all peers, collects MeshConsensusResponse
 * votes, and aggregates them into a final decision.
 *
 * This provides resilience when the coordinator is unavailable and
 * reduces latency by eliminating the coordinator hop.
 */
@Service
@ConditionalOnProperty(prefix = "neuron", name = "mesh-consensus-enabled", havingValue = "true", matchIfMissing = true)
public class MeshConsensusService implements NeuronMeshService.MeshMessageCallback {

    private static final Logger log = LoggerFactory.getLogger(MeshConsensusService.class);

    private final NeuronConfig config;
    private final NeuronIdentity identity;
    private final NeuronMeshService meshService;
    private final SatelliteAiService satelliteAiService;

    /** Pending consensus requests waiting for responses. */
    private final ConcurrentHashMap<String, ConsensusPending> pendingRequests = new ConcurrentHashMap<>();

    /** Statistics tracking. */
    private final AtomicLong consensusRequested = new AtomicLong(0);
    private final AtomicLong consensusCompleted = new AtomicLong(0);
    private final AtomicLong consensusFailed = new AtomicLong(0);

    /** Last consensus result for monitoring. */
    private volatile NeuronDecision lastResult;

    public MeshConsensusService(NeuronConfig config, NeuronIdentity identity,
                                 NeuronMeshService meshService,
                                 @Autowired(required = false) SatelliteAiService satelliteAiService) {
        this.config = config;
        this.identity = identity;
        this.meshService = meshService;
        this.satelliteAiService = satelliteAiService;
    }

    @PostConstruct
    public void init() {
        meshService.addMessageCallback(this);
        log.info("MeshConsensusService initialized");
    }

    /**
     * Request decentralized consensus from mesh peers.
     *
     * @param features game state feature vector
     * @return aggregated consensus decision
     */
    public NeuronDecision requestConsensus(double[] features) {
        if (meshService.getPeerCount() == 0) {
            log.warn("No mesh peers available, falling back to local prediction");
            return localPrediction(features);
        }

        String requestId = UUID.randomUUID().toString();
        ConsensusPending pending = new ConsensusPending(features, requestId);
        pendingRequests.put(requestId, pending);

        // Broadcast consensus request to all peers
        MeshConsensusRequest request = new MeshConsensusRequest(
                identity.getNodeId(), features, config.getConsensusTimeoutMs());
        request.setMaxHops(config.getMeshMaxHops());
        meshService.broadcast(request);

        consensusRequested.incrementAndGet();
        log.info("Requested mesh consensus: {} ({} peers)", requestId, meshService.getPeerCount());

        // Wait for responses with timeout
        try {
            pending.latch.await(config.getConsensusTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Consensus request interrupted: {}", requestId);
            consensusFailed.incrementAndGet();
            return localPrediction(features);
        } finally {
            pendingRequests.remove(requestId);
        }

        // Aggregate responses
        if (pending.responses.isEmpty()) {
            log.warn("No consensus responses received, falling back to local prediction");
            consensusFailed.incrementAndGet();
            return localPrediction(features);
        }

        NeuronDecision result = aggregateVotes(pending.responses);
        lastResult = result;
        consensusCompleted.incrementAndGet();
        log.info("Mesh consensus completed: {} ({} votes, confidence: {})",
                requestId, pending.responses.size(), result.getConfidence());
        return result;
    }

    /**
     * Handle incoming mesh messages (callback from NeuronMeshService).
     */
    @Override
    public void onMeshMessage(MeshMessage message) {
        if (message instanceof MeshConsensusRequest) {
            handleConsensusRequest((MeshConsensusRequest) message);
        } else if (message instanceof MeshConsensusResponse) {
            handleConsensusResponse((MeshConsensusResponse) message);
        }
    }

    /**
     * Handle a consensus request from a peer.
     */
    private void handleConsensusRequest(MeshConsensusRequest request) {
        if (satelliteAiService == null) {
            log.warn("Cannot handle mesh consensus request: AI service unavailable");
            return;
        }

        // Run local inference
        NeuronDecision decision = satelliteAiService.predict(request.getFeatures());

        // Send response back
        MeshConsensusResponse response = new MeshConsensusResponse(
                identity.getNodeId(),
                request.getMessageId(),
                decision.getProbabilities(),
                decision.getSelectedAction(),
                decision.getConfidence(),
                identity.getVoteWeight()
        );
        response.setMaxHops(config.getMeshMaxHops());

        // Send directly to the requester
        Optional<com.yisy.modules.doraneuron.protocol.mesh.MeshPeerInfo> requesterPeer =
                meshService.getPeers().stream()
                        .filter(p -> p.getNodeId().equals(request.getFromNodeId()))
                        .findFirst();

        requesterPeer.ifPresent(peer -> meshService.sendToPeer(peer, response));
        log.debug("Responded to mesh consensus request: {}", request.getMessageId());
    }

    /**
     * Handle a consensus response from a peer.
     */
    private void handleConsensusResponse(MeshConsensusResponse response) {
        ConsensusPending pending = pendingRequests.get(response.getRequestId());
        if (pending != null) {
            pending.responses.add(response);
            pending.latch.countDown();
            log.debug("Received mesh consensus response: {} (from: {})",
                    response.getRequestId(), response.getFromNodeId());
        } else {
            log.warn("Received consensus response for unknown request: {}", response.getRequestId());
        }
    }

    /**
     * Aggregate weighted votes from all responses.
     */
    private NeuronDecision aggregateVotes(List<MeshConsensusResponse> responses) {
        if (responses.isEmpty()) {
            return null;
        }

        // Determine output size from first response
        int outputSize = responses.get(0).getProbabilities().length;
        double[] aggregated = new double[outputSize];
        double totalWeight = 0;

        for (MeshConsensusResponse response : responses) {
            double weight = response.getVoteWeight();
            float[] probs = response.getProbabilities();

            for (int i = 0; i < Math.min(aggregated.length, probs.length); i++) {
                aggregated[i] += probs[i] * weight;
            }
            totalWeight += weight;
        }

        // Normalize
        if (totalWeight > 0) {
            for (int i = 0; i < aggregated.length; i++) {
                aggregated[i] /= totalWeight;
            }
        }

        // Convert to float array
        float[] probabilities = new float[aggregated.length];
        for (int i = 0; i < aggregated.length; i++) {
            probabilities[i] = (float) aggregated[i];
        }

        // Find selected action (argmax)
        int selectedAction = 0;
        float maxConfidence = 0;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > maxConfidence) {
                maxConfidence = probabilities[i];
                selectedAction = i;
            }
        }

        return new NeuronDecision(probabilities, selectedAction, maxConfidence, -1);
    }

    /**
     * Fall back to local prediction when mesh consensus is not available.
     */
    private NeuronDecision localPrediction(double[] features) {
        if (satelliteAiService != null) {
            float[] floatFeatures = new float[features.length];
            for (int i = 0; i < features.length; i++) {
                floatFeatures[i] = (float) features[i];
            }
            return satelliteAiService.predict(floatFeatures);
        }
        log.error("Cannot make local prediction: AI service unavailable");
        return null;
    }

    /**
     * Pending consensus request waiting for responses.
     */
    private static class ConsensusPending {
        final double[] features;
        final String requestId;
        final CountDownLatch latch;
        final List<MeshConsensusResponse> responses;

        ConsensusPending(double[] features, String requestId) {
            this.features = features;
            this.requestId = requestId;
            this.latch = new CountDownLatch(1);
            this.responses = Collections.synchronizedList(new ArrayList<>());
        }
    }

    // ── Getters for monitoring ────────────────────────────────────────────────

    public long getConsensusRequested() {
        return consensusRequested.get();
    }

    public long getConsensusCompleted() {
        return consensusCompleted.get();
    }

    public long getConsensusFailed() {
        return consensusFailed.get();
    }

    public NeuronDecision getLastResult() {
        return lastResult;
    }
}

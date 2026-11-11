package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.model.NeuronDecision;
import com.yisy.modules.doraneuron.protocol.ConsensusRequest;
import com.yisy.modules.doraneuron.protocol.ConsensusResult;
import com.yisy.modules.doraneuron.protocol.NeuronVote;
import com.yisy.modules.doraneuron.service.ConsensusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST API controller for consensus voting operations.
 *
 * Exposes endpoints for:
 * <ul>
 *   <li>Requesting distributed consensus decisions</li>
 *   <li>Receiving vote requests from the coordinator</li>
 *   <li>Running local predictions without consensus</li>
 *   <li>Querying consensus statistics</li>
 * </ul>
 *
 * This controller is conditional on consensus being enabled via
 * {@code neuron.enable-consensus=true} (default).
 */
@RestController
@RequestMapping("/api/v1/neuron/consensus")
public class ConsensusController {

    private static final Logger log = LoggerFactory.getLogger(ConsensusController.class);

    private final ConsensusService consensusService;

    public ConsensusController(ConsensusService consensusService) {
        this.consensusService = consensusService;
    }

    /**
     * Request a distributed consensus decision from the Neuron network.
     *
     * The coordinator broadcasts the request to all active Neurons, collects
     * weighted votes, aggregates them, and returns the consensus result.
     *
     * @param request the consensus request containing the feature vector
     * @return the aggregated consensus result
     */
    @PostMapping
    public ResponseEntity<ConsensusResult> requestConsensus(@RequestBody ConsensusRequest request) {
        log.info("Consensus request received: {}-dim features", request.getFeatureDim());
        ConsensusResult result = consensusService.requestConsensus(request.getFeatures(), request.getTimeoutMs());
        return ResponseEntity.ok(result);
    }

    /**
     * Called by the coordinator when this Neuron is asked to vote.
     *
     * Runs local inference and returns this Neuron's vote.
     *
     * @param request the consensus request containing features to evaluate
     * @return this Neuron's vote
     */
    @PostMapping("/vote")
    public ResponseEntity<NeuronVote> vote(@RequestBody ConsensusRequest request) {
        log.debug("Vote request received from coordinator");
        NeuronVote vote = consensusService.vote(request);
        if (vote != null) {
            return ResponseEntity.ok(vote);
        } else {
            return ResponseEntity.status(503).build();
        }
    }

    /**
     * Run a local prediction without consensus — for low-stakes decisions.
     *
     * @param features the game state feature vector
     * @return the local AI decision
     */
    @PostMapping("/local")
    public ResponseEntity<NeuronDecision> predictLocal(@RequestBody double[] features) {
        NeuronDecision decision = consensusService.predictLocal(features);
        if (decision != null) {
            return ResponseEntity.ok(decision);
        } else {
            return ResponseEntity.status(503).build();
        }
    }

    /**
     * Query consensus statistics.
     *
     * @return statistics including consensus rounds requested, votes cast, and last result
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("consensusRequested", consensusService.getConsensusRequested());
        status.put("votesCast", consensusService.getVotesCast());

        ConsensusResult lastResult = consensusService.getLastConsensusResult();
        if (lastResult != null) {
            Map<String, Object> lastResultMap = new HashMap<>();
            lastResultMap.put("voteCount", lastResult.getVoteCount());
            lastResultMap.put("totalWeight", lastResult.getTotalWeight());
            lastResultMap.put("durationMs", lastResult.getDurationMs());
            if (lastResult.getDecision() != null) {
                lastResultMap.put("selectedAction", lastResult.getDecision().getSelectedAction());
                lastResultMap.put("confidence", lastResult.getDecision().getConfidence());
            }
            status.put("lastResult", lastResultMap);
        } else {
            status.put("lastResult", null);
        }

        return ResponseEntity.ok(status);
    }
}

package com.yisy.modules.doraneuron.controller.mesh;

import com.yisy.modules.doraneuron.protocol.mesh.MeshMessage;
import com.yisy.modules.doraneuron.protocol.mesh.MeshPeerInfo;
import com.yisy.modules.doraneuron.protocol.mesh.MeshTopologyUpdate;
import com.yisy.modules.doraneuron.service.mesh.MeshConsensusService;
import com.yisy.modules.doraneuron.service.mesh.MeshExperienceSyncService;
import com.yisy.modules.doraneuron.service.mesh.NeuronMeshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API for P2P mesh network management.
 *
 * This controller provides endpoints for:
 * <ul>
 *   <li>Peer registration and discovery</li>
 *   <li>Mesh message reception from peers</li>
 *   <li>Topology queries and updates</li>
 *   <li>Mesh status and statistics</li>
 * </ul>
 *
 * These endpoints are called by other neurons for P2P communication,
 * bypassing the coordinator for decentralized operations.
 */
@RestController
@RequestMapping("/api/v1/mesh")
@ConditionalOnProperty(prefix = "neuron", name = "mesh-enabled", havingValue = "true", matchIfMissing = true)
public class MeshController {

    private static final Logger log = LoggerFactory.getLogger(MeshController.class);

    private final NeuronMeshService meshService;
    private final MeshConsensusService meshConsensusService;
    private final MeshExperienceSyncService meshExperienceSyncService;

    public MeshController(NeuronMeshService meshService,
                          @Autowired(required = false) MeshConsensusService meshConsensusService,
                          @Autowired(required = false) MeshExperienceSyncService meshExperienceSyncService) {
        this.meshService = meshService;
        this.meshConsensusService = meshConsensusService;
        this.meshExperienceSyncService = meshExperienceSyncService;
    }

    /**
     * Register a new peer connection.
     * Called by a peer to establish a P2P connection.
     */
    @PostMapping("/peer/register")
    public ResponseEntity<MeshPeerInfo> registerPeer(@RequestBody MeshPeerInfo peerInfo) {
        log.info("Peer registration request from: {}", peerInfo.getNodeId());

        // Store the peer info
        meshService.handleIncomingMessage(
                new MeshTopologyUpdate(peerInfo.getNodeId(), Collections.singletonList(peerInfo), 1, 0) {
                    { setIsRequest(false); }
                });

        // Respond with our peer info
        MeshPeerInfo myInfo = meshService.buildPeerInfo();
        return ResponseEntity.ok(myInfo);
    }

    /**
     * Receive a mesh message from a peer.
     * This is the main entry point for all P2P messages.
     */
    @PostMapping("/message")
    public ResponseEntity<Void> receiveMessage(@RequestBody MeshMessage message) {
        log.debug("Received mesh message: {}", message.getType());
        meshService.handleIncomingMessage(message);
        return ResponseEntity.ok().build();
    }

    /**
     * Get the current mesh topology.
     * Returns the list of all known peers and their status.
     */
    @GetMapping("/topology")
    public ResponseEntity<Map<String, Object>> getTopology() {
        List<Map<String, Object>> peerList = new ArrayList<>();

        for (MeshPeerInfo peer : meshService.getPeers()) {
            Map<String, Object> peerMap = new HashMap<>();
            peerMap.put("nodeId", peer.getNodeId());
            peerMap.put("nodeName", peer.getNodeName());
            peerMap.put("region", peer.getRegion());
            peerMap.put("status", peer.getStatus());
            peerMap.put("modelVersion", peer.getModelVersion());
            peerMap.put("peerCount", peer.getPeerCount());
            peerMap.put("lastHeartbeat", peer.getLastHeartbeat());
            peerList.add(peerMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("peers", peerList);
        result.put("peerCount", meshService.getPeerCount());
        result.put("messagesSent", meshService.getMessagesSent());
        result.put("messagesReceived", meshService.getMessagesReceived());

        return ResponseEntity.ok(result);
    }

    /**
     * Request topology update from peers.
     */
    @PostMapping("/topology/request")
    public ResponseEntity<Void> requestTopology() {
        log.info("Requesting topology update from peers");
        MeshTopologyUpdate request = MeshTopologyUpdate.createRequest(
                meshService.buildPeerInfo().getNodeId());
        meshService.broadcast(request);
        return ResponseEntity.ok().build();
    }

    /**
     * Get mesh network statistics.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("peerCount", meshService.getPeerCount());
        status.put("messagesSent", meshService.getMessagesSent());
        status.put("messagesReceived", meshService.getMessagesReceived());
        status.put("peersConnected", meshService.getPeersConnected());
        status.put("peersDisconnected", meshService.getPeersDisconnected());

        // Add consensus stats if available
        if (meshConsensusService != null) {
            Map<String, Object> consensusStats = new HashMap<>();
            consensusStats.put("requested", meshConsensusService.getConsensusRequested());
            consensusStats.put("completed", meshConsensusService.getConsensusCompleted());
            consensusStats.put("failed", meshConsensusService.getConsensusFailed());
            status.put("consensus", consensusStats);
        }

        // Add experience sync stats if available
        if (meshExperienceSyncService != null) {
            Map<String, Object> syncStats = new HashMap<>();
            syncStats.put("samplesSent", meshExperienceSyncService.getSamplesSent());
            syncStats.put("samplesReceived", meshExperienceSyncService.getSamplesReceived());
            syncStats.put("syncRounds", meshExperienceSyncService.getSyncRounds());
            status.put("experienceSync", syncStats);
        }

        return ResponseEntity.ok(status);
    }

    /**
     * Request decentralized consensus via mesh.
     */
    @PostMapping("/consensus")
    public ResponseEntity<Map<String, Object>> requestConsensus(@RequestBody Map<String, Object> request) {
        if (meshConsensusService == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Mesh consensus not available"));
        }

        Object featuresObj = request.get("features");
        if (!(featuresObj instanceof List<?>)) {
            return ResponseEntity.badRequest().body(Map.of("error", "features is required"));
        }

        List<?> featuresList = (List<?>) featuresObj;
        double[] features = new double[featuresList.size()];
        for (int i = 0; i < featuresList.size(); i++) {
            features[i] = ((Number) featuresList.get(i)).doubleValue();
        }

        var decision = meshConsensusService.requestConsensus(features);
        if (decision == null) {
            return ResponseEntity.status(503).body(Map.of("error", "Consensus failed"));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("selectedAction", decision.getSelectedAction());
        result.put("confidence", decision.getConfidence());
        result.put("probabilities", decision.getProbabilities());
        return ResponseEntity.ok(result);
    }

    /**
     * Request experience samples from mesh peers.
     */
    @PostMapping("/experience/request")
    public ResponseEntity<Void> requestExperience() {
        if (meshExperienceSyncService == null) {
            return ResponseEntity.status(503).build();
        }

        meshExperienceSyncService.requestExperienceFromPeers();
        return ResponseEntity.ok().build();
    }

    /**
     * Get list of connected peers.
     */
    @GetMapping("/peers")
    public ResponseEntity<List<MeshPeerInfo>> getPeers() {
        return ResponseEntity.ok(new ArrayList<>(meshService.getPeers()));
    }

    /**
     * Disconnect from a specific peer.
     */
    @DeleteMapping("/peers/{nodeId}")
    public ResponseEntity<Void> disconnectPeer(@PathVariable String nodeId) {
        log.info("Disconnecting from peer: {}", nodeId);
        // The peer will be removed by the health check when it stops responding
        // For now, we just log the request
        return ResponseEntity.ok().build();
    }
}

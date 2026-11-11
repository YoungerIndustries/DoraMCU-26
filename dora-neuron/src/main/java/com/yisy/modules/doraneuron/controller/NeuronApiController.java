package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.model.ModelVersionInfo;
import com.yisy.modules.doraneuron.model.NeuronDecision;
import com.yisy.modules.doraneuron.protocol.ModelUpdateNotification;
import com.yisy.modules.doraneuron.service.ModelSyncService;
import com.yisy.modules.doraneuron.service.SatelliteAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST API controller for Neuron AI operations.
 *
 * Exposes endpoints for:
 * <ul>
 *   <li>Running local AI inference on game state feature vectors</li>
 *   <li>Receiving model update notifications from the coordinator</li>
 *   <li>Querying AI service status and model version</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/neuron")
@ConditionalOnBean(SatelliteAiService.class)
public class NeuronApiController {

    private static final Logger log = LoggerFactory.getLogger(NeuronApiController.class);

    private final SatelliteAiService satelliteAiService;
    private final ModelSyncService modelSyncService;

    public NeuronApiController(SatelliteAiService satelliteAiService, ModelSyncService modelSyncService) {
        this.satelliteAiService = satelliteAiService;
        this.modelSyncService = modelSyncService;
    }

    /**
     * Run local AI inference on a game state feature vector.
     *
     * @param request map containing "features" as a double array
     * @return the AI decision with selected action and confidence
     */
    @PostMapping("/predict")
    public ResponseEntity<NeuronDecision> predict(@RequestBody Map<String, Object> request) {
        Object featuresObj = request.get("features");
        if (featuresObj == null) {
            return ResponseEntity.badRequest().build();
        }

        double[] features;
        if (featuresObj instanceof double[] dArr) {
            features = dArr;
        } else if (featuresObj instanceof Double[] dObjArr) {
            features = new double[dObjArr.length];
            for (int i = 0; i < dObjArr.length; i++) {
                features[i] = dObjArr[i];
            }
        } else if (featuresObj instanceof float[] fArr) {
            features = new double[fArr.length];
            for (int i = 0; i < fArr.length; i++) {
                features[i] = fArr[i];
            }
        } else {
            log.warn("Unsupported features type: {}", featuresObj.getClass().getName());
            return ResponseEntity.badRequest().build();
        }

        NeuronDecision decision = satelliteAiService.predict(features);
        return ResponseEntity.ok(decision);
    }

    /**
     * Handle model update notification pushed by the coordinator.
     *
     * @param notification the model update notification
     * @return 200 OK if accepted
     */
    @PostMapping("/model/update")
    public ResponseEntity<Void> handleModelUpdate(@RequestBody ModelUpdateNotification notification) {
        log.info("Received model update notification: {}", notification);
        modelSyncService.handleModelUpdate(notification);
        return ResponseEntity.ok().build();
    }

    /**
     * Query the current AI service status and model version.
     *
     * @return status information including model version and readiness
     */
    @GetMapping("/ai/status")
    public ResponseEntity<Map<String, Object>> getAiStatus() {
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("ready", satelliteAiService.isReady());
        status.put("modelVersion", satelliteAiService.getCurrentModelVersion());
        return ResponseEntity.ok(status);
    }

    /**
     * Trigger a manual model sync from the coordinator.
     *
     * @return 200 OK if accepted
     */
    @PostMapping("/model/sync")
    public ResponseEntity<Void> triggerModelSync() {
        log.info("Manual model sync triggered");
        modelSyncService.downloadAndSwapLatest();
        return ResponseEntity.ok().build();
    }

    /**
     * Get the current model version info.
     *
     * @return model version information
     */
    @GetMapping("/model/version")
    public ResponseEntity<ModelVersionInfo> getModelVersion() {
        long version = satelliteAiService.getCurrentModelVersion();
        ModelVersionInfo info = new ModelVersionInfo();
        info.setVersion(version);
        info.setStatus(satelliteAiService.isReady() ? "ACTIVE" : "UNAVAILABLE");
        return ResponseEntity.ok(info);
    }

    /**
     * Download a specific model version (for A/B test assignment).
     *
     * @param version the version number to download
     * @return 200 OK if accepted
     */
    @PostMapping("/model/version/{version}/download")
    public ResponseEntity<Void> downloadSpecificVersion(@PathVariable long version) {
        log.info("Downloading specific model version: v{}", version);
        modelSyncService.downloadSpecificVersion(version);
        return ResponseEntity.ok().build();
    }
}

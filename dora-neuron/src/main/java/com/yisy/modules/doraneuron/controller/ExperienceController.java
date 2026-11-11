package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.model.ExperienceSample;
import com.yisy.modules.doraneuron.service.ExperienceCollectorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for experience collection operations.
 *
 * Exposes endpoints for:
 * <ul>
 *   <li>Submitting experience samples to the local buffer</li>
 *   <li>Triggering manual flush of buffered experiences</li>
 *   <li>Querying experience collection statistics</li>
 * </ul>
 *
 * This controller is always available — it does not depend on the ONNX
 * inference engine being configured.
 */
@RestController
@RequestMapping("/api/v1/neuron/experience")
public class ExperienceController {

    private static final Logger log = LoggerFactory.getLogger(ExperienceController.class);

    private final ExperienceCollectorService collectorService;

    public ExperienceController(ExperienceCollectorService collectorService) {
        this.collectorService = collectorService;
    }

    /**
     * Submit a single experience sample to the local buffer.
     *
     * @param sample the experience sample (state, action, reward tuple)
     * @return 200 OK if accepted
     */
    @PostMapping("/collect")
    public ResponseEntity<Void> collect(@RequestBody ExperienceSample sample) {
        collectorService.collect(sample);
        return ResponseEntity.ok().build();
    }

    /**
     * Submit multiple experience samples to the local buffer.
     *
     * @param samples the list of experience samples
     * @return 200 OK if accepted
     */
    @PostMapping("/collect/batch")
    public ResponseEntity<Void> collectBatch(@RequestBody List<ExperienceSample> samples) {
        collectorService.collectAll(samples);
        return ResponseEntity.ok().build();
    }

    /**
     * Trigger an immediate flush of all buffered experience samples to the coordinator.
     *
     * @return 200 OK if accepted
     */
    @PostMapping("/flush")
    public ResponseEntity<Void> flush() {
        log.info("Manual experience flush triggered via API");
        collectorService.flushNow();
        return ResponseEntity.ok().build();
    }

    /**
     * Query experience collection statistics.
     *
     * @return statistics including buffer size, total collected, total flushed, and flush failures
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("bufferSize", collectorService.getBufferSize());
        status.put("totalCollected", collectorService.getTotalCollected());
        status.put("totalFlushed", collectorService.getTotalFlushed());
        status.put("flushFailures", collectorService.getFlushFailures());
        return ResponseEntity.ok(status);
    }
}

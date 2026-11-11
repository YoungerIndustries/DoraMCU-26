package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.service.NeuronMetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for the Neuron monitoring dashboard.
 *
 * Exposes metrics, performance data, and cluster connectivity status
 * for monitoring systems and the admin dashboard.
 */
@RestController
@RequestMapping("/api/v1/neuron/monitoring")
public class MonitoringController {

    private final NeuronMetricsService metricsService;

    public MonitoringController(NeuronMetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * Get comprehensive metrics for the dashboard.
     *
     * @return all collected metrics
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMetrics() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }

    /**
     * Get a lightweight status ping for health checks.
     *
     * @return basic status information
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "uptimeMs", metricsService.getUptimeMs(),
                "totalRequests", metricsService.getTotalRequests()
        ));
    }

    /**
     * Get inference-specific metrics.
     *
     * @return inference statistics
     */
    @GetMapping("/inference")
    public ResponseEntity<Map<String, Object>> getInferenceMetrics() {
        return ResponseEntity.ok(Map.of(
                "totalInferences", metricsService.getTotalInferences(),
                "avgTimeMs", metricsService.getAvgInferenceTimeMs()
        ));
    }
}

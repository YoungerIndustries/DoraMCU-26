package com.yisy.modules.doraneuron.controller;

import com.yisy.modules.doraneuron.service.edge.EdgeMetricsService;
import com.yisy.modules.doraneuron.service.edge.ModelQuantizationService;
import com.yisy.modules.doraneuron.util.edge.SystemResourceDetector;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for edge-specific monitoring and management.
 *
 * Exposes endpoints for:
 * - Edge system metrics (CPU temp, swap, thermal throttling)
 * - Model quantization status
 * - System resource detection results
 */
@RestController
@RequestMapping("/api/v1/neuron/edge")
public class EdgeMonitoringController {

    private final EdgeMetricsService edgeMetricsService;
    private final ModelQuantizationService quantizationService;
    private final SystemResourceDetector detector;

    public EdgeMonitoringController(EdgeMetricsService edgeMetricsService,
                                     ModelQuantizationService quantizationService,
                                     SystemResourceDetector detector) {
        this.edgeMetricsService = edgeMetricsService;
        this.quantizationService = quantizationService;
        this.detector = detector;
    }

    /**
     * Get comprehensive edge-specific metrics.
     *
     * @return Map containing CPU temperature, swap usage, thermal throttling, memory pressure, and quantization status
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getEdgeMetrics() {
        return ResponseEntity.ok(edgeMetricsService.getEdgeMetrics());
    }

    /**
     * Get the detected system profile and hardware information.
     *
     * @return Map containing system profile, CPU cores, memory, architecture, and GPU status
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("systemProfile", detector.getProfile().name());
        info.put("isEdgeDevice", detector.isEdgeDevice());
        info.put("isMidRange", detector.isMidRange());
        info.put("isServer", detector.isServer());
        info.put("availableCores", detector.getAvailableCores());
        info.put("totalMemoryMb", detector.getTotalMemoryMb());
        info.put("isArmArchitecture", detector.isArmArchitecture());
        info.put("hasGpu", detector.hasGpu());
        info.put("architecture", detector.getArchitecture());
        info.put("osName", detector.getOsName());
        info.put("recommendedInferenceThreads", detector.getRecommendedInferenceThreads());
        info.put("recommendedExperienceBufferSize", detector.getRecommendedExperienceBufferSize());
        info.put("recommendedFlushIntervalMs", detector.getRecommendedFlushIntervalMs());
        info.put("shouldEnableMesh", detector.shouldEnableMesh());
        info.put("shouldEnableConsensus", detector.shouldEnableConsensus());
        info.put("shouldUseQuantizedModel", detector.shouldUseQuantizedModel());
        return ResponseEntity.ok(info);
    }

    /**
     * Get model quantization status and statistics.
     *
     * @return Map containing quantization enabled status, cache size, and request statistics
     */
    @GetMapping("/quantization")
    public ResponseEntity<Map<String, Object>> getQuantizationStatus() {
        return ResponseEntity.ok(quantizationService.getStats());
    }

    /**
     * Clear the quantized model cache.
     * Useful after model updates to force re-quantization.
     */
    @PostMapping("/quantization/clear-cache")
    public ResponseEntity<Map<String, String>> clearQuantizationCache() {
        quantizationService.clearCache();
        return ResponseEntity.ok(Map.of("status", "Quantization cache cleared"));
    }

    /**
     * Get thermal throttling status.
     *
     * @return Map containing current temperature, throttle status, and threshold
     */
    @GetMapping("/thermal")
    public ResponseEntity<Map<String, Object>> getThermalStatus() {
        return ResponseEntity.ok(Map.of(
                "cpuTemperature", edgeMetricsService.getCpuTemperature(),
                "isThermallyThrottled", edgeMetricsService.isThermallyThrottled(),
                "memoryPressure", edgeMetricsService.getMemoryPressure(),
                "swapUsedMb", edgeMetricsService.getSwapUsedMb()
        ));
    }
}

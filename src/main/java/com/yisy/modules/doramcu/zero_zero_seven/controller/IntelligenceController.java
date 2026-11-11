package com.yisy.modules.doramcu.zero_zero_seven.controller;

import com.yisy.modules.doramcu.zero_zero_seven.intelligence.CounterIntelligenceService;
import com.yisy.modules.doramcu.zero_zero_seven.intelligence.IntelligenceService;
import com.yisy.modules.doramcu.zero_zero_seven.model.SpyNetwork;
import com.yisy.modules.doramcu.zero_zero_seven.intelligence.SurveillanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for intelligence gathering.
 */
@RestController
@RequestMapping("/api/zero-zero-seven/intelligence")
public class IntelligenceController {

    private final IntelligenceService intelligenceService;
    private final SurveillanceService surveillanceService;
    private final CounterIntelligenceService counterIntelligenceService;

    public IntelligenceController(IntelligenceService intelligenceService,
                                   SurveillanceService surveillanceService,
                                   CounterIntelligenceService counterIntelligenceService) {
        this.intelligenceService = intelligenceService;
        this.surveillanceService = surveillanceService;
        this.counterIntelligenceService = counterIntelligenceService;
    }

    @GetMapping("/gather")
    public ResponseEntity<Map<String, Object>> gatherIntelligence(@RequestParam Long empireId) {
        int intelligence = intelligenceService.gatherIntelligence(empireId);
        int bonus = intelligenceService.getIntelligenceBonus(empireId);
        Map<String, Object> result = new HashMap<>();
        result.put("intelligence", intelligence);
        result.put("bonus", bonus);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/surveillance")
    public ResponseEntity<Map<String, Object>> getSurveillance(@RequestParam Long empireId) {
        int coverage = surveillanceService.getSurveillanceCoverage(empireId);
        Map<String, Object> result = new HashMap<>();
        result.put("coverage", coverage);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/counter-intelligence/detect")
    public ResponseEntity<List<SpyNetwork>> detectEnemyNetworks(
            @RequestParam Long locationId,
            @RequestParam Long empireId) {
        return ResponseEntity.ok(counterIntelligenceService.detectEnemyNetworks(locationId, empireId));
    }

    @PostMapping("/counter-intelligence/neutralize")
    public ResponseEntity<Map<String, Object>> neutralizeNetwork(
            @RequestParam Long networkId,
            @RequestParam(defaultValue = "5") int strength) {
        boolean success = counterIntelligenceService.neutralizeNetwork(networkId, strength);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return ResponseEntity.ok(result);
    }
}

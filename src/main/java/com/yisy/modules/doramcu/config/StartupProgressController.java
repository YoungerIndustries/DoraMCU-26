package com.yisy.modules.doramcu.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller that exposes the startup progress for the startup modal
 * to poll, and allows external components (e.g., the game client) to signal
 * step completion.
 */
@RestController
@RequestMapping("/api/v1/startup")
public class StartupProgressController {

    private final StartupProgressService progressService;

    public StartupProgressController(StartupProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * Returns all startup steps with their current status.
     */
    @GetMapping("/progress")
    public ResponseEntity<List<StartupProgressService.StartupStep>> getProgress() {
        return ResponseEntity.ok(progressService.getAllSteps());
    }

    /**
     * Returns a single step by key.
     */
    @GetMapping("/step")
    public ResponseEntity<StartupProgressService.StartupStep> getStep(@RequestParam String key) {
        StartupProgressService.StartupStep step = progressService.getStep(key);
        if (step == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(step);
    }

    /**
     * Allows external components to mark a step as complete.
     * Used by the game client to signal backup snapshot and sync completion.
     */
    @PostMapping("/step/complete")
    public ResponseEntity<Void> completeStep(@RequestBody StepCompleteRequest request) {
        progressService.completeStep(request.key(), request.message());
        return ResponseEntity.ok().build();
    }

    /**
     * Allows external components to mark a step as in progress.
     */
    @PostMapping("/step/start")
    public ResponseEntity<Void> startStep(@RequestBody StepCompleteRequest request) {
        progressService.startStep(request.key(), request.message());
        return ResponseEntity.ok().build();
    }

    public record StepCompleteRequest(String key, String message) {}
}

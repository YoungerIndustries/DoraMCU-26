package com.yisy.modules.doramcu.earth2026.tycoon;

import com.yisy.modules.doramcu.earth2026.tycoon.CorporationTycoonService.TycoonGameState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for the Corporation Tycoon game mode.
 * Provides endpoints for building management, employee hiring,
 * mission completion, and research projects.
 */
@RestController
@RequestMapping("/api/v1/tycoon")
public class TycoonController {

    private final CorporationTycoonService tycoonService;

    public TycoonController(CorporationTycoonService tycoonService) {
        this.tycoonService = tycoonService;
    }

    /**
     * Get the current tycoon dashboard for a corporation.
     */
    @GetMapping("/corporations/{corporationId}")
    public ResponseEntity<Map<String, Object>> getDashboard(@PathVariable Long corporationId) {
        Map<String, Object> dashboard = tycoonService.getDashboard(corporationId);
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get available buildings that can be constructed.
     */
    @GetMapping("/corporations/{corporationId}/buildings-available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableBuildings(@PathVariable Long corporationId) {
        List<Map<String, Object>> available = tycoonService.getAvailableBuildings(corporationId);
        return ResponseEntity.ok(available);
    }

    /**
     * Construct a new building.
     */
    @PostMapping("/corporations/{corporationId}/buildings")
    public ResponseEntity<Map<String, Object>> constructBuilding(
            @PathVariable Long corporationId,
            @RequestBody Map<String, String> request) {
        String type = request.get("type");
        Map<String, Object> response = new HashMap<>();

        try {
            TycoonBuilding.BuildingType buildingType = TycoonBuilding.BuildingType.valueOf(type);
            boolean success = tycoonService.constructBuilding(corporationId, buildingType);
            response.put("success", success);
            response.put("message", success ? "Building constructed successfully" : "Failed to construct building");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid building type: " + type);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Upgrade an existing building.
     */
    @PutMapping("/corporations/{corporationId}/buildings/{type}")
    public ResponseEntity<Map<String, Object>> upgradeBuilding(
            @PathVariable Long corporationId,
            @PathVariable String type) {
        Map<String, Object> response = new HashMap<>();

        try {
            TycoonBuilding.BuildingType buildingType = TycoonBuilding.BuildingType.valueOf(type);
            boolean success = tycoonService.upgradeBuilding(corporationId, buildingType);
            response.put("success", success);
            response.put("message", success ? "Building upgraded successfully" : "Failed to upgrade building");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid building type: " + type);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Hire an employee.
     */
    @PostMapping("/corporations/{corporationId}/employees")
    public ResponseEntity<Map<String, Object>> hireEmployee(
            @PathVariable Long corporationId,
            @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String role = (String) request.get("role");
        long salary = ((Number) request.get("salary")).longValue();

        Map<String, Object> response = new HashMap<>();
        boolean success = tycoonService.hireEmployee(corporationId, name, role, salary);
        response.put("success", success);
        response.put("message", success ? "Employee hired successfully" : "Failed to hire employee");
        return ResponseEntity.ok(response);
    }

    /**
     * Complete an FPS mission and earn rewards.
     */
    @PostMapping("/corporations/{corporationId}/missions")
    public ResponseEntity<Map<String, Object>> completeMission(
            @PathVariable Long corporationId,
            @RequestBody Map<String, Object> request) {
        int difficulty = ((Number) request.get("difficulty")).intValue();
        int kills = ((Number) request.get("kills")).intValue();
        int score = ((Number) request.get("score")).intValue();

        Map<String, Object> result = tycoonService.completeMission(corporationId, difficulty, kills, score);
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    /**
     * Start a research project.
     */
    @PostMapping("/corporations/{corporationId}/research")
    public ResponseEntity<Map<String, Object>> startResearch(
            @PathVariable Long corporationId,
            @RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        long cost = ((Number) request.get("cost")).longValue();

        Map<String, Object> response = new HashMap<>();
        boolean success = tycoonService.startResearch(corporationId, name, cost);
        response.put("success", success);
        response.put("message", success ? "Research started successfully" : "Failed to start research");
        return ResponseEntity.ok(response);
    }

    /**
     * Get available research projects.
     */
    @GetMapping("/research-available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableResearch() {
        List<Map<String, Object>> available = new java.util.ArrayList<>();

        for (TycoonResearchProject.ResearchType type : TycoonResearchProject.ResearchType.values()) {
            Map<String, Object> research = new HashMap<>();
            research.put("type", type.name());
            research.put("displayName", type.getDisplayName());
            research.put("description", type.getDescription());
            research.put("baseCost", type.getBaseCost());
            available.add(research);
        }

        return ResponseEntity.ok(available);
    }

    /**
     * Get available employee roles.
     */
    @GetMapping("/employee-roles")
    public ResponseEntity<List<Map<String, Object>>> getAvailableRoles() {
        List<Map<String, Object>> roles = new java.util.ArrayList<>();

        for (TycoonEmployee.EmployeeRole role : TycoonEmployee.EmployeeRole.values()) {
            Map<String, Object> employeeRole = new HashMap<>();
            employeeRole.put("role", role.name());
            employeeRole.put("displayName", role.getDisplayName());
            employeeRole.put("creditBonus", role.getCreditBonus());
            employeeRole.put("influenceBonus", role.getInfluenceBonus());
            employeeRole.put("techBonus", role.getTechBonus());
            employeeRole.put("materialsBonus", role.getMaterialsBonus());
            roles.add(employeeRole);
        }

        return ResponseEntity.ok(roles);
    }

    /**
     * Process a game tick for resource generation.
     */
    @PostMapping("/corporations/{corporationId}/tick")
    public ResponseEntity<Map<String, Object>> tick(@PathVariable Long corporationId) {
        tycoonService.tick(corporationId);
        Map<String, Object> dashboard = tycoonService.getDashboard(corporationId);
        return ResponseEntity.ok(dashboard);
    }
}

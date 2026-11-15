package com.yisy.modules.doramcu.earth2026.controller;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Connection;
import com.yisy.modules.doramcu.earth2026.service.Earth2026ConnectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Earth 2026 Connections.
 * <p>
 * Provides endpoints for connection CRUD, query operations, strength management,
 * and relationship graph queries.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/earth2026/connections")
public class Earth2026ConnectionController {

    private final Earth2026ConnectionService connectionService;

    public Earth2026ConnectionController(Earth2026ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    // --- CRUD Endpoints ---

    @GetMapping
    public List<Earth2026Connection> getAllConnections() {
        return connectionService.findActiveConnections();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Earth2026Connection> getConnection(@PathVariable Long id) {
        return connectionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createConnection(@RequestBody Map<String, Object> request) {
        try {
            String sourceType = (String) request.get("sourceType");
            String sourceIdStr = request.get("sourceId").toString();
            String targetType = (String) request.get("targetType");
            String targetIdStr = request.get("targetId").toString();
            String connectionType = (String) request.get("connectionType");

            if (sourceType == null || targetType == null || connectionType == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "sourceType, targetType, and connectionType are required"));
            }

            Earth2026Connection.EntityType srcType = Earth2026Connection.EntityType.valueOf(sourceType);
            Long srcId = Long.valueOf(sourceIdStr);
            Earth2026Connection.EntityType tgtType = Earth2026Connection.EntityType.valueOf(targetType);
            Long tgtId = Long.valueOf(targetIdStr);
            Earth2026Connection.ConnectionType connType = Earth2026Connection.ConnectionType.valueOf(connectionType);

            int strength = request.containsKey("strength") ? Integer.valueOf(request.get("strength").toString()) : 50;
            int secrecyLevel = request.containsKey("secrecyLevel") ? Integer.valueOf(request.get("secrecyLevel").toString()) : 0;

            Earth2026Connection connection = connectionService.createConnection(
                    srcType, srcId, tgtType, tgtId, connType, strength, secrecyLevel);

            if (request.containsKey("notes")) {
                connection.setNotes((String) request.get("notes"));
                connectionService.save(connection);
            }

            return ResponseEntity.ok(connection);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid enum value: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateConnection(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        Optional<Earth2026Connection> optional = connectionService.findById(id);
        if (optional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Earth2026Connection connection = optional.get();

        if (request.containsKey("connectionType")) {
            connection.setConnectionType(Earth2026Connection.ConnectionType.valueOf((String) request.get("connectionType")));
        }
        if (request.containsKey("strength")) {
            connection.setStrength(Integer.valueOf(request.get("strength").toString()));
        }
        if (request.containsKey("secrecyLevel")) {
            connection.setSecrecyLevel(Integer.valueOf(request.get("secrecyLevel").toString()));
        }
        if (request.containsKey("active")) {
            connection.setActive((Boolean) request.get("active"));
        }
        if (request.containsKey("notes")) {
            connection.setNotes((String) request.get("notes"));
        }

        return ResponseEntity.ok(connectionService.save(connection));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteConnection(@PathVariable Long id) {
        boolean deleted = connectionService.deactivateConnection(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Connection deactivated"));
        }
        return ResponseEntity.notFound().build();
    }

    // --- Query Endpoints ---

    @GetMapping("/source/{entityType}/{entityId}")
    public List<Earth2026Connection> getBySource(@PathVariable String entityType, @PathVariable Long entityId) {
        return connectionService.findActiveBySourceEntity(
                Earth2026Connection.EntityType.valueOf(entityType), entityId);
    }

    @GetMapping("/target/{entityType}/{entityId}")
    public List<Earth2026Connection> getByTarget(@PathVariable String entityType, @PathVariable Long entityId) {
        return connectionService.findActiveByTargetEntity(
                Earth2026Connection.EntityType.valueOf(entityType), entityId);
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public List<Earth2026Connection> getByEntity(@PathVariable String entityType, @PathVariable Long entityId) {
        return connectionService.findByEntity(
                Earth2026Connection.EntityType.valueOf(entityType), entityId);
    }

    @GetMapping("/type/{connectionType}")
    public List<Earth2026Connection> getByType(@PathVariable String connectionType) {
        return connectionService.findActiveByConnectionType(
                Earth2026Connection.ConnectionType.valueOf(connectionType));
    }

    @GetMapping("/discoverable/{maxSecrecyLevel}")
    public List<Earth2026Connection> getDiscoverable(@PathVariable int maxSecrecyLevel) {
        return connectionService.findDiscoverableConnections(maxSecrecyLevel);
    }

    // --- Strength Management Endpoints ---

    @PatchMapping("/{id}/strength/increase")
    public ResponseEntity<?> increaseStrength(@PathVariable Long id, @RequestParam int amount) {
        return connectionService.increaseStrength(id, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/strength/decrease")
    public ResponseEntity<?> decreaseStrength(@PathVariable Long id, @RequestParam int amount) {
        return connectionService.decreaseStrength(id, amount)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Relationship Graph Endpoints ---

    @GetMapping("/count/{entityType}/{entityId}")
    public Map<String, Object> getConnectionCount(@PathVariable String entityType, @PathVariable Long entityId) {
        int count = connectionService.getConnectionCount(
                Earth2026Connection.EntityType.valueOf(entityType), entityId);
        return Map.of("entityType", entityType, "entityId", entityId, "connectionCount", count);
    }

    @GetMapping("/has-connection")
    public Map<String, Object> hasConnection(
            @RequestParam String sourceType, @RequestParam Long sourceId,
            @RequestParam String targetType, @RequestParam Long targetId) {
        boolean has = connectionService.hasConnection(
                Earth2026Connection.EntityType.valueOf(sourceType), sourceId,
                Earth2026Connection.EntityType.valueOf(targetType), targetId);
        return Map.of("hasConnection", has);
    }
}

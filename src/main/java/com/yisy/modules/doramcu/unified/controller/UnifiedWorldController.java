package com.yisy.modules.doramcu.unified.controller;

import com.yisy.modules.doramcu.unified.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST controller for unified world features: events, world map, and quests.
 * <p>
 * All endpoints are under {@code /api/v1/unified}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/unified")
public class UnifiedWorldController {

    private static final Logger log = LoggerFactory.getLogger(UnifiedWorldController.class);

    private final UnifiedEventBus eventBus;
    private final WorldMapService worldMapService;
    private final UnifiedQuestService questService;

    @Autowired
    public UnifiedWorldController(UnifiedEventBus eventBus,
                                   WorldMapService worldMapService,
                                   UnifiedQuestService questService) {
        this.eventBus = eventBus;
        this.worldMapService = worldMapService;
        this.questService = questService;
    }

    // ============================================================
    // Event Endpoints
    // ============================================================

    @GetMapping("/events")
    public ResponseEntity<List<UnifiedEvent>> getEvents(
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "50") int limit) {
        log.debug("GET /api/v1/unified/events?module={}&limit={}", module, limit);
        List<UnifiedEvent> events;
        if (module != null && !module.isEmpty()) {
            events = eventBus.getEventsByModule(module, limit);
        } else {
            events = eventBus.getRecentEvents(limit);
        }
        return ResponseEntity.ok(events);
    }

    @GetMapping("/events/recent")
    public ResponseEntity<List<UnifiedEvent>> getRecentEvents(
            @RequestParam(defaultValue = "20") int limit) {
        log.debug("GET /api/v1/unified/events/recent?limit={}", limit);
        return ResponseEntity.ok(eventBus.getRecentEvents(limit));
    }

    @PostMapping("/events/publish")
    public ResponseEntity<UnifiedEvent> publishEvent(@RequestBody Map<String, Object> payload) {
        log.debug("POST /api/v1/unified/events/publish");
        String type = (String) payload.getOrDefault("type", "CUSTOM");
        String sourceModule = (String) payload.getOrDefault("sourceModule", "unknown");
        String title = (String) payload.getOrDefault("title", "Untitled Event");
        String description = (String) payload.getOrDefault("description", "");
        String targetCharacterId = (String) payload.get("targetCharacterId");

        UnifiedEvent.EventType eventType;
        try {
            eventType = UnifiedEvent.EventType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            eventType = UnifiedEvent.EventType.CUSTOM;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> eventPayload = (Map<String, Object>) payload.getOrDefault("payload", new HashMap<>());

        UnifiedEvent event = new UnifiedEvent(eventType, sourceModule, title, description,
                eventPayload, targetCharacterId);
        eventBus.publish(event);
        return ResponseEntity.ok(event);
    }

    // ============================================================
    // World Map Endpoints
    // ============================================================

    @GetMapping("/worldmap")
    public ResponseEntity<Map<String, Object>> getWorldMap() {
        log.debug("GET /api/v1/unified/worldmap");
        return ResponseEntity.ok(worldMapService.getWorldMapSummary());
    }

    @GetMapping("/worldmap/nodes")
    public ResponseEntity<List<WorldNode>> getWorldNodes(
            @RequestParam(required = false) String module) {
        log.debug("GET /api/v1/unified/worldmap/nodes?module={}", module);
        return ResponseEntity.ok(worldMapService.getNodes(module));
    }

    @GetMapping("/worldmap/nodes/{nodeId}")
    public ResponseEntity<WorldNode> getWorldNode(@PathVariable String nodeId) {
        log.debug("GET /api/v1/unified/worldmap/nodes/{}", nodeId);
        return worldMapService.getNode(nodeId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/worldmap/nodes/discovered")
    public ResponseEntity<List<WorldNode>> getDiscoveredNodes() {
        log.debug("GET /api/v1/unified/worldmap/nodes/discovered");
        return ResponseEntity.ok(worldMapService.getDiscoveredNodes());
    }

    @GetMapping("/worldmap/edges")
    public ResponseEntity<List<WorldEdge>> getWorldEdges() {
        log.debug("GET /api/v1/unified/worldmap/edges");
        return ResponseEntity.ok(worldMapService.getEdges());
    }

    @GetMapping("/worldmap/edges/{nodeId}")
    public ResponseEntity<List<WorldEdge>> getConnectedEdges(@PathVariable String nodeId) {
        log.debug("GET /api/v1/unified/worldmap/edges/{}", nodeId);
        return ResponseEntity.ok(worldMapService.getConnectedEdges(nodeId));
    }

    @PostMapping("/worldmap/discover")
    public ResponseEntity<Map<String, Object>> discoverNode(@RequestBody Map<String, String> payload) {
        log.debug("POST /api/v1/unified/worldmap/discover");
        String nodeId = payload.get("nodeId");
        boolean discovered = worldMapService.discoverNode(nodeId);
        Map<String, Object> response = new HashMap<>();
        response.put("discovered", discovered);
        response.put("nodeId", nodeId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/worldmap/nodes")
    public ResponseEntity<WorldNode> addWorldNode(@RequestBody Map<String, Object> payload) {
        log.debug("POST /api/v1/unified/worldmap/nodes");
        String id = (String) payload.get("id");
        String name = (String) payload.get("name");
        String typeStr = (String) payload.getOrDefault("type", "LOCATION");
        String module = (String) payload.getOrDefault("module", "unknown");
        double x = ((Number) payload.getOrDefault("x", 0)).doubleValue();
        double y = ((Number) payload.getOrDefault("y", 0)).doubleValue();
        double z = ((Number) payload.getOrDefault("z", 0)).doubleValue();
        String description = (String) payload.getOrDefault("description", "");

        WorldNode.NodeType type;
        try {
            type = WorldNode.NodeType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = WorldNode.NodeType.LOCATION;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) payload.getOrDefault("metadata", new HashMap<>());

        WorldNode node = new WorldNode(id, name, type, module, x, y, z, description, metadata);
        worldMapService.addNode(node);
        return ResponseEntity.ok(node);
    }

    @PostMapping("/worldmap/edges")
    public ResponseEntity<WorldEdge> addWorldEdge(@RequestBody Map<String, Object> payload) {
        log.debug("POST /api/v1/unified/worldmap/edges");
        String sourceId = (String) payload.get("sourceId");
        String targetId = (String) payload.get("targetId");
        String typeStr = (String) payload.getOrDefault("type", "TRAVEL");
        double distance = ((Number) payload.getOrDefault("distance", 0)).doubleValue();
        String description = (String) payload.getOrDefault("description", "");

        WorldEdge.EdgeType type;
        try {
            type = WorldEdge.EdgeType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = WorldEdge.EdgeType.TRAVEL;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) payload.getOrDefault("metadata", new HashMap<>());

        WorldEdge edge = new WorldEdge(sourceId, targetId, type, distance, description, metadata);
        worldMapService.addEdge(edge);
        return ResponseEntity.ok(edge);
    }

    // ============================================================
    // Quest Endpoints
    // ============================================================

    @GetMapping("/quests")
    public ResponseEntity<List<UnifiedQuest>> getAllQuests() {
        log.debug("GET /api/v1/unified/quests");
        return ResponseEntity.ok(questService.getAllQuests());
    }

    @GetMapping("/quests/{questId}")
    public ResponseEntity<UnifiedQuest> getQuest(@PathVariable String questId) {
        log.debug("GET /api/v1/unified/quests/{}", questId);
        return questService.getQuest(questId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/quests/status/{status}")
    public ResponseEntity<List<UnifiedQuest>> getQuestsByStatus(@PathVariable String status) {
        log.debug("GET /api/v1/unified/quests/status/{}", status);
        try {
            UnifiedQuest.QuestStatus questStatus = UnifiedQuest.QuestStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(questService.getQuestsByStatus(questStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/quests/{questId}/activate")
    public ResponseEntity<Map<String, Object>> activateQuest(@PathVariable String questId) {
        log.debug("POST /api/v1/unified/quests/{}/activate", questId);
        boolean activated = questService.activateQuest(questId);
        Map<String, Object> response = new HashMap<>();
        response.put("activated", activated);
        response.put("questId", questId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/quests/{questId}/objective/complete")
    public ResponseEntity<Map<String, Object>> completeObjective(
            @PathVariable String questId,
            @RequestBody Map<String, String> payload) {
        log.debug("POST /api/v1/unified/quests/{}/objective/complete", questId);
        String objectiveId = payload.get("objectiveId");
        boolean completed = questService.completeObjective(questId, objectiveId);
        Map<String, Object> response = new HashMap<>();
        response.put("completed", completed);
        response.put("questId", questId);
        response.put("objectiveId", objectiveId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/quests/{questId}/objective/progress")
    public ResponseEntity<Map<String, Object>> addProgress(
            @PathVariable String questId,
            @RequestBody Map<String, Object> payload) {
        log.debug("POST /api/v1/unified/quests/{}/objective/progress", questId);
        String objectiveId = (String) payload.get("objectiveId");
        double amount = ((Number) payload.getOrDefault("amount", 1)).doubleValue();
        boolean success = questService.addProgress(questId, objectiveId, amount);
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("questId", questId);
        response.put("objectiveId", objectiveId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/quests/generate")
    public ResponseEntity<UnifiedQuest> generateQuest(@RequestBody Map<String, Object> payload) {
        log.debug("POST /api/v1/unified/quests/generate");
        String title = (String) payload.getOrDefault("title", "Generated Quest");
        String description = (String) payload.getOrDefault("description", "A generated quest.");

        @SuppressWarnings("unchecked")
        List<String> sourceModules = (List<String>) payload.getOrDefault("sourceModules", Collections.singletonList("unknown"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> objectiveData = (List<Map<String, Object>>) payload.getOrDefault("objectives", new ArrayList<>());

        List<QuestObjective> objectives = new ArrayList<>();
        for (int i = 0; i < objectiveData.size(); i++) {
            Map<String, Object> obj = objectiveData.get(i);
            objectives.add(new QuestObjective(
                    "obj-" + i,
                    (String) obj.getOrDefault("description", "Complete objective"),
                    (String) obj.getOrDefault("module", "unknown"),
                    (String) obj.getOrDefault("type", "generic"),
                    ((Number) obj.getOrDefault("target", 1)).doubleValue(),
                    (Map<String, Object>) obj.getOrDefault("metadata", new HashMap<>())
            ));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> rewards = (Map<String, Object>) payload.getOrDefault("rewards", new HashMap<>());

        UnifiedQuest quest = questService.createQuest(title, description, sourceModules, objectives, rewards);
        return ResponseEntity.ok(quest);
    }
}

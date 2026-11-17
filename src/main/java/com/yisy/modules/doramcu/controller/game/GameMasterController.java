package com.yisy.modules.doramcu.controller.game;

import com.yisy.modules.doramcu.service.game.GameMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for AI-driven Game Master narrative generation.
 * <p>
 * Provides endpoints for generating story arcs, NPC dialogue, event descriptions,
 * quests, and managing per-session Game Master settings.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/gamemaster")
public class GameMasterController {

    private final GameMasterService gameMasterService;

    public GameMasterController(GameMasterService gameMasterService) {
        this.gameMasterService = gameMasterService;
    }

    // --- Enum Lists ---

    /**
     * Get all narrative types.
     */
    @GetMapping("/narrative-types")
    public List<Map<String, String>> getNarrativeTypes() {
        return gameMasterService.getAllNarrativeTypes();
    }

    /**
     * Get all narrative styles.
     */
    @GetMapping("/narrative-styles")
    public List<Map<String, Object>> getNarrativeStyles() {
        return gameMasterService.getAllNarrativeStyles();
    }

    /**
     * Get all game universes.
     */
    @GetMapping("/universes")
    public List<Map<String, String>> getUniverses() {
        return gameMasterService.getAllGameUniverses();
    }

    // --- Settings ---

    /**
     * Get Game Master settings for a session.
     */
    @GetMapping("/settings/{sessionId}")
    public GameMasterService.GameMasterSettings getSettings(@PathVariable String sessionId) {
        return gameMasterService.getSettings(sessionId);
    }

    /**
     * Update Game Master settings for a session.
     */
    @PutMapping("/settings/{sessionId}")
    public GameMasterService.GameMasterSettings updateSettings(
            @PathVariable String sessionId,
            @RequestBody GameMasterService.GameMasterSettings settings) {
        return gameMasterService.updateSettings(sessionId, settings);
    }

    // --- Narrative Generation ---

    /**
     * Generate narrative content for a game event.
     */
    @PostMapping("/narrative/{sessionId}")
    public GameMasterService.NarrativeEvent generateNarrative(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        String type = request.getOrDefault("type", "EVENT_DESCRIPTION");
        String context = request.getOrDefault("context", "");
        GameMasterService.NarrativeType narrativeType;
        try {
            narrativeType = GameMasterService.NarrativeType.valueOf(type);
        } catch (IllegalArgumentException e) {
            narrativeType = GameMasterService.NarrativeType.EVENT_DESCRIPTION;
        }
        return gameMasterService.generateNarrative(sessionId, narrativeType, context);
    }

    /**
     * Generate narrative content with explicit type, style, and universe.
     */
    @PostMapping("/narrative/custom")
    public Map<String, String> generateCustomNarrative(@RequestBody Map<String, String> request) {
        String type = request.getOrDefault("type", "EVENT_DESCRIPTION");
        String style = request.getOrDefault("style", "DRAMATIC");
        String universe = request.getOrDefault("universe", "GENERIC");
        String context = request.getOrDefault("context", "");

        GameMasterService.NarrativeType narrativeType;
        GameMasterService.NarrativeStyle narrativeStyle;
        GameMasterService.GameUniverse gameUniverse;

        try {
            narrativeType = GameMasterService.NarrativeType.valueOf(type);
        } catch (IllegalArgumentException e) {
            narrativeType = GameMasterService.NarrativeType.EVENT_DESCRIPTION;
        }
        try {
            narrativeStyle = GameMasterService.NarrativeStyle.valueOf(style);
        } catch (IllegalArgumentException e) {
            narrativeStyle = GameMasterService.NarrativeStyle.DRAMATIC;
        }
        try {
            gameUniverse = GameMasterService.GameUniverse.valueOf(universe);
        } catch (IllegalArgumentException e) {
            gameUniverse = GameMasterService.GameUniverse.GENERIC;
        }

        String content = gameMasterService.generateContent(narrativeType, narrativeStyle, gameUniverse, context);
        return Map.of("content", content, "type", type, "style", style, "universe", universe);
    }

    // --- Story Arcs ---

    /**
     * Create a new story arc.
     */
    @PostMapping("/arcs/{sessionId}")
    public GameMasterService.StoryArc createStoryArc(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "Untitled Arc");
        String summary = request.getOrDefault("summary", "");
        return gameMasterService.createStoryArc(sessionId, title, summary);
    }

    /**
     * Get all story arcs for a session.
     */
    @GetMapping("/arcs/{sessionId}")
    public List<GameMasterService.StoryArc> getArcs(@PathVariable String sessionId) {
        return gameMasterService.getArcs(sessionId);
    }

    /**
     * Get active story arcs for a session.
     */
    @GetMapping("/arcs/{sessionId}/active")
    public List<GameMasterService.StoryArc> getActiveArcs(@PathVariable String sessionId) {
        return gameMasterService.getActiveArcs(sessionId);
    }

    /**
     * Add a chapter to a story arc.
     */
    @PostMapping("/arcs/{sessionId}/{arcId}/chapters")
    public GameMasterService.StoryArc.Chapter addChapter(
            @PathVariable String sessionId,
            @PathVariable String arcId,
            @RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "Untitled Chapter");
        String content = request.getOrDefault("content", "");
        return gameMasterService.addChapter(sessionId, arcId, title, content);
    }

    /**
     * Complete a story arc.
     */
    @PostMapping("/arcs/{sessionId}/{arcId}/complete")
    public ResponseEntity<GameMasterService.StoryArc> completeArc(
            @PathVariable String sessionId,
            @PathVariable String arcId) {
        GameMasterService.StoryArc arc = gameMasterService.completeArc(sessionId, arcId);
        if (arc == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(arc);
    }

    // --- NPCs ---

    /**
     * Create an NPC character.
     */
    @PostMapping("/npcs/{sessionId}")
    public GameMasterService.NpcCharacter createNpc(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        String name = request.getOrDefault("name", "Unknown");
        String role = request.getOrDefault("role", "Mysterious Figure");
        String personality = request.getOrDefault("personality", "Enigmatic");
        String alignment = request.getOrDefault("alignment", "NEUTRAL");

        GameMasterService.NpcCharacter.NpcAlignment npcAlignment;
        try {
            npcAlignment = GameMasterService.NpcCharacter.NpcAlignment.valueOf(alignment);
        } catch (IllegalArgumentException e) {
            npcAlignment = GameMasterService.NpcCharacter.NpcAlignment.NEUTRAL;
        }
        return gameMasterService.createNpc(sessionId, name, role, personality, npcAlignment);
    }

    /**
     * Get all NPCs for a session.
     */
    @GetMapping("/npcs/{sessionId}")
    public List<GameMasterService.NpcCharacter> getNpcs(@PathVariable String sessionId) {
        return gameMasterService.getNpcs(sessionId);
    }

    /**
     * Get a random dialogue line from an NPC.
     */
    @GetMapping("/npcs/{sessionId}/{npcId}/dialogue")
    public ResponseEntity<String> getNpcDialogue(
            @PathVariable String sessionId,
            @PathVariable String npcId) {
        String dialogue = gameMasterService.getNpcDialogue(sessionId, npcId);
        if (dialogue == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dialogue);
    }

    // --- Quests ---

    /**
     * Generate a new quest.
     */
    @PostMapping("/quests/{sessionId}")
    public ResponseEntity<GameMasterService.Quest> generateQuest(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        String title = request.getOrDefault("title", "Untitled Quest");
        String description = request.getOrDefault("description", "");
        String difficulty = request.getOrDefault("difficulty", "MODERATE");

        GameMasterService.Quest.QuestDifficulty questDifficulty;
        try {
            questDifficulty = GameMasterService.Quest.QuestDifficulty.valueOf(difficulty);
        } catch (IllegalArgumentException e) {
            questDifficulty = GameMasterService.Quest.QuestDifficulty.MODERATE;
        }

        GameMasterService.Quest quest = gameMasterService.generateQuest(sessionId, title, description, questDifficulty);
        if (quest == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(quest);
    }

    /**
     * Get all quests for a session.
     */
    @GetMapping("/quests/{sessionId}")
    public List<GameMasterService.Quest> getQuests(@PathVariable String sessionId) {
        return gameMasterService.getQuests(sessionId);
    }

    /**
     * Get available quests for a session.
     */
    @GetMapping("/quests/{sessionId}/available")
    public List<GameMasterService.Quest> getAvailableQuests(@PathVariable String sessionId) {
        return gameMasterService.getAvailableQuests(sessionId);
    }

    /**
     * Accept a quest.
     */
    @PostMapping("/quests/{sessionId}/{questId}/accept")
    public ResponseEntity<GameMasterService.Quest> acceptQuest(
            @PathVariable String sessionId,
            @PathVariable String questId) {
        GameMasterService.Quest quest = gameMasterService.acceptQuest(sessionId, questId);
        if (quest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quest);
    }

    /**
     * Complete a quest.
     */
    @PostMapping("/quests/{sessionId}/{questId}/complete")
    public ResponseEntity<GameMasterService.Quest> completeQuest(
            @PathVariable String sessionId,
            @PathVariable String questId) {
        GameMasterService.Quest quest = gameMasterService.completeQuest(sessionId, questId);
        if (quest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(quest);
    }

    // --- Event History ---

    /**
     * Get event history for a session.
     */
    @GetMapping("/events/{sessionId}")
    public List<GameMasterService.NarrativeEvent> getEventHistory(@PathVariable String sessionId) {
        return gameMasterService.getEventHistory(sessionId);
    }

    /**
     * Get recent events for a session.
     */
    @GetMapping("/events/{sessionId}/recent")
    public List<GameMasterService.NarrativeEvent> getRecentEvents(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "10") int limit) {
        return gameMasterService.getRecentEvents(sessionId, limit);
    }

    /**
     * Get events by type for a session.
     */
    @GetMapping("/events/{sessionId}/type/{type}")
    public List<GameMasterService.NarrativeEvent> getEventsByType(
            @PathVariable String sessionId,
            @PathVariable String type) {
        GameMasterService.NarrativeType narrativeType;
        try {
            narrativeType = GameMasterService.NarrativeType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return List.of();
        }
        return gameMasterService.getEventsByType(sessionId, narrativeType);
    }

    // --- Statistics ---

    /**
     * Get statistics for a session.
     */
    @GetMapping("/stats/{sessionId}")
    public Map<String, Object> getStatistics(@PathVariable String sessionId) {
        return gameMasterService.getStatistics(sessionId);
    }

    /**
     * Get all registered session IDs.
     */
    @GetMapping("/sessions")
    public List<String> getAllSessions() {
        return gameMasterService.getAllSessions();
    }

    // --- Management ---

    /**
     * Clear all data for a session.
     */
    @DeleteMapping("/session/{sessionId}")
    public void clearSession(@PathVariable String sessionId) {
        gameMasterService.clearSession(sessionId);
    }

    /**
     * Clear all data.
     */
    @DeleteMapping("/clear-all")
    public void clearAll() {
        gameMasterService.clearAll();
    }
}

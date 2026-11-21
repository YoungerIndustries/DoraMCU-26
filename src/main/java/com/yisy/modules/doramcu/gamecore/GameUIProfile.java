package com.yisy.modules.doramcu.gamecore;
import com.yisy.modules.doramcu.model.players.Player;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UI profile categories for the unified game shell.
 * Each profile defines the panel layout, 3D camera mode, HUD elements, and action categories.
 */
public enum GameUIProfile {

    FPS("FPS", "First-Person Shooter",
            "Full-screen 3D with overlay HUD",
            "fps",
            List.of(GameType.FPS, GameType.MECHWARRIOR)),

    STRATEGY_4X("4X Strategy", "4X Empire Strategy",
            "Split view with galaxy map + strategy panels",
            "galaxy",
            List.of(GameType.DORAMCU_4X, GameType.STAR_TREK_4X, GameType.ZERO_ZERO_SEVEN,
                    GameType.TRANSPORT_TYCOON)),

    RPG_NARRATIVE("RPG Narrative", "Character-Driven RPG",
            "Split view with narrative + character sheet + action buttons",
            "narrative",
            List.of(GameType.TRAVELLER, GameType.CAPTAINS_LOG, GameType.EARTH_2026,
                    GameType.BABYLON_5, GameType.DND_5E, GameType.SFB, GameType.STAR_WARS)),

    GROUND_COMBAT("Ground Combat", "Tactical Battlefield",
            "Tactical 3D view + unit panels + order queue",
            "tactical",
            List.of(GameType.THIRD_REICH, GameType.STARSHIP_TROOPERS));

    private final String id;
    private final String displayName;
    private final String description;
    private final String cameraMode;
    private final List<GameType> gameTypes;

    // Reverse mapping: GameType -> GameUIProfile
    private static final Map<GameType, GameUIProfile> GAME_TYPE_MAP = new EnumMap<>(GameType.class);

    static {
        for (GameUIProfile profile : values()) {
            for (GameType gameType : profile.gameTypes) {
                GAME_TYPE_MAP.put(gameType, profile);
            }
        }
    }

    GameUIProfile(String id, String displayName, String description, String cameraMode, List<GameType> gameTypes) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.cameraMode = cameraMode;
        this.gameTypes = gameTypes;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    /**
     * Get the UI profile for a given game type.
     */
    public static GameUIProfile forGameType(GameType gameType) {
        GameUIProfile profile = GAME_TYPE_MAP.get(gameType);
        if (profile == null) {
            throw new IllegalArgumentException("No UI profile defined for game type: " + gameType);
        }
        return profile;
    }

    /**
     * Get all game types for this profile.
     */
    public List<String> getGameTypeNames() {
        return gameTypes.stream()
                .map(GameType::name)
                .collect(Collectors.toList());
    }

    /**
     * Get panel configuration for this profile.
     */
    public PanelConfig getPanelConfig() {
        switch (this) {
            case FPS:
                return new PanelConfig(
                        "Player Status",
                        "Kill Feed",
                        "Weapons & Actions",
                        "Scoreboard",
                        List.of("health", "shield", "armor", "ammo", "score"),
                        List.of("fire", "reload", "switch-weapon", "move", "interact"));
            case STRATEGY_4X:
                return new PanelConfig(
                        "Empire Status",
                        "Mission Briefing",
                        "Strategy Actions",
                        "Event Log",
                        List.of("turn", "credits", "minerals", "energy", "influence"),
                        List.of("explore", "expand", "exploit", "exterminate", "diplomacy", "research"));
            case RPG_NARRATIVE:
                return new PanelConfig(
                        "Character Sheet",
                        "DM Narrative",
                        "Action Buttons",
                        "Combat Log",
                        List.of("hp", "temp_hp", "spell_slots", "conditions"),
                        List.of("attack", "cast_spell", "use_item", "move", "dodge", "help", "rest"));
            case GROUND_COMBAT:
                return new PanelConfig(
                        "Unit Status",
                        "Battlefield Log",
                        "Order Queue",
                        "Squad Panel",
                        List.of("unit_hp", "ammo", "morale", "turn_phase"),
                        List.of("move", "attack", "suppress", "take_cover", "call_artillery", "retreat"));
            default:
                throw new IllegalStateException("Unknown profile: " + this);
        }
    }
}

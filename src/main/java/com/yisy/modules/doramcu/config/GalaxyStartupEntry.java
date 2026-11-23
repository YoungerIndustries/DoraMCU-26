package com.yisy.modules.doramcu.config;

import com.yisy.modules.doramcu.gamecore.GameMode;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.hex.GalaxyTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single galaxy entry in the multi-galaxy startup queue.
 * <p>
 * Each game module that provides a galaxy at startup declares one of these entries,
 * specifying the priority order, galaxy name, template, and associated game type.
 * </p>
 * <p>
 * The startup queue processes entries in priority order: lower numbers first.
 * For example, Earth2026 (priority 1) builds before Traveller (priority 2),
 * which builds before D&D (priority 3), and so on.
 * </p>
 */
public class GalaxyStartupEntry {

    /**
     * Priority order for galaxy creation. Lower values are created first.
     * Earth2026 should be first (1), then Traveller (2), then others.
     */
    private final int priority;

    /**
     * The name of the galaxy to create (e.g., "Earth 2026", "Traveller Galaxy").
     */
    private final String galaxyName;

    /**
     * The galaxy template that defines generation parameters.
     */
    private final GalaxyTemplate template;

    /**
     * The game type this galaxy belongs to.
     */
    private final GameType gameType;

    /**
     * Optional lore slug for module initialization.
     */
    private final String loreSlug;

    /**
     * Whether this galaxy should be created at startup.
     */
    private final boolean enabled;

    /**
     * The set of game modes to initialize for this galaxy.
     * Defaults to ALL game modes if not explicitly specified.
     */
    private final Set<GameMode> gameModes;

    /**
     * Difficulty level for the galaxy (e.g., "Easy", "Medium", "Hard", "Extreme").
     */
    private final String difficulty;

    /**
     * Theme description for the galaxy (e.g., "Space Opera", "Grimdark Warfare").
     */
    private final String theme;

    /**
     * Longer description of the galaxy setting.
     */
    private final String description;

    public GalaxyStartupEntry(int priority, String galaxyName, GalaxyTemplate template,
                                GameType gameType, String loreSlug, boolean enabled) {
        this(priority, galaxyName, template, gameType, loreSlug, enabled, null, null, null, null);
    }

    public GalaxyStartupEntry(int priority, String galaxyName, GalaxyTemplate template,
                                GameType gameType, String loreSlug, boolean enabled,
                                Set<GameMode> gameModes) {
        this(priority, galaxyName, template, gameType, loreSlug, enabled, gameModes, null, null, null);
    }

    public GalaxyStartupEntry(int priority, String galaxyName, GalaxyTemplate template,
                                GameType gameType, String loreSlug, boolean enabled,
                                Set<GameMode> gameModes, String difficulty, String theme, String description) {
        this.priority = priority;
        this.galaxyName = galaxyName;
        this.template = template;
        this.gameType = gameType;
        this.loreSlug = loreSlug;
        this.enabled = enabled;
        this.gameModes = (gameModes != null && !gameModes.isEmpty())
                ? gameModes
                : new HashSet<>(Arrays.asList(GameMode.values()));
        this.difficulty = difficulty;
        this.theme = theme;
        this.description = description;
    }

    /**
     * Creates a simple entry with default values (all game modes enabled).
     */
    public static GalaxyStartupEntry of(int priority, String galaxyName, GalaxyTemplate template,
                                          GameType gameType) {
        return new GalaxyStartupEntry(priority, galaxyName, template, gameType,
                galaxyName.toLowerCase().replace(" ", "-"), true, null);
    }

    /**
     * Creates an entry with a custom lore slug (all game modes enabled).
     */
    public static GalaxyStartupEntry of(int priority, String galaxyName, GalaxyTemplate template,
                                          GameType gameType, String loreSlug) {
        return new GalaxyStartupEntry(priority, galaxyName, template, gameType, loreSlug, true, null);
    }

    /**
     * Creates an entry with specific game modes.
     */
    public static GalaxyStartupEntry of(int priority, String galaxyName, GalaxyTemplate template,
                                          GameType gameType, String loreSlug, Set<GameMode> gameModes) {
        return new GalaxyStartupEntry(priority, galaxyName, template, gameType, loreSlug, true, gameModes, null, null, null);
    }

    /**
     * Creates an entry with metadata (difficulty, theme, description).
     */
    public static GalaxyStartupEntry of(int priority, String galaxyName, GalaxyTemplate template,
                                          GameType gameType, String loreSlug,
                                          String difficulty, String theme, String description) {
        return new GalaxyStartupEntry(priority, galaxyName, template, gameType, loreSlug, true, null, difficulty, theme, description);
    }

    public int getPriority() {
        return priority;
    }

    public String getGalaxyName() {
        return galaxyName;
    }

    public GalaxyTemplate getTemplate() {
        return template;
    }

    public GameType getGameType() {
        return gameType;
    }

    public String getLoreSlug() {
        return loreSlug;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Set<GameMode> getGameModes() {
        return gameModes;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getTheme() {
        return theme;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "GalaxyStartupEntry{priority=" + priority + ", galaxy='" + galaxyName +
                "', template=" + template.name() + ", gameType=" + gameType.name() +
                ", gameModes=" + gameModes + "}";
    }
}

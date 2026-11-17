package com.yisy.modules.doramcu.service.misc;
import com.yisy.modules.doramcu.gamecore.GameType;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.model.galaxy.Wormhole;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * AchievementService — Cross-game achievement tracking and leaderboards.
 *
 * Provides:
 * - Achievement definitions with categories, point values, and rarity
 * - Player achievement tracking with unlock timestamps
 * - Per-game and overall leaderboards
 * - Player stats dashboard (hours played, games completed, wins/losses)
 * - Game history with results and replay links
 * - Cross-game inventory for shared items
 * - Notification system for achievement unlocks
 *
 * Usage:
 *   achievementService.unlockAchievement(playerId, "first_battle");
 *   List<AchievementDto> achievements = achievementService.getPlayerAchievements(playerId);
 *   LeaderboardEntry[] leaderboard = achievementService.getLeaderboard("overall", 0, 99);
 */
@Service
public class AchievementService {
    private static final Logger log = LoggerFactory.getLogger(AchievementService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Achievement definitions
    private final Map<String, AchievementDefinition> achievementDefinitions = new ConcurrentHashMap<>();

    // Player data
    private final Map<String, Set<String>> playerAchievements = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Long>> playerAchievementTimes = new ConcurrentHashMap<>();
    private final Map<String, PlayerStats> playerStats = new ConcurrentHashMap<>();
    private final Map<String, List<GameHistoryEntry>> playerGameHistory = new ConcurrentHashMap<>();
    private final Map<String, List<CrossGameItem>> playerInventory = new ConcurrentHashMap<>();
    private final Map<String, List<AchievementNotification>> playerNotifications = new ConcurrentHashMap<>();

    // Leaderboard scores
    private final Map<String, AtomicLong> playerScores = new ConcurrentHashMap<>();

    public AchievementService() {
        this._registerDefaultAchievements();
    }

    /**
     * Register default achievement definitions.
     */
    private void _registerDefaultAchievements() {
        // Combat achievements
        registerAchievement(new AchievementDefinition(
                "first_battle", "First Blood", "Win your first battle",
                "combat", 10, "common", "⚔️"));
        registerAchievement(new AchievementDefinition(
                "battle_veteran", "Veteran", "Win 100 battles",
                "combat", 100, "rare", "🏆"));
        registerAchievement(new AchievementDefinition(
                "fleet_commander", "Fleet Commander", "Command a fleet of 10 ships",
                "combat", 50, "uncommon", "🚀"));
        registerAchievement(new AchievementDefinition(
                "galactic_conqueror", "Galactic Conqueror", "Control 10 sectors",
                "combat", 200, "epic", "🌌"));

        // Exploration achievements
        registerAchievement(new AchievementDefinition(
                "first_step", "First Steps", "Complete your first mission",
                "exploration", 5, "common", "👣"));
        registerAchievement(new AchievementDefinition(
                "explorer", "Explorer", "Visit 50 different systems",
                "exploration", 75, "rare", "🗺️"));
        registerAchievement(new AchievementDefinition(
                "wormhole_traveler", "Wormhole Traveler", "Travel through 10 wormholes",
                "exploration", 50, "uncommon", "🌀"));
        registerAchievement(new AchievementDefinition(
                "galaxy_explorer", "Galaxy Explorer", "Visit all galaxies",
                "exploration", 150, "epic", "🌠"));

        // Social achievements
        registerAchievement(new AchievementDefinition(
                "first_trade", "First Trade", "Complete your first trade",
                "social", 10, "common", "💰"));
        registerAchievement(new AchievementDefinition(
                "merchant", "Merchant", "Complete 100 trades",
                "social", 100, "rare", "🏪"));
        registerAchievement(new AchievementDefinition(
                "diplomat", "Diplomat", "Form an alliance with another player",
                "social", 50, "uncommon", "🤝"));
        registerAchievement(new AchievementDefinition(
                "leader", "Leader", "Lead a coalition of 5 players",
                "social", 150, "epic", "👑"));

        // RPG achievements
        registerAchievement(new AchievementDefinition(
                "level_up", "Level Up", "Reach level 10",
                "rpg", 25, "uncommon", "📈"));
        registerAchievement(new AchievementDefinition(
                "max_level", "Max Level", "Reach the maximum level",
                "rpg", 200, "epic", "⭐"));
        registerAchievement(new AchievementDefinition(
                "spell_master", "Spell Master", "Cast 1000 spells",
                "rpg", 100, "rare", "✨"));
        registerAchievement(new AchievementDefinition(
                "dice_legend", "Dice Legend", "Roll a natural 20 ten times in a row",
                "rpg", 300, "legendary", "🎲"));

        // Strategy achievements
        registerAchievement(new AchievementDefinition(
                "first_turn", "First Turn", "Complete your first turn",
                "strategy", 5, "common", "🔄"));
        registerAchievement(new AchievementDefinition(
                "empire_builder", "Empire Builder", "Build 50 structures",
                "strategy", 100, "rare", "🏗️"));
        registerAchievement(new AchievementDefinition(
                "economic_power", "Economic Power", "Accumulate 1 million credits",
                "strategy", 150, "epic", "💎"));
        registerAchievement(new AchievementDefinition(
                "research_master", "Research Master", "Research all technologies",
                "strategy", 200, "epic", "🔬"));
    }

    /**
     * Register a new achievement definition.
     * @param definition Achievement definition
     */
    public void registerAchievement(AchievementDefinition definition) {
        achievementDefinitions.put(definition.id, definition);
        log.debug("Registered achievement: {}", definition.id);
    }

    /**
     * Unlock an achievement for a player.
     * @param playerId Player ID
     * @param achievementId Achievement ID
     * @return true if the achievement was newly unlocked
     */
    public boolean unlockAchievement(String playerId, String achievementId) {
        playerAchievements.computeIfAbsent(playerId, k -> new HashSet<>());
        
        if (playerAchievements.get(playerId).contains(achievementId)) {
            return false; // Already unlocked
        }
        playerAchievements.get(playerId).add(achievementId);
        playerAchievementTimes.computeIfAbsent(playerId, k -> new HashMap<>());
        playerAchievementTimes.get(playerId).put(achievementId, System.currentTimeMillis());

        // Update player score
        AchievementDefinition def = achievementDefinitions.get(achievementId);
        if (def != null) {
            playerScores.computeIfAbsent(playerId, k -> new AtomicLong(0)).addAndGet(def.points);
        }

        // Create notification
        AchievementNotification notification = new AchievementNotification(
                achievementId,
                def != null ? def.name : achievementId,
                def != null ? def.description : "",
                def != null ? def.icon : "🏆",
                System.currentTimeMillis()
        );
        playerNotifications.computeIfAbsent(playerId, k -> new ArrayList<>());
        playerNotifications.get(playerId).add(notification);

        log.info("Player {} unlocked achievement: {}", playerId, achievementId);
        return true;
    }

    /**
     * Get all achievements for a player.
     * @param playerId Player ID
     * @return List of achievement DTOs
     */
    public List<AchievementDto> getPlayerAchievements(String playerId) {
        Set<String> unlocked = playerAchievements.getOrDefault(playerId, Collections.emptySet());
        Map<String, Long> times = playerAchievementTimes.getOrDefault(playerId, Collections.emptyMap());

        return achievementDefinitions.values().stream()
                .map(def -> {
                    boolean isUnlocked = unlocked.contains(def.id);
                    long unlockTime = times.getOrDefault(def.id, 0L);
                    return new AchievementDto(def, isUnlocked, unlockTime);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get unlocked achievements for a player.
     * @param playerId Player ID
     * @return List of unlocked achievement DTOs
     */
    public List<AchievementDto> getUnlockedAchievements(String playerId) {
        return getPlayerAchievements(playerId).stream()
                .filter(a -> a.unlocked)
                .collect(Collectors.toList());
    }

    /**
     * Get achievement progress for a player.
     * @param playerId Player ID
     * @return Progress DTO
     */
    public AchievementProgress getAchievementProgress(String playerId) {
        List<AchievementDto> all = getPlayerAchievements(playerId);
        long total = all.size();
        long unlocked = all.stream().filter(a -> a.unlocked).count();
        long points = all.stream().filter(a -> a.unlocked).mapToLong(a -> a.points).sum();
        return new AchievementProgress(playerId, total, unlocked, points);
    }

    /**
     * Get leaderboard entries.
     * @param category Category: "overall", "combat", "exploration", "social", "rpg", "strategy"
     * @param offset Offset for pagination
     * @param limit Number of entries
     * @return Array of leaderboard entries
     */
    public LeaderboardEntry[] getLeaderboard(String category, int offset, int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();

        playerScores.forEach((playerId, score) -> {
            PlayerStats stats = playerStats.getOrDefault(playerId, new PlayerStats(playerId));
            entries.add(new LeaderboardEntry(playerId, score.get(), stats));
        });

        // Sort by score descending
        entries.sort((a, b) -> Long.compare(b.score, a.score));

        return entries.stream()
                .skip(offset)
                .limit(limit)
                .toArray(LeaderboardEntry[]::new);
    }

    /**
     * Get player stats.
     * @param playerId Player ID
     * @return Player stats
     */
    public PlayerStats getPlayerStats(String playerId) {
        return playerStats.computeIfAbsent(playerId, PlayerStats::new);
    }

    /**
     * Update player stats.
     * @param playerId Player ID
     * @param stats New stats
     */
    public void updatePlayerStats(String playerId, PlayerStats stats) {
        playerStats.put(playerId, stats);
    }

    /**
     * Add a game history entry.
     * @param playerId Player ID
     * @param entry Game history entry
     */
    public void addGameHistory(String playerId, GameHistoryEntry entry) {
        playerGameHistory.computeIfAbsent(playerId, k -> new ArrayList<>());
        playerGameHistory.get(playerId).add(entry);

        // Update player stats
        PlayerStats stats = getPlayerStats(playerId);
        stats.gamesCompleted++;
        if (entry.result.equals("win")) {
            stats.wins++;
        } else if (entry.result.equals("loss")) {
            stats.losses++;
        }
        stats.hoursPlayed += entry.durationMinutes / 60.0;
    }

    /**
     * Get game history for a player.
     * @param playerId Player ID
     * @param limit Number of entries
     * @return List of game history entries
     */
    public List<GameHistoryEntry> getGameHistory(String playerId, int limit) {
        List<GameHistoryEntry> history = playerGameHistory.getOrDefault(playerId, Collections.emptyList());
        if (history.size() <= limit) return history;
        return history.subList(history.size() - limit, history.size());
    }

    /**
     * Add an item to the cross-game inventory.
     * @param playerId Player ID
     * @param item Item to add
     */
    public void addToInventory(String playerId, CrossGameItem item) {
        playerInventory.computeIfAbsent(playerId, k -> new ArrayList<>());
        playerInventory.get(playerId).add(item);
    }

    /**
     * Get cross-game inventory for a player.
     * @param playerId Player ID
     * @return List of inventory items
     */
    public List<CrossGameItem> getInventory(String playerId) {
        return playerInventory.getOrDefault(playerId, Collections.emptyList());
    }

    /**
     * Get notifications for a player.
     * @param playerId Player ID
     * @return List of notifications
     */
    public List<AchievementNotification> getNotifications(String playerId) {
        return playerNotifications.getOrDefault(playerId, Collections.emptyList());
    }

    /**
     * Mark notifications as read.
     * @param playerId Player ID
     */
    public void clearNotifications(String playerId) {
        playerNotifications.remove(playerId);
    }

    /**
     * Get all achievement definitions.
     * @return Map of achievement definitions
     */
    public Map<String, AchievementDefinition> getAchievementDefinitions() {
        return Collections.unmodifiableMap(achievementDefinitions);
    }

    /**
     * Get total number of players.
     * @return Player count
     */
    public int getPlayerCount() {
        return playerScores.size();
    }

    // ── Data Classes ──

    public static class AchievementDefinition {
        public final String id;
        public final String name;
        public final String description;
        public final String category;
        public final int points;
        public final String rarity;
        public final String icon;

        public AchievementDefinition(String id, String name, String description,
                                     String category, int points, String rarity, String icon) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.category = category;
            this.points = points;
            this.rarity = rarity;
            this.icon = icon;
        }
    }

    public static class AchievementDto {
        public final String id;
        public final String name;
        public final String description;
        public final String category;
        public final int points;
        public final String rarity;
        public final String icon;
        public final boolean unlocked;
        public final long unlockTime;

        public AchievementDto(AchievementDefinition def, boolean unlocked, long unlockTime) {
            this.id = def.id;
            this.name = def.name;
            this.description = def.description;
            this.category = def.category;
            this.points = def.points;
            this.rarity = def.rarity;
            this.icon = def.icon;
            this.unlocked = unlocked;
            this.unlockTime = unlockTime;
        }
    }

    public static class AchievementProgress {
        public final String playerId;
        public final long total;
        public final long unlocked;
        public final long points;

        public AchievementProgress(String playerId, long total, long unlocked, long points) {
            this.playerId = playerId;
            this.total = total;
            this.unlocked = unlocked;
            this.points = points;
        }
    }

    public static class LeaderboardEntry {
        public final String playerId;
        public final long score;
        public final PlayerStats stats;

        public LeaderboardEntry(String playerId, long score, PlayerStats stats) {
            this.playerId = playerId;
            this.score = score;
            this.stats = stats;
        }
    }

    public static class PlayerStats {
        public String playerId;
        public int gamesCompleted;
        public int wins;
        public int losses;
        public double hoursPlayed;
        public long lastActive;

        public PlayerStats() {
            this.lastActive = System.currentTimeMillis();
        }

        public PlayerStats(String playerId) {
            this.playerId = playerId;
            this.lastActive = System.currentTimeMillis();
        }
    }

    public static class GameHistoryEntry {
        public String sessionId;
        public String gameType;
        public String result;
        public int durationMinutes;
        public long timestamp;
        public String replayId;

        public GameHistoryEntry(String sessionId, String gameType, String result,
                                int durationMinutes, String replayId) {
            this.sessionId = sessionId;
            this.gameType = gameType;
            this.result = result;
            this.durationMinutes = durationMinutes;
            this.timestamp = System.currentTimeMillis();
            this.replayId = replayId;
        }
    }

    public static class CrossGameItem {
        public String itemId;
        public String name;
        public String description;
        public String icon;
        public String category;
        public boolean equipped;
        public long acquiredTime;

        public CrossGameItem(String itemId, String name, String description,
                            String icon, String category) {
            this.itemId = itemId;
            this.name = name;
            this.description = description;
            this.icon = icon;
            this.category = category;
            this.equipped = false;
            this.acquiredTime = System.currentTimeMillis();
        }
    }

    public static class AchievementNotification {
        public String achievementId;
        public String achievementName;
        public String description;
        public String icon;
        public long timestamp;

        public AchievementNotification(String achievementId, String achievementName,
                                       String description, String icon, long timestamp) {
            this.achievementId = achievementId;
            this.achievementName = achievementName;
            this.description = description;
            this.icon = icon;
            this.timestamp = timestamp;
        }
    }
}

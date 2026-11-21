package com.yisy.modules.doramcu.fps.integration;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.fps.core.FpsPlayerState;
import com.yisy.modules.doramcu.fps.core.FpsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bridges FPS gameplay performance to RPG character progression.
 * <p>
 * Tracks FPS stats per player and converts combat performance into:
 * <ul>
 *   <li>XP gains based on kills, assists, and survival</li>
 *   <li>Stat boosts: DEX from kills, CON from survival time, TECH from weapon mastery</li>
 *   <li>Match win/loss records for character reputation</li>
 * </ul>
 * </p>
 * <p>
 * The bridge maintains a session store of FPS performance data that can be
 * queried by the RPG system to update UnifiedCharacter stats.
 * </p>
 */
@Component
public class FpsRpgBridge {

    private static final Logger log = LoggerFactory.getLogger(FpsRpgBridge.class);

    // XP configuration
    private static final int XP_PER_KILL = 50;
    private static final int XP_PER_ASSIST = 25;
    private static final int XP_PER_DEATH = -10;
    private static final int XP_PER_WIN = 200;
    private static final int XP_PER_CAPTURE = 100;
    private static final int XP_PER_MINUTE_ALIVE = 5;

    // Stat boost thresholds (cumulative stats trigger permanent boosts)
    private static final int KILLS_FOR_DEX_BOOST = 100;
    private static final int MINUTES_ALIVE_FOR_CON_BOOST = 60;
    private static final int MATCHES_WON_FOR_CHA_BOOST = 10;

    // PlayerId -> cumulative FPS stats
    private final Map<String, FpsPlayerStats> playerStatsMap = new ConcurrentHashMap<>();

    /**
     * Cumulative FPS performance stats for a player.
     */
    public static class FpsPlayerStats {
        private final String playerId;
        private int totalKills;
        private int totalDeaths;
        private int totalAssists;
        private int totalWins;
        private int totalLosses;
        private int totalCaptures;
        private long totalPlayTimeMinutes;
        private int totalMatches;
        private long totalXpEarned;

        public FpsPlayerStats(String playerId) {
            this.playerId = playerId;
        }

        public String getPlayerId() { return playerId; }
        public int getTotalKills() { return totalKills; }
        public void addKills(int kills) { this.totalKills += kills; }
        public int getTotalDeaths() { return totalDeaths; }
        public void addDeaths(int deaths) { this.totalDeaths += deaths; }
        public int getTotalAssists() { return totalAssists; }
        public void addAssists(int assists) { this.totalAssists += assists; }
        public int getTotalWins() { return totalWins; }
        public void addWins(int wins) { this.totalWins += wins; }
        public int getTotalLosses() { return totalLosses; }
        public void addLosses(int losses) { this.totalLosses += losses; }
        public int getTotalCaptures() { return totalCaptures; }
        public void addCaptures(int captures) { this.totalCaptures += captures; }
        public long getTotalPlayTimeMinutes() { return totalPlayTimeMinutes; }
        public void addPlayTimeMinutes(long minutes) { this.totalPlayTimeMinutes += minutes; }
        public int getTotalMatches() { return totalMatches; }
        public void addMatches(int matches) { this.totalMatches += matches; }
        public long getTotalXpEarned() { return totalXpEarned; }
        public void addXp(long xp) { this.totalXpEarned += xp; }

        public double getKDRatio() {
            return totalDeaths > 0 ? (double) totalKills / totalDeaths : totalKills;
        }

        public int getWinRate() {
            return totalMatches > 0 ? (int) ((double) totalWins / totalMatches * 100) : 0;
        }
    }

    /**
     * XP calculation result from a single FPS match.
     */
    public static class FpsXpResult {
        private final int killsXp;
        private final int assistsXp;
        private final int deathsXp;
        private final int winXp;
        private final int captureXp;
        private final int survivalXp;
        private final int totalXp;

        public FpsXpResult(int killsXp, int assistsXp, int deathsXp, int winXp,
                          int captureXp, int survivalXp) {
            this.killsXp = killsXp;
            this.assistsXp = assistsXp;
            this.deathsXp = deathsXp;
            this.winXp = winXp;
            this.captureXp = captureXp;
            this.survivalXp = survivalXp;
            this.totalXp = killsXp + assistsXp + deathsXp + winXp + captureXp + survivalXp;
        }

        public int getKillsXp() { return killsXp; }
        public int getAssistsXp() { return assistsXp; }
        public int getDeathsXp() { return deathsXp; }
        public int getWinXp() { return winXp; }
        public int getCaptureXp() { return captureXp; }
        public int getSurvivalXp() { return survivalXp; }
        public int getTotalXp() { return totalXp; }
    }

    /**
     * Stat boost recommendation based on cumulative FPS performance.
     */
    public static class StatBoosts {
        private int dexBoost;
        private int conBoost;
        private int chaBoost;
        private int techBoost;

        public int getDexBoost() { return dexBoost; }
        public void setDexBoost(int dexBoost) { this.dexBoost = dexBoost; }
        public int getConBoost() { return conBoost; }
        public void setConBoost(int conBoost) { this.conBoost = conBoost; }
        public int getChaBoost() { return chaBoost; }
        public void setChaBoost(int chaBoost) { this.chaBoost = chaBoost; }
        public int getTechBoost() { return techBoost; }
        public void setTechBoost(int techBoost) { this.techBoost = techBoost; }

        public boolean hasBoosts() {
            return dexBoost > 0 || conBoost > 0 || chaBoost > 0 || techBoost > 0;
        }
    }

    /**
     * Get or create the cumulative stats for a player.
     */
    public FpsPlayerStats getPlayerStats(String playerId) {
        return playerStatsMap.computeIfAbsent(playerId, FpsPlayerStats::new);
    }

    /**
     * Process the end of an FPS match for a player and calculate XP rewards.
     *
     * @param playerId the player's ID
     * @param playerState the player's final state in the match
     * @param won whether the player's team won the match
     * @param playTimeMinutes how long the player was in the match
     * @return the XP breakdown for this match
     */
    public FpsXpResult processMatchEnd(String playerId, FpsPlayerState playerState,
                                       boolean won, long playTimeMinutes) {
        FpsPlayerStats stats = getPlayerStats(playerId);

        // Calculate XP components
        int killsXp = playerState.getKills() * XP_PER_KILL;
        int assistsXp = playerState.getAssists() * XP_PER_ASSIST;
        int deathsXp = playerState.getDeaths() * XP_PER_DEATH;
        int winXp = won ? XP_PER_WIN : 0;
        int captureXp = 0; // Will be set by FpsQuestIntegration if captures occurred
        int survivalXp = (int) (playTimeMinutes * XP_PER_MINUTE_ALIVE);

        FpsXpResult xpResult = new FpsXpResult(killsXp, assistsXp, deathsXp, winXp, captureXp, survivalXp);

        // Update cumulative stats
        stats.addKills(playerState.getKills());
        stats.addDeaths(playerState.getDeaths());
        stats.addAssists(playerState.getAssists());
        if (won) {
            stats.addWins(1);
        } else {
            stats.addLosses(1);
        }
        stats.addPlayTimeMinutes(playTimeMinutes);
        stats.addMatches(1);
        stats.addXp(xpResult.getTotalXp());

        log.info("Player {} earned {} XP from FPS match (K:{}, D:{}, A:{}, won:{})",
                playerId, xpResult.getTotalXp(),
                playerState.getKills(), playerState.getDeaths(),
                playerState.getAssists(), won);

        return xpResult;
    }

    /**
     * Process a capture point event for XP.
     *
     * @param playerId the player's ID
     * @return XP earned for the capture
     */
    public int processCapturePoint(String playerId) {
        FpsPlayerStats stats = getPlayerStats(playerId);
        stats.addCaptures(1);
        log.info("Player {} captured a point (+{} XP)", playerId, XP_PER_CAPTURE);
        return XP_PER_CAPTURE;
    }

    /**
     * Calculate stat boosts based on cumulative FPS performance.
     * Each boost level requires reaching a threshold of cumulative stats.
     *
     * @param playerId the player's ID
     * @return recommended stat boosts
     */
    public StatBoosts calculateStatBoosts(String playerId) {
        FpsPlayerStats stats = getPlayerStats(playerId);
        StatBoosts boosts = new StatBoosts();

        // DEX boost: 1 point per 100 kills
        boosts.setDexBoost(stats.getTotalKills() / KILLS_FOR_DEX_BOOST);

        // CON boost: 1 point per 60 minutes of survival
        boosts.setConBoost((int) (stats.getTotalPlayTimeMinutes() / MINUTES_ALIVE_FOR_CON_BOOST));

        // CHA boost: 1 point per 10 matches won (leadership)
        boosts.setChaBoost(stats.getTotalWins() / MATCHES_WON_FOR_CHA_BOOST);

        // TECH boost: 1 point per 50 captures (tactical mastery)
        boosts.setTechBoost(stats.getTotalCaptures() / 50);

        if (boosts.hasBoosts()) {
            log.info("Player {} stat boosts: DEX+{}, CON+{}, CHA+{}, TECH+{}",
                    playerId, boosts.getDexBoost(), boosts.getConBoost(),
                    boosts.getChaBoost(), boosts.getTechBoost());
        }

        return boosts;
    }

    /**
     * Get a summary of all players' FPS stats.
     */
    public Map<String, FpsPlayerStats> getAllPlayerStats() {
        return new ConcurrentHashMap<>(playerStatsMap);
    }

    /**
     * Reset stats for a player (e.g., for testing).
     */
    public void resetPlayerStats(String playerId) {
        playerStatsMap.remove(playerId);
        log.info("Reset FPS stats for player {}", playerId);
    }
}

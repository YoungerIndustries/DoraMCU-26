package com.yisy.modules.doramcu.hex.planet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Tracks cargo flow dominance across all corporations in the Business Tycoon campaign.
 * <p>
 * Market share is calculated as:
 * <pre>
 *   (corporation's cargo value delivered) / (total cargo value delivered by all corps)
 * </pre>
 * <p>
 * The win condition for the campaign is achieving ≥60% market share for a sustained
 * number of turns. This tracker records deliveries per turn per corporation,
 * calculates market share percentages, and provides leaderboard data.
 * </p>
 */
public class MarketShareTracker {

    /** Cargo value delivered per corporation per turn */
    private final Map<String, Long> currentTurnDeliveries;

    /** Lifetime cargo value delivered per corporation */
    private final Map<String, Long> lifetimeDeliveries;

    /** History of market share percentages per turn */
    private final List<List<CorporationMarketShare>> marketShareHistory;

    /** Current turn number */
    private int currentTurn;

    /** Minimum market share percentage to win (default 60%) */
    private int winMarketSharePercentage;

    /** Number of consecutive turns at win threshold required */
    private int winSustainTurns;

    /**
     * Creates a new market share tracker.
     *
     * @param winMarketSharePercentage the market share % needed to win (default 60)
     * @param winSustainTurns          number of consecutive turns at threshold (default 3)
     */
    public MarketShareTracker(int winMarketSharePercentage, int winSustainTurns) {
        this.currentTurnDeliveries = new ConcurrentHashMap<>();
        this.lifetimeDeliveries = new ConcurrentHashMap<>();
        this.marketShareHistory = new ArrayList<>();
        this.currentTurn = 0;
        this.winMarketSharePercentage = winMarketSharePercentage;
        this.winSustainTurns = winSustainTurns;
    }

    /**
     * Creates a new market share tracker with default win conditions (60% for 3 turns).
     */
    public MarketShareTracker() {
        this(60, 3);
    }

    /**
     * Records a cargo delivery for a corporation in the current turn.
     *
     * @param corporationId the corporation ID
     * @param cargoValue    the cargo value delivered
     */
    public void recordDelivery(String corporationId, long cargoValue) {
        Objects.requireNonNull(corporationId, "Corporation ID must not be null");
        if (cargoValue < 0) {
            throw new IllegalArgumentException("Cargo value must be non-negative");
        }
        currentTurnDeliveries.merge(corporationId, cargoValue, Long::sum);
        lifetimeDeliveries.merge(corporationId, cargoValue, Long::sum);
    }

    /**
     * Returns the total cargo value delivered in the current turn.
     *
     * @return total cargo value this turn
     */
    public long getTotalCurrentTurnDelivery() {
        return currentTurnDeliveries.values().stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Returns the cargo value delivered by a corporation in the current turn.
     *
     * @param corporationId the corporation ID
     * @return cargo value delivered this turn
     */
    public long getCurrentTurnDelivery(String corporationId) {
        return currentTurnDeliveries.getOrDefault(corporationId, 0L);
    }

    /**
     * Returns the lifetime cargo value delivered by a corporation.
     *
     * @param corporationId the corporation ID
     * @return lifetime cargo value
     */
    public long getLifetimeDelivery(String corporationId) {
        return lifetimeDeliveries.getOrDefault(corporationId, 0L);
    }

    /**
     * Calculates the market share percentage for a corporation in the current turn.
     *
     * @param corporationId the corporation ID
     * @return market share percentage (0-100)
     */
    public double getMarketSharePercentage(String corporationId) {
        long total = getTotalCurrentTurnDelivery();
        if (total == 0) {
            return 0.0;
        }
        long corpDelivery = getCurrentTurnDelivery(corporationId);
        return (corpDelivery * 100.0) / total;
    }

    /**
     * Calculates the lifetime market share percentage for a corporation.
     *
     * @param corporationId the corporation ID
     * @return lifetime market share percentage (0-100)
     */
    public double getLifetimeMarketSharePercentage(String corporationId) {
        long total = getTotalLifetimeDelivery();
        if (total == 0) {
            return 0.0;
        }
        long corpDelivery = getLifetimeDelivery(corporationId);
        return (corpDelivery * 100.0) / total;
    }

    /**
     * Returns the total lifetime cargo value delivered by all corporations.
     *
     * @return total lifetime cargo value
     */
    public long getTotalLifetimeDelivery() {
        return lifetimeDeliveries.values().stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Returns the market share for all corporations in the current turn,
     * sorted by market share descending.
     *
     * @return list of market share entries
     */
    public List<CorporationMarketShare> getMarketShareLeaderboard() {
        return currentTurnDeliveries.entrySet().stream()
                .map(entry -> {
                    long total = getTotalCurrentTurnDelivery();
                    double percentage = total > 0 ? (entry.getValue() * 100.0) / total : 0.0;
                    return new CorporationMarketShare(entry.getKey(), entry.getValue(), percentage);
                })
                .sorted((a, b) -> Double.compare(b.percentage, a.percentage))
                .collect(Collectors.toList());
    }

    /**
     * Returns the lifetime market share leaderboard.
     *
     * @return list of market share entries sorted by lifetime share descending
     */
    public List<CorporationMarketShare> getLifetimeLeaderboard() {
        return lifetimeDeliveries.entrySet().stream()
                .map(entry -> {
                    long total = getTotalLifetimeDelivery();
                    double percentage = total > 0 ? (entry.getValue() * 100.0) / total : 0.0;
                    return new CorporationMarketShare(entry.getKey(), entry.getValue(), percentage);
                })
                .sorted((a, b) -> Double.compare(b.percentage, a.percentage))
                .collect(Collectors.toList());
    }

    /**
     * Finalizes the current turn and starts a new one.
     * Saves the market share snapshot to history.
     */
    public void finalizeTurn() {
        // Save current turn market share to history
        List<CorporationMarketShare> snapshot = getMarketShareLeaderboard();
        marketShareHistory.add(Collections.unmodifiableList(snapshot));

        // Reset for next turn
        currentTurnDeliveries.clear();
        currentTurn++;
    }

    /**
     * Returns the current turn number.
     *
     * @return current turn
     */
    public int getCurrentTurn() {
        return currentTurn;
    }

    /**
     * Checks if a corporation has met the win condition.
     * The corporation must have ≥60% market share for N consecutive turns.
     *
     * @param corporationId the corporation ID
     * @return true if the win condition is met
     */
    public boolean hasWon(String corporationId) {
        if (marketShareHistory.size() < winSustainTurns) {
            return false;
        }

        // Check the last N turns for sustained market share
        int start = marketShareHistory.size() - winSustainTurns;
        for (int i = start; i < marketShareHistory.size(); i++) {
            List<CorporationMarketShare> turnData = marketShareHistory.get(i);
            boolean found = false;
            for (CorporationMarketShare entry : turnData) {
                if (entry.corporationId.equals(corporationId) &&
                        entry.percentage >= winMarketSharePercentage) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the win market share percentage threshold.
     *
     * @return win threshold percentage
     */
    public int getWinMarketSharePercentage() {
        return winMarketSharePercentage;
    }

    /**
     * Sets the win market share percentage threshold.
     *
     * @param percentage the threshold (0-100)
     */
    public void setWinMarketSharePercentage(int percentage) {
        if (percentage < 1 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 1 and 100");
        }
        this.winMarketSharePercentage = percentage;
    }

    /**
     * Returns the number of consecutive turns required to sustain the win condition.
     *
     * @return sustain turns
     */
    public int getWinSustainTurns() {
        return winSustainTurns;
    }

    /**
     * Sets the number of consecutive turns required to sustain the win condition.
     *
     * @param turns the number of turns
     */
    public void setWinSustainTurns(int turns) {
        if (turns < 1) {
            throw new IllegalArgumentException("Turns must be positive");
        }
        this.winSustainTurns = turns;
    }

    /**
     * Returns the market share history.
     *
     * @return unmodifiable list of market share snapshots per turn
     */
    public List<List<CorporationMarketShare>> getMarketShareHistory() {
        return Collections.unmodifiableList(marketShareHistory);
    }

    /**
     * Returns the number of corporations tracked.
     *
     * @return corporation count
     */
    public int getCorporationCount() {
        return lifetimeDeliveries.size();
    }

    /**
     * Represents a corporation's market share at a point in time.
     */
    public static class CorporationMarketShare {
        public final String corporationId;
        public final long cargoValue;
        public final double percentage;

        CorporationMarketShare(String corporationId, long cargoValue, double percentage) {
            this.corporationId = corporationId;
            this.cargoValue = cargoValue;
            this.percentage = percentage;
        }

        @Override
        public String toString() {
            return String.format("%s: %.1f%% (%d cargo)", corporationId, percentage, cargoValue);
        }
    }
}

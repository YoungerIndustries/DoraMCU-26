package com.yisy.modules.doramcu.hex.manager;
import com.yisy.modules.doramcu.traveller.Commodity;

/**
 * Represents a player-initiated trade order.
 * <p>
 * Unlike auto-trade, trade orders are manually created by players
 * and consume turns based on the distance between source and destination.
 * The trade executes when the order completes.
 * </p>
 */
public class TradeOrder {

    private final int id;
    private final int empireId;
    private final int sourcePlanetId;
    private final int destPlanetId;
    private final Commodity commodity;
    private final int units;
    private final int totalTurns;
    private int turnsRemaining;
    private boolean completed;
    private boolean cancelled;
    private long profit;

    /**
     * Creates a new trade order.
     *
     * @param id the unique order ID
     * @param empireId the empire ID
     * @param sourcePlanetId the planet to buy from
     * @param destPlanetId the planet to sell at
     * @param commodity the commodity to trade
     * @param units the number of units to trade
     * @param distance the distance between planets (determines turns)
     */
    public TradeOrder(int id, int empireId, int sourcePlanetId, int destPlanetId,
                       Commodity commodity, int units, int distance) {
        this.id = id;
        this.empireId = empireId;
        this.sourcePlanetId = sourcePlanetId;
        this.destPlanetId = destPlanetId;
        this.commodity = commodity;
        this.units = units;
        this.totalTurns = Math.max(1, distance);
        this.turnsRemaining = this.totalTurns;
        this.completed = false;
        this.cancelled = false;
        this.profit = 0;
    }

    /** @return the unique order ID */
    public int getId() {
        return id;
    }

    /** @return the empire ID */
    public int getEmpireId() {
        return empireId;
    }

    /** @return the source planet ID */
    public int getSourcePlanetId() {
        return sourcePlanetId;
    }

    /** @return the destination planet ID */
    public int getDestPlanetId() {
        return destPlanetId;
    }

    /** @return the commodity being traded */
    public Commodity getCommodity() {
        return commodity;
    }

    /** @return the number of units */
    public int getUnits() {
        return units;
    }

    /** @return the total number of turns for this trade */
    public int getTotalTurns() {
        return totalTurns;
    }

    /** @return the number of turns remaining */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** @return true if the trade is in progress */
    public boolean isInProgress() {
        return !completed && !cancelled;
    }

    /** @return true if the trade is completed */
    public boolean isCompleted() {
        return completed;
    }

    /** @return true if the trade is cancelled */
    public boolean isCancelled() {
        return cancelled;
    }

    /** @return the profit from this trade (set on completion) */
    public long getProfit() {
        return profit;
    }

    /**
     * Advances the trade order by one turn.
     *
     * @return true if the trade just completed this turn
     */
    public boolean processTurn() {
        if (completed || cancelled) return false;
        turnsRemaining--;
        if (turnsRemaining <= 0) {
            completed = true;
        }
        return completed;
    }

    /**
     * Cancels the trade order.
     */
    public void cancel() {
        cancelled = true;
    }

    /**
     * Sets the profit for this trade (called on completion).
     *
     * @param profit the profit in credits
     */
    public void setProfit(long profit) {
        this.profit = profit;
    }

    @Override
    public String toString() {
        return String.format("TradeOrder{id=%d, empire=%d, %d->%d, %s x%d, turns=%d/%d}",
                id, empireId, sourcePlanetId, destPlanetId,
                commodity.getName(), units, totalTurns - turnsRemaining, totalTurns);
    }
}

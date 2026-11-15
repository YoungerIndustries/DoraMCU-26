package com.yisy.modules.doramcu.dto;

/**
 * Data transfer object for a single trade mission record.
 * <p>
 * Represents a completed trade mission with key details:
 * turn number, planets involved, commodity, units, and profit.
 * </p>
 */
public class TradeMissionDto {

    /** Turn number when the trade was completed. */
    private int turn;

    /** Source planet ID. */
    private int sourcePlanetId;

    /** Destination planet ID. */
    private int destPlanetId;

    /** Commodity name. */
    private String commodity;

    /** Number of units traded. */
    private int units;

    /** Total cost in credits. */
    private long cost;

    /** Total revenue in credits. */
    private long revenue;

    /** Net profit (revenue - cost). */
    private long profit;

    public TradeMissionDto() {
    }

    public TradeMissionDto(int turn, int sourcePlanetId, int destPlanetId,
                           String commodity, int units, long cost, long revenue, long profit) {
        this.turn = turn;
        this.sourcePlanetId = sourcePlanetId;
        this.destPlanetId = destPlanetId;
        this.commodity = commodity;
        this.units = units;
        this.cost = cost;
        this.revenue = revenue;
        this.profit = profit;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getSourcePlanetId() {
        return sourcePlanetId;
    }

    public void setSourcePlanetId(int sourcePlanetId) {
        this.sourcePlanetId = sourcePlanetId;
    }

    public int getDestPlanetId() {
        return destPlanetId;
    }

    public void setDestPlanetId(int destPlanetId) {
        this.destPlanetId = destPlanetId;
    }

    public String getCommodity() {
        return commodity;
    }

    public void setCommodity(String commodity) {
        this.commodity = commodity;
    }

    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    public long getCost() {
        return cost;
    }

    public void setCost(long cost) {
        this.cost = cost;
    }

    public long getRevenue() {
        return revenue;
    }

    public void setRevenue(long revenue) {
        this.revenue = revenue;
    }

    public long getProfit() {
        return profit;
    }

    public void setProfit(long profit) {
        this.profit = profit;
    }
}

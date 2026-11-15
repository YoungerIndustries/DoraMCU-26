package com.yisy.modules.doramcu.dto;

import java.util.List;

/**
 * Summary of trade activity for an empire.
 * <p>
 * Includes aggregate statistics (total trades, profit, average profit)
 * and recent trade missions for quick overview.
 * </p>
 */
public class TradeSummaryDto {

    /** Empire ID this summary belongs to. */
    private int empireId;

    /** Total number of completed trade missions. */
    private int totalTrades;

    /** Total profit across all trades (can be negative). */
    private long totalProfit;

    /** Average profit per trade. */
    private long averageProfit;

    /** Number of profitable trades. */
    private int profitableTrades;

    /** Number of unprofitable trades. */
    private int unprofitableTrades;

    /** Most recent trade missions (up to 10). */
    private List<TradeMissionDto> recentTrades;

    /** Active trade orders currently in progress. */
    private int activeTradeOrders;

    public TradeSummaryDto() {
    }

    public TradeSummaryDto(int empireId, int totalTrades, long totalProfit, long averageProfit,
                           int profitableTrades, int unprofitableTrades,
                           List<TradeMissionDto> recentTrades, int activeTradeOrders) {
        this.empireId = empireId;
        this.totalTrades = totalTrades;
        this.totalProfit = totalProfit;
        this.averageProfit = averageProfit;
        this.profitableTrades = profitableTrades;
        this.unprofitableTrades = unprofitableTrades;
        this.recentTrades = recentTrades;
        this.activeTradeOrders = activeTradeOrders;
    }

    public int getEmpireId() {
        return empireId;
    }

    public void setEmpireId(int empireId) {
        this.empireId = empireId;
    }

    public int getTotalTrades() {
        return totalTrades;
    }

    public void setTotalTrades(int totalTrades) {
        this.totalTrades = totalTrades;
    }

    public long getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(long totalProfit) {
        this.totalProfit = totalProfit;
    }

    public long getAverageProfit() {
        return averageProfit;
    }

    public void setAverageProfit(long averageProfit) {
        this.averageProfit = averageProfit;
    }

    public int getProfitableTrades() {
        return profitableTrades;
    }

    public void setProfitableTrades(int profitableTrades) {
        this.profitableTrades = profitableTrades;
    }

    public int getUnprofitableTrades() {
        return unprofitableTrades;
    }

    public void setUnprofitableTrades(int unprofitableTrades) {
        this.unprofitableTrades = unprofitableTrades;
    }

    public List<TradeMissionDto> getRecentTrades() {
        return recentTrades;
    }

    public void setRecentTrades(List<TradeMissionDto> recentTrades) {
        this.recentTrades = recentTrades;
    }

    public int getActiveTradeOrders() {
        return activeTradeOrders;
    }

    public void setActiveTradeOrders(int activeTradeOrders) {
        this.activeTradeOrders = activeTradeOrders;
    }
}

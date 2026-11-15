package com.yisy.modules.doramcu.dto;

import java.util.List;
import java.util.Map;

/**
 * Income projection and trend analysis for an empire.
 * <p>
 * Uses historical turn data to project future income trends,
 * showing income by source, growth rate, and projected values
 * for the next N turns.
 * </p>
 */
public class IncomeProjectionDto {

    /** Empire ID this projection belongs to. */
    private int empireId;

    /** Current turn number. */
    private int currentTurn;

    /** Current credit balance. */
    private long currentCredits;

    /** Current net worth (credits + resource value). */
    private long currentNetWorth;

    /** Income breakdown by source for the current turn. */
    private Map<String, Long> incomeBySource;

    /** Expenses breakdown by source for the current turn. */
    private Map<String, Long> expensesBySource;

    /** Net income for the current turn. */
    private long netIncome;

    /** Average income over the last N turns. */
    private long averageIncome;

    /** Income growth rate as a percentage (positive = growing). */
    private double incomeGrowthRate;

    /** Projected income for the next 5 turns. */
    private List<Long> projectedIncomeNext5Turns;

    /** Projected net worth for the next 5 turns. */
    private List<Long> projectedNetWorthNext5Turns;

    /** Number of historical turns used for analysis. */
    private int historicalTurnCount;

    /** Trend direction: "GROWING", "STABLE", or "DECLINING". */
    private String trendDirection;

    public IncomeProjectionDto() {
    }

    public IncomeProjectionDto(int empireId, int currentTurn, long currentCredits, long currentNetWorth,
                               Map<String, Long> incomeBySource, Map<String, Long> expensesBySource,
                               long netIncome, long averageIncome, double incomeGrowthRate,
                               List<Long> projectedIncomeNext5Turns, List<Long> projectedNetWorthNext5Turns,
                               int historicalTurnCount, String trendDirection) {
        this.empireId = empireId;
        this.currentTurn = currentTurn;
        this.currentCredits = currentCredits;
        this.currentNetWorth = currentNetWorth;
        this.incomeBySource = incomeBySource;
        this.expensesBySource = expensesBySource;
        this.netIncome = netIncome;
        this.averageIncome = averageIncome;
        this.incomeGrowthRate = incomeGrowthRate;
        this.projectedIncomeNext5Turns = projectedIncomeNext5Turns;
        this.projectedNetWorthNext5Turns = projectedNetWorthNext5Turns;
        this.historicalTurnCount = historicalTurnCount;
        this.trendDirection = trendDirection;
    }

    public int getEmpireId() {
        return empireId;
    }

    public void setEmpireId(int empireId) {
        this.empireId = empireId;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(int currentTurn) {
        this.currentTurn = currentTurn;
    }

    public long getCurrentCredits() {
        return currentCredits;
    }

    public void setCurrentCredits(long currentCredits) {
        this.currentCredits = currentCredits;
    }

    public long getCurrentNetWorth() {
        return currentNetWorth;
    }

    public void setCurrentNetWorth(long currentNetWorth) {
        this.currentNetWorth = currentNetWorth;
    }

    public Map<String, Long> getIncomeBySource() {
        return incomeBySource;
    }

    public void setIncomeBySource(Map<String, Long> incomeBySource) {
        this.incomeBySource = incomeBySource;
    }

    public Map<String, Long> getExpensesBySource() {
        return expensesBySource;
    }

    public void setExpensesBySource(Map<String, Long> expensesBySource) {
        this.expensesBySource = expensesBySource;
    }

    public long getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(long netIncome) {
        this.netIncome = netIncome;
    }

    public long getAverageIncome() {
        return averageIncome;
    }

    public void setAverageIncome(long averageIncome) {
        this.averageIncome = averageIncome;
    }

    public double getIncomeGrowthRate() {
        return incomeGrowthRate;
    }

    public void setIncomeGrowthRate(double incomeGrowthRate) {
        this.incomeGrowthRate = incomeGrowthRate;
    }

    public List<Long> getProjectedIncomeNext5Turns() {
        return projectedIncomeNext5Turns;
    }

    public void setProjectedIncomeNext5Turns(List<Long> projectedIncomeNext5Turns) {
        this.projectedIncomeNext5Turns = projectedIncomeNext5Turns;
    }

    public List<Long> getProjectedNetWorthNext5Turns() {
        return projectedNetWorthNext5Turns;
    }

    public void setProjectedNetWorthNext5Turns(List<Long> projectedNetWorthNext5Turns) {
        this.projectedNetWorthNext5Turns = projectedNetWorthNext5Turns;
    }

    public int getHistoricalTurnCount() {
        return historicalTurnCount;
    }

    public void setHistoricalTurnCount(int historicalTurnCount) {
        this.historicalTurnCount = historicalTurnCount;
    }

    public String getTrendDirection() {
        return trendDirection;
    }

    public void setTrendDirection(String trendDirection) {
        this.trendDirection = trendDirection;
    }
}

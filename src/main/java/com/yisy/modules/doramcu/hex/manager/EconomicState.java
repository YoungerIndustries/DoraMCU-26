package com.yisy.modules.doramcu.hex.manager;

import com.yisy.modules.doramcu.hex.ConstructionModuleType;
import com.yisy.modules.doramcu.hex.RefinedResourceType;
import com.yisy.modules.doramcu.hex.ResourcePipelineManager;
import com.yisy.modules.doramcu.hex.ResourceStorage;
import com.yisy.modules.doramcu.hex.RawResourceType;

import java.util.*;

/**
 * Unified economic state for an empire.
 * <p>
 * EconomicState bridges the gap between Traveller credits and DoraMCU resources
 * by providing a single view of an empire's total economic position. It tracks:
 * </p>
 * <ul>
 *   <li>Credit balance from all income sources (trade, mercenary, stations)</li>
 *   <li>Reference to the resource pipeline for raw, refined, and module resources</li>
 *   <li>Income history for trend analysis and projections</li>
 *   <li>Net worth calculation combining credits and resource values</li>
 * </ul>
 * <p>
 * All economic operations should flow through EconomicState to ensure
 * consistent tracking and reporting.
 * </p>
 */
public class EconomicState {

    private final int empireId;
    private final ResourcePipelineManager pipeline;

    /** Technology tree for this empire. */
    private final TechnologyTree technologyTree;

    /** Credit balance — the single source of truth for empire credits. */
    private long creditBalance;

    /** Per-source income tracking for the current turn. */
    private final Map<IncomeSource, Long> turnIncome = new EnumMap<>(IncomeSource.class);

    /** Per-source expense tracking for the current turn. */
    private final Map<IncomeSource, Long> turnExpenses = new EnumMap<>(IncomeSource.class);

    /** Historical income per turn for trend analysis. */
    private final List<TurnEconomicRecord> turnHistory = new ArrayList<>();

    /** Base value per raw resource unit for net worth calculation. */
    private static final long RAW_RESOURCE_BASE_VALUE = 5;

    /** Base value per refined resource unit for net worth calculation. */
    private static final long REFINED_RESOURCE_BASE_VALUE = 15;

    /** Base value per construction module for net worth calculation. */
    private static final long MODULE_BASE_VALUE = 50;

    /**
     * Creates a new EconomicState for the given empire.
     *
     * @param empireId the empire ID
     * @param pipeline the resource pipeline manager for this empire
     */
    public EconomicState(int empireId, ResourcePipelineManager pipeline) {
        if (pipeline == null) {
            throw new IllegalArgumentException("Pipeline must not be null");
        }
        this.empireId = empireId;
        this.pipeline = pipeline;
        this.technologyTree = new TechnologyTree(empireId);
        this.creditBalance = 0;
    }

    /**
     * Returns the technology tree for this empire.
     *
     * @return the technology tree
     */
    public TechnologyTree getTechnologyTree() {
        return technologyTree;
    }

    // ============================================================
    // Credit Operations
    // ============================================================

    /**
     * Adds credits from a specific income source.
     *
     * @param source the income source
     * @param amount the credits to add
     */
    public void addCredits(IncomeSource source, long amount) {
        if (amount <= 0) return;
        creditBalance += amount;
        turnIncome.merge(source, amount, Long::sum);
    }

    /**
     * Spends credits for a specific expense source.
     *
     * @param source the expense source
     * @param amount the credits to spend
     * @return true if sufficient credits were available
     */
    public boolean spendCredits(IncomeSource source, long amount) {
        if (amount <= 0) return true;
        if (creditBalance < amount) {
            return false;
        }
        creditBalance -= amount;
        turnExpenses.merge(source, amount, Long::sum);
        return true;
    }

    /**
     * Returns the current credit balance.
     *
     * @return the credit balance
     */
    public long getCreditBalance() {
        return creditBalance;
    }

    /**
     * Sets the credit balance (used for initialization or restoration).
     *
     * @param balance the new credit balance
     */
    public void setCreditBalance(long balance) {
        this.creditBalance = balance;
    }

    // ============================================================
    // Resource Pipeline Access
    // ============================================================

    /**
     * Returns the resource pipeline manager.
     *
     * @return the pipeline manager
     */
    public ResourcePipelineManager getPipeline() {
        return pipeline;
    }

    /**
     * Returns the raw resource storage.
     *
     * @return the raw storage
     */
    public ResourceStorage<RawResourceType> getRawStorage() {
        return pipeline.getRawStorage();
    }

    /**
     * Returns the refined resource storage.
     *
     * @return the refined storage
     */
    public ResourceStorage<RefinedResourceType> getRefinedStorage() {
        return pipeline.getRefinedStorage();
    }

    /**
     * Returns the module storage.
     *
     * @return the module storage
     */
    public ResourceStorage<ConstructionModuleType> getModuleStorage() {
        return pipeline.getModuleStorage();
    }

    // ============================================================
    // Net Worth Calculation
    // ============================================================

    /**
     * Calculates the empire's total net worth.
     * <p>
     * Net worth = credit balance + value of all resources at base market rates.
     * </p>
     *
     * @return the total net worth in credits
     */
    public long getNetWorth() {
        long resourceValue = calculateResourceValue();
        return creditBalance + resourceValue;
    }

    /**
     * Calculates the total value of all resources in the pipeline.
     *
     * @return the resource value in credits
     */
    public long calculateResourceValue() {
        long value = 0;

        // Raw resources
        ResourceStorage<RawResourceType> rawStorage = getRawStorage();
        for (RawResourceType type : RawResourceType.values()) {
            value += rawStorage.getAmount(type) * RAW_RESOURCE_BASE_VALUE;
        }

        // Refined resources
        ResourceStorage<RefinedResourceType> refinedStorage = getRefinedStorage();
        for (RefinedResourceType type : RefinedResourceType.values()) {
            value += refinedStorage.getAmount(type) * REFINED_RESOURCE_BASE_VALUE;
        }

        // Modules
        ResourceStorage<ConstructionModuleType> moduleStorage = getModuleStorage();
        for (ConstructionModuleType type : ConstructionModuleType.values()) {
            value += moduleStorage.getAmount(type) * MODULE_BASE_VALUE;
        }

        return value;
    }

    /**
     * Returns a breakdown of resource values by type.
     *
     * @return map of resource category to value
     */
    public Map<String, Long> getResourceValueBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();

        // Raw resources
        ResourceStorage<RawResourceType> rawStorage = getRawStorage();
        for (RawResourceType type : RawResourceType.values()) {
            long amount = rawStorage.getAmount(type);
            if (amount > 0) {
                breakdown.put("RAW_" + type.name(), amount * RAW_RESOURCE_BASE_VALUE);
            }
        }

        // Refined resources
        ResourceStorage<RefinedResourceType> refinedStorage = getRefinedStorage();
        for (RefinedResourceType type : RefinedResourceType.values()) {
            long amount = refinedStorage.getAmount(type);
            if (amount > 0) {
                breakdown.put("REFINED_" + type.name(), amount * REFINED_RESOURCE_BASE_VALUE);
            }
        }

        // Modules
        ResourceStorage<ConstructionModuleType> moduleStorage = getModuleStorage();
        for (ConstructionModuleType type : ConstructionModuleType.values()) {
            long amount = moduleStorage.getAmount(type);
            if (amount > 0) {
                breakdown.put("MODULE_" + type.name(), amount * MODULE_BASE_VALUE);
            }
        }

        return breakdown;
    }

    // ============================================================
    // Turn Processing
    // ============================================================

    /**
     * Finalizes the current turn's economic data and starts a new turn.
     * <p>
     * Called at the end of each turn to record the economic state,
     * generate research points, and reset per-turn counters.
     * </p>
     *
     * @param turn the current turn number
     */
    public void finalizeTurn(int turn) {
        // Generate research points based on facility count and income
        int researchPoints = calculateResearchPoints();
        technologyTree.addResearchPoints(researchPoints);

        TurnEconomicRecord record = new TurnEconomicRecord(
                turn,
                new EnumMap<>(turnIncome),
                new EnumMap<>(turnExpenses),
                creditBalance,
                getNetWorth()
        );
        turnHistory.add(record);

        // Reset per-turn counters
        turnIncome.clear();
        turnExpenses.clear();
    }

    /**
     * Calculates research points generated this turn.
     * Based on facility count and total income.
     *
     * @return the research points generated
     */
    private int calculateResearchPoints() {
        // Base: 10 research points per turn
        int points = 10;
        // Bonus: 5 points per processing facility
        points += pipeline.getProcessingFacilities().size() * 5;
        // Bonus: 2 points per assembler
        points += pipeline.getAssemblers().size() * 2;
        return points;
    }

    // ============================================================
    // Query Methods
    // ============================================================

    /**
     * Returns the empire ID.
     *
     * @return the empire ID
     */
    public int getEmpireId() {
        return empireId;
    }

    /**
     * Returns the income from a specific source for the current turn.
     *
     * @param source the income source
     * @return the income amount
     */
    public long getTurnIncome(IncomeSource source) {
        return turnIncome.getOrDefault(source, 0L);
    }

    /**
     * Returns the total income for the current turn.
     *
     * @return the total income
     */
    public long getTotalTurnIncome() {
        return turnIncome.values().stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Returns the expense for a specific source for the current turn.
     *
     * @param source the expense source
     * @return the expense amount
     */
    public long getTurnExpense(IncomeSource source) {
        return turnExpenses.getOrDefault(source, 0L);
    }

    /**
     * Returns the total expenses for the current turn.
     *
     * @return the total expenses
     */
    public long getTotalTurnExpenses() {
        return turnExpenses.values().stream().mapToLong(Long::longValue).sum();
    }

    /**
     * Returns the net income for the current turn (income - expenses).
     *
     * @return the net income
     */
    public long getNetTurnIncome() {
        return getTotalTurnIncome() - getTotalTurnExpenses();
    }

    /**
     * Returns the turn history (immutable).
     *
     * @return list of turn economic records
     */
    public List<TurnEconomicRecord> getTurnHistory() {
        return Collections.unmodifiableList(turnHistory);
    }

    /**
     * Returns the average income over the last N turns.
     *
     * @param turns the number of turns to average
     * @return the average income per turn
     */
    public long getAverageIncome(int turns) {
        if (turnHistory.isEmpty()) return 0;
        int count = Math.min(turns, turnHistory.size());
        long total = 0;
        for (int i = turnHistory.size() - count; i < turnHistory.size(); i++) {
            total += turnHistory.get(i).totalIncome;
        }
        return total / count;
    }

    /**
     * Returns an income projection based on historical turn data.
     * <p>
     * Analyzes the last 10 turns of economic history to calculate:
     * </p>
     * <ul>
     *   <li>Income breakdown by source for the current turn</li>
     *   <li>Average income over the last N turns</li>
     *   <li>Income growth rate (comparing last 5 turns to previous 5)</li>
     *   <li>Projected income for the next 5 turns using linear trend</li>
     *   <li>Projected net worth for the next 5 turns</li>
     *   <li>Trend direction: GROWING, STABLE, or DECLINING</li>
     * </ul>
     *
     * @param currentTurn the current turn number
     * @return an income projection DTO
     */
    public com.yisy.modules.doramcu.dto.misc.IncomeProjectionDto getIncomeProjection(int currentTurn) {
        // Income by source for current turn
        Map<String, Long> incomeBySource = new LinkedHashMap<>();
        for (Map.Entry<IncomeSource, Long> entry : turnIncome.entrySet()) {
            incomeBySource.put(entry.getKey().name(), entry.getValue());
        }

        // Expenses by source for current turn
        Map<String, Long> expensesBySource = new LinkedHashMap<>();
        for (Map.Entry<IncomeSource, Long> entry : turnExpenses.entrySet()) {
            expensesBySource.put(entry.getKey().name(), entry.getValue());
        }

        long netIncome = getNetTurnIncome();
        long avgIncome = getAverageIncome(10);

        // Calculate growth rate using turn history
        double growthRate = 0.0;
        String trendDirection = "STABLE";
        List<Long> projectedIncome = new ArrayList<>();
        List<Long> projectedNetWorth = new ArrayList<>();

        if (turnHistory.size() >= 2) {
            // Compare last 5 turns average to previous 5 turns average
            int size = turnHistory.size();
            int recentCount = Math.min(5, size);
            int olderStart = Math.max(0, size - recentCount * 2);
            int olderEnd = size - recentCount;
            int recentStart = size - recentCount;

            long olderTotal = 0;
            for (int i = olderStart; i < olderEnd; i++) {
                olderTotal += turnHistory.get(i).totalIncome;
            }
            long recentTotal = 0;
            for (int i = recentStart; i < size; i++) {
                recentTotal += turnHistory.get(i).totalIncome;
            }

            int olderCount = olderEnd - olderStart;
            long olderAvg = olderCount > 0 ? olderTotal / olderCount : 0;
            long recentAvg = recentTotal / recentCount;

            if (olderAvg > 0) {
                growthRate = ((double) (recentAvg - olderAvg) / olderAvg) * 100.0;
            }

            if (growthRate > 5.0) {
                trendDirection = "GROWING";
            } else if (growthRate < -5.0) {
                trendDirection = "DECLINING";
            } else {
                trendDirection = "STABLE";
            }

            // Project next 5 turns using linear trend
            long baseIncome = Math.max(0, recentAvg);
            double growthFactor = 1.0 + (growthRate / 100.0);
            long runningCredits = creditBalance;
            long runningNetWorth;

            for (int i = 1; i <= 5; i++) {
                long projected = (long) (baseIncome * Math.pow(growthFactor, i));
                projectedIncome.add(projected);
                runningCredits += projected - netIncome; // net of expenses
                runningNetWorth = runningCredits + calculateResourceValue();
                projectedNetWorth.add(runningNetWorth);
            }
        } else {
            // Not enough history — use current values
            long baseIncome = Math.max(0, netIncome);
            for (int i = 0; i < 5; i++) {
                projectedIncome.add(baseIncome);
                long projectedCredits = creditBalance + (long) (baseIncome * (i + 1));
                projectedNetWorth.add(projectedCredits + calculateResourceValue());
            }
        }

        return new com.yisy.modules.doramcu.dto.misc.IncomeProjectionDto(
                empireId, currentTurn, creditBalance, getNetWorth(),
                incomeBySource, expensesBySource, netIncome, avgIncome,
                growthRate, projectedIncome, projectedNetWorth,
                turnHistory.size(), trendDirection
        );
    }

    // ============================================================
    // Summary
    // ============================================================

    /**
     * Returns a comprehensive summary of the economic state.
     *
     * @return a map of economic metrics
     */
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("empireId", empireId);
        summary.put("creditBalance", creditBalance);
        summary.put("netWorth", getNetWorth());
        summary.put("resourceValue", calculateResourceValue());
        summary.put("turnIncome", new LinkedHashMap<>(turnIncome));
        summary.put("turnExpenses", new LinkedHashMap<>(turnExpenses));
        summary.put("netTurnIncome", getNetTurnIncome());

        // Resource storage summary
        Map<String, Object> resources = new LinkedHashMap<>();
        ResourceStorage<RawResourceType> rawStorage = getRawStorage();
        Map<String, Integer> rawAmounts = new LinkedHashMap<>();
        for (RawResourceType type : RawResourceType.values()) {
            rawAmounts.put(type.name(), rawStorage.getAmount(type));
        }
        resources.put("raw", rawAmounts);
        resources.put("rawCapacity", rawStorage.getTotalCapacity());

        ResourceStorage<RefinedResourceType> refinedStorage = getRefinedStorage();
        Map<String, Integer> refinedAmounts = new LinkedHashMap<>();
        for (RefinedResourceType type : RefinedResourceType.values()) {
            refinedAmounts.put(type.name(), refinedStorage.getAmount(type));
        }
        resources.put("refined", refinedAmounts);
        resources.put("refinedCapacity", refinedStorage.getTotalCapacity());

        ResourceStorage<ConstructionModuleType> moduleStorage = getModuleStorage();
        Map<String, Integer> moduleAmounts = new LinkedHashMap<>();
        for (ConstructionModuleType type : ConstructionModuleType.values()) {
            moduleAmounts.put(type.name(), moduleStorage.getAmount(type));
        }
        resources.put("modules", moduleAmounts);
        resources.put("moduleCapacity", moduleStorage.getTotalCapacity());
        summary.put("resources", resources);

        // Pipeline summary
        summary.put("miningOperations", pipeline.getMiningOperationCount());
        summary.put("processingFacilities", pipeline.getProcessingFacilities().size());
        summary.put("assemblers", pipeline.getAssemblers().size());
        summary.put("constructionQueue", pipeline.getConstructionQueueSize());

        // Technology tree summary
        summary.put("technology", technologyTree.getSummary());

        return summary;
    }

    // ============================================================
    // Inner Classes
    // ============================================================

    /**
     * Income and expense sources for tracking.
     */
    public enum IncomeSource {
        /** Trade profits from commodity trading. */
        TRADE,
        /** Mercenary contract income. */
        MERCENARY,
        /** Space station income. */
        STATION,
        /** Planet facility income. */
        FACILITY,
        /** Tax or tribute from controlled systems. */
        TAX,
        /** Inter-player trade income. */
        INTER_PLAYER_TRADE,
        /** Technology research dividends. */
        TECHNOLOGY,
        /** Other miscellaneous income. */
        OTHER
    }

    /**
     * Record of economic data for a single turn.
     */
    public static class TurnEconomicRecord {
        public final int turn;
        public final Map<IncomeSource, Long> income;
        public final Map<IncomeSource, Long> expenses;
        public final long creditBalance;
        public final long netWorth;
        public final long totalIncome;
        public final long totalExpenses;

        public TurnEconomicRecord(int turn, Map<IncomeSource, Long> income,
                                   Map<IncomeSource, Long> expenses,
                                   long creditBalance, long netWorth) {
            this.turn = turn;
            this.income = income;
            this.expenses = expenses;
            this.creditBalance = creditBalance;
            this.netWorth = netWorth;
            this.totalIncome = income.values().stream().mapToLong(Long::longValue).sum();
            this.totalExpenses = expenses.values().stream().mapToLong(Long::longValue).sum();
        }

        @Override
        public String toString() {
            return String.format("TurnEconomicRecord{turn=%d, income=%d, expenses=%d, credits=%d, netWorth=%d}",
                    turn, totalIncome, totalExpenses, creditBalance, netWorth);
        }
    }
}

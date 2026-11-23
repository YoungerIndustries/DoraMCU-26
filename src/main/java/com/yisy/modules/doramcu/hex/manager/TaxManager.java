package com.yisy.modules.doramcu.hex.manager;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.MineralQuality;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.PlanetSize;
import com.yisy.modules.doramcu.hex.Player;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages tax collection from controlled planets for each empire.
 * <p>
 * The TaxManager calculates tax income based on planet characteristics:
 * population size, economic level, facility count, planet size, mineral quality,
 * and strategic resources. Each empire has a configurable tax policy that affects
 * the tax rate and collection efficiency.
 * </p>
 * <p>
 * Tax policies range from NONE (no taxes) to MAXIMUM (highest rate but reduced
 * collection efficiency due to resistance). The collected taxes are fed into
 * the empire's EconomicState via the TAX income source.
 * </p>
 * <p>
 * Based on 4X game design patterns from Stellaris, Galactic Civilizations IV,
 * and Master of Orion where planetary taxation is a core income mechanic.
 * </p>
 */
public class TaxManager {

    private final GameContext context;

    /** Reference to CreditExchangeService for EconomicState access. */
    private CreditExchangeService creditExchangeService;

    /** Per-empire tax policy settings. */
    private final Map<Integer, TaxPolicy> empireTaxPolicies = new HashMap<>();

    /** Per-empire tax collection history (last 10 turns). */
    private final Map<Integer, Deque<Long>> empireTaxHistory = new HashMap<>();

    /** Maximum history size per empire. */
    static final int MAX_TAX_HISTORY_SIZE = 10;

    /**
     * Tax policy levels that determine the tax rate and collection efficiency.
     */
    public enum TaxPolicy {
        /** No taxes collected. Rate: 0%. */
        NONE(0.0, 1.0, "No taxes"),

        /** Light taxation — minimal burden on population. Rate: 5%. */
        LOW(0.05, 1.0, "Low taxes"),

        /** Moderate taxation — balanced income and satisfaction. Rate: 10%. */
        MEDIUM(0.10, 1.0, "Medium taxes"),

        /** High taxation — significant income with some resistance. Rate: 18%, efficiency: 90%. */
        HIGH(0.18, 0.9, "High taxes"),

        /** Maximum taxation — highest rate with notable resistance. Rate: 25%, efficiency: 75%. */
        MAXIMUM(0.25, 0.75, "Maximum taxes");

        private final double taxRate;
        private final double collectionEfficiency;
        private final String displayName;

        TaxPolicy(double taxRate, double collectionEfficiency, String displayName) {
            this.taxRate = taxRate;
            this.collectionEfficiency = collectionEfficiency;
            this.displayName = displayName;
        }

        /** @return the tax rate as a fraction (0.0 to 1.0) */
        public double getTaxRate() {
            return taxRate;
        }

        /** @return the collection efficiency multiplier (resistance reduces actual collection) */
        public double getCollectionEfficiency() {
            return collectionEfficiency;
        }

        /** @return the human-readable display name */
        public String getDisplayName() {
            return displayName;
        }
    }

    /** Base tax value per population unit. */
    static final long BASE_TAX_PER_POPULATION = 10;

    /** Base tax value per economic level. */
    static final long BASE_TAX_PER_ECONOMIC_LEVEL = 500;

    /** Base tax value per facility on the planet. */
    static final long BASE_TAX_PER_FACILITY = 200;

    /** Base tax value per strategic resource deposit. */
    static final long BASE_TAX_PER_STRATEGIC_RESOURCE = 300;

    /** Capital planet tax bonus multiplier. */
    static final double CAPITAL_TAX_BONUS = 1.5;

    /**
     * Creates a new TaxManager.
     *
     * @param context the shared game context
     */
    public TaxManager(GameContext context) {
        this.context = context;
    }

    /**
     * Sets the CreditExchangeService for EconomicState integration.
     *
     * @param service the credit exchange service
     */
    public void setCreditExchangeService(CreditExchangeService service) {
        this.creditExchangeService = service;
    }

    // ============================================================
    // Tax Policy Management
    // ============================================================

    /**
     * Returns the tax policy for an empire. Defaults to MEDIUM if not set.
     *
     * @param empireId the empire ID
     * @return the current tax policy
     */
    public TaxPolicy getTaxPolicy(int empireId) {
        return empireTaxPolicies.getOrDefault(empireId, TaxPolicy.MEDIUM);
    }

    /**
     * Sets the tax policy for an empire.
     *
     * @param empireId the empire ID
     * @param policy the tax policy
     */
    public void setTaxPolicy(int empireId, TaxPolicy policy) {
        if (policy == null) {
            throw new IllegalArgumentException("Tax policy must not be null");
        }
        empireTaxPolicies.put(empireId, policy);
    }

    // ============================================================
    // Tax Calculation
    // ============================================================

    /**
     * Calculates the tax income for a single planet.
     * <p>
     * The tax is based on:
     * </p>
     * <ul>
     *   <li>Population: more population = more taxable income</li>
     *   <li>Economic level: higher development = higher base income</li>
     *   <li>Facilities: more infrastructure = more economic activity</li>
     *   <li>Planet size: larger planets have higher base output</li>
     *   <li>Mineral quality: richer deposits increase value</li>
     *   <li>Strategic resources: rare deposits add premium value</li>
     *   <li>Capital bonus: capital planets generate 50% more tax</li>
     * </ul>
     *
     * @param planet the planet to calculate tax for
     * @param policy the tax policy applied
     * @return the tax income in credits
     */
    public long calculatePlanetTax(Planet planet, TaxPolicy policy) {
        if (planet == null || policy == TaxPolicy.NONE) {
            return 0;
        }

        long baseTax = 0;

        // Population-based tax
        int population = planet.getCurrentPopulation();
        baseTax += (long) population * BASE_TAX_PER_POPULATION;

        // Economic level tax
        int economicLevel = planet.getEconomicLevel();
        baseTax += (long) economicLevel * BASE_TAX_PER_ECONOMIC_LEVEL;

        // Facility-based tax (economic activity from infrastructure)
        int facilityCount = planet.getStartingFacilities().size();
        baseTax += (long) facilityCount * BASE_TAX_PER_FACILITY;

        // Strategic resource premium
        int strategicResourceCount = planet.getStrategicDeposits().size();
        baseTax += (long) strategicResourceCount * BASE_TAX_PER_STRATEGIC_RESOURCE;

        // Planet size multiplier (larger planets have more economic activity)
        PlanetSize size = planet.getPlanetSize();
        double sizeMultiplier = 1.0;
        switch (size) {
            case TINY -> sizeMultiplier = 0.5;
            case SMALL -> sizeMultiplier = 0.75;
            case MEDIUM -> sizeMultiplier = 1.0;
            case LARGE -> sizeMultiplier = 1.25;
            case HUGE -> sizeMultiplier = 1.5;
        }

        // Mineral quality bonus
        MineralQuality quality = planet.getMineralQuality();
        double qualityMultiplier = 1.0 + quality.getProductionBonus();

        // Apply multipliers
        double totalMultiplier = sizeMultiplier * qualityMultiplier;
        long adjustedTax = (long) (baseTax * totalMultiplier);

        // Capital bonus
        if (planet.isCapital()) {
            adjustedTax = (long) (adjustedTax * CAPITAL_TAX_BONUS);
        }

        // Apply tax policy rate and efficiency
        double policyRate = policy.getTaxRate();
        double policyEfficiency = policy.getCollectionEfficiency();
        long collectedTax = (long) (adjustedTax * policyRate * policyEfficiency);

        return Math.max(0, collectedTax);
    }

    /**
     * Calculates the total tax income for an empire across all controlled planets.
     *
     * @param empireId the empire ID
     * @return the total tax income in credits
     */
    public long calculateEmpireTax(int empireId) {
        TaxPolicy policy = getTaxPolicy(empireId);
        if (policy == TaxPolicy.NONE) {
            return 0;
        }

        return context.getPlanets().values().stream()
                .filter(p -> Integer.valueOf(empireId).equals(p.getOwnerEmpireId()))
                .mapToLong(p -> calculatePlanetTax(p, policy))
                .sum();
    }

    /**
     * Returns the list of planets owned by an empire with their individual tax values.
     *
     * @param empireId the empire ID
     * @return list of planet tax summaries
     */
    public List<PlanetTaxSummary> getPlanetTaxSummaries(int empireId) {
        TaxPolicy policy = getTaxPolicy(empireId);
        return context.getPlanets().values().stream()
                .filter(p -> Integer.valueOf(empireId).equals(p.getOwnerEmpireId()))
                .map(planet -> new PlanetTaxSummary(
                        planet.getId(),
                        planet.getName(),
                        planet.getCurrentPopulation(),
                        planet.getEconomicLevel(),
                        planet.getStartingFacilities().size(),
                        planet.getPlanetSize(),
                        planet.getMineralQuality(),
                        planet.getStrategicDeposits().size(),
                        planet.isCapital(),
                        calculatePlanetTax(planet, policy)
                ))
                .sorted(Comparator.comparingLong(PlanetTaxSummary::getTaxAmount).reversed())
                .collect(Collectors.toList());
    }

    // ============================================================
    // Turn Processing
    // ============================================================

    /**
     * Processes tax collection for all empires at the end of the turn.
     * <p>
     * For each empire, collects taxes from all controlled planets and feeds
     * the income into the empire's EconomicState. The tax amount is recorded
     * in the empire's tax history for trend analysis.
     * </p>
     */
    public void processEndOfTurn() {
        for (Player player : context.getPlayers()) {
            if (player.isEliminated()) continue;

            Integer empireIdObj = player.getEmpireId();
            if (empireIdObj == null) continue;
            int empireId = empireIdObj;
            long taxIncome = collectTax(empireId);

            // Record in history
            empireTaxHistory.computeIfAbsent(empireId, k -> new ArrayDeque<>());
            Deque<Long> history = empireTaxHistory.get(empireId);
            history.addLast(taxIncome);
            if (history.size() > MAX_TAX_HISTORY_SIZE) {
                history.removeFirst();
            }
        }
    }

    /**
     * Collects tax for a specific empire and feeds it into EconomicState.
     *
     * @param empireId the empire ID
     * @return the total tax collected
     */
    private long collectTax(int empireId) {
        TaxPolicy policy = getTaxPolicy(empireId);
        if (policy == TaxPolicy.NONE) {
            return 0;
        }

        long totalTax = 0;
        for (Planet planet : context.getPlanets().values()) {
            if (Integer.valueOf(empireId).equals(planet.getOwnerEmpireId())) {
                long planetTax = calculatePlanetTax(planet, policy);
                totalTax += planetTax;
            }
        }

        // Feed into EconomicState
        if (creditExchangeService != null && totalTax > 0) {
            EconomicState state = creditExchangeService.getEconomicState(empireId);
            if (state != null) {
                state.addCredits(EconomicState.IncomeSource.TAX, totalTax);
            }
        }

        return totalTax;
    }

    // ============================================================
    // Tax History and Projections
    // ============================================================

    /**
     * Returns the tax collection history for an empire (last 10 turns).
     *
     * @param empireId the empire ID
     * @return list of tax amounts, most recent last
     */
    public List<Long> getTaxHistory(int empireId) {
        Deque<Long> history = empireTaxHistory.get(empireId);
        if (history == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(history);
    }

    /**
     * Returns the average tax income over the recorded history.
     *
     * @param empireId the empire ID
     * @return the average tax income, or 0 if no history
     */
    public long getAverageTaxIncome(int empireId) {
        Deque<Long> history = empireTaxHistory.get(empireId);
        if (history == null || history.isEmpty()) {
            return 0;
        }
        return (long) history.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }

    /**
     * Returns a projected tax income for the next turn based on current state.
     * This is a static projection that does not account for future changes.
     *
     * @param empireId the empire ID
     * @return the projected tax income
     */
    public long getProjectedTaxIncome(int empireId) {
        return calculateEmpireTax(empireId);
    }

    // ============================================================
    // Summary
    // ============================================================

    /**
     * Returns a comprehensive summary of the tax system for an empire.
     *
     * @param empireId the empire ID
     * @return a map of tax metrics
     */
    public Map<String, Object> getTaxSummary(int empireId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        TaxPolicy policy = getTaxPolicy(empireId);
        summary.put("empireId", empireId);
        summary.put("taxPolicy", policy.name());
        summary.put("taxPolicyDisplay", policy.getDisplayName());
        summary.put("taxRate", policy.getTaxRate());
        summary.put("collectionEfficiency", policy.getCollectionEfficiency());
        summary.put("currentTaxIncome", calculateEmpireTax(empireId));
        summary.put("projectedTaxIncome", getProjectedTaxIncome(empireId));
        summary.put("averageTaxIncome", getAverageTaxIncome(empireId));
        summary.put("taxHistory", getTaxHistory(empireId));

        // Planet breakdown
        List<PlanetTaxSummary> planetSummaries = getPlanetTaxSummaries(empireId);
        summary.put("planets", planetSummaries);
        summary.put("controlledPlanetCount", planetSummaries.size());

        return summary;
    }

    // ============================================================
    // Inner Classes
    // ============================================================

    /**
     * Summary of tax data for a single planet.
     */
    public static class PlanetTaxSummary {
        private final int planetId;
        private final String planetName;
        private final int population;
        private final int economicLevel;
        private final int facilityCount;
        private final PlanetSize size;
        private final MineralQuality mineralQuality;
        private final int strategicResourceCount;
        private final boolean isCapital;
        private final long taxAmount;

        public PlanetTaxSummary(int planetId, String planetName, int population,
                                 int economicLevel, int facilityCount, PlanetSize size,
                                 MineralQuality mineralQuality, int strategicResourceCount,
                                 boolean isCapital, long taxAmount) {
            this.planetId = planetId;
            this.planetName = planetName;
            this.population = population;
            this.economicLevel = economicLevel;
            this.facilityCount = facilityCount;
            this.size = size;
            this.mineralQuality = mineralQuality;
            this.strategicResourceCount = strategicResourceCount;
            this.isCapital = isCapital;
            this.taxAmount = taxAmount;
        }

        public int getPlanetId() {
            return planetId;
        }

        public String getPlanetName() {
            return planetName;
        }

        public int getPopulation() {
            return population;
        }

        public int getEconomicLevel() {
            return economicLevel;
        }

        public int getFacilityCount() {
            return facilityCount;
        }

        public PlanetSize getSize() {
            return size;
        }

        public MineralQuality getMineralQuality() {
            return mineralQuality;
        }

        public int getStrategicResourceCount() {
            return strategicResourceCount;
        }

        public boolean isCapital() {
            return isCapital;
        }

        public long getTaxAmount() {
            return taxAmount;
        }

        @Override
        public String toString() {
            return String.format("PlanetTaxSummary{name=%s, pop=%d, econ=%d, facilities=%d, tax=%d}",
                    planetName, population, economicLevel, facilityCount, taxAmount);
        }
    }
}

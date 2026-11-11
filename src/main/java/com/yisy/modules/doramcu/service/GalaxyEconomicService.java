package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.eve.model.Treasury;
import com.yisy.modules.doramcu.hex.Empire;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.SpaceStationEconomy;
import com.yisy.modules.doramcu.model.Galaxy;
import com.yisy.modules.doramcu.model.GalaxyBond;
import com.yisy.modules.doramcu.model.GalaxyTreasury;
import com.yisy.modules.doramcu.model.GalaxyTreasuryCycle;
import com.yisy.modules.doramcu.repository.GalaxyBondRepository;
import com.yisy.modules.doramcu.repository.GalaxyRepository;
import com.yisy.modules.doramcu.repository.GalaxyTreasuryCycleRepository;
import com.yisy.modules.doramcu.repository.GalaxyTreasuryRepository;
import com.yisy.modules.doramcu.repository.TreasuryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * Service that orchestrates the galaxy-wide economic cycle.
 * <p>
 * Each cycle performs the following steps:
 * </p>
 * <ol>
 *   <li><b>Collect Taxes</b> — Each empire calculates its total revenue from planets, stations,
 *       and trade. 5% of that revenue is paid to the Galaxy Treasury.</li>
 *   <li><b>Empire Expenses</b> — Empires pay their own bills, payroll, and upkeep from the
 *       remaining 95% of their revenue.</li>
 *   <li><b>Galaxy Improvements</b> — The Galaxy Treasury pays for galaxy-wide improvements
 *       resulting from planet operations and infrastructure needs.</li>
 *   <li><b>Surplus Transfer</b> — Any remaining balance in the Galaxy Treasury is transferred
 *       to the owning Corporation's treasury.</li>
 *   <li><b>Deficit Bonds</b> — If the Galaxy Treasury cannot cover all improvement costs,
 *       the shortfall is issued as Galaxy Bonds at 10% interest until the deficit is corrected.</li>
 * </ol>
 * <p>
 * Bonds accrue interest each cycle and are redeemed automatically when surplus is available.
 * </p>
 */
@Service
public class GalaxyEconomicService {

    private static final Logger log = LoggerFactory.getLogger(GalaxyEconomicService.class);

    /** Default tax rate: 5% of empire revenue goes to the galaxy treasury. */
    public static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("0.05");

    /** Default bond interest rate: 10% annual. */
    public static final BigDecimal DEFAULT_BOND_INTEREST_RATE = new BigDecimal("0.10");

    /** Base tax revenue per planet owned by an empire. */
    private static final int BASE_PLANET_TAX = 10000;

    /** Base tax revenue per space station owned by an empire. */
    private static final int BASE_STATION_TAX = 5000;

    /** Base upkeep cost per army unit. */
    private static final int ARMY_UPKEEP_COST = 500;

    /** Base upkeep cost per navy ship. */
    private static final int NAVY_UPKEEP_COST = 1000;

    /** Base upkeep cost per facility. */
    private static final int FACILITY_UPKEEP_COST = 2000;

    /** Base improvement cost per star system. */
    private static final int SYSTEM_IMPROVEMENT_COST = 3000;

    private final GalaxyTreasuryRepository galaxyTreasuryRepository;
    private final GalaxyBondRepository galaxyBondRepository;
    private final GalaxyTreasuryCycleRepository galaxyTreasuryCycleRepository;
    private final GalaxyRepository galaxyRepository;
    private final TreasuryRepository treasuryRepository;

    public GalaxyEconomicService(
            GalaxyTreasuryRepository galaxyTreasuryRepository,
            GalaxyBondRepository galaxyBondRepository,
            GalaxyTreasuryCycleRepository galaxyTreasuryCycleRepository,
            GalaxyRepository galaxyRepository,
            TreasuryRepository treasuryRepository) {
        this.galaxyTreasuryRepository = galaxyTreasuryRepository;
        this.galaxyBondRepository = galaxyBondRepository;
        this.galaxyTreasuryCycleRepository = galaxyTreasuryCycleRepository;
        this.galaxyRepository = galaxyRepository;
        this.treasuryRepository = treasuryRepository;
    }

    // ==================== MAIN FISCAL CYCLE ====================

    /**
     * Process one complete fiscal cycle for a galaxy.
     * <p>
     * This is the main entry point for the economic cycle. It performs all steps:
     * tax collection, empire expenses, galaxy improvements, surplus transfer,
     * and deficit bond issuance.
     * </p>
     *
     * @param galaxyId the galaxy ID
     * @param cycleNumber the current cycle/turn number
     * @param empires the empires in the galaxy
     * @param improvementCosts the galaxy-wide improvement costs for this cycle
     * @return the cycle record with all details
     */
    @Transactional
    public GalaxyTreasuryCycle processFiscalCycle(
            Long galaxyId,
            Long cycleNumber,
            List<Empire> empires,
            Map<String, BigDecimal> improvementCosts) {

        log.info("Processing fiscal cycle {} for galaxy {}", cycleNumber, galaxyId);

        // Ensure galaxy treasury exists
        GalaxyTreasury treasury = ensureGalaxyTreasury(galaxyId);

        // Create cycle record
        Galaxy galaxy = galaxyRepository.findById(galaxyId)
                .orElseThrow(() -> new IllegalArgumentException("Galaxy not found: " + galaxyId));
        GalaxyTreasuryCycle cycle = new GalaxyTreasuryCycle(galaxy, cycleNumber, treasury.getBalance());

        // Step 1: Collect taxes from empires
        BigDecimal totalTaxCollected = collectTaxes(treasury, cycle, empires);
        log.info("Cycle {}: Collected {} credits in taxes from {} empires",
                cycleNumber, totalTaxCollected, empires.size());

        // Step 2: Empires pay their own expenses (handled externally — empires manage their own budgets)
        // The 95% remaining after tax is available for empire expenses

        // Step 3: Pay galaxy improvements
        BigDecimal totalImprovements = payImprovements(treasury, cycle, improvementCosts);
        log.info("Cycle {}: Paid {} credits in galaxy improvements", cycleNumber, totalImprovements);

        // Step 4: Accrue interest on outstanding bonds and track cost in cycle
        BigDecimal bondInterestCost = accrueBondInterest(galaxyId);
        if (bondInterestCost.compareTo(BigDecimal.ZERO) > 0) {
            cycle.recordImprovement("Bond Interest", bondInterestCost);
        }

        // Step 5: Try to redeem bonds with surplus
        BigDecimal bondRedemption = redeemBondsWithSurplus(treasury, galaxyId, cycleNumber);
        log.info("Cycle {}: Redeemed {} credits in bonds", cycleNumber, bondRedemption);

        // Step 6: Transfer surplus or issue deficit bonds
        BigDecimal balanceAfterImprovements = treasury.getBalance();
        if (balanceAfterImprovements.compareTo(BigDecimal.ZERO) > 0) {
            // Surplus — transfer to corporation
            transferSurplusToCorporation(treasury, galaxy, balanceAfterImprovements, cycle);
            log.info("Cycle {}: Transferred {} credits surplus to corporation",
                    cycleNumber, balanceAfterImprovements);
        } else if (balanceAfterImprovements.compareTo(BigDecimal.ZERO) < 0) {
            // Deficit — issue bonds
            issueDeficitBonds(treasury, galaxy, balanceAfterImprovements.abs(), cycleNumber, cycle);
            log.info("Cycle {}: Issued {} credits in deficit bonds",
                    cycleNumber, balanceAfterImprovements.abs());
        }

        // Finalize cycle record
        cycle.setBalanceEnd(treasury.getBalance());
        cycle.finalizeCycle();
        galaxyTreasuryCycleRepository.save(cycle);

        // Update treasury's last processed cycle
        treasury.setLastProcessedCycle(cycleNumber);
        galaxyTreasuryRepository.save(treasury);

        log.info("Cycle {} complete for galaxy {}: balance={}, tax={}, improvements={}, surplus={}, deficit={}",
                cycleNumber, galaxyId, cycle.getBalanceEnd(), cycle.getTaxRevenue(),
                cycle.getImprovementCosts(), cycle.getSurplusTransferred(), cycle.getDeficitBonds());

        return cycle;
    }

    // ==================== STEP 1: TAX COLLECTION ====================

    /**
     * Collect taxes from all empires.
     * <p>
     * Each empire's revenue is calculated from its planets and stations.
     * 5% of the revenue is paid to the galaxy treasury.
     * </p>
     *
     * @param treasury the galaxy treasury
     * @param cycle the current cycle record
     * @param empires the list of empires
     * @return total tax collected
     */
    private BigDecimal collectTaxes(GalaxyTreasury treasury, GalaxyTreasuryCycle cycle, List<Empire> empires) {
        BigDecimal totalTax = BigDecimal.ZERO;

        for (Empire empire : empires) {
            BigDecimal revenue = calculateEmpireRevenue(empire);
            BigDecimal tax = revenue.multiply(treasury.getTaxRate())
                    .setScale(2, RoundingMode.HALF_UP);

            if (tax.compareTo(BigDecimal.ZERO) > 0) {
                treasury.deposit(tax);
                cycle.recordEmpireTax(empire.getId(), tax);
                totalTax = totalTax.add(tax);
                treasury.setTotalTaxesCollected(treasury.getTotalTaxesCollected().add(tax));
            }
        }

        return totalTax;
    }

    /**
     * Calculate an empire's total revenue from its planets and stations.
     *
     * @param empire the empire
     * @return total revenue in credits
     */
    public BigDecimal calculateEmpireRevenue(Empire empire) {
        BigDecimal revenue = BigDecimal.ZERO;

        // Revenue from planets
        int planetCount = empire.getPlanetCount();
        revenue = revenue.add(BigDecimal.valueOf(planetCount * BASE_PLANET_TAX));

        // Revenue from space stations (if accessible)
        // Note: Space stations are tracked in the galaxy, not directly in the empire
        // This is a simplified calculation; actual implementation may vary

        return revenue;
    }

    /**
     * Calculate an empire's total revenue including station income.
     *
     * @param empire the empire
     * @param stations the space stations owned by the empire
     * @return total revenue in credits
     */
    public BigDecimal calculateEmpireRevenue(Empire empire, List<SpaceStation> stations) {
        BigDecimal revenue = calculateEmpireRevenue(empire);

        // Add station income
        for (SpaceStation station : stations) {
            if (!station.isDestroyed()) {
                SpaceStationEconomy economy = new SpaceStationEconomy(station);
                revenue = revenue.add(BigDecimal.valueOf(economy.getIncome()));
            }
        }

        return revenue;
    }

    // ==================== STEP 2: EMPIRE EXPENSES ====================

    /**
     * Calculate an empire's total expenses (upkeep costs).
     * <p>
     * Empires pay their own expenses from the 95% of revenue they retain.
     * Expenses include army units, navy ships, and facilities.
     * </p>
     *
     * @param empire the empire
     * @return total expenses in credits
     */
    public BigDecimal calculateEmpireExpenses(Empire empire) {
        BigDecimal expenses = BigDecimal.ZERO;

        // Army upkeep: count of army units * cost per unit
        int armyCount = empire.getArmy().getUnitCount();
        expenses = expenses.add(BigDecimal.valueOf(armyCount * ARMY_UPKEEP_COST));

        // Navy upkeep: count of navy ships * cost per ship
        int navyCount = empire.getNavy().getTotalShipCount();
        expenses = expenses.add(BigDecimal.valueOf(navyCount * NAVY_UPKEEP_COST));

        // Facility upkeep: count of space stations * cost per facility
        int facilityCount = empire.getFacilities().getSpaceStationCount();
        expenses = expenses.add(BigDecimal.valueOf(facilityCount * FACILITY_UPKEEP_COST));

        return expenses;
    }

    /**
     * Calculate an empire's net income after taxes and expenses.
     *
     * @param empire the empire
     * @param stations the space stations owned by the empire
     * @param taxRate the tax rate
     * @return net income (revenue - tax - expenses)
     */
    public BigDecimal calculateEmpireNetIncome(Empire empire, List<SpaceStation> stations, BigDecimal taxRate) {
        BigDecimal revenue = calculateEmpireRevenue(empire, stations);
        BigDecimal tax = revenue.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expenses = calculateEmpireExpenses(empire);
        return revenue.subtract(tax).subtract(expenses);
    }

    // ==================== STEP 3: GALAXY IMPROVEMENTS ====================

    /**
     * Pay for galaxy-wide improvements from the treasury.
     *
     * @param treasury the galaxy treasury
     * @param cycle the current cycle record
     * @param improvementCosts map of improvement descriptions to costs
     * @return total improvements paid
     */
    private BigDecimal payImprovements(
            GalaxyTreasury treasury,
            GalaxyTreasuryCycle cycle,
            Map<String, BigDecimal> improvementCosts) {

        if (improvementCosts == null || improvementCosts.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalImprovements = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : improvementCosts.entrySet()) {
            String description = entry.getKey();
            BigDecimal cost = entry.getValue();

            if (cost.compareTo(BigDecimal.ZERO) > 0) {
                // Try to withdraw from treasury; if insufficient, the balance goes negative
                boolean withdrew = treasury.withdraw(cost);
                cycle.recordImprovement(description, cost);
                totalImprovements = totalImprovements.add(cost);

                if (!withdrew) {
                    // Treasury went negative — this will be handled as deficit bonds later
                    log.warn("Galaxy treasury insufficient for improvement: {} (cost: {})", description, cost);
                }
            }
        }

        treasury.setTotalImprovementCosts(treasury.getTotalImprovementCosts().add(totalImprovements));
        return totalImprovements;
    }

    /**
     * Calculate default improvement costs based on the number of planets in a galaxy.
     * <p>
     * This is a convenience method for when no specific improvement costs are provided.
     * </p>
     *
     * @param galaxy the galaxy
     * @return map of improvement descriptions to costs
     */
    public Map<String, BigDecimal> calculateDefaultImprovementCosts(Galaxy galaxy) {
        Map<String, BigDecimal> costs = new HashMap<>();

        // Infrastructure maintenance based on galaxy size
        int systemCount = galaxy.getSystemCount() != null ? galaxy.getSystemCount() : 100;
        BigDecimal infrastructureCost = BigDecimal.valueOf(systemCount * SYSTEM_IMPROVEMENT_COST);

        costs.put("Infrastructure Maintenance", infrastructureCost);
        return costs;
    }

    // ==================== STEP 4: BOND INTEREST ====================

    /**
     * Accrue interest on all outstanding bonds for a galaxy.
     *
     * @param galaxyId the galaxy ID
     * @return total interest cost accrued across all bonds
     */
    private BigDecimal accrueBondInterest(Long galaxyId) {
        List<GalaxyBond> bonds = galaxyBondRepository.findOutstandingByGalaxyId(galaxyId);
        BigDecimal totalInterestCost = BigDecimal.ZERO;

        for (GalaxyBond bond : bonds) {
            BigDecimal outstandingBefore = bond.getTotalOutstanding();
            bond.accrueInterest(1);
            BigDecimal interestAccrued = bond.getTotalOutstanding().subtract(outstandingBefore);
            totalInterestCost = totalInterestCost.add(interestAccrued);
            galaxyBondRepository.save(bond);
        }

        return totalInterestCost;
    }

    // ==================== STEP 5: BOND REDEMPTION ====================

    /**
     * Try to redeem outstanding bonds using surplus treasury funds.
     *
     * @param treasury the galaxy treasury
     * @param galaxyId the galaxy ID
     * @param cycleNumber the current cycle number
     * @return total amount used for bond redemption
     */
    private BigDecimal redeemBondsWithSurplus(
            GalaxyTreasury treasury,
            Long galaxyId,
            Long cycleNumber) {

        List<GalaxyBond> bonds = galaxyBondRepository.findOutstandingByGalaxyId(galaxyId);
        BigDecimal totalRedeemed = BigDecimal.ZERO;

        for (GalaxyBond bond : bonds) {
            if (treasury.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                break; // No more surplus to redeem with
            }

            BigDecimal outstanding = bond.getTotalOutstanding();
            BigDecimal available = treasury.getBalance();
            BigDecimal redemptionAmount = available.min(outstanding);

            bond.redeem(redemptionAmount);
            bond.setRedeemedCycle(cycleNumber);
            galaxyBondRepository.save(bond);

            // Note: redeem() already adjusts the bond's state
            // We need to adjust the treasury balance
            treasury.withdraw(redemptionAmount);
            totalRedeemed = totalRedeemed.add(redemptionAmount);
        }

        return totalRedeemed;
    }

    // ==================== STEP 6: SURPLUS TRANSFER ====================

    /**
     * Transfer surplus from the galaxy treasury to the owning corporation's treasury.
     *
     * @param treasury the galaxy treasury
     * @param galaxy the galaxy
     * @param surplus the surplus amount
     * @param cycle the current cycle record
     */
    private void transferSurplusToCorporation(
            GalaxyTreasury treasury,
            Galaxy galaxy,
            BigDecimal surplus,
            GalaxyTreasuryCycle cycle) {

        // Find the owning corporation
        EveCorporation corporation = galaxy.getOwnedByCorporation();
        if (corporation == null) {
            log.warn("Galaxy {} has no owning corporation — surplus retained in galaxy treasury", galaxy.getId());
            return;
        }

        // Transfer to corporation's treasury using the existing Treasury table
        String corpOwnerId = String.valueOf(corporation.getId());
        List<Treasury> corpTreasuries = treasuryRepository.findByOwnerIdAndOwnerType(corpOwnerId, "CORPORATION");

        // Find or create CREDITS treasury entry
        Treasury creditsTreasury = corpTreasuries.stream()
                .filter(t -> "CREDITS".equals(t.getResourceType()))
                .findFirst()
                .orElse(null);

        if (creditsTreasury == null) {
            creditsTreasury = new Treasury(
                    "CREDITS",
                    surplus,
                    BigDecimal.ONE,
                    corpOwnerId,
                    "CORPORATION"
            );
        } else {
            BigDecimal newQuantity = creditsTreasury.getQuantityOunces().add(surplus);
            creditsTreasury.setQuantityOunces(newQuantity);
        }

        treasuryRepository.save(creditsTreasury);

        // Clear the galaxy treasury balance
        treasury.withdraw(surplus);
        treasury.setTotalSurplusTransferred(treasury.getTotalSurplusTransferred().add(surplus));
        cycle.setSurplusTransferred(surplus);
    }

    // ==================== DEFICIT BONDS ====================

    /**
     * Issue Galaxy Bonds to cover a deficit.
     *
     * @param treasury the galaxy treasury
     * @param galaxy the galaxy
     * @param deficit the deficit amount
     * @param cycleNumber the current cycle number
     * @param cycle the current cycle record
     */
    private void issueDeficitBonds(
            GalaxyTreasury treasury,
            Galaxy galaxy,
            BigDecimal deficit,
            Long cycleNumber,
            GalaxyTreasuryCycle cycle) {

        GalaxyBond bond = new GalaxyBond(
                galaxy,
                deficit,
                treasury.getBondInterestRate(),
                cycleNumber
        );
        bond.setNotes("Deficit bond issued for cycle " + cycleNumber);
        galaxyBondRepository.save(bond);

        treasury.setTotalDeficitBonds(treasury.getTotalDeficitBonds().add(deficit));
        cycle.setDeficitBonds(deficit);

        // Reset treasury balance to zero (deficit covered by bond)
        treasury.setBalance(BigDecimal.ZERO);

        log.info("Issued Galaxy Bond for {} credits (deficit from cycle {})", deficit, cycleNumber);
    }

    // ==================== TREASURY MANAGEMENT ====================

    /**
     * Ensure a galaxy has a treasury record, creating one if necessary.
     *
     * @param galaxyId the galaxy ID
     * @return the galaxy treasury
     */
    public GalaxyTreasury ensureGalaxyTreasury(Long galaxyId) {
        return galaxyTreasuryRepository.findByGalaxyId(galaxyId).orElseGet(() -> {
            Galaxy galaxy = galaxyRepository.findById(galaxyId)
                    .orElseThrow(() -> new IllegalArgumentException("Galaxy not found: " + galaxyId));
            GalaxyTreasury treasury = new GalaxyTreasury(galaxy);
            return galaxyTreasuryRepository.save(treasury);
        });
    }

    /**
     * Get the current treasury for a galaxy.
     *
     * @param galaxyId the galaxy ID
     * @return the treasury or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<GalaxyTreasury> getGalaxyTreasury(Long galaxyId) {
        return galaxyTreasuryRepository.findByGalaxyId(galaxyId);
    }

    /**
     * Get all outstanding bonds for a galaxy.
     *
     * @param galaxyId the galaxy ID
     * @return list of outstanding bonds
     */
    @Transactional(readOnly = true)
    public List<GalaxyBond> getOutstandingBonds(Long galaxyId) {
        return galaxyBondRepository.findOutstandingByGalaxyId(galaxyId);
    }

    /**
     * Get the total outstanding debt for a galaxy.
     *
     * @param galaxyId the galaxy ID
     * @return total outstanding debt
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalOutstandingDebt(Long galaxyId) {
        return galaxyBondRepository.sumTotalOutstandingByGalaxyId(galaxyId);
    }

    /**
     * Get the latest fiscal cycle for a galaxy.
     *
     * @param galaxyId the galaxy ID
     * @return the latest cycle or null
     */
    @Transactional(readOnly = true)
    public GalaxyTreasuryCycle getLatestCycle(Long galaxyId) {
        return galaxyTreasuryCycleRepository.findLatestByGalaxyId(galaxyId);
    }

    /**
     * Get all fiscal cycles for a galaxy.
     *
     * @param galaxyId the galaxy ID
     * @return list of cycles ordered by cycle number
     */
    @Transactional(readOnly = true)
    public List<GalaxyTreasuryCycle> getAllCycles(Long galaxyId) {
        return galaxyTreasuryCycleRepository.findByGalaxyIdOrderByCycleNumberAsc(galaxyId);
    }

    /**
     * Get fiscal summary for a galaxy.
     *
     * @param galaxyId the galaxy ID
     * @return map of summary fields
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getFiscalSummary(Long galaxyId) {
        GalaxyTreasury treasury = galaxyTreasuryRepository.findByGalaxyId(galaxyId).orElse(null);
        BigDecimal totalDebt = galaxyBondRepository.sumTotalOutstandingByGalaxyId(galaxyId);
        long bondCount = galaxyBondRepository.countOutstandingByGalaxyId(galaxyId);

        Map<String, Object> summary = new HashMap<>();
        if (treasury != null) {
            summary.put("balance", treasury.getBalance());
            summary.put("totalTaxesCollected", treasury.getTotalTaxesCollected());
            summary.put("totalImprovementCosts", treasury.getTotalImprovementCosts());
            summary.put("totalSurplusTransferred", treasury.getTotalSurplusTransferred());
            summary.put("totalDeficitBonds", treasury.getTotalDeficitBonds());
            summary.put("taxRate", treasury.getTaxRate());
            summary.put("bondInterestRate", treasury.getBondInterestRate());
            summary.put("lastProcessedCycle", treasury.getLastProcessedCycle());
        }
        summary.put("totalOutstandingDebt", totalDebt);
        summary.put("outstandingBondCount", bondCount);
        return summary;
    }

    /**
     * Process a fiscal cycle with default improvement costs.
     *
     * @param galaxyId the galaxy ID
     * @param cycleNumber the cycle number
     * @param empires the empires in the galaxy
     * @return the cycle record
     */
    @Transactional
    public GalaxyTreasuryCycle processFiscalCycle(Long galaxyId, Long cycleNumber, List<Empire> empires) {
        Galaxy galaxy = galaxyRepository.findById(galaxyId)
                .orElseThrow(() -> new IllegalArgumentException("Galaxy not found: " + galaxyId));
        Map<String, BigDecimal> improvements = calculateDefaultImprovementCosts(galaxy);
        return processFiscalCycle(galaxyId, cycleNumber, empires, improvements);
    }
}

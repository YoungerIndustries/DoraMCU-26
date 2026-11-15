package com.yisy.modules.doramcu.hex.planet.finance;

import com.yisy.modules.doramcu.hex.planet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages financial operations for corporations: loans, interest, and reports.
 * <p>
 * The service:
 * <ul>
 *   <li>Processes loans for corporations</li>
 *   <li>Calculates and charges interest on outstanding loans</li>
 *   <li>Generates financial reports per corporation</li>
 *   <li>Checks for bankruptcy conditions</li>
 * </ul>
 * </p>
 */
@Service
public class TycoonFinancialService {

    private static final Logger log = LoggerFactory.getLogger(TycoonFinancialService.class);

    /** Base interest rate (per turn) */
    private static final double BASE_INTEREST_RATE = 0.02;

    /** Maximum loan amount multiplier (based on assets) */
    private static final double MAX_LOAN_MULTIPLIER = 2.0;

    /** Corporation ID -> loan balance */
    private final Map<String, Long> loanBalances;

    /** Corporation ID -> interest rate */
    private final Map<String, Double> interestRates;

    /** Corporation ID -> last interest turn */
    private final Map<String, Integer> lastInterestTurns;

    public TycoonFinancialService() {
        this.loanBalances = new ConcurrentHashMap<>();
        this.interestRates = new ConcurrentHashMap<>();
        this.lastInterestTurns = new ConcurrentHashMap<>();
    }

    /**
     * Processes a loan for a corporation.
     *
     * @param corporation the corporation requesting the loan
     * @param amount the loan amount
     * @return true if the loan was approved
     */
    public boolean processLoan(TransportCorporation corporation, long amount) {
        if (amount <= 0) {
            log.warn("Invalid loan amount: {}", amount);
            return false;
        }

        // Calculate maximum loan based on assets
        long maxLoan = calculateMaxLoan(corporation);
        if (amount > maxLoan) {
            log.warn("Loan amount {} exceeds maximum {} for {}", amount, maxLoan, corporation.getName());
            return false;
        }

        // Check current loan balance
        long currentBalance = getLoanBalance(corporation.getId());
        if (currentBalance + amount > maxLoan) {
            log.warn("Total loan {} would exceed maximum {} for {}",
                    currentBalance + amount, maxLoan, corporation.getName());
            return false;
        }

        // Process the loan
        corporation.addCredits(amount);
        loanBalances.merge(corporation.getId(), amount, Long::sum);

        // Set interest rate based on reputation
        double rate = calculateInterestRate(corporation);
        interestRates.put(corporation.getId(), rate);

        log.info("Approved loan of {} credits for {} (rate: {}%)",
                amount, corporation.getName(), rate * 100);
        return true;
    }

    /**
     * Processes loan repayment.
     *
     * @param corporation the corporation repaying
     * @param amount the repayment amount
     * @return the actual amount repaid
     */
    public long repayLoan(TransportCorporation corporation, long amount) {
        long balance = getLoanBalance(corporation.getId());
        if (balance <= 0) {
            return 0;
        }

        long actualAmount = Math.min(amount, balance);
        if (!corporation.spendCredits(actualAmount)) {
            return 0;
        }

        loanBalances.merge(corporation.getId(), -actualAmount, Long::sum);

        // Clear if fully repaid
        if (getLoanBalance(corporation.getId()) <= 0) {
            loanBalances.remove(corporation.getId());
            interestRates.remove(corporation.getId());
            lastInterestTurns.remove(corporation.getId());
        }

        log.info("Corporation {} repaid {} credits. Remaining balance: {}",
                corporation.getName(), actualAmount, getLoanBalance(corporation.getId()));
        return actualAmount;
    }

    /**
     * Calculates interest for all corporations.
     *
     * @param corporations list of corporations
     * @param currentTurn the current turn number
     * @return total interest collected
     */
    public long calculateInterest(List<TransportCorporation> corporations, int currentTurn) {
        long totalInterest = 0;

        for (TransportCorporation corporation : corporations) {
            long balance = getLoanBalance(corporation.getId());
            if (balance <= 0) continue;

            double rate = interestRates.getOrDefault(corporation.getId(), BASE_INTEREST_RATE);
            long interest = (long) (balance * rate);

            // Deduct interest from corporation
            if (corporation.getCredits() >= interest) {
                corporation.spendCredits(interest);
                totalInterest += interest;
            } else {
                // Corporation can't pay interest - add to loan balance
                long remaining = interest - corporation.getCredits();
                corporation.spendCredits(corporation.getCredits());
                loanBalances.merge(corporation.getId(), remaining, Long::sum);
                totalInterest += interest;

                log.warn("Corporation {} couldn't pay full interest. {} added to loan balance.",
                        corporation.getName(), remaining);
            }

            lastInterestTurns.put(corporation.getId(), currentTurn);
        }

        return totalInterest;
    }

    /**
     * Generates a financial report for a corporation.
     *
     * @param corporation the corporation
     * @param vehicles list of vehicles owned by the corporation
     * @param stations list of stations owned by the corporation
     * @param startTurn the starting turn
     * @param endTurn the ending turn
     * @return the financial report
     */
    public FinancialReport generateReport(TransportCorporation corporation,
                                           List<SurfaceVehicle> vehicles,
                                           List<Station> stations,
                                           int startTurn, int endTurn) {
        long totalIncome = 0;
        long totalExpenses = 0;

        // Calculate vehicle expenses (maintenance)
        for (SurfaceVehicle vehicle : vehicles) {
            if (!vehicle.isActive()) continue;
            int turns = endTurn - startTurn;
            totalExpenses += (long) vehicle.getMaintenanceCost() * turns;
        }

        // Calculate station income
        for (Station station : stations) {
            totalIncome += station.getTotalIncome();
        }

        long loanBalance = getLoanBalance(corporation.getId());
        long interestPaid = (long) (loanBalance * getInterestRate(corporation.getId()));

        FinancialReport report = new FinancialReport(
                corporation.getId(),
                corporation.getName(),
                startTurn, endTurn,
                totalIncome, totalExpenses,
                loanBalance, interestPaid
        );

        return report;
    }

    /**
     * Checks if a corporation is bankrupt.
     *
     * @param corporation the corporation to check
     * @return true if the corporation is bankrupt
     */
    public boolean isBankrupt(TransportCorporation corporation) {
        return corporation.getCredits() < 0 &&
                getLoanBalance(corporation.getId()) >= calculateMaxLoan(corporation);
    }

    /**
     * Gets the loan balance for a corporation.
     *
     * @param corporationId the corporation ID
     * @return the loan balance
     */
    public long getLoanBalance(String corporationId) {
        return loanBalances.getOrDefault(corporationId, 0L);
    }

    /**
     * Gets the interest rate for a corporation.
     *
     * @param corporationId the corporation ID
     * @return the interest rate
     */
    public double getInterestRate(String corporationId) {
        return interestRates.getOrDefault(corporationId, BASE_INTEREST_RATE);
    }

    /**
     * Calculates the maximum loan amount for a corporation.
     */
    private long calculateMaxLoan(TransportCorporation corporation) {
        long assetValue = corporation.getTotalAssetCount() * 100;
        return (long) (assetValue * MAX_LOAN_MULTIPLIER);
    }

    /**
     * Calculates the interest rate based on reputation.
     */
    private double calculateInterestRate(TransportCorporation corporation) {
        int reputation = corporation.getReputation();
        // Higher reputation = lower interest rate
        double rate = BASE_INTEREST_RATE * (1.0 - (reputation - 10) / 100.0);
        return Math.max(0.005, Math.min(0.1, rate));
    }
}

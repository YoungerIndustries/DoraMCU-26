package com.yisy.modules.doramcu.corporation.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction;
import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction.TransactionType;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.repository.CorporateFinanceTransactionRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that manages corporation credit balance, revenue, salary payments,
 * and financial reporting.
 * <p>
 * All financial operations are logged as {@link CorporateFinanceTransaction}
 * entities for auditability. Salaries are paid when contracts complete,
 * proportional to the contract's duration.
 * </p>
 */
@Service
public class CorporationEconomyService {

    private static final Logger log = LoggerFactory.getLogger(CorporationEconomyService.class);

    /**
     * Default turns per salary period (1 turn â‰ˆ 7 days).
     */
    static final int TURNS_PER_SALARY_PERIOD = 7;

    private final EveCorporationRepository corporationRepository;
    private final CorporateFinanceTransactionRepository transactionRepository;
    private final CorporateHiringRepository hiringRepository;

    public CorporationEconomyService(
            EveCorporationRepository corporationRepository,
            CorporateFinanceTransactionRepository transactionRepository,
            CorporateHiringRepository hiringRepository) {
        this.corporationRepository = corporationRepository;
        this.transactionRepository = transactionRepository;
        this.hiringRepository = hiringRepository;
    }

    // ---- Credit Operations ----

    /**
     * Add credits to a corporation's balance and log a REVENUE transaction.
     *
     * @param corporationId the corporation ID
     * @param amount the amount to add (must be positive)
     * @param description description of the revenue
     * @param referenceId optional reference ID (e.g., contract ID)
     * @throws IllegalArgumentException if corporation not found or amount <= 0
     */
    @Transactional
    @CacheEvict(value = {"corporationFinancialReport", "corporationOfficeState"}, key = "#corporationId")
    public void creditCorporation(Long corporationId, Long amount, String description, Long referenceId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive: " + amount);
        }

        EveCorporation corp = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        Long currentBalance = corp.getCreditBalance() != null ? corp.getCreditBalance() : 0L;
        corp.setCreditBalance(currentBalance + amount);
        corporationRepository.save(corp);

        CorporateFinanceTransaction txn = new CorporateFinanceTransaction(
                corporationId, TransactionType.REVENUE, amount, description, referenceId);
        transactionRepository.save(txn);

        log.info("Credited corporation {} with {} credits. New balance: {}. Description: {}",
                corporationId, amount, corp.getCreditBalance(), description);
    }

    /**
     * Deduct credits from a corporation's balance and log a transaction.
     * Checks for insolvency after deduction.
     *
     * @param corporationId the corporation ID
     * @param amount the amount to deduct (must be positive)
     * @param type the transaction type (SALARY, PENALTY, EXPENSE)
     * @param description description of the expense
     * @param referenceId optional reference ID
     * @throws IllegalArgumentException if corporation not found or amount <= 0
     */
    @Transactional
    @CacheEvict(value = {"corporationFinancialReport", "corporationOfficeState"}, key = "#corporationId")
    public void debitCorporation(Long corporationId, Long amount, TransactionType type, String description, Long referenceId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive: " + amount);
        }

        if (type == TransactionType.REVENUE) {
            throw new IllegalArgumentException("REVENUE type cannot be used for debit operations");
        }

        EveCorporation corp = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        Long currentBalance = corp.getCreditBalance() != null ? corp.getCreditBalance() : 0L;
        Long newBalance = currentBalance - amount;
        corp.setCreditBalance(newBalance);
        corporationRepository.save(corp);

        // Log as negative amount for clarity in reports
        CorporateFinanceTransaction txn = new CorporateFinanceTransaction(
                corporationId, type, -amount, description, referenceId);
        transactionRepository.save(txn);

        if (newBalance < 0) {
            log.warn("Corporation {} balance went negative: {}. Description: {}",
                    corporationId, newBalance, description);
        } else {
            log.info("Debited {} credits from corporation {}. New balance: {}. Description: {}",
                    amount, corporationId, newBalance, description);
        }
    }

    // ---- Salary Payments ----

    /**
     * Calculate and pay salaries for all active hires based on a contract's duration.
     * Salary formula: dailySalary * durationTurns / 7 (assuming 1 turn â‰ˆ 7 days).
     *
     * @param corporationId the corporation ID
     * @param contractId the contract ID (for reference)
     * @param durationTurns the contract duration in turns
     * @return total salary amount deducted
     */
    @Transactional
    @CacheEvict(value = {"corporationFinancialReport", "corporationOfficeState"}, key = "#corporationId")
    public Long paySalaries(Long corporationId, Long contractId, int durationTurns) {
        if (durationTurns <= 0) {
            log.info("Contract {} has zero duration. No salary to pay.", contractId);
            return 0L;
        }

        EveCorporation corp = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        List<CorporateHiring> activeHires = hiringRepository
                .findByCorporationIdAndStatus(corporationId, CorporateHiring.Status.ACTIVE);

        if (activeHires.isEmpty()) {
            log.info("No active hires for corporation {}. No salary to pay.", corporationId);
            return 0L;
        }

        long totalSalary = 0L;
        for (CorporateHiring hire : activeHires) {
            long dailySalary = hire.getDailySalary() != null ? hire.getDailySalary() : 0L;
            if (dailySalary <= 0) {
                continue;
            }

            // Calculate salary for this contract period
            long hireSalary = (dailySalary * durationTurns) / TURNS_PER_SALARY_PERIOD;
            if (hireSalary <= 0) {
                hireSalary = 1L; // Minimum 1 credit per hire per contract
            }

            totalSalary += hireSalary;

            // Deduct salary
            long currentBalance = corp.getCreditBalance() != null ? corp.getCreditBalance() : 0L;
            long newBalance = currentBalance - hireSalary;
            corp.setCreditBalance(newBalance);

            // Log salary transaction
            String desc = "Salary: " + hire.getAiCharacterId() + " (" + hire.getPosition().getDisplayName() + ")";
            CorporateFinanceTransaction txn = new CorporateFinanceTransaction(
                    corporationId, TransactionType.SALARY, -hireSalary, desc, contractId);
            transactionRepository.save(txn);

            log.info("Paid salary {} credits to hire {} ({}) for contract {}. Corp balance: {}",
                    hireSalary, hire.getAiCharacterId(), hire.getPosition().getDisplayName(),
                    contractId, newBalance);
        }

        corporationRepository.save(corp);

        if (totalSalary > 0) {
            long finalBalance = corp.getCreditBalance() != null ? corp.getCreditBalance() : 0L;
            log.info("Total salary payment: {} credits for corporation {}. New balance: {}",
                    totalSalary, corporationId, finalBalance);

            if (finalBalance < 0) {
                log.warn("Corporation {} is insolvent after salary payments. Balance: {}",
                        corporationId, finalBalance);
            }
        }

        return totalSalary;
    }

    // ---- Financial Reporting ----

    /**
     * Get a financial report for a corporation.
     *
     * @param corporationId the corporation ID
     * @return map with balance, revenue, expenses, net profit, and transaction count
     * @throws IllegalArgumentException if corporation not found
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "corporationFinancialReport", key = "#corporationId")
    public Map<String, Object> getFinancialReport(Long corporationId) {
        EveCorporation corp = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        Long creditBalance = corp.getCreditBalance() != null ? corp.getCreditBalance() : 0L;

        // Calculate totals from transactions
        Long totalRevenue = transactionRepository.sumByCorporationIdAndType(corporationId, TransactionType.REVENUE);
        Long totalSalary = transactionRepository.sumByCorporationIdAndType(corporationId, TransactionType.SALARY);
        Long totalPenalty = transactionRepository.sumByCorporationIdAndType(corporationId, TransactionType.PENALTY);
        Long totalExpense = transactionRepository.sumByCorporationIdAndType(corporationId, TransactionType.EXPENSE);

        // Expenses are negative in transactions, so we need absolute values for reporting
        Long totalExpenses = Math.abs(totalSalary) + Math.abs(totalPenalty) + Math.abs(totalExpense);
        Long netProfit = totalRevenue - totalExpenses;

        Long transactionCount = transactionRepository.countByCorporationId(corporationId);

        // Calculate total salary obligations (sum of daily salaries for active hires)
        List<CorporateHiring> activeHires = hiringRepository
                .findByCorporationIdAndStatus(corporationId, CorporateHiring.Status.ACTIVE);
        long totalSalaryObligations = 0L;
        for (CorporateHiring hire : activeHires) {
            long dailySalary = hire.getDailySalary() != null ? hire.getDailySalary() : 0L;
            totalSalaryObligations += dailySalary;
        }

        Map<String, Object> report = new HashMap<>();
        report.put("corporationId", corporationId);
        report.put("corporationName", corp.getName());
        report.put("creditBalance", creditBalance);
        report.put("totalRevenue", totalRevenue);
        report.put("totalExpenses", totalExpenses);
        report.put("netProfit", netProfit);
        report.put("transactionCount", transactionCount);
        report.put("isInsolvent", creditBalance < 0);
        report.put("totalSalaryObligations", totalSalaryObligations);
        report.put("activeHireCount", activeHires.size());
        return report;
    }

    /**
     * Get transaction history for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of transactions ordered by date descending
     */
    @Transactional(readOnly = true)
    public List<CorporateFinanceTransaction> getTransactionHistory(Long corporationId) {
        return transactionRepository.findByCorporationIdOrderByCreatedAtDesc(corporationId);
    }

    /**
     * Check if a corporation is insolvent (balance < 0).
     *
     * @param corporationId the corporation ID
     * @return true if balance is negative
     */
    @Transactional(readOnly = true)
    public boolean isInsolvent(Long corporationId) {
        EveCorporation corp = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));
        long balance = corp.getCreditBalance() != null ? corp.getCreditBalance() : 0L;
        return balance < 0;
    }
}

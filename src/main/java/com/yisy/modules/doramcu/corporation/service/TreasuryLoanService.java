package com.yisy.modules.doramcu.corporation.service;

import com.yisy.modules.doramcu.corporation.model.SilverCurrencyConstants;
import com.yisy.modules.doramcu.corporation.model.TreasuryLoan;
import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction;
import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction.TransactionType;
import com.yisy.modules.doramcu.corporation.repository.TreasuryLoanRepository;
import com.yisy.modules.doramcu.corporation.repository.CorporateFinanceTransactionRepository;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.repository.EveCorporationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service that manages treasury loans for corporations.
 * <p>
 * Every new corporation receives a startup loan from the DoraMCU Treasury
 * to fund the full $500 USD starting capital. The loan is repaid monthly
 * with 5% annual interest compounded yearly.
 * </p>
 */
@Service
public class TreasuryLoanService {

    private static final Logger log = LoggerFactory.getLogger(TreasuryLoanService.class);

    private final TreasuryLoanRepository loanRepository;
    private final CorporateFinanceTransactionRepository transactionRepository;
    private final EveCorporationRepository corporationRepository;

    public TreasuryLoanService(
            TreasuryLoanRepository loanRepository,
            CorporateFinanceTransactionRepository transactionRepository,
            EveCorporationRepository corporationRepository) {
        this.loanRepository = loanRepository;
        this.transactionRepository = transactionRepository;
        this.corporationRepository = corporationRepository;
    }

    /**
     * Create a standard treasury loan for a new corporation.
     * This should be called immediately after corporation creation.
     *
     * @param corporationId the corporation ID
     * @return the created loan
     */
    @Transactional
    public TreasuryLoan createStartupLoan(Long corporationId) {
        if (loanRepository.existsByCorporationId(corporationId)) {
            log.warn("Corporation {} already has a loan. Skipping creation.", corporationId);
            return loanRepository.findByCorporationId(corporationId).orElse(null);
        }

        TreasuryLoan loan = TreasuryLoan.createStandardLoan(corporationId);
        loan = loanRepository.save(loan);

        // Log the loan as a revenue transaction (credits received from treasury)
        CorporateFinanceTransaction txn = new CorporateFinanceTransaction(
                corporationId,
                TransactionType.REVENUE,
                SilverCurrencyConstants.LOAN_PRINCIPAL_CREDITS,
                "DoraMCU Treasury Startup Loan - " +
                SilverCurrencyConstants.formatCredits(SilverCurrencyConstants.LOAN_PRINCIPAL_CREDITS),
                null);
        transactionRepository.save(txn);

        log.info("Created startup loan for corporation {}: {} over {} months at {}% annual interest",
                corporationId,
                SilverCurrencyConstants.formatCredits(loan.getPrincipalCredits()),
                calculateEstimatedMonths(loan),
                loan.getAnnualInterestRate() * 100);

        return loan;
    }

    /**
     * Process a monthly loan payment for a corporation.
     * Deducts the monthly payment from the corporation's credit balance
     * and updates the loan outstanding balance.
     *
     * @param corporationId the corporation ID
     * @return the updated loan, or empty if no active loan found
     */
    @Transactional
    public Optional<TreasuryLoan> processMonthlyPayment(Long corporationId) {
        Optional<TreasuryLoan> loanOpt = loanRepository.findByCorporationId(corporationId);
        if (loanOpt.isEmpty() || loanOpt.get().getStatus() != TreasuryLoan.LoanStatus.ACTIVE) {
            return Optional.empty();
        }

        TreasuryLoan loan = loanOpt.get();

        // Check if payment is due
        if (loan.getNextPaymentDueDate() != null &&
                LocalDateTime.now().isBefore(loan.getNextPaymentDueDate())) {
            log.debug("Payment not yet due for corporation {}. Next due: {}",
                    corporationId, loan.getNextPaymentDueDate());
            return Optional.of(loan);
        }

        // Check if annual interest is due
        if (loan.isInterestDue()) {
            long interest = loan.compoundAnnualInterest();
            if (interest > 0) {
                log.info("Compounded annual interest for corporation {}: {} credits",
                        corporationId, SilverCurrencyConstants.formatCredits(interest));
                CorporateFinanceTransaction interestTxn = new CorporateFinanceTransaction(
                        corporationId,
                        TransactionType.EXPENSE,
                        -interest,
                        "Annual interest compounding (5%) on treasury loan",
                        loan.getId());
                transactionRepository.save(interestTxn);
            }
        }

        // Calculate payment amount
        long paymentAmount = loan.getMonthlyPaymentCredits();
        long outstanding = loan.getOutstandingCredits();

        // If payment exceeds outstanding, pay only the remaining balance
        boolean isFinalPayment = paymentAmount >= outstanding;
        if (isFinalPayment) {
            paymentAmount = outstanding;
        }

        // Deduct from corporation's credit balance
        EveCorporation corp = corporationRepository.findById(corporationId)
                .orElseThrow(() -> new IllegalArgumentException("Corporation not found: " + corporationId));

        Long currentBalance = corp.getCreditBalance() != null ? corp.getCreditBalance() : 0L;
        if (currentBalance < paymentAmount) {
            log.warn("Corporation {} has insufficient credits for loan payment. " +
                    "Balance: {}, Payment: {}", corporationId, currentBalance, paymentAmount);
            // Still process the payment but note the deficit
            corp.setCreditBalance(currentBalance - paymentAmount);
        } else {
            corp.setCreditBalance(currentBalance - paymentAmount);
        }
        corporationRepository.save(corp);

        // Update loan
        loan.setOutstandingCredits(Math.max(0, outstanding - paymentAmount));
        loan.setTotalPaidCredits(loan.getTotalPaidCredits() + paymentAmount);
        loan.setPaymentsMade(loan.getPaymentsMade() + 1);
        loan.setNextPaymentDueDate(LocalDateTime.now().plusMonths(1));

        // Log the payment transaction
        CorporateFinanceTransaction txn = new CorporateFinanceTransaction(
                corporationId,
                TransactionType.EXPENSE,
                -paymentAmount,
                "Treasury loan monthly payment #" + loan.getPaymentsMade() + " - " +
                SilverCurrencyConstants.formatCredits(paymentAmount),
                loan.getId());
        transactionRepository.save(txn);

        // Check if loan is paid off
        if (loan.getOutstandingCredits() <= 0) {
            loan.setStatus(TreasuryLoan.LoanStatus.PAID_OFF);
            loan.setPaidOffAt(LocalDateTime.now());
            log.info("Corporation {} has paid off their treasury loan! Total payments: {}",
                    corporationId, SilverCurrencyConstants.formatCredits(loan.getTotalPaidCredits()));
        }

        loan = loanRepository.save(loan);

        log.info("Processed loan payment for corporation {}: {} (outstanding: {})",
                corporationId,
                SilverCurrencyConstants.formatCredits(paymentAmount),
                SilverCurrencyConstants.formatCredits(loan.getOutstandingCredits()));

        return Optional.of(loan);
    }

    /**
     * Get the loan details for a corporation.
     *
     * @param corporationId the corporation ID
     * @return loan details map
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getLoanDetails(Long corporationId) {
        Map<String, Object> result = new HashMap<>();

        Optional<TreasuryLoan> loanOpt = loanRepository.findByCorporationId(corporationId);
        if (loanOpt.isEmpty()) {
            result.put("hasLoan", false);
            result.put("message", "No treasury loan found for this corporation");
            return result;
        }

        TreasuryLoan loan = loanOpt.get();
        result.put("hasLoan", true);
        result.put("loanId", loan.getId());
        result.put("status", loan.getStatus().name());
        result.put("principalCredits", loan.getPrincipalCredits());
        result.put("principalUsd", SilverCurrencyConstants.creditsToUsd(loan.getPrincipalCredits()));
        result.put("outstandingCredits", loan.getOutstandingCredits());
        result.put("outstandingUsd", SilverCurrencyConstants.creditsToUsd(loan.getOutstandingCredits()));
        result.put("totalPaidCredits", loan.getTotalPaidCredits());
        result.put("totalPaidUsd", SilverCurrencyConstants.creditsToUsd(loan.getTotalPaidCredits()));
        result.put("totalInterestPaidCredits", loan.getTotalInterestPaidCredits());
        result.put("paymentsMade", loan.getPaymentsMade());
        result.put("monthlyPaymentCredits", loan.getMonthlyPaymentCredits());
        result.put("monthlyPaymentUsd", SilverCurrencyConstants.creditsToUsd(loan.getMonthlyPaymentCredits()));
        result.put("annualInterestRate", loan.getAnnualInterestRate());
        result.put("nextPaymentDueDate", loan.getNextPaymentDueDate());
        result.put("createdAt", loan.getCreatedAt());
        result.put("paidOffAt", loan.getPaidOffAt());

        // Calculate remaining payments estimate
        if (loan.getStatus() == TreasuryLoan.LoanStatus.ACTIVE && loan.getOutstandingCredits() > 0) {
            long remaining = loan.getOutstandingCredits();
            long monthly = loan.getMonthlyPaymentCredits();
            int estimatedMonthsLeft = (int) Math.ceil((double) remaining / monthly);
            result.put("estimatedMonthsLeft", estimatedMonthsLeft);
        }

        return result;
    }

    /**
     * Process loan payments for all active loans.
     * Called by the turn scheduler to check and process due payments.
     *
     * @return number of payments processed
     */
    @Transactional
    public int processAllDuePayments() {
        var activeLoans = loanRepository.findByStatus(TreasuryLoan.LoanStatus.ACTIVE);
        int processed = 0;

        for (TreasuryLoan loan : activeLoans) {
            if (loan.getNextPaymentDueDate() == null ||
                    LocalDateTime.now().isAfter(loan.getNextPaymentDueDate())) {
                try {
                    processMonthlyPayment(loan.getCorporationId());
                    processed++;
                } catch (Exception e) {
                    log.error("Failed to process loan payment for corporation {}: {}",
                            loan.getCorporationId(), e.getMessage());
                }
            }
        }

        if (processed > 0) {
            log.info("Processed {} treasury loan payments", processed);
        }

        return processed;
    }

    /**
     * Estimate the number of months to pay off the loan.
     *
     * @param loan the loan
     * @return estimated months remaining
     */
    private int calculateEstimatedMonths(TreasuryLoan loan) {
        if (loan.getOutstandingCredits() <= 0) return 0;
        double outstanding = loan.getOutstandingCredits();
        double monthly = loan.getMonthlyPaymentCredits();
        double rate = loan.getAnnualInterestRate() / 12.0; // monthly rate

        if (rate <= 0) {
            return (int) Math.ceil(outstanding / monthly);
        }

        // Amortization formula: n = -log(1 - (r * P) / M) / log(1 + r)
        // P = principal, M = monthly payment, r = monthly rate
        double argument = 1 - (rate * outstanding) / monthly;
        if (argument <= 0) return Integer.MAX_VALUE; // payment too small to cover interest

        return (int) Math.ceil(-Math.log(argument) / Math.log(1 + rate));
    }

    /**
     * Migrate all existing corporations to the silver-backed credit system.
     * Updates credit balances to 100,000 credits ($500 USD) and creates
     * treasury loans for corporations that don't have one yet.
     *
     * @return map with migration statistics
     */
    @Transactional
    public Map<String, Object> migrateExistingCorporations() {
        Map<String, Object> result = new HashMap<>();
        var allCorporations = corporationRepository.findAll();
        
        int total = allCorporations.size();
        int updated = 0;
        int loansCreated = 0;
        int skipped = 0;
        var details = new java.util.ArrayList<Map<String, Object>>();

        log.info("Starting migration of {} corporations to silver-backed credit system", total);

        for (EveCorporation corp : allCorporations) {
            Map<String, Object> detail = new HashMap<>();
            detail.put("corporationId", corp.getId());
            detail.put("name", corp.getName());
            detail.put("ticker", corp.getTicker());

            boolean hasLoan = loanRepository.existsByCorporationId(corp.getId());
            long oldBalance = corp.getCreditBalance();

            // Update credit balance to starting capital
            corp.setCreditBalance(SilverCurrencyConstants.STARTING_CAPITAL_CREDITS);
            corporationRepository.save(corp);
            detail.put("oldBalance", oldBalance);
            detail.put("newBalance", SilverCurrencyConstants.STARTING_CAPITAL_CREDITS);

            // Create loan if not exists
            if (!hasLoan) {
                TreasuryLoan loan = createStartupLoan(corp.getId());
                if (loan != null) {
                    loansCreated++;
                    detail.put("loanCreated", true);
                }
            } else {
                detail.put("loanCreated", false);
                detail.put("existingLoan", true);
            }

            updated++;
            details.add(detail);
            log.info("Migrated corporation {}: {} → {} credits, loan: {}",
                    corp.getName(), oldBalance, SilverCurrencyConstants.STARTING_CAPITAL_CREDITS,
                    hasLoan ? "existing" : "created");
        }

        result.put("totalCorporations", total);
        result.put("corporationsUpdated", updated);
        result.put("loansCreated", loansCreated);
        result.put("skipped", skipped);
        result.put("details", details);
        result.put("startingCapitalCredits", SilverCurrencyConstants.STARTING_CAPITAL_CREDITS);
        result.put("startingCapitalUsd", SilverCurrencyConstants.STARTING_CAPITAL_USD);

        log.info("Migration complete: {} corporations updated, {} loans created", updated, loansCreated);
        return result;
    }
}

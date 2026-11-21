package com.yisy.modules.doramcu.corporation.model;
import com.yisy.modules.doramcu.model.players.Player;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * Tracks a corporation's loan from the DoraMCU Treasury.
 * <p>
 * Every new player corporation receives a startup loan to fund the full
 * $500 USD starting capital. The loan is repaid at $5 USD per month
 * with 5% annual interest compounded yearly.
 * </p>
 */
@Entity
@Table(name = "treasury_loans", indexes = {
        @Index(name = "idx_tl_corp", columnList = "corporation_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_tl_corp", columnNames = {"corporation_id"})
})
public class TreasuryLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "corporation_id", nullable = false, unique = true)
    private Long corporationId;

    /** Original principal amount in credits */
    @Column(name = "principal_credits", nullable = false)
    private Long principalCredits;

    /** Current outstanding balance in credits (includes accrued interest) */
    @Column(name = "outstanding_credits", nullable = false)
    private Long outstandingCredits;

    /** Total amount paid back in credits */
    @Column(name = "total_paid_credits", nullable = false)
    private Long totalPaidCredits = 0L;

    /** Total interest accrued and paid in credits */
    @Column(name = "total_interest_paid_credits", nullable = false)
    private Long totalInterestPaidCredits = 0L;

    /** Number of monthly payments made */
    @Column(name = "payments_made", nullable = false)
    private Integer paymentsMade = 0;

    /** Monthly payment amount in credits */
    @Column(name = "monthly_payment_credits", nullable = false)
    private Long monthlyPaymentCredits;

    /** Annual interest rate as a decimal (e.g., 0.05 = 5%) */
    @Column(name = "annual_interest_rate", nullable = false)
    private Double annualInterestRate;

    /** When the last annual interest was compounded */
    @Column(name = "last_interest_compound_date")
    private LocalDateTime lastInterestCompoundDate;

    /** When the next payment is due */
    @Column(name = "next_payment_due_date")
    private LocalDateTime nextPaymentDueDate;

    /** Loan status */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanStatus status = LoanStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "paid_off_at")
    private LocalDateTime paidOffAt;

    @Version
    private Integer version;

    public enum LoanStatus {
        ACTIVE,
        PAID_OFF,
        DEFAULTED
    }

    public TreasuryLoan() {
        this.createdAt = LocalDateTime.now();
        this.lastInterestCompoundDate = LocalDateTime.now();
    }

    /**
     * Create a new treasury loan with standard terms.
     *
     * @param corporationId the corporation receiving the loan
     */
    public static TreasuryLoan createStandardLoan(Long corporationId) {
        TreasuryLoan loan = new TreasuryLoan();
        loan.corporationId = corporationId;
        loan.principalCredits = SilverCurrencyConstants.LOAN_PRINCIPAL_CREDITS;
        loan.outstandingCredits = SilverCurrencyConstants.LOAN_PRINCIPAL_CREDITS;
        loan.monthlyPaymentCredits = SilverCurrencyConstants.MONTHLY_PAYMENT_CREDITS;
        loan.annualInterestRate = SilverCurrencyConstants.ANNUAL_INTEREST_RATE;
        loan.nextPaymentDueDate = LocalDateTime.now().plusMonths(1);
        return loan;
    }

    // ---- Getters and Setters ----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCorporationId() {
        return corporationId;
    }

    public void setCorporationId(Long corporationId) {
        this.corporationId = corporationId;
    }

    public Long getPrincipalCredits() {
        return principalCredits;
    }

    public void setPrincipalCredits(Long principalCredits) {
        this.principalCredits = principalCredits;
    }

    public Long getOutstandingCredits() {
        return outstandingCredits;
    }

    public void setOutstandingCredits(Long outstandingCredits) {
        this.outstandingCredits = outstandingCredits;
    }

    public Long getTotalPaidCredits() {
        return totalPaidCredits;
    }

    public void setTotalPaidCredits(Long totalPaidCredits) {
        this.totalPaidCredits = totalPaidCredits;
    }

    public Long getTotalInterestPaidCredits() {
        return totalInterestPaidCredits;
    }

    public void setTotalInterestPaidCredits(Long totalInterestPaidCredits) {
        this.totalInterestPaidCredits = totalInterestPaidCredits;
    }

    public Integer getPaymentsMade() {
        return paymentsMade;
    }

    public void setPaymentsMade(Integer paymentsMade) {
        this.paymentsMade = paymentsMade;
    }

    public Long getMonthlyPaymentCredits() {
        return monthlyPaymentCredits;
    }

    public void setMonthlyPaymentCredits(Long monthlyPaymentCredits) {
        this.monthlyPaymentCredits = monthlyPaymentCredits;
    }

    public Double getAnnualInterestRate() {
        return annualInterestRate;
    }

    public void setAnnualInterestRate(Double annualInterestRate) {
        this.annualInterestRate = annualInterestRate;
    }

    public LocalDateTime getLastInterestCompoundDate() {
        return lastInterestCompoundDate;
    }

    public void setLastInterestCompoundDate(LocalDateTime lastInterestCompoundDate) {
        this.lastInterestCompoundDate = lastInterestCompoundDate;
    }

    public LocalDateTime getNextPaymentDueDate() {
        return nextPaymentDueDate;
    }

    public void setNextPaymentDueDate(LocalDateTime nextPaymentDueDate) {
        this.nextPaymentDueDate = nextPaymentDueDate;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPaidOffAt() {
        return paidOffAt;
    }

    public void setPaidOffAt(LocalDateTime paidOffAt) {
        this.paidOffAt = paidOffAt;
    }

    public Integer getVersion() {
        return version;
    }

    /**
     * Check if annual interest should be compounded.
     * Interest compounds once per year from the last compound date.
     *
     * @return true if a year has passed since the last compound
     */
    public boolean isInterestDue() {
        if (lastInterestCompoundDate == null) return true;
        return LocalDateTime.now().isAfter(lastInterestCompoundDate.plusYears(1));
    }

    /**
     * Compound the annual interest and add it to the outstanding balance.
     * Returns the interest amount added.
     */
    public long compoundAnnualInterest() {
        if (outstandingCredits <= 0 || annualInterestRate == null || annualInterestRate <= 0) {
            return 0;
        }
        BigDecimal balance = BigDecimal.valueOf(outstandingCredits);
        BigDecimal rate = BigDecimal.valueOf(annualInterestRate);
        BigDecimal interest = balance.multiply(rate).setScale(0, RoundingMode.HALF_UP);
        this.outstandingCredits += interest.longValue();
        this.lastInterestCompoundDate = LocalDateTime.now();
        return interest.longValue();
    }

    @Override
    public String toString() {
        return "TreasuryLoan{" +
                "id=" + id +
                ", corporationId=" + corporationId +
                ", principal=" + SilverCurrencyConstants.formatCredits(principalCredits) +
                ", outstanding=" + SilverCurrencyConstants.formatCredits(outstandingCredits) +
                ", paid=" + SilverCurrencyConstants.formatCredits(totalPaidCredits) +
                ", payments=" + paymentsMade +
                ", status=" + status +
                '}';
    }
}

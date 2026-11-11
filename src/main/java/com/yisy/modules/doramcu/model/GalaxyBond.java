package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a Galaxy Bond — a debt instrument issued when the
 * Galaxy Treasury runs a deficit.
 * <p>
 * When the galaxy treasury cannot cover all improvement costs after collecting
 * the 5% tax from empires and transferring surplus to the corporation treasury,
 * the shortfall is converted into Galaxy Bonds that pay 10% interest until the
 * deficit is corrected.
 * </p>
 * <p>
 * Bonds are tied to a specific galaxy and tracked until redeemed.
 * </p>
 */
@Entity
@Table(name = "galaxy_bonds")
public class GalaxyBond {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The galaxy that issued this bond.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galaxy_id", nullable = false)
    private Galaxy galaxy;

    /**
     * The face value (principal amount) of the bond in credits.
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal principal;

    /**
     * The annual interest rate (default 10%).
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    /**
     * The total interest accrued so far.
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal accruedInterest = BigDecimal.ZERO;

    /**
     * The total amount owed (principal + accrued interest).
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalOutstanding;

    /**
     * Whether this bond has been redeemed (deficit corrected).
     */
    @Column(nullable = false)
    private Boolean redeemed = false;

    /**
     * When the bond was issued.
     */
    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    /**
     * When the bond was redeemed (null if still outstanding).
     */
    @Column(name = "redeemed_at")
    private LocalDateTime redeemedAt;

    /**
     * The cycle/turn number when the bond was issued.
     */
    @Column(name = "issued_cycle")
    private Long issuedCycle;

    /**
     * The cycle/turn number when the bond was redeemed.
     */
    @Column(name = "redeemed_cycle")
    private Long redeemedCycle;

    /**
     * Optional notes describing why the bond was issued.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    public GalaxyBond() {
    }

    /**
     * Creates a new galaxy bond for the given shortfall.
     *
     * @param galaxy the galaxy issuing the bond
     * @param principal the shortfall amount
     * @param interestRate the annual interest rate
     * @param cycle the current game cycle
     */
    public GalaxyBond(Galaxy galaxy, BigDecimal principal, BigDecimal interestRate, Long cycle) {
        this.galaxy = galaxy;
        this.principal = principal;
        this.interestRate = interestRate;
        this.totalOutstanding = principal;
        this.issuedAt = LocalDateTime.now();
        this.issuedCycle = cycle;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Galaxy getGalaxy() {
        return galaxy;
    }

    public void setGalaxy(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public void setPrincipal(BigDecimal principal) {
        this.principal = principal;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public BigDecimal getAccruedInterest() {
        return accruedInterest;
    }

    public void setAccruedInterest(BigDecimal accruedInterest) {
        this.accruedInterest = accruedInterest;
    }

    public BigDecimal getTotalOutstanding() {
        return totalOutstanding;
    }

    public void setTotalOutstanding(BigDecimal totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }

    public Boolean getRedeemed() {
        return redeemed;
    }

    public void setRedeemed(Boolean redeemed) {
        this.redeemed = redeemed;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(LocalDateTime redeemedAt) {
        this.redeemedAt = redeemedAt;
    }

    public Long getIssuedCycle() {
        return issuedCycle;
    }

    public void setIssuedCycle(Long issuedCycle) {
        this.issuedCycle = issuedCycle;
    }

    public Long getRedeemedCycle() {
        return redeemedCycle;
    }

    public void setRedeemedCycle(Long redeemedCycle) {
        this.redeemedCycle = redeemedCycle;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Accrue interest for one cycle.
     *
     * @param cyclesElapsed number of cycles since last accrual
     */
    public void accrueInterest(int cyclesElapsed) {
        if (redeemed) {
            return;
        }
        // Interest is annual; assume 52 cycles per year for accrual
        BigDecimal ratePerCycle = interestRate.divide(BigDecimal.valueOf(52), 4, BigDecimal.ROUND_HALF_UP);
        BigDecimal interest = totalOutstanding.multiply(ratePerCycle).multiply(BigDecimal.valueOf(cyclesElapsed));
        this.accruedInterest = accruedInterest.add(interest);
        this.totalOutstanding = principal.add(accruedInterest);
    }

    /**
     * Redeem this bond with the given amount.
     *
     * @param amount the amount available to redeem
     * @return the remaining amount after redemption (positive if overpaid on full redemption, zero if partial)
     */
    public BigDecimal redeem(BigDecimal amount) {
        if (amount.compareTo(totalOutstanding) >= 0) {
            // Full redemption — compute the remainder before resetting state
            BigDecimal remainder = amount.subtract(totalOutstanding);
            this.redeemed = true;
            this.redeemedAt = LocalDateTime.now();
            this.accruedInterest = BigDecimal.ZERO;
            this.totalOutstanding = BigDecimal.ZERO;
            return remainder;
        } else {
            // Partial redemption
            this.totalOutstanding = totalOutstanding.subtract(amount);
            this.accruedInterest = totalOutstanding.subtract(this.principal);
            if (this.accruedInterest.compareTo(BigDecimal.ZERO) < 0) {
                this.accruedInterest = BigDecimal.ZERO;
            }
            return BigDecimal.ZERO;
        }
    }

    @Override
    public String toString() {
        return "GalaxyBond{id=" + id + ", principal=" + principal + ", interestRate=" + interestRate +
                ", totalOutstanding=" + totalOutstanding + ", redeemed=" + redeemed + "}";
    }
}

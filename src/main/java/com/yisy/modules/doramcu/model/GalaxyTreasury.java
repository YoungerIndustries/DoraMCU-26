package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing the Galaxy Treasury — the central fiscal account
 * for a galaxy that collects taxes from empires, pays for improvements,
 * and transfers surplus to the owning corporation.
 * <p>
 * Each galaxy has one treasury record that accumulates over time.
 * Per-cycle breakdowns are stored in {@link GalaxyTreasuryCycle}.
 * </p>
 * <p>
 * Flow:
 * </p>
 * <ol>
 *   <li>Empires collect taxes from their planets and operations</li>
 *   <li>Each empire pays 5% of collected taxes to the Galaxy Treasury</li>
 *   <li>Empires pay their own bills, payroll, and expenses from the remaining 95%</li>
 *   <li>The Galaxy Treasury pays for galaxy-wide improvements (planet operations, infrastructure)</li>
 *   <li>Surplus is transferred to the owning Corporation's treasury</li>
 *   <li>Shortfalls are converted to {@link GalaxyBond} instruments at 10% interest</li>
 * </ol>
 */
@Entity
@Table(name = "galaxy_treasuries")
public class GalaxyTreasury {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The galaxy this treasury belongs to.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galaxy_id", nullable = false, unique = true)
    private Galaxy galaxy;

    /**
     * Current balance in credits.
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /**
     * Total taxes collected from all empires (lifetime).
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalTaxesCollected = BigDecimal.ZERO;

    /**
     * Total improvement costs paid (lifetime).
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalImprovementCosts = BigDecimal.ZERO;

    /**
     * Total surplus transferred to the corporation (lifetime).
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalSurplusTransferred = BigDecimal.ZERO;

    /**
     * Total deficit issued as bonds (lifetime).
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal totalDeficitBonds = BigDecimal.ZERO;

    /**
     * The tax rate applied to empires (default 5%).
     */
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRate = new BigDecimal("0.05");

    /**
     * The interest rate for galaxy bonds (default 10%).
     */
    @Column(name = "bond_interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal bondInterestRate = new BigDecimal("0.10");

    /**
     * The last cycle/turn that was processed.
     */
    @Column(name = "last_processed_cycle")
    private Long lastProcessedCycle;

    /**
     * When the treasury was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When the treasury was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public GalaxyTreasury() {
    }

    /**
     * Creates a new galaxy treasury for the given galaxy.
     *
     * @param galaxy the galaxy
     */
    public GalaxyTreasury(Galaxy galaxy) {
        this.galaxy = galaxy;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getTotalTaxesCollected() {
        return totalTaxesCollected;
    }

    public void setTotalTaxesCollected(BigDecimal totalTaxesCollected) {
        this.totalTaxesCollected = totalTaxesCollected;
    }

    public BigDecimal getTotalImprovementCosts() {
        return totalImprovementCosts;
    }

    public void setTotalImprovementCosts(BigDecimal totalImprovementCosts) {
        this.totalImprovementCosts = totalImprovementCosts;
    }

    public BigDecimal getTotalSurplusTransferred() {
        return totalSurplusTransferred;
    }

    public void setTotalSurplusTransferred(BigDecimal totalSurplusTransferred) {
        this.totalSurplusTransferred = totalSurplusTransferred;
    }

    public BigDecimal getTotalDeficitBonds() {
        return totalDeficitBonds;
    }

    public void setTotalDeficitBonds(BigDecimal totalDeficitBonds) {
        this.totalDeficitBonds = totalDeficitBonds;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        validateRate(taxRate, "Tax rate");
        this.taxRate = taxRate;
    }

    public BigDecimal getBondInterestRate() {
        return bondInterestRate;
    }

    public void setBondInterestRate(BigDecimal bondInterestRate) {
        validateRate(bondInterestRate, "Bond interest rate");
        this.bondInterestRate = bondInterestRate;
    }

    public Long getLastProcessedCycle() {
        return lastProcessedCycle;
    }

    public void setLastProcessedCycle(Long lastProcessedCycle) {
        this.lastProcessedCycle = lastProcessedCycle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Deposit credits into the treasury.
     */
    public void deposit(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(amount);
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Withdraw credits from the treasury.
     * Always subtracts the amount, allowing the balance to go negative.
     * A negative balance indicates a deficit that will be covered by bond issuance.
     *
     * @param amount the amount to withdraw
     * @return true if the balance remained non-negative after withdrawal
     */
    public boolean withdraw(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.subtract(amount);
            this.updatedAt = LocalDateTime.now();
            return this.balance.compareTo(BigDecimal.ZERO) >= 0;
        }
        return true;
    }

    /**
     * Validate that a rate is between 0 and 1 (inclusive).
     */
    private void validateRate(BigDecimal rate, String name) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(name + " must be between 0 and 1, got: " + rate);
        }
    }

    @Override
    public String toString() {
        return "GalaxyTreasury{id=" + id + ", balance=" + balance +
                ", totalTaxes=" + totalTaxesCollected + ", totalImprovements=" + totalImprovementCosts +
                ", totalSurplus=" + totalSurplusTransferred + ", totalDeficit=" + totalDeficitBonds + "}";
    }
}

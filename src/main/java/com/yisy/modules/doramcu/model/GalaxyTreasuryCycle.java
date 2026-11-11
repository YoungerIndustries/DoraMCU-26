package com.yisy.modules.doramcu.model;

import jakarta.persistence.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA entity representing a single fiscal cycle's breakdown for a galaxy treasury.
 * <p>
 * Each cycle records:
 * </p>
 * <ul>
 *   <li>Tax revenue collected from each empire</li>
 *   <li>Improvement costs paid by the galaxy</li>
 *   <li>Surplus transferred to the corporation</li>
 *   <li>Deficit issued as bonds</li>
 * </ul>
 */
@Entity
@Table(name = "galaxy_treasury_cycles")
public class GalaxyTreasuryCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * The galaxy this cycle belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galaxy_id", nullable = false)
    private Galaxy galaxy;

    /**
     * The cycle/turn number.
     */
    @Column(name = "cycle_number", nullable = false)
    private Long cycleNumber;

    /**
     * Total tax revenue collected from all empires this cycle.
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal taxRevenue = BigDecimal.ZERO;

    /**
     * Total improvement costs paid this cycle.
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal improvementCosts = BigDecimal.ZERO;

    /**
     * Surplus transferred to the corporation this cycle.
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal surplusTransferred = BigDecimal.ZERO;

    /**
     * Deficit issued as bonds this cycle.
     */
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal deficitBonds = BigDecimal.ZERO;

    /**
     * Balance at the start of this cycle.
     */
    @Column(name = "balance_start", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceStart = BigDecimal.ZERO;

    /**
     * Balance at the end of this cycle.
     */
    @Column(name = "balance_end", nullable = false, precision = 18, scale = 2)
    private BigDecimal balanceEnd = BigDecimal.ZERO;

    /**
     * Per-empire tax breakdown (serialized as JSON).
     * Key: empire ID, Value: tax amount paid.
     */
    @Column(columnDefinition = "TEXT")
    private String empireTaxBreakdown;

    /**
     * Per-improvement cost breakdown (serialized as JSON).
     * Key: description, Value: cost amount.
     */
    @Column(columnDefinition = "TEXT")
    private String improvementBreakdown;

    /**
     * When this cycle was processed.
     */
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    /**
     * Notes about this cycle.
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    // Transient fields for building the cycle before persistence
    @Transient
    private final Map<Integer, BigDecimal> empireTaxes = new HashMap<>();
    @Transient
    private final Map<String, BigDecimal> improvements = new HashMap<>();

    public GalaxyTreasuryCycle() {
    }

    /**
     * Creates a new cycle record.
     *
     * @param galaxy the galaxy
     * @param cycleNumber the cycle number
     * @param balanceStart the starting balance
     */
    public GalaxyTreasuryCycle(Galaxy galaxy, Long cycleNumber, BigDecimal balanceStart) {
        this.galaxy = galaxy;
        this.cycleNumber = cycleNumber;
        this.balanceStart = balanceStart;
        this.processedAt = LocalDateTime.now();
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

    public Long getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(Long cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public BigDecimal getTaxRevenue() {
        return taxRevenue;
    }

    public void setTaxRevenue(BigDecimal taxRevenue) {
        this.taxRevenue = taxRevenue;
    }

    public BigDecimal getImprovementCosts() {
        return improvementCosts;
    }

    public void setImprovementCosts(BigDecimal improvementCosts) {
        this.improvementCosts = improvementCosts;
    }

    public BigDecimal getSurplusTransferred() {
        return surplusTransferred;
    }

    public void setSurplusTransferred(BigDecimal surplusTransferred) {
        this.surplusTransferred = surplusTransferred;
    }

    public BigDecimal getDeficitBonds() {
        return deficitBonds;
    }

    public void setDeficitBonds(BigDecimal deficitBonds) {
        this.deficitBonds = deficitBonds;
    }

    public BigDecimal getBalanceStart() {
        return balanceStart;
    }

    public void setBalanceStart(BigDecimal balanceStart) {
        this.balanceStart = balanceStart;
    }

    public BigDecimal getBalanceEnd() {
        return balanceEnd;
    }

    public void setBalanceEnd(BigDecimal balanceEnd) {
        this.balanceEnd = balanceEnd;
    }

    public String getEmpireTaxBreakdown() {
        return empireTaxBreakdown;
    }

    public void setEmpireTaxBreakdown(String empireTaxBreakdown) {
        this.empireTaxBreakdown = empireTaxBreakdown;
    }

    public String getImprovementBreakdown() {
        return improvementBreakdown;
    }

    public void setImprovementBreakdown(String improvementBreakdown) {
        this.improvementBreakdown = improvementBreakdown;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Record tax paid by an empire.
     *
     * @param empireId the empire ID
     * @param amount the tax amount
     */
    public void recordEmpireTax(int empireId, BigDecimal amount) {
        this.empireTaxes.merge(empireId, amount, BigDecimal::add);
        this.taxRevenue = this.taxRevenue.add(amount);
    }

    /**
     * Record an improvement cost.
     *
     * @param description the improvement description
     * @param amount the cost
     */
    public void recordImprovement(String description, BigDecimal amount) {
        this.improvements.merge(description, amount, BigDecimal::add);
        this.improvementCosts = this.improvementCosts.add(amount);
    }

    /**
     * Get the tax paid by a specific empire.
     *
     * @param empireId the empire ID
     * @return the tax amount or zero
     */
    public BigDecimal getEmpireTax(int empireId) {
        return empireTaxes.getOrDefault(empireId, BigDecimal.ZERO);
    }

    /**
     * Get the empire tax breakdown map.
     */
    public Map<Integer, BigDecimal> getEmpireTaxes() {
        return empireTaxes;
    }

    /**
     * Get the improvement breakdown map.
     */
    public Map<String, BigDecimal> getImprovements() {
        return improvements;
    }

    /**
     * Finalize the cycle by computing the ending balance and serializing transient maps.
     * <p>
     * The ending balance is: start + taxRevenue - improvementCosts - surplusTransferred.
     * Deficit bonds are not subtracted again here because the treasury balance is already
     * reset to zero when bonds are issued (the deficit is covered by the bond, not an
     * additional deduction).
     * </p>
     */
    public void finalizeCycle() {
        // Balance end = start + revenue - costs - surplus transferred
        // Deficit bonds are already accounted for: the treasury balance was reset to 0
        // when bonds were issued, so we do not subtract deficitBonds again.
        this.balanceEnd = this.balanceStart
                .add(this.taxRevenue)
                .subtract(this.improvementCosts)
                .subtract(this.surplusTransferred);

        // Serialize transient maps to JSON string fields for persistence
        serializeMaps();

        this.processedAt = LocalDateTime.now();
    }

    /**
     * Serialize the transient empireTaxes and improvements maps to JSON string fields.
     */
    public void serializeMaps() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            if (!empireTaxes.isEmpty()) {
                this.empireTaxBreakdown = mapper.writeValueAsString(empireTaxes);
            }
            if (!improvements.isEmpty()) {
                this.improvementBreakdown = mapper.writeValueAsString(improvements);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cycle breakdown maps", e);
        }
    }

    @Override
    public String toString() {
        return "GalaxyTreasuryCycle{cycle=" + cycleNumber + ", taxRevenue=" + taxRevenue +
                ", improvements=" + improvementCosts + ", surplus=" + surplusTransferred +
                ", deficit=" + deficitBonds + ", balanceEnd=" + balanceEnd + "}";
    }
}

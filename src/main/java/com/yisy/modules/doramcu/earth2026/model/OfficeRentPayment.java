package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA entity representing a rent payment for a corporation's office unit
 * in the Younger Industries HQ.
 */
@Entity
@Table(name = "office_rent_payments", indexes = {
        @Index(name = "idx_orp_corporation", columnList = "corporation_id"),
        @Index(name = "idx_orp_office_unit", columnList = "office_unit_id"),
        @Index(name = "idx_orp_payment_date", columnList = "payment_date"),
        @Index(name = "idx_orp_status", columnList = "status")
})
public class OfficeRentPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Corporation that made the payment.
     */
    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    /**
     * Office unit being rented.
     */
    @Column(name = "office_unit_id", nullable = false)
    private Long officeUnitId;

    /**
     * Amount paid.
     */
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /**
     * Payment status: PENDING, COMPLETED, FAILED, REFUNDED.
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    /**
     * What this payment covers: RENT, LATE_FEE, DAMAGE_FEE, SECURITY_DEPOSIT.
     */
    @Column(name = "payment_type", nullable = false, length = 30)
    private String paymentType = "RENT";

    /**
     * Start of the rental period this payment covers.
     */
    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    /**
     * End of the rental period this payment covers.
     */
    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    /**
     * When the payment was made.
     */
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    /**
     * When the payment was due.
     */
    @Column(name = "due_date")
    private LocalDateTime dueDate;

    /**
     * Whether this payment was late.
     */
    @Column(name = "is_late", nullable = false)
    private boolean isLate = false;

    /**
     * Late fee amount, if applicable.
     */
    @Column(name = "late_fee")
    private BigDecimal lateFee = BigDecimal.ZERO;

    /**
     * Notes about the payment.
     */
    @Column(length = 1000)
    private String notes;

    /**
     * When the record was created.
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public OfficeRentPayment() {
        this.createdAt = LocalDateTime.now();
    }

    public OfficeRentPayment(Long corporationId, Long officeUnitId, BigDecimal amount,
                              String paymentType, LocalDateTime periodStart, LocalDateTime periodEnd) {
        this();
        this.corporationId = corporationId;
        this.officeUnitId = officeUnitId;
        this.amount = amount;
        this.paymentType = paymentType;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }

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

    public Long getOfficeUnitId() {
        return officeUnitId;
    }

    public void setOfficeUnitId(Long officeUnitId) {
        this.officeUnitId = officeUnitId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public LocalDateTime getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDateTime periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDateTime getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDateTime periodEnd) {
        this.periodEnd = periodEnd;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isLate() {
        return isLate;
    }

    public void setLate(boolean late) {
        isLate = late;
    }

    public BigDecimal getLateFee() {
        return lateFee;
    }

    public void setLateFee(BigDecimal lateFee) {
        this.lateFee = lateFee;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "OfficeRentPayment{id=" + id + ", corpId=" + corporationId +
                ", officeUnitId=" + officeUnitId + ", amount=" + amount +
                ", type=" + paymentType + ", status=" + status + "}";
    }
}

package com.yisy.modules.doramcu.corporation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity tracking all financial operations for a corporation.
 * <p>
 * Every credit movement — revenue from contracts, salary payments to AI hires,
 * bonuses, penalties, and general expenses — is logged as a transaction for
 * auditability and financial reporting.
 * </p>
 */
@Entity
@Table(name = "corporate_finance_transactions", indexes = {
        @Index(name = "idx_fin_txn_corp", columnList = "corporationId"),
        @Index(name = "idx_fin_txn_type", columnList = "type"),
        @Index(name = "idx_fin_txn_date", columnList = "createdAt")
})
public class CorporateFinanceTransaction {

    /**
     * Type of financial transaction.
     */
    public enum TransactionType {
        REVENUE,    // Contract payment received
        SALARY,     // AI hire salary payment
        BONUS,      // Performance bonus
        PENALTY,    // Failed contract penalty
        EXPENSE     // General expense
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false)
    private Long amount; // Positive for revenue, negative for expenses

    @Column(length = 500)
    private String description;

    @Column(name = "reference_id")
    private Long referenceId; // Contract ID or hiring ID

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CorporateFinanceTransaction() {
        this.createdAt = LocalDateTime.now();
    }

    public CorporateFinanceTransaction(Long corporationId, TransactionType type, Long amount, String description, Long referenceId) {
        this.corporationId = corporationId;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.referenceId = referenceId;
        this.createdAt = LocalDateTime.now();
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

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "CorporateFinanceTransaction{" +
                "id=" + id +
                ", corporationId=" + corporationId +
                ", type=" + type +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", referenceId=" + referenceId +
                ", createdAt=" + createdAt +
                '}';
    }
}

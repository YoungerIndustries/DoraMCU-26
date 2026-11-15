package com.yisy.modules.doramcu.corporation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity tracking which AI hires are assigned to which contracts.
 * <p>
 * Links a CorporateContract to a CorporateHiring record, recording when the
 * assignment was made and how much XP the hire earned from the contract.
 * </p>
 */
@Entity
@Table(name = "corporate_contract_assignments", indexes = {
        @Index(name = "idx_contract_assign_contract", columnList = "contractId"),
        @Index(name = "idx_contract_assign_hiring", columnList = "hiringId")
})
public class CorporateContractAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "hiring_id", nullable = false)
    private Long hiringId;

    @Column(name = "ai_character_id", nullable = false)
    private Long aiCharacterId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "xp_earned")
    private Integer xpEarned = 0;

    public CorporateContractAssignment() {
    }

    public CorporateContractAssignment(Long contractId, Long hiringId, Long aiCharacterId) {
        this.contractId = contractId;
        this.hiringId = hiringId;
        this.aiCharacterId = aiCharacterId;
        this.assignedAt = LocalDateTime.now();
        this.xpEarned = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public Long getHiringId() {
        return hiringId;
    }

    public void setHiringId(Long hiringId) {
        this.hiringId = hiringId;
    }

    public Long getAiCharacterId() {
        return aiCharacterId;
    }

    public void setAiCharacterId(Long aiCharacterId) {
        this.aiCharacterId = aiCharacterId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Integer getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(Integer xpEarned) {
        this.xpEarned = xpEarned;
    }

    @Override
    public String toString() {
        return "CorporateContractAssignment{" +
                "id=" + id +
                ", contractId=" + contractId +
                ", hiringId=" + hiringId +
                ", aiCharacterId=" + aiCharacterId +
                ", xpEarned=" + xpEarned +
                '}';
    }
}

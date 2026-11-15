package com.yisy.modules.doramcu.corporation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Tracks pending interaction proposals between corporations.
 * <p>
 * Proposals include trade agreements, joint ventures, and hostile takeovers.
 * Each proposal has a status (pending, accepted, rejected, expired) and an expiration time.
 * </p>
 */
@Entity
@Table(name = "corporation_interaction_proposals", indexes = {
        @Index(name = "idx_cip_proposer", columnList = "proposerCorpId"),
        @Index(name = "idx_cip_target", columnList = "targetCorpId"),
        @Index(name = "idx_cip_status", columnList = "status"),
        @Index(name = "idx_cip_expires", columnList = "expiresAt")
})
public class CorporationInteractionProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "proposal_type", nullable = false, length = 20)
    private ProposalType proposalType;

    @Column(name = "proposer_corp_id", nullable = false)
    private Long proposerCorpId;

    @Column(name = "target_corp_id", nullable = false)
    private Long targetCorpId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private ProposalStatus status = ProposalStatus.PENDING;

    @Column(name = "proposed_at", nullable = false)
    private LocalDateTime proposedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @Column(name = "contract_id")
    private Long contractId; // For joint ventures, the contract being proposed

    @Column(name = "description", length = 500)
    private String description;

    @Version
    private Integer version;

    public CorporationInteractionProposal() {
        this.proposedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(7); // Default 7-day expiration
    }

    public CorporationInteractionProposal(ProposalType proposalType, Long proposerCorpId, Long targetCorpId) {
        this();
        this.proposalType = proposalType;
        this.proposerCorpId = proposerCorpId;
        this.targetCorpId = targetCorpId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProposalType getProposalType() {
        return proposalType;
    }

    public void setProposalType(ProposalType proposalType) {
        this.proposalType = proposalType;
    }

    public Long getProposerCorpId() {
        return proposerCorpId;
    }

    public void setProposerCorpId(Long proposerCorpId) {
        this.proposerCorpId = proposerCorpId;
    }

    public Long getTargetCorpId() {
        return targetCorpId;
    }

    public void setTargetCorpId(Long targetCorpId) {
        this.targetCorpId = targetCorpId;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
        if (status != ProposalStatus.PENDING) {
            this.respondedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getProposedAt() {
        return proposedAt;
    }

    public void setProposedAt(LocalDateTime proposedAt) {
        this.proposedAt = proposedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    /**
     * Check if the proposal is still pending and not expired.
     */
    public boolean isActive() {
        return status == ProposalStatus.PENDING && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Check if the proposal has expired.
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Mark the proposal as accepted.
     */
    public void accept() {
        this.status = ProposalStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * Mark the proposal as rejected.
     */
    public void reject() {
        this.status = ProposalStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * Mark the proposal as expired if it has passed its expiration time.
     */
    public void expireIfExpired() {
        if (isExpired() && status == ProposalStatus.PENDING) {
            this.status = ProposalStatus.EXPIRED;
        }
    }

    @Override
    public String toString() {
        return "CorporationInteractionProposal{" +
                "id=" + id +
                ", proposalType=" + proposalType +
                ", proposerCorpId=" + proposerCorpId +
                ", targetCorpId=" + targetCorpId +
                ", status=" + status +
                '}';
    }

    /**
     * Types of interaction proposals.
     */
    public enum ProposalType {
        TRADE_AGREEMENT("Trade Agreement", "Propose mutual trade benefits"),
        JOINT_VENTURE("Joint Venture", "Propose collaboration on a contract"),
        HOSTILE_TAKEOVER("Hostile Takeover", "Attempt to take over the target corporation");

        private final String displayName;
        private final String description;

        ProposalType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Status of a proposal.
     */
    public enum ProposalStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        EXPIRED
    }
}

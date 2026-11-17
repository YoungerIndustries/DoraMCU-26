package com.yisy.modules.doramcu.model.misc;
import com.yisy.modules.doramcu.model.players.Player;


import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity linking a Traveller character to a corporate contract.
 * <p>
 * Allows the player's Traveller character to work on contracts alongside
 * AI hires, using their abilities and skills to boost success.
 * </p>
 */
@Entity
@Table(name = "traveller_contract_assignments", indexes = {
        @Index(name = "idx_tca_contract", columnList = "contractId"),
        @Index(name = "idx_tca_character", columnList = "travellerCharacterId")
})
public class TravellerContractAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "contract_id", nullable = false)
    private Long contractId;

    @Column(name = "traveller_character_id", nullable = false)
    private Integer travellerCharacterId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "xp_earned")
    private Integer xpEarned = 0;

    public TravellerContractAssignment() {
    }

    public TravellerContractAssignment(Long contractId, Integer travellerCharacterId) {
        this.contractId = contractId;
        this.travellerCharacterId = travellerCharacterId;
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

    public Integer getTravellerCharacterId() {
        return travellerCharacterId;
    }

    public void setTravellerCharacterId(Integer travellerCharacterId) {
        this.travellerCharacterId = travellerCharacterId;
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
        return "TravellerContractAssignment{" +
                "id=" + id +
                ", contractId=" + contractId +
                ", travellerCharacterId=" + travellerCharacterId +
                ", xpEarned=" + xpEarned +
                '}';
    }
}

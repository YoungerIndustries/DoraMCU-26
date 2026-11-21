package com.yisy.modules.doramcu.corporation.model;
import com.yisy.modules.doramcu.model.players.Player;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a crew group within the corporation.
 * <p>
 * A crew is a group of AI characters (or player characters) under a leader AI,
 * selected from the employee pool to execute contracts as a unit.
 * Crews persist across contracts and can be managed independently.
 * </p>
 */
@Entity
@Table(name = "crew", indexes = {
        @Index(name = "idx_crew_corporation", columnList = "corporationId"),
        @Index(name = "idx_crew_status", columnList = "status"),
        @Index(name = "idx_crew_leader", columnList = "leaderHiringId"),
        @Index(name = "idx_crew_contract", columnList = "contractId")
})
public class Crew {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "leader_hiring_id", nullable = false)
    private Long leaderHiringId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "contract_id")
    private Long contractId;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "crew", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CrewMember> members = new ArrayList<>();

    /**
     * Crew lifecycle status.
     */
    public enum Status {
        /** Active and available for contract assignment */
        ACTIVE,
        /** Disbanded, members returned to employee pool */
        DISBANDED,
        /** Currently executing a contract */
        ON_CONTRACT
    }

    public Crew() {
    }

    public Crew(Long corporationId, String name, Long leaderHiringId, String description) {
        this.corporationId = corporationId;
        this.name = name;
        this.leaderHiringId = leaderHiringId;
        this.description = description;
        this.status = Status.ACTIVE;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getLeaderHiringId() {
        return leaderHiringId;
    }

    public void setLeaderHiringId(Long leaderHiringId) {
        this.leaderHiringId = leaderHiringId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public List<CrewMember> getMembers() {
        return members;
    }

    public void setMembers(List<CrewMember> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "Crew{" +
                "id=" + id +
                ", corporationId=" + corporationId +
                ", name='" + name + '\'' +
                ", leaderHiringId=" + leaderHiringId +
                ", status=" + status +
                '}';
    }
}

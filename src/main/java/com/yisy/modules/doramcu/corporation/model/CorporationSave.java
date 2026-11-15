package com.yisy.modules.doramcu.corporation.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Stores manual save snapshots of corporation state.
 * <p>
 * Allows players to save their corporation state at any time and load it later.
 * Each save captures a point-in-time snapshot of credit balance, active contracts,
 * crew state, AI assistant state, and office state as JSON strings.
 * </p>
 */
@Entity
@Table(name = "corporation_saves", indexes = {
        @Index(name = "idx_cs_corp", columnList = "corporationId"),
        @Index(name = "idx_cs_timestamp", columnList = "timestamp")
})
public class CorporationSave {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "corporation_id", nullable = false)
    private Long corporationId;

    @Column(name = "save_name", length = 100)
    private String saveName;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "credit_balance", nullable = false)
    private Long creditBalance = 0L;

    @Lob
    @Column(name = "active_contracts", columnDefinition = "CLOB")
    private String activeContracts; // JSON array of contract IDs and state

    @Lob
    @Column(name = "crew_state", columnDefinition = "CLOB")
    private String crewState; // JSON array of crew IDs and state

    @Lob
    @Column(name = "ai_assistant_state", columnDefinition = "CLOB")
    private String aiAssistantState; // JSON object with AI assistant state

    @Lob
    @Column(name = "office_state", columnDefinition = "CLOB")
    private String officeState; // JSON object with office building level

    @Version
    private Integer version;

    public CorporationSave() {
        this.timestamp = LocalDateTime.now();
    }

    public CorporationSave(Long corporationId, String saveName) {
        this();
        this.corporationId = corporationId;
        this.saveName = saveName;
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

    public String getSaveName() {
        return saveName;
    }

    public void setSaveName(String saveName) {
        this.saveName = saveName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Long getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(Long creditBalance) {
        this.creditBalance = creditBalance;
    }

    public String getActiveContracts() {
        return activeContracts;
    }

    public void setActiveContracts(String activeContracts) {
        this.activeContracts = activeContracts;
    }

    public String getCrewState() {
        return crewState;
    }

    public void setCrewState(String crewState) {
        this.crewState = crewState;
    }

    public String getAiAssistantState() {
        return aiAssistantState;
    }

    public void setAiAssistantState(String aiAssistantState) {
        this.aiAssistantState = aiAssistantState;
    }

    public String getOfficeState() {
        return officeState;
    }

    public void setOfficeState(String officeState) {
        this.officeState = officeState;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "CorporationSave{" +
                "id=" + id +
                ", corporationId=" + corporationId +
                ", saveName='" + saveName + '\'' +
                ", timestamp=" + timestamp +
                ", creditBalance=" + creditBalance +
                '}';
    }
}

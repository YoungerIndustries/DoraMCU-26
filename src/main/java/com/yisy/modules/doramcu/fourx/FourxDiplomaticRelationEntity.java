package com.yisy.modules.doramcu.fourx;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for persistent 4X diplomatic relation data.
 */
@Entity
@Table(name = "fourx_diplomatic_relations")
public class FourxDiplomaticRelationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String relationId;

    @Column(nullable = false)
    private String faction1Id;

    @Column(nullable = false)
    private String faction2Id;

    @Column(nullable = false)
    private int relationScore = 0;

    @Column(nullable = false)
    private boolean atWar = false;

    /** Active treaties serialized as JSON. */
    @Column(columnDefinition = "TEXT")
    private String activeTreatiesJson;

    @Column(nullable = false)
    private LocalDateTime lastEventAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private FourxSessionEntity session;

    public FourxDiplomaticRelationEntity() {
    }

    public FourxDiplomaticRelationEntity(String relationId, String faction1Id, String faction2Id) {
        this.relationId = relationId;
        this.faction1Id = faction1Id;
        this.faction2Id = faction2Id;
        this.lastEventAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRelationId() { return relationId; }
    public void setRelationId(String relationId) { this.relationId = relationId; }

    public String getFaction1Id() { return faction1Id; }
    public void setFaction1Id(String faction1Id) { this.faction1Id = faction1Id; }

    public String getFaction2Id() { return faction2Id; }
    public void setFaction2Id(String faction2Id) { this.faction2Id = faction2Id; }

    public int getRelationScore() { return relationScore; }
    public void setRelationScore(int relationScore) {
        this.relationScore = Math.max(-100, Math.min(100, relationScore));
    }

    public boolean isAtWar() { return atWar; }
    public void setAtWar(boolean atWar) { this.atWar = atWar; }

    public String getActiveTreatiesJson() { return activeTreatiesJson; }
    public void setActiveTreatiesJson(String activeTreatiesJson) { this.activeTreatiesJson = activeTreatiesJson; }

    public LocalDateTime getLastEventAt() { return lastEventAt; }
    public void setLastEventAt(LocalDateTime lastEventAt) { this.lastEventAt = lastEventAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public FourxSessionEntity getSession() { return session; }
    public void setSession(FourxSessionEntity session) { this.session = session; }

    /**
     * Convert to the shared DTO.
     */
    public FourxDiplomaticRelation toDto() {
        FourxDiplomaticRelation dto = new FourxDiplomaticRelation(faction1Id, faction2Id);
        dto.setId(relationId);
        dto.setRelationScore(relationScore);
        dto.setAtWar(atWar);
        dto.setLastEventAt(lastEventAt);
        return dto;
    }
}

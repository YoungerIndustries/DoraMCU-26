package com.yisy.modules.doramcu.fourx;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity for persistent 4X colony data.
 */
@Entity
@Table(name = "fourx_colonies")
public class FourxColonyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String colonyId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long population = 0;

    @Column(nullable = false)
    private int mineralsPerTurn = 0;

    @Column(nullable = false)
    private int energyPerTurn = 0;

    @Column(nullable = false)
    private int foodPerTurn = 0;

    @Column(nullable = false)
    private int researchPerTurn = 0;

    /** Building names serialized as JSON. */
    @Column(columnDefinition = "TEXT")
    private String buildingsJson;

    @Column(nullable = false)
    private String ownerFactionId;

    @Column(nullable = false)
    private String starSystemId;

    @Column(nullable = false)
    private boolean operational = true;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private FourxSessionEntity session;

    public FourxColonyEntity() {
    }

    public FourxColonyEntity(String colonyId, String name, String ownerFactionId, String starSystemId) {
        this.colonyId = colonyId;
        this.name = name;
        this.ownerFactionId = ownerFactionId;
        this.starSystemId = starSystemId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getColonyId() { return colonyId; }
    public void setColonyId(String colonyId) { this.colonyId = colonyId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getPopulation() { return population; }
    public void setPopulation(long population) { this.population = population; }

    public int getMineralsPerTurn() { return mineralsPerTurn; }
    public void setMineralsPerTurn(int mineralsPerTurn) { this.mineralsPerTurn = mineralsPerTurn; }

    public int getEnergyPerTurn() { return energyPerTurn; }
    public void setEnergyPerTurn(int energyPerTurn) { this.energyPerTurn = energyPerTurn; }

    public int getFoodPerTurn() { return foodPerTurn; }
    public void setFoodPerTurn(int foodPerTurn) { this.foodPerTurn = foodPerTurn; }

    public int getResearchPerTurn() { return researchPerTurn; }
    public void setResearchPerTurn(int researchPerTurn) { this.researchPerTurn = researchPerTurn; }

    public String getBuildingsJson() { return buildingsJson; }
    public void setBuildingsJson(String buildingsJson) { this.buildingsJson = buildingsJson; }

    public String getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(String ownerFactionId) { this.ownerFactionId = ownerFactionId; }

    public String getStarSystemId() { return starSystemId; }
    public void setStarSystemId(String starSystemId) { this.starSystemId = starSystemId; }

    public boolean isOperational() { return operational; }
    public void setOperational(boolean operational) { this.operational = operational; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public FourxSessionEntity getSession() { return session; }
    public void setSession(FourxSessionEntity session) { this.session = session; }

    /**
     * Convert to the shared DTO.
     */
    public FourxColony toDto() {
        FourxColony dto = new FourxColony(colonyId, name, ownerFactionId, starSystemId);
        dto.setPopulation(population);
        dto.setMineralsPerTurn(mineralsPerTurn);
        dto.setEnergyPerTurn(energyPerTurn);
        dto.setFoodPerTurn(foodPerTurn);
        dto.setResearchPerTurn(researchPerTurn);
        dto.setOperational(operational);
        return dto;
    }
}

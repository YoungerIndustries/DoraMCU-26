package com.yisy.modules.doramcu.fourx;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * JPA entity for persistent 4X trade route data.
 */
@Entity
@Table(name = "fourx_trade_routes")
public class FourxTradeRouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tradeRouteId;

    @Column(nullable = false)
    private String originSystemId;

    @Column(nullable = false)
    private String destinationSystemId;

    @Column(nullable = false, length = 20)
    private String cargoType;

    @Column(nullable = false)
    private int profit = 0;

    @Column(nullable = false)
    private int securityLevel = 50;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private String ownerFactionId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private FourxSessionEntity session;

    public FourxTradeRouteEntity() {
    }

    public FourxTradeRouteEntity(String tradeRouteId, String originSystemId, String destinationSystemId,
                                  String cargoType, String ownerFactionId) {
        this.tradeRouteId = tradeRouteId;
        this.originSystemId = originSystemId;
        this.destinationSystemId = destinationSystemId;
        this.cargoType = cargoType;
        this.ownerFactionId = ownerFactionId;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTradeRouteId() { return tradeRouteId; }
    public void setTradeRouteId(String tradeRouteId) { this.tradeRouteId = tradeRouteId; }

    public String getOriginSystemId() { return originSystemId; }
    public void setOriginSystemId(String originSystemId) { this.originSystemId = originSystemId; }

    public String getDestinationSystemId() { return destinationSystemId; }
    public void setDestinationSystemId(String destinationSystemId) { this.destinationSystemId = destinationSystemId; }

    public String getCargoType() { return cargoType; }
    public void setCargoType(String cargoType) { this.cargoType = cargoType; }

    public int getProfit() { return profit; }
    public void setProfit(int profit) { this.profit = profit; }

    public int getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(int securityLevel) { this.securityLevel = securityLevel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(String ownerFactionId) { this.ownerFactionId = ownerFactionId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public FourxSessionEntity getSession() { return session; }
    public void setSession(FourxSessionEntity session) { this.session = session; }

    /**
     * Convert to the shared DTO.
     */
    public FourxTradeRoute toDto() {
        FourxTradeRoute dto = new FourxTradeRoute(originSystemId, destinationSystemId,
                parseCargoType(cargoType), ownerFactionId);
        dto.setId(tradeRouteId);
        dto.setProfit(profit);
        dto.setSecurityLevel(securityLevel);
        dto.setActive(active);
        return dto;
    }

    private FourxTradeRoute.CargoType parseCargoType(String type) {
        try {
            return FourxTradeRoute.CargoType.valueOf(type);
        } catch (IllegalArgumentException e) {
            return FourxTradeRoute.CargoType.GENERAL;
        }
    }
}

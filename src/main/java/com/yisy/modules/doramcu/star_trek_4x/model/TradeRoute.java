package com.yisy.modules.doramcu.star_trek_4x.model;

import jakarta.persistence.*;

/**
 * Represents a trade route between two star systems.
 * <p>
 * A trade route has an origin and destination system, cargo type, profit, and security level.
 * </p>
 */
@Entity
@Table(name = "st4x_trade_routes")
public class TradeRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** Origin star system ID. */
    @Column(nullable = false)
    private Long originSystemId;

    /** Destination star system ID. */
    @Column(nullable = false)
    private Long destinationSystemId;

    /** Cargo type: MINERALS, ENERGY, FOOD, RESEARCH, GENERAL. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CargoType cargoType;

    /** Profit per trade cycle. */
    @Column(nullable = false)
    private int profit = 0;

    /** Security level (0-100). */
    @Column(nullable = false)
    private int securityLevel = 50;

    /** Whether this route is active. */
    @Column(nullable = false)
    private boolean active = true;

    /** Owner faction ID. */
    @Column(nullable = false)
    private Long ownerFactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    public TradeRoute() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOriginSystemId() { return originSystemId; }
    public void setOriginSystemId(Long originSystemId) { this.originSystemId = originSystemId; }

    public Long getDestinationSystemId() { return destinationSystemId; }
    public void setDestinationSystemId(Long destinationSystemId) { this.destinationSystemId = destinationSystemId; }

    public CargoType getCargoType() { return cargoType; }
    public void setCargoType(CargoType cargoType) { this.cargoType = cargoType; }

    public int getProfit() { return profit; }
    public void setProfit(int profit) { this.profit = profit; }

    public int getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(int securityLevel) { this.securityLevel = securityLevel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Long getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(Long ownerFactionId) { this.ownerFactionId = ownerFactionId; }

    public GameSession getGameSession() { return gameSession; }
    public void setGameSession(GameSession gameSession) { this.gameSession = gameSession; }

    public enum CargoType {
        MINERALS,
        ENERGY,
        FOOD,
        RESEARCH,
        GENERAL
    }
}

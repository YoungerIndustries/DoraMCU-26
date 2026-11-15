package com.yisy.modules.doramcu.traveller.exploration;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity for planet landing records.
 */
@Entity
@Table(name = "traveller_planet_landing")
public class PlanetLanding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String characterId;

    @Column(nullable = false)
    private String systemName;

    @Column(nullable = false)
    private String hexSector;

    private String planetName;

    /** Landing outcome: SUCCESS, FAILURE, ACCIDENT, ENCOUNTER */
    @Column(nullable = false)
    private String outcome;

    private String description;

    /** Credits spent on landing */
    private int creditsSpent;

    /** Experience points earned */
    private int xpEarned;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    public PlanetLanding() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCharacterId() { return characterId; }
    public void setCharacterId(String characterId) { this.characterId = Objects.requireNonNull(characterId); }

    public String getSystemName() { return systemName; }
    public void setSystemName(String systemName) { this.systemName = Objects.requireNonNull(systemName); }

    public String getHexSector() { return hexSector; }
    public void setHexSector(String hexSector) { this.hexSector = Objects.requireNonNull(hexSector); }

    public String getPlanetName() { return planetName; }
    public void setPlanetName(String planetName) { this.planetName = planetName; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = Objects.requireNonNull(outcome); }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getCreditsSpent() { return creditsSpent; }
    public void setCreditsSpent(int creditsSpent) { this.creditsSpent = creditsSpent; }

    public int getXpEarned() { return xpEarned; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

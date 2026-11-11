package com.yisy.modules.doramcu.star_trek_4x.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced star system with hybrid UWP + Star Trek properties.
 * <p>
 * Extends the hex.SolarSystem concept with Traveller UWP codes (atmosphere, gravity,
 * size, hydrosphere, population, government, law, trade) PLUS Star Trek fields
 * (alignment, native species, warp core deposits, notable features).
 * Links to Earth 2026 planet data via earth2026PlanetId.
 * </p>
 */
@Entity
@Table(name = "st4x_star_systems")
public class StarSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Sector ID this system belongs to. */
    @Column(nullable = false)
    private int sectorId;

    /** SubSector ID within the sector. */
    @Column(nullable = false)
    private int subSectorId;

    /** Position in subsector 3D hex grid. */
    @Column(nullable = false)
    private int posX, posY, posZ;

    // --- Traveller UWP codes ---

    /** Atmosphere code (0-9, A-P, none). */
    @Column(length = 5)
    private String uwpAtmosphere;

    /** Gravity code (0-9, A, none). */
    @Column(length = 5)
    private String uwpGravity;

    /** Size code (0-9). */
    @Column(length = 5)
    private String uwpSize;

    /** Hydrosphere code (0-9). */
    @Column(length = 5)
    private String uwpHydrosphere;

    /** Population code (0-9, A). */
    @Column(length = 5)
    private String uwpPopulation;

    /** Government code (0-9, A-F). */
    @Column(length = 5)
    private String uwpGovernment;

    /** Law level (0-9, A). */
    @Column(length = 5)
    private String uwpLaw;

    /** Trade codes. */
    @Column(length = 50)
    private String uwpTrade;

    // --- Star Trek specific fields ---

    /** Alignment: FEDERATION, KLINGON, ROMULAN, NEUTRAL, HOSTILE, UNKNOWN. */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StarAlignment alignment = StarAlignment.UNKNOWN;

    /** Native species on this system's planets. */
    @ElementCollection
    @CollectionTable(name = "st4x_system_species", joinColumns = @JoinColumn(name = "system_id"))
    @Column(name = "species_name")
    private List<String> nativeSpecies = new ArrayList<>();

    /** Whether this system has warp core deposits. */
    @Column(nullable = false)
    private boolean hasWarpDeposits = false;

    /** Notable features: ancient ruins, nebula, black hole, etc. */
    @Column(length = 500)
    private String notableFeatures;

    /** Trade value of this system. */
    @Column(nullable = false)
    private int tradeValue = 0;

    /** Link to Earth 2026 planet data. */
    @Column(length = 50)
    private String earth2026PlanetId;

    /** Whether this system has been discovered. */
    @Column(nullable = false)
    private boolean discovered = false;

    /** Owner faction ID. */
    @Column
    private Long ownerFactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    public StarSystem() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSectorId() { return sectorId; }
    public void setSectorId(int sectorId) { this.sectorId = sectorId; }

    public int getSubSectorId() { return subSectorId; }
    public void setSubSectorId(int subSectorId) { this.subSectorId = subSectorId; }

    public int getPosX() { return posX; }
    public void setPosX(int posX) { this.posX = posX; }

    public int getPosY() { return posY; }
    public void setPosY(int posY) { this.posY = posY; }

    public int getPosZ() { return posZ; }
    public void setPosZ(int posZ) { this.posZ = posZ; }

    public String getUwpAtmosphere() { return uwpAtmosphere; }
    public void setUwpAtmosphere(String uwpAtmosphere) { this.uwpAtmosphere = uwpAtmosphere; }

    public String getUwpGravity() { return uwpGravity; }
    public void setUwpGravity(String uwpGravity) { this.uwpGravity = uwpGravity; }

    public String getUwpSize() { return uwpSize; }
    public void setUwpSize(String uwpSize) { this.uwpSize = uwpSize; }

    public String getUwpHydrosphere() { return uwpHydrosphere; }
    public void setUwpHydrosphere(String uwpHydrosphere) { this.uwpHydrosphere = uwpHydrosphere; }

    public String getUwpPopulation() { return uwpPopulation; }
    public void setUwpPopulation(String uwpPopulation) { this.uwpPopulation = uwpPopulation; }

    public String getUwpGovernment() { return uwpGovernment; }
    public void setUwpGovernment(String uwpGovernment) { this.uwpGovernment = uwpGovernment; }

    public String getUwpLaw() { return uwpLaw; }
    public void setUwpLaw(String uwpLaw) { this.uwpLaw = uwpLaw; }

    public String getUwpTrade() { return uwpTrade; }
    public void setUwpTrade(String uwpTrade) { this.uwpTrade = uwpTrade; }

    public StarAlignment getAlignment() { return alignment; }
    public void setAlignment(StarAlignment alignment) { this.alignment = alignment; }

    public List<String> getNativeSpecies() { return nativeSpecies; }
    public void setNativeSpecies(List<String> nativeSpecies) { this.nativeSpecies = nativeSpecies; }

    public boolean isHasWarpDeposits() { return hasWarpDeposits; }
    public void setHasWarpDeposits(boolean hasWarpDeposits) { this.hasWarpDeposits = hasWarpDeposits; }

    public String getNotableFeatures() { return notableFeatures; }
    public void setNotableFeatures(String notableFeatures) { this.notableFeatures = notableFeatures; }

    public int getTradeValue() { return tradeValue; }
    public void setTradeValue(int tradeValue) { this.tradeValue = tradeValue; }

    public String getEarth2026PlanetId() { return earth2026PlanetId; }
    public void setEarth2026PlanetId(String earth2026PlanetId) { this.earth2026PlanetId = earth2026PlanetId; }

    public boolean isDiscovered() { return discovered; }
    public void setDiscovered(boolean discovered) { this.discovered = discovered; }

    public Long getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(Long ownerFactionId) { this.ownerFactionId = ownerFactionId; }

    public GameSession getGameSession() { return gameSession; }
    public void setGameSession(GameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Get the full UWP code as an 8-character string.
     */
    public String getUwpCode() {
        return (uwpAtmosphere != null ? uwpAtmosphere : "0") +
               (uwpGravity != null ? uwpGravity : "0") +
               (uwpSize != null ? uwpSize : "0") +
               (uwpHydrosphere != null ? uwpHydrosphere : "0") +
               (uwpPopulation != null ? uwpPopulation : "0") +
               (uwpGovernment != null ? uwpGovernment : "0") +
               (uwpLaw != null ? uwpLaw : "0") +
               (uwpTrade != null ? uwpTrade : "0");
    }

    public enum StarAlignment {
        FEDERATION,
        KLINGON,
        ROMULAN,
        CARDASSIAN,
        FERENGI,
        BORG,
        NEUTRAL,
        HOSTILE,
        UNKNOWN
    }
}

package com.yisy.modules.doramcu.model.galaxy;
import com.yisy.modules.doramcu.hex.Hex;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;


import jakarta.persistence.*;

/**
 * JPA entity representing a hyperlane connection between two solar systems.
 * <p>
 * Hyperlanes enable faster-than-light travel between connected solar systems.
 * They are persisted separately from the in-memory {@link com.yisy.modules.doramcu.hex.Hyperlane}
 * so that hyperlanes can be generated from database records after streamed galaxy
 * generation completes.
 * </p>
 */
@Entity
@Table(name = "hyperlanes", indexes = {
    @Index(name = "idx_hyperlane_galaxy", columnList = "galaxy_id"),
    @Index(name = "idx_hyperlane_from", columnList = "from_system_id"),
    @Index(name = "idx_hyperlane_to", columnList = "to_system_id")
})
public class Hyperlane {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "galaxy_id", nullable = false)
    private Galaxy galaxy;

    /** The source solar system ID (in-memory hex system ID). */
    @Column(name = "from_system_id", nullable = false)
    private int fromSystemId;

    /** The destination solar system ID (in-memory hex system ID). */
    @Column(name = "to_system_id", nullable = false)
    private int toSystemId;

    /** The travel distance between systems. */
    @Column(nullable = false)
    private int distance;

    public Hyperlane() {
    }

    public Hyperlane(Galaxy galaxy, int fromSystemId, int toSystemId, int distance) {
        this.galaxy = galaxy;
        this.fromSystemId = fromSystemId;
        this.toSystemId = toSystemId;
        this.distance = distance;
    }

    /**
     * Creates a hyperlane between two worlds (used by Traveller module).
     */
    public Hyperlane(com.yisy.modules.doramcu.traveller.TravellerWorld from,
                     com.yisy.modules.doramcu.traveller.TravellerWorld to) {
        this.fromSystemId = from.getId();
        this.toSystemId = to.getId();
        this.distance = Math.max(1, (int) Math.sqrt(
                Math.pow(from.getSectorX() - to.getSectorX(), 2) +
                Math.pow(from.getSectorY() - to.getSectorY(), 2)));
    }

    /**
     * Checks if this hyperlane connects to the given world.
     */
    public boolean connects(com.yisy.modules.doramcu.traveller.TravellerWorld world) {
        return fromSystemId == world.getId() || toSystemId == world.getId();
    }

    /**
     * Returns the from system ID.
     */
    public int getFrom() {
        return fromSystemId;
    }

    /**
     * Returns the to system ID.
     */
    public int getTo() {
        return toSystemId;
    }

    /**
     * Returns the distance in parsecs.
     */
    public int getDistanceParsecs() {
        return distance;
    }

    /**
     * Returns whether this hyperlane is safe for travel.
     */
    public boolean isSafe() {
        return true;
    }

    /**
     * Returns the other end of this hyperlane given one end.
     */
    public com.yisy.modules.doramcu.traveller.TravellerWorld getOtherEnd(com.yisy.modules.doramcu.traveller.TravellerWorld world) {
        if (fromSystemId == world.getId()) {
            // Would need to look up the world by toSystemId - return null for now
            return null;
        } else if (toSystemId == world.getId()) {
            return null;
        }
        return null;
    }

    /**
     * Checks if this hyperlane is traversable with the given jump rating.
     */
    public boolean isTraversable(int jumpRating) {
        return distance <= jumpRating;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Galaxy getGalaxy() {
        return galaxy;
    }

    public void setGalaxy(Galaxy galaxy) {
        this.galaxy = galaxy;
    }

    public int getFromSystemId() {
        return fromSystemId;
    }

    public void setFromSystemId(int fromSystemId) {
        this.fromSystemId = fromSystemId;
    }

    public int getToSystemId() {
        return toSystemId;
    }

    public void setToSystemId(int toSystemId) {
        this.toSystemId = toSystemId;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * Checks if this hyperlane connects to the given system ID.
     *
     * @param systemId the in-memory hex system ID to check
     * @return true if connected
     */
    public boolean connectsTo(int systemId) {
        return fromSystemId == systemId || toSystemId == systemId;
    }

    @Override
    public String toString() {
        return String.format("Hyperlane{from=%d, to=%d, distance=%d}", fromSystemId, toSystemId, distance);
    }
}

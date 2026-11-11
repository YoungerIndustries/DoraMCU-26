package com.yisy.modules.doramcu.dto;

/**
 * Request body for creating a wormhole pair between two galaxies.
 * <p>
 * Both galaxy names are required. The solar system IDs are optional;
 * if not provided, the system will select a solar system automatically.
 * </p>
 */
public class WormholePairRequest {

    /** The first galaxy to connect. */
    private String galaxyA;

    /** The second galaxy to connect. */
    private String galaxyB;

    /** Optional: solar system ID for wormhole in galaxy A. */
    private Long solarSystemIdA;

    /** Optional: solar system ID for wormhole in galaxy B. */
    private Long solarSystemIdB;

    /** Optional: custom name for wormhole in galaxy A. */
    private String nameA;

    /** Optional: custom name for wormhole in galaxy B. */
    private String nameB;

    public WormholePairRequest() {
    }

    public String getGalaxyA() {
        return galaxyA;
    }

    public void setGalaxyA(String galaxyA) {
        this.galaxyA = galaxyA;
    }

    public String getGalaxyB() {
        return galaxyB;
    }

    public void setGalaxyB(String galaxyB) {
        this.galaxyB = galaxyB;
    }

    public Long getSolarSystemIdA() {
        return solarSystemIdA;
    }

    public void setSolarSystemIdA(Long solarSystemIdA) {
        this.solarSystemIdA = solarSystemIdA;
    }

    public Long getSolarSystemIdB() {
        return solarSystemIdB;
    }

    public void setSolarSystemIdB(Long solarSystemIdB) {
        this.solarSystemIdB = solarSystemIdB;
    }

    public String getNameA() {
        return nameA;
    }

    public void setNameA(String nameA) {
        this.nameA = nameA;
    }

    public String getNameB() {
        return nameB;
    }

    public void setNameB(String nameB) {
        this.nameB = nameB;
    }
}

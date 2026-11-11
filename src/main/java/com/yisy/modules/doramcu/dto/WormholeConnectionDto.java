package com.yisy.modules.doramcu.dto;

/**
 * DTO representing a wormhole connection between two galaxies.
 * <p>
 * Contains both wormholes in a pair, showing the bidirectional
 * connection between two galaxies.
 * </p>
 */
public class WormholeConnectionDto {

    private WormholeInfoDto wormholeA;
    private WormholeInfoDto wormholeB;
    private String galaxyA;
    private String galaxyB;

    public WormholeConnectionDto() {
    }

    public WormholeInfoDto getWormholeA() {
        return wormholeA;
    }

    public void setWormholeA(WormholeInfoDto wormholeA) {
        this.wormholeA = wormholeA;
    }

    public WormholeInfoDto getWormholeB() {
        return wormholeB;
    }

    public void setWormholeB(WormholeInfoDto wormholeB) {
        this.wormholeB = wormholeB;
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
}

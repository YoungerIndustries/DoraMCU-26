package com.yisy.modules.doramcu.dto;

/**
 * Response DTO for wormhole travel.
 * <p>
 * Contains the result of a wormhole travel attempt, including the destination
 * galaxy and any conversion details if the player crossed game type boundaries.
 * </p>
 */
public class WormholeTravelResponse {

    /** The username of the player who traveled. */
    private String playerName;

    /** The galaxy the player traveled from. */
    private String fromGalaxy;

    /** The galaxy the player arrived in. */
    private String toGalaxy;

    /** Whether character conversion was performed (different game types). */
    private boolean converted;

    /** Optional details about character conversion. */
    private String conversionDetails;

    public WormholeTravelResponse() {
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getFromGalaxy() {
        return fromGalaxy;
    }

    public void setFromGalaxy(String fromGalaxy) {
        this.fromGalaxy = fromGalaxy;
    }

    public String getToGalaxy() {
        return toGalaxy;
    }

    public void setToGalaxy(String toGalaxy) {
        this.toGalaxy = toGalaxy;
    }

    public boolean isConverted() {
        return converted;
    }

    public void setConverted(boolean converted) {
        this.converted = converted;
    }

    public String getConversionDetails() {
        return conversionDetails;
    }

    public void setConversionDetails(String conversionDetails) {
        this.conversionDetails = conversionDetails;
    }
}

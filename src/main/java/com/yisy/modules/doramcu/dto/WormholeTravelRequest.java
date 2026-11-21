package com.yisy.modules.doramcu.dto;
import com.yisy.modules.doramcu.model.players.Player;

/**
 * Request body for initiating wormhole travel.
 * <p>
 * The player's current galaxy is inferred from their galaxy binding in the database.
 * </p>
 */
public class WormholeTravelRequest {

    /** The username of the player initiating travel. */
    private String playerName;

    public WormholeTravelRequest() {
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
}

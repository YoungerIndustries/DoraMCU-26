package com.yisy.modules.doramcu.starshiptroopers.model;
import com.yisy.modules.doramcu.model.players.Player;

import java.util.List;

/**
 * Represents a combat phase in a player turn.
 */
public record Phase(
    String name,
    String description
) {
}

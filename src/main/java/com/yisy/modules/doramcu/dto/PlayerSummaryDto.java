package com.yisy.modules.doramcu.dto;
import com.yisy.modules.doramcu.model.players.Player;

/**
 * Lightweight summary of a player for the empire selection screen.
 * Intentionally excludes sensitive data like loginPin and email.
 */
public record PlayerSummaryDto(
    Long id,
    String empireName,
    String governmentType,
    String ethos,
    String commanderName,
    boolean isAdmin
) {}

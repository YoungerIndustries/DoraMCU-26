package com.yisy.modules.doramcu.starwars.model;

import java.util.List;
import java.util.Map;

/**
 * Represents lore data about the Star Wars universe (planets, eras, species, events).
 * Supports rich data like regions, locations, homeworlds, and more.
 */
public record Lore(
    String category,
    String id,
    String name,
    String description,
    List<String> details,
    String region,
    String period,
    String homeworld,
    List<String> notableLocations,
    List<String> inhabitants,
    String significance,
    List<String> keyEvents,
    String jediStatus,
    String sithStatus,
    List<String> notableMembers,
    String date,
    Map<String, Object> metadata
) {
    public Lore {
        if (category == null || category.isBlank())
            throw new IllegalArgumentException("Lore category must not be blank");
        if (id == null || id.isBlank())
            throw new IllegalArgumentException("Lore id must not be blank");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Lore name must not be blank");
        if (description == null) description = "";
        if (details == null) details = List.of();
        if (region == null) region = "";
        if (period == null) period = "";
        if (homeworld == null) homeworld = "";
        if (notableLocations == null) notableLocations = List.of();
        if (inhabitants == null) inhabitants = List.of();
        if (significance == null) significance = "";
        if (keyEvents == null) keyEvents = List.of();
        if (jediStatus == null) jediStatus = "";
        if (sithStatus == null) sithStatus = "";
        if (notableMembers == null) notableMembers = List.of();
        if (date == null) date = "";
        if (metadata == null) metadata = Map.of();
    }
}

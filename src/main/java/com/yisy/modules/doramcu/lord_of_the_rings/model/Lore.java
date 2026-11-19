package com.yisy.modules.doramcu.lord_of_the_rings.model;

import java.util.List;
import java.util.Map;

/**
 * Represents lore data about the Lord of the Rings universe (realms, constellations, Valar planets, species, events).
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
    String elvenStatus,
    String sauronStatus,
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
        if (elvenStatus == null) elvenStatus = "";
        if (sauronStatus == null) sauronStatus = "";
        if (notableMembers == null) notableMembers = List.of();
        if (date == null) date = "";
        if (metadata == null) metadata = Map.of();
    }
}

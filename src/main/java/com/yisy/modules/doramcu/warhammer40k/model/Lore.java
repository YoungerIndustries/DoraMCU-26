package com.yisy.modules.doramcu.warhammer40k.model;

import java.util.List;
import java.util.Map;

/**
 * Represents lore entries for the Warhammer 40K universe.
 */
public record Lore(
    String title,
    String description,
    List<String> sourceMaterial,
    Map<String, Map<String, Object>> factions,
    List<Era> eras
) {
    public record Era(
        String name,
        String period,
        String description,
        List<String> keyEvents
    ) {
    }
}

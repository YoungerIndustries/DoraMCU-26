package com.yisy.modules.doramcu.starwars.model;

import java.util.List;

/**
 * Represents a military branch of a Star Wars faction.
 */
public record MilitaryBranch(
    String name,
    String description,
    List<String> unitTypes
) {
}

package com.yisy.modules.doramcu.lord_of_the_rings.model;

import java.util.List;

/**
 * Represents a military branch of a Lord of the Rings faction.
 */
public record MilitaryBranch(
    String name,
    String description,
    List<String> unitTypes
) {
}

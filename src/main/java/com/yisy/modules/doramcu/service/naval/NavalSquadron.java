package com.yisy.modules.doramcu.service.naval;

/**
 * Represents a naval squadron within a fleet.
 */
public class NavalSquadron {

    private final int id;
    private final String name;

    public NavalSquadron(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
}

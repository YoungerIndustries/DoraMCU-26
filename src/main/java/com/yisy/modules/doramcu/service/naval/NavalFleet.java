package com.yisy.modules.doramcu.service.naval;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a naval fleet composed of squadrons.
 */
public class NavalFleet {

    public enum FleetType {
        SUB_SECTORAL, SECTORAL, REGIONAL, IMPERIAL
    }

    private final int id;
    private final String name;
    private final FleetType type;
    private final List<NavalSquadron> squadrons = new ArrayList<>();

    public NavalFleet(int id, String name, FleetType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public FleetType getType() { return type; }
    public List<NavalSquadron> getSquadrons() { return squadrons; }

    public void addSquadron(NavalSquadron squadron) {
        squadrons.add(squadron);
    }
}

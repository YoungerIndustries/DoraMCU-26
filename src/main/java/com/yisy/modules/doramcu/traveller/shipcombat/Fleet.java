package com.yisy.modules.doramcu.traveller.shipcombat;

import com.yisy.modules.doramcu.traveller.HighGuardShipDesign;
import com.yisy.modules.doramcu.traveller.ShipTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a fleet of ships in a ship-to-ship battle.
 * <p>
 * A fleet belongs to one side (0 or 1) and contains multiple ships
 * created from {@link ShipTemplate} instances.
 * </p>
 */
public class Fleet {

    private final String id;
    private final int side;
    private final String name;
    private final List<FleetShip> ships;

    /**
     * A ship within a fleet.
     */
    public static class FleetShip {
        private final String templateName;
        private final ShipTemplate.ShipClass shipClass;
        private final int tonnage;
        private final int hullPoints;
        private final int currentHullPoints;
        private final boolean destroyed;

        FleetShip(ShipTemplate template, int currentHullPoints, boolean destroyed) {
            this.templateName = template.getName();
            this.shipClass = template.getShipClass();
            this.tonnage = template.getTotalTonnage();
            this.hullPoints = template.toDesign().getHullPoints();
            this.currentHullPoints = currentHullPoints;
            this.destroyed = destroyed;
        }

        FleetShip(HighGuardShipDesign design, int currentHullPoints, boolean destroyed) {
            this.templateName = design.getName();
            this.shipClass = null;
            this.tonnage = design.getTotalTonnage();
            this.hullPoints = design.getHullPoints();
            this.currentHullPoints = currentHullPoints;
            this.destroyed = destroyed;
        }

        public String getTemplateName() { return templateName; }
        public ShipTemplate.ShipClass getShipClass() { return shipClass; }
        public int getTonnage() { return tonnage; }
        public int getHullPoints() { return hullPoints; }
        public int getCurrentHullPoints() { return currentHullPoints; }
        public boolean isDestroyed() { return destroyed; }
    }

    private Fleet(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "Fleet ID must not be null");
        this.side = builder.side;
        this.name = Objects.requireNonNull(builder.name, "Fleet name must not be null");
        this.ships = Collections.unmodifiableList(new ArrayList<>(builder.ships));
    }

    public String getId() { return id; }
    public int getSide() { return side; }
    public String getName() { return name; }
    public List<FleetShip> getShips() { return ships; }

    /**
     * Returns the total tonnage of all ships in the fleet.
     */
    public int getTotalTonnage() {
        return ships.stream().mapToInt(FleetShip::getTonnage).sum();
    }

    /**
     * Returns the number of active (non-destroyed) ships.
     */
    public int getActiveShipCount() {
        return (int) ships.stream().filter(s -> !s.isDestroyed()).count();
    }

    /**
     * Returns the total hull points of all ships.
     */
    public int getTotalHullPoints() {
        return ships.stream().mapToInt(FleetShip::getHullPoints).sum();
    }

    /**
     * Returns the current total hull points of all ships.
     */
    public int getCurrentHullPoints() {
        return ships.stream().mapToInt(FleetShip::getCurrentHullPoints).sum();
    }

    /**
     * Creates a new builder for a Fleet.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private int side;
        private String name;
        private final List<FleetShip> ships = new ArrayList<>();

        public Builder id(String id) { this.id = id; return this; }
        public Builder side(int side) { this.side = side; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder ship(FleetShip ship) { this.ships.add(ship); return this; }
        public Builder ships(List<FleetShip> ships) { this.ships.addAll(ships); return this; }

        public Fleet build() {
            return new Fleet(this);
        }
    }
}

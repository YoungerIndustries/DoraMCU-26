package com.yisy.modules.doramcu.shipdesign;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a preset ship template that can be used to quickly create ships
 * with predefined characteristics.
 * <p>
 * Templates provide a starting point for ship design, following common FullThrust
 * archetypes like scouts, fighters, cruisers, and battleships.
 * </p>
 */
public class ShipTemplate {

    private final String name;
    private final Ship.Size size;
    private final int speed;
    private final int armor;
    private final int hitPoints;
    private final int sensorRange;
    private final int crewCapacity;
    private final int maxFuel;
    private final List<Ship.Weapon> weapons;
    private final String description;

    /**
     * Private constructor - use the builder to create templates.
     */
    private ShipTemplate(Builder builder) {
        this.name = builder.name;
        this.size = builder.size;
        this.speed = builder.speed;
        this.armor = builder.armor;
        this.hitPoints = builder.hitPoints;
        this.sensorRange = builder.sensorRange;
        this.crewCapacity = builder.crewCapacity;
        this.maxFuel = builder.maxFuel;
        this.weapons = new ArrayList<>(builder.weapons);
        this.description = builder.description;
    }

    /**
     * Returns the template's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ship size for this template.
     */
    public Ship.Size getSize() {
        return size;
    }

    /**
     * Returns the speed for this template.
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Returns the armor for this template.
     */
    public int getArmor() {
        return armor;
    }

    /**
     * Returns the hit points for this template.
     */
    public int getHitPoints() {
        return hitPoints;
    }

    /**
     * Returns the sensor range for this template.
     */
    public int getSensorRange() {
        return sensorRange;
    }

    /**
     * Returns the crew capacity for this template.
     */
    public int getCrewCapacity() {
        return crewCapacity;
    }

    /**
     * Returns the maximum fuel for this template.
     */
    public int getMaxFuel() {
        return maxFuel;
    }

    /**
     * Returns the weapons defined in this template.
     */
    public List<Ship.Weapon> getWeapons() {
        return new ArrayList<>(weapons);
    }

    /**
     * Returns the template's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Creates a new Ship from this template with the given name.
     *
     * @param shipName the name for the new ship
     * @return a new Ship instance based on this template
     */
    public Ship buildShip(String shipName) {
        Ship ship = new Ship(shipName, size, speed, armor, hitPoints,
                sensorRange, crewCapacity, maxFuel);
        for (Ship.Weapon weapon : weapons) {
            ship.addWeapon(weapon);
        }
        return ship;
    }

    /**
     * Creates a ShipBuilder pre-configured with this template's values.
     * Allows further customization before building.
     *
     * @return a configured ShipBuilder
     */
    public ShipBuilder toBuilder() {
        ShipBuilder builder = new ShipBuilder()
                .size(size)
                .speed(speed)
                .armor(armor)
                .hitPoints(hitPoints)
                .sensorRange(sensorRange)
                .crewCapacity(crewCapacity)
                .maxFuel(maxFuel);

        for (Ship.Weapon weapon : weapons) {
            builder.addWeapon(weapon);
        }

        return builder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipTemplate that = (ShipTemplate) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "ShipTemplate{" +
                "name='" + name + '\'' +
                ", size=" + size +
                ", speed=" + speed +
                ", armor=" + armor +
                ", hp=" + hitPoints +
                ", weapons=" + weapons.size() +
                ", description='" + description + '\'' +
                '}';
    }

    /**
     * Builder for creating ShipTemplate instances.
     */
    public static class Builder {
        private String name;
        private Ship.Size size;
        private int speed;
        private int armor;
        private int hitPoints;
        private int sensorRange;
        private int crewCapacity;
        private int maxFuel;
        private final List<Ship.Weapon> weapons = new ArrayList<>();
        private String description;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder size(Ship.Size size) {
            this.size = size;
            return this;
        }

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public Builder armor(int armor) {
            this.armor = armor;
            return this;
        }

        public Builder hitPoints(int hitPoints) {
            this.hitPoints = hitPoints;
            return this;
        }

        public Builder sensorRange(int sensorRange) {
            this.sensorRange = sensorRange;
            return this;
        }

        public Builder crewCapacity(int crewCapacity) {
            this.crewCapacity = crewCapacity;
            return this;
        }

        public Builder maxFuel(int maxFuel) {
            this.maxFuel = maxFuel;
            return this;
        }

        public Builder addWeapon(Ship.Weapon weapon) {
            this.weapons.add(weapon);
            return this;
        }

        public Builder addBeam(String name, int strength, int range, int arcs) {
            this.weapons.add(new Ship.Weapon(Ship.WeaponType.BEAM, name, strength, range, arcs));
            return this;
        }

        public Builder addMissile(String name, int strength, int range, int arcs) {
            this.weapons.add(new Ship.Weapon(Ship.WeaponType.MISSILE, name, strength, range, arcs));
            return this;
        }

        public Builder addTorpedo(String name, int strength, int range, int arcs) {
            this.weapons.add(new Ship.Weapon(Ship.WeaponType.TORPEDO, name, strength, range, arcs));
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public ShipTemplate build() {
            if (name == null || name.isEmpty()) {
                throw new IllegalStateException("Template name is required");
            }
            if (size == null) {
                throw new IllegalStateException("Template size is required");
            }
            return new ShipTemplate(this);
        }
    }

    // --- Built-in templates ---

    /**
     * Returns a Scout template: fast, lightly armed, good sensors.
     */
    public static ShipTemplate scout() {
        return new Builder()
                .name("Scout")
                .size(Ship.Size.SMALL)
                .speed(10)
                .armor(1)
                .hitPoints(3)
                .sensorRange(5)
                .crewCapacity(2)
                .maxFuel(5)
                .addBeam("Scanner Laser", 1, 4, 1)
                .description("Fast reconnaissance vessel with enhanced sensors")
                .build();
    }

    /**
     * Returns a Fighter template: balanced speed and firepower.
     */
    public static ShipTemplate fighter() {
        return new Builder()
                .name("Fighter")
                .size(Ship.Size.SMALL)
                .speed(8)
                .armor(2)
                .hitPoints(4)
                .sensorRange(3)
                .crewCapacity(2)
                .maxFuel(3)
                .addBeam("Port Laser", 2, 5, 1)
                .addBeam("Starboard Laser", 2, 5, 1)
                .addMissile("Missile Rack", 3, 8, 2)
                .description("Balanced combat fighter with beam and missile weapons")
                .build();
    }

    /**
     * Returns a Cruiser template: medium-sized with strong armament.
     */
    public static ShipTemplate cruiser() {
        return new Builder()
                .name("Cruiser")
                .size(Ship.Size.LARGE)
                .speed(5)
                .armor(4)
                .hitPoints(12)
                .sensorRange(5)
                .crewCapacity(12)
                .maxFuel(12)
                .addBeam("Main Battery", 4, 6, 1)
                .addBeam("Side Battery", 2, 5, 2)
                .addTorpedo("Torpedo Tubes", 5, 10, 1)
                .description("Medium cruiser with heavy beam and torpedo armament")
                .build();
    }

    /**
     * Returns a Battleship template: heavily armored and armed.
     */
    public static ShipTemplate battleship() {
        return new Builder()
                .name("Battleship")
                .size(Ship.Size.HUGE)
                .speed(3)
                .armor(6)
                .hitPoints(20)
                .sensorRange(6)
                .crewCapacity(20)
                .maxFuel(15)
                .addBeam("Main Battery", 6, 8, 1)
                .addBeam("Secondary Battery", 4, 6, 2)
                .addTorpedo("Torpedo Tubes", 7, 12, 1)
                .addMissile("Missile Battery", 4, 10, 3)
                .description("Heavy battleship with maximum firepower and armor")
                .build();
    }

    /**
     * Returns a Transport template: high capacity for cargo and crew.
     */
    public static ShipTemplate transport() {
        return new Builder()
                .name("Transport")
                .size(Ship.Size.LARGE)
                .speed(4)
                .armor(3)
                .hitPoints(10)
                .sensorRange(3)
                .crewCapacity(25)
                .maxFuel(20)
                .addBeam("Defense Laser", 1, 4, 2)
                .description("Cargo transport with high crew and fuel capacity")
                .build();
    }
}

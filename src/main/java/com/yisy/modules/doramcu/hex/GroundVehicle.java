package com.yisy.modules.doramcu.hex;

import com.yisy.modules.doramcu.hex.dungeon.GroundVehicleModuleTemplate;
import com.yisy.modules.doramcu.hex.Decal;
import com.yisy.modules.doramcu.hex.Skin;

import java.util.*;

/**
 * Represents a ground combat vehicle built from module templates with 3D structure.
 * <p>
 * A GroundVehicle combines the template-based 3D hull construction pattern used by
 * {@link SpaceStation} and {@link Ship} with ground combat-specific stats. Each vehicle
 * is assembled from one or more {@link GroundVehicleModuleTemplate} instances, producing
 * a {@link HullModule} with {@link Compartment} objects for named functional areas
 * (turret, engine bay, crew compartment, etc.).
 * </p>
 * <p>
 * Unlike {@link com.yisy.modules.doramcu.hex.planet.SurfaceVehicle}, which uses simple
 * enum-based creation for transport logistics, GroundVehicle supports:
 * </p>
 * <ul>
 *   <li>3D voxel-based hull structure with per-cell damage tracking</li>
 *   <li>Named compartments with type-specific damage effects</li>
 *   <li>Accumulated combat stats from module templates (armor, firepower, speed)</li>
 *   <li>Interior dungeon rooms for large vehicles (tanks, APCs, aircraft)</li>
 *   <li>Terrain compatibility checking</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * // Build a tank from templates
 * GroundVehicle tank = builder.buildVehicle("M1 Abrams",
 *     Arrays.asList(turretTemplateId, hullTemplateId, engineTemplateId));
 *
 * // Damage a specific compartment
 * tank.damageHullCell(0, 0, 1, 15); // Hit the turret
 *
 * // Check if engine is destroyed
 * boolean engineDestroyed = tank.isCompartmentDestroyed("Engine Bay");
 * </pre>
 * </p>
 */
public class GroundVehicle {

    private final String id;
    private final GroundVehicleModuleTemplate.GroundVehicleModuleType type;
    private String name;
    private Hex3D currentPosition;

    // ---- 3D Hull Structure ----
    private final List<HullModule> hullModules;
    private final Map<String, Compartment> compartments;

    // ---- Combat Stats (accumulated from templates) ----
    private int armorRating;
    private int currentArmor;
    private int firepower;
    private int speedKmh;
    private int crewCapacity;
    private int currentCrew;
    private double fuelLevel;
    private double maxFuel;
    private double fuelConsumption;
    private int detectionRange;
    private int supplyCost;

    // ---- Transport Stats (for civilian vehicles) ----
    private int passengerCapacity;
    private int currentPassengers;
    private int cargoCapacity;
    private final Map<String, Integer> cargoInventory;
    private int constructionCost;
    private int maintenanceCost;
    private String infrastructureRequirement;

    // ---- Terrain Compatibility ----
    private final Set<String> compatibleTerrain;

    // ---- Ownership and State ----
    private String ownerEmpireId;
    private Planet stationedPlanet;
    private boolean active;

    // ---- Interior Dungeon Integration ----
    private String interiorDungeonId;

    // ---- AI Core integration ----
    private AiCoreComponent aiCore;

    // ---- Skin support ----
    private Skin skin;
    private final List<Decal> decals;

    /**
     * Creates a new ground combat vehicle.
     *
     * @param id the unique identifier
     * @param type the vehicle module type
     */
    public GroundVehicle(String id, GroundVehicleModuleTemplate.GroundVehicleModuleType type) {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Vehicle ID must not be empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type must not be null");
        }
        this.id = id;
        this.type = type;
        this.name = type.name() + "-" + id;
        this.currentPosition = null;
        this.hullModules = new ArrayList<>();
        this.compartments = new LinkedHashMap<>();
        this.armorRating = 0;
        this.currentArmor = 0;
        this.firepower = 0;
        this.speedKmh = 0;
        this.crewCapacity = 0;
        this.currentCrew = 0;
        this.fuelLevel = 0;
        this.maxFuel = 0;
        this.fuelConsumption = 0;
        this.detectionRange = 0;
        this.supplyCost = 0;
        this.passengerCapacity = 0;
        this.currentPassengers = 0;
        this.cargoCapacity = 0;
        this.cargoInventory = new LinkedHashMap<>();
        this.constructionCost = 0;
        this.maintenanceCost = 0;
        this.infrastructureRequirement = null;
        this.compatibleTerrain = new HashSet<>();
        this.ownerEmpireId = null;
        this.stationedPlanet = null;
        this.active = true;
        this.interiorDungeonId = null;
        this.decals = new ArrayList<>();
    }

    // ---- Accessors ----

    /** @return the unique vehicle ID */
    public String getId() {
        return id;
    }

    /** @return the vehicle module type */
    public GroundVehicleModuleTemplate.GroundVehicleModuleType getType() {
        return type;
    }

    /** @return the display name */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /** @return the current hex position */
    public Hex3D getCurrentPosition() {
        return currentPosition;
    }

    /**
     * Sets the current hex position.
     *
     * @param position the hex position
     */
    public void setCurrentPosition(Hex3D position) {
        this.currentPosition = position;
    }

    // ---- Hull Module Access ----

    /** @return the list of hull modules */
    public List<HullModule> getHullModules() {
        return Collections.unmodifiableList(hullModules);
    }

    /**
     * Adds a hull module to this vehicle.
     *
     * @param module the hull module
     */
    public void addHullModule(HullModule module) {
        hullModules.add(module);
        // Index compartments from this module
        for (Compartment compartment : module.getCompartments()) {
            compartments.put(compartment.getName(), compartment);
        }
    }

    /** @return the map of named compartments */
    public Map<String, Compartment> getCompartments() {
        return Collections.unmodifiableMap(compartments);
    }

    /**
     * Returns a compartment by name.
     *
     * @param name the compartment name
     * @return the compartment or null if not found
     */
    public Compartment getCompartment(String name) {
        return compartments.get(name);
    }

    /** @return the number of hull modules */
    public int getHullModuleCount() {
        return hullModules.size();
    }

    // ---- Combat Stats ----

    /** @return the total armor rating */
    public int getArmorRating() {
        return armorRating;
    }

    /**
     * Sets the armor rating and initializes current armor.
     *
     * @param armorRating the armor rating
     */
    public void setArmorRating(int armorRating) {
        this.armorRating = armorRating;
        this.currentArmor = armorRating;
    }

    /** @return the current remaining armor */
    public int getCurrentArmor() {
        return currentArmor;
    }

    /**
     * Sets the current remaining armor.
     *
     * @param currentArmor the current armor
     */
    public void setCurrentArmor(int currentArmor) {
        this.currentArmor = Math.max(0, Math.min(currentArmor, armorRating));
    }

    /** @return the total firepower */
    public int getFirepower() {
        return firepower;
    }

    /**
     * Sets the firepower.
     *
     * @param firepower the firepower
     */
    public void setFirepower(int firepower) {
        this.firepower = firepower;
    }

    /** @return the maximum speed in km/h */
    public int getSpeedKmh() {
        return speedKmh;
    }

    /**
     * Sets the maximum speed.
     *
     * @param speedKmh the speed
     */
    public void setSpeedKmh(int speedKmh) {
        this.speedKmh = speedKmh;
    }

    /** @return the maximum crew capacity */
    public int getCrewCapacity() {
        return crewCapacity;
    }

    /**
     * Sets the crew capacity.
     *
     * @param crewCapacity the crew capacity
     */
    public void setCrewCapacity(int crewCapacity) {
        this.crewCapacity = crewCapacity;
    }

    /** @return the current crew count */
    public int getCurrentCrew() {
        return currentCrew;
    }

    /**
     * Sets the current crew count.
     *
     * @param currentCrew the crew count
     */
    public void setCurrentCrew(int currentCrew) {
        this.currentCrew = Math.max(0, Math.min(currentCrew, crewCapacity));
    }

    /** @return the current fuel level */
    public double getFuelLevel() {
        return fuelLevel;
    }

    /**
     * Sets the current fuel level.
     *
     * @param fuelLevel the fuel level
     */
    public void setFuelLevel(double fuelLevel) {
        this.fuelLevel = Math.max(0, Math.min(fuelLevel, maxFuel));
    }

    /** @return the maximum fuel capacity */
    public double getMaxFuel() {
        return maxFuel;
    }

    /**
     * Sets the maximum fuel capacity.
     *
     * @param maxFuel the max fuel
     */
    public void setMaxFuel(double maxFuel) {
        this.maxFuel = maxFuel;
        this.fuelLevel = Math.min(fuelLevel, maxFuel);
    }

    /** @return the fuel consumption per turn */
    public double getFuelConsumption() {
        return fuelConsumption;
    }

    /**
     * Sets the fuel consumption per turn.
     *
     * @param fuelConsumption the fuel consumption
     */
    public void setFuelConsumption(double fuelConsumption) {
        this.fuelConsumption = fuelConsumption;
    }

    /** @return the sensor detection range in hexes */
    public int getDetectionRange() {
        return detectionRange;
    }

    /**
     * Sets the sensor detection range.
     *
     * @param detectionRange the detection range
     */
    public void setDetectionRange(int detectionRange) {
        this.detectionRange = detectionRange;
    }

    /** @return the per-turn supply cost */
    public int getSupplyCost() {
        return supplyCost;
    }

    /**
     * Sets the per-turn supply cost.
     *
     * @param supplyCost the supply cost
     */
    public void setSupplyCost(int supplyCost) {
        this.supplyCost = supplyCost;
    }

    // ---- Transport Stats ----

    /** @return the maximum passenger capacity */
    public int getPassengerCapacity() {
        return passengerCapacity;
    }

    /**
     * Sets the passenger capacity.
     *
     * @param passengerCapacity the passenger capacity
     */
    public void setPassengerCapacity(int passengerCapacity) {
        this.passengerCapacity = passengerCapacity;
    }

    /** @return the current passenger count */
    public int getCurrentPassengers() {
        return currentPassengers;
    }

    /**
     * Sets the current passenger count.
     *
     * @param currentPassengers the passenger count
     */
    public void setCurrentPassengers(int currentPassengers) {
        this.currentPassengers = Math.max(0, Math.min(currentPassengers, passengerCapacity));
    }

    /** @return the maximum cargo capacity in units */
    public int getCargoCapacity() {
        return cargoCapacity;
    }

    /**
     * Sets the cargo capacity.
     *
     * @param cargoCapacity the cargo capacity
     */
    public void setCargoCapacity(int cargoCapacity) {
        this.cargoCapacity = cargoCapacity;
    }

    /** @return the cargo inventory map (cargo type -> amount) */
    public Map<String, Integer> getCargoInventory() {
        return Collections.unmodifiableMap(cargoInventory);
    }

    /** @return the total cargo currently on board */
    public int getTotalCargoOnBoard() {
        return cargoInventory.values().stream().mapToInt(Integer::intValue).sum();
    }

    /** @return the construction cost in credits */
    public int getConstructionCost() {
        return constructionCost;
    }

    /**
     * Sets the construction cost.
     *
     * @param constructionCost the construction cost
     */
    public void setConstructionCost(int constructionCost) {
        this.constructionCost = constructionCost;
    }

    /** @return the per-turn maintenance cost in credits */
    public int getMaintenanceCost() {
        return maintenanceCost;
    }

    /**
     * Sets the per-turn maintenance cost.
     *
     * @param maintenanceCost the maintenance cost
     */
    public void setMaintenanceCost(int maintenanceCost) {
        this.maintenanceCost = maintenanceCost;
    }

    /** @return the required infrastructure type, or null if none required */
    public String getInfrastructureRequirement() {
        return infrastructureRequirement;
    }

    /**
     * Sets the required infrastructure type.
     *
     * @param infrastructureRequirement the infrastructure requirement (NONE, ROAD, RAIL, WATER, AIRPORT)
     */
    public void setInfrastructureRequirement(String infrastructureRequirement) {
        this.infrastructureRequirement = infrastructureRequirement;
    }

    // ---- Transport Operations ----

    /**
     * Loads cargo onto this vehicle.
     *
     * @param cargoType the cargo type name
     * @param amount the amount to load
     * @return the actual amount loaded
     */
    public int loadCargo(String cargoType, int amount) {
        if (cargoType == null || cargoType.isEmpty()) {
            return 0;
        }
        int totalCargo = getTotalCargoOnBoard();
        int remainingCapacity = cargoCapacity - totalCargo;
        int loaded = Math.min(amount, Math.max(0, remainingCapacity));
        if (loaded > 0) {
            cargoInventory.merge(cargoType, loaded, Integer::sum);
        }
        return loaded;
    }

    /**
     * Unloads cargo from this vehicle.
     *
     * @param cargoType the cargo type name
     * @param amount the maximum amount to unload
     * @return the actual amount unloaded
     */
    public int unloadCargo(String cargoType, int amount) {
        if (cargoType == null || cargoType.isEmpty()) {
            return 0;
        }
        int available = cargoInventory.getOrDefault(cargoType, 0);
        int unloaded = Math.min(amount, available);
        if (unloaded > 0) {
            int remaining = available - unloaded;
            if (remaining == 0) {
                cargoInventory.remove(cargoType);
            } else {
                cargoInventory.put(cargoType, remaining);
            }
        }
        return unloaded;
    }

    /**
     * Boards passengers onto this vehicle.
     *
     * @param count number of passengers to board
     * @return the actual number boarded
     */
    public int boardPassengers(int count) {
        if (count <= 0) {
            return 0;
        }
        int remainingCapacity = passengerCapacity - currentPassengers;
        int boarded = Math.min(count, Math.max(0, remainingCapacity));
        currentPassengers += boarded;
        return boarded;
    }

    /**
     * Unboards passengers from this vehicle.
     *
     * @param count maximum number to unboard
     * @return the actual number unboarded
     */
    public int unboardPassengers(int count) {
        int unboarded = Math.min(count, currentPassengers);
        currentPassengers -= unboarded;
        return unboarded;
    }

    /**
     * Checks if this vehicle can operate with the given infrastructure.
     *
     * @param infrastructure the available infrastructure type
     * @return true if compatible or if no infrastructure is required
     */
    public boolean canOperateWithInfrastructure(String infrastructure) {
        if (infrastructureRequirement == null || infrastructureRequirement.isEmpty() ||
                infrastructureRequirement.equals("NONE")) {
            return true;
        }
        return infrastructureRequirement.equals(infrastructure);
    }

    // ---- Terrain Compatibility ----

    /** @return the set of compatible terrain types */
    public Set<String> getCompatibleTerrain() {
        return Collections.unmodifiableSet(compatibleTerrain);
    }

    /**
     * Sets the compatible terrain types.
     *
     * @param terrain comma-separated terrain type names
     */
    public void setCompatibleTerrain(String terrain) {
        this.compatibleTerrain.clear();
        if (terrain != null && !terrain.isBlank()) {
            for (String t : terrain.split(",")) {
                this.compatibleTerrain.add(t.trim().toUpperCase());
            }
        }
    }

    /**
     * Checks if this vehicle can operate on the given terrain.
     *
     * @param terrain the terrain type name
     * @return true if compatible or if no terrain restriction is set
     */
    public boolean isCompatibleWithTerrain(String terrain) {
        if (compatibleTerrain.isEmpty()) {
            return true;
        }
        return compatibleTerrain.contains(terrain.toUpperCase());
    }

    // ---- Ownership and State ----

    /** @return the owner empire ID, or null if independent */
    public String getOwnerEmpireId() {
        return ownerEmpireId;
    }

    /**
     * Sets the owner empire.
     *
     * @param empireId the empire ID
     */
    public void setOwnerEmpireId(String empireId) {
        this.ownerEmpireId = empireId;
    }

    /** @return the stationed planet */
    public Planet getStationedPlanet() {
        return stationedPlanet;
    }

    /**
     * Sets the stationed planet.
     *
     * @param planet the planet
     */
    public void setStationedPlanet(Planet planet) {
        this.stationedPlanet = planet;
    }

    /** @return true if the vehicle is active */
    public boolean isActive() {
        return active;
    }

    /**
     * Activates or deactivates this vehicle.
     *
     * @param active the active state
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    // ---- Interior Dungeon Integration ----

    /** @return the interior dungeon ID, or null if not generated */
    public String getInteriorDungeonId() {
        return interiorDungeonId;
    }

    /**
     * Sets the interior dungeon ID.
     *
     * @param dungeonId the dungeon ID
     */
    public void setInteriorDungeonId(String dungeonId) {
        this.interiorDungeonId = dungeonId;
    }

    // ---- AI Core integration ----

    /**
     * Returns the AI Core component installed on this vehicle, or null if none.
     *
     * @return the AI Core component, or null
     */
    public AiCoreComponent getAiCore() {
        return aiCore;
    }

    /**
     * Installs an AI Core component on this vehicle.
     * <p>
     * If an AI Core is already installed, it will be replaced.
     * </p>
     *
     * @param aiCore the AI Core to install
     */
    public void setAiCore(AiCoreComponent aiCore) {
        this.aiCore = aiCore;
    }

    /**
     * Removes the AI Core component from this vehicle.
     */
    public void removeAiCore() {
        this.aiCore = null;
    }

    /**
     * Returns true if this vehicle has an AI Core installed.
     *
     * @return true if an AI Core is installed
     */
    public boolean hasAiCore() {
        return aiCore != null;
    }

    /**
     * Returns the AI Core level (1-3), or 0 if no AI Core is installed.
     *
     * @return the AI Core level (1-based: BASIC=1, ADVANCED=2, ELITE=3)
     */
    public int getAiCoreLevel() {
        return aiCore != null ? aiCore.getTier().ordinal() + 1 : 0;
    }

    // ---- Damage Operations ----

    /**
     * Damages a specific cell in the hull.
     * <p>
     * Finds the cell in the first hull module and applies damage. If the cell belongs
     * to a compartment, the compartment's secondary effects are triggered.
     * </p>
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @param damageAmount the damage amount
     * @return the compartment damaged, or null if no compartment was hit
     */
    public Compartment damageHullCell(int x, int y, int z, int damageAmount) {
        if (hullModules.isEmpty()) {
            return null;
        }
        HullModule module = hullModules.get(0);
        M3space cell = module.getCell(x, y, z);
        if (cell == null) {
            return null;
        }
        // Reduce armor
        currentArmor = Math.max(0, currentArmor - damageAmount);

        // Damage the cell directly
        cell.damage(damageAmount);

        // Find the compartment containing this cell and trigger breach damage
        for (Compartment compartment : module.getCompartments()) {
            if (compartment.getCells().contains(cell)) {
                compartment.damageFromBreach(1);
                return compartment;
            }
        }
        return null;
    }

    /**
     * Checks if a compartment is destroyed.
     *
     * @param compartmentName the compartment name
     * @return true if the compartment is destroyed
     */
    public boolean isCompartmentDestroyed(String compartmentName) {
        Compartment compartment = compartments.get(compartmentName);
        return compartment != null && compartment.isDestroyed();
    }

    /**
     * Checks if the vehicle is destroyed (armor depleted).
     *
     * @return true if the vehicle is destroyed
     */
    public boolean isDestroyed() {
        return currentArmor <= 0;
    }

    /**
     * Repairs a compartment by name.
     *
     * @param compartmentName the compartment name
     * @return the number of cells repaired
     */
    public int repairCompartment(String compartmentName) {
        Compartment compartment = compartments.get(compartmentName);
        if (compartment != null) {
            return compartment.repair();
        }
        return 0;
    }

    /**
     * Refuels the vehicle.
     *
     * @param amount the amount of fuel to add
     * @return the actual amount added
     */
    public double refuel(double amount) {
        if (amount <= 0) {
            return 0;
        }
        double added = Math.min(amount, maxFuel - fuelLevel);
        fuelLevel += added;
        return added;
    }

    /**
     * Consumes fuel for a turn.
     *
     * @return true if the vehicle has enough fuel to move
     */
    public boolean consumeFuel() {
        if (fuelLevel < fuelConsumption) {
            return false;
        }
        fuelLevel -= fuelConsumption;
        return true;
    }

    /**
     * Moves the vehicle to a new position.
     *
     * @param newPosition the new hex position
     * @return true if the move was successful
     */
    public boolean move(Hex3D newPosition) {
        if (!active || isDestroyed()) {
            return false;
        }
        if (!consumeFuel()) {
            return false;
        }
        currentPosition = newPosition;
        return true;
    }

    /**
     * Processes a turn: consumes fuel and supply.
     */
    public void processTurn() {
        if (!active || isDestroyed()) return;
        consumeFuel();
    }

    // ---- Skin support ----

    /**
     * Returns the skin currently applied to this vehicle, or null if none.
     *
     * @return the skin, or null
     */
    public Skin getSkin() {
        return skin;
    }

    /**
     * Applies a skin to this vehicle.
     *
     * @param skin the skin to apply
     */
    public void setSkin(Skin skin) {
        this.skin = skin;
        if (skin != null) {
            for (Decal decal : skin.getDefaultDecals()) {
                this.decals.add(new Decal(decal.getId() + "_" + this.id, decal.getType()));
                Decal d = this.decals.get(this.decals.size() - 1);
                d.setContent(decal.getContent());
                d.setPosition(decal.getPosition());
                d.setSize(decal.getSize());
                d.setRotation(decal.getRotation());
                d.setOpacity(decal.getOpacity());
                d.setColor(decal.getColor());
            }
        }
    }

    /**
     * Removes the skin from this vehicle (does not remove decals).
     */
    public void removeSkin() {
        this.skin = null;
    }

    /**
     * Returns the list of decals on this vehicle.
     *
     * @return unmodifiable list of decals
     */
    public List<Decal> getDecals() {
        return Collections.unmodifiableList(decals);
    }

    /**
     * Adds a decal to this vehicle.
     *
     * @param decal the decal to add
     */
    public void addDecal(Decal decal) {
        if (decal == null) {
            throw new IllegalArgumentException("Decal must not be null");
        }
        decals.add(decal);
    }

    /**
     * Removes a decal from this vehicle.
     *
     * @param decal the decal to remove
     * @return true if the decal was removed
     */
    public boolean removeDecal(Decal decal) {
        return decals.remove(decal);
    }

    /**
     * Clears all decals from this vehicle.
     */
    public void clearDecals() {
        decals.clear();
    }

    @Override
    public String toString() {
        return String.format(
                "GroundVehicle{%s, type=%s, armor=%d/%d, firepower=%d, speed=%d, crew=%d/%d, fuel=%.1f/%.1f}",
                name, type, currentArmor, armorRating, firepower, speedKmh,
                currentCrew, crewCapacity, fuelLevel, maxFuel);
    }
}

package com.yisy.modules.doramcu.traveller;

import java.util.*;

/**
 * Converts Third Reich naval vessels to Traveller equivalents.
 * <p>
 * This service handles the conversion from historical naval vessels (from the 3R4 game)
 * to Traveller naval vessels that can be built on appropriate Tech Worlds. The conversion
 * maps historical vessel types to Traveller vessel types, assigns TL ratings, calculates
 * costs, and determines required skills.
 * </p>
 * <p>
 * Conversion rules:
 * </p>
 * <ul>
 *   <li>WWII-era vessels (1939-1945) are rated TL7-9</li>
 *   <li>Battleships → Traveller Battleship or Dreadnought</li>
 *   <li>Carriers → Traveller Carrier</li>
 *   <li>Cruisers → Traveller Cruiser</li>
 *   <li>Destroyers → Traveller Destroyer</li>
 *   <li>Submarines → Traveller Submarine</li>
 * </ul>
 * <p>
 * The service also provides a factory for creating Traveller naval vessels
 * based on TL and world type.
 * </p>
 */
public class NavalVesselConversionService {

    private final Map<String, NavalVesselConversion> conversionCache;

    /**
     * Creates a new conversion service.
     */
    public NavalVesselConversionService() {
        this.conversionCache = new HashMap<>();
        initializeDefaultConversions();
    }

    // ---- Core Conversion Methods ----

    /**
     * Converts a Third Reich naval vessel to a Traveller equivalent.
     * <p>
     * Creates a {@link NavalVesselConversion} mapping and a {@link TravellerNavalVessel}
     * based on the historical vessel's type and stats.
     * </p>
     *
     * @param vessel the Third Reich vessel to convert
     * @return the Traveller naval vessel
     */
    public TravellerNavalVessel convert(ThirdReichNavalVessel vessel) {
        Objects.requireNonNull(vessel, "Vessel must not be null");

        // Check cache first
        NavalVesselConversion conversion = conversionCache.get(vessel.getId());
        if (conversion == null) {
            conversion = createConversion(vessel);
            conversionCache.put(vessel.getId(), conversion);
            vessel.setConversion(conversion);
        }

        return conversion.createVessel(vessel.getName());
    }

    /**
     * Returns the conversion mapping for a vessel, creating it if necessary.
     *
     * @param vessel the Third Reich vessel
     * @return the conversion mapping
     */
    public NavalVesselConversion getConversion(ThirdReichNavalVessel vessel) {
        Objects.requireNonNull(vessel, "Vessel must not be null");

        NavalVesselConversion conversion = conversionCache.get(vessel.getId());
        if (conversion == null) {
            conversion = createConversion(vessel);
            conversionCache.put(vessel.getId(), conversion);
            vessel.setConversion(conversion);
        }
        return conversion;
    }

    /**
     * Creates a conversion mapping for a Third Reich vessel.
     *
     * @param vessel the source vessel
     * @return the conversion mapping
     */
    private NavalVesselConversion createConversion(ThirdReichNavalVessel vessel) {
        NavalVesselConversion conversion = new NavalVesselConversion(
                "conv-" + vessel.getId(), vessel.getId());

        // Map vessel type
        TravellerNavalVessel.VesselType travellerType = mapVesselType(vessel.getType());
        conversion.setTravellerType(travellerType);
        conversion.setTravellerVesselName(travellerType.getDisplayName());

        // Determine TL based on year
        int tl = determineTechLevel(vessel.getYear(), vessel.getType());
        conversion.setTechLevel(tl);

        // Determine armor class from defense value
        int armorClass = Math.min(9, vessel.getDefenseValue() / 10);
        conversion.setArmorClass(armorClass);

        // Determine tonnage from type
        int tonnage = calculateTonnage(vessel, travellerType);
        conversion.setTonnage(tonnage);

        // Calculate cost
        long cost = calculateCost(travellerType, tonnage, armorClass, tl);
        conversion.setCost(cost);

        // Add required skills
        addRequiredSkills(conversion, travellerType, tl);

        // Set description
        conversion.setDescription(String.format(
                "Converted from %s (%s, %d). TL%d design with AC%d armor.",
                vessel.getName(), vessel.getNationality(), vessel.getYear(),
                tl, armorClass));

        return conversion;
    }

    // ---- Type Mapping ----

    /**
     * Maps a Third Reich vessel type to a Traveller vessel type.
     */
    private TravellerNavalVessel.VesselType mapVesselType(ThirdReichNavalVessel.HistoricalVesselType type) {
        return switch (type) {
            case BATTLESHIP -> TravellerNavalVessel.VesselType.BATTLESHIP;
            case CARRIER -> TravellerNavalVessel.VesselType.CARRIER;
            case HEAVY_CRUISER -> TravellerNavalVessel.VesselType.CRUISER;
            case LIGHT_CRUISER -> TravellerNavalVessel.VesselType.CRUISER;
            case DESTROYER -> TravellerNavalVessel.VesselType.DESTROYER;
            case SUBMARINE -> TravellerNavalVessel.VesselType.SUBMARINE;
            case PATROL_BOAT -> TravellerNavalVessel.VesselType.PATROL_BOAT;
            case TRANSPORT -> TravellerNavalVessel.VesselType.TENDER;
            case SUPPORT -> TravellerNavalVessel.VesselType.TENDER;
        };
    }

    // ---- Tech Level Determination ----

    /**
     * Determines the tech level based on the vessel's year and type.
     * <p>
     * WWII-era vessels (1939-1945) are TL7-9:
     * </p>
     * <ul>
     *   <li>Early war (1939-1941): TL7</li>
     *   <li>Mid war (1942-1943): TL8</li>
     *   <li>Late war (1944-1945): TL9</li>
     * </ul>
     */
    private int determineTechLevel(int year, ThirdReichNavalVessel.HistoricalVesselType type) {
        if (year < 1940) {
            return 7;
        } else if (year < 1942) {
            return 7;
        } else if (year < 1944) {
            return 8;
        } else {
            return 9;
        }
    }

    // ---- Tonnage Calculation ----

    /**
     * Calculates the tonnage based on the vessel's combat factors.
     */
    private int calculateTonnage(ThirdReichNavalVessel vessel, TravellerNavalVessel.VesselType type) {
        int baseTonnage = type.getMinTonnage();
        int maxTonnage = type.getMaxTonnage();

        // Scale tonnage based on combat strength
        int combatStrength = vessel.getCombatStrength();
        double scaleFactor = 1.0 + (combatStrength / 100.0);

        int tonnage = (int) (baseTonnage * scaleFactor);
        return Math.min(tonnage, maxTonnage);
    }

    // ---- Cost Calculation ----

    /**
     * Calculates the construction cost in credits.
     */
    private long calculateCost(TravellerNavalVessel.VesselType type, int tonnage,
                                int armorClass, int techLevel) {
        long baseCost = type.getBaseCost();
        long tonnageMultiplier = Math.max(1, tonnage / type.getMinTonnage());
        long armorMultiplier = 1 + (armorClass * 10L);
        long techMultiplier = 1 + ((techLevel - 7) * 5L);

        return baseCost * tonnageMultiplier * armorMultiplier * techMultiplier;
    }

    // ---- Required Skills ----

    /**
     * Adds required skills based on vessel type and TL.
     */
    private void addRequiredSkills(NavalVesselConversion conversion,
                                    TravellerNavalVessel.VesselType type, int tl) {
        // All vessels need these core skills
        conversion.addRequiredSkill("Shipbuilding");
        conversion.addRequiredSkill("Engineering");

        // Type-specific skills
        switch (type) {
            case BATTLESHIP, DREADNOUGHT, MOTHERSHIP -> {
                conversion.addRequiredSkill("Heavy Weapons");
                conversion.addRequiredSkill("Armor Systems");
            }
            case CARRIER -> {
                conversion.addRequiredSkill("Aircraft Operations");
                conversion.addRequiredSkill("Hangar Management");
            }
            case CRUISER, DESTROYER -> {
                conversion.addRequiredSkill("Naval Gunnery");
                conversion.addRequiredSkill("Missile Systems");
            }
            case SUBMARINE -> {
                conversion.addRequiredSkill("Subsurface Operations");
                conversion.addRequiredSkill("Stealth Systems");
            }
            case AMPHIBIOUS -> {
                conversion.addRequiredSkill("Amphibious Operations");
                conversion.addRequiredSkill("Troop Transport");
            }
            default -> {
                conversion.addRequiredSkill("Naval Operations");
            }
        }

        // High TL adds advanced skills
        if (tl >= 9) {
            conversion.addRequiredSkill("Advanced Materials");
        }
        if (tl >= 11) {
            conversion.addRequiredSkill("Energy Weapons");
        }
    }

    // ---- Factory Methods ----

    /**
     * Creates a Traveller naval vessel of the given type at the given TL.
     *
     * @param name the vessel name
     * @param type the vessel type
     * @param tl the tech level
     * @return a new Traveller naval vessel
     */
    public TravellerNavalVessel createVessel(String name, TravellerNavalVessel.VesselType type, int tl) {
        Objects.requireNonNull(name, "Name must not be null");
        Objects.requireNonNull(type, "Type must not be null");

        TravellerNavalVessel vessel = new TravellerNavalVessel(
                "vessel-" + System.currentTimeMillis(), name, type);
        vessel.setTechLevel(tl);

        // Add default weapons based on type
        addDefaultWeapons(vessel, type, tl);

        return vessel;
    }

    /**
     * Returns all vessel types that can be built at the given TL.
     *
     * @param tl the tech level
     * @return list of buildable vessel types
     */
    public List<TravellerNavalVessel.VesselType> getAvailableVesselTypes(int tl) {
        List<TravellerNavalVessel.VesselType> available = new ArrayList<>();

        for (TravellerNavalVessel.VesselType type : TravellerNavalVessel.VesselType.values()) {
            // Basic vessels available at TL7+
            // Advanced vessels require higher TL
            int requiredTl = getRequiredTechLevel(type);
            if (tl >= requiredTl) {
                available.add(type);
            }
        }

        return available;
    }

    /**
     * Checks if a vessel can be built on a world with the given TL.
     *
     * @param vessel the vessel to check
     * @param worldTl the world's tech level
     * @return true if the vessel can be built
     */
    public boolean canBuild(TravellerNavalVessel vessel, int worldTl) {
        return vessel.canBuildOnWorld(worldTl);
    }

    /**
     * Returns the minimum TL required for a vessel type.
     */
    private int getRequiredTechLevel(TravellerNavalVessel.VesselType type) {
        return switch (type) {
            case PATROL_BOAT, CORVETTE, DESTROYER, SUBMARINE -> 7;
            case CRUISER, CARRIER, TENDER, AMPHIBIOUS -> 8;
            case BATTLESHIP -> 9;
            case DREADNOUGHT -> 11;
            case MOTHERSHIP -> 13;
        };
    }

    /**
     * Adds default weapons to a vessel based on its type and TL.
     */
    private void addDefaultWeapons(TravellerNavalVessel vessel,
                                    TravellerNavalVessel.VesselType type, int tl) {
        switch (type) {
            case PATROL_BOAT -> {
                addWeapon(vessel, "laser-1", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.SMALL_LASER, 1, "A1"));
            }
            case CORVETTE -> {
                addWeapon(vessel, "laser-1", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.SMALL_LASER, 2, "A1"));
                addWeapon(vessel, "laser-2", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.SMALL_LASER, 2, "A2"));
            }
            case DESTROYER -> {
                addWeapon(vessel, "laser-1", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.MEDIUM_LASER, 2, "A1"));
                addWeapon(vessel, "laser-2", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.MEDIUM_LASER, 2, "A2"));
                if (tl >= 8) {
                    addWeapon(vessel, "missile-1", NavalWeaponMount.MountType.MISSILE_SILE,
                            new ShipWeaponSystem(ShipWeaponSystem.MissileType.STANDARD, 1, 6, "B1"));
                }
            }
            case CRUISER -> {
                addWeapon(vessel, "laser-1", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.LARGE_LASER, 2, "A1"));
                addWeapon(vessel, "laser-2", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.LARGE_LASER, 2, "A2"));
                addWeapon(vessel, "laser-3", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.MEDIUM_LASER, 2, "B1"));
                addWeapon(vessel, "missile-1", NavalWeaponMount.MountType.MISSILE_SILE,
                        new ShipWeaponSystem(ShipWeaponSystem.MissileType.STANDARD, 1, 6, "B2"));
            }
            case BATTLESHIP -> {
                addWeapon(vessel, "laser-1", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.HUGE_LASER, 2, "A1"));
                addWeapon(vessel, "laser-2", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.HUGE_LASER, 2, "A2"));
                addWeapon(vessel, "laser-3", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.LARGE_LASER, 2, "B1"));
                addWeapon(vessel, "laser-4", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.LARGE_LASER, 2, "B2"));
                addWeapon(vessel, "missile-1", NavalWeaponMount.MountType.MISSILE_SILE,
                        new ShipWeaponSystem(ShipWeaponSystem.MissileType.STANDARD, 1, 6, "C1"));
                addWeapon(vessel, "missile-2", NavalWeaponMount.MountType.MISSILE_SILE,
                        new ShipWeaponSystem(ShipWeaponSystem.MissileType.STANDARD, 1, 6, "C2"));
            }
            case CARRIER -> {
                // Carriers have light defense weapons
                addWeapon(vessel, "laser-1", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.MEDIUM_LASER, 2, "A1"));
                addWeapon(vessel, "laser-2", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.MEDIUM_LASER, 2, "A2"));
            }
            case SUBMARINE -> {
                addWeapon(vessel, "missile-1", NavalWeaponMount.MountType.MISSILE_SILE,
                        new ShipWeaponSystem(ShipWeaponSystem.MissileType.STANDARD, 1, 6, "A1"));
                addWeapon(vessel, "missile-2", NavalWeaponMount.MountType.MISSILE_SILE,
                        new ShipWeaponSystem(ShipWeaponSystem.MissileType.STANDARD, 1, 6, "A2"));
            }
            default -> {
                addWeapon(vessel, "laser-1", NavalWeaponMount.MountType.TURRET,
                        new ShipWeaponSystem(ShipWeaponSystem.WeaponType.SMALL_LASER, 2, "A1"));
            }
        }
    }

    /**
     * Helper to add a weapon mount to a vessel.
     */
    private void addWeapon(TravellerNavalVessel vessel, String mountId,
                            NavalWeaponMount.MountType mountType,
                            ShipWeaponSystem weapon) {
        NavalWeaponMount mount = new NavalWeaponMount(mountId, mountType, weapon);
        vessel.addWeapon(mount);
    }

    // ---- Default Conversions ----

    /**
     * Initializes default conversion mappings for common historical vessels.
     */
    private void initializeDefaultConversions() {
        // Bismarck-class battleship
        NavalVesselConversion bismarck = new NavalVesselConversion("conv-bismarck", "bismarck");
        bismarck.setTravellerType(TravellerNavalVessel.VesselType.BATTLESHIP);
        bismarck.setTravellerVesselName("Bismarck-class Battleship");
        bismarck.setTechLevel(8);
        bismarck.setArmorClass(6);
        bismarck.setTonnage(42000);
        bismarck.setCost(21000000);
        bismarck.addRequiredSkill("Shipbuilding");
        bismarck.addRequiredSkill("Heavy Weapons");
        bismarck.setDescription("German battleship, 1940. Heavy armor and main battery.");
        conversionCache.put("bismarck", bismarck);

        // Yorktown-class carrier
        NavalVesselConversion yorktown = new NavalVesselConversion("conv-yorktown", "yorktown");
        yorktown.setTravellerType(TravellerNavalVessel.VesselType.CARRIER);
        yorktown.setTravellerVesselName("Yorktown-class Carrier");
        yorktown.setTechLevel(8);
        yorktown.setArmorClass(4);
        yorktown.setTonnage(20000);
        yorktown.setCost(16000000);
        yorktown.addRequiredSkill("Shipbuilding");
        yorktown.addRequiredSkill("Aircraft Operations");
        yorktown.setDescription("US aircraft carrier, 1942. Medium armor, aircraft operations.");
        conversionCache.put("yorktown", yorktown);

        // Type VII U-boat
        NavalVesselConversion uboat = new NavalVesselConversion("conv-u47", "u47");
        uboat.setTravellerType(TravellerNavalVessel.VesselType.SUBMARINE);
        uboat.setTravellerVesselName("Type VII Submarine");
        uboat.setTechLevel(7);
        uboat.setArmorClass(3);
        uboat.setTonnage(750);
        uboat.setCost(750000);
        uboat.addRequiredSkill("Shipbuilding");
        uboat.addRequiredSkill("Subsurface Operations");
        uboat.setDescription("German Type VII U-boat, 1940. Subsurface attack specialist.");
        conversionCache.put("u47", uboat);
    }
}

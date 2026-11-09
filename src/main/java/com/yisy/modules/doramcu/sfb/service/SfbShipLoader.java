package com.yisy.modules.doramcu.sfb.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.sfb.model.*;

import java.io.InputStream;
import java.util.*;

/**
 * Loads ship definitions from JSON files (ship-classes.json, empires.json)
 * and creates fully configured SfbShip objects.
 * <p>
 * Reads ship class data (name, designation, size class, weapons, special systems)
 * and maps it to SfbShip with SSD, shields, and weapon banks.
 * </p>
 */
public class SfbShipLoader {

    private final ObjectMapper objectMapper;
    private Map<String, JsonNode> shipClassesByEmpire;
    private Map<String, JsonNode> empires;

    public SfbShipLoader() {
        this.objectMapper = new ObjectMapper();
        loadShipClasses();
        loadEmpires();
    }

    /**
     * Constructor with custom ObjectMapper (for testing).
     */
    public SfbShipLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadShipClasses();
        loadEmpires();
    }

    /**
     * Load ship class definitions from ship-classes.json.
     */
    private void loadShipClasses() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sfb-rules-db/ship-classes.json")) {
            if (is == null) {
                shipClassesByEmpire = new HashMap<>();
                return;
            }
            JsonNode root = objectMapper.readTree(is);
            JsonNode classes = root.get("shipClasses");
            if (classes != null) {
                shipClassesByEmpire = new HashMap<>();
                classes.fields().forEachRemaining(entry -> {
                    if (!"description".equals(entry.getKey())) {
                        shipClassesByEmpire.put(entry.getKey(), entry.getValue());
                    }
                });
            }
        } catch (Exception e) {
            shipClassesByEmpire = new HashMap<>();
        }
    }

    /**
     * Load empire definitions from empires.json.
     */
    private void loadEmpires() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("sfb-rules-db/empires.json")) {
            if (is == null) {
                empires = new HashMap<>();
                return;
            }
            JsonNode root = objectMapper.readTree(is);
            JsonNode empiresNode = root.get("empires");
            if (empiresNode != null) {
                empires = new HashMap<>();
                // Empires are grouped by galaxy (alphaOctant, andromeda, etc.)
                empiresNode.fields().forEachRemaining(galaxyEntry -> {
                    if (!"description".equals(galaxyEntry.getKey())) {
                        JsonNode galaxyArray = galaxyEntry.getValue();
                        if (galaxyArray.isArray()) {
                            for (JsonNode node : galaxyArray) {
                                String name = node.get("name").asText();
                                empires.put(name.toLowerCase(), node);
                                // Also store by abbreviation if available
                                JsonNode abbr = node.get("abbreviation");
                                if (abbr != null) {
                                    empires.put(abbr.asText().toLowerCase(), node);
                                }
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            empires = new HashMap<>();
        }
    }

    /**
     * Get all available empires with ship classes.
     */
    public Set<String> getAvailableEmpires() {
        return shipClassesByEmpire.keySet();
    }

    /**
     * Get all ship classes for a given empire.
     *
     * @param empireName Empire name (e.g., "federation", "klingon", "romulan")
     * @return List of ship class names
     */
    public List<String> getShipClasses(String empireName) {
        JsonNode classes = shipClassesByEmpire.get(empireName.toLowerCase());
        if (classes == null || !classes.isArray()) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (JsonNode node : classes) {
            result.add(node.get("name").asText());
        }
        return result;
    }

    /**
     * Create a ship from a ship class definition.
     *
     * @param empireName Empire name (e.g., "federation")
     * @param shipClassName Ship class name (e.g., "Constitution-class")
     * @return Configured SfbShip instance
     */
    public SfbShip createShip(String empireName, String shipClassName) {
        return createShip(empireName, shipClassName, null, 0);
    }

    /**
     * Create a ship from a ship class definition with custom ID and side.
     *
     * @param empireName Empire name
     * @param shipClassName Ship class name
     * @param shipId Custom ship ID (null for auto-generated)
     * @param side Battle side (0 or 1)
     * @return Configured SfbShip instance
     */
    public SfbShip createShip(String empireName, String shipClassName, String shipId, int side) {
        JsonNode classes = shipClassesByEmpire.get(empireName.toLowerCase());
        if (classes == null || !classes.isArray()) {
            throw new IllegalArgumentException("Unknown empire: " + empireName);
        }

        JsonNode shipClass = null;
        for (JsonNode node : classes) {
            if (node.get("name").asText().equalsIgnoreCase(shipClassName)) {
                shipClass = node;
                break;
            }
        }

        if (shipClass == null) {
            throw new IllegalArgumentException("Unknown ship class: " + shipClassName + " for empire: " + empireName);
        }

        SfbShip ship = new SfbShip();
        ship.setId(shipId != null ? shipId : UUID.randomUUID().toString().substring(0, 8));
        ship.setName(shipClass.get("name").asText());
        ship.setShipClass(shipClass.get("designation").asText());
        ship.setSide(side);

        // Set empire
        SfbEmpire empire = createEmpire(empireName);
        ship.setEmpire(empire);

        // Configure based on size class
        String sizeClass = shipClass.get("sizeClass").asText("SC2");
        configureShipBySize(ship, sizeClass);

        // Configure weapons from JSON
        configureWeapons(ship, shipClass);

        // Configure special systems
        configureSpecialSystems(ship, shipClass);

        return ship;
    }

    /**
     * Configure ship SSD and shields based on size class.
     */
    private void configureShipBySize(SfbShip ship, String sizeClass) {
        SfbSSD ssd = new SfbSSD();

        switch (sizeClass) {
            case "SC1": // Superheavy (B10 Battleship)
                ssd.setHullBoxes(20);
                ssd.setDamConRating(5);
                ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("WARP_3", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("WARP_4", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("IMPA_1", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ssd.addSystem("IMPA_2", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ssd.addSystem("IMPA_3", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ssd.addSystem("IMPA_4", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ship.initializeShields(10);
                ship.setSpeed(8);
                break;

            case "SC2": // Heavy (CA, CL heavy)
                ssd.setHullBoxes(12);
                ssd.setDamConRating(4);
                ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("WARP_3", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("IMPA_1", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ssd.addSystem("IMPA_2", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ssd.addSystem("IMPA_3", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ship.initializeShields(7);
                ship.setSpeed(16);
                break;

            case "SC3": // Light (CL, DD)
                ssd.setHullBoxes(8);
                ssd.setDamConRating(3);
                ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("IMPA_1", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ssd.addSystem("IMPA_2", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ship.initializeShields(5);
                ship.setSpeed(24);
                break;

            case "SC4": // Frigate (FT, PF)
                ssd.setHullBoxes(5);
                ssd.setDamConRating(2);
                ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("IMPA_1", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ship.initializeShields(3);
                ship.setSpeed(32);
                break;

            default:
                ssd.setHullBoxes(10);
                ssd.setDamConRating(3);
                ssd.addSystem("WARP_1", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("WARP_2", SfbSSD.SystemCategory.WARP_ENGINES);
                ssd.addSystem("IMPA_1", SfbSSD.SystemCategory.IMPULSE_ENGINES);
                ship.initializeShields(5);
                ship.setSpeed(16);
                break;
        }

        ship.setSsd(ssd);
    }

    /**
     * Configure weapon banks from ship class JSON.
     */
    private void configureWeapons(SfbShip ship, JsonNode shipClass) {
        JsonNode weapons = shipClass.get("weapons");
        if (weapons == null || !weapons.isArray()) {
            return;
        }

        for (JsonNode weapon : weapons) {
            String weaponName = weapon.asText().toLowerCase();
            SfbWeaponBank bank = createWeaponBank(weaponName);
            if (bank != null) {
                ship.addWeaponBank(bank);
            }
        }
    }

    /**
     * Create a weapon bank from a weapon name string.
     */
    private SfbWeaponBank createWeaponBank(String weaponName) {
        if (weaponName.contains("phaser")) {
            SfbWeaponBank bank = new SfbWeaponBank("phaser", SfbWeaponBank.WeaponType.PHASER_2, 
                    SfbWeaponBank.FiringArc.A, 2);
            return bank;
        } else if (weaponName.contains("disruptor")) {
            SfbWeaponBank bank = new SfbWeaponBank("disruptor", SfbWeaponBank.WeaponType.DISRUPTOR, 
                    SfbWeaponBank.FiringArc.A, 2);
            return bank;
        } else if (weaponName.contains("polaron")) {
            SfbWeaponBank bank = new SfbWeaponBank("polaron", SfbWeaponBank.WeaponType.POLARON, 
                    SfbWeaponBank.FiringArc.A, 2);
            return bank;
        } else if (weaponName.contains("torpedo") && !weaponName.contains("plasma")) {
            SfbWeaponBank bank = new SfbWeaponBank("torpedo", SfbWeaponBank.WeaponType.PHOTON_TORPEDO, 
                    SfbWeaponBank.FiringArc.F, 1);
            bank.setAmmo(10);
            return bank;
        } else if (weaponName.contains("plasma")) {
            SfbWeaponBank bank = new SfbWeaponBank("plasma", SfbWeaponBank.WeaponType.PLASMA_TORPEDO, 
                    SfbWeaponBank.FiringArc.F, 2);
            bank.setAmmo(6);
            return bank;
        } else if (weaponName.contains("drone")) {
            SfbWeaponBank bank = new SfbWeaponBank("drone", SfbWeaponBank.WeaponType.DRONE, 
                    SfbWeaponBank.FiringArc.F, 1);
            bank.setAmmo(8);
            return bank;
        } else if (weaponName.contains("laser")) {
            SfbWeaponBank bank = new SfbWeaponBank("laser", SfbWeaponBank.WeaponType.LASER, 
                    SfbWeaponBank.FiringArc.A, 1);
            return bank;
        }
        return null;
    }

    /**
     * Configure special systems from ship class JSON.
     */
    private void configureSpecialSystems(SfbShip ship, JsonNode shipClass) {
        JsonNode systems = shipClass.get("specialSystems");
        if (systems == null || !systems.isArray()) {
            return;
        }

        for (JsonNode system : systems) {
            String systemName = system.asText().toLowerCase();
            if (systemName.contains("cloaking")) {
                // Cloaking is handled by the cloaked flag
                ship.setCloaked(false);
            }
            // Other special systems are tracked in the SSD or handled by services
        }
    }

    /**
     * Create an SfbEmpire from empire name.
     */
    private SfbEmpire createEmpire(String empireName) {
        JsonNode empireData = empires.get(empireName.toLowerCase());
        SfbEmpire empire = new SfbEmpire();
        empire.setName(empireName);

        if (empireData != null) {
            empire.setName(empireData.get("name").asText(empireName));
            JsonNode desc = empireData.get("description");
            if (desc != null) {
                empire.setDescription(desc.asText());
            }
        }

        return empire;
    }

    /**
     * Get the ObjectMapper instance (for testing).
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}

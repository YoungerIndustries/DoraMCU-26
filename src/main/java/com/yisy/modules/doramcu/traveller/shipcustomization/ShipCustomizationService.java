package com.yisy.modules.doramcu.traveller.shipcustomization;
import com.yisy.modules.doramcu.traveller.HighGuardShipDesign;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import java.util.*;

/**
 * Service for managing ship customization in the Traveller module.
 */
@Service
public class ShipCustomizationService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ShipCustomizationService.class);

    private final ShipComponentRepository componentRepository;
    private final CustomShipDesignRepository designRepository;

    public ShipCustomizationService(ShipComponentRepository componentRepository,
                                     CustomShipDesignRepository designRepository) {
        this.componentRepository = componentRepository;
        this.designRepository = designRepository;
    }

    // ---- Component Management ----

    public List<ShipComponent> getAllComponents() {
        return componentRepository.findAll();
    }

    public List<ShipComponent> getComponentsByType(ComponentType type) {
        return componentRepository.findByType(type);
    }

    public List<ShipComponent> getComponentsByTypeAndTech(ComponentType type, int techLevel) {
        return componentRepository.findByTypeAndMinTechLevelLessThanEqual(type, techLevel);
    }

    public Optional<ShipComponent> getComponent(Long id) {
        return componentRepository.findById(id);
    }

    // ---- Design Management ----

    public CustomShipDesign createDesign(String name, String ownerId, int totalTonnage, int techLevel) {
        CustomShipDesign design = new CustomShipDesign(name, ownerId, totalTonnage, techLevel);
        design.setDescription("Custom ship design: " + name);
        design.setConstructionCost((long) totalTonnage * 100);
        return designRepository.save(design);
    }

    public Optional<CustomShipDesign> getDesign(Long id) {
        return designRepository.findById(id);
    }

    public List<CustomShipDesign> getDesignsByOwner(String ownerId) {
        return designRepository.findByOwnerId(ownerId);
    }

    public CustomShipDesign updateDesign(Long id, String name, String description) {
        CustomShipDesign design = designRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Design not found: " + id));
        if (name != null) design.setName(name);
        if (description != null) design.setDescription(description);
        return designRepository.save(design);
    }

    public void deleteDesign(Long id) {
        if (!designRepository.existsById(id)) {
            throw new IllegalArgumentException("Design not found: " + id);
        }
        designRepository.deleteById(id);
    }

    public CustomShipDesign addComponent(Long designId, String slotName, Long componentId) {
        CustomShipDesign design = designRepository.findById(designId)
                .orElseThrow(() -> new IllegalArgumentException("Design not found: " + designId));
        ShipComponent component = componentRepository.findById(componentId)
                .orElseThrow(() -> new IllegalArgumentException("Component not found: " + componentId));
        if (component.getMinTechLevel() > design.getTechLevel()) {
            throw new IllegalArgumentException(
                    "Component requires TL" + component.getMinTechLevel() + " but design is TL" + design.getTechLevel());
        }
        design.addComponent(slotName, componentId);
        design.setValidated(false);
        return designRepository.save(design);
    }

    public CustomShipDesign removeComponent(Long designId, String slotName) {
        CustomShipDesign design = designRepository.findById(designId)
                .orElseThrow(() -> new IllegalArgumentException("Design not found: " + designId));
        design.removeComponent(slotName);
        design.setValidated(false);
        return designRepository.save(design);
    }

    public CustomShipDesign validateDesign(Long designId) {
        CustomShipDesign design = designRepository.findById(designId)
                .orElseThrow(() -> new IllegalArgumentException("Design not found: " + designId));

        int totalComponentTonnage = 0;
        long totalCost = 0;
        int maneuverDrive = 0;
        int powerPlant = 0;
        int armorFactor = 0;
        int cargoCap = 0;
        int crewCap = 0;
        int marineCap = 0;

        for (Map.Entry<String, Long> entry : design.getComponentSlots().entrySet()) {
            ShipComponent component = componentRepository.findById(entry.getValue())
                    .orElseThrow(() -> new IllegalArgumentException("Component not found: " + entry.getValue()));
            totalComponentTonnage += component.getTonnageCost();
            totalCost += component.getCost();

            switch (component.getType()) {
                case MANEUVER_DRIVE: maneuverDrive = Math.max(maneuverDrive, component.getDriveRating()); break;
                case POWER_PLANT: powerPlant = Math.max(powerPlant, component.getPowerRating()); break;
                case ARMOR: armorFactor = Math.max(armorFactor, component.getArmorFactor()); break;
                case CARGO: cargoCap += component.getCargoCapacity(); break;
                case CREW: crewCap += component.getCrewCapacity(); break;
                case MARINES: marineCap += component.getMarineCapacity(); break;
                default: break;
            }
        }

        if (totalComponentTonnage > design.getTotalTonnage()) {
            throw new IllegalArgumentException(
                    "Components exceed available tonnage: " + totalComponentTonnage + " > " + design.getTotalTonnage());
        }

        int hullRate = Math.max(1, (int) Math.log10(design.getTotalTonnage() / 100.0));
        int hullPoints = hullRate * (10 + armorFactor);
        int energyPoints = powerPlant * 10;
        int agility = powerPlant > 0 ? (energyPoints * 10) / (design.getTotalTonnage() / 100) : 0;

        design.setHullPoints(hullPoints);
        design.setThrust(maneuverDrive);
        design.setAgility(agility);
        design.setEnergyPoints(energyPoints);
        design.setCargoCapacity(cargoCap);
        design.setCrew(crewCap);
        design.setMarines(marineCap);
        design.setConstructionCost((long) design.getTotalTonnage() * 100 + totalCost);
        design.setValidated(true);

        log.info("Validated design {}: {} HP, {} thrust, {} agility", designId, hullPoints, maneuverDrive, agility);
        return designRepository.save(design);
    }

    public com.yisy.modules.doramcu.traveller.HighGuardShipDesign toHighGuardDesign(CustomShipDesign design) {
        if (!design.isValidated()) {
            throw new IllegalStateException("Design must be validated first: " + design.getName());
        }

        com.yisy.modules.doramcu.traveller.HighGuardShipDesign hgDesign =
                new com.yisy.modules.doramcu.traveller.HighGuardShipDesign(
                        design.getId().intValue(), design.getName(), design.getTotalTonnage());

        hgDesign.setTechLevel(design.getTechLevel());
        hgDesign.setEnergyPoints(design.getEnergyPoints());
        hgDesign.setCrew(design.getCrew());
        hgDesign.setMarines(design.getMarines());
        hgDesign.setConstructionCost(design.getConstructionCost());

        for (Map.Entry<String, Long> entry : design.getComponentSlots().entrySet()) {
            ShipComponent component = componentRepository.findById(entry.getValue()).orElse(null);
            if (component == null) continue;

            com.yisy.modules.doramcu.traveller.HighGuardWeapon weapon = mapToHighGuardWeapon(component);
            if (weapon == null) continue;

            if (component.getType() == ComponentType.TURRET_WEAPON) {
                hgDesign.addTurretWeapon(weapon, com.yisy.modules.doramcu.traveller.HighGuardWeapon.TurretSize.SINGLE);
            } else if (component.getType() == ComponentType.BAY_WEAPON) {
                hgDesign.addBayWeapon(weapon, com.yisy.modules.doramcu.traveller.HighGuardWeapon.BaySize.SMALL);
            }
        }

        return hgDesign;
    }

    // ---- Default Component Initialization ----

    @Override
    public void run(String... args) {
        if (componentRepository.count() > 0) {
            log.info("Ship components already initialized with {} components", componentRepository.count());
            return;
        }

        log.info("Initializing default ship components...");

        // Turret Weapons
        ShipComponent c;
        c = sc("Pulse Laser Single", ComponentType.TURRET_WEAPON, "Standard pulse laser, optimal at short range", 10, 50000, 6);
        c.setDamageDice(2); c.setOptimalRange(2); componentRepository.save(c);

        c = sc("Pulse Laser Double", ComponentType.TURRET_WEAPON, "Double pulse laser turret", 20, 100000, 6);
        c.setDamageDice(2); c.setOptimalRange(2); componentRepository.save(c);

        c = sc("Pulse Laser Triple", ComponentType.TURRET_WEAPON, "Triple pulse laser turret", 30, 150000, 6);
        c.setDamageDice(2); c.setOptimalRange(2); componentRepository.save(c);

        c = sc("Beam Laser Single", ComponentType.TURRET_WEAPON, "Beam laser, optimal at medium range", 10, 60000, 6);
        c.setDamageDice(2); c.setOptimalRange(3); componentRepository.save(c);

        c = sc("Beam Laser Double", ComponentType.TURRET_WEAPON, "Double beam laser turret", 20, 120000, 6);
        c.setDamageDice(2); c.setOptimalRange(3); componentRepository.save(c);

        c = sc("Particle Beam", ComponentType.TURRET_WEAPON, "Advanced particle beam, optimal at long range", 20, 200000, 11);
        c.setDamageDice(2); c.setOptimalRange(4); componentRepository.save(c);

        c = sc("Fusion Gun", ComponentType.TURRET_WEAPON, "Fusion pellet weapon, high armor penetration", 30, 300000, 12);
        c.setDamageDice(2); c.setOptimalRange(3); componentRepository.save(c);

        c = sc("Meson Gun", ComponentType.TURRET_WEAPON, "Meson beam, penetrates screens at long range", 40, 500000, 12);
        c.setDamageDice(2); c.setOptimalRange(4); componentRepository.save(c);

        c = sc("Sandcaster", ComponentType.TURRET_WEAPON, "Abrasive sand, blinds sensors at close range", 15, 100000, 8);
        c.setDamageDice(2); c.setOptimalRange(1); componentRepository.save(c);

        // Bay Weapons
        c = sc("Missile Bay Single", ComponentType.BAY_WEAPON, "Single missile launcher", 20, 100000, 6);
        componentRepository.save(c);

        c = sc("Missile Bay Double", ComponentType.BAY_WEAPON, "Double missile launcher", 40, 200000, 6);
        componentRepository.save(c);

        c = sc("Heavy Torpedo Bay", ComponentType.BAY_WEAPON, "Heavy torpedo launcher for capital ships", 100, 500000, 8);
        componentRepository.save(c);

        // Armor
        c = sc("Titanium Steel Armor", ComponentType.ARMOR, "Standard armor plating", 50, 100000, 6);
        c.setArmorFactor(2); componentRepository.save(c);

        c = sc("Crystaliron Armor", ComponentType.ARMOR, "Advanced armor, TL10+", 50, 300000, 10);
        c.setArmorFactor(4); componentRepository.save(c);

        c = sc("Bonded Superdense", ComponentType.ARMOR, "Best armor, TL12+", 50, 500000, 12);
        c.setArmorFactor(6); componentRepository.save(c);

        // Maneuver Drives
        for (int g = 1; g <= 6; g++) {
            int tl = g <= 3 ? 6 : g <= 4 ? 8 : g <= 5 ? 10 : 12;
            c = sc(g + "G Maneuver Drive", ComponentType.MANEUVER_DRIVE, g + "G thrust fusion drive",
                    g * 50, g * 200000, tl);
            c.setDriveRating(g); componentRepository.save(c);
        }

        // Jump Drives
        for (int j = 1; j <= 6; j++) {
            int tl = j <= 2 ? 6 : j <= 3 ? 8 : j <= 4 ? 10 : j <= 5 ? 12 : 14;
            c = sc("Jump-" + j + " Drive", ComponentType.JUMP_DRIVE, "Jump-" + j + " interstellar drive",
                    j * 100, j * 500000, tl);
            c.setDriveRating(j); componentRepository.save(c);
        }

        // Power Plants
        c = sc("Small Power Plant", ComponentType.POWER_PLANT, "Small power plant, rating 1", 20, 100000, 6);
        c.setPowerRating(1); componentRepository.save(c);

        c = sc("Medium Power Plant", ComponentType.POWER_PLANT, "Medium power plant, rating 2", 40, 200000, 6);
        c.setPowerRating(2); componentRepository.save(c);

        c = sc("Large Power Plant", ComponentType.POWER_PLANT, "Large power plant, rating 3", 60, 300000, 8);
        c.setPowerRating(3); componentRepository.save(c);

        // Computers
        c = sc("Computer Model 0", ComponentType.COMPUTER, "Basic computer", 5, 10000, 6);
        c.setComputerModel(0); componentRepository.save(c);

        c = sc("Computer Model 2", ComponentType.COMPUTER, "Standard computer", 10, 50000, 6);
        c.setComputerModel(2); componentRepository.save(c);

        c = sc("Computer Model 4", ComponentType.COMPUTER, "Advanced computer", 20, 100000, 8);
        c.setComputerModel(4); componentRepository.save(c);

        c = sc("Computer Model 6", ComponentType.COMPUTER, "Super computer", 40, 200000, 10);
        c.setComputerModel(6); componentRepository.save(c);

        // Sensors
        c = sc("Basic Sensors", ComponentType.SENSORS, "Standard sensors, rating 1", 5, 20000, 6);
        c.setSensorRating(1); componentRepository.save(c);

        c = sc("Advanced Sensors", ComponentType.SENSORS, "Advanced sensors, rating 2", 10, 50000, 8);
        c.setSensorRating(2); componentRepository.save(c);

        c = sc("Super Sensors", ComponentType.SENSORS, "Super sensors, rating 3", 20, 100000, 10);
        c.setSensorRating(3); componentRepository.save(c);

        // Defense
        c = sc("Nuclear Damper", ComponentType.NUCLEAR_DAMPERS, "Defense against nuclear weapons", 20, 100000, 8);
        c.setNuclearDampers(1); componentRepository.save(c);

        c = sc("Meson Screen", ComponentType.MESON_SCREENS, "Defense against meson guns", 30, 200000, 10);
        c.setMesonScreens(1); componentRepository.save(c);

        // Cargo
        c = sc("Cargo Hold 100T", ComponentType.CARGO, "100 tons cargo capacity", 100, 50000, 6);
        c.setCargoCapacity(100); componentRepository.save(c);

        c = sc("Cargo Hold 500T", ComponentType.CARGO, "500 tons cargo capacity", 500, 250000, 6);
        c.setCargoCapacity(500); componentRepository.save(c);

        // Fuel
        c = sc("Fuel Tank 100T", ComponentType.FUEL, "100 tons fuel capacity", 100, 20000, 6);
        c.setFuelCapacity(100); componentRepository.save(c);

        // Crew & Marines
        c = sc("Crew Quarters 10", ComponentType.CREW, "Accommodation for 10 crew", 10, 20000, 6);
        c.setCrewCapacity(10); componentRepository.save(c);

        c = sc("Crew Quarters 50", ComponentType.CREW, "Accommodation for 50 crew", 50, 100000, 6);
        c.setCrewCapacity(50); componentRepository.save(c);

        c = sc("Marine Force 10", ComponentType.MARINES, "Marine boarding party of 10", 20, 100000, 6);
        c.setMarineCapacity(10); componentRepository.save(c);

        log.info("Initialized {} default ship components", componentRepository.count());
    }

    private ShipComponent sc(String name, ComponentType type, String desc, int tonnage, long cost, int tl) {
        return new ShipComponent(name, type, desc, tonnage, cost, tl);
    }

    private com.yisy.modules.doramcu.traveller.HighGuardWeapon mapToHighGuardWeapon(ShipComponent component) {
        String name = component.getName().toLowerCase();
        if (name.contains("pulse laser")) return com.yisy.modules.doramcu.traveller.HighGuardWeapon.PULSE_LASER;
        if (name.contains("beam laser")) return com.yisy.modules.doramcu.traveller.HighGuardWeapon.BEAM_LASER;
        if (name.contains("particle")) return com.yisy.modules.doramcu.traveller.HighGuardWeapon.PARTICLE_BEAM;
        if (name.contains("fusion")) return com.yisy.modules.doramcu.traveller.HighGuardWeapon.FUSION_GUN;
        if (name.contains("meson")) return com.yisy.modules.doramcu.traveller.HighGuardWeapon.MESON_GUN;
        if (name.contains("sand")) return com.yisy.modules.doramcu.traveller.HighGuardWeapon.SANDCASTER;
        if (name.contains("missile")) return com.yisy.modules.doramcu.traveller.HighGuardWeapon.MISSILE_HE;
        if (name.contains("torpedo")) return com.yisy.modules.doramcu.traveller.HighGuardWeapon.TORPEDO_BASIC;
        return null;
    }
}

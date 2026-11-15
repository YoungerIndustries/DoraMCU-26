package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.hex.AiCoreComponent;
import com.yisy.modules.doramcu.hex.ArmyUnit;
import com.yisy.modules.doramcu.hex.ArmyUnitType;
import com.yisy.modules.doramcu.hex.GroundVehicle;
import com.yisy.modules.doramcu.hex.PlanetFacility;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.SpaceStation;
import com.yisy.modules.doramcu.hex.spi.AiEntityRegistration;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Service for registering ships, space stations, ground vehicles, and facilities
 * as AI characters in the AI character repository.
 * <p>
 * Each entity's computer module (AI Core) is represented as an AI character,
 * establishing its connection to the AI network. This allows the AI system
 * to treat entity computers as first-class AI characters with experience,
 * leadership ratings, and battle records.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * // Register a ship's computer as an AI character
 * AiCharacter shipAi = service.registerShip(ship, "Federation");
 *
 * // Register a station's computer as an AI character
 * AiCharacter stationAi = service.registerStation(station, "Federation");
 *
 * // Register a ground vehicle's computer as an AI character
 * AiCharacter vehicleAi = service.registerVehicle(vehicle, "Federation");
 *
 * // Register a facility's computer as an AI character
 * AiCharacter facilityAi = service.registerFacility(facility, "Federation");
 *
 * // Look up an entity's AI character
 * Optional<AiCharacter> ai = service.findShipAiCharacter(ship.getId());
 * </pre>
 * </p>
 */
@Service
public class AiEntityRegistrationService implements AiEntityRegistration {

    private static final Logger log = LoggerFactory.getLogger(AiEntityRegistrationService.class);

    private final AiCharacterRepository aiCharacterRepository;

    public AiEntityRegistrationService(AiCharacterRepository aiCharacterRepository) {
        this.aiCharacterRepository = aiCharacterRepository;
    }

    // --- Ship Registration ---

    /**
     * Registers a ship's computer as an AI character.
     * <p>
     * If the ship already has an AI character registered, returns the existing one.
     * </p>
     *
     * @param ship the ship to register
     * @param empireName the empire name the ship belongs to
     * @return the created or existing AI character
     */
    @Transactional
    public AiCharacter registerShip(Ship ship, String empireName) {
        if (ship == null) {
            throw new IllegalArgumentException("Ship must not be null");
        }

        // Check if already registered
        Optional<AiCharacter> existing = aiCharacterRepository
                .findByCharacterTypeAndLinkedShipId(AiCharacter.CharacterType.SHIP, (long) ship.getId());
        if (existing.isPresent()) {
            log.debug("Ship {} already registered as AI character: {}", ship.getName(), existing.get().getName());
            return existing.get();
        }

        AiCharacter aiCharacter = new AiCharacter();
        aiCharacter.setName(ship.getName() + " Computer");
        aiCharacter.setRole("Ship Computer");
        aiCharacter.setEmpireName(empireName != null ? empireName : "Unknown");
        aiCharacter.setCharacterType(AiCharacter.CharacterType.SHIP);
        aiCharacter.setLinkedShipId((long) ship.getId());
        aiCharacter.setActive(true);
        aiCharacter.setHirable(false);
        aiCharacter.setAiAdmin(false);

        // Set primary skill based on ship type
        aiCharacter.setPrimarySkill("Naval Combat");

        // Set description based on AI Core level
        if (ship.hasAiCore()) {
            AiCoreComponent aiCore = ship.getAiCore();
            aiCharacter.setDescription(
                    "Ship computer for " + ship.getName() +
                    " (" + ship.getShipType() + ") with " + aiCore.getLevel() + " AI Core. " +
                    "Provides " + (aiCore.getDamageBonus() * 100) + "% combat bonus."
            );
            // Training level based on AI Core tier
            switch (aiCore.getTier()) {
                case BASIC:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.BASIC);
                    break;
                case ADVANCED:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.ADVANCED);
                    break;
                case ELITE:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.MASTER);
                    break;
            }
        } else {
            aiCharacter.setDescription("Ship computer for " + ship.getName() + " (" + ship.getShipType() + ") without AI Core.");
            aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.UNTRAINED);
        }

        AiCharacter saved = aiCharacterRepository.save(aiCharacter);
        log.info("Registered ship {} as AI character: {}", ship.getName(), saved.getName());
        return saved;
    }

    /**
     * Finds the AI character for a ship by ship ID.
     *
     * @param shipId the ship ID
     * @return the AI character if found
     */
    public Optional<AiCharacter> findShipAiCharacter(int shipId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedShipId(
                AiCharacter.CharacterType.SHIP, (long) shipId);
    }

    /**
     * Finds all active ship AI characters for an empire.
     *
     * @param empireName the empire name
     * @return list of ship AI characters
     */
    public List<AiCharacter> findShipAiCharactersByEmpire(String empireName) {
        return aiCharacterRepository
                .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                        List.of(AiCharacter.CharacterType.SHIP), empireName);
    }

    // --- Space Station Registration ---

    /**
     * Registers a space station's computer as an AI character.
     * <p>
     * If the station already has an AI character registered, returns the existing one.
     * </p>
     *
     * @param station the station to register
     * @param empireName the empire name the station belongs to
     * @return the created or existing AI character
     */
    @Transactional
    public AiCharacter registerStation(SpaceStation station, String empireName) {
        if (station == null) {
            throw new IllegalArgumentException("Station must not be null");
        }

        // Check if already registered
        Optional<AiCharacter> existing = aiCharacterRepository
                .findByCharacterTypeAndLinkedStationId(AiCharacter.CharacterType.STATION, (long) station.getId());
        if (existing.isPresent()) {
            log.debug("Station {} already registered as AI character: {}", station.getName(), existing.get().getName());
            return existing.get();
        }

        AiCharacter aiCharacter = new AiCharacter();
        aiCharacter.setName(station.getName() + " Computer");
        aiCharacter.setRole("Station Computer");
        aiCharacter.setEmpireName(empireName != null ? empireName : "Unknown");
        aiCharacter.setCharacterType(AiCharacter.CharacterType.STATION);
        aiCharacter.setLinkedStationId((long) station.getId());
        aiCharacter.setActive(true);
        aiCharacter.setHirable(false);
        aiCharacter.setAiAdmin(false);

        // Set primary skill based on station type
        switch (station.getStationType()) {
            case MILITARY:
                aiCharacter.setPrimarySkill("Naval Combat");
                break;
            case RESEARCH:
                aiCharacter.setPrimarySkill("Research");
                break;
            case TRADE:
                aiCharacter.setPrimarySkill("Economics");
                break;
            case MINING:
                aiCharacter.setPrimarySkill("Resource Extraction");
                break;
            case SHIPYARD:
                aiCharacter.setPrimarySkill("Production");
                break;
            case COLONY:
                aiCharacter.setPrimarySkill("Administration");
                break;
            default:
                aiCharacter.setPrimarySkill("Station Operations");
                break;
        }

        // Set description based on AI Core level
        if (station.hasAiCore()) {
            int aiCoreLevel = station.getAiCoreLevel();
            aiCharacter.setDescription(
                    "Station computer for " + station.getName() +
                    " (" + station.getStationType() + ") with AI Core Level " + aiCoreLevel + ". " +
                    "Support radius: " + station.getAiSupportRadius() + " hexes."
            );
            switch (aiCoreLevel) {
                case 1:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.BASIC);
                    break;
                case 2:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.ADVANCED);
                    break;
                case 3:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.MASTER);
                    break;
                default:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.UNTRAINED);
                    break;
            }
        } else {
            aiCharacter.setDescription("Station computer for " + station.getName() + " (" + station.getStationType() + ") without AI Core.");
            aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.UNTRAINED);
        }

        AiCharacter saved = aiCharacterRepository.save(aiCharacter);
        log.info("Registered station {} as AI character: {}", station.getName(), saved.getName());
        return saved;
    }

    /**
     * Finds the AI character for a station by station ID.
     *
     * @param stationId the station ID
     * @return the AI character if found
     */
    public Optional<AiCharacter> findStationAiCharacter(int stationId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedStationId(
                AiCharacter.CharacterType.STATION, (long) stationId);
    }

    /**
     * Finds all active station AI characters for an empire.
     *
     * @param empireName the empire name
     * @return list of station AI characters
     */
    public List<AiCharacter> findStationAiCharactersByEmpire(String empireName) {
        return aiCharacterRepository
                .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                        List.of(AiCharacter.CharacterType.STATION), empireName);
    }

    // --- Ground Vehicle Registration ---

    /**
     * Registers a ground vehicle's computer as an AI character.
     * <p>
     * If the vehicle already has an AI character registered, returns the existing one.
     * </p>
     *
     * @param vehicle the vehicle to register
     * @param empireName the empire name the vehicle belongs to
     * @return the created or existing AI character
     */
    @Transactional
    public AiCharacter registerVehicle(GroundVehicle vehicle, String empireName) {
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle must not be null");
        }

        // Check if already registered using the string entity ID
        Optional<AiCharacter> existing = aiCharacterRepository
                .findByCharacterTypeAndLinkedEntityId(AiCharacter.CharacterType.VEHICLE, vehicle.getId());
        if (existing.isPresent()) {
            log.debug("Vehicle {} already registered as AI character: {}", vehicle.getName(), existing.get().getName());
            return existing.get();
        }

        AiCharacter aiCharacter = new AiCharacter();
        aiCharacter.setName(vehicle.getName() + " Computer");
        aiCharacter.setRole("Vehicle Computer");
        aiCharacter.setEmpireName(empireName != null ? empireName : "Unknown");
        aiCharacter.setCharacterType(AiCharacter.CharacterType.VEHICLE);
        aiCharacter.setLinkedEntityId(vehicle.getId());
        aiCharacter.setActive(vehicle.isActive());
        aiCharacter.setHirable(false);
        aiCharacter.setAiAdmin(false);

        // Set primary skill based on vehicle type
        switch (vehicle.getType()) {
            case TANK:
            case MISSILE_LAUNCHER:
                aiCharacter.setPrimarySkill("Armor Tactics");
                break;
            case APC:
            case IFV:
                aiCharacter.setPrimarySkill("Infantry Support");
                break;
            case ARTILLERY:
                aiCharacter.setPrimarySkill("Artillery");
                break;
            case FIGHTER_JET:
            case BOMBER:
            case HELICOPTER:
                aiCharacter.setPrimarySkill("Air Combat");
                break;
            case TRANSPORT_SHIP:
            case FERRY:
                aiCharacter.setPrimarySkill("Naval Transport");
                break;
            case BUS:
            case TRAIN:
            case CAR:
            case TRUCK:
            case AIRCRAFT:
                aiCharacter.setPrimarySkill("Transport");
                break;
            default:
                aiCharacter.setPrimarySkill("Ground Combat");
                break;
        }

        // Set description based on AI Core level
        if (vehicle.hasAiCore()) {
            AiCoreComponent aiCore = vehicle.getAiCore();
            aiCharacter.setDescription(
                    "Vehicle computer for " + vehicle.getName() +
                    " (" + vehicle.getType() + ") with " + aiCore.getLevel() + " AI Core. " +
                    "Armor: " + vehicle.getArmorRating() + ", Firepower: " + vehicle.getFirepower() + "."
            );
            switch (aiCore.getTier()) {
                case BASIC:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.BASIC);
                    break;
                case ADVANCED:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.ADVANCED);
                    break;
                case ELITE:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.MASTER);
                    break;
            }
        } else {
            aiCharacter.setDescription("Vehicle computer for " + vehicle.getName() + " (" + vehicle.getType() + ") without AI Core.");
            aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.UNTRAINED);
        }

        AiCharacter saved = aiCharacterRepository.save(aiCharacter);
        log.info("Registered vehicle {} as AI character: {}", vehicle.getName(), saved.getName());
        return saved;
    }

    /**
     * Finds the AI character for a vehicle by vehicle ID string.
     *
     * @param vehicleId the vehicle ID string
     * @return the AI character if found
     */
    public Optional<AiCharacter> findVehicleAiCharacter(String vehicleId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedEntityId(
                AiCharacter.CharacterType.VEHICLE, vehicleId);
    }

    /**
     * Finds all active vehicle AI characters for an empire.
     *
     * @param empireName the empire name
     * @return list of vehicle AI characters
     */
    public List<AiCharacter> findVehicleAiCharactersByEmpire(String empireName) {
        return aiCharacterRepository
                .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                        List.of(AiCharacter.CharacterType.VEHICLE), empireName);
    }

    // --- Facility Registration ---

    /**
     * Registers a facility's computer as an AI character.
     * <p>
     * If the facility already has an AI character registered, returns the existing one.
     * </p>
     *
     * @param facility the facility to register
     * @param empireName the empire name the facility belongs to
     * @return the created or existing AI character
     */
    @Transactional
    public AiCharacter registerFacility(PlanetFacility facility, String empireName) {
        if (facility == null) {
            throw new IllegalArgumentException("Facility must not be null");
        }

        // Check if already registered
        Optional<AiCharacter> existing = aiCharacterRepository
                .findByCharacterTypeAndLinkedFacilityId(AiCharacter.CharacterType.FACILITY, (long) facility.getId());
        if (existing.isPresent()) {
            log.debug("Facility {} already registered as AI character: {}", facility.getFacilityType(), existing.get().getName());
            return existing.get();
        }

        AiCharacter aiCharacter = new AiCharacter();
        aiCharacter.setName(facility.getFacilityType().name() + " Facility-" + facility.getId() + " Computer");
        aiCharacter.setRole("Facility Computer");
        aiCharacter.setEmpireName(empireName != null ? empireName : "Unknown");
        aiCharacter.setCharacterType(AiCharacter.CharacterType.FACILITY);
        aiCharacter.setLinkedFacilityId((long) facility.getId());
        aiCharacter.setActive(facility.isOperational());
        aiCharacter.setHirable(false);
        aiCharacter.setAiAdmin(false);

        // Set primary skill based on facility type
        switch (facility.getFacilityType()) {
            case SHIPYARD:
                aiCharacter.setPrimarySkill("Production");
                break;
            case MINING_FACILITY:
                aiCharacter.setPrimarySkill("Resource Extraction");
                break;
            case RESEARCH_LAB:
                aiCharacter.setPrimarySkill("Research");
                break;
            case MILITARY_BASE:
            case DEFENSE_TURRET:
                aiCharacter.setPrimarySkill("Defense");
                break;
            case TRADE_PORT:
                aiCharacter.setPrimarySkill("Economics");
                break;
            case POWER_PLANT:
                aiCharacter.setPrimarySkill("Energy Management");
                break;
            case SENSOR_ARRAY:
                aiCharacter.setPrimarySkill("Intelligence");
                break;
            case COLONY_DOME:
                aiCharacter.setPrimarySkill("Administration");
                break;
            case AGRICULTURAL_CENTER:
                aiCharacter.setPrimarySkill("Agriculture");
                break;
            default:
                aiCharacter.setPrimarySkill("Facility Operations");
                break;
        }

        // Set description based on AI Core level
        if (facility.hasAiCore()) {
            AiCoreComponent aiCore = facility.getAiCore();
            aiCharacter.setDescription(
                    "Facility computer for " + facility.getFacilityType().name() +
                    " (Level " + facility.getLevel() + ") with " + aiCore.getLevel() + " AI Core."
            );
            switch (aiCore.getTier()) {
                case BASIC:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.BASIC);
                    break;
                case ADVANCED:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.ADVANCED);
                    break;
                case ELITE:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.MASTER);
                    break;
            }
        } else {
            aiCharacter.setDescription("Facility computer for " + facility.getFacilityType().name() +
                    " (Level " + facility.getLevel() + ") without AI Core.");
            aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.UNTRAINED);
        }

        AiCharacter saved = aiCharacterRepository.save(aiCharacter);
        log.info("Registered facility {} as AI character: {}", facility.getFacilityType(), saved.getName());
        return saved;
    }

    /**
     * Finds the AI character for a facility by facility ID.
     *
     * @param facilityId the facility ID
     * @return the AI character if found
     */
    public Optional<AiCharacter> findFacilityAiCharacter(int facilityId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedFacilityId(
                AiCharacter.CharacterType.FACILITY, (long) facilityId);
    }

    /**
     * Finds all active facility AI characters for an empire.
     *
     * @param empireName the empire name
     * @return list of facility AI characters
     */
    public List<AiCharacter> findFacilityAiCharactersByEmpire(String empireName) {
        return aiCharacterRepository
                .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                        List.of(AiCharacter.CharacterType.FACILITY), empireName);
    }

    // --- Army Unit Registration ---

    /**
     * Registers an army unit's computer as an AI character.
     * <p>
     * Uses VEHICLE character type with entity ID string {@code army_unit_{unitId}}
     * for repository lookup. If the unit already has an AI character registered,
     * returns the existing one.
     * </p>
     *
     * @param unit the army unit to register
     * @param empireName the empire name the unit belongs to
     * @return the created or existing AI character
     */
    @Transactional
    public AiCharacter registerArmyUnit(ArmyUnit unit, String empireName) {
        if (unit == null) {
            throw new IllegalArgumentException("ArmyUnit must not be null");
        }

        String entityId = "army_unit_" + unit.getId();

        // Check if already registered
        Optional<AiCharacter> existing = aiCharacterRepository
                .findByCharacterTypeAndLinkedEntityId(AiCharacter.CharacterType.VEHICLE, entityId);
        if (existing.isPresent()) {
            log.debug("ArmyUnit {} already registered as AI character: {}", unit.getName(), existing.get().getName());
            return existing.get();
        }

        AiCharacter aiCharacter = new AiCharacter();
        aiCharacter.setName(unit.getName() + " Computer");
        aiCharacter.setRole("Army Unit Computer");
        aiCharacter.setEmpireName(empireName != null ? empireName : "Unknown");
        aiCharacter.setCharacterType(AiCharacter.CharacterType.VEHICLE);
        aiCharacter.setLinkedEntityId(entityId);
        aiCharacter.setActive(true);
        aiCharacter.setHirable(false);
        aiCharacter.setAiAdmin(false);

        // Set primary skill based on ArmyUnitType
        switch (unit.getUnitType()) {
            case INFANTRY:
                aiCharacter.setPrimarySkill("Infantry Tactics");
                break;
            case MECHANIZED:
                aiCharacter.setPrimarySkill("Mechanized Warfare");
                break;
            case TANK_DIVISION:
                aiCharacter.setPrimarySkill("Armor Tactics");
                break;
            case ARTILLERY:
                aiCharacter.setPrimarySkill("Artillery");
                break;
            case MISSILE_BATTERY:
                aiCharacter.setPrimarySkill("Missile Warfare");
                break;
            case AIR_SUPPORT:
                aiCharacter.setPrimarySkill("Air Combat");
                break;
            case ENGINEER:
                aiCharacter.setPrimarySkill("Engineering");
                break;
            case SPECIAL_FORCES:
                aiCharacter.setPrimarySkill("Special Operations");
                break;
            case MARINES:
                aiCharacter.setPrimarySkill("Amphibious Assault");
                break;
            default:
                aiCharacter.setPrimarySkill("Ground Combat");
                break;
        }

        // Set description based on AI Core level
        if (unit.hasAiCore()) {
            AiCoreComponent aiCore = unit.getAiCore();
            aiCharacter.setDescription(
                    "Army unit computer for " + unit.getName() +
                    " (" + unit.getUnitType() + ") with " + aiCore.getLevel() + " AI Core. " +
                    "Strength: " + unit.getCurrentStrength() + "/" + unit.getMaxStrength() + "."
            );
            switch (aiCore.getTier()) {
                case BASIC:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.BASIC);
                    break;
                case ADVANCED:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.ADVANCED);
                    break;
                case ELITE:
                    aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.MASTER);
                    break;
            }
        } else {
            aiCharacter.setDescription("Army unit computer for " + unit.getName() + " (" + unit.getUnitType() + ") without AI Core.");
            aiCharacter.setTrainingLevel(AiCharacter.TrainingLevel.UNTRAINED);
        }

        AiCharacter saved = aiCharacterRepository.save(aiCharacter);
        log.info("Registered army unit {} as AI character: {}", unit.getName(), saved.getName());
        return saved;
    }

    /**
     * Finds the AI character for an army unit by unit ID.
     *
     * @param unitId the army unit ID
     * @return the AI character if found
     */
    public Optional<AiCharacter> findArmyUnitAiCharacter(int unitId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedEntityId(
                AiCharacter.CharacterType.VEHICLE, "army_unit_" + unitId);
    }

    /**
     * Deactivates the AI character for an army unit when the unit is destroyed.
     *
     * @param unitId the army unit ID
     * @return true if the AI character was found and deactivated
     */
    @Transactional
    public boolean deactivateArmyUnitAiCharacter(int unitId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedEntityId(
                AiCharacter.CharacterType.VEHICLE, "army_unit_" + unitId)
                .map(ai -> {
                    ai.setActive(false);
                    aiCharacterRepository.save(ai);
                    log.info("Deactivated AI character for destroyed army unit: {}", ai.getName());
                    return true;
                })
                .orElse(false);
    }

    // --- Bulk Operations ---

    /**
     * Finds all active entity-linked AI characters (ships, stations, vehicles, facilities)
     * for a given empire.
     *
     * @param empireName the empire name
     * @return list of all entity AI characters
     */
    public List<AiCharacter> findAllEntityAiCharactersByEmpire(String empireName) {
        return aiCharacterRepository
                .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                        List.of(
                                AiCharacter.CharacterType.SHIP,
                                AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE,
                                AiCharacter.CharacterType.FACILITY
                        ), empireName);
    }

    /**
     * Deactivates the AI character for a ship when the ship is destroyed.
     *
     * @param shipId the ship ID
     * @return true if the AI character was found and deactivated
     */
    @Transactional
    public boolean deactivateShipAiCharacter(int shipId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedShipId(
                AiCharacter.CharacterType.SHIP, (long) shipId)
                .map(ai -> {
                    ai.setActive(false);
                    aiCharacterRepository.save(ai);
                    log.info("Deactivated AI character for destroyed ship: {}", ai.getName());
                    return true;
                })
                .orElse(false);
    }

    /**
     * Deactivates the AI character for a station when the station is destroyed.
     *
     * @param stationId the station ID
     * @return true if the AI character was found and deactivated
     */
    @Transactional
    public boolean deactivateStationAiCharacter(int stationId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedStationId(
                AiCharacter.CharacterType.STATION, (long) stationId)
                .map(ai -> {
                    ai.setActive(false);
                    aiCharacterRepository.save(ai);
                    log.info("Deactivated AI character for destroyed station: {}", ai.getName());
                    return true;
                })
                .orElse(false);
    }

    /**
     * Deactivates the AI character for a vehicle when the vehicle is destroyed.
     *
     * @param vehicleId the vehicle ID string
     * @return true if the AI character was found and deactivated
     */
    @Transactional
    public boolean deactivateVehicleAiCharacter(String vehicleId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedEntityId(
                AiCharacter.CharacterType.VEHICLE, vehicleId)
                .map(ai -> {
                    ai.setActive(false);
                    aiCharacterRepository.save(ai);
                    log.info("Deactivated AI character for destroyed vehicle: {}", ai.getName());
                    return true;
                })
                .orElse(false);
    }

    /**
     * Deactivates the AI character for a facility when the facility is disabled.
     *
     * @param facilityId the facility ID
     * @return true if the AI character was found and deactivated
     */
    @Transactional
    public boolean deactivateFacilityAiCharacter(int facilityId) {
        return aiCharacterRepository.findByCharacterTypeAndLinkedFacilityId(
                AiCharacter.CharacterType.FACILITY, (long) facilityId)
                .map(ai -> {
                    ai.setActive(false);
                    aiCharacterRepository.save(ai);
                    log.info("Deactivated AI character for disabled facility: {}", ai.getName());
                    return true;
                })
                .orElse(false);
    }
}

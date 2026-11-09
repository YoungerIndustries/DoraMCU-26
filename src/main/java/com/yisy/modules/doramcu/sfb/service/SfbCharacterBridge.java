package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.sfb.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Connects Captain's Log characters to SFB battles.
 * <p>
 * Loads characters, validates their status (injury, availability),
 * and creates SfbCharacterAssignment entries. Acts as the gateway
 * between the Captain's Log and SFB systems.
 * </p>
 */
public class SfbCharacterBridge {

    // Simulated character repository (in production, this would be a Spring Data repository)
    private final Map<Long, CaptainLogCharacter> characterRepository = new ConcurrentHashMap<>();
    private final Map<String, List<SfbCharacterAssignment>> battleAssignments = new ConcurrentHashMap<>();
    private final AtomicLong characterIdCounter = new AtomicLong(1);

    /**
     * Register a character in the bridge (for testing/demo purposes).
     */
    public Long registerCharacter(CaptainLogCharacter character) {
        Long id = character.getId();
        if (id == null) {
            id = characterIdCounter.getAndIncrement();
            character.setId(id);
        }
        characterRepository.put(id, character);
        return id;
    }

    /**
     * Load a character by ID.
     */
    public CaptainLogCharacter loadCharacter(Long id) {
        return characterRepository.get(id);
    }

    /**
     * Validate that a character is available for SFB combat.
     * Checks injury state, availability, and other constraints.
     */
    public boolean isCharacterAvailable(CaptainLogCharacter character) {
        if (character == null) return false;

        // Check if character is injured or incapacitated
        // In Captain's Log, constitution <= 0 means incapacitated
        if (character.getConstitution() <= 0) return false;

        // Check if character has a valid rank
        if (character.getRank() == null) return false;

        return true;
    }

    /**
     * Assign a character to a battle with a specific role.
     */
    public SfbCharacterAssignment assignCharacterToBattle(String battleId, Long characterId,
                                                           SfbRole role, String shipId,
                                                           SfbCrewStationType station) {
        CaptainLogCharacter character = loadCharacter(characterId);
        if (character == null) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }

        if (!isCharacterAvailable(character)) {
            throw new IllegalStateException("Character " + character.getName() + " is not available for combat");
        }

        // Validate role requirements
        if (role == SfbRole.CAPTAIN || role == SfbRole.CREW) {
            if (shipId == null || shipId.isEmpty()) {
                throw new IllegalArgumentException("Ship ID is required for " + role + " role");
            }
        }

        if (role == SfbRole.CREW && station == null) {
            throw new IllegalArgumentException("Station is required for CREW role");
        }

        SfbCharacterAssignment assignment = new SfbCharacterAssignment();
        assignment.setBattleId(battleId);
        assignment.setCharacterId(characterId);
        assignment.setCharacterName(character.getName());
        assignment.setRole(role);
        assignment.setShipId(shipId);
        assignment.setStation(station);
        assignment.setActive(true);

        // Calculate and set initial modifiers
        Map<String, Integer> modifiers = calculateCharacterModifiers(character, role, station);
        assignment.setModifiers(modifiers);

        // Add to battle assignments
        battleAssignments.computeIfAbsent(battleId, k -> new ArrayList<>()).add(assignment);

        return assignment;
    }

    /**
     * Get all character assignments for a battle.
     */
    public List<SfbCharacterAssignment> getBattleAssignments(String battleId) {
        return battleAssignments.getOrDefault(battleId, Collections.emptyList());
    }

    /**
     * Get assignments for a specific ship.
     */
    public List<SfbCharacterAssignment> getShipAssignments(String battleId, String shipId) {
        return getBattleAssignments(battleId).stream()
                .filter(a -> shipId.equals(a.getShipId()))
                .toList();
    }

    /**
     * Get the Admiral assignment for a battle.
     */
    public SfbCharacterAssignment getAdmiralAssignment(String battleId, int side) {
        return getBattleAssignments(battleId).stream()
                .filter(a -> a.getRole() == SfbRole.ADMIRAL && a.getSide() == side)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the Captain assignment for a ship.
     */
    public SfbCharacterAssignment getCaptainAssignment(String battleId, String shipId) {
        return getBattleAssignments(battleId).stream()
                .filter(a -> a.getRole() == SfbRole.CAPTAIN && shipId.equals(a.getShipId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get crew assignments for a ship.
     */
    public List<SfbCharacterAssignment> getCrewAssignments(String battleId, String shipId) {
        return getBattleAssignments(battleId).stream()
                .filter(a -> a.getRole() == SfbRole.CREW && shipId.equals(a.getShipId()))
                .toList();
    }

    /**
     * Calculate combat modifiers for a character based on their attributes and disciplines.
     */
    public Map<String, Integer> calculateCharacterModifiers(CaptainLogCharacter character,
                                                             SfbRole role, SfbCrewStationType station) {
        Map<String, Integer> modifiers = new HashMap<>();

        // Attribute-based modifiers (each attribute point gives +1 to relevant system)
        modifiers.put("FIT", character.getFitness());
        modifiers.put("TEC", character.getTechnical());
        modifiers.put("STR", character.getStrength());
        modifiers.put("REA", character.getReason());
        modifiers.put("INS", character.getInsight());
        modifiers.put("PRE", character.getPresence());

        // Discipline-based modifiers
        modifiers.put("COMMAND", character.getCommand());
        modifiers.put("LEADERSHIP", character.getLeadership());
        modifiers.put("SCIENCE", character.getScience());
        modifiers.put("SECURITY", character.getSecurity());
        modifiers.put("MEDICINE", character.getMedicine());
        modifiers.put("OPERATIONS", character.getOperations());

        // Role-specific bonuses
        switch (role) {
            case ADMIRAL:
                // Admiral gets fleet-wide bonuses based on Command/Leadership
                modifiers.put("FLEET_ENERGY_BONUS", character.getCommand() / 2);
                modifiers.put("FLEET_ACCURACY_BONUS", character.getLeadership() / 2);
                modifiers.put("FLEET_SHIELD_BONUS", (character.getCommand() + character.getLeadership()) / 3);
                break;
            case CAPTAIN:
                // Captain gets ship-specific bonuses
                modifiers.put("SHIELD_EFFICIENCY", character.getPresence() / 2);
                modifiers.put("SENSOR_RANGE", character.getReason() / 2);
                modifiers.put("WEAPON_DAMAGE", character.getStrength() / 2);
                modifiers.put("EVASION", character.getInsight() / 2);
                modifiers.put("DAMCON", character.getTechnical() / 2);
                modifiers.put("CREW_MORALE", character.getFitness() / 2);
                break;
            case CREW:
                if (station != null) {
                    // Station-specific bonuses
                    int stationBonus = 0;
                    switch (station) {
                        case HELM:
                            stationBonus = character.getOperations();
                            modifiers.put("MOVEMENT_ACCURACY", stationBonus / 2);
                            modifiers.put("SPEED_BONUS", stationBonus / 3);
                            break;
                        case TACTICAL:
                            stationBonus = character.getSecurity();
                            modifiers.put("HIT_PROBABILITY", stationBonus / 2);
                            modifiers.put("WEAPON_DAMAGE_BONUS", stationBonus / 3);
                            break;
                        case ENGINEERING:
                            stationBonus = character.getOperations();
                            modifiers.put("DAMCON_SPEED", stationBonus / 2);
                            modifiers.put("ENERGY_BONUS", stationBonus / 3);
                            break;
                        case SENSORS:
                            stationBonus = character.getScience();
                            modifiers.put("DETECTION_RANGE", stationBonus / 2);
                            modifiers.put("CLOAK_DETECTION", stationBonus / 3);
                            break;
                        case MEDICAL:
                            stationBonus = character.getMedicine();
                            modifiers.put("INJURY_RECOVERY", stationBonus / 2);
                            modifiers.put("DAMAGE_MITIGATION", stationBonus / 3);
                            break;
                    }
                }
                break;
        }

        return modifiers;
    }

    /**
     * Remove a character from a battle.
     */
    public void removeCharacterFromBattle(String battleId, Long characterId) {
        List<SfbCharacterAssignment> assignments = battleAssignments.get(battleId);
        if (assignments != null) {
            assignments.removeIf(a -> a.getCharacterId().equals(characterId));
        }
    }

    /**
     * Get all registered characters.
     */
    public List<CaptainLogCharacter> getAllCharacters() {
        return new ArrayList<>(characterRepository.values());
    }
}

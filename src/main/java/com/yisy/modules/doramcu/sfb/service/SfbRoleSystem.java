package com.yisy.modules.doramcu.sfb.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.sfb.dto.SfbCharacterModifiers;
import com.yisy.modules.doramcu.sfb.model.*;

import java.util.*;

/**
 * Manages character roles and calculates combat modifiers.
 * <p>
 * Handles Admiral fleet-wide bonuses, Captain ship-specific bonuses,
 * and Crew station-specific bonuses.
 * </p>
 */
public class SfbRoleSystem {

    private final SfbCharacterBridge characterBridge;

    public SfbRoleSystem(SfbCharacterBridge characterBridge) {
        this.characterBridge = characterBridge;
    }

    /**
     * Apply Admiral modifiers to all ships in a fleet.
     */
    public void applyAdmiralModifiers(SfbCharacterAssignment admiralAssignment, List<SfbShip> fleet) {
        if (admiralAssignment == null || admiralAssignment.getModifiers() == null) return;

        Map<String, Integer> modifiers = admiralAssignment.getModifiers();
        int energyBonus = modifiers.getOrDefault("FLEET_ENERGY_BONUS", 0);
        int accuracyBonus = modifiers.getOrDefault("FLEET_ACCURACY_BONUS", 0);
        int shieldBonus = modifiers.getOrDefault("FLEET_SHIELD_BONUS", 0);

        for (SfbShip ship : fleet) {
            // Apply energy bonus
            if (ship.getEnergyAllocation() != null && energyBonus > 0) {
                ship.getEnergyAllocation().setAuxiliaryPower(
                        ship.getEnergyAllocation().getAuxiliaryPower() + energyBonus);
            }

            // Apply shield reinforcement bonus
            if (shieldBonus > 0) {
                for (SfbShield shield : ship.getShields()) {
                    if (shield != null) {
                        shield.reinforce(shieldBonus);
                    }
                }
            }
        }
    }

    /**
     * Apply Captain modifiers to a ship.
     */
    public void applyCaptainModifiers(SfbCharacterAssignment captainAssignment, SfbShip ship) {
        if (captainAssignment == null || captainAssignment.getModifiers() == null) return;

        Map<String, Integer> modifiers = captainAssignment.getModifiers();

        // Shield efficiency: PRE-based bonus
        int shieldEfficiency = modifiers.getOrDefault("SHIELD_EFFICIENCY", 0);
        if (shieldEfficiency > 0) {
            for (SfbShield shield : ship.getShields()) {
                if (shield != null) {
                    shield.reinforce(shieldEfficiency);
                }
            }
        }

        // DamCon bonus: TEC-based
        int damConBonus = modifiers.getOrDefault("DAMCON", 0);
        if (damConBonus > 0 && ship.getSsd() != null) {
            ship.getSsd().setDamConRating(ship.getSsd().getDamConRating() + damConBonus);
        }
    }

    /**
     * Apply Crew station modifiers to a ship.
     */
    public void applyCrewModifiers(List<SfbCharacterAssignment> crewAssignments, SfbShip ship) {
        for (SfbCharacterAssignment assignment : crewAssignments) {
            if (assignment.getModifiers() == null) continue;

            Map<String, Integer> modifiers = assignment.getModifiers();

            switch (assignment.getStation()) {
                case HELM:
                    // Movement accuracy and speed bonuses
                    break;
                case TACTICAL:
                    // Weapon hit probability bonus applied during fire resolution
                    break;
                case ENGINEERING:
                    // Energy allocation bonus
                    int energyBonus = modifiers.getOrDefault("ENERGY_BONUS", 0);
                    if (energyBonus > 0 && ship.getEnergyAllocation() != null) {
                        ship.getEnergyAllocation().setAuxiliaryPower(
                                ship.getEnergyAllocation().getAuxiliaryPower() + energyBonus);
                    }
                    break;
                case SENSORS:
                    // Detection range bonus handled by SpecialSystemsService
                    break;
                case MEDICAL:
                    // Damage mitigation handled during damage resolution
                    break;
            }
        }
    }

    /**
     * Get the total modifier for a specific key from all assignments for a ship.
     */
    public int getTotalModifier(String battleId, String shipId, String modifierKey) {
        int total = 0;

        // Captain modifiers
        SfbCharacterAssignment captain = characterBridge.getCaptainAssignment(battleId, shipId);
        if (captain != null && captain.getModifiers() != null) {
            total += captain.getModifiers().getOrDefault(modifierKey, 0);
        }

        // Crew modifiers
        List<SfbCharacterAssignment> crew = characterBridge.getCrewAssignments(battleId, shipId);
        for (SfbCharacterAssignment assignment : crew) {
            if (assignment.getModifiers() != null) {
                total += assignment.getModifiers().getOrDefault(modifierKey, 0);
            }
        }

        return total;
    }

    /**
     * Get character modifiers for display.
     */
    public SfbCharacterModifiers getCharacterModifiers(Long characterId, SfbRole role,
                                                        SfbCrewStationType station) {
        CaptainLogCharacter character = characterBridge.loadCharacter(characterId);
        if (character == null) return null;

        SfbCharacterModifiers modifiers = new SfbCharacterModifiers();
        modifiers.setCharacterId(characterId);
        modifiers.setCharacterName(character.getName());
        modifiers.setRole(role);

        // Attribute bonuses
        Map<String, Integer> attributeBonuses = new HashMap<>();
        attributeBonuses.put("FIT", character.getFitness());
        attributeBonuses.put("TEC", character.getTechnical());
        attributeBonuses.put("STR", character.getStrength());
        attributeBonuses.put("REA", character.getReason());
        attributeBonuses.put("INS", character.getInsight());
        attributeBonuses.put("PRE", character.getPresence());
        modifiers.setAttributeBonuses(attributeBonuses);

        // Discipline bonuses
        Map<String, Integer> disciplineBonuses = new HashMap<>();
        disciplineBonuses.put("COMMAND", character.getCommand());
        disciplineBonuses.put("LEADERSHIP", character.getLeadership());
        disciplineBonuses.put("SCIENCE", character.getScience());
        disciplineBonuses.put("SECURITY", character.getSecurity());
        disciplineBonuses.put("MEDICINE", character.getMedicine());
        disciplineBonuses.put("OPERATIONS", character.getOperations());
        modifiers.setDisciplineBonuses(disciplineBonuses);

        // Active talents (simplified - would come from character's talent list)
        modifiers.setActiveTalents(new ArrayList<>());

        return modifiers;
    }
}

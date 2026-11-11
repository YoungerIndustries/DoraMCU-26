package com.yisy.modules.doramcu.star_trek_4x.combat;

import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekFaction;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarSystemRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarTrekFactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for fleet warfare at the strategic level.
 * <p>
 * Manages fleet deployments, movements, and engagements across the galaxy.
 * Integrates with SfbCampaignEngine for fleet management.
 * </p>
 */
@Service
public class FleetWarfareService {

    private static final Logger log = LoggerFactory.getLogger(FleetWarfareService.class);
    private static final Random random = new Random();

    private final StarTrekFactionRepository factionRepository;
    private final StarSystemRepository starSystemRepository;

    public FleetWarfareService(StarTrekFactionRepository factionRepository,
                                StarSystemRepository starSystemRepository) {
        this.factionRepository = factionRepository;
        this.starSystemRepository = starSystemRepository;
    }

    /**
     * Deploy a fleet to a system.
     *
     * @param factionId  the faction ID
     * @param systemId   the system ID
     * @param fleetSize  the fleet size
     * @return deployment result
     */
    public Map<String, Object> deployFleet(Long factionId, Long systemId, int fleetSize) {
        log.info("Deploying fleet of size {} to system {} for faction {}", fleetSize, systemId, factionId);

        StarTrekFaction faction = factionRepository.findById(factionId)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + factionId));

        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));

        // Update fleet strength
        faction.setFleetStrength(faction.getFleetStrength() + fleetSize);
        factionRepository.save(faction);

        return Map.of(
                "success", true,
                "factionName", faction.getName(),
                "systemName", system.getName(),
                "fleetSize", fleetSize,
                "totalFleetStrength", faction.getFleetStrength(),
                "message", "Fleet deployed to " + system.getName()
        );
    }

    /**
     * Move a fleet between systems.
     *
     * @param factionId    the faction ID
     * @param fromSystemId the source system ID
     * @param toSystemId   the destination system ID
     * @param fleetSize    the fleet size
     * @return movement result
     */
    public Map<String, Object> moveFleet(Long factionId, Long fromSystemId, Long toSystemId, int fleetSize) {
        log.info("Moving fleet of size {} from {} to {} for faction {}", fleetSize, fromSystemId, toSystemId, factionId);

        StarSystem from = starSystemRepository.findById(fromSystemId)
                .orElseThrow(() -> new IllegalArgumentException("Source system not found: " + fromSystemId));
        StarSystem to = starSystemRepository.findById(toSystemId)
                .orElseThrow(() -> new IllegalArgumentException("Destination system not found: " + toSystemId));

        int distance = calculateDistance(from, to);
        int movementTime = calculateMovementTime(distance);

        return Map.of(
                "success", true,
                "fromSystem", from.getName(),
                "toSystem", to.getName(),
                "fleetSize", fleetSize,
                "distance", distance,
                "movementTime", movementTime,
                "message", "Fleet moving from " + from.getName() + " to " + to.getName()
        );
    }

    /**
     * Engage an enemy fleet.
     *
     * @param attackerFactionId the attacker faction ID
     * @param defenderFactionId the defender faction ID
     * @param systemId          the system ID
     * @param attackerFleetSize the attacker fleet size
     * @param defenderFleetSize the defender fleet size
     * @return engagement result
     */
    public Map<String, Object> engageFleet(Long attackerFactionId, Long defenderFactionId,
                                            Long systemId, int attackerFleetSize, int defenderFleetSize) {
        log.info("Fleet engagement: attacker={}, defender={}, system={}", attackerFactionId, defenderFactionId, systemId);

        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));

        // Calculate combat outcome
        int attackerRoll = random.nextInt(100);
        int defenderRoll = random.nextInt(100);

        // Faction combat style bonus
        StarTrekFaction attacker = factionRepository.findById(attackerFactionId)
                .orElseThrow(() -> new IllegalArgumentException("Attacker faction not found: " + attackerFactionId));
        StarTrekFaction defender = factionRepository.findById(defenderFactionId)
                .orElseThrow(() -> new IllegalArgumentException("Defender faction not found: " + defenderFactionId));

        int attackerBonus = getCombatStyleBonus(attacker.getCombatStyle());
        int defenderBonus = getCombatStyleBonus(defender.getCombatStyle());

        int attackerTotal = attackerRoll + attackerBonus + (attackerFleetSize / 10);
        int defenderTotal = defenderRoll + defenderBonus + (defenderFleetSize / 10);

        String outcome;
        int attackerLosses;
        int defenderLosses;

        if (attackerTotal > defenderTotal) {
            outcome = "ATTACKER_VICTORY";
            attackerLosses = defenderFleetSize / 4;
            defenderLosses = defenderFleetSize / 2;
        } else if (defenderTotal > attackerTotal) {
            outcome = "DEFENDER_VICTORY";
            attackerLosses = attackerFleetSize / 2;
            defenderLosses = attackerFleetSize / 4;
        } else {
            outcome = "DRAW";
            attackerLosses = attackerFleetSize / 5;
            defenderLosses = defenderFleetSize / 5;
        }

        return Map.of(
                "outcome", outcome,
                "systemName", system.getName(),
                "attackerFaction", attacker.getName(),
                "defenderFaction", defender.getName(),
                "attackerLosses", attackerLosses,
                "defenderLosses", defenderLosses,
                "attackerRoll", attackerTotal,
                "defenderRoll", defenderTotal
        );
    }

    /**
     * Get combat style bonus.
     */
    private int getCombatStyleBonus(String combatStyle) {
        if (combatStyle == null) return 0;
        return switch (combatStyle.toUpperCase()) {
            case "OFFENSIVE" -> 10;
            case "DEFENSIVE" -> 5;
            case "GUERRILLA" -> 8;
            default -> 0;
        };
    }

    /**
     * Calculate distance between systems.
     */
    private int calculateDistance(StarSystem from, StarSystem to) {
        int dx = Math.abs(from.getPosX() - to.getPosX());
        int dy = Math.abs(from.getPosY() - to.getPosY());
        int dz = Math.abs(from.getPosZ() - to.getPosZ());
        return Math.max(dx, Math.max(dy, dz));
    }

    /**
     * Calculate movement time based on distance.
     */
    private int calculateMovementTime(int distance) {
        return Math.max(1, distance / 2);
    }

    /**
     * Get fleet composition based on empire doctrine.
     *
     * @param factionId the faction ID
     * @return fleet composition
     */
    public Map<String, Object> getFleetComposition(Long factionId) {
        StarTrekFaction faction = factionRepository.findById(factionId)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + factionId));

        int totalStrength = faction.getFleetStrength();

        // Base composition on combat style
        String style = faction.getCombatStyle() != null ? faction.getCombatStyle().toUpperCase() : "BALANCED";

        int capitalShips, escorts, fighters;

        switch (style) {
            case "OFFENSIVE":
                capitalShips = (int) (totalStrength * 0.5);
                escorts = (int) (totalStrength * 0.3);
                fighters = totalStrength - capitalShips - escorts;
                break;
            case "DEFENSIVE":
                capitalShips = (int) (totalStrength * 0.3);
                escorts = (int) (totalStrength * 0.4);
                fighters = totalStrength - capitalShips - escorts;
                break;
            case "GUERRILLA":
                capitalShips = (int) (totalStrength * 0.2);
                escorts = (int) (totalStrength * 0.3);
                fighters = totalStrength - capitalShips - escorts;
                break;
            default: // BALANCED
                capitalShips = (int) (totalStrength * 0.4);
                escorts = (int) (totalStrength * 0.35);
                fighters = totalStrength - capitalShips - escorts;
        }

        return Map.of(
                "factionName", faction.getName(),
                "totalStrength", totalStrength,
                "capitalShips", capitalShips,
                "escorts", escorts,
                "fighters", fighters,
                "combatStyle", style
        );
    }
}

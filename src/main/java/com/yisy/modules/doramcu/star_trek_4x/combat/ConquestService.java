package com.yisy.modules.doramcu.star_trek_4x.combat;

import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import com.yisy.modules.doramcu.star_trek_4x.model.StarTrekFaction;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarSystemRepository;
import com.yisy.modules.doramcu.star_trek_4x.repository.StarTrekFactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for capturing enemy systems and sectors.
 * <p>
 * Combines space and ground combat results to determine conquest outcomes.
 * </p>
 */
@Service
public class ConquestService {

    private static final Logger log = LoggerFactory.getLogger(ConquestService.class);

    private final StarSystemRepository starSystemRepository;
    private final StarTrekFactionRepository factionRepository;

    public ConquestService(StarSystemRepository starSystemRepository,
                            StarTrekFactionRepository factionRepository) {
        this.starSystemRepository = starSystemRepository;
        this.factionRepository = factionRepository;
    }

    /**
     * Capture a system after a successful battle.
     *
     * @param systemId        the system ID
     * @param attackerFactionId the attacker faction ID
     * @param battleOutcome   the battle outcome
     * @return conquest result
     */
    public Map<String, Object> captureSystem(Long systemId, Long attackerFactionId, String battleOutcome) {
        log.info("Attempting to capture system {} by faction {} with outcome {}", systemId, attackerFactionId, battleOutcome);

        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));

        StarTrekFaction attacker = factionRepository.findById(attackerFactionId)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + attackerFactionId));

        // Check if battle was won by attacker
        if (!battleOutcome.contains("ATTACKER_VICTORY")) {
            return Map.of(
                    "success", false,
                    "reason", "Battle not won by attacker",
                    "outcome", battleOutcome
            );
        }

        // Transfer system ownership
        Long previousOwner = system.getOwnerFactionId();
        system.setOwnerFactionId(attackerFactionId);
        starSystemRepository.save(system);

        // Update faction sectors
        int sectorId = system.getSectorId();
        if (!attacker.getControlledSectors().contains(sectorId)) {
            attacker.getControlledSectors().add(sectorId);
        }
        factionRepository.save(attacker);

        // Remove from previous owner
        if (previousOwner != null && !previousOwner.equals(attackerFactionId)) {
            factionRepository.findById(previousOwner).ifPresent(previousOwnerFaction -> {
                previousOwnerFaction.getControlledSectors().removeIf(s -> s == sectorId);
                factionRepository.save(previousOwnerFaction);
            });
        }

        return Map.of(
                "success", true,
                "systemName", system.getName(),
                "previousOwner", previousOwner,
                "newOwner", attacker.getName(),
                "sectorId", sectorId,
                "message", system.getName() + " captured by " + attacker.getName()
        );
    }

    /**
     * Surrender a system.
     *
     * @param systemId        the system ID
     * @param defenderFactionId the defender faction ID
     * @return surrender result
     */
    public Map<String, Object> surrenderSystem(Long systemId, Long defenderFactionId) {
        log.info("System {} surrendered by faction {}", systemId, defenderFactionId);

        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));

        StarTrekFaction defender = factionRepository.findById(defenderFactionId)
                .orElseThrow(() -> new IllegalArgumentException("Faction not found: " + defenderFactionId));

        // Remove system ownership
        system.setOwnerFactionId(null);
        starSystemRepository.save(system);

        // Remove from controlled sectors
        int sectorId = system.getSectorId();
        defender.getControlledSectors().removeIf(s -> s == sectorId);
        factionRepository.save(defender);

        return Map.of(
                "success", true,
                "systemName", system.getName(),
                "surrenderedBy", defender.getName(),
                "message", system.getName() + " surrendered by " + defender.getName()
        );
    }

    /**
     * Calculate conquest bonus based on system properties.
     *
     * @param systemId the system ID
     * @return conquest bonus map
     */
    public Map<String, Object> calculateConquestBonus(Long systemId) {
        StarSystem system = starSystemRepository.findById(systemId)
                .orElseThrow(() -> new IllegalArgumentException("System not found: " + systemId));

        int bonus = 0;

        // Warp deposits give resource bonus
        if (system.isHasWarpDeposits()) {
            bonus += 20;
        }

        // Trade value gives economic bonus
        bonus += system.getTradeValue();

        // Alignment affects diplomatic bonus
        if (system.getAlignment() == StarSystem.StarAlignment.FEDERATION) {
            bonus += 10;
        }

        return Map.of(
                "systemName", system.getName(),
                "resourceBonus", system.isHasWarpDeposits() ? 20 : 0,
                "economicBonus", system.getTradeValue(),
                "diplomaticBonus", system.getAlignment() == StarSystem.StarAlignment.FEDERATION ? 10 : 0,
                "totalBonus", bonus
        );
    }
}

package com.yisy.modules.doramcu.star_trek_4x.combat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

/**
 * Service for connecting strategic fleet engagements to SFB tactical combat.
 * <p>
 * Bridges the strategic level (fleet movements) with tactical SFB combat (SfbCombatEngine).
 * </p>
 */
@Service
public class CombatBridgeService {

    private static final Logger log = LoggerFactory.getLogger(CombatBridgeService.class);
    private static final Random random = new Random();

    /**
     * Initiate tactical combat from a strategic engagement.
     *
     * @param attackerFleetSize the attacker fleet size
     * @param defenderFleetSize the defender fleet size
     * @param systemName        the system name
     * @return tactical combat result
     */
    public Map<String, Object> initiateTacticalCombat(int attackerFleetSize, int defenderFleetSize, String systemName) {
        log.info("Initiating tactical combat in {}: attacker={}, defender={}", systemName, attackerFleetSize, defenderFleetSize);

        // Simulate SFB tactical combat
        int attackerRoll = random.nextInt(100);
        int defenderRoll = random.nextInt(100);

        // Fleet size advantage
        int attackerAdvantage = Math.max(0, (attackerFleetSize - defenderFleetSize) / 10);
        int defenderAdvantage = Math.max(0, (defenderFleetSize - attackerFleetSize) / 10);

        int attackerTotal = attackerRoll + attackerAdvantage;
        int defenderTotal = defenderRoll + defenderAdvantage;

        String outcome;
        int attackerCasualties;
        int defenderCasualties;

        if (attackerTotal > defenderTotal + 20) {
            outcome = "DECISIVE_ATTACKER_VICTORY";
            attackerCasualties = (int) (attackerFleetSize * 0.1);
            defenderCasualties = (int) (defenderFleetSize * 0.7);
        } else if (attackerTotal > defenderTotal) {
            outcome = "ATTACKER_VICTORY";
            attackerCasualties = (int) (attackerFleetSize * 0.25);
            defenderCasualties = (int) (defenderFleetSize * 0.5);
        } else if (defenderTotal > attackerTotal + 20) {
            outcome = "DECISIVE_DEFENDER_VICTORY";
            attackerCasualties = (int) (attackerFleetSize * 0.7);
            defenderCasualties = (int) (defenderFleetSize * 0.1);
        } else if (defenderTotal > attackerTotal) {
            outcome = "DEFENDER_VICTORY";
            attackerCasualties = (int) (attackerFleetSize * 0.5);
            defenderCasualties = (int) (defenderFleetSize * 0.25);
        } else {
            outcome = "DRAW";
            attackerCasualties = (int) (attackerFleetSize * 0.3);
            defenderCasualties = (int) (defenderFleetSize * 0.3);
        }

        return Map.of(
                "outcome", outcome,
                "systemName", systemName,
                "attackerRoll", attackerTotal,
                "defenderRoll", defenderTotal,
                "attackerCasualties", attackerCasualties,
                "defenderCasualties", defenderCasualties,
                "attackerRemaining", attackerFleetSize - attackerCasualties,
                "defenderRemaining", defenderFleetSize - defenderCasualties
        );
    }

    /**
     * Calculate combat odds.
     *
     * @param attackerFleetSize the attacker fleet size
     * @param defenderFleetSize the defender fleet size
     * @return combat odds map
     */
    public Map<String, Object> calculateCombatOdds(int attackerFleetSize, int defenderFleetSize) {
        double attackerOdds = (double) attackerFleetSize / (attackerFleetSize + defenderFleetSize);
        double defenderOdds = 1.0 - attackerOdds;

        return Map.of(
                "attackerOdds", Math.round(attackerOdds * 100.0) / 100.0,
                "defenderOdds", Math.round(defenderOdds * 100.0) / 100.0,
                "attackerFleetSize", attackerFleetSize,
                "defenderFleetSize", defenderFleetSize
        );
    }
}

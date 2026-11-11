package com.yisy.modules.doramcu.star_trek_4x.expansion;

import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;

/**
 * Service for constructing defensive starbase installations.
 * <p>
 * Handles starbase construction, defense ratings, and weapon systems.
 * </p>
 */
@Service
public class StarbaseBuilder {

    private static final Logger log = LoggerFactory.getLogger(StarbaseBuilder.class);
    private static final Random random = new Random();

    /**
     * Build a starbase in a system.
     *
     * @param starbaseName the starbase name
     * @param system       the system
     * @param factionId    the owning faction ID
     * @return starbase details
     */
    public Map<String, Object> buildStarbase(String starbaseName, StarSystem system, Long factionId) {
        log.info("Building starbase '{}' in system {}", starbaseName, system.getName());

        int defenseRating = calculateDefenseRating(system);
        int weaponStrength = calculateWeaponStrength();
        int shieldStrength = calculateShieldStrength();

        return Map.of(
                "starbaseName", starbaseName,
                "systemName", system.getName(),
                "systemId", system.getId(),
                "factionId", factionId,
                "defenseRating", defenseRating,
                "weaponStrength", weaponStrength,
                "shieldStrength", shieldStrength,
                "constructionCost", calculateConstructionCost(),
                "constructionTime", calculateConstructionTime()
        );
    }

    /**
     * Calculate defense rating based on system properties.
     */
    private int calculateDefenseRating(StarSystem system) {
        int baseRating = 50;

        // Trade value increases defense
        baseRating += system.getTradeValue();

        // Warp deposits provide strategic advantage
        if (system.isHasWarpDeposits()) {
            baseRating += 20;
        }

        return baseRating + random.nextInt(30);
    }

    /**
     * Calculate weapon strength.
     */
    private int calculateWeaponStrength() {
        return 30 + random.nextInt(40);
    }

    /**
     * Calculate shield strength.
     */
    private int calculateShieldStrength() {
        return 40 + random.nextInt(50);
    }

    /**
     * Calculate construction cost.
     */
    private int calculateConstructionCost() {
        return 100 + random.nextInt(100);
    }

    /**
     * Calculate construction time in turns.
     */
    private int calculateConstructionTime() {
        return 2 + random.nextInt(3);
    }
}

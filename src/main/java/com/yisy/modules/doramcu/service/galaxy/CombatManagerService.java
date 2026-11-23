package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.hex.Fleet;
import com.yisy.modules.doramcu.hex.Planet;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Combat Manager service — delegates to AiAdminCombatService for comprehensive
 * combat management including fleet readiness, threat assessment, battle simulation,
 * and strategic recommendations.
 * <p>
 * This is the domain-specific sub-manager that reports to the Galaxy Overseer.
 * It runs as part of the scheduled health check and performs these functions:
 * </p>
 * <ul>
 *   <li>Assesses fleet combat readiness (hull/shield levels, crew status)</li>
 *   <li>Evaluates threats from hostile fleets</li>
 *   <li>Simulates battles to predict outcomes</li>
 *   <li>Recommends combat strategies (attack, defend, retreat)</li>
 *   <li>Checks defense infrastructure coverage</li>
 *   <li>Creates work orders for combat readiness issues</li>
 * </ul>
 */
@Service
public class CombatManagerService {

    private static final Logger log = LoggerFactory.getLogger(CombatManagerService.class);

    private final GalaxyRepository galaxyRepository;
    private final AiAdminCombatService aiAdminCombatService;

    public CombatManagerService(GalaxyRepository galaxyRepository,
                                 AiAdminCombatService aiAdminCombatService) {
        this.galaxyRepository = galaxyRepository;
        this.aiAdminCombatService = aiAdminCombatService;
    }

    // ------------------------------------------------------------------
    // Combat assessment — delegates to AiAdminCombatService
    // ------------------------------------------------------------------

    /**
     * Assesses the combat readiness of a galaxy and adds findings to the assessment.
     * Delegates to AiAdminCombatService for comprehensive fleet and threat assessment.
     *
     * @param galaxy the galaxy to assess
     * @param assessment the galaxy assessment to add findings to
     * @return the number of work orders created
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public int assessCombatNeeds(Galaxy galaxy, GalaxyOverseerService.GalaxyAssessment assessment) {
        return assessCombatNeeds(galaxy, null, null, assessment);
    }

    /**
     * Assesses the combat readiness of a galaxy with fleet and planet data.
     * Delegates to AiAdminCombatService for comprehensive assessment.
     *
     * @param galaxy the galaxy to assess
     * @param fleets list of fleets in the galaxy (may be null)
     * @param planets list of planets in the galaxy (may be null)
     * @param assessment the galaxy assessment to add findings to
     * @return the number of work orders created
     */
    @Transactional
    public int assessCombatNeeds(Galaxy galaxy, List<Fleet> fleets, List<Planet> planets,
                                   GalaxyOverseerService.GalaxyAssessment assessment) {
        try {
            return aiAdminCombatService.assessCombatNeeds(galaxy, fleets, planets, assessment);
        } catch (Exception e) {
            log.error("Combat assessment failed for galaxy '{}': {}", galaxy.getName(), e.getMessage(), e);
            assessment.addFindingWithAction(
                    GalaxyOverseerService.GalaxyAssessment.Severity.MEDIUM,
                    String.format("Combat assessment failed for galaxy '%s': %s",
                            galaxy.getName(), e.getMessage()),
                    "Investigate combat assessment error."
            );
            return 0;
        }
    }

    /**
     * Gets a combat readiness report for a galaxy by delegating to AiAdminCombatService.
     *
     * @param galaxy the galaxy to report on
     * @param fleets the fleets in the galaxy (may be null)
     * @return the fleet readiness report from AiAdminCombatService
     */
    @Transactional(readOnly = true)
    public AiAdminCombatService.FleetReadinessReport getCombatReadiness(Galaxy galaxy, List<Fleet> fleets) {
        return aiAdminCombatService.assessFleetReadiness(galaxy, fleets);
    }
}

package com.yisy.modules.doramcu.service.galaxy;

import com.yisy.modules.doramcu.admin.service.AiAdminService;
import com.yisy.modules.doramcu.model.galaxy.Galaxy;
import com.yisy.modules.doramcu.repository.galaxy.GalaxyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exploration Manager service — delegates to AiAdminExplorationService for comprehensive
 * galaxy exploration assessment.
 * <p>
 * This is the domain-specific sub-manager that reports to the Galaxy Overseer.
 * All exploration logic has been moved to AiAdminExplorationService, which provides:
 * <ul>
 *   <li>Sector exploration coverage across the 10x10 galaxy grid</li>
 *   <li>Celestial body diversity analysis</li>
 *   <li>System colonization status assessment</li>
 *   <li>Navigation route and hyperlane connectivity mapping</li>
 *   <li>Anomaly detection (orphan systems, edge positions, overdense sectors)</li>
 *   <li>Work order creation for exploration missions</li>
 * </ul>
 * </p>
 */
@Service
public class ExplorationManagerService {

    private static final Logger log = LoggerFactory.getLogger(ExplorationManagerService.class);

    private final GalaxyRepository galaxyRepository;
    private final AiAdminExplorationService aiAdminExplorationService;

    public ExplorationManagerService(GalaxyRepository galaxyRepository,
                                      AiAdminExplorationService aiAdminExplorationService) {
        this.galaxyRepository = galaxyRepository;
        this.aiAdminExplorationService = aiAdminExplorationService;
    }

    // ------------------------------------------------------------------
    // Exploration assessment — delegates to AiAdminExplorationService
    // ------------------------------------------------------------------

    /**
     * Assesses the exploration status of a galaxy and adds findings to the assessment.
     * Delegates to AiAdminExplorationService for comprehensive exploration analysis.
     *
     * @param galaxy the galaxy to assess
     * @param assessment the galaxy assessment to add findings to
     * @return the number of work orders created
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public int assessExplorationNeeds(Galaxy galaxy, GalaxyOverseerService.GalaxyAssessment assessment) {
        return aiAdminExplorationService.assessExplorationNeeds(galaxy, assessment);
    }

    /**
     * Gets an exploration status report for a galaxy.
     * Delegates to AiAdminExplorationService for comprehensive assessment.
     *
     * @param galaxy the galaxy to report on
     * @return the exploration status report
     */
    @Transactional(readOnly = true)
    public AiAdminExplorationService.ExplorationAssessmentReport getExplorationStatus(Galaxy galaxy) {
        return aiAdminExplorationService.assessExploration(galaxy);
    }

    /**
     * Gets a sector coverage report for a galaxy.
     *
     * @param galaxy the galaxy to report on
     * @return the sector coverage report
     */
    @Transactional(readOnly = true)
    public AiAdminExplorationService.SectorCoverageReport getSectorCoverage(Galaxy galaxy) {
        return aiAdminExplorationService.assessSectorCoverage(galaxy);
    }

    /**
     * Gets a celestial diversity report for a galaxy.
     *
     * @param galaxy the galaxy to report on
     * @return the celestial diversity report
     */
    @Transactional(readOnly = true)
    public AiAdminExplorationService.CelestialDiversityReport getCelestialDiversity(Galaxy galaxy) {
        return aiAdminExplorationService.assessCelestialDiversity(galaxy);
    }

    /**
     * Gets a colonization report for a galaxy.
     *
     * @param galaxy the galaxy to report on
     * @return the colonization report
     */
    @Transactional(readOnly = true)
    public AiAdminExplorationService.ColonizationReport getColonizationStatus(Galaxy galaxy) {
        return aiAdminExplorationService.assessColonizationStatus(galaxy);
    }

    /**
     * Gets a navigation analysis report for a galaxy.
     *
     * @param galaxy the galaxy to report on
     * @return the navigation analysis report
     */
    @Transactional(readOnly = true)
    public AiAdminExplorationService.NavigationAnalysisReport getNavigationAnalysis(Galaxy galaxy) {
        return aiAdminExplorationService.assessNavigationRoutes(galaxy);
    }
}

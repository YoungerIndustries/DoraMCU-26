package com.yisy.modules.doramcu.zero_zero_seven.operations;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondAgent;
import com.yisy.modules.doramcu.zero_zero_seven.model.CovertOperation;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondAgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for sabotage missions against enemy infrastructure.
 * <p>
 * Sabotage operations target enemy resources, reducing their economic output
 * and military capabilities. Success depends on agent technical skills and stealth.
 * </p>
 */
@Service
public class SabotageService {

    private final CovertOperationService operationService;
    private final BondAgentRepository agentRepository;

    public SabotageService(CovertOperationService operationService,
                           BondAgentRepository agentRepository) {
        this.operationService = operationService;
        this.agentRepository = agentRepository;
    }

    /**
     * Launch a sabotage mission against an enemy empire.
     *
     * @param name operation name
     * @param ownerEmpireId empire launching the sabotage
     * @param targetEmpireId empire being targeted
     * @param agentId agent assigned to the mission
     * @param difficulty difficulty level (1-10)
     * @return the created covert operation
     */
    @Transactional
    public CovertOperation launchSabotage(String name, Long ownerEmpireId, Long targetEmpireId,
                                          Long agentId, int difficulty) {
        BondAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

        // Sabotage benefits from technical and stealth skills
        int adjustedDifficulty = Math.max(1, difficulty - (agent.getTechnical() + agent.getStealth()) / 4);

        return operationService.launchOperation(
                name,
                CovertOperation.OperationType.SABOTAGE,
                ownerEmpireId,
                targetEmpireId,
                agentId,
                adjustedDifficulty,
                agent.getGameSession().getId()
        );
    }

    /**
     * Calculate sabotage impact on target empire.
     *
     * @param difficulty difficulty of the sabotage
     * @return resource reduction percentage
     */
    public int calculateImpact(int difficulty) {
        return difficulty * 5; // 5% per difficulty level
    }
}

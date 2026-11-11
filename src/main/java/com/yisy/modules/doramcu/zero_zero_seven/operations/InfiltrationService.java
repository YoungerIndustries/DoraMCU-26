package com.yisy.modules.doramcu.zero_zero_seven.operations;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondAgent;
import com.yisy.modules.doramcu.zero_zero_seven.model.CovertOperation;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondAgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for infiltrating enemy organizations.
 * <p>
 * Infiltration operations place agents within enemy organizations to gather
 * long-term intelligence and influence decisions from within.
 * </p>
 */
@Service
public class InfiltrationService {

    private final CovertOperationService operationService;
    private final BondAgentRepository agentRepository;

    public InfiltrationService(CovertOperationService operationService,
                               BondAgentRepository agentRepository) {
        this.operationService = operationService;
        this.agentRepository = agentRepository;
    }

    /**
     * Launch an infiltration mission into an enemy organization.
     *
     * @param name operation name
     * @param ownerEmpireId empire launching the infiltration
     * @param targetEmpireId empire being targeted
     * @param agentId agent assigned to the mission
     * @param difficulty difficulty level (1-10)
     * @return the created covert operation
     */
    @Transactional
    public CovertOperation launchInfiltration(String name, Long ownerEmpireId, Long targetEmpireId,
                                              Long agentId, int difficulty) {
        BondAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

        // Infiltration benefits from charisma and stealth skills
        int adjustedDifficulty = Math.max(1, difficulty - (agent.getCharisma() + agent.getStealth()) / 4);

        return operationService.launchOperation(
                name,
                CovertOperation.OperationType.INFILTRATION,
                ownerEmpireId,
                targetEmpireId,
                agentId,
                adjustedDifficulty,
                agent.getGameSession().getId()
        );
    }

    /**
     * Calculate intelligence gain from a successful infiltration.
     *
     * @param difficulty difficulty of the infiltration
     * @return intelligence points gained
     */
    public int calculateIntelligenceGain(int difficulty) {
        return difficulty * 15; // High intelligence reward
    }
}

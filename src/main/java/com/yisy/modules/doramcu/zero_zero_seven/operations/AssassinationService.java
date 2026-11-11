package com.yisy.modules.doramcu.zero_zero_seven.operations;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondAgent;
import com.yisy.modules.doramcu.zero_zero_seven.model.CovertOperation;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondAgentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for targeted elimination missions.
 * <p>
 * Assassination operations target key personnel in enemy empires,
 * causing diplomatic fallout and strategic advantages. High risk, high reward.
 * </p>
 */
@Service
public class AssassinationService {

    private final CovertOperationService operationService;
    private final BondAgentRepository agentRepository;

    public AssassinationService(CovertOperationService operationService,
                                 BondAgentRepository agentRepository) {
        this.operationService = operationService;
        this.agentRepository = agentRepository;
    }

    /**
     * Launch an assassination mission against a target in an enemy empire.
     *
     * @param name operation name
     * @param ownerEmpireId empire launching the assassination
     * @param targetEmpireId empire being targeted
     * @param agentId agent assigned to the mission
     * @param difficulty difficulty level (1-10)
     * @return the created covert operation
     */
    @Transactional
    public CovertOperation launchAssassination(String name, Long ownerEmpireId, Long targetEmpireId,
                                               Long agentId, int difficulty) {
        BondAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

        // Assassination benefits from stealth and combat skills
        int adjustedDifficulty = Math.max(1, difficulty - (agent.getStealth() + agent.getCombat()) / 4);

        return operationService.launchOperation(
                name,
                CovertOperation.OperationType.ASSASSINATION,
                ownerEmpireId,
                targetEmpireId,
                agentId,
                adjustedDifficulty,
                agent.getGameSession().getId()
        );
    }

    /**
     * Calculate diplomatic impact of a successful assassination.
     *
     * @param difficulty difficulty of the assassination
     * @return reputation change
     */
    public int calculateDiplomaticImpact(int difficulty) {
        return -difficulty * 10; // Significant diplomatic penalty
    }
}

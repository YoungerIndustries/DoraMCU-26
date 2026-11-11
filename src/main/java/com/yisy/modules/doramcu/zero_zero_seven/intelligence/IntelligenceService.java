package com.yisy.modules.doramcu.zero_zero_seven.intelligence;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondAgent;
import com.yisy.modules.doramcu.zero_zero_seven.model.CovertOperation;
import com.yisy.modules.doramcu.zero_zero_seven.model.SpyNetwork;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondAgentRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.SpyNetworkRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for gathering and analyzing intelligence.
 * <p>
 * Coordinates spy networks and agents to collect intelligence on enemy empires,
 * providing strategic advantages through information superiority.
 * </p>
 */
@Service
public class IntelligenceService {

    private final SpyNetworkRepository spyNetworkRepository;
    private final BondAgentRepository agentRepository;

    public IntelligenceService(SpyNetworkRepository spyNetworkRepository,
                               BondAgentRepository agentRepository) {
        this.spyNetworkRepository = spyNetworkRepository;
        this.agentRepository = agentRepository;
    }

    /**
     * Gather intelligence from all spy networks for an empire.
     *
     * @param ownerEmpireId the empire gathering intelligence
     * @return total intelligence points gathered
     */
    public int gatherIntelligence(Long ownerEmpireId) {
        List<SpyNetwork> networks = spyNetworkRepository.findByOwnerEmpireIdAndActive(ownerEmpireId, true);
        int totalIntelligence = 0;

        for (SpyNetwork network : networks) {
            totalIntelligence += network.getIntelligenceGathered() * network.getLevel();
        }

        return totalIntelligence;
    }

    /**
     * Launch an intelligence gathering operation.
     *
     * @param name operation name
     * @param ownerEmpireId empire launching the operation
     * @param targetEmpireId empire being targeted
     * @param agentId agent assigned to the mission
     * @param difficulty difficulty level (1-10)
     * @return the created covert operation
     */
    public CovertOperation launchIntelGathering(String name, Long ownerEmpireId, Long targetEmpireId,
                                                Long agentId, int difficulty) {
        BondAgent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + agentId));

        // Intel gathering benefits from technical and stealth skills
        int adjustedDifficulty = Math.max(1, difficulty - (agent.getTechnical() + agent.getStealth()) / 4);

        CovertOperation operation = new CovertOperation(
                name,
                CovertOperation.OperationType.INTEL_GATHERING,
                ownerEmpireId,
                targetEmpireId,
                agentId,
                agent.getGameSession()
        );
        operation.setDifficulty(adjustedDifficulty);
        operation.setReward(adjustedDifficulty * 15);
        operation.setTurnsRemaining(adjustedDifficulty);
        operation.setStatus(CovertOperation.OperationStatus.IN_PROGRESS);

        return operation;
    }

    /**
     * Calculate intelligence bonus from spy networks.
     *
     * @param ownerEmpireId the empire
     * @return intelligence bonus percentage
     */
    public int getIntelligenceBonus(Long ownerEmpireId) {
        List<SpyNetwork> networks = spyNetworkRepository.findByOwnerEmpireIdAndActive(ownerEmpireId, true);
        return networks.size() * 5; // 5% per active network
    }
}

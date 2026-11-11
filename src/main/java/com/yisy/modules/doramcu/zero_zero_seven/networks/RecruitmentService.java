package com.yisy.modules.doramcu.zero_zero_seven.networks;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondAgent;
import com.yisy.modules.doramcu.zero_zero_seven.model.BondGameSession;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondAgentRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondGameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

/**
 * Service for recruiting new agents.
 * <p>
 * Handles agent recruitment with randomized skills based on empire attributes
 * and available intelligence budget.
 * </p>
 */
@Service
public class RecruitmentService {

    private final BondAgentRepository agentRepository;
    private final BondGameSessionRepository gameSessionRepository;
    private final Random random;

    public RecruitmentService(BondAgentRepository agentRepository,
                              BondGameSessionRepository gameSessionRepository) {
        this.agentRepository = agentRepository;
        this.gameSessionRepository = gameSessionRepository;
        this.random = new Random();
    }

    /**
     * Recruit a new agent for an empire.
     *
     * @param name agent name
     * @param codeName agent code name
     * @param ownerEmpireId empire recruiting the agent
     * @param gameSessionId game session
     * @return the recruited agent
     */
    @Transactional
    public BondAgent recruitAgent(String name, String codeName, Long ownerEmpireId,
                                   Long gameSessionId) {
        BondGameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameSessionId));

        BondAgent agent = new BondAgent(name, codeName, session);
        agent.setOwnerEmpireId(ownerEmpireId);
        agent.setStealth(random.nextInt(6) + 3); // 3-8
        agent.setCharisma(random.nextInt(6) + 3);
        agent.setCombat(random.nextInt(6) + 3);
        agent.setTechnical(random.nextInt(6) + 3);

        return agentRepository.save(agent);
    }

    /**
     * Recruit an agent with specified skills.
     *
     * @param name agent name
     * @param codeName agent code name
     * @param ownerEmpireId empire recruiting the agent
     * @param gameSessionId game session
     * @param stealth stealth skill (1-10)
     * @param charisma charisma skill (1-10)
     * @param combat combat skill (1-10)
     * @param technical technical skill (1-10)
     * @return the recruited agent
     */
    @Transactional
    public BondAgent recruitAgentWithSkills(String name, String codeName, Long ownerEmpireId,
                                             Long gameSessionId, int stealth, int charisma,
                                             int combat, int technical) {
        BondGameSession session = gameSessionRepository.findById(gameSessionId)
                .orElseThrow(() -> new IllegalArgumentException("Game session not found: " + gameSessionId));

        BondAgent agent = new BondAgent(name, codeName, session);
        agent.setOwnerEmpireId(ownerEmpireId);
        agent.setStealth(stealth);
        agent.setCharisma(charisma);
        agent.setCombat(combat);
        agent.setTechnical(technical);

        return agentRepository.save(agent);
    }

    /**
     * Get all agents for an empire.
     *
     * @param ownerEmpireId the empire
     * @return list of agents
     */
    public List<BondAgent> getAgentsByEmpire(Long ownerEmpireId) {
        return agentRepository.findByOwnerEmpireId(ownerEmpireId);
    }

    /**
     * Get available agents for an empire.
     *
     * @param ownerEmpireId the empire
     * @return list of available agents
     */
    public List<BondAgent> getAvailableAgents(Long ownerEmpireId) {
        return agentRepository.findByOwnerEmpireIdAndStatus(ownerEmpireId, BondAgent.AgentStatus.ACTIVE);
    }
}

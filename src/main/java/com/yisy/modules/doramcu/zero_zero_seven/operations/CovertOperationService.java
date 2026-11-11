package com.yisy.modules.doramcu.zero_zero_seven.operations;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondAgent;
import com.yisy.modules.doramcu.zero_zero_seven.model.CovertOperation;
import com.yisy.modules.doramcu.zero_zero_seven.repository.BondAgentRepository;
import com.yisy.modules.doramcu.zero_zero_seven.repository.CovertOperationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * Core service for managing covert operations.
 * <p>
 * Handles launching, advancing, completing, and failing operations.
 * Coordinates with agents and calculates success probabilities.
 * </p>
 */
@Service
public class CovertOperationService {

    private final CovertOperationRepository operationRepository;
    private final BondAgentRepository agentRepository;
    private final Random random;

    public CovertOperationService(CovertOperationRepository operationRepository,
                                   BondAgentRepository agentRepository) {
        this.operationRepository = operationRepository;
        this.agentRepository = agentRepository;
        this.random = new Random();
    }

    /**
     * Launch a new covert operation.
     */
    @Transactional
    public CovertOperation launchOperation(String name, CovertOperation.OperationType type,
                                           Long ownerEmpireId, Long targetEmpireId,
                                           Long assignedAgentId, int difficulty,
                                           Long gameSessionId) {
        BondAgent agent = agentRepository.findById(assignedAgentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found: " + assignedAgentId));

        if (!agent.isAvailable()) {
            throw new IllegalStateException("Agent is not available for assignment");
        }

        CovertOperation operation = new CovertOperation(
                name, type, ownerEmpireId, targetEmpireId, assignedAgentId,
                agent.getGameSession()
        );
        operation.setDifficulty(difficulty);
        operation.setReward(difficulty * 10);
        operation.setTurnsRemaining(difficulty);
        operation.setStatus(CovertOperation.OperationStatus.IN_PROGRESS);
        operation.setSuccessProbability(calculateSuccessProbability(agent, difficulty));

        // Update agent status
        agent.setStatus(BondAgent.AgentStatus.ON_MISSION);
        agent.setCurrentOperationId(null); // will be set after save
        agentRepository.save(agent);

        CovertOperation saved = operationRepository.save(operation);
        agent.setCurrentOperationId(saved.getId());
        agentRepository.save(agent);

        return saved;
    }

    /**
     * Advance all in-progress operations by one turn.
     */
    @Transactional
    public List<CovertOperation> advanceAllOperations(Long gameSessionId) {
        List<CovertOperation> operations = operationRepository.findByGameSessionId(gameSessionId);
        List<CovertOperation> completed = new java.util.ArrayList<>();

        for (CovertOperation op : operations) {
            if (op.getStatus() == CovertOperation.OperationStatus.IN_PROGRESS) {
                op.advanceTurn();
                if (op.getTurnsRemaining() <= 0) {
                    resolveOperation(op);
                    completed.add(op);
                }
                operationRepository.save(op);
            }
        }

        return completed;
    }

    /**
     * Resolve an operation that has completed its turns.
     */
    @Transactional
    public CovertOperation resolveOperation(CovertOperation operation) {
        int roll = random.nextInt(100);
        BondAgent agent = agentRepository.findById(operation.getAssignedAgentId())
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));

        if (roll < operation.getSuccessProbability()) {
            // Success
            operation.setStatus(CovertOperation.OperationStatus.COMPLETED);
            operation.setCompletedAt(LocalDateTime.now());
            agent.setStatus(BondAgent.AgentStatus.ACTIVE);
            agent.setCurrentOperationId(null);
            agent.setMissionsCompleted(agent.getMissionsCompleted() + 1);
        } else if (roll < operation.getSuccessProbability() + 20) {
            // Detected
            operation.setStatus(CovertOperation.OperationStatus.DETECTED);
            operation.setCompletedAt(LocalDateTime.now());
            agent.setStatus(BondAgent.AgentStatus.CAPTURED);
            agent.setCurrentOperationId(null);
        } else {
            // Failed
            operation.setStatus(CovertOperation.OperationStatus.FAILED);
            operation.setCompletedAt(LocalDateTime.now());
            agent.setStatus(BondAgent.AgentStatus.ACTIVE);
            agent.setCurrentOperationId(null);
        }

        operationRepository.save(operation);
        agentRepository.save(agent);

        return operation;
    }

    /**
     * Calculate success probability based on agent skills and difficulty.
     */
    private int calculateSuccessProbability(BondAgent agent, int difficulty) {
        int baseProbability = 70;
        int skillBonus = agent.getTotalSkills() / 4; // Average skill
        int difficultyPenalty = difficulty * 5;

        // Adjust based on operation type
        switch (agent.getMode()) {
            case COVERT:
                skillBonus += agent.getStealth();
                break;
            case OVERT:
                skillBonus += agent.getCombat();
                break;
        }

        int probability = baseProbability + skillBonus - difficultyPenalty;
        return Math.max(10, Math.min(95, probability));
    }

    /**
     * Get all operations for an empire.
     */
    public List<CovertOperation> getOperationsByEmpire(Long ownerEmpireId) {
        return operationRepository.findByOwnerEmpireId(ownerEmpireId);
    }

    /**
     * Get active operations for an empire.
     */
    public List<CovertOperation> getActiveOperations(Long ownerEmpireId) {
        return operationRepository.findByOwnerEmpireIdAndStatus(ownerEmpireId,
                CovertOperation.OperationStatus.IN_PROGRESS);
    }

    /**
     * Get all operations for a game session.
     */
    public List<CovertOperation> getAllOperations(Long gameSessionId) {
        return operationRepository.findByGameSessionId(gameSessionId);
    }
}

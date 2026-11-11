package com.yisy.modules.doramcu.zero_zero_seven.repository;

import com.yisy.modules.doramcu.zero_zero_seven.model.BondAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BondAgentRepository extends JpaRepository<BondAgent, Long> {
    List<BondAgent> findByGameSessionId(Long gameSessionId);
    List<BondAgent> findByOwnerEmpireId(Long ownerEmpireId);
    List<BondAgent> findByOwnerEmpireIdAndStatus(Long ownerEmpireId, BondAgent.AgentStatus status);
    List<BondAgent> findByOwnerEmpireIdAndMode(Long ownerEmpireId, BondAgent.AgentMode mode);
}

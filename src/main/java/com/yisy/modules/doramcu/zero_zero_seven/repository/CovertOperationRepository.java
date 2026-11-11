package com.yisy.modules.doramcu.zero_zero_seven.repository;

import com.yisy.modules.doramcu.zero_zero_seven.model.CovertOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CovertOperationRepository extends JpaRepository<CovertOperation, Long> {
    List<CovertOperation> findByGameSessionId(Long gameSessionId);
    List<CovertOperation> findByOwnerEmpireId(Long ownerEmpireId);
    List<CovertOperation> findByAssignedAgentId(Long assignedAgentId);
    List<CovertOperation> findByStatus(CovertOperation.OperationStatus status);
    List<CovertOperation> findByOwnerEmpireIdAndStatus(Long ownerEmpireId, CovertOperation.OperationStatus status);
}

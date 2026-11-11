package com.yisy.modules.doramcu.repository;

import com.yisy.modules.doramcu.fourx.FourxSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FourxSessionRepository extends JpaRepository<FourxSessionEntity, Long> {

    Optional<FourxSessionEntity> findBySessionId(String sessionId);

    List<FourxSessionEntity> findByStatus(String status);

    List<FourxSessionEntity> findByCreatedByPlayerId(Long playerId);
}

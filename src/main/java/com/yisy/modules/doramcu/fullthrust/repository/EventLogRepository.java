package com.yisy.modules.doramcu.fullthrust.repository;

import com.yisy.modules.doramcu.fullthrust.entity.EventLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventLogRepository extends JpaRepository<EventLogEntity, Long> {
    List<EventLogEntity> findByGameSessionIdOrderByTurnNumberAsc(String sessionId);
    void deleteByGameSessionId(String sessionId);
}

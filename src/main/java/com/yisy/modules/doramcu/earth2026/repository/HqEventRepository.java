package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.HqEvent;
import com.yisy.modules.doramcu.earth2026.model.HqEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for HQ events.
 */
@Repository
public interface HqEventRepository extends JpaRepository<HqEvent, Long> {

    List<HqEvent> findByHqIdAndStatus(Long hqId, String status);

    List<HqEvent> findByHqIdAndEventType(Long hqId, HqEventType eventType);

    List<HqEvent> findByHqIdAndStatusAndEndTimeAfter(Long hqId, String status, LocalDateTime after);

    @Query("SELECT e FROM HqEvent e WHERE e.hqId = :hqId AND e.status = 'ACTIVE' AND e.endTime > :now")
    List<HqEvent> findActiveEvents(@Param("hqId") Long hqId, @Param("now") LocalDateTime now);

    @Query("SELECT e FROM HqEvent e WHERE e.hqId = :hqId AND e.status = 'SCHEDULED' AND e.startTime <= :now")
    List<HqEvent> findEventsToActivate(@Param("hqId") Long hqId, @Param("now") LocalDateTime now);

    @Query("SELECT e FROM HqEvent e WHERE e.hqId = :hqId AND e.status = 'ACTIVE' AND e.endTime <= :now")
    List<HqEvent> findExpiredEvents(@Param("hqId") Long hqId, @Param("now") LocalDateTime now);

    @Query("SELECT e FROM HqEvent e WHERE e.hqId = :hqId AND e.status = 'COMPLETED' ORDER BY e.endTime DESC")
    List<HqEvent> findCompletedEventsDesc(@Param("hqId") Long hqId);

    @Query("SELECT e FROM HqEvent e WHERE e.hqId = :hqId AND e.eventType = :type AND e.status IN ('ACTIVE', 'SCHEDULED')")
    List<HqEvent> findActiveOrScheduledByType(@Param("hqId") Long hqId, @Param("type") HqEventType type);

    @Query("SELECT e FROM HqEvent e WHERE e.hqId = :hqId AND e.status = 'COMPLETED' AND e.createdAt < :before")
    List<HqEvent> findOldCompletedEvents(@Param("hqId") Long hqId, @Param("before") LocalDateTime before);
}

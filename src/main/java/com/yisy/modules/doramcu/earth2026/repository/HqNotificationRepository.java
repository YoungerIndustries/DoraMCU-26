package com.yisy.modules.doramcu.earth2026.repository;

import com.yisy.modules.doramcu.earth2026.model.HqNotification;
import com.yisy.modules.doramcu.earth2026.model.HqNotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HqNotificationRepository extends JpaRepository<HqNotification, Long> {
    List<HqNotification> findByCorporationIdOrderByCreatedAtDesc(Long corporationId);
    List<HqNotification> findByCorporationIdAndIsReadFalseOrderByCreatedAtDesc(Long corporationId);
    List<HqNotification> findByCorporationIdAndNotificationType(Long corporationId, HqNotificationType type);
    long countByCorporationIdAndIsReadFalse(Long corporationId);
    List<HqNotification> findByExpiresAtBefore(LocalDateTime now);
    void deleteByExpiresAtBefore(LocalDateTime now);
}

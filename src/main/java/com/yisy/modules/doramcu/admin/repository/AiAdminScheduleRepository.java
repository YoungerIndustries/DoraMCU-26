package com.yisy.modules.doramcu.admin.repository;

import com.yisy.modules.doramcu.admin.model.AiAdminSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface AiAdminScheduleRepository extends JpaRepository<AiAdminSchedule, Long> {

    /**
     * Find all active schedules for an AI admin.
     */
    List<AiAdminSchedule> findByAiAdminIdAndIsActiveTrue(Long aiAdminId);

    /**
     * Find all active schedules.
     */
    List<AiAdminSchedule> findByIsActiveTrue();

    /**
     * Find active schedules for a specific galaxy.
     */
    List<AiAdminSchedule> findByGalaxyNameAndIsActiveTrue(String galaxyName);

    /**
     * Find active schedules by shift type.
     */
    List<AiAdminSchedule> findByShiftTypeAndIsActiveTrue(AiAdminSchedule.ShiftType shiftType);

    /**
     * Find schedules for an admin within a date range (for calendar view).
     */
    List<AiAdminSchedule> findByAiAdminIdAndCreatedAtBetween(Long aiAdminId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    /**
     * Find admins currently on shift for a galaxy at the current time.
     * This query finds active schedules where the current time falls between start and end time.
     */
    @Query("SELECT s FROM AiAdminSchedule s WHERE s.isActive = true AND " +
            "s.galaxyName = :galaxyName AND " +
            "s.startTime <= :currentTime AND s.endTime >= :currentTime")
    List<AiAdminSchedule> findActiveShiftsForGalaxyAtTime(
            @Param("galaxyName") String galaxyName,
            @Param("galaxyName") LocalTime currentTime);

    /**
     * Find all admins currently on any shift at the current time.
     */
    @Query("SELECT s FROM AiAdminSchedule s WHERE s.isActive = true AND " +
            "s.startTime <= :currentTime AND s.endTime >= :currentTime")
    List<AiAdminSchedule> findActiveShiftsAtTime(@Param("galaxyName") LocalTime currentTime);

    /**
     * Find ON_CALL active schedules (always available as backup).
     */
    List<AiAdminSchedule> findByShiftTypeAndIsActiveTrueAndGalaxyName(
            AiAdminSchedule.ShiftType shiftType, String galaxyName);

    /**
     * Delete inactive schedules.
     */
    void deleteByIsActiveFalse();
}

package com.yisy.modules.doramcu.admin.service;

import com.yisy.modules.doramcu.admin.model.AiAdminSchedule;
import com.yisy.modules.doramcu.admin.repository.AiAdminScheduleRepository;
import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.ai.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing AI admin shift schedules.
 * Handles schedule CRUD, shift detection, calendar view, and cleanup.
 */
@Service
public class AiAdminScheduleService {

    private static final Logger log = LoggerFactory.getLogger(AiAdminScheduleService.class);

    private final AiAdminScheduleRepository scheduleRepository;
    private final AiCharacterRepository characterRepository;

    public AiAdminScheduleService(AiAdminScheduleRepository scheduleRepository,
                                  AiCharacterRepository characterRepository) {
        this.scheduleRepository = scheduleRepository;
        this.characterRepository = characterRepository;
    }

    /**
     * Create a new schedule for an AI admin.
     */
    public AiAdminSchedule createSchedule(Long aiAdminId, AiAdminSchedule.ShiftType shiftType,
                                          String galaxyName, LocalTime startTime, LocalTime endTime,
                                          DayOfWeek dayOfWeek) {
        // Verify the admin exists and is an AI admin
        Optional<AiCharacter> admin = characterRepository.findById(aiAdminId);
        if (admin.isEmpty() || !Boolean.TRUE.equals(admin.get().isAiAdmin())) {
            throw new IllegalArgumentException("AI admin with id " + aiAdminId + " not found");
        }

        AiAdminSchedule schedule = new AiAdminSchedule();
        schedule.setAiAdminId(aiAdminId);
        schedule.setShiftType(shiftType);
        schedule.setGalaxyName(galaxyName);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setIsActive(true);
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUpdatedAt(LocalDateTime.now());

        return scheduleRepository.save(schedule);
    }

    /**
     * Update an existing schedule.
     */
    public AiAdminSchedule updateSchedule(Long scheduleId, AiAdminSchedule.ShiftType shiftType,
                                          String galaxyName, LocalTime startTime, LocalTime endTime,
                                          DayOfWeek dayOfWeek, String notes) {
        AiAdminSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule with id " + scheduleId + " not found"));

        if (shiftType != null) schedule.setShiftType(shiftType);
        if (galaxyName != null) schedule.setGalaxyName(galaxyName);
        if (startTime != null) schedule.setStartTime(startTime);
        if (endTime != null) schedule.setEndTime(endTime);
        if (dayOfWeek != null) schedule.setDayOfWeek(dayOfWeek);
        if (notes != null) schedule.setNotes(notes);
        schedule.setUpdatedAt(LocalDateTime.now());

        return scheduleRepository.save(schedule);
    }

    /**
     * Deactivate a schedule (soft delete).
     */
    public void deleteSchedule(Long scheduleId) {
        AiAdminSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule with id " + scheduleId + " not found"));
        schedule.setIsActive(false);
        schedule.setUpdatedAt(LocalDateTime.now());
        scheduleRepository.save(schedule);
    }

    /**
     * Get all active schedules for an AI admin.
     */
    public List<AiAdminSchedule> getActiveSchedules(Long aiAdminId) {
        return scheduleRepository.findByAiAdminIdAndIsActiveTrue(aiAdminId);
    }

    /**
     * Get all active schedules.
     */
    public List<AiAdminSchedule> getAllActiveSchedules() {
        return scheduleRepository.findByIsActiveTrue();
    }

    /**
     * Get admins currently on shift for a specific galaxy.
     * Includes RECURRING shifts matching today's day of week, ONCE shifts, and ON_CALL shifts.
     */
    public List<Long> getAdminsOnShift(String galaxyName) {
        LocalTime now = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        Set<Long> adminIds = new HashSet<>();

        // Find time-based active shifts for the galaxy
        List<AiAdminSchedule> activeShifts = scheduleRepository.findActiveShiftsForGalaxyAtTime(galaxyName, now);
        for (AiAdminSchedule schedule : activeShifts) {
            // For RECURRING shifts, check if it matches today's day of week
            if (schedule.getShiftType() == AiAdminSchedule.ShiftType.RECURRING) {
                if (schedule.getDayOfWeek() == null || schedule.getDayOfWeek() == today) {
                    adminIds.add(schedule.getAiAdminId());
                }
            } else {
                // ONCE and ON_CALL shifts don't need day of week check
                adminIds.add(schedule.getAiAdminId());
            }
        }

        // Also include ON_CALL shifts for the galaxy
        List<AiAdminSchedule> onCallShifts = scheduleRepository
                .findByShiftTypeAndIsActiveTrueAndGalaxyName(AiAdminSchedule.ShiftType.ON_CALL, galaxyName);
        for (AiAdminSchedule schedule : onCallShifts) {
            adminIds.add(schedule.getAiAdminId());
        }

        return new ArrayList<>(adminIds);
    }

    /**
     * Get all admins currently on any shift.
     */
    public List<Long> getAllAdminsOnShift() {
        LocalTime now = LocalTime.now();
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        Set<Long> adminIds = new HashSet<>();
        List<AiAdminSchedule> activeShifts = scheduleRepository.findActiveShiftsAtTime(now);
        for (AiAdminSchedule schedule : activeShifts) {
            if (schedule.getShiftType() == AiAdminSchedule.ShiftType.RECURRING) {
                if (schedule.getDayOfWeek() == null || schedule.getDayOfWeek() == today) {
                    adminIds.add(schedule.getAiAdminId());
                }
            } else {
                adminIds.add(schedule.getAiAdminId());
            }
        }

        // Include all ON_CALL shifts
        List<AiAdminSchedule> onCallShifts = scheduleRepository
                .findByShiftTypeAndIsActiveTrue(AiAdminSchedule.ShiftType.ON_CALL);
        for (AiAdminSchedule schedule : onCallShifts) {
            adminIds.add(schedule.getAiAdminId());
        }

        return new ArrayList<>(adminIds);
    }

    /**
     * Check if an admin is currently on shift for a specific galaxy.
     */
    public boolean isOnShift(Long aiAdminId, String galaxyName) {
        List<Long> onShiftAdmins = getAdminsOnShift(galaxyName);
        return onShiftAdmins.contains(aiAdminId);
    }

    /**
     * Check if an admin is currently on any shift.
     */
    public boolean isOnAnyShift(Long aiAdminId) {
        List<Long> onShiftAdmins = getAllAdminsOnShift();
        return onShiftAdmins.contains(aiAdminId);
    }

    /**
     * Get calendar view of schedules for an admin within a date range.
     */
    public List<AiAdminSchedule> getScheduleCalendar(Long aiAdminId, LocalDateTime start, LocalDateTime end) {
        return scheduleRepository.findByAiAdminIdAndCreatedAtBetween(aiAdminId, start, end);
    }

    /**
     * Get schedule details with admin name.
     */
    public Map<String, Object> getScheduleWithAdmin(Long scheduleId) {
        AiAdminSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule with id " + scheduleId + " not found"));

        Optional<AiCharacter> admin = characterRepository.findById(schedule.getAiAdminId());
        Map<String, Object> result = new HashMap<>();
        result.put("schedule", schedule);
        result.put("adminName", admin.map(AiCharacter::getName).orElse("Unknown"));
        result.put("adminRole", admin.map(AiCharacter::getRole).orElse("Unknown"));
        result.put("adminEmpire", admin.map(AiCharacter::getEmpireName).orElse("Unknown"));
        return result;
    }

    /**
     * Scheduled task to clean up expired ONCE shifts.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredSchedules() {
        LocalDateTime now = LocalDateTime.now();
        List<AiAdminSchedule> activeSchedules = scheduleRepository.findByIsActiveTrue();
        int cleaned = 0;

        for (AiAdminSchedule schedule : activeSchedules) {
            if (schedule.getShiftType() == AiAdminSchedule.ShiftType.ONCE) {
                // For ONCE shifts, check if the end time has passed
                // Since we only store time, we check if the schedule was created more than a day ago
                // and the end time is before the current time
                if (schedule.getCreatedAt().plusDays(1).isBefore(now)) {
                    schedule.setIsActive(false);
                    schedule.setUpdatedAt(now);
                    scheduleRepository.save(schedule);
                    cleaned++;
                }
            }
        }

        if (cleaned > 0) {
            log.info("Cleaned up {} expired ONCE schedules", cleaned);
        }
    }
}

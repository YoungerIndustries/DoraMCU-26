package com.yisy.modules.doramcu.earth2026.tycoon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.corporation.service.CorporationEconomyService;
import com.yisy.modules.doramcu.corporation.service.CorporationPersistenceService;
import com.yisy.modules.doramcu.eve.model.EveCorporation;
import com.yisy.modules.doramcu.repository.misc.EveCorporationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service that manages the Corporation Tycoon game mode.
 * <p>
 * Players build and upgrade buildings, manage resources, hire employees,
 * and complete FPS missions to earn credits and grow their corporation.
 * </p>
 */
@Service
public class CorporationTycoonService {

    private static final Logger log = LoggerFactory.getLogger(CorporationTycoonService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** Resources generated per tick (1 tick = 1 minute of game time) */
    private static final double TICKS_PER_HOUR = 60.0;

    /** Default starting credits for a new corporation */
    private static final long STARTING_CREDITS = 10000;

    /** FPS mission credit rewards by difficulty */
    private static final Map<Integer, Long> MISSION_REWARDS = new HashMap<>();

    static {
        MISSION_REWARDS.put(1, 500L);
        MISSION_REWARDS.put(2, 1000L);
        MISSION_REWARDS.put(3, 2000L);
        MISSION_REWARDS.put(4, 4000L);
        MISSION_REWARDS.put(5, 8000L);
        MISSION_REWARDS.put(6, 15000L);
        MISSION_REWARDS.put(7, 25000L);
        MISSION_REWARDS.put(8, 50000L);
        MISSION_REWARDS.put(9, 100000L);
        MISSION_REWARDS.put(10, 200000L);
    }

    private final EveCorporationRepository corporationRepository;
    private final CorporationEconomyService economyService;
    private final CorporationPersistenceService persistenceService;

    // In-memory game state (persisted to database on save)
    private final ConcurrentHashMap<Long, TycoonGameState> gameStates = new ConcurrentHashMap<>();

    public CorporationTycoonService(
            EveCorporationRepository corporationRepository,
            CorporationEconomyService economyService,
            CorporationPersistenceService persistenceService) {
        this.corporationRepository = corporationRepository;
        this.economyService = economyService;
        this.persistenceService = persistenceService;
    }

    /**
     * Get or create the tycoon game state for a corporation.
     */
    public TycoonGameState getGameState(Long corporationId) {
        return gameStates.computeIfAbsent(corporationId, this::createNewGameState);
    }

    private TycoonGameState createNewGameState(Long corporationId) {
        TycoonGameState state = new TycoonGameState();
        state.corporationId = corporationId;
        state.credits = STARTING_CREDITS;
        state.influence = 0;
        state.tech = 0;
        state.materials = 0;
        state.reputation = 50;
        state.level = 1;
        state.experience = 0;
        state.experienceToNextLevel = 1000;
        state.missionsCompleted = 0;
        state.buildings = new ArrayList<>();
        state.employees = new ArrayList<>();
        state.researchProjects = new ArrayList<>();
        state.lastTick = LocalDateTime.now();
        state.createdAt = LocalDateTime.now();

        // Initialize with a basic Headquarters
        TycoonBuilding headquarters = new TycoonBuilding(TycoonBuilding.BuildingType.HEADQUARTERS);
        headquarters.construct();
        state.buildings.add(headquarters);

        log.info("Created new tycoon game state for corporation {}", corporationId);
        return state;
    }

    /**
     * Process a game tick — generate resources based on buildings and employees.
     */
    public void tick(Long corporationId) {
        TycoonGameState state = getGameState(corporationId);
        LocalDateTime now = LocalDateTime.now();
        long minutesElapsed = java.time.Duration.between(state.lastTick, now).toMinutes();

        if (minutesElapsed < 1) {
            return; // Not enough time elapsed
        }

        // Generate resources based on buildings
        for (TycoonBuilding building : state.buildings) {
            if (building.isConstructed()) {
                double factor = minutesElapsed / TICKS_PER_HOUR;
                state.credits += (long) (building.getCreditsPerHour() * factor);
                state.influence += (long) (building.getInfluencePerHour() * factor);
                state.tech += (long) (building.getTechPerHour() * factor);
                state.materials += (long) (building.getMaterialsPerHour() * factor);
            }
        }

        // Apply employee bonuses
        for (TycoonEmployee employee : state.employees) {
            if (employee.isActive()) {
                double bonusFactor = employee.getEfficiency() / 100.0;
                state.credits += (long) (employee.getCreditBonus() * minutesElapsed * bonusFactor);
                state.influence += (long) (employee.getInfluenceBonus() * minutesElapsed * bonusFactor);
            }
        }

        state.lastTick = now;

        // Sync credits to corporation economy
        EveCorporation corp = corporationRepository.findById(corporationId).orElse(null);
        if (corp != null) {
            corp.setCreditBalance(state.credits);
            corporationRepository.save(corp);
        }
    }

    /**
     * Construct a new building for the corporation.
     */
    public boolean constructBuilding(Long corporationId, TycoonBuilding.BuildingType type) {
        TycoonGameState state = getGameState(corporationId);
        long cost = getConstructionCost(type);

        if (state.credits < cost) {
            log.warn("Not enough credits to construct {} for corporation {}", type, corporationId);
            return false;
        }

        // Check if already have this building type
        boolean hasBuilding = state.buildings.stream()
                .anyMatch(b -> b.getType() == type && b.isConstructed());
        if (hasBuilding) {
            log.warn("Corporation {} already has a {}", corporationId, type);
            return false;
        }

        // Deduct cost and construct
        state.credits -= cost;
        TycoonBuilding building = new TycoonBuilding(type);
        building.construct();
        state.buildings.add(building);

        // Gain experience
        gainExperience(corporationId, 100);

        log.info("Constructed {} for corporation {}", type, corporationId);
        return true;
    }

    /**
     * Upgrade an existing building.
     */
    public boolean upgradeBuilding(Long corporationId, TycoonBuilding.BuildingType type) {
        TycoonGameState state = getGameState(corporationId);
        TycoonBuilding building = state.buildings.stream()
                .filter(b -> b.getType() == type && b.isConstructed())
                .findFirst()
                .orElse(null);

        if (building == null) {
            return false;
        }

        long cost = building.getUpgradeCost();
        if (cost < 0) {
            return false; // Max level
        }

        if (state.credits < cost) {
            return false;
        }

        state.credits -= cost;
        building.upgrade();
        gainExperience(corporationId, 200);

        log.info("Upgraded {} to level {} for corporation {}", type, building.getLevel(), corporationId);
        return true;
    }

    /**
     * Hire an employee for the corporation.
     */
    public boolean hireEmployee(Long corporationId, String name, String role, long salary) {
        TycoonGameState state = getGameState(corporationId);
        long hiringCost = 5000; // Base hiring cost

        if (state.credits < hiringCost + salary) {
            return false;
        }

        // Check employee slot availability
        int totalSlots = state.buildings.stream()
                .filter(TycoonBuilding::isConstructed)
                .mapToInt(TycoonBuilding::getEmployeeSlots)
                .sum();
        int activeEmployees = (int) state.employees.stream()
                .filter(TycoonEmployee::isActive)
                .count();

        if (activeEmployees >= totalSlots) {
            log.warn("No employee slots available for corporation {}", corporationId);
            return false;
        }

        state.credits -= (hiringCost + salary);
        TycoonEmployee employee = new TycoonEmployee();
        employee.setName(name);
        employee.setRole(role);
        employee.setDailySalary(salary);
        employee.setActive(true);
        employee.setHiredAt(LocalDateTime.now());
        employee.calculateStats();
        state.employees.add(employee);

        gainExperience(corporationId, 50);
        log.info("Hired {} as {} for corporation {}", name, role, corporationId);
        return true;
    }

    /**
     * Complete an FPS mission and earn rewards.
     */
    public Map<String, Object> completeMission(Long corporationId, int difficulty, int kills, int score) {
        TycoonGameState state = getGameState(corporationId);
        long baseReward = MISSION_REWARDS.getOrDefault(difficulty, 1000L);

        // Bonus for kills and score
        long killBonus = kills * 100L;
        long scoreBonus = score / 10L;
        long totalReward = baseReward + killBonus + scoreBonus;

        state.credits += totalReward;
        state.influence += difficulty * 10;
        state.missionsCompleted++;

        // Gain experience
        long xpGain = difficulty * 100 + kills * 10 + score / 5;
        gainExperience(corporationId, xpGain);

        Map<String, Object> result = new HashMap<>();
        result.put("creditsEarned", totalReward);
        result.put("influenceEarned", difficulty * 10);
        result.put("experienceEarned", xpGain);
        result.put("totalMissions", state.missionsCompleted);
        result.put("corporationLevel", state.level);

        log.info("Mission completed for corporation {}: {} credits, {} XP", corporationId, totalReward, xpGain);
        return result;
    }

    /**
     * Start a research project.
     */
    public boolean startResearch(Long corporationId, String projectName, long cost) {
        TycoonGameState state = getGameState(corporationId);

        if (state.credits < cost) {
            return false;
        }

        // Check for research lab
        boolean hasLab = state.buildings.stream()
                .anyMatch(b -> b.getType() == TycoonBuilding.BuildingType.RESEARCH_LAB && b.isConstructed());
        if (!hasLab) {
            log.warn("No research lab for corporation {}", corporationId);
            return false;
        }

        state.credits -= cost;
        TycoonResearchProject project = new TycoonResearchProject();
        project.setName(projectName);
        project.setCost(cost);
        project.setProgress(0);
        project.setCompleted(false);
        project.setStartedAt(LocalDateTime.now());
        state.researchProjects.add(project);

        log.info("Started research {} for corporation {}", projectName, corporationId);
        return true;
    }

    /**
     * Get available buildings that can be constructed.
     */
    public List<Map<String, Object>> getAvailableBuildings(Long corporationId) {
        TycoonGameState state = getGameState(corporationId);
        List<Map<String, Object>> available = new ArrayList<>();

        for (TycoonBuilding.BuildingType type : TycoonBuilding.BuildingType.values()) {
            boolean hasBuilding = state.buildings.stream()
                    .anyMatch(b -> b.getType() == type && b.isConstructed());

            if (!hasBuilding) {
                Map<String, Object> building = new HashMap<>();
                building.put("type", type.name());
                building.put("displayName", type.getDisplayName());
                building.put("description", type.getDescription());
                building.put("constructionCost", getConstructionCost(type));
                available.add(building);
            }
        }

        return available;
    }

    /**
     * Get the current dashboard data for the corporation.
     */
    public Map<String, Object> getDashboard(Long corporationId) {
        TycoonGameState state = getGameState(corporationId);

        // Process tick first
        tick(corporationId);

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("corporationId", state.corporationId);
        dashboard.put("credits", state.credits);
        dashboard.put("influence", state.influence);
        dashboard.put("tech", state.tech);
        dashboard.put("materials", state.materials);
        dashboard.put("reputation", state.reputation);
        dashboard.put("level", state.level);
        dashboard.put("experience", state.experience);
        dashboard.put("experienceToNextLevel", state.experienceToNextLevel);
        dashboard.put("missionsCompleted", state.missionsCompleted);

        // Buildings summary
        List<Map<String, Object>> buildings = new ArrayList<>();
        for (TycoonBuilding b : state.buildings) {
            if (b.isConstructed()) {
                Map<String, Object> building = new HashMap<>();
                building.put("type", b.getType().name());
                building.put("displayName", b.getType().getDisplayName());
                building.put("level", b.getLevel());
                building.put("maxLevel", b.getMaxLevel());
                building.put("creditsPerHour", b.getCreditsPerHour());
                building.put("influencePerHour", b.getInfluencePerHour());
                building.put("techPerHour", b.getTechPerHour());
                building.put("materialsPerHour", b.getMaterialsPerHour());
                building.put("employeeSlots", b.getEmployeeSlots());
                building.put("upgradeCost", b.getUpgradeCost());
                buildings.add(building);
            }
        }
        dashboard.put("buildings", buildings);

        // Employees summary
        List<Map<String, Object>> employees = new ArrayList<>();
        for (TycoonEmployee e : state.employees) {
            if (e.isActive()) {
                Map<String, Object> employee = new HashMap<>();
                employee.put("name", e.getName());
                employee.put("role", e.getRole());
                employee.put("efficiency", e.getEfficiency());
                employee.put("dailySalary", e.getDailySalary());
                employees.add(employee);
            }
        }
        dashboard.put("employees", employees);

        // Research projects
        List<Map<String, Object>> research = new ArrayList<>();
        for (TycoonResearchProject r : state.researchProjects) {
            Map<String, Object> project = new HashMap<>();
            project.put("name", r.getName());
            project.put("progress", r.getProgress());
            project.put("completed", r.isCompleted());
            research.add(project);
        }
        dashboard.put("researchProjects", research);

        // Resource generation rates
        long totalCreditsPerHour = state.buildings.stream()
                .filter(TycoonBuilding::isConstructed)
                .mapToLong(TycoonBuilding::getCreditsPerHour)
                .sum();
        dashboard.put("creditsPerHour", totalCreditsPerHour);

        return dashboard;
    }

    private long getConstructionCost(TycoonBuilding.BuildingType type) {
        switch (type) {
            case HEADQUARTERS: return 2000;
            case RESEARCH_LAB: return 5000;
            case MANUFACTURING_PLANT: return 8000;
            case TRADING_POST: return 3000;
            case SECURITY_OFFICE: return 4000;
            case DATA_CENTER: return 6000;
            case RECRUITING_OFFICE: return 3500;
            case BANK_BRANCH: return 10000;
            case MEDIA_OFFICE: return 4500;
            case PORT_FACILITY: return 7000;
            default: return 5000;
        }
    }

    private void gainExperience(Long corporationId, long xp) {
        TycoonGameState state = getGameState(corporationId);
        state.experience += xp;

        // Level up check
        while (state.experience >= state.experienceToNextLevel) {
            state.experience -= state.experienceToNextLevel;
            state.level++;
            state.experienceToNextLevel = (long) (state.experienceToNextLevel * 1.5);
            state.reputation += 5;

            log.info("Corporation {} leveled up to level {}", corporationId, state.level);
        }
    }

    /**
     * Inner class representing the tycoon game state for a corporation.
     */
    public static class TycoonGameState {
        Long corporationId;
        long credits;
        long influence;
        long tech;
        long materials;
        int reputation;
        int level;
        long experience;
        long experienceToNextLevel;
        int missionsCompleted;
        List<TycoonBuilding> buildings;
        List<TycoonEmployee> employees;
        List<TycoonResearchProject> researchProjects;
        LocalDateTime lastTick;
        LocalDateTime createdAt;
    }
}

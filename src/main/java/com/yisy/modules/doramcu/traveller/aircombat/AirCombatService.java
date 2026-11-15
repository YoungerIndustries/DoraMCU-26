package com.yisy.modules.doramcu.traveller.aircombat;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for air combat missions in the Traveller module.
 */
@Service
public class AirCombatService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AirCombatService.class);

    private final AircraftDesignRepository aircraftRepository;
    private final AirCombatMissionRepository missionRepository;

    public AirCombatService(AircraftDesignRepository aircraftRepository,
                             AirCombatMissionRepository missionRepository) {
        this.aircraftRepository = aircraftRepository;
        this.missionRepository = missionRepository;
    }

    public List<AircraftDesign> getAllAircraft() {
        return aircraftRepository.findAll();
    }

    public List<AircraftDesign> getAircraftByType(String type) {
        return aircraftRepository.findByType(type);
    }

    public List<AirCombatMission> getMissions(String characterId) {
        return missionRepository.findByCharacterIdOrderByCreatedAtDesc(characterId);
    }

    public AirCombatMission createMission(String characterId, String missionType) {
        Random rand = new Random();
        AirCombatMission mission = new AirCombatMission();
        mission.setCharacterId(characterId);
        mission.setMissionType(missionType != null ? missionType : "AIR_TO_AIR");
        mission.setStatus("ACTIVE");
        mission.setCreatedAt(LocalDateTime.now());

        String[] titles = {
            "Intercept Enemy Fighters", "Ground Attack Run", "Reconnaissance Patrol",
            "Escort Convoy", "Strike Command Center", "Defense of Starport"
        };
        mission.setTitle(titles[rand.nextInt(titles.length)]);
        mission.setDescription("Complete the " + mission.getMissionType() + " mission successfully.");

        mission.setDifficulty(rand.nextInt(8) + 2);
        mission.setEnemyCount(rand.nextInt(10) + 3);

        String[] enemies = {"Pirate Fighters", "Rebel Bombers", "Alien Craft", "Mercenary Jets"};
        mission.setEnemyType(enemies[rand.nextInt(enemies.length)]);

        mission.setXpReward(mission.getDifficulty() * 10 + rand.nextInt(20));
        mission.setCreditReward(mission.getDifficulty() * 100 + rand.nextInt(500));

        return missionRepository.save(mission);
    }

    public MissionResult resolveMission(Long missionId) {
        AirCombatMission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));

        if (!"ACTIVE".equals(mission.getStatus())) {
            throw new IllegalStateException("Mission is not active: " + mission.getStatus());
        }

        Random rand = new Random();
        int roll = rand.nextInt(20) + 1;
        int dc = 10 + mission.getDifficulty();

        MissionResult result = new MissionResult();
        mission.setCompletedAt(LocalDateTime.now());

        if (roll >= dc) {
            mission.setStatus("COMPLETED");
            result.setSuccess(true);
            result.setMessage("Mission completed successfully! Earned " + mission.getXpReward() + " XP and " + mission.getCreditReward() + " credits.");
            result.setXpEarned(mission.getXpReward());
            result.setCreditsEarned(mission.getCreditReward());
        } else if (roll >= dc - 5) {
            mission.setStatus("COMPLETED");
            result.setSuccess(true);
            result.setMessage("Mission completed with damage. Partial rewards.");
            result.setXpEarned(mission.getXpReward() / 2);
            result.setCreditsEarned(mission.getCreditReward() / 2);
        } else {
            mission.setStatus("FAILED");
            result.setSuccess(false);
            result.setMessage("Mission failed. " + mission.getEnemyType() + " overwhelmed your forces.");
            result.setXpEarned(mission.getXpReward() / 4);
            result.setCreditsEarned(0);
        }

        missionRepository.save(mission);
        log.info("Mission {} resolved: {}", missionId, result.isSuccess() ? "SUCCESS" : "FAILED");
        return result;
    }

    public static class MissionResult {
        private boolean success;
        private String message;
        private int xpEarned;
        private int creditsEarned;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getXpEarned() { return xpEarned; }
        public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }
        public int getCreditsEarned() { return creditsEarned; }
        public void setCreditsEarned(int creditsEarned) { this.creditsEarned = creditsEarned; }
    }

    @Override
    public void run(String... args) {
        if (aircraftRepository.count() > 0) return;

        log.info("Initializing default aircraft designs...");

        String[][] aircraft = {
            {"Star Kraken", "FIGHTER", "10", "6", "3", "12", "8", "Laser x2", "2", "1", "8", "500000"},
            {"Star Cobra", "FIGHTER", "8", "5", "2", "10", "10", "Laser x2, Missile", "2", "1", "9", "750000"},
            {"Star Dragon", "FIGHTER", "12", "7", "4", "15", "6", "Laser x3, Missile x2", "3", "1", "10", "1000000"},
            {"Star Hound", "RECON", "6", "4", "1", "8", "12", "Laser", "3", "1", "8", "300000"},
            {"Star Eagle", "BOMBER", "20", "4", "5", "20", "5", "Bomb Bay, Laser x2", "2", "2", "10", "2000000"},
            {"Star Falcon", "TRANSPORT", "15", "3", "3", "15", "4", "Laser", "4", "2", "7", "1500000"},
            {"Star Viper", "FIGHTER", "9", "5", "2", "11", "9", "Laser x2", "2", "1", "8", "600000"},
            {"Star Hawk", "FIGHTER", "11", "6", "3", "14", "7", "Laser x3", "2", "1", "9", "850000"},
            {"Star Owl", "RECON", "7", "4", "1", "9", "11", "Sensor Suite", "3", "1", "9", "400000"},
            {"Star Bear", "BOMBER", "25", "3", "6", "25", "4", "Heavy Bombs, Laser x2", "3", "3", "11", "3000000"},
        };

        for (String[] a : aircraft) {
            AircraftDesign design = new AircraftDesign();
            design.setName(a[0]);
            design.setType(a[1]);
            design.setTonnage(Integer.parseInt(a[2]));
            design.setThrust(Integer.parseInt(a[3]));
            design.setArmor(Integer.parseInt(a[4]));
            design.setHitPoints(Integer.parseInt(a[5]));
            design.setSpeed(Integer.parseInt(a[6]));
            design.setWeapons(a[7]);
            design.setFuelCapacity(Integer.parseInt(a[8]));
            design.setCrew(Integer.parseInt(a[9]));
            design.setTechLevel(Integer.parseInt(a[10]));
            design.setCost(Long.parseLong(a[11]));
            aircraftRepository.save(design);
        }

        log.info("Initialized {} default aircraft designs", aircraftRepository.count());
    }
}

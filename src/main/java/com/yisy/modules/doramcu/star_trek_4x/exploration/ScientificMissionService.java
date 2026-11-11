package com.yisy.modules.doramcu.star_trek_4x.exploration;

import com.yisy.modules.doramcu.star_trek_4x.model.StarSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Service for generating scientific research missions.
 * <p>
 * Creates research missions based on planet properties and Star Trek science themes
 * such as xenobiology, archaeology, astrophysics, and planetary science.
 * </p>
 */
@Service
public class ScientificMissionService {

    private static final Logger log = LoggerFactory.getLogger(ScientificMissionService.class);
    private static final Random random = new Random();

    private static final String[] MISSION_TYPES = {
            "XENOBIOLOGY", "ARCHAEOLOGY", "ASTROPHYSICS", "PLANETARY_SCIENCE",
            "TELEPATHY_RESEARCH", "TEMPORAL_ANOMALY", "WARP_THEORY", "ANOMALOUS_READINGS"
    };

    private static final Map<String, String[]> MISSION_DESCRIPTIONS = Map.of(
            "XENOBIOLOGY", new String[]{
                    "Study the unique flora and fauna of this world.",
                    "Analyze the genetic structure of native species.",
                    "Investigate the symbiotic relationship between local organisms.",
                    "Catalog the biodiversity of this planet's ecosystems."
            },
            "ARCHAEOLOGY", new String[]{
                    "Excavate ancient ruins discovered on the surface.",
                    "Decipher alien inscriptions found in a buried structure.",
                    "Investigate pre-warp civilization artifacts.",
                    "Map the extent of an archaeological site."
            },
            "ASTROPHYSICS", new String[]{
                    "Observe a rare celestial event in this system.",
                    "Study the gravitational effects of a nearby black hole.",
                    "Analyze the composition of a unique star.",
                    "Measure the properties of dark matter in this sector."
            },
            "PLANETARY_SCIENCE", new String[]{
                    "Conduct a comprehensive geological survey.",
                    "Investigate unusual atmospheric phenomena.",
                    "Study the planet's magnetic field anomalies.",
                    "Analyze the core composition and tectonic activity."
            },
            "TELEPATHY_RESEARCH", new String[]{
                    "Investigate reports of telepathic entities.",
                    "Study the mental abilities of native species.",
                    "Research the source of psychic disturbances.",
                    "Document telepathic communication patterns."
            },
            "TEMPORAL_ANOMALY", new String[]{
                    "Investigate a temporal rift detected in this system.",
                    "Study the effects of time dilation on local space.",
                    "Analyze chroniton particle concentrations.",
                    "Determine the cause of temporal distortions."
            },
            "WARP_THEORY", new String[]{
                    "Study the warp core deposits for scientific research.",
                    "Analyze the subspace properties of this region.",
                    "Investigate the effects of warp travel on local space.",
                    "Research new applications for dilithium crystals."
            },
            "ANOMALOUS_READINGS", new String[]{
                    "Investigate unexplained energy signatures.",
                    "Study the source of mysterious radiation.",
                    "Analyze unusual sensor readings from this system.",
                    "Determine the nature of an unknown phenomenon."
            }
    );

    /**
     * Generate a scientific mission for a discovered system.
     *
     * @param system the system to generate a mission for
     * @return mission details
     */
    public Map<String, Object> generateScientificMission(StarSystem system) {
        log.info("Generating scientific mission for system: {}", system.getName());

        String missionType = selectMissionType(system);
        String description = selectDescription(missionType);
        int difficulty = calculateDifficulty(system);
        int researchPoints = calculateResearchPoints(system, difficulty);
        int duration = calculateDuration(difficulty);

        return Map.of(
                "missionType", missionType,
                "description", description,
                "systemName", system.getName(),
                "difficulty", difficulty,
                "researchPoints", researchPoints,
                "duration", duration,
                "requirements", getRequirements(missionType),
                "successChance", calculateSuccessChance(difficulty)
        );
    }

    /**
     * Select a mission type based on system properties.
     */
    private String selectMissionType(StarSystem system) {
        // Warp deposits favor warp theory missions
        if (system.isHasWarpDeposits() && random.nextInt(100) < 40) {
            return "WARP_THEORY";
        }

        // Native species favor xenobiology
        if (system.getNativeSpecies() != null && !system.getNativeSpecies().isEmpty() && random.nextInt(100) < 35) {
            return "XENOBIOLOGY";
        }

        // Notable features favor archaeology
        if (system.getNotableFeatures() != null && system.getNotableFeatures().contains("ruins")) {
            return "ARCHAEOLOGY";
        }

        // Random selection
        return MISSION_TYPES[random.nextInt(MISSION_TYPES.length)];
    }

    /**
     * Select a description for the mission type.
     */
    private String selectDescription(String missionType) {
        String[] descriptions = MISSION_DESCRIPTIONS.getOrDefault(missionType, new String[]{"Conduct scientific research."});
        return descriptions[random.nextInt(descriptions.length)];
    }

    /**
     * Calculate mission difficulty based on system properties.
     */
    private int calculateDifficulty(StarSystem system) {
        int baseDifficulty = 1;

        // Higher tech level increases difficulty
        if (system.getUwpGovernment() != null) {
            baseDifficulty += system.getUwpGovernment().charAt(0) - '0';
        }

        // Hostile alignment increases difficulty
        if (system.getAlignment() == StarSystem.StarAlignment.HOSTILE ||
            system.getAlignment() == StarSystem.StarAlignment.BORG) {
            baseDifficulty += 3;
        }

        // Random factor
        baseDifficulty += random.nextInt(3);

        return Math.min(10, Math.max(1, baseDifficulty));
    }

    /**
     * Calculate research points reward.
     */
    private int calculateResearchPoints(StarSystem system, int difficulty) {
        int basePoints = difficulty * 10;

        // Warp deposits give bonus
        if (system.isHasWarpDeposits()) {
            basePoints += 20;
        }

        // Trade value bonus
        basePoints += system.getTradeValue() / 2;

        return basePoints;
    }

    /**
     * Calculate mission duration in turns.
     */
    private int calculateDuration(int difficulty) {
        return difficulty + random.nextInt(3);
    }

    /**
     * Get requirements for a mission type.
     */
    private List<String> getRequirements(String missionType) {
        return switch (missionType) {
            case "XENOBIOLOGY" -> List.of("Science Officer", "Laboratory", "Sample Containers");
            case "ARCHAEOLOGY" -> List.of("Science Officer", "Excavation Tools", "Translation Matrix");
            case "ASTROPHYSICS" -> List.of("Science Officer", "Observatory", "Sensor Array");
            case "PLANETARY_SCIENCE" -> List.of("Science Officer", "Geological Scanner", "Drill");
            case "TELEPATHY_RESEARCH" -> List.of("Vulcan Science Officer", "Mental Shields", "Sensor Array");
            case "TEMPORAL_ANOMALY" -> List.of("Science Officer", "Chroniton Detector", "Temporal Sensors");
            case "WARP_THEORY" -> List.of("Science Officer", "Engineering Team", "Dilithium Analyzer");
            default -> List.of("Science Officer", "Laboratory");
        };
    }

    /**
     * Calculate success chance based on difficulty.
     */
    private int calculateSuccessChance(int difficulty) {
        return Math.max(20, 90 - difficulty * 7);
    }

    /**
     * Get all available mission types.
     *
     * @return list of mission types
     */
    public String[] getMissionTypes() {
        return MISSION_TYPES.clone();
    }
}

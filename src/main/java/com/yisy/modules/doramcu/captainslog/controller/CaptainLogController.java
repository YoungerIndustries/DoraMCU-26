package com.yisy.modules.doramcu.captainslog.controller;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogEntry;
import com.yisy.modules.doramcu.captainslog.dto.CombatRoundResponse;
import com.yisy.modules.doramcu.captainslog.dto.CreateCharacterRequest;
import com.yisy.modules.doramcu.captainslog.dto.CreateCharacterTalentRequest;
import com.yisy.modules.doramcu.captainslog.dto.CreateCrewMemberRequest;
import com.yisy.modules.doramcu.captainslog.dto.CreateMissionRequest;
import com.yisy.modules.doramcu.captainslog.dto.CreateShipTalentRequest;
import com.yisy.modules.doramcu.captainslog.dto.CreateStarshipRequest;
import com.yisy.modules.doramcu.captainslog.dto.DiceRollResponse;
import com.yisy.modules.doramcu.captainslog.dto.GenerateMissionRequest;
import com.yisy.modules.doramcu.captainslog.dto.GeneratedMissionResponse;
import com.yisy.modules.doramcu.captainslog.dto.LifepathChoiceRequest;
import com.yisy.modules.doramcu.captainslog.dto.LifepathStartRequest;
import com.yisy.modules.doramcu.captainslog.dto.LifepathStepResponse;
import com.yisy.modules.doramcu.captainslog.dto.OracleQueryRequest;
import com.yisy.modules.doramcu.captainslog.dto.StartCombatRequest;
import com.yisy.modules.doramcu.captainslog.dto.StarshipCreationChoiceRequest;
import com.yisy.modules.doramcu.captainslog.dto.StarshipCreationStartRequest;
import com.yisy.modules.doramcu.captainslog.dto.StarshipCreationStepResponse;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacterTalent;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCombat;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogCrewMember;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogShipTalent;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogMission;
import com.yisy.modules.doramcu.captainslog.model.CaptainLogStarship;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogService;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogShipTalentService;
import com.yisy.modules.doramcu.captainslog.service.CaptainLogTalentService;
import com.yisy.modules.doramcu.captainslog.service.ComplicationResolutionService;
import com.yisy.modules.doramcu.captainslog.service.MissionDevelopmentService;
import com.yisy.modules.doramcu.captainslog.service.NarrativeBeatService;
import com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService;
import com.yisy.modules.doramcu.captainslog.service.StarshipCreationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for the Captain's Log Solo RPG system.
 * <p>
 * Provides endpoints for character creation, starship creation, mission management,
 * 2d20 dice rolling, and oracle queries.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/captains-log}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/captains-log")
public class CaptainLogController {

    private static final Logger log = LoggerFactory.getLogger(CaptainLogController.class);

    private final CaptainLogService captainLogService;

    public CaptainLogController(CaptainLogService captainLogService) {
        this.captainLogService = captainLogService;
    }

    // ==================== Character Endpoints ====================

    /**
     * Creates a new Captain's Log character.
     */
    @PostMapping("/characters")
    public ResponseEntity<CaptainLogCharacter> createCharacter(@RequestBody CreateCharacterRequest request) {
        log.debug("POST /api/v1/captains-log/characters - creating character: {}", request.getName());
        CaptainLogCharacter character = captainLogService.createCharacter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(character);
    }

    /**
     * Retrieves a character by ID.
     */
    @GetMapping("/characters/{id}")
    public ResponseEntity<CaptainLogCharacter> getCharacter(@PathVariable Long id) {
        log.debug("GET /api/v1/captains-log/characters/{}", id);
        return captainLogService.getCharacter(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Lists all characters.
     */
    @GetMapping("/characters")
    public ResponseEntity<List<CaptainLogCharacter>> listCharacters() {
        log.debug("GET /api/v1/captains-log/characters - listing all");
        return ResponseEntity.ok(captainLogService.listCharacters());
    }

    /**
     * Deletes a character by ID.
     */
    @DeleteMapping("/characters/{id}")
    public ResponseEntity<Void> deleteCharacter(@PathVariable Long id) {
        log.debug("DELETE /api/v1/captains-log/characters/{}", id);
        captainLogService.deleteCharacter(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== Starship Endpoints ====================

    /**
     * Creates a new starship or station.
     */
    @PostMapping("/starships")
    public ResponseEntity<CaptainLogStarship> createStarship(@RequestBody CreateStarshipRequest request) {
        log.debug("POST /api/v1/captains-log/starships - creating: {}", request.getName());
        CaptainLogStarship starship = captainLogService.createStarship(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(starship);
    }

    /**
     * Retrieves a starship by ID.
     */
    @GetMapping("/starships/{id}")
    public ResponseEntity<CaptainLogStarship> getStarship(@PathVariable Long id) {
        log.debug("GET /api/v1/captains-log/starships/{}", id);
        return captainLogService.getStarship(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Lists all starships.
     */
    @GetMapping("/starships")
    public ResponseEntity<List<CaptainLogStarship>> listStarships() {
        log.debug("GET /api/v1/captains-log/starships - listing all");
        return ResponseEntity.ok(captainLogService.listStarships());
    }

    /**
     * Deletes a starship by ID.
     */
    @DeleteMapping("/starships/{id}")
    public ResponseEntity<Void> deleteStarship(@PathVariable Long id) {
        log.debug("DELETE /api/v1/captains-log/starships/{}", id);
        captainLogService.deleteStarship(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assigns a character to a starship.
     * The character becomes part of the ship's crew roster.
     */
    @PutMapping("/characters/{characterId}/assign")
    public ResponseEntity<CaptainLogCharacter> assignCharacterToStarship(
            @PathVariable Long characterId,
            @RequestBody Map<String, Long> body) {
        Long starshipId = body.get("starshipId");
        log.info("PUT /api/v1/captains-log/characters/{}/assign - starship: {}", characterId, starshipId);
        CaptainLogCharacter character = captainLogService.assignCharacterToStarship(characterId, starshipId);
        return ResponseEntity.ok(character);
    }

    /**
     * Removes a character's starship assignment.
     */
    @DeleteMapping("/characters/{characterId}/assign")
    public ResponseEntity<CaptainLogCharacter> unassignCharacter(@PathVariable Long characterId) {
        log.info("DELETE /api/v1/captains-log/characters/{}/assign", characterId);
        CaptainLogCharacter character = captainLogService.unassignCharacter(characterId);
        return ResponseEntity.ok(character);
    }

    /**
     * Lists all characters assigned to a starship (crew roster).
     */
    @GetMapping("/starships/{id}/crew")
    public ResponseEntity<List<CaptainLogCharacter>> getCrew(@PathVariable Long id) {
        log.debug("GET /api/v1/captains-log/starships/{}/crew", id);
        return ResponseEntity.ok(captainLogService.getCrew(id));
    }

    // ==================== Mission Endpoints ====================

    /**
     * Creates a new mission for a character.
     */
    @PostMapping("/missions")
    public ResponseEntity<CaptainLogMission> createMission(@RequestBody CreateMissionRequest request) {
        log.debug("POST /api/v1/captains-log/missions - creating: {}", request.getName());
        CaptainLogMission mission = captainLogService.createMission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mission);
    }

    /**
     * Retrieves a mission by ID.
     */
    @GetMapping("/missions/{id}")
    public ResponseEntity<CaptainLogMission> getMission(@PathVariable Long id) {
        log.debug("GET /api/v1/captains-log/missions/{}", id);
        return captainLogService.getMission(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Lists all missions for a character.
     */
    @GetMapping("/missions")
    public ResponseEntity<List<CaptainLogMission>> listMissions(@RequestParam Long characterId) {
        log.debug("GET /api/v1/captains-log/missions?characterId={}", characterId);
        return ResponseEntity.ok(captainLogService.listMissions(characterId));
    }

    /**
     * Updates a mission's act log.
     */
    @PutMapping("/missions/{id}/log")
    public ResponseEntity<CaptainLogMission> updateMissionLog(
            @PathVariable Long id,
            @RequestParam int act,
            @RequestBody Map<String, String> body) {
        log.debug("PUT /api/v1/captains-log/missions/{}/log?act={}", id, act);
        String logText = body.get("text");
        CaptainLogMission mission = captainLogService.updateMissionLog(id, act, logText);
        return ResponseEntity.ok(mission);
    }

    /**
     * Completes a mission.
     */
    @PutMapping("/missions/{id}/complete")
    public ResponseEntity<CaptainLogMission> completeMission(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        log.debug("PUT /api/v1/captains-log/missions/{}/complete", id);
        String summary = body.getOrDefault("summary", "");
        String development = body.getOrDefault("development", "");
        CaptainLogMission mission = captainLogService.completeMission(id, summary, development);
        return ResponseEntity.ok(mission);
    }

    /**
     * Deletes a mission by ID.
     */
    @DeleteMapping("/missions/{id}")
    public ResponseEntity<Void> deleteMission(@PathVariable Long id) {
        log.debug("DELETE /api/v1/captains-log/missions/{}", id);
        captainLogService.deleteMission(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Spends Momentum on a mission.
     * Momentum can be spent to reroll a die, remove a Threat, or gain an advantage.
     */
    @PutMapping("/missions/{id}/spend-momentum")
    public ResponseEntity<CaptainLogMission> spendMomentum(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 1);
        CaptainLogMission mission = captainLogService.spendMomentum(id, amount);
        return ResponseEntity.ok(mission);
    }

    /**
     * Adds Momentum to a mission (e.g., from narrative advantage).
     * Capped at max 1 in Captain's Log.
     */
    @PutMapping("/missions/{id}/add-momentum")
    public ResponseEntity<CaptainLogMission> addMomentum(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 1);
        CaptainLogMission mission = captainLogService.addMomentum(id, amount);
        return ResponseEntity.ok(mission);
    }

    /**
     * Spends Threat on a mission to gain a boon on the next task attempt.
     */
    @PutMapping("/missions/{id}/spend-threat")
    public ResponseEntity<CaptainLogMission> spendThreat(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        int amount = body.getOrDefault("amount", 1);
        CaptainLogMission mission = captainLogService.spendThreat(id, amount);
        return ResponseEntity.ok(mission);
    }

    /**
     * Advances the narrative beat for a mission.
     * Moves to the next beat (1-5 per act). After beat 5, advances to the next act.
     */
    @PutMapping("/missions/{id}/advance-beat")
    public ResponseEntity<CaptainLogMission> advanceBeat(@PathVariable Long id) {
        CaptainLogMission mission = captainLogService.advanceBeat(id);
        return ResponseEntity.ok(mission);
    }

    // ==================== Log Entry Endpoints ====================

    /**
     * Creates a new log entry for a mission.
     */
    @PostMapping("/missions/{missionId}/entries")
    public ResponseEntity<CaptainLogEntry> createLogEntry(
            @PathVariable Long missionId,
            @RequestBody Map<String, Object> body) {
        log.debug("POST /api/v1/captains-log/missions/{}/entries", missionId);
        CaptainLogEntry.EntryType type = CaptainLogEntry.EntryType.valueOf(
                String.valueOf(body.getOrDefault("type", "NARRATIVE")));
        int act = Integer.parseInt(String.valueOf(body.getOrDefault("act", 1)));
        String text = String.valueOf(body.get("text"));
        CaptainLogEntry entry = captainLogService.createLogEntry(missionId, type, act, text);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    /**
     * Lists all log entries for a mission.
     */
    @GetMapping("/missions/{missionId}/entries")
    public ResponseEntity<List<CaptainLogEntry>> listLogEntries(@PathVariable Long missionId) {
        log.debug("GET /api/v1/captains-log/missions/{}/entries", missionId);
        return ResponseEntity.ok(captainLogService.listLogEntries(missionId));
    }

    // ==================== Task Attempt Endpoints ====================

    /**
     * Performs a task attempt using the correct 2d20 "count successes" mechanic.
     * <p>
     * The target number is calculated from the character's attribute + discipline values.
     * Focus bonuses, complications, momentum, and threat are all handled automatically.
     * </p>
     *
     * @param request the task attempt request with character ID, attribute, discipline, etc.
     * @return the task attempt result with successes, momentum, and threat
     */
    @PostMapping("/tasks/attempt")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.dto.TaskAttemptResponse> taskAttempt(
            @RequestBody com.yisy.modules.doramcu.captainslog.dto.TaskAttemptRequest request) {
        log.info("POST /api/v1/captains-log/tasks/attempt - character: {} attribute: {} discipline: {}",
                request.getCharacterId(), request.getAttribute(), request.getDiscipline());
        com.yisy.modules.doramcu.captainslog.dto.TaskAttemptResponse result =
                captainLogService.rollTaskAttempt(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Performs a starship system task attempt using the correct 2d20 "count successes" mechanic.
     * <p>
     * The target number is calculated from the starship's system + department ratings.
     * On failure, the system takes 1 stress. Complications, momentum, and threat are all handled.
     * </p>
     *
     * @param request the starship task attempt request with starship ID, system, department, etc.
     * @return the task attempt result with successes, momentum, and threat
     */
    @PostMapping("/starships/tasks/attempt")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.dto.TaskAttemptResponse> starshipTaskAttempt(
            @RequestBody com.yisy.modules.doramcu.captainslog.dto.StarshipTaskAttemptRequest request) {
        log.info("POST /api/v1/captains-log/starships/tasks/attempt - starship: {} system: {} department: {}",
                request.getStarshipId(), request.getSystem(), request.getDepartment());
        com.yisy.modules.doramcu.captainslog.dto.TaskAttemptResponse result =
                captainLogService.rollStarshipTaskAttempt(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Queries the suggested difficulty for a task attempt based on mission context.
     * <p>
     * Returns the calculated difficulty (1-3), suggested threats, and a human-readable
     * explanation based on mission number, character rank, and mission type.
     * </p>
     *
     * @param missionId the mission context (required)
     * @param characterId the character performing the task (required)
     */
    @GetMapping("/tasks/difficulty")
    public ResponseEntity<Map<String, Object>> queryDifficulty(
            @RequestParam Long missionId,
            @RequestParam Long characterId) {
        log.debug("GET /api/v1/captains-log/tasks/difficulty?missionId={}&characterId={}", missionId, characterId);
        com.yisy.modules.doramcu.captainslog.model.CaptainLogMission mission =
                captainLogService.getMission(missionId)
                        .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + missionId));
        com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter character =
                captainLogService.getCharacter(characterId)
                        .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
        int difficulty = captainLogService.calculateDifficulty(mission, character);
        int threats = captainLogService.calculateThreats(mission);
        String description = captainLogService.getDifficultyDescription(mission, character, difficulty);
        return ResponseEntity.ok(Map.of(
                "suggestedDifficulty", difficulty,
                "suggestedThreats", threats,
                "description", description,
                "missionNumber", mission.getMissionNumber(),
                "missionType", mission.getType(),
                "characterRank", character.getRank()
        ));
    }

    // ==================== Dice Roll Endpoints ====================

    /**
     * Performs a 2d20 skill check (legacy endpoint, backward compatible).
     *
     * @param targetNumber target number (attribute + discipline, default 11)
     * @param boons        number of boon dice (default 0)
     * @param threats      number of threat dice (default 0)
     */
    @GetMapping("/dice/roll")
    public ResponseEntity<DiceRollResponse> rollDice(
            @RequestParam(defaultValue = "11") int targetNumber,
            @RequestParam(defaultValue = "0") int boons,
            @RequestParam(defaultValue = "0") int threats) {
        log.debug("GET /api/v1/captains-log/dice/roll?targetNumber={}&boons={}&threats={}",
                targetNumber, boons, threats);
        DiceRollResponse result = captainLogService.rollSkillCheck(targetNumber, boons, threats);
        return ResponseEntity.ok(result);
    }

    // ==================== Oracle Endpoints ====================

    /**
     * Queries the oracle with a yes/no question (legacy endpoint).
     */
    @PostMapping("/oracle/query")
    public ResponseEntity<DiceRollResponse> queryOracle(@RequestBody OracleQueryRequest request) {
        log.debug("POST /api/v1/captains-log/oracle/query - question: {}", request.getQuestion());
        DiceRollResponse result = captainLogService.queryOracle(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Queries the Yes/No oracle with a probability level.
     * <p>
     * The probability level determines the likelihood of a Yes result:
     * VERY_UNLIKELY (10%), UNLIKELY (30%), FIFTY_FIFTY (50%),
     * LIKELY (70%), VERY_LIKELY (90%), CERTAIN (100%).
     * </p>
     *
     * @param probability the estimated probability level
     */
    @GetMapping("/oracle/yesno")
    public ResponseEntity<Map<String, String>> queryYesNo(
            @RequestParam(defaultValue = "FIFTY_FIFTY") com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService.ProbabilityLevel probability) {
        log.debug("GET /api/v1/captains-log/oracle/yesno?probability={}", probability);
        com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService.YesNoResult result =
                captainLogService.queryYesNo(probability);
        return ResponseEntity.ok(Map.of(
                "result", result.name(),
                "description", result.getDescription(),
                "probability", probability.name()
        ));
    }

    /**
     * Generates a random prompt card for mission inspiration.
     * Uses the expanded 60+ prompt set organized by category.
     */
    @GetMapping("/oracle/prompt")
    public ResponseEntity<Map<String, String>> generatePrompt() {
        log.debug("GET /api/v1/captains-log/oracle/prompt");
        String prompt = captainLogService.generatePromptCard();
        return ResponseEntity.ok(Map.of("prompt", prompt));
    }

    /**
     * Rolls on a random table.
     *
     * @param size the size of the table (e.g., 6 for d6, 20 for d20)
     */
    @GetMapping("/oracle/table")
    public ResponseEntity<Map<String, Integer>> rollTable(@RequestParam(defaultValue = "6") int size) {
        log.debug("GET /api/v1/captains-log/oracle/table?size={}", size);
        int result = captainLogService.rollTable(size);
        return ResponseEntity.ok(Map.of("result", result, "tableSize", size));
    }

    /**
     * Rolls for a random mission type.
     * Returns one of 20 mission types (Diplomacy, Exploration, Combat, etc.).
     */
    @GetMapping("/oracle/mission-type")
    public ResponseEntity<Map<String, String>> rollMissionType() {
        log.debug("GET /api/v1/captains-log/oracle/mission-type");
        com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService.MissionType type =
                captainLogService.rollMissionType();
        return ResponseEntity.ok(Map.of(
                "type", type.name(),
                "description", type.getDescription()
        ));
    }

    /**
     * Rolls for a random mission theme.
     * Returns one of 20 narrative themes (Betrayal, Sacrifice, Redemption, etc.).
     */
    @GetMapping("/oracle/theme")
    public ResponseEntity<Map<String, String>> rollMissionTheme() {
        log.debug("GET /api/v1/captains-log/oracle/theme");
        com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService.MissionTheme theme =
                captainLogService.rollMissionTheme();
        return ResponseEntity.ok(Map.of(
                "theme", theme.name(),
                "description", theme.getDescription()
        ));
    }

    /**
     * Rolls for a random complication.
     * Complications arise from Threat, failed checks, or narrative choices.
     */
    @GetMapping("/oracle/complication")
    public ResponseEntity<Map<String, String>> rollComplication() {
        log.debug("GET /api/v1/captains-log/oracle/complication");
        com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService.Complication complication =
                captainLogService.rollComplication();
        return ResponseEntity.ok(Map.of(
                "complication", complication.name(),
                "description", complication.getDescription()
        ));
    }

    /**
     * Rolls for a random advantage.
     * Advantages help during a mission, triggered by Momentum or successful checks.
     */
    @GetMapping("/oracle/advantage")
    public ResponseEntity<Map<String, String>> rollAdvantage() {
        log.debug("GET /api/v1/captains-log/oracle/advantage");
        com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService.Advantage advantage =
                captainLogService.rollAdvantage();
        return ResponseEntity.ok(Map.of(
                "advantage", advantage.name(),
                "description", advantage.getDescription()
        ));
    }

    /**
     * Rolls for a random encounter.
     * Returns one of 20 encounter types (Hostile Ship, Distress Signal, etc.).
     */
    @GetMapping("/oracle/encounter")
    public ResponseEntity<Map<String, String>> rollEncounter() {
        log.debug("GET /api/v1/captains-log/oracle/encounter");
        com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService.EncounterType encounter =
                captainLogService.rollEncounter();
        return ResponseEntity.ok(Map.of(
                "encounter", encounter.name(),
                "description", encounter.getDescription()
        ));
    }

    /**
     * Generates a random NPC profile.
     * Returns name, species, role, and alignment.
     */
    @GetMapping("/oracle/npc")
    public ResponseEntity<Map<String, String>> generateNpc() {
        log.debug("GET /api/v1/captains-log/oracle/npc");
        Map<String, String> npc = captainLogService.generateNpc();
        return ResponseEntity.ok(npc);
    }

    /**
     * Generates a random location profile.
     * Returns name, location type, and planet type.
     */
    @GetMapping("/oracle/location")
    public ResponseEntity<Map<String, String>> generateLocation() {
        log.debug("GET /api/v1/captains-log/oracle/location");
        Map<String, String> location = captainLogService.generateLocation();
        return ResponseEntity.ok(location);
    }

    /**
     * Rolls for a random career event.
     * Career events occur during downtime between missions.
     */
    @GetMapping("/oracle/career-event")
    public ResponseEntity<Map<String, String>> rollCareerEvent() {
        log.debug("GET /api/v1/captains-log/oracle/career-event");
        com.yisy.modules.doramcu.captainslog.service.ProbabilityMatrixService.CareerEvent event =
                captainLogService.rollCareerEvent();
        return ResponseEntity.ok(Map.of(
                "event", event.name(),
                "description", event.getDescription()
        ));
    }

    /**
     * Generates a complete mission profile by rolling on multiple tables.
     * Returns mission type, theme, encounter, NPC, location, and prompt card.
     */
    @GetMapping("/oracle/mission-profile")
    public ResponseEntity<Map<String, Object>> generateMissionProfile() {
        log.debug("GET /api/v1/captains-log/oracle/mission-profile");
        Map<String, Object> profile = captainLogService.generateMissionProfile();
        return ResponseEntity.ok(profile);
    }

    // ==================== AI Mission Generation Endpoints ====================

    /**
     * Uses AI to generate a complete mission proposal for a character.
     * <p>
     * The AI considers the character's profile (rank, assignment, attributes, disciplines,
     * values, focuses), mission history, and any optional theme or type constraints.
     * The generated mission is returned as a proposal but not saved to the database.
     * </p>
     *
     * @param request the generation request containing character ID and optional constraints
     * @return the AI-generated mission proposal
     */
    @PostMapping("/missions/generate")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.dto.GeneratedMissionResponse> generateMission(
            @RequestBody com.yisy.modules.doramcu.captainslog.dto.GenerateMissionRequest request) {
        log.info("POST /api/v1/captains-log/missions/generate - character: {} type: {} theme: {}",
                request.getCharacterId(), request.getType(), request.getTheme());
        com.yisy.modules.doramcu.captainslog.dto.GeneratedMissionResponse response =
                captainLogService.generateMissionWithAI(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Uses AI to generate a mission and immediately saves it to the database.
     * <p>
     * Combines mission generation and creation in a single call.
     * The Act 1 prompt is automatically set as the opening log.
     * </p>
     *
     * @param request the generation request containing character ID and optional constraints
     * @return the saved mission
     */
    @PostMapping("/missions/generate-and-create")
    public ResponseEntity<CaptainLogMission> generateAndCreateMission(
            @RequestBody com.yisy.modules.doramcu.captainslog.dto.GenerateMissionRequest request) {
        log.info("POST /api/v1/captains-log/missions/generate-and-create - character: {} type: {} theme: {}",
                request.getCharacterId(), request.getType(), request.getTheme());
        CaptainLogMission mission = captainLogService.generateAndCreateMission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(mission);
    }

    // ==================== Combat Endpoints ====================

    /**
     * Starts a new combat encounter (physical or starship).
     */
    @PostMapping("/combat/start")
    public ResponseEntity<CaptainLogCombat> startCombat(@RequestBody StartCombatRequest request) {
        log.info("POST /api/v1/captains-log/combat/start - opponent: {} type: {}",
                request.getOpponentName(), request.getType());
        CaptainLogCombat combat = captainLogService.startCombat(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(combat);
    }

    /**
     * Retrieves a combat by ID.
     */
    @GetMapping("/combat/{id}")
    public ResponseEntity<CaptainLogCombat> getCombat(@PathVariable Long id) {
        return captainLogService.getCombat(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Lists all combats for a mission.
     */
    @GetMapping("/combat")
    public ResponseEntity<List<CaptainLogCombat>> listCombats(@RequestParam Long missionId) {
        return ResponseEntity.ok(captainLogService.listCombats(missionId));
    }

    /**
     * Runs a single combat round.
     * The character performs a task attempt. Success deals 1 hit to the opponent;
     * failure deals 1 hit to the character. Combat ends when one side is defeated.
     */
    @PostMapping("/combat/{id}/round")
    public ResponseEntity<CombatRoundResponse> runCombatRound(@PathVariable Long id) {
        log.info("POST /api/v1/captains-log/combat/{}/round", id);
        CombatRoundResponse result = captainLogService.runCombatRound(id);
        return ResponseEntity.ok(result);
    }

    /**
     * Retreats from an active combat.
     */
    @PostMapping("/combat/{id}/retreat")
    public ResponseEntity<CaptainLogCombat> retreatFromCombat(@PathVariable Long id) {
        log.info("POST /api/v1/captains-log/combat/{}/retreat", id);
        CaptainLogCombat combat = captainLogService.retreatFromCombat(id);
        return ResponseEntity.ok(combat);
    }

    // ==================== Lifepath Character Creation Endpoints ====================

    /**
     * Starts a new lifepath character creation session.
     * <p>
     * The player provides the character's name and optionally species and pronouns.
     * The system returns the first lifepath step (Species selection) along with
     * a session ID that must be used for subsequent choices.
     * </p>
     *
     * @param request the character's name and optional species/pronouns
     * @return the first lifepath step with available choices
     */
    @PostMapping("/lifepath/start")
    public ResponseEntity<Object> startLifepath(@RequestBody LifepathStartRequest request) {
        log.info("POST /api/v1/captains-log/lifepath/start - character: {}", request.getName());
        LifepathStepResponse step = captainLogService.startLifepath(request);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("sessionId", step.getSessionId());
        response.put("step", step);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Makes a choice in the current lifepath step.
     * <p>
     * The player selects one choice ID from the available options.
     * The system applies the choice's effects and returns the next step.
     * On the final step (step 8), the character is saved and returned instead of a step.
     * </p>
     *
     * @param sessionId the lifepath session ID (returned from start)
     * @param request the chosen option ID
     * @return the next lifepath step or the final character
     */
    @PostMapping("/lifepath/{sessionId}/choice")
    public ResponseEntity<Object> makeLifepathChoice(@PathVariable long sessionId,
                                                      @RequestBody LifepathChoiceRequest request) {
        log.info("POST /api/v1/captains-log/lifepath/{}/choice - choice: {}", sessionId, request.getChoiceId());
        Object result = captainLogService.makeLifepathChoice(sessionId, request);
        if (result instanceof CaptainLogCharacter) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } else if (result instanceof LifepathStepResponse) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("sessionId", sessionId);
            response.put("step", result);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    // ==================== Crew Management Endpoints ====================

    /**
     * Creates a new crew member for a character.
     */
    @PostMapping("/characters/{characterId}/crew")
    public ResponseEntity<CaptainLogCrewMember> createCrewMember(
            @PathVariable Long characterId, @RequestBody CreateCrewMemberRequest request) {
        log.info("POST /api/v1/captains-log/characters/{}/crew - name: {}", characterId, request.getName());
        CaptainLogCrewMember crewMember = captainLogService.createCrewMember(characterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(crewMember);
    }

    /**
     * Retrieves a crew member by ID.
     */
    @GetMapping("/crew/{id}")
    public ResponseEntity<CaptainLogCrewMember> getCrewMember(@PathVariable Long id) {
        return captainLogService.getCrewMember(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Lists all crew members for a character.
     */
    @GetMapping("/characters/{characterId}/crew")
    public ResponseEntity<List<CaptainLogCrewMember>> listCrewMembers(@PathVariable Long characterId) {
        return ResponseEntity.ok(captainLogService.listCrewMembers(characterId));
    }

    /**
     * Lists only active crew members for a character.
     */
    @GetMapping("/characters/{characterId}/crew/active")
    public ResponseEntity<List<CaptainLogCrewMember>> listActiveCrewMembers(@PathVariable Long characterId) {
        return ResponseEntity.ok(captainLogService.listActiveCrewMembers(characterId));
    }

    /**
     * Deletes a crew member.
     */
    @DeleteMapping("/crew/{id}")
    public ResponseEntity<Void> deleteCrewMember(@PathVariable Long id) {
        log.debug("DELETE /api/v1/captains-log/crew/{}", id);
        captainLogService.deleteCrewMember(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggles a crew member's active status.
     */
    @PostMapping("/crew/{id}/toggle")
    public ResponseEntity<CaptainLogCrewMember> toggleCrewActive(@PathVariable Long id) {
        log.info("POST /api/v1/captains-log/crew/{}/toggle", id);
        CaptainLogCrewMember crewMember = captainLogService.toggleCrewActive(id);
        return ResponseEntity.ok(crewMember);
    }

    // ==================== Career Progression Endpoints ====================

    /**
     * Returns the current career progression status and available upgrades for a character.
     * <p>
     * Shows experience points, rank advancement availability, discipline increases,
     * focus additions, and value management options.
     * </p>
     */
    @GetMapping("/characters/{characterId}/career")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.dto.CareerProgressionResponse> getCareerStatus(
            @PathVariable Long characterId) {
        log.debug("GET /api/v1/captains-log/characters/{}/career", characterId);
        com.yisy.modules.doramcu.captainslog.dto.CareerProgressionResponse status =
                captainLogService.getCareerStatus(characterId);
        return ResponseEntity.ok(status);
    }

    /**
     * Purchases a career upgrade, spending XP from the character.
     * <p>
     * Upgrade types: RANK_ADVANCEMENT, DISCIPLINE_INCREASE, ADD_FOCUS, ADD_VALUE, RETIRE_VALUE.
     * </p>
     */
    @PostMapping("/characters/{characterId}/career/upgrade")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.dto.CareerProgressionResponse> purchaseCareerUpgrade(
            @PathVariable Long characterId,
            @RequestBody com.yisy.modules.doramcu.captainslog.dto.CareerUpgradeRequest request) {
        log.info("POST /api/v1/captains-log/characters/{}/career/upgrade - type: {}", characterId, request.getType());
        request.setCharacterId(characterId);
        com.yisy.modules.doramcu.captainslog.dto.CareerProgressionResponse result =
                captainLogService.purchaseCareerUpgrade(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Returns suggested focus names for a discipline.
     * <p>
     * Useful for guiding the player when adding a new focus via career progression.
     * </p>
     */
    @GetMapping("/career/focuses/{discipline}")
    public ResponseEntity<java.util.List<String>> getSuggestedFocuses(
            @PathVariable com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter.Discipline discipline) {
        log.debug("GET /api/v1/captains-log/career/focuses/{}", discipline);
        java.util.List<String> focuses = captainLogService.getSuggestedFocuses(discipline);
        return ResponseEntity.ok(focuses);
    }

    // ==================== Injury Management Endpoints ====================

    /**
     * Applies damage to a character and updates their injury state.
     * <p>
     * Injury states progress: HEALTHY → STUNNED → INJURED → DOWN.
     * Injured characters suffer threat penalties on task attempts.
     * </p>
     *
     * @param characterId the character to damage
     * @param damage the amount of damage (positive integer)
     */
    @PostMapping("/characters/{characterId}/injury/damage")
    public ResponseEntity<Map<String, Object>> applyDamage(
            @PathVariable Long characterId,
            @RequestParam(defaultValue = "1") int damage) {
        log.info("POST /api/v1/captains-log/characters/{}/injury/damage?damage={}", characterId, damage);
        Map<String, Object> result = captainLogService.applyDamage(characterId, damage);
        return ResponseEntity.ok(result);
    }

    /**
     * Heals a character, reducing their injury state.
     *
     * @param characterId the character to heal
     * @param healing the amount of healing (positive integer)
     */
    @PostMapping("/characters/{characterId}/injury/heal")
    public ResponseEntity<Map<String, Object>> healCharacter(
            @PathVariable Long characterId,
            @RequestParam(defaultValue = "1") int healing) {
        log.info("POST /api/v1/captains-log/characters/{}/injury/heal?healing={}", characterId, healing);
        Map<String, Object> result = captainLogService.healCharacter(characterId, healing);
        return ResponseEntity.ok(result);
    }

    /**
     * Performs a full recovery between missions, resetting injury state to HEALTHY.
     *
     * @param characterId the character to recover
     */
    @PostMapping("/characters/{characterId}/injury/recover")
    public ResponseEntity<Map<String, Object>> fullRecovery(@PathVariable Long characterId) {
        log.info("POST /api/v1/captains-log/characters/{}/injury/recover", characterId);
        Map<String, Object> result = captainLogService.fullRecovery(characterId);
        return ResponseEntity.ok(result);
    }

    // ==================== Equipment Management Endpoints ====================

    /**
     * Creates a new piece of equipment for a character.
     */
    @PostMapping("/characters/{characterId}/equipment")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment> createEquipment(
            @PathVariable Long characterId, @RequestBody com.yisy.modules.doramcu.captainslog.dto.CreateEquipmentRequest request) {
        log.info("POST /api/v1/captains-log/characters/{}/equipment - name: {}", characterId, request.getName());
        com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment equipment =
                captainLogService.createEquipment(characterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(equipment);
    }

    /**
     * Retrieves equipment by ID.
     */
    @GetMapping("/equipment/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment> getEquipment(@PathVariable Long id) {
        return captainLogService.getEquipment(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Lists all equipment for a character.
     */
    @GetMapping("/characters/{characterId}/equipment")
    public ResponseEntity<List<com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment>> listEquipment(@PathVariable Long characterId) {
        return ResponseEntity.ok(captainLogService.listEquipment(characterId));
    }

    /**
     * Lists only active equipment for a character.
     */
    @GetMapping("/characters/{characterId}/equipment/active")
    public ResponseEntity<List<com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment>> listActiveEquipment(@PathVariable Long characterId) {
        return ResponseEntity.ok(captainLogService.listActiveEquipment(characterId));
    }

    /**
     * Deletes equipment.
     */
    @DeleteMapping("/equipment/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        captainLogService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggles equipment active status.
     */
    @PostMapping("/equipment/{id}/toggle")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment> toggleEquipmentActive(@PathVariable Long id) {
        com.yisy.modules.doramcu.captainslog.model.CaptainLogEquipment equipment =
                captainLogService.toggleEquipmentActive(id);
        return ResponseEntity.ok(equipment);
    }

    // ==================== Campaign Management Endpoints ====================

    /**
     * Creates a new campaign for a character.
     */
    @PostMapping("/characters/{characterId}/campaigns")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign> createCampaign(
            @PathVariable Long characterId, @RequestBody com.yisy.modules.doramcu.captainslog.dto.CreateCampaignRequest request) {
        log.info("POST /api/v1/captains-log/characters/{}/campaigns - title: {}", characterId, request.getTitle());
        com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign campaign =
                captainLogService.createCampaign(characterId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(campaign);
    }

    /**
     * Retrieves a campaign by ID.
     */
    @GetMapping("/campaigns/{id}")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign> getCampaign(@PathVariable Long id) {
        return captainLogService.getCampaign(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Lists all campaigns for a character.
     */
    @GetMapping("/characters/{characterId}/campaigns")
    public ResponseEntity<List<com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign>> listCampaigns(@PathVariable Long characterId) {
        return ResponseEntity.ok(captainLogService.listCampaigns(characterId));
    }

    /**
     * Lists only active campaigns for a character.
     */
    @GetMapping("/characters/{characterId}/campaigns/active")
    public ResponseEntity<List<com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign>> listActiveCampaigns(@PathVariable Long characterId) {
        return ResponseEntity.ok(captainLogService.listActiveCampaigns(characterId));
    }

    /**
     * Deletes a campaign.
     */
    @DeleteMapping("/campaigns/{id}")
    public ResponseEntity<Void> deleteCampaign(@PathVariable Long id) {
        captainLogService.deleteCampaign(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Concludes a campaign, marking it as complete.
     */
    @PostMapping("/campaigns/{id}/conclude")
    public ResponseEntity<com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign> concludeCampaign(@PathVariable Long id) {
        com.yisy.modules.doramcu.captainslog.model.CaptainLogCampaign campaign =
                captainLogService.concludeCampaign(id);
        return ResponseEntity.ok(campaign);
    }

    /**
     * Adds a mission to a campaign.
     */
    @PostMapping("/campaigns/{campaignId}/missions/{missionId}")
    public ResponseEntity<CaptainLogMission> addMissionToCampaign(
            @PathVariable Long campaignId, @PathVariable Long missionId) {
        log.info("POST /api/v1/captains-log/campaigns/{}/missions/{}", campaignId, missionId);
        CaptainLogMission mission = captainLogService.addMissionToCampaign(campaignId, missionId);
        return ResponseEntity.ok(mission);
    }

    /**
     * Removes a mission from its campaign.
     */
    @DeleteMapping("/missions/{missionId}/campaign")
    public ResponseEntity<CaptainLogMission> removeMissionFromCampaign(@PathVariable Long missionId) {
        CaptainLogMission mission = captainLogService.removeMissionFromCampaign(missionId);
        return ResponseEntity.ok(mission);
    }

    /**
     * Performs a post-mission debrief — guides the player through reflection questions.
     * <p>
     * Request body contains reflection answers:
     * valuesUsed, valuesChallenged, lessonsLearned, characterGrowth, retireValue, newValue.
     * </p>
     */
    @PostMapping("/missions/{missionId}/debrief")
    public ResponseEntity<Map<String, Object>> debriefMission(
            @PathVariable Long missionId, @RequestBody Map<String, String> reflections) {
        log.info("POST /api/v1/captains-log/missions/{}/debrief", missionId);
        Map<String, Object> result = captainLogService.debriefMission(missionId, reflections);
        return ResponseEntity.ok(result);
    }

}

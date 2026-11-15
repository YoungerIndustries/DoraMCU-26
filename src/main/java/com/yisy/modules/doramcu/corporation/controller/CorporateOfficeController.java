package com.yisy.modules.doramcu.corporation.controller;

import com.yisy.modules.doramcu.corporation.model.AiAssistantAction;
import com.yisy.modules.doramcu.corporation.model.CorporateContract;
import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.model.CorporatePosition;
import com.yisy.modules.doramcu.model.TravellerContractAssignment;
import com.yisy.modules.doramcu.corporation.service.AiAssistantService;
import com.yisy.modules.doramcu.corporation.service.CareerProgressionService;
import com.yisy.modules.doramcu.corporation.service.ContractExecutionService;
import com.yisy.modules.doramcu.corporation.service.CorporationEconomyService;
import com.yisy.modules.doramcu.corporation.service.CorporateOfficeService;
import com.yisy.modules.doramcu.corporation.service.TravellerCorporationService;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for the corporate office kiosk system.
 * <p>
 * Provides endpoints for managing business contracts and AI character hiring
 * within the corporation. The kiosk is a hybrid system: a virtual dashboard
 * accessible via API, backed by a physical corporate office building.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/corporate}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/corporate")
public class CorporateOfficeController {

    private static final Logger log = LoggerFactory.getLogger(CorporateOfficeController.class);

    private final CorporateOfficeService officeService;
    private final ContractExecutionService executionService;
    private final CorporationEconomyService economyService;
    private final AiAssistantService aiAssistantService;
    private final CareerProgressionService careerProgressionService;
    private final TravellerCorporationService travellerCorporationService;

    public CorporateOfficeController(
            CorporateOfficeService officeService,
            ContractExecutionService executionService,
            CorporationEconomyService economyService,
            AiAssistantService aiAssistantService,
            CareerProgressionService careerProgressionService,
            TravellerCorporationService travellerCorporationService) {
        this.officeService = officeService;
        this.executionService = executionService;
        this.economyService = economyService;
        this.aiAssistantService = aiAssistantService;
        this.careerProgressionService = careerProgressionService;
        this.travellerCorporationService = travellerCorporationService;
    }

    // ---- Office State ----

    /**
     * Get the full office state (dashboard).
     *
     * @param corporationId the corporation ID
     * @return office state with contracts, hires, and building info
     */
    @GetMapping("/office/{corporationId}")
    public ResponseEntity<Map<String, Object>> getOfficeState(@PathVariable Long corporationId) {
        log.debug("GET /api/v1/corporate/office/{} - getting office state", corporationId);
        Map<String, Object> state = officeService.getOfficeState(corporationId);
        return ResponseEntity.ok(state);
    }

    // ---- Contract Endpoints ----

    /**
     * Browse available contracts.
     *
     * @param corporationId the corporation ID
     * @param type          optional contract type filter (TRADE, SECURITY, LOGISTICS, MISSION)
     * @return list of available contracts
     */
    @GetMapping("/office/{corporationId}/contracts")
    public ResponseEntity<List<CorporateContract>> browseContracts(
            @PathVariable Long corporationId,
            @RequestParam(required = false) String type) {
        log.debug("GET /api/v1/corporate/office/{}/contracts?type={} - browsing contracts", corporationId, type);

        CorporateContract.ContractType contractType = null;
        if (type != null && !type.isBlank()) {
            try {
                contractType = CorporateContract.ContractType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        List<CorporateContract> contracts = officeService.browseContracts(corporationId, contractType);
        return ResponseEntity.ok(contracts);
    }

    /**
     * Accept a contract.
     *
     * @param corporationId the corporation ID
     * @param contractId    the contract ID
     * @return the accepted contract
     */
    @PostMapping("/office/{corporationId}/contracts/{contractId}/accept")
    public ResponseEntity<Map<String, Object>> acceptContract(
            @PathVariable Long corporationId,
            @PathVariable Long contractId) {
        log.debug("POST /api/v1/corporate/office/{}/contracts/{}/accept - accepting contract", corporationId, contractId);

        try {
            CorporateContract contract = officeService.acceptContract(corporationId, contractId);
            return ResponseEntity.ok(Map.of(
                    "contractId", contract.getId(),
                    "title", contract.getTitle(),
                    "type", contract.getType().name(),
                    "paymentCredits", contract.getPaymentCredits(),
                    "xpReward", contract.getXpReward(),
                    "status", contract.getStatus().name()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Contract acceptance failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Complete a contract.
     *
     * @param contractId the contract ID
     * @return the completed contract
     */
    @PostMapping("/office/contracts/{contractId}/complete")
    public ResponseEntity<Map<String, Object>> completeContract(@PathVariable Long contractId) {
        log.debug("POST /api/v1/corporate/office/contracts/{}/complete - completing contract", contractId);

        try {
            CorporateContract contract = officeService.completeContract(contractId);
            return ResponseEntity.ok(Map.of(
                    "contractId", contract.getId(),
                    "title", contract.getTitle(),
                    "paymentCredits", contract.getPaymentCredits(),
                    "xpReward", contract.getXpReward(),
                    "status", contract.getStatus().name()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Contract completion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Hiring Endpoints ----

    /**
     * Browse available AI characters for hiring.
     *
     * @param corporationId the corporation ID
     * @param position      optional position filter
     * @return list of available AI characters
     */
    @GetMapping("/office/{corporationId}/hiring")
    public ResponseEntity<Map<String, Object>> browseHiring(
            @PathVariable Long corporationId,
            @RequestParam(required = false) String position) {
        log.debug("GET /api/v1/corporate/office/{}/hiring?position={} - browsing hirable AI", corporationId, position);

        CorporatePosition pos = null;
        if (position != null && !position.isBlank()) {
            try {
                pos = CorporatePosition.valueOf(position.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid position: " + position));
            }
        }

        var available = officeService.browseHirableAI(corporationId, pos);
        var activeHires = officeService.getActiveHires(corporationId);

        return ResponseEntity.ok(Map.of(
                "available", available,
                "activeHires", activeHires
        ));
    }

    /**
     * Hire an AI character for a position.
     *
     * @param corporationId the corporation ID
     * @param body          containing aiCharacterId and position
     * @return the hiring record
     */
    @PostMapping("/office/{corporationId}/hiring")
    public ResponseEntity<Map<String, Object>> hireAiCharacter(
            @PathVariable Long corporationId,
            @RequestBody Map<String, Object> body) {
        log.debug("POST /api/v1/corporate/office/{}/hiring - hiring AI character", corporationId);

        Long aiCharacterId = Long.valueOf(body.get("aiCharacterId").toString());
        String positionStr = body.get("position").toString().toUpperCase();

        CorporatePosition position;
        try {
            position = CorporatePosition.valueOf(positionStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid position: " + positionStr));
        }

        try {
            CorporateHiring hiring = officeService.hireAiCharacter(corporationId, aiCharacterId, position);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "hiringId", hiring.getId(),
                    "aiCharacterId", hiring.getAiCharacterId(),
                    "position", hiring.getPosition().getDisplayName(),
                    "status", hiring.getStatus().name(),
                    "dailySalary", hiring.getDailySalary()
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Hiring failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Fire an AI character (terminate hiring).
     *
     * @param corporationId the corporation ID
     * @param hiringId      the hiring record ID
     * @return success status
     */
    @DeleteMapping("/office/{corporationId}/hiring/{hiringId}")
    public ResponseEntity<Map<String, Object>> fireAiCharacter(
            @PathVariable Long corporationId,
            @PathVariable Long hiringId) {
        log.debug("DELETE /api/v1/corporate/office/{}/hiring/{} - firing AI character", corporationId, hiringId);

        try {
            officeService.fireAiCharacter(hiringId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "AI character fired successfully"
            ));
        } catch (IllegalArgumentException e) {
            log.warn("Firing failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Building Endpoints ----

    /**
     * Construct or upgrade the office building.
     *
     * @param corporationId the corporation ID
     * @param body          containing planetId
     * @return the new building level
     */
    @PostMapping("/office/{corporationId}/building")
    public ResponseEntity<Map<String, Object>> constructOfficeBuilding(
            @PathVariable Long corporationId,
            @RequestBody Map<String, Object> body) {
        log.debug("POST /api/v1/corporate/office/{}/building - constructing office building", corporationId);

        Long planetId = Long.valueOf(body.getOrDefault("planetId", "1").toString());
        int newLevel = officeService.constructOfficeBuilding(corporationId, planetId);

        return ResponseEntity.ok(Map.of(
                "corporationId", corporationId,
                "planetId", planetId,
                "buildingLevel", newLevel,
                "contractSlotBonus", newLevel * 2,
                "hiringCostDiscount", newLevel * 0.1
        ));
    }

    // ---- Sample Data Generation ----

    /**
     * Generate sample contracts for testing/demo.
     *
     * @param corporationId the corporation ID
     * @return list of generated contracts
     */
    @PostMapping("/office/{corporationId}/contracts/generate")
    public ResponseEntity<List<CorporateContract>> generateSampleContracts(@PathVariable Long corporationId) {
        log.debug("POST /api/v1/corporate/office/{}/contracts/generate - generating sample contracts", corporationId);
        List<CorporateContract> contracts = officeService.generateSampleContracts(corporationId);
        return ResponseEntity.ok(contracts);
    }

    /**
     * Browse available contracts filtered by universe.
     *
     * @param gameType the game type to filter by
     * @return list of available contracts from the specified universe
     */
    @GetMapping("/contracts/universe/{gameType}")
    public ResponseEntity<List<CorporateContract>> browseContractsByUniverse(@PathVariable String gameType) {
        log.debug("GET /api/v1/corporate/contracts/universe/{} - browsing contracts by universe", gameType);
        try {
            com.yisy.modules.doramcu.gamecore.GameType gt = com.yisy.modules.doramcu.gamecore.GameType.valueOf(gameType.toUpperCase());
            List<CorporateContract> contracts = officeService.browseContractsByUniverse(gt);
            return ResponseEntity.ok(contracts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Generate new contracts for a specific universe.
     *
     * @param gameType the game type to generate contracts for
     * @param count    number of contracts to generate (default 5)
     * @return list of generated contracts
     */
    @PostMapping("/contracts/universe/{gameType}/generate")
    public ResponseEntity<List<CorporateContract>> generateContractsByUniverse(
            @PathVariable String gameType,
            @RequestParam(defaultValue = "5") int count) {
        log.debug("POST /api/v1/corporate/contracts/universe/{}/generate?count={} - generating contracts", gameType, count);
        try {
            com.yisy.modules.doramcu.gamecore.GameType gt = com.yisy.modules.doramcu.gamecore.GameType.valueOf(gameType.toUpperCase());
            List<CorporateContract> contracts = officeService.generateContractsByUniverse(gt, count);
            return ResponseEntity.ok(contracts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Generate new contracts from all universes.
     *
     * @param perType number of contracts per universe (default 3)
     * @return list of generated contracts
     */
    @PostMapping("/contracts/generate-all")
    public ResponseEntity<List<CorporateContract>> generateAllUniverseContracts(
            @RequestParam(defaultValue = "3") int perType) {
        log.debug("POST /api/v1/corporate/contracts/generate-all?perType={} - generating contracts from all universes", perType);
        List<CorporateContract> contracts = officeService.generateAllUniverseContracts(perType);
        return ResponseEntity.ok(contracts);
    }

    // ---- Finance Endpoints ----

    /**
     * Get financial report for a corporation.
     *
     * @param corporationId the corporation ID
     * @return financial report with balance, revenue, expenses, and net profit
     */
    @GetMapping("/office/{corporationId}/finance")
    public ResponseEntity<Map<String, Object>> getFinancialReport(@PathVariable Long corporationId) {
        log.debug("GET /api/v1/corporate/office/{}/finance - getting financial report", corporationId);

        try {
            Map<String, Object> report = economyService.getFinancialReport(corporationId);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            log.warn("Financial report failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get transaction history for a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of financial transactions
     */
    @GetMapping("/office/{corporationId}/finance/transactions")
    public ResponseEntity<List<CorporateFinanceTransaction>> getTransactionHistory(@PathVariable Long corporationId) {
        log.debug("GET /api/v1/corporate/office/{}/finance/transactions - getting transaction history", corporationId);

        try {
            List<CorporateFinanceTransaction> transactions = economyService.getTransactionHistory(corporationId);
            return ResponseEntity.ok(transactions);
        } catch (IllegalArgumentException e) {
            log.warn("Transaction history failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ---- Contract Execution Endpoints ----

    /**
     * Advance a contract by one turn.
     *
     * @param contractId the contract ID
     * @return contract progress information
     */
    @PostMapping("/office/contracts/{contractId}/process-turn")
    public ResponseEntity<Map<String, Object>> processTurn(@PathVariable Long contractId) {
        log.debug("POST /api/v1/corporate/office/contracts/{}/process-turn - advancing contract", contractId);

        try {
            Map<String, Object> progress = executionService.processTurn(contractId);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            log.warn("Process turn failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get detailed contract progress information.
     *
     * @param contractId the contract ID
     * @return contract status with crew and success estimate
     */
    @GetMapping("/office/contracts/{contractId}/status")
    public ResponseEntity<Map<String, Object>> getContractStatus(@PathVariable Long contractId) {
        log.debug("GET /api/v1/corporate/office/contracts/{}/status - getting contract progress", contractId);

        try {
            Map<String, Object> status = executionService.getContractStatus(contractId);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            log.warn("Contract status failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get assigned crew for a contract.
     *
     * @param contractId the contract ID
     * @return list of crew members with details
     */
    @GetMapping("/office/contracts/{contractId}/crew")
    public ResponseEntity<List<Map<String, Object>>> getContractCrew(@PathVariable Long contractId) {
        log.debug("GET /api/v1/corporate/office/contracts/{}/crew - getting assigned crew", contractId);

        try {
            List<Map<String, Object>> crew = executionService.getContractCrew(contractId);
            return ResponseEntity.ok(crew);
        } catch (IllegalArgumentException e) {
            log.warn("Contract crew query failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ---- AI Assistant Endpoints ----

    /**
     * Trigger the AI management cycle for a player's corporation.
     * <p>
     * The AI assistant processes all active contracts, accepts new contracts,
     * hires crew when needed, and fires underperforming crew — all based on
     * the player's cloned personality.
     * </p>
     *
     * @param body request body containing playerId and corporationId
     * @return summary of actions taken by the AI assistant
     */
    @PostMapping("/ai/turn")
    public ResponseEntity<Map<String, Object>> triggerAiTurn(@RequestBody Map<String, Object> body) {
        Long playerId = Long.valueOf(body.get("playerId").toString());
        Long corporationId = Long.valueOf(body.get("corporationId").toString());

        log.info("POST /api/v1/corporate/ai/turn - triggering AI management cycle for player {} (corp {})",
                playerId, corporationId);

        try {
            Map<String, Object> summary = aiAssistantService.runManagementCycle(playerId, corporationId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("AI management cycle failed for player {}: {}", playerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get recent AI assistant actions for a player.
     *
     * @param playerId the player ID
     * @param limit    maximum number of actions to return (default 20)
     * @return list of recent AI actions
     */
    @GetMapping("/ai/actions")
    public ResponseEntity<List<AiAssistantAction>> getAiActions(
            @RequestParam Long playerId,
            @RequestParam(defaultValue = "20") int limit) {
        log.debug("GET /api/v1/corporate/ai/actions?playerId={}&limit={} - getting AI actions",
                playerId, limit);

        List<AiAssistantAction> actions = aiAssistantService.getRecentActions(playerId, limit);
        return ResponseEntity.ok(actions);
    }

    /**
     * Check if the AI assistant is active for a player.
     *
     * @param playerId the player ID
     * @return whether the AI assistant is active (player in VP mode)
     */
    @GetMapping("/ai/status")
    public ResponseEntity<Map<String, Object>> getAiStatus(@RequestParam Long playerId) {
        log.debug("GET /api/v1/corporate/ai/status?playerId={} - checking AI assistant status", playerId);

        boolean active = aiAssistantService.isAssistantActive(playerId);
        return ResponseEntity.ok(Map.of(
                "playerId", playerId,
                "assistantActive", active
        ));
    }

    // ---- Career Progression Endpoints ----

    /**
     * Promote a hire to the next career level.
     *
     * @param hiringId the hiring record ID
     * @return updated career details
     */
    @PostMapping("/crew/{hiringId}/promote")
    public ResponseEntity<Map<String, Object>> promoteHire(@PathVariable Long hiringId) {
        log.debug("POST /api/v1/corporate/crew/{}/promote - promoting hire", hiringId);

        try {
            var hireOpt = careerProgressionService.promote(hiringId);
            if (hireOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Hire not found or not eligible for promotion"));
            }
            var details = careerProgressionService.getCareerDetails(hiringId);
            return details.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.warn("Promotion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Demote a hire to the previous career level.
     *
     * @param hiringId the hiring record ID
     * @return updated career details
     */
    @PostMapping("/crew/{hiringId}/demote")
    public ResponseEntity<Map<String, Object>> demoteHire(@PathVariable Long hiringId) {
        log.debug("POST /api/v1/corporate/crew/{}/demote - demoting hire", hiringId);

        try {
            var hireOpt = careerProgressionService.demote(hiringId);
            if (hireOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Hire not found or already at minimum level"));
            }
            var details = careerProgressionService.getCareerDetails(hiringId);
            return details.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.warn("Demotion failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get career details for a hire.
     *
     * @param hiringId the hiring record ID
     * @return career details with level, XP, skills, and promotion eligibility
     */
    @GetMapping("/crew/{hiringId}/career")
    public ResponseEntity<Map<String, Object>> getCareerDetails(@PathVariable Long hiringId) {
        log.debug("GET /api/v1/corporate/crew/{}/career - getting career details", hiringId);

        try {
            var details = careerProgressionService.getCareerDetails(hiringId);
            return details.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.warn("Career details failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ---- Traveller Character Endpoints ----

    /**
     * Get contracts that a Traveller character qualifies for based on their skills.
     *
     * @param characterId the Traveller character ID
     * @return list of qualified contracts
     */
    @GetMapping("/office/character/qualified")
    public ResponseEntity<List<Map<String, Object>>> getQualifiedContracts(@RequestParam Integer characterId) {
        log.debug("GET /api/v1/corporate/office/character/qualified?characterId={} - getting qualified contracts", characterId);

        TravellerCharacter character = travellerCorporationService.getCharacterService().getCharacter(characterId);
        if (character == null) {
            return ResponseEntity.notFound().build();
        }

        List<CorporateContract> qualified = travellerCorporationService.getQualifiedContracts(character);
        List<Map<String, Object>> resultList = new java.util.ArrayList<>();
        for (CorporateContract contract : qualified) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", contract.getId());
            map.put("title", contract.getTitle());
            map.put("type", contract.getType().name());
            map.put("payment", contract.getPaymentCredits());
            map.put("xpReward", contract.getXpReward());
            map.put("difficulty", contract.getDifficulty());
            map.put("durationTurns", contract.getDurationTurns());
            
            // Calculate character bonus based on abilities matching contract type
            double bonus = 0.0;
            for (com.yisy.modules.doramcu.traveller.AbilityScore ability : com.yisy.modules.doramcu.traveller.AbilityScore.values()) {
                int score = character.getAbility(ability);
                if (score > 10) {
                    bonus += (score - 10) * 0.5;
                }
            }
            // Round to 1 decimal place
            bonus = Math.round(bonus * 10.0) / 10.0;
            map.put("characterBonus", bonus);
            
            // Determine qualification reason
            String reason = travellerCorporationService.getQualificationReason(character, contract);
            map.put("qualificationReason", reason);
            
            resultList.add(map);
        }
        return ResponseEntity.ok(resultList);
    }

    /**
     * Assign a Traveller character to work on a contract.
     *
     * @param characterId the Traveller character ID
     * @param contractId  the contract ID
     * @return the created assignment
     */
    @PostMapping("/office/character/{characterId}/assign")
    public ResponseEntity<Map<String, Object>> assignCharacterToContract(
            @PathVariable Integer characterId,
            @RequestParam Long contractId) {
        log.info("POST /api/v1/corporate/office/character/{}/assign?contractId={} - assigning character to contract",
                characterId, contractId);

        try {
            var assignment = travellerCorporationService.assignCharacterToContract(characterId, contractId);
            return ResponseEntity.ok(Map.of(
                    "assignmentId", assignment.getId(),
                    "characterId", assignment.getTravellerCharacterId(),
                    "contractId", assignment.getContractId(),
                    "assignedAt", assignment.getAssignedAt().toString()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get corporation stats for a Traveller character.
     *
     * @param characterId the Traveller character ID
     * @return character corporation stats
     */
    @GetMapping("/office/character/{characterId}/stats")
    public ResponseEntity<Map<String, Object>> getCharacterStats(@PathVariable Integer characterId) {
        log.debug("GET /api/v1/corporate/office/character/{}/stats - getting character stats", characterId);

        try {
            Map<String, Object> stats = travellerCorporationService.getCharacterStats(characterId);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            log.warn("Character {} not found, returning default stats", characterId);
            // Return default empty stats instead of 404 so the dashboard can still load
            Map<String, Object> defaultStats = new java.util.HashMap<>();
            defaultStats.put("characterId", characterId);
            defaultStats.put("contractsCompleted", 0);
            defaultStats.put("contractsInProgress", 0);
            defaultStats.put("totalEarnings", 0);
            defaultStats.put("credits", 0);
            defaultStats.put("reputation", 0);
            defaultStats.put("skills", new java.util.ArrayList<>());
            defaultStats.put("careers", new java.util.ArrayList<>());
            defaultStats.put("assignments", new java.util.ArrayList<>());
            return ResponseEntity.ok(defaultStats);
        }
    }

    /**
     * Get abilities for a Traveller character.
     * Returns default empty abilities if character not found.
     *
     * @param characterId the Traveller character ID
     * @return character abilities
     */
    @GetMapping("/office/character/{characterId}/abilities")
    public ResponseEntity<Map<String, Object>> getCharacterAbilities(@PathVariable Integer characterId) {
        log.debug("GET /api/v1/corporate/office/character/{}/abilities - getting character abilities", characterId);

        TravellerCharacter character = travellerCorporationService.getCharacterService().getCharacter(characterId);
        if (character == null) {
            log.warn("Character {} not found, returning default abilities", characterId);
            Map<String, Object> defaultAbilities = new java.util.HashMap<>();
            defaultAbilities.put("dex", 0);
            defaultAbilities.put("st", 0);
            defaultAbilities.put("end", 0);
            defaultAbilities.put("int", 0);
            defaultAbilities.put("edu", 0);
            defaultAbilities.put("soc", 0);
            defaultAbilities.put("app", 0);
            return ResponseEntity.ok(defaultAbilities);
        }

        Map<String, Object> abilities = new java.util.HashMap<>();
        abilities.put("dex", character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.DEX));
        abilities.put("st", character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.ST));
        abilities.put("end", character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.END));
        abilities.put("int", character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.INT));
        abilities.put("edu", character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.EDU));
        abilities.put("soc", character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.SOC));
        abilities.put("app", character.getAbility(com.yisy.modules.doramcu.traveller.AbilityScore.APP));
        return ResponseEntity.ok(abilities);
    }

    /**
     * Get skills for a Traveller character.
     * Returns default empty skills if character not found.
     *
     * @param characterId the Traveller character ID
     * @return character skills
     */
    @GetMapping("/office/character/{characterId}/skills")
    public ResponseEntity<List<Object>> getCharacterSkills(@PathVariable Integer characterId) {
        log.debug("GET /api/v1/corporate/office/character/{}/skills - getting character skills", characterId);

        TravellerCharacter character = travellerCorporationService.getCharacterService().getCharacter(characterId);
        if (character == null) {
            log.warn("Character {} not found, returning default skills", characterId);
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }

        List<Object> skills = new java.util.ArrayList<>();
        for (com.yisy.modules.doramcu.traveller.Skill skill : character.getSkills()) {
            Map<String, Object> skillMap = new java.util.HashMap<>();
            skillMap.put("name", skill.getName());
            skillMap.put("category", skill.getCategory().name());
            skillMap.put("level", 1);
            skillMap.put("description", skill.getDescription());
            skills.add(skillMap);
        }
        return ResponseEntity.ok(skills);
    }

    /**
     * Get career history for a Traveller character.
     * Returns default empty career if character not found.
     *
     * @param characterId the Traveller character ID
     * @return character career history
     */
    @GetMapping("/office/character/{characterId}/career")
    public ResponseEntity<List<Object>> getCharacterCareer(@PathVariable Integer characterId) {
        log.debug("GET /api/v1/corporate/office/character/{}/career - getting character career", characterId);

        TravellerCharacter character = travellerCorporationService.getCharacterService().getCharacter(characterId);
        if (character == null) {
            log.warn("Character {} not found, returning default career", characterId);
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }

        List<Object> careerTours = new java.util.ArrayList<>();
        int tourNumber = 1;
        for (TravellerCharacter.CareerTour tour : character.getCareerTours()) {
            Map<String, Object> tourMap = new java.util.HashMap<>();
            tourMap.put("tourNumber", tourNumber++);
            tourMap.put("career", tour.getCareer().getDisplayName());
            tourMap.put("rank", tour.getRank());
            tourMap.put("termLength", tour.getYearsServed());
            tourMap.put("discharges", tour.getRetirementType().getName());
            careerTours.add(tourMap);
        }
        return ResponseEntity.ok(careerTours);
    }

    /**
     * Get assignments for a Traveller character.
     * Returns default empty assignments if character not found.
     *
     * @param characterId the Traveller character ID
     * @return character assignments
     */
    @GetMapping("/office/character/{characterId}/assignments")
    public ResponseEntity<List<Object>> getCharacterAssignments(@PathVariable Integer characterId) {
        log.debug("GET /api/v1/corporate/office/character/{}/assignments - getting character assignments", characterId);

        TravellerCharacter character = travellerCorporationService.getCharacterService().getCharacter(characterId);
        if (character == null) {
            log.warn("Character {} not found, returning default assignments", characterId);
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }

        List<Object> assignments = new java.util.ArrayList<>();
        List<TravellerContractAssignment> charAssignments = travellerCorporationService.getAssignmentsForCharacter(characterId);
        for (TravellerContractAssignment assignment : charAssignments) {
            Optional<CorporateContract> contract = travellerCorporationService.getContractRepository().findById(assignment.getContractId());
            if (contract.isPresent()) {
                Map<String, Object> assignmentMap = new java.util.HashMap<>();
                assignmentMap.put("id", assignment.getId());
                assignmentMap.put("contractId", assignment.getContractId());
                assignmentMap.put("contractTitle", contract.get().getTitle());
                assignmentMap.put("assignedAt", assignment.getAssignedAt() != null ? assignment.getAssignedAt().toString() : "");
                assignmentMap.put("xpEarned", assignment.getXpEarned() != null ? assignment.getXpEarned() : 0);
                assignmentMap.put("status", contract.get().getStatus().name());
                assignments.add(assignmentMap);
            }
        }
        return ResponseEntity.ok(assignments);
    }
}

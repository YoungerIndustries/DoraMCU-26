package com.yisy.modules.doramcu.controller;

import com.yisy.modules.doramcu.hex.Hex3D;
import com.yisy.modules.doramcu.hex.planet.*;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for the Business Tycoon campaign mode.
 * <p>
 * Provides endpoints for:
 * <ul>
 *   <li>Starting a new campaign</li>
 *   <li>Getting campaign state and summaries</li>
 *   <li>Processing turns</li>
 *   <li>Managing trade routes</li>
 *   <li>Viewing market share and objectives</li>
 *   <li>Managing contracts</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/campaigns")
public class TransportCampaignController {

    /** In-memory campaign storage (would be persisted in production) */
    private final Map<String, TransportCampaignManager> campaigns = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Start a new Business Tycoon campaign.
     *
     * @param request the campaign creation request
     * @return the created campaign ID and initial state
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> startCampaign(@RequestBody CampaignRequest request) {
        TransportCampaignManager manager = new TransportCampaignManager.Builder()
                .campaignName(request.getCampaignName() != null ? request.getCampaignName() : "Business Tycoon")
                .playerCorporation(new TransportCorporation(
                        request.getCorporationName() != null ? request.getCorporationName() : "Player Transport",
                        "player",
                        request.getStartingCredits() != null ? request.getStartingCredits() : 5000))
                .maxTurns(request.getMaxTurns() != null ? request.getMaxTurns() : 100)
                .winMarketSharePercentage(request.getWinMarketShare() != null ? request.getWinMarketShare() : 60)
                .winSustainTurns(request.getWinSustainTurns() != null ? request.getWinSustainTurns() : 3)
                .build();

        // Add AI rivals
        if (request.getAiRivals() != null) {
            for (AiRivalRequest rival : request.getAiRivals()) {
                AiCorporationStrategy.AiPersonality personality =
                        AiCorporationStrategy.AiPersonality.valueOf(
                                rival.getPersonality() != null ? rival.getPersonality() : "BALANCED");
                manager.addAiRival(rival.getName(), personality, rival.getSeed() != null ? rival.getSeed() : System.nanoTime());
            }
        }

        campaigns.put(manager.getCampaignId(), manager);

        return ResponseEntity.ok(Map.of(
                "campaignId", manager.getCampaignId(),
                "state", toStateMap(manager)
        ));
    }

    /**
     * Get the current campaign state.
     *
     * @param campaignId the campaign ID
     * @return the campaign state summary
     */
    @GetMapping("/{campaignId}")
    public ResponseEntity<Map<String, Object>> getCampaignState(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toStateMap(manager));
    }

    /**
     * Process one turn of the campaign.
     *
     * @param campaignId the campaign ID
     * @return the turn summary
     */
    @PostMapping("/{campaignId}/turn")
    public ResponseEntity<Map<String, Object>> processTurn(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }
        if (manager.isCampaignEnded()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Campaign has already ended"));
        }

        TransportCampaignManager.TurnSummary summary = manager.processTurn();
        return ResponseEntity.ok(Map.of(
                "turn", summary.turn,
                "totalCargoValue", summary.totalCargoValue,
                "playerMarketShare", summary.playerMarketShare,
                "playerCredits", summary.playerCredits,
                "campaignEnded", manager.isCampaignEnded(),
                "result", manager.getCampaignResult()
        ));
    }

    /**
     * Get the market share leaderboard.
     *
     * @param campaignId the campaign ID
     * @return the leaderboard
     */
    @GetMapping("/{campaignId}/market-share")
    public ResponseEntity<List<Map<String, Object>>> getMarketShare(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> leaderboard = manager.getMarketShareLeaderboard().stream()
                .map(entry -> {
                    TransportCorporation corp = manager.getCorporation(entry.corporationId).orElse(null);
                    Map<String, Object> m = new HashMap<>();
                    m.put("corporationId", entry.corporationId);
                    m.put("name", corp != null ? corp.getName() : "Unknown");
                    m.put("cargoValue", entry.cargoValue);
                    m.put("percentage", entry.percentage);
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(leaderboard);
    }

    /**
     * Get the campaign objectives.
     *
     * @param campaignId the campaign ID
     * @return the objectives
     */
    @GetMapping("/{campaignId}/objectives")
    public ResponseEntity<Map<String, Object>> getObjectives(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> active = manager.getObjectives().stream()
                .map(this::toObjectiveMap)
                .collect(Collectors.toList());
        List<Map<String, Object>> completed = manager.getCompletedObjectives().stream()
                .map(this::toObjectiveMap)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "active", active,
                "completed", completed
        ));
    }

    /**
     * Get all corporations in the campaign.
     *
     * @param campaignId the campaign ID
     * @return the corporations
     */
    @GetMapping("/{campaignId}/corporations")
    public ResponseEntity<List<Map<String, Object>>> getCorporations(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> corps = manager.getAllCorporations().stream()
                .map(corp -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", corp.getId());
                    m.put("name", corp.getName());
                    m.put("rank", corp.getRank().getTitle());
                    m.put("credits", corp.getCredits());
                    m.put("reputation", corp.getReputation());
                    m.put("assets", corp.getTotalAssetCount());
                    m.put("marketShare", manager.getMarketShareTracker().getMarketSharePercentage(corp.getId()));
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(corps);
    }

    /**
     * Get all trade routes in the campaign.
     *
     * @param campaignId the campaign ID
     * @return the trade routes
     */
    @GetMapping("/{campaignId}/routes")
    public ResponseEntity<List<Map<String, Object>>> getRoutes(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> routes = manager.getTradeRouteManager().getAllRoutes().stream()
                .map(route -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", route.getId());
                    m.put("owner", route.getOwnerCorporationId());
                    m.put("type", route.getType());
                    m.put("origin", route.getOriginId());
                    m.put("destination", route.getDestinationId());
                    m.put("cargoType", route.getCargoType().getDisplayName());
                    m.put("active", route.isActive());
                    m.put("profitPerTurn", route.getNetProfitPerTurn());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(routes);
    }

    /**
     * Get available contracts.
     *
     * @param campaignId the campaign ID
     * @return the available contracts
     */
    @GetMapping("/{campaignId}/contracts")
    public ResponseEntity<List<Map<String, Object>>> getContracts(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        List<Map<String, Object>> contracts = manager.getContractManager().getAvailableContracts(0).stream()
                .map(contract -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", contract.getId());
                    m.put("type", contract.getType());
                    m.put("title", contract.getTitle());
                    m.put("payment", contract.getPaymentCredits());
                    m.put("difficulty", contract.getDifficulty());
                    m.put("duration", contract.getDurationTurns());
                    return m;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(contracts);
    }

    /**
     * Accept a contract.
     *
     * @param campaignId  the campaign ID
     * @param contractId  the contract ID
     * @return the result
     */
    @PostMapping("/{campaignId}/contracts/{contractId}/accept")
    public ResponseEntity<Map<String, String>> acceptContract(
            @PathVariable String campaignId,
            @PathVariable String contractId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        String playerCorpId = manager.getPlayerCorporation().getId();
        boolean accepted = manager.getContractManager().acceptContract(contractId, playerCorpId);

        if (accepted) {
            manager.getPlayerCorporation().addContract(contractId);
            return ResponseEntity.ok(Map.of("status", "accepted"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Contract not available"));
    }

    /**
     * Update objective progress.
     *
     * @param campaignId  the campaign ID
     * @param objectiveId the objective ID
     * @param amount      the progress amount
     * @return the result
     */
    @PostMapping("/{campaignId}/objectives/{objectiveId}/progress")
    public ResponseEntity<Map<String, Object>> updateObjectiveProgress(
            @PathVariable String campaignId,
            @PathVariable String objectiveId,
            @RequestBody Map<String, Integer> request) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        int amount = request.getOrDefault("amount", 1);
        boolean completed = manager.updateObjectiveProgress(objectiveId, amount);

        return ResponseEntity.ok(Map.of(
                "completed", completed,
                "objectiveId", objectiveId
        ));
    }

    /**
     * Get empire reputation with all AI empires.
     *
     * @param campaignId the campaign ID
     * @return reputation map
     */
    @GetMapping("/{campaignId}/reputation")
    public ResponseEntity<Map<String, Object>> getEmpireReputation(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        EmpireReputationTracker tracker = manager.getReputationTracker();
        Map<Integer, Integer> reputations = tracker.getAllReputations();

        Map<String, Object> result = new HashMap<>();
        Map<String, Object> empireReputations = new HashMap<>();
        for (Map.Entry<Integer, Integer> entry : reputations.entrySet()) {
            int empireId = entry.getKey();
            int rep = entry.getValue();
            GalaxyOverlordAI overlord = manager.getGalaxyOverlords().get(empireId);
            String empireName = overlord != null ? overlord.getEmpireName() : "Unknown";
            EmpireReputationTracker.ReputationCategory category = tracker.getCategory(empireId);
            empireReputations.put(String.valueOf(empireId), Map.of(
                    "empireId", empireId,
                    "empireName", empireName,
                    "reputation", rep,
                    "category", category.getLabel(),
                    "categoryDescription", category.getDescription(),
                    "tariffDiscount", tracker.getTariffDiscount(empireId),
                    "canAccessPlanet", tracker.canAccessPlanet(empireId),
                    "hasFreeTradeAccess", tracker.hasFreeTradeAccess(empireId)
            ));
        }
        result.put("reputations", empireReputations);
        return ResponseEntity.ok(result);
    }

    /**
     * Get trade policies for all AI empires.
     *
     * @param campaignId the campaign ID
     * @return trade policies map
     */
    @GetMapping("/{campaignId}/trade-policies")
    public ResponseEntity<Map<String, Object>> getTradePolicies(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> policies = new HashMap<>();
        for (GalaxyOverlordAI overlord : manager.getGalaxyOverlords().values()) {
            EmpireTradePolicy policy = overlord.getTradePolicy();
            policies.put(String.valueOf(overlord.getEmpireId()), Map.of(
                    "empireId", overlord.getEmpireId(),
                    "empireName", overlord.getEmpireName(),
                    "tariffPercentage", policy.getTariffPercentage(),
                    "isWarZone", policy.isWarZone(),
                    "warZoneRiskMultiplier", policy.getWarZoneRiskMultiplier(),
                    "embargoedCargo", policy.getEmbargoedCargo(),
                    "freeTradePlanets", policy.getFreeTradePlanets()
            ));
        }
        return ResponseEntity.ok(Map.of("policies", policies));
    }

    /**
     * Get available RPG quests.
     *
     * @param campaignId the campaign ID
     * @return list of available quests
     */
    @GetMapping("/{campaignId}/quests")
    public ResponseEntity<Map<String, Object>> getAvailableQuests(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        List<RpgQuest> available = manager.getQuestSystem().getAvailableQuests(
                manager.getPlayerCorporation(), manager.getReputationTracker());
        List<RpgQuest> active = manager.getQuestSystem().getActiveQuests();

        List<Map<String, Object>> availableList = new java.util.ArrayList<>();
        for (RpgQuest quest : available) {
            availableList.add(toQuestMap(quest));
        }
        List<Map<String, Object>> activeList = new java.util.ArrayList<>();
        for (RpgQuest quest : active) {
            activeList.add(toQuestMap(quest));
        }

        return ResponseEntity.ok(Map.of(
                "available", availableList,
                "active", activeList,
                "completedCount", manager.getQuestSystem().getCompletedQuestCount()
        ));
    }

    /**
     * Accept an RPG quest.
     *
     * @param campaignId the campaign ID
     * @param questId the quest ID
     * @return the result
     */
    @PostMapping("/{campaignId}/quests/{questId}/accept")
    public ResponseEntity<Map<String, Object>> acceptQuest(
            @PathVariable String campaignId,
            @PathVariable String questId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        boolean accepted = manager.getQuestSystem().acceptQuest(
                questId, manager.getPlayerCorporation(), manager.getReputationTracker());

        if (accepted) {
            manager.getPlayerCorporation().addActiveQuest(questId);
            return ResponseEntity.ok(Map.of("status", "accepted", "questId", questId));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Quest not available or prerequisites not met"));
    }

    /**
     * Add progress to an active quest.
     *
     * @param campaignId the campaign ID
     * @param questId the quest ID
     * @param request the progress amount
     * @return the result
     */
    @PostMapping("/{campaignId}/quests/{questId}/progress")
    public ResponseEntity<Map<String, Object>> addQuestProgress(
            @PathVariable String campaignId,
            @PathVariable String questId,
            @RequestBody Map<String, Integer> request) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        int amount = request.getOrDefault("amount", 1);
        boolean completed = manager.getQuestSystem().addProgress(questId, amount);

        if (completed) {
            manager.getQuestSystem().completeQuest(
                    questId, manager.getPlayerCorporation(), manager.getReputationTracker());
            manager.getPlayerCorporation().completeQuest(questId);
        }

        return ResponseEntity.ok(Map.of(
                "questId", questId,
                "completed", completed
        ));
    }

    /**
     * Get empire relations (alliances and wars).
     *
     * @param campaignId the campaign ID
     * @return empire relations map
     */
    @GetMapping("/{campaignId}/empire-relations")
    public ResponseEntity<Map<String, Object>> getEmpireRelations(@PathVariable String campaignId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> relations = new HashMap<>();
        for (GalaxyOverlordAI overlord : manager.getGalaxyOverlords().values()) {
            Map<String, Object> empireData = new HashMap<>();
            empireData.put("empireId", overlord.getEmpireId());
            empireData.put("empireName", overlord.getEmpireName());
            empireData.put("personality", overlord.getPersonality().getLabel());
            empireData.put("allies", overlord.getAllies());
            empireData.put("enemies", overlord.getEnemies());
            empireData.put("relations", overlord.getRelations());

            List<Map<String, Object>> contracts = new java.util.ArrayList<>();
            for (PlanetContract contract : overlord.getAvailableContracts()) {
                contracts.add(Map.of(
                        "id", contract.getId(),
                        "type", contract.getType().getLabel(),
                        "payment", contract.getPaymentCredits(),
                        "status", contract.getStatus()
                ));
            }
            empireData.put("availableContracts", contracts);

            relations.put(String.valueOf(overlord.getEmpireId()), empireData);
        }
        return ResponseEntity.ok(Map.of("empires", relations));
    }

    /**
     * Get planet surface data for rendering in Unity or web clients.
     * Returns settlements, industries, stations, and vehicles for a given planet.
     *
     * @param campaignId the campaign ID
     * @param planetId the planet ID
     * @return planet surface data
     */
    @GetMapping("/{campaignId}/planet-surface/{planetId}")
    public ResponseEntity<Map<String, Object>> getPlanetSurface(
            @PathVariable String campaignId,
            @PathVariable int planetId) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        // Create a dummy planet with the given ID for lookup
        com.yisy.modules.doramcu.hex.Planet planet = new com.yisy.modules.doramcu.hex.Planet(planetId);

        PlanetSurfaceBuilder builder = manager.getSurfaceBuilder();
        Map<String, Object> result = new HashMap<>();

        // Get settlements for this planet
        List<Map<String, Object>> settlements = new java.util.ArrayList<>();
        for (Settlement s : builder.getAllSettlements(planet)) {
            Map<String, Object> sm = new HashMap<>();
            sm.put("id", s.getId());
            sm.put("name", s.getName());
            Hex3D center = s.getCenterHex();
            sm.put("hexX", center.getX());
            sm.put("hexY", center.getY());
            sm.put("hexZ", center.getZ());
            sm.put("population", s.getPopulation());
            sm.put("size", s.getSize().name());
            sm.put("ownerEmpireId", s.getOwnerEmpireId());
            settlements.add(sm);
        }
        result.put("settlements", settlements);

        // Get industries for this planet
        List<Map<String, Object>> industries = new java.util.ArrayList<>();
        for (Industry i : builder.getAllIndustries(planet)) {
            Map<String, Object> im = new HashMap<>();
            im.put("id", i.getId());
            im.put("type", i.getType().name());
            im.put("label", i.getType().getDisplayName());
            Hex3D loc = i.getLocation();
            im.put("hexX", loc.getX());
            im.put("hexY", loc.getY());
            im.put("hexZ", loc.getZ());
            im.put("productionLevel", i.getProductionLevel());
            im.put("ownerEmpireId", i.getOwnerEmpireId());
            industries.add(im);
        }
        result.put("industries", industries);

        // Get stations for this planet
        List<Map<String, Object>> stations = new java.util.ArrayList<>();
        for (Station s : builder.getStations(planet, null)) {
            Map<String, Object> stm = new HashMap<>();
            stm.put("id", s.getId());
            stm.put("name", s.getName());
            Hex3D loc = s.getLocation();
            stm.put("hexX", loc.getX());
            stm.put("hexY", loc.getY());
            stm.put("hexZ", loc.getZ());
            stm.put("ownerEmpireId", s.getOwnerEmpireId());
            stations.add(stm);
        }
        result.put("stations", stations);

        // Get vehicles for this planet
        List<Map<String, Object>> vehicles = new java.util.ArrayList<>();
        for (SurfaceVehicle v : builder.getVehicles(planet, null)) {
            Map<String, Object> vm = new HashMap<>();
            vm.put("id", v.getId());
            vm.put("type", v.getType().name());
            Hex3D pos = v.getCurrentPosition();
            vm.put("hexX", pos.getX());
            vm.put("hexY", pos.getY());
            vm.put("hexZ", pos.getZ());
            vm.put("cargoCount", v.getCargoInventory().size());
            vm.put("passengerCount", v.getPassengerCount());
            vm.put("ownerEmpireId", v.getOwnerEmpireId());
            vehicles.add(vm);
        }
        result.put("vehicles", vehicles);

        return ResponseEntity.ok(result);
    }

    /**
     * Add a Galaxy Overlord (AI empire) to the campaign.
     *
     * @param campaignId the campaign ID
     * @param request the empire configuration
     * @return the created empire
     */
    @PostMapping("/{campaignId}/empires")
    public ResponseEntity<Map<String, Object>> addGalaxyOverlord(
            @PathVariable String campaignId,
            @RequestBody Map<String, String> request) {
        TransportCampaignManager manager = campaigns.get(campaignId);
        if (manager == null) {
            return ResponseEntity.notFound().build();
        }

        int empireId;
        try {
            empireId = Integer.parseInt(request.getOrDefault("empireId", "0"));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid empire ID"));
        }

        String empireName = request.getOrDefault("empireName", "Unknown Empire");
        String personalityStr = request.getOrDefault("personality", "BALANCED");

        GalaxyOverlordAI.AiPersonality personality;
        try {
            personality = GalaxyOverlordAI.AiPersonality.valueOf(personalityStr);
        } catch (IllegalArgumentException e) {
            personality = GalaxyOverlordAI.AiPersonality.BALANCED;
        }

        GalaxyOverlordAI overlord = manager.addGalaxyOverlord(empireId, empireName, personality);

        return ResponseEntity.ok(Map.of(
                "empireId", overlord.getEmpireId(),
                "empireName", overlord.getEmpireName(),
                "personality", overlord.getPersonality().getLabel()
        ));
    }

    /**
     * Convert a campaign manager to a state map.
     */
    private Map<String, Object> toStateMap(TransportCampaignManager manager) {
        TransportCampaignManager.CampaignStateSummary summary = manager.getCampaignStateSummary();
        Map<String, Object> map = new HashMap<>();
        map.put("campaignName", summary.campaignName);
        map.put("currentTurn", summary.currentTurn);
        map.put("maxTurns", summary.maxTurns);
        map.put("ended", summary.ended);
        map.put("result", summary.result);
        map.put("playerRank", summary.playerRank);
        map.put("playerCredits", summary.playerCredits);
        map.put("playerMarketShare", summary.playerMarketShare);
        map.put("activeObjectives", summary.activeObjectives);
        map.put("completedObjectives", summary.completedObjectives);
        map.put("aiRivalCount", summary.aiRivalCount);
        map.put("totalRoutes", summary.totalRoutes);
        return map;
    }

    /**
     * Convert an objective to a map.
     */
    private Map<String, Object> toObjectiveMap(CampaignObjective objective) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", objective.getId());
        map.put("title", objective.getTitle());
        map.put("description", objective.getDescription());
        map.put("type", objective.getType());
        map.put("currentValue", objective.getCurrentValue());
        map.put("targetValue", objective.getTargetValue());
        map.put("progressPercentage", objective.getProgressPercentage());
        map.put("completed", objective.isCompleted());
        map.put("rewardCredits", objective.getRewardCredits());
        map.put("rewardXp", objective.getRewardXp());
        return map;
    }

    /**
     * Convert a quest to a map.
     */
    private Map<String, Object> toQuestMap(RpgQuest quest) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", quest.getId());
        map.put("title", quest.getTitle());
        map.put("description", quest.getDescription());
        map.put("type", quest.getType().getLabel());
        map.put("issuingEmpireId", quest.getIssuingEmpireId());
        map.put("currentValue", quest.getCurrentValue());
        map.put("targetValue", quest.getTargetValue());
        map.put("progressPercentage", quest.getProgressPercentage());
        map.put("status", quest.getStatus());
        map.put("rewardCredits", quest.getRewardCredits());
        map.put("rewardXp", quest.getRewardXp());
        map.put("rewardReputation", quest.getRewardReputation());
        map.put("turnsRemaining", quest.getTurnsRemaining());
        map.put("maxTurns", quest.getMaxTurns());
        map.put("minReputation", quest.getMinReputation());
        map.put("minRank", quest.getMinRank().getTitle());
        return map;
    }

    /**
     * Request to start a new campaign.
     */
    public static class CampaignRequest {
        private String campaignName;
        private String corporationName;
        private Integer startingCredits;
        private Integer maxTurns;
        private Integer winMarketShare;
        private Integer winSustainTurns;
        private List<AiRivalRequest> aiRivals;

        public String getCampaignName() { return campaignName; }
        public void setCampaignName(String campaignName) { this.campaignName = campaignName; }
        public String getCorporationName() { return corporationName; }
        public void setCorporationName(String corporationName) { this.corporationName = corporationName; }
        public Integer getStartingCredits() { return startingCredits; }
        public void setStartingCredits(Integer startingCredits) { this.startingCredits = startingCredits; }
        public Integer getMaxTurns() { return maxTurns; }
        public void setMaxTurns(Integer maxTurns) { this.maxTurns = maxTurns; }
        public Integer getWinMarketShare() { return winMarketShare; }
        public void setWinMarketShare(Integer winMarketShare) { this.winMarketShare = winMarketShare; }
        public Integer getWinSustainTurns() { return winSustainTurns; }
        public void setWinSustainTurns(Integer winSustainTurns) { this.winSustainTurns = winSustainTurns; }
        public List<AiRivalRequest> getAiRivals() { return aiRivals; }
        public void setAiRivals(List<AiRivalRequest> aiRivals) { this.aiRivals = aiRivals; }
    }

    /**
     * Request for an AI rival configuration.
     */
    public static class AiRivalRequest {
        private String name;
        private String personality;
        private Long seed;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getPersonality() { return personality; }
        public void setPersonality(String personality) { this.personality = personality; }
        public Long getSeed() { return seed; }
        public void setSeed(Long seed) { this.seed = seed; }
    }
}

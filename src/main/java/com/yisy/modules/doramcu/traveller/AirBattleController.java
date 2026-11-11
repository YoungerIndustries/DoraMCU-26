package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.eve.model.AiCommanderExperience;
import com.yisy.modules.doramcu.service.AiCommanderExperienceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API for air battles, campaigns, and aircraft conversion.
 */
@RestController
@RequestMapping("/api/v1/traveller/air")
public class AirBattleController {

    private static final Logger log = LoggerFactory.getLogger(AirBattleController.class);

    private final AirBattleService battleService = new AirBattleService();
    private final AircraftConversionService conversionService = new AircraftConversionService();
    private final TravellerSpacecraftFactory factory = new TravellerSpacecraftFactory();
    private final AirCampaignBookGenerator bookGenerator = new AirCampaignBookGenerator();
    private final Map<String, AirCampaignLog> campaigns = new HashMap<>();
    private final AiCommanderExperienceService experienceService;

    public AirBattleController(AiCommanderExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @PostMapping("/battles")
    public ResponseEntity<?> createBattle(@RequestBody Map<String, Object> request) {
        try {
            String id = request.get("id") != null ? (String) request.get("id") : "battle-" + System.currentTimeMillis();
            String mode = "ABSTRACT";
            String altitude = "MEDIUM";
            if (request.get("mode") != null) mode = (String) request.get("mode");
            if (request.get("altitude") != null) altitude = (String) request.get("altitude");

            List<TravellerSpacecraft> attackers = new ArrayList<>();
            List<TravellerSpacecraft> defenders = new ArrayList<>();

            if (request.get("attackers") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> atkList = (List<Map<String, Object>>) request.get("attackers");
                for (Map<String, Object> a : atkList) {
                    attackers.add(parseSpacecraft(a));
                }
            }
            if (request.get("defenders") != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> defList = (List<Map<String, Object>>) request.get("defenders");
                for (Map<String, Object> d : defList) {
                    defenders.add(parseSpacecraft(d));
                }
            }

            AirBattle battle = battleService.createBattle(id, attackers, defenders,
                    AirBattle.Altitude.valueOf(altitude), AirBattle.BattleMode.valueOf(mode));
            Map<String, Object> result = new HashMap<>();
            result.put("id", id);
            result.put("mode", mode);
            result.put("altitude", altitude);
            result.put("attackers", attackers.size());
            result.put("defenders", defenders.size());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to create air battle", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/battles/{id}/resolve")
    public ResponseEntity<?> resolveBattle(@PathVariable String id, @RequestBody(required = false) Map<String, Object> request) {
        try {
            AirBattleResult result = battleService.resolveBattle(id);

            // Record AI commander experience if commander IDs provided
            Long attackerCommanderId = request != null && request.get("attackerCommanderId") != null
                    ? Long.valueOf(request.get("attackerCommanderId").toString()) : null;
            Long defenderCommanderId = request != null && request.get("defenderCommanderId") != null
                    ? Long.valueOf(request.get("defenderCommanderId").toString()) : null;

            if (experienceService != null) {
                AiCommanderExperience.Outcome expOutcome = mapTravellerOutcome(result.getOutcome());
                if (attackerCommanderId != null) {
                    try {
                        experienceService.recordBattleExperience(attackerCommanderId, "TRAVELLER",
                                "Air Battle", expOutcome, result.getDescription(), null);
                    } catch (Exception e) {
                        log.warn("Failed to record experience for attacker commander: {}", e.getMessage());
                    }
                }
                if (defenderCommanderId != null) {
                    try {
                        experienceService.recordBattleExperience(defenderCommanderId, "TRAVELLER",
                                "Air Battle", expOutcome, result.getDescription(), null);
                    } catch (Exception e) {
                        log.warn("Failed to record experience for defender commander: {}", e.getMessage());
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("battleId", result.getBattleId());
            response.put("outcome", result.getOutcome().name());
            response.put("description", result.getDescription());
            response.put("attackerLosses", result.getAttackerLosses());
            response.put("defenderLosses", result.getDefenderLosses());
            response.put("odds", result.getOdds());
            response.put("diceRoll", result.getDiceRoll());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to resolve air battle", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    AiCommanderExperience.Outcome mapTravellerOutcome(AirBattleResult.Outcome outcome) {
        switch (outcome) {
            case ATTACKER_VICTORY:
            case DEFENDER_VICTORY:
                return AiCommanderExperience.Outcome.VICTORY;
            case PYRRHIC_VICTORY:
                return AiCommanderExperience.Outcome.PYRRHIC_VICTORY;
            case DRAW:
                return AiCommanderExperience.Outcome.STALEMATE;
            case RETREAT:
                return AiCommanderExperience.Outcome.ROUTE;
            default:
                return AiCommanderExperience.Outcome.STALEMATE;
        }
    }

    @GetMapping("/battles/{id}")
    public ResponseEntity<?> getBattle(@PathVariable String id) {
        AirBattle battle = battleService.getBattle(id);
        if (battle == null) return ResponseEntity.notFound().build();
        Map<String, Object> response = new HashMap<>();
        response.put("id", battle.getId());
        response.put("state", battle.getState().name());
        response.put("mode", battle.getMode().name());
        response.put("altitude", battle.getAltitude().name());
        response.put("round", battle.getCurrentRound());
        response.put("attackers", battle.getAttackerUnits().size());
        response.put("defenders", battle.getDefenderUnits().size());
        if (battle.getResult() != null) {
            response.put("result", battle.getResult().getOutcome().name());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/battles/{id}/events")
    public ResponseEntity<?> getBattleEvents(@PathVariable String id) {
        AirBattle battle = battleService.getBattle(id);
        if (battle == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("events", battle.getEvents()));
    }

    @PostMapping("/campaigns")
    public ResponseEntity<?> createCampaign(@RequestBody Map<String, String> request) {
        String id = request.get("id") != null ? request.get("id") : "campaign-" + System.currentTimeMillis();
        String name = request.get("name") != null ? request.get("name") : "Air Campaign";
        AirCampaignLog campaign = new AirCampaignLog(id, name);
        if (request.get("nationality") != null) campaign.setNationality(request.get("nationality"));
        campaigns.put(id, campaign);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id, "name", name));
    }

    @PostMapping("/campaigns/{id}/battle")
    public ResponseEntity<?> addBattleToCampaign(@PathVariable String id, @RequestBody Map<String, Object> request) {
        AirCampaignLog campaign = campaigns.get(id);
        if (campaign == null) return ResponseEntity.notFound().build();
        AirBattleExperience exp = new AirBattleExperience(
                (String) request.get("battleId"),
                (String) request.get("airForceName"),
                (String) request.get("nationality"));
        if (request.get("altitude") != null) exp.setAltitude((String) request.get("altitude"));
        if (request.get("crtResult") != null) exp.setCrtResult((String) request.get("crtResult"));
        if (request.get("losses") != null) exp.setLosses((Integer) request.get("losses"));
        campaign.addBattle(exp);
        return ResponseEntity.ok(Map.of("message", "Battle added to campaign"));
    }

    @GetMapping("/campaigns/{id}")
    public ResponseEntity<?> getCampaign(@PathVariable String id) {
        AirCampaignLog campaign = campaigns.get(id);
        if (campaign == null) return ResponseEntity.notFound().build();
        Map<String, Object> response = new HashMap<>();
        response.put("id", campaign.getCampaignId());
        response.put("name", campaign.getCampaignName());
        response.put("battles", campaign.getBattles().size());
        response.put("summary", campaign.generateSummary());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/campaigns/{id}/book")
    public ResponseEntity<?> generateCampaignBook(@PathVariable String id) {
        AirCampaignLog campaign = campaigns.get(id);
        if (campaign == null) return ResponseEntity.notFound().build();
        String book = bookGenerator.generateBook(campaign);
        return ResponseEntity.ok(Map.of("book", book));
    }

    @PostMapping("/aircraft/convert")
    public ResponseEntity<?> convertAircraft(@RequestBody Map<String, String> request) {
        try {
            String aircraftName = request.get("aircraft");
            ThirdReichAircraft aircraft = ThirdReichAircraft.valueOf(aircraftName.toUpperCase());
            TravellerSpacecraft sc = conversionService.convert(aircraft);
            Map<String, Object> response = new HashMap<>();
            response.put("name", sc.getName());
            response.put("type", sc.getType().name());
            response.put("techLevel", sc.getTechLevel());
            response.put("attackValue", sc.getAttackValue());
            response.put("defenseValue", sc.getDefenseValue());
            response.put("cost", sc.getCost());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown aircraft: " + request.get("aircraft")));
        }
    }

    @GetMapping("/aircraft/available/{tl}")
    public ResponseEntity<?> getAvailableAircraft(@PathVariable int tl) {
        List<String> available = new ArrayList<>();
        for (TravellerSpacecraftType type : factory.getAvailableSpacecraft(tl)) {
            available.add(type.name());
        }
        return ResponseEntity.ok(Map.of("techLevel", tl, "available", available));
    }

    private TravellerSpacecraft parseSpacecraft(Map<String, Object> data) {
        String type = (String) data.get("type");
        int tl = data.get("tl") != null ? (Integer) data.get("tl") : 7;
        TravellerSpacecraft sc = factory.createSpacecraft(
                TravellerSpacecraftType.valueOf(type.toUpperCase()), tl);
        if (data.get("name") != null) sc.setName((String) data.get("name"));
        if (data.get("attackValue") != null) sc.setAttackValue((Integer) data.get("attackValue"));
        if (data.get("defenseValue") != null) sc.setDefenseValue((Integer) data.get("defenseValue"));
        return sc;
    }
}

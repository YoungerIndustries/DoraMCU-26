package com.yisy.modules.doramcu.traveller.shipcombat;

import com.yisy.modules.doramcu.traveller.ShipTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST API for ship-to-ship combat in the Traveller module.
 * <p>
 * Exposes endpoints for creating battles, resolving rounds, viewing status,
 * and browsing ship templates. Supports both Classic Traveller and High Guard
 * combat rules.
 * </p>
 * <p>
 * Base path: {@code /api/v1/traveller/ship-combat}
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/ship-combat")
public class ShipCombatController {

    private static final Logger log = LoggerFactory.getLogger(ShipCombatController.class);

    private final ShipCombatService combatService = new ShipCombatService();
    private final ShipCombatCampaignService campaignService = new ShipCombatCampaignService();

    // ---- Ship Templates ----

    /**
     * GET /templates — List all available ship templates.
     */
    @GetMapping("/templates")
    public ResponseEntity<?> getAllTemplates() {
        List<Map<String, Object>> templates = new ArrayList<>();
        for (ShipTemplate template : combatService.getAllTemplates()) {
            Map<String, Object> t = new HashMap<>();
            t.put("id", template.getId());
            t.put("name", template.getName());
            t.put("shipClass", template.getShipClass().name());
            t.put("tonnage", template.getTotalTonnage());
            t.put("maneuverDrive", template.getManeuverDrive());
            t.put("jumpDrive", template.getJumpDrive());
            t.put("energyPoints", template.getEnergyPoints());
            t.put("crew", template.getCrew());
            t.put("marines", template.getMarines());
            t.put("constructionCost", template.getConstructionCost());
            t.put("description", template.getDescription());
            templates.add(t);
        }
        return ResponseEntity.ok(Map.of("templates", templates));
    }

    /**
     * GET /templates/{shipClass} — Get templates for a specific ship class.
     */
    @GetMapping("/templates/{shipClass}")
    public ResponseEntity<?> getTemplatesByClass(@PathVariable String shipClass) {
        try {
            ShipTemplate.ShipClass sc = ShipTemplate.ShipClass.valueOf(shipClass.toUpperCase());
            List<Map<String, Object>> templates = new ArrayList<>();
            for (ShipTemplate template : combatService.getTemplatesByClass(sc)) {
                Map<String, Object> t = new HashMap<>();
                t.put("id", template.getId());
                t.put("name", template.getName());
                t.put("tonnage", template.getTotalTonnage());
                t.put("maneuverDrive", template.getManeuverDrive());
                t.put("crew", template.getCrew());
                templates.add(t);
            }
            return ResponseEntity.ok(Map.of("shipClass", shipClass, "templates", templates));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown ship class: " + shipClass));
        }
    }

    // ---- Battles ----

    /**
     * POST /battles — Create a new battle between two fleets.
     *
     * Request body:
     * {
     *   "id": "battle-1",  (optional, auto-generated if not provided)
     *   "fleet0": { "name": "Imperial Navy", "ships": ["Destroyer", "Light Cruiser"] },
     *   "fleet1": { "name": "Rebels", "ships": ["Scout/Courier", "Destroyer"] },
     *   "rulesType": "CLASSIC_TRAVELLER",  (or "HIGH_GUARD", default: "CLASSIC_TRAVELLER")
     *   "sector": "Core World"  (optional)
     * }
     */
    @PostMapping("/battles")
    public ResponseEntity<?> createBattle(@RequestBody Map<String, Object> request) {
        try {
            String id = request.get("id") != null ? (String) request.get("id")
                    : "battle-" + System.currentTimeMillis();

            @SuppressWarnings("unchecked")
            Map<String, Object> fleet0Data = (Map<String, Object>) request.get("fleet0");
            @SuppressWarnings("unchecked")
            Map<String, Object> fleet1Data = (Map<String, Object>) request.get("fleet1");

            if (fleet0Data == null || fleet1Data == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Both fleet0 and fleet1 are required"));
            }

            String fleet0Name = (String) fleet0Data.get("name");
            String fleet1Name = (String) fleet1Data.get("name");
            @SuppressWarnings("unchecked")
            List<String> fleet0Ships = (List<String>) fleet0Data.get("ships");
            @SuppressWarnings("unchecked")
            List<String> fleet1Ships = (List<String>) fleet1Data.get("ships");

            String rulesTypeStr = request.get("rulesType") != null
                    ? (String) request.get("rulesType") : "HIGH_GUARD";
            BattleSession.RulesType rulesType = BattleSession.RulesType.valueOf(rulesTypeStr.toUpperCase());

            String sector = request.get("sector") != null ? (String) request.get("sector") : "Unknown Sector";

            BattleSession session = combatService.createBattle(
                    id, fleet0Name, fleet0Ships, fleet1Name, fleet1Ships, rulesType, sector);

            Map<String, Object> response = new HashMap<>();
            response.put("id", session.getId());
            response.put("state", session.getState().name());
            response.put("rulesType", session.getRulesType().name());
            response.put("sector", session.getSector());
            response.put("fleet0", buildFleetSummary(session.getFleet0()));
            response.put("fleet1", buildFleetSummary(session.getFleet1()));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Failed to create battle", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to create battle", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /battles — List all active battle IDs.
     */
    @GetMapping("/battles")
    public ResponseEntity<?> listBattles() {
        return ResponseEntity.ok(Map.of("battles", combatService.listBattles()));
    }

    /**
     * GET /battles/{id} — Get battle status.
     */
    @GetMapping("/battles/{id}")
    public ResponseEntity<?> getBattle(@PathVariable String id) {
        try {
            BattleSession session = combatService.getBattle(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", session.getId());
            response.put("state", session.getState().name());
            response.put("rulesType", session.getRulesType().name());
            response.put("sector", session.getSector());
            response.put("currentRound", session.getCurrentRound());
            response.put("fleet0", buildFleetSummary(session.getFleet0()));
            response.put("fleet1", buildFleetSummary(session.getFleet1()));
            if (session.getOutcome() != null) {
                response.put("outcome", session.getOutcome());
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /battles/{id}/resolve-round — Resolve one round of combat.
     */
    @PostMapping("/battles/{id}/resolve-round")
    public ResponseEntity<?> resolveRound(@PathVariable String id) {
        try {
            BattleSession session = combatService.resolveRound(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", session.getId());
            response.put("state", session.getState().name());
            response.put("currentRound", session.getCurrentRound());
            if (session.getOutcome() != null) {
                response.put("outcome", session.getOutcome());
            }
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /battles/{id}/resolve — Resolve battle to completion.
     */
    @PostMapping("/battles/{id}/resolve")
    public ResponseEntity<?> resolveToCompletion(@PathVariable String id) {
        try {
            BattleSession session = combatService.resolveToCompletion(id);
            Map<String, Object> response = new HashMap<>();
            response.put("id", session.getId());
            response.put("state", session.getState().name());
            response.put("currentRound", session.getCurrentRound());
            response.put("outcome", session.getOutcome());
            response.put("events", session.getEvents());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /battles/{id}/events — Get all battle events.
     */
    @GetMapping("/battles/{id}/events")
    public ResponseEntity<?> getBattleEvents(@PathVariable String id) {
        try {
            BattleSession session = combatService.getBattle(id);
            return ResponseEntity.ok(Map.of("battleId", id, "events", session.getEvents()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ---- Campaigns ----

    /**
     * POST /campaigns — Create a new campaign.
     *
     * Request body:
     * {
     *   "id": "campaign-1",
     *   "name": "Core World Campaign",
     *   "description": "...",
     *   "sector": "Core",
     *   "faction0": "Imperial Navy",
     *   "faction1": "Rebels"
     * }
     */
    @PostMapping("/campaigns")
    public ResponseEntity<?> createCampaign(@RequestBody Map<String, Object> request) {
        try {
            String id = request.get("id") != null ? (String) request.get("id")
                    : "campaign-" + System.currentTimeMillis();
            String name = request.get("name") != null ? (String) request.get("name") : "Untitled Campaign";
            String description = request.get("description") != null ? (String) request.get("description") : "";
            String sector = request.get("sector") != null ? (String) request.get("sector") : "Unknown Sector";
            String faction0 = request.get("faction0") != null ? (String) request.get("faction0") : "Faction 0";
            String faction1 = request.get("faction1") != null ? (String) request.get("faction1") : "Faction 1";

            ShipCombatCampaignService.Campaign campaign = campaignService.createCampaign(
                    id, name, description, sector, faction0, faction1);

            return ResponseEntity.status(HttpStatus.CREATED).body(campaign.toSummary());
        } catch (Exception e) {
            log.error("Failed to create campaign", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /campaigns — List all campaign IDs.
     */
    @GetMapping("/campaigns")
    public ResponseEntity<?> listCampaigns() {
        return ResponseEntity.ok(Map.of("campaigns", campaignService.listCampaigns()));
    }

    /**
     * GET /campaigns/{id} — Get campaign details.
     */
    @GetMapping("/campaigns/{id}")
    public ResponseEntity<?> getCampaign(@PathVariable String id) {
        try {
            ShipCombatCampaignService.Campaign campaign = campaignService.getCampaign(id);
            return ResponseEntity.ok(campaign.toSummary());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /campaigns/{id}/battles — Add a battle result to a campaign.
     *
     * Request body:
     * {
     *   "battleId": "battle-1",
     *   "outcome": "SIDE_0_VICTORY",
     *   "rounds": 5
     * }
     */
    @PostMapping("/campaigns/{id}/battles")
    public ResponseEntity<?> addBattleToCampaign(@PathVariable String id, @RequestBody Map<String, Object> request) {
        try {
            String battleId = (String) request.get("battleId");
            String outcome = (String) request.get("outcome");
            int rounds = request.get("rounds") != null ? (Integer) request.get("rounds") : 0;

            ShipCombatCampaignService.Campaign campaign = campaignService.addBattle(id, battleId, outcome, rounds);
            return ResponseEntity.ok(campaign.toSummary());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /campaigns/{id}/summary — Get campaign summary.
     */
    @GetMapping("/campaigns/{id}/summary")
    public ResponseEntity<?> getCampaignSummary(@PathVariable String id) {
        try {
            ShipCombatCampaignService.Campaign campaign = campaignService.getCampaign(id);
            return ResponseEntity.ok(campaign.toSummary());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ---- Helpers ----

    private Map<String, Object> buildFleetSummary(Fleet fleet) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", fleet.getId());
        summary.put("side", fleet.getSide());
        summary.put("name", fleet.getName());
        summary.put("totalTonnage", fleet.getTotalTonnage());
        summary.put("activeShips", fleet.getActiveShipCount());
        summary.put("totalHullPoints", fleet.getTotalHullPoints());

        List<Map<String, Object>> ships = new ArrayList<>();
        for (Fleet.FleetShip ship : fleet.getShips()) {
            Map<String, Object> s = new HashMap<>();
            s.put("name", ship.getTemplateName());
            s.put("shipClass", ship.getShipClass() != null ? ship.getShipClass().name() : "Custom");
            s.put("tonnage", ship.getTonnage());
            s.put("hullPoints", ship.getHullPoints());
            s.put("currentHullPoints", ship.getCurrentHullPoints());
            s.put("destroyed", ship.isDestroyed());
            ships.add(s);
        }
        summary.put("ships", ships);
        return summary;
    }
}

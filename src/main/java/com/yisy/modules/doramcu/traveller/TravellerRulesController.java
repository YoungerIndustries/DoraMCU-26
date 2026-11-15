package com.yisy.modules.doramcu.traveller;

import com.yisy.modules.doramcu.shared.model.RulesEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.ResponseEntity.ok;

/**
 * REST controller for browsing and searching the Classic Traveller rules database.
 * <p>
 * Provides endpoints for querying rule entries and reference tables from 12 Classic Traveller
 * source books: The Traveller Book, B01-B07, S03, S07, S09, and A05.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/rules")
public class TravellerRulesController {

    private static final Logger log = LoggerFactory.getLogger(TravellerRulesController.class);

    private final TravellerRulesService travellerRulesService;

    public TravellerRulesController(TravellerRulesService travellerRulesService) {
        this.travellerRulesService = travellerRulesService;
    }

    // ==================== Summary ====================

    /**
     * Gets a summary of all rules and tables in the Traveller rules database.
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getRulesSummary() {
        log.debug("GET /api/v1/traveller/rules/summary");
        Map<String, Object> summary = new HashMap<>();

        // Count total entries by summing all categories
        long totalEntries = 0;
        for (String category : travellerRulesService.getCategories()) {
            totalEntries += travellerRulesService.getRulesByCategory(category).size();
        }

        summary.put("totalEntries", totalEntries);
        summary.put("categories", travellerRulesService.getCategories());
        summary.put("sources", travellerRulesService.getSources());
        
        // Dynamically get all table categories from the database
        List<String> tableCategories = travellerRulesService.getAllTableCategories();
        summary.put("tableCategories", tableCategories);

        return ResponseEntity.ok(summary);
    }

    // ==================== Rule Entries ====================

    /**
     * Gets all rule entries for a given category.
     */
    @GetMapping("/category")
    public ResponseEntity<List<RulesEntry>> getRulesByCategory(
            @RequestParam String category) {
        log.debug("GET /api/v1/traveller/rules/category?category={}", category);
        List<RulesEntry> rules = travellerRulesService.getRulesByCategory(category);
        return ResponseEntity.ok(rules);
    }

    /**
     * Gets all rule entries from a given source book.
     */
    @GetMapping("/source")
    public ResponseEntity<List<RulesEntry>> getRulesBySource(
            @RequestParam String source) {
        log.debug("GET /api/v1/traveller/rules/source?source={}", source);
        return ResponseEntity.ok(travellerRulesService.getRulesBySource(source));
    }

    /**
     * Searches rule entries by keyword.
     */
    @GetMapping("/search")
    public ResponseEntity<List<RulesEntry>> searchRules(
            @RequestParam String keyword) {
        log.debug("GET /api/v1/traveller/rules/search?keyword={}", keyword);
        return ResponseEntity.ok(travellerRulesService.searchRules(keyword));
    }

    /**
     * Gets all available rule categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        log.debug("GET /api/v1/traveller/rules/categories");
        return ResponseEntity.ok(travellerRulesService.getCategories());
    }

    /**
     * Gets all available source books.
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        log.debug("GET /api/v1/traveller/rules/sources");
        return ResponseEntity.ok(travellerRulesService.getSources());
    }

    // ==================== Reference Tables ====================

    /**
     * Gets all table entries for a given table category (e.g., TABLE_SAVING_THROW).
     * If no category is specified, returns all table entries.
     */
    @GetMapping("/tables")
    public ResponseEntity<List<TravellerRulesTable>> getTableEntries(
            @RequestParam(required = false) String category) {
        if (category != null && !category.isEmpty()) {
            log.debug("GET /api/v1/traveller/rules/tables?category={}", category);
            List<TravellerRulesTable> entries = travellerRulesService.getTableEntriesByCategory(category);
            return ResponseEntity.ok(entries);
        } else {
            log.debug("GET /api/v1/traveller/rules/tables (all)");
            List<TravellerRulesTable> allEntries = travellerRulesService.getAllTableEntries();
            return ResponseEntity.ok(allEntries);
        }
    }

    /**
     * Gets all table entries for a given category (alias for /tables?category=).
     */
    @GetMapping("/tables/category")
    public ResponseEntity<List<TravellerRulesTable>> getTableEntriesByCategory(
            @RequestParam String category) {
        log.debug("GET /api/v1/traveller/rules/tables/category?category={}", category);
        List<TravellerRulesTable> entries = travellerRulesService.getTableEntriesByCategory(category);
        return ResponseEntity.ok(entries);
    }

    // ---- B07 Trade Rules Endpoints ----

    private final B07RevenueService revenueService = new B07RevenueService();

    /**
     * Gets all predefined B07 trade classifications.
     */
    @GetMapping("/trade-classifications")
    public ResponseEntity<Map<String, TradeClassification>> getTradeClassifications() {
        log.debug("GET /api/v1/traveller/rules/trade-classifications");
        return ok(TradeClassification.getAllClassifications());
    }

    /**
     * Gets a specific trade classification by code.
     */
    @GetMapping("/trade-classifications/{code}")
    public ResponseEntity<TradeClassification> getTradeClassification(@PathVariable String code) {
        log.debug("GET /api/v1/traveller/rules/trade-classifications/{}", code);
        TradeClassification classification = TradeClassification.findByCode(code);
        if (classification != null) {
            return ok(classification);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Gets all trade codes.
     */
    @GetMapping("/trade-codes")
    public ResponseEntity<List<TradeCode>> getTradeCodes() {
        log.debug("GET /api/v1/traveller/rules/trade-codes");
        return ok(List.of(TradeCode.values()));
    }

    /**
     * Gets all commodities with B07 Actual Value information.
     */
    @GetMapping("/commodities")
    public ResponseEntity<List<Commodity>> getCommodities() {
        log.debug("GET /api/v1/traveller/rules/commodities");
        return ok(List.of(Commodity.values()));
    }

    /**
     * Computes the revenue breakdown for a world by name within a sector.
     */
    @GetMapping("/revenue/{worldName}")
    public ResponseEntity<B07RevenueService.RevenueBreakdown> getWorldRevenue(
            @PathVariable String worldName,
            @RequestParam String sector) {
        log.debug("GET /api/v1/traveller/rules/revenue/{}?sector={}", worldName, sector);
        // This endpoint requires a sector lookup — returns 404 if not found
        // For now, returns a placeholder indicating the world name
        return ResponseEntity.notFound().build();
    }

    // ---- Economic Events Endpoints ----

    /**
     * Gets all economic event types with their descriptions.
     */
    @GetMapping("/economic-event-types")
    public ResponseEntity<List<Map<String, Object>>> getEconomicEventTypes() {
        log.debug("GET /api/v1/traveller/rules/economic-event-types");
        List<Map<String, Object>> types = new ArrayList<>();
        for (EconomicEventType type : EconomicEventType.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", type.name());
            map.put("displayName", type.getDisplayName());
            map.put("description", type.getDescription());
            map.put("minSeverity", type.getMinSeverity());
            map.put("maxSeverity", type.getMaxSeverity());
            map.put("minDuration", type.getMinDuration());
            map.put("maxDuration", type.getMaxDuration());
            types.add(map);
        }
        return ok(types);
    }

    /**
     * Gets a specific economic event type by name.
     */
    @GetMapping("/economic-event-types/{typeName}")
    public ResponseEntity<Map<String, Object>> getEconomicEventType(@PathVariable String typeName) {
        log.debug("GET /api/v1/traveller/rules/economic-event-types/{}", typeName);
        try {
            EconomicEventType type = EconomicEventType.valueOf(typeName.toUpperCase());
            Map<String, Object> map = new HashMap<>();
            map.put("name", type.name());
            map.put("displayName", type.getDisplayName());
            map.put("description", type.getDescription());
            map.put("minSeverity", type.getMinSeverity());
            map.put("maxSeverity", type.getMaxSeverity());
            map.put("minDuration", type.getMinDuration());
            map.put("maxDuration", type.getMaxDuration());
            return ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Calculates the price modifier for a commodity under a specific event type and severity.
     */
    @GetMapping("/economic-event-price-modifier")
    public ResponseEntity<Map<String, Object>> getEconomicEventPriceModifier(
            @RequestParam String eventType,
            @RequestParam String commodity,
            @RequestParam(defaultValue = "5") int severity) {
        log.debug("GET /api/v1/traveller/rules/economic-event-price-modifier?eventType={}&commodity={}&severity={}",
                eventType, commodity, severity);
        try {
            EconomicEventType type = EconomicEventType.valueOf(eventType.toUpperCase());
            Commodity comm = Commodity.valueOf(commodity.toUpperCase());
            double modifier = type.getPriceModifier(comm, severity);
            Map<String, Object> result = new HashMap<>();
            result.put("eventType", type.getDisplayName());
            result.put("commodity", comm.getName());
            result.put("severity", severity);
            result.put("priceModifier", modifier);
            result.put("conditionOverride", type.getConditionOverride(comm, severity));
            return ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ---- Psionics Endpoints (B03) ----

    /**
     * Gets all psionic disciplines.
     */
    @GetMapping("/psionics/disciplines")
    public ResponseEntity<List<Map<String, Object>>> getPsionicDisciplines() {
        log.debug("GET /api/v1/traveller/rules/psionics/disciplines");
        List<Map<String, Object>> disciplines = new ArrayList<>();
        for (PsionicDiscipline discipline : PsionicDiscipline.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", discipline.name());
            map.put("displayName", discipline.getDisplayName());
            map.put("description", discipline.getDescription());
            map.put("tableValue", discipline.getTableValue());
            disciplines.add(map);
        }
        return ok(disciplines);
    }

    /**
     * Performs a psionic power check.
     */
    @GetMapping("/psionics/check")
    public ResponseEntity<Map<String, Object>> performPsionicCheck(
            @RequestParam String discipline,
            @RequestParam int instinctScore,
            @RequestParam(defaultValue = "1") int level,
            @RequestParam(defaultValue = "NORMAL") String difficulty) {
        log.debug("GET /api/v1/traveller/rules/psionics/check?discipline={}&instinctScore={}&level={}&difficulty={}",
                discipline, instinctScore, level, difficulty);
        try {
            PsionicDiscipline disc = PsionicDiscipline.valueOf(discipline.toUpperCase());
            PsionicCheck.Difficulty diff = PsionicCheck.Difficulty.valueOf(difficulty.toUpperCase());
            PsionicCheck check = new PsionicCheck();
            PsionicCheck.PsionicResult result = check.performCheck(instinctScore, level, diff);
            Map<String, Object> map = new HashMap<>();
            map.put("discipline", disc.getDisplayName());
            map.put("instinctScore", instinctScore);
            map.put("level", level);
            map.put("difficulty", diff.getDisplayName());
            map.put("roll", result.getRoll());
            map.put("target", result.getTarget());
            map.put("success", result.isSuccess());
            map.put("message", result.getMessage());
            return ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Generates a random psionic encounter.
     */
    @GetMapping("/psionics/encounter")
    public ResponseEntity<Map<String, Object>> generatePsionicEncounter(
            @RequestParam(defaultValue = "AVERAGE") String detectionLevel) {
        log.debug("GET /api/v1/traveller/rules/psionics/encounter?detectionLevel={}", detectionLevel);
        try {
            PsionicAbility.DetectionLevel detLevel = PsionicAbility.DetectionLevel.valueOf(detectionLevel.toUpperCase());
            PsionicEncounter encounter = new PsionicEncounter();
            PsionicEncounter.PsionicEncounterResult result = encounter.generateEncounter(detLevel);
            Map<String, Object> map = new HashMap<>();
            map.put("type", result.getType().getDisplayName());
            map.put("description", result.getType().getDescription());
            map.put("roll", result.getRoll());
            map.put("narrative", result.getNarrative());
            return ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ---- Smuggling Endpoints (B07) ----

    /**
     * Gets all contraband commodity types.
     */
    @GetMapping("/smuggling/contraband")
    public ResponseEntity<List<Map<String, Object>>> getContrabandTypes() {
        log.debug("GET /api/v1/traveller/rules/smuggling/contraband");
        List<Map<String, Object>> types = new ArrayList<>();
        for (ContrabandCommodity commodity : ContrabandCommodity.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", commodity.name());
            map.put("displayName", commodity.getDisplayName());
            map.put("description", commodity.getDescription());
            map.put("riskLevel", commodity.getRiskLevel());
            map.put("basePrice", commodity.getBasePrice());
            map.put("volumePerUnit", commodity.getVolumePerUnit());
            map.put("profitMultiplier", commodity.getProfitMultiplier());
            types.add(map);
        }
        return ok(types);
    }

    /**
     * Gets all customs check actions.
     */
    @GetMapping("/smuggling/customs/actions")
    public ResponseEntity<List<Map<String, Object>>> getCustomsActions() {
        log.debug("GET /api/v1/traveller/rules/smuggling/customs/actions");
        List<Map<String, Object>> actions = new ArrayList<>();
        for (CustomsCheck.Action action : CustomsCheck.Action.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", action.name());
            map.put("displayName", action.getDisplayName());
            map.put("description", action.getDescription());
            actions.add(map);
        }
        return ok(actions);
    }

    /**
     * Simulates a customs check.
     */
    @GetMapping("/smuggling/customs/check")
    public ResponseEntity<Map<String, Object>> simulateCustomsCheck(
            @RequestParam String action,
            @RequestParam String contraband,
            @RequestParam(defaultValue = "5") int lawLevel,
            @RequestParam(defaultValue = "2") int pilotSkill,
            @RequestParam(defaultValue = "10000") long credits) {
        log.debug("GET /api/v1/traveller/rules/smuggling/customs/check?action={}&contraband={}&lawLevel={}",
                action, contraband, lawLevel);
        try {
            CustomsCheck.Action act = CustomsCheck.Action.valueOf(action.toUpperCase());
            ContrabandCommodity comm = ContrabandCommodity.valueOf(contraband.toUpperCase());
            CustomsCheck customsCheck = new CustomsCheck();
            CustomsCheck.CustomsResult result = customsCheck.resolve(act, comm, lawLevel, pilotSkill, credits);
            Map<String, Object> map = new HashMap<>();
            map.put("action", act.getDisplayName());
            map.put("contraband", comm.getDisplayName());
            map.put("lawLevel", lawLevel);
            map.put("outcome", result.getOutcome().getDisplayName());
            map.put("roll", result.getRoll());
            map.put("target", result.getTarget());
            map.put("success", result.isSuccess());
            map.put("narrative", result.getNarrative());
            map.put("creditsLost", result.getCreditsLost());
            return ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ---- Alien Race Endpoints (S03) ----

    /**
     * Gets all alien races.
     */
    @GetMapping("/alien-races")
    public ResponseEntity<List<Map<String, Object>>> getAlienRaces() {
        log.debug("GET /api/v1/traveller/rules/alien-races");
        List<Map<String, Object>> races = new ArrayList<>();
        for (AlienRace race : AlienRace.values()) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", race.name());
            map.put("displayName", race.getDisplayName());
            map.put("description", race.getDescription());
            map.put("combatDM", race.getCombatDM());
            map.put("socialDM", race.getSocialDM());
            map.put("navigationDM", race.getNavigationDM());
            map.put("armorBonus", race.getArmorBonus());
            map.put("tradeBonusPercent", race.getTradeBonusPercent());
            races.add(map);
        }
        return ok(races);
    }

    /**
     * Gets a specific alien race by name.
     */
    @GetMapping("/alien-races/{raceName}")
    public ResponseEntity<Map<String, Object>> getAlienRace(@PathVariable String raceName) {
        log.debug("GET /api/v1/traveller/rules/alien-races/{}", raceName);
        try {
            AlienRace race = AlienRace.valueOf(raceName.toUpperCase());
            Map<String, Object> map = new HashMap<>();
            map.put("name", race.name());
            map.put("displayName", race.getDisplayName());
            map.put("description", race.getDescription());
            map.put("combatDM", race.getCombatDM());
            map.put("socialDM", race.getSocialDM());
            map.put("navigationDM", race.getNavigationDM());
            map.put("armorBonus", race.getArmorBonus());
            map.put("tradeBonusPercent", race.getTradeBonusPercent());
            return ok(map);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

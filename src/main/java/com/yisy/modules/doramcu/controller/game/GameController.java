package com.yisy.modules.doramcu.controller.game;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.hex.Ship;
import com.yisy.modules.doramcu.hex.ShipType;
import com.yisy.modules.doramcu.dto.battle.BattleSummaryDto;
import com.yisy.modules.doramcu.dto.station.BuildFacilityRequest;
import com.yisy.modules.doramcu.dto.ship_design.BuildShipRequest;
import com.yisy.modules.doramcu.dto.station.BuildStationRequest;
import com.yisy.modules.doramcu.dto.game.ColonizeRequest;
import com.yisy.modules.doramcu.dto.fleet.CreateFleetRequest;
import com.yisy.modules.doramcu.dto.battle.EngageCombatRequest;
import com.yisy.modules.doramcu.dto.session.GameStateResponse;
import com.yisy.modules.doramcu.dto.misc.IncomeProjectionDto;
import com.yisy.modules.doramcu.dto.fleet.MoveFleetRequest;
import com.yisy.modules.doramcu.dto.misc.PipelineDetailsDto;
import com.yisy.modules.doramcu.model.misc.Planet;
import com.yisy.modules.doramcu.dto.battle.ReinforceBattleRequest;
import com.yisy.modules.doramcu.dto.misc.ResourcePipelineSummary;
import com.yisy.modules.doramcu.dto.ship_design.ShipDesignRequest;
import com.yisy.modules.doramcu.dto.trade.TradeOpportunityDto;
import com.yisy.modules.doramcu.dto.trade.TradeRequest;
import com.yisy.modules.doramcu.dto.trade.TradeSummaryDto;
import com.yisy.modules.doramcu.service.session.GameSessionService;
import com.yisy.modules.doramcu.util.PlayerIdExtractor;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/4x/sessions/{sessionId}")
public class GameController {

    private static final Logger log = LoggerFactory.getLogger(GameController.class);

    private final GameSessionService gameSessionService;
    private final PlayerIdExtractor playerIdExtractor;

    public GameController(GameSessionService gameSessionService, PlayerIdExtractor playerIdExtractor) {
        this.gameSessionService = gameSessionService;
        this.playerIdExtractor = playerIdExtractor;
    }

    @GetMapping("/state")
    public ResponseEntity<GameStateResponse> getGameState(@PathVariable String sessionId,
                                                          @RequestParam(required = false, defaultValue = "false") boolean summary,
                                                          Authentication authentication) {
        log.debug("User {} requesting game state for session: {} (summary={})", authentication.getName(), sessionId, summary);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.getGameState(playerId, sessionId, summary);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/move-fleet")
    public ResponseEntity<GameStateResponse> moveFleet(@PathVariable String sessionId,
                                                       @Valid @RequestBody MoveFleetRequest request,
                                                       Authentication authentication) {
        log.debug("User {} moving fleet in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.moveFleet(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/build-ship")
    public ResponseEntity<GameStateResponse> buildShip(@PathVariable String sessionId,
                                                       @Valid @RequestBody BuildShipRequest request,
                                                       Authentication authentication) {
        log.debug("User {} building ship in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buildShip(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ship/{shipType}/module-requirements")
    public ResponseEntity<Map<String, Object>> getShipModuleRequirements(
            @PathVariable String sessionId,
            @PathVariable String shipType,
            Authentication authentication) {
        log.debug("User {} requesting module requirements for ship type {} in session: {}",
                authentication.getName(), shipType, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> requirements = gameSessionService.getShipModuleRequirements(playerId, sessionId, shipType);
        if (requirements == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(requirements);
    }

    @PostMapping("/actions/engage-combat")
    public ResponseEntity<GameStateResponse> engageCombat(@PathVariable String sessionId,
                                                          @Valid @RequestBody EngageCombatRequest request,
                                                          Authentication authentication) {
        log.debug("User {} engaging combat in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.engageCombat(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/advance-turn")
    public ResponseEntity<GameStateResponse> advanceTurn(@PathVariable String sessionId,
                                                         Authentication authentication) {
        log.debug("User {} advancing turn in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.advanceTurn(playerId, sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pipeline")
    public ResponseEntity<ResourcePipelineSummary> getPipelineStatus(@PathVariable String sessionId,
                                                                     Authentication authentication) {
        log.debug("User {} requesting pipeline status for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        ResourcePipelineSummary summary = gameSessionService.getPipelineSummary(playerId, sessionId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/pipeline/details")
    public ResponseEntity<PipelineDetailsDto> getPipelineDetails(@PathVariable String sessionId,
                                                                  Authentication authentication) {
        log.debug("User {} requesting pipeline details for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        PipelineDetailsDto details = gameSessionService.getPipelineDetails(playerId, sessionId);
        if (details == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(details);
    }

    // ---- Transport Endpoints ----

    @GetMapping("/transports")
    public ResponseEntity<Map<String, Object>> getTransportSummary(@PathVariable String sessionId,
                                                                    Authentication authentication) {
        log.debug("User {} requesting transport summary for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> summary = gameSessionService.getTransportSummary(playerId, sessionId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/transports/consolidate")
    public ResponseEntity<Map<String, Object>> consolidateTransports(@PathVariable String sessionId,
                                                                      Authentication authentication) {
        log.debug("User {} consolidating transports in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> result = gameSessionService.consolidateTransports(playerId, sessionId);
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/transports/optimize")
    public ResponseEntity<Map<String, Object>> optimizeTransports(@PathVariable String sessionId,
                                                                   Authentication authentication) {
        log.debug("User {} optimizing transport routes in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> result = gameSessionService.optimizeTransports(playerId, sessionId);
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/transports/cancel/{orderId}")
    public ResponseEntity<Map<String, Object>> cancelTransport(@PathVariable String sessionId,
                                                                @PathVariable int orderId,
                                                                Authentication authentication) {
        log.debug("User {} cancelling transport {} in session: {}", authentication.getName(), orderId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> result = gameSessionService.cancelTransport(playerId, sessionId, orderId);
        if (!((Boolean) result.get("cancelled"))) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/economy")
    public ResponseEntity<Map<String, Object>> getEconomicState(@PathVariable String sessionId,
                                                                 Authentication authentication) {
        log.debug("User {} requesting economic state for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> summary = gameSessionService.getEconomicState(playerId, sessionId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(summary);
    }

    // ---- Market Board Endpoints ----

    @GetMapping("/market")
    public ResponseEntity<Map<String, Object>> getMarketBoard(@PathVariable String sessionId,
                                                               Authentication authentication) {
        log.debug("User {} requesting market board for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> summary = gameSessionService.getMarketBoard(playerId, sessionId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/market/listing")
    public ResponseEntity<Map<String, Object>> createTradeListing(@PathVariable String sessionId,
                                                                    @RequestParam String type,
                                                                    @RequestParam String commodity,
                                                                    @RequestParam int units,
                                                                    @RequestParam int pricePerUnit,
                                                                    @RequestParam(defaultValue = "5") int turnsToLive,
                                                                    Authentication authentication) {
        log.debug("User {} creating trade listing in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> result = gameSessionService.createTradeListing(
                playerId, sessionId, type, commodity, units, pricePerUnit, turnsToLive);
        if (result.containsKey("error")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/market/listing/{listingId}")
    public ResponseEntity<Map<String, Object>> cancelTradeListing(@PathVariable String sessionId,
                                                                    @PathVariable int listingId,
                                                                    Authentication authentication) {
        log.debug("User {} cancelling trade listing {} in session: {}", authentication.getName(), listingId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> result = gameSessionService.cancelTradeListing(playerId, sessionId, listingId);
        if (!((Boolean) result.get("cancelled"))) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // ---- Technology Endpoints ----

    @GetMapping("/technology")
    public ResponseEntity<Map<String, Object>> getTechnologyTree(@PathVariable String sessionId,
                                                                   Authentication authentication) {
        log.debug("User {} requesting technology tree for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> summary = gameSessionService.getTechnologyTree(playerId, sessionId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/research/{technology}")
    public ResponseEntity<Map<String, Object>> researchTechnology(@PathVariable String sessionId,
                                                                    @PathVariable String technology,
                                                                    Authentication authentication) {
        log.debug("User {} researching {} in session: {}", authentication.getName(), technology, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> result = gameSessionService.researchTechnology(playerId, sessionId, technology);
        if (result.containsKey("error") && !(Boolean) result.get("success")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // ---- Recipe Endpoints ----

    @GetMapping("/recipes")
    public ResponseEntity<Map<String, Object>> getRecipes(@PathVariable String sessionId,
                                                           Authentication authentication) {
        log.debug("User {} requesting recipes for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> recipes = gameSessionService.getRecipes(playerId, sessionId);
        if (recipes == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(recipes);
    }

    // ---- Tax Endpoints ----

    @GetMapping("/tax")
    public ResponseEntity<Map<String, Object>> getTaxSummary(@PathVariable String sessionId,
                                                               Authentication authentication) {
        log.debug("User {} requesting tax summary for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> summary = gameSessionService.getTaxSummary(playerId, sessionId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(summary);
    }

    @PutMapping("/tax/policy/{policy}")
    public ResponseEntity<Map<String, Object>> setTaxPolicy(@PathVariable String sessionId,
                                                             @PathVariable String policy,
                                                             Authentication authentication) {
        log.debug("User {} setting tax policy {} for session: {}", authentication.getName(), policy, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> result = gameSessionService.setTaxPolicy(playerId, sessionId, policy);
        if (result.containsKey("error") && !(Boolean) result.get("success")) {
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    // ---- Economic Dashboard Endpoints ----

    @GetMapping("/economy/dashboard")
    public ResponseEntity<Map<String, Object>> getEconomicDashboard(@PathVariable String sessionId,
                                                                     Authentication authentication) {
        log.debug("User {} requesting economic dashboard for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        Map<String, Object> dashboard = gameSessionService.getEconomicDashboard(playerId, sessionId);
        if (dashboard == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/economy/trade")
    public ResponseEntity<TradeSummaryDto> getTradeSummary(@PathVariable String sessionId,
                                                            Authentication authentication) {
        log.debug("User {} requesting trade summary for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        TradeSummaryDto summary = gameSessionService.getTradeSummary(playerId, sessionId);
        if (summary == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/economy/income")
    public ResponseEntity<IncomeProjectionDto> getIncomeProjection(@PathVariable String sessionId,
                                                                    Authentication authentication) {
        log.debug("User {} requesting income projection for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        IncomeProjectionDto projection = gameSessionService.getIncomeProjection(playerId, sessionId);
        if (projection == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(projection);
    }

    // ---- New Empire Action Endpoints ----

    @PostMapping("/actions/build-station")
    public ResponseEntity<GameStateResponse> buildStation(@PathVariable String sessionId,
                                                          @Valid @RequestBody BuildStationRequest request,
                                                          Authentication authentication) {
        log.debug("User {} building station in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buildStation(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/build-facility")
    public ResponseEntity<GameStateResponse> buildFacility(@PathVariable String sessionId,
                                                           @Valid @RequestBody BuildFacilityRequest request,
                                                           Authentication authentication) {
        log.debug("User {} building facility in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buildFacility(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/create-fleet")
    public ResponseEntity<GameStateResponse> createFleet(@PathVariable String sessionId,
                                                         @Valid @RequestBody CreateFleetRequest request,
                                                         Authentication authentication) {
        log.debug("User {} creating fleet in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.createFleet(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/colonize")
    public ResponseEntity<GameStateResponse> colonizePlanet(@PathVariable String sessionId,
                                                            @Valid @RequestBody ColonizeRequest request,
                                                            Authentication authentication) {
        log.debug("User {} colonizing planet in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.colonizePlanet(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/battles")
    public ResponseEntity<List<BattleSummaryDto>> getActiveBattles(@PathVariable String sessionId,
                                                                    Authentication authentication) {
        log.debug("User {} requesting active battles for session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        List<BattleSummaryDto> battles = gameSessionService.getActiveBattles(playerId, sessionId);
        return ResponseEntity.ok(battles);
    }

    @PostMapping("/battles/{battleId}/reinforce")
    public ResponseEntity<GameStateResponse> reinforceBattle(@PathVariable String sessionId,
                                                             @PathVariable int battleId,
                                                             @Valid @RequestBody ReinforceBattleRequest request,
                                                             Authentication authentication) {
        log.debug("User {} reinforcing battle {} in session: {}", authentication.getName(), battleId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.reinforceBattle(playerId, sessionId, battleId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/battles/{battleId}/retreat")
    public ResponseEntity<GameStateResponse> retreatBattle(@PathVariable String sessionId,
                                                           @PathVariable int battleId,
                                                           Authentication authentication) {
        log.debug("User {} retreating from battle {} in session: {}", authentication.getName(), battleId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.retreatBattle(playerId, sessionId, battleId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/systems/{systemId}")
    public ResponseEntity<java.util.Map<String, Object>> getSystemDetails(@PathVariable String sessionId,
                                                                            @PathVariable int systemId,
                                                                            Authentication authentication) {
        log.debug("User {} requesting system details for system {} in session: {}", authentication.getName(), systemId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        java.util.Map<String, Object> details = gameSessionService.getSystemDetails(playerId, sessionId, systemId);
        if (details == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(details);
    }

    // ---- Traveller RPG Integration Endpoints ----

    @PostMapping("/actions/build-from-design")
    public ResponseEntity<GameStateResponse> buildFromDesign(@PathVariable String sessionId,
                                                              @Valid @RequestBody ShipDesignRequest request,
                                                              Authentication authentication) {
        log.debug("User {} building ship from design in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buildShipFromDesign(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/trade/buy")
    public ResponseEntity<GameStateResponse> buyCommodity(@PathVariable String sessionId,
                                                           @Valid @RequestBody TradeRequest request,
                                                           Authentication authentication) {
        log.debug("User {} buying commodity in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.buyCommodity(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/actions/trade/sell")
    public ResponseEntity<GameStateResponse> sellCommodity(@PathVariable String sessionId,
                                                            @Valid @RequestBody TradeRequest request,
                                                            Authentication authentication) {
        log.debug("User {} selling commodity in session: {}", authentication.getName(), sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.sellCommodity(playerId, sessionId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trade/market")
    public ResponseEntity<List<TradeOpportunityDto>> getTradeMarket(@PathVariable String sessionId,
                                                                     @RequestParam int systemId,
                                                                     Authentication authentication) {
        log.debug("User {} requesting trade market for system {} in session: {}", authentication.getName(), systemId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        List<TradeOpportunityDto> market = gameSessionService.getTradeMarket(playerId, sessionId, systemId);
        return ResponseEntity.ok(market);
    }

    @PostMapping("/actions/assign-commander")
    public ResponseEntity<GameStateResponse> assignCommander(@PathVariable String sessionId,
                                                              @RequestParam int characterId,
                                                              Authentication authentication) {
        log.debug("User {} assigning commander {} in session: {}", authentication.getName(), characterId, sessionId);

        Long playerId = playerIdExtractor.extract(authentication);
        GameStateResponse response = gameSessionService.assignCommander(playerId, sessionId, characterId);
        return ResponseEntity.ok(response);
    }
}

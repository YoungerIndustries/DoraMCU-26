package com.yisy.modules.doramcu.controller.trade;
import com.yisy.modules.doramcu.hex.Ship;

import com.yisy.modules.doramcu.dto.trade.TradeRequest;
import com.yisy.modules.doramcu.dto.trade.TradeResponse;
import com.yisy.modules.doramcu.hex.Player;
import com.yisy.modules.doramcu.hex.manager.CreditExchangeService;
import com.yisy.modules.doramcu.hex.manager.TradeCareerService;
import com.yisy.modules.doramcu.hex.manager.TradeManager;
import com.yisy.modules.doramcu.hex.manager.TradeRouteNetwork;
import com.yisy.modules.doramcu.service.session.GameSessionService;
import com.yisy.modules.doramcu.service.trade.TradeService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.yisy.modules.doramcu.hex.Game;
import com.yisy.modules.doramcu.hex.RefinedResourceType;
import com.yisy.modules.doramcu.model.session.GameSession;
import com.yisy.modules.doramcu.traveller.Commodity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for trade operations.
 * <p>
 * Provides endpoints for checking market prices, buying and selling cargo,
 * executing trade missions between Traveller worlds, and accessing trade
 * career, credit exchange, route network, and economic event data.
 * </p>
 * <p>
 * All endpoints are under {@code /api/v1/traveller/trade}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/traveller/trade")
public class TradeController {

    private static final Logger log = LoggerFactory.getLogger(TradeController.class);

    private final TradeService tradeService;
    private final GameSessionService gameSessionService;

    public TradeController(TradeService tradeService, GameSessionService gameSessionService) {
        this.tradeService = tradeService;
        this.gameSessionService = gameSessionService;
    }

    /**
     * Gets market prices for a world.
     *
     * @param worldId the world ID
     * @return market prices for all available commodities
     */
    @GetMapping("/prices/{worldId}")
    public ResponseEntity<TradeResponse> getMarketPrices(@PathVariable int worldId) {
        log.debug("GET /api/v1/traveller/trade/prices/{} - market prices", worldId);
        TradeResponse response = tradeService.getMarketPrices(worldId);
        return ResponseEntity.ok(response);
    }

    /**
     * Buys cargo at a world.
     *
     * @param request the trade request (commodity, quantity, world, ship)
     * @return the purchase result with cost and cargo status
     */
    @PostMapping("/buy")
    public ResponseEntity<TradeResponse> buyCargo(@Valid @RequestBody TradeRequest request) {
        log.debug("POST /api/v1/traveller/trade/buy - {} units of {}",
                request.getQuantity(), request.getCommodity());
        TradeResponse response = tradeService.buyCargo(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Sells cargo at a world.
     *
     * @param request the trade request (commodity, quantity, world, ship)
     * @return the sale result with revenue and cargo status
     */
    @PostMapping("/sell")
    public ResponseEntity<TradeResponse> sellCargo(@Valid @RequestBody TradeRequest request) {
        log.debug("POST /api/v1/traveller/trade/sell - {} units of {}",
                request.getQuantity(), request.getCommodity());
        TradeResponse response = tradeService.sellCargo(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Executes a trade mission between two worlds.
     * <p>
     * Automatically finds the most profitable commodity, buys at the source
     * world, and sells at the destination world.
     * </p>
     *
     * @param request the trade request (source world, destination world, ship)
     * @return the trade mission result with profit and ROI
     */
    @PostMapping("/mission")
    public ResponseEntity<TradeResponse> executeTradeMission(
            @Valid @RequestBody TradeRequest request) {
        log.debug("POST /api/v1/traveller/trade/mission - {} -> {}",
                request.getWorldId(), request.getDestinationWorldId());
        TradeResponse response = tradeService.executeTradeMission(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all available commodities.
     *
     * @return list of commodity names
     */
    @GetMapping("/commodities")
    public ResponseEntity<List<String>> getCommodities() {
        log.debug("GET /api/v1/traveller/trade/commodities - listing all commodities");
        List<String> commodities = tradeService.getAvailableCommodities();
        return ResponseEntity.ok(commodities);
    }

    /**
     * Gets trade career summary for a session's current player.
     */
    @GetMapping("/career-summary")
    public ResponseEntity<Map<String, Object>> getCareerSummary(@RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/career-summary for session {}", sessionId);
        if (sessionId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("hasCommander", false);
            empty.put("tradeProfitBonus", 0.0);
            empty.put("cargoCapacityBonus", 0.0);
            empty.put("crewSkillModifier", 1.0);
            empty.put("tradeMissionsCompleted", 0);
            empty.put("tradeEarnings", 0L);
            return ResponseEntity.ok(empty);
        }
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null || session.getGame() == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("hasCommander", false);
            empty.put("tradeProfitBonus", 0.0);
            empty.put("cargoCapacityBonus", 0.0);
            empty.put("crewSkillModifier", 1.0);
            empty.put("tradeMissionsCompleted", 0);
            empty.put("tradeEarnings", 0L);
            return ResponseEntity.ok(empty);
        }
        Game game = (Game) session.getGame();
        Player player = game.getCurrentPlayer();
        TradeManager tradeManager = game.getTradeManager();
        Map<String, Object> summary;
        if (tradeManager != null && tradeManager.getTradeCareerService() != null) {
            summary = tradeManager.getTradeCareerService().getCareerSummary(player);
        } else {
            summary = new HashMap<>();
            summary.put("hasCommander", false);
        }
        return ResponseEntity.ok(summary);
    }

    /**
     * Gets credit exchange summary for a session's current player.
     */
    @GetMapping("/exchange-summary")
    public ResponseEntity<Map<String, Object>> getExchangeSummary(@RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/exchange-summary for session {}", sessionId);
        if (sessionId == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("tradeCredits", 0L);
            empty.put("resourceConversions", new HashMap<>());
            return ResponseEntity.ok(empty);
        }
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null || session.getGame() == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("tradeCredits", 0L);
            empty.put("resourceConversions", new HashMap<>());
            return ResponseEntity.ok(empty);
        }
        Game game = (Game) session.getGame();
        Player player = game.getCurrentPlayer();
        TradeManager tradeManager = game.getTradeManager();
        Map<String, Object> summary = new HashMap<>();
        if (tradeManager != null && tradeManager.getCreditExchangeService() != null) {
            Integer empireId = null;
            for (var e : game.getEmpires().values()) {
                if (e.getRulerPlayerId() == player.getId()) {
                    empireId = e.getId();
                    break;
                }
            }
            if (empireId != null) {
                summary = tradeManager.getCreditExchangeService().getExchangeSummary(empireId);
            }
        }
        summary.putIfAbsent("tradeCredits", 0L);
        summary.putIfAbsent("resourceConversions", new HashMap<>());
        return ResponseEntity.ok(summary);
    }

    /**
     * Gets active economic events for a session.
     */
    @GetMapping("/economic-events")
    public ResponseEntity<List<Map<String, Object>>> getEconomicEvents(@RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/economic-events for session {}", sessionId);
        if (sessionId == null) {
            return ResponseEntity.ok(List.of());
        }
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null || session.getGame() == null) {
            return ResponseEntity.ok(List.of());
        }
        Game game = (Game) session.getGame();
        TradeManager tradeManager = game.getTradeManager();
        List<Map<String, Object>> events = List.of();
        if (tradeManager != null && tradeManager.getTradeRouteNetwork() != null) {
            var networkEvents = tradeManager.getTradeRouteNetwork().getActiveEvents();
            events = networkEvents.stream().map(e -> {
                Map<String, Object> m = new HashMap<>();
                m.put("type", e.type.name());
                m.put("world", e.worldName);
                m.put("turnsRemaining", e.turnsRemaining);
                m.put("description", e.description);
                return m;
            }).collect(java.util.stream.Collectors.toList());
        }
        return ResponseEntity.ok(events);
    }

    /**
     * Gets profitable trade routes for a session.
     */
    @GetMapping("/routes")
    public ResponseEntity<List<Map<String, Object>>> getTradeRoutes(@RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/routes for session {}", sessionId);
        if (sessionId == null) {
            return ResponseEntity.ok(List.of());
        }
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null || session.getGame() == null) {
            return ResponseEntity.ok(List.of());
        }
        Game game = (Game) session.getGame();
        TradeManager tradeManager = game.getTradeManager();
        List<Map<String, Object>> routes = List.of();
        if (tradeManager != null && tradeManager.getTradeRouteNetwork() != null) {
            var networkRoutes = tradeManager.getTradeRouteNetwork().findAllProfitableRoutes();
            routes = networkRoutes.stream().map(r -> {
                Map<String, Object> m = new HashMap<>();
                m.put("sourcePlanetId", r.sourcePlanetId);
                m.put("destPlanetId", r.destPlanetId);
                m.put("sourceWorldName", r.sourceWorldName);
                m.put("destWorldName", r.destWorldName);
                m.put("commodity", r.commodity.getName());
                m.put("profitPerUnit", r.profitPerUnit);
                return m;
            }).collect(java.util.stream.Collectors.toList());
        }
        return ResponseEntity.ok(routes);
    }

    // ============================================================
    // Credit Exchange Endpoints
    // ============================================================

    /**
     * Gets the credit balance for an empire.
     */
    @GetMapping("/credits/balance/{empireId}")
    public ResponseEntity<Map<String, Object>> getCreditBalance(@PathVariable int empireId,
            @RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/credits/balance/{} for session {}", empireId, sessionId);
        long balance = 0L;
        if (sessionId != null) {
            CreditExchangeService exchange = getCreditExchangeService(sessionId);
            if (exchange != null) {
                balance = exchange.getTradeCreditBalance(empireId);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("empireId", empireId);
        result.put("balance", balance);
        return ResponseEntity.ok(result);
    }

    /**
     * Adds credits to an empire's trade credit balance.
     */
    @PostMapping("/credits/add")
    public ResponseEntity<Map<String, Object>> addCredits(@RequestBody CreditOperationRequest request,
            @RequestParam(required = false) String sessionId) {
        log.debug("POST /api/v1/traveller/trade/credits/add - {} credits to empire {}",
                request.getCredits(), request.getEmpireId());
        if (sessionId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "sessionId is required"));
        }
        CreditExchangeService exchange = getCreditExchangeService(sessionId);
        if (exchange == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active game session with trade manager"));
        }
        exchange.addTradeCredits(request.getEmpireId(), request.getCredits());
        long balance = exchange.getTradeCreditBalance(request.getEmpireId());
        Map<String, Object> result = new HashMap<>();
        result.put("empireId", request.getEmpireId());
        result.put("added", request.getCredits());
        result.put("balance", balance);
        return ResponseEntity.ok(result);
    }

    /**
     * Spends credits from an empire's trade credit balance.
     */
    @PostMapping("/credits/spend")
    public ResponseEntity<Map<String, Object>> spendCredits(@RequestBody CreditOperationRequest request,
            @RequestParam(required = false) String sessionId) {
        log.debug("POST /api/v1/traveller/trade/credits/spend - {} credits from empire {}",
                request.getCredits(), request.getEmpireId());
        if (sessionId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "sessionId is required"));
        }
        CreditExchangeService exchange = getCreditExchangeService(sessionId);
        if (exchange == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active game session with trade manager"));
        }
        boolean success = exchange.spendTradeCredits(request.getEmpireId(), request.getCredits());
        long balance = exchange.getTradeCreditBalance(request.getEmpireId());
        Map<String, Object> result = new HashMap<>();
        result.put("empireId", request.getEmpireId());
        result.put("success", success);
        result.put("amount", request.getCredits());
        result.put("balance", balance);
        return ResponseEntity.ok(result);
    }

    /**
     * Gets the exchange rate for a commodity to resource conversion.
     */
    @GetMapping("/exchange-rate/{commodity}")
    public ResponseEntity<Map<String, Object>> getExchangeRate(@PathVariable String commodity,
            @RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/exchange-rate/{} for session {}", commodity, sessionId);
        Map<String, Object> result = new HashMap<>();
        result.put("commodity", commodity);
        try {
            Commodity comm = Commodity.valueOf(commodity.toUpperCase());
            RefinedResourceType resourceType = mapCommodityToResource(comm);
            result.put("resourceType", resourceType.name());
            result.put("creditToResourceRate", 1.0);
            result.put("resourceToCreditRate", 1.0);
        } catch (IllegalArgumentException e) {
            result.put("error", "Unknown commodity: " + commodity);
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Maps a commodity to its corresponding resource type.
     */
    @GetMapping("/mapping/commodity-to-resource/{commodity}")
    public ResponseEntity<Map<String, String>> getCommodityToResourceMapping(@PathVariable String commodity) {
        log.debug("GET /api/v1/traveller/trade/mapping/commodity-to-resource/{}", commodity);
        Map<String, String> result = new HashMap<>();
        try {
            Commodity comm = Commodity.valueOf(commodity.toUpperCase());
            RefinedResourceType resourceType = mapCommodityToResource(comm);
            result.put("commodity", comm.name());
            result.put("resourceType", resourceType.name());
        } catch (IllegalArgumentException e) {
            result.put("error", "Unknown commodity: " + commodity);
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Maps a resource type to its corresponding commodity.
     */
    @GetMapping("/mapping/resource-to-commodity/{resourceType}")
    public ResponseEntity<Map<String, String>> getResourceToCommodityMapping(@PathVariable String resourceType) {
        log.debug("GET /api/v1/traveller/trade/mapping/resource-to-commodity/{}", resourceType);
        Map<String, String> result = new HashMap<>();
        try {
            RefinedResourceType res = RefinedResourceType.valueOf(resourceType.toUpperCase());
            Commodity comm = mapResourceToCommodity(res);
            result.put("resourceType", res.name());
            result.put("commodity", comm.name());
        } catch (IllegalArgumentException e) {
            result.put("error", "Unknown resource type: " + resourceType);
            return ResponseEntity.badRequest().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Lists all commodity-to-resource mappings.
     */
    @GetMapping("/mapping/all")
    public ResponseEntity<List<Map<String, String>>> getAllMappings() {
        log.debug("GET /api/v1/traveller/trade/mapping/all");
        List<Map<String, String>> mappings = new java.util.ArrayList<>();
        for (Commodity comm : Commodity.values()) {
            Map<String, String> mapping = new HashMap<>();
            mapping.put("commodity", comm.name());
            mapping.put("resourceType", mapCommodityToResource(comm).name());
            mappings.add(mapping);
        }
        return ResponseEntity.ok(mappings);
    }

    /**
     * Gets all exchange summaries for all empires in a session.
     */
    @GetMapping("/exchange-all")
    public ResponseEntity<List<Map<String, Object>>> getAllExchangeSummaries(
            @RequestParam(required = false) String sessionId) {
        log.debug("GET /api/v1/traveller/trade/exchange-all for session {}", sessionId);
        if (sessionId == null) {
            return ResponseEntity.ok(List.of());
        }
        CreditExchangeService exchange = getCreditExchangeService(sessionId);
        if (exchange == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(exchange.getAllExchangeSummaries());
    }

    // ============================================================
    // Helper Methods
    // ============================================================

    private CreditExchangeService getCreditExchangeService(String sessionId) {
        GameSession session = gameSessionService.getGameSession(sessionId);
        if (session == null || session.getGame() == null) {
            return null;
        }
        Game game = (Game) session.getGame();
        TradeManager tradeManager = game.getTradeManager();
        if (tradeManager != null) {
            return tradeManager.getCreditExchangeService();
        }
        return null;
    }

    private RefinedResourceType mapCommodityToResource(Commodity commodity) {
        if (commodity == null) {
            return RefinedResourceType.METAL_ALLOY;
        }
        switch (commodity) {
            case FOOD:
            case MEDICINE:
                return RefinedResourceType.NUTRIENT;
            case FUEL:
            case CHEMICALS:
                return RefinedResourceType.FUEL;
            case ELECTRONICS:
            case INFORMATION:
                return RefinedResourceType.POWER_CELL;
            case TEXTILES:
            case TOBACCO:
            case ALCOHOL:
                return RefinedResourceType.COMPOSITE;
            case METALS:
            case MACHINERY:
            case WEAPONS:
            case GEMS:
            case LUXURY:
            case TIMBER:
            case FURS:
            case SPICES:
            case ANIMALS:
            case SLAVES:
            case RELIGIOUS:
            default:
                return RefinedResourceType.METAL_ALLOY;
        }
    }

    private Commodity mapResourceToCommodity(RefinedResourceType resourceType) {
        if (resourceType == null) {
            return Commodity.METALS;
        }
        switch (resourceType) {
            case NUTRIENT:
                return Commodity.FOOD;
            case FUEL:
                return Commodity.FUEL;
            case POWER_CELL:
                return Commodity.ELECTRONICS;
            case COMPOSITE:
                return Commodity.TEXTILES;
            case METAL_ALLOY:
            default:
                return Commodity.METALS;
        }
    }

    // ============================================================
    // DTOs
    // ============================================================

    public static class CreditOperationRequest {
        private int empireId;
        private long credits;

        public int getEmpireId() { return empireId; }
        public void setEmpireId(int id) { this.empireId = id; }
        public long getCredits() { return credits; }
        public void setCredits(long credits) { this.credits = credits; }
    }
}

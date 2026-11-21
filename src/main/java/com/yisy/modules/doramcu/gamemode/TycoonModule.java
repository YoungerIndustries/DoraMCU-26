package com.yisy.modules.doramcu.gamemode;
import com.yisy.modules.doramcu.model.players.Player;

import com.yisy.modules.doramcu.gamecore.GameMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tycoon game mode module: economic simulation with resource management,
 * trade routes, industry chains, and profit optimization.
 * <p>
 * Provides industry building, trade route management, market price fluctuations,
 * and profit tracking for tycoon-style gameplay.
 * </p>
 */
@Component
public class TycoonModule implements GameModeModule {

    private static final Logger log = LoggerFactory.getLogger(TycoonModule.class);

    // Galaxy name -> economy state
    private final Map<String, EconomyState> economies = new ConcurrentHashMap<>();

    // Galaxy name -> trade routes
    private final Map<String, List<TradeRoute>> tradeRoutes = new ConcurrentHashMap<>();

    // Galaxy name -> industries
    private final Map<String, List<Industry>> industries = new ConcurrentHashMap<>();

    // Resource types
    private static final List<String> RESOURCES = Arrays.asList(
            "Minerals", "Energy", "Food", "Water", "Metals", "Crystals",
            "Bio-Matter", "Fuel", "Medicine", "Luxury Goods"
    );

    // Industry types
    private static final List<String> INDUSTRIES = Arrays.asList(
            "Mining Station", "Refinery", "Factory", "Trading Post",
            "Research Lab", "Shipyard", "Farm", "Power Plant",
            "Hospital", "Entertainment Complex"
    );

    @Override
    public GameMode getGameMode() {
        return GameMode.TYCOON;
    }

    @Override
    public String getModuleName() {
        return "Tycoon Module";
    }

    @Override
    public String getDescription() {
        return "Economic simulation with trade routes, industry chains, and profit optimization";
    }

    @Override
    public void initializeForGalaxy(String galaxyName, String loreSlug) {
        log.info("Initializing Tycoon module for galaxy: {} with lore: {}", galaxyName, loreSlug);
        economies.put(galaxyName, new EconomyState());
        tradeRoutes.put(galaxyName, new ArrayList<>());
        industries.put(galaxyName, new ArrayList<>());

        // Build initial industries
        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            String type = INDUSTRIES.get(random.nextInt(INDUSTRIES.size()));
            industries.get(galaxyName).add(new Industry(type, 1 + random.nextInt(3)));
        }
        log.info("Built initial industries for galaxy: {}", galaxyName);
    }

    @Override
    public String processTurn(String galaxyName, int turnNumber, String playerId) {
        EconomyState economy = economies.getOrDefault(galaxyName, new EconomyState());
        List<TradeRoute> routes = tradeRoutes.getOrDefault(galaxyName, Collections.emptyList());
        List<Industry> ind = industries.getOrDefault(galaxyName, Collections.emptyList());

        StringBuilder summary = new StringBuilder();
        summary.append("Tycoon Turn ").append(turnNumber).append(" in ").append(galaxyName).append(":\n");

        // Industry production
        int totalProduction = 0;
        for (Industry industry : ind) {
            int production = industry.getLevel() * 10;
            economy.credits += production;
            totalProduction += production;
            industry.setProductionCount(industry.getProductionCount() + production);
        }
        summary.append("  Industry production: +").append(totalProduction).append(" credits\n");

        // Trade route income
        int tradeIncome = 0;
        for (TradeRoute route : routes) {
            if (route.isActive()) {
                route.progress();
                int income = route.getProfit();
                economy.credits += income;
                tradeIncome += income;
                summary.append("  Trade route ").append(route.getName())
                       .append(": +").append(income).append(" credits\n");
            }
        }
        summary.append("  Total trade income: +").append(tradeIncome).append(" credits\n");

        // Market price fluctuations
        Random random = new Random();
        for (int i = 0; i < economy.marketPrices.size(); i++) {
            double change = (random.nextDouble() - 0.5) * 0.2; // Â±10%
            double price = economy.marketPrices.get(i);
            price = Math.max(0.1, price * (1 + change));
            economy.marketPrices.set(i, price);
        }

        // Build new industry if profitable
        if (economy.credits > 500 && ind.size() < 10 && turnNumber % 3 == 0) {
            String type = INDUSTRIES.get(random.nextInt(INDUSTRIES.size()));
            ind.add(new Industry(type, 1));
            economy.credits -= 200;
            summary.append("  âœ¦ Built new ").append(type).append(" (-200 credits)\n");
        }

        // Establish new trade route
        if (economy.credits > 1000 && routes.size() < 5 && turnNumber % 5 == 0) {
            String from = RESOURCES.get(random.nextInt(RESOURCES.size()));
            String to = RESOURCES.get(random.nextInt(RESOURCES.size()));
            TradeRoute newRoute = new TradeRoute(from + " â†’ " + to);
            routes.add(newRoute);
            economy.credits -= 500;
            summary.append("  âœ¦ New trade route: ").append(newRoute.getName()).append(" (-500 credits)\n");
        }

        // Market snapshot
        summary.append("  Market: ");
        for (int i = 0; i < Math.min(3, economy.marketPrices.size()); i++) {
            if (i > 0) summary.append(", ");
            summary.append(RESOURCES.get(i)).append("=").append(String.format("%.2f", economy.marketPrices.get(i)));
        }

        summary.append("\n  Total credits: ").append(economy.credits)
               .append(", Industries: ").append(ind.size())
               .append(", Trade routes: ").append(routes.size());
        return summary.toString();
    }

    @Override
    public String getStateSummary(String galaxyName) {
        EconomyState economy = economies.getOrDefault(galaxyName, new EconomyState());
        List<Industry> ind = industries.getOrDefault(galaxyName, Collections.emptyList());
        List<TradeRoute> routes = tradeRoutes.getOrDefault(galaxyName, Collections.emptyList());
        return String.format("Tycoon: %d credits, %d industries, %d trade routes",
                economy.credits, ind.size(), routes.size());
    }

    @Override
    public boolean isActiveForGalaxy(String galaxyName) {
        return economies.containsKey(galaxyName);
    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String serializeState(String galaxyName) {
        try {
            Map<String, Object> state = new LinkedHashMap<>();
            EconomyState economy = economies.get(galaxyName);
            if (economy == null) return null;
            state.put("credits", economy.credits);
            state.put("totalProfit", economy.totalProfit);
            state.put("totalLoss", economy.totalLoss);
            state.put("marketPrices", economy.marketPrices);
            List<Industry> ind = industries.getOrDefault(galaxyName, Collections.emptyList());
            List<Map<String, Object>> indData = new ArrayList<>();
            for (Industry i : ind) {
                Map<String, Object> id = new LinkedHashMap<>();
                id.put("type", i.getType());
                id.put("level", i.getLevel());
                id.put("productionCount", i.getProductionCount());
                indData.add(id);
            }
            state.put("industries", indData);
            List<TradeRoute> routes = tradeRoutes.getOrDefault(galaxyName, Collections.emptyList());
            List<Map<String, Object>> routeData = new ArrayList<>();
            for (TradeRoute r : routes) {
                Map<String, Object> rd = new LinkedHashMap<>();
                rd.put("name", r.getName());
                rd.put("baseProfit", r.getBaseProfit());
                rd.put("cycles", r.getCycles());
                rd.put("active", r.isActive());
                routeData.add(rd);
            }
            state.put("tradeRoutes", routeData);
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Tycoon state for {}: {}", galaxyName, e.getMessage());
            return null;
        }
    }

    @Override
    public void restoreState(String galaxyName, String stateJson) {
        if (stateJson == null || stateJson.isEmpty()) return;
        try {
            Map<String, Object> state = objectMapper.readValue(stateJson, Map.class);
            EconomyState economy = new EconomyState();
            economy.credits = ((Number) state.getOrDefault("credits", 1000)).intValue();
            economy.totalProfit = ((Number) state.getOrDefault("totalProfit", 0)).intValue();
            economy.totalLoss = ((Number) state.getOrDefault("totalLoss", 0)).intValue();
            @SuppressWarnings("unchecked")
            List<Double> prices = (List<Double>) state.getOrDefault("marketPrices", Collections.nCopies(10, 1.0));
            economy.marketPrices = new ArrayList<>(prices);
            economies.put(galaxyName, economy);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> indData = (List<Map<String, Object>>) state.get("industries");
            if (indData != null) {
                List<Industry> ind = new ArrayList<>();
                for (Map<String, Object> id : indData) {
                    ind.add(new Industry((String) id.get("type"), ((Number) id.get("level")).intValue()));
                    ind.get(ind.size() - 1).setProductionCount(((Number) id.get("productionCount")).intValue());
                }
                industries.put(galaxyName, ind);
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> routeData = (List<Map<String, Object>>) state.get("tradeRoutes");
            if (routeData != null) {
                List<TradeRoute> routes = new ArrayList<>();
                for (Map<String, Object> rd : routeData) {
                    TradeRoute r = new TradeRoute((String) rd.get("name"));
                    r.setBaseProfit(((Number) rd.get("baseProfit")).intValue());
                    r.setCycles(((Number) rd.get("cycles")).intValue());
                    r.setActive((Boolean) rd.get("active"));
                    routes.add(r);
                }
                tradeRoutes.put(galaxyName, routes);
            }
            log.info("Restored Tycoon state for {}: {} credits, {} industries", galaxyName, economy.credits,
                    industries.getOrDefault(galaxyName, Collections.emptyList()).size());
        } catch (Exception e) {
            log.error("Failed to restore Tycoon state for {}: {}", galaxyName, e.getMessage(), e);
        }
    }

    /**
     * Economy state for tycoon gameplay.
     */
    public static class EconomyState {
        int credits = 1000;
        int totalProfit = 0;
        int totalLoss = 0;
        List<Double> marketPrices = new ArrayList<>(Collections.nCopies(10, 1.0));
    }

    /**
     * Industry for tycoon gameplay.
     */
    public static class Industry {
        private final String type;
        private int level;
        private int productionCount;

        public Industry(String type, int level) {
            this.type = type;
            this.level = level;
            this.productionCount = 0;
        }

        public String getType() { return type; }
        public int getLevel() { return level; }
        public int getProductionCount() { return productionCount; }
        public void setProductionCount(int count) { this.productionCount = count; }
    }

    /**
     * Trade route for tycoon gameplay.
     */
    public static class TradeRoute {
        private final String name;
        private int baseProfit;
        private int cycles;
        private boolean active;

        public TradeRoute(String name) {
            this.name = name;
            this.baseProfit = 20 + new Random().nextInt(50);
            this.cycles = 0;
            this.active = true;
        }

        public void progress() {
            if (active) {
                cycles++;
            }
        }

        public int getProfit() {
            return baseProfit + (cycles * 5);
        }

        public String getName() { return name; }
        public int getBaseProfit() { return baseProfit; }
        public void setBaseProfit(int baseProfit) { this.baseProfit = baseProfit; }
        public int getCycles() { return cycles; }
        public void setCycles(int cycles) { this.cycles = cycles; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
    }
}

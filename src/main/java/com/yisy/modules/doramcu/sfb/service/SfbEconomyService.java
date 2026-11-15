package com.yisy.modules.doramcu.sfb.service;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Economy service for SFB campaign AI.
 * <p>
 * Manages resources, shipbuilding costs, trade between factions,
 * and economic strategy for AI-controlled factions.
 * </p>
 * <p>
 * Resources include credits, materials, fuel, and manpower.
 * Shipbuilding consumes resources based on ship class and size.
 * Trade between factions is affected by diplomatic relations.
 * </p>
 */

@Service
public class SfbEconomyService {

    /**
     * Types of resources in the SFB economy.
     */
    public enum ResourceType {
        CREDITS("Credits", "Universal currency for trade and construction"),
        MATERIALS("Materials", "Raw materials for shipbuilding and repairs"),
        FUEL("Fuel", "Propellant for ships and stations"),
        MANPOWER("Manpower", "Crew and personnel for ships"),
        TECHNOLOGY("Technology", "Research points for upgrades");

        private final String name;
        private final String description;

        ResourceType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
    }

    /**
     * Ship class categories for cost calculation.
     */
    public enum ShipClassCategory {
        DESTROYER(100, 50, 20, 10),
        CRUISER(250, 120, 50, 25),
        BATTLESHIP(500, 250, 100, 50),
        DREADNOUGHT(1000, 500, 200, 100),
        CARRIER(750, 350, 150, 75),
        FRIGATE(50, 25, 10, 5),
        STATION(2000, 1000, 400, 200);

        private final int creditsCost;
        private final int materialsCost;
        private final int fuelCost;
        private final int manpowerCost;

        ShipClassCategory(int creditsCost, int materialsCost, int fuelCost, int manpowerCost) {
            this.creditsCost = creditsCost;
            this.materialsCost = materialsCost;
            this.fuelCost = fuelCost;
            this.manpowerCost = manpowerCost;
        }

        public int getCreditsCost() { return creditsCost; }
        public int getMaterialsCost() { return materialsCost; }
        public int getFuelCost() { return fuelCost; }
        public int getManpowerCost() { return manpowerCost; }
    }

    /**
     * Resource costs for building a ship.
     */
    public static class ShipBuildCost {
        private final ShipClassCategory category;
        private final int credits;
        private final int materials;
        private final int fuel;
        private final int manpower;

        private ShipBuildCost(ShipClassCategory category, double multiplier) {
            this.category = category;
            this.credits = (int) (category.creditsCost * multiplier);
            this.materials = (int) (category.materialsCost * multiplier);
            this.fuel = (int) (category.fuelCost * multiplier);
            this.manpower = (int) (category.manpowerCost * multiplier);
        }

        public ShipClassCategory getCategory() { return category; }
        public int getCredits() { return credits; }
        public int getMaterials() { return materials; }
        public int getFuel() { return fuel; }
        public int getManpower() { return manpower; }
        public int getTotalCost() { return credits + materials + fuel + manpower; }

        public static ShipBuildCost of(ShipClassCategory category) {
            return new ShipBuildCost(category, 1.0);
        }

        public static ShipBuildCost of(ShipClassCategory category, double multiplier) {
            return new ShipBuildCost(category, multiplier);
        }
    }

    /**
     * Faction economy state.
     */
    public static class FactionEconomy {
        private final String factionName;
        private final Map<ResourceType, Integer> resources;
        private final Map<ResourceType, Integer> incomePerTurn;
        private final Map<ResourceType, Integer> maxStorage;
        private int turnsSinceLastTrade;

        private FactionEconomy(Builder builder) {
            this.factionName = builder.factionName;
            this.resources = new EnumMap<>(ResourceType.class);
            this.incomePerTurn = new EnumMap<>(ResourceType.class);
            this.maxStorage = new EnumMap<>(ResourceType.class);
            this.turnsSinceLastTrade = 0;

            for (ResourceType type : ResourceType.values()) {
                this.resources.put(type, builder.resources.getOrDefault(type, 0));
                this.incomePerTurn.put(type, builder.incomePerTurn.getOrDefault(type, 0));
                this.maxStorage.put(type, builder.maxStorage.getOrDefault(type, 10000));
            }
        }

        public String getFactionName() { return factionName; }
        public Map<ResourceType, Integer> getResources() { return Collections.unmodifiableMap(resources); }
        public Map<ResourceType, Integer> getIncomePerTurn() { return Collections.unmodifiableMap(incomePerTurn); }
        public Map<ResourceType, Integer> getMaxStorage() { return Collections.unmodifiableMap(maxStorage); }
        public int getTurnsSinceLastTrade() { return turnsSinceLastTrade; }

        public int getResource(ResourceType type) {
            return resources.getOrDefault(type, 0);
        }

        public boolean canAfford(ShipBuildCost cost) {
            return resources.getOrDefault(ResourceType.CREDITS, 0) >= cost.credits
                    && resources.getOrDefault(ResourceType.MATERIALS, 0) >= cost.materials
                    && resources.getOrDefault(ResourceType.FUEL, 0) >= cost.fuel
                    && resources.getOrDefault(ResourceType.MANPOWER, 0) >= cost.manpower;
        }

        public void spendResources(ShipBuildCost cost) {
            resources.put(ResourceType.CREDITS, resources.get(ResourceType.CREDITS) - cost.credits);
            resources.put(ResourceType.MATERIALS, resources.get(ResourceType.MATERIALS) - cost.materials);
            resources.put(ResourceType.FUEL, resources.get(ResourceType.FUEL) - cost.fuel);
            resources.put(ResourceType.MANPOWER, resources.get(ResourceType.MANPOWER) - cost.manpower);
        }

        public void addResource(ResourceType type, int amount) {
            int current = resources.getOrDefault(type, 0);
            int max = maxStorage.getOrDefault(type, 10000);
            resources.put(type, Math.min(max, current + amount));
        }

        public void removeResource(ResourceType type, int amount) {
            int current = resources.getOrDefault(type, 0);
            resources.put(type, Math.max(0, current - amount));
        }

        public void advanceTurn() {
            for (ResourceType type : ResourceType.values()) {
                addResource(type, incomePerTurn.getOrDefault(type, 0));
            }
            turnsSinceLastTrade++;
        }

        public static Builder builder(String factionName) { return new Builder(factionName); }

        public static class Builder {
            private final String factionName;
            private final Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);
            private final Map<ResourceType, Integer> incomePerTurn = new EnumMap<>(ResourceType.class);
            private final Map<ResourceType, Integer> maxStorage = new EnumMap<>(ResourceType.class);

            public Builder(String factionName) {
                this.factionName = factionName;
            }

            public Builder resource(ResourceType type, int amount) { this.resources.put(type, amount); return this; }
            public Builder income(ResourceType type, int amount) { this.incomePerTurn.put(type, amount); return this; }
            public Builder maxStorage(ResourceType type, int amount) { this.maxStorage.put(type, amount); return this; }
            public Builder credits(int amount) { this.resources.put(ResourceType.CREDITS, amount); return this; }
            public Builder materials(int amount) { this.resources.put(ResourceType.MATERIALS, amount); return this; }
            public Builder fuel(int amount) { this.resources.put(ResourceType.FUEL, amount); return this; }
            public Builder manpower(int amount) { this.resources.put(ResourceType.MANPOWER, amount); return this; }

            public FactionEconomy build() {
                if (this.factionName == null || this.factionName.isEmpty()) {
                    throw new IllegalArgumentException("Faction name is required");
                }
                return new FactionEconomy(this);
            }
        }
    }

    /**
     * Trade deal between two factions.
     */
    public static class TradeDeal {
        private final String id;
        private final String factionA;
        private final String factionB;
        private final Map<ResourceType, Integer> factionAGives;
        private final Map<ResourceType, Integer> factionBGives;
        private final boolean executed;

        private TradeDeal(Builder builder) {
            this.id = builder.id;
            this.factionA = builder.factionA;
            this.factionB = builder.factionB;
            this.factionAGives = new EnumMap<>(ResourceType.class);
            this.factionBGives = new EnumMap<>(ResourceType.class);
            this.factionAGives.putAll(builder.factionAGives);
            this.factionBGives.putAll(builder.factionBGives);
            this.executed = false;
        }

        public String getId() { return id; }
        public String getFactionA() { return factionA; }
        public String getFactionB() { return factionB; }
        public Map<ResourceType, Integer> getFactionAGives() { return Collections.unmodifiableMap(factionAGives); }
        public Map<ResourceType, Integer> getFactionBGives() { return Collections.unmodifiableMap(factionBGives); }
        public boolean isExecuted() { return executed; }

        public static Builder builder() { return new Builder(); }

        public static class Builder {
            private String id;
            private String factionA;
            private String factionB;
            private final Map<ResourceType, Integer> factionAGives = new EnumMap<>(ResourceType.class);
            private final Map<ResourceType, Integer> factionBGives = new EnumMap<>(ResourceType.class);

            public Builder id(String id) { this.id = id; return this; }
            public Builder factionA(String factionA) { this.factionA = factionA; return this; }
            public Builder factionB(String factionB) { this.factionB = factionB; return this; }
            public Builder factionAGives(ResourceType type, int amount) { this.factionAGives.put(type, amount); return this; }
            public Builder factionBGives(ResourceType type, int amount) { this.factionBGives.put(type, amount); return this; }

            public TradeDeal build() {
                if (this.id == null) this.id = UUID.randomUUID().toString().substring(0, 8);
                return new TradeDeal(this);
            }
        }
    }

    /**
     * Economic strategy recommendation for AI.
     */
    public static class EconomicRecommendation {
        private final String factionName;
        private final List<String> recommendations;
        private final ShipClassCategory suggestedBuild;
        private final int shipsAffordable;
        private final Map<String, Object> details;

        private EconomicRecommendation(Builder builder) {
            this.factionName = builder.factionName;
            this.recommendations = new ArrayList<>(builder.recommendations);
            this.suggestedBuild = builder.suggestedBuild;
            this.shipsAffordable = builder.shipsAffordable;
            this.details = Collections.unmodifiableMap(new HashMap<>(builder.details));
        }

        public String getFactionName() { return factionName; }
        public List<String> getRecommendations() { return recommendations; }
        public ShipClassCategory getSuggestedBuild() { return suggestedBuild; }
        public int getShipsAffordable() { return shipsAffordable; }
        public Map<String, Object> getDetails() { return details; }

        public static Builder builder(String factionName) { return new Builder(factionName); }

        public static class Builder {
            private final String factionName;
            private final List<String> recommendations = new ArrayList<>();
            private ShipClassCategory suggestedBuild;
            private int shipsAffordable;
            private final Map<String, Object> details = new HashMap<>();

            public Builder(String factionName) { this.factionName = factionName; }

            public Builder recommendation(String recommendation) { this.recommendations.add(recommendation); return this; }
            public Builder suggestedBuild(ShipClassCategory suggestedBuild) { this.suggestedBuild = suggestedBuild; return this; }
            public Builder shipsAffordable(int shipsAffordable) { this.shipsAffordable = shipsAffordable; return this; }
            public Builder detail(String key, Object value) { this.details.put(key, value); return this; }

            public EconomicRecommendation build() { return new EconomicRecommendation(this); }
        }
    }

    private final Map<String, FactionEconomy> economies = new ConcurrentHashMap<>();
    private final Random random = new Random();

    /**
     * Register a faction's economy.
     */
    public void registerEconomy(FactionEconomy economy) {
        economies.put(economy.getFactionName(), economy);
    }

    /**
     * Register a faction with default economy settings.
     */
    public void registerEconomy(String name, int credits, int materials, int fuel, int manpower) {
        registerEconomy(FactionEconomy.builder(name)
                .credits(credits)
                .materials(materials)
                .fuel(fuel)
                .manpower(manpower)
                .income(ResourceType.CREDITS, 100)
                .income(ResourceType.MATERIALS, 50)
                .income(ResourceType.FUEL, 30)
                .income(ResourceType.MANPOWER, 20)
                .build());
    }

    /**
     * Get a faction's economy.
     */
    public FactionEconomy getEconomy(String factionName) {
        return economies.get(factionName);
    }

    /**
     * Get all registered faction names.
     */
    public List<String> getAllFactions() {
        return new ArrayList<>(economies.keySet());
    }

    /**
     * Calculate the cost to build a ship of the given category.
     */
    public ShipBuildCost getBuildCost(ShipClassCategory category) {
        return ShipBuildCost.of(category);
    }

    /**
     * Calculate the cost to build a ship with a multiplier (e.g., for tech level).
     */
    public ShipBuildCost getBuildCost(ShipClassCategory category, double multiplier) {
        return ShipBuildCost.of(category, multiplier);
    }

    /**
     * Check if a faction can afford to build a ship.
     */
    public boolean canAfford(String factionName, ShipClassCategory category) {
        FactionEconomy economy = economies.get(factionName);
        if (economy == null) return false;
        return economy.canAfford(ShipBuildCost.of(category));
    }

    /**
     * Spend resources to build a ship.
     */
    public boolean buildShip(String factionName, ShipClassCategory category) {
        FactionEconomy economy = economies.get(factionName);
        if (economy == null) return false;

        ShipBuildCost cost = ShipBuildCost.of(category);
        if (!economy.canAfford(cost)) return false;

        economy.spendResources(cost);
        return true;
    }

    /**
     * Calculate how many ships of a given category a faction can afford.
     */
    public int shipsAffordable(String factionName, ShipClassCategory category) {
        FactionEconomy economy = economies.get(factionName);
        if (economy == null) return 0;

        ShipBuildCost cost = ShipBuildCost.of(category);
        int byCredits = economy.getResource(ResourceType.CREDITS) / Math.max(1, cost.credits);
        int byMaterials = economy.getResource(ResourceType.MATERIALS) / Math.max(1, cost.materials);
        int byFuel = economy.getResource(ResourceType.FUEL) / Math.max(1, cost.fuel);
        int byManpower = economy.getResource(ResourceType.MANPOWER) / Math.max(1, cost.manpower);

        return Math.min(Math.min(byCredits, byMaterials), Math.min(byFuel, byManpower));
    }

    /**
     * Execute a trade deal between two factions.
     * Trade success is affected by diplomatic relations.
     */
    public boolean executeTrade(TradeDeal deal, SfbDiplomacyService diplomacy) {
        FactionEconomy econA = economies.get(deal.getFactionA());
        FactionEconomy econB = economies.get(deal.getFactionB());

        if (econA == null || econB == null) return false;

        // Check if both factions can afford their side of the trade
        for (Map.Entry<ResourceType, Integer> entry : deal.getFactionAGives().entrySet()) {
            if (econA.getResource(entry.getKey()) < entry.getValue()) return false;
        }
        for (Map.Entry<ResourceType, Integer> entry : deal.getFactionBGives().entrySet()) {
            if (econB.getResource(entry.getKey()) < entry.getValue()) return false;
        }

        // Execute the trade
        for (Map.Entry<ResourceType, Integer> entry : deal.getFactionAGives().entrySet()) {
            econA.removeResource(entry.getKey(), entry.getValue());
            econB.addResource(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<ResourceType, Integer> entry : deal.getFactionBGives().entrySet()) {
            econB.removeResource(entry.getKey(), entry.getValue());
            econA.addResource(entry.getKey(), entry.getValue());
        }

        econA.advanceTurn();
        econB.advanceTurn();
        return true;
    }

    /**
     * Generate economic recommendations for an AI faction.
     */
    public EconomicRecommendation getRecommendations(String factionName) {
        FactionEconomy economy = economies.get(factionName);
        if (economy == null) {
            return EconomicRecommendation.builder(factionName)
                    .recommendation("Faction economy not registered")
                    .build();
        }

        EconomicRecommendation.Builder builder = EconomicRecommendation.builder(factionName);

        // Analyze resource levels
        int credits = economy.getResource(ResourceType.CREDITS);
        int materials = economy.getResource(ResourceType.MATERIALS);
        int fuel = economy.getResource(ResourceType.FUEL);
        int manpower = economy.getResource(ResourceType.MANPOWER);

        // Check for resource shortages
        if (credits < 100) {
            builder.recommendation("Low credits: focus on trade and income generation");
        }
        if (materials < 50) {
            builder.recommendation("Low materials: prioritize material acquisition");
        }
        if (fuel < 20) {
            builder.recommendation("Low fuel: secure fuel sources before expansion");
        }
        if (manpower < 10) {
            builder.recommendation("Low manpower: recruit more personnel");
        }

        // Suggest ship builds based on resources
        for (ShipClassCategory category : ShipClassCategory.values()) {
            if (category == ShipClassCategory.STATION) continue;
            int affordable = shipsAffordable(factionName, category);
            if (affordable > 0) {
                builder.suggestedBuild(category);
                builder.shipsAffordable(Math.max(builder.shipsAffordable, affordable));
                break;
            }
        }

        // General recommendations
        if (credits > 1000 && materials > 500) {
            builder.recommendation("Strong economy: consider fleet expansion");
        }
        if (economy.getTurnsSinceLastTrade() > 5) {
            builder.recommendation("No recent trade: consider diplomatic overtures for trade deals");
        }

        builder.detail("credits", credits)
               .detail("materials", materials)
               .detail("fuel", fuel)
               .detail("manpower", manpower)
               .detail("turnsSinceLastTrade", economy.getTurnsSinceLastTrade());

        return builder.build();
    }

    /**
     * Advance all faction economies by one turn.
     */
    public void advanceTurn() {
        economies.values().forEach(FactionEconomy::advanceTurn);
    }

    /**
     * Get economic statistics for a faction.
     */
    public Map<String, Object> getEconomyStats(String factionName) {
        FactionEconomy economy = economies.get(factionName);
        if (economy == null) {
            return Map.of("error", "Faction economy not found");
        }

        return Map.of(
                "faction", factionName,
                "resources", economy.getResources(),
                "incomePerTurn", economy.getIncomePerTurn(),
                "maxStorage", economy.getMaxStorage(),
                "turnsSinceLastTrade", economy.getTurnsSinceLastTrade()
        );
    }

    /**
     * Get economy statistics for all factions.
     */
    public Map<String, Map<String, Object>> getAllEconomyStats() {
        Map<String, Map<String, Object>> stats = new HashMap<>();
        for (String faction : economies.keySet()) {
            stats.put(faction, getEconomyStats(faction));
        }
        return stats;
    }

    /**
     * Clear all economy data.
     */
    public void clear() {
        economies.clear();
    }
}

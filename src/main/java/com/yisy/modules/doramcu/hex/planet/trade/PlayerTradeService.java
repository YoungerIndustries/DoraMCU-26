package com.yisy.modules.doramcu.hex.planet.trade;

import com.yisy.modules.doramcu.hex.planet.CargoType;
import com.yisy.modules.doramcu.hex.planet.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Allows players to create and manage trade routes between stations.
 * <p>
 * Trade routes generate income based on cargo transported. Each route connects
 * two stations and transports a specific cargo type. Income is calculated based
 * on cargo value, distance, and demand.
 * </p>
 */
@Service
public class PlayerTradeService {

    private static final Logger log = LoggerFactory.getLogger(PlayerTradeService.class);

    private final Map<String, TradeRoute> routes = new ConcurrentHashMap<>();
    private final AtomicLong routeCounter = new AtomicLong(0);

    /**
     * Create a new trade route between two stations.
     *
     * @param playerId the player creating the route
     * @param fromStation the origin station
     * @param toStation the destination station
     * @param cargoType the cargo being transported
     * @return the created trade route
     */
    public TradeRoute createRoute(String playerId, Station fromStation, Station toStation, CargoType cargoType) {
        if (fromStation == null || toStation == null) {
            throw new IllegalArgumentException("Stations cannot be null");
        }
        if (fromStation.getId().equals(toStation.getId())) {
            throw new IllegalArgumentException("Route must connect different stations");
        }

        long id = routeCounter.incrementAndGet();
        String routeId = "ROUTE-" + playerId + "-" + id;

        TradeRoute route = new TradeRoute(routeId, playerId, fromStation, toStation, cargoType);
        routes.put(routeId, route);

        log.info("Trade route created: {} -> {} cargo: {}",
                fromStation.getName(), toStation.getName(), cargoType.getDisplayName());
        return route;
    }

    /**
     * Process a trade route for one turn, generating income.
     *
     * @param routeId the route ID
     * @return the income generated
     */
    public double processTurn(String routeId) {
        TradeRoute route = routes.get(routeId);
        if (route == null) {
            throw new IllegalArgumentException("Route not found: " + routeId);
        }

        double income = route.calculateIncome();
        route.addTurnIncome(income);
        route.setActive(true);

        log.debug("Route {} generated income: {}", routeId, income);
        return income;
    }

    /**
     * Process all routes for one turn.
     *
     * @return total income generated
     */
    public double processAllTurns() {
        double totalIncome = 0;
        for (TradeRoute route : routes.values()) {
            if (route.isActive()) {
                totalIncome += processTurn(route.getId());
            }
        }
        log.info("Processed {} active trade routes, total income: {}", routes.size(), totalIncome);
        return totalIncome;
    }

    /**
     * Get all routes for a player.
     *
     * @param playerId the player ID
     * @return list of trade routes
     */
    public List<TradeRoute> getPlayerRoutes(String playerId) {
        return routes.values().stream()
                .filter(r -> r.getPlayerId().equals(playerId))
                .toList();
    }

    /**
     * Get a route by ID.
     *
     * @param routeId the route ID
     * @return the route or null
     */
    public TradeRoute getRoute(String routeId) {
        return routes.get(routeId);
    }

    /**
     * Delete a route.
     *
     * @param routeId the route ID
     */
    public void deleteRoute(String routeId) {
        TradeRoute removed = routes.remove(routeId);
        if (removed != null) {
            log.info("Trade route deleted: {}", routeId);
        }
    }

    /**
     * Toggle route active status.
     *
     * @param routeId the route ID
     */
    public void toggleRoute(String routeId) {
        TradeRoute route = routes.get(routeId);
        if (route != null) {
            route.setActive(!route.isActive());
            log.info("Route {} is now {}", routeId, route.isActive() ? "active" : "inactive");
        }
    }

    /**
     * A trade route between two stations.
     */
    public static class TradeRoute {
        private final String id;
        private final String playerId;
        private final Station fromStation;
        private final Station toStation;
        private final CargoType cargoType;
        private boolean active;
        private double totalIncome;
        private int turnsOperated;

        public TradeRoute(String id, String playerId, Station fromStation,
                          Station toStation, CargoType cargoType) {
            this.id = id;
            this.playerId = playerId;
            this.fromStation = fromStation;
            this.toStation = toStation;
            this.cargoType = cargoType;
            this.active = true;
            this.totalIncome = 0;
            this.turnsOperated = 0;
        }

        public String getId() { return id; }
        public String getPlayerId() { return playerId; }
        public Station getFromStation() { return fromStation; }
        public Station getToStation() { return toStation; }
        public CargoType getCargoType() { return cargoType; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public double getTotalIncome() { return totalIncome; }
        public int getTurnsOperated() { return turnsOperated; }

        public void addTurnIncome(double income) {
            this.totalIncome += income;
            this.turnsOperated++;
        }

        /**
         * Calculate income for one turn based on cargo value and distance.
         */
        public double calculateIncome() {
            // Base income from cargo value
            double baseIncome = cargoType.getBaseValue() * 10;

            // Distance modifier (hex distance between stations)
            double distance = calculateDistance();
            double distanceModifier = 1.0 + (distance * 0.1);

            // Demand modifier (simulated)
            double demandModifier = 0.8 + (Math.random() * 0.4); // 0.8 to 1.2

            return baseIncome * distanceModifier * demandModifier;
        }

        private double calculateDistance() {
            // Simple hex distance using station positions
            return 5.0; // Default distance
        }

        @Override
        public String toString() {
            return String.format("TradeRoute{id=%s, player=%s, %s->%s, cargo=%s, income=%.2f}",
                    id, playerId, fromStation.getName(), toStation.getName(),
                    cargoType.getDisplayName(), totalIncome);
        }
    }
}

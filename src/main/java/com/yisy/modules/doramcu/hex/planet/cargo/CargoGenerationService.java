package com.yisy.modules.doramcu.hex.planet.cargo;

import com.yisy.modules.doramcu.hex.planet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Generates cargo at industries and settlements each turn.
 * <p>
 * The service:
 * <ul>
 *   <li>Industries produce cargo based on {@link IndustryType#getOutput()}</li>
 *   <li>Settlements generate passengers and mail</li>
 *   <li>Cargo appears at connected stations</li>
 *   <li>Perishable cargo decays if not picked up in time</li>
 *   <li>Industries consume cargo based on {@link IndustryType#getInput()}</li>
 * </ul>
 * </p>
 */
@Service
public class CargoGenerationService {

    private static final Logger log = LoggerFactory.getLogger(CargoGenerationService.class);

    /** Base passenger generation rate per settlement per turn */
    private static final int BASE_PASSENGER_RATE = 5;

    /** Base mail generation rate per settlement per turn */
    private static final int BASE_MAIL_RATE = 2;

    /** Maximum cargo waiting at a station before overflow */
    private static final int MAX_CARGO_WAITING = 100;

    /**
     * Processes cargo generation for all industries on a planet.
     *
     * @param industries list of industries
     * @param stations map of station IDs to stations
     * @return total cargo generated
     */
    public int generateIndustryCargo(List<Industry> industries, Map<String, Station> stations) {
        int totalGenerated = 0;

        for (Industry industry : industries) {
            if (!industry.isActive()) continue;

            Station station = findConnectedStation(industry, stations);
            if (station == null) continue;

            // Check if industry has input requirements
            Map<CargoType, Integer> input = industry.getType().getInput();
            boolean canProduce = true;

            if (!input.isEmpty()) {
                // Check if required input cargo is available at the station
                for (Map.Entry<CargoType, Integer> entry : input.entrySet()) {
                    int available = station.getCargoWaiting().getOrDefault(entry.getKey(), 0);
                    if (available < entry.getValue()) {
                        canProduce = false;
                        break;
                    }
                }
            }

            if (canProduce) {
                // Consume input cargo
                for (Map.Entry<CargoType, Integer> entry : input.entrySet()) {
                    station.removeCargo(entry.getKey(), entry.getValue());
                }

                // Produce output cargo
                Map<CargoType, Integer> output = industry.getType().getOutput();
                for (Map.Entry<CargoType, Integer> entry : output.entrySet()) {
                    CargoType cargoType = entry.getKey();
                    int amount = entry.getValue();

                    // Check station capacity
                    int currentWaiting = station.getCargoWaiting().getOrDefault(cargoType, 0);
                    int space = Math.max(0, MAX_CARGO_WAITING - currentWaiting);
                    int actualAmount = Math.min(amount, space);

                    if (actualAmount > 0) {
                        station.addCargo(cargoType, actualAmount);
                        totalGenerated += actualAmount;
                    }
                }
            }
        }

        return totalGenerated;
    }

    /**
     * Generates passengers and mail at settlements.
     *
     * @param settlements list of settlements
     * @param stations map of station IDs to stations
     * @return total passengers generated
     */
    public int generateSettlementDemand(List<Settlement> settlements, Map<String, Station> stations) {
        int totalPassengers = 0;

        for (Settlement settlement : settlements) {
            if (!settlement.isActive()) continue;

            Station station = findConnectedStation(settlement, stations);
            if (station == null) continue;

            // Generate passengers based on settlement size
            int population = settlement.getPopulation();
            int passengers = Math.max(1, population / 100);
            passengers = Math.min(passengers, BASE_PASSENGER_RATE * (1 + population / 500));

            station.addPassengers(passengers);
            totalPassengers += passengers;

            // Generate mail
            int mail = Math.max(1, population / 200);
            mail = Math.min(mail, BASE_MAIL_RATE * (1 + population / 500));
            station.addCargo(CargoType.MAIL, mail);
        }

        return totalPassengers;
    }

    /**
     * Processes cargo decay for perishable items.
     *
     * @param stations list of stations
     * @return total cargo lost to decay
     */
    public int processCargoDecay(List<Station> stations) {
        int totalDecayed = 0;

        for (Station station : stations) {
            Map<CargoType, Integer> cargoWaiting = station.getCargoWaiting();
            List<CargoType> toDecay = new ArrayList<>();

            for (Map.Entry<CargoType, Integer> entry : cargoWaiting.entrySet()) {
                CargoType cargoType = entry.getKey();
                if (!cargoType.isPerishable()) continue;

                int amount = entry.getValue();
                int decayAmount = Math.max(1, amount / cargoType.getDecayTurns());
                int remaining = Math.max(0, amount - decayAmount);

                if (remaining == 0) {
                    toDecay.add(cargoType);
                }

                totalDecayed += decayAmount;
            }

            // Update cargo amounts
            for (CargoType cargoType : cargoWaiting.keySet()) {
                if (cargoType.isPerishable()) {
                    int amount = cargoWaiting.get(cargoType);
                    int decayAmount = Math.max(1, amount / cargoType.getDecayTurns());
                    int remaining = Math.max(0, amount - decayAmount);
                    if (remaining == 0) {
                        station.removeCargo(cargoType, amount);
                    }
                }
            }
        }

        if (totalDecayed > 0) {
            log.debug("Cargo decay: {} units lost", totalDecayed);
        }

        return totalDecayed;
    }

    /**
     * Processes a full turn: generate cargo, passengers, and decay.
     *
     * @param industries list of industries
     * @param settlements list of settlements
     * @param stations list of stations
     * @return cargo generation summary
     */
    public CargoGenerationSummary processTurn(List<Industry> industries,
                                               List<Settlement> settlements,
                                               List<Station> stations) {
        Map<String, Station> stationMap = new HashMap<>();
        for (Station station : stations) {
            stationMap.put(station.getId(), station);
        }

        int cargoGenerated = generateIndustryCargo(industries, stationMap);
        int passengersGenerated = generateSettlementDemand(settlements, stationMap);
        int cargoDecayed = processCargoDecay(stations);

        return new CargoGenerationSummary(cargoGenerated, passengersGenerated, cargoDecayed);
    }

    /**
     * Finds the station connected to an industry.
     */
    private Station findConnectedStation(Industry industry, Map<String, Station> stations) {
        for (Station station : stations.values()) {
            if (station.getConnectedIndustryId() != null &&
                    station.getConnectedIndustryId().equals(industry.getId())) {
                return station;
            }
        }
        // Fallback: find nearest station
        return findNearestStation(industry.getLocation(), stations);
    }

    /**
     * Finds the station connected to a settlement.
     */
    private Station findConnectedStation(Settlement settlement, Map<String, Station> stations) {
        for (Station station : stations.values()) {
            if (station.getConnectedSettlementId() != null &&
                    station.getConnectedSettlementId().equals(settlement.getId())) {
                return station;
            }
        }
        // Fallback: find nearest station
        return findNearestStation(settlement.getCenterHex(), stations);
    }

    /**
     * Finds the nearest station to a hex position.
     */
    private Station findNearestStation(com.yisy.modules.doramcu.hex.Hex3D position,
                                        Map<String, Station> stations) {
        Station nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Station station : stations.values()) {
            int distance = position.distance(station.getPosition());
            if (distance < minDistance) {
                minDistance = distance;
                nearest = station;
            }
        }

        return nearest;
    }

    /**
     * Summary of cargo generation results.
     */
    public static class CargoGenerationSummary {
        private final int cargoGenerated;
        private final int passengersGenerated;
        private final int cargoDecayed;

        CargoGenerationSummary(int cargoGenerated, int passengersGenerated, int cargoDecayed) {
            this.cargoGenerated = cargoGenerated;
            this.passengersGenerated = passengersGenerated;
            this.cargoDecayed = cargoDecayed;
        }

        public int getCargoGenerated() {
            return cargoGenerated;
        }

        public int getPassengersGenerated() {
            return passengersGenerated;
        }

        public int getCargoDecayed() {
            return cargoDecayed;
        }

        @Override
        public String toString() {
            return String.format("CargoGenerationSummary{cargo=%d, passengers=%d, decayed=%d}",
                    cargoGenerated, passengersGenerated, cargoDecayed);
        }
    }
}

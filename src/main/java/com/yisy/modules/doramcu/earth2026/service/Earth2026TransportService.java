package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026TransportRoute;
import com.yisy.modules.doramcu.earth2026.model.YoungerOffice;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026TransportRouteRepository;
import com.yisy.modules.doramcu.earth2026.repository.YoungerOfficeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing Earth 2026 transportation between capital cities.
 * <p>
 * Players can book transport between any two connected capitals via Younger Industries
 * offices. Routes use Tech Level 5 vehicles (commercial airliners, high-speed rail)
 * and are bidirectional by default.
 * </p>
 */
@Service
public class Earth2026TransportService {

    private static final Logger log = LoggerFactory.getLogger(Earth2026TransportService.class);

    private final Earth2026TransportRouteRepository routeRepository;
    private final YoungerOfficeRepository officeRepository;

    public Earth2026TransportService(Earth2026TransportRouteRepository routeRepository,
                                      YoungerOfficeRepository officeRepository) {
        this.routeRepository = routeRepository;
        this.officeRepository = officeRepository;
    }

    /**
     * Get all active transport routes on a planet.
     *
     * @param planetId the planet ID
     * @return list of active transport routes
     */
    public List<Earth2026TransportRoute> getAllRoutes(Long planetId) {
        return routeRepository.findByPlanetIdAndActiveTrue(planetId);
    }

    /**
     * Get all routes departing from a specific city.
     *
     * @param city the departure city
     * @return list of routes departing from the city
     */
    public List<Earth2026TransportRoute> getRoutesFromCity(String city) {
        List<Earth2026TransportRoute> outgoing = routeRepository.findByFromCity(city);
        List<Earth2026TransportRoute> incoming = routeRepository.findByToCity(city).stream()
                .filter(Earth2026TransportRoute::isBidirectional)
                .collect(Collectors.toList());
        List<Earth2026TransportRoute> all = new ArrayList<>(outgoing);
        all.addAll(incoming);
        return all;
    }

    /**
     * Find a direct route between two cities.
     *
     * @param fromCity the departure city
     * @param toCity the destination city
     * @return the route if found, empty otherwise
     */
    public java.util.Optional<Earth2026TransportRoute> findRouteBetweenCities(String fromCity, String toCity) {
        // Check direct route
        List<Earth2026TransportRoute> direct = routeRepository.findByFromCityAndToCity(fromCity, toCity);
        if (!direct.isEmpty()) {
            return direct.stream().filter(Earth2026TransportRoute::isActive).findFirst();
        }
        // Check reverse direction if bidirectional
        List<Earth2026TransportRoute> reverse = routeRepository.findByFromCityAndToCity(toCity, fromCity);
        return reverse.stream()
                .filter(Earth2026TransportRoute::isBidirectional)
                .filter(Earth2026TransportRoute::isActive)
                .findFirst();
    }

    /**
     * Get the Younger Industries office at a city where players can book transport.
     *
     * @param city the city name
     * @param planetId the planet ID
     * @return the office if found, empty otherwise
     */
    public java.util.Optional<YoungerOffice> getOfficeAtCity(String city, Long planetId) {
        return officeRepository.findByCity(city).stream()
                .filter(YoungerOffice::isActive)
                .findFirst();
    }

    /**
     * Get all Younger Industries offices on a planet.
     *
     * @param planetId the planet ID
     * @return list of active offices
     */
    public List<YoungerOffice> getAllOffices(Long planetId) {
        return officeRepository.findByPlanetIdAndActiveTrue(planetId);
    }

    /**
     * Book a transport ticket between two cities.
     *
     * @param fromCity the departure city
     * @param toCity the destination city
     * @param passengerName the passenger's name
     * @return a TransportBooking if successful, empty otherwise
     */
    public java.util.Optional<TransportBooking> bookTicket(String fromCity, String toCity, String passengerName) {
        java.util.Optional<Earth2026TransportRoute> routeOpt = findRouteBetweenCities(fromCity, toCity);
        if (routeOpt.isEmpty()) {
            log.warn("No transport route found from {} to {}", fromCity, toCity);
            return java.util.Optional.empty();
        }

        Earth2026TransportRoute route = routeOpt.get();
        TransportBooking booking = new TransportBooking(
                route,
                passengerName,
                fromCity,
                toCity,
                route.getTicketCost(),
                route.getTravelTimeMinutes()
        );

        log.info("Transport booked: {} from {} to {} ({} credits, {} minutes)",
                passengerName, fromCity, toCity, route.getTicketCost(), route.getTravelTimeMinutes());
        return java.util.Optional.of(booking);
    }

    /**
     * Get a summary of the transport network on a planet.
     *
     * @param planetId the planet ID
     * @return a TransportNetworkSummary
     */
    public TransportNetworkSummary getNetworkSummary(Long planetId) {
        List<Earth2026TransportRoute> routes = routeRepository.findByPlanetIdAndActiveTrue(planetId);
        List<YoungerOffice> offices = officeRepository.findByPlanetIdAndActiveTrue(planetId);

        // Count unique cities connected
        java.util.Set<String> cities = new java.util.HashSet<>();
        for (Earth2026TransportRoute route : routes) {
            cities.add(route.getFromCity());
            cities.add(route.getToCity());
        }

        return new TransportNetworkSummary(
                routes.size(),
                offices.size(),
                cities.size(),
                routes.stream().map(Earth2026TransportRoute::getVehicleType).collect(Collectors.toSet())
        );
    }

    /**
     * Represents a booked transport ticket.
     */
    public static class TransportBooking {
        private final Earth2026TransportRoute route;
        private final String passengerName;
        private final String fromCity;
        private final String toCity;
        private final long cost;
        private final int travelTimeMinutes;

        public TransportBooking(Earth2026TransportRoute route, String passengerName,
                                String fromCity, String toCity, long cost, int travelTimeMinutes) {
            this.route = route;
            this.passengerName = passengerName;
            this.fromCity = fromCity;
            this.toCity = toCity;
            this.cost = cost;
            this.travelTimeMinutes = travelTimeMinutes;
        }

        public Earth2026TransportRoute getRoute() {
            return route;
        }

        public String getPassengerName() {
            return passengerName;
        }

        public String getFromCity() {
            return fromCity;
        }

        public String getToCity() {
            return toCity;
        }

        public long getCost() {
            return cost;
        }

        public int getTravelTimeMinutes() {
            return travelTimeMinutes;
        }

        public String getVehicleType() {
            return route.getVehicleType();
        }

        @Override
        public String toString() {
            return "TransportBooking{passenger='" + passengerName + "', from='" + fromCity +
                    "', to='" + toCity + "', vehicle='" + route.getVehicleType() +
                    "', cost=" + cost + ", time=" + travelTimeMinutes + "min}";
        }
    }

    /**
     * Summary of the transport network on a planet.
     */
    public static class TransportNetworkSummary {
        private final int routeCount;
        private final int officeCount;
        private final int cityCount;
        private final java.util.Set<String> vehicleTypes;

        public TransportNetworkSummary(int routeCount, int officeCount, int cityCount,
                                       java.util.Set<String> vehicleTypes) {
            this.routeCount = routeCount;
            this.officeCount = officeCount;
            this.cityCount = cityCount;
            this.vehicleTypes = vehicleTypes;
        }

        public int getRouteCount() {
            return routeCount;
        }

        public int getOfficeCount() {
            return officeCount;
        }

        public int getCityCount() {
            return cityCount;
        }

        public java.util.Set<String> getVehicleTypes() {
            return vehicleTypes;
        }

        @Override
        public String toString() {
            return "TransportNetworkSummary{routes=" + routeCount + ", offices=" + officeCount +
                    ", cities=" + cityCount + ", vehicleTypes=" + vehicleTypes + "}";
        }
    }
}

package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a transport route between two locations on Earth 2026.
 * <p>
 * Routes connect capital cities via Younger Industries offices, allowing players
 * to book transportation between any two connected locations. Each route specifies
 * the vehicle type, travel time, cost, and Tech Level requirements.
 * </p>
 */
@Entity
@Table(name = "earth2026_transport_routes", indexes = {
        @Index(name = "idx_transport_route_from", columnList = "from_location_id"),
        @Index(name = "idx_transport_route_to", columnList = "to_location_id"),
        @Index(name = "idx_transport_route_planet", columnList = "planet_id")
})
public class Earth2026TransportRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Route name (e.g., "Washington D.C. to London - Transatlantic Express").
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Origin location ID.
     */
    @Column(name = "from_location_id", nullable = false)
    private Long fromLocationId;

    /**
     * Destination location ID.
     */
    @Column(name = "to_location_id", nullable = false)
    private Long toLocationId;

    /**
     * Origin city name.
     */
    @Column(name = "from_city", nullable = false, length = 100)
    private String fromCity;

    /**
     * Destination city name.
     */
    @Column(name = "to_city", nullable = false, length = 100)
    private String toCity;

    /**
     * Origin country name.
     */
    @Column(name = "from_country", nullable = false, length = 100)
    private String fromCountry;

    /**
     * Destination country name.
     */
    @Column(name = "to_country", nullable = false, length = 100)
    private String toCountry;

    /**
     * The planet this route operates on.
     */
    @Column(name = "planet_id", nullable = false)
    private Long planetId;

    /**
     * Vehicle type used on this route (e.g., "Commercial Airliner", "High-Speed Rail", "Suborbital Shuttle").
     */
    @Column(name = "vehicle_type", nullable = false, length = 100)
    private String vehicleType;

    /**
     * Travel time in minutes.
     */
    @Column(name = "travel_time_minutes", nullable = false)
    private Integer travelTimeMinutes;

    /**
     * Ticket cost in credits.
     */
    @Column(name = "ticket_cost", nullable = false)
    private Long ticketCost;

    /**
     * Minimum Tech Level required to use this route.
     */
    @Column(name = "min_tech_level", nullable = false, length = 10)
    private String minTechLevel;

    /**
     * Route description.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Whether the route is currently operational.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Whether this is a bidirectional route (can travel both ways).
     */
    @Column(name = "bidirectional", nullable = false)
    private boolean bidirectional = true;

    public Earth2026TransportRoute() {
    }

    public Earth2026TransportRoute(String name, Long fromLocationId, Long toLocationId,
                                    String fromCity, String toCity, String fromCountry, String toCountry,
                                    Long planetId, String vehicleType, Integer travelTimeMinutes,
                                    Long ticketCost, String minTechLevel) {
        this.name = name;
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.fromCity = fromCity;
        this.toCity = toCity;
        this.fromCountry = fromCountry;
        this.toCountry = toCountry;
        this.planetId = planetId;
        this.vehicleType = vehicleType;
        this.travelTimeMinutes = travelTimeMinutes;
        this.ticketCost = ticketCost;
        this.minTechLevel = minTechLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getFromLocationId() {
        return fromLocationId;
    }

    public void setFromLocationId(Long fromLocationId) {
        this.fromLocationId = fromLocationId;
    }

    public Long getToLocationId() {
        return toLocationId;
    }

    public void setToLocationId(Long toLocationId) {
        this.toLocationId = toLocationId;
    }

    public String getFromCity() {
        return fromCity;
    }

    public void setFromCity(String fromCity) {
        this.fromCity = fromCity;
    }

    public String getToCity() {
        return toCity;
    }

    public void setToCity(String toCity) {
        this.toCity = toCity;
    }

    public String getFromCountry() {
        return fromCountry;
    }

    public void setFromCountry(String fromCountry) {
        this.fromCountry = fromCountry;
    }

    public String getToCountry() {
        return toCountry;
    }

    public void setToCountry(String toCountry) {
        this.toCountry = toCountry;
    }

    public Long getPlanetId() {
        return planetId;
    }

    public void setPlanetId(Long planetId) {
        this.planetId = planetId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Integer getTravelTimeMinutes() {
        return travelTimeMinutes;
    }

    public void setTravelTimeMinutes(Integer travelTimeMinutes) {
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public Long getTicketCost() {
        return ticketCost;
    }

    public void setTicketCost(Long ticketCost) {
        this.ticketCost = ticketCost;
    }

    public String getMinTechLevel() {
        return minTechLevel;
    }

    public void setMinTechLevel(String minTechLevel) {
        this.minTechLevel = minTechLevel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    @Override
    public String toString() {
        return "Earth2026TransportRoute{id=" + id + ", name='" + name + "', from='" + fromCity +
                "', to='" + toCity + "', vehicle='" + vehicleType + "'}";
    }
}

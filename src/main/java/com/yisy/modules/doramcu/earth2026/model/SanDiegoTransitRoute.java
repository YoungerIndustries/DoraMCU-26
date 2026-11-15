package com.yisy.modules.doramcu.earth2026.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a transit route in San Diego (bus or trolley).
 * <p>
 * Routes connect Earth2026 locations and provide navigation between them.
 * Each route has a series of stops with real-world coordinates.
 * </p>
 */
@Entity
@Table(name = "earth2026_transit_routes", indexes = {
        @Index(name = "idx_transit_route_name", columnList = "routeName"),
        @Index(name = "idx_transit_route_type", columnList = "routeType")
})
public class SanDiegoTransitRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * Route name (e.g., "Route 922 - Campo Bus", "Blue Line Trolley").
     */
    @Column(nullable = false, length = 100)
    private String routeName;

    /**
     * Route number or identifier (e.g., "922", "Blue").
     */
    @Column(nullable = false, length = 20)
    private String routeNumber;

    /**
     * Type of transit: "Bus" or "Trolley".
     */
    @Column(nullable = false, length = 20)
    private String routeType;

    /**
     * Description of the route.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Starting location name (e.g., "Younger Industries HQ").
     */
    @Column(nullable = false, length = 200)
    private String startLocation;

    /**
     * Ending location name (e.g., "San Diego Convention Center").
     */
    @Column(nullable = false, length = 200)
    private String endLocation;

    /**
     * Starting latitude.
     */
    @Column(nullable = false)
    private Double startLatitude;

    /**
     * Starting longitude.
     */
    @Column(nullable = false)
    private Double startLongitude;

    /**
     * Ending latitude.
     */
    @Column(nullable = false)
    private Double endLatitude;

    /**
     * Ending longitude.
     */
    @Column(nullable = false)
    private Double endLongitude;

    /**
     * Estimated travel time in minutes.
     */
    @Column(nullable = false)
    private Integer travelTimeMinutes;

    /**
     * Fare cost in cents.
     */
    @Column(nullable = false)
    private Integer fareCents;

    /**
     * Whether this route is currently active.
     */
    @Column(nullable = false)
    private boolean active = true;

    /**
     * Ordered list of stop coordinates as JSON array of [lat,lon] pairs.
     */
    @Column(columnDefinition = "TEXT")
    private String stopsJson;

    /**
     * List of stop names along the route.
     */
    @ElementCollection
    @OrderColumn
    @CollectionTable(name = "earth2026_transit_stops", joinColumns = @JoinColumn(name = "route_id"))
    private List<String> stopNames = new ArrayList<>();

    public SanDiegoTransitRoute() {
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public Integer getTravelTimeMinutes() {
        return travelTimeMinutes;
    }

    public void setTravelTimeMinutes(Integer travelTimeMinutes) {
        this.travelTimeMinutes = travelTimeMinutes;
    }

    public Integer getFareCents() {
        return fareCents;
    }

    public void setFareCents(Integer fareCents) {
        this.fareCents = fareCents;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getStopsJson() {
        return stopsJson;
    }

    public void setStopsJson(String stopsJson) {
        this.stopsJson = stopsJson;
    }

    public List<String> getStopNames() {
        return stopNames;
    }

    public void setStopNames(List<String> stopNames) {
        this.stopNames = stopNames;
    }

    @Override
    public String toString() {
        return "SanDiegoTransitRoute{" +
                "id=" + id +
                ", routeName='" + routeName + '\'' +
                ", routeType='" + routeType + '\'' +
                ", startLocation='" + startLocation + '\'' +
                ", endLocation='" + endLocation + '\'' +
                '}';
    }
}

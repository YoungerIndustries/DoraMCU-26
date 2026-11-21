package com.yisy.modules.doramcu.gamecore;
import com.yisy.modules.doramcu.model.players.Player;

import java.math.BigDecimal;
import java.util.List;

/**
 * Shared interface for real estate management across all game modules.
 * <p>
 * Used for buying, renting, and listing properties such as offices, apartments,
 * and other player-owned spaces. The system manages property prices dynamically
 * based on demand, supply, and location.
 * </p>
 * <p>
 * Earth2026 provides the primary implementation; other modules can adapt this
 * for their own property types (e.g., D&amp;D taverns, Traveller space stations).
 * </p>
 */
public interface RealEstateService {

    /**
     * Buy a property for a character.
     *
     * @param characterId the buyer's character ID
     * @param propertyId  the property ID to buy
     * @return the transaction result, or null if the purchase failed
     */
    PropertyTransaction buyProperty(Long characterId, Long propertyId);

    /**
     * Rent a property for a character.
     *
     * @param characterId the renter's character ID
     * @param propertyId  the property ID to rent
     * @return the transaction result, or null if the rental failed
     */
    PropertyTransaction rentProperty(Long characterId, Long propertyId);

    /**
     * List all available properties for sale or rent.
     *
     * @return list of property listings
     */
    List<PropertyListing> listProperties();

    /**
     * List available properties filtered by type.
     *
     * @param propertyType the type filter (e.g., "Office", "Apartment")
     * @return list of matching property listings
     */
    List<PropertyListing> listPropertiesByType(String propertyType);

    /**
     * Get current market data (average prices, occupancy rates, trends).
     *
     * @return the current market data snapshot
     */
    MarketData getMarketData();

    /**
     * Get the current price for a specific property.
     *
     * @param propertyId the property ID
     * @return the current price, or null if not found
     */
    BigDecimal getPropertyPrice(Long propertyId);

    /**
     * Get a property listing by ID.
     *
     * @param id the listing ID
     * @return the listing, or null if not found
     */
    PropertyListing getPropertyListing(Long id);

    /**
     * Get all properties owned by a character.
     *
     * @param characterId the character ID
     * @return list of owned properties
     */
    List<PropertyListing> getOwnedProperties(Long characterId);

    /**
     * Release a property (sell back to the system or vacate).
     *
     * @param characterId the character releasing the property
     * @param propertyId  the property ID to release
     * @return the transaction result, or null if the release failed
     */
    PropertyTransaction releaseProperty(Long characterId, Long propertyId);

    /**
     * A property listing for sale or rent.
     */
    class PropertyListing {
        private Long id;
        private String name;
        private String description;
        private String propertyType;
        private String buildingName;
        private Long buildingId;
        private Integer floorLevel;
        private BigDecimal buyPrice;
        private BigDecimal rentPrice;
        private String status;
        private Long ownerId;

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

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(String propertyType) {
            this.propertyType = propertyType;
        }

        public String getBuildingName() {
            return buildingName;
        }

        public void setBuildingName(String buildingName) {
            this.buildingName = buildingName;
        }

        public Long getBuildingId() {
            return buildingId;
        }

        public void setBuildingId(Long buildingId) {
            this.buildingId = buildingId;
        }

        public Integer getFloorLevel() {
            return floorLevel;
        }

        public void setFloorLevel(Integer floorLevel) {
            this.floorLevel = floorLevel;
        }

        public BigDecimal getBuyPrice() {
            return buyPrice;
        }

        public void setBuyPrice(BigDecimal buyPrice) {
            this.buyPrice = buyPrice;
        }

        public BigDecimal getRentPrice() {
            return rentPrice;
        }

        public void setRentPrice(BigDecimal rentPrice) {
            this.rentPrice = rentPrice;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(Long ownerId) {
            this.ownerId = ownerId;
        }
    }

    /**
     * Record of a property transaction (buy, rent, sell).
     */
    class PropertyTransaction {
        private Long id;
        private Long characterId;
        private Long propertyId;
        private String type;
        private BigDecimal amount;
        private String status;
        private String message;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getCharacterId() {
            return characterId;
        }

        public void setCharacterId(Long characterId) {
            this.characterId = characterId;
        }

        public Long getPropertyId() {
            return propertyId;
        }

        public void setPropertyId(Long propertyId) {
            this.propertyId = propertyId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Snapshot of current real estate market conditions.
     */
    class MarketData {
        private BigDecimal averageOfficePrice;
        private BigDecimal averageApartmentPrice;
        private double officeOccupancyRate;
        private double apartmentOccupancyRate;
        private int totalListings;
        private int availableOffices;
        private int availableApartments;
        private String marketTrend;

        public BigDecimal getAverageOfficePrice() {
            return averageOfficePrice;
        }

        public void setAverageOfficePrice(BigDecimal averageOfficePrice) {
            this.averageOfficePrice = averageOfficePrice;
        }

        public BigDecimal getAverageApartmentPrice() {
            return averageApartmentPrice;
        }

        public void setAverageApartmentPrice(BigDecimal averageApartmentPrice) {
            this.averageApartmentPrice = averageApartmentPrice;
        }

        public double getOfficeOccupancyRate() {
            return officeOccupancyRate;
        }

        public void setOfficeOccupancyRate(double officeOccupancyRate) {
            this.officeOccupancyRate = officeOccupancyRate;
        }

        public double getApartmentOccupancyRate() {
            return apartmentOccupancyRate;
        }

        public void setApartmentOccupancyRate(double apartmentOccupancyRate) {
            this.apartmentOccupancyRate = apartmentOccupancyRate;
        }

        public int getTotalListings() {
            return totalListings;
        }

        public void setTotalListings(int totalListings) {
            this.totalListings = totalListings;
        }

        public int getAvailableOffices() {
            return availableOffices;
        }

        public void setAvailableOffices(int availableOffices) {
            this.availableOffices = availableOffices;
        }

        public int getAvailableApartments() {
            return availableApartments;
        }

        public void setAvailableApartments(int availableApartments) {
            this.availableApartments = availableApartments;
        }

        public String getMarketTrend() {
            return marketTrend;
        }

        public void setMarketTrend(String marketTrend) {
            this.marketTrend = marketTrend;
        }
    }
}

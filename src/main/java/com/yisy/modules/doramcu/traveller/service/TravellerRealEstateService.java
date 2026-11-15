package com.yisy.modules.doramcu.traveller.service;

import com.yisy.modules.doramcu.gamecore.RealEstateService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Traveller adapter for the RealEstateService interface.
 * <p>
 * Provides stub implementations. Future enhancements could add Traveller-themed
 * properties like space stations, corporate offices, and port facilities.
 * </p>
 */
@Service
public class TravellerRealEstateService implements RealEstateService {

    @Override
    public PropertyTransaction buyProperty(Long characterId, Long propertyId) {
        return null;
    }

    @Override
    public PropertyTransaction rentProperty(Long characterId, Long propertyId) {
        return null;
    }

    @Override
    public List<PropertyListing> listProperties() {
        return List.of();
    }

    @Override
    public List<PropertyListing> listPropertiesByType(String propertyType) {
        return List.of();
    }

    @Override
    public MarketData getMarketData() {
        return null;
    }

    @Override
    public BigDecimal getPropertyPrice(Long propertyId) {
        return null;
    }

    @Override
    public PropertyListing getPropertyListing(Long id) {
        return null;
    }

    @Override
    public List<PropertyListing> getOwnedProperties(Long characterId) {
        return List.of();
    }

    @Override
    public PropertyTransaction releaseProperty(Long characterId, Long propertyId) {
        return null;
    }
}

package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.gamecore.RealEstateService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * D&amp;D 5E adapter for the RealEstateService interface.
 * <p>
 * Delegates to the Earth2026 implementation since D&amp;D does not have
 * its own real estate system. Future enhancements could add D&amp;D-themed
 * properties like taverns, guild halls, and wizard towers.
 * </p>
 */
@Service
public class Dnd5eRealEstateService implements RealEstateService {

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

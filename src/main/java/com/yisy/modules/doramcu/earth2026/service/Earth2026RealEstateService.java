package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.repository.*;
import com.yisy.modules.doramcu.gamecore.RealEstateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Earth2026 implementation of the RealEstateService interface.
 * <p>
 * Manages property listings, buying, renting, and dynamic pricing for offices
 * and apartments in the Younger Industries HQ and Apartment Buildings.
 * </p>
 */
@Service
public class Earth2026RealEstateService implements RealEstateService {

    private static final Logger log = LoggerFactory.getLogger(Earth2026RealEstateService.class);

    private final PropertyListingRepository listingRepository;
    private final PropertyTransactionRepository transactionRepository;
    private final OfficeUnitRepository officeUnitRepository;
    private final ApartmentUnitRepository apartmentUnitRepository;
    private final YoungerIndustriesHQRepository hqRepository;
    private final ApartmentBuildingRepository apartmentBuildingRepository;

    public Earth2026RealEstateService(PropertyListingRepository listingRepository,
                                       PropertyTransactionRepository transactionRepository,
                                       OfficeUnitRepository officeUnitRepository,
                                       ApartmentUnitRepository apartmentUnitRepository,
                                       YoungerIndustriesHQRepository hqRepository,
                                       ApartmentBuildingRepository apartmentBuildingRepository) {
        this.listingRepository = listingRepository;
        this.transactionRepository = transactionRepository;
        this.officeUnitRepository = officeUnitRepository;
        this.apartmentUnitRepository = apartmentUnitRepository;
        this.hqRepository = hqRepository;
        this.apartmentBuildingRepository = apartmentBuildingRepository;
    }

    @Override
    @Transactional
    public RealEstateService.PropertyTransaction buyProperty(Long characterId, Long propertyId) {
        com.yisy.modules.doramcu.earth2026.model.PropertyListing entity = listingRepository.findById(propertyId)
                .orElse(null);
        if (entity == null || !"AVAILABLE".equals(entity.getStatus())) {
            return toDtoTx(createFailedTxEntity(characterId, propertyId, "BUY", "Property not available"));
        }

        BigDecimal price = entity.getBuyPrice();
        if (price.compareTo(BigDecimal.ZERO) == 0) {
            return toDtoTx(completePurchaseEntity(characterId, entity, BigDecimal.ZERO, "Free office assignment"));
        }

        return toDtoTx(completePurchaseEntity(characterId, entity, price, "Property purchased"));
    }

    @Override
    @Transactional
    public RealEstateService.PropertyTransaction rentProperty(Long characterId, Long propertyId) {
        com.yisy.modules.doramcu.earth2026.model.PropertyListing entity = listingRepository.findById(propertyId)
                .orElse(null);
        if (entity == null || !"AVAILABLE".equals(entity.getStatus())) {
            return toDtoTx(createFailedTxEntity(characterId, propertyId, "RENT", "Property not available"));
        }

        BigDecimal rent = entity.getRentPrice();
        return toDtoTx(completeRentalEntity(characterId, entity, rent, "Property rented"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealEstateService.PropertyListing> listProperties() {
        return listingRepository.findByStatus("AVAILABLE").stream()
                .map(this::toDtoListing).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealEstateService.PropertyListing> listPropertiesByType(String propertyType) {
        return listingRepository.findByPropertyTypeAndStatus(propertyType, "AVAILABLE").stream()
                .map(this::toDtoListing).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RealEstateService.MarketData getMarketData() {
        RealEstateService.MarketData data = new RealEstateService.MarketData();

        List<com.yisy.modules.doramcu.earth2026.model.PropertyListing> allEntities = listingRepository.findAll();
        List<com.yisy.modules.doramcu.earth2026.model.PropertyListing> available = listingRepository.findByStatus("AVAILABLE");

        List<com.yisy.modules.doramcu.earth2026.model.PropertyListing> offices = allEntities.stream()
                .filter(l -> "Office".equals(l.getPropertyType())).collect(Collectors.toList());
        List<com.yisy.modules.doramcu.earth2026.model.PropertyListing> apartments = allEntities.stream()
                .filter(l -> "Apartment".equals(l.getPropertyType())).collect(Collectors.toList());

        data.setTotalListings(allEntities.size());
        data.setAvailableOffices((int) available.stream().filter(l -> "Office".equals(l.getPropertyType())).count());
        data.setAvailableApartments((int) available.stream().filter(l -> "Apartment".equals(l.getPropertyType())).count());

        if (!offices.isEmpty()) {
            BigDecimal avg = offices.stream()
                    .map(com.yisy.modules.doramcu.earth2026.model.PropertyListing::getBuyPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(offices.size()), BigDecimal.ROUND_HALF_UP);
            data.setAverageOfficePrice(avg);
        }
        if (!apartments.isEmpty()) {
            BigDecimal avg = apartments.stream()
                    .map(com.yisy.modules.doramcu.earth2026.model.PropertyListing::getRentPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(apartments.size()), BigDecimal.ROUND_HALF_UP);
            data.setAverageApartmentPrice(avg);
        }

        List<com.yisy.modules.doramcu.earth2026.model.YoungerIndustriesHQ> hqs = hqRepository.findByActiveTrue();
        List<com.yisy.modules.doramcu.earth2026.model.ApartmentBuilding> buildings = apartmentBuildingRepository.findByActiveTrue();

        if (!hqs.isEmpty()) {
            double totalRate = hqs.stream().mapToDouble(com.yisy.modules.doramcu.earth2026.model.YoungerIndustriesHQ::getOccupancyRate).average().orElse(0.0);
            data.setOfficeOccupancyRate(totalRate);
        }
        if (!buildings.isEmpty()) {
            double totalRate = buildings.stream().mapToDouble(com.yisy.modules.doramcu.earth2026.model.ApartmentBuilding::getOccupancyRate).average().orElse(0.0);
            data.setApartmentOccupancyRate(totalRate);
        }

        data.setMarketTrend(data.getOfficeOccupancyRate() > 0.8 ? "HIGH_DEMAND" :
                data.getOfficeOccupancyRate() > 0.5 ? "STABLE" : "LOW_DEMAND");

        return data;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPropertyPrice(Long propertyId) {
        com.yisy.modules.doramcu.earth2026.model.PropertyListing entity = listingRepository.findById(propertyId).orElse(null);
        return entity != null ? entity.getBuyPrice() : null;
    }

    @Override
    @Transactional(readOnly = true)
    public RealEstateService.PropertyListing getPropertyListing(Long id) {
        com.yisy.modules.doramcu.earth2026.model.PropertyListing entity = listingRepository.findById(id).orElse(null);
        return entity != null ? toDtoListing(entity) : null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RealEstateService.PropertyListing> getOwnedProperties(Long characterId) {
        return listingRepository.findByOwnerId(characterId).stream()
                .map(this::toDtoListing).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RealEstateService.PropertyTransaction releaseProperty(Long characterId, Long propertyId) {
        com.yisy.modules.doramcu.earth2026.model.PropertyListing entity = listingRepository.findById(propertyId).orElse(null);
        if (entity == null || !characterId.equals(entity.getOwnerId())) {
            return toDtoTx(createFailedTxEntity(characterId, propertyId, "RELEASE", "Property not owned by character"));
        }

        entity.setStatus("AVAILABLE");
        entity.setOwnerId(null);
        entity.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(entity);

        if ("Office".equals(entity.getPropertyType())) {
            officeUnitRepository.findById(entity.getUnitId()).ifPresent(unit -> {
                unit.setStatus("AVAILABLE");
                unit.setOwnerId(null);
                officeUnitRepository.save(unit);
            });
        } else if ("Apartment".equals(entity.getPropertyType())) {
            apartmentUnitRepository.findById(entity.getUnitId()).ifPresent(unit -> {
                unit.setStatus("AVAILABLE");
                unit.setTenantId(null);
                apartmentUnitRepository.save(unit);
            });
        }

        com.yisy.modules.doramcu.earth2026.model.PropertyTransaction txEntity = new com.yisy.modules.doramcu.earth2026.model.PropertyTransaction(characterId, propertyId, "RELEASE", BigDecimal.ZERO);
        txEntity.setMessage("Property released back to market");
        return toDtoTx(transactionRepository.save(txEntity));
    }

    private com.yisy.modules.doramcu.earth2026.model.PropertyTransaction completePurchaseEntity(Long characterId, com.yisy.modules.doramcu.earth2026.model.PropertyListing entity, BigDecimal price, String message) {
        entity.setStatus("SOLD");
        entity.setOwnerId(characterId);
        entity.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(entity);

        if ("Office".equals(entity.getPropertyType())) {
            officeUnitRepository.findById(entity.getUnitId()).ifPresent(unit -> {
                unit.setStatus("OCCUPIED");
                unit.setOwnerId(characterId);
                unit.setOwned(true);
                unit.setAssignedAt(LocalDateTime.now());
                officeUnitRepository.save(unit);
            });
        }

        com.yisy.modules.doramcu.earth2026.model.PropertyTransaction tx = new com.yisy.modules.doramcu.earth2026.model.PropertyTransaction(characterId, entity.getId(), "BUY", price);
        tx.setMessage(message);
        return transactionRepository.save(tx);
    }

    private com.yisy.modules.doramcu.earth2026.model.PropertyTransaction completeRentalEntity(Long characterId, com.yisy.modules.doramcu.earth2026.model.PropertyListing entity, BigDecimal rent, String message) {
        entity.setStatus("RENTED");
        entity.setOwnerId(characterId);
        entity.setUpdatedAt(LocalDateTime.now());
        listingRepository.save(entity);

        if ("Office".equals(entity.getPropertyType())) {
            officeUnitRepository.findById(entity.getUnitId()).ifPresent(unit -> {
                unit.setStatus("OCCUPIED");
                unit.setOwnerId(characterId);
                unit.setOwned(false);
                unit.setMonthlyRent(rent);
                unit.setAssignedAt(LocalDateTime.now());
                officeUnitRepository.save(unit);
            });
        } else if ("Apartment".equals(entity.getPropertyType())) {
            apartmentUnitRepository.findById(entity.getUnitId()).ifPresent(unit -> {
                unit.setStatus("OCCUPIED");
                unit.setTenantId(characterId);
                unit.setMonthlyRent(rent);
                unit.setAssignedAt(LocalDateTime.now());
                apartmentUnitRepository.save(unit);
            });
        }

        com.yisy.modules.doramcu.earth2026.model.PropertyTransaction tx = new com.yisy.modules.doramcu.earth2026.model.PropertyTransaction(characterId, entity.getId(), "RENT", rent);
        tx.setMessage(message);
        return transactionRepository.save(tx);
    }

    private com.yisy.modules.doramcu.earth2026.model.PropertyTransaction createFailedTxEntity(Long characterId, Long propertyId, String type, String message) {
        com.yisy.modules.doramcu.earth2026.model.PropertyTransaction tx = new com.yisy.modules.doramcu.earth2026.model.PropertyTransaction(characterId, propertyId, type, BigDecimal.ZERO);
        tx.setStatus("FAILED");
        tx.setMessage(message);
        return transactionRepository.save(tx);
    }

    // ==================== Entity to DTO Conversions ====================

    private RealEstateService.PropertyListing toDtoListing(com.yisy.modules.doramcu.earth2026.model.PropertyListing entity) {
        RealEstateService.PropertyListing dto = new RealEstateService.PropertyListing();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setPropertyType(entity.getPropertyType());
        dto.setBuildingName(entity.getBuildingName());
        dto.setBuildingId(entity.getBuildingId());
        dto.setFloorLevel(entity.getFloorLevel());
        dto.setBuyPrice(entity.getBuyPrice());
        dto.setRentPrice(entity.getRentPrice());
        dto.setStatus(entity.getStatus());
        dto.setOwnerId(entity.getOwnerId());
        return dto;
    }

    private RealEstateService.PropertyTransaction toDtoTx(com.yisy.modules.doramcu.earth2026.model.PropertyTransaction entity) {
        RealEstateService.PropertyTransaction dto = new RealEstateService.PropertyTransaction();
        dto.setId(entity.getId());
        dto.setCharacterId(entity.getCharacterId());
        dto.setPropertyId(entity.getPropertyId());
        dto.setType(entity.getType());
        dto.setAmount(entity.getAmount());
        dto.setStatus(entity.getStatus());
        dto.setMessage(entity.getMessage());
        return dto;
    }
}

package com.yisy.modules.doramcu.earth2026.service;
import com.yisy.modules.doramcu.model.players.Player;


import com.yisy.modules.doramcu.corporation.service.CorporationEconomyService;
import com.yisy.modules.doramcu.earth2026.model.ApartmentBuilding;
import com.yisy.modules.doramcu.earth2026.model.ApartmentUnit;
import com.yisy.modules.doramcu.earth2026.repository.ApartmentBuildingRepository;
import com.yisy.modules.doramcu.earth2026.repository.ApartmentUnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing apartment building creation by players.
 * <p>
 * Allows corporations to build their own apartment buildings for housing
 * employees and crew, funded from the corporation's credit balance.
 * </p>
 */
@Service
public class ApartmentBuildingService {

    private static final Logger log = LoggerFactory.getLogger(ApartmentBuildingService.class);

    /** Construction cost for a new apartment building (500,000 credits) */
    public static final long CONSTRUCTION_COST = 500_000L;

    private final ApartmentBuildingRepository apartmentBuildingRepository;
    private final ApartmentUnitRepository apartmentUnitRepository;
    private final CorporationEconomyService corporationEconomyService;

    public ApartmentBuildingService(ApartmentBuildingRepository apartmentBuildingRepository,
                                     ApartmentUnitRepository apartmentUnitRepository,
                                     CorporationEconomyService corporationEconomyService) {
        this.apartmentBuildingRepository = apartmentBuildingRepository;
        this.apartmentUnitRepository = apartmentUnitRepository;
        this.corporationEconomyService = corporationEconomyService;
    }

    /**
     * Builds a new apartment building for the given corporation.
     * <p>
     * Deducts the construction cost from the corporation's balance,
     * creates the building with 10 floors × 4 units = 40 apartments,
     * and sets the corporation as the owner.
     * </p>
     *
     * @param corporationId the corporation building the apartment
     * @param name          name of the building
     * @param locationId    location ID (defaults to San Diego = 1)
     * @return the created apartment building
     * @throws RuntimeException if the corporation cannot afford the construction
     */
    @Transactional
    public ApartmentBuilding buildApartmentBuilding(Long corporationId, String name, Long locationId) {
        // Deduct construction cost from corporation balance
        try {
            corporationEconomyService.debitCorporation(
                    corporationId, CONSTRUCTION_COST,
                    TransactionType.EXPENSE,
                    "Apartment building construction: " + name,
                    null);
        } catch (Exception e) {
            throw new RuntimeException("Corporation cannot afford apartment building construction: " + e.getMessage(), e);
        }

        // Create the building
        ApartmentBuilding building = new ApartmentBuilding();
        building.setName(name);
        building.setLocationId(locationId != null ? locationId : 1L);
        building.setApartmentFloors(10);
        building.setBasementLevels(10);
        building.setTotalApartments(0);
        building.setOccupiedApartments(0);
        building.setDescription("Player-owned apartment building: " + name +
                ". Built by corporation ID " + corporationId + ".");
        building.setActive(true);
        building.setCorporationId(corporationId);
        building.setCreatedAt(LocalDateTime.now());
        building.setUpdatedAt(LocalDateTime.now());
        building = apartmentBuildingRepository.save(building);

        // Create 40 apartment units: 4 per floor, 10 floors
        String[] unitSuffixes = {"Alpha", "Beta", "Gamma", "Delta"};
        int totalUnits = 0;
        for (int floor = 1; floor <= 10; floor++) {
            for (int unit = 0; unit < 4; unit++) {
                ApartmentUnit apt = new ApartmentUnit();
                apt.setBuildingId(building.getId());
                apt.setFloorLevel(floor);
                apt.setUnitNumber(unit + 1);
                apt.setName("Floor " + floor + " - " + unitSuffixes[unit]);
                apt.setDescription("Apartment " + (unit + 1) + " on floor " + floor + " of " + name + ".");
                apt.setSizeSqm(40 + (unit * 10));
                apt.setBedrooms(1 + unit / 2);
                apt.setStatus("AVAILABLE");
                int baseRent = 200 + (floor * 50) + (unit * 25);
                apt.setMonthlyRent(new BigDecimal(baseRent));
                apt.setCreatedAt(LocalDateTime.now());
                apartmentUnitRepository.save(apt);
                totalUnits++;
            }
        }

        building.setTotalApartments(totalUnits);
        building.setUpdatedAt(LocalDateTime.now());
        apartmentBuildingRepository.save(building);

        log.info("Corporation {} built apartment building '{}' with {} units for {} credits",
                corporationId, name, totalUnits, CONSTRUCTION_COST);
        return building;
    }

    /**
     * Lists all apartment buildings owned by a corporation.
     *
     * @param corporationId the corporation ID
     * @return list of apartment buildings owned by the corporation
     */
    public List<ApartmentBuilding> getPlayerBuildings(Long corporationId) {
        return apartmentBuildingRepository.findByActiveTrue().stream()
                .filter(b -> b.getCorporationId() != null && b.getCorporationId().equals(corporationId))
                .toList();
    }
}

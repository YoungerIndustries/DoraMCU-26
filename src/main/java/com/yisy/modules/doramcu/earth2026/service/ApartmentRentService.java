package com.yisy.modules.doramcu.earth2026.service;
import com.yisy.modules.doramcu.model.players.Player;


import com.yisy.modules.doramcu.corporation.model.CorporateFinanceTransaction.TransactionType;
import com.yisy.modules.doramcu.corporation.model.CorporateHiring;
import com.yisy.modules.doramcu.corporation.repository.CorporateHiringRepository;
import com.yisy.modules.doramcu.corporation.service.CorporationEconomyService;
import com.yisy.modules.doramcu.earth2026.model.ApartmentBuilding;
import com.yisy.modules.doramcu.earth2026.model.ApartmentUnit;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.repository.ApartmentBuildingRepository;
import com.yisy.modules.doramcu.earth2026.repository.ApartmentUnitRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for collecting monthly rent from apartment tenants.
 * <p>
 * Rent is deducted from the tenant's corporation balance and credited
 * to the building owner's corporation each game turn (7 days).
 * </p>
 */
@Service
public class ApartmentRentService {

    private static final Logger log = LoggerFactory.getLogger(ApartmentRentService.class);

    private final ApartmentUnitRepository apartmentUnitRepository;
    private final ApartmentBuildingRepository apartmentBuildingRepository;
    private final Earth2026CharacterRepository characterRepository;
    private final CorporationEconomyService corporationEconomyService;
    private final CorporateHiringRepository corporateHiringRepository;

    public ApartmentRentService(ApartmentUnitRepository apartmentUnitRepository,
                                 ApartmentBuildingRepository apartmentBuildingRepository,
                                 Earth2026CharacterRepository characterRepository,
                                 CorporationEconomyService corporationEconomyService,
                                 CorporateHiringRepository corporateHiringRepository) {
        this.apartmentUnitRepository = apartmentUnitRepository;
        this.apartmentBuildingRepository = apartmentBuildingRepository;
        this.characterRepository = characterRepository;
        this.corporationEconomyService = corporationEconomyService;
        this.corporateHiringRepository = corporateHiringRepository;
    }

    /**
     * Collects rent from all occupied apartments.
     * <p>
     * Iterates all occupied apartment units, deducts rent from the tenant's
     * corporation balance, and credits the building owner's corporation.
     * </p>
     *
     * @return summary map with total collected, skipped, and errors
     */
    @Transactional
    public Map<String, Object> collectRent() {
        Map<String, Object> summary = new HashMap<>();
        int totalCollected = 0;
        int skipped = 0;
        int errors = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<ApartmentUnit> occupiedUnits = apartmentUnitRepository.findByStatus("OCCUPIED");
        log.info("Collecting rent from {} occupied apartments", occupiedUnits.size());

        for (ApartmentUnit unit : occupiedUnits) {
            try {
                Optional<Earth2026Character> tenantOpt = characterRepository.findById(unit.getTenantId());
                if (tenantOpt.isEmpty()) {
                    log.warn("Tenant character {} not found for apartment {}", unit.getTenantId(), unit.getId());
                    skipped++;
                    continue;
                }

                Earth2026Character tenant = tenantOpt.get();

                // Find the building owner corporation
                Optional<ApartmentBuilding> buildingOpt = apartmentBuildingRepository.findById(unit.getBuildingId());
                if (buildingOpt.isEmpty()) {
                    log.warn("Building {} not found for apartment {}", unit.getBuildingId(), unit.getId());
                    errors++;
                    continue;
                }

                ApartmentBuilding building = buildingOpt.get();
                if (building.getCorporationId() == null) {
                    log.warn("No owner corporation for building {}", building.getId());
                    errors++;
                    continue;
                }

                BigDecimal rent = unit.getMonthlyRent();
                if (rent == null || rent.compareTo(BigDecimal.ZERO) <= 0) {
                    // Free apartment, skip
                    continue;
                }

                // Find the tenant's corporation via CorporateHiring
                Long tenantCorporationId = findTenantCorporationId(tenant);
                if (tenantCorporationId == null) {
                    log.warn("No corporation found for tenant character {}", tenant.getName());
                    skipped++;
                    continue;
                }

                // Skip if tenant and building owner are the same corporation
                if (tenantCorporationId.equals(building.getCorporationId())) {
                    continue;
                }

                // Deduct rent from tenant's corporation
                try {
                    corporationEconomyService.debitCorporation(
                            tenantCorporationId, rent.longValue(),
                            TransactionType.EXPENSE,
                            "Monthly rent for apartment " + unit.getName(),
                            null);
                } catch (Exception e) {
                    log.warn("Failed to deduct rent from tenant corporation {}: {}",
                            tenantCorporationId, e.getMessage());
                    skipped++;
                    continue;
                }

                // Credit rent to building owner's corporation
                try {
                    corporationEconomyService.creditCorporation(
                            building.getCorporationId(), rent.longValue(),
                            "Rent from " + unit.getName(),
                            null);
                } catch (Exception e) {
                    log.warn("Failed to credit rent to building owner corporation {}: {}",
                            building.getCorporationId(), e.getMessage());
                    // Reverse the tenant deduction
                    try {
                        corporationEconomyService.creditCorporation(
                                tenantCorporationId, rent.longValue(),
                                "Rent reversal - owner credit failed",
                                null);
                    } catch (Exception revertEx) {
                        log.error("Failed to reverse rent deduction for tenant: {}", revertEx.getMessage());
                    }
                    errors++;
                    continue;
                }

                totalCollected++;
                totalAmount = totalAmount.add(rent);
                log.info("Collected {} credits rent from {} for apartment {} in {}",
                        rent, tenant.getName(), unit.getName(), building.getName());

            } catch (Exception e) {
                log.error("Error collecting rent for apartment {}: {}", unit.getId(), e.getMessage());
                errors++;
            }
        }

        summary.put("totalCollected", totalCollected);
        summary.put("skipped", skipped);
        summary.put("errors", errors);
        summary.put("totalAmount", totalAmount);
        summary.put("occupiedUnits", occupiedUnits.size());

        log.info("Rent collection complete: {} collected, {} skipped, {} errors, {} total credits",
                totalCollected, skipped, errors, totalAmount);
        return summary;
    }

    /**
     * Gets the rent status for a specific character.
     *
     * @param characterId the character ID
     * @return map containing rent info, or error if character has no apartment
     */
    public Map<String, Object> getRentStatus(Long characterId) {
        Map<String, Object> result = new HashMap<>();

        Optional<Earth2026Character> characterOpt = characterRepository.findById(characterId);
        if (characterOpt.isEmpty()) {
            result.put("error", "Character not found");
            return result;
        }

        Earth2026Character character = characterOpt.get();
        if (character.getApartmentUnitId() == null) {
            result.put("hasApartment", false);
            result.put("message", "Character has no apartment assigned");
            return result;
        }

        Optional<ApartmentUnit> unitOpt = apartmentUnitRepository.findById(character.getApartmentUnitId());
        if (unitOpt.isEmpty()) {
            result.put("hasApartment", false);
            result.put("message", "Apartment unit not found");
            return result;
        }

        ApartmentUnit unit = unitOpt.get();
        result.put("hasApartment", true);
        result.put("apartmentId", unit.getId());
        result.put("apartmentName", unit.getName());
        result.put("floorLevel", unit.getFloorLevel());
        result.put("unitNumber", unit.getUnitNumber());
        result.put("monthlyRent", unit.getMonthlyRent());
        result.put("status", unit.getStatus());
        result.put("assignedAt", unit.getAssignedAt());

        // Get building info
        Optional<ApartmentBuilding> buildingOpt = apartmentBuildingRepository.findById(unit.getBuildingId());
        buildingOpt.ifPresent(building -> {
            result.put("buildingName", building.getName());
            result.put("buildingOwnerCorporationId", building.getCorporationId());
        });

        return result;
    }

    /**
     * Finds the corporation ID for a tenant character via their CorporateHiring record.
     *
     * @param character the tenant character
     * @return corporation ID or null if not found
     */
    private Long findTenantCorporationId(Earth2026Character character) {
        // Find all active player character hires and match by character ID
        List<CorporateHiring> allPlayerHires = corporateHiringRepository
                .findByCorporationIdAndPlayerCharacterIdIsNotNullAndStatus(null, CorporateHiring.Status.ACTIVE);
        for (CorporateHiring hire : allPlayerHires) {
            if (hire.getPlayerCharacterId() != null && hire.getPlayerCharacterId().equals(character.getId())) {
                return hire.getCorporationId();
            }
        }
        return null;
    }
}

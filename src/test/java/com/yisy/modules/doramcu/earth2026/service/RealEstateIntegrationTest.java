package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.earth2026.repository.*;
import com.yisy.modules.doramcu.gamecore.BuildingInstanceService;
import com.yisy.modules.doramcu.gamecore.RealEstateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the real estate system.
 * <p>
 * Tests the three core services (Earth2026RealEstateService, Earth2026BuildingInstanceService,
 * CharacterLandingService) using an H2 in-memory database. Each test is wrapped in a
 * transaction that rolls back automatically.
 * </p>
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RealEstateTestConfig.class)
@ActiveProfiles("test")
@Transactional
class RealEstateIntegrationTest {

    @Autowired
    private Earth2026RealEstateService realEstateService;

    @Autowired
    private Earth2026BuildingInstanceService buildingInstanceService;

    @Autowired
    private YoungerIndustriesHQRepository hqRepository;

    @Autowired
    private ApartmentBuildingRepository apartmentBuildingRepository;

    @Autowired
    private OfficeUnitRepository officeUnitRepository;

    @Autowired
    private ApartmentUnitRepository apartmentUnitRepository;

    @Autowired
    private PropertyListingRepository listingRepository;

    @Autowired
    private PropertyTransactionRepository transactionRepository;

    @Autowired
    private DungeonInstanceRepository dungeonInstanceRepository;

    @Autowired
    private DungeonInstanceTemplateRepository templateRepository;

    // Building Instance Service tests
    // =====================================================================

    @Test
    void initializeDefaultBuildings_createsHQAndApartmentBuilding() {
        // Act
        buildingInstanceService.initializeDefaultBuildings();

        // Assert HQ
        List<YoungerIndustriesHQ> hqs = hqRepository.findByActiveTrue();
        assertEquals(1, hqs.size());
        YoungerIndustriesHQ hq = hqs.get(0);
        assertEquals("Younger Industries HQ", hq.getName());
        assertEquals(3, hq.getOfficeFloors());
        assertEquals(3, hq.getBasementLevels());
        assertEquals(30, hq.getTotalOffices());

        // Assert Apartment Building
        List<ApartmentBuilding> buildings = apartmentBuildingRepository.findByActiveTrue();
        assertEquals(1, buildings.size());
        ApartmentBuilding building = buildings.get(0);
        assertEquals("Younger Industries Apartments", building.getName());
        assertEquals(10, building.getApartmentFloors());
        assertEquals(10, building.getBasementLevels());
        assertEquals(80, building.getTotalApartments());
    }

    @Test
    void initializeDefaultBuildings_createsOfficeUnits() {
        // Act
        buildingInstanceService.initializeDefaultBuildings();

        // Assert
        List<OfficeUnit> offices = officeUnitRepository.findAll();
        assertEquals(30, offices.size());

        // Verify distribution across floors
        for (int f = 1; f <= 3; f++) {
            final int floor = f;
            long count = offices.stream().filter(o -> o.getFloorLevel() == floor).count();
            assertEquals(10, count, "Floor " + floor + " should have 10 offices");
        }
    }

    @Test
    void initializeDefaultBuildings_createsApartmentUnits() {
        // Act
        buildingInstanceService.initializeDefaultBuildings();

        // Assert
        List<ApartmentUnit> apartments = apartmentUnitRepository.findAll();
        assertEquals(80, apartments.size());

        // Verify distribution across floors
        for (int f = 1; f <= 10; f++) {
            final int floor = f;
            long count = apartments.stream().filter(a -> a.getFloorLevel() == floor).count();
            assertEquals(8, count, "Floor " + floor + " should have 8 apartments");
        }
    }

    @Test
    void initializeDefaultBuildings_createsPropertyListings() {
        // Act
        buildingInstanceService.initializeDefaultBuildings();

        // Assert
        List<PropertyListing> listings = listingRepository.findAll();
        assertEquals(110, listings.size()); // 30 offices + 80 apartments

        long officeListings = listings.stream().filter(l -> "Office".equals(l.getPropertyType())).count();
        assertEquals(30, officeListings);

        long apartmentListings = listings.stream().filter(l -> "Apartment".equals(l.getPropertyType())).count();
        assertEquals(80, apartmentListings);
    }

    @Test
    void initializeDefaultBuildings_createsDungeonTemplates() {
        // Act
        buildingInstanceService.initializeDefaultBuildings();

        // Assert
        List<DungeonInstanceTemplate> templates = templateRepository.findByActiveTrue();
        assertEquals(8, templates.size());

        // Verify specific templates exist
        assertTrue(templates.stream().anyMatch(t -> "combat_crawl".equals(t.getTemplateId())));
        assertTrue(templates.stream().anyMatch(t -> "puzzle_dungeon".equals(t.getTemplateId())));
        assertTrue(templates.stream().anyMatch(t -> "boss_arena".equals(t.getTemplateId())));
        assertTrue(templates.stream().anyMatch(t -> "scifi_anomaly".equals(t.getTemplateId())));
    }

    @Test
    void initializeDefaultBuildings_isIdempotent() {
        // Act - call twice
        buildingInstanceService.initializeDefaultBuildings();
        buildingInstanceService.initializeDefaultBuildings();

        // Assert - should still have only 1 of each
        assertEquals(1, hqRepository.findByActiveTrue().size());
        assertEquals(1, apartmentBuildingRepository.findByActiveTrue().size());
        assertEquals(8, templateRepository.findByActiveTrue().size());
    }

    @Test
    void listBuildings_returnsAllBuildings() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();

        // Act
        List<BuildingInstanceService.BuildingInstance> buildings = buildingInstanceService.listBuildings();

        // Assert
        assertEquals(2, buildings.size());
        assertTrue(buildings.stream().anyMatch(b -> "HQ".equals(b.getBuildingType())));
        assertTrue(buildings.stream().anyMatch(b -> "Apartment".equals(b.getBuildingType())));
    }

    @Test
    void listBuildingsByType_filtersCorrectly() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();

        // Act
        List<BuildingInstanceService.BuildingInstance> hqs = buildingInstanceService.listBuildingsByType("HQ");
        List<BuildingInstanceService.BuildingInstance> apartments = buildingInstanceService.listBuildingsByType("Apartment");

        // Assert
        assertEquals(1, hqs.size());
        assertEquals(1, apartments.size());
    }

    @Test
    void getBuilding_returnsBuildingById() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);

        // Act
        BuildingInstanceService.BuildingInstance instance = buildingInstanceService.getBuilding(hq.getId());

        // Assert
        assertNotNull(instance);
        assertEquals(hq.getName(), instance.getName());
        assertEquals("HQ", instance.getBuildingType());
    }

    @Test
    void getBuilding_returnsNullForUnknownId() {
        // Act
        BuildingInstanceService.BuildingInstance instance = buildingInstanceService.getBuilding(9999L);

        // Assert
        assertNull(instance);
    }

    @Test
    void getFloors_returnsAllFloorsForHQ() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);

        // Act
        List<BuildingInstanceService.FloorInstance> floors = buildingInstanceService.getFloors(hq.getId());

        // Assert
        assertEquals(6, floors.size()); // 3 office floors + 3 basement levels

        // Verify upper floors
        for (int idx = 1; idx <= 3; idx++) {
            final int i = idx;
            BuildingInstanceService.FloorInstance floor = floors.stream()
                    .filter(f -> f.getFloorLevel() == i).findFirst().orElse(null);
            assertNotNull(floor);
            assertEquals("Office", floor.getFloorType());
        }

        // Verify basement levels
        for (int idx = 1; idx <= 3; idx++) {
            final int i = idx;
            BuildingInstanceService.FloorInstance floor = floors.stream()
                    .filter(f -> f.getFloorLevel() == -i).findFirst().orElse(null);
            assertNotNull(floor);
            assertEquals("Dungeon", floor.getFloorType());
        }
    }

    @Test
    void getFloors_returnsAllFloorsForApartmentBuilding() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        ApartmentBuilding building = apartmentBuildingRepository.findByActiveTrue().get(0);

        // Act
        List<BuildingInstanceService.FloorInstance> floors = buildingInstanceService.getFloors(building.getId());

        // Assert
        assertEquals(20, floors.size()); // 10 apartment floors + 10 basement levels
    }

    @Test
    void getFloor_returnsSpecificFloor() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);

        // Act
        BuildingInstanceService.FloorInstance floor = buildingInstanceService.getFloor(hq.getId(), 2);

        // Assert
        assertNotNull(floor);
        assertEquals(2, floor.getFloorLevel());
        assertEquals("Office", floor.getFloorType());
    }

    @Test
    void getFloor_returnsNullForUnknownFloor() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);

        // Act
        BuildingInstanceService.FloorInstance floor = buildingInstanceService.getFloor(hq.getId(), 99);

        // Assert
        assertNull(floor);
    }

    // =====================================================================
    // Dungeon Instance tests
    // =====================================================================

    @Test
    void listDungeonTemplates_returnsAllTemplates() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();

        // Act
        List<BuildingInstanceService.DungeonInstanceTemplate> templates = buildingInstanceService.listDungeonTemplates();

        // Assert
        assertEquals(8, templates.size());
    }

    @Test
    void getDungeonTemplate_returnsTemplateById() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();

        // Act
        BuildingInstanceService.DungeonInstanceTemplate template = buildingInstanceService.getDungeonTemplate("combat_crawl");

        // Assert
        assertNotNull(template);
        assertEquals("Combat Crawl", template.getName());
        assertEquals("Combat", template.getTheme());
    }

    @Test
    void getDungeonTemplate_returnsNullForUnknownId() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();

        // Act
        BuildingInstanceService.DungeonInstanceTemplate template = buildingInstanceService.getDungeonTemplate("nonexistent");

        // Assert
        assertNull(template);
    }

    @Test
    void configureBasement_createsDungeonInstance() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);

        // Act
        BuildingInstanceService.DungeonInstance instance = buildingInstanceService.configureBasement(
                hq.getId(), -1, "combat_crawl");

        // Assert
        assertNotNull(instance);
        assertEquals(hq.getId(), instance.getBuildingId());
        assertEquals(-1, instance.getBasementLevel());
        assertEquals("combat_crawl", instance.getTemplateId());
        assertEquals("Combat Crawl", instance.getTemplateName());
        assertTrue(instance.isActive());
        assertEquals("ACTIVE", instance.getStatus());
    }

    @Test
    void configureBasement_returnsNullForUnknownTemplate() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);

        // Act
        BuildingInstanceService.DungeonInstance instance = buildingInstanceService.configureBasement(
                hq.getId(), -1, "nonexistent_template");

        // Assert
        assertNull(instance);
    }

    @Test
    void configureBasement_returnsNullForUnknownBuilding() {
        // Act
        BuildingInstanceService.DungeonInstance instance = buildingInstanceService.configureBasement(
                9999L, -1, "combat_crawl");

        // Assert
        assertNull(instance);
    }

    @Test
    void configureBasement_preventsDuplicateBasementLevel() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);
        buildingInstanceService.configureBasement(hq.getId(), -1, "combat_crawl");

        // Act
        BuildingInstanceService.DungeonInstance instance = buildingInstanceService.configureBasement(
                hq.getId(), -1, "puzzle_dungeon");

        // Assert
        assertNull(instance);
    }

    @Test
    void getDungeonInstances_returnsActiveInstances() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);
        buildingInstanceService.configureBasement(hq.getId(), -1, "combat_crawl");
        buildingInstanceService.configureBasement(hq.getId(), -2, "puzzle_dungeon");

        // Act
        List<BuildingInstanceService.DungeonInstance> instances = buildingInstanceService.getDungeonInstances(hq.getId());

        // Assert
        assertEquals(2, instances.size());
    }

    @Test
    void getDungeonInstance_returnsInstanceById() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);
        BuildingInstanceService.DungeonInstance instance = buildingInstanceService.configureBasement(
                hq.getId(), -1, "combat_crawl");

        // Act
        BuildingInstanceService.DungeonInstance found = buildingInstanceService.getDungeonInstance(instance.getId());

        // Assert
        assertNotNull(found);
        assertEquals(instance.getId(), found.getId());
        assertEquals("combat_crawl", found.getTemplateId());
    }

    @Test
    void removeDungeonInstance_deactivatesInstance() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);
        BuildingInstanceService.DungeonInstance instance = buildingInstanceService.configureBasement(
                hq.getId(), -1, "combat_crawl");

        // Act
        boolean removed = buildingInstanceService.removeDungeonInstance(instance.getId());

        // Assert
        assertTrue(removed);
        BuildingInstanceService.DungeonInstance found = buildingInstanceService.getDungeonInstance(instance.getId());
        assertNotNull(found);
        assertFalse(found.isActive());
        assertEquals("ABANDONED", found.getStatus());
    }

    @Test
    void removeDungeonInstance_returnsFalseForUnknownId() {
        // Act
        boolean removed = buildingInstanceService.removeDungeonInstance(9999L);

        // Assert
        assertFalse(removed);
    }

    // =====================================================================
    // Real Estate Service tests
    // =====================================================================

    @Test
    void listProperties_returnsAvailableProperties() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();

        // Act
        List<RealEstateService.PropertyListing> properties = realEstateService.listProperties();

        // Assert
        assertEquals(110, properties.size());
        assertTrue(properties.stream().allMatch(p -> "AVAILABLE".equals(p.getStatus())));
    }

    @Test
    void listPropertiesByType_filtersByType() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();

        // Act
        List<RealEstateService.PropertyListing> offices = realEstateService.listPropertiesByType("Office");
        List<RealEstateService.PropertyListing> apartments = realEstateService.listPropertiesByType("Apartment");

        // Assert
        assertEquals(30, offices.size());
        assertEquals(80, apartments.size());
    }

    @Test
    void buyProperty_purchasesAvailableOffice() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        List<RealEstateService.PropertyListing> offices = realEstateService.listPropertiesByType("Office");
        RealEstateService.PropertyListing office = offices.get(0);

        // Act
        RealEstateService.PropertyTransaction tx = realEstateService.buyProperty(100L, office.getId());

        // Assert
        assertNotNull(tx);
        assertEquals("COMPLETED", tx.getStatus());
        assertEquals("BUY", tx.getType());
        assertEquals(100L, tx.getCharacterId());
        assertEquals(office.getId(), tx.getPropertyId());

        // Verify listing is now sold
        RealEstateService.PropertyListing updated = realEstateService.getPropertyListing(office.getId());
        assertEquals("SOLD", updated.getStatus());
        assertEquals(100L, updated.getOwnerId());
    }

    @Test
    void buyProperty_freeOfficeAssignment() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        // Create a free listing
        PropertyListing freeListing = new PropertyListing(
                "Free Office", "Office", "Test HQ", 1L, 1, 1L);
        freeListing.setBuyPrice(BigDecimal.ZERO);
        freeListing.setRentPrice(BigDecimal.ZERO);
        listingRepository.save(freeListing);

        // Act
        RealEstateService.PropertyTransaction tx = realEstateService.buyProperty(100L, freeListing.getId());

        // Assert
        assertNotNull(tx);
        assertEquals("COMPLETED", tx.getStatus());
        assertEquals(BigDecimal.ZERO, tx.getAmount());
        assertEquals("Free office assignment", tx.getMessage());
    }

    @Test
    void buyProperty_failsForUnavailableProperty() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        List<RealEstateService.PropertyListing> offices = realEstateService.listPropertiesByType("Office");
        RealEstateService.PropertyListing office = offices.get(0);

        // Buy first
        realEstateService.buyProperty(100L, office.getId());

        // Act - try to buy again
        RealEstateService.PropertyTransaction tx = realEstateService.buyProperty(101L, office.getId());

        // Assert
        assertNotNull(tx);
        assertEquals("FAILED", tx.getStatus());
        assertEquals("Property not available", tx.getMessage());
    }

    @Test
    void rentProperty_rentsAvailableApartment() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        List<RealEstateService.PropertyListing> apartments = realEstateService.listPropertiesByType("Apartment");
        RealEstateService.PropertyListing apartment = apartments.get(0);

        // Act
        RealEstateService.PropertyTransaction tx = realEstateService.rentProperty(200L, apartment.getId());

        // Assert
        assertNotNull(tx);
        assertEquals("COMPLETED", tx.getStatus());
        assertEquals("RENT", tx.getType());
        assertEquals(200L, tx.getCharacterId());

        // Verify listing is now rented
        RealEstateService.PropertyListing updated = realEstateService.getPropertyListing(apartment.getId());
        assertEquals("RENTED", updated.getStatus());
        assertEquals(200L, updated.getOwnerId());
    }

    @Test
    void rentProperty_failsForUnavailableProperty() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        List<RealEstateService.PropertyListing> apartments = realEstateService.listPropertiesByType("Apartment");
        RealEstateService.PropertyListing apartment = apartments.get(0);

        // Rent first
        realEstateService.rentProperty(200L, apartment.getId());

        // Act - try to rent again
        RealEstateService.PropertyTransaction tx = realEstateService.rentProperty(201L, apartment.getId());

        // Assert
        assertNotNull(tx);
        assertEquals("FAILED", tx.getStatus());
    }

    @Test
    void releaseProperty_releasesOwnedProperty() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        List<RealEstateService.PropertyListing> offices = realEstateService.listPropertiesByType("Office");
        RealEstateService.PropertyListing office = offices.get(0);
        realEstateService.buyProperty(100L, office.getId());

        // Act
        RealEstateService.PropertyTransaction tx = realEstateService.releaseProperty(100L, office.getId());

        // Assert
        assertNotNull(tx);
        assertEquals("COMPLETED", tx.getStatus());
        assertEquals("RELEASE", tx.getType());

        // Verify listing is available again
        RealEstateService.PropertyListing updated = realEstateService.getPropertyListing(office.getId());
        assertEquals("AVAILABLE", updated.getStatus());
        assertNull(updated.getOwnerId());
    }

    @Test
    void releaseProperty_failsForUnownedProperty() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        List<RealEstateService.PropertyListing> offices = realEstateService.listPropertiesByType("Office");
        RealEstateService.PropertyListing office = offices.get(0);

        // Act
        RealEstateService.PropertyTransaction tx = realEstateService.releaseProperty(999L, office.getId());

        // Assert
        assertNotNull(tx);
        assertEquals("FAILED", tx.getStatus());
    }

    @Test
    void getOwnedProperties_returnsPropertiesForCharacter() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        List<RealEstateService.PropertyListing> offices = realEstateService.listPropertiesByType("Office");
        realEstateService.buyProperty(100L, offices.get(0).getId());
        realEstateService.buyProperty(100L, offices.get(1).getId());

        // Act
        List<RealEstateService.PropertyListing> owned = realEstateService.getOwnedProperties(100L);

        // Assert
        assertEquals(2, owned.size());
    }

    @Test
    void getPropertyPrice_returnsPrice() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        List<RealEstateService.PropertyListing> offices = realEstateService.listPropertiesByType("Office");
        RealEstateService.PropertyListing office = offices.get(0);

        // Act
        BigDecimal price = realEstateService.getPropertyPrice(office.getId());

        // Assert
        assertNotNull(price);
        assertTrue(price.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void getPropertyPrice_returnsNullForUnknownId() {
        // Act
        BigDecimal price = realEstateService.getPropertyPrice(9999L);

        // Assert
        assertNull(price);
    }

    @Test
    void getMarketData_returnsMarketSnapshot() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();

        // Act
        RealEstateService.MarketData data = realEstateService.getMarketData();

        // Assert
        assertEquals(110, data.getTotalListings());
        assertEquals(30, data.getAvailableOffices());
        assertEquals(80, data.getAvailableApartments());
        assertNotNull(data.getAverageOfficePrice());
        assertTrue(data.getAverageOfficePrice().compareTo(BigDecimal.ZERO) > 0);
        assertNotNull(data.getMarketTrend());
    }

    @Test
    void getMarketData_reflectsOccupancy() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        List<RealEstateService.PropertyListing> offices = realEstateService.listPropertiesByType("Office");

        // Buy half the offices
        for (int i = 0; i < 15; i++) {
            realEstateService.buyProperty(100L + i, offices.get(i).getId());
        }

        // Act
        RealEstateService.MarketData data = realEstateService.getMarketData();

        // Assert
        assertEquals(15, data.getAvailableOffices());
        assertEquals(80, data.getAvailableApartments());
    }

    // Full flow integration tests
    // =====================================================================

    @Test
    void fullFlow_dungeonInstanceCreatedAndRemoved() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();
        YoungerIndustriesHQ hq = hqRepository.findByActiveTrue().get(0);

        // Act - create dungeon instance
        BuildingInstanceService.DungeonInstance instance = buildingInstanceService.configureBasement(
                hq.getId(), -1, "combat_crawl");
        assertNotNull(instance);

        // Act - verify it appears in the list
        List<BuildingInstanceService.DungeonInstance> instances = buildingInstanceService.getDungeonInstances(hq.getId());
        assertEquals(1, instances.size());

        // Act - remove it
        boolean removed = buildingInstanceService.removeDungeonInstance(instance.getId());
        assertTrue(removed);

        // Assert - no longer in active list
        instances = buildingInstanceService.getDungeonInstances(hq.getId());
        assertEquals(0, instances.size());
    }

    @Test
    void fullFlow_marketDataReflectsTransactions() {
        // Arrange
        buildingInstanceService.initializeDefaultBuildings();

        // Act - initial market data
        RealEstateService.MarketData initialData = realEstateService.getMarketData();
        assertEquals(30, initialData.getAvailableOffices());

        // Act - buy some offices
        List<RealEstateService.PropertyListing> offices = realEstateService.listPropertiesByType("Office");
        for (int i = 0; i < 10; i++) {
            realEstateService.buyProperty(100L + i, offices.get(i).getId());
        }

        // Act - updated market data
        RealEstateService.MarketData updatedData = realEstateService.getMarketData();

        // Assert
        assertEquals(20, updatedData.getAvailableOffices());
        assertEquals(80, updatedData.getAvailableApartments());
    }
}

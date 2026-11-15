package com.yisy.modules.doramcu.earth2026.service;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.*;
import com.yisy.modules.doramcu.earth2026.repository.*;
import com.yisy.modules.doramcu.gamecore.BuildingInstanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Earth2026 implementation of the BuildingInstanceService interface.
 * <p>
 * Manages building instances (Younger Industries HQ, Apartment Buildings),
 * their floors, and basement dungeon instance configuration.
 * </p>
 */
@Service
public class Earth2026BuildingInstanceService implements BuildingInstanceService {

    private static final Logger log = LoggerFactory.getLogger(Earth2026BuildingInstanceService.class);
    private static final Random random = new Random();

    private final YoungerIndustriesHQRepository hqRepository;
    private final ApartmentBuildingRepository apartmentBuildingRepository;
    private final OfficeUnitRepository officeUnitRepository;
    private final ApartmentUnitRepository apartmentUnitRepository;
    private final PropertyListingRepository listingRepository;
    private final DungeonInstanceRepository dungeonInstanceRepository;
    private final DungeonInstanceTemplateRepository templateRepository;

    public Earth2026BuildingInstanceService(YoungerIndustriesHQRepository hqRepository,
                                             ApartmentBuildingRepository apartmentBuildingRepository,
                                             OfficeUnitRepository officeUnitRepository,
                                             ApartmentUnitRepository apartmentUnitRepository,
                                             PropertyListingRepository listingRepository,
                                             DungeonInstanceRepository dungeonInstanceRepository,
                                             DungeonInstanceTemplateRepository templateRepository) {
        this.hqRepository = hqRepository;
        this.apartmentBuildingRepository = apartmentBuildingRepository;
        this.officeUnitRepository = officeUnitRepository;
        this.apartmentUnitRepository = apartmentUnitRepository;
        this.listingRepository = listingRepository;
        this.dungeonInstanceRepository = dungeonInstanceRepository;
        this.templateRepository = templateRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeDefaultBuildings() {
        if (hqRepository.findByActiveTrue().isEmpty()) {
            createDefaultHQ();
        }
        if (apartmentBuildingRepository.findByActiveTrue().isEmpty()) {
            createDefaultApartmentBuilding();
        }
        if (templateRepository.findByActiveTrue().isEmpty()) {
            createDefaultTemplates();
        }
    }

    @Override
    @Transactional
    public BuildingInstance createBuilding(String buildingType, String name, int upperFloors, int basementLevels) {
        if ("HQ".equalsIgnoreCase(buildingType)) {
            YoungerIndustriesHQ hq = new YoungerIndustriesHQ(name, 1L);
            hq.setOfficeFloors(upperFloors);
            hq.setBasementLevels(basementLevels);
            hq.setDescription("Younger Industries HQ: " + name);
            hq = hqRepository.save(hq);

            // Create office units
            int officesPerFloor = 10;
            int totalOffices = upperFloors * officesPerFloor;
            hq.setTotalOffices(totalOffices);
            hqRepository.save(hq);

            for (int floor = 1; floor <= upperFloors; floor++) {
                for (int unit = 1; unit <= officesPerFloor; unit++) {
                    OfficeUnit office = new OfficeUnit(hq.getId(), floor, unit,
                            "Office " + floor + "-" + unit);
                    office.setSizeSqm(20 + (floor * 10));
                    office.setPurchasePrice(calculateOfficePrice(floor));
                    office.setMonthlyRent(calculateOfficeRent(floor));
                    officeUnitRepository.save(office);

                    // Create listing
                    PropertyListing listing = new PropertyListing(
                            "Office " + floor + "-" + unit, "Office", name, hq.getId(), floor, office.getId());
                    listing.setDescription("Corporate office on floor " + floor + " of " + name);
                    listing.setBuyPrice(office.getPurchasePrice());
                    listing.setRentPrice(office.getMonthlyRent());
                    listingRepository.save(listing);
                }
            }

            return toBuildingInstance(hq, null);
        } else if ("Apartment".equalsIgnoreCase(buildingType)) {
            ApartmentBuilding building = new ApartmentBuilding(name, 1L);
            building.setApartmentFloors(upperFloors);
            building.setBasementLevels(basementLevels);
            building.setDescription("Apartment Building: " + name);
            building = apartmentBuildingRepository.save(building);

            // Create apartment units
            int apartmentsPerFloor = 8;
            int totalApartments = upperFloors * apartmentsPerFloor;
            building.setTotalApartments(totalApartments);
            apartmentBuildingRepository.save(building);

            for (int floor = 1; floor <= upperFloors; floor++) {
                for (int unit = 1; unit <= apartmentsPerFloor; unit++) {
                    ApartmentUnit apartment = new ApartmentUnit(building.getId(), floor, unit,
                            "Apartment " + floor + "-" + unit);
                    apartment.setSizeSqm(30 + (floor * 5));
                    apartment.setBedrooms(1 + (floor / 3));
                    apartment.setMonthlyRent(calculateApartmentRent(floor));
                    apartmentUnitRepository.save(apartment);

                    // Create listing
                    PropertyListing listing = new PropertyListing(
                            "Apartment " + floor + "-" + unit, "Apartment", name, building.getId(), floor, apartment.getId());
                    listing.setDescription("Apartment on floor " + floor + " of " + name);
                    listing.setBuyPrice(BigDecimal.ZERO);
                    listing.setRentPrice(apartment.getMonthlyRent());
                    listingRepository.save(listing);
                }
            }

            return toBuildingInstance(null, building);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingInstance getBuilding(Long id) {
        YoungerIndustriesHQ hq = hqRepository.findById(id).orElse(null);
        if (hq != null) return toBuildingInstance(hq, null);

        ApartmentBuilding building = apartmentBuildingRepository.findById(id).orElse(null);
        if (building != null) return toBuildingInstance(null, building);

        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingInstance> listBuildings() {
        List<BuildingInstance> result = new ArrayList<>();
        result.addAll(hqRepository.findByActiveTrue().stream()
                .map(hq -> toBuildingInstance(hq, null)).collect(Collectors.toList()));
        result.addAll(apartmentBuildingRepository.findByActiveTrue().stream()
                .map(b -> toBuildingInstance(null, b)).collect(Collectors.toList()));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingInstance> listBuildingsByType(String buildingType) {
        return listBuildings().stream()
                .filter(b -> buildingType.equalsIgnoreCase(b.getBuildingType()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FloorInstance> getFloors(Long buildingId) {
        List<FloorInstance> floors = new ArrayList<>();

        // Check HQ
        YoungerIndustriesHQ hq = hqRepository.findById(buildingId).orElse(null);
        if (hq != null) {
            for (int floor = 1; floor <= hq.getOfficeFloors(); floor++) {
                final int currentFloor = floor;
                FloorInstance fi = new FloorInstance();
                fi.setBuildingId(buildingId);
                fi.setFloorLevel(currentFloor);
                fi.setFloorType("Office");
                fi.setDescription("Office floor " + currentFloor);
                fi.setCapacity(10);
                int occupied = (int) officeUnitRepository.findByHqIdAndStatus(buildingId, "OCCUPIED").stream()
                        .filter(u -> u.getFloorLevel() == currentFloor).count();
                fi.setCurrentOccupancy(occupied);
                fi.setActive(true);
                floors.add(fi);
            }
            for (int level = 1; level <= hq.getBasementLevels(); level++) {
                FloorInstance fi = new FloorInstance();
                fi.setBuildingId(buildingId);
                fi.setFloorLevel(-level);
                fi.setFloorType("Dungeon");
                fi.setDescription("Basement level B" + level);
                fi.setCapacity(1);
                fi.setCurrentOccupancy(dungeonInstanceRepository.findByHqIdAndBasementLevel(buildingId, -level).isPresent() ? 1 : 0);
                fi.setActive(true);
                floors.add(fi);
            }
            return floors;
        }

        // Check Apartment Building
        ApartmentBuilding building = apartmentBuildingRepository.findById(buildingId).orElse(null);
        if (building != null) {
            for (int floor = 1; floor <= building.getApartmentFloors(); floor++) {
                final int currentFloor = floor;
                FloorInstance fi = new FloorInstance();
                fi.setBuildingId(buildingId);
                fi.setFloorLevel(currentFloor);
                fi.setFloorType("Apartment");
                fi.setDescription("Apartment floor " + currentFloor);
                fi.setCapacity(8);
                int occupied = (int) apartmentUnitRepository.findByBuildingIdAndStatus(buildingId, "OCCUPIED").stream()
                        .filter(u -> u.getFloorLevel() == currentFloor).count();
                fi.setCurrentOccupancy(occupied);
                fi.setActive(true);
                floors.add(fi);
            }
            for (int level = 1; level <= building.getBasementLevels(); level++) {
                FloorInstance fi = new FloorInstance();
                fi.setBuildingId(buildingId);
                fi.setFloorLevel(-level);
                fi.setFloorType("Dungeon");
                fi.setDescription("Basement level B" + level);
                fi.setCapacity(1);
                fi.setCurrentOccupancy(dungeonInstanceRepository.findByBuildingIdAndBasementLevel(buildingId, -level).isPresent() ? 1 : 0);
                fi.setActive(true);
                floors.add(fi);
            }
            return floors;
        }

        return floors;
    }

    @Override
    @Transactional(readOnly = true)
    public FloorInstance getFloor(Long buildingId, int floorLevel) {
        return getFloors(buildingId).stream()
                .filter(f -> f.getFloorLevel() == floorLevel)
                .findFirst().orElse(null);
    }

    @Override
    @Transactional
    public BuildingInstanceService.DungeonInstance configureBasement(Long buildingId, int basementLevel, String templateId) {
        com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate entity = templateRepository.findByTemplateId(templateId).orElse(null);
        if (entity == null) {
            log.warn("Dungeon template not found: {}", templateId);
            return null;
        }

        YoungerIndustriesHQ hq = hqRepository.findById(buildingId).orElse(null);
        ApartmentBuilding building = apartmentBuildingRepository.findById(buildingId).orElse(null);

        if (hq != null) {
            if (dungeonInstanceRepository.findByHqIdAndBasementLevel(buildingId, basementLevel).isPresent()) {
                log.warn("Basement level {} already configured in HQ {}", basementLevel, buildingId);
                return null;
            }
        } else if (building != null) {
            if (dungeonInstanceRepository.findByBuildingIdAndBasementLevel(buildingId, basementLevel).isPresent()) {
                log.warn("Basement level {} already configured in building {}", basementLevel, buildingId);
                return null;
            }
        } else {
            log.warn("Building not found: {}", buildingId);
            return null;
        }

        int difficulty = entity.getMinDifficulty() + random.nextInt(
                entity.getMaxDifficulty() - entity.getMinDifficulty() + 1);
        int roomCount = entity.getMinRoomCount() + random.nextInt(
                entity.getMaxRoomCount() - entity.getMinRoomCount() + 1);

        com.yisy.modules.doramcu.earth2026.model.DungeonInstance instance = new com.yisy.modules.doramcu.earth2026.model.DungeonInstance(
                hq != null ? hq.getId() : null,
                building != null ? building.getId() : null,
                basementLevel, templateId, entity.getName(), entity.getTheme(), difficulty);
        instance.setRoomCount(roomCount);
        instance.setUpdatedAt(LocalDateTime.now());

        return toDtoDungeonInstance(dungeonInstanceRepository.save(instance));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingInstanceService.DungeonInstance> getDungeonInstances(Long buildingId) {
        List<BuildingInstanceService.DungeonInstance> result = new ArrayList<>();
        java.util.Set<Long> addedIds = new java.util.HashSet<>();
        dungeonInstanceRepository.findByHqIdAndActiveTrue(buildingId).stream()
                .map(this::toDtoDungeonInstance)
                .filter(di -> addedIds.add(di.getId()))
                .forEach(result::add);
        dungeonInstanceRepository.findByBuildingIdAndActiveTrue(buildingId).stream()
                .map(this::toDtoDungeonInstance)
                .filter(di -> addedIds.add(di.getId()))
                .forEach(result::add);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingInstanceService.DungeonInstance getDungeonInstance(Long id) {
        com.yisy.modules.doramcu.earth2026.model.DungeonInstance entity = dungeonInstanceRepository.findById(id).orElse(null);
        return entity != null ? toDtoDungeonInstance(entity) : null;
    }

    @Override
    @Transactional
    public boolean removeDungeonInstance(Long dungeonInstanceId) {
        com.yisy.modules.doramcu.earth2026.model.DungeonInstance entity = dungeonInstanceRepository.findById(dungeonInstanceId).orElse(null);
        if (entity == null) return false;

        entity.setActive(false);
        entity.setStatus("ABANDONED");
        entity.setUpdatedAt(LocalDateTime.now());
        dungeonInstanceRepository.save(entity);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BuildingInstanceService.DungeonInstanceTemplate> listDungeonTemplates() {
        return templateRepository.findByActiveTrue().stream()
                .map(this::toDtoTemplate).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BuildingInstanceService.DungeonInstanceTemplate getDungeonTemplate(String templateId) {
        com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate entity = templateRepository.findByTemplateId(templateId).orElse(null);
        return entity != null ? toDtoTemplate(entity) : null;
    }

    private void createDefaultHQ() {
        YoungerIndustriesHQ hq = new YoungerIndustriesHQ("Younger Industries HQ", 1L);
        hq.setOfficeFloors(3);
        hq.setBasementLevels(3);
        hq.setDescription("The main Younger Industries headquarters building. 3 floors of corporate offices and 3 basement levels for dungeon instances.");
        hq = hqRepository.save(hq);

        int officesPerFloor = 10;
        int totalOffices = 3 * officesPerFloor;
        hq.setTotalOffices(totalOffices);
        hqRepository.save(hq);

        for (int floor = 1; floor <= 3; floor++) {
            for (int unit = 1; unit <= officesPerFloor; unit++) {
                OfficeUnit office = new OfficeUnit(hq.getId(), floor, unit,
                        "Office " + floor + "-" + unit);
                office.setSizeSqm(20 + (floor * 10));
                office.setPurchasePrice(calculateOfficePrice(floor));
                office.setMonthlyRent(calculateOfficeRent(floor));
                officeUnitRepository.save(office);

                PropertyListing listing = new PropertyListing(
                        "Office " + floor + "-" + unit, "Office", "Younger Industries HQ", hq.getId(), floor, office.getId());
                listing.setDescription("Corporate office on floor " + floor + " of Younger Industries HQ");
                listing.setBuyPrice(office.getPurchasePrice());
                listing.setRentPrice(office.getMonthlyRent());
                listingRepository.save(listing);
            }
        }

        log.info("Created default Younger Industries HQ with {} offices", totalOffices);
    }

    private void createDefaultApartmentBuilding() {
        ApartmentBuilding building = new ApartmentBuilding("Younger Industries Apartments", 1L);
        building.setApartmentFloors(10);
        building.setBasementLevels(10);
        building.setDescription("Apartment building with 10 floors of residential units and 10 basement dungeon levels.");
        building = apartmentBuildingRepository.save(building);

        int apartmentsPerFloor = 8;
        int totalApartments = 10 * apartmentsPerFloor;
        building.setTotalApartments(totalApartments);
        apartmentBuildingRepository.save(building);

        for (int floor = 1; floor <= 10; floor++) {
            for (int unit = 1; unit <= apartmentsPerFloor; unit++) {
                ApartmentUnit apartment = new ApartmentUnit(building.getId(), floor, unit,
                        "Apartment " + floor + "-" + unit);
                apartment.setSizeSqm(30 + (floor * 5));
                apartment.setBedrooms(1 + (floor / 3));
                apartment.setMonthlyRent(calculateApartmentRent(floor));
                apartmentUnitRepository.save(apartment);

                PropertyListing listing = new PropertyListing(
                        "Apartment " + floor + "-" + unit, "Apartment", "Younger Industries Apartments",
                        building.getId(), floor, apartment.getId());
                listing.setDescription("Apartment on floor " + floor + " of Younger Industries Apartments");
                listing.setBuyPrice(BigDecimal.ZERO);
                listing.setRentPrice(apartment.getMonthlyRent());
                listingRepository.save(listing);
            }
        }

        log.info("Created default Apartment Building with {} apartments", totalApartments);
    }

    private void createDefaultTemplates() {
        com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate[] templates = {
                new com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate("combat_crawl", "Combat Crawl",
                        "A dungeon focused on combat encounters with waves of enemies.",
                        "Combat", 1, 20, 5, 15,
                        "Combat,Elite,Boss", "Spike,Pressure Plate,Fire", "Gold,Weapons,Armor"),
                new com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate("puzzle_dungeon", "Puzzle Dungeon",
                        "A dungeon filled with traps, puzzles, and environmental challenges.",
                        "Puzzle", 1, 15, 5, 12,
                        "Puzzle,Environmental", "Trap,Complex Trap,Environmental", "Gold,Artifacts,Keys"),
                new com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate("boss_arena", "Boss Arena",
                        "A dungeon leading to a powerful boss encounter.",
                        "Boss", 5, 20, 3, 8,
                        "Boss,Elite", "Trap,Environmental", "Legendary Items,Gold,Trophies"),
                new com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate("trap_gallery", "Trap Gallery",
                        "A dungeon showcasing deadly traps and mechanisms.",
                        "Trap", 3, 18, 5, 10,
                        "Trap,Environmental", "Deadly Trap,Complex Trap,Chain Reaction", "Gold,Tools,Disguises"),
                new com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate("treasure_hunt", "Treasure Hunt",
                        "A dungeon rich with treasure and hidden caches.",
                        "Treasure", 1, 15, 5, 12,
                        "Environmental,Puzzle", "Trap,Hidden Door", "Gold,Magic Items,Relics"),
                new com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate("social_encounter", "Social Encounter",
                        "A dungeon focused on NPC interactions and diplomacy.",
                        "Social", 1, 10, 3, 8,
                        "Social,Diplomacy", "None", "Information,Favors,Alliances"),
                new com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate("horror_crawl", "Horror Crawl",
                        "A terrifying dungeon with psychological and physical threats.",
                        "Horror", 5, 20, 5, 15,
                        "Horror,Psychological", "Trap,Environmental,Psychological", "Relics,Gold,Escape Items"),
                new com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate("scifi_anomaly", "Sci-Fi Anomaly",
                        "A futuristic dungeon with alien technology and anomalies.",
                        "Sci-Fi", 3, 20, 5, 12,
                        "Alien,Technology", "Energy Field,Gravity Trap", "Tech,Data,Energy Cells")
        };

        templateRepository.saveAll(Arrays.asList(templates));

        log.info("Created {} default dungeon templates", templates.length);
    }

    private BigDecimal calculateOfficePrice(int floor) {
        return BigDecimal.valueOf(50000L + (floor - 1L) * 20000L);
    }

    private BigDecimal calculateOfficeRent(int floor) {
        return BigDecimal.valueOf(5000L + (floor - 1L) * 2000L);
    }

    private BigDecimal calculateApartmentRent(int floor) {
        return BigDecimal.valueOf(3000L + (floor - 1L) * 1000L);
    }

    private BuildingInstance toBuildingInstance(YoungerIndustriesHQ hq, ApartmentBuilding building) {
        BuildingInstance bi = new BuildingInstance();
        if (hq != null) {
            bi.setId(hq.getId());
            bi.setName(hq.getName());
            bi.setBuildingType("HQ");
            bi.setUpperFloors(hq.getOfficeFloors());
            bi.setBasementLevels(hq.getBasementLevels());
            bi.setOccupancyRate(hq.getOccupancyRate());
            bi.setActive(hq.isActive());
        } else if (building != null) {
            bi.setId(building.getId());
            bi.setName(building.getName());
            bi.setBuildingType("Apartment");
            bi.setUpperFloors(building.getApartmentFloors());
            bi.setBasementLevels(building.getBasementLevels());
            bi.setOccupancyRate(building.getOccupancyRate());
            bi.setActive(building.isActive());
        }
        return bi;
    }

    private BuildingInstanceService.DungeonInstanceTemplate toDtoTemplate(com.yisy.modules.doramcu.earth2026.model.DungeonInstanceTemplate entity) {
        BuildingInstanceService.DungeonInstanceTemplate dto = new BuildingInstanceService.DungeonInstanceTemplate();
        dto.setId(entity.getTemplateId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setTheme(entity.getTheme());
        dto.setMinDifficulty(entity.getMinDifficulty());
        dto.setMaxDifficulty(entity.getMaxDifficulty());
        dto.setMinRoomCount(entity.getMinRoomCount());
        dto.setMaxRoomCount(entity.getMaxRoomCount());
        dto.setEncounterTypes(entity.getEncounterTypes());
        dto.setTrapTypes(entity.getTrapTypes());
        dto.setTreasureTypes(entity.getTreasureTypes());
        return dto;
    }

    private BuildingInstanceService.DungeonInstance toDtoDungeonInstance(com.yisy.modules.doramcu.earth2026.model.DungeonInstance entity) {
        BuildingInstanceService.DungeonInstance dto = new BuildingInstanceService.DungeonInstance();
        dto.setId(entity.getId());
        dto.setBuildingId(entity.getBuildingId() != null ? entity.getBuildingId() : entity.getHqId());
        dto.setBasementLevel(entity.getBasementLevel());
        dto.setTemplateId(entity.getTemplateId());
        dto.setTemplateName(entity.getTemplateName());
        dto.setTheme(entity.getTheme());
        dto.setDifficulty(entity.getDifficulty());
        dto.setRoomCount(entity.getRoomCount());
        dto.setStatus(entity.getStatus());
        dto.setActive(entity.isActive());
        return dto;
    }
}

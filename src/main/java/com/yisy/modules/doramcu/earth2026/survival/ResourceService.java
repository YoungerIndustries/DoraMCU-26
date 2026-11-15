package com.yisy.modules.doramcu.earth2026.survival;

import org.springframework.stereotype.Service;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.model.Earth2026Location;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.service.BiosphereService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for managing survival resources in the Earth2026 world.
 * <p>
 * Provides resource initialization, scavenging, foraging, and hunting mechanics
 * based on location and character skills.
 * </p>
 */
@Service("earth2026ResourceService")
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);
    private static final Random random = new Random();

    private static final int EXPECTED_RESOURCE_COUNT_MIN = 30;

    private final SurvivalResourceRepository resourceRepository;
    private final Earth2026CharacterRepository characterRepository;
    private final BiosphereService biosphereService;

    private boolean initialized = false;

    public ResourceService(SurvivalResourceRepository resourceRepository,
                           Earth2026CharacterRepository characterRepository,
                           BiosphereService biosphereService) {
        this.resourceRepository = resourceRepository;
        this.characterRepository = characterRepository;
        this.biosphereService = biosphereService;
    }

    // ==================== Initialization ====================

    @Transactional
    public void initializeResources() {
        if (initialized) {
            return;
        }
        long count = resourceRepository.count();
        if (count >= EXPECTED_RESOURCE_COUNT_MIN) {
            log.info("Survival resources already populated: {} resources", count);
            initialized = true;
            return;
        }

        log.info("Initializing survival resources...");
        populateFoodResources();
        populateWaterResources();
        populateMaterialResources();
        populateMedicineResources();
        populateToolResources();
        populateWeaponResources();
        populateShelterResources();
        initialized = true;
        log.info("Survival resources initialized: {} resources", resourceRepository.count());
    }

    // ==================== Resource Queries ====================

    @Transactional(readOnly = true)
    public List<SurvivalResource> getAllResources() {
        return resourceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SurvivalResource> getResourcesByCategory(String category) {
        return resourceRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public SurvivalResource getResourceByName(String name) {
        return resourceRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found: " + name));
    }

    // ==================== Scavenging ====================

    /**
     * Scavenge for resources at a given location.
     * The amount and type of resources found depends on the location type,
     * terrain, and character skills (INT, WIS, DEX).
     *
     * @param characterId the character doing the scavenging
     * @param locationName the location name (optional, null for current location)
     * @return list of resources found
     */
    @Transactional
    public List<SurvivalResource> scavenge(Long characterId, String locationName) {
        Earth2026Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        String location = locationName != null ? locationName : character.getEarthLocation();
        if (location == null) {
            location = "Unknown Area";
        }

        // Determine location type for resource availability
        String locationType = determineLocationType(location);

        // Character skill modifiers
        int intScore = character.getIntScore();
        int wisScore = character.getWisScore();
        int dexScore = character.getDexScore();
        double skillModifier = (intScore + wisScore + dexScore) / 60.0; // 0.5 - 1.5

        // Base number of items found (1-5)
        int baseItems = 1 + random.nextInt(5);
        int itemsFound = Math.max(1, (int) (baseItems * skillModifier));

        List<SurvivalResource> found = new ArrayList<>();

        // Determine available resource pools based on location type
        List<String> availableCategories = getAvailableCategories(locationType);

        for (int i = 0; i < itemsFound; i++) {
            String category = availableCategories.get(random.nextInt(availableCategories.size()));
            List<SurvivalResource> pool = resourceRepository.findByCategory(category);
            if (!pool.isEmpty()) {
                SurvivalResource resource = pool.get(random.nextInt(pool.size()));
                found.add(resource);
            }
        }

        // Fatigue cost for scavenging
        int fatigueCost = Math.max(1, (int) (5 * (1.0 / skillModifier)));

        log.info("Character {} scavenged at {}: found {} items", character.getName(), location, found.size());
        return found;
    }

    // ==================== Foraging ====================

    /**
     * Forage for natural resources (food, water, materials) in the wild.
     * Depends heavily on the terrain type and WIS score.
     *
     * @param characterId the character doing the foraging
     * @return list of resources found
     */
    @Transactional
    public List<SurvivalResource> forage(Long characterId) {
        Earth2026Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        String location = character.getEarthLocation() != null ? character.getEarthLocation() : "Unknown Area";
        String terrainType = determineTerrainType(location);

        // WIS is the primary stat for foraging
        int wisScore = character.getWisScore();
        double skillModifier = wisScore / 10.0; // 0.5 - 2.0

        List<SurvivalResource> found = new ArrayList<>();

        // Foraging primarily yields food and water
        List<SurvivalResource> foodPool = resourceRepository.findByCategory("FOOD");
        List<SurvivalResource> waterPool = resourceRepository.findByCategory("WATER");

        // Food found
        if (!foodPool.isEmpty()) {
            int foodCount = Math.max(1, (int) (2 * skillModifier));
            for (int i = 0; i < foodCount; i++) {
                found.add(foodPool.get(random.nextInt(foodPool.size())));
            }
        }

        // Water found (less likely)
        if (!waterPool.isEmpty() && random.nextDouble() < skillModifier * 0.3) {
            found.add(waterPool.get(random.nextInt(waterPool.size())));
        }

        // Materials (rare)
        if (random.nextDouble() < skillModifier * 0.1) {
            List<SurvivalResource> materialPool = resourceRepository.findByCategory("MATERIAL");
            if (!materialPool.isEmpty()) {
                found.add(materialPool.get(random.nextInt(materialPool.size())));
            }
        }

        log.info("Character {} foraged at {}: found {} items", character.getName(), location, found.size());
        return found;
    }

    // ==================== Hunting ====================

    /**
     * Hunt for food resources. Requires STR and DEX skills.
     * Higher risk but higher reward than foraging.
     *
     * @param characterId the character doing the hunting
     * @return list of resources found (primarily food)
     */
    @Transactional
    public List<SurvivalResource> hunt(Long characterId) {
        Earth2026Character character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        String location = character.getEarthLocation() != null ? character.getEarthLocation() : "Unknown Area";

        // STR and DEX are primary stats for hunting
        int strScore = character.getStrScore();
        int dexScore = character.getDexScore();
        double skillModifier = (strScore + dexScore) / 20.0; // 1.0 - 4.0

        // Success chance based on skills
        double successChance = Math.min(0.8, 0.3 + skillModifier * 0.1);
        if (random.nextDouble() > successChance) {
            log.info("Character {} failed to hunt at {}", character.getName(), location);
            return new ArrayList<>();
        }

        List<SurvivalResource> found = new ArrayList<>();
        List<SurvivalResource> foodPool = resourceRepository.findByCategory("FOOD");

        if (!foodPool.isEmpty()) {
            // Hunting yields more food than foraging
            int foodCount = Math.max(1, (int) (3 * skillModifier));
            for (int i = 0; i < foodCount; i++) {
                // Prefer higher-value food from hunting
                List<SurvivalResource> highValueFood = foodPool.stream()
                        .filter(r -> r.getHungerValue() >= 15)
                        .toList();
                if (!highValueFood.isEmpty()) {
                    found.add(highValueFood.get(random.nextInt(highValueFood.size())));
                } else {
                    found.add(foodPool.get(random.nextInt(foodPool.size())));
                }
            }
        }

        log.info("Character {} hunted at {}: found {} items", character.getName(), location, found.size());
        return found;
    }

    // ==================== Location Helpers ====================

    private String determineLocationType(String location) {
        if (location == null) return "URBAN";
        String lower = location.toLowerCase();
        if (lower.contains("forest") || lower.contains("jungle") || lower.contains("woods")) return "FOREST";
        if (lower.contains("desert") || lower.contains("arid")) return "DESERT";
        if (lower.contains("mountain") || lower.contains("hill")) return "MOUNTAIN";
        if (lower.contains("city") || lower.contains("urban") || lower.contains("town")) return "URBAN";
        if (lower.contains("coast") || lower.contains("beach") || lower.contains("ocean")) return "COASTAL";
        if (lower.contains("farm") || lower.contains("field") || lower.contains("plains")) return "PLAINS";
        return "URBAN";
    }

    private String determineTerrainType(String location) {
        return determineLocationType(location);
    }

    private List<String> getAvailableCategories(String locationType) {
        return switch (locationType) {
            case "URBAN" -> List.of("FOOD", "WATER", "MATERIAL", "MEDICINE", "TOOL");
            case "FOREST" -> List.of("FOOD", "WATER", "MATERIAL", "SHELTER");
            case "DESERT" -> List.of("WATER", "MATERIAL");
            case "MOUNTAIN" -> List.of("FOOD", "MATERIAL", "SHELTER");
            case "COASTAL" -> List.of("FOOD", "WATER", "MATERIAL");
            case "PLAINS" -> List.of("FOOD", "WATER", "MATERIAL");
            default -> List.of("FOOD", "WATER", "MATERIAL");
        };
    }

    // ==================== Resource Population ====================

    private void populateFoodResources() {
        SurvivalResource r;

        r = new SurvivalResource("Canned Beans", "FOOD");
        r.setDescription("Canned beans, a staple survival food.");
        r.setHungerValue(20);
        r.setThirstValue(5);
        r.setWeight(0.5);
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Canned Tuna", "FOOD");
        r.setDescription("Canned tuna, high protein food.");
        r.setHungerValue(25);
        r.setWeight(0.4);
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Energy Bar", "FOOD");
        r.setDescription("High-energy survival bar.");
        r.setHungerValue(15);
        r.setFatigueReduction(5);
        r.setWeight(0.1);
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Rice Pack", "FOOD");
        r.setDescription("A pack of dried rice.");
        r.setHungerValue(30);
        r.setWeight(0.8);
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Dried Meat", "FOOD");
        r.setDescription("Jerky or dried meat strips.");
        r.setHungerValue(35);
        r.setWeight(0.3);
        r.setRarity("UNCOMMON");
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Wild Berries", "FOOD");
        r.setDescription("Foraged wild berries.");
        r.setHungerValue(10);
        r.setThirstValue(5);
        r.setWeight(0.2);
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Hunted Game", "FOOD");
        r.setDescription("Freshly hunted meat.");
        r.setHungerValue(40);
        r.setWeight(1.5);
        r.setRarity("UNCOMMON");
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Bread Loaf", "FOOD");
        r.setDescription("A loaf of bread.");
        r.setHungerValue(15);
        r.setWeight(0.4);
        r.setConsumable(true);
        resourceRepository.save(r);
    }

    private void populateWaterResources() {
        SurvivalResource r;

        r = new SurvivalResource("Water Bottle", "WATER");
        r.setDescription("A standard water bottle (500ml).");
        r.setThirstValue(25);
        r.setWeight(0.5);
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Large Water Jug", "WATER");
        r.setDescription("A large water jug (2L).");
        r.setThirstValue(50);
        r.setWeight(2.0);
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Purified Water", "WATER");
        r.setDescription("Purified drinking water.");
        r.setThirstValue(30);
        r.setHealthValue(5);
        r.setWeight(0.5);
        r.setRarity("UNCOMMON");
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Rain Water", "WATER");
        r.setDescription("Collected rain water.");
        r.setThirstValue(15);
        r.setWeight(0.3);
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Stream Water", "WATER");
        r.setDescription("Untreated stream water, may cause illness.");
        r.setThirstValue(20);
        r.setHealthValue(-5);
        r.setWeight(0.3);
        r.setConsumable(true);
        resourceRepository.save(r);
    }

    private void populateMaterialResources() {
        SurvivalResource r;

        r = new SurvivalResource("Wood Plank", "MATERIAL");
        r.setDescription("A wooden plank for building.");
        r.setWeight(2.0);
        r.setMaterial(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Metal Sheet", "MATERIAL");
        r.setDescription("A sheet of metal for construction.");
        r.setWeight(5.0);
        r.setMaterial(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Fabric Scrap", "MATERIAL");
        r.setDescription("Scrap fabric for crafting.");
        r.setWeight(0.2);
        r.setMaterial(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Rope", "MATERIAL");
        r.setDescription("A length of rope for various uses.");
        r.setWeight(0.5);
        r.setMaterial(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Duct Tape", "MATERIAL");
        r.setDescription("A roll of duct tape, versatile repair material.");
        r.setWeight(0.3);
        r.setMaterial(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Wire", "MATERIAL");
        r.setDescription("Electrical wire for repairs and crafting.");
        r.setWeight(0.4);
        r.setMaterial(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Plastic Sheet", "MATERIAL");
        r.setDescription("Plastic sheeting for shelter.");
        r.setWeight(1.0);
        r.setMaterial(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Concrete Block", "MATERIAL");
        r.setDescription("A heavy concrete block for construction.");
        r.setWeight(15.0);
        r.setMaterial(true);
        resourceRepository.save(r);
    }

    private void populateMedicineResources() {
        SurvivalResource r;

        r = new SurvivalResource("First Aid Kit", "MEDICINE");
        r.setDescription("A basic first aid kit.");
        r.setHealthValue(30);
        r.setWeight(1.0);
        r.setRarity("UNCOMMON");
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Antibiotics", "MEDICINE");
        r.setDescription("Antibiotic pills for infection.");
        r.setHealthValue(20);
        r.setWeight(0.1);
        r.setRarity("RARE");
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Painkillers", "MEDICINE");
        r.setDescription("Pain relief medication.");
        r.setHealthValue(10);
        r.setFatigueReduction(10);
        r.setWeight(0.1);
        r.setRarity("UNCOMMON");
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Bandages", "MEDICINE");
        r.setDescription("Sterile bandages for wounds.");
        r.setHealthValue(15);
        r.setWeight(0.2);
        r.setConsumable(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Stimulant", "MEDICINE");
        r.setDescription("A stimulant to reduce fatigue.");
        r.setFatigueReduction(30);
        r.setWeight(0.05);
        r.setRarity("RARE");
        r.setConsumable(true);
        resourceRepository.save(r);
    }

    private void populateToolResources() {
        SurvivalResource r;

        r = new SurvivalResource("Knife", "TOOL");
        r.setDescription("A multi-purpose survival knife.");
        r.setWeight(0.3);
        r.setTool(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Hammer", "TOOL");
        r.setDescription("A hammer for construction.");
        r.setWeight(1.0);
        r.setTool(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Saw", "TOOL");
        r.setDescription("A hand saw for cutting wood.");
        r.setWeight(1.5);
        r.setTool(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Wrench", "TOOL");
        r.setDescription("An adjustable wrench for repairs.");
        r.setWeight(0.8);
        r.setTool(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Flashlight", "TOOL");
        r.setDescription("A flashlight for visibility at night.");
        r.setWeight(0.3);
        r.setTool(true);
        resourceRepository.save(r);

        r = new SurvivalResource("Water Filter", "TOOL");
        r.setDescription("A portable water filter.");
        r.setWeight(0.4);
        r.setTool(true);
        r.setRarity("UNCOMMON");
        resourceRepository.save(r);
    }

    private void populateWeaponResources() {
        SurvivalResource r;

        r = new SurvivalResource("Improvised Club", "WEAPON");
        r.setDescription("A makeshift club for self-defense.");
        r.setWeight(1.0);
        resourceRepository.save(r);

        r = new SurvivalResource("Hunting Bow", "WEAPON");
        r.setDescription("A bow for hunting and defense.");
        r.setWeight(1.5);
        r.setRarity("UNCOMMON");
        resourceRepository.save(r);

        r = new SurvivalResource("Firearm", "WEAPON");
        r.setDescription("A firearm with limited ammunition.");
        r.setWeight(2.0);
        r.setRarity("RARE");
        resourceRepository.save(r);
    }

    private void populateShelterResources() {
        SurvivalResource r;

        r = new SurvivalResource("Tarp", "SHELTER");
        r.setDescription("A tarp for temporary shelter.");
        r.setWeight(1.5);
        r.setExposureReduction(10);
        resourceRepository.save(r);

        r = new SurvivalResource("Sleeping Bag", "SHELTER");
        r.setDescription("A sleeping bag for warmth.");
        r.setWeight(1.0);
        r.setExposureReduction(20);
        r.setTemperatureAdjustment(2.0);
        r.setFatigueReduction(15);
        resourceRepository.save(r);

        r = new SurvivalResource("Blanket", "SHELTER");
        r.setDescription("A warm blanket.");
        r.setWeight(0.8);
        r.setExposureReduction(10);
        r.setTemperatureAdjustment(1.0);
        resourceRepository.save(r);

        r = new SurvivalResource("Firewood", "SHELTER");
        r.setDescription("Firewood for a campfire.");
        r.setWeight(3.0);
        r.setExposureReduction(15);
        r.setTemperatureAdjustment(3.0);
        resourceRepository.save(r);
    }
}

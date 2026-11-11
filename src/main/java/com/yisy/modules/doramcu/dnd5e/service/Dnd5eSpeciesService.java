package com.yisy.modules.doramcu.dnd5e.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eSpecies;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eSpeciesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing D&D 5e species (races).
 * <p>
 * Provides CRUD operations for species, random name generation,
 * ability score increase lookup, and auto-population of all major
 * D&D 5e races with their 2024 PHB racial traits and name databases.
 * </p>
 */
@Service
public class Dnd5eSpeciesService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eSpeciesService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Dnd5eSpeciesRepository speciesRepository;

    public Dnd5eSpeciesService(Dnd5eSpeciesRepository speciesRepository) {
        this.speciesRepository = speciesRepository;
    }

    // --- CRUD ---

    public Dnd5eSpecies createSpecies(Dnd5eSpecies species) {
        return speciesRepository.save(species);
    }

    public Optional<Dnd5eSpecies> getSpecies(Long id) {
        return speciesRepository.findById(id);
    }

    public Optional<Dnd5eSpecies> getSpeciesByName(String name) {
        return speciesRepository.findByName(name);
    }

    public List<Dnd5eSpecies> listSpecies() {
        return speciesRepository.findAll();
    }

    public List<Dnd5eSpecies> listSpeciesByCategory(String category) {
        return speciesRepository.findByCategory(category);
    }

    public List<Dnd5eSpecies> listSpeciesBySize(String size) {
        return speciesRepository.findBySize(size);
    }

    public List<Dnd5eSpecies> listSpeciesBySubspecies(String subspecies) {
        return speciesRepository.findBySubspecies(subspecies);
    }

    public List<Dnd5eSpecies> listSpeciesWithDarkvision() {
        return speciesRepository.findByDarkvision();
    }

    public List<Dnd5eSpecies> listSpeciesByLifespan(int minLifespan) {
        return speciesRepository.findByLifespanGte(minLifespan);
    }

    public List<Dnd5eSpecies> searchSpecies(String keyword) {
        return speciesRepository.searchByKeyword(keyword);
    }

    public List<Dnd5eSpecies> listSpeciesByRacialTrait(String trait) {
        return speciesRepository.findByRacialTrait(trait);
    }

    public Dnd5eSpecies updateSpecies(Long id, Dnd5eSpecies updated) {
        Dnd5eSpecies existing = speciesRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Species not found: " + id));
        existing.setName(updated.getName());
        existing.setSubspecies(updated.getSubspecies());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setSize(updated.getSize());
        existing.setBaseSpeed(updated.getBaseSpeed());
        existing.setLifespan(updated.getLifespan());
        existing.setDarkvisionRange(updated.getDarkvisionRange());
        existing.setAbilityIncreases(updated.getAbilityIncreases());
        existing.setRacialTraits(updated.getRacialTraits());
        existing.setLanguages(updated.getLanguages());
        existing.setMaleFirstNames(updated.getMaleFirstNames());
        existing.setFemaleFirstNames(updated.getFemaleFirstNames());
        existing.setLastNames(updated.getLastNames());
        existing.setTitlePrefixes(updated.getTitlePrefixes());
        existing.setTitleSuffixes(updated.getTitleSuffixes());
        existing.setAiLeaderTitles(updated.getAiLeaderTitles());
        existing.setAiGovernorTitles(updated.getAiGovernorTitles());
        existing.setAiCaptainTitles(updated.getAiCaptainTitles());
        return speciesRepository.save(existing);
    }

    public void deleteSpecies(Long id) {
        speciesRepository.deleteById(id);
    }

    // --- Name Generation ---

    /**
     * Generate a random first name for the given species.
     * @param speciesName name of the species
     * @param male true for male name, false for female name
     * @return a random first name, or "Unknown" if no names available
     */
    public String generateFirstName(String speciesName, boolean male) {
        Dnd5eSpecies species = speciesRepository.findByName(speciesName)
                .orElse(null);
        if (species == null) {
            return "Unknown";
        }
        List<String> names = parseJsonArray(male ? species.getMaleFirstNames() : species.getFemaleFirstNames());
        if (names.isEmpty()) {
            return "Unknown";
        }
        return names.get(new Random().nextInt(names.size()));
    }

    /**
     * Generate a random last name for the given species.
     * @param speciesName name of the species
     * @return a random last name, or "" if no names available
     */
    public String generateLastName(String speciesName) {
        Dnd5eSpecies species = speciesRepository.findByName(speciesName)
                .orElse(null);
        if (species == null) {
            return "";
        }
        List<String> names = parseJsonArray(species.getLastNames());
        if (names.isEmpty()) {
            return "";
        }
        return names.get(new Random().nextInt(names.size()));
    }

    /**
     * Generate a full random name for the given species.
     * @param speciesName name of the species
     * @param male true for male name, false for female name
     * @return a full name (first + last) or just first name
     */
    public String generateFullName(String speciesName, boolean male) {
        String first = generateFirstName(speciesName, male);
        String last = generateLastName(speciesName);
        if (last.isEmpty()) {
            return first;
        }
        return first + " " + last;
    }

    /**
     * Generate a random title for the given species and role.
     * @param speciesName name of the species
     * @param role "leader", "governor", "captain", or "prefix"/"suffix"
     * @return a random title, or "" if no titles available
     */
    public String generateTitle(String speciesName, String role) {
        Dnd5eSpecies species = speciesRepository.findByName(speciesName)
                .orElse(null);
        if (species == null) {
            return "";
        }
        String json;
        switch (role.toLowerCase()) {
            case "leader":
                json = species.getAiLeaderTitles();
                break;
            case "governor":
                json = species.getAiGovernorTitles();
                break;
            case "captain":
                json = species.getAiCaptainTitles();
                break;
            case "prefix":
                json = species.getTitlePrefixes();
                break;
            case "suffix":
                json = species.getTitleSuffixes();
                break;
            default:
                return "";
        }
        List<String> titles = parseJsonArray(json);
        if (titles.isEmpty()) {
            return "";
        }
        return titles.get(new Random().nextInt(titles.size()));
    }

    // --- Ability Score Increases ---

    /**
     * Get the ability score increases for a species as a map.
     * @param speciesName name of the species
     * @return map of ability name to increase (e.g., {"STR": 2, "CON": 1})
     */
    public Map<String, Integer> getAbilityIncreases(String speciesName) {
        Dnd5eSpecies species = speciesRepository.findByName(speciesName)
                .orElse(null);
        if (species == null) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(species.getAbilityIncreases(),
                    new TypeReference<Map<String, Integer>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse ability increases for {}", speciesName, e);
            return Collections.emptyMap();
        }
    }

    /**
     * Get the racial traits for a species as a list.
     * @param speciesName name of the species
     * @return list of trait names
     */
    public List<String> getRacialTraits(String speciesName) {
        Dnd5eSpecies species = speciesRepository.findByName(speciesName)
                .orElse(null);
        if (species == null) {
            return Collections.emptyList();
        }
        return parseJsonArray(species.getRacialTraits());
    }

    // --- Reference Data ---

    public List<String> getCategories() {
        return speciesRepository.findAll().stream()
                .map(Dnd5eSpecies::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getSizes() {
        return speciesRepository.findAll().stream()
                .map(Dnd5eSpecies::getSize)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // --- Database Initialization ---

    @Transactional
    public void initializeSpeciesDatabase() {
        if (speciesRepository.existsByName("Human")) {
            log.info("Species database already initialized, skipping.");
            return;
        }
        populateSpecies();
        log.info("Species database initialized with {} species.", speciesRepository.count());
    }

    private void populateSpecies() {
        // Core PHB races
        speciesRepository.save(createHuman());
        speciesRepository.save(createElf());
        speciesRepository.save(createDwarf());
        speciesRepository.save(createHalfling());
        speciesRepository.save(createDragonborn());
        speciesRepository.save(createGnome());
        speciesRepository.save(createHalfElf());
        speciesRepository.save(createHalfOrc());
        speciesRepository.save(createTiefling());
        speciesRepository.save(createAasimar());

        // Genasi variants
        speciesRepository.save(createGenasi("Air", "Air", "Flyby", "Mingle"));
        speciesRepository.save(createGenasi("Earth", "Earth", "Earth's Blessing", "Stone's Endurance"));
        speciesRepository.save(createGenasi("Fire", "Fire", "Fire Metamorphosis", "Fire Resistance"));
        speciesRepository.save(createGenasi("Water", "Water", "Amphibious", "Control Flames"));

        // TCE races
        speciesRepository.save(createTabaxi());
        speciesRepository.save(createTortle());
        speciesRepository.save(createTriton());
        speciesRepository.save(createKenku());
        speciesRepository.save(createBugbear());
        speciesRepository.save(createGoliath());
        speciesRepository.save(createHobgoblin());
        speciesRepository.save(createLizardfolk());
        speciesRepository.save(createSatyr());
        speciesRepository.save(createFirbolg());
    }

    // --- Species Factory Methods ---

    private Dnd5eSpecies createHuman() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Human");
        s.setDescription("Adaptable and ambitious, humans are the most common and versatile of the core races.");
        s.setCategory("Core");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(100);
        s.setDarkvisionRange(0);
        s.setAbilityIncreases("{\"STR\": 1, \"DEX\": 1, \"CON\": 1, \"INT\": 1, \"WIS\": 1, \"CHA\": 1}");
        s.setRacialTraits("[\"Customizable Origin\", \"Extra Language\"]");
        s.setLanguages("Common, one additional language");
        s.setMaleFirstNames("[\"Aldern\", \"Berick\", \"Corrin\", \"Darrin\", \"Edryn\", \"Farran\", \"Garrick\", \"Haldor\", \"Ivan\", \"Jarek\", \"Kael\", \"Lorren\", \"Merric\", \"Nolan\", \"Orin\", \"Perrin\", \"Quinn\", \"Roderick\", \"Soren\", \"Theron\", \"Ulric\", \"Vance\", \"Wulfric\", \"Xander\", \"Yorick\", \"Zane\"]");
        s.setFemaleFirstNames("[\"Aeliana\", \"Brenna\", \"Caelia\", \"Daria\", \"Elara\", \"Fiona\", \"Gwendolyn\", \"Hester\", \"Isolde\", \"Janna\", \"Kaelin\", \"Liora\", \"Mira\", \"Nerissa\", \"Ophelia\", \"Petra\", \"Quinn\", \"Rowan\", \"Seraphina\", \"Thalia\", \"Una\", \"Vivian\", \"Wren\", \"Xena\", \"Yara\", \"Zara\"]");
        s.setLastNames("[\"Ashford\", \"Blackwood\", \"Carrow\", \"Dunmore\", \"Everett\", \"Fairchild\", \"Greystone\", \"Hawthorne\", \"Ironwood\", \"Jasper\", \"Kingsley\", \"Lancaster\", \"Morrow\", \"Nightingale\", \"Oakenshield\", \"Pemberton\", \"Quill\", \"Ravenswood\", \"Stonebridge\", \"Thornwall\", \"Underhill\", \"Vale\", \"Whitmore\", \"York\", \"Zephyr\"]");
        s.setTitlePrefixes("[\"Lord\", \"Lady\", \"Sir\", \"Dame\", \"Master\", \"Mistress\"]");
        s.setTitleSuffixes("[\"the Wise\", \"the Bold\", \"the Just\", \"the Fair\", \"the Strong\"]");
        s.setAiLeaderTitles("[\"King\", \"Queen\", \"Duke\", \"Duchess\", \"Baron\", \"Baroness\", \"Mayor\", \"Governor\"]");
        s.setAiGovernorTitles("[\"Lord\", \"Lady\", \"Magistrate\", \"Sheriff\", \"Steward\"]");
        s.setAiCaptainTitles("[\"Captain\", \"Commander\", \"Lieutenant\", \"Sergeant\"]");
        return s;
    }

    private Dnd5eSpecies createElf() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Elf");
        s.setDescription("Graceful and long-lived, elves are attuned to magic and nature with keen senses and an innate connection to the Feywild.");
        s.setCategory("Core");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(750);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"DEX\": 2}");
        s.setRacialTraits("[\"Darkvision\", \"Fey Ancestry\", \"Keen Senses\", \"Trance\", \"Elf Weapon Training\"]");
        s.setLanguages("Common, Elvish");
        s.setMaleFirstNames("[\"Aelar\", \"Caelar\", \"Daeron\", \"Elandor\", \"Faelar\", \"Galadon\", \"Haldir\", \"Ilyarion\", \"Javari\", \"Kaelen\", \"Luthien\", \"Mithrandir\", \"Naelar\", \"Oropher\", \"Quelvar\", \"Rivendell\", \"Thranduil\", \"Ulmo\", \"Vaelen\", \"Xandril\", \"Yavim\", \"Zephyros\"]");
        s.setFemaleFirstNames("[\"Aelindra\", \"Caelia\", \"Daeriel\", \"Elaria\", \"Faenara\", \"Galadriel\", \"Halthiriel\", \"Ilyana\", \"Javariel\", \"Kaelindra\", \"Luthien\", \"Mithrellas\", \"Naelia\", \"Orophiel\", \"Quelindra\", \"Rivendel\", \"Thranduila\", \"Ulmoira\", \"Vaelindra\", \"Xandriel\", \"Yavimiel\", \"Zephyra\"]");
        s.setLastNames("[\"Amakiir\", \"Amastacia\", \"Amphrey\", \"Anastarion\", \"Arel\", \"Galanodel\", \"Holimion\", \"Hostrith\", \"Ilphelkiir\", \"Liadon\", \"Meliamne\", \"Naelox\", \"Orastryon\", \"Riambh\", \"Siannodel\", \"Tireluvi\", \"Uomami\", \"Xilos\"]");
        s.setTitlePrefixes("[\"Lord\", \"Lady\", \"Prince\", \"Princess\", \"Thane\", \"Lady of the Wood\"]");
        s.setTitleSuffixes("[\"the Graceful\", \"the Swift\", \"the Enchanting\", \"the Ancient\", \"the Fey-touched\"]");
        s.setAiLeaderTitles("[\"Elven King\", \"Elven Queen\", \"High Lord\", \"High Lady\", \"Archdruid\"]");
        s.setAiGovernorTitles("[\"Thane\", \"Forest Warden\", \"Moon Priest\", \"Star Speaker\"]");
        s.setAiCaptainTitles("[\"Ranger Captain\", \"Scout Leader\", \"Wood Guard Commander\"]");
        return s;
    }

    private Dnd5eSpecies createDwarf() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Dwarf");
        s.setDescription("Tough and resilient, dwarves are master craftsmen who value honor, tradition, and the treasures of the earth.");
        s.setCategory("Core");
        s.setSize("Medium");
        s.setBaseSpeed(25);
        s.setLifespan(350);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"CON\": 2}");
        s.setRacialTraits("[\"Darkvision\", \"Dwarven Resilience\", \"Stonecunning\", \"Dwarven Combat Training\", \"Tool Proficiency\"]");
        s.setLanguages("Common, Dwarvish");
        s.setMaleFirstNames("[\"Adrik\", \"Alberich\", \"Baern\", \"Barendd\", \"Brottor\", \"Dain\", \"Darrak\", \"Eberk\", \"Einkil\", \"Fargrim\", \"Gardain\", \"Harbek\", \"Kildrak\", \"Morgran\", \"Orsik\", \"Rangrim\", \"Tordek\", \"Veit\", \"Versipoo\", \"Zug\"]");
        s.setFemaleFirstNames("[\"Adriana\", \"Argenta\", \"Bardryn\", \"Beren\", \"Dagnal\", \"Eldeth\", \"Gurdis\", \"Haldina\", \"Helja\", \"Kathra\", \"Kima\", \"Liftrasa\", \"Mardred\", \"Marta\", \"Nanatha\", \"Onya\", \"Perren\", \"Styna\", \"Torbera\", \"Yda\"]");
        s.setLastNames("[\"Balderk\", \"Battlehammer\", \"Brawnanvil\", \"Dankil\", \"Fireforge\", \"Gorunn\", \"Holderhek\", \"Ironfist\", \"Lutke\", \"Rumnaheim\", \"Strakeln\", \"Torunn\", \"Ungart\", \"Urist\", \"Uthor\", \"Vondal\", \"Zanbrad\"]");
        s.setTitlePrefixes("[\"Thane\", \"Jarl\", \"Master\", \"Mistress\", \"Clan Chief\"]");
        s.setTitleSuffixes("[\"the Stalwart\", \"the Forge-born\", \"the Unyielding\", \"the Stone-hearted\", \"the Deep-delver\"]");
        s.setAiLeaderTitles("[\"King\", \"Queen\", \"Jarl\", \"Clan Lord\", \"High Thane\"]");
        s.setAiGovernorTitles("[\"Thane\", \"Hall Master\", \"Forge Master\", \"Mine Warden\"]");
        s.setAiCaptainTitles("[\"Shield Captain\", \"Guard Commander\", \"Mine Captain\"]");
        return s;
    }

    private Dnd5eSpecies createHalfling() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Halfling");
        s.setDescription("Small and unassuming, halflings are lucky and nimble, preferring the comfort of home to the dangers of adventure.");
        s.setCategory("Core");
        s.setSize("Small");
        s.setBaseSpeed(25);
        s.setLifespan(150);
        s.setDarkvisionRange(0);
        s.setAbilityIncreases("{\"DEX\": 2}");
        s.setRacialTraits("[\"Lucky\", \"Brave\", \"Halfling Nimbleness\"]");
        s.setLanguages("Common, Halfling");
        s.setMaleFirstNames("[\"Cade\", \"Chele\", \"Corrin\", \"Eldon\", \"Erril\", \"Garret\", \"Hilmer\", \"Jasper\", \"Kelby\", \"Lindal\", \"Merric\", \"Milo\", \"Osborn\", \"Osvald\", \"Perrin\", \"Pwyll\", \"Quinn\", \"Roderick\", \"Shilbey\", \"Wayn\"]");
        s.setFemaleFirstNames("[\"Brenna\", \"Cora\", \"Elowen\", \"Fiona\", \"Gwen\", \"Hester\", \"Imar\", \"Jillian\", \"Kathryn\", \"Lavinia\", \"Mara\", \"Nora\", \"Ophelia\", \"Petra\", \"Quinn\", \"Rowan\", \"Seraphina\", \"Thalia\", \"Una\", \"Vivian\"]");
        s.setLastNames("[\"Cauldwell\", \"Goodwin\", \"Greenbottle\", \"Hawkins\", \"Lightfoot\", \"Merric\", \"Noble\", \"Proudfoot\", \"Tealeaf\", \"Underhill\", \"Whitmore\", \"Wheatley\"]");
        s.setTitlePrefixes("[\"Master\", \"Mistress\", \"Squire\"]");
        s.setTitleSuffixes("[\"the Lucky\", \"the Nimble\", \"the Brave\", \"the Cheerful\", \"the Unassuming\"]");
        s.setAiLeaderTitles("[\"Mayor\", \"Village Elder\", \"Innkeeper\", \"Guild Master\"]");
        s.setAiGovernorTitles("[\"Village Elder\", \"Town Crier\", \"Market Master\"]");
        s.setAiCaptainTitles("[\"Guard Captain\", \"Scout Leader\", \"Messenger Chief\"]");
        return s;
    }

    private Dnd5eSpecies createDragonborn() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Dragonborn");
        s.setDescription("Proud and honorable dragon-blooded humanoids who carry the legacy of dragons in their blood and breath.");
        s.setCategory("Core");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(80);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"STR\": 2, \"CHA\": 1}");
        s.setRacialTraits("[\"Draconic Ancestry\", \"Breath Weapon\", \"Damage Resistance\"]");
        s.setLanguages("Common, Draconic");
        s.setMaleFirstNames("[\"Akra\", \"Donaar\", \"Feng\", \"Haru\", \"Heskan\", \"Kriv\", \"Medrash\", \"Nadarr\", \"Pandjed\", \"Rhogar\", \"Shamash\", \"Tarhin\", \"Torinn\", \"Ver金\", \"Wu\"]");
        s.setFemaleFirstNames("[\"Akra\", \"Donaara\", \"Feng\", \"Haru\", \"Heska\", \"Kriv\", \"Medra\", \"Nadara\", \"Pandje\", \"Rhogara\", \"Shamasha\", \"Tarhina\", \"Torinna\", \"Ver\", \"Wu\"]");
        s.setLastNames("[\"Clethtinthiallor\", \"Daardendrian\", \"Delmirev\", \"Drachedandion\", \"Fenkenkabradon\", \"Kimbatuul\", \"Khrumztek\", \"Nemmonis\", \"Norixius\", \"Ophinshtaj\", \"Prexijhela\", \"Shestendeli\", \"Turifndol\", \"Verghoth\"]");
        s.setTitlePrefixes("[\"Warlord\", \"Chieftain\", \"Dragon\", \"Scion\"]");
        s.setTitleSuffixes("[\"the Dragonborn\", \"the Proud\", \"the Fearless\", \"the Breath of Fire\", \"the Honorable\"]");
        s.setAiLeaderTitles("[\"Warlord\", \"High Chieftain\", \"Dragon Lord\", \"Clan Leader\"]");
        s.setAiGovernorTitles("[\"District Chieftain\", \"Clan Elder\", \"Dragon Speaker\"]");
        s.setAiCaptainTitles("[\"War Captain\", \"Dragon Guard\", \"Clan Commander\"]");
        return s;
    }

    private Dnd5eSpecies createGnome() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Gnome");
        s.setDescription("Curious and inventive, gnomes are small folk with a love of magic, trickery, and mechanical devices.");
        s.setCategory("Core");
        s.setSize("Small");
        s.setBaseSpeed(25);
        s.setLifespan(425);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"INT\": 2}");
        s.setRacialTraits("[\"Darkvision\", \"Gnome Cunning\", \"Illusion Cantrip\"]");
        s.setLanguages("Common, Gnomish");
        s.setMaleFirstNames("[\"Alston\", \"Brocc\", \"Dimble\", \"Eldon\", \"Fonkin\", \"Gerbo\", \"Jebeddo\", \"Namfoodle\", \"Orryn\", \"Roondar\", \"Sindri\", \"Warryn\", \"Zook\"]");
        s.setFemaleFirstNames("[\"Bimpnottin\", \"Caramip\", \"Duvamil\", \"Ellywick\", \"Furgara\", \"Lilli\", \"Nissa\", \"Oda\", \"Roywyn\", \"Waywocket\", \"Zanna\"]");
        s.setLastNames("[\"Cambrar\", \"Doan\", \"FolkoftheWind\", \"Gnoblebottom\", \"Jokweld\", \"Nackle\", \"Ning\", \"Ninkap\", \"Raulnor\", \"Scheppen\", \"Turen\"]");
        s.setTitlePrefixes("[\"Master\", \"Mistress\", \"Tinker\", \"Artificer\"]");
        s.setTitleSuffixes("[\"the Clever\", \"the Inventive\", \"the Trickster\", \"the Illusionist\", \"the Tinkerer\"]");
        s.setAiLeaderTitles("[\"Grand Tinker\", \"High Artificer\", \"Guild Master\", \"Inventor Supreme\"]");
        s.setAiGovernorTitles("[\"District Tinker\", \"Guild Elder\", \"Inventor Council\"]");
        s.setAiCaptainTitles("[\"Tinker Captain\", \"Device Commander\", \"Gadget Leader\"]");
        return s;
    }

    private Dnd5eSpecies createHalfElf() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Half-Elf");
        s.setDescription("Part human, part elf, half-elves combine the adaptability of humans with the grace and magic of elves.");
        s.setCategory("Core");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(180);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"CHA\": 2}");
        s.setRacialTraits("[\"Darkvision\", \"Fey Ancestry\", \"Skill Versatility\"]");
        s.setLanguages("Common, Elvish");
        s.setMaleFirstNames("[\"Aelar\", \"Darian\", \"Elandor\", \"Faelar\", \"Halfel\", \"Kaelen\", \"Lorren\", \"Merric\", \"Nolan\", \"Theron\"]");
        s.setFemaleFirstNames("[\"Aeliana\", \"Daria\", \"Elara\", \"Fiona\", \"Halfel\", \"Kaelin\", \"Liora\", \"Mira\", \"Nerissa\", \"Thalia\"]");
        s.setLastNames("[\"Ashford\", \"Blackwood\", \"Everett\", \"Hawthorne\", \"Ironwood\", \"Kingsley\", \"Morrow\", \"Stonebridge\", \"Thornwall\", \"Whitmore\"]");
        s.setTitlePrefixes("[\"Lord\", \"Lady\", \"Diplomat\", \"Envoy\"]");
        s.setTitleSuffixes("[\"the Charismatic\", \"the Diplomatic\", \"the Graceful\", \"the Adaptable\", \"the Fey-touched\"]");
        s.setAiLeaderTitles("[\"Diplomat\", \"Envoy\", \"Ambassador\", \"Court Speaker\"]");
        s.setAiGovernorTitles("[\"District Envoy\", \"Court Elder\", \"Diplomatic Speaker\"]");
        s.setAiCaptainTitles("[\"Envoy Captain\", \"Diplomatic Guard\", \"Court Commander\"]");
        return s;
    }

    private Dnd5eSpecies createHalfOrc() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Half-Orc");
        s.setDescription("Part human, part orc, half-orcs are tough and intimidating, carrying the strength and resilience of their orcish heritage.");
        s.setCategory("Core");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(75);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"STR\": 2, \"CON\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Menacing\", \"Relentless Endurance\", \"Savage Attacks\"]");
        s.setLanguages("Common, Orcish");
        s.setMaleFirstNames("[\"Dench\", \"Feng\", \"Gell\", \"Henk\", \"Holg\", \"Imsh\", \"Keth\", \"Krusk\", \"Mhurren\", \"Ront\", \"Shump\", \"Thogar\", \"Venom\", \"Vergad\", \"Yersha\"]");
        s.setFemaleFirstNames("[\"Billa\", \"Grukka\", \"Kansif\", \"Myev\", \"Neega\", \"Ovak\", \"Shautha\", \"Sutha\", \"Vola\", \"Yersha\"]");
        s.setLastNames("[\"Fankh\", \"Gank\", \"Harank\", \"Hemran\", \"Kank\", \"Krusk\", \"Mank\", \"Nemman\", \"Nimk\", \"Rimk\", \"Shank\", \"Tharank\"]");
        s.setTitlePrefixes("[\"Warlord\", \"Chieftain\", \"Berserker\", \"Brute\"]");
        s.setTitleSuffixes("[\"the Fierce\", \"the Unyielding\", \"the Relentless\", \"the Savage\", \"the Menacing\"]");
        s.setAiLeaderTitles("[\"Warlord\", \"Chieftain\", \"Berserker King\", \"Clan Leader\"]");
        s.setAiGovernorTitles("[\"District Warlord\", \"Clan Elder\", \"Brute Commander\"]");
        s.setAiCaptainTitles("[\"War Captain\", \"Berserker Leader\", \"Clan Commander\"]");
        return s;
    }

    private Dnd5eSpecies createTiefling() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Tiefling");
        s.setDescription("Born of infernal blood, tieflings bear the marks of their fiendish heritage with horns, tails, and an innate connection to the planes.");
        s.setCategory("Core");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(100);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"CHA\": 2, \"INT\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Hellish Resistance\", \"Infernal Legacy\"]");
        s.setLanguages("Common, Infernal");
        s.setMaleFirstNames("[\"Amnon\", \"Arazoth\", \"Barakas\", \"Beltar\", \"Cimbal\", \"Duvik\", \"Evik\", \"Kairon\", \"Levic\", \"Mammon\", \"Mephost\", \"Narchus\", \"Orcane\", \"Paz\", \"Shax\"]");
        s.setFemaleFirstNames("[\"Amara\", \"Arazoth\", \"Baraka\", \"Beltara\", \"Cimbal\", \"Duvika\", \"Evika\", \"Kairona\", \"Levica\", \"Mammona\", \"Mephosta\", \"Narchusa\", \"Orcana\", \"Paza\", \"Shaxa\"]");
        s.setLastNames("[\"Amnon\", \"Arazoth\", \"Barakas\", \"Beltar\", \"Cimbal\", \"Duvik\", \"Evik\", \"Kairon\", \"Levict\", \"Mammon\", \"Mephost\", \"Narchus\", \"Orcane\", \"Paz\", \"Shax\"]");
        s.setTitlePrefixes("[\"Lord\", \"Lady\", \"Inquisitor\", \"Warlock\"]");
        s.setTitleSuffixes("[\"the Cursed\", \"the Fiend-touched\", \"the Infernal\", \"the Planar\", \"the Horned\"]");
        s.setAiLeaderTitles("[\"Inquisitor\", \"Archwarlock\", \"Demon Lord\", \"Fiend Speaker\"]");
        s.setAiGovernorTitles("[\"District Inquisitor\", \"Warlock Elder\", \"Infernal Speaker\"]");
        s.setAiCaptainTitles("[\"Inquisitor Captain\", \"Warlock Commander\", \"Fiend Guard\"]");
        return s;
    }

    private Dnd5eSpecies createAasimar() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Aasimar");
        s.setDescription("Touched by the celestial planes, aasimar bear the light of heaven and the power to heal or smite their foes.");
        s.setCategory("Core");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(160);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"CHA\": 2, \"WIS\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Celestial Resistance\", \"Healing Hands\", \"Celestial Arms\"]");
        s.setLanguages("Common, Celestial");
        s.setMaleFirstNames("[\"Aariyon\", \"Aeldryn\", \"Anarch\", \"Arator\", \"Avacyn\", \"Cael\", \"Dael\", \"Elyon\", \"Faelan\", \"Gaelen\", \"Haelor\", \"Ilyon\", \"Jaelin\", \"Kaelen\", \"Laelor\"]");
        s.setFemaleFirstNames("[\"Aariyona\", \"Aeldryna\", \"Anarcha\", \"Aratora\", \"Avacyna\", \"Caela\", \"Daela\", \"Elyona\", \"Faelana\", \"Gaelena\", \"Haelora\", \"Ilyona\", \"Jaelina\", \"Kaelena\", \"Laelora\"]");
        s.setLastNames("[\"Aariyon\", \"Aeldryn\", \"Anarch\", \"Arator\", \"Avacyn\", \"Cael\", \"Dael\", \"Elyon\", \"Faelan\", \"Gaelen\", \"Haelor\", \"Ilyon\", \"Jaelin\", \"Kaelen\", \"Laelor\"]");
        s.setTitlePrefixes("[\"Angel\", \"Seraph\", \"Paladin\", \"Champion\"]");
        s.setTitleSuffixes("[\"the Blessed\", \"the Celestial\", \"the Holy\", \"the Radiant\", \"the Divine\"]");
        s.setAiLeaderTitles("[\"Archangel\", \"Seraphim\", \"High Paladin\", \"Celestial Champion\"]");
        s.setAiGovernorTitles("[\"District Angel\", \"Paladin Elder\", \"Celestial Speaker\"]");
        s.setAiCaptainTitles("[\"Angel Captain\", \"Paladin Commander\", \"Celestial Guard\"]");
        return s;
    }

    private Dnd5eSpecies createGenasi(String element, String breathType, String trait1, String trait2) {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName(element + " Genasi");
        s.setDescription("Born of elemental blood, " + element.toLowerCase() + " genasi carry the power of the " + element.toLowerCase() + " elemental plane within them.");
        s.setCategory("Core");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(100);
        s.setDarkvisionRange(0);
        s.setAbilityIncreases("{\"CON\": 2, \"CHA\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Fey Ancestry\", \"Trance\", \"Elf Weapon Training\"]");
        s.setLanguages("Common, Elvish");
        s.setMaleFirstNames("[\"Aelar\", \"Caelar\", \"Daeron\", \"Elandor\", \"Faelar\", \"Galadon\", \"Haldir\", \"Ilyarion\", \"Javari\", \"Kaelen\"]");
        s.setFemaleFirstNames("[\"Aelindra\", \"Caelia\", \"Daeriel\", \"Elaria\", \"Faenara\", \"Galadriel\", \"Halthiriel\", \"Ilyana\", \"Javariel\", \"Kaelindra\"]");
        s.setLastNames("[\"Amakiir\", \"Amastacia\", \"Amphrey\", \"Anastarion\", \"Arel\", \"Galanodel\", \"Holimion\", \"Hostrith\", \"Ilphelkiir\", \"Liadon\"]");
        s.setTitlePrefixes("[\"Elemental\", \"Spirit\", \"Warden\"]");
        s.setTitleSuffixes("[\"the " + element + "-touched\", \"the Elemental\", \"the " + element + "-born\"]");
        s.setAiLeaderTitles("[\"Elemental Lord\", \"Spirit Warden\", \"Plane Speaker\"]");
        s.setAiGovernorTitles("[\"District Warden\", \"Elemental Elder\", \"Spirit Speaker\"]");
        s.setAiCaptainTitles("[\"Elemental Captain\", \"Spirit Guard\", \"Plane Commander\"]");
        return s;
    }

    private Dnd5eSpecies createTabaxi() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Tabaxi");
        s.setDescription("Feline humanoids driven by curiosity and a love of exploration, tabaxi are natural scouts and traders.");
        s.setCategory("TCE");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(70);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"DEX\": 2, \"CHA\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Feline Agility\", \"Cat's Claws\", \"Curious Nature\"]");
        s.setLanguages("Common, Primordial");
        s.setMaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\", \"Nar\", \"Osh\", \"Pur\", \"Rak\", \"Shin\"]");
        s.setFemaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\", \"Nar\", \"Osh\", \"Pur\", \"Rak\", \"Shin\"]");
        s.setLastNames("[\"Claw\", \"Paw\", \"Tail\", \"Whisker\", \"Fang\", \"Fur\", \"Pelt\", \"Stripe\", \"Spot\", \"Shadow\"]");
        s.setTitlePrefixes("[\"Scout\", \"Trader\", \"Hunter\", \"Stalker\"]");
        s.setTitleSuffixes("[\"the Swift\", \"the Curious\", \"the Feline\", \"the Explorer\", \"the Silent\"]");
        s.setAiLeaderTitles("[\"Caravan Master\", \"Trade Lord\", \"Scout Leader\", \"Hunter Chief\"]");
        s.setAiGovernorTitles("[\"District Scout\", \"Trade Elder\", \"Caravan Master\"]");
        s.setAiCaptainTitles("[\"Scout Captain\", \"Trade Guard\", \"Hunter Commander\"]");
        return s;
    }

    private Dnd5eSpecies createTortle() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Tortle");
        s.setDescription("Turtle-folk who carry their homes on their backs, tortles are patient, resilient, and deeply connected to nature.");
        s.setCategory("TCE");
        s.setSize("Medium");
        s.setBaseSpeed(20);
        s.setLifespan(100);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"STR\": 1, \"DEX\": 1, \"CON\": 1, \"WIS\": 1}");
        s.setRacialTraits("[\"Shell Defense\", \"Natural Armor\", \"Swimmer\", \"Hold Breath\", \"Stone Camouflage\"]");
        s.setLanguages("Common, Aquan");
        s.setMaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setFemaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setLastNames("[\"Shell\", \"Turtle\", \"Tortle\", \"Coral\", \"Reef\", \"Wave\", \"Tide\", \"Current\", \"Deep\", \"Shore\"]");
        s.setTitlePrefixes("[\"Elder\", \"Warden\", \"Keeper\"]");
        s.setTitleSuffixes("[\"the Patient\", \"the Resilient\", \"the Shell-born\", \"the Ancient\", \"the Steadfast\"]");
        s.setAiLeaderTitles("[\"Elder\", \"Shell Warden\", \"Tortle Chief\", \"Nature Keeper\"]");
        s.setAiGovernorTitles("[\"District Elder\", \"Shell Keeper\", \"Nature Warden\"]");
        s.setAiCaptainTitles("[\"Shell Captain\", \"Warden Commander\", \"Keeper Guard\"]");
        return s;
    }

    private Dnd5eSpecies createTriton() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Triton");
        s.setDescription("Guardians of the sea, tritons are noble and brave, defending the oceans from threats both above and below.");
        s.setCategory("TCE");
        s.setSize("Medium");
        s.setBaseSpeed(20);
        s.setLifespan(150);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"STR\": 1, \"WIS\": 1, \"CHA\": 1}");
        s.setRacialTraits("[\"Amphibious\", \"Superior Darkvision\", \"Hold Breath\", \"Swimmer\", \"Cold Resistance\"]");
        s.setLanguages("Common, Aquan, Primordial");
        s.setMaleFirstNames("[\"Adharon\", \"Arion\", \"Charr\", \"Dall\", \"Erakis\", \"Jhilar\", \"Kerit\", \"Lliir\", \"Marduk\", \"Neril\"]");
        s.setFemaleFirstNames("[\"Adhara\", \"Ariona\", \"Charr\", \"Dalla\", \"Erakis\", \"Jhil\", \"Kerit\", \"Lliir\", \"Marduk\", \"Neril\"]");
        s.setLastNames("[\"Coral\", \"Reef\", \"Wave\", \"Tide\", \"Current\", \"Deep\", \"Shore\", \"Pearl\", \"Shell\", \"Fin\"]");
        s.setTitlePrefixes("[\"Guardian\", \"Prince\", \"Princess\", \"Lord of the Sea\"]");
        s.setTitleSuffixes("[\"the Noble\", \"the Brave\", \"the Sea-born\", \"the Guardian\", \"the Deep\"]");
        s.setAiLeaderTitles("[\"Sea King\", \"Sea Queen\", \"Guardian Lord\", \"Ocean Prince\"]");
        s.setAiGovernorTitles("[\"District Guardian\", \"Sea Elder\", \"Ocean Warden\"]");
        s.setAiCaptainTitles("[\"Guardian Captain\", \"Sea Commander\", \"Ocean Guard\"]");
        return s;
    }

    private Dnd5eSpecies createKenku() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Kenku");
        s.setDescription("Bird-like humanoids who cannot speak but can perfectly mimic any sound they have heard, including voices and music.");
        s.setCategory("TCE");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(70);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"DEX\": 2, \"CHA\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Expertise\", \"Mimicry\", \"Bird's Revenge\"]");
        s.setLanguages("Common, cannot speak");
        s.setMaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setFemaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setLastNames("[\"Wing\", \"Feather\", \"Beak\", \"Claw\", \"Talons\", \"Sky\", \"Air\", \"Wind\", \"Storm\", \"Cloud\"]");
        s.setTitlePrefixes("[\"Mimic\", \"Spy\", \"Scout\", \"Shadow\"]");
        s.setTitleSuffixes("[\"the Silent\", \"the Mimic\", \"the Bird-like\", \"the Shadow\", \"the Whispering\"]");
        s.setAiLeaderTitles("[\"Mimic Lord\", \"Spy Master\", \"Shadow Chief\", \"Silent King\"]");
        s.setAiGovernorTitles("[\"District Mimic\", \"Spy Elder\", \"Shadow Warden\"]");
        s.setAiCaptainTitles("[\"Mimic Captain\", \"Spy Commander\", \"Shadow Guard\"]");
        return s;
    }

    private Dnd5eSpecies createBugbear() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Bugbear");
        s.setDescription("Lanky and mischievous goblinoids, bugbears are cunning ambushers with a love of pranks and traps.");
        s.setCategory("TCE");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(70);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"STR\": 1, \"DEX\": 1, \"CON\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Long-Limbed\", \"Surprise Attack\", \"Stout Resilience\"]");
        s.setLanguages("Common, Goblin");
        s.setMaleFirstNames("[\"Chug\", \"Duggan\", \"Fung\", \"Gash\", \"Hag\", \"Jug\", \"Krug\", \"Lug\", \"Mug\", \"Nug\"]");
        s.setFemaleFirstNames("[\"Chug\", \"Duggan\", \"Fung\", \"Gash\", \"Hag\", \"Jug\", \"Krug\", \"Lug\", \"Mug\", \"Nug\"]");
        s.setLastNames("[\"Goblin\", \"Bugbear\", \"Trap\", \"Prank\", \"Ambush\", \"Shadow\", \"Dirt\", \"Muck\", \"Filth\", \"Grit\"]");
        s.setTitlePrefixes("[\"Chieftain\", \"Trapper\", \"Ambusher\", \"Prankster\"]");
        s.setTitleSuffixes("[\"the Cunning\", \"the Mischievous\", \"the Ambusher\", \"the Trapper\", \"the Lanky\"]");
        s.setAiLeaderTitles("[\"Chieftain\", \"Trap Master\", \"Ambush Lord\", \"Prank King\"]");
        s.setAiGovernorTitles("[\"District Chieftain\", \"Trap Elder\", \"Ambush Warden\"]");
        s.setAiCaptainTitles("[\"Chieftain Captain\", \"Trap Commander\", \"Ambush Guard\"]");
        return s;
    }

    private Dnd5eSpecies createGoliath() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Goliath");
        s.setDescription("Tall and powerful mountain dwellers, goliaths are stoic warriors who value strength and endurance above all.");
        s.setCategory("TCE");
        s.setSize("Large");
        s.setBaseSpeed(30);
        s.setLifespan(80);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"STR\": 2, \"CON\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Stone's Endurance\", \"Powerful Build\", \"Natural Athlete\"]");
        s.setLanguages("Common, Giant");
        s.setMaleFirstNames("[\"Bheth\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\", \"Nar\"]");
        s.setFemaleFirstNames("[\"Bheth\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\", \"Nar\"]");
        s.setLastNames("[\"Mountain\", \"Stone\", \"Rock\", \"Peak\", \"Summit\", \"Cliff\", \"Crag\", \"Boulder\", \"Granite\", \"Marble\"]");
        s.setTitlePrefixes("[\"Champion\", \"Warrior\", \"Mountain Lord\", \"Stoic\"]");
        s.setTitleSuffixes("[\"the Strong\", \"the Enduring\", \"the Mountain-born\", \"the Stoic\", \"the Powerful\"]");
        s.setAiLeaderTitles("[\"Mountain King\", \"Champion Lord\", \"Warrior Chief\", \"Stoic Elder\"]");
        s.setAiGovernorTitles("[\"District Champion\", \"Mountain Elder\", \"Warrior Warden\"]");
        s.setAiCaptainTitles("[\"Champion Captain\", \"Warrior Commander\", \"Mountain Guard\"]");
        return s;
    }

    private Dnd5eSpecies createHobgoblin() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Hobgoblin");
        s.setDescription("Disciplined and militaristic goblinoids, hobgoblins are master tacticians who value honor and order.");
        s.setCategory("TCE");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(90);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"CON\": 2, \"STR\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Martial Advantage\", \"Saving Throw Kanat\"]");
        s.setLanguages("Common, Goblin, Giant");
        s.setMaleFirstNames("[\"Chug\", \"Duggan\", \"Fung\", \"Gash\", \"Hag\", \"Jug\", \"Krug\", \"Lug\", \"Mug\", \"Nug\"]");
        s.setFemaleFirstNames("[\"Chug\", \"Duggan\", \"Fung\", \"Gash\", \"Hag\", \"Jug\", \"Krug\", \"Lug\", \"Mug\", \"Nug\"]");
        s.setLastNames("[\"Goblin\", \"Hobgoblin\", \"War\", \"Battle\", \"Sword\", \"Shield\", \"Armor\", \"Steel\", \"Iron\", \"Blood\"]");
        s.setTitlePrefixes("[\"General\", \"Commander\", \"Centurion\", \"Tactician\"]");
        s.setTitleSuffixes("[\"the Disciplined\", \"the Militaristic\", \"the Honorable\", \"the Tactician\", \"the Ordered\"]");
        s.setAiLeaderTitles("[\"General\", \"High Commander\", \"War Lord\", \"Tactician Supreme\"]");
        s.setAiGovernorTitles("[\"District General\", \"Military Elder\", \"War Warden\"]");
        s.setAiCaptainTitles("[\"Centurion\", \"Legion Commander\", \"War Guard\"]");
        return s;
    }

    private Dnd5eSpecies createLizardfolk() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Lizardfolk");
        s.setDescription("Reptilian humanoids who live by the law of the jungle, lizardfolk are pragmatic survivors with a deep connection to nature.");
        s.setCategory("TCE");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(70);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"WIS\": 2, \"CON\": 1}");
        s.setRacialTraits("[\"Cold-Blooded\", \"Natural Armor\", \"Pack Tactics\", \"Still Hunt\"]");
        s.setLanguages("Common, Draconic");
        s.setMaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setFemaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setLastNames("[\"Scale\", \"Tail\", \"Claw\", \"Fang\", \"Jaw\", \"Swamp\", \"Marsh\", \"Reed\", \"Mire\", \"Bog\"]");
        s.setTitlePrefixes("[\"Elder\", \"Hunter\", \"Swamp Lord\", \"Survivor\"]");
        s.setTitleSuffixes("[\"the Pragmatic\", \"the Survivor\", \"the Swamp-born\", \"the Reptilian\", \"the Lawful\"]");
        s.setAiLeaderTitles("[\"Swamp King\", \"Elder Lord\", \"Hunter Chief\", \"Survivor Supreme\"]");
        s.setAiGovernorTitles("[\"District Elder\", \"Swamp Warden\", \"Hunter Elder\"]");
        s.setAiCaptainTitles("[\"Elder Captain\", \"Hunter Commander\", \"Swamp Guard\"]");
        return s;
    }

    private Dnd5eSpecies createSatyr() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Satyr");
        s.setDescription("Merry and mischievous fey creatures, satyrs love music, dance, and revelry, with a deep connection to the Feywild.");
        s.setCategory("TCE");
        s.setSize("Medium");
        s.setBaseSpeed(30);
        s.setLifespan(70);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"CHA\": 2, \"DEX\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Fey Ancestry\", \"Nimble Escape\", \"Satyr's Song\"]");
        s.setLanguages("Common, Sylvan");
        s.setMaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setFemaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setLastNames("[\"Horn\", \"Hoof\", \"Tail\", \"Reed\", \"Flute\", \"Dance\", \"Song\", \"Music\", \"Merry\", \"Fey\"]");
        s.setTitlePrefixes("[\"Merry\", \"Dancer\", \"Musician\", \"Fey\"]");
        s.setTitleSuffixes("[\"the Merry\", \"the Mischievous\", \"the Fey-touched\", \"the Musical\", \"the Dancing\"]");
        s.setAiLeaderTitles("[\"Merry King\", \"Dance Lord\", \"Music Chief\", \"Fey Elder\"]");
        s.setAiGovernorTitles("[\"District Merry\", \"Dance Elder\", \"Music Warden\"]");
        s.setAiCaptainTitles("[\"Merry Captain\", \"Dance Commander\", \"Music Guard\"]");
        return s;
    }

    private Dnd5eSpecies createFirbolg() {
        Dnd5eSpecies s = new Dnd5eSpecies();
        s.setName("Firbolg");
        s.setDescription("Gentle giants of the forest, firbolgs are peaceful guardians of nature with a deep connection to the Feywild.");
        s.setCategory("TCE");
        s.setSize("Large");
        s.setBaseSpeed(30);
        s.setLifespan(500);
        s.setDarkvisionRange(60);
        s.setAbilityIncreases("{\"WIS\": 2, \"CON\": 1}");
        s.setRacialTraits("[\"Darkvision\", \"Powerful Build\", \"Hidden Magic\", \"Firbolg Magic\"]");
        s.setLanguages("Common, Sylvan");
        s.setMaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setFemaleFirstNames("[\"Bhegl\", \"Chis\", \"Dhara\", \"Fen\", \"Ghar\", \"Hrak\", \"Jas\", \"Keth\", \"Lir\", \"Mok\"]");
        s.setLastNames("[\"Forest\", \"Tree\", \"Wood\", \"Leaf\", \"Root\", \"Branch\", \"Bark\", \"Moss\", \"Fern\", \"Flower\"]");
        s.setTitlePrefixes("[\"Guardian\", \"Elder\", \"Forest Lord\", \"Gentle\"]");
        s.setTitleSuffixes("[\"the Gentle\", \"the Peaceful\", \"the Forest-born\", \"the Guardian\", \"the Ancient\"]");
        s.setAiLeaderTitles("[\"Forest King\", \"Guardian Lord\", \"Elder Chief\", \"Nature Supreme\"]");
        s.setAiGovernorTitles("[\"District Guardian\", \"Forest Elder\", \"Nature Warden\"]");
        s.setAiCaptainTitles("[\"Guardian Captain\", \"Forest Commander\", \"Nature Guard\"]");
        return s;
    }

    // --- Utility ---

    private List<String> parseJsonArray(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON array: {}", json, e);
            return Collections.emptyList();
        }
    }
}

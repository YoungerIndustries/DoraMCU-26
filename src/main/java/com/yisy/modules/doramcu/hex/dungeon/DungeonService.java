package com.yisy.modules.doramcu.hex.dungeon;

import com.yisy.modules.doramcu.hex.Compartment;
import com.yisy.modules.doramcu.hex.CompartmentType;
import com.yisy.modules.doramcu.hex.HullModule;
import com.yisy.modules.doramcu.hex.M3space;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for managing dungeon module templates and dungeon assembly.
 * <p>
 * Provides CRUD operations for module templates, template browsing and filtering,
 * and dungeon creation from templates. Also provides pre-population of starter
 * module templates for immediate use.
 * </p>
 */
@Service
public class DungeonService {

    private final DungeonModuleTemplateRepository templateRepository;
    private final DungeonBuilder builder;

    /**
     * Creates a new DungeonService.
     *
     * @param templateRepository the JPA repository for module templates
     */
    public DungeonService(DungeonModuleTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
        this.builder = new DungeonBuilder();
    }

    // ---- Module Template CRUD ----

    /**
     * Creates and saves a new module template.
     *
     * @param template the template to save
     * @return the saved template
     */
    public DungeonModuleTemplate createModuleTemplate(DungeonModuleTemplate template) {
        return templateRepository.save(template);
    }

    /**
     * Updates an existing module template.
     *
     * @param template the template with updated data
     * @return the updated template
     */
    public DungeonModuleTemplate updateModuleTemplate(DungeonModuleTemplate template) {
        if (!templateRepository.existsById(template.getId())) {
            throw new IllegalArgumentException("Template not found: " + template.getId());
        }
        return templateRepository.save(template);
    }

    /**
     * Deletes a module template by ID.
     *
     * @param id the template ID
     */
    public void deleteModuleTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    /**
     * @return all module templates
     */
    public List<DungeonModuleTemplate> getModuleTemplates() {
        return templateRepository.findAll();
    }

    /**
     * @param id the template ID
     * @return the template, or null if not found
     */
    public Optional<DungeonModuleTemplate> getModuleTemplate(Long id) {
        return templateRepository.findById(id);
    }

    /**
     * Finds templates by dungeon room type.
     *
     * @param type the room type
     * @return list of matching templates
     */
    public List<DungeonModuleTemplate> getModulesByType(DungeonRoom type) {
        return templateRepository.findByType(type);
    }

    /**
     * Finds templates by tag.
     *
     * @param tag the tag to search for
     * @return list of matching templates
     */
    public List<DungeonModuleTemplate> getModulesByTag(String tag) {
        return templateRepository.findByTagContaining(tag);
    }

    /**
     * Finds templates by difficulty range.
     *
     * @param maxDifficulty the maximum difficulty
     * @return list of matching templates
     */
    public List<DungeonModuleTemplate> getModulesByDifficulty(int maxDifficulty) {
        return templateRepository.findByDifficultyLessThanEqual(maxDifficulty);
    }

    /**
     * Finds templates ordered by difficulty.
     *
     * @return all templates sorted by difficulty ascending
     */
    public List<DungeonModuleTemplate> getModulesOrderedByDifficulty() {
        return templateRepository.findAllOrderByDifficultyAsc();
    }

    // ---- Dungeon Assembly ----

    /**
     * Creates a dungeon from templates and connection definitions.
     *
     * @param name the dungeon name
     * @param description the dungeon description
     * @param templateIds list of template IDs to use
     * @param connections list of connection definitions
     * @return the assembled dungeon
     */
    public Dungeon createDungeon(String name, String description,
                                  List<Long> templateIds,
                                  List<DungeonBuilder.ConnectionDefinition> connections) {
        List<DungeonModuleTemplate> templates = templateRepository.findAllById(templateIds);
        if (templates.size() != templateIds.size()) {
            throw new IllegalArgumentException("One or more templates not found");
        }
        return builder.assemble(name, description, templates, connections);
    }

    /**
     * Creates a single-room dungeon from a template.
     *
     * @param name the dungeon name
     * @param description the dungeon description
     * @param templateId the template ID
     * @return the assembled dungeon
     */
    public Dungeon createDungeonFromTemplate(String name, String description, Long templateId) {
        DungeonModuleTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateId));
        Dungeon dungeon = new Dungeon(name, description);
        HullModule module = builder.buildFromTemplate(template);
        dungeon.addRoom("room_1", module);
        return dungeon;
    }

    // ---- Pre-population ----

    /**
     * Pre-populates the database with basic starter module templates.
     * <p>
     * Creates templates for: standard corridor, intersection, treasure room,
     * trap room, and boss chamber. These are only created if no templates exist yet.
     * </p>
     *
     * @return number of templates created
     */
    public int prepopulateTemplates() {
        if (templateRepository.count() > 0) {
            return 0;  // Already populated
        }

        List<DungeonModuleTemplate> templates = new ArrayList<>();

        // Standard Corridor: 10x3x1 (length x width x height)
        templates.add(createCorridorTemplate());

        // Intersection: 5x5x1 cross-shaped room
        templates.add(createIntersectionTemplate());

        // Treasure Room: 7x7x3
        templates.add(createTreasureRoomTemplate());

        // Trap Room: 5x5x3
        templates.add(createTrapRoomTemplate());

        // Boss Chamber: 10x10x4
        templates.add(createBossChamberTemplate());

        // Prison Cells: 8x6x3
        templates.add(createPrisonTemplate());

        // Library: 8x8x3
        templates.add(createLibraryTemplate());

        templateRepository.saveAll(templates);
        return templates.size();
    }

    private DungeonModuleTemplate createCorridorTemplate() {
        DungeonModuleTemplate template = new DungeonModuleTemplate();
        template.setName("Standard Corridor");
        template.setDescription("A narrow passage connecting rooms, 10 cells long and 3 cells wide.");
        template.setType(DungeonRoom.CORRIDOR);
        template.setWidth(10);
        template.setHeight(3);
        template.setDepth(1);
        template.setDifficulty(1);
        template.setTags("passage,connection,narrow");

        // Shape: full rectangle
        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 3; y++) {
                cells.add(new M3space(x, y, 0));
            }
        }
        template.setShapeCellsJson(DungeonModuleTemplate.serializeCells(cells));

        // Single compartment: the corridor itself
        template.setCompartmentsJson(
                "[{\"name\":\"Corridor\",\"type\":\"CORRIDOR\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );

        // Connection points at both ends
        template.setConnectionPointsJson(
                "[{\"x\":0,\"y\":1,\"z\":0,\"direction\":\"WEST\"},{\"x\":9,\"y\":1,\"z\":0,\"direction\":\"EAST\"}]"
        );

        return template;
    }

    private DungeonModuleTemplate createIntersectionTemplate() {
        DungeonModuleTemplate template = new DungeonModuleTemplate();
        template.setName("Cross Intersection");
        template.setDescription("A cross-shaped junction where four corridors meet.");
        template.setType(DungeonRoom.INTERSECTION);
        template.setWidth(5);
        template.setHeight(5);
        template.setDepth(1);
        template.setDifficulty(1);
        template.setTags("junction,crossroads,hub");

        // Cross shape: center row and center column
        Set<M3space> cells = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            cells.add(new M3space(i, 2, 0));  // Horizontal
            cells.add(new M3space(2, i, 0));  // Vertical
        }
        template.setShapeCellsJson(DungeonModuleTemplate.serializeCells(cells));

        template.setCompartmentsJson(
                "[{\"name\":\"Intersection\",\"type\":\"INTERSECTION\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );

        // Connection points at all four directions
        template.setConnectionPointsJson(
                "[{\"x\":0,\"y\":2,\"z\":0,\"direction\":\"WEST\"}," +
                "{\"x\":4,\"y\":2,\"z\":0,\"direction\":\"EAST\"}," +
                "{\"x\":2,\"y\":0,\"z\":0,\"direction\":\"NORTH\"}," +
                "{\"x\":2,\"y\":4,\"z\":0,\"direction\":\"SOUTH\"}]"
        );

        return template;
    }

    private DungeonModuleTemplate createTreasureRoomTemplate() {
        DungeonModuleTemplate template = new DungeonModuleTemplate();
        template.setName("Treasure Vault");
        template.setDescription("A large room filled with treasure chests and valuable artifacts.");
        template.setType(DungeonRoom.TREASURE_VAULT);
        template.setWidth(7);
        template.setHeight(7);
        template.setDepth(3);
        template.setDifficulty(5);
        template.setTags("treasure,loot,riches,guarded");

        // Full rectangular room
        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 7; x++) {
            for (int y = 0; y < 7; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(DungeonModuleTemplate.serializeCells(cells));

        template.setCompartmentsJson(
                "[{\"name\":\"Treasure Vault\",\"type\":\"TREASURE_VAULT\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );

        // Single entrance
        template.setConnectionPointsJson(
                "[{\"x\":3,\"y\":0,\"z\":0,\"direction\":\"SOUTH\"}]"
        );

        return template;
    }

    private DungeonModuleTemplate createTrapRoomTemplate() {
        DungeonModuleTemplate template = new DungeonModuleTemplate();
        template.setName("Trap Room");
        template.setDescription("A hazardous room filled with traps and deadly mechanisms.");
        template.setType(DungeonRoom.TRAP_ROOM);
        template.setWidth(5);
        template.setHeight(5);
        template.setDepth(3);
        template.setDifficulty(7);
        template.setTags("danger,hazard,puzzle,deadly");

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(DungeonModuleTemplate.serializeCells(cells));

        template.setCompartmentsJson(
                "[{\"name\":\"Trap Room\",\"type\":\"TRAP_ROOM\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );

        template.setConnectionPointsJson(
                "[{\"x\":2,\"y\":0,\"z\":0,\"direction\":\"SOUTH\"},{\"x\":2,\"y\":4,\"z\":0,\"direction\":\"NORTH\"}]"
        );

        return template;
    }

    private DungeonModuleTemplate createBossChamberTemplate() {
        DungeonModuleTemplate template = new DungeonModuleTemplate();
        template.setName("Boss Chamber");
        template.setDescription("A grand chamber designed for a major boss encounter.");
        template.setType(DungeonRoom.BOSS_CHAMBER);
        template.setWidth(10);
        template.setHeight(10);
        template.setDepth(4);
        template.setDifficulty(10);
        template.setTags("encounter,final,large,grand");

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 10; y++) {
                for (int z = 0; z < 4; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(DungeonModuleTemplate.serializeCells(cells));

        template.setCompartmentsJson(
                "[{\"name\":\"Boss Chamber\",\"type\":\"BOSS_CHAMBER\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );

        // Single grand entrance
        template.setConnectionPointsJson(
                "[{\"x\":5,\"y\":0,\"z\":0,\"direction\":\"SOUTH\"}]"
        );

        return template;
    }

    private DungeonModuleTemplate createPrisonTemplate() {
        DungeonModuleTemplate template = new DungeonModuleTemplate();
        template.setName("Prison Cells");
        template.setDescription("A row of prison cells with a guard walkway.");
        template.setType(DungeonRoom.PRISON);
        template.setWidth(8);
        template.setHeight(6);
        template.setDepth(3);
        template.setDifficulty(3);
        template.setTags("cells,holding,captivity,guards");

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 6; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(DungeonModuleTemplate.serializeCells(cells));

        template.setCompartmentsJson(
                "[{\"name\":\"Prison\",\"type\":\"PRISON\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );

        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":0,\"direction\":\"SOUTH\"}]"
        );

        return template;
    }

    private DungeonModuleTemplate createLibraryTemplate() {
        DungeonModuleTemplate template = new DungeonModuleTemplate();
        template.setName("Ancient Library");
        template.setDescription("A vast library filled with ancient tomes and forbidden knowledge.");
        template.setType(DungeonRoom.LIBRARY);
        template.setWidth(8);
        template.setHeight(8);
        template.setDepth(3);
        template.setDifficulty(4);
        template.setTags("books,knowledge,scrolls,ancient");

        Set<M3space> cells = new HashSet<>();
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 3; z++) {
                    cells.add(new M3space(x, y, z));
                }
            }
        }
        template.setShapeCellsJson(DungeonModuleTemplate.serializeCells(cells));

        template.setCompartmentsJson(
                "[{\"name\":\"Library\",\"type\":\"LIBRARY\",\"cells\":[" +
                String.join(",", cells.stream().map(c -> "\"" + c.getX() + "," + c.getY() + "," + c.getZ() + "\"").toArray(String[]::new)) +
                "]}"
        );

        template.setConnectionPointsJson(
                "[{\"x\":4,\"y\":0,\"z\":0,\"direction\":\"SOUTH\"},{\"x\":4,\"y\":7,\"z\":0,\"direction\":\"NORTH\"}]"
        );

        return template;
    }
}

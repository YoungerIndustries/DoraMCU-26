package com.yisy.modules.doramcu.lord_of_the_rings.service;

import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacter;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCharacterToolProficiency;
import com.yisy.modules.doramcu.lord_of_the_rings.model.LotrCraftingTool;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCharacterRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCharacterToolProficiencyRepository;
import com.yisy.modules.doramcu.lord_of_the_rings.repository.LotrCraftingToolRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service for managing LOTR Tool Proficiencies.
 * <p>
 * Tool proficiencies represent a character's skill with specialized tools
 * used in the Lord of the Rings world: smithing tools, herbalism kits,
 * navigation tools, musical instruments, and more.
 * </p>
 * <p>
 * LOTR-specific tools include:
 * - Elven Craftsmanship Kit (woodworking, jewelry)
 * - Dwarven Smithing Tools (forging, metalwork)
 * - Ranger's Wayfinder (navigation through wilds)
 * - Herbalism Kit (healing poultices, antidotes)
 * - Lorekeeper's Quill (scholarly research)
 * - Entish Drum (communication with Ents)
 * - Hobbit Pipe-weed Kit (tobacco cultivation)
 * - Gondorian Navigator's Astrolabe (sea navigation)
 * </p>
 */
@Service
public class LotrToolProficiencyService {

    private static final Logger log = LoggerFactory.getLogger(LotrToolProficiencyService.class);
    private static final Random random = new Random();

    private final LotrCharacterToolProficiencyRepository toolProficiencyRepository;
    private final LotrCraftingToolRepository craftingToolRepository;
    private final LotrCharacterRepository characterRepository;

    public LotrToolProficiencyService(LotrCharacterToolProficiencyRepository toolProficiencyRepository,
                                       LotrCraftingToolRepository craftingToolRepository,
                                       LotrCharacterRepository characterRepository) {
        this.toolProficiencyRepository = toolProficiencyRepository;
        this.craftingToolRepository = craftingToolRepository;
        this.characterRepository = characterRepository;
    }

    // ==========================================
    // Tool CRUD
    // ==========================================

    /**
     * Get all crafting tools.
     */
    @Transactional(readOnly = true)
    public List<LotrCraftingTool> findAllTools() {
        return craftingToolRepository.findAll();
    }

    /**
     * Get active crafting tools.
     */
    @Transactional(readOnly = true)
    public List<LotrCraftingTool> findActiveTools() {
        return craftingToolRepository.findAll().stream()
                .filter(LotrCraftingTool::isActive)
                .toList();
    }

    /**
     * Get crafting tool by ID.
     */
    @Transactional(readOnly = true)
    public LotrCraftingTool findToolById(Long id) {
        return craftingToolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Crafting tool not found: " + id));
    }

    /**
     * Get tools by category.
     */
    @Transactional(readOnly = true)
    public List<LotrCraftingTool> findToolsByCategory(String category) {
        return craftingToolRepository.findAll().stream()
                .filter(t -> category.equalsIgnoreCase(t.getCategory()))
                .toList();
    }

    /**
     * Create a new crafting tool.
     */
    @Transactional
    public LotrCraftingTool createTool(String name, String description, String category, int proficiencyBonus) {
        LotrCraftingTool tool = new LotrCraftingTool();
        tool.setName(name);
        tool.setDescription(description);
        tool.setCategory(category);
        tool.setProficiencyBonus(proficiencyBonus);
        tool.setActive(true);

        LotrCraftingTool saved = craftingToolRepository.save(tool);
        log.info("Created crafting tool: {}", name);
        return saved;
    }

    /**
     * Update a crafting tool.
     */
    @Transactional
    public LotrCraftingTool updateTool(Long id, String name, String description, String category, int proficiencyBonus) {
        LotrCraftingTool tool = craftingToolRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Crafting tool not found: " + id));

        tool.setName(name);
        tool.setDescription(description);
        tool.setCategory(category);
        tool.setProficiencyBonus(proficiencyBonus);

        LotrCraftingTool saved = craftingToolRepository.save(tool);
        log.info("Updated crafting tool: {}", id);
        return saved;
    }

    /**
     * Delete a crafting tool.
     */
    @Transactional
    public void deleteTool(Long id) {
        if (!craftingToolRepository.existsById(id)) {
            throw new IllegalArgumentException("Crafting tool not found: " + id);
        }
        craftingToolRepository.deleteById(id);
        log.info("Deleted crafting tool: {}", id);
    }

    // ==========================================
    // Character Tool Proficiency
    // ==========================================

    /**
     * Get all tool proficiencies for a character.
     */
    @Transactional(readOnly = true)
    public List<LotrCharacterToolProficiency> getCharacterToolProficiencies(Long characterId) {
        return toolProficiencyRepository.findAll().stream()
                .filter(tp -> tp.getCharacterId().equals(characterId))
                .toList();
    }

    /**
     * Check if a character is proficient with a tool.
     */
    @Transactional(readOnly = true)
    public boolean isProficient(Long characterId, Long toolId) {
        return toolProficiencyRepository.findAll().stream()
                .anyMatch(tp -> tp.getCharacterId().equals(characterId) && tp.getToolId().equals(toolId));
    }

    /**
     * Add a tool proficiency to a character.
     */
    @Transactional
    public LotrCharacterToolProficiency addProficiency(Long characterId, Long toolId) {
        if (!characterRepository.existsById(characterId)) {
            throw new IllegalArgumentException("Character not found: " + characterId);
        }
        if (!craftingToolRepository.existsById(toolId)) {
            throw new IllegalArgumentException("Crafting tool not found: " + toolId);
        }

        if (isProficient(characterId, toolId)) {
            throw new IllegalArgumentException("Character already proficient with this tool");
        }

        LotrCharacterToolProficiency tp = new LotrCharacterToolProficiency();
        tp.setCharacterId(characterId);
        tp.setToolId(toolId);

        LotrCharacterToolProficiency saved = toolProficiencyRepository.save(tp);
        log.info("Added tool proficiency: character {} -> tool {}", characterId, toolId);
        return saved;
    }

    /**
     * Remove a tool proficiency from a character.
     */
    @Transactional
    public void removeProficiency(Long id) {
        if (!toolProficiencyRepository.existsById(id)) {
            throw new IllegalArgumentException("Tool proficiency not found: " + id);
        }
        toolProficiencyRepository.deleteById(id);
        log.info("Removed tool proficiency: {}", id);
    }

    // ==========================================
    // Tool Checks
    // ==========================================

    /**
     * Make a tool proficiency check for a character.
     * <p>
     * Uses d20 + ability modifier + proficiency bonus (if proficient).
     * The ability used depends on the tool category:
     * - Smithing/Forging: STR
     * - Herbalism/Medicine: WIS
     * - Navigation/Lore: INT
     * - Musical/Performance: CHA
     * - Other: DEX
     * </p>
     */
    @Transactional(readOnly = true)
    public ToolCheckResult makeToolCheck(Long characterId, Long toolId, int dc) {
        LotrCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        LotrCraftingTool tool = craftingToolRepository.findById(toolId)
                .orElseThrow(() -> new IllegalArgumentException("Crafting tool not found: " + toolId));

        boolean proficient = isProficient(characterId, toolId);

        // Determine ability based on tool category
        String category = tool.getCategory() != null ? tool.getCategory().toLowerCase() : "";
        int abilityMod;
        String abilityName;

        if (category.contains("smith") || category.contains("forge") || category.contains("mace")) {
            abilityMod = (character.getStrScore() - 10) / 2;
            abilityName = "STR";
        } else if (category.contains("herb") || category.contains("medicine") || category.contains("heal")) {
            abilityMod = (character.getWisScore() - 10) / 2;
            abilityName = "WIS";
        } else if (category.contains("navig") || category.contains("lore") || category.contains("scholar")) {
            abilityMod = (character.getIntScore() - 10) / 2;
            abilityName = "INT";
        } else if (category.contains("music") || category.contains("perform") || category.contains("pipe")) {
            abilityMod = (character.getChaScore() - 10) / 2;
            abilityName = "CHA";
        } else {
            abilityMod = (character.getDexScore() - 10) / 2;
            abilityName = "DEX";
        }

        int profBonus = proficient ? character.getProficiencyBonus() : 0;
        int roll = random.nextInt(20) + 1;
        int total = roll + abilityMod + profBonus;
        boolean success = total >= dc;

        // Natural 20 is automatic success, natural 1 is automatic failure
        if (roll == 20) success = true;
        if (roll == 1) success = false;

        String toolName = tool.getName();
        String combatUse = getCombatUse(category);
        String explorationUse = getExplorationUse(category);
        String useAction = getUseAction(category);

        return new ToolCheckResult(
                roll, abilityMod, profBonus, abilityMod + profBonus, total, dc, success,
                character.getName(), toolName, abilityName, combatUse, explorationUse, useAction
        );
    }

    // ==========================================
    // Initialization
    // ==========================================

    /**
     * Initialize the tool database with LOTR-themed tools.
     */
    @Transactional
    public void initializeToolDatabase() {
        if (craftingToolRepository.count() > 0) {
            log.info("Crafting tools already initialized, skipping.");
            return;
        }

        // Smithing and Forging
        createTool("Dwarven Smithing Tools", "Masterwork forging tools of Khazad-dûm, capable of shaping mithril and star-metal.",
                "Smithing", 2);
        createTool("Elven Craftsmanship Kit", "Exquisite woodworking and jewelry tools from Rivendell.",
                "Crafting", 2);
        createTool("Gondorian Mason's Tools", "Stone-cutting and masonry tools used in building the White City.",
                "Masonry", 1);

        // Herbalism and Medicine
        createTool("Herbalism Kit", "A kit for gathering and preparing healing poultices, antidotes, and remedies from the flora of Middle-earth.",
                "Herbalism", 2);
        createTool("Entish Lore Kit", "Ancient knowledge of forest plants and the slow wisdom of the Ents.",
                "Herbalism", 1);
        createTool("Elven Healing Salve Kit", "Precious unguents and salves crafted by the healers of Lothlórien.",
                "Medicine", 2);

        // Navigation and Lore
        createTool("Ranger's Wayfinder", "A compass and map-making kit used by the Rangers of the North to navigate wild and dangerous lands.",
                "Navigation", 2);
        createTool("Gondorian Navigator's Astrolabe", "An ancient instrument for navigating the seas of Middle-earth, passed down from Númenor.",
                "Navigation", 2);
        createTool("Lorekeeper's Quill", "Scholarly tools for researching ancient texts, runes, and the histories of Middle-earth.",
                "Lore", 2);
        createTool("Palantír Reading Kit", "Specialized tools for interpreting the visions of the Seeing Stones (used with extreme caution).",
                "Lore", 1);

        // Musical and Performance
        createTool("Elven Harp", "A beautifully crafted harp that can soothe the soul and drive back the Shadow.",
                "Musical", 2);
        createTool("Hobbit Pipe-weed Kit", "Tools for cultivating, curing, and blending the finest pipe-weed of the Southfarthing.",
                "Pipe-weed", 1);
        createTool("Dwarven Drum", "A deep-resonating drum used in halls and tunnels to communicate over long distances.",
                "Musical", 1);
        createTool("Entish Drum", "A massive drum used to communicate with other Ents across vast distances. Only the slow and patient can use it.",
                "Musical", 1);

        // Specialized
        createTool("Thief's Tools", "Lockpicks, mirrors, and small knives for picking pockets and disarming traps.",
                "Thieves", 2);
        createTool("Disguise Kit", "Masks, wigs, and costumes for assuming different identities in Middle-earth.",
                "Disguise", 1);
        createTool("Calligraphy Set", "Fine pens and inks for writing in Elvish scripts (Tengwar and Cirth).",
                "Lore", 1);

        log.info("Initialized {} LOTR crafting tools.", craftingToolRepository.count());
    }

    // ==========================================
    // Helper Methods
    // ==========================================

    private String getCombatUse(String category) {
        return switch (category.toLowerCase()) {
            case "smithing" -> "Can be used as an improvised weapon (1d4 bludgeoning damage).";
            case "masonry" -> "Can reinforce fortifications as a bonus action, granting +1 AC to allies within 10 feet.";
            case "thieves" -> "Can quickly disarm simple traps or unlock doors during combat.";
            default -> "No direct combat use, but can provide tactical advantages in exploration.";
        };
    }

    private String getExplorationUse(String category) {
        return switch (category.toLowerCase()) {
            case "navigation" -> "Navigate through unfamiliar terrain, avoiding hazards and finding safe paths.";
            case "herbalism", "medicine" -> "Identify and gather healing herbs, create antidotes, and treat wounds.";
            case "lore", "scholar" -> "Research ancient texts, decipher runes, and uncover hidden knowledge.";
            case "musical" -> "Boost morale, communicate over distances, or soothe hostile creatures.";
            case "smithing", "crafting" -> "Repair equipment, craft simple tools, or forge weapons from scrap.";
            default -> "Assist in general exploration tasks with a +1 bonus to related checks.";
        };
    }

    private String getUseAction(String category) {
        return switch (category.toLowerCase()) {
            case "navigation" -> "As a bonus action, consult your wayfinder to determine direction and estimate travel time.";
            case "herbalism" -> "As an action, prepare a healing poultice that restores 1d4 HP when applied.";
            case "lore" -> "As an action, consult your notes to recall relevant knowledge about the area.";
            case "musical" -> "As an action, play a melody that grants advantage on the next morale check.";
            case "pipe-weed" -> "As a bonus action, share pipe-weed to grant allies a moment of respite (advantage on next WIS save).";
            default -> "As a bonus action, use the tool to gain advantage on the next related check.";
        };
    }

    /**
     * Result of a tool proficiency check.
     */
    public static class ToolCheckResult {
        private final int roll;
        private final int abilityModifier;
        private final int proficiencyBonus;
        private final int totalModifier;
        private final int total;
        private final int dc;
        private final boolean success;
        private final String characterName;
        private final String toolName;
        private final String associatedAbility;
        private final String combatUse;
        private final String explorationUse;
        private final String useAction;

        public ToolCheckResult(int roll, int abilityModifier, int proficiencyBonus, int totalModifier,
                                int total, int dc, boolean success, String characterName, String toolName,
                                String associatedAbility, String combatUse, String explorationUse, String useAction) {
            this.roll = roll;
            this.abilityModifier = abilityModifier;
            this.proficiencyBonus = proficiencyBonus;
            this.totalModifier = totalModifier;
            this.total = total;
            this.dc = dc;
            this.success = success;
            this.characterName = characterName;
            this.toolName = toolName;
            this.associatedAbility = associatedAbility;
            this.combatUse = combatUse;
            this.explorationUse = explorationUse;
            this.useAction = useAction;
        }

        public int getRoll() { return roll; }
        public int getAbilityModifier() { return abilityModifier; }
        public int getProficiencyBonus() { return proficiencyBonus; }
        public int getTotalModifier() { return totalModifier; }
        public int getTotal() { return total; }
        public int getDc() { return dc; }
        public boolean isSuccess() { return success; }
        public String getCharacterName() { return characterName; }
        public String getToolName() { return toolName; }
        public String getAssociatedAbility() { return associatedAbility; }
        public String getCombatUse() { return combatUse; }
        public String getExplorationUse() { return explorationUse; }
        public String getUseAction() { return useAction; }
    }
}

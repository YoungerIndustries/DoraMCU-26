package com.yisy.modules.doramcu.dnd5e.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCampaignTemplate;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eCampaignTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing D&D 5e Campaign Theme Templates.
 * <p>
 * Provides CRUD operations for campaign templates, template instantiation
 * (creating campaigns from templates), and auto-population of 6 themed
 * templates covering all major campaign styles.
 * </p>
 * <p>
 * Templates provide DMs with pre-built frameworks that include:
 * - Recommended subsystems for the theme
 * - Step-by-step setup instructions
 * - Session-by-session pacing guides
 * - Example hooks, villains, NPCs, and encounters
 * - Difficulty scaling advice
 * - DM tips and tricks
 * </p>
 */
@Service
public class Dnd5eCampaignTemplateService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eCampaignTemplateService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final Dnd5eCampaignTemplateRepository templateRepository;

    public Dnd5eCampaignTemplateService(Dnd5eCampaignTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    // --- CRUD ---

    public Dnd5eCampaignTemplate createTemplate(Dnd5eCampaignTemplate template) {
        return templateRepository.save(template);
    }

    public Optional<Dnd5eCampaignTemplate> getTemplate(Long id) {
        return templateRepository.findById(id);
    }

    public Optional<Dnd5eCampaignTemplate> getTemplateByName(String name) {
        return templateRepository.findByName(name);
    }

    public List<Dnd5eCampaignTemplate> listTemplates() {
        return templateRepository.findAll();
    }

    public List<Dnd5eCampaignTemplate> listActiveTemplates() {
        return templateRepository.findByActiveTrue();
    }

    public List<Dnd5eCampaignTemplate> listTemplatesByTheme(String theme) {
        return templateRepository.findByThemeAndActiveTrue(theme);
    }

    public List<Dnd5eCampaignTemplate> listTemplatesByPartyLevel(int partyLevel) {
        return templateRepository.findByPartyLevel(partyLevel);
    }

    public List<Dnd5eCampaignTemplate> listTemplatesByLevelRange(int minLevel, int maxLevel) {
        return templateRepository.findByLevelRange(minLevel, maxLevel);
    }

    public List<Dnd5eCampaignTemplate> searchTemplates(String keyword) {
        return templateRepository.searchByKeyword(keyword);
    }

    public List<String> listAllThemes() {
        return templateRepository.findAllThemes();
    }

    public Dnd5eCampaignTemplate updateTemplate(Long id, Dnd5eCampaignTemplate updated) {
        Dnd5eCampaignTemplate existing = templateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Campaign template not found: " + id));
        existing.setName(updated.getName());
        existing.setTagline(updated.getTagline());
        existing.setDescription(updated.getDescription());
        existing.setTheme(updated.getTheme());
        existing.setMinLevel(updated.getMinLevel());
        existing.setMaxLevel(updated.getMaxLevel());
        existing.setRecommendedPartySize(updated.getRecommendedPartySize());
        existing.setEstimatedSessions(updated.getEstimatedSessions());
        existing.setRecommendedSubsystems(updated.getRecommendedSubsystems());
        existing.setSetupInstructions(updated.getSetupInstructions());
        existing.setPacingGuide(updated.getPacingGuide());
        existing.setExampleHook(updated.getExampleHook());
        existing.setExampleVillain(updated.getExampleVillain());
        existing.setExampleNpcs(updated.getExampleNpcs());
        existing.setExampleEncounters(updated.getExampleEncounters());
        existing.setDifficultyScaling(updated.getDifficultyScaling());
        existing.setDmTips(updated.getDmTips());
        existing.setActive(updated.isActive());
        return templateRepository.save(existing);
    }

    public void deleteTemplate(Long id) {
        templateRepository.deleteById(id);
    }

    public void toggleTemplateActive(Long id) {
        Dnd5eCampaignTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Campaign template not found: " + id));
        template.setActive(!template.isActive());
        templateRepository.save(template);
    }

    // --- Template Instantiation ---

    /**
     * Get a summary of what a template provides for campaign creation.
     * This helps DMs understand what components they'll need to set up.
     */
    public Map<String, Object> getTemplateSummary(Long id) {
        Dnd5eCampaignTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Campaign template not found: " + id));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("id", template.getId());
        summary.put("name", template.getName());
        summary.put("tagline", template.getTagline());
        summary.put("theme", template.getTheme());
        summary.put("levelRange", template.getMinLevel() + "-" + template.getMaxLevel());
        summary.put("recommendedPartySize", template.getRecommendedPartySize());
        summary.put("estimatedSessions", template.getEstimatedSessions());

        try {
            List<String> subsystems = objectMapper.readValue(
                    template.getRecommendedSubsystems(),
                    new TypeReference<List<String>>() {});
            summary.put("recommendedSubsystems", subsystems);
        } catch (JsonProcessingException e) {
            summary.put("recommendedSubsystems", Collections.emptyList());
        }

        summary.put("setupInstructions", template.getSetupInstructions());
        summary.put("pacingGuide", template.getPacingGuide());
        summary.put("exampleHook", template.getExampleHook());
        summary.put("exampleVillain", template.getExampleVillain());
        summary.put("difficultyScaling", template.getDifficultyScaling());
        summary.put("dmTips", template.getDmTips());

        return summary;
    }

    // --- Auto-population ---

    @Transactional
    public void populateCampaignTemplates() {
        if (templateRepository.count() > 0) {
            log.info("Campaign templates already populated with {} templates", templateRepository.count());
            return;
        }

        log.info("Populating campaign templates database...");

        // 1. Horror Template
        Dnd5eCampaignTemplate horror = new Dnd5eCampaignTemplate();
        horror.setName("Ravenloft Horror");
        horror.setTagline("Descend into madness in the Domains of Dread");
        horror.setDescription(
                "A horror-themed campaign set in a Domain of Dread where players face " +
                "cosmic terrors, undead horrors, and the psychological torment of a Darklord's " +
                "eternal domain. Fear and dread mechanics drive the narrative, with Dark Gifts " +
                "offering power at a terrible cost."
        );
        horror.setTheme("Horror");
        horror.setMinLevel(1);
        horror.setMaxLevel(10);
        horror.setRecommendedPartySize(4);
        horror.setEstimatedSessions(12);
        horror.setRecommendedSubsystems("[\"horror\", \"tension_pools\", \"dark_gifts\", \"villains\", \"npcs\", \"dungeon_rooms\"]");
        horror.setSetupInstructions(
                "1. Create a Domain of Dread using the Horror subsystem\n" +
                "2. Set up a Darklord villain with lair actions and legendary actions\n" +
                "3. Configure fear and dread levels for the domain\n" +
                "4. Create tension pools for different areas of the domain\n" +
                "5. Populate dungeon rooms with horror-themed traps and encounters\n" +
                "6. Add NPCs with varying dispositions — some allies, some corrupted\n" +
                "7. Set up Dark Gifts that players can acquire at a cost\n" +
                "8. Create adventure hooks that draw players into the domain"
        );
        horror.setPacingGuide(
                "Sessions 1-2: Arrival — Players enter the domain; introduce fear mechanics\n" +
                "Sessions 3-4: Discovery — Learn about the Darklord and the domain's history\n" +
                "Sessions 5-6: First Confrontation — Encounter the Darklord's minions; dread rises\n" +
                "Sessions 7-8: Descent — Explore deeper areas; acquire Dark Gifts\n" +
                "Sessions 9-10: Revelation — Discover the Darklord's weakness\n" +
                "Sessions 11-12: Climax — Final confrontation; escape or eternal imprisonment"
        );
        horror.setExampleHook(
                "The party stumbles upon a fog-shrouded village where the sun hasn't shone in decades. " +
                "The villagers speak in hushed tones of the Darklord who rules from a crumbling castle " +
                "on the hill. One of the villagers recognizes a party member — from a life they don't remember."
        );
        horror.setExampleVillain(
                "Count Varnek the Undying — A lich who rules Barovia-like domain. " +
                "CR 21, phylactery hidden in the castle's deepest vault. " +
                "Motives: Eternal domination, collecting souls to fuel his immortality. " +
                "Lair actions: summon shadows, cause fear, manipulate the fog."
        );
        horror.setExampleNpcs(
                "[\"Father Wilhelm — the village priest who knows the domain's history (ally)\", " +
                "\"Anya the Innkeeper — secretly a spy for the Darklord (unfriendly)\", " +
                "\"Rudolph van Richten — vampire hunter who offers guidance (ally)\", " +
                "\"The Mad Hermit — knows the Darklord's weakness but speaks in riddles (indifferent)\"]"
        );
        horror.setExampleEncounters(
                "[\"Wandering zombies in the fog (tension check)\", " +
                "\"Haunted mirror that shows players' worst fears (fear check)\", " +
                "\"Vampire spawn ambush in the forest (combat)\", " +
                "\"Cursed village where time loops (puzzle)\"]"
        );
        horror.setDifficultyScaling(
                "For smaller parties (2-3): Reduce monster HP by 25%, reduce dread gain by 5 per event. " +
                "For larger parties (6-8): Increase monster HP by 50%, add additional tension triggers. " +
                "For higher levels (11-20): Add more Darklord lieutenants, increase dread thresholds."
        );
        horror.setDmTips(
                "- Use fear checks sparingly to maintain tension without frustrating players\n" +
                "- Let dread accumulate naturally through player actions, not just encounters\n" +
                "- Make Dark Gifts tempting but with meaningful curses\n" +
                "- Use the fog and atmosphere as narrative tools\n" +
                "- Allow players to find ways to reduce dread through roleplay\n" +
                "- The Darklord should feel omnipresent even when not directly encountered"
        );
        templateRepository.save(horror);

        // 2. High Magic Template
        Dnd5eCampaignTemplate highMagic = new Dnd5eCampaignTemplate();
        highMagic.setName("Arcane Academy");
        highMagic.setTagline("Master the arcane arts in a world of boundless magic");
        highMagic.setDescription(
                "A high-magic campaign centered around an ancient arcane academy where players " +
                "study, compete, and uncover mysteries that threaten the magical fabric of reality. " +
                "Growing magic items reward progression, while psionic powers offer an alternative " +
                "path to magical mastery."
        );
        highMagic.setTheme("High Magic");
        highMagic.setMinLevel(1);
        highMagic.setMaxLevel(15);
        highMagic.setRecommendedPartySize(4);
        highMagic.setEstimatedSessions(15);
        highMagic.setRecommendedSubsystems("[\"growing_items\", \"psionics\", \"feats\", \"crafting\", \"npcs\", \"villains\"]");
        highMagic.setSetupInstructions(
                "1. Create the arcane academy as a bastion with library and training facilities\n" +
                "2. Set up growing magic items that scale with player progression\n" +
                "3. Add psionic powers for non-spellcasting characters\n" +
                "4. Create NPC professors, rivals, and mentors\n" +
                "5. Design a villain who threatens the magical order\n" +
                "6. Set up crafting recipes for magical items\n" +
                "7. Add feats that enhance magical abilities"
        );
        highMagic.setPacingGuide(
                "Sessions 1-3: Enrollment — Arrive at the academy; choose mentors and rooms\n" +
                "Sessions 4-6: First Year — Learn basics; receive growing magic items\n" +
                "Sessions 7-9: Rivalry — Compete with other students; discover ancient secrets\n" +
                "Sessions 10-12: Crisis — Magical anomaly threatens the academy\n" +
                "Sessions 13-15: Resolution — Confront the source; defend the academy"
        );
        highMagic.setExampleHook(
                "The party receives letters of acceptance to the Arcane Academy, each chosen for " +
                "a unique magical aptitude. But the academy's headmaster is missing, and strange " +
                "magical disturbances suggest something is wrong within the ancient halls."
        );
        highMagic.setExampleVillain(
                "Archmage Malakor — A former academy master who sought to harness raw planar energy. " +
                "CR 18, legendary actions include spell theft and planar rift creation. " +
                "Motives: Unleash unlimited magical power, transcend mortal limitations."
        );
        highMagic.setExampleNpcs(
                "[\"Professor Elara — wise mentor who guides the party (ally)\", " +
                "\"Kael the Rival — competitive student who challenges the party (unfriendly)\", " +
                "\"The Librarian — ancient being who guards forbidden knowledge (indifferent)\", " +
                "\"Headmaster's Familiar — speaks cryptically about the headmaster's fate (helpful)\"]"
        );
        highMagic.setExampleEncounters(
                "[\"Magical duel in the training grounds (combat)\", " +
                "\"Forbidden library puzzle (skill challenge)\", " +
                "\"Planar rift incursion (exploration)\", " +
                "\"Alchemy lab explosion (crafting challenge)\"]"
        );
        highMagic.setDifficultyScaling(
                "For smaller parties: Reduce spell DCs by 2, give extra spell slots. " +
                "For larger parties: Add more magical anomalies, increase rival student numbers. " +
                "For higher levels: Introduce planar threats and elder archmages."
        );
        highMagic.setDmTips(
                "- Balance spellcasters and non-spellcasters with psionic options\n" +
                "- Use growing magic items to reward progression meaningfully\n" +
                "- Make the academy feel alive with NPCs and events\n" +
                "- Let crafting be a way to customize magical abilities\n" +
                "- Magical duels should be creative, not just damage contests"
        );
        templateRepository.save(highMagic);

        // 3. Sandbox Template
        Dnd5eCampaignTemplate sandbox = new Dnd5eCampaignTemplate();
        sandbox.setName("Kingdoms at War");
        sandbox.setTagline("Shape the fate of kingdoms in an open-world epic");
        sandbox.setDescription(
                "An open-world sandbox campaign where players navigate political intrigue, " +
                "military conflict, and exploration across multiple kingdoms. Bastions serve as " +
                "party bases, NPC relationships drive the narrative, and tension pools track " +
                "the escalating war."
        );
        sandbox.setTheme("Sandbox");
        sandbox.setMinLevel(3);
        sandbox.setMaxLevel(15);
        sandbox.setRecommendedPartySize(5);
        sandbox.setEstimatedSessions(20);
        sandbox.setRecommendedSubsystems("[\"bastions\", \"npcs\", \"tension_pools\", \"villains\", \"adventure_hooks\", \"encounters\"]");
        sandbox.setSetupInstructions(
                "1. Create multiple kingdoms with distinct themes and rulers\n" +
                "2. Set up bastions as party bases in each kingdom\n" +
                "3. Populate NPC rosters with rulers, generals, spies, and merchants\n" +
                "4. Configure tension pools for war escalation in each region\n" +
                "5. Create villains for each faction with competing motives\n" +
                "6. Generate adventure hooks that connect kingdoms\n" +
                "7. Design encounters that reflect the war's progression"
        );
        sandbox.setPacingGuide(
                "Sessions 1-4: Introduction — Explore the kingdoms; establish alliances\n" +
                "Sessions 5-8: Rising Tensions — Skirmishes begin; build bastions\n" +
                "Sessions 9-12: Open War — Major battles; NPC relationships tested\n" +
                "Sessions 13-16: Turning Point — Discover the true mastermind\n" +
                "Sessions 17-20: Resolution — End the war; shape the new order"
        );
        sandbox.setExampleHook(
                "The party is caught between two warring kingdoms, each offering rewards for " +
                "their aid. But a third faction manipulates both sides, and the true conflict " +
                "is for control of an ancient artifact buried beneath the borderlands."
        );
        sandbox.setExampleVillain(
                "General Voss — A warlord who seeks to unite the kingdoms through conquest. " +
                "CR 15, legendary actions include battlefield command and siege warfare. " +
                "Motives: Unify the realm under one banner, prove military superiority."
        );
        sandbox.setExampleNpcs(
                "[\"King Aldric — ruler of the northern kingdom (ally)\", " +
                "\"Queen Seraphina — ruler of the southern kingdom (indifferent)\", " +
                "\"Spy Master Kael — offers intelligence for favors (unfriendly)\", " +
                "\"Merchant Lord — supplies equipment at premium prices (indifferent)\"]"
        );
        sandbox.setExampleEncounters(
                "[\"Ambush on the trade road (combat)\", " +
                "\"Diplomatic negotiation (social)\", " +
                "\"Espionage mission (stealth)\", " +
                "\"Siege of a border fortress (large-scale combat)\"]"
        );
        sandbox.setDifficultyScaling(
                "For smaller parties: Focus on diplomacy and stealth over large battles. " +
                "For larger parties: Add more factions and complex political webs. " +
                "For higher levels: Introduce legendary weapons and ancient powers."
        );
        sandbox.setDmTips(
                "- Let player choices genuinely affect the war's outcome\n" +
                "- Use tension pools to make the world feel reactive\n" +
                "- NPC relationships should have consequences\n" +
                "- Bastions provide a sense of ownership and progression\n" +
                "- Keep multiple plot threads active for player agency"
        );
        templateRepository.save(sandbox);

        // 4. Dungeon Crawl Template
        Dnd5eCampaignTemplate dungeonCrawl = new Dnd5eCampaignTemplate();
        dungeonCrawl.setName("Tomb of the Forgotten King");
        dungeonCrawl.setTagline("Descend into the depths and claim ancient treasure");
        dungeonCrawl.setDescription(
                "A classic dungeon crawl campaign where players explore a multi-level " +
                "dungeon filled with traps, treasure, and deadly encounters. Each level " +
                "presents unique themes, puzzles, and bosses, with circular connections " +
                "encouraging non-linear exploration."
        );
        dungeonCrawl.setTheme("Dungeon Crawl");
        dungeonCrawl.setMinLevel(1);
        dungeonCrawl.setMaxLevel(10);
        dungeonCrawl.setRecommendedPartySize(4);
        dungeonCrawl.setEstimatedSessions(10);
        dungeonCrawl.setRecommendedSubsystems("[\"dungeon_rooms\", \"encounters\", \"npcs\", \"growing_items\", \"tension_pools\", \"crafting\"]");
        dungeonCrawl.setSetupInstructions(
                "1. Create dungeon rooms across multiple levels with circular connections\n" +
                "2. Design traps with varying DCs and consequences\n" +
                "3. Place treasure and growing magic items throughout\n" +
                "4. Populate encounters with monsters appropriate to each level\n" +
                "5. Add NPCs — trapped adventurers, dungeon guardians, and guides\n" +
                "6. Set up tension pools for wandering encounters\n" +
                "7. Include crafting opportunities for consumables"
        );
        dungeonCrawl.setPacingGuide(
                "Sessions 1-2: Entrance — Explore the upper levels; learn the dungeon's layout\n" +
                "Sessions 3-4: Descent — Deeper levels; first major boss\n" +
                "Sessions 5-6: Midpoint — Discover the dungeon's secret; gain growing item\n" +
                "Sessions 7-8: Depths — Hardest traps and encounters\n" +
                "Sessions 9-10: Throne Room — Final boss; claim the treasure"
        );
        dungeonCrawl.setExampleHook(
                "An ancient map leads the party to the entrance of a forgotten tomb. " +
                "Rumors speak of a king who was entombed alive with his treasures, " +
                "and the dungeon's traps have claimed every explorer who entered."
        );
        dungeonCrawl.setExampleVillain(
                "The Forgotten King — An undead monarch who guards his tomb with spectral servants. " +
                "CR 17, legendary actions include trap activation and spectral summoning. " +
                "Motives: Protect his treasure, prevent anyone from claiming his legacy."
        );
        dungeonCrawl.setExampleNpcs(
                "[\"Trapped Explorer — offers dungeon tips for rescue (indifferent)\", " +
                "\"Dungeon Guardian — ancient construct that tests intruders (hostile)\", " +
                "\"Ghostly Guide — the king's former advisor who offers warnings (helpful)\", " +
                "\"Treasure Hoard Dragon — guards the lower levels (hostile)\"]"
        );
        dungeonCrawl.setExampleEncounters(
                "[\"Pit trap with falling spikes (trap check)\", " +
                "\"Spectral warriors ambush (combat)\", " +
                "\"Puzzle door requiring riddle solution (puzzle)\", " +
                "\"Treasure room with cursed items (exploration)\"]"
        );
        dungeonCrawl.setDifficultyScaling(
                "For smaller parties: Reduce trap damage, add more resting spots. " +
                "For larger parties: Add more traps per room, increase monster numbers. " +
                "For higher levels: Add legendary monsters and planar threats."
        );
        dungeonCrawl.setDmTips(
                "- Use circular connections to avoid linear dungeon design\n" +
                "- Balance traps with opportunities to detect and disable them\n" +
                "- Treasure should feel rewarding and thematic\n" +
                "- Each dungeon level should have a distinct theme\n" +
                "- Wandering encounters add tension but don't overuse them"
        );
        templateRepository.save(dungeonCrawl);

        // 5. Feywild Template
        Dnd5eCampaignTemplate feywild = new Dnd5eCampaignTemplate();
        feywild.setName("Whispers of the Feywild");
        feywild.setTagline("Navigate the whimsical and dangerous Feywild realm");
        feywild.setDescription(
                "A Feywild-themed campaign where players enter the realm of the fey, " +
                "where logic is twisted, bargains are binding, and beauty masks danger. " +
                "Non-combat resolution, trickery, and social interaction are key, " +
                "with fey NPCs offering both aid and peril."
        );
        feywild.setTheme("Feywild");
        feywild.setMinLevel(1);
        feywild.setMaxLevel(10);
        feywild.setRecommendedPartySize(4);
        feywild.setEstimatedSessions(12);
        feywild.setRecommendedSubsystems("[\"npcs\", \"adventure_hooks\", \"dungeon_rooms\", \"villains\", \"growing_items\", \"feats\"]");
        feywild.setSetupInstructions(
                "1. Create fey NPCs with whimsical and dangerous personalities\n" +
                "2. Design dungeon rooms that reflect Feywild's twisted logic\n" +
                "3. Set up adventure hooks that draw players into fey bargains\n" +
                "4. Add a fey villain with archfey-level power\n" +
                "5. Include growing magic items with fey-themed abilities\n" +
                "6. Add feats that enhance social and trickery abilities\n" +
                "7. Create puzzles and challenges that reward creativity over combat"
        );
        feywild.setPacingGuide(
                "Sessions 1-2: Crossing — Enter the Feywild; learn its rules\n" +
                "Sessions 3-4: First Bargain — Meet a fey lord; make a deal\n" +
                "Sessions 5-6: Twisted Path — Navigate Feywild's logic puzzles\n" +
                "Sessions 7-8: Fey Court — Attend a fey gathering; social challenges\n" +
                "Sessions 9-10: Betrayal — Discover the villain's plot\n" +
                "Sessions 11-12: Escape — Break free of the Feywild's grip"
        );
        feywild.setExampleHook(
                "The party follows a mysterious flower into a hidden grove and finds themselves " +
                "in the Feywild. A fey lord offers them a bargain: complete a task, and they'll " +
                "be returned home with a boon. Refuse, and they may never leave."
        );
        feywild.setExampleVillain(
                "Archfey Morwenna — The Lady of Thorns who seeks to expand her domain into the Material Plane. " +
                "CR 20, legendary actions include reality warping and fey summoning. " +
                "Motives: Conquer the Material Plane, collect souls through broken bargains."
        );
        feywild.setExampleNpcs(
                "[\"Fey Lord Thistlewick — whimsical but dangerous (indifferent)\", " +
                "\"The Lost Child — a human trapped in the Feywild (helpful)\", " +
                "\"Bargain Keeper — enforces fey contracts (hostile if broken)\", " +
                "\"Dryad Guide — offers safe passage for a price (friendly)\"]"
        );
        feywild.setExampleEncounters(
                "[\"Riddle gate that blocks the path (puzzle)\", " +
                "\"Fey banquet with poisoned food (social)\", " +
                "\"Mirror maze that shows reflections' true selves (exploration)\", " +
                "\"Fey court debate where words are weapons (social)\"]"
        );
        feywild.setDifficultyScaling(
                "For smaller parties: Reduce fey lord power, add more helpful NPCs. " +
                "For larger parties: Add more fey courts and complex bargains. " +
                "For higher levels: Introduce elder fey and planar threats."
        );
        feywild.setDmTips(
                "- Emphasize non-combat resolution and creative problem-solving\n" +
                "- Fey bargains should have unexpected consequences\n" +
                "- Beauty and danger should coexist in every scene\n" +
                "- Logic puzzles should reward creativity, not just intelligence\n" +
                "- NPCs should be unpredictable but consistent in their whimsy"
        );
        templateRepository.save(feywild);

        // 6. Steampunk Template
        Dnd5eCampaignTemplate steampunk = new Dnd5eCampaignTemplate();
        steampunk.setName("Gears of Destiny");
        steampunk.setTagline("Innovate and survive in a world of magic and machinery");
        steampunk.setDescription(
                "A steampunk-themed campaign where magic and technology intertwine. " +
                "Players craft magical devices, explore industrial cities, and confront " +
                "a villain who seeks to replace magic with cold machinery. " +
                "Crafting is central, with tool proficiencies and recipes driving progression."
        );
        steampunk.setTheme("Steampunk");
        steampunk.setMinLevel(1);
        steampunk.setMaxLevel(12);
        steampunk.setRecommendedPartySize(4);
        steampunk.setEstimatedSessions(14);
        steampunk.setRecommendedSubsystems("[\"crafting\", \"tool_proficiencies\", \"npcs\", \"villains\", \"bastions\", \"adventure_hooks\"]");
        steampunk.setSetupInstructions(
                "1. Create a steampunk city as the central bastion\n" +
                "2. Set up crafting recipes for magical devices and gadgets\n" +
                "3. Add tool proficiencies for engineering and invention\n" +
                "4. Populate NPCs with inventors, industrialists, and rebels\n" +
                "5. Design a villain who threatens the balance of magic and technology\n" +
                "6. Create adventure hooks that involve invention and exploration\n" +
                "7. Add bastion facilities for crafting and research"
        );
        steampunk.setPacingGuide(
                "Sessions 1-3: Arrival — Enter the city; learn about magic-tech fusion\n" +
                "Sessions 4-6: First Invention — Craft initial gadgets; meet mentors\n" +
                "Sessions 7-9: Conspiracy — Discover the villain's plot\n" +
                "Sessions 10-12: Escalation — Race to build counter-technology\n" +
                "Sessions 13-14: Climax — Confront the villain; save the city"
        );
        steampunk.setExampleHook(
                "The party arrives in a city where magic powers every machine. But strange " +
                "malfunctions suggest someone is sabotaging the magical infrastructure, " +
                "and the city's greatest inventor has vanished without a trace."
        );
        steampunk.setExampleVillain(
                "Architect Vex — A mad inventor who seeks to replace all magic with pure machinery. " +
                "CR 16, legendary actions include machine summoning and reality engineering. " +
                "Motives: Eliminate magic, create a world of pure logic and machinery."
        );
        steampunk.setExampleNpcs(
                "[\"Inventor Zara — mentor who teaches crafting (ally)\", " +
                "\"Industrialist Greaves — funds research for profit (indifferent)\", " +
                "\"Rebel Leader — opposes the industrialists (friendly)\", " +
                "\"Clockwork Automaton — sentient machine that offers aid (helpful)\"]"
        );
        steampunk.setExampleEncounters(
                "[\"Factory sabotage investigation (exploration)\", " +
                "\"Gadget crafting challenge (crafting)\", " +
                "\"Automaton rebellion (combat)\", " +
                "\"Magical engine repair (skill challenge)\"]"
        );
        steampunk.setDifficultyScaling(
                "For smaller parties: Reduce crafting DCs, provide more materials. " +
                "For larger parties: Add more complex crafting projects and rival inventors. " +
                "For higher levels: Introduce planar engines and god-tier machinery."
        );
        steampunk.setDmTips(
                "- Make crafting central to character progression\n" +
                "- Balance magic and technology so both feel relevant\n" +
                "- Use tool proficiencies to give non-crafters meaningful roles\n" +
                "- Gadgets should solve problems creatively\n" +
                "- The city should feel alive with industry and innovation"
        );
        templateRepository.save(steampunk);

        log.info("Populated 6 campaign templates");
    }

    @Transactional
    public void initializeCampaignTemplates() {
        if (!templateRepository.existsByName("Ravenloft Horror")) {
            populateCampaignTemplates();
        }
    }
}

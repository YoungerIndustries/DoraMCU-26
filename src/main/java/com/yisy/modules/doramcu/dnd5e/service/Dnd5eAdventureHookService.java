package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eAdventureHook;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eCharacter;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eAdventureHookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for managing D&D 5e Adventure Hooks.
 * <p>
 * Provides CRUD operations for adventure hooks, personalized hook generation based on
 * character profiles (background, race, class, level), and reference data for types,
 * themes, and urgencies.
 * </p>
 * <p>
 * Key mechanics:
 * - Hook generation matches character background, race, class, and level
 * - Hooks have urgency levels: Immediate, Soon, Eventual
 * - Themes include: Horror, High Magic, Sandbox, Dungeon Crawl, Feywild, Steampunk, etc.
 * - Types include: Personal, Political, Mystery, Combat, Exploration, Social, Supernatural
 * </p>
 */
@Service
public class Dnd5eAdventureHookService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eAdventureHookService.class);

    private final Dnd5eAdventureHookRepository hookRepository;

    public Dnd5eAdventureHookService(Dnd5eAdventureHookRepository hookRepository) {
        this.hookRepository = hookRepository;

        // Initialize database with example hooks if empty
        initializeHookDatabase();
    }

    // ==================== Hook CRUD ====================

    @Transactional
    public Dnd5eAdventureHook createHook(Dnd5eAdventureHook hook) {
        log.info("Creating adventure hook: {}", hook.getName());
        return hookRepository.save(hook);
    }

    public Optional<Dnd5eAdventureHook> getHook(Long id) {
        return hookRepository.findById(id);
    }

    public Optional<Dnd5eAdventureHook> getHookByName(String name) {
        return hookRepository.findByName(name);
    }

    public List<Dnd5eAdventureHook> listHooks() {
        return hookRepository.findAll();
    }

    public List<Dnd5eAdventureHook> listActiveHooks() {
        return hookRepository.findByActiveTrue();
    }

    public List<Dnd5eAdventureHook> listHooksByType(String type) {
        return hookRepository.findByTypeAndActiveTrue(type);
    }

    public List<Dnd5eAdventureHook> listHooksByTheme(String theme) {
        return hookRepository.findByThemeAndActiveTrue(theme);
    }

    public List<Dnd5eAdventureHook> listHooksByUrgency(String urgency) {
        return hookRepository.findByUrgencyAndActiveTrue(urgency);
    }

    public List<Dnd5eAdventureHook> listHooksByTargetBackground(String background) {
        return hookRepository.findByTargetBackground(background);
    }

    public List<Dnd5eAdventureHook> listHooksByTargetRace(String race) {
        return hookRepository.findByTargetRace(race);
    }

    public List<Dnd5eAdventureHook> listHooksByTargetClass(String characterClass) {
        return hookRepository.findByTargetClass(characterClass);
    }

    public List<Dnd5eAdventureHook> searchHooks(String keyword) {
        return hookRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Dnd5eAdventureHook updateHook(Long id, Dnd5eAdventureHook updates) {
        return hookRepository.findById(id)
                .map(hook -> {
                    if (updates.getName() != null) {
                        hook.setName(updates.getName());
                    }
                    if (updates.getType() != null) {
                        hook.setType(updates.getType());
                    }
                    if (updates.getTheme() != null) {
                        hook.setTheme(updates.getTheme());
                    }
                    if (updates.getUrgency() != null) {
                        hook.setUrgency(updates.getUrgency());
                    }
                    if (updates.getTargetBackground() != null) {
                        hook.setTargetBackground(updates.getTargetBackground());
                    }
                    if (updates.getTargetRace() != null) {
                        hook.setTargetRace(updates.getTargetRace());
                    }
                    if (updates.getTargetClass() != null) {
                        hook.setTargetClass(updates.getTargetClass());
                    }
                    if (updates.getNarrative() != null) {
                        hook.setNarrative(updates.getNarrative());
                    }
                    if (updates.getDmNotes() != null) {
                        hook.setDmNotes(updates.getDmNotes());
                    }
                    hook.setMinLevel(updates.getMinLevel());
                    hook.setMaxLevel(updates.getMaxLevel());
                    hook.setActive(updates.isActive());
                    return hookRepository.save(hook);
                })
                .orElseThrow(() -> new RuntimeException("Adventure hook not found with id: " + id));
    }

    @Transactional
    public boolean deleteHook(Long id) {
        return hookRepository.findById(id)
                .map(hook -> {
                    hookRepository.delete(hook);
                    log.info("Deleted adventure hook: {}", hook.getName());
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public void toggleHookActive(Long id) {
        hookRepository.findById(id)
                .ifPresent(hook -> {
                    hook.setActive(!hook.isActive());
                    hookRepository.save(hook);
                    log.info("Toggled hook active state: {} -> {}", hook.getName(), hook.isActive());
                });
    }

    // ==================== Hook Generation ====================

    /**
     * Generate personalized adventure hooks for a character.
     * Matches hooks by character's level, background, race, and class.
     *
     * @param character the character to generate hooks for
     * @return list of matching hooks, shuffled for variety
     */
    public List<Dnd5eAdventureHook> generateHooksForCharacter(Dnd5eCharacter character) {
        String background = character.getBackground() != null ? character.getBackground() : "";
        String race = character.getRace() != null ? character.getRace() : "";
        String characterClass = character.getCharacterClass() != null ? character.getCharacterClass() : "";
        int level = character.getLevel();

        List<Dnd5eAdventureHook> hooks = hookRepository.findByCharacterProfile(
                level, background, race, characterClass);

        // Shuffle for variety
        Collections.shuffle(hooks);
        log.info("Generated {} hooks for character: {} ({}, {} {})",
                hooks.size(), character.getName(), race, characterClass, background);
        return hooks;
    }

    /**
     * Generate personalized adventure hooks for a character with a specific theme.
     *
     * @param character the character to generate hooks for
     * @param theme the theme to filter by (e.g., "Horror", "High Magic")
     * @return list of matching hooks, shuffled for variety
     */
    public List<Dnd5eAdventureHook> generateHooksForCharacterWithTheme(Dnd5eCharacter character, String theme) {
        String background = character.getBackground() != null ? character.getBackground() : "";
        String race = character.getRace() != null ? character.getRace() : "";
        String characterClass = character.getCharacterClass() != null ? character.getCharacterClass() : "";
        int level = character.getLevel();

        List<Dnd5eAdventureHook> hooks = hookRepository.findByCharacterProfileAndTheme(
                level, background, race, characterClass, theme);

        Collections.shuffle(hooks);
        log.info("Generated {} {}-themed hooks for character: {}",
                hooks.size(), theme, character.getName());
        return hooks;
    }

    /**
     * Generate hooks for a character by level only (no background/race/class filtering).
     *
     * @param level the character's level
     * @return list of level-appropriate hooks, shuffled for variety
     */
    public List<Dnd5eAdventureHook> generateHooksByLevel(int level) {
        List<Dnd5eAdventureHook> hooks = hookRepository.findByCharacterLevel(level);
        Collections.shuffle(hooks);
        return hooks;
    }

    /**
     * Generate hooks for a specific background.
     *
     * @param background the character's background
     * @return list of matching hooks
     */
    public List<Dnd5eAdventureHook> generateHooksByBackground(String background) {
        return hookRepository.findByBackgroundOrAny(background);
    }

    /**
     * Generate hooks for a specific race.
     *
     * @param race the character's race
     * @return list of matching hooks
     */
    public List<Dnd5eAdventureHook> generateHooksByRace(String race) {
        return hookRepository.findByRaceOrAny(race);
    }

    /**
     * Generate hooks for a specific class.
     *
     * @param characterClass the character's class
     * @return list of matching hooks
     */
    public List<Dnd5eAdventureHook> generateHooksByClass(String characterClass) {
        return hookRepository.findByClassOrAny(characterClass);
    }

    /**
     * Generate a random hook from all active hooks.
     *
     * @return a randomly selected active hook
     */
    public Optional<Dnd5eAdventureHook> generateRandomHook() {
        List<Dnd5eAdventureHook> hooks = hookRepository.findByActiveTrue();
        if (hooks.isEmpty()) {
            return Optional.empty();
        }
        Random random = new Random();
        return Optional.ofNullable(hooks.get(random.nextInt(hooks.size())));
    }

    /**
     * Generate a random hook of a specific type.
     *
     * @param type the hook type
     * @return a randomly selected active hook of the given type
     */
    public Optional<Dnd5eAdventureHook> generateRandomHookByType(String type) {
        List<Dnd5eAdventureHook> hooks = hookRepository.findByTypeAndActiveTrue(type);
        if (hooks.isEmpty()) {
            return Optional.empty();
        }
        Random random = new Random();
        return Optional.ofNullable(hooks.get(random.nextInt(hooks.size())));
    }

    /**
     * Generate a random hook of a specific theme.
     *
     * @param theme the hook theme
     * @return a randomly selected active hook of the given theme
     */
    public Optional<Dnd5eAdventureHook> generateRandomHookByTheme(String theme) {
        List<Dnd5eAdventureHook> hooks = hookRepository.findByThemeAndActiveTrue(theme);
        if (hooks.isEmpty()) {
            return Optional.empty();
        }
        Random random = new Random();
        return Optional.ofNullable(hooks.get(random.nextInt(hooks.size())));
    }

    // ==================== Reference Data ====================

    public List<String> getHookTypes() {
        return hookRepository.findDistinctTypes();
    }

    public List<String> getHookThemes() {
        return hookRepository.findDistinctThemes();
    }

    public List<String> getHookUrgencies() {
        return hookRepository.findDistinctUrgencies();
    }

    // ==================== Database Initialization ====================

    @Transactional
    void initializeHookDatabase() {
        if (hookRepository.count() > 0) {
            log.info("Adventure hooks already populated, skipping initialization");
            return;
        }

        log.info("Populating adventure hooks database");
        populateAdventureHooks();
    }

    void populateAdventureHooks() {
        List<Dnd5eAdventureHook> hooks = new ArrayList<>();

        // ==================== Personal Hooks ====================

        hooks.add(createHook("The Last Letter", "Personal", "Horror", "Immediate",
                "Soldier", null, null,
                "A letter arrives from a comrade you believed dead in the war. The handwriting is unmistakable, " +
                "but the message is filled with desperate pleas and warnings about something that stalks the dead. " +
                "The letter ends with coordinates to a mass grave that shouldn't exist.",
                "The comrade is possessed or reanimated. The mass grave contains creatures that were never human. " +
                "Lead to a larger horror: a necromancer or an otherworldly entity.", 1, 10));

        hooks.add(createHook("Blood Debt", "Personal", "Sandbox", "Soon",
                "Criminal", null, null,
                "Your old criminal contact shows up at your door, bleeding and terrified. They owe you a favor, " +
                "and they're using it: someone is hunting members of your old crew, one by one. You're next. " +
                "The killer knows your face, your habits, and your secrets.",
                "The killer is a rival organization or a former crew member seeking revenge. " +
                "Each fallen crew member left a clue about the killer's identity and base of operations.", 1, 15));

        hooks.add(createHook("The Forgotten Oath", "Personal", "High Magic", "Eventual",
                null, null, "Cleric",
                "In your dreams, your deity speaks to you of an oath you swore long ago — or perhaps one you will " +
                "swear. A sacred relic has been stolen from your temple, and only you can sense its fading power. " +
                "The relic's trail leads through places of great magical significance.",
                "The relic was stolen by a rival faith or a cult seeking to corrupt its power. " +
                "Each location on the trail tests the cleric's faith and reveals more about the thief's plan.", 1, 20));

        hooks.add(createHook("Family Secrets", "Personal", "Mystery", "Soon",
                "Noble", null, null,
                "Your family's ancestral home has been sealed for generations, but a recent earthquake has cracked " +
                "the doors open. Inside, you find evidence that your family's wealth and power were built on a lie — " +
                "or a secret pact with something that is now demanding payment.",
                "The family secret involves a pact with a fey, devil, or ancient entity. " +
                "The debt is coming due, and the family must either pay or find a way to break the pact.", 3, 15));

        hooks.add(createHook("The Stolen Name", "Personal", "Supernatural", "Immediate",
                null, "Elf", null,
                "You wake to find that your true elven name — the one only your kindred know — has been stolen. " +
                "Without it, you feel incomplete, your magic weakened. A shadowy figure was seen near your dwelling, " +
                "whispering in the Old Tongue. Someone is collecting elven names for a ritual.",
                "A rival elf or outsider is collecting true names to bind elven spirits. " +
                "Each stolen name gives the thief power over the victim. Time is running out.", 1, 12));

        // ==================== Political Hooks ====================

        hooks.add(createHook("The Crown's Gambit", "Political", "Sandbox", "Soon",
                "Noble", null, null,
                "The king summons you to court with a secret mission: a neighboring kingdom is plotting invasion, " +
                "and you're to infiltrate their court and gather intelligence. But the real danger is the traitor " +
                "within your own kingdom who is feeding information to the enemy.",
                "The traitor is someone close to the noble — a mentor, sibling, or trusted advisor. " +
                "The neighboring kingdom's plans are more complex than a simple invasion.", 5, 20));

        hooks.add(createHook("Guild Wars", "Political", "Combat", "Immediate",
                "Criminal", null, null,
                "The thieves' guilds of the city have declared open war. Three factions vie for control, and each " +
                "wants you on their side. Refusing means becoming a target. Choosing a side means making powerful " +
                "enemies. The city's innocent citizens are caught in the crossfire.",
                "Each guild has a different endgame: one wants to control the city, one wants to burn it down, " +
                "and one is being manipulated by an outside force. The players must navigate the politics.", 3, 15));

        hooks.add(createHook("The Merchant's Plight", "Political", "Exploration", "Eventual",
                "Merchant", null, null,
                "A wealthy merchant offers a fortune to escort a caravan through the Wildlands. But the route passes " +
                "through territories controlled by rival factions, and the merchant's cargo is far more valuable — " +
                "and dangerous — than they've let on.",
                "The cargo contains magical artifacts, stolen goods, or something alive. " +
                "Each faction along the route has their own agenda and will stop at nothing to claim the cargo.", 1, 10));

        // ==================== Mystery Hooks ====================

        hooks.add(createHook("The Vanishing Village", "Mystery", "Horror", "Immediate",
                null, null, null,
                "An entire village has disappeared overnight. No bodies, no signs of struggle — just empty homes " +
                "with meals still warm on the tables. The only clue is a strange symbol carved into the town square, " +
                "pulsing with faint magical energy. Other villages along the same trade route are reporting nightmares.",
                "A fey entity or planar rift is consuming the villages. The symbol is a beacon. " +
                "The nightmares are a symptom of the encroaching otherworldly influence.", 1, 10));

        hooks.add(createHook("The Alchemist's Formula", "Mystery", "High Magic", "Soon",
                null, null, "Wizard",
                "A renowned alchemist is found dead in their laboratory, and their life's work — a formula for a " +
                "potion that could cure any disease — is missing. Multiple parties are searching for the formula, " +
                "including the alchemist's apprentices, a rival guild, and a shadowy organization that funded the research.",
                "The formula was never completed — the alchemist discovered it required a terrible ingredient. " +
                "The real mystery is who killed the alchemist and why.", 3, 15));

        hooks.add(createHook("The Whispering Library", "Mystery", "High Magic", "Eventual",
                "Sage", null, null,
                "The great library of the city has a secret: certain books whisper to those who read them at night. " +
                "Recently, the whispers have become screams. Patrons who read the affected books vanish, and the " +
                "librarians are too terrified to close the affected wing. The books are calling for something.",
                "The books contain trapped entities or are connected to a planar rift. " +
                "The screams are a distress signal from those already consumed. The library's founder made a pact that is now coming due.", 1, 15));

        hooks.add(createHook("The Murder at Moonrise", "Mystery", "Sandbox", "Immediate",
                null, null, "Rogue",
                "A prominent figure is murdered at a moonlit gala, and you're the only one who saw the killer's face. " +
                "But when you try to describe what you saw, your memory shifts and changes. The killer is using magic " +
                "to erase evidence, and you're the only loose end they haven't dealt with yet.",
                "The victim was investigating a conspiracy that reaches the highest levels of power. " +
                "The memory-altering magic is tied to an artifact the killer carries.", 1, 12));

        // ==================== Combat Hooks ====================

        hooks.add(createHook("The Dragon's Wake", "Combat", "Sandbox", "Immediate",
                null, "Dragonborn", null,
                "A dragon has been sighted in the region, and villages are burning. But this dragon is different — " +
                "it speaks your language, and it's calling for Dragonborn to come to it. It claims to have a message " +
                "from Tiamat herself, and it offers power to those who listen.",
                "The dragon is either a true messenger of Tiamat or an imposter — a dracolich or doppelganger. " +
                "Other Dragonborn have already gone, and their fate is uncertain.", 5, 20));

        hooks.add(createHook("The Undead Army", "Combat", "Horror", "Immediate",
                null, null, "Paladin",
                "The dead are rising across the countryside, and your order has been called to defend the living. " +
                "But the undead aren't mindless — they're organized, led by commanders who were once your allies. " +
                "Something is turning the fallen into weapons, and it's spreading.",
                "A necromancer of immense power is raising the dead, but the source is a corrupted holy relic. " +
                "The paladin's own order may have inadvertently caused the outbreak.", 3, 15));

        hooks.add(createHook("The Beast of the Barrens", "Combat", "Exploration", "Soon",
                "Soldier", null, null,
                "A creature has been terrorizing the barrens, and no army can stop it. It's too fast for cavalry, " +
                "too strong for infantry, and it seems to learn from every battle. The military needs adventurers " +
                "who can track and kill the beast before it reaches the nearest city.",
                "The beast is a mutated creature, a construct, or something from another plane. " +
                "It may be connected to a larger threat — a weapon, an experiment, or an invasion.", 1, 12));

        // ==================== Exploration Hooks ====================

        hooks.add(createHook("The Sunken City", "Exploration", "High Magic", "Eventual",
                null, null, null,
                "Ancient maps speak of a city that sank beneath the waves centuries ago, filled with treasures " +
                "beyond imagination. Recently, divers have reported seeing lights beneath the surface, and fishermen " +
                "speak of a song that draws them underwater. The city is waking up.",
                "The city was sealed for a reason — an ancient evil or a powerful artifact. " +
                "The lights and song are a lure, drawing victims to awaken whatever sleeps below.", 1, 15));

        hooks.add(createHook("The Feywild Border", "Exploration", "Feywild", "Soon",
                null, "Elf", null,
                "The border between the Material Plane and the Feywild is thinning in your region. Creatures and " +
                "phenomena from the Feywild are spilling through, and the local fey are making demands. An archfey " +
                "offers an alliance, but the price is a quest into the heart of the Feywild.",
                "The archfey's true motives are hidden. The quest will test the characters' morality and force " +
                "them to navigate fey politics and trickery.", 3, 15));

        hooks.add(createHook("The Lost Expedition", "Exploration", "Sandbox", "Immediate",
                "Folk Hero", null, null,
                "An expedition into the uncharted wilderness has gone silent. Their last message mentioned ancient " +
                "ruins, strange creatures, and a discovery that could change the world. A rescue party is needed, " +
                "but the wilderness itself seems to be fighting back.",
                "The expedition found something they shouldn't have — a portal, an artifact, or a sleeping entity. " +
                "The wilderness is protected by ancient wards and creatures.", 1, 10));

        // ==================== Social Hooks ====================

        hooks.add(createHook("The Festival of Masks", "Social", "Feywild", "Eventual",
                "Charlatan", null, null,
                "The annual Festival of Masks is the city's biggest event, where nobles and commoners mingle in " +
                "disguise. This year, the festival is the cover for a grand heist, a political assassination, " +
                "and a secret meeting between rival factions. Everyone is lying, and the truth is hidden behind every mask.",
                "Multiple plotlines converge at the festival. The heist, assassination, and meeting are connected. " +
                "The characters must navigate social intrigue to uncover the truth.", 1, 12));

        hooks.add(createHook("The Diplomatic Crisis", "Social", "Sandbox", "Soon",
                "Noble", null, null,
                "An ambassador from a distant kingdom has arrived with an offer of peace — or so they claim. " +
                "But their entourage includes spies, assassins, and agents of chaos. The ambassador's true goal " +
                "is to destabilize the kingdom from within, and they're using the diplomatic proceedings as cover.",
                "The ambassador is either a master manipulator or a pawn of a larger power. " +
                "The characters must navigate court politics and espionage to prevent war.", 5, 20));

        hooks.add(createHook("The Heist of the Century", "Social", "Steampunk", "Immediate",
                "Criminal", null, null,
                "A legendary artifact is being transported through the city, and every criminal organization wants it. " +
                "The job is impossible — or so everyone says. But you've been hired by someone who knows the route, " +
                "the guards, and the secrets of the artifact. The catch: your employer may be working for the enemy.",
                "The artifact is a magical or technological device of immense power. " +
                "Multiple factions are planning heists simultaneously, creating chaos and opportunity.", 3, 15));

        // ==================== Supernatural Hooks ====================

        hooks.add(createHook("The Rift", "Supernatural", "Horror", "Immediate",
                null, null, null,
                "A tear in reality has opened in the countryside, and things are crawling through it. The local " +
                "military can't stop them, and the rift is growing. Scholars say it's a planar breach, but no one " +
                "knows how to close it. The creatures that emerge are changing the landscape and the people they touch.",
                "The rift connects to the Abyss, the Far Realm, or another dangerous plane. " +
                "Closing it requires a ritual, artifacts, or a sacrifice. The creatures are a symptom, not the cause.", 1, 15));

        hooks.add(createHook("The Cursed Bloodline", "Supernatural", "Horror", "Soon",
                null, null, null,
                "A curse has plagued a noble family for generations: every heir dies on their 21st birthday. " +
                "The current heir has come to you for help, and they have only weeks left. The curse is tied to " +
                "an ancient sin committed by the family's founder, and breaking it will require confronting the past.",
                "The curse was placed by a wronged spirit, a fey entity, or a rival family. " +
                "Breaking it requires atonement, a quest, or a dangerous ritual.", 1, 12));

        hooks.add(createHook("The God's Descent", "Supernatural", "High Magic", "Eventual",
                null, null, "Cleric",
                "A god has descended to the Material Plane, but not in their true form. They appear as a mortal, " +
                "seeking help for a problem that even divine power cannot solve. Other gods are watching, and their " +
                "agents — both allies and enemies — will interfere with your quest to help the fallen deity.",
                "The god was wounded or trapped by a greater power. Their mortal form is vulnerable. " +
                "Other gods have conflicting agendas, and the characters must navigate divine politics.", 5, 20));

        hooks.add(createHook("The Dream Walker", "Supernatural", "High Magic", "Soon",
                null, null, "Wizard",
                "People in the city are being trapped in their dreams, their bodies catatonic while their minds " +
                "wander a shared dreamscape. A powerful entity is building a kingdom in the dream world, and it needs " +
                "subjects. You can enter the dreamscape, but the rules of reality don't apply there.",
                "The dream entity is a powerful archfey, a mind flayer, or a demigod. " +
                "The dreamscape reflects the fears and desires of those trapped within it.", 3, 15));

        // ==================== Dungeon Crawl Hooks ====================

        hooks.add(createHook("The Deep Delve", "Exploration", "Dungeon Crawl", "Eventual",
                null, "Dwarf", null,
                "Dwarven legends speak of a deep delve — a mine that goes deeper than any other, reaching the " +
                "heart of the mountain where the first dwarves forged their weapons. Recently, tremors have opened " +
                "a new passage, and the sounds of machinery — ancient machinery — can be heard from below.",
                "The deep delve contains ancient dwarven technology, traps, and guardians. " +
                "Something woke the machinery, and it's not friendly.", 1, 15));

        hooks.add(createHook("The Tomb of the Forgotten King", "Exploration", "Dungeon Crawl", "Soon",
                "Sage", null, null,
                "Historical records mention a king who was erased from history — no monuments, no descendants, " +
                "no mention in any chronicle. But you've found evidence of a tomb, hidden beneath a ruined temple. " +
                "The king was forgotten for a reason, and his tomb is protected by wards that have held for millennia.",
                "The king was a tyrant, a warlock, or a vessel for an ancient evil. " +
                "The tomb's traps and guardians are designed to keep the king buried forever.", 3, 15));

        hooks.add(createHook("The Lich's Lair", "Combat", "Dungeon Crawl", "Immediate",
                null, null, "Wizard",
                "Rumors speak of a lich who has been gathering power in a hidden lair. The undead in the region " +
                "are growing bolder, and magic itself feels corrupted near the lair's location. Other adventurers " +
                "have entered — none have returned. The lich is close to completing a ritual that will change everything.",
                "The lich's lair is a dungeon of magical traps, undead guardians, and planar anomalies. " +
                "The ritual will open a portal, raise an army, or transform the lich into something greater.", 5, 20));

        // ==================== Steampunk Hooks ====================

        hooks.add(createHook("The Airship Heist", "Combat", "Steampunk", "Immediate",
                null, null, null,
                "A luxury airship carrying the region's elite is flying a route through dangerous skies. " +
                "You've been hired to protect it — or to board it and steal the most valuable passenger. " +
                "Either way, the skies are filled with enemies, and the airship's secrets run deep.",
                "The airship carries more than passengers — experimental weapons, stolen artifacts, or a captive entity. " +
                "Sky pirates, rival nations, and supernatural threats all want what's on board.", 3, 15));

        hooks.add(createHook("The Clockwork City", "Exploration", "Steampunk", "Eventual",
                null, null, null,
                "Legends speak of a city built entirely of clockwork and steam, hidden in a valley shrouded in perpetual fog. " +
                "Recently, the fog has lifted, revealing the city's spires — and the fact that it's still running, " +
                "manned by automatons that have been waiting centuries for visitors.",
                "The city was abandoned after a catastrophe, but its systems still function. " +
                "The automatons follow their last orders, and the city's core holds a power source of immense potential.", 1, 15));

        // ==================== Feywild Hooks ====================

        hooks.add(createHook("The Fey Bargain", "Social", "Feywild", "Soon",
                null, "Elf", null,
                "A fey noble offers you a boon — power, wealth, or knowledge beyond mortal comprehension. " +
                "The price seems small: a simple task, a trinket, a promise. But fey bargains are never what they seem, " +
                "and the true cost will only become clear when it's too late to back out.",
                "The fey noble's true goal is to use the characters as pawns in a fey conflict. " +
                "The bargain's hidden clauses will create complications and moral dilemmas.", 1, 12));

        hooks.add(createHook("The Wild Hunt", "Combat", "Feywild", "Immediate",
                null, null, null,
                "The Wild Hunt has been sighted in the region — a spectral procession of riders on horseback, " +
                "led by a horned figure who hunts anything in their path. Those caught in the Hunt are dragged to " +
                "the Feywild, never to return. The Hunt is growing bolder, and the material world is in danger.",
                "The Wild Hunt has crossed into the material plane due to a breach or a summons. " +
                "Stopping it requires finding the leader, closing the breach, or making a bargain with the fey.", 5, 20));

        // More hooks for variety

        hooks.add(createHook("The Plague Bearer", "Personal", "Horror", "Immediate",
                null, null, "Cleric",
                "A plague is spreading through the region, and the local clergy blame you — or your order — for bringing it. " +
                "The plague's symptoms are unnatural: victims speak in tongues, their eyes glow, and they don't die. " +
                "You must find the source and clear your name before the mob comes for you.",
                "The plague is a magical or divine curse, not a natural disease. " +
                "It was unleashed by a rival faith or a corrupted deity. The cleric must find the source and a cure.", 1, 12));

        hooks.add(createHook("The Artifact's Call", "Exploration", "High Magic", "Eventual",
                null, null, "Wizard",
                "An ancient artifact is calling to you across great distances. Only you can hear its whisper, " +
                "and it grows louder each day. Other mages sense the artifact's power and are searching for it too. " +
                "The artifact's location is hidden in riddles and prophecies that only you can decipher.",
                "The artifact is a powerful magical item that chose the wizard as its wielder. " +
                "Other mages will stop at nothing to claim it. The artifact's power comes with a price.", 3, 20));

        hooks.add(createHook("The Monster's Lure", "Combat", "Horror", "Soon",
                "Folk Hero", null, null,
                "A monster has been attacking travelers on the road, but the attacks follow a pattern — " +
                "the monster is herding victims toward a specific location. The local militia is overwhelmed, " +
                "and the monster's lair is a place of darkness and dread.",
                "The monster is collecting victims for a ritual, a meal, or a greater purpose. " +
                "The lair contains clues about the monster's origin and weaknesses.", 1, 10));

        hooks.add(createHook("The Pirate's Treasure", "Exploration", "Sandbox", "Eventual",
                "Sailor", null, null,
                "A legendary pirate's treasure map has surfaced, and every pirate crew in the region is racing to find it first. " +
                "The map leads through dangerous waters, past sea monsters and rival ships, to an island that shouldn't exist. " +
                "The treasure is real, but so are the traps and guardians that protect it.",
                "The pirate's treasure includes magical items and secrets about the pirate's final days. " +
                "The island is protected by curses, traps, and the pirate's undead crew.", 1, 12));

        hooks.add(createHook("The Academy's Secret", "Mystery", "High Magic", "Soon",
                null, null, "Wizard",
                "The magical academy where you trained has a secret: its founders built it atop a seal that contains " +
                "an ancient evil. The seal is weakening, and strange occurrences are plaguing the academy. " +
                "Students are disappearing, and the faculty is divided on how to respond.",
                "The ancient evil is stirring, and the academy's magic is both the seal and the weakness. " +
                "The characters must strengthen the seal or find another way to contain the threat.", 3, 15));

        hooks.add(createHook("The War's End", "Political", "Sandbox", "Immediate",
                "Soldier", null, null,
                "The war that defined your life has ended — but the peace treaty is a lie. Both sides are preparing " +
                "for a final, devastating strike, and only a handful of people know the truth. You're one of them, " +
                "and you must stop the war from reigniting before millions die.",
                "The peace treaty is a ruse by leaders on both sides who profit from war. " +
                "The characters must expose the truth and prevent the final strike.", 5, 20));

        hooks.add(createHook("The Spirit's Plea", "Supernatural", "Horror", "Soon",
                null, null, null,
                "A spirit appears to you in your sleep, begging for help. It was once a mortal, but now it's trapped " +
                "between life and death, and the thing that killed it is still hunting. The spirit knows the killer's " +
                "identity and location, but it can only communicate through dreams.",
                "The spirit was murdered by a powerful entity — a vampire, a lich, or a demon. " +
                "Freeing the spirit requires finding and destroying the killer.", 1, 12));

        hooks.add(createHook("The Champion's Trial", "Combat", "Sandbox", "Eventual",
                null, null, "Fighter",
                "A legendary champion's armor has been discovered, and legend says it chooses its wielder. " +
                "To claim it, you must pass a series of trials — physical, mental, and moral. " +
                "Other warriors are competing, and the trials are deadly.",
                "The trials test the warrior's worthiness and reveal the champion's legacy. " +
                "The armor's power is tied to the champion's spirit, which still guards it.", 3, 15));

        hooks.add(createHook("The Oracle's Prophecy", "Mystery", "Supernatural", "Soon",
                null, null, null,
                "An oracle has delivered a prophecy that predicts the end of the world — or the birth of a new age. " +
                "The prophecy is cryptic, and different factions interpret it differently. Some want to fulfill it; " +
                "others want to prevent it. You're caught in the middle, and the oracle has chosen you as the key.",
                "The prophecy is self-fulfilling or self-negating depending on the characters' actions. " +
                "The oracle may be genuine, manipulated, or a pawn of a larger power.", 1, 15));

        hooks.add(createHook("The Shadow Market", "Social", "Steampunk", "Eventual",
                "Criminal", null, null,
                "Whispers speak of a market that exists in the shadows, where anything can be bought or sold — " +
                "including memories, souls, and futures. The market appears only to those who know how to find it, " +
                "and its wares are both miraculous and dangerous.",
                "The market is run by fey, devils, or planar traders. " +
                "Each item has a hidden cost, and the market's true purpose is to harvest something from its customers.", 3, 15));

        hookRepository.saveAll(hooks);
        log.info("Populated {} adventure hooks", hooks.size());
    }

    Dnd5eAdventureHook createHook(String name, String type, String theme, String urgency,
                                  String targetBackground, String targetRace, String targetClass,
                                  String narrative, String dmNotes, int minLevel, int maxLevel) {
        Dnd5eAdventureHook hook = new Dnd5eAdventureHook();
        hook.setName(name);
        hook.setType(type);
        hook.setTheme(theme);
        hook.setUrgency(urgency);
        hook.setTargetBackground(targetBackground);
        hook.setTargetRace(targetRace);
        hook.setTargetClass(targetClass);
        hook.setNarrative(narrative);
        hook.setDmNotes(dmNotes);
        hook.setMinLevel(minLevel);
        hook.setMaxLevel(maxLevel);
        hook.setActive(true);
        return hook;
    }
}

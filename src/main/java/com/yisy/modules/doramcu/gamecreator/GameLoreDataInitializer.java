package com.yisy.modules.doramcu.gamecreator;
import com.yisy.modules.doramcu.model.players.Player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Initializes the universal game lore database with popular game lores on startup.
 * <p>
 * This component pre-loads the most popular game universes so that players
 * can immediately select from a rich catalog when creating new games.
 * If a requested lore is not in this list, it will be fetched from the internet
 * via {@link GameLoreService#fetchLoreFromInternet(String)}.
 * </p>
 * <p>
 * Pre-loaded lores include:
 * <ul>
 *   <li>Sci-Fi: Star Wars, Star Trek, Mass Effect, Dune, Warhammer 40K, The Expanse, Halo, etc.</li>
 *   <li>Fantasy: D&D Forgotten Realms, Lord of the Rings, World of Warcraft, Elder Scrolls, etc.</li>
 *   <li>Horror: Alien, Dead Space, Warhammer Age of Sigmar, etc.</li>
 *   <li>Other: Firefly, Babylon 5, Stargate, Red Dwarf, etc.</li>
 * </ul>
 * </p>
 */
@Component
@Order(1)
public class GameLoreDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GameLoreDataInitializer.class);

    private final GameLoreRepository gameLoreRepository;

    public GameLoreDataInitializer(GameLoreRepository gameLoreRepository) {
        this.gameLoreRepository = gameLoreRepository;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing universal game lore database...");

        List<GameLore> lores = new ArrayList<>();

        // --- SCI-FI SPACE OPERA ---

        // Star Wars
        lores.add(createLore(
                "Star Wars", "star-wars", "SCI_FI", "SPACE_OPERA",
                "A galaxy far, far away divided between the light and dark sides of the Force.",
                "The Star Wars universe spans a galaxy divided between the Jedi and Sith, " +
                "the Rebel Alliance and the Galactic Empire. Set thousands of years in the future, " +
                "it features space battles, ground combat, and the mystical Force that empowers Jedi Knights.",
                "FAR_FUTURE", "ADVENTUROUS",
                "[\"Rebel Alliance\", \"Galactic Empire\", \"Jedi Order\", \"Sith\", \"Mandalorians\", \"Hutts\", \"Moff Tarkin's Loyals\"]",
                "[\"Humans\", \"Zabrak\", \"Twi'lek\", \"Wookiees\", \"Mon Calamari\", \"Trandoshans\", \"Gungans\", \"Droids\"]",
                "LORE_SPECIFIC", "SPIRAL", 5,
                "[\"RPG\", \"4X\", \"TACTICAL\"]",
                true, true, true, true, false,
                true, true, true, false, true, true
        ));

        // Star Trek
        lores.add(createLore(
                "Star Trek", "star-trek", "SCI_FI", "SPACE_OPERA",
                "Humanity's future among the stars through exploration, diplomacy, and the United Federation of Planets.",
                "The Star Trek universe follows humanity's expansion into space through the United Federation of Planets, " +
                "Starfleet, and encounters with alien species like the Klingons, Romulans, and Borg. " +
                "Emphasizes exploration, diplomacy, and scientific discovery.",
                "FAR_FUTURE", "HOPEFUL",
                "[\"United Federation of Planets\", \"Klingon Empire\", \"Romulan Star Empire\", \"Cardassian Union\", \"Dominion\", \"Borg Collective\"]",
                "[\"Humans\", \"Vulcans\", \"Klingons\", \"Romulans\", \"Betazoids\", \"Andorians\", \"Borg\"]",
                "LORE_SPECIFIC", "SPIRAL", 4,
                "[\"RPG\", \"4X\", \"SIMULATION\"]",
                true, true, true, false, false,
                false, true, false, true, true, true
        ));

        // Mass Effect
        lores.add(createLore(
                "Mass Effect", "mass-effect", "SCI_FI", "SPACE_OPERA",
                "A galactic council of species faces the ancient threat of the Reapers.",
                "The Mass Effect universe features a galaxy united under the Citadel Council of Turians, Asari, and Salarians, " +
                "threatened by the ancient machine intelligence known as the Reapers who harvest advanced civilizations every 50,000 years. " +
                "Features the Normandy SR-2, Commander Shepard, and the powerful Element Zero technology.",
                "FAR_FUTURE", "SERIOUS",
                "[\"Citadel Council\", \"Turian Hierarchy\", \"Asari Republics\", \"Salarian Union\", \"Krogan Uprising\", \"Batarian Empire\", \"Reapers\"]",
                "[\"Humans\", \"Asari\", \"Turians\", \"Salarians\", \"Krogan\", \"Elcor\", \"Drell\", \"Volus\"]",
                "LORE_SPECIFIC", "SPIRAL", 5,
                "[\"RPG\", \"4X\", \"TACTICAL\"]",
                true, true, true, true, false,
                true, true, true, false, true, true
        ));

        // Warhammer 40,000
        lores.add(createLore(
                "Warhammer 40,000", "warhammer-40k", "SCI_FI", "DARK_SPACE_OPERA",
                "In the grim darkness of the far future, there is only war.",
                "The Warhammer 40,000 universe is set in the 41st millennium where humanity's Imperium of Man " +
                "fights constant wars against alien races, chaos forces, and mutant threats. " +
                "The galaxy is a dark, gothic sci-fi setting with space marines, eldar, orks, necrons, and tyranids.",
                "FAR_FUTURE", "GRIMDARK",
                "[\"Imperium of Man\", \"Chaos Gods\", \"Aeldari\", \"Drukhari\", \"Orks\", \"Necrons\", \"Tyranids\", \"T'au Empire\"]",
                "[\"Humans\", \"Space Marines\", \"Aeldari\", \"Orks\", \"Necrons\", \"Tyranids\", \"T'au\", \"Chosen\"]",
                "LORE_SPECIFIC", "SPIRAL", 8,
                "[\"RPG\", \"4X\", \"TACTICAL\", \"SURVIVAL\"]",
                true, true, false, true, true,
                false, true, true, true, false, true
        ));

        // Dune
        lores.add(createLore(
                "Dune", "dune", "SCI_FI", "SPACE_OPERA",
                "The desert planet Arrakis and the spice melange that controls the fate of the universe.",
                "Frank Herbert's Dune universe is set around the desert planet Arrakis, the sole source of the spice melange — " +
                "the most valuable substance in the known universe. The great houses, the Bene Gesserit sisterhood, " +
                "the Spacing Guild, and the Emperor vie for control of the spice and the fate of the galaxy.",
                "FAR_FUTURE", "SERIOUS",
                "[\"House Atreides\", \"House Harkonnen\", \"Bene Gesserit\", \"Spacing Guild\", \"Emperor's Forces\", \"Fremen\"]",
                "[\"Humans\", \"Fremen\", \"Sandworms\"]",
                "LORE_SPECIFIC", "SPIRAL", 4,
                "[\"RPG\", \"4X\", \"TACTICAL\"]",
                true, true, true, true, false,
                false, false, false, true, true, false
        ));

        // The Expanse
        lores.add(createLore(
                "The Expanse", "the-expanse", "SCI_FI", "SPACE_OPERA",
                "Humanity's future in the solar system threatened by the mysterious Protomolecule.",
                "The Expanse universe is set in a colonized solar system where Earth, Mars, and the Belt " +
                "are in a cold war. The discovery of the Protomolecule — an alien substance — " +
                "threatens to change the balance of power and reveal ancient alien secrets.",
                "NEAR_FUTURE", "SERIOUS",
                "[\"United Nations\", \"Mars Colonial Navy\", \"Belt Free Navy\", \"Free Navy\", \"Protogen\"]",
                "[\"Earth humans\", \"Martians\", \"Belters\"]",
                "LORE_SPECIFIC", "SPIRAL", 3,
                "[\"RPG\", \"4X\", \"TACTICAL\"]",
                true, true, true, true, false,
                false, false, false, true, true, true
        ));

        // Halo
        lores.add(createLore(
                "Halo", "halo", "SCI_FI", "MILITARY_SCI_FI",
                "Humanity's last stand against the Covenant and the ancient Forerunner legacy.",
                "The Halo universe follows humanity's war against the Covenant — a theocratic alliance of alien races — " +
                "and the discovery of the Halo rings, ancient weapons built by the Forerunners to destroy the Flood. " +
                "Features Master Chief, the Spartans, and the enigmatic Cortana AI.",
                "FAR_FUTURE", "SERIOUS",
                "[\"UNSC\", \"Covenant\", \"Flood\", \"Prometheans\", \"San'Shyuum Prophets\"]",
                "[\"Humans\", \"Spartans\", \"Elites\", \"Grunts\", \"Jackals\", \"Hunters\", \"Sangheili\"]",
                "LORE_SPECIFIC", "SPIRAL", 4,
                "[\"RPG\", \"4X\", \"TACTICAL\", \"SURVIVAL\"]",
                true, true, false, true, true,
                true, true, true, false, true, true
        ));

        // Babylon 5
        lores.add(createLore(
                "Babylon 5", "babylon-5", "SCI_FI", "SPACE_OPERA",
                "A massive space station serves as a diplomatic hub before a galactic war.",
                "The Babylon 5 universe centers on a massive space station serving as a diplomatic hub " +
                "between the Earth Alliance, Minbari, Centauri, Narn, and Vorlons. " +
                "A carefully planned five-year saga of political intrigue, ancient prophecies, and galactic war.",
                "FAR_FUTURE", "SERIOUS",
                "[\"Earth Alliance\", \"Minbari Federation\", \"Centauri Republic\", \"Narn Regime\", \"Vorlons\", \"Shadow Council\"]",
                "[\"Humans\", \"Minbari\", \"Centauri\", \"Narn\", \"Minbari Warriors\"]",
                "LORE_SPECIFIC", "SPIRAL", 6,
                "[\"RPG\", \"4X\"]",
                true, true, true, false, false,
                false, true, false, true, true, false
        ));

        // Firefly
        lores.add(createLore(
                "Firefly", "firefly", "SCI_FI", "SPACE_WESTERN",
                "A ragtag crew aboard the ship Serenity navigates the lawless borders of space.",
                "The Firefly universe is set after the Unification War between Earth and the Planets. " +
                "The Alliance controls the core worlds, while the border worlds are lawless frontiers. " +
                "Follows Captain Malcolm Reynolds and the crew of Serenity as they take on various jobs.",
                "FAR_FUTURE", "ADVENTUROUS",
                "[\"The Alliance\", \"Independents\", \"Reavers\"]",
                "[\"Humans\", \"Reavers\"]",
                "LORE_SPECIFIC", "SPIRAL", 2,
                "[\"RPG\", \"ADVENTURE\", \"SURVIVAL\"]",
                true, false, true, false, true,
                true, false, false, false, true, true
        ));

        // Stargate
        lores.add(createLore(
                "Stargate", "stargate", "SCI_FI", "SPACE_OPERA",
                "Ancient alien portals connect Earth to thousands of worlds across the galaxy.",
                "The Stargate universe begins when humans discover an ancient alien device — the Stargate — " +
                "that connects to thousands of worlds across the galaxy. Humanity faces threats from the Goa'uld, " +
                "Ori, and System Lords while allying with the Asgard, Tok'ra, and other free worlds.",
                "FAR_FUTURE", "ADVENTUROUS",
                "[\"SGC\", \"Goa'uld Alliance\", \"Jaffa\", \"Tok'ra\", \"Asgard\", \"Ori\", \"Ancients\"]",
                "[\"Humans\", \"Jaffa\", \"Goa'uld\", \"Asgard\", \"Ancients\", \"Wraith\"]",
                "LORE_SPECIFIC", "SPIRAL", 5,
                "[\"RPG\", \"4X\", \"TACTICAL\"]",
                true, true, true, true, false,
                true, true, true, false, true, true
        ));

        // Red Dwarf
        lores.add(createLore(
                "Red Dwarf", "red-dwarf", "SCI_FI", "COMEDY_SCI_FI",
                "The last two humans in the universe aboard a mining ship adrift in space.",
                "The Red Dwarf universe follows Dave Lister, the last human (supposedly), " +
                "and his cat in a body, aboard a mining ship that drifts through space after a radiation leak. " +
                "A comedic sci-fi setting with time travel, parallel universes, and alien encounters.",
                "FAR_FUTURE", "SATIRICAL",
                "[\"Red Dwarf Crew\", \"Arnolds\", \"Sentients\"]",
                "[\"Humans\", \"Arnolds\", \"Kryten (android)\"]",
                "LORE_SPECIFIC", "SPIRAL", 1,
                "[\"RPG\", \"ADVENTURE\", \"SANDBOX\"]",
                true, false, false, false, false,
                true, false, true, false, true, false
        ));

        // --- FANTASY ---

        // D&D Forgotten Realms
        lores.add(createLore(
                "D&D: Forgotten Realms", "dnd-forgotten-realms", "FANTASY", "HIGH_FANTASY",
                "The iconic Dungeons & Dragons campaign setting of Faerûn.",
                "The Forgotten Realms is the most popular Dungeons & Dragons campaign setting, " +
                "centered on the continent of Faerûn on the world of Toril. Features diverse races, " +
                "powerful deities, ancient magic, and endless adventure opportunities from dungeons to dragon lairs.",
                "MAGIC", "ADVENTUROUS",
                "[\"Harpers\", \"Order of the Gauntlet\", \"Lords' Alliance\", \"Cult of the Dragon\", \"Red Wizards of Thay\"]",
                "[\"Humans\", \"Elves\", \"Dwarves\", \"Halflings\", \"Gnomes\", \"Dragonborn\", \"Tieflings\", \"Goliaths\"]",
                "MYTHOLOGICAL", "SPIRAL", 6,
                "[\"RPG\", \"ADVENTURE\", \"TACTICAL\"]",
                true, true, false, true, false,
                true, true, true, false, true, false
        ));

        // Lord of the Rings
        lores.add(createLore(
                "Lord of the Rings", "lord-of-the-rings", "FANTASY", "HIGH_FANTASY",
                "Middle-earth's final battle between the forces of good and Sauron's darkness.",
                "J.R.R. Tolkien's Middle-earth spans from the Shire to Mordor, featuring the War of the Ring " +
                "against Sauron's forces. Elves, Dwarves, Men, and Hobbits unite to destroy the One Ring " +
                "and end the Dark Lord's reign of terror.",
                "MAGIC", "SERIOUS",
                "[\"Free Peoples\", \"Gondor\", \"Rohan\", \"Rivendell\", \"Mordor\", \"Isengard\"]",
                "[\"Men\", \"Elves\", \"Dwarves\", \"Hobbits\", \"Ents\", \"Orcs\", \"Uruk-hai\"]",
                "MYTHOLOGICAL", "SPIRAL", 4,
                "[\"RPG\", \"4X\", \"TACTICAL\", \"ADVENTURE\"]",
                true, true, false, true, false,
                true, false, true, false, true, false
        ));

        // World of Warcraft
        lores.add(createLore(
                "World of Warcraft", "world-of-warcraft", "FANTASY", "HIGH_FANTASY",
                "The eternal conflict between the Alliance and Horde across Azeroth.",
                "The World of Warcraft universe spans the world of Azeroth and beyond, " +
                "centered on the eternal conflict between the Alliance and the Horde. " +
                "Features diverse races, epic raids, world events, and the threat of the Burning Legion.",
                "MAGIC", "ADVENTUROUS",
                "[\"Alliance\", \"Horde\", \"Burning Legion\", \"Old Gods\", \"Night Elves\", \"Undead Scourge\"]",
                "[\"Humans\", \"Orcs\", \"Elves\", \"Dwarves\", \"Undead\", \"Tauren\", \"Trolls\", \"Gnomes\"]",
                "MYTHOLOGICAL", "SPIRAL", 6,
                "[\"RPG\", \"4X\", \"MMORPG\", \"TACTICAL\"]",
                true, true, true, true, false,
                true, true, true, false, true, false
        ));

        // The Elder Scrolls
        lores.add(createLore(
                "The Elder Scrolls", "elder-scrolls", "FANTASY", "HIGH_FANTASY",
                "The mystical world of Tamriel with its nine Divines and the Dragonborn prophecy.",
                "The Elder Scrolls universe spans the continent of Tamriel with its provinces of Skyrim, " +
                "Morrowind, Hammerfell, and more. Features the Dragonborn prophecy, the Nine Divines, " +
                "ancient Dwemer ruins, and the eternal struggle against Daedric princes.",
                "MAGIC", "ADVENTUROUS",
                "[\"Empire\", \"Thalmor\", \"Dawnguard\", \"Dark Brotherhood\", \"Mages Guild\", \"College of Winterhold\"]",
                "[\"Nords\", \"Altmer\", \"Dark Elves\", \"Imperials\", \"Redguards\", \"Argonians\", \"Khajiit\"]",
                "MYTHOLOGICAL", "SPIRAL", 5,
                "[\"RPG\", \"ADVENTURE\", \"SANDBOX\"]",
                true, true, false, false, false,
                true, true, true, false, true, false
        ));

        // Warhammer Fantasy
        lores.add(createLore(
                "Warhammer Fantasy", "warhammer-fantasy", "FANTASY", "DARK_FANTASY",
                "The Old World teeters on the brink of destruction from chaos and dark forces.",
                "The Warhammer Fantasy universe is a dark, gritty medieval world where the Empire of Man " +
                "fights against Chaos from the North, the Dark Elves of Naggaroth, the Skaven of the Under-Empire, " +
                "and the hordes of Greenskins. Magic is dangerous and the gods are capricious.",
                "MAGIC", "GRIMDARK",
                "[\"Empire\", \"Chaos Wastes\", \"Bretonnia\", \"Dwarfs\", \"Elves\", \"Dark Elves\", \"Skaven\", \"Orcs & Goblins\"]",
                "[\"Humans\", \"Dwarfs\", \"Elves\", \"Dark Elves\", \"Skaven\", \"Orcs\", \"Goblins\", \"Chaos Spawn\"]",
                "MYTHOLOGICAL", "SPIRAL", 8,
                "[\"RPG\", \"4X\", \"TACTICAL\", \"SURVIVAL\"]",
                true, true, false, true, true,
                false, false, true, false, false, false
        ));

        // --- HORROR ---

        // Alien
        lores.add(createLore(
                "Alien", "alien", "HORROR", "SCI_FI_HORROR",
                "The terrifying Xenomorph threat in deep space.",
                "The Alien universe follows encounters with the Xenomorph — a perfect killing machine " +
                "created by the ancient Engineers. Weyland-Yutani Corporation seeks to weaponize the creatures, " +
                "while survivors like Ripley fight to escape the nightmare.",
                "FAR_FUTURE", "HORROR",
                "[\"Weyland-Yutani\", \"USCSS Crews\", \"Nomads\", \"Engineers\"]",
                "[\"Humans\", \"Xenomorphs\", \"Engineers\", \"Neomorphs\"]",
                "LORE_SPECIFIC", "SPIRAL", 2,
                "[\"RPG\", \"SURVIVAL\", \"TACTICAL\"]",
                true, false, false, true, true,
                false, false, true, false, false, true
        ));

        // Dead Space
        lores.add(createLore(
                "Dead Space", "dead-space", "HORROR", "SCI_FI_HORROR",
                "The Marker artifact drives survivors to madness on deep space mining stations.",
                "The Dead Space universe follows the terrifying discovery of the Marker — an ancient artifact " +
                "that drives those near it to madness and death, transforming corpses into deadly Necromorphs. " +
                "Set aboard the USG Ishimura and other deep space vessels.",
                "FAR_FUTURE", "HORROR",
                "[\"United Consumers Habitation Coalition\", \"Consumer Construct\", \"Unitology Cult\"]",
                "[\"Humans\", \"Necromorphs\"]",
                "LORE_SPECIFIC", "SPIRAL", 1,
                "[\"RPG\", \"SURVIVAL\"]",
                true, false, false, false, true,
                false, false, true, false, false, true
        ));

        // --- POST-APOCALYPTIC ---

        // Fallout
        lores.add(createLore(
                "Fallout", "fallout", "POST_APOCALYPTIC", "RETRO_FUTURISM",
                "The wasteland after nuclear war, with vaults and mutated creatures.",
                "The Fallout universe is set in a retro-futuristic post-nuclear wasteland where survivors " +
                "live in vaults and on the surface among mutated creatures, raiders, and remnants of the old world. " +
                "Features the Brotherhood of Steel, NCR, and the mysterious Institute.",
                "NEAR_FUTURE", "ADVENTUROUS",
                "[\"Brotherhood of Steel\", \"NCR\", \"The Institute\", \"Railroad\", \"Brotherhood of Steel East Coast\"]",
                "[\"Humans\", \"Ghouls\", \"Super Mutants\", \"Synths\", \"Deathclaws\"]",
                "LORE_SPECIFIC", "SPIRAL", 4,
                "[\"RPG\", \"SURVIVAL\", \"SANDBOX\", \"TACTICAL\"]",
                true, true, true, true, true,
                true, true, true, true, true, true
        ));

        // --- ADDITIONAL POPULAR LORES ---

        // Avatar
        lores.add(createLore(
                "Avatar", "avatar", "SCI_FI", "SPACE_OPERA",
                "The moon Pandora and its native Na'vi people fighting for their homeland.",
                "The Avatar universe is set on Pandora, a moon of the gas giant Polyphemus, " +
                "home to the Na'vi — a sentient species connected to their environment through the neural network Eywa. " +
                "The RDA corporation seeks to mine the valuable unobtanium while the Na'vi fight to protect their home.",
                "FAR_FUTURE", "HOPEFUL",
                "[\"RDA\", \"Na'vi Clans\", \"RDA Security\"]",
                "[\"Humans\", \"Na'vi\", \"Recoms\"]",
                "LORE_SPECIFIC", "SPIRAL", 2,
                "[\"RPG\", \"4X\", \"TACTICAL\"]",
                true, true, true, true, false,
                false, false, false, true, true, true
        ));

        // Transformers
        lores.add(createLore(
                "Transformers", "transformers", "SCI_FI", "ROBOT_SCI_FI",
                "The eternal war between Autobots and Decepticons across the galaxy.",
                "The Transformers universe follows the ancient war between the Autobots and Decepticons " +
                "on the planet Cybertron. The conflict spreads across the galaxy as both sides seek " +
                "the AllSpark and other powerful artifacts to gain the upper hand.",
                "FAR_FUTURE", "ADVENTUROUS",
                "[\"Autobots\", \"Decepticons\", \"Maximals\", \"Predacons\"]",
                "[\"Cybertronians\", \"Maximals\", \"Predacons\"]",
                "LORE_SPECIFIC", "SPIRAL", 2,
                "[\"RPG\", \"4X\", \"TACTICAL\"]",
                true, true, true, true, false,
                true, false, true, false, false, true
        ));

        // Pokemon
        lores.add(createLore(
                "Pokemon", "pokemon", "FANTASY", "CREATURE_COLLECTING",
                "A world where humans partner with magical creatures called Pokemon.",
                "The Pokemon universe features a world where humans partner with magical creatures called Pokemon " +
                "for battle, exploration, and friendship. Trainers travel across regions, " +
                "collecting Pokemon and challenging gyms to become Champion.",
                "MODERN", "ADVENTUROUS",
                "[\"Pokemon League\", \"Team Rocket\", \"Team Magma\", \"Team Aqua\", \"Galar Academy\"]",
                "[\"Humans\", \"Pokemon\"]",
                "LORE_SPECIFIC", "SPIRAL", 3,
                "[\"RPG\", \"ADVENTURE\", \"MMORPG\"]",
                true, false, true, false, false,
                true, true, false, false, true, false
        ));

        // Minecraft
        lores.add(createLore(
                "Minecraft", "minecraft", "FANTASY", "SANDBOX",
                "A blocky world of infinite creativity, survival, and adventure.",
                "The Minecraft universe is a procedurally generated block world where players can build, " +
                "explore, survive, and create. Features the Overworld, Nether, and End dimensions " +
                "with creatures like Creepers, Ender Dragons, and the Wither.",
                "MAGIC", "ADVENTUROUS",
                "[\"Villagers\", \"Illagers\", \"Ender Dragon\", \"Wither\"]",
                "[\"Humans (Steve/Alex)\", \"Villagers\", \"Endermen\", \"Creepers\", \"Zombies\", \"Skeletons\"]",
                "NUMERICAL", "SPIRAL", 1,
                "[\"SANDBOX\", \"SURVIVAL\", \"ADVENTURE\", \"TYCOON\"]",
                true, false, true, false, true,
                true, true, true, false, true, false
        ));

        // Destiny
        lores.add(createLore(
                "Destiny", "destiny", "SCI_FI", "LOOT_SHOOTER",
                "Guardians defend the last city on Earth from alien threats and the Darkness.",
                "The Destiny universe follows the Guardians — warriors chosen by the Traveler — " +
                "to defend the last city on Earth from alien threats including the Vex, Cabal, Hive, " +
                "and the forces of the Darkness. Set in a sci-fi world with magical powers called Light.",
                "FAR_FUTURE", "SERIOUS",
                "[\"Tower\", \"Vex\", \"Cabal Empire\", \"Hive\", \"Shadow Legion\"]",
                "[\"Humans\", \"Awoken\", \"Exo\", \"Vex\", \"Cabal\", \"Hive\"]",
                "LORE_SPECIFIC", "SPIRAL", 4,
                "[\"RPG\", \"MMORPG\", \"TACTICAL\"]",
                true, true, false, true, false,
                true, true, true, false, false, true
        ));

        // Genshin Impact
        lores.add(createLore(
                "Genshin Impact", "genshin-impact", "FANTASY", "OPEN_WORLD_FANTASY",
                "The magical world of Teyvat with its seven nations and elemental powers.",
                "The Genshin Impact universe is set in the fantasy world of Teyvat, " +
                "divided into seven nations each governed by an Archon and associated with an element. " +
                "The Traveler searches for their lost sibling while uncovering the secrets of Teyvat.",
                "MAGIC", "ADVENTUROUS",
                "[\"Celestia\", \"Fatui\", \"Knights of Favonius\", \"Liyue Qixing\", \"Sumeru Akademiya\"]",
                "[\"Humans\", \"Adepti\", \"Visions bearers\"]",
                "MYTHOLOGICAL", "SPIRAL", 5,
                "[\"RPG\", \"ADVENTURE\"]",
                true, false, false, false, false,
                true, true, true, false, true, false
        ));

        // --- EARTH 2026 (DoraMCU Default) ---

        // Earth 2026 — the default starting hub for all characters
        lores.add(createLore(
                "Earth 2026", "earth-2026", "SCI_FI", "MODERN",
                "Modern-day Earth — the starting hub for all characters before transitioning to other galaxies.",
                "Earth 2026 is the default starting point in DoraMCU. Characters begin here in a modern-day setting " +
                "with real-world locations, corporations, and organizations before choosing to travel to other game " +
                "galaxies through portals. Features modern firearms, urban environments, and contemporary scenarios.",
                "NEAR_FUTURE", "REALISTIC",
                "[\"Younger Industries\", \"UN\", \"Corporate Security\", \"Government Agencies\"]",
                "[\"Humans\"]",
                "REAL_WORLD", "SINGLE_PLANET", 1,
                "[\"RPG\", \"4X\", \"TYCOON\", \"TACTICAL\", \"SURVIVAL\", \"SANDBOX\", \"MMORPG\", \"ROGUELIKE\", \"SIMULATION\", \"ADVENTURE\", \"FPS\"]",
                true, true, true, true, true,
                true, true, true, true, true, true
        ));

        log.info("Pre-loading {} game lores into the database...", lores.size());

        int saved = 0;
        for (GameLore lore : lores) {
            if (!gameLoreRepository.existsBySlug(lore.getSlug())) {
                lore.setActive(true);
                lore.setInternetSourced(false);
                lore.setCreatedAt(LocalDateTime.now());
                lore.setUpdatedAt(LocalDateTime.now());
                gameLoreRepository.save(lore);
                saved++;
            }
        }

        log.info("Pre-loaded {} new game lores ({} already existed)", saved, lores.size() - saved);
    }

    /**
     * Helper method to create a GameLore entry with all common fields.
     */
    private GameLore createLore(
            String name, String slug, String genre, String category,
            String shortDescription, String description,
            String technologyLevel, String tone,
            String factions, String species,
            String namingConvention, String galaxyShape, Integer empireCount,
            String recommendedGameModes,
            Boolean rpg, Boolean fourX, Boolean tycoon, Boolean tactical, Boolean survival,
            Boolean sandbox, Boolean mmorpg, Boolean roguelike, Boolean simulation, Boolean adventure, Boolean fps) {

        GameLore lore = new GameLore(name, slug, genre);
        lore.setCategory(category);
        lore.setShortDescription(shortDescription);
        lore.setDescription(description);
        lore.setTechnologyLevel(technologyLevel);
        lore.setTone(tone);
        lore.setFactions(factions);
        lore.setSpecies(species);
        lore.setNamingConvention(namingConvention);
        lore.setGalaxyShape(galaxyShape);
        lore.setSuggestedEmpireCount(empireCount);
        lore.setRecommendedGameModes(recommendedGameModes);
        lore.setSuitableForRpg(rpg);
        lore.setSuitableFor4x(fourX);
        lore.setSuitableForTycoon(tycoon);
        lore.setSuitableForTactical(tactical);
        lore.setSuitableForSurvival(survival);
        lore.setSuitableForSandbox(sandbox);
        lore.setSuitableForMmORPG(mmorpg);
        lore.setSuitableForRoguelike(roguelike);
        lore.setSuitableForSimulation(simulation);
        lore.setSuitableForAdventure(adventure);
        lore.setSuitableForFps(fps);
        return lore;
    }
}

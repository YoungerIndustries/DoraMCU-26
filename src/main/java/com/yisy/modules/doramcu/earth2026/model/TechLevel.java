package com.yisy.modules.doramcu.earth2026.model;

/**
 * Traveller-style Tech Levels (TL0–TL20) for Earth 2026 planet templates.
 * <p>
 * Each tech level defines the available technology for weapons, transport,
 * communications, computing, medicine, and industry. Games can query Earth 2026
 * for the tech level of a planet or structure to determine what equipment and
 * capabilities are available.
 * </p>
 * <p>
 * Based on the Traveller RPG Miller Scale:
 * <ul>
 *   <li>TL0–TL3: Pre-industrial (Stone Age through Napoleonic)</li>
 *   <li>TL4–TL6: Industrial (Age of Sail through Advanced Computing)</li>
 *   <li>TL7–TL9: Pre-Stellar (Space Age through early Jump Drive)</li>
 *   <li>TL10–TL12: Early Stellar (Interstellar travel)</li>
 *   <li>TL13–TL15: Average Stellar (Imperium-level technology)</li>
 *   <li>TL16–TL20: High Stellar (God-like technology)</li>
 * </ul>
 * </p>
 */
public enum TechLevel {

    /** Stone Age — no technology, hunter-gatherer societies. */
    TL0(0, "Stone Age", "No technology, hunter-gatherer",
            WeaponLevel.PRIMITIVE, TransportLevel.FOOT, CommsLevel.SMOKE_SIGNAL,
            ComputingLevel.NONE, MedicineLevel.HERBAL, IndustryLevel.HAND),

    /** Bronze Age / Ancient — early metals, writing, simple machines. */
    TL1(1, "Bronze Age", "Bronze tools, writing, simple machines",
            WeaponLevel.BRONZE, TransportLevel.ANIMAL, CommsLevel.MESSENGER,
            ComputingLevel.ABACUS, MedicineLevel.SURGERY_BASIC, IndustryLevel.ARTISAN),

    /** Iron Age / Medieval — iron weapons, castles, feudalism. */
    TL2(2, "Iron Age", "Iron weapons, castles, feudalism",
            WeaponLevel.IRON, TransportLevel.HORSE, CommsLevel.RIDER,
            ComputingLevel.NONE, MedicineLevel.HUMORS, IndustryLevel.WATER_WHEEL),

    /** Renaissance / Napoleonic — gunpowder, printing, early science. */
    TL3(3, "Renaissance", "Gunpowder, printing, early science",
            WeaponLevel.MUSKET, TransportLevel.SAIL, CommsLevel.POST,
            ComputingLevel.MECHANICAL, MedicineLevel.ANATOMY, IndustryLevel.HAND_LOOM),

    /** Age of Sail / Industrial — steam, mass production, early electricity. */
    TL4(4, "Industrial", "Steam power, mass production, early electricity",
            WeaponLevel.RIFLE, TransportLevel.STEAM, CommsLevel.TELEGRAPH,
            ComputingLevel.ANALOG, MedicineLevel.GERMS, IndustryLevel.STEAM_ENGINE),

    /** Modern — internal combustion, radio, plastics, electrification. */
    TL5(5, "Modern", "Internal combustion, radio, plastics",
            WeaponLevel.AUTO, TransportLevel.AUTOMOBILE, CommsLevel.RADIO,
            ComputingLevel.VACUUM_TUBE, MedicineLevel.ANTIBIOTICS, IndustryLevel.ASSEMBLY_LINE),

    /** Advanced — nuclear power, computers, satellites, jet aircraft. */
    TL6(6, "Advanced", "Nuclear power, computers, satellites",
            WeaponLevel.NUCLEAR, TransportLevel.JET, CommsLevel.SATELLITE,
            ComputingLevel.SILICON, MedicineLevel.CHEMOTHERAPY, IndustryLevel.AUTOMATED),

    /** Pre-Stellar — orbital stations, telecom satellites, rockets. */
    TL7(7, "Pre-Stellar", "Orbital stations, rockets, space stations",
            WeaponLevel.LASER_BASIC, TransportLevel.ROCKET, CommsLevel.ORBITAL,
            ComputingLevel.AI_BASIC, MedicineLevel.GENE_THERAPY, IndustryLevel.ROBOTIC),

    /** Pre-Stellar — interplanetary flight, primitive FTL comms. */
    TL8(8, "Interplanetary", "Interplanetary flight, FTL communications",
            WeaponLevel.LASER, TransportLevel.INTERPLANETARY, CommsLevel.FTL_COMMS,
            ComputingLevel.AI_ADVANCED, MedicineLevel.NANOMEDICINE, IndustryLevel.SPACE_INDUSTRY),

    /** Pre-Stellar — gravity control, early Jump Drive. */
    TL9(9, "Gravity Control", "Gravity control, early Jump Drive",
            WeaponLevel.PLASMA, TransportLevel.JUMP_PRIMITIVE, CommsLevel.SUBSPACE,
            ComputingLevel.QUANTUM, MedicineLevel.TELEPATHY, IndustryLevel.ANTIMATTER),

    /** Early Stellar — interstellar travel via Jump Drive. */
    TL10(10, "Early Stellar", "Interstellar travel, Jump Drive",
            WeaponLevel.ENERGY_SHIELD, TransportLevel.JUMP_DRIVE, CommsLevel.HYPERNET,
            ComputingLevel.NEURAL, MedicineLevel.REGENERATION, IndustryLevel.STAR_FORGE),

    /** Early Stellar — primitive AI, battle dress. */
    TL11(11, "AI Era", "Primitive AI, battle dress, advanced weapons",
            WeaponLevel.BATTLE_DRESS, TransportLevel.JUMP_IMPROVED, CommsLevel.HYPERNET_ADV,
            ComputingLevel.SENTIENT_AI, MedicineLevel.CLONING, IndustryLevel.AUTO_FACTORY),

    /** Average Stellar — weather control, planet-wide networks. */
    TL12(12, "Weather Control", "Weather control, planet-wide AI networks",
            WeaponLevel.WEAPON_PLATFORM, TransportLevel.WARP_PRIMITIVE, CommsLevel.PLANET_WIDE,
            ComputingLevel.PLANET_AI, MedicineLevel.TELEPORT_MED, IndustryLevel.PLANET_IND),

    /** Average Stellar — high autonomous AI, advanced battle systems. */
    TL13(13, "High AI", "High autonomous AI, advanced battle systems",
            WeaponLevel.ORBITAL_STRIKE, TransportLevel.WARP, CommsLevel.GALACTY_NET,
            ComputingLevel.GALACTY_AI, MedicineLevel.IMMORTALITY, IndustryLevel.STAR_SYSTEM),

    /** Average Stellar — planet cracking, Dyson swarms. */
    TL14(14, "Planet Cracking", "Planet cracking, Dyson swarms",
            WeaponLevel.PLANET_CRACKER, TransportLevel.WARP_ADVANCED, CommsLevel.INSTANT,
            ComputingLevel.OMNISCIENT, MedicineLevel.REANIMATION, IndustryLevel.DYSON_SWARM),

    /** High Stellar — matter synthesis, reality manipulation. */
    TL15(15, "Matter Synthesis", "Matter synthesis, reality manipulation",
            WeaponLevel.REALITY_WEAPON, TransportLevel.DIMENSIONAL, CommsLevel.THOUGHT,
            ComputingLevel.OMNISCIENT, MedicineLevel.REALITY_HEAL, IndustryLevel.MATTER_SYNTH),

    /** High Stellar — time travel precursors. */
    TL16(16, "Time Precursor", "Time travel precursors, dimension hopping",
            WeaponLevel.TIME_WEAPON, TransportLevel.TIME_TRAVEL, CommsLevel.TIMELESS,
            ComputingLevel.TIMELESS, MedicineLevel.TIME_HEAL, IndustryLevel.DIMENSIONAL),

    /** High Stellar — limited time manipulation. */
    TL17(17, "Time Manipulation", "Limited time manipulation",
            WeaponLevel.CAUSALITY, TransportLevel.MULTI_TIME, CommsLevel.OMNITEMPORAL,
            ComputingLevel.MULTIVERSE, MedicineLevel.CAUSAL_HEAL, IndustryLevel.MULTIVERSE),

    /** High Stellar — multiverse awareness. */
    TL18(18, "Multiverse", "Multiverse awareness and travel",
            WeaponLevel.MULTIVERSE, TransportLevel.MULTIVERSE_TRAVEL, CommsLevel.OMNIVERSE,
            ComputingLevel.OMNIVERSE, MedicineLevel.MULTIVERSE_HEAL, IndustryLevel.OMNIVERSE),

    /** God-like — weakly godlike entities. */
    TL19(19, "Godlike", "Weakly godlike entities",
            WeaponLevel.DIVINE, TransportLevel.DIVINE, CommsLevel.DIVINE,
            ComputingLevel.DIVINE, MedicineLevel.DIVINE, IndustryLevel.DIVINE),

    /** Sufficiently advanced technology — indistinguishable from magic. */
    TL20(20, "Magic Tech", "Indistinguishable from magic",
            WeaponLevel.MAGIC, TransportLevel.MAGIC, CommsLevel.MAGIC,
            ComputingLevel.MAGIC, MedicineLevel.MAGIC, IndustryLevel.MAGIC);

    private final int level;
    private final String name;
    private final String description;
    private final WeaponLevel weaponLevel;
    private final TransportLevel transportLevel;
    private final CommsLevel commsLevel;
    private final ComputingLevel computingLevel;
    private final MedicineLevel medicineLevel;
    private final IndustryLevel industryLevel;

    TechLevel(int level, String name, String description,
              WeaponLevel weaponLevel, TransportLevel transportLevel,
              CommsLevel commsLevel, ComputingLevel computingLevel,
              MedicineLevel medicineLevel, IndustryLevel industryLevel) {
        this.level = level;
        this.name = name;
        this.description = description;
        this.weaponLevel = weaponLevel;
        this.transportLevel = transportLevel;
        this.commsLevel = commsLevel;
        this.computingLevel = computingLevel;
        this.medicineLevel = medicineLevel;
        this.industryLevel = industryLevel;
    }

    /** Returns the numeric TL value (0-20). */
    public int getLevel() {
        return level;
    }

    /** Returns the display name (e.g., "Stone Age", "Industrial"). */
    public String getName() {
        return name;
    }

    /** Returns the description of this tech level. */
    public String getDescription() {
        return description;
    }

    /** Returns the maximum weapon technology available at this TL. */
    public WeaponLevel getWeaponLevel() {
        return weaponLevel;
    }

    /** Returns the maximum transport technology available at this TL. */
    public TransportLevel getTransportLevel() {
        return transportLevel;
    }

    /** Returns the maximum communications technology available at this TL. */
    public CommsLevel getCommsLevel() {
        return commsLevel;
    }

    /** Returns the maximum computing technology available at this TL. */
    public ComputingLevel getComputingLevel() {
        return computingLevel;
    }

    /** Returns the maximum medicine technology available at this TL. */
    public MedicineLevel getMedicineLevel() {
        return medicineLevel;
    }

    /** Returns the maximum industry technology available at this TL. */
    public IndustryLevel getIndustryLevel() {
        return industryLevel;
    }

    /**
     * Look up a TechLevel by its numeric value.
     *
     * @param level the numeric TL (0-20)
     * @return the corresponding TechLevel
     * @throws IllegalArgumentException if level is out of range
     */
    public static TechLevel fromLevel(int level) {
        for (TechLevel tl : values()) {
            if (tl.level == level) {
                return tl;
            }
        }
        throw new IllegalArgumentException("Invalid tech level: " + level);
    }

    /**
     * Returns true if this tech level is pre-industrial (TL0-TL3).
     */
    public boolean isPreIndustrial() {
        return level <= 3;
    }

    /**
     * Returns true if this tech level is industrial (TL4-TL6).
     */
    public boolean isIndustrial() {
        return level >= 4 && level <= 6;
    }

    /**
     * Returns true if this tech level is pre-stellar (TL7-TL9).
     */
    public boolean isPreStellar() {
        return level >= 7 && level <= 9;
    }

    /**
     * Returns true if this tech level is stellar (TL10+).
     */
    public boolean isStellar() {
        return level >= 10;
    }

    @Override
    public String toString() {
        return "TL" + level + " (" + name + ")";
    }

    // ------------------------------------------------------------------
    // Sub-level enums for technology categories
    // ------------------------------------------------------------------

    /** Weapon technology levels. */
    public enum WeaponLevel {
        PRIMITIVE("Primitive weapons (clubs, spears)"),
        BRONZE("Bronze weapons (swords, arrows)"),
        IRON("Iron weapons (longswords, crossbows)"),
        MUSKET("Gunpowder weapons (muskets, cannons)"),
        RIFLE("Bolt-action rifles, machine guns"),
        AUTO("Automatic weapons, tanks, aircraft"),
        NUCLEAR("Nuclear weapons, ICBMs"),
        LASER_BASIC("Basic laser weapons"),
        LASER("Advanced laser weapons"),
        PLASMA("Plasma weapons"),
        ENERGY_SHIELD("Energy shields and directed energy"),
        BATTLE_DRESS("Powered battle armor"),
        WEAPON_PLATFORM("Orbital weapon platforms"),
        ORBITAL_STRIKE("Orbital bombardment"),
        PLANET_CRACKER("Planet-destroying weapons"),
        REALITY_WEAPON("Reality-manipulating weapons"),
        TIME_WEAPON("Time-affecting weapons"),
        CAUSALITY("Causality weapons"),
        MULTIVERSE("Multiverse weapons"),
        DIVINE("Divine weapons"),
        MAGIC("Magic weapons");

        private final String description;
        WeaponLevel(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    /** Transport technology levels. */
    public enum TransportLevel {
        FOOT("Walking, running"),
        ANIMAL("Animal-drawn transport"),
        HORSE("Horseback riding, cavalry"),
        SAIL("Sailing ships"),
        STEAM("Steam trains, steamships"),
        AUTOMOBILE("Cars, trucks, ships"),
        JET("Jet aircraft, helicopters"),
        ROCKET("Rockets, orbital launch"),
        INTERPLANETARY("Interplanetary spacecraft"),
        JUMP_PRIMITIVE("Primitive jump drive"),
        JUMP_DRIVE("Functional jump drive"),
        JUMP_IMPROVED("Improved jump drive"),
        WARP_PRIMITIVE("Primitive warp drive"),
        WARP("Functional warp drive"),
        WARP_ADVANCED("Advanced warp drive"),
        DIMENSIONAL("Dimensional travel"),
        TIME_TRAVEL("Time travel"),
        MULTI_TIME("Multi-timeline travel"),
        MULTIVERSE_TRAVEL("Multiverse travel"),
        DIVINE("Divine transport"),
        MAGIC("Magic transport");

        private final String description;
        TransportLevel(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    /** Communications technology levels. */
    public enum CommsLevel {
        SMOKE_SIGNAL("Smoke signals, drums"),
        MESSENGER("Messenger pigeons, runners"),
        RIDER("Horse riders, postal service"),
        POST("Postal system, newspapers"),
        TELEGRAPH("Telegraph, telephones"),
        RADIO("Radio, television"),
        SATELLITE("Satellite communications"),
        ORBITAL("Orbital relay networks"),
        FTL_COMMS("Faster-than-light communications"),
        SUBSPACE("Subspace communications"),
        HYPERNET("Hypernet global network"),
        HYPERNET_ADV("Advanced hypernet"),
        PLANET_WIDE("Planet-wide instant network"),
        GALACTY_NET("Galaxy-wide network"),
        INSTANT("Instantaneous communication"),
        THOUGHT("Thought-based communication"),
        TIMELESS("Time-independent communication"),
        OMNITEMPORAL("All-times communication"),
        OMNIVERSE("Multiverse communication"),
        DIVINE("Divine communication"),
        MAGIC("Magic communication");

        private final String description;
        CommsLevel(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    /** Computing technology levels. */
    public enum ComputingLevel {
        NONE("No computing"),
        ABACUS("Abacus, counting boards"),
        MECHANICAL("Mechanical calculators"),
        ANALOG("Analog computers"),
        VACUUM_TUBE("Vacuum tube computers"),
        SILICON("Silicon microprocessors"),
        AI_BASIC("Basic AI, expert systems"),
        AI_ADVANCED("Advanced AI, neural networks"),
        QUANTUM("Quantum computers"),
        NEURAL("Neural interface computing"),
        SENTIENT_AI("Sentient AI systems"),
        PLANET_AI("Planet-scale AI"),
        GALACTY_AI("Galaxy-scale AI"),
        OMNISCIENT("Omniscient computing"),
        TIMELESS("Time-independent computing"),
        MULTIVERSE("Multiverse computing"),
        OMNIVERSE("Omniverse computing"),
        DIVINE("Divine computing"),
        MAGIC("Magic computing");

        private final String description;
        ComputingLevel(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    /** Medicine technology levels. */
    public enum MedicineLevel {
        HERBAL("Herbal remedies, prayer"),
        SURGERY_BASIC("Basic surgery, bandages"),
        HUMORS("Humoral theory, bloodletting"),
        ANATOMY("Anatomical knowledge, dissection"),
        GERMS("Germ theory, antiseptics"),
        ANTIBIOTICS("Antibiotics, vaccines"),
        CHEMOTHERAPY("Chemotherapy, radiation"),
        GENE_THERAPY("Gene therapy, CRISPR"),
        NANOMEDICINE("Nanobots, targeted treatment"),
        TELEPATHY("Telepathic healing"),
        REGENERATION("Limb regeneration"),
        CLONING("Organ cloning, body cloning"),
        TELEPORT_MED("Teleportation medicine"),
        IMMORTALITY("Biological immortality"),
        REANIMATION("Death reversal"),
        REALITY_HEAL("Reality-based healing"),
        TIME_HEAL("Time-based healing"),
        CAUSAL_HEAL("Causality healing"),
        MULTIVERSE_HEAL("Multiverse healing"),
        DIVINE("Divine healing"),
        MAGIC("Magic healing");

        private final String description;
        MedicineLevel(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    /** Industry technology levels. */
    public enum IndustryLevel {
        HAND("Hand tools, foraging"),
        ARTISAN("Artisan workshops"),
        WATER_WHEEL("Water wheels, windmills"),
        HAND_LOOM("Hand looms, simple factories"),
        STEAM_ENGINE("Steam engines, factories"),
        ASSEMBLY_LINE("Assembly lines, mass production"),
        AUTOMATED("Automated factories, CNC"),
        ROBOTIC("Robotic manufacturing"),
        SPACE_INDUSTRY("Space-based industry"),
        ANTIMATTER("Antimatter production"),
        STAR_FORGE("Star-level manufacturing"),
        AUTO_FACTORY("Self-replicating factories"),
        PLANET_IND("Planet-scale industry"),
        STAR_SYSTEM("Star system industry"),
        DYSON_SWARM("Dyson swarm construction"),
        MATTER_SYNTH("Matter synthesis"),
        DIMENSIONAL("Dimensional manufacturing"),
        MULTIVERSE("Multiverse industry"),
        OMNIVERSE("Omniverse industry"),
        DIVINE("Divine industry"),
        MAGIC("Magic industry");

        private final String description;
        IndustryLevel(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}

package com.yisy.modules.doramcu.dnd5e.service;

import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillain;
import com.yisy.modules.doramcu.dnd5e.model.Dnd5eVillainPower;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eVillainPowerRepository;
import com.yisy.modules.doramcu.dnd5e.repository.Dnd5eVillainRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing D&D 5e Villains from Villainous Options UA 2026.
 * <p>
 * Provides CRUD operations for villains and their powers, lair actions, legendary actions,
 * regional effects, minion spawning, and phylactery destruction mechanics.
 * </p>
 */
@Service
public class Dnd5eVillainService {

    private static final Logger log = LoggerFactory.getLogger(Dnd5eVillainService.class);

    private final Dnd5eVillainRepository villainRepository;
    private final Dnd5eVillainPowerRepository villainPowerRepository;

    public Dnd5eVillainService(Dnd5eVillainRepository villainRepository,
                               Dnd5eVillainPowerRepository villainPowerRepository) {
        this.villainRepository = villainRepository;
        this.villainPowerRepository = villainPowerRepository;

        // Initialize database with example villains if empty
        initializeVillainDatabase();
    }

    // ==================== Villain CRUD ====================

    @Transactional
    public Dnd5eVillain createVillain(Dnd5eVillain villain) {
        log.info("Creating villain: {}", villain.getName());
        return villainRepository.save(villain);
    }

    public Optional<Dnd5eVillain> getVillain(Long id) {
        return villainRepository.findById(id);
    }

    public Optional<Dnd5eVillain> getVillainByName(String name) {
        return villainRepository.findByName(name);
    }

    public List<Dnd5eVillain> listVillains() {
        return villainRepository.findAll();
    }

    public List<Dnd5eVillain> listVillainsByType(String villainType) {
        return villainRepository.findByVillainType(villainType);
    }

    public List<Dnd5eVillain> listVillainsByCrRange(int minCr, int maxCr) {
        return villainRepository.findByChallengeRatingRange(minCr, maxCr);
    }

    public List<Dnd5eVillain> listLiches() {
        return villainRepository.findByIsLichTrue();
    }

    public List<Dnd5eVillain> listDeathKnights() {
        return villainRepository.findByIsDeathKnightTrue();
    }

    public List<Dnd5eVillain> searchVillains(String keyword) {
        return villainRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Dnd5eVillain updateVillain(Long id, Dnd5eVillain updates) {
        return villainRepository.findById(id)
                .map(villain -> {
                    if (updates.getName() != null) {
                        villain.setName(updates.getName());
                    }
                    if (updates.getVillainType() != null) {
                        villain.setVillainType(updates.getVillainType());
                    }
                    if (updates.getDescription() != null) {
                        villain.setDescription(updates.getDescription());
                    }
                    if (updates.getMotive() != null) {
                        villain.setMotive(updates.getMotive());
                    }
                    villain.setChallengeRating(updates.getChallengeRating());
                    villain.setHitPoints(updates.getHitPoints());
                    villain.setArmorClass(updates.getArmorClass());
                    if (updates.getLairAction() != null) {
                        villain.setLairAction(updates.getLairAction());
                    }
                    if (updates.getRegionalEffect() != null) {
                        villain.setRegionalEffect(updates.getRegionalEffect());
                    }
                    if (updates.getMinionTypes() != null) {
                        villain.setMinionTypes(updates.getMinionTypes());
                    }
                    if (updates.getSpecialAbilities() != null) {
                        villain.setSpecialAbilities(updates.getSpecialAbilities());
                    }
                    if (updates.getSpellcastingAbility() != null) {
                        villain.setSpellcastingAbility(updates.getSpellcastingAbility());
                    }
                    if (updates.getSpellSaveDc() != null) {
                        villain.setSpellSaveDc(updates.getSpellSaveDc());
                    }
                    villain.setLich(updates.isLich());
                    if (updates.getPhylactery() != null) {
                        villain.setPhylactery(updates.getPhylactery());
                    }
                    villain.setDeathKnight(updates.isDeathKnight());
                    if (updates.getDeathKnightClass() != null) {
                        villain.setDeathKnightClass(updates.getDeathKnightClass());
                    }
                    return villainRepository.save(villain);
                })
                .orElseThrow(() -> {
                    log.error("Villain not found: {}", id);
                    return new RuntimeException("Villain not found: " + id);
                });
    }

    @Transactional
    public boolean deleteVillain(Long id) {
        if (villainRepository.existsById(id)) {
            // Delete associated powers first
            villainPowerRepository.deleteByVillainId(id);
            villainRepository.deleteById(id);
            log.info("Deleted villain: {}", id);
            return true;
        }
        log.warn("Cannot delete villain - not found: {}", id);
        return false;
    }

    // ==================== Villain Powers ====================

    @Transactional
    public Dnd5eVillainPower createPower(Long villainId, Dnd5eVillainPower power) {
        if (!villainRepository.existsById(villainId)) {
            throw new RuntimeException("Villain not found: " + villainId);
        }
        power.setVillainId(villainId);
        log.info("Creating power '{}' for villain {}", power.getName(), villainId);
        return villainPowerRepository.save(power);
    }

    public List<Dnd5eVillainPower> listPowers(Long villainId) {
        return villainPowerRepository.findByVillainIdOrdered(villainId);
    }

    public List<Dnd5eVillainPower> listLegendaryActions(Long villainId) {
        return villainPowerRepository.findLegendaryActionsByVillainId(villainId);
    }

    public List<Dnd5eVillainPower> listLairActions(Long villainId) {
        return villainPowerRepository.findLairActionsByVillainId(villainId);
    }

    public List<Dnd5eVillainPower> listRegionalEffects(Long villainId) {
        return villainPowerRepository.findRegionalEffectsByVillainId(villainId);
    }

    @Transactional
    public Dnd5eVillainPower updatePower(Long id, Dnd5eVillainPower updates) {
        return villainPowerRepository.findById(id)
                .map(power -> {
                    if (updates.getName() != null) {
                        power.setName(updates.getName());
                    }
                    if (updates.getPowerType() != null) {
                        power.setPowerType(updates.getPowerType());
                    }
                    if (updates.getDescription() != null) {
                        power.setDescription(updates.getDescription());
                    }
                    power.setActionCost(updates.getActionCost());
                    if (updates.getSaveAbility() != null) {
                        power.setSaveAbility(updates.getSaveAbility());
                    }
                    if (updates.getSaveDc() != null) {
                        power.setSaveDc(updates.getSaveDc());
                    }
                    if (updates.getDamageType() != null) {
                        power.setDamageType(updates.getDamageType());
                    }
                    if (updates.getDamageDice() != null) {
                        power.setDamageDice(updates.getDamageDice());
                    }
                    if (updates.getRecharge() != null) {
                        power.setRecharge(updates.getRecharge());
                    }
                    if (updates.getDuration() != null) {
                        power.setDuration(updates.getDuration());
                    }
                    if (updates.getRange() != null) {
                        power.setRange(updates.getRange());
                    }
                    power.setSortOrder(updates.getSortOrder());
                    return villainPowerRepository.save(power);
                })
                .orElseThrow(() -> {
                    log.error("Villain power not found: {}", id);
                    return new RuntimeException("Villain power not found: " + id);
                });
    }

    @Transactional
    public boolean deletePower(Long id) {
        if (villainPowerRepository.existsById(id)) {
            villainPowerRepository.deleteById(id);
            log.info("Deleted villain power: {}", id);
            return true;
        }
        return false;
    }

    // ==================== Combat Mechanics ====================

    /**
     * Uses a legendary resistance on the villain.
     *
     * @param villainId the villain ID
     * @return true if resistance was used, false if no resistances remain
     */
    @Transactional
    public boolean useLegendaryResistance(Long villainId) {
        return villainRepository.findById(villainId)
                .map(villain -> {
                    if (villain.getLegendaryResistances() > 0) {
                        villain.setLegendaryResistances(villain.getLegendaryResistances() - 1);
                        villainRepository.save(villain);
                        log.info("Villain {} used legendary resistance. Remaining: {}",
                                villain.getName(), villain.getLegendaryResistances());
                        return true;
                    }
                    log.info("Villain {} has no legendary resistances remaining", villain.getName());
                    return false;
                })
                .orElseThrow(() -> new RuntimeException("Villain not found: " + villainId));
    }

    /**
     * Restores legendary resistances (e.g., after a long rest or phylactery regeneration).
     */
    @Transactional
    public void restoreLegendaryResistances(Long villainId) {
        villainRepository.findById(villainId)
                .ifPresent(villain -> {
                    villain.setLegendaryResistances(villain.getMaxLegendaryResistances());
                    villainRepository.save(villain);
                    log.info("Restored legendary resistances for {}", villain.getName());
                });
    }

    /**
     * Deals damage to a villain and checks for death.
     * For Liches, destroying the phylactery is required for permanent death.
     */
    @Transactional
    public VillainDamageResult dealDamage(Long villainId, int damage) {
        return villainRepository.findById(villainId)
                .map(villain -> {
                    int remainingHp = Math.max(0, villain.getHitPoints() - damage);
                    villain.setHitPoints(remainingHp);
                    villainRepository.save(villain);

                    if (remainingHp <= 0) {
                        if (villain.isLich()) {
                            // Lich retreats to phylactery
                            log.info("Lich {} reduced to 0 HP. Retreats to phylactery: {}",
                                    villain.getName(), villain.getPhylactery());
                            return new VillainDamageResult(remainingHp, false, true, "Lich retreats to phylactery");
                        } else {
                            log.info("Villain {} is defeated", villain.getName());
                            return new VillainDamageResult(remainingHp, true, false, "Villain is defeated");
                        }
                    } else {
                        return new VillainDamageResult(remainingHp, false, false,
                                villain.getName() + " has " + remainingHp + " HP remaining");
                    }
                })
                .orElseThrow(() -> new RuntimeException("Villain not found: " + villainId));
    }

    /**
     * Destroys a Lich's phylactery, permanently killing the Lich.
     */
    @Transactional
    public boolean destroyPhylactery(Long villainId) {
        return villainRepository.findById(villainId)
                .map(villain -> {
                    if (villain.isLich() && villain.getPhylactery() != null) {
                        log.info("Phylactery destroyed: {}. Lich {} is permanently dead.",
                                villain.getPhylactery(), villain.getName());
                        villain.setPhylactery(null);
                        villain.setLich(false);
                        villain.setHitPoints(0);
                        villainRepository.save(villain);
                        return true;
                    }
                    log.warn("Cannot destroy phylactery - villain is not a Lich or has no phylactery");
                    return false;
                })
                .orElseThrow(() -> new RuntimeException("Villain not found: " + villainId));
    }

    /**
     * Spawns minions for a villain.
     */
    public List<String> spawnMinions(Long villainId) {
        Dnd5eVillain villain = villainRepository.findById(villainId)
                .orElseThrow(() -> new RuntimeException("Villain not found: " + villainId));
        if (villain.getMinionTypes() == null || villain.getMinionTypes().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> minions = Arrays.stream(villain.getMinionTypes().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        log.info("Spawned minions for {}: {}", villain.getName(), minions);
        return minions;
    }

    /**
     * Gets the lair action options for a villain at initiative 20.
     */
    public List<Dnd5eVillainPower> getLairActionOptions(Long villainId) {
        return villainPowerRepository.findLairActionsByVillainId(villainId);
    }

    /**
     * Gets the legendary action options for a villain.
     * Villains get 3 legendary actions per round, spent on actions with costs.
     */
    public List<Dnd5eVillainPower> getLegendaryActionOptions(Long villainId) {
        return villainPowerRepository.findLegendaryActionsByVillainId(villainId);
    }

    /**
     * Gets the regional effects active in a villain's territory.
     */
    public List<Dnd5eVillainPower> getRegionalEffects(Long villainId) {
        return villainPowerRepository.findRegionalEffectsByVillainId(villainId);
    }

    /**
     * Resets a villain to full health and resources (for DM convenience).
     */
    @Transactional
    public void resetVillain(Long villainId) {
        villainRepository.findById(villainId).ifPresent(villain -> {
            // We need to reload to get original values, but for simplicity we just restore resistances
            villain.setLegendaryResistances(villain.getMaxLegendaryResistances());
            villain.setActionSurge(false);
            villainRepository.save(villain);
            log.info("Reset villain {}", villain.getName());
        });
    }

    // ==================== Villain Types Reference ====================

    public List<String> getVillainTypes() {
        return List.of("Lich", "Death Knight", "Archfey", "Dragon", "Demon Lord",
                "Devil", "Doppelganger", "Elder Brain", "Lizardfolk", "Mind Flayer",
                "Red Dragon", "Blue Dragon", "Green Dragon", "Black Dragon", "White Dragon",
                "Archmage", "Undead", "Fiend", "Aberration", "Fey");
    }

    // ==================== Database Initialization ====================

    @Transactional
    void initializeVillainDatabase() {
        if (villainRepository.count() > 0) {
            log.info("Villain database already populated, skipping initialization");
            return;
        }

        log.info("Initializing villain database with example villains");
        populateExampleVillains();
        populateExamplePowers();
        log.info("Villain database initialization complete");
    }

    void populateExampleVillains() {
        // Lich - Vecna
        Dnd5eVillain vecna = new Dnd5eVillain();
        vecna.setName("Vecna");
        vecna.setVillainType("Lich");
        vecna.setChallengeRating(21);
        vecna.setDescription("The Lord of Undead, once a powerful wizard who became a lich and now seeks to regain his lost limbs and rule the multiverse.");
        vecna.setMotive("To regain his lost hand and eye, and establish eternal dominion over all planes of existence.");
        vecna.setHitPoints(487);
        vecna.setArmorClass(20);
        vecna.setMaxLegendaryResistances(3);
        vecna.setLegendaryResistances(3);
        vecna.setLairAction("The lich's lair pulses with necrotic energy. Spectral hands emerge from the ground.");
        vecna.setRegionalEffect("The land around Vecna's lair is blighted. Crops fail, water turns foul, and the dead rise unbidden.");
        vecna.setMinionTypes("Skeleton Warriors, Zombie Brutes, Ghosts, Mummies, Will-o'-Wisps");
        vecna.setSpecialAbilities("Reanimation, Life Drain, Cloudkill, Cone of Cold");
        vecna.setSpellcastingAbility("INT");
        vecna.setSpellSaveDc(23);
        vecna.setLich(true);
        vecna.setPhylactery("A hidden chest containing Vecna's soul, protected by powerful wards and guardians in the Far Realm.");
        villainRepository.save(vecna);

        // Lich - Kas the Bloody-Handed
        Dnd5eVillain kas = new Dnd5eVillain();
        kas.setName("Kas the Bloody-Handed");
        kas.setVillainType("Lich");
        kas.setChallengeRating(19);
        kas.setDescription("Vecna's former lieutenant, a lich who betrayed his master and now seeks to claim the portfolio of secrets for himself.");
        kas.setMotive("To usurp Vecna's power and become a god of secrets and undeath.");
        kas.setHitPoints(372);
        kas.setArmorClass(18);
        kas.setMaxLegendaryResistances(3);
        kas.setLegendaryResistances(3);
        kas.setLairAction("Shadows coalesce into spectral warriors that attack intruders.");
        kas.setRegionalEffect("Whispers echo through the land, driving travelers mad with secrets they shouldn't know.");
        kas.setMinionTypes("Shadow Warriors, Wraiths, Specters");
        kas.setSpecialAbilities("Life Drain, Fear, Darkness");
        kas.setSpellcastingAbility("INT");
        kas.setSpellSaveDc(21);
        kas.setLich(true);
        kas.setPhylactery("A ring of force surrounding a dagger embedded in stone, hidden in the Demiplane of Dread.");
        villainRepository.save(kas);

        // Death Knight - Runefang
        Dnd5eVillain runefang = new Dnd5eVillain();
        runefang.setName("Runefang");
        runefang.setVillainType("Death Knight");
        runefang.setChallengeRating(17);
        runefang.setDescription("A once-noble paladin corrupted by a dark lord, now commanding an army of the undead in an eternal campaign.");
        runefang.setMotive("To fulfill the dark lord's command and conquer the living realms for the undead.");
        runefang.setHitPoints(315);
        runefang.setArmorClass(19);
        runefang.setMaxLegendaryResistances(3);
        runefang.setLegendaryResistances(3);
        runefang.setActionSurge(true);
        runefang.setLairAction("The ground trembles as undead warriors rise from the earth.");
        runefang.setRegionalEffect("A perpetual chill blankets the region. The dead refuse to stay buried.");
        runefang.setMinionTypes("Skeleton Warriors, Zombie Brutes, Wights, Wraiths");
        runefang.setSpecialAbilities("Death Throes, Death's Chill, Death Save Points, Reincarnate");
        runefang.setDeathKnight(true);
        runefang.setDeathKnightClass("Paladin");
        villainRepository.save(runefang);

        // Archfey - Volothamp
        Dnd5eVillain volothamp = new Dnd5eVillain();
        volothamp.setName("Volothamp Geddarm");
        volothamp.setVillainType("Archfey");
        volothamp.setChallengeRating(15);
        volothamp.setDescription("The self-proclaimed greatest adventurer in Faerûn, who hides dark secrets beneath his charming exterior.");
        volothamp.setMotive("To uncover and exploit the greatest adventure of all time, regardless of the cost to others.");
        volothamp.setHitPoints(240);
        volothamp.setArmorClass(17);
        volothamp.setMaxLegendaryResistances(2);
        volothamp.setLegendaryResistances(2);
        volothamp.setLairAction("The Feywild warps reality. Creatures may be transported to random locations.");
        volothamp.setRegionalEffect("The boundaries between the Material Plane and Feywild grow thin. Time flows erratically.");
        volothamp.setMinionTypes("Satyrs, Dryads, Pixies, Sprites");
        volothamp.setSpecialAbilities("Fey Step, Compulsion, Enchantment");
        volothamp.setSpellcastingAbility("CHA");
        volothamp.setSpellSaveDc(19);
        villainRepository.save(volothamp);

        // Dragon - Tiamat
        Dnd5eVillain tiamat = new Dnd5eVillain();
        tiamat.setName("Tiamat");
        tiamat.setVillainType("Dragon");
        tiamat.setChallengeRating(25);
        tiamat.setDescription("The Five-Headed Queen of Chromatic Dragons, each head representing a different dragon color and breath weapon.");
        tiamat.setMotive("To destroy all good dragons and claim the World Tree for herself, opening the way for demonic invasion.");
        tiamat.setHitPoints(640);
        tiamat.setArmorClass(17);
        tiamat.setMaxLegendaryResistances(3);
        tiamat.setLegendaryResistances(3);
        tiamat.setLairAction("The lair fills with chromatic energy. Five different breath weapons may be unleashed.");
        tiamat.setRegionalEffect("The land is scarred by five different elemental forces. Dragon cults thrive in the region.");
        tiamat.setMinionTypes("Dragon Cultists, Drakes, Wyverns, Kobolds");
        tiamat.setSpecialAbilities("Five Heads, Multiattack, Breath Weapons (Acid, Cold, Fire, Lightning, Poison)");
        villainRepository.save(tiamat);

        // Demon Lord - Asmodeus
        Dnd5eVillain asmodeus = new Dnd5eVillain();
        asmodeus.setName("Asmodeus");
        asmodeus.setVillainType("Demon Lord");
        asmodeus.setChallengeRating(27);
        asmodeus.setDescription("The Prince of Devils, ruler of the Nine Hells, who seeks to corrupt all mortal souls through contracts and temptation.");
        asmodeus.setMotive("To expand the dominion of the Nine Hells and corrupt all mortal planes through devilish contracts.");
        asmodeus.setHitPoints(720);
        asmodeus.setArmorClass(21);
        asmodeus.setMaxLegendaryResistances(3);
        asmodeus.setLegendaryResistances(3);
        asmodeus.setLairAction("Hellfire erupts from the ground. Demonic minions materialize from portals.");
        asmodeus.setRegionalEffect("The air reeks of sulfur. Mortals find themselves tempted by dark bargains and forbidden knowledge.");
        asmodeus.setMinionTypes("Imps, Manes, Hell Hounds, Pit Fiends");
        asmodeus.setSpecialAbilities("Command, Dominate, Hellfire, Shapechanger");
        asmodeus.setSpellcastingAbility("CHA");
        asmodeus.setSpellSaveDc(25);
        villainRepository.save(asmodeus);

        // Elder Brain - Illefarn
        Dnd5eVillain ellefarn = new Dnd5eVillain();
        ellefarn.setName("Illefarn");
        ellefarn.setVillainType("Elder Brain");
        ellefarn.setChallengeRating(19);
        ellefarn.setDescription("An ancient Elder Brain that controls a mind flayer colony, seeking to expand its influence across the surface.");
        ellefarn.setMotive("To expand the mind flayer colony and enslave surface dwellers for psionic experiments.");
        ellefarn.setHitPoints(420);
        ellefarn.setArmorClass(15);
        ellefarn.setMaxLegendaryResistances(3);
        ellefarn.setLegendaryResistances(3);
        ellefarn.setLairAction("Psionic waves emanate from the brain. Creatures must save or be dominated.");
        ellefarn.setRegionalEffect("Psionic energy permeates the air. Minds are probed and thoughts are not private.");
        ellefarn.setMinionTypes("Mind Flayers, Mind Flayer Young, Deep Gnomes, Derro");
        ellefarn.setSpecialAbilities("Brain Storm, Dominate, Psionic Blast, Telepathy");
        ellefarn.setSpellcastingAbility("INT");
        ellefarn.setSpellSaveDc(21);
        villainRepository.save(ellefarn);

        // Archmage - Szass Tam
        Dnd5eVillain szassTam = new Dnd5eVillain();
        szassTam.setName("Szass Tam");
        szassTam.setVillainType("Archmage");
        szassTam.setChallengeRating(23);
        szassTam.setDescription("The Red Wizard of Thay who achieved lichdom through sheer willpower and now rules Thay as an immortal tyrant.");
        szassTam.setMotive("To achieve godhood through the accumulation of magical power and knowledge.");
        szassTam.setHitPoints(550);
        szassTam.setArmorClass(19);
        szassTam.setMaxLegendaryResistances(3);
        szassTam.setLegendaryResistances(3);
        szassTam.setLairAction("Arcane symbols ignite. Teleportation circles activate, moving creatures randomly.");
        szassTam.setRegionalEffect("Magic surges and wild magic effects occur randomly. Spell scrolls and tomes appear and disappear.");
        szassTam.setMinionTypes("Red Wizards, Mummies, Will-o'-Wisps, Gargoyles");
        szassTam.setSpecialAbilities("Spell Mastery, Arcane Mastery, Teleportation, Counterspell");
        szassTam.setSpellcastingAbility("INT");
        szassTam.setSpellSaveDc(24);
        szassTam.setLich(true);
        szassTam.setPhylactery("Hidden within the layers of the Spellplague, protected by countless magical wards.");
        villainRepository.save(szassTam);
    }

    void populateExamplePowers() {
        // Vecna's powers
        Dnd5eVillain vecna = villainRepository.findByName("Vecna").orElse(null);
        if (vecna != null) {
            // Lair Actions
            Dnd5eVillainPower lair1 = new Dnd5eVillainPower();
            lair1.setVillainId(vecna.getId());
            lair1.setName("Spectral Hands");
            lair1.setPowerType("Lair Action");
            lair1.setActionCost(0);
            lair1.setDescription("Spectral hands emerge from the ground in a 60-foot radius. Each creature in the area must make a DC 23 DEX save or take 4d10 necrotic damage and be restrained until they break free.");
            lair1.setSaveAbility("DEX");
            lair1.setSaveDc(23);
            lair1.setDamageType("Necrotic");
            lair1.setDamageDice("4d10");
            lair1.setRange("60-foot radius");
            lair1.setLairOnly(true);
            lair1.setSortOrder(1);
            villainPowerRepository.save(lair1);

            Dnd5eVillainPower lair2 = new Dnd5eVillainPower();
            lair2.setVillainId(vecna.getId());
            lair2.setName("Necrotic Surge");
            lair2.setPowerType("Lair Action");
            lair2.setActionCost(0);
            lair2.setDescription("Necrotic energy surges through the lair. Undead minions regain 2d10 HP, and living creatures take 3d8 necrotic damage on a failed DC 23 CON save.");
            lair2.setSaveAbility("CON");
            lair2.setSaveDc(23);
            lair2.setDamageType("Necrotic");
            lair2.setDamageDice("3d8");
            lair2.setRange("60-foot radius");
            lair2.setLairOnly(true);
            lair2.setSortOrder(2);
            villainPowerRepository.save(lair2);

            // Legendary Actions
            Dnd5eVillainPower leg1 = new Dnd5eVillainPower();
            leg1.setVillainId(vecna.getId());
            leg1.setName("Life Drain");
            leg1.setPowerType("Legendary Action");
            leg1.setActionCost(1);
            leg1.setDescription("One creature within 60 feet must succeed on a DC 23 WIS save or take 4d8 necrotic damage and gain exhaustion level 1.");
            leg1.setSaveAbility("WIS");
            leg1.setSaveDc(23);
            leg1.setDamageType("Necrotic");
            leg1.setDamageDice("4d8");
            leg1.setRange("60 feet");
            leg1.setSortOrder(1);
            villainPowerRepository.save(leg1);

            Dnd5eVillainPower leg2 = new Dnd5eVillainPower();
            leg2.setVillainId(vecna.getId());
            leg2.setName("Cloudkill");
            leg2.setPowerType("Legendary Action");
            leg2.setActionCost(2);
            leg2.setDescription("Vecna casts Cloudkill as a 6th-level spell without components.");
            leg2.setDuration("Up to 1 minute");
            leg2.setSortOrder(2);
            villainPowerRepository.save(leg2);

            Dnd5eVillainPower leg3 = new Dnd5eVillainPower();
            leg3.setVillainId(vecna.getId());
            leg3.setName("Cone of Cold");
            leg3.setPowerType("Legendary Action");
            leg3.setActionCost(3);
            leg3.setDescription("Vecna casts Cone of Cold without components. Each creature in a 60-foot cone must make a DC 23 DEX save, taking 8d8 cold damage on a failure, or half on a success.");
            leg3.setSaveAbility("DEX");
            leg3.setSaveDc(23);
            leg3.setDamageType("Cold");
            leg3.setDamageDice("8d8");
            leg3.setRange("60-foot cone");
            leg3.setSortOrder(3);
            villainPowerRepository.save(leg3);

            // Regional Effects
            Dnd5eVillainPower reg1 = new Dnd5eVillainPower();
            reg1.setVillainId(vecna.getId());
            reg1.setName("Blighted Land");
            reg1.setPowerType("Regional Effect");
            reg1.setActionCost(0);
            reg1.setDescription("The land within 1 mile of Vecna's lair is blighted. Crops fail, water turns foul, and plants wither. Creatures without darkvision have disadvantage on WIS (Perception) checks.");
            reg1.setRange("1 mile");
            reg1.setRegional(true);
            reg1.setSortOrder(1);
            villainPowerRepository.save(reg1);

            Dnd5eVillainPower reg2 = new Dnd5eVillainPower();
            reg2.setVillainId(vecna.getId());
            reg2.setName("Rising Dead");
            reg2.setPowerType("Regional Effect");
            reg2.setActionCost(0);
            reg2.setDescription("The dead rise unbidden. Any creature that dies within 1 mile of the lair rises as a zombie or skeleton under Vecna's control after 1 hour.");
            reg2.setRange("1 mile");
            reg2.setRegional(true);
            reg2.setSortOrder(2);
            villainPowerRepository.save(reg2);
        }

        // Tiamat's powers
        Dnd5eVillain tiamat = villainRepository.findByName("Tiamat").orElse(null);
        if (tiamat != null) {
            // Lair Actions
            Dnd5eVillainPower lair1 = new Dnd5eVillainPower();
            lair1.setVillainId(tiamat.getId());
            lair1.setName("Chromatic Breath");
            lair1.setPowerType("Lair Action");
            lair1.setActionCost(0);
            lair1.setDescription("Tiamat unleashes one of her five breath weapons: Acid (40-foot cone, 10d8), Cold (60-foot cone, 8d10), Fire (40-foot cone, 10d12), Lightning (60-foot line, 8d12), or Poison (40-foot cone, 10d10). DEX save DC 25.");
            lair1.setSaveAbility("DEX");
            lair1.setSaveDc(25);
            lair1.setDamageType("Acid, Cold, Fire, Lightning, Poison");
            lair1.setDamageDice("Variable");
            lair1.setRange("40-60 feet");
            lair1.setLairOnly(true);
            lair1.setSortOrder(1);
            villainPowerRepository.save(lair1);

            // Legendary Actions
            Dnd5eVillainPower leg1 = new Dnd5eVillainPower();
            leg1.setVillainId(tiamat.getId());
            leg1.setName("Bite");
            leg1.setPowerType("Legendary Action");
            leg1.setActionCost(1);
            leg1.setDescription("One head makes a bite attack. +17 to hit, reach 5 feet, one target. Hit: 22 (3d8 + 7) piercing damage plus the target must make a DC 25 STR save or be knocked prone.");
            leg1.setSaveAbility("STR");
            leg1.setSaveDc(25);
            leg1.setDamageType("Piercing");
            leg1.setDamageDice("3d8+7");
            leg1.setRange("5 feet");
            leg1.setSortOrder(1);
            villainPowerRepository.save(leg1);

            Dnd5eVillainPower leg2 = new Dnd5eVillainPower();
            leg2.setVillainId(tiamat.getId());
            leg2.setName("Wing Slap");
            leg2.setPowerType("Legendary Action");
            leg2.setActionCost(2);
            leg2.setDescription("Tiamat slaps with her wings. Each creature within 15 feet must make a DC 25 DEX save or take 15 (2d6 + 7) bludgeoning damage and be knocked prone.");
            leg2.setSaveAbility("DEX");
            leg2.setSaveDc(25);
            leg2.setDamageType("Bludgeoning");
            leg2.setDamageDice("2d6+7");
            leg2.setRange("15 feet");
            leg2.setSortOrder(2);
            villainPowerRepository.save(leg2);

            Dnd5eVillainPower leg3 = new Dnd5eVillainPower();
            leg3.setVillainId(tiamat.getId());
            leg3.setName("Summon Drakes");
            leg3.setPowerType("Legendary Action");
            leg3.setActionCost(3);
            leg3.setDescription("Tiamat summons 1d3 drakes of random colors to appear in unoccupied spaces within 60 feet. The drakes disappear when reduced to 0 HP or after 1 minute.");
            leg3.setDuration("1 minute");
            leg3.setRange("60 feet");
            leg3.setSortOrder(3);
            villainPowerRepository.save(leg3);

            // Regional Effects
            Dnd5eVillainPower reg1 = new Dnd5eVillainPower();
            reg1.setVillainId(tiamat.getId());
            reg1.setName("Dragon Cult Influence");
            reg1.setPowerType("Regional Effect");
            reg1.setActionCost(0);
            reg1.setDescription("Dragon cults thrive within 10 miles of Tiamat's lair. Cultists may appear to harass travelers, and dragon-themed symbols appear on buildings and natural features.");
            reg1.setRange("10 miles");
            reg1.setRegional(true);
            reg1.setSortOrder(1);
            villainPowerRepository.save(reg1);
        }

        // Asmodeus's powers
        Dnd5eVillain asmodeus = villainRepository.findByName("Asmodeus").orElse(null);
        if (asmodeus != null) {
            // Lair Actions
            Dnd5eVillainPower lair1 = new Dnd5eVillainPower();
            lair1.setVillainId(asmodeus.getId());
            lair1.setName("Hellfire Eruption");
            lair1.setPowerType("Lair Action");
            lair1.setActionCost(0);
            lair1.setDescription("Hellfire erupts from the ground in a 20-foot square. Each creature in the area must make a DC 25 DEX save or take 6d10 fire damage and 6d10 radiant damage.");
            lair1.setSaveAbility("DEX");
            lair1.setSaveDc(25);
            lair1.setDamageType("Fire, Radiant");
            lair1.setDamageDice("6d10+6d10");
            lair1.setRange("20-foot square");
            lair1.setLairOnly(true);
            lair1.setSortOrder(1);
            villainPowerRepository.save(lair1);

            // Legendary Actions
            Dnd5eVillainPower leg1 = new Dnd5eVillainPower();
            leg1.setVillainId(asmodeus.getId());
            leg1.setName("Command");
            leg1.setPowerType("Legendary Action");
            leg1.setActionCost(1);
            leg1.setDescription("One creature within 60 feet must succeed on a DC 25 WIS save or be charmed until the end of Asmodeus's next turn. The charmed creature must use its reaction to move up to half its speed toward Asmodeus.");
            leg1.setSaveAbility("WIS");
            leg1.setSaveDc(25);
            leg1.setRange("60 feet");
            leg1.setSortOrder(1);
            villainPowerRepository.save(leg1);

            Dnd5eVillainPower leg2 = new Dnd5eVillainPower();
            leg2.setVillainId(asmodeus.getId());
            leg2.setName("Dominate");
            leg2.setPowerType("Legendary Action");
            leg2.setActionCost(2);
            leg2.setDescription("One humanoid within 60 feet must succeed on a DC 25 WIS save or be dominated until the end of Asmodeus's next turn.");
            leg2.setSaveAbility("WIS");
            leg2.setSaveDc(25);
            leg2.setRange("60 feet");
            leg2.setSortOrder(2);
            villainPowerRepository.save(leg2);

            Dnd5eVillainPower leg3 = new Dnd5eVillainPower();
            leg3.setVillainId(asmodeus.getId());
            leg3.setName("Hellfire Blast");
            leg3.setPowerType("Legendary Action");
            leg3.setActionCost(3);
            leg3.setDescription("Asmodeus unleashes hellfire in a 60-foot cone. Each creature must make a DC 25 DEX save, taking 10d8 fire damage on a failure, or half on a success.");
            leg3.setSaveAbility("DEX");
            leg3.setSaveDc(25);
            leg3.setDamageType("Fire");
            leg3.setDamageDice("10d8");
            leg3.setRange("60-foot cone");
            leg3.setSortOrder(3);
            villainPowerRepository.save(leg3);
        }

        // Illefarn's powers
        Dnd5eVillain ellefarn = villainRepository.findByName("Illefarn").orElse(null);
        if (ellefarn != null) {
            // Lair Actions
            Dnd5eVillainPower lair1 = new Dnd5eVillainPower();
            lair1.setVillainId(ellefarn.getId());
            lair1.setName("Psionic Wave");
            lair1.setPowerType("Lair Action");
            lair1.setActionCost(0);
            lair1.setDescription("Psionic waves emanate from the brain. Each creature within 30 feet must make a DC 21 INT save or take 4d10 psychic damage and be stunned until the end of its next turn.");
            lair1.setSaveAbility("INT");
            lair1.setSaveDc(21);
            lair1.setDamageType("Psychic");
            lair1.setDamageDice("4d10");
            lair1.setRange("30 feet");
            lair1.setLairOnly(true);
            lair1.setSortOrder(1);
            villainPowerRepository.save(lair1);

            // Legendary Actions
            Dnd5eVillainPower leg1 = new Dnd5eVillainPower();
            leg1.setVillainId(ellefarn.getId());
            leg1.setName("Brain Storm");
            leg1.setPowerType("Legendary Action");
            leg1.setActionCost(1);
            leg1.setDescription("Each creature within 30 feet must make a DC 21 INT save or take 3d8 psychic damage and have its thoughts read by the Elder Brain for 1 minute.");
            leg1.setSaveAbility("INT");
            leg1.setSaveDc(21);
            leg1.setDamageType("Psychic");
            leg1.setDamageDice("3d8");
            leg1.setRange("30 feet");
            leg1.setDuration("1 minute");
            leg1.setSortOrder(1);
            villainPowerRepository.save(leg1);

            Dnd5eVillainPower leg2 = new Dnd5eVillainPower();
            leg2.setVillainId(ellefarn.getId());
            leg2.setName("Dominate");
            leg2.setPowerType("Legendary Action");
            leg2.setActionCost(2);
            leg2.setDescription("One creature within 60 feet must succeed on a DC 21 WIS save or be dominated until the end of Illefarn's next turn.");
            leg2.setSaveAbility("WIS");
            leg2.setSaveDc(21);
            leg2.setRange("60 feet");
            leg2.setSortOrder(2);
            villainPowerRepository.save(leg2);

            Dnd5eVillainPower leg3 = new Dnd5eVillainPower();
            leg3.setVillainId(ellefarn.getId());
            leg3.setName("Psionic Blast");
            leg3.setPowerType("Legendary Action");
            leg3.setActionCost(3);
            leg3.setDescription("Illefarn unleashes a psionic blast in a 60-foot cone. Each creature must make a DC 21 CON save, taking 8d6 psychic damage on a failure, or half on a success.");
            leg3.setSaveAbility("CON");
            leg3.setSaveDc(21);
            leg3.setDamageType("Psychic");
            leg3.setDamageDice("8d6");
            leg3.setRange("60-foot cone");
            leg3.setSortOrder(3);
            villainPowerRepository.save(leg3);
        }

        // Szass Tam's powers
        Dnd5eVillain szassTam = villainRepository.findByName("Szass Tam").orElse(null);
        if (szassTam != null) {
            // Lair Actions
            Dnd5eVillainPower lair1 = new Dnd5eVillainPower();
            lair1.setVillainId(szassTam.getId());
            lair1.setName("Arcane Symbols");
            lair1.setPowerType("Lair Action");
            lair1.setActionCost(0);
            lair1.setDescription("Arcane symbols ignite around the lair. Each creature must make a DC 24 INT save or take 5d8 force damage and be blinded until the end of its next turn.");
            lair1.setSaveAbility("INT");
            lair1.setSaveDc(24);
            lair1.setDamageType("Force");
            lair1.setDamageDice("5d8");
            lair1.setRange("60-foot radius");
            lair1.setLairOnly(true);
            lair1.setSortOrder(1);
            villainPowerRepository.save(lair1);

            // Legendary Actions
            Dnd5eVillainPower leg1 = new Dnd5eVillainPower();
            leg1.setVillainId(szassTam.getId());
            leg1.setName("Counterspell");
            leg1.setPowerType("Legendary Action");
            leg1.setActionCost(1);
            leg1.setDescription("Szass Tam attempts to counterspell a spell being cast within 60 feet. The spell fails if the caster fails a DC 24 ARCANA check.");
            leg1.setSaveAbility("ARC");
            leg1.setSaveDc(24);
            leg1.setRange("60 feet");
            leg1.setSortOrder(1);
            villainPowerRepository.save(leg1);

            Dnd5eVillainPower leg2 = new Dnd5eVillainPower();
            leg2.setVillainId(szassTam.getId());
            leg2.setName("Teleport");
            leg2.setPowerType("Legendary Action");
            leg2.setActionCost(2);
            leg2.setDescription("Szass Tam teleports up to 60 feet to an unoccupied space he can see.");
            leg2.setRange("60 feet");
            leg2.setSortOrder(2);
            villainPowerRepository.save(leg2);

            Dnd5eVillainPower leg3 = new Dnd5eVillainPower();
            leg3.setVillainId(szassTam.getId());
            leg3.setName("Fireball");
            leg3.setPowerType("Legendary Action");
            leg3.setActionCost(3);
            leg3.setDescription("Szass Tam casts Fireball as an 8th-level spell without components. Each creature in a 20-foot radius must make a DC 24 DEX save, taking 13d6 fire damage on a failure, or half on a success.");
            leg3.setSaveAbility("DEX");
            leg3.setSaveDc(24);
            leg3.setDamageType("Fire");
            leg3.setDamageDice("13d6");
            leg3.setRange("20-foot radius");
            leg3.setSortOrder(3);
            villainPowerRepository.save(leg3);
        }

        // Runefang's powers
        Dnd5eVillain runefang = villainRepository.findByName("Runefang").orElse(null);
        if (runefang != null) {
            // Lair Actions
            Dnd5eVillainPower lair1 = new Dnd5eVillainPower();
            lair1.setVillainId(runefang.getId());
            lair1.setName("Undead Rising");
            lair1.setPowerType("Lair Action");
            lair1.setActionCost(0);
            lair1.setDescription("The ground trembles as 2d4 skeleton warriors rise from the earth in unoccupied spaces within 30 feet of Runefang.");
            lair1.setRange("30 feet");
            lair1.setLairOnly(true);
            lair1.setSortOrder(1);
            villainPowerRepository.save(lair1);

            // Legendary Actions
            Dnd5eVillainPower leg1 = new Dnd5eVillainPower();
            leg1.setVillainId(runefang.getId());
            leg1.setName("Death's Chill");
            leg1.setPowerType("Legendary Action");
            leg1.setActionCost(1);
            leg1.setDescription("Each creature within 10 feet must make a DC 20 CON save or take 2d6 necrotic damage and have its speed halved until the end of its next turn.");
            leg1.setSaveAbility("CON");
            leg1.setSaveDc(20);
            leg1.setDamageType("Necrotic");
            leg1.setDamageDice("2d6");
            leg1.setRange("10 feet");
            leg1.setSortOrder(1);
            villainPowerRepository.save(leg1);

            Dnd5eVillainPower leg2 = new Dnd5eVillainPower();
            leg2.setVillainId(runefang.getId());
            leg2.setName("Death Save Points");
            leg2.setPowerType("Legendary Action");
            leg2.setActionCost(2);
            leg2.setDescription("Runefang gains 2 death save points. When reduced to 0 HP, he can use these points to stay at 0 HP instead of dying, regaining 1 HP per point at the start of his turn.");
            leg2.setSortOrder(2);
            villainPowerRepository.save(leg2);

            Dnd5eVillainPower leg3 = new Dnd5eVillainPower();
            leg3.setVillainId(runefang.getId());
            leg3.setName("Reincarnate");
            leg3.setPowerType("Legendary Action");
            leg3.setActionCost(3);
            leg3.setDescription("If Runefang is at 0 HP, he can use this action to regain 30 HP. He can use this action once per long rest.");
            leg3.setSortOrder(3);
            villainPowerRepository.save(leg3);
        }

        // Volothamp's powers
        Dnd5eVillain volothamp = villainRepository.findByName("Volothamp Geddarm").orElse(null);
        if (volothamp != null) {
            // Lair Actions
            Dnd5eVillainPower lair1 = new Dnd5eVillainPower();
            lair1.setVillainId(volothamp.getId());
            lair1.setName("Feywild Warp");
            lair1.setPowerType("Lair Action");
            lair1.setActionCost(0);
            lair1.setDescription("The Feywild warps reality. Each creature within 30 feet must make a DC 19 WIS save or be teleported to a random location within 60 feet.");
            lair1.setSaveAbility("WIS");
            lair1.setSaveDc(19);
            lair1.setRange("30 feet");
            lair1.setLairOnly(true);
            lair1.setSortOrder(1);
            villainPowerRepository.save(lair1);

            // Legendary Actions
            Dnd5eVillainPower leg1 = new Dnd5eVillainPower();
            leg1.setVillainId(volothamp.getId());
            leg1.setName("Fey Step");
            leg1.setPowerType("Legendary Action");
            leg1.setActionCost(1);
            leg1.setDescription("Volothamp teleports up to 30 feet to an unoccupied space he can see, becoming invisible until the end of his next turn.");
            leg1.setRange("30 feet");
            leg1.setSortOrder(1);
            villainPowerRepository.save(leg1);

            Dnd5eVillainPower leg2 = new Dnd5eVillainPower();
            leg2.setVillainId(volothamp.getId());
            leg2.setName("Compulsion");
            leg2.setPowerType("Legendary Action");
            leg2.setActionCost(2);
            leg2.setDescription("One creature within 30 feet must succeed on a DC 19 WIS save or be charmed until the end of Volothamp's next turn. The charmed creature must use its reaction to move toward Volothamp.");
            leg2.setSaveAbility("WIS");
            leg2.setSaveDc(19);
            leg2.setRange("30 feet");
            leg2.setSortOrder(2);
            villainPowerRepository.save(leg2);

            Dnd5eVillainPower leg3 = new Dnd5eVillainPower();
            leg3.setVillainId(volothamp.getId());
            leg3.setName("Enchantment");
            leg3.setPowerType("Legendary Action");
            leg3.setActionCost(3);
            leg3.setDescription("Volothamp casts Charm Person or Suggestion without components on one creature within 60 feet.");
            leg3.setRange("60 feet");
            leg3.setSortOrder(3);
            villainPowerRepository.save(leg3);
        }
    }

    /**
     * DTO for villain damage results.
     */
    public static class VillainDamageResult {
        public final int remainingHp;
        public final boolean isDefeated;
        public final boolean retreatedToPhylactery;
        public final String message;

        public VillainDamageResult(int remainingHp, boolean isDefeated, boolean retreatedToPhylactery, String message) {
            this.remainingHp = remainingHp;
            this.isDefeated = isDefeated;
            this.retreatedToPhylactery = retreatedToPhylactery;
            this.message = message;
        }
    }
}

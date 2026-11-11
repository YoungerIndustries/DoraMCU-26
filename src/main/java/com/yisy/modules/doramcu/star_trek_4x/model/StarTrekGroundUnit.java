package com.yisy.modules.doramcu.star_trek_4x.model;

import jakarta.persistence.*;

/**
 * Star Trek ground unit for planetary combat.
 * <p>
 * Represents a ground combat unit (Security team, Marines, etc.) that can be
 * adapted to ThirdReichGroundUnit via StarTrekTo3R4Adapter for battle resolution.
 * </p>
 */
@Entity
@Table(name = "st4x_ground_units")
public class StarTrekGroundUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    /** Unit type: SECURITY, MARINE, SCIENCE, ENGINEERING, MEDICAL, DIPLOMAT. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnitType unitType;

    /** Current strength (hit points). */
    @Column(nullable = false)
    private int strength = 100;

    /** Maximum strength. */
    @Column(nullable = false)
    private int maxStrength = 100;

    /** Morale (0-100). */
    @Column(nullable = false)
    private int morale = 80;

    /** Attack value. */
    @Column(nullable = false)
    private int attackValue = 3;

    /** Defense value. */
    @Column(nullable = false)
    private int defenseValue = 3;

    /** Character ID that leads this unit (CaptainLogCharacter or SFB character). */
    @Column
    private Long characterId;

    /** Owner faction ID. */
    @Column(nullable = false)
    private Long ownerFactionId;

    /** Weapon loadout description. */
    @Column(length = 200)
    private String weaponLoadout;

    /** Whether this unit is currently in combat. */
    @Column(nullable = false)
    private boolean inCombat = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id", nullable = false)
    private GameSession gameSession;

    public StarTrekGroundUnit() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UnitType getUnitType() { return unitType; }
    public void setUnitType(UnitType unitType) { this.unitType = unitType; }

    public int getStrength() { return strength; }
    public void setStrength(int strength) { this.strength = strength; }

    public int getMaxStrength() { return maxStrength; }
    public void setMaxStrength(int maxStrength) { this.maxStrength = maxStrength; }

    public int getMorale() { return morale; }
    public void setMorale(int morale) { this.morale = Math.max(0, Math.min(100, morale)); }

    public int getAttackValue() { return attackValue; }
    public void setAttackValue(int attackValue) { this.attackValue = attackValue; }

    public int getDefenseValue() { return defenseValue; }
    public void setDefenseValue(int defenseValue) { this.defenseValue = defenseValue; }

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }

    public Long getOwnerFactionId() { return ownerFactionId; }
    public void setOwnerFactionId(Long ownerFactionId) { this.ownerFactionId = ownerFactionId; }

    public String getWeaponLoadout() { return weaponLoadout; }
    public void setWeaponLoadout(String weaponLoadout) { this.weaponLoadout = weaponLoadout; }

    public boolean isInCombat() { return inCombat; }
    public void setInCombat(boolean inCombat) { this.inCombat = inCombat; }

    public GameSession getGameSession() { return gameSession; }
    public void setGameSession(GameSession gameSession) { this.gameSession = gameSession; }

    /**
     * Check if this unit is destroyed.
     */
    public boolean isDestroyed() {
        return strength <= 0;
    }

    /**
     * Take damage.
     */
    public void takeDamage(int damage) {
        this.strength = Math.max(0, this.strength - damage);
    }

    public enum UnitType {
        SECURITY,
        MARINE,
        SCIENCE,
        ENGINEERING,
        MEDICAL,
        DIPLOMAT
    }
}

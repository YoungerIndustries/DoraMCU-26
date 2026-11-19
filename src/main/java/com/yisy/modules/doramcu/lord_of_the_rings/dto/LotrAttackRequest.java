package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Request for an attack roll in LOTR combat.
 */
public class LotrAttackRequest {

    private Long attackerId;
    private Long defenderId;
    private String weapon;

    public Long getAttackerId() {
        return attackerId;
    }

    public void setAttackerId(Long attackerId) {
        this.attackerId = attackerId;
    }

    public Long getDefenderId() {
        return defenderId;
    }

    public void setDefenderId(Long defenderId) {
        this.defenderId = defenderId;
    }

    public String getWeapon() {
        return weapon;
    }

    public void setWeapon(String weapon) {
        this.weapon = weapon;
    }
}

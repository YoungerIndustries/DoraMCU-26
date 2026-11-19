package com.yisy.modules.doramcu.lord_of_the_rings.dto;

/**
 * Request for a skill check in LOTR.
 */
public class SkillCheckRequest {

    private Long characterId;
    private String skill;
    private Integer dc;

    public Long getCharacterId() {
        return characterId;
    }

    public void setCharacterId(Long characterId) {
        this.characterId = characterId;
    }

    public String getSkill() {
        return skill;
    }

    public void setSkill(String skill) {
        this.skill = skill;
    }

    public Integer getDc() {
        return dc;
    }

    public void setDc(Integer dc) {
        this.dc = dc;
    }
}

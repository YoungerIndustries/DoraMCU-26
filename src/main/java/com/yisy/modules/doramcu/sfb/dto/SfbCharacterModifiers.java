package com.yisy.modules.doramcu.sfb.dto;

import com.yisy.modules.doramcu.sfb.model.SfbRole;

import java.util.List;
import java.util.Map;

/**
 * Response with character combat modifiers.
 */
public class SfbCharacterModifiers {
    private Long characterId;
    private String characterName;
    private SfbRole role;
    private Map<String, Integer> attributeBonuses;
    private Map<String, Integer> disciplineBonuses;
    private List<String> activeTalents;

    public Long getCharacterId() { return characterId; }
    public void setCharacterId(Long characterId) { this.characterId = characterId; }
    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }
    public SfbRole getRole() { return role; }
    public void setRole(SfbRole role) { this.role = role; }
    public Map<String, Integer> getAttributeBonuses() { return attributeBonuses; }
    public void setAttributeBonuses(Map<String, Integer> attributeBonuses) { this.attributeBonuses = attributeBonuses; }
    public Map<String, Integer> getDisciplineBonuses() { return disciplineBonuses; }
    public void setDisciplineBonuses(Map<String, Integer> disciplineBonuses) { this.disciplineBonuses = disciplineBonuses; }
    public List<String> getActiveTalents() { return activeTalents; }
    public void setActiveTalents(List<String> activeTalents) { this.activeTalents = activeTalents; }
}

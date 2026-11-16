package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.eve.model.AiCharacter;

/**
 * Unified AI entity interface that all AI entities implement.
 */
public interface AiEntity {

    Long getId();
    String getName();
    AiCharacter.CharacterType getType();
    String getEmpireName();
    PersonalityProfile getPersonality();
    boolean isActive();
    int getExperience();
    AiCharacter.TrainingLevel getTrainingLevel();
    void addExperience(int xp);
    void activate();
    void deactivate();
    AiCharacter getAiCharacter();
}

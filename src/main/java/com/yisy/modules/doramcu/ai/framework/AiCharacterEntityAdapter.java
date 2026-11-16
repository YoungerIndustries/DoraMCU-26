package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.eve.model.AiCharacter;

/**
 * Makes AiCharacter implement the unified AiEntity interface.
 */
public class AiCharacterEntityAdapter implements AiEntity {

    private final AiCharacter delegate;

    public AiCharacterEntityAdapter(AiCharacter delegate) {
        this.delegate = delegate;
    }

    @Override
    public Long getId() { return delegate.getId(); }

    @Override
    public String getName() { return delegate.getName(); }

    @Override
    public AiCharacter.CharacterType getType() { return delegate.getCharacterType(); }

    @Override
    public String getEmpireName() { return delegate.getEmpireName(); }

    @Override
    public PersonalityProfile getPersonality() { return null; }

    @Override
    public boolean isActive() { return delegate.isActive(); }

    @Override
    public int getExperience() { return delegate.getTotalExperience(); }

    @Override
    public AiCharacter.TrainingLevel getTrainingLevel() { return delegate.getTrainingLevel(); }

    @Override
    public void addExperience(int xp) { delegate.addExperience(xp); }

    @Override
    public void activate() { delegate.setActive(true); }

    @Override
    public void deactivate() { delegate.setActive(false); }

    @Override
    public AiCharacter getAiCharacter() { return delegate; }
}

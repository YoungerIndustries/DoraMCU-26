package com.yisy.modules.doramcu.gamecore;
import org.springframework.stereotype.Service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default delegating character service with minimal character management.
 */
@Component
public class DelegatingCharacterService implements CharacterService {

    private final GameModuleRegistry gameModuleRegistry;
    private final AtomicLong characterIdCounter = new AtomicLong(1);
    private final Map<Long, CharacterInfo> characters = new ConcurrentHashMap<>();

    public DelegatingCharacterService(GameModuleRegistry gameModuleRegistry) {
        this.gameModuleRegistry = gameModuleRegistry;
    }

    @Override
    public CharacterInfo getCharacter(Long characterId) {
        CharacterService delegate = findDelegate();
        if (delegate != null) return delegate.getCharacter(characterId);
        return characters.get(characterId);
    }

    @Override
    public List<CharacterInfo> listCharacters() {
        CharacterService delegate = findDelegate();
        if (delegate != null) return delegate.listCharacters();
        return new ArrayList<>(characters.values());
    }

    @Override
    public void updateAbilityScores(Long characterId, int str, int dex, int con, int intelligence, int wis, int cha) {
        CharacterService delegate = findDelegate();
        if (delegate != null) {
            delegate.updateAbilityScores(characterId, str, dex, con, intelligence, wis, cha);
            return;
        }
        CharacterInfo ci = characters.get(characterId);
        if (ci != null) {
            ci.setStrScore(str);
            ci.setDexScore(dex);
            ci.setConScore(con);
            ci.setIntScore(intelligence);
            ci.setWisScore(wis);
            ci.setChaScore(cha);
        }
    }

    @Override
    public CharacterInfo awardExperience(Long characterId, int xp) {
        CharacterService delegate = findDelegate();
        if (delegate != null) return delegate.awardExperience(characterId, xp);
        CharacterInfo ci = characters.get(characterId);
        if (ci != null) {
            ci.setExperiencePoints(ci.getExperiencePoints() + xp);
        }
        return ci;
    }

    /**
     * Helper to create a character in the fallback store (not in the interface).
     */
    public Long createCharacter(String name) {
        Long id = characterIdCounter.getAndIncrement();
        CharacterInfo ci = new CharacterInfo();
        ci.setId(id);
        ci.setName(name);
        ci.setLevel(1);
        ci.setExperiencePoints(0);
        ci.setHitPoints(10);
        ci.setMaxHitPoints(10);
        characters.put(id, ci);
        return id;
    }

    private CharacterService findDelegate() {
        return gameModuleRegistry.getAllModules().stream()
                .filter(m -> m.isEnabled() && m.getCharacterService() != null)
                .map(GameModule::getCharacterService)
                .findFirst()
                .orElse(null);
    }
}

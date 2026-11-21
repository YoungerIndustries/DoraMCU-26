package com.yisy.modules.doramcu.fourx;

import java.util.List;
import java.util.Map;

/**
 * Interface for 4X character mechanics.
 * Implemented by game-specific character services (B5, CaptainsLog, etc.).
 */
public interface FourxCharacterService {

    Map<String, Object> createCharacter(String name, String race);

    Map<String, Object> createCharacterWithLifepath(String name, String race, String career);

    List<String> getAvailableRaces();

    List<String> getAvailableCareers();

    List<String> getAvailableSkills(String career);

    Map<String, Integer> getRaceModifiers(String race);

    List<Map<String, Object>> getCareerSkills(String career);

    Map<String, Object> advanceCareer(Map<String, Object> character, String career);

    List<Map<String, Object>> getAllCharacters();
}

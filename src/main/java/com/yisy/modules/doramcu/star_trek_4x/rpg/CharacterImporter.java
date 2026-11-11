
package com.yisy.modules.doramcu.star_trek_4x.rpg;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCharacter;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service for importing Captain's Log characters into the 4x game.
 * <p>
 * Maps CaptainLogCharacter attributes, disciplines, and talents to 4x-compatible format.
 * </p>
 */
@Service
public class CharacterImporter {

    private static final Logger log = LoggerFactory.getLogger(CharacterImporter.class);

    private final CaptainLogCharacterRepository characterRepository;

    public CharacterImporter(CaptainLogCharacterRepository characterRepository) {
        this.characterRepository = characterRepository;
    }

    /**
     * Import a Captain's Log character into the 4x game.
     *
     * @param characterId the character ID
     * @return imported character data
     */
    public Map<String, Object> importCharacter(Long characterId) {
        log.info("Importing Captain's Log character: {}", characterId);

        CaptainLogCharacter character = characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));

        // Map attributes
        Map<String, Object> importedData = Map.of(
                "characterId", character.getId(),
                "name", character.getName(),
                "rank", character.getRank() != null ? character.getRank().name() : "ENSIGN",
                "assignment", character.getAssignment() != null ? character.getAssignment().name() : "COMMAND",
                "attributes", Map.of(
                        "fitness", character.getFitness(),
                        "technical", character.getTechnical(),
                        "strength", character.getStrength(),
                        "reason", character.getReason(),
                        "insight", character.getInsight(),
                        "presence", character.getPresence()
                ),
                "disciplines", Map.of(
                        "command", character.getCommand(),
                        "leadership", character.getLeadership(),
                        "science", character.getScience(),
                        "security", character.getSecurity(),
                        "medicine", character.getMedicine(),
                        "operations", character.getOperations()
                ),
                "constitution", character.getConstitution(),
                "imported", true
        );

        log.info("Character '{}' imported successfully", character.getName());
        return importedData;
    }

    /**
     * Get a Captain's Log character by ID.
     *
     * @param characterId the character ID
     * @return the character
     */
    public CaptainLogCharacter getCharacter(Long characterId) {
        return characterRepository.findById(characterId)
                .orElseThrow(() -> new IllegalArgumentException("Character not found: " + characterId));
    }
}

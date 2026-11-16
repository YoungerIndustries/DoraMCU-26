package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for creating persistent empires and AI characters from lore group data.
 * <p>
 * This service is the foundation for the Game Type Overlay System. Each game module
 * (D&D, Star Wars, Traveller, etc.) uses this service to create actual Empire leaders
 * and AI NPCs from its lore data, rather than just logging the data.
 * </p>
 * <p>
 * For example, in D&D:
 * <ul>
 *   <li>Tiamat becomes an AiCharacter leading the Chromatic Dragon empire</li>
 *   <li>Bahamut becomes an AiCharacter leading the Metallic Dragon empire</li>
 *   <li>Asmodeus becomes an AiCharacter leading the Nine Hells empire</li>
 * </ul>
 * </p>
 * <p>
 * The service follows the Babylon5EmpireBuilder pattern but creates persistent
 * AiCharacter entities saved to the database via AiCharacterRepository.
 * </p>
 */
@Service
public class LoreGroupEmpireService {

    private static final Logger log = LoggerFactory.getLogger(LoreGroupEmpireService.class);

    private final AiCharacterRepository aiCharacterRepository;

    public LoreGroupEmpireService(AiCharacterRepository aiCharacterRepository) {
        this.aiCharacterRepository = aiCharacterRepository;
    }

    /**
     * Creates an AI character for an empire leader.
     * <p>
     * Checks if a character with the same name and empire already exists to avoid duplicates.
     * </p>
     *
     * @param name the character's name (e.g., "Tiamat", "Bahamut")
     * @param role the character's role (e.g., "Empire Leader", "Dragon God")
     * @param empireName the empire this character belongs to
     * @param description a description of the character
     * @param primarySkill the character's primary skill
     * @return the created or existing AI character
     */
    @Transactional
    public AiCharacter createEmpireLeader(String name, String role, String empireName,
                                           String description, String primarySkill) {
        // Check for existing character
        Optional<AiCharacter> existing = aiCharacterRepository.findByName(name);
        if (existing.isPresent()) {
            AiCharacter charEntity = existing.get();
            log.info("AI character '{}' already exists for empire '{}'", name, empireName);
            return charEntity;
        }

        AiCharacter aiCharacter = new AiCharacter();
        aiCharacter.setName(name);
        aiCharacter.setRole(role);
        aiCharacter.setEmpireName(empireName);
        aiCharacter.setDescription(description);
        aiCharacter.setPrimarySkill(primarySkill);
        aiCharacter.setActive(true);
        aiCharacter.setHirable(false);
        aiCharacter.setLeadershipRating(10);
        aiCharacter.setTotalExperience(1000);
        aiCharacter.setCharacterType(AiCharacter.CharacterType.NPC);

        AiCharacter saved = aiCharacterRepository.save(aiCharacter);
        log.info("Created empire leader AI character: {} (role: {}, empire: {})", name, role, empireName);
        return saved;
    }

    /**
     * Creates an AI character for an NPC from lore data.
     *
     * @param name the NPC's name
     * @param role the NPC's role (e.g., "Adventurer", "Merchant", "Monster")
     * @param empireName the empire or faction this NPC belongs to
     * @param description a description of the NPC
     * @return the created AI character
     */
    @Transactional
    public AiCharacter createNpc(String name, String role, String empireName, String description) {
        // Check for existing character
        Optional<AiCharacter> existing = aiCharacterRepository.findByName(name);
        if (existing.isPresent()) {
            log.debug("AI character '{}' already exists", name);
            return existing.get();
        }

        AiCharacter aiCharacter = new AiCharacter();
        aiCharacter.setName(name);
        aiCharacter.setRole(role);
        aiCharacter.setEmpireName(empireName);
        aiCharacter.setDescription(description);
        aiCharacter.setActive(true);
        aiCharacter.setHirable(true);
        aiCharacter.setLeadershipRating(5);
        aiCharacter.setCharacterType(AiCharacter.CharacterType.NPC);

        AiCharacter saved = aiCharacterRepository.save(aiCharacter);
        log.info("Created NPC AI character: {} (role: {}, empire: {})", name, role, empireName);
        return saved;
    }

    /**
     * Creates multiple empire leaders for a set of lore groups.
     *
     * @param galaxyName the galaxy name for logging
     * @param loreGroups the lore groups to create leaders for
     * @return the list of created AI characters
     */
    @Transactional
    public List<AiCharacter> createEmpireLeaders(String galaxyName, List<LoreGroup> loreGroups) {
        List<AiCharacter> leaders = new ArrayList<>();
        for (LoreGroup group : loreGroups) {
            for (String leaderName : group.leaderNames) {
                AiCharacter leader = createEmpireLeader(
                        leaderName,
                        group.leaderRole,
                        group.empireName,
                        group.description,
                        group.primarySkill
                );
                leaders.add(leader);
            }
            log.info("Created {} leader(s) for empire '{}' in galaxy '{}'",
                    group.leaderNames.length, group.empireName, galaxyName);
        }
        return leaders;
    }

    /**
     * Creates multiple NPCs for a galaxy.
     *
     * @param galaxyName the galaxy name for logging
     * @param npcs the NPC data to create
     * @return the list of created AI characters
     */
    @Transactional
    public List<AiCharacter> createNpcs(String galaxyName, List<NpcData> npcs) {
        List<AiCharacter> created = new ArrayList<>();
        for (NpcData npc : npcs) {
            AiCharacter aiNpc = createNpc(npc.name, npc.role, npc.empireName, npc.description);
            created.add(aiNpc);
        }
        log.info("Created {} NPCs for galaxy '{}'", created.size(), galaxyName);
        return created;
    }

    /**
     * Represents a lore group (race, faction, species) with its empire and leaders.
     */
    public static class LoreGroup {
        public final String empireName;
        public final String leaderRole;
        public final String[] leaderNames;
        public final String description;
        public final String primarySkill;

        public LoreGroup(String empireName, String leaderRole, String[] leaderNames,
                         String description, String primarySkill) {
            this.empireName = empireName;
            this.leaderRole = leaderRole;
            this.leaderNames = leaderNames;
            this.description = description;
            this.primarySkill = primarySkill;
        }

        public static LoreGroup of(String empireName, String leaderRole, String leaderName,
                                    String description, String primarySkill) {
            return new LoreGroup(empireName, leaderRole, new String[]{leaderName}, description, primarySkill);
        }

        public static LoreGroup of(String empireName, String leaderRole, String[] leaderNames,
                                    String description, String primarySkill) {
            return new LoreGroup(empireName, leaderRole, leaderNames, description, primarySkill);
        }
    }

    /**
     * Represents NPC data for creation.
     */
    public static class NpcData {
        public final String name;
        public final String role;
        public final String empireName;
        public final String description;

        public NpcData(String name, String role, String empireName, String description) {
            this.name = name;
            this.role = role;
            this.empireName = empireName;
            this.description = description;
        }

        public static NpcData of(String name, String role, String empireName) {
            return new NpcData(name, role, empireName, null);
        }

        public static NpcData of(String name, String role, String empireName, String description) {
            return new NpcData(name, role, empireName, description);
        }
    }
}

package com.yisy.modules.doramcu.service;

import com.yisy.modules.doramcu.captainslog.model.CaptainLogCrewMember;
import com.yisy.modules.doramcu.captainslog.repository.CaptainLogCrewMemberRepository;
import com.yisy.modules.doramcu.dto.PlayerCharacterEntryDto;
import com.yisy.modules.doramcu.model.CharacterRole;
import com.yisy.modules.doramcu.model.CharacterType;
import com.yisy.modules.doramcu.model.PlayerCharacterRegistry;
import com.yisy.modules.doramcu.model.User;
import com.yisy.modules.doramcu.repository.PlayerCharacterRegistryRepository;
import com.yisy.modules.doramcu.repository.UserRepository;
import com.yisy.modules.doramcu.traveller.TravellerCharacter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service that manages the player character registry.
 * <p>
 * The registry tracks all of a player's characters across game systems (Traveller,
 * Captain's Log, AI crew) with their role, galaxy location, and AI control state.
 * </p>
 */
@Service
public class PlayerCharacterService {

    private static final Logger log = LoggerFactory.getLogger(PlayerCharacterService.class);

    private final PlayerCharacterRegistryRepository registryRepository;
    private final UserRepository userRepository;
    private final CaptainLogCrewMemberRepository crewMemberRepository;

    public PlayerCharacterService(PlayerCharacterRegistryRepository registryRepository,
                                  UserRepository userRepository,
                                  CaptainLogCrewMemberRepository crewMemberRepository) {
        this.registryRepository = registryRepository;
        this.userRepository = userRepository;
        this.crewMemberRepository = crewMemberRepository;
    }

    /**
     * Registers a Traveller character in the player's registry.
     */
    @Transactional
    public PlayerCharacterRegistry registerTravellerCharacter(User player, TravellerCharacter character, CharacterRole role) {
        if (player == null) throw new IllegalArgumentException("Player is required");
        if (character == null) throw new IllegalArgumentException("Character is required");
        if (role == null) throw new IllegalArgumentException("Role is required");

        int charIdInt = character.getId();
        Long charId = (long) charIdInt;

        // Check if already registered
        if (registryRepository.existsByPlayerAndCharacterTypeAndCharacterId(player, CharacterType.TRAVELLER, charId)) {
            throw new IllegalStateException("Character '" + character.getName() + "' is already registered");
        }

        PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
        entry.setPlayer(player);
        entry.setCharacterType(CharacterType.TRAVELLER);
        entry.setCharacterId(charId);
        entry.setCharacterName(character.getName());
        entry.setCharacterRole(role);
        entry.setGalaxyName(player.getGalaxyName());
        entry.setAiControlled(false);

        entry = registryRepository.save(entry);
        log.info("Registered Traveller character '{}' as {} for player '{}'", character.getName(), role, player.getUsername());
        return entry;
    }

    /**
     * Registers a Captain's Log crew member in the player's registry.
     */
    @Transactional
    public PlayerCharacterRegistry registerCrewMember(User player, CaptainLogCrewMember crew, CharacterRole role) {
        if (player == null) throw new IllegalArgumentException("Player is required");
        if (crew == null) throw new IllegalArgumentException("Crew member is required");
        if (role == null) throw new IllegalArgumentException("Role is required");

        Long crewId = crew.getId();
        if (crewId == null) throw new IllegalArgumentException("Crew member must have an ID");

        // Check if already registered
        if (registryRepository.existsByPlayerAndCharacterTypeAndCharacterId(player, CharacterType.AI_CREW, crewId)) {
            throw new IllegalStateException("Crew member '" + crew.getName() + "' is already registered");
        }

        PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
        entry.setPlayer(player);
        entry.setCharacterType(CharacterType.AI_CREW);
        entry.setCharacterId(crewId);
        entry.setCharacterName(crew.getName());
        entry.setCharacterRole(role);
        entry.setGalaxyName(player.getGalaxyName());
        entry.setAiControlled(true);

        entry = registryRepository.save(entry);
        log.info("Registered crew member '{}' as {} for player '{}'", crew.getName(), role, player.getUsername());
        return entry;
    }

    /**
     * Records an AI encounter for a player.
     */
    @Transactional
    public PlayerCharacterRegistry registerEncounteredAi(User player, String characterName, String galaxyName) {
        if (player == null) throw new IllegalArgumentException("Player is required");
        if (characterName == null || characterName.isBlank()) throw new IllegalArgumentException("Character name is required");
        if (galaxyName == null || galaxyName.isBlank()) throw new IllegalArgumentException("Galaxy name is required");

        PlayerCharacterRegistry entry = new PlayerCharacterRegistry();
        entry.setPlayer(player);
        entry.setCharacterType(CharacterType.AI_CREW);
        entry.setCharacterId(-1L); // No persistent ID for encountered AI
        entry.setCharacterName(characterName);
        entry.setCharacterRole(CharacterRole.ENCOUNTERED);
        entry.setGalaxyName(galaxyName);
        entry.setAiControlled(true);

        entry = registryRepository.save(entry);
        log.info("Recorded AI encounter '{}' for player '{}' in galaxy '{}'", characterName, player.getUsername(), galaxyName);
        return entry;
    }

    /**
     * Gets all characters in a player's registry.
     */
    @Transactional(readOnly = true)
    public List<PlayerCharacterEntryDto> getAllCharacters(User player) {
        if (player == null) throw new IllegalArgumentException("Player is required");

        List<PlayerCharacterRegistry> entries = registryRepository.findByPlayer(player);
        List<PlayerCharacterEntryDto> dtos = new ArrayList<>();
        for (PlayerCharacterRegistry entry : entries) {
            dtos.add(toDto(entry));
        }
        return dtos;
    }

    /**
     * Gets crew members in a specific galaxy for a player.
     */
    @Transactional(readOnly = true)
    public List<PlayerCharacterEntryDto> getCrewInGalaxy(User player, String galaxyName) {
        if (player == null) throw new IllegalArgumentException("Player is required");
        if (galaxyName == null || galaxyName.isBlank()) throw new IllegalArgumentException("Galaxy name is required");

        List<PlayerCharacterRegistry> entries = registryRepository.findByPlayerAndCharacterRoleAndGalaxyName(
                player, CharacterRole.CREW, galaxyName);
        List<PlayerCharacterEntryDto> dtos = new ArrayList<>();
        for (PlayerCharacterRegistry entry : entries) {
            dtos.add(toDto(entry));
        }
        return dtos;
    }

    /**
     * Promotes a character to crew role.
     */
    @Transactional
    public PlayerCharacterEntryDto promoteToCrew(Long registryId) {
        PlayerCharacterRegistry entry = registryRepository.findById(registryId)
                .orElseThrow(() -> new IllegalArgumentException("Registry entry not found: " + registryId));

        entry.setCharacterRole(CharacterRole.CREW);
        entry.setAiControlled(true);
        entry = registryRepository.save(entry);

        log.info("Promoted '{}' to CREW role", entry.getCharacterName());
        return toDto(entry);
    }

    /**
     * Demotes a crew member to owned role.
     */
    @Transactional
    public PlayerCharacterEntryDto demoteFromCrew(Long registryId) {
        PlayerCharacterRegistry entry = registryRepository.findById(registryId)
                .orElseThrow(() -> new IllegalArgumentException("Registry entry not found: " + registryId));

        entry.setCharacterRole(CharacterRole.OWNED);
        entry.setAiControlled(false);
        entry = registryRepository.save(entry);

        log.info("Demoted '{}' from CREW to OWNED", entry.getCharacterName());
        return toDto(entry);
    }

    /**
     * Sets a character as the player's commander.
     */
    @Transactional
    public PlayerCharacterEntryDto setCommander(Long registryId) {
        PlayerCharacterRegistry entry = registryRepository.findById(registryId)
                .orElseThrow(() -> new IllegalArgumentException("Registry entry not found: " + registryId));

        // Demote existing commander of the same type
        User player = entry.getPlayer();
        CharacterType type = entry.getCharacterType();
        registryRepository.findByPlayerAndCharacterRole(player, CharacterRole.COMMANDER)
                .stream()
                .filter(e -> e.getCharacterType() == type && !e.getId().equals(registryId))
                .forEach(e -> e.setCharacterRole(CharacterRole.OWNED));

        entry.setCharacterRole(CharacterRole.COMMANDER);
        entry.setAiControlled(false);
        entry = registryRepository.save(entry);

        log.info("Set '{}' as COMMANDER", entry.getCharacterName());
        return toDto(entry);
    }

    /**
     * Moves all crew AI characters from one galaxy to another.
     * Called when a player travels through a wormhole.
     *
     * @return the number of crew members moved
     */
    @Transactional
    public int moveCrewToGalaxy(User player, String fromGalaxy, String toGalaxy) {
        if (player == null) throw new IllegalArgumentException("Player is required");
        if (fromGalaxy == null || fromGalaxy.isBlank()) throw new IllegalArgumentException("Source galaxy is required");
        if (toGalaxy == null || toGalaxy.isBlank()) throw new IllegalArgumentException("Destination galaxy is required");

        List<PlayerCharacterRegistry> crewEntries = registryRepository
                .findByPlayerAndCharacterRoleAndGalaxyName(player, CharacterRole.CREW, fromGalaxy);

        int moved = 0;
        for (PlayerCharacterRegistry entry : crewEntries) {
            entry.setGalaxyName(toGalaxy);
            entry.setHomeLocationId(null);
            entry.setHomeLocationType("WORMHOLE_GATE");
            registryRepository.save(entry);

            // Update the actual crew member entity if it exists
            if (entry.getCharacterType() == CharacterType.AI_CREW) {
                crewMemberRepository.findById(entry.getCharacterId()).ifPresent(crew -> {
                    if (crew.getGalaxyName() != null) {
                        crew.setGalaxyName(toGalaxy);
                        crewMemberRepository.save(crew);
                    }
                });
            }
            moved++;
        }

        if (moved > 0) {
            log.info("Moved {} crew members for player '{}' from '{}' to '{}'",
                    moved, player.getUsername(), fromGalaxy, toGalaxy);
        }
        return moved;
    }

    /**
     * Removes a character from the registry.
     */
    @Transactional
    public void removeCharacter(Long playerId, Long characterId) {
        registryRepository.deleteByPlayerAndCharacterId(playerId, characterId);
        log.info("Removed character {} from player {}'s registry", characterId, playerId);
    }

    /**
     * Gets a registry entry by ID.
     */
    @Transactional(readOnly = true)
    public Optional<PlayerCharacterEntryDto> getEntry(Long registryId) {
        return registryRepository.findById(registryId).map(this::toDto);
    }

    private PlayerCharacterEntryDto toDto(PlayerCharacterRegistry entry) {
        PlayerCharacterEntryDto dto = new PlayerCharacterEntryDto();
        dto.setId(entry.getId());
        dto.setPlayerId(entry.getPlayer() != null ? entry.getPlayer().getId() : null);
        dto.setPlayerName(entry.getPlayer() != null ? entry.getPlayer().getUsername() : null);
        dto.setCharacterType(entry.getCharacterType());
        dto.setCharacterId(entry.getCharacterId());
        dto.setCharacterName(entry.getCharacterName());
        dto.setCharacterRole(entry.getCharacterRole());
        dto.setGalaxyName(entry.getGalaxyName());
        dto.setAiControlled(entry.isAiControlled());
        dto.setHomeLocationId(entry.getHomeLocationId());
        dto.setHomeLocationType(entry.getHomeLocationType());
        dto.setCreatedAt(entry.getCreatedAt());
        return dto;
    }
}

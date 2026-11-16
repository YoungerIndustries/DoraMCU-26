package com.yisy.modules.doramcu.ai.framework;

import com.yisy.modules.doramcu.eve.model.AiCharacter;
import com.yisy.modules.doramcu.repository.AiCharacterRepository;
import com.yisy.modules.doramcu.service.AiEntityRegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the unified AI network manager.
 */
@Service
public class AiNetworkManagerImpl implements AiNetworkManager {

    private static final Logger log = LoggerFactory.getLogger(AiNetworkManagerImpl.class);

    private final AiCharacterRepository aiCharacterRepository;
    private final AiEntityRegistrationService entityRegistrationService;

    public AiNetworkManagerImpl(AiCharacterRepository aiCharacterRepository,
                                 AiEntityRegistrationService entityRegistrationService) {
        this.aiCharacterRepository = aiCharacterRepository;
        this.entityRegistrationService = entityRegistrationService;
    }

    @Override
    @Transactional(readOnly = true)
    public NetworkStatus getNetworkStatus(String empireName) {
        List<AiCharacter> allEntities = aiCharacterRepository
                .findByCharacterTypeInAndEmpireNameAndActiveTrue(
                        List.of(AiCharacter.CharacterType.SHIP, AiCharacter.CharacterType.STATION,
                                AiCharacter.CharacterType.VEHICLE, AiCharacter.CharacterType.FACILITY), empireName);
        long shipCount = allEntities.stream().filter(e -> e.getCharacterType() == AiCharacter.CharacterType.SHIP).count();
        long stationCount = allEntities.stream().filter(e -> e.getCharacterType() == AiCharacter.CharacterType.STATION).count();
        long vehicleCount = allEntities.stream().filter(e -> e.getCharacterType() == AiCharacter.CharacterType.VEHICLE).count();
        long facilityCount = allEntities.stream().filter(e -> e.getCharacterType() == AiCharacter.CharacterType.FACILITY).count();
        return new NetworkStatus(empireName, allEntities.size(), 0,
                (int) shipCount, (int) stationCount, (int) vehicleCount, (int) facilityCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiCharacter> getActiveEntities(String empireName) {
        return entityRegistrationService.findAllEntityAiCharactersByEmpire(empireName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AiCharacter> getAllEntities(String empireName) {
        return getActiveEntities(empireName);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<AiCharacter.CharacterType, List<AiCharacter>> getTopology(String empireName) {
        return getActiveEntities(empireName).stream()
                .collect(Collectors.groupingBy(AiCharacter::getCharacterType));
    }

    @Override
    @Transactional
    public void registerEntity(AiCharacter aiCharacter) {
        aiCharacterRepository.save(aiCharacter);
    }

    @Override
    @Transactional
    public boolean deactivateEntity(Long entityId) {
        return aiCharacterRepository.findById(entityId).map(c -> {
            c.setActive(false);
            aiCharacterRepository.save(c);
            return true;
        }).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AiCharacter> findEntity(Long entityId) {
        return aiCharacterRepository.findById(entityId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isConnected(String entityId) {
        return entityRegistrationService.findShipAiCharacter(Integer.parseInt(entityId))
                .map(AiCharacter::isActive).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NetworkStatus> getAllNetworksStatus() {
        List<AiCharacter> all = aiCharacterRepository.findByCharacterTypeIn(
                List.of(AiCharacter.CharacterType.SHIP, AiCharacter.CharacterType.STATION,
                        AiCharacter.CharacterType.VEHICLE, AiCharacter.CharacterType.FACILITY));
        Set<String> empires = all.stream().map(AiCharacter::getEmpireName)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        return empires.stream().map(this::getNetworkStatus).collect(Collectors.toList());
    }
}

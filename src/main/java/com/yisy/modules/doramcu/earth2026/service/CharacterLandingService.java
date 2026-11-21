package com.yisy.modules.doramcu.earth2026.service;
import com.yisy.modules.doramcu.model.players.Player;
import org.springframework.stereotype.Service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Character;
import com.yisy.modules.doramcu.earth2026.model.OfficeUnit;
import com.yisy.modules.doramcu.earth2026.model.PropertyListing;
import com.yisy.modules.doramcu.earth2026.model.PropertyTransaction;
import com.yisy.modules.doramcu.earth2026.model.YoungerIndustriesHQ;
import com.yisy.modules.doramcu.earth2026.repository.ApartmentUnitRepository;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026CharacterRepository;
import com.yisy.modules.doramcu.earth2026.repository.OfficeUnitRepository;
import com.yisy.modules.doramcu.earth2026.repository.PropertyListingRepository;
import com.yisy.modules.doramcu.earth2026.repository.PropertyTransactionRepository;
import com.yisy.modules.doramcu.earth2026.repository.YoungerIndustriesHQRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for handling character landing at Younger Industries HQ.
 * <p>
 * New players get their first office assigned for free. Subsequent offices
 * must be bought or rented through the real estate market.
 * </p>
 */
@Service
public class CharacterLandingService {

    private static final Logger log = LoggerFactory.getLogger(CharacterLandingService.class);

    private final Earth2026CharacterRepository characterRepository;
    private final YoungerIndustriesHQRepository hqRepository;
    private final OfficeUnitRepository officeUnitRepository;
    private final PropertyListingRepository listingRepository;
    private final PropertyTransactionRepository transactionRepository;

    public CharacterLandingService(Earth2026CharacterRepository characterRepository,
                                    YoungerIndustriesHQRepository hqRepository,
                                    OfficeUnitRepository officeUnitRepository,
                                    PropertyListingRepository listingRepository,
                                    PropertyTransactionRepository transactionRepository) {
        this.characterRepository = characterRepository;
        this.hqRepository = hqRepository;
        this.officeUnitRepository = officeUnitRepository;
        this.listingRepository = listingRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Land a new character at Younger Industries HQ and assign the first office for free.
     *
     * @param characterId the character to land
     * @return the assigned office unit, or null if no office was available
     */
    @Transactional
    public OfficeUnit landCharacterAtHQ(Long characterId) {
        Earth2026Character character = characterRepository.findById(characterId).orElse(null);
        if (character == null) {
            log.warn("Character not found: {}", characterId);
            return null;
        }

        // Check if character already has an office
        if (character.getOfficeId() != null) {
            log.info("Character {} already has an office: {}", characterId, character.getOfficeId());
            return officeUnitRepository.findById(character.getOfficeId()).orElse(null);
        }

        // Find the first available HQ
        List<YoungerIndustriesHQ> hqs = hqRepository.findByActiveTrue();
        if (hqs.isEmpty()) {
            log.warn("No active HQ buildings found");
            return null;
        }

        YoungerIndustriesHQ hq = hqs.get(0);

        // Find the first available office
        List<OfficeUnit> availableOffices = officeUnitRepository.findByHqIdAndStatus(hq.getId(), "AVAILABLE");
        if (availableOffices.isEmpty()) {
            log.warn("No available offices in HQ: {}", hq.getName());
            return null;
        }

        OfficeUnit office = availableOffices.get(0);

        // Assign the office for free
        office.setStatus("OCCUPIED");
        office.setOwnerId(characterId);
        office.setOwned(true);
        office.setPurchasePrice(BigDecimal.ZERO);
        office.setMonthlyRent(BigDecimal.ZERO);
        office.setAssignedAt(LocalDateTime.now());
        officeUnitRepository.save(office);

        // Update character
        character.setOfficeId(office.getId());
        character.setHomeStructureId(office.getId());
        character.setEarthLocation(hq.getName() + " - Office " + office.getFloorLevel() + "-" + office.getUnitNumber());
        characterRepository.save(character);

        // Update HQ occupancy
        hq.setOccupiedOffices(hq.getOccupiedOffices() + 1);
        hq.setUpdatedAt(LocalDateTime.now());
        hqRepository.save(hq);

        // Update or create listing
        listingRepository.findByBuildingIdAndStatus(hq.getId(), "AVAILABLE").stream()
                .filter(l -> l.getUnitId().equals(office.getId()))
                .findFirst()
                .ifPresent(listing -> {
                    listing.setStatus("SOLD");
                    listing.setOwnerId(characterId);
                    listing.setBuyPrice(BigDecimal.ZERO);
                    listing.setUpdatedAt(LocalDateTime.now());
                    listingRepository.save(listing);
                });

        // Record transaction
        PropertyTransaction tx = new PropertyTransaction(characterId, office.getId(), "BUY", BigDecimal.ZERO);
        tx.setMessage("First office assigned free at " + hq.getName());
        transactionRepository.save(tx);

        log.info("Landed character {} at {} - Office {}-{} (FREE)",
                character.getName(), hq.getName(), office.getFloorLevel(), office.getUnitNumber());

        return office;
    }

    /**
     * Check if a character is a new player (has no office yet).
     *
     * @param characterId the character ID
     * @return true if the character has no office
     */
    @Transactional(readOnly = true)
    public boolean isNewPlayer(Long characterId) {
        Earth2026Character character = characterRepository.findById(characterId).orElse(null);
        return character != null && character.getOfficeId() == null;
    }

    /**
     * Get the character's current office.
     *
     * @param characterId the character ID
     * @return the office unit, or null if not assigned
     */
    @Transactional(readOnly = true)
    public OfficeUnit getCharacterOffice(Long characterId) {
        Earth2026Character character = characterRepository.findById(characterId).orElse(null);
        if (character == null || character.getOfficeId() == null) return null;
        return officeUnitRepository.findById(character.getOfficeId()).orElse(null);
    }

    /**
     * Release a character's office (e.g., when transitioning to another galaxy).
     *
     * @param characterId the character ID
     * @return true if the office was released
     */
    @Transactional
    public boolean releaseCharacterOffice(Long characterId) {
        Earth2026Character character = characterRepository.findById(characterId).orElse(null);
        if (character == null || character.getOfficeId() == null) return false;

        OfficeUnit office = officeUnitRepository.findById(character.getOfficeId()).orElse(null);
        if (office == null) return false;

        // Release the office
        office.setStatus("AVAILABLE");
        office.setOwnerId(null);
        office.setOwned(false);
        office.setAssignedAt(null);
        officeUnitRepository.save(office);

        // Update character
        character.setOfficeId(null);
        characterRepository.save(character);

        // Update HQ occupancy
        YoungerIndustriesHQ hq = hqRepository.findById(office.getHqId()).orElse(null);
        if (hq != null && hq.getOccupiedOffices() > 0) {
            hq.setOccupiedOffices(hq.getOccupiedOffices() - 1);
            hq.setUpdatedAt(LocalDateTime.now());
            hqRepository.save(hq);
        }

        // Update listing
        listingRepository.findByBuildingIdAndStatus(office.getHqId(), "SOLD").stream()
                .filter(l -> l.getUnitId().equals(office.getId()))
                .findFirst()
                .ifPresent(listing -> {
                    listing.setStatus("AVAILABLE");
                    listing.setOwnerId(null);
                    listing.setUpdatedAt(LocalDateTime.now());
                    listingRepository.save(listing);
                });

        log.info("Released office for character {}: {}", characterId, character.getName());
        return true;
    }
}

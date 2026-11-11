package com.yisy.modules.doramcu.earth2026.service;

import com.yisy.modules.doramcu.earth2026.model.Earth2026Chat;
import com.yisy.modules.doramcu.earth2026.repository.Earth2026ChatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing Earth 2026 chat messages.
 * <p>
 * Supports three message types:
 * <ul>
 *   <li>public — location-based messages visible to all characters at a location</li>
 *   <li>private — direct messages between two characters</li>
 *   <li>party — messages visible to all party members</li>
 * </ul>
 * </p>
 */
@Service
public class Earth2026ChatService {

    private static final Logger log = LoggerFactory.getLogger(Earth2026ChatService.class);

    private final Earth2026ChatRepository chatRepository;

    public Earth2026ChatService(Earth2026ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    /**
     * Send a public message at a location.
     *
     * @param senderId the sender character ID
     * @param locationId the location ID
     * @param content the message content
     * @return the saved message
     */
    @Transactional
    public Earth2026Chat sendPublicMessage(Long senderId, Long locationId, String content) {
        Earth2026Chat chat = new Earth2026Chat(senderId, content);
        chat.setLocationId(locationId);
        chat.setMessageType("public");
        chat.setCreatedAt(LocalDateTime.now());
        chat = chatRepository.save(chat);
        log.debug("Public message from {} at location {}: {}", senderId, locationId, content.substring(0, Math.min(50, content.length())));
        return chat;
    }

    /**
     * Send a private message to another character.
     *
     * @param senderId the sender character ID
     * @param recipientId the recipient character ID
     * @param content the message content
     * @return the saved message
     */
    @Transactional
    public Earth2026Chat sendPrivateMessage(Long senderId, Long recipientId, String content) {
        Earth2026Chat chat = new Earth2026Chat(senderId, content);
        chat.setRecipientId(recipientId);
        chat.setMessageType("private");
        chat.setCreatedAt(LocalDateTime.now());
        chat = chatRepository.save(chat);
        log.debug("Private message from {} to {}: {}", senderId, recipientId, content.substring(0, Math.min(50, content.length())));
        return chat;
    }

    /**
     * Send a party message.
     *
     * @param senderId the sender character ID
     * @param partyId the party ID
     * @param content the message content
     * @return the saved message
     */
    @Transactional
    public Earth2026Chat sendPartyMessage(Long senderId, Long partyId, String content) {
        Earth2026Chat chat = new Earth2026Chat(senderId, content);
        chat.setPartyId(partyId);
        chat.setMessageType("party");
        chat.setCreatedAt(LocalDateTime.now());
        chat = chatRepository.save(chat);
        log.debug("Party message from {} in party {}: {}", senderId, partyId, content.substring(0, Math.min(50, content.length())));
        return chat;
    }

    /**
     * Get public messages at a location.
     *
     * @param locationId the location ID
     * @return list of messages ordered by creation time descending
     */
    public List<Earth2026Chat> getLocationMessages(Long locationId) {
        return chatRepository.findByLocationIdOrderByCreatedAtDesc(locationId);
    }

    /**
     * Get private messages between two characters.
     *
     * @param characterId1 the first character ID
     * @param characterId2 the second character ID
     * @return list of messages ordered by creation time descending
     */
    public List<Earth2026Chat> getPrivateMessages(Long characterId1, Long characterId2) {
        return chatRepository.findBySenderIdOrRecipientIdOrderByCreatedAtDesc(characterId1, characterId2);
    }

    /**
     * Get party messages.
     *
     * @param partyId the party ID
     * @return list of messages ordered by creation time descending
     */
    public List<Earth2026Chat> getPartyMessages(Long partyId) {
        return chatRepository.findByPartyIdOrderByCreatedAtDesc(partyId);
    }

    /**
     * Get all messages of a specific type.
     *
     * @param messageType the message type (public, private, party)
     * @return list of messages ordered by creation time descending
     */
    public List<Earth2026Chat> getMessagesByType(String messageType) {
        return chatRepository.findByMessageTypeOrderByCreatedAtDesc(messageType);
    }
}

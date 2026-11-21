package com.yisy.modules.doramcu.service.chat;
import com.yisy.modules.doramcu.model.players.Player;
import com.yisy.modules.doramcu.hex.chat.ChatMessage;
import com.yisy.modules.doramcu.hex.chat.ChatMessage.Channel;
import com.yisy.modules.doramcu.hex.chat.ChatMessage.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages chat messages for game sessions.
 * <p>
 * Stores messages in memory per session, supports channels (general, tactics, ooc),
 * message types (text, system, whisper), history retrieval with pagination,
 * and automatic cleanup of old messages.
 * </p>
 * <p>
 * Each session maintains a circular buffer of up to {@link #MAX_MESSAGES_PER_SESSION} messages.
 * Messages older than {@link #MESSAGE_TTL_MINUTES} are periodically cleaned up.
 * </p>
 */
@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    /** Maximum messages kept per session before oldest are trimmed. */
    public static final int MAX_MESSAGES_PER_SESSION = 500;

    /** Time-to-live for messages in minutes — older messages are cleaned up. */
    public static final int MESSAGE_TTL_MINUTES = 60;

    /** Session ID → list of chat messages (ordered by timestamp). */
    private final ConcurrentHashMap<String, List<ChatMessage>> sessionMessages = new ConcurrentHashMap<>();

    /**
     * Send a chat message to a game session.
     *
     * @param sessionId  the game session ID
     * @param senderId   the player ID sending the message
     * @param senderName optional display name
     * @param content    the message content
     * @param channel    the channel (defaults to GENERAL)
     * @return the created ChatMessage
     */
    public ChatMessage sendMessage(String sessionId, long senderId, String senderName,
                                    String content, Channel channel) {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new IllegalArgumentException("Session ID cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("Message too long (max 1000 characters)");
        }

        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .senderId(senderId)
                .senderName(senderName)
                .content(content.trim())
                .channel(channel != null ? channel : Channel.GENERAL)
                .type(MessageType.TEXT)
                .build();

        sessionMessages.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
        trimOldMessages(sessionId);

        log.debug("Chat message from player {} in session {}: {}", senderId, sessionId, message);
        return message;
    }

    /**
     * Send a whisper message from one player to another.
     *
     * @param sessionId      the game session ID
     * @param senderId       the sender's player ID
     * @param senderName     optional sender display name
     * @param targetPlayerId the target player ID
     * @param content        the message content
     * @return the created ChatMessage
     */
    public ChatMessage sendWhisper(String sessionId, long senderId, String senderName,
                                    long targetPlayerId, String content) {
        if (senderId == targetPlayerId) {
            throw new IllegalArgumentException("Cannot whisper to yourself");
        }

        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .senderId(senderId)
                .senderName(senderName)
                .content(content.trim())
                .channel(Channel.GENERAL)
                .type(MessageType.WHISPER)
                .targetPlayerId(targetPlayerId)
                .build();

        sessionMessages.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
        trimOldMessages(sessionId);

        log.debug("Whisper from player {} to {} in session {}", senderId, targetPlayerId, sessionId);
        return message;
    }

    /**
     * Post a system message (e.g., player joined/left, game events).
     *
     * @param sessionId the game session ID
     * @param content   the system message content
     * @param channel   the channel to post on
     * @return the created ChatMessage
     */
    public ChatMessage postSystemMessage(String sessionId, String content, Channel channel) {
        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .senderId(-1)
                .senderName("System")
                .content(content)
                .channel(channel != null ? channel : Channel.GENERAL)
                .type(MessageType.SYSTEM)
                .build();

        sessionMessages.computeIfAbsent(sessionId, k -> new ArrayList<>()).add(message);
        trimOldMessages(sessionId);

        log.debug("System message in session {}: {}", sessionId, content);
        return message;
    }

    /**
     * Get message history for a session.
     *
     * @param sessionId the game session ID
     * @param channel   filter by channel (null for all channels)
     * @param limit     maximum number of messages to return (0 for all)
     * @return list of messages ordered by timestamp (oldest first)
     */
    public List<ChatMessage> getHistory(String sessionId, Channel channel, int limit) {
        List<ChatMessage> messages = sessionMessages.get(sessionId);
        if (messages == null) {
            return Collections.emptyList();
        }

        List<ChatMessage> filtered;
        if (channel != null) {
            filtered = messages.stream()
                    .filter(m -> m.getChannel() == channel)
                    .collect(Collectors.toList());
        } else {
            filtered = new ArrayList<>(messages);
        }

        if (limit > 0 && filtered.size() > limit) {
            return filtered.subList(filtered.size() - limit, filtered.size());
        }
        return filtered;
    }

    /**
     * Get message history for a session since a given time.
     *
     * @param sessionId the game session ID
     * @param since     only return messages after this timestamp
     * @return list of messages ordered by timestamp
     */
    public List<ChatMessage> getHistorySince(String sessionId, Instant since) {
        List<ChatMessage> messages = sessionMessages.get(sessionId);
        if (messages == null) {
            return Collections.emptyList();
        }
        return messages.stream()
                .filter(m -> m.getTimestamp().isAfter(since))
                .collect(Collectors.toList());
    }

    /**
     * Get messages for a specific player (sent by or whispered to the player).
     *
     * @param sessionId    the game session ID
     * @param playerId     the player ID
     * @return list of messages relevant to the player
     */
    public List<ChatMessage> getPlayerMessages(String sessionId, long playerId) {
        List<ChatMessage> messages = sessionMessages.get(sessionId);
        if (messages == null) {
            return Collections.emptyList();
        }
        return messages.stream()
                .filter(m -> m.getSenderId() == playerId ||
                        (m.getType() == MessageType.WHISPER && m.getTargetPlayerId() == playerId))
                .collect(Collectors.toList());
    }

    /**
     * Get the list of available channels.
     *
     * @return all channel names
     */
    public List<String> getChannels() {
        List<String> channels = new ArrayList<>();
        for (Channel c : Channel.values()) {
            channels.add(c.getName());
        }
        return channels;
    }

    /**
     * Get the message count for a session.
     *
     * @param sessionId the game session ID
     * @return number of messages stored
     */
    public int getMessageCount(String sessionId) {
        List<ChatMessage> messages = sessionMessages.get(sessionId);
        return messages != null ? messages.size() : 0;
    }

    /**
     * Clear all messages for a session (e.g., when session ends).
     *
     * @param sessionId the game session ID
     */
    public void clearSession(String sessionId) {
        List<ChatMessage> removed = sessionMessages.remove(sessionId);
        if (removed != null) {
            log.info("Cleared {} chat messages for session {}", removed.size(), sessionId);
        }
    }

    /**
     * Get statistics for a session's chat activity.
     *
     * @param sessionId the game session ID
     * @return map with totalMessages, messagesByChannel, messagesByType, uniqueSenders
     */
    public Map<String, Object> getStats(String sessionId) {
        List<ChatMessage> messages = sessionMessages.get(sessionId);
        if (messages == null || messages.isEmpty()) {
            return Map.of(
                    "totalMessages", 0,
                    "messagesByChannel", Map.of(),
                    "messagesByType", Map.of(),
                    "uniqueSenders", 0
            );
        }

        Map<String, Integer> byChannel = new ConcurrentHashMap<>();
        Map<String, Integer> byType = new ConcurrentHashMap<>();
        long uniqueSenders = messages.stream().map(ChatMessage::getSenderId).distinct().count();

        for (ChatMessage m : messages) {
            byChannel.merge(m.getChannel().getName(), 1, Integer::sum);
            byType.merge(m.getType().name(), 1, Integer::sum);
        }

        return Map.of(
                "totalMessages", messages.size(),
                "messagesByChannel", byChannel,
                "messagesByType", byType,
                "uniqueSenders", uniqueSenders
        );
    }

    /**
     * Trim old messages from a session to prevent unbounded memory growth.
     */
    private void trimOldMessages(String sessionId) {
        List<ChatMessage> messages = sessionMessages.get(sessionId);
        if (messages == null) {
            return;
        }

        // Remove messages older than TTL
        Instant cutoff = Instant.now().minus(Duration.ofMinutes(MESSAGE_TTL_MINUTES));
        int beforeSize = messages.size();
        messages.removeIf(m -> m.getTimestamp().isBefore(cutoff));

        // Trim to max size if still over limit
        while (messages.size() > MAX_MESSAGES_PER_SESSION) {
            messages.remove(0);
        }

        int removed = beforeSize - messages.size();
        if (removed > 0) {
            log.debug("Trimmed {} old messages from session {}", removed, sessionId);
        }
    }
}

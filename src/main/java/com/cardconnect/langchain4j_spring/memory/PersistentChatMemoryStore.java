package com.cardconnect.langchain4j_spring.memory;

import com.cardconnect.langchain4j_spring.entity.ChatMessageEntity;
import com.cardconnect.langchain4j_spring.repository.ChatMessageRepository;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

/**
 * Persistent implementation of ChatMemoryStore that stores chat history in a database.
 * This allows conversation context to persist across application restarts.
 *
 * <p>Messages are serialized to JSON and stored in the database, keyed by session/memory ID.
 */
@RequiredArgsConstructor
@Slf4j
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * Retrieves chat messages for a given memory/session ID.
     *
     * @param memoryId the session identifier (must be a Long)
     * @return list of chat messages, or empty list if none found
     * @throws ClassCastException if memoryId is not a Long
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        if (!(memoryId instanceof Long)) {
            log.error("Invalid memoryId type: {}. Expected Long, got {}",
                    memoryId, memoryId.getClass().getSimpleName());
            throw new IllegalArgumentException("memoryId must be of type Long");
        }

        Long id = (Long) memoryId;
        log.debug("Retrieving messages for memory ID: {}", id);

        Optional<ChatMessageEntity> entity = chatMessageRepository.findById(id);
        if (entity.isPresent()) {
            try {
                List<ChatMessage> messages = messagesFromJson(entity.get().getContent());
                log.debug("Retrieved {} messages for memory ID: {}", messages.size(), id);
                return messages;
            } catch (Exception e) {
                log.error("Failed to deserialize messages for memory ID: {}", id, e);
                return List.of();
            }
        }

        log.debug("No messages found for memory ID: {}", id);
        return List.of();
    }

    /**
     * Stores or updates chat messages for a given memory/session ID.
     *
     * @param memoryId the session identifier (must be a Long)
     * @param messages the list of chat messages to store
     * @throws ClassCastException if memoryId is not a Long
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (!(memoryId instanceof Long)) {
            log.error("Invalid memoryId type: {}. Expected Long, got {}",
                    memoryId, memoryId.getClass().getSimpleName());
            throw new IllegalArgumentException("memoryId must be of type Long");
        }

        Long id = (Long) memoryId;
        log.debug("Updating {} messages for memory ID: {}", messages.size(), id);

        try {
            String json = messagesToJson(messages);
            ChatMessageEntity entity = new ChatMessageEntity(id, json);
            chatMessageRepository.save(entity);
            log.debug("Successfully updated messages for memory ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to update messages for memory ID: {}", id, e);
            throw new RuntimeException("Failed to persist chat messages", e);
        }
    }

    /**
     * Deletes all chat messages for a given memory/session ID.
     *
     * @param memoryId the session identifier (must be a Long)
     * @throws ClassCastException if memoryId is not a Long
     */
    @Override
    public void deleteMessages(Object memoryId) {
        if (!(memoryId instanceof Long)) {
            log.error("Invalid memoryId type: {}. Expected Long, got {}",
                    memoryId, memoryId.getClass().getSimpleName());
            throw new IllegalArgumentException("memoryId must be of type Long");
        }

        Long id = (Long) memoryId;
        log.info("Deleting messages for memory ID: {}", id);
        chatMessageRepository.deleteById(id);
    }
}
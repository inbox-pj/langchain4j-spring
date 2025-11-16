package com.cardconnect.langchain4j_spring.memory;

import com.cardconnect.langchain4j_spring.entity.ChatMessageEntity;
import com.cardconnect.langchain4j_spring.repository.ChatMessageRepository;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

@AllArgsConstructor
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private ChatMessageRepository chatMessageRepository;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        Optional<ChatMessageEntity> json = chatMessageRepository.findById((Long) memoryId);
        if (json.isPresent()) {
            return messagesFromJson(json.get().getContent());
        } else {
            return List.of();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String json = messagesToJson(messages);
        ChatMessageEntity entity = new ChatMessageEntity((Long) memoryId, json);
        chatMessageRepository.save(entity);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        chatMessageRepository.deleteById((Long) memoryId);
    }
}
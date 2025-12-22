package com.cardconnect.langchain4j_spring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stores chat conversation history in the database.
 * Each entity represents the complete chat history for a session (identified by memoryId).
 * The content is stored as a JSON string containing all messages.
 */
@Entity
@Table(name = "chat_message_entity")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageEntity {

    @Id
    @Column(name = "memory_id")
    private Long memoryId;

    @Column(columnDefinition = "LONGVARCHAR", nullable = false)
    private String content;

    @Override
    public String toString() {
        return "ChatMessageEntity{" +
                "memoryId=" + memoryId +
                ", contentLength=" + (content != null ? content.length() : 0) +
                '}';
    }
}

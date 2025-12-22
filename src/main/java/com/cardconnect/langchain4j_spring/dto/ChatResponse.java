package com.cardconnect.langchain4j_spring.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for chat interactions.
 * Null fields (like sessionId for RAG agent) are excluded from JSON response.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatResponse {

    private String response;
    private String sessionId;
    private LocalDateTime timestamp;

    public static ChatResponse of(String response, String sessionId) {
        return ChatResponse.builder()
                .response(response)
                .sessionId(sessionId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}


package com.cardconnect.langchain4j_spring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for chat interactions.
 * Used for all agent endpoints requiring session management.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequest {

    /**
     * Unique session identifier for maintaining conversation context.
     * Required for stateful agents (Customer Support, Router).
     */
    @NotBlank(message = "Session ID is required")
    @Size(max = 100, message = "Session ID must not exceed 100 characters")
    private String sessionId;

    /**
     * The user's message or question.
     */
    @NotBlank(message = "Message is required")
    @Size(max = 5000, message = "Message must not exceed 5000 characters")
    private String message;
}


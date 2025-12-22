package com.cardconnect.langchain4j_spring.controller;

import com.cardconnect.langchain4j_spring.assistant.CustomerSupportAgent;
import com.cardconnect.langchain4j_spring.assistant.RAGAgent;
import com.cardconnect.langchain4j_spring.assistant.RouterAgent;
import com.cardconnect.langchain4j_spring.dto.ChatRequest;
import com.cardconnect.langchain4j_spring.dto.ChatResponse;
import com.cardconnect.langchain4j_spring.observability.AgentMetricsService;
import dev.langchain4j.service.Result;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI agent interactions.
 * Provides endpoints for different types of AI-powered conversations.
 *
 * <p>Available agents:
 * <ul>
 *   <li>Customer Support Agent - Handles booking-related queries with tools</li>
 *   <li>Router Agent - Routes requests to specialized experts (Medical, Legal, Technical)</li>
 *   <li>RAG Agent - Answers questions using document embeddings</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/agent")
@Slf4j
@Validated
public class AgentController {

    private final CustomerSupportAgent customerSupportAgent;
    private final RouterAgent agent;
    private final RAGAgent ragAgent;
    private final AgentMetricsService metricsService;

    /**
     * Routes user requests to the appropriate expert (medical, legal, or technical).
     * Uses AI to determine which expert should handle the request.
     *
     * @param request the chat request with session ID and message
     * @return structured chat response
     */
    @PostMapping("/ask")
    @Observed(name = "agent.router", contextualName = "router-agent-request")
    public ResponseEntity<ChatResponse> ask(@Valid @RequestBody ChatRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Router agent request - Session: {}, Message length: {}",
                request.getSessionId(), request.getMessage().length());

        try {
            String response = agent.askToExpert(request.getSessionId(), request.getMessage());
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordSuccess("router", duration);
            return ResponseEntity.ok(ChatResponse.of(response, request.getSessionId()));
        } catch (Exception e) {
            log.error("Router agent request failed", e);
            metricsService.recordFailure("router", e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Customer support agent endpoint for booking-related queries.
     * Has access to booking tools and persistent memory.
     *
     * @param request the chat request with session ID and message
     * @return structured chat response
     */
    @PostMapping("/support")
    @Observed(name = "agent.support", contextualName = "support-agent-request")
    public ResponseEntity<ChatResponse> customerSupportAgent(@Valid @RequestBody ChatRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Customer support request - Session: {}, Message length: {}",
                request.getSessionId(), request.getMessage().length());

        try {
            // Convert String sessionId to Long for persistent memory store
            Long memoryId = sessionIdToLong(request.getSessionId());
            log.debug("Converted sessionId '{}' to memoryId: {}", request.getSessionId(), memoryId);

            Result<String> result = customerSupportAgent.answer(memoryId, request.getMessage());
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordSuccess("support", duration);
            return ResponseEntity.ok(ChatResponse.of(result.content(), request.getSessionId()));
        } catch (Exception e) {
            log.error("Customer support request failed", e);
            metricsService.recordFailure("support", e.getClass().getSimpleName());
            throw e;
        }
    }

    /**
     * Converts a String sessionId to a Long for use with persistent memory store.
     * Uses hashCode to ensure consistent conversion for the same sessionId.
     *
     * @param sessionId the session identifier string
     * @return a Long representation of the sessionId
     */
    private Long sessionIdToLong(String sessionId) {
        // Use hashCode and convert to positive long to ensure consistency
        return Math.abs((long) sessionId.hashCode());
    }

    /**
     * RAG (Retrieval-Augmented Generation) endpoint for document-based queries.
     * Answers questions using the embedded story documents.
     * No session required as this is stateless.
     *
     * @param message the user's question about the story
     * @return structured chat response
     */
    @PostMapping("/chat")
    @Observed(name = "agent.rag", contextualName = "rag-agent-request")
    public ResponseEntity<ChatResponse> chat(@RequestParam @NotBlank String message) {
        long startTime = System.currentTimeMillis();
        log.info("RAG agent request - Message length: {}", message.length());

        try {
            String response = ragAgent.retrieve(message);
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordSuccess("rag", duration);
            return ResponseEntity.ok(ChatResponse.of(response, null));
        } catch (Exception e) {
            log.error("RAG agent request failed", e);
            metricsService.recordFailure("rag", e.getClass().getSimpleName());
            throw e;
        }
    }
}
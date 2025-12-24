package com.cardconnect.langchain4j_spring.controller;

import com.cardconnect.langchain4j_spring.agentic.agents.conditional.ExpertRouterAgent;
import com.cardconnect.langchain4j_spring.agentic.agents.sequentional.NovelCreator;
import com.cardconnect.langchain4j_spring.assistant.CustomerSupportAgent;
import com.cardconnect.langchain4j_spring.assistant.RAGAgent;
import com.cardconnect.langchain4j_spring.assistant.RouterAgent;
import com.cardconnect.langchain4j_spring.dto.ChatRequest;
import com.cardconnect.langchain4j_spring.dto.ChatResponse;
import com.cardconnect.langchain4j_spring.dto.CvReview;
import com.cardconnect.langchain4j_spring.observability.AgentMetricsService;
import com.cardconnect.langchain4j_spring.util.StringLoader;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.service.Result;
import io.micrometer.observation.annotation.Observed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

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
@RequestMapping("/api/v1/agent")
@Slf4j
@Validated
public class AgentController {

    private final CustomerSupportAgent customerSupportAgent;
    private final RouterAgent agent;
    private final RAGAgent ragAgent;
    private final AgentMetricsService metricsService;
    private final NovelCreator novelAgent;
    private final UntypedAgent cvReviewerAgent;
    private final ExpertRouterAgent expertRouterAgent;
    private final SupervisorAgent supervisorAgent;
    private final UntypedAgent horoscopeAgent;
    private final UntypedAgent humanInLoopAgent;

    public AgentController(
            CustomerSupportAgent customerSupportAgent,
            RouterAgent agent,
            RAGAgent ragAgent,
            AgentMetricsService metricsService,
            NovelCreator novelAgent,
            @Qualifier("cvReviewer") UntypedAgent cvReviewerAgent,
            ExpertRouterAgent expertRouterAgent,
            @Qualifier("supervisorAgent")
            SupervisorAgent supervisorAgent,
            @Qualifier("writerAgent") UntypedAgent horoscopeAgent,
            @Qualifier("humanInLoopAgent")
            UntypedAgent humanInLoopAgent) {
        this.customerSupportAgent = customerSupportAgent;
        this.agent = agent;
        this.ragAgent = ragAgent;
        this.metricsService = metricsService;
        this.novelAgent = novelAgent;
        this.cvReviewerAgent = cvReviewerAgent;
        this.expertRouterAgent = expertRouterAgent;
        this.supervisorAgent = supervisorAgent;
        this.horoscopeAgent = horoscopeAgent;
        this.humanInLoopAgent = humanInLoopAgent;
    }

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

    @PostMapping("/script")
    @Observed(name = "agent.novel", contextualName = "novel-agent-request")
    public ResponseEntity<ChatResponse> script(@RequestParam @NotBlank String topic,
                                               @RequestParam @NotBlank String style) {
        long startTime = System.currentTimeMillis();

        try {
            String story = novelAgent.createNovel(topic, style);
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordSuccess("novel", duration);
            return ResponseEntity.ok(ChatResponse.of(story, null));
        } catch (Exception e) {
            log.error("novel agent request failed", e);
            metricsService.recordFailure("novel", e.getClass().getSimpleName());
            throw e;
        }
    }

    @PostMapping("/interview")
    @Observed(name = "agent.interview", contextualName = "interview-agent-request")
    public ResponseEntity<CvReview> plan() throws IOException {
        long startTime = System.currentTimeMillis();

        try {
            String candidateCv = StringLoader.loadFromResource("/static/tailored_cv.txt");
            String jobDescription = StringLoader.loadFromResource("/static/job_description_backend.txt");
            String hrRequirements = StringLoader.loadFromResource("/static/hr_requirements.txt");
            String phoneInterviewNotes = StringLoader.loadFromResource("/static/phone_interview_notes.txt");

            Map<String, Object> arguments = Map.of(
                    "candidateCv", candidateCv,
                    "jobDescription", jobDescription
                    , "hrRequirements", hrRequirements
                    , "phoneInterviewNotes", phoneInterviewNotes);


            CvReview review = (CvReview) cvReviewerAgent.invoke(arguments);
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordSuccess("interview", duration);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            log.error("interview agent request failed", e);
            metricsService.recordFailure("interview", e.getClass().getSimpleName());
            throw e;
        }
    }


    @PostMapping("/help")
    @Observed(name = "agent.help", contextualName = "help-agent-request")
    public ResponseEntity<ChatResponse> help(@Valid @RequestBody ChatRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String help = expertRouterAgent.ask(request.getMessage());
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordSuccess("help", duration);
            return ResponseEntity.ok(ChatResponse.of(help, request.getSessionId()));
        } catch (Exception e) {
            log.error("help agent request failed", e);
            metricsService.recordFailure("help", e.getClass().getSimpleName());
            throw e;
        }
    }

    @PostMapping("/bank")
    @Observed(name = "agent.bank", contextualName = "bank-agent-request")
    public ResponseEntity<ChatResponse> banking(@Valid @RequestBody ChatRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            String help = supervisorAgent.invoke(request.getMessage());
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordSuccess("bank", duration);
            return ResponseEntity.ok(ChatResponse.of(help, request.getSessionId()));
        } catch (Exception e) {
            log.error("bank agent request failed", e);
            metricsService.recordFailure("bank", e.getClass().getSimpleName());
            throw e;
        }
    }

    @PostMapping("/write")
    @Observed(name = "agent.write", contextualName = "write-agent-request")
    public ResponseEntity<ChatResponse> horoscope(@Valid @RequestBody ChatRequest request) {
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> input = Map.of("prompt", request.getMessage());
            String result = (String) horoscopeAgent.invoke(input);
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordSuccess("write", duration);
            return ResponseEntity.ok(ChatResponse.of(result, request.getSessionId()));
        } catch (Exception e) {
            log.error("write agent request failed", e);
            metricsService.recordFailure("write", e.getClass().getSimpleName());
            throw e;
        }
    }

    @PostMapping("/human-in-loop")
    @Observed(name = "agent.human-in-loop", contextualName = "human-in-loop-agent-request")
    public ResponseEntity<String> humenInLoop() {
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> input = Map.of("meetingTopic", "on-site visit",
                    "candidateAnswer", "hi",
                    "memoryId", "user-1234");

            String finalDecision = (String) humanInLoopAgent.invoke(input);
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordSuccess("human-in-loop", duration);
            return ResponseEntity.ok(finalDecision);
        } catch (Exception e) {
            log.error("human-in-loop agent request failed", e);
            metricsService.recordFailure("human-in-loop", e.getClass().getSimpleName());
            throw e;
        }
    }
}
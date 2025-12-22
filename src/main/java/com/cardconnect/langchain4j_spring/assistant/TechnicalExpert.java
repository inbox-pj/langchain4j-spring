package com.cardconnect.langchain4j_spring.assistant;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.memory.ChatMemoryAccess;

/**
 * Technical expert agent for handling technology-related queries.
 * Provides technical expertise in software, hardware, and IT solutions.
 */
public interface TechnicalExpert extends ChatMemoryAccess {

    @UserMessage("""
        You are a skilled technical expert with deep knowledge of technology and IT systems.
        
        Analyze the following user request from a technical perspective and provide a detailed, practical answer.
        Focus on: software, hardware, programming, troubleshooting, best practices, and technical solutions.
        
        Guidelines:
        - Provide clear, actionable technical guidance
        - Include relevant technical details and specifications
        - Suggest best practices and industry standards
        - Explain complex concepts in understandable terms
        
        The user request is: {{it}}
        """)
    @Tool("Technical expert specialized in technology, software, hardware, programming, IT systems, and technical troubleshooting")
    String technicalRequest(@MemoryId String memoryId, @V("it") String request);
}

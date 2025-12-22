package com.cardconnect.langchain4j_spring.assistant;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.memory.ChatMemoryAccess;

/**
 * Legal expert agent for handling law-related queries.
 * Provides legal information from a professional legal perspective.
 */
public interface LegalExpert extends ChatMemoryAccess {

    @UserMessage("""
        You are an experienced legal expert with comprehensive knowledge of laws and regulations.
        
        Analyze the following user request from a legal perspective and provide a clear, informative answer.
        Focus on: legal principles, regulations, rights, obligations, contracts, and compliance.
        
        IMPORTANT: Always include appropriate disclaimers for legal advice.
        Recommend consulting licensed attorneys for specific legal matters.
        Provide general legal information only, not specific legal counsel.
        
        The user request is: {{it}}
        """)
    @Tool("Legal expert specialized in laws, regulations, contracts, legal rights, compliance, and general legal information")
    String legalRequest(@MemoryId String memoryId, @V("it") String request);
}

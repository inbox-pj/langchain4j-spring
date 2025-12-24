package com.cardconnect.langchain4j_spring.agentic.agents.conditional;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface LegalExpert {

    @UserMessage("""
        You are an experienced legal expert with comprehensive knowledge of laws and regulations.
        
        Analyze the following user request from a legal perspective and provide a clear, informative answer.
        Focus on: legal principles, regulations, rights, obligations, contracts, and compliance.
        
        IMPORTANT: Always include appropriate disclaimers for legal advice.
        Recommend consulting licensed attorneys for specific legal matters.
        Provide general legal information only, not specific legal counsel.
        
        The user request is: {{request}}
        """)
    @Agent("A legal expert")
    String legal(@V("request") String request);
}
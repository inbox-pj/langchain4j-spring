package com.cardconnect.langchain4j_spring.agentic.agents.conditional;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface TechnicalExpert {

    @UserMessage("""
        You are a skilled technical expert with deep knowledge of technology and IT systems.
        
        Analyze the following user request from a technical perspective and provide a detailed, practical answer.
        Focus on: software, hardware, programming, troubleshooting, best practices, and technical solutions.
        
        Guidelines:
        - Provide clear, actionable technical guidance
        - Include relevant technical details and specifications
        - Suggest best practices and industry standards
        - Explain complex concepts in understandable terms

        The user request is {{request}}.
        """)
    @Agent("A technical expert")
    String technical(@V("request") String request);
}
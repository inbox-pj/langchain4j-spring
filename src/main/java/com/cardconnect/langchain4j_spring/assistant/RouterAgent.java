package com.cardconnect.langchain4j_spring.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.memory.ChatMemoryAccess;

/**
 * Router agent that analyzes user requests and delegates to specialized experts.
 * Routes requests to medical, legal, or technical experts based on content analysis.
 */
public interface RouterAgent extends ChatMemoryAccess {

    @SystemMessage("""
        You are a routing assistant. Route user requests to the appropriate expert:
        - Use medicalRequest for health/medical questions
        - Use legalRequest for legal/law questions  
        - Use technicalRequest for technology/IT questions
        
        After calling the expert, return their complete response verbatim.
        """)
    @UserMessage("{{it}}")
    String askToExpert(@MemoryId String memoryId, @V("it") String request);
}


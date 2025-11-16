package com.cardconnect.langchain4j_spring.assistant;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.UserMessage;

public interface LegalExpert {

    @UserMessage("""
        You are a legal expert.
        Analyze the following user request under a legal point of view and provide the best possible answer.
        The user request is {{it}}.
        """)
    @Tool("A legal expert")
    String legalRequest(String request);
}

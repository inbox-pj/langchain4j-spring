package com.cardconnect.langchain4j_spring.assistant;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.service.UserMessage;

public interface TechnicalExpert {

    @UserMessage("""
        You are a technical expert.
        Analyze the following user request under a technical point of view and provide the best possible answer.
        The user request is {{it}}.
        """)
    @Tool("A technical expert")
    String technicalRequest(String request);
}

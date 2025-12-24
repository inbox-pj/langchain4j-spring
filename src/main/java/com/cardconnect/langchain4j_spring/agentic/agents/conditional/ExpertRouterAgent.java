package com.cardconnect.langchain4j_spring.agentic.agents.conditional;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

public interface ExpertRouterAgent {

    @Agent
    String ask(@V("request") String request);
}
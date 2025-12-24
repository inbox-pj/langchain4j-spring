package com.cardconnect.langchain4j_spring.agentic.agents.sequentional;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.V;

public interface NovelCreator {

    @Agent
    String createNovel(@V("topic") String topic, @V("style") String style);
}
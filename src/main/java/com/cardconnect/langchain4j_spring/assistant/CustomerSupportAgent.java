package com.cardconnect.langchain4j_spring.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface CustomerSupportAgent {

    @SystemMessage(fromResource = "/templates/system-prompt.st")
    Result<String> answer(@MemoryId String memoryId, @UserMessage String userMessage);
}
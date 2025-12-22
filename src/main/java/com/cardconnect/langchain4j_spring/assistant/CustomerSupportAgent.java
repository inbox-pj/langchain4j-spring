package com.cardconnect.langchain4j_spring.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.memory.ChatMemoryAccess;

// @AiService   remove to add more control like binding tool
public interface CustomerSupportAgent extends ChatMemoryAccess {

    @SystemMessage(fromResource = "/templates/system-prompt.st")
    Result<String> answer(@MemoryId Long memoryId, @UserMessage String userMessage);
}


package com.cardconnect.langchain4j_spring.listener;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.spring.event.AiServiceRegisteredEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Listens for AI service registration events and logs the registered tools.
 * Useful for debugging and understanding which tools are available to each agent.
 */
@Component
@Slf4j
class AiServiceRegisteredEventListener implements ApplicationListener<AiServiceRegisteredEvent> {

    @Override
    public void onApplicationEvent(AiServiceRegisteredEvent event) {
        Class<?> aiServiceClass = event.aiServiceClass();
        List<ToolSpecification> toolSpecifications = event.toolSpecifications();
        log.debug("AI Service registered: {}", aiServiceClass.getSimpleName());
        for (int i = 0; i < toolSpecifications.size(); i++) {
            log.debug("  [Tool-{}]: {}", i + 1, toolSpecifications.get(i).name());
        }
    }
}
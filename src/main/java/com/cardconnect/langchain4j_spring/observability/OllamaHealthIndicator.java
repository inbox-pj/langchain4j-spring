package com.cardconnect.langchain4j_spring.observability;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator for Ollama LLM service.
 * Checks if Ollama is running and models are available.
 */
@Component
@Slf4j
public class OllamaHealthIndicator implements HealthIndicator {

    @Value("${langchain4j.ollama.chat-model.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.chat-model.model-name:qwen3:0.6b}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Health health() {
        try {
            // Check if Ollama is responding
            String tagsUrl = ollamaBaseUrl + "/api/tags";
            String response = restTemplate.getForObject(tagsUrl, String.class);

            Map<String, Object> details = new HashMap<>();
            details.put("url", ollamaBaseUrl);
            details.put("model", modelName);
            details.put("status", "UP");

            if (response != null && response.contains(modelName.split(":")[0])) {
                details.put("modelAvailable", true);
                return Health.up()
                        .withDetails(details)
                        .build();
            } else {
                details.put("modelAvailable", false);
                details.put("warning", "Model " + modelName + " not found");
                return Health.up()
                        .withDetails(details)
                        .build();
            }

        } catch (Exception e) {
            log.error("Ollama health check failed", e);
            return Health.down()
                    .withDetail("url", ollamaBaseUrl)
                    .withDetail("model", modelName)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}


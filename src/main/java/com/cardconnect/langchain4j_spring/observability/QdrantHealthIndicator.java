package com.cardconnect.langchain4j_spring.observability;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator for Qdrant vector database.
 * Checks if Qdrant is running and collection exists.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class QdrantHealthIndicator implements HealthIndicator {

    @Value("${langchain4j.qdrant.host:localhost}")
    private String qdrantHost;

    @Value("${langchain4j.qdrant.port:6334}")
    private int qdrantPort;

    @Value("${langchain4j.qdrant.collection-name:story}")
    private String collectionName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Health health() {
        try {
            // Check if Qdrant is responding
            String healthUrl = String.format("http://%s:%d/", qdrantHost, 6333);
            restTemplate.getForObject(healthUrl, String.class);

            // Check collections
            String collectionsUrl = String.format("http://%s:%d/collections", qdrantHost, 6333);
            String response = restTemplate.getForObject(collectionsUrl, String.class);

            Map<String, Object> details = new HashMap<>();
            details.put("host", qdrantHost);
            details.put("port", qdrantPort);
            details.put("collection", collectionName);
            details.put("status", "UP");

            if (response != null && response.contains(collectionName)) {
                details.put("collectionExists", true);
                return Health.up()
                        .withDetails(details)
                        .build();
            } else {
                details.put("collectionExists", false);
                details.put("warning", "Collection " + collectionName + " not found");
                return Health.up()
                        .withDetails(details)
                        .build();
            }

        } catch (Exception e) {
            log.error("Qdrant health check failed", e);
            return Health.down()
                    .withDetail("host", qdrantHost)
                    .withDetail("port", qdrantPort)
                    .withDetail("collection", collectionName)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}


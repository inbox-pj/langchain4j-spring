package com.cardconnect.langchain4j_spring.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

/**
 * Configuration for Qdrant vector database.
 * Creates and manages the embedding store for RAG operations.
 */
@Configuration
@Slf4j
public class QdrantConfig {

    @Value("${langchain4j.qdrant.host:localhost}")
    private String host;

    @Value("${langchain4j.qdrant.port:6334}")
    private int port;

    @Value("${langchain4j.qdrant.collection-name:documents}")
    private String collectionName;

    /**
     * Creates an EmbeddingStore backed by Qdrant.
     * If the collection doesn't exist, it will be created automatically.
     *
     * @param embeddingModel the embedding model to determine vector dimensions
     * @return configured embedding store
     * @throws RuntimeException if connection or collection creation fails
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel) {

        // Create an in-memory embedding store
        // EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        log.info("Initializing Qdrant embedding store - Host: {}, Port: {}, Collection: {}",
                host, port, collectionName);

        QdrantClient qdrantClient = new QdrantClient(
            QdrantGrpcClient.newBuilder(host, port, false).build()
        );

        try {
            // Try to get collection info
            qdrantClient.getCollectionInfoAsync(collectionName).get();
            log.info("Qdrant collection '{}' already exists", collectionName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while checking Qdrant collection", e);
        } catch (ExecutionException e) {
            // Collection doesn't exist, create it
            log.info("Creating Qdrant collection '{}'", collectionName);
            try {
                int vectorSize = embeddingModel.dimension();
                log.info("Using vector size: {}", vectorSize);

                qdrantClient.createCollectionAsync(
                        collectionName,
                        Collections.VectorParams.newBuilder()
                                .setSize(vectorSize)
                                .setDistance(Collections.Distance.Cosine)
                                .build()
                ).get();

                log.info("Successfully created Qdrant collection '{}'", collectionName);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while creating Qdrant collection", ie);
            } catch (ExecutionException ee) {
                log.error("Failed to create Qdrant collection '{}'", collectionName, ee);
                throw new RuntimeException("Failed to create Qdrant collection: " + collectionName, ee);
            }
        }

        return QdrantEmbeddingStore.builder()
            .client(qdrantClient)
            .collectionName(collectionName)
            .build();
    }
}

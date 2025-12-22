package com.cardconnect.langchain4j_spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@Configuration
@ConfigurationProperties(prefix = "app.langchain4j")
@Data
@Validated
public class LangChain4jProperties {

    private ChatMemory chatMemory = new ChatMemory();
    private Rag rag = new Rag();
    private Document document = new Document();

    /**
     * Configuration for chat memory management.
     * Controls how conversation history is stored and managed.
     */
    @Data
    @Validated
    public static class ChatMemory {
        /**
         * Maximum number of messages to keep in chat memory window.
         * Controls conversation context size.
         *
         * @must be between 1 and 100
         */
        @Positive
        @Max(100)
        private int maxMessages;

        /**
         * Whether to persist chat memory to database.
         * When true, conversations survive application restarts.
         */
        private boolean persistEnabled;
    }

    /**
     * Configuration for RAG (Retrieval-Augmented Generation).
     * Controls document retrieval and similarity search behavior.
     */
    @Data
    @Validated
    public static class Rag {
        /**
         * Maximum number of document chunks to retrieve from vector store.
         * Higher values provide more context but increase latency.
         *
         * @must be between 1 and 20
         */
        @Positive
        @Max(20)
        private int maxResults;

        /**
         * Minimum cosine similarity score threshold (0.0 to 1.0).
         * Only documents with similarity above this threshold are returned.
         * Higher values return only very similar documents.
         *
         * @must be between 0.0 and 1.0
         */
        @Min(0)
        @Max(1)
        private double minScore;

        /**
         * Size of text chunks for document splitting in tokens.
         * Smaller chunks provide more precise retrieval but more database entries.
         *
         * @must be positive
         */
        @Positive
        private int chunkSize;

        /**
         * Number of tokens to overlap between consecutive chunks.
         * Helps maintain context across chunk boundaries.
         *
         * @must be non-negative and less than chunkSize
         */
        @Min(0)
        private int chunkOverlap;
    }

    /**
     * Metadata configuration for ingested documents.
     * Used to tag and filter documents in the vector store.
     */
    @Data
    @Validated
    public static class Document {
        /**
         * Unique source identifier for the document.
         * Used for filtering queries to specific document sources.
         *
         * @must not be blank
         */
        @NotBlank
        private String source;

        /**
         * Author of the document.
         * Used for filtering and attribution.
         *
         * @must not be blank
         */
        @NotBlank
        private String author;

        /**
         * Type classification of the document (e.g., "story", "manual", "article").
         *
         * @must not be blank
         */
        @NotBlank
        private String type;

        /**
         * Human-readable title of the document.
         *
         * @must not be blank
         */
        @NotBlank
        private String title;

        /**
         * Language of the document content (e.g., "English", "Spanish").
         *
         * @must not be blank
         */
        @NotBlank
        private String language;
    }
}


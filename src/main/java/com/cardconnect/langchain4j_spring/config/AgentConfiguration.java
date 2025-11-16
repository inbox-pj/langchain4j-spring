package com.cardconnect.langchain4j_spring.config;

import com.cardconnect.langchain4j_spring.assistant.LegalExpert;
import com.cardconnect.langchain4j_spring.assistant.MedicalExpert;
import com.cardconnect.langchain4j_spring.assistant.RouterAgent;
import com.cardconnect.langchain4j_spring.assistant.TechnicalExpert;
import com.cardconnect.langchain4j_spring.memory.PersistentChatMemoryStore;
import com.cardconnect.langchain4j_spring.observability.ModelListenerConfiguration;
import com.cardconnect.langchain4j_spring.repository.ChatMessageRepository;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    @Bean
    RouterAgent routerAgent(ChatModel chatModel) {

        MedicalExpert medicalExpert = AiServices.builder(MedicalExpert.class)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))    // In-memory chat memory
                .chatModel(chatModel)
                .build();

        LegalExpert legalExpert = AiServices.builder(LegalExpert.class)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))    // In-memory chat memory
                .chatModel(chatModel)
                .build();

        TechnicalExpert technicalExpert = AiServices.builder(TechnicalExpert.class)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))    // In-memory chat memory
                .chatModel(chatModel)
                .build();

        return AiServices.builder(RouterAgent.class)
                .chatModel(chatModel)
                .tools(medicalExpert, legalExpert, technicalExpert)    // Tools representing different experts
                .executeToolsConcurrently()         // Executing Tools Concurrently
                .toolArgumentsErrorHandler((error, context) -> ToolErrorHandlerResult.text("something is wrong with tool argument: " + error.getMessage()))        // Tool Error Handling
                .toolExecutionErrorHandler((error, context) -> {
                    throw new RuntimeException(error);
                })
                .build();
    }

    @Bean
    ChatModelListener chatModelListener() {
        return new ModelListenerConfiguration();
    }

    @Bean
    ChatMemoryStore chatMemoryStore(ChatMessageRepository chatMessageRepository) {
        return new PersistentChatMemoryStore(chatMessageRepository);
    }

    @Bean
    ChatMemoryProvider chatMemoryProvider(ChatMemoryStore chatMemoryStore) {
        return memoryId -> MessageWindowChatMemory.builder().id(memoryId).chatMemoryStore(chatMemoryStore).maxMessages(10).build();
    }

    /*@Bean
    EmbeddingModel embeddingModel() {
        // Not the best embedding model, but good enough for this demo
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel, ResourceLoader resourceLoader, TokenCountEstimator tokenizer) throws IOException {

        // Normally, you would already have your embedding store filled with your data.
        // However, for the purpose of this demonstration, we will:

        // 1. Create an in-memory embedding store
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // 2. Load an example document ("Miles of Smiles" terms of use)
        Resource resource = resourceLoader.getResource("classpath:miles-of-smiles-terms-of-use.txt");
        Document document = loadDocument(resource.getFile().toPath(), new TextDocumentParser());

        // 3. Split the document into segments 100 tokens each
        // 4. Convert segments into embeddings
        // 5. Store embeddings into embedding store
        // All this can be done manually, but we will use EmbeddingStoreIngestor to automate this:
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(100, 0, tokenizer);
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(document);

        return embeddingStore;
    }

    @Bean
    ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {

        // You will need to adjust these parameters to find the optimal setting,
        // which will depend on multiple factors, for example:
        // - The nature of your data
        // - The embedding model you are using
        int maxResults = 1;
        double minScore = 0.6;

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .build();
    }*/


}

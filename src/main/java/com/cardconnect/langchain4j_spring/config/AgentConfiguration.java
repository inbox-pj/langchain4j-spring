package com.cardconnect.langchain4j_spring.config;

import com.cardconnect.langchain4j_spring.assistant.CustomerSupportAgent;
import com.cardconnect.langchain4j_spring.assistant.LegalExpert;
import com.cardconnect.langchain4j_spring.assistant.MedicalExpert;
import com.cardconnect.langchain4j_spring.assistant.RAGAgent;
import com.cardconnect.langchain4j_spring.assistant.RouterAgent;
import com.cardconnect.langchain4j_spring.assistant.TechnicalExpert;
import com.cardconnect.langchain4j_spring.memory.PersistentChatMemoryStore;
import com.cardconnect.langchain4j_spring.observability.ModelListenerConfiguration;
import com.cardconnect.langchain4j_spring.repository.ChatMessageRepository;
import com.cardconnect.langchain4j_spring.tools.BookingTools;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.tool.ToolErrorHandlerResult;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;
import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AgentConfiguration {

    private final LangChain4jProperties properties;

    @Bean
    RouterAgent routerAgent(ChatModel chatModel) {
        int maxMessages = properties.getChatMemory().getMaxMessages();

        MedicalExpert medicalExpert = AiServices.builder(MedicalExpert.class)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(maxMessages))
                .chatModel(chatModel)
                .build();

        LegalExpert legalExpert = AiServices.builder(LegalExpert.class)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(maxMessages))
                .chatModel(chatModel)
                .build();

        TechnicalExpert technicalExpert = AiServices.builder(TechnicalExpert.class)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(maxMessages))
                .chatModel(chatModel)
                .build();

        return AiServices.builder(RouterAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(maxMessages))
                .tools(medicalExpert, legalExpert, technicalExpert)
                .executeToolsConcurrently()
                .toolArgumentsErrorHandler((error, context) ->
                    ToolErrorHandlerResult.text("Something is wrong with tool argument: " + error.getMessage()))
                .toolExecutionErrorHandler((error, context) -> {
                    throw new RuntimeException(error);
                })
                .build();
    }

    @Bean
    CustomerSupportAgent customerSupportAgent(
            ChatModel chatModel,
            ChatMemoryProvider chatMemoryProvider,
            BookingTools bookingTools) {

        log.info("Creating CustomerSupportAgent with BookingTools");

        return AiServices.builder(CustomerSupportAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider)
                .tools(bookingTools)  // Wire BookingTools for booking operations
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
        int maxMessages = properties.getChatMemory().getMaxMessages();
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .chatMemoryStore(chatMemoryStore)
                .maxMessages(maxMessages)
                .build();
    }

    @Bean
    EmbeddingStoreIngestor documentIngestor(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel,
            ResourceLoader resourceLoader) throws IOException {

        // Load the document
        Resource resource = resourceLoader.getResource("classpath:/static/story-about-happy-carrot.txt");
        String documentId = generateDocumentId(resource);

        // Remove existing embeddings if document already ingested
        if (isDocumentIngested(embeddingStore, embeddingModel, documentId)) {
            log.info("Document already ingested, removing existing embeddings");
            removeEmbeddings(embeddingStore);
        }

        Document document = loadDocument(resource.getFile().toPath(), new TextDocumentParser());

        // Use configuration properties for metadata
        LangChain4jProperties.Document docProps = properties.getDocument();
        document.metadata().put("document_id", documentId);
        document.metadata().put("source", docProps.getSource());
        document.metadata().put("type", docProps.getType());
        document.metadata().put("author", docProps.getAuthor());
        document.metadata().put("title", docProps.getTitle());
        document.metadata().put("language", docProps.getLanguage());

        // 1. Split the document into segments 100 tokens each
        // DocumentSplitter splitter = DocumentSplitters.recursive(600, 0);
        // 2. Convert segments into embeddings
        // List<TextSegment> segments = splitter.split(document);
        // 3. Store embeddings into embedding store
        // List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        // embeddingStore.addAll(embeddings, segments);
        // All this can be done manually, but we will use EmbeddingStoreIngestor to automate this:
        // Create ingestor with configured chunk size
        int chunkSize = properties.getRag().getChunkSize();
        int chunkOverlap = properties.getRag().getChunkOverlap();
        DocumentSplitter documentSplitter = DocumentSplitters.recursive(chunkSize, chunkOverlap);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentSplitter)
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();



        log.info("Ingesting document: {} (chunk size: {}, overlap: {})",
                docProps.getTitle(), chunkSize, chunkOverlap);
        ingestor.ingest(document);

        return ingestor;
    }

    private boolean isDocumentIngested(EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel, String documentId) {
        // Create a dummy embedding for the search request
        Embedding queryEmbedding = embeddingModel.embed("test").content();

        // Query embedding store for existing document
        String source = properties.getDocument().getSource();
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .filter(metadataKey("document_id").isEqualTo(documentId)
                        .and(metadataKey("source").isEqualTo(source)))
                .queryEmbedding(queryEmbedding)
                .maxResults(1)
                .minScore(0.0)
                .build();

        return !embeddingStore.search(request).matches().isEmpty();
    }

    private void removeEmbeddings(EmbeddingStore<TextSegment> embeddingStore) {
        embeddingStore.removeAll();
        log.debug("Removed all existing embeddings from the store");
    }

    private String generateDocumentId(Resource resource) throws IOException {
        try (InputStream inputStream = resource.getInputStream()) {
            return DigestUtils.md5DigestAsHex(inputStream);
        }
    }

    @Bean
    ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {
        int maxResults = properties.getRag().getMaxResults();
        double minScore = properties.getRag().getMinScore();
        String source = properties.getDocument().getSource();
        String author = properties.getDocument().getAuthor();

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(maxResults)
                .minScore(minScore)
                .filter(metadataKey("source").isEqualTo(source)
                        .and(metadataKey("author").isEqualTo(author)))
                .build();
    }

    @Bean
    RetrievalAugmentor retrievalAugmentor(ContentRetriever contentRetriever) {
        return DefaultRetrievalAugmentor.builder()
                .queryRouter(new DefaultQueryRouter(contentRetriever))
                .build();
    }

    @Bean
    RAGAgent ragAgent(ChatModel chatModel, RetrievalAugmentor retrievalAugmentor) {
        return AiServices.builder(RAGAgent.class)
                .chatModel(chatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }

}

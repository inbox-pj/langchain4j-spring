package com.cardconnect.langchain4j_spring.config;

import com.cardconnect.langchain4j_spring.agentic.agents.conditional.CategoryRouter;
import com.cardconnect.langchain4j_spring.agentic.agents.conditional.ExpertRouterAgent;
import com.cardconnect.langchain4j_spring.agentic.agents.humantntheloop.DecisionsReachedService;
import com.cardconnect.langchain4j_spring.agentic.agents.humantntheloop.MeetingProposer;
import com.cardconnect.langchain4j_spring.agentic.agents.parellel.HrCvReviewer;
import com.cardconnect.langchain4j_spring.agentic.agents.parellel.ManagerCvReviewer;
import com.cardconnect.langchain4j_spring.agentic.agents.parellel.TeamMemberCvReviewer;
import com.cardconnect.langchain4j_spring.agentic.agents.planner.HoroscopeGenerator;
import com.cardconnect.langchain4j_spring.agentic.agents.planner.PersonExtractor;
import com.cardconnect.langchain4j_spring.agentic.agents.planner.SignExtractor;
import com.cardconnect.langchain4j_spring.agentic.agents.planner.StoryFinder;
import com.cardconnect.langchain4j_spring.agentic.agents.planner.Writer;
import com.cardconnect.langchain4j_spring.agentic.agents.sequentional.CreativeWriter;
import com.cardconnect.langchain4j_spring.agentic.agents.sequentional.NovelCreator;
import com.cardconnect.langchain4j_spring.agentic.agents.sequentional.StyleEditor;
import com.cardconnect.langchain4j_spring.agentic.agents.supervised.CreditAgent;
import com.cardconnect.langchain4j_spring.agentic.agents.supervised.ExchangeAgent;
import com.cardconnect.langchain4j_spring.agentic.agents.supervised.WithdrawAgent;
import com.cardconnect.langchain4j_spring.assistant.CustomerSupportAgent;
import com.cardconnect.langchain4j_spring.assistant.LegalExpert;
import com.cardconnect.langchain4j_spring.assistant.MedicalExpert;
import com.cardconnect.langchain4j_spring.assistant.RAGAgent;
import com.cardconnect.langchain4j_spring.assistant.RouterAgent;
import com.cardconnect.langchain4j_spring.assistant.TechnicalExpert;
import com.cardconnect.langchain4j_spring.dto.CvReview;
import com.cardconnect.langchain4j_spring.dto.RequestCategory;
import com.cardconnect.langchain4j_spring.memory.PersistentChatMemoryStore;
import com.cardconnect.langchain4j_spring.observability.ModelListenerConfiguration;
import com.cardconnect.langchain4j_spring.repository.ChatMessageRepository;
import com.cardconnect.langchain4j_spring.tools.BankTool;
import com.cardconnect.langchain4j_spring.tools.BookingTools;
import com.cardconnect.langchain4j_spring.tools.ExchangeTool;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.agent.ErrorRecoveryResult;
import dev.langchain4j.agentic.patterns.goap.GoalOrientedPlanner;
import dev.langchain4j.agentic.supervisor.SupervisorAgent;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
import dev.langchain4j.agentic.workflow.HumanInTheLoop;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.DigestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;

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


    @Bean
    NovelCreator novelCreator(ChatModel chatModel) {
        CreativeWriter creativeWriter = AgenticServices
                .agentBuilder(CreativeWriter.class)
                .chatModel(chatModel)
                .build();

        StyleEditor styleEditor = AgenticServices
                .agentBuilder(StyleEditor.class)
                .chatModel(chatModel)
                .build();

//        StyleScorer styleScorer = AgenticServices
//                .agentBuilder(StyleScorer.class)
//                .chatModel(chatModel)
//                .outputKey("score")
//                .build();

//        UntypedAgent styleReviewLoop = AgenticServices
//                .loopBuilder()
//                .subAgents(styleScorer, styleEditor)
//                .maxIterations(5)
//                .exitCondition( agenticScope -> agenticScope.readState("score", 0.0) >= 0.8)
//                .build();

        return AgenticServices
                .sequenceBuilder(NovelCreator.class)
               // .subAgents(creativeWriter, styleReviewLoop)
                .subAgents(creativeWriter, styleEditor)
                .outputKey("story")
                .build();
    }

    @Bean
    @Qualifier("cvReviewer")
    UntypedAgent cvReviewer(ChatModel chatModel) {
        HrCvReviewer hrCvReviewer = AgenticServices.agentBuilder(HrCvReviewer.class)
                .chatModel(chatModel)
                .outputKey("hrReview") // this will be overwritten in every iteration, and also be used as the final output we want to observe
                .build();

        ManagerCvReviewer managerCvReviewer = AgenticServices.agentBuilder(ManagerCvReviewer.class)
                .chatModel(chatModel)
                .outputKey("managerReview") // this overwrites the original input instructions, and is overwritten in every iteration and used as new instructions for the CvTailor
                .build();

        TeamMemberCvReviewer teamMemberCvReviewer = AgenticServices.agentBuilder(TeamMemberCvReviewer.class)
                .chatModel(chatModel)
                .outputKey("teamMemberReview") // this overwrites the original input instructions, and is overwritten in every iteration and used as new instructions for the CvTailor
                .build();

        UntypedAgent cvReviewGenerator = AgenticServices
                .parallelBuilder()
                .subAgents(hrCvReviewer, managerCvReviewer, teamMemberCvReviewer)
                .executor(Executors.newFixedThreadPool(3))
                .outputKey("fullCvReview") // this is the final output we want to observe
                .output(agenticScope -> {
                    // read the outputs of each reviewer from the agentic scope
                    CvReview hrReview = (CvReview) agenticScope.readState("hrReview");
                    CvReview managerReview = (CvReview) agenticScope.readState("managerReview");
                    CvReview teamMemberReview = (CvReview) agenticScope.readState("teamMemberReview");
                    // return a bundled review with averaged score (or any other aggregation you want here)
                    String feedback = String.join("\n",
                            "HR Review: " + hrReview.feedback,
                            "Manager Review: " + managerReview.feedback,
                            "Team Member Review: " + teamMemberReview.feedback
                    );
                    double avgScore = (hrReview.score + managerReview.score + teamMemberReview.score) / 3.0;

                    return new CvReview(avgScore, feedback);
                })
                .errorHandler(errorContext -> {
                    //ErrorRecoveryResult.retry();
                    log.error("error handling cvReviewer", errorContext);   // handle errors appropriately
                    return ErrorRecoveryResult.throwException();
                })
                .build();

        return cvReviewGenerator;
    }

    @Bean
    ExpertRouterAgent expertRouterAgent(ChatModel chatModel) {
        com.cardconnect.langchain4j_spring.agentic.agents.conditional.MedicalExpert medicalExpert = AgenticServices
                .agentBuilder(com.cardconnect.langchain4j_spring.agentic.agents.conditional.MedicalExpert.class)
                .chatModel(chatModel)
                .outputKey("response")
              //  .async(true)  Asyncronous execution
                .build();
        com.cardconnect.langchain4j_spring.agentic.agents.conditional.LegalExpert legalExpert = AgenticServices
                .agentBuilder(com.cardconnect.langchain4j_spring.agentic.agents.conditional.LegalExpert.class)
                .chatModel(chatModel)
                .outputKey("response")
                //  .async(true)  Asyncronous execution
                .build();
        com.cardconnect.langchain4j_spring.agentic.agents.conditional.TechnicalExpert technicalExpert = AgenticServices
                .agentBuilder(com.cardconnect.langchain4j_spring.agentic.agents.conditional.TechnicalExpert.class)
                .chatModel(chatModel)
                .outputKey("response")
                //  .async(true)  Asyncronous execution
                .build();
        CategoryRouter routerAgent = AgenticServices
                .agentBuilder(CategoryRouter.class)
                .chatModel(chatModel)
                .outputKey("category")
                //  .async(true)  Asyncronous execution
                .build();

        UntypedAgent expertsAgent = AgenticServices.conditionalBuilder()
                .subAgents( agenticScope -> agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.MEDICAL, medicalExpert)
                .subAgents( agenticScope -> agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.LEGAL, legalExpert)
                .subAgents( agenticScope -> agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.TECHNICAL, technicalExpert)
                .build();

        ExpertRouterAgent expertRouterAgent = AgenticServices
                .sequenceBuilder(ExpertRouterAgent.class)
                .subAgents(routerAgent, expertsAgent)
                .outputKey("response")
                .build();

        return expertRouterAgent;
    }

    @Bean
    @Qualifier("supervisorAgent")
    SupervisorAgent supervisorAgent(ChatModel chatModel) {
        BankTool bankTool = new BankTool();
        bankTool.createAccount("Mario", 1000.0);
        bankTool.createAccount("Georgios", 1000.0);

        WithdrawAgent withdrawAgent = AgenticServices
                .agentBuilder(WithdrawAgent.class)
                .chatModel(chatModel)
                .tools(bankTool)
                .build();
        CreditAgent creditAgent = AgenticServices
                .agentBuilder(CreditAgent.class)
                .chatModel(chatModel)
                .tools(bankTool)
                .build();

        ExchangeAgent exchangeAgent = AgenticServices
                .agentBuilder(ExchangeAgent.class)
                .chatModel(chatModel)
                .tools(new ExchangeTool())
                .build();

        SupervisorAgent bankSupervisor = AgenticServices
                .supervisorBuilder()
                .chatModel(chatModel)
                .subAgents(withdrawAgent, creditAgent, exchangeAgent)
                .responseStrategy(SupervisorResponseStrategy.SUMMARY)
                .contextGenerationStrategy(SupervisorContextStrategy.SUMMARIZATION)
                .supervisorContext("Policies: prefer internal tools; currency USD; no external APIs")
                .build();

        return bankSupervisor;
    }

    @Bean
    @Qualifier("writerAgent")
    UntypedAgent writerAgent(ChatModel chatModel) {
        PersonExtractor personExtractor = AgenticServices.agentBuilder(PersonExtractor.class)
                .chatModel(chatModel)
                .outputKey("person")
                .build();
        SignExtractor signExtractor = AgenticServices.agentBuilder(SignExtractor.class)
                .chatModel(chatModel)
                .outputKey("sign")
                .build();
        HoroscopeGenerator horoscopeGenerator = AgenticServices.agentBuilder(HoroscopeGenerator.class)
                .chatModel(chatModel)
                .outputKey("horoscope")
                .build();
        StoryFinder storyFinder = AgenticServices.agentBuilder(StoryFinder.class)
                .chatModel(chatModel)
                .outputKey("story")
                .build();
        Writer writer = AgenticServices.agentBuilder(Writer.class)
                .chatModel(chatModel)
                .outputKey("writeup")
                .build();

        UntypedAgent horoscopeAgent = AgenticServices.plannerBuilder()
                .subAgents(personExtractor, signExtractor, horoscopeGenerator, storyFinder, writer)
                .outputKey("writeup")
                .planner(GoalOrientedPlanner::new)
                .build();

        return horoscopeAgent;
    }

    @Bean
    @Qualifier("humanInLoopAgent")
    UntypedAgent humenInLoopAgent(ChatModel chatModel) {
        // Create involved agents
        MeetingProposer proposer = AgenticServices
                .agentBuilder(MeetingProposer.class)
                .chatModel(chatModel)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(15)) // so the agent remembers what he proposed already
                .outputKey("proposal")
                .build();
        DecisionsReachedService decisionService = AgenticServices.agentBuilder(DecisionsReachedService.class)
                .chatModel(chatModel)
                .outputKey("decisionsReached")
                .build();

        HumanInTheLoop humanInTheLoop = AgenticServices
                .humanInTheLoopBuilder()
                .description("agent that asks input from the user")
                .outputKey("candidateAnswer") // matches one of the proposer's input variable names
                .inputKey("proposal") // must match the output of the proposer agent
                .requestWriter(request -> {
                    System.out.println(request);
                    System.out.print(">>>>>>>>>>>>>>>>>>>>>>>>>> ");
                })
                .responseReader(() -> new Scanner(System.in).nextLine())
                .async(true) // no need to block the entire program while waiting for user input
                .build();

        // Chain agents into a workflow
        UntypedAgent agentSequence = AgenticServices
                .sequenceBuilder()
                .subAgents(proposer, humanInTheLoop)
                .output(agenticScope -> Map.of(
                        "proposal", agenticScope.readState("proposal"),
                        "candidateAnswer", agenticScope.readState("candidateAnswer")
                ))
                .outputKey("proposalAndAnswer")
                .build();

        UntypedAgent schedulingLoop = AgenticServices
                .loopBuilder()
                .subAgents(agentSequence)
                .exitCondition(scope -> {
                    System.out.println("--- checking exit condition ---");
                    String response = (String) scope.readState("candidateAnswer");
                    String proposal = (String) scope.readState("proposal");
                    return response != null && decisionService.isDecisionReached(proposal, response);
                })
                .outputKey("proposalAndAnswer")
                .maxIterations(5)
                .build();

        return schedulingLoop;
    }
}

# LangChain4j Spring Boot Application

A comprehensive enterprise-grade Spring Boot application showcasing advanced AI capabilities using LangChain4j framework. This application demonstrates multiple agentic patterns including sequential, parallel, conditional, supervised, planner, and human-in-the-loop workflows with RAG (Retrieval-Augmented Generation), persistent chat memory, and production-ready observability.

## ✨ Key Features

### 🤖 Advanced AI Agents
- **9 Production-Ready Endpoints** - Comprehensive REST API for various AI scenarios
- **6 Agentic Design Patterns** - Sequential, Parallel, Conditional, Supervised, Planner, Human-in-Loop
- **Classical Agents** - Customer Support, Router, RAG with specialized capabilities
- **Persistent Memory** - Chat history stored in H2 database across sessions
- **Tool Integration** - BookingTools, BankTool, ExchangeTool for real-world operations

### 🧠 LangChain4j Integration
- **Multiple Agents** - CustomerSupportAgent, RouterAgent, RAGAgent, and specialized experts
- **Expert Routing** - Automatic selection between Medical, Legal, and Technical experts
- **RAG Capabilities** - Document embedding and semantic search with Qdrant
- **Memory Management** - Persistent and in-memory chat history
- **Tool Calling** - Agents can execute functions (bookings, banking operations)

### 📊 Production-Grade Observability
- **Distributed Tracing** - Full request flow visibility with Jaeger + OpenTelemetry
- **Custom Metrics** - Agent performance, token usage, tool invocations
- **Health Indicators** - Custom health checks for Ollama, Qdrant, Database
- **Prometheus Integration** - Metrics scraping and time-series storage
- **Grafana Dashboards** - Visual monitoring and alerting
- **Structured Logging** - Trace/Span IDs in all logs

### 🎯 Real-World Use Cases
- **Customer Support** - Booking management with conversational AI
- **Document Q&A** - RAG-powered knowledge base queries
- **Content Generation** - Sequential novel/story creation
- **Resume Analysis** - Parallel CV review by multiple evaluators
- **Banking Operations** - Supervised multi-agent financial transactions
- **Personalized Content** - Planner-based horoscope generation
- **Decision Support** - Human-in-the-loop approval workflows

### 🔧 Enterprise Ready
- **Spring Boot 3.5.7** - Latest stable version
- **Java 21** - Modern Java features
- **Docker Compose** - Easy infrastructure setup
- **Flyway Migrations** - Database version control
- **Request Validation** - Input sanitization and validation
- **Error Handling** - Global exception handling
- **API Documentation** - Comprehensive endpoint documentation

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+**
- **Docker** (for Qdrant and monitoring stack)
- **Ollama** installed and running

## 🛠️ Quick Start

### 1. Start Ollama

```bash
# Pull required models
ollama pull qwen3:0.6b
ollama pull nomic-embed-text:latest

# Verify Ollama is running
curl http://localhost:11434/api/tags
```

### 2. Start Qdrant Vector Database

```bash
docker run -d -p 6333:6333 -p 6334:6334 \
  -v $(pwd)/qdrant_storage:/qdrant/storage \
  qdrant/qdrant
```

### 3. Start Observability Stack (Jaeger, Prometheus, Grafana)

```bash
# Start all monitoring services
docker-compose up -d

# Verify all services are running
docker ps
```

### 4. Build and Run Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

## 📚 API Endpoints

All endpoints use modern RESTful design with JSON request/response and comprehensive observability.

### 1. Customer Support Agent (Tools + Memory)

**Endpoint**: `POST /api/v1/agent/support`

**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/agent/support \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "user123",
    "message": "I need help with booking BN123 for John Doe"
  }'
```

**Response**:
```json
{
  "response": "Let me check your booking. I found booking BN123 for John Doe...",
  "sessionId": "user123",
  "timestamp": "2025-12-24T10:30:00"
}
```

**Features**:
- ✅ Persistent chat memory across sessions
- ✅ Access to BookingTools (get, cancel bookings)
- ✅ Customer support scenarios

---

### 2. Router Agent (Automatic Expert Selection)

**Endpoint**: `POST /api/v1/agent/ask`

**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/agent/ask \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "user123",
    "message": "What are the symptoms of flu?"
  }'
```

**Response**:
```json
{
  "response": "From a medical perspective, flu symptoms include...",
  "sessionId": "user123",
  "timestamp": "2025-12-24T10:30:00"
}
```

**Available Experts**:
- 🩺 **Medical Expert** - Health, symptoms, treatments, medications
- ⚖️ **Legal Expert** - Laws, regulations, contracts, rights
- 💻 **Technical Expert** - Technology, software, hardware, programming

---

### 3. RAG Agent (Document Query)

**Endpoint**: `POST /api/v1/agent/chat?message={your_question}`

**Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/agent/chat?message=Tell%20me%20about%20Charlie"
```

**Response**:
```json
{
  "response": "Charlie is a happy carrot who lives in VeggieVille...",
  "timestamp": "2025-12-24T10:30:00"
}
```

**Features**:
- 📚 Semantic search over embedded documents (Qdrant vector store)
- 🎯 Context-aware responses from document content
- 🔍 Only answers from embedded knowledge base

---

### 4. Conditional Expert Router (Agentic Pattern)

**Endpoint**: `POST /api/v1/agent/help`

**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/agent/help \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "user456",
    "message": "Can you explain GDPR compliance requirements?"
  }'
```

**Pattern**: Conditional routing using AI to determine best expert

---

### 5. Sequential Novel Creator (Sequential Agentic Pattern)

**Endpoint**: `POST /api/v1/agent/script?topic={topic}&style={style}`

**Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/agent/script?topic=space%20adventure&style=sci-fi"
```

**Pattern**: Sequential agent execution
1. Creative Writer creates initial story
2. Style Editor refines the writing
3. Style Scorer evaluates the quality

---

### 6. Parallel CV Reviewer (Parallel Agentic Pattern)

**Endpoint**: `POST /api/v1/agent/interview`

**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/agent/interview
```

**Response**:
```json
{
  "candidateName": "John Doe",
  "overallRecommendation": "STRONG_HIRE",
  "hrEvaluation": "...",
  "technicalEvaluation": "...",
  "managerEvaluation": "...",
  "finalScore": 85
}
```

**Pattern**: Parallel agent execution - three reviewers evaluate simultaneously:
- 👨‍💼 **HR Reviewer** - Culture fit, communication, background
- 👨‍💻 **Technical Reviewer** - Skills, experience, technical competency
- 👔 **Manager Reviewer** - Leadership, team fit, overall assessment

---

### 7. Supervised Banking Agent (Supervised Agentic Pattern)

**Endpoint**: `POST /api/v1/agent/bank`

**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/agent/bank \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "bank789",
    "message": "I want to exchange 1000 USD to EUR"
  }'
```

**Pattern**: Supervisor coordinates specialized worker agents:
- 💰 **Credit Agent** - Credit operations with BankTool
- 💱 **Exchange Agent** - Currency exchange with ExchangeTool
- 🏦 **Withdraw Agent** - Withdrawal operations with BankTool

---

### 8. Planner Agent - Horoscope Writer (Planner Agentic Pattern)

**Endpoint**: `POST /api/v1/agent/write`

**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/agent/write \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "astro001",
    "message": "Write horoscope for Alice born on May 5th"
  }'
```

**Pattern**: Multi-step planning with intermediate agents:
1. PersonExtractor extracts person details
2. SignExtractor determines zodiac sign
3. StoryFinder retrieves relevant story segments
4. HoroscopeGenerator creates personalized horoscope
5. Writer produces final formatted output

---

### 9. Human-in-the-Loop Agent (Human-in-the-Loop Pattern)

**Endpoint**: `POST /api/v1/agent/human-in-loop`

**Request**:
```bash
curl -X POST http://localhost:8080/api/v1/agent/human-in-loop
```

**Pattern**: Agent proposes decisions and waits for human approval
- 🤝 **MeetingProposer** - Suggests meeting schedules
- ✅ **DecisionsReachedService** - Human reviews and approves/rejects
- 🔄 Loop continues until human accepts the proposal

---

---

## 🤖 Agentic Patterns Implemented

This application showcases 6 different agentic design patterns from LangChain4j:

### 1. **Sequential Pattern** 
Multi-step workflow where agents execute in sequence
- **Example**: Novel Creator (Writer → Editor → Scorer)
- **Use Case**: Content creation pipelines

### 2. **Parallel Pattern**
Multiple agents execute simultaneously and results are aggregated
- **Example**: CV Reviewer (HR + Technical + Manager reviews in parallel)
- **Use Case**: Multi-perspective evaluations

### 3. **Conditional Pattern**
Route requests to different agents based on conditions
- **Example**: Expert Router (Medical/Legal/Technical routing)
- **Use Case**: Domain-specific query handling

### 4. **Supervised Pattern**
Supervisor agent coordinates and delegates to worker agents
- **Example**: Banking Agent (coordinates Credit/Exchange/Withdraw agents)
- **Use Case**: Complex operations requiring multiple specialized tools

### 5. **Planner Pattern**
Agent creates execution plan and orchestrates multiple steps
- **Example**: Horoscope Writer (extract person → get sign → find stories → generate)
- **Use Case**: Multi-step workflows with dynamic planning

### 6. **Human-in-the-Loop Pattern**
Agent proposes actions and waits for human approval
- **Example**: Meeting Proposer (suggests → human approves/rejects → iterate)
- **Use Case**: Decision-making requiring human judgment

---

## 🗄️ Database

### H2 Console (Development)
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:langchain4jdb`
- **Username**: `sa`
- **Password**: `password`

### Flyway Migrations
- Automatic database schema management
- Located in `src/main/resources/db/migration/`
- **V1__init.sql**: Initial schema (bookings, customers, chat_messages)
- **V2__add_indexes.sql**: Performance indexes
- **V3__add_sample_data.sql**: Sample booking data for testing

## 📊 Observability & Monitoring

### Access Points

| Service | URL | Purpose |
|---------|-----|---------|
| **Jaeger UI** | http://localhost:16686 | Distributed tracing |
| **Prometheus** | http://localhost:9090 | Metrics collection |
| **Grafana** | http://localhost:3000 | Dashboards (admin/admin) |
| **Health** | http://localhost:8080/actuator/health | Service health |
| **Metrics** | http://localhost:8080/actuator/metrics | All metrics |
| **Prometheus Format** | http://localhost:8080/actuator/prometheus | For scraping |

### Custom Metrics

| Metric | Description |
|--------|-------------|
| `agent.requests.total` | Total AI agent requests |
| `agent.requests.success` | Successful requests |
| `agent.requests.failed` | Failed requests |
| `agent.response.time` | Response time distribution |
| `agent.response.time.by.type` | Response time by agent type (support/router/rag/novel/interview/help/bank/write/human-in-loop) |
| `agent.tokens.input` | LLM input tokens used |
| `agent.tokens.output` | LLM output tokens generated |
| `agent.tool.invocations` | Tool execution count |
| `rag.documents.retrieved` | RAG documents retrieved |

### Health Checks

Custom health indicators for:
- ✅ **Ollama** - LLM service status + model availability
- ✅ **Qdrant** - Vector store status + collection exists
- ✅ **Database** - H2 connectivity
- ✅ **Disk Space** - Available storage
- ✅ **Liveness/Readiness** - Kubernetes probes

### Distributed Tracing

Every request includes:
- **Trace ID** - Unique identifier for complete request flow
- **Span ID** - Identifier for specific operations
- **Request ID** - Custom correlation ID

View in Jaeger to analyze:
- Complete request flow visualization
- Service dependencies
- Performance bottlenecks
- Error propagation

## 🧪 Testing

### Run Tests

```bash
# All tests
mvn test

# Specific test
mvn test -Dtest=BookingServiceTest

# With coverage
mvn clean test jacoco:report
```

## 📁 Project Structure

```
langchain4j-spring/
├── src/main/java/com/cardconnect/langchain4j_spring/
│   ├── agentic/agents/              # Agentic pattern implementations
│   │   ├── conditional/             # Conditional routing patterns
│   │   ├── humantntheloop/          # Human-in-the-loop patterns
│   │   ├── parellel/                # Parallel execution patterns
│   │   ├── planner/                 # Multi-step planner patterns
│   │   ├── sequentional/            # Sequential workflow patterns
│   │   └── supervised/              # Supervised agent patterns
│   ├── assistant/                   # Classical AI agents
│   │   ├── CustomerSupportAgent.java
│   │   ├── RAGAgent.java
│   │   ├── RouterAgent.java
│   │   ├── MedicalExpert.java
│   │   ├── LegalExpert.java
│   │   └── TechnicalExpert.java
│   ├── config/                      # Spring configuration
│   │   ├── AgentConfiguration.java
│   │   ├── QdrantConfig.java
│   │   ├── ObservabilityConfiguration.java
│   │   └── LangChain4jProperties.java
│   ├── controller/                  # REST API endpoints
│   │   └── AgentController.java
│   ├── dto/                         # Data Transfer Objects
│   ├── entity/                      # JPA entities
│   ├── exception/                   # Exception handling
│   ├── memory/                      # Chat memory implementation
│   │   └── PersistentChatMemoryStore.java
│   ├── observability/               # Monitoring & metrics
│   │   ├── AgentMetricsService.java
│   │   ├── TracingInterceptor.java
│   │   ├── OllamaHealthIndicator.java
│   │   └── QdrantHealthIndicator.java
│   ├── repository/                  # Data repositories
│   ├── service/                     # Business logic
│   │   └── BookingService.java
│   ├── tools/                       # Agent tools
│   │   ├── BookingTools.java
│   │   ├── BankTool.java
│   │   └── ExchangeTool.java
│   └── util/                        # Utility classes
├── src/main/resources/
│   ├── application.properties       # Application configuration
│   ├── db/migration/                # Flyway database migrations
│   ├── static/                      # Static files for RAG
│   │   ├── story-about-happy-carrot.txt
│   │   ├── tailored_cv.txt
│   │   ├── job_description_backend.txt
│   │   └── hr_requirements.txt
│   └── templates/
│       └── system-prompt.st
├── docker-compose.yml               # Observability stack
├── prometheus.yml                   # Prometheus configuration
└── pom.xml                          # Maven dependencies
```

## 📚 External Resources

### Official Documentation
- [LangChain4j Documentation](https://docs.langchain4j.dev/) - Main framework docs
- [LangChain4j Agentic Patterns](https://docs.langchain4j.dev/tutorials/agentic-patterns) - Agentic design patterns
- [LangChain4j Spring Boot](https://docs.langchain4j.dev/integrations/spring-boot) - Spring integration
- [Spring Boot 3.5 Documentation](https://docs.spring.io/spring-boot/docs/3.5.x/reference/html/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

### AI & LLM Resources
- [Ollama Documentation](https://ollama.ai/docs) - Local LLM runtime
- [Ollama Models](https://ollama.ai/library) - Available models
- [Qdrant Documentation](https://qdrant.tech/documentation/) - Vector database

### Observability & Monitoring
- [Jaeger Documentation](https://www.jaegertracing.io/docs/) - Distributed tracing
- [OpenTelemetry](https://opentelemetry.io/docs/) - Observability framework
- [Prometheus Documentation](https://prometheus.io/docs/) - Metrics & monitoring
- [Grafana Documentation](https://grafana.com/docs/) - Visualization
- [Micrometer](https://micrometer.io/docs) - Observability facade

### Additional Tools
- [Flyway](https://flywaydb.org/documentation/) - Database migrations
- [H2 Database](https://www.h2database.com/html/main.html) - In-memory database

## 🎯 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     Client Requests                             │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│              TracingInterceptor                                 │
│   - Generates Trace/Span/Request IDs                            │
│   - OpenTelemetry Integration                                   │
└────────────────────────┬────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│           AgentController (@Observed)                           │
│   - Validates Requests                                          │
│   - Records Custom Metrics                                      │
│   - 9 Endpoint Handlers                                         │
└────────────────────────┬────────────────────────────────────────┘
                         │
           ┌─────────────┴─────────────┐
           │                           │
           ▼                           ▼
┌──────────────────────┐    ┌──────────────────────┐
│  Classical Agents    │    │  Agentic Patterns    │
│  - CustomerSupport   │    │  - Sequential        │
│  - RouterAgent       │    │  - Parallel          │
│  - RAGAgent          │    │  - Conditional       │
│                      │    │  - Supervised        │
│                      │    │  - Planner           │
│                      │    │  - Human-in-Loop     │
└──────────┬───────────┘    └──────────┬───────────┘
           │                           │
           └─────────────┬─────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Tools & Services                             │
│   - BookingTools (getBooking, cancelBooking)                    │
│   - BankTool (credit, withdraw)                                 │
│   - ExchangeTool (currency exchange)                            │
│   - PersistentChatMemoryStore                                   │
└────────────────────────┬────────────────────────────────────────┘
                         │
           ┌─────────────┴─────────────┐
           │                           │
           ▼                           ▼
┌──────────────────────┐    ┌──────────────────────┐
│  External Services   │    │  Data Stores         │
│  - Ollama (LLM)      │    │  - H2 Database       │
│  - Qdrant (Vectors)  │    │  - Flyway Migrations │
└──────────────────────┘    └──────────────────────┘
           │                           │
           └─────────────┬─────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────┐
│              Observability Stack                                │
│   - Jaeger (Distributed Tracing)                                │
│   - Prometheus (Metrics)                                        │
│   - Grafana (Dashboards)                                        │
│   - Micrometer (Observability API)                              │
└─────────────────────────────────────────────────────────────────┘
```

## 🎓 Technology Stack

### Core Framework
- **Java**: 21 (Latest LTS)
- **Spring Boot**: 3.5.7
- **Maven**: Build & dependency management

### AI & LangChain4j
- **LangChain4j**: 1.9.1
  - `langchain4j-spring-boot-starter`
  - `langchain4j-ollama-spring-boot-starter`
  - `langchain4j-easy-rag` - RAG capabilities
  - `langchain4j-qdrant` - Vector store integration
  - `langchain4j-agentic` - Core agentic framework
  - `langchain4j-agentic-patterns` - Pre-built agentic patterns

### LLM & Embeddings
- **Ollama**: Local LLM runtime
  - Chat Model: `qwen3:0.6b` (lightweight, fast)
  - Embedding Model: `nomic-embed-text:latest`
- **Qdrant**: Vector database for embeddings (via Docker)

### Observability Stack
- **Jaeger**: Distributed tracing with OpenTelemetry
- **OpenTelemetry**: Instrumentation & telemetry
- **Prometheus**: Metrics collection & storage
- **Grafana**: Visualization & dashboards
- **Micrometer**: Observability facade API

### Database & Persistence
- **H2**: In-memory database (development)
- **Flyway**: Database schema migrations
- **HikariCP**: High-performance connection pooling
- **Spring Data JPA**: ORM & repository abstraction

### Additional Dependencies
- **Lombok**: Reduce boilerplate code
- **Spring Boot Actuator**: Production-ready features
- **Spring Boot Validation**: Request validation
- **Spring Boot AOP**: Aspect-oriented programming for @Observed


## 🚀 Quick Commands Reference

```bash
# ============================================================
# Development
# ============================================================
mvn spring-boot:run                          # Start application
mvn test                                     # Run tests
mvn clean install                            # Build project
mvn clean package                            # Package JAR

# ============================================================
# Docker Services
# ============================================================
docker-compose up -d                         # Start all observability services
docker-compose down                          # Stop all services
docker ps                                    # Check running containers
docker logs -f jaeger                        # View Jaeger logs
docker logs -f prometheus                    # View Prometheus logs

# Start Qdrant vector store
docker run -d -p 6333:6333 -p 6334:6334 \
  -v $(pwd)/qdrant_storage:/qdrant/storage \
  qdrant/qdrant

# ============================================================
# Ollama
# ============================================================
ollama pull qwen3:0.6b                      # Pull chat model
ollama pull nomic-embed-text:latest         # Pull embedding model
ollama list                                  # List installed models
ollama serve                                 # Start Ollama service
curl http://localhost:11434/api/tags        # Verify Ollama

# ============================================================
# Testing Endpoints (Examples)
# ============================================================
# Health checks
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/ollama
curl http://localhost:8080/actuator/health/qdrant

# Customer Support (with memory)
curl -X POST http://localhost:8080/api/v1/agent/support \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"user123","message":"Get booking BN123"}'

# Router Agent (expert selection)
curl -X POST http://localhost:8080/api/v1/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"user456","message":"What is diabetes?"}'

# RAG Agent (document query)
curl -X POST "http://localhost:8080/api/v1/agent/chat?message=Tell%20me%20about%20Charlie"

# Sequential Novel Creator
curl -X POST "http://localhost:8080/api/v1/agent/script?topic=space%20adventure&style=sci-fi"

# Parallel CV Reviewer
curl -X POST http://localhost:8080/api/v1/agent/interview

# Banking Supervisor Agent
curl -X POST http://localhost:8080/api/v1/agent/bank \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"bank001","message":"Exchange 1000 USD to EUR"}'

# ============================================================
# Monitoring & Observability
# ============================================================
# Access UIs
open http://localhost:16686                 # Jaeger Tracing UI
open http://localhost:9090                  # Prometheus
open http://localhost:3000                  # Grafana (admin/admin)
open http://localhost:8080/h2-console      # H2 Database Console

# View metrics
curl http://localhost:8080/actuator/metrics
curl http://localhost:8080/actuator/prometheus

# ============================================================
# Qdrant Vector Store
# ============================================================
curl http://localhost:6333/collections      # List collections
curl http://localhost:6333/collections/story # View story collection details
```

## 🔧 Troubleshooting

### Common Issues

#### 1. Ollama Connection Error
```
Error: Connection refused to localhost:11434
```
**Solution:**
```bash
# Check if Ollama is running
ps aux | grep ollama

# Start Ollama service
ollama serve

# Verify connection
curl http://localhost:11434/api/tags
```

#### 2. Qdrant Vector Store Error
```
Error: Failed to connect to Qdrant at localhost:6334
```
**Solution:**
```bash
# Start Qdrant with Docker
docker run -d -p 6333:6333 -p 6334:6334 \
  -v $(pwd)/qdrant_storage:/qdrant/storage \
  qdrant/qdrant

# Verify connection
curl http://localhost:6333/collections
```

#### 3. Model Not Found Error
```
Error: Model 'qwen3:0.6b' not found
```
**Solution:**
```bash
# Pull the required models
ollama pull qwen3:0.6b
ollama pull nomic-embed-text:latest

# Verify models are installed
ollama list
```

#### 4. H2 Console Access Issues
**Solution:**
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:langchain4jdb`
- Username: `sa`
- Password: `password`

#### 5. Jaeger Not Receiving Traces
**Solution:**
```bash
# Verify Jaeger is running
docker ps | grep jaeger

# Check OTLP endpoint configuration in application.properties
# management.otlp.tracing.endpoint=http://localhost:4318/v1/traces

# Restart application
mvn spring-boot:run
```

#### 6. Port Already in Use
```
Error: Port 8080 already in use
```
**Solution:**
```bash
# Find process using port 8080
lsof -i :8080

# Kill the process (replace PID)
kill -9 <PID>

# Or change port in application.properties
server.port=8081
```

## ❓ FAQ

### Q: Which Ollama models are supported?
**A:** Any Ollama chat model works, but lightweight models like `qwen3:0.6b`, `llama3.2:1b`, or `phi3:mini` are recommended for development. For production, consider `llama3.1:8b` or `mistral:7b`.

### Q: Can I use a different vector database instead of Qdrant?
**A:** Yes! LangChain4j supports multiple vector stores (Pinecone, Weaviate, Chroma, etc.). Update the dependency in `pom.xml` and configuration in `QdrantConfig.java`.

### Q: How do I deploy this to production?
**A:** 
1. Replace H2 with PostgreSQL/MySQL
2. Configure production Qdrant cluster
3. Use managed Ollama or cloud LLM (OpenAI, Azure, AWS Bedrock)
4. Set up proper secrets management
5. Configure production observability endpoints
6. Enable HTTPS and authentication

### Q: How do I add a new agent?
**A:**
1. Create agent interface in `assistant/` package
2. Define system prompt and tools
3. Configure bean in `AgentConfiguration.java`
4. Add controller endpoint in `AgentController.java`
5. Implement metrics tracking

### Q: What's the difference between classical agents and agentic patterns?
**A:** 
- **Classical agents**: Traditional AI assistants (CustomerSupport, RAG, Router)
- **Agentic patterns**: Advanced orchestration patterns (Sequential, Parallel, Supervised, etc.) from LangChain4j's agentic framework

### Q: How do I customize the chat memory size?
**A:** Update `application.properties`:
```properties
app.langchain4j.chat-memory.max-messages=20  # Default is 10
```

### Q: Can I use OpenAI instead of Ollama?
**A:** Yes! Replace the Ollama dependency with OpenAI:
```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai-spring-boot-starter</artifactId>
</dependency>
```
Update configuration:
```properties
langchain4j.open-ai.chat-model.api-key=your-api-key
langchain4j.open-ai.chat-model.model-name=gpt-4
```

## 📝 License

This is a demonstration project for educational purposes.

## 👨‍💻 Author

**Pankaj Jaiswal** 

## 🙏 Acknowledgments

- [LangChain4j](https://github.com/langchain4j/langchain4j) - Excellent Java AI framework
- [Ollama](https://ollama.ai/) - Easy local LLM deployment
- [Qdrant](https://qdrant.tech/) - Powerful vector search engine
- [Spring Boot](https://spring.io/projects/spring-boot) - Best-in-class Java framework

---

**Built with ❤️ using LangChain4j, Spring Boot, and AI**


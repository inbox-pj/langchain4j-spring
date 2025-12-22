# LangChain4j Spring Boot Application

A professional-grade Spring Boot application demonstrating AI-powered customer support using LangChain4j with multiple specialized agents, RAG (Retrieval-Augmented Generation) capabilities, persistent chat memory, and enterprise observability.

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

### 3. Start Observability Stack

```bash
# Start Jaeger, Prometheus, and Grafana
docker-compose -f docker-compose.yml up -d
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

All endpoints use modern RESTful design with JSON request/response.

### Customer Support Agent

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
  "timestamp": "2025-12-22T10:30:00"
}
```

**Use Cases**:
- Get booking details
- Cancel bookings
- Booking inquiries
- Customer support

### Router Agent (Automatic Expert Selection)

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
  "timestamp": "2025-12-22T10:30:00"
}
```

**Available Experts**:
- **Medical** - Health, symptoms, treatments, medications
- **Legal** - Laws, regulations, contracts, rights
- **Technical** - Technology, software, hardware, programming

### RAG Agent (Document Query)

**Endpoint**: `POST /api/v1/agent/chat?message={your_question}`

**Request**:
```bash
curl -X POST "http://localhost:8080/api/v1/agent/chat?message=Tell%20me%20about%20Charlie"
```

**Response**:
```json
{
  "response": "Charlie is a happy carrot who lives in VeggieVille...",
  "timestamp": "2025-12-22T10:30:00"
}
```

**Features**:
- Semantic search over embedded documents
- Context-aware responses
- Source-attributed answers
- Only answers from document content

## 🗄️ Database

### H2 Console (Development)
- **URL**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:langchain4jdb`
- **Username**: `sa`
- **Password**: `password`

### Flyway Migrations
- Automatic database schema management
- Located in `src/main/resources/db/migration/`
- V1: Initial schema
- V2: Sample data

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
| `agent.response.time.by.type` | Response time by agent (support/router/rag) |
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

### External Resources
- [LangChain4j Documentation](https://docs.langchain4j.dev/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Ollama Documentation](https://ollama.ai/docs)
- [Qdrant Documentation](https://qdrant.tech/documentation/)
- [Jaeger Documentation](https://www.jaegertracing.io/docs/)

## 🎯 Architecture

```
┌─────────────────────────────────────────┐
│         Client Request                  │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│     TracingInterceptor                  │
│  - Generates Trace/Span IDs             │
│  - Adds Request ID                      │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│     AgentController (@Observed)         │
│  - Validates Request                    │
│  - Records Metrics                      │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│     AI Agents (Router/Support/RAG)      │
│  - Process with LLM                     │
│  - Use Tools (BookingTools)             │
│  - Access Memory                        │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│     Services & Repositories             │
│  - BookingService                       │
│  - ChatMemoryStore                      │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│     External Services                   │
│  - Ollama (LLM)                         │
│  - Qdrant (Vector DB)                   │
│  - H2 Database                          │
└─────────────────────────────────────────┘
```

## 🎓 Technology Stack

### Core
- **Java**: 21
- **Spring Boot**: 3.5.7
- **LangChain4j**: 1.9.1
- **Maven**: Build tool

### AI & Vector Store
- **Ollama**: Local LLM (qwen3:0.6b)
- **Qdrant**: Vector database
- **Embeddings**: nomic-embed-text

### Observability
- **Jaeger**: Distributed tracing
- **OpenTelemetry**: Instrumentation
- **Prometheus**: Metrics
- **Grafana**: Dashboards
- **Micrometer**: Observability API

### Database
- **H2**: In-memory (development)
- **Flyway**: Schema migrations
- **HikariCP**: Connection pooling


## 🚀 Quick Commands Reference

```bash
# Development
mvn spring-boot:run                          # Start application
mvn test                                     # Run tests
mvn clean install                            # Build project

# Docker
docker-compose -f docker-compose-observability.yml up -d   # Start monitoring
docker ps                                    # Check running containers

# Ollama
ollama pull qwen3:0.6b                      # Pull LLM model
ollama list                                  # List models

# Testing
curl http://localhost:8080/actuator/health  # Health check
curl http://localhost:16686                 # Jaeger UI
```

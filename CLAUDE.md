# CLAUDE.md - AI Assistant Guide for AUM-SpringAI

## Project Overview

**AUM-SpringAI** is a University AI-powered FAQ system with a RAG (Retrieval-Augmented Generation) architecture. The system helps students and staff get answers to university-related questions using AI, with features for document indexing, knowledge retrieval, and intelligent chat assistance.

**Project Type**: Full-stack monorepo
- **Backend**: Spring Boot 3.5.6 + Spring AI (Java 25)
- **Frontend**: Angular 20 (TypeScript)

**Primary Purpose**: University FAQ chatbot with RAG capabilities, confidence scoring, and session management.

---

## Repository Structure

```
AUM-SpringAI/
├── Backend/                          # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/bsanju/aum/
│   │   │   │   ├── config/          # Spring configuration classes
│   │   │   │   ├── controller/      # REST API controllers
│   │   │   │   ├── event/           # Application events
│   │   │   │   ├── exception/       # Custom exceptions & handlers
│   │   │   │   ├── model/
│   │   │   │   │   ├── dto/        # Data Transfer Objects
│   │   │   │   │   └── entity/     # JPA entities
│   │   │   │   ├── repository/      # Spring Data JPA repositories
│   │   │   │   ├── scheduler/       # Scheduled tasks
│   │   │   │   ├── service/         # Business logic
│   │   │   │   └── util/            # Utility classes
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application.properties
│   │   │       ├── application-dev.yml
│   │   │       └── application-prod.yml
│   │   └── test/
│   │       └── java/com/bsanju/aum/
│   │           ├── controller/      # Controller tests
│   │           ├── service/         # Service unit tests
│   │           ├── repository/      # Repository tests
│   │           ├── integration/     # Integration tests
│   │           └── util/            # Test utilities
│   ├── build.gradle                 # Gradle build configuration
│   ├── settings.gradle
│   └── gradlew                      # Gradle wrapper
│
├── Frontend/                         # Angular application
│   └── university-ai-faq/
│       ├── src/
│       │   ├── app/                 # Angular application
│       │   │   ├── app.ts
│       │   │   ├── app.config.ts
│       │   │   ├── app.routes.ts
│       │   │   └── app.html
│       │   └── main.ts
│       ├── public/                  # Static assets
│       ├── angular.json
│       ├── package.json
│       └── tsconfig.json
│
└── README.md
```

---

## Technology Stack

### Backend Stack
- **Framework**: Spring Boot 3.5.6
- **Language**: Java 25
- **AI Integration**: Spring AI 1.0.3
  - OpenAI (GPT-3.5-turbo)
  - Ollama (llama2) - local AI option
- **Database**: Spring Data JPA (persistence layer)
- **Build Tool**: Gradle 8.x
- **Key Features**:
  - Caching (`@EnableCaching`)
  - Async processing (`@EnableAsync`)
  - Scheduled tasks (`@EnableScheduling`)
  - JPA repositories (`@EnableJpaRepositories`)
  - Actuator for monitoring
  - Micrometer metrics (Dynatrace)

### Frontend Stack
- **Framework**: Angular 20.3.0
- **Language**: TypeScript 5.9.2
- **Build Tool**: Angular CLI 20.3.6
- **Testing**: Jasmine + Karma
- **Code Style**: Prettier (configured)

---

## Architecture Patterns

### Backend Architecture

#### 1. **Layered Architecture**
```
Controller Layer → Service Layer → Repository Layer → Database
                ↓
           DTO Mapping
```

#### 2. **Key Design Patterns**

**Dependency Injection (Constructor-based)**
```java
@Service
public class ChatService {
    private final ChatClient chatClient;
    private final KnowledgeRetrievalService retrievalService;

    public ChatService(ChatClient chatClient,
                      KnowledgeRetrievalService retrievalService) {
        this.chatClient = chatClient;
        this.retrievalService = retrievalService;
    }
}
```

**Builder Pattern** (for DTOs)
```java
ChatResponse.builder()
    .response(response)
    .sessionId(sessionId)
    .confidence(confidence)
    .build();
```

**Repository Pattern** (Spring Data JPA)
```java
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findBySessionIdOrderByTimestampDesc(String sessionId);
}
```

**Event-Driven Architecture**
- `ChatCompletionEvent`
- `DocumentUpdatedEvent`
- `FeedbackReceivedEvent`

#### 3. **RAG (Retrieval-Augmented Generation) Flow**

```
User Query → Knowledge Retrieval → Context Injection → LLM Call → Response
     ↓              ↓                    ↓                ↓           ↓
  Category    Vector Search        Prompt Template    OpenAI/    Confidence
  Detection      (top-5)            Construction      Ollama     Calculation
```

**Implementation** (see `ChatService.java:49-125`):
1. Retrieve relevant documents (top 5 matches)
2. Build system prompt with context
3. Call LLM with user message
4. Calculate confidence score
5. Determine if human assistance needed
6. Save session and metrics

#### 4. **Configuration Strategy**

**Conditional Bean Loading**:
```java
@Bean
@Primary
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai", matchIfMissing = true)
public ChatClient openAiChatClient(OpenAiChatModel chatModel) {
    return ChatClient.builder(chatModel)
            .defaultOptions(options -> options
                    .withModel("gpt-3.5-turbo")
                    .withTemperature(0.7)
                    .withMaxTokens(1000))
            .build();
}
```

**Environment Profiles**:
- `application.yml` - Base configuration
- `application-dev.yml` - Development overrides
- `application-prod.yml` - Production overrides

---

## Code Conventions

### Backend Conventions

#### 1. **Package Organization**
- `config/` - All `@Configuration` classes
- `controller/` - REST endpoints (`@RestController`)
- `service/` - Business logic (`@Service`)
- `repository/` - Data access (`@Repository` / JPA interfaces)
- `model/entity/` - JPA entities (`@Entity`)
- `model/dto/` - Data transfer objects (records preferred)
- `exception/` - Custom exceptions
- `util/` - Helper classes

#### 2. **Naming Conventions**
- **Entities**: Singular nouns (e.g., `ChatSession`, `UniversityDocument`)
- **DTOs**: Suffix with `Dto` or `Request`/`Response` (e.g., `ChatResponse`, `FeedbackRequest`)
- **Services**: Suffix with `Service` (e.g., `ChatService`, `MetricsService`)
- **Repositories**: Suffix with `Repository` (e.g., `ChatSessionRepository`)
- **Controllers**: Suffix with `Controller` (e.g., `ChatController`, `AdminController`)

#### 3. **Java Code Style**
- **Constructor Injection**: Always prefer constructor injection over field injection
- **Records for DTOs**: Use Java records for immutable DTOs
- **Lombok Alternative**: Use builders manually or records (no Lombok in current codebase)
- **Logging**: Use SLF4J logger
  ```java
  private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
  ```

#### 4. **Transaction Management**
- Use `@Transactional` at service level
- Example: `ChatService.java:22`

#### 5. **Error Handling**
- Custom exceptions in `exception/` package
- Global exception handler: `GlobalExceptionHandler`
- Graceful fallbacks (see `ChatService.java:107-124`)

#### 6. **Caching Strategy**
```java
@Cacheable(value = "faq-responses", key = "#request.message().toLowerCase()")
public ChatResponse getCachedResponse(ChatRequest request) {
    return processQuery(request);
}
```

### Frontend Conventions

#### 1. **File Structure**
- Components: `*.ts`, `*.html`, `*.scss`, `*.spec.ts`
- Standalone components (Angular 20+ pattern)

#### 2. **Code Style**
- **Prettier** configured:
  - Print width: 100
  - Single quotes: true
  - Angular parser for HTML

#### 3. **TypeScript**
- Strict mode enabled
- Target: ES2022

---

## Database Schema

### Core Entities

#### ChatSession
```java
@Entity
@Table(name = "chat_sessions")
public class ChatSession {
    @Id @GeneratedValue
    private Long id;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Lob @Column(name = "user_message", nullable = false)
    private String userMessage;

    @Lob @Column(name = "ai_response", nullable = false)
    private String aiResponse;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "confidence")
    private double confidence;

    @Column(name = "needs_human_assistance")
    private boolean needsHumanAssistance;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    // Additional metadata: userIp, userAgent, responseTimeMs
}
```

**Other Entities**:
- `UniversityDocument` - Knowledge base documents
- `QueryMetrics` - Analytics and metrics
- `UserFeedback` - User feedback on responses

---

## Development Workflows

### Backend Development

#### 1. **Build & Run**
```bash
cd Backend

# Run application
./gradlew bootRun

# Run tests
./gradlew test

# Build JAR
./gradlew build

# Clean build
./gradlew clean build
```

#### 2. **Environment Configuration**
Set environment variables or configure `application-dev.yml`:
```yaml
app:
  ai:
    provider: openai  # or "ollama" for local

spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
    ollama:
      base-url: http://localhost:11434
```

#### 3. **Running Tests**
```bash
# Unit tests
./gradlew test --tests ChatServiceTest

# Integration tests
./gradlew test --tests ChatIntegrationTest

# All tests
./gradlew test
```

### Frontend Development

#### 1. **Setup & Run**
```bash
cd Frontend/university-ai-faq

# Install dependencies
npm install

# Development server
npm start
# or
ng serve

# Build for production
npm run build

# Run tests
npm test
```

#### 2. **Code Formatting**
```bash
# Prettier is configured in package.json
npx prettier --write "src/**/*.{ts,html,scss}"
```

---

## Testing Strategy

### Backend Testing

#### 1. **Test Structure**
```
test/java/com/bsanju/aum/
├── controller/          # Controller layer tests
│   ├── ChatControllerTest.java
│   └── AdminControllerTest.java
├── service/            # Service layer unit tests
│   ├── ChatServiceTest.java
│   ├── DocumentIndexingServiceTest.java
│   └── KnowledgeRetrievalServiceTest.java
├── repository/         # Repository tests
│   ├── ChatSessionRepositoryTest.java
│   └── DocumentRepositoryTest.java
├── integration/        # Integration tests
│   ├── ChatIntegrationTest.java
│   ├── DocumentIndexingIntegrationTest.java
│   └── VectorStoreIntegrationTest.java
└── util/              # Test utilities
    ├── TestDataBuilder.java
    ├── TestContainerConfig.java
    └── MockChatClient.java
```

#### 2. **Testing Patterns**
- **Unit Tests**: Mock dependencies, test business logic
- **Integration Tests**: Use TestContainers or in-memory databases
- **Test Data Builders**: Use `TestDataBuilder` for creating test fixtures
- **Mock AI Client**: Use `MockChatClient` for testing without actual LLM calls

#### 3. **Test Naming Convention**
```java
@Test
public void processQuery_withValidRequest_shouldReturnResponse() {
    // Arrange, Act, Assert
}
```

---

## Key Components Deep Dive

### 1. ChatService (`service/ChatService.java`)

**Responsibilities**:
- Process user queries with RAG
- Calculate confidence scores
- Manage chat sessions
- Record metrics
- Generate contextual suggestions

**Key Methods**:
- `processQuery(ChatRequest)` - Main query processing
- `getCachedResponse(ChatRequest)` - Cached query handling
- `getChatHistory(sessionId, limit)` - Retrieve session history
- `getSuggestions(category)` - Get category-specific suggestions

**Categories**:
- `TUITION` - Tuition and fees
- `COURSES` - Course registration and information
- `DEADLINES` - Important dates
- `POLICIES` - Academic policies
- `TECHNICAL` - Technical support

### 2. KnowledgeRetrievalService (`service/KnowledgeRetrievalService.java`)

**Responsibilities**:
- Vector similarity search
- Document retrieval from knowledge base
- Semantic matching

### 3. DocumentIndexingService (`service/DocumentIndexingService.java`)

**Responsibilities**:
- Index university documents
- Update vector store
- Document preprocessing

### 4. PromptTemplates (`util/PromptTemplates.java`)

**Responsibilities**:
- Build system prompts
- Inject context into prompts
- Template management

### 5. ConfidenceCalculator (`util/ConfidenceCalculator.java`)

**Responsibilities**:
- Calculate response confidence
- Determine if human assistance needed
- Quality scoring

---

## API Endpoints

### Controllers

#### ChatController
- `POST /api/chat` - Send chat message
- `GET /api/chat/history/{sessionId}` - Get chat history
- `GET /api/chat/suggestions/{category}` - Get suggestions

#### AdminController
- `POST /api/admin/documents` - Upload documents
- `GET /api/admin/metrics` - Get system metrics
- `POST /api/admin/reindex` - Reindex documents

#### HealthController
- `GET /health` or `/actuator/health` - Health check

---

## Important Notes for AI Assistants

### 1. **When Adding New Features**

✅ **DO**:
- Follow the existing layered architecture
- Add appropriate tests (unit + integration)
- Use constructor-based dependency injection
- Add logging at appropriate levels
- Handle exceptions gracefully
- Update this CLAUDE.md if adding significant patterns

❌ **DON'T**:
- Mix business logic into controllers
- Use field injection (`@Autowired` on fields)
- Hardcode configuration values
- Skip error handling
- Forget to add tests

### 2. **When Modifying ChatService**

- **CRITICAL**: Always test with both OpenAI and Ollama providers
- Maintain backward compatibility with existing sessions
- Update confidence calculation if changing response format
- Test category-specific suggestions
- Verify caching behavior

### 3. **When Working with Database**

- Use JPA entity naming conventions
- Add appropriate indexes for query performance
- Use `@Transactional` for multi-step operations
- Consider migration scripts for schema changes
- Test repository methods independently

### 4. **When Adding Dependencies**

**Backend** (`build.gradle`):
```gradle
dependencies {
    implementation 'group:artifact:version'
    testImplementation 'group:artifact:version'
}
```

**Frontend** (`package.json`):
```bash
npm install <package-name>
```

### 5. **Configuration Management**

- **Never commit secrets** to git
- Use environment variables for sensitive data
- Document new configuration properties
- Add defaults for development
- Use profiles for environment-specific config

### 6. **When Creating REST Endpoints**

```java
@RestController
@RequestMapping("/api/resource")
public class ResourceController {

    private final ResourceService service;

    public ResourceController(ResourceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ResourceDto> create(@RequestBody ResourceRequest request) {
        ResourceDto result = service.create(request);
        return ResponseEntity.ok(result);
    }
}
```

### 7. **Logging Levels**

- `logger.error()` - Exceptions, critical failures
- `logger.warn()` - Recoverable issues, degraded performance
- `logger.info()` - Important business events (query processed, document indexed)
- `logger.debug()` - Detailed diagnostic info (found N documents, cache hit)

### 8. **Performance Considerations**

- Use caching for frequently accessed data
- Limit vector search results (currently top-5)
- Implement pagination for large result sets
- Monitor LLM response times
- Use async processing for heavy operations

### 9. **Security Considerations**

- **CORS**: Configured in `CorsConfig`
- **Security**: Configured in `SecurityConfig`
- **Input Validation**: Always validate user inputs
- **SQL Injection**: Use JPA parameterized queries
- **Rate Limiting**: Implement for public endpoints

### 10. **Git Workflow**

```bash
# Current branch (from context)
git branch  # claude/claude-md-mi3lhgoatuhrj0r5-013ZM6G4YAWj1x9yrBuBKKRc

# Commit changes
git add .
git commit -m "feat: add new feature"

# Push to remote
git push -u origin <branch-name>
```

**Commit Message Convention**:
- `feat:` - New feature
- `fix:` - Bug fix
- `refactor:` - Code refactoring
- `test:` - Adding tests
- `docs:` - Documentation changes
- `chore:` - Maintenance tasks

---

## Common Tasks

### Adding a New Service

1. Create service class in `service/` package
2. Annotate with `@Service`
3. Use constructor injection for dependencies
4. Add logging
5. Create unit tests
6. Add integration tests if needed

### Adding a New Entity

1. Create entity in `model/entity/`
2. Add JPA annotations
3. Create repository interface
4. Add migration script (if using Flyway/Liquibase)
5. Create repository test

### Adding a New REST Endpoint

1. Add method to appropriate controller
2. Create DTO for request/response
3. Implement service method
4. Add validation
5. Add controller test
6. Update API documentation

### Modifying AI Configuration

1. Update `SpringAiConfig.java`
2. Add/modify properties in `application.yml`
3. Test with both providers (OpenAI/Ollama)
4. Update environment variable documentation

---

## Troubleshooting

### Backend Issues

**Build Failures**:
```bash
./gradlew clean build --refresh-dependencies
```

**Test Failures**:
- Check database connection
- Verify test data setup
- Check mock configurations
- Review logs in `build/test-results/`

**Runtime Issues**:
- Check `application.yml` configuration
- Verify environment variables
- Check actuator health endpoint
- Review application logs

### Frontend Issues

**Build Failures**:
```bash
rm -rf node_modules package-lock.json
npm install
```

**Development Server Issues**:
- Check port 4200 availability
- Verify Angular CLI version
- Check TypeScript errors

---

## Resources

### Spring AI Documentation
- Spring AI Reference: https://docs.spring.io/spring-ai/reference/
- OpenAI Integration: https://docs.spring.io/spring-ai/reference/api/clients/openai.html
- Ollama Integration: https://docs.spring.io/spring-ai/reference/api/clients/ollama.html

### Project-Specific
- Main Application: `BackendApplication.java:1`
- Chat Processing: `ChatService.java:49`
- AI Configuration: `SpringAiConfig.java:1`

---

## Version History

- **Current Version**: 0.0.1-SNAPSHOT
- **Java Version**: 25
- **Spring Boot**: 3.5.6
- **Spring AI**: 1.0.3
- **Angular**: 20.3.0

---

## Contact & Support

For questions or issues:
1. Check this CLAUDE.md first
2. Review existing tests for examples
3. Check Spring AI documentation
4. Review commit history for context

---

**Last Updated**: 2025-11-17
**Maintained By**: AI Assistant (Claude)
**Repository**: AUM-SpringAI

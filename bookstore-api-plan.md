# Bookstore API — Implementation Plan

## Top-Level Overview

Build a Spring Boot 3.x REST API for a bookstore e-commerce backend.  
The application manages a single `Book` domain entity with full CRUD operations exposed under `/api/v1/books`.  
For local development and testing, an H2 in-memory database is used — zero-config, auto-reset on restart, and compatible with JUnit-based integration tests.  
The solution is layered (Controller → Service → Repository) with DTOs, a mapper, centralised exception handling, Bean Validation, and Springdoc OpenAPI (Swagger UI).

**Stack:** Java 21 · Spring Boot 3.x · Maven · Spring Data JPA · H2 · Springdoc OpenAPI 2.x  
**Group / Artifact:** `com.bookstore` / `bookstore-api`  
**Base path:** `/api/v1/books`

---

## Sub-Tasks

---

### Sub-Task 1 — Project Scaffolding and Maven Configuration

**Status:** `[x] done`

**Intent**  
Create the Maven project structure (`pom.xml`, directory layout, `application.properties`) with all required dependencies declared. This is the foundation every other sub-task builds on.

**Expected Outcomes**
- `pom.xml` present with correct group/artifact, Java 21, and Spring Boot 3.x parent
- Dependencies declared: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `h2`, `springdoc-openapi-starter-webmvc-ui`, `spring-boot-starter-test`
- `src/main/resources/application.properties` configured with H2 datasource, JPA DDL-auto, H2 console enabled, and server port
- Standard Maven directory layout present: `src/main/java`, `src/main/resources`, `src/test/java`
- Application boots without errors (`BookstoreApiApplication` main class)

**Todo List**
1. Create `pom.xml` with `spring-boot-starter-parent` 3.x, Java 21 compiler settings, and all required dependencies listed above
2. Create `src/main/java/com/bookstore/BookstoreApiApplication.java` with `@SpringBootApplication` main class
3. Create `src/main/resources/application.properties` — set H2 in-memory URL (`jdbc:h2:mem:bookstoredb`), H2 console at `/h2-console`, `spring.jpa.hibernate.ddl-auto=create-drop`, `spring.jpa.show-sql=true`, server port `8080`
4. Create the following empty package directories (add a `.gitkeep` or the first class in each): `controller`, `service`, `repository`, `domain`, `dto`, `mapper`, `exception`, `config`

**Relevant Context**
- All source files live under `src/main/java/com/bookstore/`
- H2 `create-drop` DDL strategy means the schema is created from JPA entities on startup and dropped on shutdown — no manual SQL scripts needed at this stage
- Springdoc OpenAPI 2.x artifact: `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.x`

---

### Sub-Task 2 — Domain Entity

**Status:** `[x] done`

**Intent**  
Define the `Book` JPA entity. This is the single source of truth for the database schema and the object the repository and service layers operate on.

**Expected Outcomes**
- `Book.java` entity class present in `com.bookstore.domain`
- Annotated with `@Entity`, `@Table(name = "books")`
- Fields: `id` (Long, auto-generated PK), `title` (String, not null), `author` (String, not null), `isbn` (String, unique, not null), `price` (BigDecimal, not null), `stockQuantity` (int, not null), `category` (String)
- Standard JPA lifecycle annotations: `@CreatedDate`, `@LastModifiedDate` (via `@EntityListeners(AuditingEntityListener.class)`) or plain `createdAt`/`updatedAt` timestamp fields
- `equals`, `hashCode` based on `id`

**Todo List**
1. Create `src/main/java/com/bookstore/domain/Book.java` with all fields and JPA annotations
2. Use `@GeneratedValue(strategy = GenerationType.IDENTITY)` for the primary key
3. Add `createdAt` and `updatedAt` fields annotated with `@CreationTimestamp` / `@UpdateTimestamp` (Hibernate) for audit traceability
4. Enable JPA auditing in a config class or via `@EnableJpaAuditing` on the main application class

**Relevant Context**
- `isbn` must carry a `@Column(unique = true)` constraint to prevent duplicate books
- `price` must be `BigDecimal` (never `double`) for monetary accuracy
- `stockQuantity` represents inventory and must be `>= 0`

---

### Sub-Task 3 — Repository Layer

**Status:** `[x] done`

**Intent**  
Create the Spring Data JPA repository interface for `Book`. Provides CRUD and query methods with zero boilerplate implementation.

**Expected Outcomes**
- `BookRepository.java` interface in `com.bookstore.repository` extending `JpaRepository<Book, Long>`
- Custom finder: `findByIsbn(String isbn)` returning `Optional<Book>`
- Custom finder: `findByCategory(String category)` returning `List<Book>`
- Custom finder: `findByAuthorContainingIgnoreCase(String author)` returning `List<Book>`

**Todo List**
1. Create `src/main/java/com/bookstore/repository/BookRepository.java` extending `JpaRepository<Book, Long>`
2. Declare the three custom query methods listed above (Spring Data derives queries from method names — no JPQL needed)

**Relevant Context**
- No `@Repository` annotation needed — Spring Data detects the interface automatically
- These finder methods will be used by the service layer for search/filter operations

---

### Sub-Task 4 — DTOs and Mapper

**Status:** `[x] done`

**Intent**  
Define request/response DTO classes that decouple the API contract from the internal JPA entity, and provide a `BookMapper` utility to convert between them. This keeps the controller and service clean and makes the API surface independently evolvable.

**Expected Outcomes**
- `BookRequest.java` in `com.bookstore.dto` — inbound payload with Bean Validation annotations
- `BookResponse.java` in `com.bookstore.dto` — outbound payload with all display fields
- `BookMapper.java` in `com.bookstore.mapper` — static or Spring-managed methods `toEntity`, `toResponse`, `toResponseList`

**Todo List**
1. Create `BookRequest.java` with fields: `title`, `author`, `isbn`, `price`, `stockQuantity`, `category`; annotate with `@NotBlank`, `@NotNull`, `@Positive`, `@DecimalMin` as appropriate
2. Create `BookResponse.java` with all `Book` entity fields plus `createdAt`/`updatedAt`; this class is read-only (no validation annotations)
3. Create `BookMapper.java` as a `@Component` with methods:
   - `toEntity(BookRequest request) → Book`
   - `toResponse(Book book) → BookResponse`
   - `toResponseList(List<Book> books) → List<BookResponse>`
4. The mapper must NOT use any third-party mapping library (e.g. MapStruct) to keep dependencies minimal — hand-written field-by-field mapping is intentional here

**Relevant Context**
- `BookRequest` validation constraints should align with the domain rules (e.g. `price > 0`, `stockQuantity >= 0`)
- `BookResponse` includes `id` and timestamps to give clients full entity metadata

---

### Sub-Task 5 — Service Layer

**Status:** `[x] done`

**Intent**  
Implement the `BookService` class which contains all business logic. The controller delegates to this layer; this layer delegates to the repository. Keeping business logic here makes it independently unit-testable without a web layer.

**Expected Outcomes**
- `BookService.java` in `com.bookstore.service` annotated with `@Service`
- Methods: `createBook`, `getBookById`, `getAllBooks`, `updateBook`, `deleteBook`, `getBooksByCategory`, `searchBooksByAuthor`
- `ResourceNotFoundException` thrown when a book is not found by ID
- `DuplicateIsbnException` thrown when a new book's ISBN already exists

**Todo List**
1. Create `src/main/java/com/bookstore/service/BookService.java` with `@Service` and constructor-injected `BookRepository` and `BookMapper`
2. Implement `createBook(BookRequest request) → BookResponse` — check ISBN uniqueness, map to entity, save, return response
3. Implement `getBookById(Long id) → BookResponse` — find by ID or throw `ResourceNotFoundException`
4. Implement `getAllBooks() → List<BookResponse>` — return all books
5. Implement `updateBook(Long id, BookRequest request) → BookResponse` — load existing entity, update its fields, save, return response
6. Implement `deleteBook(Long id)` — find by ID (throw if not found), then delete
7. Implement `getBooksByCategory(String category) → List<BookResponse>` and `searchBooksByAuthor(String author) → List<BookResponse>` using custom repository finders

**Relevant Context**
- Custom exception classes are created in Sub-Task 6 — reference them by name here; they will exist when this task is implemented
- Use `@Transactional` on write methods (`createBook`, `updateBook`, `deleteBook`)
- Constructor injection (not `@Autowired` field injection) is the preferred pattern for testability

---

### Sub-Task 6 — Exception Handling

**Status:** `[x] done`

**Intent**  
Create a small exception hierarchy and a `@ControllerAdvice` global exception handler that returns consistent, structured JSON error responses for all error conditions. This prevents raw stack traces from leaking to API consumers.

**Expected Outcomes**
- `ResourceNotFoundException.java` in `com.bookstore.exception` extending `RuntimeException`
- `DuplicateIsbnException.java` in `com.bookstore.exception` extending `RuntimeException`
- `ErrorResponse.java` record/class in `com.bookstore.exception` with fields: `timestamp`, `status`, `error`, `message`, `path`
- `GlobalExceptionHandler.java` in `com.bookstore.exception` annotated with `@RestControllerAdvice`
- Handlers for: `ResourceNotFoundException` (404), `DuplicateIsbnException` (409), `MethodArgumentNotValidException` (400 — Bean Validation failures with field-level error messages), generic `Exception` fallback (500)

**Todo List**
1. Create `ResourceNotFoundException.java` — constructor takes `String message`, maps to HTTP 404
2. Create `DuplicateIsbnException.java` — constructor takes `String isbn`, maps to HTTP 409
3. Create `ErrorResponse.java` — Java record or class with `timestamp` (LocalDateTime), `status` (int), `error` (String), `message` (String), `path` (String)
4. Create `GlobalExceptionHandler.java` with `@RestControllerAdvice`:
   - `handleResourceNotFound` → 404
   - `handleDuplicateIsbn` → 409
   - `handleValidationErrors` — extract per-field error messages from `MethodArgumentNotValidException`, return 400 with concatenated or list-based message
   - `handleGenericException` → 500
5. Each handler method populates an `ErrorResponse` with the current timestamp and the request URI from `HttpServletRequest`

**Relevant Context**
- `MethodArgumentNotValidException` carries a `BindingResult`; iterate `getFieldErrors()` to build user-friendly messages
- Use `HttpServletRequest.getRequestURI()` to populate the `path` field

---

### Sub-Task 7 — REST Controller

**Status:** `[x] done`

**Intent**  
Expose all book operations as RESTful HTTP endpoints under `/api/v1/books`. The controller is intentionally thin — it validates input, delegates to the service, and maps results to HTTP responses.

**Expected Outcomes**
- `BookController.java` in `com.bookstore.controller` annotated with `@RestController` and `@RequestMapping("/api/v1/books")`
- Endpoints:
  - `POST /api/v1/books` — create book (201 Created)
  - `GET /api/v1/books` — list all books (200)
  - `GET /api/v1/books/{id}` — get by ID (200 / 404)
  - `PUT /api/v1/books/{id}` — update book (200 / 404)
  - `DELETE /api/v1/books/{id}` — delete book (204 No Content / 404)
  - `GET /api/v1/books/category/{category}` — filter by category (200)
  - `GET /api/v1/books/search?author={author}` — search by author (200)

**Todo List**
1. Create `BookController.java` with constructor-injected `BookService`
2. Map each endpoint as described above using `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
3. Use `@Valid` on `@RequestBody` parameters to trigger Bean Validation
4. Return `ResponseEntity<BookResponse>` (or `ResponseEntity<List<BookResponse>>`) with the correct HTTP status code for each operation
5. Add Springdoc annotations `@Operation`, `@ApiResponse`, `@Tag` to document each endpoint for Swagger UI

**Relevant Context**
- `DELETE` must return `ResponseEntity.noContent().build()` (204) on success
- `POST` must return `ResponseEntity.status(HttpStatus.CREATED).body(response)` (201)
- `@Valid` on the request body triggers `MethodArgumentNotValidException`, which is handled by `GlobalExceptionHandler`

---

### Sub-Task 8 — OpenAPI / Swagger Configuration

**Status:** `[x] done`

**Intent**  
Configure Springdoc OpenAPI to provide interactive API documentation at `/swagger-ui.html`, giving developers and testers a self-service interface to explore and invoke the API.

**Expected Outcomes**
- `OpenApiConfig.java` in `com.bookstore.config` defining an `OpenAPI` bean
- API info populated: title "Bookstore API", version "v1", description, contact
- Swagger UI accessible at `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON spec accessible at `http://localhost:8080/v3/api-docs`

**Todo List**
1. Create `src/main/java/com/bookstore/config/OpenApiConfig.java` annotated with `@Configuration`
2. Define a `@Bean` of type `OpenAPI` with `Info` object: title, version `v1`, description "Bookstore E-Commerce API", and a `Contact` with a placeholder name/email
3. Confirm no additional `application.properties` entries are needed (Springdoc auto-configures with the starter on classpath)

**Relevant Context**
- Springdoc 2.x is compatible with Spring Boot 3.x (uses Jakarta EE 9+ namespaces)
- The `springdoc-openapi-starter-webmvc-ui` dependency declared in Sub-Task 1 is sufficient — no extra config is needed beyond the `@Bean`

---

### Sub-Task 9 — H2 Test Data Seeding

**Status:** `[x] done`

**Intent**  
Populate the H2 in-memory database with sample book records on every startup so developers and testers have realistic data to work with immediately — no manual API calls needed to start testing.

**Expected Outcomes**
- Sample books (at least 5, across 2-3 categories) inserted on application startup
- Data visible in H2 console at `http://localhost:8080/h2-console` and in Swagger UI responses
- Seeding implemented via a Spring `CommandLineRunner` or a `data.sql` file

**Todo List**
1. Create `src/main/java/com/bookstore/config/DataSeeder.java` implementing `CommandLineRunner`, annotated with `@Component`
2. Inject `BookRepository` (or `BookService`) and insert 5+ sample `Book` entities covering different categories (e.g. Fiction, Technology, Science)
3. Guard the seeder with a check: only insert if the repository is empty (`bookRepository.count() == 0`) to avoid duplicate-key errors if DDL strategy is changed later

**Relevant Context**
- The H2 console JDBC URL to use: `jdbc:h2:mem:bookstoredb`
- Driver class: `org.h2.Driver`; credentials match `application.properties` (default: `sa` / empty password)
- `CommandLineRunner` runs after the application context is fully initialised, so JPA is ready

---

### Sub-Task 10 — Unit and Integration Tests

**Status:** `[x] done`

**Intent**  
Write tests that validate the core service logic (unit tests with Mockito) and the REST endpoints (integration tests with `@SpringBootTest` + `MockMvc`). These confirm correctness and serve as a regression safety net.

**Expected Outcomes**
- `BookServiceTest.java` in `src/test/java/com/bookstore/service` — unit tests for all service methods using Mockito mocks
- `BookControllerTest.java` in `src/test/java/com/bookstore/controller` — integration tests using `@WebMvcTest` and `MockMvc`
- All tests pass with `mvn test`

**Todo List**
1. Create `BookServiceTest.java`:
   - Use `@ExtendWith(MockitoExtension.class)`, mock `BookRepository` and `BookMapper`
   - Test: create book (happy path), create book (duplicate ISBN throws `DuplicateIsbnException`), get by ID (not found throws `ResourceNotFoundException`), update, delete
2. Create `BookControllerTest.java`:
   - Use `@WebMvcTest(BookController.class)`, mock `BookService` with `@MockBean`
   - Test: POST returns 201, GET all returns 200 list, GET by ID returns 200, GET by ID not found returns 404 JSON error, PUT returns 200, DELETE returns 204, POST with invalid body returns 400 with error fields
3. Use `ObjectMapper` to serialize/deserialize JSON in controller tests

**Relevant Context**
- `@WebMvcTest` does NOT load the full application context — only the web layer; `BookService` must be provided as a `@MockBean`
- `MockMvc.perform(...)` with `.andExpect(status().isCreated())` etc. is the correct assertion style
- Test data should use `BookRequest` / `BookResponse` builders or constructors directly — no need for a separate test fixture class

---

## Package Structure Reference

```
com.bookstore
├── BookstoreApiApplication.java
├── config
│   ├── OpenApiConfig.java
│   └── DataSeeder.java
├── controller
│   └── BookController.java
├── domain
│   └── Book.java
├── dto
│   ├── BookRequest.java
│   └── BookResponse.java
├── exception
│   ├── ResourceNotFoundException.java
│   ├── DuplicateIsbnException.java
│   ├── ErrorResponse.java
│   └── GlobalExceptionHandler.java
├── mapper
│   └── BookMapper.java
├── repository
│   └── BookRepository.java
└── service
    └── BookService.java
```

## API Endpoints Summary

| Method | Path | Description | Success Status |
|--------|------|-------------|----------------|
| POST | `/api/v1/books` | Create a new book | 201 Created |
| GET | `/api/v1/books` | List all books | 200 OK |
| GET | `/api/v1/books/{id}` | Get book by ID | 200 OK |
| PUT | `/api/v1/books/{id}` | Update a book | 200 OK |
| DELETE | `/api/v1/books/{id}` | Delete a book | 204 No Content |
| GET | `/api/v1/books/category/{category}` | Filter by category | 200 OK |
| GET | `/api/v1/books/search?author= | Search by author | 200 OK |

## Local Development URLs

| Resource | URL |
|----------|-----|
| API Base | `http://localhost:8080/api/v1/books` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| H2 Console | `http://localhost:8080/h2-console` |

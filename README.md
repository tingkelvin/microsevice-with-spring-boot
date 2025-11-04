# Microservices Architecture with Spring Boot

A microservices-based product management system demonstrating best practices in distributed systems architecture using Spring Boot 3.0.4, WebFlux, and Gradle.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Microservices](#microservices)
- [Shared Modules](#shared-modules)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Key Design Patterns](#key-design-patterns)
- [Running the Application](#running-the-application)
- [Testing](#testing)

## 🎯 Overview

This project implements a microservices architecture where:

- **Product Service**: Manages product information
- **Review Service**: Handles product reviews
- **Recommendation Service**: Manages product recommendations
- **Product Composite Service**: Aggregates data from all services to provide a unified view

Each microservice is independently deployable and communicates via HTTP/REST APIs.

## 🏗️ Architecture

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Product Composite Service                 │
│                      (Port 7000 / 8080)                      │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ProductCompositeServiceImpl (@RestController)       │   │
│  │  - Receives HTTP requests                            │   │
│  │  - Aggregates data from multiple services            │   │
│  └──────────────────────────────────────────────────────┘   │
│                         │                                    │
│                         ▼                                    │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  ProductCompositeIntegration (@Component)            │   │
│  │  - HTTP client layer                                 │   │
│  │  - Makes REST calls to other services                │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ▼                ▼                ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Product    │ │   Review     │ │Recommendation│
│   Service    │ │   Service    │ │   Service    │
│              │ │              │ │              │
│ Port: 7001   │ │ Port: 7003   │ │ Port: 7002   │
│ (8080 docker)│ │ (8080 docker)│ │ (8080 docker)│
└──────────────┘ └──────────────┘ └──────────────┘
        │                │                │
        └────────────────┼────────────────┘
                         │
                    ┌────┴────┐
                    │   API   │  (Shared interfaces)
                    │   Util  │  (Shared utilities)
                    └─────────┘
```

### Communication Flow

1. **Client** → Product Composite Service (GET /product-composite/{productId})
2. **Product Composite Service** → Calls Product Service (GET /product/{productId})
3. **Product Composite Service** → Calls Review Service (GET /review?productId={id})
4. **Product Composite Service** → Calls Recommendation Service (GET /recommendation?productId={id})
5. **Product Composite Service** → Aggregates all responses → Returns `ProductAggregate`

## 📁 Project Structure

```
microsevice-with-spring-boot/
├── api/                              # Shared API module
│   └── src/main/java/se/magnus/api/
│       ├── core/
│       │   ├── product/              # Product API interfaces & models
│       │   ├── review/               # Review API interfaces & models
│       │   └── recommendation/       # Recommendation API interfaces & models
│       ├── composite/
│       │   └── product/              # Composite API interfaces & models
│       └── exceptions/               # Shared exceptions
│
├── util/                             # Shared utilities module
│   └── src/main/java/se/magnus/util/
│       └── http/
│           ├── ServiceUtil.java      # Service address utility
│           ├── HttpErrorInfo.java    # Error response model
│           └── GlobalControllerExceptionHandler.java  # Exception handler
│
├── microservices/
│   ├── product-service/              # Product microservice
│   ├── review-service/               # Review microservice
│   ├── recommendation-service/       # Recommendation microservice
│   └── product-composite-service/    # Composite/aggregator service
│
├── settings.gradle                   # Gradle multi-project settings
├── docker-compose.yml                # Docker Compose configuration
└── test-em-all.bash                  # Integration test script
```

## 🔧 Microservices

### 1. Product Service
- **Port**: 7001 (local) / 8080 (Docker)
- **Responsibility**: Manages product information
- **Endpoint**: `GET /product/{productId}`
- **Implementation**: `ProductServiceImpl` implements `ProductService` interface

### 2. Review Service
- **Port**: 7003 (local) / 8080 (Docker)
- **Responsibility**: Handles product reviews
- **Endpoint**: `GET /review?productId={productId}`
- **Implementation**: `ReviewServiceImpl` implements `ReviewService` interface

### 3. Recommendation Service
- **Port**: 7002 (local) / 8080 (Docker)
- **Responsibility**: Manages product recommendations
- **Endpoint**: `GET /recommendation?productId={productId}`
- **Implementation**: `RecommendationServiceImpl` implements `RecommendationService` interface

### 4. Product Composite Service
- **Port**: 7000 (local) / 8080 (Docker) - **Only service exposed externally**
- **Responsibility**: Aggregates data from all three services
- **Endpoint**: `GET /product-composite/{productId}`
- **Architecture**:
  - `ProductCompositeServiceImpl` (@RestController) - Handles HTTP requests and business logic
  - `ProductCompositeIntegration` (@Component) - HTTP client layer for calling other services

## 📦 Shared Modules

### API Module
- Contains shared interfaces (contracts) for all services
- Includes data models (DTOs)
- Defines REST endpoint mappings using Spring annotations
- **Key Concept**: Services depend on API interfaces, not implementations (loose coupling)

### Util Module
- **ServiceUtil**: Provides service address information (hostname/IP/port)
- **GlobalControllerExceptionHandler**: Centralized exception handling using `@RestControllerAdvice`
- **HttpErrorInfo**: Standardized error response format

## 💻 Technology Stack

- **Java**: 17
- **Spring Boot**: 3.0.4
- **Spring WebFlux**: Reactive web framework
- **Gradle**: Build tool (multi-project setup)
- **Jackson**: JSON serialization/deserialization
- **RestTemplate**: HTTP client for service-to-service communication
- **Docker**: Containerization
- **JUnit 5**: Testing framework
- **WebTestClient**: Reactive testing client

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Gradle 7.x or higher (or use `./gradlew`)
- Docker & Docker Compose (optional, for containerized deployment)

### Build the Project

```bash
./gradlew build
```

### Run Individual Services Locally

Each service can be run independently:

```bash
# Product Service
cd microservices/product-service
./gradlew bootRun

# Review Service
cd microservices/review-service
./gradlew bootRun

# Recommendation Service
cd microservices/recommendation-service
./gradlew bootRun

# Product Composite Service
cd microservices/product-composite-service
./gradlew bootRun
```

### Run with Docker Compose

```bash
# Build and start all services
docker-compose up --build

# Run in detached mode
docker-compose up -d

# Stop all services
docker-compose down
```

## 🌐 API Endpoints

### Product Composite Service (Main Entry Point)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/product-composite/{productId}` | Get aggregated product data (includes product, reviews, and recommendations) |

**Example:**
```bash
curl http://localhost:7000/product-composite/1
```

### Product Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/product/{productId}` | Get product information |

**Example:**
```bash
curl http://localhost:7001/product/1
```

### Review Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/review?productId={productId}` | Get reviews for a product |

**Example:**
```bash
curl http://localhost:7003/review?productId=1
```

### Recommendation Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/recommendation?productId={productId}` | Get recommendations for a product |

**Example:**
```bash
curl http://localhost:7002/recommendation?productId=1
```

## 🎨 Key Design Patterns

### 1. Interface-Based Design (API-First)

**Concept**: Services share interfaces (contracts) from the `api` module, not implementations.

**Why**:
- Loose coupling between services
- Services can evolve independently
- Easy to test with mocks
- Clear contract definition

**Example**:
```java
// api/src/main/java/se/magnus/api/core/review/ReviewService.java
public interface ReviewService {
    @GetMapping(value = "/review", produces = "application/json")
    List<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);
}

// review-service/.../ReviewServiceImpl.java
@RestController
public class ReviewServiceImpl implements ReviewService {
    // Implementation
}
```

### 2. Component Scanning Strategy

**Pattern**: `@ComponentScan("se.magnus")` scans the entire base package

**Why**:
- Discovers components from shared modules (`util`, `api`)
- Services need shared utilities (e.g., `ServiceUtil`, `GlobalControllerExceptionHandler`)
- Keeps configuration simple

**Important**: Each service only has its own classes + shared modules on the classpath. Other services' implementation classes are NOT included (proper microservice isolation).

### 3. Integration Layer Pattern

**Pattern**: Separation between REST Controller and HTTP Client

**Components**:
- **Service Implementation** (`ProductCompositeServiceImpl`): 
  - `@RestController` - Creates HTTP endpoints
  - Handles business logic and data aggregation
- **Integration Layer** (`ProductCompositeIntegration`):
  - `@Component` - Spring bean (NOT a REST controller)
  - Handles HTTP calls to other services
  - Manages error handling and URL construction

**Benefits**:
- Single Responsibility Principle
- Easy to test (mock the integration layer)
- Can swap HTTP clients without changing business logic
- Can add cross-cutting concerns (retry, circuit breaker) in one place

### 4. Dependency Injection

**Key Annotations**:
- `@Component`: Marks a class as a Spring-managed bean
- `@RestController`: Specialized `@Component` for REST endpoints
- `@Autowired`: Injects dependencies (can be omitted on constructors in Spring 4.3+)
- `@Value`: Injects configuration values (e.g., `${server.port}`)

**Example**:
```java
@Component
public class ServiceUtil {
    @Autowired
    public ServiceUtil(@Value("${server.port}") String port) {
        this.port = port;
    }
}
```

### 5. Exception Handling

**Pattern**: Global exception handler using `@RestControllerAdvice`

**Location**: `util/src/main/java/se/magnus/util/http/GlobalControllerExceptionHandler.java`

**Features**:
- Centralized exception handling
- Standardized error responses (`HttpErrorInfo`)
- Maps exceptions to HTTP status codes

## 🔄 Service Communication

### How Services Communicate

1. **Product Composite Service** uses `RestTemplate` to call other services
2. Service URLs are configured via `application.yml`:
   ```yaml
   app:
     product-service:
       host: localhost
       port: 7001
     review-service:
       host: localhost
       port: 7003
     recommendation-service:
       host: localhost
       port: 7002
   ```

3. In Docker: Services communicate using service names (product, review, recommendation)

### Example Flow

```java
// ProductCompositeIntegration.getProduct()
String url = "http://localhost:7001/product/" + productId;
Product product = restTemplate.getForObject(url, Product.class);
```

## 🧪 Testing

### Unit Tests

Each service includes unit tests using `WebTestClient`:

```bash
# Run all tests
./gradlew test

# Run tests for a specific service
cd microservices/review-service
./gradlew test
```

### Integration Tests

Use the provided test script:

```bash
# Test all services (default: localhost:8080)
./test-em-all.bash

# Test with custom host/port
HOST=localhost PORT=7000 ./test-em-all.bash
```

### Test Scenarios

The tests verify:
- ✅ Successful requests
- ✅ Missing parameters (400 Bad Request)
- ✅ Invalid parameters (400 Bad Request)
- ✅ Not found scenarios (404 or empty lists)
- ✅ Validation errors (422 Unprocessable Entity)

## 📝 Configuration

### Port Configuration

Each service's port is configured in `application.yml`:

```yaml
server.port: 7001  # Product Service (local)
server.port: 7002  # Recommendation Service (local)
server.port: 7003  # Review Service (local)
server.port: 7000  # Product Composite Service (local)
```

For Docker, profiles override to port 8080:

```yaml
---
spring.config.activate.on-profile: docker
server.port: 8080
```

### Component Scanning

All services use:
```java
@ComponentScan("se.magnus")
```

This allows:
- Discovery of shared components from `util` module
- Discovery of service-specific components
- Clean dependency injection

## 🔍 Key Concepts Explained

### Why Interfaces in API Module?

- **Loose Coupling**: Services depend on contracts, not implementations
- **Multiple Implementations**: Can have different implementations (e.g., REST service vs. client stub)
- **Testing**: Easy to mock interfaces
- **Versioning**: Can support multiple interface versions

### Why Two Classes in Composite Service?

- **ProductCompositeServiceImpl**: Business logic + REST endpoints
- **ProductCompositeIntegration**: HTTP client layer

**Separation of Concerns**: Business logic is separate from HTTP communication details.

### Mock Data

Currently, services return mock/stub data:
- Product Service: Returns mock product for any ID (except 13 = not found)
- Review Service: Returns 3 mock reviews for any product ID (except 213 = empty)
- Recommendation Service: Returns 3 mock recommendations (except 113 = empty)

This is typical for microservices tutorials/examples before database integration.

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Gradle Multi-Project Builds](https://docs.gradle.org/current/userguide/multi_project_builds.html)

## 🤝 Contributing

This is a learning/demonstration project. Feel free to explore and modify!

## 📄 License

This project is for educational purposes.

---

**Note**: This is a demonstration microservices architecture. In production, you would typically add:
- Database persistence
- Service discovery (Eureka, Consul)
- API Gateway
- Circuit breakers (Resilience4j)
- Distributed tracing
- Monitoring and logging
- Security (OAuth2, JWT)

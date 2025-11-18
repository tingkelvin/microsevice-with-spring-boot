# Microservices with Spring Boot

A comprehensive microservices architecture built with Spring Boot, featuring multiple services for product management, detection, and journey tracking.

## 📋 Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Services](#services)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [API Documentation](#api-documentation)
- [Docker Deployment](#docker-deployment)
- [Configuration](#configuration)
- [Development](#development)

## 🎯 Overview

This project implements a microservices architecture using Spring Boot 3.0.4, featuring:

- **Product Management Services**: Product, review, and recommendation services with composite aggregation
- **Detection Services**: License Plate Recognition (LPR) and Re-identification (ReID) services
- **Journey Tracking**: Service for tracking vehicle journeys
- **Composite Services**: Aggregation services that combine data from multiple microservices

The architecture follows microservices best practices with:
- Reactive programming using Spring WebFlux
- Service-to-service communication
- Centralized exception handling
- Docker containerization
- Comprehensive testing

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Composite Services                        │
│  ┌──────────────────────┐  ┌──────────────────────────────┐ │
│  │ Product Composite    │  │ Detection Composite          │ │
│  │   (Port 8080)        │  │   (Port 8086)                │ │
│  └──────────┬───────────┘  └──────────┬───────────────────┘ │
└─────────────┼──────────────────────────┼─────────────────────┘
              │                          │
    ┌─────────┴──────────┐    ┌──────────┴──────────┐
    │                    │    │                      │
┌───▼────────┐  ┌────────▼──┐ │  ┌──────────┐  ┌────▼────┐
│ Product    │  │ Review    │ │  │ LPR      │  │ ReID    │
│ Service    │  │ Service   │ │  │ Service  │  │ Service │
└────────────┘  └───────────┘ │  └──────────┘  └─────────┘
                               │
                               │
                    ┌──────────▼──────┐
                    │ Journey Service │
                    │  (MongoDB)      │
                    └─────────────────┘
```

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.0.4
- **Language**: Java 17
- **Build Tool**: Gradle
- **Reactive Stack**: Spring WebFlux
- **Database**: MongoDB 6.0.4
- **Message Broker**: Apache Kafka
- **Containerization**: Docker & Docker Compose
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Testing**: JUnit, Bash integration tests

## 📁 Project Structure

```
microsevice-with-spring-boot/
├── api/                          # Shared API models and interfaces
│   └── src/main/java/se/magnus/api/
│       ├── composite/            # Composite service APIs
│       ├── core/                 # Core service APIs
│       ├── event/                # Event models
│       └── exceptions/           # Exception definitions
├── util/                         # Shared utilities
│   └── src/main/java/se/magnus/util/
│       └── http/                 # HTTP utilities and exception handlers
├── microservices/
│   ├── product-service/          # Product microservice
│   ├── review-service/           # Review microservice
│   ├── recommendation-service/   # Recommendation microservice
│   ├── product-composite-service/# Product composite service
│   ├── lpr-service/              # License Plate Recognition service
│   ├── reid-service/             # Re-identification service
│   ├── journey-service/          # Journey tracking service
│   └── detection-composite-service/# Detection composite service
├── docker-compose.yml            # Docker Compose configuration
├── settings.gradle               # Gradle multi-project settings
├── test-em-all.bash              # Product composite integration tests
└── test-detection-all.bash      # Detection composite integration tests
```

## 🔧 Services

### Core Services

#### Product Service
- **Port**: 7001 (local) / 8080 (Docker)
- **Description**: Manages product information
- **API**: RESTful endpoints for product CRUD operations

#### Review Service
- **Port**: 7002 (local) / 8080 (Docker)
- **Description**: Manages product reviews
- **API**: RESTful endpoints for review operations

#### Recommendation Service
- **Port**: 7003 (local) / 8080 (Docker)
- **Description**: Provides product recommendations
- **API**: RESTful endpoints for recommendation operations

### Detection Services

#### LPR Service (License Plate Recognition)
- **Port**: 7004 (local) / 8080 (Docker)
- **Description**: Processes license plate detections
- **Features**:
  - Receives detection data via REST API
  - Publishes to Kafka topic `lpr-detections`
  - UUID tracking for vehicle objects
- **API**: `POST /lpr/detections`

#### ReID Service (Re-identification)
- **Port**: 7007 (local) / 8080 (Docker)
- **Description**: Handles vehicle re-identification
- **API**: RESTful endpoints for ReID operations

### Composite Services

#### Product Composite Service
- **Port**: 7000 (local) / 8080 (Docker)
- **Description**: Aggregates data from product, review, and recommendation services
- **API**: `GET /product-composite/{productId}`

#### Detection Composite Service
- **Port**: 7006 (local) / 8080 (Docker)
- **Description**: Aggregates detection data from LPR and ReID services
- **Features**:
  - Combines LPR detections with journey data
  - Provides unified API for detection queries
- **API**: `GET /detection-composite/{sourceId}`
- **Swagger UI**: `http://localhost:7006/openapi/swagger-ui.html`

### Journey Service
- **Port**: 7005 (local) / 8080 (Docker)
- **Description**: Tracks vehicle journeys
- **Database**: MongoDB
- **Features**: Stores and retrieves journey information

## 📋 Prerequisites

Before running the application, ensure you have the following installed:

- **Java 17** or higher
- **Gradle 7.x** or higher (or use the included Gradle wrapper)
- **Docker** and **Docker Compose** (for containerized deployment)
- **MongoDB** (if running locally without Docker)
- **Apache Kafka** (if running locally without Docker)
- **curl** and **jq** (for running integration tests)

## 🚀 Getting Started

### Clone the Repository

```bash
git clone <repository-url>
cd microsevice-with-spring-boot
```

### Build the Project

```bash
# Build all modules
./gradlew build

# Build a specific service
./gradlew :microservices:lpr-service:build
```

## 🏃 Running the Application

### Local Development

#### Option 1: Run Individual Services

```bash
# Run product composite service
./gradlew :microservices:product-composite-service:bootRun

# Run detection composite service
./gradlew :microservices:detection-composite-service:bootRun

# Run LPR service
./gradlew :microservices:lpr-service:bootRun

# Run journey service (requires MongoDB)
./gradlew :microservices:journey-service:bootRun
```

#### Option 2: Docker Compose (Recommended)

```bash
# Build and start all services
docker-compose up --build

# Start in detached mode
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down
```

### Service Ports (Local Development)

| Service | Port |
|---------|------|
| Product Composite | 7000 |
| Product Service | 7001 |
| Review Service | 7002 |
| Recommendation Service | 7003 |
| LPR Service | 7004 |
| Journey Service | 7005 |
| Detection Composite | 7006 |
| ReID Service | 7007 |

### Service Ports (Docker)

| Service | Port |
|---------|------|
| Product Composite | 8080 |
| Detection Composite | 8086 |
| ReID Service | 8087 |
| MongoDB | 27017 |

## 🧪 Testing

### Unit Tests

```bash
# Run all tests
./gradlew test

# Run tests for a specific service
./gradlew :microservices:lpr-service:test
```

### Integration Tests

#### Product Composite Service Tests

```bash
# Run tests (assumes services are already running)
./test-em-all.bash

# Start services, run tests, and stop services
./test-em-all.bash start stop
```

#### Detection Composite Service Tests

```bash
# Run tests (assumes services are already running)
./test-detection-all.bash

# Start services, run tests, and stop services
./test-detection-all.bash start stop
```

### Manual API Testing

#### Product Composite Service

```bash
# Get product composite data
curl http://localhost:7000/product-composite/1

# Test with different product IDs
curl http://localhost:7000/product-composite/113
curl http://localhost:7000/product-composite/213
```

#### Detection Composite Service

```bash
# Get detection composite data
curl http://localhost:7006/detection-composite/camera-001

# Test with different source IDs
curl http://localhost:7006/detection-composite/camera-002
```

#### LPR Service

```bash
# Post detection data
curl -X POST http://localhost:7004/lpr/detections \
  -H "Content-Type: application/json" \
  -d '{
    "sourceId": "camera-001",
    "unixTime": 1699614600000,
    "ntpTime": "2025-11-10T10:30:00Z",
    "detections": [{
      "objectUuid": "uuid-001",
      "vehicleBbox": {"x": 100, "y": 100, "width": 200, "height": 150},
      "plateBbox": {"x": 120, "y": 130, "width": 80, "height": 30},
      "plateNum": "ABC123",
      "ntpTime": "2025-11-10T10:30:00Z",
      "unixTime": 1699614600000
    }]
  }'
```

## 📚 API Documentation

### Swagger UI

The Detection Composite Service includes Swagger UI for interactive API documentation:

- **Local**: http://localhost:7006/openapi/swagger-ui.html
- **API Docs**: http://localhost:7006/openapi/v3/api-docs

### API Endpoints

#### Product Composite Service
- `GET /product-composite/{productId}` - Get aggregated product data

#### Detection Composite Service
- `GET /detection-composite/{sourceId}` - Get aggregated detection data

#### LPR Service
- `POST /lpr/detections` - Submit license plate detections

## 🐳 Docker Deployment

### Docker Compose Services

The `docker-compose.yml` file defines the following services:

- **product**: Product service
- **review**: Review service
- **recommendation**: Recommendation service
- **lpr**: License Plate Recognition service
- **reid**: Re-identification service
- **journey**: Journey tracking service (depends on MongoDB)
- **product-composite**: Product composite service
- **detection-composite**: Detection composite service
- **mongodb**: MongoDB database

### Environment Variables

Services use Spring profiles for configuration:
- **Local**: Default profile (no profile specified)
- **Docker**: `docker` profile (set via `SPRING_PROFILES_ACTIVE=docker`)

### Memory Limits

All services are configured with 512MB memory limits in Docker Compose.

## ⚙️ Configuration

### Application Configuration

Each service has its own `application.yml` file with:
- Server port configuration
- Service discovery settings
- Database connections
- Kafka configuration (where applicable)
- Logging levels

### Profile-Specific Configuration

Services support multiple profiles:
- **Default**: Local development configuration
- **Docker**: Containerized deployment configuration

### MongoDB Configuration

MongoDB is configured with:
- **Username**: `root`
- **Password**: `example`
- **Database**: `journey-db`
- **Port**: `27017`

## 💻 Development

### Project Setup

1. **Create new microservice**:
   ```bash
   # Use the create-projects.bash script as reference
   # Or manually create following the existing structure
   ```

2. **Add to settings.gradle**:
   ```gradle
   include ':microservices:your-service'
   ```

3. **Add to docker-compose.yml**:
   ```yaml
   your-service:
     build: microservices/your-service
     mem_limit: 512m
     environment:
       - SPRING_PROFILES_ACTIVE=docker
   ```

### Code Structure

- **API Models**: Defined in the `api` module for shared use
- **Exception Handling**: Centralized in `util` module
- **Service Implementation**: Each microservice is self-contained

### Best Practices

- Use reactive programming (WebFlux) for non-blocking operations
- Implement proper error handling using `GlobalControllerExceptionHandler`
- Follow RESTful API design principles
- Write comprehensive unit and integration tests
- Use Docker for consistent deployment environments

## 📝 Additional Documentation

- [LPR Service Implementation](./LPR_SERVICE_IMPLEMENTATION.md) - Detailed documentation for the LPR service

## 🤝 Contributing

1. Follow the existing code structure and patterns
2. Write tests for new features
3. Update documentation as needed
4. Ensure all tests pass before submitting

## 📄 License

[Add your license information here]

## 👥 Authors

[Add author information here]

---

**Note**: This is a microservices architecture project. Ensure all required infrastructure (MongoDB, Kafka) is running before starting the services, or use Docker Compose for a complete environment setup.


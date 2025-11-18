# Reid Service

## Overview
This microservice manages Reid (Re-identification) data for vehicles tracked across different locations.

## Features
- REST API for retrieving Reid data
- Mock Reid data generation
- Support for multiple Reid identifiers

## API Endpoints

### GET /reid/{reid}
Retrieve Reid data by Reid ID.

**Request:**
```bash
curl http://localhost:7007/reid/REID-001
```

**Response:**
```json
{
  "reid": "REID-001",
  "vehicleId": "VEHICLE-123",
  "plateNumber": "ABC456",
  "timestamp": 1731348600000,
  "location": "Location-3",
  "status": "active"
}
```

## Configuration

### Application Properties
- `server.port`: HTTP server port (default: 7007, docker: 8080)

### Environment Variables (Docker)
- `SPRING_PROFILES_ACTIVE`: Set to `docker` for containerized deployment

## Running Locally

### Prerequisites
- Java 17+

### Start the service
```bash
./gradlew :microservices:reid-service:bootRun
```

The service will be available at http://localhost:7007

## Running with Docker

### Build and start all services
```bash
./gradlew build
docker compose up --build
```

The Reid service will be available at http://localhost:8087

## Testing

### Run unit tests
```bash
./gradlew :microservices:reid-service:test
```

### Sample curl requests
```bash
# Get reid data for REID-001
curl http://localhost:7007/reid/REID-001

# Get reid data for REID-002
curl http://localhost:7007/reid/REID-002
```

## Architecture

The service consists of:
- **API Models**: Reid domain object (in `api` module)
- **Service Layer**: Business logic for generating Reid data
- **REST Controller**: HTTP endpoints for accessing Reid information

## Integration

This service is designed to work with:
1. **Detection Composite Service**: Can be integrated to provide Reid data alongside detection data
2. **Journey Service**: Can correlate Reid data with journey information



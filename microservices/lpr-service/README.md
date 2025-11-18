# LPR (License Plate Recognition) Service

## Overview
This microservice processes license plate detection events and publishes them to Kafka for downstream consumption.

## Features
- REST API for receiving license plate detections
- Kafka producer for publishing detection events
- UUID tracking for vehicle objects
- Support for multiple detection sources
- Configurable Kafka topics

## API Endpoints

### POST /lpr/detections
Process and publish license plate detections to Kafka.

**Request Body:**
```json
{
  "sourceId": "camera-001",
  "unixTime": 1699614600000,
  "ntpTime": "2025-11-10T10:30:00Z",
  "detections": [
    {
      "objectUuid": "uuid-001",
      "vehicleBbox": {
        "x": 100,
        "y": 100,
        "width": 200,
        "height": 150
      },
      "plateBbox": {
        "x": 120,
        "y": 130,
        "width": 80,
        "height": 30
      },
      "plateNum": "ABC123",
      "ntpTime": "2025-11-10T10:30:00Z",
      "unixTime": 1699614600000
    }
  ]
}
```

**Response:** 200 OK (no content)

## Configuration

### Application Properties
- `server.port`: HTTP server port (default: 7004, docker: 8080)
- `lpr.kafka.topic`: Kafka topic name (default: lpr-detections)
- `spring.kafka.bootstrap-servers`: Kafka broker address

### Environment Variables (Docker)
- `SPRING_PROFILES_ACTIVE`: Set to `docker` for containerized deployment

## Running Locally

### Prerequisites
- Java 17+
- Kafka broker running on localhost:9092

### Start the service
```bash
./gradlew :microservices:lpr-service:bootRun
```

The service will be available at http://localhost:7004

## Running with Docker

### Build and start all services
```bash
./gradlew build
docker-compose up --build
```

The LPR service will be available at http://localhost:8084

## Testing

### Run unit tests
```bash
./gradlew :microservices:lpr-service:test
```

### Sample curl request
```bash
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

## Architecture

The service consists of:
- **API Models**: Domain objects for detections (in `api` module)
- **Service Layer**: Business logic for processing detections
- **Kafka Producer**: Publishing detection events to Kafka
- **UUID Tracking**: Mapping tracking IDs to persistent UUIDs

## Integration

This service is designed to work with:
1. **Detection Sources**: Cameras or video analytics systems that send detection data
2. **Kafka Broker**: Message broker for event streaming
3. **Downstream Consumers**: Services that consume detection events from Kafka



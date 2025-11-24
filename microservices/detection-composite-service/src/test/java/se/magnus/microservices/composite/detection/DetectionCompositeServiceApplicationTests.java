package se.magnus.microservices.composite.detection;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import se.magnus.api.core.lpr.BoundingBox;
import se.magnus.api.core.lpr.LicencePlate;
import se.magnus.api.core.reid.Reid;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.microservices.composite.detection.services.DetectionCompositeIntegration;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class DetectionCompositeServiceApplicationTests {

  private static final String SOURCE_ID_OK = "camera-001";
  private static final String SOURCE_ID_NOT_FOUND = "camera-999";
  private static final String SOURCE_ID_INVALID = "invalid-source";

  @Autowired
  private WebTestClient client;

  @MockBean
  private DetectionCompositeIntegration integration;

  @BeforeEach
  void setUp() {
    // Setup mock for successful case
    List<LicencePlate> plates = new ArrayList<>();
    BoundingBox vehicleBbox = new BoundingBox(100, 100, 200, 150);
    BoundingBox plateBbox = new BoundingBox(120, 130, 80, 30);
    plates.add(new LicencePlate(
      SOURCE_ID_OK,
      "uuid-001",
      vehicleBbox,
      plateBbox,
      "ABC123",
      "2025-11-10T10:30:00Z",
      1699614600000L,
      "mock-service-address"));
    when(integration.getLprs(SOURCE_ID_OK)).thenReturn(Flux.fromIterable(plates));

    List<Reid> reids = singletonList(
      new Reid("reid-001", SOURCE_ID_OK, "ABC123", 1699614600000L, "Location-1", "active", "mock-address"));
    when(integration.getReids(SOURCE_ID_OK)).thenReturn(Flux.fromIterable(reids));

    // Setup mock for not found case
    when(integration.getLprs(SOURCE_ID_NOT_FOUND))
      .thenReturn(Flux.error(new NotFoundException("NOT FOUND: " + SOURCE_ID_NOT_FOUND)));
    when(integration.getReids(SOURCE_ID_NOT_FOUND))
      .thenReturn(Flux.empty());

    // Setup mock for invalid input case
    when(integration.getLprs(SOURCE_ID_INVALID))
      .thenReturn(Flux.error(new InvalidInputException("INVALID: " + SOURCE_ID_INVALID)));
    when(integration.getReids(SOURCE_ID_INVALID))
      .thenReturn(Flux.empty());
  }

  @Test
  void contextLoads() {}

  @Test
  void getDetectionAggregate() {
    getAndVerifyDetectionAggregate(SOURCE_ID_OK, OK)
      .jsonPath("$.sourceId").isEqualTo(SOURCE_ID_OK)
      .jsonPath("$.detections.length()").isEqualTo(1)
      .jsonPath("$.detections[0].plateNum").isEqualTo("ABC123")
      .jsonPath("$.reids.length()").isEqualTo(1)
      .jsonPath("$.reids[0].reid").isEqualTo("reid-001")
      .jsonPath("$.serviceAddress").exists();
  }

  @Test
  void getDetectionAggregateWithoutReid() {
    String sourceId = "camera-002";
    
    List<LicencePlate> plates = singletonList(
      new LicencePlate(sourceId, "uuid-002", 
        new BoundingBox(100, 100, 200, 150),
        new BoundingBox(120, 130, 80, 30),
        "XYZ789", "2025-11-10T11:00:00Z", 1699616400000L, "mock-service-address"));
    when(integration.getLprs(sourceId)).thenReturn(Flux.fromIterable(plates));
    when(integration.getReids(sourceId)).thenReturn(Flux.error(new NotFoundException("No reid found")));

    getAndVerifyDetectionAggregate(sourceId, OK)
      .jsonPath("$.sourceId").isEqualTo(sourceId)
      .jsonPath("$.detections.length()").isEqualTo(1)
      .jsonPath("$.detections[0].plateNum").isEqualTo("XYZ789")
      .jsonPath("$.reids").isEmpty();
  }

  @Test
  void getDetectionAggregateNotFound() {
    getAndVerifyDetectionAggregate(SOURCE_ID_NOT_FOUND, NOT_FOUND)
      .jsonPath("$.path").isEqualTo("/detection-composite/" + SOURCE_ID_NOT_FOUND)
      .jsonPath("$.message").isEqualTo("NOT FOUND: " + SOURCE_ID_NOT_FOUND);
  }

  @Test
  void getDetectionAggregateInvalidInput() {
    getAndVerifyDetectionAggregate(SOURCE_ID_INVALID, UNPROCESSABLE_ENTITY)
      .jsonPath("$.path").isEqualTo("/detection-composite/" + SOURCE_ID_INVALID)
      .jsonPath("$.message").isEqualTo("INVALID: " + SOURCE_ID_INVALID);
  }

  @Test
  void getDetectionAggregateEmptyList() {
    String sourceId = "empty-camera";
    
    when(integration.getLprs(sourceId)).thenReturn(Flux.empty());
    when(integration.getReids(sourceId)).thenReturn(Flux.empty());

    getAndVerifyDetectionAggregate(sourceId, NOT_FOUND)
      .jsonPath("$.path").isEqualTo("/detection-composite/" + sourceId)
      .jsonPath("$.message").isEqualTo("No detections found for sourceId: " + sourceId);
  }

  private WebTestClient.BodyContentSpec getAndVerifyDetectionAggregate(String sourceId, HttpStatus expectedStatus) {
    return client.get()
      .uri("/detection-composite/" + sourceId)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }
}


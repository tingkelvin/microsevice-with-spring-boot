package se.magnus.microservices.core.lpr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.magnus.api.core.lpr.BoundingBox;
import se.magnus.api.core.lpr.LicencePlate;
import se.magnus.microservices.core.lpr.persistence.LicencePlateRepository;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class LprServiceApplicationTests {

  @Autowired
  private WebTestClient client;

  @Autowired
  private LicencePlateRepository repository;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();
  }

  @Test
  void getDetectionsBySourceId() {
    String sourceId = "camera-001";

    assertEquals(0, repository.findBySourceId(sourceId).size());

    postAndVerifyDetection(sourceId, "uuid-001", "ABC123", OK);
    postAndVerifyDetection(sourceId, "uuid-002", "XYZ789", OK);
    postAndVerifyDetection(sourceId, "uuid-003", "DEF456", OK);

    assertEquals(3, repository.findBySourceId(sourceId).size());

    getAndVerifyDetectionsBySourceId(sourceId, OK)
      .jsonPath("$.length()").isEqualTo(3)
      .jsonPath("$[2].sourceId").isEqualTo(sourceId)
      .jsonPath("$[2].objectUuid").isEqualTo("uuid-003")
      .jsonPath("$[2].plateNum").isEqualTo("DEF456");
  }

  @Test
  void duplicateError() {
    String sourceId = "camera-001";
    String objectUuid = "uuid-001";

    assertEquals(0, repository.count());

    // First POST should succeed (returns void, no body to check)
    postAndVerifyDetection(sourceId, objectUuid, "ABC123", OK);

    assertEquals(1, repository.count());

    // Second POST with same objectUuid should fail with duplicate error
    postAndVerifyDetection(sourceId, objectUuid, "ABC123", UNPROCESSABLE_ENTITY)
      .jsonPath("$.path").isEqualTo("/lpr/detection")
      .jsonPath("$.message").isEqualTo("Duplicate key, Object UUID: " + objectUuid);

    assertEquals(1, repository.count());
  }

  @Test
  void getDetectionsMultipleSources() {
    String sourceId1 = "camera-001";
    String sourceId2 = "camera-002";

    postAndVerifyDetection(sourceId1, "uuid-001", "ABC123", OK);
    postAndVerifyDetection(sourceId1, "uuid-002", "XYZ789", OK);
    postAndVerifyDetection(sourceId2, "uuid-003", "DEF456", OK);

    assertEquals(2, repository.findBySourceId(sourceId1).size());
    assertEquals(1, repository.findBySourceId(sourceId2).size());

    getAndVerifyDetectionsBySourceId(sourceId1, OK)
      .jsonPath("$.length()").isEqualTo(2);

    getAndVerifyDetectionsBySourceId(sourceId2, OK)
      .jsonPath("$.length()").isEqualTo(1);
  }

  @Test
  void getDetectionsInvalidSourceIdEmpty() {
    String sourceIdEmpty = "";

    client.get()
      .uri("/lpr/detections/" + sourceIdEmpty)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound();
  }

  @Test
  void getDetectionsNotFound() {
    String sourceId = "camera-999";

    getAndVerifyDetectionsBySourceId(sourceId, OK)
      .jsonPath("$.length()").isEqualTo(0);
  }

  @Test
  void verifyBoundingBoxes() {
    String sourceId = "camera-001";
    String objectUuid = "uuid-bbox-001";

    BoundingBox vehicleBbox = new BoundingBox(100, 100, 200, 150);
    BoundingBox plateBbox = new BoundingBox(120, 130, 80, 30);

    LicencePlate detection = new LicencePlate(
      sourceId,
      objectUuid,
      vehicleBbox,
      plateBbox,
      "ABC123",
      "2025-11-11T10:30:00Z",
      System.currentTimeMillis()
    );

    client.post()
      .uri("/lpr/detection")
      .body(just(detection), LicencePlate.class)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(OK);

    getAndVerifyDetectionsBySourceId(sourceId, OK)
      .jsonPath("$.length()").isEqualTo(1)
      .jsonPath("$[0].vehicleBbox.x").isEqualTo(100)
      .jsonPath("$[0].vehicleBbox.y").isEqualTo(100)
      .jsonPath("$[0].vehicleBbox.width").isEqualTo(200)
      .jsonPath("$[0].vehicleBbox.height").isEqualTo(150)
      .jsonPath("$[0].plateBbox.x").isEqualTo(120)
      .jsonPath("$[0].plateBbox.y").isEqualTo(130)
      .jsonPath("$[0].plateBbox.width").isEqualTo(80)
      .jsonPath("$[0].plateBbox.height").isEqualTo(30);
  }

  @Test
  void searchByPlateNumber() {
    String sourceId1 = "camera-001";
    String sourceId2 = "camera-002";
    String plateNum = "ABC123";

    postAndVerifyDetection(sourceId1, "uuid-001", plateNum, OK);
    postAndVerifyDetection(sourceId2, "uuid-002", plateNum, OK);
    postAndVerifyDetection(sourceId1, "uuid-003", "XYZ789", OK);

    // Verify we can find all detections with same plate number
    assertEquals(2, repository.findByPlateNum(plateNum).size());
    assertEquals(3, repository.count());
  }

  private WebTestClient.BodyContentSpec getAndVerifyDetectionsBySourceId(String sourceId, HttpStatus expectedStatus) {
    return client.get()
      .uri("/lpr/detections/" + sourceId)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus)
      .expectHeader().contentType(APPLICATION_JSON)
      .expectBody();
  }

  private WebTestClient.BodyContentSpec postAndVerifyDetection(
      String sourceId, String objectUuid, String plateNum, HttpStatus expectedStatus) {
    
    BoundingBox vehicleBbox = new BoundingBox(100, 100, 200, 150);
    BoundingBox plateBbox = new BoundingBox(120, 130, 80, 30);
    
    LicencePlate detection = new LicencePlate(
      sourceId,
      objectUuid,
      vehicleBbox,
      plateBbox,
      plateNum,
      "2025-11-11T10:30:00Z",
      System.currentTimeMillis()
    );

    WebTestClient.ResponseSpec response = client.post()
      .uri("/lpr/detection")
      .body(just(detection), LicencePlate.class)
      .accept(APPLICATION_JSON)
      .exchange()
      .expectStatus().isEqualTo(expectedStatus);
    
    // Only expect JSON content if status is OK
    if (expectedStatus == OK) {
      return response.expectBody();
    } else {
      return response
        .expectHeader().contentType(APPLICATION_JSON)
        .expectBody();
    }
  }
}


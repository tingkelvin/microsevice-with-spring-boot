// package se.magnus.microservices.core.reid;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
// import static org.springframework.http.HttpStatus.*;
// import static org.springframework.http.MediaType.APPLICATION_JSON;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.HttpStatus;
// import org.springframework.test.web.reactive.server.WebTestClient;
// import se.magnus.api.core.reid.Reid;
// import se.magnus.microservices.core.reid.persistence.ReidRepository;

// @SpringBootTest(webEnvironment = RANDOM_PORT)
// class ReidServiceApplicationTests extends MongoDbTestBase {

//   @Autowired
//   private WebTestClient client;

//   @Autowired
//   private ReidRepository repository;

//   @BeforeEach
//   void setupDb() {
//     repository.deleteAll();
//   }

//   @Test
//   void getReidBySourceId() {
//     String sourceId = "camera-001";

//     assertEquals(0, repository.findBySourceId(sourceId).size());

//     postAndVerifyReid(sourceId, "reid-001", "ABC123", OK);
//     postAndVerifyReid(sourceId, "reid-002", "XYZ789", OK);
//     postAndVerifyReid(sourceId, "reid-003", "DEF456", OK);

//     assertEquals(3, repository.findBySourceId(sourceId).size());

//     getAndVerifyReidBySourceId(sourceId, OK)
//       .jsonPath("$.length()").isEqualTo(3)
//       .jsonPath("$[2].sourceId").isEqualTo(sourceId)
//       .jsonPath("$[2].reid").isEqualTo("reid-003")
//       .jsonPath("$[2].plateNumber").isEqualTo("DEF456");
//   }

//   @Test
//   void duplicateError() {
//     String sourceId = "camera-001";
//     String reidId = "reid-001";

//     assertEquals(0, repository.count());

//     postAndVerifyReid(sourceId, reidId, "ABC123", OK)
//       .jsonPath("$.sourceId").isEqualTo(sourceId)
//       .jsonPath("$.reid").isEqualTo(reidId);

//     assertEquals(1, repository.count());

//     postAndVerifyReid(sourceId, reidId, "ABC123", UNPROCESSABLE_ENTITY)
//       .jsonPath("$.path").isEqualTo("/reid")
//       .jsonPath("$.message").isEqualTo("Duplicate key, Source Id: " + sourceId + ", Reid Id: " + reidId);

//     assertEquals(1, repository.count());
//   }

//   @Test
//   void deleteReids() {
//     String sourceId = "camera-001";
//     String reidId = "reid-001";

//     postAndVerifyReid(sourceId, reidId, "ABC123", OK);
//     assertEquals(1, repository.findBySourceId(sourceId).size());

//     deleteAndVerifyReidBySourceId(sourceId, OK);
//     assertEquals(0, repository.findBySourceId(sourceId).size());

//     deleteAndVerifyReidBySourceId(sourceId, OK);
//   }

//   @Test
//   void getReidsNotFound() {
//     String sourceId = "camera-999";
    
//     getAndVerifyReidBySourceId(sourceId, OK)
//       .jsonPath("$.length()").isEqualTo(0);
//   }

//   @Test
//   void getReidInvalidSourceId() {
//     String sourceId = "";
    
//     client.get()
//       .uri("/reid/" + sourceId)
//       .accept(APPLICATION_JSON)
//       .exchange()
//       .expectStatus().isEqualTo(NOT_FOUND);
//   }

//   @Test
//   void contextLoads() {
//   }

//   private WebTestClient.BodyContentSpec getAndVerifyReidBySourceId(String sourceId, HttpStatus expectedStatus) {
//     return client.get()
//       .uri("/reid/" + sourceId)
//       .accept(APPLICATION_JSON)
//       .exchange()
//       .expectStatus().isEqualTo(expectedStatus)
//       .expectHeader().contentType(APPLICATION_JSON)
//       .expectBody();
//   }

//   private WebTestClient.BodyContentSpec postAndVerifyReid(String sourceId, String reidId, String plateNumber, HttpStatus expectedStatus) {
//     Reid reid = new Reid(reidId, sourceId, plateNumber, System.currentTimeMillis(), "Location-1", "active", "SA");
    
//     return client.post()
//       .uri("/reid")
//       .bodyValue(reid)
//       .accept(APPLICATION_JSON)
//       .exchange()
//       .expectStatus().isEqualTo(expectedStatus)
//       .expectHeader().contentType(APPLICATION_JSON)
//       .expectBody();
//   }

//   private WebTestClient.BodyContentSpec deleteAndVerifyReidBySourceId(String sourceId, HttpStatus expectedStatus) {
//     return client.delete()
//       .uri("/reid/" + sourceId)
//       .accept(APPLICATION_JSON)
//       .exchange()
//       .expectStatus().isEqualTo(expectedStatus)
//       .expectBody();
//   }
// }


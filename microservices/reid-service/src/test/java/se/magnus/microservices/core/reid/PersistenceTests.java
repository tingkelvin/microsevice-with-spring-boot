package se.magnus.microservices.core.reid;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;
import se.magnus.microservices.core.reid.persistence.ReidEntity;
import se.magnus.microservices.core.reid.persistence.ReidRepository;

@DataMongoTest
class PersistenceTests extends MongoDbTestBase {

  @Autowired
  private ReidRepository repository;

  private ReidEntity savedEntity;

  @BeforeEach
  void setupDb() {
    StepVerifier.create(repository.deleteAll()).verifyComplete();

    ReidEntity entity = new ReidEntity("reid-001", "camera-001", "ABC123", System.currentTimeMillis(), "Location-1", "active");
    
    StepVerifier.create(repository.save(entity))
      .expectNextMatches(createdEntity -> {
        savedEntity = createdEntity;
        assertEqualsReid(entity, savedEntity);
        return true;
      })
      .verifyComplete();
  }

  @Test
  void create() {
    ReidEntity newEntity = new ReidEntity("reid-002", "camera-001", "DEF456", System.currentTimeMillis(), "Location-2", "active");
   
    // Save new entity and verify it matches what we created
    StepVerifier.create(repository.save(newEntity))
      .expectNextMatches(createdEntity -> {
        assertEqualsReid(newEntity, createdEntity);
        return true;
      })
      .verifyComplete();

    // Verify we can find both entities by sourceId and check their content
    StepVerifier.create(repository.findBySourceId(newEntity.getSourceId())
        .collectList())
      .expectNextMatches(entities -> {
        // Should find exactly 2 entities
        assertEquals(2, entities.size());
        
        // Verify both entities are present with correct content
        boolean foundSaved = entities.stream()
          .anyMatch(e -> areReidsEqual(savedEntity, e));
        boolean foundNew = entities.stream()
          .anyMatch(e -> areReidsEqual(newEntity, e));
        
        assertTrue(foundSaved, "Should find savedEntity");
        assertTrue(foundNew, "Should find newEntity");
        
        return true;
      })
      .verifyComplete();

    // Verify total count is 2
    StepVerifier.create(repository.count())
      .expectNext(2L)
      .verifyComplete();
  }

  // @Test
  // void update() {
  //   savedEntity.setStatus("inactive");
  //   savedEntity.setLocation("Location-2");
  //   repository.save(savedEntity);

  //   ReidEntity foundEntity = repository.findById(savedEntity.getId()).get();
  //   assertEquals(1, (int)foundEntity.getVersion());
  //   assertEquals("inactive", foundEntity.getStatus());
  //   assertEquals("Location-2", foundEntity.getLocation());
  // }

  // @Test
  // void delete() {
  //   repository.delete(savedEntity);
  //   assertFalse(repository.existsById(savedEntity.getId()));
  // }

  // @Test
  // void getBySourceId() {
  //   List<ReidEntity> entities = repository.findBySourceId(savedEntity.getSourceId());

  //   assertEquals(1, entities.size());
  //   assertEqualsReid(savedEntity, entities.get(0));
  // }

  // @Test
  // void duplicateError() {
  //   assertThrows(DuplicateKeyException.class, () -> {
  //     ReidEntity entity = new ReidEntity(savedEntity.getReid(), savedEntity.getSourceId(), "DEF456", System.currentTimeMillis(), "Location-1", "active");
  //     repository.save(entity);
  //   });
  // }

  // @Test
  // void optimisticLockError() {
  //   // Store the saved entity in two separate entity objects
  //   ReidEntity entity1 = repository.findById(savedEntity.getId()).get();
  //   ReidEntity entity2 = repository.findById(savedEntity.getId()).get();

  //   // Update the entity using the first entity object
  //   entity1.setStatus("inactive");
  //   repository.save(entity1);

  //   // Update the entity using the second entity object.
  //   // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
  //   assertThrows(OptimisticLockingFailureException.class, () -> {
  //     entity2.setStatus("pending");
  //     repository.save(entity2);
  //   });

  //   // Get the updated entity from the database and verify its new state
  //   ReidEntity updatedEntity = repository.findById(savedEntity.getId()).get();
  //   assertEquals(1, (int)updatedEntity.getVersion());
  //   assertEquals("inactive", updatedEntity.getStatus());
  // }

  // @Test
  // void paging() {
  //   repository.deleteAll();

  //   List<ReidEntity> newReids = rangeClosed(1, 10)
  //     .mapToObj(i -> new ReidEntity("reid-" + i, "camera-paging", "PLATE" + i, System.currentTimeMillis() + i, "Location-" + i, "active"))
  //     .collect(Collectors.toList());
  //   repository.saveAll(newReids);

  //   Pageable nextPage = PageRequest.of(0, 4, ASC, "reid");
  //   nextPage = testNextPage(nextPage, "[reid-1, reid-10, reid-2, reid-3]", true);
  //   nextPage = testNextPage(nextPage, "[reid-4, reid-5, reid-6, reid-7]", true);
  //   nextPage = testNextPage(nextPage, "[reid-8, reid-9]", false);
  // }

  // @Test
  // void getBySourceIdMultipleRecords() {
  //   // Add more entities with the same sourceId
  //   ReidEntity entity2 = new ReidEntity("reid-002", "camera-001", "XYZ789", System.currentTimeMillis(), "Location-2", "active");
  //   ReidEntity entity3 = new ReidEntity("reid-003", "camera-001", "DEF456", System.currentTimeMillis(), "Location-3", "inactive");
    
  //   repository.save(entity2);
  //   repository.save(entity3);

  //   List<ReidEntity> entities = repository.findBySourceId("camera-001");
  //   assertEquals(3, entities.size());
  // }

  // @Test
  // void getBySourceIdNoResults() {
  //   List<ReidEntity> entities = repository.findBySourceId("camera-999");
  //   assertEquals(0, entities.size());
  // }

  // private Pageable testNextPage(Pageable nextPage, String expectedReidIds, boolean expectsNextPage) {
  //   Page<ReidEntity> reidPage = repository.findAll(nextPage);
  //   assertEquals(expectedReidIds, reidPage.getContent().stream().map(p -> p.getReid()).collect(Collectors.toList()).toString());
  //   assertEquals(expectsNextPage, reidPage.hasNext());
  //   return reidPage.nextPageable();
  // }

    private void assertEqualsReid(ReidEntity expectedEntity, ReidEntity actualEntity) {
    assertEquals(expectedEntity.getId(), actualEntity.getId());
    assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(expectedEntity.getReid(), actualEntity.getReid());
    assertEquals(expectedEntity.getSourceId(), actualEntity.getSourceId());
    assertEquals(expectedEntity.getPlateNumber(), actualEntity.getPlateNumber());
    assertEquals(expectedEntity.getLocation(), actualEntity.getLocation());
    assertEquals(expectedEntity.getStatus(), actualEntity.getStatus());
  }

  private boolean areReidsEqual(ReidEntity expectedEntity, ReidEntity actualEntity) {
    return expectedEntity.getReid().equals(actualEntity.getReid()) &&
           expectedEntity.getSourceId().equals(actualEntity.getSourceId()) &&
           expectedEntity.getPlateNumber().equals(actualEntity.getPlateNumber()) &&
           expectedEntity.getLocation().equals(actualEntity.getLocation()) &&
           expectedEntity.getStatus().equals(actualEntity.getStatus());
  }
}


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


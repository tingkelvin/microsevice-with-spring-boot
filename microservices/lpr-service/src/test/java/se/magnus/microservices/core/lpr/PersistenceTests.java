package se.magnus.microservices.core.lpr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.transaction.annotation.Propagation.NOT_SUPPORTED;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import se.magnus.microservices.core.lpr.persistence.LicencePlateEntity;
import se.magnus.microservices.core.lpr.persistence.LicencePlateEntity.BoundingBoxEmbeddable;
import se.magnus.microservices.core.lpr.persistence.LicencePlateRepository;

@DataJpaTest
@Transactional(propagation = NOT_SUPPORTED)
class PersistenceTests {

  @Autowired
  private LicencePlateRepository repository;

  private LicencePlateEntity savedEntity;

  @BeforeEach
  void setupDb() {
    repository.deleteAll();

    BoundingBoxEmbeddable vehicleBbox = new BoundingBoxEmbeddable(100, 100, 200, 150);
    BoundingBoxEmbeddable plateBbox = new BoundingBoxEmbeddable(120, 130, 80, 30);

    LicencePlateEntity entity = new LicencePlateEntity(
      null,
      "camera-001",
      "uuid-001",
      vehicleBbox,
      plateBbox,
      "ABC123",
      "2025-11-11T10:30:00Z",
      1699614600000L,
      0
    );

    savedEntity = repository.save(entity);
    assertEqualsLicencePlate(entity, savedEntity);
  }

  @Test
  void create() {
    BoundingBoxEmbeddable vehicleBbox = new BoundingBoxEmbeddable(150, 150, 250, 200);
    BoundingBoxEmbeddable plateBbox = new BoundingBoxEmbeddable(170, 180, 90, 40);

    LicencePlateEntity newEntity = new LicencePlateEntity(
      null,
      "camera-002",
      "uuid-002",
      vehicleBbox,
      plateBbox,
      "XYZ789",
      "2025-11-11T11:00:00Z",
      1699618200000L,
      0
    );

    repository.save(newEntity);

    LicencePlateEntity foundEntity = repository.findById(newEntity.getId()).get();
    assertEqualsLicencePlate(newEntity, foundEntity);

    assertEquals(2, repository.count());
  }

  @Test
  void getBySourceId() {
    List<LicencePlateEntity> entityList = repository.findBySourceId(savedEntity.getSourceId());

    assertThat(entityList, hasSize(1));
    assertEqualsLicencePlate(savedEntity, entityList.get(0));
  }

  @Test
  void getByObjectUuid() {
    LicencePlateEntity foundEntity = repository.findByObjectUuid(savedEntity.getObjectUuid()).get();
    assertEqualsLicencePlate(savedEntity, foundEntity);
  }

  @Test
  void getByPlateNum() {
    List<LicencePlateEntity> entityList = repository.findByPlateNum(savedEntity.getPlateNum());

    assertThat(entityList, hasSize(1));
    assertEqualsLicencePlate(savedEntity, entityList.get(0));
  }

  @Test
  void duplicateError() {
    assertThrows(DataIntegrityViolationException.class, () -> {
      BoundingBoxEmbeddable vehicleBbox = new BoundingBoxEmbeddable(100, 100, 200, 150);
      BoundingBoxEmbeddable plateBbox = new BoundingBoxEmbeddable(120, 130, 80, 30);

      // Same objectUuid as savedEntity
      LicencePlateEntity entity = new LicencePlateEntity(
        null,
        "camera-001",
        "uuid-001",
        vehicleBbox,
        plateBbox,
        "ABC123",
        "2025-11-11T10:30:00Z",
        1699614600000L,
        0
      );
      repository.save(entity);
    });
  }

  @Test
  void optimisticLockError() {
    // Store the saved entity in two separate entity objects
    LicencePlateEntity entity1 = repository.findById(savedEntity.getId()).get();
    LicencePlateEntity entity2 = repository.findById(savedEntity.getId()).get();

    // Update the entity using the first entity object
    entity1.setPlateNum("GHI789");
    repository.save(entity1);

    // Update the entity using the second entity object.
    // This should fail since the second entity now holds an old version number
    assertThrows(OptimisticLockingFailureException.class, () -> {
      entity2.setPlateNum("JKL012");
      repository.save(entity2);
    });

    // Get the updated entity from the database and verify its new state
    LicencePlateEntity updatedEntity = repository.findById(savedEntity.getId()).get();
    assertEquals(1, (int)updatedEntity.getVersion());
    assertEquals("GHI789", updatedEntity.getPlateNum());
  }

  @Test
  void testBoundingBoxes() {
    BoundingBoxEmbeddable newVehicleBbox = new BoundingBoxEmbeddable(200, 200, 300, 250);
    BoundingBoxEmbeddable newPlateBbox = new BoundingBoxEmbeddable(220, 230, 100, 50);

    savedEntity.setVehicleBbox(newVehicleBbox);
    savedEntity.setPlateBbox(newPlateBbox);
    repository.save(savedEntity);

    LicencePlateEntity foundEntity = repository.findById(savedEntity.getId()).get();
    
    // Verify vehicle bounding box
    assertNotNull(foundEntity.getVehicleBbox());
    assertEquals(200, foundEntity.getVehicleBbox().getX());
    assertEquals(200, foundEntity.getVehicleBbox().getY());
    assertEquals(300, foundEntity.getVehicleBbox().getWidth());
    assertEquals(250, foundEntity.getVehicleBbox().getHeight());

    // Verify plate bounding box
    assertNotNull(foundEntity.getPlateBbox());
    assertEquals(220, foundEntity.getPlateBbox().getX());
    assertEquals(230, foundEntity.getPlateBbox().getY());
    assertEquals(100, foundEntity.getPlateBbox().getWidth());
    assertEquals(50, foundEntity.getPlateBbox().getHeight());
  }

  @Test
  void testNullBoundingBoxes() {
    savedEntity.setVehicleBbox(null);
    savedEntity.setPlateBbox(null);
    repository.save(savedEntity);

    LicencePlateEntity foundEntity = repository.findById(savedEntity.getId()).get();
    assertNull(foundEntity.getVehicleBbox());
    assertNull(foundEntity.getPlateBbox());
  }

  @Test
  void findByUnixTimeBetween() {
    // Add more entities with different timestamps
    BoundingBoxEmbeddable vehicleBbox = new BoundingBoxEmbeddable(100, 100, 200, 150);
    BoundingBoxEmbeddable plateBbox = new BoundingBoxEmbeddable(120, 130, 80, 30);

    LicencePlateEntity entity2 = new LicencePlateEntity(
      null, "camera-001", "uuid-002", vehicleBbox, plateBbox,
      "DEF456", "2025-11-11T12:00:00Z", 1699621800000L, 0
    );
    repository.save(entity2);

    LicencePlateEntity entity3 = new LicencePlateEntity(
      null, "camera-001", "uuid-003", vehicleBbox, plateBbox,
      "GHI789", "2025-11-11T13:00:00Z", 1699625400000L, 0
    );
    repository.save(entity3);

    // Find entities between first and third timestamp
    List<LicencePlateEntity> entities = repository.findByUnixTimeBetween(
      1699614600000L, 1699625400000L
    );

    assertEquals(3, entities.size());
  }

  @Test
  void existsByObjectUuid() {
    assertTrue(repository.existsByObjectUuid("uuid-001"));
    assertFalse(repository.existsByObjectUuid("uuid-999"));
  }

  private void assertEqualsLicencePlate(LicencePlateEntity expectedEntity, LicencePlateEntity actualEntity) {
    assertEquals(expectedEntity.getId(), actualEntity.getId());
    assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
    assertEquals(expectedEntity.getSourceId(), actualEntity.getSourceId());
    assertEquals(expectedEntity.getObjectUuid(), actualEntity.getObjectUuid());
    assertEquals(expectedEntity.getPlateNum(), actualEntity.getPlateNum());
    assertEquals(expectedEntity.getNtpTime(), actualEntity.getNtpTime());
    assertEquals(expectedEntity.getUnixTime(), actualEntity.getUnixTime());
    
    // Compare bounding boxes
    if (expectedEntity.getVehicleBbox() != null) {
      assertNotNull(actualEntity.getVehicleBbox());
      assertEquals(expectedEntity.getVehicleBbox().getX(), actualEntity.getVehicleBbox().getX());
      assertEquals(expectedEntity.getVehicleBbox().getY(), actualEntity.getVehicleBbox().getY());
      assertEquals(expectedEntity.getVehicleBbox().getWidth(), actualEntity.getVehicleBbox().getWidth());
      assertEquals(expectedEntity.getVehicleBbox().getHeight(), actualEntity.getVehicleBbox().getHeight());
    }
    
    if (expectedEntity.getPlateBbox() != null) {
      assertNotNull(actualEntity.getPlateBbox());
      assertEquals(expectedEntity.getPlateBbox().getX(), actualEntity.getPlateBbox().getX());
      assertEquals(expectedEntity.getPlateBbox().getY(), actualEntity.getPlateBbox().getY());
      assertEquals(expectedEntity.getPlateBbox().getWidth(), actualEntity.getPlateBbox().getWidth());
      assertEquals(expectedEntity.getPlateBbox().getHeight(), actualEntity.getPlateBbox().getHeight());
    }
  }
}


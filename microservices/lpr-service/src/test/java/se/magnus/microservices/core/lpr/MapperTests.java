package se.magnus.microservices.core.lpr;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.lpr.BoundingBox;
import se.magnus.api.core.lpr.LicencePlate;
import se.magnus.microservices.core.lpr.persistence.LicencePlateEntity;
import se.magnus.microservices.core.lpr.services.LprMapper;

class MapperTests {

  private LprMapper mapper = Mappers.getMapper(LprMapper.class);

  @Test
  void mapperTests() {
    assertNotNull(mapper);

    // Create API object with BoundingBoxes
    BoundingBox vehicleBbox = new BoundingBox(100, 100, 200, 150);
    BoundingBox plateBbox = new BoundingBox(120, 130, 80, 30);
    
    LicencePlate api = new LicencePlate(
      "camera-001",
      "uuid-001",
      vehicleBbox,
      plateBbox,
      "ABC123",
      "2025-11-11T10:30:00Z",
      1699614600000L,
      null  // serviceAddress - not set in mapper test
    );

    // Convert API to Entity
    LicencePlateEntity entity = mapper.apiToEntity(api);

    // Verify all fields are mapped correctly
    assertEquals(api.getSourceId(), entity.getSourceId());
    assertEquals(api.getObjectUuid(), entity.getObjectUuid());
    assertEquals(api.getPlateNum(), entity.getPlateNum());
    assertEquals(api.getNtpTime(), entity.getNtpTime());
    assertEquals(api.getUnixTime(), entity.getUnixTime());

    // Verify vehicle bounding box
    assertNotNull(entity.getVehicleBbox());
    assertEquals(api.getVehicleBbox().getX(), entity.getVehicleBbox().getX());
    assertEquals(api.getVehicleBbox().getY(), entity.getVehicleBbox().getY());
    assertEquals(api.getVehicleBbox().getWidth(), entity.getVehicleBbox().getWidth());
    assertEquals(api.getVehicleBbox().getHeight(), entity.getVehicleBbox().getHeight());

    // Verify plate bounding box
    assertNotNull(entity.getPlateBbox());
    assertEquals(api.getPlateBbox().getX(), entity.getPlateBbox().getX());
    assertEquals(api.getPlateBbox().getY(), entity.getPlateBbox().getY());
    assertEquals(api.getPlateBbox().getWidth(), entity.getPlateBbox().getWidth());
    assertEquals(api.getPlateBbox().getHeight(), entity.getPlateBbox().getHeight());

    // Convert Entity back to API
    LicencePlate api2 = mapper.entityToApi(entity);

    // Verify all fields match the original
    assertEquals(api.getSourceId(), api2.getSourceId());
    assertEquals(api.getObjectUuid(), api2.getObjectUuid());
    assertEquals(api.getPlateNum(), api2.getPlateNum());
    assertEquals(api.getNtpTime(), api2.getNtpTime());
    assertEquals(api.getUnixTime(), api2.getUnixTime());

    // Verify vehicle bounding box
    assertNotNull(api2.getVehicleBbox());
    assertEquals(api.getVehicleBbox().getX(), api2.getVehicleBbox().getX());
    assertEquals(api.getVehicleBbox().getY(), api2.getVehicleBbox().getY());
    assertEquals(api.getVehicleBbox().getWidth(), api2.getVehicleBbox().getWidth());
    assertEquals(api.getVehicleBbox().getHeight(), api2.getVehicleBbox().getHeight());

    // Verify plate bounding box
    assertNotNull(api2.getPlateBbox());
    assertEquals(api.getPlateBbox().getX(), api2.getPlateBbox().getX());
    assertEquals(api.getPlateBbox().getY(), api2.getPlateBbox().getY());
    assertEquals(api.getPlateBbox().getWidth(), api2.getPlateBbox().getWidth());
    assertEquals(api.getPlateBbox().getHeight(), api2.getPlateBbox().getHeight());
  }

  @Test
  void mapperNullBoundingBoxTests() {
    assertNotNull(mapper);

    // Create API object with null BoundingBoxes
    LicencePlate api = new LicencePlate(
      "camera-002",
      "uuid-002",
      null,
      null,
      "XYZ789",
      "2025-11-11T11:00:00Z",
      1699618200000L,
      null  // serviceAddress - not set in mapper test
    );

    // Convert API to Entity
    LicencePlateEntity entity = mapper.apiToEntity(api);

    // Verify null bounding boxes are handled correctly
    assertNull(entity.getVehicleBbox());
    assertNull(entity.getPlateBbox());

    // Convert back to API
    LicencePlate api2 = mapper.entityToApi(entity);

    // Verify nulls are preserved
    assertNull(api2.getVehicleBbox());
    assertNull(api2.getPlateBbox());
    assertEquals(api.getSourceId(), api2.getSourceId());
    assertEquals(api.getObjectUuid(), api2.getObjectUuid());
    assertEquals(api.getPlateNum(), api2.getPlateNum());
  }
}


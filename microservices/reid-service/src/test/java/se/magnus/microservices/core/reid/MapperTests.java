package se.magnus.microservices.core.reid;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import se.magnus.api.core.reid.Reid;
import se.magnus.microservices.core.reid.persistence.ReidEntity;
import se.magnus.microservices.core.reid.services.ReidMapper;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {

  private ReidMapper mapper = Mappers.getMapper(ReidMapper.class);

  @Test
  void mapperTests() {

    assertNotNull(mapper);

    Reid api = new Reid("reid-001", "camera-001", "ABC123", 1699614600000L, "Location-1", "active", "service-address");

    ReidEntity entity = mapper.apiToEntity(api);

    assertEquals(api.getReid(), entity.getReid());
    assertEquals(api.getSourceId(), entity.getSourceId());
    assertEquals(api.getPlateNumber(), entity.getPlateNumber());
    assertEquals(api.getTimestamp(), entity.getTimestamp());
    assertEquals(api.getLocation(), entity.getLocation());
    assertEquals(api.getStatus(), entity.getStatus());

    Reid api2 = mapper.entityToApi(entity);

    assertEquals(api.getReid(), api2.getReid());
    assertEquals(api.getSourceId(), api2.getSourceId());
    assertEquals(api.getPlateNumber(), api2.getPlateNumber());
    assertEquals(api.getTimestamp(), api2.getTimestamp());
    assertEquals(api.getLocation(), api2.getLocation());
    assertEquals(api.getStatus(), api2.getStatus());
    assertNull(api2.getServiceAddress());
  }
}


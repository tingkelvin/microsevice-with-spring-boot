package se.magnus.microservices.core.lpr.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.magnus.api.core.lpr.BoundingBox;
import se.magnus.api.core.lpr.LicencePlate;
import se.magnus.microservices.core.lpr.persistence.LicencePlateEntity;
import se.magnus.microservices.core.lpr.persistence.LicencePlateEntity.BoundingBoxEmbeddable;

@Mapper(componentModel = "spring")
public interface LprMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  LicencePlateEntity apiToEntity(LicencePlate api);

  @Mapping(target = "serviceAddress", ignore = true)
  LicencePlate entityToApi(LicencePlateEntity entity);

  // Helper methods for BoundingBox conversion
  default BoundingBoxEmbeddable map(BoundingBox boundingBox) {
    if (boundingBox == null) {
      return null;
    }
    return new BoundingBoxEmbeddable(
      boundingBox.getX(),
      boundingBox.getY(),
      boundingBox.getWidth(),
      boundingBox.getHeight()
    );
  }

  default BoundingBox map(BoundingBoxEmbeddable embeddable) {
    if (embeddable == null) {
      return null;
    }
    return new BoundingBox(
      embeddable.getX(),
      embeddable.getY(),
      embeddable.getWidth(),
      embeddable.getHeight()
    );
  }
}


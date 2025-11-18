package se.magnus.microservices.core.reid.services;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import se.magnus.api.core.reid.Reid;
import se.magnus.microservices.core.reid.persistence.ReidEntity;

@Mapper(componentModel = "spring")
public interface ReidMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "version", ignore = true)
  ReidEntity apiToEntity(Reid api);

  @Mapping(target = "serviceAddress", ignore = true)
  Reid entityToApi(ReidEntity entity);
}


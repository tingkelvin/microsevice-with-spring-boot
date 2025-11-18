package se.magnus.microservices.core.reid.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.reid.Reid;
import se.magnus.api.core.reid.ReidService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.microservices.core.reid.persistence.ReidEntity;
import se.magnus.microservices.core.reid.persistence.ReidRepository;
import se.magnus.util.http.ServiceUtil;

@RestController
public class ReidServiceImpl implements ReidService {

  private static final Logger LOG = LoggerFactory.getLogger(ReidServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final ReidRepository repository;
  private final ReidMapper mapper;

  @Autowired
  public ReidServiceImpl(ServiceUtil serviceUtil, ReidRepository repository, ReidMapper mapper) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public List<Reid> getReids(String sourceId) {
    LOG.debug("/reid return the detections data for sourceId={}", sourceId);

    if (sourceId == null || sourceId.isEmpty()) {
      throw new InvalidInputException("Invalid sourceId: " + sourceId);
    }

    List<ReidEntity> entityList = repository.findBySourceId(sourceId);
    List<Reid> list = entityList.stream()
      .map(e -> {
        Reid api = mapper.entityToApi(e);
        api.setServiceAddress(serviceUtil.getServiceAddress());
        return api;
      })
      .toList();

    LOG.debug("getDetections: response size: {}", list.size());

    return list;
  }

  @Override
  public Reid createReid(Reid body) {
    if (body.getSourceId() == null || body.getSourceId().isEmpty()) {
      throw new InvalidInputException("Invalid sourceId: " + body.getSourceId());
    }

    try {
      ReidEntity entity = mapper.apiToEntity(body);
      ReidEntity newEntity = repository.save(entity);

      LOG.debug("createReid: created reid entity: {}/{}", body.getSourceId(), body.getReid());

      Reid api = mapper.entityToApi(newEntity);
      api.setServiceAddress(serviceUtil.getServiceAddress());
      return api;

    } catch (DuplicateKeyException dke) {
      throw new InvalidInputException("Duplicate key, Source Id: " + body.getSourceId() + ", Reid Id: " + body.getReid());
    }
  }

  @Override
  public void deleteReids(String sourceId) {
    if (sourceId == null || sourceId.isEmpty()) {
      throw new InvalidInputException("Invalid sourceId: " + sourceId);
    }

    LOG.debug("deleteReids: tries to delete reids for sourceId: {}", sourceId);
    
    List<ReidEntity> entities = repository.findBySourceId(sourceId);
    repository.deleteAll(entities);
  }
}


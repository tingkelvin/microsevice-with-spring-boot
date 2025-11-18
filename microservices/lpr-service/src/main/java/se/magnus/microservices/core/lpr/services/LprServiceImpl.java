package se.magnus.microservices.core.lpr.services;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.core.lpr.LicencePlate;
import se.magnus.api.core.lpr.LprService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.microservices.core.lpr.persistence.LicencePlateEntity;
import se.magnus.microservices.core.lpr.persistence.LicencePlateRepository;
import se.magnus.util.http.ServiceUtil;

@RestController
public class LprServiceImpl implements LprService {

  private static final Logger LOG = LoggerFactory.getLogger(LprServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final LicencePlateRepository repository;
  private final LprMapper mapper;

  @Autowired
  public LprServiceImpl(ServiceUtil serviceUtil, LicencePlateRepository repository, LprMapper mapper) {
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public void createLpr(LicencePlate body) {
    try {
      LicencePlateEntity entity = mapper.apiToEntity(body);
      LicencePlateEntity newEntity = repository.save(entity);

      LOG.debug("createLpr: entity created for objectUuid: {}", body.getObjectUuid());
    } catch (DataIntegrityViolationException dive) {
      throw new InvalidInputException("Duplicate key, Object UUID: " + body.getObjectUuid());
    }
  }

  @Override
  public List<LicencePlate> getLprs(String sourceId) {
    LOG.debug("/lpr/detections return the found LPR detections for sourceId={}", sourceId);

    if (sourceId == null || sourceId.isEmpty()) {
      throw new InvalidInputException("Invalid sourceId: " + sourceId);
    }

    List<LicencePlateEntity> entities = repository.findBySourceId(sourceId);
    
    List<LicencePlate> plates = entities.stream()
      .map(mapper::entityToApi)
      .collect(Collectors.toList());

    LOG.debug("Found {} detections for sourceId: {}", plates.size(), sourceId);
    
    return plates;
  }
}


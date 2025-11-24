package se.magnus.microservices.core.lpr.services;

import static java.util.logging.Level.FINE;

import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.lpr.LicencePlate;
import se.magnus.api.core.lpr.LprService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.microservices.core.lpr.persistence.LicencePlateEntity;
import se.magnus.microservices.core.lpr.persistence.LicencePlateRepository;
import se.magnus.util.http.ServiceUtil;

@RestController
public class LprServiceImpl implements LprService {

  private static final Logger LOG = LoggerFactory.getLogger(LprServiceImpl.class);

  private final Scheduler jdbcScheduler;
  private final ServiceUtil serviceUtil;
  private final LicencePlateRepository repository;
  private final LprMapper mapper;

  @Autowired
  public LprServiceImpl(@Qualifier("jdbcScheduler") Scheduler jdbcScheduler, ServiceUtil serviceUtil, LicencePlateRepository repository, LprMapper mapper) {
    this.jdbcScheduler = jdbcScheduler;
    this.serviceUtil = serviceUtil;
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Mono<LicencePlate> createLpr(LicencePlate body) {
    return Mono.fromCallable(() -> internalCreateLpr(body))
      .subscribeOn(jdbcScheduler);
  }

  @Override
  public Flux<LicencePlate> getLprs(String sourceId) {
    LOG.debug("/lpr/detections return the found LPR detections for sourceId={}", sourceId);

    if (sourceId == null || sourceId.isEmpty()) {
      throw new InvalidInputException("Invalid sourceId: " + sourceId);
    }

    return Mono.fromCallable(() -> internalGetLprs(sourceId))
      .flatMapMany(Flux::fromIterable)
      .log(LOG.getName(), FINE)
      .subscribeOn(jdbcScheduler);
  }

  private LicencePlate internalCreateLpr(LicencePlate body) {
    try {
      LicencePlateEntity entity = mapper.apiToEntity(body);
      LicencePlateEntity newEntity = repository.save(entity);

      LOG.debug("createLpr: created a lpr entity: {}/{}", body.getSourceId(), body.getObjectUuid());
      return mapper.entityToApi(newEntity);

    } catch (DataIntegrityViolationException dive) {
      throw new InvalidInputException("Duplicate key, Source Id: " + body.getSourceId() + ", Object UUID:" + body.getObjectUuid());
    }
  }

  private List<LicencePlate> internalGetLprs(String sourceId) {

    List<LicencePlateEntity> entities = repository.findBySourceId(sourceId);
    List<LicencePlate> plates = entities.stream()
      .map(mapper::entityToApi)
      .collect(Collectors.toList());
    plates.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

    LOG.debug("Found {} detections for sourceId: {}", plates.size(), sourceId);
    
    return plates;
  }
}


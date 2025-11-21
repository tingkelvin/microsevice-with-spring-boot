package se.magnus.microservices.core.reid.services;

import static java.util.logging.Level.FINE;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

import se.magnus.api.exceptions.NotFoundException;

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
  public Flux<Reid> getReids(String sourceId) {
    LOG.debug("/reid return the detections data for sourceId={}", sourceId);

    if (sourceId == null || sourceId.isEmpty()) {
      throw new InvalidInputException("Invalid sourceId: " + sourceId);
    }

    return repository.findBySourceId(sourceId)
      .switchIfEmpty(Mono.error(new NotFoundException("No reid found for sourceId: " + sourceId)))
      .log(LOG.getName(), FINE)
      .map(e -> mapper.entityToApi(e))
      .map(e -> setServiceAddress(e));
  }

  @Override
  public Mono<Reid> createReid(Reid body) {

    if (body.getSourceId() == null || body.getSourceId().isEmpty()) {
      throw new InvalidInputException("Invalid sourceId: " + body.getSourceId());
    }

    ReidEntity entity = mapper.apiToEntity(body);

    Mono<Reid> newEntity = repository.save(entity)
      .log(LOG.getName(), FINE)
      .onErrorMap(
        DuplicateKeyException.class,
        ex -> new InvalidInputException("Duplicate key, Source Id: " + body.getSourceId() + ", Reid Id: " + body.getReid()))
      .map(e -> mapper.entityToApi(e));

    return newEntity;
  }

  private Reid setServiceAddress(Reid entity) {
    entity.setServiceAddress(serviceUtil.getServiceAddress());
    return entity;
  }
}


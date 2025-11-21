package se.magnus.microservices.core.reid.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ReidRepository extends ReactiveCrudRepository<ReidEntity, String> {

  Flux<ReidEntity> findBySourceId(String sourceId);
}


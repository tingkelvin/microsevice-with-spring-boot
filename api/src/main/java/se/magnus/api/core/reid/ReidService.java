package se.magnus.api.core.reid;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface ReidService {

  /**
   * Sample usage: "curl $HOST:$PORT/reid/camera-001".
   *
   * @param sourceId Id of the source
   * @return the reid data, if found, else empty
   */
  @GetMapping(
    value = "/reid/{sourceId}",
    produces = "application/json")
  Flux<Reid> getReids(@PathVariable String sourceId);

  /**
   * Sample usage: "curl -X POST $HOST:$PORT/reid".
   *
   * @param body Reid object
   * @return the created reid
   */
  @PostMapping(
    value = "/reid",
    consumes = "application/json",
    produces = "application/json")
  Mono<Reid> createReid(Reid body);
}


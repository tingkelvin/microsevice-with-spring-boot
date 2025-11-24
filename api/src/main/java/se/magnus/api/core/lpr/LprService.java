package se.magnus.api.core.lpr;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface LprService {

  /**
   * Sample usage: "curl $HOST:$PORT/lpr/detections/camera-001".
   *
   * @param sourceId Id of the camera/source
   * @return the LPR detections, if found, else empty
   */
  @GetMapping(
    value = "/lpr/detections/{sourceId}",
    produces = "application/json")
  Flux<LicencePlate> getLprs(@PathVariable String sourceId);

  /**
   * Sample usage, see below.
   *
   * curl -X POST $HOST:$PORT/lpr/detection \
   *   -H "Content-Type: application/json" --data \
   *   '{"objectUuid":"uuid-001","plateNum":"ABC123",...}'
   *
   * @param body A JSON representation of the new licence plate detection
   * @return the created licence plate
   */
  @PostMapping(
    value    = "/lpr/detection",
    consumes = "application/json",
    produces = "application/json")
  Mono<LicencePlate> createLpr(@RequestBody LicencePlate body);
}


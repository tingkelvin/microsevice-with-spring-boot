package se.magnus.api.core.journey;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

public interface JourneyService {
    Mono<Journey> createJourney(Journey journey);

  /**
   * Sample usage: "curl $HOST:$PORT/journey/reid-001".
   *
   * @param reid Id of the journey/reid
   * @return the journey data, if found, else null
   */
  @GetMapping(
    value = "/journey/{reid}",
    produces = "application/json")
    Mono<Journey> getJourney(@PathVariable String reid);

  Mono<Void> deleteJourney(String reid);
}


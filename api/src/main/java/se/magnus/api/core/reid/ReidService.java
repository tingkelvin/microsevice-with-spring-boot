package se.magnus.api.core.reid;

import java.util.List;
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
   * @return the reid data, if found, else null
   */
  @GetMapping(
    value = "/reid/{sourceId}",
    produces = "application/json")
  List<Reid> getReids(@PathVariable String sourceId);

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
  Reid createReid(@RequestBody Reid body);

  /**
   * Sample usage: "curl -X DELETE $HOST:$PORT/reid/camera-001".
   *
   * @param sourceId Id of the source
   */
  @DeleteMapping(value = "/reid/{sourceId}")
  void deleteReids(@PathVariable String sourceId);
}


package se.magnus.microservices.composite.detection.services;

import static java.util.logging.Level.FINE;
import static reactor.core.publisher.Flux.empty;
import static se.magnus.api.event.Event.Type.CREATE;
import static se.magnus.api.event.Event.Type.DELETE;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import se.magnus.api.core.product.Product;
import se.magnus.api.core.product.ProductService;
import se.magnus.api.core.recommendation.Recommendation;
import se.magnus.api.core.recommendation.RecommendationService;
import se.magnus.api.core.review.Review;
import se.magnus.api.core.review.ReviewService;
import se.magnus.api.event.Event;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;


@Component
public class DetectionCompositeIntegration implements LprService, ReidService {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionCompositeIntegration.class);

  private final WebClient webClient;
  ObjectMapper mapper;

  private final String lprDetectionsUrl;
  private final String lprDetectionUrl;
  private final String reidServiceUrl;

  private final StreamBridge streamBridge;

  private final Scheduler publishEventScheduler;

  @Autowired
  public DetectionCompositeIntegration(
      WebClient.Builder webClient,
      ObjectMapper objectMapper,
      StreamBridge streamBridge,
      @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
      @Value("${app.lpr-service.host}") String lprServiceHost,
      @Value("${app.lpr-service.port}") int lprServicePort,
      @Value("${app.reid-service.host}") String reidServiceHost,
      @Value("${app.reid-service.port}") int reidServicePort
    ) {

    this.webClient = webClient.build();
    this.streamBridge = streamBridge;
    this.publishEventScheduler = publishEventScheduler;
    this.mapper = objectMapper;

    lprDetectionsUrl = "http://" + lprServiceHost + ":" + lprServicePort + "/lpr/detections/";
    lprDetectionUrl = "http://" + lprServiceHost + ":" + lprServicePort + "/lpr/detection";
    reidServiceUrl = "http://" + reidServiceHost + ":" + reidServicePort + "/reid/";
  }

  @Override
  public Flux<LicencePlate> getLprs(String sourceId) {
    String url = lprDetectionsUrl + sourceId;
    LOG.debug("Will call LPR getLprs API on URL: {}", url);
    
    return webClient.get().uri(url)
      .retrieve().bodyToFlux(LicencePlate.class)
      .log(LOG.getName(), Level.FINE)
      .onErrorMap(WebClientException.class, ex -> handleWebClientException(ex));
  }

  @Override
  public Mono<LicencePlate> createLpr(LicencePlate body) {
    LOG.debug("Will call LPR createLpr API on URL: {}", lprDetectionUrl);
    
    return Mono.fromCallable(() -> {
      sendMessage("lpr-out-0", new Event(CREATE, body.getByObjectUuid(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  @Override
  public Flux<Reid> getReids(String sourceId) {
    String url = reidServiceUrl + sourceId;
    LOG.debug("Will call Reid getReids API on URL: {}", url);
    
    return webClient.get().uri(url)
      .retrieve().bodyToFlux(Reid.class)
      .log(LOG.getName(), Level.FINE)
      .onErrorMap(WebClientException.class, ex -> handleWebClientException(ex));
  }

  @Override
  public Mono<Reid> createReid(Reid body) {
    return Mono.fromCallable(() -> {
      sendMessage("reid-out-0", new Event(CREATE, body.getByObjectUuid(), body));
      return body;
    }).subscribeOn(publishEventScheduler);
  }

  private RuntimeException handleWebClientException(WebClientException ex) {
    LOG.warn("Got a WebClient error: {}, will rethrow it", ex.getMessage());
    if (ex.getMessage() != null && ex.getMessage().contains("404")) {
      return new NotFoundException(ex.getMessage());
    }
    if (ex.getMessage() != null && ex.getMessage().contains("422")) {
      return new InvalidInputException(ex.getMessage());
    }
    return ex;
  }
}


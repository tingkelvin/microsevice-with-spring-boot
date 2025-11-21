package se.magnus.microservices.composite.detection.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.magnus.api.core.lpr.LicencePlate;
import se.magnus.api.core.lpr.LprService;
import se.magnus.api.core.reid.Reid;
import se.magnus.api.core.reid.ReidService;
import se.magnus.api.exceptions.InvalidInputException;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.HttpErrorInfo;

@Component
public class DetectionCompositeIntegration implements LprService, ReidService {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionCompositeIntegration.class);

  private final WebClient webClient;
  private final ObjectMapper mapper;

  private final String lprDetectionsUrl;
  private final String lprDetectionUrl;
  private final String reidServiceUrl;

  @Autowired
  public DetectionCompositeIntegration(
    WebClient.Builder webClient,
    ObjectMapper mapper,
      @Value("${app.lpr-service.host}") String lprServiceHost,
      @Value("${app.lpr-service.port}") int lprServicePort,
      @Value("${app.reid-service.host}") String reidServiceHost,
      @Value("${app.reid-service.port}") int reidServicePort
    ) {

    this.webClient = webClient.build();
    this.mapper = mapper;

    lprDetectionsUrl = "http://" + lprServiceHost + ":" + lprServicePort + "/lpr/detections/";
    lprDetectionUrl = "http://" + lprServiceHost + ":" + lprServicePort + "/lpr/detection";
    reidServiceUrl = "http://" + reidServiceHost + ":" + reidServicePort + "/reid/";
  }

  @Override
  public List<LicencePlate> getLprs(String sourceId) {
    try {
      String url = lprDetectionsUrl + sourceId;
      LOG.debug("Will call LPR getLprs API on URL: {}", url);
      
      List<LicencePlate> detections = restTemplate.exchange(
        url,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<List<LicencePlate>>() {}
      ).getBody();

      if (detections == null) {
        detections = List.of();
      }

      LOG.debug("Found {} LPR detections for sourceId {}", detections.size(), sourceId);
      return detections;

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public void createLpr(LicencePlate body) {
    try {
      LOG.debug("Will call LPR createLpr API on URL: {}", lprDetectionUrl);
      
      restTemplate.postForObject(lprDetectionUrl, body, LicencePlate.class);
      
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
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
  public Mono<Reid> createReid(Mono<Reid> body) {
    String url = reidServiceUrl;
    LOG.debug("Will call Reid createReid API on URL: {}", url);
    
    return body.flatMap(reid -> 
      webClient.post().uri(url)
        .bodyValue(reid)
        .retrieve()
        .bodyToMono(Reid.class)
        .onErrorMap(WebClientException.class, ex -> handleWebClientException(ex))
    );
  }

  @Override
  public Mono<Void> deleteReids(String sourceId) {
    String url = reidServiceUrl + sourceId;
    LOG.debug("Will call Reid deleteReids API on URL: {}", url);
    
    return webClient.delete().uri(url)
      .retrieve()
      .bodyToMono(Void.class)
      .onErrorMap(WebClientException.class, ex -> handleWebClientException(ex))
      .then();
  }

  private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
      switch (HttpStatus.resolve(ex.getStatusCode().value())) {
        case NOT_FOUND:
        return new NotFoundException(getErrorMessage(ex));

        case UNPROCESSABLE_ENTITY:
        return new InvalidInputException(getErrorMessage(ex));

        default:
          LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
          LOG.warn("Error body: {}", ex.getResponseBodyAsString());
        return ex;
      }
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

  private String getErrorMessage(HttpClientErrorException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }
}


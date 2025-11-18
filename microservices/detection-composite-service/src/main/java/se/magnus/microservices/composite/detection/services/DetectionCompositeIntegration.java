package se.magnus.microservices.composite.detection.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
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

  private final RestTemplate restTemplate;
  private final ObjectMapper mapper;

  private final String lprDetectionsUrl;
  private final String lprDetectionUrl;
  private final String reidServiceUrl;

  @Autowired
  public DetectionCompositeIntegration(
    RestTemplate restTemplate,
    ObjectMapper mapper,
    @Value("${app.lpr-service.host}") String lprServiceHost,
    @Value("${app.lpr-service.port}") int lprServicePort,
    @Value("${app.reid-service.host}") String reidServiceHost,
    @Value("${app.reid-service.port}") int reidServicePort) {

    this.restTemplate = restTemplate;
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
  public List<Reid> getReids(String sourceId) {
    try {
      String url = reidServiceUrl + sourceId;
      LOG.debug("Will call Reid getDetections API on URL: {}", url);
      
      List<Reid> reids = restTemplate.exchange(
        url,
        HttpMethod.GET,
        null,
        new ParameterizedTypeReference<List<Reid>>() {}
      ).getBody();
      
      LOG.debug("Found {} Reid detections for sourceId: {}", reids != null ? reids.size() : 0, sourceId);
      
      return reids;

    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public Reid createReid(Reid body) {
    try {
      String url = reidServiceUrl;
      LOG.debug("Will call Reid createReid API on URL: {}", url);
      
      return restTemplate.postForObject(url, body, Reid.class);
      
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
  }

  @Override
  public void deleteReids(String sourceId) {
    try {
      String url = reidServiceUrl + sourceId;
      LOG.debug("Will call Reid deleteReids API on URL: {}", url);
      
      restTemplate.delete(url);
      
    } catch (HttpClientErrorException ex) {
      throw handleHttpClientException(ex);
    }
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

  private String getErrorMessage(HttpClientErrorException ex) {
    try {
      return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
    } catch (IOException ioex) {
      return ex.getMessage();
    }
  }
}


package se.magnus.microservices.composite.detection.services;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import se.magnus.api.composite.detection.DetectionAggregate;
import se.magnus.api.composite.detection.DetectionCompositeService;
import se.magnus.api.core.lpr.LicencePlate;
import se.magnus.api.core.reid.Reid;
import se.magnus.api.exceptions.NotFoundException;
import se.magnus.util.http.ServiceUtil;

@RestController
public class DetectionCompositeServiceImpl implements DetectionCompositeService {

  private static final Logger LOG = LoggerFactory.getLogger(DetectionCompositeServiceImpl.class);

  private final ServiceUtil serviceUtil;
  private final DetectionCompositeIntegration integration;

  @Autowired
  public DetectionCompositeServiceImpl(
    ServiceUtil serviceUtil, 
    DetectionCompositeIntegration integration) {
    
    this.serviceUtil = serviceUtil;
    this.integration = integration;
  }

  @Override
  public DetectionAggregate getDetectionAggregate(String sourceId) {
    LOG.debug("getDetectionAggregate: getting detection aggregate for sourceId={}", sourceId);

    // Get detections from LPR service
    List<LicencePlate> detections = integration.getLprs(sourceId);

    if (detections == null || detections.isEmpty()) {
      throw new NotFoundException("No detections found for sourceId: " + sourceId);
    }
    
    // Try to get reid data - optional
    List<Reid> reids = null;
    try {
      reids = integration.getReids(sourceId);
    } catch (Exception ex) {
      LOG.warn("Failed to get reid data for sourceId: {}, continuing without reid data", sourceId);
    }
    
    // Create and return the aggregate
    return new DetectionAggregate(sourceId, detections, reids, serviceUtil.getServiceAddress());
  }
}


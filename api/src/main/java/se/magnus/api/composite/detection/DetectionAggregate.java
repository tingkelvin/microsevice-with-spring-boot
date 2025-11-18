package se.magnus.api.composite.detection;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.magnus.api.core.lpr.LicencePlate;
import se.magnus.api.core.reid.Reid;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectionAggregate {
  private String sourceId;
  private List<LicencePlate> detections;
  private List<Reid> reids;
  private String serviceAddress;
}


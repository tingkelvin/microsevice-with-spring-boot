package se.magnus.api.core.lpr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Detections {
  private String sourceId;
  private List<LicencePlate> detections;
  private long unixTime;
  private String ntpTime;
}


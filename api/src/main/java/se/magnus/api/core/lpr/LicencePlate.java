package se.magnus.api.core.lpr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicencePlate {
  private String sourceId;
  private String objectUuid;
  private BoundingBox vehicleBbox;
  private BoundingBox plateBbox;
  private String plateNum;
  private String ntpTime;
  private long unixTime;
}


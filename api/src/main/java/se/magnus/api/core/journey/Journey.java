package se.magnus.api.core.journey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Journey {
  private String reid;
  private long timestamp;
  private String uuid;
  private String sourceId;
  private String zoneName;
  private String zoneId;
  private String event;
  private String hwId;
}



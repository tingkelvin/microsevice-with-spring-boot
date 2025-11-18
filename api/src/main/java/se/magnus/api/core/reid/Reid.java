package se.magnus.api.core.reid;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reid {
  private String reid;
  private String sourceId;
  private String plateNumber;
  private long timestamp;
  private String location;
  private String status;
  private String serviceAddress;
}


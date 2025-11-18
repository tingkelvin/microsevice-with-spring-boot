package se.magnus.api.composite.detection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAddresses {
  private String compositeAddress;
  private String lprAddress;
  private String reidAddress;
}


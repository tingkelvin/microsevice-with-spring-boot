package se.magnus.microservices.core.reid.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "reid")
@CompoundIndex(name = "reid_sourceId", unique = true, def = "{'reid': 1, 'sourceId': 1}")
@Data
@NoArgsConstructor
public class ReidEntity {

  @Id
  private String id;

  @Version
  private Integer version;

  private String reid;
  private String sourceId;
  private String plateNumber;
  private long timestamp;
  private String location;
  private String status;

  public ReidEntity(String reid, String sourceId, String plateNumber, long timestamp, String location, String status) {
    this.reid = reid;
    this.sourceId = sourceId;
    this.plateNumber = plateNumber;
    this.timestamp = timestamp;
    this.location = location;
    this.status = status;
  }
}


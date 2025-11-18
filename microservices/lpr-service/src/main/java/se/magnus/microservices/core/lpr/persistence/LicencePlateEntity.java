package se.magnus.microservices.core.lpr.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
  name = "licence_plates",
  indexes = {
    @Index(name = "idx_source_id", columnList = "sourceId"),
    @Index(name = "idx_object_uuid", columnList = "objectUuid", unique = true),
    @Index(name = "idx_plate_num", columnList = "plateNum"),
    @Index(name = "idx_unix_time", columnList = "unixTime")
  }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicencePlateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String sourceId;
  
  @Column(unique = true)
  private String objectUuid;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "x", column = @Column(name = "vehicle_bbox_x")),
    @AttributeOverride(name = "y", column = @Column(name = "vehicle_bbox_y")),
    @AttributeOverride(name = "width", column = @Column(name = "vehicle_bbox_width")),
    @AttributeOverride(name = "height", column = @Column(name = "vehicle_bbox_height"))
  })
  private BoundingBoxEmbeddable vehicleBbox;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "x", column = @Column(name = "plate_bbox_x")),
    @AttributeOverride(name = "y", column = @Column(name = "plate_bbox_y")),
    @AttributeOverride(name = "width", column = @Column(name = "plate_bbox_width")),
    @AttributeOverride(name = "height", column = @Column(name = "plate_bbox_height"))
  })
  private BoundingBoxEmbeddable plateBbox;

  private String plateNum;
  private String ntpTime;
  private long unixTime;

  @Version
  private int version;

  @Embeddable
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BoundingBoxEmbeddable {
    private int x;
    private int y;
    private int width;
    private int height;
  }
}


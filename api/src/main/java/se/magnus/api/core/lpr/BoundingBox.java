package se.magnus.api.core.lpr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoundingBox {
  private int x;
  private int y;
  private int width;
  private int height;
}


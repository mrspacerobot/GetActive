package com.sportsync.activity.metadata;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterMetadataField {
  private String fieldName;
  private String type;
  private Double min;
  private Double max;
}

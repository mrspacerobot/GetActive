package com.sportsync.activity.filter;

import com.sportsync.activity.Sport;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Data;

@Data
public class ActivityFilterDto {
  private Sport sportType;

  private LocalDateTime startDate;
  private LocalDateTime endDate;

  // Location fields
  private Double lat;
  private Double lon;

  @PositiveOrZero private Double radius;

  // Additional filter parameters based on filterType
  private Map<String, Object> sportSpecificFilter;
}

package com.sportsync.activity.metadata;

import com.sportsync.activity.Sport;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import java.lang.reflect.Field;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FilterMetadataService {

  private final Map<Sport, Class<?>> filterClassMap;

  public FilterMetadataService() {
    this.filterClassMap =
        Map.of(
            Sport.RUNNING, com.sportsync.activity.RunningFilter.class
            // Add more default mappings here
            );
  }

  public FilterMetadataService(Map<Sport, Class<?>> filterClassMap) {
    this.filterClassMap = filterClassMap;
  }

  @Cacheable(value = "filterMetadata", key = "#sport.name()")
  public List<FilterMetadataField> getFilterMetadata(Sport sport) {
    Class<?> filterClass = filterClassMap.get(sport);
    if (filterClass == null) {
      throw new IllegalArgumentException("No filter metadata for sport: " + sport);
    }

    return extractMetadata(filterClass);
  }

  private List<FilterMetadataField> extractMetadata(Class<?> filterClass) {
    List<FilterMetadataField> metadata = new ArrayList<>();

    for (Field field : filterClass.getDeclaredFields()) {
      String name = field.getName();
      String type = field.getType().getSimpleName();
      Double min = null;
      Double max = null;

      if (field.isAnnotationPresent(PositiveOrZero.class)) {
        min = 0.0;
      }
      if (field.isAnnotationPresent(Min.class)) {
        min = (double) field.getAnnotation(Min.class).value();
      }
      if (field.isAnnotationPresent(Max.class)) {
        max = (double) field.getAnnotation(Max.class).value();
      }

      metadata.add(new FilterMetadataField(name, type, min, max));
    }

    return metadata;
  }
}

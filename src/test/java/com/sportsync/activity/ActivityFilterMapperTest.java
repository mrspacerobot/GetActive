package com.sportsync.activity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ActivityFilterMapperTest {

  private ActivityFilterMapper mapper;

  @Mock private ObjectMapper objectMapper;

  private ActivityFilterDto filterDto;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    mapper = new ActivityFilterMapper(objectMapper);
    now = LocalDateTime.now();

    filterDto = new ActivityFilterDto();
    filterDto.setSportType(Sport.RUNNING);
    filterDto.setStartDate(now);
    filterDto.setEndDate(now.plusDays(7));
  }

  @Test
  void toActivityFilter_BasicMapping_ShouldMapAllBasicFields() {
    // Arrange
    filterDto.setSportSpecificFilter(null);

    // Act
    ActivityFilter result = mapper.toActivityFilter(filterDto);

    // Assert
    assertEquals(now, result.startDate());
    assertEquals(now.plusDays(7), result.endDate());
    assertNull(result.sportFilter());
  }

  @Test
  void toActivityFilter_WithSportFilter_ShouldMapSportFilter() {
    // Arrange
    Map<String, Object> sportFilterMap = new HashMap<>();
    sportFilterMap.put("minPace", 5.0);
    sportFilterMap.put("maxPace", 7.5);
    filterDto.setSportSpecificFilter(sportFilterMap);

    // Act
    ActivityFilter result = mapper.toActivityFilter(filterDto);

    // Assert
    assertNotNull(result.sportFilter());
    assertTrue(result.sportFilter() instanceof RunningFilter);
    RunningFilter resultRunningFilter = (RunningFilter) result.sportFilter();
    assertEquals(5.0, resultRunningFilter.minPace());
    assertEquals(7.5, resultRunningFilter.maxPace());

    // We don't need to verify ObjectMapper interactions since we're directly
    // creating the filter objects without using Jackson deserialization
  }

  @Test
  void toActivityFilter_NullSportType_ShouldNotCreateSportFilter() {
    // Arrange
    filterDto.setSportType(null);
    Map<String, Object> sportFilterMap = new HashMap<>();
    sportFilterMap.put("minPace", 5.0);
    filterDto.setSportSpecificFilter(sportFilterMap);

    // Act
    ActivityFilter result = mapper.toActivityFilter(filterDto);

    // Assert
    assertNull(result.sportFilter());

    // We don't use ObjectMapper anymore so no need to verify interactions
  }

  @Test
  void toActivityFilter_SportTypeWithNullFilter_ShouldNotCreateSportFilter() {
    // Arrange
    filterDto.setSportSpecificFilter(null);

    // Act
    ActivityFilter result = mapper.toActivityFilter(filterDto);

    // Assert
    assertNull(result.sportFilter());
  }

  @Test
  void toActivityFilter_InvalidFilterValues_ShouldThrowIllegalArgumentException() {
    // Arrange
    Map<String, Object> sportFilterMap = new HashMap<>();
    sportFilterMap.put("terrain", "INVALID_TERRAIN"); // Invalid enum value
    filterDto.setSportSpecificFilter(sportFilterMap);

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(IllegalArgumentException.class, () -> mapper.toActivityFilter(filterDto));
    assertTrue(exception.getMessage().contains("Invalid sport-specific filter data"));
  }
}

package com.sportsync.activity.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import com.sportsync.activity.Sport;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FilterMetadataServiceTest {

  private final FilterMetadataService filterMetadataService =
      new FilterMetadataService(Map.of(Sport.RUNNING, MockFilter.class));

  @ParameterizedTest
  @MethodSource("fieldTestCases")
  void shouldExtractCorrectMetadata(
      String fieldName, String expectedType, Double expectedMin, Double expectedMax) {
    List<FilterMetadataField> fields = filterMetadataService.getFilterMetadata(Sport.RUNNING);

    FilterMetadataField field =
        fields.stream()
            .filter(f -> f.getFieldName().equals(fieldName))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Field not found: " + fieldName));

    assertThat(field.getType()).isEqualTo(expectedType);

    if (expectedMin != null) assertThat(field.getMin()).isEqualTo(expectedMin);
    if (expectedMax != null) assertThat(field.getMax()).isEqualTo(expectedMax);
  }

  static Stream<Arguments> fieldTestCases() {
    return Stream.of(
        Arguments.of("minPace", "Double", 0.0, null),
        Arguments.of("maxPace", "Double", null, 50.0));
  }

  static class MockFilter {
    @PositiveOrZero Double minPace;

    @Max(50)
    Double maxPace;
  }
}

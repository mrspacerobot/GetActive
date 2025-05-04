package com.sportsync.activity.metadata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.sportsync.activity.Sport;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FilterMetadataControllerTest {

  @Mock private FilterMetadataService filterMetadataService;

  @InjectMocks private FilterMetadataController filterMetadataController;

  private List<FilterMetadataField> testFilterMetadata;

  @BeforeEach
  void setUp() {
    testFilterMetadata =
        List.of(
            FilterMetadataField.builder()
                .min(0.0)
                .max(10.0)
                .fieldName("minPace")
                .type("Double")
                .build());
  }

  @Test
  void getFilterMetadata() {
    when(filterMetadataService.getFilterMetadata(Sport.RUNNING)).thenReturn(testFilterMetadata);

    List<FilterMetadataField> fieldList =
        filterMetadataController.getFilterMetadataField(Sport.RUNNING);

    assertEquals(testFilterMetadata, fieldList);
  }
}

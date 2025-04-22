package com.sportsync.activity.validation;

import static org.junit.jupiter.api.Assertions.*;

import com.sportsync.activity.ActivityCreateDto;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class StartBeforeEndValidatorTest {

  private StartBeforeEndValidator validator;

  @Mock private ConstraintValidatorContext context;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    validator = new StartBeforeEndValidator();
  }

  @Test
  void shouldValidateWhenEndTimeIsAfterStartTime() {
    LocalDateTime now = LocalDateTime.now();

    ActivityCreateDto dto =
        ActivityCreateDto.builder().startTime(now).endTime(now.plusHours(1)).build();

    boolean isValid = validator.isValid(dto, context);

    assertTrue(isValid);
  }

  @Test
  void shouldNotValidateWhenEndTimeIsBeforeStartTime() {
    LocalDateTime now = LocalDateTime.now();
    ActivityCreateDto dto =
        ActivityCreateDto.builder().startTime(now).endTime(now.minusHours(1)).build();

    boolean isValid = validator.isValid(dto, context);

    assertFalse(isValid);
  }

  @Test
  void shouldValidateWhenEndTimeEqualsStartTime() {
    LocalDateTime now = LocalDateTime.now();
    ActivityCreateDto dto = ActivityCreateDto.builder().startTime(now).endTime(now).build();

    boolean isValid = validator.isValid(dto, context);

    assertFalse(isValid);
  }

  @Test
  void shouldValidateWhenStartTimeOrEndTimeIsNull() {
    ActivityCreateDto dto = ActivityCreateDto.builder().startTime(null).build();

    // When & Then
    assertTrue(validator.isValid(dto, context));
  }
}

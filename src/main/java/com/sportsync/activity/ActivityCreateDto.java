package com.sportsync.activity;

import com.sportsync.activity.validation.ValidTimeRange;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ValidTimeRange
public class ActivityCreateDto {
    @NotBlank(message = "Title is required")
    private final String title;

    private final String description;

    @NotNull(message = "Activity type is required") private final String activityType;

    @NotNull(message = "Start time is required") @FutureOrPresent(message = "Start time must be in the present or future")
    private final LocalDateTime startTime;

    @NotNull(message = "End time is required") @Future(message = "End time must be in the future")
    private final LocalDateTime endTime;

    @Min(value = 1, message = "Activity must allow at least 1 participant")
    private final int maxParticipants;

    @NotNull(message = "Latitude is required") @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private final Double latitude;

    @NotNull(message = "Longitude is required") @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private final Double longitude;
}

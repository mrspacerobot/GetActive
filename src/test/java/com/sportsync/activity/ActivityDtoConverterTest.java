package com.sportsync.activity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sportsync.user.User;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ActivityDtoConverterTest {

    private ActivityDtoConverter converter;

    @Mock private GeometryFactory geometryFactory;

    @Mock private Point mockPoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        converter = new ActivityDtoConverter(geometryFactory);

        when(geometryFactory.createPoint(any(Coordinate.class))).thenReturn(mockPoint);
    }

    @Test
    void convertDtoToActivity_shouldConvertAllFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        ActivityCreateDto dto =
                ActivityCreateDto.builder()
                        .title("Basketball Game")
                        .description("Casual basketball game at the park")
                        .activityType("BASKETBALL")
                        .startTime(now)
                        .endTime(now.plusHours(2))
                        .maxParticipants(10)
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .build();

        User creator = new User();
        creator.setId(UUID.randomUUID());
        creator.setName("John Doe");

        Activity activity = converter.convertDtoToActivity(dto, creator);

        assertEquals("Basketball Game", activity.getTitle());
        assertEquals("Casual basketball game at the park", activity.getDescription());
        assertEquals(Sport.BASKETBALL, activity.getActivityType());
        assertEquals(now, activity.getStartTime());
        assertEquals(now.plusHours(2), activity.getEndTime());
        assertEquals(10, activity.getMaxParticipants());
        assertSame(mockPoint, activity.getLocation());
        assertSame(creator, activity.getCreator());

        verify(geometryFactory).createPoint(any(Coordinate.class));
    }

    @Test
    void convertDtoToActivity_shouldHandleNullDescription() {
        LocalDateTime now = LocalDateTime.now();
        ActivityCreateDto dto =
                ActivityCreateDto.builder()
                        .title("Running Club")
                        .activityType("RUNNING")
                        .startTime(now)
                        .endTime(now.plusHours(1))
                        .maxParticipants(20)
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .build();

        User creator = new User();

        Activity activity = converter.convertDtoToActivity(dto, creator);

        assertEquals("Running Club", activity.getTitle());
        assertNull(activity.getDescription());
        assertEquals(Sport.RUNNING, activity.getActivityType());
    }

    @Test
    void convertDtoToActivity_shouldConvertEnumCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        ActivityCreateDto dto =
                ActivityCreateDto.builder()
                        .title("Tennis Match")
                        .activityType("TENNIS")
                        .startTime(now)
                        .endTime(now.plusHours(1))
                        .maxParticipants(4)
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .build();

        User creator = new User();

        Activity activity = converter.convertDtoToActivity(dto, creator);

        assertEquals(Sport.TENNIS, activity.getActivityType());
    }

    @Test
    void convertDtoToActivity_shouldThrowException_whenInvalidActivityType() {
        LocalDateTime now = LocalDateTime.now();
        ActivityCreateDto dto =
                ActivityCreateDto.builder()
                        .title("Invalid Sport")
                        .activityType("NOT_A_SPORT")
                        .startTime(now)
                        .endTime(now.plusHours(1))
                        .maxParticipants(10)
                        .latitude(40.7128)
                        .longitude(-74.0060)
                        .build();

        User creator = new User();

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    converter.convertDtoToActivity(dto, creator);
                });
    }
}

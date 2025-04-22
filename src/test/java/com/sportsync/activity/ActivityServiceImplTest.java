package com.sportsync.activity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sportsync.user.User;
import com.sportsync.user.UserService;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ActivityServiceImplTest {

    @Mock private ActivityRepository activityRepository;

    @Mock private UserService userService;

    private ActivityDtoConverter activityDtoConverter;

    @InjectMocks private ActivityServiceImpl activityServiceImpl;

    private final UUID testUserId = UUID.randomUUID();
    private final GeometryFactory geometryFactory = new GeometryFactory();
    private Point testPoint;

    @BeforeEach
    void setUp() {
        activityDtoConverter = new ActivityDtoConverter(geometryFactory);
        activityServiceImpl =
                new ActivityServiceImpl(activityRepository, userService, activityDtoConverter);

        testPoint = geometryFactory.createPoint(new Coordinate(34.0522, -118.2437));
    }

    @Test
    void createActivity_ValidInput_ReturnsCreatedActivity() {
        // Arrange
        User creator = createTestUser();
        ActivityCreateDto dto = createValidActivityDto();
        Activity expectedActivity = createExpectedActivity(creator, dto);

        when(userService.getUserById(testUserId)).thenReturn(creator);
        when(activityRepository.save(any(Activity.class))).thenReturn(expectedActivity);

        // Act
        Activity result = activityServiceImpl.createActivity(dto, testUserId);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getTitle(), result.getTitle());
        assertEquals(dto.getDescription(), result.getDescription());
        assertEquals(Sport.valueOf(dto.getActivityType()), result.getActivityType());
        assertEquals(dto.getStartTime(), result.getStartTime());
        assertEquals(dto.getEndTime(), result.getEndTime());
        assertEquals(dto.getMaxParticipants(), result.getMaxParticipants());
        assertEquals(creator, result.getCreator());

        verify(userService).getUserById(testUserId);
        verify(activityRepository).save(any(Activity.class));
    }

    @Test
    void filterActivities_ValidFilter_ReturnsFilteredActivities() {
        ActivityFilter filter =
                ActivityFilter.builder().sportFilter(RunningFilter.builder().build()).build();
        Pageable pageable = PageRequest.of(0, 10);

        Activity testActivity = Activity.builder().title("Filtered Activity").build();
        List<Activity> activities = Collections.singletonList(testActivity);
        Page<Activity> activityPage = new PageImpl<>(activities, pageable, activities.size());

        when(activityRepository.findWithFilter(filter, pageable)).thenReturn(activityPage);

        // Act
        Page<Activity> result = activityServiceImpl.filterActivities(filter, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Filtered Activity", result.getContent().get(0).getTitle());

        verify(activityRepository).findWithFilter(filter, pageable);
    }

    private User createTestUser() {
        return User.builder().id(testUserId).name("testuser").build();
    }

    private ActivityCreateDto createValidActivityDto() {
        return ActivityCreateDto.builder()
                .title("Morning Run")
                .description("Running around the park")
                .activityType("RUNNING")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .maxParticipants(5)
                .longitude(34.0522)
                .latitude(-118.2437)
                .build();
    }

    private Activity createExpectedActivity(User creator, ActivityCreateDto dto) {
        return Activity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .activityType(Sport.valueOf(dto.getActivityType()))
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .maxParticipants(dto.getMaxParticipants())
                .location(testPoint)
                .creator(creator)
                .build();
    }
}

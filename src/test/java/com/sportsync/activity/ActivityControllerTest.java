package com.sportsync.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ActivityControllerTest {

  @Mock private ActivityService activityService;

  @Mock private ActivityFilterMapper filterMapper;

  @InjectMocks private ActivityController activityController;

  private Activity testActivity;
  private UUID testUserId;
  private Pageable pageable;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    testUserId = UUID.randomUUID();
    testActivity = new Activity();
    testActivity.setId(UUID.randomUUID()); // Fixed UUID assignment
    testActivity.setTitle("Test Running Activity");

    pageable = Pageable.unpaged();
  }

  @Test
  void createActivity_ShouldReturnCreatedActivity() {
    // Arrange
    ActivityCreateDto createDto = ActivityCreateDto.builder().title("New Running Group").build();

    when(activityService.createActivity(any(ActivityCreateDto.class), eq(testUserId)))
        .thenReturn(testActivity);

    ResponseEntity<Activity> response = activityController.createActivity(createDto, testUserId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(testActivity, response.getBody());
    verify(activityService).createActivity(eq(createDto), eq(testUserId));
  }

  @Test
  void getActivities_WithNoParams_ShouldReturnAllActivities() {
    Page<Activity> activityPage = new PageImpl<>(Collections.singletonList(testActivity));

    when(activityService.filterActivities(any(ActivityFilter.class), any(Pageable.class)))
        .thenReturn(activityPage);

    ResponseEntity<Page<Activity>> response =
        activityController.getActivities(null, null, null, pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(activityPage, response.getBody());
    verify(activityService).filterActivities(any(ActivityFilter.class), eq(pageable));
  }

  @Test
  void filterActivities_ShouldReturnFilteredActivities() {
    ActivityFilterDto filterDto = new ActivityFilterDto();
    filterDto.setSportType(Sport.RUNNING);
    filterDto.setStartDate(LocalDateTime.now());

    ActivityFilter mappedFilter = ActivityFilter.builder().sportType(Sport.RUNNING).build();

    when(filterMapper.toActivityFilter(eq(filterDto))).thenReturn(mappedFilter);

    Page<Activity> activityPage = new PageImpl<>(Collections.singletonList(testActivity));

    when(activityService.filterActivities(eq(mappedFilter), any(Pageable.class)))
        .thenReturn(activityPage);

    ResponseEntity<Page<Activity>> response =
        activityController.filterActivities(filterDto, pageable);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(activityPage, response.getBody());
    verify(filterMapper).toActivityFilter(eq(filterDto));
    verify(activityService).filterActivities(eq(mappedFilter), eq(pageable));
  }

  @Test
  void joinActivity_ShouldReturnOkStatus() {
    Long activityId = 1L;

    ResponseEntity<Void> response = activityController.joinActivity(activityId, testUserId);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(activityService).joinActivity(eq(activityId), eq(testUserId));
  }
}

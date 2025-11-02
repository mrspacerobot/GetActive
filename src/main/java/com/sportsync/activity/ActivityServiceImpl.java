package com.sportsync.activity;

import com.sportsync.activity.filter.ActivityFilter;
import com.sportsync.user.User;
import com.sportsync.user.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActivityServiceImpl implements ActivityService {
  private final ActivityRepository activityRepository;
  private final UserService userService;
  private final ActivityDtoConverter activityDtoConverter;

  public ActivityServiceImpl(
      ActivityRepository activityRepository,
      UserService userService,
      ActivityDtoConverter activityDtoConverter) {
    this.activityRepository = activityRepository;
    this.userService = userService;
    this.activityDtoConverter = activityDtoConverter;
  }

  @Transactional
  public Activity createActivity(ActivityCreateDto dto, UUID creatorId) {
    User user = userService.getUserById(creatorId);
    Activity activity = activityDtoConverter.convertDtoToActivity(dto, user);

    return activityRepository.save(activity);
  }

  @Override
  public Page<Activity> getAllActivities(Pageable pageable) {
    return null;
  }

  @Override
  public Page<Activity> filterActivities(ActivityFilter filter, Pageable pageable) {
    return activityRepository.findWithFilter(filter, pageable);
  }

  @Override
  public Page<Activity> findNearbyActivities(
      Double lat,
      Double lon,
      Double distance,
      LocalDateTime start,
      LocalDateTime end,
      Pageable pageable) {
    return null;
  }

  public Page<Activity> searchActivities(String query, Pageable pageable) {
    // return activityRepository.search(query, pageable);
    return null;
  }

  public List<Activity> findNearbyActivities(
      double lat, double lon, double distance, LocalDateTime start, LocalDateTime end) {
    return activityRepository.findNearby(lat, lon, distance, start, end, 100);
  }

  public List<Activity> findNearbyActivities(ActivityFilter filter) {
    return null;
  }

  @Transactional
  public void joinActivity(Long activityId, UUID userId) {
    Activity activity =
        activityRepository
            .findById(activityId)
            .orElseThrow(() -> new RuntimeException("Activity not found"));

    User user = userService.getUserById(userId);

    if (activity.getParticipants().size() >= activity.getMaxParticipants()) {
      throw new RuntimeException("Activity is full");
    }

    activity.getParticipants().add(user);
    activityRepository.save(activity);
  }
}
